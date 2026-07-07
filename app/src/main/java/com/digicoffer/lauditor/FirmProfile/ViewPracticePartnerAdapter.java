package com.digicoffer.lauditor.FirmProfile;

import static android.view.View.GONE;
import static android.view.View.VISIBLE;

import android.animation.ObjectAnimator;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Dialog;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.Handler;
import android.os.Looper;
import android.provider.MediaStore;
import android.text.Layout;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.TextPaint;
import android.text.TextUtils;
import android.text.method.LinkMovementMethod;
import android.text.style.ClickableSpan;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.widget.Button;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.GridLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.annotation.NonNull;
import androidx.appcompat.widget.AppCompatImageView;
import androidx.cardview.widget.CardView;
import androidx.core.content.res.ResourcesCompat;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.digicoffer.lauditor.Groups.Models.ActionModel;
import com.digicoffer.lauditor.MainActivity;
import com.digicoffer.lauditor.R;
import com.digicoffer.lauditor.Webservice.AsyncTaskCompleteListener;
import com.digicoffer.lauditor.Webservice.HttpResultDo;
import com.digicoffer.lauditor.Webservice.CommonApiHelper.WebServiceHelper;
import com.digicoffer.lauditor.CommonFiles.GlobalFiles.AndroidUtils;
import com.digicoffer.lauditor.CommonFiles.CacheUtils.AppImageCache;
import com.digicoffer.lauditor.CommonFiles.GlobalFiles.Constants;
import com.digicoffer.lauditor.CommonFiles.GlobalFiles.DynamicUtils;
import com.digicoffer.lauditor.CommonFiles.FileSelection.Filecosen;
import com.digicoffer.lauditor.CommonFiles.CommonAdapters.CommonSpinnerAdapter;
import com.google.android.material.tabs.TabLayout;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

/**
 * Unified adapter for the Profile module.
 * <p>
 * MODE A — Practice-Partner list  (Constants.Profile_View != "Bp")
 * MODE B — Profile card  (Constants.Profile_View == "Bp")
 * <p>
 * "Complete Now" navigates to the EXACT missing field:
 * • Reads missing_fields[] from the API next_step object
 * • Maps each field name to a (section, targetFieldId) pair
 * • Stores Constants.TARGET_FIELD so BasicProfileEdit can scroll + highlight it
 */
