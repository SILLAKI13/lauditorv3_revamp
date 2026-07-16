package com.digicoffer.lauditor.Email

import android.app.Activity
import android.app.Dialog
import android.content.Context
import android.graphics.Paint
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.CheckBox
import android.widget.Filter
import android.widget.Filterable
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.digicoffer.lauditor.Documents.Models.ViewDocumentsModel
import com.digicoffer.lauditor.R
import com.digicoffer.lauditor.Webservice.AsyncTaskCompleteListener
import com.digicoffer.lauditor.Webservice.HttpResultDo
import com.digicoffer.lauditor.Webservice.CommonApiHelper.WebServiceHelper
import com.digicoffer.lauditor.CommonFiles.GlobalFiles.AndroidUtils
import com.digicoffer.lauditor.CommonFiles.GlobalFiles.Constants
import com.digicoffer.lauditor.CommonFiles.GlobalFiles.DynamicUtils
import org.json.JSONException
import org.json.JSONObject
import java.util.ArrayList

class view_document_emailadapter(
    var itemsArrayList: ArrayList<ViewDocumentsModel>,
    private val cContext: Context,
    private val activity: Activity,
    private val btn_create: Button
) : RecyclerView.Adapter<view_document_emailadapter.MyViewHolder>(), AsyncTaskCompleteListener, Filterable {

    var list_item: ArrayList<ViewDocumentsModel> = itemsArrayList
    var docs_list = ArrayList<ViewDocumentsModel>()
    private val selectedDocumentNames = ArrayList<String>()
    private val selectedDocumentIds = ArrayList<String>()
    private val selectedDocumentPaths = ArrayList<String>()
    var mail = Email()
    var tempPos = 0

    override fun getFilter(): Filter {
        return object : Filter() {
            override fun performFiltering(charSequence: CharSequence): FilterResults {
                val charString = charSequence.toString()
                itemsArrayList = if (charString.isEmpty()) {
                    list_item
                } else {
                    val filteredList = ArrayList<ViewDocumentsModel>()
                    for (row in list_item) {
                        if (AndroidUtils.isNull(row.name).lowercase().contains(charString.lowercase())) {
                            filteredList.add(row)
                        }
                    }
                    filteredList
                }
                val filterResults = FilterResults()
                filterResults.count = itemsArrayList.size
                filterResults.values = itemsArrayList
                return filterResults
            }

            @Suppress("UNCHECKED_CAST")
            override fun publishResults(charSequence: CharSequence, filterResults: FilterResults) {
                itemsArrayList = filterResults.values as ArrayList<ViewDocumentsModel>
                notifyDataSetChanged()
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.view_client_group_document, parent, false)
        return MyViewHolder(view)
    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        val viewDocumentsModel = itemsArrayList[position]

        try {
            holder.tv_client_name.text = viewDocumentsModel.uploaded_by
            holder.tv_doc_description.text = viewDocumentsModel.description
            holder.tv_image_name.text = viewDocumentsModel.name
            holder.tv_created_date.text = viewDocumentsModel.created
            holder.tv_Expiration_date.text = viewDocumentsModel.expiration_date

            holder.tv_client_name.paintFlags = holder.tv_client_name.paintFlags or Paint.UNDERLINE_TEXT_FLAG
            holder.tv_doc_description.paintFlags = holder.tv_doc_description.paintFlags or Paint.UNDERLINE_TEXT_FLAG

            holder.checkbox_id.tag = viewDocumentsModel
            holder.checkbox_id.isChecked = viewDocumentsModel.getIsChecked()

            holder.checkbox_id.setOnCheckedChangeListener { buttonView, isChecked ->
                val documentModel = buttonView.tag as? ViewDocumentsModel
                if (documentModel != null) {
                    documentModel.setIsChecked(isChecked)

                    if (isChecked) {
                        var documentExists = false
                        val count = Constants.composAttachDocAry?.size ?: 0
                        for (i in 0 until count) {
                            if (Constants.composAttachDocAry?.get(i)?.id == documentModel.id) {
                                documentExists = true
                                break
                            }
                        }

                        if (!documentExists) {
                            val local = Constants.IdNameModel()
                            local.id = documentModel.id
                            local.name = documentModel.filename
                            Constants.composAttachDocAry?.add(local)

                            val pos = (Constants.composAttachDocAry?.size ?: 0) - 1
                            tempPos = pos
                            view_document(documentModel.id ?: "")
                        }
                    } else {
                        var pos = -1
                        val count = Constants.composAttachDocAry?.size ?: 0
                        for (i in 0 until count) {
                            if (documentModel.id == Constants.composAttachDocAry?.get(i)?.id) {
                                pos = i
                                break
                            }
                        }
                        if (pos != -1) {
                            Constants.composAttachDocAry?.removeAt(pos)
                        }
                    }
                }
                if (Constants.composAttachDocAry?.isEmpty() == true) {
                    btn_create.alpha = 0.5f
                    btn_create.isEnabled = false
                } else {
                    btn_create.alpha = 1.0f
                    btn_create.isEnabled = true
                }
            }
        } catch (e: Exception) {
            e.fillInStackTrace()
            AndroidUtils.showAlert(e.message, activity)
        }
    }

    override fun getItemCount(): Int {
        return itemsArrayList.size
    }

    override fun onClick(view: View) {}

    override fun onAsyncTaskComplete(httpResult: HttpResultDo) {
        if (progress_dialog != null && progress_dialog!!.isShowing) {
            AndroidUtils.dismiss_dialog(progress_dialog)
        }
        val success = httpResult.result.toString()
        Log.d("Succ", success)
        if (httpResult.result == WebServiceHelper.ServiceCallStatus.Success) {
            try {
                val result = JSONObject(httpResult.responseContent ?: "{}")
                if (httpResult.requestType == "view_document") {
                    val data = result.getJSONObject("data")
                    val url = data.getString("url")
                    Constants.composAttachDocAry?.get(tempPos)?.url = url
                    Log.d("Doc_url", url)
                } else {
                    println("Failed to obtain authentication URL")
                }
            } catch (e: JSONException) {
                e.fillInStackTrace()
            }
        }
    }

    class MyViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val checkbox_id: CheckBox = itemView.findViewById(R.id.checkbox_id)
        val rv_doc_details: RelativeLayout = itemView.findViewById(R.id.rv_doc_details)
        val tv_image_name: TextView = itemView.findViewById(R.id.tv_image_name)
        val tv_Expiration_date: TextView = itemView.findViewById(R.id.tv_Expiration_date)
        val tv_client_name: TextView = itemView.findViewById(R.id.tv_client_name)
        val tv_doc_description: TextView = itemView.findViewById(R.id.tv_doc_description)
        val tv_created_date: TextView = itemView.findViewById(R.id.tv_created_date)
        val tv_Expiration: TextView = itemView.findViewById(R.id.tv_Expiration)

        init {
            tv_client_name.textSize = DynamicUtils.fifteen.toFloat()
            checkbox_id.visibility = View.VISIBLE
            tv_doc_description.textSize = DynamicUtils.fifteen.toFloat()
            tv_created_date.setText(R.string.date)
            tv_Expiration.setText(R.string.expiration_)
            tv_Expiration_date.setText(R.string.expiration)
        }
    }

    fun view_document(doc_id: String) {
        try {
            progress_dialog = AndroidUtils.get_progress(activity)
            val jsonObject = JSONObject()
            WebServiceHelper.callHttpWebService(
                this, activity, WebServiceHelper.RestMethodType.GET,
                "v3/document/$doc_id/view", "view_document", jsonObject.toString()
            )
        } catch (e: Exception) {
            if (progress_dialog != null && progress_dialog!!.isShowing) {
                AndroidUtils.dismiss_dialog(progress_dialog)
            }
        }
    }

    companion object {
        @JvmField
        var progress_dialog: Dialog? = null
    }
}
