package com.example.femail;

import android.content.Context;
import android.content.Intent;
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

        TextView subjectView = convertView.findViewById(R.id.item_mail_subject);
        TextView timeView = convertView.findViewById(R.id.item_mail_time);
        ImageView starView = convertView.findViewById(R.id.item_mail_star);

        ImageView profileView = convertView.findViewById(R.id.profile_circle);
        ImageView checkView = convertView.findViewById(R.id.check_icon);
        View profileContainer = convertView.findViewById(R.id.profile_container);

        subjectView.setText(mail.subject);
        timeView.setText(mail.time);
        starView.setImageResource(mail.isStarred ? R.drawable.ic_star_filled : R.drawable.ic_star_border);

        if (mail.isSelected) {
            checkView.setVisibility(View.VISIBLE);
            profileView.setVisibility(View.GONE);
        } else {
            checkView.setVisibility(View.GONE);
            profileView.setVisibility(View.VISIBLE);
        }

        profileContainer.setOnClickListener(v -> {
            mail.isSelected = !mail.isSelected;
            notifyDataSetChanged();
        });

        starView.setOnClickListener(v -> {
            mail.isStarred = !mail.isStarred;
            notifyDataSetChanged();
        });

        convertView.setOnClickListener(v -> {
            if (!mail.isSelected) {
                Intent intent = new Intent(context, ViewMail.class);
                intent.putExtra("mail_subject", mail.subject);
                intent.putExtra("mail_body", mail.body);
                intent.putExtra("mail_time", mail.time);
                intent.putExtra("from", mail.sender);
                context.startActivity(intent);
            }
        });

        return convertView;
    }


}
