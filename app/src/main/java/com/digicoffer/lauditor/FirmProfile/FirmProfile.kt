package com.digicoffer.lauditor.FirmProfile

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.OnBackPressedCallback
import androidx.compose.ui.platform.ComposeView
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentTransaction
import androidx.lifecycle.ViewModelProvider
import com.digicoffer.lauditor.CommonFiles.GlobalFiles.Constants
import com.digicoffer.lauditor.CommonFiles.GlobalFiles.NewModel
import com.digicoffer.lauditor.R
import com.digicoffer.lauditor.core.designsystem.theme.LauditorTheme
import com.digicoffer.lauditor.feature.profile.presentation.screen.PracticePartnerScreen
import com.digicoffer.lauditor.feature.profile.presentation.screen.ProfileScreen
import com.digicoffer.lauditor.feature.profile.presentation.state.ProfileUiEvent
import com.digicoffer.lauditor.feature.profile.presentation.viewmodel.ProfileViewModel

class FirmProfile : Fragment() {

    private var mViewModel: NewModel? = null
    private lateinit var viewModel: ProfileViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mViewModel = ViewModelProvider(requireActivity()).get(NewModel::class.java)
        setupOnBackPressed()
    }

    private fun setupOnBackPressed() {
        requireActivity().onBackPressedDispatcher.addCallback(
            this,
            object : OnBackPressedCallback(true) {
                override fun handleOnBackPressed() {
                    if (isEnabled) {
                        isEnabled = false
                        requireActivity().onBackPressedDispatcher.onBackPressed()
                    }
                }
            }
        )
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val isPp = "Pp".equals(Constants.Profile_View, ignoreCase = true)

        val title = when {
            isPp -> getString(R.string.practice_partners)
            Constants.isMyProfileClicked -> getString(R.string.my_profile)
            else -> getString(R.string.firm_profile)
        }
        mViewModel?.setData(title)
        requireActivity().title = title

        viewModel = ViewModelProvider(this).get(ProfileViewModel::class.java)

        if (isPp) {
            viewModel.onEvent(ProfileUiEvent.SetPracticePartnerMode(true))
        } else {
            viewModel.onEvent(ProfileUiEvent.SetPracticePartnerMode(false))
            viewModel.onEvent(ProfileUiEvent.SetMyProfileMode(Constants.isMyProfileClicked))
        }

        return ComposeView(requireContext()).apply {
            setContent {
                LauditorTheme {
                    if (isPp) {
                        PracticePartnerScreen(
                            viewModel = viewModel,
                            onNavigateBack = {
                                requireActivity().onBackPressedDispatcher.onBackPressed()
                            }
                        )
                    } else {
                        ProfileScreen(
                            viewModel = viewModel,
                            onNavigateBack = {
                                requireActivity().onBackPressedDispatcher.onBackPressed()
                            }
                        )
                    }
                }
            }
        }
    }

    // ── Compatibility stubs for legacy sub-fragments ──────────────────────
    fun loadBasicProfile() {}
    fun loadPracticePartners() {}
    fun ChangeBackGround() {}
    fun NavFragment(fragment: Fragment) {
        try {
            val ft = childFragmentManager.beginTransaction()
            ft.replace(R.id.flFirmProfile, fragment)
            ft.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE)
            ft.addToBackStack(null)
            ft.commit()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}
