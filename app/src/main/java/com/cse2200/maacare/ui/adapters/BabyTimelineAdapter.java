package com.cse2200.maacare.ui.adapters;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.cse2200.maacare.data.db.entity.BabyCareTimelineEntity;
import com.cse2200.maacare.databinding.ItemBabyTimelineBinding;
import com.cse2200.maacare.util.LocaleHelper;
import java.util.ArrayList;
import java.util.List;

/**
 * Interactive Bilingual RecyclerView Adapter for daily baby care timeline entries
 * with per-card Edit and Delete controls.
 */
public class BabyTimelineAdapter extends RecyclerView.Adapter<BabyTimelineAdapter.TimelineViewHolder> {

    public interface OnTimelineActionListener {
        void onEditTimelineEvent(BabyCareTimelineEntity event);
        void onDeleteTimelineEvent(BabyCareTimelineEntity event);
    }

    private final List<BabyCareTimelineEntity> eventList = new ArrayList<>();
    private final OnTimelineActionListener listener;

    public BabyTimelineAdapter(OnTimelineActionListener listener) {
        this.listener = listener;
    }

    public void setEvents(List<BabyCareTimelineEntity> list) {
        eventList.clear();
        if (list != null) {
            eventList.addAll(list);
        }
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public TimelineViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemBabyTimelineBinding binding = ItemBabyTimelineBinding.inflate(
                LayoutInflater.from(parent.getContext()),
                parent,
                false
        );
        return new TimelineViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull TimelineViewHolder holder, int position) {
        holder.bind(eventList.get(position), listener);
    }

    @Override
    public int getItemCount() {
        return eventList.size();
    }

    static class TimelineViewHolder extends RecyclerView.ViewHolder {
        private final ItemBabyTimelineBinding binding;

        public TimelineViewHolder(@NonNull ItemBabyTimelineBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        public void bind(BabyCareTimelineEntity event, OnTimelineActionListener listener) {
            Context ctx = binding.getRoot().getContext();
            binding.tvActivityTypeBadge.setText(LocaleHelper.translateTimelineCategory(ctx, event.getActivityType()));
            binding.tvEventTime.setText(LocaleHelper.localizeTime(ctx, event.getTimeFormatted()));
            binding.tvEventTitle.setText(LocaleHelper.translateDynamicText(ctx, event.getTitle()));
            binding.tvEventDetails.setText(LocaleHelper.translateDynamicText(ctx, event.getDetails()));

            if (event.getCategoryColor() != null) {
                try {
                    int color = Color.parseColor(event.getCategoryColor());
                    binding.viewCategoryIndicator.setBackgroundColor(color);
                } catch (Exception ignored) {}
            }

            binding.getRoot().setOnClickListener(v -> {
                if (listener != null) listener.onEditTimelineEvent(event);
            });
            binding.btnEditTimelineEvent.setOnClickListener(v -> {
                if (listener != null) listener.onEditTimelineEvent(event);
            });
            binding.btnDeleteTimelineEvent.setOnClickListener(v -> {
                if (listener != null) listener.onDeleteTimelineEvent(event);
            });
        }
    }
}
