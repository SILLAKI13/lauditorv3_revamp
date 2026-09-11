package com.digicoffer.lauditor.feature.documents.presentation.screen

import com.digicoffer.lauditor.CommonFiles.GlobalFiles.AndroidUtils
import android.widget.TextView
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
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
import com.digicoffer.lauditor.core.ui.common.dialogs.AppConfirmationDialog
import org.json.JSONObject
import java.util.Calendar
import java.util.Date
import java.text.SimpleDateFormat
import java.util.Locale

private val GillSans = FontFamily(
    Font(R.font.gill_sans)
)

private val GillSansBold = FontFamily(
    Font(R.font.gill_sans, FontWeight.Bold)
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
    val defaultBaseName = remember(doc) {
        val raw = doc.name ?: ""
        raw.substringBeforeLast('.', missingDelimiterValue = raw)
    }
    val initialName = remember(doc) {
        if (!doc.name.isNullOrEmpty()) {
            doc.name?.substringBeforeLast('.', missingDelimiterValue = doc.name ?: "") ?: ""
        } else ""
    }
    val initialDescription = remember(doc) {
        if (!doc.description.isNullOrEmpty()) {
            doc.description?.substringBeforeLast('.', missingDelimiterValue = doc.description ?: "") ?: ""
        } else {
            defaultBaseName
        }
    }
    val initialExpirationDate = remember(doc) {
        val raw = doc.expiration_date ?: ""
        if (raw.equals("NA", ignoreCase = true)) "" else raw
    }
    val initialEnableDownloadVal = remember(initialDownloadDisabled, doc) {
        !initialDownloadDisabled
    }
    val initialEnableEncryptionVal = remember(initialEncrypted, doc) {
        initialEncrypted
    }
    val initialTags = remember(doc) {
        val map = mutableMapOf<String, String>()
        val list = doc.tagslist
        if (list != null) {
            for (i in 0 until list.length()) {
                val obj = list.optJSONObject(i)
                if (obj != null) {
                    val k = obj.optString("key")
                    val v = obj.optString("value")
                    if (k.isNotEmpty()) map[k] = v
                }
            }
        }
        if (map.isEmpty()) {
            val tagObj = doc.tag
            if (tagObj != null) {
                val keys = tagObj.keys()
                while (keys.hasNext()) {
                    val k = keys.next()
                    val v = tagObj.optString(k)
                    if (k.isNotEmpty()) map[k] = v
                }
            }
        }
        map
    }

    var name by remember(doc) {
        mutableStateOf(initialName)
    }
    var description by remember(doc) {
        mutableStateOf(initialDescription)
    }
    var expirationDate by remember(doc) {
        mutableStateOf(initialExpirationDate)
    }

    // Switch states: Preserve initial boolean values passed into dialog
    var enableDownload by remember(initialDownloadDisabled, doc) {
        mutableStateOf(initialEnableDownloadVal)
    }
    var enableEncryption by remember(initialEncrypted, doc) {
        mutableStateOf(initialEnableEncryptionVal)
    }

    // Tags list mapping
    val tagsMap = remember(doc) {
        mutableStateMapOf<String, String>().apply {
            putAll(initialTags)
        }
    }

    val hasChanged by remember(name, description, expirationDate, enableDownload, enableEncryption, tagsMap.toMap()) {
        derivedStateOf {
            name != initialName ||
            description != initialDescription ||
            expirationDate != initialExpirationDate ||
            (isStaged && enableDownload != initialEnableDownloadVal) ||
            (isStaged && enableEncryption != initialEnableEncryptionVal) ||
            (isStaged && tagsMap.toMap() != initialTags)
        }
    }

    var showDiscardAlert by remember { mutableStateOf(false) }

    val handleCancelOrClose = {
        if (hasChanged) {
            showDiscardAlert = true
        } else {
            onDismiss()
        }
    }

    // Add Tag Sub-Dialog state variables
    var showAddTagDialog by remember { mutableStateOf(false) }
    var tagToEditKey by remember { mutableStateOf<String?>(null) }
    var tagToEditVal by remember { mutableStateOf<String?>(null) }

    // Alert dialog state
    var datePickerError by remember { mutableStateOf<String?>(null) }

    val context = LocalContext.current
    val configuration = androidx.compose.ui.platform.LocalConfiguration.current
    val maxDialogHeight = (configuration.screenHeightDp * 0.88f).dp

    Dialog(
        onDismissRequest = handleCancelOrClose,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.92f)
                .heightIn(max = maxDialogHeight)
                .padding(vertical = 16.dp),
            shape = RoundedCornerShape(8.dp),
            color = Color.White
        ) {
            Column(modifier = Modifier.fillMaxWidth()) {
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
                        fontFamily = GillSansBold,
                        fontSize = 16.sp
                    )
                    IconButton(
                        onClick = handleCancelOrClose,
                        modifier = Modifier.size(24.dp)
                    ) {
                        Icon(
                            painter = painterResource(id = R.drawable.cancel_white_icon),
                            contentDescription = "Close Dialog",
                            tint = Color.Unspecified,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                }

                // Body content (Inner Scroll)
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f, fill = false)
                        .verticalScroll(rememberScrollState())
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    // Document Name *
                    Text(
                        text = buildAnnotatedString {
                            withStyle(SpanStyle(color = Color(0xFF004D87), fontWeight = FontWeight.Bold, fontFamily = GillSansBold)) {
                                append("Document Name")
                            }
                            withStyle(SpanStyle(color = Color.Red, fontWeight = FontWeight.Bold, fontFamily = GillSansBold)) {
                                append(" *")
                            }
                        },
                        fontSize = 14.sp
                    )
                    OutlinedTextField(
                        value = name,
                        onValueChange = { if (it.length <= 50) name = it },
                        placeholder = { Text("Enter document name", fontSize = 14.sp, fontFamily = GillSans) },
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
                        fontFamily = GillSans,
                        modifier = Modifier.align(Alignment.End)
                    )

                    // Description *
                    Text(
                        text = buildAnnotatedString {
                            withStyle(SpanStyle(color = Color(0xFF004D87), fontWeight = FontWeight.Bold, fontFamily = GillSansBold)) {
                                append("Description")
                            }
                            withStyle(SpanStyle(color = Color.Red, fontWeight = FontWeight.Bold, fontFamily = GillSansBold)) {
                                append(" *")
                            }
                        },
                        fontSize = 14.sp
                    )
                    OutlinedTextField(
                        value = description,
                        onValueChange = { if (it.length <= 300) description = it },
                        placeholder = { Text("Enter description", fontSize = 14.sp, fontFamily = GillSans) },
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
                        fontFamily = GillSans,
                        modifier = Modifier.align(Alignment.End)
                    )

                    // Expiration Date
                    Text(
                        text = buildAnnotatedString {
                            withStyle(SpanStyle(color = Color(0xFF004D87), fontWeight = FontWeight.Bold, fontFamily = GillSansBold)) {
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
                            placeholder = { Text("Expiration Date", fontSize = 14.sp, fontFamily = GillSans) },
                            modifier = Modifier.fillMaxWidth(),
                            enabled = false,
                            trailingIcon = {
                                Icon(
                                    painter = painterResource(id = R.drawable.calendar_icon_xsmall),
                                    contentDescription = "Pick Date",
                                    tint = Color.Black,
                                    modifier = Modifier.size(20.dp)
                                )
                            },
                            colors = OutlinedTextFieldDefaults.colors(
                                disabledBorderColor = Color(0xFFCCCCCC),
                                disabledTextColor = Color.Black,
                                disabledPlaceholderColor = Color.Gray
                            )
                        )
                        // Invisible overlay Box to capture clicks and launch app's custom date picker
                        Box(
                            modifier = Modifier
                                .matchParentSize()
                                .background(Color.Transparent)
                                .clickable {
                                    val dummy = TextView(context).apply { text = expirationDate }
                                    AndroidUtils.showDatePicker(dummy, false, false, true) {
                                        expirationDate = dummy.text.toString()
                                        datePickerError = null
                                    }
                                }
                        )
                    }

                    if (isStaged) {
                        Spacer(modifier = Modifier.height(4.dp))

                        // Enable Download Switch
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Enable Download",
                                color = Color(0xFF004D87),
                                fontFamily = GillSansBold,
                                fontSize = 14.sp
                            )
                            CompactCustomSwitch(
                                checked = enableDownload,
                                onCheckedChange = { enableDownload = it }
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
                                fontFamily = GillSansBold,
                                fontSize = 14.sp
                            )
                            CompactCustomSwitch(
                                checked = enableEncryption,
                                onCheckedChange = { enableEncryption = it }
                            )
                        }

                        Spacer(modifier = Modifier.height(4.dp))

                        // Tags row
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Tags",
                                color = Color(0xFF004D87),
                                fontFamily = GillSansBold,
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
                                contentPadding = PaddingValues(horizontal = 14.dp, vertical = 6.dp),
                                modifier = Modifier.height(34.dp)
                            ) {
                                Text("Add Tag", color = Color.White, fontFamily = GillSansBold, fontSize = 13.sp)
                            }
                        }

                        // Tags list layout showing added tags as grey rounded rectangular cards (Image 1 style)
                        if (tagsMap.isNotEmpty()) {
                            FlowRow(
                                mainAxisSpacing = 8.dp,
                                crossAxisSpacing = 8.dp,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                tagsMap.entries.forEach { entry ->
                                    Row(
                                        modifier = Modifier
                                            .background(Color(0xFFEEEEEE), shape = RoundedCornerShape(6.dp))
                                            .border(BorderStroke(1.dp, Color(0xFFDDDDDE)), shape = RoundedCornerShape(6.dp))
                                            .padding(horizontal = 10.dp, vertical = 8.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        Text(
                                            text = "${entry.key} : ${entry.value}",
                                            color = Color.Black,
                                            fontFamily = GillSans,
                                            fontSize = 13.sp
                                        )
                                        Icon(
                                            painter = painterResource(id = R.drawable.edit__icon),
                                            contentDescription = "Edit Tag",
                                            tint = Color.Unspecified,
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
                }

                // Fixed Footer (Action Buttons)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color.White)
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Button(
                        onClick = handleCancelOrClose,
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFEEEEEE)),
                        shape = RoundedCornerShape(6.dp),
                        modifier = Modifier
                            .weight(1f)
                            .height(40.dp)
                    ) {
                        Text("Cancel", color = Color.Black, fontFamily = GillSansBold, fontSize = 14.sp)
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
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF004D87),
                            disabledContainerColor = Color(0xFFD0D0D0)
                        ),
                        shape = RoundedCornerShape(6.dp),
                        modifier = Modifier
                            .weight(1f)
                            .height(40.dp),
                        enabled = name.isNotEmpty() && description.isNotEmpty()
                    ) {
                        Text("Save", color = Color.White, fontFamily = GillSansBold, fontSize = 14.sp)
                    }
                }
            }
        }
    }

    // Add/Edit Tag overlay sub-dialog (Images 2 & 3 style)
    if (showAddTagDialog) {
        val draftTags = remember(showAddTagDialog) {
            mutableStateListOf<Pair<String, String>>().apply {
                addAll(tagsMap.entries.map { it.key to it.value })
            }
        }
        var keyText by remember(showAddTagDialog) { mutableStateOf(tagToEditKey ?: "") }
        var valText by remember(showAddTagDialog) { mutableStateOf(tagToEditVal ?: "") }
        var editingIndex by remember(showAddTagDialog) {
            mutableStateOf(if (tagToEditKey != null) draftTags.indexOfFirst { it.first == tagToEditKey }.takeIf { it != -1 } else null)
        }

        Dialog(
            onDismissRequest = {
                showAddTagDialog = false
                tagToEditKey = null
                tagToEditVal = null
            },
            properties = DialogProperties(usePlatformDefaultWidth = false)
        ) {
            Surface(
                modifier = Modifier
                    .fillMaxWidth(0.92f)
                    .wrapContentHeight()
                    .padding(vertical = 24.dp),
                shape = RoundedCornerShape(8.dp),
                color = Color.White
            ) {
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
                            text = "Add Tag",
                            color = Color.White,
                            fontFamily = GillSansBold,
                            fontSize = 16.sp
                        )
                        IconButton(
                            onClick = {
                                showAddTagDialog = false
                                tagToEditKey = null
                                tagToEditVal = null
                            },
                            modifier = Modifier.size(24.dp)
                        ) {
                            Icon(
                                painter = painterResource(id = R.drawable.cancel_white_icon),
                                contentDescription = "Close",
                                tint = Color.Unspecified,
                                modifier = Modifier.size(22.dp)
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
                                withStyle(SpanStyle(color = Color(0xFF004D87), fontWeight = FontWeight.Bold, fontFamily = GillSansBold)) {
                                    append("Tag Type")
                                }
                                withStyle(SpanStyle(color = Color.Red, fontWeight = FontWeight.Bold, fontFamily = GillSansBold)) {
                                    append(" *")
                                }
                            },
                            fontSize = 14.sp
                        )
                        OutlinedTextField(
                            value = keyText,
                            onValueChange = { if (it.length <= 30) keyText = it },
                            placeholder = { Text("Tag Type", fontSize = 14.sp, fontFamily = GillSans) },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = Color(0xFF004D87),
                                unfocusedBorderColor = Color(0xFFCCCCCC)
                            )
                        )
                        Text(
                            text = "${keyText.length}/30",
                            color = Color.Gray,
                            fontSize = 11.sp,
                            fontFamily = GillSans,
                            modifier = Modifier.align(Alignment.End)
                        )

                        // Tag *
                        Text(
                            text = buildAnnotatedString {
                                withStyle(SpanStyle(color = Color(0xFF004D87), fontWeight = FontWeight.Bold, fontFamily = GillSansBold)) {
                                    append("Tag")
                                }
                                withStyle(SpanStyle(color = Color.Red, fontWeight = FontWeight.Bold, fontFamily = GillSansBold)) {
                                    append(" *")
                                }
                            },
                            fontSize = 14.sp
                        )
                        OutlinedTextField(
                            value = valText,
                            onValueChange = { if (it.length <= 100) valText = it },
                            placeholder = { Text("Tag", fontSize = 14.sp, fontFamily = GillSans) },
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
                            fontFamily = GillSans,
                            modifier = Modifier.align(Alignment.End)
                        )

                        // Add / Update Button
                        Box(
                            modifier = Modifier.fillMaxWidth(),
                            contentAlignment = Alignment.CenterEnd
                        ) {
                            Button(
                                onClick = {
                                    val trimmedKey = keyText.trim()
                                    val trimmedVal = valText.trim()
                                    if (trimmedKey.isNotEmpty() && trimmedVal.isNotEmpty()) {
                                        val idx = editingIndex
                                        if (idx != null && idx in draftTags.indices) {
                                            draftTags[idx] = trimmedKey to trimmedVal
                                        } else {
                                            val existingIdx = draftTags.indexOfFirst { it.first == trimmedKey }
                                            if (existingIdx != -1) {
                                                draftTags[existingIdx] = trimmedKey to trimmedVal
                                            } else {
                                                draftTags.add(trimmedKey to trimmedVal)
                                            }
                                        }
                                        keyText = ""
                                        valText = ""
                                        editingIndex = null
                                    }
                                },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = Color(0xFF004D87),
                                    disabledContainerColor = Color(0xFFD0D0D0)
                                ),
                                shape = RoundedCornerShape(4.dp),
                                modifier = Modifier
                                    .width(90.dp)
                                    .height(34.dp),
                                enabled = keyText.trim().isNotEmpty() && valText.trim().isNotEmpty()
                            ) {
                                Text(
                                    text = if (editingIndex == null) "Add" else "Update",
                                    color = Color.White,
                                    fontFamily = GillSansBold,
                                    fontSize = 13.sp
                                )
                            }
                        }

                        // Added Tags Section (Image 2 style)
                        if (draftTags.isNotEmpty()) {
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Added Tags",
                                color = Color(0xFF004D87),
                                fontFamily = GillSansBold,
                                fontSize = 14.sp
                            )

                            Column(
                                modifier = Modifier.fillMaxWidth(),
                                verticalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                draftTags.forEachIndexed { index, pair ->
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .background(Color(0xFFEEEEEE), RoundedCornerShape(6.dp))
                                            .border(BorderStroke(1.dp, Color(0xFFDDDDDE)), RoundedCornerShape(6.dp))
                                            .padding(horizontal = 12.dp, vertical = 10.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Text(
                                            text = "${pair.first} - ${pair.second}",
                                            color = Color.Black,
                                            fontFamily = GillSans,
                                            fontSize = 13.sp,
                                            modifier = Modifier.weight(1f)
                                        )
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                                        ) {
                                            Icon(
                                                painter = painterResource(id = R.drawable.edit__icon),
                                                contentDescription = "Edit Tag",
                                                tint = Color.Unspecified,
                                                modifier = Modifier
                                                    .size(18.dp)
                                                    .clickable {
                                                        keyText = pair.first
                                                        valText = pair.second
                                                        editingIndex = index
                                                    }
                                            )
                                            Icon(
                                                painter = painterResource(id = R.drawable.cancel_red_icon),
                                                contentDescription = "Remove Tag",
                                                tint = Color.Unspecified,
                                                modifier = Modifier
                                                    .size(18.dp)
                                                    .clickable {
                                                        draftTags.removeAt(index)
                                                        if (editingIndex == index) {
                                                            editingIndex = null
                                                            keyText = ""
                                                            valText = ""
                                                        }
                                                    }
                                            )
                                        }
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        // Dialog Action buttons: Cancel & Save
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Button(
                                onClick = {
                                    showAddTagDialog = false
                                    tagToEditKey = null
                                    tagToEditVal = null
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFEEEEEE)),
                                shape = RoundedCornerShape(6.dp),
                                modifier = Modifier
                                    .weight(1f)
                                    .height(40.dp)
                            ) {
                                Text("Cancel", color = Color.Black, fontFamily = GillSansBold, fontSize = 14.sp)
                            }
                            Button(
                                onClick = {
                                    val trimmedKey = keyText.trim()
                                    val trimmedVal = valText.trim()
                                    if (trimmedKey.isNotEmpty() && trimmedVal.isNotEmpty()) {
                                        val idx = editingIndex
                                        if (idx != null && idx in draftTags.indices) {
                                            draftTags[idx] = trimmedKey to trimmedVal
                                        } else {
                                            val existingIdx = draftTags.indexOfFirst { it.first == trimmedKey }
                                            if (existingIdx != -1) {
                                                draftTags[existingIdx] = trimmedKey to trimmedVal
                                            } else {
                                                draftTags.add(trimmedKey to trimmedVal)
                                            }
                                        }
                                    }
                                    // Commit draftTags to tagsMap
                                    tagsMap.clear()
                                    draftTags.forEach { (k, v) -> tagsMap[k] = v }
                                    showAddTagDialog = false
                                    tagToEditKey = null
                                    tagToEditVal = null
                                },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = Color(0xFF004D87),
                                    disabledContainerColor = Color(0xFFD0D0D0)
                                ),
                                shape = RoundedCornerShape(6.dp),
                                modifier = Modifier
                                    .weight(1f)
                                    .height(40.dp)
                            ) {
                                Text("Save", color = Color.White, fontFamily = GillSansBold, fontSize = 14.sp)
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

    if (showDiscardAlert) {
        AppConfirmationDialog(
            title = "Alert!",
            message = "Changes you made will not be saved. Do you want to continue?",
            confirmText = "Yes",
            dismissText = "No",
            onConfirm = {
                // "Yes" -> Continue on editing
                showDiscardAlert = false
            },
            onDismiss = {
                // "No" -> Discard changes and close
                showDiscardAlert = false
                onDismiss()
            },
            onClose = {
                // Close 'X' icon -> Stay on editing
                showDiscardAlert = false
            }
        )
    }
}

