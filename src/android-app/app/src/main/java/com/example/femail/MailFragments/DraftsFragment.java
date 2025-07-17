package com.example.femail.MailFragments;

import android.app.Activity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.femail.EditDraftDialog;
import com.example.femail.Mails.MailAdapter;
import com.example.femail.Mails.MailItem;
import com.example.femail.Mails.MailViewModel;
import com.example.femail.MailActivity;
import com.example.femail.R;
import com.example.femail.AuthPrefs;

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

        TextView title = view.findViewById(R.id.mailListTitle);
        title.setText("Drafts");

        recyclerView = view.findViewById(R.id.mailListView);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        mailAdapter = new MailAdapter(getContext(), new ArrayList<>(), "drafts", this, (mail, position) -> {
            // Update the mail in the database when star is clicked
            mailViewModel.update(mail, AuthPrefs.getToken(requireContext()), AuthPrefs.getUserId(requireContext()));
            String token = AuthPrefs.getToken(requireContext());
            String userId = AuthPrefs.getUserId(requireContext());
            mailViewModel.updateMailOnServer(token, userId, mail, success -> {});
            refreshMailsFromServer();
        });
        recyclerView.setAdapter(mailAdapter);

        mailViewModel = new ViewModelProvider(requireActivity()).get(MailViewModel.class);

        mailViewModel.getDraftMails(AuthPrefs.getUserId(requireContext()))
            .observe(getViewLifecycleOwner(), mails -> {
                mailAdapter.setMailList(mails);
            });

        return view;
    }

    @Override
    public void onMailClick(MailItem mail) {
        if (mail.isDraft) {
            Activity activity = getActivity();
            if (activity instanceof MailActivity) {
                ((MailActivity) activity).showCreateOrEditMailPopup(mail);
            }
        }
    }

    private void refreshMailsFromServer() {
        String token = AuthPrefs.getToken(requireContext());
        String userId = AuthPrefs.getUserId(requireContext());
        mailViewModel.fetchMailsFromServer(token, userId).observe(getViewLifecycleOwner(), mails -> {
            for (MailItem mail : mails) {
                mailViewModel.update(mail, token, userId);
            }
        });
    }
} 