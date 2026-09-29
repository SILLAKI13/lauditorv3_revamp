package com.digicoffer.lauditor.feature.profile.presentation.components

import android.app.DatePickerDialog
import android.content.Context
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.digicoffer.lauditor.CommonFiles.GlobalFiles.Constants
import com.digicoffer.lauditor.R
import com.digicoffer.lauditor.core.ui.common.foundation.AppSpacer
import com.digicoffer.lauditor.feature.profile.presentation.state.ProfileEditFormState
import com.digicoffer.lauditor.feature.profile.presentation.state.ProfileUiEvent
import java.text.SimpleDateFormat
import java.util.*

private val GillSans = FontFamily(Font(R.font.gill_sans))
private val GillSansBold = FontFamily(Font(R.font.gill_sans_bold))

@Composable
fun EditProfileInfoCard(
    formState: ProfileEditFormState,
    isMyProfile: Boolean,
    isBioGenerating: Boolean,
    genderList: List<String>,
    onEvent: (ProfileUiEvent) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val prefs = remember(context) { context.getSharedPreferences("MyPrefs", Context.MODE_PRIVATE) }
    val isSolo = "solo".equals(Constants.CATEGORY, ignoreCase = true)
    val isEntity = "entity".equals(Constants.CATEGORY, ignoreCase = true)
    val isGHTeamMember = Constants.ROLE.equals("GH", ignoreCase = true) || Constants.ROLE.equals("TM", ignoreCase = true)

    val rawLoginMethod = prefs.getString("login_method", "")?.ifEmpty { Constants.LOGIN_METHOD } ?: Constants.LOGIN_METHOD ?: ""
    val loginMethod = (if (rawLoginMethod.isNullOrEmpty()) "email" else rawLoginMethod).lowercase(Locale.ROOT)
    
    // Lock email if login method is email or if user already has an email populated
    val isEmailEnabled = loginMethod != "email" && formState.email.isEmpty()
    
    // Lock phone if login method is mobile or if user already has a phone number populated
    val isPhoneEnabled = loginMethod != "mobile" && formState.phone.isEmpty()

    val showFirmName = !isMyProfile && !isSolo && !isGHTeamMember
    val contactLabel = if (showFirmName) "Contact Name" else "Name"
    val cardTitle = if (isMyProfile) "Profile Information" else "Firm Information"
    val bioLabel = if (isMyProfile) "Bio" else "About the Firm"
    val showDobAndGender = isSolo

    fun showDatePicker() {
        val cal = Calendar.getInstance()
        if (formState.dob.isNotBlank()) {
            try {
                val sdf = SimpleDateFormat("dd-MM-yyyy", Locale.getDefault())
                cal.time = sdf.parse(formState.dob) ?: Calendar.getInstance().time
            } catch (_: Exception) {
                try {
                    val sdf2 = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                    cal.time = sdf2.parse(formState.dob) ?: Calendar.getInstance().time
                } catch (_: Exception) {}
            }
        }
        val dp = DatePickerDialog(
            context,
            { _, y, m, d ->
                val sel = Calendar.getInstance().apply {
                    set(Calendar.YEAR, y)
                    set(Calendar.MONTH, m)
                    set(Calendar.DAY_OF_MONTH, d)
                }
                val outFormat = SimpleDateFormat("dd-MM-yyyy", Locale.getDefault())
                onEvent(ProfileUiEvent.UpdateDob(outFormat.format(sel.time)))
            },
            cal.get(Calendar.YEAR),
            cal.get(Calendar.MONTH),
            cal.get(Calendar.DAY_OF_MONTH)
        )
        dp.datePicker.maxDate = System.currentTimeMillis()
        dp.show()
    }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 14.dp, vertical = 8.dp),
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            // ── Section Header with Close Button ────────────────────────────────
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = cardTitle,
                    color = ProfileNavy,
                    fontSize = 16.sp,
                    fontFamily = GillSansBold
                )

                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = "Close",
                    tint = Color(0xFF666666),
                    modifier = Modifier
                        .size(20.dp)
                        .clickable { onEvent(ProfileUiEvent.CancelEditFirmInfo) }
                )
            }

            AppSpacer(height = 14.dp)

            // ── Firm Name (Visible on Firm Profile unless solo or GH team member) ──
            if (showFirmName) {
                ProfileFieldWithLabel(
                    label = "Firm Name",
                    value = formState.firmName,
                    onValueChange = { onEvent(ProfileUiEvent.UpdateFirmName(it)) },
                    placeholder = "Enter firm name",
                    isRequired = true,
                    enabled = !isGHTeamMember,
                    modifier = Modifier.fillMaxWidth()
                )
                AppSpacer(height = 12.dp)
            }

            // ── Contact Name / Name ────────────────────────────────────────────
            ProfileFieldWithLabel(
                label = contactLabel,
                value = formState.contactName,
                onValueChange = { onEvent(ProfileUiEvent.UpdateContactName(it)) },
                placeholder = if (showFirmName) "Enter contact name" else "Enter name",
                isRequired = true,
                modifier = Modifier.fillMaxWidth()
            )

            AppSpacer(height = 12.dp)

            // ── Email ──────────────────────────────────────────────────────────
            ProfileFieldWithLabel(
                label = "Email",
                value = formState.email,
                onValueChange = { onEvent(ProfileUiEvent.UpdateEmail(it)) },
                placeholder = "Enter email address",
                isRequired = true,
                enabled = isEmailEnabled,
                errorMessage = formState.emailError,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                modifier = Modifier.fillMaxWidth()
            )

            AppSpacer(height = 12.dp)

            // ── Phone Number ───────────────────────────────────────────────────
            ProfileFieldWithLabel(
                label = "Phone Number",
                value = formState.phone,
                onValueChange = { onEvent(ProfileUiEvent.UpdatePhone(it)) },
                placeholder = "Enter 10-digit mobile number",
                isRequired = true,
                enabled = isPhoneEnabled,
                errorMessage = formState.phoneError,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                modifier = Modifier.fillMaxWidth()
            )

            AppSpacer(height = 12.dp)

            // ── Gender & Date of Birth (Shown for individual / non-entity) ─────
            if (showDobAndGender) {
                ProfileDropdownWithLabel(
                    label = "Gender",
                    options = genderList,
                    selectedOption = formState.gender,
                    onOptionSelected = { onEvent(ProfileUiEvent.UpdateGender(it)) },
                    placeholder = "Select gender",
                    modifier = Modifier.fillMaxWidth()
                )

                AppSpacer(height = 12.dp)

                ProfileFieldWithLabel(
                    label = "Date of Birth",
                    value = formState.dob,
                    onValueChange = { onEvent(ProfileUiEvent.UpdateDob(it)) },
                    placeholder = "DD-MM-YYYY",
                    onClick = { showDatePicker() },
                    trailingIcon = {
                        Icon(
                            imageVector = Icons.Default.DateRange,
                            contentDescription = "Select Date of Birth",
                            tint = ProfileNavy,
                            modifier = Modifier
                                .size(20.dp)
                                .clickable { showDatePicker() }
                        )
                    },
                    modifier = Modifier.fillMaxWidth()
                )

                AppSpacer(height = 12.dp)
            }

            // ── Website ────────────────────────────────────────────────────────
            ProfileFieldWithLabel(
                label = "Website",
                value = formState.website,
                onValueChange = { onEvent(ProfileUiEvent.UpdateWebsite(it)) },
                placeholder = "Enter website URL",
                isRequired = false,
                enabled = !isGHTeamMember,
                modifier = Modifier.fillMaxWidth()
            )

            AppSpacer(height = 12.dp)

            // ── Bio / About the Firm ───────────────────────────────────────────
            ProfileFieldWithLabel(
                label = bioLabel,
                value = formState.bio,
                onValueChange = { onEvent(ProfileUiEvent.UpdateBio(it)) },
                placeholder = if (isMyProfile) "Write your professional bio or click 'Summarize Bio' to generate one with AI." else "Write a description about your firm or click 'Summarize Bio' to generate one with AI.",
                singleLine = false,
                minLines = 4,
                maxLines = 8,
                modifier = Modifier.fillMaxWidth()
            )

            AppSpacer(height = 10.dp)

            // ── Summarize Bio Button (Right Aligned) ────────────────────────────
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Button(
                    onClick = { onEvent(ProfileUiEvent.GenerateBioClick) },
                    enabled = !isBioGenerating,
                    shape = RoundedCornerShape(4.dp),
                    border = BorderStroke(1.dp, ProfileNavy),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color.White,
                        contentColor = ProfileNavy,
                        disabledContainerColor = Color(0xFFF5F5F5)
                    ),
                    contentPadding = PaddingValues(horizontal = 14.dp, vertical = 6.dp),
                    modifier = Modifier.height(36.dp)
                ) {
                    if (isBioGenerating) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(14.dp),
                            strokeWidth = 2.dp,
                            color = ProfileNavy
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Generating...",
                            fontSize = 13.sp,
                            fontFamily = GillSansBold,
                            color = ProfileNavy
                        )
                    } else {
                        Icon(
                            painter = painterResource(id = R.drawable.generate_icon),
                            contentDescription = null,
                            tint = ProfileNavy,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Summarize Bio",
                            fontSize = 13.sp,
                            fontFamily = GillSansBold,
                            color = ProfileNavy
                        )
                    }
                }
            }

            // ── AI Bio Hint with Info Icon ─────────────────────────────────────
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 4.dp, bottom = 16.dp),
                verticalAlignment = Alignment.Top,
                horizontalArrangement = Arrangement.Start
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.iv_info),
                    contentDescription = "Info",
                    tint = Color(0xFF757575),
                    modifier = Modifier
                        .padding(top = 1.dp, end = 6.dp)
                        .size(16.dp)
                )
                Text(
                    text = "Optional: Use AI to generate a shorter professional bio. You can re-summarize anytime.",
                    color = Color(0xFF757575),
                    fontSize = 12.sp,
                    fontFamily = GillSans,
                    lineHeight = 16.sp
                )
            }

            // ── Bottom Action Buttons: Cancel and Save ─────────────────────────
            ProfileActionButtons(
                onCancel = { onEvent(ProfileUiEvent.CancelEditFirmInfo) },
                onSave = { onEvent(ProfileUiEvent.SaveEditFirmInfo) }
            )
        }
    }
}
