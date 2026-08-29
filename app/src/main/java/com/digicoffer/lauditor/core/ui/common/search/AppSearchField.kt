package com.digicoffer.lauditor.core.ui.common.search

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.digicoffer.lauditor.R
import com.digicoffer.lauditor.core.designsystem.colors.ColorTokens
import com.digicoffer.lauditor.core.designsystem.theme.LauditorTheme

private val GillSans = FontFamily(Font(R.font.gill_sans))

/**
 * Canonical Search Field component for the application.
 * Supports standard rounded pill style with search icon, clear button,
 * and optional integrated right-aligned "Search" action button.
 */
@Composable
fun AppSearchField(
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    onClearClick: (() -> Unit)? = null,
    onSearchClick: (() -> Unit)? = null,
    searchButtonText: String = "Search",
    placeholder: String = "Search...",
    shape: Shape = RoundedCornerShape(30.dp),
    border: BorderStroke? = null,
    backgroundColor: Color = Color.White,
    elevation: Dp = 4.dp,
    height: Dp = 40.dp,
    textStyle: TextStyle = TextStyle(
        color = Color.Black,
        fontFamily = GillSans,
        fontSize = 15.sp
    ),
    searchIcon: @Composable (() -> Unit)? = {
        Image(
            painter = painterResource(id = R.drawable.search_grey),
            contentDescription = "Search",
            modifier = Modifier
                .size(20.dp)
                .padding(end = 4.dp)
        )
    },
    actionButton: @Composable (() -> Unit)? = null,
    onSearchKeyboardAction: (() -> Unit)? = null
) {
    Card(
        shape = shape,
        colors = CardDefaults.cardColors(containerColor = backgroundColor),
        elevation = CardDefaults.cardElevation(defaultElevation = elevation),
        border = border,
        modifier = modifier
            .fillMaxWidth()
            .height(height)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Spacer(modifier = Modifier.width(10.dp))

            if (searchIcon != null) {
                searchIcon()
            }

            Box(
                modifier = Modifier
                    .weight(1f)
                    .padding(start = 2.dp, end = 4.dp),
                contentAlignment = Alignment.CenterStart
            ) {
                if (value.isEmpty()) {
                    Text(
                        text = placeholder,
                        style = textStyle.copy(color = Color.Gray)
                    )
                }
                BasicTextField(
                    value = value,
                    onValueChange = onValueChange,
                    singleLine = true,
                    cursorBrush = SolidColor(Color.Black),
                    textStyle = textStyle,
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                    keyboardActions = KeyboardActions(onSearch = { 
                        (onSearchKeyboardAction ?: onSearchClick)?.invoke() 
                    }),
                    modifier = Modifier.fillMaxWidth()
                )
            }

            if (value.isNotEmpty()) {
                IconButton(
                    onClick = { onClearClick?.invoke() ?: onValueChange("") },
                    modifier = Modifier.size(24.dp)
                ) {
                    Icon(
                        Icons.Default.Clear,
                        contentDescription = "Clear search",
                        tint = Color.Gray
                    )
                }
                Spacer(modifier = Modifier.width(6.dp))
            }

            if (actionButton != null) {
                actionButton()
            } else if (onSearchClick != null) {
                Button(
                    onClick = onSearchClick,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = ColorTokens.BluePrimary,
                        contentColor = Color.White
                    ),
                    shape = RoundedCornerShape(
                        topStart = 0.dp,
                        bottomStart = 0.dp,
                        topEnd = 30.dp,
                        bottomEnd = 30.dp
                    ),
                    contentPadding = PaddingValues(horizontal = 20.dp),
                    modifier = Modifier.fillMaxHeight(),
                    elevation = null
                ) {
                    Text(
                        text = searchButtonText,
                        fontFamily = GillSans,
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp,
                        color = Color.White
                    )
                }
            }
        }
    }
}

@Preview(showBackground = true, name = "AppSearchField Simple Preview")
@Composable
fun AppSearchFieldSimplePreview() {
    LauditorTheme {
        AppSearchField(value = "", onValueChange = {}, placeholder = "Search notifications...")
    }
}

@Preview(showBackground = true, name = "AppSearchField With Button Preview")
@Composable
fun AppSearchFieldWithButtonPreview() {
    LauditorTheme {
        AppSearchField(value = "Legal case", onValueChange = {}, onSearchClick = {}, placeholder = "Search Matter")
    }
}
