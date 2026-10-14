package com.king_grey.notes_app.ui.screens.notes

import androidx.compose.runtime.Immutable

// ─────────────────────────────────────────────────────────────────────────────
// Document model
//
// A note is a title and a list of blocks. Text and checklist blocks carry inline formatting as
// ranges (InlineSpan). Everything in this file is plain Kotlin, no Compose state, so the editing
// rules can be unit tested.
// ─────────────────────────────────────────────────────────────────────────────

/** The four paragraph styles in the format popup. */
enum class ParagraphStyle { Title, Heading, Subheading, Body }

/** The four inline toggles in the format popup: B, I, U, S. */
enum class SpanKind { Bold, Italic, Underline, Strike }

/** Formatting on [start, end) of a block's text. */
@Immutable
data class InlineSpan(val start: Int, val end: Int, val kind: SpanKind)

/** One pen stroke. Points are 0..1 of the drawing's width and height, so any size can replay it. */
@Immutable
data class DrawingStroke(
    val xs: List<Float>,
    val ys: List<Float>,
    /** ARGB. */
    val color: Long,
    /** In 1/320ths of the drawing width, so it scales with the drawing. */
    val width: Float,
    val highlighter: Boolean = false,
)

@Immutable
sealed interface NoteBlock {
    val id: Long

    @Immutable
    data class Text(
        override val id: Long,
        val text: String = "",
        val style: ParagraphStyle = ParagraphStyle.Body,
        val spans: List<InlineSpan> = emptyList(),
    ) : NoteBlock

    /** A checklist row. */
    @Immutable
    data class Check(
        override val id: Long,
        val text: String = "",
        val checked: Boolean = false,
        val spans: List<InlineSpan> = emptyList(),
    ) : NoteBlock

    @Immutable
    data class Image(override val id: Long, val uri: String) : NoteBlock

    @Immutable
    data class Link(override val id: Long, val title: String, val host: String, val url: String) :
        NoteBlock

    /** [cells] is rows of columns. The first row is the header. */
    @Immutable
    data class Table(override val id: Long, val cells: List<List<String>>) : NoteBlock

    @Immutable
    data class Drawing(override val id: Long, val strokes: List<DrawingStroke>) : NoteBlock
}

fun List<NoteBlock>.nextId(): Long = (maxOfOrNull { it.id } ?: 0L) + 1L

/** Text of a text or checklist block, null for the others. */
internal fun NoteBlock.textOrNull(): String? = when (this) {
    is NoteBlock.Text -> text
    is NoteBlock.Check -> text
    else -> null
}

internal fun NoteBlock.spansOrNull(): List<InlineSpan>? = when (this) {
    is NoteBlock.Text -> spans
    is NoteBlock.Check -> spans
    else -> null
}

/** A copy of a text or checklist block with new text and spans. Other blocks come back as they are. */
internal fun NoteBlock.withText(text: String, spans: List<InlineSpan>): NoteBlock = when (this) {
    is NoteBlock.Text -> copy(text = text, spans = spans)
    is NoteBlock.Check -> copy(text = text, spans = spans)
    else -> this
}

/** The note as plain text, for sharing and for the list preview. */
fun noteToPlainText(title: String, blocks: List<NoteBlock>): String = buildString {
    if (title.isNotBlank()) appendLine(title.trim())
    blocks.forEach { block ->
        when (block) {
            is NoteBlock.Text -> if (block.text.isNotBlank()) appendLine(block.text)
            is NoteBlock.Check -> appendLine((if (block.checked) "[x] " else "[ ] ") + block.text)
            is NoteBlock.Link -> appendLine(block.url)
            is NoteBlock.Table -> block.cells.forEach { row -> appendLine(row.joinToString("\t")) }
            is NoteBlock.Image, is NoteBlock.Drawing -> Unit
        }
    }
}.trimEnd()

/** First line of body text, for the preview under a title in the list. */
fun notePreview(blocks: List<NoteBlock>): String =
    blocks.firstNotNullOfOrNull { block ->
        block.textOrNull()?.trim()?.takeIf { it.isNotEmpty() }
    }.orEmpty()

// ─────────────────────────────────────────────────────────────────────────────
// Inline formatting
// ─────────────────────────────────────────────────────────────────────────────

