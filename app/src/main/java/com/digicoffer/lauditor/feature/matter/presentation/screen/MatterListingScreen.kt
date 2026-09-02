package com.digicoffer.lauditor.feature.matter.presentation.screen

import com.digicoffer.lauditor.core.ui.common.animation.fallDownItem
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.viewmodel.compose.viewModel
import com.digicoffer.lauditor.CommonFiles.GlobalFiles.Constants
import com.digicoffer.lauditor.Matter.Models.ViewMatterModel
import com.digicoffer.lauditor.R
import com.digicoffer.lauditor.core.designsystem.colors.ColorTokens
import com.digicoffer.lauditor.core.ui.common.dialogs.AppDialog
import com.digicoffer.lauditor.core.ui.common.feedback.AppLoader
import com.digicoffer.lauditor.core.ui.common.search.AppSearchField
import com.digicoffer.lauditor.feature.matter.presentation.components.MatterListingCard
import com.digicoffer.lauditor.feature.matter.presentation.viewmodel.MatterViewModel

private val GillSans = FontFamily(Font(R.font.gill_sans))
private val GillSansBold = FontFamily(Font(R.font.gill_sans_bold))

@Composable
fun MatterListingScreen(
    viewModel: MatterViewModel = viewModel(),
    onEditMatterClick: (ViewMatterModel) -> Unit,
    onViewTimelineClick: (ViewMatterModel) -> Unit,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsState()

    val listState = androidx.compose.foundation.lazy.rememberLazyListState()

    androidx.compose.runtime.LaunchedEffect(Constants.MATTER_TYPE) {
        viewModel.fetchMatters()
    }

    androidx.compose.runtime.LaunchedEffect(uiState.currentPageList) {
        if (uiState.currentPageList.isNotEmpty()) {
            listState.scrollToItem(0)
        }
    }

    var searchInput by remember { mutableStateOf("") }
    
    // Close / Reopen popup state
    var showOpenCloseDialog by remember { mutableStateOf(false) }
    var activeMatterForDialog by remember { mutableStateOf<ViewMatterModel?>(null) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(ColorTokens.LightBlueBg)
    ) {
        // Search Bar
        AppSearchField(
            value = searchInput,
            onValueChange = {
                searchInput = it
            },
            onSearchClick = {
                viewModel.onSearchQueryChanged(searchInput.trim())
            },
            onSearchKeyboardAction = {
                viewModel.onSearchQueryChanged(searchInput.trim())
            },
            onClearClick = {
                val hadSubmittedSearch = uiState.searchQuery.isNotEmpty()
                searchInput = ""
                if (hadSubmittedSearch) {
                    viewModel.onSearchQueryChanged("")
                }
            },
            searchIcon = {
                Image(
                    painter = painterResource(id = R.drawable.search_grey),
                    contentDescription = "Search",
                    modifier = Modifier
                        .size(20.dp)
                        .padding(end = 4.dp)
                        .clickable {
                            viewModel.onSearchQueryChanged(searchInput.trim())
                        }
                )
            },
            placeholder = "Search Matter",
            modifier = Modifier.padding(10.dp)
        )

        // Scrollable matters list or empty state
        if (uiState.currentPageList.isEmpty() && !uiState.isLoading) {
            // Empty state layout replicating legacy empty_state_view in create_matter.xml
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .padding(horizontal = 32.dp, vertical = 64.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Image(
                    painter = painterResource(id = R.drawable.empty_matter),
                    contentDescription = "Empty",
                    modifier = Modifier.size(130.dp)
                )
                Spacer(modifier = Modifier.height(20.dp))
                Text(
                    text = "No Matters Yet!",
                    fontFamily = GillSansBold,
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    color = Color.Black
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Secure and organize your matters by start creating it.",
                    fontFamily = GillSans,
                    fontSize = 13.sp,
                    color = Color(0xFF6B7280),
                    textAlign = TextAlign.Center
                )
            }
        } else {
            LazyColumn(
                state = listState,
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .padding(horizontal = 2.dp)
            ) {
                itemsIndexed(uiState.currentPageList) { index, matter ->
                    MatterListingCard(
                        matter = matter,
                        onActionClick = { action, model ->
                            when (action) {
                                "Edit Matter" -> onEditMatterClick(model)
                                "View Timeline" -> onViewTimelineClick(model)
                                "Close Matter", "Reopen Matter" -> {
                                    activeMatterForDialog = model
                                    showOpenCloseDialog = true
                                }
                            }
                        },
                        modifier = Modifier.fallDownItem(index = index, triggerKey = uiState.currentPage)
                    )
                }

                // Pagination Controls at the end of the scrollable list
                if (uiState.hasPrev || uiState.hasNext || uiState.currentPageList.isNotEmpty()) {
                    item {
                        val isPrevEnabled = uiState.hasPrev
                        val isNextEnabled = uiState.hasNext

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .wrapContentHeight()
                                .padding(horizontal = 10.dp, vertical = 12.dp),
                            horizontalArrangement = Arrangement.End,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // Previous Button
                            Button(
                                onClick = { viewModel.onPreviousPage() },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = Color(0xFF004D87),
                                    disabledContainerColor = Color.LightGray,
                                    contentColor = Color.White,
                                    disabledContentColor = Color.White
                                ),
                                shape = RoundedCornerShape(8.dp),
                                elevation = ButtonDefaults.buttonElevation(defaultElevation = 2.dp),
                                modifier = Modifier
                                    .padding(horizontal = 2.dp)
                                    .width(100.dp)
                                    .height(38.dp),
                                enabled = isPrevEnabled
                            ) {
                                Text(
                                    text = "<< Prev",
                                    fontFamily = GillSans,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                            }

                            Spacer(modifier = Modifier.width(8.dp))

                            // Next Button
                            Button(
                                onClick = { viewModel.onNextPage() },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = Color(0xFF004D87),
                                    disabledContainerColor = Color.LightGray,
                                    contentColor = Color.White,
                                    disabledContentColor = Color.White
                                ),
                                shape = RoundedCornerShape(8.dp),
                                elevation = ButtonDefaults.buttonElevation(defaultElevation = 2.dp),
                                modifier = Modifier
                                    .padding(horizontal = 2.dp)
                                    .width(100.dp)
                                    .height(38.dp),
                                enabled = isNextEnabled
                            ) {
                                Text(
                                    text = "Next >>",
                                    fontFamily = GillSans,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))
                    }
                }
            }
        }
    }

    // Confirmation Alert Dialog for Close / Reopen Matter
    if (showOpenCloseDialog && activeMatterForDialog != null) {
        val model = activeMatterForDialog!!
        val isClosed = model.status == "Closed"
        val dialogMessage = if (isClosed) {
            "Are you sure you want to reopen this matter?"
        } else {
            "Are you sure you want to close this matter?"
        }
        val dialogTitle = if (isClosed) "Reopen Matter" else "Close Matter"
        val nextStatus = if (isClosed) "Active" else "Closed"

        Dialog(onDismissRequest = { showOpenCloseDialog = false }) {
            Card(
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Close icon top right
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End
                    ) {
                        Image(
                            painter = painterResource(id = R.drawable.simple_cancel),
                            contentDescription = "Cancel",
                            modifier = Modifier
                                .size(24.dp)
                                .clickable { showOpenCloseDialog = false }
                        )
                    }

                    // Header title in Primary Blue
                    Text(
                        text = dialogTitle,
                        fontFamily = GillSansBold,
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp,
                        color = ColorTokens.BluePrimary,
                        modifier = Modifier.padding(top = 2.dp)
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    // Confirmation message text
                    Text(
                        text = dialogMessage,
                        fontFamily = GillSans,
                        fontSize = 15.sp,
                        color = Color.Black,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp)
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // No / Yes Buttons Row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // No Button
                        Button(
                            onClick = { showOpenCloseDialog = false },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(0xFFECEFF1),
                                contentColor = Color.Black
                            ),
                            shape = RoundedCornerShape(8.dp),
                            elevation = ButtonDefaults.buttonElevation(defaultElevation = 0.dp),
                            modifier = Modifier
                                .width(100.dp)
                                .height(40.dp),
                            contentPadding = PaddingValues(0.dp)
                        ) {
                            Text(
                                text = "No",
                                fontFamily = GillSansBold,
                                fontWeight = FontWeight.Bold,
                                fontSize = 15.sp,
                                color = Color.Black
                            )
                        }

                        Spacer(modifier = Modifier.width(20.dp))

                        // Yes Button
                        Button(
                            onClick = {
                                showOpenCloseDialog = false
                                viewModel.closeOrReopenMatter(
                                    matterId = model.id,
                                    newStatus = nextStatus,
                                    onResult = { _, _ -> }
                                )
                            },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = ColorTokens.BluePrimary,
                                contentColor = Color.White
                            ),
                            shape = RoundedCornerShape(8.dp),
                            elevation = ButtonDefaults.buttonElevation(defaultElevation = 0.dp),
                            modifier = Modifier
                                .width(100.dp)
                                .height(40.dp),
                            contentPadding = PaddingValues(0.dp)
                        ) {
                            Text(
                                text = "Yes",
                                fontFamily = GillSansBold,
                                fontWeight = FontWeight.Bold,
                                fontSize = 15.sp,
                                color = Color.White
                            )
                        }
                    }
                }
            }
        }
    }

    // AppDialog for API alerts
    if (uiState.alertMessage != null) {
        AppDialog(
            title = uiState.alertTitle ?: "Alert",
            onDismiss = { viewModel.dismissAlert() },
            onConfirm = { viewModel.dismissAlert() },
            confirmText = "OK"
        ) {
            Text(
                text = uiState.alertMessage ?: "",
                fontFamily = GillSans,
                fontSize = 15.sp,
                textAlign = TextAlign.Center,
                color = Color.Black
            )
        }
    }

    if (uiState.isLoading) {
        AppLoader()
    }
}
