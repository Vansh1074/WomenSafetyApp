package com.example.womensafetyapp;

import android.os.Bundle;
import android.view.MenuItem;

import androidx.appcompat.widget.Toolbar;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import com.google.android.material.bottomnavigation.BottomNavigationView;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Set up Toolbar
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        BottomNavigationView bottomNavigationView = findViewById(R.id.bottom_navigation);

        // Load the ContactsFragment first
        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.fragment_container, new ContactsFragment())
                    .commit();

            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.fragment_container, new HomeFragment())
                    .commit();
        }

        // Bottom navigation item selection handling
        bottomNavigationView.setOnItemSelectedListener(new BottomNavigationView.OnItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                Fragment selectedFragment = null;
                String toolbarTitle = "";

                int id = item.getItemId();
                if (id == R.id.nav_home) {
                    selectedFragment = new HomeFragment();
                    toolbarTitle = "Women Safety App";
                } else if (id == R.id.nav_contacts) {
                    selectedFragment = new ContactsFragment();
                    toolbarTitle = "Contacts";
                } else if (id == R.id.nav_map) {
                    selectedFragment = new MapFragment();
                    toolbarTitle = "Map";
                } else if (id == R.id.nav_profile) {
                    selectedFragment = new ProfileFragment();
                    toolbarTitle = "Profile";
                }

                if (selectedFragment != null) {
                    getSupportFragmentManager().beginTransaction()
                            .replace(R.id.fragment_container, selectedFragment)
                            .commit();
                    toolbar.setTitle(toolbarTitle);
                    return true;
                }
                return false;
            }
        });
    }

}
