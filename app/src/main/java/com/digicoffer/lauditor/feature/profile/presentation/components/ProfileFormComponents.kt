package com.digicoffer.lauditor.feature.profile.presentation.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import com.digicoffer.lauditor.core.ui.common.foundation.AppSpacer
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.digicoffer.lauditor.R

private val GillSans = FontFamily(Font(R.font.gill_sans))
private val GillSansBold = FontFamily(Font(R.font.gill_sans_bold))

val ProfileNavy = Color(0xFF004D87)
val ProfileInputBg = Color(0xFFF9FAFB)
val ProfileInputBorder = Color(0xFFC0C0C0)
val ProfileChipBg = Color(0xFFE2F6FF)
val ProfileChipBorder = Color(0xFFBCE0FD)
val ProfileTabContainerBg = Color(0xFFEFEFEF)
val ProfileCancelBg = Color(0xFFEBEBEB)
val ProfileCancelBorder = Color(0xFFCCCCCC)

/**
 * Clean field with external label in navy (#004D87), red '*' asterisk if required,
 * and compact input box (height ~42dp, border #C0C0C0, bg #F9FAFB).
 */
@Composable
fun ProfileFieldWithLabel(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    isRequired: Boolean = false,
    placeholder: String = "",
    enabled: Boolean = true,
    singleLine: Boolean = true,
    minLines: Int = 1,
    maxLines: Int = if (singleLine) 1 else 5,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
    keyboardActions: KeyboardActions = KeyboardActions.Default,
    errorMessage: String? = null,
    onFocusChanged: ((Boolean) -> Unit)? = null,
    onClick: (() -> Unit)? = null,
    trailingIcon: @Composable (() -> Unit)? = null
) {
    Column(modifier = modifier) {
        if (label.isNotEmpty()) {
            Text(
                text = buildAnnotatedString {
                    append(label)
                    if (isRequired) {
                        withStyle(SpanStyle(color = Color.Red, fontWeight = FontWeight.Bold)) {
                            append(" *")
                        }
                    }
                },
                fontSize = 13.sp,
                fontFamily = GillSans,
                color = ProfileNavy,
                modifier = Modifier.padding(bottom = 4.dp)
            )
        }

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .then(
                    if (singleLine) Modifier.height(42.dp)
                    else Modifier.heightIn(min = 84.dp, max = 130.dp)
                )
                .background(
                    if (enabled) ProfileInputBg else Color(0xFFF0F2F5),
                    RoundedCornerShape(6.dp)
                )
                .border(
                    width = 0.6.dp,
                    color = if (errorMessage != null) Color.Red else ProfileInputBorder,
                    shape = RoundedCornerShape(6.dp)
                )
                .then(
                    if (onClick != null && enabled) Modifier.clickable { onClick() }
                    else Modifier
                )
                .padding(horizontal = 12.dp, vertical = if (singleLine) 10.dp else 8.dp),
            contentAlignment = if (singleLine) Alignment.CenterStart else Alignment.TopStart
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = if (singleLine) Alignment.CenterVertically else Alignment.Top
            ) {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .then(
                            if (onClick != null && enabled) Modifier.clickable { onClick() }
                            else Modifier
                        )
                ) {
                    if (value.isEmpty() && placeholder.isNotEmpty()) {
                        Text(
                            text = placeholder,
                            fontSize = 13.sp,
                            fontFamily = GillSans,
                            color = Color(0xFF9E9E9E)
                        )
                    }
                    BasicTextField(
                        value = value,
                        onValueChange = onValueChange,
                        enabled = enabled,
                        singleLine = singleLine,
                        minLines = minLines,
                        maxLines = maxLines,
                        keyboardOptions = keyboardOptions,
                        keyboardActions = keyboardActions,
                        textStyle = TextStyle(
                            fontSize = 13.sp,
                            fontFamily = GillSans,
                            color = if (enabled) Color(0xFF111827) else Color(0xFF6B7280)
                        ),
                        cursorBrush = SolidColor(ProfileNavy),
                        modifier = Modifier
                            .fillMaxWidth()
                            .onFocusChanged { focusState ->
                                if (focusState.isFocused) {
                                    onFocusChanged?.invoke(true)
                                    onClick?.invoke()
                                }
                            }
                    )
                }

                if (trailingIcon != null) {
                    Spacer(modifier = Modifier.width(6.dp))
                    trailingIcon()
                }
            }
        }

        if (!errorMessage.isNullOrEmpty()) {
            Text(
                text = errorMessage,
                color = Color.Red,
                fontSize = 11.sp,
                fontFamily = GillSans,
                modifier = Modifier.padding(top = 2.dp, start = 2.dp)
            )
        }
    }
}

