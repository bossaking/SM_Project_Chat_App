package com.example.sm_project.Adapters;

import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ExpandableListView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.sm_project.Activities.GroupChatActivity;
import com.example.sm_project.Models.Group;
import com.example.sm_project.R;

import java.util.List;

public class AllGroupsAdapter extends RecyclerView.Adapter<AllGroupsAdapter.ViewHolder> {

    private final List<Group> groups;
    private onGroupClickListener onGroupClickListener;
    private onGroupLongClickListener onGroupLongClickListener;

    public AllGroupsAdapter(List<Group> groups, onGroupClickListener onGroupClickListener, onGroupLongClickListener onGroupLongClickListener){
        this.groups = groups;
        this.onGroupClickListener = onGroupClickListener;
        this.onGroupLongClickListener = onGroupLongClickListener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.single_group_layout, parent, false);
        return new ViewHolder(view, onGroupClickListener, onGroupLongClickListener);
    }

    @Override
    public void onBindViewHolder(@NonNull AllGroupsAdapter.ViewHolder holder, int position) {
        holder.getGroupTitleTextView().setText(groups.get(position).getTitle());
        holder.getGroupLanguageTextView().setText(groups.get(position).getLanguage());
    }

    @Override
    public int getItemCount() {
        return groups != null ? groups.size() : 0;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener, View.OnLongClickListener {

        public TextView groupTitleTextView, groupLanguageTextView;
        public onGroupClickListener onGroupClickListener;
        public onGroupLongClickListener onGroupLongClickListener;

        public ViewHolder(@NonNull View itemView, onGroupClickListener onGroupClickListener, onGroupLongClickListener onGroupLongClickListener) {
            super(itemView);

            groupTitleTextView = itemView.findViewById(R.id.group_title_text_view);
            groupLanguageTextView = itemView.findViewById(R.id.group_language_text_view);
            this.onGroupClickListener = onGroupClickListener;
            this.onGroupLongClickListener = onGroupLongClickListener;

            itemView.setOnClickListener(this);
            itemView.setOnLongClickListener(this);
        }

        public TextView getGroupTitleTextView() {
            return groupTitleTextView;
        }

        public TextView getGroupLanguageTextView() {
            return groupLanguageTextView;
        }

        @Override
        public void onClick(View view) {
            onGroupClickListener.onGroupClick(getAdapterPosition());
        }

        @Override
        public boolean onLongClick(View view) {
            onGroupLongClickListener.onGroupLongClick(getAdapterPosition());
            return true;
        }
    }

    public interface onGroupClickListener{
        void onGroupClick(int position);
    }

    public interface onGroupLongClickListener{
        void onGroupLongClick(int position);
    }
}
