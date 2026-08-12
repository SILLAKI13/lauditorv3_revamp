package com.digicoffer.lauditor.core.ui.foundation

import androidx.compose.material3.HorizontalDivider
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import com.digicoffer.lauditor.core.designsystem.theme.LauditorTheme

@Composable
fun AppDivider(
    modifier: Modifier = Modifier,
    thickness: Dp = LauditorTheme.dimensions.dividerThickness,
    color: Color = LauditorTheme.colors.divider
) {
    HorizontalDivider(
        modifier = modifier,
        thickness = thickness,
        color = color
    )
}

@Preview(showBackground = true, name = "AppDivider Preview")
@Composable
fun AppDividerPreview() {
    LauditorTheme {
        AppDivider()
    }
}