/**
 * Dropdown selector with external navy label and compact input box.
 */
@Composable
fun ProfileDropdownWithLabel(
    label: String,
    options: List<String>,
    selectedOption: String,
    onOptionSelected: (String) -> Unit,
    modifier: Modifier = Modifier,
    isRequired: Boolean = false,
    placeholder: String = "Select...",
    enabled: Boolean = true,
    showClear: Boolean = false,
    onClear: (() -> Unit)? = null
) {
    var expanded by remember { mutableStateOf(false) }

    Column(modifier = modifier) {
        if (label.isNotEmpty()) {
            Text(
                text = buildAnnotatedString {
                    append(label)
                    if (isRequired) {
                        withStyle(SpanStyle(color = Color.Red, fontWeight = FontWeight.Bold)) {
                            append(" *")
                        }
                    }
                },
                fontSize = 13.sp,
                fontFamily = GillSans,
                color = ProfileNavy,
                modifier = Modifier.padding(bottom = 4.dp)
            )
        }

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(42.dp)
                .background(
                    if (enabled) ProfileInputBg else Color(0xFFF0F2F5),
                    RoundedCornerShape(6.dp)
                )
                .border(
                    width = 0.6.dp,
                    color = ProfileInputBorder,
                    shape = RoundedCornerShape(6.dp)
                )
                .clickable(enabled = enabled) { expanded = true }
                .padding(horizontal = 12.dp),
            contentAlignment = Alignment.CenterStart
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = if (selectedOption.isNotEmpty()) selectedOption else placeholder,
                    fontSize = 13.sp,
                    fontFamily = GillSans,
                    color = if (selectedOption.isNotEmpty()) Color(0xFF111827) else Color(0xFF9E9E9E),
                    modifier = Modifier.weight(1f)
                )

                if (showClear && selectedOption.isNotEmpty() && onClear != null) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Clear",
                        tint = Color(0xFF6B7280),
                        modifier = Modifier
                            .size(18.dp)
                            .clickable { onClear() }
                    )
                } else {
                    Image(
                        painter = painterResource(R.drawable.drop_down_blue),
                        contentDescription = "Dropdown",
                        modifier = Modifier
                            .size(20.dp)
                            .rotate(if (expanded) 180f else 0f)
                    )
                }
            }

            DropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false },
                modifier = Modifier
                    .background(Color.White)
                    .heightIn(max = 240.dp)
            ) {
                options.forEach { option ->
                    DropdownMenuItem(
                        text = {
                            Text(
                                text = option,
                                fontSize = 13.sp,
                                fontFamily = GillSans,
                                color = if (option == selectedOption) ProfileNavy else Color.Black
                            )
                        },
                        onClick = {
                            onOptionSelected(option)
                            expanded = false
                        }
                    )
                }
            }
        }
    }
}

/**
 * Pale Blue Capsule/Pill Chip used for Practice Areas, Services, and Languages.
 * Features capsule/pill shape (50%), pale blue background (#E2F6FF), GillSans text in #004D87,
 * and a circular outlined remove icon (ⓧ) matching the reference design.
 */
@Composable
fun ProfilePaleBlueChip(
    text: String,
    modifier: Modifier = Modifier,
    onEdit: (() -> Unit)? = null,
    onRemove: (() -> Unit)? = null
) {
    Surface(
        modifier = modifier.padding(vertical = 2.dp),
        shape = RoundedCornerShape(percent = 50),
        color = ProfileChipBg
    ) {
        Row(
            modifier = Modifier.padding(
                start = 14.dp,
                end = if (onRemove != null || onEdit != null) 8.dp else 14.dp,
                top = 6.dp,
                bottom = 6.dp
            ),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = text,
                fontSize = 12.5.sp,
                fontFamily = GillSans,
                color = ProfileNavy
            )

            if (onEdit != null) {
                Icon(
                    imageVector = Icons.Default.Edit,
                    contentDescription = "Edit",
                    tint = ProfileNavy,
                    modifier = Modifier
                        .size(14.dp)
                        .clickable { onEdit() }
                )
            }

            if (onRemove != null) {
                Box(
                    modifier = Modifier
                        .size(18.dp)
                        .clip(CircleShape)
                        .clickable { onRemove() },
                    contentAlignment = Alignment.Center
                ) {
                    Box(
                        modifier = Modifier
                            .size(15.dp)
                            .border(1.2.dp, ProfileNavy, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Remove",
                            tint = ProfileNavy,
                            modifier = Modifier.size(9.dp)
                        )
                    }
                }
            }
        }
    }
}

