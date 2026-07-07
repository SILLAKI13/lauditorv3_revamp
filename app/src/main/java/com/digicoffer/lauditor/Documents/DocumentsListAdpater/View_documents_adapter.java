package com.digicoffer.lauditor.Documents.DocumentsListAdpater;

import static android.view.View.GONE;
import static android.view.View.VISIBLE;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Paint;
import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebResourceError;
import android.webkit.WebResourceRequest;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Filter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.digicoffer.lauditor.Documents.Models.DocumentsModel;
import com.digicoffer.lauditor.Documents.Models.ViewDocumentsModel;
import com.digicoffer.lauditor.Groups.Models.ActionModel;
import com.digicoffer.lauditor.R;
import com.digicoffer.lauditor.CommonFiles.GlobalFiles.AndroidUtils;
import com.digicoffer.lauditor.CommonFiles.GlobalFiles.Constants;
import com.digicoffer.lauditor.CommonFiles.GlobalFiles.DynamicUtils;
import com.digicoffer.lauditor.CommonFiles.CommonAdapters.CommonSpinnerAdapter;
import com.google.android.material.imageview.ShapeableImageView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

public class View_documents_adapter extends RecyclerView.Adapter<View_documents_adapter.MyViewHolder> {

    public static final int VIEW_TYPE_LIST = 0;
    public static final int VIEW_TYPE_GRID = 1;
    public static final int VIEW_TYPE_DELETED = 2;
    private int expandedPosition = -1;
    ArrayList<ViewDocumentsModel> list_item;
    ArrayList<ViewDocumentsModel> itemsArrayList;
    Eventlistner eventlistner;
    Context cContext;
    boolean is_encrypted;
    boolean is_MergePdfClicked;
    String DOCUMENT_TYPE_TAG = "";
    ArrayList<DocumentsModel> tags_list = new ArrayList<>();
    private ArrayList<String> highlightIds;

    private int currentViewType = VIEW_TYPE_LIST;
    private RecyclerView recyclerView;

    // Single active PopupWindow — only one open at a time
    private PopupWindow activePopup = null;

    private final HashMap<String, Bitmap> pdfThumbnailCache = new HashMap<>();
    private final HashMap<String, Boolean> pdfLoadingIds = new HashMap<>();

    public View_documents_adapter(ArrayList<ViewDocumentsModel> itemsArrayList,
                                  Eventlistner eventlistner,
                                  Context context,
                                  boolean is_MergePdfClicked,
                                  String DOCUMENT_TYPE_TAG,
                                  ArrayList<String> highlightIds) {
        this.itemsArrayList = itemsArrayList;
        this.eventlistner = eventlistner;
        this.cContext = context;
        this.list_item = itemsArrayList;
        this.is_MergePdfClicked = is_MergePdfClicked;
        this.DOCUMENT_TYPE_TAG = DOCUMENT_TYPE_TAG;
        this.highlightIds = highlightIds;
    }

    public void setViewType(int viewType) {
        this.currentViewType = viewType;
        expandedPosition = -1;   // was: dismissActivePopup()
        notifyDataSetChanged();
    }

    public int getCurrentViewType() {
        return currentViewType;
    }

    public void setRecyclerView(RecyclerView rv) {
        this.recyclerView = rv;
        if (rv != null) {
            rv.setClipChildren(false);
            rv.setClipToPadding(false);
        }
    }

    /**
     * Dismiss any currently visible popup without leaving ghost space.
     */
    private void dismissActivePopup() {
        if (activePopup != null && activePopup.isShowing()) {
            activePopup.dismiss();
        }
        activePopup = null;
    }

    private void safeNotifyItemChanged(final int position) {
        final Runnable r = () -> {
            if (position >= 0 && position < getItemCount()) {
                notifyItemChanged(position);
            }
        };
        if (recyclerView != null) {
            recyclerView.post(r);
        } else {
            new Handler(Looper.getMainLooper()).post(r);
        }
    }

    private void safeNotifyDataSetChanged() {
        final Runnable r = this::notifyDataSetChanged;
        if (recyclerView != null) {
            recyclerView.post(r);
        } else {
            new Handler(Looper.getMainLooper()).post(r);
        }
    }

    public Filter getFilter() {
        return new Filter() {
            @Override
            protected FilterResults performFiltering(CharSequence charSequence) {
                String charString = charSequence.toString();
                if (!list_item.isEmpty()) {
                    if (charString.isEmpty()) {
                        itemsArrayList = list_item;
                    } else {
                        ArrayList<ViewDocumentsModel> filteredList = new ArrayList<>();
                        for (ViewDocumentsModel row : list_item) {
                            if (AndroidUtils.isNull(row.getName())
                                    .toLowerCase()
                                    .contains(charString.toLowerCase())) {
                                filteredList.add(row);
                            }
                        }
                        itemsArrayList = filteredList;
                    }
                }
                FilterResults filterResults = new FilterResults();
                filterResults.count = itemsArrayList.size();
                filterResults.values = itemsArrayList;
                return filterResults;
            }

            @Override
            protected void publishResults(CharSequence charSequence, FilterResults filterResults) {
                itemsArrayList = (ArrayList<ViewDocumentsModel>) filterResults.values;
                notifyDataSetChanged();
            }
        };
    }

    public interface Eventlistner {
        void edit_document(ViewDocumentsModel viewDocumentsModel);

        void Display_Document(ViewDocumentsModel viewDocumentsModel);

        void ViewDialog(ViewDocumentsModel viewDocumentsModel, String ViewType);

        void Download_Document(ViewDocumentsModel viewDocumentsModel);

        void Update_Tag(ViewDocumentsModel viewDocumentsModel);
    }

