package com.digicoffer.lauditor.feature.matter.presentation.screen

import android.widget.TextView
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.digicoffer.lauditor.core.ui.common.badges.AppPillBadge
import androidx.lifecycle.viewmodel.compose.viewModel
import com.digicoffer.lauditor.CommonFiles.GlobalFiles.AndroidUtils
import com.digicoffer.lauditor.Matter.Models.AdvocateModel
import com.digicoffer.lauditor.Matter.Models.ViewMatterModel
import com.digicoffer.lauditor.R
import com.digicoffer.lauditor.core.designsystem.colors.ColorTokens
import com.digicoffer.lauditor.feature.matter.presentation.viewmodel.MatterEditViewModel
import com.digicoffer.lauditor.core.ui.common.feedback.AppLoader

private val GillSans = FontFamily(Font(R.font.gill_sans))
private val GillSansBold = FontFamily(Font(R.font.gill_sans_bold))

@Composable
fun MatterEditScreen(
    editModel: ViewMatterModel?,
    onNavigateBack: () -> Unit,
    onNavigateNext: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: MatterEditViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current

    // Initialize the ViewModel once
    LaunchedEffect(editModel) {
        viewModel.initialize(editModel)
    }

    // Handle navigation events
    LaunchedEffect(uiState.navigateToNext, uiState.navigateToView) {
        if (uiState.navigateToNext) {
            viewModel.consumeNavigationEvents()
            onNavigateNext()
        } else if (uiState.navigateToView) {
            viewModel.consumeNavigationEvents()
            onNavigateBack()
        }
    }

    // Helper for showing Custom Date Picker via dummy TextView
    val showDatePickerPicker = { currentVal: String, allowPast: Boolean, allowCurrent: Boolean, allowFuture: Boolean, onDatePicked: (String) -> Unit ->
        val dummy = TextView(context).apply { text = currentVal }
        AndroidUtils.showDatePicker(dummy, allowPast, allowCurrent, allowFuture) {
            onDatePicked(dummy.text.toString())
        }
    }

    // Handle error dialog using native showAlert
    LaunchedEffect(uiState.errorMessage) {
        if (uiState.errorMessage != null) {
            com.digicoffer.lauditor.CommonFiles.GlobalFiles.AndroidUtils.showAlert(
                uiState.errorMessage,
                context as? android.app.Activity,
                "Alert"
            ) {
                viewModel.clearErrorMessage()
            }
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(ColorTokens.LightBlueBg)
            .padding(10.dp)
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState()),
            shape = RoundedCornerShape(8.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp)
            ) {
                val isLegal = uiState.matterType == "Legal"

                // 1. Case Title
                FormLabel(text = "Case Title", isMandatory = true)
                FormTextField(
                    value = uiState.title,
                    onValueChange = { viewModel.onTitleChanged(it) },
                    placeholder = "Case Title"
                )

                Spacer(modifier = Modifier.height(10.dp))

                // 2. Case Number (Legal) or Matter Type (General)
                if (isLegal) {
                    FormLabel(text = "Case Number", isMandatory = false)
                    FormTextField(
                        value = uiState.caseNumber,
                        onValueChange = { viewModel.onCaseNumberChanged(it) },
                        placeholder = "Case Number"
                    )
                } else {
                    FormLabel(text = "Matter Type", isMandatory = false)
                    FormTextField(
                        value = uiState.selectedCaseType,
                        onValueChange = { viewModel.onCaseTypeChanged(it) },
                        placeholder = "Matter Type"
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                // 3. Matter Number (Auto-Generated)
                FormLabel(text = "Matter Number", isMandatory = true)
                Text(
                    text = "Auto-Generated Reference Number",
                    fontFamily = GillSans,
                    fontSize = 13.sp,
                    color = Color(0xFF2079AD),
                    modifier = Modifier.padding(bottom = 4.dp)
                )
                FormTextField(
                    value = uiState.matterNumber,
                    onValueChange = {},
                    placeholder = "",
                    enabled = false
                )

                Spacer(modifier = Modifier.height(10.dp))

                // 4. Created Date
                FormLabel(text = "Created Date", isMandatory = false)
                FormDateField(
                    value = uiState.createdDate,
                    onClick = {
                        showDatePickerPicker(uiState.createdDate, true, true, false) {
                            viewModel.onDateSelected("Created Date", it)
                        }
                    }
                )

                Spacer(modifier = Modifier.height(12.dp))

                // 5. Additional Details Toggle Link
                Text(
                    text = "Additional Details",
                    fontFamily = GillSans,
                    fontSize = 17.sp,
                    color = Color(0xFF2079AD),
                    textDecoration = TextDecoration.Underline,
                    modifier = Modifier
                        .clickable { viewModel.onAdditionalDetailsToggled() }
                        .padding(vertical = 6.dp)
                )

                // 6. Additional Details Expandable Section
                AnimatedVisibility(visible = uiState.isAdditionalDetailsExpanded) {
                    Column(modifier = Modifier.fillMaxWidth()) {
                        Spacer(modifier = Modifier.height(10.dp))

                        // Case Type Spinner (Legal only)
                        if (isLegal) {
                            FormLabel(text = "Case Type", isMandatory = false)
                            FormDropdownSelector(
                                options = uiState.caseTypeList,
                                selectedOption = uiState.selectedCaseType,
                                onOptionSelected = { viewModel.onCaseTypeSelected(it) },
                                placeholder = "Select Case Type"
                            )
                            Spacer(modifier = Modifier.height(10.dp))
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        // Description Field
                        FormLabel(text = "Description", isMandatory = false)
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .border(1.dp, Color(0xFFDDDDDE), RoundedCornerShape(4.dp))
                                .background(Color(0xFFFAFAFA))
                                .padding(8.dp)
                        ) {
                            BasicTextField(
                                value = uiState.description,
                                onValueChange = {
                                    if (it.length <= 300) viewModel.onDescriptionChanged(it)
                                },
                                textStyle = TextStyle(
                                    fontFamily = GillSans,
                                    fontSize = 15.sp,
                                    color = Color.Black
                                ),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(120.dp),
                                decorationBox = { innerTextField ->
                                    if (uiState.description.isEmpty()) {
                                        Text(
                                            text = "Description",
                                            fontFamily = GillSans,
                                            fontSize = 15.sp,
                                            color = Color.Gray
                                        )
                                    }
                                    innerTextField()
                                }
                            )
                            Text(
                                text = "${uiState.description.length}/300",
                                fontFamily = GillSans,
                                fontSize = 12.sp,
                                color = Color.Gray,
                                modifier = Modifier.align(Alignment.End)
                            )
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        // Matter Tags
                        FormLabel(text = "Matter Tags", isMandatory = false)
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .border(1.dp, Color(0xFFDDDDDE), RoundedCornerShape(4.dp))
                                    .background(Color(0xFFFAFAFA))
                                    .padding(horizontal = 10.dp, vertical = 8.dp)
                            ) {
                                BasicTextField(
                                    value = uiState.tagInput,
                                    onValueChange = {
                                        if (it.length <= 30) viewModel.onTagInputChanged(it)
                                    },
                                    textStyle = TextStyle(
                                        fontFamily = GillSans,
                                        fontSize = 15.sp,
                                        color = Color.Black
                                    ),
                                    modifier = Modifier.fillMaxWidth(),
                                    decorationBox = { innerTextField ->
                                        if (uiState.tagInput.isEmpty()) {
                                            Text(
                                                text = "Matter Tags",
                                                fontFamily = GillSans,
                                                fontSize = 15.sp,
                                                color = Color.Gray
                                            )
                                        }
                                        innerTextField()
                                    }
                                )
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                            Button(
                                onClick = { viewModel.onAddTagClicked() },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = ColorTokens.BluePrimary,
                                    contentColor = Color.White
                                ),
                                shape = RoundedCornerShape(4.dp),
                                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                                modifier = Modifier.height(38.dp)
                            ) {
                                Text(text = "Add", fontFamily = GillSansBold, fontSize = 14.sp)
                            }
                        }
                        Text(
                            text = "${uiState.tagInput.length}/30",
                            fontFamily = GillSans,
                            fontSize = 12.sp,
                            color = Color.Gray,
                            modifier = Modifier
                                .align(Alignment.Start)
                                .padding(top = 2.dp)
                        )

                        // Tags List Layout
                        if (uiState.tagsList.isNotEmpty()) {
                            Spacer(modifier = Modifier.height(6.dp))
                            FlowRow(
                                modifier = Modifier.fillMaxWidth(),
                                mainAxisSpacing = 8.dp,
                                crossAxisSpacing = 8.dp
                            ) {
                                uiState.tagsList.forEachIndexed { index, tag ->
                                    Row(
                                        modifier = Modifier
                                            .background(Color(0xFFEEEEEE), RoundedCornerShape(8.dp))
                                            .border(1.dp, Color(0xFFDDDDDE), RoundedCornerShape(8.dp))
                                            .padding(horizontal = 12.dp, vertical = 8.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = tag,
                                            fontFamily = GillSans,
                                            fontSize = 14.sp,
                                            color = Color.Black
                                        )
                                        Spacer(modifier = Modifier.width(10.dp))
                                        Icon(
                                            painter = painterResource(id = R.drawable.edit__icon),
                                            contentDescription = "Edit Tag",
                                            tint = Color.Unspecified,
                                            modifier = Modifier
                                                .size(18.dp)
                                                .clickable {
                                                    viewModel.onEditTagClicked(index)
                                                }
                                        )
                                        Spacer(modifier = Modifier.width(10.dp))
                                        Icon(
                                            painter = painterResource(id = R.drawable.cancel_red_icon),
                                            contentDescription = "Remove Tag",
                                            tint = Color.Unspecified,
                                            modifier = Modifier
                                                .size(18.dp)
                                                .clickable {
                                                    viewModel.onRemoveTagClicked(index)
                                                }
                                        )
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        if (isLegal) {
                            // Date of Filing (Legal only)
                            FormLabel(text = "Date of Filing", isMandatory = false)
                            FormDateField(
                                value = uiState.dateOfFiling,
                                onClick = {
                                    showDatePickerPicker(uiState.dateOfFiling, true, true, true) {
                                        viewModel.onDateSelected("Date of Filing", it)
                                    }
                                },
                                placeholder = "Date of Filing"
                            )

                            Spacer(modifier = Modifier.height(10.dp))

                            // Court Name
                            FormLabel(text = "Court", isMandatory = false)
                            FormTextFieldWithIcon(
                                value = uiState.courtName,
                                onValueChange = { viewModel.onCourtChanged(it) },
                                placeholder = "Court",
                                iconRes = R.drawable.court_new
                            )

                            Spacer(modifier = Modifier.height(10.dp))

                            // Judges
                            FormLabel(text = "Judge(s)", isMandatory = false)
                            FormTextFieldWithIcon(
                                value = uiState.judges,
                                onValueChange = { viewModel.onJudgesChanged(it) },
                                placeholder = "Judge(s)",
                                iconRes = R.drawable.judge_new_icon
                            )
                        } else {
                            // Start & Close Dates (General only)
                            Row(modifier = Modifier.fillMaxWidth()) {
                                Column(modifier = Modifier.weight(1f)) {
                                    FormLabel(text = "Start Date", isMandatory = false)
                                    FormDateField(
                                        value = uiState.startDate,
                                        onClick = {
                                            showDatePickerPicker(uiState.startDate, true, true, true) {
                                                viewModel.onDateSelected("Start Date", it)
                                            }
                                        },
                                        placeholder = "Start Date"
                                    )
                                }
                                Spacer(modifier = Modifier.width(10.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    FormLabel(text = "Close Date", isMandatory = false)
                                    FormDateField(
                                        value = uiState.endDate,
                                        onClick = {
                                            showDatePickerPicker(uiState.endDate, true, true, true) {
                                                viewModel.onDateSelected("Close Date", it)
                                            }
                                        },
                                        placeholder = "Close Date"
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        // Priority Row
                        FormLabel(text = "Priority", isMandatory = false)
                        SegmentedPrioritySelector(
                            selectedPriority = uiState.priority,
                            onPrioritySelected = { viewModel.onPrioritySelected(it) }
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        // Status Row
                        FormLabel(text = "Status", isMandatory = false)
                        SegmentedStatusSelector(
                            selectedStatus = uiState.status,
                            onStatusSelected = { viewModel.onStatusSelected(it) }
                        )

                        Spacer(modifier = Modifier.height(15.dp))

                        // 7. Opponent Advocate Section (Legal only)
                        if (isLegal) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "Opponent Advocate(s)",
                                    fontFamily = GillSans,
                                    fontSize = 17.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = ColorTokens.BluePrimary
                                )
                                Button(
                                    onClick = { viewModel.onAddAdvocateClicked() },
                                    colors = ButtonDefaults.buttonColors(containerColor = ColorTokens.BluePrimary),
                                    shape = RoundedCornerShape(4.dp),
                                    contentPadding = PaddingValues(horizontal = 24.dp, vertical = 6.dp),
                                    modifier = Modifier.height(35.dp)
                                ) {
                                    Text(text = "Add", fontFamily = GillSansBold, fontSize = 14.sp, color = Color.White)
                                }
                            }

                            Spacer(modifier = Modifier.height(8.dp))

                            // Advocate Sub-Form Card
                            if (uiState.isAdvocateFormVisible) {
                                Card(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 6.dp),
                                    shape = RoundedCornerShape(6.dp),
                                    colors = CardDefaults.cardColors(containerColor = Color(0xFFFAFAFA)),
                                    border = BorderStroke(1.dp, Color(0xFFDDDDDE))
                                ) {
                                    Column(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(10.dp)
                                    ) {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Text(
                                                text = "Add Advocate Details",
                                                fontFamily = GillSansBold,
                                                fontSize = 15.sp,
                                                color = Color.Black
                                            )
                                            Image(
                                                painter = painterResource(id = R.drawable.cancel_red_icon),
                                                contentDescription = "Cancel",
                                                modifier = Modifier
                                                    .size(22.dp)
                                                    .clickable { viewModel.onCancelAdvocateClicked() }
                                            )
                                        }

                                        Spacer(modifier = Modifier.height(8.dp))

                                        FormLabel(text = "Name", isMandatory = false)
                                        FormTextField(
                                            value = uiState.advocateName,
                                            onValueChange = { viewModel.onAdvocateFormChanged(it, uiState.advocateEmail, uiState.advocatePhone) },
                                            placeholder = "Name"
                                        )

                                        Spacer(modifier = Modifier.height(8.dp))

                                        FormLabel(text = "Email ID", isMandatory = false)
                                        FormTextField(
                                            value = uiState.advocateEmail,
                                            onValueChange = { viewModel.onAdvocateFormChanged(uiState.advocateName, it, uiState.advocatePhone) },
                                            placeholder = "Email ID"
                                        )
                                        uiState.advocateEmailError?.let { errorText ->
                                            Text(
                                                text = errorText,
                                                color = Color.Red,
                                                fontSize = 12.sp,
                                                fontFamily = GillSans,
                                                modifier = Modifier.padding(start = 2.dp, top = 2.dp)
                                            )
                                        }

                                        Spacer(modifier = Modifier.height(8.dp))

                                        FormLabel(text = "Phone Number", isMandatory = false)
                                        FormTextField(
                                            value = uiState.advocatePhone,
                                            onValueChange = {
                                                if (it.length <= 10 && it.all { c -> c.isDigit() }) {
                                                    viewModel.onAdvocateFormChanged(uiState.advocateName, uiState.advocateEmail, it)
                                                }
                                            },
                                            placeholder = "Phone Number",
                                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                                        )
                                        uiState.advocatePhoneError?.let { errorText ->
                                            Text(
                                                text = errorText,
                                                color = Color.Red,
                                                fontSize = 12.sp,
                                                fontFamily = GillSans,
                                                modifier = Modifier.padding(start = 2.dp, top = 2.dp)
                                            )
                                        }

                                        Spacer(modifier = Modifier.height(12.dp))

                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.Center
                                        ) {
                                            Button(
                                                onClick = { viewModel.onSaveAdvocateClicked() },
                                                colors = ButtonDefaults.buttonColors(containerColor = ColorTokens.BluePrimary),
                                                shape = RoundedCornerShape(4.dp),
                                                modifier = Modifier
                                                    .width(100.dp)
                                                    .height(36.dp)
                                            ) {
                                                Text(text = "Save", fontFamily = GillSansBold, fontSize = 13.sp, color = Color.White)
                                            }
                                        }
                                    }
                                }
                            }

                            // Listed Advocates (Card with name only, edit pencil, and red close icon)
                            if (uiState.advocatesList.isNotEmpty()) {
                                Column(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    uiState.advocatesList.forEachIndexed { index, adv ->
                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .background(Color(0xFFEEEEEE), RoundedCornerShape(8.dp))
                                                .border(1.dp, Color(0xFFDDDDDE), RoundedCornerShape(8.dp))
                                                .padding(horizontal = 12.dp, vertical = 10.dp),
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.SpaceBetween
                                        ) {
                                            Text(
                                                text = adv.advocate_name ?: "",
                                                fontFamily = GillSans,
                                                fontSize = 14.sp,
                                                color = Color.Black,
                                                modifier = Modifier.weight(1f)
                                            )
                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                Icon(
                                                    painter = painterResource(id = R.drawable.edit__icon),
                                                    contentDescription = "Edit Advocate",
                                                    tint = Color.Unspecified,
                                                    modifier = Modifier
                                                        .size(18.dp)
                                                        .clickable { viewModel.onEditAdvocateClicked(index) }
                                                )
                                                Spacer(modifier = Modifier.width(12.dp))
                                                Icon(
                                                    painter = painterResource(id = R.drawable.cancel_red_icon),
                                                    contentDescription = "Remove Advocate",
                                                    tint = Color.Unspecified,
                                                    modifier = Modifier
                                                        .size(18.dp)
                                                        .clickable { viewModel.onRemoveAdvocateClicked(index) }
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }



                Spacer(modifier = Modifier.height(20.dp))

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 10.dp, bottom = 10.dp),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Button(
                        onClick = { viewModel.submitForm(isSaveLater = true, editModel = editModel) },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFFE8E8E8),
                            contentColor = Color.Black
                        ),
                        border = BorderStroke(1.dp, Color(0xFF888888)),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier
                            .width(135.dp)
                            .height(40.dp),
                        contentPadding = PaddingValues(0.dp)
                    ) {
                        Text(
                            text = "Save For Later",
                            fontFamily = GillSans,
                            fontSize = 13.sp,
                            color = Color.Black
                        )
                    }
                    Spacer(modifier = Modifier.width(20.dp))
                    Button(
                        onClick = { viewModel.submitForm(isSaveLater = false, editModel = editModel) },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = ColorTokens.BluePrimary,
                            contentColor = Color.White
                        ),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier
                            .width(135.dp)
                            .height(40.dp),
                        contentPadding = PaddingValues(0.dp)
                    ) {
                        Text(
                            text = "Save & Next",
                            fontFamily = GillSansBold,
                            fontSize = 13.sp,
                            color = Color.White
                        )
                    }
                }
            }
        }
        if (uiState.isLoading) {
            AppLoader()
        }

        // In-App Toast with App Logo
        com.digicoffer.lauditor.core.ui.common.feedback.AppToast(
            message = uiState.toastMessage,
            onDismiss = { viewModel.clearToastMessage() }
        )
    }
}

@Composable
fun FormLabel(text: String, isMandatory: Boolean) {
    val annotatedString = buildAnnotatedString {
        withStyle(style = SpanStyle(fontFamily = GillSans, fontSize = 15.sp, color = ColorTokens.BluePrimary)) {
            append(text)
        }
        if (isMandatory) {
            withStyle(style = SpanStyle(color = Color.Red, fontSize = 15.sp)) {
                append(" *")
            }
        }
    }
    Text(
        text = annotatedString,
        modifier = Modifier.padding(bottom = 4.dp, top = 6.dp)
    )
}

@Composable
fun FormTextField(
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    enabled: Boolean = true,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(42.dp)
            .border(1.dp, Color(0xFFDDDDDE), RoundedCornerShape(4.dp))
            .background(if (enabled) Color(0xFFFAFAFA) else Color(0xFFEEEEEE))
            .padding(horizontal = 10.dp),
        contentAlignment = Alignment.CenterStart
    ) {
        BasicTextField(
            value = value,
            onValueChange = onValueChange,
            enabled = enabled,
            singleLine = true,
            keyboardOptions = keyboardOptions,
            textStyle = TextStyle(
                fontFamily = GillSans,
                fontSize = 15.sp,
                color = if (enabled) Color.Black else Color.Gray
            ),
            modifier = Modifier.fillMaxWidth(),
            decorationBox = { innerTextField ->
                if (value.isEmpty()) {
                    Text(
                        text = placeholder,
                        fontFamily = GillSans,
                        fontSize = 15.sp,
                        color = Color.Gray
                    )
                }
                innerTextField()
            }
        )
    }
}

@Composable
fun FormDateField(
    value: String,
    onClick: () -> Unit,
    placeholder: String = ""
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(42.dp)
            .border(1.dp, Color(0xFFDDDDDE), RoundedCornerShape(4.dp))
            .background(Color(0xFFFAFAFA))
            .clickable { onClick() }
            .padding(horizontal = 10.dp),
        contentAlignment = Alignment.CenterStart
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = value.ifEmpty { placeholder },
                fontFamily = GillSans,
                fontSize = 15.sp,
                color = if (value.isEmpty()) Color.Gray else Color.Black
            )
            Icon(
                painter = painterResource(id = R.drawable.calendar_icon_xsmall),
                contentDescription = "Select Date",
                tint = Color.Black,
                modifier = Modifier.size(20.dp)
            )
        }
    }
}

