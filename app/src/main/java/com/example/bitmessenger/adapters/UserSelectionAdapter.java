package com.example.bitmessenger.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.bitmessenger.R;
import com.example.bitmessenger.models.User;
import java.util.ArrayList;
import java.util.List;

public class UserSelectionAdapter extends RecyclerView.Adapter<UserSelectionAdapter.ViewHolder> {
    private Context context;
    private List<User> userList;
    private List<User> selectedUsers;
    private OnUserSelectedListener onUserSelectedListener;

    public interface OnUserSelectedListener {
        void onUserSelected(User user, boolean isSelected);
    }

    public void setOnUserSelectedListener(OnUserSelectedListener listener) {
        this.onUserSelectedListener = listener;
    }

    public UserSelectionAdapter(Context context, List<User> userList) {
        this.context = context;
        this.userList = userList;
        this.selectedUsers = new ArrayList<>();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_user_selection, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        User user = userList.get(position);
        
        holder.userName.setText(user.getName());
        holder.userEmail.setText(user.getEmail());
        holder.checkBox.setChecked(selectedUsers.contains(user));

        holder.itemView.setOnClickListener(v -> {
            holder.checkBox.setChecked(!holder.checkBox.isChecked());
            if (holder.checkBox.isChecked()) {
                selectedUsers.add(user);
            } else {
                selectedUsers.remove(user);
            }
            if (onUserSelectedListener != null) {
                onUserSelectedListener.onUserSelected(user, holder.checkBox.isChecked());
            }
        });

        holder.checkBox.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                if (!selectedUsers.contains(user)) {
                    selectedUsers.add(user);
                }
            } else {
                selectedUsers.remove(user);
            }
            if (onUserSelectedListener != null) {
                onUserSelectedListener.onUserSelected(user, isChecked);
            }
        });
    }

    @Override
    public int getItemCount() {
        return userList.size();
    }

    public List<User> getSelectedUsers() {
        return selectedUsers;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView userName;
        TextView userEmail;
        CheckBox checkBox;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            userName = itemView.findViewById(R.id.user_name);
            userEmail = itemView.findViewById(R.id.user_email);
            checkBox = itemView.findViewById(R.id.user_checkbox);
        }
    }
}
