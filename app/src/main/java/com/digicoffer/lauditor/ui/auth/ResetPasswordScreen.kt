package com.digicoffer.lauditor.ui.auth

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ColorFilter
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
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.digicoffer.lauditor.R
import com.digicoffer.lauditor.core.ui.common.buttons.AppButton
import com.digicoffer.lauditor.core.ui.common.buttons.ButtonVariant
import com.digicoffer.lauditor.core.ui.common.feedback.AppLoader
import com.digicoffer.lauditor.core.ui.common.feedback.AppPasswordRequirements
import com.digicoffer.lauditor.core.ui.common.feedback.PasswordRequirementItem
import com.digicoffer.lauditor.core.ui.common.foundation.AppSpacer
import com.digicoffer.lauditor.core.ui.common.inputs.AppTextField

private val GillSans = FontFamily(Font(R.font.gill_sans))
private val GillSansBold = FontFamily(Font(R.font.gill_sans_bold))

@Composable
fun ResetPasswordScreen(
    password1Value: String,
    onPassword1Change: (String) -> Unit,
    password2Value: String,
    onPassword2Change: (String) -> Unit,
    requirements: List<PasswordRequirementItem>,
    isSubmitEnabled: Boolean,
    onSubmitClick: () -> Unit,
    onCancelClick: () -> Unit,
    isLoading: Boolean = false,
    modifier: Modifier = Modifier
) {
    var isP1Visible by remember { mutableStateOf(false) }
    var isP2Visible by remember { mutableStateOf(false) }
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
                        .padding(dimensionResource(id = R.dimen.twenty_dp)),
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

                    // Title Header
                    Text(
                        text = stringResource(id = R.string.reset_password),
                        style = TextStyle(
                            fontFamily = GillSansBold,
                            fontSize = 20.sp,
                            color = colorResource(id = R.color.black)
                        ),
                        modifier = Modifier.padding(
                            top = dimensionResource(id = R.dimen.twenty_dp),
                            bottom = dimensionResource(id = R.dimen.ten_dp)
                        )
                    )

                    // Warning Text: "Please reset your password!"
                    Text(
                        text = "Please reset your password!",
                        style = TextStyle(
                            fontFamily = GillSansBold,
                            fontSize = 15.sp,
                            color = colorResource(id = R.color.orange_color)
                        ),
                        modifier = Modifier.padding(bottom = dimensionResource(id = R.dimen.ten_dp))
                    )

                    // Password Field 1 using AppTextField
                    AppTextField(
                        value = password1Value,
                        onValueChange = onPassword1Change,
                        placeholder = stringResource(id = R.string.password),
                        shape = inputShape,
                        border = inputBorder,
                        backgroundColor = inputBg,
                        height = inputHeight,
                        contentPadding = inputPadding,
                        textStyle = inputTextStyle,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                        visualTransformation = if (isP1Visible) VisualTransformation.None else PasswordVisualTransformation(),
                        singleLine = true,
                        trailingIcon = {
                            Image(
                                painter = painterResource(
                                    id = if (isP1Visible) R.drawable.eye_open else R.drawable.eye_close
                                ),
                                contentDescription = "Toggle Password Visibility",
                                colorFilter = ColorFilter.tint(colorResource(id = R.color.blue)),
                                modifier = Modifier
                                    .padding(end = 8.dp)
                                    .height(20.dp)
                                    .clickable { isP1Visible = !isP1Visible }
                            )
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp, horizontal = 4.dp)
                    )

                    AppSpacer(height = 4.dp)

                    // Password Field 2 using AppTextField
                    AppTextField(
                        value = password2Value,
                        onValueChange = onPassword2Change,
                        placeholder = "Re-enter Password",
                        shape = inputShape,
                        border = inputBorder,
                        backgroundColor = inputBg,
                        height = inputHeight,
                        contentPadding = inputPadding,
                        textStyle = inputTextStyle,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                        visualTransformation = if (isP2Visible) VisualTransformation.None else PasswordVisualTransformation(),
                        singleLine = true,
                        trailingIcon = {
                            Image(
                                painter = painterResource(
                                    id = if (isP2Visible) R.drawable.eye_open else R.drawable.eye_close
                                ),
                                contentDescription = "Toggle Password Visibility",
                                colorFilter = ColorFilter.tint(colorResource(id = R.color.blue)),
                                modifier = Modifier
                                    .padding(end = 8.dp)
                                    .height(20.dp)
                                    .clickable { isP2Visible = !isP2Visible }
                            )
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp, horizontal = 4.dp)
                    )

                    AppSpacer(height = 10.dp)

                    // Password Requirements checklist
                    AppPasswordRequirements(
                        requirements = requirements,
                        modifier = Modifier.padding(horizontal = 4.dp)
                    )

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
                            enabled = isSubmitEnabled,
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
fun ResetPasswordScreenPreview() {
    ResetPasswordScreen(
        password1Value = "",
        onPassword1Change = {},
        password2Value = "",
        onPassword2Change = {},
        requirements = emptyList(),
        isSubmitEnabled = true,
        onSubmitClick = {},
        onCancelClick = {}
    )
}
