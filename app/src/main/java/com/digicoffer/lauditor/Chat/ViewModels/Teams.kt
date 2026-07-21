package com.digicoffer.lauditor.Chat.ViewModels

import android.app.AlertDialog
import android.app.Dialog
import android.content.SharedPreferences
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
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.digicoffer.lauditor.Chat.Adapters.MembersAdapter
import com.digicoffer.lauditor.Chat.Adapters.TeamsAdapter
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
import java.util.ArrayList

class Teams : Fragment(), AsyncTaskCompleteListener, TeamsAdapter.EventListener, MembersAdapter.OnMemberClick {

    private var progress_dialog: Dialog? = null
    private var rv_Clientrelationships: RecyclerView? = null
    private var et_Search: TextInputEditText? = null
    private var tv_chat: TextView? = null
    private var ad_dialog: AlertDialog? = null

    private var layout_empty_state: LinearLayout? = null
    private var tv_empty_subtitle: TextView? = null

    var Clientlist = ArrayList<ClientRelationshipsDo>()
    var child_list = ArrayList<ChildDO>()
    var members_list = ArrayList<ChildDO>()

    override fun onClick(view: View) {}

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
        tv_chat?.setText(R.string.list_of_teams)

        layout_empty_state = view.findViewById(R.id.layout_empty_state)
        tv_empty_subtitle = view.findViewById(R.id.tv_empty_subtitle)
        tv_empty_subtitle?.text = "There are no teams under here."

        et_Search?.addTextChangedListener(DescriptionValidation(et_Search))
        et_Search?.setHint(R.string.type_to_search)

