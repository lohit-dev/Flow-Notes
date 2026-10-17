package com.king_grey.notes_app.ui.screens.notes

import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.isImeVisible
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.selection.toggleable
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.FloatingToolbarDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.HorizontalFloatingToolbar
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SheetValue
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberBottomSheetState
import androidx.compose.material3.toShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.runtime.withFrameNanos
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onPreviewKeyEvent
import androidx.compose.ui.input.key.type
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.OffsetMapping
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.input.TransformedText
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.net.toUri
import com.king_grey.notes_app.ui.screens.folders.BackButton
import com.king_grey.notes_app.ui.screens.folders.CheckStrokeIcon
import com.king_grey.notes_app.ui.screens.folders.FolderColor
import com.king_grey.notes_app.ui.screens.folders.FolderItem
import com.king_grey.notes_app.ui.screens.folders.FolderNameDialog
import com.king_grey.notes_app.ui.screens.folders.ListIcon
import com.king_grey.notes_app.ui.screens.folders.MenuRow
import com.king_grey.notes_app.ui.screens.folders.MoreIcon
import com.king_grey.notes_app.ui.screens.folders.PenIcon
import com.king_grey.notes_app.ui.screens.folders.PinIcon
import com.king_grey.notes_app.ui.screens.folders.TrashIcon
import com.king_grey.notes_app.ui.screens.folders.strokeIcon
import com.king_grey.notes_app.ui.screens.shared.Pebble
import com.king_grey.notes_app.ui.screens.shared.screenType
import com.king_grey.notes_app.ui.theme.FlowMotion
import com.king_grey.notes_app.ui.theme.FlowNotesTheme
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

// ─────────────────────────────────────────────────────────────────────────────
// State and callbacks
// ─────────────────────────────────────────────────────────────────────────────

/** Things the editor cannot do by itself: the host opens the camera, a picker, a scanner, a recorder. */
enum class AttachKind { Camera, Photos, Files, ScanDocument, VoiceMemo }

/**
 * The note being edited. [blocks] and [title] are the source of truth: every edit goes out
 * through [NoteEditorScreen]'s onTitleChange / onBlocksChange and comes back in here. The view
 * model should update its StateFlow synchronously in those callbacks (and save with a debounce)
 * so the text fields do not lag behind the keyboard.
 */
data class NoteEditorUiState(
    val noteId: Long,
    val title: String = "",
    val blocks: List<NoteBlock> = listOf(NoteBlock.Text(1)),
    /** "1 October 2026 at 2:09 PM". */
    val dateLabel: String = "",
    val pinned: Boolean = false,
    val folders: List<FolderItem> = emptyList(),
    val folderId: Long? = null,
    /** A note that was just created: the keyboard opens on the title, or on the checklist row. */
    val isNew: Boolean = false,
    /** Settings → "New notes start with: Body". The keyboard opens on the body, not the title. */
    val startOnBody: Boolean = false,
)

private enum class EditorSheet { None, Attach, Table, Move }
private enum class EditorDialog { None, Link, NewFolder }

/** Ask a text block to take focus and put the cursor at [cursor]. [nonce] makes repeats distinct. */
private data class FocusTarget(val id: Long, val cursor: Int, val nonce: Long = System.nanoTime())

/** Title field id used for focus bookkeeping. */
private const val TitleId = -1L

/** Drawing canvas shape, from the mockup's 320 x 380 paper. */
internal const val DrawingAspect = 320f / 380f

// ─────────────────────────────────────────────────────────────────────────────
// Screen
// ─────────────────────────────────────────────────────────────────────────────

/**
 * The note editor (note details). A title, then blocks: paragraphs (Title / Heading / Subheading /
 * Body with bold, italic, underline, strike), checklists, photos, links, tables and drawings.
 *
 * Bottom floating toolbar: Aa (format popup), checklist, table, attach (camera, photos, files,
 * scan, voice memo, link) and draw. With the keyboard open a check button closes it. The ⋮ menu has
 * Share, Pin, Move to folder and Delete.
 *
 * Rules worth knowing: Enter splits a block (on an empty checklist row it ends the list);
 * Backspace at the start of a block turns a checklist row or heading back into body text, then
 * joins it with the block above. Long press a photo, link, table or drawing to remove it.
 *
 * [onDelete] means "move to Recently deleted" and the host should pop back.
 */
