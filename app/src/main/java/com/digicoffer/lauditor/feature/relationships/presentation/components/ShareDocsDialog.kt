package com.digicoffer.lauditor.feature.relationships.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
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
import com.digicoffer.lauditor.feature.notifications.presentation.components.NotificationsSearchBar
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
    LaunchedEffect(selectedSubTab, searchQuery) {
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
            docsList = results.filter { doc ->
                if (searchQuery.isNotEmpty()) {
                    doc.name?.lowercase()?.contains(searchQuery.lowercase()) == true
                } else {
                    true
                }
            }
            isLoading = false
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
                    .padding(16.dp)
            ) {
                // Header
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 15.dp, top = 15.dp, end = 15.dp, bottom = 10.dp)
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

                // Search Bar
                NotificationsSearchBar(
                    query = searchQuery,
                    onQueryChange = { searchQuery = it },
                    placeholder = "Search Documents"
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Sub-tabs (Client Documents / Firm Documents) matching legacy style
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

                Spacer(modifier = Modifier.height(12.dp))

                // Documents List Card
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .border(width = 0.5.dp, color = Color(0xFFDDDDDE), shape = RoundedCornerShape(10.dp)),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp)
                    ) {
                        if (isLoading) {
                            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                                CircularProgressIndicator(color = Color(0xFF004D87))
                            }
                        } else if (docsList.isEmpty()) {
                            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                                Text(text = "No documents found", color = Color.Gray, fontSize = 14.sp)
                            }
                        } else {
                            LazyColumn(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .weight(1f)
                            ) {
                                items(docsList) { doc ->
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(vertical = 8.dp)
                                            .border(
                                                width = 0.5.dp,
                                                color = Color(0xFFDDDDDE),
                                                shape = RoundedCornerShape(8.dp)
                                            )
                                            .clickable {
                                                onViewDoc(doc)
                                            }
                                            .padding(12.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Column(modifier = Modifier.weight(1f)) {
                                            Text(
                                                text = doc.name ?: "",
                                                color = Color.Black,
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 14.sp
                                            )
                                        }

                                        Checkbox(
                                            checked = selectedDocIds.contains(doc.id),
                                            onCheckedChange = { checked ->
                                                if (checked) {
                                                    if (!selectedDocIds.contains(doc.id)) {
                                                        selectedDocIds.add(doc.id ?: "")
                                                    }
                                                } else {
                                                    selectedDocIds.remove(doc.id)
                                                }
                                            },
                                            modifier = Modifier.size(24.dp)
                                        )
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(16.dp))

                            // Bottom Buttons
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceAround
                            ) {
                                Button(
                                    onClick = onDismiss,
                                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFE0E0E0)),
                                    shape = RoundedCornerShape(20.dp),
                                    modifier = Modifier.width(120.dp)
                                ) {
                                    Text(text = "Cancel", color = Color.Black)
                                }

                                Button(
                                    onClick = {
                                        val addArray = JSONArray()
                                        for (id in selectedDocIds) {
                                            addArray.put(JSONObject().apply {
                                                put("docid", id)
                                                put("doctype", "general")
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
                                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF004D87)),
                                    enabled = selectedDocIds.isNotEmpty(),
                                    shape = RoundedCornerShape(20.dp),
                                    modifier = Modifier.width(120.dp)
                                ) {
                                    Text(text = "Share", color = Color.White)
                                }
                            }
                        }
                    }
                }
            }
    }
}
