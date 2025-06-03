package com.example.bitmessenger.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import androidx.fragment.app.Fragment;
import com.bumptech.glide.Glide;
import com.example.bitmessenger.R;
import com.example.bitmessenger.activities.LoginActivity;
import com.example.bitmessenger.activities.ProfileEditActivity;
import com.example.bitmessenger.models.User;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import de.hdodenhof.circleimageview.CircleImageView;

public class SettingsFragment extends Fragment {
    private CircleImageView profileImage;
    private TextView profileName;
    private TextView profileStatus;
    private Button editProfileButton;
    private Button logoutButton;
    private FirebaseAuth auth;
    private DatabaseReference userRef;
    private ValueEventListener userListener;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_settings, container, false);

        // Initialize Firebase
        auth = FirebaseAuth.getInstance();
        if (auth.getCurrentUser() == null) {
            startActivity(new Intent(getActivity(), LoginActivity.class));
            if (getActivity() != null) {
                getActivity().finish();
            }
            return view;
        }

        userRef = FirebaseDatabase.getInstance().getReference("users").child(auth.getCurrentUser().getUid());

        // Initialize views
        profileImage = view.findViewById(R.id.profile_image);
        profileName = view.findViewById(R.id.profile_name);
        profileStatus = view.findViewById(R.id.profile_status);
        editProfileButton = view.findViewById(R.id.edit_profile_button);
        logoutButton = view.findViewById(R.id.logout_button);

        // Set click listeners
        editProfileButton.setOnClickListener(v -> {
            if (getActivity() != null) {
                startActivity(new Intent(getActivity(), ProfileEditActivity.class));
            }
        });

        logoutButton.setOnClickListener(v -> {
            auth.signOut();
            if (getActivity() != null) {
                startActivity(new Intent(getActivity(), LoginActivity.class));
                getActivity().finish();
            }
        });

        // Load user data
        loadUserData();

        return view;
    }

    private void loadUserData() {
        userListener = userRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                if (getActivity() == null) return;

                User user = snapshot.getValue(User.class);
                if (user != null) {
                    // Set user name, defaulting to "Unknown User" if null
                    String name = user.getName();
                    if (name == null || name.trim().isEmpty()) {
                        name = "Unknown User";
                        // Update the name in Firebase to prevent future "Unknown User" displays
                        userRef.child("name").setValue(name);
                    }
                    profileName.setText(name);
                    
                    // Set user status
                    String status = user.getStatus();
                    if (status == null || status.trim().isEmpty()) {
                        status = "Hey there! I'm using BitMessenger";
                        // Update the status in Firebase
                        userRef.child("status").setValue(status);
                    }
                    profileStatus.setText(status);
                    
                    // Load profile image
                    if (user.getImageUrl() != null && !user.getImageUrl().isEmpty()) {
                        Glide.with(getActivity())
                                .load(user.getImageUrl())
                                .placeholder(R.drawable.default_profile)
                                .error(R.drawable.default_profile)
                                .into(profileImage);
                    } else {
                        profileImage.setImageResource(R.drawable.default_profile);
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError error) {
                if (getActivity() != null) {
                    Toast.makeText(getActivity(), 
                        "Failed to load profile: " + error.getMessage(), 
                        Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (userListener != null) {
            userRef.removeEventListener(userListener);
        }
    }
}
