package com.digicoffer.lauditor.Matter.ViewModels;

import android.app.AlertDialog;
import android.content.res.ColorStateList;
import android.graphics.PorterDuff;
import android.os.Bundle;
import android.text.Editable;
import android.text.InputFilter;
import android.text.SpannableString;
import android.text.TextWatcher;
import android.text.style.UnderlineSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatButton;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.digicoffer.lauditor.Matter.Models.HistoryModel;
import com.digicoffer.lauditor.Matter.Models.ViewMatterModel;
import com.digicoffer.lauditor.R;
import com.digicoffer.lauditor.CommonFiles.GlobalFiles.AndroidUtils;
import com.google.android.material.textfield.TextInputEditText;

import org.json.JSONArray;
import org.json.JSONObject;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;
import java.util.Objects;

public class TimeLine extends Fragment {
    ArrayList<HistoryModel> historyList = new ArrayList<>();
    ViewMatter viewMatter = new ViewMatter();
    String header_name = "";
    Matter matter;
    ViewMatterModel viewMatterModel;

    public TimeLine(ArrayList<HistoryModel> historyList1, ViewMatter viewMatter1, String header_name1, Matter matter1, ViewMatterModel viewMatterModel) {
        historyList = historyList1;
        viewMatter = viewMatter1;
        header_name = header_name1;
        matter = matter1;
        this.viewMatterModel = viewMatterModel;
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.timeline_notes, container, false);
        //..
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        ImageView close_details = view.findViewById(R.id.close_details);
        LinearLayout ll_timeline = view.findViewById(R.id.ll_timeLine);
        TextView tv_header_name = view.findViewById(R.id.header_name);
        tv_header_name.setText(header_name);
        final AlertDialog dialog = builder.create();
        ll_timeline.removeAllViews();
//        if (historyList.size() <= 1) {
//            timeline_c.setVisibility(View.GONE);
//        } else timeline_c.setVisibility(View.VISIBLE);
        for (int i = 0; i < historyList.size(); i++) {
            try {
                View view_timeLine = LayoutInflater.from(getContext()).inflate(R.layout.matter_timeline, null);
                TextView tv_timeline_title = view_timeLine.findViewById(R.id.tv_timeline_title);
                tv_timeline_title.setText(R.string.matter_timeline);
                LinearLayout notes_layout = view_timeLine.findViewById(R.id.notes_layout);
                notes_layout.setVisibility(View.GONE);

                TextView tv_timeline_date = view_timeLine.findViewById(R.id.tv_timeline_date);
                tv_timeline_date.setText(R.string.date);
                RadioButton rb_corporate_notes = view_timeLine.findViewById(R.id.rb_corporate_notes);
                if (viewMatterModel.getCorporate().length() > 0) {
//                    if (i > 0)
//                        notes_layout.setVisibility(View.VISIBLE);
//                    else notes_layout.setVisibility(View.GONE);
                    if (historyList.get(i).getFrom_ts() != null
                            && historyList.get(i).getTo_ts() != null
                            && historyList.get(i).getFrom_ts().equals(historyList.get(i).getTo_ts())) {
                        notes_layout.setVisibility(View.GONE);
                    } else {
                        notes_layout.setVisibility(View.VISIBLE);
                    }
                } else {
                    notes_layout.setVisibility(View.GONE);
                }
                rb_corporate_notes.setText(R.string.corporate_notes);
                RadioButton rb_lauditor_notes = view_timeLine.findViewById(R.id.rb_lauditor_notes);

                LinearLayout ll_empty_notes = view_timeLine.findViewById(R.id.ll_empty_notes);
                LinearLayout ll_edit_notes = view_timeLine.findViewById(R.id.ll_edit_notes);
                TextInputEditText tv_edit_notes = view_timeLine.findViewById(R.id.tv_edit_notes);
                AppCompatButton btn_cancel_save = view_timeLine.findViewById(R.id.btn_cancel_save);
                AppCompatButton btn_create = view_timeLine.findViewById(R.id.btn_create);
                TextInputEditText tv_view_notes = view_timeLine.findViewById(R.id.tv_view_notes);
//                TextInputLayout tl_view_notes = view_timeLine.findViewById(R.id.tl_view_notes);
                LinearLayout linear_notes = view_timeLine.findViewById(R.id.linear_notes);
                ImageView iv_view_timeLine = view_timeLine.findViewById(R.id.iv_view);
                ImageView simple_icon = view_timeLine.findViewById(R.id.simple_icon);
                TextView normal_notes = view_timeLine.findViewById(R.id.normal_notes);
//                int maxLength = 3;
//                tv_edit_notes.setFilters(new InputFilter[] {new InputFilter.LengthFilter(maxLength)});
//                normal_notes.setPaintFlags(normal_notes.getPaintFlags() | Paint.UNDERLINE_TEXT_FLAG);
                InputFilter[] filters = new InputFilter[]{
                        new InputFilter.LengthFilter(150)
                };
                tv_edit_notes.setFilters(filters);
                tv_edit_notes.setHint(R.string.notes);
                ImageView iv_edit_notes = view_timeLine.findViewById(R.id.iv_notes);
                LinearLayout ll_icons = view_timeLine.findViewById(R.id.ll_icons);
                LinearLayout ll_add_notes = view_timeLine.findViewById(R.id.ll_add_notes);
                TextView tv_add_notes = view_timeLine.findViewById(R.id.tv_add_notes);
                TextInputEditText et_add_notes = view_timeLine.findViewById(R.id.et_add_notes);
                Button btn_add_cancel = view_timeLine.findViewById(R.id.btn_add_cancel);
                Button btn_add_create = view_timeLine.findViewById(R.id.btn_add_create);
                et_add_notes.setFilters(filters);
                et_add_notes.setHint(R.string.notes);
                boolean allday = historyList.get(i).isAllday();
                if (allday) {
                    ll_icons.setVisibility(View.GONE);
                } else {
                    ll_icons.setVisibility(View.VISIBLE);
                }
                LinearLayout ll_notes_items = view_timeLine.findViewById(R.id.ll_notes_items);
                JSONArray notes_List = historyList.get(i).getNotes_list(); // or .getNotes_listArray() if it's pre-parsed
                if (notes_List != null && notes_List.length() > 0) {

                    if (notes_List != null && notes_List.length() > 0) {
                        for (int j = 0; j < notes_List.length(); j++) {
                            JSONObject noteObj = notes_List.getJSONObject(j);
                            String noteText = noteObj.optString("notes", "");
                            String addedBy = noteObj.optString("added_by", "");
                            String addOn = noteObj.optString("add_on", "");
                            String firm = noteObj.optString("firm_name", "");

                            View noteItem = LayoutInflater.from(getContext()).inflate(R.layout.timeline_notes_layout, ll_notes_items, false);

                            TextView person_icon = noteItem.findViewById(R.id.person_icon);
                            String person_name = addedBy.substring(0, 1);
                            person_icon.setText(person_name);
                            TextView tvAddedBy = noteItem.findViewById(R.id.tv_note_added_by);
                            TextView tvAddedOn = noteItem.findViewById(R.id.tv_note_added_on);
                            TextView tvNoteFirm = noteItem.findViewById(R.id.tv_note_firm);

                            tvAddedBy.setText(addedBy);
                            tvAddedOn.setText(addOn);
                            tvNoteFirm.setText(noteText);

                            ll_notes_items.addView(noteItem);
                        }
                    }

                }
                //Checking the notes value and adding Dots to it.
//                if ((historyList.get(i).getNotes() != null) && (!historyList.get(i).getNotes().isEmpty()) && (!historyList.get(i).getNotes().equals("null"))) {
////                    String notes_text = historyList.get(i).getNotes() + "...";
//                    String notes_text = historyList.get(i).getNotes();
//                    normal_notes.setText(notes_text + "....");
//                    simple_icon.setVisibility(View.GONE);
//                    linear_notes.setVisibility(View.VISIBLE);
//                    ll_icons.setVisibility(View.VISIBLE);
//                } else {
//                    ll_icons.setVisibility(View.GONE);
//                    linear_notes.setVisibility(View.GONE);
//                    if (historyList.get(i).getEvent_type().equals("closedate")) {
//                        simple_icon.setVisibility(View.GONE);
//                        notes_layout.setVisibility(View.GONE);
//                    } else {
//                        simple_icon.setVisibility(View.VISIBLE);
////                        notes_layout.setVisibility(View.VISIBLE);
//                    }
//                    normal_notes.setText("....");
//                }
//                if (i == 0) {
////                    notes_layout.setVisibility(View.GONE);
//                    linear_notes.setVisibility(View.GONE);
//                    simple_icon.setVisibility(View.GONE);
//                }
                if ((historyList.get(i).getNotes() != null)
                        && (!historyList.get(i).getNotes().isEmpty())
                        && (!historyList.get(i).getNotes().equals("null"))) {

                    String notes_text = historyList.get(i).getNotes();
                    normal_notes.setText(notes_text + "....");

                    simple_icon.setVisibility(View.GONE);
                    linear_notes.setVisibility(View.VISIBLE);
                    ll_icons.setVisibility(View.VISIBLE);

                } else {
                    ll_icons.setVisibility(View.GONE);
                    linear_notes.setVisibility(View.GONE);

                    // ✅ Show simple_icon only if from_ts and to_ts are different
                    if (historyList.get(i).getFrom_ts() != null
                            && historyList.get(i).getTo_ts() != null
                            && historyList.get(i).getFrom_ts().equals(historyList.get(i).getTo_ts())) {
                        simple_icon.setVisibility(View.GONE);
                    } else {
                        simple_icon.setVisibility(View.VISIBLE);
                    }

                    normal_notes.setText("....");
                }


                ll_empty_notes.setVisibility(View.VISIBLE);
                normal_notes.setVisibility(View.VISIBLE);

                simple_icon.setTag(i);
                simple_icon.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
//                        ll_icons.setVisibility(View.GONE);
                        try {
                            int position = 0;
                            if (v.getTag() instanceof Integer) {
                                position = (Integer) v.getTag();
                                v = ll_timeline.getChildAt(position);
                                if (ll_add_notes.getVisibility() == View.VISIBLE) {
                                    ll_add_notes.setVisibility(View.GONE);
                                } else {
                                    ll_add_notes.setVisibility(View.VISIBLE);
                                }
//                                if (linear_notes.getVisibility() == View.VISIBLE) {
////                                    linear_notes.setVisibility(View.GONE);
//                                    ll_empty_notes.setVisibility(View.VISIBLE);
//                                    ll_edit_notes.setVisibility(View.GONE);
//                                    tv_view_notes.setVisibility(View.GONE);
//                                } else {
////                                    linear_notes.setVisibility(View.VISIBLE);
//                                    ll_empty_notes.setVisibility(View.GONE);
//                                    ll_edit_notes.setVisibility(View.VISIBLE);
//                                    tv_view_notes.setVisibility(View.GONE);
//                                }

                                HistoryModel historyModel = historyList.get(position);
                                if ((historyModel.getNotes() != null) && (!historyModel.getNotes().isEmpty()) && (!historyModel.getNotes().equals("null"))) {
//                    String notes_text = historyList.get(i).getNotes() + "...";
                                    String notes_text = historyModel.getNotes();
                                    et_add_notes.setText(notes_text);
                                } else {
                                    et_add_notes.setText("");
                                }
                                btn_add_create.setEnabled(false);
                                btn_add_create.setAlpha(0.5f);
                                btn_add_cancel.setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View v) {
                                        ll_edit_notes.setVisibility(View.GONE);
                                        tv_view_notes.setVisibility(View.GONE);
                                        ll_add_notes.setVisibility(View.GONE);
                                        ClearSelectedView(iv_edit_notes, iv_view_timeLine);
                                    }
                                });
                                et_add_notes.addTextChangedListener(new TextWatcher() {
                                    @Override
                                    public void beforeTextChanged(CharSequence s, int start, int count, int after) {

                                    }

                                    @Override
                                    public void onTextChanged(CharSequence s, int start, int before, int count) {

                                    }

                                    @Override
                                    public void afterTextChanged(Editable s) {
                                        if (!s.toString().isEmpty()) {
                                            btn_add_create.setEnabled(true);
                                            btn_add_create.setAlpha(1.0f);
                                        } else {
                                            btn_add_create.setEnabled(false);
                                            btn_add_create.setAlpha(0.5f);
                                        }
                                    }
                                });
                                btn_add_create.setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View v) {
                                        if (Objects.requireNonNull(et_add_notes.getText()).toString().isEmpty()) {
                                            AndroidUtils.showAlert("Please enter the Notes", getActivity());
                                        } else {
                                            ll_add_notes.setVisibility(View.GONE);
                                            dialog.dismiss();
                                            viewMatter.callEditNotesWebservice(historyModel.getId(), Objects.requireNonNull(et_add_notes.getText()).toString().trim());
                                        }
                                    }
                                });

                            }
                        } catch (Exception e) {
                            AndroidUtils.showAlert(e.getMessage(), getActivity());
                        }
                    }
                });
                //Adding the underline to notes text...
                SpannableString spannableString = new SpannableString(normal_notes.getText().toString());
                spannableString.setSpan(new UnderlineSpan(), 0, normal_notes.length(), 0);
                normal_notes.setText(spannableString);

                iv_edit_notes.setTag(i);
                iv_view_timeLine.setTag(i);
                rb_lauditor_notes.setChecked(true);
                rb_lauditor_notes.setButtonTintList(ColorStateList.valueOf(getResources().getColor(R.color.light_blue)));
                rb_lauditor_notes.setTextColor(getResources().getColor(R.color.light_blue));

                rb_corporate_notes.setChecked(false);
                rb_corporate_notes.setButtonTintList(ColorStateList.valueOf(getResources().getColor(R.color.black)));
                rb_corporate_notes.setTextColor(getResources().getColor(R.color.black));
