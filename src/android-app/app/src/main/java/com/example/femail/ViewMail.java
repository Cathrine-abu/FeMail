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

public class ViewMail extends AppCompatActivity {

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

        subjectView.setText(subject);
        timeView.setText(time);
        bodyView.setText(body);
        fromView.setText("From: " + from);

        final boolean[] isStarred = { getIntent().getBooleanExtra("starred", false) };

        starView.setImageResource(isStarred[0] ? R.drawable.ic_star_filled : R.drawable.ic_star_border);

        starView.setOnClickListener(v -> {
            isStarred[0] = !isStarred[0];
            starView.setImageResource(isStarred[0] ? R.drawable.ic_star_filled : R.drawable.ic_star_border);
        });
        backButton.setOnClickListener(v -> finish());


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

            popupMenu.setOnMenuItemClickListener(item -> {
                int id = item.getItemId();
                if (id == R.id.action_move) {
                    // TODO: implement move logic
                    Toast.makeText(this, "Move selected", Toast.LENGTH_SHORT).show();
                    return true;
                } else if (id == R.id.action_spam) {
                    // TODO: implement spam logic
                    Toast.makeText(this, "Marked as spam", Toast.LENGTH_SHORT).show();
                    return true;
                } else if (id == R.id.action_label) {
                    // TODO: implement label logic
                    Toast.makeText(this, "Label added", Toast.LENGTH_SHORT).show();
                    return true;
                }
                return false;
            });

            popupMenu.show();
        });


    }
}
