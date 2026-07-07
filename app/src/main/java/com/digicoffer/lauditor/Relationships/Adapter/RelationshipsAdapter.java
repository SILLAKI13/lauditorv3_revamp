package com.digicoffer.lauditor.Relationships.Adapter;

import static android.view.View.GONE;
import static android.view.View.INVISIBLE;
import static android.view.View.VISIBLE;
import static com.digicoffer.lauditor.Documents.Documents.replaceLastDotWithSlash;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;

import com.digicoffer.lauditor.Relationships.ExchangeInformationFragment;
import com.digicoffer.lauditor.Relationships.GroupRecyclerViewFragment;

import android.content.res.ColorStateList;
import android.content.res.Resources;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Handler;
import android.os.Looper;
import android.text.Editable;
import android.text.SpannableString;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.TextWatcher;
import android.text.style.AbsoluteSizeSpan;
import android.text.style.ForegroundColorSpan;
import android.text.style.StyleSpan;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;
import androidx.appcompat.widget.LinearLayoutCompat;
import androidx.cardview.widget.CardView;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.digicoffer.lauditor.Relationships.ClientRelationship;
import com.digicoffer.lauditor.Relationships.MemberModel;
import com.digicoffer.lauditor.Relationships.Model.ProfileDo;
import com.digicoffer.lauditor.Relationships.Model.RelationshipsModel;
import com.digicoffer.lauditor.Relationships.Model.SharedDocumentsDo;
import com.digicoffer.lauditor.Groups.Models.ViewGroupModel;
import com.digicoffer.lauditor.Members.Adapters.GroupsAdapter;
import com.digicoffer.lauditor.CommonFiles.GlobalFiles.NewModel;
import com.digicoffer.lauditor.R;
import com.digicoffer.lauditor.Webservice.AsyncTaskCompleteListener;
import com.digicoffer.lauditor.Webservice.HttpResultDo;
import com.digicoffer.lauditor.Webservice.CommonApiHelper.WebServiceHelper;
import com.digicoffer.lauditor.CommonFiles.GlobalFiles.AndroidUtils;
import com.digicoffer.lauditor.CommonFiles.GlobalFiles.Constants;
import com.digicoffer.lauditor.CommonFiles.GlobalFiles.DynamicUtils;
import com.digicoffer.lauditor.CommonFiles.PdfUtils.File_Content_Type;
import com.digicoffer.lauditor.CommonFiles.PdfUtils.RetrievePDFfromUrl;
import com.digicoffer.lauditor.CommonFiles.ValidationUtils.Validation;
import com.digicoffer.lauditor.CommonFiles.CommonAdapters.CommonSpinnerAdapter;
import com.github.barteksc.pdfviewer.PDFView;
import com.google.android.material.textfield.TextInputEditText;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicBoolean;

public class RelationshipsAdapter extends RecyclerView.Adapter<RelationshipsAdapter.MyViewHolder> implements Filterable, AsyncTaskCompleteListener, SharedDocumentsAdapter.EventListener {
    ArrayList<RelationshipsModel> relationshipsList = new ArrayList<>();
    ArrayList<RelationshipsModel> itemsList = new ArrayList<>();
    ArrayList<ProfileDo> citizenList = new ArrayList<>();
    int position = 0;
    MemberSelectionAdapter membersAdapter;
    ArrayList<ViewGroupModel> groupsList1;
    boolean isCancel = false;
    public View popupView;
    public  Context context;
    public EditText et_search_groups;
    AppCompatButton btn_Delete;
    JSONArray acls = new JSONArray();
    ArrayList<ViewGroupModel> oldGrouplist = new ArrayList<>();
    GroupsAdapter groupsAdapter;
    JSONArray new_groups = new JSONArray();
    CheckBox chk_select_all;
    AlertDialog dialog = null;
    public AtomicBoolean isUpdated = new AtomicBoolean(false);
    SharedDocumentsDo sharedDocumentsDo;
    ArrayList<SharedDocumentsDo> updated_shared_list = new ArrayList<>();
    ArrayList<SharedDocumentsDo> shared_by_us_list = new ArrayList<>();
    ArrayList<SharedDocumentsDo> firm_list = new ArrayList<>();
    AlertDialog alertDialog;
    Dialog progress_dialog;
    private int openActionPosition = -1;
    public AppCompatButton btn_send_request;
    Context mcontext;
    TextView tvShareDocuments;
    ArrayList<ViewGroupModel> updatedMembersList = new ArrayList<>();
    ArrayList<MemberModel> updateMembersList = new ArrayList<>();
    ArrayList<MemberModel> selectedMembersList = new ArrayList<>();
    ArrayList<SharedDocumentsDo> selected_sharedocsList = new ArrayList<>();
    ArrayList<SharedDocumentsDo> selected_client_sharedocsList = new ArrayList<>();
    ArrayList<SharedDocumentsDo> selected_firm_sharedocsList = new ArrayList<>();
    ArrayList<SharedDocumentsDo> selected_unsharedocsList = new ArrayList<>();
    ArrayList<SharedDocumentsDo> shared_list = new ArrayList<>();
    ArrayList<SharedDocumentsDo> updatedshared_list = new ArrayList<>();
    RelationshipsAdapter.MyViewHolder mholder;
    RelationshipsModel relationshipsModel_new;
    ArrayList<ViewGroupModel> groupsList = new ArrayList<>();
    AlertDialog ad_dialog, ad_dialog_delete, ad_dialog_copy, ad_dialog_docs;
    ViewGroup mParent;
    String shared_tag = "";
    String doc_nature = "";
    private String shared_relationship_id = "";
    private String shared_client_id = "";
    FragmentActivity mActivity;
    RelationshipsAdapter.EventListener eventListener;
    private RelationshipsModel relationshipmodel_profile;
    private String FLAG = "";
    View view = null;
    String groupname = "";
    String groupid = "", route = "";
    private NewModel mViewModel;
    private String TAG, highLightId = "";
    private ClientRelationship clientRelationship;
    // tracks which position is currently expanded
    private int expandedPosition = -1;

    private boolean isPopupView = false;

    public RelationshipsAdapter(ArrayList<RelationshipsModel> relationshipsList, Context context, FragmentActivity activity, EventListener listener, String TAG, ClientRelationship clientRelationship, String highLightId) {
        this.relationshipsList = relationshipsList;
        this.clientRelationship = clientRelationship;
        this.TAG = TAG;
        this.itemsList = relationshipsList;
        this.mcontext = context;
        this.mActivity = activity;
        this.eventListener = listener;
        this.isPopupView = false;
        this.highLightId = highLightId;
    }

    // Constructor for popup view
    public RelationshipsAdapter(ArrayList<RelationshipsModel> relationshipsList, Context context, FragmentActivity activity, EventListener listener, String TAG, ClientRelationship clientRelationship, String highLightId, String route, boolean isPopupView) {
        this.relationshipsList = relationshipsList;
        this.clientRelationship = clientRelationship;
        this.TAG = TAG;
        this.itemsList = relationshipsList;
        this.mcontext = context;
        this.mActivity = activity;
        this.eventListener = listener;
        this.isPopupView = isPopupView;
        this.route = route;
        this.highLightId = highLightId;
    }

    @Override
    public Filter getFilter() {
        return new Filter() {
            @Override
            protected FilterResults performFiltering(CharSequence charSequence) {
                String query = charSequence.toString().trim();
                ArrayList<RelationshipsModel> resultList;

                if (query.isEmpty()) {
                    resultList = new ArrayList<>(itemsList);
                } else {
                    resultList = new ArrayList<>();
                    for (RelationshipsModel row : itemsList) {
                        if (AndroidUtils.isNull(row.getName())
                                .toLowerCase()
                                .contains(query.toLowerCase())) {
                            resultList.add(row);
                        }
                    }
                }

                FilterResults results = new FilterResults();
                results.values = resultList;
                return results;
            }

            @SuppressWarnings("unchecked")
            @Override
            protected void publishResults(CharSequence charSequence, FilterResults results) {
                relationshipsList = (ArrayList<RelationshipsModel>) results.values;
                notifyDataSetChanged();
            }
        };
    }

