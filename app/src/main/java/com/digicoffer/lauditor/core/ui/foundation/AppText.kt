package com.digicoffer.lauditor.core.ui.foundation

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import com.digicoffer.lauditor.core.designsystem.theme.LauditorTheme

@Composable
fun AppText(
    text: String,
    modifier: Modifier = Modifier,
    style: TextStyle = LauditorTheme.typography.bodyRegular,
    color: Color = LauditorTheme.colors.onBackground,
    maxLines: Int = Int.MAX_VALUE,
    overflow: TextOverflow = TextOverflow.Clip,
    textAlign: TextAlign? = null
) {
    Text(
        text = text,
        modifier = modifier,
        style = style,
        color = color,
        maxLines = maxLines,
        overflow = overflow,
        textAlign = textAlign
    )
}

@Preview(showBackground = true, name = "AppText Light Preview")
@Composable
fun AppTextLightPreview() {
    LauditorTheme(darkTheme = false) {
        AppText(text = "AppText Component Light")
    }
}

@Preview(showBackground = true, name = "AppText Dark Preview")
@Composable
fun AppTextDarkPreview() {
    LauditorTheme(darkTheme = true) {
        AppText(text = "AppText Component Dark")
    }
}
