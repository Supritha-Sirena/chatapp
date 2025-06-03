package com.example.bitmessenger.activities;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.bitmessenger.R;
import com.example.bitmessenger.adapters.UserSelectionAdapter;
import com.example.bitmessenger.models.User;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class CreateGroupActivity extends AppCompatActivity {
    private EditText groupNameEditText;
    private RecyclerView usersRecyclerView;
    private Button createGroupButton;
    private ProgressBar progressBar;
    private UserSelectionAdapter adapter;
    private List<User> userList;
    private DatabaseReference usersRef;
    private String currentUserId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_group);

        // Initialize Firebase
        currentUserId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        usersRef = FirebaseDatabase.getInstance().getReference("users");

        // Initialize views
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        groupNameEditText = findViewById(R.id.group_name_edittext);
        usersRecyclerView = findViewById(R.id.users_recycler_view);
        createGroupButton = findViewById(R.id.create_group_button);
        progressBar = findViewById(R.id.progress_bar);

        // Setup RecyclerView
        userList = new ArrayList<>();
        adapter = new UserSelectionAdapter(this, userList);
        usersRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        usersRecyclerView.setAdapter(adapter);

        // Load users
        loadUsers();

        // Setup create button
        createGroupButton.setOnClickListener(v -> createGroup());

        // Setup user selection listener
        adapter.setOnUserSelectedListener((user, isSelected) -> {
            // Update UI or store selected users as needed
            updateCreateButtonState();
        });
    }

    private void loadUsers() {
        progressBar.setVisibility(View.VISIBLE);
        usersRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                userList.clear();
                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                    try {
                        User user = dataSnapshot.getValue(User.class);
                        String userId = dataSnapshot.getKey();
                        
                        if (user != null && userId != null) {
                            // Set the user's ID from the snapshot key
                            user.setId(userId);
                            
                            // Only add other users (not current user)
                            if (!userId.equals(currentUserId)) {
                                // Ensure required fields are present
                                if (user.getName() == null) user.setName("Unknown User");
                                if (user.getEmail() == null) user.setEmail("");
                                if (user.getStatus() == null) user.setStatus("Hey there! I'm using BitMessenger");
                                
                                userList.add(user);
                            }
                        }
                    } catch (Exception e) {
                        Log.e("CreateGroupActivity", "Error parsing user data: " + e.getMessage());
                    }
                }
                adapter.notifyDataSetChanged();
                progressBar.setVisibility(View.GONE);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                progressBar.setVisibility(View.GONE);
                Toast.makeText(CreateGroupActivity.this, "Failed to load users: " + error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void updateCreateButtonState() {
        createGroupButton.setEnabled(!adapter.getSelectedUsers().isEmpty());
    }

    private void createGroup() {
        String groupName = groupNameEditText.getText().toString().trim();
        List<User> selectedUsers = adapter.getSelectedUsers();

        if (groupName.isEmpty()) {
            groupNameEditText.setError("Group name is required");
            return;
        }

        if (selectedUsers.isEmpty()) {
            Toast.makeText(this, "Please select at least one member", Toast.LENGTH_SHORT).show();
            return;
        }

        progressBar.setVisibility(View.VISIBLE);
        createGroupButton.setEnabled(false);

        // Create group in Firebase
        DatabaseReference groupsRef = FirebaseDatabase.getInstance().getReference("Groups");
        String groupId = groupsRef.push().getKey();

        if (groupId != null) {
            // Create group object
            HashMap<String, Object> groupMap = new HashMap<>();
            groupMap.put("id", groupId);
            groupMap.put("name", groupName);
            groupMap.put("admin", currentUserId);
            groupMap.put("timestamp", System.currentTimeMillis());

            // Add members
            HashMap<String, Boolean> members = new HashMap<>();
            members.put(currentUserId, true); // Add current user as member
            for (User user : selectedUsers) {
                members.put(user.getId(), true);
            }
            groupMap.put("members", members);

            // Save to Firebase
            groupsRef.child(groupId).setValue(groupMap).addOnCompleteListener(task -> {
                progressBar.setVisibility(View.GONE);
                if (task.isSuccessful()) {
                    Toast.makeText(this, "Group created successfully", Toast.LENGTH_SHORT).show();
                    finish();
                } else {
                    createGroupButton.setEnabled(true);
                    Toast.makeText(this, "Failed to create group", Toast.LENGTH_SHORT).show();
                }
            });
        }
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }
}