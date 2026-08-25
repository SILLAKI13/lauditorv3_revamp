package com.digicoffer.lauditor.feature.timesheets.presentation.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.digicoffer.lauditor.R
import com.digicoffer.lauditor.core.designsystem.typography.FontTokens
import com.digicoffer.lauditor.core.designsystem.theme.LauditorTheme
import com.digicoffer.lauditor.core.ui.common.cards.AppCard
import com.digicoffer.lauditor.core.ui.common.foundation.AppDivider
import com.digicoffer.lauditor.core.ui.common.foundation.AppSpacer
import com.digicoffer.lauditor.core.ui.common.foundation.AppText

@Composable
fun DailyTimeSheetBlock(
    dateText: String,
    totalHoursText: String,
    entriesList: List<TimeSheetEntryUiModel>,
    onEditClick: (TimeSheetEntryUiModel) -> Unit,
    onDeleteClick: (TimeSheetEntryUiModel) -> Unit,
    modifier: Modifier = Modifier
) {
    AppCard(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp, horizontal = 10.dp), // 10dp/15dp layout margins
        backgroundColor = Color.White,
        shape = RoundedCornerShape(8.dp),
        elevation = 4.dp // Matches elevation 4dp in legacy XML
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp)
        ) {
            // 1. Date Header Text (17sp, Regular, Blue)
            AppText(
                text = dateText,
                color = Color(0xFF004D87),
                style = TextStyle(fontSize = 17.sp, fontFamily = FontTokens.DefaultFontFamily),
                modifier = Modifier.padding(start = 10.dp, top = 10.dp, bottom = 4.dp)
            )

            AppSpacer(height = 6.dp)

            // 2. Vertical list of timesheet entry rows (Flat rows matching legacy list spacing)
            Column(modifier = Modifier.fillMaxWidth()) {
                entriesList.forEachIndexed { index, entry ->
                    WeeklyTimeSheetItem(
                        entry = entry,
                        onEditClick = { onEditClick(entry) },
                        onDeleteClick = { onDeleteClick(entry) },
                        cardPadding = PaddingValues(start = 10.dp, top = 6.dp, end = 10.dp, bottom = 6.dp)
                    )
                    // Very thin grey separator line between rows inside the daily block card (matching legacy layout)
                    if (index < entriesList.size - 1) {
                        AppSpacer(height = 4.dp)
                        AppDivider()
                        AppSpacer(height = 4.dp)
                    }
                }
            }

            AppSpacer(height = 10.dp)

            // 3. Bottom Row: Total Hours (Label Blue, Value Black, Regular font weight)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 6.dp, bottom = 4.dp), // Start margin 6dp
                verticalAlignment = Alignment.CenterVertically
            ) {
                AppText(
                    text = stringResource(id = R.string.total_hours),
                    color = Color(0xFF004D87),
                    style = TextStyle(fontSize = 18.sp, fontFamily = FontTokens.DefaultFontFamily)
                )
                AppSpacer(width = 10.dp)
                AppText(
                    text = totalHoursText,
                    color = Color.Black,
                    style = TextStyle(fontSize = 18.sp, fontFamily = FontTokens.DefaultFontFamily)
                )
            }
        }
    }
}
