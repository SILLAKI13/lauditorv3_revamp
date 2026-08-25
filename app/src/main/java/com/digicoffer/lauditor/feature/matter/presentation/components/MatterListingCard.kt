package com.digicoffer.lauditor.feature.matter.presentation.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.digicoffer.lauditor.CommonFiles.GlobalFiles.Constants
import com.digicoffer.lauditor.Matter.Models.ViewMatterModel
import com.digicoffer.lauditor.R
import com.digicoffer.lauditor.core.designsystem.colors.ColorTokens

private val GillSans = FontFamily(Font(R.font.gill_sans))

@Composable
fun MatterListingCard(
    matter: ViewMatterModel,
    onActionClick: (String, ViewMatterModel) -> Unit,
    modifier: Modifier = Modifier
) {
    var expandedMenu by remember { mutableStateOf(false) }

    val isSolo = "solo" == Constants.CATEGORY
    val isLegal = "Legal" == Constants.MATTER_TYPE

    val dateValue = if (isLegal) matter.date_of_filling else matter.startdate
    val typeValue = if (isLegal) matter.casetype else matter.matterType

    val tagsStringBuilder = StringBuilder()
    val tagsArray = matter.tags_list
    if (tagsArray != null && tagsArray.length() > 0) {
        for (i in 0 until tagsArray.length()) {
            val tag = tagsArray.optString(i)
            if (tag.isNotEmpty()) {
                if (tagsStringBuilder.isNotEmpty()) tagsStringBuilder.append(", ")
                tagsStringBuilder.append(tag)
            }
        }
    }
    val tagsText = tagsStringBuilder.toString()

    val isMenuEnabled = !matter.isdisabled && matter.is_editable

    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(start = 10.dp, top = 10.dp, end = 10.dp, bottom = 0.dp),
        shape = RoundedCornerShape(10.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp)
        ) {
            // Row for Title, Case Number/ID and 3-dots
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.Top
            ) {
                Column(
                    modifier = Modifier.weight(1f)
                ) {
                    // Matter Title
                    Text(
                        text = matter.title,
                        color = Color.Black,
                        fontFamily = GillSans,
                        fontSize = 15.sp,
                        maxLines = 2,
                        modifier = Modifier.padding(start = 8.dp, end = 4.dp, top = 10.dp)
                    )

                    // Case ID / Number
                    Text(
                        text = matter.matter_id,
                        color = ColorTokens.BluePrimary,
                        fontFamily = GillSans,
                        fontSize = 15.sp,
                        maxLines = 2,
                        modifier = Modifier.padding(start = 8.dp, top = 0.dp)
                    )
                }

                // Three-Dot actions menu
                Box(
                    modifier = Modifier
                        .size(30.dp)
                        .padding(top = 2.dp, end = 4.dp)
                        .alpha(if (isMenuEnabled) 1f else 0.5f)
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.img_17),
                        contentDescription = "Actions",
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(2.dp)
                            .clickable(enabled = isMenuEnabled) {
                                expandedMenu = true
                            }
                    )

                    DropdownMenu(
                        expanded = expandedMenu,
                        onDismissRequest = { expandedMenu = false },
                        modifier = Modifier
                            .background(Color.White)
                            .width(160.dp)
                    ) {
                        DropdownMenuItem(
                            text = {
                                Text(
                                    text = "Edit Matter",
                                    fontFamily = GillSans,
                                    fontSize = 14.sp,
                                    color = Color.Black
                                )
                            },
                            onClick = {
                                expandedMenu = false
                                onActionClick("Edit Matter", matter)
                            }
                        )
                        HorizontalDivider(thickness = 0.5.dp, color = Color.LightGray)
                        DropdownMenuItem(
                            text = {
                                Text(
                                    text = "View Timeline",
                                    fontFamily = GillSans,
                                    fontSize = 14.sp,
                                    color = Color.Black
                                )
                            },
                            onClick = {
                                expandedMenu = false
                                onActionClick("View Timeline", matter)
                            }
                        )
                        HorizontalDivider(thickness = 0.5.dp, color = Color.LightGray)
                        val closeReopenText = if ("Closed" == matter.status) "Reopen Matter" else "Close Matter"
                        DropdownMenuItem(
                            text = {
                                Text(
                                    text = closeReopenText,
                                    fontFamily = GillSans,
                                    fontSize = 14.sp,
                                    color = Color.Black
                                )
                            },
                            onClick = {
                                expandedMenu = false
                                onActionClick(closeReopenText, matter)
                            }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(4.dp))

            // Details section layout
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 4.dp),
                verticalAlignment = Alignment.Bottom
            ) {
                Column(
                    modifier = Modifier.weight(1f)
                ) {
                    // Filed Date Row
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 10.dp)
                    ) {
                        Text(
                            text = "Filed : ",
                            fontFamily = GillSans,
                            fontSize = 13.sp,
                            color = Color.Black,
                            modifier = Modifier.padding(2.dp)
                        )
                        Text(
                            text = dateValue,
                            fontFamily = GillSans,
                            fontSize = 13.sp,
                            color = Color.Black,
                            modifier = Modifier.padding(2.dp)
                        )
                    }

                    // Client Row
                    Row(
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = "Client : ",
                            fontFamily = GillSans,
                            fontSize = 13.sp,
                            color = Color.Black,
                            modifier = Modifier.padding(2.dp)
                        )
                        Text(
                            text = matter.client_name,
                            fontFamily = GillSans,
                            fontSize = 13.sp,
                            color = Color.Black,
                            maxLines = 2,
                            modifier = Modifier.padding(2.dp)
                        )
                    }

                    // Owner Row (hide if Solo)
                    if (!isSolo) {
                        Row(
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = "Owner : ",
                                fontFamily = GillSans,
                                fontSize = 13.sp,
                                color = Color.Black,
                                modifier = Modifier.padding(2.dp)
                            )
                            Text(
                                text = matter.owner_name,
                                fontFamily = GillSans,
                                fontSize = 13.sp,
                                color = Color.Black,
                                modifier = Modifier.padding(2.dp)
                            )
                        }
                    }

                    // Tag Row (hide if empty)
                    if (tagsText.isNotEmpty()) {
                        Row(
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = "Tag : ",
                                fontFamily = GillSans,
                                fontSize = 13.sp,
                                color = Color.Black,
                                modifier = Modifier.padding(2.dp)
                            )
                            Text(
                                text = tagsText,
                                fontFamily = GillSans,
                                fontSize = 13.sp,
                                color = Color.Black,
                                modifier = Modifier.padding(2.dp)
                            )
                        }
                    }

                    // Matter Type Row (hide if empty)
                    if (typeValue.isNotEmpty()) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 8.dp)
                        ) {
                            Text(
                                text = "Type : ",
                                fontFamily = GillSans,
                                fontSize = 13.sp,
                                color = Color.Black,
                                modifier = Modifier.padding(2.dp)
                            )
                            Text(
                                text = typeValue,
                                fontFamily = GillSans,
                                fontSize = 13.sp,
                                color = Color.Black,
                                modifier = Modifier.padding(2.dp)
                            )
                        }
                    }
                }

                // Status chip
                val statusText = when (matter.status) {
                    "Active" -> "Active"
                    "Closed" -> "Closed"
                    else -> "Pending"
                }
                val (badgeBg, badgeText) = when (matter.status) {
                    "Active" -> Pair(Color(0xFFC8E6C9), Color(0xFF2E7D32))
                    "Closed" -> Pair(Color(0xFFFFCDD2), Color(0xFFC62828))
                    else -> Pair(Color(0xFFFFE0B2), Color(0xFFF57C00))
                }

                Box(
                    modifier = Modifier
                        .padding(bottom = 4.dp, end = 4.dp)
                        .background(badgeBg, shape = RoundedCornerShape(50.dp))
                        .padding(horizontal = 10.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = statusText,
                        fontFamily = GillSans,
                        fontSize = 12.sp,
                        color = badgeText
                    )
                }
            }
        }
    }
}
