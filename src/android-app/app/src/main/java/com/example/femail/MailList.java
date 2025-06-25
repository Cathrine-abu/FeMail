package com.example.femail;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ListView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.femail.MailItem;
import com.example.femail.R;

import java.util.ArrayList;
import java.util.List;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

public class MailList extends AppCompatActivity {

    private ListView listView;
    private List<MailItem> mailItems = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mail_list);

        listView = findViewById(R.id.mailListView);

        String category = getIntent().getStringExtra("category");
        if (category == null) category = "inbox";

        loadMails(category);

        MailAdapter adapter = new MailAdapter(this, mailItems);
        listView.setAdapter(adapter);

        listView.setOnItemClickListener((parent, view, position, id) -> {
            MailItem clickedMail = mailItems.get(position);

            Intent intent = new Intent(MailList.this, ViewMail.class);
            intent.putExtra("subject", clickedMail.getSubject());
            intent.putExtra("time", clickedMail.getTime());
            startActivity(intent);
        });
    }

    private String formatMailTime(Date mailDate) {
        Date now = new Date();

        long diffInMillis = now.getTime() - mailDate.getTime();
        long diffInDays = TimeUnit.MILLISECONDS.toDays(diffInMillis);

        if (diffInDays == 0) {
            return new SimpleDateFormat("HH:mm", Locale.getDefault()).format(mailDate);
        } else {
            return new SimpleDateFormat("dd MMM", Locale.getDefault()).format(mailDate);
        }
    }
    private void loadMails(String category) {

        Date now = new Date();
        String formattedTime = formatMailTime(now);

        if ("inbox".equals(category)) {
            mailItems.add(new MailItem("Welcome to FeMail!", formattedTime, false));
            mailItems.add(new MailItem("Project reminder", formattedTime, true));
        } else if ("spam".equals(category)) {
            mailItems.add(new MailItem("Win iPhone now!", formattedTime, false));
        } else if ("sent".equals(category)) {
            mailItems.add(new MailItem("Your email has been sent", formattedTime, true));
        }
    }


}
