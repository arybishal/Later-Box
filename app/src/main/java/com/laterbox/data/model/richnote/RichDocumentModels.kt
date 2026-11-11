package com.laterbox.data.model.richnote

import org.json.JSONArray
import org.json.JSONObject
import java.io.File
import java.util.UUID

/**
 * Span formatting types for rich inline text.
 */
enum class SpanType {
    BOLD,
    ITALIC,
    UNDERLINE,
    STRIKETHROUGH,
    LINK
}

/**
 * A formatted span applied to a character range [start, end).
 */
data class RichSpan(
    val type: SpanType,
    val start: Int,
    val end: Int,
    val linkUrl: String? = null
) {
    fun toJson(): JSONObject {
        return JSONObject().apply {
            put("type", type.name)
            put("start", start)
            put("end", end)
            if (linkUrl != null) put("linkUrl", linkUrl)
        }
    }

    companion object {
        fun fromJson(json: JSONObject): RichSpan? {
            val typeStr = json.optString("type")
            val type = try { SpanType.valueOf(typeStr) } catch (_: Exception) { return null }
            val start = json.optInt("start", 0)
            val end = json.optInt("end", 0)
            val linkUrl = json.optString("linkUrl").takeIf { it.isNotBlank() }
            if (end <= start) return null
            return RichSpan(type = type, start = start, end = end, linkUrl = linkUrl)
        }
    }
}

/**
 * Structural text style hierarchy.
 */
enum class TextStyleType {
    NORMAL,
    TITLE,
    HEADING_1,
    HEADING_2
}

/**
 * Paragraph text alignment.
 */
enum class TextAlignment {
    LEFT,
    CENTER,
    RIGHT
}

/**
 * List formatting types.
 */
enum class ListType {
    NONE,
    BULLET,
    NUMBERED,
    CHECKLIST
}

/**
 * Base sealed class for rich document blocks.
 */
sealed class RichBlock {
    abstract val id: String

    data class Text(
        override val id: String = UUID.randomUUID().toString(),
        val text: String = "",
        val spans: List<RichSpan> = emptyList(),
        val style: TextStyleType = TextStyleType.NORMAL,
        val alignment: TextAlignment = TextAlignment.LEFT,
        val listType: ListType = ListType.NONE,
        val listIndex: Int = 1,
        val isChecked: Boolean = false
    ) : RichBlock() {
        fun toJson(): JSONObject {
            return JSONObject().apply {
                put("type", "text")
                put("id", id)
                put("text", text)
                put("style", style.name)
                put("alignment", alignment.name)
                put("listType", listType.name)
                put("listIndex", listIndex)
                put("isChecked", isChecked)

                val spansArray = JSONArray()
                spans.forEach { span ->
                    spansArray.put(span.toJson())
                }
                put("spans", spansArray)
            }
        }
    }

    data class Image(
        override val id: String = UUID.randomUUID().toString(),
        val localPath: String = "",
        val fileName: String = "",
        val caption: String = "",
        val widthRatio: Float = 1.0f
    ) : RichBlock() {
        fun toJson(): JSONObject {
            return JSONObject().apply {
                put("type", "image")
                put("id", id)
                put("fileName", fileName)
                put("localPath", localPath)
                put("caption", caption)
                put("widthRatio", widthRatio.toDouble())
            }
        }
    }
}

/**
 * The structured, versioned Rich Document model.
 */
