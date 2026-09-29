package com.digicoffer.lauditor.feature.profile.presentation.screen

import android.Manifest
import android.app.Activity
import android.content.ContentValues
import android.content.Context
import android.content.pm.PackageManager
import android.net.Uri
import android.provider.MediaStore
import android.widget.Toast
import com.digicoffer.lauditor.FirmProfile.BankAccountDialog
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import com.digicoffer.lauditor.CommonFiles.GlobalFiles.Constants
import com.digicoffer.lauditor.R
import com.digicoffer.lauditor.core.ui.common.dialogs.AppConfirmationDialog
import com.digicoffer.lauditor.core.ui.common.feedback.AppLoader
import com.digicoffer.lauditor.core.ui.common.foundation.AppSpacer
import com.digicoffer.lauditor.feature.members.presentation.components.MembersAlertDialog
import com.digicoffer.lauditor.feature.profile.presentation.components.*
import com.digicoffer.lauditor.feature.profile.presentation.state.ProfileUiEvent
import com.digicoffer.lauditor.feature.profile.presentation.viewmodel.ProfileViewModel
import java.io.File
import java.io.FileOutputStream

private val GillSans = FontFamily(Font(R.font.gill_sans))
private val GillSansBold = FontFamily(Font(R.font.gill_sans_bold))