//                rb_corporate_notes.setOnClickListener(new View.OnClickListener() {
//                    @Override
//                    public void onClick(View v) {
//                        rb_lauditor_notes.setChecked(false);
//                        rb_lauditor_notes.setTextColor(getResources().getColor(R.color.black));
//                        rb_lauditor_notes.setButtonTintList(ColorStateList.valueOf(getResources().getColor(R.color.black)));
//
//                        rb_corporate_notes.setButtonTintList(ColorStateList.valueOf(getResources().getColor(R.color.light_blue)));
//                        rb_corporate_notes.setTextColor(getResources().getColor(R.color.light_blue));
////                        linear_notes.setVisibility(View.GONE);
//                        simple_icon.setVisibility(View.GONE);
//                        ll_empty_notes.setVisibility(View.GONE);
//                        ll_add_notes.setVisibility(View.GONE);
//                    }
//                });
                rb_corporate_notes.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        rb_lauditor_notes.setChecked(false);
                        rb_lauditor_notes.setTextColor(getResources().getColor(R.color.black));
                        rb_lauditor_notes.setButtonTintList(ColorStateList.valueOf(getResources().getColor(R.color.black)));

                        rb_corporate_notes.setButtonTintList(ColorStateList.valueOf(getResources().getColor(R.color.light_blue)));
                        rb_corporate_notes.setTextColor(getResources().getColor(R.color.light_blue));

                        ll_notes_items.setVisibility(View.VISIBLE); // Show corporate notes list
                        linear_notes.setVisibility(View.GONE);
                        simple_icon.setVisibility(View.GONE);
                        ll_empty_notes.setVisibility(View.GONE);
                        ll_add_notes.setVisibility(View.GONE);
                    }
                });


                rb_lauditor_notes.setTag(i);
                rb_corporate_notes.setTag(i);
                rb_lauditor_notes.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        rb_corporate_notes.setChecked(false);
                        rb_corporate_notes.setTextColor(getResources().getColor(R.color.black));
                        rb_corporate_notes.setButtonTintList(ColorStateList.valueOf(getResources().getColor(R.color.black)));

                        rb_lauditor_notes.setButtonTintList(ColorStateList.valueOf(getResources().getColor(R.color.light_blue)));
                        rb_lauditor_notes.setTextColor(getResources().getColor(R.color.light_blue));
                        linear_notes.setVisibility(View.VISIBLE);
                        ll_notes_items.setVisibility(View.GONE);
