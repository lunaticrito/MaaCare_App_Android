package com.cse2200.maacare.ui.adapters;

import android.view.LayoutInflater;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;
import com.cse2200.maacare.data.db.entity.BabyFoodLogEntity;
import com.cse2200.maacare.databinding.ItemBabyFoodLogBinding;

/**
 * Adapter for infant and toddler diet logs and allergy observations.
 */
public class BabyFoodLogAdapter extends ListAdapter<BabyFoodLogEntity, BabyFoodLogAdapter.BabyFoodViewHolder> {

    public interface OnBabyFoodDeleteListener {
        void onFoodDeleted(BabyFoodLogEntity log);
    }

    private final OnBabyFoodDeleteListener listener;

    public BabyFoodLogAdapter(OnBabyFoodDeleteListener listener) {
        super(DIFF_CALLBACK);
        this.listener = listener;
    }

    private static final DiffUtil.ItemCallback<BabyFoodLogEntity> DIFF_CALLBACK = new DiffUtil.ItemCallback<BabyFoodLogEntity>() {
        @Override
        public boolean areItemsTheSame(@NonNull BabyFoodLogEntity oldItem, @NonNull BabyFoodLogEntity newItem) {
            return oldItem.getId() == newItem.getId();
        }

        @Override
        public boolean areContentsTheSame(@NonNull BabyFoodLogEntity oldItem, @NonNull BabyFoodLogEntity newItem) {
            return oldItem.getFoodName().equals(newItem.getFoodName())
                    && oldItem.getAgeCategory().equals(newItem.getAgeCategory())
                    && oldItem.getFeedingDetails().equals(newItem.getFeedingDetails())
                    && oldItem.getAllergyReaction().equals(newItem.getAllergyReaction());
        }
    };

    @NonNull
    @Override
    public BabyFoodViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemBabyFoodLogBinding binding = ItemBabyFoodLogBinding.inflate(
                LayoutInflater.from(parent.getContext()),
                parent,
                false
        );
        return new BabyFoodViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull BabyFoodViewHolder holder, int position) {
        holder.bind(getItem(position), listener);
    }

    public static class BabyFoodViewHolder extends RecyclerView.ViewHolder {
        private final ItemBabyFoodLogBinding binding;

        public BabyFoodViewHolder(ItemBabyFoodLogBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        public void bind(BabyFoodLogEntity item, OnBabyFoodDeleteListener listener) {
            binding.tvBabyFoodName.setText(item.getFoodName());
            binding.tvBabyAgeCategoryBadge.setText(item.getAgeCategory());
            binding.tvBabyFeedingDetails.setText(item.getFeedingDetails());
            binding.tvBabyAllergyStatus.setText("Allergy/Reaction: " + item.getAllergyReaction());

            binding.btnDeleteBabyFood.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onFoodDeleted(item);
                }
            });
        }
    }
}
