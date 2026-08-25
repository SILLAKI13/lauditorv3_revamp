package com.digicoffer.lauditor.feature.matter.presentation.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.digicoffer.lauditor.R
import com.digicoffer.lauditor.core.designsystem.colors.ColorTokens
import com.digicoffer.lauditor.core.ui.common.search.AppSearchField

private val GillSans = FontFamily(Font(R.font.gill_sans))

@Composable
fun MatterSearchBar(
    query: String,
    onQueryChanged: (String) -> Unit,
    onSearchClick: () -> Unit,
    placeholderHint: String = "Search Matter",
    modifier: Modifier = Modifier
) {
    AppSearchField(
        value = query,
        onValueChange = onQueryChanged,
        onClearClick = { onQueryChanged("") },
        modifier = modifier.padding(10.dp),
        placeholder = placeholderHint,
        shape = RoundedCornerShape(30.dp),
        backgroundColor = Color.White,
        elevation = 4.dp,
        height = 40.dp,
        textStyle = TextStyle(
            color = Color.Black,
            fontFamily = GillSans,
            fontSize = 15.sp
        ),
        searchIcon = {
            Image(
                painter = painterResource(id = R.drawable.search_grey),
                contentDescription = "search",
                modifier = Modifier
                    .size(20.dp)
                    .padding(end = 4.dp)
            )
        },
        actionButton = {
            Button(
                onClick = onSearchClick,
                colors = ButtonDefaults.buttonColors(
                    containerColor = ColorTokens.BluePrimary,
                    contentColor = Color.White
                ),
                shape = RoundedCornerShape(0.dp),
                modifier = Modifier.fillMaxHeight(),
                elevation = null
            ) {
                Text(
                    text = "Search",
                    fontFamily = GillSans,
                    fontSize = 15.sp,
                    color = Color.White
                )
            }
        },
        onSearchKeyboardAction = onSearchClick
    )
}
