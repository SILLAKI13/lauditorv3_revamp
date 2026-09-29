package com.digicoffer.lauditor.feature.profile.presentation.components

import android.app.TimePickerDialog
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.digicoffer.lauditor.CommonFiles.GlobalFiles.Constants
import com.digicoffer.lauditor.R
import com.digicoffer.lauditor.core.ui.common.foundation.AppSpacer
import com.digicoffer.lauditor.core.ui.common.inputs.AppCheckbox
import com.digicoffer.lauditor.feature.profile.presentation.state.*
import java.text.SimpleDateFormat
import java.util.*

private val GillSans = FontFamily(Font(R.font.gill_sans))
private val GillSansBold = FontFamily(Font(R.font.gill_sans_bold))

@Composable
fun EditAdditionalInfoCard(
    isMyProfile: Boolean,
    selectedSubTab: String,
    practiceForm: PracticeDetailsEditState,
    courtsForm: CourtsAndCasesEditState,
    eduForm: EducationAndAwardsEditState,
    availForm: AvailabilityEditState,
    allPracticeAreas: List<String>,
    allServices: List<String> = emptyList(),
    suggestedServices: List<String>,
    searchedServices: List<String>,
    allCaseTypes: List<String>,
    courtStatesList: List<String>,
    courtStatesMap: Map<String, List<String>>,
    suggestedCourtTypes: List<String>,
    allCourtTypes: List<String> = emptyList(),
    countriesList: List<String>,
    currencyList: List<String>,
    statesList: List<String> = emptyList(),
    stateCitiesMap: Map<String, List<String>> = emptyMap(),
    onEvent: (ProfileUiEvent) -> Unit,
    modifier: Modifier = Modifier
) {
    val isFirmProfile = !isMyProfile
    val isAAM = Constants.ROLE.equals("AAM", ignoreCase = true)

    val availableTabs = remember(isFirmProfile, isAAM) {
        if (isFirmProfile || isAAM) {
            listOf(
                "practice_details" to "Practice\nDetails",
                "education_awards" to "Awards &\nRecognition"
            )
        } else {
            listOf(
                "practice_details" to "Practice\nDetails",
                "courts_cases" to "Courts\n& Cases",
                "education_awards" to "Education\n& Awards",
                "availability" to "Set\nAvailability"
            )
        }
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
                    text = "Additional Information",
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
                        .clickable { onEvent(ProfileUiEvent.CancelEditAddInfo) }
                )
            }

            AppSpacer(height = 14.dp)

            // ── Sub-Tab Rounded Pill Row ───────────────────────────────────────
            ProfileTabPillRow(
                tabs = availableTabs,
                selectedTab = selectedSubTab,
                onTabSelected = { onEvent(ProfileUiEvent.SelectEditSubTab(it)) }
            )

            AppSpacer(height = 12.dp)

            // Divider under tabs matching Java XML
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(1.dp)
                    .background(Color(0xFFCCCCCC))
            )

            AppSpacer(height = 14.dp)

            // ── Sub-Tab Content ────────────────────────────────────────────────
            when (selectedSubTab) {
                "practice_details" -> {
                    PracticeDetailsEditSection(
                        isMyProfile = isMyProfile,
                        state = practiceForm,
                        allPracticeAreas = allPracticeAreas,
                        allServices = allServices,
                        suggestedServices = suggestedServices,
                        searchedServices = searchedServices,
                        countriesList = countriesList,
                        currencyList = currencyList,
                        statesList = statesList,
                        stateCitiesMap = stateCitiesMap,
                        courtStatesList = courtStatesList,
                        courtStatesMap = courtStatesMap,
                        onEvent = onEvent
                    )
                }
                "courts_cases" -> {
                    CourtsAndCasesEditSection(
                        state = courtsForm,
                        allCaseTypes = allCaseTypes,
                        courtStatesList = courtStatesList,
                        courtStatesMap = courtStatesMap,
                        suggestedCourtTypes = suggestedCourtTypes,
                        allCourtTypes = allCourtTypes,
                        onEvent = onEvent
                    )
                }
                "education_awards" -> {
                    EducationAndAwardsEditSection(
                        isFirmProfile = isFirmProfile,
                        state = eduForm,
                        onEvent = onEvent
                    )
                }
                "availability" -> {
                    SetAvailabilityEditSection(
                        state = availForm,
                        onEvent = onEvent
                    )
                }
            }

            AppSpacer(height = 16.dp)

            // ── Bottom Action Buttons ──────────────────────────────────────────
            ProfileActionButtons(
                onCancel = { onEvent(ProfileUiEvent.CancelEditAddInfo) },
                onSave = { onEvent(ProfileUiEvent.SaveEditAddInfo) }
            )
        }
    }
}

