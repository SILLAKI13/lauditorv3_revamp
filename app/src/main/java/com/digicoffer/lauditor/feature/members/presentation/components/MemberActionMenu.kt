package com.digicoffer.lauditor.feature.members.presentation.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.digicoffer.lauditor.CommonFiles.GlobalFiles.Constants
import com.digicoffer.lauditor.Members.MembersModel
import com.digicoffer.lauditor.R

@Composable
fun MemberActionMenu(
    member: MembersModel,
    onEditClick: () -> Unit,
    onUpdateGroupAccessClick: () -> Unit,
    onResetPasswordClick: () -> Unit,
    onDeleteClick: () -> Unit,
    onUpgradeClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val isGroupHead = Constants.ROLE == "GH"

    Card(
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 16.dp),
        modifier = modifier.width(180.dp)
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            // Edit Member Info
            Text(
                text = "Edit Member Info",
                fontSize = 15.sp,
                fontFamily = FontFamily(Font(R.font.gill_sans_regular)),
                color = Color.Black,
                textAlign = TextAlign.Start,
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onEditClick() }
                    .padding(horizontal = 16.dp, vertical = 12.dp)
            )

            // Update Group Access (Administrators only)
            if (!isGroupHead) {
                HorizontalDivider(thickness = 0.5.dp, color = Color(0xFFE0E0E0))
                Text(
                    text = "Update Group Access",
                    fontSize = 15.sp,
                    fontFamily = FontFamily(Font(R.font.gill_sans_regular)),
                    color = Color.Black,
                    textAlign = TextAlign.Start,
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onUpdateGroupAccessClick() }
                        .padding(horizontal = 16.dp, vertical = 12.dp)
                )
            }

            // Reset Password
            HorizontalDivider(thickness = 0.5.dp, color = Color(0xFFE0E0E0))
            Text(
                text = "Reset Password",
                fontSize = 15.sp,
                fontFamily = FontFamily(Font(R.font.gill_sans_regular)),
                color = Color.Black,
                textAlign = TextAlign.Start,
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onResetPasswordClick() }
                    .padding(horizontal = 16.dp, vertical = 12.dp)
            )

            // Delete Member
            HorizontalDivider(thickness = 0.5.dp, color = Color(0xFFE0E0E0))
            Text(
                text = "Delete Member",
                fontSize = 15.sp,
                fontFamily = FontFamily(Font(R.font.gill_sans_regular)),
                color = Color.Black,
                textAlign = TextAlign.Start,
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onDeleteClick() }
                    .padding(horizontal = 16.dp, vertical = 12.dp)
            )

            // Upgrade as Practice Partner
            HorizontalDivider(thickness = 0.5.dp, color = Color(0xFFE0E0E0))
            Text(
                text = "Upgrade as Practice Partner",
                fontSize = 15.sp,
                fontFamily = FontFamily(Font(R.font.gill_sans_regular)),
                color = Color.Black,
                textAlign = TextAlign.Start,
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onUpgradeClick() }
                    .padding(horizontal = 16.dp, vertical = 12.dp)
            )
        }
    }
}
