package com.digicoffer.lauditor.Relationships

import android.content.Context
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.widget.AppCompatButton
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.digicoffer.lauditor.Relationships.Adapter.RelationshipsAdapter
import com.digicoffer.lauditor.Relationships.Model.RelationshipsModel
import com.digicoffer.lauditor.Groups.Models.ViewGroupModel
import com.digicoffer.lauditor.Members.Adapters.GroupsAdapter
import com.digicoffer.lauditor.R
import com.digicoffer.lauditor.CommonFiles.GlobalFiles.DynamicUtils
import com.google.android.material.textfield.TextInputEditText
import org.json.JSONException
import java.util.ArrayList
import java.util.concurrent.atomic.AtomicBoolean

class GroupRecyclerViewFragment(
    private val mcontext: Context,
    private val groupsList: ArrayList<ViewGroupModel>,
    private val clientRelationship: ClientRelationship?,
    private val relationshipsModel_new: RelationshipsModel?,
    private val relationshipsAdapter: RelationshipsAdapter
) : Fragment() {

    private var btn_send_request: AppCompatButton? = null
    private var isCancel = false
    @JvmField var isUpdated = AtomicBoolean(false)

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val popupView = inflater.inflate(R.layout.groups_recylerview_popup, container, false)
        val rv_relationship_groups = popupView.findViewById<RecyclerView>(R.id.rv_relationship_groups)
        val et_search_relationships = popupView.findViewById<TextInputEditText>(R.id.et_search_relationships)
        et_search_relationships.setHint(R.string.search_groups)

        btn_send_request = popupView.findViewById(R.id.btn_send_request)
        btn_send_request?.setText(R.string.update)
        btn_send_request?.alpha = 0.5f
        btn_send_request?.isEnabled = false
        relationshipsAdapter.btn_send_request = btn_send_request
        relationshipsAdapter.isUpdated = isUpdated
        relationshipsAdapter.et_search_groups = et_search_relationships
        relationshipsAdapter.popupView = popupView

        val btn_relationships_cancel = popupView.findViewById<AppCompatButton>(R.id.btn_relationships_cancel)
        btn_relationships_cancel.setText(R.string.cancel)

        val tv_header_name = popupView.findViewById<TextView>(R.id.header_name)
        val relName = relationshipsModel_new?.name ?: ""
        if (relName.isNotEmpty()) {
            tv_header_name.text = "Modify Group Access - $relName"
        } else {
            tv_header_name.setText(R.string.modify_group_access)
        }
        tv_header_name.textSize = DynamicUtils.twenty.toFloat()

        val iv_close = popupView.findViewById<ImageView>(R.id.close_groups)
        val chk_select_all = popupView.findViewById<CheckBox>(R.id.chk_select_all) // Optional

        rv_relationship_groups.layoutManager = LinearLayoutManager(mcontext)

        groupsAdapter = GroupsAdapter(groupsList, relationshipsAdapter, clientRelationship)
        rv_relationship_groups.adapter = groupsAdapter

        et_search_relationships.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                groupsAdapter?.filter?.filter(et_search_relationships.text.toString().trim())
            }
        })

        val dismissListener = View.OnClickListener {
            try {
                isCancel = true
                relationshipsAdapter.callUpdateGroupAccess(relationshipsModel_new?.id ?: "", groupsList, isCancel)
            } catch (e: JSONException) {
                throw RuntimeException(e)
            }
        }

        btn_relationships_cancel.setOnClickListener(dismissListener)
        iv_close.setOnClickListener(dismissListener)

        btn_send_request?.setOnClickListener {
            try {
                isCancel = false
                relationshipsAdapter.callUpdateGroupAccess(relationshipsModel_new?.id ?: "", groupsList, isCancel)
            } catch (e: JSONException) {
                e.fillInStackTrace()
            }
        }

        return popupView
    }

    companion object {
        @JvmField
        var groupsAdapter: GroupsAdapter? = null
    }
}
