package com.digicoffer.lauditor.Appointments.ViewModels

import android.annotation.SuppressLint
import android.app.Dialog
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.text.Editable
import android.text.TextUtils
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.Button
import android.widget.LinearLayout
import androidx.annotation.NonNull
import androidx.annotation.Nullable
import androidx.appcompat.widget.AppCompatButton
import androidx.cardview.widget.CardView
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.RecyclerView
import com.digicoffer.lauditor.Appointments.Adapters.AppointmentsAdapter
import com.digicoffer.lauditor.Appointments.Models.AppointmentModel
import com.digicoffer.lauditor.Appointments.Models.PaymentModel
import com.digicoffer.lauditor.CommonFiles.GlobalFiles.NewModel
import com.digicoffer.lauditor.R
import com.digicoffer.lauditor.Webservice.AsyncTaskCompleteListener
import com.digicoffer.lauditor.Webservice.HttpResultDo
import com.digicoffer.lauditor.Webservice.CommonApiHelper.WebServiceHelper
import com.digicoffer.lauditor.CommonFiles.GlobalFiles.AndroidUtils
import com.digicoffer.lauditor.CommonFiles.GlobalFiles.Constants
import com.google.android.material.textfield.TextInputEditText
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.ArrayList
import java.util.Collections
import java.util.Date
import java.util.Locale

class Appointments : Fragment(), AsyncTaskCompleteListener, View.OnClickListener, AppointmentsAdapter.InterfaceListener {

    private var linear_notes: LinearLayout? = null
    private var ll_nav_buttons: LinearLayout? = null
    private var tl_search_matter: CardView? = null
    private var rv_appointments_list: RecyclerView? = null
    private var btn_send_request: Button? = null
    private var progressDialog: Dialog? = null
    private var tl_search_appointments: View? = null
    private var btn_prev: AppCompatButton? = null
    private var btn_next: AppCompatButton? = null
    private var btn_search: AppCompatButton? = null
    private var et_search_appointments: TextInputEditText? = null

    private var ll_empty_state: LinearLayout? = null
    private var ll_search_empty_state: LinearLayout? = null

    private var mViewModel: NewModel? = null

    private var selectedAppointment: AppointmentModel? = null
    private var pendingHighlightIds = ""

    private val masterAppointmentList = ArrayList<AppointmentModel>()
    private val filteredAppointmentList = ArrayList<AppointmentModel>()
    private val currentPageList = ArrayList<AppointmentModel>()

    private var currentPage = 0
    private var totalPages = 0

    companion object {
        private const val PAGE_SIZE = 10
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mViewModel = ViewModelProvider(requireActivity()).get(NewModel::class.java)
    }

    private fun setViewModelData(data: String) {
        mViewModel?.setData(data)
    }

    override fun onResume() {
        super.onResume()
        requireActivity().title = getString(R.string.appointments)
        requireActivity().window.setSoftInputMode(
            WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN
        )
    }

    override fun onPause() {
        super.onPause()
        requireActivity().window.setSoftInputMode(
            WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE
        )
    }

    @SuppressLint("WrongViewCast")
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        setViewModelData(getString(R.string.appointments))
        requireActivity().title = getString(R.string.appointments)

        val view = inflater.inflate(R.layout.view_appointments, container, false)

        rv_appointments_list = view.findViewById(R.id.rv_appointments_list)
        ll_nav_buttons = view.findViewById(R.id.ll_nav_buttons)
        btn_prev = view.findViewById(R.id.btn_prev)
        btn_next = view.findViewById(R.id.btn_next)
        tl_search_matter = view.findViewById(R.id.ll_search_appointments)
        btn_search = tl_search_matter?.findViewById(R.id.btn_search)
        tl_search_appointments = view.findViewById(R.id.tl_search_appointments)
        et_search_appointments = tl_search_appointments?.findViewById(R.id.et_Search)
        et_search_appointments?.setHint(R.string.search_appointments)

        ll_empty_state = view.findViewById(R.id.ll_empty_state)
        ll_search_empty_state = view.findViewById(R.id.ll_search_empty_state)

