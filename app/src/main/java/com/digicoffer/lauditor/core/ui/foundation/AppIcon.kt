package com.digicoffer.lauditor.core.ui.foundation

import androidx.annotation.DrawableRes
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import com.digicoffer.lauditor.core.designsystem.icons.LauditorIcons
import com.digicoffer.lauditor.core.designsystem.theme.LauditorTheme

@Composable
fun AppIcon(
    @DrawableRes iconRes: Int,
    contentDescription: String?,
    modifier: Modifier = Modifier,
    tint: Color = LauditorTheme.colors.onBackground,
    size: Dp = LauditorTheme.dimensions.iconMedium
) {
    Icon(
        painter = painterResource(id = iconRes),
        contentDescription = contentDescription,
        modifier = modifier.size(size),
        tint = tint
    )
}

@Preview(showBackground = true, name = "AppIcon Preview")
@Composable
fun AppIconPreview() {
    LauditorTheme {
        AppIcon(iconRes = LauditorIcons.Profile, contentDescription = "Profile Icon")
    }
}
