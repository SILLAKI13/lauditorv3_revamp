package com.digicoffer.lauditor.FirmProfile;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.ContentValues;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.Editable;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.TextWatcher;
import android.text.style.ForegroundColorSpan;
import android.text.style.StyleSpan;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.OnBackPressedCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.digicoffer.lauditor.R;
import com.digicoffer.lauditor.Webservice.AsyncTaskCompleteListener;
import com.digicoffer.lauditor.Webservice.HttpResultDo;
import com.digicoffer.lauditor.Webservice.CommonApiHelper.WebServiceHelper;
import com.digicoffer.lauditor.CommonFiles.GlobalFiles.AndroidUtils;
import com.digicoffer.lauditor.CommonFiles.GlobalFiles.Constants;
import com.digicoffer.lauditor.CommonFiles.FileSelection.Filecosen;
import com.google.android.material.textfield.TextInputEditText;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class PracticePartnerView extends Fragment implements AsyncTaskCompleteListener,
        View.OnClickListener,
        ViewPracticePartnerAdapter.InterfaceListener {

    TextInputEditText et_search_pp;
    View tl_search_pp;
    RecyclerView rv_view_pp;
    Dialog progress_dialog;
    FirmProfile firmProfile;
    String profileUrl = "";
    ArrayList<MemberProfileModel>  membersviewlist           = new ArrayList<>();
    ArrayList<FirmProfileModel>    firmProfileModelArrayList = new ArrayList<>();
    ViewPracticePartnerAdapter adapter;
    ViewPracticePartnerAdapter firmProfileAdapter;

    // ── Activity Result Launchers ─────────────────────────────────────────
    ActivityResultLauncher<Uri>    cameraLauncher;
    ActivityResultLauncher<Intent> galleryLauncher;
    ActivityResultLauncher<String> requestCameraPermission;
    // cropLauncher REMOVED — we now use ProfilePhotoEditorFragment instead

    // ── Single source of truth for the camera URI ─────────────────────────
    private Uri pendingCameraUri = null;

    // ─────────────────────────────────────────────────────────────────────────
    public PracticePartnerView() {}

    public static PracticePartnerView newInstance(FirmProfile firmProfile) {
        PracticePartnerView fragment = new PracticePartnerView();
        fragment.firmProfile = firmProfile;
        return fragment;
    }

    @Override
    public void onClick(View view) {}

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.practicepartners_view, container, false);
    }

    // ─────────────────────────────────────────────────────────────────────────
    // onCreate — ALL launchers MUST be registered here
    // ─────────────────────────────────────────────────────────────────────────
    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {

        // ── STEP 1: Camera launcher ───────────────────────────────────────
        cameraLauncher = registerForActivityResult(
                new ActivityResultContracts.TakePicture(),
                success -> {
                    if (Boolean.TRUE.equals(success) && pendingCameraUri != null) {
                        Uri uriToEdit = pendingCameraUri;
                        pendingCameraUri = null;
                        ViewPracticePartnerAdapter active = getActiveAdapter();
                        if (active != null) {
                            active.openCropEditor(uriToEdit);
                        }
                    } else {
                        pendingCameraUri = null;
                    }
                });

        // ── STEP 2: Camera permission launcher ───────────────────────────
        requestCameraPermission = registerForActivityResult(
                new ActivityResultContracts.RequestPermission(),
                isGranted -> {
                    if (isGranted) {
                        ViewPracticePartnerAdapter active = getActiveAdapter();
                        if (active != null) {
                            openCameraWithUri(active);
                        }
                    } else {
                        Toast.makeText(getContext(),
                                "Camera permission denied", Toast.LENGTH_SHORT).show();
                    }
                });

        // ── STEP 3: Gallery launcher ──────────────────────────────────────
        galleryLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == Activity.RESULT_OK
                            && result.getData() != null
                            && result.getData().getData() != null) {
                        Uri galleryUri = result.getData().getData();
                        ViewPracticePartnerAdapter active = getActiveAdapter();
                        if (active != null) {
                            active.openCropEditor(galleryUri);
                        }
                    }
                });

        // NOTE: cropLauncher (Step 4) is intentionally removed.
        // ProfilePhotoEditorFragment handles the crop result via its Callback interface.

        setupOnBackPressed();
        super.onCreate(savedInstanceState);
    }

    // ─────────────────────────────────────────────────────────────────────────
    private ViewPracticePartnerAdapter getActiveAdapter() {
        return Constants.isMyProfileClicked ? firmProfileAdapter : adapter;
    }

    // ─────────────────────────────────────────────────────────────────────────
    void openCameraWithUri(ViewPracticePartnerAdapter targetAdapter) {
        try {
            ContentValues cv = new ContentValues();
            cv.put(MediaStore.Images.Media.TITLE,
                    "FirmProfile_" + System.currentTimeMillis());
            cv.put(MediaStore.Images.Media.DESCRIPTION, "Profile Image");

            Uri uri = requireActivity().getContentResolver()
                    .insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, cv);

            if (uri == null) {
                Toast.makeText(getContext(),
                        "Cannot access camera storage", Toast.LENGTH_SHORT).show();
                return;
            }

            pendingCameraUri = uri;
            if (targetAdapter != null) {
                targetAdapter.cameraImageUri = uri;
            }
            cameraLauncher.launch(uri);

        } catch (Exception e) {
            Toast.makeText(getContext(),
                    "Camera error: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Opens ProfilePhotoEditorFragment as a Fragment transaction.
    // Called by the adapter's openCropEditor().
    // ─────────────────────────────────────────────────────────────────────────
    void openCropEditorFragment(Uri imageUri) {
        if (imageUri == null) return;

        // Hide the profile info elements before opening the editor
        hideProfileInfoElements();

        ProfilePhotoEditorFragment editor = ProfilePhotoEditorFragment.newInstance(imageUri);
        editor.setCallback(new ProfilePhotoEditorFragment.Callback() {
            @Override
            public void onCropSaved(String croppedFilePath) {
                // Deliver the cropped file path back to the adapter
                ViewPracticePartnerAdapter active = getActiveAdapter();
                if (active != null) {
                    active.handleCroppedResult(croppedFilePath);
                }
                // Restore visibility after crop is saved
                restoreProfileInfoElements();
            }

            @Override
            public void onChoosePhotoRequested() {
                // User tapped back or "Choose Photo" inside the crop editor
                showFileChooserPopup();
                // Don't restore here because we're still in selection flow
            }
        });

        // Add a back stack listener to restore visibility when navigating back
        Fragment parent = getParentFragment();
        if (parent != null) {
            parent.getChildFragmentManager()
                    .beginTransaction()
                    .replace(R.id.flFirmProfile, editor)
                    .setTransition(androidx.fragment.app.FragmentTransaction.TRANSIT_FRAGMENT_FADE)
                    .addToBackStack("photo_editor")
                    .commit();

            // Add a back stack change listener
            parent.getChildFragmentManager().addOnBackStackChangedListener(
                    new androidx.fragment.app.FragmentManager.OnBackStackChangedListener() {
                        @Override
                        public void onBackStackChanged() {
                            // Check if we've returned to this fragment
                            if (parent.getChildFragmentManager().getBackStackEntryCount() == 0) {
                                restoreProfileInfoElements();
                                parent.getChildFragmentManager().removeOnBackStackChangedListener(this);
                            }
                        }
                    }
            );
        } else {
            getParentFragmentManager()
                    .beginTransaction()
                    .replace(R.id.flFirmProfile, editor)
                    .setTransition(androidx.fragment.app.FragmentTransaction.TRANSIT_FRAGMENT_FADE)
                    .addToBackStack("photo_editor")
                    .commit();

            getParentFragmentManager().addOnBackStackChangedListener(
                    new androidx.fragment.app.FragmentManager.OnBackStackChangedListener() {
                        @Override
                        public void onBackStackChanged() {
                            if (getParentFragmentManager().getBackStackEntryCount() == 0) {
                                restoreProfileInfoElements();
                                getParentFragmentManager().removeOnBackStackChangedListener(this);
                            }
                        }
                    }
            );
        }
    }

    // Helper methods to hide/show profile info elements
    private void hideProfileInfoElements() {
        if (firmProfile != null && isAdded()) {
            // Hide the switch views in the parent FirmProfile fragment
            if (firmProfile.getView() != null) {
                View tvSwitchView = firmProfile.getView().findViewById(R.id.tv_switchView);
                View tvSwitchCreate = firmProfile.getView().findViewById(R.id.tv_switchCreate);
                View llcEditView = firmProfile.getView().findViewById(R.id.llc_Edit_View);

                if (tvSwitchView != null) tvSwitchView.setVisibility(View.GONE);
                if (tvSwitchCreate != null) tvSwitchCreate.setVisibility(View.GONE);
                if (llcEditView != null) llcEditView.setVisibility(View.GONE);
            }
        }
    }

    private void restoreProfileInfoElements() {
        if (firmProfile != null && isAdded()) {
            // Restore visibility based on current state
            firmProfile.loadBasicProfile(); // This will restore the correct visibility

            // Or manually restore if needed:
            if (firmProfile.getView() != null) {
                if (Constants.Profile_View.equals("Bp")) {
                    View llcEditView = firmProfile.getView().findViewById(R.id.llc_Edit_View);
                    if (llcEditView != null) llcEditView.setVisibility(View.VISIBLE);
                } else if (Constants.Profile_View.equals("Pp")) {
                    View tvSwitchCreate = firmProfile.getView().findViewById(R.id.tv_switchCreate);
                    if (tvSwitchCreate != null) tvSwitchCreate.setVisibility(View.VISIBLE);
                }
            }
        }
    }
    // Re-shows the Filecosen camera/gallery chooser popup.
    // Called when the user taps back or "Choose Photo" in the crop editor.
    // ─────────────────────────────────────────────────────────────────────────
    private void showFileChooserPopup() {
        ViewPracticePartnerAdapter active = getActiveAdapter();
        if (active == null) return;

        // Capture current references before async callback
        final ImageView iv   = active.currentIvProfile;
        final TextView  icon = active.currentPersonIcon;
        final ImageView del  = active.currentIvDeleteCircle;

        Filecosen fc = new Filecosen(requireActivity(), new Filecosen.FileChooserCallback() {
            @Override
            public void openCamera() {
                if (requireContext().checkSelfPermission(Manifest.permission.CAMERA)
                        == PackageManager.PERMISSION_GRANTED) {
                    // Restore references then open camera
                    active.currentIvProfile      = iv;
                    active.currentPersonIcon     = icon;
                    active.currentIvDeleteCircle = del;
                    openCameraWithUri(active);
                } else {
                    requestCameraPermission.launch(Manifest.permission.CAMERA);
                }
            }

            @Override
            public void openGallery() {
                // Restore references then open gallery
                active.currentIvProfile      = iv;
                active.currentPersonIcon     = icon;
                active.currentIvDeleteCircle = del;
                try {
                    Intent intent = new Intent(Intent.ACTION_PICK,
                            MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                    intent.setType("image/*");
                    galleryLauncher.launch(intent);
                } catch (Exception e) {
                    Toast.makeText(getContext(),
                            "Gallery error: " + e.getMessage(), Toast.LENGTH_LONG).show();
                }
            }
        });
        fc.show();
    }

    // ─────────────────────────────────────────────────────────────────────────
    private void setupOnBackPressed() {
        requireActivity().getOnBackPressedDispatcher().addCallback(
                new OnBackPressedCallback(true) {
                    @Override
                    public void handleOnBackPressed() {
                        if (isEnabled()) {
                            setEnabled(false);
                            requireActivity().onBackPressed();
                        }
                    }
                });
    }

    // ─────────────────────────────────────────────────────────────────────────
    // API calls
    // ─────────────────────────────────────────────────────────────────────────
    public void callViewBasicProfile() {
        progress_dialog = AndroidUtils.get_progress(getActivity());
        try {
            JSONObject jsonObject = new JSONObject();
            WebServiceHelper.callHttpWebService(
                    this, getContext(),
                    WebServiceHelper.RestMethodType.GET,
                    "v3/profile", "View_Bp",
                    jsonObject.toString());
        } catch (Exception e) {
            e.fillInStackTrace();
        }
    }

    private void callViewPracticePartners() {
        rv_view_pp.setVisibility(View.GONE);
        progress_dialog = AndroidUtils.get_progress(getActivity());
        try {
            JSONObject jsonObject = new JSONObject();
            WebServiceHelper.callHttpWebService(
                    this, getContext(),
                    WebServiceHelper.RestMethodType.GET,
                    "v3/practice-partner", "View_Pp",
                    jsonObject.toString());
        } catch (Exception e) {
            e.fillInStackTrace();
        }
    }

    private void callDeletePracticePartners(String email) {
        progress_dialog = AndroidUtils.get_progress(getActivity());
        try {
            JSONObject jsonObject = new JSONObject();
            WebServiceHelper.callHttpWebService(
                    this, getContext(),
                    WebServiceHelper.RestMethodType.DELETE,
                    "v3/practice-partner/" + email, "Delete_Pp",
                    jsonObject.toString());
        } catch (Exception e) {
            e.fillInStackTrace();
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    // buildAdapter — cropLauncher removed, pass null in its place
    // ─────────────────────────────────────────────────────────────────────────
    private ViewPracticePartnerAdapter buildAdapter() {
        return new ViewPracticePartnerAdapter(
                membersviewlist,
                profileUrl,
                firmProfileModelArrayList,
                getActivity(),
                getContext(),
                this,
                this,
                cameraLauncher,
                galleryLauncher,
                null,               // cropLauncher removed — Fragment handles crop now
                requestCameraPermission
        ) {
            @Override
            void openCamera_new(android.widget.ImageView iv,
                                android.widget.TextView icon,
                                android.widget.ImageView del) {
                currentIvProfile      = iv;
                currentPersonIcon     = icon;
                currentIvDeleteCircle = del;
                openCameraWithUri(this);
            }
        };
    }

    // ─────────────────────────────────────────────────────────────────────────
    private void loadmembersviewlist(JSONArray jsonArray) {
        membersviewlist.clear();
        try {
            for (int i = 0; i < jsonArray.length(); i++) {
                JSONObject jsonObject = jsonArray.getJSONObject(i);
                MemberProfileModel model = new MemberProfileModel();

                String fullName = jsonObject.optString("name");
                if (!fullName.isEmpty()) {
                    String[] parts = fullName.split(" ", 2);
                    model.setFirst_name(parts[0]);
                    model.setLast_name(parts.length > 1 ? parts[1] : "");
                }
                model.setId(jsonObject.optString("id"));
                model.setEmail(jsonObject.optString("email"));
                model.setPractice(jsonObject.optString("practice"));
                model.setDesignation(jsonObject.optString("designation"));
                model.setPhone(jsonObject.optString("phone"));
                membersviewlist.add(model);
            }

            rv_view_pp.setVisibility(
                    membersviewlist.isEmpty() ? View.GONE : View.VISIBLE);

            adapter            = buildAdapter();
            firmProfileAdapter = adapter;
            Constants.firmProfileAdapter = adapter;

            rv_view_pp.setLayoutManager(new LinearLayoutManager(getContext()));
            AndroidUtils.setupBottomSpacerFooter(
                    rv_view_pp,
                    getResources().getDimensionPixelSize(R.dimen.twentyeight_dp));
            rv_view_pp.setAdapter(adapter);

            tl_search_pp.setVisibility(View.GONE);
            rv_view_pp.setVisibility(View.VISIBLE);

            et_search_pp.addTextChangedListener(new TextWatcher() {
                @Override public void beforeTextChanged(CharSequence s, int st, int c, int a) {}
                @Override public void onTextChanged(CharSequence s, int st, int b, int c) {}
                @Override
                public void afterTextChanged(Editable s) {
                    adapter.getFilter().filter(s);
                }
            });

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    // API response handler
    // ─────────────────────────────────────────────────────────────────────────
    @Override
    public void onAsyncTaskComplete(HttpResultDo httpResult) {
        if (progress_dialog != null && progress_dialog.isShowing())
            AndroidUtils.dismiss_dialog(progress_dialog);

        if (httpResult.getResult() == WebServiceHelper.ServiceCallStatus.Success) {
            try {
                JSONObject result = new JSONObject(httpResult.getResponseContent());
                Log.d("Request_Type", httpResult.getRequestType());

                if (Objects.equals(httpResult.getRequestType(), "View_Pp")) {
                    boolean isError = result.optBoolean("error");
                    if (!isError) {
                        JSONArray members = result.optJSONArray("data");
                        if (members != null) loadmembersviewlist(members);
                    }

                } else if (Objects.equals(httpResult.getRequestType(), "View_Bp")) {
                    boolean isError = result.optBoolean("error");
                    if (!isError) {
                        firmProfileModelArrayList.clear();

                        FirmProfileModel firmProfileModel = new FirmProfileModel();
                        firmProfileModel.setError(false);

                        JSONObject profileObj = Objects.requireNonNull(
                                        result.optJSONObject("data"))
                                .optJSONObject("profile");

                        if (profileObj != null) {
                            FirmProfileModel.Profile profile =
                                    new FirmProfileModel.Profile();
                            profile.setUid(profileObj.optString("uid"));
                            profile.setName(profileObj.optString("name"));
                            profile.setProfile_completion_percentage(
                                    profileObj.optInt("profile_completion_percentage"));
                            profile.setShow_profile_banner(
                                    profileObj.optBoolean("show_profile_banner"));
                            profile.setEmail(profileObj.optString("email"));
                            profile.setMobile(profileObj.optString("mobile"));
                            profile.setNationality(profileObj.optString("nationality"));
                            profile.setDate_of_birth(profileObj.optString("date_of_birth"));
                            profile.setGender(profileObj.optString("gender"));
                            profile.setBio_description(profileObj.optString("bio_description"));

                            String picUrl = profileObj.optString("profile_pic_url", "");
                            profile.setProfile_pic_url(picUrl);
                            if (Constants.isMyProfileClicked) profileUrl = picUrl;

                            profile.setBar_council_id(profileObj.optString("bar_council_id"));
                            profile.setYears_of_experience(
                                    profileObj.optInt("years_of_experience"));

                            // Consultation Fee
                            JSONObject feeObj = profileObj.optJSONObject("consultation_fee");
                            if (feeObj != null) {
                                FirmProfileModel.ConsultationFee cf =
                                        new FirmProfileModel.ConsultationFee();
                                cf.setAmount(feeObj.optString("amount", ""));
                                cf.setSymbol(feeObj.optString("symbol", "₹"));
                                profile.setConsultation_fee(cf);
                            }

                            profile.setPractice_areas(profileObj.optJSONArray("practice_areas"));
                            profile.setLanguages_spoken(profileObj.optJSONArray("languages_spoken"));
                            profile.setServices_offered(profileObj.optJSONArray("services_offered"));

                            // Firm
                            JSONObject firmObj = profileObj.optJSONObject("firm");
                            if (firmObj != null) {
                                FirmProfileModel.Firm firm = new FirmProfileModel.Firm();
                                firm.setFullname(firmObj.optString("fullname"));
                                firm.setEmail(firmObj.optString("email"));
                                firm.setBilling_currency(firmObj.optString("billing_currency"));
                                firm.setContact_phone(firmObj.optString("contact_phone"));
                                firm.setContact_person(firmObj.optString("contact_person"));
                                firm.setFirm_description(firmObj.optString("firm_description"));
                                firm.setWebsite(firmObj.optString("website"));
                                firm.setProfile_completion_percentage(
                                        firmObj.optInt("profile_completion_percentage"));
                                firm.setShow_profile_banner(
                                        firmObj.optBoolean("show_profile_banner"));
                                firm.setReg_id(firmObj.optString("reg_id"));
                                firm.setYears_of_incorporation(
                                        firmObj.optInt("years_of_incorporation"));
                                firm.setProfile_pic_url(firmObj.optString("profile_pic_url", ""));

                                // Firm profile_completion
                                JSONObject pcObj = firmObj.optJSONObject("profile_completion");
                                if (pcObj != null) {
                                    FirmProfileModel.ProfileCompletion pc =
                                            new FirmProfileModel.ProfileCompletion();
                                    pc.setCompletion_percentage(
                                            pcObj.optInt("completion_percentage"));
                                    JSONObject nsObj = pcObj.optJSONObject("next_step");
                                    if (nsObj != null) {
                                        FirmProfileModel.NextStep ns =
                                                new FirmProfileModel.NextStep();
                                        ns.setSection(nsObj.optString("section"));
                                        ns.setPriority(nsObj.optInt("priority"));
                                        ns.setRedirect_tab(nsObj.optString("redirect_tab"));
                                        JSONArray mf = nsObj.optJSONArray("missing_fields");
                                        if (mf != null) {
                                            List<String> fields = new ArrayList<>();
                                            for (int i = 0; i < mf.length(); i++)
                                                fields.add(mf.optString(i));
                                            ns.setMissing_fields(fields);
                                        }
                                        pc.setNext_step(ns);
                                    }
                                    JSONArray allInc = pcObj.optJSONArray("all_incomplete");
                                    if (allInc != null) {
                                        List<FirmProfileModel.NextStep> list = new ArrayList<>();
                                        for (int i = 0; i < allInc.length(); i++) {
                                            JSONObject sObj = allInc.optJSONObject(i);
                                            if (sObj == null) continue;
                                            FirmProfileModel.NextStep s =
                                                    new FirmProfileModel.NextStep();
                                            s.setSection(sObj.optString("section"));
                                            s.setPriority(sObj.optInt("priority"));
                                            s.setRedirect_tab(sObj.optString("redirect_tab"));
                                            JSONArray mf2 = sObj.optJSONArray("missing_fields");
                                            if (mf2 != null) {
                                                List<String> fl = new ArrayList<>();
                                                for (int j = 0; j < mf2.length(); j++)
                                                    fl.add(mf2.optString(j));
                                                s.setMissing_fields(fl);
                                            }
                                            list.add(s);
                                        }
                                        pc.setAll_incomplete(list);
                                    }
                                    firm.setProfile_completion(pc);
                                }

                                firm.setPractice_areas(firmObj.optJSONArray("practice_areas"));
                                firm.setServices_offered(firmObj.optJSONArray("services_offered"));

                                // Firm awards
                                JSONArray firmAwardArr = firmObj.optJSONArray("awards");
                                if (firmAwardArr != null) {
                                    List<FirmProfileModel.Award> awards = new ArrayList<>();
                                    for (int i = 0; i < firmAwardArr.length(); i++) {
                                        JSONObject aObj = firmAwardArr.optJSONObject(i);
                                        if (aObj == null) continue;
                                        FirmProfileModel.Award a = new FirmProfileModel.Award();
                                        a.setAward_name(aObj.optString("award_name"));
                                        a.setYear_of_award(aObj.optInt("year_of_award"));
                                        a.setPurpose(aObj.optString("purpose"));
                                        awards.add(a);
                                    }
                                    firm.setAwards(awards);
                                }

                                // Address
                                JSONObject addrObj = firmObj.optJSONObject("address");
                                if (addrObj != null) {
                                    FirmProfileModel.Address addr = new FirmProfileModel.Address();
                                    addr.setHouse_flat_no(addrObj.optString("house_flat_no"));
                                    addr.setStreet(addrObj.optString("street"));
                                    addr.setCity_town(addrObj.optString("city_town"));
                                    addr.setState(addrObj.optString("state"));
                                    addr.setCountry(addrObj.optString("country"));
                                    addr.setZipcode(addrObj.optString("zipcode"));
                                    firm.setAddress(addr);
                                }

                                // Correspondence address
                                JSONObject corrObj =
                                        firmObj.optJSONObject("correspondence_address");
                                if (corrObj != null) {
                                    FirmProfileModel.Address corr = new FirmProfileModel.Address();
                                    corr.setHouse_flat_no(corrObj.optString("house_flat_no"));
                                    corr.setStreet(corrObj.optString("street"));
                                    corr.setCity_town(corrObj.optString("city_town"));
                                    corr.setState(corrObj.optString("state"));
                                    corr.setCountry(corrObj.optString("country"));
                                    corr.setZipcode(corrObj.optString("zipcode"));
                                    firm.setCorrespondence_address(corr);
                                }

                                profile.setFirm(firm);
                            }

                            // Availability
                            JSONObject availObj = profileObj.optJSONObject("availability");
                            if (availObj != null) {
                                FirmProfileModel.Availability avail =
                                        new FirmProfileModel.Availability();
                                avail.setTimezone(availObj.optString("timezone"));
                                avail.setSlot_duration(availObj.optInt("slot_duration"));
                                avail.setBuffer_time(availObj.optInt("buffer_time"));
                                avail.setAdvance_booking_window_hours(
                                        availObj.optInt("advance_booking_window_hours"));

                                JSONArray weeklyArr = availObj.optJSONArray("weekly_schedule");
                                if (weeklyArr != null) {
                                    List<FirmProfileModel.WeeklySchedule> weekList = new ArrayList<>();
                                    for (int i = 0; i < weeklyArr.length(); i++) {
                                        JSONObject dObj = weeklyArr.optJSONObject(i);
                                        if (dObj == null) continue;
                                        FirmProfileModel.WeeklySchedule sched =
                                                new FirmProfileModel.WeeklySchedule();
                                        sched.setDay_of_week(dObj.optInt("day_of_week"));
                                        sched.setDate_label(dObj.optString("date_label"));
                                        sched.setDate(dObj.optString("date"));
                                        sched.setDay_name(dObj.optString("day_name"));
                                        sched.setIs_working_day(dObj.optBoolean("is_working_day"));

                                        JSONArray slotArr = dObj.optJSONArray("work_slots");
                                        List<FirmProfileModel.WorkSlot> slots = new ArrayList<>();
                                        if (slotArr != null) {
                                            for (int j = 0; j < slotArr.length(); j++) {
                                                JSONObject slObj = slotArr.optJSONObject(j);
                                                if (slObj == null) continue;
                                                FirmProfileModel.WorkSlot sl =
                                                        new FirmProfileModel.WorkSlot();
                                                sl.setStart_time(slObj.optString("start_time"));
                                                sl.setEnd_time(slObj.optString("end_time"));
                                                slots.add(sl);
                                            }
                                        }
                                        sched.setWork_slots(slots);

                                        JSONArray expArr = dObj.optJSONArray("expert_slots");
                                        List<FirmProfileModel.WorkSlot> expSlots = new ArrayList<>();
                                        if (expArr != null) {
                                            for (int j = 0; j < expArr.length(); j++) {
                                                JSONObject eObj = expArr.optJSONObject(j);
                                                if (eObj == null) continue;
                                                FirmProfileModel.WorkSlot es =
                                                        new FirmProfileModel.WorkSlot();
                                                es.setStart_time(eObj.optString("start_time"));
                                                es.setEnd_time(eObj.optString("end_time"));
                                                expSlots.add(es);
                                            }
                                        }
                                        sched.setExpert_slots(expSlots);
                                        weekList.add(sched);
                                    }
                                    avail.setWeekly_schedule(weekList);
                                }
                                profile.setAvailability(avail);
                            }

                            // Education
                            JSONArray eduArr = profileObj.optJSONArray("education");
                            if (eduArr != null) {
                                List<FirmProfileModel.Education> eduList = new ArrayList<>();
                                for (int i = 0; i < eduArr.length(); i++) {
                                    JSONObject eObj = eduArr.optJSONObject(i);
                                    if (eObj == null) continue;
                                    FirmProfileModel.Education edu = new FirmProfileModel.Education();
                                    edu.setDegree(eObj.optString("degree"));
                                    edu.setUniversity(eObj.optString("university"));
                                    edu.setPassing_year(eObj.optInt("passing_year"));
                                    eduList.add(edu);
                                }
                                profile.setEducation(eduList);
                            }

                            // Certifications
                            JSONArray certArr = profileObj.optJSONArray("certifications");
                            if (certArr != null) {
                                List<FirmProfileModel.Certification> certList = new ArrayList<>();
                                for (int i = 0; i < certArr.length(); i++) {
                                    JSONObject cObj = certArr.optJSONObject(i);
                                    if (cObj == null) continue;
                                    FirmProfileModel.Certification cert =
                                            new FirmProfileModel.Certification();
                                    cert.setCertification_name(cObj.optString("certification_name"));
                                    cert.setIssuing_authority(cObj.optString("issuing_authority"));
                                    cert.setYear_of_issue(cObj.optInt("year_of_issue"));
                                    certList.add(cert);
                                }
                                profile.setCertifications(certList);
                            }

                            // Profile-level awards
                            JSONArray awardArr = profileObj.optJSONArray("awards");
                            if (awardArr != null) {
                                List<FirmProfileModel.Award> awardList = new ArrayList<>();
                                for (int i = 0; i < awardArr.length(); i++) {
                                    JSONObject aObj = awardArr.optJSONObject(i);
                                    if (aObj == null) continue;
                                    FirmProfileModel.Award aw = new FirmProfileModel.Award();
                                    aw.setAward_name(aObj.optString("award_name"));
                                    aw.setYear_of_award(aObj.optInt("year_of_award"));
                                    aw.setPurpose(aObj.optString("purpose"));
                                    awardList.add(aw);
                                }
                                profile.setAwards(awardList);
                            }

                            // Cases Handled
                            JSONArray casesArr = profileObj.optJSONArray("cases_handled");
                            if (casesArr != null) {
                                List<String> casesList = new ArrayList<>();
                                for (int i = 0; i < casesArr.length(); i++)
                                    casesList.add(casesArr.optString(i));
                                profile.setCases_handled(casesList);
                            }

                            // Court Enrollments
                            JSONArray courtArr = profileObj.optJSONArray("court_enrollments");
                            if (courtArr != null) {
                                List<FirmProfileModel.CourtEnrollment> courtList = new ArrayList<>();
                                for (int i = 0; i < courtArr.length(); i++) {
                                    JSONObject cObj = courtArr.optJSONObject(i);
                                    if (cObj == null) continue;
                                    FirmProfileModel.CourtEnrollment ce = new FirmProfileModel.CourtEnrollment();
                                    ce.setCourt_type(cObj.optString("court_type"));
                                    ce.setCourt_name(cObj.optString("court_name"));
                                    ce.setState(cObj.optString("state"));
                                    ce.setCity(cObj.optString("city"));
                                    courtList.add(ce);
                                }
                                AndroidUtils.setupEdgePaddingBehavior(rv_view_pp);
                                profile.setCourt_enrollments(courtList);
                            }

                            // Subscription
                            JSONObject subObj = profileObj.optJSONObject("subscription");
                            if (subObj != null) {
                                FirmProfileModel.Subscription sub =
                                        new FirmProfileModel.Subscription();
                                sub.setActivatedOn(subObj.optString("activatedOn"));
                                sub.setModel(subObj.optString("model"));
                                sub.setActive(subObj.optBoolean("isActive"));
                                sub.setStartDate(subObj.optString("startDate"));
                                sub.setEndDate(subObj.optString("endDate"));
                                sub.setValidityDays(subObj.optString("validityDays"));
                                sub.setNextBillingDate(subObj.optString("nextBillingDate"));
                                sub.setCanUpgrade(subObj.optBoolean("canUpgrade"));
                                sub.setCanPayNow(subObj.optBoolean("canPayNow"));

                                JSONObject planObj = subObj.optJSONObject("plan");
                                if (planObj != null) {
                                    FirmProfileModel.Plan plan = new FirmProfileModel.Plan();
                                    plan.setAmount(planObj.optInt("amount"));
                                    plan.setCycle(planObj.optString("cycle"));
                                    plan.setName(planObj.optString("name"));
                                    plan.setCurrencySymbol(planObj.optString("currencySymbol"));
                                    plan.setCurrencyCode(planObj.optString("currencyCode"));
                                    sub.setPlan(plan);
                                }

                                JSONObject payObj = subObj.optJSONObject("payment");
                                if (payObj != null) {
                                    FirmProfileModel.Payment pay = new FirmProfileModel.Payment();
                                    pay.setMethod(payObj.optString("method"));
                                    pay.setMaskedId(payObj.optString("maskedId"));
                                    sub.setPayment(pay);
                                }
                                profile.setSubscription(sub);
                            }

                            profile.setAccepted_t_c_date(
                                    profileObj.optString("accepted_t&c_date", ""));

                            // Profile-level profile_completion
                            JSONObject pcObj2 = profileObj.optJSONObject("profile_completion");
                            if (pcObj2 != null) {
                                FirmProfileModel.ProfileCompletion pc2 =
                                        new FirmProfileModel.ProfileCompletion();
                                pc2.setCompletion_percentage(
                                        pcObj2.optInt("completion_percentage"));

                                JSONObject nsObj2 = pcObj2.optJSONObject("next_step");
                                if (nsObj2 != null) {
                                    FirmProfileModel.NextStep ns2 =
                                            new FirmProfileModel.NextStep();
                                    ns2.setSection(nsObj2.optString("section"));
                                    ns2.setPriority(nsObj2.optInt("priority"));
                                    ns2.setRedirect_tab(nsObj2.optString("redirect_tab"));
                                    JSONArray mf2 = nsObj2.optJSONArray("missing_fields");
                                    if (mf2 != null) {
                                        List<String> fields = new ArrayList<>();
                                        for (int i = 0; i < mf2.length(); i++)
                                            fields.add(mf2.optString(i));
                                        ns2.setMissing_fields(fields);
                                    }
                                    pc2.setNext_step(ns2);
                                }

                                JSONArray allInc2 = pcObj2.optJSONArray("all_incomplete");
                                if (allInc2 != null) {
                                    List<FirmProfileModel.NextStep> list2 = new ArrayList<>();
                                    for (int i = 0; i < allInc2.length(); i++) {
                                        JSONObject sObj = allInc2.optJSONObject(i);
                                        if (sObj == null) continue;
                                        FirmProfileModel.NextStep s =
                                                new FirmProfileModel.NextStep();
                                        s.setSection(sObj.optString("section"));
                                        s.setPriority(sObj.optInt("priority"));
                                        s.setRedirect_tab(sObj.optString("redirect_tab"));
                                        JSONArray mf3 = sObj.optJSONArray("missing_fields");
                                        if (mf3 != null) {
                                            List<String> fl = new ArrayList<>();
                                            for (int j = 0; j < mf3.length(); j++)
                                                fl.add(mf3.optString(j));
                                            s.setMissing_fields(fl);
                                        }
                                        list2.add(s);
                                    }
                                    pc2.setAll_incomplete(list2);
                                }
                                profile.setProfile_completion(pc2);
                            }

                            FirmProfileModel.Data data = new FirmProfileModel.Data();
                            data.setProfile(profile);
                            firmProfileModel.setData(data);
                        }

                        firmProfileModelArrayList.add(firmProfileModel);
                        Constants.firmProfileModel = firmProfileModel;

                        adapter            = buildAdapter();
                        firmProfileAdapter = adapter;
                        Constants.firmProfileAdapter = adapter;

                        rv_view_pp.setLayoutManager(new LinearLayoutManager(getContext()));
                        rv_view_pp.setAdapter(
                                Constants.isMyProfileClicked ? adapter : firmProfileAdapter);
                        AndroidUtils.setupBottomSpacerFooter(
                                rv_view_pp,
                                getResources().getDimensionPixelSize(R.dimen.twentyeight_dp));
                        tl_search_pp.setVisibility(View.GONE);
                        rv_view_pp.setVisibility(View.VISIBLE);
                    }

                } else if (Objects.equals(httpResult.getRequestType(), "Delete_Pp")) {
                    boolean isError = result.optBoolean("error");
                    if (!isError)
                        AndroidUtils.showAlert(result.optString("msg"), getActivity());
                    callViewPracticePartners();
                }

            } catch (Exception e) {
                e.fillInStackTrace();
            }

        } else if (httpResult.getResult() == WebServiceHelper.ServiceCallStatus.Failed) {
            if (progress_dialog != null && progress_dialog.isShowing())
                AndroidUtils.dismiss_dialog(progress_dialog);
            try {
                JSONObject result = new JSONObject(httpResult.getResponseContent());
                AndroidUtils.showErrorAlert(result.optString("msg"), getActivity());
            } catch (Exception e) {
                e.fillInStackTrace();
            }
        } else {
            if (progress_dialog != null && progress_dialog.isShowing())
                AndroidUtils.dismiss_dialog(progress_dialog);
            AndroidUtils.showErrorAlert(
                    httpResult.getResponseContent().toString(), getActivity());
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    // InterfaceListener callbacks
    // ─────────────────────────────────────────────────────────────────────────
    @Override
    public void EditPracticePartners(MemberProfileModel model) {
        Constants.PpView_Type = "update";
        firmProfile.NavFragment(new PracticePartnerAdd(model, firmProfile));
    }

    @Override
    public void EditProfile(Boolean isProfileEdit, Boolean isAdditionalInfo) {
        if (!Constants.isMyProfileClicked) {
            firmProfile.NavFragment(isProfileEdit
                    ? new BasicProfileEdit(firmProfile, true, false)
                    : new BasicProfileEdit(firmProfile, false, true));
        } else {
            firmProfile.NavFragment(
                    new BasicProfileEdit(firmProfile, isProfileEdit, isAdditionalInfo));
        }
    }

    @Override
    public void DeletePracticePartners(MemberProfileModel model) {
        Delete_Popup(getActivity(),
                model.first_name + " " + model.getLast_name(),
                model.getId());
    }

    public void add_partner() {
        firmProfile.NavFragment(PracticePartnerView.newInstance(firmProfile));
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Delete confirmation popup
    // ─────────────────────────────────────────────────────────────────────────
    public void Delete_Popup(Activity activity, String name, String id) {
        try {
            AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(activity);
            LayoutInflater inflater = activity.getLayoutInflater();
            final View dialogLayout =
                    inflater.inflate(R.layout.delete_relationship, null);
            TextView header = dialogLayout.findViewById(R.id.header_name);
            header.setText(R.string.confirmation);
            header.setTextColor(
                    ContextCompat.getColor(header.getContext(), R.color.Primary_new));

            ImageView close  = dialogLayout.findViewById(R.id.close_documents);
            TextView  msg    = dialogLayout.findViewById(R.id.tv_confirmation);
            Button    btnYes = dialogLayout.findViewById(R.id.btn_yes);
            Button    btnNo  = dialogLayout.findViewById(R.id.btn_No);

            btnYes.setBackgroundDrawable(
                    getContext().getResources().getDrawable(R.drawable.yes_button_red_button));
            btnNo.setBackgroundDrawable(
                    getContext().getResources().getDrawable(R.drawable.no_button_green_button));

            String confirmText = "Are you sure, Do you want to delete this " + name + " ?";
            SpannableString spannable = new SpannableString(confirmText);
            spannable.setSpan(
                    new ForegroundColorSpan(getContext().getColor(R.color.black)),
                    0, confirmText.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            if (name != null && !name.isEmpty()) {
                int start = confirmText.indexOf(name);
                if (start >= 0) {
                    spannable.setSpan(new StyleSpan(Typeface.BOLD),
                            start, start + name.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                }
            }
            msg.setText(spannable);

            final AlertDialog dialog = dialogBuilder.create();
            btnNo.setOnClickListener(v -> dialog.dismiss());
            close.setOnClickListener(v -> dialog.dismiss());
            btnYes.setOnClickListener(v -> {
                dialog.dismiss();
                callDeletePracticePartners(id);
            });
            dialog.setView(dialogLayout);
            dialog.setCanceledOnTouchOutside(false);
            dialog.show();

        } catch (Exception e) {
            e.fillInStackTrace();
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    // onViewCreated
    // ─────────────────────────────────────────────────────────────────────────
    @Override
    public void onViewCreated(@NonNull View v, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(v, savedInstanceState);
        try {
            tl_search_pp = v.findViewById(R.id.tl_search_pp);
            et_search_pp = tl_search_pp.findViewById(R.id.et_Search);
            rv_view_pp   = v.findViewById(R.id.rv_view_pp);
            et_search_pp.setHint(R.string.search);

            if (!Constants.Profile_View.equals("Bp")) {
                tl_search_pp.setVisibility(View.VISIBLE);
                rv_view_pp.setVisibility(View.VISIBLE);
                callViewPracticePartners();
            } else {
                tl_search_pp.setVisibility(View.GONE);
                rv_view_pp.setVisibility(View.VISIBLE);
                callViewBasicProfile();
            }

        } catch (Resources.NotFoundException e) {
            e.fillInStackTrace();
        }
    }
}