        ll_nav_buttons?.visibility = View.VISIBLE
        btn_prev?.setText(R.string.prev_)
        btn_next?.setText(R.string.next_)

        AndroidUtils.ToggleButton(0, btn_prev)
        AndroidUtils.ToggleButton(0, btn_next)

        btn_prev?.setOnClickListener(this)
        btn_next?.setOnClickListener(this)
        btn_search?.setOnClickListener(this)

        showEmptyState(true, false)

        et_search_appointments?.addTextChangedListener(object : TextWatcher {
            private val handler = Handler(Looper.getMainLooper())
            private var searchRunnable: Runnable? = null

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                if (searchRunnable != null) {
                    handler.removeCallbacks(searchRunnable!!)
                }
                val query = s?.toString()?.trim() ?: ""
                searchRunnable = Runnable { applySearchFilter(query) }
                handler.postDelayed(searchRunnable!!, 300)
            }
            override fun afterTextChanged(s: Editable?) {}
        })

        callAppointmentList()

        handleNotificationNavigation()

        return view
    }

    private fun showEmptyState(showEmpty: Boolean, isSearching: Boolean) {
        if (showEmpty) {
            rv_appointments_list?.visibility = View.GONE
            ll_nav_buttons?.visibility = View.GONE

            if (isSearching) {
                ll_search_empty_state?.visibility = View.VISIBLE
                ll_empty_state?.visibility = View.GONE
            } else {
                ll_empty_state?.visibility = View.VISIBLE
                ll_search_empty_state?.visibility = View.GONE
            }
        } else {
            rv_appointments_list?.visibility = View.VISIBLE
            ll_empty_state?.visibility = View.GONE
            ll_search_empty_state?.visibility = View.GONE
            if (filteredAppointmentList.isNotEmpty()) {
                ll_nav_buttons?.visibility = View.VISIBLE
            }
        }
    }

    private fun updateEmptyState() {
        val hasItems = filteredAppointmentList.isNotEmpty()
        val searchQuery = et_search_appointments?.text?.toString()?.trim() ?: ""
        val isSearching = !TextUtils.isEmpty(searchQuery)

        if (!hasItems) {
            showEmptyState(true, isSearching)
        } else {
            showEmptyState(false, false)
        }
    }

    private fun handleNotificationNavigation() {
        val bundle = Constants.notificationBundle
        if (bundle != null) {
            val route = bundle.getString(Constants.NavKeys.ROUTE_NAME)
            pendingHighlightIds = bundle.getString(Constants.NavKeys.APPOINTMENT_ID) ?: ""
            Constants.isFromNotification = false
            Constants.notificationBundle.clear()
        }
    }

    private fun callAppointmentList() {
        try {
            progressDialog = AndroidUtils.get_progress(requireActivity())
            val postData = JSONObject()
            WebServiceHelper.callHttpWebService(
                this,
                requireContext(),
                WebServiceHelper.RestMethodType.GET,
                "v3/appointments",
                "Appointments_List",
                postData.toString()
            )
        } catch (e: Exception) {
            if (progressDialog != null && progressDialog!!.isShowing) {
                AndroidUtils.dismiss_dialog(progressDialog)
            }
            e.printStackTrace()
        }
    }

    private fun callCancelAppointments(appointmentModel: AppointmentModel) {
        try {
            progressDialog = AndroidUtils.get_progress(requireActivity())
            val postData = JSONObject()
            WebServiceHelper.callHttpWebService(
                this,
                requireContext(),
                WebServiceHelper.RestMethodType.DELETE,
                "v3/appointments/${appointmentModel.id}/cancel",
                "Cancel_Appointments",
                postData.toString()
            )
        } catch (e: Exception) {
            if (progressDialog != null && progressDialog!!.isShowing) {
                AndroidUtils.dismiss_dialog(progressDialog)
            }
            e.printStackTrace()
        }
    }

    private fun callDeleteAppointment(appointmentModel: AppointmentModel) {
        try {
            progressDialog = AndroidUtils.get_progress(requireActivity())
            val postData = JSONObject()
            WebServiceHelper.callHttpWebService(
                this,
                requireContext(),
                WebServiceHelper.RestMethodType.DELETE,
                "v3/appointments/${appointmentModel.id}/delete",
                "Delete_Appointment",
                postData.toString()
            )
            Log.d("Delete_Payload", postData.toString())
        } catch (e: Exception) {
            if (progressDialog != null && progressDialog!!.isShowing) {
                AndroidUtils.dismiss_dialog(progressDialog)
            }
            e.printStackTrace()
        }
    }

    private fun loadAppointmentsList(appointments: JSONArray) {
        try {
            masterAppointmentList.clear()

            for (i in 0 until appointments.length()) {
                val jsonObject = appointments.optJSONObject(i) ?: continue

                val appointmentModel = AppointmentModel()
                appointmentModel.id = jsonObject.optString("id", "")
                appointmentModel.client_id = jsonObject.optString("client_id", "")
                appointmentModel.guid = jsonObject.optString("guid", "")
                appointmentModel.client_name = jsonObject.optString("client_name", "")
                appointmentModel.appointment_from = jsonObject.optString("appointment_from", "")
                appointmentModel.appointment_to = jsonObject.optString("appointment_to", "")
                appointmentModel.consultation_mode = jsonObject.optString("consultation_mode", "")
                appointmentModel.appointment_status = jsonObject.optString("appointment_status", "")
                appointmentModel.meeting_room_id = jsonObject.optString("meeting_room_id", "")
                appointmentModel.meeting_room_expires_at = jsonObject.optString("meeting_room_expires_at", "")
                appointmentModel.rsvp_status = jsonObject.optString("rsvp_status", "")
                appointmentModel.created_at = jsonObject.optString("created_at", "")

                if (jsonObject.has("client_profile_pic")) {
                    appointmentModel.client_profile_pic = jsonObject.optString("client_profile_pic", "")
                }
                if (jsonObject.has("services_offered")) {
                    appointmentModel.services_offered = jsonObject.optJSONArray("services_offered") ?: JSONArray()
                }
                if (jsonObject.has("payment")) {
                    val paymentObject = jsonObject.getJSONObject("payment")
                    val payment = PaymentModel()
                    payment.status = paymentObject.optString("status")
                    payment.amount_paid = paymentObject.optString("amount_paid")
                    payment.currency = paymentObject.optString("currency")
                    payment.symbol = paymentObject.optString("symbol")
                    payment.label = paymentObject.optString("label")
                    appointmentModel.payment = payment
                }

                masterAppointmentList.add(appointmentModel)
            }

            Handler(Looper.getMainLooper()).post {
                applySortToList(masterAppointmentList)

                filteredAppointmentList.clear()
                filteredAppointmentList.addAll(masterAppointmentList)

                currentPage = 0
                calculateTotalPages()

                renderCurrentPage()

                updateEmptyState()
            }

        } catch (e: JSONException) {
            AndroidUtils.showAlert(e.message, requireActivity())
            e.printStackTrace()
            showEmptyState(true, false)
        }
    }

    private fun applySortToList(list: List<AppointmentModel>) {
        val dateFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.ENGLISH)

        Collections.sort(list) { o1, o2 ->
            try {
                val status1 = o1.appointment_status.lowercase(Locale.ROOT)
                val status2 = o2.appointment_status.lowercase(Locale.ROOT)

                val isPriority1 = status1 == "upcoming" || status1 == "ongoing"
                val isPriority2 = status2 == "upcoming" || status2 == "ongoing"

                // Both are upcoming/ongoing → ASCENDING (earliest time first)
                if (isPriority1 && isPriority2) {
                    val d1 = dateFormat.parse(o1.appointment_from)
                    val d2 = dateFormat.parse(o2.appointment_from)
                    if (d1 == null || d2 == null) return@sort 0
                    return@sort d1.compareTo(d2)
                }

                // Only o1 is priority → o1 goes to the top
                if (isPriority1) return@sort -1

                // Only o2 is priority → o2 goes to the top
                if (isPriority2) return@sort 1

                // Neither is priority → DESCENDING (most recent first)
                val d1 = dateFormat.parse(o1.appointment_from)
                val d2 = dateFormat.parse(o2.appointment_from)
                if (d1 == null || d2 == null) return@sort 0
                return@sort d2.compareTo(d1)

            } catch (e: ParseException) {
                e.printStackTrace()
                return@sort 0
            }
        }
    }

    private fun applySearchFilter(query: String) {
        filteredAppointmentList.clear()

        if (query.isEmpty()) {
            filteredAppointmentList.addAll(masterAppointmentList)
        } else {
            val lowerQuery = query.lowercase(Locale.ROOT)
            for (model in masterAppointmentList) {
                val clientName = model.client_name.lowercase(Locale.ROOT)
                val status = model.appointment_status.lowercase(Locale.ROOT)
                val consultationMode = model.consultation_mode.lowercase(Locale.ROOT)

                if (clientName.contains(lowerQuery)
                    || status.contains(lowerQuery)
                    || consultationMode.contains(lowerQuery)
                ) {
                    filteredAppointmentList.add(model)
                }
            }
        }

        applySortToList(filteredAppointmentList)

        currentPage = 0
        calculateTotalPages()
        renderCurrentPage()

        updateEmptyState()
    }

    private fun calculateTotalPages() {
        val total = filteredAppointmentList.size
        totalPages = if (total == 0) {
            0
        } else {
            Math.ceil(total.toDouble() / PAGE_SIZE).toInt()
        }
    }

    private fun renderCurrentPage() {
        currentPageList.clear()

        val startIndex = currentPage * PAGE_SIZE
        val endIndex = Math.min(startIndex + PAGE_SIZE, filteredAppointmentList.size)

        for (i in startIndex until endIndex) {
            currentPageList.add(filteredAppointmentList[i])
        }

        loadAppointmentsRecyclerview(currentPageList)
        updatePaginationButtons()
    }

    private fun updatePaginationButtons() {
        if (currentPage > 0) {
            AndroidUtils.ToggleButton(1, btn_prev)
        } else {
            AndroidUtils.ToggleButton(0, btn_prev)
        }

        if (currentPage < totalPages - 1) {
            AndroidUtils.ToggleButton(1, btn_next)
        } else {
            AndroidUtils.ToggleButton(0, btn_next)
        }
    }

    private fun loadAppointmentsRecyclerview(pageList: ArrayList<AppointmentModel>) {
        try {
            rv_appointments_list?.removeAllViews()

            val appointmentsAdapter = AppointmentsAdapter(
                pageList,
                requireContext(),
                this,
                pendingHighlightIds,
                requireActivity()
            )

            rv_appointments_list?.adapter = appointmentsAdapter
            AndroidUtils.LoadingRecyclerview(rv_appointments_list, requireContext())
            AndroidUtils.setupBottomSpacerFooter(
                rv_appointments_list,
                resources.getDimensionPixelSize(R.dimen.twentyeight_dp)
            )

        } catch (e: Exception) {
            AndroidUtils.showAlert(e.message, requireActivity())
        }
    }

    override fun onClick(view: View) {
        val id = view.id

        if (id == R.id.btn_prev) {
            if (currentPage > 0) {
                currentPage--
                renderCurrentPage()
            }
        } else if (id == R.id.btn_next) {
            if (currentPage < totalPages - 1) {
                currentPage++
                renderCurrentPage()
            }
        } else if (id == R.id.btn_search) {
            val query = et_search_appointments?.text?.toString()?.trim() ?: ""
            applySearchFilter(query)
        }
    }

    override fun onAsyncTaskComplete(httpResult: HttpResultDo) {
        AndroidUtils.dismiss_dialog(progressDialog)

        try {
            if (httpResult.result == WebServiceHelper.ServiceCallStatus.Success) {
                val result = JSONObject(httpResult.responseContent)
                val error = result.getBoolean("error")

                // ─── Handle Appointments_List response ────────────────────────
                if (httpResult.requestType == "Appointments_List") {
                    if (error) {
                        val msg = result.getString("msg")
                        AndroidUtils.showAlert(msg, requireActivity())
                        showEmptyState(true, false)
                    } else {
                        val appointments = result.optJSONArray("appointments")
                        if (appointments != null && appointments.length() > 0) {
                            loadAppointmentsList(appointments)
                        } else {
                            masterAppointmentList.clear()
                            filteredAppointmentList.clear()
                            currentPageList.clear()
                            currentPage = 0
                            totalPages = 0
                            loadAppointmentsRecyclerview(currentPageList)
                            updatePaginationButtons()
                            showEmptyState(true, false)
                        }
                    }
                }

                // ─── Handle Cancel_Appointments response ──────────────────────
                else if (httpResult.requestType == "Cancel_Appointments") {
                    val msg = result.getString("msg")
                    AndroidUtils.showAlert(msg, requireActivity())
                    if (!error) {
                        callAppointmentList()
                    }
                }

                // ─── Handle Delete_Appointment response ───────────────────────
                else if (httpResult.requestType == "Delete_Appointment") {
                    val msg = result.getString("msg")
                    AndroidUtils.showAlert(msg, requireActivity())
                    if (!error) {
                        callAppointmentList()
                    }
                }

            } else {
                val result = JSONObject(httpResult.responseContent)
                val msg = result.getString("msg")
                AndroidUtils.showAlert(msg, requireActivity())
                showEmptyState(true, false)
            }

        } catch (e: JSONException) {
            AndroidUtils.showAlert(e.message, requireActivity())
            e.printStackTrace()
            showEmptyState(true, false)
        }
    }

    override fun ViewAppointmentHistory(
        appointmentModel: AppointmentModel,
        itemsArrayList: ArrayList<AppointmentModel>
    ) {
        selectedAppointment = appointmentModel

        ll_nav_buttons?.visibility = View.GONE
        rv_appointments_list?.visibility = View.GONE
        tl_search_matter?.visibility = View.GONE
        tl_search_appointments?.visibility = View.GONE

        ll_empty_state?.visibility = View.GONE
        ll_search_empty_state?.visibility = View.GONE

        val historyContainer = requireView().findViewById<View>(R.id.fl_appointment_history)
        historyContainer?.visibility = View.VISIBLE

        val historyFragment = AppointmentHistory()
        val bundle = Bundle()
        bundle.putString("client_id", selectedAppointment?.client_id)
        bundle.putString("client_name", selectedAppointment?.client_name)
        bundle.putString("client_profile_pic", selectedAppointment?.client_profile_pic)
        historyFragment.arguments = bundle

        historyFragment.setOnHistoryCloseListener(object : AppointmentHistory.OnHistoryCloseListener {
            override fun onHistoryClosed() {
                rv_appointments_list?.visibility = View.VISIBLE
                ll_nav_buttons?.visibility = View.VISIBLE
                tl_search_appointments?.visibility = View.VISIBLE
                historyContainer?.visibility = View.GONE

                updateEmptyState()

                requireActivity().title = getString(R.string.appointments)
            }
        })

        childFragmentManager.beginTransaction()
            .replace(R.id.fl_appointment_history, historyFragment)
            .addToBackStack("appointment_history")
            .commit()
    }

    override fun CancelAppointment(appointmentModel: AppointmentModel) {
        AndroidUtils.showConfirmationDialog(
            requireContext(),
            "Confirmation",
            "Are you sure you want to cancel this appointment? This action cannot be undone.",
            object : AndroidUtils.OnConfirmListener {
                override fun onSave() {
                    callCancelAppointments(appointmentModel)
                }

                override fun onCancel() {}
            }
        )
    }

    override fun DeleteAppointment(appointmentModel: AppointmentModel) {
        AndroidUtils.showConfirmationDialog(
            requireContext(),
            "Confirmation",
            "Are you sure you want to delete this appointment? This action cannot be undone.",
            object : AndroidUtils.OnConfirmListener {
                override fun onSave() {
                    callDeleteAppointment(appointmentModel)
                }

                override fun onCancel() {}
            }
        )
    }
}
