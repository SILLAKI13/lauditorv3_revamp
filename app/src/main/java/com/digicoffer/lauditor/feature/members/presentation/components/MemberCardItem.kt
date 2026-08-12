package com.digicoffer.lauditor.feature.members.presentation.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.digicoffer.lauditor.Members.MembersModel
import com.digicoffer.lauditor.R
import java.util.Locale

@Composable
fun MemberCardItem(
    member: MembersModel,
    onEditClick: () -> Unit,
    onUpdateGroupAccessClick: () -> Unit,
    onResetPasswordClick: () -> Unit,
    onDeleteClick: () -> Unit,
    onUpgradeClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val activeBlue = Color(0xFF004D87)
    val textGray = Color(0xFF333333)
    var showActionMenu by remember { mutableStateOf(false) }

    val symbol = getCurrencySymbol(member.currency)
    val formattedRate = if (member.defaultRate.isNullOrBlank()) "0" else "$symbol${member.defaultRate}"

    Box(modifier = modifier) {
        Card(
            shape = RoundedCornerShape(8.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 10.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                // Name & Menu icon row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = member.name.orEmpty(),
                        color = activeBlue,
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp,
                        fontFamily = FontFamily(Font(R.font.gill_sans_regular)),
                        modifier = Modifier.weight(1f)
                    )
                    Image(
                        painter = painterResource(id = R.drawable.img_17), // 3-dot spinner icon
                        contentDescription = "Options menu",
                        modifier = Modifier
                            .size(30.dp)
                            .clickable { showActionMenu = !showActionMenu }
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Metadata rows
                Text(
                    text = "Designation : ${member.designation.orEmpty()}",
                    color = textGray,
                    fontSize = 14.sp,
                    fontFamily = FontFamily(Font(R.font.gill_sans_regular))
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Currency : ${member.currency.orEmpty()}",
                    color = textGray,
                    fontSize = 14.sp,
                    fontFamily = FontFamily(Font(R.font.gill_sans_regular))
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Rate : $formattedRate",
                    color = textGray,
                    fontSize = 14.sp,
                    fontFamily = FontFamily(Font(R.font.gill_sans_regular))
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Email : ${member.email.orEmpty()}",
                    color = textGray,
                    fontSize = 14.sp,
                    fontFamily = FontFamily(Font(R.font.gill_sans_regular))
                )
            }
        }

        // Expanded actions menu dropdown overlay
        if (showActionMenu) {
            Box(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(top = 36.dp, end = 8.dp)
            ) {
                MemberActionMenu(
                    member = member,
                    onEditClick = {
                        showActionMenu = false
                        onEditClick()
                    },
                    onUpdateGroupAccessClick = {
                        showActionMenu = false
                        onUpdateGroupAccessClick()
                    },
                    onResetPasswordClick = {
                        showActionMenu = false
                        onResetPasswordClick()
                    },
                    onDeleteClick = {
                        showActionMenu = false
                        onDeleteClick()
                    },
                    onUpgradeClick = {
                        showActionMenu = false
                        onUpgradeClick()
                    }
                )
            }
        }
    }
}

private fun getCurrencySymbol(currencyString: String?): String {
    if (currencyString.isNullOrBlank()) return ""
    val currencySymbols = mapOf(
        "USD" to "$",
        "EUR" to "€",
        "JPY" to "¥",
        "GBP" to "£",
        "AUD" to "A$",
        "CAD" to "C$",
        "CHF" to "CHF",
        "KWD" to "KD",
        "BHD" to "BD",
        "INR" to "₹"
    )
    val start = currencyString.indexOf("(")
    val end = currencyString.indexOf(")")
    if (start != -1 && end != -1 && end > start) {
        val code = currencyString.substring(start + 1, end).uppercase(Locale.getDefault())
        return currencySymbols[code] ?: code
    }
    return currencyString
}
