package com.example.femail.labels;

import android.app.Dialog;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import com.example.femail.R;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class LabelSelectionDialog extends DialogFragment {

    public interface OnLabelsSelectedListener {
        void onLabelsSelected(List<LabelItem> selectedLabels);
    }

    private OnLabelsSelectedListener listener;
    private List<LabelItem> allLabels = new ArrayList<>();
    private Set<Integer> selectedLabelIds = new HashSet<>();

    public LabelSelectionDialog(List<LabelItem> allLabels, List<LabelItem> selectedLabels) {
        this.allLabels = allLabels;
        if (selectedLabels != null) {
            for (LabelItem label : selectedLabels) {
                selectedLabelIds.add(label.getId());
            }
        }
    }

    public void setOnLabelsSelectedListener(OnLabelsSelectedListener listener) {
        this.listener = listener;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        Dialog dialog = new Dialog(requireContext());
        dialog.setContentView(R.layout.dialog_label_selection);

        TextView title = dialog.findViewById(R.id.label_dialog_title);
        title.setText("Label");

        LinearLayout labelList = dialog.findViewById(R.id.label_list_container);

        for (LabelItem label : allLabels) {
            View item = getLayoutInflater().inflate(R.layout.item_label_checkbox, labelList, false);
            CheckBox checkBox = item.findViewById(R.id.label_checkbox);
            checkBox.setText(label.getName());
            checkBox.setChecked(selectedLabelIds.contains(label.getId()));
            checkBox.setOnCheckedChangeListener((buttonView, isChecked) -> {
                if (isChecked) selectedLabelIds.add(label.getId());
                else selectedLabelIds.remove(label.getId());
            });
            labelList.addView(item);
        }

        Button cancelBtn = dialog.findViewById(R.id.label_dialog_cancel);
        Button doneBtn = dialog.findViewById(R.id.label_dialog_done);

        cancelBtn.setOnClickListener(v -> dismiss());
        doneBtn.setOnClickListener(v -> {
            List<LabelItem> selected = new ArrayList<>();
            for (LabelItem label : allLabels) {
                if (selectedLabelIds.contains(label.getId())) selected.add(label);
            }
            if (listener != null) listener.onLabelsSelected(selected);
            dismiss();
        });

        return dialog;
    }
} 