package com.digicoffer.lauditor.feature.relationships.presentation.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.platform.LocalContext
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import com.digicoffer.lauditor.R
import com.digicoffer.lauditor.Relationships.Model.CountriesDO
import com.digicoffer.lauditor.Relationships.Model.IndividualModel
import com.digicoffer.lauditor.Relationships.Model.EntityModel
import com.digicoffer.lauditor.Webservice.AsyncTaskCompleteListener
import com.digicoffer.lauditor.Webservice.HttpResultDo
import com.digicoffer.lauditor.Webservice.CommonApiHelper.WebServiceHelper
import org.json.JSONObject

private val GillSans = FontFamily(
    Font(R.font.gill_sans)
)

@Composable
fun FormLabel(text: String, isMandatory: Boolean = false) {
    Text(
        text = buildAnnotatedString {
            append(text)
            if (isMandatory) {
                withStyle(style = SpanStyle(color = Color.Red)) {
                    append(" *")
                }
            }
        },
        color = Color(0xFF004D87),
        fontFamily = GillSans,
        fontWeight = FontWeight.Normal,
        fontSize = 15.sp
    )
}

@Composable
fun RelationshipCustomTextField(
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    enabled: Boolean = true,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
    modifier: Modifier = Modifier
) {
    val inputBorderColor = Color(0xFFC0C0C0)
    val inputBgColor = Color(0xFFF9FAFB)
    val textStyle = TextStyle(
        fontSize = 15.sp,
        fontFamily = GillSans,
        color = Color.Black
    )

    BasicTextField(
        value = value,
        onValueChange = onValueChange,
        textStyle = textStyle,
        keyboardOptions = keyboardOptions,
        singleLine = true,
        enabled = enabled,
        cursorBrush = SolidColor(Color.Black),
        modifier = modifier
            .fillMaxWidth()
            .background(inputBgColor, shape = RoundedCornerShape(6.dp))
            .border(width = 0.5.dp, color = inputBorderColor, shape = RoundedCornerShape(6.dp)),
        decorationBox = { innerTextField ->
            Box(
                modifier = Modifier.padding(10.dp)
            ) {
                if (value.isEmpty()) {
                    Text(
                        text = placeholder,
                        style = textStyle.copy(color = Color(0xFFA0A0A0))
                    )
                }
                innerTextField()
            }
        }
    )
}

