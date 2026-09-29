package com.digicoffer.lauditor.feature.profile.presentation.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.digicoffer.lauditor.R
import com.digicoffer.lauditor.core.ui.common.foundation.AppSpacer
import com.digicoffer.lauditor.feature.profile.presentation.state.DayAvailabilityState
import java.text.SimpleDateFormat
import java.util.*

private val GillSans = FontFamily(Font(R.font.gill_sans))
private val GillSansBold = FontFamily(Font(R.font.gill_sans_bold))

@Composable
fun SlotExclusionDialog(
    day: DayAvailabilityState,
    onSave: (List<String>) -> Unit,
    onDismiss: () -> Unit
) {
    val allSlots = remember(day.fromTime, day.toTime) {
        generate30MinSlots(day.fromTime, day.toTime)
    }

    val selectedSlots = remember { mutableStateListOf<String>().apply { addAll(day.excludedSlots) } }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Text(
                    text = "Exclude Slots - ${day.dayName}",
                    color = Color(0xFF004D87),
                    fontSize = 16.sp,
                    fontFamily = GillSansBold
                )

                Text(
                    text = "Select up to 4 slots to exclude from booking (${selectedSlots.size}/4 selected)",
                    color = Color(0xFF666666),
                    fontSize = 12.sp,
                    fontFamily = GillSans,
                    modifier = Modifier.padding(top = 4.dp, bottom = 12.dp)
                )

                if (allSlots.isEmpty()) {
                    Text(
                        text = "No valid slots available for current time range.",
                        color = Color.Red,
                        fontSize = 13.sp,
                        fontFamily = GillSans
                    )
                } else {
                    LazyVerticalGrid(
                        columns = GridCells.Fixed(2),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(max = 280.dp)
                    ) {
                        items(allSlots) { slot ->
                            val isSelected = selectedSlots.contains(slot)
                            val maxReached = selectedSlots.size >= 4 && !isSelected

                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(
                                        color = if (isSelected) Color(0xFF004D87) else if (maxReached) Color(0xFFF5F5F5) else Color.White,
                                        shape = RoundedCornerShape(6.dp)
                                    )
                                    .border(
                                        1.dp,
                                        if (isSelected) Color(0xFF004D87) else Color(0xFFCCCCCC),
                                        RoundedCornerShape(6.dp)
                                    )
                                    .clickable(enabled = !maxReached || isSelected) {
                                        if (isSelected) {
                                            selectedSlots.remove(slot)
                                        } else if (selectedSlots.size < 4) {
                                            selectedSlots.add(slot)
                                        }
                                    }
                                    .padding(vertical = 8.dp, horizontal = 4.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = slot,
                                    color = if (isSelected) Color.White else if (maxReached) Color.Gray else Color.Black,
                                    fontSize = 11.sp,
                                    fontFamily = if (isSelected) GillSansBold else GillSans,
                                    textAlign = TextAlign.Center
                                )
                            }
                        }
                    }
                }

                AppSpacer(height = 16.dp)

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Button(
                        onClick = onDismiss,
                        shape = RoundedCornerShape(6.dp),
                        border = BorderStroke(1.dp, Color(0xFFCCCCCC)),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFFEBEBEB),
                            contentColor = Color.Black
                        ),
                        modifier = Modifier
                            .weight(1f)
                            .height(40.dp)
                    ) {
                        Text(text = "Cancel", fontSize = 13.sp, fontFamily = GillSans, color = Color.Black)
                    }

                    Button(
                        onClick = { onSave(selectedSlots.toList()) },
                        shape = RoundedCornerShape(6.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF004D87),
                            contentColor = Color.White
                        ),
                        modifier = Modifier
                            .weight(1f)
                            .height(40.dp)
                    ) {
                        Text(text = "Apply", fontSize = 13.sp, fontFamily = GillSansBold, color = Color.White)
                    }
                }
            }
        }
    }
}

private fun generate30MinSlots(fromTime: String, toTime: String): List<String> {
    val slots = mutableListOf<String>()
    try {
        val sdfInFrom = SimpleDateFormat(if (fromTime.contains("AM", true) || fromTime.contains("PM", true)) "hh:mm a" else "H:mm", Locale.US)
        val sdfInTo = SimpleDateFormat(if (toTime.contains("AM", true) || toTime.contains("PM", true)) "hh:mm a" else "H:mm", Locale.US)
        val sdfOut = SimpleDateFormat("hh:mm a", Locale.US)

        val from = sdfInFrom.parse(fromTime.trim()) ?: return slots
        val to = sdfInTo.parse(toTime.trim()) ?: return slots

        val start = Calendar.getInstance().apply { time = from }
        val end = Calendar.getInstance().apply {
            time = to
            if (before(start)) add(Calendar.DATE, 1)
        }

        while (start.before(end)) {
            val slotEnd = (start.clone() as Calendar).apply { add(Calendar.MINUTE, 30) }
            if (slotEnd.after(end)) break
            slots.add("${sdfOut.format(start.time)} - ${sdfOut.format(slotEnd.time)}")
            start.time = slotEnd.time
        }
    } catch (e: Exception) {
        e.printStackTrace()
    }
    return slots
}
