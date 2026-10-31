package com.cse2200.maacare.ui.adapters;

import android.view.LayoutInflater;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;
import com.cse2200.maacare.data.db.entity.FoodLogEntity;
import com.cse2200.maacare.databinding.ItemFoodLogBinding;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * Adapter for maternal nutrition food logs with DiffUtil.
 */
public class FoodLogAdapter extends ListAdapter<FoodLogEntity, FoodLogAdapter.FoodViewHolder> {

    public interface OnFoodDeleteListener {
        void onFoodDeleted(FoodLogEntity log);
    }

    private final OnFoodDeleteListener listener;

    public FoodLogAdapter(OnFoodDeleteListener listener) {
        super(DIFF_CALLBACK);
        this.listener = listener;
    }

    private static final DiffUtil.ItemCallback<FoodLogEntity> DIFF_CALLBACK = new DiffUtil.ItemCallback<FoodLogEntity>() {
        @Override
        public boolean areItemsTheSame(@NonNull FoodLogEntity oldItem, @NonNull FoodLogEntity newItem) {
            return oldItem.getId() == newItem.getId();
        }

        @Override
        public boolean areContentsTheSame(@NonNull FoodLogEntity oldItem, @NonNull FoodLogEntity newItem) {
            return oldItem.getMealName().equals(newItem.getMealName())
                    && oldItem.getCalories() == newItem.getCalories()
                    && Double.compare(oldItem.getProteinGrams(), newItem.getProteinGrams()) == 0;
        }
    };

    @NonNull
    @Override
    public FoodViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemFoodLogBinding binding = ItemFoodLogBinding.inflate(
                LayoutInflater.from(parent.getContext()),
                parent,
                false
        );
        return new FoodViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull FoodViewHolder holder, int position) {
        FoodLogEntity item = getItem(position);
        holder.bind(item, listener);
    }

    public static class FoodViewHolder extends RecyclerView.ViewHolder {
        private final ItemFoodLogBinding binding;
        private static final SimpleDateFormat TIME_FMT = new SimpleDateFormat("hh:mm a", Locale.getDefault());

        public FoodViewHolder(ItemFoodLogBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        public void bind(FoodLogEntity item, OnFoodDeleteListener listener) {
            binding.tvFoodTitle.setText(item.getMealName());
            binding.tvFoodMeta.setText(item.getCalories() + " kcal • " + item.getProteinGrams() + "g Protein");
            binding.tvLoggedTime.setText(TIME_FMT.format(new Date(item.getLoggedTimeMillis())));

            binding.btnDeleteFood.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onFoodDeleted(item);
                }
            });
        }
    }
}
