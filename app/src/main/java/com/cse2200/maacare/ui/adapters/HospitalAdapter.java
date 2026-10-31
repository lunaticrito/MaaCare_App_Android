package com.cse2200.maacare.ui.adapters;

import android.view.LayoutInflater;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.cse2200.maacare.data.model.HospitalContact;
import com.cse2200.maacare.databinding.ItemEmergencyContactBinding;
import java.util.ArrayList;
import java.util.List;

/**
 * Adapter for 24/7 Maternal Emergency Facilities and Hospital Directory.
 */
public class HospitalAdapter extends RecyclerView.Adapter<HospitalAdapter.HospitalViewHolder> {

    public interface OnCallHospitalListener {
        void onCallHospital(HospitalContact contact);
    }

    private final List<HospitalContact> list = new ArrayList<>();
    private final OnCallHospitalListener listener;

    public HospitalAdapter(OnCallHospitalListener listener) {
        this.listener = listener;
    }

    public void submitList(List<HospitalContact> newList) {
        list.clear();
        if (newList != null) {
            list.addAll(newList);
        }
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public HospitalViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemEmergencyContactBinding binding = ItemEmergencyContactBinding.inflate(
                LayoutInflater.from(parent.getContext()),
                parent,
                false
        );
        return new HospitalViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull HospitalViewHolder holder, int position) {
        holder.bind(list.get(position), listener);
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    public static class HospitalViewHolder extends RecyclerView.ViewHolder {
        private final ItemEmergencyContactBinding binding;

        public HospitalViewHolder(ItemEmergencyContactBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        public void bind(HospitalContact item, OnCallHospitalListener listener) {
            binding.tvFacilityName.setText(item.getFacilityName());
            binding.tvFacilityCategory.setText(item.getCategory());
            binding.tvFacilityAddress.setText(item.getAddress());
            binding.tvFacilityPhone.setText(item.getPhoneNumber());

            binding.btnCallFacility.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onCallHospital(item);
                }
            });
        }
    }
}
