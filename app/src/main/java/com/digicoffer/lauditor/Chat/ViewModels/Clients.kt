package com.digicoffer.lauditor.Chat.ViewModels

import android.app.AlertDialog
import android.app.Dialog
import android.content.SharedPreferences
import android.os.AsyncTask
import android.os.Bundle
import android.preference.PreferenceManager
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.RecyclerView
import com.digicoffer.lauditor.Chat.Adapters.ChatAdapter
import com.digicoffer.lauditor.Chat.Model.ChildDO
import com.digicoffer.lauditor.Chat.Model.ClientRelationshipsDo
import com.digicoffer.lauditor.Dashboard.DahboardModels.UnreadCountModel
import com.digicoffer.lauditor.R
import com.digicoffer.lauditor.Webservice.AsyncTaskCompleteListener
import com.digicoffer.lauditor.Webservice.HttpResultDo
import com.digicoffer.lauditor.Webservice.CommonApiHelper.WebServiceHelper
import com.digicoffer.lauditor.CommonFiles.GlobalFiles.AndroidUtils
import com.digicoffer.lauditor.CommonFiles.GlobalFiles.Constants
import com.digicoffer.lauditor.CommonFiles.ValidationUtils.DescriptionValidation
import com.google.android.material.textfield.TextInputEditText
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject
import java.io.File
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL
import java.util.ArrayList
import java.util.Collections
import java.util.Objects

class Clients : Fragment(), AsyncTaskCompleteListener, ChatAdapter.EventListener {

    private var progress_dialog: Dialog? = null
    private var rv_Clientrelationships: RecyclerView? = null
    private var et_Search: TextInputEditText? = null
    private var ad_dialog: AlertDialog? = null
    private var tv_chat: TextView? = null

    private var layout_empty_state: LinearLayout? = null
    private var tv_empty_subtitle: TextView? = null

