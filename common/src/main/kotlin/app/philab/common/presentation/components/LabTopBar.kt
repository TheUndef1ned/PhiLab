package app.philab.common.presentation.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import app.philab.common.presentation.components.effects.topBarBackdrop
import app.philab.common.presentation.theme.LabDimens
import app.philab.common.presentation.theme.LabSpacing
import app.philab.common.presentation.theme.LabTheme

/** Transparent top bar over a blurred backdrop: navigation on the left, actions on the right, title centered. */
@Composable
fun LabTopBar(
    title: @Composable () -> Unit = {},
    leftActions: @Composable () -> Unit = {},
    rightActions: @Composable RowScope.() -> Unit = {},
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .topBarBackdrop()
            .windowInsetsPadding(WindowInsets.statusBars)
            .height(LabDimens.topBarHeight)
    ) {
        Box(
            modifier = Modifier
                .align(Alignment.Center)
                .padding(horizontal = LabDimens.topBarHeight),
            contentAlignment = Alignment.Center,
        ) {
            CompositionLocalProvider(LocalContentColor provides LabTheme.colors.text) { title() }
        }
        CompositionLocalProvider(LocalContentColor provides LabTheme.colors.primary) {
            Box(Modifier.align(Alignment.CenterStart).padding(start = LabSpacing.xs)) { leftActions() }
            Row(
                modifier = Modifier.align(Alignment.CenterEnd).padding(end = LabSpacing.xs),
                verticalAlignment = Alignment.CenterVertically,
                content = rightActions,
            )
        }
    }
}

@Composable
fun TopBarTitle(text: String, subtitle: String? = null) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = text,
            style = LabTheme.type.title,
            color = LabTheme.colors.text,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
        if (subtitle != null) {
            Text(
                text = subtitle,
                style = LabTheme.type.caption,
                color = LabTheme.colors.textMuted,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                textAlign = TextAlign.Center,
            )
        }
    }
}
