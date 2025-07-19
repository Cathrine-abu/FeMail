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

import android.view.ActionMode;

public class StarredFragment extends Fragment {

    private MailViewModel mailViewModel;
    private MailAdapter mailAdapter;
    private RecyclerView recyclerView;
    private ActionMode actionMode;

    public StarredFragment() {}

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.layout_mail_list, container, false);

        TextView title = view.findViewById(R.id.mailListTitle);
        title.setText("Starred");

        recyclerView = view.findViewById(R.id.mailListView);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        mailAdapter = new MailAdapter(getContext(), new ArrayList<>(), "starred", null, (mail, position) -> {
            // Update the mail in the database when star is clicked
            mailViewModel.update(mail, AuthPrefs.getToken(requireContext()), AuthPrefs.getUserId(requireContext()));
            String token = AuthPrefs.getToken(requireContext());
            mailViewModel.updateMailOnServer(token, AuthPrefs.getUserId(requireContext()), mail, success -> {});
            mailViewModel.fetchMailsFromServer(token, AuthPrefs.getUserId(requireContext()));
        });
        recyclerView.setAdapter(mailAdapter);

        mailViewModel = new ViewModelProvider(requireActivity()).get(MailViewModel.class);

        mailViewModel.getStarredMails(AuthPrefs.getUserId(requireContext()))
            .observe(getViewLifecycleOwner(), mails -> {
                mailAdapter.setMailList(mails);
            });

        mailAdapter.setSelectionListener((enabled, selectedCount) -> {
            if (enabled && actionMode == null) {
                actionMode = requireActivity().startActionMode(actionModeCallback);
            }
            if (actionMode != null) {
                actionMode.setTitle(String.valueOf(selectedCount));
                if (!enabled) {
                    actionMode.finish();
                }
            }
        });

        return view;
    }

    private void refreshMailsFromServer() {
        String token = AuthPrefs.getToken(requireContext());
        String userId = AuthPrefs.getUserId(requireContext());
        mailViewModel.fetchMailsFromServer(token, userId).observe(getViewLifecycleOwner(), mails -> {
            for (MailItem mail : mails) {
                mailViewModel.update(mail, AuthPrefs.getToken(requireContext()), AuthPrefs.getUserId(requireContext()));
            }
        });
    }

    private final ActionMode.Callback actionModeCallback = new ActionMode.Callback() {
        @Override
        public boolean onCreateActionMode(ActionMode mode, android.view.Menu menu) {
            mode.getMenuInflater().inflate(R.menu.mail_context_menu, menu);
            return true;
        }
        @Override
        public boolean onPrepareActionMode(ActionMode mode, android.view.Menu menu) {
            int count = mailAdapter.getSelectedMails().size();
            mode.setTitle(String.valueOf(count));
            return false;
        }
        @Override
        public boolean onActionItemClicked(ActionMode mode, android.view.MenuItem item) {
            List<MailItem> selected = mailAdapter.getSelectedMails();
            String token = AuthPrefs.getToken(requireContext());
            String userId = AuthPrefs.getUserId(requireContext());
            if (item.getItemId() == R.id.action_delete) {
                for (MailItem mail : selected) {
                    mail.isDeleted = true;
                    mailViewModel.update(mail, token, userId);
                    mailViewModel.updateMailOnServer(token, userId, mail, success -> {});
                }
                mailAdapter.clearSelection();
                mode.finish();
                return true;
            } else if (item.getItemId() == R.id.action_spam) {
                for (MailItem mail : selected) {
                    mail.isSpam = true;
                    // Save current direction as previousDirection before marking as spam
                    if (mail.direction != null && !mail.direction.isEmpty()) {
                        mail.previousDirection = new java.util.ArrayList<>(mail.direction);
                    }
                    mail.direction = java.util.Arrays.asList("spam");
                    mailViewModel.update(mail, token, userId);
                    mailViewModel.markMailAsSpamOnServer(token, userId, mail);
                }
                mailAdapter.clearSelection();
                mode.finish();
                return true;
            }
            return false;
        }
        @Override
        public void onDestroyActionMode(ActionMode mode) {
            mailAdapter.clearSelection();
            actionMode = null;
        }
    };
}
