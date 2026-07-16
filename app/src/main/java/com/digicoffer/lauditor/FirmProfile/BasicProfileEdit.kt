package com.digicoffer.lauditor.FirmProfile

import android.annotation.SuppressLint
import android.app.Activity
import android.app.Dialog
import android.app.TimePickerDialog
import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.graphics.Color
import android.graphics.Typeface
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.text.Editable
import android.text.InputFilter
import android.text.InputType
import android.text.TextUtils
import android.text.TextWatcher
import android.util.Log
import android.util.TypedValue
import android.view.Gravity
import android.view.KeyEvent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.widget.*
import androidx.activity.OnBackPressedCallback
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.NonNull
import androidx.annotation.Nullable
import androidx.appcompat.widget.AppCompatButton
import androidx.appcompat.widget.AppCompatImageButton
import androidx.appcompat.widget.LinearLayoutCompat
import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.digicoffer.lauditor.MainActivity
import com.digicoffer.lauditor.R
import com.digicoffer.lauditor.Webservice.AsyncTaskCompleteListener
import com.digicoffer.lauditor.Webservice.HttpResultDo
import com.digicoffer.lauditor.Webservice.CommonApiHelper.WebServiceHelper
import com.digicoffer.lauditor.CommonFiles.GlobalFiles.AndroidUtils
import com.digicoffer.lauditor.CommonFiles.CacheUtils.AppImageCache
import com.digicoffer.lauditor.CommonFiles.CommonAdapters.CommonMultiSelectionAdapter
import com.digicoffer.lauditor.CommonFiles.GlobalFiles.Constants
import com.digicoffer.lauditor.CommonFiles.ValidationUtils.DescriptionValidation
import com.digicoffer.lauditor.CommonFiles.GlobalFiles.DynamicUtils
import com.digicoffer.lauditor.CommonFiles.GlobalFiles.NewModel
import com.digicoffer.lauditor.CommonFiles.ValidationUtils.Validation
import com.digicoffer.lauditor.CommonFiles.CommonAdapters.CommonSpinnerAdapter
import com.digicoffer.lauditor.Relationships.Model.CountriesDO
import com.google.android.flexbox.FlexboxLayout
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipGroup
import com.google.android.material.tabs.TabLayout
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream
import java.io.OutputStream
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList
import kotlin.collections.HashMap

