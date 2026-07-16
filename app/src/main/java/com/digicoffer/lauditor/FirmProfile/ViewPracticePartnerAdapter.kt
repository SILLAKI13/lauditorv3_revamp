package com.digicoffer.lauditor.FirmProfile

import android.animation.ObjectAnimator
import android.annotation.SuppressLint
import android.app.Activity
import android.app.Dialog
import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Color
import android.graphics.Matrix
import android.graphics.Paint
import android.graphics.Typeface
import android.media.ExifInterface
import android.net.Uri
import android.os.Handler
import android.os.Looper
import android.provider.MediaStore
import android.text.Layout
import android.text.SpannableString
import android.text.Spanned
import android.text.TextPaint
import android.text.TextUtils
import android.text.method.LinkMovementMethod
import android.text.style.ClickableSpan
import android.util.Log
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.ViewTreeObserver
import android.view.animation.AccelerateDecelerateInterpolator
import android.widget.AdapterView
import android.widget.Button
import android.widget.Filter
import android.widget.Filterable
import android.widget.GridLayout
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.ListView
import android.widget.PopupWindow
import android.widget.ScrollView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.annotation.NonNull
import androidx.appcompat.widget.AppCompatImageView
import androidx.cardview.widget.CardView
import androidx.core.content.res.ResourcesCompat
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.digicoffer.lauditor.Groups.Models.ActionModel
import com.digicoffer.lauditor.MainActivity
import com.digicoffer.lauditor.R
import com.digicoffer.lauditor.Webservice.AsyncTaskCompleteListener
import com.digicoffer.lauditor.Webservice.HttpResultDo
import com.digicoffer.lauditor.Webservice.CommonApiHelper.WebServiceHelper
import com.digicoffer.lauditor.CommonFiles.GlobalFiles.AndroidUtils
import com.digicoffer.lauditor.CommonFiles.CacheUtils.AppImageCache
import com.digicoffer.lauditor.CommonFiles.GlobalFiles.Constants
import com.digicoffer.lauditor.CommonFiles.GlobalFiles.DynamicUtils
import com.digicoffer.lauditor.CommonFiles.FileSelection.Filecosen
import com.digicoffer.lauditor.CommonFiles.CommonAdapters.CommonSpinnerAdapter
import com.google.android.material.tabs.TabLayout
import org.json.JSONArray
import org.json.JSONObject
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream
import java.io.OutputStream
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.ArrayList
import java.util.Locale
import java.util.Objects

