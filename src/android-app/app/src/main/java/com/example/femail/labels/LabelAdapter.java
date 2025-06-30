package com.example.femail.labels;

import android.content.Context;
import android.content.Intent;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.femail.R;

import java.util.ArrayList;
import java.util.List;

public class LabelAdapter extends RecyclerView.Adapter<LabelAdapter.LabelViewHolder> {

    private List<LabelItem> labelList = new ArrayList<>();
    private Context context;
    //private List<LabelItem> labelItems;

    public LabelAdapter(Context context, List<LabelItem> labelList) {
        this.context = context;
        this.labelList = labelList;
    }

    public void setLabelList(List<LabelItem> labels) {
        this.labelList = labels;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public LabelViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_label, parent, false);
        return new LabelViewHolder(view);
    }
    // from(context) ?

    @Override
    public void onBindViewHolder(@NonNull LabelViewHolder holder, int position) {
        LabelItem label = labelList.get(position);
        holder.labelName.setText(label.getName());
    }

    @Override
    public int getItemCount() {
        return labelList == null ? 0 : labelList.size();
    }

    public static class LabelViewHolder extends RecyclerView.ViewHolder {
        TextView labelName;

        public LabelViewHolder(@NonNull View itemView) {
            super(itemView);
            labelName = itemView.findViewById(R.id.labelName);
        }
    }
}
