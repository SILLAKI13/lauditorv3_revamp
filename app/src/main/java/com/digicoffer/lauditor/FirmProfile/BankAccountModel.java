package com.digicoffer.lauditor.FirmProfile;

import org.json.JSONObject;

public class BankAccountModel {

    private boolean error;
    private AccountDetails accountDetails;

    public boolean isError() {
        return error;
    }

    public AccountDetails getAccountDetails() {
        return accountDetails;
    }

    public static BankAccountModel fromJson(String json) {
        BankAccountModel model = new BankAccountModel();
        try {
            JSONObject root = new JSONObject(json);
            model.error = root.optBoolean("error", false);
            JSONObject data = root.optJSONObject("data");
            if (data != null && data.has("account_details") && !data.isNull("account_details")) {
                JSONObject acc = data.optJSONObject("account_details");
                if (acc != null) {
                    AccountDetails details = new AccountDetails();
                    details.accountId = acc.optString("account_id", "");
                    details.accountHolderName = acc.optString("account_holder_name", "");
                    details.accountNumber = acc.optString("account_number", "");
                    details.ifscCode = acc.optString("ifsc_code", "");
                    details.bankName = acc.optString("bank_name", "");
                    details.branchName = acc.optString("branch_name", "");
                    details.verificationStatus = acc.optBoolean("verification_status", false);
                    details.isActive = acc.optBoolean("is_active", false);
                    details.verifiedOn = acc.optString("verified_on",
                            acc.optString("verified_at", ""));
                    model.accountDetails = details;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return model;
    }

    public static class AccountDetails {
        public String accountId = "";
        public String accountHolderName = "";
        public String accountNumber = "";
        public String ifscCode = "";
        public String bankName = "";
        public String branchName = "";
        public boolean verificationStatus = false;
        public boolean isActive = false;
        public String verifiedOn = "";
    }
}