package com.digicoffer.lauditor.DocEditor;

import static android.Manifest.permission.READ_EXTERNAL_STORAGE;
import static android.Manifest.permission.READ_MEDIA_IMAGES;
import static android.Manifest.permission.READ_MEDIA_VIDEO;
import static android.Manifest.permission.READ_MEDIA_VISUAL_USER_SELECTED;
import static android.view.View.GONE;
import static android.view.View.VISIBLE;

import static com.digicoffer.lauditor.DocEditor.Utility_Methods.extractBetween;
import static com.digicoffer.lauditor.DocEditor.Utility_Methods.extractCaption;
import static com.digicoffer.lauditor.DocEditor.Utility_Methods.extractImagePath;
import static com.digicoffer.lauditor.DocEditor.Utility_Methods.extractListItems;
import static com.digicoffer.lauditor.DocEditor.Utility_Methods.extractTableData;
import static com.digicoffer.lauditor.CommonFiles.GlobalFiles.Constants.Delete_doc;
import static com.digicoffer.lauditor.CommonFiles.GlobalFiles.Constants.DocEditorListingUrl;
import static com.digicoffer.lauditor.CommonFiles.GlobalFiles.Constants.DocEditorUploadImageURL;
import static com.digicoffer.lauditor.CommonFiles.GlobalFiles.Constants.LatexDocFile;
import static com.digicoffer.lauditor.CommonFiles.GlobalFiles.Constants.OpenView_doc;
import static com.digicoffer.lauditor.CommonFiles.GlobalFiles.Constants.saveAsLatexDoc;
import static com.digicoffer.lauditor.CommonFiles.GlobalFiles.Constants.saveLatexDoc;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.content.res.ColorStateList;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.OpenableColumns;
import android.text.Editable;
import android.text.InputFilter;
import android.text.TextWatcher;
import android.util.Log;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.webkit.WebView;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatButton;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.digicoffer.lauditor.DocEditor.Structure_Payload.DocumentPayloadBuilder;
import com.digicoffer.lauditor.DocEditor.Structure_Payload.FieldContentModel;
import com.digicoffer.lauditor.DocEditor.Structure_Payload.LaTeXUtils;
import com.digicoffer.lauditor.Documents.Models.DocumentsModel;
import com.digicoffer.lauditor.CommonFiles.GlobalFiles.NewModel;
import com.digicoffer.lauditor.R;
import com.digicoffer.lauditor.Webservice.AsyncTaskCompleteListener;
import com.digicoffer.lauditor.Webservice.HttpResultDo;
import com.digicoffer.lauditor.Webservice.CommonApiHelper.WebServiceHelper;
import com.digicoffer.lauditor.CommonFiles.GlobalFiles.AndroidUtils;
import com.digicoffer.lauditor.CommonFiles.FileSelection.BottomSheetUploadFile;
import com.digicoffer.lauditor.CommonFiles.GlobalFiles.Constants;
import com.digicoffer.lauditor.CommonFiles.ValidationUtils.ContentValidation;
import com.digicoffer.lauditor.CommonFiles.PdfUtils.RetrievePDFfromUrl;
import com.digicoffer.lauditor.CommonFiles.ValidationUtils.Validation;
import com.github.barteksc.pdfviewer.PDFView;
import com.google.android.material.textfield.TextInputEditText;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class DocEditor extends Fragment implements AsyncTaskCompleteListener, ViewDocAdapter.InterfaceListener, DocumentRecyclerAdapter.EventListeners, BottomSheetUploadFile.OnPhotoSelectedListner {
    TextView tv_create, tv_view, tv_list_doc, tv_file, tv_insert, tv_doc_name, tv_title, tv_author;
    Dialog progress_dialog;
    LatexDocumentResponse latexDocumentResponse = new LatexDocumentResponse();
    LinearLayout ll_view_doc, ll_create_view, ll_create_document;
    TextInputEditText et_title, et_author, et_search_document, et_search;
    RecyclerView rv_documents;
    private NewModel mViewModel;
    AlertDialog dialog;
    private TextView tempTvImageName;
    private ImageView tempImageView;
    private LinearLayout tempLlImageView;
    private File tempFile;
    private static final int MAX_COLUMNS = 5;
    //    private Button btn_addColumn;
    String NewDocName = "";
    Boolean isFileOption = true, isInsertOption = true;
    RecyclerView rv_viewDocEditor;
    private Bitmap mSelectedBitmap;
    TextView tv_image_name;
    LinearLayout ll_image_view;
    private File mSelectedUri;
    private ImageView imageView, iv_eye_icon;
    File file;
    AlertDialog dialog1;
    ViewDocAdapter adapter;
    RecyclerView rv_open_document;
    boolean isView = false;
    LinearLayout tv_switchCreate, tv_switchView;
    final boolean[] columnsManuallyAdded = {false};
    BottomSheetUploadFile bottommSheetUploadDocument;
    List<DocumentModel> docs = new ArrayList<>();
    String oldDocId = "";
    String oldDocName = "";
    ArrayList<DocListingModel> view_doc_list = new ArrayList<>();
    private DeleteTargetType pendingDeleteType = null;
    private View pendingDeleteView = null;
    private int pendingColumnIndex = -1;
    private LinearLayout pendingRowLayout = null;
    private View ImageLayout = null;
    InputFilter[] filters, filters1;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.doc_editor, container, false);
        mViewModel = new ViewModelProvider(requireActivity()).get(NewModel.class);
        mViewModel.setData(getString(R.string.doc_editor));
        tv_switchView = v.findViewById(R.id.tv_switchView);
        tv_switchCreate = v.findViewById(R.id.tv_switchCreate);
        tv_create = v.findViewById(R.id.tv_create);
        tv_create.setText(R.string.create);
        tv_file = v.findViewById(R.id.tv_file);
        tv_file.setText(R.string.file);
        tv_insert = v.findViewById(R.id.tv_insert);
        tv_insert.setText(R.string.insert);
        tv_doc_name = v.findViewById(R.id.tv_doc_name);
        tv_doc_name.setText(R.string.untitled_document);
        tv_title = v.findViewById(R.id.tv_title);
        tv_title.setText(R.string.title);
        tv_author = v.findViewById(R.id.tv_author);
        tv_author.setText(R.string.author);
        ll_create_view = v.findViewById(R.id.ll_create_view);
        ll_create_document = v.findViewById(R.id.ll_create_document);
        et_title = v.findViewById(R.id.et_title);
        et_title.setHint(R.string.enter_the_title);
        et_author = v.findViewById(R.id.et_author);
        et_author.setHint(R.string.enter_the_author);

        tv_view = v.findViewById(R.id.tv_view);
        tv_view.setText(R.string.view);
        ll_view_doc = v.findViewById(R.id.ll_view_doc);
        tv_list_doc = v.findViewById(R.id.tv_list_doc);
        tv_list_doc.setTextColor(Color.WHITE);
        tv_list_doc.setText(R.string.list_of_created_documents);
        tv_list_doc.setVisibility(GONE);
        rv_viewDocEditor = v.findViewById(R.id.rv_viewDocEditor);
        rv_documents = v.findViewById(R.id.rv_documents);
        rv_documents.setVisibility(GONE);
        iv_eye_icon = v.findViewById(R.id.iv_eye_icon);
        iv_eye_icon.setImageTintList(ColorStateList.valueOf(ContextCompat.getColor(requireContext(), R.color.blue)));
        et_search_document = v.findViewById(R.id.et_search_document);
        et_search_document.setHint(R.string.search_document);

        loadCreate();
        et_title.addTextChangedListener(new ContentValidation(et_title, false));
        et_author.addTextChangedListener(new ContentValidation(et_author, false));
        filters = new InputFilter[]{
                new InputFilter.LengthFilter(255)
        };
        filters1 = new InputFilter[]{
                new InputFilter.LengthFilter(50)
        };
        et_title.setFilters(filters);
        et_author.setFilters(filters1);
        et_title.setMaxLines(1);
        et_author.setMaxLines(1);
        et_search_document.addTextChangedListener(new Validation(et_search_document));

        tv_create.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!Constants.is_active) {
                    AndroidUtils.showRenewalPopup(getActivity());
                } else {
                    loadCreate();
                }
            }
        });
        tv_view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                loadView();
            }
        });
        AndroidUtils.setupModuleView(
                tv_switchCreate,
                getString(R.string.view_document),
                false, true, getContext(), getString(R.string.create_document),
                clickedView -> {
                    // handle click
                    loadView();
                }
        );
        AndroidUtils.setupModuleView(
                tv_switchView,
                getString(R.string.create_document),
                true, true, getContext(), getString(R.string.list_of_created_documents),
                clickedView -> {
                    if (!Constants.is_active) {
                        AndroidUtils.showRenewalPopup(getActivity());
                    } else {
                        loadCreate();
                    }
                }
        );
        tv_file.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                isInsertOption = true;
                tv_insert.setTextColor(requireContext().getColor(R.color.black));
                if (isFileOption) {
                    tv_file.setTextColor(requireContext().getColor(R.color.blue));
                    docs.clear();
                    docs.add(new DocumentModel(getString(R.string.new_), R.drawable.new_file_de));
                    docs.add(new DocumentModel(getString(R.string.open), R.drawable.open_file_de));
                    docs.add(new DocumentModel(getString(R.string.save), R.drawable.save));
                    docs.add(new DocumentModel(getString(R.string.save_as), R.drawable.save_as));
                    docs.add(new DocumentModel(getString(R.string.delete), R.drawable.delete_de));
                    loadGridView();
                } else {
                    tv_file.setTextColor(requireContext().getColor(R.color.black));
                    rv_documents.setVisibility(GONE);
                }
                isFileOption = !isFileOption;
            }
        });
        tv_insert.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                isFileOption = true;
                tv_file.setTextColor(requireContext().getColor(R.color.black));
                if (isInsertOption) {
                    tv_insert.setTextColor(requireContext().getColor(R.color.blue));
                    docs.clear();
                    docs.add(new DocumentModel(getString(R.string.overview), R.drawable.overview_de));
                    docs.add(new DocumentModel(getString(R.string.section), R.drawable.section));
                    docs.add(new DocumentModel(getString(R.string.sub_section), R.drawable.sub_section_de));
                    docs.add(new DocumentModel(getString(R.string.sub_sub_section), R.drawable.sub_sub_section_de));
                    docs.add(new DocumentModel(getString(R.string.paragraph), R.drawable.paragraph_de));
                    docs.add(new DocumentModel(getString(R.string.numbered_list), R.drawable.number_list_de));
                    docs.add(new DocumentModel(getString(R.string.bulleted_list), R.drawable.bulleted_list_de));
                    docs.add(new DocumentModel(getString(R.string.page_break), R.drawable.pagebreak_de));
                    docs.add(new DocumentModel(getString(R.string.image), R.drawable.image_de));
                    docs.add(new DocumentModel(getString(R.string.table), R.drawable.table_de));
                    loadGridView();
                } else {
                    tv_insert.setTextColor(requireContext().getColor(R.color.black));
                    rv_documents.setVisibility(GONE);
                }
                isInsertOption = !isInsertOption;
            }
        });
        iv_eye_icon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!oldDocId.isEmpty()) {
                    ViewDoc("", oldDocId);
                } else {
                    AndroidUtils.showError("Please save the document", getActivity());
                }
            }
        });
        return v;
    }

    private void loadSearch(TextInputEditText et_search_document) {
        et_search_document.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                adapter.getFilter().filter(Objects.requireNonNull(et_search_document.getText()).toString());
            }
        });
    }

    private void loadGridView() {
        // 4 items per row
        GridLayoutManager layoutManager = new GridLayoutManager(getContext(), 4);
        rv_documents.setLayoutManager(layoutManager);

        // Optimize scrolling behavior
        rv_documents.setNestedScrollingEnabled(false);
        rv_documents.setHasFixedSize(false);

        // Set adapter
        DocumentRecyclerAdapter adapter = new DocumentRecyclerAdapter(getContext(), docs, this);
        rv_documents.setAdapter(adapter);
        rv_documents.setVisibility(VISIBLE);
    }


    private void loadCreate() {
        tv_switchCreate.setVisibility(VISIBLE);
        tv_switchView.setVisibility(GONE);
        tv_create.setTextColor(requireContext().getColor(R.color.white));
        tv_view.setTextColor(requireContext().getColor(R.color.black));
        tv_create.setBackgroundDrawable(getContext().getResources().getDrawable(R.drawable.button_left_green_background));
        tv_view.setBackgroundDrawable(getContext().getResources().getDrawable(R.drawable.button_right_background));
        ll_view_doc.setVisibility(GONE);
        ll_create_view.setVisibility(VISIBLE);
        et_search_document.setText("");
//        clearViews();
        isFileOption = true;
        isInsertOption = true;
        isView = false;
    }

    public void loadView() {
        tv_switchCreate.setVisibility(GONE);
        tv_switchView.setVisibility(VISIBLE);
        tv_create.setTextColor(requireContext().getColor(R.color.black));
        tv_view.setTextColor(requireContext().getColor(R.color.white));
        tv_create.setBackgroundDrawable(getContext().getResources().getDrawable(R.drawable.button_left_background));
        tv_view.setBackgroundDrawable(getContext().getResources().getDrawable(R.drawable.button_right_green_background));
        ll_view_doc.setVisibility(VISIBLE);
        ll_create_view.setVisibility(GONE);
        loadDocumentList();
        clearViews();
        oldDocId = "";
        oldDocName = "";
        clearNames();
        rv_documents.setVisibility(GONE);
        isFileOption = true;
        isInsertOption = true;
        tv_insert.setTextColor(requireContext().getColor(R.color.black));
        tv_file.setTextColor(requireContext().getColor(R.color.black));
        isView = true;
    }

    private void loadDocumentList() {
        try {
            progress_dialog = AndroidUtils.get_progress(getActivity());
            JSONObject postdata = new JSONObject();
            WebServiceHelper.callHttpWebService(this, getContext(), WebServiceHelper.RestMethodType.GET, DocEditorListingUrl, "Get DocEditor List", postdata.toString());
        } catch (Exception e) {
            if (progress_dialog != null && progress_dialog.isShowing())
                AndroidUtils.dismiss_dialog(progress_dialog);
        }
    }

    private boolean isFileSizeValid(File file) {
        final long MAX_SIZE_MB = 2;
        long fileSizeInMB = file.length() / (1024 * 1024);
        return fileSizeInMB <= MAX_SIZE_MB;
    }

    @Override
    public void getImagepath(File imagepath, Uri ImageURI) throws IOException {
        if ((imagepath == null)) {
            mSelectedBitmap = null;
            mSelectedUri = imagepath;
            String uri = imagepath.toString();
            ImageLoader imageLoader = ImageLoader.getInstance();
            imageLoader.init(ImageLoaderConfiguration.createDefault(getActivity()));
            imageLoader.displayImage(String.valueOf(Uri.fromFile(new File(uri))), imageView);
            file = imagepath;

            Cursor c = getContext().getContentResolver().query(ImageURI, null, null, null, null);
            c.moveToFirst();
            String[] content_type = file.getName().split(".");
            @SuppressLint("Range") String file_name = c.getString(c.getColumnIndex(OpenableColumns.DISPLAY_NAME));
            tempTvImageName.setText(file_name);
            if (isFileSizeValid(file)) {
                tempLlImageView.setVisibility(VISIBLE);
                callUploadImage(file);
            } else {
                AndroidUtils.showError("File size exceeds 2MB.", getActivity());
                return;
            }

        } else {
            file = getFile(requireContext(), ImageURI);
            Log.i("FILE", "Info:" + file);
            String file_name = file.getName();
            tempTvImageName.setText(file_name);
            if (isFileSizeValid(file)) {
                tempLlImageView.setVisibility(VISIBLE);
                callUploadImage(file);
            } else {
                AndroidUtils.showError("File size exceeds 2MB.", getActivity());
                return;
            }
        }
        ll_create_view.setAlpha(1.0f);
        bottommSheetUploadDocument.dismiss();
    }

    @Override
    public void getImageBitmap(Bitmap bitmap) {
        imageView.setImageBitmap(bitmap);
        mSelectedBitmap = bitmap;
        mSelectedUri = null;
//        tv_upload_file.setEnabled(false);
        File filesDir = getContext().getFilesDir();
        File imageFile = new File(filesDir, "bitmap" + ".jpg");
        OutputStream os;
        try {
            os = new FileOutputStream(imageFile);
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, os);
            os.flush();
            os.close();
            file = imageFile;
            tempTvImageName.setText(file.getName());
            if (isFileSizeValid(file)) {
                tempLlImageView.setVisibility(VISIBLE);
                callUploadImage(file);
            } else {
                AndroidUtils.showError("File size exceeds 2MB.", getActivity());
                return;
            }

            DocumentsModel documentsModel = new DocumentsModel();
            documentsModel.setName(file.getName());
//            docsList.add(documentsModel);
        } catch (Exception e) {
            Log.e(getClass().getSimpleName(), "Error writing bitmap", e);
        }
    }

    @Override
    public void onClick(View view) {

    }

    public static File getFile(Context context, Uri uri) throws IOException {
        File destinationFilename = new File(context.getFilesDir().getPath() + File.separatorChar + queryName(context, uri));
        try (InputStream ins = context.getContentResolver().openInputStream(uri)) {
            createFileFromStream(ins, destinationFilename);
        } catch (Exception ex) {
            Log.e("Save File", ex.getMessage());
            ex.fillInStackTrace();
        }
        return destinationFilename;
    }

    private static String queryName(Context context, Uri uri) {
        Cursor returnCursor = context.getContentResolver().query(uri, null, null, null, null);
        assert returnCursor != null;
        int nameIndex = returnCursor.getColumnIndex(OpenableColumns.DISPLAY_NAME);
        returnCursor.moveToFirst();
        String name = returnCursor.getString(nameIndex);
        returnCursor.close();
        return name;
    }

    public static void createFileFromStream(InputStream ins, File destination) {
        try (OutputStream os = new FileOutputStream(destination)) {
            byte[] buffer = new byte[4096];
            int length;
            while ((length = ins.read(buffer)) > 0) {
                os.write(buffer, 0, length);
            }
            os.flush();
        } catch (Exception ex) {
            Log.e("Save File", ex.getMessage());
            ex.fillInStackTrace();
        }
    }

    @Override
    public void onAsyncTaskComplete(HttpResultDo httpResult) {
        if (progress_dialog != null && progress_dialog.isShowing())
            AndroidUtils.dismiss_dialog(progress_dialog);
        if (httpResult.getResult() == WebServiceHelper.ServiceCallStatus.Success) {
            try {
                if (httpResult.getRequestType().equals("Delete DocEditor List")) {
                    JSONObject result = new JSONObject(httpResult.getResponseContent());
                    String message = result.optString("message");
                    AndroidUtils.showAlert("Document deleted successfully", getActivity());
                    if (isView) {
                        loadDocumentList();
                    } else {
                        clearViews();
                        clearNames();
                    }
                } else if (httpResult.getRequestType().equals("Open Document")) {
                    JSONArray result = new JSONArray(httpResult.getResponseContent());
                    JSONObject jsonObject = result.optJSONObject(0);
                    if (jsonObject.has("createdon")) {
                        latexDocumentResponse.setCreatedon(Objects.requireNonNull(jsonObject.optJSONObject("createdon")).optString("$date"));
                    }
                    latexDocumentResponse.setDocument(jsonObject.optString("document"));
                    latexDocumentResponse.setDocid(jsonObject.optString("docid"));
                    latexDocumentResponse.setPage(jsonObject.optInt("page"));
                    latexDocumentResponse.setPageid(jsonObject.optString("pageid"));
                    if (jsonObject.has("updatedon")) {
                        latexDocumentResponse.setUpdatedon(Objects.requireNonNull(jsonObject.optJSONObject("updatedon")).optString("$date"));
                    }
                    latexDocumentResponse.setUserid(jsonObject.optString("userid"));
                    String documentLatex = jsonObject.optString("document");
                    loadDocumentFromPayload(documentLatex);
                    oldDocId = jsonObject.optString("docid");
                } else if (httpResult.getRequestType().equals("Upload DocEditor File")) {
                    JSONObject result = new JSONObject(httpResult.getResponseContent());
                    String message = result.optString("message");
                    AndroidUtils.showSuccess("Image Uploaded Successfully", getActivity());
//                    loadDocumentList();
                } else if (httpResult.getRequestType().equals("Save Document")) {
                    JSONObject result = new JSONObject(httpResult.getResponseContent());
                    String message = result.optString("message");
                    String docId = result.optString("id");
                    oldDocId = docId;
                    latexDocumentResponse.setDocid(docId);
//                    latexDocumentResponse.setPageid(docId);
//                    AndroidUtils.showAlert(message, getActivity());
                    if (docId.isEmpty()) {
                        AndroidUtils.showAlert(message, getActivity());
                    } else {
                        triggerSavePayload(docId);
                        dialog.dismiss();
                    }
                } else if (httpResult.getRequestType().equals("Save As Document")) {
                    JSONObject result = new JSONObject(httpResult.getResponseContent());
                    String message = result.optString("message");
                    String docId = result.optString("id");
//                    oldDocId = docId;
//                    latexDocumentResponse.setDocid(docId);
                    if (docId.isEmpty()) {
                        AndroidUtils.showAlert(message, getActivity());
                    } else {
                        AndroidUtils.showSaveAsConfirmation(getActivity(), this, NewDocName);
                        dialog.dismiss();
                    }
//                    if (!docId.isEmpty()) {
//                        triggerSavePayload(docId);
//                    }
                } else if (httpResult.getRequestType().equals("Save DocEditor")) {
                    JSONObject result = new JSONObject(httpResult.getResponseContent());
                    String message = result.optString("message");
                    String pageid = result.optString("id");
                    if ((latexDocumentResponse.getPageid() != null) && (!latexDocumentResponse.getPageid().isEmpty())) {
//                        TextView title = dialog.findViewById(R.id.alert_title);
//                        title.setText("Success");
                        AndroidUtils.showAlert_docs("Success", "Document updated successfully.", getActivity());
                    } else {
//                        TextView title = dialog.findViewById(R.id.alert_title);
//                        title.setText("Success");
                        AndroidUtils.showAlert_docs("Success", "Document saved successfully.", getActivity());
                        latexDocumentResponse.setPageid(pageid);
                    }
                    if (!oldDocName.isEmpty()) {
                        tv_doc_name.setText(oldDocName);
                    } else {
                        tv_doc_name.setText(R.string.untitled_document);
                    }
                } else if (httpResult.getRequestType().equals("Get DocEditor List")) {
                    JSONArray result = new JSONArray(httpResult.getResponseContent());
                    ListingDoc(result);
                }
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
        else if (httpResult.getResult() == WebServiceHelper.ServiceCallStatus.Failed) {
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

    private void ListingDoc(JSONArray list) {
        view_doc_list.clear();
        for (int i = 0; i < list.length(); i++) {
            DocListingModel docListingModel = new DocListingModel();
            JSONObject jsonObject = list.optJSONObject(i);
            docListingModel.setDocid(jsonObject.optString("docid"));
            docListingModel.setDocumentname(jsonObject.optString("documentname"));
            docListingModel.setUpdatedon(jsonObject.optJSONObject("updatedon"));
            docListingModel.set$date(docListingModel.getUpdatedon().optString("$date"));
            view_doc_list.add(docListingModel);
        }
        if (isView) {
            loadRecyclerView(rv_viewDocEditor);
        } else {
            loadRecyclerView(rv_open_document);
        }
    }

    public void OpenDoc(DocListingModel docListingModel) {
        clearNames();
        oldDocName = docListingModel.getDocumentname();
        String url = Delete_doc + docListingModel.docid;
        callOpenDocument(url);
        CloseView();
    }

    @Override
    public void ViewDoc(String documentname, String docid) {
//        67ac5210e4e7872eb77283f3
        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(getActivity());
        LayoutInflater inflater = getActivity().getLayoutInflater();
        View view = inflater.inflate(R.layout.view_documents, null);
        ProgressBar progressBar = view.findViewById(R.id.progress_pdf);
        ImageView iv_image = view.findViewById(R.id.doc_image);
        PDFView idPDFView = view.findViewById(R.id.idPDFView);
        WebView webView = view.findViewById(R.id.doc_webview);
        TextView header = view.findViewById(R.id.header_name);
        ImageView iv_close_edit_docs = view.findViewById(R.id.close_edit_docs);
        // 4️⃣ Create dialog
        final AlertDialog dialog = dialogBuilder.create();

        // 5️⃣ Close button
        final RetrievePDFfromUrl[] pdfTask = new RetrievePDFfromUrl[1];

        // Close button safely
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
        if (documentname.isEmpty()) {
            header.setText(R.string.preview);
        } else {
            header.setText(documentname);
        }
        String url = OpenView_doc + docid;
        idPDFView.setVisibility(VISIBLE);
        progressBar.setVisibility(VISIBLE);

        pdfTask[0] = new RetrievePDFfromUrl(idPDFView, progressBar);
        pdfTask[0].execute(url);
        dialog.setCancelable(false);
        dialog.setCanceledOnTouchOutside(false);
        dialog.setView(view);
        dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        dialog.show();
    }

    @Override
    public void DeleteDoc(String docname, String docid) {
        try {
            AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(getActivity());
            LayoutInflater inflater = requireActivity().getLayoutInflater();
            View view = inflater.inflate(R.layout.delete_relationship, null);
            TextView header_name = view.findViewById(R.id.header_name);
            header_name.setText(R.string.confirmation);
            ImageView close_documents = view.findViewById(R.id.close_documents);
            header_name.setTextColor(Color.BLACK);
            TextView tv_confirmation = view.findViewById(R.id.tv_confirmation);
            String delete_msg = "Are you sure you want to delete " + docname + " document?";

            tv_confirmation.setText(delete_msg);
            AppCompatButton bt_yes = view.findViewById(R.id.btn_yes);
            bt_yes.setBackgroundDrawable(getContext().getDrawable(R.drawable.yes_button_red_button));
            AppCompatButton btn_no = view.findViewById(R.id.btn_No);
            btn_no.setBackgroundDrawable(getContext().getDrawable(R.drawable.no_button_green_button));

//            ll_view_docs.setEnabled(false);
            final AlertDialog dialog = dialogBuilder.create();
//            ad_dialog_delete = dialog;
            // Ensure cl_document's alpha is reset when the dialog is dismissed.
//            dialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
//                @Override
//                public void onDismiss(DialogInterface dialog) {
//                    cl_document.setAlpha(1.0f);
//                }
//            });
            btn_no.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    dialog.dismiss();
                }
            });
            close_documents.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    dialog.dismiss();
                }
            });
            bt_yes.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    dialog.dismiss();
                    callDeleteDocumentWebservice(docid);
                }
            });
            dialog.setView(view);
            dialog.show();
        } catch (Exception e) {
            AndroidUtils.showAlert(e.getMessage(), getActivity());
        }
    }

    private void callOpenDocument(String url) {
        try {
            progress_dialog = AndroidUtils.get_progress(getActivity());
            JSONObject jsonObject = new JSONObject();
//                https:stagingapi.latex.digicoffer.com/v1/document/67ac5210e4e7872eb77283f3
            jsonObject = new JSONObject();
            WebServiceHelper.callHttpWebService(this, getContext(), WebServiceHelper.RestMethodType.GET, url, "Open Document", jsonObject.toString());
        } catch (Exception e) {
            if (progress_dialog != null && progress_dialog.isShowing()) {
                AndroidUtils.dismiss_dialog(progress_dialog);
            }
        }
    }

    private void callDeleteDocumentWebservice(String docid) {
        try {
            progress_dialog = AndroidUtils.get_progress(getActivity());
            JSONObject jsonObject = new JSONObject();
//                https:stagingapi.latex.digicoffer.com/v1/document/67ac5210e4e7872eb77283f3
            jsonObject = new JSONObject();
            WebServiceHelper.callHttpWebService(this, getContext(), WebServiceHelper.RestMethodType.DELETE, Delete_doc + docid, "Delete DocEditor List", jsonObject.toString());
        } catch (Exception e) {
            if (progress_dialog != null && progress_dialog.isShowing()) {
                AndroidUtils.dismiss_dialog(progress_dialog);
            }
        }
    }

    @Override
    public void AddView(String title, String hint) {
        int staticViewsCount = 5; // Adjust based on your actual layout

        String lastTagType = null;
        boolean overviewExists = false;

// Check if Overview already exists
        for (
                int i = staticViewsCount; i < ll_create_document.getChildCount(); i++) {
            View child = ll_create_document.getChildAt(i);
            TextView tvContent = child.findViewById(R.id.tv_content);
            if (tvContent != null && "Overview".equalsIgnoreCase(String.valueOf(tvContent.getTag()))) {
                overviewExists = true;
                break;
            }
        }

// Get the last added tag (excluding static views)
        for (
                int i = ll_create_document.getChildCount() - 1;
                i >= staticViewsCount; i--) {
            View child = ll_create_document.getChildAt(i);
            TextView tvContent = child.findViewById(R.id.tv_content);
            if (tvContent != null && tvContent.getTag() != null) {
                lastTagType = tvContent.getTag().toString().trim(); // This gives actual type like "Section"
                break;
            }
        }

// === Overview Validation ===
        if (title.equalsIgnoreCase("Overview")) {
            if (overviewExists) {
                AndroidUtils.showError("Only one Overview is allowed", getActivity());
                return;
            }

            if (ll_create_document.getChildCount() > staticViewsCount) {
                AndroidUtils.showError("Overview should be the first item in the document", getActivity());
                return;
            }

            // If validation passes, insert Overview at correct position
            ContentInputViewProvider inputViewProvider = new ContentInputViewProvider(getContext(), ll_create_document, new ContentInputViewProvider.ContentActionListener() {
                @Override
                public void onRemoveClicked(View view) {
                    checkConsecutivePageBreaks();
                }

                @Override
                public void onExpandClicked(String titleTxt, String title, String content, View targetView) {
                    loadExpandView(titleTxt, title, content, targetView, false, true);
                }
            }, this);

            inputViewProvider.AddDocContent(title, title, hint);
            ll_create_document.addView(inputViewProvider.getView(), staticViewsCount);
            return;
        }

// === Sub Section Validation ===
        if (title.equalsIgnoreCase("Sub Section")) {
            if (!"Section".equalsIgnoreCase(lastTagType) && !"Sub Section".equalsIgnoreCase(lastTagType)) {
                AndroidUtils.showError("Invalid Selection. Please add Section before adding Sub Section or Sub Sub Section.", getActivity());
                return;
            }
        }

// === Sub Sub Section Validation ===
        if (title.equalsIgnoreCase("Sub Sub Section")) {
            if (!"Sub Section".equalsIgnoreCase(lastTagType) && !"Sub Sub Section".equalsIgnoreCase(lastTagType)) {
                AndroidUtils.showError("Invalid Selection. Please add Sub Section before adding Sub Sub Section.", getActivity());
                return;
            }
        }

        // === Default Add for All Other Types ===
        ContentInputViewProvider inputViewProvider = new ContentInputViewProvider(getContext(), ll_create_document, new ContentInputViewProvider.ContentActionListener() {
            @Override
            public void onRemoveClicked(View view) {
                checkConsecutivePageBreaks();
            }

            @Override
            public void onExpandClicked(String titleTxt, String title, String content, View targetView) {
                loadExpandView(titleTxt, title, content, targetView, true, true);
            }
        }, this);

        inputViewProvider.AddDocContent(title, title, hint);
        ll_create_document.addView(inputViewProvider.getView());
    }

    public void loadExpandView(String titleTxt, String tv_title, String tv_content, View
            targetView, Boolean isTitleVisible, Boolean isContentVisible) {
        ExpandContentProvider dialog = new ExpandContentProvider(getContext(), ll_create_view, isTitleVisible, isContentVisible);

        // Remove prefix only if needed
        String actualCustomTitle = tv_title != null && tv_title.startsWith(titleTxt + " - ")
                ? tv_title.substring((titleTxt + " - ").length()).trim()
                : tv_title;

        dialog.setTitles(titleTxt, actualCustomTitle, tv_content);

        dialog.setOnSaveClickListener((title, content) -> {
            if (targetView != null) {
                TextView tvContentTitle = targetView.findViewById(R.id.tv_content);
                TextView tvCaption = targetView.findViewById(R.id.et_content); // for image caption, it's in caption text view

                if (tvContentTitle != null && !title.isEmpty() && isTitleVisible) {
                    tvContentTitle.setText(titleTxt + " - " + title);
                }
                if (tvContentTitle != null && title.isEmpty()) {
                    tvContentTitle.setText(titleTxt);
                }

                if (tvCaption != null && isContentVisible) {
                    tvCaption.setText(content); // Update caption text
                }
            }
            dialog.dismiss();
        });

        dialog.show();
    }


    private void callUploadImage(File new_file) {
        try {
            progress_dialog = AndroidUtils.get_progress(getActivity());
            JSONObject postdata = new JSONObject();
            WebServiceHelper.callHttpUploadWebService(this, getContext(), WebServiceHelper.RestMethodType.POST, DocEditorUploadImageURL, "Upload DocEditor File", new_file, postdata.toString());
        } catch (Exception e) {
            if (progress_dialog != null && progress_dialog.isShowing())
                AndroidUtils.dismiss_dialog(progress_dialog);
        }
    }

    //    public void AddDocListView(String title, List<String> items) {
