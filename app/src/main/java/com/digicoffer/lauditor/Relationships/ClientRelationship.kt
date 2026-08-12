package com.digicoffer.lauditor.Relationships

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.ui.platform.ComposeView
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.digicoffer.lauditor.CommonFiles.GlobalFiles.NewModel
import com.digicoffer.lauditor.feature.relationships.presentation.screen.RelationshipsScreen
import com.digicoffer.lauditor.feature.relationships.presentation.viewmodel.RelationshipsViewModel

class ClientRelationship : Fragment() {
    private var mViewModel: NewModel? = null
    private lateinit var viewModel: RelationshipsViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mViewModel = ViewModelProvider(requireActivity()).get(NewModel::class.java)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        mViewModel?.setData("Relationships")
        requireActivity().title = "Relationships"

        viewModel = ViewModelProvider(this).get(RelationshipsViewModel::class.java)

        return ComposeView(requireContext()).apply {
            setContent {
                com.digicoffer.lauditor.core.designsystem.theme.LauditorTheme {
                    RelationshipsScreen(
                        viewModel = viewModel,
                        onNavigateBack = {
                            requireActivity().onBackPressedDispatcher.onBackPressed()
                        },
                        onTitleChange = { title ->
                            mViewModel?.setData(title)
                            requireActivity().title = title
                        }
                    )
                }
            }
        }
    }
}
