package com.digicoffer.lauditor.core.ui.common.search

import com.digicoffer.lauditor.core.ui.common.foundation.AppText
import com.digicoffer.lauditor.core.ui.common.inputs.AppTextField

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.ui.Alignment
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.digicoffer.lauditor.R
import com.digicoffer.lauditor.core.designsystem.theme.LauditorTheme

@Composable
fun AppSearchField(
    value: String,
    onValueChange: (String) -> Unit,
    onClearClick: () -> Unit,
    modifier: Modifier = Modifier,
    placeholder: String = "Search...",
    // Flexible style overrides:
    shape: Shape? = null,
    border: BorderStroke? = null,
    backgroundColor: Color? = null,
    elevation: Dp? = null,
    height: Dp? = null,
    textStyle: TextStyle? = null,
    searchIcon: @Composable (() -> Unit)? = null,
    actionButton: @Composable (() -> Unit)? = null,
    onSearchKeyboardAction: (() -> Unit)? = null
) {
    val isCustomStyle = height != null || backgroundColor != null || border != null || elevation != null || actionButton != null

    if (isCustomStyle) {
        val baseShape = shape ?: androidx.compose.foundation.shape.RoundedCornerShape(4.dp)
        val baseElev = elevation ?: 0.dp
        
        Card(
            shape = baseShape,
            colors = CardDefaults.cardColors(containerColor = backgroundColor ?: Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = baseElev),
            border = border,
            modifier = modifier
                .fillMaxWidth()
                .then(if (height != null) Modifier.height(height) else Modifier)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .then(if (height != null) Modifier.height(height) else Modifier)
                    .padding(horizontal = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (searchIcon != null) {
                    searchIcon()
                } else {
                    Icon(
                        Icons.Default.Search,
                        contentDescription = "Search",
                        modifier = Modifier.size(24.dp)
                    )
                }
                
                Spacer(modifier = Modifier.width(10.dp))
                
                Box(
                    modifier = Modifier.weight(1f),
                    contentAlignment = Alignment.CenterStart
                ) {
                    BasicTextField(
                        value = value,
                        onValueChange = onValueChange,
                        singleLine = true,
                        textStyle = textStyle ?: TextStyle.Default,
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                        keyboardActions = KeyboardActions(onSearch = { onSearchKeyboardAction?.invoke() }),
                        modifier = Modifier.fillMaxWidth(),
                        decorationBox = { innerTextField ->
                            if (value.isEmpty()) {
                                Text(
                                    text = placeholder,
                                    style = textStyle ?: TextStyle.Default,
                                    color = Color.Gray
                                )
                            }
                            innerTextField()
                        }
                    )
                }
                
                if (value.isNotEmpty()) {
                    IconButton(
                        onClick = onClearClick,
                        modifier = Modifier.size(24.dp)
                    ) {
                        Icon(
                            Icons.Default.Clear,
                            contentDescription = "Clear search"
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                }
                
                if (actionButton != null) {
                    actionButton()
                }
            }
        }
    } else {
        AppTextField(
            value = value,
            onValueChange = onValueChange,
            modifier = modifier,
            placeholder = placeholder,
            leadingIcon = { Icon(Icons.Default.Search, contentDescription = "Search") },
            trailingIcon = {
                if (value.isNotEmpty()) {
                    IconButton(onClick = onClearClick) {
                        Icon(Icons.Default.Clear, contentDescription = "Clear search")
                    }
                }
            },
            singleLine = true
        )
    }
}

@Preview(showBackground = true, name = "AppSearchField Preview")
@Composable
fun AppSearchFieldPreview() {
    LauditorTheme {
        AppSearchField(value = "Search Query", onValueChange = {}, onClearClick = {})
    }
}
