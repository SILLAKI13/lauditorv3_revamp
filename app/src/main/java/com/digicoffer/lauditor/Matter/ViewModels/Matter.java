package com.digicoffer.lauditor.Matter.ViewModels;

import static android.view.View.GONE;
import static android.view.View.VISIBLE;
import static com.digicoffer.lauditor.CommonFiles.GlobalFiles.Constants.gct_en;

import android.animation.ValueAnimator;
import android.app.Dialog;
import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.LinearLayoutCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.lifecycle.ViewModelProvider;

import com.digicoffer.lauditor.Matter.Models.GroupsModel;
import com.digicoffer.lauditor.Matter.Models.HistoryModel;
import com.digicoffer.lauditor.Matter.Models.MatterModel;
import com.digicoffer.lauditor.Matter.Models.ViewMatterModel;
import com.digicoffer.lauditor.CommonFiles.GlobalFiles.NewModel;
import com.digicoffer.lauditor.R;
import com.digicoffer.lauditor.Webservice.AsyncTaskCompleteListener;
import com.digicoffer.lauditor.Webservice.HttpResultDo;
import com.digicoffer.lauditor.Webservice.CommonApiHelper.WebServiceHelper;
import com.digicoffer.lauditor.CommonFiles.GlobalFiles.AndroidUtils;
import com.digicoffer.lauditor.CommonFiles.GlobalFiles.Constants;
import com.google.android.material.imageview.ShapeableImageView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Objects;

public class Matter extends Fragment implements AsyncTaskCompleteListener {

    // ── Step icons ─────────────────────────────────────────────────────
    ShapeableImageView siv_timeline_icon, siv_matter_icon, siv_groups, siv_documents;

    // ── Tab toggle labels (Legal / General) ───────────────────────────
    private TextView tv_legal_matter, tv_general_matter;

    // ── Create / View toggle buttons ──────────────────────────────────
    private TextView tv_create;
    private TextView tv_view;

    // ── Layout containers ─────────────────────────────────────────────
    LinearLayout create_matter_view, tv_create_matter1, tv_view_matter1;
    RelativeLayout ll_matter_info, ll_timeline;
    public LinearLayoutCompat ll_matter_type, ll_create_view;

    // ── ViewModel ─────────────────────────────────────────────────────
    private NewModel mViewModel;

    // ── Progress dialog ───────────────────────────────────────────────
    Dialog progress_dialog;

    // ── Data lists ────────────────────────────────────────────────────
    ArrayList<HistoryModel> historyList = new ArrayList<>();
    ViewMatterModel viewMatterModel = new ViewMatterModel();
    String header_name = "";
    ViewMatter chk_viewMatter;
    JSONArray jsonArray = new JSONArray();
    TextView matter_info_txt, matter_timeline_txt;
    ArrayList<ViewMatterModel> itemsArrayList = new ArrayList<>();
    public ArrayList<MatterModel> matter_arraylist = new ArrayList<>();

    // ── Progress bar views ─────────────────────────────────────────────
    private View progressTrack;
    private View progressFill;

    // ── Empty state views ──────────────────────────────────────────────
    private LinearLayout emptyStateView;
    private TextView tvEmptyTitle;
    private TextView tvEmptySubtitle;

    // ── Step constants ─────────────────────────────────────────────────
    private static final int STEP_INFO      = 0;
    private static final int STEP_GCT       = 1;
    private static final int STEP_DOCUMENTS = 2;

