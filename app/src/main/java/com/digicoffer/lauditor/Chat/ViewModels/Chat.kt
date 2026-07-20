package com.digicoffer.lauditor.Chat.ViewModels

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.digicoffer.lauditor.Chat.VideoCallInterface
import com.digicoffer.lauditor.CommonFiles.GlobalFiles.AndroidUtils
import com.digicoffer.lauditor.CommonFiles.GlobalFiles.Constants
import com.digicoffer.lauditor.CommonFiles.GlobalFiles.NewModel
import com.digicoffer.lauditor.R
import java.util.Locale

class Chat : Fragment(), VideoCallInterface {

    private var tv_client: TextView? = null
    private var tv_team: TextView? = null
    private var mViewModel: NewModel? = null
    private var btn_videocall: ImageView? = null
    var client_id: String = ""
    var guid: String = ""

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.chat, container, false)
        Constants.Chat_id = ""
        mViewModel = ViewModelProvider(requireActivity()).get(NewModel::class.java)
        mViewModel?.setData("Messages")
        tv_client = view.findViewById(R.id.tv_client)
        tv_client?.setText(R.string.client)
        tv_team = view.findViewById(R.id.tv_team)
        tv_team?.setText(R.string.teams)
        btn_videocall = view.findViewById(R.id.btn_videocall)

        if (Constants.isFromNotification) {
            handleNotificationNavigation()
        } else {
            if (Constants.isClient_chat) {
                loadClient()
            } else {
                loadTeams()
            }
            if (Constants.ROLE == "AAM") {
                tv_client?.visibility = View.GONE
                loadTeams()
                tv_team?.background = requireContext().resources.getDrawable(R.drawable.rounder_button_green)
            } else if ("solo" == Constants.CATEGORY) {
                loadClient()
                tv_team?.visibility = View.GONE
                tv_client?.background = requireContext().resources.getDrawable(R.drawable.rounder_button_green)
            }
        }

        tv_client?.setOnClickListener {
            Constants.isClient_chat = true
            loadClient()
        }
        tv_team?.setOnClickListener {
            Constants.isClient_chat = false
            loadTeams()
        }

        btn_videocall?.setOnClickListener {
            val permissionsLocal = arrayOf(
                Manifest.permission.CAMERA,
                Manifest.permission.RECORD_AUDIO
            )

            if (AndroidUtils.hasPermissions(permissionsLocal, requireContext())) {
                AndroidUtils.loadWebView(requireContext(), requireActivity())
            } else {
                AndroidUtils.showAlertDialog(it, requireActivity())
            }
        }

        return view
    }

    private fun handleNotificationNavigation() {
        val bundle = Constants.notificationBundle ?: return
        val route = bundle.getString(Constants.NavKeys.ROUTE_NAME) ?: ""
        guid = bundle.getString(Constants.NavKeys.GUID) ?: ""
        client_id = bundle.getString(Constants.NavKeys.CLIENT_ID) ?: ""

        Constants.pendingChatJid = guid
        Constants.pendingChatName = guid
        Constants.pendingChatSource = if (route == "message_client_inbox") "relationship" else "relationship"

        if (route == "message_client_inbox") {
            loadClient()
        } else {
            loadTeams()
        }
    }

    private fun checkCameraAndAudioPermissions() {
        if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED ||
            ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED
        ) {
            requestPermissions(
                arrayOf(Manifest.permission.CAMERA, Manifest.permission.RECORD_AUDIO),
                AndroidUtils.PERMISSION_REQUEST_CAMERA_AUDIO
            )
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        if (requestCode == AndroidUtils.PERMISSION_REQUEST_CAMERA_AUDIO) {
            if (grantResults.size > 1 &&
                grantResults[0] == PackageManager.PERMISSION_GRANTED &&
                grantResults[1] == PackageManager.PERMISSION_GRANTED
            ) {
                // Permissions granted
            } else {
                Toast.makeText(requireContext(), "Permissions denied", Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onVideoCallClicked() {
        val permissionsLocal = arrayOf(
            Manifest.permission.CAMERA,
            Manifest.permission.RECORD_AUDIO
        )

        if (AndroidUtils.hasPermissions(permissionsLocal, requireContext())) {
            AndroidUtils.loadWebView(requireContext(), requireActivity())
        } else {
            AndroidUtils.showAlertDialog(view, requireActivity())
        }
    }

    private fun loadClient() {
        tv_team?.background = requireContext().resources.getDrawable(R.drawable.button_right_background)
        tv_client?.background = requireContext().resources.getDrawable(R.drawable.button_left_green_background)
        tv_client?.setTextColor(requireContext().getColor(R.color.white))
        tv_team?.setTextColor(requireContext().getColor(R.color.black))

        val ft = childFragmentManager.beginTransaction()
        ft.setCustomAnimations(R.anim.fade_in, R.anim.fade_out, R.anim.slide_in, R.anim.slide_out)
        ft.replace(R.id.child_container_timesheets, Clients())
        ft.addToBackStack(null)
        ft.commit()

        val pendingJid = Constants.pendingChatJid
        if (!pendingJid.isNullOrEmpty()) {
            val jid = Constants.pendingChatJid ?: ""
            val name = Constants.pendingChatName ?: ""
            val source = Constants.pendingChatSource ?: ""
            Constants.pendingChatJid = ""
            Constants.pendingChatName = ""
            Constants.pendingChatSource = ""

            val frag = MessagesList(null)
            val bundle = Bundle().apply {
                putString("EXTRA_CONTACT_JID", jid)
                putString("EXTRA_CONTACT_NAME", name)
                putString("EXTRA_CONTACT_TYPE", source)
            }
            frag.arguments = bundle

            requireActivity().supportFragmentManager
                .beginTransaction()
                .replace(R.id.id_framelayout, frag)
                .addToBackStack(null)
                .commit()
        }
    }

    private fun loadTeams() {
        tv_team?.background = requireContext().resources.getDrawable(R.drawable.button_right_green_count)
        tv_client?.background = requireContext().resources.getDrawable(R.drawable.button_left_background)
        tv_team?.setTextColor(requireContext().getColor(R.color.white))
        tv_client?.setTextColor(requireContext().getColor(R.color.black))

        val ft = childFragmentManager.beginTransaction()
        ft.setCustomAnimations(R.anim.fade_in, R.anim.fade_out, R.anim.slide_in, R.anim.slide_out)
        ft.replace(R.id.child_container_timesheets, Teams())
        ft.addToBackStack(null)
        ft.commit()

        val pendingJid = Constants.pendingChatJid
        if (!pendingJid.isNullOrEmpty()) {
            val jid = Constants.pendingChatJid ?: ""
            val name = Constants.pendingChatName ?: ""
            val source = Constants.pendingChatSource ?: ""
            Constants.pendingChatJid = ""
            Constants.pendingChatName = ""
            Constants.pendingChatSource = ""

            val frag = MessagesList(null)
            val bundle = Bundle().apply {
                putString("EXTRA_CONTACT_JID", jid)
                putString("EXTRA_CONTACT_NAME", name)
                putString("EXTRA_CONTACT_TYPE", source)
            }
            frag.arguments = bundle

            requireActivity().supportFragmentManager
                .beginTransaction()
                .replace(R.id.id_framelayout, frag)
                .addToBackStack(null)
                .commit()
        }
    }

    companion object {
        private fun getString(loginType: String, token: String, jid: String): String {
            val name = (Constants.NAME + " ").replace(" ", "%20")
            val cat = Constants.CATEGORY ?: ""
            return Constants.AVChatUrl + "?logintype=" + loginType + "&token=" + token + "&jid=" + jid + "&name=" + name + "&hideclient=" + Constants.isAdmin + "&plan=lauditor" + "&category=" + cat.lowercase(Locale.ROOT)
        }
    }
}
