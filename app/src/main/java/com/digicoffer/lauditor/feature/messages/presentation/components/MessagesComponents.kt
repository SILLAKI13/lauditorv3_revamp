package com.digicoffer.lauditor.feature.messages.presentation.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.digicoffer.lauditor.R
import com.digicoffer.lauditor.feature.messages.data.model.ChatMessageItem
import com.digicoffer.lauditor.feature.messages.data.model.ConversationItem
import com.digicoffer.lauditor.feature.messages.data.model.MessagesTab

val NavyBluePrimary = Color(0xFF004D87)
val PageBgLightBlue = Color(0xFFEAF3FB)
val MessageBubbleGrey = Color(0xFFF0F2F5)
val TextMutedGrey = Color(0xFF8E8E93)
val BorderLightGrey = Color(0xFFE2E8F0)
val UnreadBadgeRed = Color(0xFFE53935)

@Composable
fun MessagesTabRow(
    selectedTab: MessagesTab,
    isAamRole: Boolean,
    isSoloCategory: Boolean,
    onTabSelected: (MessagesTab) -> Unit,
    modifier: Modifier = Modifier
) {
    if (isAamRole || isSoloCategory) return

    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 6.dp),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        val isClients = selectedTab == MessagesTab.CLIENTS
        Surface(
            modifier = Modifier
                .weight(1f)
                .height(42.dp)
                .clip(RoundedCornerShape(topStart = 8.dp, bottomStart = 8.dp))
                .clickable { onTabSelected(MessagesTab.CLIENTS) },
            color = if (isClients) Color(0xFF0F855B) else Color.White,
            shape = RoundedCornerShape(topStart = 8.dp, bottomStart = 8.dp),
            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF0F855B))
        ) {
            Box(contentAlignment = Alignment.Center) {
                Text(
                    text = "Clients",
                    color = if (isClients) Color.White else Color.Black,
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp
                )
            }
        }

        val isTeams = selectedTab == MessagesTab.TEAMS
        Surface(
            modifier = Modifier
                .weight(1f)
                .height(42.dp)
                .clip(RoundedCornerShape(topEnd = 8.dp, bottomEnd = 8.dp))
                .clickable { onTabSelected(MessagesTab.TEAMS) },
            color = if (isTeams) Color(0xFF0F855B) else Color.White,
            shape = RoundedCornerShape(topEnd = 8.dp, bottomEnd = 8.dp),
            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF0F855B))
        ) {
            Box(contentAlignment = Alignment.Center) {
                Text(
                    text = "Teams",
                    color = if (isTeams) Color.White else Color.Black,
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp
                )
            }
        }
    }
}

@Composable
fun SearchBox(
    query: String,
    onQueryChanged: (String) -> Unit,
    placeholder: String = "Type to search",
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 6.dp),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = androidx.compose.foundation.BorderStroke(1.dp, BorderLightGrey),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                painter = painterResource(id = R.drawable.search_groups),
                contentDescription = "Search",
                tint = TextMutedGrey,
                modifier = Modifier.size(20.dp)
            )

            Spacer(modifier = Modifier.width(10.dp))

            BasicTextField(
                value = query,
                onValueChange = onQueryChanged,
                modifier = Modifier.weight(1f),
                textStyle = TextStyle(
                    color = Color.Black,
                    fontSize = 15.sp
                ),
                cursorBrush = SolidColor(NavyBluePrimary),
                singleLine = true,
                decorationBox = { innerTextField ->
                    if (query.isEmpty()) {
                        Text(
                            text = placeholder,
                            color = TextMutedGrey,
                            fontSize = 14.sp
                        )
                    }
                    innerTextField()
                }
            )

            if (query.isNotEmpty()) {
                Icon(
                    painter = painterResource(id = R.drawable.simple_cancel),
                    contentDescription = "Clear search",
                    tint = TextMutedGrey,
                    modifier = Modifier
                        .size(18.dp)
                        .clickable { onQueryChanged("") }
                )
            }
        }
    }
}