// ═════════════════════════════════════════════════════════════════════════════
// 1. PRACTICE DETAILS EDIT SECTION
// ═════════════════════════════════════════════════════════════════════════════
@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun PracticeDetailsEditSection(
    isMyProfile: Boolean,
    state: PracticeDetailsEditState,
    allPracticeAreas: List<String>,
    allServices: List<String>,
    suggestedServices: List<String>,
    searchedServices: List<String>,
    countriesList: List<String>,
    currencyList: List<String>,
    statesList: List<String> = emptyList(),
    stateCitiesMap: Map<String, List<String>> = emptyMap(),
    courtStatesList: List<String> = emptyList(),
    courtStatesMap: Map<String, List<String>> = emptyMap(),
    onEvent: (ProfileUiEvent) -> Unit
) {
    var newServiceText by remember { mutableStateOf("") }
    var newLanguageText by remember { mutableStateOf("") }
    var isServicesDropdownOpen by remember { mutableStateOf(false) }

    Column(modifier = Modifier.fillMaxWidth()) {
        // ── Professional Information Header ────────────────────────────────────
        Text(
            text = "Professional Information",
            color = ProfileNavy,
            fontSize = 15.sp,
            fontFamily = GillSansBold,
            modifier = Modifier.padding(bottom = 10.dp)
        )

        // Bar Council ID / Registration ID
        ProfileFieldWithLabel(
            label = if (isMyProfile) "Bar Council ID" else "Registration Id",
            value = state.barOrRegId,
            onValueChange = { onEvent(ProfileUiEvent.UpdateBarOrRegId(it)) },
            placeholder = if (isMyProfile) "Enter Bar Council ID" else "Enter Registration ID",
            isRequired = false,
            modifier = Modifier.fillMaxWidth()
        )

        AppSpacer(height = 12.dp)

        // Years of Experience / Year of Incorporation
        ProfileFieldWithLabel(
            label = if (isMyProfile) "Years of Experience" else "Year of Incorporation",
            value = state.yearsOfExperience,
            onValueChange = { onEvent(ProfileUiEvent.UpdateYearsOfExperience(it)) },
            placeholder = if (isMyProfile) "Enter years of experience" else "Enter year of incorporation",
            isRequired = false,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            modifier = Modifier.fillMaxWidth()
        )

        AppSpacer(height = 12.dp)

        // Billing Currency
        ProfileFieldWithLabel(
            label = "Billing Currency",
            value = state.billingCurrency.ifEmpty { "IndianRupee(INR)" },
            onValueChange = { },
            enabled = false,
            isRequired = true,
            trailingIcon = {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = "Clear",
                    tint = Color(0xFF6B7280),
                    modifier = Modifier.size(18.dp)
                )
            },
            modifier = Modifier.fillMaxWidth()
        )

        // Consultation Fee (My Profile only)
        if (isMyProfile) {
            AppSpacer(height = 12.dp)
            ProfileConsultationFeeStepper(
                fee = state.consultationFee,
                onFeeChange = { onEvent(ProfileUiEvent.UpdateConsultationFee(it)) },
                modifier = Modifier.fillMaxWidth()
            )
        }

        AppSpacer(height = 14.dp)

        // ── Practice Areas ─────────────────────────────────────────────────────
        ProfileMultiSelectDropdown(
            label = "Practice Areas",
            options = allPracticeAreas,
            selectedOptions = state.practiceAreas,
            onOptionToggled = { area ->
                if (state.practiceAreas.contains(area)) {
                    onEvent(ProfileUiEvent.RemovePracticeArea(area))
                } else {
                    onEvent(ProfileUiEvent.AddPracticeArea(area))
                }
            },
            placeholder = "Select Practice Areas",
            isRequired = true,
            modifier = Modifier.fillMaxWidth()
        )

        AppSpacer(height = 14.dp)

        // ── Services Offered ───────────────────────────────────────────────────
        val combinedServicesList = remember(suggestedServices, allServices, searchedServices, newServiceText) {
            val result = linkedSetOf<String>()
            val query = newServiceText.trim()

            if (query.isEmpty()) {
                // 1. Suggested services first (in original API order)
                suggestedServices.forEach { if (it.isNotBlank()) result.add(it) }
                // 2. Remaining services from allServices sorted alphabetically
                val rest = allServices.filter { !suggestedServices.contains(it) && it.isNotBlank() }.sorted()
                rest.forEach { result.add(it) }
                // 3. Any already selected services
                state.servicesOffered.forEach { if (it.isNotBlank()) result.add(it) }
            } else {
                // Search query active:
                // 1. Matching suggested services first
                suggestedServices.filter { it.contains(query, ignoreCase = true) }.forEach { result.add(it) }
                // 2. Matching services from allServices
                allServices.filter { it.contains(query, ignoreCase = true) }.sorted().forEach { result.add(it) }
                // 3. Matching services from API searchedServices
                searchedServices.filter { it.contains(query, ignoreCase = true) }.forEach { result.add(it) }
                // 4. Any already selected matching services
                state.servicesOffered.filter { it.contains(query, ignoreCase = true) }.forEach { result.add(it) }
            }
            result.toList()
        }

        ProfileFieldWithLabel(
            label = "Services Offered",
            value = newServiceText,
            onValueChange = {
                newServiceText = it
                isServicesDropdownOpen = true
                onEvent(ProfileUiEvent.SearchServices(it))
            },
            placeholder = "Search & Add Services",
            isRequired = false,
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
            keyboardActions = KeyboardActions(
                onDone = {
                    if (newServiceText.isNotBlank()) {
                        onEvent(ProfileUiEvent.AddService(newServiceText.trim()))
                        newServiceText = ""
                    }
                }
            ),
            onClick = { isServicesDropdownOpen = true },
            onFocusChanged = { if (it) isServicesDropdownOpen = true },
            trailingIcon = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (newServiceText.isNotBlank()) {
                        Text(
                            text = "+ Add",
                            fontSize = 12.sp,
                            fontFamily = GillSansBold,
                            color = ProfileNavy,
                            modifier = Modifier
                                .clickable {
                                    onEvent(ProfileUiEvent.AddService(newServiceText.trim()))
                                    newServiceText = ""
                                }
                                .padding(horizontal = 4.dp, vertical = 2.dp)
                        )
                    }
                    Image(
                        painter = painterResource(R.drawable.drop_down_blue),
                        contentDescription = "Dropdown",
                        modifier = Modifier
                            .size(20.dp)
                            .rotate(if (isServicesDropdownOpen) 180f else 0f)
                            .clickable { isServicesDropdownOpen = !isServicesDropdownOpen }
                    )
                }
            },
            modifier = Modifier.fillMaxWidth()
        )

        // Dropdown card with services checklist when open
        if (isServicesDropdownOpen) {
            AppSpacer(height = 6.dp)

            if (suggestedServices.isNotEmpty()) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(start = 2.dp, top = 4.dp, bottom = 4.dp)
                ) {
                    Icon(
                        painter = painterResource(R.drawable.suggest_magic_icon),
                        contentDescription = "Recommended",
                        tint = ProfileNavy,
                        modifier = Modifier.size(15.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Recommended for your selected practice areas",
                        fontSize = 12.sp,
                        fontFamily = GillSans,
                        color = ProfileNavy
                    )
                }
            }

            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 240.dp),
                shape = RoundedCornerShape(8.dp),
                color = Color.White,
                border = BorderStroke(0.8.dp, Color(0xFFD1D5DB)),
                shadowElevation = 3.dp
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .verticalScroll(rememberScrollState())
                ) {
                    if (combinedServicesList.isEmpty() && newServiceText.isBlank()) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "No services available",
                                fontSize = 12.5.sp,
                                fontFamily = GillSans,
                                color = Color(0xFF6B7280)
                            )
                        }
                    } else {
                        combinedServicesList.forEachIndexed { index, srv ->
                            val isSelected = state.servicesOffered.contains(srv)
                            val isSuggested = suggestedServices.contains(srv)

                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        if (isSelected) {
                                            onEvent(ProfileUiEvent.RemoveService(srv))
                                        } else {
                                            onEvent(ProfileUiEvent.AddService(srv))
                                        }
                                    }
                                    .padding(horizontal = 14.dp, vertical = 9.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = srv,
                                    fontSize = 13.sp,
                                    fontFamily = GillSans,
                                    color = if (isSelected) ProfileNavy else Color(0xFF1F2937),
                                    modifier = Modifier.weight(1f)
                                )

                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    if (isSuggested) {
                                        Icon(
                                            painter = painterResource(R.drawable.suggest_magic_icon),
                                            contentDescription = "Recommended",
                                            tint = ProfileNavy,
                                            modifier = Modifier
                                                .padding(end = 8.dp)
                                                .size(16.dp)
                                        )
                                    }

                                    Checkbox(
                                        checked = isSelected,
                                        onCheckedChange = { checked ->
                                            if (checked) {
                                                onEvent(ProfileUiEvent.AddService(srv))
                                            } else {
                                                onEvent(ProfileUiEvent.RemoveService(srv))
                                            }
                                        },
                                        colors = CheckboxDefaults.colors(
                                            checkedColor = ProfileNavy,
                                            uncheckedColor = Color(0xFF9CA3AF)
                                        ),
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                            }

                            if (index < combinedServicesList.size - 1) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(0.6.dp)
                                        .background(Color(0xFFF3F4F6))
                                )
                            }
                        }

                        if (newServiceText.isNotBlank() && !combinedServicesList.any { it.equals(newServiceText.trim(), ignoreCase = true) }) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(0.6.dp)
                                    .background(Color(0xFFE5E7EB))
                            )
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        onEvent(ProfileUiEvent.AddService(newServiceText.trim()))
                                        newServiceText = ""
                                    }
                                    .padding(horizontal = 14.dp, vertical = 10.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "+ Add \"${newServiceText.trim()}\"",
                                    fontSize = 13.sp,
                                    fontFamily = GillSansBold,
                                    color = ProfileNavy
                                )
                            }
                        }
                    }
                }
            }
        }

        // Pale blue selected services chips
        if (state.servicesOffered.isNotEmpty()) {
            AppSpacer(height = 8.dp)
            FlowRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                state.servicesOffered.forEach { srv ->
                    ProfilePaleBlueChip(
                        text = srv,
                        onRemove = { onEvent(ProfileUiEvent.RemoveService(srv)) }
                    )
                }
            }
        }

        // ── Languages Spoken (My Profile only) ──────────────────────────────────
        if (isMyProfile) {
            AppSpacer(height = 14.dp)

            ProfileFieldWithLabel(
                label = "Languages Spoken",
                value = newLanguageText,
                onValueChange = { newLanguageText = it },
                placeholder = "Enter Language",
                isRequired = false,
                trailingIcon = {
                    if (newLanguageText.isNotBlank()) {
                        Text(
                            text = "+ Add",
                            fontSize = 12.sp,
                            fontFamily = GillSansBold,
                            color = ProfileNavy,
                            modifier = Modifier
                                .clickable {
                                    onEvent(ProfileUiEvent.AddLanguage(newLanguageText.trim().lowercase(Locale.ROOT)))
                                    newLanguageText = ""
                                }
                                .padding(horizontal = 4.dp, vertical = 2.dp)
                        )
                    }
                },
                modifier = Modifier.fillMaxWidth()
            )

            // Pale blue selected languages chips
            if (state.languagesSpoken.isNotEmpty()) {
                AppSpacer(height = 8.dp)
                FlowRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    state.languagesSpoken.forEach { lang ->
                        ProfilePaleBlueChip(
                            text = lang,
                            onRemove = { onEvent(ProfileUiEvent.RemoveLanguage(lang)) }
                        )
                    }
                }
            }
        }

        AppSpacer(height = 18.dp)

        // ── Addresses Section ──────────────────────────────────────────────────
        Text(
            text = "Addresses",
            color = ProfileNavy,
            fontSize = 15.sp,
            fontFamily = GillSansBold,
            modifier = Modifier.padding(bottom = 10.dp)
        )

        // ── Registered Address ─────────────────────────────────────────────────
        Text(
            text = "Registered Address",
            color = ProfileNavy,
            fontSize = 14.sp,
            fontFamily = GillSansBold,
            modifier = Modifier.padding(bottom = 6.dp)
        )

        ProfileFieldWithLabel(
            label = "Details",
            value = state.registeredAddress.houseFlatNo,
            onValueChange = { onEvent(ProfileUiEvent.UpdateRegisteredAddress(state.registeredAddress.copy(houseFlatNo = it))) },
            placeholder = "House / Flat / Door No",
            isRequired = true,
            modifier = Modifier.fillMaxWidth()
        )

        AppSpacer(height = 10.dp)

        val regStateOptions = if (statesList.isNotEmpty()) statesList else courtStatesList
        val regCityOptions = stateCitiesMap[state.registeredAddress.state]
            ?: courtStatesMap[state.registeredAddress.state]
            ?: emptyList()

        ProfileSearchableDropdownField(
            label = "State",
            value = state.registeredAddress.state,
            onValueChange = { newState ->
                onEvent(ProfileUiEvent.UpdateRegisteredAddress(state.registeredAddress.copy(state = newState)))
                if (newState.isNotBlank()) {
                    onEvent(ProfileUiEvent.FetchCities(newState))
                }
            },
            options = regStateOptions,
            placeholder = "Select or Enter State",
            isRequired = true,
            emptyMessage = "No states found",
            onOptionSelected = { selectedState ->
                onEvent(ProfileUiEvent.UpdateRegisteredAddress(state.registeredAddress.copy(state = selectedState)))
                onEvent(ProfileUiEvent.FetchCities(selectedState))
            },
            modifier = Modifier.fillMaxWidth()
        )

        AppSpacer(height = 10.dp)

        ProfileSearchableDropdownField(
            label = "City",
            value = state.registeredAddress.cityTown,
            onValueChange = { newCity ->
                onEvent(ProfileUiEvent.UpdateRegisteredAddress(state.registeredAddress.copy(cityTown = newCity)))
            },
            options = regCityOptions,
            placeholder = if (state.registeredAddress.state.isBlank()) "Select a state first or enter city" else "Select or Enter City",
            isRequired = true,
            emptyMessage = if (state.registeredAddress.state.isBlank()) "Please select a state first" else "No cities found",
            onOptionSelected = { selectedCity ->
                onEvent(ProfileUiEvent.UpdateRegisteredAddress(state.registeredAddress.copy(cityTown = selectedCity)))
            },
            modifier = Modifier.fillMaxWidth()
        )

        AppSpacer(height = 10.dp)

        ProfileFieldWithLabel(
            label = "Zip",
            value = state.registeredAddress.zipcode,
            onValueChange = { onEvent(ProfileUiEvent.UpdateRegisteredAddress(state.registeredAddress.copy(zipcode = it))) },
            placeholder = "Zip code",
            isRequired = true,
            errorMessage = state.regZipError,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            modifier = Modifier.fillMaxWidth()
        )

        AppSpacer(height = 16.dp)

        // ── Mailing Address ────────────────────────────────────────────────────
        Text(
            text = "Mailing Address",
            color = ProfileNavy,
            fontSize = 14.sp,
            fontFamily = GillSansBold,
            modifier = Modifier.padding(bottom = 6.dp)
        )

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { onEvent(ProfileUiEvent.SetSameAsRegistered(!state.sameAsRegistered)) }
                .padding(vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Checkbox(
                checked = state.sameAsRegistered,
                onCheckedChange = { onEvent(ProfileUiEvent.SetSameAsRegistered(it)) },
                colors = CheckboxDefaults.colors(checkedColor = ProfileNavy)
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = "Same as registered address",
                fontSize = 13.sp,
                fontFamily = GillSans,
                color = Color.Black
            )
        }

        AppSpacer(height = 8.dp)

        val isMailingEditable = !state.sameAsRegistered
        val currentMailing = if (state.sameAsRegistered) state.registeredAddress else state.mailingAddress
        val mailCityOptions = stateCitiesMap[currentMailing.state]
            ?: courtStatesMap[currentMailing.state]
            ?: emptyList()

        ProfileFieldWithLabel(
            label = "Details",
            value = currentMailing.houseFlatNo,
            onValueChange = { onEvent(ProfileUiEvent.UpdateMailingAddress(state.mailingAddress.copy(houseFlatNo = it))) },
            placeholder = "House / Flat / Door No",
            enabled = isMailingEditable,
            isRequired = true,
            modifier = Modifier.fillMaxWidth()
        )

        AppSpacer(height = 10.dp)

        ProfileSearchableDropdownField(
            label = "State",
            value = currentMailing.state,
            onValueChange = { newState ->
                onEvent(ProfileUiEvent.UpdateMailingAddress(state.mailingAddress.copy(state = newState)))
                if (newState.isNotBlank()) {
                    onEvent(ProfileUiEvent.FetchCities(newState))
                }
            },
            options = regStateOptions,
            placeholder = "Select or Enter State",
            enabled = isMailingEditable,
            isRequired = true,
            emptyMessage = "No states found",
            onOptionSelected = { selectedState ->
                onEvent(ProfileUiEvent.UpdateMailingAddress(state.mailingAddress.copy(state = selectedState)))
                onEvent(ProfileUiEvent.FetchCities(selectedState))
            },
            modifier = Modifier.fillMaxWidth()
        )

        AppSpacer(height = 10.dp)

        ProfileSearchableDropdownField(
            label = "City",
            value = currentMailing.cityTown,
            onValueChange = { newCity ->
                onEvent(ProfileUiEvent.UpdateMailingAddress(state.mailingAddress.copy(cityTown = newCity)))
            },
            options = mailCityOptions,
            placeholder = if (currentMailing.state.isBlank()) "Select a state first or enter city" else "Select or Enter City",
            enabled = isMailingEditable,
            isRequired = true,
            emptyMessage = if (currentMailing.state.isBlank()) "Please select a state first" else "No cities found",
            onOptionSelected = { selectedCity ->
                onEvent(ProfileUiEvent.UpdateMailingAddress(state.mailingAddress.copy(cityTown = selectedCity)))
            },
            modifier = Modifier.fillMaxWidth()
        )

        AppSpacer(height = 10.dp)

        ProfileFieldWithLabel(
            label = "Zip",
            value = currentMailing.zipcode,
            onValueChange = { onEvent(ProfileUiEvent.UpdateMailingAddress(state.mailingAddress.copy(zipcode = it))) },
            placeholder = "Zip code",
            enabled = isMailingEditable,
            isRequired = true,
            errorMessage = if (state.sameAsRegistered) null else state.mailZipError,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            modifier = Modifier.fillMaxWidth()
        )
    }
}