@OptIn(
    ExperimentalLayoutApi::class,
)
@Composable
fun NoteEditorScreen(
    state: NoteEditorUiState,
    modifier: Modifier = Modifier,
    onBack: () -> Unit = {},
    onTitleChange: (String) -> Unit = {},
    onBlocksChange: (List<NoteBlock>) -> Unit = {},
    onTogglePin: () -> Unit = {},
    onMoveToFolder: (folderId: Long) -> Unit = {},
    onCreateFolderAndMove: (name: String) -> Unit = {},
    onDelete: () -> Unit = {},
    onAttach: (AttachKind) -> Unit = {},
) {
    val colors = MaterialTheme.colorScheme
    val context = LocalContext.current
    val focusManager = LocalFocusManager.current
    val keyboard = LocalSoftwareKeyboardController.current
    val listState = rememberLazyListState()
    val blocks = state.blocks
    val currentBlocks by rememberUpdatedState(blocks)

    var menuOpen by remember { mutableStateOf(false) }
    var formatOpen by remember { mutableStateOf(false) }
    var sheet by rememberSaveable { mutableStateOf(EditorSheet.None) }
    var dialog by rememberSaveable { mutableStateOf(EditorDialog.None) }

    /** -1 = no drawing open, 0 = a new drawing, otherwise the id of the block being edited. */
    var drawingFor by rememberSaveable { mutableLongStateOf(-1L) }

    // The text block the toolbar acts on, and where its selection is.
    var activeId by rememberSaveable { mutableStateOf<Long?>(null) }
    var selStart by rememberSaveable { mutableIntStateOf(0) }
    var selEnd by rememberSaveable { mutableIntStateOf(0) }

    /** Kinds toggled with a cursor and no selection: they apply to what is typed next. */
    var pending by remember { mutableStateOf<Set<SpanKind>?>(null) }
    var focusTarget by remember { mutableStateOf<FocusTarget?>(null) }

    val imeVisible = WindowInsets.isImeVisible
    val activeBlock = blocks.firstOrNull { it.id == activeId }

    // A new note opens the keyboard: on the title, or on its first checklist row.
    LaunchedEffect(state.noteId) {
        if (state.isNew) {
            val first = blocks.firstOrNull()
            focusTarget = when {
                first is NoteBlock.Check -> FocusTarget(first.id, first.text.length)
                state.startOnBody && first != null -> FocusTarget(first.id, 0)
                else -> FocusTarget(TitleId, 0)
            }
        }
    }

    fun update(newBlocks: List<NoteBlock>) = onBlocksChange(newBlocks)

    fun focus(id: Long, cursor: Int) {
        focusTarget = FocusTarget(id, cursor)
    }

    /** Insert after the active block (or at the end), and keep an empty paragraph below to type in. */
    fun insertBlock(block: NoteBlock) {
        val list = currentBlocks.toMutableList()
        val at = list.indexOfFirst { it.id == activeId }.let { if (it < 0) list.size else it + 1 }
        // an empty paragraph the cursor sits in is replaced rather than left behind
        val before = list.getOrNull(at - 1)
        if (before is NoteBlock.Text && before.text.isEmpty() && at - 1 == list.lastIndex && list.size > 1) {
            list.removeAt(at - 1)
            list.add(at - 1, block)
        } else {
            list.add(at, block)
        }
        val position = list.indexOfFirst { it.id == block.id }
        if (position == list.lastIndex || list[position + 1] !is NoteBlock.Text && list[position + 1] !is NoteBlock.Check) {
            val spare = NoteBlock.Text(list.nextId())
            list.add(position + 1, spare)
            focus(spare.id, 0)
        }
        update(list)
    }

    fun changeText(id: Long, text: String, spans: List<InlineSpan>) {
        update(currentBlocks.map { if (it.id == id) it.withText(text, spans) else it })
    }

    /** Enter: split the block at [at]. [text] is the whole text with the new line already taken out. */
    fun split(id: Long, at: Int, text: String, spans: List<InlineSpan>) {
        val list = currentBlocks.toMutableList()
        val index = list.indexOfFirst { it.id == id }
        if (index < 0) return
        val block = list[index]
        if (block is NoteBlock.Check && text.isEmpty()) {
            // Enter on an empty checklist row ends the list
            list[index] = NoteBlock.Text(id, "")
            update(list)
            focus(id, 0)
            return
        }
        val (leftSpans, rightSpans) = splitSpans(spans, at, text.length)
        val left = text.substring(0, at)
        val right = text.substring(at)
        val newId = list.nextId()
        list[index] = block.withText(left, leftSpans)
        list.add(
            index + 1,
            when (block) {
                is NoteBlock.Check -> NoteBlock.Check(newId, right, false, rightSpans)
                else -> NoteBlock.Text(newId, right, ParagraphStyle.Body, rightSpans)
            },
        )
        update(list)
        focus(newId, 0)
    }

    /** Backspace at the very start of a block. */
    fun backspaceAtStart(id: Long) {
        val list = currentBlocks.toMutableList()
        val index = list.indexOfFirst { it.id == id }
        if (index < 0) return
        when (val block = list[index]) {
            is NoteBlock.Check -> {
                list[index] = NoteBlock.Text(id, block.text, ParagraphStyle.Body, block.spans)
                update(list)
                focus(id, 0)
            }

            is NoteBlock.Text -> {
                if (block.style != ParagraphStyle.Body) {
                    list[index] = block.copy(style = ParagraphStyle.Body)
                    update(list)
                    return
                }
                val previous = list.getOrNull(index - 1) ?: return
                val previousText = previous.textOrNull()
                if (previousText != null) {
                    val joined = previous.withText(
                        previousText + block.text,
                        joinSpans(
                            previous.spansOrNull().orEmpty(),
                            previousText.length,
                            block.spans
                        ),
                    )
                    list[index - 1] = joined
                    list.removeAt(index)
                    update(list)
                    focus(previous.id, previousText.length)
                } else if (block.text.isEmpty()) {
                    // an empty paragraph under a photo or table: drop it
                    list.removeAt(index)
                    update(list)
                }
            }

            else -> Unit
        }
    }

    // ── Toolbar actions ─────────────────────────────────────────────────────

    fun toggleKind(kind: SpanKind) {
        val block = activeBlock ?: return
        val text = block.textOrNull() ?: return
        val spans = block.spansOrNull().orEmpty()
        val start = minOf(selStart, selEnd).coerceIn(0, text.length)
        val end = maxOf(selStart, selEnd).coerceIn(0, text.length)
        if (start == end) {
            val now = pending ?: kindsAt(spans, start)
            pending = if (kind in now) now - kind else now + kind
        } else {
            changeText(block.id, text, toggleKind(spans, start, end, kind, text.length))
        }
    }

    fun setParagraphStyle(style: ParagraphStyle) {
        val block = activeBlock ?: return
        val converted = when (block) {
            is NoteBlock.Text -> block.copy(style = style)
            is NoteBlock.Check -> NoteBlock.Text(block.id, block.text, style, block.spans)
            else -> return
        }
        update(currentBlocks.map { if (it.id == block.id) converted else it })
    }

    fun toggleChecklist() {
        when (activeBlock) {
            is NoteBlock.Check -> update(
                currentBlocks.map {
                    if (it.id == activeBlock.id) NoteBlock.Text(
                        activeBlock.id,
                        activeBlock.text,
                        ParagraphStyle.Body,
                        activeBlock.spans
                    ) else it
                },
            )

            is NoteBlock.Text -> update(
                currentBlocks.map {
                    if (it.id == activeBlock.id) NoteBlock.Check(
                        activeBlock.id,
                        activeBlock.text,
                        false,
                        activeBlock.spans
                    ) else it
                },
            )

            else -> {
                val row = NoteBlock.Check(currentBlocks.nextId())
                update(currentBlocks + row)
                focus(row.id, 0)
            }
        }
    }

    fun plainText() = noteToPlainText(state.title, blocks)

    // ── Sheets, dialogs, drawing ────────────────────────────────────────────

    when (sheet) {
        EditorSheet.Attach -> AttachSheet(
            onPick = { picked ->
                sheet = EditorSheet.None
                if (picked == null) dialog = EditorDialog.Link else onAttach(picked)
            },
            onDismiss = { sheet = EditorSheet.None },
        )

        EditorSheet.Table -> TableSheet(
            onInsert = { rows, cols ->
                sheet = EditorSheet.None
                insertBlock(
                    NoteBlock.Table(
                        id = currentBlocks.nextId(),
                        cells = List(rows) { List(cols) { "" } },
                    ),
                )
            },
            onDismiss = { sheet = EditorSheet.None },
        )

        EditorSheet.Move -> MoveSheet(
            count = 1,
            folders = state.folders,
            initialFolderId = state.folderId,
            onCancel = { sheet = EditorSheet.None },
            onMove = {
                sheet = EditorSheet.None
                onMoveToFolder(it)
            },
            onNewFolder = { dialog = EditorDialog.NewFolder },
        )

        EditorSheet.None -> Unit
    }

    when (dialog) {
        EditorDialog.Link -> LinkDialog(
            onDismiss = { dialog = EditorDialog.None },
            onAdd = { url ->
                dialog = EditorDialog.None
                val host = url.toUri().host.orEmpty().removePrefix("www.")
                insertBlock(
                    NoteBlock.Link(
                        id = currentBlocks.nextId(),
                        title = host.ifEmpty { url },
                        host = host.ifEmpty { url },
                        url = url,
                    ),
                )
            },
        )

        EditorDialog.NewFolder -> FolderNameDialog(
            title = "New folder",
            confirmLabel = "Create and move",
            initialName = "",
            existingNames = state.folders.map { it.name },
            onDismiss = { dialog = EditorDialog.None },
            onConfirm = {
                dialog = EditorDialog.None
                sheet = EditorSheet.None
                onCreateFolderAndMove(it)
            },
        )

        EditorDialog.None -> Unit
    }

    BackHandler(enabled = formatOpen) { formatOpen = false }

    // The mockup: status bar, then the 64dp bar, then the page starts 72dp below the bar's top.
    val statusTop = WindowInsets.statusBars.asPaddingValues().calculateTopPadding()

    Box(modifier = modifier.fillMaxSize()) {
        Scaffold(
            containerColor = colors.background,
            contentWindowInsets = WindowInsets(0, 0, 0, 0),
        ) { padding ->
            LazyColumn(
                state = listState,
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(
                    start = 20.dp,
                    end = 20.dp,
                    top = padding.calculateTopPadding() + statusTop + 72.dp,
                    bottom = 200.dp,
                ),
            ) {
                item(key = "date") {
                    // .d: 13sp, centered, 14dp below. The row is kept (empty) when there is no date so
                    // the title never slides under the top bar's fade.
                    Text(
                        text = state.dateLabel.ifEmpty { " " },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 14.dp),
                        style = screenType(13f, FontWeight.Normal, lineHeight = 15f),
                        color = colors.onSurfaceVariant,
                        textAlign = TextAlign.Center,
                    )
                }
                item(key = "title") {
                    TitleField(
                        title = state.title,
                        noteId = state.noteId,
                        focusTarget = focusTarget,
                        onFocusConsumed = { focusTarget = null },
                        onChange = onTitleChange,
                        onNext = {
                            val first = blocks.firstOrNull { it.textOrNull() != null }
                            if (first != null) focus(first.id, 0)
                        },
                        onFocus = { activeId = null },
                    )
                }
                items(blocks, key = { it.id }) { block ->
                    val only = blocks.size == 1
                    when (block) {
                        is NoteBlock.Text -> TextBlockField(
                            blockId = block.id,
                            text = block.text,
                            spans = block.spans,
                            textStyle = paragraphTextStyle(block.style, colors.onSurface),
                            placeholder = if (only) "Start typing" else null,
                            pending = pending,
                            focusTarget = focusTarget,
                            onFocusConsumed = { focusTarget = null },
                            onChange = { text, spans -> changeText(block.id, text, spans) },
                            onSplit = { at, text, spans -> split(block.id, at, text, spans) },
                            onBackspaceAtStart = { backspaceAtStart(block.id) },
                            onSelection = { range ->
                                activeId = block.id
                                if (range.start != selStart || range.end != selEnd) pending = null
                                selStart = range.start
                                selEnd = range.end
                            },
                            // h4: 18dp above (12dp of it is the previous block's), 10dp below
                            modifier = Modifier.padding(
                                top = if (block.style == ParagraphStyle.Body) 0.dp else 6.dp,
                                bottom = if (block.style == ParagraphStyle.Heading) 10.dp else 12.dp,
                            ),
                        )

                        is NoteBlock.Check -> CheckRow(
                            block = block,
                            pending = pending,
                            focusTarget = focusTarget,
                            onFocusConsumed = { focusTarget = null },
                            onToggle = { checked ->
                                update(currentBlocks.map { if (it.id == block.id) block.copy(checked = checked) else it })
                            },
                            onChange = { text, spans -> changeText(block.id, text, spans) },
                            onSplit = { at, text, spans -> split(block.id, at, text, spans) },
                            onBackspaceAtStart = { backspaceAtStart(block.id) },
                            onSelection = { range ->
                                activeId = block.id
                                if (range.start != selStart || range.end != selEnd) pending = null
                                selStart = range.start
                                selEnd = range.end
                            },
                        )

                        is NoteBlock.Image -> BlockFrame(
                            onDelete = { update(currentBlocks.filterNot { it.id == block.id }) },
                        ) { ImageBlock(block.uri) }

                        is NoteBlock.Link -> BlockFrame(
                            onDelete = { update(currentBlocks.filterNot { it.id == block.id }) },
                            onClick = {
                                runCatching {
                                    context.startActivity(
                                        Intent(Intent.ACTION_VIEW, block.url.toUri()).addFlags(
                                            Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_GRANT_READ_URI_PERMISSION
                                        ),
                                    )
                                }
                            },
                        ) { LinkCard(block) }

                        is NoteBlock.Table -> BlockFrame(
                            onDelete = { update(currentBlocks.filterNot { it.id == block.id }) },
                        ) {
                            TableBlock(block) { row, col, value ->
                                update(
                                    currentBlocks.map {
                                        if (it.id == block.id && it is NoteBlock.Table) {
                                            it.copy(
                                                cells = it.cells.mapIndexed { r, cells ->
                                                    if (r == row) cells.mapIndexed { c, old -> if (c == col) value else old } else cells
                                                },
                                            )
                                        } else it
                                    },
                                )
                            }
                        }

                        is NoteBlock.Drawing -> BlockFrame(
                            onDelete = { update(currentBlocks.filterNot { it.id == block.id }) },
                            onClick = { drawingFor = block.id },
                        ) { DrawingBlock(block) }
                    }
                }
            }
        }

        // ── Top bar: back and ⋮, fading into the page ────────────────────────
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    Brush.verticalGradient(
                        0f to colors.background,
                        0.75f to colors.background,
                        1f to Color.Transparent,
                    ),
                )
                .statusBarsPadding()
                .heightIn(min = 64.dp)
                .padding(horizontal = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            BackButton(onBack)
            Spacer(Modifier.weight(1f))
            Box {
                IconButton(onClick = { menuOpen = true }, modifier = Modifier.size(48.dp)) {
                    Icon(MoreIcon, contentDescription = "More options")
                }
                DropdownMenu(
                    expanded = menuOpen,
                    onDismissRequest = { menuOpen = false },
                    modifier = Modifier.width(236.dp),
                    shape = RoundedCornerShape(20.dp),
                    containerColor = colors.surfaceContainerHigh,
                ) {
                    MenuRow("Share", ShareIcon, {
                        menuOpen = false
                        val send = Intent(Intent.ACTION_SEND)
                            .setType("text/plain")
                            .putExtra(Intent.EXTRA_SUBJECT, state.title)
                            .putExtra(Intent.EXTRA_TEXT, plainText())
                        context.startActivity(Intent.createChooser(send, null))
                    })
                    MenuRow(
                        if (state.pinned) "Unpin note" else "Pin note",
                        PinIcon,
                        { menuOpen = false; onTogglePin() })
                    MenuRow(
                        "Move to folder",
                        MoveIcon,
                        { menuOpen = false; sheet = EditorSheet.Move })
                    HorizontalDivider(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp),
                        color = colors.outlineVariant,
                    )
                    MenuRow("Delete", TrashIcon, { menuOpen = false; onDelete() }, danger = true)
                }
            }
        }

        // ── Floating toolbar (and the format popup above it) ─────────────────
        Column(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .navigationBarsPadding()
                .imePadding()
                .padding(bottom = if (imeVisible) 16.dp else 20.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            AnimatedVisibility(
                visible = formatOpen,
                enter = fadeIn() + slideInVertically { it / 3 },
                exit = fadeOut() + slideOutVertically { it / 3 },
            ) {
                val text = activeBlock?.textOrNull().orEmpty()
                val spans = activeBlock?.spansOrNull().orEmpty()
                val lo = minOf(selStart, selEnd).coerceIn(0, text.length)
                val hi = maxOf(selStart, selEnd).coerceIn(0, text.length)
                FormatPopup(
                    kinds = if (lo == hi) pending ?: kindsAt(spans, lo) else kindsOn(spans, lo, hi),
                    style = (activeBlock as? NoteBlock.Text)?.style ?: ParagraphStyle.Body,
                    isChecklist = activeBlock is NoteBlock.Check,
                    onToggleKind = ::toggleKind,
                    onStyle = {
                        setParagraphStyle(it)
                        formatOpen = false
                    },
                    modifier = Modifier.padding(horizontal = 16.dp),
                )
            }
            // The Material 3 Expressive floating toolbar (vibrant: primaryContainer pill, primary
            // FAB). With the keyboard open, the check FAB sits beside it and closes the keyboard.
            val toolbarItems: @Composable RowScope.() -> Unit = {
                ToolbarToggle(
                    on = formatOpen,
                    label = "Format",
                    onClick = { formatOpen = !formatOpen }) {
                    Text(
                        text = "Aa",
                        style = screenType(18f, FontWeight.Black, lineHeight = 22f),
                        color = if (formatOpen) colors.onPrimary else colors.onPrimaryContainer,
                    )
                }
                ToolbarToggle(
                    on = activeBlock is NoteBlock.Check,
                    label = "Checklist",
                    onClick = ::toggleChecklist
                ) {
                    Icon(
                        ListIcon, contentDescription = null,
                        tint = if (activeBlock is NoteBlock.Check) colors.onPrimary else colors.onPrimaryContainer,
                    )
                }
                ToolbarToggle(
                    on = false,
                    label = "Insert table",
                    onClick = { formatOpen = false; sheet = EditorSheet.Table }) {
                    Icon(
                        TableIcon,
                        contentDescription = null,
                        tint = colors.onPrimaryContainer
                    )
                }
                ToolbarToggle(
                    on = false,
                    label = "Add to note",
                    onClick = { formatOpen = false; sheet = EditorSheet.Attach }) {
                    Icon(
                        ClipIcon,
                        contentDescription = null,
                        tint = colors.onPrimaryContainer
                    )
                }
                ToolbarToggle(
                    on = false,
                    label = "Draw",
                    onClick = { formatOpen = false; drawingFor = 0L }) {
                    Icon(
                        PenIcon,
                        contentDescription = null,
                        tint = colors.onPrimaryContainer
                    )
                }
            }
            if (imeVisible) {
                HorizontalFloatingToolbar(
                    expanded = true,
                    colors = FloatingToolbarDefaults.vibrantFloatingToolbarColors(),
                    floatingActionButton = {
                        FloatingToolbarDefaults.VibrantFloatingActionButton(
                            onClick = {
                                formatOpen = false
                                focusManager.clearFocus()
                                keyboard?.hide()
                            },
                        ) {
                            Icon(
                                CheckStrokeIcon,
                                contentDescription = "Done",
                                modifier = Modifier.size(24.dp),
                            )
                        }
                    },
                    content = toolbarItems,
                )
            } else {
                HorizontalFloatingToolbar(
                    expanded = true,
                    colors = FloatingToolbarDefaults.vibrantFloatingToolbarColors(),
                    content = toolbarItems,
                )
            }
        }

        // The ⋮ menu dims the page (.dim, 18%); the popup is its own window so taps still reach it.
        if (menuOpen) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.18f))
            )
        }

        // ── Drawing, full screen on top ──────────────────────────────────────
        if (drawingFor >= 0L) {
            val existing = blocks.firstOrNull { it.id == drawingFor } as? NoteBlock.Drawing
            NoteDrawingScreen(
                initial = existing?.strokes.orEmpty(),
                onCancel = { drawingFor = -1L },
                onDone = { strokes ->
                    val editing = drawingFor
                    drawingFor = -1L
                    if (existing != null) {
                        update(
                            if (strokes.isEmpty()) {
                                currentBlocks.filterNot { it.id == editing }
                            } else {
                                currentBlocks.map { if (it.id == editing) existing.copy(strokes = strokes) else it }
                            },
                        )
                    } else if (strokes.isNotEmpty()) {
                        insertBlock(NoteBlock.Drawing(currentBlocks.nextId(), strokes))
                    }
                },
            )
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Text fields
// ─────────────────────────────────────────────────────────────────────────────

/** A text field value that follows [text] when it changes from outside and keeps its own cursor. */
@Composable
private fun rememberFieldValue(key: Any, text: String): MutableState<TextFieldValue> {
    val state = remember(key) { mutableStateOf(TextFieldValue(text, TextRange(text.length))) }
    LaunchedEffect(text) {
        if (state.value.text != text) state.value = TextFieldValue(text, TextRange(text.length))
    }
    return state
}

@Composable
private fun paragraphTextStyle(style: ParagraphStyle, color: Color): TextStyle = when (style) {
    ParagraphStyle.Title -> screenType(
        24f,
        FontWeight.Black,
        lineHeight = 28f,
        letterSpacing = -0.3f
    )

    ParagraphStyle.Heading -> screenType(20f, FontWeight.Bold, lineHeight = 26f)
    ParagraphStyle.Subheading -> screenType(17f, FontWeight.SemiBold, lineHeight = 24f)
    ParagraphStyle.Body -> screenType(16f, FontWeight.Normal, lineHeight = 25f)
}.copy(color = color)

/** Draws inline formatting without changing the text or the cursor positions. */
private fun spanTransformation(spans: List<InlineSpan>) = VisualTransformation { text ->
    TransformedText(styledText(text.text, spans), OffsetMapping.Identity)
}

/** [text] with bold / italic / underline / strike applied from [spans]. */
internal fun styledText(
    text: String,
    spans: List<InlineSpan>
): androidx.compose.ui.text.AnnotatedString {
    val length = text.length
    val cuts = (spans.flatMap { listOf(it.start, it.end) } + 0 + length)
        .filter { it in 0..length }
        .distinct()
        .sorted()
    return buildAnnotatedString {
        append(text)
        for (i in 0 until cuts.size - 1) {
            val from = cuts[i]
            val to = cuts[i + 1]
            val kinds = spans.filter { it.start <= from && it.end >= to }.map { it.kind }.toSet()
            if (kinds.isEmpty()) continue
            val decorations = buildList {
                if (SpanKind.Underline in kinds) add(TextDecoration.Underline)
                if (SpanKind.Strike in kinds) add(TextDecoration.LineThrough)
            }
            addStyle(
                SpanStyle(
                    fontWeight = if (SpanKind.Bold in kinds) FontWeight.Bold else null,
                    fontStyle = if (SpanKind.Italic in kinds) FontStyle.Italic else null,
                    textDecoration = if (decorations.isEmpty()) null else TextDecoration.combine(
                        decorations
                    ),
                ),
                from,
                to,
            )
        }
    }
}

/**
 * One paragraph or checklist text. It owns its cursor; text, formatting and structure edits go
 * up as callbacks. A new line in the text means Enter and is turned into [onSplit].
 */
@Composable
private fun TextBlockField(
    blockId: Long,
    text: String,
    spans: List<InlineSpan>,
    textStyle: TextStyle,
    placeholder: String?,
    pending: Set<SpanKind>?,
    focusTarget: FocusTarget?,
    onFocusConsumed: () -> Unit,
    onChange: (String, List<InlineSpan>) -> Unit,
    onSplit: (at: Int, text: String, spans: List<InlineSpan>) -> Unit,
    onBackspaceAtStart: () -> Unit,
    onSelection: (TextRange) -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = MaterialTheme.colorScheme
    var value by rememberFieldValue(blockId, text)
    val requester = remember { FocusRequester() }
    val transformation = remember(spans) { spanTransformation(spans) }

    LaunchedEffect(focusTarget) {
        focusTarget?.takeIf { it.id == blockId }?.let { target ->
            withFrameNanos { }
            runCatching { requester.requestFocus() }
            value = value.copy(selection = TextRange(target.cursor.coerceIn(0, value.text.length)))
            onFocusConsumed()
        }
    }

    BasicTextField(
        value = value,
        onValueChange = { new ->
            val old = value
            val typed = pending ?: kindsOn(spans, old.selection.min, old.selection.max)
            if (new.text.contains('\n')) {
                val at = new.text.indexOf('\n')
                val cleaned = new.text.replace("\n", "")
                val edited = editSpans(old.text, cleaned, spans, typed)
                onSplit(at.coerceAtMost(cleaned.length), cleaned, edited)
            } else {
                val grew = new.text.length > old.text.length
                val edited = editSpans(old.text, new.text, spans, if (grew) typed else emptySet())
                value = new
                if (new.text != old.text) onChange(new.text, edited)
                onSelection(new.selection)
            }
        },
        modifier = modifier
            .fillMaxWidth()
            .focusRequester(requester)
            .onFocusChanged { if (it.isFocused) onSelection(value.selection) }
            .onPreviewKeyEvent { event ->
                if (event.type == KeyEventType.KeyDown && event.key == Key.Backspace &&
                    value.selection.collapsed && value.selection.start == 0
                ) {
                    onBackspaceAtStart()
                    true
                } else {
                    false
                }
            },
        textStyle = textStyle,
        cursorBrush = SolidColor(colors.primary),
        visualTransformation = transformation,
        keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Sentences),
        decorationBox = { inner ->
            Box {
                if (placeholder != null && value.text.isEmpty()) {
                    Text(
                        text = placeholder,
                        style = textStyle,
                        color = colors.onSurfaceVariant.copy(alpha = 0.8f)
                    )
                }
                inner()
            }
        },
    )
}

