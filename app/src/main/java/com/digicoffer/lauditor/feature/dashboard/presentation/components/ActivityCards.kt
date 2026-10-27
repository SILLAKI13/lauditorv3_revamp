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
import com.digicoffer.lauditor.feature.dashboard.data.model.DashboardCardItemModel

private val GillSansRegular = FontFamily(Font(R.font.gill_sans_regular))

@Composable
fun MeetingCard(
    data: CardDataModels.MeetingCardData,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    ActivityCommonCard(
        iconRes = R.drawable.ic_calendar_blue,
        iconTint = null,
        iconBgColor = Color(0xFFE4F2FF),
        label = "Upcoming Meeting",
        badge = data.badge,
        badgeBgColor = Color(0xFFE4F2FF),
        badgeTextColor = Color(0xFF004D87),
        hasData = data.hasMeeting,
        primaryText = data.subject ?: "",
        secondaryText = if (data.startTime != null && data.endTime != null) "${data.startTime} – ${data.endTime}" else (data.startTime ?: ""),
        emptyText = "No meetings scheduled for today",
        onClick = onClick,
        modifier = modifier
    )
}

@Composable
fun AppointmentCard(
    data: CardDataModels.AppointmentCardData,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    ActivityCommonCard(
        iconRes = R.drawable.ic_clock,
        iconTint = Color(0xFF8E24AA),
        iconBgColor = Color(0xFFF3E5F5),
        label = "Upcoming Appointment",
        badge = data.badge,
        badgeBgColor = Color(0xFFF3E5F5),
        badgeTextColor = Color(0xFF8E24AA),
        hasData = data.hasAppointment,
        primaryText = data.subject ?: "",
        secondaryText = if (data.startTime != null && data.endTime != null) "${data.startTime} – ${data.endTime}" else (data.startTime ?: ""),
        emptyText = "No appointments for today",
        onClick = onClick,
        modifier = modifier
    )
}

@Composable
fun NotificationCard(
    data: CardDataModels.NotificationCardData,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val notifs = data.notifications
    val first = notifs?.firstOrNull()
    val hasData = !notifs.isNullOrEmpty()
    val primary = if (first?.message.isNullOrEmpty()) "New notification" else first?.message ?: ""
    val secondary = first?.time ?: ""

    ActivityCommonCard(
        iconRes = R.drawable.ic_bell_orange,
        iconTint = null,
        iconBgColor = Color(0xFFFFF3E0),
        label = "Notifications",
        badge = data.badge,
        badgeBgColor = Color(0xFFFFF3E0),
        badgeTextColor = Color(0xFFE65100),
        hasData = hasData,
        primaryText = primary,
        secondaryText = secondary,
        emptyText = "No new notifications",
        onClick = onClick,
        modifier = modifier
    )
}

