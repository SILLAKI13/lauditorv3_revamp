package com.digicoffer.lauditor.core.ui.foundation

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import com.digicoffer.lauditor.core.designsystem.theme.LauditorTheme

@Composable
fun AppSpacer(
    modifier: Modifier = Modifier,
    height: Dp = LauditorTheme.dimensions.medium,
    width: Dp = LauditorTheme.dimensions.none
) {
    Spacer(modifier = modifier.height(height).width(width))
}

@Preview(showBackground = true, name = "AppSpacer Preview")
@Composable
fun AppSpacerPreview() {
    LauditorTheme {
        AppSpacer(height = LauditorTheme.dimensions.large)
    }
}
