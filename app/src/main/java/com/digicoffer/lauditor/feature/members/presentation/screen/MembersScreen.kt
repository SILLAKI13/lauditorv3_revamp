package com.digicoffer.lauditor.feature.members.presentation.screen

import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.digicoffer.lauditor.feature.notifications.presentation.components.NotificationsSearchBar
import com.digicoffer.lauditor.CommonFiles.GlobalFiles.AndroidUtils
import com.digicoffer.lauditor.CommonFiles.GlobalFiles.Constants
import com.digicoffer.lauditor.CommonFiles.GlobalFiles.NewModel
import com.digicoffer.lauditor.Groups.Models.ViewGroupModel
import com.digicoffer.lauditor.Members.MembersModel
import com.digicoffer.lauditor.R
import com.digicoffer.lauditor.core.ui.common.feedback.AppLoader
import com.digicoffer.lauditor.feature.members.presentation.components.GroupAssignmentCard
import com.digicoffer.lauditor.feature.members.presentation.components.MemberCardItem
import com.digicoffer.lauditor.feature.members.presentation.components.MemberConfirmationDialog
import com.digicoffer.lauditor.feature.members.presentation.components.MemberFormCard
import com.digicoffer.lauditor.feature.members.presentation.components.MembersAlertDialog
import com.digicoffer.lauditor.feature.members.presentation.state.MembersUiEvent
import com.digicoffer.lauditor.feature.members.presentation.viewmodel.MembersViewModel

enum class MembersScreenMode {
    LISTING,
    CREATION,
    EDITING,
    GROUP_ASSIGNMENT
}

@Composable
fun HeaderActionButton(
    text: String,
    iconResId: Int,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val activeBlue = Color(0xFF004D87)
    Card(
        shape = RoundedCornerShape(10.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        border = BorderStroke(0.5.dp, Color(0xFFCCCCCC)),
        modifier = modifier.clickable { onClick() }
    ) {
        Row(
            modifier = Modifier.padding(start = 4.dp, end = 10.dp, top = 4.dp, bottom = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(30.dp)
                    .background(activeBlue, shape = androidx.compose.foundation.shape.CircleShape),
                contentAlignment = Alignment.Center
            ) {
                if (iconResId == 0) {
                    Text(
                        text = "+",
                        color = Color.White,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                        modifier = Modifier.padding(bottom = 2.dp)
                    )
                } else {
                    Image(
                        painter = painterResource(id = iconResId),
                        contentDescription = text,
                        modifier = Modifier.size(14.dp),
                        colorFilter = androidx.compose.ui.graphics.ColorFilter.tint(Color.White)
                    )
                }
            }
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = text,
                color = activeBlue,
                fontSize = 13.sp,
                fontWeight = FontWeight.SemiBold,
                fontFamily = FontFamily(Font(R.font.gill_sans_regular))
            )
        }
    }
}

