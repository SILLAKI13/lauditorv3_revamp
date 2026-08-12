package com.digicoffer.lauditor.feature.groups.presentation.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
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
import com.digicoffer.lauditor.CommonFiles.GlobalFiles.Constants
import com.digicoffer.lauditor.Groups.Models.ViewGroupModel
import com.digicoffer.lauditor.R

@Composable
fun GroupCardItem(
    group: ViewGroupModel,
    onEditClick: () -> Unit,
    onUpdateMembersClick: () -> Unit,
    onUpdateGroupHeadClick: () -> Unit,
    onDeleteClick: () -> Unit,
    onActivityLogClick: () -> Unit
) {
    val activeBlue = Color(0xFF004D87)
    val textGray = Color(0xFF333333)
    var isMenuExpanded by remember { mutableStateOf(false) }

    val role = Constants.ROLE ?: ""
    val groupName = group.name ?: ""

    // Legacy action menu visibility rules:
    // 1. SuperUser or AAM: only Update Group Members List & Activity Log are visible.
    // 2. Role GH (Group Head): only Edit Group Info & Activity Log are visible.
    // 3. Admin / Practice Head: all 5 actions are visible.
    val menuItems = remember(role, groupName) {
        val list = mutableListOf<Pair<String, () -> Unit>>()
        if (groupName.equals("SuperUser", ignoreCase = true) || groupName.equals("AAM", ignoreCase = true)) {
            list.add("Update Group Members List" to onUpdateMembersClick)
            list.add("Group Activity Log" to onActivityLogClick)
        } else {
            if (role.equals("GH", ignoreCase = true)) {
                list.add("Edit Group Info" to onEditClick)
                list.add("Group Activity Log" to onActivityLogClick)
            } else {
                list.add("Edit Group Info" to onEditClick)
                list.add("Update Group Members List" to onUpdateMembersClick)
                list.add("Update Group Head" to onUpdateGroupHeadClick)
                list.add("Delete Group" to onDeleteClick)
                list.add("Group Activity Log" to onActivityLogClick)
            }
        }
        list
    }

    Card(
        shape = RoundedCornerShape(8.dp),
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
            // Header Row: Group Name and Options Menu
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = group.name ?: "",
                    color = activeBlue,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    fontFamily = FontFamily(Font(R.font.gill_sans_regular)),
                    modifier = Modifier.weight(1f)
                )

                Box {
                    Image(
                        painter = painterResource(id = R.drawable.img_17), // Vertical three dots options icon
                        contentDescription = "Options menu",
                        modifier = Modifier
                            .size(24.dp)
                            .clickable { isMenuExpanded = true }
                    )

                    DropdownMenu(
                        expanded = isMenuExpanded,
                        onDismissRequest = { isMenuExpanded = false },
                        modifier = Modifier.width(180.dp)
                    ) {
                        menuItems.forEachIndexed { index, pair ->
                            DropdownMenuItem(
                                text = {
                                    Text(
                                        text = pair.first,
                                        fontSize = 15.sp,
                                        fontFamily = FontFamily(Font(R.font.gill_sans_regular)),
                                        color = Color.Black
                                    )
                                },
                                onClick = {
                                    isMenuExpanded = false
                                    pair.second()
                                }
                            )
                            if (index < menuItems.lastIndex) {
                                HorizontalDivider(thickness = 0.5.dp, color = Color(0xFFE0E0E0))
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Body Meta text lines
            val groupHeadName = group.group_head_name ?: ""
            Text(
                text = "Group Head : $groupHeadName",
                color = textGray,
                fontSize = 14.sp,
                fontFamily = FontFamily(Font(R.font.gill_sans_regular))
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Created : ${group.created ?: ""}",
                color = textGray,
                fontSize = 14.sp,
                fontFamily = FontFamily(Font(R.font.gill_sans_regular))
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Number of Members : ${group.memberCount ?: ""}",
                color = textGray,
                fontSize = 14.sp,
                fontFamily = FontFamily(Font(R.font.gill_sans_regular))
            )
        }
    }
}
