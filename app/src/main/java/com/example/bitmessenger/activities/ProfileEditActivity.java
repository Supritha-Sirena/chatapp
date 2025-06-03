package com.example.bitmessenger.activities;

import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import com.bumptech.glide.Glide;
import com.example.bitmessenger.R;
import com.example.bitmessenger.models.User;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import de.hdodenhof.circleimageview.CircleImageView;
import java.util.HashMap;

public class ProfileEditActivity extends AppCompatActivity {
    private static final int PICK_IMAGE_REQUEST = 1;

    private CircleImageView profileImage;
    private EditText nameEditText;
    private EditText statusEditText;
    private Button saveButton;
    private Uri imageUri;
    private DatabaseReference userRef;
    private StorageReference storageRef;
    private ProgressDialog progressDialog;
    private String currentUserId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile_edit);

        // Initialize Firebase
        currentUserId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        userRef = FirebaseDatabase.getInstance().getReference("users").child(currentUserId);
        storageRef = FirebaseStorage.getInstance().getReference("profile_images").child(currentUserId);

        // Setup toolbar
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("Edit Profile");
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        // Initialize views
        profileImage = findViewById(R.id.profile_image);
        nameEditText = findViewById(R.id.name_edit_text);
        statusEditText = findViewById(R.id.status_edit_text);
        saveButton = findViewById(R.id.save_button);

        // Initialize progress dialog
        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Updating profile...");
        progressDialog.setCancelable(false);

        // Load current user data
        loadUserData();

        // Set click listeners
        profileImage.setOnClickListener(v -> {
            Intent intent = new Intent();
            intent.setType("image/*");
            intent.setAction(Intent.ACTION_GET_CONTENT);
            startActivityForResult(Intent.createChooser(intent, "Select Image"), PICK_IMAGE_REQUEST);
        });

        saveButton.setOnClickListener(v -> updateProfile());
    }

    private void loadUserData() {
        userRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                User user = snapshot.getValue(User.class);
                if (user != null) {
                    nameEditText.setText(user.getName());
                    statusEditText.setText(user.getStatus());
                    
                    if (user.getImageUrl() != null && !user.getImageUrl().isEmpty()) {
                        Glide.with(ProfileEditActivity.this)
                                .load(user.getImageUrl())
                                .placeholder(R.drawable.default_profile)
                                .error(R.drawable.default_profile)
                                .into(profileImage);
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError error) {
                Toast.makeText(ProfileEditActivity.this, 
                    "Failed to load profile: " + error.getMessage(), 
                    Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void updateProfile() {
        String name = nameEditText.getText().toString().trim();
        String status = statusEditText.getText().toString().trim();

        if (name.isEmpty()) {
            nameEditText.setError("Name is required");
            nameEditText.requestFocus();
            return;
        }

        progressDialog.show();
        HashMap<String, Object> updates = new HashMap<>();
        updates.put("name", name);
        updates.put("status", status);

        if (imageUri != null) {
            // Upload new image
            storageRef.putFile(imageUri)
                    .addOnSuccessListener(taskSnapshot -> {
                        storageRef.getDownloadUrl().addOnSuccessListener(uri -> {
                            updates.put("imageUrl", uri.toString());
                            updateUserData(updates);
                        });
                    })
                    .addOnFailureListener(e -> {
                        progressDialog.dismiss();
                        Toast.makeText(ProfileEditActivity.this, 
                            "Failed to upload image: " + e.getMessage(), 
                            Toast.LENGTH_SHORT).show();
                    });
        } else {
            // Update without image
            updateUserData(updates);
        }
    }

    private void updateUserData(HashMap<String, Object> updates) {
        userRef.updateChildren(updates)
                .addOnCompleteListener(task -> {
                    progressDialog.dismiss();
                    if (task.isSuccessful()) {
                        Toast.makeText(ProfileEditActivity.this, 
                            "Profile updated successfully", Toast.LENGTH_SHORT).show();
                        finish();
                    } else {
                        Toast.makeText(ProfileEditActivity.this, 
                            "Failed to update profile", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK 
                && data != null && data.getData() != null) {
            imageUri = data.getData();
            Glide.with(this)
                    .load(imageUri)
                    .placeholder(R.drawable.default_profile)
                    .error(R.drawable.default_profile)
                    .into(profileImage);
        }
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }
} 