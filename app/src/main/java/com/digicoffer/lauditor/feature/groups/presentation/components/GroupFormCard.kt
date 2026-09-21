package com.digicoffer.lauditor.feature.groups.presentation.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.digicoffer.lauditor.Groups.Models.GroupModel
import com.digicoffer.lauditor.R
import com.digicoffer.lauditor.core.ui.common.search.AppSearchField

@Composable
fun CustomTextField(
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    modifier: Modifier = Modifier
) {
    val inputBorderColor = Color(0xFFC0C0C0)
    val inputBgColor = Color(0xFFF9FAFB)
    val textStyle = TextStyle(
        fontSize = 15.sp,
        fontFamily = FontFamily(Font(R.font.gill_sans_regular)),
        color = Color.Black
    )

    BasicTextField(
        value = value,
        onValueChange = onValueChange,
        textStyle = textStyle,
        singleLine = true,
        modifier = modifier
            .fillMaxWidth()
            .background(inputBgColor, shape = RoundedCornerShape(6.dp))
            .border(width = 0.5.dp, color = inputBorderColor, shape = RoundedCornerShape(6.dp)),
        decorationBox = { innerTextField ->
            Box(modifier = Modifier.padding(10.dp)) {
                if (value.isEmpty()) {
                    Text(
                        text = placeholder,
                        style = textStyle.copy(color = Color(0xFF707070))
                    )
                }
                innerTextField()
            }
        }
    )
}

@Composable
fun CustomMultilineTextField(
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    modifier: Modifier = Modifier
) {
    val inputBorderColor = Color(0xFFC0C0C0)
    val inputBgColor = Color(0xFFF9FAFB)
    val textStyle = TextStyle(
        fontSize = 15.sp,
        fontFamily = FontFamily(Font(R.font.gill_sans_regular)),
        color = Color.Black
    )

    BasicTextField(
        value = value,
        onValueChange = onValueChange,
        textStyle = textStyle,
        singleLine = false,
        modifier = modifier
            .fillMaxWidth()
            .height(130.dp)
            .background(inputBgColor, shape = RoundedCornerShape(6.dp))
            .border(width = 0.5.dp, color = inputBorderColor, shape = RoundedCornerShape(6.dp)),
        decorationBox = { innerTextField ->
            Box(modifier = Modifier.padding(10.dp)) {
                if (value.isEmpty()) {
                    Text(
                        text = placeholder,
                        style = textStyle.copy(color = Color(0xFF707070))
                    )
                }
                innerTextField()
            }
        }
    )
}