@Composable
fun ConversationItemCard(
    item: ConversationItem,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp)
            .clickable { onClick() },
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = androidx.compose.foundation.BorderStroke(1.dp, BorderLightGrey),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Left circular avatar
            Box(
                modifier = Modifier.size(42.dp),
                contentAlignment = Alignment.Center
            ) {
                Surface(
                    modifier = Modifier.size(42.dp),
                    shape = CircleShape,
                    color = NavyBluePrimary
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text(
                            text = item.avatarInitial,
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp
                        )
                    }
                }

                if (item.isOnline) {
                    Box(
                        modifier = Modifier
                            .size(11.dp)
                            .align(Alignment.BottomEnd)
                            .background(Color(0xFF2E7D32), CircleShape)
                            .border(1.5.dp, Color.White, CircleShape)
                    )
                }
            }

            Spacer(modifier = Modifier.width(12.dp))

            // Name + last message preview
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = item.displayName,
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp,
                    color = Color.Black,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                if (item.isTyping) {
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "typing...",
                        color = NavyBluePrimary,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium
                    )
                } else if (!item.lastMessage.isNullOrEmpty()) {
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = item.lastMessage,
                        color = Color(0xFF6C757D),
                        fontSize = 13.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

            Spacer(modifier = Modifier.width(8.dp))

            // Date text on right
            if (!item.lastMessageTime.isNullOrEmpty()) {
                Text(
                    text = item.lastMessageTime,
                    color = TextMutedGrey,
                    fontSize = 12.sp,
                    modifier = Modifier.padding(end = if (item.unreadCount > 0) 8.dp else 0.dp)
                )
            }

            // Unread badge
            if (item.unreadCount > 0) {
                Surface(
                    shape = CircleShape,
                    color = UnreadBadgeRed,
                    modifier = Modifier.size(26.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text(
                            text = if (item.unreadCount > 99) "99+" else item.unreadCount.toString(),
                            color = Color.White,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun TeamAccordionCard(
    team: ConversationItem,
    onToggleExpand: () -> Unit,
    onMemberClick: (ConversationItem) -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = androidx.compose.foundation.BorderStroke(1.dp, BorderLightGrey),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onToggleExpand() }
                    .padding(horizontal = 12.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    modifier = Modifier.size(42.dp),
                    shape = CircleShape,
                    color = NavyBluePrimary
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text(
                            text = team.avatarInitial,
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp
                        )
                    }
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = team.name,
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp,
                        color = Color.Black
                    )
                    Text(
                        text = "${team.members.size} members",
                        fontSize = 12.sp,
                        color = TextMutedGrey
                    )
                }

                if (team.unreadCount > 0) {
                    Surface(
                        shape = CircleShape,
                        color = UnreadBadgeRed,
                        modifier = Modifier
                            .size(26.dp)
                            .padding(end = 4.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Text(
                                text = team.unreadCount.toString(),
                                color = Color.White,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                    Spacer(modifier = Modifier.width(6.dp))
                }

                Icon(
                    painter = painterResource(
                        id = if (team.isExpanded) R.drawable.minus_icon_small_chat else R.drawable.plus_icon_xl_chat
                    ),
                    contentDescription = if (team.isExpanded) "Collapse" else "Expand",
                    tint = Color.Unspecified,
                    modifier = Modifier.size(28.dp)
                )
            }

            AnimatedVisibility(
                visible = team.isExpanded,
                enter = expandVertically() + fadeIn(),
                exit = shrinkVertically() + fadeOut()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color(0xFFF9FAFB))
                        .padding(start = 12.dp, end = 12.dp, bottom = 8.dp)
                ) {
                    team.members.forEach { member ->
                        ConversationItemCard(
                            item = member,
                            onClick = { onMemberClick(member) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun MessageBubble(
    message: ChatMessageItem,
    showDateHeader: Boolean,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 14.dp, vertical = 4.dp),
        horizontalAlignment = if (message.isSentByMe) Alignment.End else Alignment.Start
    ) {
        if (showDateHeader && message.dateGroupHeader.isNotEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 12.dp),
                contentAlignment = Alignment.Center
            ) {
                Surface(
                    shape = RoundedCornerShape(20.dp),
                    color = Color.White,
                    border = androidx.compose.foundation.BorderStroke(1.dp, BorderLightGrey),
                    shadowElevation = 1.dp
                ) {
                    Text(
                        text = message.dateGroupHeader,
                        color = NavyBluePrimary,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 18.dp, vertical = 6.dp)
                    )
                }
            }
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = if (message.isSentByMe) Arrangement.End else Arrangement.Start,
            verticalAlignment = Alignment.Top
        ) {
            if (!message.isSentByMe) {
                Surface(
                    modifier = Modifier
                        .size(34.dp)
                        .padding(top = 2.dp),
                    shape = CircleShape,
                    color = NavyBluePrimary
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text(
                            text = message.senderName.take(1).uppercase().ifEmpty { "?" },
                            color = Color.White,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
                Spacer(modifier = Modifier.width(8.dp))
            }

            Card(
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = MessageBubbleGrey),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                modifier = Modifier.padding(bottom = 2.dp)
            ) {
                Column(
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
                ) {
                    Text(
                        text = message.message,
                        color = Color.Black,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Normal,
                        lineHeight = 20.sp
                    )

                    Spacer(modifier = Modifier.height(3.dp))

                    Text(
                        text = message.timeFormatted,
                        color = Color(0xFF555555),
                        fontSize = 11.sp
                    )
                }
            }
        }
    }
}

@Composable
fun TypingIndicatorBubble(modifier: Modifier = Modifier) {
    val transition = rememberInfiniteTransition(label = "typing")
    val dot1Offset by transition.animateFloat(
        initialValue = 0f,
        targetValue = -6f,
        animationSpec = infiniteRepeatable(
            animation = tween(400, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "dot1"
    )
    val dot2Offset by transition.animateFloat(
        initialValue = 0f,
        targetValue = -6f,
        animationSpec = infiniteRepeatable(
            animation = tween(400, delayMillis = 150, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "dot2"
    )
    val dot3Offset by transition.animateFloat(
        initialValue = 0f,
        targetValue = -6f,
        animationSpec = infiniteRepeatable(
            animation = tween(400, delayMillis = 300, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "dot3"
    )

    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 14.dp, vertical = 4.dp),
        horizontalArrangement = Arrangement.Start,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Card(
            shape = RoundedCornerShape(14.dp),
            colors = CardDefaults.cardColors(containerColor = MessageBubbleGrey),
            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                horizontalArrangement = Arrangement.spacedBy(4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .offset(y = dot1Offset.dp)
                        .background(NavyBluePrimary, CircleShape)
                )
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .offset(y = dot2Offset.dp)
                        .background(NavyBluePrimary, CircleShape)
                )
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .offset(y = dot3Offset.dp)
                        .background(NavyBluePrimary, CircleShape)
                )
            }
        }
    }
}

@Composable
fun ChatSearchBox(
    query: String,
    onQueryChanged: (String) -> Unit,
    onClose: () -> Unit,
    placeholder: String = "Search in chat...",
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 10.dp, vertical = 4.dp),
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFF9FAFB)),
        border = androidx.compose.foundation.BorderStroke(1.dp, BorderLightGrey),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 14.dp, end = 6.dp, top = 6.dp, bottom = 6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            BasicTextField(
                value = query,
                onValueChange = onQueryChanged,
                modifier = Modifier.weight(1f),
                textStyle = TextStyle(
                    color = Color.Black,
                    fontSize = 15.sp
                ),
                cursorBrush = SolidColor(NavyBluePrimary),
                singleLine = true,
                decorationBox = { innerTextField ->
                    if (query.isEmpty()) {
                        Text(
                            text = placeholder,
                            color = TextMutedGrey,
                            fontSize = 14.sp
                        )
                    }
                    innerTextField()
                }
            )

            IconButton(
                onClick = onClose,
                modifier = Modifier.size(32.dp)
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.simple_cancel),
                    contentDescription = "Close search",
                    tint = Color(0xFF333333),
                    modifier = Modifier.size(16.dp)
                )
            }
        }
    }
}