//        AddDocListView view = new AddDocListView(getContext(), ll_create_document, childView -> {
//            ll_create_document.removeView(childView);
//            checkConsecutivePageBreaks();
//        });
//
//        view.setContent(title, title, "");
//
//        if (items != null && !items.isEmpty()) {
//            view.setListItems(items);
//        }
//
//        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
//                ViewGroup.LayoutParams.MATCH_PARENT,
//                ViewGroup.LayoutParams.WRAP_CONTENT
//        );
//        params.setMargins(0, 0, 0, 20);
//        view.getView().setLayoutParams(params);
//
//        ll_create_document.addView(view.getView());
//    }
    public void AddDocListView(String tagName, List<String> items) {
        AddDocListView docListView = new AddDocListView(getContext(), ll_create_document, childView -> {
            ll_create_document.removeView(childView);
            checkConsecutivePageBreaks();
        });
        // Set title and tag
        docListView.setContent(tagName, tagName);

        // Add list items if present
        if (items != null && !items.isEmpty()) {
            docListView.setListItems(items);
        }

        // Setup layout params with bottom margin
        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
        );
        layoutParams.setMargins(0, 0, 0, 20);
        docListView.getView().setLayoutParams(layoutParams);

        // Add to parent layout
        ll_create_document.addView(docListView.getView());
    }


    public void AddPageBreak(String name) {
        int staticViewsCount = 5;
        int childCount = ll_create_document.getChildCount();

// 1. Prevent adding Page Break when only static views exist
        if (childCount == staticViewsCount) {
            AndroidUtils.showError("Page Break cannot be added at the beginning of the document.", getActivity());
            return;
        }

// 2. Prevent adding Page Break right after another Page Break
        View lastChild = ll_create_document.getChildAt(childCount - 1);
        TextView lastChildText = lastChild.findViewById(R.id.tv_content);
        if (lastChildText != null &&
                lastChildText.getText().toString().equalsIgnoreCase(getString(R.string.new_page))) {
            AndroidUtils.showError("Consecutive page breaks cannot be added.", getActivity());
            return;
        }

// Passed all conditions — now add Page Break
        View view_added_list = LayoutInflater.from(getContext()).inflate(R.layout.page_break_layout, null);
        TextView tv_content = view_added_list.findViewById(R.id.tv_content);
        ImageView iv_remove = view_added_list.findViewById(R.id.iv_remove);

        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
        );
        params.setMargins(0, 0, 0, 20);
        view_added_list.setLayoutParams(params);

        iv_remove.setOnClickListener(view -> ll_create_document.removeView(view_added_list));

