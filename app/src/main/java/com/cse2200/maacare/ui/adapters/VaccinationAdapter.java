package com.cse2200.maacare.ui.adapters;

import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;
import com.cse2200.maacare.data.db.entity.VaccinationEntity;
import com.cse2200.maacare.databinding.ItemVaccinationBinding;

/**
 * Adapter for newborn EPI vaccination schedule with interactive administered toggles.
 */
public class VaccinationAdapter extends ListAdapter<VaccinationEntity, VaccinationAdapter.VaccineViewHolder> {

    public interface OnVaccineToggleListener {
        void onVaccineToggled(VaccinationEntity vaccine, boolean isDone);
    }

    private final OnVaccineToggleListener listener;

    public VaccinationAdapter(OnVaccineToggleListener listener) {
        super(DIFF_CALLBACK);
        this.listener = listener;
    }

    private static final DiffUtil.ItemCallback<VaccinationEntity> DIFF_CALLBACK = new DiffUtil.ItemCallback<VaccinationEntity>() {
        @Override
        public boolean areItemsTheSame(@NonNull VaccinationEntity oldItem, @NonNull VaccinationEntity newItem) {
            return oldItem.getId() == newItem.getId();
        }

        @Override
        public boolean areContentsTheSame(@NonNull VaccinationEntity oldItem, @NonNull VaccinationEntity newItem) {
            return oldItem.isDone() == newItem.isDone()
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
        holder.bind(item, listener);
    }

    public static class VaccineViewHolder extends RecyclerView.ViewHolder {
        private final ItemVaccinationBinding binding;

        public VaccineViewHolder(ItemVaccinationBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        public void bind(VaccinationEntity item, OnVaccineToggleListener listener) {
            binding.tvVaccineName.setText(item.getVaccineName());
            binding.tvTargetAge.setText(item.getTargetAge());
            binding.tvDiseaseTarget.setText(item.getDiseaseTarget());

            binding.cbVaccineDone.setOnCheckedChangeListener(null);
            binding.cbVaccineDone.setChecked(item.isDone());

            if (item.isDone()) {
                binding.tvVaccineStatus.setText("Completed");
                binding.tvVaccineStatus.setTextColor(Color.parseColor("#2E7D32"));
            } else {
                binding.tvVaccineStatus.setText("Pending");
                binding.tvVaccineStatus.setTextColor(Color.parseColor("#F57C00"));
            }

            binding.cbVaccineDone.setOnCheckedChangeListener((buttonView, isChecked) -> {
                if (listener != null) {
                    listener.onVaccineToggled(item, isChecked);
                }
            });
        }
    }
}
