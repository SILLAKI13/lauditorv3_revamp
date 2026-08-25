package com.digicoffer.lauditor.core.ui.common.navigation

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.digicoffer.lauditor.R

// Gill Sans Font Family definition
val GillSansFontFamily = FontFamily(
    Font(R.font.gill_sans)
)

data class SidebarSubItemState(
    val index: Int,
    val label: String,
    val isHighlighted: Boolean
)

data class SidebarItemState(
    val id: Int,
    val title: String,
    val iconResId: Int,
    val isVisible: Boolean,
    val hasSubMenu: Boolean,
    val isExpanded: Boolean,
    val isHighlighted: Boolean,
    val subItems: List<SidebarSubItemState>
)

data class SidebarUiState(
    val items: List<SidebarItemState> = emptyList(),
    val sixtySeven: Int = 67
)

@Composable
fun SidebarMenu(
    state: SidebarUiState,
    onParentClick: (Int) -> Unit,
    onSubItemClick: (Int, Int) -> Unit,
    modifier: Modifier = Modifier
) {
    val scrollState = rememberScrollState()

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Color.White)
            .padding(top = 8.dp, bottom = 10.dp)
            .verticalScroll(scrollState)
    ) {
        state.items.forEach { item ->
            if (item.isVisible) {
                SidebarParentRow(
                    item = item,
                    onClick = { onParentClick(item.id) }
                )

                if (item.hasSubMenu) {
                    AnimatedVisibility(
                        visible = item.isExpanded,
                        enter = expandVertically(animationSpec = tween(durationMillis = 200)),
                        exit = shrinkVertically(animationSpec = tween(durationMillis = 200))
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp)
                        ) {
                            item.subItems.forEach { subItem ->
                                SidebarSubItemRow(
                                    subItem = subItem,
                                    sixtySevenDp = state.sixtySeven,
                                    onClick = { onSubItemClick(item.id, subItem.index) }
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
fun SidebarParentRow(
    item: SidebarItemState,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    // Colors from project resources
    val COLOR_EXPANDED_BG = Color(0xFFE8F6FF)
    val COLOR_ACTIVE_TEXT = Color(0xFF004D87)
    val COLOR_NORMAL_TEXT = Color(0xFF888888)
    val COLOR_CHECKED_BAR = Color(0xFF2A5FA5)

    // Highlight is active if the menu is selected or expanded (for parent with submenus)
    val isHighlighted = item.isHighlighted || (item.hasSubMenu && item.isExpanded)
    val contentColor = if (isHighlighted) COLOR_ACTIVE_TEXT else COLOR_NORMAL_TEXT
    val rowBackground = if (isHighlighted) COLOR_EXPANDED_BG else Color.Transparent

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(50.dp)
            .padding(horizontal = 4.dp)
            .background(
                color = rowBackground,
                shape = RoundedCornerShape(10.dp)
            )
            .clickable(onClick = onClick)
            .padding(horizontal = 15.dp),
        contentAlignment = Alignment.CenterStart
    ) {
        Row(
            modifier = Modifier.fillMaxSize(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Blue left accent bar
            Box(
                modifier = Modifier
                    .width(3.dp)
                    .height(30.dp)
                    .background(
                        color = if (item.isHighlighted) COLOR_CHECKED_BAR else Color.Transparent
                    )
            )

            Spacer(modifier = Modifier.width(10.dp))

            // Icon
            if (item.iconResId != 0) {
                Icon(
                    painter = painterResource(id = item.iconResId),
                    contentDescription = item.title,
                    tint = contentColor,
                    modifier = Modifier.size(22.dp)
                )
            }

            Spacer(modifier = Modifier.width(15.dp))

            // Title Label
            Text(
                text = item.title,
                color = contentColor,
                fontSize = 15.sp,
                fontFamily = GillSansFontFamily,
                modifier = Modifier.weight(1f)
            )

            // Chevron arrow
            if (item.hasSubMenu) {
                val rotationAngle by animateFloatAsState(
                    targetValue = if (item.isExpanded) 180f else 0f,
                    animationSpec = tween(durationMillis = 200)
                )
                Icon(
                    painter = painterResource(id = R.drawable.down_arrow),
                    contentDescription = "Expand",
                    tint = contentColor,
                    modifier = Modifier
                        .size(20.dp)
                        .rotate(rotationAngle)
                )
            }
        }
    }
}

@Composable
fun SidebarSubItemRow(
    subItem: SidebarSubItemState,
    sixtySevenDp: Int,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val COLOR_EXPANDED_BG = Color(0xFFE8F6FF)
    val COLOR_ACTIVE_TEXT = Color(0xFF004D87)
    val COLOR_NORMAL_TEXT = Color(0xFF888888)

    val contentColor = if (subItem.isHighlighted) COLOR_ACTIVE_TEXT else COLOR_NORMAL_TEXT
    val background = if (subItem.isHighlighted) COLOR_EXPANDED_BG else Color.Transparent

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(45.dp)
            .padding(start = 4.dp, end = 4.dp, top = 4.dp, bottom = 3.dp)
            .background(
                color = background,
                shape = RoundedCornerShape(8.dp)
            )
            .clickable(onClick = onClick)
            .padding(
                start = sixtySevenDp.dp,
                top = 10.dp,
                bottom = 10.dp,
                end = 8.dp
            ),
        contentAlignment = Alignment.CenterStart
    ) {
        Text(
            text = subItem.label,
            color = contentColor,
            fontSize = 15.sp,
            fontFamily = GillSansFontFamily
        )
    }
}