// Set the text to match what you're checking above
        tv_content.setText(R.string.new_page);
        tv_content.setTag(name);
        ll_create_document.addView(view_added_list);
        tv_content.setFocusable(true);
        tv_content.setFocusableInTouchMode(true);
        tv_content.requestFocus();
    }

    void checkConsecutivePageBreaks() {
        int staticViewsCount = 5;
        int childCount = ll_create_document.getChildCount();

        // Case 1: Prevent Page Break as the first dynamic block
        if (childCount > staticViewsCount) {
            View firstDynamic = ll_create_document.getChildAt(staticViewsCount);
            TextView firstText = firstDynamic.findViewById(R.id.tv_content);
            if (firstText != null &&
                    firstText.getText().toString().equalsIgnoreCase(getString(R.string.new_page))) {
                ll_create_document.removeViewAt(staticViewsCount);
//                AndroidUtils.showError("Page Break cannot be the first item in the document.", getActivity());
                return;
            }
        }

        // Case 2: Remove consecutive Page Breaks
        for (int i = staticViewsCount + 1; i < ll_create_document.getChildCount(); i++) {
            View prev = ll_create_document.getChildAt(i - 1);
            View curr = ll_create_document.getChildAt(i);

            TextView prevText = prev.findViewById(R.id.tv_content);
            TextView currText = curr.findViewById(R.id.tv_content);

            if (prevText != null && currText != null &&
                    prevText.getText().toString().equalsIgnoreCase(getString(R.string.new_page)) &&
                    currText.getText().toString().equalsIgnoreCase(getString(R.string.new_page))) {

                // Remove the later Page Break (curr)
                ll_create_document.removeView(curr);
                break;
            }
        }
    }


    private LinearLayout createRow(int[] columnCount,
                                   boolean showDeleteIcon,
                                   List<LinearLayout> tableRows,
                                   LinearLayout llTableContainer, LinearLayout ll_column_headers, Button btnAddColumn) {
        LinearLayout row = new LinearLayout(getContext());
        row.setOrientation(LinearLayout.HORIZONTAL);
        row.setLayoutParams(new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
        ));
        row.setGravity(Gravity.CENTER_VERTICAL);

        for (int i = 0; i < columnCount[0]; i++) {
            TextInputEditText cell = createCell();
            cell.requestFocus(); // ✅ Move cursor to this field

            // Show keyboard
            cell.post(() -> {
                InputMethodManager imm = (InputMethodManager) getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
                if (imm != null) {
                    imm.showSoftInput(cell, InputMethodManager.SHOW_IMPLICIT);
                }
            });
            row.addView(cell);
        }

        // Delete icon
        ImageView deleteIcon = new ImageView(getContext());
        deleteIcon.setImageResource(R.drawable.delete_de);
        deleteIcon.setPadding(10, 10, 10, 10);
        deleteIcon.setScaleType(ImageView.ScaleType.CENTER_INSIDE);
        deleteIcon.setAdjustViewBounds(true);

        int sizeInDp = 30;
        int sizeInPx = (int) TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP, sizeInDp, getContext().getResources().getDisplayMetrics());

        LinearLayout.LayoutParams iconParams = new LinearLayout.LayoutParams(sizeInPx, sizeInPx);
        iconParams.setMargins(10, 10, 10, 10);
        deleteIcon.setLayoutParams(iconParams);

        deleteIcon.setOnClickListener(v -> {
            if (rowHasContent(row)) {
                pendingDeleteType = DeleteTargetType.ROW;
                pendingRowLayout = row;
                ConfirmPopup("row", columnCount, tableRows, ll_column_headers, llTableContainer, btnAddColumn);
            } else {
                llTableContainer.removeView(row);
                tableRows.remove(row);
                if (tableRows.size() == 1) {
                    View lastChild = tableRows.get(0).getChildAt(tableRows.get(0).getChildCount() - 1);
                    if (lastChild instanceof ImageView) {
                        lastChild.setVisibility(GONE);
                    }
                }
            }
            updateHeaderMarginBasedOnRowDelete(tableRows, ll_column_headers);
        });

        row.addView(deleteIcon);
        deleteIcon.setVisibility(showDeleteIcon ? VISIBLE : GONE);
        return row;
    }

    private TextInputEditText createCell() {
        LayoutInflater inflater = LayoutInflater.from(requireContext());
        TextInputEditText cellView = (TextInputEditText) inflater.inflate(
                R.layout.title_description_layout, new LinearLayout(requireContext()), false
        );
        cellView.requestFocus(); // ✅ Move cursor to this field

        // Show keyboard
        cellView.post(() -> {
            InputMethodManager imm = (InputMethodManager) getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
            if (imm != null) {
                imm.showSoftInput(cellView, InputMethodManager.SHOW_IMPLICIT);
            }
        });
        cellView.addTextChangedListener(new ContentValidation(cellView, true));
        cellView.setMaxLines(1);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f
        );
        params.setMargins(4, 4, 4, 4);
        cellView.setLayoutParams(params);
        cellView.setHint(R.string.enter_your_content);
        cellView.setMaxLines(1);
        cellView.setBackground(ContextCompat.getDrawable(requireContext(), R.drawable.rectangle_light_grey_bg));
        return cellView;
    }

    private void updateRowDeleteIcons(List<LinearLayout> tableRows) {
        for (LinearLayout row : tableRows) {
            View lastChild = row.getChildAt(row.getChildCount() - 1);
            if (lastChild instanceof ImageView) {
                lastChild.setVisibility(tableRows.size() > 1 ? VISIBLE : GONE);
            }
        }
    }

    private void removeColumnAt(int indexToRemove,
                                LinearLayout llColumnHeaders,
                                List<LinearLayout> tableRows,
                                int[] columnCount,
                                Button btnAddColumn) {
        if (indexToRemove < 0) return;

        // Remove column header
        View headerView = llColumnHeaders.getChildAt(indexToRemove);
        if (headerView != null) {
            llColumnHeaders.removeViewAt(indexToRemove);
        }

        // Remove corresponding cell in each row
        for (LinearLayout row : tableRows) {
            if (row.getChildCount() > indexToRemove) {
                row.removeViewAt(indexToRemove);
            }
        }

        // Reassign tags
        for (int j = 0; j < llColumnHeaders.getChildCount(); j++) {
            llColumnHeaders.getChildAt(j).setTag(j);
        }

        columnCount[0]--;
        updateColumnMinusIconsVisibility(llColumnHeaders, btnAddColumn);

        // ✅ Re-enable "Add Column" button if column count drops below 4
        if (columnCount[0] < 4 && btnAddColumn != null) {
            btnAddColumn.setAlpha(1.0f);
            btnAddColumn.setEnabled(true);
        }
    }

    private boolean rowHasContent(LinearLayout row) {
        for (int i = 0; i < row.getChildCount(); i++) {
            View cell = row.getChildAt(i);
            if (cell instanceof TextInputEditText) {
                String text = ((TextInputEditText) cell).getText().toString().trim();
                if (!text.isEmpty()) {
                    return true;
                }
            }
        }
        return false;
    }

    public void AddTable(String title, List<List<String>> tableData) {
        View view_added_list = LayoutInflater.from(getContext()).inflate(R.layout.table_layout, null);
        LinearLayout ll_table_container;
        LinearLayout ll_column_headers;
        final List<LinearLayout> tableRows = new ArrayList<>();
        final int[] columnCount = new int[]{tableData != null && !tableData.isEmpty() ? tableData.get(0).size() : 1};
        TextView tv_content = view_added_list.findViewById(R.id.tv_content);
        ImageView iv_remove = view_added_list.findViewById(R.id.iv_remove);
        ll_table_container = view_added_list.findViewById(R.id.ll_add_more);
        Button btn_addColumn = view_added_list.findViewById(R.id.btn_addColumn);
        Button btn_addRow = view_added_list.findViewById(R.id.btn_addRow);
        ll_column_headers = view_added_list.findViewById(R.id.ll_column_headers);

        btn_addRow.setText(R.string.add_row);
        btn_addColumn.setText(R.string.add_column);
//        btn_addRow.requestFocus();
//        btn_addColumn.requestFocus();
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT
        );
        params.setMargins(0, 0, 0, 20);
        view_added_list.setLayoutParams(params);

        tableRows.clear();

        tv_content.setText(title);
        tv_content.setTag(title);
        if (tableData == null) {
            tableData = new ArrayList<>();
            List<String> emptyRow = new ArrayList<>();
            emptyRow.add("");
            tableData.add(emptyRow);
        }

        if (!tableData.isEmpty()) {
            columnCount[0] = tableData.get(0).size();
        }

        for (int i = 0; i < columnCount[0]; i++) {

            // Create container to match cell width
            LinearLayout headerCell = new LinearLayout(getContext());
            headerCell.setOrientation(LinearLayout.HORIZONTAL);
            headerCell.setGravity(Gravity.END); // align minus icon to end
            LinearLayout.LayoutParams headerParams = new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f);
            headerParams.setMargins(4, 4, 4, 4);
            headerCell.setLayoutParams(headerParams);

            // Create minus icon
            ImageView ivMinus = new ImageView(getContext());
            ivMinus.setImageResource(R.drawable.minus_large_icon);
            ivMinus.setAdjustViewBounds(true);

            int sizeInDp = 20;
            int sizeInPx = (int) TypedValue.applyDimension(
                    TypedValue.COMPLEX_UNIT_DIP, sizeInDp, getContext().getResources().getDisplayMetrics());

            LinearLayout.LayoutParams minusParams = new LinearLayout.LayoutParams(sizeInPx, sizeInPx);
            ivMinus.setLayoutParams(minusParams);
            ivMinus.setTag(i);

            ivMinus.setOnClickListener(colView -> {
                int indexToRemove = (int) colView.getTag();
                boolean hasContent = false;
                for (LinearLayout row : tableRows) {
                    if (row.getChildCount() > indexToRemove) {
                        View cell = row.getChildAt(indexToRemove);
                        if (cell instanceof TextInputEditText) {
                            String text = ((TextInputEditText) cell).getText().toString().trim();
                            if (!text.isEmpty()) {
                                hasContent = true;
                                break;
                            }
                        }
                    }
                }
                if (hasContent) {
                    pendingDeleteType = DeleteTargetType.COLUMN;
                    pendingDeleteView = colView;
                    pendingColumnIndex = indexToRemove;
                    ConfirmPopup("column", columnCount, tableRows, ll_column_headers, ll_table_container, btn_addColumn);
                } else {
                    removeColumnAt(indexToRemove, ll_column_headers, tableRows, columnCount, btn_addColumn);
                }
            });

            // Add icon to container, container to header
            headerCell.addView(ivMinus);
            ll_column_headers.addView(headerCell);
        }

        // Create rows
        for (int r = 0; r < tableData.size(); r++) {
            List<String> rowData = tableData.get(r);
            LinearLayout row = new LinearLayout(getContext());
            row.setOrientation(LinearLayout.HORIZONTAL);
            row.setGravity(Gravity.CENTER_VERTICAL);
            row.setLayoutParams(new LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT
            ));

            for (int c = 0; c < rowData.size(); c++) {
                TextInputEditText cell = createCell();
                cell.setText(rowData.get(c));
                row.addView(cell);
            }

            ImageView deleteIcon = new ImageView(getContext());
            deleteIcon.setImageResource(R.drawable.delete_de);
            deleteIcon.setPadding(10, 10, 10, 10);
            deleteIcon.setScaleType(ImageView.ScaleType.CENTER_INSIDE);
            deleteIcon.setAdjustViewBounds(true);

            int sizeInDp = 30;
            int sizeInPx = (int) TypedValue.applyDimension(
                    TypedValue.COMPLEX_UNIT_DIP, sizeInDp, getContext().getResources().getDisplayMetrics());

            LinearLayout.LayoutParams iconParams = new LinearLayout.LayoutParams(sizeInPx, sizeInPx);
            iconParams.setMargins(10, 10, 10, 10);
            deleteIcon.setLayoutParams(iconParams);

            deleteIcon.setOnClickListener(v -> {
                if (rowHasContent(row)) {
                    pendingDeleteType = DeleteTargetType.ROW;
                    pendingRowLayout = row;
                    ConfirmPopup("row", columnCount, tableRows, ll_column_headers, ll_table_container, btn_addColumn);
                } else {
                    ll_table_container.removeView(row);
                    tableRows.remove(row);
                    if (tableRows.size() == 1) {
                        View lastChild = tableRows.get(0).getChildAt(tableRows.get(0).getChildCount() - 1);
                        if (lastChild instanceof ImageView) {
                            lastChild.setVisibility(GONE);
                        }
                    }
                }
//                handleSingleRowState(tableRows, ll_column_headers, btn_addColumn);
                updateHeaderMarginBasedOnRowDelete(tableRows, ll_column_headers);
            });

            row.addView(deleteIcon);
            tableRows.add(row);
            ll_table_container.addView(row);
        }
        if (!tableData.isEmpty()) {
            updateHeaderMarginBasedOnRowDelete(tableRows, ll_column_headers);
        }
        // Set delete icon visibility
        updateRowDeleteIcons(tableRows);

        updateColumnMinusIconsVisibility(ll_column_headers, btn_addColumn);

        iv_remove.setOnClickListener(v -> {
//            boolean hasContent = tableHasAnyContent(ll_table_container);
//            if (hasContent) {
            pendingDeleteType = DeleteTargetType.TABLE;
            pendingDeleteView = view_added_list; // the whole table view
            ConfirmPopup("table", columnCount, tableRows, ll_column_headers, ll_table_container, btn_addColumn);
//            } else {
//                tableRows.clear();
//                columnCount[0] = 0;
//                ll_create_document.removeView(view_added_list);
//                checkConsecutivePageBreaks();
//            }
        });


        btn_addRow.setOnClickListener(v -> {
            LinearLayout newRow = createRow(columnCount, true, tableRows, ll_table_container, ll_column_headers, btn_addColumn);
            tableRows.add(newRow);
            ll_table_container.addView(newRow);
            updateRowDeleteIcons(tableRows);
            updateColumnMinusIconsVisibility(ll_column_headers, btn_addColumn);
            btn_addColumn.setClickable(true);
            updateAddColumnButtonState(btn_addColumn, columnCount[0]);
//            handleSingleRowState(tableRows, ll_column_headers, btn_addColumn);
            updateHeaderMarginBasedOnRowDelete(tableRows, ll_column_headers);
        });
        btn_addColumn.setOnClickListener(v -> {
            updateAddColumnButtonState(btn_addColumn, columnCount[0]);

            final int newColumnIndex = columnCount[0];
            columnCount[0]++;

            // Create header cell container
            LinearLayout headerCell = new LinearLayout(getContext());
            headerCell.setOrientation(LinearLayout.HORIZONTAL);
            headerCell.setGravity(Gravity.END); // Align minus icon to the end
            LinearLayout.LayoutParams headerParams = new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f);
            headerParams.setMargins(4, 4, 4, 4);
            headerCell.setLayoutParams(headerParams);

            // Create minus icon
            ImageView newMinus = new ImageView(getContext());
            newMinus.setImageResource(R.drawable.minus_large_icon);
            newMinus.setAdjustViewBounds(true);

            int sizeInDp = 20;
            int sizeInPx = (int) TypedValue.applyDimension(
                    TypedValue.COMPLEX_UNIT_DIP, sizeInDp, getContext().getResources().getDisplayMetrics());

            LinearLayout.LayoutParams minusParams = new LinearLayout.LayoutParams(sizeInPx, sizeInPx);
            newMinus.setLayoutParams(minusParams);
            newMinus.setTag(newColumnIndex);

            // Set click listener for minus icon
            newMinus.setOnClickListener(colView -> {
                int indexToRemove = (int) colView.getTag();

                boolean hasContent = false;
                for (LinearLayout row : tableRows) {
                    if (row.getChildCount() > indexToRemove) {
                        View cell = row.getChildAt(indexToRemove);
                        if (cell instanceof TextInputEditText) {
                            String text = ((TextInputEditText) cell).getText().toString().trim();
                            if (!text.isEmpty()) {
                                hasContent = true;
                                break;
                            }
                        }
                    }
                }

                if (hasContent) {
                    pendingDeleteType = DeleteTargetType.COLUMN;
                    pendingDeleteView = colView;
                    pendingColumnIndex = indexToRemove;
                    ConfirmPopup("column", columnCount, tableRows, ll_column_headers, ll_table_container, btn_addColumn);
                } else {
                    removeColumnAt(indexToRemove, ll_column_headers, tableRows, columnCount, btn_addColumn);

                    if (columnCount[0] < 4) {
                        btn_addColumn.setAlpha(1.0f);
                        btn_addColumn.setEnabled(true);
                    }
                }
            });

            // Add icon to container
            headerCell.addView(newMinus);

            // Add container to header row
            ll_column_headers.addView(headerCell);

            // Add cell to each row
            for (LinearLayout row : tableRows) {
                TextInputEditText newCell = createCell();
                row.addView(newCell, row.getChildCount() - 1); // insert before delete icon
            }

            updateColumnMinusIconsVisibility(ll_column_headers, btn_addColumn);
            updateAddColumnButtonState(btn_addColumn, columnCount[0]);
//            handleSingleRowState(tableRows, ll_column_headers, btn_addColumn);
            updateHeaderMarginBasedOnRowDelete(tableRows, ll_column_headers);
        });


        ll_create_document.addView(view_added_list);
        updateAddColumnButtonState(btn_addColumn, columnCount[0]);
