package com.king_grey.notes_app.ui.screens.search

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialShapes
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SheetValue
import androidx.compose.material3.Text
import androidx.compose.material3.rememberBottomSheetState
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.material3.toShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import com.king_grey.notes_app.ui.screens.folders.BackButton
import com.king_grey.notes_app.ui.screens.folders.CloseIcon
import com.king_grey.notes_app.ui.screens.folders.EmptyBlock
import com.king_grey.notes_app.ui.screens.folders.FolderColor
import com.king_grey.notes_app.ui.screens.folders.FolderItem
import com.king_grey.notes_app.ui.screens.folders.strokeIcon
import com.king_grey.notes_app.ui.screens.notes.NoteChip
import com.king_grey.notes_app.ui.screens.notes.NoteItem
import com.king_grey.notes_app.ui.screens.notes.NoteRow
import com.king_grey.notes_app.ui.screens.shared.screenType
import com.king_grey.notes_app.ui.theme.FlowNotesTheme

// ─────────────────────────────────────────────────────────────────────────────
// Models
// ─────────────────────────────────────────────────────────────────────────────

/**
 * What the search screen shows. [results] are for the current query and folder chip, the view
 * model does the matching (title and body) and may debounce [SearchScreen]'s onQueryChange.
 */
data class SearchUiState(
    val folders: List<FolderItem> = emptyList(),
    /** null = All. */
    val selectedFolderId: Long? = null,
    val recent: List<String> = emptyList(),
    val results: List<NoteItem> = emptyList(),
)

// ─────────────────────────────────────────────────────────────────────────────
// Screen
// ─────────────────────────────────────────────────────────────────────────────

/**
 * Full-screen search (the expanded state of the Notes search pill).
 *
 * Before typing: folder chips and the Recent list. With a query: a result count and the matching
 * notes with the match highlighted, or the no-results state. The mic opens a listening sheet and
 * puts what was said into the field.
 *
 * Needs `RECORD_AUDIO` in the manifest for the mic (asked at the first tap), and on Android 11+
 * `<queries><intent><action android:name="android.speech.RecognitionService"/></intent></queries>`
 * so the recognizer can be found. If there is no recognizer the mic is not shown.
 */
@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun SearchScreen(
    state: SearchUiState,
    modifier: Modifier = Modifier,
    initialQuery: String = "",
    onBack: () -> Unit = {},
    onQueryChange: (String) -> Unit = {},
    onFolderFilter: (folderId: Long?) -> Unit = {},
    onOpenNote: (NoteItem) -> Unit = {},
    /** Called when a search is committed (keyboard Search, a result opened, a recent tapped). */
    onSubmit: (String) -> Unit = {},
    onClearRecent: () -> Unit = {},
) {
    val colors = MaterialTheme.colorScheme
    var query by rememberSaveable { mutableStateOf(initialQuery) }
    val focus = remember { FocusRequester() }
    val trimmed = query.trim()
    val currentOnQueryChange by rememberUpdatedState(onQueryChange)

    LaunchedEffect(query) { currentOnQueryChange(query) }
    LaunchedEffect(Unit) { if (query.isEmpty()) focus.requestFocus() }

    val voice = rememberVoiceSearch { heard ->
        query = heard
        onSubmit(heard.trim())
    }
    if (voice.listening) VoiceSheet(onCancel = voice.cancel)

    Scaffold(
        modifier = modifier,
        containerColor = colors.surfaceContainerLow,
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .statusBarsPadding()
                .imePadding(),
        ) {
            // .qtop: back, the field, then mic (empty) or clear (typed)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 64.dp)
                    .padding(horizontal = 4.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                BackButton(onBack)
                BasicTextField(
                    value = query,
                    onValueChange = { query = it },
                    modifier = Modifier
                        .weight(1f)
                        .focusRequester(focus),
                    singleLine = true,
                    textStyle = screenType(18f, FontWeight.Normal, lineHeight = 24f)
                        .copy(color = colors.onSurface),
                    cursorBrush = SolidColor(colors.primary),
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                    keyboardActions = KeyboardActions(onSearch = {
                        if (trimmed.isNotEmpty()) onSubmit(
                            trimmed
                        )
                    }),
                    decorationBox = { inner ->
                        Box(contentAlignment = Alignment.CenterStart) {
                            if (query.isEmpty()) {
                                Text(
                                    text = "Search notes",
                                    style = screenType(18f, FontWeight.Normal, lineHeight = 24f),
                                    color = colors.onSurfaceVariant,
                                )
                            }
                            inner()
                        }
                    },
                )
                if (query.isEmpty()) {
                    if (voice.available) {
                        IconButton(onClick = voice.start, modifier = Modifier.size(48.dp)) {
                            Icon(
                                MicIcon,
                                contentDescription = "Search by voice",
                                tint = colors.onSurfaceVariant
                            )
                        }
                    }
                } else {
                    IconButton(
                        onClick = { query = ""; focus.requestFocus() },
                        modifier = Modifier.size(48.dp)
                    ) {
                        Icon(
                            CloseIcon,
                            contentDescription = "Clear",
                            tint = colors.onSurfaceVariant
                        )
                    }
                }
            }
            HorizontalDivider(color = colors.outlineVariant.copy(alpha = 0.7f))

            if (state.folders.isNotEmpty()) {
                LazyRow(
                    modifier = Modifier.padding(top = 10.dp, bottom = 2.dp),
                    contentPadding = PaddingValues(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    item(key = "chip-all") {
                        NoteChip("All", state.selectedFolderId == null) { onFolderFilter(null) }
                    }
                    items(state.folders.size, key = { "chip-${state.folders[it].id}" }) { i ->
                        val folder = state.folders[i]
                        NoteChip(folder.name, state.selectedFolderId == folder.id) {
                            onFolderFilter(
                                folder.id
                            )
                        }
                    }
                }
            }

            when {
                trimmed.isEmpty() -> RecentList(
                    recent = state.recent,
                    onPick = { picked ->
                        query = picked
                        onSubmit(picked)
                    },
                    onFill = { query = it },
                    onClear = onClearRecent,
                )

                state.results.isEmpty() -> {
                    val folderName =
                        state.folders.firstOrNull { it.id == state.selectedFolderId }?.name
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f),
                        contentAlignment = Alignment.Center,
                    ) {
                        EmptyBlock(
                            art = MaterialShapes.Flower.toShape(),
                            accent = MaterialShapes.Puffy.toShape(),
                            icon = SearchXIcon,
                            title = "No results for “$trimmed”",
                            text = if (folderName != null) {
                                "Nothing in $folderName matches. Try a shorter word or search every folder."
                            } else {
                                "Try a shorter word or check the spelling."
                            },
                            actionLabel = if (folderName != null) "Search all notes" else null,
                            onAction = { onFolderFilter(null) },
                            compact = true,
                            modifier = Modifier.padding(bottom = 24.dp),
                        )
                    }
                }

                else -> ResultList(
                    results = state.results,
                    query = trimmed,
                    onOpen = {
                        onSubmit(trimmed)
                        onOpenNote(it)
                    },
                )
            }
        }
    }
}

