package com.example.womensafetyapp.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.womensafetyapp.R;
import com.example.womensafetyapp.models.Email;
import java.util.List;

public class EmailAdapter extends RecyclerView.Adapter<EmailAdapter.EmailViewHolder> {

    private List<Email> emailList;
    private OnEmailDeleteListener deleteListener;

    public EmailAdapter(List<Email> emailList, OnEmailDeleteListener deleteListener) {
        this.emailList = emailList;
        this.deleteListener = deleteListener;
    }

    @NonNull
    @Override
    public EmailViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.custom_email_adapter, parent, false);
        return new EmailViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull EmailViewHolder holder, int position) {
        Email email = emailList.get(position);
        holder.emailTextView.setText(email.getEmailAddress());

        // Handle delete icon click
        holder.deleteIcon.setOnClickListener(v -> {
            if (deleteListener != null) {
                deleteListener.onEmailDelete(email);
            }
        });
    }

    @Override
    public int getItemCount() {
        return emailList.size();
    }

    public interface OnEmailDeleteListener {
        void onEmailDelete(Email email);
    }

    public static class EmailViewHolder extends RecyclerView.ViewHolder {
        TextView emailTextView;
        ImageView deleteIcon;

        public EmailViewHolder(@NonNull View itemView) {
            super(itemView);
            emailTextView = itemView.findViewById(R.id.tvEmailAddress);
            deleteIcon = itemView.findViewById(R.id.ivDeleteEmail);
        }
    }
}
