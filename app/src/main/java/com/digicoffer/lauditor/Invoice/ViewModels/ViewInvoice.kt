package com.digicoffer.lauditor.Invoice.ViewModels

import android.annotation.SuppressLint
import android.app.Activity
import android.app.AlertDialog
import android.app.Dialog
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.Drawable
import android.net.Uri
import android.os.Bundle
import android.text.Editable
import android.text.InputFilter
import android.text.InputType
import android.text.TextWatcher
import android.util.Log
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.EditText
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.ListView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.NonNull
import androidx.annotation.Nullable
import androidx.appcompat.widget.AppCompatButton
import androidx.cardview.widget.CardView
import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.digicoffer.lauditor.Documents.Models.MattersModel
import com.digicoffer.lauditor.Invoice.Adapters.LineItemAdapter
import com.digicoffer.lauditor.Invoice.Adapters.ViewInvoiceAdapter
import com.digicoffer.lauditor.Invoice.Models.InvoiceModel
import com.digicoffer.lauditor.Invoice.Models.LineItemModel
import com.digicoffer.lauditor.R
import com.digicoffer.lauditor.Webservice.AsyncTaskCompleteListener
import com.digicoffer.lauditor.Webservice.HttpResultDo
import com.digicoffer.lauditor.Webservice.CommonApiHelper.WebServiceHelper
import com.digicoffer.lauditor.CommonFiles.GlobalFiles.AndroidUtils
import com.digicoffer.lauditor.CommonFiles.ValidationUtils.DescriptionValidation
import com.digicoffer.lauditor.CommonFiles.GlobalFiles.FileDownloader
import com.digicoffer.lauditor.CommonFiles.FileSelection.Filecosen
import com.digicoffer.lauditor.CommonFiles.ValidationUtils.Validation
import com.digicoffer.lauditor.CommonFiles.CommonAdapters.CommonSpinnerAdapter
import com.google.android.material.textfield.TextInputEditText
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream
import java.io.OutputStream
import java.text.SimpleDateFormat
import java.util.ArrayList
import java.util.Date
import java.util.Locale
import java.util.Objects

class ViewInvoice : Fragment(), AsyncTaskCompleteListener, ViewInvoiceAdapter.InvoiceActionListener {

    companion object {
        private const val MODE_LIST = "LIST"
        private const val MODE_CREATE = "CREATE"
        private const val MODE_EDIT = "EDIT"
        private const val MODE_VIEW = "VIEW"

        private val CURRENCY_LIST = arrayOf(
            "AUD", "BHD", "CAD", "EUR", "INR", "JPY", "KWD", "GBP", "CHF", "USD"
        )
    }

    private var ClientAdapter: CommonSpinnerAdapter<com.digicoffer.lauditor.Documents.Models.ClientsModel>? = null
    private var Matteradapter: CommonSpinnerAdapter<MattersModel>? = null

    private var currentMode = MODE_LIST
    private var editingModel: InvoiceModel? = null

    private var suggestedInvoiceSequence = 0
    private var suggestedInvoiceCode = ""

    private var tv_switchView: View? = null
    private var tv_switchCreate: View? = null
    private var tv_switchView_title: TextView? = null
    private var tv_switchView_arrow: ImageView? = null

    private var ll_view_invoice_section: LinearLayout? = null
    private var rv_invoice_list: RecyclerView? = null
    private var et_search_invoice: TextInputEditText? = null

    private var ll_empty_state: LinearLayout? = null

    private var v_create_form: View? = null

    private var ll_upload_logo: LinearLayout? = null
    private var cv_logo_preview: CardView? = null
    private var iv_logo: ImageView? = null
    private var iv_remove_logo: ImageView? = null
    private var logoUri: Uri? = null
    private var include_logo = true
    private var logoUrl = ""
    private var pendingLogoUpload = false

    private var et_invoice_no: TextInputEditText? = null
    private var invoice_name: TextView? = null
    private var invoice_date: TextView? = null
    private var invoice_due_date: TextView? = null
    private var tv_gst: TextView? = null
    private var tv_pan: TextView? = null
    private var tv_client_gst: TextView? = null
    private var tv_client_pan: TextView? = null
    private var tv_select_client: TextView? = null
    private var tv_client_address: TextView? = null

    private var tv_invoice_date: AppCompatButton? = null
    private var tv_due_date: AppCompatButton? = null

    private var et_gst: TextInputEditText? = null
    private var et_pan: TextInputEditText? = null
    private var et_client_gst: TextInputEditText? = null
    private var et_client_pan: TextInputEditText? = null
    private var et_client_address: TextInputEditText? = null
    private var tv_address_count: TextView? = null
    private var tv_additional_details: TextView? = null

    private var rv_line_items: RecyclerView? = null
    private var cv_add_row: TextView? = null
    private var lineItemAdapter: LineItemAdapter? = null
    private val lineItems = ArrayList<LineItemModel>()

    private var ll_sp_currency: View? = null
    private var sp_currency_list: View? = null
    private var selectedCurrency = "INR"

    private var tv_sub_total: TextView? = null
    private var tv_discount_value: TextView? = null
    private var tv_tax_value: TextView? = null
    private var tv_total: TextView? = null
    private var et_discount: TextInputEditText? = null
    private var et_tax: TextInputEditText? = null
    private var et_additional_details: TextInputEditText? = null
    private var tv_additional_count: TextView? = null

    private var btn_cancel: AppCompatButton? = null
    private var btn_save: AppCompatButton? = null

    private val invoiceList = ArrayList<InvoiceModel>()
    private var invoiceAdapter: ViewInvoiceAdapter? = null
    private var progressDialog: Dialog? = null

    private var cameraImageUri: Uri? = null
    private lateinit var cameraLauncher: ActivityResultLauncher<Uri>
    private lateinit var galleryLauncher: ActivityResultLauncher<Intent>
    private lateinit var requestCameraPermission: ActivityResultLauncher<String>

    private var ll_sp_client: View? = null
    private var ll_sp_matter: View? = null
    private var ll_matter: View? = null
    private var sp_client_list: ListView? = null
    private var sp_matter_list: ListView? = null

    private var client_id = ""
    private var client_type = "consumer"
    private var client_name = ""
    private var matter_id = ""
    private var matter_name = ""
    private var matter_type = ""
    private var isClientchecked = true
    private var isMatterchecked = true

    private val clientsList = ArrayList<com.digicoffer.lauditor.Documents.Models.ClientsModel>()
    private val mattersList = ArrayList<MattersModel>()
    private val CorpClientsList = ArrayList<com.digicoffer.lauditor.Documents.Models.ClientsModel>()

    private var et_sp_client: TextView? = null
    private var et_sp_matter: TextView? = null
    private var tv_select_matter: TextView? = null
    private var iv_clear_client: ImageView? = null
    private var iv_drop_down_client: ImageView? = null
    private var iv_clear_matter: ImageView? = null
    private var iv_drop_down_matter: ImageView? = null

    private var replaceOriginal = false

    override fun onCreate(@Nullable savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        cameraLauncher = registerForActivityResult(
            ActivityResultContracts.TakePicture()
        ) { success ->
            if (success && cameraImageUri != null) {
                handleLogoUri(cameraImageUri!!)
            }
        }

        galleryLauncher = registerForActivityResult(
            ActivityResultContracts.StartActivityForResult()
        ) { result ->
            if (result.resultCode == Activity.RESULT_OK && result.data != null && result.data!!.data != null) {
                handleLogoUri(result.data!!.data!!)
            }
        }

        requestCameraPermission = registerForActivityResult(
            ActivityResultContracts.RequestPermission()
        ) { granted ->
            if (granted) {
                openCamera()
            } else {
                Toast.makeText(requireContext(), "Camera permission denied", Toast.LENGTH_SHORT).show()
            }
        }
    }

    @Nullable
    override fun onCreateView(
        @NonNull inflater: LayoutInflater,
        @Nullable container: ViewGroup?,
        @Nullable savedInstanceState: Bundle?
    ): View? {
        val wrapper = FrameLayout(requireContext())
        wrapper.layoutParams = ViewGroup.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.MATCH_PARENT
        )

        val root = inflater.inflate(R.layout.fragment_invoice_list, wrapper, false)
        bindListViews(root)
        wrapper.addView(root)

        v_create_form = inflater.inflate(R.layout.create_invoice, wrapper, false)
        bindFormViews(v_create_form!!)
        wrapper.addView(v_create_form)

        setupSwitchRow(root)
        setupFormListeners()
        setupLineItemsTable()

