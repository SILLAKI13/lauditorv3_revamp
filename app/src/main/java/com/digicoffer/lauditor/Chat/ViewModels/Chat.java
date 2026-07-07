package com.digicoffer.lauditor.Chat.ViewModels;

import static com.digicoffer.lauditor.CommonFiles.GlobalFiles.AndroidUtils.PERMISSION_REQUEST_CAMERA_AUDIO;
import static com.digicoffer.lauditor.CommonFiles.GlobalFiles.AndroidUtils.hasPermissions;
import static com.digicoffer.lauditor.CommonFiles.GlobalFiles.AndroidUtils.loadWebView;
import static com.digicoffer.lauditor.CommonFiles.GlobalFiles.AndroidUtils.showAlertDialog;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.lifecycle.ViewModelProvider;

import com.digicoffer.lauditor.Chat.VideoCallInterface;
import com.digicoffer.lauditor.CommonFiles.GlobalFiles.NewModel;
import com.digicoffer.lauditor.R;
import com.digicoffer.lauditor.CommonFiles.GlobalFiles.Constants;

import java.util.Locale;

public class Chat extends Fragment implements VideoCallInterface {

    TextView tv_client, tv_team;
    private NewModel mViewModel;
    ImageView btn_videocall;
    String client_id = "";
    String guid = "";

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.chat, container, false);
        Constants.Chat_id = "";
        mViewModel = new ViewModelProvider(requireActivity()).get(NewModel.class);
        mViewModel.setData("Messages");
        tv_client = view.findViewById(R.id.tv_client);
        tv_client.setText(R.string.client);
        tv_team = view.findViewById(R.id.tv_team);
        tv_team.setText(R.string.teams);
        btn_videocall = view.findViewById(R.id.btn_videocall);

        // Check if client or teams view should be loaded
        if (Constants.isFromNotification) {
            handleNotificationNavigation();
        } else {
            if (Constants.isClient_chat)
                loadClient();
            else
                loadTeams();
            if (Constants.ROLE.equals("AAM")) {
                tv_client.setVisibility(View.GONE);
                loadTeams();
                tv_team.setBackgroundDrawable(requireContext().getResources().getDrawable(R.drawable.rounder_button_green));
            } else if ("solo".equals(Constants.CATEGORY)) {
                loadClient();
                tv_team.setVisibility(View.GONE);
                tv_client.setBackgroundDrawable(requireContext().getResources().getDrawable(R.drawable.rounder_button_green));
            }
        }

        // Click listeners for navigation buttons
        tv_client.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Constants.isClient_chat = true;
                loadClient();
            }
        });
        tv_team.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Constants.isClient_chat = false;
                loadTeams();
            }
        });

        // Click listener for video call button
        btn_videocall.setOnClickListener(v -> {

            String[] permissions_local = {
                    Manifest.permission.CAMERA,
                    Manifest.permission.RECORD_AUDIO
            };

            if (hasPermissions(permissions_local, getContext())) {
                loadWebView(getContext(), getActivity());   // ✅ THIS starts the video call
            } else {
                showAlertDialog(v, getActivity());
            }
        });


        return view;
    }

    /**
     * Handles a tap on a push notification. Previously this only picked
     * which LIST (Clients vs Teams) to show, based on "route", but never
     * used guid/client_id to actually open the specific conversation the
     * notification was about - so tapping a notification just landed on
     * the generic list, not the relevant chat.
     * <p>
     * Fix: reuse the same pendingChatJid mechanism loadClient()/loadTeams()
     * already use for the "appointment chat icon -> skip list, go straight
     * to MessagesList" flow, so a notification tap does the same thing.
     * <p>
     * NOTE: no contact display-name field was found in the notification
     * bundle (only route/guid/client_id were confirmed present via
     * Logcat). Using guid itself as a fallback display name so this
     * compiles and works correctly now - replace with a real name field
     * once one is confirmed to exist in the payload.
     */
    private void handleNotificationNavigation() {
        Bundle bundle = Constants.notificationBundle;
        String route = bundle.getString(Constants.NavKeys.ROUTE_NAME);
        guid = bundle.getString(Constants.NavKeys.GUID);
        client_id = bundle.getString(Constants.NavKeys.CLIENT_ID);
        assert route != null;

        // Reuse the existing, proven "skip list, go straight to
        // MessagesList" mechanism instead of just opening the generic list.
        Constants.pendingChatJid = guid != null ? guid : "";
        Constants.pendingChatName = guid != null ? guid : ""; // TODO: replace with real name field once confirmed
        Constants.pendingChatSource = route.equals("message_client_inbox") ? "relationship" : "relationship";

        if (route.equals("message_client_inbox")) {
            loadClient();
        } else {
            loadTeams();
        }
    }

    // Method to check and request camera and audio permissions
    private void checkCameraAndAudioPermissions() {
        if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED ||
                ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {

            // Request permissions
            requestPermissions(new String[]{Manifest.permission.CAMERA, Manifest.permission.RECORD_AUDIO},
                    PERMISSION_REQUEST_CAMERA_AUDIO);
        } else {
            // Permissions already granted
        }
    }

    // Handle permission request results
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == PERMISSION_REQUEST_CAMERA_AUDIO) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED &&
                    grantResults[1] == PackageManager.PERMISSION_GRANTED) {
                // Permissions granted
            } else {
                Toast.makeText(requireContext(), "Permissions denied", Toast.LENGTH_SHORT).show();
            }
        }
    }

    @NonNull
    private static String getString(String loginType, String token, String jid) {
        String name = (Constants.NAME + " ").replace(" ", "%20");
        String finalUrl = Constants.AVChatUrl + "?logintype=" + loginType + "&token=" + token + "&jid=" + jid + "&name=" + name + "&hideclient=" + Constants.isAdmin + "&plan=lauditor" + "&category=" + Constants.CATEGORY.toLowerCase(Locale.ROOT);
        return finalUrl;
    }


    // Method to load client view
    private void loadClient() {
        tv_team.setBackgroundDrawable(requireContext().getResources().getDrawable(R.drawable.button_right_background));
        tv_client.setBackgroundDrawable(requireContext().getResources().getDrawable(R.drawable.button_left_green_background));
        tv_client.setTextColor(requireContext().getColor(R.color.white));
        tv_team.setTextColor(requireContext().getColor(R.color.black));

        // Normal flow — load Clients list
        FragmentTransaction ft = getChildFragmentManager().beginTransaction();
        ft.setCustomAnimations(R.anim.fade_in, R.anim.fade_out, R.anim.slide_in, R.anim.slide_out);
        ft.replace(R.id.child_container_timesheets, new Clients());
        ft.addToBackStack(null);
        ft.commit();
        // ✅ If coming from appointment chat icon OR a push notification,
        //    skip Clients list and go straight to MessagesList
        if (!Constants.pendingChatJid.isEmpty()) {
            String jid = Constants.pendingChatJid;
            String name = Constants.pendingChatName;
            String source = Constants.pendingChatSource;
            // Reset so it doesn't trigger again on back-press
            Constants.pendingChatJid = "";
            Constants.pendingChatName = "";
            Constants.pendingChatSource = "";

            MessagesList frag = new MessagesList(null);
            Bundle bundle = new Bundle();
            bundle.putString("EXTRA_CONTACT_JID", jid);
            bundle.putString("EXTRA_CONTACT_NAME", name);
            bundle.putString("EXTRA_CONTACT_TYPE", source);
            frag.setArguments(bundle);

            requireActivity().getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.id_framelayout, frag)
                    .addToBackStack(null)
                    .commit();
            return; // ← skip loading Clients fragment
        }
    }

    // Method to load teams view
    private void loadTeams() {
        tv_team.setBackgroundDrawable(requireContext().getResources().getDrawable(R.drawable.button_right_green_count));
        tv_client.setBackgroundDrawable(requireContext().getResources().getDrawable(R.drawable.button_left_background));
        tv_team.setTextColor(requireContext().getColor(R.color.white));
        tv_client.setTextColor(requireContext().getColor(R.color.black));
        FragmentTransaction fragmentTransaction = getChildFragmentManager().beginTransaction();
        fragmentTransaction.setCustomAnimations(
                R.anim.fade_in,  // Enter animation
                R.anim.fade_out, // Exit animation
                R.anim.slide_in, // Pop-enter animation
                R.anim.slide_out // Pop-exit animation
        );
        Teams clients = new Teams();
        fragmentTransaction.replace(R.id.child_container_timesheets, clients);
        fragmentTransaction.addToBackStack(null);
        fragmentTransaction.commit();

        // ✅ Same skip-to-MessagesList handling for team notifications/links
        if (!Constants.pendingChatJid.isEmpty()) {
            String jid = Constants.pendingChatJid;
            String name = Constants.pendingChatName;
            String source = Constants.pendingChatSource;
            Constants.pendingChatJid = "";
            Constants.pendingChatName = "";
            Constants.pendingChatSource = "";

            MessagesList frag = new MessagesList(null);
            Bundle bundle = new Bundle();
            bundle.putString("EXTRA_CONTACT_JID", jid);
            bundle.putString("EXTRA_CONTACT_NAME", name);
            bundle.putString("EXTRA_CONTACT_TYPE", source);
            frag.setArguments(bundle);

            requireActivity().getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.id_framelayout, frag)
                    .addToBackStack(null)
                    .commit();
        }
    }


    @Override
    public void onVideoCallClicked() {
        String[] permissions_local = {
                Manifest.permission.CAMERA,
                Manifest.permission.RECORD_AUDIO
        };

        if (hasPermissions(permissions_local, getContext())) {
            loadWebView(getContext(), getActivity());   // ✅ TRIGGER WEBVIEW
        } else {
            showAlertDialog(getView(), getActivity());
        }
    }
}