private fun formatCourtTypeDisplay(courtType: String): String {
    val trimmed = courtType.trim()
    if (trimmed.isEmpty()) return ""
    return when (trimmed.lowercase(Locale.ROOT)) {
        "supreme_court", "supremecourt", "supreme court" -> "Supreme Court"
        "high_court", "highcourt", "high court" -> "High Court"
        "district_court", "district court", "district & other lower courts" -> "District & Other Lower Courts"
        "arbitration_centers", "arbitration centers" -> "Arbitration Centers"
        "central_bureau_of_investigation_(cbi)", "central bureau of investigation (cbi)" -> "Central Bureau of Investigation (CBI)"
        "debt_recovery_tribunal_(drt)", "debt recovery tribunal (drt)" -> "Debt Recovery Tribunal (DRT)"
        "enforcement_directorate_(ed)", "enforcement directorate (ed)" -> "Enforcement Directorate (ED)"
        "civil_courts", "civil courts" -> "Civil Courts"
        "maritime_boards", "maritime boards" -> "Maritime Boards"
        "tax_tribunals", "tax tribunals" -> "Tax Tribunals"
        else -> {
            if (trimmed.contains("_")) {
                trimmed.split("_").joinToString(" ") { word ->
                    when {
                        word.startsWith("(") && word.endsWith(")") -> word.uppercase(Locale.ROOT)
                        word.equals("cbi", true) -> "(CBI)"
                        word.equals("drt", true) -> "(DRT)"
                        word.equals("ed", true) -> "(ED)"
                        else -> word.replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.ROOT) else it.toString() }
                    }
                }
            } else {
                trimmed
            }
        }
    }
}

