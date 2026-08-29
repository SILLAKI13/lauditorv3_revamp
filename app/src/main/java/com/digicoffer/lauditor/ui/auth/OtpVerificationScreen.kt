package com.digicoffer.lauditor.ui.auth

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.sp
import com.digicoffer.lauditor.R
import com.digicoffer.lauditor.core.ui.common.feedback.AppLoader
import com.digicoffer.lauditor.core.ui.common.foundation.AppSpacer
import com.digicoffer.lauditor.core.ui.common.inputs.AppOtpField

private val GillSans = FontFamily(Font(R.font.gill_sans))
private val GillSansBold = FontFamily(Font(R.font.gill_sans_bold))

@Composable
fun OtpVerificationScreen(
    otpValue: String,
    onOtpChange: (String) -> Unit,
    timerText: String,
    canResend: Boolean,
    onResendClick: () -> Unit,
    onVerifyClick: () -> Unit,
    onCancelClick: () -> Unit,
    onRegisterHereClick: () -> Unit,
    isLoading: Boolean = false,
    otpSentMessage: String? = null,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(colorResource(id = R.color.blue_pale))
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Card(
                shape = RoundedCornerShape(dimensionResource(id = R.dimen.thirty_dp)),
                colors = CardDefaults.cardColors(containerColor = colorResource(id = R.color.white)),
                elevation = CardDefaults.cardElevation(defaultElevation = dimensionResource(id = R.dimen.twenty_dp)),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(dimensionResource(id = R.dimen.twenty_dp))
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(dimensionResource(id = R.dimen.thirty_dp)),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Logo Image matching logo_layout.xml
                    Image(
                        painter = painterResource(id = R.drawable.lex_z_lawyer_horizontal_tp),
                        contentDescription = "Lex-Z Lawyers Logo",
                        modifier = Modifier
                            .padding(top = dimensionResource(id = R.dimen.ten_dp))
                            .width(dimensionResource(id = R.dimen.ThreeHundred_dp))
                            .height(dimensionResource(id = R.dimen.Eighty_dp))
                    )

                    // Success Confirmation Message matching otpverification.xml line 38
                    if (!otpSentMessage.isNullOrEmpty()) {
                        androidx.compose.material3.Text(
                            text = otpSentMessage,
                            style = TextStyle(
                                fontFamily = GillSansBold,
                                fontSize = 15.sp,
                                color = colorResource(id = R.color.Red)
                            ),
                            modifier = Modifier.padding(top = dimensionResource(id = R.dimen.ten_dp))
                        )
                    }

                    // Title Header matching otpverification.xml line 49
                    androidx.compose.material3.Text(
                        text = "Verify OTP",
                        style = TextStyle(
                            fontFamily = GillSansBold,
                            fontSize = 20.sp,
                            color = colorResource(id = R.color.black)
                        ),
                        modifier = Modifier.padding(top = dimensionResource(id = R.dimen.twenty_dp))
                    )

                    // Subtitle Label matching otpverification.xml line 60
                    androidx.compose.material3.Text(
                        text = "Enter 6-digit OTP",
                        style = TextStyle(
                            fontFamily = GillSans,
                            fontSize = 15.sp,
                            color = colorResource(id = R.color.black)
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(
                                start = dimensionResource(id = R.dimen.four_dp),
                                top = dimensionResource(id = R.dimen.twenty_dp),
                                bottom = dimensionResource(id = R.dimen.ten_dp)
                            )
                    )

                    // OTP Input Fields matching otpverification.xml line 73
                    AppOtpField(
                        otpValue = otpValue,
                        onOtpChange = onOtpChange
                    )

                    // Resend OTP Countdown Timer Link matching otpverification.xml line 186
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(
                                top = dimensionResource(id = R.dimen.ten_dp),
                                end = dimensionResource(id = R.dimen.ten_dp)
                            ),
                        contentAlignment = Alignment.CenterEnd
                    ) {
                        androidx.compose.material3.Text(
                            text = timerText,
                            style = TextStyle(
                                fontFamily = GillSansBold,
                                fontSize = 15.sp,
                                color = if (canResend) colorResource(id = R.color.Primary_new) else colorResource(id = R.color.grey),
                                textDecoration = if (canResend) TextDecoration.Underline else TextDecoration.None
                            ),
                            modifier = Modifier.clickable(enabled = canResend) { onResendClick() }
                        )
                    }

                    // Action Buttons matching layout_button.xml line 30
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(
                                top = dimensionResource(id = R.dimen.twenty_dp),
                                start = dimensionResource(id = R.dimen.ten_dp),
                                end = dimensionResource(id = R.dimen.ten_dp)
                            )
                    ) {
                        val cancelShape = RoundedCornerShape(dimensionResource(id = R.dimen.eight_dp))
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .height(dimensionResource(id = R.dimen.forty_dp))
                                .shadow(elevation = dimensionResource(id = R.dimen.four_dp), shape = cancelShape)
                                .background(color = colorResource(id = R.color.dark_grey), shape = cancelShape)
                                .clickable { onCancelClick() },
                            contentAlignment = Alignment.Center
                        ) {
                            androidx.compose.material3.Text(
                                text = "Cancel",
                                style = TextStyle(
                                    fontFamily = GillSans,
                                    fontWeight = FontWeight.W600,
                                    fontSize = 15.sp,
                                    color = colorResource(id = R.color.black)
                                )
                            )
                        }

                        AppSpacer(width = dimensionResource(id = R.dimen.ten_dp))

                        val submitShape = RoundedCornerShape(dimensionResource(id = R.dimen.eight_dp))
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .height(dimensionResource(id = R.dimen.forty_dp))
                                .shadow(elevation = dimensionResource(id = R.dimen.four_dp), shape = submitShape)
                                .background(color = colorResource(id = R.color.blue), shape = submitShape)
                                .clickable { onVerifyClick() },
                            contentAlignment = Alignment.Center
                        ) {
                            androidx.compose.material3.Text(
                                text = "Verify OTP",
                                style = TextStyle(
                                    fontFamily = GillSansBold,
                                    fontSize = 15.sp,
                                    color = colorResource(id = R.color.white)
                                )
                            )
                        }
                    }
                }
            }
        }

        if (isLoading) {
            AppLoader()
        }
    }
}

@Preview(showBackground = true, name = "OTP Verification Preview")
@Composable
fun OtpVerificationScreenPreview() {
    OtpVerificationScreen(
        otpValue = "123456",
        onOtpChange = {},
        timerText = "Resend OTP in 30s",
        canResend = false,
        onResendClick = {},
        onVerifyClick = {},
        onCancelClick = {},
        onRegisterHereClick = {}
    )
}
