package com.example.femail.MailFragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.femail.MailAdapter;
import com.example.femail.MailItem;
import com.example.femail.MailViewModel;
import com.example.femail.R;

import java.util.ArrayList;
import java.util.List;

public class SpamFragment extends Fragment {

    private MailAdapter mailAdapter;
    private MailViewModel mailViewModel;

    public SpamFragment() {}

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.layout_mail_list, container, false);

        TextView title = view.findViewById(R.id.mailListTitle);
        title.setText("Spam");

        RecyclerView recyclerView = view.findViewById(R.id.mailListView);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        mailAdapter = new MailAdapter(getContext(), new ArrayList<>());
        recyclerView.setAdapter(mailAdapter);

        mailViewModel = new ViewModelProvider(requireActivity()).get(MailViewModel.class);
        mailViewModel.getSpamMails().observe(getViewLifecycleOwner(), mails -> {
            mailAdapter.setMailList(mails);
        });

        return view;
    }
}
