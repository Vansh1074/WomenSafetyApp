package com.example.womensafetyapp.models;

import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

public class SharedContactsViewModel extends ViewModel {
    private static final String EMAILS_PREFS = "contacts_prefs";
    private static final String EMAILS_KEY = "emails_key";
    private final MutableLiveData<List<Email>> emailList = new MutableLiveData<>(new ArrayList<>());
    private final SharedPreferences sharedPreferences;

    public SharedContactsViewModel(Application application) {
        sharedPreferences = application.getSharedPreferences(EMAILS_PREFS, Context.MODE_PRIVATE);
        loadEmails();
    }
    private final MutableLiveData<List<Contact>> contactList = new MutableLiveData<>(new ArrayList<>());
    public LiveData<List<Contact>> getContactList() {
        return contactList;
    }

    public void setContactList(List<Contact> contacts) {
        contactList.setValue(contacts);
    }

    public void addContact(Contact contact) {
        List<Contact> currentContacts = contactList.getValue();
        if (currentContacts != null) {
            currentContacts.add(contact);
            contactList.setValue(currentContacts);
        }
    }

    public void removeContact(Contact contact) {
        List<Contact> currentContacts = contactList.getValue();
        if (currentContacts != null) {
            currentContacts.remove(contact);
            contactList.setValue(currentContacts);
        }
    }


    // Load emails from SharedPreferences and set to LiveData
    private void loadEmails() {
        Gson gson = new Gson();
        String json = sharedPreferences.getString(EMAILS_KEY, null);
        Type type = new TypeToken<ArrayList<Email>>() {}.getType();
        List<Email> emails = gson.fromJson(json, type);
        if (emails == null) {
            emails = new ArrayList<>();
        }
        emailList.setValue(emails);
    }

    // Method to get the LiveData email list
    public LiveData<List<Email>> getEmailList() {
        return emailList;
    }
}

