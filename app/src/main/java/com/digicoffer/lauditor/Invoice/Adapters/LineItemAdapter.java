package com.digicoffer.lauditor.Invoice.Adapters;

import android.text.Editable;
import android.text.InputFilter;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.digicoffer.lauditor.R;
import com.digicoffer.lauditor.Invoice.Models.LineItemModel;
import com.digicoffer.lauditor.CommonFiles.ValidationUtils.DescriptionValidation;
import com.google.android.material.textfield.TextInputEditText;

import java.util.ArrayList;
import java.util.Locale;

public class LineItemAdapter extends RecyclerView.Adapter<LineItemAdapter.ItemViewHolder> {

    // ── Currency list (kept for reference, currency spinner is in create_invoice.xml header) ──
    private static final String[] CURRENCY_LIST = {
            "AUD", "BHD", "CAD", "EUR", "INR", "JPY", "KWD", "GBP", "CHF", "USD"
    };

    // ── Fields ────────────────────────────────────────────────────────────────
    private final ArrayList<LineItemModel> lineItems;
    private final Runnable onChanged;
    private boolean editable = true;
    private String selectedCurrency = "INR";
    private OnCurrencyChangedListener currencyListener;

    // ── Callback interface ────────────────────────────────────────────────────
    public interface OnCurrencyChangedListener {
        void onCurrencyChanged(String currency);
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Constructor
    // ─────────────────────────────────────────────────────────────────────────

    public LineItemAdapter(ArrayList<LineItemModel> lineItems, Runnable onChanged) {
        this.lineItems = lineItems;
        this.onChanged = onChanged;
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Public setters
    // ─────────────────────────────────────────────────────────────────────────

    public void setEditable(boolean editable) {
        this.editable = editable;
        notifyDataSetChanged();
    }

    public void setSelectedCurrency(String currency) {
        this.selectedCurrency = currency;
        notifyDataSetChanged();
    }

    public void setOnCurrencyChangedListener(OnCurrencyChangedListener listener) {
        this.currencyListener = listener;
    }

    // ─────────────────────────────────────────────────────────────────────────
    // RecyclerView overrides
    // ─────────────────────────────────────────────────────────────────────────

    @Override
    public int getItemCount() {
        return lineItems.size();
    }

    @NonNull
    @Override
    public ItemViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_invoice_row, parent, false);
        return new ItemViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ItemViewHolder h, int position) {
        LineItemModel item = lineItems.get(position);

        // ── Sl.No ─────────────────────────────────────────────────────────────
        if (h.tv_sl_no != null)
            h.tv_sl_no.setText(String.valueOf(position + 1));

        // ── Currency symbol prefix on rate field ──────────────────────────────
        String sym = getCurrencySymbol(selectedCurrency);
        if (h.tv_currency_symbol != null)
            h.tv_currency_symbol.setText(sym);

        // ── Amount ────────────────────────────────────────────────────────────
        if (h.tv_amount != null)
            h.tv_amount.setText(String.format(Locale.getDefault(), "%.2f", item.getAmount()));

        // ── Editable state ────────────────────────────────────────────────────
        applyEditable(h.et_description, editable);
        applyEditable(h.et_rate, editable);
        applyEditable(h.et_quantity, editable);

        if (h.iv_delete != null)
            h.iv_delete.setVisibility(editable ? View.VISIBLE : View.GONE);

        // ── Remove old watchers before setting values ─────────────────────────
        h.et_description.removeTextChangedListener(h.descWatcher);
        h.et_rate.removeTextChangedListener(h.rateWatcher);
        h.et_quantity.removeTextChangedListener(h.qtyWatcher);

        // ── Set current values ────────────────────────────────────────────────
        h.et_description.setText(item.getName());
        h.et_rate.setText(item.getUnitPrice() == 0 ? ""
                : String.valueOf((int) item.getUnitPrice()));
        h.et_quantity.setText(item.getQuantity() == 0 ? ""
                : String.valueOf(item.getQuantity()));

        // ── Watchers ──────────────────────────────────────────────────────────

        h.descWatcher = new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int st, int c, int a) {
            }

            @Override
            public void onTextChanged(CharSequence s, int st, int b, int c) {
                int pos = h.getAdapterPosition();
                if (pos != RecyclerView.NO_ID && pos >= 0 && pos < lineItems.size()) {
                    lineItems.get(pos).setName(s.toString());
                }
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        };

