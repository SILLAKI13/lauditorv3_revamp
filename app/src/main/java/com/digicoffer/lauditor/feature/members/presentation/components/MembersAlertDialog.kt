package com.digicoffer.lauditor.feature.members.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Text
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
import com.digicoffer.lauditor.R

@Composable
fun MembersAlertDialog(
    title: String,
    message: String,
    onConfirm: () -> Unit,
    confirmLabel: String = "OK",
    showCancel: Boolean = false,
    cancelLabel: String = "Cancel",
    onCancel: (() -> Unit)? = null,
    onDismiss: () -> Unit
) {
    val activeBlue = Color(0xFF004D87)
    val darkGray = Color(0xFF333333)

    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 6.dp),
            modifier = Modifier
                .width(280.dp) // Exact compact width
                .wrapContentHeight()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 16.dp, bottom = 0.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Title
                Text(
                    text = title,
                    color = activeBlue,
                    fontWeight = FontWeight.Bold,
                    fontSize = 17.sp,
                    fontFamily = FontFamily(Font(R.font.gill_sans_regular)),
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(horizontal = 16.dp)
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Message
                Text(
                    text = message,
                    color = darkGray,
                    fontSize = 15.sp,
                    fontFamily = FontFamily(Font(R.font.gill_sans_regular)),
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(start = 16.dp, end = 16.dp, bottom = 16.dp)
                )

                // Horizontal Divider Line
                HorizontalDivider(
                    thickness = 0.7.dp,
                    color = Color(0xFFE5E5E5)
                )

                // Buttons
                if (!showCancel) {
                    // Simple Alert single button OK (centered text button, no background)
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onConfirm() }
                            .padding(vertical = 12.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = confirmLabel,
                            color = activeBlue,
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp,
                            fontFamily = FontFamily(Font(R.font.gill_sans_regular))
                        )
                    }
                } else {
                    // Confirmation dialog: two buttons side-by-side with grey background for cancel, blue background for confirm
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 12.dp, vertical = 10.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        // Cancel button
                        Button(
                            onClick = { onCancel?.invoke() ?: onDismiss() },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFEEEEEE)),
                            shape = RoundedCornerShape(10.dp),
                            contentPadding = PaddingValues(0.dp),
                            modifier = Modifier
                                .weight(1f)
                                .height(36.dp)
                        ) {
                            Text(
                                text = cancelLabel,
                                color = Color.Black,
                                fontWeight = FontWeight.Medium,
                                fontSize = 14.sp,
                                fontFamily = FontFamily(Font(R.font.gill_sans_regular))
                            )
                        }

                        // Confirm button
                        Button(
                            onClick = onConfirm,
                            colors = ButtonDefaults.buttonColors(containerColor = activeBlue),
                            shape = RoundedCornerShape(10.dp),
                            contentPadding = PaddingValues(0.dp),
                            modifier = Modifier
                                .weight(1f)
                                .height(36.dp)
                        ) {
                            Text(
                                text = confirmLabel,
                                color = Color.White,
                                fontWeight = FontWeight.Medium,
                                fontSize = 14.sp,
                                fontFamily = FontFamily(Font(R.font.gill_sans_regular))
                            )
                        }
                    }
                }
            }
        }
    }
}
