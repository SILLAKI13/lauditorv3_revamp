package com.digicoffer.lauditor.core.ui.common.inputs

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.RadioButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.digicoffer.lauditor.core.designsystem.theme.LauditorTheme
import com.digicoffer.lauditor.core.ui.common.foundation.AppText

@Composable
fun AppRadioButton(
    selected: Boolean,
    onClick: () -> Unit,
    label: String,
    modifier: Modifier = Modifier,
    enabled: Boolean = true
) {
    Row(
        modifier = modifier.clickable(enabled = enabled, onClick = onClick),
        verticalAlignment = Alignment.CenterVertically
    ) {
        RadioButton(selected = selected, onClick = onClick, enabled = enabled)
        AppText(text = label, modifier = Modifier.padding(start = LauditorTheme.dimensions.small))
    }
}

@Preview(showBackground = true, name = "AppRadioButton Preview")
@Composable
fun AppRadioButtonPreview() {
    LauditorTheme {
        AppRadioButton(selected = true, onClick = {}, label = "Radio Choice")
    }
}
