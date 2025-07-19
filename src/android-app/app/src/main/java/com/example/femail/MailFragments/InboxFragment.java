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
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

public class InboxFragment extends Fragment {

    private MailViewModel mailViewModel;
    private MailAdapter mailAdapter;
    private ActionMode actionMode;

    public InboxFragment() {}

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.layout_mail_list, container, false);

        TextView title = view.findViewById(R.id.mailListTitle);
        title.setText("Inbox");

        RecyclerView recyclerView = view.findViewById(R.id.mailListView);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        mailAdapter = new MailAdapter(getContext(), new ArrayList<>(), "inbox", null, (mail, position) -> {
            mailViewModel.update(mail, AuthPrefs.getToken(requireContext()), AuthPrefs.getUserId(requireContext()));
            String token = AuthPrefs.getToken(requireContext());
            String userId = AuthPrefs.getUserId(requireContext());
            mailViewModel.updateMailOnServer(token, userId, mail, success -> {
                if (success) {
                    refreshMailsFromServer();
                }
            });
        });
        recyclerView.setAdapter(mailAdapter);

        mailViewModel = new ViewModelProvider(requireActivity()).get(MailViewModel.class);
        
        // Fetch fresh mails from server when fragment loads
        refreshMailsFromServer();
        
        mailViewModel.getInboxMails(AuthPrefs.getUserId(requireContext()))
            .observe(getViewLifecycleOwner(), mails -> {
                for (MailItem mail : mails) {
                    android.util.Log.d("MailDebug", "id=" + mail.id + ", time=" + mail.time + ", subject=" + mail.subject + ", isStarred=" + mail.isStarred);
                }
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
        android.util.Log.d("InboxFragment", "refreshMailsFromServer called with token: " + (token != null ? "present" : "null") + ", userId: " + userId);
        // Use incremental refresh instead of full refresh
        mailViewModel.fetchNewMailsFromServer(token, userId).observe(getViewLifecycleOwner(), mails -> {
            if (mails != null && !mails.isEmpty()) {
                android.util.Log.d("InboxFragment", "Received " + mails.size() + " new mails from server");
            }
        });
    }

    private final ActionMode.Callback actionModeCallback = new ActionMode.Callback() {
        @Override
        public boolean onCreateActionMode(ActionMode mode, Menu menu) {
            MenuInflater inflater = mode.getMenuInflater();
            inflater.inflate(R.menu.mail_context_menu, menu);
            return true;
        }
        @Override
        public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
            int count = mailAdapter.getSelectedMails().size();
            mode.setTitle(String.valueOf(count));
            return false;
        }
        @Override
        public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
            List<com.example.femail.Mails.MailItem> selected = mailAdapter.getSelectedMails();
            if (item.getItemId() == R.id.action_delete) {
                for (com.example.femail.Mails.MailItem mail : selected) {
                    mail.isDeleted = true;
                    mailViewModel.update(mail, AuthPrefs.getToken(requireContext()), AuthPrefs.getUserId(requireContext()));
                    mailViewModel.updateMailOnServer(AuthPrefs.getToken(requireContext()), AuthPrefs.getUserId(requireContext()), mail, success -> {});
                }
                mailAdapter.clearSelection();
                mode.finish();
                return true;
            } else if (item.getItemId() == R.id.action_spam) {
                for (com.example.femail.Mails.MailItem mail : selected) {
                    mail.isSpam = true;
                    // Save current direction as previousDirection before marking as spam
                    if (mail.direction != null && !mail.direction.isEmpty()) {
                        mail.previousDirection = new java.util.ArrayList<>(mail.direction);
                    }
                    mail.direction = java.util.Arrays.asList("spam");
                    mailViewModel.update(mail, AuthPrefs.getToken(requireContext()), AuthPrefs.getUserId(requireContext()));
                    mailViewModel.markMailAsSpamOnServer(AuthPrefs.getToken(requireContext()), AuthPrefs.getUserId(requireContext()), mail);
                }
                mailAdapter.clearSelection();
                refreshMailsFromServer();
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