//                        ll_add_notes.setVisibility(View.VISIBLE);
                        int position = 0;
                        if (v.getTag() instanceof Integer) {
                            position = (Integer) v.getTag();
//                            if ((historyList.get(position).getNotes() != null) || (!historyList.get(position).getNotes().isEmpty()) || (!historyList.get(position).getNotes().equals("null"))) {
//                                simple_icon.setVisibility(View.GONE);
//                                ll_empty_notes.setVisibility(View.GONE);
//                            } else {
//                                ll_empty_notes.setVisibility(View.VISIBLE);
//                                simple_icon.setVisibility(View.VISIBLE);
//                            }
                            if ((historyList.get(position).getNotes() != null)
                                    && (!historyList.get(position).getNotes().isEmpty())
                                    && (!historyList.get(position).getNotes().equals("null"))) {

                                String notes_text = historyList.get(position).getNotes();
                                normal_notes.setText(notes_text + "....");

                                simple_icon.setVisibility(View.GONE);
                                linear_notes.setVisibility(View.VISIBLE);
                                ll_icons.setVisibility(View.VISIBLE);
                                ll_empty_notes.setVisibility(View.VISIBLE);
                                normal_notes.setVisibility(View.VISIBLE);

                            } else {
                                ll_icons.setVisibility(View.GONE);
                                linear_notes.setVisibility(View.GONE);

                                // ✅ Show simple_icon only if from_ts and to_ts are different
                                if (historyList.get(position).getFrom_ts() != null
                                        && historyList.get(position).getTo_ts() != null
                                        && historyList.get(position).getFrom_ts().equals(historyList.get(position).getTo_ts())) {
                                    simple_icon.setVisibility(View.GONE);
                                } else {
                                    simple_icon.setVisibility(View.VISIBLE);
                                }

                                normal_notes.setText("....");
                            }
                        }
                    }
                });
                ClearSelectedView(iv_edit_notes, iv_view_timeLine);
                iv_edit_notes.setColorFilter(ContextCompat.getColor(getContext(), android.R.color.black), PorterDuff.Mode.SRC_IN);
                iv_view_timeLine.setColorFilter(ContextCompat.getColor(getContext(), android.R.color.black), PorterDuff.Mode.SRC_IN);
                iv_edit_notes.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        try {
                            int position = 0;
                            if (v.getTag() instanceof Integer) {
                                position = (Integer) v.getTag();
                                v = ll_timeline.getChildAt(position);
                                if (ll_edit_notes.getVisibility() == View.VISIBLE) {
                                    ClearSelectedView(iv_edit_notes, iv_view_timeLine);
                                    ll_empty_notes.setVisibility(View.VISIBLE);
                                    ll_edit_notes.setVisibility(View.GONE);
                                    tv_view_notes.setVisibility(View.GONE);
                                } else {
                                    EditClicked(iv_edit_notes, iv_view_timeLine);
                                    ll_empty_notes.setVisibility(View.GONE);
                                    ll_edit_notes.setVisibility(View.VISIBLE);
                                    tv_view_notes.setVisibility(View.GONE);
                                }

                                HistoryModel historyModel = historyList.get(position);
                                if ((historyModel.getNotes() != null) && (!historyModel.getNotes().isEmpty()) && (!historyModel.getNotes().equals("null"))) {
//                    String notes_text = historyList.get(i).getNotes() + "...";
                                    String notes_text = historyModel.getNotes();
                                    tv_edit_notes.setText(notes_text);
                                } else {
                                    tv_edit_notes.setText("");
                                }
                                btn_create.setEnabled(false);
                                btn_create.setAlpha(0.5f);
                                tv_edit_notes.addTextChangedListener(new TextWatcher() {
                                    @Override
                                    public void beforeTextChanged(CharSequence s, int start, int count, int after) {

                                    }

                                    @Override
                                    public void onTextChanged(CharSequence s, int start, int before, int count) {

                                    }

                                    @Override
                                    public void afterTextChanged(Editable s) {
                                        if (!s.toString().isEmpty()) {
                                            btn_create.setEnabled(true);
                                            btn_create.setAlpha(1.0f);
                                        } else {
                                            btn_add_create.setEnabled(false);
                                            btn_add_create.setAlpha(0.5f);
                                        }
                                    }
                                });
                                btn_cancel_save.setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View v) {
                                        ll_empty_notes.setVisibility(View.VISIBLE);
                                        ll_edit_notes.setVisibility(View.GONE);
                                        tv_view_notes.setVisibility(View.GONE);
                                        ll_icons.setVisibility(View.VISIBLE);
                                        ClearSelectedView(iv_edit_notes, iv_view_timeLine);
                                    }
                                });
                                btn_create.setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View v) {
                                        if (Objects.requireNonNull(tv_edit_notes.getText()).toString().isEmpty()) {
                                            AndroidUtils.showAlert("Please enter the Notes", getActivity());
                                            tv_edit_notes.requestFocus();
                                        } else {
                                            dialog.dismiss();
                                            viewMatter.callEditNotesWebservice(historyModel.getId(), tv_edit_notes.getText().toString().trim());
                                        }
                                    }
                                });

                            }
                        } catch (Exception e) {
                            AndroidUtils.showAlert(e.getMessage(), getActivity());
                        }
                    }
                });
                iv_view_timeLine.setTag(i);
                iv_view_timeLine.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        try {
                            int position = 0;
                            if (v.getTag() instanceof Integer) {
                                position = (Integer) v.getTag();

                                // Toggle: if already visible, hide it; else show it
                                if (tv_view_notes.getVisibility() == View.VISIBLE) {
                                    ClearSelectedView(iv_edit_notes, iv_view_timeLine);
                                    ll_empty_notes.setVisibility(View.VISIBLE);
                                    ll_edit_notes.setVisibility(View.GONE);
                                    tv_view_notes.setVisibility(View.GONE);
                                } else {
                                    ViewClicked(iv_edit_notes, iv_view_timeLine);
                                    ll_empty_notes.setVisibility(View.GONE);
                                    ll_edit_notes.setVisibility(View.GONE);
                                    tv_view_notes.setVisibility(View.VISIBLE);
                                }

                                HistoryModel historyModel = historyList.get(position);
                                tv_view_notes.setText(historyModel.getNotes());
                            }
                        } catch (Exception e) {
                            AndroidUtils.showAlert(e.getMessage(), getActivity());
                        }
                    }
                });
                tv_timeline_title.setText(historyList.get(i).getTitle());
                String inputDateStr = historyList.get(i).getFrom_ts(); // "2025-06-24T11:45:00"

                try {
                    SimpleDateFormat inputFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.ENGLISH);
                    SimpleDateFormat outputFormat = new SimpleDateFormat("MMMM dd, yyyy | hh:mm a", Locale.ENGLISH);

                    Date date = inputFormat.parse(inputDateStr);
                    String formattedDate = outputFormat.format(date);

                    tv_timeline_date.setText(formattedDate);
                } catch (ParseException e) {
                    e.printStackTrace();
//                    tv_timeline_date.setText("Invalid date");
                }

                ll_timeline.addView(view_timeLine);
            } catch (Exception ex) {
                AndroidUtils.showAlert(ex.getMessage(), getActivity());
            }
        }
        close_details.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                matter.loadViewUI();
