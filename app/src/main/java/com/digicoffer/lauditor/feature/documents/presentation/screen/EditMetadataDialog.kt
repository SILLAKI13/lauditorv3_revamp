package com.digicoffer.lauditor.feature.documents.presentation.screen

import android.widget.TextView
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.digicoffer.lauditor.R
import com.digicoffer.lauditor.Documents.Models.ViewDocumentsModel
import com.digicoffer.lauditor.core.ui.common.dialogs.AppDialog
import org.json.JSONObject
import java.util.Calendar
import java.util.Date
import java.text.SimpleDateFormat
import java.util.Locale

private val GillSans = FontFamily(
    Font(R.font.gill_sans)
)

@Composable
fun EditMetadataDialog(
    doc: ViewDocumentsModel,
    initialDownloadDisabled: Boolean,
    initialEncrypted: Boolean,
    isStaged: Boolean = true,
    onSave: (String, String, String, Boolean, Boolean, JSONObject?) -> Unit,
    onDismiss: () -> Unit
) {
    var name by remember { mutableStateOf(doc.name ?: "") }
    var description by remember { mutableStateOf(doc.description ?: "") }
    var expirationDate by remember { mutableStateOf(doc.expiration_date ?: "") }
    
    // Switch states
    var enableDownload by remember { mutableStateOf(!initialDownloadDisabled) }
    var enableEncryption by remember { mutableStateOf(initialEncrypted) }

    // Tags list mapping
    val tagsMap = remember {
        mutableStateMapOf<String, String>().apply {
            // First load from tagslist
            val list = doc.tagslist
            if (list != null) {
                for (i in 0 until list.length()) {
                    val obj = list.optJSONObject(i)
                    if (obj != null) {
                        val k = obj.optString("key")
                        val v = obj.optString("value")
                        if (k.isNotEmpty()) {
                            put(k, v)
                        }
                    }
                }
            }
            // Fallback to tags JSONObject if empty
            if (isEmpty()) {
                val tagObj = doc.tag
                if (tagObj != null) {
                    val keys = tagObj.keys()
                    while (keys.hasNext()) {
                        val k = keys.next()
                        val v = tagObj.optString(k)
                        if (k.isNotEmpty()) {
                            put(k, v)
                        }
                    }
                }
            }
        }
    }

    // Add Tag Sub-Dialog state variables
    var showAddTagDialog by remember { mutableStateOf(false) }
    var tagToEditKey by remember { mutableStateOf<String?>(null) }
    var tagToEditVal by remember { mutableStateOf<String?>(null) }

    // Alert dialog state
    var datePickerError by remember { mutableStateOf<String?>(null) }

    val context = LocalContext.current

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.9f)
                .padding(horizontal = 16.dp, vertical = 24.dp),
            shape = RoundedCornerShape(8.dp),
            color = Color.White
        ) {
            Column(modifier = Modifier.fillMaxSize()) {
                // Header (Edit Metadata)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color(0xFF004D87))
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Edit Metadata",
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp
                    )
                    IconButton(onClick = onDismiss) {
                        Icon(
                            painter = painterResource(id = R.drawable.cancel_white_icon),
                            contentDescription = "Close Dialog",
                            tint = Color.Unspecified,
                            modifier = Modifier.size(24.dp)
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
                    // Document Name *
                    Text(
                        text = buildAnnotatedString {
                            withStyle(SpanStyle(color = Color(0xFF004D87), fontWeight = FontWeight.Bold)) {
                                append("Document Name")
                            }
                            withStyle(SpanStyle(color = Color.Red, fontWeight = FontWeight.Bold)) {
                                append(" *")
                            }
                        },
                        fontSize = 14.sp
                    )
                    OutlinedTextField(
                        value = name,
                        onValueChange = { if (it.length <= 50) name = it },
                        placeholder = { Text("Enter document name", fontSize = 14.sp) },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Color(0xFF004D87),
                            unfocusedBorderColor = Color(0xFFCCCCCC)
                        )
                    )
                    Text(
                        text = "${name.length}/50",
                        color = Color.Gray,
                        fontSize = 11.sp,
                        modifier = Modifier.align(Alignment.End)
                    )

                    // Description *
                    Text(
                        text = buildAnnotatedString {
                            withStyle(SpanStyle(color = Color(0xFF004D87), fontWeight = FontWeight.Bold)) {
                                append("Description")
                            }
                            withStyle(SpanStyle(color = Color.Red, fontWeight = FontWeight.Bold)) {
                                append(" *")
                            }
                        },
                        fontSize = 14.sp
                    )
                    OutlinedTextField(
                        value = description,
                        onValueChange = { if (it.length <= 300) description = it },
                        placeholder = { Text("Enter description", fontSize = 14.sp) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(100.dp),
                        maxLines = 5,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Color(0xFF004D87),
                            unfocusedBorderColor = Color(0xFFCCCCCC)
                        )
                    )
                    Text(
                        text = "${description.length}/300",
                        color = Color.Gray,
                        fontSize = 11.sp,
                        modifier = Modifier.align(Alignment.End)
                    )

                    // Expiration Date
                    Text(
                        text = buildAnnotatedString {
                            withStyle(SpanStyle(color = Color(0xFF004D87), fontWeight = FontWeight.Bold)) {
                                append("Expiration Date")
                            }
                        },
                        fontSize = 14.sp
                    )
                    Box(
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        OutlinedTextField(
                            value = expirationDate,
                            onValueChange = {},
                            placeholder = { Text("Expiration Date", fontSize = 14.sp) },
                            modifier = Modifier.fillMaxWidth(),
                            enabled = false,
                            trailingIcon = {
                                Icon(
                                    painter = painterResource(id = R.drawable.ic_calendar_blue),
                                    contentDescription = "Pick Date",
                                    tint = Color.Unspecified,
                                    modifier = Modifier.size(24.dp)
                                )
                            },
                            colors = OutlinedTextFieldDefaults.colors(
                                disabledBorderColor = Color(0xFFCCCCCC),
                                disabledTextColor = Color.Black,
                                disabledPlaceholderColor = Color.Gray
                            )
                        )
                        // Invisible overlay Box to capture clicks and launch custom DatePicker restricted spinner dialog
                        Box(
                            modifier = Modifier
                                .matchParentSize()
                                .background(Color.Transparent)
                                .clickable {
                                    val textView = TextView(context).apply {
                                        text = expirationDate
                                    }
                                    // Limit dates: allowPastDates = false, allowCurrentDate = true, allowFutureDates = true
                                    com.digicoffer.lauditor.CommonFiles.GlobalFiles.AndroidUtils.showDatePicker(
                                        textView,
                                        false,
                                        true,
                                        true
                                    ) {
                                        val raw = textView.text.toString()
                                        val formatted = com.digicoffer.lauditor.CommonFiles.GlobalFiles.AndroidUtils.convertAnyDateToDDMMYYYY(raw)
                                        try {
                                            val sdf = SimpleDateFormat("dd-MM-yyyy", Locale.US)
                                            val selectedDate = sdf.parse(formatted)
                                            val today = Calendar.getInstance().apply {
                                                set(Calendar.HOUR_OF_DAY, 0)
                                                set(Calendar.MINUTE, 0)
                                                set(Calendar.SECOND, 0)
                                                set(Calendar.MILLISECOND, 0)
                                            }.time
                                            if (selectedDate != null && selectedDate.before(today)) {
                                                datePickerError = "Please select today or a future date"
                                                textView.text = ""
                                            } else {
                                                expirationDate = formatted
                                            }
                                        } catch (e: Exception) {
                                            expirationDate = formatted
                                        }
                                    }
                                }
                        )
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    // Enable Download Switch
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Enable Download",
                            color = Color(0xFF004D87),
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp
                        )
                        Switch(
                            checked = enableDownload,
                            onCheckedChange = { enableDownload = it },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = Color.White,
                                checkedTrackColor = Color(0xFF004D87)
                            )
                        )
                    }

                    // Enable Encryption Switch
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Enable Encryption",
                            color = Color(0xFF004D87),
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp
                        )
                        Switch(
                            checked = enableEncryption,
                            onCheckedChange = { enableEncryption = it },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = Color.White,
                                checkedTrackColor = Color(0xFF004D87)
                            )
                        )
                    }

                    // Tags row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Tags",
                            color = Color(0xFF004D87),
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp
                        )
                        Button(
                            onClick = {
                                tagToEditKey = null
                                tagToEditVal = null
                                showAddTagDialog = true
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF004D87)),
                            shape = RoundedCornerShape(4.dp),
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                        ) {
                            Text("Add Tag", color = Color.White, fontSize = 12.sp)
                        }
                    }

                    // Chips layout showing added tags
                    if (tagsMap.isNotEmpty()) {
                        FlowRow(
                            mainAxisSpacing = 8.dp,
                            crossAxisSpacing = 8.dp,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            tagsMap.entries.forEach { entry ->
                                Row(
                                    modifier = Modifier
                                        .background(Color(0xFFE4F2FF), shape = RoundedCornerShape(16.dp))
                                        .border(BorderStroke(1.dp, Color(0xFF0073C6)), shape = RoundedCornerShape(16.dp))
                                        .padding(horizontal = 10.dp, vertical = 6.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Text(
                                        text = "${entry.key} : ${entry.value}",
                                        color = Color.Black,
                                        fontFamily = GillSans,
                                        fontSize = 13.sp
                                    )
                                    Icon(
                                        painter = painterResource(id = R.drawable.edit_new_icon_),
                                        contentDescription = "Edit Tag",
                                        tint = Color(0xFF0073C6),
                                        modifier = Modifier
                                            .size(16.dp)
                                            .clickable {
                                                tagToEditKey = entry.key
                                                tagToEditVal = entry.value
                                                showAddTagDialog = true
                                            }
                                    )
                                    Icon(
                                        painter = painterResource(id = R.drawable.cancel_red_icon),
                                        contentDescription = "Remove Tag",
                                        tint = Color.Unspecified,
                                        modifier = Modifier
                                            .size(16.dp)
                                            .clickable {
                                                tagsMap.remove(entry.key)
                                            }
                                    )
                                }
                            }
                        }
                    }
                }

                // Footer (Action Buttons)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Button(
                        onClick = onDismiss,
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFEFEFEF)),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Cancel", color = Color.Black, fontWeight = FontWeight.Bold)
                    }
                    Button(
                        onClick = {
                            if (name.isNotEmpty() && description.isNotEmpty()) {
                                val tagsJson = if (tagsMap.isNotEmpty()) {
                                    JSONObject().apply {
                                        tagsMap.forEach { (k, v) -> put(k, v) }
                                    }
                                } else {
                                    null
                                }
                                onSave(name, description, expirationDate, !enableDownload, enableEncryption, tagsJson)
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF004D87)),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.weight(1f),
                        enabled = name.isNotEmpty() && description.isNotEmpty()
                    ) {
                        Text("Save", color = Color.White, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }

    // Add/Edit Tag overlay sub-dialog
    if (showAddTagDialog) {
        Dialog(
            onDismissRequest = { showAddTagDialog = false },
            properties = DialogProperties(usePlatformDefaultWidth = false)
        ) {
            Surface(
                modifier = Modifier
                    .fillMaxWidth(0.9f)
                    .wrapContentHeight()
                    .padding(horizontal = 16.dp, vertical = 24.dp),
                shape = RoundedCornerShape(8.dp),
                color = Color.White,
                border = BorderStroke(1.dp, Color.Black)
            ) {
                var keyText by remember { mutableStateOf(tagToEditKey ?: "") }
                var valText by remember { mutableStateOf(tagToEditVal ?: "") }
                
                Column(modifier = Modifier.fillMaxWidth()) {
                    // Sub-dialog Header
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color(0xFF004D87))
                            .padding(horizontal = 16.dp, vertical = 12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = if (tagToEditKey == null) "Add Tag" else "Edit Tag",
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp
                        )
                        IconButton(onClick = { showAddTagDialog = false }) {
                            Icon(
                                painter = painterResource(id = R.drawable.cancel_white_icon),
                                contentDescription = "Close",
                                tint = Color.Unspecified,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                    }

                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        // Tag Type *
                        Text(
                            text = buildAnnotatedString {
                                withStyle(SpanStyle(color = Color(0xFF004D87), fontWeight = FontWeight.Bold)) {
                                    append("Tag Type")
                                }
                                withStyle(SpanStyle(color = Color.Red, fontWeight = FontWeight.Bold)) {
                                    append(" *")
                                }
                            },
                            fontSize = 14.sp
                        )
                        OutlinedTextField(
                            value = keyText,
                            onValueChange = { if (it.length <= 30) keyText = it },
                            placeholder = { Text("Tag Type", fontSize = 14.sp) },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true,
                            enabled = tagToEditKey == null, // Type is unique tag identifier, cannot modify it if editing
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = Color(0xFF004D87),
                                unfocusedBorderColor = Color(0xFFCCCCCC)
                            )
                        )
                        Text(
                            text = "${keyText.length}/30",
                            color = Color.Gray,
                            fontSize = 11.sp,
                            modifier = Modifier.align(Alignment.End)
                        )

                        // Tag *
                        Text(
                            text = buildAnnotatedString {
                                withStyle(SpanStyle(color = Color(0xFF004D87), fontWeight = FontWeight.Bold)) {
                                    append("Tag")
                                }
                                withStyle(SpanStyle(color = Color.Red, fontWeight = FontWeight.Bold)) {
                                    append(" *")
                                }
                            },
                            fontSize = 14.sp
                        )
                        OutlinedTextField(
                            value = valText,
                            onValueChange = { if (it.length <= 100) valText = it },
                            placeholder = { Text("Tag", fontSize = 14.sp) },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = Color(0xFF004D87),
                                unfocusedBorderColor = Color(0xFFCCCCCC)
                            )
                        )
                        Text(
                            text = "${valText.length}/100",
                            color = Color.Gray,
                            fontSize = 11.sp,
                            modifier = Modifier.align(Alignment.End)
                        )

                        // Add/Update Button (saves local modification to tagsMap)
                        Box(
                            modifier = Modifier.fillMaxWidth(),
                            contentAlignment = Alignment.CenterEnd
                        ) {
                            Button(
                                onClick = {
                                    if (keyText.trim().isNotEmpty() && valText.trim().isNotEmpty()) {
                                        tagsMap[keyText.trim()] = valText.trim()
                                        keyText = ""
                                        valText = ""
                                        showAddTagDialog = false
                                    }
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF004D87)),
                                shape = RoundedCornerShape(4.dp),
                                enabled = keyText.trim().isNotEmpty() && valText.trim().isNotEmpty()
                            ) {
                                Text(
                                    text = if (tagToEditKey == null) "Add" else "Update",
                                    color = Color.White,
                                    fontSize = 12.sp
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        // Dialog Confirm buttons
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Button(
                                onClick = { showAddTagDialog = false },
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFEFEFEF)),
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier.weight(1f)
                            ) {
                                Text("Cancel", color = Color.Black, fontWeight = FontWeight.Bold)
                            }
                            Button(
                                onClick = {
                                    if (keyText.trim().isNotEmpty() && valText.trim().isNotEmpty()) {
                                        tagsMap[keyText.trim()] = valText.trim()
                                    }
                                    showAddTagDialog = false
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF004D87)),
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier.weight(1f),
                                enabled = tagsMap.isNotEmpty() || (keyText.trim().isNotEmpty() && valText.trim().isNotEmpty())
                            ) {
                                Text("Save", color = Color.White, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }
        }
    }

    // App error alert dialog
    if (datePickerError != null) {
        AppDialog(
            title = "Alert",
            onConfirm = { datePickerError = null },
            onDismiss = { datePickerError = null },
            confirmText = "OK",
            dismissText = null,
            content = {
                Text(
                    text = datePickerError ?: "",
                    fontFamily = GillSans,
                    fontSize = 14.sp,
                    color = Color.Black,
                    textAlign = TextAlign.Center
                )
            }
        )
    }
}

