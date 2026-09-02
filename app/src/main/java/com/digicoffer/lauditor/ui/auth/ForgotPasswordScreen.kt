package com.digicoffer.lauditor.ui.auth

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.digicoffer.lauditor.R
import com.digicoffer.lauditor.core.ui.common.buttons.AppButton
import com.digicoffer.lauditor.core.ui.common.buttons.ButtonVariant
import com.digicoffer.lauditor.core.ui.common.dropdowns.AppDropdown
import com.digicoffer.lauditor.core.ui.common.feedback.AppLoader
import com.digicoffer.lauditor.core.ui.common.foundation.AppSpacer
import com.digicoffer.lauditor.core.ui.common.inputs.AppTextField

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
    val focusManager = LocalFocusManager.current
    val keyboardController = LocalSoftwareKeyboardController.current

    val inputShape = RoundedCornerShape(6.dp)
    val inputBorder = BorderStroke(0.5.dp, colorResource(id = R.color.silver))
    val inputBg = colorResource(id = R.color.text_field_color)
    val inputHeight = 42.dp
    val inputPadding = PaddingValues(horizontal = 12.dp, vertical = 2.dp)
    val inputTextStyle = TextStyle(
        fontFamily = GillSans,
        fontSize = 14.sp,
        color = colorResource(id = R.color.black)
    )

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(colorResource(id = R.color.blue_pale))
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .imePadding()
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
                        painter = painterResource(id = R.drawable.lex_z_lawyer_horizontal_tp),
                        contentDescription = "Lex-Z Lawyers Logo",
                        modifier = Modifier
                            .padding(top = dimensionResource(id = R.dimen.ten_dp))
                            .width(dimensionResource(id = R.dimen.ThreeHundred_dp))
                            .height(dimensionResource(id = R.dimen.Eighty_dp))
                    )

                    // Subtitle Content Text
                    Text(
                        text = if (awaitingFirmSelection) {
                            "Multiple firms found. Please select the firm you want to reset the password for."
                        } else {
                            "Don't worry ! It happens. Please enter the email associated with your account"
                        },
                        style = TextStyle(
                            fontFamily = GillSans,
                            fontSize = 14.sp,
                            color = colorResource(id = R.color.grey_color_dark)
                        ),
                        modifier = Modifier.padding(
                            start = 12.dp,
                            end = 12.dp,
                            top = dimensionResource(id = R.dimen.ten_dp)
                        )
                    )

                    // Title Header
                    Text(
                        text = stringResource(id = R.string.forgot_password),
                        style = TextStyle(
                            fontFamily = GillSansBold,
                            fontSize = 20.sp,
                            color = colorResource(id = R.color.black)
                        ),
                        modifier = Modifier.padding(
                            top = dimensionResource(id = R.dimen.ten_dp),
                            bottom = 12.dp
                        )
                    )

                    // Email Input Box using common AppTextField
                    AppTextField(
                        value = emailValue,
                        onValueChange = onEmailChange,
                        placeholder = stringResource(id = R.string.email),
                        shape = inputShape,
                        border = inputBorder,
                        backgroundColor = inputBg,
                        height = inputHeight,
                        contentPadding = inputPadding,
                        textStyle = inputTextStyle,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                        singleLine = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 4.dp, vertical = 4.dp)
                    )

                    // Firm selection dropdown if multiple firms
                    if (awaitingFirmSelection && firmList.isNotEmpty()) {
                        AppSpacer(height = 6.dp)
                        AppDropdown(
                            options = firmList,
                            selectedOption = selectedFirm,
                            onOptionSelected = onFirmSelected,
                            label = "Select Firm",
                            modifier = Modifier.padding(horizontal = 4.dp)
                        )
                        AppSpacer(height = 6.dp)
                    }

                    AppSpacer(height = 14.dp)

                    // Action Buttons using common AppButton
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 4.dp),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        AppButton(
                            text = stringResource(id = R.string.cancel),
                            onClick = {
                                focusManager.clearFocus(force = true)
                                keyboardController?.hide()
                                onCancelClick()
                            },
                            modifier = Modifier
                                .width(dimensionResource(id = R.dimen.module_btn_size))
                                .height(dimensionResource(id = R.dimen.forty_dp)),
                            shape = RoundedCornerShape(dimensionResource(id = R.dimen.eight_dp)),
                            containerColor = colorResource(id = R.color.dark_grey),
                            contentColor = colorResource(id = R.color.black),
                            textStyle = TextStyle(
                                fontFamily = GillSansBold,
                                fontSize = 13.sp,
                                color = colorResource(id = R.color.black)
                            )
                        )

                        AppSpacer(width = dimensionResource(id = R.dimen.ten_dp))

                        AppButton(
                            text = stringResource(id = R.string.submit),
                            onClick = {
                                focusManager.clearFocus(force = true)
                                keyboardController?.hide()
                                onSubmitClick()
                            },
                            modifier = Modifier
                                .width(dimensionResource(id = R.dimen.module_btn_size))
                                .height(dimensionResource(id = R.dimen.forty_dp)),
                            shape = RoundedCornerShape(dimensionResource(id = R.dimen.eight_dp)),
                            containerColor = colorResource(id = R.color.blue),
                            contentColor = colorResource(id = R.color.white),
                            textStyle = TextStyle(
                                fontFamily = GillSansBold,
                                fontSize = 13.sp,
                                color = colorResource(id = R.color.white)
                            )
                        )
                    }

                    AppSpacer(height = dimensionResource(id = R.dimen.ten_dp))
                }
            }
        }

        if (isLoading) {
            AppLoader()
        }
    }
}

@Preview(showBackground = true)
@Composable
fun ForgotPasswordScreenPreview() {
    ForgotPasswordScreen(
        emailValue = "user@example.com",
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
