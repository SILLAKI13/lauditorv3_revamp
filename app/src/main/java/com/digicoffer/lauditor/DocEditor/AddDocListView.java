package com.digicoffer.lauditor.DocEditor;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.digicoffer.lauditor.R;
import com.digicoffer.lauditor.CommonFiles.GlobalFiles.AndroidUtils;
import com.digicoffer.lauditor.CommonFiles.ValidationUtils.ContentValidation;
import com.digicoffer.lauditor.CommonFiles.ValidationUtils.ListValidation;
import com.google.android.material.textfield.TextInputEditText;

import java.util.ArrayList;
import java.util.List;

public class AddDocListView {

    private final View rootView;
    private final TextView tv_content;
    private final ImageView iv_remove, iv_plus_icon;
    private final LinearLayout ll_add_more, ll_add_content;
    private String titleKey;

    public interface ActionListener {
        void onRemove(View view);
    }

    public AddDocListView(Context context, ViewGroup parentContainer, ActionListener listener) {
        rootView = LayoutInflater.from(context).inflate(R.layout.list_layout_de, parentContainer, false);

        tv_content = rootView.findViewById(R.id.tv_content);
        iv_remove = rootView.findViewById(R.id.iv_remove);
        ll_add_more = rootView.findViewById(R.id.ll_add_more);
        ll_add_content = rootView.findViewById(R.id.ll_add_content);
        iv_plus_icon = rootView.findViewById(R.id.iv_plus_icon);

        iv_remove.setOnClickListener(v -> {
            if (parentContainer != null) {
                parentContainer.removeView(rootView);
            }
            if (listener != null) {
                listener.onRemove(rootView);
            }
        });

        iv_plus_icon.setOnClickListener(v -> {
            if (!validateBeforeAdding(context)) return;
            addListItemView(context, false);
        });

        ll_add_more.setOnClickListener(v -> {
            if (!validateBeforeAdding(context)) return;
            addListItemView(context, false);
        });

        // Add the first list item by default
        addListItemView(context, true);
    }

    private void addListItemView(Context context, boolean isFirstItem) {
        View view_added_list = LayoutInflater.from(context).inflate(R.layout.list_item_view_de, null);
        TextInputEditText et_list_item = view_added_list.findViewById(R.id.et_list_item);
        ImageView iv_delete_events = view_added_list.findViewById(R.id.iv_delete_events);
        et_list_item.setHint(R.string.enter_list_items);
        et_list_item.addTextChangedListener(new ListValidation(et_list_item));
        et_list_item.requestFocus();

        et_list_item.post(() -> {
            InputMethodManager imm = (InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE);
            if (imm != null) {
                imm.showSoftInput(et_list_item, InputMethodManager.SHOW_IMPLICIT);
            }
        });

        if (isFirstItem) {
            iv_delete_events.setVisibility(View.GONE); // Hide delete for first item
        } else {
            iv_delete_events.setOnClickListener(deleteView -> ll_add_content.removeView(view_added_list));
        }

        ll_add_content.addView(view_added_list);
    }

    private boolean validateBeforeAdding(Context context) {
        if (ll_add_content.getChildCount() > 0) {
            View lastView = ll_add_content.getChildAt(ll_add_content.getChildCount() - 1);
            TextInputEditText lastEt = lastView.findViewById(R.id.et_list_item);
            if (lastEt != null && lastEt.getText() != null && lastEt.getText().toString().trim().isEmpty()) {
                AndroidUtils.showError("Please add the text in the " + tv_content.getText().toString() + ".", ((Activity) context));
                lastEt.requestFocus();
                return false;
            }
        }
        return true;
    }

    public void setContent(String titleKey, String titleText) {
        this.titleKey = titleKey;
        tv_content.setText(titleText);
        tv_content.setTag(titleText);
    }

    public void setListItems(List<String> items) {
        ll_add_content.removeAllViews(); // Clear existing items

        if (items == null || items.isEmpty()) return;

        for (int i = 0; i < items.size(); i++) {
            String item = items.get(i);
            View view_added_list = LayoutInflater.from(rootView.getContext()).inflate(R.layout.list_item_view_de, null);
            TextInputEditText et_list_item = view_added_list.findViewById(R.id.et_list_item);
            ImageView iv_delete_events = view_added_list.findViewById(R.id.iv_delete_events);

            et_list_item.setText(item);
            et_list_item.addTextChangedListener(new ContentValidation(et_list_item,false));

            if (i == 0) {
                iv_delete_events.setVisibility(View.GONE); // No delete for first item
            } else {
                iv_delete_events.setOnClickListener(v -> ll_add_content.removeView(view_added_list));
            }

            ll_add_content.addView(view_added_list);
        }
    }

    public List<String> getListItems() {
        List<String> items = new ArrayList<>();

        for (int i = 0; i < ll_add_content.getChildCount(); i++) {
            View itemView = ll_add_content.getChildAt(i);
            TextInputEditText etItem = itemView.findViewById(R.id.et_list_item);
            if (etItem != null && etItem.getText() != null) {
                String text = etItem.getText().toString().trim();
                if (!text.isEmpty()) {
                    items.add(text);
                }
            }
        }

        return items;
    }

    public View getView() {
        return rootView;
    }

    public String getTitleKey() {
        return titleKey;
    }

    public void setTitleText(String title) {
        tv_content.setText(title);
    }
}
