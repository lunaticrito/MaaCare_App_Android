package com.cse2200.maacare.ui.adapters;

import android.view.LayoutInflater;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.cse2200.maacare.data.model.DangerSign;
import com.cse2200.maacare.databinding.ItemDangerSignBinding;
import java.util.ArrayList;
import java.util.List;

/**
 * Adapter for Obstetric Danger Signs and Offline First-Aid Protocol Cards.
 */
public class DangerSignAdapter extends RecyclerView.Adapter<DangerSignAdapter.DangerSignViewHolder> {

    private final List<DangerSign> list = new ArrayList<>();

    public void submitList(List<DangerSign> newList) {
        list.clear();
        if (newList != null) {
            list.addAll(newList);
        }
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public DangerSignViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemDangerSignBinding binding = ItemDangerSignBinding.inflate(
                LayoutInflater.from(parent.getContext()),
                parent,
                false
        );
        return new DangerSignViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull DangerSignViewHolder holder, int position) {
        holder.bind(list.get(position));
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    public static class DangerSignViewHolder extends RecyclerView.ViewHolder {
        private final ItemDangerSignBinding binding;

        public DangerSignViewHolder(ItemDangerSignBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        public void bind(DangerSign item) {
            binding.tvDangerName.setText(item.getSignName());
            binding.tvRiskBadge.setText(item.getRiskLevel());
            binding.tvDangerDesc.setText(item.getDescription());
            binding.tvFirstAidAction.setText(item.getFirstAidAction());
        }
    }
}
