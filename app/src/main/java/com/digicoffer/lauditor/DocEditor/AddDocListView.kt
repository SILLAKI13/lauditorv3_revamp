package com.digicoffer.lauditor.DocEditor

import android.app.Activity
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import com.digicoffer.lauditor.R
import com.digicoffer.lauditor.CommonFiles.GlobalFiles.AndroidUtils
import com.digicoffer.lauditor.CommonFiles.ValidationUtils.ContentValidation
import com.digicoffer.lauditor.CommonFiles.ValidationUtils.ListValidation
import com.google.android.material.textfield.TextInputEditText
import java.util.ArrayList

class AddDocListView(
    context: Context,
    parentContainer: ViewGroup?,
    listener: ActionListener?
) {
    private val rootView: View = LayoutInflater.from(context).inflate(R.layout.list_layout_de, parentContainer, false)
    private val tv_content: TextView = rootView.findViewById(R.id.tv_content)
    private val iv_remove: ImageView = rootView.findViewById(R.id.iv_remove)
    private val iv_plus_icon: ImageView = rootView.findViewById(R.id.iv_plus_icon)
    private val ll_add_more: LinearLayout = rootView.findViewById(R.id.ll_add_more)
    private val ll_add_content: LinearLayout = rootView.findViewById(R.id.ll_add_content)
    private var titleKey: String? = null

    interface ActionListener {
        fun onRemove(view: View)
    }

    init {
        iv_remove.setOnClickListener {
            parentContainer?.removeView(rootView)
            listener?.onRemove(rootView)
        }

        iv_plus_icon.setOnClickListener {
            if (!validateBeforeAdding(context)) return@setOnClickListener
            addListItemView(context, false)
        }

        ll_add_more.setOnClickListener {
            if (!validateBeforeAdding(context)) return@setOnClickListener
            addListItemView(context, false)
        }

        // Add the first list item by default
        addListItemView(context, true)
    }

    private fun addListItemView(context: Context, isFirstItem: Boolean) {
        val view_added_list = LayoutInflater.from(context).inflate(R.layout.list_item_view_de, null)
        val et_list_item = view_added_list.findViewById<TextInputEditText>(R.id.et_list_item)
        val iv_delete_events = view_added_list.findViewById<ImageView>(R.id.iv_delete_events)
        et_list_item.setHint(R.string.enter_list_items)
        et_list_item.addTextChangedListener(ListValidation(et_list_item))
        et_list_item.requestFocus()

        et_list_item.post {
            val imm = context.getSystemService(Context.INPUT_METHOD_SERVICE) as? InputMethodManager
            imm?.showSoftInput(et_list_item, InputMethodManager.SHOW_IMPLICIT)
        }

        if (isFirstItem) {
            iv_delete_events.visibility = View.GONE // Hide delete for first item
        } else {
            iv_delete_events.setOnClickListener {
                ll_add_content.removeView(view_added_list)
            }
        }

        ll_add_content.addView(view_added_list)
    }

    private fun validateBeforeAdding(context: Context): Boolean {
        if (ll_add_content.childCount > 0) {
            val lastView = ll_add_content.getChildAt(ll_add_content.childCount - 1)
            val lastEt = lastView.findViewById<TextInputEditText>(R.id.et_list_item)
            if (lastEt != null && lastEt.text != null && lastEt.text.toString().trim { it <= ' ' }.isEmpty()) {
                AndroidUtils.showError(
                    "Please add the text in the " + tv_content.text.toString() + ".",
                    context as Activity
                )
                lastEt.requestFocus()
                return false
            }
        }
        return true
    }

    fun setContent(titleKey: String?, titleText: String?) {
        this.titleKey = titleKey
        tv_content.text = titleText
        tv_content.tag = titleText
    }

    fun setListItems(items: List<String>?) {
        ll_add_content.removeAllViews() // Clear existing items

        if (items == null || items.isEmpty()) return

        for (i in items.indices) {
            val item = items[i]
            val view_added_list = LayoutInflater.from(rootView.context).inflate(R.layout.list_item_view_de, null)
            val et_list_item = view_added_list.findViewById<TextInputEditText>(R.id.et_list_item)
            val iv_delete_events = view_added_list.findViewById<ImageView>(R.id.iv_delete_events)

            et_list_item.setText(item)
            et_list_item.addTextChangedListener(ContentValidation(et_list_item, false))

            if (i == 0) {
                iv_delete_events.visibility = View.GONE // No delete for first item
            } else {
                iv_delete_events.setOnClickListener {
                    ll_add_content.removeView(view_added_list)
                }
            }

            ll_add_content.addView(view_added_list)
        }
    }

    fun getListItems(): List<String> {
        val items: MutableList<String> = ArrayList()

        for (i in 0 until ll_add_content.childCount) {
            val itemView = ll_add_content.getChildAt(i)
            val etItem = itemView.findViewById<TextInputEditText>(R.id.et_list_item)
            if (etItem != null && etItem.text != null) {
                val text = etItem.text.toString().trim { it <= ' ' }
                if (text.isNotEmpty()) {
                    items.add(text)
                }
            }
        }

        return items
    }

    fun getView(): View {
        return rootView
    }

    fun getTitleKey(): String? {
        return titleKey
    }

    fun setTitleText(title: String?) {
        tv_content.text = title
    }
}
