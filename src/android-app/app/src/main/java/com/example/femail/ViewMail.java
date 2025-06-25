package com.example.femail;

import android.os.Bundle;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;

public class ViewMail extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_mail);

        String subject = getIntent().getStringExtra("subject");
        String time = getIntent().getStringExtra("time");

        TextView subjectView = findViewById(R.id.view_mail_subject);
        TextView timeView = findViewById(R.id.view_mail_time);

        subjectView.setText(subject);
        timeView.setText(time);
    }
}
