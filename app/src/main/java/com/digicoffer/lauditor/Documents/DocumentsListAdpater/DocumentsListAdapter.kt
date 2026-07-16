package com.digicoffer.lauditor.Documents.DocumentsListAdpater

import android.text.TextUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.CheckBox
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.widget.LinearLayoutCompat
import androidx.recyclerview.widget.RecyclerView
import com.digicoffer.lauditor.Documents.Documents
import com.digicoffer.lauditor.Documents.ViewModel.DocumentsEn
import com.digicoffer.lauditor.Documents.Models.DocumentsModel
import com.digicoffer.lauditor.Matter.OldViewModels.MatterDocuments
import com.digicoffer.lauditor.Matter.ViewModels.MatterDocuments_En
import com.digicoffer.lauditor.R
import java.util.ArrayList

class DocumentsListAdapter : RecyclerView.Adapter<DocumentsListAdapter.ViewHolder> {
    var itemsArrayList: ArrayList<DocumentsModel>
    var list_item: ArrayList<DocumentsModel>
    var select_checked: Boolean = false
    var isfiledeleted: Boolean = false
    var tag: String = ""
    var subtag: String = ""
    var documents: Documents? = null
    var documents_en: DocumentsEn? = null
    var matterDocuments: MatterDocuments? = null
    var matterDocuments_en: MatterDocuments_En? = null
    var eventListener: EventListener

    constructor(
        itemsArrayList: ArrayList<DocumentsModel>,
        tag: String,
        subtag: String,
        eventListener: EventListener,
        documents: Documents
    ) : super() {
        this.documents = documents
        this.itemsArrayList = itemsArrayList
        this.list_item = itemsArrayList
        this.tag = tag
        this.subtag = subtag
        this.eventListener = eventListener
    }

    constructor(
        itemsArrayList: ArrayList<DocumentsModel>,
        tag: String,
        subtag: String,
        eventListener: EventListener,
        documents_en: DocumentsEn
    ) : super() {
        this.documents_en = documents_en
        this.itemsArrayList = itemsArrayList
        this.list_item = itemsArrayList
        this.tag = tag
        this.subtag = subtag
        this.eventListener = eventListener
    }

    constructor(
        itemsArrayList: ArrayList<DocumentsModel>,
        tag: String,
        subtag: String,
        eventListener: EventListener,
        matterDocuments: MatterDocuments
    ) : super() {
        this.matterDocuments = matterDocuments
        this.itemsArrayList = itemsArrayList
        this.list_item = itemsArrayList
        this.tag = tag
        this.subtag = subtag
        this.eventListener = eventListener
    }

    constructor(
        itemsArrayList: ArrayList<DocumentsModel>,
        tag: String,
        subtag: String,
        eventListener: EventListener,
        matterDocuments_en: MatterDocuments_En
    ) : super() {
        this.matterDocuments_en = matterDocuments_en
        this.itemsArrayList = itemsArrayList
        this.list_item = itemsArrayList
        this.tag = tag
        this.subtag = subtag
        this.eventListener = eventListener
    }

    interface EventListener {
        fun ViewTags(documentsModel: DocumentsModel, itemsArrayList: ArrayList<DocumentsModel>)
        fun EditDocuments(documentsModel: DocumentsModel, itemsArrayList: ArrayList<DocumentsModel>, position: Int)
        fun RemoveDocument(position: Int, tag: String)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val itemView = LayoutInflater.from(parent.context).inflate(R.layout.displays_documents_list, parent, false)
        return ViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val documentsModel = itemsArrayList[position]
        itemsArrayList = list_item
        holder.tv_document_name.text = documentsModel.name + "." + documentsModel.content_type
        holder.tv_document_name.maxLines = 1
        holder.tv_document_name.ellipsize = TextUtils.TruncateAt.END
        holder.cb_documents_list.isChecked = itemsArrayList[position].isChecked
        holder.cb_documents_list.tag = position
        holder.iv_edit_meta.visibility = View.VISIBLE
        holder.lock_open.visibility = View.GONE