@Composable
private fun CourtPracticeCardItem(
    courtItem: CourtEnrollmentItemState,
    courtTypesList: List<String>,
    suggestedCourtTypes: List<String>,
    statesList: List<String>,
    courtStatesMap: Map<String, List<String>>,
    onEvent: (ProfileUiEvent) -> Unit
) {
    var isCourtTypeOpen by remember { mutableStateOf(false) }
    var isStateOpen by remember { mutableStateOf(false) }
    var isCityOpen by remember { mutableStateOf(false) }

    var courtSearchQuery by remember { mutableStateOf("") }
    var stateSearchQuery by remember { mutableStateOf("") }
    var cityText by remember(courtItem.city) { mutableStateOf(courtItem.city) }

    val isSupremeCourt = courtItem.courtType.equals("Supreme Court", ignoreCase = true)
    val isStateEnabled = courtItem.courtType.isNotBlank() && !isSupremeCourt
    val isCityEnabled = courtItem.state.isNotBlank() && !isSupremeCourt

    // Filtered court types
    val filteredCourtTypes = remember(courtTypesList, courtSearchQuery) {
        val q = courtSearchQuery.trim().lowercase(Locale.ROOT)
        if (q.isEmpty()) courtTypesList
        else courtTypesList.filter { formatCourtTypeDisplay(it).lowercase(Locale.ROOT).contains(q) }
    }

    // Filtered states
    val filteredStates = remember(statesList, stateSearchQuery) {
        val q = stateSearchQuery.trim().lowercase(Locale.ROOT)
        if (q.isEmpty()) statesList
        else statesList.filter { it.lowercase(Locale.ROOT).contains(q) }
    }

    // Available cities for selected state
    val availableCities = remember(courtItem.state, courtStatesMap, cityText) {
        val list = courtStatesMap[courtItem.state] ?: emptyList()
        val q = cityText.trim().lowercase(Locale.ROOT)
        if (q.isEmpty()) list
        else list.filter { it.lowercase(Locale.ROOT).contains(q) }
    }

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 5.dp),
        shape = RoundedCornerShape(8.dp),
        color = Color(0xFFFAFAFA),
        border = BorderStroke(1.dp, Color(0xFFE5E7EB))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(10.dp)
        ) {
            // ── Row 1: Court Type Box | State Box | Delete Icon ──
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // 1. Court Type Box
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(42.dp)
                        .background(Color.White, RoundedCornerShape(6.dp))
                        .border(1.dp, Color(0xFFD1D5DB), RoundedCornerShape(6.dp))
                        .clickable {
                            isCourtTypeOpen = !isCourtTypeOpen
                            isStateOpen = false
                            isCityOpen = false
                            courtSearchQuery = ""
                        }
                        .padding(horizontal = 8.dp),
                    contentAlignment = Alignment.CenterStart
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = if (courtItem.courtType.isNotBlank()) formatCourtTypeDisplay(courtItem.courtType) else "Select Court Type",
                            fontSize = 13.sp,
                            fontFamily = GillSans,
                            color = if (courtItem.courtType.isNotBlank()) Color.Black else Color(0xFF9CA3AF),
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.weight(1f)
                        )

                        if (courtItem.courtType.isNotBlank()) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Clear Court Type",
                                tint = ProfileNavy,
                                modifier = Modifier
                                    .size(20.dp)
                                    .clickable {
                                        onEvent(ProfileUiEvent.UpdateCourtRow(courtItem.id, "", "", "", ""))
                                        isCourtTypeOpen = false
                                    }
                            )
                        } else {
                            Image(
                                painter = painterResource(R.drawable.drop_down_blue),
                                contentDescription = "Dropdown",
                                modifier = Modifier
                                    .size(18.dp)
                                    .rotate(if (isCourtTypeOpen) 180f else 0f)
                            )
                        }
                    }
                }

                // 2. State Box
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(42.dp)
                        .background(
                            if (isStateEnabled) Color.White else Color(0xFFF3F4F6),
                            RoundedCornerShape(6.dp)
                        )
                        .border(1.dp, Color(0xFFD1D5DB), RoundedCornerShape(6.dp))
                        .clickable(enabled = isStateEnabled) {
                            isStateOpen = !isStateOpen
                            isCourtTypeOpen = false
                            isCityOpen = false
                            stateSearchQuery = ""
                        }
                        .padding(horizontal = 8.dp),
                    contentAlignment = Alignment.CenterStart
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = if (isSupremeCourt) "Delhi" else if (courtItem.state.isNotBlank()) courtItem.state else "Select State",
                            fontSize = 13.sp,
                            fontFamily = GillSans,
                            color = if (isSupremeCourt || courtItem.state.isNotBlank()) {
                                if (isStateEnabled) Color.Black else Color(0xFF6B7280)
                            } else Color(0xFF9CA3AF),
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.weight(1f)
                        )

                        if (courtItem.state.isNotBlank() && isStateEnabled) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Clear State",
                                tint = ProfileNavy,
                                modifier = Modifier
                                    .size(20.dp)
                                    .clickable {
                                        onEvent(ProfileUiEvent.UpdateCourtRow(courtItem.id, courtItem.courtType, "", "", courtItem.courtName))
                                        isStateOpen = false
                                    }
                            )
                        } else {
                            Image(
                                painter = painterResource(R.drawable.drop_down_blue),
                                contentDescription = "Dropdown",
                                modifier = Modifier
                                    .size(18.dp)
                                    .rotate(if (isStateOpen) 180f else 0f)
                            )
                        }
                    }
                }

                // 3. Delete Trash Icon
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = "Delete Court",
                    tint = Color(0xFFE53935),
                    modifier = Modifier
                        .size(22.dp)
                        .clickable { onEvent(ProfileUiEvent.RemoveCourtRow(courtItem.id)) }
                )
            }

            // ── Court Type Dropdown Menu (Popup) ──
            if (isCourtTypeOpen) {
                AppSpacer(height = 6.dp)
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(8.dp),
                    color = Color.White,
                    border = BorderStroke(0.8.dp, Color(0xFFD1D5DB)),
                    shadowElevation = 4.dp
                ) {
                    Column(modifier = Modifier.fillMaxWidth()) {
                        // Search Bar
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(8.dp)
                                .background(Color.White, RoundedCornerShape(20.dp))
                                .border(0.8.dp, Color(0xFFD1D5DB), RoundedCornerShape(20.dp))
                                .padding(horizontal = 10.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.Search,
                                contentDescription = "Search",
                                tint = Color(0xFF9CA3AF),
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            BasicTextField(
                                value = courtSearchQuery,
                                onValueChange = { courtSearchQuery = it },
                                singleLine = true,
                                textStyle = TextStyle(
                                    fontSize = 13.sp,
                                    fontFamily = GillSans,
                                    color = Color.Black
                                ),
                                decorationBox = { innerTextField ->
                                    if (courtSearchQuery.isEmpty()) {
                                        Text(
                                            text = "Search Court",
                                            fontSize = 13.sp,
                                            fontFamily = GillSans,
                                            color = Color(0xFF9CA3AF)
                                        )
                                    }
                                    innerTextField()
                                },
                                modifier = Modifier.weight(1f)
                            )
                        }

                        HorizontalDivider(color = Color(0xFFEEEEEE), thickness = 0.8.dp)

                        // Court Types List
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .heightIn(max = 180.dp)
                                .verticalScroll(rememberScrollState())
                        ) {
                            if (filteredCourtTypes.isEmpty()) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(14.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = "No court types found",
                                        fontSize = 12.5.sp,
                                        fontFamily = GillSans,
                                        color = Color(0xFF9CA3AF)
                                    )
                                }
                            } else {
                                filteredCourtTypes.forEachIndexed { index, ct ->
                                    val formatted = formatCourtTypeDisplay(ct)
                                    val isSuggested = suggestedCourtTypes.any { formatCourtTypeDisplay(it).equals(formatted, ignoreCase = true) }

                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clickable {
                                                val isSupreme = formatted.equals("Supreme Court", ignoreCase = true)
                                                val defState = if (isSupreme) "Delhi" else (statesList.firstOrNull() ?: "")
                                                val defCity = if (isSupreme) "New Delhi" else (courtStatesMap[defState]?.firstOrNull() ?: "")
                                                onEvent(
                                                    ProfileUiEvent.UpdateCourtRow(
                                                        courtItem.id,
                                                        formatted,
                                                        defState,
                                                        defCity,
                                                        courtItem.courtName
                                                    )
                                                )
                                                isCourtTypeOpen = false
                                                courtSearchQuery = ""
                                            }
                                            .padding(horizontal = 14.dp, vertical = 10.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Text(
                                            text = formatted,
                                            fontSize = 13.5.sp,
                                            fontFamily = GillSans,
                                            color = Color(0xFF1F2937),
                                            modifier = Modifier.weight(1f)
                                        )

                                        if (isSuggested) {
                                            Icon(
                                                painter = painterResource(R.drawable.suggest_magic_icon),
                                                contentDescription = "Recommended",
                                                tint = ProfileNavy,
                                                modifier = Modifier.size(16.dp)
                                            )
                                        }
                                    }

                                    if (index < filteredCourtTypes.size - 1) {
                                        HorizontalDivider(color = Color(0xFFF3F4F6), thickness = 0.6.dp)
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // ── State Dropdown Menu (Popup) ──
            if (isStateOpen && isStateEnabled) {
                AppSpacer(height = 6.dp)
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(8.dp),
                    color = Color.White,
                    border = BorderStroke(0.8.dp, Color(0xFFD1D5DB)),
                    shadowElevation = 4.dp
                ) {
                    Column(modifier = Modifier.fillMaxWidth()) {
                        // Search Bar
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(8.dp)
                                .background(Color.White, RoundedCornerShape(20.dp))
                                .border(0.8.dp, Color(0xFFD1D5DB), RoundedCornerShape(20.dp))
                                .padding(horizontal = 10.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.Search,
                                contentDescription = "Search",
                                tint = Color(0xFF9CA3AF),
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            BasicTextField(
                                value = stateSearchQuery,
                                onValueChange = { stateSearchQuery = it },
                                singleLine = true,
                                textStyle = TextStyle(
                                    fontSize = 13.sp,
                                    fontFamily = GillSans,
                                    color = Color.Black
                                ),
                                decorationBox = { innerTextField ->
                                    if (stateSearchQuery.isEmpty()) {
                                        Text(
                                            text = "Search State",
                                            fontSize = 13.sp,
                                            fontFamily = GillSans,
                                            color = Color(0xFF9CA3AF)
                                        )
                                    }
                                    innerTextField()
                                },
                                modifier = Modifier.weight(1f)
                            )
                        }

                        HorizontalDivider(color = Color(0xFFEEEEEE), thickness = 0.8.dp)

                        // States List
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .heightIn(max = 180.dp)
                                .verticalScroll(rememberScrollState())
                        ) {
                            if (filteredStates.isEmpty()) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(14.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = "No states found",
                                        fontSize = 12.5.sp,
                                        fontFamily = GillSans,
                                        color = Color(0xFF9CA3AF)
                                    )
                                }
                            } else {
                                filteredStates.forEachIndexed { index, st ->
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clickable {
                                                val defCity = courtStatesMap[st]?.firstOrNull() ?: ""
                                                onEvent(
                                                    ProfileUiEvent.UpdateCourtRow(
                                                        courtItem.id,
                                                        courtItem.courtType,
                                                        st,
                                                        defCity,
                                                        courtItem.courtName
                                                    )
                                                )
                                                isStateOpen = false
                                                stateSearchQuery = ""
                                            }
                                            .padding(horizontal = 14.dp, vertical = 10.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = st,
                                            fontSize = 13.5.sp,
                                            fontFamily = GillSans,
                                            color = Color(0xFF1F2937),
                                            modifier = Modifier.weight(1f)
                                        )
                                    }

                                    if (index < filteredStates.size - 1) {
                                        HorizontalDivider(color = Color(0xFFF3F4F6), thickness = 0.6.dp)
                                    }
                                }
                            }
                        }
                    }
                }
            }

            AppSpacer(height = 8.dp)

            // ── Row 2: City Box / Dropdown ──
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(42.dp)
                    .background(
                        if (isCityEnabled) Color.White else Color(0xFFF3F4F6),
                        RoundedCornerShape(6.dp)
                    )
                    .border(1.dp, Color(0xFFD1D5DB), RoundedCornerShape(6.dp))
                    .padding(horizontal = 8.dp),
                contentAlignment = Alignment.CenterStart
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    if (isCityEnabled) {
                        BasicTextField(
                            value = cityText,
                            onValueChange = {
                                cityText = it
                                isCityOpen = true
                            },
                            singleLine = true,
                            textStyle = TextStyle(
                                fontSize = 13.5.sp,
                                fontFamily = GillSans,
                                color = Color.Black
                            ),
                            decorationBox = { innerTextField ->
                                if (cityText.isEmpty()) {
                                    Text(
                                        text = "Search & Add City",
                                        fontSize = 13.5.sp,
                                        fontFamily = GillSans,
                                        color = Color(0xFF9CA3AF)
                                    )
                                }
                                innerTextField()
                            },
                            modifier = Modifier.weight(1f)
                        )
                    } else {
                        Text(
                            text = if (isSupremeCourt) "New Delhi" else if (courtItem.city.isNotBlank()) courtItem.city else "Search & Add City",
                            fontSize = 13.5.sp,
                            fontFamily = GillSans,
                            color = if (isSupremeCourt || courtItem.city.isNotBlank()) Color(0xFF6B7280) else Color(0xFF9CA3AF),
                            modifier = Modifier.weight(1f)
                        )
                    }

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        if (isCityEnabled && cityText.isNotBlank() && !cityText.equals(courtItem.city, ignoreCase = true)) {
                            // Confirm button
                            Icon(
                                imageVector = Icons.Default.Check,
                                contentDescription = "Confirm City",
                                tint = ProfileNavy,
                                modifier = Modifier
                                    .size(20.dp)
                                    .clickable {
                                        onEvent(
                                            ProfileUiEvent.UpdateCourtRow(
                                                courtItem.id,
                                                courtItem.courtType,
                                                courtItem.state,
                                                cityText.trim(),
                                                courtItem.courtName
                                            )
                                        )
                                        isCityOpen = false
                                    }
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            // Clear button
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Clear City",
                                tint = Color(0xFFE53935),
                                modifier = Modifier
                                    .size(20.dp)
                                    .clickable {
                                        cityText = ""
                                        onEvent(
                                            ProfileUiEvent.UpdateCourtRow(
                                                courtItem.id,
                                                courtItem.courtType,
                                                courtItem.state,
                                                "",
                                                courtItem.courtName
                                            )
                                        )
                                        isCityOpen = false
                                    }
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                        }

                        Image(
                            painter = painterResource(R.drawable.drop_down_blue),
                            contentDescription = "Dropdown",
                            modifier = Modifier
                                .size(18.dp)
                                .rotate(if (isCityOpen) 180f else 0f)
                                .clickable(enabled = isCityEnabled) {
                                    isCityOpen = !isCityOpen
                                    isCourtTypeOpen = false
                                    isStateOpen = false
                                }
                        )
                    }
                }
            }

            // ── City Dropdown Menu (Popup) ──
            if (isCityOpen && isCityEnabled) {
                AppSpacer(height = 6.dp)
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(8.dp),
                    color = Color.White,
                    border = BorderStroke(0.8.dp, Color(0xFFD1D5DB)),
                    shadowElevation = 4.dp
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(max = 180.dp)
                            .verticalScroll(rememberScrollState())
                    ) {
                        if (availableCities.isEmpty()) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(14.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = if (cityText.isBlank()) "No cities available" else "Press ✓ to add \"$cityText\"",
                                    fontSize = 12.5.sp,
                                    fontFamily = GillSans,
                                    color = Color(0xFF9CA3AF)
                                )
                            }
                        } else {
                            availableCities.forEachIndexed { index, city ->
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable {
                                            cityText = city
                                            onEvent(
                                                ProfileUiEvent.UpdateCourtRow(
                                                    courtItem.id,
                                                    courtItem.courtType,
                                                    courtItem.state,
                                                    city,
                                                    courtItem.courtName
                                                )
                                            )
                                            isCityOpen = false
                                        }
                                        .padding(horizontal = 14.dp, vertical = 10.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = city,
                                        fontSize = 13.5.sp,
                                        fontFamily = GillSans,
                                        color = Color(0xFF1F2937),
                                        modifier = Modifier.weight(1f)
                                    )
                                }

                                if (index < availableCities.size - 1) {
                                    HorizontalDivider(color = Color(0xFFF3F4F6), thickness = 0.6.dp)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

// ═════════════════════════════════════════════════════════════════════════════
// 2. COURTS & CASES EDIT SECTION
// ═════════════════════════════════════════════════════════════════════════════
@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun CourtsAndCasesEditSection(
    state: CourtsAndCasesEditState,
    allCaseTypes: List<String>,
    courtStatesList: List<String>,
    courtStatesMap: Map<String, List<String>>,
    suggestedCourtTypes: List<String>,
    allCourtTypes: List<String> = emptyList(),
    onEvent: (ProfileUiEvent) -> Unit
) {
    var newCaseTypeText by remember { mutableStateOf("") }

    // Unified list of sorted court types: Supreme Court first, suggested next, then rest sorted
    val sortedCourtTypes = remember(allCourtTypes, suggestedCourtTypes) {
        val result = linkedSetOf<String>()
        result.add("Supreme Court")
        suggestedCourtTypes.forEach { if (it.isNotBlank()) result.add(formatCourtTypeDisplay(it)) }
        val rest = allCourtTypes.map { formatCourtTypeDisplay(it) }.filter { !result.contains(it) && it.isNotBlank() }.sorted()
        rest.forEach { result.add(it) }
        if (result.size <= 1) {
            listOf(
                "Supreme Court",
                "Arbitration Centers",
                "Central Bureau of Investigation (CBI)",
                "Civil Courts",
                "Debt Recovery Tribunal (DRT)",
                "District & Other Lower Courts",
                "Enforcement Directorate (ED)",
                "High Court",
                "Maritime Boards",
                "Tax Tribunals"
            )
        } else {
            result.toList()
        }
    }

    val statesList = remember(courtStatesList) {
        if (courtStatesList.isNotEmpty()) courtStatesList
        else listOf(
            "Andaman and Nicobar Islands",
            "Andhra Pradesh",
            "Arunachal Pradesh",
            "Assam",
            "Bihar",
            "Chandigarh",
            "Chhattisgarh",
            "Delhi",
            "Goa",
            "Gujarat",
            "Haryana",
            "Himachal Pradesh",
            "Jammu and Kashmir",
            "Jharkhand",
            "Karnataka",
            "Kerala",
            "Ladakh",
            "Madhya Pradesh",
            "Maharashtra",
            "Manipur",
            "Meghalaya",
            "Mizoram",
            "Nagaland",
            "Odisha",
            "Puducherry",
            "Punjab",
            "Rajasthan",
            "Sikkim",
            "Tamil Nadu",
            "Telangana",
            "Tripura",
            "Uttar Pradesh",
            "Uttarakhand",
            "West Bengal"
        )
    }

    Column(modifier = Modifier.fillMaxWidth()) {
        // ── Section 1 Header: Court Practice Details ──────────────────────────
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = "Court Practice Details",
                color = ProfileNavy,
                fontSize = 15.sp,
                fontFamily = GillSansBold
            )

            Text(
                text = "+ Add Court",
                fontSize = 13.sp,
                fontFamily = GillSansBold,
                color = ProfileNavy,
                modifier = Modifier
                    .clickable { onEvent(ProfileUiEvent.AddCourtRow) }
                    .padding(vertical = 4.dp, horizontal = 4.dp)
            )
        }

        // Subtitle: Recommended for your practice areas
        if (suggestedCourtTypes.isNotEmpty()) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(top = 4.dp, bottom = 6.dp)
            ) {
                Icon(
                    painter = painterResource(R.drawable.suggest_magic_icon),
                    contentDescription = "Recommended",
                    tint = ProfileNavy,
                    modifier = Modifier.size(15.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "Recommended for your selected practice areas",
                    fontSize = 12.sp,
                    fontFamily = GillSans,
                    color = ProfileNavy
                )
            }
        }

        AppSpacer(height = 4.dp)

        // Court Practice Item Cards
        state.courtEnrollments.forEach { courtItem ->
            CourtPracticeCardItem(
                courtItem = courtItem,
                courtTypesList = sortedCourtTypes,
                suggestedCourtTypes = suggestedCourtTypes,
                statesList = statesList,
                courtStatesMap = courtStatesMap,
                onEvent = onEvent
            )
        }

        AppSpacer(height = 14.dp)

        // ── Section 2: Types of Cases Handled ──────────────────────────────────
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = "Types of Cases Handled",
                color = ProfileNavy,
                fontSize = 15.sp,
                fontFamily = GillSansBold
            )

            Text(
                text = "${state.casesHandled.size} added",
                fontSize = 12.sp,
                fontFamily = GillSans,
                color = Color(0xFF888888)
            )
        }

        AppSpacer(height = 6.dp)

        ProfileFieldWithLabel(
            label = "",
            value = newCaseTypeText,
            onValueChange = { newCaseTypeText = it },
            placeholder = "Add case type (e.g. Criminal, Civil)...",
            isRequired = false,
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
            keyboardActions = KeyboardActions(
                onDone = {
                    if (newCaseTypeText.isNotBlank()) {
                        onEvent(ProfileUiEvent.AddCaseType(newCaseTypeText.trim()))
                        newCaseTypeText = ""
                    }
                }
            ),
            trailingIcon = {
                if (newCaseTypeText.isNotBlank()) {
                    Text(
                        text = "+ Add",
                        fontSize = 12.sp,
                        fontFamily = GillSansBold,
                        color = ProfileNavy,
                        modifier = Modifier
                            .clickable {
                                onEvent(ProfileUiEvent.AddCaseType(newCaseTypeText.trim()))
                                newCaseTypeText = ""
                            }
                            .padding(horizontal = 4.dp, vertical = 2.dp)
                    )
                }
            },
            modifier = Modifier.fillMaxWidth()
        )

        // Selected pale blue chips for cases handled
        if (state.casesHandled.isNotEmpty()) {
            AppSpacer(height = 8.dp)
            FlowRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                state.casesHandled.forEach { caseType ->
                    ProfilePaleBlueChip(
                        text = caseType,
                        onRemove = { onEvent(ProfileUiEvent.RemoveCaseType(caseType)) }
                    )
                }
            }
        }
    }
}

