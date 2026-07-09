package com.digicoffer.lauditor.Appointments.ViewModels

import android.app.Dialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.annotation.NonNull
import androidx.annotation.Nullable
import androidx.appcompat.widget.AppCompatButton
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.digicoffer.lauditor.Appointments.Adapters.AppointmentHistoryAdapter
import com.digicoffer.lauditor.Appointments.Models.AppointmentModel
import com.digicoffer.lauditor.Appointments.Models.PaymentModel
import com.digicoffer.lauditor.R
import com.digicoffer.lauditor.Webservice.AsyncTaskCompleteListener
import com.digicoffer.lauditor.Webservice.HttpResultDo
import com.digicoffer.lauditor.Webservice.CommonApiHelper.WebServiceHelper
import com.digicoffer.lauditor.CommonFiles.GlobalFiles.AndroidUtils
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject
import java.util.ArrayList

class AppointmentHistory : Fragment(), AsyncTaskCompleteListener, AppointmentHistoryAdapter.OnNoteActionListener {

    private var rvAppointmentsTimeline: RecyclerView? = null
    private var tvClientName: TextView? = null
    private var tv_appointment_history: TextView? = null
    private var person_icon: TextView? = null
    private var iv_close_history: ImageView? = null
    private var iv_profile: ImageView? = null
    private var btnTimeline: AppCompatButton? = null
    private var progressDialog: Dialog? = null
    private val historyList = ArrayList<AppointmentModel>()
    private var adapter: AppointmentHistoryAdapter? = null
    private var clientId = ""
    private var clientName = ""
    private var client_profile_pic = ""

    interface OnHistoryCloseListener {
        fun onHistoryClosed()
    }

    private var closeListener: OnHistoryCloseListener? = null