@Composable
fun MessagesCard(
    data: CardDataModels.MessagesCardData,
    clientCount: Int,
    teamCount: Int,
    userRole: String?,
    userCategory: String?,
    onCardClick: () -> Unit,
    onClientClick: () -> Unit,
    onTeamClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val totalCount = clientCount + teamCount
    val isAAM = userRole == "AAM"
    val isSolo = userCategory == "solo"

    Card(
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        modifier = modifier
            .width(300.dp)
            .height(160.dp)
            .padding(6.dp)
            .clickable { onCardClick() }
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(15.dp)
        ) {
            // Header Row: Icon + Badge
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Icon
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(Color(0xFFE4F2FF)),
                    contentAlignment = Alignment.Center
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.ic_chat_green),
                        contentDescription = "Messages",
                        colorFilter = ColorFilter.tint(Color(0xFF004D87)),
                        modifier = Modifier.size(20.dp)
                    )
                }

                Spacer(modifier = Modifier.weight(1f))

                // Pill Badge
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color(0xFFE4F2FF))
                        .padding(horizontal = 8.dp, vertical = 4.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = totalCount.toString(),
                        style = TextStyle(
                            fontFamily = GillSansRegular,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF004D87)
                        )
                    )
                }
            }

            // Card Label
            Text(
                text = "New Messages",
                style = TextStyle(
                    fontFamily = GillSansRegular,
                    fontSize = 15.sp,
                    color = Color(0xFF757575)
                ),
                modifier = Modifier.padding(top = 2.dp)
            )

            // Content: Client & Team Counts
            if (totalCount > 0) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 4.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    if (!isAAM) {
                        // Client Box
                        Column(
                            modifier = Modifier
                                .weight(1f)
                                .clickable { onClientClick() },
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(30.dp)
                                    .clip(CircleShape)
                                    .background(Color(0xFFE3F2FD)),
                                contentAlignment = Alignment.Center
                            ) {
                                Image(
                                    painter = painterResource(id = R.drawable.dashboard_client_icon),
                                    contentDescription = "Client",
                                    colorFilter = ColorFilter.tint(Color(0xFF1976D2)),
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                            Text(
                                text = clientCount.toString(),
                                style = TextStyle(
                                    fontFamily = GillSansRegular,
                                    fontSize = 17.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.Black
                                )
                            )
                            Text(
                                text = "Client",
                                style = TextStyle(
                                    fontFamily = GillSansRegular,
                                    fontSize = 12.sp,
                                    color = Color(0xFF757575)
                                )
                            )
                        }
                    }

                    if (!isSolo) {
                        // Team Box
                        Column(
                            modifier = Modifier
                                .weight(1f)
                                .clickable { onTeamClick() },
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(30.dp)
                                    .clip(CircleShape)
                                    .background(Color(0xFFEDE7F6)),
                                contentAlignment = Alignment.Center
                            ) {
                                Image(
                                    painter = painterResource(id = R.drawable.dashboard_team_icon),
                                    contentDescription = "Team",
                                    colorFilter = ColorFilter.tint(Color(0xFF512DA8)),
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                            Text(
                                text = teamCount.toString(),
                                style = TextStyle(
                                    fontFamily = GillSansRegular,
                                    fontSize = 17.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.Black
                                )
                            )
                            Text(
                                text = "Team",
                                style = TextStyle(
                                    fontFamily = GillSansRegular,
                                    fontSize = 12.sp,
                                    color = Color(0xFF757575)
                                )
                            )
                        }
                    }
                }
            } else {
                Text(
                    text = "No new messages",
                    style = TextStyle(
                        fontFamily = GillSansRegular,
                        fontSize = 12.sp,
                        color = Color(0xFF757575)
                    ),
                    modifier = Modifier.padding(top = 10.dp)
                )
            }
        }
    }
}

@Composable
private fun ActivityCommonCard(
    iconRes: Int,
    iconTint: Color?,
    iconBgColor: Color,
    label: String,
    badge: CardDataModels.BadgeData?,
    badgeBgColor: Color,
    badgeTextColor: Color,
    hasData: Boolean,
    primaryText: String,
    secondaryText: String,
    emptyText: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
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
            // Header: Icon + Badge
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(iconBgColor),
                    contentAlignment = Alignment.Center
                ) {
                    Image(
                        painter = painterResource(id = iconRes),
                        contentDescription = label,
                        colorFilter = iconTint?.let { ColorFilter.tint(it) },
                        modifier = Modifier.size(20.dp)
                    )
                }

                Spacer(modifier = Modifier.weight(1f))

                val badgeCount = badge?.label ?: badge?.count?.toInt()?.toString() ?: "0"
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .background(badgeBgColor)
                        .padding(horizontal = 8.dp, vertical = 4.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = badgeCount,
                        style = TextStyle(
                            fontFamily = GillSansRegular,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = badgeTextColor
                        )
                    )
                }
            }

            // Card Label
            Text(
                text = label,
                style = TextStyle(
                    fontFamily = GillSansRegular,
                    fontSize = 15.sp,
                    color = Color(0xFF757575)
                ),
                modifier = Modifier.padding(top = 4.dp)
            )

            if (hasData) {
                Text(
                    text = primaryText,
                    style = TextStyle(
                        fontFamily = GillSansRegular,
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.Black
                    ),
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.padding(top = 2.dp)
                )

                if (secondaryText.isNotBlank()) {
                    Text(
                        text = secondaryText,
                        style = TextStyle(
                            fontFamily = GillSansRegular,
                            fontSize = 12.sp,
                            color = Color(0xFF757575)
                        ),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.padding(top = 2.dp)
                    )
                }
            } else {
                Text(
                    text = emptyText,
                    style = TextStyle(
                        fontFamily = GillSansRegular,
                        fontSize = 12.sp,
                        color = Color(0xFF757575)
                    ),
                    modifier = Modifier.padding(top = 10.dp)
                )
            }
        }
    }
}
