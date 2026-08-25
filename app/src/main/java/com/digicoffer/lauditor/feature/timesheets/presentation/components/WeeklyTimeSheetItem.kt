package com.digicoffer.lauditor.feature.timesheets.presentation.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.digicoffer.lauditor.R
import com.digicoffer.lauditor.core.designsystem.typography.FontTokens
import com.digicoffer.lauditor.core.designsystem.theme.LauditorTheme
import com.digicoffer.lauditor.core.ui.common.foundation.AppSpacer
import com.digicoffer.lauditor.core.ui.common.foundation.AppText

data class TimeSheetEntryUiModel(
    val id: String,
    val matterName: String,
    val hours: String,
    val minutes: String,
    val billableStatus: String,
    val taskName: String,
    val isEditable: Boolean,
    val isSubmitted: Boolean
)

@Composable
fun WeeklyTimeSheetItem(
    entry: TimeSheetEntryUiModel,
    onEditClick: (() -> Unit)? = null,
    onDeleteClick: (() -> Unit)? = null,
    showTotalHours: Boolean = false,
    totalHoursValue: String = "",
    tmLabelText: String? = null,
    cardPadding: PaddingValues = PaddingValues(start = 15.dp, top = 8.dp, end = 8.dp, bottom = 8.dp),
    matterNameColor: Color = Color(0xFF004D87),
    dividerColor: Color? = null
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(cardPadding)
    ) {
        // 1. Matter Title Header (18sp, Regular, Blue)
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (tmLabelText != null) {
                AppText(
                    text = tmLabelText,
                    color = Color(0xFF004D87),
                    style = TextStyle(fontSize = 18.sp, fontFamily = FontTokens.DefaultFontFamily)
                )
            }
            AppText(
                text = entry.matterName,
                color = matterNameColor,
                style = TextStyle(fontSize = 18.sp, fontFamily = FontTokens.DefaultFontFamily)
            )
        }

        AppSpacer(height = 6.dp)

        // 2. Info Grid Content Row
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Left Column: Hours & Minutes (15sp, Regular, Black, wrap content)
            Column(
                modifier = Modifier.wrapContentWidth(),
                horizontalAlignment = Alignment.Start
            ) {
                AppText(
                    text = entry.hours,
                    color = Color.Black,
                    style = TextStyle(fontSize = 15.sp, fontFamily = FontTokens.DefaultFontFamily)
                )
                AppSpacer(height = 2.dp)
                AppText(
                    text = entry.minutes,
                    color = Color.Black,
                    style = TextStyle(fontSize = 15.sp, fontFamily = FontTokens.DefaultFontFamily)
                )
            }

            // Dotted vertical line separator (18dp total space, very close to columns)
            Canvas(
                modifier = Modifier
                    .padding(horizontal = 8.dp)
                    .width(2.dp)
                    .height(36.dp)
            ) {
                val pathEffect = PathEffect.dashPathEffect(floatArrayOf(6f, 6f), 0f)
                drawLine(
                    color = Color(0xFFCCCCCC),
                    start = Offset(0f, 0f),
                    end = Offset(0f, size.height),
                    strokeWidth = 2f,
                    pathEffect = pathEffect
                )
            }

            // Center-Right Column: Billable & Task Name (15sp, Regular, Blue)
            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(end = 4.dp),
                horizontalAlignment = Alignment.Start
            ) {
                AppText(
                    text = entry.billableStatus,
                    color = Color(0xFF004D87),
                    style = TextStyle(fontSize = 15.sp, fontFamily = FontTokens.DefaultFontFamily)
                )
                AppSpacer(height = 2.dp)
                AppText(
                    text = entry.taskName,
                    color = Color(0xFF004D87),
                    style = TextStyle(fontSize = 15.sp, fontFamily = FontTokens.DefaultFontFamily)
                )
            }

            // Action Buttons Column
            if (!entry.isSubmitted && entry.matterName.isNotEmpty()) {
                Row(
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(end = 4.dp)
                ) {
                    if (entry.isEditable && onEditClick != null) {
                        Image(
                            painter = painterResource(id = R.drawable.edit_new_icon_),
                            contentDescription = stringResource(id = R.string.edit),
                            modifier = Modifier
                                .size(24.dp)
                                .clickable { onEditClick() }
                        )
                    }
                    if (onDeleteClick != null) {
                        AppSpacer(width = 8.dp)
                        Image(
                            painter = painterResource(id = R.drawable.delete_de),
                            contentDescription = stringResource(id = R.string.delete),
                            modifier = Modifier
                                .size(22.dp)
                                .clickable { onDeleteClick() }
                        )
                    }
                }
            }
        }

        // 3. Bottom Row: Total Hours (For Project aggregates)
        if (showTotalHours) {
            AppSpacer(height = 8.dp)
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                AppText(
                    text = stringResource(id = R.string.total_hours),
                    color = Color(0xFF004D87),
                    style = TextStyle(fontSize = 18.sp, fontFamily = FontTokens.DefaultFontFamily)
                )
                AppSpacer(width = 10.dp)
                AppText(
                    text = totalHoursValue,
                    color = Color.Black,
                    style = TextStyle(fontSize = 18.sp, fontFamily = FontTokens.DefaultFontFamily)
                )
            }
        }
    }
}
