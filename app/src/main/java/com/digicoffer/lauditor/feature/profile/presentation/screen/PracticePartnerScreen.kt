package com.digicoffer.lauditor.feature.profile.presentation.screen

import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.digicoffer.lauditor.R
import com.digicoffer.lauditor.core.ui.common.buttons.AppHeaderButton
import com.digicoffer.lauditor.core.ui.common.dialogs.AppConfirmationDialog
import com.digicoffer.lauditor.core.ui.common.dialogs.AppDialog
import com.digicoffer.lauditor.core.ui.common.feedback.AppLoader
import com.digicoffer.lauditor.core.ui.common.search.AppSearchField
import com.digicoffer.lauditor.feature.profile.presentation.components.PracticePartnerAddCard
import com.digicoffer.lauditor.feature.profile.presentation.components.PracticePartnerCard
import com.digicoffer.lauditor.feature.profile.presentation.state.ProfileUiEvent
import com.digicoffer.lauditor.feature.profile.presentation.viewmodel.ProfileViewModel

private val GillSans = FontFamily(
    Font(R.font.gill_sans)
)

@Composable
fun PracticePartnerScreen(
    viewModel: ProfileViewModel = viewModel(),
    onNavigateBack: () -> Unit = {}
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current

    var searchDraft by remember { mutableStateOf("") }

    BackHandler(enabled = uiState.isAddingOrEditingPracticePartner) {
        viewModel.onEvent(ProfileUiEvent.CancelAddEditPracticePartner)
    }

    LaunchedEffect(Unit) {
        viewModel.onEvent(ProfileUiEvent.SetPracticePartnerMode(true))
    }

    LaunchedEffect(uiState.toastMessage) {
        uiState.toastMessage?.let { msg ->
            Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
            viewModel.onEvent(ProfileUiEvent.DismissToast)
        }
    }

    // Filtered list based on search query
    val filteredList = remember(uiState.practicePartnersList, uiState.partnerSearchQuery) {
        if (uiState.partnerSearchQuery.isBlank()) {
            uiState.practicePartnersList
        } else {
            val q = uiState.partnerSearchQuery.trim().lowercase()
            uiState.practicePartnersList.filter {
                it.name.lowercase().contains(q) ||
                it.designation.lowercase().contains(q) ||
                it.specialist.lowercase().contains(q) ||
                it.email.lowercase().contains(q) ||
                it.phone.contains(q)
            }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFE4F2FF)) // Canonical app background
    ) {
        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            Spacer(modifier = Modifier.height(12.dp))

            // ── Subheader row: Title and Add/View Toggle Button (Relationships Design) ──
            if (!uiState.isAddingOrEditingPracticePartner) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "View Practice Partners",
                        color = Color(0xFF004D87),
                        fontFamily = GillSans,
                        fontWeight = FontWeight.Bold,
                        fontSize = 17.sp,
                        lineHeight = 22.sp,
                        modifier = Modifier.weight(1f).padding(end = 8.dp)
                    )

                    AppHeaderButton(
                        text = "Add Practice Partner",
                        iconRes = R.drawable.simple_plus_icon,
                        iconContentDescription = "Add",
                        onClick = { viewModel.onEvent(ProfileUiEvent.OpenAddPracticePartner) }
                    )
                }

                // Unified Search Bar (Relationships Design)
                AppSearchField(
                    value = searchDraft,
                    onValueChange = {
                        searchDraft = it
                        if (it.isEmpty()) {
                            viewModel.onEvent(ProfileUiEvent.SearchPracticePartner(""))
                        }
                    },
                    onSearchClick = {
                        viewModel.onEvent(ProfileUiEvent.SearchPracticePartner(searchDraft))
                    },
                    onClearClick = {
                        searchDraft = ""
                        viewModel.onEvent(ProfileUiEvent.SearchPracticePartner(""))
                    },
                    onSearchKeyboardAction = {
                        viewModel.onEvent(ProfileUiEvent.SearchPracticePartner(searchDraft))
                    },
                    placeholder = "Search Practice Partner",
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                )
            } else {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = if (uiState.practicePartnerForm.id != null) "Edit Practice Partner" else "Add Practice Partner",
                        color = Color(0xFF004D87),
                        fontFamily = GillSans,
                        fontWeight = FontWeight.Bold,
                        fontSize = 17.sp,
                        lineHeight = 22.sp,
                        modifier = Modifier.weight(1f).padding(end = 8.dp)
                    )

                    AppHeaderButton(
                        text = "View Practice Partners",
                        iconRes = R.drawable.eye_icon,
                        iconContentDescription = "View",
                        onClick = { viewModel.onEvent(ProfileUiEvent.CancelAddEditPracticePartner) }
                    )
                }
                Spacer(modifier = Modifier.height(6.dp))
            }

            // ── Main Body Content ──────────────────────────────────────────
            if (uiState.isAddingOrEditingPracticePartner) {
                PracticePartnerAddCard(
                    formState = uiState.practicePartnerForm,
                    onEvent = { viewModel.onEvent(it) },
                    modifier = Modifier.weight(1f)
                )
            } else {
                if (filteredList.isEmpty() && !uiState.isLoading) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center,
                            modifier = Modifier.padding(16.dp)
                        ) {
                            Image(
                                painter = painterResource(id = R.drawable.empty_relationship),
                                contentDescription = "No practice partners",
                                modifier = Modifier.size(130.dp)
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                text = "No Practice Partners Yet!",
                                fontFamily = GillSans,
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.Black
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "There are no Practice Partners added yet.",
                                fontFamily = GillSans,
                                fontSize = 14.sp,
                                color = Color(0xFF707070),
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f),
                        contentPadding = PaddingValues(bottom = 24.dp)
                    ) {
                        items(
                            items = filteredList,
                            key = { it.id.ifEmpty { it.email } }
                        ) { partner ->
                            PracticePartnerCard(
                                partner = partner,
                                onEditClick = { viewModel.onEvent(ProfileUiEvent.OpenEditPracticePartner(it)) },
                                onDeleteClick = { viewModel.onEvent(ProfileUiEvent.ConfirmDeletePracticePartner(it)) }
                            )
                        }
                    }
                }
            }
        }

        // ── Delete Confirmation Dialog (Relationships Dialog Design) ──────
        uiState.partnerToDelete?.let { partner ->
            val confirmAnnotated = buildAnnotatedString {
                append("Are you sure, Do you want to delete this ")
                withStyle(style = SpanStyle(fontWeight = FontWeight.Bold)) {
                    append(partner.name)
                }
                append(" ?")
            }

            AppConfirmationDialog(
                title = "Confirmation",
                annotatedMessage = confirmAnnotated,
                confirmText = "Yes",
                dismissText = "No",
                onConfirm = { viewModel.onEvent(ProfileUiEvent.DeletePracticePartnerConfirmed) },
                onDismiss = { viewModel.onEvent(ProfileUiEvent.DismissDeletePracticePartnerConfirm) },
                onClose = { viewModel.onEvent(ProfileUiEvent.DismissDeletePracticePartnerConfirm) }
            )
        }

        // ── General Alert Dialog (Canonical AppDialog design) ───────────
        val alertMsg = uiState.alertMessage
        if (!alertMsg.isNullOrBlank()) {
            AppDialog(
                title = if (uiState.alertTitle?.equals("Error", ignoreCase = true) == true) "Alert" else (uiState.alertTitle ?: "Alert"),
                confirmText = "OK",
                onConfirm = { viewModel.onEvent(ProfileUiEvent.DismissDialogs) },
                onDismiss = { viewModel.onEvent(ProfileUiEvent.DismissDialogs) },
                content = {
                    Text(
                        text = alertMsg,
                        color = Color(0xFF333333),
                        fontSize = 15.sp,
                        fontFamily = GillSans,
                        textAlign = TextAlign.Center,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 8.dp)
                    )
                }
            )
        }

        // ── Loading Indicator ─────────────────────────────────────────────
        if (uiState.isLoading) {
            AppLoader()
        }
    }
}
