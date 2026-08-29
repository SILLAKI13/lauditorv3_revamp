package com.digicoffer.lauditor.core.ui.common.dropdowns

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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.digicoffer.lauditor.R

@Composable
fun <T> DropdownSelectorField(
    items: List<T>,
    selectedItem: T?,
    onItemSelected: (T) -> Unit,
    itemToLabel: (T) -> String,
    placeholder: String,
    modifier: Modifier = Modifier,
    cornerRadius: Dp = 6.dp, // @dimen/six_dp (6dp)
    borderColor: Color = Color(0xFFC0C0C0), // silver (#C0C0C0)
    backgroundColor: Color = Color(0xFFF9FAFB), // text_field_color (#F9FAFB)
    textColor: Color = Color.Black,
    textStyle: TextStyle = TextStyle.Default,
    onClearSelection: (() -> Unit)? = null,
    enabled: Boolean = true,
    showSearch: Boolean = false
) {
    var expanded by remember { mutableStateOf(false) }

    val density = LocalDensity.current
    // Convert sp values to Dp to match scale-independent legacy XML dimensions
    val twelveSpAsDp = with(density) { 12.sp.toDp() }
    val fifteenSpAsDp = with(density) { 15.sp.toDp() }
    val containerWidth = twelveSpAsDp + fifteenSpAsDp

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 4.dp) // layout_marginStart/End 4dp from XML
    ) {
        // Selector Field Box (dropdown_spinner_layout.xml style)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(40.dp) // Set height to 40dp matching legacy spinner height
                .clip(RoundedCornerShape(cornerRadius))
                .background(if (enabled) backgroundColor else Color(0xFFFAFAFA))
                .border(0.5.dp, if (enabled) borderColor else Color(0xFFE5E5E5), RoundedCornerShape(cornerRadius)) // @dimen/point_five (0.5dp)
                .clickable(enabled = enabled) { expanded = !expanded },
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            val displayedLabel = selectedItem?.let(itemToLabel) ?: ""
            val textToShow = if (displayedLabel.isEmpty()) placeholder else displayedLabel
            val isPlaceholder = displayedLabel.isEmpty()
            Text(
                text = textToShow,
                style = textStyle,
                color = if (isPlaceholder) Color(0xFFA0A0A0) else textColor, // grey_color_dark (#A0A0A0) or Black
                modifier = Modifier
                    .weight(1f)
                    .padding(start = 10.dp) // Exact padding: @dimen/ten_dp (10dp) start padding
            )

            // Fixed-size container to prevent layout shifting and text movement when switching drawables
            Box(
                modifier = if (selectedItem != null && onClearSelection != null) {
                    Modifier
                        .width(containerWidth)
                        .fillMaxHeight()
                        .clickable {
                            onClearSelection.invoke()
                            expanded = false
                        }
                } else {
                    Modifier
                        .width(containerWidth)
                        .fillMaxHeight()
                },
                contentAlignment = Alignment.CenterStart // Positions the icon at the start of the container, leaving 15sp margin at the end
            ) {
                if (selectedItem != null && onClearSelection != null) {
                    Icon(
                        painter = painterResource(id = R.drawable.simple_cancel_blue),
                        contentDescription = "Clear Selection",
                        tint = Color.Unspecified,
                        modifier = Modifier.size(twelveSpAsDp) // Reduced chevron/cancel size to 12sp to match screenshot
                    )
                } else {
                    Icon(
                        painter = painterResource(id = R.drawable.drop_down_blue),
                        contentDescription = "Dropdown Arrow",
                        tint = Color.Unspecified,
                        modifier = Modifier.size(twelveSpAsDp) // Reduced chevron/cancel size to 12sp to match screenshot
                    )
                }
            }
        }

        // Inline ListView (sp__category / spinner_list.xml style)
        if (expanded) {
            Spacer(modifier = Modifier.height(4.dp))
            var searchQuery by remember { mutableStateOf("") }
            val filteredItems = remember(items, searchQuery) {
                if (searchQuery.isEmpty()) {
                    items
                } else {
                    items.filter { itemToLabel(it).contains(searchQuery, ignoreCase = true) }
                }
            }

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(if (showSearch) 200.dp else 150.dp) // height extension for search textfield
                    .clip(RoundedCornerShape(cornerRadius))
                    .background(backgroundColor)
                    .border(0.5.dp, borderColor, RoundedCornerShape(cornerRadius))
            ) {
                if (showSearch) {
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = { searchQuery = it },
                        placeholder = { Text("Search...", fontSize = 12.sp) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(4.dp)
                            .height(48.dp),
                        singleLine = true,
                        textStyle = textStyle.copy(fontSize = 13.sp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedContainerColor = Color.White,
                            unfocusedContainerColor = Color.White,
                            focusedBorderColor = Color(0xFF004D87),
                            unfocusedBorderColor = Color(0xFFCCCCCC)
                        )
                    )
                }

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .verticalScroll(rememberScrollState())
                ) {
                    filteredItems.forEach { item ->
                        val isSelected = (item == selectedItem)
                        val label = itemToLabel(item)

                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    onItemSelected(item)
                                    expanded = false
                                }
                                .background(Color.Transparent)
                        ) {
                            Text(
                                text = label,
                                style = textStyle,
                                color = textColor,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(
                                        start = 6.dp,  // margin 4dp + paddingStart 2dp
                                        top = 10.dp,   // margin 4dp + paddingTop 6dp
                                        bottom = 10.dp // margin 4dp + paddingBottom 6dp
                                    )
                            )

                            // Divider line (dark_grey #DDDDDE, height 1dp)
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(1.dp)
                                    .background(Color(0xFFDDDDDE))
                            )
                        }
                    }
                }
            }
        }
    }
}
