package com.digicoffer.lauditor.AuditTrails.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.digicoffer.lauditor.AuditTrails.Model.AuditTrailsUiState
import com.digicoffer.lauditor.R
import com.digicoffer.lauditor.core.designsystem.components.*

@Composable
fun AuditTrailsScreen(
    state: AuditTrailsUiState,
    categories: List<String>,
    onCategorySelected: (String) -> Unit,
    onQueryChanged: (String) -> Unit,
    onStartDateClick: () -> Unit,
    onEndDateClick: () -> Unit,
    onClearStartDate: () -> Unit,
    onClearEndDate: () -> Unit,
    onPageSelected: (Int) -> Unit,
    onClearCategory: () -> Unit,
    onAdvancedSearchToggle: () -> Unit,
    isDatePickerVisible: Boolean,
    startDateText: String,
    endDateText: String,
    modifier: Modifier = Modifier
) {
    // Define exact Font Family matching legacy
    val GillSansFontFamily = FontFamily(
        Font(R.font.gill_sans, FontWeight.Normal),
        Font(R.font.gill_sans_bold, FontWeight.Bold)
    )

    val baseTextStyle = TextStyle(
        fontFamily = GillSansFontFamily,
        fontSize = 15.sp
    )

    val historyActTextStyle = TextStyle(
        fontFamily = GillSansFontFamily,
        fontSize = 20.sp,
        fontWeight = FontWeight.Bold
    )

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFFE4F2FF)) // EXACT background color R.color.blue_pale
    ) {
        // Main Filters Card (cv_fetch)
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(10.dp)
                .shadow(4.dp, shape = RoundedCornerShape(8.dp))
                .background(Color.White, shape = RoundedCornerShape(8.dp))
                .padding(10.dp)
        ) {
            Column(
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // History of Actions header
                Text(
                    text = "History of Actions",
                    style = historyActTextStyle,
                    color = Color(0xFF004D87)
                )

                // Category selector title with red asterisk
                val categoryLabel = buildAnnotatedString {
                    withStyle(style = SpanStyle(color = Color(0xFF004D87), fontFamily = GillSansFontFamily, fontSize = 15.sp)) {
                        append("Category ")
                    }
                    withStyle(style = SpanStyle(color = Color.Red, fontFamily = GillSansFontFamily, fontSize = 15.sp)) {
                        append("*")
                    }
                }
                Text(text = categoryLabel)

                // Dropdown Selector Field
                DropdownSelectorField(
                    items = categories,
                    selectedItem = state.selectedCategory.ifEmpty { null },
                    onItemSelected = onCategorySelected,
                    itemToLabel = { it },
                    placeholder = "Select Category",
                    onClearSelection = if (state.selectedCategory.isNotEmpty()) onClearCategory else null,
                    textStyle = baseTextStyle
                )

                // Search Bar
                AuditSearchBar(
                    query = state.searchQuery,
                    onQueryChange = onQueryChanged,
                    placeholder = "Search",
                    textStyle = baseTextStyle,
                    borderColor = Color.Transparent,
                    backgroundColor = Color.White
                )

                // Advanced Search Toggle Button (Underlined blue text)
                Text(
                    text = "Advanced Search",
                    style = baseTextStyle.copy(
                        color = Color(0xFF004D87),
                        textDecoration = TextDecoration.Underline
                    ),
                    modifier = Modifier
                        .clickable { onAdvancedSearchToggle() }
                        .padding(vertical = 4.dp)
                )

                // Date Interval Selection Collapsible layout
                if (isDatePickerVisible) {
                    DateIntervalSelector(
                        startDateText = startDateText,
                        endDateText = endDateText,
                        onStartDateClick = onStartDateClick,
                        onEndDateClick = onEndDateClick,
                        onClearStartDate = onClearStartDate,
                        onClearEndDate = onClearEndDate,
                        textStyle = baseTextStyle
                    )
                }
            }
        }

        // List Area
        if (state.isLoading) {
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = Color(0xFF004D87))
            }
        } else if (state.pageItems.isEmpty()) {
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                EmptyStateCard(
                    title = "Data not available",
                    subtitle = "There are no matching records.",
                    textStyle = baseTextStyle
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = 6.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                items(state.pageItems) { audit ->
                    // Determine Category display label format
                    val categoryLabel = when (audit.name) {
                        "AUTH" -> "Authentication"
                        "TEAM MEMBER" -> "Team Member"
                        "RELATIONSHIP" -> "Relationship"
                        "RELATIONSHIP INVITE" -> "Relationship Invite"
                        "SHARE" -> "Share"
                        "DOCUMENT" -> "Document"
                        "LEGAL MATTER" -> "Legal Matter"
                        "GENERAL MATTER" -> "General Matter"
                        "GROUPS" -> "Groups"
                        else -> audit.name ?: ""
                    }

                    AuditTrailCard(
                        categoryName = categoryLabel,
                        timestamp = audit.timestamp ?: "",
                        messageBody = audit.message ?: "",
                        textStyle = baseTextStyle
                    )
                }
            }
        }

        // Pagination layout
        ComposeHorizontalPagerRibbon(
            currentPage = state.currentPage,
            totalPages = state.totalPages,
            onPageSelected = onPageSelected,
            textStyle = baseTextStyle
        )
    }
}
