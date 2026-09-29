package com.digicoffer.lauditor.feature.profile.presentation.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.digicoffer.lauditor.CommonFiles.GlobalFiles.Constants
import com.digicoffer.lauditor.FirmProfile.FirmProfileModel
import com.digicoffer.lauditor.R
import com.digicoffer.lauditor.core.ui.common.cards.AppCard
import com.digicoffer.lauditor.core.ui.common.foundation.AppDivider
import com.digicoffer.lauditor.core.ui.common.foundation.AppProfileAvatar
import com.digicoffer.lauditor.core.ui.common.foundation.AppSpacer
import org.json.JSONArray
import java.text.SimpleDateFormat
import java.util.ArrayList
import java.util.Locale

private val GillSans = FontFamily(Font(R.font.gill_sans))
private val GillSansBold = FontFamily(Font(R.font.gill_sans_bold))

@Composable
fun ProfileViewCard(
    firmProfileModel: FirmProfileModel?,
    isMyProfile: Boolean,
    selectedSubTab: String,
    isBannerDismissed: Boolean = false,
    onSubTabSelected: (String) -> Unit = {},
    onEditFirmInfoClick: () -> Unit = {},
    onEditAddInfoClick: () -> Unit = {},
    onPhotoEditClick: () -> Unit = {},
    onDeletePhotoClick: () -> Unit = {},
    onCompleteNowClick: () -> Unit = {},
    onDismissBannerClick: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val profile = firmProfileModel?.data?.profile
    val firm = profile?.firm

    val isSolo = "solo".equals(Constants.CATEGORY, ignoreCase = true)
    val isGHTeamMember = Constants.ROLE.equals("GH", ignoreCase = true) || Constants.ROLE.equals("TM", ignoreCase = true)

    val displayName = if (isMyProfile || isSolo) {
        profile?.name ?: ""
    } else {
        firm?.fullname ?: profile?.name ?: ""
    }

    val picUrl = if (firmProfileModel != null) {
        if (isMyProfile) {
            profile?.profile_pic_url ?: ""
        } else {
            firm?.profile_pic_url?.takeIf { it.isNotBlank() }
                ?: profile?.profile_pic_url
                ?: ""
        }
    } else {
        ""
    }

    val completionPercentage = if (isMyProfile) {
        val pc = profile?.profile_completion
        pc?.completion_percentage ?: profile?.profile_completion_percentage ?: 0
    } else {
        val pc = firm?.profile_completion
        pc?.completion_percentage ?: firm?.profile_completion_percentage ?: profile?.profile_completion_percentage ?: 0
    }

    val shouldShowBanner = !isBannerDismissed && completionPercentage in 1..99

    val cardTitle = if (isMyProfile) {
        "Profile Information"
    } else {
        "Firm Information"
    }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 6.dp)
    ) {
        // ── 1. Profile Completion Banner ──────────────────────────────────
        if (shouldShowBanner) {
            ProfileCompletionBanner(
                percentage = completionPercentage,
                isFirmProfile = !isMyProfile && !isSolo,
                onCompleteNowClick = onCompleteNowClick,
                onCloseClick = onDismissBannerClick
            )
            AppSpacer(height = 8.dp)
        }

        // ── 2. Profile Information / Firm Information Card ────────────────
        AppCard(
            modifier = Modifier.fillMaxWidth(),
            elevation = 3.dp,
            backgroundColor = Color.White,
            shape = RoundedCornerShape(12.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(14.dp)
            ) {
                // Header with Title & Edit Box
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = cardTitle,
                        fontSize = 17.sp,
                        fontFamily = GillSansBold,
                        color = Color(0xFF004D87)
                    )

                    if (!isGHTeamMember) {
                        Image(
                            painter = painterResource(id = R.drawable.edit_box),
                            contentDescription = "Edit Profile",
                            modifier = Modifier
                                .size(26.dp)
                                .clickable { onEditFirmInfoClick() }
                        )
                    }
                }

                AppSpacer(height = 10.dp)

                // Compact Centered Avatar with Photo Edit & Delete Badges
                Box(
                    modifier = Modifier.fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    Box(
                        modifier = Modifier.size(100.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        AppProfileAvatar(
                            imageUrl = picUrl,
                            name = displayName.ifEmpty { "User" },
                            size = 94.dp,
                            fontSize = 32.sp,
                            fallbackBgColor = Color(0xFF004D87)
                        )

                        // Delete Badge (Bottom Start) - only if photo URL is present
                        if (!isGHTeamMember && picUrl.isNotBlank()) {
                            Box(
                                modifier = Modifier
                                    .align(Alignment.BottomStart)
                                    .size(28.dp)
                                    .clip(CircleShape)
                                    .background(Color(0xFFD32F2F))
                                    .clickable { onDeletePhotoClick() },
                                contentAlignment = Alignment.Center
                            ) {
                                Image(
                                    painter = painterResource(id = R.drawable.delete_white_icon),
                                    contentDescription = "Delete Photo",
                                    modifier = Modifier.size(14.dp)
                                )
                            }
                        }

                        // Edit Badge (Bottom End)
                        if (!isGHTeamMember) {
                            Box(
                                modifier = Modifier
                                    .align(Alignment.BottomEnd)
                                    .size(28.dp)
                                    .clip(CircleShape)
                                    .background(Color.White)
                                    .border(BorderStroke(1.dp, Color(0xFFC0C0C0)), CircleShape)
                                    .clickable { onPhotoEditClick() },
                                contentAlignment = Alignment.Center
                            ) {
                                Image(
                                    painter = painterResource(id = R.drawable.edit_new_icon_),
                                    contentDescription = "Edit Photo",
                                    modifier = Modifier.size(14.dp)
                                )
                            }
                        }
                    }
                }

                AppSpacer(height = 12.dp)

                // Profile Info Rows
                if (isMyProfile) {
                    // Exact My Profile Field Order from Java/XML (Image 1)
                    if (!firm?.fullname.isNullOrBlank()) {
                        ProfileFieldRow(label = "Firm Name", value = firm?.fullname)
                    }
                    ProfileFieldRow(label = "Contact Name", value = profile?.name)
                    ProfileFieldRow(label = "Email", value = profile?.email)
                    val country = firm?.address?.country ?: profile?.nationality ?: "India"
                    ProfileFieldRow(label = "Country", value = country)
                    ProfileFieldRow(label = "Phone Number", value = profile?.mobile)
                    if (!profile?.date_of_birth.isNullOrBlank()) {
                        ProfileFieldRow(label = "Date of Birth", value = profile?.date_of_birth)
                    }
                    if (!profile?.gender.isNullOrBlank()) {
                        ProfileFieldRow(label = "Gender", value = profile?.gender)
                    }
                    ProfileFieldRow(label = "Nationality", value = profile?.nationality ?: "Indian")
                    if (!firm?.website.isNullOrBlank()) {
                        ProfileFieldRow(label = "Website", value = firm?.website)
                    }
                    if (!profile?.bio_description.isNullOrBlank()) {
                        ExpandableBioRow(label = "Bio", text = profile.bio_description ?: "")
                    }
                } else {
                    // Exact Firm Profile Field Order from Java/XML
                    ProfileFieldRow(label = "Firm Name", value = firm?.fullname)
                    ProfileFieldRow(label = "Contact Name", value = firm?.contact_person ?: profile?.name)
                    ProfileFieldRow(label = "Email", value = firm?.email ?: profile?.email)
                    val country = firm?.address?.country ?: profile?.nationality ?: "India"
                    ProfileFieldRow(label = "Country", value = country)
                    ProfileFieldRow(label = "Phone Number", value = firm?.contact_phone ?: profile?.mobile)
                    if (!profile?.nationality.isNullOrBlank()) {
                        ProfileFieldRow(label = "Nationality", value = profile?.nationality)
                    }
                    if (!firm?.website.isNullOrBlank()) {
                        ProfileFieldRow(label = "Website", value = firm?.website)
                    }
                    if (!firm?.firm_description.isNullOrBlank()) {
                        ExpandableBioRow(label = "About the Firm", text = firm.firm_description ?: "")
                    }
                }
            }
        }

        AppSpacer(height = 10.dp)

        // ── 3. Additional Information Card ────────────────────────────────
        AppCard(
            modifier = Modifier.fillMaxWidth(),
            elevation = 3.dp,
            backgroundColor = Color.White,
            shape = RoundedCornerShape(12.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(14.dp)
            ) {
                // Header with Title & Edit Box
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Additional Information",
                        fontSize = 17.sp,
                        fontFamily = GillSansBold,
                        color = Color(0xFF004D87)
                    )

                    if (!isGHTeamMember) {
                        Image(
                            painter = painterResource(id = R.drawable.edit_box),
                            contentDescription = "Edit Additional Information",
                            modifier = Modifier
                                .size(26.dp)
                                .clickable { onEditAddInfoClick() }
                        )
                    }
                }

                AppSpacer(height = 10.dp)

                // Sub-tabs capsule container (Exact match of Image 3 XML TabLayout)
                val tabs = if (isMyProfile) {
                    listOf(
                        "practice_details" to "Practice\nDetails",
                        "courts_cases" to "Courts\n& Cases",
                        "education_awards" to "Education\n& Awards",
                        "availability" to "Set\nAvailability"
                    )
                } else {
                    listOf(
                        "practice_details" to "Practice\nDetails",
                        "education_awards" to "Awards &\nRecognition"
                    )
                }

                ProfileTabPillRow(
                    tabs = tabs,
                    selectedTab = selectedSubTab,
                    onTabSelected = onSubTabSelected
                )

                AppSpacer(height = 10.dp)
                AppDivider(color = Color(0xFFE5E7EB), thickness = 1.dp)
                AppSpacer(height = 10.dp)

                // Sub-Tab Content Rendering
                when (selectedSubTab) {
                    "practice_details" -> {
                        PracticeDetailsSubTab(profile = profile, firm = firm, isMyProfile = isMyProfile)
                    }
                    "courts_cases" -> {
                        CourtsSubTab(profile = profile)
                    }
                    "education_awards" -> {
                        AwardsSubTab(firm = firm, profile = profile, isMyProfile = isMyProfile)
                    }
                    "availability" -> {
                        AvailabilitySubTab(availability = profile?.availability)
                    }
                    else -> {
                        PracticeDetailsSubTab(profile = profile, firm = firm, isMyProfile = isMyProfile)
                    }
                }
            }
        }

        AppSpacer(height = 16.dp)
    }
}

