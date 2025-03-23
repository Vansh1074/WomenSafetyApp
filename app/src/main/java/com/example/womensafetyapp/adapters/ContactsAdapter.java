package com.example.womensafetyapp.adapters;

import android.annotation.SuppressLint;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.womensafetyapp.R;
import com.example.womensafetyapp.models.Contact;

import java.util.List;

public class ContactsAdapter extends RecyclerView.Adapter<ContactsAdapter.ContactViewHolder> {

    private List<Contact> contactList;
    private OnContactDeleteListener deleteListener;

    public ContactsAdapter(List<Contact> contactList, OnContactDeleteListener deleteListener) {
        this.contactList = contactList;
        this.deleteListener = deleteListener;
    }

    @NonNull
    @Override
    public ContactViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.custom_contacts_adapter, parent, false);
        return new ContactViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ContactViewHolder holder, @SuppressLint("RecyclerView") int position) {
        Contact contact = contactList.get(position);
        holder.tvName.setText(contact.getName());
        holder.tvPhoneNumber.setText(contact.getPhoneNumber());

        // Set delete button click listener
        holder.btDeleteContact.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                deleteListener.onContactDelete(contact);
                notifyItemRemoved(position);
            }
        });
    }

    @Override
    public int getItemCount() {
        return contactList.size();
    }

    public static class ContactViewHolder extends RecyclerView.ViewHolder {
        TextView tvName, tvPhoneNumber;
        ImageButton btDeleteContact;

        public ContactViewHolder(View itemView) {
            super(itemView);
            tvName = itemView.findViewById(R.id.tvName);
            tvPhoneNumber = itemView.findViewById(R.id.tvPhoneNumber);
            btDeleteContact = itemView.findViewById(R.id.btDeleteContact);
        }
    }

    // Interface for deleting contact
    public interface OnContactDeleteListener {
        void onContactDelete(Contact contact);
    }
}
