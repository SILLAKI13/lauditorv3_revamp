package com.digicoffer.lauditor.FirmProfile

import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.LinearLayout
import android.widget.TextView
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.widget.LinearLayoutCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentTransaction
import androidx.lifecycle.ViewModelProvider
import com.digicoffer.lauditor.CommonFiles.GlobalFiles.AndroidUtils
import com.digicoffer.lauditor.CommonFiles.GlobalFiles.Constants
import com.digicoffer.lauditor.CommonFiles.GlobalFiles.NewModel
import com.digicoffer.lauditor.R
import com.digicoffer.lauditor.Webservice.AsyncTaskCompleteListener
import com.digicoffer.lauditor.Webservice.HttpResultDo

class FirmProfile : Fragment(), AsyncTaskCompleteListener {
    var tvBasicProfile: TextView? = null
    var tvPracticePartners: TextView? = null
    var tvEdit: TextView? = null
    var tvView: TextView? = null
    var tvPercentage: TextView? = null
    var flFirmProfile: FrameLayout? = null
    var llc_Edit_View: LinearLayoutCompat? = null
    var tv_switchEditProfile: LinearLayout? = null
    var ll_profile: LinearLayout? = null
    var tv_switchView: View? = null
    var tv_switchCreate: View? = null
    private var mViewModel: NewModel? = null
    override fun onClick(view: View) {}

    fun interface OnEditProfileClickListener {
        fun onEditProfileClick()
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.activity_firm_profile, container, false)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        setupOnBackPressed()
        super.onCreate(savedInstanceState)
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

    override fun onViewCreated(v: View, savedInstanceState: Bundle?) {
        super.onViewCreated(v, savedInstanceState)
        ll_profile = v.findViewById(R.id.ll_profile)
        tvView = v.findViewById(R.id.tvView)
        tvEdit = v.findViewById(R.id.tvEdit)
        tvBasicProfile = v.findViewById(R.id.tvBasicProfile)
        tvPracticePartners = v.findViewById(R.id.tvPracticePartners)
        flFirmProfile = v.findViewById(R.id.flFirmProfile)
        llc_Edit_View = v.findViewById(R.id.llc_Edit_View)
        tv_switchView = v.findViewById(R.id.tv_switchView)
        tv_switchCreate = v.findViewById(R.id.tv_switchCreate)
        tv_switchEditProfile = v.findViewById(R.id.tv_switchEditProfile)

        val isGHTeamMember = Constants.ROLE.equals("GH", ignoreCase = true) ||
                Constants.ROLE.equals("TM", ignoreCase = true)

        if (Constants.CATEGORY == "solo" && Constants.ROLE == "SU") {
            ll_profile?.visibility = View.VISIBLE
        } else if (Constants.isMyProfileClicked || isGHTeamMember) {
            ll_profile?.visibility = View.GONE
        } else {
            ll_profile?.visibility = View.VISIBLE
        }

        tvView?.setText(R.string.subscription_)
        tvEdit?.setText(R.string.profile_info)
        tvBasicProfile?.setText(R.string.profile)
        tvPracticePartners?.text = "Practice Partners"

        if (isGHTeamMember) {
            tvView?.visibility = View.GONE
            tvEdit?.visibility = View.GONE
            Constants.issubscription = false
        }

        mViewModel = ViewModelProvider(requireActivity()).get(NewModel::class.java)
        if (Constants.isMyProfileClicked) {
            mViewModel?.setData(resources.getString(R.string.my_profile))
        } else {
            mViewModel?.setData(resources.getString(R.string.firm_profile))
        }

        if (Constants.Profile_View == "Bp") {
            loadBasicProfile()
        } else {
            loadPracticePartners()
        }

        tvPracticePartners?.setOnClickListener { loadPracticePartners() }
        tvBasicProfile?.setOnClickListener { loadBasicProfile() }

        tvEdit?.setOnClickListener {
            Constants.Profile_View = "Bp"
            Constants.Edit_OR_View = "View"
            Constants.issubscription = false

            tvEdit?.background = resources.getDrawable(R.drawable.button_left_green_background, null)
            tvView?.background = resources.getDrawable(R.drawable.button_right_background, null)
            tvEdit?.setTextColor(Color.WHITE)
            tvView?.setTextColor(Color.BLACK)

            ChangePage()
            Constants.PROFILE_EDIT_TAB = "practice_details"
        }

        tvView?.setOnClickListener {
            val isGH = Constants.ROLE.equals("GH", ignoreCase = true) ||
                    Constants.ROLE.equals("TM", ignoreCase = true)
            if (isGH) return@setOnClickListener

            Constants.Edit_OR_View = "View"
            Constants.issubscription = true
            ChangeBackGround()
        }

        AndroidUtils.setupModuleView(
            tv_switchView,
            getString(R.string.add_practice_partners),
            true,
            true,
            context,
            getString(R.string.view_practice_partners)
        ) {
            if (!Constants.is_active) {
                AndroidUtils.showRenewalPopup(requireActivity())
            } else {
                tv_switchView?.visibility = View.GONE
                tv_switchCreate?.visibility = View.VISIBLE
                NavFragment(PracticePartnerAdd(null, this))
                mViewModel?.setData(resources.getString(R.string.add_practice_partners))
            }
        }

        AndroidUtils.setupModuleView(
            tv_switchCreate,
            getString(R.string.view_practice_partners),
            false,
            true,
            context,
            getString(R.string.add_practice_partners)
        ) {
            tv_switchView?.visibility = View.VISIBLE
            tv_switchCreate?.visibility = View.GONE
            NavFragment(PracticePartnerView.newInstance(this))
            if (Constants.isMyProfileClicked) {
                mViewModel?.setData(resources.getString(R.string.my_profile))
            } else {
                mViewModel?.setData(resources.getString(R.string.firm_profile))
            }
        }
    }

