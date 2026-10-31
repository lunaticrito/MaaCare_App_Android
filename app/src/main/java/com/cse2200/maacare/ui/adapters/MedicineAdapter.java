package com.cse2200.maacare.ui.adapters;

import android.view.LayoutInflater;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;
import com.cse2200.maacare.data.db.entity.MedicineEntity;
import com.cse2200.maacare.databinding.ItemMedicineReminderBinding;

/**
 * Adapter for prescribed medications with interactive taken toggles, reminder switches, and delete actions.
 */
public class MedicineAdapter extends ListAdapter<MedicineEntity, MedicineAdapter.MedicineViewHolder> {

    public interface OnMedicineActionListener {
        void onTakenToggled(MedicineEntity medicine, boolean isTaken);
        void onReminderToggled(MedicineEntity medicine, boolean isEnabled);
        void onDeleteClicked(MedicineEntity medicine);
    }

    private final OnMedicineActionListener listener;

    public MedicineAdapter(OnMedicineActionListener listener) {
        super(DIFF_CALLBACK);
        this.listener = listener;
    }

    private static final DiffUtil.ItemCallback<MedicineEntity> DIFF_CALLBACK = new DiffUtil.ItemCallback<MedicineEntity>() {
        @Override
        public boolean areItemsTheSame(@NonNull MedicineEntity oldItem, @NonNull MedicineEntity newItem) {
            return oldItem.getId() == newItem.getId();
        }

        @Override
        public boolean areContentsTheSame(@NonNull MedicineEntity oldItem, @NonNull MedicineEntity newItem) {
            return oldItem.isTakenToday() == newItem.isTakenToday()
                    && oldItem.isReminderEnabled() == newItem.isReminderEnabled()
                    && oldItem.getName().equals(newItem.getName())
                    && oldItem.getReminderTime().equals(newItem.getReminderTime());
        }
    };

    @NonNull
    @Override
    public MedicineViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemMedicineReminderBinding binding = ItemMedicineReminderBinding.inflate(
                LayoutInflater.from(parent.getContext()),
                parent,
                false
        );
        return new MedicineViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull MedicineViewHolder holder, int position) {
        MedicineEntity item = getItem(position);
        holder.bind(item, listener);
    }

    public static class MedicineViewHolder extends RecyclerView.ViewHolder {
        private final ItemMedicineReminderBinding binding;

        public MedicineViewHolder(ItemMedicineReminderBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        public void bind(MedicineEntity item, OnMedicineActionListener listener) {
            binding.tvMedicineName.setText(item.getName());
            binding.tvDosage.setText(item.getDosage());
            binding.tvScheduledTime.setText("Scheduled: " + item.getReminderTime());

            // Clear listeners before setting state to prevent reciprocal trigger
            binding.cbTakenToday.setOnCheckedChangeListener(null);
            binding.cbTakenToday.setChecked(item.isTakenToday());

            binding.switchReminder.setOnCheckedChangeListener(null);
            binding.switchReminder.setChecked(item.isReminderEnabled());

            binding.cbTakenToday.setOnCheckedChangeListener((buttonView, isChecked) -> {
                if (listener != null) {
                    listener.onTakenToggled(item, isChecked);
                }
            });

            binding.switchReminder.setOnCheckedChangeListener((buttonView, isChecked) -> {
                if (listener != null) {
                    listener.onReminderToggled(item, isChecked);
                }
            });

            binding.btnDeleteMedicine.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onDeleteClicked(item);
                }
            });
        }
    }
}
