package com.digicoffer.lauditor.Settings

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.OnBackPressedCallback
import androidx.compose.ui.platform.ComposeView
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.digicoffer.lauditor.CommonFiles.GlobalFiles.NewModel
import com.digicoffer.lauditor.R
import com.digicoffer.lauditor.core.designsystem.theme.LauditorTheme
import com.digicoffer.lauditor.feature.settings.presentation.screen.SettingsScreen
import com.digicoffer.lauditor.feature.settings.presentation.viewmodel.SettingsViewModel

class Settings : Fragment() {

    private var mViewModel: NewModel? = null
    private lateinit var viewModel: SettingsViewModel

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
        val title = getString(R.string.settings)
        mViewModel?.setData(title)
        requireActivity().title = title

        viewModel = ViewModelProvider(this).get(SettingsViewModel::class.java)

        return ComposeView(requireContext()).apply {
            setContent {
                LauditorTheme {
                    SettingsScreen(
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