@Composable
private fun CheckIconAnimation(checked: Boolean, tint: Color) {
    AnimatedVisibility(
        visible = checked,
        enter = scaleIn(
            FlowMotion.bouncy(),
            initialScale = 0.2f
        ) + fadeIn(FlowMotion.effects()),
        exit = scaleOut(
            FlowMotion.fastSpatial(),
            targetScale = 0.2f
        ) + fadeOut(FlowMotion.effects()),
    ) {
        Icon(
            CheckStrokeIcon,
            contentDescription = null,
            modifier = Modifier.size(16.dp),
            tint = tint,
        )
    }
}

/** .ck: a 22dp rounded box (filled with a check when done) and its text, struck through when done. */
@Composable
private fun CheckRow(
    block: NoteBlock.Check,
    pending: Set<SpanKind>?,
    focusTarget: FocusTarget?,
    onFocusConsumed: () -> Unit,
    onToggle: (Boolean) -> Unit,
    onChange: (String, List<InlineSpan>) -> Unit,
    onSplit: (at: Int, text: String, spans: List<InlineSpan>) -> Unit,
    onBackspaceAtStart: () -> Unit,
    onSelection: (TextRange) -> Unit,
) {
    val colors = MaterialTheme.colorScheme
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 12.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.Top,
    ) {
        // The box rounds off into a circle when ticked, with a spring, and the tick pops in.
        val corner by animateDpAsState(
            targetValue = if (block.checked) 11.dp else 6.dp,
            animationSpec = FlowMotion.bouncy(),
            label = "check-corner",
        )
        val boxShape = RoundedCornerShape(corner.coerceAtLeast(0.dp))
        val fill by animateColorAsState(
            targetValue = if (block.checked) colors.primary else Color.Transparent,
            animationSpec = FlowMotion.effects(),
            label = "check-fill",
        )
        val ring by animateColorAsState(
            targetValue = if (block.checked) colors.primary else colors.onSurfaceVariant,
            animationSpec = FlowMotion.effects(),
            label = "check-ring",
        )
        Box(
            modifier = Modifier
                .padding(top = 1.dp)
                .size(22.dp)
                .clip(boxShape)
                .background(fill)
                .border(2.dp, ring, boxShape)
                .toggleable(value = block.checked, role = Role.Checkbox, onValueChange = onToggle),
            contentAlignment = Alignment.Center,
        ) {
            CheckIconAnimation(checked = block.checked, tint = colors.onPrimary)
        }
        val style = screenType(16f, FontWeight.Normal, lineHeight = 22f).copy(
            color = if (block.checked) colors.onSurfaceVariant else colors.onSurface,
            textDecoration = if (block.checked) TextDecoration.LineThrough else null,
        )
        TextBlockField(
            blockId = block.id,
            text = block.text,
            spans = block.spans,
            textStyle = style,
            placeholder = if (block.text.isEmpty()) "List item" else null,
            pending = pending,
            focusTarget = focusTarget,
            onFocusConsumed = onFocusConsumed,
            onChange = onChange,
            onSplit = onSplit,
            onBackspaceAtStart = onBackspaceAtStart,
            onSelection = onSelection,
            modifier = Modifier.weight(1f),
        )
    }
}

