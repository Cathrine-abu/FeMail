package com.example.femail;

import android.os.Bundle;
import android.util.Log;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.room.Room;

import com.example.femail.Mails.MailAdapter;
import com.example.femail.Mails.MailDao;
import com.example.femail.Mails.MailDatabase;
import com.example.femail.Mails.MailItem;
import com.example.femail.AuthPrefs;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

public class MailList extends AppCompatActivity {

    private RecyclerView listView;
    private List<MailItem> mailItems;
    private MailAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.layout_mail_list);

        listView = findViewById(R.id.mailListView);
        listView.setLayoutManager(new LinearLayoutManager(this));

        String category = getIntent().getStringExtra("category");
        if (category == null) category = "inbox";

        loadMails(category);

        adapter = new MailAdapter(this, mailItems, "list");
        listView.setAdapter(adapter);

        Log.d("DEBUG_LIST", "Loaded mails: " + mailItems.size());
    }

    private void loadMails(String category) {
        MailDatabase db = Room.databaseBuilder(getApplicationContext(),
                        MailDatabase.class, "mail-db")
                .allowMainThreadQueries()
                .build();

        MailDao mailDao = db.mailDao();
//        if (mailDao.getInboxMails().isEmpty()) {
//            mailDao.insertMail(new MailItem("wow", "4444", new Date(), "inbox", "testUserId"));
//            mailDao.insertMail(new MailItem("wow", "hhhh", new Date(), "inbox", "testUserId"));
//            mailDao.insertMail(new MailItem("wow", "88888", new Date(), "inbox", "testUserId"));
//            mailDao.insertMail(new MailItem("wow", "hh444478hh", new Date(), "inbox", "testUserId"));
//
//        }
        switch (category) {
            case "inbox":
                mailItems = mailDao.getInboxMails(AuthPrefs.getUserId(this));
                break;
            case "spam":
                mailItems = mailDao.getSpamMails(AuthPrefs.getUserId(this));
                break;
            case "sent":
                mailItems = mailDao.getSentMails(AuthPrefs.getUserId(this));
                break;
            default:
                mailItems = mailDao.getInboxMails(AuthPrefs.getUserId(this));
        }
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
}