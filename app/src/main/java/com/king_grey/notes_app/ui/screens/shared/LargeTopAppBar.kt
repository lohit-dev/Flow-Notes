package com.king_grey.notes_app.ui.screens.shared

import androidx.compose.foundation.layout.RowScope
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.LargeFlexibleTopAppBar
import androidx.compose.material3.MaterialShapes
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.TopAppBarScrollBehavior
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.lerp
import androidx.compose.ui.text.style.TextOverflow
import androidx.graphics.shapes.CornerRounding
import androidx.graphics.shapes.RoundedPolygon

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
val MaterialShapes.Companion.Pebble: RoundedPolygon
    get() = RoundedPolygon(
        numVertices = 5,
        radius = 1f,
        rounding = CornerRounding(0.4f),
    ).normalized()

@Composable
fun FlowNotesLargeTopAppBar(
    title: String,
    scrollBehavior: TopAppBarScrollBehavior,
    modifier: Modifier = Modifier,
    scrolledContainerColor: Color = MaterialTheme.colorScheme.surfaceContainer,
    actions: @Composable RowScope.() -> Unit = {},
    subtitle: String? = null,
    navigationIcon: @Composable () -> Unit = {},
) {
    val expandedTitle = screenType(44f, FontWeight.Black, lineHeight = 46.2f, letterSpacing = -1.2f)
    val collapsedTitle = screenType(22f, FontWeight.Bold, lineHeight = 28f)

    LargeFlexibleTopAppBar(
        modifier = modifier,
        title = {
            Text(
                text = title,
                modifier = Modifier.semantics { heading() },
                style = lerp(
                    expandedTitle,
                    collapsedTitle,
                    scrollBehavior.state.collapsedFraction,
                ),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        },
        subtitle = subtitle?.let {
            {
                Text(
                    text = it,
                    style = screenType(15f, FontWeight.Normal, lineHeight = 20f),
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                )
            }
        },
        navigationIcon = navigationIcon,
        expandedHeight = TopAppBarDefaults.LargeAppBarExpandedHeight,
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = MaterialTheme.colorScheme.background,
            scrolledContainerColor = scrolledContainerColor,
        ),
        actions = actions,
        scrollBehavior = scrollBehavior,
    )
}
