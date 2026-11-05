package com.cse2200.maacare.ui.adapters;

import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.cse2200.maacare.R;
import com.cse2200.maacare.data.db.entity.BabyCareTimelineEntity;
import java.util.ArrayList;
import java.util.List;

public class BabyTimelineAdapter extends RecyclerView.Adapter<BabyTimelineAdapter.TimelineViewHolder> {

    private final List<BabyCareTimelineEntity> eventList = new ArrayList<>();

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
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_baby_timeline, parent, false);
        return new TimelineViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull TimelineViewHolder holder, int position) {
        holder.bind(eventList.get(position));
    }

    @Override
    public int getItemCount() {
        return eventList.size();
    }

    static class TimelineViewHolder extends RecyclerView.ViewHolder {
        private final View viewCategoryIndicator;
        private final TextView tvActivityTypeBadge;
        private final TextView tvEventTime;
        private final TextView tvEventTitle;
        private final TextView tvEventDetails;

        public TimelineViewHolder(@NonNull View itemView) {
            super(itemView);
            viewCategoryIndicator = itemView.findViewById(R.id.view_category_indicator);
            tvActivityTypeBadge = itemView.findViewById(R.id.tv_activity_type_badge);
            tvEventTime = itemView.findViewById(R.id.tv_event_time);
            tvEventTitle = itemView.findViewById(R.id.tv_event_title);
            tvEventDetails = itemView.findViewById(R.id.tv_event_details);
        }

        public void bind(BabyCareTimelineEntity event) {
            tvActivityTypeBadge.setText(event.getActivityType());
            tvEventTime.setText(event.getTimeFormatted());
            tvEventTitle.setText(event.getTitle());
            tvEventDetails.setText(event.getDetails());

            if (event.getCategoryColor() != null) {
                try {
                    int color = Color.parseColor(event.getCategoryColor());
                    viewCategoryIndicator.setBackgroundColor(color);
                } catch (Exception ignored) {}
            }
        }
    }
}