//        btn_addRow.setFocusable(true);
//        btn_addRow.setFocusableInTouchMode(true);
//        btn_addRow.requestFocus();

// Optional scroll to ensure visibility
        btn_addRow.getParent().requestChildFocus(btn_addRow, btn_addColumn);
    }

    //    private void updateHeaderMarginBasedOnRowDelete(List<LinearLayout> tableRows, LinearLayout llColumnHeaders) {
//        boolean anyDeleteVisible = false;
//
//        for (LinearLayout row : tableRows) {
//            View lastChild = row.getChildAt(row.getChildCount() - 1);
//            if (lastChild instanceof ImageView && lastChild.getVisibility() == View.VISIBLE) {
//                anyDeleteVisible = true;
//                break;
//            }
//        }
//
//        if (llColumnHeaders.getChildCount() > 0) {
//            View lastHeaderCell = llColumnHeaders.getChildAt(llColumnHeaders.getChildCount() - 1);
//            LinearLayout.LayoutParams params = (LinearLayout.LayoutParams) lastHeaderCell.getLayoutParams();
//
//            int marginInDp = anyDeleteVisible ? 30 : 4;
//            int marginInPx = (int) TypedValue.applyDimension(
//                    TypedValue.COMPLEX_UNIT_DIP,
//                    marginInDp,
//                    getContext().getResources().getDisplayMetrics()
//            );
//
//            params.setMargins(params.leftMargin, params.topMargin, marginInPx, params.bottomMargin);
//            lastHeaderCell.setLayoutParams(params);
//        } else {
//            View lastHeaderCell = llColumnHeaders.getChildAt(llColumnHeaders.getChildCount() - 1);
//            LinearLayout.LayoutParams params = (LinearLayout.LayoutParams) lastHeaderCell.getLayoutParams();
//
//            int marginInDp = anyDeleteVisible ? 3 : 4;
//            int marginInPx = (int) TypedValue.applyDimension(
//                    TypedValue.COMPLEX_UNIT_DIP,
//                    marginInDp,
//                    getContext().getResources().getDisplayMetrics()
//            );
//
//            params.setMargins(params.leftMargin, params.topMargin, marginInPx, params.bottomMargin);
//            lastHeaderCell.setLayoutParams(params);
//        }
//    }
    private int dpToPx(int dp) {
        return (int) TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP,
                dp,
                getResources().getDisplayMetrics()
        );
    }

    private void updateHeaderMarginBasedOnRowDelete
            (List<LinearLayout> tableRows, LinearLayout llColumnHeaders) {
        boolean anyDeleteVisible = false;
        for (LinearLayout row : tableRows) {
            View lastChild = row.getChildAt(row.getChildCount() - 1);
            if (lastChild instanceof ImageView && lastChild.getVisibility() == VISIBLE) {
                anyDeleteVisible = true;
                break;
            }
        }

        ViewGroup.MarginLayoutParams lp = (ViewGroup.MarginLayoutParams) llColumnHeaders.getLayoutParams();
        int endMarginPx = dpToPx(anyDeleteVisible ? 35 : 4);

        // Set both 'end' (RTL-aware) and 'right' for maximum compatibility
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
            lp.setMarginEnd(endMarginPx);
        }
        lp.rightMargin = endMarginPx;

        llColumnHeaders.setLayoutParams(lp);
        llColumnHeaders.requestLayout();
    }

    public void ConfirmPopup(String FieldName,
                             int[] columnCount,
                             List<LinearLayout> tableRows,
                             LinearLayout ll_column_headers,
                             LinearLayout llTableContainer, Button btn_addColumn) {
        try {
            AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(getActivity());
            ll_create_view.setAlpha(0.5f);
            LayoutInflater inflater = requireActivity().getLayoutInflater();
            View view = inflater.inflate(R.layout.delete_relationship, null);
            TextView header_name = view.findViewById(R.id.header_name);
            header_name.setText(R.string.confirmation);
            ImageView close_documents = view.findViewById(R.id.close_documents);
            header_name.setTextColor(Color.BLACK);
            TextView tv_confirmation = view.findViewById(R.id.tv_confirmation);
            String delete_msg = "Are you sure you want to remove this " + FieldName + "?";
            tv_confirmation.setText(delete_msg);
            AppCompatButton bt_yes = view.findViewById(R.id.btn_yes);
            AppCompatButton btn_no = view.findViewById(R.id.btn_No);
            final AlertDialog dialog = dialogBuilder.create();
            btn_no.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    dialog.dismiss();
                    ll_create_view.setAlpha(1.0f);
                }
            });
            close_documents.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    dialog.dismiss();
                    ll_create_view.setAlpha(1.0f);
                }
            });
            bt_yes.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    dialog.dismiss();
                    ll_create_view.setAlpha(1.0f);
                    if (FieldName.equals("table")) {
                        ll_create_document.removeView(pendingDeleteView);
                        tableRows.clear();
                        columnCount[0] = 0;
                        checkConsecutivePageBreaks();
                    } else if (FieldName.equals("image")) {
                        if (ImageLayout != null) {
                            ll_create_document.removeView(ImageLayout);
                            ImageLayout = null; // Reset after use
                            checkConsecutivePageBreaks();
                        }
                    } else {
                        if (pendingDeleteType == DeleteTargetType.ROW && pendingRowLayout != null) {
                            llTableContainer.removeView(pendingRowLayout);
                            tableRows.remove(pendingRowLayout);
                            updateRowDeleteIcons(tableRows);
                            updateHeaderMarginBasedOnRowDelete(tableRows, ll_column_headers);
                        } else if (pendingDeleteType == DeleteTargetType.COLUMN &&
                                pendingDeleteView != null && pendingColumnIndex != -1) {

                            // Correct parent to remove minus icon
                            ll_column_headers.removeView(pendingDeleteView);
                            columnCount[0]--;

                            // Remove cell from each row
                            for (LinearLayout row : tableRows) {
                                if (row.getChildCount() > pendingColumnIndex) {
                                    row.removeViewAt(pendingColumnIndex);
                                }
                            }

                            // Reassign tags to updated header icons
                            for (int j = 0; j < ll_column_headers.getChildCount(); j++) {
                                ll_column_headers.getChildAt(j).setTag(j);
                            }

                            updateColumnMinusIconsVisibility(ll_column_headers, btn_addColumn);
                            resetDeleteState();
                            updateHeaderMarginBasedOnRowDelete(tableRows, ll_column_headers);
                        }
                    }
                }
            });
            dialog.setCancelable(false);
            dialog.setCanceledOnTouchOutside(false);
            dialog.setView(view);
            dialog.show();
        } catch (Exception e) {
            AndroidUtils.showAlert(e.getMessage(), getActivity());
        }
    }

    private void updateAddColumnButtonState(Button btn_addColumn, int currentColumnCount) {
        if (currentColumnCount >= 4) {
            btn_addColumn.setAlpha(0.5f);
            btn_addColumn.setEnabled(false);
        } else {
            btn_addColumn.setAlpha(1.0f);
            btn_addColumn.setEnabled(true);
        }
    }

    public void ConfirmPopup(String FieldName) {
        try {
            AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(getActivity());
            ll_create_view.setAlpha(0.5f);
            LayoutInflater inflater = requireActivity().getLayoutInflater();
            View view = inflater.inflate(R.layout.delete_relationship, null);
            TextView header_name = view.findViewById(R.id.header_name);
            header_name.setText(R.string.confirmation);
            ImageView close_documents = view.findViewById(R.id.close_documents);
            header_name.setTextColor(Color.BLACK);
            TextView tv_confirmation = view.findViewById(R.id.tv_confirmation);
            String delete_msg = "Are you sure you want to remove this " + FieldName + "?";
            tv_confirmation.setText(delete_msg);
            AppCompatButton bt_yes = view.findViewById(R.id.btn_yes);
            AppCompatButton btn_no = view.findViewById(R.id.btn_No);

//            ll_view_docs.setEnabled(false);
            final AlertDialog dialog = dialogBuilder.create();

            btn_no.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    dialog.dismiss();
                    ll_create_view.setAlpha(1.0f);
                }
            });
            close_documents.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    dialog.dismiss();
                    ll_create_view.setAlpha(1.0f);
                }
            });
            bt_yes.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    dialog.dismiss();
                    ll_create_view.setAlpha(1.0f);
                    if (FieldName.equals("image")) {
                        if (ImageLayout != null) {
                            ll_create_document.removeView(ImageLayout);
                            ImageLayout = null; // Reset after use
                            checkConsecutivePageBreaks();
                        }
                    }
                }
            });
            dialog.setCancelable(false);
            dialog.setCanceledOnTouchOutside(false);
            dialog.setView(view);
            dialog.show();
        } catch (Exception e) {
            AndroidUtils.showAlert(e.getMessage(), getActivity());
        }
    }

    private void updateColumnMinusIconsVisibility(LinearLayout ll_column_headers, Button
            btn_addColumn) {
        boolean shouldShow = ll_column_headers.getChildCount() > 1;

        for (int i = 0; i < ll_column_headers.getChildCount(); i++) {
            View icon = ll_column_headers.getChildAt(i);
            icon.setVisibility(shouldShow ? VISIBLE : GONE);
        }

        // Enable or disable the addColumn button based on max columns allowed
        int currentColumns = ll_column_headers.getChildCount();
        if (btn_addColumn != null) {
            btn_addColumn.setEnabled(currentColumns < MAX_COLUMNS); // You must define MAX_COLUMNS
            btn_addColumn.setAlpha(currentColumns < MAX_COLUMNS ? 1f : 0.5f); // Optional visual feedback
        }
    }

    enum DeleteTargetType {
        ROW,
        TABLE, COLUMN
    }

    private void resetDeleteState() {
        pendingDeleteType = null;
        pendingDeleteView = null;
        pendingColumnIndex = -1;
        pendingRowLayout = null;
    }

    public void AddImage(String title, String imagePath, String caption) {
        View view_added_list = LayoutInflater.from(getContext()).inflate(R.layout.image_layout, null);
        TextView tv_content = view_added_list.findViewById(R.id.tv_content);
        ImageView iv_remove = view_added_list.findViewById(R.id.iv_remove);
        ImageView iv_expand = view_added_list.findViewById(R.id.iv_expand);
        Button btn_browse = view_added_list.findViewById(R.id.btn_browse);
        TextView tv_max_File = view_added_list.findViewById(R.id.tv_max_File);
        ll_image_view = view_added_list.findViewById(R.id.ll_image_view);
        tv_image_name = ll_image_view.findViewById(R.id.tv_image_name);
        tv_image_name.setText("");
        tv_content.setText(title);
        tv_content.setTag(caption);
        tv_max_File.setText(R.string.max_file_size_2_mb);
        btn_browse.setText(R.string.browse_small);
//        btn_browse.setBackground(getActivity().getDrawable(R.drawable.rectangular_complete_blue_background));

        // === Image View Load ===
        if (imagePath != null && !imagePath.isEmpty()) {
            ll_image_view.setVisibility(VISIBLE);
            tv_image_name.setText(imagePath);
            tv_image_name.setTag(imagePath); // useful for saving again
        } else {
            ll_image_view.setVisibility(GONE);
        }

        iv_remove.setOnClickListener(v ->
        {
            ImageLayout = view_added_list;
            if (!tv_image_name.getText().toString().isEmpty()) {
                ConfirmPopup("image");
            } else {
                ll_create_document.removeView(view_added_list);
                checkConsecutivePageBreaks();
            }
        });
        iv_expand.setOnClickListener(v -> {
            TextView tv_current_title = view_added_list.findViewById(R.id.tv_content);
            String fullTitle = tv_current_title != null ? tv_current_title.getText().toString() : "";
            String customTitle = fullTitle.startsWith("Image - ") ? fullTitle.substring("Image - ".length()) : "";

            loadExpandView("Image", customTitle, "", view_added_list, true, false);
        });
        btn_browse.setOnClickListener(v -> {
            tempTvImageName = view_added_list.findViewById(R.id.tv_image_name);
            tempLlImageView = view_added_list.findViewById(R.id.ll_image_view);
            tempImageView = view_added_list.findViewById(R.id.imageView); // Optional if needed
            checkPermissionREAD_EXTERNAL_STORAGE(getContext());
        });

        int insertIndex = ll_create_document.getChildCount(); // default to end

//        for (int i = 0; i < ll_create_document.getChildCount(); i++) {
//            View v = ll_create_document.getChildAt(i);
//            TextView tv = v.findViewById(R.id.tv_content);
//            if (tv != null && "Overview".equalsIgnoreCase(String.valueOf(tv.getTag()))) {
//                insertIndex = i + 1;
//                break;
//            }
//        }

        ll_create_document.addView(view_added_list, insertIndex);
        view_added_list.setFocusable(true);
        view_added_list.setFocusableInTouchMode(true);
        view_added_list.requestFocus();

        view_added_list.post(() -> {
            view_added_list.getParent().requestChildFocus(view_added_list, view_added_list);
        });
    }

    ActivityResultLauncher<String[]> requestPermissions =
            registerForActivityResult(
                    new ActivityResultContracts.RequestMultiplePermissions(),
                    results -> {

                        boolean permissionGranted = false;

                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
                            permissionGranted =
                                    Boolean.TRUE.equals(results.get(READ_MEDIA_IMAGES)) ||
                                            Boolean.TRUE.equals(results.get(READ_MEDIA_VIDEO)) ||
                                            Boolean.TRUE.equals(results.get(READ_MEDIA_VISUAL_USER_SELECTED)); // Allow limited
                        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                            permissionGranted =
                                    Boolean.TRUE.equals(results.get(READ_MEDIA_IMAGES)) ||
                                            Boolean.TRUE.equals(results.get(READ_MEDIA_VIDEO));
                        } else {
                            permissionGranted =
                                    Boolean.TRUE.equals(results.get(READ_EXTERNAL_STORAGE));
                        }

                        if (permissionGranted) {
                            BottomSheetUploadfile(); // ✅ CALL HERE
                        } else {
                            // Optional: show toast or settings dialog
//                            Toast.makeText(getContext(),
//                                    "Permission required to upload files",
//                                    Toast.LENGTH_SHORT).show();
                        }
                    }
            );

    public boolean checkPermissionREAD_EXTERNAL_STORAGE(
            final Context context) {
        int currentAPIVersion = Build.VERSION.SDK_INT;
        if (currentAPIVersion >= android.os.Build.VERSION_CODES.M) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
                if (ContextCompat.checkSelfPermission(context, READ_MEDIA_IMAGES) != PackageManager.PERMISSION_GRANTED && ((ContextCompat.checkSelfPermission(context, READ_MEDIA_VIDEO)) != PackageManager.PERMISSION_GRANTED) && ((ContextCompat.checkSelfPermission(context, READ_MEDIA_VISUAL_USER_SELECTED)) != PackageManager.PERMISSION_GRANTED)) {
                    requestPermissions.launch(new String[]{READ_MEDIA_IMAGES, READ_MEDIA_VIDEO, READ_MEDIA_VISUAL_USER_SELECTED});
                } else {
                    BottomSheetUploadfile();
                }
            } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                if (ContextCompat.checkSelfPermission(context, READ_MEDIA_IMAGES) != PackageManager.PERMISSION_GRANTED && ((ContextCompat.checkSelfPermission(context, READ_MEDIA_VIDEO)) != PackageManager.PERMISSION_GRANTED)) {
                    requestPermissions.launch(new String[]{READ_MEDIA_IMAGES, READ_MEDIA_VIDEO});
                } else {
                    BottomSheetUploadfile();
                }
            } else {
                if (ContextCompat.checkSelfPermission(context, READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                    requestPermissions.launch(new String[]{READ_EXTERNAL_STORAGE});
                } else {
                    BottomSheetUploadfile();
                }
            }
//                BottomSheetUploadfile();
            return false;
        } else {
            return true;
        }
    }
