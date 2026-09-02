package com.digicoffer.lauditor.feature.relationships.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.compose.foundation.Image
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import com.digicoffer.lauditor.R
import com.digicoffer.lauditor.Relationships.Model.RelationshipsModel
import com.digicoffer.lauditor.Relationships.Model.SharedDocumentsDo
import com.digicoffer.lauditor.core.ui.common.search.AppSearchField
import org.json.JSONArray
import org.json.JSONObject

private val GillSans = FontFamily(
    Font(R.font.gill_sans)
)

@Composable
fun ShareDocsDialog(
    model: RelationshipsModel,
    isCorporate: Boolean,
    initialCategory: String,
    onDismiss: () -> Unit,
    onSearchDocs: (JSONObject, (List<SharedDocumentsDo>) -> Unit) -> Unit,
    onShareDocs: (JSONObject) -> Unit,
    onViewDoc: (SharedDocumentsDo) -> Unit
) {
    var searchQuery by remember { mutableStateOf("") }
    var selectedSubTab by remember { mutableStateOf(initialCategory) } // "client" or "firm"
    var docsList by remember { mutableStateOf<List<SharedDocumentsDo>>(emptyList()) }
    var isLoading by remember { mutableStateOf(false) }

    val selectedDocIds = remember { mutableStateListOf<String>() }

    // Fetch initial documents matching relationship context
    LaunchedEffect(selectedSubTab) {
        isLoading = true
        val payload = if (selectedSubTab == "client") {
            JSONObject().apply {
                put("category", "client")
                put("clients", model.client_id ?: "")
                put("matters", "all")
                put("exclude_already_shared", true)
                put("relationship_id", model.id ?: "")
            }
        } else {
            JSONObject().apply {
                put("category", "firm")
                put("groups", model.groups ?: JSONArray())
            }
        }
        onSearchDocs(payload) { results ->
            docsList = results
            isLoading = false
        }
    }

    val filteredDocsList = remember(docsList, searchQuery) {
        if (searchQuery.isEmpty()) {
            docsList
        } else {
            docsList.filter { it.name?.lowercase()?.contains(searchQuery.lowercase()) == true }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFE4F2FF))
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(10.dp)
        ) {
            // Header
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 5.dp, top = 5.dp, end = 5.dp, bottom = 10.dp)
            ) {
                Image(
                    painter = painterResource(id = R.drawable.back_arrow),
                    contentDescription = "Back",
                    modifier = Modifier
                        .size(20.dp)
                        .clickable { onDismiss() },
                    colorFilter = androidx.compose.ui.graphics.ColorFilter.tint(Color(0xFF004D87))
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Share Documents",
                    color = Color(0xFF004D87),
                    fontWeight = FontWeight.Bold,
                    fontFamily = GillSans,
                    fontSize = 18.sp
                )
            }

            // Card container wrapping all page content below the header
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                shape = RoundedCornerShape(10.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(15.dp)
                ) {
                    // Sub-tabs (Client Documents / Firm Documents)
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(40.dp)
                    ) {
                        // Left Pill: Client Documents
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxHeight()
                                .clip(RoundedCornerShape(topStart = 20.dp, bottomStart = 20.dp))
                                .background(if (selectedSubTab == "client") Color(0xFF004D87) else Color(0xFFEEEEEE))
                                .clickable { selectedSubTab = "client"; selectedDocIds.clear() },
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "Client Documents",
                                color = if (selectedSubTab == "client") Color.White else Color.Black,
                                fontWeight = FontWeight.Bold,
                                fontFamily = GillSans,
                                fontSize = 15.sp
                            )
                        }

                        Spacer(modifier = Modifier.width(1.dp))

                        // Right Pill: Firm Documents
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxHeight()
                                .clip(RoundedCornerShape(topEnd = 20.dp, bottomEnd = 20.dp))
                                .background(if (selectedSubTab == "firm") Color(0xFF004D87) else Color(0xFFEEEEEE))
                                .clickable { selectedSubTab = "firm"; selectedDocIds.clear() },
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "Firm Documents",
                                color = if (selectedSubTab == "firm") Color.White else Color.Black,
                                fontWeight = FontWeight.Bold,
                                fontFamily = GillSans,
                                fontSize = 15.sp
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    if (docsList.isNotEmpty() && !isLoading) {
                        // Search Bar
                        AppSearchField(
                            value = searchQuery,
                            onValueChange = { searchQuery = it },
                            placeholder = "Search Documents"
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        // Select All row (Right aligned checkbox)
                        val allSelected = filteredDocsList.isNotEmpty() && filteredDocsList.all { selectedDocIds.contains(it.id) }
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(start = 12.dp),
                            horizontalArrangement = Arrangement.Start,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Checkbox(
                                checked = allSelected,
                                onCheckedChange = { checked ->
                                    if (checked) {
                                        filteredDocsList.forEach { doc ->
                                            if (!selectedDocIds.contains(doc.id)) {
                                                selectedDocIds.add(doc.id ?: "")
                                            }
                                        }
                                    } else {
                                        selectedDocIds.clear()
                                    }
                                },
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Select All",
                                color = Color.Black,
                                fontFamily = GillSans,
                                fontSize = 15.sp
                            )
                        }
                    }

                    Divider(color = Color(0xFFDDDDDE), thickness = 0.5.dp, modifier = Modifier.padding(vertical = 5.dp))

                    // Document List or states
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f)
                    ) {
                        if (filteredDocsList.isEmpty() && !isLoading) {
                            Text(
                                text = "No documents to show",
                                color = Color.Gray,
                                fontFamily = GillSans,
                                fontSize = 15.sp,
                                modifier = Modifier.align(Alignment.Center)
                            )
                        } else if (filteredDocsList.isNotEmpty()) {
                            LazyColumn(
                                modifier = Modifier.fillMaxSize()
                            ) {
                                items(filteredDocsList) { doc ->
                                    DocumentRow(
                                        doc = doc,
                                        sharedTag = "share",
                                        onViewDoc = { onViewDoc(doc) },
                                        isSelected = selectedDocIds.contains(doc.id),
                                        onCheckedChange = { checked ->
                                            if (checked) {
                                                if (!selectedDocIds.contains(doc.id)) {
                                                    selectedDocIds.add(doc.id ?: "")
                                                }
                                            } else {
                                                selectedDocIds.remove(doc.id)
                                            }
                                        }
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Bottom Buttons (always visible)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceAround
                    ) {
                        OutlinedButton(
                            onClick = onDismiss,
                            colors = ButtonDefaults.outlinedButtonColors(
                                containerColor = Color(0xFFEEEEEE),
                                contentColor = Color.Black
                            ),
                            border = BorderStroke(1.dp, Color(0xFFDDDDDE)),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.width(120.dp)
                        ) {
                            Text(text = "Cancel", color = Color.Black, fontFamily = GillSans, fontWeight = FontWeight.Bold)
                        }

                        Button(
                            onClick = {
                                val addArray = JSONArray()
                                for (id in selectedDocIds) {
                                    val doc = docsList.find { it.id == id }
                                    addArray.put(JSONObject().apply {
                                        put("docid", id)
                                        put("doctype", "general")
                                        val matters = JSONArray()
                                        if (doc?.has_Confidential == true) {
                                            matters.put(doc.matter_details_id)
                                        }
                                        put("matters", matters)
                                    })
                                }
                                val payload = JSONObject().apply {
                                    if (isCorporate) {
                                        put("relid", model.id)
                                    }
                                    put("add", addArray)
                                    put("remove", JSONArray())
                                    put("message", "")
                                }
                                onShareDocs(payload)
                            },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(0xFF004D87),
                                disabledContainerColor = Color(0xFF004D87).copy(alpha = 0.5f)
                            ),
                            enabled = selectedDocIds.isNotEmpty(),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.width(120.dp)
                        ) {
                            val buttonText = if (selectedDocIds.isNotEmpty()) "Share (${selectedDocIds.size})" else "Share"
                            Text(text = buttonText, color = Color.White, fontFamily = GillSans, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun DocumentRow(
    doc: SharedDocumentsDo,
    sharedTag: String, // "withme", "byme", or "share"
    onViewDoc: (SharedDocumentsDo) -> Unit,
    isSelected: Boolean = false,
    onCheckedChange: ((Boolean) -> Unit)? = null,
    onRemoveClick: (() -> Unit)? = null
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(4.dp)
            .background(Color.White)
            .clickable { onViewDoc(doc) }
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Document info column
            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(4.dp)
            ) {
                // Name
                Text(
                    text = doc.name ?: "",
                    color = if (sharedTag == "withme") Color(0xFF004D87) else Color.Black,
                    fontWeight = FontWeight.Normal,
                    fontFamily = GillSans,
                    fontSize = if (sharedTag == "withme") 18.sp else 17.sp
                )

                // Confidential Label
                if (doc.has_Confidential) {
                    Row(
                        modifier = Modifier.padding(top = 2.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Confidential : ",
                            color = Color.Black,
                            fontWeight = FontWeight.Bold,
                            fontFamily = GillSans,
                            fontSize = 15.sp
                        )
                        Text(
                            text = doc.matter_details_name ?: "",
                            color = Color.Black,
                            fontWeight = FontWeight.Normal,
                            fontFamily = GillSans,
                            fontSize = 15.sp
                        )
                    }
                }

                // Date and description for shared with me
                if (sharedTag == "withme") {
                    // Date
                    Text(
                        text = doc.created ?: "",
                        color = Color(0xFF1976D2),
                        fontWeight = FontWeight.Medium,
                        fontFamily = GillSans,
                        fontSize = 17.sp,
                        modifier = Modifier.padding(top = 2.dp)
                    )
                    // Description
                    Text(
                        text = doc.description ?: doc.filename ?: "",
                        color = Color.Black,
                        fontWeight = FontWeight.Normal,
                        fontFamily = GillSans,
                        fontSize = 17.sp,
                        modifier = Modifier.padding(top = 2.dp)
                    )
                }
            }

            // Right side buttons/checkboxes
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.End
            ) {
                if (onRemoveClick != null) {
                    IconButton(
                        onClick = onRemoveClick,
                        modifier = Modifier.size(30.dp)
                    ) {
                        Icon(
                            painter = painterResource(id = R.drawable.cancel_red_icon),
                            contentDescription = "Remove",
                            tint = Color.Unspecified,
                            modifier = Modifier.size(30.dp)
                        )
                    }
                }

                if (onCheckedChange != null) {
                    Checkbox(
                        checked = isSelected,
                        onCheckedChange = onCheckedChange,
                        modifier = Modifier
                            .padding(horizontal = 10.dp)
                            .size(25.dp)
                    )
                }
            }
        }
        Divider(
            color = Color(0xFFA0A0A0),
            thickness = 0.5.dp,
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(10.dp))
    }
}