    private void Send_Invite(RelationshipsModel relationshipsModel) {
        try {
            AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(mcontext);
            LayoutInflater inflater = mActivity.getLayoutInflater();
            View view = inflater.inflate(R.layout.delete_relationship, null);
            TextView tv_confirmation = view.findViewById(R.id.tv_confirmation);
            ImageView close_documents = view.findViewById(R.id.close_documents);
            String invite_txt = "Are you sure you want send relationship invite to " + relationshipsModel.getName() + ".";
            tv_confirmation.setText(invite_txt);
            AppCompatButton bt_yes = view.findViewById(R.id.btn_yes);
            AppCompatButton btn_no = view.findViewById(R.id.btn_No);
            close_documents.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    ad_dialog_copy.dismiss();
                }
            });
            btn_no.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    ad_dialog_copy.dismiss();
                }
            });
            bt_yes.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    callInviteTempClients(relationshipsModel);
                    ad_dialog_copy.dismiss();
                }
            });
            final AlertDialog dialog = dialogBuilder.create();
            ad_dialog_copy = dialog;
            dialog.setView(view);
            dialog.setCanceledOnTouchOutside(false);
            dialog.show();
        } catch (Exception e) {
            AndroidUtils.showAlert(e.getMessage(), mActivity);
        }
    }

    @Override
    public void CopyDocument(SharedDocumentsDo sharedDocumentsDo) {
        try {
            AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(mcontext);
            LayoutInflater inflater = mActivity.getLayoutInflater();
            View view = inflater.inflate(R.layout.delete_relationship, null);
            TextView tv_confirmation = view.findViewById(R.id.tv_confirmation);
            tv_confirmation.setText("Are you sure you want to copy " + sharedDocumentsDo.getName() + "?");
            AppCompatButton bt_yes = view.findViewById(R.id.btn_yes);
            AppCompatButton btn_no = view.findViewById(R.id.btn_No);
            btn_no.setTextColor(mcontext.getColor(R.color.white));
            bt_yes.setTextColor(mcontext.getColor(R.color.black));
            bt_yes.setBackground(mcontext.getDrawable(R.drawable.yes_button_red_button));
            btn_no.setBackground(mcontext.getDrawable(R.drawable.no_button_green_button));
            btn_no.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    ad_dialog_copy.dismiss();
                }
            });
            bt_yes.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    String action = "copy";
                    callCopyDocumentWebservice(sharedDocumentsDo, action);
                    ad_dialog_copy.dismiss();
                }
            });
            final AlertDialog dialog = dialogBuilder.create();
            ad_dialog_copy = dialog;
            dialog.setView(view);
            dialog.setCanceledOnTouchOutside(false);
            dialog.show();
        } catch (Exception e) {
            AndroidUtils.showAlert(e.getMessage(), mActivity);
        }
    }

    @Override
    public void viewDocument(SharedDocumentsDo sharedDocumentsDo) {
        String action = "view";
        callCopyDocumentWebservice(sharedDocumentsDo, action);
    }

    private void callCopyDocumentWebservice(SharedDocumentsDo sharedDocumentsDo, String action) {
        JSONObject jsonObject = new JSONObject();
        String request = "";
        if (Objects.equals(action, "copy")) {
            request = "Copy Document";
        } else {
            request = "View Document";
        }
        WebServiceHelper.callHttpWebService(this, mcontext, WebServiceHelper.RestMethodType.GET, "v2/relationship/" + shared_relationship_id + "/" + sharedDocumentsDo.getId() + "/" + action, request, jsonObject.toString());
    }

    public interface EventListener {
        void RefreshViewRelationshipsData();
    }

    @NonNull
    @Override
    public RelationshipsAdapter.MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        mParent = parent;
        view = LayoutInflater.from(parent.getContext()).inflate(R.layout.view_relationships_design, parent, false);
        mViewModel = new ViewModelProvider(clientRelationship.getActivity()).get(NewModel.class);
        return new RelationshipsAdapter.MyViewHolder(view);
    }

    @Override
    public int getItemViewType(int position) {
        // FIX: Do NOT return position — that disables all ViewHolder recycling.
        // Return 0 (single view type) so RecyclerView can recycle holders correctly.
        return 0;
    }

    @SuppressLint("UseCompatLoadingForDrawables")
    @Override
    public void onBindViewHolder(@NonNull RelationshipsAdapter.MyViewHolder holder, int position) {
        RelationshipsModel relationshipsModel = relationshipsList.get(position);
        mholder = holder;

        // Hide old circle dot — chip handles status display now
        holder.iv_initiated.setVisibility(View.GONE);

        if (openActionPosition == position) {
            holder.action_list_card.setVisibility(VISIBLE);
        } else {
            holder.action_list_card.setVisibility(GONE);
        }

        // FIX: Do NOT capture position as a local int variable.
        // Always call holder.getBindingAdapterPosition() inside the click listener
        // so we get the correct position at click time, not at bind time.
        holder.custom_spinner_cardview.setOnClickListener(v -> {
            int currentPosition = holder.getBindingAdapterPosition();
            if (currentPosition == RecyclerView.NO_ID) return;
            RelationshipsModel currentModel = relationshipsList.get(currentPosition);

            if (openActionPosition == currentPosition) {
                // Same item tapped again → close it
                openActionPosition = -1;
                holder.action_list_card.setVisibility(GONE);
            } else {
                // Close previously open item (if any)
                int previousOpenPosition = openActionPosition;
                openActionPosition = currentPosition;

                // Notify the previously open item to hide its card
                if (previousOpenPosition != -1) {
                    notifyItemChanged(previousOpenPosition);
                }

                // Now show the action list for this item
                showActionList(currentModel, holder);
            }
        });

        if (isPopupView) {
            holder.cv_relationships_details.setVisibility(View.GONE);
            holder.ll_expandable_layout.setVisibility(VISIBLE);

            if (route.equals("relationship_shared_with_me_individual_list")
                    || route.equals("relationship_shared_with_me_corporate_list")
                    || route.equals("relationship_shared_with_me_business_list")) {
                mViewModel.setData("Share Documents");
                holder.rb_share_document.setBackgroundDrawable(mcontext.getResources().getDrawable(R.drawable.button_right_green_count));
                holder.rb_share_document.setTextColor(Color.WHITE);
                holder.rb_profile.setBackgroundDrawable(mcontext.getResources().getDrawable(R.drawable.button_left_background));
                holder.rb_profile.setTextColor(Color.BLACK);
                holder.et_search_relationships.setVisibility(GONE);
                holder.rg_shared_status.setVisibility(VISIBLE);
                loadshared_with_us();
                shared_tag = "withme";
                unsharedDocumentation(holder, shared_tag, relationshipsModel);
            } else {
                unhideProfileDetails(relationshipsModel, holder);
            }
        } else {
            holder.cv_relationships_details.setVisibility(VISIBLE);
            holder.ll_expandable_layout.setVisibility(GONE);

            boolean isExpandable = relationshipsList.get(position).isExpandable();
            if (isExpandable) {
                highLightId = "";
                unhideProfileDetails(relationshipsModel, holder);
                holder.ll_expandable_layout.setVisibility(VISIBLE);
            } else {
                citizenList.clear();
                shared_list.clear();
                holder.ll_expandable_layout.setVisibility(GONE);
            }
        }

        holder.btn_accept.setVisibility(GONE);
        holder.btn_accept.setOnClickListener(view -> callAcceptRequest(relationshipsModel.getId()));

        holder.ll_icons.setVisibility(GONE);
        holder.custom_spinner_cardview.setVisibility(VISIBLE);
        holder.iv_activate_relationships.setVisibility(GONE);

        // Status chip — replaces iv_initiated circle dot
        if (relationshipsModel.getStatus().toLowerCase(Locale.ROOT).equals("inactive")) {

            holder.tv_initiated.setText(R.string.inactive);
            holder.tv_initiated.setTextColor(
                    ContextCompat.getColor(holder.itemView.getContext(), R.color.cancelled_text)
            );
            holder.tv_initiated.setBackgroundResource(R.drawable.cancelled_badge);
            holder.tv_initiated.setVisibility(VISIBLE);

            holder.iv_tm_relationships.setVisibility(GONE);
            holder.iv_activate_relationships.setVisibility(VISIBLE);
            holder.iv_send_invite.setVisibility(GONE);
            holder.rb_share_document.setVisibility(GONE);
            holder.rb_profile.setBackgroundDrawable(mcontext.getResources().getDrawable(R.drawable.rectangular_button_green_count));

        } else {
            if (relationshipsModel.isAccepted()) {

                holder.tv_initiated.setText(R.string.active);
                holder.tv_initiated.setTextColor(
                        ContextCompat.getColor(holder.itemView.getContext(), R.color.completed_text)
                );
                holder.tv_initiated.setBackgroundResource(R.drawable.completed_badge);
                holder.tv_initiated.setVisibility(VISIBLE);

                holder.iv_send_invite.setVisibility(GONE);
                if (!"solo".equals(Constants.CATEGORY)) {
                    holder.iv_tm_relationships.setVisibility(VISIBLE);
                } else {
                    holder.iv_tm_relationships.setVisibility(GONE);
                }

            } else {
                holder.tv_initiated.setText(R.string.pending);
                holder.tv_initiated.setTextColor(
                        ContextCompat.getColor(holder.itemView.getContext(), R.color.pending_text)
                );
                holder.tv_initiated.setBackgroundResource(R.drawable.pending_badge);
                holder.tv_initiated.setVisibility(VISIBLE);

                holder.iv_send_invite.setVisibility(GONE);
                holder.iv_tm_relationships.setVisibility(GONE);
                holder.rb_share_document.setVisibility(GONE);
                holder.rb_profile.setBackgroundDrawable(mcontext.getResources().getDrawable(R.drawable.rectangular_button_green_count));
            }
        }

        holder.deletedBy.setVisibility(GONE);
        holder.iv_restore_relationships.setVisibility(GONE);

        // TAG == "temp"
        if (TAG.equals("temp")
                && relationshipsModel.isIstemp()
                && !relationshipsModel.getClientType().equals("Entity")) {

            holder.tv_initiated.setVisibility(GONE);
            holder.iv_initiated.setVisibility(GONE);

            holder.iv_send_invite.setVisibility(VISIBLE);
            AndroidUtils.ToggleButton(1, holder.iv_groups_relationships);
            holder.rb_share_document.setVisibility(GONE);
            holder.rb_profile.setBackgroundDrawable(mcontext.getResources().getDrawable(R.drawable.rectangular_button_green_count));
        }

        if (!Constants.ROLE.equals("GH") && !Constants.ROLE.equals("TM")) {
            holder.iv_groups_relationships.setVisibility(GONE);
        } else {
            holder.iv_groups_relationships.setVisibility(GONE);
            holder.iv_tm_relationships.setVisibility(GONE);
        }

        if (relationshipsModel.isDisabled()) {
            AndroidUtils.ToggleButton(0, holder.iv_send_invite);
            AndroidUtils.ToggleButton(0, holder.iv_groups_relationships);
            AndroidUtils.ToggleButton(0, holder.iv_tm_relationships);
        } else {
            if (relationshipsModel.isCanAccess()) {
                AndroidUtils.ToggleButton(1, holder.iv_send_invite);
                AndroidUtils.ToggleButton(1, holder.iv_groups_relationships);
                AndroidUtils.ToggleButton(1, holder.iv_tm_relationships);
            } else {
                AndroidUtils.ToggleButton(0, holder.iv_send_invite);
                AndroidUtils.ToggleButton(0, holder.iv_groups_relationships);
                AndroidUtils.ToggleButton(0, holder.iv_tm_relationships);
            }
        }

        if (relationshipsModel.isCanAccept()) {
            holder.btn_accept.setVisibility(VISIBLE);
            holder.iv_groups_relationships.setVisibility(GONE);
            holder.iv_tm_relationships.setVisibility(GONE);
            holder.iv_initiated.setVisibility(GONE);
            holder.tv_initiated.setVisibility(GONE);
        }

        Log.i("Tag", "Relationship:" + relationshipsModel.getAdminName());
        holder.tv_relationship_name.setText(relationshipsModel.getName());
        holder.deletedBy.setText("Deleted By " + relationshipsModel.getDeletedBy());
        holder.tv_created_date.setText("Created " + relationshipsModel.getCreated());
        holder.tv_consumer.setText(relationshipsModel.getClientType());

        // TAG == "deleted"
        if (TAG.equals("deleted")) {
            holder.tv_initiated.setVisibility(INVISIBLE);
            holder.iv_initiated.setVisibility(GONE);

            holder.deletedBy.setVisibility(VISIBLE);
            holder.iv_restore_relationships.setVisibility(VISIBLE);
            holder.iv_activate_relationships.setVisibility(GONE);
            holder.iv_groups_relationships.setVisibility(GONE);
            holder.iv_tm_relationships.setVisibility(GONE);
            holder.rb_share_document.setVisibility(GONE);
        }

        // Click listeners
        holder.iv_groups_relationships.setOnClickListener(view -> {
            try {
                groupsList.clear();
                updatedMembersList.clear();
                callGroupsWebservice(holder, relationshipsModel);
            } catch (Exception e) {
                AndroidUtils.showAlert(e.getMessage(), mActivity);
                e.fillInStackTrace();
            }
        });

        holder.iv_tm_relationships.setOnClickListener(view -> {
            try {
                callMembersWebservice(holder, relationshipsModel);
            } catch (Exception e) {
                AndroidUtils.showAlert(e.getMessage(), mActivity);
                e.fillInStackTrace();
            }
        });

        holder.iv_send_invite.setOnClickListener(v ->
                AndroidUtils.showConfirmationDialog(
                        mcontext, "Confirmation",
                        "Are you sure you want send relationship invite to " + relationshipsModel.getName() + " ?",
                        relationshipsModel.getName(),
                        new AndroidUtils.OnConfirmListener() {
                            @Override
                            public void onSave() {
                                callInviteTempClients(relationshipsModel);
                            }

                            @Override
                            public void onCancel() {
                            }
                        }
                )
        );

        holder.iv_restore_relationships.setOnClickListener(view ->
                AndroidUtils.showConfirmationDialog(
                        mcontext, "Confirmation",
                        "Are you sure to restore " + relationshipsModel.getName() + " relationship?",
                        relationshipsModel.getName(),
                        new AndroidUtils.OnConfirmListener() {
                            @Override
                            public void onSave() {
                                callRestoreRelationshipWebservice(relationshipsModel.getId());
                            }

                            @Override
                            public void onCancel() {
                            }
                        }
                )
        );

        holder.iv_activate_relationships.setOnClickListener(view ->
                AndroidUtils.showConfirmationDialog(
                        mcontext, "Confirmation",
                        "Are you sure to activate " + relationshipsModel.getName() + " relationship?",
                        relationshipsModel.getName(),
                        new AndroidUtils.OnConfirmListener() {
                            @Override
                            public void onSave() {
                                callRestoreRelationshipWebservice(relationshipsModel.getId());
                            }

                            @Override
                            public void onCancel() {
                            }
                        }
                )
        );

        FLAG = "first_click";

        holder.iv_delete_relationships.setOnClickListener(view -> deleteRelationships(relationshipsModel));

        holder.rb_profile.setOnClickListener(v -> {
            mViewModel.setData("Profile");
            unhideProfileDetails(relationshipsModel, holder);
        });

        // cv_relationships_details click → launch ExchangeInformationActivity
        holder.cv_relationships_details.setOnClickListener(view -> {
            if (!TAG.equals("deleted")) {
                launchExchangeInformation(relationshipsModel);
                highLightId = "";
            }
        });

        holder.rb_share_document.setOnClickListener(v -> {
            mViewModel.setData("Share Documents");
            holder.rb_share_document.setBackgroundDrawable(mcontext.getResources().getDrawable(R.drawable.button_right_green_count));
            holder.rb_share_document.setTextColor(Color.WHITE);
            holder.rb_profile.setBackgroundDrawable(mcontext.getResources().getDrawable(R.drawable.button_left_background));
            holder.rb_profile.setTextColor(Color.BLACK);
            holder.et_search_relationships.setVisibility(GONE);
            holder.rg_shared_status.setVisibility(VISIBLE);
            loadshared_with_us();
            unsharedDocumentation(holder, shared_tag, relationshipsModel);
        });

        holder.iv_share_docs.setOnClickListener(view -> {
            shared_tag = "byme";
            doc_nature = "share_doc";
            unsharedDocumentation(holder, shared_tag, relationshipsModel);
            holder.rb_shared_with_us.setBackground(mcontext.getResources().getDrawable(R.drawable.button_left_background));
            holder.rb_shared_with_us.setTextColor(Color.BLACK);
            holder.rb_shared_by_us.setBackground(mcontext.getResources().getDrawable(R.drawable.button_right_background));
            holder.rb_shared_by_us.setTextColor(Color.BLACK);
            holder.rg_document_type.setVisibility(VISIBLE);
            if (!"solo".equals(Constants.CATEGORY)) {
                holder.rb_firm_document.setVisibility(VISIBLE);
                holder.rb_client_document.setBackgroundDrawable(mcontext.getResources().getDrawable(R.drawable.button_left_background));
                holder.rb_client_document.setTextColor(Color.BLACK);
                holder.rb_firm_document.setBackgroundDrawable(mcontext.getResources().getDrawable(R.drawable.button_right_background));
                holder.rb_firm_document.setTextColor(Color.BLACK);
            } else {
                holder.rb_client_document.setBackgroundDrawable(mcontext.getResources().getDrawable(R.drawable.rounder_button_grey));
                holder.rb_client_document.setTextColor(Color.BLACK);
                holder.rb_firm_document.setVisibility(GONE);
            }
        });

        holder.rb_shared_with_us.setOnClickListener(v -> {
            try {
                loadshared_with_us();
                unsharedDocumentation(holder, shared_tag, relationshipsModel);
            } catch (Resources.NotFoundException e) {
                AndroidUtils.showAlert(e.getMessage(), mActivity);
            }
        });

        holder.rb_shared_by_us.setOnClickListener(v -> {
            try {
                holder.rb_shared_with_us.setBackground(mcontext.getResources().getDrawable(R.drawable.button_left_background));
                holder.rb_shared_with_us.setTextColor(Color.BLACK);
                holder.rb_shared_by_us.setBackground(mcontext.getResources().getDrawable(R.drawable.button_right_green_count));
                holder.rb_shared_by_us.setTextColor(Color.WHITE);
                shared_tag = "byme";
                doc_nature = "byme";
                unsharedDocumentation(holder, shared_tag, relationshipsModel);
            } catch (Resources.NotFoundException e) {
                AndroidUtils.showAlert(e.getMessage(), mActivity);
            }
        });

        holder.rb_client_document.setOnClickListener(v -> {
            if (!"solo".equals(Constants.CATEGORY)) {
                holder.rb_firm_document.setVisibility(VISIBLE);
                holder.rb_client_document.setBackgroundDrawable(mcontext.getResources().getDrawable(R.drawable.button_left_green_background));
                holder.rb_client_document.setTextColor(Color.WHITE);
                holder.rb_firm_document.setBackgroundDrawable(mcontext.getResources().getDrawable(R.drawable.button_right_background));
                holder.rb_firm_document.setTextColor(Color.BLACK);
            } else {
                holder.rb_client_document.setBackgroundDrawable(mcontext.getResources().getDrawable(R.drawable.rounder_button_green));
                holder.rb_client_document.setTextColor(Color.WHITE);
                holder.rb_firm_document.setVisibility(GONE);
            }
            shared_tag = "client";
            try {
                callDocumentTypeWebservice(relationshipsModel.getId(), shared_tag, holder,
                        relationshipsModel.getGroups(), relationshipsModel.getClient_id());
            } catch (Exception e) {
                e.fillInStackTrace();
            }
        });

        holder.rb_firm_document.setOnClickListener(v -> {
            holder.rb_client_document.setBackground(mcontext.getResources().getDrawable(R.drawable.button_left_background));
            holder.rb_client_document.setTextColor(Color.BLACK);
            holder.rb_firm_document.setBackground(mcontext.getResources().getDrawable(R.drawable.button_right_green_count));
            holder.rb_firm_document.setTextColor(Color.WHITE);
            shared_tag = "firm";
            callDocumentTypeWebservice(relationshipsModel.getId(), shared_tag, holder,
                    relationshipsModel.getGroups(), relationshipsModel.getClient_id());
        });
    }

    // Helper: launch ExchangeInformationActivity with all required extras
    private void launchExchangeInformation(RelationshipsModel relationshipsModel) {
        ExchangeInformationFragment fragment = ExchangeInformationFragment.newInstance(
                relationshipsModel.getId(),
                relationshipsModel.getName(),
                TAG,
                relationshipsModel.getClient_id(),
                relationshipsModel.isAccepted(),
                relationshipsModel.getGroups() != null ? relationshipsModel.getGroups().toString() : "[]"
        );

        ((AppCompatActivity) mcontext).getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.id_framelayout, fragment)
                .addToBackStack("current_fragment")
                .commit();
    }

    private void clear_search() {
        mholder.et_Search.setText("");
        mholder.et_search_relationships.setText("");
    }

    private void loadshared_with_us() {
        shared_tag = "withme";
        doc_nature = "withme";
        mholder.rb_shared_with_us.setBackgroundDrawable(mcontext.getResources().getDrawable(R.drawable.button_left_green_background));
        mholder.rb_shared_with_us.setTextColor(Color.WHITE);
        mholder.rb_shared_by_us.setBackgroundDrawable(mcontext.getResources().getDrawable(R.drawable.button_right_background));
        mholder.rb_shared_by_us.setTextColor(Color.BLACK);
    }

    // showActionList: builds the action dropdown per relationship state
    // FIX: removed the unused third argument (finalPosition1) that caused the
    // "Expected 2 arguments but found 3" compile error
    private void showActionList(RelationshipsModel relationshipsModel, MyViewHolder holder) {
        ArrayList<String> itemActions = new ArrayList<>();

        if (relationshipsModel.isAccepted() && !relationshipsModel.getStatus().toLowerCase(Locale.ROOT).equals("inactive")) {
            // "Exchange Information" opens the full Exchange page
            itemActions.add("Exchange Information");
            if (Constants.ROLE.equals("GH") || Constants.ROLE.equals("TM")) {
                itemActions.add("Manage Groups");
            }
            // FIX: Hide "Manage Team Members" for solo category — solo users have no team members
            if (!"solo".equals(Constants.CATEGORY)) {
                itemActions.add("Manage Team Members");
            }
            if (!TAG.equals("deleted")) {
                itemActions.add("Delete Relationship");
            }
        } else if (relationshipsModel.getStatus().toLowerCase(Locale.ROOT).equals("inactive")) {
            itemActions.add("Activate Relationship");
            itemActions.add("Delete Relationship");
        } else if (TAG.equals("temp") && relationshipsModel.isIstemp()) {
            itemActions.add("Send Invite");
            itemActions.add("Delete Relationship");
        } else if (!relationshipsModel.isAccepted()) {
            // even for pending, allow viewing exchange info
            itemActions.add("Exchange Information");
            itemActions.add("Delete Relationship");
        }

        if (TAG.equals("deleted")) {
            itemActions.add("Restore Relationship");
            itemActions.add("Permanently Delete");
        }

        // Set up the spinner adapter
        CommonSpinnerAdapter<String> itemAdapter = new CommonSpinnerAdapter<>((Activity) mcontext, itemActions);
        holder.sp_action.setAdapter(itemAdapter);
        holder.sp_action.post(() -> AndroidUtils.setDynamicHeight(holder.sp_action));

        // Show the action list card
        holder.action_list_card.setVisibility(VISIBLE);

        // Handle item selection
        holder.sp_action.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                String selectedAction = itemActions.get(i);
                handleAction(relationshipsModel, selectedAction, holder);
                // Hide the action list card after selection
                holder.action_list_card.setVisibility(GONE);
                // Reset open position
                openActionPosition = -1;
            }
        });
    }

    // handleAction with holder (primary — called from showActionList)
    private void handleAction(RelationshipsModel relationshipsModel, String action, MyViewHolder holder) {
        switch (action) {
            case "Exchange Information":
                launchExchangeInformation(relationshipsModel);
                break;
            case "Manage Groups":
                callGroupsWebservice(holder, relationshipsModel);
                break;
            case "Manage Team Members":
                // FIX: pass holder directly so callMembersWebservice captures it correctly
                callMembersWebservice(holder, relationshipsModel);
                break;
            case "Delete Relationship":
                deleteRelationships(relationshipsModel);
                break;
            case "Share Documents":
                mViewModel.setData("Share Documents");
                holder.rb_share_document.setBackgroundDrawable(mcontext.getResources().getDrawable(R.drawable.button_right_green_count));
                holder.rb_share_document.setTextColor(Color.WHITE);
                holder.rb_profile.setBackgroundDrawable(mcontext.getResources().getDrawable(R.drawable.button_left_background));
                holder.rb_profile.setTextColor(Color.BLACK);
                holder.et_search_relationships.setVisibility(GONE);
                holder.rg_shared_status.setVisibility(VISIBLE);
                loadshared_with_us();
                unsharedDocumentation(holder, shared_tag, relationshipsModel);
                break;
            case "Activate Relationship":
                callRestoreRelationshipWebservice(relationshipsModel.getId());
                break;
            case "Send Invite":
                callInviteTempClients(relationshipsModel);
                break;
            case "Accept Request":
                callAcceptRequest(relationshipsModel.getId());
                break;
            case "Reject Request":
                deleteRelationships(relationshipsModel);
                break;
            case "Restore Relationship":
                callRestoreRelationshipWebservice(relationshipsModel.getId());
                break;
            case "Permanently Delete":
                callTerminateRelationshipWebservice(relationshipsModel.getId());
                break;
        }
    }

    private void showActionDialog(RelationshipsModel relationshipsModel, ArrayList<String> actions) {
        // Create custom dialog with list of actions
        AlertDialog.Builder builder = new AlertDialog.Builder(mcontext);
        builder.setTitle("Actions");

        String[] actionArray = actions.toArray(new String[0]);

        builder.setItems(actionArray, (dialog, which) -> {
            String selectedAction = actionArray[which];
            handleAction(relationshipsModel, selectedAction);
        });

        builder.setNegativeButton("Cancel", null);
        builder.show();
    }

    // handleAction without holder (fallback — called from showActionDialog)
    private void handleAction(RelationshipsModel relationshipsModel, String action) {
        switch (action) {
            case "Exchange Information":
                launchExchangeInformation(relationshipsModel);
                break;
            case "Manage Groups":
                callGroupsWebservice(mholder, relationshipsModel);
                break;
            case "Manage Team Members":
                callMembersWebservice(mholder, relationshipsModel);
                break;
            case "Delete Relationship":
                deleteRelationships(relationshipsModel);
                break;
            case "Share Documents":
                mViewModel.setData("Share Documents");
                mholder.rb_share_document.setBackgroundDrawable(mcontext.getResources().getDrawable(R.drawable.button_right_green_count));
                mholder.rb_share_document.setTextColor(Color.WHITE);
                mholder.rb_profile.setBackgroundDrawable(mcontext.getResources().getDrawable(R.drawable.button_left_background));
                mholder.rb_profile.setTextColor(Color.BLACK);
                mholder.et_search_relationships.setVisibility(GONE);
                mholder.rg_shared_status.setVisibility(VISIBLE);
                loadshared_with_us();
                unsharedDocumentation(mholder, shared_tag, relationshipsModel);
                break;
            case "Activate Relationship":
                callRestoreRelationshipWebservice(relationshipsModel.getId());
                break;
            case "Send Invite":
                callInviteTempClients(relationshipsModel);
                break;
            case "Accept Request":
                callAcceptRequest(relationshipsModel.getId());
                break;
            case "Reject Request":
                deleteRelationships(relationshipsModel);
                break;
            case "Restore Relationship":
                callRestoreRelationshipWebservice(relationshipsModel.getId());
                break;
            case "Permanently Delete":
                callTerminateRelationshipWebservice(relationshipsModel.getId());
                break;
        }
    }

    private void callDocumentTypeWebservice(String id, String shared_tag, MyViewHolder
            holder, JSONArray jsonArray, String client_id) {
        progress_dialog = AndroidUtils.get_progress(mActivity);
        try {
            mholder = holder;
            shared_relationship_id = id;
            shared_client_id = client_id;
            JSONObject jsonObject = new JSONObject();
            if (shared_tag.equals("client")) {
                jsonObject.put("category", "client");
                jsonObject.put("clients", client_id);
                jsonObject.put("matters", "all");
            } else {
                JSONArray groups = new JSONArray();
                if (jsonArray != null) {
                    for (int i = 0; i < jsonArray.length(); i++) {
                        JSONObject jsonObject1 = jsonArray.getJSONObject(i);
                        groups.put((jsonObject1.getString("id")));
                    }
                }
                jsonObject.put("category", "firm");
                jsonObject.put("groups", groups);
            }
            WebServiceHelper.callHttpWebService(this, mcontext, WebServiceHelper.RestMethodType.PUT, "v3/document/filter", "Existing Documents", jsonObject.toString());
        } catch (Exception e) {
            e.fillInStackTrace();
        }
    }

    private void unsharedDocumentation(MyViewHolder holder, String
            shared_tag, RelationshipsModel relationshipsModel) {
        holder.cv_Profile.setVisibility(GONE);
        holder.rg_shared_status.setVisibility(VISIBLE);
        holder.ll_documents.setVisibility(VISIBLE);
        holder.nestedScrollView.setVisibility(GONE);
        holder.rg_document_type.setVisibility(GONE);
        holder.ll_documents.setVisibility(GONE);
        holder.iv_share_docs.setVisibility(VISIBLE);
        shared_list.clear();
        callSharedDocumentsWebservice(relationshipsModel.getId(), shared_tag, holder, relationshipsModel.getClient_id());
    }

    private void callInviteTempClients(RelationshipsModel relationshipsModel) {
        try {
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("client_id", relationshipsModel.getClient_id());
            jsonObject.put("client_type", relationshipsModel.getClientType());
            WebServiceHelper.callHttpWebService(this, mcontext, WebServiceHelper.RestMethodType.POST, "v3/convert-temp-clients", "Invite Temp Clients", jsonObject.toString());
        } catch (Exception e) {
            e.fillInStackTrace();
        }
    }

    public void View_doc(String doc_id, SharedDocumentsDo sharedDocumentsDo) {
        progress_dialog = AndroidUtils.get_progress(mActivity);
        this.sharedDocumentsDo = sharedDocumentsDo;
        JSONObject jsonObject = new JSONObject();
        if (shared_tag.equals("withme")) {
            if ((TAG.equals("corporate")) || (TAG.equals("business")))
                WebServiceHelper.callHttpWebService(this, mcontext, WebServiceHelper.RestMethodType.GET, "v3/document/" + doc_id + "/view", "View Other Doc", jsonObject.toString());
            else
                WebServiceHelper.callHttpWebService(this, mcontext, WebServiceHelper.RestMethodType.GET, "v2/relationship/" + shared_relationship_id + "/" + doc_id + "/view", "View Doc With Us", jsonObject.toString());
        } else {
            WebServiceHelper.callHttpWebService(this, mcontext, WebServiceHelper.RestMethodType.GET, "v3/document/" + doc_id + "/view", "View Other Doc", jsonObject.toString());
        }
    }

    public void callDecryptApi(String id) {
        try {
            progress_dialog = AndroidUtils.get_progress(mActivity);
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("docid", id);
            if (shared_tag.equals("withme")) {
                jsonObject.put("shared_doc", true);
            }
            WebServiceHelper.callHttpWebService(this, mcontext, WebServiceHelper.RestMethodType.POST, Constants.decryptUrl, "Decrypt Doc", jsonObject.toString());
        } catch (Exception e) {
            if (progress_dialog != null && progress_dialog.isShowing()) {
                AndroidUtils.dismiss_dialog(progress_dialog);
            }
        }
    }

    private void checkViewType(String url, String CONTENT_TYPE, SharedDocumentsDo
            sharedDocumentsDo) {
        boolean isImage = File_Content_Type.isImage(sharedDocumentsDo.getContent_type());
        boolean isPDF = File_Content_Type.isPDF(sharedDocumentsDo.getContent_type());
        boolean isEncrypted = sharedDocumentsDo.isAdded_encryption() || sharedDocumentsDo.isIs_encrypted();

        if (isEncrypted) {
            callDecryptApi(sharedDocumentsDo.getId());
        } else if (!isPDF && (!isImage)) {
            callOtherDocViewApi(sharedDocumentsDo.getId());
        } else {
            display_doc(url, sharedDocumentsDo);
        }
    }

    private void display_doc(String url, SharedDocumentsDo sharedDocumentsDo) {
        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(mActivity);
        LayoutInflater inflater = mActivity.getLayoutInflater();
        View view = inflater.inflate(R.layout.view_documents, null);
        ProgressBar progressBar = view.findViewById(R.id.progress_pdf);
        ImageView iv_image = view.findViewById(R.id.doc_image);
        PDFView idPDFView = view.findViewById(R.id.idPDFView);
        WebView webView = view.findViewById(R.id.doc_webview);
        TextView header = view.findViewById(R.id.header_name);
        ImageView iv_close_edit_docs = view.findViewById(R.id.close_edit_docs);
        boolean isImage = File_Content_Type.isImage(sharedDocumentsDo.getContent_type());
        header.setText(sharedDocumentsDo.getName());
        final AlertDialog dialog = dialogBuilder.create();

        final RetrievePDFfromUrl[] pdfTask = new RetrievePDFfromUrl[1];

        iv_close_edit_docs.setOnClickListener(v -> {
            try {
                if (idPDFView != null) idPDFView.recycle();
                if (pdfTask[0] != null) {
                    pdfTask[0].cancelLoading();
                    pdfTask[0].cancel(true);
                }
            } catch (Exception ignored) {
            }
            dialog.dismiss();
        });
        String lowerUrl = url.toLowerCase();
        boolean urlIsPDF =
                lowerUrl.contains("application/pdf") ||
                        lowerUrl.contains(".pdf");
        if (urlIsPDF) {
            idPDFView.setVisibility(VISIBLE);
            progressBar.setVisibility(VISIBLE);

            pdfTask[0] = new RetrievePDFfromUrl(idPDFView, progressBar);
            pdfTask[0].execute(url);
        } else {
            if (isImage) {
                iv_image.setVisibility(VISIBLE);
                Glide.with(mcontext)
                        .load(url)
                        .placeholder(R.drawable.progress_animation)
                        .centerCrop()
                        .into(iv_image);
            } else {
                idPDFView.setVisibility(VISIBLE);
                progressBar.setVisibility(VISIBLE);

                pdfTask[0] = new RetrievePDFfromUrl(idPDFView, progressBar);
                pdfTask[0].execute(url);
            }
        }
        dialog.setCancelable(false);
        dialog.setCanceledOnTouchOutside(false);
        dialog.setView(view);
        dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        dialog.show();
    }

    private void handleDocumentDisplay(String url, SharedDocumentsDo docModel, PDFView
            idPDFView, ImageView iv_image) {
        boolean isImage = File_Content_Type.isImage(docModel.getContent_type());
        boolean isPDF = File_Content_Type.isPDF(docModel.getContent_type());
        boolean isEncrypted = docModel.isAdded_encryption() || docModel.isIs_encrypted();

        if (isEncrypted) {
            dialog.dismiss();
            callDecryptApi(docModel.getId());
            return;
        }
        String lowerUrl = url.toLowerCase();
        boolean urlIsImage =
                lowerUrl.contains("image") ||
                        lowerUrl.contains(".jpg") ||
                        lowerUrl.contains(".jpeg") ||
                        lowerUrl.contains(".png") ||
                        lowerUrl.contains(".webp");
        if (isImage || urlIsImage) {
            idPDFView.setVisibility(GONE);
            iv_image.setVisibility(VISIBLE);

            Glide.with(mcontext)
                    .load(url)
                    .placeholder(R.drawable.progress_animation)
                    .centerCrop()
                    .into(iv_image);
        } else if (isPDF) {
            iv_image.setVisibility(GONE);
            idPDFView.setVisibility(VISIBLE);
            new RetrievePDFfromUrl(idPDFView, null).execute(url);
        } else {
            dialog.dismiss();
            callOtherDocViewApi(docModel.getId());
        }
    }

    public void callOtherDocViewApi(String id) {
        try {
            progress_dialog = AndroidUtils.get_progress(mActivity);
            JSONObject jsonObject = new JSONObject();
            WebServiceHelper.callHttpWebService(this, mcontext, WebServiceHelper.RestMethodType.GET, Constants.base_URL + "v3/document/" + id + "/view", "Other Doc View", jsonObject.toString());
        } catch (Exception e) {
            if (progress_dialog != null && progress_dialog.isShowing()) {
                AndroidUtils.dismiss_dialog(progress_dialog);
            }
        }
    }

    private void openshared_with_us(ArrayList<SharedDocumentsDo> shared_list) {
        mholder.rv_shared_with_us.removeAllViews();
        if (shared_tag.equals("withme")) {
            mholder.ll_doc_button.setVisibility(GONE);
        } else {
            mholder.ll_doc_button.setVisibility(VISIBLE);
        }
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(mcontext, LinearLayoutManager.VERTICAL, false);
        mholder.rv_shared_with_us.setLayoutManager(layoutManager);
        SharedDocumentsAdapter documentsAdapter = new SharedDocumentsAdapter(shared_list, shared_tag, mcontext, this, shared_relationship_id, shared_client_id, mActivity, this, highLightId);
        mholder.rv_shared_with_us.setAdapter(documentsAdapter);
    }

    private void applyHighlight(RelationshipsAdapter.MyViewHolder holder, RelationshipsModel
            model) {
        if (highLightId != null && highLightId.contains(model.getId())) {
            holder.cv_relationships_details.setBackground(
                    mcontext.getResources().getDrawable(R.drawable.blue_stroke_card)
            );
            holder.cv_relationships_details.setCardElevation(8f);

            holder.cv_relationships_details.setBackground(
                    ContextCompat.getDrawable(holder.itemView.getContext(),
                            R.drawable.blue_stroke_card)
            );

            new Handler(Looper.getMainLooper()).postDelayed(() -> {
                holder.cv_relationships_details.setBackground(
                        ContextCompat.getDrawable(holder.itemView.getContext(),
                                R.drawable.rectangular_white_background)
                );
            }, 5000);
        } else {
            holder.cv_relationships_details.setCardBackgroundColor(
                    mcontext.getResources().getColor(android.R.color.white)
            );
            holder.cv_relationships_details.setCardElevation(4f);
        }
    }

    private void open_tm_popup() {
        try {
            AtomicBoolean isUpdated = new AtomicBoolean(false);

            AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(mcontext);
            LayoutInflater inflater = mActivity.getLayoutInflater();
            View view = inflater.inflate(R.layout.update_member_layout, null);

            // RecyclerView & Adapter
            RecyclerView rv_assign_members = view.findViewById(R.id.rv_assign_members);
            membersAdapter = new MemberSelectionAdapter(updateMembersList, (selected, userInteracted) -> {
                selectedMembersList.clear();
                selectedMembersList.addAll(selected);
                isUpdated.set(userInteracted);
            });

            rv_assign_members.setLayoutManager(new LinearLayoutManager(mcontext));
            rv_assign_members.setAdapter(membersAdapter);
            AndroidUtils.LoadList(rv_assign_members, mcontext, membersAdapter.getItemCount(), false);

            // Search filtering
            View tl_search_members = view.findViewById(R.id.tl_search_members);
            TextInputEditText et_search_members = tl_search_members.findViewById(R.id.et_Search);
            et_search_members.addTextChangedListener(new Validation(et_search_members));
            et_search_members.setHint(R.string.search_members);
            et_search_members.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                }

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {
                    membersAdapter.getFilter().filter(s.toString());
                    AndroidUtils.LoadList(rv_assign_members, mcontext, membersAdapter.getItemCount(), false);
                }

                @Override
                public void afterTextChanged(Editable s) {
                }
            });

            // Update member title
            TextView tv_update_member = view.findViewById(R.id.tv_update_member);
            if (!relationshipsModel_new.getName().isEmpty()) {
                tv_update_member.setText(mcontext.getString(R.string.update_member_access) + " - " + relationshipsModel_new.getName());
            } else {
                tv_update_member.setText(mcontext.getString(R.string.update_member_access));
            }

            // Buttons
            AppCompatButton btnSendRequest = view.findViewById(R.id.btn_create);
            AppCompatButton btnCancel = view.findViewById(R.id.btn_cancel_save);
            ImageView ivCancel = view.findViewById(R.id.close_details);

            AlertDialog dialog = dialogBuilder.create();
            dialog.setView(view);
            dialog.setCanceledOnTouchOutside(false);
            dialog.show();
            ad_dialog_docs = dialog;

            View.OnClickListener closeDialogListener = v -> {
                if (isUpdated.get()) {
                    ConfirmPopup(relationshipsModel_new.getName(), relationshipsModel_new.getId(), true, false, null);
                }
                dialog.dismiss();
            };

            btnSendRequest.setOnClickListener(v -> {
                ConfirmPopup(relationshipsModel_new.getName(), relationshipsModel_new.getId(), false, false, null);
            });

            btnCancel.setOnClickListener(closeDialogListener);
            ivCancel.setOnClickListener(closeDialogListener);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void callUpdateMembers(String shared_relationship_id) {
        try {
            progress_dialog = AndroidUtils.get_progress(mActivity);
            JSONObject jsonObject = new JSONObject();
            JSONArray jsonArray = new JSONArray();
            for (MemberModel member : selectedMembersList) {
                jsonArray.put(member.getId());
            }
            jsonObject.put("members", jsonArray);
            Log.d("Payload", jsonObject.toString());
            if (TAG.equals("corporate")) {
                WebServiceHelper.callHttpWebService(this, mcontext, WebServiceHelper.RestMethodType.PATCH, "v3/relationship/" + shared_relationship_id + "/members", "Update Members", jsonObject.toString());
            } else {
                WebServiceHelper.callHttpWebService(this, mcontext, WebServiceHelper.RestMethodType.PUT, "v2/relationship/" + shared_relationship_id + "/members", "Update Members", jsonObject.toString());
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private void ConfirmPopup(String rel_name, String shared_client_id, boolean isCancel,
                              boolean isGroup, ArrayList<ViewGroupModel> list_item) {
        try {
            AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(mcontext);
            LayoutInflater inflater = mActivity.getLayoutInflater();
            View view = inflater.inflate(R.layout.delete_relationship, null);
            TextView tv_confirmation = view.findViewById(R.id.tv_confirmation);
            TextView header_name = view.findViewById(R.id.header_name);
            header_name.setTextColor(mcontext.getColor(R.color.blue));
            ImageView close_documents = view.findViewById(R.id.close_documents);
            String invite_txt = "";
            if (isCancel) {
                header_name.setText(R.string.alert_);
                invite_txt = "Changes you made will not be saved. Do you want to save?";
                tv_confirmation.setText(invite_txt);
            } else {
                tv_confirmation.setTextColor(mcontext.getColor(R.color.black));
                header_name.setText(R.string.confirmation);
                String message;
                if (isGroup) {
                    message = "Are you sure you want to modify the Group access for " + rel_name + "?";
                } else {
                    message = "Are you sure you want to modify the Member access for " + rel_name + "?";
                }
                SpannableString spannable = new SpannableString(message);

                int start = message.indexOf(rel_name);
                if (start >= 0) {
                    spannable.setSpan(new StyleSpan(Typeface.BOLD), start, start + rel_name.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                    spannable.setSpan(new ForegroundColorSpan(Color.BLACK), start, start + rel_name.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                }

                tv_confirmation.setText(spannable);
            }

            AppCompatButton bt_yes = view.findViewById(R.id.btn_yes);
            AppCompatButton btn_no = view.findViewById(R.id.btn_No);
            btn_no.setTextColor(mcontext.getColor(R.color.white));
            bt_yes.setTextColor(mcontext.getColor(R.color.black));
            bt_yes.setBackground(mcontext.getDrawable(R.drawable.yes_button_red_button));
            btn_no.setBackground(mcontext.getDrawable(R.drawable.no_button_green_button));
            final AlertDialog dialog = dialogBuilder.create();
            ad_dialog_copy = dialog;
            dialog.setView(view);
            dialog.setCanceledOnTouchOutside(false);
            dialog.show();
            close_documents.setOnClickListener(v -> {
                if (isGroup) {
                    clientRelationship.ll_view_rel.setEnabled(true);
                    clientRelationship.ll_view_rel.setAlpha(1.0f);
                    et_search_groups.setText("");
                } else {
                    ad_dialog_docs.dismiss();
                }
                dialog.dismiss();
            });
            btn_no.setOnClickListener(view2 -> {
                if (isGroup) {
                    clientRelationship.ll_view_rel.setEnabled(true);
                    clientRelationship.ll_view_rel.setAlpha(1.0f);
                    et_search_groups.setText("");
                } else {
                    ad_dialog_docs.dismiss();
                }
                dialog.dismiss();
            });
            bt_yes.setOnClickListener(view1 -> {
                if (isGroup) {
                    Constants.mainActivity.Remove_Page(new GroupRecyclerViewFragment(mcontext, groupsList, clientRelationship, relationshipsModel_new, this));
                    callUpdateGroups(shared_client_id, list_item);
                    clientRelationship.ll_view_rel.setEnabled(true);
                    clientRelationship.ll_view_rel.setAlpha(1.0f);
                    et_search_groups.setText("");
                    popupView.setVisibility(GONE);
                    groupsList.clear();
                } else {
                    callUpdateMembers(shared_client_id);
                    ad_dialog_docs.dismiss();
                }
                dialog.dismiss();
            });
        } catch (Exception e) {
            AndroidUtils.showAlert(e.getMessage(), mActivity);
        }
    }

    private void openSharedPopupWindow(ArrayList<SharedDocumentsDo> sharedList) {
        try {
            AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(mcontext);
            LayoutInflater inflater = mActivity.getLayoutInflater();
            View view = inflater.inflate(R.layout.shared_document_recyclerview_popup, null);
            RecyclerView rvRelationshipGroups = view.findViewById(R.id.rv_relationship_documents);
            TextInputEditText etSearchRelationships = view.findViewById(R.id.et_search_relationships);
            etSearchRelationships.addTextChangedListener(new Validation(etSearchRelationships));
            LinearLayoutCompat llButtons = view.findViewById(R.id.ll_buttons);
            LinearLayout ll_select_all = view.findViewById(R.id.ll_select_all);
            TextView tv_select_all = view.findViewById(R.id.tv_select_all);
            tv_select_all.setText(R.string.select_all);
            chk_select_all = view.findViewById(R.id.chk_select_all);
            TextView tvHeaderName = view.findViewById(R.id.header_name);
            TextView tvMessage = view.findViewById(R.id.message);
            tvMessage.setGravity(Gravity.CENTER);
            tvMessage.setText(R.string.no_documents_to_show);
            tvMessage.setTextSize(DynamicUtils.twenty);
            AppCompatButton btnSendRequest = view.findViewById(R.id.btn_send_request);
            AppCompatButton btnRelationshipsCancel = view.findViewById(R.id.btn_relationships_cancel);
            ImageView ivCancel = view.findViewById(R.id.close_edit_docs);

            String headerName;
            int buttonTextResId = R.string.share;
            switch (shared_tag) {
                case "withme":
                    headerName = "Documents Shared With Us";
                    ll_select_all.setVisibility(GONE);
                    llButtons.setVisibility(GONE);
                    break;
                case "byme":
                    headerName = "Documents Shared By Us";
                    ll_select_all.setVisibility(VISIBLE);
                    llButtons.setVisibility(VISIBLE);
                    buttonTextResId = R.string.unshare;
                    break;
                case "client":
                    ll_select_all.setVisibility(VISIBLE);
                    headerName = "Client Documents";
                    break;
                default:
                    ll_select_all.setVisibility(VISIBLE);
                    headerName = "Firm Documents";
                    break;
            }

            tvHeaderName.setText(headerName);
            btnSendRequest.setText(buttonTextResId);

            if (sharedList.isEmpty()) {
                llButtons.setVisibility(GONE);
                etSearchRelationships.setVisibility(GONE);
                // FIX: still show the dialog so user sees the empty state message
                AlertDialog dialog = dialogBuilder.create();
                ad_dialog_docs = dialog;
                dialog.setView(view);
                dialog.setCanceledOnTouchOutside(false);
                dialog.show();
            } else {
                rvRelationshipGroups.setLayoutManager(new LinearLayoutManager(mcontext, LinearLayoutManager.VERTICAL, false));

                SharedDocumentsAdapter documentsAdapter = new SharedDocumentsAdapter(sharedList, shared_tag, mcontext, this, shared_relationship_id, shared_client_id, mActivity, this, highLightId);
                rvRelationshipGroups.setAdapter(documentsAdapter);

                chk_select_all.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        documentsAdapter.selectOrDeselectAll(chk_select_all.isChecked());
                        if (shared_tag.equals("byme"))
                            setSelected_unsharedocsList(sharedList);
                        else
                            setSelected_sharedocsList(sharedList);
                    }
                });
                etSearchRelationships.setHint(R.string.search_documents);
                etSearchRelationships.addTextChangedListener(new TextWatcher() {
                    @Override
                    public void onTextChanged(CharSequence s, int start, int before, int count) {
                    }

                    @Override
                    public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                    }

                    @Override
                    public void afterTextChanged(Editable s) {
                        documentsAdapter.getFilter().filter(s.toString().trim());
                    }
                });

                View.OnClickListener closeDialogListener = v -> {
                    etSearchRelationships.setText("");
                    if (shared_tag.equals("byme")) {
                        selected_unsharedocsList.clear();
                        mholder.ll_expandable_layout.setVisibility(GONE);
                        clientRelationship.viewRelationshipsData();
                    } else if (shared_tag.equals("client")) {
                        selected_sharedocsList.removeAll(selected_client_sharedocsList);
                        selected_client_sharedocsList.clear();
                        mholder.ll_expandable_layout.setVisibility(GONE);
                        clientRelationship.viewRelationshipsData();
                    } else if (shared_tag.equals("firm")) {
                        selected_sharedocsList.removeAll(selected_firm_sharedocsList);
                        selected_firm_sharedocsList.clear();
                        mholder.ll_expandable_layout.setVisibility(GONE);
                        clientRelationship.viewRelationshipsData();
                    }
                    ad_dialog_docs.dismiss();
                };

                btnRelationshipsCancel.setOnClickListener(closeDialogListener);
                ivCancel.setOnClickListener(closeDialogListener);

                btnSendRequest.setOnClickListener(v -> {
                    try {
                        updated_shared_list.clear();
                        JSONArray remove = new JSONArray();

                        ArrayList<SharedDocumentsDo> selectedDocs = documentsAdapter.getSelectedList();

                        for (SharedDocumentsDo doc : selectedDocs) {
                            JSONObject jsonObject = new JSONObject();
                            jsonObject.put("docid", doc.getId());
                            jsonObject.put("doctype", "general");

                            if (shared_tag.equals("firm")) {
                                jsonObject.put("matters", doc.getMatter_details());
                            }

                            remove.put(jsonObject);
                        }

                        updated_shared_list.addAll(selectedDocs);

                        if (!updated_shared_list.isEmpty()) {
                            etSearchRelationships.setText("");
                            remove_popup(updated_shared_list, remove);
                        } else {
                            Log.d("Selected_list_size", "Please select at least one document");
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                });

                AlertDialog dialog = dialogBuilder.create();
                ad_dialog_docs = dialog;
                dialog.setView(view);
                dialog.setCanceledOnTouchOutside(false);
                dialog.show();
            }
        } catch (Exception e) {
            AndroidUtils.showAlert(e.getMessage(), clientRelationship.getActivity());
            e.fillInStackTrace();
        }
    }


    private void remove_popup(ArrayList<SharedDocumentsDo> updatedSharedList, JSONArray
            remove) {
        try {
            AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(mcontext);
            LayoutInflater inflater = mActivity.getLayoutInflater();
            View view = inflater.inflate(R.layout.share_document_popup, null);

            TextView tvShareDoc = view.findViewById(R.id.tv_share_doc);
            TextView tvUnshareDoc = view.findViewById(R.id.tv_unshare_doc);
            RecyclerView rvShareDocuments = view.findViewById(R.id.rv_share_documents);
            RecyclerView rvUnshareDocuments = view.findViewById(R.id.rv_unshare_documents);
            LinearLayout llShareDoc = view.findViewById(R.id.ll_share_doc);
            LinearLayout llUnshareDoc = view.findViewById(R.id.ll_unshare_doc);

            tvShareDoc.setTextColor(mcontext.getResources().getColor(R.color.black));
            tvUnshareDoc.setTextColor(mcontext.getResources().getColor(R.color.black));
            if (shared_tag.equals("byme")) {
                tvUnshareDoc.setText(R.string.unshare_documents);
                tvShareDoc.setText(R.string.unshare_documents);
                tvShareDocuments.setText("Documents Unshare");
            } else {
                tvUnshareDoc.setText(R.string.share_documents);
                tvShareDoc.setText(R.string.share_documents);
                tvShareDocuments.setText("Documents Share");
            }

            updatedSharedList.addAll(shared_tag.equals("byme") ? selected_unsharedocsList : selected_sharedocsList);

            llUnshareDoc.setVisibility(selected_unsharedocsList.isEmpty() ? GONE : VISIBLE);
            llShareDoc.setVisibility(selected_sharedocsList.isEmpty() ? GONE : VISIBLE);

            setupRecyclerView(rvShareDocuments, selected_sharedocsList);
            setupRecyclerView(rvUnshareDocuments, selected_unsharedocsList);

            TextInputEditText etShareMessage = view.findViewById(R.id.et_share_message);
            etShareMessage.setVisibility(VISIBLE);

            // Find the TextView inside the included header_title layout
            tvShareDocuments = view.findViewById(R.id.tv_share_documents).findViewById(R.id.header_name);
// OR if header_title layout has a different TextView id, use that id instead
            Button btnCancelShare = view.findViewById(R.id.btn_cancel_share);
            Button btnOkShare = view.findViewById(R.id.btn_ok_share);

            if (shared_tag.equals("bym" +
                    "e")) {
                tvShareDocuments.setText("Documents Unshare");
                btnOkShare.setText("Unshare");
            } else {
                tvShareDocuments.setText("Documents Share");
                btnOkShare.setText("Share");
            }
            btnCancelShare.setOnClickListener(v -> alertDialog.dismiss());
            btnOkShare.setOnClickListener(v -> {
                try {
                    callUnsharedDocumentWebservice(shared_relationship_id, remove, Objects.requireNonNull(etShareMessage.getText()).toString().trim(), shared_client_id, false);
                } catch (JSONException e) {
                    throw new RuntimeException(e);
                }
                alertDialog.dismiss();
            });

            AlertDialog dialog = dialogBuilder.create();
            alertDialog = dialog;
            dialog.setView(view);
            dialog.setCanceledOnTouchOutside(false);
            dialog.show();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private void setupRecyclerView(RecyclerView
                                           recyclerView, ArrayList<SharedDocumentsDo> documentList) {
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(mcontext, LinearLayoutManager.VERTICAL, false);
        recyclerView.setLayoutManager(layoutManager);
        UnshareDocumentAdapter documentsAdapter = new UnshareDocumentAdapter(documentList, this);
        recyclerView.setAdapter(documentsAdapter);
    }


    public void callUnsharedDocumentWebservice(String id, JSONArray remove, String
            message, String clientId, boolean isRemove) throws JSONException {
        progress_dialog = AndroidUtils.get_progress(mActivity);
        JSONObject jsonObject = new JSONObject();
        JSONArray removeDoc = new JSONArray();
        JSONArray addDoc = new JSONArray();


        for (SharedDocumentsDo doc : selected_unsharedocsList) {
            JSONObject docObject = new JSONObject();
            docObject.put("docid", doc.getId());
            docObject.put("doctype", "general");
            removeDoc.put(docObject);
        }

        for (SharedDocumentsDo doc : selected_sharedocsList) {
            JSONObject docObject = new JSONObject();
            JSONArray matter_details = new JSONArray();
            if (doc.isHas_Confidential()) {
                matter_details.put(doc.getMatter_details_id());
            }
            docObject.put("docid", doc.getId());
            docObject.put("doctype", "general");
            docObject.put("matters", matter_details);
            addDoc.put(docObject);
        }

        if (remove.length() != 0) {
            if (Objects.equals(shared_tag, "byme")) {
                if (isRemove) {
                    jsonObject.put("remove", remove);
                } else {
                    jsonObject.put("remove", removeDoc);
                }
                jsonObject.put("add", addDoc);
            } else {
                jsonObject.put("remove", removeDoc);
                jsonObject.put("add", addDoc);
            }

            jsonObject.put("message", message);

            if (TAG.equals("corporate")) {
                JSONObject corporate = new JSONObject();
                corporate.put("relid", id);
                if (isRemove) {
                    corporate.put("remove", remove);
                } else {
                    corporate.put("remove", removeDoc);
                }
                corporate.put("add", addDoc);
                corporate.put("message", message);
                WebServiceHelper.callHttpWebService(this, mcontext, WebServiceHelper.RestMethodType.POST, "v3/share", "UnshareDocuments", corporate.toString());
            } else {
                WebServiceHelper.callHttpWebService(this, mcontext, WebServiceHelper.RestMethodType.PUT, "v2/relationship/" + id + "/docs/share", "UnshareDocuments", jsonObject.toString());
            }
        } else {
            AndroidUtils.showAlert("Please select at least one document", mActivity);
        }
    }


    private void callSharedDocumentsWebservice(String id, String shared_tag, MyViewHolder
            holder, String client_id) {
        progress_dialog = AndroidUtils.get_progress(mActivity);
        mholder = holder;
        shared_relationship_id = id;
        shared_client_id = client_id;
        JSONObject jsonObject = new JSONObject();
        if (TAG.equals("corporate"))
            WebServiceHelper.callHttpWebService(this, mcontext, WebServiceHelper.RestMethodType.GET, "v3/share/" + id + "/" + shared_tag, "Shared Corp Documents", jsonObject.toString());
        else
            WebServiceHelper.callHttpWebService(this, mcontext, WebServiceHelper.RestMethodType.GET, "v2/relationship/" + id + "/docs/shared/" + shared_tag, "Shared Documents", jsonObject.toString());
    }

    private void unhideProfileDetails(RelationshipsModel relationshipsModel, MyViewHolder
            holder) {
        mholder = holder;
        holder.rb_share_document.setBackgroundDrawable(mcontext.getResources().getDrawable(R.drawable.button_right_background));
        holder.rb_share_document.setTextColor(Color.BLACK);
        if (!relationshipsModel.isAccepted())
            holder.rb_profile.setBackgroundDrawable(mcontext.getResources().getDrawable(R.drawable.rectangular_button_green_count));
        else
            holder.rb_profile.setBackgroundDrawable(mcontext.getResources().getDrawable(R.drawable.button_left_green_background));
        holder.rb_profile.setTextColor(Color.WHITE);
        holder.rg_shared_status.setVisibility(GONE);
        relationshipmodel_profile = relationshipsModel;
        holder.nestedScrollView.setVisibility(GONE);
        holder.ll_documents.setVisibility(GONE);
        holder.iv_share_docs.setVisibility(GONE);
        holder.rg_document_type.setVisibility(GONE);
        callProfileWebservice(relationshipsModel.getId());
        shared_list.clear();
        shared_tag = "";
    }

    private void callProfileWebservice(String id) {
        JSONObject jsonObject = new JSONObject();
        if (TAG.equals("corporate")) {
            WebServiceHelper.callHttpWebService(this, mcontext, WebServiceHelper.RestMethodType.GET, "v3/profile/" + id, "Profile", jsonObject.toString());

        } else {
            WebServiceHelper.callHttpWebService(this, mcontext, WebServiceHelper.RestMethodType.GET, "v2/relationship/" + id + "/profile", "Profile", jsonObject.toString());
        }
    }


    @SuppressLint("SetTextI18n")
    private void deleteRelationships(RelationshipsModel relationshipsModel) {
        try {
            AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(mcontext);
            LayoutInflater inflater = mActivity.getLayoutInflater();
            View view = inflater.inflate(R.layout.delete_relationship, null);
            ImageView close_documents = view.findViewById(R.id.close_documents);
            TextView tv_confirmation = view.findViewById(R.id.tv_confirmation);
            String confirmText = "", confirmName = "";
            if (TAG.equals("deleted")) {
                confirmName = relationshipsModel.getDeletedBy();
                confirmText = "Are you sure you want to delete the relationship request sent by " + confirmName + "?";
            } else {
                confirmName = relationshipsModel.getName();
                confirmText = "Are you sure you want to delete the relationship request sent to " + confirmName + "?";
            }

            SpannableString spannable = new SpannableString(confirmText);

            spannable.setSpan(
                    new ForegroundColorSpan(mcontext.getColor(R.color.black)),
                    0,
                    confirmText.length(),
                    Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
            );

            if (confirmName != null && !confirmName.isEmpty()) {
                int start = confirmText.indexOf(confirmName);
                if (start >= 0) {
                    int end = start + confirmName.length();

                    spannable.setSpan(
                            new StyleSpan(Typeface.BOLD),
                            start,
                            end,
                            Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
                    );
                }
            }

            tv_confirmation.setText(spannable);

            TextView header_name = view.findViewById(R.id.header_name);
            header_name.setText(R.string.confirmation);
            header_name.setTextColor(mcontext.getColor(R.color.blue));
            AppCompatButton bt_yes = view.findViewById(R.id.btn_yes);
            AppCompatButton btn_no = view.findViewById(R.id.btn_No);
            btn_no.setTextColor(mcontext.getColor(R.color.white));
            bt_yes.setTextColor(mcontext.getColor(R.color.black));
            bt_yes.setBackground(mcontext.getDrawable(R.drawable.yes_button_red_button));
            btn_no.setBackground(mcontext.getDrawable(R.drawable.no_button_green_button));
            bt_yes.setBackgroundDrawable(mcontext.getDrawable(R.drawable.yes_button_red_button));
            btn_no.setBackgroundDrawable(mcontext.getDrawable(R.drawable.no_button_green_button));

            btn_no.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    ad_dialog_delete.dismiss();
                }
            });
            close_documents.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    ad_dialog_delete.dismiss();
                }
            });
            bt_yes.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (TAG.equals("deleted")) {
                        callTerminateRelationshipWebservice(relationshipsModel.getId());
                    } else {
                        callDeleteRelationshipWebservice(relationshipsModel.getId());
                    }
                    ad_dialog_delete.dismiss();
                }
            });
            final AlertDialog dialog = dialogBuilder.create();
            ad_dialog_delete = dialog;
            dialog.setView(view);
            dialog.setCanceledOnTouchOutside(false);
            dialog.show();
        } catch (Exception e) {
            AndroidUtils.showAlert(e.getMessage(), mActivity);
        }
    }

    private void callRestoreRelationshipWebservice(String id) {
        try {
            progress_dialog = AndroidUtils.get_progress(mActivity);
            JSONObject jsonObject = new JSONObject();
            WebServiceHelper.callHttpWebService(this, mcontext, WebServiceHelper.RestMethodType.POST, "v2/relationship/" + id + "/terminate/restore", "Archive Relationship", jsonObject.toString());
        } catch (Exception e) {
            AndroidUtils.showAlert(e.getMessage(), mActivity);
        }
    }

    private void callTerminateRelationshipWebservice(String id) {
        try {
            progress_dialog = AndroidUtils.get_progress(mActivity);
            JSONObject jsonObject = new JSONObject();
            WebServiceHelper.callHttpWebService(this, mcontext, WebServiceHelper.RestMethodType.DELETE, "v2/relationship/" + id + "/archive", "Delete_Relationship", jsonObject.toString());
        } catch (Exception e) {
            AndroidUtils.showAlert(e.getMessage(), mActivity);
        }
    }

    private void callDeleteRelationshipWebservice(String id) {
        try {
            progress_dialog = AndroidUtils.get_progress(mActivity);
            JSONObject jsonObject = new JSONObject();
            WebServiceHelper.callHttpWebService(this, mcontext, WebServiceHelper.RestMethodType.DELETE, "v2/relationship/" + id + "/delete", "Delete_Relationship", jsonObject.toString());
        } catch (Exception e) {
            AndroidUtils.showAlert(e.getMessage(), mActivity);
        }
    }

    private void callGroupsWebservice(MyViewHolder holder, RelationshipsModel
            relationshipsModel) {
        try {
            progress_dialog = AndroidUtils.get_progress(mActivity);
            for (int j = 0; j < relationshipsModel.getGroups().length(); j++) {
                ViewGroupModel viewGroupModel = new ViewGroupModel();
                JSONObject jsonObject = relationshipsModel.getGroups().getJSONObject(j);
                viewGroupModel.setGroup_id(jsonObject.getString("id"));
                viewGroupModel.setGroup_name(jsonObject.getString("name"));
                viewGroupModel.setCan_delete(jsonObject.optBoolean("can_delete"));
                viewGroupModel.setCan_assign_docs(jsonObject.optBoolean("can_assign_docs"));
                updatedMembersList.add(viewGroupModel);
            }
            relationshipsModel_new = relationshipsModel;
            mholder = holder;
            JSONObject postdata = new JSONObject();
            WebServiceHelper.callHttpWebService(this, mcontext, WebServiceHelper.RestMethodType.GET, "v3/groups", "Get Groups", postdata.toString());
        } catch (JSONException e) {
            e.fillInStackTrace();
        }
    }

    // FIX: callMembersWebservice now stores relationshipsModel_new before the API call
    // so that when onAsyncTaskComplete fires for "Get Members", the model is ready.
    // open_tm_popup() uses only relationshipsModel_new and updateMembersList —
    // it does NOT need mholder — so the popup works correctly regardless of scroll state.
    private void callMembersWebservice(MyViewHolder holder, RelationshipsModel relationshipsModel) {
        progress_dialog = AndroidUtils.get_progress(mActivity);
        relationshipsModel_new = relationshipsModel;
        mholder = holder;
        JSONObject postdata = new JSONObject();
        WebServiceHelper.callHttpWebService(this, mcontext, WebServiceHelper.RestMethodType.GET,
                "v2/relationship/" + relationshipsModel.getId() + "/members",
                "Get Members", postdata.toString());
    }

    @Override
    public int getItemCount() {
        return relationshipsList.size();
    }

    @Override
    public void onClick(View view) {

    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public void onAsyncTaskComplete(HttpResultDo httpResult) {
        if (progress_dialog != null && progress_dialog.isShowing()) {
            AndroidUtils.dismiss_dialog(progress_dialog);
        }
        try {
            if (httpResult.getResult() == WebServiceHelper.ServiceCallStatus.Success) {
                JSONObject result = new JSONObject(httpResult.getResponseContent());
                boolean error = result.optBoolean("error", false);
                String msg = result.optString("msg", "");
                String type = httpResult.getRequestType();

                if (type.equals("Get Groups")) {

                    if (!error) loadViewGroups(result.getJSONArray("data"));

                } else if (type.equals("Get Members")) {

                    if (!error) {
                        JSONArray members = result.optJSONArray("members");
                        if (members == null) {
                            // FIX: guard against null members array from API
                            Log.d("Get Members", "members array is null in response");
                            AndroidUtils.showAlert("No team members found for " + relationshipsModel_new.getName() + ". Add team members first.", mActivity);
                            return;
                        }
                        loadViewMembers(members);
                    } else {
                        AndroidUtils.showAlert(msg, mActivity);
                    }

                } else if (type.equals("Update Members")) {

                    if (error) {
                        AndroidUtils.showAlert(msg, clientRelationship.getActivity());
                    } else {
                        AndroidUtils.showAlert(msg, mActivity);
                    }
                    eventListener.RefreshViewRelationshipsData();

                } else if (type.equals("Update Group Access")) {

                    if (!isCancel) {
                        AndroidUtils.showAlert(msg, mActivity);
                    }
                    if (!error) {
                        groupsList.clear();
                        relationshipsList.clear();

                        if (type.equals("Update Group Access")) {
                            ad_dialog.dismiss();
                        } else {
                            ad_dialog_delete.dismiss();
                        }

                        eventListener.RefreshViewRelationshipsData();
                    }
                    clear_search();

                } else if (type.equals("Delete Relationship")) {

                    if (!error) {
                        groupsList.clear();
                        relationshipsList.clear();

                        if (type.equals("Update Group Access")) {
                            ad_dialog.dismiss();
                        } else {
                            ad_dialog_delete.dismiss();
                        }

                        eventListener.RefreshViewRelationshipsData();
                    }
                    clear_search();

                } else if (type.equals("Profile")) {

                    if (!error) {
                        mholder.rg_profile.setVisibility(VISIBLE);
                        loadProfile(result.getJSONObject("data"));
                    }
                    if (TAG.equals("individuals") && mholder.tv_individual_first_name.getText().toString().isEmpty()) {
                        callProfileWebservice(relationshipsModel_new.getId());
                    } else if (TAG.equals("individuals") && mholder.tv_first_name.getText().toString().isEmpty()) {
                        callProfileWebservice(relationshipsModel_new.getId());
                    }

                    clear_search();

                } else if (type.equals("Shared Corp Documents") ||
                        type.equals("Shared Documents")) {

                    if (!error) {
                        loadSharedwithmeDocuments(result.getJSONObject("documents"));
                        clear_search();
                    }

                } else if (type.equals("Copy Document")) {

                    ad_dialog_copy.dismiss();
                    if (error) {
                        AndroidUtils.showAlert(msg, clientRelationship.getActivity());
                    } else {
                        AndroidUtils.showAlert(msg, mActivity);
                    }
                    clear_search();

                } else if (type.equals("UnshareDocuments")) {

                    AndroidUtils.showAlert(msg, mActivity);
                    shared_list.clear();
                    selected_unsharedocsList.clear();
                    selected_sharedocsList.clear();
                    selected_firm_sharedocsList.clear();
                    selected_client_sharedocsList.clear();
                    // FIX: null-check before dismissing ad_dialog_docs
                    if (ad_dialog_docs != null && ad_dialog_docs.isShowing()) {
                        ad_dialog_docs.dismiss();
                    }
                    clear_search();
                    mholder.ll_expandable_layout.setVisibility(GONE);

                } else if (type.equals("Invite Temp Clients")) {

                    AndroidUtils.showAlert(msg, mActivity);
                    clientRelationship.callViewRelationshipWebservice("", "");
                    // FIX: null-check before dismissing ad_dialog_docs
                    if (ad_dialog_docs != null && ad_dialog_docs.isShowing()) {
                        ad_dialog_docs.dismiss();
                    }
                    clear_search();

                } else if (type.equals("View Doc With Us") ||
                        type.equals("View Other Doc")) {

                    if (!error) {
                        String url = type.equals("View Doc With Us") ?
                                result.getString("url") :
                                result.getJSONObject("data").getString("url");

                        String ReceivedFileName = "";
                        String content_type = replaceLastDotWithSlash(sharedDocumentsDo.getFilename());
                        String[] content_type1 = content_type.split("/");

                        if (content_type1.length >= 2) {
                            ReceivedFileName = content_type1[1];
                        }

                        if (ReceivedFileName.equalsIgnoreCase("apng") || ReceivedFileName.equalsIgnoreCase("avif")
                                || ReceivedFileName.equalsIgnoreCase("gif") || ReceivedFileName.equalsIgnoreCase("jpeg")
                                || ReceivedFileName.equalsIgnoreCase("png") || ReceivedFileName.equalsIgnoreCase("svg")
                                || ReceivedFileName.equalsIgnoreCase("webp") || ReceivedFileName.equalsIgnoreCase("jpg")) {

                            sharedDocumentsDo.setDoctype("image/" + ReceivedFileName);
                        } else {
                            sharedDocumentsDo.setDoctype("application/" + ReceivedFileName);
                        }

                        checkViewType(url, sharedDocumentsDo.getDoctype(), sharedDocumentsDo);
                        Log.d("TAG_Image", url);

                    } else {
                        AndroidUtils.showAlert(msg, clientRelationship.getActivity());
                    }
                    clear_search();

                } else if (type.equals("Decrypt Doc") ||
                        type.equals("Other Doc View")) {

                    if (!error) {
                        String url = result.getJSONObject("data").getString("url");
                        display_doc(url, sharedDocumentsDo);
                        Log.d("TAG_Image", url);
                    } else {
                        AndroidUtils.showAlert(msg, clientRelationship.getActivity());
                    }
                    clear_search();

                } else if (type.equals("Existing Documents")) {

                    if (!error) {
                        loadOtherDocs(result.getJSONArray("data"));
                    }
                    clear_search();

                } else if (type.equals("Remove Groups")) {

                    if (result.has("counts")) {
                        JSONObject jsonObject = result.getJSONObject("counts");
                        String doc_count = jsonObject.optString("documents");
                        int count = Integer.parseInt(doc_count);

                        ArrayList<ViewGroupModel> newGrouplist = new ArrayList<>();

                        for (int i = 0; i < groupsList.size(); i++) {
                            if (!groupsList.get(i).isChecked()) {
                                newGrouplist.add(groupsList.get(i));
                            }
                        }

                        oldGrouplist.clear();
                        for (int i = 0; i < groupsList.size(); i++) {
                            if (!groupsList.get(i).isChecked()) {
                                oldGrouplist.add(groupsList.get(i));
                            }
                        }

                        if (count > 0) {
                            GroupsAssignPopup(groupid, groupname, doc_count, newGrouplist);
                        } else {

                            for (int i = 0; i < groupsList.size(); i++) {
                                ViewGroupModel g = groupsList.get(i);

                                if (g.getId().equals(groupid)) {
                                    g.setChecked(false);
                                    g.setCan_assign_docs(false);
                                }
                            }

                            if (GroupRecyclerViewFragment.groupsAdapter != null) {
                                GroupRecyclerViewFragment.groupsAdapter.notifyDataSetChanged();
                            }

                            load_selected_groups(groupsList);
                        }
                    }
                } else if (type.equals("Alter Groups")) {
                    boolean iserror = result.optBoolean("error");
                    if (iserror) {
                        String successmsg = result.getString("msg");
                        AndroidUtils.showErrorAlert(successmsg, clientRelationship.getActivity());
                    } else {
                        String successmsg = result.getString("msg");
                        AndroidUtils.showAlert(successmsg, clientRelationship.getActivity());
                        UpdateGroups();
                    }
                } else if (type.equals("Update Groups")) {

                    boolean iserror = result.optBoolean("error");
                    if (!iserror) {
                        String updatemsg = result.getString("msg");
                        GroupRecyclerViewFragment.groupsAdapter.notifyDataSetChanged();
                        Constants.isAlterPopup = false;
                    } else {
                        AndroidUtils.showAlert(result.getString("msg"), clientRelationship.getActivity());
                    }
                } else if (type.equals("Accept Request")) {
                    boolean iserror = result.optBoolean("error");
                    if (!iserror) {
                        String successmsg = result.getString("msg");
                        AndroidUtils.showAlert(successmsg, clientRelationship.getActivity());
                        clientRelationship.callViewRelationshipWebservice("", "");
                    } else {
                        String successmsg = result.getString("msg");
                        AndroidUtils.showErrorAlert(successmsg, clientRelationship.getActivity());
                    }
                } else if (type.equals("Delete_Relationship")) {
                    boolean iserror = result.optBoolean("error");
                    if (!iserror) {
                        String successmsg = result.getString("msg");
                        AndroidUtils.showAlert(successmsg, clientRelationship.getActivity());
                        clientRelationship.callViewRelationshipWebservice("", "");
                    } else {
                        String successmsg = result.getString("msg");
                        AndroidUtils.showErrorAlert(successmsg, clientRelationship.getActivity());
                    }
                } else if (type.equals("Archive Relationship")) {
                    boolean iserror = result.optBoolean("error");
                    if (!iserror) {
                        String successmsg = result.getString("msg");
                        AndroidUtils.showAlert(successmsg, clientRelationship.getActivity());
                        clientRelationship.callViewRelationshipWebservice("", "");
                    } else {
                        String successmsg = result.getString("msg");
                        AndroidUtils.showErrorAlert(successmsg, clientRelationship.getActivity());
                    }
                }
            } else if (httpResult.getResult() == WebServiceHelper.ServiceCallStatus.Failed) {
                try {
                    JSONObject result = new JSONObject(httpResult.getResponseContent());
                    if (result.optBoolean("error")) {
                        AndroidUtils.showErrorAlert(result.optString("msg"), mActivity);
                    }
                } catch (Exception e) {
                    e.fillInStackTrace();
                }
            } else {
                AndroidUtils.showErrorAlert(httpResult.getResponseContent().toString(), mActivity);
            }
        } catch (
                JSONException e) {
            e.fillInStackTrace();
        }
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
        if (acls.length() == 0) {
            btn_Delete.setAlpha(0.5f);
            btn_Delete.setEnabled(false);
        } else {
            btn_Delete.setAlpha(1.0f);
            btn_Delete.setEnabled(true);
        }
    }


    private void GroupsAssignPopup(String group_id, String group_name, String
            doc_count, ArrayList<ViewGroupModel> groupsList1) {
        try {
            AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(mcontext);
            LayoutInflater inflater = mActivity.getLayoutInflater();
            View view = inflater.inflate(R.layout.assign_to_other_group, null);

            TextView tv_update_group = view.findViewById(R.id.tv_update_group);
            tv_update_group.setText(R.string.update_group);
            tv_update_group.setTextColor(mcontext.getColor(R.color.blue));

            btn_Delete = view.findViewById(R.id.Delete);
            btn_Delete.setText(R.string.delete);
            btn_Delete.setAlpha(0.5f);
            btn_Delete.setEnabled(false);
            TextView tv_warning_msg = view.findViewById(R.id.tv_warning_msg);
            TextView tv_assign_group = view.findViewById(R.id.tv_assign_group);
            tv_assign_group.setText(R.string.assign_to_another_active_groups);
            tv_assign_group.setTextColor(mcontext.getColor(R.color.blue));
            Constants.isAlterPopup = true;
            RecyclerView rv_groups_view = view.findViewById(R.id.rv_groups_view);

            RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(mcontext, LinearLayoutManager.VERTICAL, false);
            rv_groups_view.setLayoutManager(layoutManager);

            GroupsAdapter groupsAdapter1 = new GroupsAdapter(groupsList1, this, clientRelationship);
            rv_groups_view.setAdapter(groupsAdapter1);
            TextInputEditText et_search_members = view.findViewById(R.id.tv_search_groups);
            et_search_members.addTextChangedListener(new Validation(et_search_members));

            SpannableString groupName = new SpannableString(group_name);
            groupName.setSpan(new ForegroundColorSpan(ContextCompat.getColor(clientRelationship.getActivity(), R.color.black)), 0, groupName.length(), 0);
            groupName.setSpan(new AbsoluteSizeSpan(18, true), 0, groupName.length(), 0);
            groupName.setSpan(new StyleSpan(Typeface.BOLD), 0, groupName.length(), 0);

            SpannableString documentText = new SpannableString(doc_count + " Documents.");
            documentText.setSpan(new ForegroundColorSpan(ContextCompat.getColor(clientRelationship.getActivity(), R.color.blue)), 0, documentText.length(), 0);
            documentText.setSpan(new AbsoluteSizeSpan(18, true), 0, documentText.length(), 0);
            documentText.setSpan(new StyleSpan(Typeface.BOLD), 0, documentText.length(), 0);

            SpannableStringBuilder msgBuilder = new SpannableStringBuilder();
            msgBuilder.append("This ")
                    .append("'").append(groupName).append("'")
                    .append(" group currently contains ")
                    .append(documentText)
                    .append(". Before updating, please assign them to another active group.");

            tv_warning_msg.setText(msgBuilder, TextView.BufferType.SPANNABLE);

            AppCompatButton btn_Cancel = view.findViewById(R.id.Cancel);

            btn_Cancel.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    unCheckList(groupsList1);
                    groupsAdapter1.notifyDataSetChanged();
                    ad_dialog_delete.dismiss();
                    Constants.isAlterPopup = false;
                }
            });
            ImageView iv_close = view.findViewById(R.id.iv_close);
            iv_close.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    unCheckList(groupsList1);
                    groupsAdapter1.notifyDataSetChanged();
                    ad_dialog_delete.dismiss();
                    Constants.isAlterPopup = false;
                }
            });
            btn_Delete.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    ad_dialog_delete.dismiss();
                    AlterGroups(group_id, groupsList1);
                }
            });
            et_search_members.addTextChangedListener(new TextWatcher() {
                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {
                }

                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {

                }

                @Override
                public void afterTextChanged(Editable s) {
                    groupsAdapter1.getFilter().filter(s);
                }

            });
            groupsAdapter1.notifyDataSetChanged();
            rv_groups_view.refreshDrawableState();

            final AlertDialog dialog = dialogBuilder.create();
            ad_dialog_delete = dialog;
            dialog.setView(view);
            dialog.setCanceledOnTouchOutside(false);
            dialog.show();

        } catch (Exception e) {
            AndroidUtils.showAlert(e.getMessage(), mActivity);
        }
    }

    private void unCheckList(ArrayList<ViewGroupModel> groupsList1) {
        if (!groupsList1.isEmpty()) {
            for (int i = 0; i < groupsList1.size(); i++) {
                ViewGroupModel viewGroupModel = groupsList1.get(i);
                viewGroupModel.setChecked(false);
            }
        }
    }

    private void loadOtherDocs(JSONArray docs) throws JSONException {
        shared_list.clear();
        firm_list.clear();

        for (int i = 0; i < docs.length(); i++) {
            JSONObject docs_new = docs.getJSONObject(i);
            SharedDocumentsDo sharedDocumentsDo = createSharedDocumentsDo(docs_new);
            shared_list.add(sharedDocumentsDo);
        }

        ArrayList<SharedDocumentsDo> selectedlist = new ArrayList<>();
        for (SharedDocumentsDo sharedDoc : shared_list) {
            sharedDoc.setChecked(selected_sharedocsList.contains(sharedDoc));
            selectedlist.add(sharedDoc);
        }
        ArrayList<SharedDocumentsDo> differentList = new ArrayList<>();

        for (SharedDocumentsDo sharedDoc : selectedlist) {
            if (!shared_by_us_list.contains(sharedDoc)) {
                differentList.add(sharedDoc);
            }
        }

        Log.d("Shared_firm_list", firm_list.size() + "...." + shared_by_us_list.size());
        if (shared_tag.equals("firm") || (shared_tag.equals("client"))) {
            openSharedPopupWindow(differentList);
        } else {
            openSharedPopupWindow(shared_list);
        }
    }

    private SharedDocumentsDo createSharedDocumentsDo(JSONObject docs_new) throws JSONException {
        SharedDocumentsDo sharedDocumentsDo = new SharedDocumentsDo();

        sharedDocumentsDo.setContent_type(docs_new.optString("content_type"));
        sharedDocumentsDo.setCreated(docs_new.optString("created"));
        sharedDocumentsDo.setDescription(docs_new.optString("description"));
        sharedDocumentsDo.setExpiration_date(docs_new.optString("expiration_date"));
        sharedDocumentsDo.setFilename(docs_new.optString("filename"));
        sharedDocumentsDo.setId(docs_new.optString("id"));
        sharedDocumentsDo.setIs_disabled(docs_new.optBoolean("is_disabled"));
        sharedDocumentsDo.setIs_encrypted(docs_new.optBoolean("is_encrypted"));
        sharedDocumentsDo.setAdded_encryption(docs_new.optBoolean("added_encryption"));
        sharedDocumentsDo.setIs_password(docs_new.optBoolean("is_password"));
        sharedDocumentsDo.setName(docs_new.optString("name"));
        sharedDocumentsDo.setUploaded_by(docs_new.optString("uploaded_by"));

        if (docs_new.has("matter_details")) {
            sharedDocumentsDo.setMatter_details(docs_new.getJSONArray("matter_details"));
            JSONObject jsonObject = sharedDocumentsDo.getMatter_details().optJSONObject(0);
            if (jsonObject != null) {
                sharedDocumentsDo.setMatter_details_name(jsonObject.optString("name"));
                sharedDocumentsDo.setMatter_details_id(jsonObject.optString("id"));
                if (!sharedDocumentsDo.getMatter_details_name().isEmpty()) {
                    sharedDocumentsDo.setHas_Confidential(true);
                }
            }
        } else {
            sharedDocumentsDo.setHas_Confidential(false);
        }

        return sharedDocumentsDo;
    }


    private void loadSharedwithmeDocuments(JSONObject documents) {
        try {
            // FIX: clear shared_by_us_list once before processing all arrays,
            // not inside processDocumentArray where it gets cleared on each call.
            shared_by_us_list.clear();

            JSONArray generalArray = documents.getJSONArray("general");
            processDocumentArray(generalArray);

            processDocumentArray(documents.getJSONArray("credential"));
            processDocumentArray(documents.getJSONArray("merged"));
            processDocumentArray(documents.getJSONArray("versioned"));
            processDocumentArray(documents.getJSONArray("identity"));
            processDocumentArray(documents.getJSONArray("personal"));

            // Move highlight ID to 0th index
            if (highLightId != null && !highLightId.isEmpty()) {
                SharedDocumentsDo highlightDoc = null;
                int indexToMove = -1;

                for (int i = 0; i < shared_list.size(); i++) {
                    if (shared_list.get(i).getId().equals(highLightId)) {
                        highlightDoc = shared_list.get(i);
                        indexToMove = i;
                        break;
                    }
                }

                if (highlightDoc != null && indexToMove != -1) {
                    shared_list.remove(indexToMove);
                    shared_list.add(0, highlightDoc);
                }
            }

            if (!doc_nature.equals("share_doc")) {
                ArrayList<SharedDocumentsDo> unselectedlist = new ArrayList<>();
                for (SharedDocumentsDo sharedDoc : shared_list) {
                    sharedDoc.setChecked(selected_unsharedocsList.contains(sharedDoc));
                    unselectedlist.add(sharedDoc);
                }
                openSharedPopupWindow(unselectedlist);
            }

        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    // FIX: removed the shared_by_us_list.clear() from inside this method.
    // The clear now happens once in loadSharedwithmeDocuments before calling this
    // method 6 times. Previously each call wiped the list so only the last
    // array's documents ended up in shared_by_us_list.
    private void processDocumentArray(JSONArray documentArray) {
        try {
            for (int i = 0; i < documentArray.length(); i++) {
                JSONObject documentObj = documentArray.getJSONObject(i);
                SharedDocumentsDo sharedDocumentsDo = new SharedDocumentsDo();

                sharedDocumentsDo.setContent_type(documentObj.optString("content_type"));
                sharedDocumentsDo.setCreated(documentObj.optString("created"));
                sharedDocumentsDo.setDescription(documentObj.optString("description"));
                sharedDocumentsDo.setExpiration_date(documentObj.optString("expiration_date"));
                sharedDocumentsDo.setFilename(documentObj.optString("filename"));
                sharedDocumentsDo.setId(documentObj.optString("id"));
                sharedDocumentsDo.setIs_disabled(documentObj.optBoolean("is_disabled"));
                sharedDocumentsDo.setIs_encrypted(documentObj.optBoolean("is_encrypted"));
                sharedDocumentsDo.setAdded_encryption(documentObj.optBoolean("added_encryption"));
                sharedDocumentsDo.setIs_password(documentObj.optBoolean("is_password"));
                sharedDocumentsDo.setName(documentObj.optString("name"));
                sharedDocumentsDo.setUploaded_by(documentObj.optString("uploaded_by"));

                if (documentObj.has("matter_details")) {
                    sharedDocumentsDo.setMatter_details(documentObj.getJSONArray("matter_details"));
                    JSONObject jsonObject = sharedDocumentsDo.getMatter_details().optJSONObject(0);
                    if (jsonObject != null) {
                        sharedDocumentsDo.setMatter_details_name(jsonObject.optString("name"));
                        sharedDocumentsDo.setMatter_details_id(jsonObject.optString("id"));
                        if (!sharedDocumentsDo.getMatter_details_name().isEmpty()) {
                            sharedDocumentsDo.setHas_Confidential(true);
                        }
                    }
                } else {
                    sharedDocumentsDo.setHas_Confidential(false);
                }
                shared_list.add(sharedDocumentsDo);
            }
            shared_by_us_list.addAll(shared_list);
            Log.d("Shared_by_us_list", "" + shared_by_us_list.size());
        } catch (JSONException e) {
            e.fillInStackTrace();
        }
    }


    private void DocArray(JSONObject general) {
    }

    private void DocArray(JSONArray jsonArray, String arrayName) throws JSONException {
        for (int i = 0; i < jsonArray.length(); i++) {
            JSONObject object = jsonArray.getJSONObject(i);
            System.out.println("Parsed from " + arrayName + ": " + object.toString());
        }
    }


    private void loadDocumentsRecyclerview() {
        if (shared_list.isEmpty()) {
            AndroidUtils.showAlert("No Documents to display", mActivity);
            mholder.et_Search.setVisibility(GONE);
        } else {
            mholder.rv_documents.removeAllViews();
            mholder.rv_documents.setLayoutManager(new GridLayoutManager(mcontext, 1));

            SharedDocumentsAdapter adapter = new SharedDocumentsAdapter(shared_list, shared_tag, mcontext, this, shared_relationship_id, shared_client_id, mActivity, this, highLightId);
            mholder.rv_documents.setAdapter(adapter);

            mholder.et_Search.addTextChangedListener(new TextWatcher() {
                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {

                }

                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                }

                @Override
                public void afterTextChanged(Editable s) {
                    adapter.getFilter().filter(mholder.et_Search.getText().toString().trim());
                }

            });
        }
    }

    private void loadProfile(JSONObject data) throws JSONException {
        try {
            ProfileDo profileDo = new ProfileDo();
            if (!TAG.equals("individuals")) {
                profileDo.setFullname(data.optString("fullname"));
                profileDo.setUid(data.optString("uid"));
                profileDo.setContact_person(data.optString("contact_person"));
                profileDo.setContact_phone(data.optString("contact_phone"));
                profileDo.setEmail(data.optString("email"));
                profileDo.setContact_phone(data.optString("contact_phone"));
                profileDo.setEmail(data.optString("email"));
                profileDo.setWebsite(data.optString("website"));
                if (data.has("address")) {
                    JSONObject address = data.getJSONObject("address");
                    profileDo.setCountry(address.optString("country"));
                    profileDo.setAddress(data.optJSONObject("address"));
                    profileDo.setHouse_flat_no(address.optString("house_flat_no"));
                    profileDo.setStreet(address.optString("street"));
                    profileDo.setCity_town(address.optString("city_town"));
                    profileDo.setState(address.optString("state"));
                    profileDo.setZipcode(address.optString("zipcode"));
                }
            } else {
                profileDo.setFirst_name(data.optString("first_name"));
                profileDo.setLast_name(data.optString("last_name"));
                profileDo.setMiddle_name(data.optString("middle_name"));
                profileDo.setDob(data.optString("dob"));
                profileDo.setEmail(data.optString("email"));
                profileDo.setMobile(data.optString("mobile"));
                profileDo.setCitizen(data.optJSONArray("citizen"));
                JSONArray citizen = data.optJSONArray("citizen");
                profileDo.setWebsite(data.optString("website"));
                for (int i = 0; i < Objects.requireNonNull(citizen).length(); i++) {
                    JSONObject jsonObject = citizen.getJSONObject(i);
                    if (jsonObject.getString("index").equals("citizen_primary")) {
                        profileDo.setIndex(jsonObject.optString("index"));
                        profileDo.setCountry(jsonObject.optString("country"));
                        profileDo.setAffiliation_type(jsonObject.optString("work_address"));
                        profileDo.setHome_address(jsonObject.optString("home_address"));
                        profileDo.setWork_phone(jsonObject.optString("work_phone"));
                        profileDo.setAlt_phone(jsonObject.optString("alt_phone"));
                        break;
                    }
                }
            }

            FLAG = "second_click";

            if (TAG.equals("individuals")) {
                mholder.ll_individual_profile.setVisibility(VISIBLE);
                mholder.ll_entity_profile.setVisibility(GONE);
                mholder.tv_individual_first_name.setText(profileDo.getFirst_name());
                mholder.tv_individual_last_name.setText(profileDo.getLast_name());
                mholder.tv_individual_email.setText(profileDo.getEmail());
                if (!profileDo.getMobile().equals("null")) {
                    mholder.tv_individual_mobile.setText(profileDo.getMobile());
                } else {
                    mholder.tv_individual_mobile.setText("");
                }
                mholder.tv_individual_country.setText(profileDo.getCountry());
                mholder.tv_individual_home_address.setText(profileDo.getHome_address());
                mholder.tv_individual_work_phone.setText(profileDo.getWork_phone());
                mholder.tv_individual_alt_phone.setText(profileDo.getAlt_phone());
            } else {
                mholder.ll_entity_profile.setVisibility(VISIBLE);
                mholder.ll_individual_profile.setVisibility(GONE);

                mholder.tv_first_name.setText(profileDo.getFullname());
                mholder.tv_email.setText(profileDo.getEmail());
                mholder.tv_country.setText(profileDo.getCountry());
                if (!profileDo.getContact_phone().equals("null")) {
                    mholder.tv_individual_mobile.setText(profileDo.getContact_phone());
                } else {
                    mholder.tv_individual_mobile.setText("");
                }
                mholder.tv_contact_name.setText(profileDo.getContact_person());
                mholder.tv_mobile.setText(profileDo.getContact_phone());
                mholder.tv_website.setText("");
                mholder.tv_billing_currency.setText("");
            }
            mholder.cv_Profile.setVisibility(VISIBLE);
        } catch (JSONException e) {
            e.fillInStackTrace();
            AndroidUtils.showAlert(e.getMessage(), mActivity);
        }
    }

    public void checkRemoveGroups(String groups_id, String
            groupname, ArrayList<ViewGroupModel> groupsList) {
        try {
            this.groupname = groupname;
            this.groupid = groups_id;
            groupsList1 = groupsList;
            progress_dialog = AndroidUtils.get_progress(mActivity);
            JSONObject jsonObject = new JSONObject();
            WebServiceHelper.callHttpWebService(this, mcontext, WebServiceHelper.RestMethodType.GET, "v3/relationship/groups/" + relationshipsModel_new.getClient_id() + "/" + groups_id, "Remove Groups", jsonObject.toString());
        } catch (Exception e) {
            AndroidUtils.showAlert(e.getMessage(), mActivity);
        }
    }

    public void UpdateGroups() {
        try {
            progress_dialog = AndroidUtils.get_progress(mActivity);
            JSONObject jsonObject = new JSONObject();
            if (!groupsList.isEmpty()) {
                for (int i = 0; i < groupsList.size(); i++) {
                    ViewGroupModel viewGroupModel = groupsList.get(i);
                    if (viewGroupModel.isChecked()) {
                        if (viewGroupModel.getId().equals(groupid)) {
                            groupsList.get(i).setChecked(false);
                            viewGroupModel.setCan_assign_docs(false);
                            this.acls.remove(i);
                        } else {
                            this.acls.put(viewGroupModel.getId());
                            viewGroupModel.setCan_assign_docs(true);
                            groupsList.get(i).setChecked(true);
                        }
                    }
                }
            }
            GroupRecyclerViewFragment.groupsAdapter.notifyDataSetChanged();
            load_selected_groups(groupsList);
            jsonObject.put("acls", this.acls);
            if (TAG.equals("corporate")) {
                WebServiceHelper.callHttpWebService(this, mcontext, WebServiceHelper.RestMethodType.PATCH, "v3/acl/" + relationshipsModel_new.getId() + "/update", "Update Groups", jsonObject.toString());
            } else {
                WebServiceHelper.callHttpWebService(this, mcontext, WebServiceHelper.RestMethodType.PUT, "v2/relationship/" + relationshipsModel_new.getId() + "/acls", "Update Groups", jsonObject.toString());
            }

        } catch (Exception e) {
            AndroidUtils.showAlert(e.getMessage(), mActivity);
        }
    }

    public void AlterGroups(String groups_id, ArrayList<ViewGroupModel> groupsList) {
        try {
            progress_dialog = AndroidUtils.get_progress(mActivity);
            JSONObject jsonObject = new JSONObject();
            if (!groupsList.isEmpty()) {
                for (int i = 0; i < groupsList.size(); i++) {
                    ViewGroupModel viewGroupModel = groupsList.get(i);
                    if (viewGroupModel.isChecked()) {
                        acls.put(viewGroupModel.getId());
                    }
                }
            }
            jsonObject.put("new_groups", acls);
            WebServiceHelper.callHttpWebService(this, mcontext, WebServiceHelper.RestMethodType.PATCH, "v3/relationship/groups/" + relationshipsModel_new.getClient_id() + "/" + groups_id, "Alter Groups", jsonObject.toString());
        } catch (Exception e) {
            AndroidUtils.showAlert(e.getMessage(), mActivity);
        }
    }

    private void callAcceptRequest(String id) {
        try {
            progress_dialog = AndroidUtils.get_progress(mActivity);
            JSONObject jsonObject = new JSONObject();
            if (TAG.equals("corporate")) {
                jsonObject.put("response", "yes");
                WebServiceHelper.callHttpWebService(this, mcontext, WebServiceHelper.RestMethodType.PUT, "v3/corporate/" + id + "/accept", "Accept Request", jsonObject.toString());
            } else
                WebServiceHelper.callHttpWebService(this, mcontext, WebServiceHelper.RestMethodType.POST, "v2/relationship/" + id + "/accept", "Accept Request", jsonObject.toString());
        } catch (Exception e) {
            AndroidUtils.showAlert(e.getMessage(), mActivity);
        }
    }

    public void loadSelectedNewGroups(ArrayList<ViewGroupModel> list_item) {
        if (!list_item.isEmpty()) {
            for (int i = 0; i < list_item.size(); i++) {
                ViewGroupModel viewGroupModel = list_item.get(i);
                if (viewGroupModel.isChecked()) {
                    new_groups.put(viewGroupModel.getId());
                }
            }
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
            isUpdated.set(false);
        } else {
            btn_send_request.setAlpha(1.0f);
            btn_send_request.setEnabled(true);
            isUpdated.set(true);
        }
    }

    private void loadViewMembers(JSONArray members) throws JSONException {
        try {
            selectedMembersList.clear();
            for (int i = 0; i < relationshipsModel_new.getMembersList().length(); i++) {
                JSONObject jsonObject = relationshipsModel_new.getMembersList().getJSONObject(i);
                MemberModel selected = new MemberModel();
                selected.setId(jsonObject.getString("id"));
                selected.setName(jsonObject.getString("name"));
                selectedMembersList.add(selected);
            }

            updateMembersList.clear();
            for (int j = 0; j < members.length(); j++) {
                JSONObject jsonObject = members.getJSONObject(j);
                String id = jsonObject.getString("id");
                String name = jsonObject.getString("name");

                MemberModel member = new MemberModel();
                member.setId(id);
                member.setName(name);
                member.setChecked(false);

                for (MemberModel selected : selectedMembersList) {
                    if (selected.getId().equals(id)) {
                        member.setChecked(true);
                        break;
                    }
                }

                updateMembersList.add(member);
            }
            // FIX: improved empty state message — tells user exactly what to do
            if (!updateMembersList.isEmpty()) {
                open_tm_popup();
            } else {
                AndroidUtils.showAlert(
                        "No team members found for " + relationshipsModel_new.getName() +
                                ". Please add team members to this group first.",
                        mActivity
                );
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private void loadViewGroups(JSONArray data) throws JSONException {
        ViewGroupModel viewGroupModel;
        groupsList.clear();
        for (int i = 0; i < data.length(); i++) {
            JSONObject jsonObject = data.getJSONObject(i);
            viewGroupModel = new ViewGroupModel();
            viewGroupModel.setId(jsonObject.getString("id"));
            String date = jsonObject.getString("created");
            Date date_new = AndroidUtils.stringToDateTimeDefault(date, "yyyy-MM-dd'T'HH:mm:ss.SSS");
            String created = AndroidUtils.getDateToString(date_new, "MMM dd YYYY");
            viewGroupModel.setCreated(created);
            JSONArray members = jsonObject.getJSONArray("members");
            viewGroupModel.setMembers(members);
            viewGroupModel.setDescription(jsonObject.getString("description"));
            viewGroupModel.setName(jsonObject.getString("name"));
            JSONObject group_head = jsonObject.getJSONObject("groupHead");
            viewGroupModel.setGroup_head_id(group_head.getString("id"));
            viewGroupModel.setGroup_head_name(group_head.getString("name"));
            viewGroupModel.setOwner_name(group_head.getString("name"));
            for (int j = 0; j < data.length(); j++) {
                for (int k = 0; k < updatedMembersList.size(); k++) {
                    if (viewGroupModel.getId().matches(updatedMembersList.get(k).getGroup_id())) {
                        viewGroupModel.setChecked(true);
                        if (updatedMembersList.get(k).isCan_assign_docs()) {
                            viewGroupModel.setCan_assign_docs(true);
                        } else {
                            viewGroupModel.setCan_assign_docs(false);
                        }
                        if (!updatedMembersList.get(k).isCan_delete()) {
                            viewGroupModel.setCan_delete(false);
                        } else {
                            viewGroupModel.setCan_delete(true);
                        }
                    }
                }
            }
            if ((!jsonObject.getString("name").equals("AAM")) && ((!jsonObject.getString("name").equals("SuperUser"))))
                groupsList.add(viewGroupModel);
        }
        Log.i("ArrayList", "info" + updatedMembersList.size());
        String mtag = "VG";
        loadGroupsRecylerview();
    }

    private void loadGroupsRecylerview() {
        clientRelationship.ll_view_rel.setEnabled(false);
        clientRelationship.ll_view_rel.setClickable(false);
        clientRelationship.ll_view_rel.setAlpha(0.5f);
        Constants.mainActivity.Add_Page(new GroupRecyclerViewFragment(mcontext, groupsList, clientRelationship, relationshipsModel_new, this));
    }

    public void callUpdateGroupAccess(String id, ArrayList<ViewGroupModel> list_item,
                                      boolean isCancel) throws JSONException {
        this.isCancel = isCancel;
        if (isCancel) {
            if (isUpdated.get()) {
                ConfirmPopup(relationshipsModel_new.getName(), relationshipsModel_new.getId(), true, true, list_item);
            } else {
                Constants.mainActivity.Remove_Page(new GroupRecyclerViewFragment(mcontext, groupsList, clientRelationship, relationshipsModel_new, this));
                clientRelationship.ll_view_rel.setEnabled(true);
                clientRelationship.ll_view_rel.setAlpha(1.0f);
                et_search_groups.setText("");
                groupsList.clear();
                popupView.setVisibility(GONE);
            }
        } else {
            if (isUpdated.get()) {
                ConfirmPopup(relationshipsModel_new.getName(), relationshipsModel_new.getId(), false, true, list_item);
            } else {
                Constants.mainActivity.Remove_Page(new GroupRecyclerViewFragment(mcontext, groupsList, clientRelationship, relationshipsModel_new, this));
                callUpdateGroups(id, list_item);
                clientRelationship.ll_view_rel.setEnabled(true);
                clientRelationship.ll_view_rel.setAlpha(1.0f);
                et_search_groups.setText("");
                groupsList.clear();
                popupView.setVisibility(GONE);
            }
        }
    }

    public void callUpdateGroups(String id, ArrayList<ViewGroupModel> list_item) {
        try {
            progress_dialog = AndroidUtils.get_progress(mActivity);
            JSONObject postData = new JSONObject();
            JSONArray acls = new JSONArray();
            if (!list_item.isEmpty()) {
                for (int i = 0; i < list_item.size(); i++) {
                    ViewGroupModel viewGroupModel = list_item.get(i);
                    if (viewGroupModel.isChecked()) {
                        acls.put(viewGroupModel.getId());
                    }
                }
                postData.put("acls", acls);
                if (TAG.equals("corporate")) {
                    WebServiceHelper.callHttpWebService(this, mcontext, WebServiceHelper.RestMethodType.PATCH, "v3/acl/" + id + "/update", "Update Group Access", postData.toString());
                } else {
                    WebServiceHelper.callHttpWebService(this, mcontext, WebServiceHelper.RestMethodType.PUT, "v2/relationship/" + id + "/acls", "Update Group Access", postData.toString());
                }
                // FIX: removed the premature clientRelationship.callViewRelationshipWebservice() call
                // that was here before. The refresh is now done inside onAsyncTaskComplete
                // when the "Update Group Access" response arrives, so the list reflects
                // the actual saved state rather than stale data.
            } else {
                AndroidUtils.showAlert("No Groups Available", mActivity);
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public void setSelected_sharedocsList(ArrayList<SharedDocumentsDo> list_item) {
        selected_sharedocsList.clear();
        if (shared_tag.equals("client")) {
            selected_client_sharedocsList.clear();
            for (int i = 0; i < list_item.size(); i++) {
                SharedDocumentsDo groupModel = list_item.get(i);
                if (groupModel.isChecked()) {
                    selected_client_sharedocsList.add(groupModel);
                }
            }
        } else if (shared_tag.equals("firm")) {
            selected_firm_sharedocsList.clear();
            for (int i = 0; i < list_item.size(); i++) {
                SharedDocumentsDo groupModel = list_item.get(i);
                if (groupModel.isChecked()) {
                    selected_firm_sharedocsList.add(groupModel);
                }
            }
        }
        selected_sharedocsList.addAll(selected_client_sharedocsList);
        selected_sharedocsList.addAll(selected_firm_sharedocsList);
        Log.d("selected_sharedocsList", "" + selected_sharedocsList.size());
    }

    public void setSelected_unsharedocsList(ArrayList<SharedDocumentsDo> list_item) {
        selected_unsharedocsList.clear();
        for (int i = 0; i < list_item.size(); i++) {
            SharedDocumentsDo groupModel = list_item.get(i);
            if (groupModel.isChecked()) {
                selected_unsharedocsList.add(groupModel);
            }
        }
        Log.d("selected_unsharedocsList", "" + selected_unsharedocsList.size());
    }

    public class MyViewHolder extends RecyclerView.ViewHolder {
        TextView deletedBy, tv_relationship_name, tv_created_date, tv_consumer, tv_initiated, tv_first_name, tv_email, tv_country, tv_contact_name, tv_mobile, tv_website, tv_billing_currency;
        ImageView iv_restore_relationships, iv_initiated, iv_activate_relationships, iv_groups_relationships, iv_tm_relationships, iv_delete_relationships, iv_send_invite;
        LinearLayout rg_profile, rg_shared_status, rg_shared_type, rg_document_type, ll_icons;
        CardView cv_Profile, cv_relationships;
        Button btn_accept;
        TextView email, first_name, country, contact_name, mobile, website, billing_currency, individual_last_name, individual_first_name, individual_email, individual_mobile, individual_country, individual_home_address, individual_work_phone, individual_alt_phone, tv_individual_first_name, tv_individual_last_name, tv_individual_email, tv_individual_mobile, tv_individual_country, tv_individual_home_address, tv_individual_work_phone, tv_individual_alt_phone;
        LinearLayout ll_documents, ll_expandable_layout, nestedScrollView, ll_shared_with_us, ll_doc_button, ll_individual_profile, ll_entity_profile;
        RecyclerView rv_documents, rv_shared_with_us;
        CardView cv_relationships_details;
        TextView rb_share_document, tv_more_details, rb_profile, rb_shared_by_us, rb_shared_with_us, rb_client_document, rb_firm_document;
        TextInputEditText et_Search, et_search_relationships;
        com.google.android.material.imageview.ShapeableImageView iv_share_docs;
        ImageView custom_spinner_cardview;
        CardView action_list_card;
        ListView sp_action;

        public MyViewHolder(@NonNull View itemView) {
            super(itemView);
            tv_relationship_name = itemView.findViewById(R.id.tv_relationship_name);
            tv_relationship_name.setTextSize(DynamicUtils.twenty);
            iv_send_invite = itemView.findViewById(R.id.iv_send_invite);
            deletedBy = itemView.findViewById(R.id.deletedBy);
            iv_restore_relationships = itemView.findViewById(R.id.iv_restore_relationships);
            tv_created_date = itemView.findViewById(R.id.tv_created_date);
            tv_consumer = itemView.findViewById(R.id.tv_consumer);
            tv_more_details = itemView.findViewById(R.id.tv_more_details);
            tv_more_details.setVisibility(GONE);
            ll_icons = itemView.findViewById(R.id.ll_icons);
            email = itemView.findViewById(R.id.email);
            email.setText(R.string.email__);
            first_name = itemView.findViewById(R.id.first_name);
            first_name.setText(R.string.first_name__);
            country = itemView.findViewById(R.id.country);
            country.setText(R.string.country__);
            contact_name = itemView.findViewById(R.id.contact_name);
            contact_name.setText(R.string.contact_name__);
            mobile = itemView.findViewById(R.id.mobile);
            mobile.setText(R.string.mobile__);
            website = itemView.findViewById(R.id.website);
            website.setText(R.string.website__);
            billing_currency = itemView.findViewById(R.id.billing_currency);
            billing_currency.setText(R.string.billing_currency__);
            tv_initiated = itemView.findViewById(R.id.tv_initiated);
            tv_initiated.setTextSize(13);

            iv_initiated = itemView.findViewById(R.id.iv_initiated);
            iv_groups_relationships = itemView.findViewById(R.id.iv_groups_relationships);
            iv_groups_relationships.setVisibility(GONE);
            iv_activate_relationships = itemView.findViewById(R.id.iv_activate_relationships);
            iv_activate_relationships.setVisibility(GONE);
            iv_tm_relationships = itemView.findViewById(R.id.iv_tm_relationships);
            iv_delete_relationships = itemView.findViewById(R.id.iv_delete_relationships);
            iv_delete_relationships.setColorFilter(mcontext.getColor(R.color.blue));
            iv_delete_relationships.setImageTintList(ColorStateList.valueOf(mcontext.getColor(R.color.blue)));
            iv_delete_relationships.setVisibility(VISIBLE);
            rg_profile = itemView.findViewById(R.id.profile);
            rg_shared_status = itemView.findViewById(R.id.shared_status);
            rg_shared_type = itemView.findViewById(R.id.shared_type);
            ll_shared_with_us = itemView.findViewById(R.id.ll_shared_with_us);
            rv_shared_with_us = ll_shared_with_us.findViewById(R.id.rv_doc_share);
            ll_doc_button = ll_shared_with_us.findViewById(R.id.ll_doc_button);
            rg_document_type = itemView.findViewById(R.id.document_type);
            tv_first_name = itemView.findViewById(R.id.tv_first_name);
            tv_email = itemView.findViewById(R.id.tv_email);
            tv_country = itemView.findViewById(R.id.tv_country);
            tv_contact_name = itemView.findViewById(R.id.tv_contact_name);
            tv_mobile = itemView.findViewById(R.id.tv_mobile);
            tv_website = itemView.findViewById(R.id.tv_website);
            tv_billing_currency = itemView.findViewById(R.id.tv_billing_currency);
            cv_Profile = itemView.findViewById(R.id.cv_profile);
            ll_individual_profile = itemView.findViewById(R.id.ll_individual_profile);
            custom_spinner_cardview = itemView.findViewById(R.id.custom_spinner_cardview);
            action_list_card = itemView.findViewById(R.id.action_list_card);
            sp_action = itemView.findViewById(R.id.list_client);

            individual_first_name = itemView.findViewById(R.id.individual_first_name);
            individual_first_name.setText(R.string.first_name__);
            individual_last_name = itemView.findViewById(R.id.individual_last_name);
            individual_last_name.setText(R.string.last_name__);
            individual_mobile = itemView.findViewById(R.id.individual_mobile);
            individual_mobile.setText(R.string.mobile__);
            individual_email = itemView.findViewById(R.id.individual_email);
            individual_email.setText(R.string.email__);
            individual_country = itemView.findViewById(R.id.individual_country);
            individual_country.setText(R.string.country__);
            individual_home_address = itemView.findViewById(R.id.individual_home_address);
            individual_home_address.setText(R.string.home_address);
            individual_work_phone = itemView.findViewById(R.id.individual_work_phone);
            individual_work_phone.setText(R.string.work_phone);
            individual_alt_phone = itemView.findViewById(R.id.individual_alt_phone);
            individual_alt_phone.setText(R.string.alt_phone);
            tv_individual_first_name = itemView.findViewById(R.id.tv_individual_first_name);
            tv_individual_last_name = itemView.findViewById(R.id.tv_individual_last_name);
            tv_individual_email = itemView.findViewById(R.id.tv_individual_email);
            tv_individual_mobile = itemView.findViewById(R.id.tv_individual_mobile);
            tv_individual_country = itemView.findViewById(R.id.tv_individual_country);
            tv_individual_home_address = itemView.findViewById(R.id.tv_individual_home_address);
            tv_individual_work_phone = itemView.findViewById(R.id.tv_individual_work_phone);
            tv_individual_alt_phone = itemView.findViewById(R.id.tv_individual_alt_phone);
            ll_entity_profile = itemView.findViewById(R.id.ll_entity_profile);
            btn_accept = itemView.findViewById(R.id.accept);
            btn_accept.setBackgroundDrawable(mcontext.getDrawable(R.drawable.no_button_green_button));
            btn_accept.setText(R.string.accept);
            rb_profile = itemView.findViewById(R.id.rb_profile);
            rb_profile.setText(R.string.profile);
            et_search_relationships = itemView.findViewById(R.id.et_search_relationships);
            et_search_relationships.setHint(R.string.search_documents);
            et_search_relationships.setVisibility(GONE);
            et_search_relationships.addTextChangedListener(new Validation(et_search_relationships));
            iv_share_docs = itemView.findViewById(R.id.iv_share_docs);
            rb_share_document = itemView.findViewById(R.id.rb_share_button);
            rb_share_document.setText(R.string.share_document);
            rb_shared_by_us = itemView.findViewById(R.id.rb_shared_by_us);
            rb_shared_by_us.setText(R.string.shared_by_us);
            rb_shared_with_us = itemView.findViewById(R.id.rb_shared_with_us);
            rb_shared_with_us.setText(R.string.shared_with_us);
            nestedScrollView = itemView.findViewById(R.id.nestedScrollView);
            ll_documents = itemView.findViewById(R.id.ll_documents);
            rv_documents = itemView.findViewById(R.id.rv_shared_documents);
            cv_relationships_details = itemView.findViewById(R.id.cv_relationships_details);

            et_Search = itemView.findViewById(R.id.et_search_documents);
            et_Search.addTextChangedListener(new Validation(et_Search));
            et_Search.setHint(R.string.search_documents);
            et_Search.setVisibility(GONE);
            ll_expandable_layout = itemView.findViewById(R.id.ll_expandable_layout);
            rb_client_document = itemView.findViewById(R.id.rb_client_document);
            rb_client_document.setText(R.string.client_document);
            rb_firm_document = itemView.findViewById(R.id.rb_firm_document);
            rb_firm_document.setText(R.string.firm_document);
        }
    }

    public void check_select_all(boolean check_status) {
        chk_select_all.setChecked(check_status);
    }
}