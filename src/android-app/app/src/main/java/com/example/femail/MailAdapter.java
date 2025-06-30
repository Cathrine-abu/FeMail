package com.example.femail;

import android.content.Context;
import android.content.Intent;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

public class MailAdapter extends RecyclerView.Adapter<MailAdapter.MailViewHolder> {

    private List<MailItem> mailList;
    private Context context;
    private OnMailClickListener mailClickListener;

    public MailAdapter(Context context, List<MailItem> mailList) {
        this(context, mailList, null);
    }

    public MailAdapter(Context context, List<MailItem> mailList, OnMailClickListener listener) {
        this.context = context;
        this.mailList = mailList;
        this.mailClickListener = listener;
    }

    public void setMailList(List<MailItem> mails) {
        this.mailList = mails;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public MailViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_mail, parent, false);
        return new MailViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MailViewHolder holder, int position) {
        MailItem mail = mailList.get(position);
        holder.subjectView.setText(mail.subject);
        holder.timeView.setText(mail.time);
        holder.starView.setImageResource(mail.isStarred ? R.drawable.ic_star_filled : R.drawable.ic_star_border);
        holder.fromView.setText(mail.from);
        holder.bodyView.setText(mail.body);

        holder.checkView.setVisibility(mail.isSelected ? View.VISIBLE : View.GONE);
        holder.profileView.setVisibility(mail.isSelected ? View.GONE : View.VISIBLE);

        holder.profileContainer.setOnClickListener(v -> {
            mail.isSelected = !mail.isSelected;
            notifyItemChanged(position);
        });

        holder.starView.setOnClickListener(v -> {
            mail.isStarred = !mail.isStarred;
            notifyItemChanged(position);
        });

        holder.itemView.setOnClickListener(v -> {
            if (!mail.isSelected) {
                if (mailClickListener != null) {
                    mailClickListener.onMailClick(mail);
                } else {
                    Intent intent = new Intent(context, ViewMail.class);
                    intent.putExtra("mail_subject", mail.subject);
                    intent.putExtra("mail_body", mail.body);
                    intent.putExtra("mail_time", mail.time);
                    intent.putExtra("mail_from", mail.from);
                    context.startActivity(intent);
                }
            }
        });
    }

    @Override
    public int getItemCount() {
        return mailList == null ? 0 : mailList.size();
    }

    public interface OnMailClickListener {
        void onMailClick(MailItem mail);
    }

    public static class MailViewHolder extends RecyclerView.ViewHolder {
        TextView subjectView, timeView, fromView, bodyView;
        ImageView starView, profileView, checkView;
        View profileContainer;

        public MailViewHolder(@NonNull View itemView) {
            super(itemView);
            subjectView = itemView.findViewById(R.id.item_mail_subject);
            timeView = itemView.findViewById(R.id.item_mail_time);
            starView = itemView.findViewById(R.id.item_mail_star);
            profileView = itemView.findViewById(R.id.profile_circle);
            checkView = itemView.findViewById(R.id.check_icon);
            profileContainer = itemView.findViewById(R.id.profile_container);
            fromView = itemView.findViewById(R.id.item_mail_from);
            bodyView = itemView.findViewById(R.id.item_mail_body);
        }
    }
}

