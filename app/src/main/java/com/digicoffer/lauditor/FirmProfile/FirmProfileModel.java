package com.digicoffer.lauditor.FirmProfile;

import org.json.JSONArray;

import java.util.List;

public class FirmProfileModel {

    private boolean error;
    private Data data;

    public boolean isError() {
        return error;
    }

    public void setError(boolean error) {
        this.error = error;
    }

    public Data getData() {
        return data;
    }

    public void setData(Data data) {
        this.data = data;
    }

    // ================= DATA =================
    public static class Data {
        private Profile profile;

        public Profile getProfile() {
            return profile;
        }

        public void setProfile(Profile profile) {
            this.profile = profile;
        }
    }

    // ================= NEXT STEP =================
    public static class NextStep {
        private String section;
        private int priority;
        private String redirect_tab;
        private List<String> missing_fields;

        public String getSection() {
            return safe(section);
        }

        public void setSection(String section) {
            this.section = section;
        }

        public int getPriority() {
            return priority;
        }

        public void setPriority(int priority) {
            this.priority = priority;
        }

        public String getRedirect_tab() {
            return safe(redirect_tab);
        }

        public void setRedirect_tab(String redirect_tab) {
            this.redirect_tab = redirect_tab;
        }

        public List<String> getMissing_fields() {
            return missing_fields;
        }

        public void setMissing_fields(List<String> missing_fields) {
            this.missing_fields = missing_fields;
        }
    }

    // ================= COURT ENROLLMENT =================
    public static class CourtEnrollment {
        private String court_type;
        private String court_name;
        private String state;
        private String city;

        public String getCourt_type() {
            return safe(court_type);
        }

        public void setCourt_type(String court_type) {
            this.court_type = court_type;
        }

        public String getCourt_name() {
            return safe(court_name);
        }

        public void setCourt_name(String court_name) {
            this.court_name = court_name;
        }

        public String getState() {
            return safe(state);
        }

        public void setState(String state) {
            this.state = state;
        }

        public String getCity() {
            return safe(city);
        }

        public void setCity(String city) {
            this.city = city;
        }
    }

    // ================= PROFILE =================
    public static class Profile {
        private Boolean show_profile_banner = true;
        private String uid;
        private Firm firm;
        private String name;
        private String email;
        private String mobile;  // Changed from 'phone' to 'mobile' to match JSON
        private String date_of_birth;
        private String gender;
        private String nationality;
        private String bio_description;
        private JSONArray languages_spoken;
        private String bar_council_id;
        private int years_of_experience;
        private JSONArray practice_areas;
        private JSONArray services_offered;
        private List<Education> education;
        private List<Certification> certifications;
        private List<Award> awards;
        private ConsultationFee consultation_fee;  // Changed from String to object
        private Availability availability;
        private Subscription subscription;
        private ProfileCompletion profile_completion;
        private int profile_completion_percentage;  // Keep for backward compatibility
        private String profile_pic_hash;
        private String profile_pic_url;
        private String accepted_t_c_date;
        private String firm_description;
        private Terms terms;
        private String reg_id;
        private int years_of_incorporation;
        private JSONArray practice_areas_firm;
        // Inside Profile class — add these two fields alongside existing ones
        private List<String> cases_handled;
        private List<CourtEnrollment> court_enrollments;

        // Getters & Setters
        public List<String> getCases_handled() {
            return cases_handled;
        }

        public void setCases_handled(List<String> cases_handled) {
            this.cases_handled = cases_handled;
        }

        public List<CourtEnrollment> getCourt_enrollments() {
            return court_enrollments;
        }

        public void setCourt_enrollments(List<CourtEnrollment> court_enrollments) {
            this.court_enrollments = court_enrollments;
        }
        // ================= PROFILE class — ADD these missing getters/setters =================

        public String getReg_id() {
            return safe(reg_id);
        }

        public void setReg_id(String reg_id) {
            this.reg_id = reg_id;
        }

        public int getYears_of_incorporation() {
            return years_of_incorporation;
        }

        public void setYears_of_incorporation(int years_of_incorporation) {
            this.years_of_incorporation = years_of_incorporation;
        }

        public JSONArray getPractice_areas_firm() {
            return practice_areas_firm;
        }

        public void setPractice_areas_firm(JSONArray practice_areas_firm) {
            this.practice_areas_firm = practice_areas_firm;
        }

        // Getters and Setters
        public String getUid() {
            return uid;
        }

        public String getFirm_description() {
            return firm_description;
        }

        public void setFirm_description(String firm_description) {
            this.firm_description = firm_description;
        }

        public void setUid(String uid) {
            this.uid = uid;
        }

