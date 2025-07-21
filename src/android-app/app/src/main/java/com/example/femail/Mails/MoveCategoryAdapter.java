package com.example.femail.Mails;

import android.content.Context;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.femail.R;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class MoveCategoryAdapter extends RecyclerView.Adapter<MoveCategoryAdapter.ViewHolder> {
    private final Map<String, String> categories;
    private final List<String> keys;
    private final OnCategoryClickListener listener;

    public interface OnCategoryClickListener {
        void onCategoryClick(String categoryId, String categoryName);
    }

    public MoveCategoryAdapter(Map<String, String> categories, OnCategoryClickListener listener) {
        this.categories = categories;
        this.keys = new ArrayList<>(categories.keySet());
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
        String categoryId = keys.get(position);
        String categoryName = categories.get(categoryId);
        holder.labelName.setText(categoryName);

        // Resolve `colorOnSurface` from the current theme
        TypedValue typedValue = new TypedValue();
        Context context = holder.itemView.getContext();
        context.getTheme().resolveAttribute(com.google.android.material.R.attr.colorOnSurface, typedValue, true);
        int colorOnSurface = typedValue.data;
        holder.labelName.setTextColor(colorOnSurface);
        holder.itemView.setOnClickListener(v -> listener.onCategoryClick(categoryId, categoryName));
    }

    @Override
    public int getItemCount() {
        return keys.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView labelName;
        ViewHolder(View itemView) {
            super(itemView);
            labelName = itemView.findViewById(R.id.labelName);
        }
    }
}