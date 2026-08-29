package com.digicoffer.lauditor.feature.groups.presentation.screen

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.digicoffer.lauditor.Groups.Models.GroupModel
import com.digicoffer.lauditor.Groups.Models.ViewGroupModel
import com.digicoffer.lauditor.R
import com.digicoffer.lauditor.feature.groups.presentation.components.*
import com.digicoffer.lauditor.feature.groups.presentation.state.GroupsUiEvent
import com.digicoffer.lauditor.feature.groups.presentation.viewmodel.GroupsViewModel
import com.digicoffer.lauditor.core.ui.common.buttons.AppHeaderButton
import com.digicoffer.lauditor.feature.members.presentation.components.MembersAlertDialog
import com.digicoffer.lauditor.feature.notifications.presentation.components.NotificationsSearchBar
import org.json.JSONObject

enum class ScreenMode {
    LIST,
    CREATE,
    EDIT,
    UPDATE_MEMBERS,
    UPDATE_GROUP_HEAD,
    ACTIVITY_LOG,
    DELETE
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GroupsScreen(
    viewModel: GroupsViewModel = viewModel(),
    onBack: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    var screenMode by remember { mutableStateOf(ScreenMode.LIST) }
    var selectedGroup by remember { mutableStateOf<ViewGroupModel?>(null) }

    var searchQuery by remember { mutableStateOf("") }
    val activeBlue = Color(0xFF004D87)

    // Custom Alert/Dialog states
    var alertMessage by remember { mutableStateOf<String?>(null) }
    var successMessage by remember { mutableStateOf<String?>(null) }

    // Create Group tab state lifted to screen scope
    var isAddTeamMemberTab by remember { mutableStateOf(true) }

    // Deletion states
    var deleteCounts by remember { mutableStateOf<JSONObject?>(null) }
    var reassignedGroupId by remember { mutableStateOf("") }
    var reassignedGroupName by remember { mutableStateOf("Select Group Name") }
    var isReassignExpanded by remember { mutableStateOf(false) }

    // Trigger loads
    LaunchedEffect(Unit) {
        viewModel.onEvent(GroupsUiEvent.LoadGroups)
        viewModel.onEvent(GroupsUiEvent.LoadMembers)
    }

    // Capture repository success/error toast actions and route them via MembersAlertDialog
    LaunchedEffect(uiState.toastMessage) {
        uiState.toastMessage?.let { msg ->
            if (msg.contains("successfully", ignoreCase = true) || msg.contains("success", ignoreCase = true) || msg.contains("updated", ignoreCase = true) || msg.contains("deleted", ignoreCase = true)) {
                successMessage = msg
            } else {
                alertMessage = msg
            }
            viewModel.onEvent(GroupsUiEvent.DismissToast)
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFE6F0FA)) // Light blue background (#E6F0FA)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
        ) {
            // Header Action Subheader Row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                val headerTitle = when (screenMode) {
                    ScreenMode.LIST -> "List Of Groups"
                    ScreenMode.CREATE -> "Create Group"
                    ScreenMode.EDIT -> "Edit Group Info"
                    ScreenMode.UPDATE_MEMBERS -> "Update Group Members"
                    ScreenMode.UPDATE_GROUP_HEAD -> "Update Group Head"
                    ScreenMode.ACTIVITY_LOG -> "Activity Log"
                    ScreenMode.DELETE -> "Assign Group"
                }

                Text(
                    text = headerTitle,
                    color = activeBlue,
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    fontFamily = FontFamily(Font(R.font.gill_sans_regular))
                )

