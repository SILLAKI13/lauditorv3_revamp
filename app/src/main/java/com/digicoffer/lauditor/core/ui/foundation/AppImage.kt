package com.digicoffer.lauditor.core.ui.foundation

import androidx.annotation.DrawableRes
import androidx.compose.foundation.Image
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import com.digicoffer.lauditor.core.designsystem.icons.LauditorIcons
import com.digicoffer.lauditor.core.designsystem.theme.LauditorTheme

@Composable
fun AppImage(
    @DrawableRes imageRes: Int,
    contentDescription: String?,
    modifier: Modifier = Modifier,
    contentScale: ContentScale = ContentScale.Crop
) {
    Image(
        painter = painterResource(id = imageRes),
        contentDescription = contentDescription,
        modifier = modifier,
        contentScale = contentScale
    )
}

@Preview(showBackground = true, name = "AppImage Preview")
@Composable
fun AppImagePreview() {
    LauditorTheme {
        AppImage(imageRes = LauditorIcons.Profile, contentDescription = "Sample Image")
    }
}
