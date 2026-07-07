package com.digicoffer.lauditor.Relationships;

import static android.view.View.GONE;
import static android.view.View.VISIBLE;

import android.app.AlertDialog;
import android.app.Dialog;
import android.graphics.Color;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
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
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.digicoffer.lauditor.Relationships.Adapter.RelationshipsAdapter;
import com.digicoffer.lauditor.Relationships.Adapter.SharedDocumentsAdapter;
import com.digicoffer.lauditor.Relationships.Adapter.UnshareDocumentAdapter;
import com.digicoffer.lauditor.Relationships.Model.SharedDocumentsDo;
import com.digicoffer.lauditor.R;
import com.digicoffer.lauditor.Webservice.AsyncTaskCompleteListener;
import com.digicoffer.lauditor.Webservice.HttpResultDo;
import com.digicoffer.lauditor.Webservice.CommonApiHelper.WebServiceHelper;
import com.digicoffer.lauditor.CommonFiles.GlobalFiles.AndroidUtils;
import com.digicoffer.lauditor.CommonFiles.GlobalFiles.Constants;
import com.digicoffer.lauditor.CommonFiles.ValidationUtils.Validation;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Objects;

/**
 * ShareDocumentsFragment
 * <p>
 * Drop-in Fragment replacement for ShareDocumentsActivity.
 * <p>
 * Usage – create with the factory method:
 * <p>
 * ShareDocumentsFragment fragment = ShareDocumentsFragment.newInstance(
 * relationshipId, clientId, tag, groupsJsonString);
 * <p>
 * getSupportFragmentManager()
 * .beginTransaction()
 * .replace(R.id.fragment_container, fragment)
 * .addToBackStack(null)
 * .commit();
 */
