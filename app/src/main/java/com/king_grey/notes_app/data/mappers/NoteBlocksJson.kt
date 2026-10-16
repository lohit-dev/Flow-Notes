package com.king_grey.notes_app.data.mappers

import com.king_grey.notes_app.ui.screens.notes.DrawingStroke
import com.king_grey.notes_app.ui.screens.notes.InlineSpan
import com.king_grey.notes_app.ui.screens.notes.NoteBlock
import com.king_grey.notes_app.ui.screens.notes.ParagraphStyle
import com.king_grey.notes_app.ui.screens.notes.SpanKind
import org.json.JSONArray
import org.json.JSONObject

/** Stores a note's blocks as one JSON string. Unknown or damaged blocks are skipped on read. */
internal object NoteBlocksJson {

    fun encode(blocks: List<NoteBlock>): String =
        JSONArray().apply { blocks.forEach { put(it.toJson()) } }.toString()

    fun decode(json: String): List<NoteBlock> {
        if (json.isBlank()) return emptyList()
        return runCatching {
            val array = JSONArray(json)
            (0 until array.length()).mapNotNull { runCatching { array.getJSONObject(it).toBlock() }.getOrNull() }
        }.getOrDefault(emptyList())
    }

    private fun NoteBlock.toJson(): JSONObject = JSONObject().apply {
        put("id", id)
        when (val block = this@toJson) {
            is NoteBlock.Text -> {
                put("type", "text")
                put("text", block.text)
                put("style", block.style.name)
                put("spans", block.spans.spansJson())
            }
            is NoteBlock.Check -> {
                put("type", "check")
                put("text", block.text)
                put("checked", block.checked)
                put("spans", block.spans.spansJson())
            }
            is NoteBlock.Image -> {
                put("type", "image")
                put("uri", block.uri)
            }
            is NoteBlock.Link -> {
                put("type", "link")
                put("title", block.title)
                put("host", block.host)
                put("url", block.url)
            }
            is NoteBlock.Table -> {
                put("type", "table")
                put(
                    "cells",
                    JSONArray().apply {
                        block.cells.forEach { row -> put(JSONArray().apply { row.forEach { put(it) } }) }
                    },
                )
            }
            is NoteBlock.Drawing -> {
                put("type", "drawing")
                put(
                    "strokes",
                    JSONArray().apply {
                        block.strokes.forEach { s ->
                            put(
                                JSONObject().apply {
                                    put("xs", JSONArray().apply { s.xs.forEach { put(it.toDouble()) } })
                                    put("ys", JSONArray().apply { s.ys.forEach { put(it.toDouble()) } })
                                    put("color", s.color)
                                    put("width", s.width.toDouble())
                                    put("highlighter", s.highlighter)
                                },
                            )
                        }
                    },
                )
            }
        }
    }

    private fun JSONObject.toBlock(): NoteBlock {
        val id = getLong("id")
        return when (getString("type")) {
            "text" -> NoteBlock.Text(
                id = id,
                text = optString("text"),
                style = enumOrDefault(optString("style"), ParagraphStyle.Body),
                spans = optJSONArray("spans").toSpans(),
            )
            "check" -> NoteBlock.Check(
                id = id,
                text = optString("text"),
                checked = optBoolean("checked"),
                spans = optJSONArray("spans").toSpans(),
            )
            "image" -> NoteBlock.Image(id, getString("uri"))
            "link" -> NoteBlock.Link(id, optString("title"), optString("host"), getString("url"))
            "table" -> NoteBlock.Table(
                id,
                getJSONArray("cells").let { rows ->
                    (0 until rows.length()).map { r ->
                        val row = rows.getJSONArray(r)
                        (0 until row.length()).map { row.getString(it) }
                    }
                },
            )
            "drawing" -> NoteBlock.Drawing(
                id,
                getJSONArray("strokes").let { arr ->
                    (0 until arr.length()).map { i ->
                        val o = arr.getJSONObject(i)
                        DrawingStroke(
                            xs = o.getJSONArray("xs").floats(),
                            ys = o.getJSONArray("ys").floats(),
                            color = o.getLong("color"),
                            width = o.getDouble("width").toFloat(),
                            highlighter = o.optBoolean("highlighter"),
                        )
                    }
                },
            )
            else -> error("unknown block type")
        }
    }

    private fun List<InlineSpan>.spansJson(): JSONArray = JSONArray().apply {
        forEach { s ->
            put(JSONObject().apply { put("start", s.start); put("end", s.end); put("kind", s.kind.name) })
        }
    }

    private fun JSONArray?.toSpans(): List<InlineSpan> {
        if (this == null) return emptyList()
        return (0 until length()).mapNotNull { i ->
            val o = getJSONObject(i)
            val kind = SpanKind.entries.firstOrNull { it.name == o.optString("kind") } ?: return@mapNotNull null
            InlineSpan(o.getInt("start"), o.getInt("end"), kind)
        }
    }

    private fun JSONArray.floats(): List<Float> = (0 until length()).map { getDouble(it).toFloat() }

    private inline fun <reified E : Enum<E>> enumOrDefault(name: String, default: E): E =
        enumValues<E>().firstOrNull { it.name == name } ?: default
}