        public Firm getFirm() {
            return firm;
        }

        public void setFirm(Firm firm) {
            this.firm = firm;
        }

        public String getName() {
            return safe(name);
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getEmail() {
            return safe(email);
        }

        public void setEmail(String email) {
            this.email = email;
        }

        public String getMobile() {
            return safe(mobile);
        }

        public void setMobile(String mobile) {
            this.mobile = mobile;
        }

        public String getDate_of_birth() {
            return safe(date_of_birth);
        }

        public void setDate_of_birth(String date_of_birth) {
            this.date_of_birth = date_of_birth;
        }

        public String getGender() {
            return safe(gender);
        }

        public void setGender(String gender) {
            this.gender = gender;
        }

        public String getNationality() {
            return safe(nationality);
        }

        public void setNationality(String nationality) {
            this.nationality = nationality;
        }

        public String getBio_description() {
            return safe(bio_description);
        }

        public void setBio_description(String bio_description) {
            this.bio_description = bio_description;
        }

        public JSONArray getLanguages_spoken() {
            return languages_spoken;
        }

        public void setLanguages_spoken(JSONArray languages_spoken) {
            this.languages_spoken = languages_spoken;
        }

        public String getBar_council_id() {
            return safe(bar_council_id);
        }

        public void setBar_council_id(String bar_council_id) {
            this.bar_council_id = bar_council_id;
        }

        public int getYears_of_experience() {
            return years_of_experience;
        }

        public void setYears_of_experience(int years_of_experience) {
            this.years_of_experience = years_of_experience;
        }

        public JSONArray getPractice_areas() {
            return practice_areas;
        }

        public void setPractice_areas(JSONArray practice_areas) {
            this.practice_areas = practice_areas;
        }

        public JSONArray getServices_offered() {
            return services_offered;
        }

        public void setServices_offered(JSONArray services_offered) {
            this.services_offered = services_offered;
        }

        public List<Education> getEducation() {
            return education;
        }

        public void setEducation(List<Education> education) {
            this.education = education;
        }

        public List<Certification> getCertifications() {
            return certifications;
        }

        public void setCertifications(List<Certification> certifications) {
            this.certifications = certifications;
        }

        public List<Award> getAwards() {
            return awards;
        }

        public void setAwards(List<Award> awards) {
            this.awards = awards;
        }

        public ConsultationFee getConsultation_fee() {
            return consultation_fee;
        }

        public void setConsultation_fee(ConsultationFee consultation_fee) {
            this.consultation_fee = consultation_fee;
        }

        public Availability getAvailability() {
            return availability;
        }

        public void setAvailability(Availability availability) {
            this.availability = availability;
        }

        public Subscription getSubscription() {
            return subscription;
        }

        public void setSubscription(Subscription subscription) {
            this.subscription = subscription;
        }

        public ProfileCompletion getProfile_completion() {
            return profile_completion;
        }

        public void setProfile_completion(ProfileCompletion profile_completion) {
            this.profile_completion = profile_completion;
        }

        public int getProfile_completion_percentage() {
            return profile_completion_percentage;
        }

        public void setProfile_completion_percentage(int profile_completion_percentage) {
            this.profile_completion_percentage = profile_completion_percentage;
        }

        public String getProfile_pic_hash() {
            return profile_pic_hash;
        }

        public void setProfile_pic_hash(String profile_pic_hash) {
            this.profile_pic_hash = profile_pic_hash;
        }

        public String getProfile_pic_url() {
            return profile_pic_url;
        }

        public void setProfile_pic_url(String profile_pic_url) {
            this.profile_pic_url = profile_pic_url;
        }

        public String getAccepted_t_c_date() {
            return safe(accepted_t_c_date);
        }

        public void setAccepted_t_c_date(String accepted_t_c_date) {
            this.accepted_t_c_date = accepted_t_c_date;
        }

        public Terms getTerms() {
            return terms;
        }

        public void setTerms(Terms terms) {
            this.terms = terms;
        }

        public Boolean getShow_profile_banner() {
            return show_profile_banner;
        }

        public void setShow_profile_banner(Boolean show_profile_banner) {
            this.show_profile_banner = show_profile_banner;
        }
    }

    // ================= CONSULTATION FEE =================
    public static class ConsultationFee {
        private String amount;
        private String symbol;

        public String getAmount() {
            return amount;
        }

        public void setAmount(String amount) {
            this.amount = amount;
        }

        public String getSymbol() {
            return symbol;
        }

        public void setSymbol(String symbol) {
            this.symbol = symbol;
        }
    }

