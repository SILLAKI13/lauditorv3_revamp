package com.digicoffer.lauditor.CommonFiles.GlobalFiles;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.format.DateFormat;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.digicoffer.lauditor.Chat.Model.ChildDO;
import com.digicoffer.lauditor.Dashboard.DahboardModels.UnreadCountModel;
import com.digicoffer.lauditor.Dashboard.Dashboard;
import com.digicoffer.lauditor.FirmProfile.FirmProfileModel;
import com.digicoffer.lauditor.FirmProfile.ViewPracticePartnerAdapter;
import com.digicoffer.lauditor.LoginActivity.Models.Dashboard_Model;
import com.digicoffer.lauditor.LoginActivity.ViewModels.LoginActivity;
import com.digicoffer.lauditor.MainActivity;
import com.digicoffer.lauditor.Matter.ViewModels.GCT_En;
import com.digicoffer.lauditor.Matter.OldViewModels.MatterInformation;
import com.digicoffer.lauditor.Matter.ViewModels.MatterInformation_En;
import com.digicoffer.lauditor.Matter.Models.ClientGroupModel;
import com.digicoffer.lauditor.Matter.Models.ClientsModel;
import com.digicoffer.lauditor.Matter.Models.GroupsModel;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public class Constants {


    public static boolean isfwd_or_isbwd = false;
    public static boolean meeting_has_value = false;
    public static String DOWNLOAD_FILE_TAG = "DOWNLOAD_FILE";
    public static TextView notifyBadge;
    public static Boolean sessionExpired = false;
    public static String matterFilterType = "";
    // In Constants.java
    public static String LOGIN_METHOD = ""; // "email" or "mobile"
    public static String GeneratedMatterId = "";
    public static String TARGET_FIELD = "";   // e.g. "et_Email", "et_ContactPhone"
    public static String PROFILE_SECTION = "";   // "basic" | "additional" | "education" | "availability"
    public static String lastProfileContext = "";   // "firm" | "my"

    // Add this line with your other static fields in Constants.java
    public static String pendingFcmNavigation = "";
    public static String GeneratedMatterTitle = "";
    // Add this anywhere in your Constants.java class body:
    public static String DOCUMENT_TYPE = "matter";
    public static int User_Allowed = 0;
    public static boolean is_active = true;
    public static boolean isFromNotification = false;
    public static Bundle notificationBundle = new Bundle();
    public static Boolean issubscription = false;
    public static String pendingNotificationNavJson = "";
    public static final String NO_INTERNET_MSG = "No internet connection. Please try again.";
    public static String ContactName = "";
    public static String Notification_Base_Url;
    public static String product = "Lawyers";

    public static boolean is24HoursFormat(Context context) {
        return DateFormat.is24HourFormat(context);
    }

    public class NavKeys {
        public static final String MODULE = "module";
        public static final String ACTION = "action";
        public static final String VIEW_TYPE = "view_type";

        // Common notification params
        public static final String APPOINTMENT_ID = "appointment_id";
        public static final String EVENT_ID = "event_id";
        public static final String GROUPS_ID = "group_ids";
        public static final String CLIENT_ID = "client_id";
        public static final String GUID = "client_id";
        public static final String MATTER_ID = "matter_id";
        public static final String HIGHLIGHT_IDS = "highlight_ids";
        public static final String RELATIONSHIP_ID = "relationship_id";
        public static final String ROUTE_NAME = "route_name";
    }

    public static String pendingChatJid = "";   // guid + "_" + client_id
    public static String pendingChatName = "";
    public static String pendingChatSource = "";

    public static String PROFILE_EDIT_TAB = "practice_details";

    //ID Name Model
    public static class IdNameModel {
        String id;
        String name;
        String url;

        public String getId() {
            return id;
        }

        public void setId(String id) {
            this.id = id;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getUrl() {
            return url;
        }

        public void setUrl(String url) {
            this.url = url;
        }
    }

    public static HashMap<String, Boolean> FEATURES = new HashMap<>();

    public static boolean isFeatureEnabled(String key) {
        if (FEATURES.containsKey(key)) {
            return Boolean.TRUE.equals(FEATURES.get(key));
        }
        return true;
    }

    // In Constants.java
    public static ViewPracticePartnerAdapter firmProfileAdapter = null;
    public static String matterDate = "";
    public static Boolean show_register = false;
    public static ArrayList<ClientGroupModel> allClientGroups = new ArrayList<>();
    public static String DocTagType = "";
    public static String Timesheet_Card = "Myts";
    public static boolean is_ts_submitted = false;
    public static boolean is_ts_not_submitted = false;
    public static boolean ts_card_clicked = false;
    public static boolean isDocEditor = false;
    public static int tempPos = -1;
    public static String firm_image = "";
    public static String fromjid = "";
    public static String updatedFromIid = "";
    public static String message_id = "";
    public static String stanzaId = "";
    public static RecyclerView recyclerView = null;
    public static String Chat_id = "";
    public static ArrayList<ClientsModel> selected_temp_clients_list = new ArrayList<>();
    public static MatterInformation matterInformation = new MatterInformation();
    public static MatterInformation_En matterInformation_en = null;
    public static ArrayList<com.digicoffer.lauditor.Documents.Models.DocumentsModel> upload_documents_list = new ArrayList<>();
    public static GCT_En gct_en = null;
    public static ArrayList<UnreadCountModel> unreadcount_from_list = new ArrayList<>();
    public static ArrayList<UnreadCountModel> unreadclient_list = new ArrayList<>();
    public static ArrayList<UnreadCountModel> unreadteam_list = new ArrayList<>();
    public static String clientChat_count = "0";
    public static String teamChat_count = "0";
    public static boolean IS_MyDay = true;
    public static Dashboard dashboard;
    public static com.digicoffer.lauditor.Dashboard.NewRevampViewModels.Dashboard dashboard_en;
    public static ArrayList<IdNameModel> composAttachDocAry = new ArrayList<>();
    public static String base_URL = "http://10.0.2.2:8011/consumer/";
    public static String TOKEN = "";
    public static String guid = "";
    public static MainActivity mainActivity;
    public static LoginActivity loginActivity;
    public static JSONArray doc_id = new JSONArray();
    public static JSONArray ex_group_attachment = new JSONArray();
    public static JSONArray ex_client = new JSONArray();
    public static JSONArray clientList = new JSONArray();
    public static JSONArray corpclientList = new JSONArray();
    public static boolean Biometric_checked = false;
    public static boolean is_biometric = false;
    public static String NAME = "";
    public static String termsVersion = "v1.0";
    public static boolean requiresTermsAcceptance;
    public static String PROBIZ_TYPE = "";
    public static String ENTITY_ID = "";
    public static String pdfFilePath;
    public static int currentPage = 1;
    public static String is_meeting = "Create";
    public static String Matter_CreateOrViewDetails = "Create";
    public static ArrayList<UnreadCountModel> unreadList = new ArrayList<>();
    public static String VitacapeExtention = "@devchat.vitacape.com";
    public static JSONArray totalchatclientlist;
    public static ArrayList<GroupsModel> groupsList_Access = new ArrayList<>();
    public static Boolean ISPRODUCTION = false;
    public static Boolean IS_STAGING = false;
    public static boolean Has_Meeting = false;
    public static boolean forgot_pwd_request = false;
    public static String Email = "";
    public static String Mobile = "";
    public static String Access_token = "";
    public static String Refresh_token = "";
    public static String FirmEmail = "";
    public static String PpView_Type = "";
    public static String BpEdit_Type = "";
    public static FirmProfileModel firmProfileModel;
    public static String MATTER_TYPE = "";
    public static String Rel_Type = "";
    public static boolean is_CreateMatter = true;
    public static String msg_id = "";
    public static String GUID = "";
    public static String listid = "";
    public static String listid1 = "";
    public static String part_id = "";
    public static boolean create_matter = true;
    public static JSONArray model = new JSONArray();
    public static String dashboardAllEndpoint = "v3/dashboard";
    public static String adminBaseURL = "https://adminapi.dev2.digicoffer.com/";
    public static String paymentUrl = "https://staging.payment.digicoffer.com/renew?useremail";
    public static String outlooklabel = "outlook/label/";
    public static String outlookauth = "outlook/authurl?authtoken=";
    public static String outlookmsg = "outlook/messages/";
    public static String outlookattachupload = "message/attachment/upload/";
    public static String outlooksendmail = "sendmail/attach/documents/";
    public static String sending_mail = "";
    public static String emailsendmail = "gmail/sendmail/attach/documents/";
    public static String outlookdetails = "outlook/message/detail/";
    public static String AVClientChatUrl = "https://dev.testavchat.digicoffer.com/";
    public static String AppointmentListingUrl = "https://apidev2.digicoffer.com/professional/v3/appointments";
    //    public static String MyDay_KPI =
    public static String selectedMatterTab = "Client";
    public static String PROF_URL;
    public static String Firm_name = "";
    public static String firm_id = "";
    public static ArrayList<String> Firm_names = new ArrayList<>();
    public static ArrayList<String> Firm_ids = new ArrayList<>();

    public static String Firm_id = "";
    public static String hires_group = "";
    public static String hires_client = "";
    public static String NAME_NEW = "";
    public static String name = "";
    public static boolean isAlterPopup = false;
    public static String Matter_title;
    public static String Matter_id = "";
    public static String owner_id = "";
    public static String owner_name = "";
    public static boolean isClient_chat = true;
    public static boolean isGmail = true;
    public static String EMAIL_BASE_URL = "https://mailapi.digicoffer.com/api/v1/";
    public static String EMAIL_GET_URL = "";
    public static String getunreadcountlist_URL;
    public static String CONVERSATION_META_URL;
    public static String DocEditorListingUrl = "";
    public static String DocEditorUploadImageURL = "";
    public static String AVChatUrl = "";
    public static String saveLatexDoc = "";
    public static String saveAsLatexDoc = "";
    public static String LatexDocFile = "";
    public static String AVChatMeetingLinkUrl = "";
    public static String decryptUrl = "";
    public static String docfilepdfUrl = "";
    public static String dashboard_image = "";
    public static String doctopdfUrl = "";
    public static String OpenView_doc = "https://stagingapi.latex.digicoffer.com/v1/document/openview/";
    public static String Delete_doc = "https:stagingapi.latex.digicoffer.com/v1/document/";
    public static String email_info_alert = "Please enter a valid email address";
    public static String email_alert = "Please Enter A Valid Email Address";
    public static String cemail_alert = "Email and confirm email mismatch, please check.";
    public static ArrayList<Dashboard_Model> KPICARDS = new ArrayList<>();
    public static ArrayList<Dashboard_Model> MYDAYCARDS = new ArrayList<>();

    public static void check_url() {
        if (ISPRODUCTION) {
            adminBaseURL = "https://adminapi.digicoffer.com/";
            AVChatMeetingLinkUrl = "https://avchat.digicoffer.com/api/v1/create-meeting-link";
            paymentUrl = "https://payment.digicoffer.com/renew?useremail";
            decryptUrl = "https://api.digicoffer.com/professional/v3/decrypt";
            docfilepdfUrl = "https://prod.utils.doc2pdf.digicoffer.com/api/v1/docfile2pdf";
            doctopdfUrl = "https://prod.utils.doc2pdf.digicoffer.com/api/v1/doc2pdf";
            PROF_URL = "https://api.digicoffer.com/professional/";
            AVChatUrl = "https://avchat.digicoffer.com/";
            XMPP_DOMAIN = "chat.vitacape.com";
            VitacapeExtention = "@chat.vitacape.com";
            getunreadcountlist_URL = "https://prod.utils.chat.digicoffer.com/api/v1/";
            CONVERSATION_META_URL =  "https://prod.utils.chat.digicoffer.com/api/v1/conversation/meta";
            DocEditorListingUrl = "https://prod.utils.latex.digicoffer.com/v1/documents";
            OpenView_doc = "https://prod.utils.latex.digicoffer.com/v1/document/openview/";
            Delete_doc = "https://prod.utils.latex.digicoffer.com/v1/document/";
            saveLatexDoc = "https://prod.utils.latex.digicoffer.com/v1/document/page/";
            saveAsLatexDoc = "https://prod.utils.latex.digicoffer.com/v1/document/duplicate/";
            LatexDocFile = "https://prod.utils.latex.digicoffer.com/v1/document";
            DocEditorUploadImageURL = "https://prod.utils.latex.digicoffer.com/v1/upload";
            AVClientChatUrl = "https://avchat.digicoffer.com/";

            Notification_Base_Url = "https://fcm.digicoffer.com";
            if (!isGmail) {
                EMAIL_BASE_URL = "https://dev.utils.mail.digicoffer.com/outlook/";
                EMAIL_GET_URL = "https://dev.utils.mail.digicoffer.com/api/v1/outlook/";
                sending_mail = outlooksendmail;
                mail_document = outlookattachupload;
            } else {
                EMAIL_BASE_URL = "https://mailapi.digicoffer.com/api/v1/";
                sending_mail = emailsendmail;
                mail_document = gmail_document;
            }
        } else if (IS_STAGING) {
            adminBaseURL = "https://adminapi.staging.digicoffer.com/";
            paymentUrl = "https://staging.payment.digicoffer.com/renew?useremail";
            decryptUrl = "https://api.staging.digicoffer.com/professional/v3/decrypt";
            docfilepdfUrl = "https://dev.utils.doc2pdf.digicoffer.com/api/v1/docfile2pdf";
            doctopdfUrl = "https://dev.utils.doc2pdf.digicoffer.com/api/v1/doc2pdf";
            PROF_URL = "https://api.staging.digicoffer.com/professional/";
            AVChatUrl = "https://staging.avchat.digicoffer.com/";
            AVChatMeetingLinkUrl = "https://staging.api.avchat.digicoffer.com/api/v1/create-meeting-link";
            XMPP_DOMAIN = "devchat.vitacape.com";
            VitacapeExtention = "@devchat.vitacape.com";
            getunreadcountlist_URL = "https://dev.utils.chat.digicoffer.com/api/v1/";
            CONVERSATION_META_URL =   "https://dev.utils.chat.digicoffer.com/api/v1/conversation/meta";
            DocEditorListingUrl = "https://stagingapi.latex.digicoffer.com/v1/documents";
            OpenView_doc = "https://stagingapi.latex.digicoffer.com/v1/document/openview/";
            Delete_doc = "https://stagingapi.latex.digicoffer.com/v1/document/";
            saveLatexDoc = "https://stagingapi.latex.digicoffer.com/v1/document/page/";
            saveAsLatexDoc = "https://stagingapi.latex.digicoffer.com/v1/document/duplicate/";
            LatexDocFile = "https://stagingapi.latex.digicoffer.com/v1/document";
            DocEditorUploadImageURL = "https://stagingapi.latex.digicoffer.com/v1/upload";
            AVClientChatUrl = "https://staging.avchat.digicoffer.com/";
            Notification_Base_Url = "https://fcm.digicoffer.com";
            if (!isGmail) {
                EMAIL_BASE_URL = "https://dev.utils.mail.digicoffer.com/outlook/";
                EMAIL_GET_URL = "https://dev.utils.mail.digicoffer.com/api/v1/outlook/";
                sending_mail = outlooksendmail;
                mail_document = outlookattachupload;
            } else {
                EMAIL_BASE_URL = "https://mailapi.digicoffer.com/api/v1/";
                sending_mail = emailsendmail;
                mail_document = gmail_document;
            }
        } else {
            adminBaseURL = "https://adminapi.dev2.digicoffer.com/";
            paymentUrl = "https://dev2.payment.digicoffer.com/renew?useremail";
            decryptUrl = "https://apidev2.digicoffer.com/professional/v3/decrypt";
            docfilepdfUrl = "https://dev.utils.doc2pdf.digicoffer.com/api/v1/docfile2pdf";
            doctopdfUrl = "https://dev.utils.doc2pdf.digicoffer.com/api/v1/doc2pdf";
            PROF_URL = "https://apidev2.digicoffer.com/professional/";
            AVChatUrl = "https://dev.testavchat.digicoffer.com/";
            XMPP_DOMAIN = "devchat.vitacape.com";
            AVChatMeetingLinkUrl = "https://devapi.testavchat.digicoffer.com/api/v1/create-meeting-link";
            VitacapeExtention = "@devchat.vitacape.com";
            getunreadcountlist_URL = "https://dev.utils.chat.digicoffer.com/api/v1/";
            CONVERSATION_META_URL =   "https://dev.utils.chat.digicoffer.com/api/v1/conversation/meta";
            DocEditorListingUrl = "https://devapi.latex.digicoffer.com/v1/documents";
            OpenView_doc = "https://devapi.latex.digicoffer.com/v1/document/openview/";
            Delete_doc = "https://devapi.latex.digicoffer.com/v1/document/";
            saveLatexDoc = "https://devapi.latex.digicoffer.com/v1/document/page/";
            saveAsLatexDoc = "https://devapi.latex.digicoffer.com/v1/document/duplicate/";
            LatexDocFile = "https://devapi.latex.digicoffer.com/v1/document";
            DocEditorUploadImageURL = "https://devapi.latex.digicoffer.com/v1/upload";
            AVClientChatUrl = "https://dev.testavchat.digicoffer.com/";
            Notification_Base_Url = "https://fcm.digicoffer.com";
            if (!isGmail) {
                EMAIL_BASE_URL = "https://dev.utils.mail.digicoffer.com/outlook/";
                EMAIL_GET_URL = "https://dev.utils.mail.digicoffer.com/api/v1/outlook/";
                sending_mail = outlooksendmail;
                mail_document = outlookattachupload;
            } else {
                EMAIL_BASE_URL = "https://dev.utils.mail.digicoffer.com/api/v1/";
                sending_mail = emailsendmail;
                mail_document = gmail_document;
            }
        }
    }

    //https://api.digicoffer.com/professional/login

//    https://apidev2.digicoffer.com/professional/login

    //    https://api.staging.digicoffer.com/professional/login
//    public static String PROF_URL = ISPRODUCTION ? "https://api.digicoffer.com/professional/" : "https://apidev2.digicoffer.com/professional/";
    public static String BIZ_URL = ISPRODUCTION ? "https://api.digicoffer.com/business/" : "https://apidev.digicoffer.com/business/";
    //    public static final String EMAIL_BASE_URL = ISPRODUCTION ? "https://mailapi.digicoffer.com/api/v1/" : "https://dev.utils.mail.digicoffer.com/";
    public static final String EMAIL_UPLOAD_URL = "https://mailapi.digicoffer.com/api/v1/";
    public static final String EMAIL_LISTING_URL = "https://mailapi.digicoffer.com/api/v1/";

    public static final String gmail_label = "gmail/label/";
    public static final String gmail_messages = "gmail/messages/";
    public static String mail_document = "";
    public static final String gmail_document = "gmail/message/attachment/upload/";
    public static boolean isGoogle = true;
    //    public static String PROF_URL = "http://10.0.2.2:8011/professional/";
//    public static String BIZ_URL = "http://10.0.2.2:8011/business/";
//    public static String PROF_URL = "http://10.0.2.2:8011/professional/";
//    public static String BIZ_URL = "http://10.0.2.2:8011/business/";
//    public static String base_URL = "";
    //    public static String base_URL = "https://apidev.digicoffer.com/business/";
    public static String USER_ID = "";
    public static String ID = "";
    public static boolean isCreate = false;
    public static String FIRM_NAME = "";
    public static boolean IS_ADMIN = true;
    public static boolean Valid_Token = true;
    public static String UID = "";
    public static String ROLE = "";
    public static String CATEGORY = "";
    public static String PASSWORD_MODE = "";
    public static String OLD_PASSWORD = "";
    public static String Profile_View = "Bp";

    public static String Edit_OR_View = "View";
    public static boolean isMyProfileClicked = true;
    public static String PK = "";
    public static boolean isSubscriptionEnded = false;
    public static JSONArray Groups;
    public static boolean isAdmin = false;
    public static String Old_Token = "";
    public static String VERSION = ISPRODUCTION ? "1.0.2" : "1.0.29";
    //    public static String XMPP_DOMAIN = "dev.chat.digisecitus.com";
    public static String XMPP_DOMAIN = "devchat.vitacape.com";
    //    public static String XMPP_DOMAIN = ISPRODUCTION ? "chat.digicoffer.com" : "devchat.vitacape.com";
    public static String DOWNLOAD_VIEWFILE_TAG = "DOWNLOAD_VIEWFILE";

    //public static String email = "akhilaTM2@mailinator.com";
//    public static String email = "akhila.bs@lauditor.com";//akhila.bs@lauditor.com, soundaryavembaiyan@yahoo.com
//    public static String password = "Test@123";

    public static JSONArray teamResArray = new JSONArray();

    public static Map<String, ArrayList<ChildDO>> teamMapChatList = new HashMap<String, ArrayList<ChildDO>>();

    public static String chat_SENT = "SENT";
    public static JSONObject jsonObject_dashboard;
    public static String chat_RECEIVE = "RECEIVE";
    public static String meetingApiEndpoint = "v3/dashboard/meeting/-330";
    public static String chatRelationshipApiEndpoint = "v3/dashboard/otherapi";
    public static String Dashboard = "v3/dashboard/layout";
    public static String clientTeamApiEndpoint = "v3/dashboard/chat-team";
    public static String emailApiEndpoint = "v3/dashboard/email";
    public static String hoursEndpoint = "v3/dashboard/hours";
    public static String hiringEndpoint = "v3/dashboard/hiring";
    public static String storageEndpoint = "v3/dashboard/storage";
    public static String timesheetEndpoint = "v3/dashboard/timesheet";
    public static String matterEndpoint = "v3/dashboard/matter";
    public static String subscriptionEndpoint = "v3/dashboard/subscription";
    public static String groupsEndpoint = "v3/dashboard/groupsandtms";
    public static String newclientEndpoint = "v3/dashboard/new-clients";
    public static String externalCounselsEndpoint = "v3/dashboard/external-counsels";
    public static String relationshipsEndpoint = "v3/dashboard/relationships";
    public static String notificationEndpoint = "v3/dashboard/notification";
    //https://api.staging.digicoffer.com/professional/v3/chatlist
    //https://dev.utils.chat.digicoffer.com/api/v1/getunreadcountlist
    public static String chatlistEndpoint = "v3/chatlist";


    //DocEditor Models
    public static final String title = "title";
    public static final String author = "author";
    public static final String overview = "overview";
    public static final String docsection = "section";
    public static final String subSection = "subSection";
    public static final String subSubSection = "subSubSection";
    public static final String paragraph = "paragraph";
    public static final String orderedNumList = "orderedList";
    public static final String unorderedBulltedList = "unorderedList";
    public static final String pageBreak = "pageBreak";
    public static final String image = "image";
    public static final String table = "table";

    public static boolean restoreSessionFromPrefs(Context ctx) {
        SharedPreferences prefs = ctx.getSharedPreferences("MyPrefs", Context.MODE_PRIVATE);
        String token = prefs.getString("Token", "");
        String jsonKey = prefs.getString("Json_key", "");
        String refresh_token = prefs.getString("refresh_token", "");
        Constants.TOKEN = token;
        Constants.Refresh_token = refresh_token;
        if (token.isEmpty() || jsonKey.isEmpty()) return false;

        try {
            JSONObject userJson = new JSONObject(jsonKey);

            Constants.NAME = userJson.getString("name");
            Constants.NAME_NEW = userJson.getString("name");
            Constants.USER_ID = userJson.getString("user_id");
            Constants.UID = userJson.getString("uid");
            Constants.PK = userJson.getString("pk");
            Constants.PASSWORD_MODE = userJson.getString("password_mode");
            Constants.IS_ADMIN = userJson.getBoolean("admin");
            Constants.FIRM_NAME = userJson.getString("firm_name");
            Constants.ROLE = userJson.getString("role");
            Constants.CATEGORY = userJson.optString("category");
            Constants.FirmEmail = userJson.optString("email");
            Constants.Groups = userJson.getJSONArray("groups");
            Constants.termsVersion = userJson.getString("termsVersion");
            Constants.requiresTermsAcceptance = userJson.optBoolean("requiresTermsAcceptance");
            Constants.Email = prefs.getString("email", "");
            Constants.LOGIN_METHOD = prefs.getString("login_method", "email");
            Constants.OLD_PASSWORD = prefs.getString("password", "");
            Constants.is_active = true;

            // Firm arrays
            JSONArray namesArray = new JSONArray(prefs.getString("firmNames", "[]"));
            JSONArray idsArray = new JSONArray(prefs.getString("firmIds", "[]"));
            Constants.Firm_names.clear();
            Constants.Firm_ids.clear();
            for (int i = 0; i < namesArray.length(); i++)
                Constants.Firm_names.add(namesArray.getString(i));
            for (int i = 0; i < idsArray.length(); i++)
                Constants.Firm_ids.add(idsArray.getString(i));

            // Admin flag
            Constants.isAdmin = "AAM".equals(Constants.ROLE)
                    || (Constants.Groups.length() == 1
                    && Constants.Groups.getString(0).equals("AAM"));

            // Subscription / features
            JSONObject sub = userJson.optJSONObject("subscription");
            if (sub != null) {
                Constants.is_active = sub.optBoolean("is_active", true);
                JSONObject feat = sub.optJSONObject("features");
                if (feat != null) {
                    Constants.FEATURES.clear();
                    Iterator<String> k = feat.keys();
                    while (k.hasNext()) {
                        String key = k.next();
                        Constants.FEATURES.put(key, feat.optBoolean(key, false));
                    }
                }
            }

            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
}
