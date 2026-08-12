package com.digicoffer.lauditor.CommonFiles.GlobalFiles

import android.content.Context
import android.os.Bundle
import android.text.format.DateFormat
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.digicoffer.lauditor.Chat.Model.ChildDO
import com.digicoffer.lauditor.Dashboard.DahboardModels.UnreadCountModel
import com.digicoffer.lauditor.Dashboard.NewRevampViewModels.Dashboard
import com.digicoffer.lauditor.FirmProfile.FirmProfileModel
import com.digicoffer.lauditor.FirmProfile.ViewPracticePartnerAdapter
import com.digicoffer.lauditor.LoginActivity.Models.Dashboard_Model
import com.digicoffer.lauditor.LoginActivity.ViewModels.LoginActivity
import com.digicoffer.lauditor.MainActivity
import com.digicoffer.lauditor.Matter.ViewModels.GCT_En
import com.digicoffer.lauditor.Matter.OldViewModels.MatterInformation
import com.digicoffer.lauditor.Matter.ViewModels.MatterInformation_En
import com.digicoffer.lauditor.Matter.Models.ClientGroupModel
import com.digicoffer.lauditor.Matter.Models.ClientsModel
import com.digicoffer.lauditor.Matter.Models.GroupsModel
import org.json.JSONArray
import org.json.JSONObject
import java.util.ArrayList
import java.util.HashMap

object Constants {

    @JvmField var isfwd_or_isbwd = false
    @JvmField var meeting_has_value = false
    const val DOWNLOAD_FILE_TAG = "DOWNLOAD_FILE"
    
    @JvmField var notifyBadge: TextView? = null
    @JvmField var sessionExpired: Boolean? = false
    @JvmField var matterFilterType: String? = ""
    @JvmField var LOGIN_METHOD: String? = ""
    @JvmField var GeneratedMatterId: String? = ""
    @JvmField var TARGET_FIELD: String? = ""
    @JvmField var PROFILE_SECTION: String? = ""
    @JvmField var lastProfileContext: String? = ""
    @JvmField var pendingFcmNavigation: String? = ""
    @JvmField var GeneratedMatterTitle: String? = ""
    @JvmField var DOCUMENT_TYPE: String? = "matter"
    @JvmField var User_Allowed = 0
    @JvmStatic
    var is_active: Boolean
        get() = true
        set(value) {}
    @JvmField var isFromNotification = false
    @JvmField var notificationBundle = Bundle()
    @JvmField var issubscription = false
    @JvmField var pendingNotificationNavJson: String? = ""
    const val NO_INTERNET_MSG = "No internet connection. Please try again."
    @JvmField var ContactName: String? = ""
    @JvmField var Notification_Base_Url: String? = ""
    @JvmField var FCM_TOKEN: String? = ""
    @JvmField var product: String? = "Lawyers"

    @JvmStatic
    fun is24HoursFormat(context: Context): Boolean {
        return DateFormat.is24HourFormat(context)
    }

    object NavKeys {
        const val MODULE = "module"
        const val ACTION = "action"
        const val VIEW_TYPE = "view_type"
        const val APPOINTMENT_ID = "appointment_id"
        const val EVENT_ID = "event_id"
        const val GROUPS_ID = "group_ids"
        const val CLIENT_ID = "client_id"
        const val GUID = "client_id"
        const val MATTER_ID = "matter_id"
        const val HIGHLIGHT_IDS = "highlight_ids"
        const val RELATIONSHIP_ID = "relationship_id"
        const val ROUTE_NAME = "route_name"
    }

    @JvmField var pendingChatJid: String? = ""
    @JvmField var pendingChatName: String? = ""
    @JvmField var pendingChatSource: String? = ""
    @JvmField var PROFILE_EDIT_TAB: String? = "practice_details"

    class IdNameModel {
        @JvmField var id: String? = null
        @JvmField var name: String? = null
        @JvmField var url: String? = null

        fun getId(): String? = id
        fun setId(id: String?) { this.id = id }
        fun getName(): String? = name
        fun setName(name: String?) { this.name = name }
        fun getUrl(): String? = url
        fun setUrl(url: String?) { this.url = url }
    }

    @JvmField var FEATURES = HashMap<String, Boolean>()

    @JvmStatic
    fun isFeatureEnabled(key: String): Boolean {
        return if (FEATURES.containsKey(key)) {
            java.lang.Boolean.TRUE == FEATURES[key]
        } else {
            true
        }
    }

