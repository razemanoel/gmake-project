package com.example.app.adapters;

import android.graphics.Color;
import android.graphics.Typeface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;
import com.example.app.utils.DateUtils;
import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.example.app.R;
import com.example.app.entities.Mail;
import com.example.app.entities.MailLabel;

import java.util.ArrayList;
import java.util.List;

public class MailCardAdapter extends RecyclerView.Adapter<MailCardAdapter.MailViewHolder> {

    public interface OnMailActionListener {
        void onMailClick(Mail mail);
        void onToggleRead(Mail mail);
        void onStar(Mail mail);
        void onTrash(Mail mail);
    }

    private List<Mail> mailList = new ArrayList<>();
    private final OnMailActionListener listener;

    public MailCardAdapter(OnMailActionListener listener) {
        this.listener = listener;
    }

    public void setMailList(List<Mail> mails) {
        this.mailList = mails != null ? mails : new ArrayList<>();
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public MailViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.mail_card_item, parent, false);
        return new MailViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull MailViewHolder h, int position) {
        Mail mail = mailList.get(position);

        // Subject and body
        h.subject.setText(mail.getSubject());
        h.body   .setText(mail.getBody());

        // — Unread? → bold, otherwise normal
        boolean isUnread = false;
        if (mail.getLabels() != null) {
            for (MailLabel l : mail.getLabels()) {
                if ("unread".equalsIgnoreCase(l.getId())) {
                    isUnread = true;
                    break;
                }
            }
        }
        int textStyle = isUnread ? Typeface.BOLD : Typeface.NORMAL;
        h.subject   .setTypeface(h.subject.getTypeface(),   textStyle);
        h.body      .setTypeface(h.body.getTypeface(),      textStyle);
        h.from      .setTypeface(h.from.getTypeface(),      textStyle);
        h.timestamp .setTypeface(h.timestamp.getTypeface(), textStyle);
        h.labels    .setTypeface(h.labels.getTypeface(),    textStyle);

        int bgColor = isUnread
                ? ContextCompat.getColor(h.itemView.getContext(), R.color.unread_light_blue)
                : Color.WHITE;
        h.itemView.setBackgroundColor(bgColor);

        // — From/To logic
        boolean isSent = false;
        if (mail.getLabels() != null) {
            for (MailLabel lbl : mail.getLabels()) {
                if ("sent".equalsIgnoreCase(lbl.getId())) {
                    isSent = true;
                    break;
                }
            }
        }
        h.from.setText(isSent
                ? "To:   " + mail.getToUsername()
                : "From: " + mail.getFromUsername()
        );

        // Timestamp
        h.timestamp.setText("Sent: " + DateUtils.formatMailTs(mail.getTimestamp()));

        // Labels line
        if (mail.getLabels() != null && !mail.getLabels().isEmpty()) {
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < mail.getLabels().size(); i++) {
                if (i > 0) sb.append(", ");
                sb.append(mail.getLabels().get(i).getName());
            }
            h.labels.setText("Labels: " + sb);
        } else {
            h.labels.setText("Labels: None");
        }

        // — Starred? choose icon
        boolean isStarred = false;
        if (mail.getLabels() != null) {
            for (MailLabel l : mail.getLabels()) {
                if ("star".equalsIgnoreCase(l.getId())) {
                    isStarred = true;
                    break;
                }
            }
        }
        h.btnStar.setImageResource(
                isStarred
                        ? android.R.drawable.btn_star_big_on
                        : android.R.drawable.btn_star_big_off
        );

        // — Read/unread icon
        h.btnToggleRead.setImageResource(
                isUnread
                        ? R.drawable.ic_markunread  // your “unread” drawable
                        : R.drawable.ic_markread     // your “read” drawable
        );

        // Trash icon is static in XML (ic_menu_delete)

        // — Click to open details
        h.itemView.setOnClickListener(v -> {
            if (listener != null) listener.onMailClick(mail);
        });

        // — Action buttons
        h.btnToggleRead.setOnClickListener(v -> listener.onToggleRead(mail));
        h.btnStar      .setOnClickListener(v -> listener.onStar(mail));
        h.btnTrash     .setOnClickListener(v -> listener.onTrash(mail));
    }

    @Override
    public int getItemCount() {
        return mailList.size();
    }

    public static class MailViewHolder extends RecyclerView.ViewHolder {
        public final TextView subject, body, from, timestamp, labels;
        public final ImageButton btnToggleRead, btnStar, btnTrash;

        public MailViewHolder(@NonNull View itemView) {
            super(itemView);
            subject        = itemView.findViewById(R.id.textSubject);
            body           = itemView.findViewById(R.id.textBody);
            from           = itemView.findViewById(R.id.textFrom);
            timestamp      = itemView.findViewById(R.id.textTimestamp);
            labels         = itemView.findViewById(R.id.textLabels);

            btnToggleRead = itemView.findViewById(R.id.btnToggleRead);
            btnStar       = itemView.findViewById(R.id.btnStar);
            btnTrash      = itemView.findViewById(R.id.btnTrash);
        }
    }
}