/** h3: 34 / 900. Enter or Next moves to the body. */
@Composable
private fun TitleField(
    title: String,
    noteId: Long,
    focusTarget: FocusTarget?,
    onFocusConsumed: () -> Unit,
    onChange: (String) -> Unit,
    onNext: () -> Unit,
    onFocus: () -> Unit,
) {
    val colors = MaterialTheme.colorScheme
    var value by rememberFieldValue("title-$noteId", title)
    val requester = remember { FocusRequester() }
    val style = screenType(
        34f,
        FontWeight.Black,
        lineHeight = 37f,
        letterSpacing = -0.6f
    ).copy(color = colors.onSurface)

    LaunchedEffect(focusTarget) {
        if (focusTarget?.id == TitleId) {
            withFrameNanos { }
            runCatching { requester.requestFocus() }
            onFocusConsumed()
        }
    }

    BasicTextField(
        value = value,
        onValueChange = {
            // Enter in the title means "go to the body"
            if (it.text.contains('\n')) {
                onNext()
            } else {
                value = it
                if (it.text != title) onChange(it.text)
            }
        },
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 10.dp)
            .focusRequester(requester)
            .onFocusChanged { if (it.isFocused) onFocus() },
        textStyle = style,
        cursorBrush = SolidColor(colors.primary),
        keyboardOptions = KeyboardOptions(
            capitalization = KeyboardCapitalization.Sentences,
            imeAction = ImeAction.Next
        ),
        keyboardActions = KeyboardActions(onNext = { onNext() }),
        decorationBox = { inner ->
            Box {
                if (value.text.isEmpty()) {
                    Text(
                        text = "Title",
                        style = style,
                        color = colors.onSurfaceVariant.copy(alpha = 0.55f)
                    )
                }
                inner()
            }
        },
    )
}

