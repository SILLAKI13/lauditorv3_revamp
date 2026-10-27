package com.digicoffer.lauditor.feature.messages.presentation.viewmodel

import android.app.Activity
import android.app.Application
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.SharedPreferences
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.util.Log
import androidx.core.content.ContextCompat
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.digicoffer.lauditor.Appointments.Models.AppointmentModel
import com.digicoffer.lauditor.CommonFiles.ChatService.ChatConnection
import com.digicoffer.lauditor.CommonFiles.ChatService.ChatConnectionService
import com.digicoffer.lauditor.CommonFiles.ChatService.PresenceManager
import com.digicoffer.lauditor.CommonFiles.ChatService.TypingStateManager
import com.digicoffer.lauditor.CommonFiles.GlobalFiles.AndroidUtils
import com.digicoffer.lauditor.CommonFiles.GlobalFiles.Constants
import com.digicoffer.lauditor.Webservice.CommonApiHelper.WebServiceHelper
import com.digicoffer.lauditor.feature.messages.data.model.ChatMessageItem
import com.digicoffer.lauditor.feature.messages.data.model.ConversationItem
import com.digicoffer.lauditor.feature.messages.data.model.MessagesTab
import com.digicoffer.lauditor.feature.messages.data.repository.MessagesRepository
import com.digicoffer.lauditor.feature.messages.presentation.state.MessagesUiEvent
import com.digicoffer.lauditor.feature.messages.presentation.state.MessagesUiState
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

class MessagesViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = MessagesRepository(application.applicationContext)
    private val _uiState = MutableStateFlow(MessagesUiState())
    val uiState: StateFlow<MessagesUiState> = _uiState.asStateFlow()

    private var currentJid = ""
    private var lastComposingSentAt = 0L
    private var isCurrentlyComposing = false
    private val typingPauseHandler = Handler(Looper.getMainLooper())
    private val pausedRunnable = Runnable { sendPausedIfWasComposing() }

    private var cachedAppointments: List<AppointmentModel> = emptyList()

    private var newMessageReceiver: BroadcastReceiver? = null

    private fun parseAppointmentDate(dateStr: String?): Date? {
        if (dateStr.isNullOrBlank()) return null
        val patterns = arrayOf(
            "yyyy-MM-dd'T'HH:mm:ss.SSSSSS'Z'",
            "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'",
            "yyyy-MM-dd'T'HH:mm:ss'Z'",
            "yyyy-MM-dd'T'HH:mm:ss.SSSSSS",
            "yyyy-MM-dd'T'HH:mm:ss.SSS",
            "yyyy-MM-dd'T'HH:mm:ss",
            "yyyy-MM-dd HH:mm:ss"
        )
        for (pattern in patterns) {
            try {
                val sdf = SimpleDateFormat(pattern, Locale.ENGLISH)
                if (pattern.endsWith("'Z'")) {
                    sdf.timeZone = java.util.TimeZone.getTimeZone("UTC")
                }
                val parsed = sdf.parse(dateStr)
                if (parsed != null) return parsed
            } catch (ignored: Exception) {}
        }
        return null
    }

    private fun evaluateChatRestriction(
        clientGuid: String,
        clientId: String,
        clientName: String,
        clientSource: String,
        clientType: String,
        directAppointmentFrom: String?,
        appointments: List<AppointmentModel>
    ): Pair<Boolean, String> {
        val defaultMessage = "Chat will be available 1 hour before your scheduled appointment"
        val now = System.currentTimeMillis()
        val oneHourMs = 60 * 60 * 1000L

        // 1. Direct check on relationship object if appointment_from is provided directly
        if (!directAppointmentFrom.isNullOrEmpty()) {
            val apptDate = parseAppointmentDate(directAppointmentFrom)
            if (apptDate != null) {
                val availableFrom = apptDate.time - oneHourMs
                if (now < availableFrom) {
                    return Pair(true, defaultMessage)
                }
            }
        }

        // 2. Find all appointments matching this client
        val clientAppts = appointments.filter { appt ->
            (appt.guid.isNotEmpty() && (appt.guid.equals(clientGuid, ignoreCase = true) || appt.guid.equals(clientId, ignoreCase = true))) ||
            (appt.client_id.isNotEmpty() && (appt.client_id.equals(clientId, ignoreCase = true) || appt.client_id.equals(clientGuid, ignoreCase = true))) ||
            (appt.client_name.isNotEmpty() && appt.client_name.equals(clientName, ignoreCase = true))
        }

        if (clientAppts.isEmpty()) {
            return Pair(false, defaultMessage)
        }

        // Check if client has ANY completed or past appointment
        val hasPastOrCompleted = clientAppts.any { appt ->
            val status = appt.appointment_status.trim().lowercase(Locale.ROOT)
            if (status == "completed") return@any true
            val toDate = parseAppointmentDate(appt.appointment_to)
            if (toDate != null && now > toDate.time && status != "cancelled" && status != "canceled") return@any true
            appt.appointmentHistory.isNotEmpty()
        }

        if (hasPastOrCompleted) {
            return Pair(false, defaultMessage)
        }

        // Filter active upcoming appointments
        val upcomingAppts = clientAppts.filter { appt ->
            val status = appt.appointment_status.trim().lowercase(Locale.ROOT)
            status != "cancelled" && status != "canceled"
        }.mapNotNull { appt ->
            val fromDate = parseAppointmentDate(appt.appointment_from)
            if (fromDate != null) Pair(appt, fromDate) else null
        }.sortedBy { it.second.time }

        val earliestUpcoming = upcomingAppts.firstOrNull()
        if (earliestUpcoming != null) {
            val apptStartTime = earliestUpcoming.second.time
            val availableFromTime = apptStartTime - oneHourMs
            if (now < availableFromTime) {
                return Pair(true, defaultMessage)
            }
        }

        return Pair(false, defaultMessage)
    }

    private val presenceListener = object : PresenceManager.PresenceChangeListener {
        override fun onPresenceChanged(guid: String, isOnline: Boolean) {
            _uiState.update { state ->
                val updatedClients = state.clientsList.map { item ->
                    if (item.guid == guid) item.copy(isOnline = isOnline) else item
                }
                val updatedTeams = state.teamsList.map { team ->
                    val updatedMembers = team.members.map { m ->
                        if (m.guid == guid) m.copy(isOnline = isOnline) else m
                    }
                    if (team.guid == guid) team.copy(isOnline = isOnline, members = updatedMembers)
                    else team.copy(members = updatedMembers)
                }
                val active = state.activeConversation
                val partnerOnline = if (active?.guid == guid) isOnline else state.isPartnerOnline
                state.copy(
                    clientsList = updatedClients,
                    teamsList = updatedTeams,
                    isPartnerOnline = partnerOnline
                )
            }
        }
    }

    private val typingListener = object : TypingStateManager.TypingChangeListener {
        override fun onTypingChanged(guid: String, isTyping: Boolean) {
            _uiState.update { state ->
                val updatedClients = state.clientsList.map { item ->
                    if (item.guid == guid) item.copy(isTyping = isTyping) else item
                }
                val updatedTeams = state.teamsList.map { team ->
                    val updatedMembers = team.members.map { m ->
                        if (m.guid == guid) m.copy(isTyping = isTyping) else m
                    }
                    if (team.guid == guid) team.copy(isTyping = isTyping, members = updatedMembers)
                    else team.copy(members = updatedMembers)
                }
                val active = state.activeConversation
                val partnerTyping = if (active?.guid == guid) isTyping else state.isPartnerTyping
                state.copy(
                    clientsList = updatedClients,
                    teamsList = updatedTeams,
                    isPartnerTyping = partnerTyping
                )
            }
        }
    }

    private fun getPrefs(): SharedPreferences {
        return android.preference.PreferenceManager.getDefaultSharedPreferences(getApplication<Application>())
    }

    init {
        val prefs = getPrefs()
        var uid = Constants.UID ?: ""
        if (!Constants.ROLE.equals("admin", ignoreCase = true)) {
            val userId = Constants.USER_ID ?: ""
            if (userId.isNotEmpty() && !uid.endsWith("_$userId")) {
                uid = "${uid}_$userId"
            }
        }
        var jid = if (uid.isNotEmpty()) uid else (prefs.getString("xmpp_jid", null) ?: "")
        val pass = if (!Constants.TOKEN.isNullOrEmpty()) Constants.TOKEN!! else (prefs.getString("xmpp_password", null) ?: "")
        if (jid.isNotEmpty()) {
            prefs.edit().putString("xmpp_jid", jid).apply()
        }
        if (pass.isNotEmpty()) {
            prefs.edit().putString("xmpp_password", pass).putBoolean("xmpp_logged_in", true).apply()
        }
        currentJid = if (jid.contains("@")) jid.split("@")[0] else jid

        if (jid.isNotEmpty() && pass.isNotEmpty()) {
            ChatConnection.loginUser(getApplication(), jid, pass)
        }

        val isAam = "AAM" == Constants.ROLE
        val isSolo = "solo" == Constants.CATEGORY
        val initialTab = if (isAam) MessagesTab.TEAMS else if (isSolo) MessagesTab.CLIENTS else {
            if (Constants.isClient_chat) MessagesTab.CLIENTS else MessagesTab.TEAMS
        }

        _uiState.update {
            it.copy(
                isAamRole = isAam,
                isSoloCategory = isSolo,
                selectedTab = initialTab
            )
        }

        PresenceManager.addListener(presenceListener)
        TypingStateManager.addListener(typingListener)
        registerNewMessageReceiver()

        loadAllData()
        checkPendingNotificationNavigation()
    }

    fun onEvent(event: MessagesUiEvent) {
        when (event) {
            is MessagesUiEvent.SelectTab -> {
                if (!Constants.is_active) {
                    Constants.mainActivity?.let { AndroidUtils.showRenewalPopup(it) }
                    return
                }
                val featureKey = if (event.tab == MessagesTab.CLIENTS) "client_chat" else "team_chat"
                if (!Constants.isFeatureEnabled(featureKey)) {
                    Constants.mainActivity?.let { AndroidUtils.showUpgradePopup(it) }
                    return
                }
                Constants.isClient_chat = (event.tab == MessagesTab.CLIENTS)
                _uiState.update { it.copy(selectedTab = event.tab, searchQuery = "") }
            }
            is MessagesUiEvent.SearchQueryChanged -> {
                _uiState.update { it.copy(searchQuery = event.query) }
            }
            is MessagesUiEvent.OpenConversation -> {
                if (!Constants.is_active) {
                    Constants.mainActivity?.let { AndroidUtils.showRenewalPopup(it) }
                    return
                }
                val featureKey = if (_uiState.value.selectedTab == MessagesTab.CLIENTS) "client_chat" else "team_chat"
                if (!Constants.isFeatureEnabled(featureKey)) {
                    Constants.mainActivity?.let { AndroidUtils.showUpgradePopup(it) }
                    return
                }
                openConversation(event.conversation)
            }
            is MessagesUiEvent.CloseConversation -> {
                closeConversation()
            }
            is MessagesUiEvent.ToggleTeamExpanded -> {
                toggleTeamExpanded(event.teamId)
            }
            is MessagesUiEvent.ChatMessageInputChanged -> {
                handleChatMessageInput(event.text)
            }
            is MessagesUiEvent.SendMessage -> {
                sendCurrentMessage()
            }
            is MessagesUiEvent.ChatSearchQueryChanged -> {
                _uiState.update { it.copy(chatSearchQuery = event.query) }
            }
            is MessagesUiEvent.ToggleChatSearch -> {
                _uiState.update { it.copy(isChatSearchActive = event.isOpen, chatSearchQuery = if (!event.isOpen) "" else it.chatSearchQuery) }
            }
            is MessagesUiEvent.RefreshList -> {
                loadAllData()
            }
            is MessagesUiEvent.LaunchVideoCall -> {
                launchVideoCall(event.activity, event.view)
            }
            is MessagesUiEvent.DismissAlert -> {
                _uiState.update { it.copy(errorMessage = null, successMessage = null) }
            }
        }
    }

    private fun checkPendingNotificationNavigation() {
        val pendingJid = Constants.pendingChatJid
        if (!pendingJid.isNullOrEmpty()) {
            val jid = Constants.pendingChatJid ?: ""
            val name = Constants.pendingChatName ?: ""
            val source = Constants.pendingChatSource ?: "relationship"
            Constants.pendingChatJid = ""
            Constants.pendingChatName = ""
            Constants.pendingChatSource = ""

            val pendingItem = ConversationItem(
                guid = jid,
                name = name,
                clientType = source,
                source = source
            )
            openConversation(pendingItem)
        }
    }

    fun setTab(tab: MessagesTab) {
        _uiState.update { it.copy(selectedTab = tab, searchQuery = "") }
    }

    private fun loadAllData() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }

            val teamsJob = launch(Dispatchers.IO) {
                if (!_uiState.value.isSoloCategory) {
                    try {
                        loadTeamsInternal()
                    } catch (e: Exception) {
                        Log.e("MessagesViewModel", "loadTeams error: ${e.message}", e)
                    }
                }
            }

            val clientsJob = launch(Dispatchers.IO) {
                try {
                    loadClientsInternal()
                } catch (e: Exception) {
                    Log.e("MessagesViewModel", "loadClients error: ${e.message}", e)
                }
            }

            if (_uiState.value.selectedTab == MessagesTab.TEAMS) {
                teamsJob.join()
            } else {
                clientsJob.join()
            }
            _uiState.update { it.copy(isLoading = false) }
        }
    }

    private suspend fun loadClientsInternal() = withContext(Dispatchers.IO) {
        val corporateDeferred = async { repository.fetchCorporateRelationships() }
        val clientDeferred = async { repository.fetchClientRelationships() }
        val appointmentsDeferred = async { repository.fetchAppointments() }

        val corporateResult = corporateDeferred.await()
        val clientResult = clientDeferred.await()
        val appointmentsResult = appointmentsDeferred.await()

        val parsedAppointments = mutableListOf<AppointmentModel>()
        if (appointmentsResult.result == WebServiceHelper.ServiceCallStatus.Success) {
            try {
                val rootJson = JSONObject(appointmentsResult.responseContent ?: "{}")
                if (!rootJson.optBoolean("error", false)) {
                    val apptArray = rootJson.optJSONArray("appointments") ?: JSONArray()
                    for (i in 0 until apptArray.length()) {
                        val apptObj = apptArray.optJSONObject(i) ?: continue
                        parsedAppointments.add(
                            AppointmentModel().apply {
                                id = apptObj.optString("id", "")
                                client_id = apptObj.optString("client_id", "")
                                guid = apptObj.optString("guid", "")
                                client_name = apptObj.optString("client_name", "")
                                appointment_from = apptObj.optString("appointment_from", "")
                                appointment_to = apptObj.optString("appointment_to", "")
                                appointment_status = apptObj.optString("appointment_status", "")
                                consultation_mode = apptObj.optString("consultation_mode", "")
                            }
                        )
                    }
                }
            } catch (e: Exception) {
                Log.e("MessagesViewModel", "Appointments parse error: ${e.message}")
            }
        }
        cachedAppointments = parsedAppointments

        val clients = mutableListOf<ConversationItem>()
        val firmMap = mutableMapOf<String, String>()

        // 1. Parse Corporate List
        if (corporateResult.result == WebServiceHelper.ServiceCallStatus.Success) {
            try {
                val json = JSONObject(corporateResult.responseContent ?: "{}")
                if (!json.optBoolean("error")) {
                    val array = json.optJSONArray("relationships") ?: JSONArray()
                    for (i in 0 until array.length()) {
                        val obj = array.getJSONObject(i)
                        val guid = obj.optString("client_id").ifEmpty { obj.optString("guid") }
                        if (guid.isNotEmpty()) {
                            clients.add(
                                ConversationItem(
                                    guid = guid,
                                    id = obj.optString("id"),
                                    name = obj.optString("name"),
                                    clientType = obj.optString("type", "corporate"),
                                    source = obj.optString("source")
                                )
                            )
                        }
                    }
                }
            } catch (e: Exception) {
                Log.e("MessagesViewModel", "Corporate parse error: ${e.message}")
            }
        }

        // 2. Parse Individual / Entity Client List
        if (clientResult.result == WebServiceHelper.ServiceCallStatus.Success) {
            try {
                val json = JSONObject(clientResult.responseContent ?: "{}")
                if (!json.optBoolean("error")) {
                    val data = json.optJSONObject("data")
                    val array = data?.optJSONArray("relationships") ?: JSONArray()
                    for (i in 0 until array.length()) {
                        val obj = array.getJSONObject(i)
                        val clientType = obj.optString("type")
                        val relId = obj.optString("rel_id")
                        val name = obj.optString("name")
                        val guid = obj.optString("guid").ifEmpty { obj.optString("client_id") }

                        if ("entity".equals(clientType, ignoreCase = true) && relId.isNotEmpty()) {
                            firmMap[relId] = name
                            // Fetch sub-users of entity
                            val usersRes = repository.fetchRelationshipUsers(relId)
                            if (usersRes.result == WebServiceHelper.ServiceCallStatus.Success) {
                                try {
                                    val uJson = JSONObject(usersRes.responseContent ?: "{}")
                                    if (!uJson.optBoolean("error")) {
                                        val uArray = uJson.optJSONObject("data")?.optJSONArray("users") ?: JSONArray()
                                        for (j in 0 until uArray.length()) {
                                            val uObj = uArray.getJSONObject(j)
                                            val uGuid = uObj.optString("guid")
                                            if (uGuid.isNotEmpty() && clients.none { it.guid == uGuid }) {
                                                clients.add(
                                                    ConversationItem(
                                                        guid = uGuid,
                                                        name = uObj.optString("name"),
                                                        firmName = name,
                                                        clientType = "consumer",
                                                        source = "relationship"
                                                    )
                                                )
                                            }
                                        }
                                    }
                                } catch (e: Exception) {
                                    Log.e("MessagesViewModel", "Entity users parse error: ${e.message}")
                                }
                            }
                        } else if (guid.isNotEmpty() && clients.none { it.guid == guid }) {
                            val directApptFrom = obj.optString("appointment_from").ifEmpty {
                                obj.optJSONObject("appointment")?.optString("appointment_from") ?: ""
                            }
                            val (isRestricted, restrictionMsg) = evaluateChatRestriction(
                                clientGuid = guid,
                                clientId = obj.optString("id"),
                                clientName = name,
                                clientSource = obj.optString("source"),
                                clientType = clientType,
                                directAppointmentFrom = directApptFrom,
                                appointments = cachedAppointments
                            )

                            clients.add(
                                ConversationItem(
                                    guid = guid,
                                    id = obj.optString("id"),
                                    name = name,
                                    clientType = clientType,
                                    source = obj.optString("source"),
                                    isChatRestricted = isRestricted,
                                    chatRestrictionMessage = restrictionMsg,
                                    appointmentFrom = directApptFrom
                                )
                            )
                        }
                    }
                }
            } catch (e: Exception) {
                Log.e("MessagesViewModel", "Client list parse error: ${e.message}")
            }
        }

        // Apply unread count from Constants.unreadList
        val updatedWithUnread = clients.map { client ->
            var unreadTotal = 0
            for (unread in Constants.unreadList) {
                if (unread.fromjid != null && unread.fromjid.equals(client.guid, ignoreCase = true)) {
                    unreadTotal += unread.count?.toIntOrNull() ?: 0
                }
            }
            client.copy(
                unreadCount = unreadTotal,
                isOnline = PresenceManager.isOnline(client.guid),
                isTyping = TypingStateManager.isTyping(client.guid)
            )
        }

        // Fetch meta (last message, timestamp, unread) before emitting list to guarantee new-to-old sorting on initial render
        try {
            val chatJids = updatedWithUnread.map { it.guid + (Constants.VitacapeExtention ?: "") }
            val metaMap = repository.fetchConversationMeta(chatJids)

            val finalizedClients = updatedWithUnread.map { client ->
                val meta = metaMap[client.guid]
                if (meta != null) {
                    client.copy(
                        lastMessage = meta.lastMessage,
                        lastMessageTime = AndroidUtils.getChatTimeText(Date(meta.lastMessageTimestamp)),
                        lastMessageTimestamp = meta.lastMessageTimestamp,
                        unreadCount = if (meta.unreadCount.isNotEmpty()) meta.unreadCount.toIntOrNull() ?: client.unreadCount else client.unreadCount
                    )
                } else {
                    client
                }
            }.sortedWith(createConversationComparator())

            _uiState.update { it.copy(clientsList = finalizedClients) }
        } catch (e: Exception) {
            Log.e("MessagesViewModel", "Clients meta fetch error: ${e.message}")
            _uiState.update { it.copy(clientsList = updatedWithUnread.sortedWith(createConversationComparator())) }
        }
    }

    private suspend fun loadTeamsInternal() = withContext(Dispatchers.IO) {
        val membersResult = repository.fetchTeamMembers()
        val teamItems = mutableListOf<ConversationItem>()

        if (membersResult.result == WebServiceHelper.ServiceCallStatus.Success) {
            try {
                val json = JSONObject(membersResult.responseContent ?: "{}")
                val data = json.optJSONObject("data")
                val users = data?.optJSONArray("users") ?: JSONArray()

                for (i in 0 until users.length()) {
                    val uObj = users.getJSONObject(i)
                    val guid = uObj.optString("guid")
                    val name = uObj.optString("name")
                    val id = uObj.optString("id")
                    val isdisabled = uObj.optBoolean("isdisabled", false)

                    if (guid.isEmpty() || isdisabled) continue

                    var mUnread = 0
                    for (unread in Constants.unreadList) {
                        if (unread.fromjid != null && unread.fromjid.equals(guid, ignoreCase = true)) {
                            mUnread += unread.count?.toIntOrNull() ?: 0
                        }
                    }

                    teamItems.add(
                        ConversationItem(
                            guid = guid,
                            id = id,
                            name = name,
                            clientType = "Team",
                            source = "relationship",
                            unreadCount = mUnread,
                            isOnline = PresenceManager.isOnline(guid),
                            isTyping = TypingStateManager.isTyping(guid)
                        )
                    )
                }
            } catch (e: Exception) {
                Log.e("MessagesViewModel", "Members parse error: ${e.message}")
            }
        }

        // Fetch meta for team members (last message, timestamp, unread) before emitting list
        try {
            val allMemberGuids = teamItems.map { it.guid }
            val metaMap = repository.fetchConversationMeta(allMemberGuids.map { it + (Constants.VitacapeExtention ?: "") })

            val finalizedTeams = teamItems.map { team ->
                val meta = metaMap[team.guid]
                if (meta != null) {
                    team.copy(
                        lastMessage = meta.lastMessage,
                        lastMessageTime = AndroidUtils.getChatTimeText(Date(meta.lastMessageTimestamp)),
                        lastMessageTimestamp = meta.lastMessageTimestamp,
                        unreadCount = if (meta.unreadCount.isNotEmpty()) meta.unreadCount.toIntOrNull() ?: team.unreadCount else team.unreadCount
                    )
                } else {
                    team
                }
            }.sortedWith(createConversationComparator())

            _uiState.update { it.copy(teamsList = finalizedTeams) }
        } catch (e: Exception) {
            Log.e("MessagesViewModel", "Teams meta fetch error: ${e.message}")
            _uiState.update { it.copy(teamsList = teamItems.sortedWith(createConversationComparator())) }
        }
    }

    private fun createConversationComparator(): Comparator<ConversationItem> {
        return Comparator { c1, c2 ->
            val t1 = c1.lastMessageTimestamp
            val t2 = c2.lastMessageTimestamp
            if (t1 != t2) {
                t2.compareTo(t1)
            } else {
                val u1 = c1.unreadCount
                val u2 = c2.unreadCount
                if (u1 != u2) {
                    u2.compareTo(u1)
                } else {
                    c1.displayName.compareTo(c2.displayName, ignoreCase = true)
                }
            }
        }
    }

    private fun toggleTeamExpanded(teamId: String) {
        _uiState.update { state ->
            val updated = state.teamsList.map { team ->
                if (team.id == teamId || team.guid == teamId) {
                    team.copy(isExpanded = !team.isExpanded)
                } else team
            }
            state.copy(teamsList = updated)
        }
    }

    fun openConversation(conversation: ConversationItem) {
        getPrefs().edit().putString("CURRENTCHAT_JID", conversation.guid).apply()

        // Reset unread count for this conversation
        repository.resetUnreadCount(conversation.guid)

        val (isRestricted, restrictionMsg) = evaluateChatRestriction(
            clientGuid = conversation.guid,
            clientId = conversation.id,
            clientName = conversation.name,
            clientSource = conversation.source,
            clientType = conversation.clientType,
            directAppointmentFrom = conversation.appointmentFrom,
            appointments = cachedAppointments
        )

        _uiState.update { state ->
            val updatedClients = state.clientsList.map {
                if (it.guid == conversation.guid) {
                    it.copy(
                        unreadCount = 0,
                        isChatRestricted = isRestricted,
                        chatRestrictionMessage = restrictionMsg
                    )
                } else it
            }
            val updatedTeams = state.teamsList.map { team ->
                val updatedMembers = team.members.map { m ->
                    if (m.guid == conversation.guid) m.copy(unreadCount = 0) else m
                }
                if (team.guid == conversation.guid) team.copy(unreadCount = 0, members = updatedMembers)
                else team.copy(members = updatedMembers, unreadCount = updatedMembers.sumOf { it.unreadCount })
            }

            state.copy(
                activeConversation = conversation.copy(
                    unreadCount = 0,
                    isChatRestricted = isRestricted,
                    chatRestrictionMessage = restrictionMsg
                ),
                isChatRestricted = isRestricted,
                chatRestrictionMessage = restrictionMsg,
                chatMessages = emptyList(),
                isChatHistoryLoading = true,
                messageInputText = "",
                chatSearchQuery = "",
                isChatSearchActive = false,
                isPartnerOnline = PresenceManager.isOnline(conversation.guid),
                isPartnerTyping = TypingStateManager.isTyping(conversation.guid),
                clientsList = updatedClients,
                teamsList = updatedTeams
            )
        }

        fetchChatHistory(conversation)
    }

    private fun fetchChatHistory(conversation: ConversationItem) {
        viewModelScope.launch {
            _uiState.update { it.copy(isChatHistoryLoading = true) }

            val prefs = getPrefs()
            var prefJid = prefs.getString("xmpp_jid", null)
            var prefPass = prefs.getString("xmpp_password", null)
            if (prefJid.isNullOrEmpty()) {
                var uid = Constants.UID ?: ""
                if (!Constants.ROLE.equals("admin", ignoreCase = true)) {
                    val userId = Constants.USER_ID ?: ""
                    if (userId.isNotEmpty() && !uid.endsWith("_$userId")) {
                        uid = "${uid}_$userId"
                    }
                }
                prefJid = uid
            }
            if (prefPass.isNullOrEmpty()) {
                prefPass = Constants.TOKEN ?: ""
            }
            currentJid = if (prefJid.contains("@")) prefJid.split("@")[0] else prefJid

            if (ChatConnection.mConnection == null || !ChatConnection.mConnection!!.isConnected || !ChatConnection.mConnection!!.isAuthenticated) {
                try {
                    ChatConnection.loginUser(getApplication(), prefJid, prefPass)
                    var waited = 0
                    while ((ChatConnection.mConnection == null || !ChatConnection.mConnection!!.isAuthenticated) && waited < 4000) {
                        kotlinx.coroutines.delay(200)
                        waited += 200
                    }
                } catch (e: Exception) {
                    Log.e("MessagesViewModel", "Error connecting XMPP: ${e.message}")
                }
            }

            var result = repository.fetchArchiveMessages(
                contactJid = conversation.guid,
                currentJid = currentJid,
                contactName = conversation.displayName
            )

            if (result.isFailure || (result.getOrNull()?.isEmpty() == true && !conversation.lastMessage.isNullOrEmpty())) {
                // If XMPP connection was waking up, retry after a short delay
                kotlinx.coroutines.delay(800)
                result = repository.fetchArchiveMessages(
                    contactJid = conversation.guid,
                    currentJid = currentJid,
                    contactName = conversation.displayName
                )
            }

            if ((result.isFailure || result.getOrNull()?.isEmpty() == true) && conversation.id.isNotEmpty() && conversation.id != conversation.guid) {
                result = repository.fetchArchiveMessages(
                    contactJid = conversation.id,
                    currentJid = currentJid,
                    contactName = conversation.displayName
                )
            }

            result.onSuccess { messages ->
                val lastMsgId = messages.lastOrNull { !it.isSentByMe }?.id
                _uiState.update {
                    it.copy(
                        chatMessages = messages,
                        isChatHistoryLoading = false,
                        lastSeenMessageId = lastMsgId
                    )
                }
                if (!lastMsgId.isNullOrEmpty()) {
                    repository.sendReadMarker(conversation.guid, lastMsgId)
                }
            }.onFailure { err ->
                Log.e("MessagesViewModel", "MAM fetch failed: ${err.message}")
                _uiState.update { it.copy(isChatHistoryLoading = false) }
            }
        }
    }

    private fun closeConversation() {
        sendGone()
        getPrefs().edit().remove("CURRENTCHAT_JID").apply()

        _uiState.update {
            it.copy(
                activeConversation = null,
                chatMessages = emptyList(),
                messageInputText = "",
                chatSearchQuery = "",
                isChatSearchActive = false
            )
        }
    }

    private fun handleChatMessageInput(text: String) {
        _uiState.update { it.copy(messageInputText = text) }

        val contactJid = _uiState.value.activeConversation?.guid ?: return
        if (text.trim().isEmpty()) {
            sendPausedIfWasComposing()
            return
        }

        sendComposingThrottled(contactJid)
        schedulePausedAfterInactivity()
    }

    private fun sendComposingThrottled(contactJid: String) {
        val now = System.currentTimeMillis()
        if (now - lastComposingSentAt < 3000L && isCurrentlyComposing) {
            return
        }
        repository.sendChatState(contactJid, ChatConnection.CHAT_STATE_COMPOSING)
        lastComposingSentAt = now
        isCurrentlyComposing = true
    }

    private fun schedulePausedAfterInactivity() {
        typingPauseHandler.removeCallbacks(pausedRunnable)
        typingPauseHandler.postDelayed(pausedRunnable, 1000L)
    }

    private fun sendPausedIfWasComposing() {
        typingPauseHandler.removeCallbacks(pausedRunnable)
        if (!isCurrentlyComposing) return
        val contactJid = _uiState.value.activeConversation?.guid ?: return
        repository.sendChatState(contactJid, ChatConnection.CHAT_STATE_PAUSED)
        isCurrentlyComposing = false
    }

    private fun sendGone() {
        typingPauseHandler.removeCallbacks(pausedRunnable)
        val contactJid = _uiState.value.activeConversation?.guid ?: return
        repository.sendChatState(contactJid, ChatConnection.CHAT_STATE_GONE)
        isCurrentlyComposing = false
    }

    private fun sendCurrentMessage() {
        val text = _uiState.value.messageInputText.trim()
        val active = _uiState.value.activeConversation ?: return

        if (_uiState.value.isChatRestricted || active.isChatRestricted) {
            _uiState.update { 
                it.copy(
                    errorMessage = active.chatRestrictionMessage.ifEmpty { 
                        "Chat will be available 1 hour before your scheduled appointment" 
                    }
                ) 
            }
            return
        }

        if (text.isEmpty()) {
            _uiState.update { it.copy(errorMessage = "Message field cannot be empty") }
            return
        }

        viewModelScope.launch {
            val isConnected = (ChatConnection.mConnection != null && ChatConnection.mConnection!!.isAuthenticated) ||
                    (ChatConnectionService.getState() == ChatConnection.ConnectionState.CONNECTED)

            if (!isConnected) {
                val context = getApplication<android.app.Application>()
                val prefs = getPrefs()
                var prefUid = prefs.getString("xmpp_jid", null)
                var prefPass = prefs.getString("xmpp_password", null)
                if (prefUid.isNullOrEmpty()) {
                    val baseUid = Constants.UID ?: ""
                    prefUid = if (!Constants.ROLE.equals("admin", ignoreCase = true)) {
                        val uId = Constants.USER_ID ?: ""
                        if (uId.isNotEmpty()) "${baseUid}_$uId" else baseUid
                    } else {
                        baseUid
                    }
                }
                if (prefPass.isNullOrEmpty()) {
                    prefPass = Constants.TOKEN ?: ""
                }
                if (!prefUid.isNullOrEmpty() && !prefPass.isNullOrEmpty()) {
                    ChatConnection.loginUser(context, prefUid, prefPass)
                }

                var waited = 0
                while ((ChatConnection.mConnection == null || !ChatConnection.mConnection!!.isAuthenticated) && waited < 3000) {
                    kotlinx.coroutines.delay(200)
                    waited += 200
                }

                val reconnected = ChatConnection.mConnection != null && ChatConnection.mConnection!!.isAuthenticated
                if (!reconnected) {
                    _uiState.update { it.copy(errorMessage = "Connecting to chat service... Please try after a few seconds.") }
                    return@launch
                }
            }

            var uid = Constants.UID ?: ""
            if (!Constants.ROLE.equals("admin", ignoreCase = true)) {
                val userId = Constants.USER_ID ?: ""
                if (userId.isNotEmpty() && !uid.endsWith("_$userId")) {
                    uid = "${uid}_$userId"
                }
            }
            if (uid.isNotEmpty()) {
                currentJid = if (uid.contains("@")) uid.split("@")[0] else uid
            }

            repository.sendMessage(text, active.guid, currentJid)

            val timeFormat = SimpleDateFormat("hh:mm a", Locale.ENGLISH)
            val todayHeader = "Today"
            val fullFormat = SimpleDateFormat("MMM dd, yyyy hh:mm a", Locale.ENGLISH)
            val now = Date()

            val newMessage = ChatMessageItem(
                id = "${System.currentTimeMillis()}_${(0..999).random()}",
                message = text,
                isSentByMe = true,
                senderName = Constants.NAME ?: "",
                createdAtFormatted = fullFormat.format(now),
                dateGroupHeader = todayHeader,
                timeFormatted = timeFormat.format(now),
                isCurrentChat = true
            )

            _uiState.update { state ->
                val updatedMessages = state.chatMessages + newMessage
                val updatedClients = state.clientsList.map { item ->
                    if (item.guid == active.guid) {
                        item.copy(
                            lastMessage = text,
                            lastMessageTime = AndroidUtils.getChatTimeText(now),
                            lastMessageTimestamp = System.currentTimeMillis()
                        )
                    } else item
                }.sortedWith(createConversationComparator())

                val updatedTeams = state.teamsList.map { team ->
                    val updatedMembers = team.members.map { m ->
                        if (m.guid == active.guid) {
                            m.copy(
                                lastMessage = text,
                                lastMessageTime = AndroidUtils.getChatTimeText(now),
                                lastMessageTimestamp = System.currentTimeMillis()
                            )
                        } else m
                    }.sortedWith(createConversationComparator())
                    if (team.guid == active.guid) {
                        team.copy(
                            lastMessage = text,
                            lastMessageTime = AndroidUtils.getChatTimeText(now),
                            lastMessageTimestamp = System.currentTimeMillis(),
                            members = updatedMembers
                        )
                    } else team.copy(members = updatedMembers)
                }.sortedWith(createConversationComparator())

                state.copy(
                    chatMessages = updatedMessages,
                    messageInputText = "",
                    clientsList = updatedClients,
                    teamsList = updatedTeams
                )
            }
        }
    }

    private fun registerNewMessageReceiver() {
        newMessageReceiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context?, intent: Intent?) {
                if (intent == null || intent.action != ChatConnectionService.NEW_MESSAGE) return

                val fromJidRaw = intent.getStringExtra(ChatConnectionService.BUNDLE_FROM_JID) ?: return
                val body = intent.getStringExtra(ChatConnectionService.BUNDLE_MESSAGE_BODY) ?: ""
                val incomingMessageId = intent.getStringExtra("message_id") ?: ""
                val fromGuid = if (fromJidRaw.contains("@")) fromJidRaw.split("@")[0] else fromJidRaw

                val active = _uiState.value.activeConversation
                val timeFormat = SimpleDateFormat("hh:mm a", Locale.ENGLISH)
                val fullFormat = SimpleDateFormat("MMM dd, yyyy hh:mm a", Locale.ENGLISH)
                val now = Date()

                if (active != null && active.guid == fromGuid) {
                    val incoming = ChatMessageItem(
                        id = incomingMessageId.ifEmpty { System.currentTimeMillis().toString() },
                        message = body,
                        isSentByMe = false,
                        senderName = active.displayName,
                        createdAtFormatted = fullFormat.format(now),
                        dateGroupHeader = "Today",
                        timeFormatted = timeFormat.format(now),
                        isCurrentChat = true
                    )
                    _uiState.update { state ->
                        state.copy(
                            chatMessages = state.chatMessages + incoming,
                            lastSeenMessageId = incomingMessageId
                        )
                    }
                    if (incomingMessageId.isNotEmpty()) {
                        repository.sendReadMarker(fromGuid, incomingMessageId)
                    }
                } else {
                    // Update list items unread count
                    _uiState.update { state ->
                        val updatedClients = state.clientsList.map { item ->
                            if (item.guid == fromGuid) {
                                item.copy(
                                    lastMessage = body,
                                    lastMessageTime = AndroidUtils.getChatTimeText(now),
                                    lastMessageTimestamp = System.currentTimeMillis(),
                                    unreadCount = item.unreadCount + 1
                                )
                            } else item
                        }.sortedWith(createConversationComparator())

                        val updatedTeams = state.teamsList.map { team ->
                            val updatedMembers = team.members.map { m ->
                                if (m.guid == fromGuid) {
                                    m.copy(
                                        lastMessage = body,
                                        lastMessageTime = AndroidUtils.getChatTimeText(now),
                                        lastMessageTimestamp = System.currentTimeMillis(),
                                        unreadCount = m.unreadCount + 1
                                    )
                                } else m
                            }.sortedWith(createConversationComparator())
                            if (team.guid == fromGuid) {
                                team.copy(
                                    lastMessage = body,
                                    lastMessageTime = AndroidUtils.getChatTimeText(now),
                                    lastMessageTimestamp = System.currentTimeMillis(),
                                    unreadCount = team.unreadCount + 1,
                                    members = updatedMembers
                                )
                            } else team.copy(members = updatedMembers, unreadCount = updatedMembers.sumOf { it.unreadCount })
                        }.sortedWith(createConversationComparator())

                        state.copy(clientsList = updatedClients, teamsList = updatedTeams)
                    }
                }
            }
        }

        val filter = IntentFilter(ChatConnectionService.NEW_MESSAGE)
        val context = getApplication<Application>().applicationContext
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            context.registerReceiver(newMessageReceiver, filter, Context.RECEIVER_NOT_EXPORTED)
        } else {
            ContextCompat.registerReceiver(context, newMessageReceiver, filter, ContextCompat.RECEIVER_NOT_EXPORTED)
        }
    }

    private fun launchVideoCall(activity: Activity, view: android.view.View?) {
        val permissions = arrayOf(
            android.Manifest.permission.CAMERA,
            android.Manifest.permission.RECORD_AUDIO
        )
        val context = getApplication<Application>().applicationContext
        if (AndroidUtils.hasPermissions(permissions, context)) {
            AndroidUtils.loadWebView(context, activity)
        } else {
            AndroidUtils.showAlertDialog(view ?: activity.window.decorView, activity)
        }
    }

    override fun onCleared() {
        super.onCleared()
        sendGone()
        PresenceManager.removeListener(presenceListener)
        TypingStateManager.removeListener(typingListener)
        newMessageReceiver?.let {
            try {
                getApplication<Application>().applicationContext.unregisterReceiver(it)
            } catch (ignored: Exception) {
            }
        }
    }
}
