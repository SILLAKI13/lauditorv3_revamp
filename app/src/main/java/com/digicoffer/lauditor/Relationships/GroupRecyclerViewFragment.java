package com.digicoffer.lauditor.Relationships;

import android.content.Context;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatButton;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.digicoffer.lauditor.Relationships.Adapter.RelationshipsAdapter;
import com.digicoffer.lauditor.Relationships.Model.RelationshipsModel;
import com.digicoffer.lauditor.Groups.Models.ViewGroupModel;
import com.digicoffer.lauditor.Members.Adapters.GroupsAdapter;
import com.digicoffer.lauditor.R;
import com.digicoffer.lauditor.CommonFiles.GlobalFiles.DynamicUtils;
import com.google.android.material.textfield.TextInputEditText;

import org.json.JSONException;

import java.util.ArrayList;
import java.util.concurrent.atomic.AtomicBoolean;

public class GroupRecyclerViewFragment extends Fragment {

    private Context mcontext;
    public static GroupsAdapter groupsAdapter;
    private ArrayList<ViewGroupModel> groupsList; // You should pass or initialize this
    private ClientRelationship clientRelationship;
    private RelationshipsModel relationshipsModel_new;
    private AppCompatButton btn_send_request;
    private RelationshipsAdapter relationshipsAdapter;
    private boolean isCancel = false;
    AtomicBoolean isUpdated = new AtomicBoolean(false);

    public GroupRecyclerViewFragment(Context context, ArrayList<ViewGroupModel> groups, ClientRelationship clientRel, RelationshipsModel relModel, RelationshipsAdapter relationshipsAdapter) {
        this.mcontext = context;
        this.groupsList = groups;
        this.clientRelationship = clientRel;
        this.relationshipsModel_new = relModel;
        this.relationshipsAdapter = relationshipsAdapter;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View popupView = inflater.inflate(R.layout.groups_recylerview_popup, container, false);
        RecyclerView rv_relationship_groups = popupView.findViewById(R.id.rv_relationship_groups);
        TextInputEditText et_search_relationships = popupView.findViewById(R.id.et_search_relationships);
        et_search_relationships.setHint(R.string.search_groups);

        btn_send_request = popupView.findViewById(R.id.btn_send_request);
        btn_send_request.setText(R.string.update);
        btn_send_request.setAlpha(0.5f);
        btn_send_request.setEnabled(false);
        relationshipsAdapter.btn_send_request = btn_send_request;
        relationshipsAdapter.isUpdated = isUpdated;
        relationshipsAdapter.et_search_groups = et_search_relationships;
        relationshipsAdapter.popupView = popupView;

        AppCompatButton btn_relationships_cancel = popupView.findViewById(R.id.btn_relationships_cancel);
        btn_relationships_cancel.setText(R.string.cancel);

        TextView tv_header_name = popupView.findViewById(R.id.header_name);
        if (!relationshipsModel_new.getName().isEmpty()) {
            tv_header_name.setText("Modify Group Access - " + relationshipsModel_new.getName());
        } else {
            tv_header_name.setText(R.string.modify_group_access);
        }
        tv_header_name.setTextSize(DynamicUtils.twenty);

        ImageView iv_close = popupView.findViewById(R.id.close_groups);
        CheckBox chk_select_all = popupView.findViewById(R.id.chk_select_all); // Optional

        rv_relationship_groups.setLayoutManager(new LinearLayoutManager(mcontext));
       // rv_relationship_groups.setHasFixedSize(true);

        groupsAdapter = new GroupsAdapter(groupsList, relationshipsAdapter, clientRelationship);
        rv_relationship_groups.setAdapter(groupsAdapter);

        et_search_relationships.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                groupsAdapter.getFilter().filter(et_search_relationships.getText().toString().trim());
            }
        });

        View.OnClickListener dismissListener = view -> {
            try {
                isCancel = true;
                relationshipsAdapter.callUpdateGroupAccess(relationshipsModel_new.getId(), groupsList, isCancel);
//                clientRelationship.ll_view_rel.setEnabled(true);
//                clientRelationship.ll_view_rel.setAlpha(1.0f);
//                et_search_relationships.setText("");
//                popupView.setVisibility(View.GONE);
//                clientRelationship.callViewRelationshipWebservice();
            } catch (JSONException e) {
                throw new RuntimeException(e);
            }
        };

        btn_relationships_cancel.setOnClickListener(dismissListener);
        iv_close.setOnClickListener(dismissListener);

        btn_send_request.setOnClickListener(v -> {
            try {
                isCancel = false;
                relationshipsAdapter.callUpdateGroupAccess(relationshipsModel_new.getId(), groupsList, isCancel);
//                clientRelationship.ll_view_rel.setEnabled(true);
//                clientRelationship.ll_view_rel.setAlpha(1.0f);
//                et_search_relationships.setText("");
//                popupView.setVisibility(View.GONE);
//                clientRelationship.callViewRelationshipWebservice();
            } catch (JSONException e) {
                e.fillInStackTrace();
            }
        });

        return popupView;
    }
}