@Composable
fun MembersScreen(
    viewModel: MembersViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val state by viewModel.uiState.collectAsState()

    var screenMode by remember { mutableStateOf(MembersScreenMode.LISTING) }
    var searchQuery by remember { mutableStateOf("") }
    
    // Selection state trackers
    var selectedMemberForEdit by remember { mutableStateOf<MembersModel?>(null) }
    var selectedGroupIdsForForm by remember { mutableStateOf<List<String>>(emptyList()) }
    var isUgaMode by remember { mutableStateOf(false) } // Update Group Access mode
    var selectedMemberForUga by remember { mutableStateOf<MembersModel?>(null) }

    // Hoisted form state variables
    var formName by remember { mutableStateOf("") }
    var formDesignation by remember { mutableStateOf("") }
    var formCurrency by remember { mutableStateOf("USDollar(USD)") }
    var formRate by remember { mutableStateOf("") }
    var formEmail by remember { mutableStateOf("") }
    var formConfirmEmail by remember { mutableStateOf("") }
    var isAssignGroupsExpanded by remember { mutableStateOf(false) }

    // Dialog state trackers
    var confirmDialogMessage by remember { mutableStateOf<String?>(null) }
    var pendingConfirmAction by remember { mutableStateOf<(() -> Unit)?>(null) }
    var infoAlertMessage by remember { mutableStateOf<String?>(null) }
    var infoAlertTitle by remember { mutableStateOf("Alert !") }

    val activeBlue = Color(0xFF004D87)
    val lightBlueBg = Color(0xFFE6F0FA)
    val inputBorderColor = Color(0xFFCCCCCC)

    val mainActivityViewModel = remember {
        try {
            androidx.lifecycle.ViewModelProvider(context as androidx.fragment.app.FragmentActivity).get(NewModel::class.java)
        } catch (e: Exception) {
            null
        }
    }

    LaunchedEffect(screenMode, selectedMemberForEdit) {
        val titleText = when (screenMode) {
            MembersScreenMode.LISTING -> "View Members"
            MembersScreenMode.CREATION -> "Create Members"
            MembersScreenMode.EDITING -> "Edit Member Info"
            MembersScreenMode.GROUP_ASSIGNMENT -> "Assign Group(s)"
        }
        (context as? android.app.Activity)?.let { activity ->
            activity.title = titleText
            mainActivityViewModel?.setData(titleText)
        }

        if (screenMode == MembersScreenMode.CREATION) {
            formName = ""
            formDesignation = ""
            formRate = ""
            formCurrency = "USDollar(USD)"
            formEmail = ""
            formConfirmEmail = ""
            selectedGroupIdsForForm = emptyList()
            isAssignGroupsExpanded = false
        } else if (screenMode == MembersScreenMode.EDITING) {
            val member = selectedMemberForEdit
            if (member != null) {
                formName = member.name.orEmpty()
                formDesignation = member.designation.orEmpty()
                formRate = member.defaultRate.orEmpty()
                formCurrency = member.currency.orEmpty()
                formEmail = member.email.orEmpty()
                formConfirmEmail = member.email.orEmpty()
                isAssignGroupsExpanded = false
            }
        }
    }

    // Load initial members list on start
    LaunchedEffect(Unit) {
        viewModel.onEvent(MembersUiEvent.LoadMembers)
        viewModel.onEvent(MembersUiEvent.LoadGroups)
    }

    // Display toast/alerts from viewmodel state (Uses custom Compose MembersAlertDialog)
    LaunchedEffect(state.toastMessage) {
        state.toastMessage?.let {
            infoAlertTitle = if (it.contains("success", ignoreCase = true) || 
                                it.contains("successfully", ignoreCase = true) || 
                                it.contains("sent", ignoreCase = true) ||
                                it.contains("created", ignoreCase = true) ||
                                it.contains("updated", ignoreCase = true) ||
                                it.contains("deleted", ignoreCase = true) ||
                                it.contains("removed", ignoreCase = true) ||
                                it.contains("upgraded", ignoreCase = true) ||
                                it.contains("converted", ignoreCase = true)) {
                "Success"
            } else {
                "Alert !"
            }
            infoAlertMessage = it
            viewModel.onEvent(MembersUiEvent.DismissToast)
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(lightBlueBg)
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            
            // Sub-header Row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                val subHeaderTitle = when (screenMode) {
                    MembersScreenMode.LISTING -> "List Of Members"
                    MembersScreenMode.CREATION -> "Create Members"
                    MembersScreenMode.EDITING -> "View Members"
                    MembersScreenMode.GROUP_ASSIGNMENT -> "Assign Group(s)"
                }

                Text(
                    text = subHeaderTitle,
                    color = activeBlue,
                    fontWeight = FontWeight.Bold,
                    fontSize = 20.sp,
                    fontFamily = FontFamily(Font(R.font.gill_sans_regular))
                )

                // Sub-header action controls
                when (screenMode) {
                    MembersScreenMode.LISTING -> {
                        HeaderActionButton(
                            text = "Create Members",
                            iconResId = 0, // draws '+'
                            onClick = {
                                if (!Constants.is_active) {
                                    (context as? android.app.Activity)?.let {
                                        AndroidUtils.showRenewalPopup(it)
                                    }
                                } else {
                                    selectedGroupIdsForForm = emptyList()
                                    screenMode = MembersScreenMode.CREATION
                                }
                            }
                        )
                    }
                    MembersScreenMode.CREATION, MembersScreenMode.EDITING, MembersScreenMode.GROUP_ASSIGNMENT -> {
                        HeaderActionButton(
                            text = "View Members",
                            iconResId = R.drawable.eye_icon,
                            onClick = {
                                screenMode = MembersScreenMode.LISTING
                                isUgaMode = false
                            }
                        )
                    }
                }
            }

            // Body layouts based on state mode
            when (screenMode) {
                MembersScreenMode.LISTING -> {
                    NotificationsSearchBar(
                        query = searchQuery,
                        onQueryChange = { searchQuery = it },
                        placeholder = "Search Team Members",
                        modifier = Modifier.padding(horizontal = 10.dp)
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    // License limits banner (Aligned left matching request)
                    Text(
                        text = "Number of Licenses : ${state.licenseCount} out of ${state.licenseTotal}",
                        color = activeBlue,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 15.sp,
                        fontFamily = FontFamily(Font(R.font.gill_sans_regular)),
                        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
                        textAlign = TextAlign.Start
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    // Filter list items locally
                    val filteredList = state.membersList.filter {
                        val name = it.name.orEmpty()
                        val email = it.email.orEmpty()
                        val designation = it.designation.orEmpty()
                        name.contains(searchQuery, ignoreCase = true) ||
                                email.contains(searchQuery, ignoreCase = true) ||
                                designation.contains(searchQuery, ignoreCase = true)
                    }

                    // Main members list lazy view
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f)
                    ) {
                        items(filteredList) { member ->
                            MemberCardItem(
                                member = member,
                                onEditClick = {
                                    selectedMemberForEdit = member
                                    screenMode = MembersScreenMode.EDITING
                                },
                                onUpdateGroupAccessClick = {
                                    isUgaMode = true
                                    selectedMemberForUga = member
                                    screenMode = MembersScreenMode.GROUP_ASSIGNMENT
                                },
                                onResetPasswordClick = {
                                    confirmDialogMessage = context.getString(R.string.reset_password_tm) + member.name + "?"
                                    pendingConfirmAction = {
                                        viewModel.onEvent(MembersUiEvent.ResetPassword(member.id ?: "") {
                                            confirmDialogMessage = null
                                        })
                                    }
                                },
                                onDeleteClick = {
                                    confirmDialogMessage = context.getString(R.string.delete_team_member) + member.name + " ?"
                                    pendingConfirmAction = {
                                        viewModel.onEvent(MembersUiEvent.DeleteMember(member.id ?: "") {
                                            confirmDialogMessage = null
                                        })
                                    }
                                },
                                onUpgradeClick = {
                                    confirmDialogMessage = context.getString(R.string.upgrade_members) + " " + member.name + " " + "Practice Partner and provide Super User access ?"
                                    pendingConfirmAction = {
                                        viewModel.onEvent(MembersUiEvent.UpgradePracticePartner(member.id ?: "") {
                                            confirmDialogMessage = null
                                        })
                                    }
                                }
                            )
                        }
                    }
                }

                MembersScreenMode.CREATION -> {
                    val isLimitReached = state.isSubscriptionEnded
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .verticalScroll(rememberScrollState())
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .alpha(if (isLimitReached) 0.5f else 1f)
                        ) {
                            MemberFormCard(
                                name = formName,
                                onNameChange = { formName = it },
                                designation = formDesignation,
                                onDesignationChange = { formDesignation = it },
                                currency = formCurrency,
                                onCurrencyChange = { formCurrency = it },
                                rate = formRate,
                                onRateChange = { formRate = it },
                                email = formEmail,
                                onEmailChange = { formEmail = it },
                                confirmEmail = formConfirmEmail,
                                onConfirmEmailChange = { formConfirmEmail = it },
                                showButtons = !isAssignGroupsExpanded,
                                onCancelClick = { screenMode = MembersScreenMode.LISTING },
                                onSaveClick = {
                                    if (!isLimitReached) {
                                        viewModel.onEvent(
                                            MembersUiEvent.CreateMember(
                                                name = formName,
                                                designation = formDesignation,
                                                defaultRate = formRate,
                                                currency = formCurrency,
                                                email = formEmail,
                                                emailConfirm = formConfirmEmail,
                                                groups = selectedGroupIdsForForm,
                                                onSuccess = { screenMode = MembersScreenMode.LISTING }
                                            )
                                        )
                                    }
                                },
                                onAssignGroupsClick = {
                                    isAssignGroupsExpanded = !isAssignGroupsExpanded
                                }
                            )

                            if (isAssignGroupsExpanded) {
                                Spacer(modifier = Modifier.height(16.dp))
                                GroupAssignmentCard(
                                    groupsList = state.groupsList,
                                    initialSelectedGroupIds = selectedGroupIdsForForm,
                                    onCancelClick = {
                                        isAssignGroupsExpanded = false
                                    },
                                    onSaveClick = { list ->
                                        selectedGroupIdsForForm = list
                                        if (!isLimitReached) {
                                            viewModel.onEvent(
                                                MembersUiEvent.CreateMember(
                                                    name = formName,
                                                    designation = formDesignation,
                                                    defaultRate = formRate,
                                                    currency = formCurrency,
                                                    email = formEmail,
                                                    emailConfirm = formConfirmEmail,
                                                    groups = list,
                                                    onSuccess = { screenMode = MembersScreenMode.LISTING }
                                                )
                                            )
                                        }
                                    }
                                )
                            }
                        }

                        if (isLimitReached) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(20.dp)
                                    .background(Color.White.copy(alpha = 0.9f), shape = RoundedCornerShape(8.dp))
                                    .padding(16.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "Your present subscription does not allow creation of more users",
                                    color = Color.Red,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 16.sp,
                                    fontFamily = FontFamily(Font(R.font.gill_sans_regular)),
                                    textAlign = TextAlign.Center
                                )
                            }
                        }
                    }
                }

                MembersScreenMode.EDITING -> {
                    val member = selectedMemberForEdit
                    if (member != null) {
                        val initialGroupIds = remember(member) {
                            val ids = ArrayList<String>()
                            val groups = member.groups
                            if (groups != null) {
                                for (i in 0 until groups.length()) {
                                    ids.add(groups.getJSONObject(i).optString("id"))
                                }
                            }
                            ids
                        }

                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .verticalScroll(rememberScrollState())
                        ) {
                            Column(modifier = Modifier.fillMaxWidth()) {
                                MemberFormCard(
                                    name = formName,
                                    onNameChange = { formName = it },
                                    designation = formDesignation,
                                    onDesignationChange = { formDesignation = it },
                                    currency = formCurrency,
                                    onCurrencyChange = { formCurrency = it },
                                    rate = formRate,
                                    onRateChange = { formRate = it },
                                    email = formEmail,
                                    onEmailChange = { formEmail = it },
                                    confirmEmail = formConfirmEmail,
                                    onConfirmEmailChange = { formConfirmEmail = it },
                                    showButtons = !isAssignGroupsExpanded,
                                    onCancelClick = { screenMode = MembersScreenMode.LISTING },
                                    onSaveClick = {
                                        viewModel.onEvent(
                                            MembersUiEvent.UpdateMember(
                                                id = member.id ?: "",
                                                name = formName,
                                                designation = formDesignation,
                                                defaultRate = formRate,
                                                currency = formCurrency,
                                                email = formEmail,
                                                emailConfirm = formConfirmEmail,
                                                onSuccess = { screenMode = MembersScreenMode.LISTING }
                                            )
                                        )
                                    },
                                    onAssignGroupsClick = {
                                        isAssignGroupsExpanded = !isAssignGroupsExpanded
                                    }
                                )

                                if (isAssignGroupsExpanded) {
                                    Spacer(modifier = Modifier.height(16.dp))
                                    GroupAssignmentCard(
                                        groupsList = state.groupsList,
                                        initialSelectedGroupIds = initialGroupIds,
                                        onCancelClick = {
                                            isAssignGroupsExpanded = false
                                        },
                                        onSaveClick = { list ->
                                            viewModel.onEvent(
                                                MembersUiEvent.UpdateMember(
                                                    id = member.id ?: "",
                                                    name = formName,
                                                    designation = formDesignation,
                                                    defaultRate = formRate,
                                                    currency = formCurrency,
                                                    email = formEmail,
                                                    emailConfirm = formConfirmEmail,
                                                    onSuccess = {
                                                        viewModel.onEvent(
                                                            MembersUiEvent.UpdateGroupAccess(
                                                                id = member.id ?: "",
                                                                groups = list,
                                                                onSuccess = { screenMode = MembersScreenMode.LISTING }
                                                            )
                                                        )
                                                    }
                                                )
                                            )
                                        }
                                    )
                                }
                            }
                        }
                    }
                }

                MembersScreenMode.GROUP_ASSIGNMENT -> {
                    // Checkbox checklist selection groups card
                    val initialGroupIds = if (isUgaMode) {
                        val member = selectedMemberForUga
                        val ids = ArrayList<String>()
                        val groups = member?.groups
                        if (groups != null) {
                            for (i in 0 until groups.length()) {
                                ids.add(groups.getJSONObject(i).optString("id"))
                            }
                        }
                        ids
                    } else {
                        selectedGroupIdsForForm
                    }

                    GroupAssignmentCard(
                        groupsList = state.groupsList,
                        initialSelectedGroupIds = initialGroupIds,
                        onCancelClick = {
                            screenMode = if (isUgaMode) MembersScreenMode.LISTING else MembersScreenMode.CREATION
                        },
                        onSaveClick = { list ->
                            if (isUgaMode) {
                                val member = selectedMemberForUga
                                if (member != null) {
                                    viewModel.onEvent(
                                        MembersUiEvent.UpdateGroupAccess(
                                            id = member.id ?: "",
                                            groups = list,
                                            onSuccess = { screenMode = MembersScreenMode.LISTING }
                                        )
                                    )
                                }
                            } else {
                                selectedGroupIdsForForm = list
                                screenMode = MembersScreenMode.CREATION
                            }
                        }
                    )
                }
            }
        }

        // Confirmation Prompts Modal Overlay
        confirmDialogMessage?.let { msg ->
            MemberConfirmationDialog(
                message = msg,
                onConfirm = {
                    pendingConfirmAction?.invoke()
                    confirmDialogMessage = null
                },
                onDismiss = {
                    confirmDialogMessage = null
                    pendingConfirmAction = null
                }
            )
        }

        // Custom Alert Dialog Modal Overlay (Replacing all Toasts!)
        infoAlertMessage?.let { msg ->
            MembersAlertDialog(
                title = infoAlertTitle,
                message = msg,
                onConfirm = { infoAlertMessage = null },
                onDismiss = { infoAlertMessage = null }
            )
        }

        // Circular Loading Overlay
        if (state.isLoading) {
            AppLoader()
        }
    }
}
