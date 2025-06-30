package com.example.femail;

import android.os.Bundle;
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
import androidx.lifecycle.ViewModelProvider;

import com.example.femail.labels.LabelItem;
import com.example.femail.labels.LabelViewModel;
import com.example.femail.MailViewModel;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton;
import com.google.android.material.navigation.NavigationView;

public class MailActivity extends AppCompatActivity {
    private EditText searchInput;
    private ImageView profilePic, clearSearch, hamburgerMenu, createLabel;
    private DrawerLayout drawerLayout;
    private ExtendedFloatingActionButton composeBtn;
    private LabelViewModel labelViewModel;
    private MailViewModel mailViewModel;
    private NavigationView navigationView;
    private final int LABEL_GROUP_ID = R.id.group_labels;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mail);


        searchInput = findViewById(R.id.searchInput);
        profilePic = findViewById(R.id.profilePic);
        clearSearch = findViewById(R.id.clearSearch);
        hamburgerMenu = findViewById(R.id.hamburgerMenu);

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
            if (id == R.id.create_label) {
                showCreateOrEditLabelPopup(null);
                return true;
            }
            return true;
        });
    }
    
    private void showProfilePopup() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View view = getLayoutInflater().inflate(R.layout.dialog_profile, null);

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

        // You can load profileImage using Glide/Picasso if it's from URL

        usernameText.setText(username + "@femail.com");
        String genderIcon = gender.equals("female") ? "♀" : gender.equals("male") ? "♂" : "";
        fullNameGender.setText("Hi, " + fullName + "! " + genderIcon);
        birthdayText.setText("Birthday: " + birthday);
        phoneText.setText("Phone number: " + phone);

        logoutButton.setOnClickListener(v -> {
            // Clear token or session
            Toast.makeText(this, "Logged out", Toast.LENGTH_SHORT).show();
            // Optionally finish() or navigate
        });

        builder.setView(view);
        builder.setCancelable(true);
        builder.show();
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
            labelTitle.setText("Edit Label");
            btnCreate.setText("Save");
        } else {
            labelTitle.setText("New Label");
            btnCreate.setText("Create");
        }

        // Cancel button closes dialog
        btnCancel.setOnClickListener(v -> dialog.dismiss());

        // Create/Save button logic
        btnCreate.setOnClickListener(v -> {
            String labelName = labelInput.getText().toString().trim();
            if (labelName.isEmpty()) {
                labelError.setText("Label name cannot be empty");
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
                labelError.setText("Label name already exists");
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

}
