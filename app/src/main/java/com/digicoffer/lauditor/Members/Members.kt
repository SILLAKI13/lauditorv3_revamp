package com.digicoffer.lauditor.Members

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.ui.platform.ComposeView
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.digicoffer.lauditor.CommonFiles.GlobalFiles.NewModel
import com.digicoffer.lauditor.R
import com.digicoffer.lauditor.feature.members.data.repository.MembersRepository
import com.digicoffer.lauditor.feature.members.presentation.screen.MembersScreen
import com.digicoffer.lauditor.feature.members.presentation.viewmodel.MembersViewModel

class Members : Fragment() {
    private var mViewModel: NewModel? = null
    private lateinit var viewModel: MembersViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mViewModel = ViewModelProvider(requireActivity()).get(NewModel::class.java)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        mViewModel?.setData(getString(R.string.view_members))
        requireActivity().title = getString(R.string.view_members)

        val repository = MembersRepository(requireContext())
        val factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                return MembersViewModel(repository) as T
            }
        }
        viewModel = ViewModelProvider(this, factory).get(MembersViewModel::class.java)

        return ComposeView(requireContext()).apply {
            setContent {
                com.digicoffer.lauditor.core.designsystem.theme.LauditorTheme {
                    MembersScreen(viewModel = viewModel)
                }
            }
        }
    }

    companion object {
        var FLAG = ""
    }
}
