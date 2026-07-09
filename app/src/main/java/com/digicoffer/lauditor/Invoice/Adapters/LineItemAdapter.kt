package com.digicoffer.lauditor.Invoice.Adapters

import android.text.Editable
import android.text.InputFilter
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.digicoffer.lauditor.R
import com.digicoffer.lauditor.Invoice.Models.LineItemModel
import com.digicoffer.lauditor.CommonFiles.ValidationUtils.DescriptionValidation
import com.google.android.material.textfield.TextInputEditText
import java.util.ArrayList
import java.util.Locale

class LineItemAdapter(
    val lineItems: ArrayList<LineItemModel>,
    private val onChanged: Runnable?
) : RecyclerView.Adapter<LineItemAdapter.ItemViewHolder>() {

    private var editable = true
    private var selectedCurrency = "INR"
    private var currencyListener: OnCurrencyChangedListener? = null

    interface OnCurrencyChangedListener {
        fun onCurrencyChanged(currency: String)
    }

    fun setEditable(editable: Boolean) {
        this.editable = editable
        notifyDataSetChanged()
    }

    fun setSelectedCurrency(currency: String) {
        this.selectedCurrency = currency
        notifyDataSetChanged()
    }

    fun setOnCurrencyChangedListener(listener: OnCurrencyChangedListener?) {
        this.currencyListener = listener
    }

    override fun getItemCount(): Int {
        return lineItems.size
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ItemViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_invoice_row, parent, false)
        return ItemViewHolder(view)
    }

    override fun onBindViewHolder(h: ItemViewHolder, position: Int) {
        val item = lineItems[position]

        if (h.tv_sl_no != null) {
            h.tv_sl_no.text = (position + 1).toString()
        }

        val sym = getCurrencySymbol(selectedCurrency)
        if (h.tv_currency_symbol != null) {
            h.tv_currency_symbol.text = sym
        }

        if (h.tv_amount != null) {
            h.tv_amount.text = String.format(Locale.getDefault(), "%.2f", item.getAmount())
        }

        applyEditable(h.et_description, editable)
        applyEditable(h.et_rate, editable)
        applyEditable(h.et_quantity, editable)

        if (h.iv_delete != null) {
            h.iv_delete.visibility = if (editable) View.VISIBLE else View.GONE
        }

        h.et_description.removeTextChangedListener(h.descWatcher)
        h.et_rate.removeTextChangedListener(h.rateWatcher)
        h.et_quantity.removeTextChangedListener(h.qtyWatcher)

        h.et_description.setText(item.name)
        h.et_rate.setText(
            if (item.unitPrice == 0.0) "" else item.unitPrice.toInt().toString()
        )
        h.et_quantity.setText(
            if (item.quantity == 0) "" else item.quantity.toString()
        )

        h.descWatcher = object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, st: Int, c: Int, a: Int) {}
            override fun onTextChanged(s: CharSequence?, st: Int, b: Int, c: Int) {
                val pos = h.bindingAdapterPosition
                if (pos != RecyclerView.NO_POSITION && pos >= 0 && pos < lineItems.size) {
                    lineItems[pos].name = s?.toString() ?: ""
                }
            }
            override fun afterTextChanged(s: Editable?) {}
        }

        h.rateWatcher = object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, st: Int, c: Int, a: Int) {}
            override fun onTextChanged(s: CharSequence?, st: Int, b: Int, c: Int) {
                val pos = h.bindingAdapterPosition
                if (pos != RecyclerView.NO_POSITION && pos >= 0 && pos < lineItems.size) {
                    var rate = 0.0
                    try {
                        rate = s.toString().toDouble()
                    } catch (ignored: Exception) {
                    }
                    lineItems[pos].unitPrice = rate
                    refreshAmount(h, lineItems[pos])
                    onChanged?.run()
                }
            }
            override fun afterTextChanged(s: Editable?) {}
        }

        h.qtyWatcher = object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, st: Int, c: Int, a: Int) {}
            override fun onTextChanged(s: CharSequence?, st: Int, b: Int, c: Int) {
                val pos = h.bindingAdapterPosition
                if (pos != RecyclerView.NO_POSITION && pos >= 0 && pos < lineItems.size) {
                    var qty = 1
                    try {
                        qty = s.toString().toInt()
                    } catch (ignored: Exception) {
                    }
                    lineItems[pos].quantity = qty
                    refreshAmount(h, lineItems[pos])
                    onChanged?.run()
                }
            }
            override fun afterTextChanged(s: Editable?) {}
        }

        h.et_description.addTextChangedListener(h.descWatcher)
        h.et_rate.addTextChangedListener(h.rateWatcher)
        h.et_quantity.addTextChangedListener(h.qtyWatcher)

        h.iv_delete?.setOnClickListener {
            val pos = h.bindingAdapterPosition
            if (pos != RecyclerView.NO_POSITION && pos >= 0 && pos < lineItems.size && lineItems.size > 1) {
                lineItems.removeAt(pos)
                notifyItemRemoved(pos)
                notifyItemRangeChanged(pos, lineItems.size)
                onChanged?.run()
            }
        }
    }

    private fun refreshAmount(h: ItemViewHolder, item: LineItemModel) {
        if (h.tv_amount != null) {
            h.tv_amount.text = String.format(Locale.getDefault(), "%.2f", item.getAmount())
        }
    }

    private fun applyEditable(et: TextInputEditText?, editable: Boolean) {
        if (et == null) return
        et.isEnabled = editable
        et.isFocusable = editable
        et.isFocusableInTouchMode = editable
        et.isClickable = editable
    }

    private fun getCurrencySymbol(code: String?): String {
        if (code == null) return "₹"
        return when (code) {
            "INR" -> "₹"
            "USD" -> "$"
            "GBP" -> "£"
            "EUR" -> "€"
            "JPY" -> "¥"
            "AUD" -> "A$"
            "CAD" -> "C$"
            "CHF" -> "Fr"
            "BHD" -> "BD"
            "KWD" -> "KD"
            else -> "$code "
        }
    }

    class ItemViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val et_description: TextInputEditText = itemView.findViewById(R.id.et_description)
        val et_rate: TextInputEditText = itemView.findViewById(R.id.et_rate)
        val et_quantity: TextInputEditText = itemView.findViewById(R.id.et_quantity)
        val tv_amount: TextView? = itemView.findViewById(R.id.tv_amount)
        val tv_sl_no: TextView? = itemView.findViewById(R.id.tv_sl_no)
        val tv_currency_symbol: TextView? = itemView.findViewById(R.id.tv_currency_symbol)
        val iv_delete: ImageView? = itemView.findViewById(R.id.iv_delete)

        var descWatcher: TextWatcher? = null
        var rateWatcher: TextWatcher? = null
        var qtyWatcher: TextWatcher? = null

        init {
            val filters1 = arrayOf<InputFilter>(InputFilter.LengthFilter(250))
            et_description.addTextChangedListener(DescriptionValidation(et_description))
            et_description.filters = filters1
        }
    }

    companion object {
        private val CURRENCY_LIST = arrayOf("AUD", "BHD", "CAD", "EUR", "INR", "JPY", "KWD", "GBP", "CHF", "USD")
    }
}
