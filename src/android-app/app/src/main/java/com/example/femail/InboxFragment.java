package com.example.femail;

import android.os.Bundle;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.femail.MailAdapter;
import com.example.femail.MailViewModel;
import com.example.femail.R;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;

public class InboxFragment extends Fragment {

    private MailViewModel mailViewModel;
    private MailAdapter mailAdapter;

    public InboxFragment() {}

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_inbox, container, false);

        RecyclerView recyclerView = view.findViewById(R.id.mail_recycler_view);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        mailAdapter = new MailAdapter(getContext(), new ArrayList<>());
        recyclerView.setAdapter(mailAdapter);

        mailViewModel = new ViewModelProvider(requireActivity()).get(MailViewModel.class);
        mailViewModel.getAllMails().observe(getViewLifecycleOwner(), mails -> {
            mailAdapter.setMailList(mails);
        });

        return view;
    }
}