        holder.cb_documents_list.setOnClickListener {
            val pos = holder.cb_documents_list.tag as Int
            itemsArrayList[pos].isChecked = !itemsArrayList[pos].isChecked
            check_allselected()
            isAnyItemChecked()
            notifyDataSetChanged()
        }
        holder.btn_view_tags.setOnClickListener {
            eventListener.ViewTags(documentsModel, itemsArrayList)
        }
        holder.iv_edit_meta.setOnClickListener {
            eventListener.EditDocuments(documentsModel, itemsArrayList, position)
        }
        holder.lock_open.setOnClickListener {
            val pos = holder.adapterPosition
            if (pos != RecyclerView.NO_POSITION) {
                val doc = itemsArrayList[pos]
                doc.isencrypted = true
                notifyItemChanged(pos) // Only update this item
                checkEncryptionStatus() // Check if all are encrypted
            }
        }
        holder.lock_close.setOnClickListener {
            val pos = holder.adapterPosition
            if (pos != RecyclerView.NO_POSITION) {
                val doc = itemsArrayList[pos]
                doc.isencrypted = false
                notifyItemChanged(pos) // Only update this item
                checkEncryptionStatus() // Check if all are decrypted
            }
        }
        holder.enable_download_icon.setOnClickListener {
            val pos = holder.adapterPosition
            if (pos != RecyclerView.NO_POSITION) {
                val doc = itemsArrayList[pos]
                doc.isIsenabled = false
                notifyItemChanged(pos) // Only update this item
                checkEnableDownloadStatus() // Check if all are decrypted
            }
        }
        holder.disable_download_icon.setOnClickListener {
            val pos = holder.adapterPosition
            if (pos != RecyclerView.NO_POSITION) {
                val doc = itemsArrayList[pos]
                doc.isIsenabled = true
                notifyItemChanged(pos) // Only update this item
                checkEnableDownloadStatus()
            }
        }
        holder.iv_cancel.setOnClickListener {
            val pos = holder.adapterPosition
            if (pos != RecyclerView.NO_POSITION) {
                eventListener.RemoveDocument(pos, tag)
            }
        }
    }


    fun isAnyItemChecked() {
        var isSelected = false
        for (item in itemsArrayList) {
            if (item.isChecked) {
                isSelected = true
                break
            }
        }
        matterDocuments_en?.isAnyOneSelected(isSelected)
    }

    fun check_allselected() {
        var allSelected = true
        for (item in itemsArrayList) {
            if (!item.isChecked) {
                allSelected = false
                break
            }
        }
        if (matterDocuments != null) {
            matterDocuments?.check_select_all(allSelected)
        } else if (matterDocuments_en != null) {
            matterDocuments_en?.check_select_all(allSelected)
        } else {
            documents?.check_select_all(allSelected)
        }
    }

    fun checkEncryptionStatus() {
        var allEncrypted = true
        var allDecrypted = true
        for (item in itemsArrayList) {
            if (item.isencrypted == true) {
                allDecrypted = false // At least one is encrypted
            } else {
                allEncrypted = false // At least one is decrypted
            }
            if (!allDecrypted && !allEncrypted) {
                break // No need to check further
            }
        }
        if (matterDocuments != null) {
            if (allEncrypted) {
                matterDocuments?.check_encrypted(true)
            } else if (allDecrypted) {
                matterDocuments?.check_encrypted(false)
            }
        } else if (matterDocuments_en != null) {
            if (allEncrypted) {
                matterDocuments_en?.check_encrypted(true)
            } else if (allDecrypted) {
                matterDocuments_en?.check_encrypted(false)
            }
        } else {
            if (allEncrypted) {
                documents?.check_encrypted(true)
            } else if (allDecrypted) {
                documents?.check_encrypted(false)
            }
        }
    }