@Composable
fun FlowRow(
    modifier: Modifier = Modifier,
    mainAxisSpacing: androidx.compose.ui.unit.Dp = 0.dp,
    crossAxisSpacing: androidx.compose.ui.unit.Dp = 0.dp,
    content: @Composable () -> Unit
) {
    androidx.compose.ui.layout.Layout(
        content = content,
        modifier = modifier
    ) { measurables, constraints ->
        val mainAxisSpacingPx = mainAxisSpacing.roundToPx()
        val crossAxisSpacingPx = crossAxisSpacing.roundToPx()

        val rows = mutableListOf<List<androidx.compose.ui.layout.Placeable>>()
        val rowHeights = mutableListOf<Int>()
        var currentRow = mutableListOf<androidx.compose.ui.layout.Placeable>()
        var currentRowWidth = 0
        var currentRowHeight = 0

        measurables.forEach { measurable ->
            val placeable = measurable.measure(constraints.copy(minWidth = 0))
            if (currentRowWidth + placeable.width > constraints.maxWidth && currentRow.isNotEmpty()) {
                rows.add(currentRow)
                rowHeights.add(currentRowHeight)
                currentRow = mutableListOf()
                currentRowWidth = 0
                currentRowHeight = 0
            }
            currentRow.add(placeable)
            currentRowWidth += placeable.width + mainAxisSpacingPx
            currentRowHeight = maxOf(currentRowHeight, placeable.height)
        }
        if (currentRow.isNotEmpty()) {
            rows.add(currentRow)
            rowHeights.add(currentRowHeight)
        }

        val totalHeight = rowHeights.sum() + (rowHeights.size - 1).coerceAtLeast(0) * crossAxisSpacingPx
        val totalWidth = constraints.maxWidth

        layout(totalWidth, totalHeight) {
            var y = 0
            rows.forEachIndexed { rowIndex, row ->
                var x = 0
                row.forEach { placeable ->
                    placeable.placeRelative(x, y)
                    x += placeable.width + mainAxisSpacingPx
                }
                y += rowHeights[rowIndex] + crossAxisSpacingPx
            }
        }
    }
}