// ─────────────────────────────────────────────────────────────────────────────
// Non-text blocks
// ─────────────────────────────────────────────────────────────────────────────

/** Tap does [onClick]; long press offers Remove. */
@Composable
private fun BlockFrame(
    onDelete: () -> Unit,
    onClick: (() -> Unit)? = null,
    content: @Composable () -> Unit,
) {
    var menu by remember { mutableStateOf(false) }
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 16.dp)
            .combinedClickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = { onClick?.invoke() },
                onLongClick = { menu = true },
            ),
    ) {
        content()
        DropdownMenu(
            expanded = menu,
            onDismissRequest = { menu = false },
            shape = RoundedCornerShape(20.dp),
            containerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
        ) {
            MenuRow("Remove", TrashIcon, { menu = false; onDelete() }, danger = true)
        }
    }
}

/** .photo: 200dp, 28dp corners. Decoded off the main thread at about the size it is shown. */
@Composable
internal fun ImageBlock(uri: String) {
    val context = LocalContext.current
    val bitmap by produceState<Bitmap?>(initialValue = null, uri) {
        value = withContext(Dispatchers.IO) { decodeBitmap(context, uri.toUri(), maxSide = 1280) }
    }
    val shape = RoundedCornerShape(28.dp)
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 4.dp)
            .height(200.dp)
            .clip(shape)
            .background(MaterialTheme.colorScheme.surfaceContainer),
    ) {
        bitmap?.let {
            Image(
                bitmap = it.asImageBitmap(),
                contentDescription = "Photo in note",
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop,
            )
        }
    }
}

