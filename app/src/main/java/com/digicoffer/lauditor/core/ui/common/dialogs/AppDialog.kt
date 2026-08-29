package com.digicoffer.lauditor.core.ui.common.dialogs

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.digicoffer.lauditor.R

private val GillSans = FontFamily(
    Font(R.font.gill_sans)
)

@Composable
fun AppDialog(
    title: String,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
    confirmText: String = "OK",
    dismissText: String? = null, // Default to null for single OK button alert style
    content: @Composable (() -> Unit)? = null
) {
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = modifier
                .fillMaxWidth(0.85f)
                .wrapContentHeight(),
            shape = RoundedCornerShape(10.dp),
            color = Color.White,
            shadowElevation = 6.dp
        ) {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Centered Bold Title (e.g. Alert !)
                Text(
                    text = title,
                    fontFamily = GillSans,
                    fontWeight = FontWeight.Bold,
                    fontSize = 17.sp,
                    color = Color.Black,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(top = 18.dp, bottom = 14.dp)
                )

                // Divider below Title
                HorizontalDivider(thickness = 0.5.dp, color = Color(0xFFE0E0E0))

                // Message Content (Centered)
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 20.dp),
                    contentAlignment = Alignment.Center
                ) {
                    if (content != null) {
                        content()
                    }
                }

                // Divider above Buttons
                HorizontalDivider(thickness = 0.5.dp, color = Color(0xFFE0E0E0))

                // Bottom Buttons Layout
                if (dismissText != null) {
                    // Two buttons side-by-side separated by a vertical divider
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxHeight()
                                .clickable { onDismiss() },
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = dismissText,
                                color = Color.Gray,
                                fontFamily = GillSans,
                                fontWeight = FontWeight.Bold,
                                fontSize = 15.sp,
                                textAlign = TextAlign.Center
                            )
                        }

                        // Vertical Divider
                        VerticalDivider(
                            modifier = Modifier.fillMaxHeight(),
                            thickness = 0.5.dp,
                            color = Color(0xFFE0E0E0)
                        )

                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxHeight()
                                .clickable { onConfirm() },
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = confirmText,
                                color = Color(0xFF0073C6),
                                fontFamily = GillSans,
                                fontWeight = FontWeight.Bold,
                                fontSize = 15.sp,
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                } else {
                    // Single OK button centered at the bottom
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                            .clickable { onConfirm() },
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = confirmText,
                            color = Color(0xFF0073C6),
                            fontFamily = GillSans,
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }
        }
    }
}
