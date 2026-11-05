package com.cse2200.maacare.ui.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.cse2200.maacare.R;
import com.cse2200.maacare.data.db.entity.BabyMilestoneEntity;
import java.util.ArrayList;
import java.util.List;

public class BabyMilestoneAdapter extends RecyclerView.Adapter<BabyMilestoneAdapter.MilestoneViewHolder> {

    public interface OnMilestoneUpdateListener {
        void onUpdate(BabyMilestoneEntity milestone, boolean isAchieved, String remarks);
    }

    private final List<BabyMilestoneEntity> milestoneList = new ArrayList<>();
    private final OnMilestoneUpdateListener listener;

    public BabyMilestoneAdapter(OnMilestoneUpdateListener listener) {
        this.listener = listener;
    }

    public void setMilestones(List<BabyMilestoneEntity> list) {
        milestoneList.clear();
        if (list != null) {
            milestoneList.addAll(list);
        }
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public MilestoneViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_baby_milestone, parent, false);
        return new MilestoneViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MilestoneViewHolder holder, int position) {
        holder.bind(milestoneList.get(position), listener);
    }

    @Override
    public int getItemCount() {
        return milestoneList.size();
    }

    static class MilestoneViewHolder extends RecyclerView.ViewHolder {
        private final TextView tvCategory;
        private final TextView tvQuestion;
        private final RadioGroup rgAchieved;
        private final RadioButton rbYes;
        private final RadioButton rbNo;
        private final EditText etRemark;
        private final Button btnSave;

        public MilestoneViewHolder(@NonNull View itemView) {
            super(itemView);
            tvCategory = itemView.findViewById(R.id.tv_milestone_category);
            tvQuestion = itemView.findViewById(R.id.tv_milestone_question);
            rgAchieved = itemView.findViewById(R.id.rg_milestone_achieved);
            rbYes = itemView.findViewById(R.id.rb_milestone_yes);
            rbNo = itemView.findViewById(R.id.rb_milestone_no);
            etRemark = itemView.findViewById(R.id.et_milestone_remark);
            btnSave = itemView.findViewById(R.id.btn_save_milestone);
        }

        public void bind(BabyMilestoneEntity item, OnMilestoneUpdateListener listener) {
            tvCategory.setText(item.getCategory());
            tvQuestion.setText(item.getQuestionText());

            if (item.isAchieved()) {
                rbYes.setChecked(true);
            } else {
                rbNo.setChecked(true);
            }

            if (item.getRemarkText() != null) {
                etRemark.setText(item.getRemarkText());
            } else {
                etRemark.setText("");
            }

            btnSave.setOnClickListener(v -> {
                boolean achieved = rbYes.isChecked();
                String remark = etRemark.getText() != null ? etRemark.getText().toString().trim() : "";
                if (listener != null) {
                    listener.onUpdate(item, achieved, remark);
                    Toast.makeText(itemView.getContext(), "Milestone observation saved!", Toast.LENGTH_SHORT).show();
                }
            });
        }
    }
}
