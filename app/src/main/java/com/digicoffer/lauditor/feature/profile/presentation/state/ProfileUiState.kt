package com.digicoffer.lauditor.feature.profile.presentation.state

import com.digicoffer.lauditor.FirmProfile.FirmProfileModel

data class AddressState(
    val houseFlatNo: String = "",
    val street: String = "",
    val cityTown: String = "",
    val state: String = "",
    val country: String = "India",
    val zipcode: String = ""
)

data class CourtEnrollmentItemState(
    val id: String = java.util.UUID.randomUUID().toString(),
    val courtType: String = "",
    val state: String = "",
    val city: String = "",
    val courtName: String = "",
    val courtTypeError: String? = null,
    val stateError: String? = null,
    val cityError: String? = null
)

data class EduItemState(
    val id: String = java.util.UUID.randomUUID().toString(),
    val title: String = "",
    val institution: String = "",
    val year: String = ""
)

data class DayAvailabilityState(
    val dayName: String = "",
    val date: String = "",
    val dateLabel: String = "",
    val isWorkingDay: Boolean = false,
    val fromTime: String = "09:00 AM",
    val toTime: String = "05:00 PM",
    val excludedSlots: List<String> = emptyList()
)

data class ProfileEditFormState(
    val firmName: String = "",
    val contactName: String = "",
    val email: String = "",
    val phone: String = "",
    val website: String = "",
    val gender: String = "",
    val dob: String = "",
    val bio: String = "",
    val consultationFee: String = "",
    val currency: String = "INR",
    val emailError: String? = null,
    val phoneError: String? = null
)

data class PracticeDetailsEditState(
    val barOrRegId: String = "",
    val yearsOfExperience: String = "",
    val billingCurrency: String = "IndianRupee(INR)",
    val consultationFee: String = "500",
    val practiceAreas: List<String> = emptyList(),
    val servicesOffered: List<String> = emptyList(),
    val languagesSpoken: List<String> = emptyList(),
    val registeredAddress: AddressState = AddressState(),
    val mailingAddress: AddressState = AddressState(),
    val sameAsRegistered: Boolean = false,
    val regZipError: String? = null,
    val mailZipError: String? = null
)

data class CourtsAndCasesEditState(
    val courtEnrollments: List<CourtEnrollmentItemState> = emptyList(),
    val casesHandled: List<String> = emptyList()
)

data class EducationAndAwardsEditState(
    val educationList: List<EduItemState> = emptyList(),
    val certificationsList: List<EduItemState> = emptyList(),
    val awardsList: List<EduItemState> = emptyList()
)

data class AvailabilityEditState(
    val weeklySchedule: List<DayAvailabilityState> = emptyList()
)

data class PracticePartnerItem(
    val id: String = "",
    val name: String = "",
    val designation: String = "",
    val specialist: String = "",
    val email: String = "",
    val phone: String = ""
)

data class PracticePartnerFormState(
    val id: String? = null,
    val name: String = "",
    val designation: String = "",
    val specialist: String = "",
    val email: String = "",
    val phone: String = "",
    val nameError: String? = null,
    val designationError: String? = null,
    val specialistError: String? = null,
    val emailError: String? = null,
    val phoneError: String? = null
)

data class ProfileUiState(
    val firmProfileModel: FirmProfileModel? = null,
    val isMyProfile: Boolean = false,
    val isPracticePartnerView: Boolean = false,
    val practicePartnersList: List<PracticePartnerItem> = emptyList(),
    val isAddingOrEditingPracticePartner: Boolean = false,
    val practicePartnerForm: PracticePartnerFormState = PracticePartnerFormState(),
    val partnerToDelete: PracticePartnerItem? = null,
    val partnerSearchQuery: String = "",
    val mainTab: String = "profile_info", // "profile_info" vs "subscription"
    val subTab: String = "practice_details", // "practice_details", "education_awards", "courts_cases", "availability"
    val isBannerDismissed: Boolean = false,
    val showPhotoChooser: Boolean = false,
    val showDeletePhotoConfirm: Boolean = false,
    val showDeleteAccountConfirm: Boolean = false,
    val isLoading: Boolean = false,
    val isBioGenerating: Boolean = false,
    val toastMessage: String? = null,
    val alertMessage: String? = null,
    val alertTitle: String? = null,
    val hasBankAccount: Boolean = false,
    val bankAccountModel: com.digicoffer.lauditor.FirmProfile.BankAccountModel? = null,
    val showBankAccountDialog: Boolean = false,

    // Edit Mode State
    val isEditingProfileInfo: Boolean = false,
    val isEditingAdditionalInfo: Boolean = false,
    val editSubTab: String = "practice_details", // "practice_details", "courts_cases", "education_awards", "availability"

    // Edit Form Data
    val profileForm: ProfileEditFormState = ProfileEditFormState(),
    val practiceDetailsForm: PracticeDetailsEditState = PracticeDetailsEditState(),
    val courtsCasesForm: CourtsAndCasesEditState = CourtsAndCasesEditState(),
    val educationAwardsForm: EducationAndAwardsEditState = EducationAndAwardsEditState(),
    val availabilityForm: AvailabilityEditState = AvailabilityEditState(),

    // Metadata Dropdown Options
    val statesList: List<String> = emptyList(),
    val stateCitiesMap: Map<String, List<String>> = emptyMap(),
    val allPracticeAreas: List<String> = emptyList(),
    val allServices: List<String> = emptyList(),
    val suggestedServices: List<String> = emptyList(),
    val searchedServices: List<String> = emptyList(),
    val allCaseTypes: List<String> = emptyList(),
    val courtStatesList: List<String> = emptyList(),
    val courtStatesMap: Map<String, List<String>> = emptyMap(),
    val suggestedCourtTypes: List<String> = emptyList(),
    val allCourtTypes: List<String> = emptyList(),
    val highCourtsList: List<String> = emptyList(),
    val countriesList: List<String> = emptyList(),
    val currencyList: List<String> = listOf("INR", "USD", "EUR", "GBP", "AUD", "CAD", "SGD", "AED"),
    val genderList: List<String> = listOf("Male", "Female", "Other"),

    // Dialog state for slot exclusion
    val activeExcludeDay: DayAvailabilityState? = null
)
