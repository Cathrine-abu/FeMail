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
import com.example.femail.Mails.MailDao;
import com.example.femail.Mails.MailDatabase;
import com.example.femail.MailActivity;
import com.example.femail.R;
import com.example.femail.AuthPrefs;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import android.view.ActionMode;

public class DraftsFragment extends Fragment implements MailAdapter.OnMailClickListener {

    private MailViewModel mailViewModel;
    private MailAdapter mailAdapter;
    private RecyclerView recyclerView;
    private ActionMode actionMode;
    private ExecutorService executorService = Executors.newSingleThreadExecutor();

    public DraftsFragment() {}

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.layout_mail_list, container, false);

        TextView title = view.findViewById(R.id.mailListTitle);
        title.setText("Drafts");

        recyclerView = view.findViewById(R.id.mailListView);
        mailAdapter = new MailAdapter(getContext(), new ArrayList<>(), "drafts", this, (mail, position) -> {
            // Update the mail in the database when star is clicked
            mailViewModel.update(mail, AuthPrefs.getToken(requireContext()), AuthPrefs.getUserId(requireContext()));
            String token = AuthPrefs.getToken(requireContext());
            String userId = AuthPrefs.getUserId(requireContext());
            mailViewModel.updateMailOnServer(token, userId, mail, success -> {});
            refreshMailsFromServer();
        });
        recyclerView.setAdapter(mailAdapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        mailViewModel = new ViewModelProvider(requireActivity()).get(MailViewModel.class);

        String userId = AuthPrefs.getUserId(requireContext());
        String token = AuthPrefs.getToken(requireContext());

        // Debug: Log all mails in database
        mailViewModel.debugAllMails(userId);
        
        // Observe drafts from database
        mailViewModel.getDraftMails(userId).observe(getViewLifecycleOwner(), mails -> {
            android.util.Log.d("DraftsFragment", "Received " + (mails != null ? mails.size() : 0) + " drafts from database");
            if (mails != null) {
                for (MailItem mail : mails) {
                    android.util.Log.d("DraftsFragment", "Draft: id=" + mail.id + ", subject=" + mail.subject + ", isDraft=" + mail.isDraft + ", direction=" + mail.direction + ", userId=" + mail.userId);
                }
            } else {
                android.util.Log.w("DraftsFragment", "Received null mails list");
            }
            
            // Debug: Also check what's in the database directly
            MailDao mailDao = MailDatabase.getDatabase(requireContext()).mailDao();
            executorService.execute(() -> {
                List<MailItem> allMails = mailDao.getAllMailsDebug(userId);
                android.util.Log.d("DraftsFragment", "Direct DB check - total mails: " + (allMails != null ? allMails.size() : 0));
                if (allMails != null) {
                    for (MailItem mail : allMails) {
                        if (mail.isDraft) {
                            android.util.Log.d("DraftsFragment", "Found draft in DB: id=" + mail.id + ", isDraft=" + mail.isDraft + ", direction=" + mail.direction);
                        }
                    }
                }
            });
            
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

    private final ActionMode.Callback actionModeCallback = new ActionMode.Callback() {
        @Override
        public boolean onCreateActionMode(ActionMode mode, android.view.Menu menu) {
            mode.getMenuInflater().inflate(R.menu.mail_context_menu, menu);
            // Optionally hide spam/unspam actions for drafts
            menu.findItem(R.id.action_spam).setVisible(false);
            //menu.findItem(R.id.action_unspam).setVisible(false);
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