    // ================= TERMS =================
    public static class Terms {
        private boolean accepted;
        private String accepted_at;
        private String version;

        public boolean isAccepted() {
            return accepted;
        }

        public void setAccepted(boolean accepted) {
            this.accepted = accepted;
        }

        public String getAccepted_at() {
            return accepted_at;
        }

        public void setAccepted_at(String accepted_at) {
            this.accepted_at = accepted_at;
        }

        public String getVersion() {
            return version;
        }

        public void setVersion(String version) {
            this.version = version;
        }
    }

    // ================= PROFILE COMPLETION =================
    public static class ProfileCompletion {
        private int completion_percentage;
        private NextStep next_step;
        private List<NextStep> all_incomplete;

        public int getCompletion_percentage() {
            return completion_percentage;
        }

        public void setCompletion_percentage(int completion_percentage) {
            this.completion_percentage = completion_percentage;
        }

        public NextStep getNext_step() {
            return next_step;
        }

        public void setNext_step(NextStep next_step) {
            this.next_step = next_step;
        }

        public List<NextStep> getAll_incomplete() {
            return all_incomplete;
        }

        public void setAll_incomplete(List<NextStep> all_incomplete) {
            this.all_incomplete = all_incomplete;
        }
    }

    // ================= FIRM =================
    public static class Firm {
        private Boolean show_profile_banner = true;
        private String fullname;
        private String email;
        private String billing_currency;
        private String contact_phone;
        private String contact_person;
        private String reg_id;  // Added missing field
        private int years_of_incorporation;  // Added missing field
        private String firm_description;  // Added missing field
        private JSONArray practice_areas;  // Added missing field
        private JSONArray services_offered;  // Added missing field
        private List<Award> awards;  // Added missing field
        private String website;
        private Address address;
        private String profile_pic_url;
        private ProfileCompletion profile_completion;
        private int profile_completion_percentage;
        private Address correspondence_address;

        public ProfileCompletion getProfile_completion() {
            return profile_completion;
        }

        public String getProfile_pic_url() {
            return profile_pic_url;
        }

        public void setProfile_pic_url(String profile_pic_url) {
            this.profile_pic_url = profile_pic_url;
        }

        public void setProfile_completion(ProfileCompletion profile_completion) {
            this.profile_completion = profile_completion;
        }

        public int getProfile_completion_percentage() {
            return profile_completion_percentage;
        }

        public void setProfile_completion_percentage(int profile_completion_percentage) {
            this.profile_completion_percentage = profile_completion_percentage;
        }

        // Getters and Setters
        public String getFullname() {
            return safe(fullname);
        }

        public void setFullname(String fullname) {
            this.fullname = fullname;
        }

        public String getEmail() {
            return safe(email);
        }

        public void setEmail(String email) {
            this.email = email;
        }

        public String getBilling_currency() {
            return safe(billing_currency);
        }

        public void setBilling_currency(String billing_currency) {
            this.billing_currency = billing_currency;
        }

        public String getContact_phone() {
            return safe(contact_phone);
        }

        public void setContact_phone(String contact_phone) {
            this.contact_phone = contact_phone;
        }

        public String getContact_person() {
            return safe(contact_person);
        }

        public void setContact_person(String contact_person) {
            this.contact_person = contact_person;
        }

        public String getReg_id() {
            return safe(reg_id);
        }

        public void setReg_id(String reg_id) {
            this.reg_id = reg_id;
        }

        public int getYears_of_incorporation() {
            return years_of_incorporation;
        }

        public void setYears_of_incorporation(int years_of_incorporation) {
            this.years_of_incorporation = years_of_incorporation;
        }

        public String getFirm_description() {
            return safe(firm_description);
        }

        public void setFirm_description(String firm_description) {
            this.firm_description = firm_description;
        }

        public JSONArray getPractice_areas() {
            return practice_areas;
        }

        public void setPractice_areas(JSONArray practice_areas) {
            this.practice_areas = practice_areas;
        }

        public JSONArray getServices_offered() {
            return services_offered;
        }

        public void setServices_offered(JSONArray services_offered) {
            this.services_offered = services_offered;
        }

        public List<Award> getAwards() {
            return awards;
        }

        public void setAwards(List<Award> awards) {
            this.awards = awards;
        }

        public String getWebsite() {
            return safe(website);
        }

        public void setWebsite(String website) {
            this.website = website;
        }

        public Address getAddress() {
            return address;
        }

        public void setAddress(Address address) {
            this.address = address;
        }

        public Address getCorrespondence_address() {
            return correspondence_address;
        }

        public void setCorrespondence_address(Address correspondence_address) {
            this.correspondence_address = correspondence_address;
        }