@Composable
fun ProfileScreen(
    viewModel: ProfileViewModel = viewModel(),
    onNavigateBack: () -> Unit = {}
) {
    val uiState by viewModel.uiState.collectAsState()
    val scrollState = rememberScrollState()
    val context = LocalContext.current

    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) {
            val file = getFileFromUri(context, uri)
            if (file != null) {
                viewModel.onEvent(ProfileUiEvent.UploadPhoto(file))
            }
        }
    }

    var cameraImageUriString by rememberSaveable { mutableStateOf<String?>(null) }
    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture()
    ) { success: Boolean ->
        if (success && cameraImageUriString != null) {
            val uri = Uri.parse(cameraImageUriString)
            val file = getFileFromUri(context, uri)
            if (file != null) {
                viewModel.onEvent(ProfileUiEvent.UploadPhoto(file))
            }
        }
    }

    fun launchCamera() {
        try {
            val cv = ContentValues().apply {
                put(MediaStore.Images.Media.TITLE, "Profile_" + System.currentTimeMillis())
                put(MediaStore.Images.Media.DESCRIPTION, "Profile Image")
            }
            val uri = context.contentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, cv)
            cameraImageUriString = uri?.toString()
            if (uri != null) {
                cameraLauncher.launch(uri)
            } else {
                Toast.makeText(context, "Cannot access camera storage", Toast.LENGTH_SHORT).show()
            }
        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(context, "Camera error: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }

    val cameraPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        if (isGranted) {
            launchCamera()
        } else {
            Toast.makeText(context, "Camera permission is required to take a photo", Toast.LENGTH_SHORT).show()
        }
    }

    fun handleCameraClick() {
        viewModel.onEvent(ProfileUiEvent.DismissPhotoChooser)
        if (ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
            launchCamera()
        } else {
            cameraPermissionLauncher.launch(Manifest.permission.CAMERA)
        }
    }

    val isSolo = "solo".equals(Constants.CATEGORY, ignoreCase = true)
    val isSU = "SU".equals(Constants.ROLE, ignoreCase = true)
    val isGHTeamMember = Constants.ROLE.equals("GH", ignoreCase = true) || Constants.ROLE.equals("TM", ignoreCase = true)
    val isEditing = uiState.isEditingProfileInfo || uiState.isEditingAdditionalInfo

    // Exact parity with Java FirmProfile.java (lines 119-125):
    // if (Constants.CATEGORY.equals("solo") && Constants.ROLE.equals("SU")) -> VISIBLE
    // else if (Constants.isMyProfileClicked || isGHTeamMember) -> GONE
    // else -> VISIBLE
    val canShowSegmentedControl = when {
        isEditing -> false
        isSolo && isSU -> true
        uiState.isMyProfile || isGHTeamMember -> false
        else -> true
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFE4F2FF)) // Canonical background color (#E4F2FF)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
        ) {
            // ── Segmented Control Tab Row (Profile Info vs Subscription) ───
            if (canShowSegmentedControl) {
                AppSpacer(height = 6.dp)
                Row(
                    modifier = Modifier
                        .wrapContentWidth()
                        .align(Alignment.CenterHorizontally)
                        .padding(horizontal = 12.dp, vertical = 4.dp),
                    horizontalArrangement = Arrangement.Center
                ) {
                    SegmentedTabButton(
                        text = "Profile Info",
                        isSelected = uiState.mainTab == "profile_info",
                        onClick = { viewModel.onEvent(ProfileUiEvent.SelectMainTab("profile_info")) },
                        isLeft = true
                    )
                    SegmentedTabButton(
                        text = "Settlement details",
                        isSelected = uiState.mainTab == "subscription",
                        onClick = { viewModel.onEvent(ProfileUiEvent.SelectMainTab("subscription")) },
                        isLeft = false
                    )
                }
                AppSpacer(height = 4.dp)
            } else {
                AppSpacer(height = 4.dp)
            }

            // ── Scrollable Body Content ───────────────────────────────────
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(scrollState)
            ) {
                if (uiState.isEditingProfileInfo) {
                    // ── Edit Profile / Firm Info Screen ───────────────────────
                    EditProfileInfoCard(
                        formState = uiState.profileForm,
                        isMyProfile = uiState.isMyProfile,
                        isBioGenerating = uiState.isBioGenerating,
                        genderList = uiState.genderList,
                        onEvent = { viewModel.onEvent(it) }
                    )
                } else if (uiState.isEditingAdditionalInfo) {
                    // ── Edit Additional Info Screen ───────────────────────────
                    EditAdditionalInfoCard(
                        isMyProfile = uiState.isMyProfile,
                        selectedSubTab = uiState.editSubTab,
                        practiceForm = uiState.practiceDetailsForm,
                        courtsForm = uiState.courtsCasesForm,
                        eduForm = uiState.educationAwardsForm,
                        availForm = uiState.availabilityForm,
                        allPracticeAreas = uiState.allPracticeAreas,
                        allServices = uiState.allServices,
                        suggestedServices = uiState.suggestedServices,
                        searchedServices = uiState.searchedServices,
                        allCaseTypes = uiState.allCaseTypes,
                        courtStatesList = uiState.courtStatesList,
                        courtStatesMap = uiState.courtStatesMap,
                        suggestedCourtTypes = uiState.suggestedCourtTypes,
                        allCourtTypes = uiState.allCourtTypes,
                        countriesList = uiState.countriesList,
                        currencyList = uiState.currencyList,
                        statesList = uiState.statesList,
                        stateCitiesMap = uiState.stateCitiesMap,
                        onEvent = { viewModel.onEvent(it) }
                    )
                } else if (uiState.mainTab == "subscription" && canShowSegmentedControl) {
                    SubscriptionViewCard(
                        firmProfileModel = uiState.firmProfileModel,
                        hasBankAccount = uiState.hasBankAccount,
                        onDeleteAccountClick = {
                            viewModel.onEvent(ProfileUiEvent.DeleteAccountClick)
                        },
                        onBankAccountClick = {
                            viewModel.onEvent(ProfileUiEvent.BankAccountClick)
                        }
                    )
                } else {
                    ProfileViewCard(
                        firmProfileModel = uiState.firmProfileModel,
                        isMyProfile = uiState.isMyProfile,
                        selectedSubTab = uiState.subTab,
                        isBannerDismissed = uiState.isBannerDismissed,
                        onSubTabSelected = { subTab ->
                            viewModel.onEvent(ProfileUiEvent.SelectSubTab(subTab))
                        },
                        onEditFirmInfoClick = {
                            viewModel.onEvent(ProfileUiEvent.OpenEditFirmInfo)
                        },
                        onEditAddInfoClick = {
                            viewModel.onEvent(ProfileUiEvent.OpenEditAddInfo)
                        },
                        onPhotoEditClick = {
                            viewModel.onEvent(ProfileUiEvent.OpenPhotoChooser)
                        },
                        onDeletePhotoClick = {
                            viewModel.onEvent(ProfileUiEvent.OpenDeletePhotoConfirm)
                        },
                        onCompleteNowClick = {
                            viewModel.onEvent(ProfileUiEvent.CompleteNowClick)
                        },
                        onDismissBannerClick = {
                            viewModel.onEvent(ProfileUiEvent.DismissBanner)
                        }
                    )
                }
            }
        }

        // ── Photo Chooser Dialog ───────────────────────────────────────────
        if (uiState.showPhotoChooser) {
            PhotoChooserDialog(
                onCameraClick = {
                    handleCameraClick()
                },
                onGalleryClick = {
                    viewModel.onEvent(ProfileUiEvent.DismissPhotoChooser)
                    galleryLauncher.launch("image/*")
                },
                onDismiss = {
                    viewModel.onEvent(ProfileUiEvent.DismissPhotoChooser)
                }
            )
        }

        // ── Delete Photo Confirmation Dialog ───────────────────────────────
        if (uiState.showDeletePhotoConfirm) {
            AppConfirmationDialog(
                title = "Confirmation",
                message = "Are you sure you want to remove the Profile Picture ?",
                onConfirm = {
                    viewModel.onEvent(ProfileUiEvent.ConfirmDeletePhoto)
                },
                onDismiss = {
                    viewModel.onEvent(ProfileUiEvent.DismissDeletePhotoConfirm)
                }
            )
        }

        // ── Delete Account Confirmation Dialog ─────────────────────────────
        if (uiState.showDeleteAccountConfirm) {
            AppConfirmationDialog(
                title = "Confirmation",
                message = "This action cannot be undone. You will lose access to all your data.\n\nAre you sure?",
                onConfirm = {
                    viewModel.onEvent(ProfileUiEvent.ConfirmDeleteAccount)
                },
                onDismiss = {
                    viewModel.onEvent(ProfileUiEvent.DismissDeleteAccountConfirm)
                }
            )
        }

        // ── Slot Exclusion Dialog ──────────────────────────────────────────
        val activeDay = uiState.activeExcludeDay
        if (activeDay != null) {
            SlotExclusionDialog(
                day = activeDay,
                onSave = { selectedSlots ->
                    viewModel.onEvent(ProfileUiEvent.SaveExcludedSlots(activeDay.dayName, selectedSlots))
                },
                onDismiss = {
                    viewModel.onEvent(ProfileUiEvent.DismissExcludeSlotsDialog)
                }
            )
        }

        // ── Bank Account Dialog ────────────────────────────────────────────
        if (uiState.showBankAccountDialog) {
            val act = context as? Activity ?: Constants.mainActivity
            if (act != null) {
                val prefillName = Constants.NAME?.ifEmpty { "" } ?: ""
                val dialog = BankAccountDialog(
                    act,
                    context,
                    uiState.bankAccountModel?.accountDetails,
                    prefillName
                ) {
                    viewModel.fetchBankAccount()
                }
                dialog.show()
            }
            viewModel.onEvent(ProfileUiEvent.DismissBankAccountDialog)
        }

        // ── Dialogs & Loaders ──────────────────────────────────────────────
        if (uiState.isLoading) {
            AppLoader()
        }

        val alertMsg = uiState.alertMessage
        if (!alertMsg.isNullOrEmpty()) {
            MembersAlertDialog(
                title = uiState.alertTitle ?: "Alert !",
                message = alertMsg,
                onConfirm = { viewModel.onEvent(ProfileUiEvent.DismissDialogs) },
                onDismiss = { viewModel.onEvent(ProfileUiEvent.DismissDialogs) }
            )
        }

        val toastMsg = uiState.toastMessage
        if (!toastMsg.isNullOrEmpty()) {
            LaunchedEffect(toastMsg) {
                Toast.makeText(context, toastMsg, Toast.LENGTH_SHORT).show()
                viewModel.onEvent(ProfileUiEvent.DismissToast)
            }
        }
    }
}

