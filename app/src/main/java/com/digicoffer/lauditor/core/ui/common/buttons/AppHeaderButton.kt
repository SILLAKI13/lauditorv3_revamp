package com.digicoffer.lauditor.core.ui.common.buttons

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.digicoffer.lauditor.R
import com.digicoffer.lauditor.core.designsystem.colors.ColorTokens

private val GillSans = FontFamily(Font(R.font.gill_sans))

/**
 * Reusable header action button (e.g. "+ Add Relationship", "+ Create Matter", "+ Upload New", "Document View").
 * Encapsulates the standard white card (RoundedCornerShape(10.dp)), 2.dp elevation,
 * 30.dp circular Brand Blue icon container, and 13.sp Brand Blue title text.
 */
@Composable
fun AppHeaderButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    iconRes: Int? = null,
    iconContentDescription: String? = null,
    enabled: Boolean = true,
    containerColor: Color = Color.White,
    contentColor: Color = ColorTokens.BluePrimary,
    iconCircleColor: Color = ColorTokens.BluePrimary,
    iconTint: Color = Color.White,
    border: BorderStroke? = null
) {
    Card(
        onClick = onClick,
        enabled = enabled,
        colors = CardDefaults.cardColors(containerColor = containerColor),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        shape = RoundedCornerShape(10.dp),
        border = border,
        modifier = modifier
            .wrapContentSize()
            .height(40.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .padding(start = 6.dp, end = 12.dp)
                .fillMaxHeight()
        ) {
            Box(
                modifier = Modifier
                    .size(30.dp)
                    .background(iconCircleColor, shape = CircleShape),
                contentAlignment = Alignment.Center
            ) {
                if (iconRes != null && iconRes != 0) {
                    Icon(
                        painter = painterResource(id = iconRes),
                        contentDescription = iconContentDescription,
                        tint = iconTint,
                        modifier = Modifier.size(14.dp)
                    )
                } else {
                    Text(
                        text = "+",
                        color = iconTint,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(bottom = 2.dp)
                    )
                }
            }
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = text,
                color = contentColor,
                fontFamily = GillSans,
                fontSize = 13.sp,
                fontWeight = FontWeight.Normal,
                maxLines = 1
            )
        }
    }
}
