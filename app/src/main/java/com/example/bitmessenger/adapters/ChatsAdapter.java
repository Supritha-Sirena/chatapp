package com.example.bitmessenger.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import com.example.bitmessenger.R;
import com.example.bitmessenger.models.Chat;
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
import de.hdodenhof.circleimageview.CircleImageView;

public class ChatsAdapter extends RecyclerView.Adapter<ChatsAdapter.ViewHolder> {
    private Context context;
    private List<Chat> chatList;
    private OnChatClickListener listener;

    public interface OnChatClickListener {
        void onChatClick(String userId);
    }

    public ChatsAdapter(Context context, List<Chat> chatList, OnChatClickListener listener) {
        this.context = context;
        this.chatList = chatList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_chat, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Chat chat = chatList.get(position);

        // Determine the other user in the chat
        String currentUserId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        String otherUserId = chat.getSender().equals(currentUserId) ? chat.getReceiver() : chat.getSender();

        // Load user data
        DatabaseReference userRef = FirebaseDatabase.getInstance().getReference("Users").child(otherUserId);
        userRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                User user = snapshot.getValue(User.class);
                if (user != null) {
                    holder.username.setText(user.getName());
                    holder.lastMessage.setText(chat.getMessage());

                    // Format timestamp
                    SimpleDateFormat sdf = new SimpleDateFormat("HH:mm", Locale.getDefault());
                    String time = sdf.format(new Date(chat.getTimestamp()));
                    holder.timestamp.setText(time);

                    // Load profile image
                    if (user.getImageUrl() != null && !user.getImageUrl().isEmpty()) {
                        Glide.with(context).load(user.getImageUrl()).into(holder.profileImage);
                    } else {
                        holder.profileImage.setImageResource(R.drawable.default_profile);
                    }

                    // Show online status
                    holder.onlineStatus.setVisibility(user.isOnline() ? View.VISIBLE : View.GONE);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                // Handle error
            }
        });

        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onChatClick(otherUserId);
            }
        });
    }

    @Override
    public int getItemCount() {
        return chatList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        public CircleImageView profileImage;
        public TextView username;
        public TextView lastMessage;
        public TextView timestamp;
        public View onlineStatus;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            profileImage = itemView.findViewById(R.id.profile_image);
            username = itemView.findViewById(R.id.username_text);
            lastMessage = itemView.findViewById(R.id.last_message_text);
            timestamp = itemView.findViewById(R.id.time_text);
            onlineStatus = itemView.findViewById(R.id.online_status);
        }
    }
}