@Composable
private fun SegmentedTabButton(
    text: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    isLeft: Boolean = true
) {
    val backgroundColor = if (isSelected) Color(0xFF004D87) else Color.White
    val textColor = if (isSelected) Color.White else Color(0xFF585858)
    val shape = if (isLeft) {
        RoundedCornerShape(topStart = 6.dp, bottomStart = 6.dp)
    } else {
        RoundedCornerShape(topEnd = 6.dp, bottomEnd = 6.dp)
    }

    Box(
        modifier = modifier
            .width(140.dp)
            .height(34.dp)
            .background(backgroundColor, shape)
            .border(0.5.dp, Color(0xFFCCCCCC), shape)
            .clickable { onClick() },
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            color = textColor,
            fontSize = 13.sp,
            fontFamily = if (isSelected) GillSansBold else GillSans,
            textAlign = TextAlign.Center
        )
    }
}

private fun getFileFromUri(context: Context, uri: Uri): File? {
    return try {
        val contentResolver = context.contentResolver
        val tempFile = File(context.cacheDir, "upload_profile_${System.currentTimeMillis()}.jpg")
        contentResolver.openInputStream(uri)?.use { input ->
            FileOutputStream(tempFile).use { output ->
                input.copyTo(output)
            }
        }
        tempFile
    } catch (e: Exception) {
        e.printStackTrace()
        null
    }
}