@Composable
fun FormTextFieldWithIcon(
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    iconRes: Int
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(42.dp)
            .border(1.dp, Color(0xFFDDDDDE), RoundedCornerShape(4.dp))
            .background(Color(0xFFFAFAFA))
            .padding(horizontal = 10.dp),
        contentAlignment = Alignment.CenterStart
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            BasicTextField(
                value = value,
                onValueChange = onValueChange,
                singleLine = true,
                textStyle = TextStyle(
                    fontFamily = GillSans,
                    fontSize = 15.sp,
                    color = Color.Black
                ),
                modifier = Modifier.weight(1f),
                decorationBox = { innerTextField ->
                    if (value.isEmpty()) {
                        Text(
                            text = placeholder,
                            fontFamily = GillSans,
                            fontSize = 15.sp,
                            color = Color.Gray
                        )
                    }
                    innerTextField()
                }
            )
            Spacer(modifier = Modifier.width(6.dp))
            Icon(
                painter = painterResource(id = iconRes),
                contentDescription = placeholder,
                tint = Color.Gray,
                modifier = Modifier.size(22.dp)
            )
        }
    }
}

@Composable
fun FormDropdownSelector(
    options: List<String>,
    selectedOption: String,
    onOptionSelected: (String) -> Unit,
    placeholder: String
) {
    var expanded by remember { mutableStateOf(false) }

    Column(modifier = Modifier.fillMaxWidth()) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(42.dp)
                .border(1.dp, Color(0xFFDDDDDE), RoundedCornerShape(4.dp))
                .background(Color(0xFFFAFAFA))
                .clickable { expanded = !expanded }
                .padding(horizontal = 10.dp),
            contentAlignment = Alignment.CenterStart
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = selectedOption.ifEmpty { placeholder },
                    fontFamily = GillSans,
                    fontSize = 15.sp,
                    color = if (selectedOption.isEmpty()) Color.Gray else Color.Black
                )
                Icon(
                    painter = painterResource(id = R.drawable.drop_down_blue),
                    contentDescription = "Dropdown",
                    tint = ColorTokens.BluePrimary,
                    modifier = Modifier.size(20.dp)
                )
            }
        }

        if (expanded) {
            Spacer(modifier = Modifier.height(2.dp))
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 200.dp)
                    .wrapContentHeight()
                    .border(1.dp, Color(0xFFDDDDDE), RoundedCornerShape(4.dp)),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                shape = RoundedCornerShape(4.dp)
            ) {
                if (options.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "No options available",
                            fontFamily = GillSans,
                            fontSize = 14.sp,
                            color = Color.Gray
                        )
                    }
                } else {
                    val localView = androidx.compose.ui.platform.LocalView.current
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .verticalScroll(rememberScrollState())
                            .pointerInput(Unit) {
                                awaitPointerEventScope {
                                    while (true) {
                                        awaitPointerEvent(androidx.compose.ui.input.pointer.PointerEventPass.Initial)
                                        var p = localView.parent
                                        while (p != null) {
                                            p.requestDisallowInterceptTouchEvent(true)
                                            p = p.parent
                                        }
                                    }
                                }
                            }
                    ) {
                        options.forEach { option ->
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        onOptionSelected(option)
                                        expanded = false
                                    }
                                    .border(0.5.dp, Color(0xFFF2F2F2))
                                    .padding(horizontal = 12.dp, vertical = 10.dp),
                                contentAlignment = Alignment.CenterStart
                            ) {
                                Text(
                                    text = option,
                                    fontFamily = GillSans,
                                    fontSize = 15.sp,
                                    color = Color.Black
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun SegmentedPrioritySelector(
    selectedPriority: String,
    onPrioritySelected: (String) -> Unit
) {
    Row(
        modifier = Modifier
            .wrapContentWidth()
            .height(32.dp)
            .border(1.dp, Color(0xFFDDDDDE), RoundedCornerShape(16.dp))
    ) {
        val priorities = listOf("High", "Medium", "Low")
        priorities.forEachIndexed { index, priority ->
            val isSelected = selectedPriority.equals(priority, ignoreCase = true)
            val isFirst = index == 0
            val isLast = index == priorities.lastIndex

            val shape = when {
                isFirst -> RoundedCornerShape(topStart = 16.dp, bottomStart = 16.dp)
                isLast -> RoundedCornerShape(topEnd = 16.dp, bottomEnd = 16.dp)
                else -> RoundedCornerShape(0.dp)
            }

            Box(
                modifier = Modifier
                    .width(60.dp)
                    .fillMaxHeight()
                    .background(
                        if (isSelected) ColorTokens.BluePrimary else Color(0xFFEEEEEE),
                        shape = shape
                    )
                    .clickable { onPrioritySelected(priority) },
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = priority,
                    fontFamily = if (isSelected) GillSansBold else GillSans,
                    fontSize = 13.sp,
                    color = if (isSelected) Color.White else Color.Black
                )
            }

            if (index < priorities.size - 1) {
                Box(
                    modifier = Modifier
                        .width(1.dp)
                        .fillMaxHeight()
                        .background(Color(0xFFDDDDDE))
                )
            }
        }
    }
}

@Composable
fun SegmentedStatusSelector(
    selectedStatus: String,
    onStatusSelected: (String) -> Unit
) {
    Row(
        modifier = Modifier
            .wrapContentWidth()
            .height(32.dp)
            .border(1.dp, Color(0xFFDDDDDE), RoundedCornerShape(16.dp))
    ) {
        val statuses = listOf("Active", "Pending")
        statuses.forEachIndexed { index, status ->
            val isSelected = selectedStatus.equals(status, ignoreCase = true)
            val isFirst = index == 0
            val isLast = index == statuses.lastIndex

            val shape = when {
                isFirst -> RoundedCornerShape(topStart = 16.dp, bottomStart = 16.dp)
                isLast -> RoundedCornerShape(topEnd = 16.dp, bottomEnd = 16.dp)
                else -> RoundedCornerShape(0.dp)
            }

            Box(
                modifier = Modifier
                    .width(70.dp)
                    .fillMaxHeight()
                    .background(
                        if (isSelected) ColorTokens.BluePrimary else Color(0xFFEEEEEE),
                        shape = shape
                    )
                    .clickable { onStatusSelected(status) },
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = status,
                    fontFamily = if (isSelected) GillSansBold else GillSans,
                    fontSize = 13.sp,
                    color = if (isSelected) Color.White else Color.Black
                )
            }

            if (index < statuses.size - 1) {
                Box(
                    modifier = Modifier
                        .width(1.dp)
                        .fillMaxHeight()
                        .background(Color(0xFFDDDDDE))
                )
            }
        }
    }
}

