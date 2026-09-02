package com.digicoffer.lauditor.feature.members.presentation.components

import android.util.Patterns
import android.widget.Toast
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
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.digicoffer.lauditor.CommonFiles.GlobalFiles.AndroidUtils
import com.digicoffer.lauditor.R

@Composable
fun CustomTextField(
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
    modifier: Modifier = Modifier
) {
    val inputBorderColor = Color(0xFFC0C0C0)
    val inputBgColor = Color(0xFFF9FAFB) // Exact color from rectangle_light_grey.xml (#F9FAFB)
    val textStyle = TextStyle(
        fontSize = 15.sp,
        fontFamily = FontFamily(Font(R.font.gill_sans_regular)),
        color = Color.Black
    )

    androidx.compose.foundation.text.BasicTextField(
        value = value,
        onValueChange = onValueChange,
        textStyle = textStyle,
        keyboardOptions = keyboardOptions,
        singleLine = true,
        modifier = modifier
            .fillMaxWidth()
            .background(inputBgColor, shape = RoundedCornerShape(6.dp))
            .border(width = 0.5.dp, color = inputBorderColor, shape = RoundedCornerShape(6.dp)),
        decorationBox = { innerTextField ->
            Box(
                modifier = Modifier
                    .padding(10.dp) // Exact padding from title_description_layout.xml (10.dp)
            ) {
                if (value.isEmpty()) {
                    Text(
                        text = placeholder,
                        style = textStyle.copy(color = Color(0xFF707070)) // grey_color_dark color
                    )
                }
                innerTextField()
            }
        }
    )
}

