package com.digicoffer.lauditor.TimeSheets.ViewModels;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.animation.Animation;
import android.view.animation.LinearInterpolator;
import android.view.animation.RotateAnimation;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;

import com.digicoffer.lauditor.R;
import com.digicoffer.lauditor.TimeSheets.Adapters.ProjectAdapter;
import com.digicoffer.lauditor.TimeSheets.Models.ProjectTMModel;
import com.digicoffer.lauditor.TimeSheets.Models.ProjectsModel;
import com.digicoffer.lauditor.Webservice.AsyncTaskCompleteListener;
import com.digicoffer.lauditor.Webservice.HttpResultDo;
import com.digicoffer.lauditor.Webservice.CommonApiHelper.WebServiceHelper;
import com.digicoffer.lauditor.CommonFiles.GlobalFiles.AndroidUtils;
import com.digicoffer.lauditor.CommonFiles.CommonAdapters.CommonSpinnerAdapter;
import com.google.android.material.textfield.TextInputEditText;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

public class AGS_Projects extends Fragment implements AsyncTaskCompleteListener {
    private ListView sp_ags_project, sp_ags_tm;
    private TextInputEditText et_search_matter;
    private ImageView proj_img_dropdown_icon, proj_img_clear_icon, tm_img_dropdown_icon, tm_img_clear_icon;
    private TextView tv_sp_project, tv_sp_team_member, project_id, team_member_id;
    private String date, isweek;
    private LinearLayout team_member_layout, llprojectLayout, projectLayout;
    private boolean ischecked_project = true, ischecked_team_member = true;
    private RecyclerView rv_projects;
    ProjectAdapter projectAdapter;
    private TextView tv_billable_hours, tv_non_billable_hours, tv_total_project_hours, hours_id1, hours_id2, non_billable_id, billable_id, total_hours_id;
    private Dialog progress_dialog;
    private ArrayList<ProjectsModel> projectsList = new ArrayList<>();
    private ArrayList<ProjectsModel> updated_projectList = new ArrayList<>();
    private String selected_project;
    private ArrayList<ProjectTMModel> projectTmList = new ArrayList<>();
    private ArrayList<ProjectTMModel> updated_projectTmList = new ArrayList<>();
    private String selected_tm;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.ags_projects, container, false);
        Bundle bundle = getArguments();
        assert bundle != null;
        date = bundle.getString("date");
        isweek = bundle.getString("isweek");

        llprojectLayout = view.findViewById(R.id.llprojectLayout);
        sp_ags_project = view.findViewById(R.id.sp_ags_project);
        sp_ags_tm = view.findViewById(R.id.sp_ags_tm);
        sp_ags_project.setVisibility(View.GONE);
        sp_ags_tm.setVisibility(View.GONE);

        View project_view = view.findViewById(R.id.tv_sp_project);
        tv_sp_project = project_view.findViewById(R.id.tv_spinner_view);
        proj_img_dropdown_icon = project_view.findViewById(R.id.img_dropdown_icon);
        proj_img_clear_icon = project_view.findViewById(R.id.img_clear_icon);

        View tm_view = view.findViewById(R.id.tv_sp_team_member);
        tv_sp_team_member = tm_view.findViewById(R.id.tv_spinner_view);
        tm_img_dropdown_icon = tm_view.findViewById(R.id.img_dropdown_icon);
        tm_img_clear_icon = tm_view.findViewById(R.id.img_clear_icon);

        project_id = view.findViewById(R.id.project_id);
        projectLayout = view.findViewById(R.id.projectLayout);
        team_member_layout = view.findViewById(R.id.team_member_layout);
        team_member_id = view.findViewById(R.id.team_member_id);

        project_id.setText(R.string.project);
        team_member_id.setText(R.string.team_members);
        team_member_layout.setVisibility(View.GONE);
        View tl_search_matter = view.findViewById(R.id.tl_search_matter);
        et_search_matter = tl_search_matter.findViewById(R.id.et_Search);
        et_search_matter.setHint(R.string.search);

        rv_projects = view.findViewById(R.id.rv_projects);
        tv_billable_hours = view.findViewById(R.id.tv_billable_hours);
        hours_id1 = view.findViewById(R.id.hours_id1);
        hours_id2 = view.findViewById(R.id.hours_id2);
        non_billable_id = view.findViewById(R.id.non_billable_id);
        billable_id = view.findViewById(R.id.billable_id);
        total_hours_id = view.findViewById(R.id.total_hours_id);
        tv_non_billable_hours = view.findViewById(R.id.tv_non_billable_hours);
        tv_total_project_hours = view.findViewById(R.id.tv_total_project_hours);

        billable_id.setTextColor(requireContext().getColor(R.color.Blue_text_color));
        non_billable_id.setTextColor(requireContext().getColor(R.color.Blue_text_color));
        total_hours_id.setTextColor(requireContext().getColor(R.color.Blue_text_color));
        non_billable_id.setText(R.string.non_billable);
        billable_id.setText(R.string.billable);
        total_hours_id.setText(R.string.total_hours);
        hours_id1.setText(R.string.hours);
        hours_id2.setText(R.string.hours);

        project_view.setOnClickListener(v -> {
            if (ischecked_project) sp_ags_project.setVisibility(View.VISIBLE);
            else sp_ags_project.setVisibility(View.GONE);
            ischecked_project = !ischecked_project;
        });

        tm_view.setOnClickListener(v -> {
            AndroidUtils.display_listview(ischecked_team_member, sp_ags_tm);
            ischecked_team_member = !ischecked_team_member;
        });

        try {
            callProjectsWebService(isweek);
        } catch (ParseException e) {
            e.printStackTrace();
        }

        projectAdapter = new ProjectAdapter(new ArrayList<>(), getContext(), new ArrayList<>());
        rv_projects.setAdapter(projectAdapter);
        AndroidUtils.LoadingRecyclerview(rv_projects, getContext());
        AndroidUtils.setupBottomSpacerFooter(rv_projects, getResources().getDimensionPixelSize(R.dimen.twentyeight_dp));
        rv_projects.setItemViewCacheSize(20);
