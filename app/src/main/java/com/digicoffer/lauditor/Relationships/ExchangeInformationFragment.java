package com.digicoffer.lauditor.Relationships;

import static android.view.View.GONE;
import static android.view.View.VISIBLE;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.Dialog;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatButton;
import androidx.cardview.widget.CardView;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.digicoffer.lauditor.Relationships.Adapter.RelationshipsAdapter;
import com.digicoffer.lauditor.Relationships.Adapter.SharedDocumentsAdapter;
import com.digicoffer.lauditor.Relationships.Adapter.UnshareDocumentAdapter;
import com.digicoffer.lauditor.Relationships.Model.ProfileDo;
import com.digicoffer.lauditor.Relationships.Model.SharedDocumentsDo;
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
import com.github.barteksc.pdfviewer.PDFView;
import com.google.android.material.textfield.TextInputEditText;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Objects;

/**
 * ExchangeInformationFragment
 * <p>
 * Drop-in Fragment replacement for ExchangeInformationActivity.
 * <p>
 * Usage – create with the factory method:
 * <p>
 * ExchangeInformationFragment fragment = ExchangeInformationFragment.newInstance(
 * relationshipId, relationshipName, tag, clientId, isAccepted, groupsJsonString);
 * <p>
 * getSupportFragmentManager()
 * .beginTransaction()
 * .replace(R.id.fragment_container, fragment)
 * .addToBackStack(null)
 * .commit();
 */
