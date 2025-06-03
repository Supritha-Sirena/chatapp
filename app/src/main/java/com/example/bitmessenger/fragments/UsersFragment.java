package com.example.bitmessenger.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.bitmessenger.R;
import com.example.bitmessenger.activities.ChatActivity;
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

public class UsersFragment extends Fragment {
    private RecyclerView recyclerView;
    private UserAdapter userAdapter;
    private List<User> users;
    private ValueEventListener usersListener;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_users, container, false);

        recyclerView = view.findViewById(R.id.users_recycler_view);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        users = new ArrayList<>();
        userAdapter = new UserAdapter(getContext(), users, user -> {
            Intent intent = new Intent(getActivity(), ChatActivity.class);
            intent.putExtra("user_id", user.getId());
            startActivity(intent);
        });
        recyclerView.setAdapter(userAdapter);

        loadUsers();

        return view;
    }

    private void loadUsers() {
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("users");
        String currentUserId = FirebaseAuth.getInstance().getCurrentUser().getUid();

        usersListener = reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                users.clear();
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    User user = snapshot.getValue(User.class);
                    if (user != null && !user.getId().equals(currentUserId)) {
                        users.add(user);
                    }
                }
                userAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
            }
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (usersListener != null) {
            FirebaseDatabase.getInstance().getReference("users").removeEventListener(usersListener);
        }
    }
} 