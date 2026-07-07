package com.digicoffer.lauditor.CommonFiles.GlobalFiles;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.digicoffer.lauditor.R;

public class Disabled_view extends Fragment {
    public TextView frozen_PageText;
    private View view;
    private final String frozenText;
    boolean isred = false;

    public Disabled_view(String frozenText, boolean isRed) {
        this.frozenText = frozenText;
        isred = isRed;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.disabled_view, container, false);
        frozen_PageText = view.findViewById(R.id.frozen_PageText);
//        frozen_PageText.setText(R.string.timesheet_already_submitted_please_select_other_week);
        frozen_PageText.setText(frozenText);
        if (isred)
            frozen_PageText.setTextColor(getResources().getColor(R.color.Red));
        return view;
    }
}