// ── Profile Completion Banner ────────────────────────────────────────────────
@Composable
private fun ProfileCompletionBanner(
    percentage: Int,
    isFirmProfile: Boolean,
    onCompleteNowClick: () -> Unit,
    onCloseClick: () -> Unit
) {
    AppCard(
        modifier = Modifier.fillMaxWidth(),
        elevation = 3.dp,
        backgroundColor = Color.White,
        shape = RoundedCornerShape(10.dp),
        border = BorderStroke(0.5.dp, Color(0xFFE5E7EB))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Circular Percentage Ring
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .border(2.dp, Color(0xFF004D87), CircleShape)
                    .background(Color(0xFFE2F6FF), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "$percentage%",
                    color = Color(0xFF004D87),
                    fontSize = 12.sp,
                    fontFamily = GillSansBold
                )
            }

            AppSpacer(width = 10.dp)

            val messageText = if (isFirmProfile) {
                if (percentage > 50) {
                    "Please complete your Firm profile to stand out and get discovered by more clients"
                } else {
                    "Firm Profile cannot go live if incomplete"
                }
            } else {
                if (percentage > 50) {
                    "Please complete your profile to stand out and get discovered by more clients"
                } else {
                    "Profile cannot go live if incomplete"
                }
            }

            Text(
                text = messageText,
                fontSize = 13.sp,
                fontFamily = GillSans,
                color = Color(0xFF333333),
                lineHeight = 16.sp,
                modifier = Modifier.weight(1f)
            )

            AppSpacer(width = 6.dp)

            Text(
                text = "Complete Now",
                fontSize = 12.sp,
                fontFamily = GillSansBold,
                color = Color(0xFF004D87),
                modifier = Modifier
                    .clickable { onCompleteNowClick() }
                    .padding(horizontal = 4.dp, vertical = 6.dp)
            )

            AppSpacer(width = 4.dp)

            Image(
                painter = painterResource(id = R.drawable.simple_cancel),
                contentDescription = "Dismiss Banner",
                modifier = Modifier
                    .size(18.dp)
                    .clickable { onCloseClick() }
            )
        }
    }
}

