package com.digicoffer.lauditor.core.ui.common.feedback

import com.digicoffer.lauditor.core.ui.common.buttons.AppButton

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.digicoffer.lauditor.core.designsystem.theme.LauditorTheme
import com.digicoffer.lauditor.core.ui.common.foundation.AppSpacer
import com.digicoffer.lauditor.core.ui.common.foundation.AppText

@Composable
fun AppEmptyState(
    title: String,
    modifier: Modifier = Modifier,
    description: String? = null,
    actionText: String? = null,
    onActionClick: (() -> Unit)? = null,
    // Flexible style overrides:
    imageRes: Int? = null,
    imageSize: Dp = 130.dp,
    titleStyle: TextStyle? = null,
    descriptionStyle: TextStyle? = null,
    titleColor: Color? = null,
    descriptionColor: Color? = null,
    verticalArrangement: Arrangement.Vertical = Arrangement.Top
) {
    Column(
        modifier = modifier.fillMaxSize().padding(LauditorTheme.dimensions.large),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = verticalArrangement
    ) {
        if (imageRes != null) {
            Image(
                painter = painterResource(id = imageRes),
                contentDescription = "Empty",
                modifier = Modifier.size(imageSize)
            )
            AppSpacer(height = 20.dp)
        } else {
            AppSpacer(height = LauditorTheme.dimensions.giant)
        }

        if (titleStyle != null) {
            Text(
                text = title,
                style = titleStyle,
                color = titleColor ?: Color.Black,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
        } else {
            Text(
                text = title,
                style = LauditorTheme.typography.headerTitle,
                color = titleColor ?: Color.Black,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
        }

        if (description != null) {
            AppSpacer(height = LauditorTheme.dimensions.small)
            if (descriptionStyle != null) {
                Text(
                    text = description,
                    style = descriptionStyle,
                    color = descriptionColor ?: Color.Gray,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )
            } else {
                Text(
                    text = description,
                    style = LauditorTheme.typography.bodyRegular,
                    color = descriptionColor ?: LauditorTheme.colors.onSurfaceVariant,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }

        if (actionText != null && onActionClick != null) {
            AppSpacer(height = LauditorTheme.dimensions.large)
            AppButton(text = actionText, onClick = onActionClick)
        }
    }
}

@Preview(showBackground = true, name = "AppEmptyState Preview")
@Composable
fun AppEmptyStatePreview() {
    LauditorTheme {
        AppEmptyState(title = "No Data Found", description = "Try adjusting your filter settings.", actionText = "Refresh", onActionClick = {})
    }
}
