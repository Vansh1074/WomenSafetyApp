package com.example.womensafetyapp;

import static java.security.AccessController.getContext;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import com.example.womensafetyapp.models.Contact;
import com.example.womensafetyapp.models.Email;
import com.example.womensafetyapp.models.SharedContactsViewModel;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.core.app.ActivityCompat;

import java.util.ArrayList;
import java.util.List;

public class HomeFragment extends Fragment {

    private SharedContactsViewModel sharedContactsViewModel;
    private List<Contact> contactList = new ArrayList<>();
    List<Email> emailList = new ArrayList<>();
    Button btEmergencyCall, btShareLocation, btSoundAlarm, btCallPolice;
    private MediaPlayer mediaPlayer;
    private FusedLocationProviderClient fusedLocationClient;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home, container, false);

        btEmergencyCall = view.findViewById(R.id.btEmergencyCall);
        btShareLocation = view.findViewById(R.id.btShareLocation);
        btSoundAlarm = view.findViewById(R.id.btSoundAlarm);
        btCallPolice = view.findViewById(R.id.btCallPolice);

        // Initialize the FusedLocationProviderClient
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireContext());


        // Get the ViewModel
        sharedContactsViewModel = new ViewModelProvider(requireActivity()).get(SharedContactsViewModel.class);

        // Observe changes in the contact list
        sharedContactsViewModel.getContactList().observe(getViewLifecycleOwner(), new Observer<List<Contact>>() {
            @Override
            public void onChanged(List<Contact> contacts) {
                contactList.clear();
                contactList.addAll(contacts);
                // Notify the user with a Toast showing the updated count
//                Toast.makeText(getContext(), "Updated Contacts count: " + contactList.size(), Toast.LENGTH_SHORT).show();
            }
        });

        sharedContactsViewModel.getEmailList().observe(getViewLifecycleOwner(), new Observer<List<Email>>() {
            @Override
            public void onChanged(List<Email> emails) {
                // Handle the email list here, you can log it or use it as needed
                emailList.clear();
                emailList.addAll(emails);
            }
        });

        btEmergencyCall.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Check for CALL_PHONE permission
                if (ActivityCompat.checkSelfPermission(requireContext(), Manifest.permission.CALL_PHONE) != PackageManager.PERMISSION_GRANTED) {
                    Toast.makeText(getContext(), "Permission to make calls is required", Toast.LENGTH_SHORT).show();
                    // Request permission if not granted
                    ActivityCompat.requestPermissions(requireActivity(), new String[]{Manifest.permission.CALL_PHONE}, 1);
                } else {
                    for (Contact contact : contactList) {
                        String phoneNumber = contact.getPhoneNumber();
                        if (phoneNumber != null && !phoneNumber.isEmpty()) {
                            // Create intent to call the contact
                            Intent callIntent = new Intent(Intent.ACTION_CALL);
                            callIntent.setData(Uri.parse("tel:" + phoneNumber));
                            startActivity(callIntent);
//                            break; // Remove this break to call all contacts in a loop
                        }
                    }
                }
            }
        });
        btSoundAlarm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (btSoundAlarm.getText().toString().equals("Sound Alarm")) {
                    startAlarm();
                    btSoundAlarm.setText("Stop Alarm");
                } else {
                    stopAlarm();
                    btSoundAlarm.setText("Sound Alarm");
                }

            }
        });

        btShareLocation.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (ActivityCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                    // Request location permission if not granted
                    ActivityCompat.requestPermissions(requireActivity(), new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1);
                } else {
                    // Fetch the current location and send it
                    getLocationAndShare();
                }
            }
        });



        btCallPolice.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (ActivityCompat.checkSelfPermission(requireContext(), Manifest.permission.CALL_PHONE) != PackageManager.PERMISSION_GRANTED) {
                    Toast.makeText(getContext(), "Permission to make calls is required", Toast.LENGTH_SHORT).show();
                    // Request permission if not granted
                    ActivityCompat.requestPermissions(requireActivity(), new String[]{Manifest.permission.CALL_PHONE}, 1);
                } else {
                    Intent callIntent = new Intent(Intent.ACTION_CALL);
                    callIntent.setData(Uri.parse("tel:" + "9998355888"));
                    startActivity(callIntent);
                }
            }
        });

        return view;
    }


    private void getLocationAndShare() {
        if (ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }

        fusedLocationClient.getLastLocation()
                .addOnSuccessListener(requireActivity(), location -> {
                    if (location != null) {
                        double latitude = location.getLatitude();
                        double longitude = location.getLongitude();

                        // Create a Google Maps link
                        String locationLink = "https://www.google.com/maps?q=" + latitude + "," + longitude;
                        String locationString = "I am currently at: \n" + locationLink;

                        // Send the location link to all emails
                        sendLocationToEmails(locationString);
                    } else {
                        Toast.makeText(getContext(), "Location not available", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void sendLocationToEmails(String locationString) {
        // Create a list to hold all email addresses
        List<String> emailAddresses = new ArrayList<>();

        // Loop through the email list and add all email addresses to the list
        for (Email email : emailList) {
            String recipient = email.getEmailAddress();  // Get the email address
            if (recipient != null && !recipient.isEmpty()) {
                emailAddresses.add(recipient);  // Add non-empty email to the list
            }
        }

        // Convert the list to a String array for the intent
        String[] recipients = emailAddresses.toArray(new String[0]);

        // Prepare the email subject and message
        String subject = "Emergency Location";
        String message = locationString;

        // Create an Intent to send an email
        Intent emailIntent = new Intent(Intent.ACTION_SEND);
        emailIntent.putExtra(Intent.EXTRA_EMAIL, recipients);  // Add all email addresses
        emailIntent.putExtra(Intent.EXTRA_SUBJECT, subject);
        emailIntent.putExtra(Intent.EXTRA_TEXT, message);

        // Set the MIME type to prompt the user to choose an email client
        emailIntent.setType("message/rfc822");

        // Check if there is an email app available
        if (emailIntent.resolveActivity(requireContext().getPackageManager()) != null) {
            startActivity(Intent.createChooser(emailIntent, "Choose an Email client:"));
        } else {
            Toast.makeText(getContext(), "No email app found", Toast.LENGTH_SHORT).show();
        }
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 1) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Permission granted, get the location
                getLocationAndShare();
            } else {
                Toast.makeText(getContext(), "Location permission is required to share your location", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void startAlarm() {
        if (mediaPlayer == null) {
            mediaPlayer = MediaPlayer.create(getContext(), R.raw.alarm_sound);
            mediaPlayer.setLooping(true); // Set to loop so it continue until stopped
            setVolumeToMax(); // funciton to set volume to max
            mediaPlayer.start(); // Start playing the alarm sound
        } else if (!mediaPlayer.isPlaying()) {
            mediaPlayer.start(); // Resume playing if not already playing
        }
    }

    private void stopAlarm() {
        if (mediaPlayer != null && mediaPlayer.isPlaying()) {
            mediaPlayer.stop();
            mediaPlayer.release();
            mediaPlayer = null;
        }
    }

    private void setVolumeToMax() {
        AudioManager audioManager = (AudioManager) requireContext().getSystemService(Context.AUDIO_SERVICE);
        if (audioManager != null) {
            audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC), 0);
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (mediaPlayer != null) {
            mediaPlayer.stop();
            mediaPlayer.release();
            mediaPlayer = null;
        }
    }

}
