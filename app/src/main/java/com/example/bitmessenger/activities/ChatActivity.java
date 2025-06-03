package com.example.bitmessenger.activities;

import android.os.Bundle;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

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

public class ChatActivity extends AppCompatActivity {
    private RecyclerView recyclerView;
    private EditText messageInput;
    private ImageButton sendButton;
    private TextView chatUsername;
    private TextView onlineStatus;

    private MessageAdapter messageAdapter;
    private List<Message> messages;

    private FirebaseUser currentUser;
    private DatabaseReference reference;
    private String chatUserId;
    private ValueEventListener messagesListener;
    private ValueEventListener userStatusListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        // Initialize Firebase
        currentUser = FirebaseAuth.getInstance().getCurrentUser();
        reference = FirebaseDatabase.getInstance().getReference();

        // Get chat user ID from intent
        chatUserId = getIntent().getStringExtra("user_id");
        if (chatUserId == null) {
            Toast.makeText(this, "User not found", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // Initialize views
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        toolbar.setNavigationOnClickListener(v -> finish());

        recyclerView = findViewById(R.id.messages_recycler_view);
        messageInput = findViewById(R.id.message_input);
        sendButton = findViewById(R.id.send_button);
        chatUsername = findViewById(R.id.chat_username);
        onlineStatus = findViewById(R.id.online_status);

        // Setup RecyclerView
        recyclerView.setHasFixedSize(true);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        layoutManager.setStackFromEnd(true);
        recyclerView.setLayoutManager(layoutManager);

        messages = new ArrayList<>();
        messageAdapter = new MessageAdapter(this, messages);
        recyclerView.setAdapter(messageAdapter);

        // Load user info
        loadUserInfo();

        // Load messages
        loadMessages();

        // Send message
        sendButton.setOnClickListener(v -> {
            String msg = messageInput.getText().toString().trim();
            if (!msg.isEmpty()) {
                sendMessage(msg);
            }
        });
    }

    private void loadUserInfo() {
        reference.child("users").child(chatUserId).addValueEventListener(
            userStatusListener = new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    User user = dataSnapshot.getValue(User.class);
                    if (user != null) {
                        chatUsername.setText(user.getName());
                        onlineStatus.setText(user.isOnline() ? "Online" : "Offline");
                    }
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {
                }
            });
    }

    private void loadMessages() {
        reference.child("chats")
                .addValueEventListener(messagesListener = new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        messages.clear();
                        for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                            Message message = snapshot.getValue(Message.class);
                            if (message != null && 
                                ((message.getSenderId().equals(currentUser.getUid()) && 
                                  message.getReceiverId().equals(chatUserId)) ||
                                 (message.getSenderId().equals(chatUserId) && 
                                  message.getReceiverId().equals(currentUser.getUid())))) {
                                messages.add(message);
                            }
                        }
                        messageAdapter.notifyDataSetChanged();
                        recyclerView.scrollToPosition(messages.size() - 1);
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {
                    }
                });
    }

    private void sendMessage(String message) {
        DatabaseReference chatRef = reference.child("chats").push();
        
        Message chatMessage = new Message(
            currentUser.getUid(),
            chatUserId,
            message,
            System.currentTimeMillis()
        );
        chatMessage.setMessageId(chatRef.getKey());
        
        chatRef.setValue(chatMessage).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                messageInput.setText("");
            } else {
                Toast.makeText(ChatActivity.this, "Failed to send message", Toast.LENGTH_SHORT).show();
            }
        });

        // Update latest message in chat list
        updateChatList(message);
    }

    private void updateChatList(String lastMessage) {
        HashMap<String, Object> chatMap = new HashMap<>();
        chatMap.put("lastMessage", lastMessage);
        chatMap.put("timestamp", System.currentTimeMillis());

        // Update for current user
        reference.child("chatList")
                .child(currentUser.getUid())
                .child(chatUserId)
                .updateChildren(chatMap);

        // Update for chat user
        reference.child("chatList")
                .child(chatUserId)
                .child(currentUser.getUid())
                .updateChildren(chatMap);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (messagesListener != null) {
            reference.child("chats").removeEventListener(messagesListener);
        }
        if (userStatusListener != null) {
            reference.child("users").child(chatUserId).removeEventListener(userStatusListener);
        }
    }
} 