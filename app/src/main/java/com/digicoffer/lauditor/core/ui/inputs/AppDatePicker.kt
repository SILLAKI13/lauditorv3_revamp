package com.digicoffer.lauditor.core.ui.inputs

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.digicoffer.lauditor.core.designsystem.theme.LauditorTheme

@Composable
fun AppDatePicker(
    selectedDate: String,
    onDateClick: () -> Unit,
    modifier: Modifier = Modifier,
    label: String = "Date"
) {
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
