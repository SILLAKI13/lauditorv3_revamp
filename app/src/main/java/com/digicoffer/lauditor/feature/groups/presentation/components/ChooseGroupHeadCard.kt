package com.digicoffer.lauditor.feature.groups.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.digicoffer.lauditor.Groups.Models.GroupModel
import com.digicoffer.lauditor.Groups.Models.ViewGroupModel
import com.digicoffer.lauditor.R
import com.digicoffer.lauditor.core.ui.common.search.AppSearchField

@Composable
fun ChooseGroupHeadCard(
    group: ViewGroupModel,
    membersList: List<GroupModel>,
    onSave: (groupHeadId: String) -> Unit,
    onCancel: () -> Unit,
    onValidationError: (String) -> Unit
) {
    var selectedHeadId by remember { mutableStateOf(group.group_head_id ?: "") }
    var searchQuery by remember { mutableStateOf("") }
    val activeBlue = Color(0xFF004D87)

    // Wrap in verticalScroll to ensure full scrollability
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
                // "Select Head of the Group" title inside the Card above search field
                Text(
                    text = "Select Head of the Group",
                    color = activeBlue,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    fontFamily = FontFamily(Font(R.font.gill_sans_regular)),
                    modifier = Modifier.padding(bottom = 12.dp)
                )

                // Search Bar INSIDE the Card
                AppSearchField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    placeholder = "Search Team Members",
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(12.dp))

                val groupMemberIds = remember(group) {
                    val set = mutableSetOf<String>()
                    group.members?.let { array ->
                        for (i in 0 until array.length()) {
                            val obj = array.optJSONObject(i)
                            // Read member id using "id" property, NOT "group_id"
                            val memberId = if (obj != null) {
                                obj.optString("id")
                            } else {
                                array.optString(i)
                            }
                            if (!memberId.isNullOrBlank()) {
                                set.add(memberId)
                            }
                        }
                    }
                    set
                }

                val filteredList = membersList.filter {
                    (groupMemberIds.isEmpty() || groupMemberIds.contains(it.id)) &&
                            it.name.contains(searchQuery, ignoreCase = true)
                }

                // Render checklist rows using Column + forEach for clean scroll flow
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    filteredList.forEach { member ->
                        val isChosen = selectedHeadId == member.id

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(Color(0xFFF8F8F8), shape = RoundedCornerShape(6.dp))
                                .border(width = 0.5.dp, color = Color(0xFFC0C0C0), shape = RoundedCornerShape(6.dp))
                                .clickable { selectedHeadId = if (isChosen) "" else member.id }
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

                            androidx.compose.runtime.CompositionLocalProvider(
                                LocalMinimumInteractiveComponentSize provides androidx.compose.ui.unit.Dp.Unspecified
                            ) {
                                // Visually use Checkboxes instead of RadioButtons, set onCheckedChange = null to prevent double toggle!
                                Checkbox(
                                    checked = isChosen,
                                    onCheckedChange = null,
                                    colors = CheckboxDefaults.colors(checkedColor = activeBlue),
                                    modifier = Modifier.size(22.dp)
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Action Buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Button(
                        onClick = onCancel,
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFEEEEEE)),
                        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFDDDDDE)),
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
                            if (selectedHeadId.isEmpty()) {
                                onValidationError("Please select Head of the Group")
                            } else {
                                onSave(selectedHeadId)
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
    }
}