private fun decodeBitmap(context: android.content.Context, uri: Uri, maxSide: Int): Bitmap? =
    runCatching {
        val resolver = context.contentResolver
        val bounds = BitmapFactory.Options().apply { inJustDecodeBounds = true }
        resolver.openInputStream(uri)?.use { BitmapFactory.decodeStream(it, null, bounds) }
        var sample = 1
        while (bounds.outWidth / sample > maxSide || bounds.outHeight / sample > maxSide) sample *= 2
        val options = BitmapFactory.Options().apply { inSampleSize = sample }
        resolver.openInputStream(uri)?.use { BitmapFactory.decodeStream(it, null, options) }
    }.getOrNull()

/** .lnk: a 64dp tile, the title and the host. */
@Composable
internal fun LinkCard(block: NoteBlock.Link) {
    val colors = MaterialTheme.colorScheme
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(24.dp))
            .background(colors.surfaceContainer)
            .padding(10.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Box(
            modifier = Modifier
                .size(64.dp)
                .clip(
                    RoundedCornerShape(
                        topStart = 18.dp,
                        topEnd = 18.dp,
                        bottomEnd = 18.dp,
                        bottomStart = 26.dp
                    )
                )
                .background(colors.tertiaryContainer),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                ClipIcon,
                contentDescription = null,
                modifier = Modifier.size(26.dp),
                tint = colors.onTertiaryContainer
            )
        }
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = block.title,
                style = screenType(16f, FontWeight.SemiBold, lineHeight = 22f),
                color = colors.onSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Text(
                text = block.host,
                style = screenType(13f, FontWeight.Normal, lineHeight = 18f),
                color = colors.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
    }
}

/** A rounded grid of editable cells; the first row is the header. */
@Composable
private fun TableBlock(
    block: NoteBlock.Table,
    onCell: (row: Int, col: Int, value: String) -> Unit
) {
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
                row.forEachIndexed { c, cell ->
                    var value by rememberFieldValue("cell-${block.id}-$r-$c", cell)
                    BasicTextField(
                        value = value,
                        onValueChange = {
                            value = it
                            if (it.text != cell) onCell(r, c, it.text)
                        },
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight()
                            .background(if (r == 0) colors.surfaceContainerHigh else Color.Transparent)
                            .border(0.5.dp, line.copy(alpha = 0.6f))
                            .padding(horizontal = 12.dp, vertical = 10.dp),
                        textStyle = screenType(
                            14f,
                            if (r == 0) FontWeight.Bold else FontWeight.Normal,
                            lineHeight = 20f
                        )
                            .copy(color = colors.onSurface),
                        cursorBrush = SolidColor(colors.primary),
                    )
                }
            }
        }
    }
}

/** A drawing in a note: the strokes, fitted into a 220dp tall card. Tap to edit. */
@Composable
internal fun DrawingBlock(block: NoteBlock.Drawing) {
    val colors = MaterialTheme.colorScheme
    val ink = colors.onSurface
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(220.dp)
            .clip(RoundedCornerShape(24.dp))
            .background(colors.surfaceContainerLow)
            .border(1.dp, colors.outlineVariant, RoundedCornerShape(24.dp)),
        contentAlignment = Alignment.Center,
    ) {
        Canvas(
            modifier = Modifier
                .fillMaxHeight()
                .aspectRatio(DrawingAspect)
        ) {
            drawStrokes(block.strokes, ink)
        }
    }
}

/** Replays strokes (0..1 coordinates) onto this canvas. Color 0 means "ink", the theme's text color. */
internal fun androidx.compose.ui.graphics.drawscope.DrawScope.drawStrokes(
    strokes: List<DrawingStroke>,
    ink: Color
) {
    for ((xs, ys, color1, width1, highlighter) in strokes) {
        if (xs.isEmpty()) continue
        val color = if (color1 == 0L) ink else Color(color1.toInt())
        val width = width1 / 320f * size.width * (if (highlighter) 2.4f else 1f)
        val tint = if (highlighter) color.copy(alpha = 0.35f) else color
        if (xs.size == 1) {
            drawCircle(
                tint,
                radius = width / 2f,
                center = Offset(xs[0] * size.width, ys[0] * size.height)
            )
            continue
        }
        val path = Path().apply {
            moveTo(xs[0] * size.width, ys[0] * size.height)
            for (i in 1 until xs.size) lineTo(
                xs[i] * size.width,
                ys[i] * size.height
            )
        }
        drawPath(
            path,
            tint,
            style = Stroke(width = width, cap = StrokeCap.Round, join = StrokeJoin.Round)
        )
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Toolbar and format popup
// ─────────────────────────────────────────────────────────────────────────────

/** .ftb > span: a 48dp round button that is primary when on. */
@Composable
private fun ToolbarToggle(
    on: Boolean,
    label: String,
    onClick: () -> Unit,
    content: @Composable () -> Unit
) {
    val colors = MaterialTheme.colorScheme
    Box(
        modifier = Modifier
            .size(48.dp)
            .clip(RoundedCornerShape(24.dp))
            .background(if (on) colors.primary else Color.Transparent)
            .clickable(onClickLabel = label, role = Role.Button, onClick = onClick),
        contentAlignment = Alignment.Center,
    ) { content() }
}

/** .fmt: B I U S as a connected row, then Title / Heading / Subheading / Body. */
@Composable
private fun FormatPopup(
    kinds: Set<SpanKind>,
    style: ParagraphStyle,
    isChecklist: Boolean,
    onToggleKind: (SpanKind) -> Unit,
    onStyle: (ParagraphStyle) -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = MaterialTheme.colorScheme
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(28.dp),
        color = colors.surfaceContainerHigh,
        shadowElevation = 8.dp,
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            // .seg: 44dp buttons, 2dp apart, outer corners round, pressed ones fully round
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(2.dp),
            ) {
                val items =
                    listOf(SpanKind.Bold, SpanKind.Italic, SpanKind.Underline, SpanKind.Strike)
                items.forEachIndexed { index, kind ->
                    val on = kind in kinds
                    val shape = if (on) {
                        RoundedCornerShape(22.dp)
                    } else {
                        RoundedCornerShape(
                            topStart = if (index == 0) 22.dp else 8.dp,
                            bottomStart = if (index == 0) 22.dp else 8.dp,
                            topEnd = if (index == items.lastIndex) 22.dp else 8.dp,
                            bottomEnd = if (index == items.lastIndex) 22.dp else 8.dp,
                        )
                    }
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .heightIn(min = 44.dp)
                            .clip(shape)
                            .background(if (on) colors.primary else colors.surfaceContainerHighest)
                            .toggleable(value = on, role = Role.Checkbox) { onToggleKind(kind) },
                        contentAlignment = Alignment.Center,
                    ) {
                        val tint = if (on) colors.onPrimary else colors.onSurface
                        Text(
                            text = when (kind) {
                                SpanKind.Bold -> "B"
                                SpanKind.Italic -> "I"
                                SpanKind.Underline -> "U"
                                SpanKind.Strike -> "S"
                            },
                            style = screenType(
                                18f,
                                if (kind == SpanKind.Bold) FontWeight.Black else FontWeight.Medium,
                                lineHeight = 22f
                            )
                                .copy(
                                    fontStyle = if (kind == SpanKind.Italic) FontStyle.Italic else null,
                                    textDecoration = when (kind) {
                                        SpanKind.Underline -> TextDecoration.Underline
                                        SpanKind.Strike -> TextDecoration.LineThrough
                                        else -> null
                                    },
                                ),
                            color = tint,
                        )
                    }
                }
            }
            StyleRow(
                "Title",
                24f,
                FontWeight.Black,
                selected = !isChecklist && style == ParagraphStyle.Title
            ) {
                onStyle(ParagraphStyle.Title)
            }
            StyleRow(
                "Heading",
                20f,
                FontWeight.Bold,
                selected = !isChecklist && style == ParagraphStyle.Heading
            ) {
                onStyle(ParagraphStyle.Heading)
            }
            StyleRow(
                "Subheading",
                17f,
                FontWeight.SemiBold,
                selected = !isChecklist && style == ParagraphStyle.Subheading
            ) {
                onStyle(ParagraphStyle.Subheading)
            }
            StyleRow(
                "Body",
                16f,
                FontWeight.Normal,
                selected = !isChecklist && style == ParagraphStyle.Body
            ) {
                onStyle(ParagraphStyle.Body)
            }
        }
    }
}

