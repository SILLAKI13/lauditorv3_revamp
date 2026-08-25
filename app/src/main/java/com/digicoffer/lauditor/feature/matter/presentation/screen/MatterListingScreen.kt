package com.digicoffer.lauditor.feature.matter.presentation.screen

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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.platform.LocalContext
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
import com.digicoffer.lauditor.Matter.Models.ViewMatterModel
import com.digicoffer.lauditor.R
import com.digicoffer.lauditor.core.designsystem.colors.ColorTokens
import com.digicoffer.lauditor.feature.matter.presentation.components.MatterListingCard
import com.digicoffer.lauditor.feature.matter.presentation.components.MatterSearchBar
import com.digicoffer.lauditor.feature.matter.presentation.viewmodel.MatterViewModel
import com.digicoffer.lauditor.core.ui.common.feedback.AppLoader

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
    val context = LocalContext.current

    if (uiState.toastMessage != null) {
        LaunchedEffect(uiState.toastMessage) {
            android.widget.Toast.makeText(context, uiState.toastMessage, android.widget.Toast.LENGTH_SHORT).show()
            viewModel.clearToastMessage()
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
        MatterSearchBar(
            query = searchInput,
            onQueryChanged = {
                searchInput = it
                viewModel.onSearchQueryChanged(it)
            },
            onSearchClick = {
                viewModel.fetchMatters()
            },
            placeholderHint = "Search Matter"
        )

        // Scrollable matters list or empty state
        if (uiState.matterList.isEmpty() && !uiState.isLoading) {
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
                    color = Color(0xFF6B7280), // dark_grey_new replacement
                    textAlign = TextAlign.Center
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .padding(horizontal = 2.dp)
            ) {
                items(uiState.matterList.size) { index ->
                    val matter = uiState.matterList[index]
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
                        }
                    )
                }
            }
        }

        // Pagination Controls Row at Bottom-End
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .wrapContentHeight()
                .padding(10.dp),
            horizontalArrangement = Arrangement.End,
            verticalAlignment = Alignment.CenterVertically
        ) {
            val hasPrev = !uiState.prevCursor.isNullOrEmpty() && uiState.prevCursor != "null"
            val hasNext = !uiState.nextCursor.isNullOrEmpty() && uiState.nextCursor != "null"

            // Previous Button
            Button(
                onClick = {
                    if (hasPrev) {
                        viewModel.fetchMatters(navPosition = "before", cursor = uiState.prevCursor ?: "")
                    }
                },
                colors = ButtonDefaults.buttonColors(
                    containerColor = ColorTokens.BluePrimary,
                    contentColor = Color.White
                ),
                shape = RoundedCornerShape(10.dp),
                elevation = ButtonDefaults.buttonElevation(defaultElevation = 4.dp),
                modifier = Modifier
                    .alpha(if (hasPrev) 1f else 0.5f)
                    .padding(horizontal = 2.dp)
                    .width(100.dp)
                    .height(40.dp),
                enabled = hasPrev
            ) {
                Text(
                    text = "Previous",
                    fontFamily = GillSans,
                    fontSize = 15.sp,
                    color = Color.White
                )
            }

            Spacer(modifier = Modifier.width(4.dp))

            // Next Button
            Button(
                onClick = {
                    if (hasNext) {
                        viewModel.fetchMatters(navPosition = "after", cursor = uiState.nextCursor ?: "")
                    }
                },
                colors = ButtonDefaults.buttonColors(
                    containerColor = ColorTokens.BluePrimary,
                    contentColor = Color.White
                ),
                shape = RoundedCornerShape(10.dp),
                elevation = ButtonDefaults.buttonElevation(defaultElevation = 4.dp),
                modifier = Modifier
                    .alpha(if (hasNext) 1f else 0.5f)
                    .padding(horizontal = 2.dp)
                    .width(100.dp)
                    .height(40.dp),
                enabled = hasNext
            ) {
                Text(
                    text = "Next",
                    fontFamily = GillSans,
                    fontSize = 15.sp,
                    color = Color.White
                )
            }
        }
    }

    // Confirmation Alert Dialog matching delete_relationship.xml exactly
    if (showOpenCloseDialog && activeMatterForDialog != null) {
        val model = activeMatterForDialog!!
        val isClosed = model.status == "Closed"
        val dialogMessage = if (isClosed) {
            "Are you sure you want to reopen this matter?"
        } else {
            "Are you sure you want to close this matter?"
        }
        val nextStatus = if (isClosed) "Active" else "Closed"

        Dialog(onDismissRequest = { showOpenCloseDialog = false }) {
            Card(
                shape = RoundedCornerShape(6.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 10.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(4.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(4.dp),
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
                                .size(30.dp)
                                .clickable { showOpenCloseDialog = false }
                        )
                    }

                    // Header title: Alert !
                    Text(
                        text = "Alert !",
                        fontFamily = GillSansBold,
                        fontWeight = FontWeight.Bold,
                        fontSize = 20.sp,
                        color = Color.Black,
                        modifier = Modifier.padding(top = 4.dp)
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    // Confirmation message text
                    Text(
                        text = dialogMessage,
                        fontFamily = GillSans,
                        fontSize = 17.sp,
                        color = Color.Black,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 10.dp)
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    // Yes / No Row
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 10.dp, bottom = 8.dp),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // No Button (replicates btn_No background yes_button_red_button)
                        Button(
                            onClick = { showOpenCloseDialog = false },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(0xFFEEEEEE),
                                contentColor = Color.Black
                            ),
                            shape = RoundedCornerShape(4.dp),
                            elevation = ButtonDefaults.buttonElevation(defaultElevation = 4.dp),
                            modifier = Modifier
                                .width(70.dp)
                                .height(40.dp),
                            contentPadding = PaddingValues(0.dp)
                        ) {
                            Text(
                                text = "No",
                                fontFamily = GillSansBold,
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp,
                                color = Color.Black
                            )
                        }

                        Spacer(modifier = Modifier.width(40.dp))

                        // Yes Button (replicates btn_yes background no_button_green_button)
                        Button(
                            onClick = {
                                showOpenCloseDialog = false
                                viewModel.closeOrReopenMatter(
                                    matterId = model.id,
                                    newStatus = nextStatus,
                                    onResult = { success, msg ->
                                        // Result is handled inside ViewModel updating list
                                    }
                                )
                            },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = ColorTokens.BluePrimary,
                                contentColor = Color.White
                            ),
                            shape = RoundedCornerShape(4.dp),
                            elevation = ButtonDefaults.buttonElevation(defaultElevation = 4.dp),
                            modifier = Modifier
                                .width(70.dp)
                                .height(40.dp),
                            contentPadding = PaddingValues(0.dp)
                        ) {
                            Text(
                                text = "Yes",
                                fontFamily = GillSansBold,
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp,
                                color = Color.White
                            )
                        }
                    }
                }
            }
        }
    }
    if (uiState.isLoading) {
        AppLoader()
    }
}