@Composable
fun CompactCustomSwitch(
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    // Java/XML SwitchMaterial track colors: dullBlueColor (#85B2E0) when checked, grey_color_dark (#A0A0A0) when unchecked
    val trackColor = if (checked) Color(0xFF85B2E0) else Color(0xFFA0A0A0)
    val thumbOffset by animateDpAsState(
        targetValue = if (checked) 20.dp else 0.dp,
        animationSpec = androidx.compose.animation.core.tween(durationMillis = 150),
        label = "thumbOffset"
    )
    Box(
        modifier = modifier
            .size(width = 42.dp, height = 28.dp)
            .clickable(
                interactionSource = remember { androidx.compose.foundation.interaction.MutableInteractionSource() },
                indication = null
            ) { onCheckedChange(!checked) },
        contentAlignment = Alignment.CenterStart
    ) {
        // Track: 36x14dp pill vertically centered
        Box(
            modifier = Modifier
                .align(Alignment.Center)
                .size(width = 36.dp, height = 14.dp)
                .background(trackColor, RoundedCornerShape(7.dp))
        )
        // Thumb: 22dp diameter circle extending 4dp above and below the 14dp track, with subtle elevation shadow
        Box(
            modifier = Modifier
                .offset(x = thumbOffset)
                .size(22.dp)
                .shadow(elevation = 2.dp, shape = CircleShape)
                .background(Color.White, CircleShape)
                .border(0.5.dp, Color(0x22000000), CircleShape)
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
