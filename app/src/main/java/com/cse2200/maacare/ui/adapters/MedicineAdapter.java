package com.cse2200.maacare.ui.adapters;

import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;
import com.cse2200.maacare.R;
import com.cse2200.maacare.data.db.entity.MedicineEntity;
import com.cse2200.maacare.databinding.ItemMedicineReminderBinding;
import com.cse2200.maacare.util.LocaleHelper;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * Bilingual Adapter for prescribed medications with interactive taken toggles, reminder switches,
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
            Context ctx = binding.getRoot().getContext();
            boolean bn = LocaleHelper.isBangla(ctx);

            binding.tvMedicineName.setText(LocaleHelper.translateDynamicText(ctx, item.getName()));
            binding.tvDosage.setText(LocaleHelper.translateDynamicText(ctx, item.getDosage()));

            String localizedTime = LocaleHelper.localizeTime(ctx, item.getReminderTime());
            binding.tvScheduledTime.setText(bn ? "নির্ধারিত সময়: " + localizedTime : "Scheduled: " + localizedTime);

            boolean isCompleted = item.isCourseCompleted();
            if (isCompleted) {
                binding.tvCourseStatusTag.setText(ctx.getString(R.string.course_status_completed));
                binding.tvCourseStatusTag.setTextColor(Color.parseColor("#616161"));
                binding.tvCourseStatusTag.setBackgroundColor(Color.parseColor("#EEEEEE"));

                String endDateStr = DATE_FMT.format(new Date(item.getEndDateMillis()));
                if (bn) {
                    binding.tvCourseBadge.setText(LocaleHelper.toBanglaDigits(
                            item.getDurationDays() + " দিনের কোর্স " + endDateStr + " তারিখে শেষ হয়েছে"));
                } else {
                    binding.tvCourseBadge.setText(item.getDurationDays() + "-day course ended on " + endDateStr);
                }
                binding.pbCourseProgress.setProgress(100);
                binding.pbCourseProgress.setProgressTintList(ColorStateList.valueOf(Color.parseColor("#9E9E9E")));

                binding.switchReminder.setEnabled(false);
                binding.switchReminder.setChecked(false);
            } else {
                binding.tvCourseStatusTag.setText(ctx.getString(R.string.course_status_active));
                binding.tvCourseStatusTag.setTextColor(Color.parseColor("#004D40"));
                binding.tvCourseStatusTag.setBackgroundColor(Color.parseColor("#E0F2F1"));

                int curDay = item.getCurrentDayInCourse();
                int totalDays = item.getDurationDays();
                int remaining = item.getDaysRemainingInCourse();

                String badgeText = ctx.getString(R.string.course_day_format, curDay, totalDays, remaining);
                binding.tvCourseBadge.setText(bn ? LocaleHelper.toBanglaDigits(badgeText) : badgeText);
                binding.pbCourseProgress.setProgress(item.getCourseProgressPercent());
                binding.pbCourseProgress.setProgressTintList(ColorStateList.valueOf(Color.parseColor("#00897B")));

                binding.switchReminder.setEnabled(true);
                binding.switchReminder.setChecked(item.isReminderEnabled());
            }

            binding.cbTakenToday.setOnCheckedChangeListener(null);
            binding.cbTakenToday.setChecked(item.isTakenToday());
            binding.switchReminder.setOnCheckedChangeListener(null);

            binding.cbTakenToday.setOnCheckedChangeListener((buttonView, isChecked) -> {
                if (listener != null) listener.onTakenToggled(item, isChecked);
            });

            binding.switchReminder.setOnCheckedChangeListener((buttonView, isChecked) -> {
                if (listener != null) listener.onReminderToggled(item, isChecked);
            });

            binding.btnDeleteMedicine.setOnClickListener(v -> {
                if (listener != null) listener.onDeleteClicked(item);
            });
        }
    }
}