@Composable
fun RelationshipFormCard(
    countriesList: List<CountriesDO>,
    onCancel: () -> Unit,
    onSubmitRequest: (JSONObject, (Boolean, String) -> Unit) -> Unit,
    onValidationError: (String) -> Unit,
    onTitleChange: (String) -> Unit = {}
) {
    val context = LocalContext.current
    var activeTab by remember { mutableStateOf("Individual") } // "Individual", "Entity", "Corporate"

    // Search candidates search bar
    var searchQuery by remember { mutableStateOf("") }

    // Individual Fields
    var firstName by remember { mutableStateOf("") }
    var lastName by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var confirmEmail by remember { mutableStateOf("") }
    var mobile by remember { mutableStateOf("") }
    var selectedCountry by remember { mutableStateOf<CountriesDO?>(null) }
    var countryMenuExpanded by remember { mutableStateOf(false) }

    // Entity & Corporate Fields
    var entityName by remember { mutableStateOf("") }
    var contactPerson by remember { mutableStateOf("") }
    var contactPhone by remember { mutableStateOf("") }

    // Legacy parity behavioral & state locks
    var isFormEnabled by remember { mutableStateOf(false) }
    var isFieldsEditable by remember { mutableStateOf(false) }
    var isConfirmEmailVisible by remember { mutableStateOf(true) }
    var searchStatusMessage by remember { mutableStateOf<String?>(null) }
    var isSearchStatusSuccess by remember { mutableStateOf(false) }
    var isLoading by remember { mutableStateOf(false) }

    // Confirmation Alert States
    var showConfirmDialog by remember { mutableStateOf(false) }
    var showSuccessDialog by remember { mutableStateOf(false) }
    var successDialogMessage by remember { mutableStateOf("") }
    var showErrorDialog by remember { mutableStateOf(false) }
    var errorDialogMessage by remember { mutableStateOf("") }
    var savedPayload by remember { mutableStateOf<JSONObject?>(null) }

    // Autocomplete list states
    var individualSuggestions by remember { mutableStateOf<List<IndividualModel>>(emptyList()) }
    var showIndividualSuggestions by remember { mutableStateOf(false) }
    var individualId by remember { mutableStateOf("") }

    var entityCorporateSuggestions by remember { mutableStateOf<List<EntityModel>>(emptyList()) }
    var showEntityCorporateSuggestions by remember { mutableStateOf(false) }
    var entity_id by remember { mutableStateOf("") }

    val filteredEntityCorporate = remember(searchQuery, entityCorporateSuggestions) {
        if (searchQuery.trim().isEmpty()) {
            entityCorporateSuggestions
        } else {
            entityCorporateSuggestions.filter {
                it.name?.lowercase()?.contains(searchQuery.lowercase().trim()) == true
            }
        }
    }

    fun performIndividualSearch(query: String) {
        isLoading = true
        val postData = JSONObject()
        postData.put("search", query.trim())
        WebServiceHelper.callHttpWebService(
            object : AsyncTaskCompleteListener {
                override fun onAsyncTaskComplete(httpResult: HttpResultDo) {
                    isLoading = false
                    if (httpResult.result == WebServiceHelper.ServiceCallStatus.Success) {
                        try {
                            val result = JSONObject(httpResult.responseContent ?: "{}")
                            val data = result.optJSONArray("data") ?: org.json.JSONArray()
                            val list = mutableListOf<IndividualModel>()
                            for (i in 0 until data.length()) {
                                val jsonObject = data.getJSONObject(i)
                                val model = IndividualModel().apply {
                                    id = jsonObject.optString("id")
                                    first_name = jsonObject.optString("first_name", "")
                                    last_name = jsonObject.optString("last_name", "")
                                    email = jsonObject.optString("email")
                                    confirmEmail = jsonObject.optString("email")
                                    mobile = jsonObject.optString("mobile")
                                    val rawFirstName = jsonObject.optString("first_name", "").trim()
                                    val rawLastName = jsonObject.optString("last_name", "").trim()
                                    name = when {
                                        rawFirstName.isNotEmpty() && rawLastName.isNotEmpty() -> "$rawFirstName $rawLastName"
                                        rawFirstName.isNotEmpty() -> rawFirstName
                                        rawLastName.isNotEmpty() -> rawLastName
                                        else -> jsonObject.optString("email", "Unknown")
                                    }
                                    country = jsonObject.optString("country")
                                }
                                list.add(model)
                            }
                            individualSuggestions = list
                            if (list.isNotEmpty()) {
                                showIndividualSuggestions = true
                                isFormEnabled = true
                            } else {
                                isFormEnabled = true
                                isFieldsEditable = true
                                isConfirmEmailVisible = true
                                firstName = ""
                                lastName = ""
                                email = ""
                                confirmEmail = ""
                                mobile = ""
                                selectedCountry = null
                                searchStatusMessage = "$query - not found. Please fill in the details below to send relationship invite."
                                isSearchStatusSuccess = false
                            }
                        } catch (e: Exception) {
                            e.printStackTrace()
                        }
                    }
                }
                override fun onClick(view: android.view.View) {}
            },
            context,
            WebServiceHelper.RestMethodType.POST,
            "v3/relationship/search/consumer",
            "Search Consumer",
            postData.toString()
        )
    }

    fun selectIndividualSuggestion(selected: IndividualModel) {
        individualId = selected.id ?: ""
        firstName = selected.first_name ?: ""
        lastName = selected.last_name ?: ""
        email = selected.email ?: ""
        confirmEmail = selected.email ?: ""
        mobile = selected.mobile ?: ""
        
        val countryMatch = countriesList.find { 
            it.name?.trim()?.equals(selected.country?.trim() ?: "", ignoreCase = true) == true || 
            it.value?.trim()?.equals(selected.country?.trim() ?: "", ignoreCase = true) == true
        }
        selectedCountry = countryMatch ?: CountriesDO().apply {
            name = selected.country
            value = selected.country
        }
        
        isConfirmEmailVisible = false
        isFieldsEditable = false
        isFormEnabled = true
        searchStatusMessage = "Individual ${selected.name ?: ""} - found!"
        isSearchStatusSuccess = true
        showIndividualSuggestions = false
    }

    fun fetchEntityCorporateList(clientType: String) {
        val endpoint = if (clientType == "Corporate") "v2/relationship/search/corporate" else "v2/relationship/search/entity"
        val requestType = if (clientType == "Corporate") "Corporate List" else "Entities List"
        
        WebServiceHelper.callHttpWebService(
            object : AsyncTaskCompleteListener {
                override fun onAsyncTaskComplete(httpResult: HttpResultDo) {
                    if (httpResult.result == WebServiceHelper.ServiceCallStatus.Success) {
                        try {
                            val result = JSONObject(httpResult.responseContent ?: "{}")
                            val data = result.optJSONArray("data") ?: org.json.JSONArray()
                            val list = mutableListOf<EntityModel>()
                            for (i in 0 until data.length()) {
                                val jsonObject = data.getJSONObject(i)
                                val model = EntityModel().apply {
                                    entityID = jsonObject.optString("entityId")
                                    name = jsonObject.optString("name")
                                    contactName = jsonObject.optString("contactName")
                                }
                                list.add(model)
                            }
                            entityCorporateSuggestions = list
                            showEntityCorporateSuggestions = true
                        } catch (e: Exception) {
                            e.printStackTrace()
                        }
                    }
                }
                override fun onClick(view: android.view.View) {}
            },
            context,
            WebServiceHelper.RestMethodType.GET,
            endpoint,
            requestType,
            JSONObject().toString()
        )
    }

    fun fetchEntityCorporateDetails(entityId: String) {
        isLoading = true
        entity_id = entityId
        WebServiceHelper.callHttpWebService(
            object : AsyncTaskCompleteListener {
                override fun onAsyncTaskComplete(httpResult: HttpResultDo) {
                    isLoading = false
                    if (httpResult.result == WebServiceHelper.ServiceCallStatus.Success) {
                        try {
                            val result = JSONObject(httpResult.responseContent ?: "{}")
                            val data = result.getJSONObject("data")
                            
                            val entityNameVal = data.optString("entityName", "")
                            val emailVal = data.optString("email", "")
                            val contactPersonVal = data.optString("contactPerson", "")
                            val contactPhoneVal = data.optString("contactPhone", "")
                            val countryVal = data.optString("country", "")
                            
                            entityName = entityNameVal
                            email = emailVal
                            confirmEmail = emailVal
                            contactPerson = contactPersonVal
                            contactPhone = contactPhoneVal
                            
                            val countryMatch = countriesList.find { 
                                it.name?.trim()?.equals(countryVal.trim(), ignoreCase = true) == true || 
                                it.value?.trim()?.equals(countryVal.trim(), ignoreCase = true) == true
                            }
                            selectedCountry = countryMatch ?: CountriesDO().apply {
                                name = countryVal
                                value = countryVal
                            }
                            
                            isConfirmEmailVisible = false
                            isFieldsEditable = false
                            isFormEnabled = true
                            searchStatusMessage = if (activeTab == "Corporate") "Corporate $entityNameVal - found!" else "Entity $entityNameVal - found!"
                            isSearchStatusSuccess = true
                        } catch (e: Exception) {
                            e.printStackTrace()
                        }
                    }
                }
                override fun onClick(view: android.view.View) {}
            },
            context,
            WebServiceHelper.RestMethodType.GET,
            "v2/relationship/entity/$entityId",
            "Search Entity",
            JSONObject().toString()
        )
    }

    LaunchedEffect(activeTab) {
        searchQuery = ""
        firstName = ""
        lastName = ""
        email = ""
        confirmEmail = ""
        mobile = ""
        selectedCountry = null
        countryMenuExpanded = false
        
        entityName = ""
        contactPerson = ""
        contactPhone = ""
        
        isFormEnabled = false
        isFieldsEditable = false
        isConfirmEmailVisible = true
        searchStatusMessage = null
        isSearchStatusSuccess = false
        
        individualSuggestions = emptyList()
        showIndividualSuggestions = false
        individualId = ""
        
        entityCorporateSuggestions = emptyList()
        showEntityCorporateSuggestions = false
        entity_id = ""

        when (activeTab) {
            "Individual" -> onTitleChange("Add Relationships")
            "Entity" -> {
                onTitleChange("Entity")
                fetchEntityCorporateList("Entity")
            }
            "Corporate" -> {
                onTitleChange("Corporate")
                fetchEntityCorporateList("Corporate")
            }
        }
    }

    // Trigger individual search as user types or on text changes in Individual tab
    LaunchedEffect(searchQuery) {
        if (activeTab == "Individual" && searchQuery.trim().isNotEmpty()) {
            performIndividualSearch(searchQuery)
        } else if (activeTab == "Individual") {
            individualSuggestions = emptyList()
            showIndividualSuggestions = false
        }
    }

    val scrollState = rememberScrollState()

    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
                .verticalScroll(scrollState)
        ) {
            // Top Page Title and View Relationships trigger button (outside card container)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Add Relationships - $activeTab",
                    color = Color(0xFF004D87),
                    fontFamily = GillSans,
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp
                )

                Surface(
                    modifier = Modifier
                        .height(40.dp)
                        .clickable { onCancel() },
                    shape = RoundedCornerShape(10.dp),
                    color = Color.White,
                    shadowElevation = 2.dp
                ) {
                    Row(
                        modifier = Modifier.padding(start = 4.dp, end = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(30.dp)
                                .background(Color(0xFF004D87), shape = CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                painter = painterResource(id = R.drawable.eye_icon),
                                contentDescription = "View",
                                tint = Color.White,
                                modifier = Modifier.size(14.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "View Relationships",
                            color = Color(0xFF004D87),
                            fontFamily = GillSans,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Normal
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Single White Card Container containing Tabs + Search + Form fields
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(width = 0.5.dp, color = Color(0xFFDDDDDE), shape = RoundedCornerShape(8.dp)),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                shape = RoundedCornerShape(8.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(10.dp)
                ) {
                    // 1. Separate Tabs
                    Row(
                        modifier = Modifier.fillMaxWidth().height(40.dp),
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        val tabs = listOf("Individual", "Entity", "Corporate")
                        tabs.forEach { tab ->
                            val isSelected = activeTab == tab
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .fillMaxHeight()
                                    .border(
                                        width = 0.5.dp,
                                        color = if (isSelected) Color(0xFF004D87) else Color(0xFFC0C0C0),
                                        shape = RoundedCornerShape(8.dp)
                                    )
                                    .background(
                                        color = if (isSelected) Color(0xFF004D87) else Color.White,
                                        shape = RoundedCornerShape(8.dp)
                                    )
                                    .clickable { activeTab = tab },
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = tab,
                                    color = if (isSelected) Color.White else Color.Black,
                                    fontFamily = GillSans,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // 2. Search input section
                    Box(modifier = Modifier.fillMaxWidth()) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(modifier = Modifier.weight(1f)) {
                                RelationshipCustomTextField(
                                    value = searchQuery,
                                    onValueChange = {
                                        searchQuery = it
                                        if (activeTab != "Individual") {
                                            showEntityCorporateSuggestions = true
                                        }
                                    },
                                    placeholder = "Search",
                                    modifier = Modifier.onFocusChanged { focusState ->
                                        if (focusState.isFocused) {
                                            if (activeTab == "Individual") {
                                                if (searchQuery.trim().isNotEmpty()) {
                                                    performIndividualSearch(searchQuery)
                                                }
                                            } else {
                                                fetchEntityCorporateList(activeTab)
                                            }
                                        }
                                    }.clickable {
                                        if (activeTab != "Individual") {
                                            fetchEntityCorporateList(activeTab)
                                        }
                                    }
                                )
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                            Button(
                                onClick = {
                                    if (searchQuery.trim().isEmpty()) {
                                        onValidationError("Please check the search field")
                                        return@Button
                                    }
                                    if (activeTab == "Individual") {
                                        performIndividualSearch(searchQuery)
                                        searchQuery = "" // Clear search text field
                                    } else {
                                        val match = entityCorporateSuggestions.find {
                                            it.name?.trim()?.equals(searchQuery.trim(), ignoreCase = true) == true
                                        }
                                        if (match != null) {
                                            fetchEntityCorporateDetails(match.entityID ?: "")
                                            searchQuery = "" // Clear search text field
                                        } else {
                                            if (activeTab == "Corporate") {
                                                isFormEnabled = false
                                                isFieldsEditable = false
                                                isConfirmEmailVisible = false
                                                entityName = ""
                                                email = ""
                                                confirmEmail = ""
                                                contactPerson = ""
                                                contactPhone = ""
                                                selectedCountry = null
                                                searchStatusMessage = "$searchQuery - not found."
                                                isSearchStatusSuccess = false
                                                showEntityCorporateSuggestions = false
                                                searchQuery = "" // Clear search text field
                                            } else {
                                                isFormEnabled = true
                                                isFieldsEditable = true
                                                isConfirmEmailVisible = true
                                                entityName = searchQuery
                                                email = ""
                                                confirmEmail = ""
                                                contactPerson = ""
                                                contactPhone = ""
                                                selectedCountry = null
                                                searchStatusMessage = "$searchQuery - not found. Please fill in the details below to send relationship invite."
                                                isSearchStatusSuccess = false
                                                showEntityCorporateSuggestions = false
                                                searchQuery = "" // Clear search text field
                                            }
                                        }
                                    }
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF004D87)),
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier.height(40.dp)
                            ) {
                                Text(
                                    text = "Search",
                                    color = Color.White,
                                    fontFamily = GillSans,
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }

                        // Autocomplete overlay popups
                        if (activeTab == "Individual" && showIndividualSuggestions && individualSuggestions.isNotEmpty()) {
                            androidx.compose.ui.window.Popup(
                                offset = androidx.compose.ui.unit.IntOffset(0, 170),
                                onDismissRequest = { showIndividualSuggestions = false },
                                properties = androidx.compose.ui.window.PopupProperties(focusable = true)
                            ) {
                                Card(
                                    modifier = Modifier
                                        .fillMaxWidth(0.9f)
                                        .heightIn(max = 250.dp)
                                        .border(width = 0.5.dp, color = Color(0xFFC0C0C0), shape = RoundedCornerShape(8.dp)),
                                    colors = CardDefaults.cardColors(containerColor = Color.White),
                                    shape = RoundedCornerShape(8.dp),
                                    elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
                                ) {
                                    LazyColumn(modifier = Modifier.fillMaxWidth()) {
                                        items(individualSuggestions) { item ->
                                            Column(
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .clickable { selectIndividualSuggestion(item) }
                                                    .padding(10.dp)
                                            ) {
                                                Text(
                                                    text = item.name ?: "",
                                                    color = Color.Black,
                                                    fontFamily = GillSans,
                                                    fontSize = 15.sp,
                                                    fontWeight = FontWeight.Bold
                                                )
                                                Spacer(modifier = Modifier.height(2.dp))
                                                Text(
                                                    text = com.digicoffer.lauditor.CommonFiles.GlobalFiles.AndroidUtils.maskEmail(item.email),
                                                    color = Color(0xFF707070),
                                                    fontFamily = GillSans,
                                                    fontSize = 13.sp
                                                )
                                                Spacer(modifier = Modifier.height(2.dp))
                                                Text(
                                                    text = com.digicoffer.lauditor.CommonFiles.GlobalFiles.AndroidUtils.maskPhoneNumber(item.mobile),
                                                    color = Color(0xFF707070),
                                                    fontFamily = GillSans,
                                                    fontSize = 13.sp
                                                )
                                            }
                                            HorizontalDivider(thickness = 0.5.dp, color = Color(0xFFDDDDDE))
                                        }
                                    }
                                }
                            }
                        }

                        if (activeTab != "Individual" && showEntityCorporateSuggestions && filteredEntityCorporate.isNotEmpty()) {
                            androidx.compose.ui.window.Popup(
                                offset = androidx.compose.ui.unit.IntOffset(0, 170),
                                onDismissRequest = { showEntityCorporateSuggestions = false },
                                properties = androidx.compose.ui.window.PopupProperties(focusable = true)
                            ) {
                                Card(
                                    modifier = Modifier
                                        .fillMaxWidth(0.9f)
                                        .heightIn(max = 200.dp)
                                        .border(width = 0.5.dp, color = Color(0xFFC0C0C0), shape = RoundedCornerShape(8.dp)),
                                    colors = CardDefaults.cardColors(containerColor = Color.White),
                                    shape = RoundedCornerShape(8.dp),
                                    elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
                                ) {
                                    LazyColumn(modifier = Modifier.fillMaxWidth()) {
                                        items(filteredEntityCorporate) { item ->
                                            Column(
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .clickable {
                                                        fetchEntityCorporateDetails(item.entityID ?: "")
                                                        showEntityCorporateSuggestions = false
                                                    }
                                                    .padding(12.dp)
                                            ) {
                                                Text(
                                                    text = item.name ?: "",
                                                    color = Color.Black,
                                                    fontFamily = GillSans,
                                                    fontSize = 15.sp
                                                )
                                            }
                                            HorizontalDivider(thickness = 0.5.dp, color = Color(0xFFDDDDDE))
                                        }
                                    }
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    if (isLoading) {
                        Box(
                            modifier = Modifier.fillMaxWidth().height(60.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator(color = Color(0xFF004D87))
                        }
                        Spacer(modifier = Modifier.height(10.dp))
                    }

                    // Found / Not Found Status messages
                    searchStatusMessage?.let { msg ->
                        Text(
                            text = msg,
                            color = if (isSearchStatusSuccess) Color(0xFF4CAF50) else Color.Red,
                            fontFamily = GillSans,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(vertical = 4.dp)
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                    }

                    // Reusable Country Selector component
                    @Composable
                    fun CountrySelector() {
                        FormLabel(text = "Country", isMandatory = true)
                        Spacer(modifier = Modifier.height(6.dp))
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(Color(0xFFF9FAFB), shape = RoundedCornerShape(6.dp))
                                .border(width = 0.5.dp, color = Color(0xFFC0C0C0), shape = RoundedCornerShape(6.dp))
                                .clickable(enabled = isFormEnabled && isFieldsEditable) { countryMenuExpanded = !countryMenuExpanded }
                                .padding(10.dp),
                            contentAlignment = Alignment.CenterStart
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = selectedCountry?.name ?: "Select Country",
                                    color = if (selectedCountry != null) Color.Black else Color(0xFFA0A0A0),
                                    fontFamily = GillSans,
                                    fontSize = 15.sp
                                )
                                Icon(
                                    painter = painterResource(id = R.drawable.drop_down_blue),
                                    contentDescription = "Open Country List",
                                    tint = Color(0xFF004D87),
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }

                        if (countryMenuExpanded && isFormEnabled && isFieldsEditable) {
                            Spacer(modifier = Modifier.height(4.dp))
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(150.dp)
                                    .background(Color(0xFFF9FAFB), shape = RoundedCornerShape(6.dp))
                                    .border(width = 0.5.dp, color = Color(0xFFC0C0C0), shape = RoundedCornerShape(6.dp))
                            ) {
                                LazyColumn(
                                    modifier = Modifier.fillMaxSize().padding(horizontal = 4.dp, vertical = 4.dp)
                                ) {
                                    items(countriesList) { country ->
                                        Column(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .clickable {
                                                    selectedCountry = country
                                                    countryMenuExpanded = false
                                                }
                                                .padding(vertical = 10.dp, horizontal = 10.dp)
                                        ) {
                                            Text(
                                                text = country.name ?: "",
                                                color = Color.Black,
                                                fontFamily = GillSans,
                                                fontSize = 15.sp
                                            )
                                        }
                                        HorizontalDivider(thickness = 1.dp, color = Color(0xFFA0A0A0))
                                    }
                                }
                            }
                        }
                    }

                    // 3. Form Input Fields (with initial alpha dimming state)
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .graphicsLayer(alpha = if (isFormEnabled) 1.0f else 0.5f)
                    ) {
                        if (activeTab == "Individual") {
                            // Individual Tab Order: First Name, Last Name, Email, Confirm Email, Mobile, Country
                            FormLabel(text = "First Name", isMandatory = true)
                            Spacer(modifier = Modifier.height(6.dp))
                            RelationshipCustomTextField(
                                value = firstName,
                                onValueChange = { firstName = it },
                                placeholder = "First Name",
                                enabled = isFormEnabled && isFieldsEditable
                            )
                            Spacer(modifier = Modifier.height(10.dp))

                            FormLabel(text = "Last Name", isMandatory = true)
                            Spacer(modifier = Modifier.height(6.dp))
                            RelationshipCustomTextField(
                                value = lastName,
                                onValueChange = { lastName = it },
                                placeholder = "Last Name",
                                enabled = isFormEnabled && isFieldsEditable
                            )
                            Spacer(modifier = Modifier.height(10.dp))

                            FormLabel(text = "Email", isMandatory = true)
                            Spacer(modifier = Modifier.height(6.dp))
                            RelationshipCustomTextField(
                                value = email,
                                onValueChange = { email = it },
                                placeholder = "Email",
                                enabled = isFormEnabled && isFieldsEditable
                            )
                            Spacer(modifier = Modifier.height(10.dp))

                            if (isConfirmEmailVisible) {
                                FormLabel(text = "Confirm Email", isMandatory = true)
                                Spacer(modifier = Modifier.height(6.dp))
                                RelationshipCustomTextField(
                                    value = confirmEmail,
                                    onValueChange = { confirmEmail = it },
                                    placeholder = "Confirm Email",
                                    enabled = isFormEnabled && isFieldsEditable
                                )
                                Spacer(modifier = Modifier.height(10.dp))
                            }

                            FormLabel(text = "Mobile", isMandatory = false)
                            Spacer(modifier = Modifier.height(6.dp))
                            RelationshipCustomTextField(
                                value = mobile,
                                onValueChange = { mobile = it },
                                placeholder = "Mobile",
                                enabled = isFormEnabled && isFieldsEditable
                            )
                            Spacer(modifier = Modifier.height(10.dp))

                            CountrySelector()

                        } else if (activeTab == "Entity") {
                            // Entity Tab Order: Entity Name, Contact Person, Country, Email, Confirm Email, Contact Phone Number
                            FormLabel(text = "Entity Name", isMandatory = true)
                            Spacer(modifier = Modifier.height(6.dp))
                            RelationshipCustomTextField(
                                value = entityName,
                                onValueChange = { entityName = it },
                                placeholder = "Entity Name",
                                enabled = isFormEnabled && isFieldsEditable
                            )
                            Spacer(modifier = Modifier.height(10.dp))

                            FormLabel(text = "Contact Person", isMandatory = true)
                            Spacer(modifier = Modifier.height(6.dp))
                            RelationshipCustomTextField(
                                value = contactPerson,
                                onValueChange = { contactPerson = it },
                                placeholder = "Contact Person",
                                enabled = isFormEnabled && isFieldsEditable
                            )
                            Spacer(modifier = Modifier.height(10.dp))

                            CountrySelector()
                            Spacer(modifier = Modifier.height(10.dp))

                            FormLabel(text = "Email", isMandatory = true)
                            Spacer(modifier = Modifier.height(6.dp))
                            RelationshipCustomTextField(
                                value = email,
                                onValueChange = { email = it },
                                placeholder = "Email",
                                enabled = isFormEnabled && isFieldsEditable
                            )
                            Spacer(modifier = Modifier.height(10.dp))

                            if (isConfirmEmailVisible) {
                                FormLabel(text = "Confirm Email", isMandatory = true)
                                Spacer(modifier = Modifier.height(6.dp))
                                RelationshipCustomTextField(
                                    value = confirmEmail,
                                    onValueChange = { confirmEmail = it },
                                    placeholder = "Confirm Email",
                                    enabled = isFormEnabled && isFieldsEditable
                                )
                                Spacer(modifier = Modifier.height(10.dp))
                            }

                            FormLabel(text = "Contact Phone Number", isMandatory = true)
                            Spacer(modifier = Modifier.height(6.dp))
                            RelationshipCustomTextField(
                                value = contactPhone,
                                onValueChange = { contactPhone = it },
                                placeholder = "Contact Phone Number",
                                enabled = isFormEnabled && isFieldsEditable
                            )
                            Spacer(modifier = Modifier.height(10.dp))

                        } else {
                            // Corporate Tab Order: Entity Name, Contact Person, Country, Email, Mobile
                            FormLabel(text = "Entity Name", isMandatory = true)
                            Spacer(modifier = Modifier.height(6.dp))
                            RelationshipCustomTextField(
                                value = entityName,
                                onValueChange = { entityName = it },
                                placeholder = "Entity Name",
                                enabled = isFormEnabled && isFieldsEditable
                            )
                            Spacer(modifier = Modifier.height(10.dp))

                            FormLabel(text = "Contact Person", isMandatory = true)
                            Spacer(modifier = Modifier.height(6.dp))
                            RelationshipCustomTextField(
                                value = contactPerson,
                                onValueChange = { contactPerson = it },
                                placeholder = "Contact Person",
                                enabled = isFormEnabled && isFieldsEditable
                            )
                            Spacer(modifier = Modifier.height(10.dp))

                            CountrySelector()
                            Spacer(modifier = Modifier.height(10.dp))

                            FormLabel(text = "Email", isMandatory = true)
                            Spacer(modifier = Modifier.height(6.dp))
                            RelationshipCustomTextField(
                                value = email,
                                onValueChange = { email = it },
                                placeholder = "Email",
                                enabled = isFormEnabled && isFieldsEditable
                            )
                            Spacer(modifier = Modifier.height(10.dp))

                            FormLabel(text = "Mobile", isMandatory = true)
                            Spacer(modifier = Modifier.height(6.dp))
                            RelationshipCustomTextField(
                                value = contactPhone,
                                onValueChange = { contactPhone = it },
                                placeholder = "Mobile",
                                enabled = isFormEnabled && isFieldsEditable
                            )
                            Spacer(modifier = Modifier.height(10.dp))
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        // Bottom Buttons row
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceAround
                        ) {
                            val isFormComplete = if (activeTab == "Individual") {
                                individualId.isNotEmpty() || (firstName.isNotBlank() && lastName.isNotBlank() && email.isNotBlank() && (!isConfirmEmailVisible || confirmEmail.isNotBlank()) && selectedCountry != null)
                            } else if (activeTab == "Entity") {
                                entity_id.isNotEmpty() || (entityName.isNotBlank() && contactPerson.isNotBlank() && contactPhone.isNotBlank() && email.isNotBlank() && (!isConfirmEmailVisible || confirmEmail.isNotBlank()) && selectedCountry != null)
                            } else {
                                entity_id.isNotEmpty() || (entityName.isNotBlank() && contactPerson.isNotBlank() && contactPhone.isNotBlank() && email.isNotBlank() && selectedCountry != null)
                            }

                            Button(
                                onClick = onCancel,
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFE0E0E0)),
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier.width(120.dp).height(40.dp)
                            ) {
                                Text(
                                    text = "Cancel",
                                    color = Color.Black,
                                    fontFamily = GillSans,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Normal
                                )
                            }

                            Button(
                                onClick = {
                                    if (!isFormEnabled) return@Button
                                    
                                    val isExisting = (activeTab == "Individual" && individualId.isNotEmpty()) || (activeTab != "Individual" && entity_id.isNotEmpty())
                                    
                                    if (!isExisting) {
                                        if (activeTab == "Individual") {
                                            if (firstName.isBlank()) {
                                                onValidationError("Please enter First Name")
                                                return@Button
                                            }
                                            if (lastName.isBlank()) {
                                                onValidationError("Please enter Last Name")
                                                return@Button
                                            }
                                        } else {
                                            if (entityName.isBlank()) {
                                                onValidationError(if (activeTab == "Entity") "Please enter Entity Name" else "Please enter Corporate Name")
                                                return@Button
                                            }
                                            if (contactPerson.isBlank()) {
                                                onValidationError("Please enter Contact Person")
                                                return@Button
                                            }
                                            if (activeTab == "Entity" && contactPhone.isBlank()) {
                                                onValidationError("Please enter Contact Phone Number")
                                                return@Button
                                            }
                                            if (activeTab == "Corporate" && contactPhone.isBlank()) {
                                                onValidationError("Please enter Mobile")
                                                return@Button
                                            }
                                        }

                                        if (email.isBlank()) {
                                            onValidationError("Please enter Email")
                                            return@Button
                                        }
                                        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                                            onValidationError("Please check Email format")
                                            return@Button
                                        }
                                        if (isConfirmEmailVisible && activeTab != "Corporate") {
                                            if (confirmEmail.isBlank()) {
                                                onValidationError("Please enter Confirm Email")
                                                return@Button
                                            }
                                            if (confirmEmail != email) {
                                                onValidationError("Confirm Email does not match Email")
                                                return@Button
                                            }
                                        }
                                        if (selectedCountry == null) {
                                            onValidationError("Please select Country")
                                            return@Button
                                        }
                                    }

                                    // Assemble Request Payload including client type information
                                    val payload = JSONObject().apply {
                                        if (activeTab == "Individual") {
                                            if (individualId.isNotEmpty()) {
                                                put("consumerId", individualId)
                                            } else {
                                                put("first_name", firstName)
                                                put("last_name", lastName)
                                                put("mobile", mobile)
                                            }
                                            put("client_type", "individual")
                                        } else {
                                            if (entity_id.isNotEmpty()) {
                                                put("entityId", entity_id)
                                            } else {
                                                put("fullname", entityName)
                                                put("contact_person", contactPerson)
                                                put("contact_phone", contactPhone)
                                            }
                                            put("client_type", activeTab.lowercase())
                                        }
                                        put("email", email)
                                        put("country", selectedCountry?.name)
                                    }
                                    savedPayload = payload
                                    showConfirmDialog = true
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF004D87)),
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier
                                    .width(120.dp)
                                    .height(40.dp)
                                    .graphicsLayer(alpha = if (isFormComplete && isFormEnabled) 1.0f else 0.5f)
                            ) {
                                Text(
                                    text = "Send Request",
                                    color = Color.White,
                                    fontFamily = GillSans,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Normal
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    // Confirmation dialogues
    if (showConfirmDialog) {
        AlertDialog(
            onDismissRequest = { showConfirmDialog = false },
            title = {
                Text(
                    text = "Confirmation",
                    color = Color(0xFF004D87),
                    fontFamily = GillSans,
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp
                )
            },
            text = {
                Text(
                    text = "Do you want to send relationship invitation?",
                    color = Color.Black,
                    fontFamily = GillSans,
                    fontSize = 15.sp
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        showConfirmDialog = false
                        savedPayload?.let { payload ->
                            isLoading = true
                            onSubmitRequest(payload) { success, msg ->
                                isLoading = false
                                if (success) {
                                    successDialogMessage = "Invitation sent successfully"
                                    showSuccessDialog = true
                                } else {
                                    errorDialogMessage = msg
                                    showErrorDialog = true
                                }
                            }
                        }
                    }
                ) {
                    Text("Yes", color = Color(0xFF004D87), fontFamily = GillSans, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showConfirmDialog = false }) {
                    Text("No", color = Color.Gray, fontFamily = GillSans)
                }
            },
            containerColor = Color.White,
            shape = RoundedCornerShape(8.dp)
        )
    }

    if (showSuccessDialog) {
        AlertDialog(
            onDismissRequest = { 
                showSuccessDialog = false 
                onCancel() 
            },
            title = {
                Text(
                    text = "Alert !",
                    color = Color(0xFF004D87),
                    fontFamily = GillSans,
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp
                )
            },
            text = {
                Text(
                    text = successDialogMessage,
                    color = Color.Black,
                    fontFamily = GillSans,
                    fontSize = 15.sp
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        showSuccessDialog = false
                        onCancel()
                    }
                ) {
                    Text("OK", color = Color(0xFF004D87), fontFamily = GillSans, fontWeight = FontWeight.Bold)
                }
            },
            containerColor = Color.White,
            shape = RoundedCornerShape(8.dp)
        )
    }

    if (showErrorDialog) {
        AlertDialog(
            onDismissRequest = { showErrorDialog = false },
            title = {
                Text(
                    text = "Alert !",
                    color = Color(0xFF004D87),
                    fontFamily = GillSans,
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp
                )
            },
            text = {
                Text(
                    text = errorDialogMessage,
                    color = Color.Black,
                    fontFamily = GillSans,
                    fontSize = 15.sp
                )
            },
            confirmButton = {
                TextButton(onClick = { showErrorDialog = false }) {
                    Text("OK", color = Color(0xFF004D87), fontFamily = GillSans, fontWeight = FontWeight.Bold)
                }
            },
            containerColor = Color.White,
            shape = RoundedCornerShape(8.dp)
        )
    }
}