        h.rateWatcher = new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int st, int c, int a) {
            }

            @Override
            public void onTextChanged(CharSequence s, int st, int b, int c) {
                int pos = h.getAdapterPosition();
                if (pos != RecyclerView.NO_ID && pos >= 0 && pos < lineItems.size()) {
                    double rate = 0;
                    try {
                        rate = Double.parseDouble(s.toString());
                    } catch (Exception ignored) {
                    }
                    lineItems.get(pos).setUnitPrice(rate);
                    refreshAmount(h, lineItems.get(pos));
                    if (onChanged != null) onChanged.run();
                }
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        };

        h.qtyWatcher = new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int st, int c, int a) {
            }

            @Override
            public void onTextChanged(CharSequence s, int st, int b, int c) {
                int pos = h.getAdapterPosition();
                if (pos != RecyclerView.NO_ID && pos >= 0 && pos < lineItems.size()) {
                    int qty = 1;
                    try {
                        qty = Integer.parseInt(s.toString());
                    } catch (Exception ignored) {
                    }
                    lineItems.get(pos).setQuantity(qty);
                    refreshAmount(h, lineItems.get(pos));
                    if (onChanged != null) onChanged.run();
                }
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        };

        h.et_description.addTextChangedListener(h.descWatcher);
        h.et_rate.addTextChangedListener(h.rateWatcher);
        h.et_quantity.addTextChangedListener(h.qtyWatcher);

        // ── Delete button ─────────────────────────────────────────────────────
        if (h.iv_delete != null) {
            h.iv_delete.setOnClickListener(v -> {
                int pos = h.getAdapterPosition();
                if (pos != RecyclerView.NO_ID && pos >= 0
                        && pos < lineItems.size() && lineItems.size() > 1) {
                    lineItems.remove(pos);
                    notifyItemRemoved(pos);
                    notifyItemRangeChanged(pos, lineItems.size());
                    if (onChanged != null) onChanged.run();
                }
            });
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Helpers
    // ─────────────────────────────────────────────────────────────────────────

    private void refreshAmount(ItemViewHolder h, LineItemModel item) {
        if (h.tv_amount != null)
            h.tv_amount.setText(
                    String.format(Locale.getDefault(), "%.2f", item.getAmount()));
    }

    private void applyEditable(TextInputEditText et, boolean editable) {
        if (et == null) return;
        et.setEnabled(editable);
        et.setFocusable(editable);
        et.setFocusableInTouchMode(editable);
        et.setClickable(editable);
    }

    private static String getCurrencySymbol(String code) {
        if (code == null) return "₹";
        switch (code) {
            case "INR":
                return "₹";
            case "USD":
                return "$";
            case "GBP":
                return "£";
            case "EUR":
                return "€";
            case "JPY":
                return "¥";
            case "AUD":
                return "A$";
            case "CAD":
                return "C$";
            case "CHF":
                return "Fr";
            case "BHD":
                return "BD";
            case "KWD":
                return "KD";
            default:
                return code + " ";
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    // ViewHolder
    // ─────────────────────────────────────────────────────────────────────────

    public static class ItemViewHolder extends RecyclerView.ViewHolder {
        TextInputEditText et_description, et_rate, et_quantity;
        TextView tv_amount, tv_sl_no, tv_currency_symbol;
        ImageView iv_delete;
        TextWatcher descWatcher, rateWatcher, qtyWatcher;

        public ItemViewHolder(@NonNull View itemView) {
            super(itemView);
            et_description = itemView.findViewById(R.id.et_description);
            et_rate = itemView.findViewById(R.id.et_rate);
            et_quantity = itemView.findViewById(R.id.et_quantity);
            tv_amount = itemView.findViewById(R.id.tv_amount);
            tv_sl_no = itemView.findViewById(R.id.tv_sl_no);
            tv_currency_symbol = itemView.findViewById(R.id.tv_currency_symbol);
            iv_delete = itemView.findViewById(R.id.iv_delete);

            InputFilter[] filters1 = new InputFilter[]{
                    new InputFilter.LengthFilter(250)
            };
            et_description.addTextChangedListener(new DescriptionValidation(et_description));

            et_description.setFilters(filters1);
        }
    }
}