/**
 * Multi-Select Dropdown with external navy label, compact input box,
 * animated toggle arrow, expandable options list with Checkboxes, and FlowRow pale blue chips below.
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun ProfileMultiSelectDropdown(
    label: String,
    options: List<String>,
    selectedOptions: List<String>,
    onOptionToggled: (String) -> Unit,
    modifier: Modifier = Modifier,
    isRequired: Boolean = false,
    placeholder: String = "Select...",
    enabled: Boolean = true
) {
    var expanded by remember { mutableStateOf(false) }

    Column(modifier = modifier) {
        if (label.isNotEmpty()) {
            Text(
                text = buildAnnotatedString {
                    append(label)
                    if (isRequired) {
                        withStyle(SpanStyle(color = Color.Red, fontWeight = FontWeight.Bold)) {
                            append(" *")
                        }
                    }
                },
                fontSize = 13.sp,
                fontFamily = GillSans,
                color = ProfileNavy,
                modifier = Modifier.padding(bottom = 4.dp)
            )
        }

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(42.dp)
                .background(
                    if (enabled) ProfileInputBg else Color(0xFFF0F2F5),
                    RoundedCornerShape(6.dp)
                )
                .border(
                    width = 0.6.dp,
                    color = ProfileInputBorder,
                    shape = RoundedCornerShape(6.dp)
                )
                .clickable(enabled = enabled) { expanded = !expanded }
                .padding(horizontal = 12.dp),
            contentAlignment = Alignment.CenterStart
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = placeholder,
                    fontSize = 13.sp,
                    fontFamily = GillSans,
                    color = Color(0xFF9E9E9E),
                    modifier = Modifier.weight(1f)
                )

                Image(
                    painter = painterResource(R.drawable.drop_down_blue),
                    contentDescription = "Dropdown",
                    modifier = Modifier
                        .size(20.dp)
                        .rotate(if (expanded) 180f else 0f)
                )
            }
        }

        // Expandable options list with Checkboxes
        if (expanded && options.isNotEmpty()) {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 4.dp)
                    .heightIn(max = 200.dp),
                shape = RoundedCornerShape(6.dp),
                color = Color.White,
                border = BorderStroke(0.6.dp, ProfileInputBorder),
                shadowElevation = 4.dp
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .verticalScroll(rememberScrollState())
                        .padding(vertical = 4.dp)
                ) {
                    options.forEach { option ->
                        val isSelected = selectedOptions.contains(option)
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { onOptionToggled(option) }
                                .padding(horizontal = 12.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Checkbox(
                                checked = isSelected,
                                onCheckedChange = { onOptionToggled(option) },
                                colors = CheckboxDefaults.colors(
                                    checkedColor = ProfileNavy,
                                    uncheckedColor = Color(0xFF757575)
                                )
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = option,
                                fontSize = 13.sp,
                                fontFamily = GillSans,
                                color = if (isSelected) ProfileNavy else Color(0xFF222222)
                            )
                        }
                    }
                }
            }
        }

        // Selected chips wrapping layout
        if (selectedOptions.isNotEmpty()) {
            AppSpacer(height = 6.dp)
            FlowRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                selectedOptions.forEach { option ->
                    ProfilePaleBlueChip(
                        text = option,
                        onRemove = { onOptionToggled(option) }
                    )
                }
            }
        }
    }
}

/**
 * Stepper for Consultation Fee: [ - ]  ₹ 500  [ + ]
 * Enforces a strict minimum of ₹500. When fee == 500, minus button is visually and functionally disabled.
 */
