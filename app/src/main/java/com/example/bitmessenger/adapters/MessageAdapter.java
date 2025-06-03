package com.example.bitmessenger.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.bitmessenger.R;
import com.example.bitmessenger.models.Message;
import com.google.firebase.auth.FirebaseAuth;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class MessageAdapter extends RecyclerView.Adapter<MessageAdapter.MessageViewHolder> {
    private Context context;
    private List<Message> messages;
    private String currentUserId;

    public MessageAdapter(Context context, List<Message> messages) {
        this.context = context;
        this.messages = messages;
        this.currentUserId = FirebaseAuth.getInstance().getCurrentUser().getUid();
    }

    @NonNull
    @Override
    public MessageViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_message, parent, false);
        return new MessageViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MessageViewHolder holder, int position) {
        Message message = messages.get(position);
        
        if (message.getSenderId().equals(currentUserId)) {
            // Show sent message
            holder.sentLayout.setVisibility(View.VISIBLE);
            holder.receivedLayout.setVisibility(View.GONE);
            holder.sentMessageText.setText(message.getMessage());
            holder.sentMessageTime.setText(formatTime(message.getTimestamp()));
        } else {
            // Show received message
            holder.receivedLayout.setVisibility(View.VISIBLE);
            holder.sentLayout.setVisibility(View.GONE);
            holder.receivedMessageText.setText(message.getMessage());
            holder.receivedMessageTime.setText(formatTime(message.getTimestamp()));
        }
    }

    @Override
    public int getItemCount() {
        return messages.size();
    }

    private String formatTime(long timestamp) {
        SimpleDateFormat sdf = new SimpleDateFormat("HH:mm", Locale.getDefault());
        return sdf.format(new Date(timestamp));
    }

    public void addMessage(Message message) {
        messages.add(message);
        notifyItemInserted(messages.size() - 1);
    }

    static class MessageViewHolder extends RecyclerView.ViewHolder {
        LinearLayout sentLayout, receivedLayout;
        TextView sentMessageText, sentMessageTime;
        TextView receivedMessageText, receivedMessageTime;

        MessageViewHolder(View itemView) {
            super(itemView);
            sentLayout = itemView.findViewById(R.id.sent_message_layout);
            receivedLayout = itemView.findViewById(R.id.received_message_layout);
            sentMessageText = itemView.findViewById(R.id.sent_message_text);
            sentMessageTime = itemView.findViewById(R.id.sent_message_time);
            receivedMessageText = itemView.findViewById(R.id.received_message_text);
            receivedMessageTime = itemView.findViewById(R.id.received_message_time);
        }
    }
}
