package com.digicoffer.lauditor.feature.dashboard.presentation.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.digicoffer.lauditor.Dashboard.DahboardModels.CardDataModels
import com.digicoffer.lauditor.R

private val GillSansRegular = FontFamily(Font(R.font.gill_sans_regular))

@Composable
fun BillableHoursCard(
    data: CardDataModels.BillableCardData,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val badgeLabel = data.badge?.label ?: (if (data.billablePercentage >= 0) "+0.00%" else "0.00%")
    val isPositive = data.billablePercentage >= 0

    Card(
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        modifier = modifier
            .width(300.dp)
            .height(160.dp)
            .padding(6.dp)
            .clickable { onClick() }
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(15.dp)
        ) {
            // Header Row: Clock Icon + Percentage Pill
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(Color(0xFFE4F2FF)),
                    contentAlignment = Alignment.Center
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.ic_clock),
                        contentDescription = "Billable Hours",
                        colorFilter = ColorFilter.tint(Color(0xFF004D87)),
                        modifier = Modifier.size(20.dp)
                    )
                }

                Spacer(modifier = Modifier.weight(1f))

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .background(if (isPositive) Color(0xFFE8F5E9) else Color(0xFFFFEBEE))
                        .padding(horizontal = 10.dp, vertical = 4.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = badgeLabel,
                        style = TextStyle(
                            fontFamily = GillSansRegular,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (isPositive) Color(0xFF43A047) else Color(0xFFE53935)
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = "Total Billable Hours",
                style = TextStyle(
                    fontFamily = GillSansRegular,
                    fontSize = 12.sp,
                    color = Color(0xFF757575)
                )
            )

            Text(
                text = data.billableHours ?: "0:00",
                style = TextStyle(
                    fontFamily = GillSansRegular,
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.Black
                ),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            Text(
                text = data.dateRangeLabel ?: "",
                style = TextStyle(
                    fontFamily = GillSansRegular,
                    fontSize = 12.sp,
                    color = Color(0xFF757575)
                )
            )
        }
    }
}

@Composable
fun ApproxRevenueCard(
    data: CardDataModels.ApproxRevenueCardData,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val sym = data.currencySymbol ?: "₹"
    val formattedRevenue = sym + String.format("%,.2f", data.approxRevenue)
    val badgeLabel = data.badge?.label ?: "+0.00%"

    Card(
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        modifier = modifier
            .width(300.dp)
            .height(160.dp)
            .padding(6.dp)
            .clickable { onClick() }
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(15.dp)
        ) {
            // Header Row: Icon + Percentage Pill
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(Color(0xFFE8F5E9)),
                    contentAlignment = Alignment.Center
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.ic_revenue),
                        contentDescription = "Approx Revenue",
                        modifier = Modifier.size(20.dp)
                    )
                }

                Spacer(modifier = Modifier.weight(1f))

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color(0xFFE8F5E9))
                        .padding(horizontal = 10.dp, vertical = 4.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = badgeLabel,
                        style = TextStyle(
                            fontFamily = GillSansRegular,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF43A047)
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = "Approximate Revenue",
                style = TextStyle(
                    fontFamily = GillSansRegular,
                    fontSize = 12.sp,
                    color = Color(0xFF757575)
                )
            )

            Text(
                text = formattedRevenue,
                style = TextStyle(
                    fontFamily = GillSansRegular,
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.Black
                ),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            Text(
                text = data.dateRangeLabel ?: "",
                style = TextStyle(
                    fontFamily = GillSansRegular,
                    fontSize = 11.sp,
                    color = Color(0xFF757575)
                )
            )
        }
    }
}

