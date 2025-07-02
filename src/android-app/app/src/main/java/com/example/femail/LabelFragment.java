package com.example.femail;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.femail.Mails.MailAdapter;
import com.example.femail.Mails.MailItem;
import com.example.femail.Mails.MailViewModel;
import com.example.femail.AuthPrefs;
import java.util.ArrayList;
import java.util.List;

public class LabelFragment extends Fragment {
    private static final String ARG_LABEL_NAME = "label_name";
    private MailViewModel mailViewModel;
    private MailAdapter mailAdapter;
    private RecyclerView recyclerView;
    private String labelName;

    public static LabelFragment newInstance(String labelName) {
        LabelFragment fragment = new LabelFragment();
        Bundle args = new Bundle();
        args.putString(ARG_LABEL_NAME, labelName);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_label, container, false);
        recyclerView = view.findViewById(R.id.recycler_view_label_mails);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        mailAdapter = new MailAdapter(getContext(), new ArrayList<>(), "label");
        recyclerView.setAdapter(mailAdapter);
        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        if (getArguments() != null) {
            labelName = getArguments().getString(ARG_LABEL_NAME);
        }
        mailViewModel = new ViewModelProvider(requireActivity()).get(MailViewModel.class);
        if (labelName != null) {
            mailViewModel.getMailsByLabel(labelName, AuthPrefs.getUserId(requireContext()))
                .observe(getViewLifecycleOwner(), mails -> {
                    mailAdapter.setMailList(mails != null ? mails : new ArrayList<>());
                });
        }
    }
} 