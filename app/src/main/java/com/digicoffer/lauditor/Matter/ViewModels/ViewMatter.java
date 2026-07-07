package com.digicoffer.lauditor.Matter.ViewModels;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.Editable;
import android.text.SpannableString;
import android.text.SpannableStringBuilder;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.text.style.AbsoluteSizeSpan;
import android.text.style.ForegroundColorSpan;
import android.text.style.StyleSpan;
import android.util.Log;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatButton;
import androidx.cardview.widget.CardView;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.digicoffer.lauditor.Groups.Models.ViewGroupModel;
import com.digicoffer.lauditor.Matter.Adapters.ViewMatterAdapter;
import com.digicoffer.lauditor.Matter.Models.GroupsModel;
import com.digicoffer.lauditor.Matter.Models.HistoryModel;
import com.digicoffer.lauditor.Matter.Models.MatterModel;
import com.digicoffer.lauditor.Matter.Models.ViewMatterModel;
import com.digicoffer.lauditor.Members.Adapters.GroupsAdapter;
import com.digicoffer.lauditor.R;
import com.digicoffer.lauditor.Webservice.AsyncTaskCompleteListener;
import com.digicoffer.lauditor.Webservice.HttpResultDo;
import com.digicoffer.lauditor.Webservice.CommonApiHelper.WebServiceHelper;
import com.digicoffer.lauditor.CommonFiles.GlobalFiles.AndroidUtils;
import com.digicoffer.lauditor.CommonFiles.GlobalFiles.Constants;
import com.digicoffer.lauditor.CommonFiles.GlobalFiles.DynamicUtils;
import com.digicoffer.lauditor.CommonFiles.ValidationUtils.Validation;
import com.google.android.material.textfield.TextInputEditText;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Iterator;
import java.util.Locale;
import java.util.Objects;
import java.util.TimeZone;
import java.util.concurrent.TimeUnit;

