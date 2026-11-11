package com.cse2200.maacare.ui.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;
import com.cse2200.maacare.R;
import com.cse2200.maacare.data.db.entity.BabyVitalsEntity;
import com.cse2200.maacare.databinding.ItemBabyVitalsBinding;
import com.cse2200.maacare.util.LocaleHelper;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

/**
 * Bilingual DiffUtil-backed RecyclerView Adapter for saved Baby Health & Vitals history cards
 * supporting Edit and Delete actions.
 */
public class BabyVitalsAdapter extends ListAdapter<BabyVitalsEntity, BabyVitalsAdapter.VitalsViewHolder> {

    public interface OnVitalsActionListener {
        void onEditVitals(BabyVitalsEntity vitals);
        void onDeleteVitals(BabyVitalsEntity vitals);
    }

    private final OnVitalsActionListener listener;
    private static final SimpleDateFormat TIME_FMT = new SimpleDateFormat("hh:mm a", Locale.getDefault());
    private static final SimpleDateFormat FULL_DATE_FMT = new SimpleDateFormat("MMM dd, hh:mm a", Locale.getDefault());
    private static final SimpleDateFormat DAY_FMT = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());

    public BabyVitalsAdapter(OnVitalsActionListener listener) {
        super(DIFF_CALLBACK);
        this.listener = listener;
    }

    private static final DiffUtil.ItemCallback<BabyVitalsEntity> DIFF_CALLBACK = new DiffUtil.ItemCallback<BabyVitalsEntity>() {
        @Override
        public boolean areItemsTheSame(@NonNull BabyVitalsEntity oldItem, @NonNull BabyVitalsEntity newItem) {
            return oldItem.getId() == newItem.getId();
        }

        @Override
        public boolean areContentsTheSame(@NonNull BabyVitalsEntity oldItem, @NonNull BabyVitalsEntity newItem) {
            return oldItem.isSick() == newItem.isSick()
                    && oldItem.getPulseFrequency() == newItem.getPulseFrequency()
                    && oldItem.getSpo2Frequency() == newItem.getSpo2Frequency()
                    && oldItem.getRecordedMillis() == newItem.getRecordedMillis()
                    && String.valueOf(oldItem.getTempDegreeOrFreq()).equals(String.valueOf(newItem.getTempDegreeOrFreq()))
                    && String.valueOf(oldItem.getSymptomsNotes()).equals(String.valueOf(newItem.getSymptomsNotes()));
        }
    };

    @NonNull
    @Override
    public VitalsViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemBabyVitalsBinding binding = ItemBabyVitalsBinding.inflate(
                LayoutInflater.from(parent.getContext()),
                parent,
                false
        );
        return new VitalsViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull VitalsViewHolder holder, int position) {
        holder.bind(getItem(position), listener);
    }

    static class VitalsViewHolder extends RecyclerView.ViewHolder {
        private final ItemBabyVitalsBinding binding;

        public VitalsViewHolder(@NonNull ItemBabyVitalsBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        public void bind(BabyVitalsEntity vitals, OnVitalsActionListener listener) {
            Context ctx = binding.getRoot().getContext();
            boolean bn = LocaleHelper.isBangla(ctx);

            binding.tvVitalsTimestamp.setText(formatRelativeTimestamp(ctx, vitals.getRecordedMillis()));

            if (vitals.isSick()) {
                binding.tvVitalsStatusPill.setText(bn ? "অসুস্থ / সতর্কতা প্রয়োজন" : "Sick / Needs Attention");
                binding.tvVitalsStatusPill.setTextColor(ContextCompat.getColor(ctx, R.color.emergency_red));
            } else {
                binding.tvVitalsStatusPill.setText(bn ? "সুস্থ / স্বাভাবিক" : "Healthy / Normal");
                binding.tvVitalsStatusPill.setTextColor(ContextCompat.getColor(ctx, R.color.success_green));
            }

            String tempStr = vitals.getTempDegreeOrFreq() != null && !vitals.getTempDegreeOrFreq().isEmpty()
                    ? vitals.getTempDegreeOrFreq() : "98.6°F";
            if (bn) {
                String tempLocalized = LocaleHelper.toBanglaDigits(tempStr.replace("Normal", "স্বাভাবিক"));
                String pulseBn = LocaleHelper.toBanglaDigits(String.valueOf(vitals.getPulseFrequency()));
                String spo2Bn = LocaleHelper.toBanglaDigits(String.valueOf(vitals.getSpo2Frequency()));
                binding.tvVitalsMetricsLine.setText("তাপমাত্রা: " + tempLocalized + "  |  পালস: " + pulseBn + " বিপিএম  |  SpO2: " + spo2Bn + "%");
            } else {
                binding.tvVitalsMetricsLine.setText(String.format(
                        Locale.getDefault(),
                        "Temp: %s  |  Pulse: %d bpm  |  SpO2: %d%%",
                        tempStr, vitals.getPulseFrequency(), vitals.getSpo2Frequency()
                ));
            }

            String notes = vitals.getSymptomsNotes();
            if (notes == null || notes.isEmpty()) {
                binding.tvVitalsNotes.setText(bn ? "কোনো উপসর্গ উল্লেখ করা হয়নি।" : "No symptoms noted.");
            } else {
                binding.tvVitalsNotes.setText(LocaleHelper.translateDynamicText(ctx, notes));
            }

            binding.btnEditVitalsItem.setOnClickListener(v -> {
                if (listener != null) listener.onEditVitals(vitals);
            });
            binding.btnDeleteVitalsItem.setOnClickListener(v -> {
                if (listener != null) listener.onDeleteVitals(vitals);
            });
        }

        private String formatRelativeTimestamp(Context ctx, long millis) {
            Date date = new Date(millis);
            String targetDay = DAY_FMT.format(date);
            Calendar cal = Calendar.getInstance();
            String today = DAY_FMT.format(cal.getTime());
            cal.add(Calendar.DAY_OF_YEAR, -1);
            String yesterday = DAY_FMT.format(cal.getTime());

            boolean bn = LocaleHelper.isBangla(ctx);
            if (targetDay.equals(today)) {
                String raw = "Today, " + TIME_FMT.format(date);
                return bn ? LocaleHelper.localizeTime(ctx, raw) : raw;
            } else if (targetDay.equals(yesterday)) {
                String raw = "Yesterday, " + TIME_FMT.format(date);
                return bn ? LocaleHelper.localizeTime(ctx, raw) : raw;
            } else {
                String raw = FULL_DATE_FMT.format(date);
                return bn ? LocaleHelper.localizeTime(ctx, raw) : raw;
            }
        }
    }
}
