package com.cse2200.maacare.ui.adapters;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;
import com.cse2200.maacare.R;
import com.cse2200.maacare.data.db.entity.VaccinationEntity;
import com.cse2200.maacare.databinding.ItemVaccinationBinding;
import com.cse2200.maacare.util.LocaleHelper;

/**
 * Bilingual Adapter for newborn EPI & WHO vaccination schedule with interactive administered toggles
 * and dynamic Completed, Due Now, and Upcoming status badges.
 */
public class VaccinationAdapter extends ListAdapter<VaccinationEntity, VaccinationAdapter.VaccineViewHolder> {

    public interface OnVaccineToggleListener {
        void onVaccineToggled(VaccinationEntity vaccine, boolean isDone);
    }

    private final OnVaccineToggleListener listener;
    private int babyAgeDays = 56; // Default ~8 weeks

    public VaccinationAdapter(OnVaccineToggleListener listener) {
        super(DIFF_CALLBACK);
        this.listener = listener;
    }

    public void setBabyAgeDays(int babyAgeDays) {
        this.babyAgeDays = babyAgeDays;
        notifyDataSetChanged();
    }

    private static final DiffUtil.ItemCallback<VaccinationEntity> DIFF_CALLBACK = new DiffUtil.ItemCallback<VaccinationEntity>() {
        @Override
        public boolean areItemsTheSame(@NonNull VaccinationEntity oldItem, @NonNull VaccinationEntity newItem) {
            return oldItem.getId() == newItem.getId();
        }

        @Override
        public boolean areContentsTheSame(@NonNull VaccinationEntity oldItem, @NonNull VaccinationEntity newItem) {
            return oldItem.isDone() == newItem.isDone()
                    && oldItem.getTargetAgeDays() == newItem.getTargetAgeDays()
                    && oldItem.getVaccineName().equals(newItem.getVaccineName())
                    && oldItem.getTargetAge().equals(newItem.getTargetAge());
        }
    };

    @NonNull
    @Override
    public VaccineViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemVaccinationBinding binding = ItemVaccinationBinding.inflate(
                LayoutInflater.from(parent.getContext()),
                parent,
                false
        );
        return new VaccineViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull VaccineViewHolder holder, int position) {
        VaccinationEntity item = getItem(position);
        holder.bind(item, babyAgeDays, listener);
    }

    public static class VaccineViewHolder extends RecyclerView.ViewHolder {
        private final ItemVaccinationBinding binding;

        public VaccineViewHolder(ItemVaccinationBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        public void bind(VaccinationEntity item, int babyAgeDays, OnVaccineToggleListener listener) {
            Context ctx = binding.getRoot().getContext();
            binding.tvVaccineName.setText(LocaleHelper.translateDynamicText(ctx, item.getVaccineName()));
            binding.tvTargetAge.setText(LocaleHelper.translateDynamicText(ctx, item.getTargetAge()));
            binding.tvDiseaseTarget.setText(LocaleHelper.translateDynamicText(ctx, item.getDiseaseTarget()));

            binding.cbVaccineDone.setOnCheckedChangeListener(null);
            binding.cbVaccineDone.setChecked(item.isDone());

            String status = item.computeStatus(babyAgeDays);

            if ("COMPLETED".equals(status)) {
                binding.tvVaccineStatus.setText(ctx.getString(R.string.vaccine_badge_completed));
                binding.tvVaccineStatus.setTextColor(Color.parseColor("#2E7D32"));
                binding.tvVaccineStatus.setBackgroundColor(Color.parseColor("#E8F5E9"));
                binding.tvVaccineStatus.setPadding(12, 4, 12, 4);
            } else if ("DUE NOW".equals(status)) {
                binding.tvVaccineStatus.setText(ctx.getString(R.string.vaccine_badge_due_now));
                binding.tvVaccineStatus.setTextColor(Color.parseColor("#C62828"));
                binding.tvVaccineStatus.setBackgroundColor(Color.parseColor("#FFEBEE"));
                binding.tvVaccineStatus.setPadding(12, 4, 12, 4);
            } else {
                binding.tvVaccineStatus.setText(ctx.getString(R.string.vaccine_badge_upcoming));
                binding.tvVaccineStatus.setTextColor(Color.parseColor("#00695C"));
                binding.tvVaccineStatus.setBackgroundColor(Color.parseColor("#E0F2F1"));
                binding.tvVaccineStatus.setPadding(12, 4, 12, 4);
            }

            binding.cbVaccineDone.setOnCheckedChangeListener((buttonView, isChecked) -> {
                if (listener != null) {
                    listener.onVaccineToggled(item, isChecked);
                }
            });
        }
    }
}
