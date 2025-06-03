package com.example.bitmessenger.activities;

import android.os.Bundle;
import android.util.Log;
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
import com.example.bitmessenger.adapters.MessageAdapter;
import com.example.bitmessenger.models.Message;
import com.example.bitmessenger.models.User;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class MessageActivity extends AppCompatActivity {
    private static final String TAG = "MessageActivity";
    private RecyclerView recyclerView;
    private EditText messageInput;
    private ImageButton sendButton;
    private TextView usernameTextView;

    private MessageAdapter messageAdapter;
    private List<Message> messages;

    private String receiverId;
    private String receiverName;
    private FirebaseUser currentUser;
    private DatabaseReference reference;
    private ValueEventListener userListener;
    private ValueEventListener messagesListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_message);

        // Initialize Firebase
        currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser == null) {
            Toast.makeText(this, "Please login first", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        reference = FirebaseDatabase.getInstance().getReference();

        // Get receiver info from intent
        receiverId = getIntent().getStringExtra("userId");
        receiverName = getIntent().getStringExtra("userName");

        if (receiverId == null) {
            Toast.makeText(this, "User not found", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        Log.d(TAG, "Starting chat with user ID: " + receiverId);

        // Initialize views
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("");
        }
        toolbar.setNavigationOnClickListener(v -> finish());

        recyclerView = findViewById(R.id.messages_recycler_view);
        messageInput = findViewById(R.id.message_input);
        sendButton = findViewById(R.id.send_button);
        usernameTextView = findViewById(R.id.chat_username);

        // Set initial receiver name if available
        if (receiverName != null) {
            usernameTextView.setText(receiverName);
        }

        // Setup RecyclerView
        recyclerView.setHasFixedSize(true);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        layoutManager.setStackFromEnd(true);
        recyclerView.setLayoutManager(layoutManager);

        messages = new ArrayList<>();
        messageAdapter = new MessageAdapter(this, messages);
        recyclerView.setAdapter(messageAdapter);

        // Load receiver info
        loadReceiverInfo();

        // Load messages
        loadMessages();

        // Setup send button
        sendButton.setOnClickListener(v -> {
            String msg = messageInput.getText().toString().trim();
            if (!msg.isEmpty()) {
                sendMessage(msg);
            }
        });
    }

    private void loadReceiverInfo() {
        DatabaseReference userRef = FirebaseDatabase.getInstance().getReference("users").child(receiverId);
        userListener = userRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                try {
                    User user = snapshot.getValue(User.class);
                    if (user != null) {
                        user.setId(snapshot.getKey());
                        if (user.getName() != null) {
                            usernameTextView.setText(user.getName());
                            receiverName = user.getName();
                        } else {
                            usernameTextView.setText("Unknown User");
                        }
                        Log.d(TAG, "Loaded receiver info: " + user.getName());
                    } else {
                        Log.e(TAG, "Failed to load user data: user is null");
                        Toast.makeText(MessageActivity.this, "Failed to load user info", Toast.LENGTH_SHORT).show();
                    }
                } catch (Exception e) {
                    Log.e(TAG, "Error loading user data: " + e.getMessage());
                    Toast.makeText(MessageActivity.this, "Error loading user info", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e(TAG, "Database error: " + error.getMessage());
                Toast.makeText(MessageActivity.this, "Failed to load user info", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void loadMessages() {
        messagesListener = reference.child("chats").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                try {
                    messages.clear();
                    for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                        Message message = snapshot.getValue(Message.class);
                        if (message != null && message.getSenderId() != null && message.getReceiverId() != null && 
                            ((message.getSenderId().equals(currentUser.getUid()) && 
                              message.getReceiverId().equals(receiverId)) ||
                             (message.getSenderId().equals(receiverId) && 
                              message.getReceiverId().equals(currentUser.getUid())))) {
                            messages.add(message);
                        }
                    }
                    messageAdapter.notifyDataSetChanged();
                    if (!messages.isEmpty()) {
                        recyclerView.scrollToPosition(messages.size() - 1);
                    }
                    Log.d(TAG, "Loaded " + messages.size() + " messages");
                } catch (Exception e) {
                    Log.e(TAG, "Error loading messages: " + e.getMessage());
                    Toast.makeText(MessageActivity.this, "Error loading messages", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e(TAG, "Database error: " + error.getMessage());
                Toast.makeText(MessageActivity.this, "Failed to load messages", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void sendMessage(String messageText) {
        if (currentUser == null || receiverId == null) {
            Toast.makeText(this, "Cannot send message at this time", Toast.LENGTH_SHORT).show();
            return;
        }

        DatabaseReference chatRef = reference.child("chats").push();
        String messageId = chatRef.getKey();
        
        Message message = new Message(
            currentUser.getUid(),
            receiverId,
            messageText,
            System.currentTimeMillis()
        );
        if (messageId != null) {
            message.setMessageId(messageId);
        }
        
        chatRef.setValue(message).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                messageInput.setText("");
                updateChatList(messageText);
            } else {
                Toast.makeText(MessageActivity.this, "Failed to send message", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void updateChatList(String lastMessage) {
        if (currentUser == null || receiverId == null) return;

        HashMap<String, Object> chatMap = new HashMap<>();
        chatMap.put("lastMessage", lastMessage);
        chatMap.put("timestamp", System.currentTimeMillis());
        chatMap.put("userName", receiverName != null ? receiverName : "Unknown User");

        // Update for current user
        reference.child("chatList")
                .child(currentUser.getUid())
                .child(receiverId)
                .updateChildren(chatMap);

        // Update for receiver
        reference.child("chatList")
                .child(receiverId)
                .child(currentUser.getUid())
                .updateChildren(chatMap);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (userListener != null) {
            reference.child("users").child(receiverId).removeEventListener(userListener);
        }
        if (messagesListener != null) {
            reference.child("chats").removeEventListener(messagesListener);
        }
    }
}