// ── Profile Field Row ────────────────────────────────────────────────────────
@Composable
private fun ProfileFieldRow(label: String, value: String?) {
    if (value.isNullOrBlank() || value.equals("null", ignoreCase = true)) return

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
    ) {
        Text(
            text = label,
            fontSize = 13.sp,
            fontFamily = GillSans,
            color = Color(0xFF585858)
        )
        AppSpacer(height = 2.dp)
        Text(
            text = value,
            fontSize = 15.sp,
            fontFamily = GillSans,
            color = Color(0xFF004D87)
        )
    }
}

// ── Expandable Bio Row ───────────────────────────────────────────────────────
@Composable
private fun ExpandableBioRow(label: String, text: String) {
    var isExpanded by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
    ) {
        Text(
            text = label,
            fontSize = 13.sp,
            fontFamily = GillSans,
            color = Color(0xFF585858)
        )
        AppSpacer(height = 2.dp)

        Text(
            text = text,
            fontSize = 14.sp,
            fontFamily = GillSans,
            color = Color(0xFF004D87),
            lineHeight = 18.sp,
            maxLines = if (isExpanded) Int.MAX_VALUE else 3,
            overflow = TextOverflow.Ellipsis
        )

        if (text.length > 80) {
            AppSpacer(height = 4.dp)
            Text(
                text = if (isExpanded) "Read less" else "Read more",
                fontSize = 13.sp,
                fontFamily = GillSansBold,
                color = Color(0xFF004D87),
                textDecoration = TextDecoration.Underline,
                modifier = Modifier
                    .clickable { isExpanded = !isExpanded }
                    .padding(vertical = 2.dp)
            )
        }
    }
}