        public Boolean getShow_profile_banner() {
            return show_profile_banner;
        }

        public void setShow_profile_banner(Boolean show_profile_banner) {
            this.show_profile_banner = show_profile_banner;
        }
    }

    // ================= ADDRESS =================
    public static class Address {
        private String house_flat_no;
        private String street;
        private String city_town;
        private String state;
        private String country;
        private String zipcode;

        public String getHouse_flat_no() {
            return safe(house_flat_no);
        }

        public void setHouse_flat_no(String house_flat_no) {
            this.house_flat_no = house_flat_no;
        }

        public String getStreet() {
            return safe(street);
        }

        public void setStreet(String street) {
            this.street = street;
        }

        public String getCity_town() {
            return safe(city_town);
        }

        public void setCity_town(String city_town) {
            this.city_town = city_town;
        }

        public String getState() {
            return safe(state);
        }

        public void setState(String state) {
            this.state = state;
        }

        public String getCountry() {
            return safe(country);
        }

        public void setCountry(String country) {
            this.country = country;
        }

        public String getZipcode() {
            return safe(zipcode);
        }

        public void setZipcode(String zipcode) {
            this.zipcode = zipcode;
        }
    }

    // ================= EDUCATION =================
    public static class Education {
        private String degree;
        private String university;
        private int passing_year;

        public String getDegree() {
            return degree;
        }

        public void setDegree(String degree) {
            this.degree = degree;
        }

        public String getUniversity() {
            return university;
        }

        public void setUniversity(String university) {
            this.university = university;
        }

        public int getPassing_year() {
            return passing_year;
        }

        public void setPassing_year(int passing_year) {
            this.passing_year = passing_year;
        }
    }

    // ================= CERTIFICATION =================
    public static class Certification {
        private String certification_name;
        private String issuing_authority;
        private int year_of_issue;

        public String getCertification_name() {
            return certification_name;
        }

        public void setCertification_name(String certification_name) {
            this.certification_name = certification_name;
        }

        public String getIssuing_authority() {
            return issuing_authority;
        }

        public void setIssuing_authority(String issuing_authority) {
            this.issuing_authority = issuing_authority;
        }

        public int getYear_of_issue() {
            return year_of_issue;
        }

        public void setYear_of_issue(int year_of_issue) {
            this.year_of_issue = year_of_issue;
        }
    }

    // ================= AWARD =================
    public static class Award {
        private String award_name;
        private String purpose;
        private int year_of_award;

        public String getAward_name() {
            return award_name;
        }

        public void setAward_name(String award_name) {
            this.award_name = award_name;
        }

        public String getPurpose() {
            return safe(purpose);
        }

        public void setPurpose(String purpose) {
            this.purpose = purpose;
        }

        public int getYear_of_award() {
            return year_of_award;
        }

        public void setYear_of_award(int year_of_award) {
            this.year_of_award = year_of_award;
        }
    }

    // ================= AVAILABILITY =================
    public static class Availability {
        private List<WeeklySchedule> weekly_schedule;
        private String timezone;
        private int slot_duration;
        private int buffer_time;
        private int advance_booking_window_hours;

        public List<WeeklySchedule> getWeekly_schedule() {
            return weekly_schedule;
        }

        public void setWeekly_schedule(List<WeeklySchedule> weekly_schedule) {
            this.weekly_schedule = weekly_schedule;
        }

        public String getTimezone() {
            return timezone;
        }

        public void setTimezone(String timezone) {
            this.timezone = timezone;
        }

        public int getSlot_duration() {
            return slot_duration;
        }

        public void setSlot_duration(int slot_duration) {
            this.slot_duration = slot_duration;
        }

        public int getBuffer_time() {
            return buffer_time;
        }

        public void setBuffer_time(int buffer_time) {
            this.buffer_time = buffer_time;
        }

        public int getAdvance_booking_window_hours() {
            return advance_booking_window_hours;
        }

        public void setAdvance_booking_window_hours(int advance_booking_window_hours) {
            this.advance_booking_window_hours = advance_booking_window_hours;
        }
    }

    // ================= WEEKLY SCHEDULE =================
    public static class WeeklySchedule {
        private String date;
        private String date_label;
        private int day_of_week;
        private String day_name;
        private boolean is_working_day;
        private List<WorkSlot> work_slots;
        private List<WorkSlot> expert_slots;
        private boolean is_override;

        public String getDate() {
            return date;
        }

        public void setDate(String date) {
            this.date = date;
        }

        public String getDate_label() {
            return date_label;
        }

        public void setDate_label(String date_label) {
            this.date_label = date_label;
        }

