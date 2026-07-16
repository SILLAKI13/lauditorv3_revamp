package com.digicoffer.lauditor.FirmProfile

import android.Manifest
import android.app.Activity
import android.app.AlertDialog
import android.app.Dialog
import android.content.ContentValues
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Typeface
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.text.Editable
import android.text.SpannableString
import android.text.Spanned
import android.text.TextWatcher
import android.text.style.ForegroundColorSpan
import android.text.style.StyleSpan
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.digicoffer.lauditor.CommonFiles.FileSelection.Filecosen
import com.digicoffer.lauditor.CommonFiles.GlobalFiles.AndroidUtils
import com.digicoffer.lauditor.CommonFiles.GlobalFiles.Constants
import com.digicoffer.lauditor.R
import com.digicoffer.lauditor.Webservice.AsyncTaskCompleteListener
import com.digicoffer.lauditor.Webservice.CommonApiHelper.WebServiceHelper
import com.digicoffer.lauditor.Webservice.HttpResultDo
import com.google.android.material.textfield.TextInputEditText
import org.json.JSONArray
import org.json.JSONObject
import java.util.ArrayList

class PracticePartnerView : Fragment(), AsyncTaskCompleteListener, View.OnClickListener,
    ViewPracticePartnerAdapter.InterfaceListener {

    var et_search_pp: TextInputEditText? = null
    var tl_search_pp: View? = null
    var rv_view_pp: RecyclerView? = null
    var progress_dialog: Dialog? = null
    var firmProfile: FirmProfile? = null
    var profileUrl = ""
    var membersviewlist = ArrayList<MemberProfileModel>()
    var firmProfileModelArrayList = ArrayList<FirmProfileModel>()
    var adapter: ViewPracticePartnerAdapter? = null
    var firmProfileAdapter: ViewPracticePartnerAdapter? = null

    // Activity Result Launchers
    private var cameraLauncher: ActivityResultLauncher<Uri>? = null
    private var galleryLauncher: ActivityResultLauncher<Intent>? = null
    private var requestCameraPermission: ActivityResultLauncher<String>? = null

    // Single source of truth for the camera URI
    private var pendingCameraUri: Uri? = null

    override fun onClick(view: View) {}

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.practicepartners_view, container, false)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        cameraLauncher = registerForActivityResult(
            ActivityResultContracts.TakePicture()
        ) { success ->
            if (success == true && pendingCameraUri != null) {
                val uriToEdit = pendingCameraUri
                pendingCameraUri = null
                val active = getActiveAdapter()
                if (active != null && uriToEdit != null) {
                    active.openCropEditor(uriToEdit)
                }
            } else {
                pendingCameraUri = null
            }
        }

        requestCameraPermission = registerForActivityResult(
            ActivityResultContracts.RequestPermission()
        ) { isGranted ->
            if (isGranted) {
                val active = getActiveAdapter()
                if (active != null) {
                    openCameraWithUri(active)
                }
            } else {
                Toast.makeText(context, "Camera permission denied", Toast.LENGTH_SHORT).show()
            }
        }

        galleryLauncher = registerForActivityResult(
            ActivityResultContracts.StartActivityForResult()
        ) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                val dataIntent = result.data
                val galleryUri = dataIntent?.data
                if (galleryUri != null) {
                    val active = getActiveAdapter()
                    active?.openCropEditor(galleryUri)
                }
            }
        }

        setupOnBackPressed()
    }

    private fun getActiveAdapter(): ViewPracticePartnerAdapter? {
        return if (Constants.isMyProfileClicked) firmProfileAdapter else adapter
    }

    fun openCameraWithUri(targetAdapter: ViewPracticePartnerAdapter?) {
        try {
            val cv = ContentValues()
            cv.put(MediaStore.Images.Media.TITLE, "FirmProfile_${System.currentTimeMillis()}")
            cv.put(MediaStore.Images.Media.DESCRIPTION, "Profile Image")

            val uri = requireActivity().contentResolver.insert(
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                cv
            )

            if (uri == null) {
                Toast.makeText(context, "Cannot access camera storage", Toast.LENGTH_SHORT).show()
                return
            }

            pendingCameraUri = uri
            if (targetAdapter != null) {
                targetAdapter.cameraImageUri = uri
            }
            cameraLauncher?.launch(uri)
        } catch (e: Exception) {
            Toast.makeText(context, "Camera error: ${e.message}", Toast.LENGTH_LONG).show()
        }
    }

    fun openCropEditorFragment(imageUri: Uri?) {
        if (imageUri == null) return

        hideProfileInfoElements()

        val editor = ProfilePhotoEditorFragment.newInstance(imageUri)
        editor.setCallback(object : ProfilePhotoEditorFragment.Callback {
            override fun onCropSaved(croppedFilePath: String) {
                val active = getActiveAdapter()
                active?.handleCroppedResult(croppedFilePath)
                restoreProfileInfoElements()
            }

            override fun onChoosePhotoRequested() {
                showFileChooserPopup()
            }
        })

        val parent = parentFragment
        if (parent != null) {
            parent.childFragmentManager
                .beginTransaction()
                .replace(R.id.flFirmProfile, editor)
                .setTransition(androidx.fragment.app.FragmentTransaction.TRANSIT_FRAGMENT_FADE)
                .addToBackStack("photo_editor")
                .commit()

            parent.childFragmentManager.addOnBackStackChangedListener(
                object : androidx.fragment.app.FragmentManager.OnBackStackChangedListener {
                    override fun onBackStackChanged() {
                        if (parent.childFragmentManager.backStackEntryCount == 0) {
                            restoreProfileInfoElements()
                            parent.childFragmentManager.removeOnBackStackChangedListener(this)
                        }
                    }
                }
            )
        } else {
            parentFragmentManager
                .beginTransaction()
                .replace(R.id.flFirmProfile, editor)
                .setTransition(androidx.fragment.app.FragmentTransaction.TRANSIT_FRAGMENT_FADE)
                .addToBackStack("photo_editor")
                .commit()

            parentFragmentManager.addOnBackStackChangedListener(
                object : androidx.fragment.app.FragmentManager.OnBackStackChangedListener {
                    override fun onBackStackChanged() {
                        if (parentFragmentManager.backStackEntryCount == 0) {
                            restoreProfileInfoElements()
                            parentFragmentManager.removeOnBackStackChangedListener(this)
                        }
                    }
                }
            )
        }
    }

    private fun hideProfileInfoElements() {
        val fp = firmProfile
        if (fp != null && isAdded) {
            val fpView = fp.view
            if (fpView != null) {
                val tvSwitchView = fpView.findViewById<View>(R.id.tv_switchView)
                val tvSwitchCreate = fpView.findViewById<View>(R.id.tv_switchCreate)
                val llcEditView = fpView.findViewById<View>(R.id.llc_Edit_View)

                tvSwitchView?.visibility = View.GONE
                tvSwitchCreate?.visibility = View.GONE
                llcEditView?.visibility = View.GONE
            }
        }
    }

    private fun restoreProfileInfoElements() {
        val fp = firmProfile
        if (fp != null && isAdded) {
            fp.loadBasicProfile()

            val fpView = fp.view
            if (fpView != null) {
                if (Constants.Profile_View == "Bp") {
                    val llcEditView = fpView.findViewById<View>(R.id.llc_Edit_View)
                    llcEditView?.visibility = View.VISIBLE
                } else if (Constants.Profile_View == "Pp") {
                    val tvSwitchCreate = fpView.findViewById<View>(R.id.tv_switchCreate)
                    tvSwitchCreate?.visibility = View.VISIBLE
                }
            }
        }
    }

    private fun showFileChooserPopup() {
        val active = getActiveAdapter() ?: return

        val iv = active.currentIvProfile
        val icon = active.currentPersonIcon
        val del = active.currentIvDeleteCircle

        val fc = Filecosen(requireActivity(), object : Filecosen.FileChooserCallback {
            override fun openCamera() {
                if (requireContext().checkSelfPermission(Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
                    active.currentIvProfile = iv
                    active.currentPersonIcon = icon
                    active.currentIvDeleteCircle = del
                    openCameraWithUri(active)
                } else {
                    requestCameraPermission?.launch(Manifest.permission.CAMERA)
                }
            }

            override fun openGallery() {
                active.currentIvProfile = iv
                active.currentPersonIcon = icon
                active.currentIvDeleteCircle = del
                try {
                    val intent = Intent(
                        Intent.ACTION_PICK,
                        MediaStore.Images.Media.EXTERNAL_CONTENT_URI
                    )
                    intent.type = "image/*"
                    galleryLauncher?.launch(intent)
                } catch (e: Exception) {
                    Toast.makeText(context, "Gallery error: ${e.message}", Toast.LENGTH_LONG).show()
                }
            }
        })
        fc.show()
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

    fun callViewBasicProfile() {
        progress_dialog = AndroidUtils.get_progress(activity)
        try {
            val jsonObject = JSONObject()
            WebServiceHelper.callHttpWebService(
                this,
                requireContext(),
                WebServiceHelper.RestMethodType.GET,
                "v3/profile",
                "View_Bp",
                jsonObject.toString()
            )
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun callViewPracticePartners() {
        rv_view_pp?.visibility = View.GONE
        progress_dialog = AndroidUtils.get_progress(activity)
        try {
            val jsonObject = JSONObject()
            WebServiceHelper.callHttpWebService(
                this,
                requireContext(),
                WebServiceHelper.RestMethodType.GET,
                "v3/practice-partner",
                "View_Pp",
                jsonObject.toString()
            )
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun callDeletePracticePartners(email: String) {
        progress_dialog = AndroidUtils.get_progress(activity)
        try {
            val jsonObject = JSONObject()
            WebServiceHelper.callHttpWebService(
                this,
                requireContext(),
                WebServiceHelper.RestMethodType.DELETE,
                "v3/practice-partner/$email",
                "Delete_Pp",
                jsonObject.toString()
            )
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun buildAdapter(): ViewPracticePartnerAdapter {
        return object : ViewPracticePartnerAdapter(
            membersviewlist,
            profileUrl,
            firmProfileModelArrayList,
            requireActivity(),
            requireContext(),
            this@PracticePartnerView,
            this@PracticePartnerView,
            cameraLauncher,
            galleryLauncher,
            null,
            requestCameraPermission
        ) {
            override fun openCamera_new(
                iv: ImageView?,
                icon: TextView?,
                del: ImageView?
            ) {
                currentIvProfile = iv
                currentPersonIcon = icon
                currentIvDeleteCircle = del
                openCameraWithUri(this)
            }
        }
    }

    private fun loadmembersviewlist(jsonArray: JSONArray) {
        membersviewlist.clear()
        try {
            for (i in 0 until jsonArray.length()) {
                val jsonObject = jsonArray.getJSONObject(i)
                val model = MemberProfileModel()

                val fullName = jsonObject.optString("name")
                if (fullName.isNotEmpty()) {
                    val parts = fullName.split(" ", limit = 2)
                    model.first_name = parts[0]
                    model.last_name = if (parts.size > 1) parts[1] else ""
                }
                model.id = jsonObject.optString("id")
                model.email = jsonObject.optString("email")
                model.practice = jsonObject.optString("practice")
                model.designation = jsonObject.optString("designation")
                model.phone = jsonObject.optString("phone")
                membersviewlist.add(model)
            }

            rv_view_pp?.visibility = if (membersviewlist.isEmpty()) View.GONE else View.VISIBLE

            adapter = buildAdapter()
            firmProfileAdapter = adapter
            Constants.firmProfileAdapter = adapter

            rv_view_pp?.layoutManager = LinearLayoutManager(context)
            AndroidUtils.setupBottomSpacerFooter(
                rv_view_pp,
                resources.getDimensionPixelSize(R.dimen.twentyeight_dp)
            )
            rv_view_pp?.adapter = adapter

            tl_search_pp?.visibility = View.GONE
            rv_view_pp?.visibility = View.VISIBLE

            et_search_pp?.addTextChangedListener(object : TextWatcher {
                override fun beforeTextChanged(s: CharSequence?, st: Int, c: Int, a: Int) {}
                override fun onTextChanged(s: CharSequence?, st: Int, b: Int, c: Int) {}
                override fun afterTextChanged(s: Editable?) {
                    adapter?.filter?.filter(s)
                }
            })
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    override fun onAsyncTaskComplete(httpResult: HttpResultDo) {
        val dialog = progress_dialog
        if (dialog != null && dialog.isShowing) {
            AndroidUtils.dismiss_dialog(dialog)
        }

        if (httpResult.result == WebServiceHelper.ServiceCallStatus.Success) {
            try {
                val result = JSONObject(httpResult.responseContent)
                Log.d("Request_Type", httpResult.requestType)

                if (httpResult.requestType == "View_Pp") {
                    val isError = result.optBoolean("error")
                    if (!isError) {
                        val members = result.optJSONArray("data")
                        if (members != null) loadmembersviewlist(members)
                    }
                } else if (httpResult.requestType == "View_Bp") {
                    val isError = result.optBoolean("error")
                    if (!isError) {
                        firmProfileModelArrayList.clear()

                        val firmProfileModel = FirmProfileModel()
                        firmProfileModel.error = false

                        val dataObj = result.optJSONObject("data")
                        val profileObj = dataObj?.optJSONObject("profile")

                        if (profileObj != null) {
                            val profile = FirmProfileModel.Profile()
                            profile.uid = profileObj.optString("uid")
                            profile.name = profileObj.optString("name")
                            profile.profile_completion_percentage =
                                profileObj.optInt("profile_completion_percentage")
                            profile.show_profile_banner =
                                profileObj.optBoolean("show_profile_banner")
                            profile.email = profileObj.optString("email")
                            profile.mobile = profileObj.optString("mobile")
                            profile.nationality = profileObj.optString("nationality")
                            profile.date_of_birth = profileObj.optString("date_of_birth")
                            profile.gender = profileObj.optString("gender")
                            profile.bio_description = profileObj.optString("bio_description")

                            val picUrl = profileObj.optString("profile_pic_url", "")
                            profile.profile_pic_url = picUrl
                            if (Constants.isMyProfileClicked) {
                                profileUrl = picUrl
                            }

                            profile.bar_council_id = profileObj.optString("bar_council_id")
                            profile.years_of_experience = profileObj.optInt("years_of_experience")

                            // Consultation Fee
                            val feeObj = profileObj.optJSONObject("consultation_fee")
                            if (feeObj != null) {
                                val cf = FirmProfileModel.ConsultationFee()
                                cf.amount = feeObj.optString("amount", "")
                                cf.symbol = feeObj.optString("symbol", "₹")
                                profile.consultation_fee = cf
                            }

                            profile.practice_areas = profileObj.optJSONArray("practice_areas")
                            profile.languages_spoken = profileObj.optJSONArray("languages_spoken")
                            profile.services_offered = profileObj.optJSONArray("services_offered")

                            // Firm
                            val firmObj = profileObj.optJSONObject("firm")
                            if (firmObj != null) {
                                val firm = FirmProfileModel.Firm()
                                firm.fullname = firmObj.optString("fullname")
                                firm.email = firmObj.optString("email")
                                firm.billing_currency = firmObj.optString("billing_currency")
                                firm.contact_phone = firmObj.optString("contact_phone")
                                firm.contact_person = firmObj.optString("contact_person")
                                firm.firm_description = firmObj.optString("firm_description")
                                firm.website = firmObj.optString("website")
                                firm.profile_completion_percentage =
                                    firmObj.optInt("profile_completion_percentage")
                                firm.show_profile_banner =
                                    firmObj.optBoolean("show_profile_banner")
                                firm.reg_id = firmObj.optString("reg_id")
                                firm.years_of_incorporation =
                                    firmObj.optInt("years_of_incorporation")
                                firm.profile_pic_url = firmObj.optString("profile_pic_url", "")

                                // Firm profile_completion
                                val pcObj = firmObj.optJSONObject("profile_completion")
                                if (pcObj != null) {
                                    val pc = FirmProfileModel.ProfileCompletion()
                                    pc.completion_percentage =
                                        pcObj.optInt("completion_percentage")
                                    val nsObj = pcObj.optJSONObject("next_step")
                                    if (nsObj != null) {
                                        val ns = FirmProfileModel.NextStep()
                                        ns.section = nsObj.optString("section")
                                        ns.priority = nsObj.optInt("priority")
                                        ns.redirect_tab = nsObj.optString("redirect_tab")
                                        val mf = nsObj.optJSONArray("missing_fields")
                                        if (mf != null) {
                                            val fields = ArrayList<String>()
                                            for (i in 0 until mf.length()) {
                                                fields.add(mf.optString(i))
                                            }
                                            ns.missing_fields = fields
                                        }
                                        pc.next_step = ns
                                    }
                                    val allInc = pcObj.optJSONArray("all_incomplete")
                                    if (allInc != null) {
                                        val list = ArrayList<FirmProfileModel.NextStep>()
                                        for (i in 0 until allInc.length()) {
                                            val sObj = allInc.optJSONObject(i) ?: continue
                                            val s = FirmProfileModel.NextStep()
                                            s.section = sObj.optString("section")
                                            s.priority = sObj.optInt("priority")
                                            s.redirect_tab = sObj.optString("redirect_tab")
                                            val mf2 = sObj.optJSONArray("missing_fields")
                                            if (mf2 != null) {
                                                val fl = ArrayList<String>()
                                                for (j in 0 until mf2.length()) {
                                                    fl.add(mf2.optString(j))
                                                }
                                                s.missing_fields = fl
                                            }
                                            list.add(s)
                                        }
                                        pc.all_incomplete = list
                                    }
                                    firm.profile_completion = pc
                                }

                                firm.practice_areas = firmObj.optJSONArray("practice_areas")
                                firm.services_offered = firmObj.optJSONArray("services_offered")

                                // Firm awards
                                val firmAwardArr = firmObj.optJSONArray("awards")
                                if (firmAwardArr != null) {
                                    val awards = ArrayList<FirmProfileModel.Award>()
                                    for (i in 0 until firmAwardArr.length()) {
                                        val aObj = firmAwardArr.optJSONObject(i) ?: continue
                                        val a = FirmProfileModel.Award()
                                        a.award_name = aObj.optString("award_name")
                                        a.year_of_award = aObj.optInt("year_of_award")
                                        a.purpose = aObj.optString("purpose")
                                        awards.add(a)
                                    }
                                    firm.awards = awards
                                }

                                // Address
                                val addrObj = firmObj.optJSONObject("address")
                                if (addrObj != null) {
                                    val addr = FirmProfileModel.Address()
                                    addr.house_flat_no = addrObj.optString("house_flat_no")
                                    addr.street = addrObj.optString("street")
                                    addr.city_town = addrObj.optString("city_town")
                                    addr.state = addrObj.optString("state")
                                    addr.country = addrObj.optString("country")
                                    addr.zipcode = addrObj.optString("zipcode")
                                    firm.address = addr
                                }

                                // Correspondence address
                                val corrObj = firmObj.optJSONObject("correspondence_address")
                                if (corrObj != null) {
                                    val corr = FirmProfileModel.Address()
                                    corr.house_flat_no = corrObj.optString("house_flat_no")
                                    corr.street = corrObj.optString("street")
                                    corr.city_town = corrObj.optString("city_town")
                                    corr.state = corrObj.optString("state")
                                    corr.country = corrObj.optString("country")
                                    corr.zipcode = corrObj.optString("zipcode")
                                    firm.correspondence_address = corr
                                }

                                profile.firm = firm
                            }

                            // Availability
                            val availObj = profileObj.optJSONObject("availability")
                            if (availObj != null) {
                                val avail = FirmProfileModel.Availability()
                                avail.timezone = availObj.optString("timezone")
                                avail.slot_duration = availObj.optInt("slot_duration")
                                avail.buffer_time = availObj.optInt("buffer_time")
                                avail.advance_booking_window_hours =
                                    availObj.optInt("advance_booking_window_hours")

                                val weeklyArr = availObj.optJSONArray("weekly_schedule")
                                if (weeklyArr != null) {
                                    val weekList = ArrayList<FirmProfileModel.WeeklySchedule>()
                                    for (i in 0 until weeklyArr.length()) {
                                        val dObj = weeklyArr.optJSONObject(i) ?: continue
                                        val sched = FirmProfileModel.WeeklySchedule()
                                        sched.day_of_week = dObj.optInt("day_of_week")
                                        sched.date_label = dObj.optString("date_label")
                                        sched.date = dObj.optString("date")
                                        sched.day_name = dObj.optString("day_name")
                                        sched.isIs_working_day = dObj.optBoolean("is_working_day")

                                        val slotArr = dObj.optJSONArray("work_slots")
                                        val slots = ArrayList<FirmProfileModel.WorkSlot>()
                                        if (slotArr != null) {
                                            for (j in 0 until slotArr.length()) {
                                                val slObj = slotArr.optJSONObject(j) ?: continue
                                                val sl = FirmProfileModel.WorkSlot()
                                                sl.start_time = slObj.optString("start_time")
                                                sl.end_time = slObj.optString("end_time")
                                                slots.add(sl)
                                            }
                                        }
                                        sched.work_slots = slots

                                        val expArr = dObj.optJSONArray("expert_slots")
                                        val expSlots = ArrayList<FirmProfileModel.WorkSlot>()
                                        if (expArr != null) {
                                            for (j in 0 until expArr.length()) {
                                                val eObj = expArr.optJSONObject(j) ?: continue
                                                val es = FirmProfileModel.WorkSlot()
                                                es.start_time = eObj.optString("start_time")
                                                es.end_time = eObj.optString("end_time")
                                                expSlots.add(es)
                                            }
                                        }
                                        sched.expert_slots = expSlots
                                        weekList.add(sched)
                                    }
                                    avail.weekly_schedule = weekList
                                }
                                profile.availability = avail
                            }

                            // Education
                            val eduArr = profileObj.optJSONArray("education")
                            if (eduArr != null) {
                                val eduList = ArrayList<FirmProfileModel.Education>()
                                for (i in 0 until eduArr.length()) {
                                    val eObj = eduArr.optJSONObject(i) ?: continue
                                    val edu = FirmProfileModel.Education()
                                    edu.degree = eObj.optString("degree")
                                    edu.university = eObj.optString("university")
                                    edu.passing_year = eObj.optInt("passing_year")
                                    eduList.add(edu)
                                }
                                profile.education = eduList
                            }

                            // Certifications
                            val certArr = profileObj.optJSONArray("certifications")
                            if (certArr != null) {
                                val certList = ArrayList<FirmProfileModel.Certification>()
                                for (i in 0 until certArr.length()) {
                                    val cObj = certArr.optJSONObject(i) ?: continue
                                    val cert = FirmProfileModel.Certification()
                                    cert.certification_name = cObj.optString("certification_name")
                                    cert.issuing_authority = cObj.optString("issuing_authority")
                                    cert.year_of_issue = cObj.optInt("year_of_issue")
                                    certList.add(cert)
                                }
                                profile.certifications = certList
                            }

                            // Profile-level awards
                            val awardArr = profileObj.optJSONArray("awards")
                            if (awardArr != null) {
                                val awardList = ArrayList<FirmProfileModel.Award>()
                                for (i in 0 until awardArr.length()) {
                                    val aObj = awardArr.optJSONObject(i) ?: continue
                                    val aw = FirmProfileModel.Award()
                                    aw.award_name = aObj.optString("award_name")
                                    aw.year_of_award = aObj.optInt("year_of_award")
                                    aw.purpose = aObj.optString("purpose")
                                    awardList.add(aw)
                                }
                                profile.awards = awardList
                            }

                            // Cases Handled
                            val casesArr = profileObj.optJSONArray("cases_handled")
                            if (casesArr != null) {
                                val casesList = ArrayList<String>()
                                for (i in 0 until casesArr.length()) {
                                    casesList.add(casesArr.optString(i))
                                }
                                profile.cases_handled = casesList
                            }

                            // Court Enrollments
                            val courtArr = profileObj.optJSONArray("court_enrollments")
                            if (courtArr != null) {
                                val courtList = ArrayList<FirmProfileModel.CourtEnrollment>()
                                for (i in 0 until courtArr.length()) {
                                    val cObj = courtArr.optJSONObject(i) ?: continue
                                    val ce = FirmProfileModel.CourtEnrollment()
                                    ce.court_type = cObj.optString("court_type")
                                    ce.court_name = cObj.optString("court_name")
                                    ce.state = cObj.optString("state")
                                    ce.city = cObj.optString("city")
                                    courtList.add(ce)
                                }
                                rv_view_pp?.let { AndroidUtils.setupEdgePaddingBehavior(it) }
                                profile.court_enrollments = courtList
                            }

                            // Subscription
                            val subObj = profileObj.optJSONObject("subscription")
                            if (subObj != null) {
                                val sub = FirmProfileModel.Subscription()
                                sub.activatedOn = subObj.optString("activatedOn")
                                sub.model = subObj.optString("model")
                                sub.isActive = subObj.optBoolean("isActive")
                                sub.startDate = subObj.optString("startDate")
                                sub.endDate = subObj.optString("endDate")
                                sub.validityDays = subObj.optString("validityDays")
                                sub.nextBillingDate = subObj.optString("nextBillingDate")
                                sub.isCanUpgrade = subObj.optBoolean("canUpgrade")
                                sub.isCanPayNow = subObj.optBoolean("canPayNow")

                                val planObj = subObj.optJSONObject("plan")
                                if (planObj != null) {
                                    val plan = FirmProfileModel.Plan()
                                    plan.amount = planObj.optInt("amount")
                                    plan.cycle = planObj.optString("cycle")
                                    plan.name = planObj.optString("name")
                                    plan.currencySymbol = planObj.optString("currencySymbol")
                                    plan.currencyCode = planObj.optString("currencyCode")
                                    sub.plan = plan
                                }

                                val payObj = subObj.optJSONObject("payment")
                                if (payObj != null) {
                                    val pay = FirmProfileModel.Payment()
                                    pay.method = payObj.optString("method")
                                    pay.maskedId = payObj.optString("maskedId")
                                    sub.payment = pay
                                }
                                profile.subscription = sub
                            }

                            profile.accepted_t_c_date = profileObj.optString("accepted_t&c_date", "")

                            // Profile-level profile_completion
                            val pcObj2 = profileObj.optJSONObject("profile_completion")
                            if (pcObj2 != null) {
                                val pc2 = FirmProfileModel.ProfileCompletion()
                                pc2.completion_percentage = pcObj2.optInt("completion_percentage")

                                val nsObj2 = pcObj2.optJSONObject("next_step")
                                if (nsObj2 != null) {
                                    val ns2 = FirmProfileModel.NextStep()
                                    ns2.section = nsObj2.optString("section")
                                    ns2.priority = nsObj2.optInt("priority")
                                    ns2.redirect_tab = nsObj2.optString("redirect_tab")
                                    val mf2 = nsObj2.optJSONArray("missing_fields")
                                    if (mf2 != null) {
                                        val fields = ArrayList<String>()
                                        for (i in 0 until mf2.length()) {
                                            fields.add(mf2.optString(i))
                                        }
                                        ns2.missing_fields = fields
                                    }
                                    pc2.next_step = ns2
                                }

                                val allInc2 = pcObj2.optJSONArray("all_incomplete")
                                if (allInc2 != null) {
                                    val list2 = ArrayList<FirmProfileModel.NextStep>()
                                    for (i in 0 until allInc2.length()) {
                                        val sObj = allInc2.optJSONObject(i) ?: continue
                                        val s = FirmProfileModel.NextStep()
                                        s.section = sObj.optString("section")
                                        s.priority = sObj.optInt("priority")
                                        s.redirect_tab = sObj.optString("redirect_tab")
                                        val mf3 = sObj.optJSONArray("missing_fields")
                                        if (mf3 != null) {
                                            val fl = ArrayList<String>()
                                            for (j in 0 until mf3.length()) {
                                                fl.add(mf3.optString(j))
                                            }
                                            s.missing_fields = fl
                                        }
                                        list2.add(s)
                                    }
                                    pc2.all_incomplete = list2
                                }
                                profile.profile_completion = pc2
                            }

                            val data = FirmProfileModel.Data()
                            data.profile = profile
                            firmProfileModel.data = data
                        }

                        firmProfileModelArrayList.add(firmProfileModel)
                        Constants.firmProfileModel = firmProfileModel

                        adapter = buildAdapter()
                        firmProfileAdapter = adapter
                        Constants.firmProfileAdapter = adapter

                        rv_view_pp?.layoutManager = LinearLayoutManager(context)
                        rv_view_pp?.adapter = if (Constants.isMyProfileClicked) adapter else firmProfileAdapter
                        AndroidUtils.setupBottomSpacerFooter(
                            rv_view_pp,
                            resources.getDimensionPixelSize(R.dimen.twentyeight_dp)
                        )
                        tl_search_pp?.visibility = View.GONE
                        rv_view_pp?.visibility = View.VISIBLE
                    }
                } else if (httpResult.requestType == "Delete_Pp") {
                    val isError = result.optBoolean("error")
                    if (!isError) {
                        AndroidUtils.showAlert(result.optString("msg"), activity)
                    }
                    callViewPracticePartners()
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        } else if (httpResult.result == WebServiceHelper.ServiceCallStatus.Failed) {
            val dialog = progress_dialog
            if (dialog != null && dialog.isShowing) {
                AndroidUtils.dismiss_dialog(dialog)
            }
            try {
                val result = JSONObject(httpResult.responseContent)
                AndroidUtils.showErrorAlert(result.optString("msg"), activity)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        } else {
            val dialog = progress_dialog
            if (dialog != null && dialog.isShowing) {
                AndroidUtils.dismiss_dialog(dialog)
            }
            AndroidUtils.showErrorAlert(
                httpResult.responseContent.toString(), activity
            )
        }
    }

    override fun EditPracticePartners(model: MemberProfileModel?) {
        Constants.PpView_Type = "update"
        firmProfile?.NavFragment(PracticePartnerAdd(model, firmProfile!!))
    }

    override fun EditProfile(isProfileEdit: Boolean?, isAdditionalInfo: Boolean?) {
        val fp = firmProfile ?: return
        if (!Constants.isMyProfileClicked) {
            fp.NavFragment(
                if (isProfileEdit == true)
                    BasicProfileEdit(fp, true, false)
                else
                    BasicProfileEdit(fp, false, true)
            )
        } else {
            fp.NavFragment(
                BasicProfileEdit(fp, isProfileEdit ?: false, isAdditionalInfo ?: false)
            )
        }
    }

    override fun DeletePracticePartners(model: MemberProfileModel?) {
        if (model != null) {
            Delete_Popup(
                activity,
                "${model.first_name} ${model.last_name}",
                model.id ?: ""
            )
        }
    }

    fun add_partner() {
        firmProfile?.NavFragment(newInstance(firmProfile!!))
    }

    fun Delete_Popup(activity: Activity?, name: String?, id: String) {
        try {
            val dialogBuilder = AlertDialog.Builder(activity)
            val inflater = activity!!.layoutInflater
            val dialogLayout = inflater.inflate(R.layout.delete_relationship, null)
            val header = dialogLayout.findViewById<TextView>(R.id.header_name)
            header.setText(R.string.confirmation)
            header.setTextColor(
                ContextCompat.getColor(header.context, R.color.Primary_new)
            )

            val close = dialogLayout.findViewById<ImageView>(R.id.close_documents)
            val msg = dialogLayout.findViewById<TextView>(R.id.tv_confirmation)
            val btnYes = dialogLayout.findViewById<Button>(R.id.btn_yes)
            val btnNo = dialogLayout.findViewById<Button>(R.id.btn_No)

            btnYes.setBackgroundDrawable(
                context?.resources?.getDrawable(R.drawable.yes_button_red_button)
            )
            btnNo.setBackgroundDrawable(
                context?.resources?.getDrawable(R.drawable.no_button_green_button)
            )

            val confirmText = "Are you sure, Do you want to delete this $name ?"
            val spannable = SpannableString(confirmText)
            spannable.setSpan(
                ForegroundColorSpan(context!!.getColor(R.color.black)),
                0, confirmText.length, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
            )
            if (name != null && name.isNotEmpty()) {
                val start = confirmText.indexOf(name)
                if (start >= 0) {
                    spannable.setSpan(
                        StyleSpan(Typeface.BOLD),
                        start, start + name.length, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
                    )
                }
            }
            msg.text = spannable

            val dialog = dialogBuilder.create()
            btnNo.setOnClickListener { dialog.dismiss() }
            close.setOnClickListener { dialog.dismiss() }
            btnYes.setOnClickListener {
                dialog.dismiss()
                callDeletePracticePartners(id)
            }
            dialog.setView(dialogLayout)
            dialog.setCanceledOnTouchOutside(false)
            dialog.show()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    override fun onViewCreated(v: View, savedInstanceState: Bundle?) {
        super.onViewCreated(v, savedInstanceState)
        try {
            tl_search_pp = v.findViewById(R.id.tl_search_pp)
            et_search_pp = tl_search_pp?.findViewById(R.id.et_Search)
            rv_view_pp = v.findViewById(R.id.rv_view_pp)
            et_search_pp?.setHint(R.string.search)

            if (Constants.Profile_View != "Bp") {
                tl_search_pp?.visibility = View.VISIBLE
                rv_view_pp?.visibility = View.VISIBLE
                callViewPracticePartners()
            } else {
                tl_search_pp?.visibility = View.GONE
                rv_view_pp?.visibility = View.VISIBLE
                callViewBasicProfile()
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    companion object {
        @JvmStatic
        fun newInstance(firmProfile: FirmProfile): PracticePartnerView {
            val fragment = PracticePartnerView()
            fragment.firmProfile = firmProfile
            return fragment
        }
    }
}
