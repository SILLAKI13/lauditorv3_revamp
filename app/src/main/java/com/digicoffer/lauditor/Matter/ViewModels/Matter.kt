package com.digicoffer.lauditor.Matter.ViewModels

import android.app.Dialog
import android.content.Context
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.provider.OpenableColumns
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color as ComposeColor
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.digicoffer.lauditor.CommonFiles.FileSelection.BottomSheetUploadFile
import com.digicoffer.lauditor.CommonFiles.GlobalFiles.AndroidUtils
import com.digicoffer.lauditor.CommonFiles.GlobalFiles.Constants
import com.digicoffer.lauditor.CommonFiles.GlobalFiles.NewModel
import com.digicoffer.lauditor.Matter.Models.GroupsModel
import com.digicoffer.lauditor.Matter.Models.HistoryModel
import com.digicoffer.lauditor.Matter.Models.MatterModel
import com.digicoffer.lauditor.Matter.Models.ViewMatterModel
import com.digicoffer.lauditor.R
import com.digicoffer.lauditor.Webservice.AsyncTaskCompleteListener
import com.digicoffer.lauditor.Webservice.CommonApiHelper.WebServiceHelper
import com.digicoffer.lauditor.Webservice.HttpResultDo
import com.digicoffer.lauditor.core.designsystem.colors.ColorTokens
import com.digicoffer.lauditor.feature.matter.presentation.screen.DocumentsScreen
import com.digicoffer.lauditor.feature.matter.presentation.screen.GctScreen
import com.digicoffer.lauditor.feature.matter.presentation.screen.MatterEditScreen
import com.digicoffer.lauditor.feature.matter.presentation.screen.MatterListingScreen
import com.digicoffer.lauditor.feature.matter.presentation.viewmodel.MatterEditViewModel
import com.digicoffer.lauditor.feature.matter.presentation.viewmodel.MatterViewModel
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject
import java.io.File
import java.io.IOException
import java.io.InputStream
import java.nio.file.Files
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.TimeUnit

enum class MatterScreen {
    LISTING,
    STEP_INFO,
    STEP_GCT,
    STEP_DOCUMENTS,
    TIMELINE
}

class Matter : Fragment(), AsyncTaskCompleteListener, View.OnClickListener, BottomSheetUploadFile.OnPhotoSelectedListner {

    private var mViewModel: NewModel? = null
    var progress_dialog: Dialog? = null

    var historyList = ArrayList<HistoryModel>()
    var viewMatterModel = ViewMatterModel()
    var header_name = ""
    var chk_viewMatter: ViewMatter? = null
    var itemsArrayList = ArrayList<ViewMatterModel>()
    
    @JvmField
    var matter_arraylist = ArrayList<MatterModel>()

    // View model instance declared as class member to populate clients/members across detail navigation
    private lateinit var editViewModel: MatterEditViewModel

    // Compose Navigation State
    val activeScreenState = mutableStateOf(MatterScreen.LISTING)
    val isCreateModeState = mutableStateOf(false)
    val matterTypeState = mutableStateOf(Constants.MATTER_TYPE ?: "Legal")

    // Font families
    private val GillSans = FontFamily(Font(R.font.gill_sans))
    private val GillSansBold = FontFamily(Font(R.font.gill_sans_bold))

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        mViewModel = ViewModelProvider(requireActivity())[NewModel::class.java]
        mViewModel?.setData("Matter")

        editViewModel = ViewModelProvider(this)[MatterEditViewModel::class.java]
        val matterViewModel = ViewModelProvider(this)[MatterViewModel::class.java]

        Constants.is_CreateMatter = Constants.isCreate

        val filterType = Constants.matterFilterType
        if (!filterType.isNullOrEmpty()) {
            Constants.matterFilterType = ""
            matter_arraylist.clear()
            if (filterType.equals("General", ignoreCase = true)) {
                loadGeneralMatter()
            } else {
                loadLegalMatter()
            }
        } else if (Constants.MATTER_TYPE == "General") {
            matter_arraylist.clear()
            loadGeneralMatter()
        } else {
            matter_arraylist.clear()
            loadLegalMatter()
        }

