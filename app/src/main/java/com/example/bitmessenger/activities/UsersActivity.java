package com.example.bitmessenger.activities;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.bitmessenger.R;
import com.example.bitmessenger.adapters.UserAdapter;
import com.example.bitmessenger.models.User;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import java.util.ArrayList;
import java.util.List;

public class UsersActivity extends AppCompatActivity {
    private static final String TAG = "UsersActivity";
    private RecyclerView recyclerView;
    private UserAdapter adapter;
    private List<User> userList;
    private ProgressBar progressBar;
    private DatabaseReference usersRef;
    private ValueEventListener usersListener;
    private String currentUserId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_users);

        // Initialize Firebase
        FirebaseAuth auth = FirebaseAuth.getInstance();
        if (auth.getCurrentUser() == null) {
            Toast.makeText(this, "Please login first", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }
        
        currentUserId = auth.getCurrentUser().getUid();
        usersRef = FirebaseDatabase.getInstance().getReference("users");

        // Initialize views
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("Select User");
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        recyclerView = findViewById(R.id.users_recycler_view);
        progressBar = findViewById(R.id.progress_bar);

        // Setup RecyclerView
        userList = new ArrayList<>();
        adapter = new UserAdapter(this, userList, user -> {
            if (user != null && user.getId() != null) {
                Intent intent = new Intent(UsersActivity.this, MessageActivity.class);
                intent.putExtra("userId", user.getId());
                intent.putExtra("userName", user.getName() != null ? user.getName() : "Unknown User");
                startActivity(intent);
            } else {
                Toast.makeText(UsersActivity.this, "Invalid user data", Toast.LENGTH_SHORT).show();
            }
        });

        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);

        // Start listening for users
        loadUsers();
    }

    private void loadUsers() {
        progressBar.setVisibility(View.VISIBLE);
        
        usersListener = usersRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                try {
                    userList.clear();
                    for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                        try {
                            User user = dataSnapshot.getValue(User.class);
                            String userId = dataSnapshot.getKey();
                            
                            if (user != null && userId != null && !userId.equals(currentUserId)) {
                                // Ensure user has all required fields
                                if (user.getName() == null) user.setName("Unknown User");
                                if (user.getEmail() == null) user.setEmail("");
                                if (user.getStatus() == null) user.setStatus("Hey there! I'm using BitMessenger");
                                
                                // Set the user's ID from the snapshot key
                                user.setId(userId);
                                userList.add(user);
                                Log.d(TAG, "Loaded user: " + user.getName() + " (ID: " + userId + ")");
                            }
                        } catch (Exception e) {
                            Log.e(TAG, "Error parsing user data: " + e.getMessage());
                        }
                    }
                    adapter.notifyDataSetChanged();
                    progressBar.setVisibility(View.GONE);

                    if (userList.isEmpty()) {
                        Toast.makeText(UsersActivity.this, "No users found", Toast.LENGTH_SHORT).show();
                    }
                } catch (Exception e) {
                    Log.e(TAG, "Error loading users: " + e.getMessage());
                    progressBar.setVisibility(View.GONE);
                    Toast.makeText(UsersActivity.this, 
                        "Error loading users: " + e.getMessage(), 
                        Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e(TAG, "Database error: " + error.getMessage());
                progressBar.setVisibility(View.GONE);
                Toast.makeText(UsersActivity.this, 
                    "Error: " + error.getMessage(), 
                    Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // Remove the listener when activity is destroyed
        if (usersListener != null) {
            usersRef.removeEventListener(usersListener);
        }
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }
} 