        public int getDay_of_week() {
            return day_of_week;
        }

        public void setDay_of_week(int day_of_week) {
            this.day_of_week = day_of_week;
        }

        public String getDay_name() {
            return day_name;
        }

        public void setDay_name(String day_name) {
            this.day_name = day_name;
        }

        public boolean isIs_working_day() {
            return is_working_day;
        }

        public void setIs_working_day(boolean is_working_day) {
            this.is_working_day = is_working_day;
        }

        public List<WorkSlot> getWork_slots() {
            return work_slots;
        }

        public void setWork_slots(List<WorkSlot> work_slots) {
            this.work_slots = work_slots;
        }

        public List<WorkSlot> getExpert_slots() {
            return expert_slots;
        }

        public void setExpert_slots(List<WorkSlot> expert_slots) {
            this.expert_slots = expert_slots;
        }

        public boolean isIs_override() {
            return is_override;
        }

        public void setIs_override(boolean is_override) {
            this.is_override = is_override;
        }
    }

    // ================= WORK SLOT =================
    public static class WorkSlot {
        private String start_time;
        private String end_time;

        public String getStart_time() {
            return start_time;
        }

        public void setStart_time(String start_time) {
            this.start_time = start_time;
        }

        public String getEnd_time() {
            return end_time;
        }

        public void setEnd_time(String end_time) {
            this.end_time = end_time;
        }
    }

    // ================= SUBSCRIPTION =================
    public static class Subscription {
        private String activatedOn;
        private String model;
        private boolean isActive;
        private String startDate;
        private String endDate;
        private String validityDays;
        private String nextBillingDate;
        private boolean canUpgrade;
        private boolean canPayNow;
        private Plan plan;
        private Payment payment;

        public String getActivatedOn() {
            return activatedOn;
        }

        public void setActivatedOn(String activatedOn) {
            this.activatedOn = activatedOn;
        }

        public String getModel() {
            return model;
        }

        public void setModel(String model) {
            this.model = model;
        }

        public boolean isActive() {
            return isActive;
        }

        public void setActive(boolean active) {
            isActive = active;
        }

        public String getStartDate() {
            return startDate;
        }

        public void setStartDate(String startDate) {
            this.startDate = startDate;
        }

        public String getEndDate() {
            return endDate;
        }

        public void setEndDate(String endDate) {
            this.endDate = endDate;
        }

        public String getValidityDays() {
            return validityDays;
        }

        public void setValidityDays(String validityDays) {
            this.validityDays = validityDays;
        }

        public String getNextBillingDate() {
            return nextBillingDate;
        }

        public void setNextBillingDate(String nextBillingDate) {
            this.nextBillingDate = nextBillingDate;
        }

        public boolean isCanUpgrade() {
            return canUpgrade;
        }

        public void setCanUpgrade(boolean canUpgrade) {
            this.canUpgrade = canUpgrade;
        }

        public boolean isCanPayNow() {
            return canPayNow;
        }

        public void setCanPayNow(boolean canPayNow) {
            this.canPayNow = canPayNow;
        }

        public Plan getPlan() {
            return plan;
        }

        public void setPlan(Plan plan) {
            this.plan = plan;
        }

        public Payment getPayment() {
            return payment;
        }

        public void setPayment(Payment payment) {
            this.payment = payment;
        }
    }

    // ================= PLAN =================
    public static class Plan {
        private int amount;
        private String cycle;
        private String name;
        private String currencySymbol;
        private String currencyCode;

        public int getAmount() {
            return amount;
        }

        public void setAmount(int amount) {
            this.amount = amount;
        }

        public String getCycle() {
            return cycle;
        }

        public void setCycle(String cycle) {
            this.cycle = cycle;
        }

        public String getName() {
            return safe(name);
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getCurrencySymbol() {
            return currencySymbol;
        }

        public void setCurrencySymbol(String currencySymbol) {
            this.currencySymbol = currencySymbol;
        }

        public String getCurrencyCode() {
            return currencyCode;
        }

        public void setCurrencyCode(String currencyCode) {
            this.currencyCode = currencyCode;
        }
    }

    // ================= PAYMENT =================
    public static class Payment {
        private String method;
        private String maskedId;

        public String getMethod() {
            return safe(method);
        }

        public void setMethod(String method) {
            this.method = method;
        }

        public String getMaskedId() {
            return safe(maskedId);
        }

        public void setMaskedId(String maskedId) {
            this.maskedId = maskedId;
        }
    }

    // ================= SAFE STRING =================
    private static String safe(String value) {
        return value == null || value.equalsIgnoreCase("null") ? "" : value.trim();
    }
}