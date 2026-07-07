package com.digicoffer.lauditor.FirmProfile;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.digicoffer.lauditor.R;

import org.json.JSONArray;

/**
 * A single unified adapter that replaces ChildAdapter, ServiceAdapter, and LanguageAdapter.
 * <p>
 * Usage:
 * rvPractice.setAdapter(new TagAdapter(practiceArray, TagAdapter.Style.PRACTICE));
 * rvServices.setAdapter(new TagAdapter(servicesArray, TagAdapter.Style.SERVICE));
 * rvLanguages.setAdapter(new TagAdapter(languagesArray, TagAdapter.Style.LANGUAGE));
 */
public class TagAdapter extends RecyclerView.Adapter<TagAdapter.TagViewHolder> {

    // ── Card height in dp — change this one value to resize all tag cards ──
    private static final int CARD_HEIGHT_DP = 60;

    public enum Style {
        PRACTICE,   // green background  (replaces ChildAdapter)
        SERVICE,    // dark-grey background (replaces ServiceAdapter)
        LANGUAGE    // blue background    (replaces LanguageAdapter)
    }

    private final JSONArray list;
    private final Style style;

    public TagAdapter(JSONArray list, Style style) {
        this.list = list;
        this.style = style;
    }

    @NonNull
    @Override
    public TagViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_language, parent, false);

//        // ── Enforce a common fixed height for every card ──────────────────
//        float density = parent.getContext().getResources().getDisplayMetrics().density;
//        int heightPx = (int) (CARD_HEIGHT_DP * density);
//        ViewGroup.LayoutParams params = view.getLayoutParams();
//        if (params == null) {
//            params = new ViewGroup.LayoutParams(
//                    ViewGroup.LayoutParams.MATCH_PARENT, DynamicUtils.fifty);
//        } else {
//            params.height = heightPx;
//        }
//        view.setLayoutParams(params);

        return new TagViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull TagViewHolder holder, int position) {
        holder.tvTag.setText(list.optString(position, ""));

        switch (style) {
            case PRACTICE:
                holder.tvTag.setBackgroundResource(R.drawable.chip_pale_blue);
//                holder.tvTag.setTextColor(ContextCompat.getColor(holder.itemView.getContext(), R.color.white));
                break;

            case SERVICE:
                holder.tvTag.setBackgroundResource(R.drawable.chip_pale_blue);
//                holder.tvTag.setTextColor(ContextCompat.getColor(holder.itemView.getContext(), R.color.white));
                break;

            case LANGUAGE:
                holder.tvTag.setBackgroundResource(R.drawable.chip_pale_blue);
//                holder.tvTag.setTextColor(ContextCompat.getColor(holder.itemView.getContext(), R.color.white));
                break;
        }
    }

    @Override
    public int getItemCount() {
        return list == null ? 0 : list.length();
    }

    static class TagViewHolder extends RecyclerView.ViewHolder {
        final TextView tvTag;

        TagViewHolder(@NonNull View itemView) {
            super(itemView);
            tvTag = itemView.findViewById(R.id.tvLanguage);
        }
    }
}