public class ExchangeInformationFragment extends Fragment
        implements AsyncTaskCompleteListener,
        SharedDocumentsAdapter.EventListener {


    public static final String ARG_RELATIONSHIP_ID = "rel_id";
    public static final String ARG_RELATIONSHIP_NAME = "rel_name";
    public static final String ARG_TAG = "tag";
    public static final String ARG_CLIENT_ID = "client_id";
    public static final String ARG_IS_ACCEPTED = "is_accepted";
    public static final String ARG_GROUPS_JSON = "groups_json";

    /* ─── Views – Profile ────────────────────────────────────────── */
    private TextView tv_name, tv_email_val, tv_contact_val,
            tv_country_val, tv_phone_val, tv_address_val, tv_website_val;
    private LinearLayout ll_individual_profile, ll_entity_profile;
    private TextView tv_ind_fname, tv_ind_lname, tv_ind_email,
            tv_ind_mobile, tv_ind_country, tv_ind_home, tv_ind_work, tv_ind_alt;

    /* ─── Hide / Show details toggle ────────────────────────────── */
    private TextView tv_toggle_details;
    private LinearLayout ll_profile_details;

    /* ─── Shared docs section ────────────────────────────────────── */
    private ImageView btn_share, iv_dropdown;
    private CardView share_card;
    private TextView rb_share_with_me, rb_share_by_me;
    private LinearLayout ll_doc_type_row;
    private TextView rb_client_doc, rb_firm_doc;
    private RecyclerView rv_shared_docs;
    private TextView tv_no_docs;

    /* ─── Share By Me bottom action bar ─────────────────────────── */
    private LinearLayout ll_share_byme_actions;
    private AppCompatButton btn_action_cancel, btn_action_share;

    /* ─── Progress ───────────────────────────────────────────────── */
    private Dialog progress_dialog;

    /* ─── State ──────────────────────────────────────────────────── */
    private String relationshipId = "";
    private String relationshipName = "";
    private String TAG_rel = "";
    private String clientId = "";
    private boolean isAccepted = false;
    private JSONArray groupsArray = new JSONArray();

    private String shared_tag = "withme";
    private String doc_nature = "withme";
    private boolean isProfileExpanded = true;

    private SharedDocumentsDo currentDocModel = null;

    private ArrayList<SharedDocumentsDo> shared_list = new ArrayList<>();
    private ArrayList<SharedDocumentsDo> shared_by_us_list = new ArrayList<>();
    private ArrayList<SharedDocumentsDo> selected_sharedocsList = new ArrayList<>();
    private ArrayList<SharedDocumentsDo> selected_unsharedocsList = new ArrayList<>();
    private ArrayList<SharedDocumentsDo> selected_client_list = new ArrayList<>();
    private ArrayList<SharedDocumentsDo> selected_firm_list = new ArrayList<>();

    private AlertDialog ad_dialog_docs, ad_dialog_copy, alertDialog_confirm;
    private CheckBox chk_select_all_ref;
    private SharedDocumentsAdapter currentSharedDocsAdapter = null;

    /* ══════════════════════════════════════════════════════════════
       FACTORY METHOD
       ══════════════════════════════════════════════════════════════ */
    public static ExchangeInformationFragment newInstance(
            String relationshipId,
            String relationshipName,
            String tag,
            String clientId,
            boolean isAccepted,
            String groupsJson) {

        Bundle args = new Bundle();
        args.putString(ARG_RELATIONSHIP_ID, relationshipId);
        args.putString(ARG_RELATIONSHIP_NAME, relationshipName);
        args.putString(ARG_TAG, tag);
        args.putString(ARG_CLIENT_ID, clientId);
        args.putBoolean(ARG_IS_ACCEPTED, isAccepted);
        args.putString(ARG_GROUPS_JSON, groupsJson);

        ExchangeInformationFragment fragment = new ExchangeInformationFragment();
        fragment.setArguments(args);
        return fragment;
    }

    /* ══════════════════════════════════════════════════════════════
       LIFECYCLE
       ══════════════════════════════════════════════════════════════ */
    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        /* ── Read arguments (replaces getIntent().getStringExtra()) ── */
        Bundle args = getArguments();
        if (args != null) {
            relationshipId = args.getString(ARG_RELATIONSHIP_ID, "");
            relationshipName = args.getString(ARG_RELATIONSHIP_NAME, "");
            TAG_rel = args.getString(ARG_TAG, "");
            clientId = args.getString(ARG_CLIENT_ID, "");
            isAccepted = args.getBoolean(ARG_IS_ACCEPTED, false);
            String groupsJson = args.getString(ARG_GROUPS_JSON, "");
            try {
                if (groupsJson != null && !groupsJson.isEmpty())
                    groupsArray = new JSONArray(groupsJson);
            } catch (JSONException ignored) {
            }
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.activity_exchange_information, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        /* ── Custom header back button ── */
        ImageView btnBack = view.findViewById(R.id.btn_header_back);
        btnBack.setOnClickListener(v -> requireActivity().onBackPressed());

        /* ── Bind views ── */
        bindProfileViews(view);
        bindSharedDocViews(view);

        /* ── Load profile ── */
        callProfileWebservice(relationshipId);

        /* ── Default: Share With Me tab ── */
        switchToShareWithMe();
    }

    /* ══════════════════════════════════════════════════════════════
       VIEW BINDING
       ══════════════════════════════════════════════════════════════ */
    @SuppressLint("SetTextI18n")
    private void bindProfileViews(View root) {
        tv_name = root.findViewById(R.id.tv_exc_name);
        ll_entity_profile = root.findViewById(R.id.ll_exc_entity_profile);
        ll_individual_profile = root.findViewById(R.id.ll_exc_individual_profile);
        ll_profile_details = root.findViewById(R.id.ll_exc_profile_details);
        tv_toggle_details = root.findViewById(R.id.tv_exc_toggle_details);
        iv_dropdown = root.findViewById(R.id.iv_dropdown);

        // Entity fields
        tv_email_val = root.findViewById(R.id.tv_exc_email_under_name);
        tv_contact_val = root.findViewById(R.id.tv_exc_contact_val);
        tv_country_val = root.findViewById(R.id.tv_exc_country_val);
        tv_phone_val = root.findViewById(R.id.tv_exc_phone_val);
        tv_address_val = root.findViewById(R.id.tv_exc_address_val);
        tv_website_val = root.findViewById(R.id.tv_exc_website_val);

        // Individual fields
        tv_ind_fname = root.findViewById(R.id.tv_exc_ind_fname);
        tv_ind_lname = root.findViewById(R.id.tv_exc_ind_lname);
        tv_ind_email = root.findViewById(R.id.tv_exc_ind_email);
        tv_ind_mobile = root.findViewById(R.id.tv_exc_ind_mobile);
        tv_ind_country = root.findViewById(R.id.tv_exc_ind_country);
        tv_ind_home = root.findViewById(R.id.tv_exc_ind_home);
        tv_ind_work = root.findViewById(R.id.tv_exc_ind_work);
        tv_ind_alt = root.findViewById(R.id.tv_exc_ind_alt);

        /* ── Hide / Show details toggle ── */
        isProfileExpanded = false;
        ll_profile_details.setVisibility(GONE);
        tv_toggle_details.setText(R.string.view_details);
        iv_dropdown.setRotation(0f);

        tv_toggle_details.setOnClickListener(v -> {
            if (isProfileExpanded) {
                ll_profile_details.setVisibility(GONE);
                tv_toggle_details.setText(R.string.view_details);
                iv_dropdown.setRotation(0f);
                isProfileExpanded = false;
            } else {
                ll_profile_details.setVisibility(VISIBLE);
                tv_toggle_details.setText(R.string.hide_details);
                iv_dropdown.setRotation(180f);
                isProfileExpanded = true;
            }
        });
        iv_dropdown.setOnClickListener(v -> {
            if (isProfileExpanded) {
                ll_profile_details.setVisibility(GONE);
                tv_toggle_details.setText(R.string.view_details);
                iv_dropdown.setRotation(0f);
                isProfileExpanded = false;
            } else {
                ll_profile_details.setVisibility(VISIBLE);
                tv_toggle_details.setText(R.string.hide_details);
                iv_dropdown.setRotation(180f);
                isProfileExpanded = true;
            }
        });
    }

    private void bindSharedDocViews(View root) {
        share_card = root.findViewById(R.id.share_card);
        btn_share = root.findViewById(R.id.btn_exc_share);
        rb_share_with_me = root.findViewById(R.id.rb_exc_share_with_me);
        rb_share_by_me = root.findViewById(R.id.rb_exc_share_by_me);
        ll_doc_type_row = root.findViewById(R.id.ll_exc_doc_type_row);
        rb_client_doc = root.findViewById(R.id.rb_exc_client_doc);
        rb_firm_doc = root.findViewById(R.id.rb_exc_firm_doc);
        rv_shared_docs = root.findViewById(R.id.rv_exc_shared_docs);
        tv_no_docs = root.findViewById(R.id.tv_exc_no_docs);
        ll_share_byme_actions = root.findViewById(R.id.ll_exc_share_byme_actions);
        btn_action_cancel = root.findViewById(R.id.btn_exc_action_cancel);
        btn_action_share = root.findViewById(R.id.btn_exc_action_share);
        btn_action_share.setText(R.string.unshare);

        rv_shared_docs.setLayoutManager(
                new LinearLayoutManager(requireContext(), LinearLayoutManager.VERTICAL, false));

        if (!isAccepted) share_card.setVisibility(GONE);

        ll_share_byme_actions.setVisibility(GONE);

        /* ── Tab: Share With Me ── */
        rb_share_with_me.setOnClickListener(v -> switchToShareWithMe());

        /* ── Tab: Share By Me ── */
        rb_share_by_me.setOnClickListener(v -> {
            shared_tag = "byme";
            doc_nature = "byme";   // ✅ already set — confirm this line stays first
            highlightTab(rb_share_with_me, rb_share_by_me, false);
            ll_doc_type_row.setVisibility(VISIBLE); // show Client/Firm sub-tabs
            shared_list.clear();
            shared_by_us_list.clear(); // ✅ clear stale snapshot before fresh load
            ll_share_byme_actions.setVisibility(VISIBLE);
            callSharedDocumentsWebservice(relationshipId, shared_tag, clientId);
        });

        /* ── Client Doc sub-tab ── */
        rb_client_doc.setOnClickListener(v -> {
            shared_tag = "client";
            if (!"solo".equals(Constants.CATEGORY)) {
                rb_client_doc.setBackgroundDrawable(
                        requireContext().getResources().getDrawable(R.drawable.button_left_green_round_background));
                rb_client_doc.setTextColor(android.graphics.Color.WHITE);
                rb_firm_doc.setBackgroundDrawable(
                        requireContext().getResources().getDrawable(R.drawable.button_right_round_background));
                rb_firm_doc.setTextColor(android.graphics.Color.BLACK);
            } else {
                rb_client_doc.setBackgroundDrawable(
                        requireContext().getResources().getDrawable(R.drawable.rounder_button_dark_blue));
                rb_client_doc.setTextColor(android.graphics.Color.WHITE);
                rb_firm_doc.setVisibility(GONE);
            }
            callDocumentTypeWebservice(relationshipId, shared_tag, groupsArray, clientId);
        });

        /* ── Firm Doc sub-tab ── */
        rb_firm_doc.setOnClickListener(v -> {
            shared_tag = "firm";
            rb_client_doc.setBackgroundDrawable(
                    requireContext().getResources().getDrawable(R.drawable.button_left_round_background));
            rb_client_doc.setTextColor(android.graphics.Color.BLACK);
            rb_firm_doc.setBackgroundDrawable(
                    requireContext().getResources().getDrawable(R.drawable.button_right_green_round_background));
            rb_firm_doc.setTextColor(android.graphics.Color.WHITE);
            callDocumentTypeWebservice(relationshipId, shared_tag, groupsArray, clientId);
        });

        /* ── Share button → navigate to ShareDocumentsFragment ── */
        btn_share.setOnClickListener(v -> {
            ShareDocumentsFragment fragment = ShareDocumentsFragment.newInstance(
                    relationshipId,
                    clientId,
                    TAG_rel,
                    groupsArray != null ? groupsArray.toString() : "[]");

            requireActivity().getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.id_framelayout, fragment)
                    .addToBackStack("current_fragment")
                    .commit();
        });

        /* ── Share By Me action bar: Cancel ── */
        btn_action_cancel.setOnClickListener(v -> {
            ll_share_byme_actions.setVisibility(GONE);
            clearDocSelections();
            switchToShareWithMe();
        });

        /* ── Share By Me action bar: Share ── */
        btn_action_share.setOnClickListener(v -> {
            if (currentSharedDocsAdapter == null) return;
            ArrayList<SharedDocumentsDo> selectedDocs = currentSharedDocsAdapter.getSelectedList();
            if (selectedDocs == null || selectedDocs.isEmpty()) {
                AndroidUtils.showAlert("Please select at least one document", getActivity());
                return;
            }
            try {
                JSONArray remove = new JSONArray();
                for (SharedDocumentsDo doc : selectedDocs) {
                    JSONObject o = new JSONObject();
                    o.put("docid", doc.getId());
                    o.put("doctype", "general");
                    remove.put(o);
                }
                showShareConfirmPopup(new ArrayList<>(selectedDocs), remove);
            } catch (JSONException e) {
                e.fillInStackTrace();
            }
        });
    }

    /* ══════════════════════════════════════════════════════════════
       TAB HELPERS
       ══════════════════════════════════════════════════════════════ */
    private void switchToShareWithMe() {
        shared_tag = "withme";
        doc_nature = "withme";
        highlightTab(rb_share_with_me, rb_share_by_me, true);
        ll_doc_type_row.setVisibility(GONE);
        ll_share_byme_actions.setVisibility(GONE);
        shared_list.clear();
        currentSharedDocsAdapter = null;
        callSharedDocumentsWebservice(relationshipId, shared_tag, clientId);
    }

    private void highlightTab(TextView active, TextView inactive, boolean isWithMe) {
        if (isWithMe) {
            active.setBackgroundDrawable(
                    requireContext().getResources().getDrawable(R.drawable.button_left_green_round_background));
            active.setTextColor(android.graphics.Color.WHITE);
            inactive.setBackgroundDrawable(
                    requireContext().getResources().getDrawable(R.drawable.button_right_round_background));
            inactive.setTextColor(android.graphics.Color.BLACK);
        } else {
            active.setBackgroundDrawable(
                    requireContext().getResources().getDrawable(R.drawable.button_left_round_background));
            active.setTextColor(android.graphics.Color.BLACK);
            inactive.setBackgroundDrawable(
                    requireContext().getResources().getDrawable(R.drawable.button_right_green_round_background));
            inactive.setTextColor(android.graphics.Color.WHITE);
        }
    }

    /* ══════════════════════════════════════════════════════════════
       API CALLS
       ══════════════════════════════════════════════════════════════ */
    private void callProfileWebservice(String id) {
        JSONObject json = new JSONObject();
        if ("corporate".equals(TAG_rel)) {
            WebServiceHelper.callHttpWebService(
                    this, getActivity(), WebServiceHelper.RestMethodType.GET,
                    "v3/profile/" + id, "Profile", json.toString());
        } else {
            WebServiceHelper.callHttpWebService(
                    this, getActivity(), WebServiceHelper.RestMethodType.GET,
                    "v2/relationship/" + id + "/profile", "Profile", json.toString());
        }
    }

    private void callSharedDocumentsWebservice(String id, String tag, String cid) {
        progress_dialog = AndroidUtils.get_progress(getActivity());
        JSONObject json = new JSONObject();
        if ("corporate".equals(TAG_rel)) {
            WebServiceHelper.callHttpWebService(
                    this, getActivity(), WebServiceHelper.RestMethodType.GET,
                    "v3/share/" + id + "/" + tag, "Shared Corp Documents", json.toString());
        } else {
            WebServiceHelper.callHttpWebService(
                    this, getActivity(), WebServiceHelper.RestMethodType.GET,
                    "v2/relationship/" + id + "/docs/shared/" + tag,
                    "Shared Documents", json.toString());
        }
    }

    private void callDocumentTypeWebservice(String id, String tag, JSONArray groups, String cid) {
        progress_dialog = AndroidUtils.get_progress(getActivity());
        try {
            JSONObject json = new JSONObject();
            if ("client".equals(tag)) {
                json.put("category", "client");
                json.put("clients", cid);
                json.put("matters", "all");
            } else {
                JSONArray grps = new JSONArray();
                if (groups != null) {
                    for (int i = 0; i < groups.length(); i++)
                        grps.put(groups.getJSONObject(i).getString("id"));
                }
                json.put("category", "firm");
                json.put("groups", grps);
            }
            WebServiceHelper.callHttpWebService(
                    this, getActivity(), WebServiceHelper.RestMethodType.PUT,
                    "v3/document/filter", "Existing Documents", json.toString());
        } catch (JSONException e) {
            e.fillInStackTrace();
        }
    }

    public void View_doc(String doc_id, SharedDocumentsDo docModel) {
        progress_dialog = AndroidUtils.get_progress(getActivity());
        this.currentDocModel = docModel;
        JSONObject json = new JSONObject();
        if ("withme".equals(shared_tag)) {
            if ("corporate".equals(TAG_rel) || "business".equals(TAG_rel)) {
                WebServiceHelper.callHttpWebService(
                        this, getActivity(), WebServiceHelper.RestMethodType.GET,
                        "v3/document/" + doc_id + "/view", "View Other Doc", json.toString());
            } else {
                WebServiceHelper.callHttpWebService(
                        this, getActivity(), WebServiceHelper.RestMethodType.GET,
                        "v2/relationship/" + relationshipId + "/" + doc_id + "/view",
                        "View Doc With Us", json.toString());
            }
        } else {
            WebServiceHelper.callHttpWebService(
                    this, getActivity(), WebServiceHelper.RestMethodType.GET,
                    "v3/document/" + doc_id + "/view", "View Other Doc", json.toString());
        }
    }

    public void callDecryptApi(String id) {
        try {
            progress_dialog = AndroidUtils.get_progress(getActivity());
            JSONObject json = new JSONObject();
            json.put("docid", id);
            if ("withme".equals(shared_tag)) json.put("shared_doc", true);
            WebServiceHelper.callHttpWebService(
                    this, getActivity(), WebServiceHelper.RestMethodType.POST,
                    Constants.decryptUrl, "Decrypt Doc", json.toString());
        } catch (JSONException e) {
            dismissProgress();
        }
    }

    public void callOtherDocViewApi(String id) {
        try {
            progress_dialog = AndroidUtils.get_progress(getActivity());
            JSONObject json = new JSONObject();
            WebServiceHelper.callHttpWebService(
                    this, getActivity(), WebServiceHelper.RestMethodType.GET,
                    Constants.base_URL + "v3/document/" + id + "/view",
                    "Other Doc View", json.toString());
        } catch (Exception e) {
            dismissProgress();
        }
    }

    public void callUnsharedDocumentWebservice(
            String id, JSONArray remove, String message, String cid, boolean isRemove)
            throws JSONException {

        progress_dialog = AndroidUtils.get_progress(getActivity());
        JSONObject json = new JSONObject();
        JSONArray removeDoc = new JSONArray();
        JSONArray addDoc = new JSONArray();

        for (SharedDocumentsDo doc : selected_unsharedocsList) {
            JSONObject o = new JSONObject();
            o.put("docid", doc.getId());
            o.put("doctype", "general");
            removeDoc.put(o);
        }
        for (SharedDocumentsDo doc : selected_sharedocsList) {
            JSONObject o = new JSONObject();
            JSONArray matters = new JSONArray();
            if (doc.isHas_Confidential()) matters.put(doc.getMatter_details_id());
            o.put("docid", doc.getId());
            o.put("doctype", "general");
            o.put("matters", matters);
            addDoc.put(o);
        }

        if (remove.length() != 0) {
            if ("byme".equals(shared_tag)) {
                json.put("remove", isRemove ? remove : removeDoc);
                json.put("add", addDoc);
            } else {
                json.put("remove", removeDoc);
                json.put("add", addDoc);
            }
            json.put("message", message);

            if ("corporate".equals(TAG_rel)) {
                JSONObject corp = new JSONObject();
                corp.put("relid", id);
                corp.put("remove", isRemove ? remove : removeDoc);
                corp.put("add", addDoc);
                corp.put("message", message);
                WebServiceHelper.callHttpWebService(
                        this, getActivity(), WebServiceHelper.RestMethodType.POST,
                        "v3/share", "UnshareDocuments", corp.toString());
            } else {
                WebServiceHelper.callHttpWebService(
                        this, getActivity(), WebServiceHelper.RestMethodType.PUT,
                        "v2/relationship/" + id + "/docs/share",
                        "UnshareDocuments", json.toString());
            }
        } else {
            AndroidUtils.showAlert("Please select at least one document", getActivity());
        }
    }

    @Override
    public void onClick(View view) { /* no-op */ }

    /* ══════════════════════════════════════════════════════════════
       WEBSERVICE CALLBACK
       ══════════════════════════════════════════════════════════════ */
    @Override
    public void onAsyncTaskComplete(HttpResultDo httpResult) {
        dismissProgress();
        try {
            if (httpResult.getResult() == WebServiceHelper.ServiceCallStatus.Success) {
                JSONObject result = new JSONObject(httpResult.getResponseContent());
                boolean error = result.optBoolean("error", false);
                String msg = result.optString("msg", "");
                String type = httpResult.getRequestType();

                switch (type) {

                    case "Profile":
                        if (!error) loadProfile(result.getJSONObject("data"));
                        break;

                    case "Shared Corp Documents":
                    case "Shared Documents":
                        if (!error) loadSharedWithMeDocs(result.getJSONObject("documents"));
                        break;

                    case "Existing Documents":
                        if (!error) loadOtherDocs(result.getJSONArray("data"));
                        break;

                    case "View Doc With Us":
                    case "View Other Doc":
                        if (!error) {
                            String url = type.equals("View Doc With Us")
                                    ? result.getString("url")
                                    : result.getJSONObject("data").getString("url");
                            enrichContentType(url);
                            checkViewType(url, currentDocModel);
                        } else {
                            AndroidUtils.showAlert(msg, getActivity());
                        }
                        break;

                    case "Decrypt Doc":
                    case "Other Doc View":
                        if (!error) {
                            String url = result.getJSONObject("data").getString("url");
                            display_doc(url, currentDocModel);
                        } else {
                            AndroidUtils.showAlert(msg, getActivity());
                        }
                        break;

                    case "UnshareDocuments":
                        AndroidUtils.showAlert(msg, getActivity());
                        clearDocSelections();
                        ll_share_byme_actions.setVisibility(GONE);
                        if (ad_dialog_docs != null && ad_dialog_docs.isShowing())
                            ad_dialog_docs.dismiss();
                        if (alertDialog_confirm != null && alertDialog_confirm.isShowing())
                            alertDialog_confirm.dismiss();
                        callSharedDocumentsWebservice(relationshipId, shared_tag, clientId);
                        break;

                    default:
                        Log.d("ExchangeInfoFrag", "Unhandled type: " + type);
                        break;
                }
            } else {
                try {
                    JSONObject err = new JSONObject(httpResult.getResponseContent());
                    if (err.optBoolean("error"))
                        AndroidUtils.showErrorAlert(err.optString("msg"), getActivity());
                } catch (Exception ignored) {
                    AndroidUtils.showErrorAlert(
                            httpResult.getResponseContent().toString(), getActivity());
                }
            }
        } catch (JSONException e) {
            e.fillInStackTrace();
        }
    }

    /* ══════════════════════════════════════════════════════════════
       DATA LOADING HELPERS
       ══════════════════════════════════════════════════════════════ */
    @SuppressLint("SetTextI18n")
    private void loadProfile(JSONObject data) throws JSONException {
        ProfileDo p = new ProfileDo();
        if (!"individuals".equals(TAG_rel)) {
            p.setFullname(data.optString("fullname"));
            p.setEmail(data.optString("email"));
            p.setContact_person(data.optString("contact_person"));
            p.setContact_phone(data.optString("contact_phone"));
            p.setWebsite(data.optString("website"));
            if (data.has("address")) {
                JSONObject addr = data.getJSONObject("address");
                p.setCountry(addr.optString("country"));
                p.setHouse_flat_no(addr.optString("house_flat_no"));
                p.setStreet(addr.optString("street"));
                p.setCity_town(addr.optString("city_town"));
                p.setState(addr.optString("state"));
                p.setZipcode(addr.optString("zipcode"));
            }

            ll_individual_profile.setVisibility(GONE);
            ll_entity_profile.setVisibility(VISIBLE);

            if (tv_name != null) tv_name.setText(p.getFullname());
            tv_email_val.setText(p.getEmail());
            tv_contact_val.setText(p.getContact_person());
            tv_country_val.setText(p.getCountry());
            String phone = p.getContact_phone();
            tv_phone_val.setText("null".equals(phone) ? "" : phone);

            StringBuilder addr = new StringBuilder();
            if (!isNullOrEmpty(p.getHouse_flat_no())) addr.append(p.getHouse_flat_no());
            if (!isNullOrEmpty(p.getStreet())) addr.append(", ").append(p.getStreet());
            if (!isNullOrEmpty(p.getCity_town())) addr.append(", ").append(p.getCity_town());
            if (!isNullOrEmpty(p.getState())) addr.append(", ").append(p.getState());
            if (!isNullOrEmpty(p.getCountry())) addr.append(", ").append(p.getCountry());
            if (!isNullOrEmpty(p.getZipcode())) addr.append(" ").append(p.getZipcode());
            tv_address_val.setText(addr.toString());
            tv_website_val.setText(p.getWebsite());

        } else {
            p.setFirst_name(data.optString("first_name"));
            p.setLast_name(data.optString("last_name"));
            p.setFullname(data.optString("first_name") + " " + data.optString("last_name"));
            p.setEmail(data.optString("email"));
            p.setMobile(data.optString("mobile"));
            JSONArray citizen = data.optJSONArray("citizen");
            if (citizen != null) {
                for (int i = 0; i < citizen.length(); i++) {
                    JSONObject c = citizen.getJSONObject(i);
                    if ("citizen_primary".equals(c.optString("index"))) {
                        p.setCountry(c.optString("country"));
                        p.setHome_address(c.optString("home_address"));
                        p.setWork_phone(c.optString("work_phone"));
                        p.setAlt_phone(c.optString("alt_phone"));
                        break;
                    }
                }
            }

            ll_entity_profile.setVisibility(GONE);
            ll_individual_profile.setVisibility(VISIBLE);

            tv_ind_fname.setText(p.getFirst_name());
            tv_ind_lname.setText(p.getLast_name());
            tv_ind_email.setText(p.getEmail());
            String mob = p.getMobile();
            tv_ind_mobile.setText("null".equals(mob) ? "" : mob);
            tv_ind_country.setText(p.getCountry());
            tv_ind_home.setText(p.getHome_address());
            tv_ind_work.setText(p.getWork_phone());
            tv_ind_alt.setText(p.getAlt_phone());
            if (tv_name != null) tv_name.setText(p.getFullname());
            tv_email_val.setText(p.getEmail());
        }
    }
    private void loadSharedWithMeDocs(JSONObject documents) {
        try {
            shared_list.clear();
            processDocArray(documents.getJSONArray("general"));
            processDocArray(documents.getJSONArray("credential"));
            processDocArray(documents.getJSONArray("merged"));
            processDocArray(documents.getJSONArray("versioned"));
            processDocArray(documents.getJSONArray("identity"));
            processDocArray(documents.getJSONArray("personal"));

            // ✅ Always snapshot the full list — used by loadOtherDocs to filter
            //    already-shared docs when user picks Client/Firm sub-tab
            shared_by_us_list.clear();
            shared_by_us_list.addAll(shared_list);

            if ("byme".equals(doc_nature)) {
                // Share By Me tab: show the list directly with unshare checkboxes
                ArrayList<SharedDocumentsDo> list = new ArrayList<>();
                for (SharedDocumentsDo d : shared_list) {
                    d.setChecked(selected_unsharedocsList.contains(d));
                    list.add(d);
                }
                renderDocList(list);
            } else if ("withme".equals(doc_nature)) {
                // Share With Me tab: show read-only list
                ArrayList<SharedDocumentsDo> list = new ArrayList<>(shared_list);
                renderDocList(list);
            }
            // if doc_nature == "share_doc" — do nothing, picker opened separately

        } catch (JSONException e) {
            e.fillInStackTrace();
        }
    }

    private void processDocArray(JSONArray arr) throws JSONException {
        for (int i = 0; i < arr.length(); i++)
            shared_list.add(buildSharedDoc(arr.getJSONObject(i)));
        // ✅ Remove the shared_by_us_list lines from here entirely
    }

    private void loadOtherDocs(JSONArray docs) throws JSONException {
        shared_list.clear();
        for (int i = 0; i < docs.length(); i++)
            shared_list.add(buildSharedDoc(docs.getJSONObject(i)));

        ArrayList<SharedDocumentsDo> diff = new ArrayList<>();
        for (SharedDocumentsDo d : shared_list) {
            d.setChecked(selected_sharedocsList.contains(d));
            // ✅ Now works — shared_by_us_list was populated from "byme" API response
            //    and equals() compares by id, so already-shared docs are excluded
            if (!shared_by_us_list.contains(d)) diff.add(d);
        }

        openDocumentPickerPopup(
                "firm".equals(shared_tag) || "client".equals(shared_tag) ? diff : shared_list);
    }