class BasicProfileEdit(
    var firmProfile: FirmProfile,
    var isProfileEdit: Boolean,
    var isadditionalinfo: Boolean
) : Fragment(), AsyncTaskCompleteListener, View.OnClickListener {

    var tv_BasicProfile: TextView? = null
    var tv_Firmname: TextView? = null
    var tv_Country: TextView? = null
    var tv_Email: TextView? = null
    var tv_contactName: TextView? = null
    var tv_ContactPhone: TextView? = null
    var tv_Website: TextView? = null
    var tv_DefaultCurrency: TextView? = null
    var tv_nationality: TextView? = null
    var tv_dob: TextView? = null
    var tv_bio: TextView? = null

    var optional_txt: TextView? = null
    var tv_generate: TextView? = null
    var tv_RegisterAddress: TextView? = null
    var tv_gender: TextView? = null
    var tv_Building: TextView? = null
    var tv_Street: TextView? = null
    var tv_City: TextView? = null
    var tv_State: TextView? = null
    var tv_ad_Country: TextView? = null
    var tv_Zip: TextView? = null
    var tv_mailingAddressnNote: TextView? = null
    var tv_yes: TextView? = null
    var tv_no: TextView? = null
    var etPractice: TextView? = null

    var et_Firmname: TextInputEditText? = null
    var et_Email: TextInputEditText? = null
    var et_contactName: TextInputEditText? = null
    var et_ContactPhone: TextInputEditText? = null
    var et_Website: TextInputEditText? = null
    var et_nationality: TextInputEditText? = null
    var et_bio: TextInputEditText? = null
    var etExperience: TextInputEditText? = null

    var et_dob: AppCompatButton? = null
    var allCourtTypesList: ArrayList<String> = ArrayList()
    var et_confees: TextView? = null
    var tv_minus: TextView? = null
    var tv_plus: TextView? = null
    var ll_et_confees: LinearLayout? = null
    var tl_generate: LinearLayout? = null
    var ll_confee_row: LinearLayoutCompat? = null
    var sp_et_DefaultCurrency: ListView? = null
    var sp_practice: RecyclerView? = null
    var rvServicesList: RecyclerView? = null
    var cbYes: CheckBox? = null
    var isCaseTypeChecked = true
    var iv_profile: ImageView? = null
    var iv_delete_circle: ImageView? = null
    var practiceAreaAdapter: CommonMultiSelectionAdapter? = null
    var dayAvailability: MutableMap<String, Array<String>> = HashMap()
    val practiceAreas: MutableList<String> = ArrayList()
    val languagesSpoken: MutableList<String> = ArrayList()
    val servicesOffered: MutableList<String> = ArrayList()
    var llEducation: LinearLayout? = null
    var llCertification: LinearLayout? = null
    var llAwards: LinearLayout? = null
    var etLanguages: TextInputEditText? = null
    var etServices: TextInputEditText? = null
    var etFirmName: TextInputEditText? = null
    var et_counsel_id: TextInputEditText? = null
    var tl_practice_area: LinearLayout? = null
    var ll_et_DefaultCurrency: LinearLayout? = null
    var ll_gender: LinearLayout? = null

    // Services Offered API
    var suggestedServicesList: ArrayList<String> = ArrayList()
    var allServicesList: ArrayList<String> = ArrayList()
    var searchedServicesList: ArrayList<String> = ArrayList()
    var servicesDropdownAdapter: CommonMultiSelectionAdapter? = null
    var isServicesDropdownOpen = false

    // Court States / Suggest API
    var courtStateList: ArrayList<String> = ArrayList()
    var courtStatesMap: MutableMap<String, ArrayList<String>> = HashMap()
    var suggestedCourtTypesList: ArrayList<String> = ArrayList()
    var pendingCourtStatesCallback: Runnable? = null
    var ll_suggestedServices: LinearLayout? = null
    var ll_suggestedCourtType: LinearLayout? = null
    var pendingCourtSuggestCallback: Runnable? = null

    var isSameAddressChecked = false

    // Pre-built from HIGH_COURTS API
    var highCourtStateList: ArrayList<String> = ArrayList()
    var highCourtCitiesMap: MutableMap<String, ArrayList<String>> = HashMap()
    var highCourtNameMap: MutableMap<String, String> = HashMap() // state -> court name
    var requestCameraPermission: ActivityResultLauncher<String>? = null
    var cameraImageUri: Uri? = null
    var gender_list: ArrayList<String> = ArrayList()
    var currency_list: ArrayList<String> = ArrayList()
    var default_currency = ""
    var gender = ""

    var et_Building: TextInputEditText? = null
    var et_Street: TextInputEditText? = null
    var et_City: TextInputEditText? = null
    var et_State: TextInputEditText? = null
    var et_Zip: TextInputEditText? = null

    var img_dropdown_icon_Country: ImageView? = null
    var img_clear_icon_Country: ImageView? = null
    var img_dropdown_icon_adCountry: ImageView? = null
    var img_clear_icon_adCountry: ImageView? = null
    var img_dropdown_icon_gender: ImageView? = null
    var img_clear_icon_gender: ImageView? = null
    var img_dropdown_icon_madCountry: ImageView? = null
    var img_clear_icon_madCountry: ImageView? = null
    var img_dropdown_icon_Currency: ImageView? = null
    var img_clear_icon_Currency: ImageView? = null
    var iv_cancel: ImageView? = null
    var iv_edit_circle: ImageView? = null
    var iv_cancel_add: ImageView? = null

    var tv_MailingAddress: TextView? = null
    var mtv_Building: TextView? = null
    var mtv_Street: TextView? = null
    var mtv_City: TextView? = null
    var mtv_State: TextView? = null
    var mtv_ad_Country: TextView? = null
    var mtv_Zip: TextView? = null

    var met_Building: TextInputEditText? = null
    var met_Street: TextInputEditText? = null
    var met_City: TextInputEditText? = null
    var met_State: TextInputEditText? = null
    var met_Zip: TextInputEditText? = null

    var met_ad_gender: TextView? = null
    var met_ad_Country: TextView? = null
    var et_ad_Country: TextView? = null
    var et_Country: TextView? = null
    var et_DefaultCurrency: TextView? = null

    var ll_et_gender: LinearLayout? = null
    var ll_met_ad_country: LinearLayout? = null
    var ll_et_ad_country: LinearLayout? = null
    var ll_et_country: LinearLayout? = null

    var sp_met_ad_country: ListView? = null
    var sp_et_ad_country: ListView? = null
    var sp_et_country: ListView? = null
    var sp_et_gender: ListView? = null

    var btn_cancel_pp: Button? = null
    var btn_save_pp: Button? = null
    var btn_cancel_ai: Button? = null
    var btn_save_ai: Button? = null

    var progress_dialog: Dialog? = null
    var country_name1 = ""
    var country_name2 = ""
    var country_name3 = ""
    var practice = ""

    var iscountry_checked1 = true
    var iscountry_checked2 = true
    var iscountry_checked3 = true
    var iscurrency_checked = true
    var isgender_checked = true

    var firmProfileModel: FirmProfileModel? = null

    var emailhaserror = false
    var phonehaserror = false
    var ziphaserror = false
    var mziphaserror = false

    var countriesList: ArrayList<CountriesDO> = ArrayList()

    var tvErrorEmail: TextView? = null
    var tvErrorZip: TextView? = null
    var tvErrorMzip: TextView? = null
    var tvErrorPhone: TextView? = null
    var person_icon: TextView? = null
    var tv_add_info: TextView? = null

    var mViewModel: NewModel? = null
    var profileUrl = ""

    var llViewScreen: LinearLayout? = null
    var chipPractice: ChipGroup? = null
    var chipLanguages: ChipGroup? = null
    var chipServices: ChipGroup? = null
    var tabLayout: TabLayout? = null
    var ll_add_edit: LinearLayout? = null
    var ll_edit_Bp: LinearLayout? = null
    var genderlayout: LinearLayoutCompat? = null
    var doblayout: LinearLayoutCompat? = null
    var firmlayout: LinearLayoutCompat? = null
    var nationalitylayout: LinearLayoutCompat? = null
    var countrylayout: LinearLayoutCompat? = null
    var billing_layout: LinearLayoutCompat? = null

    var caseTypeList: ArrayList<String> = ArrayList()
    var cameraLauncher: ActivityResultLauncher<Uri>? = null
    var galleryLauncher: ActivityResultLauncher<Intent>? = null
    var firmProfileModelArrayList: ArrayList<FirmProfileModel> = ArrayList()

    var excludedSlotsMap: MutableMap<String, List<String>> = HashMap()
    var img_dropdown_icon: ImageView? = null
    var img_clear_icon: ImageView? = null
    var imgServicesDrop: ImageView? = null

    var llCourtEnrollments: LinearLayout? = null
    var originalCasesHandled: JSONArray? = null
    var chipCasesHandled: ChipGroup? = null
    var originalValues: MutableMap<String, Any> = HashMap()
    var originalPracticeAreas: JSONArray? = null
    var originalLanguages: JSONArray? = null
    var originalServices: JSONArray? = null
    var originalEducation: JSONArray? = null
    var originalCertifications: JSONArray? = null
    var originalAwards: JSONArray? = null
    var originalWeeklySchedule: List<FirmProfileModel.WeeklySchedule>? = null
    var highCourtsList: ArrayList<CourtData> = ArrayList()
    var supremeCourtData: CourtData? = null
    var selectedState = ""
    var selectedCity = ""
    var selectedCourtName = ""

    // Spinner adapters
    var genderAdapter: CommonSpinnerAdapter<*>? = null
    var currencyAdapter: CommonSpinnerAdapter<*>? = null
    var adCountryAdapter: CommonSpinnerAdapter<*>? = null
    var madCountryAdapter: CommonSpinnerAdapter<*>? = null

    // Live chip state
    var liveChipPracticeAreas: JSONArray? = null
    var liveChipLanguages: JSONArray? = null
    var liveChipServices: JSONArray? = null

    companion object {
        private const val REQ_CAMERA = 101
        private const val REQ_GALLERY = 102
        private const val TYPE_EDUCATION = 1
        private const val TYPE_CERTIFICATION = 2
        private const val TYPE_AWARD = 3
        private const val SLOT_DURATION_MIN = 30
        private const val MAX_EXCLUDED_SLOTS = 4
        private const val COURT_CITY_OTHER_OPTION = "+ Other (enter city)"
        private const val SERVICES_OTHER_OPTION = "+ Other (enter custom)"

        @JvmStatic
        fun formatHoursMinutes(minutes: Long): String {
            val hours = minutes / 60
            val mins = minutes % 60
            return if (mins == 0L) {
                "$hours hours"
            } else {
                "${hours}h ${mins}m"
            }
        }

        @JvmStatic
        fun to24Hour(time12h: String): String {
            val input = SimpleDateFormat("hh:mm a", Locale.US)
            val output = SimpleDateFormat("HH:mm", Locale.US)
            return output.format(input.parse(time12h) ?: Date())
        }

        @JvmStatic
        fun slotToJson(slotText: String): JSONObject {
            val parts = slotText.split(" - ")
            val obj = JSONObject()
            obj.put("start_time", to24Hour(parts[0]))
            obj.put("end_time", to24Hour(parts[1]))
            return obj
        }

        @JvmStatic
        fun convertCourtTypeToApiFormat(displayType: String): String {
            if (displayType.equals("Supreme Court", ignoreCase = true)) {
                return "supreme_court"
            } else if (displayType.equals("High Court", ignoreCase = true)) {
                return "high_court"
            } else if (displayType.equals("District & Other Lower Courts", ignoreCase = true)) {
                return "district_court"
            }
            return displayType.lowercase().replace(" ", "_")
        }

        @JvmStatic
        fun convertApiToDisplayFormat(apiType: String): String {
            if (apiType.equals("supreme_court", ignoreCase = true)) {
                return "Supreme Court"
            } else if (apiType.equals("high_court", ignoreCase = true)) {
                return "High Court"
            } else if (apiType.equals("district_court", ignoreCase = true)) {
                return "District & Other Lower Courts"
            }
            return apiType.replace("_", " ").split(" ").joinToString(" ") { it.replaceFirstChar { char -> if (char.isLowerCase()) char.titlecase(Locale.ROOT) else it } }
        }

        @JvmStatic
        fun formatFieldName(field: String?): String {
            if (field.isNullOrEmpty()) return field ?: ""
            val words = field.split("_")
            val sb = StringBuilder()
            for (word in words) {
                if (sb.isNotEmpty()) sb.append(" ")
                if (word.isNotEmpty()) {
                    sb.append(word.substring(0, 1).uppercase()).append(word.substring(1))
                }
            }
            return sb.toString()
        }
    }

    override fun onClick(view: View) {
        // Handle global click actions if any
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.basic_profile_edit, container, false)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setupOnBackPressed()
        cameraLauncher = registerForActivityResult(
            ActivityResultContracts.TakePicture()
        ) { success ->
            if (success && cameraImageUri != null) {
                handleFileUri(cameraImageUri!!)
            }
        }
        requestCameraPermission = registerForActivityResult(
            ActivityResultContracts.RequestPermission()
        ) { isGranted ->
            if (isGranted) {
                openCamera_new()
            } else {
                Toast.makeText(context, "Camera permission denied", Toast.LENGTH_SHORT).show()
            }
        }
        galleryLauncher = registerForActivityResult(
            ActivityResultContracts.StartActivityForResult()
        ) { result ->
            if (result.resultCode == Activity.RESULT_OK && result.data != null && result.data!!.data != null) {
                val galleryUri = result.data!!.data!!
                setProfileImage(galleryUri)
                profile_upload(galleryUri)
            }
        }
    }

    override fun onViewCreated(v: View, savedInstanceState: Bundle?) {
        super.onViewCreated(v, savedInstanceState)
        try {
            et_Email = v.findViewById(R.id.et_Email)
            et_ContactPhone = v.findViewById(R.id.et_ContactPhone)
            val prefs = requireActivity().getSharedPreferences("MyPrefs", Context.MODE_PRIVATE)
            var loginMethod = prefs.getString("login_method", "") ?: ""

            if (loginMethod.isEmpty()) {
                loginMethod = Constants.LOGIN_METHOD ?: ""
            }

            if (loginMethod == "email") {
                AndroidUtils.ToggleButton(1, et_ContactPhone)
                AndroidUtils.ToggleButton(0, et_Email)
            } else if (loginMethod == "mobile") {
                AndroidUtils.ToggleButton(0, et_ContactPhone)
                AndroidUtils.ToggleButton(1, et_Email)
            } else {
                et_Email?.isEnabled = true
                et_ContactPhone?.isEnabled = true
            }

            mViewModel = ViewModelProvider(requireActivity()).get(NewModel::class.java)
            mViewModel?.setData(resources.getString(R.string.edit_profile))
            tv_gender = v.findViewById(R.id.tv_gender)
            tv_gender?.setText(R.string.gender)
            et_dob = v.findViewById(R.id.et_dob)
            tl_generate = v.findViewById(R.id.tl_generate)
            optional_txt = v.findViewById(R.id.optional_txt)
            optional_txt?.setText(R.string.optional_txt)
            tv_generate = tl_generate?.findViewById(R.id.tv_image_name)
            tv_generate?.setText(R.string.summarize_bio)
            tv_BasicProfile = v.findViewById(R.id.tv_BasicProfile)
            iv_cancel = v.findViewById(R.id.iv_cancel)
            iv_cancel_add = v.findViewById(R.id.iv_cancel_add)
            iv_profile = v.findViewById(R.id.iv_profile)
            person_icon = v.findViewById(R.id.person_icon)
            person_icon?.textSize = 37f
            iv_edit_circle = v.findViewById(R.id.iv_edit_circle)
            iv_delete_circle = v.findViewById(R.id.iv_delete_circle)
            genderlayout = v.findViewById(R.id.genderlayout)
            doblayout = v.findViewById(R.id.doblayout)
            nationalitylayout = v.findViewById(R.id.nationalitylayout)
            nationalitylayout?.visibility = View.GONE
            countrylayout = v.findViewById(R.id.countrylayout)
            countrylayout?.visibility = View.GONE
            firmlayout = v.findViewById(R.id.firmlayout)
            iv_edit_circle?.visibility = View.VISIBLE
            iv_delete_circle?.visibility = View.GONE
            ll_add_edit = v.findViewById(R.id.ll_add_edit)
            ll_edit_Bp = v.findViewById(R.id.ll_edit_Bp)

            iv_delete_circle?.setOnClickListener {
                AndroidUtils.showConfirmationDialog(requireContext(), "Confirmation", "Are you sure you want to remove the Profile Picture ?", object : AndroidUtils.OnConfirmListener {
                    override fun onSave() {
                        delete()
                    }
                    override fun onCancel() {}
                })
            }

            if (isProfileEdit && !isadditionalinfo) {
                ll_edit_Bp?.visibility = View.VISIBLE
                ll_add_edit?.visibility = View.GONE
            } else if (!isProfileEdit && isadditionalinfo) {
                ll_add_edit?.visibility = View.VISIBLE
                ll_edit_Bp?.visibility = View.GONE
            }

            tv_Firmname = v.findViewById(R.id.tv_Firmname)
            tv_nationality = v.findViewById(R.id.tv_nationality)
            tv_dob = v.findViewById(R.id.tv_dob)
            tv_bio = v.findViewById(R.id.tv_bio)
            tv_Country = v.findViewById(R.id.tv_Country)
            tv_Email = v.findViewById(R.id.tv_Email)
            tv_contactName = v.findViewById(R.id.tv_contactName)
            tv_ContactPhone = v.findViewById(R.id.tv_ContactPhone)
            tv_Website = v.findViewById(R.id.tv_Website)
            et_Firmname = v.findViewById(R.id.et_Firmname)
            et_contactName = v.findViewById(R.id.et_contactName)

            val til_contactName = v.findViewById<TextInputLayout>(R.id.til_contactName)
            if ("solo".equals(Constants.CATEGORY, ignoreCase = true)) {
                til_contactName?.isCounterEnabled = false
            } else {
                et_contactName?.filters = arrayOf()
                til_contactName?.isCounterEnabled = false
            }

            et_Website = v.findViewById(R.id.et_Website)
            et_bio = v.findViewById(R.id.et_bio)
            et_bio?.setText(R.string.summarize_bio_txt)
            et_bio?.addTextChangedListener(DescriptionValidation(et_bio))

            if ("solo".equals(Constants.CATEGORY, ignoreCase = true)) {
                firmlayout?.visibility = View.GONE
            } else {
                firmlayout?.visibility = View.VISIBLE
            }

            tv_RegisterAddress = v.findViewById(R.id.tv_RegisterAddress)
            tv_Building = v.findViewById(R.id.tv_Building)
            tv_City = v.findViewById(R.id.tv_City)
            tv_Street = v.findViewById(R.id.tv_Street)
            tv_State = v.findViewById(R.id.tv_State)
            tv_ad_Country = v.findViewById(R.id.tv_ad_Country)
            tv_Zip = v.findViewById(R.id.tv_Zip)
            tv_mailingAddressnNote = v.findViewById(R.id.tv_mailingAddressnNote)
            cbYes = v.findViewById(R.id.cb_yes)

            cbYes?.setOnCheckedChangeListener { _, isChecked ->
                if (isChecked) {
                    met_Building?.setText(et_Building?.text.toString())
                    met_Street?.setText(et_Street?.text.toString())
                    met_City?.setText(et_City?.text.toString())
                    met_State?.setText(et_State?.text.toString())
                    met_ad_Country?.setText(et_ad_Country?.text.toString())
                    met_Zip?.setText(et_Zip?.text.toString())
                    cbYes?.setTextColor(Color.WHITE)
                } else {
                    cbYes?.setTextColor(Color.BLACK)
                }
            }

            tv_MailingAddress = v.findViewById(R.id.tv_MailingAddress)
            mtv_Building = v.findViewById(R.id.mtv_Building)
            mtv_City = v.findViewById(R.id.mtv_City)
            mtv_Street = v.findViewById(R.id.mtv_Street)
            mtv_State = v.findViewById(R.id.mtv_State)
            mtv_ad_Country = v.findViewById(R.id.mtv_ad_Country)
            mtv_Zip = v.findViewById(R.id.mtv_Zip)

            et_Building = v.findViewById(R.id.et_Building)
            et_Street = v.findViewById(R.id.et_Street)
            et_City = v.findViewById(R.id.et_City)
            et_State = v.findViewById(R.id.et_State)
            et_Zip = v.findViewById(R.id.et_Zip)

            met_Building = v.findViewById(R.id.met_Building)
            met_Street = v.findViewById(R.id.met_Street)
            met_City = v.findViewById(R.id.met_City)
            met_State = v.findViewById(R.id.met_State)
            met_Zip = v.findViewById(R.id.met_Zip)

            sp_met_ad_country = v.findViewById(R.id.sp_met_ad_country)
            ll_met_ad_country = v.findViewById(R.id.ll_met_ad_country)
            met_ad_Country = ll_met_ad_country?.findViewById(R.id.tv_spinner_view)
            img_dropdown_icon_madCountry = ll_met_ad_country?.findViewById(R.id.img_dropdown_icon)
            img_clear_icon_madCountry = ll_met_ad_country?.findViewById(R.id.img_clear_icon)

            et_nationality = v.findViewById(R.id.et_nationality)
            et_nationality?.setText("Indian")
            et_nationality?.isEnabled = false

            sp_et_gender = v.findViewById(R.id.sp_et_gender)
            ll_et_gender = v.findViewById(R.id.ll_et_gender)
            met_ad_gender = ll_et_gender?.findViewById(R.id.tv_spinner_view)
            img_dropdown_icon_gender = ll_et_gender?.findViewById(R.id.img_dropdown_icon)
            img_clear_icon_gender = ll_et_gender?.findViewById(R.id.img_clear_icon)

            sp_et_DefaultCurrency = v.findViewById(R.id.sp_et_DefaultCurrency)
            ll_et_DefaultCurrency = v.findViewById(R.id.ll_et_DefaultCurrency)
            et_DefaultCurrency = ll_et_DefaultCurrency?.findViewById(R.id.tv_spinner_view)
            img_dropdown_icon_Currency = ll_et_DefaultCurrency?.findViewById(R.id.img_dropdown_icon)
            img_clear_icon_Currency = ll_et_DefaultCurrency?.findViewById(R.id.img_clear_icon)

            gender_list.clear()
            gender_list.add("Male")
            gender_list.add("Female")
            gender_list.add("Other")
            val genderAdapterLocal = CommonSpinnerAdapter(requireActivity(), gender_list)
            genderAdapter = genderAdapterLocal
            sp_et_gender?.adapter = genderAdapterLocal
            AndroidUtils.LoadList(sp_et_gender, requireContext(), gender_list.size, true)
            met_ad_gender?.hint = "Select Gender"

            ll_et_gender?.setOnClickListener {
                val isVisible = sp_et_gender?.visibility == View.VISIBLE
                AndroidUtils.DisplaySpinnerView(
                    sp_et_gender, met_ad_gender, gender,
                    img_dropdown_icon_gender, img_clear_icon_gender,
                    !isVisible, genderAdapter, "Search Gender"
                )
            }

            sp_et_gender?.setOnItemClickListener { _, _, position, _ ->
                gender = gender_list[position]
                AndroidUtils.DisplaySpinnerView(
                    sp_et_gender, met_ad_gender, gender,
                    img_dropdown_icon_gender, img_clear_icon_gender,
                    false, genderAdapter, "Search Gender"
                )
            }

            img_clear_icon_gender?.setOnClickListener {
                gender = ""
                AndroidUtils.DisplaySpinnerView(
                    sp_et_gender, met_ad_gender, "",
                    img_dropdown_icon_gender, img_clear_icon_gender,
                    false, genderAdapter, "Search Gender"
                )
            }

            currency_list = AndroidUtils.getCurrency_list()
            val currencyAdapterLocal = CommonSpinnerAdapter(requireActivity(), currency_list)
            currencyAdapter = currencyAdapterLocal
            sp_et_DefaultCurrency?.adapter = currencyAdapterLocal
            AndroidUtils.LoadList(sp_et_DefaultCurrency, requireContext(), currency_list.size, true)
            et_DefaultCurrency?.hint = "Search Currency"

            ll_et_DefaultCurrency?.setOnClickListener {
                val isVisible = sp_et_DefaultCurrency?.visibility == View.VISIBLE
                AndroidUtils.DisplaySpinnerView(
                    sp_et_DefaultCurrency, et_DefaultCurrency, default_currency,
                    img_dropdown_icon_Currency, img_clear_icon_Currency,
                    !isVisible, currencyAdapter, "Search Currency"
                )
            }

            sp_et_DefaultCurrency?.setOnItemClickListener { _, _, position, _ ->
                default_currency = currency_list[position]
                AndroidUtils.DisplaySpinnerView(
                    sp_et_DefaultCurrency, et_DefaultCurrency, default_currency,
                    img_dropdown_icon_Currency, img_clear_icon_Currency,
                    false, currencyAdapter, "Search Currency"
                )
            }

            img_clear_icon_Currency?.setOnClickListener {
                default_currency = ""
                AndroidUtils.DisplaySpinnerView(
                    sp_et_DefaultCurrency, et_DefaultCurrency, "",
                    img_dropdown_icon_Currency, img_clear_icon_Currency,
                    false, currencyAdapter, "Search Currency"
                )
            }

            sp_et_ad_country = v.findViewById(R.id.sp_et_ad_country)
            ll_et_ad_country = v.findViewById(R.id.ll_et_ad_country)
            et_ad_Country = ll_et_ad_country?.findViewById(R.id.tv_spinner_view)
            img_dropdown_icon_adCountry = ll_et_ad_country?.findViewById(R.id.img_dropdown_icon)
            img_clear_icon_adCountry = ll_et_ad_country?.findViewById(R.id.img_clear_icon)

            ll_et_country = v.findViewById(R.id.ll_et_country)
            sp_et_country = v.findViewById(R.id.sp_et_country)
            et_Country = ll_et_country?.findViewById(R.id.tv_spinner_view)
            img_dropdown_icon_Country = ll_et_country?.findViewById(R.id.img_dropdown_icon)
            img_dropdown_icon_Country?.visibility = View.GONE
            img_clear_icon_Country = ll_et_country?.findViewById(R.id.img_clear_icon)
            img_clear_icon_Country?.visibility = View.GONE

            ll_et_country?.isEnabled = false
            ll_et_country?.isClickable = false
            ll_et_country?.isFocusable = false
            img_dropdown_icon_Country?.isEnabled = false
            img_dropdown_icon_Country?.isClickable = false
            img_dropdown_icon_Country?.isFocusable = false
            img_clear_icon_Country?.isEnabled = false
            img_clear_icon_Country?.isClickable = false
            img_clear_icon_Country?.isFocusable = false
            sp_et_country?.isEnabled = false
            sp_et_country?.isClickable = false
            et_Country?.isEnabled = false
            et_Country?.isClickable = false
            et_Country?.isFocusable = false

            if ("entity".equals(Constants.CATEGORY, ignoreCase = true)) {
                doblayout?.visibility = View.GONE
                genderlayout?.visibility = View.GONE
            } else {
                doblayout?.visibility = View.VISIBLE
                genderlayout?.visibility = View.VISIBLE
            }

            img_dropdown_icon_Country?.visibility = View.GONE
            img_clear_icon_Country?.visibility = View.GONE

            btn_cancel_pp = v.findViewById(R.id.btn_cancel_pp)
            btn_save_pp = v.findViewById(R.id.btn_save_pp)
            btn_cancel_ai = v.findViewById(R.id.btn_cancel_ai)
            btn_save_ai = v.findViewById(R.id.btn_save_ai)

            tvErrorMzip = v.findViewById(R.id.tvErrorMzip)
            tvErrorEmail = v.findViewById(R.id.tvErrorEmail)
            tvErrorZip = v.findViewById(R.id.tvErrorZip)
            tvErrorPhone = v.findViewById(R.id.tvErrorPhone)

            et_ContactPhone?.addTextChangedListener(Validation(et_ContactPhone))
            et_ContactPhone?.inputType = InputType.TYPE_CLASS_NUMBER

            val zipFilters = arrayOf<InputFilter>(
                InputFilter.LengthFilter(6),
                InputFilter { source, start, end, _, _, _ ->
                    for (i in start until end) {
                        if (!Character.isDigit(source[i])) {
                            return@InputFilter ""
                        }
                    }
                    null
                }
            )
            et_Zip?.filters = zipFilters
            met_Zip?.filters = zipFilters

            et_Zip?.addTextChangedListener(Validation(et_Zip))
            met_Zip?.addTextChangedListener(Validation(met_Zip))
            et_ContactPhone?.let { AndroidUtils.NumberFilter(it, true) }

            et_Firmname?.addTextChangedListener(Validation(et_Firmname))
            et_Email?.addTextChangedListener(Validation(et_Email))
            et_contactName?.addTextChangedListener(Validation(et_contactName))
            et_ContactPhone?.addTextChangedListener(Validation(et_ContactPhone))
            et_Website?.addTextChangedListener(Validation(et_Website))
            et_Building?.addTextChangedListener(Validation(et_Building))
            et_City?.addTextChangedListener(Validation(et_City))
            et_Street?.addTextChangedListener(Validation(et_Street))
            et_State?.addTextChangedListener(Validation(et_State))
            et_Zip?.addTextChangedListener(Validation(et_Zip))
            met_Building?.addTextChangedListener(Validation(met_Building))
            met_City?.addTextChangedListener(Validation(met_City))
            met_Street?.addTextChangedListener(Validation(met_Street))
            met_State?.addTextChangedListener(Validation(met_State))
            met_Zip?.addTextChangedListener(Validation(met_Zip))

            tabLayout = v.findViewById(R.id.tab_layout)
            llViewScreen = v.findViewById(R.id.ll_viewscreen)
            tv_add_info = v.findViewById(R.id.tv_add_info)
            tv_add_info?.textSize = DynamicUtils.twenty.toFloat()
            tv_add_info?.setText(R.string.additional_info)

            val isFp = isFirmProfile()
            tabLayout?.clearOnTabSelectedListeners()
            tabLayout?.removeAllTabs()
            tabLayout?.addTab(tabLayout!!.newTab().setText("Practice Details"))

            if (isFp) {
                tabLayout?.addTab(tabLayout!!.newTab().setText("Awards & Recognition"))
            } else {
                val isAMMorSuperuser = Constants.ROLE.equals("AAM", ignoreCase = true)
                if (isAMMorSuperuser) {
                    tabLayout?.addTab(tabLayout!!.newTab().setText("Awards & Recognition"))
                } else {
                    tabLayout?.addTab(tabLayout!!.newTab().setText("Courts & Cases"))
                    tabLayout?.addTab(tabLayout!!.newTab().setText("Education & Awards"))
                    tabLayout?.addTab(tabLayout!!.newTab().setText("Set Availability"))
                }
            }

            val defaultTab = tabLayout?.getTabAt(0)
            defaultTab?.select()
            tabLayout?.clearOnTabSelectedListeners()
            tabLayout?.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
                override fun onTabSelected(tab: TabLayout.Tab) {
                    snapshotLiveChipState()
                    llViewScreen?.removeAllViews()
                    val isFirmProfileInner = isFirmProfile()
                    val isAMMorSuperuserInner = Constants.ROLE.equals("AAM", ignoreCase = true)

                    if (isFirmProfileInner || isAMMorSuperuserInner) {
                        when (tab.position) {
                            0 -> {
                                Constants.PROFILE_EDIT_TAB = "practice_details"
                                createPracticeDetails()
                            }
                            1 -> {
                                Constants.PROFILE_EDIT_TAB = "education_awards"
                                createAwardsOnlyEdit()
                            }
                        }
                    } else {
                        when (tab.position) {
                            0 -> {
                                Constants.PROFILE_EDIT_TAB = "practice_details"
                                createPracticeDetails()
                            }
                            1 -> {
                                Constants.PROFILE_EDIT_TAB = "courts_cases"
                                createCourtsAndCasesEdit()
                            }
                            2 -> {
                                Constants.PROFILE_EDIT_TAB = "education_awards"
                                createEducationsAwardsEdit()
                            }
                            3 -> {
                                Constants.PROFILE_EDIT_TAB = "availability"
                                createSetAvailability()
                            }
                        }
                    }
                }
                override fun onTabUnselected(tab: TabLayout.Tab) {}
                override fun onTabReselected(tab: TabLayout.Tab) {}
            })

            createPracticeDetails()
            when (Constants.PROFILE_EDIT_TAB) {
                "practice_details" -> createPracticeDetails()
                "education_awards" -> {
                    if (isFirmProfile() || Constants.ROLE.equals("AAM", ignoreCase = true)) {
                        createAwardsOnlyEdit()
                    } else {
                        createEducationsAwardsEdit()
                    }
                }
                "availability" -> {
                    if (!isFirmProfile() && !Constants.ROLE.equals("AAM", ignoreCase = true)) {
                        createSetAvailability()
                    }
                }
                "courts_cases" -> {
                    if (!isFirmProfile() && !Constants.ROLE.equals("AAM", ignoreCase = true)) {
                        createCourtsAndCasesEdit()
                    }
                }
            }

            et_Email?.addTextChangedListener(object : TextWatcher {
                override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
                    tvErrorEmail?.visibility = View.GONE
                    emailhaserror = false
                }
                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
                override fun afterTextChanged(s: Editable?) {
                    val text = et_Email?.text.toString().trim()
                    if (text.isNotEmpty()) {
                        if (!AndroidUtils.isValidEmail(text)) {
                            tvErrorEmail?.visibility = View.VISIBLE
                            tvErrorEmail?.setText(R.string.enter_a_valid_email_address)
                            emailhaserror = true
                        } else {
                            tvErrorEmail?.visibility = View.GONE
                            emailhaserror = false
                        }
                    }
                }
            })

            et_ContactPhone?.addTextChangedListener(object : TextWatcher {
                override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
                    tvErrorPhone?.visibility = View.GONE
                    phonehaserror = false
                }
                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
                override fun afterTextChanged(s: Editable?) {
                    val text = s?.toString() ?: ""
                    if (text.isNotEmpty()) {
                        if (text.length < 10) {
                            tvErrorPhone?.visibility = View.VISIBLE
                            tvErrorPhone?.setText(R.string.please_enter_the_10_digit_mobile_number)
                            phonehaserror = true
                        }
                    } else {
                        tvErrorPhone?.visibility = View.GONE
                        phonehaserror = false
                    }
                }
            })

            et_Zip?.addTextChangedListener(object : TextWatcher {
                override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
                    tvErrorZip?.visibility = View.GONE
                    ziphaserror = false
                }
                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
                override fun afterTextChanged(s: Editable?) {
                    val text = s?.toString() ?: ""
                    if (text.isNotEmpty()) {
                        if (text.length != 6) {
                            tvErrorZip?.visibility = View.VISIBLE
                            tvErrorZip?.text = "Invalid ZIP code. Must be exactly 6 digits."
                            ziphaserror = true
                        } else {
                            tvErrorZip?.visibility = View.GONE
                            ziphaserror = false
                        }
                    } else {
                        tvErrorZip?.visibility = View.GONE
                        ziphaserror = false
                    }
                }
            })

            met_Zip?.addTextChangedListener(object : TextWatcher {
                override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
                    tvErrorMzip?.visibility = View.GONE
                    mziphaserror = false
                }
                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
                override fun afterTextChanged(s: Editable?) {
                    val text = s?.toString() ?: ""
                    if (text.isNotEmpty()) {
                        if (text.length != 6) {
                            tvErrorMzip?.visibility = View.VISIBLE
                            tvErrorMzip?.text = "Invalid ZIP code. Must be exactly 6 digits."
                            mziphaserror = true
                        } else {
                            tvErrorMzip?.visibility = View.GONE
                            mziphaserror = false
                        }
                    } else {
                        tvErrorMzip?.visibility = View.GONE
                        mziphaserror = false
                    }
                }
            })

            btn_save_pp?.setOnClickListener {
                if (firmProfileModel != null) {
                    SaveDetailsBp()
                }
            }

            btn_save_ai?.setOnClickListener {
                if (firmProfileModel != null) {
                    SaveDetailsAdditionalinfo()
                }
            }

            btn_cancel_pp?.setOnClickListener {
                clearDetails()
            }

            btn_cancel_ai?.setOnClickListener {
                clearDetails()
            }

            iv_cancel?.setOnClickListener {
                clearDetails()
                val fp = isFirmProfile()
                if (fp) {
                    mViewModel?.setData("Firm Profile")
                } else {
                    mViewModel?.setData(resources.getString(R.string.my_profile))
                }
            }

            iv_cancel_add?.setOnClickListener {
                clearDetails()
            }

            sp_et_country?.setOnItemClickListener { _, _, position, _ ->
                country_name1 = countriesList[position].name ?: ""
                AndroidUtils.DisplaySpinnerView(sp_et_country, et_Country, country_name1, img_dropdown_icon_Country, img_clear_icon_Country, false, adCountryAdapter, "Search Country")
                iscountry_checked1 = true
            }

            sp_et_ad_country?.setOnItemClickListener { _, _, position, _ ->
                country_name2 = countriesList[position].name ?: ""
                AndroidUtils.DisplaySpinnerView(sp_et_ad_country, et_ad_Country, country_name2, img_dropdown_icon_adCountry, img_clear_icon_adCountry, false, adCountryAdapter, "Search Country")
                iscountry_checked2 = true
            }

            sp_met_ad_country?.setOnItemClickListener { _, _, position, _ ->
                country_name3 = countriesList[position].name ?: ""
                AndroidUtils.DisplaySpinnerView(sp_met_ad_country, met_ad_Country, country_name3, img_dropdown_icon_madCountry, img_clear_icon_madCountry, false, madCountryAdapter, "Search Country")
                iscountry_checked3 = true
            }

            loadData()

        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun handleFileUri(uri: Uri) {
        setProfileImage(uri)
        profile_upload(uri)
    }

    private fun snapshotLiveChipState() {
        if (chipPractice != null) {
            liveChipPracticeAreas = getChipValuesFromGroup(chipPractice!!)
        }
        if (chipLanguages != null) {
            liveChipLanguages = getChipValuesFromGroup(chipLanguages!!)
        }
        if (chipServices != null) {
            liveChipServices = getChipValuesFromGroup(chipServices!!)
        }
    }

    private fun setupOnBackPressed() {
        requireActivity().onBackPressedDispatcher.addCallback(
            this,
            object : OnBackPressedCallback(true) {
                override fun handleOnBackPressed() {
                    if (isEnabled) {
                        isEnabled = false
                        requireActivity().onBackPressed()
                    }
                }
            })
    }

    private fun openCamera_new() {
        try {
            val values = ContentValues()
            values.put(MediaStore.Images.Media.TITLE, "FirmProfile_" + System.currentTimeMillis())
            values.put(MediaStore.Images.Media.DESCRIPTION, "Profile Image")
            cameraImageUri = requireActivity().contentResolver.insert(
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values
            )
            if (cameraImageUri == null) {
                Toast.makeText(context, "Cannot access camera storage", Toast.LENGTH_SHORT).show()
                return
            }
            val uri = cameraImageUri
            if (uri != null) {
                cameraLauncher?.launch(uri)
            }
            profile()
        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(context, "Camera error: " + e.message, Toast.LENGTH_LONG).show()
        }
    }

    private fun setProfileImage(uri: Uri) {
        if (iv_profile != null) {
            Glide.with(this)
                .load(uri)
                .circleCrop()
                .placeholder(R.drawable.ic_profile_placeholder)
                .error(R.drawable.ic_profile_placeholder)
                .into(iv_profile!!)
        }
    }

    private fun getFileFromUri(uri: Uri): File {
        val inputStream = requireActivity().contentResolver.openInputStream(uri)
            ?: throw Exception("Unable to open InputStream for URI: $uri")
        val file = File(requireActivity().cacheDir, "profile_${System.currentTimeMillis()}.jpg")
        val outputStream = FileOutputStream(file)
        val buffer = byteReplicate(4096)
        var read: Int
        var totalBytes = 0L
        while (inputStream.read(buffer).also { read = it } != -1) {
            outputStream.write(buffer, 0, read)
            totalBytes += read
        }
        outputStream.flush()
        outputStream.close()
        inputStream.close()
        if (totalBytes == 0L) {
            throw Exception("File created but empty (0 bytes)")
        }
        return file
    }

    private fun byteReplicate(size: Int): ByteArray {
        return ByteArray(size)
    }

    private fun profile_upload(imageUri: Uri) {
        Constants.firm_image = imageUri.toString()
        if (iv_profile != null) {
            AndroidUtils.loadProfileImage(requireContext(), Constants.firm_image, iv_profile, person_icon)
        }
        progress_dialog = AndroidUtils.get_progress(activity)
        try {
            val imageFile = getFileFromUri(imageUri)
            val jsonObject = JSONObject()
            jsonObject.put("type", "profile_pic")
            WebServiceHelper.callHttpUploadWebService(
                this,
                requireContext(),
                WebServiceHelper.RestMethodType.POST,
                "v3/profile/pic/upload",
                "Profile_upload",
                imageFile,
                jsonObject.toString()
            )
        } catch (e: Exception) {
            AndroidUtils.dismiss_dialog(progress_dialog)
        }
    }

    private fun profile() {
        try {
            WebServiceHelper.callHttpWebService(
                this,
                requireContext(),
                WebServiceHelper.RestMethodType.GET,
                "v3/profile/pic",
                "Profile",
                JSONObject().toString()
            )
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun delete() {
        progress_dialog = AndroidUtils.get_progress(requireActivity())
        try {
            WebServiceHelper.callHttpWebService(
                this,
                requireContext(),
                WebServiceHelper.RestMethodType.DELETE,
                "v3/profile/pic",
                "Profile_delete",
                JSONObject().toString()
            )
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun loadData() {
        firmProfileModel = Constants.firmProfileModel
        setData()
        callCountriesWebService()
    }

    private fun callCountriesWebService() {
        try {
            WebServiceHelper.callHttpWebService(
                this,
                requireContext(),
                WebServiceHelper.RestMethodType.GET,
                "countries",
                "COUNTRIES",
                JSONObject().toString()
            )
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun callCaseType() {
        progress_dialog = AndroidUtils.get_progress(requireActivity())
        try {
            WebServiceHelper.callHttpWebService(
                this,
                requireContext(),
                WebServiceHelper.RestMethodType.GET,
                "v2/matter/casetypes",
                "Case Type",
                JSONObject().toString()
            )
        } catch (e: Exception) {
            AndroidUtils.dismiss_dialog(progress_dialog)
            e.printStackTrace()
        }
    }

    private fun callHighCourts() {
        progress_dialog = AndroidUtils.get_progress(requireActivity())
        try {
            WebServiceHelper.callHttpWebService(
                this,
                requireContext(),
                WebServiceHelper.RestMethodType.GET,
                "v3/court/high",
                "HIGH_COURTS",
                JSONObject().toString()
            )
        } catch (e: Exception) {
            AndroidUtils.dismiss_dialog(progress_dialog)
            e.printStackTrace()
        }
    }

    private fun callSupremeCourt() {
        progress_dialog = AndroidUtils.get_progress(requireActivity())
        try {
            WebServiceHelper.callHttpWebService(
                this,
                requireContext(),
                WebServiceHelper.RestMethodType.GET,
                "v3/court/supreme",
                "SUPREME_COURT",
                JSONObject().toString()
            )
        } catch (e: Exception) {
            AndroidUtils.dismiss_dialog(progress_dialog)
            e.printStackTrace()
        }
    }

    private fun callCourtStatesApi(onComplete: Runnable?) {
        if (!courtStateList.isEmpty()) {
            if (onComplete != null && isAdded) requireActivity().runOnUiThread(onComplete)
            return
        }
        progress_dialog = AndroidUtils.get_progress(requireActivity())
        try {
            pendingCourtStatesCallback = onComplete
            WebServiceHelper.callHttpWebService(
                this,
                requireContext(),
                WebServiceHelper.RestMethodType.GET,
                "v3/court/states",
                "COURT_STATES",
                JSONObject().toString()
            )
        } catch (e: Exception) {
            AndroidUtils.dismiss_dialog(progress_dialog)
            e.printStackTrace()
        }
    }

    private fun callCourtSuggestApi(practiceAreas: List<String>, onComplete: Runnable?) {
        progress_dialog = AndroidUtils.get_progress(requireActivity())
        try {
            pendingCourtSuggestCallback = onComplete
            val body = JSONObject()
            val pa = JSONArray()
            for (p in practiceAreas) pa.put(p)
            body.put("practice_areas", pa)

            WebServiceHelper.callHttpWebService(
                this,
                requireContext(),
                WebServiceHelper.RestMethodType.POST,
                "v3/court/suggest",
                "COURT_SUGGEST",
                body.toString()
            )
        } catch (e: Exception) {
            AndroidUtils.dismiss_dialog(progress_dialog)
            e.printStackTrace()
        }
    }

    private fun fetchSuggestedServices(practiceAreas: List<String>) {
        if (practiceAreas.isEmpty()) {
            allServicesList.clear()
            suggestedServicesList.clear()
        }
        try {
            val body = JSONObject()
            val pa = JSONArray()
            for (p in practiceAreas) pa.put(p)
            body.put("practice_areas", pa)

            WebServiceHelper.callHttpWebService(
                this,
                requireContext(),
                WebServiceHelper.RestMethodType.POST,
                "v3/services/suggest",
                "SERVICES_SUGGEST",
                body.toString()
            )
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun fetchSearchedServices(query: String?, onComplete: Runnable?) {
        if (query.isNullOrEmpty()) {
            searchedServicesList.clear()
            searchedServicesList.addAll(suggestedServicesList)
            onComplete?.run()
            return
        }
        try {
            WebServiceHelper.callHttpWebService(
                this,
                requireContext(),
                WebServiceHelper.RestMethodType.GET,
                "v3/services/search?query=" + Uri.encode(query.trim()),
                "SERVICES_SEARCH",
                JSONObject().toString()
            )
            searchedServicesList.clear()
        } catch (e: Exception) {
            e.printStackTrace()
            searchedServicesList.clear()
            onComplete?.run()
        }
    }

    private fun setData() {
        if (firmProfileModel == null || firmProfileModel?.data == null || firmProfileModel?.data?.profile == null) {
            return
        }
        val profile = firmProfileModel!!.data!!.profile!!
        val firm = profile.firm

        val fp = isFirmProfile()

        if (fp && firm != null) {
            et_Firmname?.setText(firm.fullname)
            et_Email?.setText(firm.email)
            et_contactName?.setText(firm.contact_person)
            et_ContactPhone?.setText(firm.contact_phone)
            et_Website?.setText(firm.website)
            et_DefaultCurrency?.setText(firm.billing_currency)
            default_currency = firm.billing_currency ?: ""
            profileUrl = firm.profile_pic_url ?: ""
        } else {
            et_Firmname?.setText(profile.name)
            et_Email?.setText(profile.email)
            et_contactName?.setText(profile.name)
            et_ContactPhone?.setText(profile.mobile)
            et_Website?.setText("")
            et_DefaultCurrency?.setText(profile.firm?.billing_currency ?: "")
            default_currency = profile.firm?.billing_currency ?: ""
            profileUrl = profile.profile_pic_url ?: ""
        }

        if (profileUrl.isNotEmpty()) {
            Constants.firm_image = profileUrl
            iv_profile?.let { AndroidUtils.loadProfileImage(requireContext(), profileUrl, it, person_icon) }
            iv_delete_circle?.visibility = View.VISIBLE
        } else {
            iv_delete_circle?.visibility = View.GONE
            person_icon?.visibility = View.VISIBLE
            val initial = if (fp && firm != null && !TextUtils.isEmpty(firm.fullname)) {
                firm.fullname!!.substring(0, 1).uppercase()
            } else if (!TextUtils.isEmpty(profile.name)) {
                profile.name!!.substring(0, 1).uppercase()
            } else {
                "?"
            }
            person_icon?.text = initial
        }

        val currencyPrefs = requireActivity().getSharedPreferences("BillingPrefs", Context.MODE_PRIVATE)
        default_currency = currencyPrefs.getString("currency", "") ?: ""
        if (default_currency.isEmpty()) {
            default_currency = if (fp && firm != null) firm.billing_currency ?: "INR" else profile.firm?.billing_currency ?: "INR"
        }
        et_DefaultCurrency?.text = default_currency
    }

    private fun loadCountryData() {
        adCountryAdapter = CommonSpinnerAdapter(activity, countriesList)
        madCountryAdapter = CommonSpinnerAdapter(activity, countriesList)
        val countryAdapter = CommonSpinnerAdapter(activity, countriesList)

        sp_et_country?.adapter = countryAdapter

        AndroidUtils.DisplaySpinnerView(
            sp_et_ad_country, et_ad_Country, country_name1,
            img_dropdown_icon_adCountry, img_clear_icon_adCountry, false, adCountryAdapter, "Search Country"
        )

        AndroidUtils.DisplaySpinnerView(
            sp_met_ad_country, met_ad_Country, country_name2,
            img_dropdown_icon_madCountry, img_clear_icon_madCountry, false, madCountryAdapter, "Search Country"
        )
    }

    private fun getChipValuesFromGroup(group: ChipGroup): JSONArray {
        val array = JSONArray()
        for (i in 0 until group.childCount) {
            val chip = group.getChildAt(i) as Chip
            array.put(chip.text.toString())
        }
        return array
    }

    private fun getChipValues(chipGroup: ChipGroup): JSONArray {
        return getChipValuesFromGroup(chipGroup)
    }

    private fun buildProfilePatchPayload(): JSONObject {
        val obj = JSONObject()
        val fp = isFirmProfile()

        if (fp) {
            obj.put("fullname", et_Firmname?.text.toString().trim())
            obj.put("email", et_Email?.text.toString().trim())
            obj.put("contact_phone", et_ContactPhone?.text.toString().trim())
            obj.put("contact_person", et_contactName?.text.toString().trim())
            obj.put("website", et_Website?.text.toString().trim())
            obj.put("billing_currency", et_DefaultCurrency?.text.toString().trim())
        } else {
            obj.put("name", et_contactName?.text.toString().trim())
            obj.put("email", et_Email?.text.toString().trim())
            obj.put("mobile", et_ContactPhone?.text.toString().trim())
        }
        return obj
    }

    private fun buildAddress(
        building: TextInputEditText?,
        street: TextInputEditText?,
        city: TextInputEditText?,
        state: TextInputEditText?,
        country: TextView?,
        zip: TextInputEditText?
    ): JSONObject {
        val obj = JSONObject()
        obj.put("house_flat_no", building?.text.toString().trim())
        obj.put("street", street?.text.toString().trim())
        obj.put("city_town", city?.text.toString().trim())
        obj.put("state", state?.text.toString().trim())
        obj.put("country", "India")
        obj.put("zipcode", zip?.text.toString().trim())
        return obj
    }

    private fun buildadditional(): JSONObject {
        val obj = JSONObject()
        val fp = isFirmProfile()

        val experienceStr = etExperience?.text.toString().trim()
        val expVal = if (experienceStr.isEmpty()) 0 else experienceStr.toInt()

        if (fp) {
            obj.put("years_of_incorporation", expVal)
            val practiceSource = if (chipPractice != null) getChipValues(chipPractice!!) else JSONArray()
            obj.put("practice_areas", practiceSource)
            val servicesSource = if (chipServices != null) getChipValues(chipServices!!) else JSONArray()
            obj.put("services_offered", servicesSource)
            obj.put("address", buildAddress(et_Building, et_Street, et_City, et_State, et_ad_Country, et_Zip))
            obj.put("correspondence_address", buildAddress(met_Building, met_Street, met_City, met_State, met_ad_Country, met_Zip))
        } else {
            obj.put("years_of_experience", expVal)
            val practiceSource = if (chipPractice != null) getChipValues(chipPractice!!) else JSONArray()
            obj.put("practice_areas", practiceSource)
            val servicesSource = if (chipServices != null) getChipValues(chipServices!!) else JSONArray()
            obj.put("services_offered", servicesSource)
            val langSource = if (chipLanguages != null) getChipValues(chipLanguages!!) else JSONArray()
            obj.put("languages_spoken", langSource)
            obj.put("education", getEducationJson(llEducation))
            obj.put("certifications", getCertificationJson(llCertification))
            obj.put("awards", getAwardsJson(llAwards))
            obj.put("court_enrollments", getCourtEnrollmentsJson())
            obj.put("cases_handled", getCasesHandledJson())
            obj.put("address", buildAddress(et_Building, et_Street, et_City, et_State, et_ad_Country, et_Zip))
            obj.put("correspondence_address", buildAddress(met_Building, met_Street, met_City, met_State, met_ad_Country, met_Zip))
        }
        return obj
    }

    private fun getCourtEnrollmentsJson(): JSONArray {
        val array = JSONArray()
        if (llCourtEnrollments != null) {
            for (i in 0 until llCourtEnrollments!!.childCount) {
                val row = llCourtEnrollments!!.getChildAt(i)
                val llCt = row.findViewById<LinearLayout>(R.id.ll_court_type)
                val tvCt = llCt?.findViewById<TextView>(R.id.tv_spinner_view)
                val courtType = tvCt?.text.toString().trim()

                val llState = row.findViewById<LinearLayout>(R.id.ll_state_spinner)
                val tvState = llState?.findViewById<TextView>(R.id.tv_spinner_view)
                val state = tvState?.text.toString().trim()

                val etCity = row.findViewById<TextInputEditText>(R.id.et_city_search)
                val city = etCity?.text.toString().trim()

                if (courtType.isNotEmpty()) {
                    val obj = JSONObject()
                    obj.put("court_type", convertCourtTypeToApiFormat(courtType))
                    obj.put("state", state)
                    obj.put("city", city)
                    obj.put("court_name", "") // not mandatory in payload
                    array.put(obj)
                }
            }
        }
        return array
    }

    private fun getCasesHandledJson(): JSONArray {
        val array = JSONArray()
        if (chipCasesHandled != null) {
            for (i in 0 until chipCasesHandled!!.childCount) {
                val chip = chipCasesHandled!!.getChildAt(i) as Chip
                array.put(chip.text.toString())
            }
        }
        return array
    }

    private fun getEducationJson(ll: LinearLayout?): JSONArray {
        val array = JSONArray()
        if (ll != null) {
            for (i in 0 until ll.childCount) {
                val view = ll.getChildAt(i)
                val etDegree = view.findViewById<EditText>(R.id.et_title)
                val etUniversity = view.findViewById<EditText>(R.id.et_content)
                val etYear = view.findViewById<EditText>(R.id.et_year)
                val degree = etDegree.text.toString().trim()
                val university = etUniversity.text.toString().trim()
                val yearText = etYear.text.toString().trim()
                if (degree.isEmpty() && university.isEmpty() && yearText.isEmpty()) {
                    continue
                }
                val obj = JSONObject()
                obj.put("degree", degree)
                obj.put("university", university)
                if (yearText.isEmpty()) {
                    obj.put("passing_year", JSONObject.NULL)
                } else {
                    obj.put("passing_year", yearText.toInt())
                }
                array.put(obj)
            }
        }
        return array
    }

    private fun getCertificationJson(ll: LinearLayout?): JSONArray {
        val array = JSONArray()
        if (ll != null) {
            for (i in 0 until ll.childCount) {
                val view = ll.getChildAt(i)
                val etName = view.findViewById<EditText>(R.id.et_title)
                val etAuthority = view.findViewById<EditText>(R.id.et_content)
                val etYear = view.findViewById<EditText>(R.id.et_year)
                val name = etName.text.toString().trim()
                val authority = etAuthority.text.toString().trim()
                val yearText = etYear.text.toString().trim()
                if (name.isEmpty() && authority.isEmpty() && yearText.isEmpty()) {
                    continue
                }
                val obj = JSONObject()
                obj.put("certification_name", name)
                obj.put("issuing_authority", authority)
                if (yearText.isEmpty()) {
                    obj.put("year_of_issue", JSONObject.NULL)
                } else {
                    obj.put("year_of_issue", yearText.toInt())
                }
                array.put(obj)
            }
        }
        return array
    }

    private fun getAwardsJson(ll: LinearLayout?): JSONArray {
        val array = JSONArray()
        if (ll != null) {
            for (i in 0 until ll.childCount) {
                val view = ll.getChildAt(i)
                val etName = view.findViewById<EditText>(R.id.et_title)
                val etYear = view.findViewById<EditText>(R.id.et_year)
                val etPurpose = view.findViewById<EditText>(R.id.et_content)
                val name = etName.text.toString().trim()
                val yearStr = etYear.text.toString().trim()
                val purpose = etPurpose.text.toString().trim()
                if (name.isEmpty() && yearStr.isEmpty() && purpose.isEmpty()) {
                    continue
                }
                val obj = JSONObject()
                obj.put("award_name", name)
                if (yearStr.isEmpty()) {
                    obj.put("year_of_award", JSONObject.NULL)
                } else {
                    obj.put("year_of_award", yearStr.toInt())
                }
                obj.put("purpose", purpose)
                array.put(obj)
            }
        }
        return array
    }

    private fun updateProfile() {
        try {
            val payload = buildProfilePatchPayload()
            WebServiceHelper.callHttpWebService(
                this,
                requireContext(),
                WebServiceHelper.RestMethodType.PATCH,
                "v3/profile",
                "Update_Bp",
                payload.toString()
            )
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun update_add_Profile() {
        try {
            val payload = buildadditional()
            WebServiceHelper.callHttpWebService(
                this,
                requireContext(),
                WebServiceHelper.RestMethodType.PATCH,
                "v3/profile",
                "Update_Bp",
                payload.toString()
            )
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun SaveDetailsBp() {
        try {
            val isFirmNameEmpty = et_Firmname?.text.toString().trim().isEmpty()
            val isContactPhoneEmpty = et_ContactPhone?.text.toString().trim().isEmpty()
            val isDefaultCurrencyEmpty = et_DefaultCurrency?.text.toString().trim().isEmpty()
            val isEmailEmpty = et_Email?.text.toString().trim().isEmpty()

            val isSolo = "solo".equals(Constants.CATEGORY, ignoreCase = true)

            if ((isSolo && isFirmNameEmpty)
                || isContactPhoneEmpty
                || isDefaultCurrencyEmpty
                || isEmailEmpty
                || emailhaserror
                || phonehaserror
            ) {
                var msg = "Please enter the"
                if (isSolo && isFirmNameEmpty) {
                    msg = append(msg, "First Name")
                }
                if (isContactPhoneEmpty) {
                    msg = append(msg, "Phone Number")
                } else if (phonehaserror) {
                    msg = append(msg, "valid Phone Number")
                }
                if (isDefaultCurrencyEmpty) {
                    msg = append(msg, "Default Currency")
                }
                if (isEmailEmpty) {
                    msg = append(msg, "Email")
                } else if (emailhaserror) {
                    msg = append(msg, "valid Email")
                }

                AndroidUtils.showAlert(msg, activity)
                return
            }

            updateProfile()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun append(msg: String, value: String): String {
        return if (msg == "Please enter the") {
            "$msg $value"
        } else {
            "$msg, $value"
        }
    }

    private fun isEmpty(et: EditText?): Boolean {
        return et?.text == null || et.text.toString().trim().isEmpty()
    }

    private fun appendMsg(msg: StringBuilder, field: String) {
        if (msg.length > "Please enter the ".length) {
            msg.append(", ")
        }
        msg.append(field)
    }

    private fun validateRegisteredAddress(msg: StringBuilder): Boolean {
        var hasError = false
        if (isEmpty(et_Building)) {
            appendMsg(msg, "Building")
            hasError = true
        }
        if (isEmpty(et_City)) {
            appendMsg(msg, "City")
            hasError = true
        }
        if (isEmpty(et_State)) {
            appendMsg(msg, "State")
            hasError = true
        }
        if (isEmpty(et_Zip)) {
            appendMsg(msg, "ZIP Code")
            hasError = true
        } else if (et_Zip?.text.toString().trim().length < 6) {
            appendMsg(msg, "valid 6-digit ZIP Code")
            ziphaserror = true
            hasError = true
        }
        return hasError
    }

    private fun validateMailingAddress(msg: StringBuilder): Boolean {
        var hasError = false
        if (isEmpty(met_Building)) {
            appendMsg(msg, "Mailing Building")
            hasError = true
        }
        if (isEmpty(met_City)) {
            appendMsg(msg, "Mailing City")
            hasError = true
        }
        if (isEmpty(met_State)) {
            appendMsg(msg, "Mailing State")
            hasError = true
        }
        if (isEmpty(met_Zip)) {
            appendMsg(msg, "Mailing ZIP Code")
            hasError = true
        } else if (met_Zip?.text.toString().trim().length < 6) {
            appendMsg(msg, "valid 6-digit Mailing ZIP Code")
            mziphaserror = true
            hasError = true
        }
        return hasError
    }

    private fun SaveDetailsAdditionalinfo() {
        try {
            createPendingChips()
            val pos = tabLayout?.selectedTabPosition ?: 0
            val tabs = arrayOf("practice_details", "courts_cases", "education_awards", "availability")
            Constants.PROFILE_EDIT_TAB = if (pos in tabs.indices) tabs[pos] else "practice_details"

            val isFirmProfileMode = isFirmProfile()
            if (!isFirmProfileMode) {
                val practiceValues = if (chipPractice != null) getChipValuesFromGroup(chipPractice!!) else JSONArray()
                if (practiceValues.length() == 0) {
                    AndroidUtils.showAlert("Please check the Practice Area.", activity)
                    return
                }
            }

            if (llCourtEnrollments != null && llCourtEnrollments!!.childCount > 0) {
                if (validateCourtEnrollments()) {
                    AndroidUtils.showAlert("Please check the court details.", activity)
                    return
                }
            }

            val msg = StringBuilder("Please enter the ")
            var hasError = false
            hasError = hasError or validateRegisteredAddress(msg)
            hasError = hasError or validateMailingAddress(msg)
            if (hasError) {
                AndroidUtils.showAlert(msg.toString(), activity)
                return
            }

            update_add_Profile()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun createPendingChips() {
        // Implement if any pending chips need processing
    }

    private fun validateCourtEnrollments(): Boolean {
        var hasError = false
        if (llCourtEnrollments == null) return false

        for (i in 0 until llCourtEnrollments!!.childCount) {
            val row = llCourtEnrollments!!.getChildAt(i)
            setCourtError(row, R.id.tvCourtTypeError, null)
            setCourtError(row, R.id.tvStateNameError, null)
            setCourtError(row, R.id.tvCourtCityError, null)
            setCourtError(row, R.id.tvCourtNameError, null)

            val llCt = row.findViewById<LinearLayout>(R.id.ll_court_type)
            val tvCt = llCt?.findViewById<TextView>(R.id.tv_spinner_view)
            val courtType = tvCt?.text.toString().trim()
            if (TextUtils.isEmpty(courtType)) {
                setCourtError(row, R.id.tvCourtTypeError, "Required")
                hasError = true
                continue
            }

            val isSupreme = courtType.equals("Supreme Court", ignoreCase = true)
            val llStateSpin = row.findViewById<LinearLayout>(R.id.ll_state_spinner)
            val tvState = llStateSpin?.findViewById<TextView>(R.id.tv_spinner_view)
            val stateVal = tvState?.text.toString().trim()
            if (!isSupreme && TextUtils.isEmpty(stateVal)) {
                setCourtError(row, R.id.tvStateNameError, "Required")
                hasError = true
            }

            val etCitySearch = row.findViewById<TextInputEditText>(R.id.et_city_search)
            val cityVal = etCitySearch?.text.toString().trim()
            if (!isSupreme && TextUtils.isEmpty(cityVal)) {
                setCourtError(row, R.id.tvCourtCityError, "Required")
                hasError = true
            }
        }
        return hasError
    }

    private fun setCourtError(row: View, errorViewId: Int, message: String?) {
        val errorTv = row.findViewById<TextView>(errorViewId) ?: return
        if (TextUtils.isEmpty(message)) {
            errorTv.visibility = View.GONE
            errorTv.text = ""
        } else {
            errorTv.text = message
            errorTv.setTextColor(ContextCompat.getColor(requireContext(), R.color.Red))
            errorTv.visibility = View.VISIBLE
        }
    }

    private fun updateCachedUserData(name: String, firmName: String) {
        val prefs = requireActivity().getSharedPreferences("MyPrefs", Context.MODE_PRIVATE)
        prefs.edit().apply {
            putString("name", name)
            putString("firm_name", firmName)
            apply()
        }
    }

    override fun onAsyncTaskComplete(httpResult: HttpResultDo) {
        if (progress_dialog != null && progress_dialog!!.isShowing) {
            AndroidUtils.dismiss_dialog(progress_dialog)
        }

        if (httpResult.result == WebServiceHelper.ServiceCallStatus.Success) {
            try {
                val result = JSONObject(httpResult.responseContent)
                Log.d("Request_Type", httpResult.requestType ?: "")

                if (httpResult.requestType == "Update_Bp") {
                    val iserror = result.optBoolean("error")
                    if (!iserror) {
                        val data = result.optJSONObject("data")
                        val msg = data?.optString("msg", "") ?: result.optString("msg", "")
                        AndroidUtils.showAlert_docs("Success !", msg, activity)

                        if (et_contactName != null && et_contactName?.text != null) {
                            val confirmedName = et_contactName?.text.toString().trim()
                            if (!TextUtils.isEmpty(confirmedName)) {
                                Constants.NAME = confirmedName
                                Constants.ContactName = confirmedName
                            }
                        }

                        if (et_Firmname != null && et_Firmname?.text != null) {
                            val confirmedFirmName = et_Firmname?.text.toString().trim()
                            if (!TextUtils.isEmpty(confirmedFirmName)) {
                                Constants.FIRM_NAME = confirmedFirmName
                            }
                        }

                        if (person_icon != null) {
                            val initial = if (!TextUtils.isEmpty(Constants.NAME)) {
                                Constants.NAME!!.substring(0, 1).uppercase()
                            } else if (!TextUtils.isEmpty(Constants.FIRM_NAME)) {
                                Constants.FIRM_NAME!!.substring(0, 1).uppercase()
                            } else {
                                "?"
                            }
                            person_icon?.text = initial
                        }

                        updateCachedUserData(Constants.NAME ?: "", Constants.FIRM_NAME ?: "")

                        if (activity is MainActivity) {
                            (activity as MainActivity).updateProfileInitial()
                        }

                        clearDetails()
                    } else {
                        if (result.has("errors")) {
                            val errors = result.optJSONArray("errors")
                            if (errors != null && errors.length() > 0) {
                                val err = errors.optJSONObject(0)
                                val field = err?.optString("field", "") ?: ""
                                val errMsg = err?.optString("msg", "") ?: ""
                                AndroidUtils.showAlert("$field : $errMsg", activity)
                            }
                        } else {
                            AndroidUtils.showAlert(result.optString("msg", ""), activity)
                        }
                    }

                    if (et_contactName != null && et_contactName?.text != null) {
                        Constants.ContactName = et_contactName?.text.toString().trim()
                    }

                } else if (httpResult.requestType == "Generate_Bio") {
                    val iserror = result.optBoolean("error")
                    if (!iserror) {
                        val data = result.optJSONObject("data")
                        if (data != null) {
                            val bio = data.optString("bio", "")
                            if (et_bio != null) {
                                et_bio?.setText("")
                                et_bio?.setText(bio)
                                et_bio?.requestFocus()
                                et_bio?.setSelection(if (et_bio?.text != null) et_bio?.text!!.length else 0)
                            }
                        }
                    } else {
                        if (result.has("errors")) {
                            val errors = result.optJSONArray("errors")
                            if (errors != null && errors.length() > 0) {
                                val fieldNames = StringBuilder()
                                for (i in 0 until errors.length()) {
                                    val err = errors.optJSONObject(i)
                                    if (err != null) {
                                        val field = err.optString("field", "")
                                        if (field.isNotEmpty()) {
                                            if (fieldNames.isNotEmpty()) fieldNames.append(", ")
                                            fieldNames.append(formatFieldName(field))
                                        }
                                    }
                                }
                                AndroidUtils.showAlert("Please fill in the necessary fields: $fieldNames", activity)
                            }
                        } else {
                            AndroidUtils.showAlert(result.optString("msg", "Failed to generate bio"), activity)
                        }
                    }
                } else if (httpResult.requestType == "COURT_STATES") {
                    val iserror = result.optBoolean("error")
                    if (!iserror) {
                        val dataObj = result.optJSONObject("data")
                        if (dataObj != null) {
                            courtStatesMap.clear()
                            courtStateList.clear()
                            val statesArray = dataObj.optJSONArray("states")
                            if (statesArray != null) {
                                for (i in 0 until statesArray.length()) {
                                    val stObj = statesArray.optJSONObject(i) ?: continue
                                    val stName = stObj.optString("state", "").trim()
                                    val citiesArr = stObj.optJSONArray("cities")
                                    val cities = ArrayList<String>()
                                    if (citiesArr != null) {
                                        for (j in 0 until citiesArr.length()) {
                                            val c = citiesArr.optString(j, "").trim()
                                            if (c.isNotEmpty()) cities.add(c)
                                        }
                                    }
                                    if (stName.isNotEmpty()) {
                                        courtStatesMap[stName] = cities
                                        courtStateList.add(stName)
                                    }
                                }
                            }
                            courtStateList.sort()
                        }
                    }
                    val cb = pendingCourtStatesCallback
                    pendingCourtStatesCallback = null
                    if (cb != null && isAdded) requireActivity().runOnUiThread(cb)

                } else if (httpResult.requestType == "COURT_SUGGEST") {
                    val iserror = result.optBoolean("error")
                    suggestedCourtTypesList.clear()
                    allCourtTypesList.clear()
                    if (!iserror) {
                        val dataObj = result.optJSONObject("data")
                        if (dataObj != null) {
                            val suggestions = dataObj.optJSONArray("court_suggestions")
                            if (suggestions != null) {
                                for (i in 0 until suggestions.length()) {
                                    val s = suggestions.optString(i, "").trim()
                                    if (s.isNotEmpty()) suggestedCourtTypesList.add(s)
                                }
                            }
                            val allCourts = dataObj.optJSONArray("all_courts")
                            if (allCourts != null) {
                                for (i in 0 until allCourts.length()) {
                                    val s = allCourts.optString(i, "").trim()
                                    if (s.isNotEmpty()) allCourtTypesList.add(s)
                                }
                            }
                        }
                    }
                    val cb = pendingCourtSuggestCallback
                    pendingCourtSuggestCallback = null
                    if (cb != null && isAdded) requireActivity().runOnUiThread(cb)

                } else if (httpResult.requestType == "SERVICES_SUGGEST") {
                    val iserror = result.optBoolean("error")
                    if (!iserror) {
                        val data = result.optJSONObject("data")
                        suggestedServicesList.clear()
                        allServicesList.clear()
                        if (data != null) {
                            val suggested = data.optJSONArray("service_suggestions")
                            if (suggested != null) {
                                for (i in 0 until suggested.length()) {
                                    val s = suggested.optString(i, "").trim()
                                    if (s.isNotEmpty()) suggestedServicesList.add(s)
                                }
                            }
                            val allArr = data.optJSONArray("all_services")
                            if (allArr != null) {
                                for (i in 0 until allArr.length()) {
                                    val s = allArr.optString(i, "").trim()
                                    if (s.isNotEmpty()) allServicesList.add(s)
                                }
                            }
                        }
                    }

                    if (rvServicesList != null && rvServicesList?.adapter != null) {
                        val adapter = rvServicesList?.adapter as CommonMultiSelectionAdapter
                        val practiceView = if (llViewScreen != null && llViewScreen!!.childCount > 0) llViewScreen!!.getChildAt(0) else null
                        val etSearch = practiceView?.findViewById<TextInputEditText>(R.id.et_services_search)
                        refreshServicesAdapter(etSearch, adapter, imgServicesDrop)
                    }

                    val cb = pendingCourtStatesCallback
                    pendingCourtStatesCallback = null
                    if (cb != null && isAdded) requireActivity().runOnUiThread(cb)

                    rvServicesList?.visibility = View.GONE
                    isServicesDropdownOpen = false
                    imgServicesDrop?.animate()?.rotation(0f)?.setDuration(200)?.start()
                    val currentPA = getCurrentPracticeAreaValues()
                    callCourtSuggestApi(currentPA) {
                        if (isAdded && llCourtEnrollments != null && llCourtEnrollments!!.childCount > 0) {
                            refreshCourtEnrollmentRows()
                        }
                    }

                } else if (httpResult.requestType == "SERVICES_SEARCH") {
                    val iserror = result.optBoolean("error")
                    searchedServicesList.clear()
                    if (!iserror) {
                        val data = result.optJSONObject("data")
                        if (data != null) {
                            val arr = data.optJSONArray("services")
                            if (arr != null) {
                                for (i in 0 until arr.length()) {
                                    val s = arr.optString(i, "").trim()
                                    if (s.isNotEmpty()) searchedServicesList.add(s)
                                }
                            }
                        }
                    }
                    val cb = pendingCourtStatesCallback
                    pendingCourtStatesCallback = null
                    if (cb != null && isAdded) requireActivity().runOnUiThread(cb)

                } else if (httpResult.requestType == "HIGH_COURTS") {
                    val iserror = result.optBoolean("error")
                    if (!iserror) {
                        val dataArray = result.getJSONArray("data")
                        highCourtsList.clear()
                        highCourtStateList.clear()
                        highCourtCitiesMap.clear()
                        highCourtNameMap.clear()

                        for (i in 0 until dataArray.length()) {
                            val courtObj = dataArray.getJSONObject(i)
                            val court = CourtData()
                            court.id = courtObj.optString("id")
                            court.name = courtObj.optString("name")
                            court.court_type = courtObj.optString("court_type")
                            court.city = courtObj.optString("city")
                            court.state = courtObj.optString("state")

                            val jurisdictionArray = courtObj.optJSONArray("jurisdiction")
                            if (jurisdictionArray != null) {
                                val jurisdiction = ArrayList<String>()
                                for (j in 0 until jurisdictionArray.length()) {
                                    jurisdiction.add(jurisdictionArray.getString(j))
                                }
                                court.jurisdiction = jurisdiction
                            }

                            val benchesArray = courtObj.optJSONArray("benches")
                            if (benchesArray != null) {
                                val benches = ArrayList<String>()
                                for (j in 0 until benchesArray.length()) {
                                    benches.add(benchesArray.getString(j))
                                }
                                court.benches = benches
                            }

                            highCourtsList.add(court)

                            val st = court.state
                            if (!st.isNullOrEmpty()) {
                                if (!highCourtStateList.contains(st)) {
                                    highCourtStateList.add(st)
                                }

                                val cities = ArrayList<String>()
                                if (!court.city.isNullOrEmpty()) {
                                    cities.add(court.city!!)
                                }
                                if (court.benches != null) {
                                    for (bench in court.benches!!) {
                                        if (!bench.isNullOrEmpty() && !cities.contains(bench)) {
                                            cities.add(bench)
                                        }
                                    }
                                }
                                highCourtCitiesMap[st] = cities

                                if (!court.name.isNullOrEmpty()) {
                                    highCourtNameMap[st] = court.name!!
                                }
                            }
                        }
                        highCourtStateList.sort()
                    }
                    callSupremeCourt()

                } else if (httpResult.requestType == "SUPREME_COURT") {
                    val iserror = result.optBoolean("error")
                    if (!iserror) {
                        val dataObj = result.getJSONObject("data")
                        supremeCourtData = CourtData()
                        supremeCourtData?.id = dataObj.optString("id")
                        supremeCourtData?.name = dataObj.optString("name")
                        supremeCourtData?.court_type = dataObj.optString("court_type")
                        supremeCourtData?.city = dataObj.optString("city")
                        supremeCourtData?.state = dataObj.optString("state")

                        refreshCourtEnrollmentRows()
                    }

                } else if (httpResult.requestType == "Case Type") {
                    if (!result.getBoolean("error")) {
                        val jsonArray = result.getJSONArray("data")
                        caseTypeList.clear()
                        for (i in 0 until jsonArray.length()) {
                            caseTypeList.add(jsonArray.getString(i))
                        }
                        if (caseTypeList.isNotEmpty()) loadCaseTypes()
                    } else {
                        AndroidUtils.showAlert(result.getString("msg"), activity, "")
                    }
                    val currentPA = getCurrentPracticeAreaValues()
                    fetchSuggestedServices(currentPA)

                } else if (httpResult.requestType == "COUNTRIES") {
                    val dataObj = result.optJSONObject("data")
                    if (dataObj != null) {
                        val jsonArray = dataObj.getJSONArray("countries")
                        countriesList.clear()
                        for (i in 1 until jsonArray.length()) {
                            val subArr = jsonArray.getJSONArray(i)
                            val countriesDO = CountriesDO()
                            countriesDO.name = subArr.optString(1, "")
                            countriesDO.value = subArr.optString(0, "")
                            countriesList.add(countriesDO)
                        }
                        loadCountryData()
                        callCaseType()
                    }

                } else if (httpResult.requestType == "Profile_upload") {
                    val iserror = result.optBoolean("error")
                    AndroidUtils.showAlert(result.optString("msg"), activity)
                    if (!iserror) {
                        profile()
                    }

                } else if (httpResult.requestType == "Profile_delete") {
                    val iserror = result.optBoolean("error")
                    AndroidUtils.showAlert(result.optString("msg"), activity)
                    if (!iserror) {
                        Constants.firm_image = ""
                        if (iv_profile != null) {
                            iv_profile?.setImageResource(R.drawable.ic_profile_placeholder)
                            iv_profile?.visibility = View.GONE
                        }
                        if (iv_delete_circle != null) {
                            iv_delete_circle?.visibility = View.GONE
                        }
                        if (person_icon != null) {
                            person_icon?.visibility = View.VISIBLE
                            val initial = if (!TextUtils.isEmpty(Constants.NAME)) {
                                Constants.NAME!!.substring(0, 1).uppercase()
                            } else if (!TextUtils.isEmpty(Constants.FIRM_NAME)) {
                                Constants.FIRM_NAME!!.substring(0, 1).uppercase()
                            } else {
                                "?"
                            }
                            person_icon?.text = initial
                        }
                    }

                } else if (httpResult.requestType == "Profile") {
                    val iserror = result.optBoolean("error")
                    if (!iserror) {
                        val d = result.optJSONObject("data")
                        if (d != null) {
                            val url = d.optString("imageUrl", "")
                            if (url.isNotEmpty()) {
                                Constants.firm_image = url
                                AppImageCache.preload(requireContext(), url)
                                iv_profile?.let { AndroidUtils.loadProfileImage(requireContext(), url, it, person_icon) }
                                iv_delete_circle?.visibility = View.VISIBLE
                            } else {
                                iv_delete_circle?.visibility = View.GONE
                            }
                        }
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    private fun loadCaseTypes() {
        val currentlySelected = ArrayList<String>()
        if (chipPractice != null) {
            val existing = getChipValuesFromGroup(chipPractice!!)
            for (i in 0 until existing.length()) {
                currentlySelected.add(existing.optString(i, ""))
            }
        }
        practiceAreaAdapter = CommonMultiSelectionAdapter(
            requireContext(),
            caseTypeList,
            currentlySelected,
            object : CommonMultiSelectionAdapter.OnSelectionChangeListener {
                override fun onSelectionChanged(selectedItems: List<String>) {
                    if (chipPractice != null) {
                        chipPractice?.removeAllViews()
                        for (item in selectedItems) {
                            addChip(chipPractice!!, item)
                        }
                    }
                    syncCourtSectionFromPracticeChange()
                }
            }
        )
        if (sp_practice != null) {
            sp_practice?.layoutManager = LinearLayoutManager(context)
            sp_practice?.adapter = practiceAreaAdapter
        }
        if (tl_practice_area != null) {
            tl_practice_area?.setOnClickListener { togglePracticeRecyclerView() }
        }
        if (img_dropdown_icon != null) {
            img_dropdown_icon?.setOnClickListener { togglePracticeRecyclerView() }
        }
        if (img_clear_icon != null) {
            img_clear_icon?.visibility = View.GONE
        }
    }

    private fun togglePracticeRecyclerView() {
        if (sp_practice == null) return
        if (sp_practice?.visibility == View.VISIBLE) {
            sp_practice?.visibility = View.GONE
            isCaseTypeChecked = true
            img_dropdown_icon?.animate()?.rotation(0f)?.setDuration(200)?.start()
        } else {
            sp_practice?.visibility = View.VISIBLE
            isCaseTypeChecked = false
            img_dropdown_icon?.animate()?.rotation(180f)?.setDuration(200)?.start()
        }
    }

    private fun syncCourtSectionFromPracticeChange() {
        if (!isAdded) return
        val practiceView = if (llViewScreen != null && llViewScreen!!.childCount > 0) llViewScreen!!.getChildAt(0) else null

        var tvAddCourt: TextView? = null
        var tvWarning: TextView? = null
        var chipPracticeLocal: ChipGroup? = null

        if (practiceView != null) {
            val tvAddCourtView = practiceView.findViewById<View>(R.id.tv_add_court)
            tvAddCourt = if (tvAddCourtView is TextView) tvAddCourtView else null
            tvWarning = practiceView.findViewById(R.id.tv_no_practice_warning)
            chipPracticeLocal = practiceView.findViewById(R.id.chipGroupPractice)
        }

        updateCourtSectionState(tvAddCourt, tvWarning, chipPracticeLocal)

        val hasPracticeAreas = getCurrentPracticeAreaValues().isNotEmpty()
        tvWarning?.visibility = if (hasPracticeAreas) View.GONE else View.VISIBLE

        if (llCourtEnrollments != null) {
            for (i in 0 until llCourtEnrollments!!.childCount) {
                val row = llCourtEnrollments!!.getChildAt(i)
                lockOrUnlockCourtRow(row, hasPracticeAreas)
            }
        }
    }

    private fun lockOrUnlockCourtRow(row: View?, enabled: Boolean) {
        if (row == null) return
        val alpha = if (enabled) 1f else 0.4f

        val llCourtType = row.findViewById<LinearLayout>(R.id.ll_court_type)
        val llStateSpinner = row.findViewById<LinearLayout>(R.id.ll_state_spinner)
        val tlCityArea = row.findViewById<LinearLayout>(R.id.tl_city_area)
        val etCitySearch = row.findViewById<TextInputEditText>(R.id.et_city_search)

        val imgCtDrop = llCourtType?.findViewById<ImageView>(R.id.img_dropdown_icon)
        val imgCtClear = llCourtType?.findViewById<ImageView>(R.id.img_clear_icon)
        val imgStateDrop = llStateSpinner?.findViewById<ImageView>(R.id.img_dropdown_icon)
        val imgStateClear = llStateSpinner?.findViewById<ImageView>(R.id.img_clear_icon)
        val btnCityConfirm = row.findViewById<AppCompatImageButton>(R.id.btn_city_confirm)
        val btnCityCancel = row.findViewById<AppCompatImageButton>(R.id.btn_city_cancel)
        val imgCityDropdown = row.findViewById<ImageView>(R.id.img_city_dropdown)

        llCourtType?.isEnabled = enabled
        llCourtType?.isClickable = enabled
        llCourtType?.alpha = alpha
        if (!enabled) llCourtType?.setOnClickListener(null)

        llStateSpinner?.isEnabled = enabled
        llStateSpinner?.isClickable = enabled
        llStateSpinner?.alpha = alpha
        if (!enabled) llStateSpinner?.setOnClickListener(null)

        tlCityArea?.isEnabled = enabled
        tlCityArea?.isClickable = enabled
        tlCityArea?.alpha = alpha

        etCitySearch?.isEnabled = enabled
        etCitySearch?.isFocusable = enabled
        etCitySearch?.isFocusableInTouchMode = enabled
        etCitySearch?.isClickable = enabled
        etCitySearch?.alpha = alpha

        if (!enabled) {
            imgCtDrop?.visibility = View.GONE
            imgCtClear?.visibility = View.GONE
            imgStateDrop?.visibility = View.GONE
            imgStateClear?.visibility = View.GONE
            btnCityConfirm?.visibility = View.GONE
            btnCityCancel?.visibility = View.GONE
            imgCityDropdown?.visibility = View.GONE
        } else {
            val tvCt = llCourtType?.findViewById<TextView>(R.id.tv_spinner_view)
            val hasCourtType = tvCt != null && !TextUtils.isEmpty(tvCt.text)
            imgCtDrop?.visibility = if (hasCourtType) View.GONE else View.VISIBLE
            imgCtClear?.visibility = if (hasCourtType) View.VISIBLE else View.GONE

            val tvState = llStateSpinner?.findViewById<TextView>(R.id.tv_spinner_view)
            val hasState = tvState != null && !TextUtils.isEmpty(tvState.text)
            imgStateDrop?.visibility = if (hasState) View.GONE else View.VISIBLE
            imgStateClear?.visibility = if (hasState) View.VISIBLE else View.GONE

            val hasCity = etCitySearch != null && !TextUtils.isEmpty(etCitySearch.text)
            imgCityDropdown?.visibility = if (hasCity) View.GONE else View.VISIBLE
            btnCityConfirm?.visibility = if (hasCity) View.GONE else View.VISIBLE
            btnCityCancel?.visibility = if (hasCity) View.GONE else View.VISIBLE
        }
    }

    private fun updateCourtSectionState(
        tvAddCourt: TextView?,
        tvWarning: TextView?,
        chipGroupPractice: ChipGroup?
    ) {
        if (tvAddCourt == null) return
        val source = if (chipPractice != null) chipPractice else chipGroupPractice
        var hasPracticeAreas = false
        if (source != null && source.childCount > 0) {
            val vals = getChipValuesFromGroup(source)
            hasPracticeAreas = vals.length() > 0
        }

        if (hasPracticeAreas) {
            tvAddCourt.isEnabled = true
            tvAddCourt.isClickable = true
            tvAddCourt.alpha = 1f
            tvWarning?.visibility = View.GONE
        } else {
            tvAddCourt.isEnabled = false
            tvAddCourt.isClickable = false
            tvAddCourt.alpha = 0.4f
        }
    }

    private fun buildSortedServicesList(): ArrayList<String> {
        val result = ArrayList<String>()
        for (s in suggestedServicesList) {
            if (!result.contains(s)) result.add(s)
        }
        val rest = ArrayList<String>()
        for (s in allServicesList) {
            if (!suggestedServicesList.contains(s) && !result.contains(s)) {
                rest.add(s)
            }
        }
        rest.sort()
        result.addAll(rest)
        return result
    }

    private fun clearDetails() {
        liveChipPracticeAreas = null
        liveChipLanguages = null
        liveChipServices = null
        et_Firmname?.setText("")
        et_Country?.text = ""
        et_Email?.setText("")
        et_contactName?.setText("")
        et_ContactPhone?.setText("")
        et_Website?.setText("")
        et_DefaultCurrency?.text = ""
        et_Building?.setText("")
        et_City?.setText("")
        et_Street?.setText("")
        et_State?.setText("")
        et_ad_Country?.text = ""
        et_Zip?.setText("")
        met_Building?.setText("")
        met_City?.setText("")
        met_Street?.setText("")
        met_State?.setText("")
        met_ad_Country?.text = ""
        met_Zip?.setText("")
        Constants.Edit_OR_View = "View"
        Constants.issubscription = false
        firmProfile.ChangeBackGround()
        firmProfile.NavFragment(PracticePartnerView.newInstance(firmProfile))

        val fp = isFirmProfile()
        if (fp) {
            mViewModel?.setData("Firm Profile")
        } else {
            mViewModel?.setData(resources.getString(R.string.my_profile))
        }
    }

    private fun isFirmProfile(): Boolean {
        return !Constants.isMyProfileClicked
    }

    private fun addChip(chipGroup: ChipGroup, text: String) {
        val showEditIcon = chipGroup.id == R.id.chipGroupCasesHandled
        EditableChipHelper.addChip(
            chipGroup.context,
            chipGroup,
            text,
            showEditIcon,
            { newText ->
                if (chipGroup.id == R.id.chipGroupCasesHandled) {
                    val practiceView = if (llViewScreen != null && llViewScreen!!.childCount > 0) llViewScreen!!.getChildAt(0) else null
                    if (practiceView != null) {
                        val etCases = practiceView.findViewById<TextInputEditText>(R.id.et_cases_handled)
                        if (etCases != null) {
                            etCases.setText(newText)
                            etCases.setSelection(etCases.text?.length ?: 0)
                            etCases.requestFocus()
                        }
                    }
                }
            },
            null
        )
    }

    private fun setChips(chipGroup: ChipGroup?, values: JSONArray?) {
        chipGroup?.removeAllViews()
        if (values != null) {
            for (i in 0 until values.length()) {
                val s = values.optString(i, "")
                if (s.isNotEmpty()) {
                    chipGroup?.let { addChip(it, s) }
                }
            }
        }
    }

    private fun setupChipInput(editText: TextInputEditText?, chipGroup: ChipGroup?) {
        editText?.setOnEditorActionListener { _, actionId, event ->
            val isDone = actionId == EditorInfo.IME_ACTION_DONE ||
                    (event != null && event.keyCode == KeyEvent.KEYCODE_ENTER && event.action == KeyEvent.ACTION_DOWN)
            if (isDone) {
                val text = editText.text.toString().trim()
                if (text.isNotEmpty() && chipGroup != null) {
                    addChip(chipGroup, text)
                }
                editText.setText("")
                true
            } else {
                false
            }
        }
    }

    private val registeredToMailingWatcher = object : TextWatcher {
        override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
        override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        override fun afterTextChanged(s: Editable?) {
            if (isSameAddressChecked) {
                copyRegisteredToMailing()
            }
        }
    }

    private fun copyRegisteredToMailing() {
        met_Building?.setText(et_Building?.text.toString())
        met_Street?.setText(et_Street?.text.toString())
        met_City?.setText(et_City?.text.toString())
        met_State?.setText(et_State?.text.toString())
        met_ad_Country?.text = et_ad_Country?.text.toString()
        met_Zip?.setText(et_Zip?.text.toString())
    }

    private fun setMailingEditable(editable: Boolean) {
        met_Building?.isEnabled = editable
        met_Street?.isEnabled = editable
        met_City?.isEnabled = editable
        met_State?.isEnabled = editable
        met_Zip?.isEnabled = editable
    }

    private fun checkFields() {
        val shouldDisable = et_Firmname?.text.isNullOrEmpty() ||
                et_Country?.text.isNullOrEmpty() ||
                et_contactName?.text.isNullOrEmpty() ||
                et_ContactPhone?.text.isNullOrEmpty() ||
                et_Website?.text.isNullOrEmpty() ||
                et_bio?.text.isNullOrEmpty() ||
                et_DefaultCurrency?.text.isNullOrEmpty() ||
                emailhaserror || phonehaserror ||
                et_ad_Country?.text.isNullOrEmpty() ||
                et_Building?.text.isNullOrEmpty() ||
                et_Street?.text.isNullOrEmpty() ||
                et_City?.text.isNullOrEmpty() ||
                et_State?.text.isNullOrEmpty() ||
                et_Zip?.text.isNullOrEmpty() ||
                ziphaserror ||
                met_ad_Country?.text.isNullOrEmpty() ||
                met_Building?.text.isNullOrEmpty() ||
                met_Street?.text.isNullOrEmpty() ||
                met_City?.text.isNullOrEmpty() ||
                met_State?.text.isNullOrEmpty() ||
                met_Zip?.text.isNullOrEmpty() ||
                mziphaserror
        btn_save_pp?.isEnabled = !shouldDisable
    }

    private fun buildCourtNameList(state: String): ArrayList<String> {
        val names = ArrayList<String>()
        val name = highCourtNameMap[state]
        if (!name.isNullOrEmpty()) names.add(name)
        return names
    }

    private fun addCourtEnrollmentRow(
        inflater: LayoutInflater,
        parent: LinearLayout?,
        courtType: String,
        state: String,
        city: String,
        courtName: String
    ) {
        if (parent == null) return
        val row = inflater.inflate(R.layout.court_enrollment_item, parent, false)

        val llCourtType = row.findViewById<LinearLayout>(R.id.ll_court_type)
        val tvCt = llCourtType?.findViewById<TextView>(R.id.tv_spinner_view)
        tvCt?.text = convertApiToDisplayFormat(courtType)

        val llStateSpinner = row.findViewById<LinearLayout>(R.id.ll_state_spinner)
        val tvState = llStateSpinner?.findViewById<TextView>(R.id.tv_spinner_view)
        tvState?.text = state

        val etCitySearch = row.findViewById<TextInputEditText>(R.id.et_city_search)
        etCitySearch?.setText(city)

        val ivDelete = row.findViewById<ImageView>(R.id.iv_delete)
        ivDelete?.setOnClickListener {
            parent.removeView(row)
        }

        parent.addView(row)
    }

    private fun refreshCourtEnrollmentRows() {
        if (llCourtEnrollments == null) return
        val existingRows = ArrayList<Array<String>>()
        for (i in 0 until llCourtEnrollments!!.childCount) {
            val row = llCourtEnrollments!!.getChildAt(i)
            val llCt = row.findViewById<LinearLayout>(R.id.ll_court_type)
            val tvCt = llCt?.findViewById<TextView>(R.id.tv_spinner_view)
            val courtType = tvCt?.text.toString().trim()

            val llStateSpin = row.findViewById<LinearLayout>(R.id.ll_state_spinner)
            val tvState = llStateSpin?.findViewById<TextView>(R.id.tv_spinner_view)
            val state = tvState?.text.toString().trim()

            val etCitySearch = row.findViewById<TextInputEditText>(R.id.et_city_search)
            val city = etCitySearch?.text.toString().trim()

            existingRows.add(arrayOf(convertCourtTypeToApiFormat(courtType), state, city, ""))
        }

        llCourtEnrollments?.removeAllViews()
        val inflater = LayoutInflater.from(requireContext())
        for (row in existingRows) {
            addCourtEnrollmentRow(inflater, llCourtEnrollments, row[0], row[1], row[2], row[3])
        }
    }

    private fun createAwardsOnlyEdit(): View {
        llViewScreen?.removeAllViews()
        val inflater = LayoutInflater.from(requireContext())
        val detailView = inflater.inflate(R.layout.educational_edit_view, llViewScreen, false)

        val eduSection = detailView.findViewById<View>(R.id.ll_education)
        val certSection = detailView.findViewById<View>(R.id.ll_certification)
        val tvEduTitle = detailView.findViewById<TextView>(R.id.tv_title_education)
        val tvCertTitle = detailView.findViewById<TextView>(R.id.tv_title_certification)
        val tv_add_education = detailView.findViewById<TextView>(R.id.tv_add_education)
        val tv_add_certification = detailView.findViewById<TextView>(R.id.tv_add_certification)

        eduSection?.visibility = View.GONE
        certSection?.visibility = View.GONE
        tvEduTitle?.visibility = View.GONE
        tvCertTitle?.visibility = View.GONE
        tv_add_education?.visibility = View.GONE
        tv_add_certification?.visibility = View.GONE

        llAwards = detailView.findViewById(R.id.ll_awards)
        val tvAwardsTitle = detailView.findViewById<TextView>(R.id.tv_title_awards)
        val tv_add_awards = detailView.findViewById<TextView>(R.id.tv_add_awards)

        tvAwardsTitle?.text = "Awards & Recognition"
        tv_add_awards?.setText(R.string._add_more)

        tv_add_awards?.setOnClickListener {
            llAwards?.let { parent ->
                addItem(inflater, parent, tvAwardsTitle!!, "", "", "", TYPE_AWARD)
            }
        }

        val profile = firmProfileModel?.data?.profile
        val awards = profile?.awards ?: profile?.firm?.awards
        loadItems(inflater, llAwards!!, tvAwardsTitle!!, awards, TYPE_AWARD)

        llViewScreen?.addView(detailView)
        return detailView
    }

    private fun createEducationsAwardsEdit(): View {
        llViewScreen?.removeAllViews()
        val inflater = LayoutInflater.from(requireContext())
        val editView = inflater.inflate(R.layout.educational_edit_view, llViewScreen, false)

        llEducation = editView.findViewById(R.id.ll_education)
        llCertification = editView.findViewById(R.id.ll_certification)
        llAwards = editView.findViewById(R.id.ll_awards)

        val tvEduTitle = editView.findViewById<TextView>(R.id.tv_title_education)
        val tvCertTitle = editView.findViewById<TextView>(R.id.tv_title_certification)
        val tvAwardsTitle = editView.findViewById<TextView>(R.id.tv_title_awards)

        val tv_add_education = editView.findViewById<TextView>(R.id.tv_add_education)
        val tv_add_certification = editView.findViewById<TextView>(R.id.tv_add_certification)
        val tv_add_awards = editView.findViewById<TextView>(R.id.tv_add_awards)

        tv_add_education?.setText(R.string._add_more)
        tv_add_certification?.setText(R.string._add_more)
        tv_add_awards?.setText(R.string._add_more)

        tv_add_education?.setOnClickListener {
            llEducation?.let { parent ->
                addItem(inflater, parent, tvEduTitle!!, "", "", "", TYPE_EDUCATION)
            }
        }
        tv_add_certification?.setOnClickListener {
            llCertification?.let { parent ->
                addItem(inflater, parent, tvCertTitle!!, "", "", "", TYPE_CERTIFICATION)
            }
        }
        tv_add_awards?.setOnClickListener {
            llAwards?.let { parent ->
                addItem(inflater, parent, tvAwardsTitle!!, "", "", "", TYPE_AWARD)
            }
        }

        val profile = firmProfileModel?.data?.profile
        loadItems(inflater, llEducation!!, tvEduTitle!!, profile?.education, TYPE_EDUCATION)
        loadItems(inflater, llCertification!!, tvCertTitle!!, profile?.certifications, TYPE_CERTIFICATION)
        loadItems(inflater, llAwards!!, tvAwardsTitle!!, profile?.awards, TYPE_AWARD)

        llViewScreen?.addView(editView)
        return editView
    }

    private fun loadItems(
        inflater: LayoutInflater,
        parent: LinearLayout,
        title: TextView,
        list: List<Any>?,
        type: Int
    ) {
        parent.removeAllViews()
        parent.visibility = View.VISIBLE
        title.visibility = View.VISIBLE
        if (list.isNullOrEmpty()) {
            addItem(inflater, parent, title, "", "", "", type)
            return
        }
        for (obj in list) {
            when (type) {
                TYPE_EDUCATION -> {
                    val e = obj as FirmProfileModel.Education
                    addItem(inflater, parent, title, e.degree ?: "", e.university ?: "", e.passing_year.toString(), type)
                }
                TYPE_CERTIFICATION -> {
                    val c = obj as FirmProfileModel.Certification
                    addItem(inflater, parent, title, c.certification_name ?: "", c.issuing_authority ?: "", c.year_of_issue.toString(), type)
                }
                TYPE_AWARD -> {
                    val a = obj as FirmProfileModel.Award
                    addItem(inflater, parent, title, a.award_name ?: "", a.purpose ?: "", a.year_of_award.toString(), type)
                }
            }
        }
    }

    private fun addItem(
        inflater: LayoutInflater,
        parent: LinearLayout,
        title: TextView,
        titleText: String,
        content: String,
        year: String,
        type: Int
    ) {
        parent.visibility = View.VISIBLE
        title.visibility = View.VISIBLE
        val row = inflater.inflate(R.layout.educational_item_view, parent, false)
        val etTitle = row.findViewById<TextInputEditText>(R.id.et_title)
        val etContent = row.findViewById<TextInputEditText>(R.id.et_content)
        val etYear = row.findViewById<TextInputEditText>(R.id.et_year)
        etTitle.addTextChangedListener(Validation(etTitle))
        etContent.addTextChangedListener(Validation(etContent))
        etYear.inputType = InputType.TYPE_CLASS_NUMBER
        etYear.hint = "Year"
        AndroidUtils.NumberFilter(etYear, true)
        when (type) {
            TYPE_EDUCATION -> {
                etTitle.hint = "Degree Name"
                etContent.hint = "Institution"
            }
            TYPE_CERTIFICATION -> {
                etTitle.hint = "Certification Name"
                etContent.hint = "Institution"
            }
            TYPE_AWARD -> {
                etTitle.hint = "Award Name"
                etContent.hint = "Institution"
            }
        }
        etTitle.setText(titleText)
        etContent.setText(content)
        etYear.setText(year)
        setupDeleteButton(row, parent)
        parent.addView(row)
    }

    private fun setupDeleteButton(row: View, parent: LinearLayout) {
        val ivDelete = row.findViewById<ImageView>(R.id.iv_delete)
        ivDelete?.setOnClickListener {
            parent.removeView(row)
            if (parent.childCount == 0) {
                parent.visibility = View.GONE
            }
        }
    }

    private fun createCourtsAndCasesEdit(): View {
        llViewScreen?.removeAllViews()
        val inflater = LayoutInflater.from(requireContext())
        val practiceView = inflater.inflate(R.layout.practicedetails_edit, llViewScreen, false)

        val container = LinearLayout(requireContext())
        container.orientation = LinearLayout.VERTICAL
        container.layoutParams = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        )

        val llCourtSection = practiceView.findViewById<View>(R.id.ll_court_section)
        val llCasesSection = practiceView.findViewById<View>(R.id.ll_cases_handled_section)

        if (llCourtSection != null) {
            if (llCourtSection.parent is ViewGroup) {
                (llCourtSection.parent as ViewGroup).removeView(llCourtSection)
            }
            llCourtSection.visibility = View.VISIBLE

            val tvCourtLabel = llCourtSection.findViewById<TextView>(R.id.tv_court_enrollments_label)
            tvCourtLabel?.text = "Court Practice Details"
            ll_suggestedCourtType = llCourtSection.findViewById(R.id.ll_suggestedCourtType)
            val tv_recommendedCourtType = llCourtSection.findViewById<TextView>(R.id.tv_recommendedCourtType)
            tv_recommendedCourtType?.setText(R.string.recommended_court_types_for_your_selected_practice_areas)
            llCourtEnrollments = llCourtSection.findViewById(R.id.ll_court_enrollments)
            val tvNoCourt = llCourtSection.findViewById<TextView>(R.id.tvNoCourtEnrollments)

            val tvAddCourtView = llCourtSection.findViewById<View>(R.id.tv_add_court)
            val tvAddCourt = if (tvAddCourtView is TextView) tvAddCourtView else null

            val tvWarning = llCourtSection.findViewById<TextView>(R.id.tv_no_practice_warning)
            val chipPracticeLocal = practiceView.findViewById<ChipGroup>(R.id.chipGroupPractice)

            tvNoCourt?.visibility = View.GONE
            if (!suggestedCourtTypesList.isEmpty()) {
                ll_suggestedCourtType?.visibility = View.VISIBLE
            } else {
                ll_suggestedCourtType?.visibility = View.GONE
            }

            val courts = firmProfileModel?.data?.profile?.court_enrollments
            val courtInflater = LayoutInflater.from(requireContext())

            updateCourtSectionState(tvAddCourt, tvWarning, chipPracticeLocal)

            if (llCourtEnrollments != null) {
                llCourtEnrollments?.removeAllViews()
                llCourtEnrollments?.visibility = View.VISIBLE

                if (!courts.isNullOrEmpty()) {
                    val currentPA = getCurrentPracticeAreaValues()
                    callCourtStatesApi {
                        callCourtSuggestApi(currentPA) {
                            if (!isAdded || llCourtEnrollments == null) return@callCourtSuggestApi
                            llCourtEnrollments?.removeAllViews()
                            for (ce in courts) {
                                addCourtEnrollmentRow(
                                    courtInflater,
                                    llCourtEnrollments,
                                    ce.court_type ?: "",
                                    ce.state ?: "",
                                    ce.city ?: "",
                                    ce.court_name ?: ""
                                )
                            }
                        }
                    }
                }
            }

            val source = if (chipPractice != null) chipPractice else chipPracticeLocal
            var hasPracticeAreas = false
            if (source != null && source.childCount > 0) {
                val vals = getChipValuesFromGroup(source)
                hasPracticeAreas = vals.length() > 0
            }
            if (tvWarning != null) {
                tvWarning.text = "Select at least one Practice Area to enable court details entry. "
                tvWarning.visibility = if (hasPracticeAreas) View.GONE else View.VISIBLE
            }
            if (tvAddCourt != null) {
                tvAddCourt.setText(R.string._add_court)
                tvAddCourt.setOnClickListener {
                    if (!isAdded || llCourtEnrollments == null) return@setOnClickListener
                    val currentPA = getCurrentPracticeAreaValues()
                    callCourtStatesApi(null)
                    callCourtSuggestApi(currentPA) {
                        if (!isAdded || llCourtEnrollments == null) return@callCourtSuggestApi
                        addCourtEnrollmentRow(courtInflater, llCourtEnrollments, "", "", "", "")
                    }
                }
            }

            if (chipPracticeLocal != null) {
                chipPracticeLocal.setOnHierarchyChangeListener(object : ViewGroup.OnHierarchyChangeListener {
                    override fun onChildViewAdded(parent: View, child: View) {
                        updateCourtSectionState(tvAddCourt, tvWarning, chipPracticeLocal)
                    }
                    override fun onChildViewRemoved(parent: View, child: View) {
                        updateCourtSectionState(tvAddCourt, tvWarning, chipPracticeLocal)
                        val src = if (chipPractice != null) chipPractice else chipPracticeLocal
                        var hasPA = false
                        if (src != null) {
                            val vals = getChipValuesFromGroup(src)
                            hasPA = vals.length() > 0
                        }
                        if (!hasPA && llCourtEnrollments != null && llCourtEnrollments!!.childCount > 0) {
                            tvWarning?.visibility = View.VISIBLE
                        }
                    }
                })
            }

            container.addView(llCourtSection)
        }

        if (llCourtSection != null && llCasesSection != null) {
            val divider = View(requireContext())
            val dividerHeightPx = TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP, 1.5f,
                requireContext().resources.displayMetrics
            ).toInt()
            val marginPx = TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP, 10f,
                requireContext().resources.displayMetrics
            ).toInt()
            val divParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, dividerHeightPx
            )
            divParams.setMargins(marginPx, marginPx, marginPx, marginPx)
            divider.layoutParams = divParams
            divider.setBackgroundColor(Color.parseColor("#CCCCCC"))
            container.addView(divider)
        }

        if (llCasesSection != null) {
            if (llCasesSection.parent is ViewGroup) {
                (llCasesSection.parent as ViewGroup).removeView(llCasesSection)
            }
            llCasesSection.visibility = View.VISIBLE

            val tvCasesLabel = llCasesSection.findViewById<TextView>(R.id.tv_cases_handled_label)
            tvCasesLabel?.text = "Types of Cases Handled"

            val chipCases = llCasesSection.findViewById<ChipGroup>(R.id.chipGroupCasesHandled)
            val etCases = llCasesSection.findViewById<TextInputEditText>(R.id.et_cases_handled)
            chipCasesHandled = chipCases
            if (chipCasesHandled != null) {
                chipCasesHandled?.removeAllViews()
                val cases = firmProfileModel?.data?.profile?.cases_handled
                if (cases != null) {
                    for (c in cases) {
                        addChip(chipCasesHandled!!, c)
                    }
                }
            }

            etCases?.filters = arrayOf(InputFilter { source, start, end, dest, dstart, dend ->
                if (source != null && source.toString() == "\n") {
                    return@InputFilter null
                }
                val destLen = dest.length - (dend - dstart)
                val keep = 500 - destLen
                if (keep <= 0) {
                    return@InputFilter ""
                }
                if (end - start <= keep) {
                    return@InputFilter null
                }
                source.subSequence(start, start + keep)
            })

            etCases?.setOnEditorActionListener { _, actionId, event ->
                val isDone = actionId == EditorInfo.IME_ACTION_DONE ||
                        (event != null && event.keyCode == KeyEvent.KEYCODE_ENTER && event.action == KeyEvent.ACTION_DOWN)
                if (!isDone) return@setOnEditorActionListener false
                var text = if (etCases.text != null) etCases.text.toString().replace("\n", "").trim() else ""
                if (text.isEmpty()) return@setOnEditorActionListener true
                if (text.length > 500) text = text.substring(0, 500)
                chipCasesHandled?.let { addChip(it, text) }
                etCases.setText("")
                true
            }

            etCases?.addTextChangedListener(object : TextWatcher {
                override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
                override fun afterTextChanged(s: Editable?) {
                    val raw = s?.toString() ?: ""
                    if (raw.contains("\n")) {
                        var text = raw.replace("\n", "").trim()
                        if (text.isNotEmpty()) {
                            if (text.length > 500) text = text.substring(0, 500)
                            chipCasesHandled?.let { addChip(it, text) }
                        }
                        etCases.setText("")
                    }
                }
            })

            container.addView(llCasesSection)
        }

        llViewScreen?.addView(container)
        return container
    }

    private fun createSetAvailability(): View {
        llViewScreen?.removeAllViews()
        val view = LayoutInflater.from(requireContext())
            .inflate(R.layout.activity_book_setting_edit, llViewScreen, false)
        llViewScreen?.addView(view)
        llViewScreen?.visibility = View.VISIBLE

        view.findViewById<View>(R.id.set_clear).visibility = View.GONE
        view.findViewById<View>(R.id.slot_min).visibility = View.GONE

        view.findViewById<View>(R.id.iv_info1).setOnClickListener { v1 ->
            val msg = "How it works: Set your available hours. We will automatically create " +
                    "30-minute booking slots. Need a break? Click Exclude icon to block specific " +
                    "times (e.g., 12:00-01.00 PM)."

            val popupText = TextView(requireContext())
            popupText.text = msg
            popupText.setPadding(30, 20, 30, 20)
            popupText.setTextColor(ResourcesCompat.getColor(requireContext().resources, R.color.blue, null))
            popupText.setBackgroundResource(R.drawable.info_box_bg)
            popupText.typeface = ResourcesCompat.getFont(requireContext(), R.font.gill_sans)
            popupText.setTextSize(TypedValue.COMPLEX_UNIT_SP, 13f)

            val popup = PopupWindow(
                popupText,
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT,
                true
            )

            popup.elevation = 10f
            popup.showAsDropDown(v1, 0, 10)
        }

        val llDaysContainer = view.findViewById<LinearLayout>(R.id.ll_days_container)
        llDaysContainer.removeAllViews()

        val inflater = LayoutInflater.from(requireContext())
        val apiSchedule = getApiWeeklySchedule()
        val dayViews = ArrayList<View>()

        for (schedule in apiSchedule) {
            val dayName = schedule.day_name ?: ""
            val dateLabel = schedule.date_label ?: ""

            val dayView = inflater.inflate(R.layout.item_week_day, llDaysContainer, false)

            val tvDay = dayView.findViewById<TextView>(R.id.tvDay)
            tvDay.text = if (!TextUtils.isEmpty(dateLabel)) dateLabel else dayName

            dayView.tag = dayName
            dayView.setTag(R.id.tvDay, schedule.date)

            setupDayRow(dayView, dayName, schedule)

            llDaysContainer.addView(dayView)
            dayViews.add(dayView)
        }

        val weekdays = HashSet(
            listOf("Monday", "Tuesday", "Wednesday", "Thursday", "Friday")
        )

        view.findViewById<View>(R.id.tv_set_weekdays).setOnClickListener {
            for (dayView in dayViews) {
                val tagObj = dayView.tag
                if (tagObj !is String) continue
                val cbDay = dayView.findViewById<CheckBox>(R.id.cbDay)
                cbDay?.isChecked = weekdays.contains(tagObj)
            }
        }

        view.findViewById<View>(R.id.tv_clear_all).setOnClickListener {
            for (dayView in dayViews) {
                val cbDay = dayView.findViewById<CheckBox>(R.id.cbDay)
                cbDay?.isChecked = false
            }
        }

        return view
    }

    private fun getApiWeeklySchedule(): List<FirmProfileModel.WeeklySchedule> {
        val profile = firmProfileModel?.data?.profile ?: return ArrayList()
        val availability = profile.availability ?: return ArrayList()
        return availability.weekly_schedule ?: ArrayList()
    }

    private fun setupDayRow(dayView: View, dayName: String, savedSchedule: FirmProfileModel.WeeklySchedule?) {
        val cbDay = dayView.findViewById<CheckBox>(R.id.cbDay)
        val tvFrom = dayView.findViewById<TextView>(R.id.tvFromTime)
        val tvTo = dayView.findViewById<TextView>(R.id.tvToTime)
        val tvavail = dayView.findViewById<TextView>(R.id.noavail)
        tvavail.setText(R.string.not_available)
        val toText = dayView.findViewById<TextView>(R.id.to_text)
        val ivSlot = dayView.findViewById<ImageView>(R.id.slot)
        val exceptionContainer = dayView.findViewById<LinearLayout>(R.id.exceptionContainer)
        val llSelectedSlots = dayView.findViewById<FlexboxLayout>(R.id.layoutSelectedSlots)
        val tvOutTime = dayView.findViewById<TextView>(R.id.tvOutTime)
        val timinglayout = dayView.findViewById<LinearLayout>(R.id.timinglayout)
        timinglayout.visibility = View.GONE
        val tvAvailTime = dayView.findViewById<TextView>(R.id.tvAvailTime)
        val selectedSlots = ArrayList<String>()

        cbDay.isChecked = false
        tvFrom.visibility = View.GONE
        tvTo.visibility = View.GONE
        toText.visibility = View.GONE
        ivSlot.visibility = View.GONE
        exceptionContainer.visibility = View.GONE
        llSelectedSlots.removeAllViews()
        tvOutTime.text = "Availability:"
        tvAvailTime.text = "Excluded:"

        if (savedSchedule != null) {
            val working = savedSchedule.isIs_working_day
            cbDay.isChecked = working
            if (working) {
                tvFrom.visibility = View.VISIBLE
                tvTo.visibility = View.VISIBLE
                toText.visibility = View.VISIBLE
                timinglayout.visibility = View.VISIBLE
                tvavail.visibility = View.GONE

                val workSlots = savedSchedule.work_slots
                if (!workSlots.isNullOrEmpty()) {
                    tvFrom.text = convertTo12HourFormat(workSlots[0].start_time)
                    tvTo.text = convertTo12HourFormat(workSlots[0].end_time)
                }

                val expertSlots = savedSchedule.expert_slots
                if (!expertSlots.isNullOrEmpty()) {
                    for (slot in expertSlots) {
                        val start12 = convertTo12HourFormat(slot.start_time)
                        val end12 = convertTo12HourFormat(slot.end_time)
                        selectedSlots.add("$start12 - $end12")
                    }
                }
            }
        }

        refreshDay(dayName, tvFrom, tvTo, ivSlot, llSelectedSlots, selectedSlots, tvOutTime, tvAvailTime)

        cbDay.setOnCheckedChangeListener { _, isChecked ->
            tvFrom.visibility = if (isChecked) View.VISIBLE else View.GONE
            tvTo.visibility = if (isChecked) View.VISIBLE else View.GONE
            toText.visibility = if (isChecked) View.VISIBLE else View.GONE
            tvavail.visibility = if (isChecked) View.GONE else View.VISIBLE
            timinglayout.visibility = if (isChecked) View.VISIBLE else View.GONE
            if (!isChecked) {
                ivSlot.visibility = View.GONE
                llSelectedSlots.removeAllViews()
                selectedSlots.clear()
                dayAvailability.remove(dayName)
                tvOutTime.text = "0h out"
                tvAvailTime.text = "0h avail"
            } else {
                updateSlotIconState(tvFrom, tvTo, ivSlot)
            }
        }

        tvFrom.setOnClickListener {
            openTimePicker(tvFrom.text.toString()) { time ->
                tvFrom.text = time
                if (!tvTo.text.toString().isEmpty()) {
                    val fixedToTime = validateMinimumDuration(tvFrom.text.toString(), tvTo.text.toString())
                    tvTo.text = fixedToTime
                }
                filterInvalidSlots(selectedSlots, tvFrom.text.toString(), tvTo.text.toString())
                rebuildSelectedSlotsView(llSelectedSlots, selectedSlots, tvFrom.context, exceptionContainer, timinglayout)
                refreshDay(dayName, tvFrom, tvTo, ivSlot, llSelectedSlots, selectedSlots, tvOutTime, tvAvailTime)
                updateSlotIconState(tvFrom, tvTo, ivSlot)
            }
        }

        tvTo.setOnClickListener {
            openTimePicker(tvTo.text.toString()) { time ->
                val fixedToTime = validateMinimumDuration(tvFrom.text.toString(), time)
                tvTo.text = fixedToTime
                filterInvalidSlots(selectedSlots, tvFrom.text.toString(), tvTo.text.toString())
                rebuildSelectedSlotsView(llSelectedSlots, selectedSlots, tvFrom.context, exceptionContainer, timinglayout)
                refreshDay(dayName, tvFrom, tvTo, ivSlot, llSelectedSlots, selectedSlots, tvOutTime, tvAvailTime)
                updateSlotIconState(tvFrom, tvTo, ivSlot)
            }
        }

        ivSlot.setOnClickListener {
            openSlotPopup(dayView.context, tvFrom, tvTo, selectedSlots, llSelectedSlots, exceptionContainer, ivSlot, tvOutTime, tvAvailTime, timinglayout)
        }
    }

    private fun filterInvalidSlots(selectedSlots: MutableList<String>, fromTime: String, toTime: String) {
        if (selectedSlots.isEmpty()) return
        if (TextUtils.isEmpty(fromTime) || TextUtils.isEmpty(toTime)) return
        val validSlots = generateSlots(fromTime, toTime)
        selectedSlots.retainAll(validSlots)
    }

    private fun rebuildSelectedSlotsView(
        llSelectedSlots: FlexboxLayout?,
        selectedSlots: List<String>,
        context: Context,
        exceptionContainer: LinearLayout?,
        timingLayout: LinearLayout?
    ) {
        if (llSelectedSlots == null) return
        llSelectedSlots.removeAllViews()
        for (s in selectedSlots) {
            llSelectedSlots.addView(createSelectedSlotTextView(context, s))
        }
        if (selectedSlots.isEmpty()) {
            timingLayout?.visibility = View.GONE
            exceptionContainer?.removeAllViews()
        } else {
            timingLayout?.visibility = View.VISIBLE
        }
    }

    private fun updateSlotIconState(tvFrom: TextView, tvTo: TextView, ivSlot: ImageView) {
        updateSlotIconVisibility(tvFrom, tvTo, ivSlot)
    }

    private fun validateMinimumDuration(fromTime: String, toTime: String): String {
        try {
            val sdf = SimpleDateFormat("hh:mm a", Locale.US)
            val from = sdf.parse(fromTime)
            val to = sdf.parse(toTime)
            if (from != null && to != null) {
                if (!to.after(from)) {
                    return addMinutes(fromTime, 30)
                }
                val diffMinutes = (to.getTime() - from.getTime()) / (1000 * 60)
                if (diffMinutes < 30) {
                    return addMinutes(fromTime, 30)
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return toTime
    }

    private fun addMinutes(time: String, minutes: Int): String {
        try {
            val sdf = SimpleDateFormat("hh:mm a", Locale.US)
            val date = sdf.parse(time) ?: return time
            val cal = Calendar.getInstance()
            cal.time = date
            cal.add(Calendar.MINUTE, minutes)
            return sdf.format(cal.time)
        } catch (e: Exception) {
            return time
        }
    }

    private fun openSlotPopup(
        context: Context,
        tvFrom: TextView,
        tvTo: TextView,
        selectedSlots: MutableList<String>,
        llSelectedSlots: FlexboxLayout,
        exceptionContainer: LinearLayout,
        ivSlot: ImageView,
        tvOutTime: TextView,
        tvAvailTime: TextView,
        timingLayout: LinearLayout
    ) {
        val fromTime = tvFrom.text.toString()
        val toTime = tvTo.text.toString()
        if (fromTime.isEmpty() || toTime.isEmpty()) return
        ivSlot.visibility = View.GONE
        val slotView = LayoutInflater.from(context).inflate(R.layout.layout_excluded_slots, exceptionContainer, false)
        val gridSlots = slotView.findViewById<GridLayout>(R.id.gridSlots)
        val tempSelected = ArrayList(selectedSlots)
        val allSlots = generateSlots(fromTime, toTime)

        val allSlotViews = ArrayList<TextView>()

        for (slot in allSlots) {
            val slotTile = createSlotView(context, slot, tempSelected, allSlotViews)
            allSlotViews.add(slotTile)
            gridSlots.addView(slotTile)
        }

        refreshSlotStates(allSlotViews, tempSelected)

        slotView.findViewById<View>(R.id.btnCancel).setOnClickListener {
            exceptionContainer.removeView(slotView)
            ivSlot.visibility = if (selectedSlots.isEmpty()) View.GONE else View.VISIBLE
        }

        slotView.findViewById<View>(R.id.btnSave).setOnClickListener {
            if (tempSelected.size > 4) {
                AndroidUtils.showAlert("You can select a maximum of 4 blocked slots.", activity)
                return@setOnClickListener
            }
            selectedSlots.clear()
            selectedSlots.addAll(tempSelected)
            llSelectedSlots.removeAllViews()
            for (s in selectedSlots) {
                llSelectedSlots.addView(createSelectedSlotTextView(context, s))
            }
            if (selectedSlots.isEmpty()) {
                timingLayout.visibility = View.GONE
            } else {
                timingLayout.visibility = View.VISIBLE
                updateSummary(fromTime, toTime, selectedSlots, tvOutTime, tvAvailTime)
            }
            exceptionContainer.removeView(slotView)
            ivSlot.visibility = View.VISIBLE
        }

        slotView.findViewById<View>(R.id.btnClose).setOnClickListener {
            exceptionContainer.removeView(slotView)
            ivSlot.visibility = View.VISIBLE
        }

        exceptionContainer.addView(slotView)
        exceptionContainer.visibility = View.VISIBLE
        slotView.bringToFront()
    }

    private fun refreshDay(
        dayName: String,
        tvFrom: TextView,
        tvTo: TextView,
        ivSlot: ImageView,
        llSelectedSlots: FlexboxLayout,
        selectedSlots: MutableList<String>,
        tvOutTime: TextView,
        tvAvailTime: TextView
    ) {
        val fromTime = tvFrom.text.toString().trim()
        val toTime = tvTo.text.toString().trim()
        if (fromTime.isEmpty() || toTime.isEmpty()) {
            ivSlot.visibility = View.GONE
            llSelectedSlots.removeAllViews()
            selectedSlots.clear()
            tvOutTime.text = "0h out"
            tvAvailTime.text = "0h avail"
            return
        }
        dayAvailability[dayName] = arrayOf(fromTime, toTime)
        updateSlotIconVisibility(tvFrom, tvTo, ivSlot)
        llSelectedSlots.removeAllViews()
        for (slot in selectedSlots) {
            llSelectedSlots.addView(createSelectedSlotTextView(tvFrom.context, slot))
        }
        updateSummary(fromTime, toTime, selectedSlots, tvOutTime, tvAvailTime)
    }

    private fun convertTo12HourFormat(time24: String?): String {
        if (time24 == null) return ""
        try {
            val inputFormat = SimpleDateFormat("H:mm", Locale.US)
            val outputFormat = SimpleDateFormat("hh:mm a", Locale.US)
            val date = inputFormat.parse(time24)
            if (date != null) return outputFormat.format(date)
        } catch (e: Exception) {
            // fallback
        }
        return time24
    }

    private fun createSelectedSlotTextView(context: Context, text: String): TextView {
        val tv = TextView(context)
        tv.text = text
        tv.setTextSize(TypedValue.COMPLEX_UNIT_SP, 12f)
        tv.setTextColor(Color.BLACK)
        tv.gravity = Gravity.CENTER
        tv.setBackgroundResource(R.drawable.bg_exclude_container)
        val padding = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 8f, context.resources.displayMetrics).toInt()
        tv.setPadding(padding, padding, padding, padding)
        val params = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.WRAP_CONTENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        )
        val margin = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 6f, context.resources.displayMetrics).toInt()
        params.setMargins(margin, margin, margin, margin)
        tv.layoutParams = params
        return tv
    }

    private fun updateSlotIconVisibility(from: TextView, to: TextView, slotIcon: ImageView) {
        val fromTime = from.text.toString()
        val toTime = to.text.toString()
        if (fromTime.isEmpty() || toTime.isEmpty()) {
            slotIcon.visibility = View.GONE
            return
        }
        val showIcon = isMoreThanFourHours(fromTime, toTime)
        slotIcon.visibility = if (showIcon) View.VISIBLE else View.GONE
    }

    private fun isMoreThanFourHours(fromTime: String, toTime: String): Boolean {
        try {
            val sdf = SimpleDateFormat("hh:mm a", Locale.US)
            var from = sdf.parse(fromTime)
            var to = sdf.parse(toTime)
            if (from != null && to != null) {
                if (to.before(from)) {
                    val cal = Calendar.getInstance()
                    cal.time = to
                    cal.add(Calendar.DATE, 1)
                    to = cal.getTime()
                }
                val diffMillis = to.getTime() - from.getTime()
                return diffMillis >= (4 * 60 * 60 * 1000)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return false
    }

    private fun generateSlots(fromTime: String, toTime: String): List<String> {
        val slots = ArrayList<String>()
        try {
            val sdf = SimpleDateFormat("hh:mm a", Locale.US)
            val startVal = sdf.parse(fromTime)
            val endVal = sdf.parse(toTime)
            if (startVal != null && endVal != null) {
                val start = Calendar.getInstance()
                start.time = startVal
                val end = Calendar.getInstance()
                end.time = endVal
                if (end.before(start)) end.add(Calendar.DATE, 1)
                while (start.before(end)) {
                    val slotEndCal = start.clone() as Calendar
                    slotEndCal.add(Calendar.MINUTE, 30)
                    if (slotEndCal.after(end)) break
                    slots.add(sdf.format(start.time) + " - " + sdf.format(slotEndCal.time))
                    start.time = slotEndCal.time
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return slots
    }

    private fun createSlotView(
        context: Context,
        slotText: String,
        tempSelectedSlots: MutableList<String>,
        allSlotViews: List<TextView>
    ): TextView {
        val tv = TextView(context)
        tv.text = slotText
        tv.gravity = Gravity.CENTER
        tv.typeface = ResourcesCompat.getFont(tv.context, R.font.gill_sans)
        tv.setTextSize(TypedValue.COMPLEX_UNIT_SP, 12f)
        val padding = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 12f, context.resources.displayMetrics).toInt()
        tv.setPadding(padding, padding, padding, padding)
        tv.setBackgroundResource(R.drawable.bg_slot_unselected)

        val params = GridLayout.LayoutParams()
        params.width = 0
        params.columnSpec = GridLayout.spec(GridLayout.UNDEFINED, 1f)
        val margin = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 8f, context.resources.displayMetrics).toInt()
        params.setMargins(margin, margin, margin, margin)
        tv.layoutParams = params

        if (tempSelectedSlots.contains(slotText)) {
            tv.isSelected = true
            tv.setTextColor(ContextCompat.getColor(context, R.color.white))
            tv.setBackgroundResource(R.drawable.bg_slot_selected)
        } else {
            tv.setTextColor(ContextCompat.getColor(context, R.color.black))
        }

        tv.setOnClickListener { v ->
            val isNowSelected = !v.isSelected

            if (isNowSelected && tempSelectedSlots.size >= 4) {
                return@setOnClickListener
            }

            v.isSelected = isNowSelected

            if (isNowSelected) {
                tv.setBackgroundResource(R.drawable.bg_slot_selected)
                tv.setTextColor(ContextCompat.getColor(context, R.color.white))
                if (!tempSelectedSlots.contains(slotText)) tempSelectedSlots.add(slotText)
            } else {
                tv.setBackgroundResource(R.drawable.bg_slot_unselected)
                tv.setTextColor(ContextCompat.getColor(context, R.color.black))
                tempSelectedSlots.remove(slotText)
            }

            refreshSlotStates(allSlotViews, tempSelectedSlots)
        }

        return tv
    }

    private fun refreshSlotStates(allSlotViews: List<TextView>, tempSelectedSlots: List<String>) {
        val maxReached = tempSelectedSlots.size >= 4
        for (slot in allSlotViews) {
            if (slot.isSelected) {
                slot.alpha = 1.0f
                slot.isClickable = true
            } else if (maxReached) {
                slot.alpha = 0.5f
                slot.isClickable = false
            } else {
                slot.alpha = 1.0f
                slot.isClickable = true
            }
        }
    }

    private fun updateSummary(
        fromTime: String?,
        toTime: String?,
        selectedSlots: List<String>?,
        tvAvailability: TextView?,
        tvExcluded: TextView?
    ) {
        if (fromTime.isNullOrBlank() || toTime.isNullOrBlank()) {
            tvAvailability?.text = "Availability:"
            tvExcluded?.text = "Excluded:"
            return
        }
        try {
            val sdf = SimpleDateFormat("hh:mm a", Locale.US)
            val from = sdf.parse(fromTime)
            val to = sdf.parse(toTime)
            if (from == null || to == null) return

            val startCal = Calendar.getInstance()
            startCal.time = from
            val endCal = Calendar.getInstance()
            endCal.time = to
            if (endCal.before(startCal)) {
                endCal.add(Calendar.DATE, 1)
            }

            val totalMinutes = (endCal.timeInMillis - startCal.timeInMillis) / (1000 * 60)
            val excludedSlots = selectedSlots?.size ?: 0
            val totalSlots = totalMinutes / 30
            val availableSlots = Math.max(0L, totalSlots - excludedSlots)
            val availableMinutes = availableSlots * 30

            tvAvailability?.text =
                "Availability: " +
                        formatHoursMinutes(availableMinutes) +
                        " (" + availableSlots + " slot" + (if (availableSlots == 1L) "" else "s") + ")"

            if (selectedSlots.isNullOrEmpty()) {
                tvExcluded?.text = "Excluded: 0 slots"
            } else {
                tvExcluded?.text =
                    "Excluded: " + excludedSlots +
                            " slot" + (if (excludedSlots == 1) "" else "s") +
                            " ; " + TextUtils.join(", ", selectedSlots)
            }

        } catch (e: Exception) {
            e.printStackTrace()
            tvAvailability?.text = "Availability:"
            tvExcluded?.text = "Excluded:"
        }
    }


    private fun openTimePicker(existingTime: String, callback: TimeCallback) {
        val cal = Calendar.getInstance()
        if (!existingTime.isEmpty()) {
            try {
                val sdf = SimpleDateFormat("hh:mm a", Locale.getDefault())
                val date = sdf.parse(existingTime)
                if (date != null) cal.time = date
            } catch (ignored: Exception) {
            }
        }
        val hour = cal.get(Calendar.HOUR_OF_DAY)
        val minute = cal.get(Calendar.MINUTE)
        val dialog = TimePickerDialog(
            context,
            { _, h, m ->
                val selectedMinute = if (m < 15) 0 else if (m < 45) 30 else 0
                var selectedHour = h
                if (m >= 45) {
                    selectedHour = (h + 1) % 24
                }
                val c = Calendar.getInstance()
                c.set(Calendar.HOUR_OF_DAY, selectedHour)
                c.set(Calendar.MINUTE, selectedMinute)
                val sdf = SimpleDateFormat("hh:mm a", Locale.getDefault())
                callback.onTimeSelected(sdf.format(c.time))
            },
            hour,
            minute,
            false
        )
        dialog.show()
    }

    fun interface TimeCallback {
        fun onTimeSelected(time: String)
    }

    private fun setupServicesOfferedUI(practiceView: View) {
        val etServicesSearch = practiceView.findViewById<TextInputEditText>(R.id.et_services_search)
        val btnServiceConfirm = practiceView.findViewById<AppCompatImageButton>(R.id.btn_service_confirm)
        val btnServiceCancel = practiceView.findViewById<AppCompatImageButton>(R.id.btn_service_cancel)
        val imgServicesDropLocal = practiceView.findViewById<ImageView>(R.id.img_services_dropdown)
        rvServicesList = practiceView.findViewById(R.id.sp_services_list)
        ll_suggestedServices = practiceView.findViewById(R.id.ll_suggestedServices)
        val tv_recommendedTxt = practiceView.findViewById<TextView>(R.id.tv_recommendedTxt)
        tv_recommendedTxt?.setText(R.string.recommended_services_for_your_selected_practice_areas)
        chipServices = practiceView.findViewById(R.id.chipGroupServices)

        val chipPracticeLocal = practiceView.findViewById<ChipGroup>(R.id.chipGroupPractice)
        if (etServicesSearch == null || rvServicesList == null) return

        val applyEnabledState = Runnable {
            val source = if (chipPractice != null) chipPractice else chipPracticeLocal
            var hasPracticeAreas = false
            if (source != null && source.childCount > 0) {
                val vals = getChipValuesFromGroup(source)
                hasPracticeAreas = vals.length() > 0
            }

            val alpha = if (hasPracticeAreas) 1f else 0.5f
            val tlServicesArea = practiceView.findViewById<LinearLayout>(R.id.tl_services_area)
            tlServicesArea?.alpha = alpha

            etServicesSearch.isEnabled = hasPracticeAreas
            etServicesSearch.isFocusable = hasPracticeAreas
            etServicesSearch.isFocusableInTouchMode = hasPracticeAreas
            etServicesSearch.isClickable = hasPracticeAreas
            imgServicesDropLocal?.isEnabled = hasPracticeAreas
            imgServicesDropLocal?.isClickable = hasPracticeAreas
            btnServiceConfirm?.isEnabled = hasPracticeAreas
            btnServiceCancel?.isEnabled = hasPracticeAreas

            if (!hasPracticeAreas) {
                rvServicesList?.visibility = View.GONE
                imgServicesDropLocal?.animate()?.rotation(0f)?.setDuration(200)?.start()
                etServicesSearch.setText("")
            }
        }

        applyEnabledState.run()

        chipPracticeLocal?.setOnHierarchyChangeListener(object : ViewGroup.OnHierarchyChangeListener {
            override fun onChildViewAdded(parent: View, child: View) {
                applyEnabledState.run()
            }
            override fun onChildViewRemoved(parent: View, child: View) {
                applyEnabledState.run()
            }
        })

        btnServiceConfirm?.visibility = View.GONE
        btnServiceCancel?.visibility = View.GONE

        val servicesAdapter = CommonMultiSelectionAdapter(
            requireContext(),
            buildSortedServicesList(),
            ArrayList(),
            object : CommonMultiSelectionAdapter.OnSelectionChangeListener {
                override fun onSelectionChanged(selectedItems: List<String>) {
                    if (chipServices == null) return
                    chipServices?.removeAllViews()
                    for (item in selectedItems) {
                        if (item.isNotEmpty()) addChip(chipServices!!, item)
                    }
                }
            }
        )

        servicesAdapter.setSuggestedItems(suggestedServicesList, true)
        if (!suggestedServicesList.isEmpty()) {
            ll_suggestedServices?.visibility = View.VISIBLE
        } else {
            ll_suggestedServices?.visibility = View.GONE
        }
        rvServicesList?.layoutManager = LinearLayoutManager(requireContext())
        rvServicesList?.adapter = servicesAdapter
        rvServicesList?.visibility = View.GONE

        val density = requireContext().resources.displayMetrics.density
        val itemPx = (48 * density).toInt()
        val maxPx = (48 * 5 * density).toInt()

        servicesAdapter.registerAdapterDataObserver(object : RecyclerView.AdapterDataObserver() {
            override fun onChanged() {
                updateHeight()
            }
            override fun onItemRangeInserted(positionStart: Int, itemCount: Int) {
                updateHeight()
            }
            override fun onItemRangeRemoved(positionStart: Int, itemCount: Int) {
                updateHeight()
            }
            private fun updateHeight() {
                val count = servicesAdapter.itemCount
                if (count == 0) {
                    rvServicesList?.visibility = View.GONE
                    return
                }
                val height = Math.min(count * itemPx, maxPx)
                val lp = rvServicesList?.layoutParams
                if (lp != null) {
                    lp.height = height
                    rvServicesList?.layoutParams = lp
                }
            }
        })

        updateAdapterSelection(servicesAdapter)

        val updatingAdapter = booleanArrayOf(false)
        chipServices?.setOnHierarchyChangeListener(object : ViewGroup.OnHierarchyChangeListener {
            override fun onChildViewAdded(parent: View, child: View) {
                if (updatingAdapter[0]) return
                updatingAdapter[0] = true
                try {
                    updateAdapterSelection(servicesAdapter)
                } finally {
                    updatingAdapter[0] = false
                }
            }
            override fun onChildViewRemoved(parent: View, child: View) {
                if (updatingAdapter[0]) return
                updatingAdapter[0] = true
                try {
                    updateAdapterSelection(servicesAdapter)
                } finally {
                    updatingAdapter[0] = false
                }
            }
        })

        etServicesSearch.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                val query = s?.toString() ?: ""
                btnServiceConfirm?.visibility = if (query.isNotEmpty()) View.VISIBLE else View.GONE
                btnServiceCancel?.visibility = if (query.isNotEmpty()) View.VISIBLE else View.GONE

                if (query.isNotEmpty() && rvServicesList?.visibility != View.VISIBLE) {
                    rvServicesList?.visibility = View.VISIBLE
                    imgServicesDropLocal?.animate()?.rotation(180f)?.setDuration(200)?.start()
                }

                servicesAdapter.filter(query)

                if (servicesAdapter.itemCount == 0 && query.isNotEmpty()) {
                    rvServicesList?.visibility = View.GONE
                    imgServicesDropLocal?.animate()?.rotation(0f)?.setDuration(200)?.start()
                }
            }
            override fun afterTextChanged(s: Editable?) {}
        })

        btnServiceConfirm?.setOnClickListener {
            val text = etServicesSearch.text.toString().trim()
            if (text.isNotEmpty() && chipServices != null) {
                var exists = false
                for (i in 0 until chipServices!!.childCount) {
                    val child = chipServices!!.getChildAt(i)
                    val tv = child.findViewById<TextView>(R.id.tv_chip_text)
                    if (tv != null && tv.text.toString() == text) {
                        exists = true
                        break
                    }
                }
                if (!exists) {
                    addChip(chipServices!!, text)
                    updateAdapterSelection(servicesAdapter)
                }
            }
            etServicesSearch.setText("")
            rvServicesList?.visibility = View.GONE
            imgServicesDropLocal?.animate()?.rotation(0f)?.setDuration(200)?.start()
        }

        btnServiceCancel?.setOnClickListener {
            etServicesSearch.setText("")
            rvServicesList?.visibility = View.GONE
            imgServicesDropLocal?.animate()?.rotation(0f)?.setDuration(200)?.start()
        }

        etServicesSearch.setOnEditorActionListener { _, actionId, event ->
            val isDone = actionId == EditorInfo.IME_ACTION_DONE ||
                    (event != null && event.keyCode == KeyEvent.KEYCODE_ENTER && event.action == KeyEvent.ACTION_DOWN)
            if (!isDone) return@setOnEditorActionListener false

            val text = etServicesSearch.text.toString().trim()
            if (text.isNotEmpty() && chipServices != null) {
                var exists = false
                for (i in 0 until chipServices!!.childCount) {
                    val child = chipServices!!.getChildAt(i)
                    val tv = child.findViewById<TextView>(R.id.tv_chip_text)
                    if (tv != null && tv.text.toString() == text) {
                        exists = true
                        break
                    }
                }
                if (!exists) {
                    addChip(chipServices!!, text)
                    updateAdapterSelection(servicesAdapter)
                }
            }
            etServicesSearch.setText("")
            rvServicesList?.visibility = View.GONE
            imgServicesDropLocal?.animate()?.rotation(0f)?.setDuration(200)?.start()
            true
        }

        imgServicesDropLocal?.setOnClickListener {
            val src = if (chipPractice != null) chipPractice else chipPracticeLocal
            var hasPANow = false
            if (src != null && src.childCount > 0) {
                val vals = getChipValuesFromGroup(src)
                hasPANow = vals.length() > 0
            }
            if (!hasPANow) return@setOnClickListener

            if (rvServicesList?.visibility == View.VISIBLE) {
                rvServicesList?.visibility = View.GONE
                imgServicesDropLocal.animate().rotation(0f).setDuration(200).start()
                etServicesSearch.setText("")
            } else {
                refreshServicesAdapter(etServicesSearch, servicesAdapter, imgServicesDropLocal)
                servicesAdapter.filter("")
                rvServicesList?.visibility = View.VISIBLE
                imgServicesDropLocal.animate().rotation(180f).setDuration(200).start()
                etServicesSearch.requestFocus()
            }
        }

        etServicesSearch.setOnFocusChangeListener { _, hasFocus ->
            val src = if (chipPractice != null) chipPractice else chipPracticeLocal
            var hasPANow = false
            if (src != null && src.childCount > 0) {
                val vals = getChipValuesFromGroup(src)
                hasPANow = vals.length() > 0
            }
            if (!hasPANow) {
                etServicesSearch.clearFocus()
                return@setOnFocusChangeListener
            }
            if (hasFocus && rvServicesList?.visibility != View.VISIBLE) {
                refreshServicesAdapter(etServicesSearch, servicesAdapter, imgServicesDropLocal)
                servicesAdapter.filter("")
                rvServicesList?.visibility = View.VISIBLE
                imgServicesDropLocal?.animate()?.rotation(180f)?.setDuration(200)?.start()
            }
        }

        etServicesSearch.setOnClickListener {
            val src = if (chipPractice != null) chipPractice else chipPracticeLocal
            var hasPANow = false
            if (src != null && src.childCount > 0) {
                val vals = getChipValuesFromGroup(src)
                hasPANow = vals.length() > 0
            }
            if (!hasPANow) return@setOnClickListener

            if (rvServicesList?.visibility != View.VISIBLE) {
                refreshServicesAdapter(etServicesSearch, servicesAdapter, imgServicesDropLocal)
                servicesAdapter.filter("")
                rvServicesList?.visibility = View.VISIBLE
                imgServicesDropLocal?.animate()?.rotation(180f)?.setDuration(200)?.start()
            }
        }

        imgServicesDrop = imgServicesDropLocal
    }

    private fun refreshServicesAdapter(
        searchEditText: TextInputEditText?,
        adapter: CommonMultiSelectionAdapter?,
        dropIcon: ImageView?
    ) {
        if (adapter == null) return
        val dataToShow = buildSortedServicesList()
        adapter.updateData(dataToShow)
        adapter.setSuggestedItems(suggestedServicesList, true)
        if (!suggestedServicesList.isEmpty()) {
            ll_suggestedServices?.visibility = View.VISIBLE
        } else {
            ll_suggestedServices?.visibility = View.GONE
        }
        updateAdapterSelection(adapter)
        searchEditText?.setText("")
    }

    private fun updateAdapterSelection(adapter: CommonMultiSelectionAdapter?) {
        if (adapter == null || chipServices == null) return

        val selected = ArrayList<String>()
        for (i in 0 until chipServices!!.childCount) {
            val child = chipServices!!.getChildAt(i)
            val tv = child.findViewById<TextView>(R.id.tv_chip_text)
            if (tv != null) {
                selected.add(tv.text.toString())
            }
        }

        val normalizedSelected = ArrayList<String>()
        for (s in selected) {
            normalizedSelected.add(s.lowercase(Locale.ROOT).replace("\\s+".toRegex(), ""))
        }

        val resolvedSelected = ArrayList<String>()
        val adapterData = adapter.getAllItems() ?: ArrayList()
        for (chip in selected) {
            val chipNorm = chip.lowercase(Locale.ROOT).replace("\\s+".toRegex(), "")
            var matched = false
            for (apiItem in adapterData) {
                val apiNorm = apiItem.lowercase(Locale.ROOT).replace("\\s+".toRegex(), "")
                if (apiNorm == chipNorm) {
                    resolvedSelected.add(apiItem)
                    matched = true
                    break
                }
            }
            if (!matched) resolvedSelected.add(chip)
        }

        adapter.setSelectedItems(resolvedSelected)
    }

    private fun getCurrentPracticeAreaValues(): List<String> {
        val result = ArrayList<String>()
        if (chipPractice == null) return result
        val arr = getChipValuesFromGroup(chipPractice!!)
        for (i in 0 until arr.length()) {
            result.add(arr.optString(i, ""))
        }
        return result
    }

    private fun createPracticeDetails() {
        callCountriesWebService()
        llViewScreen?.removeAllViews()
        val inflater = LayoutInflater.from(requireContext())
        val practiceView = inflater.inflate(R.layout.practicedetails_edit, llViewScreen, false)

        val isAMMorSuperuser = Constants.ROLE.equals("AAM", ignoreCase = true)
        val isFirmProfileMode = isFirmProfile()

        val llCasesSection = practiceView.findViewById<View>(R.id.ll_cases_handled_section)
        val llCourtSection = practiceView.findViewById<View>(R.id.ll_court_section)
        llCasesSection?.visibility = View.GONE
        llCourtSection?.visibility = View.GONE

        val tvProfHeader = practiceView.findViewById<TextView>(R.id.tv_professional_info_header)
        tvProfHeader?.text = "Professional Information"

        val tvAddrHeader = practiceView.findViewById<TextView>(R.id.tv_address_info_header)
        tvAddrHeader?.text = "Addresses"

        val tv_Firmname = practiceView.findViewById<TextView>(R.id.tv_Firmname)
        if (isFirmProfileMode || isAMMorSuperuser) {
            tv_Firmname?.text = "Registration Id"
        } else {
            tv_Firmname?.text = getString(R.string.bar_id)
        }

        val tv_nationality = practiceView.findViewById<TextView>(R.id.tv_nationality)
        tv_nationality?.setText(R.string.year_exp)
        val tv_pratice = practiceView.findViewById<TextView>(R.id.tv_pratice)
        tv_pratice?.setText(R.string.practice_area)

        val tv_lan = practiceView.findViewById<TextView>(R.id.tv_lan)
        if (isFirmProfileMode || isAMMorSuperuser) {
            tv_lan?.visibility = View.GONE
        } else {
            tv_lan?.visibility = View.VISIBLE
            tv_lan?.setText(R.string.language)
        }

        val tv_service = practiceView.findViewById<TextView>(R.id.tv_service)
        tv_service?.setText(R.string.service_offered)
        val tv_RegisterAddress = practiceView.findViewById<TextView>(R.id.tv_RegisterAddress)
        tv_RegisterAddress?.setText(R.string.mailing_address)

        val tv_Building = practiceView.findViewById<TextView>(R.id.tv_Building)
        tv_Building?.text = "Details"

        val tv_Street = practiceView.findViewById<TextView>(R.id.tv_Street)
        tv_Street?.visibility = View.GONE
        val tv_StreetParent = tv_Street?.parent as? View
        tv_StreetParent?.visibility = View.GONE

        val tv_City = practiceView.findViewById<TextView>(R.id.tv_City)
        tv_City?.setText(R.string.city)
        val tv_State = practiceView.findViewById<TextView>(R.id.tv_State)
        tv_State?.setText(R.string.state)
        val tv_ad_Country = practiceView.findViewById<TextView>(R.id.tv_ad_Country)
        tv_ad_Country?.setText(R.string.country)
        val tv_Zip = practiceView.findViewById<TextView>(R.id.tv_Zip)
        tv_Zip?.setText(R.string.zip)
        val tv_mailingAddressnNote = practiceView.findViewById<TextView>(R.id.tv_mailingAddressnNote)
        tv_mailingAddressnNote?.setText(R.string.mailing_address_is_same_as_registered_address)
        val tv_MailingAddress = practiceView.findViewById<TextView>(R.id.tv_MailingAddress)
        tv_MailingAddress?.setText(R.string.mailing_address)

        val mtv_Building = practiceView.findViewById<TextView>(R.id.mtv_Building)
        mtv_Building?.text = "Details"

        val mtv_Street = practiceView.findViewById<TextView>(R.id.mtv_Street)
        mtv_Street?.visibility = View.GONE
        val mtv_StreetParent = mtv_Street?.parent as? View
        mtv_StreetParent?.visibility = View.GONE

        val mtv_City = practiceView.findViewById<TextView>(R.id.mtv_City)
        mtv_City?.setText(R.string.city)
        val mtv_State = practiceView.findViewById<TextView>(R.id.mtv_State)
        mtv_State?.setText(R.string.state)
        val mtv_ad_Country = practiceView.findViewById<TextView>(R.id.mtv_ad_Country)
        mtv_ad_Country?.setText(R.string.country)

        sp_et_ad_country = practiceView.findViewById(R.id.sp_et_ad_country)
        sp_met_ad_country = practiceView.findViewById(R.id.sp_met_ad_country)
        ll_met_ad_country = practiceView.findViewById(R.id.ll_met_ad_country)
        ll_et_ad_country = practiceView.findViewById(R.id.ll_et_ad_country)

        val mtv_Zip = practiceView.findViewById<TextView>(R.id.mtv_Zip)
        mtv_Zip?.setText(R.string.zip)

        val ll_register_country = practiceView.findViewById<LinearLayoutCompat>(R.id.ll_register_country)
        ll_register_country?.visibility = View.GONE
        val ll_mailing_country = practiceView.findViewById<LinearLayoutCompat>(R.id.ll_mailing_country)
        ll_mailing_country?.visibility = View.GONE

        sp_practice = practiceView.findViewById(R.id.sp_practice)
        tl_practice_area = practiceView.findViewById(R.id.tl_practice_area)
        etPractice = tl_practice_area?.findViewById(R.id.tv_spinner_view)
        etPractice?.setHint(R.string.select_practice_areas)
        img_clear_icon = tl_practice_area?.findViewById(R.id.img_clear_icon)
        img_dropdown_icon = tl_practice_area?.findViewById(R.id.img_dropdown_icon)

        bindFirmPracticeDetails(practiceView)

        if (isFirmProfileMode || isAMMorSuperuser) {
            etLanguages?.visibility = View.GONE
            (etLanguages?.parent as? View)?.visibility = View.GONE
            chipLanguages?.visibility = View.GONE
            (chipLanguages?.parent as? View)?.visibility = View.GONE
        }

        et_Building?.addTextChangedListener(registeredToMailingWatcher)
        et_Street?.addTextChangedListener(registeredToMailingWatcher)
        et_City?.addTextChangedListener(registeredToMailingWatcher)
        et_State?.addTextChangedListener(registeredToMailingWatcher)
        et_Zip?.addTextChangedListener(registeredToMailingWatcher)

        tv_RegisterAddress?.setText(R.string.register_address)
        tv_RegisterAddress?.textSize = DynamicUtils.twenty.toFloat()
        tv_Building?.text = "Details"
        tv_City?.setText(R.string.city)
        tv_State?.setText(R.string.state)
        tv_ad_Country?.setText(R.string.country)
        tv_Zip?.setText(R.string.zip)
        tv_mailingAddressnNote?.setText(R.string.mailing_address_is_same_as_registered_address)
        tv_MailingAddress?.setText(R.string.mailing_address)
        tv_MailingAddress?.textSize = DynamicUtils.twenty.toFloat()
        mtv_Building?.text = "Details"
        mtv_City?.setText(R.string.city)
        mtv_State?.setText(R.string.state)
        mtv_ad_Country?.setText(R.string.country)
        mtv_Zip?.setText(R.string.zip)

        tl_practice_area?.setOnClickListener {
            if (isCaseTypeChecked) {
                sp_practice?.visibility = View.VISIBLE
            } else {
                sp_practice?.visibility = View.GONE
            }
            isCaseTypeChecked = !isCaseTypeChecked
        }

        cbYes = practiceView.findViewById(R.id.cb_yes)
        cbYes?.setOnCheckedChangeListener { _, isChecked ->
            isSameAddressChecked = isChecked
            if (isChecked) {
                copyRegisteredToMailing()
                setMailingEditable(false)
                cbYes?.setTextColor(Color.WHITE)
            } else {
                setMailingEditable(true)
                cbYes?.setTextColor(Color.BLACK)
            }
        }
        checkIfAddressesMatch()

        if (isFirmProfileMode || isAMMorSuperuser) {
            val parentRow = tv_Firmname?.parent as? LinearLayout
            if (parentRow != null && parentRow.childCount > 1) {
                parentRow.getChildAt(1).visibility = View.GONE
            }
        }

        llViewScreen?.addView(practiceView)
        llViewScreen?.visibility = View.VISIBLE
    }

    private fun checkIfAddressesMatch() {
        if (cbYes == null) return
        val match = et_Building?.text.toString().trim() == met_Building?.text.toString().trim() &&
                et_Street?.text.toString().trim() == met_Street?.text.toString().trim() &&
                et_City?.text.toString().trim() == met_City?.text.toString().trim() &&
                et_State?.text.toString().trim() == met_State?.text.toString().trim() &&
                et_Zip?.text.toString().trim() == met_Zip?.text.toString().trim()
        cbYes?.isChecked = match
        if (match) {
            setMailingEditable(false)
        }
    }

    @SuppressLint("SetTextI18n")
    private fun bindFirmPracticeDetails(view: View) {
        if (firmProfileModel == null || firmProfileModel?.data == null || firmProfileModel?.data?.profile == null) {
            return
        }

        val profile = firmProfileModel!!.data!!.profile!!
        val firm = profile.firm
        val fp = isFirmProfile()

        etFirmName = view.findViewById(R.id.et_Firmname)
        etExperience = view.findViewById(R.id.et_year_exp)

        val expValue = if (fp && firm != null) firm.years_of_incorporation else profile.years_of_experience
        etExperience?.setText(if (expValue > 0) expValue.toString() else "")
        etExperience?.inputType = InputType.TYPE_CLASS_NUMBER
        etExperience?.filters = arrayOf(InputFilter.LengthFilter(3))

        etLanguages = view.findViewById(R.id.et_languages)
        etLanguages?.setHint(R.string.select_languages_spoken)
        etServices = view.findViewById(R.id.et_services_search)
        chipPractice = view.findViewById(R.id.chipGroupPractice)

        val practiceSource = if (liveChipPracticeAreas != null) {
            liveChipPracticeAreas
        } else {
            if (fp && firm != null) firm.practice_areas else profile.practice_areas
        }
        setChips(chipPractice, practiceSource)
        tv_DefaultCurrency = view.findViewById(R.id.tv_DefaultCurrency)
        tv_DefaultCurrency?.setText(R.string.billing_currency)
        chipLanguages = view.findViewById(R.id.chipGroupLanguages)
        val langSource = if (liveChipLanguages != null) liveChipLanguages else profile.languages_spoken
        setChips(chipLanguages, langSource)

        chipServices = view.findViewById(R.id.chipGroupServices)
        val servicesSource = if (liveChipServices != null) {
            liveChipServices
        } else {
            if (fp && firm != null) firm.services_offered else profile.services_offered
        }
        setChips(chipServices, servicesSource)

        setupServicesOfferedUI(view)
        setupChipInput(etLanguages, chipLanguages)

        et_Building = view.findViewById(R.id.et_Building)
        et_Street = view.findViewById(R.id.et_Street)
        et_City = view.findViewById(R.id.et_City)
        et_State = view.findViewById(R.id.et_State)
        et_Zip = view.findViewById(R.id.et_Zip)

        sp_et_ad_country = view.findViewById(R.id.sp_et_ad_country)
        ll_et_ad_country = view.findViewById(R.id.ll_et_ad_country)
        et_ad_Country = ll_et_ad_country?.findViewById(R.id.tv_spinner_view)
        img_dropdown_icon_adCountry = ll_et_ad_country?.findViewById(R.id.img_dropdown_icon)
        img_clear_icon_adCountry = ll_et_ad_country?.findViewById(R.id.img_clear_icon)

        sp_et_ad_country?.isEnabled = false
        sp_et_ad_country?.isClickable = false
        ll_et_ad_country?.isEnabled = false
        ll_et_ad_country?.isClickable = false
        ll_et_ad_country?.isFocusable = false
        img_dropdown_icon_adCountry?.visibility = View.GONE
        img_clear_icon_adCountry?.visibility = View.GONE

        val address = firm?.address
        if (address != null) {
            et_Building?.setText(address.house_flat_no)
            et_Street?.setText(address.street)
            et_City?.setText(address.city_town)
            et_State?.setText(address.state)
            et_ad_Country?.text = address.country
            et_Zip?.setText(address.zipcode)
        }

        met_Building = view.findViewById(R.id.met_Building)
        met_Street = view.findViewById(R.id.met_Street)
        met_City = view.findViewById(R.id.met_City)
        met_State = view.findViewById(R.id.met_State)
        met_Zip = view.findViewById(R.id.met_Zip)

        et_Zip?.inputType = InputType.TYPE_CLASS_NUMBER
        met_Zip?.inputType = InputType.TYPE_CLASS_NUMBER

        val zipFilters = arrayOf<InputFilter>(
            InputFilter.LengthFilter(6),
            InputFilter { source, start, end, _, _, _ ->
                for (i in start until end) {
                    if (!Character.isDigit(source[i])) {
                        return@InputFilter ""
                    }
                }
                null
            }
        )
        et_Zip?.filters = zipFilters
        met_Zip?.filters = zipFilters

        et_Zip?.addTextChangedListener(Validation(et_Zip))
        met_Zip?.addTextChangedListener(Validation(met_Zip))
        et_ContactPhone?.let { AndroidUtils.NumberFilter(it, true) }

        sp_met_ad_country = view.findViewById(R.id.sp_met_ad_country)
        ll_met_ad_country = view.findViewById(R.id.ll_met_ad_country)
        met_ad_Country = ll_met_ad_country?.findViewById(R.id.tv_spinner_view)
        img_dropdown_icon_madCountry = ll_met_ad_country?.findViewById(R.id.img_dropdown_icon)
        img_clear_icon_madCountry = ll_met_ad_country?.findViewById(R.id.img_clear_icon)

        val corrAddress = firm?.correspondence_address
        if (corrAddress != null) {
            met_Building?.setText(corrAddress.house_flat_no)
            met_Street?.setText(corrAddress.street)
            met_City?.setText(corrAddress.city_town)
            met_State?.setText(corrAddress.state)
            met_ad_Country?.text = corrAddress.country
            met_Zip?.setText(corrAddress.zipcode)
        }
    }
}
