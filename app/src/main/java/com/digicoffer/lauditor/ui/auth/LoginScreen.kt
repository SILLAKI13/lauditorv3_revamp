package com.digicoffer.lauditor.ui.auth

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
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
    firmNameValue: String = "",
    onFirmNameChange: (String) -> Unit = {},
    registerEmailValue: String,
    onRegisterEmailChange: (String) -> Unit,
    registerPhoneValue: String,
    onRegisterPhoneChange: (String) -> Unit,
    licensesValue: Int = 1,
    onLicensesChange: (Int) -> Unit = {},
    selectedCountry: String = "India",
    selectedCountryCode: String = "+91",
    onCountrySelected: (String, String) -> Unit = { _, _ -> },
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
    onContactSalesClick: () -> Unit = {},
    onTermsClick: () -> Unit = {},
    onPrivacyClick: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    var isPasswordVisible by remember { mutableStateOf(false) }
    var showContactSalesNotice by remember(isLoginMode) { mutableStateOf(false) }
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
                        !isLoginMode -> "Create your account"
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
                            bottom = if (!isLoginMode) 2.dp else dimensionResource(id = R.dimen.ten_dp),
                            start = dimensionResource(id = R.dimen.twenty_dp),
                            end = dimensionResource(id = R.dimen.twenty_dp)
                        )
                    )

                    if (!isLoginMode) {
                        Text(
                            text = "Fill in the details to sign up and grow your business.",
                            style = TextStyle(
                                fontFamily = GillSans,
                                fontSize = 12.sp,
                                color = Color(0xFF6B7280),
                                textAlign = TextAlign.Center
                            ),
                            modifier = Modifier.padding(bottom = 8.dp)
                        )

                        // Start your 30-days free trial LITE banner
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp, horizontal = 4.dp)
                                .background(
                                    color = Color(0xFFF0F7FF),
                                    shape = RoundedCornerShape(8.dp)
                                )
                                .border(
                                    border = BorderStroke(1.dp, Color(0xFFB8D5F2)),
                                    shape = RoundedCornerShape(8.dp)
                                )
                                .padding(horizontal = 12.dp, vertical = 10.dp)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(36.dp)
                                        .background(Color(0xFFE8F0FE), RoundedCornerShape(6.dp)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Image(
                                        painter = painterResource(id = R.drawable.ic_calendar_blue),
                                        contentDescription = "Trial Calendar",
                                        modifier = Modifier.size(20.dp)
                                    )
                                }

                                AppSpacer(width = 10.dp)

                                Column(
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = "Start your 30-days free trial",
                                            style = TextStyle(
                                                fontFamily = GillSansBold,
                                                fontSize = 13.sp,
                                                color = Color(0xFF0C2340)
                                            )
                                        )
                                        AppSpacer(width = 6.dp)
                                        Box(
                                            modifier = Modifier
                                                .background(Color(0xFFE0EEFA), RoundedCornerShape(4.dp))
                                                .padding(horizontal = 6.dp, vertical = 2.dp)
                                        ) {
                                            Text(
                                                text = "LITE",
                                                style = TextStyle(
                                                    fontFamily = GillSansBold,
                                                    fontSize = 10.sp,
                                                    color = Color(0xFF004D87)
                                                )
                                            )
                                        }
                                    }
                                    AppSpacer(height = 2.dp)
                                    Text(
                                        text = "Full access to every feature. No Payment required.",
                                        style = TextStyle(
                                            fontFamily = GillSans,
                                            fontSize = 11.sp,
                                            color = Color(0xFF6B7280)
                                        )
                                    )
                                }
                            }
                        }

                        AppSpacer(height = 6.dp)
                    }

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
                        // Register Mode Section
                        // Name and Firm Name Row (matching Web design)
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp, horizontal = 4.dp),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            AppTextField(
                                value = fullNameValue,
                                onValueChange = onFullNameChange,
                                placeholder = "Name",
                                shape = inputShape,
                                border = inputBorder,
                                backgroundColor = inputBg,
                                height = inputHeight,
                                contentPadding = inputPadding,
                                textStyle = inputTextStyle,
                                singleLine = true,
                                modifier = Modifier.weight(1f)
                            )

                            AppTextField(
                                value = firmNameValue,
                                onValueChange = onFirmNameChange,
                                placeholder = "Firm Name",
                                shape = inputShape,
                                border = inputBorder,
                                backgroundColor = inputBg,
                                height = inputHeight,
                                contentPadding = inputPadding,
                                textStyle = inputTextStyle,
                                singleLine = true,
                                modifier = Modifier.weight(1f)
                            )
                        }

                        AppSpacer(height = 4.dp)

                        // Country Dropdown
                        var isCountryDropdownExpanded by remember { mutableStateOf(false) }

                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp, horizontal = 4.dp)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(inputHeight)
                                    .background(inputBg, inputShape)
                                    .border(inputBorder, inputShape)
                                    .clickable {
                                        focusManager.clearFocus(force = true)
                                        keyboardController?.hide()
                                        isCountryDropdownExpanded = !isCountryDropdownExpanded
                                    }
                                    .padding(horizontal = 12.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                val displayCountry = if (selectedCountry.contains("United States", ignoreCase = true) || selectedCountry.equals("US", ignoreCase = true)) {
                                    "🇺🇸  United States"
                                } else {
                                    "🇮🇳  India"
                                }
                                Text(
                                    text = displayCountry,
                                    style = inputTextStyle
                                )
                                Icon(
                                    imageVector = Icons.Default.ArrowDropDown,
                                    contentDescription = "Select Country",
                                    tint = colorResource(id = R.color.blue)
                                )
                            }

                            DropdownMenu(
                                expanded = isCountryDropdownExpanded,
                                onDismissRequest = { isCountryDropdownExpanded = false },
                                modifier = Modifier
                                    .background(colorResource(id = R.color.white))
                                    .fillMaxWidth(0.85f)
                            ) {
                                DropdownMenuItem(
                                    text = {
                                        Text(
                                            text = "🇮🇳  India",
                                            style = inputTextStyle
                                        )
                                    },
                                    onClick = {
                                        onCountrySelected("India", "+91")
                                        isCountryDropdownExpanded = false
                                    }
                                )
                                DropdownMenuItem(
                                    text = {
                                        Text(
                                            text = "🇺🇸  United States",
                                            style = inputTextStyle
                                        )
                                    },
                                    onClick = {
                                        onCountrySelected("United States", "+1")
                                        isCountryDropdownExpanded = false
                                    }
                                )
                            }
                        }

                        AppSpacer(height = 4.dp)

                        AppTextField(
                            value = registerEmailValue,
                            onValueChange = onRegisterEmailChange,
                            placeholder = "Email Address",
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

                        // Mobile Number with Country Code prefix
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp, horizontal = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .width(54.dp)
                                    .height(inputHeight)
                                    .background(inputBg, inputShape)
                                    .border(inputBorder, inputShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = selectedCountryCode,
                                    style = TextStyle(
                                        fontFamily = GillSansBold,
                                        fontSize = 14.sp,
                                        color = colorResource(id = R.color.black)
                                    )
                                )
                            }

                            AppSpacer(width = 6.dp)

                            AppTextField(
                                value = registerPhoneValue,
                                onValueChange = onRegisterPhoneChange,
                                placeholder = "Mobile Number",
                                shape = inputShape,
                                border = inputBorder,
                                backgroundColor = inputBg,
                                height = inputHeight,
                                contentPadding = inputPadding,
                                textStyle = inputTextStyle,
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                                singleLine = true,
                                modifier = Modifier.weight(1f)
                            )
                        }

                        AppSpacer(height = 4.dp)

                        // Licenses Stepper
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp, horizontal = 4.dp)
                                .height(inputHeight)
                                .background(inputBg, inputShape)
                                .border(inputBorder, inputShape)
                                .padding(horizontal = 12.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxSize(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = "Licenses",
                                    style = inputTextStyle
                                )

                                Row(
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    // Decrement Button [-]
                                    Box(
                                        modifier = Modifier
                                            .size(28.dp)
                                            .background(
                                                color = if (licensesValue > 1) colorResource(id = R.color.blue) else Color(0xFFDCE6F1),
                                                shape = RoundedCornerShape(4.dp)
                                            )
                                            .clickable(enabled = licensesValue > 1) {
                                                focusManager.clearFocus(force = true)
                                                keyboardController?.hide()
                                                showContactSalesNotice = false
                                                onLicensesChange(licensesValue - 1)
                                            },
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = "−",
                                            style = TextStyle(
                                                fontFamily = GillSansBold,
                                                fontSize = 16.sp,
                                                color = if (licensesValue > 1) Color.White else Color(0xFF8C9BAE),
                                                textAlign = TextAlign.Center
                                            )
                                        )
                                    }

                                    Text(
                                        text = licensesValue.toString(),
                                        style = TextStyle(
                                            fontFamily = GillSansBold,
                                            fontSize = 14.sp,
                                            color = colorResource(id = R.color.black),
                                            textAlign = TextAlign.Center
                                        ),
                                        modifier = Modifier.padding(horizontal = 16.dp)
                                    )

                                    // Increment Button [+]
                                    Box(
                                        modifier = Modifier
                                            .size(28.dp)
                                            .background(
                                                color = colorResource(id = R.color.blue),
                                                shape = RoundedCornerShape(4.dp)
                                            )
                                            .clickable {
                                                focusManager.clearFocus(force = true)
                                                keyboardController?.hide()
                                                if (licensesValue < 10) {
                                                    onLicensesChange(licensesValue + 1)
                                                } else {
                                                    showContactSalesNotice = true
                                                }
                                            },
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = "+",
                                            style = TextStyle(
                                                fontFamily = GillSansBold,
                                                fontSize = 16.sp,
                                                color = Color.White,
                                                textAlign = TextAlign.Center
                                            )
                                        )
                                    }
                                }
                            }
                        }

                        // Need more than 10 licenses? Info Box (shown only when user clicks add after 10)
                        if (showContactSalesNotice && licensesValue >= 10) {
                            AppSpacer(height = 6.dp)
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp, horizontal = 4.dp)
                                    .background(
                                        color = Color(0xFFF0F7FF),
                                        shape = RoundedCornerShape(8.dp)
                                    )
                                    .border(
                                        border = BorderStroke(1.dp, Color(0xFFC3D9FF)),
                                        shape = RoundedCornerShape(8.dp)
                                    )
                                    .padding(12.dp)
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Image(
                                        painter = painterResource(id = R.drawable.ic_info_circle_blue),
                                        contentDescription = "Info",
                                        modifier = Modifier
                                            .padding(top = 2.dp)
                                            .size(18.dp)
                                    )

                                    AppSpacer(width = 8.dp)

                                    Column(
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Text(
                                            text = "Need more than 10 licenses?",
                                            style = TextStyle(
                                                fontFamily = GillSansBold,
                                                fontSize = 13.sp,
                                                color = Color(0xFF0C2340)
                                            )
                                        )
                                        AppSpacer(height = 4.dp)
                                        Text(
                                            text = "Our standard registration supports up to 10 users. For additional licenses, please contact our sales team.",
                                            style = TextStyle(
                                                fontFamily = GillSans,
                                                fontSize = 12.sp,
                                                color = Color(0xFF4A5568),
                                                lineHeight = 16.sp
                                            )
                                        )
                                        AppSpacer(height = 8.dp)
                                        Text(
                                            text = "Contact Sales Team – Click Here",
                                            style = TextStyle(
                                                fontFamily = GillSansBold,
                                                fontSize = 12.sp,
                                                color = colorResource(id = R.color.blue),
                                                textDecoration = TextDecoration.Underline
                                            ),
                                            modifier = Modifier.clickable {
                                                focusManager.clearFocus(force = true)
                                                keyboardController?.hide()
                                                onContactSalesClick()
                                            }
                                        )
                                    }
                                }
                            }
                        }

                        AppSpacer(height = 8.dp)

                        // Terms of Service and Privacy Policy (neat 2-line layout)
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 4.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = "By signing up, you agree to our",
                                style = TextStyle(
                                    fontFamily = GillSans,
                                    fontSize = 12.sp,
                                    color = Color(0xFF4A5568),
                                    textAlign = TextAlign.Center
                                )
                            )
                            AppSpacer(height = 2.dp)
                            Row(
                                horizontalArrangement = Arrangement.Center,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "Terms of Service",
                                    style = TextStyle(
                                        fontFamily = GillSansBold,
                                        fontSize = 12.sp,
                                        color = colorResource(id = R.color.blue),
                                        textDecoration = TextDecoration.Underline
                                    ),
                                    modifier = Modifier.clickable {
                                        focusManager.clearFocus(force = true)
                                        keyboardController?.hide()
                                        onTermsClick()
                                    }
                                )
                                Text(
                                    text = " and ",
                                    style = TextStyle(
                                        fontFamily = GillSans,
                                        fontSize = 12.sp,
                                        color = Color(0xFF4A5568)
                                    )
                                )
                                Text(
                                    text = "Privacy Policy",
                                    style = TextStyle(
                                        fontFamily = GillSansBold,
                                        fontSize = 12.sp,
                                        color = colorResource(id = R.color.blue),
                                        textDecoration = TextDecoration.Underline
                                    ),
                                    modifier = Modifier.clickable {
                                        focusManager.clearFocus(force = true)
                                        keyboardController?.hide()
                                        onPrivacyClick()
                                    }
                                )
                            }
                        }
                    }

                    AppSpacer(height = 14.dp)

                    // Action Button using common AppButton with exact XML sizing
                    val btnText = if (isLoginMode && isPasswordMode) "Login" else "Send OTP"
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

@Preview(showBackground = true, name = "Register Light Preview")
@Composable
fun RegisterScreenLightPreview() {
    LoginScreen(
        emailOrMobile = "",
        onEmailOrMobileChange = {},
        passwordValue = "",
        onPasswordChange = {},
        fullNameValue = "John Doe",
        onFullNameChange = {},
        firmNameValue = "Legal Associates",
        onFirmNameChange = {},
        registerEmailValue = "john@example.com",
        onRegisterEmailChange = {},
        registerPhoneValue = "9876543210",
        onRegisterPhoneChange = {},
        licensesValue = 10,
        isLoginMode = false,
        isPasswordMode = false
    )
}
