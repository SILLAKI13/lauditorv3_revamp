package com.digicoffer.lauditor.FirmProfile

import org.json.JSONArray

class FirmProfileModel {
    var error: Boolean = false
    var data: Data? = null

    class Data {
        var profile: Profile? = null
    }

    class NextStep {
        var section: String? = null
            get() = safe(field)
        var priority: Int = 0
        var redirect_tab: String? = null
            get() = safe(field)
        var missing_fields: List<String>? = null
    }

    class CourtEnrollment {
        var court_type: String? = null
            get() = safe(field)
        var court_name: String? = null
            get() = safe(field)
        var state: String? = null
            get() = safe(field)
        var city: String? = null
            get() = safe(field)
    }

    class Profile {
        var show_profile_banner: Boolean? = true
        var uid: String? = null
        var firm: Firm? = null
        var name: String? = null
            get() = safe(field)
        var email: String? = null
            get() = safe(field)
        var mobile: String? = null
            get() = safe(field)
        var date_of_birth: String? = null
            get() = safe(field)
        var gender: String? = null
            get() = safe(field)
        var nationality: String? = null
            get() = safe(field)
        var bio_description: String? = null
            get() = safe(field)
        var languages_spoken: JSONArray? = null
        var bar_council_id: String? = null
            get() = safe(field)
        var years_of_experience: Int = 0
        var practice_areas: JSONArray? = null
        var services_offered: JSONArray? = null
        var education: List<Education>? = null
        var certifications: List<Certification>? = null
        var awards: List<Award>? = null
        var consultation_fee: ConsultationFee? = null
        var availability: Availability? = null
        var subscription: Subscription? = null
        var profile_completion: ProfileCompletion? = null
        var profile_completion_percentage: Int = 0
        var profile_pic_hash: String? = null
        var profile_pic_url: String? = null
        var accepted_t_c_date: String? = null
            get() = safe(field)
        var firm_description: String? = null
        var terms: Terms? = null
        var reg_id: String? = null
            get() = safe(field)
        var years_of_incorporation: Int = 0
        var practice_areas_firm: JSONArray? = null
        var cases_handled: List<String>? = null
        var court_enrollments: List<CourtEnrollment>? = null
    }

    class ConsultationFee {
        var amount: String? = null
        var symbol: String? = null
    }

    class Terms {
        var isAccepted: Boolean = false
        var accepted_at: String? = null
        var version: String? = null
    }

    class ProfileCompletion {
        var completion_percentage: Int = 0
        var next_step: NextStep? = null
        var all_incomplete: List<NextStep>? = null
    }

    class Firm {
        var show_profile_banner: Boolean? = true
        var fullname: String? = null
            get() = safe(field)
        var email: String? = null
            get() = safe(field)
        var billing_currency: String? = null
            get() = safe(field)
        var contact_phone: String? = null
            get() = safe(field)
        var contact_person: String? = null
            get() = safe(field)
        var reg_id: String? = null
            get() = safe(field)
        var years_of_incorporation: Int = 0
        var firm_description: String? = null
            get() = safe(field)
        var practice_areas: JSONArray? = null
        var services_offered: JSONArray? = null
        var awards: List<Award>? = null
        var website: String? = null
            get() = safe(field)
        var address: Address? = null
        var profile_pic_url: String? = null
        var profile_completion: ProfileCompletion? = null
        var profile_completion_percentage: Int = 0
        var correspondence_address: Address? = null
    }

    class Address {
        var house_flat_no: String? = null
            get() = safe(field)
        var street: String? = null
            get() = safe(field)
        var city_town: String? = null
            get() = safe(field)
        var state: String? = null
            get() = safe(field)
        var country: String? = null
            get() = safe(field)
        var zipcode: String? = null
            get() = safe(field)
    }

    class Education {
        var degree: String? = null
        var university: String? = null
        var passing_year: Int = 0
    }

    class Certification {
        var certification_name: String? = null
        var issuing_authority: String? = null
        var year_of_issue: Int = 0
    }

    class Award {
        var award_name: String? = null
        var purpose: String? = null
            get() = safe(field)
        var year_of_award: Int = 0
    }

    class Availability {
        var weekly_schedule: List<WeeklySchedule>? = null
        var timezone: String? = null
        var slot_duration: Int = 0
        var buffer_time: Int = 0
        var advance_booking_window_hours: Int = 0
    }

    class WeeklySchedule {
        var date: String? = null
        var date_label: String? = null
        var day_of_week: Int = 0
        var day_name: String? = null
        var isIs_working_day: Boolean = false
        var work_slots: List<WorkSlot>? = null
        var expert_slots: List<WorkSlot>? = null
        var isIs_override: Boolean = false
    }

    class WorkSlot {
        var start_time: String? = null
        var end_time: String? = null
    }

    class Subscription {
        var activatedOn: String? = null
        var model: String? = null
        var isActive: Boolean = false
        var startDate: String? = null
        var endDate: String? = null
        var validityDays: String? = null
        var nextBillingDate: String? = null
        var isCanUpgrade: Boolean = false
        var isCanPayNow: Boolean = false
        var plan: Plan? = null
        var payment: Payment? = null
    }

    class Plan {
        var amount: Int = 0
        var cycle: String? = null
        var name: String? = null
            get() = safe(field)
        var currencySymbol: String? = null
        var currencyCode: String? = null
    }

    class Payment {
        var method: String? = null
            get() = safe(field)
        var maskedId: String? = null
            get() = safe(field)
    }

    companion object {
        private fun safe(value: String?): String {
            return if (value == null || value.equals("null", ignoreCase = true)) "" else value.trim()
        }
    }
}
