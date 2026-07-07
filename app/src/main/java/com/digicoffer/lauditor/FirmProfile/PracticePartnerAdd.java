package com.digicoffer.lauditor.FirmProfile;

import android.app.Dialog;
import android.content.res.Resources;
import android.os.Bundle;
import android.text.Editable;
import android.text.InputType;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.LinearLayoutCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.digicoffer.lauditor.CommonFiles.GlobalFiles.NewModel;
import com.digicoffer.lauditor.R;
import com.digicoffer.lauditor.Webservice.AsyncTaskCompleteListener;
import com.digicoffer.lauditor.Webservice.HttpResultDo;
import com.digicoffer.lauditor.Webservice.CommonApiHelper.WebServiceHelper;
import com.digicoffer.lauditor.CommonFiles.GlobalFiles.AndroidUtils;
import com.digicoffer.lauditor.CommonFiles.GlobalFiles.Constants;
import com.digicoffer.lauditor.CommonFiles.GlobalFiles.DynamicUtils;
import com.digicoffer.lauditor.CommonFiles.ValidationUtils.Validation;
import com.google.android.material.textfield.TextInputEditText;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Objects;

public class PracticePartnerAdd extends Fragment implements AsyncTaskCompleteListener, View.OnClickListener {

    TextView tv_add_pp, tv_Fname, tv_Lname, tv_designation, tv_specialist, tv_email, tv_phonenumber;
    TextInputEditText et_Fname, et_Lname, et_designation, et_specialist, et_email, et_phonenumber;
    Button btn_cancel_pp, btn_save_pp;
    Dialog progress_dialog;
    MemberProfileModel memberProfileModel;
    FirmProfile firmProfile;
    boolean isemailhasError = false;
    ImageView iv_cancel;
    boolean isphonehasError = false;
    TextView response_email, tvErrorPhone;
    LinearLayoutCompat last_name_layout;
    private NewModel mViewModel;

    public PracticePartnerAdd(MemberProfileModel model, FirmProfile firmProfile) {
        memberProfileModel = model;
        this.firmProfile = firmProfile;
    }

