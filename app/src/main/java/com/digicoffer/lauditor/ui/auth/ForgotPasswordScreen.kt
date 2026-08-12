package com.digicoffer.lauditor.ui.auth

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.digicoffer.lauditor.R
import com.digicoffer.lauditor.core.ui.feedback.AppLoader
import com.digicoffer.lauditor.core.ui.foundation.AppSpacer
import com.digicoffer.lauditor.core.ui.inputs.AppDropdown

private val GillSans = FontFamily(Font(R.font.gill_sans))
private val GillSansBold = FontFamily(Font(R.font.gill_sans_bold))

@Composable
fun ForgotPasswordScreen(
    emailValue: String,
    onEmailChange: (String) -> Unit,
    firmList: List<String>,
    selectedFirm: String?,
    onFirmSelected: (String) -> Unit,
    awaitingFirmSelection: Boolean,
    isLoading: Boolean,
    onSubmitClick: () -> Unit,
    onCancelClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val cleanEmail = emailValue.trim()
    val isSubmitEnabled = cleanEmail.isNotEmpty()

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(colorResource(id = R.color.blue_pale))
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .padding(dimensionResource(id = R.dimen.Fifteen_dp))
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Card(
                shape = RoundedCornerShape(dimensionResource(id = R.dimen.thirty_dp)),
                colors = CardDefaults.cardColors(containerColor = colorResource(id = R.color.white)),
                elevation = CardDefaults.cardElevation(defaultElevation = dimensionResource(id = R.dimen.twenty_dp)),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(dimensionResource(id = R.dimen.FiveHundred_dp))
                    .padding(dimensionResource(id = R.dimen.twenty_dp))
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(dimensionResource(id = R.dimen.ten_dp)),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Logo Image matching logo_layout.xml
                    Image(
                        painter = painterResource(id = R.drawable.logo_new),
                        contentDescription = "Lauditor Logo",
                        modifier = Modifier
                            .padding(top = dimensionResource(id = R.dimen.ten_dp))
                            .width(dimensionResource(id = R.dimen.ThreeHundred_dp))
                            .height(dimensionResource(id = R.dimen.Eighty_dp))
                    )

                    // Subtitle Content Text matching grey_normal_txt.xml & Screenshot 4
                    androidx.compose.material3.Text(
                        text = if (awaitingFirmSelection) {
                            "Multiple firms found. Please select the firm you want to reset the password for."
                        } else {
                            "Don't worry ! It happens. Please enter the email associated with your account"
                        },
                        style = TextStyle(
                            fontFamily = GillSans,
                            fontSize = 15.sp,
                            color = colorResource(id = R.color.grey_color_dark)
                        ),
                        modifier = Modifier.padding(
                            start = dimensionResource(id = R.dimen.Fifteen_dp) + dimensionResource(id = R.dimen.ten_dp),
                            end = dimensionResource(id = R.dimen.ten_dp),
                            top = dimensionResource(id = R.dimen.ten_dp)
                        )
                    )

                    // Title Header matching login_title_layout.xml
                    androidx.compose.material3.Text(
                        text = stringResource(id = R.string.forgot_password),
                        style = TextStyle(
                            fontFamily = GillSansBold,
                            fontSize = 20.sp,
                            color = colorResource(id = R.color.black)
                        ),
                        modifier = Modifier.padding(
                            top = dimensionResource(id = R.dimen.ten_dp),
                            bottom = dimensionResource(id = R.dimen.twenty_dp)
                        )
                    )

                    // Email Input Box matching activity_forgetpassword.xml line 58
                    val inputShape = RoundedCornerShape(dimensionResource(id = R.dimen.six_dp))
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(dimensionResource(id = R.dimen.ten_dp))
                            .shadow(elevation = dimensionResource(id = R.dimen.four_dp), shape = inputShape)
                            .background(color = colorResource(id = R.color.text_field_color), shape = inputShape)
                            .border(width = 0.5.dp, color = colorResource(id = R.color.silver), shape = inputShape)
                            .padding(horizontal = dimensionResource(id = R.dimen.Fifteen_dp), vertical = 4.dp)
                    ) {
                        Box(
                            contentAlignment = Alignment.CenterStart,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(48.dp)
                        ) {
                            if (emailValue.isEmpty()) {
                                androidx.compose.material3.Text(
                                    text = stringResource(id = R.string.email),
                                    style = TextStyle(
                                        fontFamily = GillSans,
                                        fontSize = 15.sp,
                                        color = colorResource(id = R.color.grey_color_dark)
                                    )
                                )
                            }
                            BasicTextField(
                                value = emailValue,
                                onValueChange = onEmailChange,
                                textStyle = TextStyle(
                                    fontFamily = GillSans,
                                    fontSize = 15.sp,
                                    color = colorResource(id = R.color.black)
                                ),
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                                singleLine = true,
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    }

                    // Multi-Firm Dropdown Layout
                    if (firmList.isNotEmpty()) {
                        AppDropdown(
                            options = firmList,
                            selectedOption = selectedFirm,
                            onOptionSelected = onFirmSelected,
                            label = "Select Firm",
                            modifier = Modifier.padding(horizontal = dimensionResource(id = R.dimen.ten_dp))
                        )
                        AppSpacer(height = dimensionResource(id = R.dimen.ten_dp))
                    }

                    // Action Buttons matching layout_button.xml line 30
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(
                                top = dimensionResource(id = R.dimen.Fifteen_dp),
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
                        val btnColor = if (isSubmitEnabled) colorResource(id = R.color.blue) else colorResource(id = R.color.dullBlueColor)
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .height(dimensionResource(id = R.dimen.forty_dp))
                                .shadow(elevation = dimensionResource(id = R.dimen.four_dp), shape = submitShape)
                                .background(color = btnColor, shape = submitShape)
                                .clickable(enabled = isSubmitEnabled) { onSubmitClick() },
                            contentAlignment = Alignment.Center
                        ) {
                            androidx.compose.material3.Text(
                                text = "Submit",
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

@Preview(showBackground = true, name = "Forgot Password Preview")
@Composable
fun ForgotPasswordScreenPreview() {
    ForgotPasswordScreen(
        emailValue = "user@lauditor.com",
        onEmailChange = {},
        firmList = emptyList(),
        selectedFirm = null,
        onFirmSelected = {},
        awaitingFirmSelection = false,
        isLoading = false,
        onSubmitClick = {},
        onCancelClick = {}
    )
}
