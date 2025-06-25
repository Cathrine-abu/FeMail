package com.example.femail;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.List;

public class MailAdapter extends ArrayAdapter<MailItem> {
    private Context context;
    private List<MailItem> mailItems;

    public MailAdapter(Context context, List<MailItem> mailItems) {
        super(context, 0, mailItems);
        this.context = context;
        this.mailItems = mailItems;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        MailItem mail = mailItems.get(position);

        if (convertView == null) {
            convertView = LayoutInflater.from(context).inflate(R.layout.item_mail, parent, false);
        }

        TextView subjectView = convertView.findViewById(R.id.mail_subject);
        TextView timeView = convertView.findViewById(R.id.mail_date);
        ImageView starView = convertView.findViewById(R.id.mail_star);

        subjectView.setText(mail.subject);
        timeView.setText(mail.time);
        starView.setImageResource(mail.isStarred ? R.drawable.ic_star_filled : R.drawable.ic_star_border);

        starView.setOnClickListener(v -> {
            mail.isStarred = !mail.isStarred;
            notifyDataSetChanged();
        });

        return convertView;
    }
}