/** .st2: a 48dp row set in its own style; the current one is tonal with a check. */
@Composable
private fun StyleRow(
    label: String,
    size: Float,
    weight: FontWeight,
    selected: Boolean,
    onClick: () -> Unit
) {
    val colors = MaterialTheme.colorScheme
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = 48.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(if (selected) colors.secondaryContainer else Color.Transparent)
            .clickable(role = Role.RadioButton, onClick = onClick)
            .padding(horizontal = 16.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = label,
            modifier = Modifier.weight(1f),
            style = screenType(size, weight, lineHeight = size + 4f),
            color = if (selected) colors.onSecondaryContainer else colors.onSurface,
        )
        if (selected) {
            Icon(
                CheckStrokeIcon,
                contentDescription = "Selected",
                modifier = Modifier.size(20.dp),
                tint = colors.primary
            )
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Sheets and dialogs
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun SheetHandle() {
    Box(
        modifier = Modifier
            .padding(top = 14.dp, bottom = 12.dp)
            .size(width = 32.dp, height = 4.dp)
            .clip(RoundedCornerShape(2.dp))
            .background(MaterialTheme.colorScheme.outlineVariant),
    )
}

/** #f-attach: six round tiles. Link is null: the editor asks for the address itself. */
@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)
@Composable
private fun AttachSheet(onPick: (AttachKind?) -> Unit, onDismiss: () -> Unit) {
    val colors = MaterialTheme.colorScheme
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = rememberBottomSheetState(initialValue = SheetValue.Hidden),
        containerColor = colors.surfaceContainerLow,
        shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp),
        dragHandle = { SheetHandle() },
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 16.dp, end = 16.dp, bottom = 26.dp),
        ) {
            Text(
                text = "Add to note",
                modifier = Modifier.padding(start = 4.dp, end = 4.dp, bottom = 8.dp),
                style = screenType(20f, FontWeight.Bold, lineHeight = 24f),
                color = colors.onSurface,
            )
            val tiles: List<Triple<String, ImageVector, AttachKind?>> = listOf(
                Triple("Camera", CameraIcon, AttachKind.Camera),
                Triple("Photos", ImageIcon, AttachKind.Photos),
                Triple("Files", FileIcon, AttachKind.Files),
                Triple("Scan document", ScanIcon, AttachKind.ScanDocument),
                Triple("Voice memo", MicIcon, AttachKind.VoiceMemo),
                Triple("Link", ClipIcon, null),
            )
            tiles.chunked(3).forEach { rowTiles ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 7.dp),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                ) {
                    rowTiles.forEach { (label, icon, kind) ->
                        Column(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(16.dp))
                                .clickable(role = Role.Button) { onPick(kind) }
                                .padding(vertical = 4.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(8.dp),
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(64.dp)
                                    .clip(androidx.compose.material3.MaterialShapes.Pebble.toShape())
                                    .background(colors.primaryContainer),
                                contentAlignment = Alignment.Center,
                            ) {
                                Icon(
                                    icon,
                                    contentDescription = null,
                                    modifier = Modifier.size(26.dp),
                                    tint = colors.onPrimaryContainer
                                )
                            }
                            Text(
                                text = label,
                                style = screenType(13f, FontWeight.Normal, lineHeight = 18f),
                                color = colors.onSurface,
                                textAlign = TextAlign.Center,
                            )
                        }
                    }
                }
            }
        }
    }
}

