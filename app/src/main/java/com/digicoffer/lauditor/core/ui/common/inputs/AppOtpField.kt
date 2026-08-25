package com.digicoffer.lauditor.core.ui.common.inputs

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.digicoffer.lauditor.R

private val GillSansBold = FontFamily(Font(R.font.gill_sans_bold))

@Composable
fun AppOtpField(
    otpValue: String,
    onOtpChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    length: Int = 6,
    enabled: Boolean = true
) {
    BasicTextField(
        value = otpValue,
        onValueChange = { input ->
            val digitsOnly = input.filter { it.isDigit() }
            if (digitsOnly.length <= length) {
                onOtpChange(digitsOnly)
            }
        },
        enabled = enabled,
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
        decorationBox = {
            Row(
                horizontalArrangement = Arrangement.spacedBy(
                    dimensionResource(id = R.dimen.four_dp),
                    Alignment.CenterHorizontally
                ),
                modifier = Modifier.fillMaxWidth()
            ) {
                val boxShape = RoundedCornerShape(dimensionResource(id = R.dimen.eight_dp))
                for (i in 0 until length) {
                    val char = otpValue.getOrNull(i)?.toString() ?: ""
                    val isFocused = otpValue.length == i || (otpValue.length == length && i == length - 1)

                    Box(
                        modifier = Modifier
                            .width(44.dp)
                            .height(dimensionResource(id = R.dimen.Fifty_dp))
                            .background(color = colorResource(id = R.color.white), shape = boxShape)
                            .border(
                                width = if (isFocused) dimensionResource(id = R.dimen.two_dp) else dimensionResource(id = R.dimen.one_dp),
                                color = if (isFocused) colorResource(id = R.color.Primary_new) else colorResource(id = R.color.silver),
                                shape = boxShape
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        androidx.compose.material3.Text(
                            text = char,
                            style = TextStyle(
                                fontFamily = GillSansBold,
                                fontSize = 20.sp,
                                color = colorResource(id = R.color.black),
                                textAlign = TextAlign.Center
                            )
                        )
                    }
                }
            }
        },
        modifier = modifier
    )
}

@Preview(showBackground = true, name = "AppOtpField Preview")
@Composable
fun AppOtpFieldPreview() {
    AppOtpField(
        otpValue = "123456",
        onOtpChange = {}
    )
}