public class ViewMatter extends Fragment implements AsyncTaskCompleteListener, ViewMatterAdapter.InterfaceListener {
    LinearLayout linear_notes, ll_nav_buttons;
    CardView tl_search_matter;
    RecyclerView rv_matter_list;
    CardView cv_client_details;
    JSONArray acls = new JSONArray();
    Button btn_send_request;
    String groupname = "";
    String matter_id;
    Dialog dialog;
    ArrayList<ViewGroupModel> newGrouplist = new ArrayList<>();
    String groups_id = "";
    ArrayList<ViewGroupModel> OldGroupsList = new ArrayList<>();
    ViewMatterModel viewMatterModel1;
    RecyclerView rv_group_update;
    TextInputEditText et_search_matter;
    public static String FLAG = "";
    String groupid = "";
    Dialog progressDialog;
    String Prev_Cursor = "";
    String Next_Cursor = "";
    Button btn_Delete;
    AppCompatButton btn_prev, btn_next, btn_search;
    ArrayList<ViewMatterModel> matterList = new ArrayList<>();
    ArrayList<HistoryModel> historyList = new ArrayList<>();
    ArrayList<ViewGroupModel> groupsArrayList = new ArrayList<>();
    ArrayList<ViewGroupModel> originalList = new ArrayList<>();
    ArrayList<ViewGroupModel> checkedList = new ArrayList<ViewGroupModel>();
    JSONArray clients = new JSONArray();
    JSONArray documents = new JSONArray();
    JSONArray advocates = new JSONArray();
    JSONArray members = new JSONArray();
    ArrayList<ViewMatterModel> existing_documents = new ArrayList<>();
    ArrayList<ViewMatterModel> timesheets = new ArrayList<>();
    ArrayList<ViewMatterModel> existing_members = new ArrayList<>();
    GroupsAdapter groupsAdapter;
    ArrayList<ViewGroupModel> groupsList = new ArrayList<>();
    ArrayList<ViewGroupModel> oldGrouplist = new ArrayList<>();
    TextInputEditText et_search_members;
    Matter matter;
    String TimeLineId = "";
    String Header_name = "";
    String Matter_id = "";
    String Matter_Status = "";
    String Matter_Title = "";
    String Case_Number = "";
    GroupsModel groupsModel;
    private Activity v;
    LinearLayout con_id;
    ArrayList<MatterModel> matterArraylist;


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.view_matter, container, false);
        rv_matter_list = view.findViewById(R.id.rv_matter_list);
        ll_nav_buttons = view.findViewById(R.id.ll_nav_buttons);
        btn_prev = view.findViewById(R.id.btn_prev);
        btn_prev.setText(R.string.prev_);
        btn_next = view.findViewById(R.id.btn_next);
        btn_next.setText(R.string.next_);
        tl_search_matter = view.findViewById(R.id.tl_search_matter);
        btn_search = tl_search_matter.findViewById(R.id.btn_search);
        et_search_matter = tl_search_matter.findViewById(R.id.et_search_tm);
        et_search_matter.setHint("Search Matter");
        et_search_matter.setTextSize(DynamicUtils.fifteen);
        et_search_matter.addTextChangedListener(new Validation(et_search_matter));
        cv_client_details = view.findViewById(R.id.cv_client_details);
        con_id = view.findViewById(R.id.con_id);
        rv_group_update = view.findViewById(R.id.rv_group_update);
        matter = (Matter) getParentFragment();
        btn_prev.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                callFilteredMatterListWebservice("before", Prev_Cursor);
            }
        });
        btn_next.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                callFilteredMatterListWebservice("after", Next_Cursor);
            }
        });
        callFilteredMatterListWebservice("", "");

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        getActivity().getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_NOTHING);
    }

    @Override
    public void onPause() {
        super.onResume();
        getActivity().getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);
    }

    public void callMatterListWebservice() {
        try {
            progressDialog = AndroidUtils.get_progress(getActivity());
            JSONObject postdata = new JSONObject();
            WebServiceHelper.callHttpWebService(this, getContext(), WebServiceHelper.RestMethodType.GET, "v2/matter/" + Constants.MATTER_TYPE.toLowerCase(Locale.ROOT) + "?paginate=true", "Matter List", postdata.toString());
        } catch (Exception e) {
            if (progressDialog != null && progressDialog.isShowing()) {
                AndroidUtils.dismiss_dialog(progressDialog);
            }
            e.fillInStackTrace();
        }
    }

    public void callFilteredMatterListWebservice(String NavPosition, String id) {
        try {
            String url = "v2/matter/" + Constants.MATTER_TYPE.toLowerCase(Locale.ROOT) + "?paginate=true";
            if (!NavPosition.isEmpty()) {
                url = "v2/matter/" + Constants.MATTER_TYPE.toLowerCase(Locale.ROOT) + "?" + NavPosition + "=" + id + "&paginate=true";
            } else if (!Objects.requireNonNull(et_search_matter.getText()).toString().isEmpty()) {
                url = "v2/matter/" + Constants.MATTER_TYPE.toLowerCase(Locale.ROOT) + "?" + NavPosition + "=" + id + "&paginate=true" + "&search=" + Objects.requireNonNull(et_search_matter.getText()).toString();
            } else {
                url = "v2/matter/" + Constants.MATTER_TYPE.toLowerCase(Locale.ROOT) + "?paginate=true";
            }
            progressDialog = AndroidUtils.get_progress(getActivity());
            JSONObject postdata = new JSONObject();
            WebServiceHelper.callHttpWebService(this, getContext(), WebServiceHelper.RestMethodType.GET, url, "Matter List", postdata.toString());
        } catch (Exception e) {
            if (progressDialog != null && progressDialog.isShowing()) {
                AndroidUtils.dismiss_dialog(progressDialog);
            }
            e.fillInStackTrace();
        }
    }

    public void callEditMatterInfo(ViewMatterModel viewMatterModel) {
        try {
            progressDialog = AndroidUtils.get_progress(getActivity());
            JSONObject postdata = new JSONObject();
            WebServiceHelper.callHttpWebService(this, getContext(), WebServiceHelper.RestMethodType.GET, "v2/matter/" + Constants.MATTER_TYPE.toLowerCase(Locale.ROOT) + "/" + viewMatterModel.getId(), "Edit Matter", postdata.toString());
        } catch (Exception e) {
            if (progressDialog != null && progressDialog.isShowing()) {
                AndroidUtils.dismiss_dialog(progressDialog);
            }
            e.fillInStackTrace();
        }
    }

    @Override
    public void onClick(View view) {

    }

    @Override
    public void onAsyncTaskComplete(HttpResultDo httpResult) {
        if (progressDialog != null && progressDialog.isShowing())
            AndroidUtils.dismiss_dialog(progressDialog);
        try {
            if (httpResult.getResult() == WebServiceHelper.ServiceCallStatus.Success) {
                JSONObject result = new JSONObject(httpResult.getResponseContent());
                if (httpResult.getRequestType().equals("Remove Groups")) {
                    if (result.has("counts")) {
                        JSONObject jsonObject = result.optJSONObject("counts");
                        assert jsonObject != null;
                        String doc_count = jsonObject.optString("documents");
                        String rel_count = jsonObject.optString("relationships");

                        loadNewList(groupsArrayList);
                        oldGrouplist.clear();
                        for (int i = 0; i < originalList.size(); i++) {
                            if (!originalList.get(i).isChecked()) {
                                oldGrouplist.add(originalList.get(i));
                            }
                        }
                        checkedList.clear();
                        for (int i = 0; i < groupsArrayList.size(); i++) {
                            if (groupsArrayList.get(i).isChecked()) {
                                checkedList.add(groupsArrayList.get(i));
                            }
                        }
                        if (oldGrouplist.isEmpty()) {
                            AndroidUtils.showAlert("To update matter groups, the client should be linked to more than one group.\n" +
                                    "Please assign the client to more groups.", getActivity(), "Info");
                        } else {
                            if (!newGrouplist.isEmpty())
                                GroupsAssignPopup(groupname, rel_count, doc_count, newGrouplist);
                        }
                    }
                } else if (httpResult.getRequestType().equals("Alter Groups")) {
                    try {
                        String successmsg = result.getString("msg");
                        AndroidUtils.showAlert(successmsg, getActivity());

                        loadNewList(groupsArrayList);
                        for (ViewGroupModel group : groupsArrayList) {
                            for (ViewGroupModel checked : checkedList) {
                                if (group.getGroup_id().equals(checked.getGroup_id())) {
                                    group.setChecked(true);
                                    group.setCan_delete(false);
                                    break;
                                }
                            }
                        }

                        for (ViewGroupModel group : groupsArrayList) {
                            if (group.getGroup_id().equals(groups_id)) {
                                group.setChecked(false);
                                group.setCan_delete(true);
                                continue;
                            }
                            if (isGroupInNewList(group.getGroup_id())) {
                                group.setChecked(true);
                                group.setCan_delete(false);
                            }
                        }

                        groupsAdapter.notifyDataSetChanged();
                        callUpdateGroupsWebservice(groupsArrayList);

                    } catch (Exception e) {
                        AndroidUtils.showAlert(e.getMessage(), getActivity());
                    }
                } else {
                    boolean error = result.getBoolean("error");
                    if (httpResult.getRequestType().equals("Groups")) {
                        JSONArray data = result.getJSONArray("groups");
                        loadGroupsData(data);
                    } else if (httpResult.getRequestType().equals("Matter List")) {
                        if (error) {
                            String msg = result.getString("msg");
                            AndroidUtils.showAlert(msg, getActivity());
                        } else {
                            Prev_Cursor = result.optString("prev_cursor");
                            Next_Cursor = result.optString("next_cursor");
                            if (Prev_Cursor.isEmpty() || Prev_Cursor.equals("null") || Prev_Cursor == null) {
                                AndroidUtils.ToggleButton(0, btn_prev);
                            } else {
                                AndroidUtils.ToggleButton(1, btn_prev);
                            }
                            if (Next_Cursor.isEmpty() || Next_Cursor.equals("null") || Next_Cursor == null) {
                                AndroidUtils.ToggleButton(0, btn_next);
                            } else {
                                AndroidUtils.ToggleButton(1, btn_next);
                            }
                            JSONArray matters = result.optJSONArray("matters");
                            try {
                                assert matters != null;
                                loadMattersList(matters);
                            } catch (Exception e) {
                                AndroidUtils.showAlert(e.getMessage(), getActivity());
                                e.fillInStackTrace();
                            }
                        }
                    } else if (httpResult.getRequestType().equals("Edit Matter")) {
                        if (error) {
                            String msg = result.getString("msg");
                            AndroidUtils.showAlert(msg, getActivity());
                        } else {
                            JSONObject matters = result.optJSONObject("matter");
                            try {
                                assert matters != null;
                                loadeditmatter(matters);
                            } catch (Exception e) {
                                AndroidUtils.showAlert(e.getMessage(), getActivity());
                                e.fillInStackTrace();
                            }
                        }
                    } else if (httpResult.getRequestType().equals("TimeLine")) {
                        if (error) {
                            String msg = result.getString("msg");
                            AndroidUtils.showAlert(msg, getActivity());
                        } else {
                            loadHistory(result);
                        }
                    } else if (httpResult.getRequestType().equals("Notes")) {
                        historyList.clear();
                        boolean iserror = result.optBoolean("error");
                        String updatemsg = result.getString("msg");
                        if (!iserror) {
                            AndroidUtils.showAlert(updatemsg, getActivity());
                            callTimeLineWebservice();
                        } else {
                            AndroidUtils.showAlert(updatemsg, getActivity());
                        }
                    } else if (httpResult.getRequestType().equals("Update Groups")) {
                        boolean iserror = result.optBoolean("error");
                        if (!iserror) {
                            String updatemsg = result.getString("msg");
                            if (!Constants.isAlterPopup)
                                AndroidUtils.showAlert(updatemsg, getActivity());
                            Log.d("successmsg", updatemsg);
                            groupsAdapter.notifyDataSetChanged();
                            load_selected_groups(groupsArrayList);
                            Constants.isAlterPopup = false;
                            callFilteredMatterListWebservice("", "");
                        } else {
                            AndroidUtils.showAlert(result.getString("msg"), getActivity());
                        }
                    } else if (httpResult.getRequestType().equals("Update Matter")) {

                        String msg = result.getString("msg");
                        Log.d("Message", msg);
                        if (error) {
                            AndroidUtils.showAlert(msg, getActivity());
                        } else {
                            rv_matter_list.removeAllViews();
                            callFilteredMatterListWebservice("", "");
                            AndroidUtils.showAlert(msg, getActivity(),"Success");
                        }

                    } else if (httpResult.getRequestType().equals("matter_update")) {

                        String msg = result.getString("msg");
                        Log.d("Message", msg);
                        if (error) {
                            AndroidUtils.showAlert(msg, getActivity());
                        } else {
                            rv_matter_list.removeAllViews();
                            callFilteredMatterListWebservice("", "");
                            AndroidUtils.showAlert(msg, getActivity(),"Success");
                        }

                    } else if (httpResult.getRequestType().equals("Delete Matter")) {
                        String msg = result.getString("msg");
                        if (error) {
                            AndroidUtils.showAlert(msg, getActivity());
                        } else {
                            rv_matter_list.removeAllViews();
                            callFilteredMatterListWebservice("", "");
                            AndroidUtils.showAlert(msg, getActivity(),"Success");
                        }
                    }
                }
            } else if (httpResult.getResult() == WebServiceHelper.ServiceCallStatus.Failed) {
                try {
                    JSONObject result = new JSONObject(httpResult.getResponseContent());
                    if (result.optBoolean("error")) {
                        AndroidUtils.showErrorAlert(result.optString("msg"), getActivity());
                    }
                } catch (Exception e) {
                    e.fillInStackTrace();
                }
            } else {
                AndroidUtils.showErrorAlert(httpResult.getResponseContent().toString(), getActivity());
            }
        } catch (JSONException e) {
            e.fillInStackTrace();
        }
    }

    private void loadNewList(ArrayList<ViewGroupModel> originalList) {
        newGrouplist.clear();
        for (ViewGroupModel group : originalList) {
            if (!group.getGroup_id().equals(groups_id)) {
                newGrouplist.add(group);
            }
        }
    }

    private boolean isGroupInNewList(String groupId) {
        for (ViewGroupModel model : newGrouplist) {
            if (model.getGroup_id().equals(groupId) && model.isChecked()) {
                return true;
            }
        }
        return false;
    }

    private void loadGroupsData(JSONArray data) {
        try {
            Constants.groupsList_Access.clear();
            for (int i = 0; i < data.length(); i++) {
                JSONObject jsonObject = data.getJSONObject(i);
                GroupsModel groupsModel = new GroupsModel();
                groupsModel.setGroup_id(jsonObject.getString("id"));
                groupsModel.setGroup_name(jsonObject.getString("name"));
                if ((!jsonObject.optString("name").equals("AAM")) && ((!jsonObject.optString("name").equals("SuperUser")))) {
                    Constants.groupsList_Access.add(groupsModel);
                }
            }
            load_groups_view(viewMatterModel1);
        } catch (JSONException e) {
            e.fillInStackTrace();
            AndroidUtils.showAlert(e.getMessage(), getActivity());
        }
    }

    private void openViewGroupsPopup() {
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        LayoutInflater inflater = requireActivity().getLayoutInflater();
        View view = inflater.inflate(R.layout.update_groups_popup, null);
        TextView header = view.findViewById(R.id.header_name);
        header.setText(viewMatterModel1.getTitle() + " - Update Group(S)");
        LinearLayout ll_groups = view.findViewById(R.id.ll_groups);
        rv_group_update = view.findViewById(R.id.rv_group_update);
        AppCompatButton btn_cancel_save = view.findViewById(R.id.btn_cancel_save);
        AppCompatButton btn_create = view.findViewById(R.id.btn_create);
        btn_create.setText(R.string.update);
        btn_create.setEnabled(false);
        btn_create.setAlpha(0.5f);
        btn_send_request = btn_create;
        ImageView close_details = view.findViewById(R.id.close_details);
        et_search_members = view.findViewById(R.id.et_search_members);
        et_search_members.setHint(R.string.groups);
        et_search_members.addTextChangedListener(new Validation(et_search_members));
        CheckBox chk_select_all = view.findViewById(R.id.chk_select_all);
        Constants.isAlterPopup = false;
        groupsAdapter = new GroupsAdapter(groupsArrayList, this);
        rv_group_update.setLayoutManager(new LinearLayoutManager(getContext()));
        rv_group_update.setAdapter(groupsAdapter);
        et_search_members.addTextChangedListener(new TextWatcher() {
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                groupsAdapter.getFilter().filter(s);
            }

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void afterTextChanged(Editable s) {
            }

        });
        rv_group_update.refreshDrawableState();

        final AlertDialog dialog = builder.create();
        btn_cancel_save.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                groupsArrayList.clear();
                dialog.dismiss();
            }
        });
        btn_create.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
                callUpdateGroupsWebservice(groupsArrayList);
            }
        });
        close_details.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                groupsArrayList.clear();
                dialog.dismiss();
            }
        });
        dialog.setCancelable(false);
        dialog.setView(view);
        dialog.show();
    }

    public void loadNewGroups(ArrayList<ViewGroupModel> list_item) {
        JSONArray acls = new JSONArray();
        if (!list_item.isEmpty()) {
            for (int i = 0; i < list_item.size(); i++) {
                ViewGroupModel viewGroupModel = list_item.get(i);
                if (viewGroupModel.isChecked()) {
                    acls.put(viewGroupModel.getId());
                }
            }
        }
        originalList.clear();
        originalList.addAll(list_item);
        if (acls.length() == 0) {
            btn_Delete.setAlpha(0.5f);
            btn_Delete.setEnabled(false);
        } else {
            btn_Delete.setAlpha(1.0f);
            btn_Delete.setEnabled(true);
        }
    }

    public void checkRemoveGroups(String groups_id, String groupname, ArrayList<ViewGroupModel> groupsList) {
        try {
            this.groupname = groupname;
            this.groups_id = groups_id;
            OldGroupsList = groupsList;
            progressDialog = AndroidUtils.get_progress(getActivity());
            JSONObject jsonObject = new JSONObject();
            WebServiceHelper.callHttpWebService(this, getContext(), WebServiceHelper.RestMethodType.GET, "v3/matter/groups/" + Matter_id + "/" + groups_id, "Remove Groups", jsonObject.toString());
        } catch (Exception e) {
            AndroidUtils.showAlert(e.getMessage(), getActivity());
        }
    }

    @SuppressLint("SetTextI18n")
    private void GroupsAssignPopup(String group_name, String rel_count, String doc_count, ArrayList<ViewGroupModel> groupsList1) {
        try {
            AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(getContext());
            LayoutInflater inflater = getActivity().getLayoutInflater();
            View view = inflater.inflate(R.layout.assign_to_other_group, null);

            TextView tv_update_group = view.findViewById(R.id.tv_update_group);
            tv_update_group.setText(R.string.update_group);

            btn_Delete = view.findViewById(R.id.Delete);
            btn_Delete.setText(R.string.delete);
            btn_Delete.setAlpha(0.5f);
            btn_Delete.setEnabled(false);

            TextView tv_warning_msg = view.findViewById(R.id.tv_warning_msg);
            TextView tv_assign_group = view.findViewById(R.id.tv_assign_group);
            tv_assign_group.setAutoSizeTextTypeUniformWithConfiguration(1, 16, 1, TypedValue.COMPLEX_UNIT_SP);
            tv_assign_group.setText(R.string.assign_to_another_groups);
            RecyclerView rv_groups_view = view.findViewById(R.id.rv_groups_view);

            RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(getContext(), LinearLayoutManager.VERTICAL, false);
            rv_groups_view.setLayoutManager(layoutManager);
            Constants.isAlterPopup = true;
            if (!groupsList1.isEmpty()) {
                ArrayList<ViewGroupModel> groupsList = new ArrayList<>();
                for (int i = 0; i < groupsList1.size(); i++) {
                    ViewGroupModel viewGroupModel = groupsList1.get(i);
                    viewGroupModel.setChecked(false);
                    groupsList.add(viewGroupModel);
                }
            }
            GroupsAdapter groupsAdapter1 = new GroupsAdapter(groupsList1, this);
            rv_groups_view.setAdapter(groupsAdapter1);
            TextInputEditText et_search_members = view.findViewById(R.id.tv_search_groups);
            et_search_members.addTextChangedListener(new Validation(et_search_members));

            SpannableString groupName = new SpannableString("'" + group_name + "'");
            groupName.setSpan(new ForegroundColorSpan(ContextCompat.getColor(getActivity(), R.color.black)), 0, groupName.length(), 0);
            groupName.setSpan(new StyleSpan(Typeface.BOLD), 0, groupName.length(), 0);
            groupName.setSpan(new AbsoluteSizeSpan(18, true), 0, groupName.length(), 0);

            SpannableStringBuilder msgBuilder = new SpannableStringBuilder();
            msgBuilder.append("This ")
                    .append(groupName)
                    .append(" group currently contains ");

            SpannableString documentText = new SpannableString(doc_count + " Documents. ");
            documentText.setSpan(new ForegroundColorSpan(ContextCompat.getColor(getActivity(), R.color.Blue_text_color)), 0, documentText.length(), 0);
            msgBuilder.append(documentText);
            msgBuilder.append("Before updating, please assign them to another group.");
            tv_warning_msg.setText(msgBuilder, TextView.BufferType.SPANNABLE);

            AppCompatButton btn_Cancel = view.findViewById(R.id.Cancel);
            dialog = progressDialog;

            btn_Cancel.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    progressDialog.dismiss();
                    Constants.isAlterPopup = false;
                    rv_groups_view.removeAllViews();
                    unCheckList();
                }
            });
            ImageView iv_close = view.findViewById(R.id.iv_close);
            iv_close.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    progressDialog.dismiss();
                    Constants.isAlterPopup = false;
                    rv_groups_view.removeAllViews();
                    unCheckList();
                }
            });
            btn_Delete.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Constants.isAlterPopup = true;
                    progressDialog.dismiss();
                    AlterGroups();
                }
            });
            et_search_members.addTextChangedListener(new TextWatcher() {
                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {
                    groupsAdapter1.getFilter().filter(s);
                }

                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {

                }

                @Override
                public void afterTextChanged(Editable s) {
                }

            });
            rv_groups_view.refreshDrawableState();

            final AlertDialog dialog = dialogBuilder.create();
            progressDialog = dialog;
            dialog.setView(view);
            dialog.setCanceledOnTouchOutside(false);
            dialog.show();

        } catch (Exception e) {
            AndroidUtils.showAlert(e.getMessage(), getActivity());
        }
    }

    private void unCheckList() {
        try {
            for (ViewGroupModel group : groupsArrayList) {
                boolean shouldBeChecked = false;
                for (ViewGroupModel checked : checkedList) {
                    if (group.getGroup_id().equals(checked.getGroup_id())) {
                        shouldBeChecked = true;
                        break;
                    }
                }
                group.setChecked(shouldBeChecked);
            }
            groupsAdapter.notifyDataSetChanged();
        } catch (Exception e) {
            Log.e("unCheckList", "Error updating checked states: " + e.getMessage());
        }
    }

    public void AlterGroups() {
        try {
            JSONObject jsonObject = new JSONObject();
            if (!newGrouplist.isEmpty()) {
                for (int i = 0; i < newGrouplist.size(); i++) {
                    ViewGroupModel viewGroupModel = newGrouplist.get(i);
                    if (viewGroupModel.isChecked()) {
                        acls.put(viewGroupModel.getGroup_id());
                    }
                }
            }
            jsonObject.put("new_groups", acls);
            WebServiceHelper.callHttpWebService(this, getContext(), WebServiceHelper.RestMethodType.PATCH, "v3/matter/groups/" + Matter_id + "/" + groups_id, "Alter Groups", jsonObject.toString());
        } catch (Exception e) {
            AndroidUtils.showAlert(e.getMessage(), getActivity());
        }
    }

    public void load_selected_groups(ArrayList<ViewGroupModel> list_item) {
        JSONArray acls = new JSONArray();
        if (!list_item.isEmpty()) {
            for (int i = 0; i < list_item.size(); i++) {
                ViewGroupModel viewGroupModel = list_item.get(i);
                if (viewGroupModel.isChecked()) {
                    acls.put(viewGroupModel.getId());
                }
            }
        }
        if (acls.length() == 0) {
            btn_send_request.setAlpha(0.5f);
            btn_send_request.setEnabled(false);
        } else {
            btn_send_request.setAlpha(1.0f);
            btn_send_request.setEnabled(true);
        }
    }

    private void loadGroupsRecylerview() {
        FLAG = "second_click";
        rv_group_update.setLayoutManager(new GridLayoutManager(getContext(), 1));
        groupsAdapter = new GroupsAdapter(groupsList);
        rv_group_update.setAdapter(groupsAdapter);
        et_search_members.addTextChangedListener(new TextWatcher() {
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                groupsAdapter.getFilter().filter(et_search_members.getText().toString());
            }

        });
    }

    private void callUpdateGroupsWebservice(ArrayList<ViewGroupModel> groupsList) {
        progressDialog = AndroidUtils.get_progress(getActivity());
        try {
            JSONObject postdata = new JSONObject();
            JSONArray group_acls = new JSONArray();
            for (int i = 0; i < groupsList.size(); i++) {
                ViewGroupModel viewGroupModel = groupsList.get(i);
                if (viewGroupModel.isChecked()) {
                    group_acls.put(groupsList.get(i).getGroup_id());
                }
            }
            if (group_acls.length() == 0) {
                AndroidUtils.showAlert("Please select atleast one group", getActivity());
            } else {
                postdata.put("group_acls", group_acls);
                WebServiceHelper.callHttpWebService(this, getContext(), WebServiceHelper.RestMethodType.PUT, "matter/" + Constants.MATTER_TYPE.toLowerCase(Locale.ROOT) + "/" + Matter_id + "/acls", "Update Groups", postdata.toString());
            }
        } catch (Exception e) {
            e.fillInStackTrace();
        }
    }

    private void loadHistory(JSONObject result) {
        try {
            historyList.clear();
            String id = result.optString("id");
            String title = result.optString("title");
            JSONArray jsonArray = result.optJSONArray("history");
            for (int i = 0; i < jsonArray.length(); i++) {
                JSONObject jsonObject = jsonArray.optJSONObject(i);
                HistoryModel historyModel = new HistoryModel();
                if (jsonObject.has("allday")) {
                    historyModel.setAllday(jsonObject.getBoolean("allday"));
                } else {
                    historyModel.setAllday(true);
                }
                historyModel.setId(jsonObject.optString("id"));
                historyModel.setDescription(jsonObject.optString("description"));
                historyModel.setEvent_type(jsonObject.optString("event_type"));
                historyModel.setFrom_ts(jsonObject.optString("from_ts"));
                historyModel.setTo_ts(jsonObject.optString("to_ts"));
                historyModel.setTitle(jsonObject.optString("title"));
                historyModel.setNotes(jsonObject.optString("notes"));
                historyModel.setNotes_list(jsonObject.optJSONArray("notes_list"));
                historyList.add(historyModel);
            }
        } catch (JSONException e) {
            AndroidUtils.showAlert(e.getMessage(), getActivity());
            e.fillInStackTrace();
        }
        if (matter != null) {
            matter.View_Details(historyList, this, Header_name, viewMatterModel1);
            if (con_id != null) {
                con_id.setVisibility(View.GONE);
            }
        }
    }

    public void openViewDetailsPopUp() {
        try {
            matter.View_Details(viewMatterModel1, this, historyList, Header_name);
            con_id.setVisibility(View.GONE);
        } catch (Exception e) {
            AndroidUtils.showAlert(e.getMessage(), getActivity());
        }
    }

    public void openTimeline() {
        try {
            matter.View_Details(viewMatterModel1, this, historyList, Header_name);
            con_id.setVisibility(View.GONE);
        } catch (Exception e) {
            AndroidUtils.showAlert(e.getMessage(), getActivity());
        }
    }

    void callEditNotesWebservice(String id, String notes) {
        progressDialog = AndroidUtils.get_progress(getActivity());
        try {
            JSONObject postdata = new JSONObject();
            postdata.put("notes", notes);
            WebServiceHelper.callHttpWebService(this, getContext(), WebServiceHelper.RestMethodType.PUT, "event/notes/" + id, "Notes", postdata.toString());
        } catch (Exception e) {
            if (progressDialog != null || progressDialog.isShowing()) {
                AndroidUtils.dismiss_dialog(progressDialog);
            }
        }
    }

    private void loadeditmatter(JSONObject jsonObject) {
        try {
            ViewMatterModel viewMatterModel = new ViewMatterModel();
            viewMatterModel.setId(jsonObject.optString("id"));
            Constants.Matter_id = "";
            Constants.Matter_id = viewMatterModel.getId();
            if (jsonObject.has("caseNumber")) {
                viewMatterModel.setCaseNumber(jsonObject.optString("caseNumber"));
            }
            if (jsonObject.has("caseType")) {
                viewMatterModel.setCasetype(jsonObject.optString("caseType"));
            }
            JSONArray clientIds = new JSONArray();
            JSONArray clientsArray = jsonObject.optJSONArray("clients");
            if (clientsArray != null) {
                for (int i = 0; i < clientsArray.length(); i++) {
                    JSONObject clientObject = clientsArray.optJSONObject(i);
                    if (clientObject != null) {
                        String clientId = clientObject.optString("id", "");
                        clientIds.put(clientId);
                    }
                }
            }
            viewMatterModel.setClients(clientsArray);
            if (jsonObject.has("corporate")) {
                String CorpclientId = "";
                JSONArray CorpclientsArray = jsonObject.optJSONArray("corporate");
                if (CorpclientsArray != null) {
                    for (int i = 0; i < CorpclientsArray.length(); i++) {
                        JSONObject clientObject = CorpclientsArray.optJSONObject(i);
                        if (clientObject != null) {
                            CorpclientId = clientObject.optString("id", "");
                        }
                    }
                }
                viewMatterModel.setCorporate(CorpclientsArray);
                viewMatterModel.setCorpId(CorpclientId);
                clientIds.put(CorpclientId);
            }
            viewMatterModel.setCreated(jsonObject.optString("created_on"));
            viewMatterModel.setClients_list(clientIds);
            if (jsonObject.has("courtName")) {
                viewMatterModel.setCourtName(jsonObject.optString("courtName"));
            }
            if (jsonObject.has("isdisabled")) {
                viewMatterModel.setIsdisabled(jsonObject.optBoolean("isdisabled"));
            }
            if (jsonObject.has("date_of_filling")) {
                viewMatterModel.setDate_of_filling(AndroidUtils.formatToMMMddYYYY(jsonObject.optString("date_of_filling")));
            }
            if (jsonObject.has("closedate")) {
                viewMatterModel.setClosedate(AndroidUtils.formatToMMMddYYYY(jsonObject.optString("closedate")));
            }
            if (jsonObject.has("matterNumber")) {
                viewMatterModel.setMatterNumber(jsonObject.optString("matterNumber"));
            }
            if (jsonObject.has("matterType")) {
                viewMatterModel.setMatterType(jsonObject.optString("matterType"));
            }
            if (jsonObject.has("owner")) {
                JSONObject jsonObject1 = jsonObject.optJSONObject("owner");
                assert jsonObject1 != null;
                Constants.owner_id = jsonObject1.optString("id");
                Constants.owner_name = jsonObject1.optString("name");
            }
            if (jsonObject.has("startdate")) {
                viewMatterModel.setStartdate(AndroidUtils.formatToMMMddYYYY(jsonObject.optString("startdate")));
            }
            if (jsonObject.has("timesheets")) {
                viewMatterModel.setTimesheets(jsonObject.optJSONArray("timesheets"));
            }
            if (jsonObject.has("created_date")) {
                viewMatterModel.setCreated_date(AndroidUtils.formatToMMMddYYYY(jsonObject.optString("created_date")));
            }
            if (jsonObject.has("created_date")) {
                viewMatterModel.setCreated(AndroidUtils.formatToMMMddYYYY(jsonObject.optString("created_date")));
            }
            if (jsonObject.has("matter_id")) {
                viewMatterModel.setMatter_id(jsonObject.optString("matter_id"));
            }
            viewMatterModel.setDescription(jsonObject.optString("description"));
            viewMatterModel.setDocuments(jsonObject.optJSONArray("documents"));
            viewMatterModel.setGroupAcls(jsonObject.optJSONArray("groupAcls"));
            viewMatterModel.setGroups(jsonObject.optJSONArray("groups"));
            if (jsonObject.has("hearingDateDetails")) {
                viewMatterModel.setHearingDateDetails(jsonObject.optJSONObject("hearingDateDetails"));
            }
            viewMatterModel.setIs_editable(jsonObject.getBoolean("is_editable"));
            if (jsonObject.has("judges")) {
                viewMatterModel.setJudges(jsonObject.optString("judges"));
            }
            if (jsonObject.has("matterClosedDate")) {
                viewMatterModel.setMatterClosedDate(jsonObject.optString("matterClosedDate"));
            }
            viewMatterModel.setMembers(jsonObject.optJSONArray("members"));
            if (jsonObject.has("nextHearingDate")) {
                viewMatterModel.setNextHearingDate(jsonObject.optString("nextHearingDate"));
            }
            if (jsonObject.has("opponentAdvocates")) {
                viewMatterModel.setOpponentAdvocates(jsonObject.optJSONArray("opponentAdvocates"));
            }
            viewMatterModel.setOwner(jsonObject.optJSONObject("owner"));
            viewMatterModel.setPriority(jsonObject.optString("priority"));
            viewMatterModel.setStatus(jsonObject.optString("status"));
            JSONObject tagsObject = jsonObject.getJSONObject("tags");
            JSONArray tagsArray = new JSONArray();
            Iterator<String> keys = tagsObject.keys();
            while (keys.hasNext()) {
                String key = keys.next();
                try {
                    tagsArray.put(tagsObject.getString(key));
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
            viewMatterModel.setTags_list(tagsArray);
            if (jsonObject.has("tempClients")) {
                viewMatterModel.setTempClients(jsonObject.optJSONArray("tempClients"));
            }
            if (jsonObject.has("timesheets")) {
                viewMatterModel.setTimesheets(jsonObject.optJSONArray("timesheets"));
            }
            viewMatterModel.setTemporaryClients(jsonObject.optJSONArray("temporaryClients"));
            viewMatterModel.setTitle(jsonObject.optString("title"));
            viewMatterModel1 = viewMatterModel;
            if (Constants.Matter_CreateOrViewDetails.equals("View Timeline")) {
                openViewDetailsPopUp();
            } else if (Constants.Matter_CreateOrViewDetails.equals("Close/Reopen")) {
                Close_Matter(viewMatterModel);
            } else {
                callTimeLineWebservice();
            }
        } catch (JSONException e) {
            AndroidUtils.showAlert(e.getMessage(), getActivity());
            e.fillInStackTrace();
        }
    }

    // ══════════════════════════════════════════════════════════════════
    //  loadMattersList
    //  *** EMPTY STATE CHECK IS HERE ***
    //  After matterList is fully built, we check if it is empty.
    //  If empty  → tell parent Matter fragment to show the empty state.
    //  If not    → hide empty state and load the RecyclerView normally.
    // ══════════════════════════════════════════════════════════════════
    private void loadMattersList(JSONArray matters) {
        try {
            matterList.clear();
            for (int i = 0; i < matters.length(); i++) {
                JSONObject jsonObject = matters.optJSONObject(i);

                ViewMatterModel viewMatterModel = new ViewMatterModel();
                viewMatterModel.setId(jsonObject.optString("id"));
                viewMatterModel.setCreated(jsonObject.optString("created_on"));
                if (jsonObject.has("caseNumber")) {
                    viewMatterModel.setCaseNumber(jsonObject.optString("caseNumber"));
                }
                if (jsonObject.has("caseType")) {
                    viewMatterModel.setCasetype(jsonObject.optString("caseType"));
                }
                viewMatterModel.setClients(matters.optJSONObject(i).optJSONArray("clients"));
                if (jsonObject.has("corporate")) {
                    viewMatterModel.setCorporate(jsonObject.getJSONArray("corporate"));
                }

                JSONArray client = viewMatterModel.getClients();
                ArrayList<String> clientNamesList = new ArrayList<>();

                if (client != null && client.length() > 0) {
                    for (int j = 0; j < client.length(); j++) {
                        Object item = client.get(j);
                        if (item instanceof JSONArray) {
                            JSONArray clientArray = (JSONArray) item;
                            for (int k = 0; k < clientArray.length(); k++) {
                                if (!clientArray.isNull(k)) {
                                    JSONObject clientObj = clientArray.getJSONObject(k);
                                    if (clientObj.has("name") && !clientObj.getString("name").isEmpty()) {
                                        clientNamesList.add(clientObj.getString("name"));
                                    }
                                }
                            }
                        } else if (item instanceof JSONObject) {
                            JSONObject clientObj = (JSONObject) item;
                            if (clientObj.has("name") && !clientObj.getString("name").isEmpty()) {
                                clientNamesList.add(clientObj.getString("name"));
                            }
                        }
                    }
                }

                if (clientNamesList.isEmpty() && viewMatterModel.getCorporate() != null && viewMatterModel.getCorporate().length() > 0) {
                    JSONArray corporateArray = viewMatterModel.getCorporate();
                    for (int j = 0; j < corporateArray.length(); j++) {
                        JSONObject corpClient = corporateArray.getJSONObject(j);
                        String corpName = corpClient.optString("name");
                        if (!corpName.isEmpty()) {
                            clientNamesList.add(corpName);
                        }
                    }
                }

                String client_name = TextUtils.join(", ", clientNamesList);
                viewMatterModel.setClient_name(client_name);
                if (jsonObject.has("courtName")) {
                    viewMatterModel.setCourtName(jsonObject.optString("courtName"));
                }
                if (jsonObject.has("isdisabled")) {
                    viewMatterModel.setIsdisabled(jsonObject.optBoolean("isdisabled"));
                }
                if (jsonObject.has("date_of_filling")) {
                    viewMatterModel.setDate_of_filling(AndroidUtils.formatToMMMddYYYY(jsonObject.optString("date_of_filling")));
                }
                if (jsonObject.has("closedate")) {
                    viewMatterModel.setClosedate(AndroidUtils.formatToMMMddYYYY(jsonObject.optString("closedate")));
                }
                if (jsonObject.has("matterNumber")) {
                    viewMatterModel.setMatterNumber(jsonObject.optString("matterNumber"));
                }
                if (jsonObject.has("matterType")) {
                    viewMatterModel.setMatterType(jsonObject.optString("matterType"));
                }
                if (jsonObject.has("startdate")) {
                    viewMatterModel.setStartdate(AndroidUtils.formatToMMMddYYYY(jsonObject.optString("startdate")));
                }
                if (jsonObject.has("timesheets")) {
                    viewMatterModel.setTimesheets(jsonObject.optJSONArray("timesheets"));
                }
                if (jsonObject.has("created_date")) {
                    viewMatterModel.setCreated_date(AndroidUtils.formatToMMMddYYYY(jsonObject.optString("created_date")));
                }
                if (jsonObject.has("matter_id")) {
                    viewMatterModel.setMatter_id(jsonObject.optString("matter_id"));
                }
                viewMatterModel.setDescription(jsonObject.optString("description"));
                viewMatterModel.setDocuments(jsonObject.optJSONArray("documents"));
                viewMatterModel.setGroupAcls(matters.optJSONObject(i).optJSONArray("groupAcls"));
                viewMatterModel.setGroups(matters.optJSONObject(i).optJSONArray("groups"));
                if (jsonObject.has("hearingDateDetails")) {
                    viewMatterModel.setHearingDateDetails(jsonObject.optJSONObject("hearingDateDetails"));
                }
                viewMatterModel.setIs_editable(jsonObject.getBoolean("is_editable"));
                if (jsonObject.has("judges")) {
                    viewMatterModel.setJudges(jsonObject.optString("judges"));
                }
                if (jsonObject.has("matterClosedDate")) {
                    viewMatterModel.setMatterClosedDate(jsonObject.optString("matterClosedDate"));
                }
                viewMatterModel.setMembers(jsonObject.optJSONArray("members"));
                if (jsonObject.has("nextHearingDate")) {
                    viewMatterModel.setNextHearingDate(jsonObject.optString("nextHearingDate"));
                }
                if (jsonObject.has("opponentAdvocates")) {
                    viewMatterModel.setOpponentAdvocates(jsonObject.optJSONArray("opponentAdvocates"));
                }
                viewMatterModel.setOwner(jsonObject.optJSONObject("owner"));
                JSONObject owner = viewMatterModel.getOwner();
                if (!(viewMatterModel.getOwner().length() == 0)) {
                    String owner_name = owner.getString("name");
                    String owner_id = owner.getString("id");
                    viewMatterModel.setOwner_name(owner_name);
                } else {
                    viewMatterModel.setOwner_name(" ");
                }
                viewMatterModel.setPriority(jsonObject.optString("priority"));
                viewMatterModel.setStatus(jsonObject.optString("status"));
                JSONObject tagsObject = jsonObject.getJSONObject("tags");
                JSONArray tagsArray = new JSONArray();
                Iterator<String> keys = tagsObject.keys();
                while (keys.hasNext()) {
                    String key = keys.next();
                    try {
                        tagsArray.put(tagsObject.getString(key));
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
                viewMatterModel.setTags_list(tagsArray);
                if (jsonObject.has("tempClients")) {
                    viewMatterModel.setTempClients(jsonObject.optJSONArray("tempClients"));
                }
                if (jsonObject.has("timesheets")) {
                    viewMatterModel.setTimesheets(jsonObject.optJSONArray("timesheets"));
                }
                viewMatterModel.setTemporaryClients(jsonObject.optJSONArray("temporaryClients"));
                viewMatterModel.setTitle(jsonObject.optString("title"));

                matterList.add(viewMatterModel);

                SimpleDateFormat dateFormat = new SimpleDateFormat("MMM dd, yyyy", Locale.ENGLISH);
                Handler handler = new Handler(Looper.getMainLooper());
                handler.post(() -> Collections.sort(matterList, new Comparator<ViewMatterModel>() {
                    @Override
                    public int compare(ViewMatterModel o1, ViewMatterModel o2) {
                        try {
                            Date d1 = dateFormat.parse(o1.getCreated());
                            Date d2 = dateFormat.parse(o2.getCreated());
                            assert d2 != null;
                            return d2.compareTo(d1);
                        } catch (ParseException e) {
                            e.fillInStackTrace();
                            return 0;
                        }
                    }
                }));
            }

            // ══════════════════════════════════════════════════════════
            //  EMPTY STATE CHECK — added here, after matterList is built
            //  and before loadMatterRecyclerview() is called.
            //
            //  matter = parent Matter fragment (already assigned in
            //  onCreateView via: matter = (Matter) getParentFragment())
            //
            //  If the API returned zero matters → show empty state panel.
            //  If matters exist               → hide empty state panel
            //                                   and load RecyclerView.
            // ══════════════════════════════════════════════════════════
            if (matter != null) {
                if (matterList.isEmpty()) {
                    // No matters found — show "No Matters Yet!" screen
                    matter.showEmptyState(false);
                } else {
                    // Matters exist — hide empty state, show the list
                    matter.hideEmptyState();
                    loadMatterRecyclerview();
                }
            } else {
                // Fallback: parent fragment not found, still load list
                loadMatterRecyclerview();
            }
            // ══════════════════════════════════════════════════════════

        } catch (JSONException e) {
            AndroidUtils.showAlert(e.getMessage(), getActivity());
            e.fillInStackTrace();
        }
    }

    private void loadMatterRecyclerview() {
        try {
            rv_matter_list.removeAllViews();
            ViewMatterAdapter viewMatterAdapter = new ViewMatterAdapter(matterList, getContext(), this);
            rv_matter_list.setAdapter(viewMatterAdapter);
            AndroidUtils.LoadingRecyclerview(rv_matter_list, getContext());
            AndroidUtils.setupBottomSpacerFooter(rv_matter_list, getResources().getDimensionPixelSize(R.dimen.twentyeight_dp));
            viewMatterAdapter.setRecyclerView(rv_matter_list);
            viewMatterAdapter.getFilter().filter(Objects.requireNonNull(et_search_matter.getText()).toString());
            et_search_matter.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {
                    if (s.length() == 0) {
                        callFilteredMatterListWebservice("", "");
                    }
                }

                @Override
                public void afterTextChanged(Editable s) {}
            });
            btn_search.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    callFilteredMatterListWebservice("", "");
                }
            });
        } catch (Exception e) {
            AndroidUtils.showAlert(e.getMessage(), getActivity());
        }
    }

    private void callTimeLineWebservice() {
        try {
            progressDialog = AndroidUtils.get_progress(getActivity());
            JSONObject postdata = new JSONObject();
            Calendar calendar = new GregorianCalendar();
            TimeZone timeZone = calendar.getTimeZone();
            int offset = timeZone.getRawOffset();
            long hours = TimeUnit.MILLISECONDS.toMinutes(offset);
            long timezoneoffset = (-1) * (hours);
            WebServiceHelper.callHttpWebService(this, getContext(), WebServiceHelper.RestMethodType.GET, "matter/" + Constants.MATTER_TYPE.toLowerCase(Locale.ROOT) + "/" + TimeLineId + "/history/" + timezoneoffset, "TimeLine", postdata.toString());
        } catch (Exception e) {
            if (progressDialog != null || progressDialog.isShowing()) {
                AndroidUtils.dismiss_dialog(progressDialog);
            }
            e.fillInStackTrace();
        }
    }

    @Override
    public void DeleteMatter(ViewMatterModel viewMatterModel, ArrayList<ViewMatterModel> itemsArrayList) {
        try {
            AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(getActivity());
            LayoutInflater inflater = getActivity().getLayoutInflater();
            View view = inflater.inflate(R.layout.delete_relationship, null);
            TextView tv_confirmation = view.findViewById(R.id.tv_confirmation);
            ImageView close_details = view.findViewById(R.id.close_documents);
            tv_confirmation.setText("Are you sure you want to Delete Document?");
            AppCompatButton bt_yes = view.findViewById(R.id.btn_yes);
            AppCompatButton btn_no = view.findViewById(R.id.btn_No);
            final AlertDialog dialog = dialogBuilder.create();
            close_details.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    groupsArrayList.clear();
                    dialog.dismiss();
                }
            });
            btn_no.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    dialog.dismiss();
                }
            });
            bt_yes.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    dialog.dismiss();
                    callDeleteMatterWebService(viewMatterModel);
                }
            });
            dialog.setView(view);
            dialog.show();
        } catch (Exception e) {
            AndroidUtils.showAlert(e.getMessage(), getActivity());
        }
    }

    @Override
    public void Edit_Matter_Info(ViewMatterModel viewMatterModel) {
        matter_id = viewMatterModel.getId();
        TimeLineId = viewMatterModel.getId();
        Header_name = viewMatterModel.getTitle();
        Constants.Matter_title = viewMatterModel.getTitle();
        viewMatterModel1 = viewMatterModel;
        if (Constants.Matter_CreateOrViewDetails.equals("Edit Matter Info")) {
            callTimeLineWebservice();
        } else {
            callEditMatterInfo(viewMatterModel);
        }
    }

    private void callDeleteMatterWebService(ViewMatterModel viewMatterModel) {
        progressDialog = AndroidUtils.get_progress(getActivity());
        try {
            JSONObject postdata = new JSONObject();
            WebServiceHelper.callHttpWebService(this, getContext(), WebServiceHelper.RestMethodType.DELETE, "matter/" + Constants.MATTER_TYPE.toLowerCase(Locale.ROOT) + "/delete/" + viewMatterModel.getId(), "Delete Matter", postdata.toString());
        } catch (Exception e) {
            if (progressDialog != null && progressDialog.isShowing()) {
                AndroidUtils.dismiss_dialog(progressDialog);
            }
            e.fillInStackTrace();
        }
    }

    @SuppressLint("ResourceType")
    @Override
    public void View_Details(ViewMatterModel viewMatterModel, ArrayList<ViewMatterModel> itemsArrayList) {
        TimeLineId = viewMatterModel.getId();
        Header_name = viewMatterModel.getTitle();
        Constants.Matter_title = "";
        Constants.Matter_title = viewMatterModel.getTitle();
        matter_id = viewMatterModel.getId();
        callEditMatterInfo(viewMatterModel);
    }

    public void callGroupsWebservice(ViewMatterModel viewMatterModel) {
        try {
            viewMatterModel1 = viewMatterModel;
            progressDialog = AndroidUtils.get_progress(getActivity());
            JSONObject postdata = new JSONObject();
            JSONArray clients = new JSONArray();
            JSONArray client = viewMatterModel.getClients();
            JSONObject client_value = new JSONObject();
            if (client.length() > 0) {
                for (int i = 0; i < client.length(); i++) {
                    client_value = client.getJSONObject(i);
                    if (client_value.has("id")) {
                        JSONObject jsonObject = new JSONObject();
                        jsonObject.put("id", client_value.getString("id"));
                        jsonObject.put("name", client_value.getString("name"));
                        jsonObject.put("type", client_value.getString("type"));
                        clients.put(jsonObject);
                    }
                }
            }
            if (viewMatterModel.getCorporate().length() > 0) {
                for (int i = 0; i < viewMatterModel.getCorporate().length(); i++) {
                    JSONObject CorpClient_value = viewMatterModel.getCorporate().getJSONObject(i);
                    if (CorpClient_value.has("id")) {
                        JSONObject jsonObject = new JSONObject();
                        jsonObject.put("id", CorpClient_value.getString("id"));
                        jsonObject.put("name", CorpClient_value.getString("name"));
                        jsonObject.put("type", CorpClient_value.getString("type"));
                        clients.put(jsonObject);
                    }
                }
            }
            postdata.put("attachment_type", "groups");
            postdata.put("clients", clients);
            postdata.put("mode", "edit");
            WebServiceHelper.callHttpWebService(this, getContext(), WebServiceHelper.RestMethodType.PUT, "matter/attachments", "Groups", postdata.toString());
        } catch (Exception e) {
            e.fillInStackTrace();
        }
    }

    @Override
    public void Update_Group(ViewMatterModel viewMatterModel) {
        callGroupsWebservice(viewMatterModel);
    }

    private void load_groups_view(ViewMatterModel viewMatterModel) {
        groupsArrayList.clear();
        originalList.clear();
        checkedList.clear();
        Matter_id = viewMatterModel.getId();
        for (int j = 0; j < Constants.groupsList_Access.size(); j++) {
            GroupsModel fullGroupList = Constants.groupsList_Access.get(j);
            ViewGroupModel viewgroypModel = new ViewGroupModel();
            viewgroypModel.setGroup_id(fullGroupList.getGroup_id());
            viewgroypModel.setGroup_name(fullGroupList.getGroup_name());
            viewgroypModel.setName(fullGroupList.getGroup_name());
            viewgroypModel.setChecked(false);
            for (int i = 0; i < viewMatterModel.getGroups().length(); i++) {
                JSONObject jsonObject = viewMatterModel.getGroups().optJSONObject(i);
                if (jsonObject.optString("id").equalsIgnoreCase(viewgroypModel.getGroup_id())) {
                    viewgroypModel.setChecked(true);
                }
                if (jsonObject.optString("id").equalsIgnoreCase(viewgroypModel.getGroup_id())) {
                    viewgroypModel.setCan_delete(false);
                }
            }
            groupsArrayList.add(viewgroypModel);
            originalList.add(viewgroypModel);
            if (viewgroypModel.isChecked()) {
                checkedList.add(viewgroypModel);
            }
        }
        openViewGroupsPopup();
    }

    private void callgroupsWebservice(ArrayList<ViewGroupModel> groupsArrayList) {
        progressDialog = AndroidUtils.get_progress(getActivity());
        try {
            JSONObject postdata = new JSONObject();
            WebServiceHelper.callHttpWebService(this, getContext(), WebServiceHelper.RestMethodType.GET, "v3/groups", "Groups", postdata.toString());
        } catch (Exception e) {
            AndroidUtils.showAlert(e.getMessage(), getActivity());
            e.fillInStackTrace();
        }
    }

    @Override
    public void Close_Matter(ViewMatterModel viewMatterModel) {
        clients   = new JSONArray();
        members   = new JSONArray();
        documents = new JSONArray();
        advocates = new JSONArray();

        try {
            if (viewMatterModel.getMembers() != null) {
                for (int i = 0; i < viewMatterModel.getMembers().length(); i++) {
                    JSONObject tm     = viewMatterModel.getMembers().getJSONObject(i);
                    JSONObject member = new JSONObject();
                    member.put("id", tm.getString("id"));
                    members.put(member);
                }
            }
            if (viewMatterModel.getDocuments() != null) {
                for (int i = 0; i < viewMatterModel.getDocuments().length(); i++) {
                    JSONObject tm  = viewMatterModel.getDocuments().getJSONObject(i);
                    JSONObject doc = new JSONObject();
                    doc.put("docid",   tm.getString("docid"));
                    doc.put("doctype", tm.getString("doctype"));
                    doc.put("user_id", tm.getString("user_id"));
                    documents.put(doc);
                }
            }
            if (viewMatterModel.getOpponentAdvocates() != null) {
                for (int i = 0; i < viewMatterModel.getOpponentAdvocates().length(); i++) {
                    JSONObject tm       = viewMatterModel.getOpponentAdvocates().getJSONObject(i);
                    JSONObject advocate = new JSONObject();
                    advocate.put("name",  tm.get("name"));
                    advocate.put("email", tm.get("email"));
                    advocate.put("phone", tm.get("phone"));
                    advocates.put(advocate);
                }
            }
            if (viewMatterModel.getClients() != null) {
                for (int i = 0; i < viewMatterModel.getClients().length(); i++) {
                    JSONObject tm     = viewMatterModel.getClients().getJSONObject(i);
                    JSONObject client = new JSONObject();
                    client.put("id",   tm.get("id"));
                    client.put("type", tm.get("type"));
                    clients.put(client);
                }
            }
        } catch (JSONException e) {
            e.fillInStackTrace();
        }
        openCloseMatterPopup(viewMatterModel);
    }

    private void openCloseMatterPopup(ViewMatterModel viewMatterModel) {
        try {
            AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(getActivity());
            LayoutInflater inflater = requireActivity().getLayoutInflater();
            View view = inflater.inflate(R.layout.delete_relationship, null);
            TextView header_name = view.findViewById(R.id.header_name);
            header_name.setTextColor(Color.BLACK);
            ImageView close_documents = view.findViewById(R.id.close_documents);
            TextView tv_confirmation = view.findViewById(R.id.tv_confirmation);
            String reopen_msg = "Are you sure you want to reopen this matter?";
            String close_msg  = "Are you sure you want to close this matter?";
            if (viewMatterModel.getStatus().equals("Closed")) {
                tv_confirmation.setText(reopen_msg);
                Matter_Status = "Active";
            } else {
                tv_confirmation.setText(close_msg);
                Matter_Status = "Closed";
            }
            AppCompatButton bt_yes = view.findViewById(R.id.btn_yes);
            AppCompatButton btn_no = view.findViewById(R.id.btn_No);
            final AlertDialog dialog = dialogBuilder.create();
            btn_no.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    dialog.dismiss();
                }
            });
            close_documents.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    dialog.dismiss();
                }
            });
            bt_yes.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    try {
                        dialog.dismiss();
                        callCloseMatterWebService(viewMatterModel, Matter_Status);
                    } catch (Exception e) {
                        e.fillInStackTrace();
                        AndroidUtils.showAlert(e.getMessage(), getActivity());
                    }
                }
            });
            dialog.setView(view);
            dialog.show();
        } catch (Exception e) {
            AndroidUtils.showAlert(e.getMessage(), getActivity());
        }
    }

    private void callCloseMatterWebService(ViewMatterModel viewMatterModel, String matter_Status) {
        progressDialog = AndroidUtils.get_progress(getActivity());
        try {
            JSONObject postdata = new JSONObject();
            postdata.put("status", matter_Status);
            WebServiceHelper.callHttpWebService(this, getContext(), WebServiceHelper.RestMethodType.PATCH, "v2/matter/" + Constants.MATTER_TYPE.toLowerCase(Locale.ROOT) + "/" + viewMatterModel.getId(), "matter_update", postdata.toString());
            Log.d("Update_Matter", postdata.toString());
        } catch (Exception e) {
            if (progressDialog != null && progressDialog.isShowing()) {
                progressDialog.dismiss();
                AndroidUtils.showAlert(e.getMessage(), getActivity());
            }
            e.fillInStackTrace();
        }
    }

    @Override
    public void ReopenMatter(ViewMatterModel viewMatterModel) {

    }
}