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
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.withStyle
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
                        .padding(dimensionResource(id = R.dimen.twenty_dp)),
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

                    // Title Header matching login_title_layout.xml
                    val titleText = when {
                        !isLoginMode -> "Register With OTP"
                        isPasswordMode -> "Log in with Password"
                        else -> "Login With OTP"
                    }
                    androidx.compose.material3.Text(
                        text = titleText,
                        style = TextStyle(
                            fontFamily = GillSansBold,
                            fontSize = 20.sp,
                            color = colorResource(id = R.color.black)
                        ),
                        modifier = Modifier.padding(
                            top = dimensionResource(id = R.dimen.twenty_dp),
                            bottom = dimensionResource(id = R.dimen.twenty_dp),
                            start = dimensionResource(id = R.dimen.twenty_dp),
                            end = dimensionResource(id = R.dimen.twenty_dp)
                        )
                    )

                    if (isLoginMode) {
                        // Email / Mobile Input Container matching login.xml line 48
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
                            XmlBasicInput(
                                value = emailOrMobile,
                                onValueChange = onEmailOrMobileChange,
                                hint = "Email or Mobile Number",
                                keyboardType = KeyboardType.Email
                            )
                        }

                        // Multi-Firm Warning Prompt
                        if (firmList.isNotEmpty()) {
                            androidx.compose.material3.Text(
                                text = "This email is associated with multiple firms. Please select a firm to continue.",
                                style = TextStyle(
                                    fontFamily = GillSans,
                                    fontSize = 13.sp,
                                    color = colorResource(id = R.color.Red)
                                ),
                                modifier = Modifier.padding(
                                    start = dimensionResource(id = R.dimen.ten_dp),
                                    end = dimensionResource(id = R.dimen.ten_dp),
                                    bottom = 8.dp
                                )
                            )
                            AppDropdown(
                                options = firmList,
                                selectedOption = selectedFirm,
                                onOptionSelected = onFirmSelected,
                                label = "Select Firm",
                                modifier = Modifier.padding(horizontal = dimensionResource(id = R.dimen.ten_dp))
                            )
                            AppSpacer(height = dimensionResource(id = R.dimen.ten_dp))
                        }

                        // Password Section (Password Mode)
                        if (isPasswordMode) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(dimensionResource(id = R.dimen.ten_dp))
                                    .shadow(elevation = dimensionResource(id = R.dimen.four_dp), shape = inputShape)
                                    .background(color = colorResource(id = R.color.text_field_color), shape = inputShape)
                                    .border(width = 0.5.dp, color = colorResource(id = R.color.silver), shape = inputShape)
                                    .padding(horizontal = dimensionResource(id = R.dimen.Fifteen_dp), vertical = 4.dp)
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Box(modifier = Modifier.weight(1f)) {
                                        XmlBasicInput(
                                            value = passwordValue,
                                            onValueChange = onPasswordChange,
                                            hint = "Password",
                                            keyboardType = KeyboardType.Password,
                                            visualTransformation = if (isPasswordVisible) VisualTransformation.None else PasswordVisualTransformation()
                                        )
                                    }
                                    Image(
                                        painter = painterResource(
                                            id = if (isPasswordVisible) R.drawable.eye_open else R.drawable.eye_close
                                        ),
                                        contentDescription = "Toggle Password",
                                        colorFilter = androidx.compose.ui.graphics.ColorFilter.tint(colorResource(id = R.color.blue)),
                                        modifier = Modifier
                                            .padding(end = dimensionResource(id = R.dimen.five_dp))
                                            .height(dimensionResource(id = R.dimen.twenty_five))
                                            .clickable { isPasswordVisible = !isPasswordVisible }
                                    )
                                }
                            }

                            // Links: Left "Forgot Password ?", Right "Use OTP Instead" (Underlined per XML)
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(
                                        start = dimensionResource(id = R.dimen.ten_dp),
                                        end = dimensionResource(id = R.dimen.ten_dp),
                                        bottom = 4.dp
                                    )
                            ) {
                                androidx.compose.material3.Text(
                                    text = "Forgot Password ?",
                                    style = TextStyle(
                                        fontFamily = GillSans,
                                        fontSize = 13.sp,
                                        color = colorResource(id = R.color.black),
                                        textDecoration = TextDecoration.Underline
                                    ),
                                    modifier = Modifier
                                        .weight(1f)
                                        .clickable { onForgotPasswordClick() }
                                )
                                androidx.compose.material3.Text(
                                    text = "Use OTP Instead",
                                    style = TextStyle(
                                        fontFamily = GillSans,
                                        fontSize = 13.sp,
                                        color = colorResource(id = R.color.Primary_new),
                                        textDecoration = TextDecoration.Underline
                                    ),
                                    textAlign = TextAlign.End,
                                    modifier = Modifier.clickable { onTogglePasswordModeClick() }
                                )
                            }
                        } else {
                            // OTP Mode Link: Right "Use Password Instead" (Underlined per XML)
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(
                                        end = dimensionResource(id = R.dimen.ten_dp),
                                        bottom = 4.dp
                                    ),
                                contentAlignment = Alignment.CenterEnd
                            ) {
                                androidx.compose.material3.Text(
                                    text = "Use Password Instead",
                                    style = TextStyle(
                                        fontFamily = GillSans,
                                        fontSize = 13.sp,
                                        color = colorResource(id = R.color.Primary_new),
                                        textDecoration = TextDecoration.Underline
                                    ),
                                    modifier = Modifier.clickable { onTogglePasswordModeClick() }
                                )
                            }
                        }

                        // Biometric Section
                        if (isBiometricAvailable && biometricChecked) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(dimensionResource(id = R.dimen.ten_dp))
                            ) {
                                val bioCardShape = RoundedCornerShape(dimensionResource(id = R.dimen.eight_dp))
                                Card(
                                    shape = bioCardShape,
                                    colors = CardDefaults.cardColors(containerColor = colorResource(id = R.color.white)),
                                    elevation = CardDefaults.cardElevation(defaultElevation = dimensionResource(id = R.dimen.four_dp)),
                                    modifier = Modifier
                                        .width(25.dp)
                                        .height(25.dp)
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

                                androidx.compose.material3.Text(
                                    text = "Enable Biometric Authentication",
                                    style = TextStyle(
                                        fontFamily = GillSans,
                                        fontSize = 12.sp,
                                        color = colorResource(id = R.color.black)
                                    ),
                                    modifier = Modifier.padding(start = dimensionResource(id = R.dimen.ten_dp))
                                )
                            }
                        }
                    } else {
                        // Register Mode Section
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
                            XmlBasicInput(
                                value = fullNameValue,
                                onValueChange = onFullNameChange,
                                hint = "Full Name"
                            )
                        }

                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(dimensionResource(id = R.dimen.ten_dp))
                                .shadow(elevation = dimensionResource(id = R.dimen.four_dp), shape = inputShape)
                                .background(color = colorResource(id = R.color.text_field_color), shape = inputShape)
                                .border(width = 0.5.dp, color = colorResource(id = R.color.silver), shape = inputShape)
                                .padding(horizontal = dimensionResource(id = R.dimen.Fifteen_dp), vertical = 4.dp)
                        ) {
                            XmlBasicInput(
                                value = registerEmailValue,
                                onValueChange = onRegisterEmailChange,
                                hint = "Enter the Email",
                                keyboardType = KeyboardType.Email
                            )
                        }

                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(dimensionResource(id = R.dimen.ten_dp))
                                .shadow(elevation = dimensionResource(id = R.dimen.four_dp), shape = inputShape)
                                .background(color = colorResource(id = R.color.text_field_color), shape = inputShape)
                                .border(width = 0.5.dp, color = colorResource(id = R.color.silver), shape = inputShape)
                                .padding(horizontal = dimensionResource(id = R.dimen.Fifteen_dp), vertical = 4.dp)
                        ) {
                            XmlBasicInput(
                                value = registerPhoneValue,
                                onValueChange = onRegisterPhoneChange,
                                hint = "Enter the Mobile Number",
                                keyboardType = KeyboardType.Phone
                            )
                        }
                    }

                    AppSpacer(height = dimensionResource(id = R.dimen.ten_dp))

                    // Terms & Conditions with underlined T&Cs and Privacy Policy spans
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(
                                start = dimensionResource(id = R.dimen.ten_dp),
                                end = dimensionResource(id = R.dimen.ten_dp),
                                bottom = dimensionResource(id = R.dimen.ten_dp)
                            )
                            .padding(4.dp)
                    ) {
                        if (!isLoginMode) {
                            Checkbox(
                                checked = termsAccepted,
                                onCheckedChange = onTermsAcceptedChange,
                                colors = CheckboxDefaults.colors(
                                    checkedColor = colorResource(id = R.color.blue),
                                    uncheckedColor = colorResource(id = R.color.silver)
                                ),
                                modifier = Modifier.padding(end = dimensionResource(id = R.dimen.ten_dp))
                            )
                        }
                        val prefixText = if (isLoginMode) "By signing-in, you agree to our " else "By signing-up, you agree to our "
                        val annotatedTerms = buildAnnotatedString {
                            append(prefixText)
                            pushStringAnnotation(tag = "Terms", annotation = "terms")
                            withStyle(
                                style = SpanStyle(
                                    color = colorResource(id = R.color.Primary_new),
                                    textDecoration = TextDecoration.Underline
                                )
                            ) {
                                append("T&Cs")
                            }
                            pop()
                            append(" and ")
                            pushStringAnnotation(tag = "Privacy", annotation = "privacy")
                            withStyle(
                                style = SpanStyle(
                                    color = colorResource(id = R.color.Primary_new),
                                    textDecoration = TextDecoration.Underline
                                )
                            ) {
                                append("Privacy Policy")
                            }
                            pop()
                        }
                        androidx.compose.foundation.text.ClickableText(
                            text = annotatedTerms,
                            style = TextStyle(
                                fontFamily = GillSans,
                                fontSize = 13.sp,
                                color = colorResource(id = R.color.black)
                            ),
                            onClick = { offset ->
                                annotatedTerms.getStringAnnotations(tag = "Terms", start = offset, end = offset)
                                    .firstOrNull()?.let {
                                        onTermsClick()
                                    }
                                annotatedTerms.getStringAnnotations(tag = "Privacy", start = offset, end = offset)
                                    .firstOrNull()?.let {
                                        onPrivacyClick()
                                    }
                            }
                        )
                    }

                    AppSpacer(height = dimensionResource(id = R.dimen.twenty_dp))

                    // Centered Action Button matching submit_button.xml
                    val btnText = when {
                        !isLoginMode -> "Register"
                        isPasswordMode -> "Login"
                        else -> "Send OTP"
                    }
                    val btnShape = RoundedCornerShape(dimensionResource(id = R.dimen.eight_dp))
                    Box(
                        modifier = Modifier
                            .width(dimensionResource(id = R.dimen.module_btn_size) * 1.6f)
                            .height(dimensionResource(id = R.dimen.forty_dp))
                            .padding(top = dimensionResource(id = R.dimen.four_dp))
                            .shadow(elevation = dimensionResource(id = R.dimen.four_dp), shape = btnShape)
                            .background(color = colorResource(id = R.color.blue), shape = btnShape)
                            .clickable { onSubmitClick() },
                        contentAlignment = Alignment.Center
                    ) {
                        androidx.compose.material3.Text(
                            text = btnText,
                            style = TextStyle(
                                fontFamily = GillSansBold,
                                fontSize = 15.sp,
                                color = colorResource(id = R.color.white)
                            )
                        )
                    }

                    AppSpacer(height = dimensionResource(id = R.dimen.twenty_dp))

                    // Account Toggle Link (Underlined per XML)
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        androidx.compose.material3.Text(
                            text = if (isLoginMode) "Don't have an account? " else "Already have an account? ",
                            style = TextStyle(
                                fontFamily = GillSans,
                                fontSize = 15.sp,
                                color = colorResource(id = R.color.black)
                            )
                        )
                        androidx.compose.material3.Text(
                            text = if (isLoginMode) "Register Here" else "Log In",
                            style = TextStyle(
                                fontFamily = GillSans,
                                fontSize = 15.sp,
                                color = colorResource(id = R.color.Primary_new),
                                textDecoration = TextDecoration.Underline
                            ),
                            modifier = Modifier.clickable { onToggleModeClick() }
                        )
                    }

                    // Support Information Link (Login Mode Only, Underlined per XML)
                    if (isLoginMode) {
                        AppSpacer(height = dimensionResource(id = R.dimen.ten_dp))
                        Row(
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            androidx.compose.material3.Text(
                                text = "For more information ",
                                style = TextStyle(
                                    fontFamily = GillSans,
                                    fontSize = 15.sp,
                                    color = colorResource(id = R.color.black)
                                )
                            )
                            androidx.compose.material3.Text(
                                text = "Contact Support",
                                style = TextStyle(
                                    fontFamily = GillSans,
                                    fontSize = 15.sp,
                                    color = colorResource(id = R.color.Primary_new),
                                    textDecoration = TextDecoration.Underline
                                ),
                                modifier = Modifier
                                    .padding(start = dimensionResource(id = R.dimen.four_dp))
                                    .clickable { onContactSupportClick() }
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

@Composable
private fun XmlBasicInput(
    value: String,
    onValueChange: (String) -> Unit,
    hint: String,
    keyboardType: KeyboardType = KeyboardType.Text,
    visualTransformation: VisualTransformation = VisualTransformation.None
) {
    Box(
        contentAlignment = Alignment.CenterStart,
        modifier = Modifier
            .fillMaxWidth()
            .height(48.dp)
    ) {
        if (value.isEmpty()) {
            androidx.compose.material3.Text(
                text = hint,
                style = TextStyle(
                    fontFamily = GillSans,
                    fontSize = 15.sp,
                    color = colorResource(id = R.color.grey_color_dark)
                )
            )
        }
        BasicTextField(
            value = value,
            onValueChange = onValueChange,
            textStyle = TextStyle(
                fontFamily = GillSans,
                fontSize = 15.sp,
                color = colorResource(id = R.color.black)
            ),
            keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
            visualTransformation = visualTransformation,
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )
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