    fun loadBasicProfile() {
        Constants.Profile_View = "Bp"
        Constants.Edit_OR_View = "View"
        Constants.issubscription = false
        tvEdit?.setText(R.string.profile_info)
        if (Constants.isMyProfileClicked) {
            mViewModel?.setData(resources.getString(R.string.my_profile))
        } else {
            mViewModel?.setData(resources.getString(R.string.firm_profile))
        }
        llc_Edit_View?.visibility = View.VISIBLE

        val isGHTeamMember = Constants.ROLE.equals("GH", ignoreCase = true) ||
                Constants.ROLE.equals("TM", ignoreCase = true)
        if (isGHTeamMember) {
            tvView?.visibility = View.GONE
        }

        ChangeBackGround()
        tv_switchView?.visibility = View.GONE
        tv_switchCreate?.visibility = View.GONE
    }

    fun loadPracticePartners() {
        Constants.Profile_View = "Pp"
        Constants.Edit_OR_View = "View"
        llc_Edit_View?.visibility = View.GONE
        tv_switchView?.visibility = View.GONE
        tv_switchCreate?.visibility = View.VISIBLE
        ChangeBackGround()
    }

    fun ChangeBackGround() {
        val isGHTeamMember = Constants.ROLE.equals("GH", ignoreCase = true) ||
                Constants.ROLE.equals("TM", ignoreCase = true)
        if (isGHTeamMember) {
            Constants.issubscription = false
            tvView?.visibility = View.GONE
            tvBasicProfile?.background = resources.getDrawable(R.drawable.rectangular_button_green_count, null)
        }

        if (Constants.Profile_View == "Bp") {
            tvBasicProfile?.background = resources.getDrawable(R.drawable.button_left_green_background, null)
            tvPracticePartners?.background = resources.getDrawable(R.drawable.button_right_background, null)
            tvBasicProfile?.setTextColor(Color.WHITE)
            tvPracticePartners?.setTextColor(Color.BLACK)
            tvEdit?.setText(R.string.profile_info)
            if (Constants.isMyProfileClicked) {
                mViewModel?.setData(resources.getString(R.string.my_profile))
            } else {
                mViewModel?.setData(resources.getString(R.string.firm_profile))
            }
            llc_Edit_View?.visibility = View.VISIBLE
        } else {
            llc_Edit_View?.visibility = View.GONE
            tvBasicProfile?.background = resources.getDrawable(R.drawable.button_left_background, null)
            tvPracticePartners?.background = resources.getDrawable(R.drawable.button_right_green_count, null)
            tvPracticePartners?.setTextColor(Color.WHITE)
            tvBasicProfile?.setTextColor(Color.BLACK)
            tvEdit?.setText(R.string.add)
            mViewModel?.setData(resources.getString(R.string.practice_partners))
        }

        if (Constants.Edit_OR_View == "Edit") {
            tvEdit?.background = resources.getDrawable(R.drawable.button_left_green_background, null)
            tvView?.background = resources.getDrawable(R.drawable.button_right_background, null)
            tvEdit?.setTextColor(Color.WHITE)
            tvView?.setTextColor(Color.BLACK)
        } else if (Constants.issubscription) {
            tvEdit?.background = resources.getDrawable(R.drawable.button_left_background, null)
            tvView?.background = resources.getDrawable(R.drawable.button_right_green_background, null)
            tvEdit?.setTextColor(Color.BLACK)
            tvView?.setTextColor(Color.WHITE)
        } else {
            tvEdit?.background = resources.getDrawable(R.drawable.button_left_green_background, null)
            tvView?.background = resources.getDrawable(R.drawable.button_right_background, null)
            tvEdit?.setTextColor(Color.WHITE)
            tvView?.setTextColor(Color.BLACK)
        }

        if ("solo".equals(Constants.CATEGORY, ignoreCase = true)) {
            tvPracticePartners?.visibility = View.GONE
            tvBasicProfile?.visibility = View.GONE
            tvBasicProfile?.background = resources.getDrawable(R.drawable.rectangular_button_green_count, null)
        } else {
            tvPracticePartners?.visibility = View.VISIBLE
        }

        ChangePage()
    }

