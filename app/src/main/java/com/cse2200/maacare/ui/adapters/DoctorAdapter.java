package com.cse2200.maacare.ui.adapters;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.cse2200.maacare.R;
import com.cse2200.maacare.data.db.entity.DoctorEntity;
import com.cse2200.maacare.databinding.ItemDoctorCardBinding;
import com.cse2200.maacare.util.LocaleHelper;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Bilingual RecyclerView Adapter for the 64-District Bangladesh Doctors Directory
 * supporting one-tap calling and assigning a specialist as Primary Emergency Contact.
 */
public class DoctorAdapter extends RecyclerView.Adapter<DoctorAdapter.DoctorViewHolder> {

    public interface OnDoctorActionListener {
        void onSetEmergencyDoctor(DoctorEntity doctor);
    }

    private final List<DoctorEntity> doctorList = new ArrayList<>();
    private final OnDoctorActionListener listener;

    public DoctorAdapter() {
        this(null);
    }

    public DoctorAdapter(OnDoctorActionListener listener) {
        this.listener = listener;
    }

    public void setDoctors(List<DoctorEntity> list) {
        doctorList.clear();
        if (list != null) {
            doctorList.addAll(list);
        }
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public DoctorViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemDoctorCardBinding binding = ItemDoctorCardBinding.inflate(
                LayoutInflater.from(parent.getContext()),
                parent,
                false
        );
        return new DoctorViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull DoctorViewHolder holder, int position) {
        holder.bind(doctorList.get(position), listener);
    }

    @Override
    public int getItemCount() {
        return doctorList.size();
    }

    static class DoctorViewHolder extends RecyclerView.ViewHolder {
        private final ItemDoctorCardBinding binding;

        public DoctorViewHolder(@NonNull ItemDoctorCardBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        public void bind(DoctorEntity doc, OnDoctorActionListener listener) {
            Context ctx = binding.getRoot().getContext();
            boolean bn = LocaleHelper.isBangla(ctx);

            String divisionStr = LocaleHelper.translateDivision(ctx, doc.getDivision());
            String districtStr = LocaleHelper.translateDistrict(ctx, doc.getDistrict());
            if (bn) {
                binding.tvDoctorLocationBadge.setText(districtStr + " জেলা • " + divisionStr + " বিভাগ");
                binding.tvDoctorRating.setText("★ " + LocaleHelper.toBanglaDigits(String.format(Locale.US, "%.1f", doc.getRating())));
            } else {
                binding.tvDoctorLocationBadge.setText(districtStr + " • " + divisionStr + " Div");
                binding.tvDoctorRating.setText(String.format(Locale.US, "★ %.1f", doc.getRating()));
            }

            binding.tvDoctorName.setText(doc.getName());

            String categoryTag = LocaleHelper.translateSpecialtyCategory(ctx, doc.getSpecialtyCategory());
            if (bn) {
                binding.tvDoctorSpecialty.setText(categoryTag + " • " + doc.getSpecialty());
            } else {
                binding.tvDoctorSpecialty.setText(doc.getSpecialty());
            }

            binding.tvDoctorHospital.setText(doc.getHospital());

            String slotText = LocaleHelper.localizeTime(ctx, doc.getNextSlot());
            String phoneDisplay = bn ? LocaleHelper.toBanglaDigits(doc.getPhone()) : doc.getPhone();
            binding.tvDoctorSlot.setText(phoneDisplay + " • " + slotText);

            binding.tvBadgeAvailability.setVisibility(doc.isAvailableToday() ? View.VISIBLE : View.GONE);

            binding.btnCallDoctor.setText(bn ? "📞 কল করুন" : ctx.getString(R.string.btn_call_doctor));
            binding.btnSetEmergencyDoctor.setText(ctx.getString(R.string.btn_set_emergency_contact));

            binding.btnCallDoctor.setOnClickListener(v -> {
                Intent dialIntent = new Intent(Intent.ACTION_DIAL);
                dialIntent.setData(Uri.parse("tel:" + doc.getPhone()));
                ctx.startActivity(dialIntent);
            });

            binding.btnSetEmergencyDoctor.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onSetEmergencyDoctor(doc);
                }
            });
        }
    }
}