/** #f-table: a 5 x 5 grid; tap a cell to pick that many rows and columns. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun TableSheet(onInsert: (rows: Int, cols: Int) -> Unit, onDismiss: () -> Unit) {
    val colors = MaterialTheme.colorScheme
    val sheetState = rememberBottomSheetState(initialValue = SheetValue.Hidden)
    val scope = rememberCoroutineScope()
    var rows by rememberSaveable { mutableIntStateOf(3) }
    var cols by rememberSaveable { mutableIntStateOf(3) }
    fun closeThen(action: () -> Unit) {
        scope.launch { sheetState.hide() }.invokeOnCompletion { action() }
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = colors.surfaceContainerLow,
        shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp),
        dragHandle = { SheetHandle() },
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 16.dp, end = 16.dp, bottom = 26.dp),
        ) {
            Text(
                text = "Insert table",
                modifier = Modifier.padding(start = 4.dp, end = 4.dp, bottom = 8.dp),
                style = screenType(20f, FontWeight.Bold, lineHeight = 24f),
                color = colors.onSurface,
            )
            Column(
                modifier = Modifier.padding(horizontal = 14.dp, vertical = 4.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp),
            ) {
                for (r in 1..5) {
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        for (c in 1..5) {
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .aspectRatio(1f)
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(if (r <= rows && c <= cols) colors.primary else colors.surfaceContainerHigh)
                                    .clickable(onClickLabel = "$r rows, $c columns") {
                                        rows = r
                                        cols = c
                                    },
                            )
                        }
                    }
                }
            }
            Text(
                text = "$rows rows × $cols columns",
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 12.dp),
                style = screenType(14f, FontWeight.Normal, lineHeight = 20f),
                color = colors.onSurfaceVariant,
                textAlign = TextAlign.Center,
            )
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 12.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.End),
            ) {
                FilledTonalButton(
                    onClick = { closeThen(onDismiss) },
                    modifier = Modifier.height(48.dp),
                    shapes = ButtonDefaults.shapes(),
                    colors = ButtonDefaults.filledTonalButtonColors(
                        containerColor = colors.secondaryContainer,
                        contentColor = colors.onSecondaryContainer,
                    ),
                    contentPadding = PaddingValues(horizontal = 24.dp),
                ) { Text("Cancel", style = screenType(14f, FontWeight.Bold, lineHeight = 20f)) }
                Button(
                    onClick = { closeThen { onInsert(rows, cols) } },
                    modifier = Modifier.height(48.dp),
                    shapes = ButtonDefaults.shapes(),
                    contentPadding = PaddingValues(horizontal = 24.dp),
                ) { Text("Insert", style = screenType(14f, FontWeight.Bold, lineHeight = 20f)) }
            }
        }
    }
}

/** Paste an address; it becomes a link card. A missing scheme gets https. */
@Composable
private fun LinkDialog(onDismiss: () -> Unit, onAdd: (String) -> Unit) {
    val colors = MaterialTheme.colorScheme
    var url by rememberSaveable { mutableStateOf("") }
    val trimmed = url.trim()
    val withScheme =
        if (trimmed.startsWith("http://") || trimmed.startsWith("https://")) trimmed else "https://$trimmed"
    val valid = trimmed.contains('.') && !trimmed.contains(' ')

    com.king_grey.notes_app.ui.screens.folders.DialogFrame(
        icon = ClipIcon,
        isError = false,
        title = "Add link",
        onDismiss = onDismiss,
    ) {
        OutlinedTextField(
            value = url,
            onValueChange = { url = it },
            modifier = Modifier.fillMaxWidth(),
            label = { Text("Address") },
            singleLine = true,
            shape = RoundedCornerShape(16.dp),
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
            keyboardActions = KeyboardActions(onDone = { if (valid) onAdd(withScheme) }),
        )
        Column(
            modifier = Modifier.padding(top = 18.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            com.king_grey.notes_app.ui.screens.folders.DialogButton(
                text = "Add",
                onClick = { onAdd(withScheme) },
                container = colors.primary,
                content = colors.onPrimary,
                enabled = valid,
            )
            com.king_grey.notes_app.ui.screens.folders.DialogButton(
                text = "Cancel",
                onClick = onDismiss,
                container = colors.secondaryContainer,
                content = colors.onSecondaryContainer,
            )
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Icons
// ─────────────────────────────────────────────────────────────────────────────

private val TableIcon by lazy {
    strokeIcon(
        "Table",
        "M6 4h12a3 3 0 0 1 3 3v10a3 3 0 0 1-3 3H6a3 3 0 0 1-3-3V7a3 3 0 0 1 3-3zM3 10h18M3 15h18M10 4v16"
    )
}
private val ClipIcon by lazy {
    strokeIcon(
        "Clip",
        "m20 11-8.5 8.5a5 5 0 0 1-7-7L13 4a3.3 3.3 0 0 1 4.7 4.7l-8.5 8.5a1.7 1.7 0 0 1-2.4-2.4L14 9"
    )
}
private val MicIcon by lazy {
    strokeIcon(
        "Mic",
        "M12 3a3 3 0 0 0-3 3v5a3 3 0 0 0 6 0V6a3 3 0 0 0-3-3zM5 11a7 7 0 0 0 14 0M12 18v3"
    )
}
private val CameraIcon by lazy {
    strokeIcon(
        "Camera",
        "M4 8h3l2-3h6l2 3h3a1 1 0 0 1 1 1v9a1 1 0 0 1-1 1H4a1 1 0 0 1-1-1V9a1 1 0 0 1 1-1zM15.5 13a3.5 3.5 0 1 1-7 0a3.5 3.5 0 1 1 7 0z"
    )
}
private val ImageIcon by lazy {
    strokeIcon(
        "Image",
        "M6 4h12a3 3 0 0 1 3 3v10a3 3 0 0 1-3 3H6a3 3 0 0 1-3-3V7a3 3 0 0 1 3-3zM10.6 10a1.6 1.6 0 1 1-3.2 0a1.6 1.6 0 1 1 3.2 0zM4 18l5-5 4 4 3-3 4 4"
    )
}
private val FileIcon by lazy {
    strokeIcon("File", "M14 3H7a2 2 0 0 0-2 2v14a2 2 0 0 0 2 2h10a2 2 0 0 0 2-2V8zM14 3v5h5")
}
private val ScanIcon by lazy {
    strokeIcon(
        "Scan",
        "M4 8V6a2 2 0 0 1 2-2h2M16 4h2a2 2 0 0 1 2 2v2M20 16v2a2 2 0 0 1-2 2h-2M8 20H6a2 2 0 0 1-2-2v-2M4 12h16"
    )
}

// ─────────────────────────────────────────────────────────────────────────────
// Previews
// ─────────────────────────────────────────────────────────────────────────────

private val PreviewBlocks = listOf(
    NoteBlock.Text(
        1,
        "Build one small app end to end: a notes app with three tabs. Learn each SwiftUI piece by using it, not by reading about it.",
        spans = listOf(InlineSpan(0, 9, SpanKind.Bold)),
    ),
    NoteBlock.Text(2, "This week", ParagraphStyle.Heading),
    NoteBlock.Check(3, "Create the TabView with three tabs", checked = true),
    NoteBlock.Check(4, "Build the Notes list with List and sections", checked = true),
    NoteBlock.Check(5, "Store the appearance choice with @AppStorage"),
    NoteBlock.Check(6, "Add a note editor with TextEditor"),
    NoteBlock.Text(7, "Keep views small, and let @State own the data that changes."),
    NoteBlock.Text(8, "Reading", ParagraphStyle.Heading),
    NoteBlock.Link(
        9,
        "Adopting Liquid Glass",
        "developer.apple.com",
        "https://developer.apple.com"
    ),
)

@Preview(widthDp = 360, heightDp = 760, showBackground = true)
@Composable
private fun NoteEditorPreview() {
    FlowNotesTheme(dynamicColor = false) {
        NoteEditorScreen(
            state = NoteEditorUiState(
                noteId = 1,
                title = "Swift learning plan",
                blocks = PreviewBlocks,
                dateLabel = "1 October 2026 at 2:09 PM",
                folders = listOf(FolderItem(3, "Learning", FolderColor.Violet, 8)),
                folderId = 3,
            ),
        )
    }
}

@Preview(widthDp = 360, heightDp = 760, showBackground = true)
@Composable
private fun NoteEditorNewPreview() {
    FlowNotesTheme(dynamicColor = false) {
        NoteEditorScreen(
            state = NoteEditorUiState(
                noteId = 2,
                dateLabel = "1 October 2026 at 2:09 PM",
                isNew = true
            ),
        )
    }
}