/** .qlab + .qrow2: "Recent" with Clear all, then one row per search. */
@Composable
private fun RecentList(
    recent: List<String>,
    onPick: (String) -> Unit,
    onFill: (String) -> Unit,
    onClear: () -> Unit,
) {
    if (recent.isEmpty()) return
    val colors = MaterialTheme.colorScheme
    LazyColumn(modifier = Modifier.fillMaxSize()) {
        item(key = "label") {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 20.dp, end = 20.dp, top = 14.dp, bottom = 4.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = "Recent",
                    modifier = Modifier.weight(1f),
                    style = screenType(14f, FontWeight.SemiBold, lineHeight = 20f),
                    color = colors.onSurfaceVariant,
                )
                Text(
                    text = "Clear all",
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .clickable(role = Role.Button, onClick = onClear)
                        .padding(horizontal = 4.dp, vertical = 2.dp),
                    style = screenType(14f, FontWeight.SemiBold, lineHeight = 20f),
                    color = colors.primary,
                )
            }
        }
        items(recent.size, key = { "r-${recent[it]}" }) { i ->
            val term = recent[i]
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 56.dp)
                    .clickable(onClick = { onPick(term) })
                    .padding(start = 20.dp, end = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                Icon(
                    ClockIcon,
                    contentDescription = null,
                    modifier = Modifier.size(22.dp),
                    tint = colors.onSurfaceVariant
                )
                Text(
                    text = term,
                    modifier = Modifier.weight(1f),
                    style = screenType(16f, FontWeight.Normal, lineHeight = 22f),
                    color = colors.onSurface,
                )
                IconButton(onClick = { onFill(term) }, modifier = Modifier.size(40.dp)) {
                    Icon(
                        ArrowNorthWestIcon,
                        contentDescription = "Put “$term” in the search box",
                        modifier = Modifier.size(20.dp),
                        tint = colors.onSurfaceVariant,
                    )
                }
            }
        }
    }
}

