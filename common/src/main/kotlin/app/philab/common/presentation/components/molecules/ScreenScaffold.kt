package app.philab.common.presentation.components.molecules

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import app.philab.common.presentation.theme.LabTheme
import app.philab.common.utils.LocalHazeState
import dev.chrisbanes.haze.hazeSource

/**
 * Common screen frame: blurred top bar over a LazyColumn (the blur source);
 * [insideTabs] reserves space for the floating Liquid Glass tab bar.
 */
@Composable
fun ScreenScaffold(
    topBar: @Composable () -> Unit,
    insideTabs: Boolean,
    modifier: Modifier = Modifier,
    listState: LazyListState = rememberLazyListState(),
    itemSpacing: Dp = 16.dp,
    content: LazyListScope.() -> Unit,
) {
    val hazeState = LocalHazeState.current
    val navBottom = WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding()
    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = LabTheme.colors.background,
        topBar = topBar,
    ) { paddingValues ->
        LazyColumn(
            state = listState,
            modifier = Modifier
                .fillMaxSize()
                .background(LabTheme.colors.background)
                .hazeSource(hazeState)
                .imePadding(),
            contentPadding = PaddingValues(
                top = paddingValues.calculateTopPadding() + 12.dp,
                bottom = if (insideTabs) 108.dp else navBottom + 24.dp,
            ),
            verticalArrangement = Arrangement.spacedBy(itemSpacing),
            content = content,
        )
    }
}
