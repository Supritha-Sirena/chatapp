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
import com.example.bitmessenger.models.Group;
import java.util.List;
import de.hdodenhof.circleimageview.CircleImageView;

public class GroupsAdapter extends RecyclerView.Adapter<GroupsAdapter.ViewHolder> {
    private Context context;
    private List<Group> groupList;
    private OnGroupClickListener listener;

    public interface OnGroupClickListener {
        void onGroupClick(String groupId);
    }

    public GroupsAdapter(Context context, List<Group> groupList, OnGroupClickListener listener) {
        this.context = context;
        this.groupList = groupList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_group, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Group group = groupList.get(position);
        holder.groupName.setText(group.getName());

        // Load group image if available
        if (group.getImageUrl() != null && !group.getImageUrl().isEmpty()) {
            Glide.with(context).load(group.getImageUrl()).into(holder.groupImage);
        } else {
            holder.groupImage.setImageResource(R.drawable.default_group);
        }

        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onGroupClick(group.getId());
            }
        });
    }

    @Override
    public int getItemCount() {
        return groupList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        public CircleImageView groupImage;
        public TextView groupName;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            groupImage = itemView.findViewById(R.id.group_image);
            groupName = itemView.findViewById(R.id.group_name);
        }
    }
}