// ═════════════════════════════════════════════════════════════════════════════
// 3. EDUCATION & AWARDS EDIT SECTION
// ═════════════════════════════════════════════════════════════════════════════
@Composable
private fun EduAwardItemCard(
    title: String,
    titleHint: String,
    onTitleChange: (String) -> Unit,
    institution: String,
    institutionHint: String,
    onInstitutionChange: (String) -> Unit,
    year: String,
    yearHint: String = "YYYY",
    onYearChange: (String) -> Unit,
    onRemove: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
    ) {
        // Row 1: Title input | Year input | Delete X icon
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Title Input
            Box(
                modifier = Modifier
                    .weight(1f)
                    .height(42.dp)
                    .background(Color(0xFFF0F2F5), RoundedCornerShape(6.dp))
                    .padding(horizontal = 10.dp),
                contentAlignment = Alignment.CenterStart
            ) {
                BasicTextField(
                    value = title,
                    onValueChange = onTitleChange,
                    singleLine = true,
                    textStyle = TextStyle(
                        fontSize = 14.sp,
                        fontFamily = GillSans,
                        color = Color.Black
                    ),
                    decorationBox = { innerTextField ->
                        if (title.isEmpty()) {
                            Text(
                                text = titleHint,
                                fontSize = 14.sp,
                                fontFamily = GillSans,
                                color = Color(0xFF9CA3AF)
                            )
                        }
                        innerTextField()
                    },
                    modifier = Modifier.fillMaxWidth()
                )
            }

            // Year Input
            Box(
                modifier = Modifier
                    .width(80.dp)
                    .height(42.dp)
                    .background(Color(0xFFF0F2F5), RoundedCornerShape(6.dp))
                    .padding(horizontal = 10.dp),
                contentAlignment = Alignment.CenterStart
            ) {
                BasicTextField(
                    value = year,
                    onValueChange = {
                        val filtered = it.filter { c -> c.isDigit() }.take(4)
                        onYearChange(filtered)
                    },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    textStyle = TextStyle(
                        fontSize = 14.sp,
                        fontFamily = GillSans,
                        color = Color.Black
                    ),
                    decorationBox = { innerTextField ->
                        if (year.isEmpty()) {
                            Text(
                                text = yearHint,
                                fontSize = 14.sp,
                                fontFamily = GillSans,
                                color = Color(0xFF9CA3AF)
                            )
                        }
                        innerTextField()
                    },
                    modifier = Modifier.fillMaxWidth()
                )
            }

            // Delete X Icon
            Icon(
                imageVector = Icons.Default.Close,
                contentDescription = "Delete",
                tint = Color(0xFF9CA3AF),
                modifier = Modifier
                    .size(20.dp)
                    .clickable { onRemove() }
            )
        }

        AppSpacer(height = 6.dp)

        // Row 2: Institution / Authority Input (Matching width of Row 1 inputs)
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .weight(1f)
                    .height(42.dp)
                    .background(Color(0xFFF0F2F5), RoundedCornerShape(6.dp))
                    .padding(horizontal = 10.dp),
                contentAlignment = Alignment.CenterStart
            ) {
                BasicTextField(
                    value = institution,
                    onValueChange = onInstitutionChange,
                    singleLine = true,
                    textStyle = TextStyle(
                        fontSize = 14.sp,
                        fontFamily = GillSans,
                        color = Color.Black
                    ),
                    decorationBox = { innerTextField ->
                        if (institution.isEmpty()) {
                            Text(
                                text = institutionHint,
                                fontSize = 14.sp,
                                fontFamily = GillSans,
                                color = Color(0xFF9CA3AF)
                            )
                        }
                        innerTextField()
                    },
                    modifier = Modifier.fillMaxWidth()
                )
            }

            // Invisible spacer to match delete icon width on the right
            Spacer(modifier = Modifier.width(28.dp))
        }
    }
}

