package com.digicoffer.lauditor.feature.profile.presentation.state

import java.io.File

sealed interface ProfileUiEvent {
    object LoadProfile : ProfileUiEvent
    data class SetMyProfileMode(val isMyProfile: Boolean) : ProfileUiEvent
    data class SelectMainTab(val tab: String) : ProfileUiEvent
    data class SelectSubTab(val subTab: String) : ProfileUiEvent
    object DismissBanner : ProfileUiEvent
    object DismissDialogs : ProfileUiEvent
    object DismissToast : ProfileUiEvent

    // Photo Management
    object OpenPhotoChooser : ProfileUiEvent
    object DismissPhotoChooser : ProfileUiEvent
    object OpenDeletePhotoConfirm : ProfileUiEvent
    object DismissDeletePhotoConfirm : ProfileUiEvent
    object ConfirmDeletePhoto : ProfileUiEvent
    data class UploadPhoto(val file: File) : ProfileUiEvent

    // View actions
    object CompleteNowClick : ProfileUiEvent
    object DeleteAccountClick : ProfileUiEvent
    object ConfirmDeleteAccount : ProfileUiEvent
    object DismissDeleteAccountConfirm : ProfileUiEvent
    object BankAccountClick : ProfileUiEvent
    object DismissBankAccountDialog : ProfileUiEvent

    // Edit Navigation
    object OpenEditFirmInfo : ProfileUiEvent
    object CancelEditFirmInfo : ProfileUiEvent
    object SaveEditFirmInfo : ProfileUiEvent

    object OpenEditAddInfo : ProfileUiEvent
    object CancelEditAddInfo : ProfileUiEvent
    object SaveEditAddInfo : ProfileUiEvent
    data class SelectEditSubTab(val subTab: String) : ProfileUiEvent

    // Profile Info Form field changes
    data class UpdateFirmName(val value: String) : ProfileUiEvent
    data class UpdateContactName(val value: String) : ProfileUiEvent
    data class UpdateEmail(val value: String) : ProfileUiEvent
    data class UpdatePhone(val value: String) : ProfileUiEvent
    data class UpdateWebsite(val value: String) : ProfileUiEvent
    data class UpdateGender(val value: String) : ProfileUiEvent
    data class UpdateDob(val value: String) : ProfileUiEvent
    data class UpdateBio(val value: String) : ProfileUiEvent
    data class UpdateConsultationFee(val value: String) : ProfileUiEvent
    data class UpdateCurrency(val value: String) : ProfileUiEvent
    object GenerateBioClick : ProfileUiEvent

    // Practice Details Form changes
    data class UpdateBarOrRegId(val value: String) : ProfileUiEvent
    data class UpdateYearsOfExperience(val value: String) : ProfileUiEvent
    data class AddPracticeArea(val area: String) : ProfileUiEvent
    data class RemovePracticeArea(val area: String) : ProfileUiEvent
    data class AddService(val service: String) : ProfileUiEvent
    data class RemoveService(val service: String) : ProfileUiEvent
    data class SearchServices(val query: String) : ProfileUiEvent
    data class AddLanguage(val language: String) : ProfileUiEvent
    data class RemoveLanguage(val language: String) : ProfileUiEvent
    data class UpdateRegisteredAddress(val address: AddressState) : ProfileUiEvent
    data class UpdateMailingAddress(val address: AddressState) : ProfileUiEvent
    data class SetSameAsRegistered(val checked: Boolean) : ProfileUiEvent
    data class FetchCities(val state: String) : ProfileUiEvent

    // Courts & Cases Form changes
    object AddCourtRow : ProfileUiEvent
    data class AddCourtWithSuggestion(val courtType: String) : ProfileUiEvent
    data class RemoveCourtRow(val id: String) : ProfileUiEvent
    data class UpdateCourtRow(val id: String, val courtType: String, val state: String, val city: String, val courtName: String) : ProfileUiEvent
    data class AddCaseType(val caseType: String) : ProfileUiEvent
    data class RemoveCaseType(val caseType: String) : ProfileUiEvent

    // Education & Awards Form changes
    object AddEducationRow : ProfileUiEvent
    data class RemoveEducationRow(val id: String) : ProfileUiEvent
    data class UpdateEducationRow(val id: String, val degree: String, val university: String, val year: String) : ProfileUiEvent

    object AddCertificationRow : ProfileUiEvent
    data class RemoveCertificationRow(val id: String) : ProfileUiEvent
    data class UpdateCertificationRow(val id: String, val name: String, val authority: String, val year: String) : ProfileUiEvent

    object AddAwardRow : ProfileUiEvent
    data class RemoveAwardRow(val id: String) : ProfileUiEvent
    data class UpdateAwardRow(val id: String, val name: String, val purpose: String, val year: String) : ProfileUiEvent

    // Availability Form changes
    data class ToggleWorkingDay(val dayName: String, val isWorking: Boolean) : ProfileUiEvent
    data class UpdateWorkingHours(val dayName: String, val fromTime: String, val toTime: String) : ProfileUiEvent
    data class OpenExcludeSlotsDialog(val day: DayAvailabilityState) : ProfileUiEvent
    object DismissExcludeSlotsDialog : ProfileUiEvent
    data class SaveExcludedSlots(val dayName: String, val excludedSlots: List<String>) : ProfileUiEvent

    // Practice Partner actions
    object LoadPracticePartners : ProfileUiEvent
    data class SetPracticePartnerMode(val isPp: Boolean) : ProfileUiEvent
    object OpenAddPracticePartner : ProfileUiEvent
    data class OpenEditPracticePartner(val item: PracticePartnerItem) : ProfileUiEvent
    object CancelAddEditPracticePartner : ProfileUiEvent
    object SavePracticePartner : ProfileUiEvent
    data class ConfirmDeletePracticePartner(val item: PracticePartnerItem) : ProfileUiEvent
    object DismissDeletePracticePartnerConfirm : ProfileUiEvent
    object DeletePracticePartnerConfirmed : ProfileUiEvent
    data class SearchPracticePartner(val query: String) : ProfileUiEvent
    data class UpdatePartnerName(val value: String) : ProfileUiEvent
    data class UpdatePartnerDesignation(val value: String) : ProfileUiEvent
    data class UpdatePartnerSpecialist(val value: String) : ProfileUiEvent
    data class UpdatePartnerEmail(val value: String) : ProfileUiEvent
    data class UpdatePartnerPhone(val value: String) : ProfileUiEvent
}