@Composable
fun ChatHeaderBar(
    contact: ConversationItem,
    isOnline: Boolean,
    onBackClick: () -> Unit,
    onToggleSearch: () -> Unit,
    onVideoCallClick: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 10.dp, vertical = 6.dp),
        shape = RoundedCornerShape(10.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = androidx.compose.foundation.BorderStroke(1.dp, BorderLightGrey),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = onBackClick,
                modifier = Modifier.size(36.dp)
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.back_arrow_black),
                    contentDescription = "Back",
                    tint = Color.Black,
                    modifier = Modifier.size(20.dp)
                )
            }

            Spacer(modifier = Modifier.width(4.dp))

            // Avatar + presence dot
            Box(
                modifier = Modifier.size(42.dp),
                contentAlignment = Alignment.Center
            ) {
                Surface(
                    modifier = Modifier.size(40.dp),
                    shape = CircleShape,
                    color = NavyBluePrimary
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text(
                            text = contact.avatarInitial,
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp
                        )
                    }
                }

                Box(
                    modifier = Modifier
                        .size(11.dp)
                        .align(Alignment.BottomEnd)
                        .background(
                            if (isOnline) Color(0xFF2E7D32) else Color(0xFFE53935),
                            CircleShape
                        )
                        .border(1.5.dp, Color.White, CircleShape)
                )
            }

            Spacer(modifier = Modifier.width(10.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = contact.displayName,
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp,
                    color = Color.Black,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = if (isOnline) "Online" else "Offline",
                    fontSize = 12.sp,
                    color = TextMutedGrey
                )
            }

            IconButton(
                onClick = onToggleSearch,
                modifier = Modifier.size(36.dp)
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.green_search),
                    contentDescription = "Search in chat",
                    tint = NavyBluePrimary,
                    modifier = Modifier.size(22.dp)
                )
            }
        }
    }
}

