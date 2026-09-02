package com.digicoffer.lauditor.ui.auth

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
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
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.digicoffer.lauditor.R
import com.digicoffer.lauditor.core.ui.common.buttons.AppButton
import com.digicoffer.lauditor.core.ui.common.dropdowns.AppDropdown
import com.digicoffer.lauditor.core.ui.common.feedback.AppLoader
import com.digicoffer.lauditor.core.ui.common.foundation.AppSpacer
import com.digicoffer.lauditor.core.ui.common.inputs.AppTextField

private val GillSans = FontFamily(Font(R.font.gill_sans))
private val GillSansBold = FontFamily(Font(R.font.gill_sans_bold))

@Composable
fun LoginScreen(
    emailOrMobile: String,
    onEmailOrMobileChange: (String) -> Unit,
    passwordValue: String,
    onPasswordChange: (String) -> Unit,
    fullNameValue: String,
    onFullNameChange: (String) -> Unit,
    registerEmailValue: String,
    onRegisterEmailChange: (String) -> Unit,
    registerPhoneValue: String,
    onRegisterPhoneChange: (String) -> Unit,
    isLoginMode: Boolean,
    isPasswordMode: Boolean,
    onTogglePasswordModeClick: () -> Unit = {},
    onToggleModeClick: () -> Unit = {},
    firmList: List<String> = emptyList(),
    selectedFirm: String? = null,
    onFirmSelected: (String) -> Unit = {},
    isBiometricAvailable: Boolean = false,
    biometricChecked: Boolean = false,
    onBiometricCheckedChange: (Boolean) -> Unit = {},
    termsAccepted: Boolean = false,
    onTermsAcceptedChange: (Boolean) -> Unit = {},
    isLoading: Boolean = false,
    onSubmitClick: () -> Unit = {},
    onForgotPasswordClick: () -> Unit = {},
    onContactSupportClick: () -> Unit = {},
    onTermsClick: () -> Unit = {},
    onPrivacyClick: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    var isPasswordVisible by remember { mutableStateOf(false) }
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

                    // Title Header matching login_title_layout.xml
                    val titleText = when {
                        !isLoginMode -> "Register With OTP"
                        isPasswordMode -> "Log in with Password"
                        else -> "Login With OTP"
                    }
                    Text(
                        text = titleText,
                        style = TextStyle(
                            fontFamily = GillSansBold,
                            fontSize = 20.sp,
                            color = colorResource(id = R.color.black)
                        ),
                        modifier = Modifier.padding(
                            top = dimensionResource(id = R.dimen.twenty_dp),
                            bottom = dimensionResource(id = R.dimen.ten_dp),
                            start = dimensionResource(id = R.dimen.twenty_dp),
                            end = dimensionResource(id = R.dimen.twenty_dp)
                        )
                    )

                    if (isLoginMode) {
                        // Email / Mobile Input using common AppTextField
                        AppTextField(
                            value = emailOrMobile,
                            onValueChange = onEmailOrMobileChange,
                            placeholder = "Email or Mobile Number",
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
                                .padding(vertical = 4.dp, horizontal = 4.dp)
                        )

                        // Multi-Firm Warning Prompt
                        if (firmList.isNotEmpty()) {
                            AppSpacer(height = 6.dp)
                            Text(
                                text = "This email is associated with multiple firms. Please select a firm to continue.",
                                style = TextStyle(
                                    fontFamily = GillSans,
                                    fontSize = 13.sp,
                                    color = colorResource(id = R.color.Red)
                                ),
                                modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                            )
                            AppDropdown(
                                options = firmList,
                                selectedOption = selectedFirm,
                                onOptionSelected = onFirmSelected,
                                label = "Select Firm",
                                modifier = Modifier.padding(horizontal = 4.dp)
                            )
                            AppSpacer(height = dimensionResource(id = R.dimen.ten_dp))
                        }

                        // Password Section (Password Mode)
                        if (isPasswordMode) {
                            AppSpacer(height = 4.dp)
                            AppTextField(
                                value = passwordValue,
                                onValueChange = onPasswordChange,
                                placeholder = "Password",
                                shape = inputShape,
                                border = inputBorder,
                                backgroundColor = inputBg,
                                height = inputHeight,
                                contentPadding = inputPadding,
                                textStyle = inputTextStyle,
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                                visualTransformation = if (isPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                                singleLine = true,
                                trailingIcon = {
                                    Image(
                                        painter = painterResource(
                                            id = if (isPasswordVisible) R.drawable.eye_open else R.drawable.eye_close
                                        ),
                                        contentDescription = "Toggle Password",
                                        colorFilter = ColorFilter.tint(colorResource(id = R.color.blue)),
                                        modifier = Modifier
                                            .padding(end = 8.dp)
                                            .height(20.dp)
                                            .clickable { isPasswordVisible = !isPasswordVisible }
                                    )
                                },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp, horizontal = 4.dp)
                            )

                            AppSpacer(height = 4.dp)

                            // Links: Left "Forgot Password ?", Right "Use OTP Instead"
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 4.dp, vertical = 4.dp)
                            ) {
                                Text(
                                    text = "Forgot Password ?",
                                    style = TextStyle(
                                        fontFamily = GillSans,
                                        fontSize = 13.sp,
                                        color = colorResource(id = R.color.black),
                                        textDecoration = TextDecoration.Underline
                                    ),
                                    modifier = Modifier
                                        .weight(1f)
                                        .clickable {
                                            focusManager.clearFocus(force = true)
                                            keyboardController?.hide()
                                            onForgotPasswordClick()
                                        }
                                )
                                Text(
                                    text = "Use OTP Instead",
                                    style = TextStyle(
                                        fontFamily = GillSans,
                                        fontSize = 13.sp,
                                        color = colorResource(id = R.color.Primary_new),
                                        textDecoration = TextDecoration.Underline
                                    ),
                                    textAlign = TextAlign.End,
                                    modifier = Modifier.clickable {
                                        focusManager.clearFocus(force = true)
                                        keyboardController?.hide()
                                        onTogglePasswordModeClick()
                                    }
                                )
                            }
                        } else {
                            // OTP Mode Link: Right "Use Password Instead"
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 4.dp, vertical = 4.dp),
                                contentAlignment = Alignment.CenterEnd
                            ) {
                                Text(
                                    text = "Use Password Instead",
                                    style = TextStyle(
                                        fontFamily = GillSans,
                                        fontSize = 13.sp,
                                        color = colorResource(id = R.color.Primary_new),
                                        textDecoration = TextDecoration.Underline
                                    ),
                                    modifier = Modifier.clickable {
                                        focusManager.clearFocus(force = true)
                                        keyboardController?.hide()
                                        onTogglePasswordModeClick()
                                    }
                                )
                            }
                        }

                        // Biometric Section
                        if (isBiometricAvailable && biometricChecked) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 4.dp, vertical = 6.dp)
                            ) {
                                val bioCardShape = RoundedCornerShape(dimensionResource(id = R.dimen.eight_dp))
                                Card(
                                    shape = bioCardShape,
                                    colors = CardDefaults.cardColors(containerColor = colorResource(id = R.color.white)),
                                    elevation = CardDefaults.cardElevation(defaultElevation = dimensionResource(id = R.dimen.four_dp)),
                                    modifier = Modifier
                                        .width(24.dp)
                                        .height(24.dp)
                                ) {
                                    Box(
                                        contentAlignment = Alignment.Center,
                                        modifier = Modifier.fillMaxSize()
                                    ) {
                                        Checkbox(
                                            checked = biometricChecked,
                                            onCheckedChange = onBiometricCheckedChange,
                                            colors = CheckboxDefaults.colors(
                                                checkedColor = colorResource(id = R.color.blue),
                                                uncheckedColor = colorResource(id = R.color.silver)
                                            )
                                        )
                                    }
                                }

                                Text(
                                    text = "Enable Biometric Authentication",
                                    style = TextStyle(
                                        fontFamily = GillSans,
                                        fontSize = 12.sp,
                                        color = colorResource(id = R.color.black)
                                    ),
                                    modifier = Modifier.padding(start = 8.dp)
                                )
                            }
                        }
                    } else {
                        // Register Mode Section using common AppTextField
                        AppTextField(
                            value = fullNameValue,
                            onValueChange = onFullNameChange,
                            placeholder = "Full Name",
                            shape = inputShape,
                            border = inputBorder,
                            backgroundColor = inputBg,
                            height = inputHeight,
                            contentPadding = inputPadding,
                            textStyle = inputTextStyle,
                            singleLine = true,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp, horizontal = 4.dp)
                        )

                        AppSpacer(height = 4.dp)

                        AppTextField(
                            value = registerEmailValue,
                            onValueChange = onRegisterEmailChange,
                            placeholder = "Enter the Email",
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
                                .padding(vertical = 4.dp, horizontal = 4.dp)
                        )

                        AppSpacer(height = 4.dp)

                        AppTextField(
                            value = registerPhoneValue,
                            onValueChange = onRegisterPhoneChange,
                            placeholder = "Enter the Mobile Number",
                            shape = inputShape,
                            border = inputBorder,
                            backgroundColor = inputBg,
                            height = inputHeight,
                            contentPadding = inputPadding,
                            textStyle = inputTextStyle,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                            singleLine = true,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp, horizontal = 4.dp)
                        )
                    }

                    AppSpacer(height = 14.dp)

                    // Action Button using common AppButton with exact XML sizing
                    val btnText = when {
                        !isLoginMode -> "Register"
                        isPasswordMode -> "Login"
                        else -> "Send OTP"
                    }
                    AppButton(
                        text = btnText,
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

                    AppSpacer(height = 14.dp)

                    // Account Toggle Link
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = if (isLoginMode) "Don't have an account? " else "Already have an account? ",
                            style = TextStyle(
                                fontFamily = GillSans,
                                fontSize = 14.sp,
                                color = colorResource(id = R.color.black)
                            )
                        )
                        Text(
                            text = if (isLoginMode) "Register Here" else "Log In",
                            style = TextStyle(
                                fontFamily = GillSans,
                                fontSize = 14.sp,
                                color = colorResource(id = R.color.Primary_new),
                                textDecoration = TextDecoration.Underline
                            ),
                            modifier = Modifier.clickable {
                                focusManager.clearFocus(force = true)
                                keyboardController?.hide()
                                onToggleModeClick()
                            }
                        )
                    }

                    // Support Information Link (Login Mode Only)
                    if (isLoginMode) {
                        AppSpacer(height = 8.dp)
                        Row(
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "For more information ",
                                style = TextStyle(
                                    fontFamily = GillSans,
                                    fontSize = 14.sp,
                                    color = colorResource(id = R.color.black)
                                )
                            )
                            Text(
                                text = "Contact Support",
                                style = TextStyle(
                                    fontFamily = GillSans,
                                    fontSize = 14.sp,
                                    color = colorResource(id = R.color.Primary_new),
                                    textDecoration = TextDecoration.Underline
                                ),
                                modifier = Modifier
                                    .padding(start = 4.dp)
                                    .clickable {
                                        focusManager.clearFocus(force = true)
                                        keyboardController?.hide()
                                        onContactSupportClick()
                                    }
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

@Preview(showBackground = true, name = "Login Light Preview")
@Composable
fun LoginScreenLightPreview() {
    LoginScreen(
        emailOrMobile = "",
        onEmailOrMobileChange = {},
        passwordValue = "",
        onPasswordChange = {},
        fullNameValue = "",
        onFullNameChange = {},
        registerEmailValue = "",
        onRegisterEmailChange = {},
        registerPhoneValue = "",
        onRegisterPhoneChange = {},
        isLoginMode = true,
        isPasswordMode = true
    )
}
