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

import com.example.femail.Mails.MailAdapter;
import com.example.femail.Mails.MailItem;
import com.example.femail.Mails.MailViewModel;
import com.example.femail.R;
import com.example.femail.AuthPrefs;

import java.util.ArrayList;
import java.util.List;

public class PromotionsFragment extends Fragment {

    private MailViewModel mailViewModel;
    private MailAdapter mailAdapter;

    public PromotionsFragment() {}

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.layout_mail_list, container, false);

        TextView title = view.findViewById(R.id.mailListTitle);
        title.setText("Promotions");

        RecyclerView recyclerView = view.findViewById(R.id.mailListView);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        mailAdapter = new MailAdapter(getContext(), new ArrayList<>(), "promotions", null, (mail, position) -> {
            // Update the mail in the database when star is clicked
            mailViewModel.update(mail);
        });
        recyclerView.setAdapter(mailAdapter);

        mailViewModel = new ViewModelProvider(requireActivity()).get(MailViewModel.class);
        mailViewModel.getPromotionsMails(AuthPrefs.getUserId(requireContext()))
            .observe(getViewLifecycleOwner(), mails -> {
                mailAdapter.setMailList(mails);
            });

        return view;
    }
} 