/** Clamps to [0, length], drops empty ranges and merges overlapping or touching ranges of one kind. */
fun normalizeSpans(spans: List<InlineSpan>, length: Int): List<InlineSpan> {
    val out = ArrayList<InlineSpan>()
    for (kind in SpanKind.entries) {
        val ofKind = spans
            .filter { it.kind == kind }
            .map { InlineSpan(it.start.coerceIn(0, length), it.end.coerceIn(0, length), kind) }
            .filter { it.end > it.start }
            .sortedBy { it.start }
        var current: InlineSpan? = null
        for (span in ofKind) {
            val c = current
            if (c == null) {
                current = span
            } else if (span.start <= c.end) {
                current = c.copy(end = maxOf(c.end, span.end))
            } else {
                out += c
                current = span
            }
        }
        current?.let { out += it }
    }
    return out.sortedWith(compareBy({ it.start }, { it.kind }))
}

/** Kinds that apply to text typed with the cursor at [position]: spans that end there or run past it. */
fun kindsAt(spans: List<InlineSpan>, position: Int): Set<SpanKind> =
    spans.filter { it.start < position && position <= it.end }.map { it.kind }.toSet()

/** True when every character of [start, end) has [kind]. */
fun isCovered(spans: List<InlineSpan>, start: Int, end: Int, kind: SpanKind): Boolean =
    end > start && spans.any { it.kind == kind && it.start <= start && it.end >= end }

/** Kinds that cover the whole of [start, end) (or, with a cursor, the kinds at that spot). */
fun kindsOn(spans: List<InlineSpan>, start: Int, end: Int): Set<SpanKind> =
    if (end > start) {
        SpanKind.entries.filter { isCovered(spans, start, end, it) }.toSet()
    } else {
        kindsAt(spans, start)
    }

/** Turns [kind] on over [start, end), or off if all of it already has it. */
fun toggleKind(
    spans: List<InlineSpan>,
    start: Int,
    end: Int,
    kind: SpanKind,
    length: Int
): List<InlineSpan> {
    if (end <= start) return spans
    if (isCovered(spans, start, end, kind)) {
        val cut = ArrayList<InlineSpan>()
        for (span in spans) {
            if (span.kind != kind || span.end <= start || span.start >= end) {
                cut += span
                continue
            }
            if (span.start < start) cut += InlineSpan(span.start, start, kind)
            if (span.end > end) cut += InlineSpan(end, span.end, kind)
        }
        return normalizeSpans(cut, length)
    }
    return normalizeSpans(spans + InlineSpan(start, end, kind), length)
}

/**
 * Moves the spans along with an edit from [old] to [new] text. The edit is found as the middle
 * part that differs (common start and common end are untouched). Text that was inserted gets
 * [typedKinds]: the kinds the cursor was in, or the ones just toggled with no selection.
 */
fun editSpans(
    old: String,
    new: String,
    spans: List<InlineSpan>,
    typedKinds: Set<SpanKind>
): List<InlineSpan> {
    if (old == new) return spans
    var prefix = 0
    val shortest = minOf(old.length, new.length)
    while (prefix < shortest && old[prefix] == new[prefix]) prefix++
    var suffix = 0
    while (suffix < shortest - prefix && old[old.length - 1 - suffix] == new[new.length - 1 - suffix]) suffix++

    val removedStart = prefix
    val removedEnd = old.length - suffix
    val insertedEnd = new.length - suffix
    val delta = new.length - old.length

    fun mapStart(p: Int) = when {
        p < removedStart -> p
        p >= removedEnd -> p + delta
        else -> removedStart
    }

    fun mapEnd(p: Int) = when {
        p <= removedStart -> p
        p >= removedEnd -> p + delta
        else -> removedStart
    }

    val moved = spans.map { InlineSpan(mapStart(it.start), mapEnd(it.end), it.kind) }
    val typed = if (insertedEnd > removedStart) {
        typedKinds.map { InlineSpan(removedStart, insertedEnd, it) }
    } else {
        emptyList()
    }
    return normalizeSpans(moved + typed, new.length)
}

/** Splits spans at [index]: the ones before it, and the ones after it shifted to start at 0. */
fun splitSpans(
    spans: List<InlineSpan>,
    index: Int,
    length: Int
): Pair<List<InlineSpan>, List<InlineSpan>> {
    val left = spans.filter { it.start < index }.map { it.copy(end = minOf(it.end, index)) }
    val right = spans.filter { it.end > index }
        .map { InlineSpan(maxOf(it.start, index) - index, it.end - index, it.kind) }
    return normalizeSpans(left, index) to normalizeSpans(right, length - index)
}

/** Spans for [left] followed by [right] when the two texts are joined. */
fun joinSpans(left: List<InlineSpan>, leftLength: Int, right: List<InlineSpan>): List<InlineSpan> =
    normalizeSpans(left + right.map {
        InlineSpan(
            it.start + leftLength,
            it.end + leftLength,
            it.kind
        )
    }, Int.MAX_VALUE)