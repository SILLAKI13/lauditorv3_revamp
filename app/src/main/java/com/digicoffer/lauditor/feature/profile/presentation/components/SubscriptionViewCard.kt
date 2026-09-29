package com.digicoffer.lauditor.feature.profile.presentation.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.digicoffer.lauditor.CommonFiles.GlobalFiles.Constants
import com.digicoffer.lauditor.FirmProfile.FirmProfileModel
import com.digicoffer.lauditor.R
import com.digicoffer.lauditor.core.ui.common.cards.AppCard
import com.digicoffer.lauditor.core.ui.common.foundation.AppSpacer
import com.digicoffer.lauditor.core.ui.common.foundation.AppText
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone

private val GillSans = FontFamily(Font(R.font.gill_sans))
private val GillSansBold = FontFamily(Font(R.font.gill_sans_bold))

@Composable
fun SubscriptionViewCard(
    firmProfileModel: FirmProfileModel?,
    hasBankAccount: Boolean = false,
    onDeleteAccountClick: () -> Unit = {},
    onBankAccountClick: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 14.dp, vertical = 8.dp)
    ) {
        // ── Settlement Details: Bank Account Details Card ─────────────────
        val cardBgColor = if (hasBankAccount) Color(0xFFE3F7E9) else Color(0xFFE3F0FF)
        val iconTint = if (hasBankAccount) Color(0xFF2E7D32) else Color(0xFF004D87)
        val titleColor = if (hasBankAccount) Color(0xFF1A1A2E) else Color(0xFF004D87)
        val titleText = if (hasBankAccount) "Bank Account Details" else "Add your bank account details"
        val descText = if (hasBankAccount) {
            "Your bank account has been added successfully and will be used for payment settlements."
        } else {
            "Add your bank account details to receive secure payment settlements and faster payouts. Your banking information is securely encrypted and used only for payment processing."
        }
        val secureColor = if (hasBankAccount) Color(0xFF2E7D32) else Color(0xFF004D87)
        val buttonText = if (hasBankAccount) "View / Manage Bank Details" else "Add Bank Account Details"
        val buttonBgColor = if (hasBankAccount) Color(0xFF2AAD66) else Color(0xFF004D87)

        AppCard(
            modifier = Modifier.fillMaxWidth(),
            elevation = 2.dp,
            backgroundColor = cardBgColor,
            shape = RoundedCornerShape(10.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(14.dp),
                verticalAlignment = Alignment.Top
            ) {
                Image(
                    painter = painterResource(id = R.drawable.ic_bank),
                    contentDescription = "Bank Icon",
                    colorFilter = ColorFilter.tint(iconTint),
                    modifier = Modifier
                        .size(34.dp)
                        .padding(top = 2.dp)
                )

                AppSpacer(width = 12.dp)

                Column(modifier = Modifier.weight(1f)) {
                    AppText(
                        text = titleText,
                        style = TextStyle(
                            fontSize = 15.sp,
                            fontFamily = GillSansBold,
                            color = titleColor
                        )
                    )

                    AppSpacer(height = 4.dp)

                    AppText(
                        text = descText,
                        style = TextStyle(
                            fontSize = 12.sp,
                            fontFamily = GillSans,
                            color = Color(0xFF585858),
                            lineHeight = 16.sp
                        )
                    )

                    AppSpacer(height = 6.dp)

                    AppText(
                        text = "Your details are 100% secure",
                        style = TextStyle(
                            fontSize = 12.sp,
                            fontFamily = GillSansBold,
                            color = secureColor
                        )
                    )

                    AppSpacer(height = 10.dp)

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(38.dp)
                            .background(buttonBgColor, RoundedCornerShape(6.dp))
                            .clickable { onBankAccountClick() },
                        contentAlignment = Alignment.Center
                    ) {
                        AppText(
                            text = buttonText,
                            style = TextStyle(
                                fontSize = 13.sp,
                                fontFamily = GillSansBold,
                                color = Color.White
                            )
                        )
                    }
                }
            }
        }

        AppSpacer(height = 20.dp)
    }
}