//                   matter.loadViewUI();
            }
        });
        //..
        return view;
    }

    private void ClearSelectedView(ImageView iv_edit_notes, ImageView iv_view_timeLine) {
        iv_edit_notes.setColorFilter(ContextCompat.getColor(getContext(), android.R.color.black), PorterDuff.Mode.SRC_IN);
        iv_view_timeLine.setColorFilter(ContextCompat.getColor(getContext(), android.R.color.black), PorterDuff.Mode.SRC_IN);
    }

    private void EditClicked(ImageView iv_edit_notes, ImageView iv_view_timeLine) {
        iv_edit_notes.setColorFilter(ContextCompat.getColor(getContext(), android.R.color.holo_green_dark), PorterDuff.Mode.SRC_IN);
        iv_view_timeLine.setColorFilter(ContextCompat.getColor(getContext(), android.R.color.black), PorterDuff.Mode.SRC_IN);
    }

    private void ViewClicked(ImageView iv_edit_notes, ImageView iv_view_timeLine) {
        iv_edit_notes.setColorFilter(ContextCompat.getColor(getContext(), android.R.color.black), PorterDuff.Mode.SRC_IN);
        iv_view_timeLine.setColorFilter(ContextCompat.getColor(getContext(), android.R.color.holo_green_dark), PorterDuff.Mode.SRC_IN);
    }
}