package com.digicoffer.lauditor.feature.appointments.presentation.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.digicoffer.lauditor.R
import java.text.SimpleDateFormat
import java.util.Locale

@Composable
fun HistoryNoteItem(
    noteId: String,
    noteText: String,
    createdOn: String,
    onEditClick: () -> Unit,
    onDeleteClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(10.dp)
            .background(Color(0xFFECEFF1), shape = RoundedCornerShape(4.dp))
            .padding(10.dp)
    ) {
        Row(
            verticalAlignment = Alignment.Top,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(
                text = noteText,
                fontSize = 15.sp,
                color = Color.Black,
                fontFamily = FontFamily(Font(R.font.gill_sans_regular)),
                maxLines = 5,
                modifier = Modifier.weight(1f)
            )

            Spacer(modifier = Modifier.width(10.dp))

            // Edit Icon
            Image(
                painter = painterResource(id = R.drawable.edit__icon),
                contentDescription = "Edit Note",
                modifier = Modifier
                    .size(20.dp)
                    .clickable { onEditClick() }
            )

            Spacer(modifier = Modifier.width(10.dp))

            // Delete Icon
            Image(
                painter = painterResource(id = R.drawable.delete_de),
                contentDescription = "Delete Note",
                modifier = Modifier
                    .size(17.dp, 15.dp)
                    .clickable { onDeleteClick() }
            )
        }

        HorizontalDivider(
            thickness = 1.dp,
            color = Color(0xFFB0BEC5),
            modifier = Modifier.padding(top = 10.dp, bottom = 5.dp)
        )

        Text(
            text = formatNoteDate(createdOn),
            fontSize = 15.sp,
            color = Color.Black,
            fontFamily = FontFamily(Font(R.font.gill_sans_regular))
        )
    }
}

private fun formatNoteDate(dateStr: String): String {
    return try {
        // Format date string from yyyy-MM-dd'T'HH:mm:ss to localized format
        val parser = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.ENGLISH)
        val formatter = SimpleDateFormat("MMM dd, yyyy h:mm a", Locale.ENGLISH)
        val date = parser.parse(dateStr)
        if (date != null) formatter.format(date) else dateStr
    } catch (e: Exception) {
        dateStr
    }
}
