package com.digicoffer.lauditor.Invoice.ViewModels

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.ui.platform.ComposeView
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.digicoffer.lauditor.CommonFiles.GlobalFiles.NewModel
import com.digicoffer.lauditor.core.designsystem.theme.LauditorTheme
import com.digicoffer.lauditor.feature.invoice.data.repository.InvoiceRepository
import com.digicoffer.lauditor.feature.invoice.presentation.screen.InvoiceRoute
import com.digicoffer.lauditor.feature.invoice.presentation.viewmodel.InvoiceViewModel

class ViewInvoice : Fragment() {

    private lateinit var viewModel: InvoiceViewModel

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val titleViewModel = try {
            ViewModelProvider(requireActivity()).get(NewModel::class.java)
        } catch (e: Exception) {
            null
        }
        titleViewModel?.setData("Invoices")

        val repository = InvoiceRepository(requireContext())
        val factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                return InvoiceViewModel(repository, requireContext()) as T
            }
        }
        viewModel = ViewModelProvider(this, factory).get(InvoiceViewModel::class.java)

        return ComposeView(requireContext()).apply {
            setViewCompositionStrategy(androidx.compose.ui.platform.ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed)
            setContent {
                LauditorTheme {
                    InvoiceRoute(viewModel = viewModel)
                }
            }
        }
    }
}
