package com.example.bitmessenger.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.bitmessenger.R;
import com.example.bitmessenger.models.GroupMessage;
import com.example.bitmessenger.models.User;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class GroupMessageAdapter extends RecyclerView.Adapter<GroupMessageAdapter.MessageViewHolder> {
    private static final int MSG_TYPE_LEFT = 0;
    private static final int MSG_TYPE_RIGHT = 1;

    private Context context;
    private List<GroupMessage> messageList;
    private String currentUserId;
    private DatabaseReference usersRef;

    public GroupMessageAdapter(Context context, List<GroupMessage> messageList) {
        this.context = context;
        this.messageList = messageList;
        this.currentUserId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        this.usersRef = FirebaseDatabase.getInstance().getReference("Users");
    }

    @NonNull
    @Override
    public MessageViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (viewType == MSG_TYPE_RIGHT) {
            View view = LayoutInflater.from(context).inflate(R.layout.item_message_sent, parent, false);
            return new MessageViewHolder(view);
        } else {
            View view = LayoutInflater.from(context).inflate(R.layout.item_message_received, parent, false);
            return new MessageViewHolder(view);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull MessageViewHolder holder, int position) {
        GroupMessage message = messageList.get(position);
        holder.messageText.setText(message.getMessage());

        // Format and set timestamp
        SimpleDateFormat sdf = new SimpleDateFormat("HH:mm", Locale.getDefault());
        String time = sdf.format(new Date(message.getTimestamp()));
        holder.timeText.setText(time);

        // Load sender name for received messages
        if (getItemViewType(position) == MSG_TYPE_LEFT) {
            loadSenderName(message.getSenderId(), holder);
        }

        // Set message status
        if (getItemViewType(position) == MSG_TYPE_RIGHT) {
            switch (message.getStatus()) {
                case "sent":
                    holder.statusText.setText("✓");
                    break;
                case "delivered":
                    holder.statusText.setText("✓✓");
                    break;
                case "read":
                    holder.statusText.setText("✓✓");
                    holder.statusText.setTextColor(context.getResources().getColor(R.color.primary_500));
                    break;
            }
            holder.statusText.setVisibility(View.VISIBLE);
        } else {
            holder.statusText.setVisibility(View.GONE);
        }
    }

    private void loadSenderName(String senderId, MessageViewHolder holder) {
        usersRef.child(senderId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                User user = snapshot.getValue(User.class);
                if (user != null) {
                    holder.senderName.setText(user.getName());
                    holder.senderName.setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                // Handle error
            }
        });
    }

    @Override
    public int getItemCount() {
        return messageList.size();
    }

    @Override
    public int getItemViewType(int position) {
        if (messageList.get(position).getSenderId().equals(currentUserId)) {
            return MSG_TYPE_RIGHT;
        } else {
            return MSG_TYPE_LEFT;
        }
    }

    public static class MessageViewHolder extends RecyclerView.ViewHolder {
        public TextView messageText;
        public TextView timeText;
        public TextView statusText;
        public TextView senderName;

        public MessageViewHolder(@NonNull View itemView) {
            super(itemView);
            messageText = itemView.findViewById(R.id.message_text);
            timeText = itemView.findViewById(R.id.time_text);
            statusText = itemView.findViewById(R.id.status_text);
            senderName = itemView.findViewById(R.id.sender_name);
        }
    }
} 