@Composable
fun ProfileConsultationFeeStepper(
    fee: String,
    currencySymbol: String = "₹",
    onFeeChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    isRequired: Boolean = true
) {
    val feeDigits = fee.filter { it.isDigit() }
    val currentFee = (feeDigits.toIntOrNull() ?: 500).coerceAtLeast(500)
    val isMinusEnabled = currentFee > 500

    Column(modifier = modifier) {
        Text(
            text = buildAnnotatedString {
                append("Consultation fee")
                if (isRequired) {
                    withStyle(SpanStyle(color = Color.Red, fontWeight = FontWeight.Bold)) {
                        append(" *")
                    }
                }
            },
            fontSize = 13.sp,
            fontFamily = GillSans,
            color = ProfileNavy,
            modifier = Modifier.padding(bottom = 4.dp)
        )

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(42.dp)
                .background(ProfileInputBg, RoundedCornerShape(6.dp))
                .border(0.6.dp, ProfileInputBorder, RoundedCornerShape(6.dp))
                .padding(horizontal = 8.dp),
            contentAlignment = Alignment.CenterStart
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "$currencySymbol $currentFee",
                    fontSize = 13.5.sp,
                    fontFamily = GillSans,
                    color = Color.Black,
                    modifier = Modifier.weight(1f)
                )

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    // [-] button
                    Box(
                        modifier = Modifier
                            .size(width = 36.dp, height = 30.dp)
                            .background(
                                if (isMinusEnabled) Color(0xFFEBEBEB) else Color(0xFFF3F4F6),
                                RoundedCornerShape(4.dp)
                            )
                            .border(0.5.dp, Color(0xFFCCCCCC), RoundedCornerShape(4.dp))
                            .clickable(enabled = isMinusEnabled) {
                                val newFee = (currentFee - 50).coerceAtLeast(500)
                                onFeeChange(newFee.toString())
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "−",
                            fontSize = 18.sp,
                            fontFamily = GillSansBold,
                            color = if (isMinusEnabled) Color.Black else Color(0xFF9E9E9E)
                        )
                    }

                    // [+] button
                    Box(
                        modifier = Modifier
                            .size(width = 36.dp, height = 30.dp)
                            .background(Color(0xFFEBEBEB), RoundedCornerShape(4.dp))
                            .border(0.5.dp, Color(0xFFCCCCCC), RoundedCornerShape(4.dp))
                            .clickable {
                                val newFee = currentFee + 50
                                onFeeChange(newFee.toString())
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "+",
                            fontSize = 18.sp,
                            fontFamily = GillSansBold,
                            color = Color.Black
                        )
                    }
                }
            }
        }
    }
}

/**
 * Rounded pill sub-tab bar: Container in #EFEFEF (radius 26dp, height 52dp), selected pill #004D87 with white text.
 */
@Composable
fun ProfileTabPillRow(
    tabs: List<Pair<String, String>>,
    selectedTab: String,
    onTabSelected: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(52.dp)
            .background(Color(0xFFEFEFEF), RoundedCornerShape(26.dp))
            .padding(4.dp),
        contentAlignment = Alignment.Center
    ) {
        Row(
            modifier = Modifier.fillMaxSize(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            tabs.forEach { (tabKey, tabLabel) ->
                val isSelected = selectedTab == tabKey
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                        .clip(RoundedCornerShape(22.dp))
                        .background(if (isSelected) ProfileNavy else Color.Transparent)
                        .clickable { onTabSelected(tabKey) }
                        .padding(horizontal = 2.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = tabLabel,
                        fontSize = 12.sp,
                        fontFamily = if (isSelected) GillSansBold else GillSans,
                        color = if (isSelected) Color.White else Color(0xFF444444),
                        textAlign = TextAlign.Center,
                        lineHeight = 14.sp
                    )
                }
            }
        }
    }
}

/**
 * Standard Save / Cancel Action Buttons
 */
@Composable
fun ProfileActionButtons(
    onCancel: () -> Unit,
    onSave: () -> Unit,
    modifier: Modifier = Modifier,
    saveEnabled: Boolean = true,
    cancelText: String = "Cancel",
    saveText: String = "Save"
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Cancel Button
        Button(
            onClick = onCancel,
            shape = RoundedCornerShape(6.dp),
            border = BorderStroke(1.dp, ProfileCancelBorder),
            colors = ButtonDefaults.buttonColors(
                containerColor = ProfileCancelBg,
                contentColor = Color.Black
            ),
            modifier = Modifier
                .weight(1f)
                .height(42.dp)
        ) {
            Text(
                text = cancelText,
                fontSize = 14.sp,
                fontFamily = GillSans,
                color = Color.Black
            )
        }

        // Save Button
        Button(
            onClick = onSave,
            enabled = saveEnabled,
            shape = RoundedCornerShape(6.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = ProfileNavy,
                contentColor = Color.White,
                disabledContainerColor = ProfileNavy.copy(alpha = 0.5f)
            ),
            modifier = Modifier
                .weight(1f)
                .height(42.dp)
        ) {
            Text(
                text = saveText,
                fontSize = 14.sp,
                fontFamily = GillSansBold,
                color = Color.White
            )
        }
    }
}

/**
 * Editable Searchable Dropdown field with external navy label, text input for custom typing & search filtering,
 * and a dropdown list of matching options.
 */
