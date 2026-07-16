package com.digicoffer.lauditor.FirmProfile

import android.app.Dialog
import android.os.Bundle
import android.text.Editable
import android.text.InputType
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.widget.LinearLayoutCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.digicoffer.lauditor.CommonFiles.GlobalFiles.AndroidUtils
import com.digicoffer.lauditor.CommonFiles.GlobalFiles.Constants
import com.digicoffer.lauditor.CommonFiles.GlobalFiles.DynamicUtils
import com.digicoffer.lauditor.CommonFiles.GlobalFiles.NewModel
import com.digicoffer.lauditor.CommonFiles.ValidationUtils.Validation
import com.digicoffer.lauditor.R
import com.digicoffer.lauditor.Webservice.AsyncTaskCompleteListener
import com.digicoffer.lauditor.Webservice.CommonApiHelper.WebServiceHelper
import com.digicoffer.lauditor.Webservice.HttpResultDo
import com.google.android.material.textfield.TextInputEditText
import org.json.JSONException
import org.json.JSONObject

class PracticePartnerAdd(
    private var memberProfileModel: MemberProfileModel?,
    private val firmProfile: FirmProfile
) : Fragment(), AsyncTaskCompleteListener, View.OnClickListener {

    private var tv_add_pp: TextView? = null
    private var tv_Fname: TextView? = null
    private var tv_Lname: TextView? = null
    private var tv_designation: TextView? = null
    private var tv_specialist: TextView? = null
    private var tv_email: TextView? = null
    private var tv_phonenumber: TextView? = null

    private var et_Fname: TextInputEditText? = null
    private var et_Lname: TextInputEditText? = null
    private var et_designation: TextInputEditText? = null
    private var et_specialist: TextInputEditText? = null
    private var et_email: TextInputEditText? = null
    private var et_phonenumber: TextInputEditText? = null

    private var btn_cancel_pp: Button? = null
    private var btn_save_pp: Button? = null
    private var progress_dialog: Dialog? = null
    private var isemailhasError = false
    private var iv_cancel: ImageView? = null
    private var isphonehasError = false
    private var response_email: TextView? = null
    private var tvErrorPhone: TextView? = null
    private var last_name_layout: LinearLayoutCompat? = null
    private var mViewModel: NewModel? = null

    override fun onClick(view: View) {}

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.activity_practice_partner_add, container, false)
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
        try {
            mViewModel = ViewModelProvider(requireActivity()).get(NewModel::class.java)
            tv_add_pp = v.findViewById(R.id.tv_add_pp)
            tv_Fname = v.findViewById(R.id.tv_Fname)
            tv_Lname = v.findViewById(R.id.tv_Lname)
            iv_cancel = v.findViewById(R.id.iv_cancel)
            tv_designation = v.findViewById(R.id.tv_designation)
            tv_specialist = v.findViewById(R.id.tv_specialist)
            tv_email = v.findViewById(R.id.tv_email)
            tv_phonenumber = v.findViewById(R.id.tv_phonenumber)
            last_name_layout = v.findViewById(R.id.last_name_layout)
            last_name_layout?.visibility = View.GONE

            et_Fname = v.findViewById(R.id.et_Fname)
            et_Fname?.addTextChangedListener(Validation(et_Fname))

            et_Lname = v.findViewById(R.id.et_Lname)
            et_Lname?.visibility = View.GONE

            et_designation = v.findViewById(R.id.et_designation)
            et_designation?.addTextChangedListener(Validation(et_designation))

            et_specialist = v.findViewById(R.id.et_specialist)
            et_specialist?.addTextChangedListener(Validation(et_specialist))

            response_email = v.findViewById(R.id.response_email)
            tvErrorPhone = v.findViewById(R.id.tvErrorPhone)

            et_email = v.findViewById(R.id.et_email)
            et_email?.addTextChangedListener(Validation(et_email))
            et_email?.addTextChangedListener(object : TextWatcher {
                override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
                    response_email?.visibility = View.GONE
                    isemailhasError = false
                }

                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}

                override fun afterTextChanged(s: Editable?) {
                    val emailText = et_email?.text?.toString()?.trim() ?: ""
                    if (emailText.isNotEmpty()) {
                        if (!AndroidUtils.isValidEmail(emailText)) {
                            response_email?.visibility = View.VISIBLE
                            response_email?.setText(R.string.enter_a_valid_email_address)
                            isemailhasError = true
                        } else {
                            response_email?.visibility = View.GONE
                            isemailhasError = false
                        }
                    }
                }
            })

            et_phonenumber = v.findViewById(R.id.et_phonenumber)
            et_phonenumber?.inputType = InputType.TYPE_CLASS_NUMBER
            et_phonenumber?.addTextChangedListener(Validation(et_phonenumber))
            AndroidUtils.NumberFilter(et_phonenumber, true)

            et_phonenumber?.addTextChangedListener(object : TextWatcher {
                override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
                    tvErrorPhone?.visibility = View.GONE
                    isphonehasError = false
                }

                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}

                override fun afterTextChanged(s: Editable?) {
                    val text = s?.toString() ?: ""
                    if (text.isNotEmpty()) {
                        if (text.length < 10) {
                            tvErrorPhone?.visibility = View.VISIBLE
                            tvErrorPhone?.setText(R.string.please_enter_the_10_digit_mobile_number)
                            isphonehasError = true
                        } else {
                            tvErrorPhone?.visibility = View.GONE
                            isphonehasError = false
                        }
                    }
                }
            })

            btn_cancel_pp = v.findViewById(R.id.btn_cancel_pp)
            btn_save_pp = v.findViewById(R.id.btn_save_pp)

            setData()

            val model = memberProfileModel
            if (model != null && Constants.PpView_Type == "update") {
                mViewModel?.setData(resources.getString(R.string.practice_partners))
                tv_add_pp?.setText(R.string.edit_practice_partners)
                et_Fname?.setText(model.first_name)
                et_designation?.setText(model.designation)
                et_specialist?.setText(model.practice)
                et_email?.setText(model.email)
                et_phonenumber?.setText(model.phone)
            } else {
                mViewModel?.setData(resources.getString(R.string.add_practice_partners))
                tv_add_pp?.setText(R.string.add_practice_partners)
                et_Fname?.setText("")
                et_designation?.setText("")
                et_specialist?.setText("")
                et_email?.setText("")
                et_phonenumber?.setText("")
            }

            btn_save_pp?.setOnClickListener {
                val fName = et_Fname?.text?.toString() ?: ""
                val desg = et_designation?.text?.toString() ?: ""
                val spec = et_specialist?.text?.toString() ?: ""
                val email = et_email?.text?.toString() ?: ""
                val phone = et_phonenumber?.text?.toString() ?: ""

                if (fName.isEmpty() || desg.isEmpty() || spec.isEmpty() || email.isEmpty() || phone.isEmpty() || isphonehasError || isemailhasError) {
                    var msg = "Please enter the"
                    if (fName.isEmpty()) {
                        msg += " First Name"
                    }
                    if (desg.isEmpty()) {
                        msg += if (msg == "Please enter the") " Designation" else ", Designation"
                    }
                    if (spec.isEmpty()) {
                        msg += if (msg == "Please enter the") " Specialist" else ", Specialist"
                    }
                    if (email.isEmpty()) {
                        msg += if (msg == "Please enter the") " Email" else ", Email"
                    } else if (isemailhasError) {
                        msg += if (msg == "Please enter the") " valid Email" else ", valid Email"
                    }
                    if (phone.isEmpty()) {
                        msg += if (msg == "Please enter the") " Phone" else ", Phone"
                    } else if (isphonehasError) {
                        msg += if (msg == "Please enter the") " valid Phone number" else ", valid Phone number"
                    }
                    AndroidUtils.showAlert(msg, activity)
                } else {
                    if (memberProfileModel != null && Constants.PpView_Type == "update") {
                        SaveDetailsPp("update")
                    } else {
                        SaveDetailsPp("add")
                        Constants.PpView_Type = ""
                    }
                }
            }

            btn_cancel_pp?.setOnClickListener { clearDetails() }
            iv_cancel?.setOnClickListener { clearDetails() }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun SaveDetailsPp(save_type: String) {
        progress_dialog = AndroidUtils.get_progress(activity)
        try {
            val jsonObject = JSONObject()
            jsonObject.put("name", et_Fname?.text?.toString() ?: "")
            jsonObject.put("designation", et_designation?.text?.toString() ?: "")
            jsonObject.put("practice", et_specialist?.text?.toString() ?: "")
            jsonObject.put("email", et_email?.text?.toString() ?: "")
            jsonObject.put("phone", et_phonenumber?.text?.toString() ?: "")

            val model = memberProfileModel
            if (save_type == "update" && model != null) {
                WebServiceHelper.callHttpWebService(
                    this,
                    requireContext(),
                    WebServiceHelper.RestMethodType.PUT,
                    "v3/practice-partner/${model.id}",
                    "Update_Pp",
                    jsonObject.toString()
                )
            } else {
                WebServiceHelper.callHttpWebService(
                    this,
                    requireContext(),
                    WebServiceHelper.RestMethodType.POST,
                    "v3/practice-partner",
                    "Add_Pp",
                    jsonObject.toString()
                )
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun setData() {
        tv_add_pp?.setText(R.string.add_practice_partners)
        tv_add_pp?.setTextSize(android.util.TypedValue.COMPLEX_UNIT_SP, DynamicUtils.twenty.toFloat())
        tv_Fname?.setText(R.string.name)
        tv_Lname?.setText(R.string.last_name)
        tv_designation?.setText(R.string.desg)
        tv_specialist?.setText(R.string.specs)
        tv_email?.setText(R.string.email_add)
        tv_phonenumber?.setText(R.string.phone_no)

        et_Fname?.setHint(R.string.name)
        et_Lname?.setHint(R.string.last_name)
        et_designation?.setHint(R.string.desg)
        et_specialist?.setHint(R.string.specs)
        et_email?.setHint(R.string.email_add)
        et_phonenumber?.setHint(R.string.phone_no)
    }

    private fun clearDetails() {
        et_Fname?.setText("")
        et_Lname?.setText("")
        et_designation?.setText("")
        et_specialist?.setText("")
        et_email?.setText("")
        et_phonenumber?.setText("")
        Constants.Edit_OR_View = "View"
        firmProfile.ChangeBackGround()
        mViewModel?.setData(resources.getString(R.string.practice_partners))
        firmProfile.NavFragment(PracticePartnerView.newInstance(firmProfile))
    }

    override fun onAsyncTaskComplete(httpResult: HttpResultDo) {
        if (progress_dialog != null && progress_dialog!!.isShowing) {
            AndroidUtils.dismiss_dialog(progress_dialog)
        }

        val responseContent = httpResult.responseContent ?: ""
        val statusCode = httpResult.status_code
        val requestType = httpResult.requestType ?: ""

        Log.d("Request_Type", requestType)

        try {
            val resultJson = JSONObject(responseContent)

            if (requestType == "Add_Pp") {
                if (statusCode == 401) {
                    val msg = resultJson.optString("msg", "Unauthorized access")
                    AndroidUtils.showAlert(msg, activity)
                    return
                }

                val isError = resultJson.optBoolean("error", false)
                val msg = resultJson.optString(
                    "msg",
                    if (isError) "Something went wrong" else "Operation completed"
                )
                AndroidUtils.showAlert(msg, activity)

                if (!isError) {
                    clearDetails()
                }
            } else if (requestType == "Update_Pp") {
                val isError = resultJson.optBoolean("error", false)
                val msg = resultJson.optString(
                    "msg",
                    if (isError) "Something went wrong" else "Operation completed"
                )
                AndroidUtils.showAlert(msg, activity)

                if (!isError) {
                    clearDetails()
                }
            } else {
                val isError = resultJson.optBoolean("error", false)
                val msg = resultJson.optString(
                    "msg",
                    if (isError) "Something went wrong" else "Operation completed"
                )
                AndroidUtils.showAlert(msg, activity)
            }
        } catch (e: JSONException) {
            e.printStackTrace()
            AndroidUtils.showAlert("Something went wrong", activity)
        } catch (e: Exception) {
            e.printStackTrace()
            AndroidUtils.showAlert("An unexpected error occurred", activity)
        }
    }
}
