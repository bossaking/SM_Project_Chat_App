package com.example.sm_project.Activities;


import android.content.Intent;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.sm_project.Adapters.AllGroupsAdapter;
import com.example.sm_project.AlertDialogues.AddNewGroupDialog;
import com.example.sm_project.AlertDialogues.SettingsGroupDialog;
import com.example.sm_project.Models.Group;
import com.example.sm_project.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.firestore.*;

import java.util.ArrayList;
import java.util.List;

import static com.example.sm_project.Activities.GroupChatActivity.CHAT_ID;

public class AllGroupsActivity extends AppCompatActivity implements AllGroupsAdapter.onGroupClickListener, AllGroupsAdapter.onGroupLongClickListener {

    private static final String ADD_NEW_GROUP_TAG = "Add new group";
    private static final String SETTINGS_GROUP_TAG = "Group settings";
    public static final String USER_ID = "User id";

    private RecyclerView allGroupsRecyclerView;
    private AllGroupsAdapter allGroupsAdapter;

    private List<Group> groups;

    private String userId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_all_groups);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        userId = getIntent().getStringExtra(USER_ID);

        groups = new ArrayList<>();

        allGroupsRecyclerView = findViewById(R.id.all_groups_recycler_view);
        allGroupsAdapter = new AllGroupsAdapter(groups, this, this);
        allGroupsRecyclerView.setAdapter(allGroupsAdapter);
        allGroupsRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        getAllGroupsFromFirebase();
    }

    private void getAllGroupsFromFirebase(){
        CollectionReference reference = FirebaseFirestore.getInstance().collection("Groups");
        reference.addSnapshotListener((value, error) -> {
            for(DocumentChange dc : value.getDocumentChanges()){
                switch (dc.getType()){
                    case ADDED:
                        Group group = dc.getDocument().toObject(Group.class);
                        groups.add(dc.getNewIndex(), group);
                        allGroupsAdapter.notifyDataSetChanged();
                        break;
                    case REMOVED:
                        groups.remove(dc.getOldIndex());
                        allGroupsAdapter.notifyDataSetChanged();
                        break;
                }
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.all_groups_toolbar_menu, menu);

        if(loggedIn()){
            menu.findItem(R.id.add_new_group_button).setVisible(true);
            menu.findItem(R.id.user_profile_button).setVisible(true);
        }

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {

        switch (item.getItemId()){
            case R.id.add_new_group_button:
                AddNewGroupDialog dialog = new AddNewGroupDialog(userId);
                dialog.show(getSupportFragmentManager(), ADD_NEW_GROUP_TAG);
                break;
        }

        return true;
    }

    @Override
    public void onGroupClick(int position) {
        Intent intent = new Intent(this, GroupChatActivity.class);
        intent.putExtra(CHAT_ID, groups.get(position).getId());
        intent.putExtra(USER_ID, userId);
        startActivity(intent);
    }

    @Override
    public void onGroupLongClick(int position) {
        if(loggedIn() && groups.get(position).getOwner().equals(FirebaseAuth.getInstance().getUid())) {
            SettingsGroupDialog settingsGroupDialog = new SettingsGroupDialog(groups.get(position).getId());
            settingsGroupDialog.show(getSupportFragmentManager(), SETTINGS_GROUP_TAG);
        }
    }

    private boolean loggedIn(){
        return FirebaseAuth.getInstance().getCurrentUser() != null;
    }
}