public class ViewPracticePartnerAdapter
        extends RecyclerView.Adapter<ViewPracticePartnerAdapter.MyViewHolder>
        implements Filterable, AsyncTaskCompleteListener {

    // ─────────────────────────────────────────────────────────────────────────
    //  Constants
    // ─────────────────────────────────────────────────────────────────────────
    private static final long MAX_FILE_SIZE_BYTES = 2 * 1024 * 1024L;
    private static final int INITIAL_QUALITY = 90;
    private static final int MIN_QUALITY = 40;
    private static final int QUALITY_STEP = 10;
    CardView profileCompletionBanner;
    // ─────────────────────────────────────────────────────────────────────────
    //  Fields
    // ─────────────────────────────────────────────────────────────────────────
    ArrayList<MemberProfileModel> membersviewlist;
    ArrayList<MemberProfileModel> itemsList;
    Uri cameraImageUri;
    Dialog progress_dialog;
    Activity activity;
    Context context;

    private ActivityResultLauncher<Uri> cameraLauncher;
    private ActivityResultLauncher<Intent> galleryLauncher;
    private ActivityResultLauncher<Intent> cropLauncher;
    private ActivityResultLauncher<String> requestCameraPermission;
    PracticePartnerView practicePartnerView;
    View view;
    InterfaceListener eventListener;
    ArrayList<FirmProfileModel> firmProfileModelArrayList;
    String profileUrl = "";
    ImageView currentIvProfile;
    TextView currentPersonIcon;
    ImageView currentIvEditCircle;
    ImageView currentIvDeleteCircle;

    private int expandedPosition = -1;
    private RecyclerView recyclerView;

    private enum ProfileCompletionStatus {
        BASIC_PROFILE_INCOMPLETE,
        ADDITIONAL_INFO_INCOMPLETE,
        COMPLETE
    }

    // ─────────────────────────────────────────────────────────────────────────
    //  FieldDestination — holds section + exact view ID string for the field
    // ─────────────────────────────────────────────────────────────────────────
    private static class FieldDestination {
        /**
         * "basic" | "additional" | "education" | "availability" | "courts_cases"
         */
        final String section;
        /**
         * The string ID of the target view, e.g. "et_Email". Empty = scroll to top of section.
         */
        final String fieldId;
        /**
         * Which tab index to open (0=practice_details, 1=edu_awards, 2=availability, 3=courts_cases)
         */
        final int tabIndex;

        FieldDestination(String section, String fieldId, int tabIndex) {
            this.section = section;
            this.fieldId = fieldId;
            this.tabIndex = tabIndex;
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    //  Master field → destination map
    // ─────────────────────────────────────────────────────────────────────────

    /**
     * Maps an API missing_field name to the (section, view-id, tab) that contains it.
     * Returns null if the field is unknown / optional.
     */
    private FieldDestination resolveFieldDestination(String apiField) {
        if (apiField == null) return null;
        switch (apiField.toLowerCase().trim()) {

            // ── Basic Profile fields ──────────────────────────────────────
            case "firm_name":
            case "fullname":
                return new FieldDestination("basic", "et_Firmname", 0);

            case "contact_person":
            case "name":
                return new FieldDestination("basic", "et_contactName", 0);

            case "email":
                return new FieldDestination("basic", "et_Email", 0);

            case "contact_phone":
            case "mobile":
            case "phone":
                return new FieldDestination("basic", "et_ContactPhone", 0);

            case "billing_currency":
            case "default_currency":
                return new FieldDestination("additional", "ll_et_DefaultCurrency", 0);

            case "website":
                return new FieldDestination("basic", "et_Website", 0);

            case "bio_description":
            case "firm_description":
            case "bio":
            case "about":
                return new FieldDestination("basic", "et_bio", 0);

            case "consultation_fee":
                return new FieldDestination("additional", "ll_et_confees", 0);

            case "gender":
                return new FieldDestination("basic", "ll_et_gender", 0);

            case "date_of_birth":
            case "dob":
                return new FieldDestination("basic", "et_dob", 0);

            case "nationality":
                return new FieldDestination("basic", "et_nationality", 0);

            // ── Registered Address ────────────────────────────────────────
            case "house_flat_no":
            case "address_house":
                return new FieldDestination("basic", "et_Building", 0);

            case "street":
            case "address_street":
                return new FieldDestination("basic", "et_Street", 0);

            case "city_town":
            case "address_city":
                return new FieldDestination("basic", "et_City", 0);

            case "state":
            case "address_state":
                return new FieldDestination("basic", "et_State", 0);

            case "country":
            case "address_country":
                return new FieldDestination("basic", "ll_et_ad_country", 0);

            case "zipcode":
            case "zip":
            case "address_zip":
                return new FieldDestination("basic", "et_Zip", 0);

            case "address":
                return new FieldDestination("basic", "et_Building", 0);

            // ── Mailing / Correspondence Address ──────────────────────────
            case "correspondence_address":
            case "mailing_address":
                return new FieldDestination("basic", "met_Building", 0);

            case "correspondence_house_flat_no":
                return new FieldDestination("basic", "met_Building", 0);

            case "correspondence_street":
                return new FieldDestination("basic", "met_Street", 0);

            case "correspondence_city":
            case "correspondence_city_town":
                return new FieldDestination("basic", "met_City", 0);

            case "correspondence_state":
                return new FieldDestination("basic", "met_State", 0);

            case "correspondence_country":
                return new FieldDestination("basic", "ll_met_ad_country", 0);

            case "correspondence_zipcode":
            case "correspondence_zip":
                return new FieldDestination("basic", "met_Zip", 0);

            // ── Profile picture / logo ────────────────────────────────────
            case "profile_pic":
            case "logo":
            case "firm_logo":
            case "profile_picture":
                return new FieldDestination("pic", "iv_edit_circle", 0);

            // ── Practice Details (Additional Info tab 0) ──────────────────
            case "bar_council_id":
            case "reg_id":
            case "registration_id":
                return new FieldDestination("additional", "et_counsel_id", 0);

            case "years_of_experience":
            case "years_of_incorporation":
            case "experience":
                return new FieldDestination("additional", "et_year_exp", 0);

            case "practice_areas":
                return new FieldDestination("additional", "tl_practice_area", 0);

            case "languages_spoken":
            case "languages":
                return new FieldDestination("additional", "et_languages", 0);

            case "services_offered":
            case "services":
                return new FieldDestination("additional", "et_services", 0);

            // ── Courts & Cases tab (tab index 1) ──────────────────────────
            case "cases_handled":
            case "case_types":
                return new FieldDestination("courts_cases", "et_cases_handled", 1);

            case "court_enrollments":
            case "court_details":
                return new FieldDestination("courts_cases", "ll_court_enrollments", 1);

            // ── Practice Details address fields (same tab) ────────────────
            case "practice_address":
            case "office_address":
                return new FieldDestination("additional", "et_Building", 0);

            // ── Education & Awards tab (tab index 2) ──────────────────────
            case "education":
            case "degree":
            case "university":
                return new FieldDestination("education", "ll_education", 2);

            case "certifications":
            case "certification":
                return new FieldDestination("education", "ll_certification", 2);

            case "awards":
            case "award":
                return new FieldDestination("education", "ll_awards", 2);

            // ── Availability tab (tab index 3) ────────────────────────────
            case "availability":
            case "weekly_schedule":
            case "slot_duration":
            case "working_hours":
            case "work_slots":
                return new FieldDestination("availability", "ll_days_container", 3);

            default:
                return null;
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    //  Constructor
    // ─────────────────────────────────────────────────────────────────────────
    public ViewPracticePartnerAdapter(
            ArrayList<MemberProfileModel> membersviewlist,
            String profileUrl,
            ArrayList<FirmProfileModel> firmProfileModelArrayList,
            Activity activity,
            Context context,
            PracticePartnerView practicePartnerView,
            InterfaceListener eventListener,
            ActivityResultLauncher<Uri> cameraLauncher,
            ActivityResultLauncher<Intent> galleryLauncher,
            ActivityResultLauncher<Intent> cropLauncher,
            ActivityResultLauncher<String> requestCameraPermission) {

        this.practicePartnerView = practicePartnerView;
        this.eventListener = eventListener;
        this.membersviewlist = membersviewlist;
        this.firmProfileModelArrayList = firmProfileModelArrayList;
        this.itemsList = membersviewlist;
        this.profileUrl = profileUrl;
        this.activity = activity;
        this.context = context;
        this.cameraLauncher = cameraLauncher;
        this.galleryLauncher = galleryLauncher;
        this.cropLauncher = cropLauncher;
        this.requestCameraPermission = requestCameraPermission;
    }

    // ─────────────────────────────────────────────────────────────────────────
    //  Key helper — mirrors BasicProfileEdit.isFirmProfile()
    // ─────────────────────────────────────────────────────────────────────────
    private boolean isFirmProfile() {
        return !Constants.isMyProfileClicked;
    }

    // ─────────────────────────────────────────────────────────────────────────
    //  RecyclerView reference + safe notify helpers
    // ─────────────────────────────────────────────────────────────────────────
    public void setRecyclerView(RecyclerView rv) {
        this.recyclerView = rv;
    }

    private void safeNotify(int position) {
        Runnable r = () -> {
            if (position >= 0 && position < getItemCount())
                notifyItemChanged(position);
        };
        if (recyclerView != null) recyclerView.post(r);
        else new Handler(Looper.getMainLooper()).post(r);
    }

    public void collapseExpanded() {
        if (expandedPosition == -1) return;
        int pos = expandedPosition;
        expandedPosition = -1;
        safeNotify(pos);
    }

    // ─────────────────────────────────────────────────────────────────────────
    //  Filter (Mode A only)
    // ─────────────────────────────────────────────────────────────────────────
    @Override
    public Filter getFilter() {
        return new Filter() {
            @Override
            protected FilterResults performFiltering(CharSequence charSequence) {
                String cs = charSequence.toString().toLowerCase();
                if (cs.isEmpty()) {
                    membersviewlist = itemsList;
                } else {
                    ArrayList<MemberProfileModel> filtered = new ArrayList<>();
                    for (MemberProfileModel row : itemsList) {
                        String fn = row.getFirst_name() != null ? row.getFirst_name().toLowerCase() : "";
                        String ln = row.getLast_name() != null ? row.getLast_name().toLowerCase() : "";
                        if (fn.contains(cs) || ln.contains(cs)) filtered.add(row);
                    }
                    membersviewlist = filtered;
                }
                FilterResults res = new FilterResults();
                res.count = membersviewlist.size();
                res.values = membersviewlist;
                return res;
            }

            @Override
            protected void publishResults(CharSequence c, FilterResults r) {
                membersviewlist = (ArrayList<MemberProfileModel>) r.values;
                notifyDataSetChanged();
            }
        };
    }

    // ─────────────────────────────────────────────────────────────────────────
    //  ViewHolder creation
    // ─────────────────────────────────────────────────────────────────────────
    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (!Constants.Profile_View.equals("Bp"))
            view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.view_pp_card, parent, false);
        else
            view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.basicprofile_view_card, parent, false);
        return new MyViewHolder(view);
    }

    // ─────────────────────────────────────────────────────────────────────────
    //  Item count
    // ─────────────────────────────────────────────────────────────────────────
    @Override
    public int getItemCount() {
        return Constants.Profile_View.equals("Bp")
                ? firmProfileModelArrayList.size()
                : membersviewlist.size();
    }

    // ─────────────────────────────────────────────────────────────────────────
    //  onBindViewHolder
    // ─────────────────────────────────────────────────────────────────────────
    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder,
                                 @SuppressLint("RecyclerView") int position) {

        // ══════════════════════════════════════════════════════════════════
        //  MODE A — Practice Partner list card
        // ══════════════════════════════════════════════════════════════════
        if (!Constants.Profile_View.equals("Bp")) {

            MemberProfileModel model = membersviewlist.get(position);
            holder.tv_name.setText(model.getFirst_name());
            holder.tv_designation_name.setText(model.getDesignation());
            holder.tv_specialist_name.setText(model.getPractice());
            holder.tv_email.setText(model.getEmail());
            holder.tv_phonenumber.setText(model.getPhone());

            if (holder.action_list_card != null)
                holder.action_list_card.setVisibility(View.GONE);

            toggleVisibility(holder.tv_phonenumber,
                    !TextUtils.isEmpty(model.getPhone()) && !"null".equals(model.getPhone()),
                    model.getPhone());
            toggleVisibility(holder.tv_specialist_name,
                    !TextUtils.isEmpty(model.getPractice()) && !"null".equals(model.getPractice()),
                    model.getPractice());

            ArrayList<ActionModel> actions = new ArrayList<>();
            actions.add(new ActionModel("Edit"));
            actions.add(new ActionModel("Delete"));

            boolean isExpanded = (position == expandedPosition);

            if (holder.iv_action_menu != null) holder.iv_action_menu.setOnClickListener(null);
            if (holder.action_list != null) holder.action_list.setOnItemClickListener(null);

            if (isExpanded && holder.action_list != null) {
                CommonSpinnerAdapter adapter = new CommonSpinnerAdapter((Activity) context, actions);
                holder.action_list.setAdapter(adapter);
                holder.action_list.post(() -> AndroidUtils.setDynamicHeight(holder.action_list));
                if (holder.action_list_card != null)
                    holder.action_list_card.setVisibility(View.VISIBLE);
            } else {
                if (holder.action_list != null) holder.action_list.setAdapter(null);
                if (holder.action_list_card != null)
                    holder.action_list_card.setVisibility(View.GONE);
            }

            if (holder.iv_action_menu != null) {
                holder.iv_action_menu.setOnClickListener(v -> {
                    int cur = holder.getAdapterPosition();
                    if (cur == RecyclerView.NO_POSITION) return;
                    int prev = expandedPosition;
                    expandedPosition = (expandedPosition == cur) ? -1 : cur;
                    if (prev != -1 && prev != cur) safeNotify(prev);
                    safeNotify(cur);
                });
            }

            if (holder.action_list != null) {
                holder.action_list.setOnItemClickListener((parent, v, pos, id) -> {
                    int cur = holder.getAdapterPosition();
                    if (cur == RecyclerView.NO_POSITION) return;
                    String name = actions.get(pos).getName();
                    expandedPosition = -1;
                    safeNotify(cur);
                    new Handler(Looper.getMainLooper()).post(() -> dispatchAction(name, model));
                });
            }
            return;
        }

        // ══════════════════════════════════════════════════════════════════
        //  MODE B — Profile card
        // ══════════════════════════════════════════════════════════════════
        boolean fp = isFirmProfile();

        if (fp && Constants.PROFILE_EDIT_TAB.equals("availability")) {
            Constants.PROFILE_EDIT_TAB = "practice_details";
        }
        if (!fp && Constants.lastProfileContext != null && Constants.lastProfileContext.equals("firm")) {
            Constants.PROFILE_EDIT_TAB = "practice_details";
        } else if (fp && Constants.lastProfileContext != null && Constants.lastProfileContext.equals("my")) {
            Constants.PROFILE_EDIT_TAB = "practice_details";
        }
        Constants.lastProfileContext = fp ? "firm" : "my";

        boolean isGHTeamMember = Constants.ROLE.equalsIgnoreCase("GH")
                || Constants.ROLE.equalsIgnoreCase("TM");
        boolean isAMMorSU = Constants.ROLE.equalsIgnoreCase("AAM");

        // ── Profile image ───────────────────────────────────────────────
        String imageToLoad = "";
        if (fp) {
            FirmProfileModel.Profile prof0 = firmProfileModelArrayList.get(position).getData() != null
                    ? firmProfileModelArrayList.get(position).getData().getProfile() : null;
            String url = (prof0 != null && prof0.getFirm().getProfile_pic_url() != null)
                    ? prof0.getFirm().getProfile_pic_url() : "";
            if (!url.isEmpty()) {
                AppImageCache.preload(context, url);
                AndroidUtils.loadProfileImage(context, url, holder.iv_profile, holder.person_icon);
                animateScalePulse(holder.iv_profile);
                if (holder.iv_delete_circle != null)
                    holder.iv_delete_circle.setVisibility(VISIBLE);
            } else {
                if (holder.iv_delete_circle != null)
                    holder.iv_delete_circle.setVisibility(GONE);
            }
        } else {
            imageToLoad = Constants.firm_image;
        }

        AndroidUtils.loadProfileImage(context, imageToLoad, holder.iv_profile, holder.person_icon);

        if (!TextUtils.isEmpty(imageToLoad)) {
            holder.iv_delete_circle.setVisibility(VISIBLE);
        }

        // ── Section header ──────────────────────────────────────────────
        if (fp) {
            holder.tv_BasicProfile.setText("Firm Information");
        } else if (isAMMorSU) {
            holder.tv_BasicProfile.setText("Firm Profile");
        } else {
            holder.tv_BasicProfile.setText(context.getString(R.string.profile_inform));
        }
        holder.tv_BasicProfile.setTextSize(20);

        // ── Model extraction ────────────────────────────────────────────
        FirmProfileModel model = firmProfileModelArrayList.get(position);
        FirmProfileModel.Data data = model.getData();
        if (data == null) return;
        FirmProfileModel.Profile profile = data.getProfile();
        if (profile == null) return;
        FirmProfileModel.Firm firm = profile.getFirm();
        if (firm == null) return;
        FirmProfileModel.Subscription subscription = profile.getSubscription();

        // ── Debug logging ────────────────────────────────────────────────
        FirmProfileModel.ProfileCompletion pc1 = profile.getProfile_completion();
        FirmProfileModel.NextStep ns1 = pc1 != null ? pc1.getNext_step() : null;
        if (ns1 != null) {
            Log.d("REDIRECT_TAB_DEBUG", "section=" + ns1.getSection());
            Log.d("REDIRECT_TAB_DEBUG", "redirect_tab=" + ns1.getRedirect_tab());
            Log.d("REDIRECT_TAB_DEBUG", "priority=" + ns1.getPriority());
            Log.d("REDIRECT_TAB_DEBUG", "missing_fields=" + ns1.getMissing_fields());
        }

        holder.FirmName.setText(R.string.fn_);

        // ── Profile completion percentage ────────────────────────────────
        int percentage;
        if (fp) {
            FirmProfileModel.ProfileCompletion completion = firm.getProfile_completion();
            percentage = completion != null ? completion.getCompletion_percentage() : 0;
        } else {
            FirmProfileModel.ProfileCompletion completion = profile.getProfile_completion();
            percentage = completion != null ? completion.getCompletion_percentage() : 0;
        }
        holder.tvPercentage.setText(percentage + "%");
        if (fp) {
            if (holder.tvMessage != null) {
                holder.tvMessage.setText(percentage > 50
                        ? "Please complete your Firm profile to stand out and get discovered by more clients"
                        : "Firm Profile cannot go live if incomplete");
            }
        } else {
            if (holder.tvMessage != null) {
                holder.tvMessage.setText(percentage > 50
                        ? "Please complete your profile to stand out and get discovered by more clients"
                        : "Profile cannot go live if incomplete");
            }
        }

        // ── Consultation fee: hidden for Firm Profile ────────────────────

        // ── Section visibility rules ─────────────────────────────────────
        if (isGHTeamMember) {
            holder.ll_subcription.setVisibility(View.GONE);
            holder.ll_basicprofile.setVisibility(View.VISIBLE);
            holder.ll_additional_inform.setVisibility(View.VISIBLE);
            if (percentage == 100) {
                profileCompletionBanner.setVisibility(View.GONE);
            } else {
                profileCompletionBanner.setVisibility(View.VISIBLE);
                updateCompletionBanner(holder, model);
            }
        } else if (Constants.issubscription) {
            holder.ll_basicprofile.setVisibility(View.GONE);
            holder.ll_subcription.setVisibility(View.VISIBLE);
            profileCompletionBanner.setVisibility(View.GONE);
            holder.ll_additional_inform.setVisibility(View.GONE);
        } else {
            holder.ll_basicprofile.setVisibility(View.VISIBLE);
            holder.ll_subcription.setVisibility(View.GONE);
            profileCompletionBanner.setVisibility(percentage == 100 ? View.GONE : View.VISIBLE);
            if (percentage != 100)
                updateCompletionBanner(holder, model);
            holder.ll_additional_inform.setVisibility(View.VISIBLE);
        }

        // ── Firm name visibility ─────────────────────────────────────────
        if (!fp && "solo".equalsIgnoreCase(Constants.CATEGORY)) {
            holder.FirmName.setVisibility(View.GONE);
            holder.tv_FirmName.setVisibility(View.GONE);
        } else {
            holder.FirmName.setVisibility(VISIBLE);
            holder.tv_FirmName.setVisibility(VISIBLE);
            holder.tv_FirmName.setText(firm.getFullname());
        }

        holder.Country.setText(R.string.country);
        String countryVal = firm.getAddress() != null ? firm.getAddress().getCountry() : null;
        if (TextUtils.isEmpty(countryVal) || "null".equals(countryVal)) countryVal = "India";
        holder.tv_Country.setText(countryVal);

        holder.Email.setText(R.string.email);
        if (holder.tv_Email != null) {
            if ((isGHTeamMember || "solo".equals(Constants.CATEGORY))) {
                holder.tv_Email.setText(profile.getEmail());
            } else {
                if (fp) {
                    holder.tv_Email.setText(firm.getEmail());
                } else {
                    holder.tv_Email.setText(profile.getEmail());
                }
            }
        }

        // ── Contact name label / value ────────────────────────────────────
        if (isGHTeamMember || "solo".equalsIgnoreCase(Constants.CATEGORY)) {
            holder.ContactName.setText(R.string.name);
            holder.tv_ContactName.setText(profile.getName());
        } else {
            holder.ContactName.setText(R.string._contact_name);
            if (fp) {
                holder.tv_ContactName.setText(firm.getContact_person());
            } else {
                holder.tv_ContactName.setText(profile.getName());
            }
        }

        // ── Write fresh API values into Constants ────────────────────────
        String freshName = profile.getName();
        String freshFirmName = holder.tv_FirmName.getText().toString().trim();
        if (!TextUtils.isEmpty(freshName)) Constants.NAME = freshName;
        if (!TextUtils.isEmpty(freshFirmName)) Constants.FIRM_NAME = freshFirmName;

        // ── Set person_icon initial ──────────────────────────────────────
        if (holder.person_icon != null) {
            String initial;
            if (Constants.isMyProfileClicked && !TextUtils.isEmpty(Constants.NAME)) {
                initial = Constants.NAME.substring(0, 1).toUpperCase();
            } else if (!TextUtils.isEmpty(Constants.FIRM_NAME)) {
                initial = Constants.FIRM_NAME.substring(0, 1).toUpperCase();
            } else {
                initial = "?";
            }
            holder.person_icon.setText(initial);
        }

        // ── Push updated initial to MainActivity header ───────────────────
        if (activity instanceof MainActivity) {
            ((MainActivity) activity).updateProfileInitial();
        }

        try {
            Constants.jsonObject_dashboard.put("name", Constants.NAME);
            Constants.jsonObject_dashboard.put("firm_name", Constants.FIRM_NAME);
        } catch (Exception e) {
            e.fillInStackTrace();
        }

        // ── Phone ────────────────────────────────────────────────────────
        holder.ContactPhone.setText(R.string.phone_no);
        if (fp) {
            holder.tv_ContactPhone.setText(
                    firm.getContact_phone() != null ? firm.getContact_phone() : "");
        } else {
            holder.tv_ContactPhone.setText(profile.getMobile());
        }

        // ── Website ──────────────────────────────────────────────────────
        holder.Website.setText(R.string._website);
        holder.tv_Website.setText(firm.getWebsite());

        // ── DOB / Gender / Nationality ───────────────────────────────────
        holder.dob.setText(R.string.dob);
        holder.tv_dob.setText(profile.getDate_of_birth());
        holder.gender.setText(R.string.gender);
        holder.tv_gender.setText(profile.getGender());
        holder.nationality.setText(R.string.nationality);
        holder.tv_nationality.setText(profile.getNationality());

        // ── Bio label + value ─────────────────────────────────────────────
        if (holder.bio != null) {
            holder.bio.setText((fp || isAMMorSU)
                    ? "About the Firm"
                    : context.getString(R.string.bio));
        }
        String bioText = (fp || isAMMorSU)
                ? firm.getFirm_description()
                : profile.getBio_description();
        ReadMoreUtils.setReadMore(holder.tv_bio, bioText, 2);

        // ── Address labels ────────────────────────────────────────────────
        holder.tv_Address.setText(R.string.register_address);
        holder.tv_Address.setTextSize(20);
        holder.Building.setText(R.string.building_);
        holder.Street.setText(R.string.street_);
        holder.City.setText(R.string.city_);
        holder.State.setText(R.string.state_);
        holder.Ad_Country.setText(R.string.country_);
        holder.Zip.setText(R.string.zip_);
        holder.mtv_Address.setText(R.string.mailing_address);
        holder.mtv_Address.setTextSize(20);
        holder.mBuilding.setText(R.string.building_);
        holder.mStreet.setText(R.string.street_);
        holder.mCity.setText(R.string.city_);
        holder.mState.setText(R.string.state_);
        holder.mAd_Country.setText(R.string.country_);
        holder.mZip.setText(R.string.zip_);

        // ── Tab content ───────────────────────────────────────────────────
        holder.tabLayout.clearOnTabSelectedListeners();
        String currentTab = Constants.PROFILE_EDIT_TAB;
        boolean isAMMorSUInner = Constants.ROLE.equalsIgnoreCase("AAM");

        switch (currentTab) {
            case "practice_details":
                showPracticeDetails(holder, holder.getAdapterPosition());
                break;
            case "courts_cases":
                if (!fp && !isAMMorSUInner) createCourtsAndCasesView(holder, model);
                else showPracticeDetails(holder, holder.getAdapterPosition());
                break;
            case "education_awards":
                if (fp || isAMMorSUInner) createAwardsOnlyView(holder, model);
                else createEducationsAwardsView(holder, model);
                break;
            case "availability":
                if (!fp && !isAMMorSUInner) createAvailabilityView(holder, model);
                else showPracticeDetails(holder, holder.getAdapterPosition());
                break;
            default:
                showPracticeDetails(holder, holder.getAdapterPosition());
                break;
        }

        int tabIndexToSelect;
        switch (currentTab) {
            case "courts_cases":
                tabIndexToSelect = (fp || isAMMorSUInner) ? 0 : 1;
                break;
            case "education_awards":
                tabIndexToSelect = (fp || isAMMorSUInner) ? 1 : 2;
                break;
            case "availability":
                tabIndexToSelect = (fp || isAMMorSUInner) ? 0 : 3;
                break;
            default:
                tabIndexToSelect = 0;
                break;
        }
        TabLayout.Tab tabToSelect = holder.tabLayout.getTabAt(tabIndexToSelect);
        if (tabToSelect != null && !tabToSelect.isSelected()) tabToSelect.select();

        // ── Tab selected listener ─────────────────────────────────────────
        holder.tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                holder.ll_viewscreen.removeAllViews();
                boolean fpInner = isFirmProfile();
                boolean isAMMorSUBind = Constants.ROLE.equalsIgnoreCase("AAM");
                switch (tab.getPosition()) {
                    case 0:
                        Constants.PROFILE_EDIT_TAB = "practice_details";
                        showPracticeDetails(holder, holder.getAdapterPosition());
                        break;
                    case 1:
                        if (!fpInner && !isAMMorSUBind) {
                            Constants.PROFILE_EDIT_TAB = "courts_cases";
                            createCourtsAndCasesView(holder, model);
                        } else {
                            Constants.PROFILE_EDIT_TAB = "education_awards";
                            if (fpInner || isAMMorSUBind) createAwardsOnlyView(holder, model);
                            else createEducationsAwardsView(holder, model);
                        }
                        break;
                    case 2:
                        if (!fpInner && !isAMMorSUBind) {
                            Constants.PROFILE_EDIT_TAB = "education_awards";
                            if (fpInner || isAMMorSUBind) createAwardsOnlyView(holder, model);
                            else createEducationsAwardsView(holder, model);
                        }
                        break;
                    case 3:
                        if (!fpInner && !isAMMorSUBind) {
                            Constants.PROFILE_EDIT_TAB = "availability";
                            createAvailabilityView(holder, model);
                        }
                        break;
                }
                holder.scrollView.post(() ->
                        holder.ll_viewscreen.getViewTreeObserver()
                                .addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
                                    @Override
                                    public void onGlobalLayout() {
                                        holder.scrollView.smoothScrollTo(0, holder.ll_viewscreen.getTop());
                                        holder.ll_viewscreen.getViewTreeObserver()
                                                .removeOnGlobalLayoutListener(this);
                                    }
                                })
                );
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {
            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {
            }
        });

        // ── Subscription section ──────────────────────────────────────────
        if (!isGHTeamMember && subscription != null) {
            if (subscription.getModel().equalsIgnoreCase("free")
                    || subscription.getPlan().getName().equalsIgnoreCase("Free Trial")) {
                holder.ll_payment.setVisibility(GONE);
            } else {
                holder.ll_payment.setVisibility(VISIBLE);
            }
            holder.stv_subscription.setText(R.string.subscription);
            holder.stv_subscription.setTextColor(context.getColor(R.color.blue));
            holder.saccount_activated_on.setText(R.string.account_activated_on);
            holder.tvaccount_activated_on.setText(subscription.getActivatedOn());
            holder.splan_details.setText(R.string.plan_details);
            holder.tvplan_details.setText(subscription.getPlan().getName());
            holder.splan_validity.setText(R.string.plan_validity);
            holder.tvplan_validity.setText(subscription.getValidityDays());
            holder.ssubscription_start_date.setText(R.string.subscription_start_date);
            holder.tvsubscription_start_date.setText(subscription.getStartDate());
            holder.amount.setText(R.string.amount_paid);
            holder.amount.setVisibility(GONE);
            holder.termsDate.setText("T&C Accepted On");
            holder.tv_termsDate.setText(profile.getAccepted_t_c_date());
            if (subscription.getPlan() != null)
                holder.tv_amount.setText(String.valueOf(subscription.getPlan().getAmount()));
            else
                holder.tv_amount.setText("");
            holder.smode_of_payment.setText(R.string.mode_of_payment);
            String payMethod = subscription.getPayment() != null
                    ? subscription.getPayment().getMethod() : "-";
            holder.tvmode_of_payment.setText(payMethod);
            holder.snext_billing_date.setText(R.string.next_billing_date);
            holder.tvnext_billing_date.setText(
                    subscription.getEndDate() != null ? subscription.getEndDate() : "-");
        }

        // ── "Complete Now" click — precise field-level navigation ─────────
        final FirmProfileModel finalModel = model;
        if (holder.tvCompleteNow != null) {
            holder.tvCompleteNow.setOnClickListener(v -> {
                if (!Constants.is_active) {
                    AndroidUtils.showRenewalPopup(activity);
                    return;
                }
                navigateToExactField(finalModel);
            });
        }

        if (holder.ivClose != null)
            holder.ivClose.setOnClickListener(v -> updateProfileBanner());
    }

    // ─────────────────────────────────────────────────────────────────────────
    //  navigateToExactField — core "Complete Now" logic
    //
    //  Priority order:
    //    1. API next_step.missing_fields[] — use the FIRST recognisable field
    //    2. API next_step.redirect_tab    — fall back to tab-level navigation
    //    3. Local profile-completion check — last resort
    // ─────────────────────────────────────────────────────────────────────────
    private void navigateToExactField(FirmProfileModel model) {
        if (model == null || model.getData() == null || model.getData().getProfile() == null) {
            Constants.TARGET_FIELD = "";
            Constants.PROFILE_EDIT_TAB = "";
            eventListener.EditProfile(true, false);
            return;
        }

        FirmProfileModel.Profile profile = model.getData().getProfile();
        FirmProfileModel.ProfileCompletion pcx = isFirmProfile()
                ? profile.getFirm().getProfile_completion()
                : profile.getProfile_completion();
        FirmProfileModel.NextStep ns = pcx != null ? pcx.getNext_step() : null;

        if (ns != null) {
            Log.d("REDIRECT_TAB_DEBUG", "section=" + ns.getSection());
            Log.d("REDIRECT_TAB_DEBUG", "redirect_tab=" + ns.getRedirect_tab());
            Log.d("REDIRECT_TAB_DEBUG", "priority=" + ns.getPriority());
            Log.d("REDIRECT_TAB_DEBUG", "missing_fields=" + ns.getMissing_fields());
        }

        if (ns != null && ns.getMissing_fields() != null && !ns.getMissing_fields().isEmpty()) {
            for (String field : ns.getMissing_fields()) {
                FieldDestination dest = resolveFieldDestination(field);
                if (dest != null) {
                    Log.d("COMPLETE_NOW", "Missing field: " + field
                            + " section=" + dest.section
                            + " fieldId=" + dest.fieldId
                            + " tab=" + dest.tabIndex);
                    applyFieldDestination(dest, model);
                    return;
                }
            }
        }

        if (ns != null && !TextUtils.isEmpty(ns.getRedirect_tab())) {
            String dest = resolveDestinationFromTab(ns.getRedirect_tab());
            Log.d("COMPLETE_NOW", "redirect_tab=" + ns.getRedirect_tab() + " dest=" + dest);
            applyTabLevelDestination(dest, model);
            return;
        }

        handleFallbackNavigation(model);
    }


    /**
     * Applies a precise FieldDestination: sets Constants so BasicProfileEdit
     * can scroll to + highlight the exact view.
     */
    private void applyFieldDestination(FieldDestination dest, FirmProfileModel model) {
        Constants.TARGET_FIELD = dest.fieldId;

        switch (dest.section) {
            case "basic":
                Constants.PROFILE_EDIT_TAB = "";
                eventListener.EditProfile(true, false);
                break;
            case "pic":
                Constants.TARGET_FIELD = "";
                AndroidUtils.showAlert("Please upload a profile picture.", activity);
                break;
            case "additional":
                Constants.PROFILE_EDIT_TAB = "practice_details";
                eventListener.EditProfile(false, true);
                break;
            case "courts_cases":
                Constants.PROFILE_EDIT_TAB = "courts_cases";
                eventListener.EditProfile(false, true);
                break;
            case "education":
                Constants.PROFILE_EDIT_TAB = "education_awards";
                eventListener.EditProfile(false, true);
                break;
            case "availability":
                boolean isFP = isFirmProfile();
                boolean isAMM = Constants.ROLE.equalsIgnoreCase("AAM");
                if (!isFP && !isAMM) {
                    Constants.PROFILE_EDIT_TAB = "availability";
                    eventListener.EditProfile(false, true);
                } else {
                    Constants.PROFILE_EDIT_TAB = "practice_details";
                    eventListener.EditProfile(false, true);
                }
                break;
            default:
                Constants.TARGET_FIELD = "";
                Constants.PROFILE_EDIT_TAB = "";
                eventListener.EditProfile(true, false);
                break;
        }
    }


    /**
     * Tab-level fallback (no specific field known)
     */
    private void applyTabLevelDestination(String destination, FirmProfileModel model) {
        Constants.TARGET_FIELD = "";
        switch (destination) {
            case "basic":
                Constants.PROFILE_EDIT_TAB = "";
                eventListener.EditProfile(true, false);
                break;
            case "pic":
                AndroidUtils.showAlert("Please upload a profile picture.", activity);
                break;
            case "additional":
                Constants.PROFILE_EDIT_TAB = "practice_details";
                eventListener.EditProfile(false, true);
                break;
            case "courts_cases":
                Constants.PROFILE_EDIT_TAB = "courts_cases";
                eventListener.EditProfile(false, true);
                break;
            case "education":
                Constants.PROFILE_EDIT_TAB = "education_awards";
                eventListener.EditProfile(false, true);
                break;
            case "availability":
                boolean isFP = isFirmProfile();
                boolean isAMM = Constants.ROLE.equalsIgnoreCase("AAM");
                if (!isFP && !isAMM) {
                    Constants.PROFILE_EDIT_TAB = "availability";
                    eventListener.EditProfile(false, true);
                } else {
                    Constants.PROFILE_EDIT_TAB = "practice_details";
                    eventListener.EditProfile(false, true);
                }
                break;
            default:
                handleFallbackNavigation(model);
                break;
        }
    }


    private String resolveDestinationFromTab(String redirectTab) {
        switch (redirectTab.toLowerCase().trim()) {
            case "profile":
            case "basic_profile":
            case "firm_profile":
                return "basic";
            case "profile_pic":
            case "logo":
                return "pic";
            case "practice":
            case "practice_details":
            case "additional_info":
            case "additional":
            case "practice_optional":
                return "additional";
            case "courts":
            case "court_enrollments":
            case "courts_cases":
                return "courts_cases";
            case "education":
            case "education_awards":
            case "awards":
                return "education";
            case "availability":
                return "availability";
            default:
                return "additional";
        }
    }


    // ─────────────────────────────────────────────────────────────────────────
    //  Tiny view-toggle helper
    // ─────────────────────────────────────────────────────────────────────────
    private void toggleVisibility(TextView tv, boolean show, String text) {
        if (show) {
            tv.setVisibility(View.VISIBLE);
            tv.setText(text);
        } else {
            tv.setVisibility(View.GONE);
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    //  Awards-only view (Firm Profile OR AAM/SU — tab 2)
    // ─────────────────────────────────────────────────────────────────────────
    private View createAwardsOnlyView(MyViewHolder holder, FirmProfileModel firmProfileModel) {
        LayoutInflater inflater = LayoutInflater.from(holder.itemView.getContext());
        View detailView = inflater.inflate(R.layout.education_awards_layout, holder.ll_viewscreen, false);

        for (int id : new int[]{R.id.ll_education, R.id.ll_certification,
                R.id.tv_title_education, R.id.tv_title_certification}) {
            View v = detailView.findViewById(id);
            if (v != null) v.setVisibility(View.GONE);
        }

        LinearLayout llAwards = detailView.findViewById(R.id.ll_awards);
        TextView tvAwardsTitle = detailView.findViewById(R.id.tv_title_awards);
        tvAwardsTitle.setText("Awards & Recognition");
        tvAwardsTitle.setTextColor(context.getColor(R.color.grey_light));

        FirmProfileModel.Profile profile =
                (firmProfileModel != null && firmProfileModel.getData() != null)
                        ? firmProfileModel.getData().getProfile() : null;

        if (profile == null) {
            holder.ll_viewscreen.setVisibility(View.GONE);
            return detailView;
        }

        boolean fp = isFirmProfile();
        List<FirmProfileModel.Award> awards = fp
                ? (profile.getFirm() != null ? profile.getFirm().getAwards() : null)
                : profile.getAwards();
        populateAwards(inflater, llAwards, awards);

        holder.ll_viewscreen.removeAllViews();
        holder.ll_viewscreen.addView(detailView);
        holder.ll_viewscreen.setVisibility(View.VISIBLE);
        return detailView;
    }

    // ─────────────────────────────────────────────────────────────────────────
    //  Court row helper
    // ─────────────────────────────────────────────────────────────────────────
    private void addCourtRow(LayoutInflater inflater, LinearLayout parent,
                             FirmProfileModel.CourtEnrollment ce) {
        View row = inflater.inflate(R.layout.ll_court_layout, parent, false);
        row.findViewById(R.id.tv_placeholder).setVisibility(View.GONE);
        row.findViewById(R.id.ll_data_container).setVisibility(View.VISIBLE);
        TextView tv_courtType = row.findViewById(R.id.tv_courtType);
        TextView tv_courtState = row.findViewById(R.id.tv_courtState);
        TextView tv_courtCity = row.findViewById(R.id.tv_courtCity);
        TextView tv_courtName = row.findViewById(R.id.tv_courtName);
        String courtType1 = ce.getCourt_name()
                .replace("_", " ")
                .toLowerCase(Locale.getDefault());

        // Convert each word's first letter to uppercase
        if (!courtType1.isEmpty()) {
            String[] words = courtType1.split(" ");
            StringBuilder result = new StringBuilder();
            for (String word : words) {
                if (!word.isEmpty()) {
                    result.append(word.substring(0, 1).toUpperCase())
                            .append(word.substring(1))
                            .append(" ");
                }
            }
            courtType1 = result.toString().trim();
        }

        if (tv_courtType != null) tv_courtType.setText(courtType1);
        tv_courtState.setText(ce.getState());
        tv_courtCity.setText(ce.getCity());
        parent.addView(row);
    }

    private String loadSelectedItem(String courtType) {
        String CourtType = courtType;
        if (courtType.equals("district_court")) {
            CourtType = "District & Other Lower Courts";
        } else if (courtType.equals("high_court")) {
            CourtType = "High Court";
        } else {
            CourtType = "Supreme Court";
        }
        return CourtType;
    }

    // ─────────────────────────────────────────────────────────────────────────
    //  Practice Details tab
    // ─────────────────────────────────────────────────────────────────────────
    @SuppressLint("SetTextI18n")
    private void showPracticeDetails(MyViewHolder holder, int position) {
        holder.ll_viewscreen.removeAllViews();

        LayoutInflater inflater = LayoutInflater.from(holder.itemView.getContext());
        View detailView = inflater.inflate(R.layout.practicedetails_view, holder.ll_viewscreen, false);

        FirmProfileModel m = firmProfileModelArrayList.get(position);
        if (m == null || m.getData() == null
                || m.getData().getProfile() == null
                || m.getData().getProfile().getFirm() == null) {
            holder.ll_viewscreen.addView(detailView);
            return;
        }

        boolean fp = isFirmProfile();
        boolean isAMMorSU = Constants.ROLE.equalsIgnoreCase("AAM");

        FirmProfileModel.Firm firm = m.getData().getProfile().getFirm();
        FirmProfileModel.Profile profile = m.getData().getProfile();

        // ── Professional Information section header ───────────────────────
        TextView tvProfHeader = detailView.findViewById(R.id.tv_professional_info_header);
        if (tvProfHeader != null) {
            tvProfHeader.setText("Professional Information");
            tvProfHeader.setVisibility(View.VISIBLE);
        }

        // ── Bar Council ID vs Registration ID ────────────────────────────
        TextView firmNameLabel = detailView.findViewById(R.id.FirmName);
        firmNameLabel.setText(fp ? "Registration Id" : context.getString(R.string.bar_id));
        firmNameLabel.setTextColor(context.getColor(R.color.grey_light));
        String regOrBarId = fp
                ? (firm.getReg_id() != null ? firm.getReg_id() : "")
                : (profile.getBar_council_id() != null ? profile.getBar_council_id() : "");
        ((TextView) detailView.findViewById(R.id.tv_FirmName)).setText(regOrBarId);

        // ── Year of Experience vs Year of Incorporation ───────────────────
        TextView tvYearLabel = detailView.findViewById(R.id.Country);
        tvYearLabel.setText(fp ? "Year of Incorporation" : context.getString(R.string.year_exp));
        tvYearLabel.setTextColor(context.getColor(R.color.grey_light));
        String yearValue = fp
                ? String.valueOf(firm.getYears_of_incorporation())
                : String.valueOf(profile.getYears_of_experience());
        ((TextView) detailView.findViewById(R.id.tv_Country)).setText(yearValue);

        // ── Consultation Fee (My Profile only) ────────────────────────────
        TextView tvConFeeLabel = detailView.findViewById(R.id.con_fee);
        TextView tvConFeeValue = detailView.findViewById(R.id.tv_con_fee);
        View conFeeRow = detailView.findViewById(R.id.ll_confee_row);
        if (conFeeRow != null) conFeeRow.setVisibility(fp ? GONE : VISIBLE);
        if (tvConFeeLabel != null) {
            tvConFeeLabel.setVisibility(fp || isAMMorSU ? GONE : VISIBLE);
            if (!fp && !isAMMorSU) {
                tvConFeeLabel.setText(R.string.con_fees);
                tvConFeeLabel.setTextColor(context.getColor(R.color.grey_light));
            }
        }
        if (tvConFeeValue != null) {
            tvConFeeValue.setVisibility(fp || isAMMorSU ? GONE : VISIBLE);
            if (!fp && !isAMMorSU) {
                FirmProfileModel.ConsultationFee cf = profile.getConsultation_fee();
                String sym = (cf != null && cf.getSymbol() != null) ? cf.getSymbol() : "₹";
                String fee = (cf != null && cf.getAmount() != null) ? cf.getAmount() : "";
                tvConFeeValue.setText(sym + " " + fee);
            }
        }

        // ── Billing Currency ──────────────────────────────────────────────
        TextView tvCurrencyLabel = detailView.findViewById(R.id.billing_currency1);
        TextView tvCurrencyValue = detailView.findViewById(R.id.tv_billing_currency);
        if (tvCurrencyLabel != null) {
            tvCurrencyLabel.setText(R.string.billing_currency);
            tvCurrencyLabel.setTextColor(context.getColor(R.color.grey_light));
        }
        if (tvCurrencyValue != null) {
            tvCurrencyValue.setText(
                    firm.getBilling_currency() != null ? firm.getBilling_currency() : "");
        }

        // ── Practice Areas ────────────────────────────────────────────────
        TextView tvPracticeLabel = detailView.findViewById(R.id.Email);
        tvPracticeLabel.setText(R.string.practice_area);
        tvPracticeLabel.setTextColor(context.getColor(R.color.grey_light));

        RecyclerView rvPractice = detailView.findViewById(R.id.rvPracticeAreas);
        TextView tvNoPractice = detailView.findViewById(R.id.tvNoPractice);
        JSONArray practiceArray = fp ? firm.getPractice_areas() : profile.getPractice_areas();

        if (practiceArray == null || practiceArray.length() == 0) {
            rvPractice.setVisibility(View.GONE);
            tvNoPractice.setVisibility(View.VISIBLE);
        } else {
            tvNoPractice.setVisibility(View.GONE);
            rvPractice.setVisibility(View.VISIBLE);
            rvPractice.setLayoutManager(new GridLayoutManager(context, 2));
            rvPractice.setAdapter(new TagAdapter(practiceArray, TagAdapter.Style.PRACTICE));
        }

        // ── Services Offered ──────────────────────────────────────────────
        TextView tvServiceLabel = detailView.findViewById(R.id.ContactPhone);
        tvServiceLabel.setText(R.string.service_offered);
        tvServiceLabel.setTextColor(context.getColor(R.color.grey_light));

        RecyclerView rvServices = detailView.findViewById(R.id.rvServices);
        TextView tvNoService = detailView.findViewById(R.id.tvnoServices);
        JSONArray servicesArray = fp ? firm.getServices_offered() : profile.getServices_offered();

        if (servicesArray == null || servicesArray.length() == 0) {
            rvServices.setVisibility(View.GONE);
            tvNoService.setVisibility(View.VISIBLE);
        } else {
            tvNoService.setVisibility(View.GONE);
            rvServices.setVisibility(View.VISIBLE);
            rvServices.setLayoutManager(new GridLayoutManager(context, 2));
            rvServices.setAdapter(new TagAdapter(servicesArray, TagAdapter.Style.SERVICE));
        }

        // ── Languages Spoken — hidden for Firm Profile / AAM ─────────────
        TextView tvLangLabel = detailView.findViewById(R.id.ContactName);
        TextView tvLangValue = detailView.findViewById(R.id.tv_ContactName);
        RecyclerView rvLang = detailView.findViewById(R.id.rvLanguages);
        TextView tvNoLang = detailView.findViewById(R.id.tvnoLanguage);

        if (fp || isAMMorSU) {
            if (tvLangLabel != null) tvLangLabel.setVisibility(View.GONE);
            if (tvLangValue != null) tvLangValue.setVisibility(View.GONE);
            if (rvLang != null) rvLang.setVisibility(View.GONE);
            if (tvNoLang != null) tvNoLang.setVisibility(View.GONE);
        } else {
            if (tvLangLabel != null) {
                tvLangLabel.setText(R.string.language);
                tvLangLabel.setTextColor(context.getColor(R.color.grey_light));
            }
            if (tvLangValue != null) tvLangValue.setVisibility(View.GONE);
            JSONArray langArray = profile.getLanguages_spoken();
            if (langArray == null || langArray.length() == 0) {
                if (rvLang != null) rvLang.setVisibility(View.GONE);
                if (tvNoLang != null) tvNoLang.setVisibility(View.VISIBLE);
            } else {
                if (tvNoLang != null) tvNoLang.setVisibility(View.GONE);
                if (rvLang != null) {
                    rvLang.setVisibility(View.VISIBLE);
                    rvLang.setLayoutManager(new GridLayoutManager(context, 2));
                    rvLang.setAdapter(new TagAdapter(langArray, TagAdapter.Style.LANGUAGE));
                }
            }
        }

        // ── Cases Handled — always hidden here (moved to Courts & Cases tab) ─
        View tvCasesLabel = detailView.findViewById(R.id.cases_handled);
        RecyclerView rvCases = detailView.findViewById(R.id.rvCasesHandled);
        TextView tvNoCases = detailView.findViewById(R.id.tvnoCases);
        if (tvCasesLabel != null) tvCasesLabel.setVisibility(View.GONE);
        if (rvCases != null) rvCases.setVisibility(View.GONE);
        if (tvNoCases != null) tvNoCases.setVisibility(View.GONE);

        // ── Address Information section header ────────────────────────────
        TextView tvAddrHeader = detailView.findViewById(R.id.tv_address_info_header);
        if (tvAddrHeader != null) {
            tvAddrHeader.setText("Addresses");
            tvAddrHeader.setVisibility(View.VISIBLE);
        }

        // ── Registered Address ────────────────────────────────────────────
        TextView tvRegAddrLabel = detailView.findViewById(R.id.tv_registered_address_label);
        TextView tvRegAddrValue = detailView.findViewById(R.id.tv_registered_address_value);
        if (tvRegAddrLabel != null) {
            tvRegAddrLabel.setText(R.string.register_address);
            tvRegAddrLabel.setTextColor(context.getColor(R.color.grey_light));
        }
        if (tvRegAddrValue != null) {
            tvRegAddrValue.setText(getFullAddress(firm.getAddress()));
        }

        // ── Mailing Address ───────────────────────────────────────────────
        TextView tvMailAddrLabel = detailView.findViewById(R.id.tv_mailing_address_label);
        TextView tvMailAddrValue = detailView.findViewById(R.id.tv_mailing_address_value);
        if (tvMailAddrLabel != null) {
            tvMailAddrLabel.setText(R.string.mailing_address);
            tvMailAddrLabel.setTextColor(context.getColor(R.color.grey_light));
        }
        if (tvMailAddrValue != null) {
            tvMailAddrValue.setText(getFullAddress(firm.getCorrespondence_address()));
        }

        // ── Court section — always hidden in Practice Details tab ─────────
        View llCourtSection = detailView.findViewById(R.id.ll_court_section);
        if (llCourtSection != null) llCourtSection.setVisibility(View.GONE);

        holder.ll_viewscreen.addView(detailView);
        holder.ll_viewscreen.setVisibility(View.VISIBLE);
    }

    // ─────────────────────────────────────────────────────────────────────────
    //  Education + Certifications + Awards tab (My Profile only)
    // ─────────────────────────────────────────────────────────────────────────
    private View createEducationsAwardsView(MyViewHolder holder, FirmProfileModel firmProfileModel) {
        LayoutInflater inflater = LayoutInflater.from(holder.itemView.getContext());
        View detailView = inflater.inflate(R.layout.education_awards_layout, holder.ll_viewscreen, false);

        LinearLayout llEducation = detailView.findViewById(R.id.ll_education);
        LinearLayout llCertification = detailView.findViewById(R.id.ll_certification);
        LinearLayout llAwards = detailView.findViewById(R.id.ll_awards);
        TextView tvEdu = detailView.findViewById(R.id.tv_title_education);
        TextView tvCert = detailView.findViewById(R.id.tv_title_certification);
        TextView tvAw = detailView.findViewById(R.id.tv_title_awards);

        tvEdu.setText(R.string.educational_qualifications);
        tvCert.setText(R.string.certifications);
        tvAw.setText(R.string.awards_recoginations);
        tvEdu.setTextColor(context.getColor(R.color.grey_light));
        tvCert.setTextColor(context.getColor(R.color.grey_light));
        tvAw.setTextColor(context.getColor(R.color.grey_light));

        FirmProfileModel.Profile profile =
                (firmProfileModel != null && firmProfileModel.getData() != null)
                        ? firmProfileModel.getData().getProfile() : null;

        if (profile == null) {
            holder.ll_viewscreen.setVisibility(View.GONE);
            return detailView;
        }

        populateEducation(inflater, llEducation, profile.getEducation());
        populateCertification(inflater, llCertification, profile.getCertifications());
        populateAwards(inflater, llAwards, profile.getAwards());

        holder.ll_viewscreen.removeAllViews();
        holder.ll_viewscreen.addView(detailView);
        holder.ll_viewscreen.setVisibility(View.VISIBLE);
        return detailView;
    }

    // ─────────────────────────────────────────────────────────────────────────
    //  Availability tab (My Profile only)
    // ─────────────────────────────────────────────────────────────────────────
    private View createAvailabilityView(MyViewHolder holder, FirmProfileModel firmProfileModel) {
        LayoutInflater inflater = LayoutInflater.from(holder.itemView.getContext());
        View detailView = inflater.inflate(R.layout.book_set_availability, holder.ll_viewscreen, false);

        Button btnSlotDuration = detailView.findViewById(R.id.btn_slot_duration);
        TextView tvMinBookingHours = detailView.findViewById(R.id.tv_min_booking_hours);
        LinearLayout slotHours = detailView.findViewById(R.id.slot_hours);
        AppCompatImageView ivInfo = detailView.findViewById(R.id.iv_info);
        ImageView ivInfo1 = detailView.findViewById(R.id.iv_info1);
        TextView tvInfo = detailView.findViewById(R.id.tvInfo);

        slotHours.setVisibility(View.GONE);
        ivInfo.setVisibility(View.GONE);
        tvInfo.setText(R.string.availability_info_txt);

        ivInfo.setOnClickListener(v -> showPopup(v,
                "Appointments require a minimum advance booking for preparation.", Color.WHITE));
        ivInfo1.setOnClickListener(v -> showPopup(v,
                "How it works: Set your available hours. We will automatically create "
                        + "30-minute booking slots. Need a break? Click Exclude icon to block specific "
                        + "times (e.g., 12:00-01.00 PM).",
                context.getColor(R.color.blue)));

        FirmProfileModel.Availability availability = null;
        if (firmProfileModel != null && firmProfileModel.getData() != null
                && firmProfileModel.getData().getProfile() != null) {
            availability = firmProfileModel.getData().getProfile().getAvailability();
        }
        if (availability != null) {
            btnSlotDuration.setText(availability.getSlot_duration() + " Minutes");
            int advH = availability.getAdvance_booking_window_hours();
            tvMinBookingHours.setText(advH + (advH == 1 ? " Hour" : " Hours"));
            populateWeeklySchedule(availability, detailView);
        }

        holder.ll_viewscreen.addView(detailView);
        holder.ll_viewscreen.setVisibility(View.VISIBLE);
        return detailView;
    }

    // ─────────────────────────────────────────────────────────────────────────
    //  Courts & Cases tab (My Profile only — non-firm, non-AMM) - Now Tab 1
    // ─────────────────────────────────────────────────────────────────────────
    private View createCourtsAndCasesView(MyViewHolder holder, FirmProfileModel firmProfileModel) {
        LayoutInflater inflater = LayoutInflater.from(holder.itemView.getContext());

        holder.ll_viewscreen.removeAllViews();

        int dp = (int) context.getResources().getDisplayMetrics().density;

        LinearLayout container = new LinearLayout(context);
        container.setOrientation(LinearLayout.VERTICAL);
        container.setPadding(16 * dp, 16 * dp, 16 * dp, 16 * dp);
        container.setLayoutParams(new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT));

        FirmProfileModel.Profile profile =
                (firmProfileModel != null && firmProfileModel.getData() != null)
                        ? firmProfileModel.getData().getProfile() : null;

        // ── Court Enrollments section header ──────────────────────────────
        TextView tvCourtHeader = new TextView(context);
        tvCourtHeader.setText("Court Practice Details");
        tvCourtHeader.setTextColor(context.getColor(R.color.grey_light));
        tvCourtHeader.setTextSize(16);
        tvCourtHeader.setPadding(0, 0, 0, 8 * dp);
        container.addView(tvCourtHeader);

        // ── Practicing At label ───────────────────────────────────────────
        TextView tvPracticeAt = new TextView(context);
        tvPracticeAt.setText("Practicing At");
        tvPracticeAt.setTextColor(context.getColor(R.color.grey_light));
        tvPracticeAt.setTextSize(14);
        tvPracticeAt.setPadding(0, 8 * dp, 0, 8 * dp);
        container.addView(tvPracticeAt);

        // ── Court enrollment rows ─────────────────────────────────────────
        LinearLayout llCourts = new LinearLayout(context);
        llCourts.setOrientation(LinearLayout.VERTICAL);
        llCourts.setLayoutParams(new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT));
        container.addView(llCourts);

        List<FirmProfileModel.CourtEnrollment> courts =
                profile != null ? profile.getCourt_enrollments() : null;
        if (courts == null || courts.isEmpty()) {
            addPlaceholder(inflater, llCourts, "No court enrollments added");
        } else {
            for (FirmProfileModel.CourtEnrollment ce : courts) {
                addCourtRow(inflater, llCourts, ce);
            }
        }

        // ── Divider ───────────────────────────────────────────────────────
        View divider = new View(context);
        LinearLayout.LayoutParams divLp = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, 1 * dp);
        divLp.setMargins(0, 16 * dp, 0, 16 * dp);
        divider.setLayoutParams(divLp);
        divider.setBackgroundColor(0xFFCCCCCC);
        container.addView(divider);

        // ── Cases Handled section header ──────────────────────────────────
        TextView tvCasesHeader = new TextView(context);
        tvCasesHeader.setText("Types of Cases Handled");
        tvCasesHeader.setTextColor(context.getColor(R.color.grey_light));
        tvCasesHeader.setTextSize(16);
        tvCasesHeader.setPadding(0, 0, 0, 8 * dp);
        container.addView(tvCasesHeader);

        // ── Cases handled chips ───────────────────────────────────────────
        LinearLayout llCases = new LinearLayout(context);
        llCases.setOrientation(LinearLayout.VERTICAL);
        llCases.setLayoutParams(new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT));
        container.addView(llCases);

        List<String> casesList = profile != null ? profile.getCases_handled() : null;
        if (casesList == null || casesList.isEmpty()) {
            addPlaceholder(inflater, llCases, "No cases handled added");
        } else {
            RecyclerView rvCases = new RecyclerView(context);
            LinearLayout.LayoutParams rvLp = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT);
            rvCases.setLayoutParams(rvLp);
            rvCases.setNestedScrollingEnabled(false);
            rvCases.setLayoutManager(new GridLayoutManager(context, 2));
            JSONArray casesArray = new JSONArray();
            for (String c : casesList) casesArray.put(c);
            rvCases.setAdapter(new TagAdapter(casesArray, TagAdapter.Style.SERVICE));
            llCases.addView(rvCases);
        }

        holder.ll_viewscreen.addView(container);
        holder.ll_viewscreen.setVisibility(View.VISIBLE);
        return container;
    }

    private void showPopup(View anchor, String msg, int textColor) {
        TextView tv = new TextView(anchor.getContext());
        tv.setText(msg);
        tv.setPadding(30, 20, 30, 20);
        tv.setTextColor(textColor);
        tv.setBackgroundResource(R.drawable.info_box_bg);
        if (textColor != Color.WHITE)
            tv.setTypeface(ResourcesCompat.getFont(context, R.font.gill_sans));
        tv.setTextSize(13f);
        PopupWindow popup = new PopupWindow(tv,
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT, true);
        popup.setElevation(10f);
        popup.showAsDropDown(anchor, 0, 10);
    }

    // ─────────────────────────────────────────────────────────────────────────
    //  Populate helpers
    // ─────────────────────────────────────────────────────────────────────────
    private void populateEducation(LayoutInflater i, LinearLayout p,
                                   List<FirmProfileModel.Education> list) {
        p.removeAllViews();
        if (list == null || list.isEmpty()) {
            addPlaceholder(i, p, "No educational qualifications added");
            return;
        }
        for (FirmProfileModel.Education e : list)
            addListRow(i, p, e.getDegree(), e.getUniversity(), String.valueOf(e.getPassing_year()));
    }

    private void populateCertification(LayoutInflater i, LinearLayout p,
                                       List<FirmProfileModel.Certification> list) {
        p.removeAllViews();
        if (list == null || list.isEmpty()) {
            addPlaceholder(i, p, "No certifications added");
            return;
        }
        for (FirmProfileModel.Certification c : list)
            addListRow(i, p, c.getCertification_name(), c.getIssuing_authority(),
                    String.valueOf(c.getYear_of_issue()));
    }

    private void populateAwards(LayoutInflater i, LinearLayout p,
                                List<FirmProfileModel.Award> list) {
        p.removeAllViews();
        if (list == null || list.isEmpty()) {
            addPlaceholder(i, p, "No awards added");
            return;
        }
        for (FirmProfileModel.Award a : list)
            addListRow(i, p, a.getAward_name(), a.getPurpose(), String.valueOf(a.getYear_of_award()));
    }

    private void addPlaceholder(LayoutInflater inflater, LinearLayout parent, String msg) {
        View row = inflater.inflate(R.layout.ll_grey_list_item, parent, false);
        row.findViewById(R.id.ll_data_container).setVisibility(View.GONE);
        TextView tv = row.findViewById(R.id.tv_placeholder);
        tv.setText(msg);
        tv.setVisibility(View.VISIBLE);
        parent.addView(row);
    }

    private void addListRow(LayoutInflater inflater, LinearLayout parent,
                            String title, String content, String year) {
        View row = inflater.inflate(R.layout.ll_grey_list_item, parent, false);
        row.findViewById(R.id.tv_placeholder).setVisibility(View.GONE);
        row.findViewById(R.id.ll_data_container).setVisibility(View.VISIBLE);
        TextView tvTitle = row.findViewById(R.id.tv_title);
        TextView tvContent = row.findViewById(R.id.tv_content);
        TextView tvYear = row.findViewById(R.id.tv_year);
        TextView dot = row.findViewById(R.id.dot);
        tvTitle.setText(title);
        tvContent.setText(content);
        if (year == null || year.trim().isEmpty() || "0".equals(year)) {
            tvYear.setText("");
            dot.setVisibility(View.GONE);
        } else {
            tvYear.setText(year);
            dot.setVisibility(View.VISIBLE);
        }
        tvTitle.setTextColor(context.getColor(R.color.Primary_new));
        tvContent.setTextColor(context.getColor(R.color.pale_blue));
        tvYear.setTextColor(context.getColor(R.color.pale_blue));
        parent.addView(row);
    }

    // ─────────────────────────────────────────────────────────────────────────
    //  Weekly schedule
    // ─────────────────────────────────────────────────────────────────────────
    private void populateWeeklySchedule(FirmProfileModel.Availability availability, View root) {
        if (availability == null || availability.getWeekly_schedule() == null) return;
        LinearLayout ll = root.findViewById(R.id.ll_weekly_hours);
        ll.removeAllViews();
        LayoutInflater inflater = LayoutInflater.from(root.getContext());
        Typeface font = ResourcesCompat.getFont(root.getContext(), R.font.gill_sans);

        for (FirmProfileModel.WeeklySchedule day : availability.getWeekly_schedule()) {
            List<FirmProfileModel.WorkSlot> work = day.getWork_slots();
            List<FirmProfileModel.WorkSlot> blocked = day.getExpert_slots();
            View dv = inflater.inflate(R.layout.component_day_time, ll, false);

            TextView tvDay = dv.findViewById(R.id.tv_day);
            TextView tvWork = dv.findViewById(R.id.tv_work_time);
            TextView tvAvail = dv.findViewById(R.id.tv_available_time);
            LinearLayout llBlk = dv.findViewById(R.id.ll_blocked_row);
            GridLayout grid = dv.findViewById(R.id.ll_blockedtime);
            View divider = dv.findViewById(R.id.view);

            tvAvail.setVisibility(View.GONE);
            llBlk.setVisibility(View.GONE);
            divider.setVisibility(View.GONE);
            grid.removeAllViews();

            String lbl = day.getDate_label();
            tvDay.setText(!TextUtils.isEmpty(lbl) ? lbl : day.getDay_name());

            if (work != null && !work.isEmpty()) {
                FirmProfileModel.WorkSlot ws = work.get(0);
                tvWork.setText(convertTo12Hour(ws.getStart_time()) + " - "
                        + convertTo12Hour(ws.getEnd_time()));
            } else {
                tvWork.setText("Not available");
                tvWork.setAlpha(0.5f);
            }

            if (blocked != null && !blocked.isEmpty()) {
                llBlk.setVisibility(View.VISIBLE);
                divider.setVisibility(View.VISIBLE);
                for (FirmProfileModel.WorkSlot b : blocked) {
                    View chip = inflater.inflate(R.layout.grey_bg_textview, grid, false);
                    TextView tv = chip.findViewById(R.id.tv_start_time);
                    tv.setText(convertTo12Hour(b.getStart_time()) + " – "
                            + convertTo12Hour(b.getEnd_time()));
                    tv.setTypeface(font);
                    tv.setAlpha(0.5f);
                    GridLayout.LayoutParams p = new GridLayout.LayoutParams();
                    p.width = 0;
                    p.columnSpec = GridLayout.spec(GridLayout.UNDEFINED, 1f);
                    p.setMargins(12, 12, 12, 12);
                    chip.setLayoutParams(p);
                    grid.addView(chip);
                }
            }

            double avail = calculateAvailableHours(work, blocked);
            if (avail > 0) {
                tvAvail.setVisibility(View.VISIBLE);
                tvAvail.setText(formatHours(avail));
            }
            ll.addView(dv);
        }
    }

    private double calculateAvailableHours(List<FirmProfileModel.WorkSlot> work,
                                           List<FirmProfileModel.WorkSlot> blocked) {
        if (work == null || work.isEmpty()) return 0;
        double wm = 0;
        for (FirmProfileModel.WorkSlot w : work) wm += diffMin(w.getStart_time(), w.getEnd_time());
        double bm = 0;
        if (blocked != null)
            for (FirmProfileModel.WorkSlot b : blocked)
                bm += diffMin(b.getStart_time(), b.getEnd_time());
        return Math.max(0, (wm - bm) / 60);
    }

    private int diffMin(String s, String e) {
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("HH:mm", Locale.getDefault());
            return (int) ((sdf.parse(e).getTime() - sdf.parse(s).getTime()) / 60000);
        } catch (Exception ex) {
            return 0;
        }
    }

    private String formatHours(double h) {
        int hh = (int) h, mm = (int) ((h - hh) * 60);
        return mm == 0 ? hh + " hours Available" : hh + "h " + mm + "m Available";
    }

    private String convertTo12Hour(String t) {
        try {
            return new SimpleDateFormat("hh:mm a", Locale.getDefault())
                    .format(new SimpleDateFormat("HH:mm").parse(t));
        } catch (ParseException e) {
            return t;
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    //  Address helper
    // ─────────────────────────────────────────────────────────────────────────
    private String getFullAddress(FirmProfileModel.Address a) {
        if (a == null) return "-";
        StringBuilder sb = new StringBuilder();
        appendWithComma(sb, a.getHouse_flat_no());
        appendWithComma(sb, a.getStreet());
        appendWithComma(sb, a.getCity_town());
        appendWithComma(sb, a.getState());
        appendWithComma(sb, a.getCountry());
        appendWithComma(sb, a.getZipcode());
        return sb.length() > 0 ? sb.toString() : "-";
    }

    private void appendWithComma(StringBuilder sb, String v) {
        if (!TextUtils.isEmpty(v)) {
            if (sb.length() > 0) sb.append(", ");
            sb.append(v);
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    //  Camera / Gallery helpers
    // ─────────────────────────────────────────────────────────────────────────
    private void openGallery_new(ImageView iv, TextView icon, ImageView del) {
        if (galleryLauncher == null) return;
        currentIvProfile = iv;
        currentPersonIcon = icon;
        currentIvDeleteCircle = del;
        try {
            Intent intent = new Intent(Intent.ACTION_PICK,
                    MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            intent.setType("image/*");
            galleryLauncher.launch(intent);
        } catch (Exception e) {
            Toast.makeText(context, "Gallery error: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    void openCamera_new(ImageView iv, TextView icon, ImageView del) {
        if (cameraLauncher == null) return;
        currentIvProfile = iv;
        currentPersonIcon = icon;
        currentIvDeleteCircle = del;
        try {
            ContentValues cv = new ContentValues();
            cv.put(MediaStore.Images.Media.TITLE, "FirmProfile_" + System.currentTimeMillis());
            cv.put(MediaStore.Images.Media.DESCRIPTION, "Profile Image");
            cameraImageUri = activity.getContentResolver()
                    .insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, cv);
            if (cameraImageUri == null) {
                Toast.makeText(context, "Cannot access camera storage", Toast.LENGTH_SHORT).show();
                return;
            }
            cameraLauncher.launch(cameraImageUri);
        } catch (Exception e) {
            Toast.makeText(context, "Camera error: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    public void openCropEditor(Uri rawImageUri) {
        if (rawImageUri == null || practicePartnerView == null) return;
        practicePartnerView.openCropEditorFragment(rawImageUri);
    }

    public void handleCroppedResult(String croppedFilePath) {
        if (croppedFilePath == null) return;
        if (currentIvProfile == null || currentPersonIcon == null) return;

        File croppedFile = new File(croppedFilePath);
        if (!croppedFile.exists()) {
            Toast.makeText(context, "Cropped file not found", Toast.LENGTH_SHORT).show();
            return;
        }

        Uri croppedUri = Uri.fromFile(croppedFile);
        currentIvProfile.setVisibility(View.VISIBLE);
        currentPersonIcon.setVisibility(View.GONE);
        Glide.with(activity)
                .load(croppedUri)
                .circleCrop()
                .diskCacheStrategy(DiskCacheStrategy.NONE)
                .skipMemoryCache(true)
                .placeholder(R.drawable.ic_profile_placeholder)
                .error(R.drawable.ic_profile_placeholder)
                .into(currentIvProfile);
        animateFadeIn(currentIvProfile);
        profile_upload(croppedUri, currentIvProfile, currentPersonIcon, currentIvDeleteCircle);
    }

    void handleFileUri(Uri uri, ImageView iv, TextView icon, ImageView del) {
        setProfileImage(uri, iv);
        profile_upload(uri, iv, icon, del);
    }

    private void setProfileImage(Uri uri, ImageView iv) {
        Glide.with(activity)
                .load(uri)
                .circleCrop()
                .diskCacheStrategy(DiskCacheStrategy.NONE)
                .skipMemoryCache(true)
                .placeholder(R.drawable.ic_profile_placeholder)
                .error(R.drawable.ic_profile_placeholder)
                .into(iv);
        animateFadeIn(iv);
    }

    // ─────────────────────────────────────────────────────────────────────────
    //  Animations
    // ─────────────────────────────────────────────────────────────────────────
    private void animateFadeIn(View v) {
        v.setAlpha(0f);
        v.animate().alpha(1f).setDuration(400)
                .setInterpolator(new AccelerateDecelerateInterpolator()).start();
    }

    private void animateScalePulse(View v) {
        ObjectAnimator x = ObjectAnimator.ofFloat(v, "scaleX", 1f, 1.1f, 1f);
        ObjectAnimator y = ObjectAnimator.ofFloat(v, "scaleY", 1f, 1.1f, 1f);
        x.setDuration(350);
        y.setDuration(350);
        x.setInterpolator(new AccelerateDecelerateInterpolator());
        y.setInterpolator(new AccelerateDecelerateInterpolator());
        x.start();
        y.start();
    }

    private void animateShake(View v) {
        ObjectAnimator.ofFloat(v, "translationX",
                        0f, -20f, 20f, -15f, 15f, -10f, 10f, 0f)
                .setDuration(500)
                .start();
    }

    // ─────────────────────────────────────────────────────────────────────────
    //  Image compression
    // ─────────────────────────────────────────────────────────────────────────
    private File getFileFromUri(Uri uri) throws Exception {
        if (uri == null) throw new Exception("URI is null");
        InputStream is = activity.getContentResolver().openInputStream(uri);
        if (is == null) throw new Exception("Cannot open InputStream");
        File f = new File(activity.getCacheDir(),
                "profile_raw_" + System.currentTimeMillis() + ".jpg");
        OutputStream os = new FileOutputStream(f);
        byte[] buf = new byte[4096];
        int r;
        long total = 0;
        while ((r = is.read(buf)) != -1) {
            os.write(buf, 0, r);
            total += r;
        }
        os.flush();
        os.close();
        is.close();
        if (total == 0) throw new Exception("File is empty");
        return f;
    }

    private Bitmap fixExifRotation(Bitmap bm, String path) {
        try {
            ExifInterface exif = new ExifInterface(path);
            int orient = exif.getAttributeInt(ExifInterface.TAG_ORIENTATION,
                    ExifInterface.ORIENTATION_NORMAL);
            int degrees = 0;
            if (orient == ExifInterface.ORIENTATION_ROTATE_90) degrees = 90;
            if (orient == ExifInterface.ORIENTATION_ROTATE_180) degrees = 180;
            if (orient == ExifInterface.ORIENTATION_ROTATE_270) degrees = 270;
            if (degrees != 0) {
                Matrix m = new Matrix();
                m.postRotate(degrees);
                Bitmap rot = Bitmap.createBitmap(bm, 0, 0, bm.getWidth(), bm.getHeight(), m, true);
                bm.recycle();
                return rot;
            }
        } catch (Exception ignored) {
        }
        return bm;
    }

    private File compressImageTo2MB(Uri imageUri) throws Exception {
        File raw = getFileFromUri(imageUri);
        BitmapFactory.Options opts = new BitmapFactory.Options();
        opts.inPreferredConfig = Bitmap.Config.ARGB_8888;
        Bitmap bm = BitmapFactory.decodeFile(raw.getAbsolutePath(), opts);
        if (bm == null) throw new Exception("Cannot decode image");
        bm = fixExifRotation(bm, raw.getAbsolutePath());

        int max = 1920, w = bm.getWidth(), h = bm.getHeight();
        if (w > max || h > max) {
            float sc = w > h ? (float) max / w : (float) max / h;
            Bitmap sc2 = Bitmap.createScaledBitmap(bm,
                    Math.round(w * sc), Math.round(h * sc), true);
            bm.recycle();
            bm = sc2;
        }

        int quality = INITIAL_QUALITY;
        byte[] bytes;
        do {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            bm.compress(Bitmap.CompressFormat.JPEG, quality, baos);
            bytes = baos.toByteArray();
            if (bytes.length <= MAX_FILE_SIZE_BYTES) break;
            quality -= QUALITY_STEP;
            if (quality < MIN_QUALITY) {
                int hw = bm.getWidth() / 2, hh = bm.getHeight() / 2;
                if (hw < 100 || hh < 100) break;
                Bitmap halved = Bitmap.createScaledBitmap(bm, hw, hh, true);
                bm.recycle();
                bm = halved;
                quality = INITIAL_QUALITY;
            }
        } while (true);
        bm.recycle();

        File out = new File(activity.getCacheDir(),
                "profile_compressed_" + System.currentTimeMillis() + ".jpg");
        FileOutputStream fos = new FileOutputStream(out);
        fos.write(bytes);
        fos.flush();
        fos.close();
        if (raw.exists()) raw.delete();
        return out;
    }

    // ─────────────────────────────────────────────────────────────────────────
    //  Upload
    // ─────────────────────────────────────────────────────────────────────────
    private void profile_upload(Uri imageUri, ImageView iv, TextView icon, ImageView del) {
        progress_dialog = AndroidUtils.get_progress(activity);
        iv.setVisibility(View.VISIBLE);
        icon.setVisibility(View.GONE);
        Glide.with(activity)
                .load(imageUri)
                .circleCrop()
                .diskCacheStrategy(DiskCacheStrategy.NONE)
                .skipMemoryCache(true)
                .into(iv);
        animateFadeIn(iv);

        boolean fp = isFirmProfile();
        String URL = fp ? "v3/firm/profile/pic/upload" : "v3/profile/pic/upload";

        new Thread(() -> {
            try {
                File compressed = compressImageTo2MB(imageUri);
                JSONObject json = new JSONObject();
                json.put("type", fp ? "firm_logo" : "profile_pic");
                WebServiceHelper.callHttpUploadWebService(
                        this,
                        context,
                        WebServiceHelper.RestMethodType.POST,
                        URL,
                        "Profile_upload",
                        compressed,
                        json.toString());
            } catch (Exception e) {
                Log.e("UPLOAD_DEBUG", "Upload failed", e);
                activity.runOnUiThread(() -> {
                    animateShake(iv);
                    Toast.makeText(context,
                            "Image error: " + e.getMessage(), Toast.LENGTH_LONG).show();
                });
            }
        }).start();
    }

    private void profile() {
        progress_dialog = AndroidUtils.get_progress(activity);
        boolean fp = isFirmProfile();
        String URL = fp ? "v3/firm/profile/pic" : "v3/profile/pic";
        WebServiceHelper.callHttpWebService(this, context,
                WebServiceHelper.RestMethodType.GET,
                URL, "Profile", new JSONObject().toString());
    }

    private void delete() {
        progress_dialog = AndroidUtils.get_progress(activity);
        boolean fp = isFirmProfile();
        String URL = fp ? "v3/firm/profile/pic" : "v3/profile/pic";
        try {
            WebServiceHelper.callHttpWebService(this, context,
                    WebServiceHelper.RestMethodType.DELETE,
                    URL, "Profile_delete", new JSONObject().toString());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void DeleteAccount() {
        progress_dialog = AndroidUtils.get_progress(activity);
        WebServiceHelper.callHttpWebService(this, context,
                WebServiceHelper.RestMethodType.POST,
                "v3/profile/delete-account", "Delete Account",
                new JSONObject().toString());
    }

    @Override
    public void onClick(View view) {
    }

    // ─────────────────────────────────────────────────────────────────────────
    //  API callback
    // ─────────────────────────────────────────────────────────────────────────
    @Override
    public void onAsyncTaskComplete(HttpResultDo httpResult) {
        if (progress_dialog != null && progress_dialog.isShowing())
            AndroidUtils.dismiss_dialog(progress_dialog);
        if (currentIvProfile == null || currentPersonIcon == null) return;
        if (httpResult.getResult() != WebServiceHelper.ServiceCallStatus.Success) return;

        try {
            JSONObject result = new JSONObject(httpResult.getResponseContent());
            String type = httpResult.getRequestType();
            Log.d("Request_Type", type);

            boolean isImageFetch = "Profile".equals(type) || "Firm_Logo".equals(type);
            boolean isImageUpload = "Profile_upload".equals(type) || "Firm_Logo_Upload".equals(type);
            boolean isImageDelete = "Profile_delete".equals(type) || "Firm_Logo_Delete".equals(type);

            if (isImageFetch) {
                boolean err = result.optBoolean("error");
                if (!err) {
                    JSONObject d = result.optJSONObject("data");
                    if (d != null) {
                        String url = d.optString("imageUrl");
                        if (!TextUtils.isEmpty(url)) {
                            AppImageCache.invalidate(context, Constants.firm_image);
                            if (!isFirmProfile()) Constants.firm_image = url;
                            AppImageCache.preload(context, url);
                            AndroidUtils.loadProfileImage(context, url,
                                    currentIvProfile, currentPersonIcon);
                            animateScalePulse(currentIvProfile);
                            if (Constants.isMyProfileClicked) {
                                currentPersonIcon.setText(!TextUtils.isEmpty(Constants.NAME)
                                        ? Constants.NAME.substring(0, 1).toUpperCase() : "?");
                            } else {
                                currentPersonIcon.setText(!TextUtils.isEmpty(Constants.FIRM_NAME)
                                        ? Constants.FIRM_NAME.substring(0, 1).toUpperCase() : "?");
                            }
                            if (currentIvDeleteCircle != null)
                                currentIvDeleteCircle.setVisibility(VISIBLE);
                        } else {
                            if (currentIvDeleteCircle != null)
                                currentIvDeleteCircle.setVisibility(GONE);
                        }
                    }
                }
                if (Constants.ROLE.equals("AAM")) {
                    Constants.mainActivity.profile();
                } else {
                    if (!isFirmProfile()) Constants.mainActivity.profile();
                }

            } else if (isImageUpload) {
                boolean err = result.optBoolean("error");
                if (!err) {
                    if (currentIvDeleteCircle != null)
                        currentIvDeleteCircle.setVisibility(VISIBLE);
                    profile();
                    AndroidUtils.showAlert(result.optString("msg", "Success"), activity);
                } else {
                    AndroidUtils.showAlert(result.optString("msg"), activity);
                    animateShake(currentIvProfile);
                }
                practicePartnerView.callViewBasicProfile();

            } else if (Objects.equals(httpResult.getRequestType(), "Update_Bp")) {
                boolean iserror = result.optBoolean("error");
                if (!iserror) {
                    // ── Parse the success message ─────────────────────────
                    JSONObject data = result.optJSONObject("data");
                    if (data != null) {
                        String msg = (data != null)
                                ? data.optString("msg", "")
                                : result.optString("msg", "");
                        AndroidUtils.showAlert_docs("Success !", msg, activity);
                        profileCompletionBanner.setVisibility(View.GONE);
                    } else {
                        // ── API returned an error ─────────────────────────────
                        if (result.has("errors")) {
                            JSONArray errors = result.optJSONArray("errors");
                            if (errors != null && errors.length() > 0) {
                                JSONObject err = errors.optJSONObject(0);
                                String field = err != null ? err.optString("field", "") : "";
                                String errMsg = err != null ? err.optString("msg", "") : "";
                                AndroidUtils.showAlert(field + " : " + errMsg, activity);
                            }
                        } else {
                            AndroidUtils.showAlert(result.optString("msg", ""), activity);
                        }
                    }
                }
            } else if (isImageDelete) {
                boolean err = result.optBoolean("error");
                Constants.mainActivity.profile();
                if (!err) {
                    Constants.firm_image = "";
                    if (currentIvDeleteCircle != null)
                        currentIvDeleteCircle.setVisibility(GONE);
                    currentIvProfile.setImageResource(R.drawable.ic_profile_placeholder);
                    currentIvProfile.setVisibility(View.GONE);
                    currentIvProfile.setClickable(false);
                    currentPersonIcon.setVisibility(VISIBLE);
                    if (Constants.isMyProfileClicked) {
                        currentPersonIcon.setText(!TextUtils.isEmpty(Constants.NAME)
                                ? Constants.NAME.substring(0, 1).toUpperCase() : "?");
                    } else {
                        currentPersonIcon.setText(!TextUtils.isEmpty(Constants.FIRM_NAME)
                                ? Constants.FIRM_NAME.substring(0, 1).toUpperCase() : "?");
                    }
                    AndroidUtils.showAlert(result.optString("msg", "Success"), activity);
                } else {
                    AndroidUtils.showAlert(result.optString("msg"), activity);
                }
                practicePartnerView.callViewBasicProfile();

            } else if ("Delete Account".equals(type)) {
                AndroidUtils.showAlert(result.optString("msg",
                                "Account deletion request submitted. Will be completed in 2 weeks."),
                        activity, "Success");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    //  Profile completion helpers
    // ─────────────────────────────────────────────────────────────────────────
    private ProfileCompletionStatus checkProfileCompletionStatus(FirmProfileModel m) {
        if (m == null || m.getData() == null || m.getData().getProfile() == null)
            return ProfileCompletionStatus.BASIC_PROFILE_INCOMPLETE;

        FirmProfileModel.Profile p = m.getData().getProfile();
        FirmProfileModel.Firm f = p.getFirm();
        boolean basic = true;

        if (f == null) {
            basic = false;
        } else {
            if ("solo".equalsIgnoreCase(Constants.CATEGORY)) {
                if (TextUtils.isEmpty(f.getContact_person())) basic = false;
            } else {
                if (TextUtils.isEmpty(f.getFullname())) basic = false;
            }
            if (TextUtils.isEmpty(f.getEmail())) basic = false;
            if (TextUtils.isEmpty(f.getContact_phone())
                    || f.getContact_phone().length() < 10) basic = false;
            if (TextUtils.isEmpty(f.getBilling_currency())) basic = false;
            if (TextUtils.isEmpty(f.getWebsite())) basic = false;
        }
        if ("solo".equalsIgnoreCase(Constants.CATEGORY)) {
            if (TextUtils.isEmpty(p.getGender())) basic = false;
            if (TextUtils.isEmpty(p.getDate_of_birth())) basic = false;
        }
        FirmProfileModel.ConsultationFee cf = p.getConsultation_fee();
        if (cf == null || TextUtils.isEmpty(cf.getAmount())) basic = false;
        if (f != null && f.getAddress() != null) {
            FirmProfileModel.Address a = f.getAddress();
            if (TextUtils.isEmpty(a.getHouse_flat_no()) || TextUtils.isEmpty(a.getCity_town())
                    || TextUtils.isEmpty(a.getState()) || TextUtils.isEmpty(a.getCountry())
                    || TextUtils.isEmpty(a.getZipcode()) || a.getZipcode().length() < 5)
                basic = false;
        } else basic = false;
        if (f != null && f.getCorrespondence_address() != null) {
            FirmProfileModel.Address a = f.getCorrespondence_address();
            if (TextUtils.isEmpty(a.getHouse_flat_no()) || TextUtils.isEmpty(a.getCity_town())
                    || TextUtils.isEmpty(a.getState()) || TextUtils.isEmpty(a.getCountry())
                    || TextUtils.isEmpty(a.getZipcode()) || a.getZipcode().length() < 5)
                basic = false;
        } else basic = false;
        if (!basic) return ProfileCompletionStatus.BASIC_PROFILE_INCOMPLETE;

        boolean additional = true;
        if (TextUtils.isEmpty(p.getBar_council_id())) additional = false;
        Integer yoe = p.getYears_of_experience();
        if (yoe == null || yoe <= 0) additional = false;
        if (p.getPractice_areas() == null || p.getPractice_areas().length() == 0)
            additional = false;
        if (p.getLanguages_spoken() == null || p.getLanguages_spoken().length() == 0)
            additional = false;
        if (p.getServices_offered() == null || p.getServices_offered().length() == 0)
            additional = false;
        if (p.getEducation() == null || p.getEducation().isEmpty()) additional = false;
        if (p.getAvailability() == null || p.getAvailability().getWeekly_schedule() == null
                || p.getAvailability().getWeekly_schedule().isEmpty()) {
            additional = false;
        } else {
            boolean hasWork = false;
            for (FirmProfileModel.WeeklySchedule s : p.getAvailability().getWeekly_schedule())
                if (s.isIs_working_day()) {
                    hasWork = true;
                    break;
                }
            if (!hasWork) additional = false;
        }
        if (!additional) return ProfileCompletionStatus.ADDITIONAL_INFO_INCOMPLETE;
        return ProfileCompletionStatus.COMPLETE;
    }

    private void updateCompletionBanner(MyViewHolder holder, FirmProfileModel model) {
        boolean showBanner = false;
        try {
            if (model != null && model.getData() != null && model.getData().getProfile() != null) {
                FirmProfileModel.Profile profile = model.getData().getProfile();
                if (isFirmProfile()) {
                    showBanner = profile.getFirm() != null && profile.getFirm().getShow_profile_banner();
                } else {
                    showBanner = profile.getShow_profile_banner();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        if (showBanner) {
            holder.tvCompleteNow.setText(R.string.complete_now);
            profileCompletionBanner.setVisibility(View.VISIBLE);
        } else {
            profileCompletionBanner.setVisibility(View.GONE);
        }
    }

    private void updateProfileBanner() {
        try {
            JSONObject request = new JSONObject();
            JSONObject payload = new JSONObject();

            boolean fp = isFirmProfile();
            Log.d("Update_Bp", payload.toString());
            if (fp) {
                payload.put("profile_completion_banner_dismissed", true);
                request.put("firm", payload);
            } else {
                request.put("profile_completion_banner_dismissed", true);
            }

            WebServiceHelper.callHttpWebService(
                    this,
                    context,
                    WebServiceHelper.RestMethodType.PATCH,
                    "v3/profile",
                    "Update_Bp",
                    request.toString()
            );

        } catch (Exception e) {
            e.fillInStackTrace();
        }
    }
//    private void updateCompletionBanner(MyViewHolder holder, FirmProfileModel model) {
//        int pct = 0;
//        try {
//            if (model != null && model.getData() != null && model.getData().getProfile() != null) {
//                FirmProfileModel.Profile profile = model.getData().getProfile();
//                FirmProfileModel.ProfileCompletion completion = isFirmProfile()
//                        ? (profile.getFirm() != null ? profile.getFirm().getProfile_completion() : null)
//                        : profile.getProfile_completion();
//                pct = completion != null ? completion.getCompletion_percentage() : 0;
//            }
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//
//        if (pct >= 100) {
//            holder.profileCompletionBanner.setVisibility(View.GONE);
//        } else {
//            holder.tvCompleteNow.setText(R.string.complete_now);
//            holder.profileCompletionBanner.setVisibility(View.VISIBLE);
//        }
//    }

    private void handleFallbackNavigation(FirmProfileModel model) {
        Constants.TARGET_FIELD = "";
        int pct = 0;
        try {
            if (model != null && model.getData() != null && model.getData().getProfile() != null) {
                FirmProfileModel.Profile profile = model.getData().getProfile();
                FirmProfileModel.ProfileCompletion completion = isFirmProfile()
                        ? (profile.getFirm() != null ? profile.getFirm().getProfile_completion() : null)
                        : profile.getProfile_completion();
                pct = completion != null ? completion.getCompletion_percentage() : 0;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        if (pct >= 100) {
            if (recyclerView != null) {
                MyViewHolder vh = (MyViewHolder)
                        recyclerView.findViewHolderForAdapterPosition(0);
                if (vh != null && profileCompletionBanner != null) {
                    profileCompletionBanner.setVisibility(View.GONE);
                }
            }
            AndroidUtils.showAlert("Profile is already complete!", activity);
        } else {
            switch (checkProfileCompletionStatus(model)) {
                case BASIC_PROFILE_INCOMPLETE:
                    Constants.PROFILE_EDIT_TAB = "";
                    eventListener.EditProfile(true, false);
                    break;
                case ADDITIONAL_INFO_INCOMPLETE:
                    Constants.PROFILE_EDIT_TAB = "practice_details";
                    eventListener.EditProfile(false, true);
                    break;
                case COMPLETE:
                    Constants.PROFILE_EDIT_TAB = "practice_details";
                    eventListener.EditProfile(false, true);
                    break;
            }
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    //  Mode A — dispatch action
    // ─────────────────────────────────────────────────────────────────────────
    private void dispatchAction(String action, MemberProfileModel model) {
        switch (action) {
            case "Edit":
                if (!Constants.is_active) AndroidUtils.showRenewalPopup(activity);
                else eventListener.EditPracticePartners(model);
                break;
            case "Delete":
                eventListener.DeletePracticePartners(model);
                break;
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    //  Interface
    // ─────────────────────────────────────────────────────────────────────────
    public interface InterfaceListener {
        void EditPracticePartners(MemberProfileModel model);

        void EditProfile(Boolean isProfileEdit, Boolean isAdditionalInfo);

        void DeletePracticePartners(MemberProfileModel model);
    }

    // ═════════════════════════════════════════════════════════════════════════
    //  ViewHolder
    // ═════════════════════════════════════════════════════════════════════════
    public class MyViewHolder extends RecyclerView.ViewHolder {

        // ── Mode A fields ─────────────────────────────────────────────────
        TextView tv_name, tv_designation, tv_designation_name, person_icon,
                tv_specialist, tv_specialist_name, tv_email, tv_phonenumber;
        ImageView iv_action_menu;
        CardView action_list_card;
        ListView action_list;

        // ── Mode B (Profile card) fields ──────────────────────────────────
        ImageView iv_profile, iv_edit, iv_edit_circle, iv_delete_circle,
                ivClose, iv_edit_addinfo;
        ScrollView scrollView;
        TabLayout tabLayout;
        LinearLayout ll_basicprofile, ll_subcription, ll_viewscreen,
                ll_additional_inform, ll_payment, ll_delete_account;

        TextView tvMessage, tv_BasicProfile, FirmName, tv_FirmName,
                Country, tv_Country, Email, tv_Email, ContactName, tv_ContactName,
                ContactPhone, tv_ContactPhone, Website, tv_Website, dob, tv_dob,
                gender, tv_gender, nationality, tv_nationality, bio, tv_bio, tvPercentage, tvCompleteNow, tv_add_info,
                email, phone;

        TextView tv_Address, Building, tv_Building, Street, tv_Street,
                City, tv_City, State, tv_State, Ad_Country, tv_Ad_Country,
                Zip, tv_Zip, tv_amount, amount, termsDate, tv_termsDate,
                tv_delete_account, account_del;

        TextView mtv_Address, mBuilding, mtv_Building, mStreet, mtv_Street,
                mCity, mtv_City, mState, mtv_State,
                mAd_Country, mtv_Ad_Country, mZip, mtv_Zip,
                stv_subscription, tv_personal_info, tv_sub,
                saccount_activated_on, tvaccount_activated_on,
                ssubscription_start_date, tvsubscription_start_date,
                splan_details, tvplan_details, smode_of_payment, tvmode_of_payment,
                splan_validity, tvplan_validity, snext_billing_date, tvnext_billing_date;

        FirmProfile firmProfile;

        @SuppressLint("ResourceAsColor")
        public MyViewHolder(@NonNull View itemView) {
            super(itemView);

            // ── Mode A ────────────────────────────────────────────────────
            if (!Constants.Profile_View.equals("Bp")) {
                tv_name = itemView.findViewById(R.id.tv_name);
                email = itemView.findViewById(R.id.email);
                email.setText(R.string.email_);
                phone = itemView.findViewById(R.id.phone);
                phone.setText(R.string.phone_);
                tv_designation = itemView.findViewById(R.id.tv_designation);
                tv_designation_name = itemView.findViewById(R.id.tv_designation_name);
                tv_specialist = itemView.findViewById(R.id.tv_specialist);
                tv_specialist_name = itemView.findViewById(R.id.tv_specialist_name);
                tv_email = itemView.findViewById(R.id.tv_email);
                tv_phonenumber = itemView.findViewById(R.id.tv_phonenumber);
                tv_designation.setText(R.string.designation);
                tv_specialist.setText(R.string.specialist);
                iv_action_menu = itemView.findViewById(R.id.iv_action_menu);
                action_list_card = itemView.findViewById(R.id.action_list_card);
                action_list = itemView.findViewById(R.id.action_list);
                tv_phonenumber.setGravity(Gravity.CENTER_VERTICAL);
                return;
            }

            // ── Mode B ────────────────────────────────────────────────────
            boolean isGHTeamMember = Constants.ROLE.equalsIgnoreCase("GH")
                    || Constants.ROLE.equalsIgnoreCase("TM");
            boolean isAMMorSU = Constants.ROLE.equalsIgnoreCase("AAM")
                    || Constants.ROLE.equalsIgnoreCase("SU");

            person_icon = itemView.findViewById(R.id.person_icon);
            person_icon.setTextSize(DynamicUtils.thirtyFive);
            iv_profile = itemView.findViewById(R.id.iv_profile);
            iv_edit_circle = itemView.findViewById(R.id.iv_edit_circle);
            iv_delete_circle = itemView.findViewById(R.id.iv_delete_circle);
            iv_delete_circle.setVisibility(GONE);

            iv_delete_circle.setOnClickListener(v ->
                    AndroidUtils.showConfirmationDialog(context,
                            "Confirmation",
                            "Are you sure you want to remove the Profile Picture ?",
                            new AndroidUtils.OnConfirmListener() {
                                @Override
                                public void onSave() {
                                    delete();
                                }

                                @Override
                                public void onCancel() {
                                }
                            }));

            iv_edit_circle.setVisibility(VISIBLE);
            currentIvProfile = iv_profile;
            currentPersonIcon = person_icon;
            currentIvEditCircle = iv_edit_circle;
            currentIvDeleteCircle = iv_delete_circle;

            iv_edit_circle.setOnClickListener(v -> {
                currentIvProfile = iv_profile;
                currentPersonIcon = person_icon;
                currentIvDeleteCircle = iv_delete_circle;

                Filecosen fc = new Filecosen(activity, new Filecosen.FileChooserCallback() {
                    @Override
                    public void openCamera() {
                        if (context.checkSelfPermission(android.Manifest.permission.CAMERA)
                                == PackageManager.PERMISSION_GRANTED) {
                            openCamera_new(iv_profile, person_icon, iv_delete_circle);
                        } else {
                            requestCameraPermission.launch(android.Manifest.permission.CAMERA);
                        }
                    }

                    @Override
                    public void openGallery() {
                        openGallery_new(iv_profile, person_icon, iv_delete_circle);
                    }
                });
                fc.show();
            });

            ll_basicprofile = itemView.findViewById(R.id.ll_basicprofile);
            ll_subcription = itemView.findViewById(R.id.ll_subcription);
            ll_additional_inform = itemView.findViewById(R.id.ll_additional_inform);
            ll_subcription.setVisibility(View.GONE);

            iv_edit = itemView.findViewById(R.id.iv_edit);
            iv_edit_addinfo = itemView.findViewById(R.id.iv_edit_addinfo);
            applyTabletLayout(ll_basicprofile, ll_additional_inform);

            iv_edit.setOnClickListener(v -> {
                if (!Constants.is_active) AndroidUtils.showRenewalPopup(activity);
                else {
                    Constants.TARGET_FIELD = "";
                    Constants.PROFILE_EDIT_TAB = "";
                    eventListener.EditProfile(true, false);
                }
            });
            iv_edit_addinfo.setOnClickListener(v -> {
                if (!Constants.is_active) AndroidUtils.showRenewalPopup(activity);
                else {
                    Constants.TARGET_FIELD = "";
                    if (TextUtils.isEmpty(Constants.PROFILE_EDIT_TAB)) {
                        Constants.PROFILE_EDIT_TAB = "practice_details";
                    }
                    eventListener.EditProfile(false, true);
                }
            });
            tv_BasicProfile = itemView.findViewById(R.id.tv_BasicProfile);
            FirmName = itemView.findViewById(R.id.FirmName);
            FirmName.setTextColor(context.getColor(R.color.grey_light));
            tv_FirmName = itemView.findViewById(R.id.tv_FirmName);
            tvPercentage = itemView.findViewById(R.id.tvPercentage);
            profileCompletionBanner = itemView.findViewById(R.id.profileCompletionBanner);
            tvCompleteNow = itemView.findViewById(R.id.tvCompleteNow);
            tvCompleteNow.setPaintFlags(tvCompleteNow.getPaintFlags() | Paint.UNDERLINE_TEXT_FLAG);
            ivClose = itemView.findViewById(R.id.ivClose);
            tvMessage = itemView.findViewById(R.id.tvMessage);
            ivClose.setOnClickListener(v -> updateProfileBanner());

            Country = itemView.findViewById(R.id.Country);
            Country.setTextColor(context.getColor(R.color.grey_light));
            tv_Country = itemView.findViewById(R.id.tv_Country);
            scrollView = itemView.findViewById(R.id.scrollview);
            Email = itemView.findViewById(R.id.Email);
            Email.setTextColor(context.getColor(R.color.grey_light));
            tv_Email = itemView.findViewById(R.id.tv_Email);
            ContactName = itemView.findViewById(R.id.ContactName);
            ContactName.setTextColor(context.getColor(R.color.grey_light));
            tv_ContactName = itemView.findViewById(R.id.tv_ContactName);
            ContactPhone = itemView.findViewById(R.id.ContactPhone);
            ContactPhone.setTextColor(context.getColor(R.color.grey_light));
            tv_ContactPhone = itemView.findViewById(R.id.tv_ContactPhone);
            Website = itemView.findViewById(R.id.Website);
            Website.setTextColor(context.getColor(R.color.grey_light));
            tv_Website = itemView.findViewById(R.id.tv_Website);
            dob = itemView.findViewById(R.id.dob);
            dob.setTextColor(context.getColor(R.color.grey_light));
            tv_dob = itemView.findViewById(R.id.tv_dob);
            gender = itemView.findViewById(R.id.gender);
            gender.setTextColor(context.getColor(R.color.grey_light));
            tv_gender = itemView.findViewById(R.id.tv_gender);
            nationality = itemView.findViewById(R.id.nationality);
            nationality.setTextColor(context.getColor(R.color.grey_light));
            tv_nationality = itemView.findViewById(R.id.tv_nationality);
            bio = itemView.findViewById(R.id.bio);
            bio.setTextColor(context.getColor(R.color.grey_light));
            tv_bio = itemView.findViewById(R.id.tv_bio);

            if (!isFirmProfile() && "solo".equalsIgnoreCase(Constants.CATEGORY)) {
                FirmName.setVisibility(View.GONE);
                tv_FirmName.setVisibility(View.GONE);
                ContactName.setText(R.string.name);
            } else {
                FirmName.setVisibility(VISIBLE);
                tv_FirmName.setVisibility(VISIBLE);
                ContactName.setText(isGHTeamMember ? context.getString(R.string.name) : context.getString(R.string._contact_name));
            }

            if ("entity".equalsIgnoreCase(Constants.CATEGORY)) {
                dob.setVisibility(View.GONE);
                tv_dob.setVisibility(View.GONE);
                gender.setVisibility(View.GONE);
                tv_gender.setVisibility(View.GONE);
            } else {
                dob.setVisibility(VISIBLE);
                tv_dob.setVisibility(VISIBLE);
                gender.setVisibility(VISIBLE);
                tv_gender.setVisibility(VISIBLE);
            }

            // Registered address
            tv_Address = itemView.findViewById(R.id.tv_Address);
            Building = itemView.findViewById(R.id.Building);
            Building.setTextColor(context.getColor(R.color.grey_light));
            tv_Building = itemView.findViewById(R.id.tv_Building);
            Street = itemView.findViewById(R.id.Street);
            Street.setTextColor(context.getColor(R.color.grey_light));
            tv_Street = itemView.findViewById(R.id.tv_Street);
            City = itemView.findViewById(R.id.City);
            City.setTextColor(context.getColor(R.color.grey_light));
            tv_City = itemView.findViewById(R.id.tv_City);
            State = itemView.findViewById(R.id.State);
            State.setTextColor(context.getColor(R.color.grey_light));
            tv_State = itemView.findViewById(R.id.tv_State);
            Ad_Country = itemView.findViewById(R.id.Ad_Country);
            Ad_Country.setTextColor(context.getColor(R.color.grey_light));
            tv_Ad_Country = itemView.findViewById(R.id.tv_Ad_Country);
            Zip = itemView.findViewById(R.id.Zip);
            Zip.setTextColor(context.getColor(R.color.grey_light));
            tv_Zip = itemView.findViewById(R.id.tv_Zip);

            // Mailing address
            mtv_Address = itemView.findViewById(R.id.tv_Address1);
            mBuilding = itemView.findViewById(R.id.Building1);
            mBuilding.setTextColor(context.getColor(R.color.grey_light));
            mtv_Building = itemView.findViewById(R.id.tv_Building1);
            mStreet = itemView.findViewById(R.id.Street1);
            mStreet.setTextColor(context.getColor(R.color.grey_light));
            mtv_Street = itemView.findViewById(R.id.tv_Street1);
            mCity = itemView.findViewById(R.id.City1);
            mCity.setTextColor(context.getColor(R.color.grey_light));
            mtv_City = itemView.findViewById(R.id.tv_City1);
            mState = itemView.findViewById(R.id.State1);
            mState.setTextColor(context.getColor(R.color.grey_light));
            mtv_State = itemView.findViewById(R.id.tv_State1);
            mAd_Country = itemView.findViewById(R.id.Ad_Country1);
            mAd_Country.setTextColor(context.getColor(R.color.grey_light));
            mtv_Ad_Country = itemView.findViewById(R.id.tv_Ad_Country1);
            mZip = itemView.findViewById(R.id.Zip1);
            mZip.setTextColor(context.getColor(R.color.grey_light));
            mtv_Zip = itemView.findViewById(R.id.tv_Zip1);

            // Subscription views — bind only if NOT GH/TM
            if (!isGHTeamMember) {
                stv_subscription = ll_subcription.findViewById(R.id.tv_Address);
                saccount_activated_on = ll_subcription.findViewById(R.id.Building);
                tv_amount = ll_subcription.findViewById(R.id.tv_amount);
                tv_amount.setVisibility(GONE);
                amount = ll_subcription.findViewById(R.id.amount);
                amount.setVisibility(GONE);
                ll_payment = ll_subcription.findViewById(R.id.ll_payment);
                ll_delete_account = ll_subcription.findViewById(R.id.ll_delete_account);
                account_del = ll_delete_account.findViewById(R.id.account_del);
                tv_delete_account = ll_delete_account.findViewById(R.id.tv_delete_account);
                termsDate = ll_subcription.findViewById(R.id.termsDate);
                tv_termsDate = ll_subcription.findViewById(R.id.tv_termsDate);
                account_del.setText(R.string.account_delete_request);
                tvaccount_activated_on = ll_subcription.findViewById(R.id.tv_Building);
                ssubscription_start_date = ll_subcription.findViewById(R.id.Street);
                tvsubscription_start_date = ll_subcription.findViewById(R.id.tv_Street);
                splan_details = ll_subcription.findViewById(R.id.City);
                tvplan_details = ll_subcription.findViewById(R.id.tv_City);
                smode_of_payment = ll_subcription.findViewById(R.id.State);
                tvmode_of_payment = ll_subcription.findViewById(R.id.tv_State);
                splan_validity = ll_subcription.findViewById(R.id.Ad_Country);
                tvplan_validity = ll_subcription.findViewById(R.id.tv_Ad_Country);
                snext_billing_date = ll_subcription.findViewById(R.id.Zip);
                tvnext_billing_date = ll_subcription.findViewById(R.id.tv_Zip);

                tv_delete_account.setOnClickListener(v ->
                        AndroidUtils.showConfirmationDialog(context,
                                "Confirmation",
                                "This action cannot be undone. You will lose access to all your data.\n\nAre you sure?",
                                new AndroidUtils.OnConfirmListener() {
                                    @Override
                                    public void onSave() {
                                        DeleteAccount();
                                    }

                                    @Override
                                    public void onCancel() {
                                    }
                                }));
            }

            // ── Tabs ─────────────────────────────────────────────────────
            tabLayout = itemView.findViewById(R.id.tab_layout);
            ll_viewscreen = itemView.findViewById(R.id.ll_viewscreen);
            tv_add_info = itemView.findViewById(R.id.tv_add_info);
            tv_add_info.setText(R.string.additional_info);
            tv_add_info.setTextColor(context.getColor(R.color.Blue_text_color));
            tv_add_info.setTextSize(20);

            boolean fp = isFirmProfile();
            boolean isAMMorSUVH = Constants.ROLE.equalsIgnoreCase("AAM");

            tabLayout.clearOnTabSelectedListeners();
            tabLayout.removeAllTabs();

            // Tab 0: Practice Details
            tabLayout.addTab(tabLayout.newTab().setText("Practice\nDetails"));

            if (fp || isAMMorSUVH) {
                // For Firm Profile: Tab 1 = Awards & Recognition
                tabLayout.addTab(tabLayout.newTab().setText("Awards &\nRecognition"));
            } else {
                // For My Profile: Tab 1 = Courts & Cases, Tab 2 = Education & Awards, Tab 3 = Availability
                tabLayout.addTab(tabLayout.newTab().setText("Courts\n& Cases"));
                tabLayout.addTab(tabLayout.newTab().setText("Education\n& Awards"));
                tabLayout.addTab(tabLayout.newTab().setText("Set\nAvailability"));
            }

            // Force multi-line text in all tabs - FIXED
            for (int i = 0; i < tabLayout.getTabCount(); i++) {
                TabLayout.Tab t = tabLayout.getTabAt(i);
                if (t != null) {
                    setTabTextStyle(t);
                }
            }

            TabLayout.Tab def = tabLayout.getTabAt(0);
            if (def != null) def.select();
        }

        /**
         * Helper method to set tab text style - handles both finding via resource ID
         * and fallback recursive search
         */
        private void setTabTextStyle(TabLayout.Tab tab) {
            try {
                // First try: use reflection to get the text view directly
                java.lang.reflect.Field field = TabLayout.Tab.class.getDeclaredField("mTextView");
                field.setAccessible(true);
                TextView textView = (TextView) field.get(tab);
                if (textView != null) {
                    textView.setSingleLine(false);
                    textView.setGravity(Gravity.CENTER);
                    textView.setMaxLines(2);
                    return;
                }
            } catch (Exception e) {
                // Reflection failed, try fallback
            }

            // Fallback: try to find the text view in the tab's view hierarchy
            View tabView = tab.view;
            if (tabView != null) {
                findTextViewInView(tabView);
            }
        }

        /**
         * Recursively finds the first TextView in a view hierarchy and applies multi-line styles
         */
        private void findTextViewInView(View view) {
            if (view instanceof TextView) {
                TextView tv = (TextView) view;
                tv.setSingleLine(false);
                tv.setGravity(Gravity.CENTER);
                tv.setMaxLines(2);
            } else if (view instanceof ViewGroup) {
                ViewGroup group = (ViewGroup) view;
                for (int i = 0; i < group.getChildCount(); i++) {
                    findTextViewInView(group.getChildAt(i));
                }
            }
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    //  Tablet layout helper
    // ─────────────────────────────────────────────────────────────────────────
    private void applyTabletLayout(LinearLayout llBasic, LinearLayout llAdditional) {
        if (!DynamicUtils.isTablet(context)) return;
        ViewGroup parent = (ViewGroup) llBasic.getParent();
        if (parent == null) return;
        int idx = parent.indexOfChild(llBasic);
        parent.removeView(llBasic);
        parent.removeView(llAdditional);
        LinearLayout row = new LinearLayout(context);
        row.setOrientation(LinearLayout.HORIZONTAL);
        row.setLayoutParams(new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT));
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                0, LinearLayout.LayoutParams.MATCH_PARENT, 1f);
        lp.setMargins(0, 20, 8, 0);
        llBasic.setLayoutParams(lp);
        LinearLayout.LayoutParams rp = new LinearLayout.LayoutParams(
                0, LinearLayout.LayoutParams.MATCH_PARENT, 1f);
        rp.setMargins(8, 20, 0, 0);
        llAdditional.setLayoutParams(rp);
        row.addView(llBasic);
        row.addView(llAdditional);
        parent.addView(row, idx);
    }

    // ─────────────────────────────────────────────────────────────────────────
    //  ReadMore utility
    // ─────────────────────────────────────────────────────────────────────────
    public static class ReadMoreUtils {
        public static void setReadMore(TextView textView, String fullText, int collapsedLines) {
            if (TextUtils.isEmpty(fullText)) {
                textView.setText("");
                return;
            }
            final String MORE = " Read more", LESS = " Read less";
            textView.setText(fullText);
            textView.setEllipsize(null);
            textView.setMaxLines(Integer.MAX_VALUE);
            textView.post(() -> {
                Layout layout = textView.getLayout();
                if (layout == null || layout.getLineCount() <= collapsedLines) return;
                int end = layout.getLineEnd(collapsedLines - 1);
                String trimmed = fullText.substring(0, end).trim();
                SpannableString sp = new SpannableString(trimmed + MORE);
                sp.setSpan(new ClickableSpan() {
                    boolean expanded = false;

                    @Override
                    public void onClick(@NonNull View w) {
                        expanded = !expanded;
                        if (expanded) setExpanded(textView, fullText, LESS, this);
                        else setReadMore(textView, fullText, collapsedLines);
                    }

                    @Override
                    public void updateDrawState(@NonNull TextPaint ds) {
                        ds.setColor(Color.BLACK);
                        ds.setUnderlineText(true);
                    }
                }, sp.length() - MORE.length(), sp.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                textView.setText(sp);
                textView.setMovementMethod(LinkMovementMethod.getInstance());
            });
        }

        private static void setExpanded(TextView tv, String full,
                                        String less, ClickableSpan span) {
            SpannableString sp = new SpannableString(full + less);
            sp.setSpan(span, sp.length() - less.length(), sp.length(),
                    Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            tv.setMaxLines(Integer.MAX_VALUE);
            tv.setText(sp);
            tv.setMovementMethod(LinkMovementMethod.getInstance());
        }
    }
}