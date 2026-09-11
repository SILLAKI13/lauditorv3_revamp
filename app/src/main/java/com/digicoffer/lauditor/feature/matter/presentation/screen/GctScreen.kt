package com.digicoffer.lauditor.feature.matter.presentation.screen

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.withStyle
import com.digicoffer.lauditor.CommonFiles.GlobalFiles.AndroidUtils
import com.digicoffer.lauditor.CommonFiles.GlobalFiles.Constants
import com.digicoffer.lauditor.Matter.Models.ClientsModel
import com.digicoffer.lauditor.Matter.Models.TeamModel
import com.digicoffer.lauditor.Matter.Models.ViewMatterModel
import com.digicoffer.lauditor.R
import com.digicoffer.lauditor.core.designsystem.colors.ColorTokens
import com.digicoffer.lauditor.core.ui.common.dialogs.AppDialog
import com.digicoffer.lauditor.core.ui.common.dropdowns.AppDropdown
import com.digicoffer.lauditor.core.ui.common.feedback.AppLoader
import com.digicoffer.lauditor.feature.matter.presentation.viewmodel.MatterEditViewModel

private val GillSans = FontFamily(Font(R.font.gill_sans))
private val GillSansBold = FontFamily(Font(R.font.gill_sans_bold))

@Composable
private fun AddClientFormLabel(text: String, isMandatory: Boolean = true) {
    Text(
        text = buildAnnotatedString {
            append(text)
            if (isMandatory) {
                withStyle(style = SpanStyle(color = Color.Red, fontWeight = FontWeight.Bold)) {
                    append(" *")
                }
            }
        },
        fontFamily = GillSansBold,
        fontSize = 14.sp,
        color = ColorTokens.BluePrimary,
        modifier = Modifier.padding(bottom = 4.dp)
    )
}

