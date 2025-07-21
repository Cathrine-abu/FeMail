package com.example.femail;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.femail.Mails.MailItem;
import com.example.femail.Mails.MailViewModel;
import com.example.femail.AuthPrefs;
import com.example.femail.Mails.MoveCategoryAdapter;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class ViewMail extends AppCompatActivity {

    private MailViewModel mailViewModel;
    private MailItem currentMail;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.d("DEBUG_INTENT", "subject: " + getIntent().getStringExtra("subject"));

        super.onCreate(savedInstanceState);
        setContentView(R.layout.layout_view_mail);

        String subject = getIntent().getStringExtra("mail_subject");
        String time = getIntent().getStringExtra("mail_time");
        String body = getIntent().getStringExtra("mail_body");
        String from = getIntent().getStringExtra("mail_from");
        String to = getIntent().getStringExtra("mail_to");
        String mailId = getIntent().getStringExtra("mail_id");
        String sourceFragment = getIntent().getStringExtra("source_fragment");
        
        ArrayList<String> direction = getIntent().getStringArrayListExtra("direction");
        
        // Initialize ViewModel
        mailViewModel = new ViewModelProvider(this).get(MailViewModel.class);
        
        // Create current mail object for operations
        String userId = AuthPrefs.getUserId(this);
        currentMail = new MailItem(
            mailId,
            subject,
            body,
            from,
            to != null && !to.isEmpty() ? java.util.List.of(to) : null,
            time,
            getIntent().getBooleanExtra("starred", false),
            true, // isRead
            getIntent().getBooleanExtra("isSpam", false), // isSpam
            false, // isDraft
            getIntent().getBooleanExtra("isDeleted", false), // isDeleted
            direction != null ? direction : java.util.List.of("inbox"),
            userId, // user
            userId, // owner
            null,
            "inbox",
            false,
            userId // userId
        );
        TextView detailFrom = findViewById(R.id.detail_from);
        TextView detailTo = findViewById(R.id.detail_to);
        TextView detailDate = findViewById(R.id.detail_date);

        // Initialize views
        TextView subjectView = findViewById(R.id.view_mail_subject);
        TextView timeView = findViewById(R.id.view_mail_time);
        TextView bodyView = findViewById(R.id.view_mail_body);
        TextView fromView = findViewById(R.id.view_mail_from);

        ImageView starView = findViewById(R.id.view_mail_star);
        ImageView backButton = findViewById(R.id.back_button);
        ImageView trashButton = findViewById(R.id.trash_button);

        // Display sender/recipient information based on mail direction
        if (direction != null && direction.contains("sent")) {
            // For sent mails, show "To: [recipients]" in the main view
            detailFrom.setText("From: " + from);
            detailTo.setText("To: " + to);
            detailDate.setText("Date: " + time);
            
            subjectView.setText(subject);
            timeView.setText(time);
            bodyView.setText(body);
            fromView.setText("To: " + to);
        } else {
            // For received mails, show "From: [sender]" in the main view
            detailFrom.setText("From: " + from);
            detailTo.setText("To: " + to);
            detailDate.setText("Date: " + time);
            
            subjectView.setText(subject);
            timeView.setText(time);
            bodyView.setText(body);
            fromView.setText("From: " + from);
        }

        final boolean[] isStarred = { getIntent().getBooleanExtra("starred", false) };

        starView.setImageResource(isStarred[0] ? R.drawable.ic_star_filled : R.drawable.ic_star_border);

        // Hide star icon if mail is in trash
        boolean isDeleted = getIntent().getBooleanExtra("isDeleted", false);
        boolean inTrash = isDeleted || (direction != null && direction.contains("trash"));
        if (inTrash) {
            starView.setVisibility(View.GONE);
        } else {
            starView.setVisibility(View.VISIBLE);
        }

        starView.setOnClickListener(v -> {
            isStarred[0] = !isStarred[0];
            starView.setImageResource(isStarred[0] ? R.drawable.ic_star_filled : R.drawable.ic_star_border);
            // Update the mail in database
            currentMail.isStarred = isStarred[0];
            mailViewModel.update(currentMail, AuthPrefs.getToken(this), AuthPrefs.getUserId(this));
            String token = AuthPrefs.getToken(this);
            //String userId = AuthPrefs.getUserId(this);
            mailViewModel.updateMailOnServer(token, userId, currentMail, success -> {});
            mailViewModel.fetchMailsFromServer(token, userId);
        });
        backButton.setOnClickListener(v -> finish());
        trashButton.setOnClickListener(v -> {
            if (currentMail.isDeleted || (currentMail.direction != null && currentMail.direction.contains("trash"))) {
                // Permanently delete
                mailViewModel.delete(currentMail, AuthPrefs.getToken(this), AuthPrefs.getUserId(this));
                Toast.makeText(this, "Mail deleted forever", Toast.LENGTH_SHORT).show();
            } else {
                // Move to trash
                currentMail.isDeleted = true;
                currentMail.direction = java.util.List.of("trash");
                currentMail.owner = userId;
                currentMail.user = userId;
                mailViewModel.update(currentMail, AuthPrefs.getToken(this), AuthPrefs.getUserId(this));
                String token = AuthPrefs.getToken(this);
                mailViewModel.updateMailOnServer(token, userId, currentMail, success -> {});
                mailViewModel.fetchMailsFromServer(token, userId);
                Toast.makeText(this, "Moved to trash", Toast.LENGTH_SHORT).show();
            }
            finish();
        });

        ImageView dropdownIcon = findViewById(R.id.dropdown_icon);
        LinearLayout mailDetailsContainer = findViewById(R.id.mail_details_container);

        dropdownIcon.setOnClickListener(v -> {
            if (mailDetailsContainer.getVisibility() == View.GONE) {
                mailDetailsContainer.setVisibility(View.VISIBLE);
            } else {
                mailDetailsContainer.setVisibility(View.GONE);
            }
        });

        ImageView menuButton = findViewById(R.id.menu_button);

        menuButton.setOnClickListener(v -> {
            PopupMenu popupMenu = new PopupMenu(ViewMail.this, v);
            popupMenu.getMenuInflater().inflate(R.menu.mail_options_menu, popupMenu.getMenu());

            // Hide 'Move' option if not from inbox or trash
            if (sourceFragment == null || (!sourceFragment.equals("inbox") && !sourceFragment.equals("trash"))) {
                popupMenu.getMenu().findItem(R.id.action_move).setVisible(false);
            }

            // If mail is spam, show 'Unspam' instead of 'Spam'
            if (currentMail.isSpam) {
                popupMenu.getMenu().findItem(R.id.action_spam).setTitle("Unspam");
            }

            popupMenu.setOnMenuItemClickListener(item -> {
                int id = item.getItemId();
                if (id == R.id.action_move) {
                    showMoveMailDialog(currentMail);
                    return true;
                } else if (id == R.id.action_spam) {
                    String token = AuthPrefs.getToken(this);
                    if (currentMail.isSpam) {
                        // Unspam
                        currentMail.isSpam = false;
                        if (currentMail.previousDirection != null && !currentMail.previousDirection.isEmpty()) {
                            currentMail.direction = currentMail.previousDirection;
                        } else {
                            currentMail.direction = java.util.List.of("inbox");
                        }
                    } else {
                        // Mark as spam
                        currentMail.isSpam = true;
                        // Save current direction as previousDirection before marking as spam
                        if (currentMail.direction != null && !currentMail.direction.isEmpty()) {
                            currentMail.previousDirection = new java.util.ArrayList<>(currentMail.direction);
                        }
                        currentMail.direction = java.util.List.of("spam");
                    }
                    mailViewModel.update(currentMail, token, AuthPrefs.getUserId(this));
                    mailViewModel.updateMailOnServer(token, AuthPrefs.getUserId(this), currentMail, success -> {});
                    mailViewModel.fetchMailsFromServer(token, AuthPrefs.getUserId(this));
                    finish();
                    return true;
                } else if (id == R.id.action_label) {
                    // Show label selection dialog
                    com.example.femail.labels.LabelViewModel labelViewModel = new androidx.lifecycle.ViewModelProvider(this).get(com.example.femail.labels.LabelViewModel.class);
                    labelViewModel.getAllLabels(AuthPrefs.getUserId(this)).observe(this, labels -> {
                        com.example.femail.labels.LabelSelectionDialog dialog =
                            new com.example.femail.labels.LabelSelectionDialog(labels, null); // Pass current mail's labels if you have them
                        dialog.setOnLabelsSelectedListener(selectedLabels -> {
                            // TODO: Save selected labels to the mail (update MailItem and database)
                            Toast.makeText(this, "Selected: " + selectedLabels.size() + " labels", Toast.LENGTH_SHORT).show();
                        });
                        dialog.show(getSupportFragmentManager(), "LabelSelectionDialog");
                    });
                    return true;
                }
                return false;
            });

            popupMenu.show();
        });


    }

    private void showMoveMailDialog(MailItem mail) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View view = getLayoutInflater().inflate(R.layout.dialog_move_mail, null);
        builder.setView(view);

        RecyclerView categoryList = view.findViewById(R.id.categoryList);
        categoryList.setLayoutManager(new LinearLayoutManager(this));

        Map<String, String> categories = new LinkedHashMap<>();
        categories.put("Primary", "Primary");
        categories.put("Social", "Social");
        categories.put("Promotions", "Promotions");
        categories.put("Updates", "Primary");
        final AlertDialog[] dialogHolder = new AlertDialog[1];

        // Fetch user labels and update the adapter when loaded
        com.example.femail.labels.LabelViewModel labelViewModel = new androidx.lifecycle.ViewModelProvider(this).get(com.example.femail.labels.LabelViewModel.class);
        labelViewModel.getAllLabels(AuthPrefs.getUserId(this)).observe(this, labels -> {
            if (labels != null) {
                for (com.example.femail.labels.LabelItem label : labels) {
                    categories.put(String.valueOf(label.getId()), label.getName());
                }
            }
            MoveCategoryAdapter adapter = new MoveCategoryAdapter(categories, (categoryId, categoryName) -> {
                moveMailToCategory(mail, categoryId, categoryName);
                dialogHolder[0].dismiss();
            });
            categoryList.setAdapter(adapter);
        });
        dialogHolder[0] = builder.create();
        dialogHolder[0].show();
    }

    private void moveMailToCategory(MailItem mail, String categoryId, String categoryName) {
        String token = AuthPrefs.getToken(this);
        String userId = AuthPrefs.getUserId(this);
        if (categoryId.equalsIgnoreCase("Primary")) {
            mail.category = "primary";
            mail.direction = new ArrayList<>(Arrays.asList("inbox", "primary"));
            mail.isDeleted = false;
            mailViewModel.update(mail, AuthPrefs.getToken(this), AuthPrefs.getUserId(this));
            mailViewModel.updateMailOnServer(token, userId, mail, success -> {});
            mailViewModel.fetchMailsFromServer(token, userId);
            Toast.makeText(this, "Mail moved to Primary (Inbox)", Toast.LENGTH_SHORT).show();
            return;
        }
        mail.category = categoryId.toLowerCase();
        // Remove "inbox" from direction and add the new category
        if (mail.direction != null) {
            List<String> newDirection = new ArrayList<>(mail.direction);
            newDirection.remove("inbox");
            if (!newDirection.contains(categoryId.toLowerCase())) {
                newDirection.add(categoryId.toLowerCase());
            }
            mail.direction = newDirection;
        } else {
            mail.direction = new ArrayList<>(Arrays.asList(categoryId.toLowerCase()));
        }
        // Remove from trash
        mail.isDeleted = false;
        mailViewModel.update(mail, AuthPrefs.getToken(this), AuthPrefs.getUserId(this));
        mailViewModel.updateMailOnServer(token, userId, mail,success -> {});
        mailViewModel.fetchMailsFromServer(token, userId);
        Toast.makeText(this, "Mail moved to " + categoryName, Toast.LENGTH_SHORT).show();
        refreshMailsFromServer();
    }

    private void refreshMailsFromServer() {
        String token = AuthPrefs.getToken(this);
        String userId = AuthPrefs.getUserId(this);
        mailViewModel.fetchMailsFromServer(token, userId).observe(this, mails -> {
            for (MailItem mail : mails) {
                mailViewModel.update(mail, AuthPrefs.getToken(this), AuthPrefs.getUserId(this));
            }
        });
    }

    private void moveMailToTrash(MailItem mail) {
        mail.isDeleted = true;
        mail.category = "Trash";
        String token = AuthPrefs.getToken(this);
        String userId = AuthPrefs.getUserId(this);
        mailViewModel.updateMailOnServerWithRoom(token, userId, mail);
        mailViewModel.fetchMailsFromServer(token, userId);
    }

    private void markMailAsSpam(MailItem mail) {
        mail.isSpam = true;
        mail.category = "Spam";
        String token = AuthPrefs.getToken(this);
        String userId = AuthPrefs.getUserId(this);
        mailViewModel.updateMailOnServerWithRoom(token, userId, mail);
        mailViewModel.fetchMailsFromServer(token, userId);
    }
}
