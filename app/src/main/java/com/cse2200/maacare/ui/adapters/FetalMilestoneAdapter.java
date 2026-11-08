package com.cse2200.maacare.ui.adapters;

import android.view.LayoutInflater;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;
import com.cse2200.maacare.data.model.FetalMilestone;
import com.cse2200.maacare.databinding.ItemFetalMilestoneBinding;

/**
 * High-performance RecyclerView adapter using DiffUtil and ViewBinding for Fetal Milestones.
 */
public class FetalMilestoneAdapter extends ListAdapter<FetalMilestone, FetalMilestoneAdapter.MilestoneViewHolder> {

    public interface OnMilestoneClickListener {
        void onMilestoneClick(FetalMilestone milestone);
    }

    private final OnMilestoneClickListener listener;

    public FetalMilestoneAdapter() {
        this(null);
    }

    public FetalMilestoneAdapter(OnMilestoneClickListener listener) {
        super(DIFF_CALLBACK);
        this.listener = listener;
    }

    private static final DiffUtil.ItemCallback<FetalMilestone> DIFF_CALLBACK = new DiffUtil.ItemCallback<FetalMilestone>() {
        @Override
        public boolean areItemsTheSame(@NonNull FetalMilestone oldItem, @NonNull FetalMilestone newItem) {
            return oldItem.getWeek() == newItem.getWeek();
        }

        @Override
        public boolean areContentsTheSame(@NonNull FetalMilestone oldItem, @NonNull FetalMilestone newItem) {
            return oldItem.getDevelopmentSummary().equals(newItem.getDevelopmentSummary())
                    && oldItem.getBabySize().equals(newItem.getBabySize());
        }
    };

    @NonNull
    @Override
    public MilestoneViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemFetalMilestoneBinding binding = ItemFetalMilestoneBinding.inflate(
                LayoutInflater.from(parent.getContext()),
                parent,
                false
        );
        return new MilestoneViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull MilestoneViewHolder holder, int position) {
        FetalMilestone item = getItem(position);
        holder.bind(item, listener);
    }

    public static class MilestoneViewHolder extends RecyclerView.ViewHolder {
        private final ItemFetalMilestoneBinding binding;

        public MilestoneViewHolder(ItemFetalMilestoneBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        public void bind(FetalMilestone item, OnMilestoneClickListener listener) {
            binding.tvMilestoneWeek.setText("Week " + item.getWeek());
            String trimesterName = item.getTrimester() == 1 ? "1st Trimester" :
                    (item.getTrimester() == 2 ? "2nd Trimester" : "3rd Trimester");
            binding.tvTrimesterTag.setText(trimesterName);
            binding.tvFetalSize.setText(item.getBabySize());
            binding.tvDevSummary.setText(item.getDevelopmentSummary());
            binding.tvMaternalChanges.setText("Mom: " + item.getMaternalChanges());
            binding.tvClinicalAdvice.setText("Clinical Advice: " + item.getClinicalAdvice());

            itemView.setOnClickListener(v -> {
                if (listener != null) listener.onMilestoneClick(item);
            });
        }
    }
}