/** .qsum + .qres: "3 notes" then a segmented list with the match highlighted. */
@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
private fun ResultList(results: List<NoteItem>, query: String, onOpen: (NoteItem) -> Unit) {
    val colors = MaterialTheme.colorScheme
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(start = 8.dp, end = 8.dp, bottom = 24.dp),
    ) {
        item(key = "summary") {
            Text(
                text = if (results.size == 1) "1 note" else "${results.size} notes",
                modifier = Modifier.padding(start = 12.dp, end = 12.dp, top = 12.dp, bottom = 6.dp),
                style = screenType(14f, FontWeight.SemiBold, lineHeight = 20f),
                color = colors.onSurfaceVariant,
            )
        }
        items(results.size, key = { "res-${results[it].id}" }) { i ->
            NoteRow(
                note = results[i],
                selecting = false,
                selected = false,
                shapes = ListItemDefaults.segmentedShapes(index = i, count = results.size),
                onClick = { onOpen(results[i]) },
                highlight = query,
            )
            if (i < results.lastIndex) Spacer(Modifier.height(2.dp))
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Voice input (#f-voice)
// ─────────────────────────────────────────────────────────────────────────────

private class VoiceSearch(
    val available: Boolean,
    val listening: Boolean,
    val start: () -> Unit,
    val cancel: () -> Unit,
)

/** Wraps SpeechRecognizer: asks for the mic once, listens once, hands back the best match. */
@Composable
private fun rememberVoiceSearch(onResult: (String) -> Unit): VoiceSearch {
    val context = LocalContext.current
    val available = remember { SpeechRecognizer.isRecognitionAvailable(context) }
    val recognizer =
        remember { if (available) SpeechRecognizer.createSpeechRecognizer(context) else null }
    var listening by remember { mutableStateOf(false) }
    val currentOnResult by rememberUpdatedState(onResult)

    DisposableEffect(recognizer) {
        recognizer?.setRecognitionListener(object : RecognitionListener {
            override fun onResults(results: Bundle?) {
                listening = false
                val best = results
                    ?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                    ?.firstOrNull()
                if (!best.isNullOrBlank()) currentOnResult(best)
            }

            override fun onError(error: Int) {
                listening = false
            }

            override fun onReadyForSpeech(params: Bundle?) = Unit
            override fun onBeginningOfSpeech() = Unit
            override fun onRmsChanged(rmsdB: Float) = Unit
            override fun onBufferReceived(buffer: ByteArray?) = Unit
            override fun onEndOfSpeech() = Unit
            override fun onPartialResults(partialResults: Bundle?) = Unit
            override fun onEvent(eventType: Int, params: Bundle?) = Unit
        })
        onDispose { recognizer?.destroy() }
    }

    fun begin() {
        val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH)
            .putExtra(
                RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                RecognizerIntent.LANGUAGE_MODEL_FREE_FORM
            )
        listening = true
        recognizer?.startListening(intent)
    }

    val permission =
        rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
            if (granted) begin()
        }

    return VoiceSearch(
        available = available,
        listening = listening,
        start = {
            val granted =
                ContextCompat.checkSelfPermission(context, Manifest.permission.RECORD_AUDIO) ==
                        PackageManager.PERMISSION_GRANTED
            if (granted) begin() else permission.launch(Manifest.permission.RECORD_AUDIO)
        },
        cancel = {
            recognizer?.cancel()
            listening = false
        },
    )
}

/** .vc: a 32dp-cornered card from the bottom, a pulsing mic, "Listening…", a few moving bars, Cancel. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun VoiceSheet(onCancel: () -> Unit) {
    val colors = MaterialTheme.colorScheme
    val transition = rememberInfiniteTransition(label = "voice")
    val pulse by transition.animateFloat(
        initialValue = 1f,
        targetValue = 1.12f,
        animationSpec = infiniteRepeatable(tween(900, easing = LinearEasing), RepeatMode.Reverse),
        label = "pulse",
    )

    ModalBottomSheet(
        onDismissRequest = onCancel,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
        containerColor = colors.surfaceContainerHigh,
        shape = RoundedCornerShape(topStart = 32.dp, topEnd = 32.dp),
        dragHandle = null,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 24.dp, end = 24.dp, top = 26.dp, bottom = 34.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            // .vr / .vm: two soft rings around a 72dp mic
            Box(contentAlignment = Alignment.Center, modifier = Modifier.size(132.dp)) {
                Box(
                    modifier = Modifier
                        .size(132.dp)
                        .scale(pulse)
                        .clip(CircleShape)
                        .background(colors.primary.copy(alpha = 0.09f)),
                )
                Box(
                    modifier = Modifier
                        .size(104.dp)
                        .clip(CircleShape)
                        .background(colors.primary.copy(alpha = 0.18f)),
                )
                Box(
                    modifier = Modifier
                        .size(72.dp)
                        .clip(CircleShape)
                        .background(colors.primary),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(
                        MicIcon,
                        contentDescription = null,
                        modifier = Modifier.size(30.dp),
                        tint = colors.onPrimary
                    )
                }
            }
            Text(
                text = "Listening…",
                modifier = Modifier.padding(top = 14.dp),
                style = screenType(22f, FontWeight.Bold, lineHeight = 28f),
                color = colors.onSurface,
            )
            Text(
                text = "Say a title or a word from a note",
                style = screenType(14f, FontWeight.Normal, lineHeight = 20f),
                color = colors.onSurfaceVariant,
            )
            Row(
                modifier = Modifier
                    .padding(top = 12.dp)
                    .height(28.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(5.dp),
            ) {
                listOf(0, 120, 240, 120, 0).forEach { delay -> WaveBar(delay) }
            }
            FilledTonalButton(
                onClick = onCancel,
                modifier = Modifier
                    .padding(top = 12.dp)
                    .heightIn(min = 44.dp),
                shapes = ButtonDefaults.shapes(),
                colors = ButtonDefaults.filledTonalButtonColors(
                    containerColor = colors.secondaryContainer,
                    contentColor = colors.onSecondaryContainer,
                ),
                contentPadding = PaddingValues(horizontal = 24.dp),
            ) { Text("Cancel", style = screenType(14f, FontWeight.Bold, lineHeight = 20f)) }
        }
    }
}

@Composable
private fun WaveBar(delayMillis: Int) {
    val transition = rememberInfiniteTransition(label = "bar")
    val height by transition.animateFloat(
        initialValue = 6f,
        targetValue = 26f,
        animationSpec = infiniteRepeatable(
            animation = tween(450, delayMillis = delayMillis, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse,
        ),
        label = "barHeight",
    )
    Box(
        modifier = Modifier
            .width(4.dp)
            .height(height.dp)
            .clip(RoundedCornerShape(2.dp))
            .background(MaterialTheme.colorScheme.primary),
    )
}

// ─────────────────────────────────────────────────────────────────────────────
// Icons
// ─────────────────────────────────────────────────────────────────────────────

private val MicIcon by lazy {
    strokeIcon(
        "Mic",
        "M12 3a3 3 0 0 0-3 3v5a3 3 0 0 0 6 0V6a3 3 0 0 0-3-3zM5 11a7 7 0 0 0 14 0M12 18v3"
    )
}
private val ClockIcon by lazy {
    strokeIcon(
        "Clock",
        "M21 12a9 9 0 1 1-18 0a9 9 0 1 1 18 0zM12 7v5l3 2"
    )
}
private val ArrowNorthWestIcon by lazy { strokeIcon("NorthWest", "M17 17 7 7M7 15V7h8") }
private val SearchXIcon by lazy {
    strokeIcon(
        "SearchX",
        "M18 11a7 7 0 1 1-14 0a7 7 0 1 1 14 0zM20 20l-3.5-3.5M8.6 8.6l4.8 4.8M13.4 8.6l-4.8 4.8"
    )
}

// ─────────────────────────────────────────────────────────────────────────────
// Previews
// ─────────────────────────────────────────────────────────────────────────────

private val PreviewFolders = listOf(
    FolderItem(1, "Personal", FolderColor.Green, 12),
    FolderItem(2, "Work", FolderColor.Amber, 16),
    FolderItem(3, "Learning", FolderColor.Violet, 8),
    FolderItem(4, "Ideas", FolderColor.Rose, 6),
)

@Preview(widthDp = 360, heightDp = 640, showBackground = true)
@Composable
private fun SearchRecentPreview() {
    FlowNotesTheme(dynamicColor = false) {
        SearchScreen(
            state = SearchUiState(
                folders = PreviewFolders,
                recent = listOf("SwiftUI", "Hyderabad", "Grocery"),
            ),
        )
    }
}

@Preview(widthDp = 360, heightDp = 640, showBackground = true)
@Composable
private fun SearchResultsPreview() {
    FlowNotesTheme(dynamicColor = false) {
        SearchScreen(
            initialQuery = "swift",
            state = SearchUiState(
                folders = PreviewFolders,
                results = listOf(
                    NoteItem(
                        1,
                        "Swift learning plan",
                        "Views, state, bindings and navigation",
                        "Yesterday",
                        3,
                        "Learning",
                        FolderColor.Violet,
                        pinned = true
                    ),
                    NoteItem(
                        2,
                        "SwiftUI state cheat sheet",
                        "@State, @Binding, @Observable",
                        "Friday",
                        3,
                        "Learning",
                        FolderColor.Violet
                    ),
                    NoteItem(
                        3,
                        "Ledger app sketch",
                        "A small SwiftUI app for daily timeline",
                        "12/09/2026",
                        4,
                        "Ideas",
                        FolderColor.Rose
                    ),
                ),
            ),
        )
    }
}

@Preview(widthDp = 360, heightDp = 640, showBackground = true)
@Composable
private fun SearchNoResultsPreview() {
    FlowNotesTheme(dynamicColor = false) {
        SearchScreen(
            initialQuery = "kubernetes",
            state = SearchUiState(folders = PreviewFolders, selectedFolderId = 3),
        )
    }
}