open class ViewPracticePartnerAdapter(
    var membersviewlist: ArrayList<MemberProfileModel>,
    var profileUrl: String,
    var firmProfileModelArrayList: ArrayList<FirmProfileModel>,
    var activity: Activity,
    var context: Context,
    var practicePartnerView: PracticePartnerView,
    var eventListener: InterfaceListener,
    private var cameraLauncher: ActivityResultLauncher<Uri>?,
    private var galleryLauncher: ActivityResultLauncher<Intent>?,
    private var cropLauncher: ActivityResultLauncher<Intent>?,
    private var requestCameraPermission: ActivityResultLauncher<String>?
) : RecyclerView.Adapter<ViewPracticePartnerAdapter.MyViewHolder>(), Filterable, AsyncTaskCompleteListener {

    var itemsList: ArrayList<MemberProfileModel> = membersviewlist
    var cameraImageUri: Uri? = null
    var progress_dialog: Dialog? = null
    var view: View? = null
    var currentIvProfile: ImageView? = null
    var currentPersonIcon: TextView? = null
    var currentIvEditCircle: ImageView? = null
    var currentIvDeleteCircle: ImageView? = null
    var expandedPosition = -1
    private var recyclerView: RecyclerView? = null
    var profileCompletionBanner: CardView? = null

    private enum class ProfileCompletionStatus {
        BASIC_PROFILE_INCOMPLETE,
        ADDITIONAL_INFO_INCOMPLETE,
        COMPLETE
    }

    private class FieldDestination(
        val section: String,
        val fieldId: String,
        val tabIndex: Int
    )

    private fun resolveFieldDestination(apiField: String?): FieldDestination? {
        if (apiField == null) return null
        return when (apiField.lowercase(Locale.getDefault()).trim()) {
            "firm_name", "fullname" -> FieldDestination("basic", "et_Firmname", 0)
            "contact_person", "name" -> FieldDestination("basic", "et_contactName", 0)
            "email" -> FieldDestination("basic", "et_Email", 0)
            "contact_phone", "mobile", "phone" -> FieldDestination("basic", "et_ContactPhone", 0)
            "billing_currency", "default_currency" -> FieldDestination("additional", "ll_et_DefaultCurrency", 0)
            "website" -> FieldDestination("basic", "et_Website", 0)
            "bio_description", "firm_description", "bio", "about" -> FieldDestination("basic", "et_bio", 0)
            "consultation_fee" -> FieldDestination("additional", "ll_et_confees", 0)
            "gender" -> FieldDestination("basic", "ll_et_gender", 0)
            "date_of_birth", "dob" -> FieldDestination("basic", "et_dob", 0)
            "nationality" -> FieldDestination("basic", "et_nationality", 0)

            // Registered Address
            "house_flat_no", "address_house" -> FieldDestination("basic", "et_Building", 0)
            "street", "address_street" -> FieldDestination("basic", "et_Street", 0)
            "city_town", "address_city" -> FieldDestination("basic", "et_City", 0)
            "state", "address_state" -> FieldDestination("basic", "et_State", 0)
            "country", "address_country" -> FieldDestination("basic", "ll_et_ad_country", 0)
            "zipcode", "zip", "address_zip" -> FieldDestination("basic", "et_Zip", 0)
            "address" -> FieldDestination("basic", "et_Building", 0)

            // Mailing Address
            "correspondence_address", "mailing_address", "correspondence_house_flat_no" -> FieldDestination("basic", "met_Building", 0)
            "correspondence_street" -> FieldDestination("basic", "met_Street", 0)
            "correspondence_city", "correspondence_city_town" -> FieldDestination("basic", "met_City", 0)
            "correspondence_state" -> FieldDestination("basic", "met_State", 0)
            "correspondence_country" -> FieldDestination("basic", "ll_met_ad_country", 0)
            "correspondence_zipcode", "correspondence_zip" -> FieldDestination("basic", "met_Zip", 0)

            // Profile Picture
            "profile_pic", "logo", "firm_logo", "profile_picture" -> FieldDestination("pic", "iv_edit_circle", 0)

            // Practice Details
            "bar_council_id", "reg_id", "registration_id" -> FieldDestination("additional", "et_counsel_id", 0)
            "years_of_experience", "years_of_incorporation", "experience" -> FieldDestination("additional", "et_year_exp", 0)
            "practice_areas" -> FieldDestination("additional", "tl_practice_area", 0)
            "languages_spoken", "languages" -> FieldDestination("additional", "et_languages", 0)
            "services_offered", "services" -> FieldDestination("additional", "et_services", 0)

            // Courts & Cases
            "cases_handled", "case_types" -> FieldDestination("courts_cases", "et_cases_handled", 1)
            "court_enrollments", "court_details" -> FieldDestination("courts_cases", "ll_court_enrollments", 1)

            // Practice Details Address
            "practice_address", "office_address" -> FieldDestination("additional", "et_Building", 0)

            // Education & Awards
            "education", "degree", "university" -> FieldDestination("education", "ll_education", 2)
            "certifications", "certification" -> FieldDestination("education", "ll_certification", 2)
            "awards", "award" -> FieldDestination("education", "ll_awards", 2)

            // Availability
            "availability", "weekly_schedule", "slot_duration", "working_hours", "work_slots" -> FieldDestination("availability", "ll_days_container", 3)

            else -> null
        }
    }

    private fun isFirmProfile(): Boolean {
        return !Constants.isMyProfileClicked
    }

    fun setRecyclerView(rv: RecyclerView?) {
        this.recyclerView = rv
    }

    private fun safeNotify(position: Int) {
        val r = Runnable {
            if (position in 0 until itemCount) {
                notifyItemChanged(position)
            }
        }
        val rv = recyclerView
        if (rv != null) {
            rv.post(r)
        } else {
            Handler(Looper.getMainLooper()).post(r)
        }
    }

    fun collapseExpanded() {
        if (expandedPosition == -1) return
        val pos = expandedPosition
        expandedPosition = -1
        safeNotify(pos)
    }

    override fun getFilter(): Filter {
        return object : Filter() {
            override fun performFiltering(charSequence: CharSequence?): FilterResults {
                val cs = charSequence?.toString()?.lowercase(Locale.getDefault()) ?: ""
                if (cs.isEmpty()) {
                    membersviewlist = itemsList
                } else {
                    val filtered = ArrayList<MemberProfileModel>()
                    for (row in itemsList) {
                        val fn = row.first_name?.lowercase(Locale.getDefault()) ?: ""
                        val ln = row.last_name?.lowercase(Locale.getDefault()) ?: ""
                        if (fn.contains(cs) || ln.contains(cs)) {
                            filtered.add(row)
                        }
                    }
                    membersviewlist = filtered
                }
                val res = FilterResults()
                res.count = membersviewlist.size
                res.values = membersviewlist
                return res
            }

            @Suppress("UNCHECKED_CAST")
            override fun publishResults(c: CharSequence?, r: FilterResults?) {
                membersviewlist = (r?.values as? ArrayList<MemberProfileModel>) ?: ArrayList()
                notifyDataSetChanged()
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        val v = if (Constants.Profile_View != "Bp") {
            LayoutInflater.from(parent.context).inflate(R.layout.view_pp_card, parent, false)
        } else {
            LayoutInflater.from(parent.context).inflate(R.layout.basicprofile_view_card, parent, false)
        }
        view = v
        return MyViewHolder(v)
    }

    override fun getItemCount(): Int {
        return if (Constants.Profile_View == "Bp") {
            firmProfileModelArrayList.size
        } else {
            membersviewlist.size
        }
    }

    override fun onBindViewHolder(holder: MyViewHolder, @SuppressLint("RecyclerView") position: Int) {
        if (Constants.Profile_View != "Bp") {
            val model = membersviewlist[position]
            holder.tv_name?.text = model.first_name
            holder.tv_designation_name?.text = model.designation
            holder.tv_specialist_name?.text = model.practice
            holder.tv_email?.text = model.email
            holder.tv_phonenumber?.text = model.phone

            holder.action_list_card?.visibility = View.GONE

            toggleVisibility(
                holder.tv_phonenumber,
                !TextUtils.isEmpty(model.phone) && "null" != model.phone,
                model.phone ?: ""
            )
            toggleVisibility(
                holder.tv_specialist_name,
                !TextUtils.isEmpty(model.practice) && "null" != model.practice,
                model.practice ?: ""
            )

            val actions = ArrayList<ActionModel>()
            actions.add(ActionModel("Edit"))
            actions.add(ActionModel("Delete"))

            val isExpanded = (position == expandedPosition)

            holder.iv_action_menu?.setOnClickListener(null)
            holder.action_list?.onItemClickListener = null

            if (isExpanded && holder.action_list != null) {
                val adapter = CommonSpinnerAdapter(context as Activity, actions)
                holder.action_list?.adapter = adapter
                holder.action_list?.post { AndroidUtils.setDynamicHeight(holder.action_list) }
                holder.action_list_card?.visibility = View.VISIBLE
            } else {
                holder.action_list?.adapter = null
                holder.action_list_card?.visibility = View.GONE
            }

            holder.iv_action_menu?.setOnClickListener {
                val cur = holder.adapterPosition
                if (cur == RecyclerView.NO_POSITION) return@setOnClickListener
                val prev = expandedPosition
                expandedPosition = if (expandedPosition == cur) -1 else cur
                if (prev != -1 && prev != cur) safeNotify(prev)
                safeNotify(cur)
            }

            holder.action_list?.onItemClickListener = AdapterView.OnItemClickListener { parent, v, pos, id ->
                val cur = holder.adapterPosition
                if (cur == RecyclerView.NO_POSITION) return@OnItemClickListener
                val name = actions[pos].name
                expandedPosition = -1
                safeNotify(cur)
                Handler(Looper.getMainLooper()).post { dispatchAction(name, model) }
            }
            return
        }

        val fp = isFirmProfile()

        if (fp && Constants.PROFILE_EDIT_TAB == "availability") {
            Constants.PROFILE_EDIT_TAB = "practice_details"
        }
        if (!fp && Constants.lastProfileContext != null && Constants.lastProfileContext == "firm") {
            Constants.PROFILE_EDIT_TAB = "practice_details"
        } else if (fp && Constants.lastProfileContext != null && Constants.lastProfileContext == "my") {
            Constants.PROFILE_EDIT_TAB = "practice_details"
        }
        Constants.lastProfileContext = if (fp) "firm" else "my"

        val isGHTeamMember = Constants.ROLE.equals("GH", ignoreCase = true)
                || Constants.ROLE.equals("TM", ignoreCase = true)
        val isAMMorSU = Constants.ROLE.equals("AAM", ignoreCase = true)

        // ── Profile image ───────────────────────────────────────────────
        var imageToLoad = ""
        if (fp) {
            val prof0 = if (firmProfileModelArrayList[position].data != null) {
                firmProfileModelArrayList[position].data?.profile
            } else {
                null
            }
            val url = if (prof0?.firm?.profile_pic_url != null) prof0.firm?.profile_pic_url else ""
            if (!url.isNullOrEmpty()) {
                AppImageCache.preload(context, url)
                AndroidUtils.loadProfileImage(context, url, holder.iv_profile, holder.person_icon)
                holder.iv_profile?.let { animateScalePulse(it) }
                holder.iv_delete_circle?.visibility = View.VISIBLE
            } else {
                holder.iv_delete_circle?.visibility = View.GONE
            }
        } else {
            imageToLoad = Constants.firm_image ?: ""
        }

        if (!fp) {
            AndroidUtils.loadProfileImage(context, imageToLoad, holder.iv_profile, holder.person_icon)
            if (!TextUtils.isEmpty(imageToLoad)) {
                holder.iv_delete_circle?.visibility = View.VISIBLE
            }
        }

        // ── Section header ──────────────────────────────────────────────
        if (fp) {
            holder.tv_BasicProfile?.text = "Firm Information"
        } else if (isAMMorSU) {
            holder.tv_BasicProfile?.text = "Firm Profile"
        } else {
            holder.tv_BasicProfile?.text = context.getString(R.string.profile_inform)
        }
        holder.tv_BasicProfile?.textSize = 20f

        // ── Model extraction ────────────────────────────────────────────
        val model = firmProfileModelArrayList[position]
        val data = model.data ?: return
        val profile = data.profile ?: return
        val firm = profile.firm ?: return
        val subscription = profile.subscription

        // ── Debug logging ────────────────────────────────────────────────
        val pc1 = profile.profile_completion
        val ns1 = pc1?.next_step
        if (ns1 != null) {
            Log.d("REDIRECT_TAB_DEBUG", "section=" + ns1.section)
            Log.d("REDIRECT_TAB_DEBUG", "redirect_tab=" + ns1.redirect_tab)
            Log.d("REDIRECT_TAB_DEBUG", "priority=" + ns1.priority)
            Log.d("REDIRECT_TAB_DEBUG", "missing_fields=" + ns1.missing_fields)
        }

        holder.FirmName?.setText(R.string.fn_)

        // ── Profile completion percentage ────────────────────────────────
        val percentage = if (fp) {
            firm.profile_completion?.completion_percentage ?: 0
        } else {
            profile.profile_completion?.completion_percentage ?: 0
        }
        holder.tvPercentage?.text = "$percentage%"
        if (fp) {
            holder.tvMessage?.text = if (percentage > 50) {
                "Please complete your Firm profile to stand out and get discovered by more clients"
            } else {
                "Firm Profile cannot go live if incomplete"
            }
        } else {
            holder.tvMessage?.text = if (percentage > 50) {
                "Please complete your profile to stand out and get discovered by more clients"
            } else {
                "Profile cannot go live if incomplete"
            }
        }

        // ── Section visibility rules ─────────────────────────────────────
        if (isGHTeamMember) {
            holder.ll_subcription?.visibility = View.GONE
            holder.ll_basicprofile?.visibility = View.VISIBLE
            holder.ll_additional_inform?.visibility = View.VISIBLE
            if (percentage == 100) {
                profileCompletionBanner?.visibility = View.GONE
            } else {
                profileCompletionBanner?.visibility = View.VISIBLE
                updateCompletionBanner(holder, model)
            }
        } else if (Constants.issubscription) {
            holder.ll_basicprofile?.visibility = View.GONE
            holder.ll_subcription?.visibility = View.VISIBLE
            profileCompletionBanner?.visibility = View.GONE
            holder.ll_additional_inform?.visibility = View.GONE
        } else {
            holder.ll_basicprofile?.visibility = View.VISIBLE
            holder.ll_subcription?.visibility = View.GONE
            profileCompletionBanner?.visibility = if (percentage == 100) View.GONE else View.VISIBLE
            if (percentage != 100) {
                updateCompletionBanner(holder, model)
            }
            holder.ll_additional_inform?.visibility = View.VISIBLE
        }

        // ── Firm name visibility ─────────────────────────────────────────
        if (!fp && "solo".equals(Constants.CATEGORY, ignoreCase = true)) {
            holder.FirmName?.visibility = View.GONE
            holder.tv_FirmName?.visibility = View.GONE
        } else {
            holder.FirmName?.visibility = View.VISIBLE
            holder.tv_FirmName?.visibility = View.VISIBLE
            holder.tv_FirmName?.text = firm.fullname
        }

        holder.Country?.setText(R.string.country)
        var countryVal = firm.address?.country
        if (TextUtils.isEmpty(countryVal) || "null" == countryVal) {
            countryVal = "India"
        }
        holder.tv_Country?.text = countryVal

        holder.Email?.setText(R.string.email)
        if (holder.tv_Email != null) {
            if (isGHTeamMember || "solo" == Constants.CATEGORY) {
                holder.tv_Email?.text = profile.email
            } else {
                if (fp) {
                    holder.tv_Email?.text = firm.email
                } else {
                    holder.tv_Email?.text = profile.email
                }
            }
        }

        // ── Contact name label / value ────────────────────────────────────
        if (isGHTeamMember || "solo".equals(Constants.CATEGORY, ignoreCase = true)) {
            holder.ContactName?.setText(R.string.name)
            holder.tv_ContactName?.text = profile.name
        } else {
            holder.ContactName?.setText(R.string._contact_name)
            if (fp) {
                holder.tv_ContactName?.text = firm.contact_person
            } else {
                holder.tv_ContactName?.text = profile.name
            }
        }

        // ── Write fresh API values into Constants ────────────────────────
        val freshName = profile.name
        val freshFirmName = holder.tv_FirmName?.text?.toString()?.trim() ?: ""
        if (!TextUtils.isEmpty(freshName)) {
            Constants.NAME = freshName
        }
        if (!TextUtils.isEmpty(freshFirmName)) {
            Constants.FIRM_NAME = freshFirmName
        }

        // ── Set person_icon initial ──────────────────────────────────────
        if (holder.person_icon != null) {
            val initial = if (Constants.isMyProfileClicked && !TextUtils.isEmpty(Constants.NAME)) {
                Constants.NAME!!.substring(0, 1).uppercase(Locale.getDefault())
            } else if (!TextUtils.isEmpty(Constants.FIRM_NAME)) {
                Constants.FIRM_NAME!!.substring(0, 1).uppercase(Locale.getDefault())
            } else {
                "?"
            }
            holder.person_icon?.text = initial
        }

        // ── Push updated initial to MainActivity header ───────────────────
        if (activity is MainActivity) {
            (activity as MainActivity).updateProfileInitial()
        }

        try {
            Constants.jsonObject_dashboard?.put("name", Constants.NAME)
            Constants.jsonObject_dashboard?.put("firm_name", Constants.FIRM_NAME)
        } catch (e: Exception) {
            e.fillInStackTrace()
        }

        // ── Phone ────────────────────────────────────────────────────────
        holder.ContactPhone?.setText(R.string.phone_no)
        if (fp) {
            holder.tv_ContactPhone?.text = firm.contact_phone ?: ""
        } else {
            holder.tv_ContactPhone?.text = profile.mobile
        }

        // ── Website ──────────────────────────────────────────────────────
        holder.Website?.setText(R.string._website)
        holder.tv_Website?.text = firm.website

        // ── DOB / Gender / Nationality ───────────────────────────────────
        holder.dob?.setText(R.string.dob)
        holder.tv_dob?.text = profile.date_of_birth
        holder.gender?.setText(R.string.gender)
        holder.tv_gender?.text = profile.gender
        holder.nationality?.setText(R.string.nationality)
        holder.tv_nationality?.text = profile.nationality

        // ── Bio label + value ─────────────────────────────────────────────
        if (holder.bio != null) {
            holder.bio?.text = if (fp || isAMMorSU) {
                "About the Firm"
            } else {
                context.getString(R.string.bio)
            }
        }
        val bioText = if (fp || isAMMorSU) {
            firm.firm_description
        } else {
            profile.bio_description
        }
        ReadMoreUtils.setReadMore(holder.tv_bio, bioText, 2)

        // ── Address labels ────────────────────────────────────────────────
        holder.tv_Address?.setText(R.string.register_address)
        holder.tv_Address?.textSize = 20f
        holder.Building?.setText(R.string.building_)
        holder.Street?.setText(R.string.street_)
        holder.City?.setText(R.string.city_)
        holder.State?.setText(R.string.state_)
        holder.Ad_Country?.setText(R.string.country_)
        holder.Zip?.setText(R.string.zip_)
        holder.mtv_Address?.setText(R.string.mailing_address)
        holder.mtv_Address?.textSize = 20f
        holder.mBuilding?.setText(R.string.building_)
        holder.mStreet?.setText(R.string.street_)
        holder.mCity?.setText(R.string.city_)
        holder.mState?.setText(R.string.state_)
        holder.mAd_Country?.setText(R.string.country_)
        holder.mZip?.setText(R.string.zip_)

        // ── Tab content ───────────────────────────────────────────────────
        holder.tabLayout?.clearOnTabSelectedListeners()
        val currentTab = Constants.PROFILE_EDIT_TAB
        val isAMMorSUInner = Constants.ROLE.equals("AAM", ignoreCase = true)

        when (currentTab) {
            "practice_details" -> showPracticeDetails(holder, holder.adapterPosition)
            "courts_cases" -> {
                if (!fp && !isAMMorSUInner) {
                    createCourtsAndCasesView(holder, model)
                } else {
                    showPracticeDetails(holder, holder.adapterPosition)
                }
            }
            "education_awards" -> {
                if (fp || isAMMorSUInner) {
                    createAwardsOnlyView(holder, model)
                } else {
                    createEducationsAwardsView(holder, model)
                }
            }
            "availability" -> {
                if (!fp && !isAMMorSUInner) {
                    createAvailabilityView(holder, model)
                } else {
                    showPracticeDetails(holder, holder.adapterPosition)
                }
            }
            else -> showPracticeDetails(holder, holder.adapterPosition)
        }

        val tabIndexToSelect = when (currentTab) {
            "courts_cases" -> if (fp || isAMMorSUInner) 0 else 1
            "education_awards" -> if (fp || isAMMorSUInner) 1 else 2
            "availability" -> if (fp || isAMMorSUInner) 0 else 3
            else -> 0
        }
        val tabToSelect = holder.tabLayout?.getTabAt(tabIndexToSelect)
        if (tabToSelect != null && !tabToSelect.isSelected) {
            tabToSelect.select()
        }

        // ── Tab selected listener ─────────────────────────────────────────
        holder.tabLayout?.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab) {
                holder.ll_viewscreen?.removeAllViews()
                val fpInner = isFirmProfile()
                val isAMMorSUBind = Constants.ROLE.equals("AAM", ignoreCase = true)
                when (tab.position) {
                    0 -> {
                        Constants.PROFILE_EDIT_TAB = "practice_details"
                        showPracticeDetails(holder, holder.adapterPosition)
                    }
                    1 -> {
                        if (!fpInner && !isAMMorSUBind) {
                            Constants.PROFILE_EDIT_TAB = "courts_cases"
                            createCourtsAndCasesView(holder, model)
                        } else {
                            Constants.PROFILE_EDIT_TAB = "education_awards"
                            if (fpInner || isAMMorSUBind) {
                                createAwardsOnlyView(holder, model)
                            } else {
                                createEducationsAwardsView(holder, model)
                            }
                        }
                    }
                    2 -> {
                        if (!fpInner && !isAMMorSUBind) {
                            Constants.PROFILE_EDIT_TAB = "education_awards"
                            if (fpInner || isAMMorSUBind) {
                                createAwardsOnlyView(holder, model)
                            } else {
                                createEducationsAwardsView(holder, model)
                            }
                        }
                    }
                    3 -> {
                        if (!fpInner && !isAMMorSUBind) {
                            Constants.PROFILE_EDIT_TAB = "availability"
                            createAvailabilityView(holder, model)
                        }
                    }
                }
                holder.scrollView?.post {
                    holder.ll_viewscreen?.viewTreeObserver
                        ?.addOnGlobalLayoutListener(object : ViewTreeObserver.OnGlobalLayoutListener {
                            override fun onGlobalLayout() {
                                holder.scrollView?.smoothScrollTo(0, holder.ll_viewscreen?.top ?: 0)
                                holder.ll_viewscreen?.viewTreeObserver
                                    ?.removeOnGlobalLayoutListener(this)
                            }
                        })
                }
            }

            override fun onTabUnselected(tab: TabLayout.Tab) {}
            override fun onTabReselected(tab: TabLayout.Tab) {}
        })

        // ── Subscription section ──────────────────────────────────────────
        if (!isGHTeamMember && subscription != null) {
            if (subscription.model.equals("free", ignoreCase = true)
                || subscription.plan?.name.equals("Free Trial", ignoreCase = true)) {
                holder.ll_payment?.visibility = View.GONE
            } else {
                holder.ll_payment?.visibility = View.VISIBLE
            }
            holder.stv_subscription?.setText(R.string.subscription)
            holder.stv_subscription?.setTextColor(context.getColor(R.color.blue))
            holder.saccount_activated_on?.setText(R.string.account_activated_on)
            holder.tvaccount_activated_on?.text = subscription.activatedOn
            holder.splan_details?.setText(R.string.plan_details)
            holder.tvplan_details?.text = subscription.plan?.name
            holder.splan_validity?.setText(R.string.plan_validity)
            holder.tvplan_validity?.text = subscription.validityDays
            holder.ssubscription_start_date?.setText(R.string.subscription_start_date)
            holder.tvsubscription_start_date?.text = subscription.startDate
            holder.amount?.setText(R.string.amount_paid)
            holder.amount?.visibility = View.GONE
            holder.termsDate?.text = "T&C Accepted On"
            holder.tv_termsDate?.text = profile.accepted_t_c_date
            if (subscription.plan != null) {
                holder.tv_amount?.text = subscription.plan?.amount.toString()
            } else {
                holder.tv_amount?.text = ""
            }
            holder.smode_of_payment?.setText(R.string.mode_of_payment)
            val payMethod = if (subscription.payment != null) subscription.payment?.method else "-"
            holder.tvmode_of_payment?.text = payMethod
            holder.snext_billing_date?.setText(R.string.next_billing_date)
            holder.tvnext_billing_date?.text = if (subscription.endDate != null) subscription.endDate else "-"
        }

        // ── "Complete Now" click — precise field-level navigation ─────────
        val finalModel = model
        holder.tvCompleteNow?.setOnClickListener {
            if (!Constants.is_active) {
                AndroidUtils.showRenewalPopup(activity)
                return@setOnClickListener
            }
            navigateToExactField(finalModel)
        }

        holder.ivClose?.setOnClickListener {
            updateProfileBanner()
        }
    }

    private fun navigateToExactField(model: FirmProfileModel?) {
        if (model?.data?.profile == null) {
            Constants.TARGET_FIELD = ""
            Constants.PROFILE_EDIT_TAB = ""
            eventListener.EditProfile(true, false)
            return
        }

        val profile = model.data?.profile ?: return
        val pcx = if (isFirmProfile()) {
            profile.firm?.profile_completion
        } else {
            profile.profile_completion
        }
        val ns = pcx?.next_step

        if (ns != null) {
            Log.d("REDIRECT_TAB_DEBUG", "section=" + ns.section)
            Log.d("REDIRECT_TAB_DEBUG", "redirect_tab=" + ns.redirect_tab)
            Log.d("REDIRECT_TAB_DEBUG", "priority=" + ns.priority)
            Log.d("REDIRECT_TAB_DEBUG", "missing_fields=" + ns.missing_fields)
        }

        val missingFields = ns?.missing_fields
        if (ns != null && !missingFields.isNullOrEmpty()) {
            for (field in missingFields) {
                val dest = resolveFieldDestination(field)
                if (dest != null) {
                    Log.d("COMPLETE_NOW", "Missing field: " + field
                            + " section=" + dest.section
                            + " fieldId=" + dest.fieldId
                            + " tab=" + dest.tabIndex)
                    applyFieldDestination(dest, model)
                    return
                }
            }
        }

        val redirectTab = ns?.redirect_tab
        if (ns != null && !TextUtils.isEmpty(redirectTab)) {
            val dest = resolveDestinationFromTab(redirectTab ?: "")
            Log.d("COMPLETE_NOW", "redirect_tab=" + redirectTab + " dest=" + dest)
            applyTabLevelDestination(dest, model)
            return
        }

        handleFallbackNavigation(model)
    }

    private fun applyFieldDestination(dest: FieldDestination, model: FirmProfileModel) {
        Constants.TARGET_FIELD = dest.fieldId

        when (dest.section) {
            "basic" -> {
                Constants.PROFILE_EDIT_TAB = ""
                eventListener.EditProfile(true, false)
            }
            "pic" -> {
                Constants.TARGET_FIELD = ""
                AndroidUtils.showAlert("Please upload a profile picture.", activity)
            }
            "additional" -> {
                Constants.PROFILE_EDIT_TAB = "practice_details"
                eventListener.EditProfile(false, true)
            }
            "courts_cases" -> {
                Constants.PROFILE_EDIT_TAB = "courts_cases"
                eventListener.EditProfile(false, true)
            }
            "education" -> {
                Constants.PROFILE_EDIT_TAB = "education_awards"
                eventListener.EditProfile(false, true)
            }
            "availability" -> {
                val isFP = isFirmProfile()
                val isAMM = Constants.ROLE.equals("AAM", ignoreCase = true)
                if (!isFP && !isAMM) {
                    Constants.PROFILE_EDIT_TAB = "availability"
                    eventListener.EditProfile(false, true)
                } else {
                    Constants.PROFILE_EDIT_TAB = "practice_details"
                    eventListener.EditProfile(false, true)
                }
            }
            else -> {
                Constants.TARGET_FIELD = ""
                Constants.PROFILE_EDIT_TAB = ""
                eventListener.EditProfile(true, false)
            }
        }
    }

    private fun applyTabLevelDestination(destination: String, model: FirmProfileModel) {
        Constants.TARGET_FIELD = ""
        when (destination) {
            "basic" -> {
                Constants.PROFILE_EDIT_TAB = ""
                eventListener.EditProfile(true, false)
            }
            "pic" -> {
                AndroidUtils.showAlert("Please upload a profile picture.", activity)
            }
            "additional" -> {
                Constants.PROFILE_EDIT_TAB = "practice_details"
                eventListener.EditProfile(false, true)
            }
            "courts_cases" -> {
                Constants.PROFILE_EDIT_TAB = "courts_cases"
                eventListener.EditProfile(false, true)
            }
            "education" -> {
                Constants.PROFILE_EDIT_TAB = "education_awards"
                eventListener.EditProfile(false, true)
            }
            "availability" -> {
                val isFP = isFirmProfile()
                val isAMM = Constants.ROLE.equals("AAM", ignoreCase = true)
                if (!isFP && !isAMM) {
                    Constants.PROFILE_EDIT_TAB = "availability"
                    eventListener.EditProfile(false, true)
                } else {
                    Constants.PROFILE_EDIT_TAB = "practice_details"
                    eventListener.EditProfile(false, true)
                }
            }
            else -> {
                handleFallbackNavigation(model)
            }
        }
    }

    private fun resolveDestinationFromTab(redirectTab: String): String {
        return when (redirectTab.lowercase(Locale.getDefault()).trim()) {
            "profile", "basic_profile", "firm_profile" -> "basic"
            "profile_pic", "logo" -> "pic"
            "practice", "practice_details", "additional_info", "additional", "practice_optional" -> "additional"
            "courts", "court_enrollments", "courts_cases" -> "courts_cases"
            "education", "education_awards", "awards" -> "education"
            "availability" -> "availability"
            else -> "additional"
        }
    }

    private fun toggleVisibility(tv: TextView?, show: Boolean, text: String) {
        if (tv != null) {
            if (show) {
                tv.visibility = View.VISIBLE
                tv.text = text
            } else {
                tv.visibility = View.GONE
            }
        }
    }

    private fun createAwardsOnlyView(holder: MyViewHolder, firmProfileModel: FirmProfileModel?): View {
        val inflater = LayoutInflater.from(holder.itemView.context)
        val detailView = inflater.inflate(R.layout.education_awards_layout, holder.ll_viewscreen, false)

        val ids = intArrayOf(R.id.ll_education, R.id.ll_certification, R.id.tv_title_education, R.id.tv_title_certification)
        for (id in ids) {
            val v = detailView.findViewById<View>(id)
            v?.visibility = View.GONE
        }

        val llAwards = detailView.findViewById<LinearLayout>(R.id.ll_awards)
        val tvAwardsTitle = detailView.findViewById<TextView>(R.id.tv_title_awards)
        tvAwardsTitle?.text = "Awards & Recognition"
        tvAwardsTitle?.setTextColor(context.getColor(R.color.grey_light))

        val profile = firmProfileModel?.data?.profile

        if (profile == null) {
            holder.ll_viewscreen?.visibility = View.GONE
            return detailView
        }

        val fp = isFirmProfile()
        val awards = if (fp) {
            profile.firm?.awards
        } else {
            profile.awards
        }
        populateAwards(inflater, llAwards, awards)

        holder.ll_viewscreen?.removeAllViews()
        holder.ll_viewscreen?.addView(detailView)
        holder.ll_viewscreen?.visibility = View.VISIBLE
        return detailView
    }

    private fun addCourtRow(inflater: LayoutInflater, parent: LinearLayout, ce: FirmProfileModel.CourtEnrollment) {
        val row = inflater.inflate(R.layout.ll_court_layout, parent, false)
        row.findViewById<View>(R.id.tv_placeholder)?.visibility = View.GONE
        row.findViewById<View>(R.id.ll_data_container)?.visibility = View.VISIBLE
        val tvCourtType = row.findViewById<TextView>(R.id.tv_courtType)
        val tvCourtState = row.findViewById<TextView>(R.id.tv_courtState)
        val tvCourtCity = row.findViewById<TextView>(R.id.tv_courtCity)
        val tvCourtName = row.findViewById<TextView>(R.id.tv_courtName)
        var courtType1 = (ce.court_name ?: "")
            .replace("_", " ")
            .lowercase(Locale.getDefault())

        if (courtType1.isNotEmpty()) {
            val words = courtType1.split(" ")
            val result = StringBuilder()
            for (word in words) {
                if (word.isNotEmpty()) {
                    result.append(word.substring(0, 1).uppercase(Locale.getDefault()))
                        .append(word.substring(1))
                        .append(" ")
                }
            }
            courtType1 = result.toString().trim()
        }

        tvCourtType?.text = courtType1
        tvCourtState?.text = ce.state
        tvCourtCity?.text = ce.city
        parent.addView(row)
    }

    private fun loadSelectedItem(courtType: String): String {
        return when (courtType) {
            "district_court" -> "District & Other Lower Courts"
            "high_court" -> "High Court"
            else -> "Supreme Court"
        }
    }

    @SuppressLint("SetTextI18n")
    private fun showPracticeDetails(holder: MyViewHolder, position: Int) {
        holder.ll_viewscreen?.removeAllViews()

        val inflater = LayoutInflater.from(holder.itemView.context)
        val detailView = inflater.inflate(R.layout.practicedetails_view, holder.ll_viewscreen, false)

        val m = firmProfileModelArrayList.getOrNull(position)
        if (m?.data?.profile?.firm == null) {
            holder.ll_viewscreen?.addView(detailView)
            return
        }

        val fp = isFirmProfile()
        val isAMMorSU = Constants.ROLE.equals("AAM", ignoreCase = true)

        val firm = m.data?.profile?.firm ?: return
        val profile = m.data?.profile ?: return

        // ── Professional Information section header ───────────────────────
        val tvProfHeader = detailView.findViewById<TextView>(R.id.tv_professional_info_header)
        if (tvProfHeader != null) {
            tvProfHeader.text = "Professional Information"
            tvProfHeader.visibility = View.VISIBLE
        }

        // ── Bar Council ID vs Registration ID ────────────────────────────
        val firmNameLabel = detailView.findViewById<TextView>(R.id.FirmName)
        firmNameLabel?.text = if (fp) "Registration Id" else context.getString(R.string.bar_id)
        firmNameLabel?.setTextColor(context.getColor(R.color.grey_light))
        val regOrBarId = if (fp) {
            firm.reg_id ?: ""
        } else {
            profile.bar_council_id ?: ""
        }
        detailView.findViewById<TextView>(R.id.tv_FirmName)?.text = regOrBarId

        // ── Year of Experience vs Year of Incorporation ───────────────────
        val tvYearLabel = detailView.findViewById<TextView>(R.id.Country)
        tvYearLabel?.text = if (fp) "Year of Incorporation" else context.getString(R.string.year_exp)
        tvYearLabel?.setTextColor(context.getColor(R.color.grey_light))
        val yearValue = if (fp) {
            firm.years_of_incorporation.toString()
        } else {
            profile.years_of_experience.toString()
        }
        detailView.findViewById<TextView>(R.id.tv_Country)?.text = yearValue

        // ── Consultation Fee (My Profile only) ────────────────────────────
        val tvConFeeLabel = detailView.findViewById<TextView>(R.id.con_fee)
        val tvConFeeValue = detailView.findViewById<TextView>(R.id.tv_con_fee)
        val conFeeRow = detailView.findViewById<View>(R.id.ll_confee_row)
        conFeeRow?.visibility = if (fp) View.GONE else View.VISIBLE
        if (tvConFeeLabel != null) {
            tvConFeeLabel.visibility = if (fp || isAMMorSU) View.GONE else View.VISIBLE
            if (!fp && !isAMMorSU) {
                tvConFeeLabel.setText(R.string.con_fees)
                tvConFeeLabel.setTextColor(context.getColor(R.color.grey_light))
            }
        }
        if (tvConFeeValue != null) {
            tvConFeeValue.visibility = if (fp || isAMMorSU) View.GONE else View.VISIBLE
            if (!fp && !isAMMorSU) {
                val cf = profile.consultation_fee
                val sym = cf?.symbol ?: "₹"
                val fee = cf?.amount ?: ""
                tvConFeeValue.text = "$sym $fee"
            }
        }

        // ── Billing Currency ──────────────────────────────────────────────
        val tvCurrencyLabel = detailView.findViewById<TextView>(R.id.billing_currency1)
        val tvCurrencyValue = detailView.findViewById<TextView>(R.id.tv_billing_currency)
        if (tvCurrencyLabel != null) {
            tvCurrencyLabel.setText(R.string.billing_currency)
            tvCurrencyLabel.setTextColor(context.getColor(R.color.grey_light))
        }
        if (tvCurrencyValue != null) {
            tvCurrencyValue.text = firm.billing_currency ?: ""
        }

        // ── Practice Areas ────────────────────────────────────────────────
        val tvPracticeLabel = detailView.findViewById<TextView>(R.id.Email)
        tvPracticeLabel?.setText(R.string.practice_area)
        tvPracticeLabel?.setTextColor(context.getColor(R.color.grey_light))

        val rvPractice = detailView.findViewById<RecyclerView>(R.id.rvPracticeAreas)
        val tvNoPractice = detailView.findViewById<TextView>(R.id.tvNoPractice)
        val practiceArray = if (fp) firm.practice_areas else profile.practice_areas

        if (practiceArray == null || practiceArray.length() == 0) {
            rvPractice?.visibility = View.GONE
            tvNoPractice?.visibility = View.VISIBLE
        } else {
            tvNoPractice?.visibility = View.GONE
            rvPractice?.visibility = View.VISIBLE
            rvPractice?.layoutManager = GridLayoutManager(context, 2)
            rvPractice?.adapter = TagAdapter(practiceArray, TagAdapter.Style.PRACTICE)
        }

        // ── Services Offered ──────────────────────────────────────────────
        val tvServiceLabel = detailView.findViewById<TextView>(R.id.ContactPhone)
        tvServiceLabel?.setText(R.string.service_offered)
        tvServiceLabel?.setTextColor(context.getColor(R.color.grey_light))

        val rvServices = detailView.findViewById<RecyclerView>(R.id.rvServices)
        val tvNoService = detailView.findViewById<TextView>(R.id.tvnoServices)
        val servicesArray = if (fp) firm.services_offered else profile.services_offered

        if (servicesArray == null || servicesArray.length() == 0) {
            rvServices?.visibility = View.GONE
            tvNoService?.visibility = View.VISIBLE
        } else {
            tvNoService?.visibility = View.GONE
            rvServices?.visibility = View.VISIBLE
            rvServices?.layoutManager = GridLayoutManager(context, 2)
            rvServices?.adapter = TagAdapter(servicesArray, TagAdapter.Style.SERVICE)
        }

        // ── Languages Spoken — hidden for Firm Profile / AAM ─────────────
        val tvLangLabel = detailView.findViewById<TextView>(R.id.ContactName)
        val tvLangValue = detailView.findViewById<TextView>(R.id.tv_ContactName)
        val rvLang = detailView.findViewById<RecyclerView>(R.id.rvLanguages)
        val tvNoLang = detailView.findViewById<TextView>(R.id.tvnoLanguage)

        if (fp || isAMMorSU) {
            tvLangLabel?.visibility = View.GONE
            tvLangValue?.visibility = View.GONE
            rvLang?.visibility = View.GONE
            tvNoLang?.visibility = View.GONE
        } else {
            if (tvLangLabel != null) {
                tvLangLabel.setText(R.string.language)
                tvLangLabel.setTextColor(context.getColor(R.color.grey_light))
            }
            tvLangValue?.visibility = View.GONE
            val langArray = profile.languages_spoken
            if (langArray == null || langArray.length() == 0) {
                rvLang?.visibility = View.GONE
                tvNoLang?.visibility = View.VISIBLE
            } else {
                tvNoLang?.visibility = View.GONE
                if (rvLang != null) {
                    rvLang.visibility = View.VISIBLE
                    rvLang.layoutManager = GridLayoutManager(context, 2)
                    rvLang.adapter = TagAdapter(langArray, TagAdapter.Style.LANGUAGE)
                }
            }
        }

        // ── Cases Handled — always hidden here (moved to Courts & Cases tab) ─
        val tvCasesLabel = detailView.findViewById<View>(R.id.cases_handled)
        val rvCases = detailView.findViewById<RecyclerView>(R.id.rvCasesHandled)
        val tvNoCases = detailView.findViewById<TextView>(R.id.tvnoCases)
        tvCasesLabel?.visibility = View.GONE
        rvCases?.visibility = View.GONE
        tvNoCases?.visibility = View.GONE

        // ── Address Information section header ────────────────────────────
        val tvAddrHeader = detailView.findViewById<TextView>(R.id.tv_address_info_header)
        if (tvAddrHeader != null) {
            tvAddrHeader.text = "Addresses"
            tvAddrHeader.visibility = View.VISIBLE
        }

        // ── Registered Address ────────────────────────────────────────────
        val tvRegAddrLabel = detailView.findViewById<TextView>(R.id.tv_registered_address_label)
        val tvRegAddrValue = detailView.findViewById<TextView>(R.id.tv_registered_address_value)
        if (tvRegAddrLabel != null) {
            tvRegAddrLabel.setText(R.string.register_address)
            tvRegAddrLabel.setTextColor(context.getColor(R.color.grey_light))
        }
        if (tvRegAddrValue != null) {
            tvRegAddrValue.text = getFullAddress(firm.address)
        }

        // ── Mailing Address ───────────────────────────────────────────────
        val tvMailAddrLabel = detailView.findViewById<TextView>(R.id.tv_mailing_address_label)
        val tvMailAddrValue = detailView.findViewById<TextView>(R.id.tv_mailing_address_value)
        if (tvMailAddrLabel != null) {
            tvMailAddrLabel.setText(R.string.mailing_address)
            tvMailAddrLabel.setTextColor(context.getColor(R.color.grey_light))
        }
        if (tvMailAddrValue != null) {
            tvMailAddrValue.text = getFullAddress(firm.correspondence_address)
        }

        // ── Court section — always hidden in Practice Details tab ─────────
        val llCourtSection = detailView.findViewById<View>(R.id.ll_court_section)
        llCourtSection?.visibility = View.GONE

        holder.ll_viewscreen?.addView(detailView)
        holder.ll_viewscreen?.visibility = View.VISIBLE
    }

    private fun createEducationsAwardsView(holder: MyViewHolder, firmProfileModel: FirmProfileModel?): View {
        val inflater = LayoutInflater.from(holder.itemView.context)
        val detailView = inflater.inflate(R.layout.education_awards_layout, holder.ll_viewscreen, false)

        val llEducation = detailView.findViewById<LinearLayout>(R.id.ll_education)
        val llCertification = detailView.findViewById<LinearLayout>(R.id.ll_certification)
        val llAwards = detailView.findViewById<LinearLayout>(R.id.ll_awards)
        val tvEdu = detailView.findViewById<TextView>(R.id.tv_title_education)
        val tvCert = detailView.findViewById<TextView>(R.id.tv_title_certification)
        val tvAw = detailView.findViewById<TextView>(R.id.tv_title_awards)

        tvEdu?.setText(R.string.educational_qualifications)
        tvCert?.setText(R.string.certifications)
        tvAw?.setText(R.string.awards_recoginations)
        tvEdu?.setTextColor(context.getColor(R.color.grey_light))
        tvCert?.setTextColor(context.getColor(R.color.grey_light))
        tvAw?.setTextColor(context.getColor(R.color.grey_light))

        val profile = firmProfileModel?.data?.profile

        if (profile == null) {
            holder.ll_viewscreen?.visibility = View.GONE
            return detailView
        }

        populateEducation(inflater, llEducation, profile.education)
        populateCertification(inflater, llCertification, profile.certifications)
        populateAwards(inflater, llAwards, profile.awards)

        holder.ll_viewscreen?.removeAllViews()
        holder.ll_viewscreen?.addView(detailView)
        holder.ll_viewscreen?.visibility = View.VISIBLE
        return detailView
    }

    private fun createAvailabilityView(holder: MyViewHolder, firmProfileModel: FirmProfileModel?): View {
        val inflater = LayoutInflater.from(holder.itemView.context)
        val detailView = inflater.inflate(R.layout.book_set_availability, holder.ll_viewscreen, false)

        val btnSlotDuration = detailView.findViewById<Button>(R.id.btn_slot_duration)
        val tvMinBookingHours = detailView.findViewById<TextView>(R.id.tv_min_booking_hours)
        val slotHours = detailView.findViewById<LinearLayout>(R.id.slot_hours)
        val ivInfo = detailView.findViewById<AppCompatImageView>(R.id.iv_info)
        val ivInfo1 = detailView.findViewById<ImageView>(R.id.iv_info1)
        val tvInfo = detailView.findViewById<TextView>(R.id.tvInfo)

        slotHours?.visibility = View.GONE
        ivInfo?.visibility = View.GONE
        tvInfo?.setText(R.string.availability_info_txt)

        ivInfo?.setOnClickListener { v ->
            showPopup(v, "Appointments require a minimum advance booking for preparation.", Color.WHITE)
        }
        ivInfo1?.setOnClickListener { v ->
            showPopup(
                v,
                "How it works: Set your available hours. We will automatically create 30-minute booking slots. Need a break? Click Exclude icon to block specific times (e.g., 12:00-01.00 PM).",
                context.getColor(R.color.blue)
            )
        }

        val availability = firmProfileModel?.data?.profile?.availability
        if (availability != null) {
            btnSlotDuration?.text = "${availability.slot_duration} Minutes"
            val advH = availability.advance_booking_window_hours
            tvMinBookingHours?.text = "$advH" + (if (advH == 1) " Hour" else " Hours")
            populateWeeklySchedule(availability, detailView)
        }

        holder.ll_viewscreen?.addView(detailView)
        holder.ll_viewscreen?.visibility = View.VISIBLE
        return detailView
    }

    private fun createCourtsAndCasesView(holder: MyViewHolder, firmProfileModel: FirmProfileModel?): View {
        val inflater = LayoutInflater.from(holder.itemView.context)

        holder.ll_viewscreen?.removeAllViews()

        val dp = context.resources.displayMetrics.density.toInt()

        val container = LinearLayout(context)
        container.orientation = LinearLayout.VERTICAL
        container.setPadding(16 * dp, 16 * dp, 16 * dp, 16 * dp)
        container.layoutParams = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        )

        val profile = firmProfileModel?.data?.profile

        // ── Court Enrollments section header ──────────────────────────────
        val tvCourtHeader = TextView(context)
        tvCourtHeader.text = "Court Practice Details"
        tvCourtHeader.setTextColor(context.getColor(R.color.grey_light))
        tvCourtHeader.textSize = 16f
        tvCourtHeader.setPadding(0, 0, 0, 8 * dp)
        container.addView(tvCourtHeader)

        // ── Practicing At label ───────────────────────────────────────────
        val tvPracticeAt = TextView(context)
        tvPracticeAt.text = "Practicing At"
        tvPracticeAt.setTextColor(context.getColor(R.color.grey_light))
        tvPracticeAt.textSize = 14f
        tvPracticeAt.setPadding(0, 8 * dp, 0, 8 * dp)
        container.addView(tvPracticeAt)

        // ── Court enrollment rows ─────────────────────────────────────────
        val llCourts = LinearLayout(context)
        llCourts.orientation = LinearLayout.VERTICAL
        llCourts.layoutParams = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        )
        container.addView(llCourts)

        val courts = profile?.court_enrollments
        if (courts.isNullOrEmpty()) {
            addPlaceholder(inflater, llCourts, "No court enrollments added")
        } else {
            for (ce in courts) {
                addCourtRow(inflater, llCourts, ce)
            }
        }

        // ── Divider ───────────────────────────────────────────────────────
        val divider = View(context)
        val divLp = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT, 1 * dp
        )
        divLp.setMargins(0, 16 * dp, 0, 16 * dp)
        divider.layoutParams = divLp
        divider.setBackgroundColor(-0x333334) // 0xFFCCCCCC
        container.addView(divider)

        // ── Cases Handled section header ──────────────────────────────────
        val tvCasesHeader = TextView(context)
        tvCasesHeader.text = "Types of Cases Handled"
        tvCasesHeader.setTextColor(context.getColor(R.color.grey_light))
        tvCasesHeader.textSize = 16f
        tvCasesHeader.setPadding(0, 0, 0, 8 * dp)
        container.addView(tvCasesHeader)

        // ── Cases handled chips ───────────────────────────────────────────
        val llCases = LinearLayout(context)
        llCases.orientation = LinearLayout.VERTICAL
        llCases.layoutParams = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        )
        container.addView(llCases)

        val casesList = profile?.cases_handled
        if (casesList.isNullOrEmpty()) {
            addPlaceholder(inflater, llCases, "No cases handled added")
        } else {
            val rvCases = RecyclerView(context)
            val rvLp = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
            rvCases.layoutParams = rvLp
            rvCases.isNestedScrollingEnabled = false
            rvCases.layoutManager = GridLayoutManager(context, 2)
            val casesArray = JSONArray()
            for (c in casesList) {
                casesArray.put(c)
            }
            rvCases.adapter = TagAdapter(casesArray, TagAdapter.Style.SERVICE)
            llCases.addView(rvCases)
        }

        holder.ll_viewscreen?.addView(container)
        holder.ll_viewscreen?.visibility = View.VISIBLE
        return container
    }

    private fun showPopup(anchor: View, msg: String, textColor: Int) {
        val tv = TextView(anchor.context)
        tv.text = msg
        tv.setPadding(30, 20, 30, 20)
        tv.setTextColor(textColor)
        tv.setBackgroundResource(R.drawable.info_box_bg)
        if (textColor != Color.WHITE) {
            tv.typeface = ResourcesCompat.getFont(context, R.font.gill_sans)
        }
        tv.textSize = 13f
        val popup = PopupWindow(
            tv,
            LinearLayout.LayoutParams.WRAP_CONTENT,
            LinearLayout.LayoutParams.WRAP_CONTENT,
            true
        )
        popup.elevation = 10f
        popup.showAsDropDown(anchor, 0, 10)
    }

    private fun populateEducation(i: LayoutInflater, p: LinearLayout, list: List<FirmProfileModel.Education>?) {
        p.removeAllViews()
        if (list.isNullOrEmpty()) {
            addPlaceholder(i, p, "No educational qualifications added")
            return
        }
        for (e in list) {
            addListRow(i, p, e.degree ?: "", e.university ?: "", e.passing_year.toString())
        }
    }

    private fun populateCertification(i: LayoutInflater, p: LinearLayout, list: List<FirmProfileModel.Certification>?) {
        p.removeAllViews()
        if (list.isNullOrEmpty()) {
            addPlaceholder(i, p, "No certifications added")
            return
        }
        for (c in list) {
            addListRow(i, p, c.certification_name ?: "", c.issuing_authority ?: "", c.year_of_issue.toString())
        }
    }

    private fun populateAwards(i: LayoutInflater, p: LinearLayout, list: List<FirmProfileModel.Award>?) {
        p.removeAllViews()
        if (list.isNullOrEmpty()) {
            addPlaceholder(i, p, "No awards added")
            return
        }
        for (a in list) {
            addListRow(i, p, a.award_name ?: "", a.purpose ?: "", a.year_of_award.toString())
        }
    }

    private fun addPlaceholder(inflater: LayoutInflater, parent: LinearLayout, msg: String) {
        val row = inflater.inflate(R.layout.ll_grey_list_item, parent, false)
        row.findViewById<View>(R.id.ll_data_container)?.visibility = View.GONE
        val tv = row.findViewById<TextView>(R.id.tv_placeholder)
        tv?.text = msg
        tv?.visibility = View.VISIBLE
        parent.addView(row)
    }

    private fun addListRow(inflater: LayoutInflater, parent: LinearLayout, title: String, content: String, year: String?) {
        val row = inflater.inflate(R.layout.ll_grey_list_item, parent, false)
        row.findViewById<View>(R.id.tv_placeholder)?.visibility = View.GONE
        row.findViewById<View>(R.id.ll_data_container)?.visibility = View.VISIBLE
        val tvTitle = row.findViewById<TextView>(R.id.tv_title)
        val tvContent = row.findViewById<TextView>(R.id.tv_content)
        val tvYear = row.findViewById<TextView>(R.id.tv_year)
        val dot = row.findViewById<TextView>(R.id.dot)
        tvTitle?.text = title
        tvContent?.text = content
        if (year == null || year.trim().isEmpty() || "0" == year) {
            tvYear?.text = ""
            dot?.visibility = View.GONE
        } else {
            tvYear?.text = year
            dot?.visibility = View.VISIBLE
        }
        tvTitle?.setTextColor(context.getColor(R.color.Primary_new))
        tvContent?.setTextColor(context.getColor(R.color.pale_blue))
        tvYear?.setTextColor(context.getColor(R.color.pale_blue))
        parent.addView(row)
    }

    private fun populateWeeklySchedule(availability: FirmProfileModel.Availability, root: View) {
        val weeklySchedule = availability.weekly_schedule ?: return
        val ll = root.findViewById<LinearLayout>(R.id.ll_weekly_hours)
        ll?.removeAllViews()
        val inflater = LayoutInflater.from(root.context)
        val font = ResourcesCompat.getFont(root.context, R.font.gill_sans)

        for (day in weeklySchedule) {
            val work = day.work_slots
            val blocked = day.expert_slots
            val dv = inflater.inflate(R.layout.component_day_time, ll, false)

            val tvDay = dv.findViewById<TextView>(R.id.tv_day)
            val tvWork = dv.findViewById<TextView>(R.id.tv_work_time)
            val tvAvail = dv.findViewById<TextView>(R.id.tv_available_time)
            val llBlk = dv.findViewById<LinearLayout>(R.id.ll_blocked_row)
            val grid = dv.findViewById<GridLayout>(R.id.ll_blockedtime)
            val divider = dv.findViewById<View>(R.id.view)

            tvAvail?.visibility = View.GONE
            llBlk?.visibility = View.GONE
            divider?.visibility = View.GONE
            grid?.removeAllViews()

            val lbl = day.date_label
            tvDay?.text = if (!TextUtils.isEmpty(lbl)) lbl else day.day_name

            if (!work.isNullOrEmpty()) {
                val ws = work[0]
                tvWork?.text = "${convertTo12Hour(ws.start_time ?: "")} - ${convertTo12Hour(ws.end_time ?: "")}"
            } else {
                tvWork?.text = "Not available"
                tvWork?.alpha = 0.5f
            }

            if (!blocked.isNullOrEmpty()) {
                llBlk?.visibility = View.VISIBLE
                divider?.visibility = View.VISIBLE
                for (b in blocked) {
                    val chip = inflater.inflate(R.layout.grey_bg_textview, grid, false)
                    val tv = chip.findViewById<TextView>(R.id.tv_start_time)
                    tv?.text = "${convertTo12Hour(b.start_time ?: "")} – ${convertTo12Hour(b.end_time ?: "")}"
                    tv?.typeface = font
                    tv?.alpha = 0.5f
                    val p = GridLayout.LayoutParams()
                    p.width = 0
                    p.columnSpec = GridLayout.spec(GridLayout.UNDEFINED, 1f)
                    p.setMargins(12, 12, 12, 12)
                    chip.layoutParams = p
                    grid?.addView(chip)
                }
            }

            val avail = calculateAvailableHours(work, blocked)
            if (avail > 0) {
                tvAvail?.visibility = View.VISIBLE
                tvAvail?.text = formatHours(avail)
            }
            ll?.addView(dv)
        }
    }

    private fun calculateAvailableHours(
        work: List<FirmProfileModel.WorkSlot>?,
        blocked: List<FirmProfileModel.WorkSlot>?
    ): Double {
        if (work.isNullOrEmpty()) return 0.0
        var wm = 0.0
        for (w in work) {
            wm += diffMin(w.start_time ?: "", w.end_time ?: "")
        }
        var bm = 0.0
        if (!blocked.isNullOrEmpty()) {
            for (b in blocked) {
                bm += diffMin(b.start_time ?: "", b.end_time ?: "")
            }
        }
        return Math.max(0.0, (wm - bm) / 60.0)
    }

    private fun diffMin(s: String, e: String): Int {
        return try {
            val sdf = SimpleDateFormat("HH:mm", Locale.getDefault())
            ((sdf.parse(e).time - sdf.parse(s).time) / 60000).toInt()
        } catch (ex: Exception) {
            0
        }
    }

    private fun formatHours(h: Double): String {
        val hh = h.toInt()
        val mm = ((h - hh) * 60).toInt()
        return if (mm == 0) "$hh hours Available" else "${hh}h ${mm}m Available"
    }

    private fun convertTo12Hour(t: String): String {
        return try {
            val sdf12 = SimpleDateFormat("hh:mm a", Locale.getDefault())
            val sdf24 = SimpleDateFormat("HH:mm", Locale.getDefault())
            sdf12.format(sdf24.parse(t))
        } catch (e: ParseException) {
            t
        }
    }

    private fun getFullAddress(a: FirmProfileModel.Address?): String {
        if (a == null) return "-"
        val sb = StringBuilder()
        appendWithComma(sb, a.house_flat_no ?: "")
        appendWithComma(sb, a.street ?: "")
        appendWithComma(sb, a.city_town ?: "")
        appendWithComma(sb, a.state ?: "")
        appendWithComma(sb, a.country ?: "")
        appendWithComma(sb, a.zipcode ?: "")
        return if (sb.length > 0) sb.toString() else "-"
    }

    private fun appendWithComma(sb: StringBuilder, v: String) {
        if (!TextUtils.isEmpty(v)) {
            if (sb.length > 0) sb.append(", ")
            sb.append(v)
        }
    }

    private fun openGallery_new(iv: ImageView?, icon: TextView?, del: ImageView?) {
        val launcher = galleryLauncher ?: return
        currentIvProfile = iv
        currentPersonIcon = icon
        currentIvDeleteCircle = del
        try {
            val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
            intent.type = "image/*"
            launcher.launch(intent)
        } catch (e: Exception) {
            Toast.makeText(context, "Gallery error: " + e.message, Toast.LENGTH_LONG).show()
        }
    }

    open fun openCamera_new(iv: ImageView?, icon: TextView?, del: ImageView?) {
        val launcher = cameraLauncher ?: return
        currentIvProfile = iv
        currentPersonIcon = icon
        currentIvDeleteCircle = del
        try {
            val cv = ContentValues()
            cv.put(MediaStore.Images.Media.TITLE, "FirmProfile_" + System.currentTimeMillis())
            cv.put(MediaStore.Images.Media.DESCRIPTION, "Profile Image")
            val uri = activity.contentResolver.insert(
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI, cv
            )
            cameraImageUri = uri
            if (uri == null) {
                Toast.makeText(context, "Cannot access camera storage", Toast.LENGTH_SHORT).show()
                return
            }
            launcher.launch(uri)
        } catch (e: Exception) {
            Toast.makeText(context, "Camera error: " + e.message, Toast.LENGTH_LONG).show()
        }
    }

    fun openCropEditor(rawImageUri: Uri?) {
        if (rawImageUri == null) return
        practicePartnerView.openCropEditorFragment(rawImageUri)
    }

    fun handleCroppedResult(croppedFilePath: String?) {
        if (croppedFilePath == null) return
        val iv = currentIvProfile ?: return
        val icon = currentPersonIcon ?: return

        val croppedFile = File(croppedFilePath)
        if (!croppedFile.exists()) {
            Toast.makeText(context, "Cropped file not found", Toast.LENGTH_SHORT).show()
            return
        }

        val croppedUri = Uri.fromFile(croppedFile)
        iv.visibility = View.VISIBLE
        icon.visibility = View.GONE
        Glide.with(activity)
            .load(croppedUri)
            .circleCrop()
            .diskCacheStrategy(DiskCacheStrategy.NONE)
            .skipMemoryCache(true)
            .placeholder(R.drawable.ic_profile_placeholder)
            .error(R.drawable.ic_profile_placeholder)
            .into(iv)
        animateFadeIn(iv)
        profile_upload(croppedUri, iv, icon, currentIvDeleteCircle)
    }

    fun handleFileUri(uri: Uri, iv: ImageView, icon: TextView, del: ImageView?) {
        setProfileImage(uri, iv)
        profile_upload(uri, iv, icon, del)
    }

    private fun setProfileImage(uri: Uri, iv: ImageView) {
        Glide.with(activity)
            .load(uri)
            .circleCrop()
            .diskCacheStrategy(DiskCacheStrategy.NONE)
            .skipMemoryCache(true)
            .placeholder(R.drawable.ic_profile_placeholder)
            .error(R.drawable.ic_profile_placeholder)
            .into(iv)
        animateFadeIn(iv)
    }

    private fun animateFadeIn(v: View) {
        v.alpha = 0f
        v.animate().alpha(1f).setDuration(400)
            .setInterpolator(AccelerateDecelerateInterpolator()).start()
    }

    private fun animateScalePulse(v: View) {
        val x = ObjectAnimator.ofFloat(v, "scaleX", 1f, 1.1f, 1f)
        val y = ObjectAnimator.ofFloat(v, "scaleY", 1f, 1.1f, 1f)
        x.duration = 350
        y.duration = 350
        x.interpolator = AccelerateDecelerateInterpolator()
        y.interpolator = AccelerateDecelerateInterpolator()
        x.start()
        y.start()
    }

    private fun animateShake(v: View) {
        ObjectAnimator.ofFloat(v, "translationX", 0f, -20f, 20f, -15f, 15f, -10f, 10f, 0f)
            .setDuration(500)
            .start()
    }

    private fun getFileFromUri(uri: Uri?): File {
        if (uri == null) throw Exception("URI is null")
        val inputStream = activity.contentResolver.openInputStream(uri) ?: throw Exception("Cannot open InputStream")
        val f = File(activity.cacheDir, "profile_raw_${System.currentTimeMillis()}.jpg")
        val os = FileOutputStream(f)
        val buf = ByteArray(4096)
        var r: Int
        var total: Long = 0
        while (inputStream.read(buf).also { r = it } != -1) {
            os.write(buf, 0, r)
            total += r.toLong()
        }
        os.flush()
        os.close()
        inputStream.close()
        if (total == 0L) throw Exception("File is empty")
        return f
    }

    private fun fixExifRotation(bm: Bitmap, path: String): Bitmap {
        try {
            val exif = ExifInterface(path)
            val orient = exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL)
            var degrees = 0
            if (orient == ExifInterface.ORIENTATION_ROTATE_90) degrees = 90
            if (orient == ExifInterface.ORIENTATION_ROTATE_180) degrees = 180
            if (orient == ExifInterface.ORIENTATION_ROTATE_270) degrees = 270
            if (degrees != 0) {
                val m = Matrix()
                m.postRotate(degrees.toFloat())
                val rot = Bitmap.createBitmap(bm, 0, 0, bm.width, bm.height, m, true)
                bm.recycle()
                return rot
            }
        } catch (ignored: Exception) {
        }
        return bm
    }

    private fun compressImageTo2MB(imageUri: Uri): File {
        val raw = getFileFromUri(imageUri)
        val opts = BitmapFactory.Options()
        opts.inPreferredConfig = Bitmap.Config.ARGB_8888
        var bm = BitmapFactory.decodeFile(raw.absolutePath, opts) ?: throw Exception("Cannot decode image")
        bm = fixExifRotation(bm, raw.absolutePath)

        val max = 1920
        val w = bm.width
        val h = bm.height
        if (w > max || h > max) {
            val sc = if (w > h) max.toFloat() / w else max.toFloat() / h
            val sc2 = Bitmap.createScaledBitmap(bm, Math.round(w * sc), Math.round(h * sc), true)
            bm.recycle()
            bm = sc2
        }

        var quality = INITIAL_QUALITY
        var bytes: ByteArray
        while (true) {
            val baos = ByteArrayOutputStream()
            bm.compress(Bitmap.CompressFormat.JPEG, quality, baos)
            bytes = baos.toByteArray()
            if (bytes.size <= MAX_FILE_SIZE_BYTES) break
            quality -= QUALITY_STEP
            if (quality < MIN_QUALITY) {
                val hw = bm.width / 2
                val hh = bm.height / 2
                if (hw < 100 || hh < 100) break
                val halved = Bitmap.createScaledBitmap(bm, hw, hh, true)
                bm.recycle()
                bm = halved
                quality = INITIAL_QUALITY
            }
        }
        bm.recycle()

        val out = File(activity.cacheDir, "profile_compressed_${System.currentTimeMillis()}.jpg")
        val fos = FileOutputStream(out)
        fos.write(bytes)
        fos.flush()
        fos.close()
        if (raw.exists()) {
            raw.delete()
        }
        return out
    }

    private fun profile_upload(imageUri: Uri, iv: ImageView, icon: TextView, del: ImageView?) {
        progress_dialog = AndroidUtils.get_progress(activity)
        iv.visibility = View.VISIBLE
        icon.visibility = View.GONE
        Glide.with(activity)
            .load(imageUri)
            .circleCrop()
            .diskCacheStrategy(DiskCacheStrategy.NONE)
            .skipMemoryCache(true)
            .into(iv)
        animateFadeIn(iv)

        val fp = isFirmProfile()
        val urlStr = if (fp) "v3/firm/profile/pic/upload" else "v3/profile/pic/upload"

        Thread {
            try {
                val compressed = compressImageTo2MB(imageUri)
                val json = JSONObject()
                json.put("type", if (fp) "firm_logo" else "profile_pic")
                WebServiceHelper.callHttpUploadWebService(
                    this,
                    context,
                    WebServiceHelper.RestMethodType.POST,
                    urlStr,
                    "Profile_upload",
                    compressed,
                    json.toString()
                )
            } catch (e: Exception) {
                Log.e("UPLOAD_DEBUG", "Upload failed", e)
                activity.runOnUiThread {
                    animateShake(iv)
                    Toast.makeText(context, "Image error: ${e.message}", Toast.LENGTH_LONG).show()
                }
            }
        }.start()
    }

    private fun profile() {
        progress_dialog = AndroidUtils.get_progress(activity)
        val fp = isFirmProfile()
        val urlStr = if (fp) "v3/firm/profile/pic" else "v3/profile/pic"
        WebServiceHelper.callHttpWebService(
            this, context,
            WebServiceHelper.RestMethodType.GET,
            urlStr, "Profile", JSONObject().toString()
        )
    }

    private fun delete() {
        progress_dialog = AndroidUtils.get_progress(activity)
        val fp = isFirmProfile()
        val urlStr = if (fp) "v3/firm/profile/pic" else "v3/profile/pic"
        try {
            WebServiceHelper.callHttpWebService(
                this, context,
                WebServiceHelper.RestMethodType.DELETE,
                urlStr, "Profile_delete", JSONObject().toString()
            )
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun DeleteAccount() {
        progress_dialog = AndroidUtils.get_progress(activity)
        WebServiceHelper.callHttpWebService(
            this, context,
            WebServiceHelper.RestMethodType.POST,
            "v3/profile/delete-account", "Delete Account",
            JSONObject().toString()
        )
    }

    override fun onClick(view: View) {}

    override fun onAsyncTaskComplete(httpResult: HttpResultDo) {
        val prog = progress_dialog
        if (prog != null && prog.isShowing) {
            AndroidUtils.dismiss_dialog(prog)
        }
        val iv = currentIvProfile ?: return
        val icon = currentPersonIcon ?: return
        if (httpResult.result != WebServiceHelper.ServiceCallStatus.Success) return

        try {
            val result = JSONObject(httpResult.responseContent)
            val type = httpResult.requestType
            Log.d("Request_Type", type ?: "")

            val isImageFetch = "Profile" == type || "Firm_Logo" == type
            val isImageUpload = "Profile_upload" == type || "Firm_Logo_Upload" == type
            val isImageDelete = "Profile_delete" == type || "Firm_Logo_Delete" == type

            if (isImageFetch) {
                val err = result.optBoolean("error")
                if (!err) {
                    val d = result.optJSONObject("data")
                    if (d != null) {
                        val url = d.optString("imageUrl")
                        if (!TextUtils.isEmpty(url)) {
                            AppImageCache.invalidate(context, Constants.firm_image)
                            if (!isFirmProfile()) {
                                Constants.firm_image = url
                            }
                            AppImageCache.preload(context, url)
                            AndroidUtils.loadProfileImage(
                                context, url,
                                iv, icon
                            )
                            animateScalePulse(iv)
                            if (Constants.isMyProfileClicked) {
                                icon.text = if (!TextUtils.isEmpty(Constants.NAME)) {
                                    Constants.NAME!!.substring(0, 1).uppercase(Locale.getDefault())
                                } else {
                                    "?"
                                }
                            } else {
                                icon.text = if (!TextUtils.isEmpty(Constants.FIRM_NAME)) {
                                    Constants.FIRM_NAME!!.substring(0, 1).uppercase(Locale.getDefault())
                                } else {
                                    "?"
                                }
                            }
                            currentIvDeleteCircle?.visibility = View.VISIBLE
                        } else {
                            currentIvDeleteCircle?.visibility = View.GONE
                        }
                    }
                }
                if (Constants.ROLE == "AAM") {
                    Constants.mainActivity?.profile()
                } else {
                    if (!isFirmProfile()) {
                        Constants.mainActivity?.profile()
                    }
                }

            } else if (isImageUpload) {
                val err = result.optBoolean("error")
                if (!err) {
                    currentIvDeleteCircle?.visibility = View.VISIBLE
                    profile()
                    AndroidUtils.showAlert(result.optString("msg", "Success"), activity)
                } else {
                    AndroidUtils.showAlert(result.optString("msg"), activity)
                    animateShake(iv)
                }
                practicePartnerView.callViewBasicProfile()

            } else if (type == "Update_Bp") {
                val iserror = result.optBoolean("error")
                if (!iserror) {
                    val data = result.optJSONObject("data")
                    if (data != null) {
                        val msg = data.optString("msg", "")
                        AndroidUtils.showAlert_docs("Success !", msg, activity)
                        profileCompletionBanner?.visibility = View.GONE
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
                }
            } else if (isImageDelete) {
                val err = result.optBoolean("error")
                Constants.mainActivity?.profile()
                if (!err) {
                    Constants.firm_image = ""
                    currentIvDeleteCircle?.visibility = View.GONE
                    iv.setImageResource(R.drawable.ic_profile_placeholder)
                    iv.visibility = View.GONE
                    iv.isClickable = false
                    icon.visibility = View.VISIBLE
                    if (Constants.isMyProfileClicked) {
                        icon.text = if (!TextUtils.isEmpty(Constants.NAME)) {
                            Constants.NAME!!.substring(0, 1).uppercase(Locale.getDefault())
                        } else {
                            "?"
                        }
                    } else {
                        icon.text = if (!TextUtils.isEmpty(Constants.FIRM_NAME)) {
                            Constants.FIRM_NAME!!.substring(0, 1).uppercase(Locale.getDefault())
                        } else {
                            "?"
                        }
                    }
                    AndroidUtils.showAlert(result.optString("msg", "Success"), activity)
                } else {
                    AndroidUtils.showAlert(result.optString("msg"), activity)
                }
                practicePartnerView.callViewBasicProfile()

            } else if ("Delete Account" == type) {
                AndroidUtils.showAlert(
                    result.optString(
                        "msg",
                        "Account deletion request submitted. Will be completed in 2 weeks."
                    ),
                    activity, "Success"
                )
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun checkProfileCompletionStatus(m: FirmProfileModel?): ProfileCompletionStatus {
        if (m?.data?.profile == null) return ProfileCompletionStatus.BASIC_PROFILE_INCOMPLETE

        val p = m.data?.profile ?: return ProfileCompletionStatus.BASIC_PROFILE_INCOMPLETE
        val f = p.firm
        var basic = true

        if (f == null) {
            basic = false
        } else {
            if ("solo".equals(Constants.CATEGORY, ignoreCase = true)) {
                if (TextUtils.isEmpty(f.contact_person)) basic = false
            } else {
                if (TextUtils.isEmpty(f.fullname)) basic = false
            }
            if (TextUtils.isEmpty(f.email)) basic = false
            val phoneVal = f.contact_phone
            if (TextUtils.isEmpty(phoneVal) || (phoneVal != null && phoneVal.length < 10)) basic = false
            if (TextUtils.isEmpty(f.billing_currency)) basic = false
            if (TextUtils.isEmpty(f.website)) basic = false
        }
        if ("solo".equals(Constants.CATEGORY, ignoreCase = true)) {
            if (TextUtils.isEmpty(p.gender)) basic = false
            if (TextUtils.isEmpty(p.date_of_birth)) basic = false
        }
        val cf = p.consultation_fee
        if (cf == null || TextUtils.isEmpty(cf.amount)) basic = false
        if (f?.address != null) {
            val a = f.address ?: return ProfileCompletionStatus.BASIC_PROFILE_INCOMPLETE
            val zip = a.zipcode
            if (TextUtils.isEmpty(a.house_flat_no) || TextUtils.isEmpty(a.city_town)
                || TextUtils.isEmpty(a.state) || TextUtils.isEmpty(a.country)
                || TextUtils.isEmpty(zip) || (zip != null && zip.length < 5)
            ) {
                basic = false
            }
        } else {
            basic = false
        }
        if (f?.correspondence_address != null) {
            val a = f.correspondence_address ?: return ProfileCompletionStatus.BASIC_PROFILE_INCOMPLETE
            val zip = a.zipcode
            if (TextUtils.isEmpty(a.house_flat_no) || TextUtils.isEmpty(a.city_town)
                || TextUtils.isEmpty(a.state) || TextUtils.isEmpty(a.country)
                || TextUtils.isEmpty(zip) || (zip != null && zip.length < 5)
            ) {
                basic = false
            }
        } else {
            basic = false
        }
        if (!basic) return ProfileCompletionStatus.BASIC_PROFILE_INCOMPLETE

        var additional = true
        if (TextUtils.isEmpty(p.bar_council_id)) additional = false
        val yoe = p.years_of_experience
        if (yoe <= 0) additional = false
        if (p.practice_areas == null || p.practice_areas?.length() == 0) additional = false
        if (p.languages_spoken == null || p.languages_spoken?.length() == 0) additional = false
        if (p.services_offered == null || p.services_offered?.length() == 0) additional = false
        if (p.education.isNullOrEmpty()) additional = false
        val schedule = p.availability?.weekly_schedule
        if (p.availability == null || schedule.isNullOrEmpty()) {
            additional = false
        } else {
            var hasWork = false
            for (s in schedule) {
                if (s.isIs_working_day) {
                    hasWork = true
                    break
                }
            }
            if (!hasWork) additional = false
        }
        if (!additional) return ProfileCompletionStatus.ADDITIONAL_INFO_INCOMPLETE
        return ProfileCompletionStatus.COMPLETE
    }

    private fun updateCompletionBanner(holder: MyViewHolder, model: FirmProfileModel?) {
        var showBanner = false
        try {
            if (model?.data?.profile != null) {
                val profile = model.data?.profile ?: return
                showBanner = if (isFirmProfile()) {
                    profile.firm != null && (profile.firm?.show_profile_banner == true)
                } else {
                    profile.show_profile_banner == true
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }

        if (showBanner) {
            holder.tvCompleteNow?.setText(R.string.complete_now)
            profileCompletionBanner?.visibility = View.VISIBLE
        } else {
            profileCompletionBanner?.visibility = View.GONE
        }
    }

    private fun updateProfileBanner() {
        try {
            val request = JSONObject()
            val payload = JSONObject()

            val fp = isFirmProfile()
            Log.d("Update_Bp", payload.toString())
            if (fp) {
                payload.put("profile_completion_banner_dismissed", true)
                request.put("firm", payload)
            } else {
                request.put("profile_completion_banner_dismissed", true)
            }

            WebServiceHelper.callHttpWebService(
                this,
                context,
                WebServiceHelper.RestMethodType.PATCH,
                "v3/profile",
                "Update_Bp",
                request.toString()
            )

        } catch (e: Exception) {
            e.fillInStackTrace()
        }
    }

    private fun handleFallbackNavigation(model: FirmProfileModel?) {
        Constants.TARGET_FIELD = ""
        var pct = 0
        try {
            if (model?.data?.profile != null) {
                val profile = model.data?.profile ?: return
                val completion = if (isFirmProfile()) {
                    profile.firm?.profile_completion
                } else {
                    profile.profile_completion
                }
                pct = completion?.completion_percentage ?: 0
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }

        if (pct >= 100) {
            val rv = recyclerView
            if (rv != null) {
                val vh = rv.findViewHolderForAdapterPosition(0) as? MyViewHolder
                if (vh != null) {
                    profileCompletionBanner?.visibility = View.GONE
                }
            }
            AndroidUtils.showAlert("Profile is already complete!", activity)
        } else {
            when (checkProfileCompletionStatus(model)) {
                ProfileCompletionStatus.BASIC_PROFILE_INCOMPLETE -> {
                    Constants.PROFILE_EDIT_TAB = ""
                    eventListener.EditProfile(true, false)
                }
                ProfileCompletionStatus.ADDITIONAL_INFO_INCOMPLETE -> {
                    Constants.PROFILE_EDIT_TAB = "practice_details"
                    eventListener.EditProfile(false, true)
                }
                ProfileCompletionStatus.COMPLETE -> {
                    Constants.PROFILE_EDIT_TAB = "practice_details"
                    eventListener.EditProfile(false, true)
                }
            }
        }
    }

    private fun dispatchAction(action: String?, model: MemberProfileModel) {
        when (action) {
            "Edit" -> {
                if (!Constants.is_active) {
                    AndroidUtils.showRenewalPopup(activity)
                } else {
                    eventListener.EditPracticePartners(model)
                }
            }
            "Delete" -> {
                eventListener.DeletePracticePartners(model)
            }
        }
    }

    interface InterfaceListener {
        fun EditPracticePartners(model: MemberProfileModel?)
        fun EditProfile(isProfileEdit: Boolean?, isAdditionalInfo: Boolean?)
        fun DeletePracticePartners(model: MemberProfileModel?)
    }

    inner class MyViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        // ── Mode A fields ─────────────────────────────────────────────────
        var tv_name: TextView? = null
        var tv_designation: TextView? = null
        var tv_designation_name: TextView? = null
        var person_icon: TextView? = null
        var tv_specialist: TextView? = null
        var tv_specialist_name: TextView? = null
        var tv_email: TextView? = null
        var tv_phonenumber: TextView? = null
        var iv_action_menu: ImageView? = null
        var action_list_card: CardView? = null
        var action_list: ListView? = null
        var emailLabel: TextView? = null
        var phone: TextView? = null

        // ── Mode B (Profile card) fields ──────────────────────────────────
        var iv_profile: ImageView? = null
        var iv_edit: ImageView? = null
        var iv_edit_circle: ImageView? = null
        var iv_delete_circle: ImageView? = null
        var ivClose: ImageView? = null
        var iv_edit_addinfo: ImageView? = null
        var scrollView: ScrollView? = null
        var tabLayout: TabLayout? = null
        var ll_basicprofile: LinearLayout? = null
        var ll_subcription: LinearLayout? = null
        var ll_viewscreen: LinearLayout? = null
        var ll_additional_inform: LinearLayout? = null
        var ll_payment: LinearLayout? = null
        var ll_delete_account: LinearLayout? = null

        var tvMessage: TextView? = null
        var tv_BasicProfile: TextView? = null
        var FirmName: TextView? = null
        var tv_FirmName: TextView? = null
        var Country: TextView? = null
        var tv_Country: TextView? = null
        var Email: TextView? = null
        var tv_Email: TextView? = null
        var ContactName: TextView? = null
        var tv_ContactName: TextView? = null
        var ContactPhone: TextView? = null
        var tv_ContactPhone: TextView? = null
        var Website: TextView? = null
        var tv_Website: TextView? = null
        var dob: TextView? = null
        var tv_dob: TextView? = null
        var gender: TextView? = null
        var tv_gender: TextView? = null
        var nationality: TextView? = null
        var tv_nationality: TextView? = null
        var bio: TextView? = null
        var tv_bio: TextView? = null
        var tvPercentage: TextView? = null
        var tvCompleteNow: TextView? = null
        var tv_add_info: TextView? = null

        var tv_Address: TextView? = null
        var Building: TextView? = null
        var tv_Building: TextView? = null
        var Street: TextView? = null
        var tv_Street: TextView? = null
        var City: TextView? = null
        var tv_City: TextView? = null
        var State: TextView? = null
        var tv_State: TextView? = null
        var Ad_Country: TextView? = null
        var tv_Ad_Country: TextView? = null
        var Zip: TextView? = null
        var tv_Zip: TextView? = null
        var tv_amount: TextView? = null
        var amount: TextView? = null
        var termsDate: TextView? = null
        var tv_termsDate: TextView? = null
        var tv_delete_account: TextView? = null
        var account_del: TextView? = null

        var mtv_Address: TextView? = null
        var mBuilding: TextView? = null
        var mtv_Building: TextView? = null
        var mStreet: TextView? = null
        var mtv_Street: TextView? = null
        var mCity: TextView? = null
        var mtv_City: TextView? = null
        var mState: TextView? = null
        var mtv_State: TextView? = null
        var mAd_Country: TextView? = null
        var mtv_Ad_Country: TextView? = null
        var mZip: TextView? = null
        var mtv_Zip: TextView? = null
        var stv_subscription: TextView? = null
        var tv_personal_info: TextView? = null
        var tv_sub: TextView? = null
        var saccount_activated_on: TextView? = null
        var tvaccount_activated_on: TextView? = null
        var ssubscription_start_date: TextView? = null
        var tvsubscription_start_date: TextView? = null
        var splan_details: TextView? = null
        var tvplan_details: TextView? = null
        var smode_of_payment: TextView? = null
        var tvmode_of_payment: TextView? = null
        var splan_validity: TextView? = null
        var tvplan_validity: TextView? = null
        var snext_billing_date: TextView? = null
        var tvnext_billing_date: TextView? = null

        var firmProfile: FirmProfile? = null

        init {
            // ── Mode A ────────────────────────────────────────────────────
            if (Constants.Profile_View != "Bp") {
                tv_name = itemView.findViewById(R.id.tv_name)
                emailLabel = itemView.findViewById(R.id.email)
                emailLabel?.setText(R.string.email_)
                phone = itemView.findViewById(R.id.phone)
                phone?.setText(R.string.phone_)
                tv_designation = itemView.findViewById(R.id.tv_designation)
                tv_designation_name = itemView.findViewById(R.id.tv_designation_name)
                tv_specialist = itemView.findViewById(R.id.tv_specialist)
                tv_specialist_name = itemView.findViewById(R.id.tv_specialist_name)
                tv_email = itemView.findViewById(R.id.tv_email)
                tv_phonenumber = itemView.findViewById(R.id.tv_phonenumber)
                tv_designation?.setText(R.string.designation)
                tv_specialist?.setText(R.string.specialist)
                iv_action_menu = itemView.findViewById(R.id.iv_action_menu)
                action_list_card = itemView.findViewById(R.id.action_list_card)
                action_list = itemView.findViewById(R.id.action_list)
                tv_phonenumber?.gravity = Gravity.CENTER_VERTICAL
            } else {
                // ── Mode B ────────────────────────────────────────────────────
                val isGHTeamMember = Constants.ROLE.equals("GH", ignoreCase = true)
                        || Constants.ROLE.equals("TM", ignoreCase = true)

                person_icon = itemView.findViewById(R.id.person_icon)
                person_icon?.textSize = DynamicUtils.thirtyFive.toFloat()
                iv_profile = itemView.findViewById(R.id.iv_profile)
                iv_edit_circle = itemView.findViewById(R.id.iv_edit_circle)
                iv_delete_circle = itemView.findViewById(R.id.iv_delete_circle)
                iv_delete_circle?.visibility = View.GONE

                iv_delete_circle?.setOnClickListener { v ->
                    AndroidUtils.showConfirmationDialog(
                        context,
                        "Confirmation",
                        "Are you sure you want to remove the Profile Picture ?",
                        object : AndroidUtils.OnConfirmListener {
                            override fun onSave() {
                                delete()
                            }

                            override fun onCancel() {}
                        }
                    )
                }

                iv_edit_circle?.visibility = View.VISIBLE
                currentIvProfile = iv_profile
                currentPersonIcon = person_icon
                currentIvEditCircle = iv_edit_circle
                currentIvDeleteCircle = iv_delete_circle

                iv_edit_circle?.setOnClickListener { v ->
                    currentIvProfile = iv_profile
                    currentPersonIcon = person_icon
                    currentIvDeleteCircle = iv_delete_circle

                    val fc = Filecosen(activity, object : Filecosen.FileChooserCallback {
                        override fun openCamera() {
                            if (context.checkSelfPermission(android.Manifest.permission.CAMERA)
                                == PackageManager.PERMISSION_GRANTED
                            ) {
                                openCamera_new(iv_profile, person_icon, iv_delete_circle)
                            } else {
                                requestCameraPermission?.launch(android.Manifest.permission.CAMERA)
                            }
                        }

                        override fun openGallery() {
                            openGallery_new(iv_profile, person_icon, iv_delete_circle)
                        }
                    })
                    fc.show()
                }

                ll_basicprofile = itemView.findViewById(R.id.ll_basicprofile)
                ll_subcription = itemView.findViewById(R.id.ll_subcription)
                ll_additional_inform = itemView.findViewById(R.id.ll_additional_inform)
                ll_subcription?.visibility = View.GONE

                iv_edit = itemView.findViewById(R.id.iv_edit)
                iv_edit_addinfo = itemView.findViewById(R.id.iv_edit_addinfo)
                applyTabletLayout(ll_basicprofile!!, ll_additional_inform!!)

                iv_edit?.setOnClickListener { v ->
                    if (!Constants.is_active) {
                        AndroidUtils.showRenewalPopup(activity)
                    } else {
                        Constants.TARGET_FIELD = ""
                        Constants.PROFILE_EDIT_TAB = ""
                        eventListener.EditProfile(true, false)
                    }
                }
                iv_edit_addinfo?.setOnClickListener { v ->
                    if (!Constants.is_active) {
                        AndroidUtils.showRenewalPopup(activity)
                    } else {
                        Constants.TARGET_FIELD = ""
                        if (TextUtils.isEmpty(Constants.PROFILE_EDIT_TAB)) {
                            Constants.PROFILE_EDIT_TAB = "practice_details"
                        }
                        eventListener.EditProfile(false, true)
                    }
                }
                tv_BasicProfile = itemView.findViewById(R.id.tv_BasicProfile)
                FirmName = itemView.findViewById(R.id.FirmName)
                FirmName?.setTextColor(context.getColor(R.color.grey_light))
                tv_FirmName = itemView.findViewById(R.id.tv_FirmName)
                tvPercentage = itemView.findViewById(R.id.tvPercentage)
                profileCompletionBanner = itemView.findViewById(R.id.profileCompletionBanner)
                tvCompleteNow = itemView.findViewById(R.id.tvCompleteNow)
                tvCompleteNow?.paintFlags = (tvCompleteNow?.paintFlags ?: 0) or Paint.UNDERLINE_TEXT_FLAG
                ivClose = itemView.findViewById(R.id.ivClose)
                tvMessage = itemView.findViewById(R.id.tvMessage)
                ivClose?.setOnClickListener { v -> updateProfileBanner() }

                Country = itemView.findViewById(R.id.Country)
                Country?.setTextColor(context.getColor(R.color.grey_light))
                tv_Country = itemView.findViewById(R.id.tv_Country)
                scrollView = itemView.findViewById(R.id.scrollview)
                Email = itemView.findViewById(R.id.Email)
                Email?.setTextColor(context.getColor(R.color.grey_light))
                tv_Email = itemView.findViewById(R.id.tv_Email)
                ContactName = itemView.findViewById(R.id.ContactName)
                ContactName?.setTextColor(context.getColor(R.color.grey_light))
                tv_ContactName = itemView.findViewById(R.id.tv_ContactName)
                ContactPhone = itemView.findViewById(R.id.ContactPhone)
                ContactPhone?.setTextColor(context.getColor(R.color.grey_light))
                tv_ContactPhone = itemView.findViewById(R.id.tv_ContactPhone)
                Website = itemView.findViewById(R.id.Website)
                Website?.setTextColor(context.getColor(R.color.grey_light))
                tv_Website = itemView.findViewById(R.id.tv_Website)
                dob = itemView.findViewById(R.id.dob)
                dob?.setTextColor(context.getColor(R.color.grey_light))
                tv_dob = itemView.findViewById(R.id.tv_dob)
                gender = itemView.findViewById(R.id.gender)
                gender?.setTextColor(context.getColor(R.color.grey_light))
                tv_gender = itemView.findViewById(R.id.tv_gender)
                nationality = itemView.findViewById(R.id.nationality)
                nationality?.setTextColor(context.getColor(R.color.grey_light))
                tv_nationality = itemView.findViewById(R.id.tv_nationality)
                bio = itemView.findViewById(R.id.bio)
                bio?.setTextColor(context.getColor(R.color.grey_light))
                tv_bio = itemView.findViewById(R.id.tv_bio)

                if (!isFirmProfile() && "solo".equals(Constants.CATEGORY, ignoreCase = true)) {
                    FirmName?.visibility = View.GONE
                    tv_FirmName?.visibility = View.GONE
                    ContactName?.setText(R.string.name)
                } else {
                    FirmName?.visibility = View.VISIBLE
                    tv_FirmName?.visibility = View.VISIBLE
                    ContactName?.setText(
                        if (isGHTeamMember) {
                            context.getString(R.string.name)
                        } else {
                            context.getString(R.string._contact_name)
                        }
                    )
                }

                if ("entity".equals(Constants.CATEGORY, ignoreCase = true)) {
                    dob?.visibility = View.GONE
                    tv_dob?.visibility = View.GONE
                    gender?.visibility = View.GONE
                    tv_gender?.visibility = View.GONE
                } else {
                    dob?.visibility = View.VISIBLE
                    tv_dob?.visibility = View.VISIBLE
                    gender?.visibility = View.VISIBLE
                    tv_gender?.visibility = View.VISIBLE
                }

                // Registered address
                tv_Address = itemView.findViewById(R.id.tv_Address)
                Building = itemView.findViewById(R.id.Building)
                Building?.setTextColor(context.getColor(R.color.grey_light))
                tv_Building = itemView.findViewById(R.id.tv_Building)
                Street = itemView.findViewById(R.id.Street)
                Street?.setTextColor(context.getColor(R.color.grey_light))
                tv_Street = itemView.findViewById(R.id.tv_Street)
                City = itemView.findViewById(R.id.City)
                City?.setTextColor(context.getColor(R.color.grey_light))
                tv_City = itemView.findViewById(R.id.tv_City)
                State = itemView.findViewById(R.id.State)
                State?.setTextColor(context.getColor(R.color.grey_light))
                tv_State = itemView.findViewById(R.id.tv_State)
                Ad_Country = itemView.findViewById(R.id.Ad_Country)
                Ad_Country?.setTextColor(context.getColor(R.color.grey_light))
                tv_Ad_Country = itemView.findViewById(R.id.tv_Ad_Country)
                Zip = itemView.findViewById(R.id.Zip)
                Zip?.setTextColor(context.getColor(R.color.grey_light))
                tv_Zip = itemView.findViewById(R.id.tv_Zip)

                // Mailing address
                mtv_Address = itemView.findViewById(R.id.tv_Address1)
                mBuilding = itemView.findViewById(R.id.Building1)
                mBuilding?.setTextColor(context.getColor(R.color.grey_light))
                mtv_Building = itemView.findViewById(R.id.tv_Building1)
                mStreet = itemView.findViewById(R.id.Street1)
                mStreet?.setTextColor(context.getColor(R.color.grey_light))
                mtv_Street = itemView.findViewById(R.id.tv_Street1)
                mCity = itemView.findViewById(R.id.City1)
                mCity?.setTextColor(context.getColor(R.color.grey_light))
                mtv_City = itemView.findViewById(R.id.tv_City1)
                mState = itemView.findViewById(R.id.State1)
                mState?.setTextColor(context.getColor(R.color.grey_light))
                mtv_State = itemView.findViewById(R.id.tv_State1)
                mAd_Country = itemView.findViewById(R.id.Ad_Country1)
                mAd_Country?.setTextColor(context.getColor(R.color.grey_light))
                mtv_Ad_Country = itemView.findViewById(R.id.tv_Ad_Country1)
                mZip = itemView.findViewById(R.id.Zip1)
                mZip?.setTextColor(context.getColor(R.color.grey_light))
                mtv_Zip = itemView.findViewById(R.id.tv_Zip1)

                // Subscription views — bind only if NOT GH/TM
                if (!isGHTeamMember) {
                    val subLayout = ll_subcription
                    if (subLayout != null) {
                        stv_subscription = subLayout.findViewById(R.id.tv_Address)
                        saccount_activated_on = subLayout.findViewById(R.id.Building)
                        tv_amount = subLayout.findViewById(R.id.tv_amount)
                        tv_amount?.visibility = View.GONE
                        amount = subLayout.findViewById(R.id.amount)
                        amount?.visibility = View.GONE
                        ll_payment = subLayout.findViewById(R.id.ll_payment)
                        ll_delete_account = subLayout.findViewById(R.id.ll_delete_account)
                        val delAccountLayout = ll_delete_account
                        if (delAccountLayout != null) {
                            account_del = delAccountLayout.findViewById(R.id.account_del)
                            tv_delete_account = delAccountLayout.findViewById(R.id.tv_delete_account)
                        }
                        termsDate = subLayout.findViewById(R.id.termsDate)
                        tv_termsDate = subLayout.findViewById(R.id.tv_termsDate)
                        account_del?.setText(R.string.account_delete_request)
                        tvaccount_activated_on = subLayout.findViewById(R.id.tv_Building)
                        ssubscription_start_date = subLayout.findViewById(R.id.Street)
                        tvsubscription_start_date = subLayout.findViewById(R.id.tv_Street)
                        splan_details = subLayout.findViewById(R.id.City)
                        tvplan_details = subLayout.findViewById(R.id.tv_City)
                        smode_of_payment = subLayout.findViewById(R.id.State)
                        tvmode_of_payment = subLayout.findViewById(R.id.tv_State)
                        splan_validity = subLayout.findViewById(R.id.Ad_Country)
                        tvplan_validity = subLayout.findViewById(R.id.tv_Ad_Country)
                        snext_billing_date = subLayout.findViewById(R.id.Zip)
                        tvnext_billing_date = subLayout.findViewById(R.id.tv_Zip)
                    }

                    tv_delete_account?.setOnClickListener { v ->
                        AndroidUtils.showConfirmationDialog(
                            context,
                            "Confirmation",
                            "This action cannot be undone. You will lose access to all your data.\n\nAre you sure?",
                            object : AndroidUtils.OnConfirmListener {
                                override fun onSave() {
                                    DeleteAccount()
                                }

                                override fun onCancel() {}
                            }
                        )
                    }
                }

                // ── Tabs ─────────────────────────────────────────────────────
                tabLayout = itemView.findViewById(R.id.tab_layout)
                ll_viewscreen = itemView.findViewById(R.id.ll_viewscreen)
                tv_add_info = itemView.findViewById(R.id.tv_add_info)
                tv_add_info?.setText(R.string.additional_info)
                tv_add_info?.setTextColor(context.getColor(R.color.Blue_text_color))
                tv_add_info?.textSize = 20f

                val fpVal = isFirmProfile()
                val isAMMorSUVH = Constants.ROLE.equals("AAM", ignoreCase = true)

                tabLayout?.clearOnTabSelectedListeners()
                tabLayout?.removeAllTabs()

                // Tab 0: Practice Details
                val tabLayoutRef = tabLayout
                if (tabLayoutRef != null) {
                    tabLayoutRef.addTab(tabLayoutRef.newTab().setText("Practice\nDetails"))

                    if (fpVal || isAMMorSUVH) {
                        // For Firm Profile: Tab 1 = Awards & Recognition
                        tabLayoutRef.addTab(tabLayoutRef.newTab().setText("Awards &\nRecognition"))
                    } else {
                        // For My Profile: Tab 1 = Courts & Cases, Tab 2 = Education & Awards, Tab 3 = Availability
                        tabLayoutRef.addTab(tabLayoutRef.newTab().setText("Courts\n& Cases"))
                        tabLayoutRef.addTab(tabLayoutRef.newTab().setText("Education\n& Awards"))
                        tabLayoutRef.addTab(tabLayoutRef.newTab().setText("Set\nAvailability"))
                    }

                    // Force multi-line text in all tabs
                    for (i in 0 until tabLayoutRef.tabCount) {
                        val t = tabLayoutRef.getTabAt(i)
                        if (t != null) {
                            setTabTextStyle(t)
                        }
                    }

                    val def = tabLayoutRef.getTabAt(0)
                    def?.select()
                }
            }
        }

        private fun setTabTextStyle(tab: TabLayout.Tab) {
            try {
                val field = TabLayout.Tab::class.java.getDeclaredField("mTextView")
                field.isAccessible = true
                val textView = field.get(tab) as? TextView
                if (textView != null) {
                    textView.isSingleLine = false
                    textView.gravity = Gravity.CENTER
                    textView.maxLines = 2
                    return
                }
            } catch (e: Exception) {
                // Reflection failed, try fallback
            }

            val tabView = tab.view
            if (tabView != null) {
                findTextViewInView(tabView)
            }
        }

        private fun findTextViewInView(view: View) {
            if (view is TextView) {
                view.isSingleLine = false
                view.gravity = Gravity.CENTER
                view.maxLines = 2
            } else if (view is ViewGroup) {
                for (i in 0 until view.childCount) {
                    findTextViewInView(view.getChildAt(i))
                }
            }
        }
    }

    private fun applyTabletLayout(llBasic: LinearLayout, llAdditional: LinearLayout) {
        if (!DynamicUtils.isTablet(context)) return
        val parent = llBasic.parent as? ViewGroup ?: return
        val idx = parent.indexOfChild(llBasic)
        parent.removeView(llBasic)
        parent.removeView(llAdditional)
        val row = LinearLayout(context)
        row.orientation = LinearLayout.HORIZONTAL
        row.layoutParams = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        )
        val lp = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.MATCH_PARENT, 1f)
        lp.setMargins(0, 20, 8, 0)
        llBasic.layoutParams = lp
        val rp = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.MATCH_PARENT, 1f)
        rp.setMargins(8, 20, 0, 0)
        llAdditional.layoutParams = rp
        row.addView(llBasic)
        row.addView(llAdditional)
        parent.addView(row, idx)
    }

    object ReadMoreUtils {
        fun setReadMore(textView: TextView?, fullText: String?, collapsedLines: Int) {
            if (textView == null) return
            if (TextUtils.isEmpty(fullText)) {
                textView.text = ""
                return
            }
            val MORE = " Read more"
            val LESS = " Read less"
            textView.text = fullText
            textView.ellipsize = null
            textView.maxLines = Int.MAX_VALUE
            textView.post {
                val layout = textView.layout ?: return@post
                if (layout.lineCount <= collapsedLines) return@post
                val end = layout.getLineEnd(collapsedLines - 1)
                val trimmed = (fullText ?: "").substring(0, end).trim()
                val sp = SpannableString(trimmed + MORE)
                sp.setSpan(object : ClickableSpan() {
                    var expanded = false

                    override fun onClick(w: View) {
                        expanded = !expanded
                        if (expanded) {
                            setExpanded(textView, fullText ?: "", LESS, this)
                        } else {
                            setReadMore(textView, fullText, collapsedLines)
                        }
                    }

                    override fun updateDrawState(ds: TextPaint) {
                        ds.color = Color.BLACK
                        ds.isUnderlineText = true
                    }
                }, sp.length - MORE.length, sp.length, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
                textView.text = sp
                textView.movementMethod = LinkMovementMethod.getInstance()
            }
        }

        private fun setExpanded(tv: TextView, full: String, less: String, span: ClickableSpan) {
            val sp = SpannableString(full + less)
            sp.setSpan(
                span, sp.length - less.length, sp.length,
                Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
            )
            tv.maxLines = Int.MAX_VALUE
            tv.text = sp
            tv.movementMethod = LinkMovementMethod.getInstance()
        }
    }

    companion object {
        private const val MAX_FILE_SIZE_BYTES = 2 * 1024 * 1024L
        private const val INITIAL_QUALITY = 90
        private const val MIN_QUALITY = 40
        private const val QUALITY_STEP = 10
    }
}