// ── Practice Details Sub-Tab ─────────────────────────────────────────────────
@Composable
private fun PracticeDetailsSubTab(
    profile: FirmProfileModel.Profile?,
    firm: FirmProfileModel.Firm?,
    isMyProfile: Boolean
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        // Section 1: Professional Information
        Text(
            text = "Professional Information",
            fontSize = 15.sp,
            fontFamily = GillSansBold,
            color = Color(0xFF004D87)
        )
        AppSpacer(height = 8.dp)

        if (isMyProfile) {
            ProfileFieldRow(label = "Bar Council ID", value = profile?.bar_council_id)
            if (profile?.years_of_experience != null && profile.years_of_experience > 0) {
                ProfileFieldRow(label = "Years of Experience", value = "${profile.years_of_experience}")
            }
            if (!firm?.billing_currency.isNullOrBlank()) {
                ProfileFieldRow(label = "Billing Currency", value = firm?.billing_currency)
            }
            val fee = profile?.consultation_fee
            if (fee != null && !fee.amount.isNullOrBlank()) {
                ProfileFieldRow(label = "Consultation fee", value = "${fee.symbol ?: "₹"} ${fee.amount}")
            }
        } else {
            ProfileFieldRow(label = "Registration Id", value = firm?.reg_id)
            if (firm?.years_of_incorporation != null && firm.years_of_incorporation > 0) {
                ProfileFieldRow(label = "Year of Incorporation", value = "${firm.years_of_incorporation}")
            }
            if (!firm?.billing_currency.isNullOrBlank()) {
                ProfileFieldRow(label = "Billing Currency", value = firm?.billing_currency)
            }
        }

        // Practice Areas Chips (2-Column Grid matching XML Image 3)
        val practiceAreasArray = if (isMyProfile) profile?.practice_areas else firm?.practice_areas ?: profile?.practice_areas
        val practiceAreasList = parseJsonArray(practiceAreasArray)

        AppSpacer(height = 6.dp)
        Text(
            text = "Practice Areas",
            fontSize = 13.sp,
            fontFamily = GillSans,
            color = Color(0xFF585858)
        )
        AppSpacer(height = 6.dp)
        if (practiceAreasList.isNotEmpty()) {
            TwoColumnTagGrid(items = practiceAreasList)
        } else {
            Text(
                text = "No practice areas specified",
                fontSize = 13.sp,
                fontFamily = GillSans,
                color = Color(0xFF757575),
                modifier = Modifier.padding(vertical = 4.dp)
            )
        }
        AppSpacer(height = 8.dp)

        // Services Offered Chips (2-Column Grid matching XML Image 3)
        val servicesArray = if (isMyProfile) profile?.services_offered else firm?.services_offered ?: profile?.services_offered
        val servicesList = parseJsonArray(servicesArray)

        AppSpacer(height = 6.dp)
        Text(
            text = "Services Offered",
            fontSize = 13.sp,
            fontFamily = GillSans,
            color = Color(0xFF585858)
        )
        AppSpacer(height = 6.dp)
        if (servicesList.isNotEmpty()) {
            TwoColumnTagGrid(items = servicesList)
        } else {
            Text(
                text = "No services specified",
                fontSize = 13.sp,
                fontFamily = GillSans,
                color = Color(0xFF757575),
                modifier = Modifier.padding(vertical = 4.dp)
            )
        }
        AppSpacer(height = 8.dp)

        // Languages Spoken Chips (2-Column Grid matching XML Image 3)
        val languagesArray = profile?.languages_spoken
        val languagesList = parseJsonArray(languagesArray)

        AppSpacer(height = 6.dp)
        Text(
            text = "Languages Spoken",
            fontSize = 13.sp,
            fontFamily = GillSans,
            color = Color(0xFF585858)
        )
        AppSpacer(height = 6.dp)
        if (languagesList.isNotEmpty()) {
            TwoColumnTagGrid(items = languagesList)
        } else {
            Text(
                text = "No languages specified",
                fontSize = 13.sp,
                fontFamily = GillSans,
                color = Color(0xFF757575),
                modifier = Modifier.padding(vertical = 4.dp)
            )
        }
        AppSpacer(height = 8.dp)

        // Section 2: Address Information
        val regAddress = firm?.address
        val corrAddress = firm?.correspondence_address

        if (regAddress != null || corrAddress != null) {
            AppSpacer(height = 6.dp)
            AppDivider(color = Color(0xFFE5E7EB), thickness = 1.dp)
            AppSpacer(height = 8.dp)

            Text(
                text = "Address Information",
                fontSize = 15.sp,
                fontFamily = GillSansBold,
                color = Color(0xFF004D87)
            )
            AppSpacer(height = 6.dp)

            if (regAddress != null) {
                val fullAddress = listOfNotNull(
                    regAddress.house_flat_no?.takeIf { it.isNotBlank() },
                    regAddress.street?.takeIf { it.isNotBlank() },
                    regAddress.city_town?.takeIf { it.isNotBlank() },
                    regAddress.state?.takeIf { it.isNotBlank() },
                    regAddress.country?.takeIf { it.isNotBlank() },
                    regAddress.zipcode?.takeIf { it.isNotBlank() }
                ).joinToString(", ")

                ProfileFieldRow(label = "Registered Address", value = fullAddress.ifEmpty { "Not specified" })
            }

            if (corrAddress != null) {
                val fullCorrAddress = listOfNotNull(
                    corrAddress.house_flat_no?.takeIf { it.isNotBlank() },
                    corrAddress.street?.takeIf { it.isNotBlank() },
                    corrAddress.city_town?.takeIf { it.isNotBlank() },
                    corrAddress.state?.takeIf { it.isNotBlank() },
                    corrAddress.country?.takeIf { it.isNotBlank() },
                    corrAddress.zipcode?.takeIf { it.isNotBlank() }
                ).joinToString(", ")

                ProfileFieldRow(label = "Correspondence Address", value = fullCorrAddress.ifEmpty { "Not specified" })
            }
        }
    }
}