//    private void loadOtherDocs(JSONArray docs) throws JSONException {
//        shared_list.clear();
//        for (int i = 0; i < docs.length(); i++)
//            shared_list.add(buildSharedDoc(docs.getJSONObject(i)));
//
//        ArrayList<SharedDocumentsDo> diff = new ArrayList<>();
//        for (SharedDocumentsDo d : shared_list) {
//            d.setChecked(selected_sharedocsList.contains(d));
//            if (!shared_by_us_list.contains(d)) diff.add(d);
//        }
//
//        openDocumentPickerPopup(
//                "firm".equals(shared_tag) || "client".equals(shared_tag) ? diff : shared_list);
//    }

    private SharedDocumentsDo buildSharedDoc(JSONObject o) throws JSONException {
        SharedDocumentsDo d = new SharedDocumentsDo();
        d.setContent_type(o.optString("content_type"));
        d.setCreated(o.optString("created"));
        d.setDescription(o.optString("description"));
        d.setExpiration_date(o.optString("expiration_date"));
        d.setFilename(o.optString("filename"));
        d.setId(o.optString("id"));
        d.setIs_disabled(o.optBoolean("is_disabled"));
        d.setIs_encrypted(o.optBoolean("is_encrypted"));
        d.setAdded_encryption(o.optBoolean("added_encryption"));
        d.setIs_password(o.optBoolean("is_password"));
        d.setName(o.optString("name"));
        d.setUploaded_by(o.optString("uploaded_by"));
        if (o.has("matter_details")) {
            d.setMatter_details(o.getJSONArray("matter_details"));
            JSONObject m = d.getMatter_details().optJSONObject(0);
            if (m != null) {
                d.setMatter_details_name(m.optString("name"));
                d.setMatter_details_id(m.optString("id"));
                if (!d.getMatter_details_name().isEmpty()) d.setHas_Confidential(true);
            }
        } else {
            d.setHas_Confidential(false);
        }
        return d;
    }

    private void renderDocList(ArrayList<SharedDocumentsDo> list) {
        if (list.isEmpty()) {
            tv_no_docs.setVisibility(VISIBLE);
            rv_shared_docs.setVisibility(GONE);
        } else {
            tv_no_docs.setVisibility(GONE);
            rv_shared_docs.setVisibility(VISIBLE);
            SharedDocumentsAdapter adapter = new SharedDocumentsAdapter(
                    list, shared_tag, requireContext(), this,
                    relationshipId, clientId, requireActivity(), buildBridgeAdapter(), "");
            currentSharedDocsAdapter = adapter;
            rv_shared_docs.setAdapter(adapter);
        }
    }

    /* ══════════════════════════════════════════════════════════════
       DOCUMENT PICKER POPUP
       ══════════════════════════════════════════════════════════════ */
    private void openDocumentPickerPopup(ArrayList<SharedDocumentsDo> docList) {
        try {
            AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
            View view = LayoutInflater.from(requireContext())
                    .inflate(R.layout.shared_document_recyclerview_popup, null);

            RecyclerView rv = view.findViewById(R.id.rv_relationship_documents);
            TextInputEditText etSearch = view.findViewById(R.id.et_search_relationships);
            etSearch.addTextChangedListener(new Validation(etSearch));

            LinearLayout ll_buttons = view.findViewById(R.id.ll_buttons);
            LinearLayout ll_select_all = view.findViewById(R.id.ll_select_all);
            TextView tv_select_all = view.findViewById(R.id.tv_select_all);
            tv_select_all.setText(R.string.select_all);
            chk_select_all_ref = view.findViewById(R.id.chk_select_all);
            TextView tvHeader = view.findViewById(R.id.header_name);
            TextView tvMsg = view.findViewById(R.id.message);
            tvMsg.setGravity(Gravity.CENTER);
            tvMsg.setText(R.string.no_documents_to_show);
            tvMsg.setTextSize(DynamicUtils.twenty);
            AppCompatButton btnShare = view.findViewById(R.id.btn_send_request);
            AppCompatButton btnCancel = view.findViewById(R.id.btn_relationships_cancel);
            ImageView ivClose = view.findViewById(R.id.close_edit_docs);

            String header;
            int btnTextRes = R.string.share;
            switch (shared_tag) {
                case "withme":
                    header = "Documents Shared With Us";
                    ll_select_all.setVisibility(GONE);
                    ll_buttons.setVisibility(GONE);
                    break;
                case "byme":
                    header = "Documents Shared By Us";
                    ll_select_all.setVisibility(VISIBLE);
                    ll_buttons.setVisibility(VISIBLE);
                    btnTextRes = R.string.unshare;
                    break;
                case "client":
                    ll_select_all.setVisibility(VISIBLE);
                    header = "Client Documents";
                    break;
                default:
                    ll_select_all.setVisibility(VISIBLE);
                    header = "Firm Documents";
                    break;
            }
            tvHeader.setText(header);
            btnShare.setText(btnTextRes);

            if (docList.isEmpty()) {
                ll_buttons.setVisibility(GONE);
                etSearch.setVisibility(GONE);
            } else {
                rv.setLayoutManager(
                        new LinearLayoutManager(requireContext(), LinearLayoutManager.VERTICAL, false));
                SharedDocumentsAdapter adapter = new SharedDocumentsAdapter(
                        docList, shared_tag, requireContext(), this,
                        relationshipId, clientId, requireActivity(), buildBridgeAdapter(), "");
                rv.setAdapter(adapter);

                chk_select_all_ref.setOnClickListener(chk -> {
                    adapter.selectOrDeselectAll(chk_select_all_ref.isChecked());
                    if ("byme".equals(shared_tag)) setSelected_unsharedocsList(docList);
                    else setSelected_sharedocsList(docList);
                });

                etSearch.setHint(R.string.search_documents);
                etSearch.addTextChangedListener(new TextWatcher() {
                    @Override
                    public void beforeTextChanged(CharSequence s, int st, int c, int a) {
                    }

                    @Override
                    public void onTextChanged(CharSequence s, int st, int b, int c) {
                    }

                    @Override
                    public void afterTextChanged(Editable s) {
                        adapter.getFilter().filter(s.toString().trim());
                    }
                });

                View.OnClickListener closeListener = v -> {
                    etSearch.setText("");
                    clearDocSelections();
                    ad_dialog_docs.dismiss();
                };
                btnCancel.setOnClickListener(closeListener);
                ivClose.setOnClickListener(closeListener);

                btnShare.setOnClickListener(v -> {
                    try {
                        ArrayList<SharedDocumentsDo> selectedDocs = adapter.getSelectedList();
                        JSONArray remove = new JSONArray();
                        for (SharedDocumentsDo doc : selectedDocs) {
                            JSONObject o = new JSONObject();
                            o.put("docid", doc.getId());
                            o.put("doctype", "general");
                            if ("firm".equals(shared_tag))
                                o.put("matters", doc.getMatter_details());
                            remove.put(o);
                        }
                        if (!selectedDocs.isEmpty()) {
                            etSearch.setText("");
                            showShareConfirmPopup(new ArrayList<>(selectedDocs), remove);
                        } else {
                            Log.d("ExchangeInfoFrag", "No document selected");
                        }
                    } catch (Exception e) {
                        e.fillInStackTrace();
                    }
                });
            }

            AlertDialog dlg = builder.create();
            ad_dialog_docs = dlg;
            dlg.setView(view);
            dlg.setCanceledOnTouchOutside(false);
            dlg.show();

        } catch (Exception e) {
            AndroidUtils.showAlert(e.getMessage(), getActivity());
        }
    }

    private void showShareConfirmPopup(
            ArrayList<SharedDocumentsDo> updatedList, JSONArray remove) {
        try {
            AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
            View view = LayoutInflater.from(requireContext())
                    .inflate(R.layout.share_document_popup, null);

            TextView tvShareDoc = view.findViewById(R.id.tv_share_doc);
            TextView tvUnshareDoc = view.findViewById(R.id.tv_unshare_doc);
            RecyclerView rvShare = view.findViewById(R.id.rv_share_documents);
            RecyclerView rvUnshare = view.findViewById(R.id.rv_unshare_documents);
            LinearLayout llShare = view.findViewById(R.id.ll_share_doc);
            LinearLayout llUnshare = view.findViewById(R.id.ll_unshare_doc);

            tvShareDoc.setTextColor(requireContext().getResources().getColor(R.color.black));
            tvUnshareDoc.setTextColor(requireContext().getResources().getColor(R.color.black));
            tvUnshareDoc.setText(R.string.selected_documents);
            tvShareDoc.setText(R.string.selected_documents);

            llUnshare.setVisibility(selected_unsharedocsList.isEmpty() ? GONE : VISIBLE);
            llShare.setVisibility(selected_sharedocsList.isEmpty() ? GONE : VISIBLE);

            setupSimpleRecycler(rvShare, selected_sharedocsList);
            setupSimpleRecycler(rvUnshare, selected_unsharedocsList);

            TextInputEditText etMsg = view.findViewById(R.id.et_share_message);
            etMsg.setVisibility(VISIBLE);
            TextView tvTitle = view.findViewById(R.id.tv_share_documents);

            Button btnCancel = view.findViewById(R.id.btn_cancel_share);
            Button btnOk = view.findViewById(R.id.btn_ok_share);
            btnOk.setText(R.string.share);
            if ("byme".equals(shared_tag)) {
                tvTitle.setText(R.string.documents_unshare);
                btnOk.setText(R.string.unshare);
            } else {
                tvTitle.setText(R.string.documents_share);
                btnOk.setText(R.string.share);
            }
            AlertDialog dlg = builder.create();
            alertDialog_confirm = dlg;
            dlg.setView(view);
            dlg.setCanceledOnTouchOutside(false);
            dlg.show();

            btnCancel.setOnClickListener(v -> dlg.dismiss());
            btnOk.setOnClickListener(v -> {
                try {
                    callUnsharedDocumentWebservice(
                            relationshipId, remove,
                            Objects.requireNonNull(etMsg.getText()).toString().trim(),
                            clientId, false);
                } catch (JSONException e) {
                    throw new RuntimeException(e);
                }
                dlg.dismiss();
            });

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private void setupSimpleRecycler(RecyclerView rv, ArrayList<SharedDocumentsDo> list) {
        rv.setLayoutManager(
                new LinearLayoutManager(requireContext(), LinearLayoutManager.VERTICAL, false));
        rv.setAdapter(new UnshareDocumentAdapter(list, buildBridgeAdapter()));
    }

    /* ══════════════════════════════════════════════════════════════
       DOCUMENT DISPLAY
       ══════════════════════════════════════════════════════════════ */
    private void checkViewType(String url, SharedDocumentsDo doc) {
        boolean isEncrypted = doc.isAdded_encryption() || doc.isIs_encrypted();
        if (isEncrypted) {
            callDecryptApi(doc.getId());
        } else if (!File_Content_Type.isPDF(doc.getContent_type())
                && !File_Content_Type.isImage(doc.getContent_type())) {
            callOtherDocViewApi(doc.getId());
        } else {
            display_doc(url, doc);
        }
    }

    private void display_doc(String url, SharedDocumentsDo doc) {
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        View view = LayoutInflater.from(requireContext())
                .inflate(R.layout.view_documents, null);
        PDFView pdfView = view.findViewById(R.id.idPDFView);
        ImageView ivImg = view.findViewById(R.id.doc_image);
        android.widget.ProgressBar pb = view.findViewById(R.id.progress_pdf);
        TextView header = view.findViewById(R.id.header_name);
        ImageView ivClose = view.findViewById(R.id.close_edit_docs);
        header.setText(doc.getName());

        boolean isImage = File_Content_Type.isImage(doc.getContent_type());
        boolean urlIsPDF = url.toLowerCase().contains("application/pdf")
                || url.toLowerCase().contains(".pdf");

        final AlertDialog dlg = builder.create();
        final RetrievePDFfromUrl[] task = new RetrievePDFfromUrl[1];

        ivClose.setOnClickListener(v -> {
            try {
                if (pdfView != null) pdfView.recycle();
                if (task[0] != null) {
                    task[0].cancelLoading();
                    task[0].cancel(true);
                }
            } catch (Exception ignored) {
            }
            dlg.dismiss();
        });

        if (urlIsPDF) {
            pdfView.setVisibility(VISIBLE);
            pb.setVisibility(VISIBLE);
            task[0] = new RetrievePDFfromUrl(pdfView, pb);
            task[0].execute(url);
        } else if (isImage) {
            ivImg.setVisibility(VISIBLE);
            com.bumptech.glide.Glide.with(this).load(url)
                    .placeholder(R.drawable.progress_animation).centerCrop().into(ivImg);
        } else {
            pdfView.setVisibility(VISIBLE);
            pb.setVisibility(VISIBLE);
            task[0] = new RetrievePDFfromUrl(pdfView, pb);
            task[0].execute(url);
        }

        dlg.setCancelable(false);
        dlg.setCanceledOnTouchOutside(false);
        dlg.setView(view);
        dlg.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        dlg.show();
    }

    /* ══════════════════════════════════════════════════════════════
       SharedDocumentsAdapter.EventListener impl
       ══════════════════════════════════════════════════════════════ */
    @Override
    public void CopyDocument(SharedDocumentsDo doc) {
        AlertDialog.Builder b = new AlertDialog.Builder(requireContext());
        View v = LayoutInflater.from(requireContext())
                .inflate(R.layout.delete_relationship, null);
        ((TextView) v.findViewById(R.id.tv_confirmation))
                .setText("Are you sure you want to copy " + doc.getName() + "?");
        AppCompatButton btnYes = v.findViewById(R.id.btn_yes);
        AppCompatButton btnNo = v.findViewById(R.id.btn_No);
        final AlertDialog dlg = b.create();
        ad_dialog_copy = dlg;
        dlg.setView(v);
        dlg.setCanceledOnTouchOutside(false);
        dlg.show();
        btnNo.setOnClickListener(x -> dlg.dismiss());
        btnYes.setOnClickListener(x -> {
            JSONObject json = new JSONObject();
            WebServiceHelper.callHttpWebService(
                    this, getActivity(), WebServiceHelper.RestMethodType.GET,
                    "v2/relationship/" + relationshipId + "/" + doc.getId() + "/copy",
                    "Copy Document", json.toString());
            dlg.dismiss();
        });
    }

    @Override
    public void viewDocument(SharedDocumentsDo doc) {
        View_doc(doc.getId(), doc);
    }

    /* ══════════════════════════════════════════════════════════════
       SELECTION HELPERS
       ══════════════════════════════════════════════════════════════ */
    public void setSelected_sharedocsList(ArrayList<SharedDocumentsDo> list) {
        selected_sharedocsList.clear();
        if ("client".equals(shared_tag)) {
            selected_client_list.clear();
            for (SharedDocumentsDo d : list) if (d.isChecked()) selected_client_list.add(d);
        } else if ("firm".equals(shared_tag)) {
            selected_firm_list.clear();
            for (SharedDocumentsDo d : list) if (d.isChecked()) selected_firm_list.add(d);
        }
        selected_sharedocsList.addAll(selected_client_list);
        selected_sharedocsList.addAll(selected_firm_list);
    }

    public void setSelected_unsharedocsList(ArrayList<SharedDocumentsDo> list) {
        selected_unsharedocsList.clear();
        for (SharedDocumentsDo d : list) if (d.isChecked()) selected_unsharedocsList.add(d);
    }

    public void check_select_all(boolean allSelected) {
        if (chk_select_all_ref != null) chk_select_all_ref.setChecked(allSelected);
    }

    private void clearDocSelections() {
        shared_list.clear();
        selected_unsharedocsList.clear();
        selected_sharedocsList.clear();
        selected_firm_list.clear();
        selected_client_list.clear();
        currentSharedDocsAdapter = null;
    }

    /* ══════════════════════════════════════════════════════════════
       BRIDGE ADAPTER
       ══════════════════════════════════════════════════════════════ */
    private RelationshipsAdapter buildBridgeAdapter() {
        return new RelationshipsAdapter(
                new ArrayList<>(), requireContext(), getActivity(), () -> {
        }, TAG_rel,
                null, "") {

            @Override
            public void View_doc(String doc_id, SharedDocumentsDo docModel) {
                currentDocModel = docModel;
                ExchangeInformationFragment.this.View_doc(doc_id, docModel);
            }

            @Override
            public void callDecryptApi(String id) {
                ExchangeInformationFragment.this.callDecryptApi(id);
            }

            @Override
            public void callOtherDocViewApi(String id) {
                ExchangeInformationFragment.this.callOtherDocViewApi(id);
            }

            @Override
            public void callUnsharedDocumentWebservice(
                    String id, JSONArray remove, String message,
                    String cid, boolean isRemove) throws JSONException {
                ExchangeInformationFragment.this
                        .callUnsharedDocumentWebservice(id, remove, message, cid, isRemove);
            }

            @Override
            public void setSelected_sharedocsList(ArrayList<SharedDocumentsDo> list) {
                ExchangeInformationFragment.this.setSelected_sharedocsList(list);
            }

            @Override
            public void setSelected_unsharedocsList(ArrayList<SharedDocumentsDo> list) {
                ExchangeInformationFragment.this.setSelected_unsharedocsList(list);
            }

            @Override
            public void check_select_all(boolean all) {
                ExchangeInformationFragment.this.check_select_all(all);
            }
        };
    }

    /* ══════════════════════════════════════════════════════════════
       MISC HELPERS
       ══════════════════════════════════════════════════════════════ */
    private void dismissProgress() {
        if (progress_dialog != null && progress_dialog.isShowing())
            AndroidUtils.dismiss_dialog(progress_dialog);
    }

    private boolean isNullOrEmpty(String s) {
        return s == null || s.isEmpty() || "null".equals(s);
    }

    private void enrichContentType(String url) {
        if (currentDocModel == null) return;
        String name = currentDocModel.getFilename();
        String ct = com.digicoffer.lauditor.Documents.Documents.replaceLastDotWithSlash(name);
        String[] parts = ct.split("/");
        if (parts.length >= 2) {
            String ext = parts[1];
            boolean isImg = ext.matches("(?i)apng|avif|gif|jpeg|png|svg|webp|jpg");
            currentDocModel.setDoctype((isImg ? "image/" : "application/") + ext);
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        dismissProgress(); // prevent window leaks
    }
}