//
//    public boolean validateFieldsFromLayout(LinearLayout ll_create_document) {
//        int staticViewsCount = 5;
//        for (int i = staticViewsCount; i < ll_create_document.getChildCount(); i++) {
//            View view = ll_create_document.getChildAt(i);
//            TextView tvLabel = view.findViewById(R.id.tv_content);
//            if (tvLabel == null) continue;
//
//            String tagName = tvLabel.getTag() != null ? tvLabel.getTag().toString().trim() : "";
//            String userLabel = tvLabel.getText().toString().trim();
//
//            if (tagName.equalsIgnoreCase("Overview") ||
//                    tagName.equalsIgnoreCase("Section") ||
//                    tagName.equalsIgnoreCase("Sub Section") ||
//                    tagName.equalsIgnoreCase("Sub Sub Section") ||
//                    tagName.equalsIgnoreCase("Paragraph")) {
//
//                TextInputEditText etContent = view.findViewById(R.id.et_content);
//                if (etContent == null || etContent.getText().toString().trim().isEmpty()) {
//                    AndroidUtils.showError(userLabel + " is mandatory. Please add the text.", getActivity());
//                    return false;
//                }
//
//            } else if (tagName.equalsIgnoreCase("Table")) {
//                LinearLayout tableContainer = view.findViewById(R.id.ll_add_more);
//                if (tableContainer == null || tableContainer.getChildCount() == 0) {

    /// /                    AndroidUtils.showError(userLabel + " must have at least one row.", getActivity());