    private var Clientlist = ArrayList<ClientRelationshipsDo>()
    private var Corporate_Client_list = ArrayList<ClientRelationshipsDo>()
    private var Client_list = ArrayList<ClientRelationshipsDo>()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.client, container, false)
        Constants.Chat_id = ""

        rv_Clientrelationships = view.findViewById(R.id.rv_clientrelationships)
        Constants.recyclerView = rv_Clientrelationships

        val tl_Search = view.findViewById<View>(R.id.tl_Search)
        et_Search = tl_Search.findViewById(R.id.et_Search)

        tv_chat = view.findViewById(R.id.tv_chat)
        tv_chat?.setText(R.string.list_of_clients)

        layout_empty_state = view.findViewById(R.id.layout_empty_state)
        tv_empty_subtitle = view.findViewById(R.id.tv_empty_subtitle)
        tv_empty_subtitle?.text = "There are no clients under here."

        et_Search?.setHint(R.string.type_to_search)
        et_Search?.addTextChangedListener(DescriptionValidation(et_Search))

        callCorporateWebservice()
        return view
    }

    override fun onClick(view: View) {}

    private fun callWebservice() {
        progress_dialog = AndroidUtils.get_progress(activity)
        val postData = JSONObject()
        try {
            WebServiceHelper.callHttpWebService(
                this, requireContext(),
                WebServiceHelper.RestMethodType.GET,
                "v2/relationship/client/list?include_appointment_consumers=True",
                "CLIENT_RELATIONSHIP",
                postData.toString()
            )
        } catch (e: Exception) {
            progress_dialog?.let {
                if (it.isShowing) AndroidUtils.dismiss_dialog(it)
            }
        }
    }

    private fun callCorporateWebservice() {
        progress_dialog = AndroidUtils.get_progress(activity)
        val postData = JSONObject()
        try {
            WebServiceHelper.callHttpWebService(
                this, requireContext(),
                WebServiceHelper.RestMethodType.GET,
                "v3/corporate/list",
                "CORPORATE_RELATIONSHIP",
                postData.toString()
            )
        } catch (e: Exception) {
            progress_dialog?.let {
                if (it.isShowing) AndroidUtils.dismiss_dialog(it)
            }
        }
    }

    override fun onAsyncTaskComplete(httpResult: HttpResultDo) {
        progress_dialog?.let {
            if (it.isShowing) AndroidUtils.dismiss_dialog(it)
        }

        if (httpResult.result == WebServiceHelper.ServiceCallStatus.Success) {
            try {
                val result = JSONObject(httpResult.responseContent)

                if (httpResult.requestType == "CLIENT_RELATIONSHIP") {
                    if (!result.optBoolean("error")) {
                        val jsonObject = result.optJSONObject("data")
                        val jsonArray = jsonObject?.optJSONArray("relationships")
                        et_Search?.setText("")
                        if (jsonArray != null) {
                            loadClientRelationshipsData(jsonArray)
                        }
                    } else {
                        AndroidUtils.showValidationALert(
                            "Alert",
                            result.optString("msg"),
                            context
                        )
                    }
                } else if (httpResult.requestType == "CORPORATE_RELATIONSHIP") {
                    if (!result.optBoolean("error")) {
                        val jsonArray = result.getJSONArray("relationships")
                        et_Search?.setText("")
                        loadCorporateList(jsonArray)
                        callWebservice()
                    } else {
                        AndroidUtils.showValidationALert(
                            "Alert",
                            result.optString("msg"),
                            context
                        )
                    }
                }
            } catch (e: JSONException) {
                e.printStackTrace()
            }
        } else if (httpResult.result == WebServiceHelper.ServiceCallStatus.Failed) {
            try {
                val result = JSONObject(httpResult.responseContent)
                AndroidUtils.showErrorAlert(result.optString("msg"), activity)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        } else {
            AndroidUtils.showErrorAlert(
                httpResult.responseContent?.toString() ?: "",
                activity
            )
        }
    }

    private fun loadCorporateList(jsonArray: JSONArray) {
        try {
            Corporate_Client_list.clear()
            for (i in 0 until jsonArray.length()) {
                val jsonObject = jsonArray.getJSONObject(i)
                val clientRelationshipsDo = ClientRelationshipsDo().apply {
                    adminName = jsonObject.optString("adminName")
                    canAccept = jsonObject.optBoolean("canAccept")
                    clientType = jsonObject.optString("type")
                    consent = jsonObject.optString("consent")
                    created = jsonObject.optString("created")
                    guid = jsonObject.optString("client_id")
                    id = jsonObject.optString("id")
                    isAccepted = jsonObject.optBoolean("isAccepted")
                    isClient = jsonObject.optBoolean("isClient")
                    isEditable = jsonObject.optBoolean("isEditable")
                    name = jsonObject.optString("name")
                    rel_id = jsonObject.optString("rel_id")
                    source = jsonObject.optString("source")
                }
                Corporate_Client_list.add(clientRelationshipsDo)
            }
        } catch (e: Exception) {
            e.fillInStackTrace()
        }
    }

    private fun loadClientRelationshipsData(jsonArray: JSONArray) {
        try {
            Clientlist.clear()
            Client_list.clear()

            for (i in 0 until jsonArray.length()) {
                val jsonObject = jsonArray.getJSONObject(i)
                val clientRelationshipsDo = ClientRelationshipsDo().apply {
                    adminName = jsonObject.optString("adminName")
                    canAccept = jsonObject.optBoolean("canAccept")
                    clientType = jsonObject.optString("type")
                    consent = jsonObject.optString("consent")
                    created = jsonObject.optString("created")
                    guid = jsonObject.optString("guid")
                    id = jsonObject.optString("id")
                    isAccepted = jsonObject.optBoolean("isAccepted")
                    isClient = jsonObject.optBoolean("isClient")
                    isEditable = jsonObject.optBoolean("isEditable")
                    name = jsonObject.optString("name")
                    rel_id = jsonObject.optString("rel_id")
                    source = jsonObject.optString("source")
                }
                Clientlist.add(clientRelationshipsDo)
            }

            Clientlist.addAll(Corporate_Client_list)
            loadRecycleView()
        } catch (e: Exception) {
            e.fillInStackTrace()
        }
    }

    private fun loadRecycleView() {
        applyUnreadCounts(Clientlist)
        sortClientsByUnreadCount(Clientlist)
        updateEmptyState(Clientlist.isEmpty())

        if (Clientlist.isEmpty()) return

        val adapter = ChatAdapter(Clientlist, this, requireContext(), requireActivity())
        rv_Clientrelationships?.adapter = adapter
        AndroidUtils.LoadingRecyclerview(rv_Clientrelationships, context)
        rv_Clientrelationships?.let {
            AndroidUtils.setupBottomSpacerFooter(
                it,
                resources.getDimensionPixelSize(R.dimen.twentyeight_dp)
            )
        }

        et_Search?.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                adapter.filter.filter(s?.toString() ?: "")
                rv_Clientrelationships?.post {
                    updateEmptyState(adapter.itemCount == 0)
                }
            }
        })
    }

    private fun updateEmptyState(isEmpty: Boolean) {
        if (isEmpty) {
            rv_Clientrelationships?.visibility = View.GONE
            layout_empty_state?.visibility = View.VISIBLE
        } else {
            rv_Clientrelationships?.visibility = View.VISIBLE
            layout_empty_state?.visibility = View.GONE
        }
    }

    override fun view_users(jid: String?, name: String?, clientType: String?) {
        val frag = MessagesList(rv_Clientrelationships)
        val bundle = Bundle().apply {
            putString("EXTRA_CONTACT_JID", jid)
            putString("EXTRA_CONTACT_NAME", name)
            putString("EXTRA_CONTACT_TYPE", clientType)
        }
        frag.arguments = bundle
        activity?.supportFragmentManager
            ?.beginTransaction()
            ?.replace(R.id.id_framelayout, frag)
            ?.addToBackStack(null)
            ?.commit()
    }

    fun Message(childDO: ChildDO) {
        try {
            val jid = childDO.uid ?: ""
            val pref = PreferenceManager.getDefaultSharedPreferences(context)
            val currentJID = pref.getString("xmpp_jid", null) ?: ""
            Log.d("jid+currentJID", "${jid}_$currentJID")
            if (childDO.id.isNullOrEmpty()) {
                ChatHistoryTask(currentJID, jid).execute("")
                move_message_fragment("", childDO.name ?: "", jid, childDO.source ?: "")
            } else {
                ChatHistoryTask(childDO.id ?: "", jid).execute("")
                move_message_fragment(childDO.id ?: "", childDO.name ?: "", jid, childDO.source ?: "")
            }
        } catch (e: Exception) {
            Log.d("Error:", e.message ?: "")
            e.fillInStackTrace()
        }
    }

    private fun move_message_fragment(tmid: String, name: String, jid: String, source: String) {
        try {
            val xmppJid = if (tmid == "") jid else "${jid}_$tmid"
            Log.d("xmpp_jid", xmppJid)
            val frag = MessagesList(rv_Clientrelationships)
            val bundle = Bundle().apply {
                putString("EXTRA_CONTACT_JID", xmppJid)
                putString("EXTRA_CONTACT_NAME", name)
                putString("EXTRA_CONTACT_TYPE", source)
            }
            frag.arguments = bundle
            activity?.supportFragmentManager
                ?.beginTransaction()
                ?.replace(R.id.id_framelayout, frag)
                ?.addToBackStack(null)
                ?.commit()
        } catch (e: Exception) {
            AndroidUtils.logMsg(e.message)
        }
    }

    inner class ChatHistoryTask(currentJID: String, JID: String) : AsyncTask<String, String, String>() {
        private val XMPP_DOMAIN = "https://${Constants.XMPP_DOMAIN}/"
        private var url = ""

        init {
            this.url = "$XMPP_DOMAIN/unread/$currentJID${File.separator}$JID"
        }

        override fun doInBackground(vararg strings: String?): String {
            return requestUnreadCount(url)
        }

        override fun onPostExecute(response: String?) {
            try {
                val jsonResponse = JSONObject(response ?: "")
                if (!jsonResponse.optBoolean("error")) {
                    Log.d(
                        "count_message",
                        jsonResponse.getJSONObject("data").optString("count")
                    )
                }
            } catch (e: Exception) {
                e.message
            }
        }
    }

    private fun requestUnreadCount(url: String): String {
        var data = ""
        var httpURLConnection: HttpURLConnection? = null
        try {
            httpURLConnection = URL(url).openConnection() as HttpURLConnection
            httpURLConnection.setRequestProperty("Authorization", "Bearer ${Constants.TOKEN}")
            httpURLConnection.requestMethod = "GET"
            val statusCode = httpURLConnection.responseCode
            if (statusCode == 200) {
                val `in` = httpURLConnection.inputStream
                val inputStreamReader = InputStreamReader(`in`)
                var inputStreamData = inputStreamReader.read()
                val sb = StringBuilder()
                while (inputStreamData != -1) {
                    val current = inputStreamData.toChar()
                    inputStreamData = inputStreamReader.read()
                    sb.append(current)
                }
                data = sb.toString()
            } else {
                AndroidUtils.showAlert("Err connection, Please try again", activity)
            }
        } catch (e: Exception) {
            AndroidUtils.logMsg(e.message)
        } finally {
            httpURLConnection?.disconnect()
        }
        return data
    }

    private fun sortClientsByUnreadCount(list: ArrayList<ClientRelationshipsDo>) {
        list.sortWith { c1, c2 ->
            var unread1 = 0
            var unread2 = 0
            try {
                unread1 = if (c1.unread_count.isNullOrEmpty()) 0 else c1.unread_count!!.toInt()
            } catch (ignored: Exception) {
            }
            try {
                unread2 = if (c2.unread_count.isNullOrEmpty()) 0 else c2.unread_count!!.toInt()
            } catch (ignored: Exception) {
            }
            unread2.compareTo(unread1)
        }
    }

    private fun applyUnreadCounts(clients: ArrayList<ClientRelationshipsDo>) {
        for (client in clients) {
            var totalUnread = 0
            for (unread in Constants.unreadList) {
                if (unread.fromjid != null &&
                    unread.fromjid.equals(client.guid, ignoreCase = true)
                ) {
                    try {
                        totalUnread += unread.count?.toIntOrNull() ?: 0
                    } catch (ignored: Exception) {
                    }
                }
            }
            client.unread_count = totalUnread.toString()
            Log.d("UnreadMap", "${client.name} -> $totalUnread")
        }
    }
}
