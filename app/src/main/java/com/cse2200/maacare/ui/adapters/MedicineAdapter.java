package com.cse2200.maacare.ui.adapters;

import android.content.res.ColorStateList;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;
import com.cse2200.maacare.data.db.entity.MedicineEntity;
import com.cse2200.maacare.databinding.ItemMedicineReminderBinding;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * Adapter for prescribed medications with interactive taken toggles, reminder switches,
 * delete actions, and real-time course duration tracking progress bars.
 */
public class MedicineAdapter extends ListAdapter<MedicineEntity, MedicineAdapter.MedicineViewHolder> {

    public interface OnMedicineActionListener {
        void onTakenToggled(MedicineEntity medicine, boolean isTaken);
        void onReminderToggled(MedicineEntity medicine, boolean isEnabled);
        void onDeleteClicked(MedicineEntity medicine);
    }

    private final OnMedicineActionListener listener;
    private static final SimpleDateFormat DATE_FMT = new SimpleDateFormat("MMM dd", Locale.getDefault());

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
                    && oldItem.getDurationDays() == newItem.getDurationDays()
                    && oldItem.getEndDateMillis() == newItem.getEndDateMillis()
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

            // Course Duration Tracking
            boolean isCompleted = item.isCourseCompleted();
            if (isCompleted) {
                binding.tvCourseStatusTag.setText("COURSE COMPLETED");
                binding.tvCourseStatusTag.setTextColor(Color.parseColor("#616161"));
                binding.tvCourseStatusTag.setBackgroundColor(Color.parseColor("#EEEEEE"));

                String endDateStr = DATE_FMT.format(new Date(item.getEndDateMillis()));
                binding.tvCourseBadge.setText(item.getDurationDays() + "-day course ended on " + endDateStr);
                binding.pbCourseProgress.setProgress(100);
                binding.pbCourseProgress.setProgressTintList(ColorStateList.valueOf(Color.parseColor("#9E9E9E")));

                binding.switchReminder.setEnabled(false);
                binding.switchReminder.setChecked(false);
            } else {
                binding.tvCourseStatusTag.setText("ACTIVE COURSE");
                binding.tvCourseStatusTag.setTextColor(Color.parseColor("#004D40"));
                binding.tvCourseStatusTag.setBackgroundColor(Color.parseColor("#E0F2F1"));

                int curDay = item.getCurrentDayInCourse();
                int totalDays = item.getDurationDays();
                int remaining = item.getDaysRemainingInCourse();

                binding.tvCourseBadge.setText("Day " + curDay + " of " + totalDays + " • " + remaining + " days left");
                int progress = item.getCourseProgressPercent();
                binding.pbCourseProgress.setProgress(progress);
                binding.pbCourseProgress.setProgressTintList(ColorStateList.valueOf(Color.parseColor("#00897B")));

                binding.switchReminder.setEnabled(true);
                binding.switchReminder.setChecked(item.isReminderEnabled());
            }

            // Clear listeners before setting state to prevent reciprocal trigger
            binding.cbTakenToday.setOnCheckedChangeListener(null);
            binding.cbTakenToday.setChecked(item.isTakenToday());

            binding.switchReminder.setOnCheckedChangeListener(null);

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