@Composable
fun ProfileSearchableDropdownField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    options: List<String>,
    modifier: Modifier = Modifier,
    placeholder: String = "",
    isRequired: Boolean = false,
    enabled: Boolean = true,
    emptyMessage: String = "No options found",
    errorMessage: String? = null,
    onOptionSelected: (String) -> Unit
) {
    var isDropdownOpen by remember { mutableStateOf(false) }

    // Filtered options based on user typing
    val filteredOptions = remember(options, value) {
        val q = value.trim()
        if (q.isEmpty()) {
            options
        } else {
            val matches = options.filter { it.contains(q, ignoreCase = true) }
            if (matches.isEmpty()) options else matches
        }
    }

    Column(modifier = modifier) {
        if (label.isNotEmpty()) {
            Text(
                text = buildAnnotatedString {
                    append(label)
                    if (isRequired) {
                        withStyle(SpanStyle(color = Color.Red, fontWeight = FontWeight.Bold)) {
                            append(" *")
                        }
                    }
                },
                fontSize = 13.sp,
                fontFamily = GillSans,
                color = ProfileNavy,
                modifier = Modifier.padding(bottom = 4.dp)
            )
        }

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(42.dp)
                .background(
                    if (enabled) ProfileInputBg else Color(0xFFF0F2F5),
                    RoundedCornerShape(6.dp)
                )
                .border(
                    width = 0.6.dp,
                    color = if (errorMessage != null) Color.Red else ProfileInputBorder,
                    shape = RoundedCornerShape(6.dp)
                )
                .padding(horizontal = 12.dp, vertical = 10.dp),
            contentAlignment = Alignment.CenterStart
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier.weight(1f)
                ) {
                    if (value.isEmpty() && placeholder.isNotEmpty()) {
                        Text(
                            text = placeholder,
                            fontSize = 13.sp,
                            fontFamily = GillSans,
                            color = Color(0xFF9E9E9E)
                        )
                    }
                    BasicTextField(
                        value = value,
                        onValueChange = {
                            onValueChange(it)
                            if (enabled) isDropdownOpen = true
                        },
                        enabled = enabled,
                        singleLine = true,
                        textStyle = TextStyle(
                            fontSize = 13.sp,
                            fontFamily = GillSans,
                            color = if (enabled) Color(0xFF111827) else Color(0xFF6B7280)
                        ),
                        cursorBrush = SolidColor(ProfileNavy),
                        modifier = Modifier
                            .fillMaxWidth()
                            .onFocusChanged { focusState ->
                                if (focusState.isFocused && enabled) {
                                    isDropdownOpen = true
                                }
                            }
                    )
                }

                Spacer(modifier = Modifier.width(6.dp))

                Image(
                    painter = painterResource(R.drawable.drop_down_blue),
                    contentDescription = "Dropdown",
                    modifier = Modifier
                        .size(20.dp)
                        .rotate(if (isDropdownOpen) 180f else 0f)
                        .clickable(enabled = enabled) {
                            isDropdownOpen = !isDropdownOpen
                        }
                )
            }
        }

        if (isDropdownOpen && enabled) {
            AppSpacer(height = 4.dp)
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 200.dp),
                shape = RoundedCornerShape(6.dp),
                color = Color.White,
                border = BorderStroke(0.8.dp, Color(0xFFD1D5DB)),
                shadowElevation = 4.dp
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .verticalScroll(rememberScrollState())
                ) {
                    if (filteredOptions.isEmpty()) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = emptyMessage,
                                fontSize = 12.5.sp,
                                fontFamily = GillSans,
                                color = Color(0xFF9CA3AF)
                            )
                        }
                    } else {
                        filteredOptions.forEachIndexed { index, option ->
                            val isSelected = option.equals(value.trim(), ignoreCase = true)
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        onOptionSelected(option)
                                        isDropdownOpen = false
                                    }
                                    .padding(horizontal = 14.dp, vertical = 10.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = option,
                                    fontSize = 13.sp,
                                    fontFamily = GillSans,
                                    color = if (isSelected) ProfileNavy else Color(0xFF1F2937),
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                    modifier = Modifier.weight(1f)
                                )
                            }

                            if (index < filteredOptions.size - 1) {
                                HorizontalDivider(color = Color(0xFFF3F4F6), thickness = 0.6.dp)
                            }
                        }
                    }
                }
            }
        }

        if (!errorMessage.isNullOrEmpty()) {
            Text(
                text = errorMessage,
                color = Color.Red,
                fontSize = 11.sp,
                fontFamily = GillSans,
                modifier = Modifier.padding(top = 2.dp, start = 2.dp)
            )
        }
    }
}

