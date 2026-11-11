package com.laterbox.data.model

import com.laterbox.data.model.richnote.ListType
import com.laterbox.data.model.richnote.RichBlock
import com.laterbox.data.model.richnote.RichDocument
import com.laterbox.data.model.richnote.RichSpan
import com.laterbox.data.model.richnote.RichTextFormatter
import com.laterbox.data.model.richnote.SpanType
import com.laterbox.data.model.richnote.TextAlignment
import com.laterbox.data.model.richnote.TextStyleType
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File

class RichDocumentTest {

    @Test
    fun testRichDocument_toJson_and_fromJson_roundtrip() {
        val spans = listOf(
            RichSpan(type = SpanType.BOLD, start = 0, end = 12),
            RichSpan(type = SpanType.ITALIC, start = 5, end = 20),
            RichSpan(type = SpanType.LINK, start = 25, end = 35, linkUrl = "https://example.com")
        )

        val blocks = listOf(
            RichBlock.Text(
                id = "block-1",
                text = "Meeting with product and design team",
                spans = spans,
                style = TextStyleType.TITLE,
                alignment = TextAlignment.LEFT,
                listType = ListType.NONE
            ),
            RichBlock.Image(
                id = "block-2",
                localPath = "/data/user/0/com.laterbox/files/media/wireframe.png",
                fileName = "wireframe.png",
                caption = "Initial mobile wireframe mockup"
            ),
            RichBlock.Text(
                id = "block-3",
                text = "Review user journey",
                style = TextStyleType.NORMAL,
                listType = ListType.CHECKLIST,
                isChecked = true
            ),
            RichBlock.Text(
                id = "block-4",
                text = "Approve final release candidate",
                style = TextStyleType.NORMAL,
                listType = ListType.CHECKLIST,
                isChecked = false
            ),
            RichBlock.Text(
                id = "block-5",
                text = "First priority action item",
                style = TextStyleType.NORMAL,
                listType = ListType.NUMBERED,
                listIndex = 1
            ),
            RichBlock.Text(
                id = "block-6",
                text = "Second priority action item",
                style = TextStyleType.NORMAL,
                listType = ListType.NUMBERED,
                listIndex = 2
            ),
            RichBlock.Text(
                id = "block-7",
                text = "Key takeaway bullet point",
                style = TextStyleType.NORMAL,
                listType = ListType.BULLET
            )
        )

        val originalDoc = RichDocument(
            version = 1,
            title = "Project Launch Plan",
            blocks = blocks
        )

        val json = originalDoc.toJson()
        assertTrue(json.contains("\"type\":\"rich_note\""))
        assertTrue(json.contains("Project Launch Plan"))

        // Simulate portable restoration in a new media directory
        val mockMediaDir = File("/mock/restored/media")
        val restoredDoc = RichDocument.fromJson(json, mockMediaDir)

        assertEquals(1, restoredDoc.version)
        assertEquals("Project Launch Plan", restoredDoc.title)
        assertEquals(7, restoredDoc.blocks.size)

        // Block 1: Title with mixed spans
        val b1 = restoredDoc.blocks[0] as RichBlock.Text
        assertEquals("Meeting with product and design team", b1.text)
        assertEquals(TextStyleType.TITLE, b1.style)
        assertEquals(3, b1.spans.size)
        assertEquals(SpanType.BOLD, b1.spans[0].type)
        assertEquals(SpanType.LINK, b1.spans[2].type)
        assertEquals("https://example.com", b1.spans[2].linkUrl)

        // Block 2: Image block with portable path resolution
        val b2 = restoredDoc.blocks[1] as RichBlock.Image
        assertEquals("wireframe.png", b2.fileName)
        assertEquals("Initial mobile wireframe mockup", b2.caption)
        assertEquals(File(mockMediaDir, "wireframe.png").absolutePath, b2.localPath)

        // Block 3 & 4: Checklists with completion state
        val b3 = restoredDoc.blocks[2] as RichBlock.Text
        assertEquals(ListType.CHECKLIST, b3.listType)
        assertTrue(b3.isChecked)

        val b4 = restoredDoc.blocks[3] as RichBlock.Text
        assertEquals(ListType.CHECKLIST, b4.listType)
        assertFalse(b4.isChecked)

        // Block 5 & 6: Numbered list with indices
        val b5 = restoredDoc.blocks[4] as RichBlock.Text
        assertEquals(ListType.NUMBERED, b5.listType)
        assertEquals(1, b5.listIndex)

        val b6 = restoredDoc.blocks[5] as RichBlock.Text
        assertEquals(ListType.NUMBERED, b6.listType)
        assertEquals(2, b6.listIndex)

        // Block 7: Bullet
        val b7 = restoredDoc.blocks[6] as RichBlock.Text
        assertEquals(ListType.BULLET, b7.listType)
    }

