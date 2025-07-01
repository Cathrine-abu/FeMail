package com.example.femail;

import android.app.Dialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import com.example.femail.Mails.MailItem;

public class EditDraftDialog extends DialogFragment {

    private MailItem mailItem;
    private EditText subjectEditText;
    private EditText bodyEditText;
    private OnDraftEditedListener listener;

    public interface OnDraftEditedListener {
        void onDraftEdited(MailItem mailItem);
    }

    public static EditDraftDialog newInstance(MailItem mailItem) {
        EditDraftDialog dialog = new EditDraftDialog();
        dialog.mailItem = mailItem;
        return dialog;
    }

    public void setOnDraftEditedListener(OnDraftEditedListener listener) {
        this.listener = listener;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.layout_compose_mail, container, false);

        subjectEditText = view.findViewById(R.id.subjectEditText);
        bodyEditText = view.findViewById(R.id.bodyEditText);
        Button sendButton = view.findViewById(R.id.sendButton);

        if (mailItem != null) {
            subjectEditText.setText(mailItem.subject);
            bodyEditText.setText(mailItem.body);
        }

        sendButton.setOnClickListener(v -> {
            if (mailItem != null) {
                mailItem.subject = subjectEditText.getText().toString();
                mailItem.body = bodyEditText.getText().toString();
                
                if (listener != null) {
                    listener.onDraftEdited(mailItem);
                }
                
                dismiss();
            }
        });

        return view;
    }

    @Override
    public void onStart() {
        super.onStart();
        if (getDialog() != null) {
            getDialog().getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        }
    }
} 