@Composable
private fun EducationAndAwardsEditSection(
    isFirmProfile: Boolean,
    state: EducationAndAwardsEditState,
    onEvent: (ProfileUiEvent) -> Unit
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        if (!isFirmProfile) {
            // ── 1. Educational Qualifications ───────────────────────────────────
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Educational Qualifications",
                    color = ProfileNavy,
                    fontSize = 14.5.sp,
                    fontFamily = GillSansBold
                )

                Text(
                    text = "+ Add More",
                    fontSize = 13.sp,
                    fontFamily = GillSansBold,
                    color = ProfileNavy,
                    modifier = Modifier
                        .clickable { onEvent(ProfileUiEvent.AddEducationRow) }
                        .padding(vertical = 4.dp, horizontal = 4.dp)
                )
            }

            AppSpacer(height = 6.dp)

            state.educationList.forEach { eduItem ->
                EduAwardItemCard(
                    title = eduItem.title,
                    titleHint = "Degree",
                    onTitleChange = { onEvent(ProfileUiEvent.UpdateEducationRow(eduItem.id, it, eduItem.institution, eduItem.year)) },
                    institution = eduItem.institution,
                    institutionHint = "Institution",
                    onInstitutionChange = { onEvent(ProfileUiEvent.UpdateEducationRow(eduItem.id, eduItem.title, it, eduItem.year)) },
                    year = eduItem.year,
                    onYearChange = { onEvent(ProfileUiEvent.UpdateEducationRow(eduItem.id, eduItem.title, eduItem.institution, it)) },
                    onRemove = { onEvent(ProfileUiEvent.RemoveEducationRow(eduItem.id)) }
                )
            }

            AppSpacer(height = 14.dp)

            // ── 2. Certifications ───────────────────────────────────────────────
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Certifications",
                    color = ProfileNavy,
                    fontSize = 14.5.sp,
                    fontFamily = GillSansBold
                )

                Text(
                    text = "+ Add More",
                    fontSize = 13.sp,
                    fontFamily = GillSansBold,
                    color = ProfileNavy,
                    modifier = Modifier
                        .clickable { onEvent(ProfileUiEvent.AddCertificationRow) }
                        .padding(vertical = 4.dp, horizontal = 4.dp)
                )
            }

            AppSpacer(height = 6.dp)

            state.certificationsList.forEach { certItem ->
                EduAwardItemCard(
                    title = certItem.title,
                    titleHint = "Certification Name",
                    onTitleChange = { onEvent(ProfileUiEvent.UpdateCertificationRow(certItem.id, it, certItem.institution, certItem.year)) },
                    institution = certItem.institution,
                    institutionHint = "Institution",
                    onInstitutionChange = { onEvent(ProfileUiEvent.UpdateCertificationRow(certItem.id, certItem.title, it, certItem.year)) },
                    year = certItem.year,
                    onYearChange = { onEvent(ProfileUiEvent.UpdateCertificationRow(certItem.id, certItem.title, certItem.institution, it)) },
                    onRemove = { onEvent(ProfileUiEvent.RemoveCertificationRow(certItem.id)) }
                )
            }

            AppSpacer(height = 14.dp)

            // ── 3. Awards & Recognitions(Optional) ──────────────────────────────
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Awards & Recognitions(Optional)",
                    color = ProfileNavy,
                    fontSize = 14.5.sp,
                    fontFamily = GillSansBold
                )

                Text(
                    text = "+ Add More",
                    fontSize = 13.sp,
                    fontFamily = GillSansBold,
                    color = ProfileNavy,
                    modifier = Modifier
                        .clickable { onEvent(ProfileUiEvent.AddAwardRow) }
                        .padding(vertical = 4.dp, horizontal = 4.dp)
                )
            }

            AppSpacer(height = 6.dp)

            state.awardsList.forEach { awardItem ->
                EduAwardItemCard(
                    title = awardItem.title,
                    titleHint = "Award Name",
                    onTitleChange = { onEvent(ProfileUiEvent.UpdateAwardRow(awardItem.id, it, awardItem.institution, awardItem.year)) },
                    institution = awardItem.institution,
                    institutionHint = "Institution",
                    onInstitutionChange = { onEvent(ProfileUiEvent.UpdateAwardRow(awardItem.id, awardItem.title, it, awardItem.year)) },
                    year = awardItem.year,
                    onYearChange = { onEvent(ProfileUiEvent.UpdateAwardRow(awardItem.id, awardItem.title, awardItem.institution, it)) },
                    onRemove = { onEvent(ProfileUiEvent.RemoveAwardRow(awardItem.id)) }
                )
            }
        } else {
            // Firm Profile: Awards & Recognition only
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Awards & Recognition",
                    color = ProfileNavy,
                    fontSize = 14.5.sp,
                    fontFamily = GillSansBold
                )

                Text(
                    text = "+ Add More",
                    fontSize = 13.sp,
                    fontFamily = GillSansBold,
                    color = ProfileNavy,
                    modifier = Modifier
                        .clickable { onEvent(ProfileUiEvent.AddAwardRow) }
                        .padding(vertical = 4.dp, horizontal = 4.dp)
                )
            }

            AppSpacer(height = 6.dp)

            state.awardsList.forEach { awardItem ->
                EduAwardItemCard(
                    title = awardItem.title,
                    titleHint = "Award Name",
                    onTitleChange = { onEvent(ProfileUiEvent.UpdateAwardRow(awardItem.id, it, awardItem.institution, awardItem.year)) },
                    institution = awardItem.institution,
                    institutionHint = "Institution",
                    onInstitutionChange = { onEvent(ProfileUiEvent.UpdateAwardRow(awardItem.id, awardItem.title, it, awardItem.year)) },
                    year = awardItem.year,
                    onYearChange = { onEvent(ProfileUiEvent.UpdateAwardRow(awardItem.id, awardItem.title, awardItem.institution, it)) },
                    onRemove = { onEvent(ProfileUiEvent.RemoveAwardRow(awardItem.id)) }
                )
            }
        }
    }
}