    @Test
    fun testLegacyPlainTextNotes_parsedWithoutDataLoss() {
        val legacyText = """
            # Architecture Review
            ## Frontend Modules
            - Implement Rich Notes Editor
            - Add undo and redo operations
            1. First step: define Room schema
            2. Second step: update ViewModel
            [ ] Untested item
            [x] Completed test item
            Just regular paragraph content.
        """.trimIndent()

        val doc = RichDocument.fromJson(legacyText)
        assertNotNull(doc)
        assertEquals(9, doc.blocks.size)

        // Block 0: Title (# Architecture Review)
        val b0 = doc.blocks[0] as RichBlock.Text
        assertEquals("Architecture Review", b0.text)
        assertEquals(TextStyleType.TITLE, b0.style)

        // Block 1: Heading 1 (## Frontend Modules)
        val b1 = doc.blocks[1] as RichBlock.Text
        assertEquals("Frontend Modules", b1.text)
        assertEquals(TextStyleType.HEADING_1, b1.style)

        // Block 2 & 3: Bullets (- ...)
        val b2 = doc.blocks[2] as RichBlock.Text
        assertEquals("Implement Rich Notes Editor", b2.text)
        assertEquals(ListType.BULLET, b2.listType)

        // Block 4 & 5: Numbered
        val b4 = doc.blocks[4] as RichBlock.Text
        assertEquals("First step: define Room schema", b4.text)
        assertEquals(ListType.NUMBERED, b4.listType)
        assertEquals(1, b4.listIndex)

        val b5 = doc.blocks[5] as RichBlock.Text
        assertEquals("Second step: update ViewModel", b5.text)
        assertEquals(ListType.NUMBERED, b5.listType)
        assertEquals(2, b5.listIndex)

        // Block 6 & 7: Checklists
        val b6 = doc.blocks[6] as RichBlock.Text
        assertEquals("Untested item", b6.text)
        assertEquals(ListType.CHECKLIST, b6.listType)
        assertFalse(b6.isChecked)

        val b7 = doc.blocks[7] as RichBlock.Text
        assertEquals("Completed test item", b7.text)
        assertEquals(ListType.CHECKLIST, b7.listType)
        assertTrue(b7.isChecked)
    }

    @Test
    fun testEmptyOrNullInput_yieldsDefaultEmptyTextBlock() {
        val docNull = RichDocument.fromJson(null)
        assertEquals(1, docNull.blocks.size)
        assertTrue(docNull.blocks[0] is RichBlock.Text)
        assertEquals("", (docNull.blocks[0] as RichBlock.Text).text)

        val docBlank = RichDocument.fromJson("   ")
        assertEquals(1, docBlank.blocks.size)
        assertTrue(docBlank.blocks[0] is RichBlock.Text)
        assertEquals("", (docBlank.blocks[0] as RichBlock.Text).text)
    }

    @Test
    fun testToPlainText_generatesReadableFormattedOutput() {
        val doc = RichDocument(
            title = "Shopping List",
            blocks = listOf(
                RichBlock.Text(text = "Groceries", style = TextStyleType.HEADING_1),
                RichBlock.Text(text = "Apples", listType = ListType.BULLET),
                RichBlock.Text(text = "Milk", listType = ListType.CHECKLIST, isChecked = true),
                RichBlock.Text(text = "Bread", listType = ListType.CHECKLIST, isChecked = false),
                RichBlock.Image(caption = "Bakery storefront"),
                RichBlock.Text(text = "Checkout at counter 3", listType = ListType.NUMBERED, listIndex = 1)
            )
        )

        val plainText = doc.toPlainText()
        assertTrue(plainText.contains("Shopping List"))
        assertTrue(plainText.contains("## Groceries"))
        assertTrue(plainText.contains("• Apples"))
        assertTrue(plainText.contains("[✓] Milk"))
        assertTrue(plainText.contains("[ ] Bread"))
        assertTrue(plainText.contains("[Image: Bakery storefront]"))
        assertTrue(plainText.contains("1. Checkout at counter 3"))
    }

    @Test
    fun testToggleSpan_addAndRemoveFormatting() {
        val initialSpans = emptyList<RichSpan>()

        // 1. Add Bold on range [5, 15)
        val withBold = RichTextFormatter.toggleSpan(
            spans = initialSpans,
            type = SpanType.BOLD,
            start = 5,
            end = 15
        )
        assertEquals(1, withBold.size)
        assertEquals(SpanType.BOLD, withBold[0].type)
        assertEquals(5, withBold[0].start)
        assertEquals(15, withBold[0].end)

        // 2. Add Italic overlapping on range [10, 20)
        val withItalic = RichTextFormatter.toggleSpan(
            spans = withBold,
            type = SpanType.ITALIC,
            start = 10,
            end = 20
        )
        assertEquals(2, withItalic.size)

        // 3. Toggle Bold OFF on range [5, 15)
        val removedBold = RichTextFormatter.toggleSpan(
            spans = withItalic,
            type = SpanType.BOLD,
            start = 5,
            end = 15
        )
        assertEquals(1, removedBold.size)
        assertEquals(SpanType.ITALIC, removedBold[0].type)
    }

    @Test
    fun testAdjustSpansOnTextChange_insertionAndDeletion() {
        val originalSpans = listOf(
            RichSpan(type = SpanType.BOLD, start = 10, end = 20)
        )

        // Test insertion before span: inserting 5 chars at index 0 shifts span to [15, 25)
        val shifted = RichTextFormatter.adjustSpansOnTextChange(
            spans = originalSpans,
            oldText = "012345678901234567890",
            newText = "ABCDE012345678901234567890",
            cursorPos = 0
        )
        assertEquals(1, shifted.size)
        assertEquals(15, shifted[0].start)
        assertEquals(25, shifted[0].end)

        // Test deletion before span: deleting 5 chars at index 0 shifts span back to [5, 15)
        val shiftedBack = RichTextFormatter.adjustSpansOnTextChange(
            spans = originalSpans,
            oldText = "ABCDE012345678901234567890",
            newText = "012345678901234567890",
            cursorPos = 0
        )
        assertEquals(1, shiftedBack.size)
        assertEquals(5, shiftedBack[0].start)
        assertEquals(15, shiftedBack[0].end)
    }
}
