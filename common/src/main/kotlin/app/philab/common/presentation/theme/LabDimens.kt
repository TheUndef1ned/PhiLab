package app.philab.common.presentation.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.unit.dp

object LabSpacing {
    val micro = 2.dp
    val xs = 4.dp
    val s = 8.dp
    val m = 12.dp
    val l = 16.dp
    val xl = 24.dp
    val xxl = 32.dp
    val screen = 16.dp
}

object LabShapes {
    val card = RoundedCornerShape(18.dp)
    val cardSmall = RoundedCornerShape(12.dp)
    val field = RoundedCornerShape(14.dp)
    val button = RoundedCornerShape(16.dp)
    val cell = RoundedCornerShape(8.dp)
    val icon = RoundedCornerShape(10.dp)
    val toast = RoundedCornerShape(16.dp)
    val pill = RoundedCornerShape(percent = 50)
    /** Answer block: a "tab" cut on the bottom-left corner. */
    val answer = RoundedCornerShape(topStart = 18.dp, topEnd = 18.dp, bottomEnd = 18.dp, bottomStart = 6.dp)
    /** Formula block in the cheat-sheet. */
    val formula = RoundedCornerShape(topStart = 6.dp, topEnd = 14.dp, bottomEnd = 14.dp, bottomStart = 14.dp)
}

object LabDimens {
    val topBarHeight = 56.dp
    val divider = 1.dp
    val icon = 20.dp
    val fieldHeight = 50.dp
    val chartHeight = 280.dp
    val tableCellWidth = 86.dp
    val tableCellHeight = 36.dp
    val bottomBarHeight = 64.dp
    val bottomBarIcon = 24.dp
    val bottomBarMargin = 20.dp
    /** Space the floating bottom bar takes over the content. */
    val bottomBarReserved = 108.dp
    val cardElevation = 6.dp
    val floatingElevation = 14.dp
}