//                    AndroidUtils.showError(userLabel + " column values are mandatory. Please add value.", getActivity());
//                    return false;
//                }
//
//                LinearLayout firstRow = (LinearLayout) tableContainer.getChildAt(0);
//                boolean isRowEmpty = true;
//
//                for (int j = 0; j < firstRow.getChildCount(); j++) {
//                    View cell = firstRow.getChildAt(j);
//                    if (cell instanceof TextInputEditText) {
//                        String text = ((TextInputEditText) cell).getText().toString().trim();
//                        if (!text.isEmpty()) {
//                            isRowEmpty = false;
//                            break;
//                        }
//                    }
//                }
//
//                if (isRowEmpty) {
//                    AndroidUtils.showError(userLabel + " column values are mandatory. Please add value.", getActivity());
//                    return false;
//                }
//
//            } else if (tagName.equalsIgnoreCase("Image")) {
//                TextView tvImageName = view.findViewById(R.id.tv_image_name);
//                if (tvImageName == null || tvImageName.getText().toString().trim().isEmpty()) {
//                    AndroidUtils.showError(userLabel + " is mandatory. Please add the image.", getActivity());
//                    return false;
//                }
//
//            } else if (tagName.equalsIgnoreCase("Numbered List") || tagName.equalsIgnoreCase("Bulleted List")) {
//                LinearLayout listContainer = view.findViewById(R.id.ll_add_content);
//                boolean hasAtLeastOneItem = false;
//
//                for (int j = 0; j < listContainer.getChildCount(); j++) {
//                    View itemView = listContainer.getChildAt(j);
//                    if (itemView != null) {
//                        TextInputEditText etItem = itemView.findViewById(R.id.et_list_item);
//                        if (etItem != null && !etItem.getText().toString().trim().isEmpty()) {
//                            hasAtLeastOneItem = true;
//                            break;
//                        }
//                    }
//                }
//
//                if (!hasAtLeastOneItem) {
//                    AndroidUtils.showError(userLabel + " is mandatory. Please add the text.", getActivity());
//                    return false;
//                }
//            }
//        }
//        return true; // All fields are valid
//    }
    public boolean validateFieldsFromLayout(LinearLayout ll_create_document) {
        int staticViewsCount = 5;
        for (int i = staticViewsCount; i < ll_create_document.getChildCount(); i++) {
            View view = ll_create_document.getChildAt(i);
            TextView tvLabel = view.findViewById(R.id.tv_content);
            if (tvLabel == null) continue;

            String tagName = tvLabel.getTag() != null ? tvLabel.getTag().toString().trim() : "";
            String userLabel = tvLabel.getText().toString().trim();

            // Use tagName as fallback if userLabel is custom
            String labelForError = (userLabel.equalsIgnoreCase(tagName) || userLabel.isEmpty()) ? tagName : tagName;

            if (tagName.equalsIgnoreCase("Overview") ||
                    tagName.equalsIgnoreCase("Section") ||
                    tagName.equalsIgnoreCase("Sub Section") ||
                    tagName.equalsIgnoreCase("Sub Sub Section") ||
                    tagName.equalsIgnoreCase("Paragraph")) {

                TextInputEditText etContent = view.findViewById(R.id.et_content);
                if (etContent == null || etContent.getText().toString().trim().isEmpty()) {
                    AndroidUtils.showError(labelForError + " is mandatory. Please add the text.", getActivity());
                    return false;
                }

            } else if (tagName.equalsIgnoreCase("Table")) {
                LinearLayout tableContainer = view.findViewById(R.id.ll_add_more);

                boolean isAnyCellFilled = false;

                for (int rowIndex = 0; rowIndex < tableContainer.getChildCount(); rowIndex++) {
                    View rowView = tableContainer.getChildAt(rowIndex);
                    if (!(rowView instanceof LinearLayout)) continue;

                    LinearLayout rowLayout = (LinearLayout) rowView;

                    for (int cellIndex = 0; cellIndex < rowLayout.getChildCount(); cellIndex++) {
                        View cell = rowLayout.getChildAt(cellIndex);
                        if (cell instanceof TextInputEditText) {
                            String text = ((TextInputEditText) cell).getText().toString().trim();
                            if (!text.isEmpty()) {
                                isAnyCellFilled = true; // Found at least one non-empty cell
                                break;
                            }
                        }
                    }

                    if (isAnyCellFilled) break; // no need to check further rows
                }

                if (!isAnyCellFilled) {
                    AndroidUtils.showError(labelForError + " column values are mandatory. Please add value.", getActivity());
                    return false;
                }
            } else if (tagName.equalsIgnoreCase("Image")) {
                TextView tvImageName = view.findViewById(R.id.tv_image_name);
                if (tvImageName == null || tvImageName.getText().toString().trim().isEmpty()) {
                    AndroidUtils.showError(labelForError + " is mandatory. Please add the image.", getActivity());
                    return false;
                }

            } else if (tagName.equalsIgnoreCase("Numbered List") || tagName.equalsIgnoreCase("Bulleted List")) {
                LinearLayout listContainer = view.findViewById(R.id.ll_add_content);

                for (int j = 0; j < listContainer.getChildCount(); j++) {
                    View itemView = listContainer.getChildAt(j);
                    if (itemView != null) {
                        TextInputEditText etItem = itemView.findViewById(R.id.et_list_item);
                        if (etItem != null && etItem.getText().toString().trim().isEmpty()) {
                            AndroidUtils.showError(labelForError + " is mandatory. All list items must be filled.", getActivity());
                            return false;
                        }
                    }
                }
            }

        }
        return true; // All fields are valid
    }


    private void BottomSheetUploadfile() {
        Constants.isDocEditor = true;
        ll_create_view.setAlpha(0.5f);
        bottommSheetUploadDocument = new BottomSheetUploadFile(ll_create_view, false);
        bottommSheetUploadDocument.show(getParentFragmentManager(), "");
        bottommSheetUploadDocument.setTargetFragment(this, 1);
    }

    public void triggerSavePayload(String docId) {
        try {
            // 1. Collect all dynamic fields (Section, Sub Section, Lists, Tables, etc.)
            List<FieldContentModel> fieldList = LaTeXUtils.collectFieldContentsFromLayout(getContext(), ll_create_document);

            // 2. Add title and author
            String title = et_title.getText().toString().trim();
            String author = et_author.getText().toString().trim();

            FieldContentModel titleModel = new FieldContentModel();
            titleModel.fieldname = Constants.title;
            titleModel.contentText = title;

            FieldContentModel authorModel = new FieldContentModel();
            authorModel.fieldname = Constants.author;
            authorModel.contentText = author;

            // 3. Add Overview manually if missing
            boolean hasOverview = false;
            for (FieldContentModel field : fieldList) {
                if (Constants.overview.equals(field.fieldname)) {
                    hasOverview = true;
                    break;
                }
            }

            if (!hasOverview) {
                for (int i = 0; i < ll_create_document.getChildCount(); i++) {
                    View view = ll_create_document.getChildAt(i);
                    TextView tvContentTitle = view.findViewById(R.id.tv_content);
                    TextInputEditText etContent = view.findViewById(R.id.et_content);

                    if (tvContentTitle != null && etContent != null) {
                        Object tagObj = tvContentTitle.getTag();
                        if (tagObj != null && "Overview".equalsIgnoreCase(tagObj.toString().trim())) {
                            FieldContentModel overviewModel = new FieldContentModel();
                            overviewModel.fieldname = Constants.overview;
                            overviewModel.contentText = etContent.getText().toString().trim();
                            fieldList.add(overviewModel);
                            break;
                        }
                    }
                }
            }

            // 4. Clean up: Ensure null or default section titles don't get added in LaTeX
            for (FieldContentModel model : fieldList) {
                if (model.fieldname.equals(Constants.docsection)) {
                    model.title = extractCustomTitle(model.title, "Section - ");
                } else if (model.fieldname.equals(Constants.subSection)) {
                    model.title = extractCustomTitle(model.title, "Sub Section - ");
                } else if (model.fieldname.equals(Constants.subSubSection)) {
                    model.title = extractCustomTitle(model.title, "Sub Sub Section - ");
                } else if (model.fieldname.contains(Constants.image)) {
                    model.title = extractCustomTitle(model.title, "Image - ");
                } else if (model.fieldname.equals(Constants.paragraph)) {
                    model.title = extractCustomTitle(model.title, "Paragraph - ");
                }

            }

            // 5. Add title and author at top
            fieldList.add(0, authorModel);
            fieldList.add(0, titleModel);

            // 6. Build LaTeX string (will strip trailing <ltk> inside)
            String userId = Constants.USER_ID;
            String docString = DocumentPayloadBuilder.createLatexPayload(fieldList, userId);

            // 7. Post payload
            sendPostPayload(getContext(), docString, 1, docId);

        } catch (Exception e) {
            AndroidUtils.showError("Failed to save document: " + e.getMessage(), getActivity());
            e.printStackTrace();
        }
    }

    private boolean hasValidTitle(String title) {
        return title != null && !title.trim().isEmpty() && !"null".equalsIgnoreCase(title.trim());
    }

    public void loadNewDoc() {
        if (!oldDocId.isEmpty()) {
            clearViews();
            clearNames();
        } else {
            int childCount = ll_create_document.getChildCount();
            if (childCount <= 5) {
                AndroidUtils.showError("Please add atleast one segment from the 'insert' menu to create the document", getActivity());
            } else {
                AndroidUtils.showConfirmation(
                        getActivity(),
                        requireContext().getString(R.string.confirmation_),
                        requireContext().getString(R.string.changes_you_made_may_not_be_saved_do_you_want_to_save),
                        requireContext().getString(R.string.yes),
                        new AndroidUtils.OnConfirmListener() {
                            @Override
                            public void onSave() {
                                SaveFile();
                            }

                            @Override
                            public void onCancel() {
                                clearViews();
                                clearNames();
                            }
                        }
                );

            }
        }
    }

    public void clearNames() {
        oldDocName = "";
        tv_doc_name.setText(R.string.untitled_document);
        latexDocumentResponse.setCreatedon("");
        latexDocumentResponse.setDocument("");
        latexDocumentResponse.setDocid("");
        latexDocumentResponse.setPage(0);
        latexDocumentResponse.setPageid("");
        latexDocumentResponse.setUpdatedon("");
        latexDocumentResponse.setUserid("");
    }

    private String extractCustomTitle(String title, String defaultPrefix) {
        if (title == null || title.trim().isEmpty()) return null;

        title = title.trim();
        if (title.startsWith(defaultPrefix)) {
            String customPart = title.substring(defaultPrefix.length()).trim();
            return customPart.isEmpty() ? null : customPart;
        }
        return title; // If user manually typed something custom
    }

    public void clearViews() {
        for (int i = ll_create_document.getChildCount() - 1; i >= 5; i--) {
            ll_create_document.removeViewAt(i);
        }
        oldDocId = "";
        et_title.setText("");
        et_author.setText("");
    }

    public void sendPostPayload(Context context, String documentText, int page, String docId) {
        try {
            JSONObject payload = new JSONObject();
            payload.put("document", documentText);
            payload.put("page", page);

            String url = saveLatexDoc + docId; // ✅ <-- Replace with actual URL
            if ((latexDocumentResponse.getPageid() != null) && (!latexDocumentResponse.getPageid().isEmpty())) {
                WebServiceHelper.callHttpWebService(
                        this, // AsyncTaskCompleteListener
                        getContext(),
                        WebServiceHelper.RestMethodType.PATCH,
                        url,
                        "Save DocEditor",
                        payload.toString()
                );
            } else {
                WebServiceHelper.callHttpWebService(
                        this, // AsyncTaskCompleteListener
                        getContext(),
                        WebServiceHelper.RestMethodType.POST,
                        url,
                        "Save DocEditor",
                        payload.toString()
                );
            }
            Log.d("Payload", payload.toString());
        } catch (JSONException e) {
            e.printStackTrace();
            AndroidUtils.showError("Payload error: " + e.getMessage(), getActivity());
        }
    }

    public void loadOpenDocument() {
        if (!oldDocId.isEmpty()) {
            loadDocumentView();
        } else {
            int childCount = ll_create_document.getChildCount();
            if (childCount <= 5) {
                loadDocumentView();
//                AndroidUtils.showError("Please add atleast one segment from the 'insert' menu to create the document", getActivity());
            } else {
                AndroidUtils.showConfirmation(
                        getActivity(),
                        requireContext().getString(R.string.confirmation_),
                        requireContext().getString(R.string.changes_you_made_may_not_be_saved_do_you_want_to_save),
                        requireContext().getString(R.string.yes),
                        new AndroidUtils.OnConfirmListener() {
                            @Override
                            public void onSave() {
                                SaveFile();
                            }

                            @Override
                            public void onCancel() {
                                loadDocumentView();
                            }
                        }
                );

            }
        }
    }

    private void loadDocumentView() {
        clearViews();
        clearNames();
        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(getActivity());

        ll_create_view.setAlpha(0.5f);
        LayoutInflater inflater = requireActivity().getLayoutInflater();

        // Inflate using parent so margins/background/elevation are respected
        View view = inflater.inflate(R.layout.open_document_layout, (ViewGroup) getActivity().findViewById(android.R.id.content), false);

        TextView tv_content = view.findViewById(R.id.tv_content);
        tv_content.setText(R.string.open_document);

        et_search = view.findViewById(R.id.et_search);
        et_search.setHint(R.string.search_document);
        et_search.addTextChangedListener(new Validation(et_search));
        rv_open_document = view.findViewById(R.id.rv_open_document);
        ImageView iv_remove = view.findViewById(R.id.iv_remove);
        LinearLayout ll_add_content = view.findViewById(R.id.ll_add_content);

        dialogBuilder.setView(view); // setView should be before dialog.show()
        final AlertDialog dialog = dialogBuilder.create();
        dialog1 = dialog;
        dialog.setOnDismissListener(dialogInterface -> {
            ll_create_view.setAlpha(1.0f);
        });

        iv_remove.setOnClickListener(v -> {
            dialog.dismiss();
        });

        dialog.show(); // only one call
        // Move dialog to top with 50dp margin
        Window window = dialog.getWindow();
        if (window != null) {
            window.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));

            float scale = getActivity().getResources().getDisplayMetrics().density;
            int horizontalMarginInDp = 20;
            int verticalMarginInDp = 100;

            int marginHorizontalPx = (int) (horizontalMarginInDp * scale + 0.5f);
            int marginVerticalPx = (int) (verticalMarginInDp * scale + 0.5f);

            int screenWidth = getActivity().getResources().getDisplayMetrics().widthPixels;
            int screenHeight = getActivity().getResources().getDisplayMetrics().heightPixels;

            int dialogWidth = screenWidth - (2 * marginHorizontalPx);
            int dialogHeight = screenHeight - (2 * marginVerticalPx);

            window.setLayout(dialogWidth, dialogHeight); // Set both width and height

            WindowManager.LayoutParams layoutParams = window.getAttributes();
            layoutParams.gravity = Gravity.CENTER;
            window.setAttributes(layoutParams);
        }
        loadDocumentList();
    }

    private void CloseView() {
        ll_create_view.setAlpha(1.0f);
//        isView = true;
        dialog1.dismiss();
    }

    private void loadRecyclerView(RecyclerView recyclerView) {
        if (isView) {
            recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
            loadSearch(et_search_document);
        } else {
            // Show 2-column grid layout when isView is false
            recyclerView.setLayoutManager(new GridLayoutManager(getContext(), 2));
            loadSearch(et_search);
        }

        adapter = new ViewDocAdapter(getContext(), view_doc_list, this, this, isView);
        recyclerView.setAdapter(adapter);
        //recyclerView.setHasFixedSize(true);
    }

    public void loadDocumentFromPayload(String latexPayload) {
        int childCount = ll_create_document.getChildCount();
        if (childCount > 5) {
            clearViews();
        }

        if (latexPayload == null || latexPayload.trim().isEmpty()) return;

        // Skip boilerplate before <ltk>
        if (latexPayload.contains("<ltk>")) {
            latexPayload = latexPayload.substring(latexPayload.indexOf("<ltk>"));
        }

        String[] blocks = latexPayload.split("<ltk>");
        for (String block : blocks) {
            block = block.trim();
            if (block.isEmpty()) continue;

            if (block.startsWith("\\documentclass") || block.startsWith("\\usepackage")
                    || block.startsWith("\\geometry") || block.startsWith("\\begin{document}")
                    || block.startsWith("\\maketitle") || block.startsWith("\\date{")) {
                continue;
            }

            if (!oldDocName.isEmpty()) {
                tv_doc_name.setText(oldDocName);
            }

            // === Title ===
//            if (block.startsWith("\\title{")) {
//                String title = extractBetween(block, "\\title{", "}");
//                et_title.setText(title);
//                continue;
//            }
            if (block.startsWith("\\title") && block.contains("{")) {
                String title = extractBetween(block, "\\title", "}");
                if (title != null) {
                    title = title.replaceFirst("^\\s*\\{", "").trim(); // remove starting { and spaces
                    et_title.setText(title);
                }
                continue;
            }


            // === Author ===
            if (block.startsWith("\\author{")) {
                String author = extractBetween(block, "\\author{", "}");
                et_author.setText(author);
                continue;
            }

            // === Overview ===
            if (block.startsWith("\\abstract")) {
                String content = block.replaceFirst("\\\\abstract", "").trim();
                AddView("Overview", content);
                continue;
            }

            // === Section ===
            if (block.startsWith("\\section{")) {
                String label = extractBetween(block, "\\section{", "}");
                String displayLabel;
                if (label == null || label.trim().isEmpty()) {
                    displayLabel = "Section";
                    label = "Section";
                } else {
                    displayLabel = "Section - " + label.trim();
                }

                String content = block.replaceFirst("\\\\section\\{.*?\\}", "").trim();
                AddView("Section", "[[title]]" + displayLabel + "\n" + content);
                continue;
            }


            // === Sub Section ===
            if (block.startsWith("\\subsection{")) {
                String label = extractBetween(block, "\\subsection{", "}");
                String displayLabel;
                if (label == null || label.trim().isEmpty()) {
                    displayLabel = "Sub Section";
                    label = "Sub Section";
                } else {
                    displayLabel = "Sub Section - " + label.trim();
                }

                String content = block.replaceFirst("\\\\subsection\\{.*?\\}", "").trim();
                AddView("Sub Section", "[[title]]" + displayLabel + "\n" + content);
                continue;
            }

// === Sub Sub Section ===
            if (block.startsWith("\\subsubsection{")) {
                String label = extractBetween(block, "\\subsubsection{", "}");
                String displayLabel;
                if (label == null || label.trim().isEmpty()) {
                    displayLabel = "Sub Sub Section";
                    label = "Sub Sub Section";
                } else {
                    displayLabel = "Sub Sub Section - " + label.trim();
                }

                String content = block.replaceFirst("\\\\subsubsection\\{.*?\\}", "").trim();
                AddView("Sub Sub Section", "[[title]]" + displayLabel + "\n" + content);
                continue;
            }


            // === Page Break ===
            if (block.startsWith("\\newpage")) {
                AddPageBreak("Page Break");
                continue;
            }

            // === Ordered List ===
            if (block.startsWith("\\begin{enumerate}")) {
                List<String> items = extractListItems(block);
                AddDocListView("Numbered List", items);
                continue;
            }

            // === Unordered List ===
            if (block.startsWith("\\begin{itemize}")) {
                List<String> items = extractListItems(block);
                AddDocListView("Bulleted List", items);
                continue;
            }

            // === Table ===
            if (block.contains("\\begin{tabularx}")) {
                List<List<String>> table = extractTableData(block);
                AddTable("Table", table);
                continue;
            }

            // === Image ===
            if (block.contains("\\includegraphics")) {
                String imagePath = extractImagePath(block);
                String caption = extractCaption(block);
                String displayLabel;

                if (caption == null || caption.trim().isEmpty() || caption.equals("Image")) {
                    displayLabel = "Image";
                    caption = "Image";
                } else {
                    displayLabel = "Image - " + caption.trim();
                }
                if (caption == null || caption.trim().isEmpty()) caption = "Image";
                String fileName = imagePath.substring(imagePath.lastIndexOf("/") + 1);
                AddImage(displayLabel, fileName, caption);
                continue;
            }


            // === Paragraph with custom title ===
            if (block.startsWith("\\paragraph{")) {
                String label = extractBetween(block, "\\paragraph{", "}");
                String displayLabel;
                if (label == null || label.trim().isEmpty()) {
                    displayLabel = "Paragraph";
                    label = "Paragraph";
                } else {
                    displayLabel = "Paragraph - " + label.trim();
                }

                String content = block.replaceFirst("\\\\paragraph\\{.*?\\}", "").trim();
                AddView("Paragraph", "[[title]]" + displayLabel + "\n" + content);
                continue;
            }


            // === Paragraph (fallback) ===
//            if (!block.isEmpty()) {
//                AddView("Paragraph", block);
//            }
        }
    }


    public void SaveFile() {
        if (!Constants.is_active) {
            AndroidUtils.showRenewalPopup(getActivity());
        } else {
            if (validateFieldsFromLayout(ll_create_document)) {
                if ((latexDocumentResponse.getPageid() != null) && (!latexDocumentResponse.getPageid().isEmpty())) {
                    int staticViewsCount = 5;
                    int childCount = ll_create_document.getChildCount();
                    if (childCount == staticViewsCount) {
                        AndroidUtils.showError("Please add atleast one segment from the 'insert' menu to create the document", getActivity());
                    } else {
                        triggerSavePayload(latexDocumentResponse.getPageid());
                    }
                } else {
                    loadCreateFile(false);
                }
            }
        }
    }

    public void SaveAsFile() {
        if (!Constants.is_active) {
            AndroidUtils.showRenewalPopup(getActivity());
        } else {
            if ((latexDocumentResponse.getDocid() != null) && (!latexDocumentResponse.getDocid().isEmpty())) {
                loadCreateFile(true);
            } else {
                AndroidUtils.showError("Please save changes before making a copy", getActivity());
            }
        }
    }

    public void DeleteFile() {
        if ((latexDocumentResponse.getDocid() != null) && (!latexDocumentResponse.getDocid().isEmpty())) {
            DeleteDoc(oldDocName, oldDocId);
        } else {
            AndroidUtils.showError("Please select the document", getActivity());
        }
    }

    public void loadCreateFile(boolean isSaveAs) {
        if (!Constants.is_active) {
            AndroidUtils.showRenewalPopup(getActivity());
        } else {
            int staticViewsCount = 5;
            int childCount = ll_create_document.getChildCount();

// 1. Prevent adding Page Break when only static views exist
            if (childCount == staticViewsCount) {
                AndroidUtils.showError("Please add atleast one segment from the 'insert' menu to create the document", getActivity());
            } else {
                AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(getActivity());
                ll_create_view.setAlpha(0.5f);
                LayoutInflater inflater = requireActivity().getLayoutInflater();
                View view = inflater.inflate(R.layout.file_name_layout_de, null);
                TextView tv_file_name = view.findViewById(R.id.tv_file_name);
                TextInputEditText et_file_name = view.findViewById(R.id.et_file_name);
                Button btn_save_file = view.findViewById(R.id.btn_save_file);
                ImageView iv_cancel_icon = view.findViewById(R.id.iv_cancel_icon);
                TextView tv_error_msg = view.findViewById(R.id.tv_error_msg);
                tv_error_msg.setTextColor(Color.RED);
                tv_error_msg.setText(R.string.filename_is_required);
                tv_file_name.setText(R.string.file_name);
                et_file_name.setHint(R.string.enter_document_name);
                et_file_name.addTextChangedListener(new ContentValidation(et_file_name, true));
                filters1 = new InputFilter[]{
                        new InputFilter.LengthFilter(25)
                };
                et_file_name.setFilters(filters1);
                final AlertDialog dialog = dialogBuilder.create();
                this.dialog = dialog;
//            ad_dialog_delete = dialog;
                // Ensure cl_document's alpha is reset when the dialog is dismissed.
                dialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
                    @Override
                    public void onDismiss(DialogInterface dialog) {
                        ll_create_view.setAlpha(1.0f);
                    }
                });
                et_file_name.addTextChangedListener(new TextWatcher() {
                    @Override
                    public void beforeTextChanged(CharSequence s, int start, int count, int after) {

                    }

                    @Override
                    public void onTextChanged(CharSequence s, int start, int before, int count) {
                        if (s.length() < 1) {
                            tv_error_msg.setVisibility(VISIBLE);
                        } else {
                            tv_error_msg.setVisibility(GONE);
                        }
                    }

                    @Override
                    public void afterTextChanged(Editable s) {

                    }
                });
                btn_save_file.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (Objects.requireNonNull(et_file_name.getText()).toString().isEmpty()) {
                            tv_error_msg.setVisibility(VISIBLE);
                        } else {
                            if (isSaveAs) {
                                callSaveAsDocFile(Objects.requireNonNull(et_file_name.getText()).toString(), oldDocId);
                            } else {
                                callSaveDocFile(Objects.requireNonNull(et_file_name.getText()).toString());
                            }
                        }
                    }
                });
                iv_cancel_icon.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        dialog.dismiss();
                    }
                });
                dialog.setView(view);
                dialog.show();
                dialog.setView(view);
                dialog.show();

