package com.cse2200.maacare.ui.adapters;

import android.content.Intent;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.cse2200.maacare.R;
import com.cse2200.maacare.data.db.entity.DoctorEntity;
import java.util.ArrayList;
import java.util.List;

public class DoctorAdapter extends RecyclerView.Adapter<DoctorAdapter.DoctorViewHolder> {

    private final List<DoctorEntity> doctorList = new ArrayList<>();

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
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_doctor_card, parent, false);
        return new DoctorViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull DoctorViewHolder holder, int position) {
        holder.bind(doctorList.get(position));
    }

    @Override
    public int getItemCount() {
        return doctorList.size();
    }

    static class DoctorViewHolder extends RecyclerView.ViewHolder {
        private final TextView tvName;
        private final TextView tvSpecialty;
        private final TextView tvRating;
        private final TextView tvHospital;
        private final TextView tvSlot;
        private final TextView tvBadgeAvailability;
        private final Button btnCall;
        private final Button btnBook;

        public DoctorViewHolder(@NonNull View itemView) {
            super(itemView);
            tvName = itemView.findViewById(R.id.tv_doctor_name);
            tvSpecialty = itemView.findViewById(R.id.tv_doctor_specialty);
            tvRating = itemView.findViewById(R.id.tv_doctor_rating);
            tvHospital = itemView.findViewById(R.id.tv_doctor_hospital);
            tvSlot = itemView.findViewById(R.id.tv_doctor_slot);
            tvBadgeAvailability = itemView.findViewById(R.id.tv_badge_availability);
            btnCall = itemView.findViewById(R.id.btn_call_doctor);
            btnBook = itemView.findViewById(R.id.btn_book_doctor);
        }

        public void bind(DoctorEntity doc) {
            tvName.setText(doc.getName());
            tvSpecialty.setText(doc.getSpecialty());
            tvRating.setText(String.format("★ %.1f", doc.getRating()));
            tvHospital.setText(String.format("%s • %.1f km away", doc.getHospital(), doc.getDistanceKm()));
            tvSlot.setText(String.format("Next Slot: %s", doc.getNextSlot()));

            if (doc.isAvailableToday()) {
                tvBadgeAvailability.setVisibility(View.VISIBLE);
            } else {
                tvBadgeAvailability.setVisibility(View.GONE);
            }

            btnCall.setOnClickListener(v -> {
                Intent dialIntent = new Intent(Intent.ACTION_DIAL);
                dialIntent.setData(Uri.parse("tel:" + doc.getPhone()));
                itemView.getContext().startActivity(dialIntent);
            });

            btnBook.setOnClickListener(v -> {
                Toast.makeText(itemView.getContext(), "Booking slot with " + doc.getName() + " confirmed!", Toast.LENGTH_SHORT).show();
            });
        }
    }
}