@Composable
fun MemberFormCard(
    name: String,
    onNameChange: (String) -> Unit,
    designation: String,
    onDesignationChange: (String) -> Unit,
    currency: String,
    onCurrencyChange: (String) -> Unit,
    rate: String,
    onRateChange: (String) -> Unit,
    email: String,
    onEmailChange: (String) -> Unit,
    confirmEmail: String,
    onConfirmEmailChange: (String) -> Unit,
    showButtons: Boolean,
    onCancelClick: () -> Unit,
    onSaveClick: () -> Unit,
    onAssignGroupsClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val activeBlue = Color(0xFF004D87)
    val inputBorderColor = Color(0xFFC0C0C0)
    val inputBgColor = Color(0xFFF9FAFB) // Exact color from rectangle_light_grey.xml (#F9FAFB)
    val cancelBgColor = Color(0xFFEEEEEE)
    val cancelBorderColor = Color(0xFFDDDDDE)

    var isDropdownExpanded by remember { mutableStateOf(false) }
    var showValidationErrorAlert by remember { mutableStateOf(false) }

    val currencies = AndroidUtils.getCurrency_list() ?: listOf("USDollar(USD)", "IndianRupee(INR)", "Euro(EUR)")

    Card(
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
        modifier = modifier
            .fillMaxWidth()
            .padding(16.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            // Name *
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(text = "Name", color = activeBlue, fontWeight = FontWeight.SemiBold, fontSize = 15.sp, fontFamily = FontFamily(Font(R.font.gill_sans_regular)))
                Text(text = " *", color = Color.Red, fontWeight = FontWeight.Bold, fontSize = 15.sp)
            }
            CustomTextField(
                value = name,
                onValueChange = onNameChange,
                placeholder = "Name",
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Designation *
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(text = "Designation", color = activeBlue, fontWeight = FontWeight.SemiBold, fontSize = 15.sp, fontFamily = FontFamily(Font(R.font.gill_sans_regular)))
                Text(text = " *", color = Color.Red, fontWeight = FontWeight.Bold, fontSize = 15.sp)
            }
            CustomTextField(
                value = designation,
                onValueChange = onDesignationChange,
                placeholder = "Designation",
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Currency & Rate row
            Row(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(text = "Default Currency", color = activeBlue, fontWeight = FontWeight.SemiBold, fontSize = 15.sp, fontFamily = FontFamily(Font(R.font.gill_sans_regular)))
                        Text(text = " *", color = Color.Red, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(inputBgColor, shape = RoundedCornerShape(6.dp))
                            .border(width = 0.5.dp, color = inputBorderColor, shape = RoundedCornerShape(6.dp))
                            .clickable { isDropdownExpanded = true }
                            .padding(horizontal = 10.dp, vertical = 10.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = if (currency.isEmpty()) "Select Currency" else currency,
                                color = if (currency.isEmpty()) Color.Gray else Color.Black,
                                fontFamily = FontFamily(Font(R.font.gill_sans_regular)),
                                fontSize = 15.sp,
                                maxLines = 1,
                                overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis,
                                modifier = Modifier.weight(1f)
                            )
                            if (currency.isNotEmpty()) {
                                Image(
                                    painter = painterResource(id = R.drawable.simple_cancel_blue),
                                    contentDescription = "Clear",
                                    modifier = Modifier
                                        .size(20.dp)
                                        .clickable { onCurrencyChange("") }
                                )
                            } else {
                                Image(
                                    painter = painterResource(id = R.drawable.drop_down_blue),
                                    contentDescription = "Dropdown",
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }
                        DropdownMenu(
                            expanded = isDropdownExpanded,
                            onDismissRequest = { isDropdownExpanded = false },
                            modifier = Modifier
                                .background(Color.White)
                                .fillMaxWidth(0.45f)
                        ) {
                            currencies.forEach { currencyItem ->
                                DropdownMenuItem(
                                    text = {
                                        Text(
                                            text = currencyItem,
                                            fontFamily = FontFamily(Font(R.font.gill_sans_regular)),
                                            fontSize = 15.sp,
                                            color = Color.Black
                                        )
                                    },
                                    onClick = {
                                        onCurrencyChange(currencyItem)
                                        isDropdownExpanded = false
                                    }
                                )
                            }
                        }
                    }
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(text = "Default Rate", color = activeBlue, fontWeight = FontWeight.SemiBold, fontSize = 15.sp, fontFamily = FontFamily(Font(R.font.gill_sans_regular)))
                        Text(text = " *", color = Color.Red, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                    CustomTextField(
                        value = rate,
                        onValueChange = { if (it.isEmpty() || it.all { char -> char.isDigit() || char == '.' }) onRateChange(it) },
                        placeholder = "Default Rate",
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Email *
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(text = "Email", color = activeBlue, fontWeight = FontWeight.SemiBold, fontSize = 15.sp, fontFamily = FontFamily(Font(R.font.gill_sans_regular)))
                Text(text = " *", color = Color.Red, fontWeight = FontWeight.Bold, fontSize = 15.sp)
            }
            Spacer(modifier = Modifier.height(6.dp))
            CustomTextField(
                value = email,
                onValueChange = onEmailChange,
                placeholder = "Email",
                modifier = Modifier.fillMaxWidth()
            )

            val isEmailValid = email.isEmpty() || android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()
            if (!isEmailValid) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Please enter a valid email address",
                    color = Color(0xFF585858), // grey_medium (#585858)
                    fontSize = 12.sp,
                    fontFamily = FontFamily(Font(R.font.gill_sans_regular)),
                    modifier = Modifier.padding(start = 4.dp)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Confirm Email *
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(text = "Confirm Email", color = activeBlue, fontWeight = FontWeight.SemiBold, fontSize = 15.sp, fontFamily = FontFamily(Font(R.font.gill_sans_regular)))
                Text(text = " *", color = Color.Red, fontWeight = FontWeight.Bold, fontSize = 15.sp)
            }
            Spacer(modifier = Modifier.height(6.dp))
            CustomTextField(
                value = confirmEmail,
                onValueChange = onConfirmEmailChange,
                placeholder = "Confirm Email",
                modifier = Modifier.fillMaxWidth()
            )

            val isConfirmEmailValid = confirmEmail.isEmpty() || android.util.Patterns.EMAIL_ADDRESS.matcher(confirmEmail).matches()
            if (confirmEmail.isNotEmpty()) {
                if (!isConfirmEmailValid) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Please enter a valid email address",
                        color = Color(0xFF585858), // grey_medium (#585858)
                        fontSize = 12.sp,
                        fontFamily = FontFamily(Font(R.font.gill_sans_regular)),
                        modifier = Modifier.padding(start = 4.dp)
                    )
                } else if (email != confirmEmail) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Email and confirm email mismatch, please check.",
                        color = Color(0xFF585858), // grey_medium (#585858)
                        fontSize = 12.sp,
                        fontFamily = FontFamily(Font(R.font.gill_sans_regular)),
                        modifier = Modifier.padding(start = 4.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Assign Group(s) Selector Row (Matches input fields styling)
            Text(text = "Assign Group(s)", color = activeBlue, fontWeight = FontWeight.SemiBold, fontSize = 15.sp, fontFamily = FontFamily(Font(R.font.gill_sans_regular)))
            Spacer(modifier = Modifier.height(6.dp))
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(inputBgColor, shape = RoundedCornerShape(6.dp))
                    .border(width = 0.5.dp, color = inputBorderColor, shape = RoundedCornerShape(6.dp))
                    .clickable {
                        val isEmailValidCheck = android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()
                        if (name.isBlank() || designation.isBlank() || rate.isBlank() ||
                            email.isBlank() || confirmEmail.isBlank() ||
                            !isEmailValidCheck || email != confirmEmail) {
                            showValidationErrorAlert = true
                        } else {
                            onAssignGroupsClick()
                        }
                    }
                    .padding(horizontal = 10.dp, vertical = 10.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Assign Group(s)",
                        color = Color.Black,
                        fontFamily = FontFamily(Font(R.font.gill_sans_regular)),
                        fontSize = 15.sp
                    )
                    Image(
                        painter = painterResource(id = R.drawable.back_arrow), // arrow icon
                        contentDescription = "Select groups",
                        modifier = Modifier.size(14.dp)
                    )
                }
            }

            if (showButtons) {
                Spacer(modifier = Modifier.height(24.dp))

                // Form buttons (Save & Cancel)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center
                ) {
                    Button(
                        onClick = onCancelClick,
                        colors = ButtonDefaults.buttonColors(containerColor = cancelBgColor),
                        border = BorderStroke(1.dp, cancelBorderColor),
                        shape = RoundedCornerShape(8.dp),
                        contentPadding = PaddingValues(0.dp),
                        modifier = Modifier
                            .weight(1f)
                            .height(40.dp)
                    ) {
                        Text(
                            text = "Cancel",
                            color = Color.Black,
                            fontWeight = FontWeight.Medium,
                            fontSize = 14.sp,
                            fontFamily = FontFamily(Font(R.font.gill_sans_regular))
                        )
                    }

                    Spacer(modifier = Modifier.width(16.dp))

                    Button(
                        onClick = {
                            val isEmailValid = android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()
                            if (name.isBlank() || designation.isBlank() || rate.isBlank() ||
                                email.isBlank() || confirmEmail.isBlank() ||
                                !isEmailValid || email != confirmEmail) {
                                showValidationErrorAlert = true
                            } else {
                                onSaveClick()
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = activeBlue),
                        shape = RoundedCornerShape(8.dp),
                        contentPadding = PaddingValues(0.dp),
                        modifier = Modifier
                            .weight(1f)
                            .height(40.dp)
                    ) {
                        Text(
                            text = "Save",
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

    if (showValidationErrorAlert) {
        MembersAlertDialog(
            title = "Alert !",
            message = "Please check the Name, Designation, Default Rate, Email, Confirm Email",
            onConfirm = { showValidationErrorAlert = false },
            onDismiss = { showValidationErrorAlert = false }
        )
    }
}
