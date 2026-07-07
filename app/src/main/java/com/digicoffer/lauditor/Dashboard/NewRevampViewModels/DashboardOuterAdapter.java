package com.digicoffer.lauditor.Dashboard.NewRevampViewModels;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.digicoffer.lauditor.R;

import java.util.ArrayList;
import java.util.List;

public class DashboardOuterAdapter extends RecyclerView.Adapter<DashboardOuterAdapter.SectionHolder> {


        private final Context                               context;
        private final DashboardCardAdapter.CardActionListener listener;
        private       List<DashboardSection>                sections = new ArrayList<>();

    public DashboardOuterAdapter(Context context,
            DashboardCardAdapter.CardActionListener listener) {
        this.context  = context;
        this.listener = listener;
    }

        public void submitSections(List<DashboardSection> newSections) {
        this.sections = newSections != null ? newSections : new ArrayList<>();
        notifyDataSetChanged();
    }

        @Override public int getItemCount() { return sections.size(); }

        @NonNull @Override
        public SectionHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_dashboard_section, parent, false);
        return new SectionHolder(v);
    }

        @Override
        public void onBindViewHolder(@NonNull SectionHolder holder, int position) {
        holder.bind(sections.get(position));
    }

        class SectionHolder extends RecyclerView.ViewHolder {
            final TextView           tvTitle, tvBadge;
            final RecyclerView       rvCards;
            final LinearLayoutManager llm; // ← owned by this holder, created ONCE

            SectionHolder(View v) {
                super(v);
                tvTitle  = v.findViewById(R.id.tv_section_title);
                tvBadge  = v.findViewById(R.id.tv_section_badge);
                rvCards  = v.findViewById(R.id.rv_section_cards);

                // Create the LayoutManager exactly once per holder, and set it
                // exactly once. It will never be reassigned to another RecyclerView.
                llm = new LinearLayoutManager(itemView.getContext(),
                        LinearLayoutManager.HORIZONTAL, false);
                rvCards.setLayoutManager(llm);
            }

            void bind(DashboardSection section) {
                tvTitle.setText(section.title);

                // ── Section badge: always visible, shows card count ──────────
                // Even when count = 0 the badge stays shown (e.g. "0")
                tvBadge.setText(String.valueOf(section.cardCount()));
                tvBadge.setVisibility(View.VISIBLE);   // ← was: GONE when count == 0

                DashboardCardAdapter adapter = new DashboardCardAdapter(context, listener);
                adapter.submitList(section.items);
                rvCards.setAdapter(adapter);
            }
        }
    }
