package com.example.femail.MailFragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.view.ActionMode;

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

public class SpamFragment extends Fragment {

    private MailAdapter mailAdapter;
    private MailViewModel mailViewModel;
    private ActionMode actionMode;

    public SpamFragment() {}

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.layout_mail_list, container, false);

        TextView title = view.findViewById(R.id.mailListTitle);
        title.setText("Spam");

        RecyclerView recyclerView = view.findViewById(R.id.mailListView);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        mailAdapter = new MailAdapter(getContext(), new ArrayList<>(), "spam", null, (mail, position) -> {
            // Update the mail in the database when star is clicked
            mailViewModel.update(mail, AuthPrefs.getToken(requireContext()), AuthPrefs.getUserId(requireContext()));
            String token = AuthPrefs.getToken(requireContext());
            mailViewModel.updateMailOnServerWithRoom(token, AuthPrefs.getUserId(requireContext()), mail);
            mailViewModel.fetchMailsFromServer(token, AuthPrefs.getUserId(requireContext()));
            refreshMailsFromServer();
        });
        recyclerView.setAdapter(mailAdapter);

        mailViewModel = new ViewModelProvider(requireActivity()).get(MailViewModel.class);
        
        // Fetch fresh mails from server when fragment loads
        refreshMailsFromServer();
        
        // Use the correct LiveData method for spam mails (newest first)
        // getSpamMails() returns LiveData and is correctly implemented in MailViewModel
        mailViewModel.getSpamMails(AuthPrefs.getUserId(requireContext()))
            .observe(getViewLifecycleOwner(), mails -> {
                mailAdapter.setMailList(mails);
                recyclerView.scrollToPosition(0);
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
                // In Spam, this acts as 'unspam'
                for (MailItem mail : selected) {
                    mail.isSpam = false;
                    // Move mail to previous direction if available, else to inbox
                    if (mail.previousDirection != null && !mail.previousDirection.isEmpty()) {
                        mail.direction = new java.util.ArrayList<>(mail.previousDirection);
                    } else {
                        mail.direction = java.util.Arrays.asList("inbox");
                    }
                    mailViewModel.update(mail, token, userId);
                    mailViewModel.updateMailOnServer(token, userId, mail, success -> {});
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
