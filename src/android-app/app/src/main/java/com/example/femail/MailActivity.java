package com.example.femail;

import android.annotation.SuppressLint;
import android.content.res.Resources;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.*;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.lifecycle.ViewModelProvider;

import com.example.femail.labels.LabelItem;
import com.example.femail.labels.LabelViewModel;
import com.example.femail.Mails.MailItem;
import com.example.femail.Mails.MailViewModel;
import com.example.femail.MailFragments.InboxFragment;
import com.example.femail.MailFragments.PrimaryFragment;
import com.example.femail.MailFragments.SocialFragment;
import com.example.femail.MailFragments.PromotionsFragment;
import com.example.femail.MailFragments.UpdatesFragment;
import com.example.femail.MailFragments.SendFragment;
import com.example.femail.MailFragments.DraftsFragment;
import com.example.femail.MailFragments.SpamFragment;
import com.example.femail.MailFragments.StarredFragment;
import com.example.femail.MailFragments.TrashFragment;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.example.femail.labels.LabelItem;
import com.example.femail.labels.LabelViewModel;
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton;
import com.google.android.material.navigation.NavigationView;

public class MailActivity extends AppCompatActivity {
    private EditText searchInput;
    private ImageView profilePic, clearSearch, hamburgerMenu;
    private DrawerLayout drawerLayout;
    private ExtendedFloatingActionButton composeBtn;
    private LabelViewModel labelViewModel;
    private MailViewModel mailViewModel;
    private NavigationView navigationView;
    private final int LABEL_GROUP_ID = R.id.group_labels;

