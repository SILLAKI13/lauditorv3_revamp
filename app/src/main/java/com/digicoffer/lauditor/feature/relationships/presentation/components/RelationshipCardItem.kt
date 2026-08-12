package com.digicoffer.lauditor.feature.relationships.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.digicoffer.lauditor.R
import com.digicoffer.lauditor.Relationships.Model.RelationshipsModel

import java.text.SimpleDateFormat
import java.util.Locale
import java.util.TimeZone

private val GillSans = FontFamily(
    Font(R.font.gill_sans)
)

fun formatCreatedDate(rawDate: String): String {
    if (rawDate.isEmpty()) return ""
    // If it's already formatted, return it
    if (rawDate.contains(",") && rawDate.split(" ").size >= 3) return rawDate
    return try {
        val isoFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.US).apply {
            timeZone = TimeZone.getTimeZone("UTC")
        }
        val date = isoFormat.parse(rawDate) ?: return rawDate
        val outputFormat = SimpleDateFormat("MMM dd, yyyy", Locale.US)
        outputFormat.format(date)
    } catch (e: Exception) {
        try {
            val isoFormatWithMillis = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS", Locale.US).apply {
                timeZone = TimeZone.getTimeZone("UTC")
            }
            val date = isoFormatWithMillis.parse(rawDate) ?: return rawDate
            val outputFormat = SimpleDateFormat("MMM dd, yyyy", Locale.US)
            outputFormat.format(date)
        } catch (ex: Exception) {
            try {
                val simpleFormat = SimpleDateFormat("yyyy-MM-dd", Locale.US)
                val date = simpleFormat.parse(rawDate) ?: return rawDate
                val outputFormat = SimpleDateFormat("MMM dd, yyyy", Locale.US)
                outputFormat.format(date)
            } catch (ex2: Exception) {
                rawDate
            }
        }
    }
}

@Composable
fun RelationshipCardItem(
    model: RelationshipsModel,
    onActionClick: (String, RelationshipsModel) -> Unit,
    onCardClick: (RelationshipsModel) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    val isInactive = model.status?.lowercase() == "inactive"
    val isPending = !isInactive && (model.status?.lowercase() == "pending" || !model.isAccepted)

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onCardClick(model) }
            .padding(vertical = 5.dp, horizontal = 10.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        shape = RoundedCornerShape(8.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(10.dp)
        ) {
            Column(
                modifier = Modifier.fillMaxWidth()
            ) {
                Spacer(modifier = Modifier.height(10.dp))
                Text(
                    text = model.name ?: "",
                    color = Color(0xFF004D87),
                    fontFamily = GillSans,
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Created ${formatCreatedDate(model.created ?: "")}",
                    color = Color.Black,
                    fontFamily = GillSans,
                    fontSize = 13.sp
                )
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = model.clientType ?: "Consumer",
                        color = Color.Black,
                        fontFamily = GillSans,
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp
                    )

                    val (textColor, bgColor, label) = when {
                        isInactive -> Triple(Color(0xFFC62828), Color(0xFFFFCDD2), "Inactive")
                        isPending -> Triple(Color(0xFFF57C00), Color(0xFFFFE0B2), "Pending")
                        else -> Triple(Color(0xFF2E7D32), Color(0xFFC8E6C9), "Active")
                    }

                    val badgeShape = if (isPending) RoundedCornerShape(20.dp) else RoundedCornerShape(15.dp)
                    Box(
                        modifier = Modifier
                            .clip(badgeShape)
                            .background(bgColor)
                            .padding(horizontal = 10.dp, vertical = 4.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = label,
                            color = textColor,
                            fontFamily = GillSans,
                            fontWeight = FontWeight.Normal,
                            fontSize = 13.sp,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }

            Box(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(top = 2.dp, end = 4.dp)
            ) {
                IconButton(
                    onClick = { expanded = true },
                    modifier = Modifier.size(30.dp)
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.img_17),
                        contentDescription = "Actions Menu",
                        tint = Color.Black,
                        modifier = Modifier.padding(2.dp)
                    )
                }

                DropdownMenu(
                    expanded = expanded,
                    onDismissRequest = { expanded = false },
                    modifier = Modifier.background(Color.White)
                ) {
                    DropdownMenuItem(
                        text = {
                            Text(
                                text = "Exchange Information",
                                color = Color.Black,
                                fontFamily = GillSans
                            )
                        },
                        onClick = {
                            expanded = false
                            onActionClick("Exchange Information", model)
                        }
                    )
                    Divider(color = Color(0xFFDDDDDE))
                    val isIndividual = model.clientType?.lowercase() == "consumer"
                    if (!isIndividual) {
                        DropdownMenuItem(
                            text = {
                                Text(
                                    text = "Manage Groups",
                                    color = Color.Black,
                                    fontFamily = GillSans
                                )
                            },
                            onClick = {
                                expanded = false
                                onActionClick("Manage Groups", model)
                            }
                        )
                        Divider(color = Color(0xFFDDDDDE))
                    }
                    val isActive = !isPending && !isInactive
                    if (isActive) {
                        DropdownMenuItem(
                            text = {
                                Text(
                                    text = "Manage Team Members",
                                    color = Color.Black,
                                    fontFamily = GillSans
                                )
                            },
                            onClick = {
                                expanded = false
                                onActionClick("Manage Team Members", model)
                            }
                        )
                        Divider(color = Color(0xFFDDDDDE))
                    }
                    DropdownMenuItem(
                        text = {
                            Text(
                                text = "Delete Relationship",
                                color = Color.Black,
                                fontFamily = GillSans
                            )
                        },
                        onClick = {
                            expanded = false
                            onActionClick("Delete Relationship", model)
                        }
                    )
                    Divider(color = Color(0xFFDDDDDE))
                }
            }
        }
    }
}
