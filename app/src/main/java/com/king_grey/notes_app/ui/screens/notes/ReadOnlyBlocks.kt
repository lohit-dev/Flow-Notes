package com.king_grey.notes_app.ui.screens.notes

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import com.king_grey.notes_app.ui.screens.folders.CheckStrokeIcon
import com.king_grey.notes_app.ui.screens.shared.screenType

/**
 * A note's content with nothing editable: used where a note is only shown (the deleted-note
 * preview). Text keeps its paragraph styles and inline formatting; checklists show their state.
 */
@Composable
internal fun ReadOnlyBlocks(blocks: List<NoteBlock>, modifier: Modifier = Modifier) {
    val colors = MaterialTheme.colorScheme
    Column(modifier = modifier.fillMaxWidth()) {
        blocks.forEach { block ->
            when (block) {
                is NoteBlock.Text -> if (block.text.isNotEmpty()) {
                    Text(
                        text = styledText(block.text, block.spans),
                        modifier = Modifier.padding(
                            top = if (block.style == ParagraphStyle.Body) 0.dp else 6.dp,
                            bottom = if (block.style == ParagraphStyle.Heading) 10.dp else 12.dp,
                        ),
                        style = readOnlyTextStyle(block.style),
                        color = colors.onSurface,
                    )
                }

                is NoteBlock.Check -> Row(
                    modifier = Modifier.padding(bottom = 10.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.Top,
                ) {
                    Box(
                        modifier = Modifier
                            .padding(top = 3.dp)
                            .size(22.dp)
                            .clip(CircleShape)
                            .then(
                                if (block.checked) Modifier.background(colors.primary)
                                else Modifier.border(2.dp, colors.onSurfaceVariant, CircleShape),
                            ),
                        contentAlignment = Alignment.Center,
                    ) {
                        if (block.checked) {
                            Icon(
                                CheckStrokeIcon,
                                contentDescription = "Done",
                                modifier = Modifier.size(14.dp),
                                tint = colors.onPrimary,
                            )
                        }
                    }
                    Text(
                        text = styledText(block.text, block.spans),
                        style = screenType(16f, FontWeight.Normal, lineHeight = 25f).let {
                            if (block.checked) it.copy(textDecoration = TextDecoration.LineThrough) else it
                        },
                        color = if (block.checked) colors.onSurfaceVariant else colors.onSurface,
                    )
                }

                is NoteBlock.Image -> Box(Modifier.padding(bottom = 16.dp)) { ImageBlock(block.uri) }
                is NoteBlock.Link -> Box(Modifier.padding(bottom = 16.dp)) { LinkCard(block) }
                is NoteBlock.Table -> Box(Modifier.padding(bottom = 16.dp)) { ReadOnlyTable(block) }
                is NoteBlock.Drawing -> Box(Modifier.padding(bottom = 16.dp)) { DrawingBlock(block) }
            }
        }
    }
}

@Composable
private fun readOnlyTextStyle(style: ParagraphStyle) = when (style) {
    ParagraphStyle.Title -> screenType(24f, FontWeight.Black, lineHeight = 28f, letterSpacing = -0.3f)
    ParagraphStyle.Heading -> screenType(20f, FontWeight.Bold, lineHeight = 26f)
    ParagraphStyle.Subheading -> screenType(17f, FontWeight.SemiBold, lineHeight = 24f)
    ParagraphStyle.Body -> screenType(16f, FontWeight.Normal, lineHeight = 25f)
}

@Composable
private fun ReadOnlyTable(block: NoteBlock.Table) {
    val colors = MaterialTheme.colorScheme
    val shape = RoundedCornerShape(16.dp)
    val line = colors.outlineVariant
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(shape)
            .border(1.dp, line, shape),
    ) {
        block.cells.forEachIndexed { r, row ->
            Row(modifier = Modifier.height(IntrinsicSize.Min)) {
                row.forEach { cell ->
                    Text(
                        text = cell,
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight()
                            .background(if (r == 0) colors.surfaceContainerHigh else androidx.compose.ui.graphics.Color.Transparent)
                            .border(0.5.dp, line.copy(alpha = 0.6f))
                            .padding(horizontal = 12.dp, vertical = 10.dp),
                        style = screenType(14f, if (r == 0) FontWeight.Bold else FontWeight.Normal, lineHeight = 20f),
                        color = colors.onSurface,
                    )
                }
            }
        }
    }
}
