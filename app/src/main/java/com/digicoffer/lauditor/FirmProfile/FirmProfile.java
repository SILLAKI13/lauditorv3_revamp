package com.digicoffer.lauditor.FirmProfile;

import static android.view.View.GONE;
import static android.view.View.VISIBLE;

import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.LinearLayoutCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.lifecycle.ViewModelProvider;

import com.digicoffer.lauditor.CommonFiles.GlobalFiles.NewModel;
import com.digicoffer.lauditor.R;
import com.digicoffer.lauditor.Webservice.AsyncTaskCompleteListener;
import com.digicoffer.lauditor.Webservice.HttpResultDo;
import com.digicoffer.lauditor.CommonFiles.GlobalFiles.AndroidUtils;
import com.digicoffer.lauditor.CommonFiles.GlobalFiles.Constants;


public class FirmProfile extends Fragment implements AsyncTaskCompleteListener {
    TextView tvBasicProfile, tvPracticePartners, tvEdit, tvView, tvPercentage;
    FrameLayout flFirmProfile;
    LinearLayoutCompat llc_Edit_View;
    LinearLayout tv_switchEditProfile, ll_profile;
    View tv_switchView;
    View tv_switchCreate;
    private NewModel mViewModel;

    @Override
    public void onClick(View view) {

    }

