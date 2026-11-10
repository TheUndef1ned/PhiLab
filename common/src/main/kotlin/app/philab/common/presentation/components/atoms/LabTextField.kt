package app.philab.common.presentation.components.atoms

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import app.philab.common.presentation.theme.LabDimens
import app.philab.common.presentation.theme.LabShapes
import app.philab.common.presentation.theme.LabTheme
import app.philab.common.presentation.theme.LabSpacing

/**
 * Labeled input: rounded field with a primary border on focus.
 * Accepts expressions: "1/4", "10^-3", "√5" — parsed by ExpressionParser in the store.
 */
@Composable
fun LabTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    modifier: Modifier = Modifier,
    placeholder: String = "",
    error: Boolean = false,
    supporting: String? = null,
    keyboardType: KeyboardType = KeyboardType.Text,
    singleLine: Boolean = true,
) {
    var focused by remember { mutableStateOf(false) }
    val borderColor = when {
        error -> LabTheme.colors.error
        focused -> LabTheme.colors.primary
        else -> LabTheme.colors.divider
    }
    Column(modifier = modifier) {
        Text(
            text = label,
            style = LabTheme.type.caption,
            color = LabTheme.colors.textMuted,
            modifier = Modifier.padding(start = LabSpacing.xs, bottom = LabSpacing.xs)
        )
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = LabDimens.fieldHeight)
                .background(LabTheme.colors.card, LabShapes.field)
                .border(LabDimens.divider, borderColor, LabShapes.field)
                .padding(horizontal = LabSpacing.m),
            contentAlignment = Alignment.CenterStart
        ) {
            if (value.isEmpty() && placeholder.isNotEmpty()) {
                Text(
                    text = placeholder,
                    style = LabTheme.type.body,
                    color = LabTheme.colors.textDisabled
                )
            }
            BasicTextField(
                value = value,
                onValueChange = onValueChange,
                singleLine = singleLine,
                textStyle = LabTheme.type.body.copy(color = LabTheme.colors.text),
                cursorBrush = SolidColor(LabTheme.colors.primary),
                keyboardOptions = KeyboardOptions(keyboardType = keyboardType, imeAction = ImeAction.Done),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = LabSpacing.m)
                    .onFocusChanged { focused = it.isFocused }
            )
        }
        if (supporting != null) {
            Text(
                text = supporting,
                style = LabTheme.type.hint,
                color = if (error) LabTheme.colors.error else LabTheme.colors.textMuted,
                modifier = Modifier.padding(start = LabSpacing.xs, top = LabSpacing.xs)
            )
        }
    }
}