    @JvmField var firmProfileAdapter: ViewPracticePartnerAdapter? = null
    @JvmField var matterDate: String? = ""
    @JvmField var show_register = false
    @JvmField var allClientGroups = ArrayList<ClientGroupModel>()
    @JvmField var DocTagType: String? = ""
    @JvmField var Timesheet_Card: String? = "Myts"
    @JvmField var is_ts_submitted = false
    @JvmField var is_ts_not_submitted = false
    @JvmField var ts_card_clicked = false
    @JvmField var isDocEditor = false
    @JvmField var tempPos = -1
    @JvmField var firm_image: String? = ""
    @JvmField var fromjid: String? = ""
    @JvmField var updatedFromIid: String? = ""
    @JvmField var message_id: String? = ""
    @JvmField var stanzaId: String? = ""
    @JvmField var recyclerView: RecyclerView? = null
    @JvmField var Chat_id: String? = ""
    @JvmField var selected_temp_clients_list = ArrayList<ClientsModel>()
    @JvmField var matterInformation = MatterInformation()
    @JvmField var matterInformation_en: MatterInformation_En? = null
    @JvmField var upload_documents_list = ArrayList<com.digicoffer.lauditor.Documents.Models.DocumentsModel>()
    @JvmField var gct_en: GCT_En? = null
    @JvmField var unreadcount_from_list = ArrayList<UnreadCountModel>()
    @JvmField var unreadclient_list = ArrayList<UnreadCountModel>()
    @JvmField var unreadteam_list = ArrayList<UnreadCountModel>()
    @JvmField var clientChat_count: String? = "0"
    @JvmField var teamChat_count: String? = "0"
    @JvmField var IS_MyDay = true
    @JvmField var dashboard: Dashboard? = null
    @JvmField var dashboard_en: com.digicoffer.lauditor.Dashboard.NewRevampViewModels.Dashboard? = null
    @JvmField var composAttachDocAry = ArrayList<IdNameModel>()
    @JvmField var base_URL: String? = "http://10.0.2.2:8011/consumer/"
    @JvmField var TOKEN: String? = ""
    @JvmField var guid: String? = ""
    @JvmField var mainActivity: MainActivity? = null
    @JvmField var loginActivity: LoginActivity? = null
    @JvmField var doc_id = JSONArray()
    @JvmField var ex_group_attachment = JSONArray()
    @JvmField var ex_client = JSONArray()
    @JvmField var clientList = JSONArray()
    @JvmField var corpclientList = JSONArray()
    @JvmField var Biometric_checked = false
    @JvmField var is_biometric = false
    @JvmField var NAME: String? = ""
    @JvmField var termsVersion: String? = "v1.0"
    @JvmField var requiresTermsAcceptance = false
    @JvmField var PROBIZ_TYPE: String? = ""
    @JvmField var ENTITY_ID: String? = ""
    @JvmField var pdfFilePath: String? = null
    @JvmField var currentPage = 1
    @JvmField var is_meeting: String? = "Create"
    @JvmField var Matter_CreateOrViewDetails: String? = "Create"
    @JvmField var unreadList = ArrayList<UnreadCountModel>()
    @JvmField var VitacapeExtention: String? = "@devchat.vitacape.com"
    @JvmField var totalchatclientlist = JSONArray()
    @JvmField var groupsList_Access = ArrayList<GroupsModel>()
    @JvmField var ISPRODUCTION = false
    @JvmField var IS_STAGING = false
    @JvmField var Has_Meeting = false
    @JvmField var forgot_pwd_request = false
    @JvmField var Email: String? = ""
    @JvmField var Mobile: String? = ""
    @JvmField var Access_token: String? = ""
    @JvmField var Refresh_token: String? = ""
    @JvmField var FirmEmail: String? = ""
    @JvmField var PpView_Type: String? = ""
    @JvmField var BpEdit_Type: String? = ""
    @JvmField var firmProfileModel: FirmProfileModel? = null
    @JvmField var MATTER_TYPE: String? = ""
    @JvmField var Rel_Type: String? = ""
    @JvmField var is_CreateMatter = true
    @JvmField var msg_id: String? = ""
    @JvmField var GUID: String? = ""
    @JvmField var listid: String? = ""
    @JvmField var listid1: String? = ""
    @JvmField var part_id: String? = ""
    @JvmField var create_matter = true
    @JvmField var model = JSONArray()
    @JvmField var dashboardAllEndpoint: String? = "v3/dashboard"
    @JvmField var adminBaseURL: String? = "https://adminapi.dev2.digicoffer.com/"
    @JvmField var paymentUrl: String? = "https://staging.payment.digicoffer.com/renew?useremail"
    @JvmField var outlooklabel: String? = "outlook/label/"
    @JvmField var outlookauth: String? = "outlook/authurl?authtoken="
    @JvmField var outlookmsg: String? = "outlook/messages/"
    @JvmField var outlookattachupload: String? = "message/attachment/upload/"
    @JvmField var outlooksendmail: String? = "sendmail/attach/documents/"
    @JvmField var sending_mail: String? = ""
    @JvmField var emailsendmail: String? = "gmail/sendmail/attach/documents/"
    @JvmField var outlookdetails: String? = "outlook/message/detail/"
    @JvmField var AVClientChatUrl: String? = "https://dev.testavchat.digicoffer.com/"
    @JvmField var AppointmentListingUrl: String? = "https://apidev2.digicoffer.com/professional/v3/appointments"
    @JvmField var selectedMatterTab: String? = "Client"
    @JvmField var PROF_URL: String? = ""
    @JvmField var Firm_name: String? = ""
    @JvmField var firm_id: String? = ""
    @JvmField var Firm_names = ArrayList<String>()
    @JvmField var Firm_ids = ArrayList<String>()
    @JvmField var Firm_id: String? = ""
    @JvmField var hires_group: String? = ""
    @JvmField var hires_client: String? = ""
    @JvmField var NAME_NEW: String? = ""
    @JvmField var name: String? = ""
    @JvmField var isAlterPopup = false
    @JvmField var Matter_title: String? = ""
    @JvmField var Matter_id: String? = ""
    @JvmField var owner_id: String? = ""
    @JvmField var owner_name: String? = ""
    @JvmField var isClient_chat = true
    @JvmField var isGmail = true
    @JvmField var EMAIL_BASE_URL: String? = "https://mailapi.digicoffer.com/api/v1/"
    @JvmField var EMAIL_GET_URL: String? = ""
    @JvmField var getunreadcountlist_URL: String? = ""
    @JvmField var CONVERSATION_META_URL: String? = ""
    @JvmField var DocEditorListingUrl: String? = ""
    @JvmField var DocEditorUploadImageURL: String? = ""
    @JvmField var AVChatUrl: String? = ""
    @JvmField var saveLatexDoc: String? = ""
    @JvmField var saveAsLatexDoc: String? = ""
    @JvmField var LatexDocFile: String? = ""
    @JvmField var AVChatMeetingLinkUrl: String? = ""
    @JvmField var decryptUrl: String? = ""
    @JvmField var docfilepdfUrl: String? = ""
    @JvmField var dashboard_image: String? = ""
    @JvmField var doctopdfUrl: String? = ""
    @JvmField var OpenView_doc: String? = "https://stagingapi.latex.digicoffer.com/v1/document/openview/"
    @JvmField var Delete_doc: String? = "https:stagingapi.latex.digicoffer.com/v1/document/"
    @JvmField var email_info_alert: String? = "Please enter a valid email address"
    @JvmField var email_alert: String? = "Please Enter A Valid Email Address"
    @JvmField var cemail_alert: String? = "Email and confirm email mismatch, please check."
    @JvmField var BIZ_URL: String? = if (ISPRODUCTION) "https://api.digicoffer.com/business/" else "https://apidev.digicoffer.com/business/"
    const val EMAIL_UPLOAD_URL = "https://mailapi.digicoffer.com/api/v1/"
    const val EMAIL_LISTING_URL = "https://mailapi.digicoffer.com/api/v1/"
    const val gmail_label = "gmail/label/"
    const val gmail_messages = "gmail/messages/"
    @JvmField var mail_document: String? = ""
    const val gmail_document = "gmail/message/attachment/upload/"
    @JvmField var isGoogle = true
    @JvmField var USER_ID: String? = ""
    @JvmField var ID: String? = ""
    @JvmField var FIRM_NAME: String? = ""
    @JvmField var IS_ADMIN = true
    @JvmField var Valid_Token = true
    @JvmField var isCreate = false
    @JvmField var UID: String? = ""
    @JvmField var ROLE: String? = ""
    @JvmField var CATEGORY: String? = ""
    @JvmField var PASSWORD_MODE: String? = ""
    @JvmField var OLD_PASSWORD: String? = ""
    @JvmField var Profile_View: String? = "Bp"
    @JvmField var Edit_OR_View: String? = "View"
    @JvmField var isMyProfileClicked = true
    @JvmField var PK: String? = ""
    @JvmField var isSubscriptionEnded = false
    @JvmField var Groups = JSONArray()
    @JvmField var isAdmin = false
    @JvmField var Old_Token: String? = ""
    @JvmField var VERSION: String? = if (ISPRODUCTION) "1.0.2" else "1.0.29"
    @JvmField var XMPP_DOMAIN: String? = "devchat.vitacape.com"
    const val DOWNLOAD_VIEWFILE_TAG = "DOWNLOAD_VIEWFILE"
    @JvmField var teamResArray = JSONArray()
    @JvmField var teamMapChatList = HashMap<String, ArrayList<ChildDO>>()
    @JvmField var chat_SENT: String? = "SENT"
    @JvmField var jsonObject_dashboard: JSONObject? = null
    @JvmField var chat_RECEIVE: String? = "RECEIVE"
    @JvmField var meetingApiEndpoint: String? = "v3/dashboard/meeting/-330"
    @JvmField var chatRelationshipApiEndpoint: String? = "v3/dashboard/otherapi"
    @JvmField var Dashboard: String? = "v3/dashboard/layout"
    @JvmField var clientTeamApiEndpoint: String? = "v3/dashboard/chat-team"
    @JvmField var emailApiEndpoint: String? = "v3/dashboard/email"
    @JvmField var hoursEndpoint: String? = "v3/dashboard/hours"
    @JvmField var hiringEndpoint: String? = "v3/dashboard/hiring"
    @JvmField var storageEndpoint: String? = "v3/dashboard/storage"
    @JvmField var timesheetEndpoint: String? = "v3/dashboard/timesheet"
    @JvmField var matterEndpoint: String? = "v3/dashboard/matter"
    @JvmField var subscriptionEndpoint: String? = "v3/dashboard/subscription"
    @JvmField var groupsEndpoint: String? = "v3/dashboard/groupsandtms"
    @JvmField var newclientEndpoint: String? = "v3/dashboard/new-clients"
    @JvmField var externalCounselsEndpoint: String? = "v3/dashboard/external-counsels"
    @JvmField var relationshipsEndpoint: String? = "v3/dashboard/relationships"
    @JvmField var notificationEndpoint: String? = "v3/dashboard/notification"
    @JvmField var chatlistEndpoint: String? = "v3/chatlist"

