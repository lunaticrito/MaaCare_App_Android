package com.cse2200.maacare.ui.adapters;

import android.graphics.Paint;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.cse2200.maacare.R;
import com.cse2200.maacare.data.db.entity.BabyEssentialItemEntity;
import java.util.ArrayList;
import java.util.List;

public class EssentialsAdapter extends RecyclerView.Adapter<EssentialsAdapter.EssentialViewHolder> {

    public interface OnEssentialCheckChangeListener {
        void onCheckChanged(BabyEssentialItemEntity item, boolean isChecked);
    }

    private final List<BabyEssentialItemEntity> itemsList = new ArrayList<>();
    private final OnEssentialCheckChangeListener listener;

    public EssentialsAdapter(OnEssentialCheckChangeListener listener) {
        this.listener = listener;
    }

    public void setItems(List<BabyEssentialItemEntity> list) {
        itemsList.clear();
        if (list != null) {
            itemsList.addAll(list);
        }
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public EssentialViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_essential_checklist, parent, false);
        return new EssentialViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull EssentialViewHolder holder, int position) {
        holder.bind(itemsList.get(position), listener);
    }

    @Override
    public int getItemCount() {
        return itemsList.size();
    }

    static class EssentialViewHolder extends RecyclerView.ViewHolder {
        private final CheckBox cbItem;
        private final TextView tvItemName;
        private final TextView tvCategoryPill;

        public EssentialViewHolder(@NonNull View itemView) {
            super(itemView);
            cbItem = itemView.findViewById(R.id.cb_essential_item);
            tvItemName = itemView.findViewById(R.id.tv_essential_item_name);
            tvCategoryPill = itemView.findViewById(R.id.tv_essential_category_pill);
        }

        public void bind(BabyEssentialItemEntity item, OnEssentialCheckChangeListener listener) {
            tvItemName.setText(item.getItemName());
            tvCategoryPill.setText(item.getCategory());

            cbItem.setOnCheckedChangeListener(null);
            cbItem.setChecked(item.isChecked());
            updateStrikethrough(item.isChecked());

            cbItem.setOnCheckedChangeListener((buttonView, isChecked) -> {
                item.setChecked(isChecked);
                updateStrikethrough(isChecked);
                if (listener != null) {
                    listener.onCheckChanged(item, isChecked);
                }
            });
        }

        private void updateStrikethrough(boolean checked) {
            if (checked) {
                tvItemName.setPaintFlags(tvItemName.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
                tvItemName.setAlpha(0.6f);
            } else {
                tvItemName.setPaintFlags(tvItemName.getPaintFlags() & (~Paint.STRIKE_THRU_TEXT_FLAG));
                tvItemName.setAlpha(1.0f);
            }
        }
    }
}
