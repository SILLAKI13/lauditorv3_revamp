package com.digicoffer.lauditor.feature.profile.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.digicoffer.lauditor.R
import com.digicoffer.lauditor.feature.profile.presentation.state.PracticePartnerItem

private val GillSans = FontFamily(
    Font(R.font.gill_sans)
)

private fun cleanDisplayValue(value: String?): String {
    if (value == null) return ""
    val trimmed = value.trim()
    return if (trimmed.equals("null", ignoreCase = true)) "" else trimmed
}

@Composable
fun PracticePartnerCard(
    partner: PracticePartnerItem,
    onEditClick: (PracticePartnerItem) -> Unit,
    onDeleteClick: (PracticePartnerItem) -> Unit,
    modifier: Modifier = Modifier
) {
    var expanded by remember { mutableStateOf(false) }

    Card(
        modifier = modifier
            .fillMaxWidth()
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
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = cleanDisplayValue(partner.name),
                    color = Color(0xFF004D87),
                    fontFamily = GillSans,
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp
                )
                Spacer(modifier = Modifier.height(8.dp))

                PartnerDetailRow(label = "Designation :", value = partner.designation)
                Spacer(modifier = Modifier.height(4.dp))
                PartnerDetailRow(label = "Specialist :", value = partner.specialist)
                Spacer(modifier = Modifier.height(4.dp))
                PartnerDetailRow(label = "Email :", value = partner.email)
                Spacer(modifier = Modifier.height(4.dp))
                PartnerDetailRow(label = "Phone :", value = partner.phone)
                Spacer(modifier = Modifier.height(4.dp))
            }

            // 3-dots action menu in top right corner matching RelationshipCardItem
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
                                text = "Edit Practice Partner",
                                color = Color.Black,
                                fontFamily = GillSans
                            )
                        },
                        onClick = {
                            expanded = false
                            onEditClick(partner)
                        }
                    )
                    HorizontalDivider(color = Color(0xFFDDDDDE), thickness = 0.5.dp)
                    DropdownMenuItem(
                        text = {
                            Text(
                                text = "Delete Practice Partner",
                                color = Color.Black,
                                fontFamily = GillSans
                            )
                        },
                        onClick = {
                            expanded = false
                            onDeleteClick(partner)
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun PartnerDetailRow(label: String, value: String?) {
    val cleanVal = cleanDisplayValue(value)
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            color = Color.Black,
            fontFamily = GillSans,
            fontWeight = FontWeight.Bold,
            fontSize = 14.sp,
            modifier = Modifier.width(95.dp)
        )
        Text(
            text = cleanVal,
            color = Color.Black,
            fontFamily = GillSans,
            fontSize = 14.sp
        )
    }
}
