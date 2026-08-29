package com.digicoffer.lauditor.feature.documents.presentation.screen

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.digicoffer.lauditor.R
import com.digicoffer.lauditor.Documents.Models.ViewDocumentsModel

private val GillSans = FontFamily(
    Font(R.font.gill_sans)
)

@Composable
fun UpdateTagsDialog(
    doc: ViewDocumentsModel,
    onSave: (String, Map<String, String>) -> Unit,
    onDismiss: () -> Unit
) {
    val initialTags = remember {
        mutableStateMapOf<String, String>().apply {
            val list = doc.tagslist
            if (list != null) {
                for (i in 0 until list.length()) {
                    val obj = list.getJSONObject(i)
                    val k = obj.optString("key")
                    val v = obj.optString("value")
                    if (k.isNotEmpty()) put(k, v)
                }
            }
        }
    }

    var newTagKey by remember { mutableStateOf("") }
    var newTagVal by remember { mutableStateOf("") }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.9f)
                .fillMaxHeight(0.9f)
                .padding(vertical = 24.dp),
            shape = RoundedCornerShape(8.dp),
            color = Color.White
        ) {
            Column(modifier = Modifier.fillMaxSize()) {
                // Header (Update Tag)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color(0xFF004D87))
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Update Tag",
                        color = Color.White,
                        fontFamily = GillSans,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp
                    )
                    IconButton(onClick = onDismiss) {
                        Icon(
                            painter = painterResource(id = R.drawable.cancel_white_icon),
                            contentDescription = "Close Dialog",
                            tint = Color.Unspecified,
                            modifier = Modifier.size(28.dp)
                        )
                    }
                }

                // Body content
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .padding(16.dp)
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    // Tag Type * Label
                    Text(
                        text = buildAnnotatedString {
                            withStyle(SpanStyle(color = Color(0xFF004D87), fontFamily = GillSans, fontWeight = FontWeight.Bold)) {
                                append("Tag Type")
                            }
                            withStyle(SpanStyle(color = Color.Red, fontFamily = GillSans, fontWeight = FontWeight.Bold)) {
                                append(" *")
                            }
                        },
                        fontSize = 14.sp
                    )
                    
                    // Tag Type OutlinedTextField
                    OutlinedTextField(
                        value = newTagKey,
                        onValueChange = { if (it.length <= 30) newTagKey = it },
                        placeholder = { Text("Tag Type", fontSize = 14.sp, fontFamily = GillSans) },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Color(0xFF004D87),
                            unfocusedBorderColor = Color(0xFFCCCCCC)
                        )
                    )
                    Text(
                        text = "${newTagKey.length}/30",
                        color = Color.Gray,
                        fontFamily = GillSans,
                        fontSize = 12.sp,
                        modifier = Modifier.padding(start = 4.dp)
                    )

                    // Tag * Label
                    Text(
                        text = buildAnnotatedString {
                            withStyle(SpanStyle(color = Color(0xFF004D87), fontFamily = GillSans, fontWeight = FontWeight.Bold)) {
                                append("Tag")
                            }
                            withStyle(SpanStyle(color = Color.Red, fontFamily = GillSans, fontWeight = FontWeight.Bold)) {
                                append(" *")
                            }
                        },
                        fontSize = 14.sp
                    )
                    
                    // Tag OutlinedTextField
                    OutlinedTextField(
                        value = newTagVal,
                        onValueChange = { if (it.length <= 100) newTagVal = it },
                        placeholder = { Text("Tag", fontSize = 14.sp, fontFamily = GillSans) },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Color(0xFF004D87),
                            unfocusedBorderColor = Color(0xFFCCCCCC)
                        )
                    )
                    Text(
                        text = "${newTagVal.length}/100",
                        color = Color.Gray,
                        fontFamily = GillSans,
                        fontSize = 12.sp,
                        modifier = Modifier.padding(start = 4.dp)
                    )

                    // Add Button on the right
                    Box(
                        modifier = Modifier.fillMaxWidth(),
                        contentAlignment = Alignment.CenterEnd
                    ) {
                        Button(
                            onClick = {
                                if (newTagKey.trim().isNotEmpty() && newTagVal.trim().isNotEmpty()) {
                                    initialTags[newTagKey.trim()] = newTagVal.trim()
                                    newTagKey = ""
                                    newTagVal = ""
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF004D87)),
                            shape = RoundedCornerShape(6.dp),
                            modifier = Modifier.width(90.dp).height(38.dp),
                            contentPadding = PaddingValues(0.dp)
                        ) {
                            Text(
                                text = "Add",
                                color = Color.White,
                                fontFamily = GillSans,
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp
                            )
                        }
                    }

                    // Existing Tags Header
                    if (initialTags.isNotEmpty()) {
                        Text(
                            text = "Current Tags:",
                            color = Color.Gray,
                            fontFamily = GillSans,
                            fontSize = 14.sp,
                            modifier = Modifier.padding(top = 10.dp)
                        )
                        
                        // List of tags
                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            initialTags.entries.forEach { entry ->
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .background(Color(0xFFF9F9F9), shape = RoundedCornerShape(4.dp))
                                        .padding(horizontal = 10.dp, vertical = 6.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "${entry.key} - ${entry.value}",
                                        fontSize = 13.sp,
                                        fontFamily = GillSans,
                                        color = Color.Black
                                    )
                                    IconButton(
                                        onClick = { initialTags.remove(entry.key) },
                                        modifier = Modifier.size(20.dp)
                                    ) {
                                        Icon(
                                            painter = painterResource(id = R.drawable.cancel_red_icon),
                                            contentDescription = "Delete Tag",
                                            tint = Color.Unspecified,
                                            modifier = Modifier.size(16.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                // Bottom Buttons (Cancel / Save)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Button(
                        onClick = onDismiss,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFFEFEFEF)
                        ),
                        border = BorderStroke(0.5.dp, Color(0xFFCCCCCC)),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier
                            .width(100.dp)
                            .height(40.dp),
                        contentPadding = PaddingValues(0.dp)
                    ) {
                        Text(
                            text = "Cancel",
                            color = Color.Black,
                            fontFamily = GillSans,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp
                        )
                    }
                    Spacer(modifier = Modifier.width(24.dp))
                    Button(
                        onClick = { onSave(doc.name ?: "", initialTags.toMap()) },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF004D87)
                        ),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier
                            .width(100.dp)
                            .height(40.dp),
                        contentPadding = PaddingValues(0.dp)
                    ) {
                        Text(
                            text = "Save",
                            color = Color.White,
                            fontFamily = GillSans,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp
                        )
                    }
                }
            }
        }
    }
}
