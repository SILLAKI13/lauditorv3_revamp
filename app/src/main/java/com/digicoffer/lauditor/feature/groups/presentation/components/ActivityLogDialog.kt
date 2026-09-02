package com.digicoffer.lauditor.feature.groups.presentation.components

import android.app.DatePickerDialog
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.digicoffer.lauditor.Groups.Models.GroupModel
import com.digicoffer.lauditor.Groups.Models.SearchDo
import com.digicoffer.lauditor.Groups.Models.ViewGroupModel
import com.digicoffer.lauditor.R
import com.digicoffer.lauditor.core.ui.common.search.AppSearchField
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ActivityLogDialog(
    group: ViewGroupModel,
    membersList: List<GroupModel>,
    auditLogs: List<SearchDo>,
    onSearch: (category: String, tm: String, client: String, fromDate: String, search: String, toDate: String) -> Unit,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val activeBlue = Color(0xFF004D87)
    val textGray = Color(0xFF707070)

    val categories = listOf(
        "Authorization",
        "Groups",
        "Team Members",
        "Relationships",
        "Share",
        "Documents",
        "Merge PDF",
        "Matters",
        "Timesheets"
    )

    var selectedCategory by remember { mutableStateOf("Groups") }
    var isCategoryExpanded by remember { mutableStateOf(false) }

    var clientQuery by remember { mutableStateOf("") }
    var tmQuery by remember { mutableStateOf("") }
    var messageQuery by remember { mutableStateOf("") }
    var logSearchQuery by remember { mutableStateOf("") }

    var fromDateStr by remember { mutableStateOf("From") }
    var toDateStr by remember { mutableStateOf("To") }

    val calendar = Calendar.getInstance()

    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(10.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 12.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp)
            ) {
                // Header row containing Category label and Close icon
                Box(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.align(Alignment.CenterStart),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Category",
                            color = activeBlue,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 15.sp,
                            fontFamily = FontFamily(Font(R.font.gill_sans_regular))
                        )
                        Text(text = " *", color = Color.Red, fontSize = 15.sp)
                    }

                    Image(
                        painter = painterResource(id = R.drawable.icon_cancel), // X close button
                        contentDescription = "Close log",
                        modifier = Modifier
                            .size(22.dp)
                            .align(Alignment.TopEnd)
                            .clickable { onDismiss() }
                    )
                }

                Spacer(modifier = Modifier.height(6.dp))

                // Category dropdown selector
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color(0xFFF9FAFB), shape = RoundedCornerShape(6.dp))
                        .border(width = 0.5.dp, color = Color(0xFFC0C0C0), shape = RoundedCornerShape(6.dp))
                        .clickable { isCategoryExpanded = true }
                        .padding(10.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = selectedCategory,
                            color = Color.Black,
                            fontSize = 15.sp,
                            fontFamily = FontFamily(Font(R.font.gill_sans_regular))
                        )
                        Image(
                            painter = painterResource(id = R.drawable.menu_down_icon),
                            contentDescription = "Dropdown menu",
                            modifier = Modifier.size(18.dp)
                        )
                    }

                    DropdownMenu(
                        expanded = isCategoryExpanded,
                        onDismissRequest = { isCategoryExpanded = false },
                        modifier = Modifier.fillMaxWidth(0.7f)
                    ) {
                        categories.forEach { cat ->
                            DropdownMenuItem(
                                text = { Text(cat) },
                                onClick = {
                                    selectedCategory = cat
                                    isCategoryExpanded = false
                                }
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Client Input
                Text(
                    text = "Client",
                    color = activeBlue,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 15.sp,
                    fontFamily = FontFamily(Font(R.font.gill_sans_regular))
                )
                Spacer(modifier = Modifier.height(6.dp))
                CustomTextField(
                    value = clientQuery,
                    onValueChange = { clientQuery = it },
                    placeholder = "Client"
                )

                Spacer(modifier = Modifier.height(14.dp))

                // Team Members Input
                Text(
                    text = "Team Members",
                    color = activeBlue,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 15.sp,
                    fontFamily = FontFamily(Font(R.font.gill_sans_regular))
                )
                Spacer(modifier = Modifier.height(6.dp))
                CustomTextField(
                    value = tmQuery,
                    onValueChange = { tmQuery = it },
                    placeholder = "Team Members"
                )

                Spacer(modifier = Modifier.height(14.dp))

                // Search field Input
                Text(
                    text = "Search",
                    color = activeBlue,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 15.sp,
                    fontFamily = FontFamily(Font(R.font.gill_sans_regular))
                )
                Spacer(modifier = Modifier.height(6.dp))
                CustomTextField(
                    value = messageQuery,
                    onValueChange = { messageQuery = it },
                    placeholder = "Search"
                )

                Spacer(modifier = Modifier.height(14.dp))

                // Date Fields (From / To side-by-side)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "From",
                            color = activeBlue,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 15.sp,
                            fontFamily = FontFamily(Font(R.font.gill_sans_regular))
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(Color(0xFFF9FAFB), shape = RoundedCornerShape(6.dp))
                                .border(width = 0.5.dp, color = Color(0xFFC0C0C0), shape = RoundedCornerShape(6.dp))
                                .clickable {
                                    val dp = DatePickerDialog(
                                        context,
                                        { _, year, month, day ->
                                            calendar.set(year, month, day)
                                            val sdf = SimpleDateFormat("dd-MM-yyyy", Locale.US)
                                            fromDateStr = sdf.format(calendar.time)
                                        },
                                        calendar.get(Calendar.YEAR),
                                        calendar.get(Calendar.MONTH),
                                        calendar.get(Calendar.DAY_OF_MONTH)
                                    )
                                    dp.datePicker.maxDate = System.currentTimeMillis()
                                    dp.show()
                                }
                                .padding(10.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = fromDateStr,
                                color = if (fromDateStr == "From") textGray else Color.Black,
                                fontSize = 15.sp,
                                fontFamily = FontFamily(Font(R.font.gill_sans_regular))
                            )
                            Image(
                                painter = painterResource(id = R.drawable.img_15), // Calendar Icon
                                contentDescription = "Calendar From",
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "To",
                            color = activeBlue,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 15.sp,
                            fontFamily = FontFamily(Font(R.font.gill_sans_regular))
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(Color(0xFFF9FAFB), shape = RoundedCornerShape(6.dp))
                                .border(width = 0.5.dp, color = Color(0xFFC0C0C0), shape = RoundedCornerShape(6.dp))
                                .clickable {
                                    val dp = DatePickerDialog(
                                        context,
                                        { _, year, month, day ->
                                            calendar.set(year, month, day)
                                            val sdf = SimpleDateFormat("dd-MM-yyyy", Locale.US)
                                            toDateStr = sdf.format(calendar.time)
                                        },
                                        calendar.get(Calendar.YEAR),
                                        calendar.get(Calendar.MONTH),
                                        calendar.get(Calendar.DAY_OF_MONTH)
                                    )
                                    dp.datePicker.maxDate = System.currentTimeMillis()
                                    dp.show()
                                }
                                .padding(10.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = toDateStr,
                                color = if (toDateStr == "To") textGray else Color.Black,
                                fontSize = 15.sp,
                                fontFamily = FontFamily(Font(R.font.gill_sans_regular))
                            )
                            Image(
                                painter = painterResource(id = R.drawable.img_15), // Calendar Icon
                                contentDescription = "Calendar To",
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(18.dp))

                // Left-aligned Search button matching screenshot size
                Button(
                    onClick = {
                        val fDate = if (fromDateStr == "From") "" else fromDateStr
                        val tDate = if (toDateStr == "To") "" else toDateStr
                        onSearch(selectedCategory, tmQuery, clientQuery, fDate, messageQuery, tDate)
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = activeBlue),
                    shape = RoundedCornerShape(8.dp),
                    contentPadding = PaddingValues(horizontal = 24.dp, vertical = 0.dp),
                    modifier = Modifier
                        .height(38.dp)
                ) {
                    Text(
                        text = "Search",
                        color = Color.White,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 14.sp,
                        fontFamily = FontFamily(Font(R.font.gill_sans_regular))
                    )
                }

                Spacer(modifier = Modifier.height(18.dp))

                // Search filter bar
                AppSearchField(
                    value = logSearchQuery,
                    onValueChange = { logSearchQuery = it },
                    placeholder = "Search",
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(14.dp))

                // Results list scroll container
                val filteredLogs = auditLogs.filter {
                    logSearchQuery.isEmpty() || (it.msg ?: "").contains(logSearchQuery, ignoreCase = true)
                }

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = 240.dp)
                ) {
                    if (filteredLogs.isEmpty()) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 24.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "No records found",
                                color = Color.Gray,
                                fontSize = 14.sp,
                                fontFamily = FontFamily(Font(R.font.gill_sans_regular))
                            )
                        }
                    } else {
                        // Display inside separate elevated cards exactly like the screenshot
                        filteredLogs.forEach { log ->
                            Card(
                                shape = RoundedCornerShape(8.dp),
                                colors = CardDefaults.cardColors(containerColor = Color.White),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp)
                                    .border(width = 0.5.dp, color = Color(0xFFC0C0C0), shape = RoundedCornerShape(8.dp))
                            ) {
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(14.dp)
                                ) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = selectedCategory,
                                            color = Color.Black,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 15.sp,
                                            fontFamily = FontFamily(Font(R.font.gill_sans_regular))
                                        )
                                        Text(
                                            text = log.timestamp ?: "",
                                            color = Color.Gray,
                                            fontSize = 13.sp,
                                            fontFamily = FontFamily(Font(R.font.gill_sans_regular))
                                        )
                                    }
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Text(
                                        text = log.msg ?: "",
                                        color = Color.Black,
                                        fontSize = 14.sp,
                                        fontFamily = FontFamily(Font(R.font.gill_sans_regular))
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
