package com.digicoffer.lauditor.Email

import android.app.Activity
import android.content.Intent
import android.net.Uri
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
import com.digicoffer.lauditor.feature.email.data.repository.EmailRepository
import com.digicoffer.lauditor.feature.email.presentation.screen.EmailRoute
import com.digicoffer.lauditor.feature.email.presentation.state.EmailUiEvent
import com.digicoffer.lauditor.feature.email.presentation.viewmodel.EmailViewModel

class Email : Fragment() {

    private lateinit var viewModel: EmailViewModel
    private var isAwaitingAuthReturn = false

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
        titleViewModel?.setData("Emails")

        val repository = EmailRepository(requireContext())
        val factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                return EmailViewModel(repository) as T
            }
        }
        viewModel = ViewModelProvider(this, factory).get(EmailViewModel::class.java)

        return ComposeView(requireContext()).apply {
            setContent {
                LauditorTheme {
                    EmailRoute(
                        viewModel = viewModel,
                        onLaunchAuthUrl = { url ->
                            launchAuthUrl(url)
                        }
                    )
                }
            }
        }
    }

    private fun launchAuthUrl(authUrl: String) {
        try {
            isAwaitingAuthReturn = true
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(authUrl))
            startActivityForResult(intent, AUTH_REQUEST_CODE)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    override fun onResume() {
        super.onResume()
        if (isAwaitingAuthReturn && ::viewModel.isInitialized) {
            isAwaitingAuthReturn = false
            viewModel.onEvent(EmailUiEvent.CheckAuthReturn)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == AUTH_REQUEST_CODE && ::viewModel.isInitialized) {
            isAwaitingAuthReturn = false
            viewModel.onEvent(EmailUiEvent.CheckAuthReturn)
        }
    }

    /**
     * Retained for backward-compatibility with AndroidUtils.showEmailAlert
     */
    fun send_email() {
        if (::viewModel.isInitialized) {
            viewModel.onEvent(EmailUiEvent.ConfirmSendNoSubject)
        }
    }

    companion object {
        private const val AUTH_REQUEST_CODE = 1001
        @JvmField
        var progress_dialog: android.app.Dialog? = null
    }
}