data class RichDocument(
    val version: Int = 1,
    val title: String = "",
    val blocks: List<RichBlock> = listOf(RichBlock.Text())
) {
    /**
     * Converts this rich document into a formatted plain-text string.
     * Preserves headings, lists, checklist indicators, captions, and paragraph spacing.
     */
    fun toPlainText(): String {
        val sb = StringBuilder()
        if (title.isNotBlank()) {
            sb.appendLine(title)
            sb.appendLine()
        }

        blocks.forEachIndexed { index, block ->
            when (block) {
                is RichBlock.Text -> {
                    val prefix = when (block.listType) {
                        ListType.BULLET -> "• "
                        ListType.NUMBERED -> "${block.listIndex}. "
                        ListType.CHECKLIST -> if (block.isChecked) "[✓] " else "[ ] "
                        ListType.NONE -> when (block.style) {
                            TextStyleType.TITLE -> "# "
                            TextStyleType.HEADING_1 -> "## "
                            TextStyleType.HEADING_2 -> "### "
                            TextStyleType.NORMAL -> ""
                        }
                    }
                    sb.append(prefix).append(block.text)
                    if (index < blocks.lastIndex) {
                        sb.appendLine()
                    }
                }
                is RichBlock.Image -> {
                    val imgDesc = if (block.caption.isNotBlank()) "[Image: ${block.caption}]" else "[Image]"
                    sb.append(imgDesc)
                    if (index < blocks.lastIndex) {
                        sb.appendLine()
                    }
                }
            }
        }
        return sb.toString().trim()
    }

    /**
     * Serializes the document into versioned JSON.
     */
    fun toJson(): String {
        val root = JSONObject().apply {
            put("version", version)
            put("type", "rich_note")
            put("title", title)

            val blocksArray = JSONArray()
            blocks.forEach { block ->
                when (block) {
                    is RichBlock.Text -> blocksArray.put(block.toJson())
                    is RichBlock.Image -> blocksArray.put(block.toJson())
                }
            }
            put("blocks", blocksArray)
        }
        return root.toString()
    }

    companion object {
        private val NUMBERED_LIST_REGEX = Regex("""^(\d+)\.\s+(.*)""")

        /**
         * Parses a string into a RichDocument.
         * If the string is in rich note JSON format, parses all blocks, styles, spans, and images.
         * If the string is plain text (legacy notes), gracefully parses lines into structured paragraphs and lists.
         */
        fun fromJson(jsonStr: String?, mediaDir: File? = null): RichDocument {
            if (jsonStr.isNullOrBlank()) {
                return RichDocument(blocks = listOf(RichBlock.Text()))
            }

            val trimmed = jsonStr.trim()
            if (trimmed.startsWith("{") && trimmed.contains("\"type\":\"rich_note\"")) {
                try {
                    val root = JSONObject(trimmed)
                    val version = root.optInt("version", 1)
                    val title = root.optString("title", "")
                    val blocksArray = root.optJSONArray("blocks") ?: JSONArray()
                    val blocks = mutableListOf<RichBlock>()

                    for (i in 0 until blocksArray.length()) {
                        val blockObj = blocksArray.optJSONObject(i) ?: continue
                        when (blockObj.optString("type")) {
                            "text" -> {
                                val id = blockObj.optString("id", UUID.randomUUID().toString())
                                val text = blockObj.optString("text", "")
                                val style = try {
                                    TextStyleType.valueOf(blockObj.optString("style", "NORMAL"))
                                } catch (_: Exception) {
                                    TextStyleType.NORMAL
                                }
                                val alignment = try {
                                    TextAlignment.valueOf(blockObj.optString("alignment", "LEFT"))
                                } catch (_: Exception) {
                                    TextAlignment.LEFT
                                }
                                val listType = try {
                                    ListType.valueOf(blockObj.optString("listType", "NONE"))
                                } catch (_: Exception) {
                                    ListType.NONE
                                }
                                val listIndex = blockObj.optInt("listIndex", 1)
                                val isChecked = blockObj.optBoolean("isChecked", false)

                                val spansList = mutableListOf<RichSpan>()
                                val spansArray = blockObj.optJSONArray("spans")
                                if (spansArray != null) {
                                    for (j in 0 until spansArray.length()) {
                                        val sObj = spansArray.optJSONObject(j) ?: continue
                                        RichSpan.fromJson(sObj)?.let { spansList.add(it) }
                                    }
                                }

                                blocks.add(
                                    RichBlock.Text(
                                        id = id,
                                        text = text,
                                        spans = spansList,
                                        style = style,
                                        alignment = alignment,
                                        listType = listType,
                                        listIndex = listIndex,
                                        isChecked = isChecked
                                    )
                                )
                            }
                            "image" -> {
                                val id = blockObj.optString("id", UUID.randomUUID().toString())
                                val fileName = blockObj.optString("fileName", "")
                                val storedLocalPath = blockObj.optString("localPath", "")
                                // Portable resolution: if mediaDir is provided and fileName is present, resolve to current mediaDir
                                val resolvedPath = if (mediaDir != null && fileName.isNotBlank()) {
                                    File(mediaDir, fileName).absolutePath
                                } else {
                                    storedLocalPath
                                }
                                val caption = blockObj.optString("caption", "")
                                val widthRatio = blockObj.optDouble("widthRatio", 1.0).toFloat()

                                blocks.add(
                                    RichBlock.Image(
                                        id = id,
                                        localPath = resolvedPath,
                                        fileName = fileName,
                                        caption = caption,
                                        widthRatio = widthRatio
                                    )
                                )
                            }
                        }
                    }

                    if (blocks.isEmpty()) {
                        blocks.add(RichBlock.Text())
                    }

                    return RichDocument(
                        version = version,
                        title = title,
                        blocks = blocks
                    )
                } catch (_: Exception) {
                    // Fall back to plain-text parsing if JSON is malformed
                }
            }

            // Legacy plain text parsing
            return fromPlainText(trimmed)
        }

        /**
         * Converts plain text into a structured RichDocument without losing content.
         */
        fun fromPlainText(plainText: String): RichDocument {
            if (plainText.isBlank()) {
                return RichDocument(blocks = listOf(RichBlock.Text()))
            }

            val lines = plainText.lines()
            val blocks = mutableListOf<RichBlock>()
            var numberedCounter = 1

            for (rawLine in lines) {
                val line = rawLine.trimEnd()
                when {
                    line.startsWith("### ") -> {
                        blocks.add(RichBlock.Text(text = line.removePrefix("### "), style = TextStyleType.HEADING_2))
                        numberedCounter = 1
                    }
                    line.startsWith("## ") -> {
                        blocks.add(RichBlock.Text(text = line.removePrefix("## "), style = TextStyleType.HEADING_1))
                        numberedCounter = 1
                    }
                    line.startsWith("# ") -> {
                        blocks.add(RichBlock.Text(text = line.removePrefix("# "), style = TextStyleType.TITLE))
                        numberedCounter = 1
                    }
                    line.startsWith("- ") || line.startsWith("* ") || line.startsWith("• ") -> {
                        val content = line.substring(2)
                        blocks.add(RichBlock.Text(text = content, listType = ListType.BULLET))
                        numberedCounter = 1
                    }
                    line.startsWith("[ ] ") -> {
                        blocks.add(RichBlock.Text(text = line.removePrefix("[ ] "), listType = ListType.CHECKLIST, isChecked = false))
                        numberedCounter = 1
                    }
                    line.startsWith("[x] ", ignoreCase = true) || line.startsWith("[✓] ") -> {
                        val content = if (line.startsWith("[x] ", ignoreCase = true)) line.substring(4) else line.removePrefix("[✓] ")
                        blocks.add(RichBlock.Text(text = content, listType = ListType.CHECKLIST, isChecked = true))
                        numberedCounter = 1
                    }
                    NUMBERED_LIST_REGEX.matches(line) -> {
                        val match = NUMBERED_LIST_REGEX.find(line)!!
                        val index = match.groupValues[1].toIntOrNull() ?: numberedCounter
                        val content = match.groupValues[2]
                        blocks.add(RichBlock.Text(text = content, listType = ListType.NUMBERED, listIndex = index))
                        numberedCounter = index + 1
                    }
                    else -> {
                        blocks.add(RichBlock.Text(text = line, style = TextStyleType.NORMAL))
                        numberedCounter = 1
                    }
                }
            }

            if (blocks.isEmpty()) {
                blocks.add(RichBlock.Text())
            }

            return RichDocument(blocks = blocks)
        }
    }
}
