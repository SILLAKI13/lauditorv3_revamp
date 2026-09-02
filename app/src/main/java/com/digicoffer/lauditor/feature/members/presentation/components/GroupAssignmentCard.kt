package com.digicoffer.lauditor.feature.members.presentation.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.digicoffer.lauditor.core.ui.common.search.AppSearchField
import com.digicoffer.lauditor.Groups.Models.ViewGroupModel
import com.digicoffer.lauditor.R

@Composable
fun GroupAssignmentCard(
    groupsList: List<ViewGroupModel>,
    initialSelectedGroupIds: List<String>,
    onCancelClick: () -> Unit,
    onSaveClick: (selectedGroupIds: List<String>) -> Unit,
    modifier: Modifier = Modifier
) {
    val activeBlue = Color(0xFF004D87)
    val inputBorderColor = Color(0xFFC0C0C0)
    val inputBgColor = Color(0xFFF9FAFB)
    val cancelBgColor = Color(0xFFEEEEEE)
    val cancelBorderColor = Color(0xFFDDDDDE)

    var searchQuery by remember { mutableStateOf("") }
    val selectedStates = remember {
        mutableStateMapOf<String, Boolean>().apply {
            initialSelectedGroupIds.forEach { put(it, true) }
        }
    }

    val filteredGroups = groupsList.filter {
        val name = it.name ?: it.group_name ?: ""
        name.contains(searchQuery, ignoreCase = true)
    }

    Card(
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
        modifier = modifier
            .fillMaxWidth()
            .padding(16.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            // Title
            Text(
                text = "Assign Group(s)",
                color = activeBlue,
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp,
                fontFamily = FontFamily(Font(R.font.gill_sans_regular))
            )

            Spacer(modifier = Modifier.height(16.dp))

            AppSearchField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                placeholder = "Search Groups",
                modifier = Modifier.fillMaxWidth().padding(horizontal = 0.dp)
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Checklist container box (height constraint, transparent background)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(280.dp)
            ) {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(6.dp) // Stacked with small vertical spacing
                ) {
                    itemsIndexed(filteredGroups) { index, group ->
                        val id = group.id ?: group.group_id ?: ""
                        val name = group.name ?: group.group_name ?: ""
                        val isChecked = selectedStates[id] ?: false

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(Color(0xFFF8F8F8), shape = RoundedCornerShape(6.dp))
                                .border(width = 0.5.dp, color = Color(0xFFC0C0C0), shape = RoundedCornerShape(6.dp))
                                .clickable { selectedStates[id] = !isChecked }
                                .padding(start = 14.dp, end = 10.dp, top = 10.dp, bottom = 10.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = name,
                                color = Color.Black,
                                fontSize = 15.sp,
                                fontFamily = FontFamily(Font(R.font.gill_sans_regular)),
                                modifier = Modifier.weight(1f)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            androidx.compose.runtime.CompositionLocalProvider(
                                androidx.compose.material3.LocalMinimumInteractiveComponentSize provides androidx.compose.ui.unit.Dp.Unspecified
                            ) {
                                Checkbox(
                                    checked = isChecked,
                                    onCheckedChange = { selectedStates[id] = it },
                                    colors = CheckboxDefaults.colors(checkedColor = activeBlue),
                                    modifier = Modifier.size(22.dp)
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Save/Cancel Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center
            ) {
                Button(
                    onClick = onCancelClick,
                    colors = ButtonDefaults.buttonColors(containerColor = cancelBgColor),
                    border = BorderStroke(1.dp, cancelBorderColor),
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

                Spacer(modifier = Modifier.width(16.dp))

                Button(
                    onClick = {
                        val selectedIds = selectedStates.filter { it.value }.keys.toList()
                        onSaveClick(selectedIds)
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
