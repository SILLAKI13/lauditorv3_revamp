package com.digicoffer.lauditor.feature.email.presentation.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.digicoffer.lauditor.R
import com.digicoffer.lauditor.feature.email.data.model.EmailProvider

private val GillSans = FontFamily(Font(R.font.gill_sans))

@Composable
fun EmailProviderSelector(
    selectedProvider: EmailProvider,
    onProviderSelected: (EmailProvider) -> Unit,
    onContinueClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .padding(10.dp)
            .shadow(4.dp, RoundedCornerShape(8.dp)),
        shape = RoundedCornerShape(8.dp),
        color = Color.White
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp)
        ) {
            Text(
                text = stringResource(id = R.string.choose_a_main_account_provider),
                fontFamily = GillSans,
                fontSize = 17.sp,
                fontWeight = FontWeight.Medium,
                color = Color.Black
            )

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Google Option
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .clickable { onProviderSelected(EmailProvider.GOOGLE) }
                        .padding(end = 24.dp)
                ) {
                    RadioButton(
                        selected = selectedProvider == EmailProvider.GOOGLE,
                        onClick = { onProviderSelected(EmailProvider.GOOGLE) },
                        colors = RadioButtonDefaults.colors(
                            selectedColor = Color(0xFF039BE5),
                            unselectedColor = Color(0xFFB0BEC5)
                        )
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Image(
                        painter = painterResource(id = R.drawable.google),
                        contentDescription = "Google",
                        modifier = Modifier
                            .width(65.dp)
                            .height(30.dp)
                    )
                }

                // Outlook Option
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .clickable { onProviderSelected(EmailProvider.OUTLOOK) }
                ) {
                    RadioButton(
                        selected = selectedProvider == EmailProvider.OUTLOOK,
                        onClick = { onProviderSelected(EmailProvider.OUTLOOK) },
                        colors = RadioButtonDefaults.colors(
                            selectedColor = Color(0xFF039BE5),
                            unselectedColor = Color(0xFFB0BEC5)
                        )
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Image(
                        painter = painterResource(id = R.drawable.outlook),
                        contentDescription = "Outlook",
                        modifier = Modifier
                            .width(75.dp)
                            .height(32.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Button(
                onClick = onContinueClick,
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF004D87)
                ),
                shape = RoundedCornerShape(20.dp),
                modifier = Modifier
                    .align(Alignment.End)
                    .height(38.dp)
            ) {
                Text(
                    text = stringResource(id = R.string.continue_),
                    fontFamily = GillSans,
                    fontSize = 14.sp,
                    color = Color.White
                )
            }
        }
    }
}
