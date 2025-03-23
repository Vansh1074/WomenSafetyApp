package com.example.womensafetyapp.models;

public class Email {
    private String emailAddress;

    public Email(String emailAddress) {
        this.emailAddress = emailAddress;
    }

    public String getEmailAddress() {
        return emailAddress;
    }

    public void setEmailAddress(String emailAddress) {
        this.emailAddress = emailAddress;
    }

    public String getEmail(){return emailAddress;}
}