    const val title = "title"
    const val author = "author"
    const val overview = "overview"
    const val docsection = "section"
    const val subSection = "subSection"
    const val subSubSection = "subSubSection"
    const val paragraph = "paragraph"
    const val orderedNumList = "orderedList"
    const val unorderedBulltedList = "unorderedList"
    const val pageBreak = "pageBreak"
    const val image = "image"
    const val table = "table"
    @JvmField var KPICARDS = ArrayList<Dashboard_Model>()
    @JvmField var MYDAYCARDS = ArrayList<Dashboard_Model>()

    @JvmStatic
    fun check_url() {
        if (ISPRODUCTION) {
            adminBaseURL = "https://adminapi.digicoffer.com/"
            AVChatMeetingLinkUrl = "https://avchat.digicoffer.com/api/v1/create-meeting-link"
            paymentUrl = "https://payment.digicoffer.com/renew?useremail"
            decryptUrl = "https://api.digicoffer.com/professional/v3/decrypt"
            docfilepdfUrl = "https://prod.utils.doc2pdf.digicoffer.com/api/v1/docfile2pdf"
            doctopdfUrl = "https://prod.utils.doc2pdf.digicoffer.com/api/v1/doc2pdf"
            PROF_URL = "https://api.digicoffer.com/professional/"
            AVChatUrl = "https://avchat.digicoffer.com/"
            XMPP_DOMAIN = "chat.vitacape.com"
            VitacapeExtention = "@chat.vitacape.com"
            getunreadcountlist_URL = "https://prod.utils.chat.digicoffer.com/api/v1/"
            CONVERSATION_META_URL = "https://prod.utils.chat.digicoffer.com/api/v1/conversation/meta"
            DocEditorListingUrl = "https://prod.utils.latex.digicoffer.com/v1/documents"
            OpenView_doc = "https://prod.utils.latex.digicoffer.com/v1/document/openview/"
            Delete_doc = "https://prod.utils.latex.digicoffer.com/v1/document/"
            saveLatexDoc = "https://prod.utils.latex.digicoffer.com/v1/document/page/"
            saveAsLatexDoc = "https://prod.utils.latex.digicoffer.com/v1/document/duplicate/"
            LatexDocFile = "https://prod.utils.latex.digicoffer.com/v1/document"
            DocEditorUploadImageURL = "https://prod.utils.latex.digicoffer.com/v1/upload"
            AVClientChatUrl = "https://avchat.digicoffer.com/"
            Notification_Base_Url = "https://fcm.digicoffer.com"
            if (!isGmail) {
                EMAIL_BASE_URL = "https://dev.utils.mail.digicoffer.com/outlook/"
                EMAIL_GET_URL = "https://dev.utils.mail.digicoffer.com/api/v1/outlook/"
                sending_mail = outlooksendmail
                mail_document = outlookattachupload
            } else {
                EMAIL_BASE_URL = "https://mailapi.digicoffer.com/api/v1/"
                sending_mail = emailsendmail
                mail_document = gmail_document
            }
        } else if (IS_STAGING) {
            adminBaseURL = "https://adminapi.staging.digicoffer.com/"
            paymentUrl = "https://staging.payment.digicoffer.com/renew?useremail"
            decryptUrl = "https://api.staging.digicoffer.com/professional/v3/decrypt"
            docfilepdfUrl = "https://dev.utils.doc2pdf.digicoffer.com/api/v1/docfile2pdf"
            doctopdfUrl = "https://dev.utils.doc2pdf.digicoffer.com/api/v1/doc2pdf"
            PROF_URL = "https://api.staging.digicoffer.com/professional/"
            AVChatUrl = "https://staging.avchat.digicoffer.com/"
            AVChatMeetingLinkUrl = "https://staging.api.avchat.digicoffer.com/api/v1/create-meeting-link"
            XMPP_DOMAIN = "devchat.vitacape.com"
            VitacapeExtention = "@devchat.vitacape.com"
            getunreadcountlist_URL = "https://dev.utils.chat.digicoffer.com/api/v1/"
            CONVERSATION_META_URL = "https://dev.utils.chat.digicoffer.com/api/v1/conversation/meta"
            DocEditorListingUrl = "https://stagingapi.latex.digicoffer.com/v1/documents"
            OpenView_doc = "https://stagingapi.latex.digicoffer.com/v1/document/openview/"
            Delete_doc = "https://stagingapi.latex.digicoffer.com/v1/document/"
            saveLatexDoc = "https://stagingapi.latex.digicoffer.com/v1/document/page/"
            saveAsLatexDoc = "https://stagingapi.latex.digicoffer.com/v1/document/duplicate/"
            LatexDocFile = "https://stagingapi.latex.digicoffer.com/v1/document"
            DocEditorUploadImageURL = "https://stagingapi.latex.digicoffer.com/v1/upload"
            AVClientChatUrl = "https://staging.avchat.digicoffer.com/"
            Notification_Base_Url = "https://fcm.digicoffer.com"
            if (!isGmail) {
                EMAIL_BASE_URL = "https://dev.utils.mail.digicoffer.com/outlook/"
                EMAIL_GET_URL = "https://dev.utils.mail.digicoffer.com/api/v1/outlook/"
                sending_mail = outlooksendmail
                mail_document = outlookattachupload
            } else {
                EMAIL_BASE_URL = "https://mailapi.digicoffer.com/api/v1/"
                sending_mail = emailsendmail
                mail_document = gmail_document
            }
        } else {
            adminBaseURL = "https://adminapi.dev2.digicoffer.com/"
            paymentUrl = "https://dev2.payment.digicoffer.com/renew?useremail"
            decryptUrl = "https://apidev2.digicoffer.com/professional/v3/decrypt"
            docfilepdfUrl = "https://dev.utils.doc2pdf.digicoffer.com/api/v1/docfile2pdf"
            doctopdfUrl = "https://dev.utils.doc2pdf.digicoffer.com/api/v1/doc2pdf"
            PROF_URL = "https://apidev2.digicoffer.com/professional/"
            AVChatUrl = "https://dev.testavchat.digicoffer.com/"
            XMPP_DOMAIN = "devchat.vitacape.com"
            AVChatMeetingLinkUrl = "https://devapi.testavchat.digicoffer.com/api/v1/create-meeting-link"
            VitacapeExtention = "@devchat.vitacape.com"
            getunreadcountlist_URL = "https://dev.utils.chat.digicoffer.com/api/v1/"
            CONVERSATION_META_URL = "https://dev.utils.chat.digicoffer.com/api/v1/conversation/meta"
            DocEditorListingUrl = "https://devapi.latex.digicoffer.com/v1/documents"
            OpenView_doc = "https://devapi.latex.digicoffer.com/v1/document/openview/"
            Delete_doc = "https://devapi.latex.digicoffer.com/v1/document/"
            saveLatexDoc = "https://devapi.latex.digicoffer.com/v1/document/page/"
            saveAsLatexDoc = "https://devapi.latex.digicoffer.com/v1/document/duplicate/"
            LatexDocFile = "https://devapi.latex.digicoffer.com/v1/document"
            DocEditorUploadImageURL = "https://devapi.latex.digicoffer.com/v1/upload"
            AVClientChatUrl = "https://dev.testavchat.digicoffer.com/"
            Notification_Base_Url = "https://fcm.digicoffer.com"
            if (!isGmail) {
                EMAIL_BASE_URL = "https://dev.utils.mail.digicoffer.com/outlook/"
                EMAIL_GET_URL = "https://dev.utils.mail.digicoffer.com/api/v1/outlook/"
                sending_mail = outlooksendmail
                mail_document = outlookattachupload
            } else {
                EMAIL_BASE_URL = "https://dev.utils.mail.digicoffer.com/api/v1/"
                sending_mail = emailsendmail
                mail_document = gmail_document
            }
        }
    }