@Composable
fun GroupFormCard(
    initialName: String = "",
    initialDescription: String = "",
    initialSelectedMemberIds: Set<String> = emptySet(),
    initialGroupHeadId: String = "",
    membersList: List<GroupModel>,
    isAddTeamMemberTab: Boolean,
    onTabChange: (Boolean) -> Unit,
    onSave: (name: String, description: String, groupHead: String, members: List<String>) -> Unit,
    onCancel: () -> Unit,
    onValidationError: (String) -> Unit
) {
    var name by remember { mutableStateOf(initialName) }
    var description by remember { mutableStateOf(initialDescription) }
    var selectedMemberIds by remember { mutableStateOf(initialSelectedMemberIds) }
    var selectedGroupHeadId by remember { mutableStateOf(initialGroupHeadId) }

    var isChecklistVisible by remember { mutableStateOf(initialName.isNotEmpty() || initialSelectedMemberIds.isNotEmpty()) }
    var searchQuery by remember { mutableStateOf("") }

    val activeBlue = Color(0xFF004D87)
    val labelColor = Color(0xFF004D87)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(bottom = 16.dp)
    ) {
        if (!isChecklistVisible) {
            // Scrollable view when checklist is not expanded
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .verticalScroll(rememberScrollState())
            ) {
                // Card 1: Form Fields (Group Name, Description, Expand Trigger)
                Card(
                    shape = RoundedCornerShape(10.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 6.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                    ) {
                        // Group Name
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "Group Name",
                                color = labelColor,
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 15.sp,
                                fontFamily = FontFamily(Font(R.font.gill_sans_regular))
                            )
                            Text(text = " *", color = Color.Red, fontSize = 15.sp)
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        CustomTextField(
                            value = name,
                            onValueChange = { name = it },
                            placeholder = "Group Name"
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        // Description
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "Description",
                                color = labelColor,
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 15.sp,
                                fontFamily = FontFamily(Font(R.font.gill_sans_regular))
                            )
                            Text(text = " *", color = Color.Red, fontSize = 15.sp)
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        CustomMultilineTextField(
                            value = description,
                            onValueChange = { description = it },
                            placeholder = "Description"
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        // Select Team member(s) Trigger Row with Name/Desc validation check
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(Color(0xFFF9FAFB), shape = RoundedCornerShape(6.dp))
                                .border(width = 0.5.dp, color = Color(0xFFC0C0C0), shape = RoundedCornerShape(6.dp))
                                .clickable {
                                    if (name.isBlank() || description.isBlank()) {
                                        onValidationError("Please enter Group Name and Description first.")
                                    } else {
                                        isChecklistVisible = true
                                    }
                                }
                                .padding(10.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Select Team member(s)",
                                color = Color.Black,
                                fontSize = 15.sp,
                                fontFamily = FontFamily(Font(R.font.gill_sans_regular))
                            )
                        }
                    }
                }
            }
        } else {
            // Layout when checklist is expanded: Form summary/inputs on top, list scrollable in middle, buttons fixed at bottom
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
            ) {
                // Card 1: Collapsible top form
                Card(
                    shape = RoundedCornerShape(10.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 6.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                    ) {
                        // Group Name
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "Group Name",
                                color = labelColor,
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 15.sp,
                                fontFamily = FontFamily(Font(R.font.gill_sans_regular))
                            )
                            Text(text = " *", color = Color.Red, fontSize = 15.sp)
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        CustomTextField(
                            value = name,
                            onValueChange = { name = it },
                            placeholder = "Group Name"
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        // Description
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "Description",
                                color = labelColor,
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 15.sp,
                                fontFamily = FontFamily(Font(R.font.gill_sans_regular))
                            )
                            Text(text = " *", color = Color.Red, fontSize = 15.sp)
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        CustomMultilineTextField(
                            value = description,
                            onValueChange = { description = it },
                            placeholder = "Description",
                            modifier = Modifier.height(70.dp)
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        // Select Team member(s) Trigger Row
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(Color(0xFFF9FAFB), shape = RoundedCornerShape(6.dp))
                                .border(width = 0.5.dp, color = Color(0xFFC0C0C0), shape = RoundedCornerShape(6.dp))
                                .clickable { isChecklistVisible = !isChecklistVisible }
                                .padding(10.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Select Team member(s)",
                                color = Color.Black,
                                fontSize = 15.sp,
                                fontFamily = FontFamily(Font(R.font.gill_sans_regular))
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Toggle tabs: Add Team Member / Add Group Head
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp)
                        .border(width = 0.5.dp, color = Color(0xFFC0C0C0), shape = RoundedCornerShape(8.dp))
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color.White)
                ) {
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .background(if (isAddTeamMemberTab) activeBlue else Color.White)
                            .clickable { 
                                onTabChange(true)
                                searchQuery = ""
                            }
                            .padding(vertical = 10.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "Add Team Member",
                            color = if (isAddTeamMemberTab) Color.White else Color.Black,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 14.sp,
                            fontFamily = FontFamily(Font(R.font.gill_sans_regular))
                        )
                    }
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .background(if (!isAddTeamMemberTab) activeBlue else Color.White)
                            .clickable { 
                                if (selectedMemberIds.isEmpty()) {
                                    onValidationError("Please check the member selection")
                                } else {
                                    onTabChange(false)
                                    searchQuery = ""
                                }
                            }
                            .padding(vertical = 10.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "Add Group Head",
                            color = if (!isAddTeamMemberTab) Color.White else Color.Black,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 14.sp,
                            fontFamily = FontFamily(Font(R.font.gill_sans_regular))
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Select All checkbox card
                if (isAddTeamMemberTab) {
                    val filteredMembersForSelectAll = membersList.filter { it.name.contains(searchQuery, ignoreCase = true) }
                    val isAllSelected = filteredMembersForSelectAll.isNotEmpty() && filteredMembersForSelectAll.all { selectedMemberIds.contains(it.id) }

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp),
                        horizontalArrangement = Arrangement.End,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Card(
                            shape = RoundedCornerShape(8.dp),
                            colors = CardDefaults.cardColors(containerColor = Color.White),
                            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                            border = BorderStroke(0.5.dp, Color(0xFFC0C0C0)),
                            modifier = Modifier.clickable {
                                val list = filteredMembersForSelectAll.map { it.id }
                                selectedMemberIds = if (isAllSelected) {
                                    selectedMemberIds - list.toSet()
                                } else {
                                    selectedMemberIds + list
                                }
                            }
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                CompositionLocalProvider(
                                    LocalMinimumInteractiveComponentSize provides androidx.compose.ui.unit.Dp.Unspecified
                                ) {
                                    Checkbox(
                                        checked = isAllSelected,
                                        onCheckedChange = null,
                                        colors = CheckboxDefaults.colors(checkedColor = activeBlue),
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "Select All",
                                    color = Color.Black,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Medium,
                                    fontFamily = FontFamily(Font(R.font.gill_sans_regular))
                                )
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                }

                // Checklist Scroll Area inside its own Card wrapper
                Card(
                    shape = RoundedCornerShape(10.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .padding(horizontal = 16.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp)
                    ) {
                        // Show "Select Head of the Group" title when Add Group Head tab is active
                        if (!isAddTeamMemberTab) {
                            Text(
                                text = "Select Head of the Group",
                                color = activeBlue,
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp,
                                fontFamily = FontFamily(Font(R.font.gill_sans_regular)),
                                modifier = Modifier.padding(bottom = 12.dp)
                            )
                        }

                        // Search Team Members Bar INSIDE the Card
                        AppSearchField(
                            value = searchQuery,
                            onValueChange = { searchQuery = it },
                            placeholder = "Search Team Members",
                            modifier = Modifier.fillMaxWidth()
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        // If in "Add Group Head" tab, only select from members already chosen in "Add Team Member" tab
                        val displayList = if (isAddTeamMemberTab) {
                            membersList
                        } else {
                            membersList.filter { selectedMemberIds.contains(it.id) }
                        }

                        val filteredList = displayList.filter { it.name.contains(searchQuery, ignoreCase = true) }

                        // Scrollable list items via LazyColumn
                        LazyColumn(
                            modifier = Modifier
                                .fillMaxWidth()
                                .weight(1f),
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            items(filteredList, key = { it.id }) { member ->
                                val isChecked = selectedMemberIds.contains(member.id)
                                val isChosenHead = selectedGroupHeadId == member.id

                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .background(Color(0xFFF8F8F8), shape = RoundedCornerShape(6.dp))
                                        .border(width = 0.5.dp, color = Color(0xFFC0C0C0), shape = RoundedCornerShape(6.dp))
                                        .clickable {
                                            if (isAddTeamMemberTab) {
                                                selectedMemberIds = if (isChecked) {
                                                    if (isChosenHead) selectedGroupHeadId = ""
                                                    selectedMemberIds - member.id
                                                } else {
                                                    selectedMemberIds + member.id
                                                }
                                            } else {
                                                selectedGroupHeadId = if (isChosenHead) "" else member.id
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
                                        if (isAddTeamMemberTab) {
                                            Checkbox(
                                                checked = isChecked,
                                                onCheckedChange = null,
                                                colors = CheckboxDefaults.colors(checkedColor = activeBlue),
                                                modifier = Modifier.size(22.dp)
                                            )
                                        } else {
                                            Checkbox(
                                                checked = isChosenHead,
                                                onCheckedChange = null,
                                                colors = CheckboxDefaults.colors(checkedColor = activeBlue),
                                                modifier = Modifier.size(22.dp)
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

        Spacer(modifier = Modifier.height(16.dp))

        // Fixed Action Buttons Row at bottom
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Button(
                onClick = onCancel,
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFEEEEEE)),
                border = BorderStroke(1.dp, Color(0xFFDDDDDE)),
                shape = RoundedCornerShape(10.dp),
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

            Button(
                onClick = {
                    if (name.isBlank()) {
                        onValidationError("Please check Name")
                    } else if (description.isBlank()) {
                        onValidationError("Please check Description")
                    } else if (selectedMemberIds.isEmpty()) {
                        onValidationError("Please check the member selection")
                    } else if (isAddTeamMemberTab) {
                        // Switch automatically to Add Group Head tab when valid
                        onTabChange(false)
                    } else if (selectedGroupHeadId.isEmpty()) {
                        // Alert missing group head on Add Group Head tab
                        onValidationError("Please select Head of the Group")
                    } else {
                        onSave(name, description, selectedGroupHeadId, selectedMemberIds.toList())
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = activeBlue),
                shape = RoundedCornerShape(10.dp),
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
