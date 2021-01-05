package com.example.sm_project.Adapters;

import android.content.Intent;
import android.os.Build;
import android.text.method.PasswordTransformationMethod;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;
import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.recyclerview.widget.RecyclerView;
import com.example.sm_project.Activities.GroupChatActivity;
import com.example.sm_project.Models.Group;
import com.example.sm_project.R;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class AllGroupsAdapter extends RecyclerView.Adapter<AllGroupsAdapter.ViewHolder> implements Filterable {

    private List<Group> groups;
    private List<Group> searchedGroups;
    private List<Group> filteredList;
    private final List<String> observedGroupsIds;
    private final onGroupClickListener onGroupClickListener;
    private final onGroupLongClickListener onGroupLongClickListener;
    private final onObservedButtonClickListener onObservedButtonClickListener;

    public AllGroupsAdapter(List<Group> allGroups, List<Group> groups, List<Group> filteredList, List<String> observedGroupsIds, onGroupClickListener onGroupClickListener, onGroupLongClickListener onGroupLongClickListener,
                            onObservedButtonClickListener onObservedButtonClickListener) {
        this.searchedGroups = allGroups;
        this.groups = groups;
        this.filteredList = filteredList;
        this.observedGroupsIds = observedGroupsIds;
        this.onGroupClickListener = onGroupClickListener;
        this.onGroupLongClickListener = onGroupLongClickListener;
        this.onObservedButtonClickListener = onObservedButtonClickListener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.single_group_layout, parent, false);
        return new ViewHolder(view, onGroupClickListener, onGroupLongClickListener, onObservedButtonClickListener);
    }

    @Override
    public void onBindViewHolder(@NonNull AllGroupsAdapter.ViewHolder holder, int position) {
        holder.getAddToObservedButton().setChecked(inObserved(position));
        holder.getGroupTitleTextView().setText(searchedGroups.get(position).getTitle());
        holder.getGroupLanguageTextView().setText(searchedGroups.get(position).getLanguage());
    }

    private boolean inObserved(int position){
        return observedGroupsIds.stream().filter(g -> g.equals(searchedGroups.get(position).getId())).collect(Collectors.toList()).size() != 0;
    }

    @Override
    public int getItemCount() {
        return searchedGroups != null ? searchedGroups.size() : 0;
    }

    @Override
    public Filter getFilter() {
        return groupsSearchFilter;
    }

    private final Filter groupsSearchFilter = new Filter() {
        @RequiresApi(api = Build.VERSION_CODES.N)
        @Override
        protected FilterResults performFiltering(CharSequence charSequence) {
            List<Group> newFilteredList = new ArrayList<>();
            if(charSequence == null || charSequence.length() == 0){
                newFilteredList.addAll(filteredList);
            }else{
                String filteredPattern = charSequence.toString().trim().toLowerCase();
                for(Group group : filteredList){
                    if(group.getTitle().toLowerCase().contains(filteredPattern)){
                        newFilteredList.add(group);
                    }
                }
            }

            FilterResults results = new FilterResults();
            results.values = newFilteredList;

            return results;
        }

        @RequiresApi(api = Build.VERSION_CODES.N)
        @Override
        protected void publishResults(CharSequence charSequence, FilterResults filterResults) {
            searchedGroups.clear();
            searchedGroups.addAll((List)filterResults.values);
            searchedGroups.sort(Group.groupTitleComparator);
            notifyDataSetChanged();
        }
    };

    public List<Group> getSearchedGroups() {
        return searchedGroups;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener, View.OnLongClickListener {

        public TextView groupTitleTextView, groupLanguageTextView;
        public ToggleButton addToObservedButton;
        public onGroupClickListener onGroupClickListener;
        public onGroupLongClickListener onGroupLongClickListener;
        public onObservedButtonClickListener onObservedButtonClickListener;

        public ViewHolder(@NonNull View itemView, onGroupClickListener onGroupClickListener, onGroupLongClickListener onGroupLongClickListener,
                          onObservedButtonClickListener onObservedButtonClickListener) {
            super(itemView);

            this.onGroupClickListener = onGroupClickListener;
            this.onGroupLongClickListener = onGroupLongClickListener;
            this.onObservedButtonClickListener = onObservedButtonClickListener;

            groupTitleTextView = itemView.findViewById(R.id.group_title_text_view);
            groupLanguageTextView = itemView.findViewById(R.id.group_language_text_view);
            addToObservedButton = itemView.findViewById(R.id.observed_button);
            addToObservedButton.setOnClickListener(view -> onObservedButtonClickListener.onObservedButtonClick(getAdapterPosition(),
                    addToObservedButton.isChecked()));

            itemView.setOnClickListener(this);
            itemView.setOnLongClickListener(this);
        }

        public TextView getGroupTitleTextView() {
            return groupTitleTextView;
        }

        public TextView getGroupLanguageTextView() {
            return groupLanguageTextView;
        }

        public ToggleButton getAddToObservedButton() {
            return addToObservedButton;
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

    public interface onGroupClickListener {
        void onGroupClick(int position);
    }

    public interface onGroupLongClickListener {
        void onGroupLongClick(int position);
    }

    public interface onObservedButtonClickListener {
        void onObservedButtonClick(int position, boolean status);
    }
}
