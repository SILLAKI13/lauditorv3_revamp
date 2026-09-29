package com.digicoffer.lauditor.feature.profile.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.digicoffer.lauditor.R
import com.digicoffer.lauditor.feature.profile.presentation.state.PracticePartnerFormState
import com.digicoffer.lauditor.feature.profile.presentation.state.ProfileUiEvent

private val GillSans = FontFamily(
    Font(R.font.gill_sans)
)

@Composable
private fun FormLabel(text: String, isMandatory: Boolean = false) {
    Text(
        text = buildAnnotatedString {
            append(text)
            if (isMandatory) {
                withStyle(style = SpanStyle(color = Color.Red)) {
                    append(" *")
                }
            }
        },
        color = Color(0xFF004D87),
        fontFamily = GillSans,
        fontWeight = FontWeight.Normal,
        fontSize = 15.sp
    )
}

@Composable
private fun PartnerCustomTextField(
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    enabled: Boolean = true,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
    interactionSource: MutableInteractionSource = remember { MutableInteractionSource() },
    modifier: Modifier = Modifier
) {
    val inputBorderColor = Color(0xFFC0C0C0)
    val inputBgColor = if (enabled) Color(0xFFF9FAFB) else Color(0xFFEEEEEE)
    val textStyle = TextStyle(
        fontSize = 15.sp,
        fontFamily = GillSans,
        color = Color.Black
    )

    BasicTextField(
        value = value,
        onValueChange = onValueChange,
        textStyle = textStyle,
        keyboardOptions = keyboardOptions,
        interactionSource = interactionSource,
        singleLine = true,
        enabled = enabled,
        cursorBrush = SolidColor(Color.Black),
        modifier = modifier
            .fillMaxWidth()
            .background(inputBgColor, shape = RoundedCornerShape(6.dp))
            .border(width = 0.5.dp, color = inputBorderColor, shape = RoundedCornerShape(6.dp)),
        decorationBox = { innerTextField ->
            Box(
                modifier = Modifier.padding(10.dp)
            ) {
                if (value.isEmpty()) {
                    Text(
                        text = placeholder,
                        style = textStyle.copy(color = Color(0xFFA0A0A0))
                    )
                }
                innerTextField()
            }
        }
    )
}

@Composable
fun PracticePartnerAddCard(
    formState: PracticePartnerFormState,
    onEvent: (ProfileUiEvent) -> Unit,
    modifier: Modifier = Modifier
) {
    val scrollState = rememberScrollState()

    Card(
        modifier = modifier
            .fillMaxWidth()
            .border(width = 0.5.dp, color = Color(0xFFDDDDDE), shape = RoundedCornerShape(8.dp))
            .padding(horizontal = 10.dp, vertical = 6.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        shape = RoundedCornerShape(8.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp)
                .verticalScroll(scrollState)
        ) {
            // ── Name ─────────────────────────────────────────────────────────
            FormLabel(text = "Name", isMandatory = true)
            Spacer(modifier = Modifier.height(6.dp))
            PartnerCustomTextField(
                value = formState.name,
                onValueChange = { onEvent(ProfileUiEvent.UpdatePartnerName(it)) },
                placeholder = "Name"
            )
            if (formState.nameError != null) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = formState.nameError,
                    color = Color.Red,
                    fontFamily = GillSans,
                    fontSize = 12.sp
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            // ── Designation ──────────────────────────────────────────────────
            FormLabel(text = "Designation", isMandatory = true)
            Spacer(modifier = Modifier.height(6.dp))
            PartnerCustomTextField(
                value = formState.designation,
                onValueChange = { onEvent(ProfileUiEvent.UpdatePartnerDesignation(it)) },
                placeholder = "Designation"
            )
            if (formState.designationError != null) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = formState.designationError,
                    color = Color.Red,
                    fontFamily = GillSans,
                    fontSize = 12.sp
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            // ── Specialist ───────────────────────────────────────────────────
            FormLabel(text = "Specialist", isMandatory = true)
            Spacer(modifier = Modifier.height(6.dp))
            PartnerCustomTextField(
                value = formState.specialist,
                onValueChange = { onEvent(ProfileUiEvent.UpdatePartnerSpecialist(it)) },
                placeholder = "Specialist"
            )
            if (formState.specialistError != null) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = formState.specialistError,
                    color = Color.Red,
                    fontFamily = GillSans,
                    fontSize = 12.sp
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            // ── Email ────────────────────────────────────────────────────────
            FormLabel(text = "Email", isMandatory = true)
            Spacer(modifier = Modifier.height(6.dp))
            PartnerCustomTextField(
                value = formState.email,
                onValueChange = { onEvent(ProfileUiEvent.UpdatePartnerEmail(it)) },
                placeholder = "Email",
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email)
            )
            if (formState.emailError != null) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = formState.emailError,
                    color = Color.Red,
                    fontFamily = GillSans,
                    fontSize = 12.sp
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            // ── Phone Number ─────────────────────────────────────────────────
            FormLabel(text = "Phone Number", isMandatory = true)
            Spacer(modifier = Modifier.height(6.dp))
            PartnerCustomTextField(
                value = formState.phone,
                onValueChange = { onEvent(ProfileUiEvent.UpdatePartnerPhone(it)) },
                placeholder = "Phone Number",
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone)
            )
            if (formState.phoneError != null) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = formState.phoneError,
                    color = Color.Red,
                    fontFamily = GillSans,
                    fontSize = 12.sp
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            // ── Action Buttons: Cancel and Save (Relationships Form Style) ──
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Button(
                    onClick = { onEvent(ProfileUiEvent.CancelAddEditPracticePartner) },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFE0E0E0)),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier
                        .width(120.dp)
                        .height(40.dp)
                ) {
                    Text(
                        text = "Cancel",
                        color = Color.Black,
                        fontFamily = GillSans,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Normal
                    )
                }

                Spacer(modifier = Modifier.width(16.dp))

                Button(
                    onClick = { onEvent(ProfileUiEvent.SavePracticePartner) },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF004D87)),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier
                        .width(120.dp)
                        .height(40.dp)
                ) {
                    Text(
                        text = if (formState.id != null) "Update" else "Save",
                        color = Color.White,
                        fontFamily = GillSans,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Normal
                    )
                }
            }
            Spacer(modifier = Modifier.height(10.dp))
        }
    }
}
