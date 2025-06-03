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
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.example.bitmessenger.R;
import com.example.bitmessenger.activities.CreateGroupActivity;
import com.example.bitmessenger.activities.GroupChatActivity;
import com.example.bitmessenger.adapters.GroupsAdapter;
import com.example.bitmessenger.models.Group;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class GroupsFragment extends Fragment implements GroupsAdapter.OnGroupClickListener {
    private static final String TAG = "GroupsFragment";
    private RecyclerView groupsRecyclerView;
    private GroupsAdapter adapter;
    private List<Group> groupList;
    private DatabaseReference groupsRef;
    private FirebaseUser currentUser;
    private FloatingActionButton createGroupButton;
    private View emptyView;
    private ValueEventListener groupsListener;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_groups, container, false);

        // Initialize Firebase
        currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser == null) {
            Log.e(TAG, "User is not authenticated");
            Toast.makeText(getContext(), "Please login first", Toast.LENGTH_SHORT).show();
            return view;
        }
        
        groupsRef = FirebaseDatabase.getInstance().getReference("Groups");

        // Initialize views with null checks
        initializeViews(view);

        // Setup RecyclerView
        setupRecyclerView();

        // Load groups
        loadGroups();

        return view;
    }

    private void initializeViews(View view) {
        if (view == null) {
            Log.e(TAG, "View is null during initialization");
            return;
        }

        groupsRecyclerView = view.findViewById(R.id.groups_recycler_view);
        if (groupsRecyclerView == null) {
            Log.e(TAG, "Failed to find groups_recycler_view");
        }

        createGroupButton = view.findViewById(R.id.create_group_button);
        if (createGroupButton == null) {
            Log.e(TAG, "Failed to find create_group_button");
        } else {
            createGroupButton.setOnClickListener(v -> {
                if (getActivity() != null) {
                    Intent intent = new Intent(getActivity(), CreateGroupActivity.class);
                    startActivity(intent);
                } else {
                    Log.e(TAG, "Activity is null when trying to start CreateGroupActivity");
                }
            });
        }

        emptyView = view.findViewById(R.id.empty_view);
        if (emptyView == null) {
            Log.e(TAG, "Failed to find empty_view");
        }
    }

    private void setupRecyclerView() {
        if (groupsRecyclerView == null) {
            Log.e(TAG, "Cannot setup RecyclerView - view is null");
            return;
        }

        groupList = new ArrayList<>();
        adapter = new GroupsAdapter(getContext(), groupList, this);
        groupsRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        groupsRecyclerView.setAdapter(adapter);
    }

    private void loadGroups() {
        if (currentUser == null || getContext() == null) return;

        groupsListener = groupsRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                try {
                    Log.d(TAG, "Loading groups. Count: " + snapshot.getChildrenCount());
                    groupList.clear();
                    
                    for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                        try {
                            String groupId = dataSnapshot.getKey();
                            Group group = dataSnapshot.getValue(Group.class);
                            
                            if (group != null && groupId != null) {
                                // Set the group ID
                                group.setId(groupId);
                                
                                // Ensure required fields are present
                                if (group.getName() == null) {
                                    group.setName("Unnamed Group");
                                }
                                if (group.getMembers() == null) {
                                    group.setMembers(new HashMap<>());
                                }
                                
                                // Only add groups where the current user is a member
                                if (group.getMembers().containsKey(currentUser.getUid()) && 
                                    Boolean.TRUE.equals(group.getMembers().get(currentUser.getUid()))) {
                                    groupList.add(group);
                                    Log.d(TAG, "Added group: " + group.getName());
                                }
                            } else {
                                Log.w(TAG, "Invalid group data at: " + groupId);
                            }
                        } catch (Exception e) {
                            Log.e(TAG, "Error parsing group data: " + e.getMessage());
                        }
                    }
                    
                    if (getContext() != null) {
                        adapter.notifyDataSetChanged();
                        updateEmptyView();
                        Log.d(TAG, "Updated group list with " + groupList.size() + " groups");
                    }
                } catch (Exception e) {
                    Log.e(TAG, "Error loading groups: " + e.getMessage(), e);
                    if (getContext() != null) {
                        Toast.makeText(getContext(), 
                            "Error loading groups: " + e.getMessage(), 
                            Toast.LENGTH_LONG).show();
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e(TAG, "Database error: " + error.getMessage() + "\nDetails: " + error.getDetails());
                if (getContext() != null) {
                    Toast.makeText(getContext(), 
                        "Failed to load groups: " + error.getMessage(), 
                        Toast.LENGTH_LONG).show();
                }
            }
        });
    }

    private void updateEmptyView() {
        if (emptyView != null) {
            boolean isEmpty = groupList == null || groupList.isEmpty();
            emptyView.setVisibility(isEmpty ? View.VISIBLE : View.GONE);
            groupsRecyclerView.setVisibility(isEmpty ? View.GONE : View.VISIBLE);
        }
    }

    @Override
    public void onGroupClick(String groupId) {
        if (getActivity() != null && groupId != null) {
            Intent intent = new Intent(getActivity(), GroupChatActivity.class);
            intent.putExtra("groupId", groupId);
            startActivity(intent);
        } else {
            if (getContext() != null) {
                Toast.makeText(getContext(), "Invalid group", Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (groupsListener != null) {
            groupsRef.removeEventListener(groupsListener);
        }
    }
}