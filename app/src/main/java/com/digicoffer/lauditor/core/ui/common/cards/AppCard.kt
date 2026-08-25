package com.digicoffer.lauditor.core.ui.common.cards

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import com.digicoffer.lauditor.core.designsystem.theme.LauditorTheme
import com.digicoffer.lauditor.core.ui.common.foundation.AppText

@Composable
fun AppCard(
    modifier: Modifier = Modifier,
    elevation: Dp = LauditorTheme.dimensions.cardElevation,
    border: BorderStroke? = null,
    backgroundColor: Color = LauditorTheme.colors.surface,
    shape: Shape? = null,
    content: @Composable ColumnScope.() -> Unit
) {
    Card(
        modifier = modifier,
        elevation = CardDefaults.cardElevation(defaultElevation = elevation),
        border = border,
        colors = CardDefaults.cardColors(containerColor = backgroundColor),
        shape = shape ?: LauditorTheme.shapes.medium,
        content = content
    )
}

@Preview(showBackground = true, name = "AppCard Preview")
@Composable
fun AppCardPreview() {
    LauditorTheme {
        AppCard {
            AppText("Card Surface Content")
        }
    }
}