    private fun ChangePage() {
        if (Constants.Profile_View != "Bp") {
            if (Constants.Edit_OR_View == "Edit") {
                if (!Constants.is_active) {
                    AndroidUtils.showRenewalPopup(requireActivity())
                } else {
                    NavFragment(PracticePartnerAdd(null, this))
                    mViewModel?.setData(resources.getString(R.string.add_practice_partners))
                }
            } else {
                tv_switchView?.visibility = View.VISIBLE
                tv_switchCreate?.visibility = View.GONE
                NavFragment(PracticePartnerView.newInstance(this))
                mViewModel?.setData(resources.getString(R.string.practice_partners))
            }
        } else {
            if (Constants.Edit_OR_View == "Edit") {
                if (!Constants.is_active) {
                    AndroidUtils.showRenewalPopup(requireActivity())
                } else {
                    NavFragment(BasicProfileEdit(this, true, false))
                    mViewModel?.setData(resources.getString(R.string.edit_profile))
                }
            } else {
                tv_switchView?.visibility = View.GONE
                tv_switchCreate?.visibility = View.GONE
                NavFragment(PracticePartnerView.newInstance(this))
                if (Constants.isMyProfileClicked) {
                    mViewModel?.setData(resources.getString(R.string.my_profile))
                } else {
                    mViewModel?.setData(resources.getString(R.string.firm_profile))
                }
            }
        }
    }

    fun NavFragment(fragment: Fragment) {
        val ft = childFragmentManager.beginTransaction()
        ft.replace(R.id.flFirmProfile, fragment)
        ft.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE)
        ft.addToBackStack(null)
        ft.commit()
    }

    override fun onAsyncTaskComplete(httpResult: HttpResultDo) {}
}
