package com.digicoffer.lauditor.feature.profile.presentation.viewmodel

import android.app.Application
import android.text.TextUtils
import android.util.Patterns
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.digicoffer.lauditor.CommonFiles.CacheUtils.AppImageCache
import com.digicoffer.lauditor.CommonFiles.GlobalFiles.Constants
import com.digicoffer.lauditor.FirmProfile.FirmProfileModel
import com.digicoffer.lauditor.Webservice.CommonApiHelper.WebServiceHelper
import com.digicoffer.lauditor.feature.profile.data.repository.ProfileRepository
import com.digicoffer.lauditor.feature.profile.presentation.state.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.json.JSONArray
import org.json.JSONObject
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

class ProfileViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = ProfileRepository(application.applicationContext)

    private val _uiState = MutableStateFlow(
        ProfileUiState(
            isMyProfile = Constants.isMyProfileClicked,
            mainTab = "profile_info",
            subTab = "practice_details"
        )
    )
    val uiState: StateFlow<ProfileUiState> = _uiState.asStateFlow()

    private val originalValues = mutableMapOf<String, Any>()
    private var originalPracticeAreas: JSONArray? = null
    private var originalLanguages: JSONArray? = null
    private var originalServices: JSONArray? = null
    private var originalEducation: JSONArray? = null
    private var originalCertifications: JSONArray? = null
    private var originalAwards: JSONArray? = null
    private var originalCasesHandled: JSONArray? = null
    private var originalWeeklySchedule: List<FirmProfileModel.WeeklySchedule>? = null

    init {
        loadProfile()
        loadMetadata()
    }

    fun onEvent(event: ProfileUiEvent) {
        when (event) {
            is ProfileUiEvent.LoadProfile -> loadProfile()
            is ProfileUiEvent.SetMyProfileMode -> {
                Constants.isMyProfileClicked = event.isMyProfile
                _uiState.update {
                    val newSubTab = if (!event.isMyProfile && it.subTab != "practice_details" && it.subTab != "education_awards") {
                        "practice_details"
                    } else {
                        it.subTab
                    }
                    it.copy(
                        isMyProfile = event.isMyProfile,
                        subTab = newSubTab,
                        mainTab = if (event.isMyProfile) "profile_info" else it.mainTab,
                        isEditingProfileInfo = false,
                        isEditingAdditionalInfo = false,
                        editSubTab = "practice_details"
                    )
                }
                loadProfile()
            }
            is ProfileUiEvent.SelectMainTab -> {
                _uiState.update { it.copy(mainTab = event.tab) }
                if (event.tab == "subscription") {
                    fetchBankAccount()
                }
            }
            is ProfileUiEvent.SelectSubTab -> {
                _uiState.update { it.copy(subTab = event.subTab) }
            }
            is ProfileUiEvent.DismissBanner -> {
                _uiState.update { it.copy(isBannerDismissed = true) }
            }
            is ProfileUiEvent.OpenPhotoChooser -> {
                _uiState.update { it.copy(showPhotoChooser = true) }
            }
            is ProfileUiEvent.DismissPhotoChooser -> {
                _uiState.update { it.copy(showPhotoChooser = false) }
            }
            is ProfileUiEvent.OpenDeletePhotoConfirm -> {
                _uiState.update { it.copy(showDeletePhotoConfirm = true) }
            }
            is ProfileUiEvent.DismissDeletePhotoConfirm -> {
                _uiState.update { it.copy(showDeletePhotoConfirm = false) }
            }
            is ProfileUiEvent.ConfirmDeletePhoto -> {
                deletePhoto()
            }
            is ProfileUiEvent.UploadPhoto -> {
                uploadPhoto(event.file)
            }
            is ProfileUiEvent.DismissDialogs -> {
                _uiState.update { it.copy(alertMessage = null, alertTitle = null) }
            }
            is ProfileUiEvent.DismissToast -> {
                _uiState.update { it.copy(toastMessage = null) }
            }
            is ProfileUiEvent.CompleteNowClick -> {
                openMissingFieldsDirectly()
            }
            is ProfileUiEvent.DeleteAccountClick -> {
                _uiState.update { it.copy(showDeleteAccountConfirm = true) }
            }
            is ProfileUiEvent.DismissDeleteAccountConfirm -> {
                _uiState.update { it.copy(showDeleteAccountConfirm = false) }
            }
            is ProfileUiEvent.ConfirmDeleteAccount -> {
                deleteAccount()
            }
            is ProfileUiEvent.BankAccountClick -> {
                _uiState.update { it.copy(showBankAccountDialog = true) }
            }
            is ProfileUiEvent.DismissBankAccountDialog -> {
                _uiState.update { it.copy(showBankAccountDialog = false) }
            }

            // Edit Navigation
            is ProfileUiEvent.OpenEditFirmInfo -> {
                populateProfileEditForm()
                _uiState.update { it.copy(isEditingProfileInfo = true, isEditingAdditionalInfo = false) }
            }
            is ProfileUiEvent.CancelEditFirmInfo -> {
                _uiState.update { it.copy(isEditingProfileInfo = false) }
            }
            is ProfileUiEvent.SaveEditFirmInfo -> {
                saveProfileInfo()
            }

            is ProfileUiEvent.OpenEditAddInfo -> {
                populateAdditionalInfoForm()
                _uiState.update {
                    it.copy(
                        isEditingAdditionalInfo = true,
                        isEditingProfileInfo = false,
                        editSubTab = if (it.isMyProfile) "practice_details" else "practice_details"
                    )
                }
            }
            is ProfileUiEvent.CancelEditAddInfo -> {
                _uiState.update { it.copy(isEditingAdditionalInfo = false) }
            }
            is ProfileUiEvent.SaveEditAddInfo -> {
                saveAdditionalInfo()
            }
            is ProfileUiEvent.SelectEditSubTab -> {
                _uiState.update { it.copy(editSubTab = event.subTab) }
                if (event.subTab == "courts_cases") {
                    fetchCourtSuggestionsForCurrentPractice()
                }
            }

            // Profile Info Form field changes
            is ProfileUiEvent.UpdateFirmName -> {
                _uiState.update { it.copy(profileForm = it.profileForm.copy(firmName = event.value)) }
            }
            is ProfileUiEvent.UpdateContactName -> {
                _uiState.update { it.copy(profileForm = it.profileForm.copy(contactName = event.value)) }
            }
            is ProfileUiEvent.UpdateEmail -> {
                val error = if (event.value.isNotEmpty() && !Patterns.EMAIL_ADDRESS.matcher(event.value).matches()) {
                    "Enter a valid email address"
                } else null
                _uiState.update { it.copy(profileForm = it.profileForm.copy(email = event.value, emailError = error)) }
            }
            is ProfileUiEvent.UpdatePhone -> {
                val digitsOnly = event.value.filter { it.isDigit() }.take(10)
                val error = if (digitsOnly.isNotEmpty() && digitsOnly.length < 10) {
                    "Please enter the 10 digit mobile number"
                } else null
                _uiState.update { it.copy(profileForm = it.profileForm.copy(phone = digitsOnly, phoneError = error)) }
            }
            is ProfileUiEvent.UpdateWebsite -> {
                _uiState.update { it.copy(profileForm = it.profileForm.copy(website = event.value)) }
            }
            is ProfileUiEvent.UpdateGender -> {
                _uiState.update { it.copy(profileForm = it.profileForm.copy(gender = event.value)) }
            }
            is ProfileUiEvent.UpdateDob -> {
                _uiState.update { it.copy(profileForm = it.profileForm.copy(dob = event.value)) }
            }
            is ProfileUiEvent.UpdateBio -> {
                _uiState.update { it.copy(profileForm = it.profileForm.copy(bio = event.value)) }
            }
            is ProfileUiEvent.UpdateConsultationFee -> {
                val feeDigits = event.value.filter { it.isDigit() }
                val clampedFee = ((feeDigits.toIntOrNull() ?: 500).coerceAtLeast(500)).toString()
                _uiState.update { 
                    it.copy(
                        profileForm = it.profileForm.copy(consultationFee = clampedFee),
                        practiceDetailsForm = it.practiceDetailsForm.copy(consultationFee = clampedFee)
                    ) 
                }
            }
            is ProfileUiEvent.UpdateCurrency -> {
                _uiState.update { 
                    it.copy(
                        profileForm = it.profileForm.copy(currency = event.value),
                        practiceDetailsForm = it.practiceDetailsForm.copy(billingCurrency = event.value)
                    ) 
                }
            }
            is ProfileUiEvent.GenerateBioClick -> {
                generateBio()
            }

            // Practice Details Form changes
            is ProfileUiEvent.UpdateBarOrRegId -> {
                _uiState.update { it.copy(practiceDetailsForm = it.practiceDetailsForm.copy(barOrRegId = event.value)) }
            }
            is ProfileUiEvent.UpdateYearsOfExperience -> {
                val digitsOnly = event.value.filter { it.isDigit() }.take(3)
                _uiState.update { it.copy(practiceDetailsForm = it.practiceDetailsForm.copy(yearsOfExperience = digitsOnly)) }
            }
            is ProfileUiEvent.AddPracticeArea -> {
                val current = _uiState.value.practiceDetailsForm.practiceAreas
                if (!current.contains(event.area)) {
                    val updated = current + event.area
                    _uiState.update { it.copy(practiceDetailsForm = it.practiceDetailsForm.copy(practiceAreas = updated)) }
                    fetchSuggestedServices(updated)
                    fetchCourtSuggestions(updated)
                }
            }
            is ProfileUiEvent.RemovePracticeArea -> {
                val updated = _uiState.value.practiceDetailsForm.practiceAreas - event.area
                _uiState.update { it.copy(practiceDetailsForm = it.practiceDetailsForm.copy(practiceAreas = updated)) }
                fetchSuggestedServices(updated)
                fetchCourtSuggestions(updated)
            }
            is ProfileUiEvent.AddService -> {
                val current = _uiState.value.practiceDetailsForm.servicesOffered
                if (!current.contains(event.service)) {
                    _uiState.update { it.copy(practiceDetailsForm = it.practiceDetailsForm.copy(servicesOffered = current + event.service)) }
                }
            }
            is ProfileUiEvent.RemoveService -> {
                val updated = _uiState.value.practiceDetailsForm.servicesOffered - event.service
                _uiState.update { it.copy(practiceDetailsForm = it.practiceDetailsForm.copy(servicesOffered = updated)) }
            }
            is ProfileUiEvent.SearchServices -> {
                searchServices(event.query)
            }
            is ProfileUiEvent.AddLanguage -> {
                val current = _uiState.value.practiceDetailsForm.languagesSpoken
                if (!current.contains(event.language)) {
                    _uiState.update { it.copy(practiceDetailsForm = it.practiceDetailsForm.copy(languagesSpoken = current + event.language)) }
                }
            }
            is ProfileUiEvent.RemoveLanguage -> {
                val updated = _uiState.value.practiceDetailsForm.languagesSpoken - event.language
                _uiState.update { it.copy(practiceDetailsForm = it.practiceDetailsForm.copy(languagesSpoken = updated)) }
            }
            is ProfileUiEvent.FetchCities -> {
                fetchCitiesForState(event.state)
            }
            is ProfileUiEvent.UpdateRegisteredAddress -> {
                val zip = event.address.zipcode.filter { it.isDigit() }.take(6)
                val cleanAddress = event.address.copy(zipcode = zip)
                val zipError = if (zip.isNotEmpty() && zip.length != 6) "Invalid ZIP code. Must be exactly 6 digits." else null
                
                val currentSameAs = _uiState.value.practiceDetailsForm.sameAsRegistered
                val newMailing = if (currentSameAs) cleanAddress else _uiState.value.practiceDetailsForm.mailingAddress

                val oldState = _uiState.value.practiceDetailsForm.registeredAddress.state
                if (cleanAddress.state.isNotBlank() && cleanAddress.state != oldState) {
                    fetchCitiesForState(cleanAddress.state)
                }

                _uiState.update {
                    it.copy(
                        practiceDetailsForm = it.practiceDetailsForm.copy(
                            registeredAddress = cleanAddress,
                            mailingAddress = newMailing,
                            regZipError = zipError
                        )
                    )
                }
            }
            is ProfileUiEvent.UpdateMailingAddress -> {
                val zip = event.address.zipcode.filter { it.isDigit() }.take(6)
                val cleanAddress = event.address.copy(zipcode = zip)
                val zipError = if (zip.isNotEmpty() && zip.length != 6) "Invalid ZIP code. Must be exactly 6 digits." else null

                val oldState = _uiState.value.practiceDetailsForm.mailingAddress.state
                if (cleanAddress.state.isNotBlank() && cleanAddress.state != oldState) {
                    fetchCitiesForState(cleanAddress.state)
                }

                _uiState.update {
                    it.copy(
                        practiceDetailsForm = it.practiceDetailsForm.copy(
                            mailingAddress = cleanAddress,
                            mailZipError = zipError
                        )
                    )
                }
            }
            is ProfileUiEvent.SetSameAsRegistered -> {
                val regAddr = _uiState.value.practiceDetailsForm.registeredAddress
                _uiState.update {
                    it.copy(
                        practiceDetailsForm = it.practiceDetailsForm.copy(
                            sameAsRegistered = event.checked,
                            mailingAddress = if (event.checked) regAddr else it.practiceDetailsForm.mailingAddress
                        )
                    )
                }
            }

            // Courts & Cases Form changes
            is ProfileUiEvent.AddCourtRow -> {
                val current = _uiState.value.courtsCasesForm.courtEnrollments
                val newRow = CourtEnrollmentItemState()
                _uiState.update {
                    it.copy(courtsCasesForm = it.courtsCasesForm.copy(courtEnrollments = current + newRow))
                }
            }
            is ProfileUiEvent.AddCourtWithSuggestion -> {
                val current = _uiState.value.courtsCasesForm.courtEnrollments
                val isSupreme = event.courtType.equals("Supreme Court", ignoreCase = true)
                val defState = if (isSupreme) "Delhi" else (_uiState.value.courtStatesList.firstOrNull() ?: "")
                val defCity = if (isSupreme) "New Delhi" else (_uiState.value.courtStatesMap[defState]?.firstOrNull() ?: "")
                val newRow = CourtEnrollmentItemState(
                    courtType = event.courtType,
                    state = defState,
                    city = defCity
                )
                _uiState.update {
                    it.copy(courtsCasesForm = it.courtsCasesForm.copy(courtEnrollments = current + newRow))
                }
            }
            is ProfileUiEvent.RemoveCourtRow -> {
                val updated = _uiState.value.courtsCasesForm.courtEnrollments.filterNot { it.id == event.id }
                _uiState.update {
                    it.copy(courtsCasesForm = it.courtsCasesForm.copy(courtEnrollments = updated))
                }
            }
            is ProfileUiEvent.UpdateCourtRow -> {
                val isSupreme = event.courtType.equals("Supreme Court", ignoreCase = true)
                val finalState = if (event.courtType.isBlank()) {
                    ""
                } else if (isSupreme) {
                    "Delhi"
                } else if (event.state.isBlank()) {
                    _uiState.value.courtStatesList.firstOrNull() ?: ""
                } else {
                    event.state
                }

                val finalCity = if (event.courtType.isBlank()) {
                    ""
                } else if (isSupreme) {
                    "New Delhi"
                } else if (event.city.isBlank() && finalState.isNotBlank()) {
                    _uiState.value.courtStatesMap[finalState]?.firstOrNull() ?: ""
                } else {
                    event.city
                }

                val updated = _uiState.value.courtsCasesForm.courtEnrollments.map { row ->
                    if (row.id == event.id) {
                        row.copy(
                            courtType = event.courtType,
                            state = finalState,
                            city = finalCity,
                            courtName = event.courtName,
                            courtTypeError = null,
                            stateError = null,
                            cityError = null
                        )
                    } else row
                }
                _uiState.update {
                    it.copy(courtsCasesForm = it.courtsCasesForm.copy(courtEnrollments = updated))
                }
            }
            is ProfileUiEvent.AddCaseType -> {
                val current = _uiState.value.courtsCasesForm.casesHandled
                if (!current.contains(event.caseType)) {
                    _uiState.update { it.copy(courtsCasesForm = it.courtsCasesForm.copy(casesHandled = current + event.caseType)) }
                }
            }
            is ProfileUiEvent.RemoveCaseType -> {
                val updated = _uiState.value.courtsCasesForm.casesHandled - event.caseType
                _uiState.update { it.copy(courtsCasesForm = it.courtsCasesForm.copy(casesHandled = updated)) }
            }

            // Education & Awards Form changes
            is ProfileUiEvent.AddEducationRow -> {
                val current = _uiState.value.educationAwardsForm.educationList
                _uiState.update {
                    it.copy(educationAwardsForm = it.educationAwardsForm.copy(educationList = current + EduItemState()))
                }
            }
            is ProfileUiEvent.RemoveEducationRow -> {
                val updated = _uiState.value.educationAwardsForm.educationList.filterNot { it.id == event.id }
                _uiState.update {
                    it.copy(educationAwardsForm = it.educationAwardsForm.copy(educationList = updated))
                }
            }
            is ProfileUiEvent.UpdateEducationRow -> {
                val updated = _uiState.value.educationAwardsForm.educationList.map {
                    if (it.id == event.id) it.copy(title = event.degree, institution = event.university, year = event.year.filter { c -> c.isDigit() }.take(4))
                    else it
                }
                _uiState.update {
                    it.copy(educationAwardsForm = it.educationAwardsForm.copy(educationList = updated))
                }
            }

            is ProfileUiEvent.AddCertificationRow -> {
                val current = _uiState.value.educationAwardsForm.certificationsList
                _uiState.update {
                    it.copy(educationAwardsForm = it.educationAwardsForm.copy(certificationsList = current + EduItemState()))
                }
            }
            is ProfileUiEvent.RemoveCertificationRow -> {
                val updated = _uiState.value.educationAwardsForm.certificationsList.filterNot { it.id == event.id }
                _uiState.update {
                    it.copy(educationAwardsForm = it.educationAwardsForm.copy(certificationsList = updated))
                }
            }
            is ProfileUiEvent.UpdateCertificationRow -> {
                val updated = _uiState.value.educationAwardsForm.certificationsList.map {
                    if (it.id == event.id) it.copy(title = event.name, institution = event.authority, year = event.year.filter { c -> c.isDigit() }.take(4))
                    else it
                }
                _uiState.update {
                    it.copy(educationAwardsForm = it.educationAwardsForm.copy(certificationsList = updated))
                }
            }

            is ProfileUiEvent.AddAwardRow -> {
                val current = _uiState.value.educationAwardsForm.awardsList
                _uiState.update {
                    it.copy(educationAwardsForm = it.educationAwardsForm.copy(awardsList = current + EduItemState()))
                }
            }
            is ProfileUiEvent.RemoveAwardRow -> {
                val updated = _uiState.value.educationAwardsForm.awardsList.filterNot { it.id == event.id }
                _uiState.update {
                    it.copy(educationAwardsForm = it.educationAwardsForm.copy(awardsList = updated))
                }
            }
            is ProfileUiEvent.UpdateAwardRow -> {
                val updated = _uiState.value.educationAwardsForm.awardsList.map {
                    if (it.id == event.id) it.copy(title = event.name, institution = event.purpose, year = event.year.filter { c -> c.isDigit() }.take(4))
                    else it
                }
                _uiState.update {
                    it.copy(educationAwardsForm = it.educationAwardsForm.copy(awardsList = updated))
                }
            }

            // Availability Form changes
            is ProfileUiEvent.ToggleWorkingDay -> {
                val updated = _uiState.value.availabilityForm.weeklySchedule.map {
                    if (it.dayName.equals(event.dayName, ignoreCase = true)) {
                        it.copy(isWorkingDay = event.isWorking)
                    } else it
                }
                _uiState.update {
                    it.copy(availabilityForm = it.availabilityForm.copy(weeklySchedule = updated))
                }
            }
            is ProfileUiEvent.UpdateWorkingHours -> {
                val updated = _uiState.value.availabilityForm.weeklySchedule.map {
                    if (it.dayName.equals(event.dayName, ignoreCase = true)) {
                        it.copy(fromTime = event.fromTime, toTime = event.toTime)
                    } else it
                }
                _uiState.update {
                    it.copy(availabilityForm = it.availabilityForm.copy(weeklySchedule = updated))
                }
            }
            is ProfileUiEvent.OpenExcludeSlotsDialog -> {
                _uiState.update { it.copy(activeExcludeDay = event.day) }
            }
            is ProfileUiEvent.DismissExcludeSlotsDialog -> {
                _uiState.update { it.copy(activeExcludeDay = null) }
            }
            is ProfileUiEvent.SaveExcludedSlots -> {
                val updated = _uiState.value.availabilityForm.weeklySchedule.map {
                    if (it.dayName.equals(event.dayName, ignoreCase = true)) {
                        it.copy(excludedSlots = event.excludedSlots)
                    } else it
                }
                _uiState.update {
                    it.copy(
                        availabilityForm = it.availabilityForm.copy(weeklySchedule = updated),
                        activeExcludeDay = null
                    )
                }
            }

            // Practice Partner events
            is ProfileUiEvent.SetPracticePartnerMode -> {
                _uiState.update { it.copy(isPracticePartnerView = event.isPp) }
                if (event.isPp) {
                    loadPracticePartners()
                }
            }
            is ProfileUiEvent.LoadPracticePartners -> {
                loadPracticePartners()
            }
            is ProfileUiEvent.OpenAddPracticePartner -> {
                _uiState.update {
                    it.copy(
                        isAddingOrEditingPracticePartner = true,
                        practicePartnerForm = PracticePartnerFormState()
                    )
                }
            }
            is ProfileUiEvent.OpenEditPracticePartner -> {
                _uiState.update {
                    it.copy(
                        isAddingOrEditingPracticePartner = true,
                        practicePartnerForm = PracticePartnerFormState(
                            id = event.item.id,
                            name = event.item.name,
                            designation = event.item.designation,
                            specialist = event.item.specialist,
                            email = event.item.email,
                            phone = event.item.phone
                        )
                    )
                }
            }
            is ProfileUiEvent.CancelAddEditPracticePartner -> {
                _uiState.update {
                    it.copy(
                        isAddingOrEditingPracticePartner = false,
                        practicePartnerForm = PracticePartnerFormState()
                    )
                }
            }
            is ProfileUiEvent.SavePracticePartner -> {
                savePracticePartner()
            }
            is ProfileUiEvent.ConfirmDeletePracticePartner -> {
                _uiState.update { it.copy(partnerToDelete = event.item) }
            }
            is ProfileUiEvent.DismissDeletePracticePartnerConfirm -> {
                _uiState.update { it.copy(partnerToDelete = null) }
            }
            is ProfileUiEvent.DeletePracticePartnerConfirmed -> {
                deletePracticePartner()
            }
            is ProfileUiEvent.SearchPracticePartner -> {
                _uiState.update { it.copy(partnerSearchQuery = event.query) }
            }
            is ProfileUiEvent.UpdatePartnerName -> {
                _uiState.update { it.copy(practicePartnerForm = it.practicePartnerForm.copy(name = event.value, nameError = null)) }
            }
            is ProfileUiEvent.UpdatePartnerDesignation -> {
                _uiState.update { it.copy(practicePartnerForm = it.practicePartnerForm.copy(designation = event.value, designationError = null)) }
            }
            is ProfileUiEvent.UpdatePartnerSpecialist -> {
                _uiState.update { it.copy(practicePartnerForm = it.practicePartnerForm.copy(specialist = event.value, specialistError = null)) }
            }
            is ProfileUiEvent.UpdatePartnerEmail -> {
                val error = if (event.value.isNotEmpty() && !Patterns.EMAIL_ADDRESS.matcher(event.value).matches()) "Enter a valid email address" else null
                _uiState.update { it.copy(practicePartnerForm = it.practicePartnerForm.copy(email = event.value, emailError = error)) }
            }
            is ProfileUiEvent.UpdatePartnerPhone -> {
                val digits = event.value.filter { it.isDigit() }.take(10)
                val error = if (digits.isNotEmpty() && digits.length < 10) "Please enter 10 digit mobile number" else null
                _uiState.update { it.copy(practicePartnerForm = it.practicePartnerForm.copy(phone = digits, phoneError = error)) }
            }
        }
    }

    fun loadPracticePartners() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            val result = repository.fetchPracticePartners()
            _uiState.update { it.copy(isLoading = false) }
            if (result.result == WebServiceHelper.ServiceCallStatus.Success) {
                try {
                    val root = JSONObject(result.responseContent ?: "{}")
                    val isError = root.optBoolean("error", false)
                    if (!isError) {
                        val arr = root.optJSONArray("data")
                        val list = mutableListOf<PracticePartnerItem>()
                        if (arr != null) {
                            for (i in 0 until arr.length()) {
                                val obj = arr.optJSONObject(i) ?: continue
                                fun clean(key: String): String {
                                    if (obj.isNull(key)) return ""
                                    val v = obj.optString(key, "").trim()
                                    return if (v.equals("null", ignoreCase = true)) "" else v
                                }
                                list.add(
                                    PracticePartnerItem(
                                        id = clean("id"),
                                        name = clean("name"),
                                        designation = clean("designation"),
                                        specialist = clean("practice"),
                                        email = clean("email"),
                                        phone = clean("phone")
                                    )
                                )
                            }
                        }
                        _uiState.update { it.copy(practicePartnersList = list) }
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }
    }

    private fun savePracticePartner() {
        val form = _uiState.value.practicePartnerForm
        var hasError = false
        var nameErr: String? = null
        var desgErr: String? = null
        var specErr: String? = null
        var emailErr: String? = null
        var phoneErr: String? = null

        if (form.name.trim().isEmpty()) {
            nameErr = "Please enter Name"
            hasError = true
        }
        if (form.designation.trim().isEmpty()) {
            desgErr = "Please enter Designation"
            hasError = true
        }
        if (form.specialist.trim().isEmpty()) {
            specErr = "Please enter Specialist"
            hasError = true
        }
        if (form.email.trim().isEmpty() || !Patterns.EMAIL_ADDRESS.matcher(form.email.trim()).matches()) {
            emailErr = "Please enter a valid Email"
            hasError = true
        }
        if (form.phone.trim().isEmpty() || form.phone.trim().length < 10) {
            phoneErr = "Please enter a valid 10-digit Phone number"
            hasError = true
        }

        if (hasError) {
            _uiState.update {
                it.copy(
                    practicePartnerForm = it.practicePartnerForm.copy(
                        nameError = nameErr,
                        designationError = desgErr,
                        specialistError = specErr,
                        emailError = emailErr,
                        phoneError = phoneErr
                    )
                )
            }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            val payload = JSONObject().apply {
                put("name", form.name.trim())
                put("designation", form.designation.trim())
                put("practice", form.specialist.trim())
                put("email", form.email.trim())
                put("phone", form.phone.trim())
            }

            val result = if (form.id != null) {
                repository.updatePracticePartner(form.id, payload)
            } else {
                repository.addPracticePartner(payload)
            }
            _uiState.update { it.copy(isLoading = false) }

            if (result.result == WebServiceHelper.ServiceCallStatus.Success) {
                val successMsg = if (form.id != null) "Practice Partner updated successfully" else "Practice Partner added successfully"
                _uiState.update {
                    it.copy(
                        isAddingOrEditingPracticePartner = false,
                        practicePartnerForm = PracticePartnerFormState(),
                        toastMessage = successMsg
                    )
                }
                loadPracticePartners()
            } else {
                val errMsg = try {
                    val json = JSONObject(result.responseContent ?: "{}")
                    json.optString("msg", json.optString("message", "Failed to save Practice Partner"))
                } catch (e: Exception) {
                    "Failed to save Practice Partner"
                }
                _uiState.update { it.copy(alertTitle = "Alert", alertMessage = errMsg) }
            }
        }
    }

    private fun deletePracticePartner() {
        val partner = _uiState.value.partnerToDelete ?: return
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, partnerToDelete = null) }
            val result = repository.deletePracticePartner(partner.id)
            _uiState.update { it.copy(isLoading = false) }

            if (result.result == WebServiceHelper.ServiceCallStatus.Success) {
                _uiState.update { it.copy(toastMessage = "Practice Partner deleted successfully") }
                loadPracticePartners()
            } else {
                val errMsg = try {
                    val json = JSONObject(result.responseContent ?: "{}")
                    json.optString("msg", json.optString("message", "Failed to delete Practice Partner"))
                } catch (e: Exception) {
                    "Failed to delete Practice Partner"
                }
                _uiState.update { it.copy(alertTitle = "Alert", alertMessage = errMsg) }
            }
        }
    }

    private fun loadMetadata() {
        viewModelScope.launch {
            // Load States via v3/states
            try {
                val statesResult = repository.fetchStates()
                if (statesResult.result == WebServiceHelper.ServiceCallStatus.Success) {
                    val root = JSONObject(statesResult.responseContent ?: "{}")
                    val data = root.optJSONObject("data")
                    val statesArray = data?.optJSONArray("states") ?: root.optJSONArray("states") ?: root.optJSONArray("data")
                    if (statesArray != null) {
                        val stateList = mutableListOf<String>()
                        for (i in 0 until statesArray.length()) {
                            val st = statesArray.optString(i).trim()
                            if (st.isNotEmpty()) stateList.add(st)
                        }
                        stateList.sort()
                        if (stateList.isNotEmpty()) {
                            _uiState.update { it.copy(statesList = stateList, courtStatesList = if (it.courtStatesList.isEmpty()) stateList else it.courtStatesList) }
                        }
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }

            // Load Court States
            try {
                val statesResult = repository.fetchCourtStates()
                if (statesResult.result == WebServiceHelper.ServiceCallStatus.Success) {
                    val root = JSONObject(statesResult.responseContent ?: "{}")
                    val data = root.optJSONObject("data")
                    val stateList = mutableListOf<String>()
                    val stateMap = mutableMapOf<String, List<String>>()

                    val statesArray = data?.optJSONArray("states")
                        ?: root.optJSONArray("states")
                        ?: (if (root.optJSONArray("data") != null) root.optJSONArray("data") else null)

                    if (statesArray != null) {
                        for (i in 0 until statesArray.length()) {
                            val stObj = statesArray.optJSONObject(i)
                            if (stObj != null) {
                                val stName = stObj.optString("state", stObj.optString("name", "")).trim()
                                val citiesArr = stObj.optJSONArray("cities")
                                val citiesList = mutableListOf<String>()
                                if (citiesArr != null) {
                                    for (j in 0 until citiesArr.length()) {
                                        val c = citiesArr.optString(j, "").trim()
                                        if (c.isNotEmpty()) citiesList.add(c)
                                    }
                                }
                                if (stName.isNotEmpty()) {
                                    stateList.add(stName)
                                    stateMap[stName] = citiesList
                                }
                            } else {
                                val stName = statesArray.optString(i, "").trim()
                                if (stName.isNotEmpty()) {
                                    stateList.add(stName)
                                    stateMap[stName] = emptyList()
                                }
                            }
                        }
                    } else if (data != null) {
                        val keys = data.keys()
                        while (keys.hasNext()) {
                            val st = keys.next()
                            val citiesArr = data.optJSONArray(st)
                            val citiesList = mutableListOf<String>()
                            if (citiesArr != null) {
                                for (i in 0 until citiesArr.length()) {
                                    val cObj = citiesArr.optJSONObject(i)
                                    if (cObj != null) {
                                        val cName = cObj.optString("city", cObj.optString("name", "")).trim()
                                        if (cName.isNotEmpty()) citiesList.add(cName)
                                    } else {
                                        val c = citiesArr.optString(i, "").trim()
                                        if (c.isNotEmpty()) citiesList.add(c)
                                    }
                                }
                            }
                            stateList.add(st)
                            stateMap[st] = citiesList
                        }
                    }
                    stateList.sort()
                    _uiState.update { it.copy(courtStatesList = stateList, courtStatesMap = stateMap) }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }

            // Load Case Types
            try {
                val caseResult = repository.fetchCaseTypes()
                if (caseResult.result == WebServiceHelper.ServiceCallStatus.Success) {
                    val root = JSONObject(caseResult.responseContent ?: "{}")
                    val arr = root.optJSONArray("data")
                    if (arr != null) {
                        val list = mutableListOf<String>()
                        for (i in 0 until arr.length()) {
                            list.add(arr.optString(i))
                        }
                        _uiState.update { it.copy(allCaseTypes = list, allPracticeAreas = list) }
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }

            // Load Countries
            try {
                val countriesResult = repository.fetchCountries()
                if (countriesResult.result == WebServiceHelper.ServiceCallStatus.Success) {
                    val root = JSONObject(countriesResult.responseContent ?: "{}")
                    val arr = root.optJSONArray("data") ?: root.optJSONArray("countries")
                    if (arr != null) {
                        val list = mutableListOf<String>()
                        for (i in 0 until arr.length()) {
                            val cObj = arr.optJSONObject(i)
                            if (cObj != null) {
                                val name = cObj.optString("name", cObj.optString("country_name", ""))
                                if (name.isNotEmpty()) list.add(name)
                            } else {
                                val innerArr = arr.optJSONArray(i)
                                if (innerArr != null && innerArr.length() > 1) {
                                    val name = innerArr.optString(1)
                                    if (name.isNotEmpty()) list.add(name)
                                } else {
                                    val name = arr.optString(i)
                                    if (name.isNotEmpty()) list.add(name)
                                }
                            }
                        }
                        if (list.isNotEmpty()) {
                            _uiState.update { it.copy(countriesList = list) }
                        }
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }

            // Preload all services and court suggestions
            fetchSuggestedServices(emptyList())
            fetchCourtSuggestions(emptyList())
        }
    }

    fun fetchCitiesForState(stateName: String) {
        val trimmed = stateName.trim()
        if (trimmed.isBlank()) return
        viewModelScope.launch {
            try {
                val res = repository.fetchCities(trimmed)
                if (res.result == WebServiceHelper.ServiceCallStatus.Success) {
                    val root = JSONObject(res.responseContent ?: "{}")
                    val data = root.optJSONObject("data")
                    val citiesArr = data?.optJSONArray("cities") ?: root.optJSONArray("cities") ?: root.optJSONArray("data")
                    val list = mutableListOf<String>()
                    if (citiesArr != null) {
                        for (i in 0 until citiesArr.length()) {
                            val c = citiesArr.optString(i).trim()
                            if (c.isNotEmpty()) list.add(c)
                        }
                    }
                    list.sort()
                    val updatedMap = _uiState.value.stateCitiesMap.toMutableMap()
                    updatedMap[trimmed] = list
                    val updatedCourtMap = _uiState.value.courtStatesMap.toMutableMap()
                    updatedCourtMap[trimmed] = list
                    _uiState.update {
                        it.copy(
                            stateCitiesMap = updatedMap,
                            courtStatesMap = updatedCourtMap
                        )
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    private fun fetchSuggestedServices(practiceAreas: List<String>) {
        viewModelScope.launch {
            try {
                val result = repository.fetchSuggestedServices(practiceAreas)
                if (result.result == WebServiceHelper.ServiceCallStatus.Success) {
                    val root = JSONObject(result.responseContent ?: "{}")
                    val data = root.optJSONObject("data")
                    if (data != null) {
                        val suggestedArr = data.optJSONArray("service_suggestions")
                        val suggestedList = mutableListOf<String>()
                        if (suggestedArr != null) {
                            for (i in 0 until suggestedArr.length()) {
                                val s = suggestedArr.optString(i).trim()
                                if (s.isNotEmpty()) suggestedList.add(s)
                            }
                        }
                        val allArr = data.optJSONArray("all_services")
                        val allList = mutableListOf<String>()
                        if (allArr != null) {
                            for (i in 0 until allArr.length()) {
                                val s = allArr.optString(i).trim()
                                if (s.isNotEmpty()) allList.add(s)
                            }
                        }
                        _uiState.update { it.copy(suggestedServices = suggestedList, allServices = allList) }
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    private fun formatCourtTypeDisplay(courtType: String): String {
        val trimmed = courtType.trim()
        if (trimmed.isEmpty()) return ""
        return when (trimmed.lowercase(Locale.ROOT)) {
            "supreme_court", "supremecourt", "supreme court" -> "Supreme Court"
            "high_court", "highcourt", "high court" -> "High Court"
            "district_court", "district court", "district & other lower courts" -> "District & Other Lower Courts"
            "arbitration_centers", "arbitration centers" -> "Arbitration Centers"
            "central_bureau_of_investigation_(cbi)", "central bureau of investigation (cbi)" -> "Central Bureau of Investigation (CBI)"
            "debt_recovery_tribunal_(drt)", "debt recovery tribunal (drt)" -> "Debt Recovery Tribunal (DRT)"
            "enforcement_directorate_(ed)", "enforcement directorate (ed)" -> "Enforcement Directorate (ED)"
            "civil_courts", "civil courts" -> "Civil Courts"
            "maritime_boards", "maritime boards" -> "Maritime Boards"
            "tax_tribunals", "tax tribunals" -> "Tax Tribunals"
            else -> {
                if (trimmed.contains("_")) {
                    trimmed.split("_").joinToString(" ") { word ->
                        when {
                            word.startsWith("(") && word.endsWith(")") -> word.uppercase(Locale.ROOT)
                            word.equals("cbi", true) -> "(CBI)"
                            word.equals("drt", true) -> "(DRT)"
                            word.equals("ed", true) -> "(ED)"
                            else -> word.replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.ROOT) else it.toString() }
                        }
                    }
                } else {
                    trimmed
                }
            }
        }
    }

    private fun convertTo12HourFormat(time24: String): String {
        val trimmed = time24.trim()
        if (trimmed.isEmpty()) return "09:00 AM"
        if (trimmed.contains("AM", ignoreCase = true) || trimmed.contains("PM", ignoreCase = true)) {
            return trimmed
        }
        return try {
            val hasSeconds = trimmed.count { it == ':' } >= 2
            val inputFormat = SimpleDateFormat(if (hasSeconds) "H:mm:ss" else "H:mm", Locale.US)
            val outputFormat = SimpleDateFormat("hh:mm a", Locale.US)
            val date = inputFormat.parse(trimmed)
            if (date != null) outputFormat.format(date) else trimmed
        } catch (e: Exception) {
            trimmed
        }
    }

    private fun to24HourFormat(time12: String): String {
        val trimmed = time12.trim()
        if (trimmed.isEmpty()) return "09:00"
        if (!trimmed.contains("AM", ignoreCase = true) && !trimmed.contains("PM", ignoreCase = true)) {
            return trimmed
        }
        return try {
            val inputFormat = SimpleDateFormat("hh:mm a", Locale.US)
            val outputFormat = SimpleDateFormat("HH:mm", Locale.US)
            val date = inputFormat.parse(trimmed)
            if (date != null) outputFormat.format(date) else trimmed
        } catch (e: Exception) {
            trimmed
        }
    }

    private fun fetchCourtSuggestions(practiceAreas: List<String>) {
        viewModelScope.launch {
            try {
                val result = repository.fetchCourtSuggest(practiceAreas)
                if (result.result == WebServiceHelper.ServiceCallStatus.Success) {
                    val root = JSONObject(result.responseContent ?: "{}")
                    val data = root.optJSONObject("data")
                    val suggestedArr = data?.optJSONArray("court_suggestions") ?: root.optJSONArray("data")
                    val allArr = data?.optJSONArray("all_courts")
                    val suggestedList = mutableListOf<String>()
                    if (suggestedArr != null) {
                        for (i in 0 until suggestedArr.length()) {
                            val ct = suggestedArr.optString(i).trim()
                            if (ct.isNotEmpty()) suggestedList.add(formatCourtTypeDisplay(ct))
                        }
                    }
                    val allList = mutableListOf<String>()
                    if (allArr != null) {
                        for (i in 0 until allArr.length()) {
                            val ct = allArr.optString(i).trim()
                            if (ct.isNotEmpty()) allList.add(formatCourtTypeDisplay(ct))
                        }
                    }
                    _uiState.update { it.copy(suggestedCourtTypes = suggestedList, allCourtTypes = allList) }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    private fun fetchCourtSuggestionsForCurrentPractice() {
        val pa = _uiState.value.practiceDetailsForm.practiceAreas
        fetchCourtSuggestions(pa)
    }

    private fun searchServices(query: String) {
        if (query.isBlank()) {
            _uiState.update { it.copy(searchedServices = emptyList()) }
            return
        }
        viewModelScope.launch {
            try {
                val result = repository.searchServices(query)
                if (result.result == WebServiceHelper.ServiceCallStatus.Success) {
                    val root = JSONObject(result.responseContent ?: "{}")
                    val data = root.optJSONObject("data")
                    val arr = data?.optJSONArray("services") ?: root.optJSONArray("data")
                    if (arr != null) {
                        val list = mutableListOf<String>()
                        for (i in 0 until arr.length()) {
                            val s = arr.optString(i).trim()
                            if (s.isNotEmpty()) list.add(s)
                        }
                        _uiState.update { it.copy(searchedServices = list) }
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    private fun formatDobForApi(rawDob: String): String {
        val trimmed = rawDob.trim()
        if (trimmed.isEmpty()) return ""
        return try {
            if (trimmed.matches(Regex("^\\d{4}-\\d{2}-\\d{2}$"))) {
                trimmed
            } else {
                val inputFormat = SimpleDateFormat("dd-MM-yyyy", Locale.US)
                val outputFormat = SimpleDateFormat("yyyy-MM-dd", Locale.US)
                val date = inputFormat.parse(trimmed)
                if (date != null) outputFormat.format(date) else trimmed
            }
        } catch (e: Exception) {
            trimmed
        }
    }

    private fun formatDobForDisplay(rawDob: String): String {
        val trimmed = rawDob.trim()
        if (trimmed.isEmpty()) return ""
        return try {
            if (trimmed.matches(Regex("^\\d{2}-\\d{2}-\\d{4}$"))) {
                trimmed
            } else {
                val inputFormat = SimpleDateFormat("yyyy-MM-dd", Locale.US)
                val outputFormat = SimpleDateFormat("dd-MM-yyyy", Locale.US)
                val date = inputFormat.parse(trimmed)
                if (date != null) outputFormat.format(date) else trimmed
            }
        } catch (e: Exception) {
            trimmed
        }
    }

    private fun populateProfileEditForm() {
        val model = _uiState.value.firmProfileModel ?: return
        val profile = model.data?.profile
        val firm = profile?.firm
        val isFirm = !_uiState.value.isMyProfile

        originalValues.clear()

        val firmName = firm?.fullname ?: ""
        val contactName = if (isFirm) (firm?.contact_person ?: "") else (profile?.name ?: "")
        val email = if (isFirm) (firm?.email ?: "") else (profile?.email ?: "")
        val phone = if (isFirm) (firm?.contact_phone ?: "") else (profile?.mobile ?: "")
        val website = firm?.website ?: ""
        val gender = profile?.gender ?: ""
        val dob = formatDobForDisplay(profile?.date_of_birth ?: "")
        val bio = if (isFirm) (firm?.firm_description ?: "") else (profile?.bio_description ?: "")
        val consultationFee = profile?.consultation_fee?.amount ?: ""
        val currency = firm?.billing_currency ?: "INR"

        originalValues["fullname"] = firmName
        originalValues["contact_person"] = contactName
        originalValues["name"] = profile?.name ?: ""
        originalValues["email"] = email
        originalValues["mobile"] = phone
        originalValues["website"] = website
        originalValues["gender"] = gender
        originalValues["date_of_birth"] = dob
        originalValues[if (isFirm) "firm_description" else "bio_description"] = bio
        originalValues["consultation_fee"] = consultationFee

        _uiState.update {
            it.copy(
                profileForm = ProfileEditFormState(
                    firmName = firmName,
                    contactName = contactName,
                    email = email,
                    phone = phone,
                    website = website,
                    gender = gender,
                    dob = dob,
                    bio = bio,
                    consultationFee = consultationFee,
                    currency = currency
                )
            )
        }

        populateAdditionalInfoForm()
    }

    private fun populateAdditionalInfoForm() {
        val model = _uiState.value.firmProfileModel ?: return
        val profile = model.data?.profile
        val firm = profile?.firm
        val isFirm = !_uiState.value.isMyProfile

        originalValues.clear()

        val barOrRegId = if (isFirm) (firm?.reg_id ?: "") else (profile?.bar_council_id ?: "")
        val yearsExp = if (isFirm) (firm?.years_of_incorporation?.toString() ?: "0") else (profile?.years_of_experience?.toString() ?: "0")
        
        originalValues["bar_council_id"] = barOrRegId
        originalValues["years_of_experience"] = yearsExp

        // Practice Areas
        val paList = mutableListOf<String>()
        val paArr = if (isFirm) firm?.practice_areas else profile?.practice_areas
        originalPracticeAreas = paArr
        if (paArr != null) {
            for (i in 0 until paArr.length()) {
                val s = paArr.optString(i)
                if (s.isNotBlank()) paList.add(s)
            }
        }

        // Services Offered
        val sList = mutableListOf<String>()
        val sArr = if (isFirm) firm?.services_offered else profile?.services_offered
        originalServices = sArr
        if (sArr != null) {
            for (i in 0 until sArr.length()) {
                val s = sArr.optString(i)
                if (s.isNotBlank()) sList.add(s)
            }
        }

        // Languages
        val lList = mutableListOf<String>()
        val lArr = profile?.languages_spoken
        originalLanguages = lArr
        if (lArr != null) {
            for (i in 0 until lArr.length()) {
                val l = lArr.optString(i)
                if (l.isNotBlank()) lList.add(l)
            }
        }

        // Addresses
        val reg = firm?.address
        val regAddr = AddressState(
            houseFlatNo = reg?.house_flat_no ?: "",
            street = reg?.street ?: "",
            cityTown = reg?.city_town ?: "",
            state = reg?.state ?: "",
            country = reg?.country ?: "India",
            zipcode = reg?.zipcode ?: ""
        )
        originalValues["address_house_flat_no"] = regAddr.houseFlatNo
        originalValues["address_street"] = regAddr.street
        originalValues["address_city_town"] = regAddr.cityTown
        originalValues["address_state"] = regAddr.state
        originalValues["address_country"] = regAddr.country
        originalValues["address_zipcode"] = regAddr.zipcode

        val corr = firm?.correspondence_address
        val corrAddr = AddressState(
            houseFlatNo = corr?.house_flat_no ?: "",
            street = corr?.street ?: "",
            cityTown = corr?.city_town ?: "",
            state = corr?.state ?: "",
            country = corr?.country ?: "India",
            zipcode = corr?.zipcode ?: ""
        )
        originalValues["correspondence_house_flat_no"] = corrAddr.houseFlatNo
        originalValues["correspondence_street"] = corrAddr.street
        originalValues["correspondence_city_town"] = corrAddr.cityTown
        originalValues["correspondence_state"] = corrAddr.state
        originalValues["correspondence_country"] = corrAddr.country
        originalValues["correspondence_zipcode"] = corrAddr.zipcode

        val sameAs = regAddr.houseFlatNo.isNotBlank() &&
                regAddr.houseFlatNo.equals(corrAddr.houseFlatNo, ignoreCase = true) &&
                regAddr.cityTown.equals(corrAddr.cityTown, ignoreCase = true) &&
                regAddr.state.equals(corrAddr.state, ignoreCase = true) &&
                regAddr.zipcode.equals(corrAddr.zipcode, ignoreCase = true)

        if (regAddr.state.isNotBlank()) {
            fetchCitiesForState(regAddr.state)
        }
        if (corrAddr.state.isNotBlank() && corrAddr.state != regAddr.state) {
            fetchCitiesForState(corrAddr.state)
        }

        // Courts
        val courtsList = mutableListOf<CourtEnrollmentItemState>()
        profile?.court_enrollments?.forEach { ce ->
            val rawCt = if (!ce.court_name.isNullOrBlank()) ce.court_name!! else (ce.court_type ?: "")
            val displayCt = formatCourtTypeDisplay(rawCt)
            val isSupreme = displayCt.equals("Supreme Court", ignoreCase = true)
            fun clean(s: String?): String {
                if (s == null || s.trim().equals("null", ignoreCase = true) || s.trim().equals("states", ignoreCase = true)) return ""
                val t = s.trim()
                if (t.startsWith("{") && t.contains("state")) {
                    try {
                        val obj = JSONObject(t)
                        val st = obj.optString("state", "")
                        if (st.isNotBlank()) return st
                    } catch (_: Exception) {}
                    return ""
                }
                return t
            }
            val cleanState = clean(ce.state)
            val cleanCity = clean(ce.city)
            courtsList.add(
                CourtEnrollmentItemState(
                    courtType = displayCt,
                    state = if (isSupreme && cleanState.isBlank()) "Delhi" else cleanState,
                    city = if (isSupreme && cleanCity.isBlank()) "New Delhi" else cleanCity,
                    courtName = ce.court_name ?: ""
                )
            )
        }

        // Cases
        val casesList = mutableListOf<String>()
        profile?.cases_handled?.forEach { casesList.add(it) }

        // Education
        val eduList = mutableListOf<EduItemState>()
        profile?.education?.forEach { edu ->
            eduList.add(
                EduItemState(
                    title = edu.degree ?: "",
                    institution = edu.university ?: "",
                    year = if (edu.passing_year > 0) edu.passing_year.toString() else ""
                )
            )
        }

        // Certifications
        val certList = mutableListOf<EduItemState>()
        profile?.certifications?.forEach { cert ->
            certList.add(
                EduItemState(
                    title = cert.certification_name ?: "",
                    institution = cert.issuing_authority ?: "",
                    year = if (cert.year_of_issue > 0) cert.year_of_issue.toString() else ""
                )
            )
        }

        // Awards
        val awardsList = mutableListOf<EduItemState>()
        val srcAwards = if (isFirm) firm?.awards else profile?.awards
        srcAwards?.forEach { award ->
            awardsList.add(
                EduItemState(
                    title = award.award_name ?: "",
                    institution = award.purpose ?: "",
                    year = if (award.year_of_award > 0) award.year_of_award.toString() else ""
                )
            )
        }

        // Availability
        val apiSchedule = profile?.availability?.weekly_schedule
        val scheduleList = if (!apiSchedule.isNullOrEmpty()) {
            apiSchedule.map { item ->
                val isWorking = item.isIs_working_day
                val fromTime = item.work_slots?.firstOrNull()?.start_time?.let { convertTo12HourFormat(it) } ?: "09:00 AM"
                val toTime = item.work_slots?.firstOrNull()?.end_time?.let { convertTo12HourFormat(it) } ?: "05:00 PM"
                val excluded = item.expert_slots?.mapNotNull { slot ->
                    val st = slot.start_time
                    val et = slot.end_time
                    if (!st.isNullOrBlank() && !et.isNullOrBlank()) {
                        "${convertTo12HourFormat(st)} - ${convertTo12HourFormat(et)}"
                    } else null
                } ?: emptyList()

                DayAvailabilityState(
                    dayName = item.day_name ?: "",
                    date = item.date ?: "",
                    dateLabel = item.date_label ?: "${item.day_name}, ${item.date ?: ""}",
                    isWorkingDay = isWorking,
                    fromTime = fromTime,
                    toTime = toTime,
                    excludedSlots = excluded
                )
            }
        } else {
            val cal = Calendar.getInstance()
            val dayFormat = SimpleDateFormat("EEEE", Locale.US)
            val apiDateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.US)
            val labelDateFormat = SimpleDateFormat("EEE, dd MMM", Locale.US)
            (0..6).map {
                val dayName = dayFormat.format(cal.time)
                val date = apiDateFormat.format(cal.time)
                val dateLabel = labelDateFormat.format(cal.time)
                val isWorking = !dayName.equals("Sunday", ignoreCase = true)
                cal.add(Calendar.DATE, 1)
                DayAvailabilityState(
                    dayName = dayName,
                    date = date,
                    dateLabel = dateLabel,
                    isWorkingDay = isWorking,
                    fromTime = "09:00 AM",
                    toTime = "05:00 PM",
                    excludedSlots = emptyList()
                )
            }
        }
        originalWeeklySchedule = profile?.availability?.weekly_schedule

        val rawFee = profile?.consultation_fee?.amount ?: ""
        val initFee = if (rawFee.isNotBlank()) rawFee else "500"
        val rawCurr = firm?.billing_currency ?: "INR"
        val initCurr = if (rawCurr.contains("INR", true) || rawCurr.contains("Indian", true)) "IndianRupee(INR)" else rawCurr

        _uiState.update {
            it.copy(
                practiceDetailsForm = PracticeDetailsEditState(
                    barOrRegId = barOrRegId,
                    yearsOfExperience = yearsExp,
                    billingCurrency = initCurr,
                    consultationFee = initFee,
                    practiceAreas = paList,
                    servicesOffered = sList,
                    languagesSpoken = lList,
                    registeredAddress = regAddr,
                    mailingAddress = corrAddr,
                    sameAsRegistered = sameAs
                ),
                courtsCasesForm = CourtsAndCasesEditState(
                    courtEnrollments = courtsList,
                    casesHandled = casesList
                ),
                educationAwardsForm = EducationAndAwardsEditState(
                    educationList = eduList,
                    certificationsList = certList,
                    awardsList = awardsList
                ),
                availabilityForm = AvailabilityEditState(
                    weeklySchedule = scheduleList
                )
            )
        }

        fetchSuggestedServices(paList)
        fetchCourtSuggestions(paList)
        if (regAddr.state.isNotBlank()) {
            fetchCitiesForState(regAddr.state)
        }
        if (corrAddr.state.isNotBlank() && !corrAddr.state.equals(regAddr.state, ignoreCase = true)) {
            fetchCitiesForState(corrAddr.state)
        }
    }

    private fun generateBio() {
        val model = _uiState.value.firmProfileModel
        val profile = model?.data?.profile
        val firm = profile?.firm
        val isFirm = !_uiState.value.isMyProfile
        val form = _uiState.value.profileForm
        val practiceForm = _uiState.value.practiceDetailsForm

        viewModelScope.launch {
            _uiState.update { it.copy(isBioGenerating = true) }
            try {
                val payload = JSONObject().apply {
                    val name = if (isFirm) form.firmName.ifEmpty { firm?.fullname ?: "" } else form.contactName.ifEmpty { profile?.name ?: "" }
                    if (name.isNotEmpty()) put("name", name)

                    val genderVal = if (!isFirm) {
                        form.gender.ifEmpty { profile?.gender ?: "" }.lowercase(Locale.ROOT)
                    } else ""
                    if (genderVal.isNotEmpty()) put("gender", genderVal)

                    val city = practiceForm.registeredAddress.cityTown.ifEmpty { firm?.address?.city_town ?: "" }
                    if (city.isNotEmpty()) put("city", city)

                    val pa = JSONArray()
                    if (practiceForm.practiceAreas.isNotEmpty()) {
                        practiceForm.practiceAreas.forEach { pa.put(it) }
                    } else {
                        val fallbackPa = if (isFirm) firm?.practice_areas else profile?.practice_areas
                        if (fallbackPa != null) {
                            for (i in 0 until fallbackPa.length()) pa.put(fallbackPa.optString(i))
                        }
                    }
                    if (pa.length() > 0) put("practice_areas", pa)

                    val serv = JSONArray()
                    if (practiceForm.servicesOffered.isNotEmpty()) {
                        practiceForm.servicesOffered.forEach { serv.put(it) }
                    } else {
                        val fallbackServ = if (isFirm) firm?.services_offered else profile?.services_offered
                        if (fallbackServ != null) {
                            for (i in 0 until fallbackServ.length()) serv.put(fallbackServ.optString(i))
                        }
                    }
                    if (serv.length() > 0) put("services_offered", serv)

                    val exp = practiceForm.yearsOfExperience.toIntOrNull()
                        ?: (if (isFirm) firm?.years_of_incorporation ?: 0 else profile?.years_of_experience ?: 0)
                    if (exp > 0) put("years_of_experience", exp)

                    if (!isFirm) {
                        val langs = JSONArray()
                        if (practiceForm.languagesSpoken.isNotEmpty()) {
                            practiceForm.languagesSpoken.forEach { langs.put(it) }
                        } else {
                            val modelLangs = profile?.languages_spoken
                            if (modelLangs != null) {
                                for (i in 0 until modelLangs.length()) langs.put(modelLangs.optString(i))
                            }
                        }
                        if (langs.length() > 0) put("languages_spoken", langs)

                        val cases = JSONArray()
                        if (_uiState.value.courtsCasesForm.casesHandled.isNotEmpty()) {
                            _uiState.value.courtsCasesForm.casesHandled.forEach { cases.put(it) }
                        } else {
                            val modelCases = profile?.cases_handled
                            if (modelCases != null) {
                                for (c in modelCases) cases.put(c)
                            }
                        }
                        if (cases.length() > 0) put("cases_handled", cases)

                        val edu = JSONArray()
                        if (_uiState.value.educationAwardsForm.educationList.any { it.title.isNotBlank() }) {
                            _uiState.value.educationAwardsForm.educationList.forEach {
                                if (it.title.isNotBlank()) {
                                    edu.put(JSONObject().apply {
                                        put("degree", it.title)
                                        put("university", it.institution)
                                        put("passing_year", it.year.toIntOrNull() ?: 0)
                                    })
                                }
                            }
                        } else {
                            val modelEdu = profile?.education
                            if (modelEdu != null) {
                                for (e in modelEdu) {
                                    edu.put(JSONObject().apply {
                                        put("degree", e.degree ?: "")
                                        put("university", e.university ?: "")
                                        put("passing_year", e.passing_year)
                                    })
                                }
                            }
                        }
                        if (edu.length() > 0) put("education", edu)

                        val cert = JSONArray()
                        if (_uiState.value.educationAwardsForm.certificationsList.any { it.title.isNotBlank() }) {
                            _uiState.value.educationAwardsForm.certificationsList.forEach {
                                if (it.title.isNotBlank()) {
                                    cert.put(JSONObject().apply {
                                        put("certification_name", it.title)
                                        put("issuing_authority", it.institution)
                                        put("year_of_issue", it.year.toIntOrNull() ?: 0)
                                    })
                                }
                            }
                        } else {
                            val modelCert = profile?.certifications
                            if (modelCert != null) {
                                for (c in modelCert) {
                                    cert.put(JSONObject().apply {
                                        put("certification_name", c.certification_name ?: "")
                                        put("issuing_authority", c.issuing_authority ?: "")
                                        put("year_of_issue", c.year_of_issue)
                                    })
                                }
                            }
                        }
                        if (cert.length() > 0) put("certifications", cert)

                        val courtsArr = JSONArray()
                        if (_uiState.value.courtsCasesForm.courtEnrollments.any { it.courtName.isNotBlank() || it.courtType.isNotBlank() }) {
                            _uiState.value.courtsCasesForm.courtEnrollments.forEach {
                                courtsArr.put(JSONObject().apply {
                                    put("court_name", it.courtName)
                                    put("court_type", it.courtType)
                                    put("state", it.state)
                                    put("city", it.city)
                                })
                            }
                        } else {
                            val modelCourts = profile?.court_enrollments
                            if (modelCourts != null) {
                                for (ce in modelCourts) {
                                    courtsArr.put(JSONObject().apply {
                                        put("court_name", ce.court_name ?: "")
                                        put("court_type", ce.court_type ?: "")
                                        put("state", ce.state ?: "")
                                        put("city", ce.city ?: "")
                                    })
                                }
                            }
                        }
                        if (courtsArr.length() > 0) put("court_enrollments", courtsArr)
                    }

                    val awards = JSONArray()
                    if (_uiState.value.educationAwardsForm.awardsList.any { it.title.isNotBlank() }) {
                        _uiState.value.educationAwardsForm.awardsList.forEach {
                            if (it.title.isNotBlank()) {
                                awards.put(JSONObject().apply {
                                    put("award_name", it.title)
                                    put("purpose", it.institution)
                                    put("year_of_award", it.year.toIntOrNull() ?: 0)
                                })
                            }
                        }
                    } else {
                        val rawAwards = if (isFirm) firm?.awards else profile?.awards
                        if (rawAwards != null) {
                            for (a in rawAwards) {
                                awards.put(JSONObject().apply {
                                    put("award_name", a.award_name ?: "")
                                    put("purpose", a.purpose ?: "")
                                    put("year_of_award", a.year_of_award)
                                })
                            }
                        }
                    }
                    if (awards.length() > 0) put("awards", awards)

                    val addr = JSONObject().apply {
                        val regAddr = practiceForm.registeredAddress
                        val fallbackAddr = firm?.address
                        put("house_flat_no", regAddr.houseFlatNo.ifEmpty { fallbackAddr?.house_flat_no ?: "" })
                        put("street", regAddr.street.ifEmpty { fallbackAddr?.street ?: "" })
                        put("city_town", regAddr.cityTown.ifEmpty { fallbackAddr?.city_town ?: "" })
                        put("state", regAddr.state.ifEmpty { fallbackAddr?.state ?: "" })
                        put("country", regAddr.country.ifEmpty { fallbackAddr?.country ?: "India" })
                        put("zipcode", regAddr.zipcode.ifEmpty { fallbackAddr?.zipcode ?: "" })
                    }
                    put("address", addr)
                }

                val httpResult = repository.generateBio(payload)
                _uiState.update { it.copy(isBioGenerating = false) }

                if (httpResult.result == WebServiceHelper.ServiceCallStatus.Success) {
                    val resJson = JSONObject(httpResult.responseContent ?: "{}")
                    val isError = resJson.optBoolean("error", false)
                    if (!isError) {
                        val data = resJson.optJSONObject("data")
                        val generatedBio = data?.optString("bio", "") ?: resJson.optString("bio", "")
                        if (generatedBio.isNotBlank()) {
                            _uiState.update { it.copy(profileForm = it.profileForm.copy(bio = generatedBio)) }
                        } else {
                            _uiState.update { it.copy(toastMessage = "Unable to generate bio from provided details") }
                        }
                    } else {
                        if (resJson.has("errors")) {
                            val errors = resJson.optJSONArray("errors")
                            val fieldNames = mutableListOf<String>()
                            if (errors != null) {
                                for (i in 0 until errors.length()) {
                                    val err = errors.optJSONObject(i)
                                    val f = err?.optString("field", "") ?: ""
                                    if (f.isNotEmpty()) fieldNames.add(f.replace("_", " ").capitalize(Locale.ROOT))
                                }
                            }
                            val msg = if (fieldNames.isNotEmpty()) "Please fill in the necessary fields: " + fieldNames.joinToString(", ")
                            else resJson.optString("msg", "Failed to generate bio")
                            _uiState.update { it.copy(alertTitle = "Alert !", alertMessage = msg) }
                        } else {
                            _uiState.update { it.copy(alertTitle = "Alert !", alertMessage = resJson.optString("msg", "Failed to generate bio")) }
                        }
                    }
                } else {
                    _uiState.update { it.copy(alertTitle = "Alert !", alertMessage = "Failed to generate bio. Please check your network connection.") }
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(isBioGenerating = false, alertTitle = "Alert !", alertMessage = e.message ?: "Failed to generate bio") }
            }
        }
    }

    private fun updateCachedUserData(name: String, firmName: String) {
        try {
            val prefs = getApplication<Application>().getSharedPreferences("MyPrefs", android.content.Context.MODE_PRIVATE)
            val existingJson = prefs.getString("Json_key", "")
            if (!existingJson.isNullOrEmpty()) {
                val userJson = JSONObject(existingJson)
                if (name.isNotEmpty()) {
                    userJson.put("name", name)
                    userJson.put("contact_person", name)
                }
                if (firmName.isNotEmpty()) {
                    userJson.put("firm_name", firmName)
                }
                prefs.edit().putString("Json_key", userJson.toString()).apply()
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun extractApiErrorMessage(json: JSONObject, defaultMsg: String): String {
        if (json.has("errors")) {
            val errors = json.optJSONArray("errors")
            if (errors != null && errors.length() > 0) {
                val messages = mutableListOf<String>()
                for (i in 0 until errors.length()) {
                    val err = errors.optJSONObject(i)
                    if (err != null) {
                        val field = err.optString("field", "")
                        val msg = err.optString("msg", err.optString("message", ""))
                        if (field.isNotEmpty() && msg.isNotEmpty()) {
                            messages.add("$field: $msg")
                        } else if (msg.isNotEmpty()) {
                            messages.add(msg)
                        } else if (field.isNotEmpty()) {
                            messages.add("Validation error on $field")
                        }
                    } else {
                        val str = errors.optString(i, "")
                        if (str.isNotEmpty()) messages.add(str)
                    }
                }
                if (messages.isNotEmpty()) {
                    return messages.joinToString("\n")
                }
            }
        }
        val msg = json.optString("msg", json.optString("message", ""))
        return if (msg.isNotBlank() && !msg.equals("true", ignoreCase = true) && !msg.equals("false", ignoreCase = true)) msg else defaultMsg
    }

    private fun saveProfileInfo() {
        val form = _uiState.value.profileForm
        val isFirm = !_uiState.value.isMyProfile
        val isSolo = "solo".equals(Constants.CATEGORY, ignoreCase = true)
        val isGHorTM = Constants.ROLE.equals("GH", ignoreCase = true) || Constants.ROLE.equals("TM", ignoreCase = true)

        // Validation
        if (form.phone.isEmpty() || form.phone.length < 10) {
            _uiState.update { it.copy(alertTitle = "Alert !", alertMessage = "Please enter the valid Phone Number") }
            return
        }
        if (form.email.isEmpty() || !Patterns.EMAIL_ADDRESS.matcher(form.email).matches()) {
            _uiState.update { it.copy(alertTitle = "Alert !", alertMessage = "Please enter the valid Email") }
            return
        }
        if (isSolo && form.contactName.isEmpty()) {
            _uiState.update { it.copy(alertTitle = "Alert !", alertMessage = "Please enter the First Name") }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            try {
                val root = JSONObject()
                val firm = JSONObject()

                if (isFirm) {
                    if (!isGHorTM) {
                        firm.put("fullname", form.firmName)
                    }
                    firm.put("email", form.email)
                    firm.put("contact_phone", form.phone)
                    firm.put("contact_person", form.contactName)
                    if (!isGHorTM && form.website.isNotBlank()) {
                        firm.put("website", form.website)
                    }
                    if (form.bio.isNotBlank()) {
                        firm.put("firm_description", form.bio)
                    }
                    if (form.currency.isNotBlank()) {
                        firm.put("billing_currency", form.currency)
                    }
                    root.put("firm", firm)
                } else {
                    // My Profile
                    root.put("name", form.contactName)
                    root.put("email", form.email)
                    root.put("mobile", form.phone)
                    if (form.bio.isNotBlank()) {
                        root.put("bio_description", form.bio)
                    }
                    val feeClean = form.consultationFee.replace("₹", "").filter { it.isDigit() }
                    if (feeClean.isNotBlank()) {
                        val feeObj = JSONObject().apply {
                            put("amount", feeClean)
                        }
                        root.put("consultation_fee", feeObj)
                    }

                    if (isSolo) {
                        if (form.gender.isNotBlank()) {
                            root.put("gender", form.gender.lowercase(Locale.ROOT))
                        }
                        if (form.dob.isNotBlank()) {
                            root.put("date_of_birth", formatDobForApi(form.dob))
                        }
                        firm.put("fullname", form.contactName.ifEmpty { form.firmName })
                        firm.put("email", form.email)
                        firm.put("contact_phone", form.phone)
                        if (form.website.isNotBlank()) {
                            firm.put("website", form.website)
                        }
                        if (firm.length() > 0) {
                            root.put("firm", firm)
                        }
                    }
                }

                val result = repository.updateProfile(root)
                _uiState.update { it.copy(isLoading = false) }

                if (result.result == WebServiceHelper.ServiceCallStatus.Success) {
                    val resJson = try { JSONObject(result.responseContent ?: "{}") } catch (e: Exception) { JSONObject() }
                    val isError = resJson.optBoolean("error", false)
                    if (!isError) {
                        if (isFirm) {
                            Constants.FIRM_NAME = form.firmName
                            Constants.ContactName = form.contactName
                        } else {
                            Constants.NAME = form.contactName
                            Constants.ContactName = form.contactName
                            if (isSolo) {
                                Constants.FIRM_NAME = form.contactName.ifEmpty { form.firmName }
                            }
                        }
                        updateCachedUserData(Constants.NAME ?: "", Constants.FIRM_NAME ?: "")

                        val successMsg = resJson.optString("msg", resJson.optString("message", "Profile updated successfully"))
                        _uiState.update { it.copy(isEditingProfileInfo = false, toastMessage = if (successMsg.isNotBlank()) successMsg else "Profile updated successfully") }
                        loadProfile()
                    } else {
                        val errMsg = extractApiErrorMessage(resJson, "Failed to update profile")
                        _uiState.update { it.copy(alertTitle = "Alert", alertMessage = errMsg) }
                    }
                } else {
                    val resJson = try { JSONObject(result.responseContent ?: "{}") } catch (e: Exception) { JSONObject() }
                    val errMsg = extractApiErrorMessage(resJson, "Failed to update profile")
                    _uiState.update { it.copy(alertTitle = "Alert", alertMessage = errMsg) }
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(isLoading = false, alertTitle = "Alert", alertMessage = e.message ?: "Failed to save profile") }
            }
        }
    }

    private fun saveAdditionalInfo() {
        val form = _uiState.value.practiceDetailsForm
        val courtsForm = _uiState.value.courtsCasesForm
        val eduForm = _uiState.value.educationAwardsForm
        val availForm = _uiState.value.availabilityForm
        val isFirm = !_uiState.value.isMyProfile
        val isSolo = "solo".equals(Constants.CATEGORY, ignoreCase = true)

        // Validations
        if (!isFirm && form.practiceAreas.isEmpty()) {
            _uiState.update { it.copy(alertTitle = "Alert !", alertMessage = "Please check the Practice Area.") }
            return
        }

        // Court Validation
        if (!isFirm && courtsForm.courtEnrollments.isNotEmpty()) {
            for (ce in courtsForm.courtEnrollments) {
                if (ce.courtType.isBlank()) {
                    _uiState.update { it.copy(alertTitle = "Alert !", alertMessage = "Please select Court Type for all court rows.") }
                    return
                }
                if (!ce.courtType.equals("Supreme Court", ignoreCase = true)) {
                    if (ce.state.isBlank()) {
                        _uiState.update { it.copy(alertTitle = "Alert !", alertMessage = "Please select State for all court rows.") }
                        return
                    }
                    if (ce.city.isBlank()) {
                        _uiState.update { it.copy(alertTitle = "Alert !", alertMessage = "Please enter City for all court rows.") }
                        return
                    }
                }
            }
        }

        // Address Validation
        if (form.registeredAddress.houseFlatNo.isBlank() || form.registeredAddress.cityTown.isBlank() || form.registeredAddress.state.isBlank() || form.registeredAddress.zipcode.length != 6) {
            _uiState.update { it.copy(alertTitle = "Alert !", alertMessage = "Please enter complete Registered Address with a valid 6-digit ZIP code.") }
            return
        }
        if (!form.sameAsRegistered) {
            if (form.mailingAddress.houseFlatNo.isBlank() || form.mailingAddress.cityTown.isBlank() || form.mailingAddress.state.isBlank() || form.mailingAddress.zipcode.length != 6) {
                _uiState.update { it.copy(alertTitle = "Alert !", alertMessage = "Please enter complete Mailing Address with a valid 6-digit ZIP code.") }
                return
            }
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            try {
                val root = JSONObject()
                val firm = JSONObject()

                if (isFirm) {
                    if (form.barOrRegId.isNotBlank()) {
                        firm.put("reg_id", form.barOrRegId)
                    }
                    val expVal = form.yearsOfExperience.toIntOrNull()
                    if (expVal != null) {
                        firm.put("years_of_incorporation", expVal)
                    }

                    val paArr = JSONArray()
                    form.practiceAreas.forEach { paArr.put(it) }
                    firm.put("practice_areas", paArr)

                    val sArr = JSONArray()
                    form.servicesOffered.forEach { sArr.put(it) }
                    firm.put("services_offered", sArr)

                    val awardsArr = JSONArray()
                    eduForm.awardsList.forEach {
                        if (it.title.isNotBlank()) {
                            awardsArr.put(JSONObject().apply {
                                put("award_name", it.title)
                                put("purpose", it.institution)
                                put("year_of_award", it.year.toIntOrNull() ?: 0)
                            })
                        }
                    }
                    firm.put("awards", awardsArr)

                    val regAddrJson = JSONObject().apply {
                        put("house_flat_no", form.registeredAddress.houseFlatNo)
                        put("street", form.registeredAddress.street)
                        put("city_town", form.registeredAddress.cityTown)
                        put("state", form.registeredAddress.state)
                        put("country", form.registeredAddress.country.ifEmpty { "India" })
                        put("zipcode", form.registeredAddress.zipcode)
                    }
                    firm.put("address", regAddrJson)

                    val mailingAddr = if (form.sameAsRegistered) form.registeredAddress else form.mailingAddress
                    val mailAddrJson = JSONObject().apply {
                        put("house_flat_no", mailingAddr.houseFlatNo)
                        put("street", mailingAddr.street)
                        put("city_town", mailingAddr.cityTown)
                        put("state", mailingAddr.state)
                        put("country", mailingAddr.country.ifEmpty { "India" })
                        put("zipcode", mailingAddr.zipcode)
                    }
                    firm.put("correspondence_address", mailAddrJson)

                    root.put("firm", firm)
                } else {
                    root.put("bar_council_id", form.barOrRegId)
                    val expVal = form.yearsOfExperience.toIntOrNull() ?: 0
                    root.put("years_of_experience", expVal)

                    if (form.consultationFee.isNotBlank()) {
                        val feeClean = form.consultationFee.replace("₹", "").filter { it.isDigit() }
                        val feeVal = feeClean.toIntOrNull() ?: 500
                        val curVal = if (form.billingCurrency.contains("USD", ignoreCase = true)) "USD" else "INR"
                        root.put("consultation_fee", JSONObject().apply {
                            put("amount", feeVal)
                            put("currency", curVal)
                        })
                    }

                    val paArr = JSONArray()
                    form.practiceAreas.forEach { paArr.put(it) }
                    root.put("practice_areas", paArr)
                    if (isSolo) firm.put("practice_areas", paArr)

                    val sArr = JSONArray()
                    form.servicesOffered.forEach { sArr.put(it) }
                    root.put("services_offered", sArr)
                    if (isSolo) firm.put("services_offered", sArr)

                    val lArr = JSONArray()
                    form.languagesSpoken.forEach { lArr.put(it) }
                    root.put("languages_spoken", lArr)

                    val cArr = JSONArray()
                    courtsForm.courtEnrollments.forEach { ce ->
                        if (ce.courtType.isNotBlank()) {
                            val apiCt = when (ce.courtType.lowercase(Locale.ROOT)) {
                                "supreme court", "supreme_court" -> "supreme_court"
                                "high court", "high_court" -> "high_court"
                                "district & other lower courts", "district court", "district_court" -> "district_court"
                                else -> ce.courtType.lowercase(Locale.ROOT).replace(" ", "_")
                            }
                            val isSc = apiCt == "supreme_court"
                            val finalState = if (isSc && ce.state.isBlank()) "Delhi" else ce.state
                            val finalCity = if (isSc && ce.city.isBlank()) "New Delhi" else ce.city
                            cArr.put(JSONObject().apply {
                                put("court_name", apiCt)
                                put("court_type", apiCt)
                                put("state", finalState)
                                put("city", finalCity)
                            })
                        }
                    }
                    root.put("court_enrollments", cArr)

                    val casesArr = JSONArray()
                    courtsForm.casesHandled.forEach { casesArr.put(it) }
                    root.put("cases_handled", casesArr)

                    val eduArr = JSONArray()
                    eduForm.educationList.forEach {
                        if (it.title.isNotBlank()) {
                            eduArr.put(JSONObject().apply {
                                put("degree", it.title)
                                put("university", it.institution)
                                put("passing_year", it.year.toIntOrNull() ?: 0)
                            })
                        }
                    }
                    root.put("education", eduArr)

                    val certArr = JSONArray()
                    eduForm.certificationsList.forEach {
                        if (it.title.isNotBlank()) {
                            certArr.put(JSONObject().apply {
                                put("certification_name", it.title)
                                put("issuing_authority", it.institution)
                                put("year_of_issue", it.year.toIntOrNull() ?: 0)
                            })
                        }
                    }
                    root.put("certifications", certArr)

                    val awardsArr = JSONArray()
                    eduForm.awardsList.forEach {
                        if (it.title.isNotBlank()) {
                            awardsArr.put(JSONObject().apply {
                                put("award_name", it.title)
                                put("purpose", it.institution)
                                put("year_of_award", it.year.toIntOrNull() ?: 0)
                            })
                        }
                    }
                    root.put("awards", awardsArr)
                    if (isSolo) firm.put("awards", awardsArr)

                    val avail = JSONObject()
                    val weekArr = JSONArray()
                    availForm.weeklySchedule.forEach { d ->
                        val dayObj = JSONObject().apply {
                            put("day_name", d.dayName)
                            put("date", d.date)
                            put("is_working_day", d.isWorkingDay)

                            val workSlots = JSONArray()
                            if (d.isWorkingDay) {
                                workSlots.put(JSONObject().apply {
                                    put("start_time", to24HourFormat(d.fromTime))
                                    put("end_time", to24HourFormat(d.toTime))
                                })
                            }
                            put("work_slots", workSlots)

                            val expSlots = JSONArray()
                            d.excludedSlots.forEach { slot ->
                                val parts = slot.split(" - ")
                                if (parts.size == 2) {
                                    expSlots.put(JSONObject().apply {
                                        put("start_time", to24HourFormat(parts[0].trim()))
                                        put("end_time", to24HourFormat(parts[1].trim()))
                                    })
                                }
                            }
                            put("expert_slots", expSlots)
                        }
                        weekArr.put(dayObj)
                    }
                    avail.put("weekly_schedule", weekArr)
                    root.put("availability", avail)

                    val regAddrJson = JSONObject().apply {
                        put("house_flat_no", form.registeredAddress.houseFlatNo)
                        put("street", form.registeredAddress.street)
                        put("city_town", form.registeredAddress.cityTown)
                        put("state", form.registeredAddress.state)
                        put("country", form.registeredAddress.country.ifEmpty { "India" })
                        put("zipcode", form.registeredAddress.zipcode)
                    }
                    firm.put("address", regAddrJson)

                    val mailingAddr = if (form.sameAsRegistered) form.registeredAddress else form.mailingAddress
                    val mailAddrJson = JSONObject().apply {
                        put("house_flat_no", mailingAddr.houseFlatNo)
                        put("street", mailingAddr.street)
                        put("city_town", mailingAddr.cityTown)
                        put("state", mailingAddr.state)
                        put("country", mailingAddr.country.ifEmpty { "India" })
                        put("zipcode", mailingAddr.zipcode)
                    }
                    firm.put("correspondence_address", mailAddrJson)

                    if (firm.length() > 0) {
                        root.put("firm", firm)
                    }
                }

                val result = repository.updateProfile(root)
                _uiState.update { it.copy(isLoading = false) }

                if (result.result == WebServiceHelper.ServiceCallStatus.Success) {
                    val resJson = try { JSONObject(result.responseContent ?: "{}") } catch (e: Exception) { JSONObject() }
                    val isError = resJson.optBoolean("error", false)
                    if (!isError) {
                        val successMsg = resJson.optString("msg", resJson.optString("message", "Additional Information updated successfully"))
                        _uiState.update { it.copy(isEditingAdditionalInfo = false, toastMessage = if (successMsg.isNotBlank()) successMsg else "Additional Information updated successfully") }
                        loadProfile()
                    } else {
                        val errMsg = resJson.optString("msg", resJson.optString("message", "Failed to update additional information"))
                        _uiState.update { it.copy(alertTitle = "Alert", alertMessage = errMsg) }
                    }
                } else {
                    val errMsg = try {
                        val json = JSONObject(result.responseContent ?: "{}")
                        json.optString("msg", json.optString("message", "Failed to update additional information"))
                    } catch (e: Exception) {
                        "Failed to update additional information"
                    }
                    _uiState.update { it.copy(alertTitle = "Alert", alertMessage = errMsg) }
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(isLoading = false, alertTitle = "Alert", alertMessage = e.message ?: "Failed to save additional info") }
            }
        }
    }

    private fun resolveFieldDestination(apiField: String?): String? {
        if (apiField == null) return null
        return when (apiField.lowercase(Locale.ROOT).trim()) {
            "firm_name", "fullname", "contact_person", "name", "email",
            "contact_phone", "mobile", "phone", "website", "bio_description",
            "firm_description", "bio", "about", "gender", "date_of_birth", "dob",
            "nationality", "house_flat_no", "address_house", "street", "address_street",
            "city_town", "address_city", "state", "address_state", "country", "address_country",
            "zipcode", "zip", "address_zip", "address", "correspondence_address", "mailing_address",
            "correspondence_house_flat_no", "correspondence_street", "correspondence_city",
            "correspondence_city_town", "correspondence_state", "correspondence_country",
            "correspondence_zipcode", "correspondence_zip" -> "basic"

            "profile_pic", "logo", "firm_logo", "profile_picture" -> "pic"

            "billing_currency", "default_currency", "consultation_fee",
            "bar_council_id", "reg_id", "registration_id", "years_of_experience",
            "years_of_incorporation", "experience", "practice_areas", "languages_spoken",
            "languages", "services_offered", "services", "practice_address", "office_address" -> "additional"

            "cases_handled", "case_types", "court_enrollments", "court_details" -> "courts_cases"

            "education", "degree", "university", "certifications", "certification",
            "awards", "award" -> "education"

            "availability", "weekly_schedule", "slot_duration", "working_hours",
            "work_slots" -> "availability"

            else -> null
        }
    }

    private fun resolveDestinationFromTab(redirectTab: String?): String {
        if (redirectTab == null) return "additional"
        return when (redirectTab.lowercase(Locale.ROOT).trim()) {
            "profile", "basic_profile", "firm_profile" -> "basic"
            "profile_pic", "logo" -> "pic"
            "practice", "practice_details", "additional_info", "additional", "practice_optional" -> "additional"
            "courts", "court_enrollments", "courts_cases" -> "courts_cases"
            "education", "education_awards", "awards" -> "education"
            "availability" -> "availability"
            else -> "additional"
        }
    }

    private fun openMissingFieldsDirectly() {
        val model = _uiState.value.firmProfileModel
        val nextStep = model?.data?.profile?.firm?.profile_completion?.next_step
            ?: model?.data?.profile?.profile_completion?.next_step

        var destination: String? = null
        val missingFields = nextStep?.missing_fields
        if (!missingFields.isNullOrEmpty()) {
            for (field in missingFields) {
                val dest = resolveFieldDestination(field)
                if (dest != null) {
                    destination = dest
                    break
                }
            }
        }

        if (destination == null) {
            val redirectTab = nextStep?.redirect_tab
            destination = resolveDestinationFromTab(redirectTab)
        }

        val isFirm = !_uiState.value.isMyProfile
        when (destination) {
            "basic" -> {
                populateProfileEditForm()
                _uiState.update {
                    it.copy(
                        isEditingProfileInfo = true,
                        isEditingAdditionalInfo = false
                    )
                }
            }
            "pic" -> {
                _uiState.update { it.copy(showPhotoChooser = true) }
            }
            "courts_cases" -> {
                populateAdditionalInfoForm()
                _uiState.update {
                    it.copy(
                        isEditingAdditionalInfo = true,
                        isEditingProfileInfo = false,
                        editSubTab = if (isFirm) "practice_details" else "courts_cases"
                    )
                }
            }
            "education" -> {
                populateAdditionalInfoForm()
                _uiState.update {
                    it.copy(
                        isEditingAdditionalInfo = true,
                        isEditingProfileInfo = false,
                        editSubTab = "education_awards"
                    )
                }
            }
            "availability" -> {
                populateAdditionalInfoForm()
                _uiState.update {
                    it.copy(
                        isEditingAdditionalInfo = true,
                        isEditingProfileInfo = false,
                        editSubTab = if (isFirm) "practice_details" else "availability"
                    )
                }
            }
            else -> {
                populateAdditionalInfoForm()
                _uiState.update {
                    it.copy(
                        isEditingAdditionalInfo = true,
                        isEditingProfileInfo = false,
                        editSubTab = "practice_details"
                    )
                }
            }
        }
    }

    fun loadProfile() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            val httpResult = repository.fetchProfile()
            _uiState.update { it.copy(isLoading = false) }

            if (httpResult.result == WebServiceHelper.ServiceCallStatus.Success) {
                try {
                    val result = JSONObject(httpResult.responseContent ?: "{}")
                    val isError = result.optBoolean("error", false)
                    if (!isError) {
                        val parsedModel = parseProfileModel(result)
                        Constants.firmProfileModel = parsedModel

                        // Sync Globals
                        val profile = parsedModel.data?.profile
                        val firm = profile?.firm
                        val freshName = profile?.name ?: ""
                        val freshFirmName = firm?.fullname ?: ""

                        if (!TextUtils.isEmpty(freshName)) {
                            Constants.NAME = freshName
                            Constants.ContactName = freshName
                        }
                        if (!TextUtils.isEmpty(freshFirmName)) {
                            Constants.FIRM_NAME = freshFirmName
                        }
                        val freshPicUrl = profile?.profile_pic_url?.takeIf { it.isNotBlank() }
                            ?: firm?.profile_pic_url?.takeIf { it.isNotBlank() }
                            ?: ""
                        Constants.firm_image = freshPicUrl
                        try {
                            val prefs = getApplication<Application>().getSharedPreferences("MyPrefs", android.content.Context.MODE_PRIVATE)
                            prefs.edit().putString("firm_image", freshPicUrl).apply()
                        } catch (_: Exception) {}
                        Constants.mainActivity?.updateTopBarProfile()

                        _uiState.update {
                            it.copy(
                                firmProfileModel = parsedModel,
                                isMyProfile = Constants.isMyProfileClicked
                            )
                        }
                        fetchBankAccount()
                    } else {
                        val msg = result.optString("message", "Unable to load profile data.")
                        _uiState.update {
                            it.copy(
                                alertTitle = "Alert",
                                alertMessage = msg
                            )
                        }
                    }
                } catch (e: Exception) {
                    _uiState.update {
                        it.copy(
                            alertTitle = "Alert",
                            alertMessage = e.message ?: "Failed to parse profile data."
                        )
                    }
                }
            } else {
                _uiState.update {
                    it.copy(
                        alertTitle = "Alert",
                        alertMessage = httpResult.responseContent ?: "Failed to connect to server."
                    )
                }
            }
        }
    }

    fun fetchBankAccount() {
        viewModelScope.launch {
            try {
                val result = repository.fetchBankAccount()
                if (result.result == WebServiceHelper.ServiceCallStatus.Success) {
                    val model = com.digicoffer.lauditor.FirmProfile.BankAccountModel.fromJson(result.responseContent ?: "{}")
                    val hasAcc = model != null && !model.isError && model.accountDetails != null &&
                            (!model.accountDetails.accountNumber.isNullOrBlank() || !model.accountDetails.accountId.isNullOrBlank())
                    _uiState.update {
                        it.copy(
                            hasBankAccount = hasAcc,
                            bankAccountModel = model
                        )
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    private fun uploadPhoto(file: File) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, showPhotoChooser = false) }
            val isFirm = !_uiState.value.isMyProfile
            val oldUrl = Constants.firm_image
            val result = repository.uploadProfilePhoto(file, isFirm)
            _uiState.update { it.copy(isLoading = false) }
            if (result.result == WebServiceHelper.ServiceCallStatus.Success) {
                if (!oldUrl.isNullOrEmpty()) {
                    AppImageCache.invalidate(null, oldUrl)
                }
                _uiState.update { it.copy(toastMessage = "Profile photo updated successfully") }
                loadProfile()
            } else {
                val errMsg = try {
                    val json = JSONObject(result.responseContent ?: "{}")
                    json.optString("msg", json.optString("message", "Failed to update profile photo"))
                } catch (e: Exception) {
                    "Failed to update profile photo"
                }
                _uiState.update { it.copy(toastMessage = errMsg) }
            }
        }
    }

    private fun deletePhoto() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, showDeletePhotoConfirm = false) }
            val isFirm = !_uiState.value.isMyProfile
            val oldUrl = Constants.firm_image
            val result = repository.deleteProfilePhoto(isFirm)
            _uiState.update { it.copy(isLoading = false) }
            if (result.result == WebServiceHelper.ServiceCallStatus.Success) {
                Constants.firm_image = ""
                val prefs = getApplication<Application>().getSharedPreferences("MyPrefs", android.content.Context.MODE_PRIVATE)
                prefs.edit().remove("firm_image").apply()
                if (!oldUrl.isNullOrEmpty()) {
                    AppImageCache.invalidate(null, oldUrl)
                }
                Constants.mainActivity?.updateTopBarProfile()
                _uiState.update { it.copy(toastMessage = "Profile photo removed successfully") }
                loadProfile()
            } else {
                val errMsg = try {
                    val json = JSONObject(result.responseContent ?: "{}")
                    json.optString("msg", json.optString("message", "Failed to remove profile photo"))
                } catch (e: Exception) {
                    "Failed to remove profile photo"
                }
                _uiState.update { it.copy(toastMessage = errMsg) }
            }
        }
    }

    private fun deleteAccount() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, showDeleteAccountConfirm = false) }
            val result = repository.deleteAccount()
            _uiState.update { it.copy(isLoading = false) }
            if (result.result == WebServiceHelper.ServiceCallStatus.Success) {
                val msg = try {
                    val json = JSONObject(result.responseContent ?: "{}")
                    json.optString("msg", "Account deletion request submitted. Will be completed in 2 weeks.")
                } catch (e: Exception) {
                    "Account deletion request submitted. Will be completed in 2 weeks."
                }
                _uiState.update {
                    it.copy(
                        alertTitle = "Success",
                        alertMessage = msg
                    )
                }
            } else {
                val errMsg = try {
                    val json = JSONObject(result.responseContent ?: "{}")
                    json.optString("msg", json.optString("message", "Failed to submit account deletion request"))
                } catch (e: Exception) {
                    "Failed to submit account deletion request"
                }
                _uiState.update {
                    it.copy(
                        alertTitle = "Alert",
                        alertMessage = errMsg
                    )
                }
            }
        }
    }

    private fun parseProfileModel(result: JSONObject): FirmProfileModel {
        val model = FirmProfileModel()
        model.error = result.optBoolean("error", false)

        val dataObj = result.optJSONObject("data") ?: return model
        val data = FirmProfileModel.Data()
        val profileObj = dataObj.optJSONObject("profile")

        if (profileObj != null) {
            val profile = FirmProfileModel.Profile()
            profile.uid = profileObj.optString("uid")
            profile.name = profileObj.optString("name")
            profile.profile_completion_percentage = profileObj.optInt("profile_completion_percentage", 0)
            profile.show_profile_banner = profileObj.optBoolean("show_profile_banner", true)
            profile.email = profileObj.optString("email")
            profile.mobile = profileObj.optString("mobile")
            profile.nationality = profileObj.optString("nationality")
            profile.date_of_birth = profileObj.optString("date_of_birth")
            profile.gender = profileObj.optString("gender")
            profile.bio_description = profileObj.optString("bio_description")
            profile.profile_pic_url = profileObj.optString("profile_pic_url", "")
            profile.bar_council_id = profileObj.optString("bar_council_id")
            profile.years_of_experience = profileObj.optInt("years_of_experience", 0)
            profile.accepted_t_c_date = profileObj.optString("accepted_t_c_date", profileObj.optString("accepted_t&c_date", ""))

            // Terms
            val termsObj = profileObj.optJSONObject("terms")
            if (termsObj != null) {
                val terms = FirmProfileModel.Terms()
                terms.isAccepted = termsObj.optBoolean("is_accepted", false)
                terms.accepted_at = termsObj.optString("accepted_at", "")
                terms.version = termsObj.optString("version", "")
                profile.terms = terms
                if (profile.accepted_t_c_date.isNullOrBlank()) {
                    profile.accepted_t_c_date = terms.accepted_at
                }
            }

            // Consultation Fee
            val feeObj = profileObj.optJSONObject("consultation_fee")
            if (feeObj != null) {
                val cf = FirmProfileModel.ConsultationFee()
                cf.amount = feeObj.optString("amount", "")
                cf.symbol = feeObj.optString("symbol", "₹")
                profile.consultation_fee = cf
            }

            // Subscription
            val firmObjForSub = profileObj.optJSONObject("firm")
            val subObj = profileObj.optJSONObject("subscription") ?: firmObjForSub?.optJSONObject("subscription")
            if (subObj != null) {
                val sub = FirmProfileModel.Subscription()
                sub.activatedOn = subObj.optString("activatedOn", subObj.optString("activated_on", ""))
                sub.model = subObj.optString("model", "")
                sub.isActive = subObj.optBoolean("isActive", subObj.optBoolean("is_active", false))
                sub.startDate = subObj.optString("startDate", subObj.optString("start_date", ""))
                sub.endDate = subObj.optString("endDate", subObj.optString("end_date", ""))
                sub.validityDays = subObj.optString("validityDays", subObj.optString("validity_days", ""))
                sub.nextBillingDate = subObj.optString("nextBillingDate", subObj.optString("next_billing_date", ""))
                sub.isCanUpgrade = subObj.optBoolean("canUpgrade", subObj.optBoolean("can_upgrade", false))
                sub.isCanDowngrade = subObj.optBoolean("canDowngrade", subObj.optBoolean("can_downgrade", false))
                sub.isCanPayNow = subObj.optBoolean("canPayNow", subObj.optBoolean("can_pay_now", false))

                val planObj = subObj.optJSONObject("plan")
                if (planObj != null) {
                    val plan = FirmProfileModel.Plan()
                    plan.amount = planObj.optInt("amount", 0)
                    plan.cycle = planObj.optString("cycle", "")
                    plan.name = planObj.optString("name", "")
                    plan.currencySymbol = planObj.optString("currencySymbol", "₹")
                    plan.currencyCode = planObj.optString("currencyCode", "INR")
                    sub.plan = plan
                }

                val payObj = subObj.optJSONObject("payment")
                if (payObj != null) {
                    val pay = FirmProfileModel.Payment()
                    pay.method = payObj.optString("method", "")
                    pay.maskedId = payObj.optString("maskedId", "")
                    sub.payment = pay
                }

                profile.subscription = sub
            }

            // Profile Completion
            val pcProfileObj = profileObj.optJSONObject("profile_completion")
            if (pcProfileObj != null) {
                val pc = FirmProfileModel.ProfileCompletion()
                pc.completion_percentage = pcProfileObj.optInt("completion_percentage", 0)
                profile.profile_completion = pc
            }

            profile.practice_areas = profileObj.optJSONArray("practice_areas")
            profile.languages_spoken = profileObj.optJSONArray("languages_spoken")
            profile.services_offered = profileObj.optJSONArray("services_offered")

            // Education
            val eduArr = profileObj.optJSONArray("education")
            if (eduArr != null) {
                val eduList = ArrayList<FirmProfileModel.Education>()
                for (i in 0 until eduArr.length()) {
                    val eObj = eduArr.optJSONObject(i) ?: continue
                    val edu = FirmProfileModel.Education()
                    edu.degree = eObj.optString("degree")
                    edu.university = eObj.optString("university")
                    edu.passing_year = eObj.optInt("passing_year")
                    eduList.add(edu)
                }
                profile.education = eduList
            }

            // Certifications
            val certArr = profileObj.optJSONArray("certifications")
            if (certArr != null) {
                val certList = ArrayList<FirmProfileModel.Certification>()
                for (i in 0 until certArr.length()) {
                    val cObj = certArr.optJSONObject(i) ?: continue
                    val cert = FirmProfileModel.Certification()
                    cert.certification_name = cObj.optString("certification_name")
                    cert.issuing_authority = cObj.optString("issuing_authority")
                    cert.year_of_issue = cObj.optInt("year_of_issue")
                    certList.add(cert)
                }
                profile.certifications = certList
            }

            // Awards (Profile-level)
            val awardArr = profileObj.optJSONArray("awards")
            if (awardArr != null) {
                val awardList = ArrayList<FirmProfileModel.Award>()
                for (i in 0 until awardArr.length()) {
                    val aObj = awardArr.optJSONObject(i) ?: continue
                    val aw = FirmProfileModel.Award()
                    aw.award_name = aObj.optString("award_name")
                    aw.year_of_award = aObj.optInt("year_of_award")
                    aw.purpose = aObj.optString("purpose")
                    awardList.add(aw)
                }
                profile.awards = awardList
            }

            // Cases Handled
            val casesArr = profileObj.optJSONArray("cases_handled")
            if (casesArr != null) {
                val casesList = ArrayList<String>()
                for (i in 0 until casesArr.length()) {
                    val cStr = casesArr.optString(i, "")
                    if (cStr.isNotBlank()) casesList.add(cStr)
                }
                profile.cases_handled = casesList
            }

            // Court Enrollments
            val courtArr = profileObj.optJSONArray("court_enrollments")
            if (courtArr != null) {
                val courtList = ArrayList<FirmProfileModel.CourtEnrollment>()
                for (i in 0 until courtArr.length()) {
                    val cObj = courtArr.optJSONObject(i) ?: continue
                    val ce = FirmProfileModel.CourtEnrollment()
                    ce.court_type = cObj.optString("court_type")
                    ce.court_name = cObj.optString("court_name")
                    ce.state = cObj.optString("state")
                    ce.city = cObj.optString("city")
                    courtList.add(ce)
                }
                profile.court_enrollments = courtList
            }

            // Availability
            val availObj = profileObj.optJSONObject("availability")
            if (availObj != null) {
                val avail = FirmProfileModel.Availability()
                avail.timezone = availObj.optString("timezone")
                avail.slot_duration = availObj.optInt("slot_duration", 0)
                avail.buffer_time = availObj.optInt("buffer_time", 0)
                avail.advance_booking_window_hours = availObj.optInt("advance_booking_window_hours", 0)

                val weekArr = availObj.optJSONArray("weekly_schedule")
                if (weekArr != null) {
                    val weekList = ArrayList<FirmProfileModel.WeeklySchedule>()
                    for (i in 0 until weekArr.length()) {
                        val dObj = weekArr.optJSONObject(i) ?: continue
                        val sched = FirmProfileModel.WeeklySchedule()
                        sched.date = dObj.optString("date")
                        sched.date_label = dObj.optString("date_label")
                        sched.day_of_week = dObj.optInt("day_of_week")
                        sched.day_name = dObj.optString("day_name")
                        sched.isIs_working_day = dObj.optBoolean("is_working_day", false)
                        sched.isIs_override = dObj.optBoolean("is_override", false)

                        val slotArr = dObj.optJSONArray("work_slots")
                        if (slotArr != null) {
                            val slots = ArrayList<FirmProfileModel.WorkSlot>()
                            for (j in 0 until slotArr.length()) {
                                val slObj = slotArr.optJSONObject(j) ?: continue
                                val sl = FirmProfileModel.WorkSlot()
                                sl.start_time = slObj.optString("start_time")
                                sl.end_time = slObj.optString("end_time")
                                slots.add(sl)
                            }
                            sched.work_slots = slots
                        }

                        val expArr = dObj.optJSONArray("expert_slots")
                        if (expArr != null) {
                            val expSlots = ArrayList<FirmProfileModel.WorkSlot>()
                            for (j in 0 until expArr.length()) {
                                val eObj = expArr.optJSONObject(j) ?: continue
                                val es = FirmProfileModel.WorkSlot()
                                es.start_time = eObj.optString("start_time")
                                es.end_time = eObj.optString("end_time")
                                expSlots.add(es)
                            }
                            sched.expert_slots = expSlots
                        }

                        weekList.add(sched)
                    }
                    avail.weekly_schedule = weekList
                }
                profile.availability = avail
            }

            // Firm
            val firmObj = profileObj.optJSONObject("firm")
            if (firmObj != null) {
                val firm = FirmProfileModel.Firm()
                firm.fullname = firmObj.optString("fullname")
                firm.email = firmObj.optString("email")
                firm.billing_currency = firmObj.optString("billing_currency")
                firm.contact_phone = firmObj.optString("contact_phone")
                firm.contact_person = firmObj.optString("contact_person")
                firm.firm_description = firmObj.optString("firm_description")
                firm.website = firmObj.optString("website")
                firm.profile_completion_percentage = firmObj.optInt("profile_completion_percentage", 0)
                firm.show_profile_banner = firmObj.optBoolean("show_profile_banner", true)
                firm.reg_id = firmObj.optString("reg_id")
                firm.years_of_incorporation = firmObj.optInt("years_of_incorporation", 0)
                firm.profile_pic_url = firmObj.optString("profile_pic_url", "")
                firm.practice_areas = firmObj.optJSONArray("practice_areas")
                firm.services_offered = firmObj.optJSONArray("services_offered")

                // Firm Profile Completion
                val pcObj = firmObj.optJSONObject("profile_completion")
                if (pcObj != null) {
                    val pc = FirmProfileModel.ProfileCompletion()
                    pc.completion_percentage = pcObj.optInt("completion_percentage", 0)
                    val nsObj = pcObj.optJSONObject("next_step")
                    if (nsObj != null) {
                        val ns = FirmProfileModel.NextStep()
                        ns.section = nsObj.optString("section")
                        ns.priority = nsObj.optInt("priority")
                        ns.redirect_tab = nsObj.optString("redirect_tab")
                        val mf = nsObj.optJSONArray("missing_fields")
                        if (mf != null) {
                            val fields = ArrayList<String>()
                            for (i in 0 until mf.length()) {
                                fields.add(mf.optString(i))
                            }
                            ns.missing_fields = fields
                        }
                        pc.next_step = ns
                    }
                    firm.profile_completion = pc
                }

                // Awards
                val firmAwardArr = firmObj.optJSONArray("awards")
                if (firmAwardArr != null) {
                    val awards = ArrayList<FirmProfileModel.Award>()
                    for (i in 0 until firmAwardArr.length()) {
                        val aObj = firmAwardArr.optJSONObject(i) ?: continue
                        val a = FirmProfileModel.Award()
                        a.award_name = aObj.optString("award_name")
                        a.year_of_award = aObj.optInt("year_of_award")
                        a.purpose = aObj.optString("purpose")
                        awards.add(a)
                    }
                    firm.awards = awards
                }

                // Registered Address
                val addrObj = firmObj.optJSONObject("address")
                if (addrObj != null) {
                    val addr = FirmProfileModel.Address()
                    addr.house_flat_no = addrObj.optString("house_flat_no")
                    addr.street = addrObj.optString("street")
                    addr.city_town = addrObj.optString("city_town")
                    addr.state = addrObj.optString("state")
                    addr.country = addrObj.optString("country")
                    addr.zipcode = addrObj.optString("zipcode")
                    firm.address = addr
                }

                // Correspondence Address
                val corrObj = firmObj.optJSONObject("correspondence_address")
                if (corrObj != null) {
                    val corr = FirmProfileModel.Address()
                    corr.house_flat_no = corrObj.optString("house_flat_no")
                    corr.street = corrObj.optString("street")
                    corr.city_town = corrObj.optString("city_town")
                    corr.state = corrObj.optString("state")
                    corr.country = corrObj.optString("country")
                    corr.zipcode = corrObj.optString("zipcode")
                    firm.correspondence_address = corr
                }

                profile.firm = firm
            }

            data.profile = profile
        }

        model.data = data
        return model
    }
}