public class ShareDocumentsFragment extends Fragment
        implements AsyncTaskCompleteListener,
        SharedDocumentsAdapter.EventListener {

    /* ─── Arguments (replaces Intent Extras) ────────────────────── */
    public static final String ARG_RELATIONSHIP_ID = "rel_id";
    public static final String ARG_CLIENT_ID       = "client_id";
    public static final String ARG_TAG             = "tag";
    public static final String ARG_GROUPS_JSON     = "groups_json";

    /* ─── Views ──────────────────────────────────────────────────── */
    private TextView          rb_client_doc, rb_firm_doc;

    // FIX: declared as plain View so it works regardless of whether
    // R.id.tl_share_docs_search is a CardView, FrameLayout, or
    // TextInputLayout in your XML.  We only call setVisibility() on it.
    private View              search_wrapper;

    // The actual TextInputLayout inside the wrapper — may be null if
    // the XML does not contain one; hint is set defensively with null-checks.
    private TextInputLayout   tl_search_layout;

    private TextInputEditText et_search;
    private CheckBox          chk_select_all;
    private TextView          tv_select_all_label;
    private TextView          tv_selected_count;
    private RecyclerView      rv_documents;
    private TextView          tv_no_docs;
    private AppCompatButton   btn_cancel, btn_share_action;
    private LinearLayout      ll_select_all_row;

    /* ─── Progress ───────────────────────────────────────────────── */
    private Dialog progress_dialog;

    /* ─── State ──────────────────────────────────────────────────── */
    private String    relationshipId  = "";
    private String    clientId        = "";
    private String    TAG_rel         = "";
    private JSONArray groupsArray     = new JSONArray();

    private String shared_tag = "client";   // "client" | "firm"

    private ArrayList<SharedDocumentsDo> shared_list            = new ArrayList<>();
    private ArrayList<SharedDocumentsDo> shared_by_us_list      = new ArrayList<>();
    private ArrayList<SharedDocumentsDo> selected_sharedocsList = new ArrayList<>();
    private ArrayList<SharedDocumentsDo> selected_client_list   = new ArrayList<>();
    private ArrayList<SharedDocumentsDo> selected_firm_list     = new ArrayList<>();

    private SharedDocumentsAdapter currentAdapter    = null;
    private AlertDialog            alertDialog_confirm;

    /* ══════════════════════════════════════════════════════════════
       FACTORY METHOD
       ══════════════════════════════════════════════════════════════ */
    public static ShareDocumentsFragment newInstance(
            String relationshipId,
            String clientId,
            String tag,
            String groupsJson) {

        Bundle args = new Bundle();
        args.putString(ARG_RELATIONSHIP_ID, relationshipId);
        args.putString(ARG_CLIENT_ID,       clientId);
        args.putString(ARG_TAG,             tag);
        args.putString(ARG_GROUPS_JSON,     groupsJson);

        ShareDocumentsFragment fragment = new ShareDocumentsFragment();
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
            clientId       = args.getString(ARG_CLIENT_ID,       "");
            TAG_rel        = args.getString(ARG_TAG,             "");
            String groupsJson = args.getString(ARG_GROUPS_JSON,  "");
            try {
                if (groupsJson != null && !groupsJson.isEmpty()) {
                    groupsArray = new JSONArray(groupsJson);
                }
            } catch (JSONException ignored) {
            }
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.activity_share_documents, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        /* ── Custom header ── */
        ImageView btnBack = view.findViewById(R.id.btn_header_back);
        btnBack.setOnClickListener(v -> requireActivity().onBackPressed());

        TextView tvTitle = view.findViewById(R.id.tv_header_title);
        tvTitle.setText(R.string.share_documents);

        bindViews(view);

        /* ── Default: Client Documents tab ── */
        selectClientTab();
        callDocumentTypeWebservice(relationshipId, shared_tag, groupsArray, clientId);
    }

    /* ══════════════════════════════════════════════════════════════
       VIEW BINDING
       ══════════════════════════════════════════════════════════════ */
    private void bindViews(View root) {
        rb_client_doc = root.findViewById(R.id.rb_share_docs_client);
        rb_firm_doc   = root.findViewById(R.id.rb_share_docs_firm);

        // ── Search wrapper ────────────────────────────────────────────
        // R.id.tl_share_docs_search is whatever view wraps the search
        // bar in your XML (CardView, FrameLayout, TextInputLayout, etc.).
        // We hold it as a plain View and only call setVisibility() on it,
        // which avoids the ClassCastException completely.
        search_wrapper = root.findViewById(R.id.tl_share_docs_search);

        // Try to resolve the actual TextInputLayout for hint management.
        // Case 1 – the wrapper IS the TextInputLayout.
        if (search_wrapper instanceof TextInputLayout) {
            tl_search_layout = (TextInputLayout) search_wrapper;
        } else {
            // Case 2 – the TextInputLayout is a child of the wrapper.
            // Find the first TextInputLayout inside it.
            tl_search_layout = search_wrapper != null
                    ? (TextInputLayout) search_wrapper.findViewById(R.id.search_tm)
                    : null;
        }

        // Find the EditText — always directly by ID, never through a cast.
        et_search = root.findViewById(R.id.et_Search);

        // Apply hint safely — TextInputLayout swallows hints set only on
        // the child EditText, so we set it on both when possible.
        applySearchHint();

        chk_select_all      = root.findViewById(R.id.chk_share_docs_select_all);
        tv_select_all_label = root.findViewById(R.id.tv_share_docs_select_all);
        tv_selected_count   = root.findViewById(R.id.tv_share_docs_count);
        rv_documents        = root.findViewById(R.id.rv_share_docs_list);
        tv_no_docs          = root.findViewById(R.id.tv_share_docs_no_docs);
        btn_cancel          = root.findViewById(R.id.btn_share_docs_cancel);
        btn_share_action    = root.findViewById(R.id.btn_share_docs_share);
        btn_share_action.setText(R.string.share);
        ll_select_all_row   = root.findViewById(R.id.ll_share_docs_select_all_row);

        rv_documents.setLayoutManager(
                new LinearLayoutManager(requireContext(), LinearLayoutManager.VERTICAL, false));

        /* ── Client tab ── */
        rb_client_doc.setOnClickListener(v -> {
            shared_tag = "client";
            selectClientTab();
            callDocumentTypeWebservice(relationshipId, shared_tag, groupsArray, clientId);
        });

        /* ── Firm tab ── */
        rb_firm_doc.setOnClickListener(v -> {
            shared_tag = "firm";
            selectFirmTab();
            callDocumentTypeWebservice(relationshipId, shared_tag, groupsArray, clientId);
        });

        /* ── Select All checkbox ── */
        chk_select_all.setOnClickListener(v -> {
            if (currentAdapter != null) {
                currentAdapter.selectOrDeselectAll(chk_select_all.isChecked());
                setSelected_sharedocsList(shared_list);
                updateSelectedCount();
            }
        });

        /* ── Search ── */
        et_search.addTextChangedListener(new Validation(et_search));
        et_search.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int st, int c, int a) {
            }

            @Override
            public void onTextChanged(CharSequence s, int st, int b, int c) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                if (currentAdapter != null)
                    currentAdapter.getFilter().filter(s.toString().trim());
            }
        });

        /* ── Cancel ── */
        btn_cancel.setOnClickListener(v -> requireActivity().onBackPressed());

        /* ── Share action ── */
        btn_share_action.setOnClickListener(v -> {
            if (currentAdapter == null) return;
            ArrayList<SharedDocumentsDo> selectedDocs = currentAdapter.getSelectedList();
            if (selectedDocs == null || selectedDocs.isEmpty()) {
                AndroidUtils.showAlert("Please select at least one document", getActivity());
                return;
            }

            // ── Save IDs before popup mutates the objects ──────────────
            ArrayList<String> savedSelectedIds = new ArrayList<>();
            for (SharedDocumentsDo doc : selectedDocs) {
                savedSelectedIds.add(doc.getId());
            }

            try {
                JSONArray remove = new JSONArray();
                for (SharedDocumentsDo doc : selectedDocs) {
                    JSONObject o = new JSONObject();
                    o.put("docid",   doc.getId());
                    o.put("doctype", "general");
                    if ("firm".equals(shared_tag)) o.put("matters", doc.getMatter_details());
                    remove.put(o);
                }
                showShareConfirmPopup(new ArrayList<>(selectedDocs), remove, savedSelectedIds);
            } catch (JSONException e) {
                e.fillInStackTrace();
            }
        });
    }

    /**
     * Applies the search hint to both the TextInputLayout (if present)
     * and the EditText. Safe to call multiple times — all null-checked.
     */
    private void applySearchHint() {
        if (tl_search_layout != null) {
            tl_search_layout.setHint(getString(R.string.search_documents));
        }
        if (et_search != null) {
            et_search.setHint(R.string.search_documents);
        }
    }

    /* ══════════════════════════════════════════════════════════════
       TAB VISUAL HELPERS
       ══════════════════════════════════════════════════════════════ */
    private void selectClientTab() {
        if (!"solo".equals(Constants.CATEGORY)) {
            rb_firm_doc.setVisibility(VISIBLE);
            rb_client_doc.setBackgroundDrawable(
                    requireContext().getResources().getDrawable(R.drawable.button_left_green_round_background));
            rb_client_doc.setTextColor(Color.WHITE);
            rb_firm_doc.setBackgroundDrawable(
                    requireContext().getResources().getDrawable(R.drawable.button_right_round_background));
            rb_firm_doc.setTextColor(Color.BLACK);
        } else {
            rb_client_doc.setBackgroundDrawable(
                    requireContext().getResources().getDrawable(R.drawable.rounder_button_green));
            rb_client_doc.setTextColor(Color.WHITE);
            rb_firm_doc.setVisibility(GONE);
        }
    }

    private void selectFirmTab() {
        rb_client_doc.setBackgroundDrawable(
                requireContext().getResources().getDrawable(R.drawable.button_left_round_background));
        rb_client_doc.setTextColor(Color.BLACK);
        rb_firm_doc.setBackgroundDrawable(
                requireContext().getResources().getDrawable(R.drawable.button_right_green_round_background));
        rb_firm_doc.setTextColor(Color.WHITE);
    }

    /* ══════════════════════════════════════════════════════════════
       API CALLS
       ══════════════════════════════════════════════════════════════ */
    private void callDocumentTypeWebservice(String id, String tag, JSONArray groups, String cid) {
        progress_dialog = AndroidUtils.get_progress(getActivity());
        try {
            JSONObject json = new JSONObject();
            if ("client".equals(tag)) {
                json.put("category", "client");
                json.put("clients", cid);
                json.put("matters", "all");
                json.put("exclude_already_shared",true);
                json.put("relationship_id", id);
            } else {
                JSONArray grps = new JSONArray();
                if (groups != null) {
                    for (int i = 0; i < groups.length(); i++)
                        grps.put(groups.getJSONObject(i).getString("id"));
                }
                json.put("category", "firm");
//                json.put("groups", grps);
            }
            WebServiceHelper.callHttpWebService(
                    this, getActivity(), WebServiceHelper.RestMethodType.PUT,
                    "v3/document/filter", "Existing Documents", json.toString());
        } catch (JSONException e) {
            e.fillInStackTrace();
        }
    }

    private void callShareDocumentsWebservice(String id, JSONArray remove,
                                              JSONArray addDoc, String message) {
        try {
            progress_dialog = AndroidUtils.get_progress(getActivity());
            JSONObject json = new JSONObject();

            if ("corporate".equals(TAG_rel)) {
                JSONObject corp = new JSONObject();
                corp.put("relid",   id);
                corp.put("remove",  new JSONArray());
                corp.put("add",     addDoc);
                corp.put("message", message);
                WebServiceHelper.callHttpWebService(
                        this, getActivity(), WebServiceHelper.RestMethodType.POST,
                        "v3/share", "ShareDocuments", corp.toString());
            } else {
                json.put("remove",  new JSONArray());
                json.put("add",     addDoc);
                json.put("message", message);
                WebServiceHelper.callHttpWebService(
                        this, getActivity(), WebServiceHelper.RestMethodType.PUT,
                        "v2/relationship/" + id + "/docs/share",
                        "ShareDocuments", json.toString());
            }
        } catch (JSONException e) {
            e.fillInStackTrace();
            dismissProgress();
        }
    }

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
                String  msg   = result.optString("msg",   "");
                String  type  = httpResult.getRequestType();

                switch (type) {
                    case "Existing Documents":
                        if (!error) loadDocs(result.getJSONArray("data"));
                        break;

                    case "ShareDocuments":
                        AndroidUtils.showAlert(msg, getActivity());
                        if (!error) {
                            if (alertDialog_confirm != null && alertDialog_confirm.isShowing())
                                alertDialog_confirm.dismiss();
                            requireActivity().onBackPressed(); // replaces finish()
                        }
                        break;

                    default:
                        Log.d("ShareDocsFragment", "Unhandled type: " + type);
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
       DATA LOADING
       ══════════════════════════════════════════════════════════════ */
    private void loadDocs(JSONArray docs) throws JSONException {
        shared_list.clear();
        shared_by_us_list.clear();
        for (int i = 0; i < docs.length(); i++) {
            shared_list.add(buildSharedDoc(docs.getJSONObject(i)));
        }

        ArrayList<SharedDocumentsDo> displayList = new ArrayList<>();
        for (SharedDocumentsDo d : shared_list) {
            d.setChecked(selected_sharedocsList.contains(d));
            if (!shared_by_us_list.contains(d)) displayList.add(d);
        }

        renderDocList(displayList.isEmpty() ? shared_list : displayList);
        updateSelectedCount();
    }

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
            rv_documents.setVisibility(GONE);
            ll_select_all_row.setVisibility(GONE);
            // Hide the whole search wrapper (CardView / FrameLayout / TextInputLayout)
            if (search_wrapper != null) search_wrapper.setVisibility(GONE);
        } else {
            tv_no_docs.setVisibility(GONE);
            rv_documents.setVisibility(VISIBLE);
            ll_select_all_row.setVisibility(VISIBLE);
            // Show the search wrapper and re-apply the hint
            if (search_wrapper != null) search_wrapper.setVisibility(VISIBLE);
            // Re-apply hint every time list is shown — TextInputLayout
            // can clear it during re-layout after a visibility change.
            applySearchHint();

            SharedDocumentsAdapter adapter = new SharedDocumentsAdapter(
                    list, shared_tag, requireContext(), this,
                    relationshipId, clientId, requireActivity(), buildBridgeAdapter(), "");
            currentAdapter = adapter;
            rv_documents.setAdapter(adapter);
        }
    }

    /* ══════════════════════════════════════════════════════════════
       CONFIRM SHARE POPUP
       ══════════════════════════════════════════════════════════════ */
    private void restoreSelections(ArrayList<String> savedIds) {
        if (currentAdapter == null || savedIds == null) return;

        // Restore the checked flag on the underlying data objects
        for (SharedDocumentsDo doc : shared_list) {
            if (savedIds.contains(doc.getId())) {
                doc.setChecked(true);
            }
        }

        currentAdapter.notifyDataSetChanged();
        updateSelectedCount();
    }

    private void showShareConfirmPopup(
            ArrayList<SharedDocumentsDo> selectedDocs,
            JSONArray remove,
            ArrayList<String> savedSelectedIds) {
        try {
            AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
            View view = LayoutInflater.from(requireContext())
                    .inflate(R.layout.share_document_popup, null);

            TextView     tvShareDoc   = view.findViewById(R.id.tv_share_doc);
            TextView     tvUnshareDoc = view.findViewById(R.id.tv_unshare_doc);
            RecyclerView rvShare      = view.findViewById(R.id.rv_share_documents);
            LinearLayout llShare      = view.findViewById(R.id.ll_share_doc);
            LinearLayout llUnshare   = view.findViewById(R.id.ll_unshare_doc);

            tvShareDoc.setTextColor(requireContext().getResources().getColor(R.color.black));
            tvUnshareDoc.setTextColor(requireContext().getResources().getColor(R.color.black));
            tvShareDoc.setText(R.string.selected_documents);
            tvUnshareDoc.setText(R.string.selected_documents);

            llUnshare.setVisibility(GONE);
            llShare.setVisibility(VISIBLE);

            rvShare.setLayoutManager(
                    new LinearLayoutManager(requireContext(), LinearLayoutManager.VERTICAL, false));
            rvShare.setAdapter(new UnshareDocumentAdapter(selectedDocs, buildBridgeAdapter()));

            TextInputEditText etMsg        = view.findViewById(R.id.et_share_message);
            etMsg.setVisibility(VISIBLE);
            TextView          tvPopupTitle = view.findViewById(R.id.tv_share_documents);
            tvPopupTitle.setText(R.string.documents_share_unshare);

            Button btnCancel = view.findViewById(R.id.btn_cancel_share);
            Button btnOk     = view.findViewById(R.id.btn_ok_share);
            btnOk.setText(R.string.share);
            if ("byme".equals(shared_tag)) {
                tvPopupTitle.setText(R.string.documents_unshare);
                btnOk.setText(R.string.unshare);
            } else {
                tvPopupTitle.setText(R.string.documents_share);
                btnOk.setText(R.string.share);
            }

            AlertDialog dlg = builder.create();
            alertDialog_confirm = dlg;
            dlg.setView(view);
            dlg.setCanceledOnTouchOutside(false);
            dlg.show();
            dlg.getWindow().setBackgroundDrawableResource(android.R.color.transparent);

            btnCancel.setOnClickListener(v -> {
                // ── Restore checked state that the popup adapter mutated ──
                restoreSelections(savedSelectedIds);
                dlg.dismiss();
            });

            // ── Also restore if the dialog is dismissed by back/outside tap ──
            dlg.setOnDismissListener(dialog -> restoreSelections(savedSelectedIds));

            btnOk.setOnClickListener(v -> {
                // ── Clear the dismiss listener so restoreSelections
                //    doesn't run on a successful share ──────────────
                dlg.setOnDismissListener(null);

                JSONArray addDoc = new JSONArray();
                try {
                    for (SharedDocumentsDo doc : selectedDocs) {
                        JSONObject o      = new JSONObject();
                        JSONArray  matters = new JSONArray();
                        if (doc.isHas_Confidential()) matters.put(doc.getMatter_details_id());
                        o.put("docid",   doc.getId());
                        o.put("doctype", "general");
                        o.put("matters", matters);
                        addDoc.put(o);
                    }
                    String message = Objects.requireNonNull(etMsg.getText()).toString().trim();
                    callShareDocumentsWebservice(relationshipId, remove, addDoc, message);
                } catch (JSONException e) {
                    throw new RuntimeException(e);
                }
                dlg.dismiss();
            });

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
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
        updateSelectedCount();
    }

    public void setSelected_unsharedocsList(ArrayList<SharedDocumentsDo> list) {
        // Not used in share flow but required by bridge adapter
    }

    public void check_select_all(boolean allSelected) {
        chk_select_all.setChecked(allSelected);
        updateSelectedCount();
    }

    private void updateSelectedCount() {
        int count = 0;
        if (currentAdapter != null) {
            ArrayList<SharedDocumentsDo> sel = currentAdapter.getSelectedList();
            if (sel != null) count = sel.size();
        }
        if (count > 0) {
            tv_selected_count.setText(count + " selected");
            tv_selected_count.setVisibility(VISIBLE);
            btn_share_action.setText("Share (" + count + ")");
        } else {
            tv_selected_count.setVisibility(GONE);
            btn_share_action.setText("Share");
        }
    }

    /* ══════════════════════════════════════════════════════════════
       SharedDocumentsAdapter.EventListener – not used for share flow
       ══════════════════════════════════════════════════════════════ */
    @Override
    public void CopyDocument(SharedDocumentsDo doc) { /* not used */ }

    @Override
    public void viewDocument(SharedDocumentsDo doc) { /* not used */ }

    @Override
    public void onClick(View view) { /* no-op */ }

    /* ══════════════════════════════════════════════════════════════
       BRIDGE ADAPTER
       ══════════════════════════════════════════════════════════════ */
    private RelationshipsAdapter buildBridgeAdapter() {
        return new RelationshipsAdapter(
                new ArrayList<>(), requireContext(), getActivity(), () -> {
        }, TAG_rel,
                null, "") {

            @Override
            public void setSelected_sharedocsList(ArrayList<SharedDocumentsDo> list) {
                ShareDocumentsFragment.this.setSelected_sharedocsList(list);
            }

            @Override
            public void setSelected_unsharedocsList(ArrayList<SharedDocumentsDo> list) {
                ShareDocumentsFragment.this.setSelected_unsharedocsList(list);
            }

            @Override
            public void check_select_all(boolean all) {
                ShareDocumentsFragment.this.check_select_all(all);
            }
        };
    }

    /* ══════════════════════════════════════════════════════════════
       MISC
       ══════════════════════════════════════════════════════════════ */
    private void dismissProgress() {
        if (progress_dialog != null && progress_dialog.isShowing())
            AndroidUtils.dismiss_dialog(progress_dialog);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        dismissProgress();
    }
}