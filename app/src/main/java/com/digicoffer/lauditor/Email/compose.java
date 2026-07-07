package com.digicoffer.lauditor.Email;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import com.digicoffer.lauditor.R;

class ComposeFragment extends Fragment {

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.compose, container, false);

        @SuppressLint({"MissingInflatedId", "LocalSuppress"}) ImageView closeDocuments = view.findViewById(R.id.compose);
        closeDocuments.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                try {
                    // Replace the current fragment with a new instance of ComposeFragment
                    FragmentManager fragmentManager = requireActivity().getSupportFragmentManager();
                    fragmentManager.beginTransaction()
                            .replace(R.id.child_container, new ComposeFragment())
                            .addToBackStack(null) // Adds the transaction to the back stack
                            .commit();
                } catch (Exception e) {
                    e.fillInStackTrace();
                    // Handle the exception gracefully, for example, by showing an error message
                    Toast.makeText(getActivity(), "Failed to open compose fragment", Toast.LENGTH_SHORT).show();
                }
            }
        });

        return view;
    }
}