    fun checkEnableDownloadStatus() {
        var allEnabled = true
        var allDisabled = true
        for (item in itemsArrayList) {
            if (item.isIsenabled) {
                allDisabled = false
            } else {
                allEnabled = false
            }
            if (!allDisabled && !allEnabled) {
                break
            }
        }
        if (matterDocuments != null) {
            if (allEnabled) {
                matterDocuments?.check_enabled(true)
            } else if (allDisabled) {
                matterDocuments?.check_enabled(false)
            }
        } else if (matterDocuments_en != null) {
            if (allEnabled) {
                matterDocuments_en?.check_enabled(true)
            } else if (allDisabled) {
                matterDocuments_en?.check_enabled(false)
            }
        } else {
            if (allEnabled) {
                documents?.check_enabled(true)
            } else if (allDisabled) {
                documents?.check_enabled(false)
            }
        }
    }

    fun EncryptAllorDecryptAll(isChecked: Boolean): Boolean {
        for (item in itemsArrayList) {
            item.isencrypted = isChecked
        }
        notifyDataSetChanged()
        return isChecked
    }

    fun EnableAllorDisableAll(isChecked: Boolean): Boolean {
        for (item in itemsArrayList) {
            item.isIsenabled = isChecked
        }
        notifyDataSetChanged()
        return isChecked
    }

    fun selectOrDeselectAll(isChecked: Boolean): Boolean {
        for (i in list_item.indices) {
            list_item[i].isChecked = isChecked
            notifyDataSetChanged()
        }
        return isChecked
    }

    override fun getItemId(position: Int): Long {
        return position.toLong()
    }

    override fun getItemCount(): Int {
        return itemsArrayList.size
    }

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val cb_documents_list: CheckBox = itemView.findViewById(R.id.chk_selected_documents)
        val tv_document_name: TextView = itemView.findViewById(R.id.tv_document_name)
        val chk_box_layout: LinearLayoutCompat = itemView.findViewById(R.id.chk_box_layout)
        val iv_cancel: ImageView = itemView.findViewById(R.id.iv_cancel)
        val iv_edit_meta: ImageView = itemView.findViewById(R.id.iv_edit_meta)
        val lock_close: ImageView = itemView.findViewById(R.id.lock_close)
        val lock_open: ImageView = itemView.findViewById(R.id.lock_open)
        val enable_download_icon: ImageView = itemView.findViewById(R.id.enable_download_icon)
        val disable_download_icon: ImageView = itemView.findViewById(R.id.disable_download_icon)
        val btn_view_tags: Button = itemView.findViewById(R.id.btn_view_tags)

        init {
            if (matterDocuments_en != null) {
                lock_open.visibility = View.VISIBLE
            }
            iv_edit_meta.setImageResource(R.drawable.edit__icon)
            btn_view_tags.setText(R.string.view_tags)
            if (tag == "add_tag") {
                chk_box_layout.visibility = View.VISIBLE
                cb_documents_list.visibility = View.VISIBLE
                iv_edit_meta.visibility = View.GONE
            } else if (tag == "edit_meta") {
                iv_edit_meta.visibility = View.VISIBLE
                cb_documents_list.visibility = View.GONE
                chk_box_layout.visibility = View.GONE
            } else if (tag == "en_encrption") {
                lock_open.visibility = View.GONE
                lock_close.visibility = View.VISIBLE
            } else if (tag == "dis_encrption") {
                lock_open.visibility = View.VISIBLE
                lock_close.visibility = View.GONE
            } else if (tag == "enable_download") {
                disable_download_icon.visibility = View.GONE
                enable_download_icon.visibility = View.VISIBLE
            } else if (tag == "disable_download") {
                disable_download_icon.visibility = View.VISIBLE
                enable_download_icon.visibility = View.GONE
            } else {
                chk_box_layout.visibility = View.GONE
                iv_edit_meta.setVisibility(View.GONE)
                cb_documents_list.setVisibility(View.GONE)
                lock_open.visibility = View.VISIBLE
                lock_close.visibility = View.GONE
            }
        }
    }
}