        return ComposeView(requireContext()).apply {
            setContent {
                com.digicoffer.lauditor.core.designsystem.theme.LauditorTheme {
                    MatterScreenContainer(editViewModel, matterViewModel)
                }
            }
        }
    }

    @Composable
    fun ModuleIconButton(
        text: String,
        iconRes: Int,
        onClick: () -> Unit
    ) {
        Card(
            onClick = onClick,
            colors = CardDefaults.cardColors(containerColor = ComposeColor.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
            shape = RoundedCornerShape(10.dp), // 10.dp corner radius for rectangle shape
            modifier = Modifier
                .wrapContentSize()
                .height(40.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .padding(start = 6.dp, end = 12.dp)
                    .fillMaxHeight()
            ) {
                Box(
                    modifier = Modifier
                        .size(30.dp)
                        .background(ColorTokens.BluePrimary, shape = CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        painter = painterResource(id = iconRes),
                        contentDescription = null,
                        tint = ComposeColor.White,
                        modifier = Modifier.size(14.dp)
                    )
                }
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = text,
                    color = ColorTokens.BluePrimary,
                    fontFamily = GillSans,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Normal,
                    maxLines = 1
                )
            }
        }
    }

    @Composable
    fun MatterScreenContainer(
        editViewModel: MatterEditViewModel,
        matterViewModel: MatterViewModel
    ) {
        val activeScreen by activeScreenState
        val isCreateMode by isCreateModeState
        val matterType by matterTypeState

        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(ColorTokens.LightBlueBg) // Removed F7F7F7 and use LightBlueBg to avoid white/grey header background
        ) {
            // Header controls
            if (activeScreen == MatterScreen.LISTING) {
                // Listing Header with title and borderless "Create Matter" button on the right
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = if (matterType == "Legal") "List of Legal Matters" else "List of General Matters",
                        fontFamily = GillSansBold,
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp,
                        color = ColorTokens.BluePrimary
                    )

                    ModuleIconButton(
                        text = "Create Matter",
                        iconRes = R.drawable.simple_plus_icon,
                        onClick = {
                            if (!Constants.is_active) {
                                AndroidUtils.showRenewalPopup(requireActivity())
                            } else {
                                Constants.Matter_id = ""
                                Constants.upload_documents_list.clear()
                                Constants.create_matter = true
                                Constants.Matter_CreateOrViewDetails = "Create"
                                matter_arraylist.clear()
                                loadCreateUI()
                            }
                        }
                    )
                }
            } else if (isCreateMode || Constants.Matter_CreateOrViewDetails == "Edit Matter Info") {
                // Header row containing only the "View Matter" button with eye icon aligned to the right (No title and no close icon)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    ModuleIconButton(
                        text = "View Matter",
                        iconRes = R.drawable.eye_icon,
                        onClick = { loadViewUI() }
                    )
                }
            }

            // Stepper Header (only visible when in create/edit or details flow, not in listing view)
            if (activeScreen != MatterScreen.LISTING) {
                StepperHeader(activeScreen)
            }

            // Details Header Components (separated "dfs" box and circular close icon, matching timeline_notes.xml)
            val showDetailsHeader = !isCreateMode && (activeScreen == MatterScreen.TIMELINE)
            if (showDetailsHeader) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 10.dp, vertical = 6.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Left component: "dfs" title card container, non-bold GillSans text, with 1.5f weight distribution
                    Card(
                        modifier = Modifier
                            .weight(1.5f)
                            .height(40.dp),
                        shape = RoundedCornerShape(10.dp),
                        colors = CardDefaults.cardColors(containerColor = ComposeColor.White),
                        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                    ) {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = header_name.ifEmpty { viewMatterModel.title },
                                fontFamily = GillSans,
                                fontWeight = FontWeight.Normal,
                                fontSize = 15.sp,
                                color = ColorTokens.BluePrimary,
                                textAlign = TextAlign.Center
                            )
                        }
                    }

                    // Right component: circular close icon (cancel_icon_1), separate with 1.0f weight distribution
                    Box(
                        modifier = Modifier
                            .weight(1.0f),
                        contentAlignment = Alignment.CenterEnd
                    ) {
                        IconButton(
                            onClick = { loadViewUI() },
                            modifier = Modifier.size(35.dp)
                        ) {
                            Icon(
                                painter = painterResource(id = R.drawable.cancel_icon_1),
                                contentDescription = "Close",
                                tint = ComposeColor.Unspecified,
                                modifier = Modifier.size(35.dp)
                            )
                        }
                    }
                }
            }

            // Screen Content Frame
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
            ) {
                when (activeScreen) {
                    MatterScreen.LISTING -> {
                        MatterListingScreen(
                            viewModel = matterViewModel,
                            onEditMatterClick = { model ->
                                Constants.Matter_CreateOrViewDetails = "Edit Matter Info"
                                Constants.matterDate = model.created
                                viewMatterModel = model
                                matter_arraylist.clear()
                                val matterModel = MatterModel().apply {
                                    matter_title = model.title
                                    case_number = model.caseNumber
                                    case_type = model.casetype
                                    description = model.description
                                    court = model.courtName
                                    judge = model.judges
                                    case_priority = model.priority
                                    status = model.status
                                    date_of_filing = model.date_of_filling
                                    start_date = model.startdate
                                    end_date = model.closedate
                                    opponent_advocate = model.opponentAdvocates
                                    clients = model.clients
                                    members = model.members
                                    matter_id = model.matterNumber
                                    corp_client_id = model.corporate.optJSONObject(0)?.optString("id") ?: ""
                                }
                                matter_arraylist.add(matterModel)
                                Constants.Matter_id = model.id ?: ""
                                editViewModel.initialize(model)
                                loadMatterInformation()
                            },
                            onViewTimelineClick = { model ->
                                Constants.create_matter = false
                                Constants.Matter_CreateOrViewDetails = "View Timeline"
                                Constants.matterDate = model.created
                                viewMatterModel = model
                                Constants.Matter_id = model.id ?: ""
                                editViewModel.initialize(model)
                                callTimeLineWebservice()
                            }
                        )
                    }
                    MatterScreen.STEP_INFO -> {
                        MatterEditScreen(
                            editModel = if (isCreateMode) null else viewMatterModel,
                            onNavigateBack = { loadViewUI() },
                            onNavigateNext = {
                                val currentId = Constants.Matter_id
                                if (!currentId.isNullOrEmpty()) {
                                    loadGCT()
                                } else {
                                    editViewModel.submitForm(isSaveLater = false, editModel = viewMatterModel)
                                }
                            },
                            viewModel = editViewModel
                        )
                    }
                    MatterScreen.STEP_GCT -> {
                        GctScreen(
                            editModel = if (isCreateMode) null else viewMatterModel,
                            onNavigateBack = { loadViewUI() },
                            onNavigateNext = {
                                val currentId = Constants.Matter_id
                                if (!currentId.isNullOrEmpty()) {
                                    loadDocuments()
                                }
                            },
                            viewModel = editViewModel
                        )
                    }
                    MatterScreen.STEP_DOCUMENTS -> {
                        DocumentsScreen(
                            viewModel = editViewModel,
                            onBrowseClick = { showPhotoOptions() },
                            onViewDocument = {},
                            onCancel = { loadViewUI() }
                        )
                    }
                    MatterScreen.TIMELINE -> {
                        TimeLineScreenContent()
                    }
                }
            }
        }
    }

    @Composable
    fun StepperHeader(activeScreen: MatterScreen) {
        val step = when (activeScreen) {
            MatterScreen.STEP_INFO -> 0
            MatterScreen.STEP_GCT -> 1
            MatterScreen.STEP_DOCUMENTS -> 2
            MatterScreen.TIMELINE -> 0
            MatterScreen.LISTING -> 0
        }

        val isTimelineMode = activeScreen == MatterScreen.TIMELINE || (!isCreateModeState.value && activeScreen != MatterScreen.STEP_INFO)

        val step1Text = if (isTimelineMode) "Timeline" else "Matter Information"
        val step2Text = if ("solo" == Constants.CATEGORY) "Client(s)" else "Client (s) & Team Member(S)"
        val step3Text = "Document(S)"

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 12.dp, horizontal = 16.dp), // Removed white background from StepperHeader container
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(40.dp) // Reverted to legacy 40.dp height
            ) {
                // Row of Icons containing connecting line segments between step circles (starting exactly after matter info, ending exactly before document icon)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 32.dp)
                        .align(Alignment.Center),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (isTimelineMode) {
                        StepCircle(
                            iconRes = R.drawable.timeline_new,
                            isActive = activeScreen == MatterScreen.TIMELINE,
                            onClick = {
                                if (activeScreen != MatterScreen.TIMELINE) {
                                    callTimeLineWebservice()
                                }
                            }
                        )
                    } else {
                        StepCircle(
                            iconRes = R.drawable.single_document_icon_foreground,
                            isActive = activeScreen == MatterScreen.STEP_INFO,
                            onClick = {
                                loadMatterInformation()
                            }
                        )
                    }

                    // Line between Step 1 and Step 2
                    val line1Active = step >= 1
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(2.dp)
                            .background(if (line1Active) ColorTokens.BluePrimary else ComposeColor.LightGray)
                    )

                    StepCircle(
                        iconRes = R.drawable.gct_new,
                        isActive = activeScreen == MatterScreen.STEP_GCT,
                        onClick = {
                            if (!isCreateModeState.value) {
                                Matter_Gct()
                            } else if (matter_arraylist.isEmpty() || (matter_arraylist[0].matter_title ?: "").isEmpty()) {
                                AndroidUtils.showAlert("Please check the Matter Information section", activity, "Info")
                            } else {
                                val matterId = Constants.Matter_id
                                if (!matterId.isNullOrEmpty()) {
                                    loadGCT()
                                }
                            }
                        }
                    )

                    // Line between Step 2 and Step 3
                    val line2Active = step >= 2
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(2.dp)
                            .background(if (line2Active) ColorTokens.BluePrimary else ComposeColor.LightGray)
                    )

                    StepCircle(
                        iconRes = R.drawable.documents_new,
                        isActive = activeScreen == MatterScreen.STEP_DOCUMENTS,
                        onClick = {
                            if (!isCreateModeState.value) {
                                Matter_Doc()
                            } else if (matter_arraylist.isEmpty() || (matter_arraylist[0].matter_title ?: "").isEmpty()) {
                                AndroidUtils.showAlert("Please check the Matter Information section", activity, "Info")
                            } else {
                                val matterId = Constants.Matter_id
                                if (!matterId.isNullOrEmpty()) {
                                    loadDocuments()
                                }
                            }
                        }
                    )
                }
            }

            Spacer(modifier = Modifier.height(4.dp))

            // Row of labels below the icons matching circles alignment
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 0.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                StepLabel(
                    text = step1Text,
                    isActive = if (isTimelineMode) activeScreen == MatterScreen.TIMELINE else activeScreen == MatterScreen.STEP_INFO
                )
                StepLabel(
                    text = step2Text,
                    isActive = activeScreen == MatterScreen.STEP_GCT
                )
                StepLabel(
                    text = step3Text,
                    isActive = activeScreen == MatterScreen.STEP_DOCUMENTS
                )
            }
        }
    }

    @Composable
    fun StepCircle(
        iconRes: Int,
        isActive: Boolean,
        onClick: () -> Unit
    ) {
        val isPaddingCompensated = iconRes == R.drawable.single_document_icon_foreground
        // Increased iconSize to 54.dp for padding compensated icons to match exactly the 30.dp sizes of the other icons (compensating for transparent padding)
        val iconSize = if (isPaddingCompensated) 54.dp else 30.dp

        Box(
            modifier = Modifier
                .size(40.dp) // Outer circle shape size set back to legacy XML 40.dp
                .background(
                    color = if (isActive) ColorTokens.BluePrimary else ComposeColor.White,
                    shape = CircleShape
                )
                .border(
                    width = 1.dp,
                    color = if (isActive) ColorTokens.BluePrimary else ComposeColor.Gray,
                    shape = CircleShape
                )
                .clickable { onClick() },
            contentAlignment = Alignment.Center
        ) {
            Icon(
                painter = painterResource(id = iconRes),
                contentDescription = null,
                tint = if (isActive) ComposeColor.White else ComposeColor.Black,
                modifier = Modifier.size(iconSize)
            )
        }
    }

    @Composable
    fun StepLabel(
        text: String,
        isActive: Boolean,
        modifier: Modifier = Modifier
    ) {
        Text(
            text = text,
            fontSize = 13.sp, // Sized up to 13.sp to match legacy text size
            color = ComposeColor.Black,
            fontFamily = GillSans,
            textAlign = TextAlign.Center,
            fontWeight = if (isActive) FontWeight.Bold else FontWeight.Normal,
            maxLines = 2,
            lineHeight = 15.sp,
            modifier = modifier.width(110.dp) // Adjusted width for layout parity and correct line breaks
        )
    }

    @Composable
    fun TimeLineScreenContent() {
        // Entire list of timeline items is wrapped in a single card, matching ll_timeLine rectangular_white_background exactly
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(10.dp),
            shape = RoundedCornerShape(10.dp),
            colors = CardDefaults.cardColors(containerColor = ComposeColor.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
        ) {
            androidx.compose.foundation.lazy.LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp)
            ) {
                itemsIndexed(historyList) { index, history ->
                    TimelineItemRow(index, history)
                    if (index < historyList.size - 1) {
                        Spacer(modifier = Modifier.height(10.dp))
                        HorizontalDivider(color = ComposeColor(0xFFEEEEEE), thickness = 0.5.dp)
                        Spacer(modifier = Modifier.height(10.dp))
                    }
                }
            }
        }
    }

    @Composable
    fun TimelineItemRow(index: Int, history: HistoryModel) {
        val corp = viewMatterModel.corporate
        val showCorporateTab = corp != null && corp.length() > 0 && history.from_ts != history.to_ts
        var isLauditorSelected by remember { mutableStateOf(true) }

        var showEditArea by remember { mutableStateOf(false) }
        var showViewArea by remember { mutableStateOf(false) }
        var showAddArea by remember { mutableStateOf(false) }
        
        var noteInputText by remember { mutableStateOf("") }

        val notesText = history.notes
        val hasNotes = notesText != null && notesText.isNotEmpty() && notesText != "null"
        val showSimpleIcon = !hasNotes && history.from_ts != history.to_ts

        Column(
            modifier = Modifier.fillMaxWidth()
        ) {
            // Title (rendered in blue as per R.layout.title_name_layout spec: 15.sp, GillSansBold)
            Text(
                text = history.title ?: "",
                fontFamily = GillSansBold,
                fontSize = 15.sp,
                color = ColorTokens.BluePrimary
            )
            Spacer(modifier = Modifier.height(4.dp))
            
            // Date (rendered in black as per R.layout.black_normal_txt spec: 15.sp, GillSans)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = formatEventDate(history.from_ts),
                    fontFamily = GillSans,
                    fontSize = 15.sp,
                    color = ComposeColor.Black,
                    modifier = Modifier.weight(1f)
                )

                // If showSimpleIcon is true, display the edit/add icon here
                if (showSimpleIcon && !showAddArea) {
                    IconButton(
                        onClick = {
                            noteInputText = ""
                            showAddArea = true
                        },
                        modifier = Modifier.size(24.dp)
                    ) {
                        Icon(
                            painter = painterResource(id = R.drawable.edit__icon),
                            contentDescription = "Add Notes",
                            tint = ComposeColor.Unspecified,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }

            // If showAddArea is true (user clicked simple edit icon to add new notes)
            if (showAddArea) {
                Spacer(modifier = Modifier.height(8.dp))
                Column(modifier = Modifier.fillMaxWidth()) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Notes",
                            color = ColorTokens.BluePrimary,
                            fontFamily = GillSans,
                            fontSize = 15.sp
                        )
                        Text(
                            text = " *",
                            color = ComposeColor.Red,
                            fontSize = 15.sp
                        )
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    OutlinedTextField(
                        value = noteInputText,
                        onValueChange = { if (it.length <= 150) noteInputText = it },
                        modifier = Modifier.fillMaxWidth(),
                        textStyle = androidx.compose.ui.text.TextStyle(fontFamily = GillSans),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = ColorTokens.BluePrimary,
                            unfocusedBorderColor = ComposeColor.LightGray
                        )
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End
                    ) {
                        Button(
                            onClick = { showAddArea = false },
                            colors = ButtonDefaults.buttonColors(containerColor = ComposeColor(0xFFE8E8E8)),
                            border = BorderStroke(1.dp, ComposeColor(0xFF888888)),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.height(36.dp)
                        ) {
                            Text(text = "Cancel", fontFamily = GillSansBold, color = ComposeColor.Black, fontSize = 12.sp)
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Button(
                            onClick = {
                                if (noteInputText.isNotEmpty()) {
                                    showAddArea = false
                                    chk_viewMatter?.callEditNotesWebservice(history.id ?: "", noteInputText.trim())
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = ColorTokens.BluePrimary),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.height(36.dp),
                            enabled = noteInputText.isNotEmpty()
                        ) {
                            Text(text = "Create", fontFamily = GillSansBold, color = ComposeColor.White, fontSize = 12.sp)
                        }
                    }
                }
            }

            // Show Corporate vs Lauditor notes tab if applicable
            if (showCorporateTab) {
                Spacer(modifier = Modifier.height(10.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .clickable { isLauditorSelected = true }
                            .padding(end = 16.dp)
                    ) {
                        RadioButton(
                            selected = isLauditorSelected,
                            onClick = { isLauditorSelected = true },
                            colors = RadioButtonDefaults.colors(
                                    selectedColor = ColorTokens.BluePrimary,
                                    unselectedColor = ComposeColor.Black
                                )
                        )
                        Text(
                            text = "Lauditor Notes",
                            fontSize = 14.sp,
                            fontFamily = GillSansBold,
                            color = if (isLauditorSelected) ColorTokens.BluePrimary else ComposeColor.Black,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.clickable { isLauditorSelected = false }
                    ) {
                        RadioButton(
                            selected = !isLauditorSelected,
                            onClick = { isLauditorSelected = false },
                            colors = RadioButtonDefaults.colors(
                                    selectedColor = ColorTokens.BluePrimary,
                                    unselectedColor = ComposeColor.Black
                                )
                        )
                        Text(
                            text = "Corporate Notes",
                            fontSize = 14.sp,
                            fontFamily = GillSansBold,
                            color = if (!isLauditorSelected) ColorTokens.BluePrimary else ComposeColor.Black,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            // Render notes if present and (either not corporate tab or lauditor tab selected)
            if (hasNotes && (!showCorporateTab || isLauditorSelected)) {
                Spacer(modifier = Modifier.height(8.dp))
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp),
                    colors = CardDefaults.cardColors(containerColor = ComposeColor(0xFFFAFAFA)),
                    border = BorderStroke(0.5.dp, ComposeColor.LightGray)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(10.dp)
                    ) {
                        if (!showEditArea && !showViewArea) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "${notesText}....",
                                    fontSize = 14.sp,
                                    fontFamily = GillSans,
                                    color = ComposeColor.Black,
                                    textDecoration = TextDecoration.Underline,
                                    modifier = Modifier.weight(1f)
                                )

                                // Action icons (Edit / Eye) if not all day event
                                if (!history.allday) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        IconButton(
                                            onClick = {
                                                noteInputText = notesText ?: ""
                                                showEditArea = true
                                                showViewArea = false
                                            },
                                            modifier = Modifier.size(28.dp)
                                        ) {
                                            Icon(
                                                painter = painterResource(id = R.drawable.edit__icon),
                                                contentDescription = "Edit Notes",
                                                tint = ComposeColor.Unspecified,
                                                modifier = Modifier.size(20.dp)
                                            )
                                        }
                                        Spacer(modifier = Modifier.width(4.dp))
                                        IconButton(
                                            onClick = {
                                                showViewArea = true
                                                showEditArea = false
                                            },
                                            modifier = Modifier.size(28.dp)
                                        ) {
                                            Icon(
                                                painter = painterResource(id = R.drawable.eye_icon),
                                                contentDescription = "View Notes",
                                                tint = ComposeColor.Unspecified,
                                                modifier = Modifier.size(20.dp)
                                            )
                                        }
                                    }
                                }
                            }
                        }

                        if (showViewArea) {
                            OutlinedTextField(
                                value = notesText ?: "",
                                onValueChange = {},
                                readOnly = true,
                                modifier = Modifier.fillMaxWidth(),
                                textStyle = androidx.compose.ui.text.TextStyle(fontFamily = GillSans),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = ComposeColor.LightGray,
                                    unfocusedBorderColor = ComposeColor.LightGray
                                )
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.End
                            ) {
                                Button(
                                    onClick = { showViewArea = false },
                                    colors = ButtonDefaults.buttonColors(containerColor = ColorTokens.BluePrimary),
                                    shape = RoundedCornerShape(10.dp),
                                    modifier = Modifier.height(36.dp)
                                ) {
                                    Text(text = "Close", fontFamily = GillSansBold, color = ComposeColor.White, fontSize = 12.sp)
                                }
                            }
                        }

                        if (showEditArea) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "Notes",
                                    color = ColorTokens.BluePrimary,
                                    fontFamily = GillSans,
                                    fontSize = 15.sp
                                )
                                Text(
                                    text = " *",
                                    color = ComposeColor.Red,
                                    fontSize = 15.sp
                                )
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            OutlinedTextField(
                                value = noteInputText,
                                onValueChange = { if (it.length <= 150) noteInputText = it },
                                modifier = Modifier.fillMaxWidth(),
                                textStyle = androidx.compose.ui.text.TextStyle(fontFamily = GillSans),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = ColorTokens.BluePrimary,
                                    unfocusedBorderColor = ComposeColor.LightGray
                                )
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.End
                            ) {
                                Button(
                                    onClick = { showEditArea = false },
                                    colors = ButtonDefaults.buttonColors(containerColor = ComposeColor(0xFFE8E8E8)),
                                    border = BorderStroke(1.dp, ComposeColor(0xFF888888)),
                                    shape = RoundedCornerShape(10.dp),
                                    modifier = Modifier.height(36.dp)
                                ) {
                                    Text(text = "Cancel", fontFamily = GillSansBold, color = ComposeColor.Black, fontSize = 12.sp)
                                }
                                Spacer(modifier = Modifier.width(8.dp))
                                Button(
                                    onClick = {
                                        if (noteInputText.isNotEmpty()) {
                                            showEditArea = false
                                            chk_viewMatter?.callEditNotesWebservice(history.id ?: "", noteInputText.trim())
                                        }
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = ColorTokens.BluePrimary),
                                    shape = RoundedCornerShape(10.dp),
                                    modifier = Modifier.height(36.dp),
                                    enabled = noteInputText.isNotEmpty()
                                ) {
                                    Text(text = "Save", fontFamily = GillSansBold, color = ComposeColor.White, fontSize = 12.sp)
                                }
                            }
                        }
                    }
                }
            }

            // Render Corporate Notes list if corporate tab selected and notes list present
            if (showCorporateTab && !isLauditorSelected) {
                val notesList = history.notes_list
                if (notesList != null && notesList.length() > 0) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Column(modifier = Modifier.fillMaxWidth()) {
                        for (j in 0 until notesList.length()) {
                            val noteObj = notesList.optJSONObject(j) ?: continue
                            val noteText = noteObj.optString("notes", "")
                            val addedBy = noteObj.optString("added_by", "")
                            val addOn = noteObj.optString("add_on", "")
                            
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp),
                                verticalAlignment = Alignment.Top
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(36.dp)
                                        .background(ColorTokens.BluePrimary, shape = CircleShape),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = if (addedBy.isNotEmpty()) addedBy.substring(0, 1).uppercase() else "",
                                        color = ComposeColor.White,
                                        fontWeight = FontWeight.Bold,
                                        fontFamily = GillSansBold,
                                        fontSize = 16.sp
                                    )
                                }
                                Spacer(modifier = Modifier.width(10.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = addedBy,
                                        fontWeight = FontWeight.Bold,
                                        fontFamily = GillSansBold,
                                        fontSize = 14.sp,
                                        color = ComposeColor.Black
                                    )
                                    Text(
                                        text = addOn,
                                        fontFamily = GillSans,
                                        fontSize = 11.sp,
                                        color = ComposeColor.Gray
                                    )
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text(
                                        text = noteText,
                                        fontFamily = GillSans,
                                        fontSize = 13.sp,
                                        color = ComposeColor.DarkGray
                                    )
                                }
                            }
                            if (j < notesList.length() - 1) {
                                HorizontalDivider(color = ComposeColor(0xFFEEEEEE), thickness = 0.5.dp)
                            }
                        }
                    }
                } else {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(text = "No Corporate Notes available.", fontFamily = GillSans, fontSize = 13.sp, color = ComposeColor.Gray)
                }
            }
        }
    }

    private fun formatEventDate(dateStr: String?): String {
        if (dateStr.isNullOrEmpty()) return ""
        return try {
            val inputFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.ENGLISH)
            val outputFormat = SimpleDateFormat("MMMM dd, yyyy | hh:mm a", Locale.ENGLISH)
            val date = inputFormat.parse(dateStr)
            if (date != null) outputFormat.format(date) else dateStr ?: ""
        } catch (e: Exception) {
            dateStr ?: ""
        }
    }

    fun loadViewUI() {
        Constants.GeneratedMatterId = ""
        Constants.Matter_id = ""
        Constants.allClientGroups.clear()
        Constants.upload_documents_list.clear()
        Constants.selected_temp_clients_list.clear()
        Constants.is_CreateMatter = false

        isCreateModeState.value = false
        activeScreenState.value = MatterScreen.LISTING

        callGroupsWebservice()

        if (matterTypeState.value == "Legal") {
            mViewModel?.setData("View Legal Matter")
        } else {
            mViewModel?.setData("View General Matter")
        }
    }

    fun loadCreateUI() {
        Constants.GeneratedMatterId = ""
        Constants.selected_temp_clients_list.clear()
        Constants.is_CreateMatter = true
        Constants.allClientGroups.clear()

        isCreateModeState.value = true
        activeScreenState.value = MatterScreen.STEP_INFO

        if (Constants.MATTER_TYPE == "Legal") {
            mViewModel?.setData("Create Legal Matter")
        } else {
            mViewModel?.setData("Create General Matter")
        }
    }

    fun loadLegalMatter() {
        Constants.MATTER_TYPE = "Legal"
        matterTypeState.value = "Legal"
        if (isCreateModeState.value) {
            loadCreateUI()
        } else {
            loadViewUI()
        }
    }

    fun loadGeneralMatter() {
        Constants.MATTER_TYPE = "General"
        matterTypeState.value = "General"
        if (isCreateModeState.value) {
            loadCreateUI()
        } else {
            loadViewUI()
        }
    }

    fun loadDocuments() {
        activeScreenState.value = MatterScreen.STEP_DOCUMENTS
    }

    fun loadGCT() {
        activeScreenState.value = MatterScreen.STEP_GCT
    }

    fun Matter_Gct() {
        activeScreenState.value = MatterScreen.STEP_GCT
    }

    fun Matter_Doc() {
        activeScreenState.value = MatterScreen.STEP_DOCUMENTS
    }

    fun Matter_Notes() {
        activeScreenState.value = MatterScreen.TIMELINE
    }

    fun loadMatterInformation() {
        activeScreenState.value = MatterScreen.STEP_INFO
    }

    private fun loadTimeline() {
        Constants.create_matter = false
        Constants.Matter_id = viewMatterModel.id ?: ""
        activeScreenState.value = MatterScreen.TIMELINE
    }

    fun View_Details(
        viewMatterModel: ViewMatterModel,
        viewMatter: ViewMatter,
        historyList: ArrayList<HistoryModel>,
        header_name: String
    ) {
        chk_viewMatter = viewMatter
        Constants.create_matter = false

        this.historyList = historyList
        this.header_name = header_name
        this.viewMatterModel = viewMatterModel

        editViewModel.initialize(viewMatterModel)
        loadTimeline()
    }

    fun View_Details(
        historyList: ArrayList<HistoryModel>,
        viewMatter: ViewMatter,
        header_name: String,
        viewMatterModel: ViewMatterModel
    ) {
        chk_viewMatter = viewMatter
        Constants.create_matter = false

        this.historyList = historyList
        this.header_name = header_name
        this.viewMatterModel = viewMatterModel

        editViewModel.initialize(viewMatterModel)
        loadTimeline()
    }

    fun callTimeLineWebservice() {
        try {
            progress_dialog = AndroidUtils.get_progress(activity)
            val postdata = JSONObject()
            val calendar: Calendar = GregorianCalendar()
            val timeZone = calendar.timeZone
            val offset = timeZone.rawOffset
            val hours = TimeUnit.MILLISECONDS.toMinutes(offset.toLong())
            val timezoneoffset = -1 * hours
            val matterType = (Constants.MATTER_TYPE ?: "").lowercase(Locale.ROOT)
            val timelineId = viewMatterModel.id ?: ""
            WebServiceHelper.callHttpWebService(
                this,
                requireContext(),
                WebServiceHelper.RestMethodType.GET,
                "matter/$matterType/$timelineId/history/$timezoneoffset",
                "TimeLine",
                postdata.toString()
            )
        } catch (e: Exception) {
            if (progress_dialog != null && progress_dialog!!.isShowing) {
                AndroidUtils.dismiss_dialog(progress_dialog)
            }
            e.fillInStackTrace()
        }
    }

    private fun loadHistory(result: JSONObject) {
        try {
            historyList.clear()
            val id = result.optString("id")
            val title = result.optString("title")
            val jsonArray = result.optJSONArray("history")
            if (jsonArray != null) {
                for (i in 0 until jsonArray.length()) {
                    val jsonObject = jsonArray.optJSONObject(i) ?: continue
                    val historyModel = HistoryModel()
                    if (jsonObject.has("allday")) {
                        historyModel.allday = jsonObject.getBoolean("allday")
                    } else {
                        historyModel.allday = true
                    }
                    historyModel.id = jsonObject.optString("id")
                    historyModel.description = jsonObject.optString("description")
                    historyModel.event_type = jsonObject.optString("event_type")
                    historyModel.from_ts = jsonObject.optString("from_ts")
                    historyModel.to_ts = jsonObject.optString("to_ts")
                    historyModel.title = jsonObject.optString("title")
                    historyModel.notes = jsonObject.optString("notes")
                    historyModel.notes_list = jsonObject.optJSONArray("notes_list") ?: JSONArray()
                    historyList.add(historyModel)
                }
            }
        } catch (e: JSONException) {
            AndroidUtils.showAlert(e.message, activity)
            e.fillInStackTrace()
        }
        header_name = viewMatterModel.title ?: ""
        loadTimeline()
    }

    fun callEditNotesWebservice(id: String, notes: String) {
        progress_dialog = AndroidUtils.get_progress(activity)
        try {
            val postdata = JSONObject()
            postdata.put("notes", notes)
            WebServiceHelper.callHttpWebService(
                this,
                requireContext(),
                WebServiceHelper.RestMethodType.PUT,
                "event/notes/$id",
                "Notes",
                postdata.toString()
            )
        } catch (e: Exception) {
            if (progress_dialog != null && progress_dialog!!.isShowing) {
                AndroidUtils.dismiss_dialog(progress_dialog)
            }
            e.fillInStackTrace()
        }
    }

    fun showEmptyState(isCreateMode: Boolean) {}
    fun hideEmptyState() {}

    private fun callGroupsWebservice() {
        progress_dialog = AndroidUtils.get_progress(activity)
        val postdata = JSONObject()
        WebServiceHelper.callHttpWebService(
            this,
            requireContext(),
            WebServiceHelper.RestMethodType.PUT,
            "matter/attachments",
            "Groups",
            postdata.toString()
        )
    }

    override fun onClick(view: View) {}

    override fun onAsyncTaskComplete(httpResult: HttpResultDo) {
        if (progress_dialog != null && progress_dialog!!.isShowing) {
            AndroidUtils.dismiss_dialog(progress_dialog)
        }

        if (httpResult.result == WebServiceHelper.ServiceCallStatus.Success) {
            try {
                val result = JSONObject(httpResult.responseContent)
                if (httpResult.requestType == "Groups") {
                    val data = result.getJSONArray("data")
                    loadGroupsData(data)
                } else if (httpResult.requestType == "TimeLine") {
                    val iserror = result.optBoolean("error")
                    if (iserror) {
                        val msg = result.getString("msg")
                        AndroidUtils.showAlert(msg, activity)
                    } else {
                        loadHistory(result)
                    }
                } else if (httpResult.requestType == "Notes") {
                    val iserror = result.optBoolean("error")
                    val updatemsg = result.getString("msg")
                    if (!iserror) {
                        AndroidUtils.showAlert(updatemsg, activity)
                        callTimeLineWebservice()
                    } else {
                        AndroidUtils.showAlert(updatemsg, activity)
                    }
                }
            } catch (e: JSONException) {
                e.fillInStackTrace()
            }
        }
    }

    private fun loadGroupsData(data: JSONArray) {
        try {
            Constants.groupsList_Access.clear()
            for (i in 0 until data.length()) {
                val jsonObject = data.getJSONObject(i)
                val groupsModel = GroupsModel()
                groupsModel.group_id = jsonObject.getString("id")
                groupsModel.group_name = jsonObject.getString("name")
                if (jsonObject.optString("name") != "AAM" && jsonObject.optString("name") != "SuperUser") {
                    Constants.groupsList_Access.add(groupsModel)
                }
            }
        } catch (e: JSONException) {
            e.fillInStackTrace()
            AndroidUtils.showAlert(e.message, activity)
        }
    }

    private fun showPhotoOptions() {
        val bottommSheetUploadDocument = BottomSheetUploadFile(null)
        bottommSheetUploadDocument.show(parentFragmentManager, "")
        bottommSheetUploadDocument.setTargetFragment(this, 1)
    }

    override fun getImagepath(imagepath: File?, ImageURI: Uri?) {
        if (imagepath != null) {
            val name = imagepath.name
            editViewModel.addUploadFile(imagepath, name)
        } else if (ImageURI != null) {
            val file = getFile(requireContext(), ImageURI)
            val name = file.name
            editViewModel.addUploadFile(file, name)
        }
    }

    override fun getImageBitmap(bitmap: android.graphics.Bitmap?) {}

    fun getFile(context: Context, uri: Uri): File {
        val destinationFilename = File(context.filesDir.path + File.separatorChar + queryName(context, uri))
        try {
            context.contentResolver.openInputStream(uri).use { ins ->
                if (ins != null) {
                    createFileFromStream(ins, destinationFilename)
                }
            }
        } catch (ex: Exception) {
            ex.fillInStackTrace()
        }
        return destinationFilename
    }

    fun createFileFromStream(ins: InputStream, destination: File?) {
        try {
            java.nio.file.Files.newOutputStream(destination!!.toPath()).use { os ->
                val buffer = ByteArray(4096)
                var length: Int
                while (ins.read(buffer).also { length = it } > 0) {
                    os.write(buffer, 0, length)
                }
                os.flush()
            }
        } catch (ex: Exception) {
            ex.fillInStackTrace()
        }
    }

    private fun queryName(context: Context, uri: Uri): String {
        val returnCursor = context.contentResolver.query(uri, null, null, null, null)
        if (returnCursor != null && returnCursor.moveToFirst()) {
            val nameIndex = returnCursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
            val name = returnCursor.getString(nameIndex)
            returnCursor.close()
            return name
        }
        return uri.lastPathSegment ?: "file"
    }
}
