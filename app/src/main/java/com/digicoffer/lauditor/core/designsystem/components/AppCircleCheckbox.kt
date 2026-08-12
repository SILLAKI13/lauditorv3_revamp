package com.digicoffer.lauditor.core.designsystem.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.digicoffer.lauditor.R
import com.digicoffer.lauditor.core.designsystem.theme.LauditorTheme

@Composable
fun AppCircleCheckbox(
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
    size: Dp = 22.dp,
    enabled: Boolean = true,
    tint: Color = Color.Unspecified
) {
    val interactionSource = remember { MutableInteractionSource() }
    
    Icon(
        painter = painterResource(
            id = if (checked) R.drawable.ic_checkbox_checked else R.drawable.ic_checkbox_unchecked
        ),
        contentDescription = "Selection Checkbox",
        tint = tint,
        modifier = modifier
            .size(size)
            .clickable(
                enabled = enabled,
                interactionSource = interactionSource,
                indication = null // Disable default rectangular ripple to keep visual parity clean
            ) {
                onCheckedChange(!checked)
            }
    )
}

@Preview(showBackground = true, name = "AppCircleCheckbox Checked Preview")
@Composable
fun AppCircleCheckboxCheckedPreview() {
    LauditorTheme {
        AppCircleCheckbox(checked = true, onCheckedChange = {})
    }
}

@Preview(showBackground = true, name = "AppCircleCheckbox Unchecked Preview")
@Composable
fun AppCircleCheckboxUncheckedPreview() {
    LauditorTheme {
        AppCircleCheckbox(checked = false, onCheckedChange = {})
    }
}