@Composable
fun FlowRow(
    modifier: Modifier = Modifier,
    mainAxisSpacing: androidx.compose.ui.unit.Dp = 0.dp,
    crossAxisSpacing: androidx.compose.ui.unit.Dp = 0.dp,
    content: @Composable () -> Unit
) {
    androidx.compose.ui.layout.Layout(
        content = content,
        modifier = modifier
    ) { measurables, constraints ->
        val mainAxisSpacingPx = mainAxisSpacing.roundToPx()
        val crossAxisSpacingPx = crossAxisSpacing.roundToPx()

        val rows = mutableListOf<List<androidx.compose.ui.layout.Placeable>>()
        val rowHeights = mutableListOf<Int>()
        var currentRow = mutableListOf<androidx.compose.ui.layout.Placeable>()
        var currentRowWidth = 0
        var currentRowHeight = 0

        measurables.forEach { measurable ->
            val placeable = measurable.measure(constraints.copy(minWidth = 0))
            if (currentRowWidth + placeable.width > constraints.maxWidth && currentRow.isNotEmpty()) {
                rows.add(currentRow)
                rowHeights.add(currentRowHeight)
                currentRow = mutableListOf()
                currentRowWidth = 0
                currentRowHeight = 0
            }
            currentRow.add(placeable)
            currentRowWidth += placeable.width + mainAxisSpacingPx
            currentRowHeight = maxOf(currentRowHeight, placeable.height)
        }
        if (currentRow.isNotEmpty()) {
            rows.add(currentRow)
            rowHeights.add(currentRowHeight)
        }

        val totalHeight = rowHeights.sum() + (rowHeights.size - 1).coerceAtLeast(0) * crossAxisSpacingPx
        val totalWidth = constraints.maxWidth

        layout(totalWidth, totalHeight) {
            var y = 0
            rows.forEachIndexed { rowIndex, row ->
                var x = 0
                row.forEach { placeable ->
                    placeable.placeRelative(x, y)
                    x += placeable.width + mainAxisSpacingPx
                }
                y += rowHeights[rowIndex] + crossAxisSpacingPx
            }
        }
    }
}
