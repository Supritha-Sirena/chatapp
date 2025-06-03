package com.example.bitmessenger.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import com.example.bitmessenger.R;
import com.example.bitmessenger.fragments.ChatsFragment;
import com.example.bitmessenger.fragments.GroupsFragment;
import com.example.bitmessenger.fragments.SettingsFragment;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import java.util.HashMap;

public class MainActivity extends AppCompatActivity {
    private FirebaseUser currentUser;
    private DatabaseReference reference;
    private FragmentManager fragmentManager;
    private Fragment activeFragment;
    private Fragment chatsFragment;
    private Fragment groupsFragment;
    private Fragment settingsFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        // Check if user is logged in
        currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser == null) {
            startActivity(new Intent(this, LoginActivity.class));
            finish();
            return;
        }

        setContentView(R.layout.activity_main);

        // Initialize Firebase
        reference = FirebaseDatabase.getInstance().getReference();

        // Setup toolbar
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("BitMessenger");
        }

        // Initialize fragments
        fragmentManager = getSupportFragmentManager();
        chatsFragment = new ChatsFragment();
        groupsFragment = new GroupsFragment();
        settingsFragment = new SettingsFragment();
        activeFragment = chatsFragment;

        // Add fragments
        fragmentManager.beginTransaction()
                .add(R.id.fragment_container, settingsFragment, "settings")
                .hide(settingsFragment)
                .add(R.id.fragment_container, groupsFragment, "groups")
                .hide(groupsFragment)
                .add(R.id.fragment_container, chatsFragment, "chats")
                .commit();

        // Setup bottom navigation
        BottomNavigationView bottomNav = findViewById(R.id.bottom_navigation);
        bottomNav.setOnNavigationItemSelectedListener(item -> {
            Fragment selectedFragment = null;
            int itemId = item.getItemId();
            
            if (itemId == R.id.nav_chats) {
                selectedFragment = chatsFragment;
            } else if (itemId == R.id.nav_groups) {
                selectedFragment = groupsFragment;
            } else if (itemId == R.id.nav_settings) {
                selectedFragment = settingsFragment;
            }

            if (selectedFragment != null && selectedFragment != activeFragment) {
                fragmentManager.beginTransaction()
                        .hide(activeFragment)
                        .show(selectedFragment)
                        .commit();
                activeFragment = selectedFragment;
                return true;
            }
            return false;
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        updateStatus(true);
    }

    @Override
    protected void onPause() {
        super.onPause();
        updateStatus(false);
    }

    private void updateStatus(boolean online) {
        if (currentUser != null) {
            HashMap<String, Object> hashMap = new HashMap<>();
            hashMap.put("online", online);
            if (!online) {
                hashMap.put("lastSeen", System.currentTimeMillis());
            }
            reference.child("users").child(currentUser.getUid()).updateChildren(hashMap);
        }
    }
}