//        rv_projects.setLayoutAnimation(
//                AnimationUtils.loadLayoutAnimation(getContext(), R.anim.layout_fall_down)
//        );
//        rv_projects.scheduleLayoutAnimation();
        rv_projects.setItemViewCacheSize(20);
//        rv_projects.setHasFixedSize(true);

        et_search_matter.addTextChangedListener(new TextWatcher() {
            @Override
            public void afterTextChanged(Editable s) {
                projectAdapter.getFilter().filter(s);
            }

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }
        });
        return view;
    }

    private void callProjectsWebService(String isweek) throws ParseException {
        progress_dialog = showLoadingDialog(getActivity());
        JSONObject postdata = new JSONObject();

        if ("week".equals(isweek)) {
            if (date == null || date.isEmpty()) {
                WebServiceHelper.callHttpWebService(this, getContext(), WebServiceHelper.RestMethodType.GET,
                        "matter/timesheets/project-all-weekly", "Projects", postdata.toString());
            } else {
                SimpleDateFormat inputFormat = new SimpleDateFormat("dd-MM-yyyy", Locale.US);
                SimpleDateFormat outputFormat = new SimpleDateFormat("ddMMyyyy", Locale.US);
                Date new_date = inputFormat.parse(date);
                String outputDate = outputFormat.format(new_date);
                WebServiceHelper.callHttpWebService(this, getContext(), WebServiceHelper.RestMethodType.GET,
                        "matter/timesheets/project-all-weekly-" + outputDate, "Projects", postdata.toString());
            }
        } else {
            if (date == null || date.isEmpty()) {
                WebServiceHelper.callHttpWebService(this, getContext(), WebServiceHelper.RestMethodType.GET,
                        "matter/timesheets/project-all-monthly", "Projects", postdata.toString());
            } else {
                SimpleDateFormat inputFormat = new SimpleDateFormat("dd-MM-yyyy", Locale.US);
                SimpleDateFormat outputFormat = new SimpleDateFormat("ddMMyyyy", Locale.US);
                Date new_date = inputFormat.parse(date);
                String outputDate = outputFormat.format(new_date);
                WebServiceHelper.callHttpWebService(this, getContext(), WebServiceHelper.RestMethodType.GET,
                        "matter/timesheets/project-all-monthly-" + outputDate, "Projects", postdata.toString());
            }
        }
    }

    @Override
    public void onClick(View view) {
    }

    @Override
    public void onAsyncTaskComplete(HttpResultDo httpResult) {
        if (httpResult.getResult() == WebServiceHelper.ServiceCallStatus.Success) {
            try {
                JSONObject result = new JSONObject(httpResult.getResponseContent());
                if ("Projects".equals(httpResult.getRequestType())) {
                    if (!result.getBoolean("error")) {
                        JSONObject jsonObject = result.optJSONObject("timesheets");
                        JSONObject grandtotal = jsonObject.optJSONObject("grandTotal");
                        tv_billable_hours.setText(grandtotal.optString("billable"));
                        tv_non_billable_hours.setText(grandtotal.optString("nonbillable"));
                        tv_total_project_hours.setText(grandtotal.optString("total") + " Hours");

                        JSONArray jsonArray = jsonObject.optJSONArray("data");
                        assert jsonArray != null;
                        if (jsonArray.length() == 0) {
                            llprojectLayout.setVisibility(View.GONE);
                        } else {
                            llprojectLayout.setVisibility(View.VISIBLE);
                            loadProjectsRecyclerview();
                        }
                        loadProjects(jsonArray);
                    } else {
                        AndroidUtils.showAlert(result.optString("msg"), getActivity());
                        ischecked_project = true;
                        llprojectLayout.setVisibility(View.GONE);
                    }
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        } else if (httpResult.getResult() == WebServiceHelper.ServiceCallStatus.Failed) {
            if (progress_dialog != null && progress_dialog.isShowing())
                AndroidUtils.dismiss_dialog(progress_dialog);
            try {
                JSONObject result = new JSONObject(httpResult.getResponseContent());
//                if (result.optBoolean("error")) {
                AndroidUtils.showErrorAlert(result.optString("msg"), getActivity());
//                }
            } catch (Exception e) {
                e.fillInStackTrace();
            }
        } else {
            if (progress_dialog != null && progress_dialog.isShowing())
                AndroidUtils.dismiss_dialog(progress_dialog);
            AndroidUtils.showErrorAlert(httpResult.getResponseContent().toString(), getActivity());
        }
    }

    private void loadProjects(JSONArray jsonArray) throws JSONException {
        new Thread(() -> {
            ArrayList<ProjectsModel> tempList = new ArrayList<>();
            try {
                for (int i = 0; i < jsonArray.length(); i++) {
                    JSONObject jsonObject = jsonArray.getJSONObject(i);
                    ProjectsModel model = new ProjectsModel();
                    model.setCaseNo(jsonObject.getString("caseNo"));
                    model.setProjectName(jsonObject.getString("projectName"));
                    model.setClientNames(jsonObject.getJSONArray("clientNames"));
                    model.setMatterId(jsonObject.getString("matterId"));
                    model.setTeamMembers(jsonObject.getJSONArray("teamMembers"));
                    tempList.add(model);
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }

            new Handler(requireContext().getMainLooper()).post(() -> {
                projectsList.clear();
                projectsList.addAll(tempList);

                if (projectsList.isEmpty()) {
                    sp_ags_project.setVisibility(View.GONE);
                    safeDismissProgressDialog();
                } else {
                    loadProjectsRecyclerview();
                    updated_projectTmList.clear();
                    loadRecyclerview(projectsList, updated_projectTmList);

                    // ⏳ Wait for the RecyclerView to finish its first layout pass
                    rv_projects.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
                        @Override
                        public void onGlobalLayout() {
                            // Check if at least one visible item exists
                            if (rv_projects.getChildCount() > 0) {
                                safeDismissProgressDialog();
                                rv_projects.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                            }
                        }
                    });
                }
            });
        }).start();
    }

    public static AlertDialog showLoadingDialog(Activity activity) {
        AlertDialog dialog = null;
        try {
            AlertDialog.Builder builder = new AlertDialog.Builder(activity);
            LayoutInflater inflater = activity.getLayoutInflater();
            View view = inflater.inflate(R.layout.loading, null); // your loading layout
            builder.setView(view);
            dialog = builder.create();
            if (dialog.getWindow() != null) {
                dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            }
            dialog.setCancelable(false);
            dialog.setCanceledOnTouchOutside(false);
            dialog.show();
            ImageView loadingImage = dialog.findViewById(R.id.imageView);
            RotateAnimation rotate = new RotateAnimation(0, 360,
                    Animation.RELATIVE_TO_SELF, 0.5f,
                    Animation.RELATIVE_TO_SELF, 0.5f);
            rotate.setDuration(1000);
            rotate.setRepeatCount(Animation.INFINITE);
            rotate.setInterpolator(new LinearInterpolator());
            loadingImage.startAnimation(rotate);

        } catch (Exception e) {
            e.printStackTrace();
        }
        return dialog;
    }

    private void loadProjectsRecyclerview() {
        final CommonSpinnerAdapter spinner_adapter = new CommonSpinnerAdapter((Activity) getContext(), projectsList);
        sp_ags_project.setAdapter(spinner_adapter);
        AndroidUtils.LoadList(sp_ags_project, getContext(), projectsList.size(), true);
        sp_ags_project.setOnItemClickListener((parent, view, position, id) -> {
            projectTmList.clear();
            updated_projectList.clear();
            selected_project = projectsList.get(position).getMatterId();
            String selected_project_name = projectsList.get(position).getProjectName();
            tv_sp_project.setText(selected_project_name);
            selected_tm = "";
            tv_sp_team_member.setText(selected_tm);

            try {
                for (int i = 0; i < projectsList.size(); i++) {
                    if (projectsList.get(i).getMatterId().equals(selected_project)) {
                        for (int j = 0; j < projectsList.get(i).getTeamMembers().length(); j++) {
                            JSONObject jsonObject = projectsList.get(i).getTeamMembers().getJSONObject(j);
                            ProjectTMModel projectTMModel = new ProjectTMModel();
                            projectTMModel.setBillableHours(jsonObject.getString("billableHours"));
                            projectTMModel.setName(jsonObject.getString("name"));
                            projectTMModel.setNonBillablehours(jsonObject.getString("nonBillablehours"));
                            projectTMModel.setTotal(jsonObject.getString("total"));
                            projectTmList.add(projectTMModel);
                        }
                    }
                }
                for (ProjectsModel projectsModel : projectsList) {
                    if (projectsModel.getMatterId().equals(selected_project)) {
                        updated_projectList.add(projectsModel);
                    }
                }
                loadRecyclerview(updated_projectList, updated_projectTmList);
                tm_img_clear_icon.setVisibility(View.GONE);
                tm_img_dropdown_icon.setVisibility(View.VISIBLE);
                team_member_layout.setVisibility(projectTmList.isEmpty() ? View.GONE : View.VISIBLE);
            } catch (JSONException e) {
                e.printStackTrace();
            }

            sp_ags_project.setVisibility(View.GONE);
            ischecked_project = true;
            tv_sp_team_member.setVisibility(View.VISIBLE);
            proj_img_clear_icon.setVisibility(View.VISIBLE);
            proj_img_dropdown_icon.setVisibility(View.GONE);

            CommonSpinnerAdapter status_adapter = new CommonSpinnerAdapter((Activity) getContext(), projectTmList);
            sp_ags_tm.setAdapter(status_adapter);
            AndroidUtils.LoadList(sp_ags_tm, getContext(), projectTmList.size(), true);

            sp_ags_tm.setOnItemClickListener((parent1, view1, position1, id1) -> {
                selected_tm = projectTmList.get(position1).getName();
                updated_projectTmList.clear();
                tv_sp_team_member.setText(selected_tm);
                sp_ags_tm.setVisibility(View.GONE);
                ischecked_team_member = true;
                tm_img_clear_icon.setVisibility(View.VISIBLE);
                tm_img_dropdown_icon.setVisibility(View.GONE);

                for (ProjectTMModel projectTMModel : projectTmList) {
                    if (projectTMModel.getName().equals(selected_tm)) {
                        updated_projectTmList.add(projectTMModel);
                    }
                }
//                updated_projectTmList.clear();
                loadRecyclerview(updated_projectList, updated_projectTmList);
            });

            tm_img_clear_icon.setOnClickListener(v -> {
                selected_tm = "";
                tv_sp_team_member.setText(selected_tm);
                updated_projectTmList.clear();
                ischecked_team_member = true;
                tm_img_clear_icon.setVisibility(View.GONE);
                tm_img_dropdown_icon.setVisibility(View.VISIBLE);
                loadRecyclerview(updated_projectList, updated_projectTmList);
            });

            proj_img_clear_icon.setOnClickListener(v -> {
                // Reset selections
                selected_project = "";
                tv_sp_project.setText("");
                selected_tm = "";
                tv_sp_team_member.setText(selected_tm);
                ischecked_project = true;
                ischecked_team_member = true;
                proj_img_clear_icon.setVisibility(View.GONE);
                proj_img_dropdown_icon.setVisibility(View.VISIBLE);
                tm_img_clear_icon.setVisibility(View.GONE);
                tm_img_dropdown_icon.setVisibility(View.VISIBLE);
                team_member_layout.setVisibility(View.GONE);

                if (isAdded()) {
                    progress_dialog = showLoadingDialog(getActivity());
                    new Thread(() -> {
                        ArrayList<ProjectsModel> refreshedList = new ArrayList<>(projectsList);
                        new Handler(requireContext().getMainLooper()).post(() -> {
                            projectAdapter.updateData(refreshedList, new ArrayList<>());
                            projectAdapter.notifyDataSetChanged();

                            rv_projects.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
                                @Override
                                public void onGlobalLayout() {
                                    if (rv_projects.getChildCount() > 0) {
                                        safeDismissProgressDialog();
                                        rv_projects.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                                    }
                                }
                            });
                        });
                    }).start();
                }
            });
        });
    }

    private void safeDismissProgressDialog() {
        if (isAdded() && progress_dialog != null && progress_dialog.isShowing()) {
            progress_dialog.dismiss();
        }
    }

    private void loadRecyclerview
            (ArrayList<ProjectsModel> updated_projectlist, ArrayList<ProjectTMModel> updated_projecttmList) {
        projectAdapter.updateData(updated_projectlist, updated_projecttmList);
        projectAdapter.notifyDataSetChanged();
    }

}