    @SuppressLint("NonConstantResourceId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mail);


        searchInput = findViewById(R.id.searchInput);
        profilePic = findViewById(R.id.profilePic);
        clearSearch = findViewById(R.id.clearSearch);
        composeBtn = findViewById(R.id.composeBtn);
        hamburgerMenu = findViewById(R.id.hamburgerMenu);
        drawerLayout = findViewById(R.id.drawerLayout);
        navigationView = findViewById(R.id.navigation_view);

        clearSearch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                searchInput.setText("");
            }
        });

        searchInput.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                if (keyCode == KeyEvent.KEYCODE_ENTER && event.getAction() == KeyEvent.ACTION_DOWN) {
                    String searchText = searchInput.getText().toString().trim();
                    if (!searchText.isEmpty()) {
                        Toast.makeText(MailActivity.this, "Searching for: " + searchText, Toast.LENGTH_SHORT).show();
                    }
                    return true;
                }
                return false;
            }
        });

        profilePic.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showProfilePopup();
            }
        });

        composeBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showCreateOrEditMailPopup(null);
            }
        });

        hamburgerMenu.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                drawerLayout.openDrawer(GravityCompat.START);
            }
        });


        // MVVM: Observe labels and inject into menu
        labelViewModel = new ViewModelProvider(this).get(LabelViewModel.class);
        mailViewModel = new ViewModelProvider(this).get(MailViewModel.class);
        labelViewModel.getAllLabels().observe(this, labels -> {
            Menu menu = navigationView.getMenu();
            menu.removeGroup(LABEL_GROUP_ID);

            for (LabelItem label : labels) {
                MenuItem menuItem = menu.add(LABEL_GROUP_ID, Menu.NONE, Menu.NONE, label.getName())
                        .setIcon(R.drawable.ic_double_arrow)
                        .setCheckable(true);
                menuItem.setActionView(R.layout.menu_item_action_icon);

                // Handle clicks on the icon (action view)
                View actionView = menuItem.getActionView();
                actionView.setTag(label);

                ImageView icon = actionView.findViewById(R.id.right_icon);
                icon.setOnClickListener(v -> {
                    showLabelOptionPopup(v);
                });

                // Handle clicks on the menu item text itself
                menuItem.setOnMenuItemClickListener(item -> {
                    return true;
                });
            }
        });

        // Handle label clicks
        navigationView.setNavigationItemSelectedListener(item -> {
            int id = item.getItemId();

            // Handle mail navigation
            if (id == R.id.nav_inbox) {
                navigateToFragment(new InboxFragment());
                return true;
            } else if (id == R.id.nav_primary) {
                navigateToFragment(new PrimaryFragment());
                return true;
            } else if (id == R.id.nav_social) {
                navigateToFragment(new SocialFragment());
                return true;
            } else if (id == R.id.nav_promotions) {
                navigateToFragment(new PromotionsFragment());
                return true;
            } else if (id == R.id.nav_updates) {
                navigateToFragment(new UpdatesFragment());
                return true;
            } else if (id == R.id.nav_starred) {
                navigateToFragment(new StarredFragment());
                return true;
            } else if (id == R.id.nav_sent) {
                navigateToFragment(new SendFragment());
                return true;
            } else if (id == R.id.nav_drafts) {
                navigateToFragment(new DraftsFragment());
                return true;
            } else if (id == R.id.nav_spam) {
                navigateToFragment(new SpamFragment());
                return true;
            } else if (id == R.id.nav_trash) {
                navigateToFragment(new TrashFragment());
                return true;
            } else if (id == R.id.create_label) {
                showCreateOrEditLabelPopup(null);
                return true;
            }
            
            // Handle label clicks (if any labels are clicked)
            String labelName = item.getTitle().toString();
            drawerLayout.closeDrawers();
            return true;
        });

        // Set initial fragment (Inbox by default)
        if (savedInstanceState == null) {
            navigateToFragment(new InboxFragment());
        }
    }

    @SuppressLint("SetTextI18n")
    private void showProfilePopup() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View view = getLayoutInflater().inflate(R.layout.dialog_profile, null);

        Resources res = getResources();
        // Example user object (replace with your actual user data)
        String username = "shirafis";
        String fullName = "Shira Fisher";
        String gender = "female";
        String phone = "123456789";
        String birthday = "2000-01-01";

        ImageView profileImage = view.findViewById(R.id.profileImage);
        TextView usernameText = view.findViewById(R.id.usernameText);
        TextView fullNameGender = view.findViewById(R.id.fullNameGender);
        TextView birthdayText = view.findViewById(R.id.birthdayText);
        TextView phoneText = view.findViewById(R.id.phoneText);
        Button logoutButton = view.findViewById(R.id.logoutButton);

        String genderIcon = "";
        if (gender.equals(res.getString(R.string.gender_female))) {
            genderIcon = res.getString(R.string.female_icon);
        } else if (gender.equals(res.getString(R.string.gender_male))) {
            genderIcon = res.getString(R.string.male_icon);
        }
        usernameText.setText(res.getString(R.string.profile_end_mail, username));
        fullNameGender.setText(res.getString(R.string.profile_hi, fullName, genderIcon));
        birthdayText.setText(getString(R.string.profile_birthday, birthday));
        phoneText.setText(res.getString(R.string.profile_phone, phone));

        logoutButton.setOnClickListener(v -> {
            Toast.makeText(this, res.getString(R.string.profile_log_out), Toast.LENGTH_SHORT).show();
        });

        logoutButton.setOnClickListener(v -> {
            // Clear token or session
            Toast.makeText(this, R.string.profile_log_out, Toast.LENGTH_SHORT).show();
            // Optionally finish() or navigate
        });

        builder.setView(view);
        builder.setCancelable(true);
        builder.show();
    }
    private void showCreateOrEditMailPopup(@Nullable MailItem existingMail) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View view = getLayoutInflater().inflate(R.layout.dialog_create_edit_mail, null);
        builder.setView(view);
        builder.setCancelable(true);

        AlertDialog dialog = builder.create();

        EditText inputTo = view.findViewById(R.id.inputTo);
        EditText inputSubject = view.findViewById(R.id.inputSubject);
        EditText inputBody = view.findViewById(R.id.inputBody);
        TextView mailTitle = view.findViewById(R.id.mailTitle);
        TextView mailError = view.findViewById(R.id.mailError);
        Button btnDraft = view.findViewById(R.id.btnDraft);
        Button btnCreate = view.findViewById(R.id.btnSend);
       // android.widget.Spinner categorySpinner = view.findViewById(R.id.categorySpinner);

        // Set up category spinner
        String[] categories = {"primary", "social", "promotions", "updates"};
        android.widget.ArrayAdapter<String> spinnerAdapter = new android.widget.ArrayAdapter<>(this, android.R.layout.simple_spinner_item, categories);
        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        //categorySpinner.setAdapter(spinnerAdapter);

        // Set up for edit/new mode
        if (existingMail != null) {
            mailTitle.setText(R.string.edit_message);
            inputTo.setText(existingMail.to != null && !existingMail.to.isEmpty() ? existingMail.to.get(0) : "");
            inputSubject.setText(existingMail.subject);
            inputBody.setText(existingMail.body);
        } else {
            mailTitle.setText(R.string.new_message);
        }
        // Remove all categorySpinner references and duplicate category declarations
        String category = "primary";

        // Save as draft button
        btnDraft.setOnClickListener(v -> {
            String mailTo = inputTo.getText().toString().trim();
            String mailSubject = inputSubject.getText().toString().trim();
            String mailBody = inputBody.getText().toString().trim();
            if (mailTo.isEmpty() || mailSubject.isEmpty() || mailBody.isEmpty()) {
                mailError.setText(R.string.mandatory_fields);
                return;
            }

            if (existingMail != null) {
                // Update existing mail as draft
                String currentTime = new java.text.SimpleDateFormat("HH:mm", java.util.Locale.getDefault()).format(new java.util.Date());
                existingMail.to = mailTo.isEmpty() ? null : java.util.List.of(mailTo);
                existingMail.subject = mailSubject;
                existingMail.body = mailBody;
                existingMail.time = currentTime;
                existingMail.isDraft = true;
                existingMail.direction = java.util.List.of("draft");
                existingMail.category = category;
                mailViewModel.update(existingMail);
            } else {
                // Create new draft mail
                String currentTime = new java.text.SimpleDateFormat("HH:mm", java.util.Locale.getDefault()).format(new java.util.Date());
                MailItem newMail = new MailItem(
                    String.valueOf(System.currentTimeMillis()),
                    mailSubject,
                    mailBody,
                    "me", // from
                    mailTo.isEmpty() ? null : java.util.List.of(mailTo),
                    currentTime,
                    false, false, false, true, false, // isStarred, isRead, isSpam, isDraft, isDeleted
                    java.util.List.of("draft"),
                    "current_user", "current_user", null, category, false
                );
                mailViewModel.insert(newMail);
            }
            Toast.makeText(this, "Saved as draft", Toast.LENGTH_SHORT).show();
            dialog.dismiss();
        });

        // Send button - save as sent mail
        btnCreate.setOnClickListener(v -> {
            String mailTo = inputTo.getText().toString().trim();
            String mailSubject = inputSubject.getText().toString().trim();
            String mailBody = inputBody.getText().toString().trim();

            if (mailTo.isEmpty() || mailSubject.isEmpty() || mailBody.isEmpty()) {
                mailError.setText(R.string.mandatory_fields);
                return;
            }

            if (existingMail != null) {
                // Update existing mail as sent
                String currentTime = new java.text.SimpleDateFormat("HH:mm", java.util.Locale.getDefault()).format(new java.util.Date());
                existingMail.to = java.util.List.of(mailTo);
                existingMail.subject = mailSubject;
                existingMail.body = mailBody;
                existingMail.time = currentTime;
                existingMail.isDraft = false;
                existingMail.direction = java.util.List.of("sent");
                existingMail.category = category;
                mailViewModel.update(existingMail);
            } else {
                // Create new sent mail
                String currentTime = new java.text.SimpleDateFormat("HH:mm", java.util.Locale.getDefault()).format(new java.util.Date());
                MailItem newMail = new MailItem(
                    String.valueOf(System.currentTimeMillis()),
                    mailSubject,
                    mailBody,
                    "me", // from
                    java.util.List.of(mailTo),
                    currentTime,
                    false, true, false, false, false, // isStarred, isRead, isSpam, isDraft, isDeleted
                    java.util.List.of("sent"),
                    "current_user", "current_user", null, category, false
                );
                mailViewModel.insert(newMail);
            }
            Toast.makeText(this, "Mail sent", Toast.LENGTH_SHORT).show();
            dialog.dismiss();
        });
        dialog.show();
    }
    private void showCreateOrEditLabelPopup(@Nullable LabelItem existingLabel) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View view = getLayoutInflater().inflate(R.layout.dialog_create_edit_label, null);
        builder.setView(view);
        builder.setCancelable(true);

        AlertDialog dialog = builder.create();

        EditText labelInput = view.findViewById(R.id.labelInput);
        TextView labelError = view.findViewById(R.id.labelError);
        TextView labelTitle = view.findViewById(R.id.labelTitle);
        Button btnCancel = view.findViewById(R.id.btn_cancel);
        Button btnCreate = view.findViewById(R.id.btn_create);

        // Set up for edit mode
        if (existingLabel != null) {
            labelInput.setText(existingLabel.getName());
            labelTitle.setText(R.string.edit_label);
            btnCreate.setText(R.string.save);
        } else {
            labelTitle.setText(R.string.new_label);
            btnCreate.setText(R.string.create);
        }

        // Cancel button closes dialog
        btnCancel.setOnClickListener(v -> dialog.dismiss());

        // Create/Save button logic
        btnCreate.setOnClickListener(v -> {
            String labelName = labelInput.getText().toString().trim();
            if (labelName.isEmpty()) {
                labelError.setText(R.string.empty_label);
                return;
            }

            // Check for duplicates
            boolean isDuplicate = false;
            for (LabelItem item : labelViewModel.getAllLabels().getValue()) {
                // If it's an existing label, allow the same name only if it's the same item
                if (item.getName().equalsIgnoreCase(labelName)) {
                    if (existingLabel == null || item.getId() != existingLabel.getId()) {
                        isDuplicate = true;
                        break;
                    }
                }
            }

            if (isDuplicate) {
                labelError.setText(R.string.duplicate_label);
                return;
            }
            if (existingLabel != null) {
                // Update the existing label
                existingLabel.setName(labelName);
                labelViewModel.update(existingLabel);
            } else {
                // Create new label
                LabelItem newLabel = new LabelItem(labelName);
                labelViewModel.insert(newLabel);
            }
            dialog.dismiss();
        });

        dialog.show();
    }
    private void showLabelOptionPopup(View anchorView) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View view = getLayoutInflater().inflate(R.layout.dialog_label_options, null);
        builder.setView(view);
        builder.setCancelable(true);

        AlertDialog dialog = builder.create();

        // Get the label associated with this icon
        View actionView = (View) anchorView.getParent();
        LabelItem label = (LabelItem) actionView.getTag();

        TextView editLabel = view.findViewById(R.id.editLabelTitle);
        TextView deleteLabel = view.findViewById(R.id.deleteLabelTitle);

        editLabel.setOnClickListener(v -> {
            dialog.dismiss();
            showCreateOrEditLabelPopup(label);
        });

        deleteLabel.setOnClickListener(v -> {
            dialog.dismiss();
            showRemoveLabelPopup(label);
        });

        dialog.show();
    }
    private void showRemoveLabelPopup(LabelItem label) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View view = getLayoutInflater().inflate(R.layout.dialog_remove_label, null);
        builder.setView(view);
        builder.setCancelable(true);

        AlertDialog dialog = builder.create();

        TextView labelSubtitle = view.findViewById(R.id.labelSubtitle);
        Button btnCancel = view.findViewById(R.id.btn_cancel);
        Button btnDelete = view.findViewById(R.id.btn_create);  // This is your delete button

        labelSubtitle.setText(getString(R.string.delete_the_label, label.getName()));

        btnCancel.setOnClickListener(v -> dialog.dismiss());

        btnDelete.setOnClickListener(v -> {
            // Delete the label
            labelViewModel.delete(label);
            dialog.dismiss();
        });

        dialog.show();
    }

    /**
     * Navigate to a specific fragment
     */
    private void navigateToFragment(Fragment fragment) {
        FragmentManager fragmentManager = getSupportFragmentManager();
        fragmentManager.beginTransaction()
                .replace(R.id.fragmentContainer, fragment)
                .addToBackStack(null)
                .commit();
        drawerLayout.closeDrawers();
    }

}
