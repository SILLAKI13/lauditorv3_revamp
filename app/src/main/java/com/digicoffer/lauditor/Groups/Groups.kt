package com.digicoffer.lauditor.Groups

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.ui.platform.ComposeView
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.digicoffer.lauditor.CommonFiles.GlobalFiles.NewModel
import com.digicoffer.lauditor.R
import com.digicoffer.lauditor.feature.groups.presentation.screen.GroupsScreen
import com.digicoffer.lauditor.feature.groups.presentation.viewmodel.GroupsViewModel

class Groups : Fragment() {
    private var mViewModel: NewModel? = null
    private lateinit var viewModel: GroupsViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mViewModel = ViewModelProvider(requireActivity()).get(NewModel::class.java)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        mViewModel?.setData("View Groups")
        requireActivity().title = "View Groups"

        viewModel = ViewModelProvider(this).get(GroupsViewModel::class.java)

        return ComposeView(requireContext()).apply {
            setContent {
                com.digicoffer.lauditor.core.designsystem.theme.LauditorTheme {
                    GroupsScreen(
                        viewModel = viewModel,
                        onBack = {
                            requireActivity().onBackPressedDispatcher.onBackPressed()
                        }
                    )
                }
            }
        }
    }
}