    public interface OnEditProfileClickListener {
        void onEditProfileClick();
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.activity_firm_profile, container, false);
        return v;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        setupOnBackPressed();
        super.onCreate(savedInstanceState);
    }

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

    @Override
    public void onViewCreated(@NonNull View v, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(v, savedInstanceState);
        ll_profile = v.findViewById(R.id.ll_profile);
        tvView = v.findViewById(R.id.tvView);
        tvEdit = v.findViewById(R.id.tvEdit);
        tvBasicProfile = v.findViewById(R.id.tvBasicProfile);
        tvPracticePartners = v.findViewById(R.id.tvPracticePartners);
        flFirmProfile = v.findViewById(R.id.flFirmProfile);
        llc_Edit_View = v.findViewById(R.id.llc_Edit_View);
        tv_switchView = v.findViewById(R.id.tv_switchView);
        tv_switchCreate = v.findViewById(R.id.tv_switchCreate);
        tv_switchEditProfile = v.findViewById(R.id.tv_switchEditProfile);
        boolean isGHTeamMember = Constants.ROLE.equalsIgnoreCase("GH")
                || Constants.ROLE.equalsIgnoreCase("TM");
        if (Constants.CATEGORY.equals("solo") && Constants.ROLE.equals("SU")) {
            ll_profile.setVisibility(VISIBLE);
        } else if (Constants.isMyProfileClicked || isGHTeamMember) {
            ll_profile.setVisibility(GONE);
        } else {
            ll_profile.setVisibility(VISIBLE);
        }
        tvView.setText(R.string.subscription_);
        tvEdit.setText(R.string.profile_info);
        tvBasicProfile.setText(R.string.profile);
        tvPracticePartners.setText("Practice Partners");

        if (isGHTeamMember) {
            tvView.setVisibility(View.GONE);
            tvEdit.setVisibility(GONE);
            // Make sure subscription state is always false for GH/TM
            Constants.issubscription = false;
        }

        mViewModel = new ViewModelProvider(requireActivity()).get(NewModel.class);
        if (Constants.isMyProfileClicked) {
            mViewModel.setData(getResources().getString(R.string.my_profile));
        } else {
            mViewModel.setData(getResources().getString(R.string.firm_profile));
        }

        if (Constants.Profile_View.equals("Bp")) {
            loadBasicProfile();
        } else {
            loadPracticePartners();
        }

        tvPracticePartners.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                loadPracticePartners();
            }
        });

        tvBasicProfile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                loadBasicProfile();
            }
        });

        tvEdit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Constants.Profile_View = "Bp";
                Constants.Edit_OR_View = "View";
                Constants.issubscription = false;

                tvEdit.setBackgroundDrawable(
                        getContext().getResources().getDrawable(
                                R.drawable.button_left_green_background));
                tvView.setBackgroundDrawable(
                        getContext().getResources().getDrawable(
                                R.drawable.button_right_background));
                tvEdit.setTextColor(Color.WHITE);
                tvView.setTextColor(Color.BLACK);

                ChangePage();
                Constants.PROFILE_EDIT_TAB = "practice_details";
            }
        });

        tvView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // ── GH / TM: subscription tab is hidden, this click
                //    should never fire, but guard it anyway ──────────
                boolean isGH = Constants.ROLE.equalsIgnoreCase("GH") ||
                        Constants.ROLE.equalsIgnoreCase("TM");
                if (isGH) return;

                Constants.Edit_OR_View = "View";
                Constants.issubscription = true;
                ChangeBackGround();
            }
        });

        AndroidUtils.setupModuleView(
                tv_switchView,
                getString(R.string.add_practice_partners),
                true,
                true,
                getContext(),
                getString(R.string.view_practice_partners),
                clickedView -> {
                    if (!Constants.is_active) {
                        AndroidUtils.showRenewalPopup(getActivity());
                    } else {
                        tv_switchView.setVisibility(GONE);
                        tv_switchCreate.setVisibility(VISIBLE);
                        NavFragment(new PracticePartnerAdd(null, this));
                        mViewModel.setData(getResources().getString(R.string.add_practice_partners));
                    }
                }
        );

        AndroidUtils.setupModuleView(
                tv_switchCreate,
                getString(R.string.view_practice_partners),
                false,
                true,
                getContext(),
                getString(R.string.add_practice_partners),
                clickedView -> {
                    tv_switchView.setVisibility(VISIBLE);
                    tv_switchCreate.setVisibility(GONE);
                    NavFragment(PracticePartnerView.newInstance(this));
                    if (Constants.isMyProfileClicked) {
                        mViewModel.setData(getResources().getString(R.string.my_profile));
                    } else {
                        mViewModel.setData(getResources().getString(R.string.firm_profile));
                    }
                }
        );
    }

    public void loadBasicProfile() {
        Constants.Profile_View = "Bp";
        Constants.Edit_OR_View = "View";
        Constants.issubscription = false;
        tvEdit.setText(R.string.profile_info);
        if (Constants.isMyProfileClicked) {
            mViewModel.setData(getResources().getString(R.string.my_profile));
        } else {
            mViewModel.setData(getResources().getString(R.string.firm_profile));
        }
        llc_Edit_View.setVisibility(VISIBLE);

        // ── GH / TM: keep Subscription tab hidden on every load ───────
        boolean isGHTeamMember = Constants.ROLE.equalsIgnoreCase("GH") ||
                Constants.ROLE.equalsIgnoreCase("TM");
        if (isGHTeamMember) {
            tvView.setVisibility(View.GONE);
        }

        ChangeBackGround();
        tv_switchView.setVisibility(GONE);
        tv_switchCreate.setVisibility(GONE);
    }

    public void loadPracticePartners() {
        Constants.Profile_View = "Pp";
        Constants.Edit_OR_View = "View";
        llc_Edit_View.setVisibility(GONE);
        tv_switchView.setVisibility(GONE);
        tv_switchCreate.setVisibility(VISIBLE);
        ChangeBackGround();
    }

    public void ChangeBackGround() {
        // ── GH / TM: never allow issubscription = true ────────────────
        boolean isGHTeamMember = Constants.ROLE.equalsIgnoreCase("GH") ||
                Constants.ROLE.equalsIgnoreCase("TM");
        if (isGHTeamMember) {
            Constants.issubscription = false;
            tvView.setVisibility(View.GONE);

            tvBasicProfile.setBackgroundDrawable(
                    getContext().getResources().getDrawable(
                            R.drawable.rectangular_button_green_count));
        }

        if (Constants.Profile_View.equals("Bp")) {
            tvBasicProfile.setBackgroundDrawable(
                    getContext().getResources().getDrawable(
                            R.drawable.button_left_green_background));
            tvPracticePartners.setBackgroundDrawable(
                    getContext().getResources().getDrawable(
                            R.drawable.button_right_background));
            tvBasicProfile.setTextColor(Color.WHITE);
            tvPracticePartners.setTextColor(Color.BLACK);
            tvEdit.setText(R.string.profile_info);
            if (Constants.isMyProfileClicked) {
                mViewModel.setData(getResources().getString(R.string.my_profile));
            } else {
                mViewModel.setData(getResources().getString(R.string.firm_profile));
            }
            llc_Edit_View.setVisibility(VISIBLE);
        } else {
            llc_Edit_View.setVisibility(GONE);
            tvBasicProfile.setBackgroundDrawable(
                    getContext().getResources().getDrawable(
                            R.drawable.button_left_background));
            tvPracticePartners.setBackgroundDrawable(
                    getContext().getResources().getDrawable(
                            R.drawable.button_right_green_count));
            tvPracticePartners.setTextColor(Color.WHITE);
            tvBasicProfile.setTextColor(Color.BLACK);
            tvEdit.setText(R.string.add);
            mViewModel.setData(getResources().getString(R.string.practice_partners));
        }

        if (Constants.Edit_OR_View.equals("Edit")) {
            tvEdit.setBackgroundDrawable(
                    getContext().getResources().getDrawable(
                            R.drawable.button_left_green_background));
            tvView.setBackgroundDrawable(
                    getContext().getResources().getDrawable(
                            R.drawable.button_right_background));
            tvEdit.setTextColor(Color.WHITE);
            tvView.setTextColor(Color.BLACK);
        } else if (Constants.issubscription) {
            // issubscription is always false for GH/TM so this block
            // will never execute for them
            tvEdit.setBackgroundDrawable(
                    getContext().getResources().getDrawable(
                            R.drawable.button_left_background));
            tvView.setBackgroundDrawable(
                    getContext().getResources().getDrawable(
                            R.drawable.button_right_green_background));
            tvEdit.setTextColor(Color.BLACK);
            tvView.setTextColor(Color.WHITE);
        } else {
            tvEdit.setBackgroundDrawable(
                    getContext().getResources().getDrawable(
                            R.drawable.button_left_green_background));
            tvView.setBackgroundDrawable(
                    getContext().getResources().getDrawable(
                            R.drawable.button_right_background));
            tvEdit.setTextColor(Color.WHITE);
            tvView.setTextColor(Color.BLACK);
        }

        if ("solo".equalsIgnoreCase(Constants.CATEGORY)) {
            tvPracticePartners.setVisibility(GONE);
            tvBasicProfile.setVisibility(GONE);
            tvBasicProfile.setBackgroundDrawable(
                    getContext().getResources().getDrawable(
                            R.drawable.rectangular_button_green_count));
        } else {
            tvPracticePartners.setVisibility(VISIBLE);
        }

        ChangePage();
    }

    private void ChangePage() {
        if (!Constants.Profile_View.equals("Bp")) {
            if (Constants.Edit_OR_View.equals("Edit")) {
                if (!Constants.is_active) {
                    AndroidUtils.showRenewalPopup(getActivity());
                } else {
                    NavFragment(new PracticePartnerAdd(null, this));
                    mViewModel.setData(getResources().getString(R.string.add_practice_partners));
                }
            } else {
                tv_switchView.setVisibility(VISIBLE);
                tv_switchCreate.setVisibility(GONE);
                NavFragment(PracticePartnerView.newInstance(this));
                mViewModel.setData(getResources().getString(R.string.practice_partners));
            }
        } else {
            if (Constants.Edit_OR_View.equals("Edit")) {
                if (!Constants.is_active) {
                    AndroidUtils.showRenewalPopup(getActivity());
                } else {
                    NavFragment(new BasicProfileEdit(this, true, false));
                    mViewModel.setData(getResources().getString(R.string.edit_profile));
                }
            } else {
                tv_switchView.setVisibility(GONE);
                tv_switchCreate.setVisibility(GONE);
                NavFragment(PracticePartnerView.newInstance(this));
                if (Constants.isMyProfileClicked) {
                    mViewModel.setData(getResources().getString(R.string.my_profile));
                } else {
                    mViewModel.setData(getResources().getString(R.string.firm_profile));
                }
            }
        }
    }

    public void NavFragment(Fragment fragment) {
        FragmentTransaction ft = getChildFragmentManager().beginTransaction();
        ft.replace(R.id.flFirmProfile, fragment);
        ft.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE);
        ft.addToBackStack(null);
        ft.commit();
    }

    @Override
    public void onAsyncTaskComplete(HttpResultDo httpResult) {

    }
}