    // ══════════════════════════════════════════════════════════════════
    //  onCreateView
    // ══════════════════════════════════════════════════════════════════
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.create_matter, container, false);

        // ── ViewModel ────────────────────────────────────────────────
        mViewModel = new ViewModelProvider(requireActivity()).get(NewModel.class);

        // ── Step label: Matter Info ───────────────────────────────────
        matter_info_txt = view.findViewById(R.id.matter_info_txt);
        matter_info_txt.setText(R.string.matter_information);
        matter_info_txt.setTextColor(
                Objects.requireNonNull(getContext()).getColor(R.color.black));

        // ── Step label: GCT ──────────────────────────────────────────
        TextView matter_gct_txt = view.findViewById(R.id.matter_gct_txt);
        matter_gct_txt.setTextColor(
                Objects.requireNonNull(getContext()).getColor(R.color.black));
        if ("solo".equals(Constants.CATEGORY)) {
            matter_gct_txt.setText(R.string.client_s);
        } else {
            matter_gct_txt.setText(R.string.group_s_clients_amp_team_member_s);
        }

        // ── Step label: Documents ────────────────────────────────────
        TextView matter_doc_txt = view.findViewById(R.id.matter_doc_txt);
        matter_doc_txt.setTextColor(
                Objects.requireNonNull(getContext()).getColor(R.color.black));
        matter_doc_txt.setText(R.string.document_s);

        mViewModel.setData("Matter");

        // ── Step label: Timeline ─────────────────────────────────────
        matter_timeline_txt = view.findViewById(R.id.matter_timeline_txt);
        matter_timeline_txt.setTextColor(
                Objects.requireNonNull(getContext()).getColor(R.color.black));

        // ── Icon views ───────────────────────────────────────────────
        ll_matter_info    = view.findViewById(R.id.ll_matter_info);
        ll_timeline       = view.findViewById(R.id.ll_timeline);
        siv_timeline_icon = view.findViewById(R.id.siv_timeline_icon);
        siv_matter_icon   = view.findViewById(R.id.siv_matter_icon);
        siv_groups        = view.findViewById(R.id.siv_groups);
        siv_documents     = view.findViewById(R.id.siv_documents);

        // ── Layout containers ────────────────────────────────────────
        create_matter_view = view.findViewById(R.id.create_matter_view);
        ll_matter_type     = view.findViewById(R.id.ll_matter_type);
        ll_create_view     = view.findViewById(R.id.ll_create_view);

        // ── Tab buttons (Legal / General) ────────────────────────────
        tv_legal_matter  = view.findViewById(R.id.tv_legal_matter);
        tv_general_matter = view.findViewById(R.id.tv_general_matter);
        tv_legal_matter.setText(R.string.legal_matter);
        tv_general_matter.setText(R.string.general_matter);

        // ── Create / View toggle buttons ─────────────────────────────
        tv_create        = view.findViewById(R.id.tv_create_matter);
        tv_create_matter1 = view.findViewById(R.id.tv_create_matter1);
        tv_view_matter1   = view.findViewById(R.id.tv_view_matter1);
        tv_view_matter1.setVisibility(GONE);

        // ── Progress bar views ───────────────────────────────────────
        progressTrack = view.findViewById(R.id.progress_track);
        progressFill  = view.findViewById(R.id.progress_fill);

        // ── Empty state views ────────────────────────────────────────
        emptyStateView  = view.findViewById(R.id.empty_state_view);
        tvEmptyTitle    = view.findViewById(R.id.tv_empty_title);
        tvEmptySubtitle = view.findViewById(R.id.tv_empty_subtitle);

        // ── Initial visibility ───────────────────────────────────────
        ll_timeline.setVisibility(GONE);
        ll_matter_info.setVisibility(VISIBLE);

        // ── Module view: Create Matter button ────────────────────────
        AndroidUtils.setupModuleView(
                tv_create_matter1,
                getString(R.string.create_matter),
                true,
                true,
                getContext(),
                getString(R.string.list_of_legal_matters),
                clickedView -> {
                    if (!Constants.is_active) {
                        AndroidUtils.showRenewalPopup(getActivity());
                    } else {
                        Constants.Matter_id = "";
                        Constants.upload_documents_list.clear();
                        Constants.create_matter = true;
                        Constants.Matter_CreateOrViewDetails = "Create";
                        matter_arraylist.clear();
                        hideEmptyState();
                        loadCreateUI();
                    }
                }
        );

        // ── Module view: View Matter button ──────────────────────────
        AndroidUtils.setupModuleView(
                tv_view_matter1,
                getString(R.string.view_matter),
                false,
                false,
                getContext(),
                getString(R.string.create_matter),
                clickedView -> loadViewUI()
        );

        ll_create_view.setVisibility(GONE);
        tv_create.setText(R.string.create);
        tv_view = view.findViewById(R.id.tv_view_matter);
        tv_view.setText(R.string.view);

        Constants.is_CreateMatter = Constants.isCreate;

        // ── Dashboard bar-chart filter navigation ────────────────────
        if (Constants.matterFilterType != null && !Constants.matterFilterType.isEmpty()) {
            String filterType = Constants.matterFilterType;
            Constants.matterFilterType = "";
            matter_arraylist.clear();
            if (filterType.equalsIgnoreCase("General")) {
                loadGeneralMatter();
            } else {
                loadLegalMatter();
            }
        } else if (Constants.MATTER_TYPE.equals("General")) {
            matter_arraylist.clear();
            loadGeneralMatter();
        } else {
            matter_arraylist.clear();
            loadLegalMatter();
        }

        // ── Legal tab click ──────────────────────────────────────────
        tv_legal_matter.setOnClickListener(v -> {
            Constants.Matter_id = "";
            matter_arraylist.clear();
            loadLegalMatter();
        });

        // ── General tab click ────────────────────────────────────────
        tv_general_matter.setOnClickListener(v -> {
            Constants.Matter_id = "";
            matter_arraylist.clear();
            loadGeneralMatter();
        });

        // ── View toggle click ────────────────────────────────────────
        tv_view.setOnClickListener(v -> loadViewUI());

        // ── Timeline icon click ──────────────────────────────────────
        siv_timeline_icon.setOnClickListener(v -> loadTimeline());

        // ── Matter Info icon click ───────────────────────────────────
        siv_matter_icon.setOnClickListener(v -> {
            if (!Constants.create_matter) {
                loadMatterInformation();
            } else if ((matter_arraylist != null)
                    && (!matter_arraylist.isEmpty()
                    && Constants.Matter_CreateOrViewDetails.equalsIgnoreCase("Create"))) {
                loadMatterInformation();
            }
        });

        // ── GCT icon click ───────────────────────────────────────────
        siv_groups.setOnClickListener(v -> {
            if (!Constants.create_matter) {
                Matter_Gct();
            } else if ((matter_arraylist != null)
                    && (matter_arraylist.isEmpty()
                    || (matter_arraylist.get(0).getMatter_title().isEmpty()))) {
                AndroidUtils.showAlert(
                        "Please check the Matter \n Information section",
                        getActivity(), "Info");
            } else {
                if (!Constants.Matter_id.isEmpty()) {
                    loadGCT();
                } else {
                    if (Constants.matterInformation_en != null) {
                        Constants.matterInformation_en.CheckUnique();
                    }
                }
            }
        });

        // ── Documents icon click ─────────────────────────────────────
        siv_documents.setOnClickListener(v -> {
            if (!Constants.create_matter) {
                Matter_Doc();
            } else if ((matter_arraylist != null)
                    && (matter_arraylist.isEmpty()
                    || (matter_arraylist.get(0).getMatter_title().isEmpty()))) {
                AndroidUtils.showAlert(
                        "Please check the Matter \n Information section",
                        getActivity(), "Info");
            } else {
                try {
                    if (!Constants.Matter_id.isEmpty()) {
                        if (matter_arraylist != null && !matter_arraylist.isEmpty()) {
                            for (MatterModel matterModel : matter_arraylist) {
                                boolean isMemberValid =
                                        matterModel.getMembers() != null
                                                && matterModel.getMembers().length() > 0;
                                boolean isClientValid =
                                        matterModel.getClients() != null
                                                && matterModel.getClients().length() > 0;
                                boolean isCorpValid =
                                        matterModel.getCorp_client_id() != null
                                                && !matterModel.getCorp_client_id().isEmpty();
                                boolean isTempClientsValid =
                                        matterModel.getTemp_clients_list() != null
                                                && matterModel.getTemp_clients_list().length() > 0;

                                if (isMemberValid || isClientValid
                                        || isCorpValid || isTempClientsValid) {
                                    gct_en.update_matter();
                                } else {
                                    loadDocuments();
                                }
                            }
                        }
                    } else {
                        if (Constants.matterInformation_en != null) {
                            Constants.matterInformation_en.CheckUnique();
                        }
                    }
                } catch (JSONException e) {
                    throw new RuntimeException(e);
                }
            }
        });

        matter_arraylist = new ArrayList<>();
        return view;
    }

    // ══════════════════════════════════════════════════════════════════
    //  EMPTY STATE — show
    //  Call this from ViewMatter (or anywhere) when the list is empty.
    //  isCreateMode = true  → "Start Your First Matter" wording
    //  isCreateMode = false → "No Matters Yet!" wording (matches design)
    // ══════════════════════════════════════════════════════════════════
    public void showEmptyState(boolean isCreateMode) {
        if (emptyStateView == null) return;

        // Both modes use the same wording that matches the screenshot
        tvEmptyTitle.setText("No Matters Yet!");
        tvEmptySubtitle.setText(
                "Secure and organize your matters by start creating it.");

        // Hide real fragment content
        View childContainer = getView() != null
                ? getView().findViewById(R.id.child_container) : null;
        if (childContainer != null) {
            childContainer.setVisibility(GONE);
        }

        // Show empty state panel
        emptyStateView.setVisibility(VISIBLE);
    }

    // ══════════════════════════════════════════════════════════════════
    //  EMPTY STATE — hide
    //  Called before every real fragment load so child_container
    //  is visible again and the empty panel disappears.
    // ══════════════════════════════════════════════════════════════════
    public void hideEmptyState() {
        if (emptyStateView == null) return;

        emptyStateView.setVisibility(GONE);

        View childContainer = getView() != null
                ? getView().findViewById(R.id.child_container) : null;
        if (childContainer != null) {
            childContainer.setVisibility(VISIBLE);
        }
    }

    // ══════════════════════════════════════════════════════════════════
    //  PROGRESS BAR
    // ══════════════════════════════════════════════════════════════════
    private void updateProgressBar(int step, boolean isViewTimeline) {
        if (progressTrack == null || progressFill == null) return;

        if (isViewTimeline) {
            progressTrack.setVisibility(View.GONE);
            progressFill.setVisibility(View.GONE);
            return;
        }

        progressTrack.setVisibility(View.VISIBLE);
        progressFill.setVisibility(View.VISIBLE);

        progressTrack.post(() -> {
            int totalWidth = progressTrack.getWidth();

            int targetWidth;
            switch (step) {
                case STEP_GCT:
                    targetWidth = totalWidth / 2;
                    break;
                case STEP_DOCUMENTS:
                    targetWidth = totalWidth;
                    break;
                default:
                    targetWidth = 0;
                    break;
            }

            int startWidth = progressFill.getWidth();

            ValueAnimator anim = ValueAnimator.ofInt(startWidth, targetWidth);
            anim.setDuration(300);
            anim.addUpdateListener(animator -> {
                int val = (int) animator.getAnimatedValue();
                ViewGroup.LayoutParams p = progressFill.getLayoutParams();
                p.width = val;
                progressFill.setLayoutParams(p);
            });
            anim.start();
        });
    }

    // ══════════════════════════════════════════════════════════════════
    //  PUBLIC GETTER
    // ══════════════════════════════════════════════════════════════════
    public ArrayList<MatterModel> getMatter_arraylist() {
        return matter_arraylist;
    }

    // ══════════════════════════════════════════════════════════════════
    //  LOAD VIEW UI  (matter list screen)
    // ══════════════════════════════════════════════════════════════════
    public void loadViewUI() {
        Constants.GeneratedMatterId = "";
        Constants.Matter_id = "";
        Constants.allClientGroups.clear();
        Constants.upload_documents_list.clear();
        Constants.selected_temp_clients_list.clear();
        Constants.is_CreateMatter = false;

        tv_create.setBackgroundDrawable(
                getContext().getResources().getDrawable(R.drawable.button_left_background));
        tv_create.setTextColor(Color.BLACK);
        tv_view.setBackgroundDrawable(
                getContext().getResources().getDrawable(R.drawable.button_right_green_count));
        tv_view.setTextColor(Color.WHITE);

        create_matter_view.setVisibility(GONE);
        ll_matter_type.setVisibility(VISIBLE);
        ll_create_view.setVisibility(GONE);
        tv_create_matter1.setVisibility(VISIBLE);
        tv_view_matter1.setVisibility(GONE);

        // Hide progress bar on the list screen
        updateProgressBar(STEP_INFO, true);

        callGroupsWebservice();
        viewMatter();

        if (Constants.MATTER_TYPE.equals("Legal")) {
            mViewModel.setData("View Legal Matter");
            AndroidUtils.updateModuleTitle(
                    tv_create_matter1,
                    getString(R.string.list_of_legal_matters));
        } else {
            mViewModel.setData("View General Matter");
            AndroidUtils.updateModuleTitle(
                    tv_create_matter1,
                    getString(R.string.list_of_general_matters));
        }
    }

    // ══════════════════════════════════════════════════════════════════
    //  VIEW DETAILS  (called from ViewMatter with history list)
    // ══════════════════════════════════════════════════════════════════
    void View_Details(ArrayList<HistoryModel> historyList,
                      ViewMatter viewMatter,
                      String header_name,
                      ViewMatterModel viewMatterModel) {
        chk_viewMatter = viewMatter;
        matter_arraylist.add(0, viewMatterModel);

        if (Constants.owner_id.isEmpty()) {
            JSONObject jsonObject1 = viewMatterModel.getOwner();
            assert jsonObject1 != null;
            Constants.owner_id   = jsonObject1.optString("id");
            Constants.owner_name = jsonObject1.optString("name");
        }

        this.historyList   = historyList;
        this.header_name   = header_name;
        this.viewMatterModel = viewMatterModel;

        loadTimeline();
    }

    // ══════════════════════════════════════════════════════════════════
    //  LOAD TIMELINE
    // ══════════════════════════════════════════════════════════════════
    private void loadTimeline() {
        Constants.create_matter = false;
        Constants.Matter_id     = viewMatterModel.getId();

        ll_matter_type.setVisibility(GONE);
        ll_create_view.setVisibility(GONE);
        tv_create_matter1.setVisibility(GONE);
        tv_view_matter1.setVisibility(VISIBLE);
        ll_matter_info.setVisibility(GONE);
        ll_timeline.setVisibility(VISIBLE);

        siv_matter_icon.setImageDrawable(
                getContext().getResources().getDrawable(R.drawable.single_document_icon));
        siv_matter_icon.setColorFilter(null);
        updateIconStates(siv_timeline_icon);

        // Progress bar not needed in timeline
        updateProgressBar(STEP_INFO, true);

        // Hide empty state — real content incoming
        hideEmptyState();

        create_matter_view.setVisibility(VISIBLE);
        matter_info_txt.setText(R.string.matter_information);
        matter_timeline_txt.setText(R.string.timeline);

        FragmentTransaction ft = getChildFragmentManager().beginTransaction();
        Fragment childFragment  = new TimeLine(
                historyList, chk_viewMatter, header_name, this, viewMatterModel);
        ft.replace(R.id.child_container, childFragment);
        ft.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE);
        ft.addToBackStack(null);
        ft.commit();
    }

    // ══════════════════════════════════════════════════════════════════
    //  VIEW MATTER  (loads ViewMatter fragment into child_container)
    //  ViewMatter must call showEmptyState(false) if its list is empty,
    //  or hideEmptyState() if data arrives.
    // ══════════════════════════════════════════════════════════════════
    private void viewMatter() {
        hideEmptyState();

        FragmentTransaction ft = getChildFragmentManager().beginTransaction();
        ViewMatter matterInformation = new ViewMatter();
        ft.replace(R.id.child_container, matterInformation);
        ft.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE);
        ft.addToBackStack(null);
        ft.commit();
    }

    // ══════════════════════════════════════════════════════════════════
    //  LOAD CREATE UI
    // ══════════════════════════════════════════════════════════════════
    private void loadCreateUI() {
        Constants.GeneratedMatterId = "";
        Constants.selected_temp_clients_list.clear();
        Constants.is_CreateMatter = true;
        Constants.allClientGroups.clear();

        create_matter_view.setVisibility(VISIBLE);
        ll_create_view.setVisibility(GONE);
        tv_create_matter1.setVisibility(GONE);
        tv_view_matter1.setVisibility(VISIBLE);
        matter_info_txt.setText(R.string.matter_information);

        if (Constants.Matter_CreateOrViewDetails.equalsIgnoreCase("Create")) {
            tv_create.setBackgroundDrawable(
                    getContext().getResources().getDrawable(R.drawable.button_left_green_background));
            tv_create.setTextColor(Color.WHITE);
            tv_view.setBackgroundDrawable(
                    getContext().getResources().getDrawable(R.drawable.button_right_background));
            tv_view.setTextColor(Color.BLACK);

            if (Constants.MATTER_TYPE.equals("Legal")) {
                mViewModel.setData("Create Legal Matter");
            } else {
                mViewModel.setData("Create General Matter");
            }
            loadMatterInformation();
        }
    }

    // ══════════════════════════════════════════════════════════════════
    //  LOAD LEGAL MATTER
    // ══════════════════════════════════════════════════════════════════
    void loadLegalMatter() {
        Constants.MATTER_TYPE = "Legal";
        tv_legal_matter.setBackgroundDrawable(
                getContext().getResources().getDrawable(R.drawable.button_left_green_background));
        tv_legal_matter.setTextColor(Color.WHITE);
        tv_general_matter.setBackgroundDrawable(
                getContext().getResources().getDrawable(R.drawable.button_right_background));
        tv_general_matter.setTextColor(Color.BLACK);

        if (Constants.is_CreateMatter) {
            loadCreateUI();
        } else {
            loadViewUI();
        }
    }

    // ══════════════════════════════════════════════════════════════════
    //  LOAD GENERAL MATTER
    // ══════════════════════════════════════════════════════════════════
    void loadGeneralMatter() {
        Constants.MATTER_TYPE = "General";
        tv_legal_matter.setBackgroundDrawable(
                getContext().getResources().getDrawable(R.drawable.button_left_background));
        tv_legal_matter.setTextColor(Color.BLACK);
        tv_general_matter.setBackgroundDrawable(
                getContext().getResources().getDrawable(R.drawable.button_right_green_count));
        tv_general_matter.setTextColor(Color.WHITE);

        if (Constants.is_CreateMatter) {
            loadCreateUI();
        } else {
            loadViewUI();
        }
    }

    // ══════════════════════════════════════════════════════════════════
    //  LOAD DOCUMENTS  (step 2)
    // ══════════════════════════════════════════════════════════════════
    public void loadDocuments() {
        siv_matter_icon.setImageDrawable(
                getContext().getResources().getDrawable(R.drawable.single_document_icon));
        siv_matter_icon.setColorFilter(null);
        updateIconStates(siv_documents);

        updateProgressBar(STEP_DOCUMENTS, false);
        hideEmptyState();

        FragmentTransaction ft = getChildFragmentManager().beginTransaction();
        MatterDocuments_En matterInformation = new MatterDocuments_En();
        ft.replace(R.id.child_container, matterInformation);
        ft.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE);
        ft.addToBackStack(null);
        ft.commit();
    }

    // ══════════════════════════════════════════════════════════════════
    //  LOAD GCT  (step 1)
    // ══════════════════════════════════════════════════════════════════
    public void loadGCT() {
        siv_matter_icon.setImageDrawable(
                getContext().getResources().getDrawable(R.drawable.single_document_icon));
        siv_matter_icon.setColorFilter(null);
        updateIconStates(siv_groups);

        updateProgressBar(STEP_GCT, false);
        hideEmptyState();

        FragmentTransaction ft = getChildFragmentManager().beginTransaction();
        Fragment childFragment  = new GCT_En();
        ft.replace(R.id.child_container, childFragment);
        ft.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE);
        ft.addToBackStack(null);
        ft.commit();
    }

    // ══════════════════════════════════════════════════════════════════
    //  MATTER NOTES
    // ══════════════════════════════════════════════════════════════════
    void Matter_Notes() {
        siv_matter_icon.setImageDrawable(
                getContext().getResources().getDrawable(R.mipmap.timeline_green));
        siv_groups.setImageDrawable(
                getContext().getResources().getDrawable(R.mipmap.frame_white_background));
        siv_groups.setClickable(true);
        siv_documents.setImageDrawable(
                getContext().getResources().getDrawable(R.mipmap.white_document));
        siv_documents.setClickable(true);
    }

    // ══════════════════════════════════════════════════════════════════
    //  MATTER GCT  (view mode navigation to GCT tab)
    // ══════════════════════════════════════════════════════════════════
    void Matter_Gct() {
        siv_matter_icon.setImageDrawable(
                getContext().getResources().getDrawable(R.drawable.single_document_icon));
        siv_matter_icon.setColorFilter(null);
        updateIconStates(siv_groups);

        updateProgressBar(STEP_GCT,
                Constants.Matter_CreateOrViewDetails.equals("Edit Matter Info"));
        hideEmptyState();

        FragmentTransaction ft = getChildFragmentManager().beginTransaction();
        Fragment childFragment  = new GCT_En();
        ft.replace(R.id.child_container, childFragment);
        ft.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE);
        ft.addToBackStack(null);
        ft.commit();
    }

    // ══════════════════════════════════════════════════════════════════
    //  MATTER DOC  (view mode navigation to Documents tab)
    // ══════════════════════════════════════════════════════════════════
    void Matter_Doc() {
        siv_matter_icon.setImageDrawable(
                getContext().getResources().getDrawable(R.drawable.single_document_icon));
        siv_matter_icon.setColorFilter(null);
        updateIconStates(siv_documents);

        updateProgressBar(STEP_DOCUMENTS,
                Constants.Matter_CreateOrViewDetails.equals("Edit Matter Info"));
        hideEmptyState();

        FragmentTransaction ft = getChildFragmentManager().beginTransaction();
        MatterDocuments_En matterInformation = new MatterDocuments_En();
        ft.replace(R.id.child_container, matterInformation);
        ft.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE);
        ft.addToBackStack(null);
        ft.commit();
    }

    // ══════════════════════════════════════════════════════════════════
    //  LOAD MATTER INFORMATION  (step 0)
    // ══════════════════════════════════════════════════════════════════
    void loadMatterInformation() {
        ll_timeline.setVisibility(GONE);
        ll_matter_info.setVisibility(VISIBLE);

        siv_matter_icon.setImageDrawable(
                getContext().getResources().getDrawable(R.drawable.single_document_icon_white));
        siv_matter_icon.setColorFilter(null);

        updateIconStates(null);
        updateProgressBar(STEP_INFO, false);
        hideEmptyState();

        siv_groups.setClickable(true);
        siv_documents.setClickable(true);

        FragmentTransaction ft = getChildFragmentManager().beginTransaction();
        MatterInformation_En matterInformation = new MatterInformation_En();
        ft.replace(R.id.child_container, matterInformation);
        ft.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE);
        ft.addToBackStack(null);
        ft.commit();
    }

    // ══════════════════════════════════════════════════════════════════
    //  VIEW DETAILS  (called from ViewMatter row tap)
    // ══════════════════════════════════════════════════════════════════
    public void View_Details(ViewMatterModel viewMatterModel,
                             ViewMatter viewMatter,
                             ArrayList<HistoryModel> historyList,
                             String header_name) {
        chk_viewMatter = viewMatter;
        Constants.create_matter = false;

        ll_matter_type.setVisibility(GONE);
        ll_create_view.setVisibility(GONE);
        tv_create_matter1.setVisibility(GONE);
        tv_view_matter1.setVisibility(VISIBLE);

        siv_matter_icon.setImageDrawable(
                getContext().getResources().getDrawable(R.drawable.single_document_icon_white));
        siv_groups.setImageDrawable(
                getContext().getResources().getDrawable(R.mipmap.frame_white_background));
        siv_groups.setClickable(true);
        siv_documents.setImageDrawable(
                getContext().getResources().getDrawable(R.mipmap.white_document));
        siv_documents.setClickable(true);

        create_matter_view.setVisibility(VISIBLE);
        matter_info_txt.setText(R.string.matter_information);
        matter_arraylist.add(0, viewMatterModel);
        Constants.Matter_id = viewMatterModel.getId();

        updateProgressBar(STEP_INFO, false);
        hideEmptyState();

        loadMatterInformation();
    }

    // ══════════════════════════════════════════════════════════════════
    //  WEBSERVICE — Groups
    // ══════════════════════════════════════════════════════════════════
    private void callGroupsWebservice() {
        progress_dialog = AndroidUtils.get_progress(getActivity());
        JSONObject postdata = new JSONObject();
        WebServiceHelper.callHttpWebService(
                this,
                getContext(),
                WebServiceHelper.RestMethodType.PUT,
                "matter/attachments",
                "Groups",
                postdata.toString());
    }

    @Override
    public void onClick(View view) {
        // reserved
    }

    // ══════════════════════════════════════════════════════════════════
    //  WEBSERVICE — callback
    // ══════════════════════════════════════════════════════════════════
    @Override
    public void onAsyncTaskComplete(HttpResultDo httpResult) {
        if (progress_dialog != null && progress_dialog.isShowing()) {
            AndroidUtils.dismiss_dialog(progress_dialog);
        }

        if (httpResult.getResult() == WebServiceHelper.ServiceCallStatus.Success) {
            try {
                JSONObject result = new JSONObject(httpResult.getResponseContent());
                if (httpResult.getRequestType().equals("Groups")) {
                    JSONArray data = result.getJSONArray("data");
                    loadGroupsData(data);
                }
            } catch (JSONException e) {
                e.fillInStackTrace();
            }
        }
    }

    // ══════════════════════════════════════════════════════════════════
    //  LOAD GROUPS DATA
    // ══════════════════════════════════════════════════════════════════
    private void loadGroupsData(JSONArray data) {
        try {
            Constants.groupsList_Access.clear();
            for (int i = 0; i < data.length(); i++) {
                JSONObject jsonObject = data.getJSONObject(i);
                GroupsModel groupsModel = new GroupsModel();
                groupsModel.setGroup_id(jsonObject.getString("id"));
                groupsModel.setGroup_name(jsonObject.getString("name"));
                if ((!jsonObject.optString("name").equals("AAM"))
                        && (!jsonObject.optString("name").equals("SuperUser"))) {
                    Constants.groupsList_Access.add(groupsModel);
                }
            }
        } catch (JSONException e) {
            e.fillInStackTrace();
            AndroidUtils.showAlert(e.getMessage(), getActivity());
        }
    }

    // ══════════════════════════════════════════════════════════════════
    //  UPDATE ICON STATES
    //  activeIcon = the currently selected step icon (highlighted blue)
    //  null       = no icon highlighted (e.g. Matter Info step uses the
    //               matter icon separately, not in this array)
    // ══════════════════════════════════════════════════════════════════
    private void updateIconStates(@Nullable ShapeableImageView activeIcon) {
        ShapeableImageView[] navIcons = {
                siv_timeline_icon,
                siv_groups,
                siv_documents
        };
        int[] navDrawables = {
                R.drawable.timeline_new,
                R.drawable.gct_new,
                R.drawable.documents_new
        };

        for (int i = 0; i < navIcons.length; i++) {
            if (navIcons[i] == null || getContext() == null) continue;

            boolean isActive = (navIcons[i] == activeIcon);

            navIcons[i].setBackgroundColor(getContext().getColor(
                    isActive ? R.color.scheduled_text : R.color.white));

            navIcons[i].setImageDrawable(
                    getContext().getResources().getDrawable(navDrawables[i]));

            navIcons[i].setColorFilter(
                    getContext().getColor(isActive ? R.color.white : R.color.black),
                    android.graphics.PorterDuff.Mode.SRC_IN);
        }
    }
}