package com.cse2200.maacare.ui.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.cse2200.maacare.data.model.DangerSign;
import com.cse2200.maacare.databinding.ItemDangerSignBinding;
import com.cse2200.maacare.util.LocaleHelper;
import java.util.ArrayList;
import java.util.List;

/**
 * Bilingual Adapter for Obstetric Danger Signs and Offline First-Aid Protocol Cards.
 */
public class DangerSignAdapter extends RecyclerView.Adapter<DangerSignAdapter.DangerSignViewHolder> {

    private final List<DangerSign> list = new ArrayList<>();

    public void submitList(List<DangerSign> newList) {
        list.clear();
        if (newList != null) {
            list.addAll(newList);
        }
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public DangerSignViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemDangerSignBinding binding = ItemDangerSignBinding.inflate(
                LayoutInflater.from(parent.getContext()),
                parent,
                false
        );
        return new DangerSignViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull DangerSignViewHolder holder, int position) {
        holder.bind(list.get(position));
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    public static class DangerSignViewHolder extends RecyclerView.ViewHolder {
        private final ItemDangerSignBinding binding;

        public DangerSignViewHolder(ItemDangerSignBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        public void bind(DangerSign item) {
            Context ctx = binding.getRoot().getContext();
            boolean bn = LocaleHelper.isBangla(ctx);

            if (bn) {
                binding.tvDangerName.setText(item.getSignNameBn() != null && !item.getSignNameBn().isEmpty()
                        ? item.getSignNameBn() : item.getSignName());

                String risk = item.getRiskLevel() != null ? item.getRiskLevel() : "";
                if ("CRITICAL".equalsIgnoreCase(risk)) {
                    binding.tvRiskBadge.setText("অতি জরুরি");
                } else if ("HIGH".equalsIgnoreCase(risk)) {
                    binding.tvRiskBadge.setText("উচ্চ ঝুঁকি");
                } else {
                    binding.tvRiskBadge.setText(risk);
                }

                binding.tvDangerDesc.setText(item.getDescriptionBn() != null && !item.getDescriptionBn().isEmpty()
                        ? item.getDescriptionBn() : item.getDescription());

                String actionBn = item.getFirstAidActionBn() != null && !item.getFirstAidActionBn().isEmpty()
                        ? item.getFirstAidActionBn() : "করণীয়: " + item.getFirstAidAction();
                binding.tvFirstAidAction.setText(actionBn);
            } else {
                binding.tvDangerName.setText(item.getSignName());
                binding.tvRiskBadge.setText(item.getRiskLevel());
                binding.tvDangerDesc.setText(item.getDescription());
                String actionEn = item.getFirstAidAction() != null ? item.getFirstAidAction() : "";
                binding.tvFirstAidAction.setText(actionEn.startsWith("Action:") ? actionEn : "Action: " + actionEn);
            }
        }
    }
}
