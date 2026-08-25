package com.digicoffer.lauditor.feature.notifications.presentation.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.digicoffer.lauditor.R
import com.digicoffer.lauditor.core.designsystem.theme.LauditorTheme
import com.digicoffer.lauditor.core.ui.common.search.AppSearchField

private val GillSans = FontFamily(Font(R.font.gill_sans))

@Composable
fun NotificationsSearchBar(
    query: String,
    onQueryChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    placeholder: String = "Search notifications..."
) {
    AppSearchField(
        value = query,
        onValueChange = onQueryChange,
        onClearClick = { onQueryChange("") },
        modifier = modifier.padding(10.dp),
        placeholder = placeholder,
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
                contentDescription = "Search",
                modifier = Modifier
                    .size(20.dp)
                    .padding(end = 4.dp)
            )
        }
    )
}

@Preview(showBackground = true, name = "NotificationsSearchBar Preview")
@Composable
fun NotificationsSearchBarPreview() {
    LauditorTheme {
        NotificationsSearchBar(query = "", onQueryChange = {})
    }
}
