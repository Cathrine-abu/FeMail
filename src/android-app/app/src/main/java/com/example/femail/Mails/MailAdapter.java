package com.example.femail.Mails;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.femail.R;
import com.example.femail.ViewMail;

import java.util.List;

public class MailAdapter extends RecyclerView.Adapter<MailAdapter.MailViewHolder> {

    private List<MailItem> mailList;
    private Context context;
    private OnMailClickListener mailClickListener;
    private OnStarClickListener starClickListener;
    private boolean selectionMode = false;
    private SelectionListener selectionListener;

    public interface SelectionListener {
        void onSelectionModeChanged(boolean enabled, int selectedCount);
    }
    public void setSelectionListener(SelectionListener listener) {
        this.selectionListener = listener;
    }
    public void clearSelection() {
        for (MailItem mail : mailList) mail.isSelected = false;
        selectionMode = false;
        notifyDataSetChanged();
        if (selectionListener != null) selectionListener.onSelectionModeChanged(false, 0);
    }
    public void selectAll() {
        for (MailItem mail : mailList) mail.isSelected = true;
        selectionMode = true;
        notifyDataSetChanged();
        if (selectionListener != null) selectionListener.onSelectionModeChanged(true, mailList.size());
    }
    public List<MailItem> getSelectedMails() {
        List<MailItem> selected = new java.util.ArrayList<>();
        for (MailItem mail : mailList) if (mail.isSelected) selected.add(mail);
        return selected;
    }

    public MailAdapter(Context context, List<MailItem> mailList) {
        this(context, mailList, null);
    }

    public MailAdapter(Context context, List<MailItem> mailList, OnMailClickListener listener) {
        this(context, mailList, listener, null);
    }

    public MailAdapter(Context context, List<MailItem> mailList, OnMailClickListener mailListener, OnStarClickListener starListener) {
        this.context = context;
        this.mailList = mailList;
        this.mailClickListener = mailListener;
        this.starClickListener = starListener;
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

        View.OnClickListener selectListener = v -> {
            mail.isSelected = !mail.isSelected;
            Toast.makeText(context, "Clicked: " + mail.subject, Toast.LENGTH_SHORT).show(); // Debug
            if (mail.isSelected) selectionMode = true;
            else {
                boolean anySelected = false;
                for (MailItem m : mailList) if (m.isSelected) anySelected = true;
                selectionMode = anySelected;
            }
            Toast.makeText(context, "Adapter: Notifying selectionListener: " + selectionMode + ", " + getSelectedMails().size(), Toast.LENGTH_SHORT).show();
            if (selectionListener != null)
                selectionListener.onSelectionModeChanged(selectionMode, getSelectedMails().size());
            notifyItemChanged(position);
        };
        holder.profileContainer.setOnClickListener(selectListener);
        holder.profileView.setOnClickListener(selectListener);

        holder.starView.setOnClickListener(v -> {
            mail.isStarred = !mail.isStarred;
            notifyItemChanged(position);
            if (starClickListener != null) {
                starClickListener.onStarClick(mail, position);
            }
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
                    // Add the "To" information
                    String toAddress = "";
                    if (mail.to != null && !mail.to.isEmpty()) {
                        toAddress = mail.to.get(0); // Get the first recipient
                    }
                    intent.putExtra("mail_to", toAddress);
                    intent.putExtra("starred", mail.isStarred);
                    intent.putExtra("mail_id", mail.id);
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

    public interface OnStarClickListener {
        void onStarClick(MailItem mail, int position);
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

