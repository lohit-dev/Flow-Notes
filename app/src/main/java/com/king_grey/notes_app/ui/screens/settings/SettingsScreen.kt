package com.king_grey.notes_app.ui.screens.settings

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemColors
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.ListItemShapes
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SegmentedListItem
import androidx.compose.material3.SheetValue
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberBottomSheetState
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathFillType
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.addPathNodes
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.input.pointer.positionChanged
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.ProgressBarRangeInfo
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.progressBarRangeInfo
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.setProgress
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import com.king_grey.notes_app.R
import com.king_grey.notes_app.ui.screens.shared.ConnectedToggleGroup
import com.king_grey.notes_app.ui.screens.shared.FlowNotesLargeTopAppBar
import com.king_grey.notes_app.ui.screens.shared.screenType
import com.king_grey.notes_app.ui.theme.FlowNotesTheme
import kotlinx.coroutines.launch

// ─────────────────────────────────────────────────────────────────────────────
// Models
// ─────────────────────────────────────────────────────────────────────────────

enum class AppearanceMode(val label: String) { System("System"), Light("Light"), Dark("Dark") }

/** Mockup accent swatches (.ac). Map each one to the matching palette in your theme. */
enum class AccentColor(val label: String, val color: Color) {
    Violet("Violet", Color(0xFF5B57E8)),
    Sapphire("Sapphire", Color(0xFF4F7FD0)),
    Meadow("Meadow", Color(0xFF2F9E4F)),
    Sunset("Sunset", Color(0xFFD4641E)),
    Orchid("Orchid", Color(0xFFA0449A)),
}

enum class NoteSort(val label: String) {
    DateEdited("Date edited"),
    DateCreated("Date created"),
    Title("Title"),
}

enum class SortOrder(val label: String) { NewestFirst("Newest first"), OldestFirst("Oldest first") }

enum class NewNoteStart(val label: String) { Title("Title"), Body("Body"), Checklist("Checklist") }

/** Which bottom sheet is open. */
enum class SettingsSheet { None, Sort, StartWith }

const val TextSizeMin = 14
const val TextSizeMax = 24

data class SettingsUiState(
    val appearance: AppearanceMode = AppearanceMode.System,
    val accent: AccentColor = AccentColor.Violet,
    val dynamicColor: Boolean = false,
    val textSize: Int = 17,                          // sp, TextSizeMin..TextSizeMax
    val sort: NoteSort = NoteSort.DateEdited,
    val sortOrder: SortOrder = SortOrder.NewestFirst,
    val newNoteStart: NewNoteStart = NewNoteStart.Title,
    val showPreviews: Boolean = true,
    val versionLabel: String = "1.0 (1)",
    val openSheet: SettingsSheet = SettingsSheet.None,
)

// ─────────────────────────────────────────────────────────────────────────────
// Screen
// ─────────────────────────────────────────────────────────────────────────────

/**
 * Settings from the Flow Notes mockup: Appearance (mode, accent, text size), Notes (sort, new note
 * start, previews) and About (version, privacy policy, rate).
 *
 * Scaffold + LargeFlexibleTopAppBar + LazyColumn of Material 3 list items. The two choice sheets
 * (sort, new notes start with) live at the bottom of this file. The privacy policy page and the
 * in-app review are the host's job: [onOpenPrivacy] and [onRateApp].
 */
