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

import com.example.femail.Mails.MailItem;
import com.example.femail.Mails.MailViewModel;
import com.example.femail.AuthPrefs;

import java.util.ArrayList;

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
            "current_user",
            "current_user",
            null,
            "inbox",
            false,
            AuthPrefs.getUserId(this) // userId
        );
        TextView detailFrom = findViewById(R.id.detail_from);
        TextView detailTo = findViewById(R.id.detail_to);
        TextView detailDate = findViewById(R.id.detail_date);

        detailFrom.setText("From: " + from);
        detailTo.setText("To: " + to);
        detailDate.setText("Date: " + time);

        TextView subjectView = findViewById(R.id.view_mail_subject);
        TextView timeView = findViewById(R.id.view_mail_time);
        TextView bodyView = findViewById(R.id.view_mail_body);
        TextView fromView = findViewById(R.id.view_mail_from);

        ImageView starView = findViewById(R.id.view_mail_star);
        ImageView backButton = findViewById(R.id.back_button);
        ImageView trashButton = findViewById(R.id.trash_button);

        subjectView.setText(subject);
        timeView.setText(time);
        bodyView.setText(body);
        fromView.setText("From: " + from);

        final boolean[] isStarred = { getIntent().getBooleanExtra("starred", false) };

        starView.setImageResource(isStarred[0] ? R.drawable.ic_star_filled : R.drawable.ic_star_border);

        starView.setOnClickListener(v -> {
            isStarred[0] = !isStarred[0];
            starView.setImageResource(isStarred[0] ? R.drawable.ic_star_filled : R.drawable.ic_star_border);
            // Update the mail in database
            currentMail.isStarred = isStarred[0];
            mailViewModel.update(currentMail);
        });
        backButton.setOnClickListener(v -> finish());
        trashButton.setOnClickListener(v -> {
            if (currentMail.isDeleted || (currentMail.direction != null && currentMail.direction.contains("trash"))) {
                // Permanently delete
                mailViewModel.delete(currentMail);
                Toast.makeText(this, "Mail deleted forever", Toast.LENGTH_SHORT).show();
            } else {
                // Move to trash
                currentMail.isDeleted = true;
                currentMail.direction = java.util.List.of("trash");
                mailViewModel.update(currentMail);
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

            // Hide 'Move' option if not from inbox
            if (sourceFragment == null || !sourceFragment.equals("inbox")) {
                popupMenu.getMenu().findItem(R.id.action_move).setVisible(false);
            }

            // If mail is spam, show 'Unspam' instead of 'Spam'
            if (currentMail.isSpam) {
                popupMenu.getMenu().findItem(R.id.action_spam).setTitle("Unspam");
            }

            // If mail is deleted (in trash), show 'Delete Forever' instead of 'Move'
            if (currentMail.isDeleted) {
                popupMenu.getMenu().findItem(R.id.action_move).setTitle("Delete Forever");
            }

            popupMenu.setOnMenuItemClickListener(item -> {
                int id = item.getItemId();
                if (id == R.id.action_move) {
                    if (currentMail.isDeleted) {
                        // Delete forever
                        mailViewModel.delete(currentMail);
                        Toast.makeText(this, "Mail deleted forever", Toast.LENGTH_SHORT).show();
                        finish();
                    } else {
                        // Move to trash
                        currentMail.isDeleted = true;
                        currentMail.direction = java.util.List.of("trash");
                        mailViewModel.update(currentMail);
                        Toast.makeText(this, "Moved to trash", Toast.LENGTH_SHORT).show();
                        finish();
                    }
                    return true;
                } else if (id == R.id.action_spam) {
                    if (currentMail.isSpam) {
                        // Unspam
                        currentMail.isSpam = false;
                        if (currentMail.previousDirection != null && !currentMail.previousDirection.isEmpty()) {
                            currentMail.direction = currentMail.previousDirection;
                        } else {
                            currentMail.direction = java.util.List.of("inbox");
                        }
                        mailViewModel.update(currentMail);
                        Toast.makeText(this, "Mail moved out of spam", Toast.LENGTH_SHORT).show();
                        finish();
                    } else {
                        // Mark as spam
                        currentMail.isSpam = true;
                        currentMail.previousDirection = currentMail.direction;
                        currentMail.direction = java.util.List.of("spam");
                        mailViewModel.update(currentMail);
                        Toast.makeText(this, "Marked as spam", Toast.LENGTH_SHORT).show();
                        finish();
                    }
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
}
