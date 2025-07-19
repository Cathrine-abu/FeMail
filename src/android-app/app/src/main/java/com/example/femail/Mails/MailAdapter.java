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
import java.util.Collections;
import java.util.Comparator;

public class MailAdapter extends RecyclerView.Adapter<MailAdapter.MailViewHolder> {

    private List<MailItem> mailList;
    private Context context;
    private OnMailClickListener mailClickListener;
    private OnStarClickListener starClickListener;
    private boolean selectionMode = false;
    private SelectionListener selectionListener;
    private String sourceFragment;

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

    public MailAdapter(Context context, List<MailItem> mailList, String sourceFragment) {
        this(context, mailList, sourceFragment, null, null);
    }

    public MailAdapter(Context context, List<MailItem> mailList, String sourceFragment, OnMailClickListener listener) {
        this(context, mailList, sourceFragment, listener, null);
    }

    public MailAdapter(Context context, List<MailItem> mailList, String sourceFragment, OnMailClickListener mailListener, OnStarClickListener starListener) {
        this.context = context;
        this.mailList = mailList;
        this.sourceFragment = sourceFragment;
        this.mailClickListener = mailListener;
        this.starClickListener = starListener;
    }

    public void setMailList(List<MailItem> mails) {
        if (mails != null) {
            android.util.Log.d("MailAdapter", "Setting mail list with " + mails.size() + " mails");
            
            java.util.Map<String, MailItem> mailMap = new java.util.HashMap<>();
            for (MailItem mail : mails) {
                String key = (mail.subject == null ? "" : mail.subject.trim().toLowerCase())
                           + "|" + (mail.body == null ? "" : mail.body.trim().toLowerCase());
                // Prefer real mail over temp mail for the same key
                if (!mail.id.startsWith("temp-") || !mailMap.containsKey(key)) {
                    mailMap.put(key, mail);
                }
            }
            java.util.List<MailItem> filtered = new java.util.ArrayList<>(mailMap.values());
            
            // Log first few mails before sorting
            for (int i = 0; i < Math.min(3, filtered.size()); i++) {
                MailItem mail = filtered.get(i);
                android.util.Log.d("MailAdapter", "Before sorting [" + i + "]: id=" + mail.id + ", time=" + mail.time + ", timestamp=" + mail.timestamp + ", subject=" + mail.subject);
            }
            
            java.util.Collections.sort(filtered, new java.util.Comparator<MailItem>() {
                @Override
                public int compare(MailItem o1, MailItem o2) {
                    String t1 = o1.timestamp != null ? o1.timestamp : o1.time;
                    String t2 = o2.timestamp != null ? o2.timestamp : o2.time;
                    if (t1 == null && t2 == null) return 0;
                    if (t1 == null) return 1;
                    if (t2 == null) return -1;
                    int result = t2.compareTo(t1); // Descending order
                    android.util.Log.d("MailAdapter", "Comparing: " + t1 + " vs " + t2 + " = " + result);
                    return result;
                }
            });
            
            // Log first few mails after sorting
            for (int i = 0; i < Math.min(3, filtered.size()); i++) {
                MailItem mail = filtered.get(i);
                android.util.Log.d("MailAdapter", "After sorting [" + i + "]: id=" + mail.id + ", time=" + mail.time + ", timestamp=" + mail.timestamp + ", subject=" + mail.subject);
            }
            
            this.mailList = filtered;
        } else {
            this.mailList = mails;
        }
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
        // Show only the clock time (HH:mm) regardless of stored format
        try {
            java.text.SimpleDateFormat inputFormat = new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss", java.util.Locale.getDefault());
            java.util.Date date = inputFormat.parse(mail.time);
            java.text.SimpleDateFormat outputFormat = new java.text.SimpleDateFormat("HH:mm", java.util.Locale.getDefault());
            holder.timeView.setText(outputFormat.format(date));
        } catch (Exception e) {
            holder.timeView.setText(mail.time); // fallback
        }
        holder.starView.setImageResource(mail.isStarred ? R.drawable.ic_star_filled : R.drawable.ic_star_border);
        
        // Show spam icon in Sent folder if mail is spam
        ImageView spamView = holder.itemView.findViewById(R.id.item_mail_spam);
        if ("sent".equals(sourceFragment) && mail.isSpam) {
            spamView.setVisibility(View.VISIBLE);
        } else {
            spamView.setVisibility(View.GONE);
        }
        
        // Display sender/recipient information based on mail direction
        if (mail.direction != null && mail.direction.contains("sent")) {
            // For sent mails, show "To: [recipients]"
            String recipients = "";
            if (mail.to != null && !mail.to.isEmpty()) {
                recipients = String.join(", ", mail.to);
            }
            holder.fromView.setText("To: " + recipients);
        } else {
            // For received mails, show "From: [sender]"
            holder.fromView.setText("From: " + mail.from);
        }
        
        holder.bodyView.setText(mail.body);

        // Hide star icon if in trash
        if ("trash".equals(sourceFragment)) {
            holder.starView.setVisibility(View.GONE);
        } else {
            holder.starView.setVisibility(View.VISIBLE);
        }

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
            android.util.Log.d("MailAdapterStar", "Clicked star for mail id=" + mail.id + ", subject=" + mail.subject + ", isStarred=" + mail.isStarred);
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
                    intent.putExtra("source_fragment", sourceFragment);
                    intent.putExtra("isSpam", mail.isSpam);
                    intent.putExtra("isDeleted", mail.isDeleted);
                    if (mail.direction != null) {
                        intent.putStringArrayListExtra("direction", new java.util.ArrayList<>(mail.direction));
                    }
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

    public void deleteSelected() {
        List<MailItem> toRemove = new java.util.ArrayList<>();
        for (MailItem mail : mailList) {
            if (mail.isSelected) {
                toRemove.add(mail);
            }
        }
        mailList.removeAll(toRemove);
        if (selectionListener != null) selectionListener.onSelectionModeChanged(false, 0);
        notifyDataSetChanged();
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