    @JvmStatic
    fun restoreSessionFromPrefs(ctx: Context): Boolean {
        val prefs = ctx.getSharedPreferences("MyPrefs", Context.MODE_PRIVATE)
        val token = prefs.getString("Token", "") ?: ""
        val jsonKey = prefs.getString("Json_key", "") ?: ""
        val refresh_token = prefs.getString("refresh_token", "") ?: ""
        Constants.TOKEN = token
        Constants.Refresh_token = refresh_token
        if (token.isEmpty() || jsonKey.isEmpty()) return false

        return try {
            val userJson = JSONObject(jsonKey)

            Constants.NAME = userJson.getString("name")
            Constants.NAME_NEW = userJson.getString("name")
            Constants.USER_ID = userJson.getString("user_id")
            Constants.UID = userJson.getString("uid")
            Constants.PK = userJson.getString("pk")
            Constants.PASSWORD_MODE = userJson.getString("password_mode")
            Constants.IS_ADMIN = userJson.getBoolean("admin")
            Constants.FIRM_NAME = userJson.getString("firm_name")
            Constants.ROLE = userJson.getString("role")
            Constants.CATEGORY = userJson.optString("category")
            Constants.FirmEmail = userJson.optString("email")
            Constants.Groups = userJson.getJSONArray("groups")
            Constants.termsVersion = userJson.getString("termsVersion")
            Constants.requiresTermsAcceptance = userJson.optBoolean("requiresTermsAcceptance")
            Constants.Email = prefs.getString("email", "") ?: ""
            Constants.LOGIN_METHOD = prefs.getString("login_method", "email") ?: "email"
            Constants.OLD_PASSWORD = prefs.getString("password", "") ?: ""
            Constants.is_active = true

            val namesArray = JSONArray(prefs.getString("firmNames", "[]"))
            val idsArray = JSONArray(prefs.getString("firmIds", "[]"))
            Constants.Firm_names.clear()
            Constants.Firm_ids.clear()
            for (i in 0 until namesArray.length()) {
                Constants.Firm_names.add(namesArray.getString(i))
            }
            for (i in 0 until idsArray.length()) {
                Constants.Firm_ids.add(idsArray.getString(i))
            }

            Constants.isAdmin = "AAM" == Constants.ROLE 
                    || (Constants.Groups.length() == 1 
                    && Constants.Groups.getString(0) == "AAM")

            val sub = userJson.optJSONObject("subscription")
            if (sub != null) {
                Constants.is_active = true
                val feat = sub.optJSONObject("features")
                if (feat != null) {
                    Constants.FEATURES.clear()
                    val k = feat.keys()
                    while (k.hasNext()) {
                        val key = k.next()
                        Constants.FEATURES[key] = feat.optBoolean(key, false)
                    }
                }
            }

            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }
}
