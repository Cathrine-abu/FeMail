package com.example.femail.Mails;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.femail.R;
import java.util.List;

public class MoveCategoryAdapter extends RecyclerView.Adapter<MoveCategoryAdapter.ViewHolder> {
    private final List<String> categories;
    private final OnCategoryClickListener listener;

    public interface OnCategoryClickListener {
        void onCategoryClick(String category);
    }

    public MoveCategoryAdapter(List<String> categories, OnCategoryClickListener listener) {
        this.categories = categories;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_label, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        String category = categories.get(position);
        holder.labelName.setText(category);
        holder.itemView.setOnClickListener(v -> listener.onCategoryClick(category));
    }

    @Override
    public int getItemCount() {
        return categories.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView labelName;
        ViewHolder(View itemView) {
            super(itemView);
            labelName = itemView.findViewById(R.id.labelName);
        }
    }
} 