    @Override
    public void onClick(View view) {
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.activity_practice_partner_add, container, false);
        return v;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        setupOnBackPressed();
        super.onCreate(savedInstanceState);
    }

    private void setupOnBackPressed() {
        requireActivity().getOnBackPressedDispatcher().addCallback(new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                if (isEnabled()) {
//                    AndroidUtils.showToast("Dashboard",getContext());
                    setEnabled(false);
                    requireActivity().onBackPressed();
                }
            }
        });
    }

    @Override
    public void onViewCreated(@NonNull View v, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(v, savedInstanceState);
        try {
            mViewModel = new ViewModelProvider(requireActivity()).get(NewModel.class);
            tv_add_pp = v.findViewById(R.id.tv_add_pp);
            tv_Fname = v.findViewById(R.id.tv_Fname);
            tv_Lname = v.findViewById(R.id.tv_Lname);
            iv_cancel = v. findViewById(R.id.iv_cancel);
            tv_designation = v.findViewById(R.id.tv_designation);
            tv_specialist = v.findViewById(R.id.tv_specialist);
            tv_email = v.findViewById(R.id.tv_email);
            tv_phonenumber = v.findViewById(R.id.tv_phonenumber);
            last_name_layout = v.findViewById(R.id.last_name_layout);
            last_name_layout.setVisibility(View.GONE);
            et_Fname = v.findViewById(R.id.et_Fname);
            et_Fname.addTextChangedListener(new Validation(et_Fname));

            et_Lname = v.findViewById(R.id.et_Lname);
            et_Lname.setVisibility(View.GONE);

            et_designation = v.findViewById(R.id.et_designation);
            et_designation.addTextChangedListener(new Validation(et_designation));
            et_specialist = v.findViewById(R.id.et_specialist);
            et_specialist.addTextChangedListener(new Validation(et_specialist));
            response_email = v.findViewById(R.id.response_email);
            tvErrorPhone = v.findViewById(R.id.tvErrorPhone);
            et_email = v.findViewById(R.id.et_email);
            et_email.addTextChangedListener(new Validation(et_email));
            et_email.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                    response_email.setVisibility(View.GONE);
                    isemailhasError = false;
                }

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {
                }

                @Override
                public void afterTextChanged(Editable s) {
                    if (!et_email.getText().toString().trim().isEmpty()) {
                        if (!AndroidUtils.isValidEmail(et_email.getText().toString())) {
//                    tv_email.setError("Enter a valid email address");
//                    AndroidUtils.showAlert("Please enter the Email", getContext());
                            response_email.setVisibility(View.VISIBLE);
                            response_email.setText(R.string.enter_a_valid_email_address);
                            isemailhasError = true;
//                hasErrors = true;
                        } else {
                            response_email.setVisibility(View.GONE);
                            isemailhasError = false;
                        }
                    }

                }
            });
            et_phonenumber = v.findViewById(R.id.et_phonenumber);
            et_phonenumber.setInputType(InputType.TYPE_CLASS_NUMBER);
            et_phonenumber.addTextChangedListener(new Validation(et_phonenumber));
            AndroidUtils.NumberFilter(et_phonenumber, true);

            et_phonenumber.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                    tvErrorPhone.setVisibility(View.GONE);
                    isphonehasError = false;
                }

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {
                }

                @Override
                public void afterTextChanged(Editable s) {
                    if (!s.toString().isEmpty()) {
                        if (s.length() < 10) {
                            tvErrorPhone.setVisibility(View.VISIBLE);
                            tvErrorPhone.setText(R.string.please_enter_the_10_digit_mobile_number);
                            isphonehasError = true;
                        } else {
                            tvErrorPhone.setVisibility(View.GONE);
                            isphonehasError = false;
                        }
                    }

                }
            });

            btn_cancel_pp = v.findViewById(R.id.btn_cancel_pp);
            btn_save_pp = v.findViewById(R.id.btn_save_pp);
            setData();
            if ((memberProfileModel != null) && (Constants.PpView_Type.equals("update"))) {
                mViewModel.setData(getResources().getString(R.string.practice_partners));
                tv_add_pp.setText(R.string.edit_practice_partners);
                et_Fname.setText(memberProfileModel.getFirst_name());

                et_designation.setText(memberProfileModel.getDesignation());
                et_specialist.setText(memberProfileModel.getPractice());
                et_email.setText(memberProfileModel.getEmail());
                et_phonenumber.setText(memberProfileModel.getPhone());
            } else {
                mViewModel.setData(getResources().getString(R.string.add_practice_partners));
                tv_add_pp.setText(R.string.add_practice_partners);
                et_Fname.setText("");

                et_designation.setText("");
                et_specialist.setText("");
                et_email.setText("");
                et_phonenumber.setText("");
            }

            btn_save_pp.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if ((Objects.requireNonNull(et_Fname.getText()).toString().isEmpty())  || (Objects.requireNonNull(et_designation.getText()).toString().isEmpty()) || (Objects.requireNonNull(et_specialist.getText()).toString().isEmpty()) || (Objects.requireNonNull(et_email.getText()).toString().isEmpty()) || (Objects.requireNonNull(et_phonenumber.getText()).toString().isEmpty()) || (isphonehasError) || (isemailhasError)) {
                        String msg = "Please enter the";
                        if (Objects.requireNonNull(et_Fname.getText()).toString().isEmpty()) {
                            msg = msg + " First Name";
                        }
//                        if (Objects.requireNonNull(et_Lname.getText()).toString().isEmpty()) {
//                            if (msg.equals("Please enter the")) {
//                                msg = msg + " Last Name";
//                            } else {
//                                msg = msg + ", Last Name";
//                            }
//                        }
                        if (Objects.requireNonNull(et_designation.getText()).toString().isEmpty()) {
                            if (msg.equals("Please enter the")) {
                                msg = msg + " Designation";
                            } else {
                                msg = msg + ", Designation";
                            }
                        }
                        if (Objects.requireNonNull(et_specialist.getText()).toString().isEmpty()) {
                            if (msg.equals("Please enter the")) {
                                msg = msg + " Specialist";
                            } else {
                                msg = msg + ", Specialist";
                            }
                        }
                        if (Objects.requireNonNull(et_email.getText()).toString().isEmpty()) {
                            if (msg.equals("Please enter the")) {
                                msg = msg + " Email";
                            } else {
                                msg = msg + ", Email";
                            }
                        } else {
                            if (isemailhasError) {
                                if (msg.equals("Please enter the")) {
                                    msg = msg + " valid Email";
                                } else {
                                    msg = msg + ", valid Email";
                                }
                            }
                        }
                        if (Objects.requireNonNull(et_phonenumber.getText()).toString().isEmpty()) {
                            if (msg.equals("Please enter the")) {
                                msg = msg + " Phone";
                            } else {
                                msg = msg + ", Phone";
                            }
                        } else {
                            if (isphonehasError) {
                                if (msg.equals("Please enter the")) {
                                    msg = msg + " valid Phone number";
                                } else {
                                    msg = msg + ", valid Phone number";
                                }
                            }
                        }
                        AndroidUtils.showAlert(msg, getActivity());
                    } else {
                        if ((memberProfileModel != null) && (Constants.PpView_Type.equals("update"))) {
                            SaveDetailsPp("update");
                        } else {
                            SaveDetailsPp("add");
                            Constants.PpView_Type = "";
                        }
                    }
                }
            });
            btn_cancel_pp.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    clearDetails();
                }
            });
            iv_cancel.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    clearDetails();
                }
            });
        } catch (Resources.NotFoundException e) {
            e.fillInStackTrace();
        }
    }

    private void SaveDetailsPp(String save_type) {
//        https:api.staging.digicoffer.com/professional/partners/add
        progress_dialog = AndroidUtils.get_progress(getActivity());
        try {
            JSONObject jsonObject = new JSONObject();
//        {"first_name":"bharathi","last_name":"ganesh","designation":"Developer","phone":"1111111111","email":"bharathi.ganesh@digicoffer.com","practice":"Swift, Java"}
            jsonObject.put("name", Objects.requireNonNull(et_Fname.getText()).toString());

            jsonObject.put("designation", Objects.requireNonNull(et_designation.getText()).toString());
            jsonObject.put("practice", Objects.requireNonNull(et_specialist.getText()).toString());
            jsonObject.put("email", Objects.requireNonNull(et_email.getText()).toString());
            jsonObject.put("phone", Objects.requireNonNull(et_phonenumber.getText()).toString());

            if (save_type.equals("update"))
                WebServiceHelper.callHttpWebService(this, getContext(), WebServiceHelper.RestMethodType.PUT, "v3/practice-partner/" + memberProfileModel.getId(), "Update_Pp", jsonObject.toString());
            else
                WebServiceHelper.callHttpWebService(this, getContext(), WebServiceHelper.RestMethodType.POST, "v3/practice-partner", "Add_Pp", jsonObject.toString());

        } catch (Exception e) {
            e.fillInStackTrace();
        }
    }

    private void setData() {
        tv_add_pp.setText(R.string.add_practice_partners);
        tv_add_pp.setTextSize(DynamicUtils.twenty);
        tv_Fname.setText(R.string.name);
        tv_Lname.setText(R.string.last_name);
        tv_designation.setText(R.string.desg);
        tv_specialist.setText(R.string.specs);
        tv_email.setText(R.string.email_add);
        tv_phonenumber.setText(R.string.phone_no);

        et_Fname.setHint(R.string.name);
        et_Lname.setHint(R.string.last_name);
        et_designation.setHint(R.string.desg);
        et_specialist.setHint(R.string.specs);
        et_email.setHint(R.string.email_add);
        et_phonenumber.setHint(R.string.phone_no);
    }

    private void clearDetails() {
        et_Fname.setText("");
        et_Lname.setText("");
        et_designation.setText("");
        et_specialist.setText("");
        et_email.setText("");
        et_phonenumber.setText("");
        Constants.Edit_OR_View = "View";
        firmProfile.ChangeBackGround();
        mViewModel.setData(getResources().getString(R.string.practice_partners));
        firmProfile.NavFragment(PracticePartnerView.newInstance(firmProfile));
    }


    @Override
    public void onAsyncTaskComplete(HttpResultDo httpResult) {
        // Dismiss progress dialog
        if (progress_dialog != null && progress_dialog.isShowing()) {
            AndroidUtils.dismiss_dialog(progress_dialog);
        }

        String responseContent = httpResult.getResponseContent(); // raw response
        int statusCode = httpResult.getStatus_code(); // HTTP status
        String requestType = httpResult.getRequestType();

        Log.d("Request_Type", requestType);

        try {
            JSONObject resultJson = new JSONObject(responseContent);

            // Handle Add_Pp request
            if (Objects.equals(requestType, "Add_Pp")) {
                // Handle Unauthorized explicitly
                if (statusCode == 401) {
                    String msg = resultJson.optString("msg", "Unauthorized access");
                    AndroidUtils.showAlert(msg, getActivity());
                    return;
                }

                boolean isError = resultJson.optBoolean("error", false);
                String msg = resultJson.optString("msg", isError ? "Something went wrong" : "Operation completed");
                AndroidUtils.showAlert(msg, getActivity());

                if (!isError) {
                    clearDetails();
                }
            }
            // Handle Update_Pp request
            else if (Objects.equals(requestType, "Update_Pp")) {
                boolean isError = resultJson.optBoolean("error", false);
                String msg = resultJson.optString("msg", isError ? "Something went wrong" : "Operation completed");
                AndroidUtils.showAlert(msg, getActivity());

                if (!isError) {
                    clearDetails();
                }
            }
            // Optional: handle other request types here
            else {
                // Default handling for other requests
                boolean isError = resultJson.optBoolean("error", false);
                String msg = resultJson.optString("msg", isError ? "Something went wrong" : "Operation completed");
                AndroidUtils.showAlert(msg, getActivity());
            }

        } catch (JSONException e) {
            e.printStackTrace();
            AndroidUtils.showAlert("Something went wrong", getActivity());
        } catch (Exception e) {
            e.printStackTrace();
            AndroidUtils.showAlert("An unexpected error occurred", getActivity());
        }
    }

}