@Composable
fun SettingsScreen(
    state: SettingsUiState,
    modifier: Modifier = Modifier,
    onAppearanceChange: (AppearanceMode) -> Unit = {},
    onAccentChange: (AccentColor) -> Unit = {},
    onDynamicColorChange: (Boolean) -> Unit = {},
    onTextSizeChange: (Int) -> Unit = {},
    onOpenSheet: (SettingsSheet) -> Unit = {},
    onCloseSheet: () -> Unit = {},
    onSortSave: (NoteSort, SortOrder) -> Unit = { _, _ -> },
    onStartWithSave: (NewNoteStart) -> Unit = {},
    onShowPreviewsChange: (Boolean) -> Unit = {},
    onOpenPrivacy: () -> Unit = {},
    onRateApp: () -> Unit = {},
) {
    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior()

    when (state.openSheet) {
        SettingsSheet.Sort -> SortSheet(
            sort = state.sort,
            order = state.sortOrder,
            onSave = onSortSave,
            onDismiss = onCloseSheet,
        )

        SettingsSheet.StartWith -> StartWithSheet(
            selected = state.newNoteStart,
            onSave = onStartWithSave,
            onDismiss = onCloseSheet,
        )

        SettingsSheet.None -> Unit
    }

    Scaffold(
        modifier = modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        containerColor = MaterialTheme.colorScheme.background,
        // The host Scaffold already handles the bottom (navigation bar) inset.
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        topBar = {
            FlowNotesLargeTopAppBar(
                title = "Settings",
                subtitle = "Flow Notes",
                scrollBehavior = scrollBehavior,
            )
        },
    ) { padding ->
        LazyColumn(
            modifier = Modifier.fillMaxWidth(),
            contentPadding = PaddingValues(
                start = 16.dp,
                end = 16.dp,
                top = padding.calculateTopPadding() + 4.dp,
                bottom = padding.calculateBottomPadding() + 24.dp,
            ),
        ) {
            // ── Appearance ───────────────────────────────────────────────────
            item(key = "h-appearance") { SectionHeader("Appearance", first = true) }
            item(key = "appearance") {
                val modes = AppearanceMode.entries
                ConnectedToggleGroup(
                    options = modes.map { it.label },
                    selectedIndex = modes.indexOf(state.appearance),
                    onSelect = { onAppearanceChange(modes[it]) },
                )
                Spacer(Modifier.height(8.dp))
                SettingsGroup(
                    { shapes ->
                        SwitchRow(
                            label = "Dynamic color",
                            checked = state.dynamicColor,
                            onCheckedChange = onDynamicColorChange,
                            shapes = shapes,
                        )
                    },
                    { shapes ->
                        AccentRow(
                            selected = state.accent,
                            onSelect = onAccentChange,
                            shapes = shapes,
                        )
                    },
                    { shapes ->
                        TextSizeRow(
                            size = state.textSize,
                            onSizeChange = onTextSizeChange,
                            shapes = shapes,
                        )
                    },
                )
            }

            // ── Notes ────────────────────────────────────────────────────────
            item(key = "h-notes") { SectionHeader("Notes") }
            item(key = "notes") {
                SettingsGroup(
                    { shapes ->
                        NavRow(
                            label = "Sort notes by",
                            value = state.sort.label,
                            onClick = { onOpenSheet(SettingsSheet.Sort) },
                            shapes = shapes,
                        )
                    },
                    { shapes ->
                        NavRow(
                            label = "New notes start with",
                            value = state.newNoteStart.label,
                            onClick = { onOpenSheet(SettingsSheet.StartWith) },
                            shapes = shapes,
                        )
                    },
                    { shapes ->
                        SwitchRow(
                            label = "Show note previews",
                            checked = state.showPreviews,
                            onCheckedChange = onShowPreviewsChange,
                            shapes = shapes,
                        )
                    },
                )
            }

            // ── About ────────────────────────────────────────────────────────
            item(key = "h-about") { SectionHeader("About") }
            item(key = "about") {
                SettingsGroup(
                    { shapes -> ValueRow("Version", state.versionLabel, shapes) },
                    { shapes -> NavRow("Privacy policy", null, onOpenPrivacy, shapes) },
                    { shapes -> NavRow("Rate the app", null, onRateApp, shapes) },
                )
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Section header and grouped rows
// ─────────────────────────────────────────────────────────────────────────────

/** .h2: 20 / 700, margin 20 4 10 (the first one sits flush under the app name). */
@Composable
private fun SectionHeader(title: String, first: Boolean = false) {
    Text(
        text = title,
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 4.dp, end = 4.dp, top = if (first) 0.dp else 20.dp, bottom = 10.dp)
            .semantics { heading() },
        style = screenType(20f, FontWeight.Bold, lineHeight = 24f),
        color = MaterialTheme.colorScheme.onSurface,
    )
}

private val RowPadding = PaddingValues(horizontal = 18.dp, vertical = 8.dp)

/** .rw: surfaceContainer rows. */
@Composable
private fun rowColors(): ListItemColors =
    ListItemDefaults.colors(containerColor = MaterialTheme.colorScheme.surfaceContainer)

/** .list: 2dp between rows, 28dp outer corners (the segmented list shapes). */
@Composable
private fun SettingsGroup(vararg rows: @Composable (ListItemShapes) -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
        rows.forEachIndexed { index, row ->
            row(ListItemDefaults.segmentedShapes(index = index, count = rows.size))
        }
    }
}

@Composable
private fun RowLabel(text: String) {
    Text(
        text = text,
        style = screenType(16f, FontWeight.Normal, lineHeight = 22f),
        color = MaterialTheme.colorScheme.onSurface,
    )
}

@Composable
private fun RowValue(text: String) {
    Text(
        text = text,
        style = screenType(14f, FontWeight.Normal, lineHeight = 18f),
        color = MaterialTheme.colorScheme.onSurfaceVariant,
    )
}

/** Tappable row: label, optional value, chevron. */
@Composable
private fun NavRow(
    label: String,
    value: String?,
    onClick: () -> Unit,
    shapes: ListItemShapes,
) {
    SegmentedListItem(
        onClick = onClick,
        shapes = shapes,
        modifier = Modifier.heightIn(min = 56.dp),
        trailingContent = {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                if (value != null) RowValue(value)
                Icon(
                    painter = painterResource(R.drawable.ic_chevron),
                    contentDescription = null,
                    modifier = Modifier.size(20.dp),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        },
        colors = rowColors(),
        contentPadding = RowPadding,
    ) { RowLabel(label) }
}

/** Not interactive: label and value (Version). */
@Composable
private fun ValueRow(label: String, value: String, shapes: ListItemShapes) {
    ListItem(
        shapes = shapes,
        modifier = Modifier.heightIn(min = 56.dp),
        trailingContent = { RowValue(value) },
        colors = rowColors(),
        contentPadding = RowPadding,
    ) { RowLabel(label) }
}

/** .sw2: a switch with a check in the thumb when on. */
@Composable
private fun SwitchRow(
    label: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    shapes: ListItemShapes,
) {
    SegmentedListItem(
        onClick = { onCheckedChange(!checked) },
        shapes = shapes,
        modifier = Modifier
            .heightIn(min = 56.dp)
            .semantics { contentDescription = label },
        trailingContent = {
            Switch(
                checked = checked,
                onCheckedChange = null,
                thumbContent = if (checked) {
                    {
                        Icon(
                            imageVector = CheckIcon,
                            contentDescription = null,
                            modifier = Modifier.size(SwitchDefaults.IconSize),
                        )
                    }
                } else null,
            )
        },
        colors = rowColors(),
        contentPadding = RowPadding,
    ) { RowLabel(label) }
}

// ─────────────────────────────────────────────────────────────────────────────
// Accent color row (.rw.col with five swatches)
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun AccentRow(
    selected: AccentColor,
    onSelect: (AccentColor) -> Unit,
    shapes: ListItemShapes,
) {
    ListItem(
        shapes = shapes,
        colors = rowColors(),
        contentPadding = PaddingValues(start = 18.dp, end = 18.dp, top = 12.dp, bottom = 14.dp),
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            RowLabel("Accent color")
            Row(
                modifier = Modifier.selectableGroup(),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                AccentColor.entries.forEach { accent ->
                    AccentSwatch(
                        accent = accent,
                        selected = accent == selected,
                        onClick = { onSelect(accent) },
                    )
                }
            }
        }
    }
}

/** .ac: 40dp circle that squares off to a 14dp corner and shows a white check when selected. */
@Composable
private fun AccentSwatch(accent: AccentColor, selected: Boolean, onClick: () -> Unit) {
    val corner by animateDpAsState(if (selected) 14.dp else 20.dp, label = "accent-corner")
    Box(
        modifier = Modifier
            .size(40.dp)
            .clip(RoundedCornerShape(corner))
            .background(accent.color)
            .semantics { contentDescription = accent.label }
            .selectable(selected = selected, role = Role.RadioButton, onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        if (selected) {
            Icon(
                imageVector = CheckIcon,
                contentDescription = null,
                modifier = Modifier.size(20.dp),
                tint = Color.White,
            )
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Text size row (.rw.col with the slider and a preview line)
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun TextSizeRow(size: Int, onSizeChange: (Int) -> Unit, shapes: ListItemShapes) {
    val colors = MaterialTheme.colorScheme
    val clamped = size.coerceIn(TextSizeMin, TextSizeMax)
    val range = (TextSizeMax - TextSizeMin).toFloat()

    ListItem(
        shapes = shapes,
        colors = rowColors(),
        contentPadding = PaddingValues(start = 18.dp, end = 18.dp, top = 12.dp, bottom = 14.dp),
    ) {
        Column {
            RowLabel("Text size")
            Spacer(Modifier.height(10.dp))
            TextSizeSlider(
                fraction = (clamped - TextSizeMin) / range,
                onFractionChange = { fraction ->
                    onSizeChange(TextSizeMin + Math.round(fraction * range))
                },
            )
            Text(
                text = "The quick brown fox jumps over the lazy dog.",
                modifier = Modifier.padding(top = 4.dp),
                style = screenType(
                    clamped.toFloat(),
                    FontWeight.Normal,
                    lineHeight = clamped * 1.4f
                ),
                color = colors.onSurfaceVariant,
            )
        }
    }
}

/** .sl: a filled part with a bold "A", a 4dp handle, and a track with a dot at the end. */
@Composable
private fun TextSizeSlider(fraction: Float, onFractionChange: (Float) -> Unit) {
    val colors = MaterialTheme.colorScheme
    val primary = colors.primary
    var pressed by remember { mutableStateOf(false) }
    val currentOnChange by rememberUpdatedState(onFractionChange)
    val handleWidth by animateDpAsState(if (pressed) 2.dp else 4.dp, label = "handle")

    BoxWithConstraints(
        modifier = Modifier
            .fillMaxWidth()
            .height(56.dp)
            .semantics {
                contentDescription = "Text size"
                progressBarRangeInfo = ProgressBarRangeInfo(fraction, 0f..1f)
                setProgress { target ->
                    currentOnChange(target.coerceIn(0f, 1f))
                    true
                }
            }
            .pointerInput(Unit) {
                awaitEachGesture {
                    val down = awaitFirstDown()
                    pressed = true
                    down.consume()
                    currentOnChange((down.position.x / size.width).coerceIn(0f, 1f))
                    do {
                        val event = awaitPointerEvent()
                        event.changes.forEach { change ->
                            if (change.positionChanged()) {
                                change.consume()
                                currentOnChange((change.position.x / size.width).coerceIn(0f, 1f))
                            }
                        }
                    } while (event.changes.any { it.pressed })
                    pressed = false
                }
            },
    ) {
        val position = maxWidth * fraction.coerceIn(0f, 1f)
        val inactiveStart = position + 8.dp

        Box(
            modifier = Modifier
                .offset(y = 8.dp)
                .width((position - 8.dp).coerceAtLeast(0.dp))
                .height(40.dp)
                .clip(
                    RoundedCornerShape(
                        topStart = 20.dp, topEnd = 4.dp, bottomEnd = 4.dp, bottomStart = 20.dp,
                    ),
                )
                .background(primary),
            contentAlignment = Alignment.CenterStart,
        ) {
            Text(
                text = "A",
                modifier = Modifier.padding(start = 12.dp),
                style = screenType(14f, FontWeight.Black, lineHeight = 18f),
                color = colors.onPrimary,
                maxLines = 1,
                softWrap = false,
            )
        }
        Box(
            modifier = Modifier
                .offset(x = inactiveStart, y = 8.dp)
                .width((maxWidth - inactiveStart).coerceAtLeast(0.dp))
                .height(40.dp)
                .clip(
                    RoundedCornerShape(
                        topStart = 4.dp, topEnd = 20.dp, bottomEnd = 20.dp, bottomStart = 4.dp,
                    ),
                )
                .background(colors.secondaryContainer),
        ) {
            Box(
                modifier = Modifier
                    .align(Alignment.CenterEnd)
                    .padding(end = 16.dp)
                    .size(4.dp)
                    .clip(CircleShape)
                    .background(primary),
            )
        }
        Box(
            modifier = Modifier
                .offset { IntOffset(x = (position - handleWidth / 2).roundToPx(), y = 0) }
                .width(handleWidth)
                .height(56.dp)
                .clip(RoundedCornerShape(2.dp))
                .background(primary),
        )
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Choice sheets (.bs): title, radio rows, Cancel / Done
// ─────────────────────────────────────────────────────────────────────────────

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ChoiceSheet(
    title: String,
    onCancel: () -> Unit,
    onDone: () -> Unit,
    content: @Composable ColumnScope.() -> Unit,
) {
    val colors = MaterialTheme.colorScheme
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val scope = rememberCoroutineScope()
    fun closeThen(action: () -> Unit) {
        scope.launch { sheetState.hide() }.invokeOnCompletion { action() }
    }

    ModalBottomSheet(
        onDismissRequest = onCancel,
        sheetState = sheetState,
        containerColor = colors.surfaceContainerLow,
        shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp),
        dragHandle = {
            Box(
                modifier = Modifier
                    .padding(top = 14.dp, bottom = 12.dp)
                    .size(width = 32.dp, height = 4.dp)
                    .clip(RoundedCornerShape(2.dp))
                    .background(colors.outlineVariant),
            )
        },
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 16.dp, end = 16.dp, bottom = 26.dp),
        ) {
            Text(
                text = title,
                modifier = Modifier
                    .padding(start = 4.dp, end = 4.dp, bottom = 8.dp)
                    .semantics { heading() },
                style = screenType(20f, FontWeight.Bold, lineHeight = 24f),
                color = colors.onSurface,
            )
            content()
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 12.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.End),
            ) {
                FilledTonalButton(
                    onClick = { closeThen(onCancel) },
                    modifier = Modifier.height(48.dp),
                    shapes = ButtonDefaults.shapes(),
                    colors = ButtonDefaults.filledTonalButtonColors(
                        containerColor = colors.secondaryContainer,
                        contentColor = colors.onSecondaryContainer,
                    ),
                    contentPadding = PaddingValues(horizontal = 24.dp),
                ) {
                    Text("Cancel", style = screenType(14f, FontWeight.Bold, lineHeight = 20f))
                }
                Button(
                    onClick = { closeThen(onDone) },
                    modifier = Modifier.height(48.dp),
                    shapes = ButtonDefaults.shapes(),
                    contentPadding = PaddingValues(horizontal = 24.dp),
                ) {
                    Text("Done", style = screenType(14f, FontWeight.Bold, lineHeight = 20f))
                }
            }
        }
    
    }
}

/** .rw.bsr: a plain row with a radio at the end. */
@Composable
private fun ChoiceRow(label: String, selected: Boolean, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = 56.dp)
            .selectable(selected = selected, role = Role.RadioButton, onClick = onClick)
            .padding(horizontal = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Box(modifier = Modifier.weight(1f)) { RowLabel(label) }
        RadioButton(selected = selected, onClick = null)
    }
}

/** #f-sort: Date edited / Date created / Title, then Newest first / Oldest first. */
@Composable
internal fun SortSheet(
    sort: NoteSort,
    order: SortOrder,
    onSave: (NoteSort, SortOrder) -> Unit,
    onDismiss: () -> Unit,
) {
    var pickedSort by remember { mutableStateOf(sort) }
    var pickedOrder by remember { mutableStateOf(order) }

    ChoiceSheet(
        title = "Sort notes by",
        onCancel = onDismiss,
        onDone = {
            onSave(pickedSort, pickedOrder)
            onDismiss()
        },
    ) {
        Column(modifier = Modifier.selectableGroup()) {
            NoteSort.entries.forEach { option ->
                ChoiceRow(
                    label = option.label,
                    selected = option == pickedSort,
                    onClick = { pickedSort = option },
                )
            }
        }
        val orders = SortOrder.entries
        ConnectedToggleGroup(
            options = orders.map { it.label },
            selectedIndex = orders.indexOf(pickedOrder),
            onSelect = { pickedOrder = orders[it] },
            modifier = Modifier.padding(start = 4.dp, end = 4.dp, top = 10.dp),
        )
    }
}

/** #f-startwith: Title / Body / Checklist. */
@Composable
private fun StartWithSheet(
    selected: NewNoteStart,
    onSave: (NewNoteStart) -> Unit,
    onDismiss: () -> Unit,
) {
    var picked by remember { mutableStateOf(selected) }

    ChoiceSheet(
        title = "New notes start with",
        onCancel = onDismiss,
        onDone = {
            onSave(picked)
            onDismiss()
        },
    ) {
        Column(modifier = Modifier.selectableGroup()) {
            NewNoteStart.entries.forEach { option ->
                ChoiceRow(
                    label = option.label,
                    selected = option == picked,
                    onClick = { picked = option },
                )
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Check icon drawn in code (the mockup's #check symbol)
// ─────────────────────────────────────────────────────────────────────────────

private val CheckIcon: ImageVector by lazy {
    ImageVector.Builder(
        name = "Check", defaultWidth = 24.dp, defaultHeight = 24.dp,
        viewportWidth = 24f, viewportHeight = 24f,
    ).addPath(
        pathData = addPathNodes("M9,16.17L4.83,12l-1.42,1.41L9,19 21,7l-1.41,-1.41z"),
        pathFillType = PathFillType.NonZero,
        fill = SolidColor(Color.Black),
    ).build()
}

// ─────────────────────────────────────────────────────────────────────────────
// Previews
// ─────────────────────────────────────────────────────────────────────────────

@Preview(widthDp = 360, heightDp = 760, showBackground = true)
@Composable
private fun SettingsScreenPreview() {
    FlowNotesTheme(dynamicColor = false) {
        SettingsScreen(state = SettingsUiState())
    }
}

@Preview(widthDp = 360, heightDp = 760, showBackground = true)
@Composable
private fun SettingsScreenSortSheetPreview() {
    FlowNotesTheme(dynamicColor = false) {
        SettingsScreen(state = SettingsUiState(openSheet = SettingsSheet.Sort))
    }
}