        callViewGroupsWebservice()
        return view
    }

    private fun callWebservice() {
        progress_dialog = AndroidUtils.get_progress(activity)
        val postData = JSONObject()
        try {
            WebServiceHelper.callHttpWebService(
                this, requireContext(),
                WebServiceHelper.RestMethodType.GET,
                "chatusers",
                "Team_Chat",
                postData.toString()
            )
        } catch (e: Exception) {
            progress_dialog?.let {
                if (it.isShowing) AndroidUtils.dismiss_dialog(it)
            }
        }
    }

    private fun callViewGroupsWebservice() {
        try {
            val postdata = JSONObject()
            progress_dialog = AndroidUtils.get_progress(activity)
            WebServiceHelper.callHttpWebService(
                this, requireContext(),
                WebServiceHelper.RestMethodType.GET,
                "v3/members",
                "Get Members",
                postdata.toString()
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

                if ("Team_Chat" == httpResult.requestType) {
                    if (!result.getBoolean("error")) {
                        val jsonArray = result.getJSONArray("groups")
                        Constants.teamResArray = jsonArray
                        et_Search?.setText("")
                        loadClientRelationshipsData(jsonArray)
                    } else {
                        AndroidUtils.showValidationALert(
                            "Alert",
                            result.optString("msg"),
                            context
                        )
                    }
                } else if ("Get Members" == httpResult.requestType) {
                    val data = result.getJSONObject("data")
                    val users = data.getJSONArray("users")
                    loadMembers(users)
                }
            } catch (e: JSONException) {
                e.printStackTrace()
            }
        } else if (httpResult.result == WebServiceHelper.ServiceCallStatus.Failed) {
            progress_dialog?.let {
                if (it.isShowing) AndroidUtils.dismiss_dialog(it)
            }
            try {
                val result = JSONObject(httpResult.responseContent)
                AndroidUtils.showErrorAlert(result.optString("msg"), activity)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        } else {
            progress_dialog?.let {
                if (it.isShowing) AndroidUtils.dismiss_dialog(it)
            }
            AndroidUtils.showErrorAlert(
                httpResult.responseContent?.toString() ?: "",
                activity
            )
        }
    }

    private fun loadMembers(users: JSONArray) {
        members_list.clear()
        for (i in 0 until users.length()) {
            val jsonObject = users.getJSONObject(i)
            val member = ChildDO().apply {
                id = jsonObject.getString("id")
                name = jsonObject.getString("name")
                setGuid(jsonObject.getString("guid"))
                unread_count = ""
            }
            members_list.add(member)
        }
        loadMembersRecyclerView()
    }

    private fun loadMembersRecyclerView() {
        applyUnreadCounts(members_list)
        sortClientsByUnreadCount(members_list)
        updateEmptyState(members_list.isEmpty())

        if (members_list.isEmpty()) return

        val adapter = MembersAdapter(members_list, requireContext(), this)
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
                adapter.filter.filter(et_Search?.text?.toString() ?: "")
                rv_Clientrelationships?.post {
                    updateEmptyState(adapter.itemCount == 0)
                }
            }
        })
    }

    override fun onMemberClick(member: ChildDO?) {
        if (member == null) return
        val frag = MessagesList(rv_Clientrelationships)
        val bundle = Bundle().apply {
            putString("EXTRA_CONTACT_JID", member.guid ?: "")
            putString("EXTRA_CONTACT_NAME", member.name ?: "")
            putString("EXTRA_CONTACT_TYPE", "relationship")
        }
        frag.arguments = bundle

        activity?.supportFragmentManager
            ?.beginTransaction()
            ?.replace(R.id.id_framelayout, frag)
            ?.addToBackStack(null)
            ?.commit()
    }

    private fun loadClientRelationshipsData(jsonArray: JSONArray) {
        try {
            Clientlist.clear()

            for (i in 0 until jsonArray.length()) {
                val clientRelationshipsDo = ClientRelationshipsDo()
                val jsonObject = jsonArray.getJSONObject(i)
                clientRelationshipsDo.id = jsonObject.getString("id")
                clientRelationshipsDo.name = jsonObject.getString("groupName")
                clientRelationshipsDo.clientType = "Team"
                clientRelationshipsDo.Users = jsonObject.getJSONArray("users")
                clientRelationshipsDo.unread_count = ""

                val userArray = jsonObject.getJSONArray("users")
                clientRelationshipsDo.Users = userArray

                val userGuids = ArrayList<String>()
                for (j in 0 until userArray.length()) {
                    val jsonuser = userArray.getJSONObject(j)
                    userGuids.add(jsonuser.getString("guid"))
                }

                var unreadCountStr = ""
                var matchedGuid: String? = null
                for (guid in userGuids) {
                    for (unreadCount in Constants.unreadList) {
                        if (guid == unreadCount.fromjid) {
                            unreadCountStr = unreadCount.count ?: ""
                            matchedGuid = guid
                            break
                        }
                    }
                    if (matchedGuid != null) break
                }

                clientRelationshipsDo.guid = matchedGuid ?: ""
                clientRelationshipsDo.unread_count = unreadCountStr

                Clientlist.add(clientRelationshipsDo)
                Constants.teamMapChatList.put(clientRelationshipsDo.id ?: "", child_list)
            }

            loadRecycleView()
        } catch (e: Exception) {
            e.fillInStackTrace()
        }
    }

    private fun loadRecycleView() {
        updateEmptyState(Clientlist.isEmpty())
        if (Clientlist.isEmpty()) return

        rv_Clientrelationships?.layoutManager = GridLayoutManager(context, 1)
        val adapter = TeamsAdapter(Clientlist, this, requireContext(), requireActivity())

        Log.d("Clientlist", Clientlist.size.toString())
        rv_Clientrelationships?.adapter = adapter
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

    override fun Message(childDO: ChildDO?) {
        if (childDO == null) return
        try {
            val jid = childDO.uid ?: ""
            val pref = PreferenceManager.getDefaultSharedPreferences(context)
            val currentJID = pref.getString("xmpp_jid", null) ?: ""
            Log.d("jid+currentJID", "${jid}_$currentJID")
            if (childDO.id.isNullOrEmpty()) {
                move_message_fragment("", childDO.name ?: "", jid)
            } else {
                move_message_fragment(childDO.id ?: "", childDO.name ?: "", jid)
            }
        } catch (e: Exception) {
            Log.d("Error:", e.message ?: "")
            e.fillInStackTrace()
        }
    }

    override fun view_users(jid: String?, name: String?) {
        val frag = MessagesList(rv_Clientrelationships)
        val bundle = Bundle().apply {
            putString("EXTRA_CONTACT_JID", jid)
            putString("EXTRA_CONTACT_NAME", name)
            putString("EXTRA_CONTACT_TYPE", "relationship")
        }
        frag.arguments = bundle
        activity?.supportFragmentManager
            ?.beginTransaction()
            ?.replace(R.id.id_framelayout, frag)
            ?.addToBackStack(null)
            ?.commit()
    }

    override fun Users(clientRelationshipsDo: ClientRelationshipsDo?, holder: TeamsAdapter.MyViewHolder?) {}

    private fun move_message_fragment(tmid: String, name: String, jid: String) {
        try {
            val xmppJid = if (tmid == "") jid else "${jid}_$tmid"
            Log.d("xmpp_jid", xmppJid)
            val frag = MessagesList(rv_Clientrelationships)
            val bundle = Bundle().apply {
                putString("EXTRA_CONTACT_JID", xmppJid)
                putString("EXTRA_CONTACT_NAME", name)
                putString("EXTRA_CONTACT_TYPE", "relationship")
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

    private fun sortClientsByUnreadCount(list: ArrayList<ChildDO>) {
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

    private fun applyUnreadCounts(members: ArrayList<ChildDO>) {
        for (client in members) {
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