// ═════════════════════════════════════════════════════════════════════════════
// 4. SET AVAILABILITY EDIT SECTION
// ═════════════════════════════════════════════════════════════════════════════
@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun SetAvailabilityEditSection(
    state: AvailabilityEditState,
    onEvent: (ProfileUiEvent) -> Unit
) {
    val context = LocalContext.current
    var expandedExcludeDayName by remember { mutableStateOf<String?>(null) }
    var tempExcludedSlots by remember { mutableStateOf<List<String>>(emptyList()) }
    var showInfoTooltip by remember { mutableStateOf(false) }

    fun showTimePicker(initialTime: String, onSelected: (String) -> Unit) {
        val cal = Calendar.getInstance()
        var initH = 9
        var initM = 0
        try {
            val sdf = SimpleDateFormat(if (initialTime.contains("AM", true) || initialTime.contains("PM", true)) "hh:mm a" else "H:mm", Locale.US)
            val d = sdf.parse(initialTime.trim())
            if (d != null) {
                cal.time = d
                initH = cal.get(Calendar.HOUR_OF_DAY)
                initM = cal.get(Calendar.MINUTE)
            }
        } catch (_: Exception) {}

        TimePickerDialog(
            context,
            { _, hourOfDay, minute ->
                val outCal = Calendar.getInstance().apply {
                    set(Calendar.HOUR_OF_DAY, hourOfDay)
                    set(Calendar.MINUTE, minute)
                }
                val outSdf = SimpleDateFormat("hh:mm a", Locale.US)
                onSelected(outSdf.format(outCal.time))
            },
            initH,
            initM,
            false
        ).show()
    }

    Column(modifier = Modifier.fillMaxWidth()) {
        // Section Header: Booking Settings
        Text(
            text = "Booking Settings",
            color = ProfileNavy,
            fontSize = 15.sp,
            fontFamily = GillSansBold
        )

        AppSpacer(height = 6.dp)

        // Subheading: Weekly hours with info icon
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Image(
                painter = painterResource(R.drawable.timer),
                contentDescription = "Timer",
                modifier = Modifier.size(18.dp)
            )

            Text(
                text = "Weekly hours",
                fontSize = 14.sp,
                fontFamily = GillSansBold,
                color = Color(0xFF2079AD)
            )

            Icon(
                imageVector = Icons.Default.Info,
                contentDescription = "Info",
                tint = Color(0xFF004D87),
                modifier = Modifier
                    .size(18.dp)
                    .clickable { showInfoTooltip = !showInfoTooltip }
            )
        }

        if (showInfoTooltip) {
            AppSpacer(height = 6.dp)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFFE8F0FE), RoundedCornerShape(6.dp))
                    .padding(10.dp)
            ) {
                Text(
                    text = "Set your available hours. We will automatically create 30-minute booking slots. Need a break? Click the exclude icon to block specific times.",
                    fontSize = 12.5.sp,
                    fontFamily = GillSans,
                    color = Color(0xFF004D87),
                    lineHeight = 16.sp
                )
            }
        }

        AppSpacer(height = 6.dp)

        Text(
            text = "Set when you are typically available for meetings",
            fontSize = 13.5.sp,
            fontFamily = GillSans,
            color = Color(0xFF585858)
        )

        AppSpacer(height = 10.dp)

        // 7-day rolling schedule list
        state.weeklySchedule.forEach { dayItem ->
            val isExpanded = expandedExcludeDayName == dayItem.dayName
            val isEligible = isDurationAtLeast4Hours(dayItem.fromTime, dayItem.toTime)
            val displayDate = if (dayItem.dateLabel.isNotBlank()) dayItem.dateLabel else dayItem.dayName

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                shape = RoundedCornerShape(8.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                border = BorderStroke(0.8.dp, Color(0xFFE5E7EB)),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 10.dp, vertical = 8.dp)
                ) {
                    // Main Day Row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        // Checkbox + Day Label
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .weight(1f, fill = false)
                                .clickable {
                                    val nextWorking = !dayItem.isWorkingDay
                                    onEvent(ProfileUiEvent.ToggleWorkingDay(dayItem.dayName, nextWorking))
                                    if (!nextWorking && isExpanded) {
                                        expandedExcludeDayName = null
                                    }
                                }
                        ) {
                            Checkbox(
                                checked = dayItem.isWorkingDay,
                                onCheckedChange = { isChecked ->
                                    onEvent(ProfileUiEvent.ToggleWorkingDay(dayItem.dayName, isChecked))
                                    if (!isChecked && isExpanded) {
                                        expandedExcludeDayName = null
                                    }
                                },
                                colors = CheckboxDefaults.colors(
                                    checkedColor = Color(0xFF004D87),
                                    uncheckedColor = Color(0xFF757575)
                                ),
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = displayDate,
                                fontSize = 13.5.sp,
                                fontFamily = if (dayItem.isWorkingDay) GillSansBold else GillSans,
                                color = if (dayItem.isWorkingDay) Color.Black else Color(0xFF585858)
                            )
                        }

                        if (!dayItem.isWorkingDay) {
                            Text(
                                text = "Not Available",
                                fontSize = 13.sp,
                                fontFamily = GillSans,
                                color = Color(0xFF757575),
                                modifier = Modifier.padding(end = 4.dp)
                            )
                        } else {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                // From Time Box
                                Box(
                                    modifier = Modifier
                                        .background(Color(0xFFEAEAED), RoundedCornerShape(6.dp))
                                        .clickable {
                                            showTimePicker(dayItem.fromTime) { newFrom ->
                                                val fixedTo = validateMinimumDuration(newFrom, dayItem.toTime)
                                                val validExcluded = filterInvalidExcludedSlots(dayItem.excludedSlots, newFrom, fixedTo)
                                                onEvent(ProfileUiEvent.UpdateWorkingHours(dayItem.dayName, newFrom, fixedTo))
                                                if (validExcluded.size != dayItem.excludedSlots.size) {
                                                    onEvent(ProfileUiEvent.SaveExcludedSlots(dayItem.dayName, validExcluded))
                                                }
                                            }
                                        }
                                        .padding(horizontal = 9.dp, vertical = 6.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = dayItem.fromTime,
                                        fontSize = 12.5.sp,
                                        fontFamily = GillSans,
                                        color = Color.Black
                                    )
                                }

                                Text(
                                    text = "-",
                                    fontSize = 14.sp,
                                    fontFamily = GillSans,
                                    color = Color(0xFF585858)
                                )

                                // To Time Box
                                Box(
                                    modifier = Modifier
                                        .background(Color(0xFFEAEAED), RoundedCornerShape(6.dp))
                                        .clickable {
                                            showTimePicker(dayItem.toTime) { newTo ->
                                                val fixedTo = validateMinimumDuration(dayItem.fromTime, newTo)
                                                val validExcluded = filterInvalidExcludedSlots(dayItem.excludedSlots, dayItem.fromTime, fixedTo)
                                                onEvent(ProfileUiEvent.UpdateWorkingHours(dayItem.dayName, dayItem.fromTime, fixedTo))
                                                if (validExcluded.size != dayItem.excludedSlots.size) {
                                                    onEvent(ProfileUiEvent.SaveExcludedSlots(dayItem.dayName, validExcluded))
                                                }
                                            }
                                        }
                                        .padding(horizontal = 9.dp, vertical = 6.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = dayItem.toTime,
                                        fontSize = 12.5.sp,
                                        fontFamily = GillSans,
                                        color = Color.Black
                                    )
                                }

                                Spacer(modifier = Modifier.width(4.dp))

                                // Slot Exclusion Icon Button
                                Box(
                                    modifier = Modifier
                                        .size(26.dp)
                                        .background(
                                            color = if (isExpanded) Color(0xFF004D87) else Color(0xFFE8F0FE),
                                            shape = RoundedCornerShape(6.dp)
                                        )
                                        .border(
                                            width = 1.dp,
                                            color = Color(0xFF004D87),
                                            shape = RoundedCornerShape(6.dp)
                                        )
                                        .clickable(enabled = isEligible) {
                                            if (isExpanded) {
                                                expandedExcludeDayName = null
                                            } else {
                                                expandedExcludeDayName = dayItem.dayName
                                                tempExcludedSlots = dayItem.excludedSlots.toList()
                                            }
                                        },
                                    contentAlignment = Alignment.Center
                                ) {
                                    Image(
                                        painter = painterResource(R.drawable.slot_icon),
                                        contentDescription = "Exclude Slots",
                                        colorFilter = ColorFilter.tint(
                                            if (isExpanded) Color.White else if (isEligible) Color(0xFF004D87) else Color(0xFF004D87).copy(alpha = 0.35f)
                                        ),
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            }
                        }
                    }

                    // Inline Slot Exclusion View (Matching Java XML layout_excluded_slots)
                    if (isExpanded && dayItem.isWorkingDay) {
                        AppSpacer(height = 8.dp)
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(Color(0xFFE8F0FE), RoundedCornerShape(8.dp))
                                .padding(12.dp)
                        ) {
                            Column(modifier = Modifier.fillMaxWidth()) {
                                // Header
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(
                                        text = "Select slots to exclude",
                                        fontSize = 14.5.sp,
                                        fontFamily = GillSansBold,
                                        color = Color.Black
                                    )
                                    Icon(
                                        imageVector = Icons.Default.Close,
                                        contentDescription = "Close",
                                        tint = Color(0xFF888888),
                                        modifier = Modifier
                                            .size(20.dp)
                                            .clickable { expandedExcludeDayName = null }
                                    )
                                }

                                AppSpacer(height = 4.dp)

                                Text(
                                    text = "Click 30-minute slots to exclude (${tempExcludedSlots.size}/4 slots, max 2 hours)",
                                    fontSize = 11.5.sp,
                                    fontFamily = GillSans,
                                    color = Color(0xFF666666)
                                )

                                AppSpacer(height = 10.dp)

                                val availableSlots = remember(dayItem.fromTime, dayItem.toTime) {
                                    generate30MinSlots(dayItem.fromTime, dayItem.toTime)
                                }

                                if (availableSlots.isEmpty()) {
                                    Text(
                                        text = "No slots available for the selected time range",
                                        fontSize = 12.sp,
                                        fontFamily = GillSans,
                                        color = Color(0xFF888888),
                                        modifier = Modifier.padding(vertical = 8.dp)
                                    )
                                } else {
                                    val chunkedSlots = availableSlots.chunked(2)
                                    Column(
                                        modifier = Modifier.fillMaxWidth(),
                                        verticalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        chunkedSlots.forEach { rowSlots ->
                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                                            ) {
                                                rowSlots.forEach { slot ->
                                                    val isSelected = tempExcludedSlots.contains(slot)
                                                    val maxReached = tempExcludedSlots.size >= 4 && !isSelected

                                                    Box(
                                                        modifier = Modifier
                                                            .weight(1f)
                                                            .background(
                                                                color = if (isSelected) Color(0xFF004D87) else Color.White,
                                                                shape = RoundedCornerShape(8.dp)
                                                            )
                                                            .border(
                                                                width = 1.dp,
                                                                color = if (isSelected) Color(0xFF004D87) else Color(0xFFD6D6D6),
                                                                shape = RoundedCornerShape(8.dp)
                                                            )
                                                            .clickable(enabled = !maxReached || isSelected) {
                                                                if (isSelected) {
                                                                    tempExcludedSlots = tempExcludedSlots - slot
                                                                } else if (tempExcludedSlots.size < 4) {
                                                                    tempExcludedSlots = tempExcludedSlots + slot
                                                                }
                                                            }
                                                            .padding(vertical = 9.dp, horizontal = 4.dp),
                                                        contentAlignment = Alignment.Center
                                                    ) {
                                                        Text(
                                                            text = slot,
                                                            color = if (isSelected) Color.White else if (maxReached) Color(0xFF999999) else Color.Black,
                                                            fontSize = 11.sp,
                                                            fontFamily = if (isSelected) GillSansBold else GillSans,
                                                            textAlign = TextAlign.Center
                                                        )
                                                    }
                                                }
                                                if (rowSlots.size == 1) {
                                                    Spacer(modifier = Modifier.weight(1f))
                                                }
                                            }
                                        }
                                    }
                                }

                                AppSpacer(height = 12.dp)

                                // Action Buttons
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.End,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    // Cancel Button
                                    Box(
                                        modifier = Modifier
                                            .background(Color(0xFFEAEAED), RoundedCornerShape(6.dp))
                                            .clickable { expandedExcludeDayName = null }
                                            .padding(horizontal = 16.dp, vertical = 7.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = "Cancel",
                                            fontSize = 12.5.sp,
                                            fontFamily = GillSans,
                                            color = Color.Black
                                        )
                                    }

                                    Spacer(modifier = Modifier.width(10.dp))

                                    // Save Button
                                    Box(
                                        modifier = Modifier
                                            .background(Color(0xFF004D87), RoundedCornerShape(6.dp))
                                            .clickable {
                                                onEvent(ProfileUiEvent.SaveExcludedSlots(dayItem.dayName, tempExcludedSlots))
                                                expandedExcludeDayName = null
                                            }
                                            .padding(horizontal = 20.dp, vertical = 7.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = "Save",
                                            fontSize = 12.5.sp,
                                            fontFamily = GillSansBold,
                                            color = Color.White
                                        )
                                    }
                                }
                            }
                        }
                    }

                    // Display Timing Summary Card when not expanding the editor and excluded slots exist
                    if (!isExpanded && dayItem.isWorkingDay && dayItem.excludedSlots.isNotEmpty()) {
                        AppSpacer(height = 6.dp)

                        // Timing Summary Card
                        Surface(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(6.dp),
                            color = Color(0xFFF9FAFB),
                            border = BorderStroke(0.6.dp, Color(0xFFE5E7EB))
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .width(3.5.dp)
                                        .height(34.dp)
                                        .background(Color(0xFF004D87), RoundedCornerShape(2.dp))
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Column {
                                    val totalSlots = remember(dayItem.fromTime, dayItem.toTime) {
                                        generate30MinSlots(dayItem.fromTime, dayItem.toTime).size
                                    }
                                    val excludedCount = dayItem.excludedSlots.size
                                    val availCount = maxOf(0, totalSlots - excludedCount)
                                    val availMinutes = availCount * 30
                                    val hours = availMinutes / 60
                                    val mins = availMinutes % 60
                                    val availTimeStr = if (mins > 0) "${hours}h ${mins}m" else "${hours} hours"
                                    val slotsListStr = dayItem.excludedSlots.joinToString(", ")

                                    Text(
                                        text = "Availability: $availTimeStr ($availCount slot${if (availCount == 1) "" else "s"})",
                                        fontSize = 12.5.sp,
                                        fontFamily = GillSansBold,
                                        color = Color.Black
                                    )
                                    Text(
                                        text = "Excluded: $excludedCount slot${if (excludedCount == 1) "" else "s"} ; $slotsListStr",
                                        fontSize = 11.5.sp,
                                        fontFamily = GillSans,
                                        color = Color(0xFF585858)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

// ── Availability Helper Functions ─────────────────────────────────────────────
private fun parseTimeToMinutes(timeStr: String): Int {
    val clean = timeStr.trim()
    if (clean.isEmpty()) return 0
    return try {
        val sdf = SimpleDateFormat(if (clean.contains("AM", true) || clean.contains("PM", true)) "hh:mm a" else "H:mm", Locale.US)
        val d = sdf.parse(clean) ?: return 0
        val cal = Calendar.getInstance().apply { this.time = d }
        cal.get(Calendar.HOUR_OF_DAY) * 60 + cal.get(Calendar.MINUTE)
    } catch (e: Exception) {
        0
    }
}

private fun isDurationAtLeast4Hours(fromTime: String, toTime: String): Boolean {
    val fromMin = parseTimeToMinutes(fromTime)
    val toMin = parseTimeToMinutes(toTime)
    val diff = if (toMin >= fromMin) toMin - fromMin else (toMin + 24 * 60) - fromMin
    return diff >= 240 // 4 hours = 240 mins
}

private fun generate30MinSlots(fromTime: String, toTime: String): List<String> {
    val slots = mutableListOf<String>()
    try {
        val sdfInFrom = SimpleDateFormat(if (fromTime.contains("AM", true) || fromTime.contains("PM", true)) "hh:mm a" else "H:mm", Locale.US)
        val sdfInTo = SimpleDateFormat(if (toTime.contains("AM", true) || toTime.contains("PM", true)) "hh:mm a" else "H:mm", Locale.US)
        val sdfOut = SimpleDateFormat("hh:mm a", Locale.US)

        val from = sdfInFrom.parse(fromTime.trim()) ?: return slots
        val to = sdfInTo.parse(toTime.trim()) ?: return slots

        val start = Calendar.getInstance().apply { time = from }
        val end = Calendar.getInstance().apply {
            time = to
            if (before(start)) add(Calendar.DATE, 1)
        }

        while (start.before(end)) {
            val slotEnd = (start.clone() as Calendar).apply { add(Calendar.MINUTE, 30) }
            if (slotEnd.after(end)) break
            slots.add("${sdfOut.format(start.time)} - ${sdfOut.format(slotEnd.time)}")
            start.time = slotEnd.time
        }
    } catch (e: Exception) {
        e.printStackTrace()
    }
    return slots
}

private fun validateMinimumDuration(fromTime: String, toTime: String): String {
    try {
        val sdfInFrom = SimpleDateFormat(if (fromTime.contains("AM", true) || fromTime.contains("PM", true)) "hh:mm a" else "H:mm", Locale.US)
        val sdfInTo = SimpleDateFormat(if (toTime.contains("AM", true) || toTime.contains("PM", true)) "hh:mm a" else "H:mm", Locale.US)
        val from = sdfInFrom.parse(fromTime.trim()) ?: return toTime
        val to = sdfInTo.parse(toTime.trim()) ?: return toTime

        val startCal = Calendar.getInstance().apply { time = from }
        val endCal = Calendar.getInstance().apply { time = to }

        val diffMin = (endCal.timeInMillis - startCal.timeInMillis) / (1000 * 60)
        if (diffMin < 30) {
            startCal.add(Calendar.MINUTE, 30)
            val outSdf = SimpleDateFormat("hh:mm a", Locale.US)
            return outSdf.format(startCal.time)
        }
    } catch (e: Exception) {
        e.printStackTrace()
    }
    return toTime
}

private fun filterInvalidExcludedSlots(selectedSlots: List<String>, fromTime: String, toTime: String): List<String> {
    if (selectedSlots.isEmpty()) return emptyList()
    val valid = generate30MinSlots(fromTime, toTime).toSet()
    return selectedSlots.filter { valid.contains(it) }
}