@Composable
fun SubscriptionCard(
    data: CardDataModels.SubscriptionCardData,
    onPayNow: () -> Unit,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val statusLabel = data.status?.label ?: if (data.isActive) "Active" else "Inactive"
    val isGreenStatus = data.isActive && !data.isExpired
    val statusBg = if (isGreenStatus) Color(0xFFE8F5E9) else Color(0xFFFFEBEE)
    val statusText = if (isGreenStatus) Color(0xFF43A047) else Color(0xFFE53935)

    val gradientBanner = Brush.horizontalGradient(
        colors = listOf(
            Color(0xFF004D87),
            Color(0xFF0A75B8)
        )
    )

    Card(
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        modifier = modifier
            .width(300.dp)
            .height(160.dp)
            .padding(6.dp)
            .clickable { onClick() }
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(15.dp)
        ) {
            // Row 1: Title + Status Pill
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Subscription",
                    style = TextStyle(
                        fontFamily = GillSansRegular,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.Black
                    ),
                    modifier = Modifier.weight(1f)
                )

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .background(statusBg)
                        .padding(horizontal = 12.dp, vertical = 4.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = statusLabel,
                        style = TextStyle(
                            fontFamily = GillSansRegular,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = statusText
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Row 2: Gradient Blue Banner
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(8.dp))
                    .background(brush = gradientBanner)
                    .padding(8.dp)
            ) {
                Column {
                    Text(
                        text = "Product Subscription",
                        style = TextStyle(
                            fontFamily = GillSansRegular,
                            fontSize = 11.sp,
                            color = Color(0xDDFFFFFF)
                        )
                    )
                    Text(
                        text = if (!data.planLabel.isNullOrEmpty()) data.planLabel!! else "—",
                        style = TextStyle(
                            fontFamily = GillSansRegular,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        ),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Row 3: Validity Date
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = if (data.isExpired) "Expired On" else "Valid Until",
                    style = TextStyle(
                        fontFamily = GillSansRegular,
                        fontSize = 10.sp,
                        color = Color(0xFF757575)
                    ),
                    modifier = Modifier.weight(1f)
                )

                Text(
                    text = if (!data.validTill.isNullOrEmpty()) data.validTill!! else "—",
                    style = TextStyle(
                        fontFamily = GillSansRegular,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Medium,
                        color = Color.Black
                    )
                )
            }
        }
    }
}

@Composable
fun HiringKpiCard(
    data: CardDataModels.HiringCardData,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val groups = data.groups ?: emptyList()
    val group1 = groups.getOrNull(0)
    val group2 = groups.getOrNull(1)

    Card(
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        modifier = modifier
            .width(300.dp)
            .height(160.dp)
            .padding(6.dp)
            .clickable { onClick() }
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(15.dp)
        ) {
            // Header Row: Title + Total Badge
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "New Hires",
                    style = TextStyle(
                        fontFamily = GillSansRegular,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.Black
                    ),
                    modifier = Modifier.weight(1f)
                )

                val badgeCount = data.badge?.label ?: data.totalMembers.toString()
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color(0xFFE4F2FF))
                        .padding(horizontal = 8.dp, vertical = 4.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = badgeCount,
                        style = TextStyle(
                            fontFamily = GillSansRegular,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF004D87)
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Two Stat Boxes: Group 1 & Group 2
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Group 1 Box (e.g. Clients / Groups)
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color(0xFFE4F2FF))
                        .padding(vertical = 8.dp, horizontal = 6.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = (group1?.memberCount ?: 0).toString(),
                            style = TextStyle(
                                fontFamily = GillSansRegular,
                                fontSize = 22.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF004D87)
                            )
                        )
                        Text(
                            text = group1?.name ?: "Group 1",
                            style = TextStyle(
                                fontFamily = GillSansRegular,
                                fontSize = 12.sp,
                                color = Color(0xFF004D87)
                            ),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }

                // Group 2 Box (e.g. Teams / Members)
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color(0xFFEDE7F6))
                        .padding(vertical = 8.dp, horizontal = 6.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = (group2?.memberCount ?: 0).toString(),
                            style = TextStyle(
                                fontFamily = GillSansRegular,
                                fontSize = 22.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF512DA8)
                            )
                        )
                        Text(
                            text = group2?.name ?: "Group 2",
                            style = TextStyle(
                                fontFamily = GillSansRegular,
                                fontSize = 12.sp,
                                color = Color(0xFF512DA8)
                            ),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }
        }
    }
}