@Composable
fun ChatComposerBar(
    text: String,
    onTextChanged: (String) -> Unit,
    onSendClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        color = Color.White,
        shadowElevation = 4.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .weight(1f)
                    .background(Color.White, RoundedCornerShape(24.dp))
                    .border(1.dp, Color(0xFFD1D5DB), RoundedCornerShape(24.dp))
                    .padding(horizontal = 16.dp, vertical = 10.dp)
            ) {
                BasicTextField(
                    value = text,
                    onValueChange = onTextChanged,
                    textStyle = TextStyle(color = Color.Black, fontSize = 15.sp),
                    cursorBrush = SolidColor(NavyBluePrimary),
                    maxLines = 4,
                    decorationBox = { innerTextField ->
                        if (text.isEmpty()) {
                            Text(
                                text = "Type a message",
                                color = TextMutedGrey,
                                fontSize = 14.sp
                            )
                        }
                        innerTextField()
                    }
                )
            }

            Spacer(modifier = Modifier.width(10.dp))

            val isSendEnabled = text.trim().isNotEmpty()
            IconButton(
                onClick = onSendClick,
                enabled = isSendEnabled,
                modifier = Modifier.size(44.dp)
            ) {
                Image(
                    painter = painterResource(id = R.drawable.send_btn),
                    contentDescription = "Send",
                    alpha = if (isSendEnabled) 1f else 0.5f,
                    modifier = Modifier.size(42.dp)
                )
            }
        }
    }
}

@Composable
fun MessagesEmptyState(
    message: String = "No Messages Yet!",
    subtitle: String = "Start chatting with your contacts and enhance your communication now.",
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Image(
            painter = painterResource(id = R.drawable.chat_empty),
            contentDescription = "Empty",
            modifier = Modifier.size(130.dp)
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = message,
            color = Color.Black,
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = subtitle,
            color = Color(0xFF6C757D),
            fontSize = 14.sp,
            textAlign = TextAlign.Center
        )
    }
}
