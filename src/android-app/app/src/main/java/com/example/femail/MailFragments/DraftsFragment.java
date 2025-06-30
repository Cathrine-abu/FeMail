package com.example.femail.MailFragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.femail.EditDraftDialog;
import com.example.femail.MailAdapter;
import com.example.femail.MailItem;
import com.example.femail.MailViewModel;
import com.example.femail.R;

import java.util.ArrayList;
import java.util.List;

public class DraftsFragment extends Fragment implements MailAdapter.OnMailClickListener {

    private MailViewModel mailViewModel;
    private MailAdapter mailAdapter;
    private RecyclerView recyclerView;

    public DraftsFragment() {}

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.layout_mail_list, container, false);

        recyclerView = view.findViewById(R.id.mailListView);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        mailAdapter = new MailAdapter(getContext(), new ArrayList<>(), this);
        recyclerView.setAdapter(mailAdapter);

        mailViewModel = new ViewModelProvider(requireActivity()).get(MailViewModel.class);

        mailViewModel.getDraftMails().observe(getViewLifecycleOwner(), mails -> {
            mailAdapter.setMailList(mails);
        });

        return view;
    }

    @Override
    public void onMailClick(MailItem mail) {
        if (mail.isDraft) {
            EditDraftDialog dialog = EditDraftDialog.newInstance(mail);
            dialog.show(getParentFragmentManager(), "EditDraftDialog");
        }
    }
} 