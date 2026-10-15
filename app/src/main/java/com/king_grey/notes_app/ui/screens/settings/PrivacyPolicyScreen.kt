package com.king_grey.notes_app.ui.screens.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.king_grey.notes_app.ui.screens.folders.BackButton
import com.king_grey.notes_app.ui.screens.notes.segmentShape
import com.king_grey.notes_app.ui.screens.shared.FlowNotesLargeTopAppBar
import com.king_grey.notes_app.ui.screens.shared.screenType
import com.king_grey.notes_app.ui.theme.FlowNotesTheme

private data class PolicySection(val title: String, val body: String)

private val PolicySections = listOf(
    PolicySection(
        "Your notes stay on your phone",
        "Flow Notes keeps your notes, folders and settings on this device. There is no account " +
                "and no sign in, and your notes are never uploaded to our servers.",
    ),
    PolicySection(
        "No tracking",
        "Flow Notes has no advertising, no analytics and no trackers. It does not collect " +
                "personal information or share anything about you with anyone.",
    ),
    PolicySection(
        "Backups",
        "If Android backup is turned on for your phone, your notes may be included in your " +
                "device backup. That backup is managed by your Google account, not by Flow Notes. " +
                "Uninstalling the app removes everything it stored on the phone.",
    ),
    PolicySection(
        "Questions",
        "If you have questions about this policy, contact the developer through the app's " +
                "store listing.",
    ),
)

/** Settings → Privacy policy: a large app bar with back, and the policy as grouped cards. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PrivacyPolicyScreen(
    modifier: Modifier = Modifier,
    onBack: () -> Unit = {},
) {
    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior()
    val colors = MaterialTheme.colorScheme

    Scaffold(
        modifier = modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        containerColor = colors.background,
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        topBar = {
            FlowNotesLargeTopAppBar(
                title = "Privacy policy",
                subtitle = "Updated 1 October 2026",
                scrollBehavior = scrollBehavior,
                navigationIcon = { BackButton(onBack) },
            )
        },
    ) { padding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(
                start = 16.dp,
                end = 16.dp,
                top = padding.calculateTopPadding() + 4.dp,
                bottom = padding.calculateBottomPadding() + 24.dp,
            ),
        ) {
            itemsIndexed(PolicySections, key = { _, section -> section.title }) { index, section ->
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = if (index == 0) 0.dp else 2.dp)
                        .clip(segmentShape(index, PolicySections.size))
                        .background(colors.surfaceContainer)
                        .padding(horizontal = 18.dp, vertical = 16.dp),
                ) {
                    Text(
                        text = section.title,
                        modifier = Modifier.semantics { heading() },
                        style = screenType(16f, FontWeight.SemiBold, lineHeight = 22f),
                        color = colors.onSurface,
                    )
                    Text(
                        text = section.body,
                        modifier = Modifier.padding(top = 6.dp),
                        style = screenType(15f, FontWeight.Normal, lineHeight = 22f),
                        color = colors.onSurfaceVariant,
                    )
                }
            }
        }
    }
}

@Preview(widthDp = 360, heightDp = 760, showBackground = true)
@Composable
private fun PrivacyPolicyPreview() {
    FlowNotesTheme(dynamicColor = false) { PrivacyPolicyScreen() }
}
