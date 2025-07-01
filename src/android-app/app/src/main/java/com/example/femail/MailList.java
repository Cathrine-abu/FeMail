package com.example.femail;

import android.os.Bundle;
import android.util.Log;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.room.Room;

import com.example.femail.Mails.MailAdapter;
import com.example.femail.Mails.MailDao;
import com.example.femail.Mails.MailDatabase;
import com.example.femail.Mails.MailItem;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

public class MailList extends AppCompatActivity {

    private RecyclerView listView;
    private List<MailItem> mailItems;
    private MailAdapter adapter;
    private Toolbar actionBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.layout_mail_list);

        listView = findViewById(R.id.mailListView);
        listView.setLayoutManager(new LinearLayoutManager(this));

        String category = getIntent().getStringExtra("category");
        if (category == null) category = "inbox";

        loadMails(category);

        adapter = new MailAdapter(this, mailItems);
        adapter.setSelectionListener((enabled, count) -> {
            android.util.Log.d("DEBUG_BAR", "SelectionListener callback: enabled=" + enabled + ", count=" + count);
            android.widget.Toast.makeText(this, "Selection mode: " + enabled + ", count: " + count, android.widget.Toast.LENGTH_SHORT).show();
            if (enabled) {
                actionBar.setVisibility(View.VISIBLE);
                actionBar.setTitle(count + " selected");
            } else {
                actionBar.setVisibility(View.GONE);
            }
        });
        android.util.Log.d("DEBUG_BAR", "SelectionListener set: " + (adapter != null));
        listView.setAdapter(adapter);

        actionBar = findViewById(R.id.action_bar);
        actionBar.setVisibility(View.GONE);
        actionBar.inflateMenu(R.menu.mail_action_menu);
        actionBar.setOnMenuItemClickListener(item -> {
            if (item.getItemId() == R.id.action_back) {
                adapter.clearSelection();
                return true;
            } else if (item.getItemId() == R.id.action_select_all) {
                adapter.selectAll();
                return true;
            } else if (item.getItemId() == R.id.action_trash) {
                for (MailItem mail : adapter.getSelectedMails()) {
                    // TODO: Delete via ViewModel or DAO
                }
                adapter.clearSelection();
                return true;
            } else if (item.getItemId() == R.id.action_more) {
                android.widget.Toast.makeText(this, "More options coming soon", android.widget.Toast.LENGTH_SHORT).show();
                return true;
            }
            return false;
        });
        Log.d("DEBUG_LIST", "Loaded mails: " + mailItems.size());
    }

    private void loadMails(String category) {
        MailDatabase db = Room.databaseBuilder(getApplicationContext(),
                        MailDatabase.class, "mail-db")
                .allowMainThreadQueries()
                .build();

        MailDao mailDao = db.mailDao();
        if (mailDao.getInboxMails().isEmpty()) {
            mailDao.insertMail(new MailItem("wow", "4444", new Date(), "inbox"));
            mailDao.insertMail(new MailItem("wow", "hhhh", new Date(), "inbox"));
            mailDao.insertMail(new MailItem("wow", "88888", new Date(), "inbox"));
            mailDao.insertMail(new MailItem("wow", "hh444478hh", new Date(), "inbox"));

        }
        switch (category) {
            case "inbox":
                mailItems = mailDao.getInboxMails();
                break;
            case "spam":
                mailItems = mailDao.getSpamMails();
                break;
            case "sent":
                mailItems = mailDao.getSentMails();
                break;
            default:
                mailItems = mailDao.getInboxMails();
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