                // Sub-header Action Button
                if (screenMode == ScreenMode.LIST) {
                    AppHeaderButton(
                        text = "Create Group",
                        iconRes = null, // draws '+'
                        onClick = {
                            isAddTeamMemberTab = true
                            screenMode = ScreenMode.CREATE
                        }
                    )
                } else {
                    AppHeaderButton(
                        text = "View Groups",
                        iconRes = R.drawable.eye_icon, // Eye icon drawable (View)
                        onClick = {
                            screenMode = ScreenMode.LIST
                        }
                    )
                }
            }

            // Screen content switcher
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
            ) {
                when (screenMode) {
                    ScreenMode.LIST -> {
                        Column(modifier = Modifier.fillMaxSize()) {
                            // Search bar
                            NotificationsSearchBar(
                                query = searchQuery,
                                onQueryChange = { searchQuery = it },
                                placeholder = "Search Groups",
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 16.dp, vertical = 8.dp)
                            )

                            // List of group cards
                            val filteredGroups = uiState.groupsList.filter {
                                it.name.contains(searchQuery, ignoreCase = true)
                            }

                            if (uiState.isLoading) {
                                Box(
                                    modifier = Modifier.fillMaxSize(),
                                    contentAlignment = Alignment.Center
                                ) {
                                    CircularProgressIndicator(color = activeBlue)
                                }
                            } else if (filteredGroups.isEmpty()) {
                                Box(
                                    modifier = Modifier.fillMaxSize(),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = "No groups found",
                                        color = Color.Gray,
                                        fontSize = 14.sp,
                                        fontFamily = FontFamily(Font(R.font.gill_sans_regular))
                                    )
                                }
                            } else {
                                LazyColumn(
                                    modifier = Modifier.fillMaxSize(),
                                    contentPadding = PaddingValues(bottom = 24.dp)
                                ) {
                                    items(filteredGroups) { group ->
                                        GroupCardItem(
                                            group = group,
                                            onEditClick = {
                                                selectedGroup = group
                                                screenMode = ScreenMode.EDIT
                                            },
                                            onUpdateMembersClick = {
                                                selectedGroup = group
                                                screenMode = ScreenMode.UPDATE_MEMBERS
                                            },
                                            onUpdateGroupHeadClick = {
                                                selectedGroup = group
                                                screenMode = ScreenMode.UPDATE_GROUP_HEAD
                                            },
                                            onDeleteClick = {
                                                selectedGroup = group
                                                reassignedGroupId = ""
                                                reassignedGroupName = "Select Group Name"
                                                viewModel.onEvent(GroupsUiEvent.FetchGroupCounts(group.id) { counts ->
                                                    deleteCounts = counts
                                                    screenMode = ScreenMode.DELETE
                                                })
                                            },
                                            onActivityLogClick = {
                                                selectedGroup = group
                                                viewModel.onEvent(GroupsUiEvent.FetchAuditLogs(group.id, "", "", "", ""))
                                                screenMode = ScreenMode.ACTIVITY_LOG
                                            }
                                        )
                                    }
                                }
                            }
                        }
                    }

                    ScreenMode.CREATE -> {
                        GroupFormCard(
                            membersList = uiState.membersList,
                            isAddTeamMemberTab = isAddTeamMemberTab,
                            onTabChange = { isAddTeamMemberTab = it },
                            onSave = { name, desc, head, members ->
                                // Filter out empty, blank, null values and ensure distinct valid IDs only
                                val cleanMembers = (members + head).filter { it.isNotBlank() }.distinct()
                                viewModel.onEvent(
                                    GroupsUiEvent.CreateGroup(name, desc, head, cleanMembers) {
                                        screenMode = ScreenMode.LIST
                                    }
                                )
                            },
                            onCancel = { screenMode = ScreenMode.LIST },
                            onValidationError = { alertMessage = it }
                        )
                    }

                    ScreenMode.EDIT -> {
                        selectedGroup?.let { group ->
                            // Key states by group to ensure correct reset when switching target
                            var editName by remember(group) { mutableStateOf(group.name ?: "") }
                            var editDesc by remember(group) { mutableStateOf(group.description ?: "") }

                            Card(
                                shape = RoundedCornerShape(10.dp),
                                colors = CardDefaults.cardColors(containerColor = Color.White),
                                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp)
                            ) {
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(16.dp)
                                ) {
                                    Text(
                                        text = "Group Name *",
                                        color = activeBlue,
                                        fontWeight = FontWeight.SemiBold,
                                        fontSize = 15.sp,
                                        fontFamily = FontFamily(Font(R.font.gill_sans_regular))
                                    )
                                    Spacer(modifier = Modifier.height(6.dp))
                                    CustomTextField(
                                        value = editName,
                                        onValueChange = { editName = it },
                                        placeholder = "Group Name"
                                    )

                                    Spacer(modifier = Modifier.height(16.dp))

                                    Text(
                                        text = "Description *",
                                        color = activeBlue,
                                        fontWeight = FontWeight.SemiBold,
                                        fontSize = 15.sp,
                                        fontFamily = FontFamily(Font(R.font.gill_sans_regular))
                                    )
                                    Spacer(modifier = Modifier.height(6.dp))
                                    CustomMultilineTextField(
                                        value = editDesc,
                                        onValueChange = { editDesc = it },
                                        placeholder = "Description"
                                    )

                                    Spacer(modifier = Modifier.height(24.dp))

                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                                    ) {
                                        Button(
                                            onClick = { screenMode = ScreenMode.LIST },
                                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFEEEEEE)),
                                            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFDDDDDE)),
                                            shape = RoundedCornerShape(10.dp),
                                            modifier = Modifier
                                                .weight(1f)
                                                .height(40.dp)
                                        ) {
                                            Text("Cancel", color = Color.Black)
                                        }

                                        Button(
                                            onClick = {
                                                if (editName.isBlank()) {
                                                    alertMessage = "Please check Name"
                                                } else if (editDesc.isBlank()) {
                                                    alertMessage = "Please check Description"
                                                } else {
                                                    viewModel.onEvent(
                                                        GroupsUiEvent.UpdateGroup(group.id, editName, editDesc) {
                                                            screenMode = ScreenMode.LIST
                                                        }
                                                    )
                                                }
                                            },
                                            colors = ButtonDefaults.buttonColors(containerColor = activeBlue),
                                            shape = RoundedCornerShape(10.dp),
                                            modifier = Modifier
                                                .weight(1f)
                                                .height(40.dp)
                                        ) {
                                            Text("Save", color = Color.White)
                                        }
                                    }
                                }
                            }
                        }
                    }

                    ScreenMode.UPDATE_MEMBERS -> {
                        selectedGroup?.let { group ->
                            // Correct JSON mapping: extract `"id"` from members Objects to prevent empty blank IDs in PATCH array!
                            val initialMemberSet = remember(group) {
                                val set = mutableSetOf<String>()
                                group.members?.let { array ->
                                    for (i in 0 until array.length()) {
                                        val obj = array.optJSONObject(i)
                                        val mId = if (obj != null) {
                                            obj.optString("id") // Uses "id" property, NOT "group_id"
                                        } else {
                                            array.optString(i)
                                        }
                                        if (!mId.isNullOrBlank()) {
                                            set.add(mId)
                                        }
                                    }
                                }
                                set.toSet()
                            }

                            // Key states by group to ensure correct reset
                            var selectedMemberIds by remember(group) { mutableStateOf(initialMemberSet) }
                            var memSearchQuery by remember(group) { mutableStateOf("") }

                            // Wrap entire screen content in a scrollable Column to prevent nested scroll crashes
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .verticalScroll(rememberScrollState())
                                    .padding(bottom = 24.dp)
                            ) {
                                Card(
                                    shape = RoundedCornerShape(10.dp),
                                    colors = CardDefaults.cardColors(containerColor = Color.White),
                                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(16.dp)
                                ) {
                                    Column(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(16.dp)
                                    ) {
                                        Text(
                                            text = "Update Members for ${group.name}",
                                            color = activeBlue,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 16.sp,
                                            fontFamily = FontFamily(Font(R.font.gill_sans_regular))
                                        )

                                        Spacer(modifier = Modifier.height(12.dp))

                                        // Search Team Members Bar INSIDE the Card Container
                                        NotificationsSearchBar(
                                            query = memSearchQuery,
                                            onQueryChange = { memSearchQuery = it },
                                            placeholder = "Search Team Members",
                                            modifier = Modifier.fillMaxWidth()
                                        )

                                        Spacer(modifier = Modifier.height(12.dp))

                                        val filteredMembers = uiState.membersList.filter {
                                            // Filter out the Group Head since they cannot be deselected from the checklist
                                            it.id != group.group_head_id && it.name.contains(memSearchQuery, ignoreCase = true)
                                        }

                                        // Render list rows using standard Column + forEach for clean scrolling flow
                                        Column(
                                            modifier = Modifier.fillMaxWidth(),
                                            verticalArrangement = Arrangement.spacedBy(6.dp)
                                        ) {
                                            filteredMembers.forEach { member ->
                                                val isChecked = selectedMemberIds.contains(member.id)

                                                Row(
                                                    modifier = Modifier
                                                        .fillMaxWidth()
                                                        .background(Color(0xFFF8F8F8), shape = RoundedCornerShape(6.dp))
                                                        .border(width = 0.5.dp, color = Color(0xFFC0C0C0), shape = RoundedCornerShape(6.dp))
                                                        .clickable {
                                                            selectedMemberIds = if (isChecked) {
                                                                selectedMemberIds - member.id
                                                            } else {
                                                                selectedMemberIds + member.id
                                                            }
                                                        }
                                                        .padding(start = 14.dp, end = 10.dp, top = 10.dp, bottom = 10.dp),
                                                    horizontalArrangement = Arrangement.SpaceBetween,
                                                    verticalAlignment = Alignment.CenterVertically
                                                ) {
                                                    Text(
                                                        text = member.name,
                                                        color = Color.Black,
                                                        fontSize = 15.sp,
                                                        fontFamily = FontFamily(Font(R.font.gill_sans_regular)),
                                                        modifier = Modifier.weight(1f)
                                                    )
                                                    Spacer(modifier = Modifier.width(8.dp))

                                                    CompositionLocalProvider(
                                                        LocalMinimumInteractiveComponentSize provides androidx.compose.ui.unit.Dp.Unspecified
                                                    ) {
                                                        // Set onCheckedChange = null to prevent double toggle issues. Click handled entirely by parent Row!
                                                        Checkbox(
                                                            checked = isChecked,
                                                            onCheckedChange = null,
                                                            colors = CheckboxDefaults.colors(checkedColor = activeBlue),
                                                            modifier = Modifier.size(22.dp)
                                                        )
                                                    }
                                                }
                                            }
                                        }

                                        Spacer(modifier = Modifier.height(24.dp))

                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.spacedBy(16.dp)
                                        ) {
                                            Button(
                                                onClick = { screenMode = ScreenMode.LIST },
                                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFEEEEEE)),
                                                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFDDDDDE)),
                                                shape = RoundedCornerShape(10.dp),
                                                modifier = Modifier
                                                    .weight(1f)
                                                    .height(40.dp)
                                            ) {
                                                Text("Cancel", color = Color.Black)
                                            }

                                            Button(
                                                onClick = {
                                                    // Ensure no empty, null, or blank member IDs are submitted
                                                    val finalMembers = (selectedMemberIds + group.group_head_id)
                                                        .filter { it.isNotBlank() }
                                                        .distinct()
                                                    viewModel.onEvent(
                                                        GroupsUiEvent.UpdateGroupMembers(group.id, finalMembers.toList()) {
                                                            screenMode = ScreenMode.LIST
                                                        }
                                                    )
                                                },
                                                colors = ButtonDefaults.buttonColors(containerColor = activeBlue),
                                                shape = RoundedCornerShape(10.dp),
                                                modifier = Modifier
                                                    .weight(1f)
                                                    .height(40.dp)
                                            ) {
                                                Text("Save", color = Color.White)
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }

                    ScreenMode.UPDATE_GROUP_HEAD -> {
                        selectedGroup?.let { group ->
                            ChooseGroupHeadCard(
                                group = group,
                                membersList = uiState.membersList,
                                onSave = { headId ->
                                    viewModel.onEvent(
                                        GroupsUiEvent.UpdateGroupHead(group.id, headId) {
                                            screenMode = ScreenMode.LIST
                                        }
                                    )
                                },
                                onCancel = { screenMode = ScreenMode.LIST },
                                onValidationError = { alertMessage = it }
                            )
                        }
                    }

                    ScreenMode.ACTIVITY_LOG -> {
                        selectedGroup?.let { group ->
                            ActivityLogDialog(
                                group = group,
                                membersList = uiState.membersList,
                                auditLogs = uiState.auditLogs,
                                onSearch = { cat, tm, client, fromDate, search, toDate ->
                                    viewModel.onEvent(
                                        GroupsUiEvent.FetchAuditLogs(group.id, fromDate, toDate, tm, search)
                                    )
                                },
                                onDismiss = { screenMode = ScreenMode.LIST }
                            )
                        }
                    }

                    ScreenMode.DELETE -> {
                        selectedGroup?.let { group ->
                            val docs = deleteCounts?.optInt("documents") ?: 0
                            val matters = deleteCounts?.optInt("matters") ?: 0
                            val rels = deleteCounts?.optInt("relationships") ?: 0
                            val membersCount = deleteCounts?.optInt("members") ?: 0

                            val otherGroups = uiState.groupsList.filter { it.id != group.id }

                            // Render full-page scrollable Delete confirmation layout matching screenshot exactly
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .verticalScroll(rememberScrollState())
                                    .padding(bottom = 24.dp)
                            ) {
                                Card(
                                    shape = RoundedCornerShape(10.dp),
                                    colors = CardDefaults.cardColors(containerColor = Color.White),
                                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(16.dp)
                                ) {
                                    Column(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(16.dp)
                                    ) {
                                        // Disabled Group Name field
                                        Text(
                                            text = "Group Name *",
                                            color = activeBlue,
                                            fontWeight = FontWeight.SemiBold,
                                            fontSize = 15.sp,
                                            fontFamily = FontFamily(Font(R.font.gill_sans_regular))
                                        )
                                        Spacer(modifier = Modifier.height(6.dp))
                                        CustomTextField(
                                            value = group.name ?: "",
                                            onValueChange = {},
                                            placeholder = "Group Name",
                                            modifier = Modifier.clickable(enabled = false) {}
                                        )

                                        Spacer(modifier = Modifier.height(16.dp))

                                        // Disabled Description field
                                        Text(
                                            text = "Description *",
                                            color = activeBlue,
                                            fontWeight = FontWeight.SemiBold,
                                            fontSize = 15.sp,
                                            fontFamily = FontFamily(Font(R.font.gill_sans_regular))
                                        )
                                        Spacer(modifier = Modifier.height(6.dp))
                                        CustomMultilineTextField(
                                            value = group.description ?: "",
                                            onValueChange = {},
                                            placeholder = "Description",
                                            modifier = Modifier.clickable(enabled = false) {}
                                        )

                                        Spacer(modifier = Modifier.height(20.dp))

                                        // Blue Resource Counts Container matching screenshot exactly
                                        Column(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .background(activeBlue, shape = RoundedCornerShape(10.dp))
                                                .padding(16.dp)
                                        ) {
                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                horizontalArrangement = Arrangement.SpaceBetween,
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Text(
                                                    text = group.name ?: "",
                                                    color = Color.White,
                                                    fontWeight = FontWeight.Bold,
                                                    fontSize = 16.sp,
                                                    fontFamily = FontFamily(Font(R.font.gill_sans_regular))
                                                )
                                                Image(
                                                    painter = painterResource(id = R.drawable.delete_white_icon),
                                                    contentDescription = "Delete Icon",
                                                    modifier = Modifier.size(24.dp)
                                                )
                                            }

                                            Spacer(modifier = Modifier.height(14.dp))

                                            // White Horizontal pills displaying resource counts
                                            val pills = listOf(
                                                "$docs Documents",
                                                "$matters Matters",
                                                "$rels Relationships",
                                                "$membersCount Members"
                                            )

                                            pills.forEach { pillText ->
                                                Box(
                                                    modifier = Modifier
                                                        .fillMaxWidth()
                                                        .padding(vertical = 4.dp)
                                                        .background(Color.White, shape = RoundedCornerShape(8.dp))
                                                        .padding(horizontal = 14.dp, vertical = 10.dp)
                                                ) {
                                                    Text(
                                                        text = pillText,
                                                        color = Color.Black,
                                                        fontWeight = FontWeight.Bold,
                                                        fontSize = 15.sp,
                                                        fontFamily = FontFamily(Font(R.font.gill_sans_regular))
                                                    )
                                                }
                                            }
                                        }

                                        Spacer(modifier = Modifier.height(20.dp))

                                        // Reassign Dropdown selection box
                                        Text(
                                            text = "Group Name *",
                                            color = activeBlue,
                                            fontWeight = FontWeight.SemiBold,
                                            fontSize = 15.sp,
                                            fontFamily = FontFamily(Font(R.font.gill_sans_regular))
                                        )
                                        Spacer(modifier = Modifier.height(6.dp))
                                        Box(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .background(Color(0xFFF9FAFB), shape = RoundedCornerShape(6.dp))
                                                .border(width = 0.5.dp, color = Color(0xFFC0C0C0), shape = RoundedCornerShape(6.dp))
                                                .clickable { isReassignExpanded = true }
                                                .padding(10.dp)
                                        ) {
                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                horizontalArrangement = Arrangement.SpaceBetween,
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Text(
                                                    text = reassignedGroupName,
                                                    color = if (reassignedGroupId.isEmpty()) Color(0xFF707070) else Color.Black,
                                                    fontSize = 15.sp,
                                                    fontFamily = FontFamily(Font(R.font.gill_sans_regular))
                                                )
                                                Image(
                                                    painter = painterResource(id = R.drawable.menu_down_icon), // Down arrow icon
                                                    contentDescription = "Dropdown Down Arrow",
                                                    modifier = Modifier.size(20.dp)
                                                )
                                            }

                                            DropdownMenu(
                                                expanded = isReassignExpanded,
                                                onDismissRequest = { isReassignExpanded = false },
                                                modifier = Modifier.fillMaxWidth(0.8f)
                                            ) {
                                                otherGroups.forEach { other ->
                                                    DropdownMenuItem(
                                                        text = { Text(other.name ?: "") },
                                                        onClick = {
                                                            reassignedGroupId = other.id
                                                            reassignedGroupName = other.name ?: ""
                                                            isReassignExpanded = false
                                                        }
                                                    )
                                                }
                                            }
                                        }

                                        Spacer(modifier = Modifier.height(24.dp))

                                        // Action buttons canceling/deleting
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.spacedBy(16.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Button(
                                                onClick = { screenMode = ScreenMode.LIST },
                                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFEEEEEE)),
                                                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFDDDDDE)),
                                                shape = RoundedCornerShape(10.dp),
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

                                            Button(
                                                onClick = {
                                                    // Triggers deletion API reassign call
                                                    viewModel.onEvent(
                                                        GroupsUiEvent.DeleteGroup(group.id, reassignedGroupId) {
                                                            screenMode = ScreenMode.LIST
                                                        }
                                                    )
                                                },
                                                enabled = reassignedGroupId.isNotEmpty(),
                                                colors = ButtonDefaults.buttonColors(
                                                    containerColor = activeBlue,
                                                    disabledContainerColor = activeBlue.copy(alpha = 0.4f)
                                                ),
                                                shape = RoundedCornerShape(10.dp),
                                                modifier = Modifier
                                                    .weight(1f)
                                                    .height(40.dp)
                                            ) {
                                                Text(
                                                    text = "Delete",
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
                        }
                    }
                }
            }
        }

        // Custom Validation / Error Alert Popups
        alertMessage?.let { msg ->
            val titleText = if (msg.contains("Head of the Group", ignoreCase = true)) "Alert !" else "Alert"
            MembersAlertDialog(
                title = titleText,
                message = msg,
                confirmLabel = "OK",
                onConfirm = {
                    alertMessage = null
                },
                onDismiss = { alertMessage = null }
            )
        }

        // Custom Success Alert Popups
        successMessage?.let { msg ->
            MembersAlertDialog(
                title = "Success",
                message = msg,
                confirmLabel = "OK",
                onConfirm = { successMessage = null },
                onDismiss = { successMessage = null }
            )
        }
    }
}
