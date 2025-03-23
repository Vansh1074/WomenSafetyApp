package com.example.womensafetyapp;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import android.app.AlertDialog;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.womensafetyapp.adapters.ContactsAdapter;
import com.example.womensafetyapp.models.Contact;
import com.example.womensafetyapp.models.SharedContactsViewModel;
import com.example.womensafetyapp.models.Email;
import com.example.womensafetyapp.models.SharedContactsViewModelFactory;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import com.example.womensafetyapp.adapters.EmailAdapter;

public class ContactsFragment extends Fragment implements ContactsAdapter.OnContactDeleteListener,EmailAdapter.OnEmailDeleteListener {

    private static final int REQUEST_CONTACT_PERMISSION = 1;
    private static final int PICK_CONTACT_REQUEST = 2;
    private static final String CONTACTS_PREFS = "contacts_prefs";
    private static final String CONTACTS_KEY = "contacts_key";
    private ContactsAdapter contactsAdapter;
    private List<Contact> contactList = new ArrayList<>();
    private SharedPreferences sharedPreferences;
    private SharedContactsViewModel sharedContactsViewModel;

    private List<Email> emailList = new ArrayList<>();
    private RecyclerView emailRecyclerView;
    private EmailAdapter emailAdapter;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_contacts, container, false);

        Button btAddContact = view.findViewById(R.id.btAddContact);
        Button btAddEmail = view.findViewById(R.id.btAddEmail);

        RecyclerView recyclerView = view.findViewById(R.id.rvContacts);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        sharedPreferences = requireActivity().getSharedPreferences(CONTACTS_PREFS, Context.MODE_PRIVATE);
        sharedContactsViewModel = new ViewModelProvider(requireActivity(), new SharedContactsViewModelFactory(requireActivity().getApplication()))
                .get(SharedContactsViewModel.class);

        // Load saved contacts from SharedPreferences
        contactList = loadContacts();
        sharedContactsViewModel.setContactList(contactList);

        contactsAdapter = new ContactsAdapter(contactList, this);
        recyclerView.setAdapter(contactsAdapter);

        emailRecyclerView = view.findViewById(R.id.rvEmails);
        emailRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        // Load saved emails from SharedPreferences
        emailList = loadEmails();

        emailAdapter = new EmailAdapter(emailList, this);  // Pass the delete listener
        emailRecyclerView.setAdapter(emailAdapter);

        btAddContact.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.READ_CONTACTS)
                        != PackageManager.PERMISSION_GRANTED) {
                    ActivityCompat.requestPermissions(requireActivity(),
                            new String[]{Manifest.permission.READ_CONTACTS}, REQUEST_CONTACT_PERMISSION);
                } else {
                    openContactPicker();
                }
            }
        });


        btAddEmail.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showAddEmailDialog();
            }
        });

        return view;
    }

    // Helper method to check if contact already exists in the list
    private boolean isContactInList(String name, String phoneNumber) {
        for (Contact contact : contactList) {
            if (contact.getName().equals(name) && contact.getPhoneNumber().equals(phoneNumber)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public void onContactDelete(Contact contact) {
        contactList.remove(contact);  // Remove contact from list
        contactsAdapter.notifyDataSetChanged();
        saveContacts();  // Save updated contact list to SharedPreferences

        // Update ViewModel with the modified contact list
        sharedContactsViewModel.setContactList(contactList);
    }

    private void openContactPicker() {
        Intent intent = new Intent(Intent.ACTION_PICK, ContactsContract.Contacts.CONTENT_URI);
        startActivityForResult(intent, PICK_CONTACT_REQUEST);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_CONTACT_REQUEST && resultCode == getActivity().RESULT_OK) {
            Uri contactUri = data.getData();
            retrieveContact(contactUri);
        }
    }

    @SuppressLint("Range")
    private void retrieveContact(Uri contactUri) {
        ContentResolver contentResolver = requireActivity().getContentResolver();
        Cursor cursor = contentResolver.query(contactUri, null, null, null, null);

        if (cursor != null && cursor.moveToFirst()) {
            String contactId = cursor.getString(cursor.getColumnIndex(ContactsContract.Contacts._ID));
            String name = cursor.getString(cursor.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME));

            String phoneNumber = null;
            Cursor phones = contentResolver.query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI, null,
                    ContactsContract.CommonDataKinds.Phone.CONTACT_ID + " = ?", new String[]{contactId}, null);

            if (phones != null && phones.moveToFirst()) {
                phoneNumber = phones.getString(phones.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));
                phones.close();
            }

            // Check if contact is already in the list
            if (!isContactInList(name, phoneNumber)) {
                Contact contact = new Contact(name, phoneNumber);
                contactList.add(contact);
                contactsAdapter.notifyDataSetChanged();
                saveContacts(); // Save the updated contact list to SharedPreferences
            } else {
                Toast.makeText(getContext(), "Contact already exists", Toast.LENGTH_SHORT).show();
            }

            cursor.close();
        }
    }

    private void saveContacts() {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        Gson gson = new Gson();
        String json = gson.toJson(contactList);
        editor.putString(CONTACTS_KEY, json);
        editor.apply();
    }

    private List<Contact> loadContacts() {
        Gson gson = new Gson();
        String json = sharedPreferences.getString(CONTACTS_KEY, null);
        Type type = new TypeToken<ArrayList<Contact>>() {}.getType();
        return gson.fromJson(json, type) == null ? new ArrayList<>() : gson.fromJson(json, type);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_CONTACT_PERMISSION) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                openContactPicker();
            } else {
                Toast.makeText(getContext(), "Permission to access contacts is required", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void showAddEmailDialog() {
        // Inflate the dialog layout
        LayoutInflater inflater = LayoutInflater.from(getContext());
        View dialogView = inflater.inflate(R.layout.dialog_add_email, null);

        // Create the dialog
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setView(dialogView);
        AlertDialog dialog = builder.create();

        // Find dialog views
        EditText etEmailInput = dialogView.findViewById(R.id.etEmailInput);
        Button btnOk = dialogView.findViewById(R.id.btnOk);
        Button btnCancel = dialogView.findViewById(R.id.btnCancel);

        // Set listeners for buttons
        btnOk.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String email = etEmailInput.getText().toString().trim();
                if (!email.isEmpty() && android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                    Email newEmail = new Email(email);  // Assuming Email is a model class with a constructor
                    emailList.add(newEmail);
                    emailAdapter.notifyDataSetChanged();
                    saveEmails();
                    dialog.dismiss();
                } else {
                    Toast.makeText(getContext(), "Enter a valid email address", Toast.LENGTH_SHORT).show();
                }
            }
        });

        btnCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });

        dialog.show();
    }


    public void onEmailDelete(Email email) {
        emailList.remove(email);  // Remove email from list
        emailAdapter.notifyDataSetChanged();  // Refresh RecyclerView
        saveEmails();  // Save updated email list to SharedPreferences if needed
    }

    private void saveEmails() {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        Gson gson = new Gson();
        String json = gson.toJson(emailList);
        editor.putString("emails_key", json);
        editor.apply();
    }

    private List<Email> loadEmails() {
        Gson gson = new Gson();
        String json = sharedPreferences.getString("emails_key", null);
        Type type = new TypeToken<ArrayList<Email>>() {}.getType();
        return gson.fromJson(json, type) == null ? new ArrayList<>() : gson.fromJson(json, type);
    }
}
