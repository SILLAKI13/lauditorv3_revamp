package com.digicoffer.lauditor.core.ui.feedback

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.digicoffer.lauditor.R

private val GillSans = FontFamily(Font(R.font.gill_sans))

data class PasswordRequirementItem(
    val text: String,
    val isMet: Boolean
)

@Composable
fun AppPasswordRequirements(
    requirements: List<PasswordRequirementItem>,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
    ) {
        requirements.forEach { rule ->
            if (!rule.isMet) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(
                        start = dimensionResource(id = R.dimen.ten_dp),
                        top = 2.dp,
                        bottom = 2.dp
                    )
                ) {
                    androidx.compose.material3.Text(
                        text = "• ${rule.text}",
                        style = TextStyle(
                            fontFamily = GillSans,
                            fontSize = 13.sp,
                            color = colorResource(id = R.color.grey_color_dark)
                        )
                    )
                }
            }
        }
    }
}

@Preview(showBackground = true, name = "AppPasswordRequirements Preview")
@Composable
fun AppPasswordRequirementsPreview() {
    AppPasswordRequirements(
        requirements = listOf(
            PasswordRequirementItem("Must be 8–15 characters long", false),
            PasswordRequirementItem("Must include uppercase and lowercase letters", false),
            PasswordRequirementItem("Must include a number and a special character", true)
        )
    )
}