// ── Courts & Cases Sub-Tab (Exact Parity of Image 1 XML) ─────────────────────
@Composable
private fun CourtsSubTab(profile: FirmProfileModel.Profile?) {
    val courts = profile?.court_enrollments
    val casesList = profile?.cases_handled

    Column(modifier = Modifier.fillMaxWidth()) {
        // Section Header: Court Practice Details
        Text(
            text = "Court Practice Details",
            fontSize = 15.sp,
            fontFamily = GillSans,
            color = Color(0xFF585858)
        )
        AppSpacer(height = 8.dp)

        // Practicing At label
        Text(
            text = "Practicing At",
            fontSize = 14.sp,
            fontFamily = GillSans,
            color = Color(0xFF585858)
        )
        AppSpacer(height = 6.dp)

        // Court Enrollments List
        if (courts.isNullOrEmpty()) {
            Text(
                text = "No court enrollments added",
                fontSize = 13.sp,
                fontFamily = GillSans,
                color = Color(0xFF757575),
                modifier = Modifier.padding(vertical = 4.dp)
            )
        } else {
            courts.forEach { court ->
                val rawName = court.court_name ?: court.court_type ?: "Court"
                val courtTitle = rawName.replace("_", " ").split(" ")
                    .filter { it.isNotBlank() }
                    .joinToString(" ") { it.replaceFirstChar(Char::titlecase) }

                val isSupreme = courtTitle.equals("Supreme Court", ignoreCase = true)
                fun sanitizeLoc(loc: String?): String? {
                    if (loc.isNullOrBlank() || loc.equals("null", ignoreCase = true) || loc.equals("states", ignoreCase = true)) return null
                    val t = loc.trim()
                    if (t.startsWith("{") && t.contains("state")) {
                        try {
                            val obj = org.json.JSONObject(t)
                            val st = obj.optString("state", "")
                            if (st.isNotBlank()) return st
                        } catch (_: Exception) {}
                        return null
                    }
                    return t
                }
                val cleanState = sanitizeLoc(court.state)
                val cleanCity = sanitizeLoc(court.city)
                val finalState = cleanState?.ifEmpty { if (isSupreme) "Delhi" else null } ?: if (isSupreme) "Delhi" else null
                val finalCity = cleanCity?.ifEmpty { if (isSupreme) "New Delhi" else null } ?: if (isSupreme) "New Delhi" else null
                val locationParts = listOfNotNull(finalState, finalCity)
                val locationText = locationParts.joinToString(" • ")

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp)
                        .background(Color(0xFFF9FAFB), RoundedCornerShape(10.dp))
                        .border(0.5.dp, Color(0xFFE5E7EB), RoundedCornerShape(10.dp))
                        .padding(12.dp)
                ) {
                    Column {
                        // Court Type Chip
                        Box(
                            modifier = Modifier
                                .background(Color(0xFFE2F6FF), RoundedCornerShape(16.dp))
                                .padding(horizontal = 14.dp, vertical = 6.dp)
                        ) {
                            Text(
                                text = courtTitle,
                                fontSize = 14.sp,
                                fontFamily = GillSans,
                                color = Color(0xFF004D87)
                            )
                        }

                        if (locationParts.isNotEmpty() && locationText.isNotBlank()) {
                            AppSpacer(height = 8.dp)
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(6.dp)
                                        .background(Color(0xFF004D87), CircleShape)
                                )
                                AppSpacer(width = 8.dp)
                                Text(
                                    text = locationText,
                                    fontSize = 14.sp,
                                    fontFamily = GillSans,
                                    color = Color(0xFF1A1A2E)
                                )
                            }
                        }
                    }
                }
            }
        }

        AppSpacer(height = 10.dp)
        AppDivider(color = Color(0xFFE5E7EB), thickness = 0.8.dp)
        AppSpacer(height = 10.dp)

        // Section Header: Types of Cases Handled
        Text(
            text = "Types of Cases Handled",
            fontSize = 15.sp,
            fontFamily = GillSans,
            color = Color(0xFF585858)
        )
        AppSpacer(height = 6.dp)

        if (casesList.isNullOrEmpty()) {
            Text(
                text = "No cases handled added",
                fontSize = 13.sp,
                fontFamily = GillSans,
                color = Color(0xFF757575),
                modifier = Modifier.padding(vertical = 4.dp)
            )
        } else {
            TwoColumnTagGrid(items = casesList)
        }
    }
}