@Composable
fun GctScreen(
    editModel: ViewMatterModel?,
    onNavigateBack: () -> Unit,
    onNavigateNext: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: MatterEditViewModel
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current

    // Fetch team members and countries on initialization
    LaunchedEffect(Unit) {
        viewModel.loadTeamMembers()
        viewModel.loadCountries()
    }

    // Dropdown checked state map (tm_id -> isChecked)
    val checkedDropdownMembers = remember { mutableStateMapOf<String, Boolean>() }

    // Filtered team members for selection dropdown
    val filteredDropdownMembers = remember(uiState.teamMembersList, uiState.selectedTeamMembers) {
        uiState.teamMembersList.filter { member ->
            val isOwner = member.tm_id == Constants.owner_id
            val isCurrentUser = member.tm_id == Constants.USER_ID
            val isAlreadySelected = uiState.selectedTeamMembers.any { it.tm_id == member.tm_id }

            // Apply legacy exclusion rules and avoid duplicates
            if (Constants.create_matter) {
                !isCurrentUser && !isAlreadySelected
            } else {
                !isOwner && !isCurrentUser && !isAlreadySelected
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
                // Header (Matter Title + Close Button)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = if (Constants.create_matter) (uiState.title.ifEmpty { "Matter" }) else "",
                        fontFamily = GillSansBold,
                        fontSize = 20.sp,
                        color = ColorTokens.BluePrimary,
                        modifier = Modifier.weight(1f)
                    )
                    IconButton(
                        onClick = {
                            viewModel.resetTransientState()
                            onNavigateBack()
                        },
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            painter = painterResource(id = R.drawable.cancel_icon_1),
                            contentDescription = "Cancel",
                            tint = Color.Unspecified,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Section 1: Add Client(s)
                Text(
                    text = "Add Client(s)",
                    fontFamily = GillSansBold,
                    fontSize = 16.sp,
                    color = ColorTokens.BluePrimary
                )

                Spacer(modifier = Modifier.height(6.dp))

                var showSearchSuggestionsDropdown by remember { mutableStateOf(false) }

                LaunchedEffect(uiState.searchResults) {
                    if (uiState.searchResults.isNotEmpty()) {
                        showSearchSuggestionsDropdown = true
                    }
                }

                // Search Bar Box with Dropdown Overlay
                Box(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.fillMaxWidth()) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .height(42.dp)
                                    .border(1.dp, Color(0xFFDDDDDE), RoundedCornerShape(4.dp))
                                    .background(Color(0xFFFAFAFA))
                                    .padding(horizontal = 10.dp),
                                contentAlignment = Alignment.CenterStart
                            ) {
                                BasicTextField(
                                    value = uiState.searchQuery,
                                    onValueChange = { viewModel.onSearchQueryChanged(it) },
                                    singleLine = true,
                                    textStyle = TextStyle(
                                        fontFamily = GillSans,
                                        fontSize = 15.sp,
                                        color = Color.Black
                                    ),
                                    modifier = Modifier.fillMaxWidth(),
                                    decorationBox = { innerTextField ->
                                        if (uiState.searchQuery.isEmpty()) {
                                            Text(
                                                text = "Search",
                                                fontFamily = GillSans,
                                                fontSize = 15.sp,
                                                color = Color.Gray
                                            )
                                        }
                                        innerTextField()
                                    }
                                )
                            }

                            Button(
                                onClick = { viewModel.searchClients() },
                                colors = ButtonDefaults.buttonColors(containerColor = ColorTokens.BluePrimary),
                                shape = RoundedCornerShape(4.dp),
                                modifier = Modifier.height(42.dp),
                                contentPadding = PaddingValues(horizontal = 20.dp)
                            ) {
                                Text(
                                    text = "Search",
                                    fontFamily = GillSansBold,
                                    fontSize = 15.sp,
                                    color = Color.White
                                )
                            }
                        }
                    }

                    // Suggestions Overlay Box
                    val filteredSearchResults = uiState.searchResults.filter { result ->
                        !uiState.selectedClients.any { it.client_id == result.client_id }
                    }
                    if (showSearchSuggestionsDropdown && filteredSearchResults.isNotEmpty()) {
                        Card(
                            shape = RoundedCornerShape(4.dp),
                            colors = CardDefaults.cardColors(containerColor = Color.White),
                            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
                            border = BorderStroke(1.dp, Color(0xFFDDDDDE)),
                            modifier = Modifier
                                .padding(top = 46.dp)
                                .fillMaxWidth(0.80f)
                                .heightIn(max = 240.dp)
                        ) {
                            LazyColumn(
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                items(filteredSearchResults) { result ->
                                    Column(
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .clickable {
                                                    viewModel.addClient(result)
                                                    showSearchSuggestionsDropdown = false
                                                }
                                                .padding(horizontal = 16.dp, vertical = 12.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Text(
                                                text = result.client_name ?: "",
                                                fontFamily = GillSans,
                                                fontSize = 15.sp,
                                                color = Color.Black,
                                                modifier = Modifier.fillMaxWidth()
                                            )
                                        }
                                        HorizontalDivider(color = Color(0xFFEEEEEE), thickness = 1.dp)
                                    }
                                }
                            }
                        }
                    }
                }

                // Loading Indicator for Search
                if (uiState.isSearchingClient) {
                    Spacer(modifier = Modifier.height(10.dp))
                    Box(
                        modifier = Modifier.fillMaxWidth(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(color = ColorTokens.BluePrimary)
                    }
                }

                // Client Not Found Banner & Add New Client Form
                if (uiState.clientNotFoundQuery != null && uiState.isAddClientFormVisible) {
                    var selectedClientType by remember { mutableStateOf("consumer") }
                    var tempFirstName by remember(uiState.clientNotFoundQuery) { mutableStateOf(uiState.clientNotFoundQuery ?: "") }
                    var tempLastName by remember(uiState.clientNotFoundQuery) { mutableStateOf("") }
                    var tempEmail by remember(uiState.clientNotFoundQuery) { mutableStateOf("") }
                    var tempConfirmEmail by remember(uiState.clientNotFoundQuery) { mutableStateOf("") }
                    var tempPhone by remember(uiState.clientNotFoundQuery) { mutableStateOf("") }
                    var tempCountry by remember(uiState.clientNotFoundQuery) { mutableStateOf("") }
                    var countryDropdownExpanded by remember { mutableStateOf(false) }

                    Column(modifier = Modifier.fillMaxWidth()) {
                        // 1. Red warning message
                        Text(
                            text = "${uiState.clientNotFoundQuery} - not found. Please fill in the details below to send relationship invite.",
                            fontFamily = GillSans,
                            fontSize = 13.sp,
                            color = Color(0xFFD32F2F),
                            modifier = Modifier.padding(top = 8.dp, bottom = 12.dp)
                        )

                        // 2. Client Type Toggle
                        Text(
                            text = "Client Type",
                            fontFamily = GillSansBold,
                            fontSize = 15.sp,
                            color = ColorTokens.BluePrimary,
                            modifier = Modifier.padding(bottom = 6.dp)
                        )

                        Row(
                            modifier = Modifier
                                .width(200.dp)
                                .height(38.dp)
                                .background(Color(0xFFECEFF1), RoundedCornerShape(20.dp))
                                .padding(2.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .fillMaxHeight()
                                    .background(
                                        if (selectedClientType == "consumer") ColorTokens.BluePrimary else Color.Transparent,
                                        RoundedCornerShape(18.dp)
                                    )
                                    .clickable { selectedClientType = "consumer" },
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "Individual",
                                    fontFamily = if (selectedClientType == "consumer") GillSansBold else GillSans,
                                    fontSize = 14.sp,
                                    color = if (selectedClientType == "consumer") Color.White else Color.Black
                                )
                            }
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .fillMaxHeight()
                                    .background(
                                        if (selectedClientType == "entity") ColorTokens.BluePrimary else Color.Transparent,
                                        RoundedCornerShape(18.dp)
                                    )
                                    .clickable { selectedClientType = "entity" },
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "Entity",
                                    fontFamily = if (selectedClientType == "entity") GillSansBold else GillSans,
                                    fontSize = 14.sp,
                                    color = if (selectedClientType == "entity") Color.White else Color.Black
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        if (selectedClientType == "consumer") {
                            // First Name
                            AddClientFormLabel(text = "First Name", isMandatory = true)
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(42.dp)
                                    .border(1.dp, Color(0xFFDDDDDE), RoundedCornerShape(4.dp))
                                    .background(Color.White)
                                    .padding(horizontal = 10.dp),
                                contentAlignment = Alignment.CenterStart
                            ) {
                                BasicTextField(
                                    value = tempFirstName,
                                    onValueChange = { tempFirstName = it },
                                    singleLine = true,
                                    textStyle = TextStyle(fontFamily = GillSans, fontSize = 15.sp, color = Color.Black),
                                    modifier = Modifier.fillMaxWidth(),
                                    decorationBox = { inner ->
                                        if (tempFirstName.isEmpty()) {
                                            Text(text = "First Name", fontFamily = GillSans, fontSize = 15.sp, color = Color.Gray)
                                        }
                                        inner()
                                    }
                                )
                            }

                            Spacer(modifier = Modifier.height(10.dp))

                            // Last Name
                            AddClientFormLabel(text = "Last Name", isMandatory = true)
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(42.dp)
                                    .border(1.dp, Color(0xFFDDDDDE), RoundedCornerShape(4.dp))
                                    .background(Color.White)
                                    .padding(horizontal = 10.dp),
                                contentAlignment = Alignment.CenterStart
                            ) {
                                BasicTextField(
                                    value = tempLastName,
                                    onValueChange = { tempLastName = it },
                                    singleLine = true,
                                    textStyle = TextStyle(fontFamily = GillSans, fontSize = 15.sp, color = Color.Black),
                                    modifier = Modifier.fillMaxWidth(),
                                    decorationBox = { inner ->
                                        if (tempLastName.isEmpty()) {
                                            Text(text = "Last Name", fontFamily = GillSans, fontSize = 15.sp, color = Color.Gray)
                                        }
                                        inner()
                                    }
                                )
                            }
                        } else {
                            // Firm Name
                            AddClientFormLabel(text = "Firm Name", isMandatory = true)
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(42.dp)
                                    .border(1.dp, Color(0xFFDDDDDE), RoundedCornerShape(4.dp))
                                    .background(Color.White)
                                    .padding(horizontal = 10.dp),
                                contentAlignment = Alignment.CenterStart
                            ) {
                                BasicTextField(
                                    value = tempFirstName,
                                    onValueChange = { tempFirstName = it },
                                    singleLine = true,
                                    textStyle = TextStyle(fontFamily = GillSans, fontSize = 15.sp, color = Color.Black),
                                    modifier = Modifier.fillMaxWidth(),
                                    decorationBox = { inner ->
                                        if (tempFirstName.isEmpty()) {
                                            Text(text = "Firm Name", fontFamily = GillSans, fontSize = 15.sp, color = Color.Gray)
                                        }
                                        inner()
                                    }
                                )
                            }

                            Spacer(modifier = Modifier.height(10.dp))

                            // Contact Person
                            AddClientFormLabel(text = "Contact Person", isMandatory = true)
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(42.dp)
                                    .border(1.dp, Color(0xFFDDDDDE), RoundedCornerShape(4.dp))
                                    .background(Color.White)
                                    .padding(horizontal = 10.dp),
                                contentAlignment = Alignment.CenterStart
                            ) {
                                BasicTextField(
                                    value = tempLastName,
                                    onValueChange = { tempLastName = it },
                                    singleLine = true,
                                    textStyle = TextStyle(fontFamily = GillSans, fontSize = 15.sp, color = Color.Black),
                                    modifier = Modifier.fillMaxWidth(),
                                    decorationBox = { inner ->
                                        if (tempLastName.isEmpty()) {
                                            Text(text = "Contact Person", fontFamily = GillSans, fontSize = 15.sp, color = Color.Gray)
                                        }
                                        inner()
                                    }
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        // Email Address
                        AddClientFormLabel(text = "Email Address", isMandatory = true)
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(42.dp)
                                .border(1.dp, Color(0xFFDDDDDE), RoundedCornerShape(4.dp))
                                .background(Color.White)
                                .padding(horizontal = 10.dp),
                            contentAlignment = Alignment.CenterStart
                        ) {
                            BasicTextField(
                                value = tempEmail,
                                onValueChange = { tempEmail = it },
                                singleLine = true,
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                                textStyle = TextStyle(fontFamily = GillSans, fontSize = 15.sp, color = Color.Black),
                                modifier = Modifier.fillMaxWidth(),
                                decorationBox = { inner ->
                                    if (tempEmail.isEmpty()) {
                                        Text(text = "Email Address", fontFamily = GillSans, fontSize = 15.sp, color = Color.Gray)
                                    }
                                    inner()
                                }
                            )
                        }
                        val isEmailValid = tempEmail.isEmpty() || AndroidUtils.isValidEmail(tempEmail.trim())
                        if (!isEmailValid) {
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Please Enter A Valid Email Address",
                                color = Color(0xFF585858),
                                fontSize = 12.sp,
                                fontFamily = GillSans,
                                modifier = Modifier.padding(start = 2.dp)
                            )
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        // Confirm Email Address
                        AddClientFormLabel(text = "Confirm Email Address", isMandatory = true)
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(42.dp)
                                .border(1.dp, Color(0xFFDDDDDE), RoundedCornerShape(4.dp))
                                .background(Color.White)
                                .padding(horizontal = 10.dp),
                            contentAlignment = Alignment.CenterStart
                        ) {
                            BasicTextField(
                                value = tempConfirmEmail,
                                onValueChange = { tempConfirmEmail = it },
                                singleLine = true,
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                                textStyle = TextStyle(fontFamily = GillSans, fontSize = 15.sp, color = Color.Black),
                                modifier = Modifier.fillMaxWidth(),
                                decorationBox = { inner ->
                                    if (tempConfirmEmail.isEmpty()) {
                                        Text(text = "Confirm Email Address", fontFamily = GillSans, fontSize = 15.sp, color = Color.Gray)
                                    }
                                    inner()
                                }
                            )
                        }
                        val isConfirmEmailValid = tempConfirmEmail.isEmpty() || AndroidUtils.isValidEmail(tempConfirmEmail.trim())
                        if (tempConfirmEmail.isNotEmpty()) {
                            if (!isConfirmEmailValid) {
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "Please Enter A Valid Email Address",
                                    color = Color(0xFF585858),
                                    fontSize = 12.sp,
                                    fontFamily = GillSans,
                                    modifier = Modifier.padding(start = 2.dp)
                                )
                            } else if (tempEmail.isNotEmpty() && tempEmail.trim() != tempConfirmEmail.trim()) {
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "Please Enter A Valid Confirm Email Address",
                                    color = Color(0xFF585858),
                                    fontSize = 12.sp,
                                    fontFamily = GillSans,
                                    modifier = Modifier.padding(start = 2.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        // Country
                        AddClientFormLabel(text = "Country", isMandatory = true)
                        Box(modifier = Modifier.fillMaxWidth()) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(42.dp)
                                    .border(1.dp, Color(0xFFDDDDDE), RoundedCornerShape(4.dp))
                                    .background(Color.White)
                                    .clickable { countryDropdownExpanded = !countryDropdownExpanded }
                                    .padding(horizontal = 10.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = tempCountry.ifEmpty { "Select Country" },
                                    fontFamily = GillSans,
                                    fontSize = 15.sp,
                                    color = if (tempCountry.isEmpty()) Color.Gray else Color.Black
                                )
                                Icon(
                                    painter = painterResource(id = R.drawable.down_arrow),
                                    contentDescription = "Dropdown",
                                    tint = ColorTokens.BluePrimary,
                                    modifier = Modifier.size(16.dp)
                                )
                            }

                            DropdownMenu(
                                expanded = countryDropdownExpanded,
                                onDismissRequest = { countryDropdownExpanded = false },
                                modifier = Modifier
                                    .fillMaxWidth(0.9f)
                                    .heightIn(max = 250.dp)
                                    .background(Color.White)
                            ) {
                                uiState.countriesList.forEach { countryPair ->
                                    DropdownMenuItem(
                                        text = {
                                            Text(
                                                text = countryPair.second,
                                                fontFamily = GillSans,
                                                fontSize = 15.sp,
                                                color = Color.Black
                                            )
                                        },
                                        onClick = {
                                            tempCountry = countryPair.second
                                            countryDropdownExpanded = false
                                        }
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        // Phone Number (Optional)
                        AddClientFormLabel(text = "Phone Number", isMandatory = false)
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(42.dp)
                                .border(1.dp, Color(0xFFDDDDDE), RoundedCornerShape(4.dp))
                                .background(Color.White)
                                .padding(horizontal = 10.dp),
                            contentAlignment = Alignment.CenterStart
                        ) {
                            BasicTextField(
                                value = tempPhone,
                                onValueChange = { input ->
                                    if (input.isEmpty() || input.all { it.isDigit() }) {
                                        if (input.length <= 15) {
                                            tempPhone = input
                                        }
                                    }
                                },
                                singleLine = true,
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                                textStyle = TextStyle(fontFamily = GillSans, fontSize = 15.sp, color = Color.Black),
                                modifier = Modifier.fillMaxWidth(),
                                decorationBox = { inner ->
                                    if (tempPhone.isEmpty()) {
                                        Text(text = "Phone Number", fontFamily = GillSans, fontSize = 15.sp, color = Color.Gray)
                                    }
                                    inner()
                                }
                            )
                        }
                        val isPhoneValid = tempPhone.isEmpty() || tempPhone.trim().length >= 10
                        if (tempPhone.isNotEmpty() && !isPhoneValid) {
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Please enter a 10 digit valid mobile number.",
                                color = Color(0xFF585858),
                                fontSize = 12.sp,
                                fontFamily = GillSans,
                                modifier = Modifier.padding(start = 2.dp)
                            )
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        // Action Buttons
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.Center,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Button(
                                onClick = { viewModel.dismissClientNotFound() },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = Color(0xFFECEFF1)
                                ),
                                shape = RoundedCornerShape(6.dp),
                                modifier = Modifier
                                    .width(130.dp)
                                    .height(42.dp)
                            ) {
                                Text(
                                    text = "Cancel",
                                    fontFamily = GillSansBold,
                                    fontSize = 15.sp,
                                    color = Color.Black
                                )
                            }

                            Spacer(modifier = Modifier.width(16.dp))

                            val isEmailStrictValid = tempEmail.isNotBlank() && AndroidUtils.isValidEmail(tempEmail.trim())
                            val isConfirmEmailStrictValid = tempConfirmEmail.isNotBlank() && AndroidUtils.isValidEmail(tempConfirmEmail.trim()) && tempConfirmEmail.trim() == tempEmail.trim()
                            val isPhoneStrictValid = tempPhone.isBlank() || tempPhone.trim().length >= 10
                            val isFormFilled = tempFirstName.isNotBlank() && tempLastName.isNotBlank() &&
                                    isEmailStrictValid && isConfirmEmailStrictValid && tempCountry.isNotBlank() && isPhoneStrictValid

                            Button(
                                onClick = {
                                    viewModel.inviteClient(
                                        clientType = selectedClientType,
                                        firstName = tempFirstName,
                                        lastName = tempLastName,
                                        email = tempEmail,
                                        confirmEmail = tempConfirmEmail,
                                        phone = tempPhone,
                                        country = tempCountry
                                    )
                                },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = if (isFormFilled) ColorTokens.BluePrimary else Color(0xFF8FAECB),
                                    disabledContainerColor = Color(0xFF8FAECB)
                                ),
                                shape = RoundedCornerShape(6.dp),
                                modifier = Modifier
                                    .width(150.dp)
                                    .height(42.dp)
                            ) {
                                Text(
                                    text = "Add as Client",
                                    fontFamily = GillSansBold,
                                    fontSize = 15.sp,
                                    color = Color.White
                                )
                            }
                        }
                    }
                }

                // Selected Clients Section
                if (uiState.selectedClients.isNotEmpty()) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 12.dp, bottom = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Selected Client(s)",
                            fontFamily = GillSansBold,
                            fontSize = 16.sp,
                            color = ColorTokens.BluePrimary
                        )
                        Text(
                            text = " *",
                            fontFamily = GillSansBold,
                            fontSize = 16.sp,
                            color = Color.Red
                        )
                    }

                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        uiState.selectedClients.forEach { client ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(Color(0xFFEEEEEE), RoundedCornerShape(6.dp))
                                    .border(1.dp, Color(0xFFDDDDDE), RoundedCornerShape(6.dp))
                                    .padding(horizontal = 12.dp, vertical = 8.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = client.client_name ?: "",
                                    fontFamily = GillSans,
                                    fontSize = 15.sp,
                                    color = Color.Black
                                )
                                Image(
                                    painter = painterResource(id = R.drawable.cancel_red_icon),
                                    contentDescription = "Remove Client",
                                    modifier = Modifier
                                        .size(20.dp)
                                        .clickable { viewModel.removeClient(client) }
                                )
                            }
                        }
                    }
                }

                if (Constants.CATEGORY != "solo") {
                    Spacer(modifier = Modifier.height(15.dp))

                    // Section 2: Assign Team Member(s)
                    Text(
                        text = "Assign Team Member(s)",
                        fontFamily = GillSansBold,
                        fontSize = 16.sp,
                        color = ColorTokens.BluePrimary
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    val assignedNames = uiState.selectedTeamMembers.map { it.tm_name ?: "" }
                    val dropdownHeaderText = "Select Assign Team Member(s)"

                    AppDropdown(
                        options = filteredDropdownMembers,
                        selectedOption = null,
                        onOptionSelected = {},
                        isInline = true,
                        isExpanded = uiState.isTeamMembersDropdownExpanded,
                        onExpandedChange = { viewModel.toggleTeamMembersDropdown() },
                        customHeader = {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(42.dp)
                                    .border(1.dp, Color(0xFFDDDDDE), RoundedCornerShape(4.dp))
                                    .background(Color(0xFFFAFAFA))
                                    .clickable { viewModel.toggleTeamMembersDropdown() }
                                    .padding(horizontal = 10.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = dropdownHeaderText,
                                    fontFamily = GillSans,
                                    fontSize = 15.sp,
                                    color = Color.Gray
                                )
                                Icon(
                                    painter = painterResource(id = R.drawable.drop_down_blue),
                                    contentDescription = "Toggle Dropdown",
                                    tint = Color.Black,
                                    modifier = Modifier
                                        .size(14.dp)
                                        .rotate(if (uiState.isTeamMembersDropdownExpanded) 180f else 0f)
                                )
                            }
                        },
                        customContent = {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(top = 4.dp)
                                    .border(1.dp, Color(0xFFDDDDDE), RoundedCornerShape(4.dp))
                                    .background(Color(0xFFFAFAFA))
                                    .padding(8.dp)
                            ) {
                                if (filteredDropdownMembers.isEmpty()) {
                                    Text(
                                        text = "No team members available",
                                        fontFamily = GillSans,
                                        fontSize = 14.sp,
                                        color = Color.Gray,
                                        modifier = Modifier.padding(8.dp)
                                    )
                                } else {
                                    filteredDropdownMembers.forEach { member ->
                                        val isChecked = checkedDropdownMembers[member.tm_id] ?: false
                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .clickable {
                                                    checkedDropdownMembers[member.tm_id ?: ""] = !isChecked
                                                }
                                                .padding(horizontal = 8.dp, vertical = 6.dp),
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.SpaceBetween
                                        ) {
                                            Text(
                                                text = member.tm_name ?: "",
                                                fontFamily = GillSans,
                                                fontSize = 15.sp,
                                                color = Color.Black
                                            )
                                            Checkbox(
                                                checked = isChecked,
                                                onCheckedChange = { checkedDropdownMembers[member.tm_id ?: ""] = it },
                                                colors = CheckboxDefaults.colors(checkedColor = ColorTokens.BluePrimary)
                                            )
                                        }
                                    }

                                    Spacer(modifier = Modifier.height(10.dp))

                                    // Dropdown Add Button
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.End
                                    ) {
                                        Button(
                                            onClick = {
                                                filteredDropdownMembers.forEach { member ->
                                                    if (checkedDropdownMembers[member.tm_id] == true) {
                                                        viewModel.addTeamMember(member)
                                                    }
                                                }
                                                checkedDropdownMembers.clear()
                                                viewModel.setTeamMembersDropdownExpanded(false)
                                            },
                                            colors = ButtonDefaults.buttonColors(
                                                containerColor = ColorTokens.BluePrimary,
                                                disabledContainerColor = Color(0xFFBDC7D0)
                                            ),
                                            shape = RoundedCornerShape(4.dp),
                                            contentPadding = PaddingValues(horizontal = 24.dp)
                                        ) {
                                            Text(
                                                text = "Add",
                                                fontFamily = GillSansBold,
                                                fontSize = 14.sp,
                                                color = Color.White
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    )

                    // Selected Team Members Rows (Assigned Team Members)
                    if (uiState.selectedTeamMembers.isNotEmpty()) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 10.dp),
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Text(
                                text = "Assigned Team Member(s)",
                                fontFamily = GillSansBold,
                                fontSize = 16.sp,
                                color = ColorTokens.BluePrimary,
                                modifier = Modifier.padding(top = 12.dp, bottom = 6.dp)
                            )

                            uiState.selectedTeamMembers.forEach { member ->
                                val isRemovable = viewModel.isMemberRemovable(member)
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .background(Color(0xFFEEEEEE), RoundedCornerShape(6.dp))
                                        .border(1.dp, Color(0xFFDDDDDE), RoundedCornerShape(6.dp))
                                        .padding(horizontal = 12.dp, vertical = 8.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = member.tm_name ?: "",
                                        fontFamily = GillSans,
                                        fontSize = 15.sp,
                                        color = Color.Black
                                    )
                                    if (isRemovable) {
                                        Image(
                                            painter = painterResource(id = R.drawable.cancel_red_icon),
                                            contentDescription = "Remove Member",
                                            modifier = Modifier
                                                .size(20.dp)
                                                .clickable { viewModel.removeTeamMember(member) }
                                        )
                                    }
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))
                }

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 16.dp, bottom = 8.dp),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Save for Later Button
                    Button(
                        onClick = {
                            viewModel.saveClientsAndMembers(isSaveLater = true) {
                                onNavigateBack()
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFE8E8E8)),
                        border = BorderStroke(1.dp, Color(0xFF888888)),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier
                            .width(135.dp)
                            .height(40.dp),
                        contentPadding = PaddingValues(0.dp)
                    ) {
                        Text(
                            text = "Save For Later",
                            fontFamily = GillSansBold,
                            fontSize = 13.sp,
                            color = Color.Black
                        )
                    }

                    Spacer(modifier = Modifier.width(20.dp))

                    // Save & Next Button
                    Button(
                        onClick = {
                            viewModel.saveClientsAndMembers(isSaveLater = false) {
                                onNavigateNext()
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = ColorTokens.BluePrimary),
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
    }

    if (uiState.alertMessage != null) {
        AppDialog(
            title = uiState.alertTitle ?: "Alert",
            onDismiss = { viewModel.dismissAlert() },
            onConfirm = { viewModel.dismissAlert() },
            confirmText = "OK"
        ) {
            Text(
                text = uiState.alertMessage ?: "",
                fontFamily = GillSans,
                fontSize = 15.sp,
                textAlign = TextAlign.Center,
                color = Color.Black
            )
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
