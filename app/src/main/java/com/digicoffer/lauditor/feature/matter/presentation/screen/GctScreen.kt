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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.draw.rotate
import com.digicoffer.lauditor.CommonFiles.GlobalFiles.Constants
import com.digicoffer.lauditor.Matter.Models.ClientsModel
import com.digicoffer.lauditor.Matter.Models.TeamModel
import com.digicoffer.lauditor.Matter.Models.ViewMatterModel
import com.digicoffer.lauditor.R
import com.digicoffer.lauditor.core.designsystem.colors.ColorTokens
import com.digicoffer.lauditor.core.ui.common.feedback.AppLoader
import com.digicoffer.lauditor.core.ui.common.dropdowns.AppDropdown
import com.digicoffer.lauditor.feature.matter.presentation.viewmodel.MatterEditViewModel
import androidx.compose.foundation.text.BasicTextField

private val GillSans = FontFamily(Font(R.font.gill_sans))
private val GillSansBold = FontFamily(Font(R.font.gill_sans_bold))

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

    // Fetch team members on initialization
    LaunchedEffect(Unit) {
        viewModel.loadTeamMembers()
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
            .fillMaxWidth()
            .wrapContentHeight()
            .background(ColorTokens.LightBlueBg)
            .padding(10.dp)
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth(),
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
                        onClick = { onNavigateBack() },
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
                                    .border(1.dp, Color(0xFFE8E8E8), RoundedCornerShape(4.dp))
                                    .background(Color.White)
                                    .padding(horizontal = 12.dp, vertical = 10.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = client.client_name ?: "",
                                    fontFamily = GillSans,
                                    fontSize = 15.sp,
                                    color = Color.Black
                                )
                                IconButton(
                                    onClick = { viewModel.removeClient(client) },
                                    modifier = Modifier.size(24.dp)
                                ) {
                                    Icon(
                                        painter = painterResource(id = R.drawable.cancel_icon),
                                        contentDescription = "Remove Client",
                                        tint = Color.Red,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            }
                        }
                    }
                }

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
                val newlyCheckedNames = uiState.teamMembersList
                    .filter { checkedDropdownMembers[it.tm_id] == true }
                    .map { it.tm_name ?: "" }
                val allNames = (assignedNames + newlyCheckedNames).distinct()
                val dropdownHeaderText = if (allNames.isNotEmpty()) {
                    allNames.joinToString(", ")
                } else {
                    "Select Assign Team Member(s)"
                }

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
                                color = if (allNames.isNotEmpty()) Color.Black else Color.Gray
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
                                            viewModel.toggleTeamMembersDropdown()
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

                // Selected Team Members Rows
                if (uiState.selectedTeamMembers.isNotEmpty()) {
                    Column(
                        modifier = Modifier.fillMaxWidth().padding(top = 10.dp),
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
                                    .border(1.dp, Color(0xFFE8E8E8), RoundedCornerShape(4.dp))
                                    .background(Color.White)
                                    .padding(horizontal = 12.dp, vertical = 10.dp),
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
                                    IconButton(
                                        onClick = { viewModel.removeTeamMember(member) },
                                        modifier = Modifier.size(24.dp)
                                    ) {
                                        Icon(
                                            painter = painterResource(id = R.drawable.cancel_icon),
                                            contentDescription = "Remove Member",
                                            tint = Color.Red,
                                            modifier = Modifier.size(18.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
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
    if (uiState.isLoading) {
        AppLoader()
    }
}
