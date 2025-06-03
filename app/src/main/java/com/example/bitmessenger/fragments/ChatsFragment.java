package com.example.bitmessenger.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.example.bitmessenger.R;
import com.example.bitmessenger.activities.MessageActivity;
import com.example.bitmessenger.adapters.UsersAdapter;
import com.example.bitmessenger.models.User;
import java.util.ArrayList;
import java.util.List;

public class ChatsFragment extends Fragment implements UsersAdapter.OnUserClickListener {
    private static final String TAG = "ChatsFragment";
    private RecyclerView usersRecyclerView;
    private UsersAdapter usersAdapter;
    private List<User> userList;
    private View emptyView;
    private FirebaseUser currentUser;
    private DatabaseReference usersRef;
    private ValueEventListener usersListener;
    private FirebaseAuth mAuth;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_chats, container, false);

        // Initialize views
        usersRecyclerView = view.findViewById(R.id.users_recycler_view);
        emptyView = view.findViewById(R.id.empty_view);

        // Initialize Firebase Auth
        mAuth = FirebaseAuth.getInstance();
        currentUser = mAuth.getCurrentUser();
        
        // Check authentication state
        if (currentUser == null) {
            Log.e(TAG, "User is not authenticated");
            Toast.makeText(getContext(), "Please login first", Toast.LENGTH_SHORT).show();
            return view;
        }

        Log.d(TAG, "Current user ID: " + currentUser.getUid());

        // Initialize Firebase Database reference
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        usersRef = database.getReference().child("users");
        
        // Log the database path
        Log.d(TAG, "Database path: " + usersRef.toString());

        // Setup RecyclerView
        userList = new ArrayList<>();
        usersAdapter = new UsersAdapter(getContext(), userList, this);
        usersRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        usersRecyclerView.setAdapter(usersAdapter);

        // Load users
        loadUsers();

        return view;
    }

    private void loadUsers() {
        if (getContext() == null) return;

        // Verify authentication before loading
        if (currentUser == null || mAuth.getCurrentUser() == null) {
            Log.e(TAG, "Attempting to load users while not authenticated");
            Toast.makeText(getContext(), "Authentication required", Toast.LENGTH_SHORT).show();
            return;
        }

        usersListener = usersRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                try {
                    Log.d(TAG, "Number of users found: " + snapshot.getChildrenCount());
                    userList.clear();
                    for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                        try {
                            String userId = dataSnapshot.getKey();
                            User user = dataSnapshot.getValue(User.class);
                            
                            if (user != null && userId != null) {
                                // Set the user's ID from the snapshot key
                                user.setId(userId);
                                
                                // Skip current user
                                if (userId.equals(currentUser.getUid())) {
                                    continue;
                                }

                                // Ensure required fields are present
                                if (user.getName() == null) user.setName("Unknown User");
                                if (user.getEmail() == null) user.setEmail("");
                                if (user.getStatus() == null) user.setStatus("Hey there! I'm using BitMessenger");
                                
                                userList.add(user);
                                Log.d(TAG, "Loaded user: " + user.getName() + " (ID: " + userId + ")");
                            } else {
                                Log.w(TAG, "Invalid user data at: " + dataSnapshot.getKey());
                            }
                        } catch (Exception e) {
                            Log.e(TAG, "Error parsing user data: " + e.getMessage());
                        }
                    }
                    
                    if (getContext() != null) {
                        usersAdapter.notifyDataSetChanged();
                        updateEmptyView();
                        Log.d(TAG, "Updated user list with " + userList.size() + " users");
                    }
                } catch (Exception e) {
                    Log.e(TAG, "Error loading users: " + e.getMessage(), e);
                    if (getContext() != null) {
                        Toast.makeText(getContext(), 
                            "Error loading users: " + e.getMessage(), 
                            Toast.LENGTH_LONG).show();
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e(TAG, "Database error: " + error.getMessage() + "\nDetails: " + error.getDetails());
                if (getContext() != null) {
                    Toast.makeText(getContext(), 
                        "Failed to load users: " + error.getMessage(), 
                        Toast.LENGTH_LONG).show();
                }
            }
        });
    }

    private void updateEmptyView() {
        if (emptyView != null) {
            boolean isEmpty = userList == null || userList.isEmpty();
            emptyView.setVisibility(isEmpty ? View.VISIBLE : View.GONE);
            usersRecyclerView.setVisibility(isEmpty ? View.GONE : View.VISIBLE);
        }
    }

    @Override
    public void onUserClick(User user) {
        if (getActivity() != null && user != null && user.getId() != null) {
            Log.d(TAG, "Starting chat with user: " + user.getName() + " (ID: " + user.getId() + ")");
            Intent intent = new Intent(getActivity(), MessageActivity.class);
            intent.putExtra("userId", user.getId());
            intent.putExtra("userName", user.getName());
            startActivity(intent);
        } else {
            if (getContext() != null) {
                Toast.makeText(getContext(), "Invalid user data", Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (usersListener != null) {
            usersRef.removeEventListener(usersListener);
        }
    }
}
