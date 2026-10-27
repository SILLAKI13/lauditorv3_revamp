package com.digicoffer.lauditor.Chat.ViewModels

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.OnBackPressedCallback
import androidx.compose.ui.platform.ComposeView
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.digicoffer.lauditor.Chat.VideoCallInterface
import com.digicoffer.lauditor.CommonFiles.ChatService.ChatConnection
import com.digicoffer.lauditor.CommonFiles.GlobalFiles.AndroidUtils
import com.digicoffer.lauditor.CommonFiles.GlobalFiles.Constants
import com.digicoffer.lauditor.CommonFiles.GlobalFiles.NewModel
import com.digicoffer.lauditor.R
import com.digicoffer.lauditor.core.designsystem.theme.LauditorTheme
import com.digicoffer.lauditor.feature.messages.presentation.screen.MessagesScreen
import com.digicoffer.lauditor.feature.messages.presentation.viewmodel.MessagesViewModel

class Chat : Fragment(), VideoCallInterface {

    private var mViewModel: NewModel? = null
    private lateinit var viewModel: MessagesViewModel

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
        Constants.Chat_id = ""
        val title = "Messages"
        mViewModel?.setData(title)
        requireActivity().title = title

        try {
            val prefs = android.preference.PreferenceManager.getDefaultSharedPreferences(requireContext())
            var uid = prefs.getString("xmpp_jid", null)
            var pass = prefs.getString("xmpp_password", null)
            if (uid.isNullOrEmpty()) {
                uid = Constants.UID ?: ""
                if (!Constants.ROLE.equals("admin", ignoreCase = true)) {
                    uid = uid + "_" + (Constants.USER_ID ?: "")
                }
            }
            if (pass.isNullOrEmpty()) {
                pass = Constants.TOKEN
            }
            if (!uid.isNullOrEmpty() && !pass.isNullOrEmpty()) {
                if (ChatConnection.mConnection == null || !ChatConnection.mConnection!!.isAuthenticated) {
                    ChatConnection.loginUser(requireContext().applicationContext, uid, pass)
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }

        if (!Constants.is_active) {
            AndroidUtils.showRenewalPopup(requireActivity())
        }

        viewModel = ViewModelProvider(this).get(MessagesViewModel::class.java)
        val initialTab = if (Constants.isClient_chat) com.digicoffer.lauditor.feature.messages.data.model.MessagesTab.CLIENTS else com.digicoffer.lauditor.feature.messages.data.model.MessagesTab.TEAMS
        viewModel.setTab(initialTab)

        return ComposeView(requireContext()).apply {
            setContent {
                LauditorTheme {
                    MessagesScreen(
                        viewModel = viewModel,
                        onNavigateBack = {
                            requireActivity().onBackPressedDispatcher.onBackPressed()
                        }
                    )
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        if (::viewModel.isInitialized) {
            val tab = if (Constants.isClient_chat) com.digicoffer.lauditor.feature.messages.data.model.MessagesTab.CLIENTS else com.digicoffer.lauditor.feature.messages.data.model.MessagesTab.TEAMS
            viewModel.setTab(tab)
        }
    }

    override fun onVideoCallClicked() {
        val permissionsLocal = arrayOf(
            android.Manifest.permission.CAMERA,
            android.Manifest.permission.RECORD_AUDIO
        )

        if (AndroidUtils.hasPermissions(permissionsLocal, requireContext())) {
            AndroidUtils.loadWebView(requireContext(), requireActivity())
        } else {
            AndroidUtils.showAlertDialog(view, requireActivity())
        }
    }
}