// Move dialog to top with 50dp margin
                Window window = dialog.getWindow();
                if (window != null) {
                    WindowManager.LayoutParams layoutParams = window.getAttributes();
                    layoutParams.gravity = Gravity.TOP | Gravity.CENTER_HORIZONTAL;

                    // Convert 50dp to pixels
                    int marginInPx = (int) TypedValue.applyDimension(
                            TypedValue.COMPLEX_UNIT_DIP, 50, getResources().getDisplayMetrics());

                    layoutParams.y = marginInPx;
                    window.setAttributes(layoutParams);
                }
            }
        }
    }

    public void callSaveDocFile(String DocName) {
        try {
            JSONObject payload = new JSONObject();
            payload.put("documentname", DocName);
            oldDocName = DocName;
            WebServiceHelper.callHttpWebService(
                    this, // AsyncTaskCompleteListener
                    getContext(),
                    WebServiceHelper.RestMethodType.POST,
                    LatexDocFile,
                    "Save Document",
                    payload.toString()
            );
        } catch (JSONException e) {
            e.printStackTrace();
            AndroidUtils.showError("Payload error: " + e.getMessage(), getActivity());
        }
    }

    public void callSaveAsDocFile(String DocName, String docid) {
        try {
            JSONObject payload = new JSONObject();
            payload.put("documentname", DocName);
            NewDocName = DocName;
            WebServiceHelper.callHttpWebService(
                    this, // AsyncTaskCompleteListener
                    getContext(),
                    WebServiceHelper.RestMethodType.POST,
                    saveAsLatexDoc + docid,
                    "Save As Document",
                    payload.toString()
            );
        } catch (JSONException e) {
            e.printStackTrace();
            AndroidUtils.showError("Payload error: " + e.getMessage(), getActivity());
        }
    }
}