    @Override
    public int getItemViewType(int position) {
        if (DOCUMENT_TYPE_TAG.equals("Deleted")) return VIEW_TYPE_DELETED;
        return currentViewType;
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view;
        switch (viewType) {
            case VIEW_TYPE_DELETED:
                view = LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.deleted_doc_view, parent, false);
                break;
            case VIEW_TYPE_GRID:
                view = LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.view_documents_grid_item, parent, false);
                break;
            case VIEW_TYPE_LIST:
            default:
                view = LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.view_documents_list, parent, false);
                break;
        }
        return new MyViewHolder(view, viewType);
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {
        ViewDocumentsModel viewDocumentsModel = itemsArrayList.get(position);
        try {
            if (DOCUMENT_TYPE_TAG.equals("Deleted")) {
                bindDeletedView(holder, viewDocumentsModel, position);
            } else if (currentViewType == VIEW_TYPE_GRID) {
                bindGridView(holder, viewDocumentsModel, position);
            } else {
                bindListView(holder, viewDocumentsModel, position);
            }
        } catch (Exception e) {
            Log.e("BindError", "Exception in onBindViewHolder: " + e.getMessage(), e);
        }
    }

    @Override
    public void onViewRecycled(@NonNull MyViewHolder holder) {
        super.onViewRecycled(holder);
        holder.currentDocId = "";
        if (holder.wv_doc_preview_webview != null) {
            holder.wv_doc_preview_webview.stopLoading();
            holder.wv_doc_preview_webview.loadUrl("about:blank");
            holder.wv_doc_preview_webview.setWebViewClient(null);
        }
        if (holder.wv_doc_preview_image != null) {
            Glide.with(cContext).clear(holder.wv_doc_preview_image);
        }
        // Nothing to collapse — popup floats outside the item view
    }

    // ─────────────────────────────────────────────────────────────────────────
    // BIND: DELETED VIEW
    // ─────────────────────────────────────────────────────────────────────────

    private void bindDeletedView(@NonNull MyViewHolder holder,
                                 ViewDocumentsModel viewDocumentsModel,
                                 int position) {

        holder.tv_client_name.setText(viewDocumentsModel.getName());
        holder.tv_doc_description.setText(viewDocumentsModel.getDescription());
        holder.tv_created_date.setText(" " + viewDocumentsModel.getCreated());
        holder.tv_deleted_by_date.setText(viewDocumentsModel.getDeletedBy());

        String exp_date = viewDocumentsModel.getDeletedOn();
        Date date_new = AndroidUtils.stringToDateTimeDefault(exp_date, "MMM dd, yyyy hh:mm a");
        String created = AndroidUtils.getDateToString(date_new, "MMM dd, yyyy");
        holder.tv_deleted_on_date.setText(created);

        holder.tv_doc_type_name.setText(viewDocumentsModel.getCategory());

        boolean dis = viewDocumentsModel.isIs_disabled();
        holder.cv_view_documents.setEnabled(!dis);
        holder.ll_view_icons.setAlpha(dis ? 0.5f : 1.0f);

        holder.tv_doc_description.setPaintFlags(
                holder.tv_doc_description.getPaintFlags() | Paint.UNDERLINE_TEXT_FLAG);

        holder.cv_view_documents.setOnClickListener(v ->
                eventlistner.Display_Document(viewDocumentsModel));

        ArrayList<ActionModel> itemActions = new ArrayList<>();
        itemActions.add(new ActionModel("Restore"));
        itemActions.add(new ActionModel("Delete"));

        // ── Inline expand/collapse — same pattern as bindGridView/bindListView ──
        if (holder.custom_spinner_cardview != null) {
            boolean isExpanded = (position == expandedPosition);

            // Clear stale listeners before rebinding
            holder.custom_spinner_cardview.setOnClickListener(null);
            if (holder.sp_action != null) holder.sp_action.setOnItemClickListener(null);

            if (isExpanded && holder.action_list_card != null && holder.sp_action != null) {
                CommonSpinnerAdapter itemAdapter =
                        new CommonSpinnerAdapter((Activity) cContext, itemActions);
                holder.sp_action.setAdapter(itemAdapter);
                holder.sp_action.post(() -> AndroidUtils.setDynamicHeight(holder.sp_action));
                holder.action_list_card.setVisibility(VISIBLE);
                holder.sp_action.setVisibility(VISIBLE);
            } else {
                if (holder.sp_action != null) holder.sp_action.setAdapter(null);
                if (holder.action_list_card != null) holder.action_list_card.setVisibility(GONE);
                if (holder.sp_action != null) holder.sp_action.setVisibility(GONE);
            }

            holder.custom_spinner_cardview.setOnClickListener(v -> {
                int cur = holder.getAdapterPosition();
                if (cur == RecyclerView.NO_POSITION) return;

                int prev = expandedPosition;
                expandedPosition = (expandedPosition == cur) ? -1 : cur;

                if (prev != -1 && prev != cur) safeNotifyItemChanged(prev);
                safeNotifyItemChanged(cur);
            });

            if (holder.sp_action != null) {
                holder.sp_action.setOnItemClickListener((parent, view, pos, id) -> {
                    int cur = holder.getAdapterPosition();
                    if (cur == RecyclerView.NO_POSITION) return;

                    String actionName = itemActions.get(pos).getName();
                    expandedPosition = -1;
                    safeNotifyItemChanged(cur);

                    new Handler(Looper.getMainLooper()).post(() ->
                            dispatchDeletedAction(actionName, viewDocumentsModel));
                });
            }
        }

        applyHighlight(holder, viewDocumentsModel);
    }

    // ─────────────────────────────────────────────────────────────────────────
    // BIND: LIST VIEW
    // ─────────────────────────────────────────────────────────────────────────

    private void bindListView(@NonNull MyViewHolder holder,
                              ViewDocumentsModel viewDocumentsModel,
                              int position) {

        holder.tv_client_name.setText(viewDocumentsModel.getUploaded_by());
        holder.tv_client_name_one.setText(viewDocumentsModel.getUploaded_by());
        holder.tv_doc_description.setText(viewDocumentsModel.getDescription());
        holder.tv_document_display_name.setText(viewDocumentsModel.getName());
        holder.tv_image_name.setText(viewDocumentsModel.getName());
        holder.tv_created_date.setText(viewDocumentsModel.getCreated());
        holder.tv_Expiration_date.setText(viewDocumentsModel.getExpiration_date());

        is_encrypted = viewDocumentsModel.isIs_encrypted();
        boolean showLock = is_encrypted || viewDocumentsModel.isAdded_encryption();
        boolean isDownloadDisabled = viewDocumentsModel.isIs_disabled();

        if (!is_MergePdfClicked) {
            safeGone(holder.lock_open);
            safeGone(holder.lock_close);
            safeGone(holder.iv_edit_document);
            safeGone(holder.tv_Expiration_date);
            applyDownloadState(holder, isDownloadDisabled);
        } else {
            applyLockState(holder, showLock);
            applyDownloadState(holder, isDownloadDisabled);
            safeVisible(holder.iv_edit_document);
            safeVisible(holder.tv_Expiration_date);
        }

        applyDisabledState(holder, viewDocumentsModel.isIsdisabled());
        bindTags(holder, viewDocumentsModel.getTagslist());

        holder.tv_client_name.setPaintFlags(
                holder.tv_client_name.getPaintFlags() | Paint.UNDERLINE_TEXT_FLAG);
        holder.tv_doc_description.setPaintFlags(
                holder.tv_doc_description.getPaintFlags() | Paint.UNDERLINE_TEXT_FLAG);

        holder.cv_view_documents.setOnClickListener(v ->
                eventlistner.Display_Document(viewDocumentsModel));
        attachLegacyClickListeners(holder, viewDocumentsModel);
        ArrayList<ActionModel> itemActions = buildStandardActions(showLock, isDownloadDisabled);

        if (holder.custom_spinner_cardview != null) {
            boolean isExpanded = (position == expandedPosition);

            holder.custom_spinner_cardview.setOnClickListener(null);
            if (holder.sp_action != null) holder.sp_action.setOnItemClickListener(null);

            if (isExpanded && holder.action_list_card != null && holder.sp_action != null) {
                CommonSpinnerAdapter itemAdapter =
                        new CommonSpinnerAdapter((Activity) cContext, itemActions);
                holder.sp_action.setAdapter(itemAdapter);
                holder.sp_action.post(() -> AndroidUtils.setDynamicHeight(holder.sp_action));
                holder.action_list_card.setVisibility(VISIBLE);
                holder.sp_action.setVisibility(VISIBLE);
            } else {
                if (holder.sp_action != null) holder.sp_action.setAdapter(null);
                if (holder.action_list_card != null) holder.action_list_card.setVisibility(GONE);
                if (holder.sp_action != null) holder.sp_action.setVisibility(GONE);
            }

            holder.custom_spinner_cardview.setOnClickListener(v -> {
                int cur = holder.getAdapterPosition();
                if (cur == RecyclerView.NO_POSITION) return;

                int prev = expandedPosition;
                expandedPosition = (expandedPosition == cur) ? -1 : cur;

                if (prev != -1 && prev != cur) safeNotifyItemChanged(prev);
                safeNotifyItemChanged(cur);
            });

            if (holder.sp_action != null) {
                holder.sp_action.setOnItemClickListener((parent, view, pos, id) -> {
                    int cur = holder.getAdapterPosition();
                    if (cur == RecyclerView.NO_POSITION) return;

                    String actionName = itemActions.get(pos).getName();
                    expandedPosition = -1;
                    safeNotifyItemChanged(cur);

                    new Handler(Looper.getMainLooper()).post(() ->
                            dispatchAction(actionName, viewDocumentsModel));
                });
            }
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    // BIND: GRID VIEW
    // ─────────────────────────────────────────────────────────────────────────

    @SuppressLint("SetJavaScriptEnabled")
    private void bindGridView(@NonNull MyViewHolder holder,
                              ViewDocumentsModel viewDocumentsModel,
                              int position) {

        resetThumbnailToLoadingState(holder);

        holder.tv_document_display_name.setText(viewDocumentsModel.getName());

        String createdDate = viewDocumentsModel.getCreated();
        if (createdDate != null && createdDate.contains(",")) {
            String[] parts = createdDate.split(",");
            holder.tv_created_date.setText(
                    parts.length >= 2 ? parts[0].trim() + "," + parts[1].trim() : createdDate);
        } else {
            holder.tv_created_date.setText(createdDate != null ? createdDate : "");
        }

        holder.currentDocId = viewDocumentsModel.getId();

        String fileUrl = viewDocumentsModel.getOrigin();
        String contentType = viewDocumentsModel.getContent_type();
        boolean isEncrypted = viewDocumentsModel.isIs_encrypted()
                || viewDocumentsModel.isAdded_encryption();

        loadGridThumbnailWebView(holder, viewDocumentsModel.getId(), fileUrl,
                contentType, isEncrypted);

        is_encrypted = viewDocumentsModel.isIs_encrypted();
        boolean showLock = is_encrypted || viewDocumentsModel.isAdded_encryption();
        boolean isDownloadDisabled = viewDocumentsModel.isIs_disabled();

        if (!is_MergePdfClicked) {
            safeGone(holder.lock_open);
            safeGone(holder.lock_close);
            applyDownloadState(holder, isDownloadDisabled);
        } else {
            applyLockState(holder, showLock);
            applyDownloadState(holder, isDownloadDisabled);
        }

        applyDisabledState(holder, viewDocumentsModel.isIsdisabled());

        holder.cv_view_documents.setOnClickListener(v ->
                eventlistner.Display_Document(viewDocumentsModel));

        attachLegacyClickListeners(holder, viewDocumentsModel);

        ArrayList<ActionModel> itemActions = buildStandardActions(showLock, isDownloadDisabled);

        if (holder.custom_spinner_cardview != null) {
            boolean isExpanded = (position == expandedPosition);

            // Clear stale listeners before rebinding
            holder.custom_spinner_cardview.setOnClickListener(null);
            if (holder.sp_action != null) holder.sp_action.setOnItemClickListener(null);

            if (isExpanded && holder.action_list_card != null && holder.sp_action != null) {
                CommonSpinnerAdapter itemAdapter =
                        new CommonSpinnerAdapter((Activity) cContext, itemActions);
                holder.sp_action.setAdapter(itemAdapter);
                holder.sp_action.post(() -> AndroidUtils.setDynamicHeight(holder.sp_action));
                holder.action_list_card.setVisibility(VISIBLE);
                holder.sp_action.setVisibility(VISIBLE);
            } else {
                if (holder.sp_action != null) holder.sp_action.setAdapter(null);
                if (holder.action_list_card != null) holder.action_list_card.setVisibility(GONE);
                if (holder.sp_action != null) holder.sp_action.setVisibility(GONE);
            }

            holder.custom_spinner_cardview.setOnClickListener(v -> {
                int cur = holder.getAdapterPosition();
                if (cur == RecyclerView.NO_POSITION) return;

                int prev = expandedPosition;
                expandedPosition = (expandedPosition == cur) ? -1 : cur;

                if (prev != -1 && prev != cur) safeNotifyItemChanged(prev);
                safeNotifyItemChanged(cur);
            });

            if (holder.sp_action != null) {
                holder.sp_action.setOnItemClickListener((parent, view, pos, id) -> {
                    int cur = holder.getAdapterPosition();
                    if (cur == RecyclerView.NO_POSITION) return;

                    String actionName = itemActions.get(pos).getName();
                    expandedPosition = -1;
                    safeNotifyItemChanged(cur);

                    new Handler(Looper.getMainLooper()).post(() ->
                            dispatchAction(actionName, viewDocumentsModel));
                });
            }
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    // HELPERS
    // ─────────────────────────────────────────────────────────────────────────

    private ArrayList<ActionModel> buildStandardActions(boolean showLock, boolean isDownloadDisabled) {
        ArrayList<ActionModel> actions = new ArrayList<>();
        actions.add(new ActionModel("View"));
        actions.add(new ActionModel("Edit Info"));
        // Only add Download if the document is NOT disabled
        if (isDownloadDisabled) {
            actions.add(new ActionModel("Download"));
        }
        actions.add(new ActionModel("Delete"));
        actions.add(new ActionModel("Update Tags"));
        return actions;
    }

    @SuppressLint("SetJavaScriptEnabled")
    private void loadGridThumbnailWebView(@NonNull MyViewHolder holder,
                                          String docId, String fileUrl,
                                          String contentType, boolean isEncrypted) {

        if (holder.pb_doc_loading != null) holder.pb_doc_loading.setVisibility(VISIBLE);
        if (holder.view_thumb_placeholder != null)
            holder.view_thumb_placeholder.setVisibility(VISIBLE);
        if (holder.wv_doc_preview_webview != null)
            holder.wv_doc_preview_webview.setVisibility(GONE);
        if (holder.wv_doc_preview_image != null) holder.wv_doc_preview_image.setVisibility(GONE);

        if (isEncrypted || fileUrl == null || fileUrl.trim().isEmpty()) {
            fetchDocumentUrlThenPreview(holder, docId, contentType, isEncrypted);
            return;
        }
        showDocumentInWebView(holder, docId, fileUrl, contentType);
    }

    private void fetchDocumentUrlThenPreview(@NonNull MyViewHolder holder,
                                             String docId, String contentType,
                                             boolean isEncrypted) {
        new Thread(() -> {
            String fetchedUrl = null;
            String effectiveContentType = contentType;
            try {
                if (isEncrypted) {
                    java.net.URL apiUrl = new java.net.URL(Constants.decryptUrl);
                    java.net.HttpURLConnection conn =
                            (java.net.HttpURLConnection) apiUrl.openConnection();
                    String boundary = "----WebKitFormBoundary" + System.currentTimeMillis();
                    conn.setRequestMethod("POST");
                    conn.setDoOutput(true);
                    conn.setDoInput(true);
                    conn.setRequestProperty("Content-Type",
                            "multipart/form-data; boundary=" + boundary);
                    conn.setRequestProperty("Authorization", "Bearer " + Constants.TOKEN);
                    conn.setRequestProperty("x-client-type", "mobile");
                    conn.setConnectTimeout(15000);
                    conn.setReadTimeout(30000);

                    java.io.DataOutputStream out =
                            new java.io.DataOutputStream(conn.getOutputStream());
                    out.writeBytes("--" + boundary + "\r\n");
                    out.writeBytes("Content-Disposition: form-data; name=\"docid\"\r\n\r\n");
                    out.writeBytes(docId + "\r\n");
                    out.writeBytes("--" + boundary + "\r\n");
                    out.writeBytes("Content-Disposition: form-data; name=\"download\"\r\n\r\n");
                    out.writeBytes("false\r\n");
                    out.writeBytes("--" + boundary + "--\r\n");
                    out.flush();
                    out.close();

                    int code = conn.getResponseCode();
                    String respCt = conn.getContentType();

                    if (code == 200) {
                        if (respCt != null && respCt.contains("application/pdf")) {
                            File pdfFile = File.createTempFile(
                                    "enc_prev_" + docId, ".pdf", cContext.getCacheDir());
                            try (InputStream in = new BufferedInputStream(conn.getInputStream());
                                 FileOutputStream fo = new FileOutputStream(pdfFile)) {
                                byte[] buf = new byte[4096];
                                int n;
                                while ((n = in.read(buf)) != -1) fo.write(buf, 0, n);
                            }
                            fetchedUrl = "localfile://" + pdfFile.getAbsolutePath();
                            effectiveContentType = "application/pdf";
                        } else {
                            java.io.BufferedReader reader = new java.io.BufferedReader(
                                    new java.io.InputStreamReader(conn.getInputStream()));
                            StringBuilder sb = new StringBuilder();
                            String line;
                            while ((line = reader.readLine()) != null) sb.append(line);
                            reader.close();
                            JSONObject resp = new JSONObject(sb.toString());
                            if (!resp.optBoolean("error", true)) {
                                JSONObject data = resp.optJSONObject("data");
                                if (data != null) {
                                    fetchedUrl = data.optString("url");
                                    String ct = data.optString("content_type", contentType);
                                    if (!ct.isEmpty()) effectiveContentType = ct;
                                }
                            }
                        }
                    }
                } else {
                    java.net.URL apiUrl = new java.net.URL(
                            Constants.PROF_URL + "v3/document/" + docId + "/view");
                    java.net.HttpURLConnection conn =
                            (java.net.HttpURLConnection) apiUrl.openConnection();
                    conn.setRequestMethod("GET");
                    conn.setConnectTimeout(15000);
                    conn.setReadTimeout(15000);
                    String token = Constants.TOKEN;
                    if (token != null && !token.isEmpty())
                        conn.setRequestProperty("Authorization", "Bearer " + token);
                    conn.connect();

                    java.io.BufferedReader reader = new java.io.BufferedReader(
                            new java.io.InputStreamReader(conn.getInputStream()));
                    StringBuilder sb = new StringBuilder();
                    String line;
                    while ((line = reader.readLine()) != null) sb.append(line);
                    reader.close();

                    JSONObject resp = new JSONObject(sb.toString());
                    if (!resp.optBoolean("error", true)) {
                        JSONObject data = resp.optJSONObject("data");
                        if (data != null) {
                            String rawUrl = data.optString("url");
                            String fileCt = data.optString("content_type", contentType);
                            if (!fileCt.isEmpty()) effectiveContentType = fileCt;

                            boolean isImg = fileCt.toLowerCase().startsWith("image/");
                            boolean isPdf = fileCt.toLowerCase().contains("pdf")
                                    || rawUrl.toLowerCase().contains(".pdf");

                            if (isImg || isPdf) {
                                fetchedUrl = rawUrl;
                            } else {
                                fetchedUrl = callDoc2PdfApiSync(rawUrl);
                                if (fetchedUrl == null) fetchedUrl = rawUrl;
                            }
                        }
                    }
                }
            } catch (Exception e) {
                Log.e("FetchUrl", "fetchDocumentUrlThenPreview: " + e.getMessage(), e);
            }

            final String finalUrl = fetchedUrl;
            final String finalContentType = effectiveContentType;

            new Handler(Looper.getMainLooper()).post(() -> {
                if (!docId.equals(holder.currentDocId)) return;
                if (finalUrl != null && !finalUrl.isEmpty()) {
                    showDocumentInWebView(holder, docId, finalUrl, finalContentType);
                } else {
                    showThumbFallback(holder);
                }
            });
        }).start();
    }

    @SuppressLint("SetJavaScriptEnabled")
    private void showDocumentInWebView(@NonNull MyViewHolder holder,
                                       String docId, String fileUrl, String contentType) {

        boolean isImage = false;
        if (contentType != null) isImage = contentType.toLowerCase().startsWith("image/");
        if (!isImage && fileUrl != null) {
            String lower = fileUrl.toLowerCase();
            isImage = lower.endsWith(".jpg") || lower.endsWith(".jpeg")
                    || lower.endsWith(".png") || lower.endsWith(".gif")
                    || lower.endsWith(".webp") || lower.endsWith(".bmp");
        }

        boolean isPdf = false;
        if (contentType != null) isPdf = contentType.toLowerCase().contains("pdf");
        if (!isPdf && fileUrl != null)
            isPdf = fileUrl.toLowerCase().endsWith(".pdf")
                    || fileUrl.startsWith("localfile://");

        if (isImage) {
            if (holder.pb_doc_loading != null) holder.pb_doc_loading.setVisibility(GONE);
            if (holder.view_thumb_placeholder != null)
                holder.view_thumb_placeholder.setVisibility(GONE);

            if (holder.wv_doc_preview_image != null) {
                holder.wv_doc_preview_image.setVisibility(VISIBLE);
                if (holder.wv_doc_preview_webview != null)
                    holder.wv_doc_preview_webview.setVisibility(GONE);
                Glide.with(cContext)
                        .load(fileUrl)
                        .centerCrop()
                        .into(holder.wv_doc_preview_image);
            } else if (holder.wv_doc_preview_webview != null) {
                showImageInWebView(holder, docId, fileUrl);
            }
            return;
        }

        if (isPdf) {
            Bitmap cached = pdfThumbnailCache.get(docId);
            if (cached != null) {
                displayPdfBitmap(holder, cached);
                return;
            }
            if (Boolean.TRUE.equals(pdfLoadingIds.get(docId))) return;
            pdfLoadingIds.put(docId, true);

            if (holder.pb_doc_loading != null) holder.pb_doc_loading.setVisibility(VISIBLE);
            if (holder.view_thumb_placeholder != null)
                holder.view_thumb_placeholder.setVisibility(VISIBLE);

            final String urlForThread = fileUrl;
            new Thread(() -> {
                Bitmap bitmap = null;
                File tempFile = null;
                boolean isLocal = urlForThread.startsWith("localfile://");
                try {
                    if (isLocal) {
                        tempFile = new File(urlForThread.replace("localfile://", ""));
                    } else {
                        tempFile = File.createTempFile(
                                "grid_prev_" + docId, ".pdf", cContext.getCacheDir());
                        java.net.URL url = new java.net.URL(urlForThread);
                        java.net.HttpURLConnection conn =
                                (java.net.HttpURLConnection) url.openConnection();
                        conn.setConnectTimeout(15000);
                        conn.setReadTimeout(30000);
                        conn.connect();
                        try (InputStream in = new BufferedInputStream(conn.getInputStream());
                             FileOutputStream fo = new FileOutputStream(tempFile)) {
                            byte[] buf = new byte[4096];
                            int n;
                            while ((n = in.read(buf)) != -1) fo.write(buf, 0, n);
                        }
                    }

                    android.os.ParcelFileDescriptor pfd =
                            android.os.ParcelFileDescriptor.open(tempFile,
                                    android.os.ParcelFileDescriptor.MODE_READ_ONLY);
                    android.graphics.pdf.PdfRenderer renderer =
                            new android.graphics.pdf.PdfRenderer(pfd);
                    android.graphics.pdf.PdfRenderer.Page page = renderer.openPage(0);
                    int w = 600;
                    int h = (int) (w * (float) page.getHeight() / page.getWidth());
                    bitmap = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888);
                    bitmap.eraseColor(android.graphics.Color.WHITE);
                    page.render(bitmap, null, null,
                            android.graphics.pdf.PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY);
                    page.close();
                    renderer.close();
                    pfd.close();
                } catch (Exception e) {
                    Log.e("GridPdfPreview", "PDF render failed: " + e.getMessage());
                } finally {
                    if (tempFile != null && !isLocal) tempFile.delete();
                }

                final Bitmap finalBitmap = bitmap;
                new Handler(Looper.getMainLooper()).post(() -> {
                    pdfLoadingIds.put(docId, false);
                    if (finalBitmap != null) pdfThumbnailCache.put(docId, finalBitmap);
                    if (docId.equals(holder.currentDocId)) {
                        if (finalBitmap != null) {
                            displayPdfBitmap(holder, finalBitmap);
                        } else {
                            showInGoogleDocsWebView(holder, docId, urlForThread);
                        }
                    }
                });
            }).start();
            return;
        }

        showInGoogleDocsWebView(holder, docId, fileUrl);
    }

    private void displayPdfBitmap(@NonNull MyViewHolder holder, Bitmap bitmap) {
        if (holder.pb_doc_loading != null) holder.pb_doc_loading.setVisibility(GONE);
        if (holder.view_thumb_placeholder != null)
            holder.view_thumb_placeholder.setVisibility(GONE);

        ImageView target = holder.wv_doc_preview_image;
        if (target != null) {
            target.setVisibility(VISIBLE);
            if (holder.wv_doc_preview_webview != null)
                holder.wv_doc_preview_webview.setVisibility(GONE);
            target.setImageBitmap(bitmap);
            target.setScaleType(ImageView.ScaleType.CENTER_CROP);
        } else if (holder.wv_doc_preview_webview != null) {
            java.io.ByteArrayOutputStream baos = new java.io.ByteArrayOutputStream();
            bitmap.compress(Bitmap.CompressFormat.PNG, 90, baos);
            String base64 = android.util.Base64.encodeToString(
                    baos.toByteArray(), android.util.Base64.DEFAULT);
            String html = "<!DOCTYPE html><html><head>"
                    + "<meta name='viewport' content='width=device-width,initial-scale=1'>"
                    + "<style>body{margin:0;padding:0;background:#fff;}"
                    + "img{width:100%;height:auto;display:block;}</style></head><body>"
                    + "<img src='data:image/png;base64," + base64 + "'/>"
                    + "</body></html>";
            configureWebView(holder.wv_doc_preview_webview);
            holder.wv_doc_preview_webview.setVisibility(VISIBLE);
            holder.wv_doc_preview_webview.loadDataWithBaseURL(
                    null, html, "text/html", "UTF-8", null);
        }
    }

    @SuppressLint("SetJavaScriptEnabled")
    private void showImageInWebView(@NonNull MyViewHolder holder,
                                    String docId, String imageUrl) {
        if (holder.wv_doc_preview_webview == null) {
            showThumbFallback(holder);
            return;
        }
        configureWebView(holder.wv_doc_preview_webview);
        holder.wv_doc_preview_webview.setWebViewClient(buildWebViewClient(holder, docId));
        String html = "<!DOCTYPE html><html><head>"
                + "<meta name='viewport' content='width=device-width,initial-scale=1'>"
                + "<style>body{margin:0;padding:0;background:#fff;display:flex;"
                + "align-items:center;justify-content:center;min-height:100vh;}"
                + "img{max-width:100%;max-height:100%;object-fit:cover;}</style>"
                + "</head><body><img src='" + imageUrl + "'/></body></html>";
        holder.wv_doc_preview_webview.setVisibility(VISIBLE);
        holder.wv_doc_preview_webview.loadDataWithBaseURL(null, html, "text/html", "UTF-8", null);
    }

    @SuppressLint("SetJavaScriptEnabled")
    private void showInGoogleDocsWebView(@NonNull MyViewHolder holder,
                                         String docId, String fileUrl) {
        if (holder.wv_doc_preview_webview == null) {
            showThumbFallback(holder);
            return;
        }
        configureWebView(holder.wv_doc_preview_webview);
        holder.wv_doc_preview_webview.setWebViewClient(buildWebViewClient(holder, docId));
        String encodedUrl = android.net.Uri.encode(fileUrl);
        String viewerUrl = "https://docs.google.com/viewer?embedded=true&url=" + encodedUrl;
        holder.wv_doc_preview_webview.setVisibility(VISIBLE);
        holder.wv_doc_preview_webview.loadUrl(viewerUrl);
        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            if (holder.pb_doc_loading != null
                    && holder.pb_doc_loading.getVisibility() == VISIBLE) {
                showThumbFallback(holder);
            }
        }, 12000);
    }

    @SuppressLint("SetJavaScriptEnabled")
    private void configureWebView(WebView wv) {
        WebSettings s = wv.getSettings();
        s.setJavaScriptEnabled(true);
        s.setLoadWithOverviewMode(true);
        s.setUseWideViewPort(true);
        s.setBuiltInZoomControls(false);
        s.setDisplayZoomControls(false);
        s.setDomStorageEnabled(true);
        s.setSupportZoom(false);
        s.setMixedContentMode(WebSettings.MIXED_CONTENT_ALWAYS_ALLOW);
        s.setCacheMode(WebSettings.LOAD_DEFAULT);
        s.setAllowFileAccess(true);
        s.setLoadsImagesAutomatically(true);
        wv.setBackgroundColor(ContextCompat.getColor(cContext, android.R.color.white));
        wv.setScrollBarStyle(View.SCROLLBARS_INSIDE_OVERLAY);
        wv.setHorizontalScrollBarEnabled(false);
        wv.setVerticalScrollBarEnabled(false);
        wv.setOnTouchListener((v, ev) -> true);
    }

    private WebViewClient buildWebViewClient(@NonNull MyViewHolder holder, String docId) {
        return new WebViewClient() {
            @Override
            public void onPageFinished(WebView view, String url) {
                if (!docId.equals(holder.currentDocId)) return;
                if (holder.pb_doc_loading != null)
                    holder.pb_doc_loading.setVisibility(GONE);
                if (holder.view_thumb_placeholder != null)
                    holder.view_thumb_placeholder.setVisibility(GONE);
                view.setVisibility(VISIBLE);
            }

            @Override
            public void onReceivedError(WebView view, WebResourceRequest req,
                                        WebResourceError err) {
                showThumbFallback(holder);
            }

            @SuppressWarnings("deprecation")
            @Override
            public void onReceivedError(WebView view, int code, String desc, String url) {
                showThumbFallback(holder);
            }
        };
    }

    private void resetThumbnailToLoadingState(@NonNull MyViewHolder holder) {
        if (holder.view_thumb_placeholder != null)
            holder.view_thumb_placeholder.setVisibility(VISIBLE);
        if (holder.pb_doc_loading != null)
            holder.pb_doc_loading.setVisibility(VISIBLE);
        if (holder.wv_doc_preview_webview != null) {
            holder.wv_doc_preview_webview.setVisibility(GONE);
            holder.wv_doc_preview_webview.stopLoading();
            holder.wv_doc_preview_webview.loadUrl("about:blank");
        }
        if (holder.wv_doc_preview_image != null)
            holder.wv_doc_preview_image.setVisibility(GONE);
    }

    private void showThumbFallback(@NonNull MyViewHolder holder) {
        if (holder.pb_doc_loading != null) holder.pb_doc_loading.setVisibility(GONE);
        if (holder.wv_doc_preview_webview != null)
            holder.wv_doc_preview_webview.setVisibility(GONE);
        if (holder.wv_doc_preview_image != null) holder.wv_doc_preview_image.setVisibility(GONE);
        if (holder.view_thumb_placeholder != null)
            holder.view_thumb_placeholder.setVisibility(VISIBLE);
    }

    private String callDoc2PdfApiSync(String fileUrl) {
        try {
            java.net.URL apiUrl = new java.net.URL(Constants.doctopdfUrl);
            java.net.HttpURLConnection conn =
                    (java.net.HttpURLConnection) apiUrl.openConnection();
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Content-Type", "application/json");
            conn.setRequestProperty("Authorization", "Bearer " + Constants.TOKEN);
            conn.setDoOutput(true);
            conn.setConnectTimeout(15000);
            conn.setReadTimeout(60000);

            JSONObject body = new JSONObject();
            body.put("url", fileUrl);
            byte[] input = body.toString().getBytes("utf-8");
            conn.getOutputStream().write(input, 0, input.length);
            conn.connect();

            int code = conn.getResponseCode();
            String ctHdr = conn.getContentType();
            if (code == 200) {
                if (ctHdr != null && ctHdr.contains("application/pdf")) {
                    File pdfFile = File.createTempFile(
                            "doc2pdf_" + System.currentTimeMillis(), ".pdf",
                            cContext.getCacheDir());
                    try (InputStream in = new BufferedInputStream(conn.getInputStream());
                         FileOutputStream fo = new FileOutputStream(pdfFile)) {
                        byte[] buf = new byte[4096];
                        int n;
                        while ((n = in.read(buf)) != -1) fo.write(buf, 0, n);
                    }
                    return "localfile://" + pdfFile.getAbsolutePath();
                } else {
                    java.io.BufferedReader reader = new java.io.BufferedReader(
                            new java.io.InputStreamReader(conn.getInputStream()));
                    StringBuilder sb = new StringBuilder();
                    String line;
                    while ((line = reader.readLine()) != null) sb.append(line);
                    reader.close();
                    JSONObject resp = new JSONObject(sb.toString());
                    if (!resp.optBoolean("error", true)) {
                        JSONObject data = resp.optJSONObject("data");
                        if (data != null) return data.optString("url");
                    }
                }
            }
        } catch (Exception e) {
            Log.e("FetchUrl", "doc2pdf failed: " + e.getMessage());
        }
        return null;
    }

    private void dispatchDeletedAction(String action, ViewDocumentsModel m) {
        switch (action) {
            case "Restore":
                eventlistner.ViewDialog(m, "restore");
                break;
            case "Delete":
                eventlistner.ViewDialog(m, "deleted");
                break;
        }
    }

    private void dispatchAction(String action, ViewDocumentsModel m) {
        switch (action) {
            case "View":
                eventlistner.Display_Document(m);
                break;
            case "Edit Info":
                eventlistner.edit_document(m);
                break;
            case "Delete":
                eventlistner.ViewDialog(m, "delete");
                break;
            case "Update Tags":
                eventlistner.Update_Tag(m);
                break;
            case "Download":
                eventlistner.Download_Document(m);
                eventlistner.ViewDialog(m, "download");
                break;
        }
    }

    private void applyLockState(@NonNull MyViewHolder holder, boolean isEncrypted) {
        if (isEncrypted) {
            safeVisible(holder.lock_close);
            safeGone(holder.lock_open);
        } else {
            safeVisible(holder.lock_open);
            safeGone(holder.lock_close);
        }
    }

    private void applyDownloadState(@NonNull MyViewHolder holder, boolean isDisabled) {
        if (isDisabled) {
            safeGone(holder.iv_deleteDisabled);
            safeVisible(holder.iv_deleteEnabled);
        } else {
            safeVisible(holder.iv_deleteDisabled);
            safeGone(holder.iv_deleteEnabled);
        }
    }

    private void applyDisabledState(@NonNull MyViewHolder holder, boolean isDisabled) {
        if (isDisabled) {
            safeSetEnabled(holder.lock_open, false);
            safeSetEnabled(holder.lock_close, false);
            safeSetEnabled(holder.iv_edit_document, false);
            holder.cv_view_documents.setEnabled(false);
            if (holder.ll_view_icons != null) holder.ll_view_icons.setAlpha(0.5f);
        } else {
            safeSetEnabled(holder.lock_open, true);
            safeSetEnabled(holder.lock_close, true);
            safeSetEnabled(holder.iv_edit_document, true);
            holder.cv_view_documents.setEnabled(true);
            if (holder.ll_view_icons != null) holder.ll_view_icons.setAlpha(1.0f);
        }
    }

    private void bindTags(@NonNull MyViewHolder holder, JSONArray tagArray) {
        if (holder.ll_added_tags == null) return;
        if (tagArray != null && tagArray.length() > 0) {
            List<String> valueList = new ArrayList<>();
            for (int i = 0; i < tagArray.length(); i++) {
                try {
                    JSONObject tagObject = tagArray.getJSONObject(i);
                    String tagType = tagObject.optString("key");
                    String tagName = tagObject.optString("value");
                    DocumentsModel tagModel = new DocumentsModel();
                    tagModel.setTag_type(tagType);
                    tagModel.setTag_name(tagName);
                    tags_list.add(tagModel);
                    valueList.add("  " + tagType + "-" + tagName);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
            if (holder.tv_added_tags != null)
                holder.tv_added_tags.setText(String.join(", ", valueList));
            holder.ll_added_tags.setVisibility(VISIBLE);
        } else {
            holder.ll_added_tags.setVisibility(GONE);
        }
    }

    private void attachLegacyClickListeners(@NonNull MyViewHolder holder,
                                            ViewDocumentsModel m) {
        if (holder.lock_open != null)
            holder.lock_open.setOnClickListener(v -> eventlistner.ViewDialog(m, "encrypt"));
        if (holder.lock_close != null)
            holder.lock_close.setOnClickListener(v -> eventlistner.ViewDialog(m, "decrypt"));
        if (holder.iv_deleteEnabled != null)
            holder.iv_deleteEnabled.setOnClickListener(v -> {
                eventlistner.Download_Document(m);
                eventlistner.ViewDialog(m, "enabled");
            });
        if (holder.iv_deleteDisabled != null)
            holder.iv_deleteDisabled.setOnClickListener(v ->
                    eventlistner.ViewDialog(m, "disabled"));
        if (holder.iv_edit_document != null)
            holder.iv_edit_document.setOnClickListener(v -> eventlistner.edit_document(m));
        if (holder.uv_delete_document != null)
            holder.uv_delete_document.setOnClickListener(v ->
                    eventlistner.ViewDialog(m, "delete"));
        if (holder.tag_icon != null)
            holder.tag_icon.setOnClickListener(v -> eventlistner.Update_Tag(m));
        if (holder.download_icon != null)
            holder.download_icon.setOnClickListener(v -> {
                eventlistner.Download_Document(m);
                eventlistner.ViewDialog(m, "download");
            });
    }

    private void applyHighlight(@NonNull MyViewHolder holder, ViewDocumentsModel model) {
        if (highlightIds != null && highlightIds.contains(model.getId())) {
            holder.cv_view_documents.setBackground(
                    ContextCompat.getDrawable(holder.itemView.getContext(),
                            R.drawable.blue_stroke_card));
            holder.cv_view_documents.setCardElevation(8f);
            new Handler(Looper.getMainLooper()).postDelayed(() ->
                    holder.cv_view_documents.setBackground(
                            ContextCompat.getDrawable(holder.itemView.getContext(),
                                    R.drawable.rectangular_white_background)), 5000);
        } else {
            holder.cv_view_documents.setCardBackgroundColor(
                    cContext.getResources().getColor(android.R.color.white));
            holder.cv_view_documents.setCardElevation(4f);
        }
    }

    private void safeVisible(View v) {
        if (v != null) v.setVisibility(VISIBLE);
    }

    private void safeGone(View v) {
        if (v != null) v.setVisibility(GONE);
    }

    private void safeSetEnabled(View v, boolean e) {
        if (v != null) v.setEnabled(e);
    }

    public void setData(ArrayList<ViewDocumentsModel> newData) {
        itemsArrayList = newData;
        notifyDataSetChanged();
    }

    @Override
    public int getItemCount() {
        return itemsArrayList == null ? 0 : itemsArrayList.size();
    }

    // ─────────────────────────────────────────────────────────────────────────
    // VIEW HOLDER
    // ─────────────────────────────────────────────────────────────────────────

    public class MyViewHolder extends RecyclerView.ViewHolder {

        CardView cv_view_documents;
        LinearLayout ll_view_icons;
        TextView tv_client_name;
        TextView tv_doc_description;
        TextView tv_created_date;

        // custom_spinner_cardview is now ONLY an anchor for the PopupWindow;
        // action_list_card and sp_action are kept for list/deleted view types
        // that still use the inline XML approach (no ghost-space issue there).
        ImageView custom_spinner_cardview;
        CardView action_list_card;
        ListView sp_action;

        ImageView iv_doc_image;
        ImageView iv_edit_document;
        ImageView uv_delete_document;
        ImageView download_icon;
        ImageView tag_icon;
        ImageView lock_open;
        ImageView lock_close;
        ImageView iv_deleteEnabled;
        ImageView iv_deleteDisabled;
        LinearLayout ll_added_tags;
        TextView tv_tags;
        TextView tv_added_tags;
        TextView tv_document_display_name;
        TextView tv_client_name_one;
        TextView tv_image_name;
        TextView tv_Expiration;
        TextView tv_Expiration_date;

        ImageView wv_doc_preview_image;
        WebView wv_doc_preview_webview;

        ProgressBar pb_doc_loading;
        View view_thumb_placeholder;
        String currentDocId = "";

        ShapeableImageView siv_profile_icon;
        android.widget.RelativeLayout rl_deletedView;
        ImageView delete_icon;
        ImageView restore_icon;
        TextView tv_deleted_on;
        TextView tv_deleted_on_date;
        TextView tv_doc_type;
        TextView tv_doc_type_name;
        TextView tv_deleted_by;
        TextView tv_deleted_by_date;

        @SuppressLint("SetJavaScriptEnabled")
        public MyViewHolder(@NonNull View itemView, int viewType) {
            super(itemView);

            switch (viewType) {

                case VIEW_TYPE_DELETED:
                    cv_view_documents = itemView.findViewById(R.id.cv_view_documents);
                    ll_view_icons = itemView.findViewById(R.id.ll_view_icons);
                    rl_deletedView = itemView.findViewById(R.id.rl_deletedView);
                    tv_client_name = itemView.findViewById(R.id.tv_client_name);
                    if (tv_client_name != null) tv_client_name.setTextSize(DynamicUtils.fifteen);
                    tv_doc_description = itemView.findViewById(R.id.tv_doc_description);
                    if (tv_doc_description != null)
                        tv_doc_description.setTextSize(DynamicUtils.fifteen);
                    tv_created_date = itemView.findViewById(R.id.tv_created_date);
                    if (tv_created_date != null) tv_created_date.setText(R.string.date);
                    tv_deleted_by = itemView.findViewById(R.id.tv_deleted_by);
                    if (tv_deleted_by != null) tv_deleted_by.setText(R.string.deleted_by);
                    tv_deleted_by_date = itemView.findViewById(R.id.tv_deleted_by_date);
                    tv_deleted_on = itemView.findViewById(R.id.tv_deleted_on);
                    if (tv_deleted_on != null) tv_deleted_on.setText(R.string.deleted_on);
                    tv_deleted_on_date = itemView.findViewById(R.id.tv_deleted_on_date);
                    tv_doc_type = itemView.findViewById(R.id.tv_doc_type);
                    if (tv_doc_type != null) tv_doc_type.setText(R.string.document_type);
                    tv_doc_type_name = itemView.findViewById(R.id.tv_doc_type_name);
                    restore_icon = itemView.findViewById(R.id.restore_icon);
                    delete_icon = itemView.findViewById(R.id.delete_icon);
                    custom_spinner_cardview = itemView.findViewById(R.id.custom_spinner_cardview);
                    // action_list_card / sp_action still present in deleted_doc_view XML
                    action_list_card = itemView.findViewById(R.id.action_list_card);
                    sp_action = itemView.findViewById(R.id.action_list);
                    break;

                case VIEW_TYPE_GRID:
                    cv_view_documents = itemView.findViewById(R.id.cv_view_documents);
                    ll_view_icons = itemView.findViewById(R.id.ll_view_icons);
                    wv_doc_preview_image = itemView.findViewById(R.id.wv_doc_preview);
                    wv_doc_preview_webview = itemView.findViewById(R.id.wv_doc_preview_web);
                    iv_doc_image = itemView.findViewById(R.id.iv_doc_image);
                    pb_doc_loading = itemView.findViewById(R.id.pb_doc_loading);
                    view_thumb_placeholder = itemView.findViewById(R.id.view_thumb_placeholder);
                    tv_document_display_name = itemView.findViewById(R.id.tv_document_display_name);
                    if (tv_document_display_name != null) {
                        tv_document_display_name.setMaxLines(2);
                        tv_document_display_name.setEllipsize(TextUtils.TruncateAt.END);
                    }
                    tv_created_date = itemView.findViewById(R.id.tv_created_date);
                    lock_open = itemView.findViewById(R.id.lock_open);
                    lock_close = itemView.findViewById(R.id.lock_close);
                    iv_deleteEnabled = itemView.findViewById(R.id.iv_deleteEnabled);
                    iv_deleteDisabled = itemView.findViewById(R.id.iv_deleteDisabled);
                    ll_added_tags = itemView.findViewById(R.id.ll_added_tags);
                    tv_added_tags = itemView.findViewById(R.id.tv_added_tags);
                    tv_tags = itemView.findViewById(R.id.tv_tags);
                    if (tv_tags != null) tv_tags.setText(R.string.tags);
                    // The three-dot spinner anchor — popup floats from here
                    custom_spinner_cardview = itemView.findViewById(R.id.custom_spinner_cardview);
                    // action_list_card / sp_action have been REMOVED from
                    // view_documents_grid_item.xml — these will be null for grid items,
                    // which is intentional. The PopupWindow replaces them entirely.
                    action_list_card = itemView.findViewById(R.id.action_list_card);
                    sp_action = itemView.findViewById(R.id.action_list);
                    tv_client_name = itemView.findViewById(R.id.tv_client_name);
                    tv_client_name_one = itemView.findViewById(R.id.tv_client_name_one);
                    tv_image_name = itemView.findViewById(R.id.tv_image_name);
                    tv_doc_description = itemView.findViewById(R.id.tv_doc_description);
                    tv_Expiration = itemView.findViewById(R.id.tv_Expiration);
                    tv_Expiration_date = itemView.findViewById(R.id.tv_Expiration_date);
                    download_icon = itemView.findViewById(R.id.download_icon);
                    iv_edit_document = itemView.findViewById(R.id.iv_edit_document);
                    uv_delete_document = itemView.findViewById(R.id.uv_delete_document);
                    tag_icon = itemView.findViewById(R.id.tag_icon);

                    if (wv_doc_preview_webview != null) {
                        WebSettings ws = wv_doc_preview_webview.getSettings();
                        ws.setJavaScriptEnabled(true);
                        ws.setLoadWithOverviewMode(true);
                        ws.setUseWideViewPort(true);
                        ws.setBuiltInZoomControls(false);
                        ws.setDisplayZoomControls(false);
                        ws.setDomStorageEnabled(true);
                        ws.setSupportZoom(false);
                        ws.setMixedContentMode(WebSettings.MIXED_CONTENT_ALWAYS_ALLOW);
                        ws.setLoadsImagesAutomatically(true);
                        wv_doc_preview_webview.setBackgroundColor(
                                ContextCompat.getColor(itemView.getContext(),
                                        android.R.color.white));
                        wv_doc_preview_webview.setScrollBarStyle(
                                View.SCROLLBARS_INSIDE_OVERLAY);
                        wv_doc_preview_webview.setHorizontalScrollBarEnabled(false);
                        wv_doc_preview_webview.setVerticalScrollBarEnabled(false);
                        wv_doc_preview_webview.setOnTouchListener((v, ev) -> true);
                    }

                    if (itemView instanceof ViewGroup) {
                        ((ViewGroup) itemView).setClipChildren(false);
                        ((ViewGroup) itemView).setClipToPadding(false);
                    }
                    break;

                case VIEW_TYPE_LIST:
                default:
                    cv_view_documents = itemView.findViewById(R.id.cv_view_documents);
                    ll_view_icons = itemView.findViewById(R.id.ll_view_icons);
                    ll_added_tags = itemView.findViewById(R.id.ll_added_tags);
                    iv_doc_image = itemView.findViewById(R.id.iv_doc_image);
                    tv_document_display_name = itemView.findViewById(R.id.tv_document_display_name);
                    if (tv_document_display_name != null) {
                        tv_document_display_name.setMaxLines(2);
                        tv_document_display_name.setEllipsize(TextUtils.TruncateAt.END);
                    }
                    tv_client_name = itemView.findViewById(R.id.tv_client_name);
                    if (tv_client_name != null) tv_client_name.setTextSize(DynamicUtils.fifteen);
                    tv_client_name_one = itemView.findViewById(R.id.tv_client_name_one);
                    if (tv_client_name_one != null)
                        tv_client_name_one.setTextSize(DynamicUtils.fifteen);
                    tv_image_name = itemView.findViewById(R.id.tv_image_name);
                    if (tv_image_name != null) {
                        tv_image_name.setMaxLines(1);
                        tv_image_name.setEllipsize(TextUtils.TruncateAt.END);
                    }
                    tv_doc_description = itemView.findViewById(R.id.tv_doc_description);
                    if (tv_doc_description != null)
                        tv_doc_description.setTextSize(DynamicUtils.fifteen);
                    tv_created_date = itemView.findViewById(R.id.tv_created_date);
                    if (tv_created_date != null) tv_created_date.setText(R.string.date);
                    tv_Expiration = itemView.findViewById(R.id.tv_Expiration);
                    if (tv_Expiration != null) tv_Expiration.setText(R.string.expiration_);
                    tv_Expiration_date = itemView.findViewById(R.id.tv_Expiration_date);
                    tv_tags = itemView.findViewById(R.id.tv_tags);
                    if (tv_tags != null) tv_tags.setText(R.string.tags);
                    tv_added_tags = itemView.findViewById(R.id.tv_added_tags);
                    lock_open = itemView.findViewById(R.id.lock_open);
                    lock_close = itemView.findViewById(R.id.lock_close);
                    iv_deleteEnabled = itemView.findViewById(R.id.iv_deleteEnabled);
                    iv_deleteDisabled = itemView.findViewById(R.id.iv_deleteDisabled);
                    download_icon = itemView.findViewById(R.id.download_icon);
                    iv_edit_document = itemView.findViewById(R.id.iv_edit_document);
                    uv_delete_document = itemView.findViewById(R.id.uv_delete_document);
                    tag_icon = itemView.findViewById(R.id.tag_icon);
                    custom_spinner_cardview = itemView.findViewById(R.id.custom_spinner_cardview);
                    action_list_card = itemView.findViewById(R.id.action_list_card);
                    sp_action = itemView.findViewById(R.id.action_list);
                    break;
            }
        }
    }
}