// ── Education & Awards Sub-Tab (Exact Parity of Image 3 XML) ─────────────────
@Composable
private fun AwardsSubTab(
    firm: FirmProfileModel.Firm?,
    profile: FirmProfileModel.Profile?,
    isMyProfile: Boolean
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        if (isMyProfile) {
            // ── 1. Educational Qualifications ─────────────────────────────
            Text(
                text = "Educational Qualifications",
                fontSize = 14.sp,
                fontFamily = GillSans,
                color = Color(0xFF585858)
            )
            AppSpacer(height = 6.dp)

            val eduList = profile?.education
            if (eduList.isNullOrEmpty()) {
                Text(
                    text = "No educational qualifications added",
                    fontSize = 13.sp,
                    fontFamily = GillSans,
                    color = Color(0xFF757575),
                    modifier = Modifier.padding(vertical = 4.dp)
                )
            } else {
                eduList.forEach { edu ->
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp)
                            .background(Color(0xFFF9FAFB), RoundedCornerShape(10.dp))
                            .border(0.5.dp, Color(0xFFE5E7EB), RoundedCornerShape(10.dp))
                            .padding(12.dp)
                    ) {
                        Column {
                            Text(
                                text = edu.degree ?: "Degree",
                                fontSize = 16.sp,
                                fontFamily = GillSansBold,
                                color = Color(0xFF004D87)
                            )
                            val subParts = listOfNotNull(
                                edu.university?.takeIf { it.isNotBlank() },
                                edu.passing_year.takeIf { it > 0 }?.let { "$it" }
                            )
                            if (subParts.isNotEmpty()) {
                                AppSpacer(height = 4.dp)
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Box(
                                        modifier = Modifier
                                            .size(6.dp)
                                            .background(Color(0xFF004D87), CircleShape)
                                    )
                                    AppSpacer(width = 8.dp)
                                    Text(
                                        text = subParts.joinToString(" • "),
                                        fontSize = 14.sp,
                                        fontFamily = GillSans,
                                        color = Color(0xFF004D87)
                                    )
                                }
                            }
                        }
                    }
                }
            }

            AppSpacer(height = 10.dp)

            // ── 2. Certifications ─────────────────────────────────────────
            Text(
                text = "Certifications",
                fontSize = 14.sp,
                fontFamily = GillSans,
                color = Color(0xFF585858)
            )
            AppSpacer(height = 6.dp)

            val certList = profile?.certifications
            if (certList.isNullOrEmpty()) {
                Text(
                    text = "No certifications added",
                    fontSize = 13.sp,
                    fontFamily = GillSans,
                    color = Color(0xFF757575),
                    modifier = Modifier.padding(vertical = 4.dp)
                )
            } else {
                certList.forEach { cert ->
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp)
                            .background(Color(0xFFF9FAFB), RoundedCornerShape(10.dp))
                            .border(0.5.dp, Color(0xFFE5E7EB), RoundedCornerShape(10.dp))
                            .padding(12.dp)
                    ) {
                        Column {
                            Text(
                                text = cert.certification_name ?: "Certification",
                                fontSize = 16.sp,
                                fontFamily = GillSansBold,
                                color = Color(0xFF004D87)
                            )
                            val subParts = listOfNotNull(
                                cert.issuing_authority?.takeIf { it.isNotBlank() },
                                cert.year_of_issue.takeIf { it > 0 }?.let { "$it" }
                            )
                            if (subParts.isNotEmpty()) {
                                AppSpacer(height = 4.dp)
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Box(
                                        modifier = Modifier
                                            .size(6.dp)
                                            .background(Color(0xFF004D87), CircleShape)
                                    )
                                    AppSpacer(width = 8.dp)
                                    Text(
                                        text = subParts.joinToString(" • "),
                                        fontSize = 14.sp,
                                        fontFamily = GillSans,
                                        color = Color(0xFF004D87)
                                    )
                                }
                            }
                        }
                    }
                }
            }

            AppSpacer(height = 10.dp)

            // ── 3. Awards & Recognitions(Optional) ─────────────────────────
            Text(
                text = "Awards & Recognitions(Optional)",
                fontSize = 14.sp,
                fontFamily = GillSans,
                color = Color(0xFF585858)
            )
            AppSpacer(height = 6.dp)

            val awardList = profile?.awards
            if (awardList.isNullOrEmpty()) {
                Text(
                    text = "No awards added",
                    fontSize = 13.sp,
                    fontFamily = GillSans,
                    color = Color(0xFF757575),
                    modifier = Modifier.padding(vertical = 4.dp)
                )
            } else {
                awardList.forEach { award ->
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp)
                            .background(Color(0xFFF9FAFB), RoundedCornerShape(10.dp))
                            .border(0.5.dp, Color(0xFFE5E7EB), RoundedCornerShape(10.dp))
                            .padding(12.dp)
                    ) {
                        Column {
                            Text(
                                text = award.award_name ?: "Award",
                                fontSize = 16.sp,
                                fontFamily = GillSansBold,
                                color = Color(0xFF004D87)
                            )
                            val subParts = listOfNotNull(
                                award.purpose?.takeIf { it.isNotBlank() },
                                award.year_of_award.takeIf { it > 0 }?.let { "$it" }
                            )
                            if (subParts.isNotEmpty()) {
                                AppSpacer(height = 4.dp)
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Box(
                                        modifier = Modifier
                                            .size(6.dp)
                                            .background(Color(0xFF004D87), CircleShape)
                                    )
                                    AppSpacer(width = 8.dp)
                                    Text(
                                        text = subParts.joinToString(" • "),
                                        fontSize = 14.sp,
                                        fontFamily = GillSans,
                                        color = Color(0xFF004D87)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        } else {
            // Firm Profile Awards
            Text(
                text = "Awards & Recognition",
                fontSize = 15.sp,
                fontFamily = GillSansBold,
                color = Color(0xFF004D87)
            )
            AppSpacer(height = 6.dp)

            val awards = firm?.awards ?: profile?.awards
            if (awards.isNullOrEmpty()) {
                Text(
                    text = "No awards or recognitions added yet.",
                    fontSize = 13.sp,
                    fontFamily = GillSans,
                    color = Color(0xFF757575),
                    modifier = Modifier.padding(vertical = 8.dp)
                )
            } else {
                awards.forEach { award ->
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp)
                            .background(Color(0xFFF9FAFB), RoundedCornerShape(10.dp))
                            .border(0.5.dp, Color(0xFFE5E7EB), RoundedCornerShape(10.dp))
                            .padding(12.dp)
                    ) {
                        Column {
                            Text(
                                text = award.award_name ?: "Award",
                                fontSize = 16.sp,
                                fontFamily = GillSansBold,
                                color = Color(0xFF004D87)
                            )
                            val subParts = listOfNotNull(
                                award.purpose?.takeIf { it.isNotBlank() },
                                award.year_of_award.takeIf { it > 0 }?.let { "$it" }
                            )
                            if (subParts.isNotEmpty()) {
                                AppSpacer(height = 4.dp)
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Box(
                                        modifier = Modifier
                                            .size(6.dp)
                                            .background(Color(0xFF004D87), CircleShape)
                                    )
                                    AppSpacer(width = 8.dp)
                                    Text(
                                        text = subParts.joinToString(" • "),
                                        fontSize = 14.sp,
                                        fontFamily = GillSans,
                                        color = Color(0xFF004D87)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

// ── Availability Sub-Tab (Set Availability Parity) ───────────────────────────
@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun AvailabilitySubTab(availability: FirmProfileModel.Availability?) {
    if (availability == null) {
        Text(
            text = "Availability schedule not set.",
            fontSize = 13.sp,
            fontFamily = GillSans,
            color = Color(0xFF757575),
            modifier = Modifier.padding(vertical = 8.dp)
        )
        return
    }

    var showInfoHint by remember { mutableStateOf(false) }

    Column(modifier = Modifier.fillMaxWidth()) {
        /*
        // Slot Duration & Minimum Booking Hours Row
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Slot Duration Box
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Slot Duration",
                    fontSize = 13.sp,
                    fontFamily = GillSans,
                    color = Color(0xFF585858)
                )
                AppSpacer(height = 6.dp)
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(40.dp)
                        .background(Color(0xFF00B3A7), RoundedCornerShape(6.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "${if (availability.slot_duration > 0) availability.slot_duration else 30} Minutes",
                        fontSize = 13.sp,
                        fontFamily = GillSansBold,
                        color = Color.White
                    )
                }
            }

            // Minimum Booking Hours Box
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Minimum Booking Hours",
                    fontSize = 13.sp,
                    fontFamily = GillSans,
                    color = Color(0xFF585858)
                )
                AppSpacer(height = 6.dp)
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(40.dp)
                        .background(Color(0xFFF0F0F0), RoundedCornerShape(6.dp))
                        .border(0.5.dp, Color(0xFFCCCCCC), RoundedCornerShape(6.dp))
                        .padding(horizontal = 12.dp),
                    contentAlignment = Alignment.CenterStart
                ) {
                    val advHours = if (availability.advance_booking_window_hours > 0) availability.advance_booking_window_hours else 4
                    Text(
                        text = "$advHours ${if (advHours == 1) "Hour" else "Hours"}",
                        fontSize = 13.sp,
                        fontFamily = GillSans,
                        color = Color.Black
                    )
                }
            }
        }

        AppSpacer(height = 14.dp)
        */

        // Weekly Hours Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Image(
                    painter = painterResource(id = R.drawable.timer),
                    contentDescription = "Timer",
                    modifier = Modifier.size(20.dp)
                )
                AppSpacer(width = 6.dp)
                Text(
                    text = "Weekly hours",
                    fontSize = 15.sp,
                    fontFamily = GillSansBold,
                    color = Color(0xFF2079AD)
                )
            }

            Image(
                painter = painterResource(id = R.drawable.iv_info),
                contentDescription = "Info",
                modifier = Modifier
                    .size(20.dp)
                    .clickable { showInfoHint = !showInfoHint }
            )
        }

        if (showInfoHint) {
            AppSpacer(height = 8.dp)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFFEFF6FF), RoundedCornerShape(8.dp))
                    .border(1.dp, Color(0xFFBFDBFE), RoundedCornerShape(8.dp))
                    .padding(12.dp)
            ) {
                Text(
                    text = "How it works: Set your available hours. We will automatically create 30-minute booking slots. Need a break? Click Exclude icon to block specific times (e.g., 12:00-01.00 PM).",
                    fontSize = 12.sp,
                    fontFamily = GillSans,
                    color = Color(0xFF004D87),
                    lineHeight = 16.sp
                )
            }
        }

        AppSpacer(height = 6.dp)
        Text(
            text = "Set when you are typically available for meetings",
            fontSize = 13.sp,
            fontFamily = GillSans,
            color = Color(0xFF585858)
        )

        AppSpacer(height = 10.dp)

        // Weekly Schedule List
        val scheduleList = availability.weekly_schedule
        if (scheduleList.isNullOrEmpty()) {
            Text(
                text = "No weekly schedule configured.",
                fontSize = 13.sp,
                fontFamily = GillSans,
                color = Color(0xFF757575),
                modifier = Modifier.padding(vertical = 6.dp)
            )
        } else {
            scheduleList.forEach { day ->
                val dayLabel = day.date_label?.takeIf { it.isNotBlank() } ?: day.day_name ?: "Day"
                val workSlots = day.work_slots
                val blockedSlots = day.expert_slots
                val workSlot = workSlots?.firstOrNull()
                val isAvailable = workSlot != null && !workSlot.start_time.isNullOrBlank() && !workSlot.end_time.isNullOrBlank()
                val workTimeText = if (isAvailable) {
                    "${convertTo12Hour(workSlot?.start_time)} - ${convertTo12Hour(workSlot?.end_time)}"
                } else {
                    "Not available"
                }
                val availHours = calculateAvailableHours(workSlots, blockedSlots)

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp)
                        .background(Color(0xFFF9FAFB), RoundedCornerShape(8.dp))
                        .border(0.5.dp, Color(0xFFE5E7EB), RoundedCornerShape(8.dp))
                        .padding(10.dp)
                ) {
                    Column {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.Top
                        ) {
                            Text(
                                text = dayLabel,
                                fontSize = 14.sp,
                                fontFamily = GillSansBold,
                                color = Color.Black
                            )

                            Column(horizontalAlignment = Alignment.End) {
                                Text(
                                    text = workTimeText,
                                    fontSize = 14.sp,
                                    fontFamily = GillSans,
                                    color = if (!isAvailable) Color(0xFF888888) else Color(0xFF585858)
                                )

                                if (isAvailable && availHours > 0) {
                                    AppSpacer(height = 4.dp)
                                    Box(
                                        modifier = Modifier
                                            .background(Color(0xFFC8E6C9), RoundedCornerShape(15.dp))
                                            .padding(horizontal = 10.dp, vertical = 3.dp)
                                    ) {
                                        Text(
                                            text = formatHours(availHours),
                                            fontSize = 12.sp,
                                            fontFamily = GillSans,
                                            color = Color(0xFF2E7D32)
                                        )
                                    }
                                }
                            }
                        }

                        // Blocked Slots (Expert Slots)
                        if (!blockedSlots.isNullOrEmpty()) {
                            AppSpacer(height = 6.dp)
                            AppDivider(color = Color(0xFFEEEEEE), thickness = 0.5.dp)
                            AppSpacer(height = 6.dp)

                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Image(
                                    painter = painterResource(id = R.drawable.block),
                                    contentDescription = "Excluded",
                                    modifier = Modifier.size(14.dp)
                                )
                                AppSpacer(width = 4.dp)
                                Text(
                                    text = "Excluded",
                                    fontSize = 13.sp,
                                    fontFamily = GillSansBold,
                                    color = Color(0xFFD32F2F)
                                )
                                AppSpacer(width = 8.dp)

                                FlowRow(
                                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                                    verticalArrangement = Arrangement.spacedBy(4.dp),
                                    modifier = Modifier.weight(1f)
                                ) {
                                    blockedSlots.forEach { slot ->
                                        Box(
                                            modifier = Modifier
                                                .background(Color(0xFFEEEEEE), RoundedCornerShape(4.dp))
                                                .padding(horizontal = 6.dp, vertical = 2.dp)
                                        ) {
                                            Text(
                                                text = "${convertTo12Hour(slot.start_time)} – ${convertTo12Hour(slot.end_time)}",
                                                fontSize = 12.sp,
                                                fontFamily = GillSans,
                                                color = Color(0xFF555555)
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

// ── 2-Column Grid Layout for Tags/Pills (Exact XML Parity Image 3) ────────────
@Composable
private fun TwoColumnTagGrid(items: List<String>) {
    val chunkedItems = items.chunked(2)

    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        chunkedItems.forEach { rowItems ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Item 1 (Left column)
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(46.dp)
                        .background(Color(0xFFE2F6FF), RoundedCornerShape(23.dp))
                        .padding(horizontal = 10.dp, vertical = 4.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = rowItems[0],
                        fontSize = 13.sp,
                        fontFamily = GillSans,
                        color = Color(0xFF004D87),
                        textAlign = TextAlign.Center,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                // Item 2 (Right column) or Empty spacer
                if (rowItems.size > 1) {
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(46.dp)
                            .background(Color(0xFFE2F6FF), RoundedCornerShape(23.dp))
                            .padding(horizontal = 10.dp, vertical = 4.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = rowItems[1],
                            fontSize = 13.sp,
                            fontFamily = GillSans,
                            color = Color(0xFF004D87),
                            textAlign = TextAlign.Center,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                } else {
                    Spacer(modifier = Modifier.weight(1f))
                }
            }
        }
    }
}

private fun convertTo12Hour(timeStr: String?): String {
    if (timeStr.isNullOrBlank()) return ""
    return try {
        val inputFormat = SimpleDateFormat("HH:mm", Locale.getDefault())
        val outputFormat = SimpleDateFormat("hh:mm a", Locale.getDefault())
        val date = inputFormat.parse(timeStr)
        if (date != null) outputFormat.format(date) else timeStr
    } catch (e: Exception) {
        timeStr
    }
}

private fun parseJsonArray(jsonArray: JSONArray?): List<String> {
    if (jsonArray == null) return emptyList()
    val list = ArrayList<String>()
    for (i in 0 until jsonArray.length()) {
        val str = jsonArray.optString(i, "")
        if (str.isNotBlank()) list.add(str)
    }
    return list
}

private fun calculateAvailableHours(
    work: List<FirmProfileModel.WorkSlot>?,
    blocked: List<FirmProfileModel.WorkSlot>?
): Double {
    if (work.isNullOrEmpty()) return 0.0
    var wm = 0.0
    for (w in work) wm += diffMin(w.start_time, w.end_time)
    var bm = 0.0
    if (blocked != null) {
        for (b in blocked) bm += diffMin(b.start_time, b.end_time)
    }
    return Math.max(0.0, (wm - bm) / 60.0)
}

private fun diffMin(s: String?, e: String?): Int {
    if (s.isNullOrBlank() || e.isNullOrBlank()) return 0
    return try {
        val sdf = SimpleDateFormat("HH:mm", Locale.getDefault())
        val start = sdf.parse(s)?.time ?: 0L
        val end = sdf.parse(e)?.time ?: 0L
        ((end - start) / 60000).toInt()
    } catch (ex: Exception) {
        0
    }
}

private fun formatHours(h: Double): String {
    val hh = h.toInt()
    val mm = ((h - hh) * 60).toInt()
    return if (mm == 0) "$hh hours Available" else "${hh}h ${mm}m Available"
}

