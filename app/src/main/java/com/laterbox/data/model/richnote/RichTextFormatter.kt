package com.laterbox.data.model.richnote

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration

/**
 * Utility functions for rendering and manipulating formatted spans on RichBlock.Text.
 */
object RichTextFormatter {

    /**
     * Converts a raw text string and its spans into a Compose AnnotatedString.
     */
    fun toAnnotatedString(
        text: String,
        spans: List<RichSpan>,
        linkColor: Color = Color(0xFF1687D9)
    ): AnnotatedString {
        if (text.isEmpty()) return AnnotatedString("")
        if (spans.isEmpty()) return AnnotatedString(text)

        return buildAnnotatedString {
            append(text)
            val textLen = text.length

            for (span in spans) {
                val clampedStart = span.start.coerceIn(0, textLen)
                val clampedEnd = span.end.coerceIn(0, textLen)
                if (clampedStart >= clampedEnd) continue

                when (span.type) {
                    SpanType.BOLD -> {
                        addStyle(
                            style = SpanStyle(fontWeight = FontWeight.Bold),
                            start = clampedStart,
                            end = clampedEnd
                        )
                    }
                    SpanType.ITALIC -> {
                        addStyle(
                            style = SpanStyle(fontStyle = FontStyle.Italic),
                            start = clampedStart,
                            end = clampedEnd
                        )
                    }
                    SpanType.UNDERLINE -> {
                        addStyle(
                            style = SpanStyle(textDecoration = TextDecoration.Underline),
                            start = clampedStart,
                            end = clampedEnd
                        )
                    }
                    SpanType.STRIKETHROUGH -> {
                        addStyle(
                            style = SpanStyle(textDecoration = TextDecoration.LineThrough),
                            start = clampedStart,
                            end = clampedEnd
                        )
                    }
                    SpanType.LINK -> {
                        addStyle(
                            style = SpanStyle(
                                color = linkColor,
                                textDecoration = TextDecoration.Underline
                            ),
                            start = clampedStart,
                            end = clampedEnd
                        )
                        span.linkUrl?.let { url ->
                            addStringAnnotation(
                                tag = "URL",
                                annotation = url,
                                start = clampedStart,
                                end = clampedEnd
                            )
                        }
                    }
                }
            }
        }
    }

    /**
     * Toggles a span over the range [start, end).
     * If the range already has this span, it removes it from that range.
     * Otherwise, it applies the span to that range and merges adjacent/overlapping spans.
     */
    fun toggleSpan(
        spans: List<RichSpan>,
        type: SpanType,
        start: Int,
        end: Int,
        linkUrl: String? = null
    ): List<RichSpan> {
        if (start >= end) return spans

        val isAlreadyCovered = spans.any { it.type == type && it.start <= start && it.end >= end }
        val otherTypeSpans = spans.filter { it.type != type }
        val sameTypeSpans = spans.filter { it.type == type }

        val newSameTypeSpans = mutableListOf<RichSpan>()

        if (isAlreadyCovered) {
            // Remove the span from the range [start, end)
            for (span in sameTypeSpans) {
                if (span.end <= start || span.start >= end) {
                    // Completely outside
                    newSameTypeSpans.add(span)
                } else {
                    // Overlaps
                    if (span.start < start) {
                        newSameTypeSpans.add(span.copy(end = start))
                    }
                    if (span.end > end) {
                        newSameTypeSpans.add(span.copy(start = end))
                    }
                }
            }
        } else {
            // Add the span to the range [start, end) and merge overlapping
            val candidates = sameTypeSpans.toMutableList()
            candidates.add(RichSpan(type = type, start = start, end = end, linkUrl = linkUrl))
            candidates.sortBy { it.start }

            var current = candidates.first()
            for (i in 1 until candidates.size) {
                val next = candidates[i]
                if (next.start <= current.end) {
                    // Merge
                    val mergedEnd = maxOf(current.end, next.end)
                    val mergedUrl = linkUrl ?: current.linkUrl ?: next.linkUrl
                    current = current.copy(end = mergedEnd, linkUrl = mergedUrl)
                } else {
                    newSameTypeSpans.add(current)
                    current = next
                }
            }
            newSameTypeSpans.add(current)
        }

        return otherTypeSpans + newSameTypeSpans
    }

    /**
     * Removes all spans covering the specified range.
     */
    fun clearFormatting(spans: List<RichSpan>, start: Int, end: Int): List<RichSpan> {
        if (start >= end) return spans
        val result = mutableListOf<RichSpan>()
        for (span in spans) {
            if (span.end <= start || span.start >= end) {
                result.add(span)
            } else {
                if (span.start < start) {
                    result.add(span.copy(end = start))
                }
                if (span.end > end) {
                    result.add(span.copy(start = end))
                }
            }
        }
        return result
    }

    /**
     * Adjusts spans after a text edit (insertion or deletion) in a block.
     */
    fun adjustSpansOnTextChange(
        spans: List<RichSpan>,
        oldText: String,
        newText: String,
        cursorPos: Int
    ): List<RichSpan> {
        val diff = newText.length - oldText.length
        if (diff == 0) return spans.map { it.copy(end = it.end.coerceAtMost(newText.length)) }

        val newSpans = mutableListOf<RichSpan>()
        for (span in spans) {
            if (diff > 0) {
                // Text inserted
                if (span.start >= cursorPos) {
                    newSpans.add(span.copy(start = span.start + diff, end = span.end + diff))
                } else if (span.end >= cursorPos) {
                    newSpans.add(span.copy(end = span.end + diff))
                } else {
                    newSpans.add(span)
                }
            } else {
                // Text deleted
                val deleteStart = cursorPos
                val deleteEnd = cursorPos - diff // diff is negative
                if (span.end <= deleteStart) {
                    newSpans.add(span)
                } else if (span.start >= deleteEnd) {
                    newSpans.add(span.copy(start = span.start + diff, end = span.end + diff))
                } else {
                    // Span was inside or overlapping deleted region
                    val newStart = minOf(span.start, deleteStart)
                    val newEnd = maxOf(newStart, span.end + diff)
                    if (newEnd > newStart) {
                        newSpans.add(span.copy(start = newStart, end = newEnd))
                    }
                }
            }
        }
        return newSpans.filter { it.start < it.end && it.end <= newText.length }
    }
}
