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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.digicoffer.lauditor.R
import com.digicoffer.lauditor.core.ui.common.feedback.AppLoader
import com.digicoffer.lauditor.core.ui.common.feedback.AppPasswordRequirements
import com.digicoffer.lauditor.core.ui.common.feedback.PasswordRequirementItem
import com.digicoffer.lauditor.core.ui.common.foundation.AppSpacer

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
                    androidx.compose.material3.Text(
                        text = stringResource(id = R.string.reset_password),
                        style = TextStyle(
                            fontFamily = GillSansBold,
                            fontSize = 20.sp,
                            color = colorResource(id = R.color.black)
                        ),
                        modifier = Modifier.padding(
                            top = dimensionResource(id = R.dimen.twenty_dp),
                            bottom = dimensionResource(id = R.dimen.twenty_dp)
                        )
                    )

                    // Password Field 1 matching password_layout.xml
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
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Box(modifier = Modifier.weight(1f)) {
                                XmlBasicInput(
                                    value = password1Value,
                                    onValueChange = onPassword1Change,
                                    hint = stringResource(id = R.string.password),
                                    keyboardType = KeyboardType.Password,
                                    visualTransformation = if (isP1Visible) VisualTransformation.None else PasswordVisualTransformation()
                                )
                            }
                            Image(
                                painter = painterResource(
                                    id = if (isP1Visible) R.drawable.eye_open else R.drawable.eye_close
                                ),
                                contentDescription = "Toggle Password",
                                modifier = Modifier
                                    .padding(end = dimensionResource(id = R.dimen.five_dp))
                                    .height(dimensionResource(id = R.dimen.twenty_five))
                                    .clickable { isP1Visible = !isP1Visible }
                            )
                        }
                    }

                    // Password Field 2 matching password_layout.xml
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
                                    value = password2Value,
                                    onValueChange = onPassword2Change,
                                    hint = stringResource(id = R.string.confirm_password),
                                    keyboardType = KeyboardType.Password,
                                    visualTransformation = if (isP2Visible) VisualTransformation.None else PasswordVisualTransformation()
                                )
                            }
                            Image(
                                painter = painterResource(
                                    id = if (isP2Visible) R.drawable.eye_open else R.drawable.eye_close
                                ),
                                contentDescription = "Toggle Password",
                                modifier = Modifier
                                    .padding(end = dimensionResource(id = R.dimen.five_dp))
                                    .height(dimensionResource(id = R.dimen.twenty_five))
                                    .clickable { isP2Visible = !isP2Visible }
                            )
                        }
                    }

                    // Password Requirements List matching reset_password_file.xml line 50
                    AppPasswordRequirements(
                        requirements = requirements,
                        modifier = Modifier.padding(horizontal = dimensionResource(id = R.dimen.ten_dp))
                    )

                    // Action Buttons matching layout_button.xml line 30
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(
                                top = dimensionResource(id = R.dimen.ten_dp),
                                bottom = dimensionResource(id = R.dimen.twenty_dp),
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
                                text = stringResource(id = R.string.reset),
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

@Preview(showBackground = true, name = "Reset Password Preview")
@Composable
fun ResetPasswordScreenPreview() {
    ResetPasswordScreen(
        password1Value = "Password123!",
        onPassword1Change = {},
        password2Value = "Password123!",
        onPassword2Change = {},
        requirements = listOf(
            PasswordRequirementItem("Must be 8–15 characters long", true),
            PasswordRequirementItem("Must include uppercase and lowercase letters", true),
            PasswordRequirementItem("Must include a number and a special character", true)
        ),
        isSubmitEnabled = true,
        onSubmitClick = {},
        onCancelClick = {}
    )
}