    fun setOnHistoryCloseListener(listener: OnHistoryCloseListener?) {
        this.closeListener = listener
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (arguments != null) {
            clientName = requireArguments().getString("client_name", "")
            clientId = requireArguments().getString("client_id", "")
            client_profile_pic = requireArguments().getString("client_profile_pic", "")
        }
        callAppointmentHistoryList(clientId)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.appointment_history, container, false)
        initializeViews(view)
        return view
    }

    private fun callAppointmentHistoryList(client_id: String) {
        try {
            progressDialog = AndroidUtils.get_progress(requireActivity())
            val postData = JSONObject()
            WebServiceHelper.callHttpWebService(
                this,
                requireContext(),
                WebServiceHelper.RestMethodType.GET,
                "v3/appointment/history?client_id=$client_id",
                "Appointments_History",
                postData.toString()
            )
        } catch (e: Exception) {
            if (progressDialog != null && progressDialog!!.isShowing) {
                AndroidUtils.dismiss_dialog(progressDialog)
            }
            e.printStackTrace()
        }
    }

    private fun initializeViews(view: View) {
        rvAppointmentsTimeline = view.findViewById(R.id.rv_appointments_timeline)
        tvClientName = view.findViewById(R.id.tv_client_name)
        tv_appointment_history = view.findViewById(R.id.tv_appointment_history)
        btnTimeline = view.findViewById(R.id.btn_timeline)
        iv_close_history = view.findViewById(R.id.iv_close_history)
        iv_profile = view.findViewById(R.id.iv_profile)
        person_icon = view.findViewById(R.id.person_icon)

        tv_appointment_history?.setText(R.string.appointments_history)
        tvClientName?.text = clientName
        AndroidUtils.loadProfileImage(requireContext(), client_profile_pic, iv_profile, person_icon, clientName)
        btnTimeline?.isSelected = true

        iv_close_history?.setOnClickListener {
            parentFragmentManager.popBackStack()
            closeListener?.onHistoryClosed()
        }
    }

    private fun setupRecyclerView() {
        rvAppointmentsTimeline?.layoutManager = LinearLayoutManager(requireContext())
        adapter = AppointmentHistoryAdapter(historyList, requireContext(), this)
        rvAppointmentsTimeline?.adapter = adapter
        AndroidUtils.LoadAnimation(rvAppointmentsTimeline, requireContext())
        adapter?.notifyDataSetChanged()
    }

    private fun callDeleteHistoryNotes(id: String, notes_id: String) {
        try {
            progressDialog = AndroidUtils.get_progress(requireActivity())
            val postData = JSONObject()
            postData.put("note_id", notes_id)
            WebServiceHelper.callHttpWebService(
                this,
                requireContext(),
                WebServiceHelper.RestMethodType.DELETE,
                "v3/appointments/$id/notes",
                "Delete_Notes",
                postData.toString()
            )
        } catch (e: Exception) {
            if (progressDialog != null && progressDialog!!.isShowing) {
                AndroidUtils.dismiss_dialog(progressDialog)
            }
            e.printStackTrace()
        }
    }

    private fun callUpdateHistoryNotes(id: String, notes_id: String, notes: String) {
        try {
            progressDialog = AndroidUtils.get_progress(requireActivity())
            val postData = JSONObject()
            postData.put("note_id", notes_id)
            postData.put("note", notes)
            postData.put("note_details", "")
            WebServiceHelper.callHttpWebService(
                this,
                requireContext(),
                WebServiceHelper.RestMethodType.PATCH,
                "v3/appointments/$id/notes",
                "Update_Notes",
                postData.toString()
            )
        } catch (e: Exception) {
            if (progressDialog != null && progressDialog!!.isShowing) {
                AndroidUtils.dismiss_dialog(progressDialog)
            }
            e.printStackTrace()
        }
    }

    private fun loadAppointmentHistoryNotes(id: String, notes: String) {
        try {
            progressDialog = AndroidUtils.get_progress(requireActivity())
            val postData = JSONObject()
            postData.put("note", notes)
            postData.put("note_details", "")
            WebServiceHelper.callHttpWebService(
                this,
                requireContext(),
                WebServiceHelper.RestMethodType.POST,
                "v3/appointments/$id/notes",
                "Appointments_History_Notes",
                postData.toString()
            )
        } catch (e: Exception) {
            if (progressDialog != null && progressDialog!!.isShowing) {
                AndroidUtils.dismiss_dialog(progressDialog)
            }
            e.printStackTrace()
        }
    }

    override fun onSaveNote(appointment: AppointmentModel, note: String, position: Int) {
        loadAppointmentHistoryNotes(appointment.id, note)
    }

    override fun onCancelNote(appointment: AppointmentModel, position: Int) {
        // Handle cancel action if needed
    }

    override fun onEditNote(appointment: AppointmentModel, noteId: String, noteText: String, position: Int) {
        callUpdateHistoryNotes(appointment.id, noteId, noteText)
    }

    override fun onDeleteNote(appointment: AppointmentModel, noteId: String, position: Int) {
        callDeleteHistoryNotes(appointment.id, noteId)
    }

    override fun onClick(view: View) {
        // Handle views click
    }

    override fun onAsyncTaskComplete(httpResult: HttpResultDo) {
        AndroidUtils.dismiss_dialog(progressDialog)

        try {
            if (httpResult.result == WebServiceHelper.ServiceCallStatus.Success) {
                val result = JSONObject(httpResult.responseContent)
                val error = result.getBoolean("error")

                if (httpResult.requestType == "Appointments_History") {
                    historyList.clear()
                    if (error) {
                        AndroidUtils.showAlert(result.getString("msg"), requireActivity())
                    } else {
                        val historyArray = result.optJSONArray("appointments")
                        if (historyArray != null) {
                            for (i in 0 until historyArray.length()) {
                                val obj = historyArray.optJSONObject(i) ?: continue
                                val historyModel = AppointmentModel()

                                historyModel.id = obj.optString("id")
                                historyModel.appointment_from = obj.optString("appointment_from")
                                historyModel.appointment_to = obj.optString("appointment_to")
                                historyModel.appointment_status = obj.optString("appointment_status")
                                historyModel.created_at = obj.optString("created_at")
                                historyModel.meeting_room_id = obj.optString("meeting_room_id")
                                historyModel.meeting_room_expires_at = obj.optString("meeting_room_expires_at")

                                // Notes
                                if (obj.has("notes")) {
                                    historyModel.notes = obj.optJSONArray("notes") ?: JSONArray()
                                }

                                // Payment
                                if (obj.has("payment")) {
                                    val paymentObj = obj.getJSONObject("payment")
                                    val payment = PaymentModel()
                                    payment.status = paymentObj.optString("status")
                                    payment.amount_paid = paymentObj.optString("amount_paid")
                                    payment.currency = paymentObj.optString("currency")
                                    payment.symbol = paymentObj.optString("symbol")
                                    payment.label = paymentObj.optString("label")
                                    historyModel.payment = payment
                                }

                                historyList.add(historyModel)
                            }
                            setupRecyclerView()
                        }
                    }
                } else if (httpResult.requestType == "Appointments_History_Notes") {
                    val msg = result.getString("msg")
                    if (error) {
                        AndroidUtils.showAlert(msg, requireActivity())
                    } else {
                        AndroidUtils.showAlert(msg, requireActivity())
                        callAppointmentHistoryList(clientId)
                    }
                } else if (httpResult.requestType == "Update_Notes") {
                    val msg = result.getString("msg")
                    if (error) {
                        AndroidUtils.showAlert(msg, requireActivity())
                    } else {
                        AndroidUtils.showAlert(msg, requireActivity(), "Success")
                        callAppointmentHistoryList(clientId)
                    }
                } else if (httpResult.requestType == "Delete_Notes") {
                    val msg = result.getString("msg")
                    if (error) {
                        AndroidUtils.showAlert(msg, requireActivity())
                    } else {
                        AndroidUtils.showAlert(msg, requireActivity())
                        callAppointmentHistoryList(clientId)
                    }
                }
            }
        } catch (e: JSONException) {
            AndroidUtils.showAlert(e.message, requireActivity())
            e.printStackTrace()
        }
    }
}