        switchToMode(MODE_LIST)
        return wrapper
    }

    private fun bindListViews(v: View) {
        tv_switchView = v.findViewById(R.id.tv_switchView)
        if (tv_switchView != null) {
            tv_switchView_title = tv_switchView!!.findViewById(R.id.tv_title)
            val arrowId = tv_switchView!!.resources.getIdentifier(
                "iv_arrow", "id", tv_switchView!!.context.packageName
            )
            if (arrowId != 0) {
                tv_switchView_arrow = tv_switchView!!.findViewById(arrowId)
            }
        }

        ll_view_invoice_section = v.findViewById(R.id.ll_view_invoice_section)
        rv_invoice_list = v.findViewById(R.id.rv_invoice_list)
        ll_empty_state = v.findViewById(R.id.ll_empty_state)

        val tl_et_search_invoice = v.findViewById<View>(R.id.et_search_invoice)
        et_search_invoice = tl_et_search_invoice.findViewById(R.id.et_Search)
        et_search_invoice?.setHint(R.string.search_invoices)
    }

    private fun bindFormViews(v: View) {
        tv_switchCreate = v.findViewById(R.id.tv_switchCreate)
        cv_logo_preview = v.findViewById(R.id.cv_logo_preview)
        iv_logo = v.findViewById(R.id.iv_logo)
        iv_remove_logo = v.findViewById(R.id.iv_remove_logo)
        ll_upload_logo = v.findViewById(R.id.ll_upload_logo)

        et_invoice_no = v.findViewById(R.id.et_invoice_no)

        invoice_name = v.findViewById(R.id.tv_invoice_no)
        invoice_name?.text = "Invoice No"
        invoice_date = v.findViewById(R.id.invoice_date)
        invoice_date?.text = "Invoice Date"
        invoice_due_date = v.findViewById(R.id.invoice_due_date)
        invoice_due_date?.text = "Due Date"

        tv_invoice_date = v.findViewById(R.id.tv_invoice_date)
        tv_invoice_date?.setHint("Invoice Date")

        tv_due_date = v.findViewById(R.id.tv_due_date)
        tv_due_date?.setHint("Due Date")

        tv_gst = v.findViewById(R.id.tv_gst)
        tv_gst?.setText(R.string.gst)

        tv_pan = v.findViewById(R.id.tv_pan)
        tv_pan?.setText(R.string.pan)

        tv_client_gst = v.findViewById(R.id.tv_client_gst)
        tv_client_gst?.setText(R.string.client_gst)

        tv_client_pan = v.findViewById(R.id.tv_client_pan)
        tv_client_pan?.setText(R.string.client_pan)

        tv_select_client = v.findViewById(R.id.tv_select_client)
        tv_select_client?.setText(R.string.select_client)

        tv_client_address = v.findViewById(R.id.tv_client_address)
        tv_client_address?.setText(R.string.client_address)

        et_gst = v.findViewById(R.id.et_gst)
        et_gst?.setHint(R.string.gst)
        et_pan = v.findViewById(R.id.et_pan)
        et_pan?.setHint(R.string.pan)

        et_client_gst = v.findViewById(R.id.et_client_gst)
        et_client_gst?.setHint(R.string.client_gst)

        et_client_pan = v.findViewById(R.id.et_client_pan)
        et_client_pan?.setHint(R.string.client_pan)

        ll_sp_client = v.findViewById(R.id.ll_sp_client)
        sp_client_list = v.findViewById(R.id.sp_client_list)

        ll_sp_matter = v.findViewById(R.id.ll_sp_matter)
        sp_matter_list = v.findViewById(R.id.sp_matter_list)

        tv_select_matter = v.findViewById(R.id.tv_select_matter)
        ll_matter = v.findViewById(R.id.ll_matter)
        ll_matter?.visibility = View.GONE

        et_sp_matter = ll_sp_matter?.findViewById(R.id.tv_spinner_view)
        iv_clear_matter = ll_sp_matter?.findViewById(R.id.img_clear_icon)
        iv_drop_down_matter = ll_sp_matter?.findViewById(R.id.img_dropdown_icon)
        et_sp_matter?.setHint(R.string.select_matters)
        tv_select_matter?.setText(R.string.select_matters_optional)

        et_sp_client = ll_sp_client?.findViewById(R.id.tv_spinner_view)
        iv_clear_client = ll_sp_client?.findViewById(R.id.img_clear_icon)
        iv_drop_down_client = ll_sp_client?.findViewById(R.id.img_dropdown_icon)
        et_sp_client?.setHint(R.string.select_client)

        et_client_address = v.findViewById(R.id.et_client_address)
        et_client_address?.setHint(R.string.client_address)

        tv_address_count = v.findViewById(R.id.tv_address_count)

        rv_line_items = v.findViewById(R.id.rv_line_items)
        cv_add_row = v.findViewById(R.id.btn_add)

        ll_sp_currency = v.findViewById(R.id.ll_sp_currency)
        sp_currency_list = v.findViewById(R.id.sp_currency_list)

        tv_sub_total = v.findViewById(R.id.tv_sub_total)
        tv_discount_value = v.findViewById(R.id.tv_discount_value)
        tv_tax_value = v.findViewById(R.id.tv_tax_value)
        tv_total = v.findViewById(R.id.tv_total)
        et_discount = v.findViewById(R.id.et_discount)
        et_tax = v.findViewById(R.id.et_tax)
        et_additional_details = v.findViewById(R.id.et_additional_details)
        tv_additional_count = v.findViewById(R.id.tv_additional_count)
        tv_additional_details = v.findViewById(R.id.tv_additional_details)

        et_gst?.addTextChangedListener(Validation(et_gst))
        et_pan?.addTextChangedListener(Validation(et_pan))
        et_invoice_no?.addTextChangedListener(Validation(et_invoice_no))
        et_search_invoice?.addTextChangedListener(Validation(et_search_invoice))
        et_tax?.addTextChangedListener(Validation(et_tax))
        et_client_gst?.addTextChangedListener(Validation(et_client_gst))
        et_client_address?.addTextChangedListener(DescriptionValidation(et_client_address))
        et_client_pan?.addTextChangedListener(Validation(et_client_pan))
        et_additional_details?.addTextChangedListener(DescriptionValidation(et_additional_details))
        tv_additional_details?.setText(R.string.additional_details)

        val filters1 = arrayOf<InputFilter>(InputFilter.LengthFilter(250))
        et_additional_details?.setHint(R.string.additional_details)
        et_additional_details?.maxLines = 5
        et_additional_details?.filters = filters1

        val filters = arrayOf<InputFilter>(InputFilter.LengthFilter(150))
        et_client_address?.maxLines = 5
        et_client_address?.filters = filters

        AndroidUtils.setupModuleView(
            tv_switchCreate,
            getString(R.string.view_invoice),
            false, true, requireContext(), getString(R.string.create_invoice)
        ) {
            switchToMode(MODE_LIST)
        }

        val cancelWrapper = v.findViewById<View>(R.id.btn_cancel)
        if (cancelWrapper is AppCompatButton) {
            btn_cancel = cancelWrapper
        } else if (cancelWrapper != null) {
            btn_cancel = cancelWrapper.findViewById(R.id.btn_cancel)
        }

        val saveWrapper = v.findViewById<View>(R.id.btn_save)
        if (saveWrapper is AppCompatButton) {
            btn_save = saveWrapper
        } else if (saveWrapper != null) {
            btn_save = saveWrapper.findViewById(R.id.btn_submit)
            if (btn_save == null) btn_save = saveWrapper.findViewById(R.id.btn_save)
        }

        callClientWebservice()
    }

    private fun setupCurrencySpinner() {
        if (ll_sp_currency == null) return

        val tvCurrencyDisplay: TextView? = ll_sp_currency!!.findViewById(R.id.tv_spinner_view)
        val ivDropdownCurrency: ImageView? = ll_sp_currency!!.findViewById(R.id.img_dropdown_icon)
        val ivClearCurrency: ImageView? = ll_sp_currency!!.findViewById(R.id.img_clear_icon)

        ivClearCurrency?.visibility = View.GONE
        tvCurrencyDisplay?.text = selectedCurrency

        val lvCurrency = ListView(requireContext())
        lvCurrency.divider = android.graphics.drawable.ColorDrawable(Color.parseColor("#E0E0E0"))
        lvCurrency.dividerHeight = dp(1)
        lvCurrency.setBackgroundColor(Color.WHITE)

        val currencyAdapter = object : ArrayAdapter<String>(
            requireContext(), android.R.layout.simple_list_item_1, CURRENCY_LIST
        ) {
            @NonNull
            override fun getView(position: Int, @Nullable convertView: View?, @NonNull parent: ViewGroup): View {
                val tv = super.getView(position, convertView, parent) as TextView
                tv.setPadding(dp(16), dp(14), dp(16), dp(14))
                tv.textSize = 14f
                tv.typeface = Typeface.create("gill-sans-mt", Typeface.NORMAL)
                if (CURRENCY_LIST[position] == selectedCurrency) {
                    tv.setBackgroundColor(
                        ContextCompat.getColor(requireContext(), R.color.Blue_text_color)
                    )
                    tv.setTextColor(Color.WHITE)
                    tv.typeface = Typeface.create("gill-sans-mt", Typeface.BOLD)
                } else {
                    tv.setBackgroundColor(Color.WHITE)
                    tv.setTextColor(Color.parseColor("#333333"))
                }
                return tv
            }
        }

        lvCurrency.adapter = currencyAdapter

        val popupWidth = if (ll_sp_currency!!.width > 0) {
            ll_sp_currency!!.width
        } else {
            (requireContext().resources.displayMetrics.widthPixels * 0.35f).toInt()
        }
        val itemHeight = dp(48)
        val popupHeight = itemHeight * CURRENCY_LIST.size

        val popupWindow = android.widget.PopupWindow(
            lvCurrency, popupWidth, popupHeight, true
        )
        popupWindow.setBackgroundDrawable(android.graphics.drawable.ColorDrawable(Color.WHITE))
        popupWindow.elevation = dp(8).toFloat()
        popupWindow.isOutsideTouchable = true

        var isCurrencyOpen = false

        ll_sp_currency!!.setOnClickListener {
            if (currentMode == MODE_VIEW) return@setOnClickListener
            if (isCurrencyOpen) {
                popupWindow.dismiss()
                isCurrencyOpen = false
                ivDropdownCurrency?.animate()?.rotation(0f)?.setDuration(200)?.start()
            } else {
                val w = if (ll_sp_currency!!.width > 0) {
                    ll_sp_currency!!.width
                } else {
                    (requireContext().resources.displayMetrics.widthPixels * 0.35f).toInt()
                }
                popupWindow.width = w
                currencyAdapter.notifyDataSetChanged()
                popupWindow.showAsDropDown(ll_sp_currency, 0, 0)
                isCurrencyOpen = true
                ivDropdownCurrency?.animate()?.rotation(180f)?.setDuration(200)?.start()
            }
        }

        popupWindow.setOnDismissListener {
            isCurrencyOpen = false
            ivDropdownCurrency?.animate()?.rotation(0f)?.setDuration(200)?.start()
        }

        lvCurrency.setOnItemClickListener { parent, view, position, id ->
            selectedCurrency = CURRENCY_LIST[position]
            tvCurrencyDisplay?.text = selectedCurrency
            popupWindow.dismiss()
            isCurrencyOpen = false
            ivDropdownCurrency?.animate()?.rotation(0f)?.setDuration(200)?.start()
            currencyAdapter.notifyDataSetChanged()
            lineItemAdapter?.setSelectedCurrency(selectedCurrency)
            recalculateTotal()
        }

        sp_currency_list?.visibility = View.GONE
    }

    private fun showLogoPickerDialog() {
        val filecosen = Filecosen(
            requireActivity(),
            object : Filecosen.FileChooserCallback {
                override fun openCamera() {
                    if (requireContext().checkSelfPermission(android.Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
                        this@ViewInvoice.openCamera()
                    } else {
                        requestCameraPermission.launch(android.Manifest.permission.CAMERA)
                    }
                }

                override fun openGallery() {
                    this@ViewInvoice.openGallery()
                }
            }
        )
        filecosen.show()
    }

    private fun openCamera() {
        try {
            val values = android.content.ContentValues()
            values.put(android.provider.MediaStore.Images.Media.TITLE, "Invoice_Logo_${System.currentTimeMillis()}")
            values.put(android.provider.MediaStore.Images.Media.DESCRIPTION, "Invoice Logo")
            cameraImageUri = requireActivity().contentResolver.insert(
                android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values
            )
            if (cameraImageUri != null) {
                cameraLauncher.launch(cameraImageUri!!)
            }
        } catch (e: Exception) {
            Toast.makeText(requireContext(), "Camera error: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }

    private fun openGallery() {
        try {
            val intent = Intent(
                Intent.ACTION_PICK,
                android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI
            )
            intent.type = "image/*"
            galleryLauncher.launch(intent)
        } catch (e: Exception) {
            Toast.makeText(requireContext(), "Gallery error: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }

    private fun handleLogoUri(uri: Uri) {
        logoUri = uri
        logoUrl = ""
        include_logo = true
        pendingLogoUpload = true
        if (iv_logo != null) {
            Glide.with(this).load(uri).centerCrop().into(iv_logo!!)
        }
        ll_upload_logo?.visibility = View.GONE
        cv_logo_preview?.visibility = View.VISIBLE
        iv_remove_logo?.visibility = View.VISIBLE
    }

    private fun loadLogoFromUrl(url: String?) {
        if (url.isNullOrEmpty()) return
        logoUrl = url
        logoUri = null
        include_logo = true
        pendingLogoUpload = false
        if (iv_logo != null) {
            Glide.with(this).load(url).centerCrop()
                .placeholder(R.drawable.rectangle_grey_background)
                .error(R.drawable.rectangle_grey_background)
                .into(iv_logo!!)
        }
        ll_upload_logo?.visibility = View.GONE
        cv_logo_preview?.visibility = View.VISIBLE
        iv_remove_logo?.visibility = if (currentMode == MODE_VIEW) View.GONE else View.VISIBLE
    }

    private fun setupSwitchRow(root: View) {
        tv_switchView?.setOnClickListener {
            if (currentMode == MODE_LIST) {
                switchToMode(MODE_CREATE)
            } else {
                switchToMode(MODE_LIST)
            }
        }
        AndroidUtils.setupModuleView(
            tv_switchView,
            getString(R.string.create_invoice),
            true, true, requireContext(), getString(R.string.view_invoice)
        ) {
            switchToMode(MODE_CREATE)
        }
    }

    private fun switchToMode(mode: String) {
        currentMode = mode

        ll_view_invoice_section?.visibility = View.GONE
        v_create_form?.visibility = View.GONE

        when (mode) {
            MODE_LIST -> {
                tv_switchView?.visibility = View.VISIBLE
                tv_switchCreate?.visibility = View.GONE
                ll_view_invoice_section?.visibility = View.VISIBLE
                setLabel("View Invoice", false)
                callInvoiceListWebservice()
            }

            MODE_CREATE -> {
                tv_switchView?.visibility = View.GONE
                tv_switchCreate?.visibility = View.VISIBLE
                editingModel = null
                suggestedInvoiceCode = ""
                suggestedInvoiceSequence = 0
                v_create_form?.visibility = View.VISIBLE
                setLabel("Create Invoice", true)
                clearForm()
                setFormEditable(true)
                updateSaveButtonLabel()
                callSuggestInvoiceIdWebservice()
                callGetLogoWebservice()
                val sdf = SimpleDateFormat("MMM dd, yyyy", Locale.ENGLISH)
                val todayDate = sdf.format(Date())
                tv_invoice_date?.text = todayDate
            }

            MODE_EDIT -> {
                v_create_form?.visibility = View.VISIBLE
                setLabel("Edit Invoice", true)
                setFormEditable(true)
                updateSaveButtonLabel()
                callGetLogoWebservice()
            }

            MODE_VIEW -> {
                v_create_form?.visibility = View.VISIBLE
                setLabel("Invoice Details", true)
                setFormEditable(false)
                updateSaveButtonLabel()
                callGetLogoWebservice()
            }
        }
    }

    private fun updateSaveButtonLabel() {
        if (btn_save == null) return
        if (currentMode == MODE_VIEW) {
            btn_save?.setText(R.string.download)
            btn_cancel?.text = "Back to List"
        } else if (currentMode == MODE_EDIT) {
            btn_save?.setText(R.string.update)
            btn_cancel?.setText(R.string.cancel)
        } else {
            btn_save?.setText(R.string.save)
            btn_cancel?.setText(R.string.cancel)
        }
    }

    private fun setFormEditable(editable: Boolean) {
        setEditableField(et_invoice_no, editable)
        setEditableField(et_gst, editable)
        setEditableField(et_pan, editable)
        setEditableField(et_client_gst, editable)
        setEditableField(et_client_pan, editable)
        setEditableField(et_client_address, editable)
        setEditableField(et_additional_details, editable)
        setEditableField(et_discount, editable)
        setEditableField(et_tax, editable)

        tv_invoice_date?.isClickable = editable
        tv_due_date?.isClickable = editable
        ll_upload_logo?.isClickable = editable
        iv_remove_logo?.isClickable = editable
        cv_add_row?.visibility = if (editable) View.VISIBLE else View.GONE
        if (ll_sp_client != null) {
            ll_sp_client!!.isClickable = editable
            iv_clear_client?.isEnabled = editable
            iv_drop_down_client?.isEnabled = editable
        }
        if (ll_sp_currency != null) {
            ll_sp_currency!!.isClickable = editable
            ll_sp_currency!!.isEnabled = editable
            val ivDropdown: ImageView? = ll_sp_currency!!.findViewById(R.id.img_dropdown_icon)
            ivDropdown?.visibility = if (editable) View.VISIBLE else View.GONE
            if (!editable) {
                sp_currency_list?.visibility = View.GONE
            }
        }
        lineItemAdapter?.setEditable(editable)
    }

    private fun setEditableField(et: TextInputEditText?, editable: Boolean) {
        if (et == null) return
        et.isEnabled = editable
        et.isFocusable = editable
        et.isFocusableInTouchMode = editable
        et.isClickable = editable
    }

    private fun downloadInvoice() {
        if (editingModel == null) {
            AndroidUtils.showAlert("No invoice selected.", requireActivity())
            return
        }
        val docId = editingModel!!.docid
        if (docId.isEmpty()) {
            AndroidUtils.showAlert("Invoice document ID not found.", requireActivity())
            return
        }
        callDownloadInvoiceWebservice(docId)
    }

    private fun setLabel(label: String, expanded: Boolean) {
        tv_switchView_title?.text = label
        tv_switchView_arrow?.animate()
            ?.rotation(if (expanded) 180f else 0f)
            ?.setDuration(200)?.start()
    }

    private fun callClientWebservice() {
        try {
            progressDialog = AndroidUtils.get_progress(requireActivity())
            WebServiceHelper.callHttpWebService(
                this, requireContext(),
                WebServiceHelper.RestMethodType.GET,
                "v3/client/all/list", "Clients List",
                JSONObject().toString()
            )
        } catch (e: Exception) {
            dismissProgress()
        }
    }

    private fun callCorpClientWebservice() {
        try {
            WebServiceHelper.callHttpWebService(
                this, requireContext(),
                WebServiceHelper.RestMethodType.GET,
                "v3/corporate/list", "Corp Clients List",
                JSONObject().toString()
            )
        } catch (e: Exception) {
            dismissProgress()
        }
    }

    private fun loadClients(data: JSONObject) {
        val relationships = data.getJSONArray("relationships")
        clientsList.clear()
        for (i in 0 until relationships.length()) {
            val obj = relationships.getJSONObject(i)
            val cm = com.digicoffer.lauditor.Documents.Models.ClientsModel()
            cm.id = obj.getString("id")
            cm.name = obj.getString("name")
            cm.type = obj.getString("type")
            clientsList.add(cm)
        }
    }

    private fun loadCorpClients(data: JSONObject) {
        val relationships = data.getJSONArray("relationships")
        CorpClientsList.clear()
        for (i in 0 until relationships.length()) {
            val obj = relationships.getJSONObject(i)
            val cm = com.digicoffer.lauditor.Documents.Models.ClientsModel()
            cm.id = obj.getString("id")
            cm.name = obj.getString("name")
            cm.type = if (obj.getString("type") == "consumer") "consumer" else "corporate"
            CorpClientsList.add(cm)
        }
        clientsList.addAll(CorpClientsList)
        if (clientsList.isEmpty()) {
            ll_sp_client?.visibility = View.GONE
        } else {
            initUI(clientsList)
        }
    }

    private fun initUI(list: ArrayList<com.digicoffer.lauditor.Documents.Models.ClientsModel>) {
        ClientAdapter = CommonSpinnerAdapter(requireActivity(), list)
        sp_client_list?.adapter = ClientAdapter
        sp_client_list?.setOnItemClickListener { parent, view, position, id ->
            val selectedClientItem = parent.getItemAtPosition(position) as com.digicoffer.lauditor.Documents.Models.ClientsModel
            client_id = selectedClientItem.id ?: ""
            client_name = selectedClientItem.name ?: ""
            client_type = if (selectedClientItem.type != null) selectedClientItem.type!! else "consumer"

            AndroidUtils.DisplaySpinnerView(
                sp_client_list, et_sp_client, client_name,
                iv_drop_down_client, iv_clear_client, false, ClientAdapter, "Search Clients"
            )
            callMatterList()
            isClientchecked = true
        }
    }

    private fun initMatterUI() {
        Matteradapter = CommonSpinnerAdapter(requireActivity(), this.mattersList)
        sp_matter_list?.adapter = Matteradapter
        AndroidUtils.LoadList(sp_matter_list, requireContext(), mattersList.size, true)
        isMatterchecked = true
    }

    private fun setupFormListeners() {
        setupCurrencySpinner()

        ll_sp_client?.setOnClickListener {
            if (currentMode == MODE_VIEW) return@setOnClickListener
            if (clientsList.isEmpty()) {
                sp_client_list?.visibility = View.GONE
            } else {
                val isVisible = sp_client_list?.visibility == View.VISIBLE
                AndroidUtils.DisplaySpinnerView(
                    sp_client_list, et_sp_client, client_name,
                    iv_drop_down_client, iv_clear_client, !isVisible, ClientAdapter, "Search Client"
                )
                AndroidUtils.display_listview(isClientchecked, sp_client_list)
            }
            isClientchecked = !isClientchecked
        }

        ll_sp_matter?.setOnClickListener {
            if (currentMode == MODE_VIEW) return@setOnClickListener
            if (mattersList.isEmpty()) {
                sp_matter_list?.visibility = View.GONE
            } else {
                val isVisible = sp_matter_list?.visibility == View.VISIBLE
                AndroidUtils.DisplaySpinnerView(
                    sp_matter_list, et_sp_matter, matter_name,
                    iv_drop_down_matter, iv_clear_matter, !isVisible, Matteradapter, "Search Matter"
                )
                AndroidUtils.display_listview(isMatterchecked, sp_matter_list)
            }
            isMatterchecked = !isMatterchecked
        }

        sp_matter_list?.setOnItemClickListener { parent, view, position, id ->
            val selectedMatter = parent.getItemAtPosition(position) as MattersModel
            matter_id = selectedMatter.id ?: ""
            matter_name = selectedMatter.title ?: ""
            matter_type = selectedMatter.type ?: ""
            et_sp_matter?.text = matter_name
            AndroidUtils.DisplaySpinnerView(
                sp_matter_list, et_sp_matter, matter_name,
                iv_drop_down_matter, iv_clear_matter, false, Matteradapter, "Search Matter"
            )
            isMatterchecked = true
        }

        iv_clear_client?.setOnClickListener {
            matter_id = ""
            matter_name = ""
            matter_type = ""
            client_id = ""
            client_name = ""
            client_type = "consumer"
            AndroidUtils.DisplaySpinnerView(
                sp_client_list, et_sp_client, "",
                iv_drop_down_client, iv_clear_client, false, ClientAdapter, "Search Client"
            )
        }

        iv_clear_matter?.setOnClickListener {
            matter_id = ""
            AndroidUtils.DisplaySpinnerView(
                sp_matter_list, et_sp_matter, "",
                iv_drop_down_matter, iv_clear_matter, false, Matteradapter, "Search Matter"
            )
        }

        ll_upload_logo?.setOnClickListener {
            if (currentMode != MODE_VIEW) {
                showLogoPickerDialog()
            }
        }

        iv_remove_logo?.setOnClickListener {
            if (currentMode == MODE_VIEW) return@setOnClickListener
            logoUri = null
            logoUrl = ""
            include_logo = false
            pendingLogoUpload = false
            iv_logo?.setImageDrawable(null)
            cv_logo_preview?.visibility = View.GONE
            ll_upload_logo?.visibility = View.VISIBLE
        }

        tv_invoice_date?.setOnClickListener {
            if (currentMode != MODE_VIEW) {
                AndroidUtils.showDatePicker(tv_invoice_date, true)
            }
        }

        tv_due_date?.setOnClickListener {
            if (currentMode != MODE_VIEW) {
                AndroidUtils.showDatePicker(tv_due_date, false)
            }
        }

        if (et_client_address != null && tv_address_count != null) {
            et_client_address!!.addTextChangedListener(makeCounter(tv_address_count!!, 150))
        }
        if (et_additional_details != null && tv_additional_count != null) {
            et_additional_details!!.addTextChangedListener(makeCounter(tv_additional_count!!, 250))
        }

        val totalWatcher = object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                recalculateTotal()
            }
            override fun afterTextChanged(s: Editable?) {}
        }
        et_discount?.addTextChangedListener(totalWatcher)
        et_tax?.addTextChangedListener(totalWatcher)

        et_search_invoice?.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                invoiceAdapter?.filter?.filter(s) { count ->
                    updateEmptyState(count == 0)
                }
            }
            override fun afterTextChanged(s: Editable?) {}
        })

        btn_cancel?.setOnClickListener {
            clearForm()
            switchToMode(MODE_LIST)
        }

        btn_save?.setOnClickListener {
            if (currentMode == MODE_VIEW && editingModel != null) {
                downloadInvoice()
            } else {
                if (!validateForm()) return@setOnClickListener
                if (currentMode == MODE_EDIT && editingModel != null) {
                    showSaveChangesDialog()
                } else {
                    submitForm(false)
                }
            }
        }
    }

    private fun updateEmptyState(isEmpty: Boolean) {
        ll_empty_state?.visibility = if (isEmpty) View.VISIBLE else View.GONE
        rv_invoice_list?.visibility = if (isEmpty) View.GONE else View.VISIBLE
    }

    private fun showSaveChangesDialog() {
        if (!isAdded) return

        val root = LinearLayout(requireContext())
        root.orientation = LinearLayout.VERTICAL
        root.setBackgroundColor(Color.WHITE)
        val p = dp(20)
        root.setPadding(p, p, p, p)

        val headerRow = LinearLayout(requireContext())
        headerRow.orientation = LinearLayout.HORIZONTAL
        headerRow.gravity = Gravity.CENTER_VERTICAL

        val tvTitle = TextView(requireContext())
        tvTitle.text = "Save Changes"
        tvTitle.textSize = 18f
        tvTitle.setTextColor(Color.parseColor("#1A3A6B"))
        val typeface1 = ResourcesCompat.getFont(requireContext(), R.font.gill_sans_bold)
        tvTitle.typeface = typeface1
        tvTitle.layoutParams = LinearLayout.LayoutParams(
            0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f
        )
        headerRow.addView(tvTitle)

        val ivClose = ImageView(requireContext())
        ivClose.setImageResource(android.R.drawable.ic_menu_close_clear_cancel)
        ivClose.setColorFilter(Color.GRAY)
        ivClose.layoutParams = LinearLayout.LayoutParams(dp(24), dp(24))
        headerRow.addView(ivClose)
        root.addView(headerRow)

        val divider = View(requireContext())
        divider.setBackgroundColor(Color.parseColor("#E0E0E0"))
        val divLp = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT, dp(1)
        )
        divLp.topMargin = dp(14)
        divLp.bottomMargin = dp(14)
        divider.layoutParams = divLp
        root.addView(divider)

        val tvQuestion = TextView(requireContext())
        val typeface2 = ResourcesCompat.getFont(requireContext(), R.font.gill_sans)
        tvQuestion.typeface = typeface2
        tvQuestion.text = "Do you want to replace the existing invoice or save both?"
        tvQuestion.textSize = 14f
        tvQuestion.setTextColor(Color.DKGRAY)
        val qLp = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT
        )
        qLp.bottomMargin = dp(12)
        tvQuestion.layoutParams = qLp
        root.addView(tvQuestion)

        val optionsCard = LinearLayout(requireContext())
        optionsCard.orientation = LinearLayout.VERTICAL
        optionsCard.setBackgroundColor(Color.parseColor("#F5F5F5"))
        val op = dp(14)
        optionsCard.setPadding(op, op, op, op)

        val replaceRow = LinearLayout(requireContext())
        replaceRow.orientation = LinearLayout.HORIZONTAL
        replaceRow.gravity = Gravity.CENTER_VERTICAL
        val replaceRowLp = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT
        )
        replaceRowLp.bottomMargin = dp(12)
        replaceRow.layoutParams = replaceRowLp

        val ivReplace = ImageView(requireContext())
        ivReplace.setImageResource(R.drawable.recycle_icon)
        ivReplace.clearColorFilter()
        val iconLp = LinearLayout.LayoutParams(dp(28), dp(28))
        iconLp.setMargins(0, 0, dp(10), 0)
        ivReplace.layoutParams = iconLp
        replaceRow.addView(ivReplace)

        val tvReplace = TextView(requireContext())
        val typeface3 = ResourcesCompat.getFont(requireContext(), R.font.gill_sans)
        tvReplace.typeface = typeface3
        tvReplace.textSize = 13f
        tvReplace.setTextColor(Color.DKGRAY)
        val replaceSpan = android.text.SpannableString(
            "Replace: Updates the current invoice with your changes"
        )
        replaceSpan.setSpan(
            android.text.style.StyleSpan(Typeface.BOLD), 0, 8,
            android.text.Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
        )
        tvReplace.text = replaceSpan
        replaceRow.addView(tvReplace)
        optionsCard.addView(replaceRow)

        val saveBothRow = LinearLayout(requireContext())
        saveBothRow.orientation = LinearLayout.HORIZONTAL
        saveBothRow.gravity = Gravity.CENTER_VERTICAL

        val ivSaveBoth = ImageView(requireContext())
        ivSaveBoth.setImageResource(R.drawable.blue_copy)
        ivSaveBoth.clearColorFilter()
        val iconLp2 = LinearLayout.LayoutParams(dp(28), dp(28))
        iconLp2.setMargins(0, 0, dp(10), 0)
        ivSaveBoth.layoutParams = iconLp2
        saveBothRow.addView(ivSaveBoth)

        val tvSaveBoth = TextView(requireContext())
        val typeface4 = ResourcesCompat.getFont(requireContext(), R.font.gill_sans)
        tvSaveBoth.typeface = typeface4
        tvSaveBoth.textSize = 13f
        tvSaveBoth.setTextColor(Color.DKGRAY)
        val saveBothSpan = android.text.SpannableString(
            "Save Both: Creates a new invoice while keeping the original"
        )
        saveBothSpan.setSpan(
            android.text.style.StyleSpan(Typeface.BOLD), 0, 10,
            android.text.Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
        )
        tvSaveBoth.text = saveBothSpan
        saveBothRow.addView(tvSaveBoth)
        optionsCard.addView(saveBothRow)

        val optionsCardLp = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT
        )
        optionsCardLp.bottomMargin = dp(8)
        optionsCard.layoutParams = optionsCardLp
        root.addView(optionsCard)

        val btnRow = LinearLayout(requireContext())
        btnRow.orientation = LinearLayout.HORIZONTAL
        btnRow.gravity = Gravity.END
        val btnRowLp = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT
        )
        btnRowLp.topMargin = dp(20)
        btnRowLp.bottomMargin = dp(8)
        btnRow.layoutParams = btnRowLp

        val btnReplace = AppCompatButton(requireContext())
        btnReplace.text = "Replace"
        btnReplace.isAllCaps = false
        btnReplace.setTextColor(Color.WHITE)
        btnReplace.textSize = 14f
        val typeface6 = ResourcesCompat.getFont(requireContext(), R.font.gill_sans)
        btnReplace.typeface = typeface6
        btnReplace.setBackgroundColor(Color.parseColor("#757575"))
        var replaceIcon = ContextCompat.getDrawable(requireContext(), R.drawable.recycle_icon)
        if (replaceIcon != null) {
            replaceIcon = replaceIcon.mutate()
            replaceIcon.setColorFilter(Color.WHITE, android.graphics.PorterDuff.Mode.SRC_IN)
            replaceIcon.setBounds(0, 0, dp(18), dp(18))
        }
        btnReplace.setCompoundDrawables(replaceIcon, null, null, null)
        btnReplace.compoundDrawablePadding = dp(6)
        val replaceBtnLp = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.WRAP_CONTENT, dp(48)
        )
        replaceBtnLp.setMargins(0, 0, dp(10), 0)
        btnReplace.layoutParams = replaceBtnLp
        btnReplace.setPadding(dp(16), 0, dp(16), 0)

        val btnSaveBoth = AppCompatButton(requireContext())
        btnSaveBoth.text = "Save Both"
        btnSaveBoth.isAllCaps = false
        btnSaveBoth.setTextColor(Color.WHITE)
        btnSaveBoth.textSize = 14f
        val typeface5 = ResourcesCompat.getFont(requireContext(), R.font.gill_sans)
        btnSaveBoth.typeface = typeface5
        btnSaveBoth.setBackgroundColor(Color.parseColor("#1A3A6B"))
        var saveBothIcon = ContextCompat.getDrawable(requireContext(), R.drawable.blue_copy)
        if (saveBothIcon != null) {
            saveBothIcon = saveBothIcon.mutate()
            saveBothIcon.setColorFilter(Color.WHITE, android.graphics.PorterDuff.Mode.SRC_IN)
            saveBothIcon.setBounds(0, 0, dp(18), dp(18))
        }
        btnSaveBoth.setCompoundDrawables(saveBothIcon, null, null, null)
        btnSaveBoth.compoundDrawablePadding = dp(6)
        val saveBothBtnLp = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.WRAP_CONTENT, dp(48)
        )
        btnSaveBoth.layoutParams = saveBothBtnLp
        btnSaveBoth.setPadding(dp(16), 0, dp(16), 0)

        btnRow.addView(btnReplace)
        btnRow.addView(btnSaveBoth)
        root.addView(btnRow)

        val dialog = AlertDialog.Builder(requireActivity()).setView(root).create()
        if (dialog.window != null) {
            dialog.window!!.setBackgroundDrawableResource(android.R.color.transparent)
        }

        ivClose.setOnClickListener { dialog.dismiss() }
        btnReplace.setOnClickListener {
            dialog.dismiss()
            submitForm(true)
        }
        btnSaveBoth.setOnClickListener {
            dialog.dismiss()
            submitForm(false)
        }

        dialog.show()
    }

    private fun submitForm(replace: Boolean) {
        this.replaceOriginal = replace
        if (pendingLogoUpload && logoUri != null) {
            callUploadLogoWebservice(logoUri!!)
        } else {
            saveInvoice()
        }
    }

    private fun saveInvoice() {
        try {
            val payload = buildPayload()
            Log.d("INVOICE_PAYLOAD", payload.toString())
            if (MODE_EDIT == currentMode && editingModel != null) {
                callPatchInvoiceWebservice(editingModel!!.id, payload)
            } else {
                callCreateInvoiceWebservice(payload)
            }
        } catch (e: JSONException) {
            AndroidUtils.showAlert(e.message, requireActivity())
        }
    }

    @SuppressLint("DefaultLocale")
    private fun buildPayload(): JSONObject {
        val payload = JSONObject()

        val invoiceCodeToSend = if (suggestedInvoiceCode.isEmpty()) {
            getText(et_invoice_no)
        } else {
            suggestedInvoiceCode
        }
        payload.put("invoice_code", invoiceCodeToSend)
        payload.put("firm_gst_no", getText(et_gst))
        payload.put("firm_pan_no", getText(et_pan))
        payload.put("client_gst_no", getText(et_client_gst))
        payload.put("client_pan_no", getText(et_client_pan))
        payload.put("name", if (client_name.isEmpty()) "Invoice" else client_name)
        payload.put("billto", getText(et_client_address))
        payload.put("notes", getText(et_additional_details))
        payload.put("currency_code", selectedCurrency)
        payload.put("include_logo", include_logo)
        payload.put("matters", JSONArray())

        if (MODE_EDIT != currentMode) {
            payload.put("invoice_sequence", suggestedInvoiceSequence)
        }

        val invoiceDateApi = if (tv_invoice_date?.tag != null) {
            tv_invoice_date!!.tag.toString()
        } else {
            AndroidUtils.convertAnyDateToYYYYMMDD(tv_invoice_date?.text?.toString() ?: "")
        }
        val dueDateApi = if (tv_due_date?.tag != null) {
            tv_due_date!!.tag.toString()
        } else {
            AndroidUtils.convertAnyDateToYYYYMMDD(tv_due_date?.text?.toString() ?: "")
        }

        val info = JSONObject()
        info.put("date", invoiceDateApi)
        info.put("duedate", dueDateApi)
        payload.put("info", info)

        val invoiceItems = JSONArray()
        for (item in lineItems) {
            val obj = JSONObject()
            obj.put("name", if (item.name != null) item.name else "")
            obj.put("unitPrice", item.unitPrice.toInt())
            obj.put("quantity", if (item.quantity > 0) item.quantity else 1)
            invoiceItems.put(obj)
        }
        payload.put("invoice_items", invoiceItems)

        val subTotal = computeSubTotal()
        val taxPct = parseDouble(getText(et_tax))
        val discPct = parseDouble(getText(et_discount))

        val taxObj = JSONObject()
        taxObj.put("tax_type", "Tax")
        taxObj.put("amount", subTotal)
        taxObj.put("tax", taxPct)
        payload.put("tax_items", JSONArray().put(taxObj))

        val discObj = JSONObject()
        discObj.put("discount_type", "Discount")
        discObj.put("amount", subTotal)
        discObj.put("discount", discPct)
        payload.put("discounts", JSONArray().put(discObj))

        val clientObj = JSONObject()
        if (client_id.isNotEmpty()) {
            clientObj.put("id", client_id)
            clientObj.put("type", client_type)
        }
        payload.put("client", JSONArray().put(clientObj))

        val matterobj = JSONObject()
        if (matter_id.isNotEmpty()) {
            matterobj.put("id", matter_id)
            matterobj.put("name", matter_name)
            matterobj.put("type", matter_type)
        }
        payload.put("matters", JSONArray().put(matterobj))

        if (MODE_EDIT == currentMode) {
            payload.put("replace_original", replaceOriginal)
        }

        return payload
    }

    private fun computeSubTotal(): Double {
        var sub = 0.0
        for (item in lineItems) {
            sub += item.getAmount()
        }
        return sub
    }

    private fun callInvoiceListWebservice() {
        try {
            progressDialog = AndroidUtils.get_progress(requireActivity())
            WebServiceHelper.callHttpWebService(
                this, requireContext(),
                WebServiceHelper.RestMethodType.GET,
                "v3/invoice", "Invoice List",
                JSONObject().toString()
            )
        } catch (e: Exception) {
            dismissProgress()
        }
    }

    private fun callDownloadInvoiceWebservice(id: String) {
        try {
            progressDialog = AndroidUtils.get_progress(requireActivity())
            WebServiceHelper.callHttpWebService(
                this, requireContext(),
                WebServiceHelper.RestMethodType.GET,
                "v3/document/$id/download?module=invoice",
                "Download Invoice",
                JSONObject().toString()
            )
        } catch (e: Exception) {
            dismissProgress()
        }
    }

    private fun callSuggestInvoiceIdWebservice() {
        try {
            WebServiceHelper.callHttpWebService(
                this, requireContext(),
                WebServiceHelper.RestMethodType.GET,
                "v3/invoice/suggest-id", "Suggest Invoice Id",
                JSONObject().toString()
            )
        } catch (e: Exception) {
            // non-critical
        }
    }

    private fun callGetLogoWebservice() {
        try {
            WebServiceHelper.callHttpWebService(
                this, requireContext(),
                WebServiceHelper.RestMethodType.GET,
                "invoice/logoget", "Get Invoice Logo",
                JSONObject().toString()
            )
        } catch (e: Exception) {
            // non-critical
        }
    }

    private fun callUploadLogoWebservice(imageUri: Uri) {
        progressDialog = AndroidUtils.get_progress(requireActivity())
        try {
            val imageFile = getFileFromUri(imageUri)
            val jsonObject = JSONObject()
            jsonObject.put("type", "logo")
            WebServiceHelper.callHttpUploadWebService(
                this, requireContext(),
                WebServiceHelper.RestMethodType.POST,
                "invoice/logoup", "Upload Logo",
                imageFile, jsonObject.toString()
            )
        } catch (e: Exception) {
            Log.e("LOGO_UPLOAD", "Upload failed: ${e.message}", e)
            dismissProgress()
            pendingLogoUpload = false
            saveInvoice()
        }
    }

    private fun getFileFromUri(uri: Uri): File {
        if (!isAdded) throw Exception("Fragment not attached")
        val inputStream = requireActivity().contentResolver.openInputStream(uri)
            ?: throw Exception("Unable to open InputStream for URI: $uri")
        val file = File(
            requireActivity().cacheDir,
            "invoice_logo_${System.currentTimeMillis()}.jpg"
        )
        val outputStream = FileOutputStream(file)
        val buffer = ByteArray(4096)
        var read: Int
        var totalBytes = 0L
        while (inputStream.read(buffer).also { read = it } != -1) {
            outputStream.write(buffer, 0, read)
            totalBytes += read
        }
        outputStream.flush()
        outputStream.close()
        inputStream.close()
        if (totalBytes == 0L) throw Exception("File is empty (0 bytes)")
        Log.d("LOGO_UPLOAD", "File: ${file.absolutePath} size: ${file.length()} bytes")
        return file
    }

    private fun callInvoiceDetailWebservice(id: String, requestType: String) {
        try {
            progressDialog = AndroidUtils.get_progress(requireActivity())
            WebServiceHelper.callHttpWebService(
                this, requireContext(),
                WebServiceHelper.RestMethodType.GET,
                "v3/invoice/$id", requestType,
                JSONObject().toString()
            )
        } catch (e: Exception) {
            dismissProgress()
        }
    }

    private fun callCreateInvoiceWebservice(data: JSONObject) {
        try {
            progressDialog = AndroidUtils.get_progress(requireActivity())
            WebServiceHelper.callHttpWebService(
                this, requireContext(),
                WebServiceHelper.RestMethodType.POST,
                "v3/invoice", "Create Invoice",
                data.toString()
            )
        } catch (e: Exception) {
            dismissProgress()
        }
    }

    private fun callPatchInvoiceWebservice(id: String, data: JSONObject) {
        try {
            progressDialog = AndroidUtils.get_progress(requireActivity())
            WebServiceHelper.callHttpWebService(
                this, requireContext(),
                WebServiceHelper.RestMethodType.PATCH,
                "v3/invoice/$id", "Edit Invoice",
                data.toString()
            )
        } catch (e: Exception) {
            dismissProgress()
        }
    }

    private fun callChangeStatusWebservice(id: String, status: String) {
        try {
            val data = JSONObject()
            data.put("status", status)
            progressDialog = AndroidUtils.get_progress(requireActivity())
            WebServiceHelper.callHttpWebService(
                this, requireContext(),
                WebServiceHelper.RestMethodType.PATCH,
                "v3/invoice/$id", "Change Invoice Status",
                data.toString()
            )
        } catch (e: Exception) {
            dismissProgress()
        }
    }

    private fun callDeleteInvoiceWebservice(id: String) {
        try {
            progressDialog = AndroidUtils.get_progress(requireActivity())
            WebServiceHelper.callHttpWebService(
                this, requireContext(),
                WebServiceHelper.RestMethodType.DELETE,
                "v3/invoice/$id", "Delete Invoice",
                JSONObject().toString()
            )
        } catch (e: Exception) {
            dismissProgress()
        }
    }

    private fun callMatterList() {
        try {
            progressDialog = AndroidUtils.get_progress(requireActivity())
            val jsonObject = JSONObject()
            Log.d("Client_id", client_id)
            WebServiceHelper.callHttpWebService(
                this, requireContext(),
                WebServiceHelper.RestMethodType.GET,
                "v3/matter/all/$client_id", "Matters List",
                jsonObject.toString()
            )
        } catch (e: Exception) {
            if (progressDialog != null && progressDialog!!.isShowing) {
                AndroidUtils.dismiss_dialog(progressDialog)
            }
        }
    }

    private fun callShareInvoiceWebservice(model: InvoiceModel, message: String) {
        try {
            val docObj = JSONObject()
            docObj.put("docid", model.docid)
            docObj.put("doctype", "general")
            docObj.put("matters", JSONArray())

            val payload = JSONObject()
            payload.put("add", JSONArray().put(docObj))
            payload.put("message", message)
            payload.put("remove", JSONArray())

            progressDialog = AndroidUtils.get_progress(requireActivity())
            WebServiceHelper.callHttpWebService(
                this, requireContext(),
                WebServiceHelper.RestMethodType.PUT,
                "v2/relationship/${model.rel_id}/docs/share?module=invoice",
                "Share Invoice",
                payload.toString()
            )
        } catch (e: JSONException) {
            AndroidUtils.showAlert(e.message, requireActivity())
        }
    }

    override fun onAsyncTaskComplete(httpResult: HttpResultDo?) {
        dismissProgress()
        if (httpResult == null) return

        try {
            if (httpResult.result == WebServiceHelper.ServiceCallStatus.Success) {
                val result = JSONObject(httpResult.responseContent ?: "")
                val error = result.optBoolean("error", false)

                when (httpResult.requestType) {
                    "Invoice List" -> {
                        if (!error) {
                            val data = result.optJSONArray("data")
                            if (data != null) loadInvoiceList(data)
                        } else {
                            AndroidUtils.showAlert(result.optString("msg"), requireActivity())
                        }
                    }

                    "Download Invoice" -> {
                        val dData = result.optJSONObject("data")
                        if (dData != null) {
                            val url1 = dData.optString("url")
                            AndroidUtils.showAlert(
                                "You have successfully downloaded the document.",
                                requireActivity(), "Success"
                            )
                            FileDownloader.downloadFile(requireContext(), url1, "Invoice_Download")
                        }
                    }

                    "Suggest Invoice Id" -> {
                        if (!error) {
                            val data = result.optJSONObject("data")
                            if (data != null) {
                                suggestedInvoiceCode = data.optString("invoice_code", "")
                                suggestedInvoiceSequence = data.optInt("invoice_sequence", 0)
                                if (et_invoice_no != null) {
                                    et_invoice_no?.setText(suggestedInvoiceCode)
                                    et_invoice_no?.isEnabled = true
                                    et_invoice_no?.isFocusable = true
                                    et_invoice_no?.isFocusableInTouchMode = true
                                }
                            }
                        }
                    }

                    "Clients List" -> {
                        val data1 = result.getJSONObject("data")
                        callCorpClientWebservice()
                        loadClients(data1)
                        if (client_id.isNotEmpty()) callMatterList()
                    }

                    "Matters List" -> {
                        if (error) {
                            val msg = result.getString("msg")
                            AndroidUtils.showAlert(msg, requireActivity())
                        } else {
                            val matters = result.getJSONArray("matterList")
                            loadMatters(matters)
                        }
                    }

                    "Corp Clients List" -> {
                        loadCorpClients(result)
                    }

                    "Get Invoice Logo" -> {
                        if (!error) {
                            val data = result.optJSONObject("data")
                            if (data != null) {
                                val logoError = data.optBoolean("error", false)
                                if (!logoError) {
                                    val url = data.optString("url", "")
                                    if (url.isNotEmpty()) loadLogoFromUrl(url)
                                }
                            }
                        }
                    }

                    "Upload Logo" -> {
                        pendingLogoUpload = false
                        if (!error) {
                            Log.d("LOGO_UPLOAD", "Logo uploaded OK, saving invoice...")
                        } else {
                            Log.w("LOGO_UPLOAD", "Logo upload error: ${result.optString("msg")}")
                        }
                        saveInvoice()
                    }

                    "Invoice View" -> {
                        if (!error) {
                            val obj = result.optJSONObject("invoice")
                            if (obj != null) {
                                val m = buildDetailModel(obj)
                                editingModel = m
                                populateForm(m)
                                switchToMode(MODE_VIEW)
                            }
                        } else {
                            AndroidUtils.showAlert(result.optString("msg"), requireActivity())
                        }
                    }

                    "Invoice Detail" -> {
                        if (!error) {
                            val obj = result.optJSONObject("invoice")
                            if (obj != null) {
                                val m = buildDetailModel(obj)
                                editingModel = m
                                populateForm(m)
                                switchToMode(MODE_EDIT)
                            }
                        } else {
                            AndroidUtils.showAlert(result.optString("msg"), requireActivity())
                        }
                    }

                    "Create Invoice" -> {
                        if (!error) {
                            AndroidUtils.showAlert("Invoice created successfully.", requireActivity())
                            clearForm()
                            switchToMode(MODE_LIST)
                        } else {
                            AndroidUtils.showAlert(
                                result.optString("msg", "Failed to create invoice."),
                                requireActivity()
                            )
                        }
                    }

                    "Edit Invoice" -> {
                        if (!error) {
                            AndroidUtils.showAlert(
                                result.optString("msg", "Invoice updated successfully."),
                                requireActivity()
                            )
                            clearForm()
                            switchToMode(MODE_LIST)
                        } else {
                            AndroidUtils.showAlert(
                                result.optString("msg", "Update failed."),
                                requireActivity()
                            )
                        }
                    }

                    "Change Invoice Status" -> {
                        AndroidUtils.showAlert(
                            result.optString("msg", if (error) "Status update failed." else "Status updated."),
                            requireActivity()
                        )
                        if (!error) callInvoiceListWebservice()
                    }

                    "Delete Invoice" -> {
                        AndroidUtils.showAlert(
                            result.optString("msg", if (error) "Delete failed." else "Invoice deleted."),
                            requireActivity()
                        )
                        if (!error) callInvoiceListWebservice()
                    }

                    "Share Invoice" -> {
                        AndroidUtils.showAlert(
                            result.optString("msg", if (error) "Failed to share invoice." else "Invoice shared successfully."),
                            requireActivity()
                        )
                    }
                }

            } else {
                try {
                    val r = JSONObject(httpResult.responseContent ?: "")
                    AndroidUtils.showErrorAlert(r.optString("msg"), requireActivity())
                } catch (ignored: Exception) {
                    AndroidUtils.showErrorAlert(httpResult.responseContent ?: "", requireActivity())
                }
            }
        } catch (e: JSONException) {
            e.printStackTrace()
        }
    }

    private fun loadMatters(matters: JSONArray) {
        mattersList.clear()
        for (i in 0 until matters.length()) {
            val jsonObject = matters.getJSONObject(i)
            val mattersModel = MattersModel()
            mattersModel.id = jsonObject.optString("id")
            mattersModel.title = jsonObject.optString("title")
            mattersModel.type = jsonObject.optString("type")
            mattersList.add(mattersModel)
        }
        if (mattersList.isEmpty()) {
            ll_matter?.visibility = View.GONE
        } else {
            ll_matter?.visibility = View.VISIBLE
        }
        initMatterUI()
    }

    private fun setupLineItemsTable() {
        if (rv_line_items == null) return
        rv_line_items!!.background = ContextCompat.getDrawable(requireContext(), R.drawable.rectangular_white_background)
        rv_line_items!!.layoutManager = LinearLayoutManager(requireContext())
        lineItemAdapter = LineItemAdapter(lineItems) { recalculateTotal() }

        lineItemAdapter?.setOnCurrencyChangedListener(object : LineItemAdapter.OnCurrencyChangedListener {
            override fun onCurrencyChanged(currency: String) {
                selectedCurrency = currency
                if (ll_sp_currency != null) {
                    val tvCurrencyDisplay: TextView? = ll_sp_currency!!.findViewById(R.id.tv_spinner_view)
                    tvCurrencyDisplay?.text = selectedCurrency
                }
                recalculateTotal()
            }
        })

        rv_line_items!!.adapter = lineItemAdapter
        rv_line_items!!.isNestedScrollingEnabled = false
        addLineItemRow()
        cv_add_row?.setOnClickListener { addLineItemRow() }
    }

    private fun addLineItemRow() {
        lineItems.add(LineItemModel())
        lineItemAdapter?.notifyItemInserted(lineItems.size - 1)
        recalculateTotal()
    }

    @SuppressLint("DefaultLocale")
    fun recalculateTotal() {
        var subTotal = 0.0
        for (item in lineItems) {
            subTotal += item.getAmount()
        }

        val sym = if ("INR" == selectedCurrency) "₹ " else "$selectedCurrency "

        tv_sub_total?.text = sym + String.format(Locale.getDefault(), "%.2f", subTotal)

        val discPct = parseDouble(getText(et_discount))
        val taxPct = parseDouble(getText(et_tax))

        val discountAmount = subTotal * discPct / 100.0
        val afterDiscount = subTotal - discountAmount

        val taxAmount = afterDiscount * taxPct / 100.0

        val total = afterDiscount + taxAmount

        tv_discount_value?.text = sym + String.format(Locale.getDefault(), "%.2f", discountAmount)
        tv_tax_value?.text = sym + String.format(Locale.getDefault(), "%.2f", taxAmount)
        tv_total?.text = sym + String.format(Locale.getDefault(), "%.2f", total)
    }

    private fun clearForm() {
        editingModel = null
        logoUri = null
        logoUrl = ""
        include_logo = true
        pendingLogoUpload = false
        suggestedInvoiceCode = ""
        suggestedInvoiceSequence = 0
        replaceOriginal = false

        client_id = ""
        client_name = ""
        client_type = "consumer"
        matter_id = ""
        matter_name = ""
        matter_type = ""

        selectedCurrency = "INR"
        if (ll_sp_currency != null) {
            val tvCurrencyDisplay: TextView? = ll_sp_currency!!.findViewById(R.id.tv_spinner_view)
            tvCurrencyDisplay?.text = "INR"
            sp_currency_list?.visibility = View.GONE
        }
        lineItemAdapter?.setSelectedCurrency("INR")

        iv_logo?.setImageDrawable(null)
        cv_logo_preview?.visibility = View.GONE
        ll_upload_logo?.visibility = View.VISIBLE

        if (et_sp_client != null && iv_drop_down_client != null && iv_clear_client != null && sp_client_list != null) {
            AndroidUtils.DisplaySpinnerView(
                sp_client_list, et_sp_client, "",
                iv_drop_down_client, iv_clear_client, false, ClientAdapter, "Search Client"
            )
        }
        if (et_sp_matter != null && iv_drop_down_matter != null && iv_clear_matter != null && sp_matter_list != null) {
            AndroidUtils.DisplaySpinnerView(
                sp_matter_list, et_sp_matter, "",
                iv_drop_down_matter, iv_clear_matter, false, Matteradapter, "Search Matter"
            )
        }
        ll_matter?.visibility = View.GONE

        if (et_invoice_no != null) {
            et_invoice_no?.setText("")
            et_invoice_no?.isEnabled = true
            et_invoice_no?.isFocusable = true
            et_invoice_no?.isFocusableInTouchMode = true
        }

        setText(et_gst, "")
        setText(et_pan, "")
        setText(et_client_gst, "")
        setText(et_client_pan, "")
        setText(et_client_address, "")
        setText(et_additional_details, "")
        setText(et_discount, "")
        setText(et_tax, "")

        tv_invoice_date?.text = ""
        tv_due_date?.text = ""

        tv_sub_total?.text = "₹ 0.00"
        tv_discount_value?.text = "₹ 0.00"
        tv_tax_value?.text = "₹ 0.00"
        tv_total?.text = "₹ 0.00"

        tv_address_count?.text = "0/150"
        tv_additional_count?.text = "0/250"

        lineItems.clear()
        lineItemAdapter?.notifyDataSetChanged()
        addLineItemRow()

        btn_save?.text = "Save"
    }

    private fun populateForm(m: InvoiceModel) {
        setText(et_invoice_no, m.invoice_no)
        setText(et_gst, m.firmGstNo)
        setText(et_pan, m.firmPanNo)
        setText(et_client_gst, m.clientGstNo)
        setText(et_client_pan, m.clientPanNo)
        setText(et_client_address, m.billto)
        setText(et_additional_details, m.notes)

        tv_invoice_date?.text = AndroidUtils.formatToMMMddYYYY(m.date)
        tv_due_date?.text = AndroidUtils.formatToMMMddYYYY(m.dueDate)

        client_id = if (m.client_id != null) m.client_id!! else ""
        matter_id = if (m.MatterId != null) m.MatterId!! else ""
        matter_name = if (m.MatterName != null) m.MatterName!! else ""
        matter_type = if (m.matter_type != null) m.matter_type!! else ""
        client_type = if (m.client_type != null) m.client_type!! else "consumer"
        client_name = if (m.name != null) m.name!! else ""

        if (et_sp_client != null && iv_drop_down_client != null && iv_clear_client != null && sp_client_list != null && client_name.isNotEmpty()) {
            AndroidUtils.DisplaySpinnerView(
                sp_client_list, et_sp_client, client_name,
                iv_drop_down_client, iv_clear_client, false, ClientAdapter, "Search Client"
            )
        }

        if (et_sp_matter != null && iv_drop_down_matter != null && iv_clear_matter != null && sp_matter_list != null && matter_name.isNotEmpty()) {
            AndroidUtils.DisplaySpinnerView(
                sp_matter_list, et_sp_matter, matter_name,
                iv_drop_down_matter, iv_clear_matter, false, Matteradapter, "Search Matter"
            )
            ll_matter?.visibility = View.VISIBLE
        }

        selectedCurrency = if (m.currencyCode != null && m.currencyCode.isNotEmpty()) m.currencyCode!! else "INR"
        if (ll_sp_currency != null) {
            val tvCurrencyDisplay: TextView? = ll_sp_currency!!.findViewById(R.id.tv_spinner_view)
            tvCurrencyDisplay?.text = selectedCurrency
        }
        lineItemAdapter?.setSelectedCurrency(selectedCurrency)

        if (m.logoUrl != null && m.logoUrl!!.isNotEmpty()) {
            loadLogoFromUrl(m.logoUrl)
        } else {
            logoUri = null
            logoUrl = ""
            include_logo = m.includeLogo
            pendingLogoUpload = false
            iv_logo?.setImageDrawable(null)
            cv_logo_preview?.visibility = View.GONE
            ll_upload_logo?.visibility = View.VISIBLE
        }

        lineItems.clear()
        if (m.lineItems != null && m.lineItems.isNotEmpty()) {
            lineItems.addAll(m.lineItems)
        } else {
            lineItems.add(LineItemModel())
        }
        lineItemAdapter?.notifyDataSetChanged()

        setText(
            et_tax,
            if (m.taxAmount == 0.0) "" else m.taxAmount.toInt().toString()
        )
        setText(
            et_discount,
            if (m.discountAmount == 0.0) "" else m.discountAmount.toInt().toString()
        )

        recalculateTotal()
        callClientWebservice()
    }

    private fun validateForm(): Boolean {
        if (tv_invoice_date == null || tv_invoice_date!!.text.toString().isEmpty()) {
            AndroidUtils.showAlert("Please select Invoice Date.", requireActivity())
            return false
        }
        if (tv_due_date == null || tv_due_date!!.text.toString().isEmpty()) {
            AndroidUtils.showAlert("Please select Due Date.", requireActivity())
            return false
        }
        if (client_id.isEmpty()) {
            AndroidUtils.showAlert("Please select a Client.", requireActivity())
            return false
        }
        if (getText(et_client_address).isEmpty()) {
            AndroidUtils.showAlert("Please enter Client Address.", requireActivity())
            return false
        }
        return true
    }

    private fun loadInvoiceList(data: JSONArray) {
        try {
            invoiceList.clear()
            for (i in 0 until data.length()) {
                val obj = data.optJSONObject(i)
                if (obj != null) invoiceList.add(buildInvoiceModel(obj))
            }
            loadInvoiceRecyclerView()
        } catch (e: Exception) {
            AndroidUtils.showAlert(e.message, requireActivity())
        }
    }

    private fun buildInvoiceModel(obj: JSONObject): InvoiceModel {
        val m = InvoiceModel()
        m.id = obj.optString("id")
        m.name = obj.optString("name")
        m.invoice_no = obj.optString("invoice_no")
        m.createdby = obj.optString("createdby")
        m.status = obj.optString("status")
        m.date = obj.optString("date")
        m.dueDate = obj.optString("dueDate")
        m.created = obj.optString("created")
        m.isdisabled = obj.optBoolean("isdisabled", false)
        m.client_id = obj.optString("client_id")
        m.client_type = obj.optString("client_type")
        m.docid = obj.optString("docid")
        m.document_status = obj.optString("document_status")
        m.rel_id = obj.optString("rel_id")
        m.can_share = obj.optBoolean("can_share", false)
        return m
    }

    private fun buildDetailModel(obj: JSONObject): InvoiceModel {
        val m = InvoiceModel()
        m.id = obj.optString("id")
        m.name = obj.optString("name")
        m.invoice_no = obj.optString("invoice_no")
        m.invoiceSequence = obj.optInt("invoice_sequence", 0)
        m.status = obj.optString("status")
        m.billto = obj.optString("billto")
        m.notes = obj.optString("notes")
        m.currencyCode = obj.optString("currency_code", "INR")
        m.includeLogo = obj.optBoolean("include_logo", true)
        m.docid = obj.optString("docid")
        m.document_status = obj.optString("document_status")
        m.firmGstNo = obj.optString("firm_gst_no")
        m.firmPanNo = obj.optString("firm_pan_no")
        m.clientGstNo = obj.optString("client_gst_no")
        m.clientPanNo = obj.optString("client_pan_no")
        m.rel_id = obj.optString("rel_id")
        m.can_share = obj.optBoolean("can_share", false)

        if (obj.has("client")) {
            val clientArray = obj.optJSONArray("client")
            if (clientArray != null && clientArray.length() > 0) {
                val clientObj = clientArray.optJSONObject(0)
                if (clientObj != null) {
                    m.client_id = clientObj.optString("id")
                    m.client_type = clientObj.optString("type")
                }
            }
        }

        if (obj.has("matters")) {
            val mattersArray = obj.optJSONArray("matters")
            if (mattersArray != null && mattersArray.length() > 0) {
                val matterObj = mattersArray.optJSONObject(0)
                if (matterObj != null) {
                    m.MatterName = matterObj.optString("name")
                    m.MatterId = matterObj.optString("id")
                    m.matter_type = matterObj.optString("type")
                }
            }
        }

        val logo = obj.optString(
            "logo_url", obj.optString("logoUrl", obj.optString("logo", ""))
        )
        m.logoUrl = logo
        m.date = obj.optString("date")
        m.dueDate = obj.optString("duedate", obj.optString("dueDate"))

        val items = obj.optJSONArray("items")
        val list = ArrayList<LineItemModel>()
        if (items != null) {
            for (i in 0 until items.length()) {
                val it = items.optJSONObject(i) ?: continue
                val li = LineItemModel()
                li.name = it.optString("name")
                li.unitPrice = it.optDouble("unit_price", 0.0)
                li.quantity = it.optInt("no_of_units", 1)
                list.add(li)
            }
        }
        m.lineItems = list

        val taxItems = obj.optJSONArray("tax_items")
        if (taxItems != null && taxItems.length() > 0) {
            val t = taxItems.optJSONObject(0)
            if (t != null) m.taxAmount = t.optDouble("tax", 0.0)
        }

        val discounts = obj.optJSONArray("discounts")
        if (discounts != null && discounts.length() > 0) {
            val d = discounts.optJSONObject(0)
            if (d != null) m.discountAmount = d.optDouble("discount", 0.0)
        }
        return m
    }

    private fun loadInvoiceRecyclerView() {
        if (rv_invoice_list == null) return

        invoiceAdapter = ViewInvoiceAdapter(invoiceList, requireContext(), this)
        invoiceAdapter?.setRecyclerView(rv_invoice_list)
        rv_invoice_list?.adapter = invoiceAdapter
        AndroidUtils.LoadingRecyclerview(rv_invoice_list, requireContext())
        AndroidUtils.setupBottomSpacerFooter(
            rv_invoice_list, getResources().getDimensionPixelSize(R.dimen.twentyeight_dp)
        )

        updateEmptyState(invoiceList.isEmpty())

        if (et_search_invoice != null) {
            val search = et_search_invoice!!.text.toString()
            if (search.isNotEmpty()) {
                invoiceAdapter?.filter?.filter(search) { count ->
                    updateEmptyState(count == 0)
                }
            }
        }
    }

    override fun onViewDetails(invoiceModel: InvoiceModel) {
        callInvoiceDetailWebservice(invoiceModel.id, "Invoice View")
    }

    override fun onEditInvoice(invoiceModel: InvoiceModel) {
        client_id = invoiceModel.client_id ?: ""
        matter_id = invoiceModel.MatterId ?: ""
        client_type = invoiceModel.client_type ?: "consumer"
        callInvoiceDetailWebservice(invoiceModel.id, "Invoice Detail")
    }

    override fun onChangeStatus(invoiceModel: InvoiceModel) {
        try {
            val currentStatus = invoiceModel.status?.lowercase(Locale.ROOT) ?: ""
            val newStatus = when (currentStatus) {
                "raised" -> "collected"
                "collected" -> "raised"
                "cancelled" -> "raised"
                else -> "raised"
            }

            val fromStatus = capitalize(invoiceModel.status)
            val toStatus = capitalize(newStatus)

            showSingleStatusConfirmDialog(
                invoiceModel, newStatus,
                "Change Invoice Status",
                "Are you sure you want to change the invoice status from\n$fromStatus to $toStatus?"
            )
        } catch (e: Exception) {
            AndroidUtils.showAlert(e.message, requireActivity())
        }
    }

    private fun showSingleStatusConfirmDialog(
        model: InvoiceModel,
        newStatus: String,
        title: String,
        confirmMsg: String
    ) {
        try {
            val dialogBuilder = AlertDialog.Builder(requireActivity())
            val inflater = requireActivity().layoutInflater
            val view = inflater.inflate(R.layout.delete_relationship, null)

            val header_name = view.findViewById<TextView>(R.id.header_name)
            header_name.text = title
            header_name.setTextColor(Color.BLACK)

            val close_documents = view.findViewById<ImageView>(R.id.close_documents)
            val tv_confirmation = view.findViewById<TextView>(R.id.tv_confirmation)
            tv_confirmation.text = confirmMsg

            val bt_yes = view.findViewById<AppCompatButton>(R.id.btn_yes)
            val btn_no = view.findViewById<AppCompatButton>(R.id.btn_No)

            val dialog = dialogBuilder.create()
            dialog.setView(view)

            close_documents.setOnClickListener { dialog.dismiss() }
            btn_no.setOnClickListener { dialog.dismiss() }
            bt_yes.setOnClickListener {
                dialog.dismiss()
                callChangeStatusWebservice(model.id, newStatus)
            }
            dialog.show()
        } catch (e: Exception) {
            AndroidUtils.showAlert(e.message, requireActivity())
        }
    }

    override fun onShareInvoice(model: InvoiceModel) {
        if (!model.can_share) {
            AndroidUtils.showAlert(
                "You do not have permission to share this invoice.", requireActivity()
            )
            return
        }
        if (model.rel_id.isNullOrEmpty()) {
            AndroidUtils.showAlert(
                "No relationship ID found for this invoice.", requireActivity()
            )
            return
        }
        if (model.docid.isNullOrEmpty()) {
            AndroidUtils.showAlert(
                "No document ID found for this invoice.", requireActivity()
            )
            return
        }

        val root = LinearLayout(requireContext())
        root.orientation = LinearLayout.VERTICAL
        root.setBackgroundColor(Color.WHITE)
        root.setPadding(dp(20), dp(24), dp(20), dp(0))

        val tvShareWith = TextView(requireContext())
        tvShareWith.text = "Share With: ${model.name}"
        tvShareWith.textSize = 18f
        tvShareWith.setTextColor(Color.parseColor("#1A3A6B"))
        val typeface1 = ResourcesCompat.getFont(requireContext(), R.font.gill_sans_bold)
        tvShareWith.typeface = typeface1
        val shareWithLp = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        )
        shareWithLp.bottomMargin = dp(4)
        tvShareWith.layoutParams = shareWithLp
        root.addView(tvShareWith)

        val tvVia = TextView(requireContext())
        tvVia.text = "Via: Lauditor(Secure)"
        tvVia.textSize = 18f
        tvVia.setTextColor(Color.parseColor("#1A3A6B"))
        val typeface2 = ResourcesCompat.getFont(requireContext(), R.font.gill_sans)
        tvVia.typeface = typeface2
        val viaLp = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        )
        viaLp.bottomMargin = dp(16)
        tvVia.layoutParams = viaLp
        root.addView(tvVia)

        val etMessage = EditText(requireContext())
        etMessage.hint = "Message"
        etMessage.setHintTextColor(Color.GRAY)
        etMessage.inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_FLAG_MULTI_LINE
        etMessage.gravity = Gravity.TOP or Gravity.START
        etMessage.background = null
        etMessage.setPadding(dp(12), dp(12), dp(12), dp(12))
        etMessage.textSize = 14f

        val etCard = CardView(requireContext())
        etCard.radius = dp(6).toFloat()
        etCard.cardElevation = 0f
        etCard.setCardBackgroundColor(Color.parseColor("#F0F0F0"))
        val etCardLp = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT, dp(200)
        )
        etCardLp.bottomMargin = dp(20)
        etCard.layoutParams = etCardLp
        etCard.addView(etMessage)
        root.addView(etCard)

        val btnRow = LinearLayout(requireContext())
        btnRow.orientation = LinearLayout.HORIZONTAL
        btnRow.gravity = Gravity.END
        btnRow.setPadding(0, 0, 0, dp(16))

        val btnCancel = AppCompatButton(requireContext())
        btnCancel.text = "Cancel"
        btnCancel.setTextColor(Color.DKGRAY)
        val typeface4 = ResourcesCompat.getFont(requireContext(), R.font.gill_sans)
        btnCancel.typeface = typeface4
        btnCancel.setBackgroundColor(Color.parseColor("#E0E0E0"))
        val cancelLp = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.WRAP_CONTENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        )
        cancelLp.setMargins(0, 0, dp(12), 0)
        btnCancel.layoutParams = cancelLp

        val btnSend = AppCompatButton(requireContext())
        btnSend.text = "Send"
        btnSend.setTextColor(Color.WHITE)
        btnSend.setBackgroundColor(
            ContextCompat.getColor(requireContext(), R.color.Blue_text_color)
        )
        val typeface3 = ResourcesCompat.getFont(requireContext(), R.font.gill_sans)
        btnSend.typeface = typeface3
        btnRow.addView(btnCancel)
        btnRow.addView(btnSend)
        root.addView(btnRow)

        val dialog = AlertDialog.Builder(requireActivity()).setView(root).create()
        if (dialog.window != null) {
            dialog.window!!.setBackgroundDrawableResource(android.R.color.white)
            dialog.window!!.setLayout(
                (resources.displayMetrics.widthPixels * 0.92f).toInt(),
                android.view.WindowManager.LayoutParams.WRAP_CONTENT
            )
        }

        btnCancel.setOnClickListener { dialog.dismiss() }
        btnSend.setOnClickListener {
            dialog.dismiss()
            callShareInvoiceWebservice(model, etMessage.text.toString().trim())
        }

        dialog.show()

        if (dialog.window != null) {
            dialog.window!!.setLayout(
                (resources.displayMetrics.widthPixels * 0.92f).toInt(),
                android.view.WindowManager.LayoutParams.WRAP_CONTENT
            )
        }
    }

    override fun onDeleteInvoice(model: InvoiceModel) {
        try {
            val dialogBuilder = AlertDialog.Builder(requireActivity())
            val inflater = requireActivity().layoutInflater
            val view = inflater.inflate(R.layout.delete_relationship, null)

            val header_name = view.findViewById<TextView>(R.id.header_name)
            header_name.text = "Delete Invoice"
            header_name.setTextColor(Color.BLACK)

            val close_documents = view.findViewById<ImageView>(R.id.close_documents)
            val tv_confirmation = view.findViewById<TextView>(R.id.tv_confirmation)
            tv_confirmation.text = "Are you sure you want to delete invoice ${model.invoice_no}?"

            val bt_yes = view.findViewById<AppCompatButton>(R.id.btn_yes)
            val btn_no = view.findViewById<AppCompatButton>(R.id.btn_No)

            val dialog = dialogBuilder.create()
            dialog.setView(view)

            close_documents.setOnClickListener { dialog.dismiss() }
            btn_no.setOnClickListener { dialog.dismiss() }
            bt_yes.setOnClickListener {
                dialog.dismiss()
                callDeleteInvoiceWebservice(model.id)
            }
            dialog.show()
        } catch (e: Exception) {
            AndroidUtils.showAlert(e.message, requireActivity())
        }
    }

    private fun dismissProgress() {
        if (progressDialog != null && progressDialog!!.isShowing) {
            AndroidUtils.dismiss_dialog(progressDialog)
        }
    }

    private fun getText(et: TextInputEditText?): String {
        if (et == null || et.text == null) return ""
        return et.text.toString().trim()
    }

    private fun setText(et: TextInputEditText?, value: String?) {
        et?.setText(value ?: "")
    }

    private fun parseDouble(s: String): Double {
        return try {
            s.trim().toDouble()
        } catch (e: Exception) {
            0.0
        }
    }

    private fun makeCounter(counterView: TextView, max: Int): TextWatcher {
        return object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                counterView.text = "${s?.length ?: 0}/$max"
            }
            override fun afterTextChanged(s: Editable?) {}
        }
    }

    private fun dp(value: Int): Int {
        return (value * requireContext().resources.displayMetrics.density).toInt()
    }

    private fun capitalize(s: String?): String {
        if (s.isNullOrEmpty()) return ""
        return Character.toUpperCase(s[0]) + s.substring(1).lowercase(Locale.ROOT)
    }

    override fun onClick(view: View?) {}
}
