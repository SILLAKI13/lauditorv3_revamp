package com.digicoffer.lauditor.feature.email.presentation.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.digicoffer.lauditor.R
import com.digicoffer.lauditor.feature.email.data.model.EmailAttachmentItem
import com.digicoffer.lauditor.feature.email.data.model.EmailMessageItem

private val GillSans = FontFamily(Font(R.font.gill_sans))

@Composable
fun EmailCard(
    message: EmailMessageItem,
    onAttachmentClick: (EmailAttachmentItem) -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 10.dp, vertical = 6.dp)
            .shadow(3.dp, RoundedCornerShape(8.dp)),
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp)
        ) {
            // Sender Name
            Text(
                text = message.from.ifEmpty { "Unknown Sender" },
                fontFamily = GillSans,
                fontWeight = FontWeight.Bold,
                fontSize = 17.sp,
                color = Color(0xFF003366),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            Spacer(modifier = Modifier.height(4.dp))

            // Subject
            Text(
                text = message.subject.ifEmpty { "(No Subject)" },
                fontFamily = GillSans,
                fontSize = 14.sp,
                color = Color(0xFF333333),
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )

            // Attachments
            if (message.attachments.isNotEmpty()) {
                Spacer(modifier = Modifier.height(10.dp))
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    message.attachments.forEach { attachment ->
                        AttachmentChip(
                            attachment = attachment,
                            onClick = { onAttachmentClick(attachment) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun AttachmentChip(
    attachment: EmailAttachmentItem,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .border(1.dp, Color(0xFFD1D5DB), RoundedCornerShape(6.dp))
            .background(Color(0xFFF8FAFC), RoundedCornerShape(6.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 10.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        val iconRes = if (attachment.filename.lowercase().endsWith(".pdf")) {
            R.drawable.pdf_icon2
        } else {
            R.drawable.attachment_icons
        }

        Image(
            painter = painterResource(id = iconRes),
            contentDescription = "Attachment",
            modifier = Modifier.size(20.dp)
        )

        Spacer(modifier = Modifier.width(8.dp))

        Text(
            text = attachment.filename,
            fontFamily = GillSans,
            fontSize = 13.sp,
            color = Color(0xFF1E293B),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.weight(1f)
        )
    }
}
