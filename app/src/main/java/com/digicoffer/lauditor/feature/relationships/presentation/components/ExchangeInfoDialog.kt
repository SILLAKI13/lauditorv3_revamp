package com.digicoffer.lauditor.feature.relationships.presentation.components

import androidx.compose.foundation.Image
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.digicoffer.lauditor.R
import com.digicoffer.lauditor.Relationships.Model.RelationshipsModel
import com.digicoffer.lauditor.Relationships.Model.SharedDocumentsDo
import com.digicoffer.lauditor.feature.notifications.presentation.components.NotificationsSearchBar
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import org.json.JSONArray
import org.json.JSONObject
import androidx.compose.foundation.BorderStroke


private val GillSans = FontFamily(
    Font(R.font.gill_sans)
)

@Composable
fun ExchangeInfoDialog(
    model: RelationshipsModel,
    sharedDocs: List<SharedDocumentsDo>,
    isLoading: Boolean,
    isCorporate: Boolean,
    onDismiss: () -> Unit,
    onLoadProfile: ((JSONObject?) -> Unit) -> Unit,
    onLoadDocs: (String) -> Unit, // "withme" or "byme"
    onUnshareDocs: (JSONObject) -> Unit,
    onShareClick: (String) -> Unit,
    onSearchDocs: (JSONObject, (List<SharedDocumentsDo>) -> Unit) -> Unit,
    onShareDocs: (JSONObject) -> Unit,
    onViewDoc: (SharedDocumentsDo, String) -> Unit
) {
    var hideDetails by remember { mutableStateOf(true) } // collapsed by default
    var selectedSharedTab by remember { mutableStateOf("withme") } // "withme" or "byme"
    var selectedSubTab by remember { mutableStateOf<String?>(null) } // "client" or "firm"
    var showDocPickerPopup by remember { mutableStateOf(false) }
    var pickerCategory by remember { mutableStateOf("client") }
    var validationAlertMessage by remember { mutableStateOf("") }

    // Store selected doc ids for unsharing
    val selectedDocIds = remember { mutableStateListOf<String>() }

    // Dynamic Profile State
    var profileData by remember { mutableStateOf<JSONObject?>(null) }
    var isProfileLoading by remember { mutableStateOf(false) }

    LaunchedEffect(model.id) {
        isProfileLoading = true
        onLoadProfile { result ->
            isProfileLoading = false
            profileData = result
        }
    }

    // Load initial documents
    LaunchedEffect(selectedSharedTab) {
        onLoadDocs(selectedSharedTab)
        selectedDocIds.clear()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFE4F2FF)) // Pale Blue background matching R.color.blue_pale
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(10.dp)
        ) {
            // Header Row
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
                    text = "Exchange Information",
                    color = Color(0xFF004D87),
                    fontWeight = FontWeight.Bold,
                    fontFamily = GillSans,
                    fontSize = 18.sp
                )
            }

            // Scrollable container for screen content
            LazyColumn(
                modifier = Modifier.fillMaxWidth().weight(1f),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // 1. Profile Details Card
                item {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 2.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        shape = RoundedCornerShape(10.dp),
                        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(15.dp)
                        ) {
                            // Profile basic row (Avatar + Name & Email + View/Hide Details toggle)
                            val isIndividual = model.clientType?.lowercase() == "consumer" || model.clientType?.lowercase() == "individual"
                            val fullName = if (isIndividual) {
                                val fName = profileData?.optString("first_name") ?: ""
                                val lName = profileData?.optString("last_name") ?: ""
                                if (fName.isNotEmpty() || lName.isNotEmpty()) "$fName $lName" else (model.name ?: "")
                            } else {
                                profileData?.optString("fullname") ?: (model.name ?: "")
                            }
                            val email = profileData?.optString("email") ?: ""

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                                    // Avatar image circle/rect spec
                                    Box(
                                        modifier = Modifier
                                            .size(48.dp)
                                            .clip(RoundedCornerShape(8.dp))
                                            .background(Color(0xFF004D87)),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = fullName.take(1).uppercase(),
                                            color = Color.White,
                                            fontWeight = FontWeight.Bold,
                                            fontFamily = GillSans,
                                            fontSize = 20.sp
                                        )
                                    }
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Column {
                                        Text(
                                            text = fullName,
                                            color = Color.Black,
                                            fontFamily = GillSans,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 15.sp
                                        )
                                        Text(
                                            text = email,
                                            color = Color.Gray,
                                            fontFamily = GillSans,
                                            fontSize = 15.sp
                                        )
                                    }
                                }

                                Row(
                                    modifier = Modifier.clickable { hideDetails = !hideDetails },
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = if (hideDetails) "View details" else "Hide details",
                                        color = Color(0xFF004D87),
                                        fontFamily = GillSans,
                                        fontSize = 12.sp
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Icon(
                                        painter = painterResource(id = R.drawable.drop_down_blue),
                                        contentDescription = "Toggle Details",
                                        modifier = Modifier
                                            .size(15.dp)
                                            .rotate(if (hideDetails) 0f else 180f),
                                        tint = Color(0xFF004D87)
                                    )
                                }
                            }

                            if (!hideDetails) {
                                Spacer(modifier = Modifier.height(12.dp))
                                Divider(
                                    modifier = Modifier.padding(bottom = 10.dp),
                                    color = Color(0xFFDDDDDE)
                                )

                                if (isProfileLoading) {
                                    // Do not render inline progress indicator as global AppLoader covers it
                                } else {
                                    if (isIndividual) {
                                        // Individual detail fields (no icons)
                                        val mobileRaw = profileData?.optString("mobile") ?: ""
                                        val mobile = if (mobileRaw == "null") "" else mobileRaw
                                        var country = ""
                                        var homeAddress = ""
                                        var workPhone = ""
                                        var altPhone = ""
                                        val citizenArray = profileData?.optJSONArray("citizen")
                                        if (citizenArray != null) {
                                            for (i in 0 until citizenArray.length()) {
                                                val c = citizenArray.optJSONObject(i)
                                                if (c != null && c.optString("index") == "citizen_primary") {
                                                    country = c.optString("country")
                                                    homeAddress = c.optString("home_address")
                                                    workPhone = c.optString("work_phone")
                                                    altPhone = c.optString("alt_phone")
                                                    break
                                                }
                                            }
                                        }

                                        // Render all individual fields (always visible)
                                        DetailFieldIndividual(label = "Mobile", value = mobile)
                                        DetailFieldIndividual(label = "Country", value = country)
                                        DetailFieldIndividual(label = "Home Address", value = homeAddress)
                                        DetailFieldIndividual(label = "Work Phone", value = workPhone)
                                        DetailFieldIndividual(label = "Alt Phone", value = altPhone)
                                    } else {
                                        // Entity/Corporate detail fields (with icons)
                                        val contactPerson = profileData?.optString("contact_person") ?: ""
                                        val country = profileData?.optJSONObject("address")?.optString("country") ?: ""
                                        val contactPhoneRaw = profileData?.optString("contact_phone") ?: ""
                                        val contactPhone = if (contactPhoneRaw == "null") "" else contactPhoneRaw
                                        val website = profileData?.optString("website") ?: ""

                                        val addrObj = profileData?.optJSONObject("address")
                                        val houseFlat = addrObj?.optString("house_flat_no") ?: ""
                                        val street = addrObj?.optString("street") ?: ""
                                        val cityTown = addrObj?.optString("city_town") ?: ""
                                        val state = addrObj?.optString("state") ?: ""
                                        val zipcode = addrObj?.optString("zipcode") ?: ""
                                        
                                        val addrParts = mutableListOf<String>()
                                        if (houseFlat.isNotEmpty() && houseFlat != "null") addrParts.add(houseFlat)
                                        if (street.isNotEmpty() && street != "null") addrParts.add(street)
                                        if (cityTown.isNotEmpty() && cityTown != "null") addrParts.add(cityTown)
                                        if (state.isNotEmpty() && state != "null") addrParts.add(state)
                                        if (country.isNotEmpty() && country != "null") addrParts.add(country)
                                        
                                        var addressVal = addrParts.joinToString(", ")
                                        if (zipcode.isNotEmpty() && zipcode != "null") {
                                            addressVal += " $zipcode"
                                        }

                                        // Render all entity fields (always visible)
                                        DetailFieldEntity(
                                            label = "Contact Person",
                                            value = contactPerson,
                                            iconResId = R.drawable.person_icon
                                        )
                                        DetailFieldEntity(
                                            label = "Country",
                                            value = country,
                                            iconResId = R.drawable.location_icon
                                        )
                                        DetailFieldEntity(
                                            label = "Phone",
                                            value = contactPhone,
                                            iconResId = R.drawable.phone_icon
                                        )
                                        DetailFieldEntity(
                                            label = "Address",
                                            value = addressVal,
                                            iconResId = R.drawable.location_icon
                                        )
                                        DetailFieldEntity(
                                            label = "Website",
                                            value = website,
                                            iconResId = R.drawable.website
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                // 2. Shared Folder Card (visible only if isAccepted is true)
                if (model.isAccepted) {
                    item {
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 2.dp),
                            colors = CardDefaults.cardColors(containerColor = Color.White),
                            shape = RoundedCornerShape(10.dp),
                            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(15.dp)
                            ) {
                                 // Title row & circular share button
                                 Row(
                                     modifier = Modifier.fillMaxWidth(),
                                     horizontalArrangement = Arrangement.SpaceBetween,
                                     verticalAlignment = Alignment.CenterVertically
                                 ) {
                                     Text(
                                         text = "SHARED FOLDER",
                                         fontWeight = FontWeight.Bold,
                                         fontFamily = GillSans,
                                         color = Color.Gray,
                                         fontSize = 15.sp
                                     )
                                     Image(
                                         painter = painterResource(id = R.drawable.share_btn),
                                         contentDescription = "Share Docs",
                                         modifier = Modifier
                                             .size(40.dp)
                                             .clickable { onShareClick("client") }
                                     )
                                 }

                                 Spacer(modifier = Modifier.height(10.dp))
                                 Divider(
                                     modifier = Modifier.padding(bottom = 10.dp),
                                     color = Color(0xFFDDDDDE)
                                 )

                                 // Tab Pills (Shared With Me / Shared By Me) matching legacy XML
                                 Row(
                                     modifier = Modifier
                                         .fillMaxWidth()
                                         .height(40.dp)
                                 ) {
                                     // Left Pill: Shared With Me
                                     Box(
                                         modifier = Modifier
                                             .weight(1f)
                                             .fillMaxHeight()
                                             .clip(RoundedCornerShape(topStart = 20.dp, bottomStart = 20.dp))
                                             .background(if (selectedSharedTab == "withme") Color(0xFF004D87) else Color(0xFFEEEEEE))
                                             .clickable { 
                                                 selectedSharedTab = "withme"
                                                 selectedSubTab = null
                                             },
                                         contentAlignment = Alignment.Center
                                     ) {
                                         Text(
                                             text = "Shared With Me",
                                             color = if (selectedSharedTab == "withme") Color.White else Color.Black,
                                             fontWeight = FontWeight.Bold,
                                             fontFamily = GillSans,
                                             fontSize = 15.sp
                                         )
                                     }

                                     Spacer(modifier = Modifier.width(1.dp))

                                     // Right Pill: Shared By Me
                                     Box(
                                         modifier = Modifier
                                             .weight(1f)
                                             .fillMaxHeight()
                                             .clip(RoundedCornerShape(topEnd = 20.dp, bottomEnd = 20.dp))
                                             .background(if (selectedSharedTab == "byme") Color(0xFF004D87) else Color(0xFFEEEEEE))
                                             .clickable { 
                                                 selectedSharedTab = "byme"
                                                 selectedSubTab = null
                                             },
                                         contentAlignment = Alignment.Center
                                     ) {
                                         Text(
                                             text = "Shared By Me",
                                             color = if (selectedSharedTab == "byme") Color.White else Color.Black,
                                             fontWeight = FontWeight.Bold,
                                             fontFamily = GillSans,
                                             fontSize = 15.sp
                                         )
                                     }
                                 }

                                   if (selectedSharedTab == "byme") {
                                       Spacer(modifier = Modifier.height(12.dp))
                                       Row(
                                           modifier = Modifier
                                               .fillMaxWidth()
                                               .height(34.dp)
                                       ) {
                                           // Left Pill: Client Documents
                                           Box(
                                               modifier = Modifier
                                                   .weight(1f)
                                                   .fillMaxHeight()
                                                   .clip(RoundedCornerShape(topStart = 10.dp, bottomStart = 10.dp))
                                                   .background(Color(0xFFEEEEEE))
                                                   .clickable {
                                                       onShareClick("client")
                                                   },
                                               contentAlignment = Alignment.Center
                                           ) {
                                               Text(
                                                   text = "Client Documents",
                                                   color = Color.Black,
                                                   fontWeight = FontWeight.Bold,
                                                   fontFamily = GillSans,
                                                   fontSize = 12.sp
                                               )
                                           }

                                           Spacer(modifier = Modifier.width(3.dp))

                                           // Right Pill: Firm Documents
                                           Box(
                                               modifier = Modifier
                                                   .weight(1f)
                                                   .fillMaxHeight()
                                                   .clip(RoundedCornerShape(topEnd = 10.dp, bottomEnd = 10.dp))
                                                   .background(Color(0xFFEEEEEE))
                                                   .clickable {
                                                       onShareClick("firm")
                                                   },
                                               contentAlignment = Alignment.Center
                                           ) {
                                               Text(
                                                   text = "Firm Documents",
                                                   color = Color.Black,
                                                   fontWeight = FontWeight.Bold,
                                                   fontFamily = GillSans,
                                                   fontSize = 12.sp
                                               )
                                           }
                                       }
                                   }

                                  Spacer(modifier = Modifier.height(16.dp))

                                  // Document listing or empty state
                                  if (sharedDocs.isEmpty() && !isLoading) {
                                      Box(
                                          modifier = Modifier.fillMaxWidth().height(100.dp),
                                          contentAlignment = Alignment.Center
                                      ) {
                                          Text(
                                              text = "No documents to show",
                                              color = Color.Gray,
                                              fontFamily = GillSans,
                                              fontSize = 15.sp
                                          )
                                      }
                                  } else if (sharedDocs.isNotEmpty()) {
                                     Column(
                                         modifier = Modifier.fillMaxWidth()
                                     ) {
                                         sharedDocs.forEach { doc ->
                                             DocumentRow(
                                                 doc = doc,
                                                 sharedTag = selectedSharedTab,
                                                 onViewDoc = { onViewDoc(doc, selectedSharedTab) },
                                                 isSelected = selectedDocIds.contains(doc.id),
                                                 onCheckedChange = if (selectedSharedTab == "byme") { checked: Boolean ->
                                                     if (checked) {
                                                         if (!selectedDocIds.contains(doc.id)) {
                                                             selectedDocIds.add(doc.id ?: "")
                                                         }
                                                     } else {
                                                         selectedDocIds.remove(doc.id)
                                                     }
                                                 } else null,
                                                 onRemoveClick = if (selectedSharedTab == "byme") {
                                                     {
                                                         val removeArray = JSONArray().apply {
                                                             put(JSONObject().apply {
                                                                 put("docid", doc.id)
                                                                 put("doctype", "general")
                                                             })
                                                         }
                                                         val payload = JSONObject().apply {
                                                             if (isCorporate) {
                                                                 put("relid", model.id)
                                                             }
                                                             put("remove", removeArray)
                                                             put("add", JSONArray())
                                                             put("message", "")
                                                         }
                                                         onUnshareDocs(payload)
                                                     }
                                                 } else null
                                             )
                                         }
                                     }
                                 }

                                  // Batch Unshare Button Bar
                                  if (selectedSharedTab == "byme") {
                                      Spacer(modifier = Modifier.height(16.dp))
                                      Row(
                                          modifier = Modifier.fillMaxWidth(),
                                          horizontalArrangement = Arrangement.SpaceAround
                                      ) {
                                          OutlinedButton(
                                              onClick = {
                                                  selectedDocIds.clear()
                                                  onDismiss()
                                              },
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
                                                  if (selectedDocIds.isEmpty()) {
                                                      validationAlertMessage = "Please select at least one document"
                                                  } else {
                                                      val removeArray = JSONArray()
                                                      for (id in selectedDocIds) {
                                                          removeArray.put(JSONObject().apply {
                                                              put("docid", id)
                                                              put("doctype", "general")
                                                          })
                                                      }
                                                      val payload = JSONObject().apply {
                                                          if (isCorporate) {
                                                              put("relid", model.id)
                                                          }
                                                          put("remove", removeArray)
                                                          put("add", JSONArray())
                                                          put("message", "")
                                                      }
                                                      onUnshareDocs(payload)
                                                      selectedDocIds.clear()
                                                  }
                                              },
                                              colors = ButtonDefaults.buttonColors(
                                                  containerColor = Color(0xFF004D87),
                                                  disabledContainerColor = Color(0xFF004D87).copy(alpha = 0.5f)
                                              ),
                                              enabled = selectedDocIds.isNotEmpty(),
                                              shape = RoundedCornerShape(10.dp),
                                              modifier = Modifier.width(120.dp)
                                          ) {
                                              val buttonText = if (selectedDocIds.isNotEmpty()) "Unshare (${selectedDocIds.size})" else "Unshare"
                                              Text(text = buttonText, color = Color.White, fontFamily = GillSans, fontWeight = FontWeight.Bold)
                                          }
                                      }
                                  }
                              }
                          }
                      }
                  }
             }
         }
     }

     if (validationAlertMessage.isNotEmpty()) {
         AlertDialog(
             onDismissRequest = { validationAlertMessage = "" },
             title = { Text("Lauditor", fontFamily = GillSans, fontWeight = FontWeight.Bold) },
             text = { Text(validationAlertMessage, fontFamily = GillSans) },
             confirmButton = {
                 Button(
                     onClick = { validationAlertMessage = "" },
                     colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF004D87))
                 ) {
                     Text("OK", color = Color.White, fontFamily = GillSans)
                 }
             }
         )
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

@Composable
fun DetailFieldIndividual(label: String, value: String) {
    val displayValue = if (value == "null" || value.isEmpty()) "" else value
    Column(modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp)) {
        Text(
            text = label,
            color = Color(0xFF004D87),
            fontFamily = GillSans,
            fontSize = 15.sp
        )
        Text(
            text = displayValue.ifEmpty { " " },
            color = Color.Black,
            fontFamily = GillSans,
            fontSize = 15.sp
        )
    }
}

@Composable
fun DetailFieldEntity(label: String, value: String, iconResId: Int) {
    val displayValue = if (value == "null" || value.isEmpty()) "" else value
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.padding(vertical = 4.dp).fillMaxWidth()
    ) {
        Image(
            painter = painterResource(id = iconResId),
            contentDescription = label,
            modifier = Modifier
                .size(40.dp)
                .padding(8.dp)
        )
        Spacer(modifier = Modifier.width(10.dp))
        Column {
            Text(
                text = label,
                color = Color(0xFF004D87),
                fontFamily = GillSans,
                fontSize = 15.sp
            )
            Text(
                text = displayValue.ifEmpty { " " },
                color = Color.Black,
                fontFamily = GillSans,
                fontSize = 15.sp
            )
        }
    }
}
