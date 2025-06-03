package com.example.bitmessenger.activities;

import android.os.Bundle;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.bitmessenger.R;
import com.example.bitmessenger.adapters.GroupMessageAdapter;
import com.example.bitmessenger.models.GroupMessage;
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

public class GroupChatActivity extends AppCompatActivity {
    private RecyclerView messagesRecyclerView;
    private EditText messageInput;
    private ImageButton sendButton;
    private TextView groupNameText;
    private TextView membersCountText;
    private GroupMessageAdapter adapter;
    private List<GroupMessage> messages;
    private String groupId;
    private String groupName;
    private DatabaseReference groupRef;
    private DatabaseReference messagesRef;
    private String currentUserId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_group_chat);

        // Get group ID from intent
        groupId = getIntent().getStringExtra("groupId");
        if (groupId == null) {
            Toast.makeText(this, "Group not found", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // Initialize Firebase
        currentUserId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        groupRef = FirebaseDatabase.getInstance().getReference("Groups").child(groupId);
        messagesRef = FirebaseDatabase.getInstance().getReference("GroupMessages");

        // Initialize views
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        groupNameText = findViewById(R.id.group_name_text);
        membersCountText = findViewById(R.id.members_count_text);
        messagesRecyclerView = findViewById(R.id.messages_recycler_view);
        messageInput = findViewById(R.id.message_input);
        sendButton = findViewById(R.id.send_button);

        // Setup RecyclerView
        messages = new ArrayList<>();
        adapter = new GroupMessageAdapter(this, messages);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        layoutManager.setStackFromEnd(true);
        messagesRecyclerView.setLayoutManager(layoutManager);
        messagesRecyclerView.setAdapter(adapter);

        // Load group info
        loadGroupInfo();
        // Load messages
        loadMessages();

        // Setup send button
        sendButton.setOnClickListener(v -> {
            String message = messageInput.getText().toString().trim();
            if (!message.isEmpty()) {
                sendMessage(message);
                messageInput.setText("");
            }
        });
    }

    private void loadGroupInfo() {
        groupRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    groupName = snapshot.child("name").getValue(String.class);
                    groupNameText.setText(groupName);

                    // Count members
                    DataSnapshot membersSnapshot = snapshot.child("members");
                    int memberCount = 0;
                    for (DataSnapshot memberSnapshot : membersSnapshot.getChildren()) {
                        if (memberSnapshot.getValue(Boolean.class)) {
                            memberCount++;
                        }
                    }
                    membersCountText.setText(memberCount + " members");
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(GroupChatActivity.this, "Failed to load group info", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void loadMessages() {
        messagesRef.orderByChild("timestamp")
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        messages.clear();
                        for (DataSnapshot messageSnapshot : snapshot.getChildren()) {
                            GroupMessage message = messageSnapshot.getValue(GroupMessage.class);
                            if (message != null && message.getGroupId().equals(groupId)) {
                                messages.add(message);
                            }
                        }
                        adapter.notifyDataSetChanged();
                        if (!messages.isEmpty()) {
                            messagesRecyclerView.smoothScrollToPosition(messages.size() - 1);
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        Toast.makeText(GroupChatActivity.this, "Failed to load messages", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void sendMessage(String messageText) {
        String messageId = messagesRef.push().getKey();
        if (messageId != null) {
            GroupMessage message = new GroupMessage(
                    messageId,
                    groupId,
                    currentUserId,
                    messageText,
                    System.currentTimeMillis(),
                    "sent"
            );

            messagesRef.child(messageId).setValue(message)
                    .addOnSuccessListener(aVoid -> {
                        // Message sent successfully
                        updateMessageStatus(messageId, "delivered");
                    })
                    .addOnFailureListener(e -> 
                        Toast.makeText(GroupChatActivity.this, "Failed to send message", Toast.LENGTH_SHORT).show()
                    );
        }
    }

    private void updateMessageStatus(String messageId, String status) {
        HashMap<String, Object> updateMap = new HashMap<>();
        updateMap.put("status", status);
        messagesRef.child(messageId).updateChildren(updateMap);
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }
}