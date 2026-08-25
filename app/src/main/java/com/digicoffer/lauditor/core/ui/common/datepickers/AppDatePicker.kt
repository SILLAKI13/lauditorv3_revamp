package com.digicoffer.lauditor.core.ui.common.datepickers

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.digicoffer.lauditor.core.designsystem.theme.LauditorTheme

@Composable
fun AppDatePicker(
    selectedDate: String,
    onDateClick: () -> Unit,
    modifier: Modifier = Modifier,
    label: String = "Date",
    // Flexible style overrides:
    placeholder: String = "",
    shape: Shape? = null,
    border: BorderStroke? = null,
    backgroundColor: Color? = null,
    textStyle: TextStyle? = null,
    height: Dp? = null,
    contentPadding: PaddingValues? = null,
    iconRes: Int? = null,
    trailingIcon: @Composable (() -> Unit)? = null
) {
    val isCustomStyle = height != null || backgroundColor != null || border != null || iconRes != null || textStyle != null

    if (isCustomStyle) {
        val baseShape = shape ?: androidx.compose.foundation.shape.RoundedCornerShape(0.dp)
        var baseModifier = Modifier.fillMaxWidth().clickable { onDateClick() }
        if (height != null) {
            baseModifier = baseModifier.height(height)
        }
        if (border != null) {
            baseModifier = baseModifier.border(border, baseShape)
        }
        if (backgroundColor != null) {
            baseModifier = baseModifier.background(backgroundColor, baseShape)
        }
        if (contentPadding != null) {
            baseModifier = baseModifier.padding(contentPadding)
        }

        Box(
            modifier = baseModifier,
            contentAlignment = Alignment.CenterStart
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = selectedDate.ifEmpty { placeholder },
                    style = textStyle ?: TextStyle.Default
                )
                if (trailingIcon != null) {
                    trailingIcon()
                } else if (iconRes != null) {
                    Icon(
                        painter = painterResource(id = iconRes),
                        contentDescription = "Select Date",
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }
    } else {
        OutlinedTextField(
            value = selectedDate,
            onValueChange = {},
            readOnly = true,
            label = { Text(label) },
            trailingIcon = {
                IconButton(onClick = onDateClick) {
                    Icon(Icons.Default.DateRange, contentDescription = "Select date")
                }
            },
            modifier = modifier.fillMaxWidth()
        )
    }
}

@Composable
fun AppTimePicker(
    selectedTime: String,
    onTimeClick: () -> Unit,
    modifier: Modifier = Modifier,
    label: String = "Time"
) {
    OutlinedTextField(
        value = selectedTime,
        onValueChange = {},
        readOnly = true,
        label = { Text(label) },
        trailingIcon = {
            IconButton(onClick = onTimeClick) {
                Icon(Icons.Default.DateRange, contentDescription = "Select time")
            }
        },
        modifier = modifier.fillMaxWidth()
    )
}

@Preview(showBackground = true, name = "AppDatePicker Preview")
@Composable
fun AppDatePickerPreview() {
    LauditorTheme {
        AppDatePicker(selectedDate = "2026-07-21", onDateClick = {})
    }
}
