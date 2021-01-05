package com.example.sm_project.Activities;

import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.SearchView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import androidx.appcompat.view.menu.MenuBuilder;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import com.bumptech.glide.request.target.SimpleTarget;
import com.bumptech.glide.request.transition.Transition;
import com.example.sm_project.Adapters.AllGroupsAdapter;
import com.example.sm_project.AlertDialogues.AddNewGroupDialog;
import com.example.sm_project.AlertDialogues.FiltersDialog;
import com.example.sm_project.AlertDialogues.LoadingDialog;
import com.example.sm_project.AlertDialogues.SettingsGroupDialog;
import com.example.sm_project.Models.Group;
import com.example.sm_project.R;
import com.google.android.material.snackbar.BaseTransientBottomBar;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.*;
import java.util.*;
import java.util.stream.Collectors;

import static com.example.sm_project.Activities.GroupChatActivity.CHAT_ID;
import static com.example.sm_project.AlertDialogues.LoadingDialog.LOADING_DIALOG_TAG;

public class AllGroupsActivity extends AppCompatActivity implements AllGroupsAdapter.onGroupClickListener, AllGroupsAdapter.onGroupLongClickListener,
        AllGroupsAdapter.onObservedButtonClickListener, FiltersDialog.filtersDialogListener {

    public static final int CHAT_RC = 3;

    private static final String ADD_NEW_GROUP_TAG = "Add new group";
    private static final String SETTINGS_GROUP_TAG = "Group settings";
    private static final String FILTERS_TAG = "Filters";
    public static final String USER_ID = "User id";
    public static final String USER_NICKNAME = "User nickname";

    private RecyclerView allGroupsRecyclerView;
    private AllGroupsAdapter allGroupsAdapter;

    private List<Group> groups;
    private List<Group> allGroups;
    private List<Group> filteredList;

    private List<String> observedGroupsIds;

    private String userId;

    boolean mObservedFirst;
    List<String> mLanguages;

    private LoadingDialog loadingDialog;

    @Override
    protected void onResume() {
        super.onResume();

        try {

            Glide
                    .with(getApplicationContext())
                    .asBitmap()
                    .load(UserProfileActivity.backgroundImageUri)
                    .into(new SimpleTarget<Bitmap>(Resources.getSystem().getDisplayMetrics().widthPixels,
                            Resources.getSystem().getDisplayMetrics().heightPixels) {
                        @Override
                        public void onResourceReady(@NonNull Bitmap resource, @Nullable Transition<? super Bitmap> transition) {
                            Drawable dr = new BitmapDrawable(getResources(), resource);
                            getWindow().getDecorView().setBackground(dr);
                        }
                    });

        } catch (Exception e) {
            Toast.makeText(this, e.getLocalizedMessage(), Toast.LENGTH_LONG).show();
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_all_groups);



        loadingDialog = new LoadingDialog();
        loadingDialog.show(getSupportFragmentManager(), LOADING_DIALOG_TAG);

        Toolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setOverflowIcon(getDrawable(R.drawable.ic_more));
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(false);

        userId = getIntent().getStringExtra(USER_ID);

        groups = new ArrayList<>();
        allGroups = new ArrayList<>();
        filteredList = new ArrayList<>();
        observedGroupsIds = new ArrayList<>();
        mLanguages = new ArrayList<>();
        mObservedFirst = false;

        allGroupsRecyclerView = findViewById(R.id.all_groups_recycler_view);

        DocumentReference userReference = FirebaseFirestore.getInstance().collection("Users").document(userId);
        userReference.get().addOnSuccessListener(documentSnapshot -> {
            try {
                mObservedFirst = documentSnapshot.getBoolean("observedFirst");
            }catch (NullPointerException e){
                mObservedFirst = false;
            }


            CollectionReference languages = userReference.collection("languages");
            languages.get().addOnSuccessListener(queryDocumentSnapshots -> {
                for (DocumentSnapshot ds : queryDocumentSnapshots) {
                    mLanguages.add(ds.getString("language"));
                }

                if(mLanguages.isEmpty()){
                    String actualSystemLanguage = Locale.getDefault().getDisplayLanguage().substring(0,1).toUpperCase() +
                            Locale.getDefault().getDisplayLanguage().substring(1);
                    List<String> allLanguages = new ArrayList<String>(Arrays.asList(getResources().getStringArray(R.array.languages)));
                    if(allLanguages.contains(actualSystemLanguage)) {
                        mLanguages.add(actualSystemLanguage);
                    }else{
                        mLanguages.add("English");
                    }
                }

                CollectionReference reference = FirebaseFirestore.getInstance().collection("Users").document(userId)
                        .collection("observedGroups");
                reference.get().addOnSuccessListener(groupsQueryDocumentSnapshots -> {

                    for (DocumentSnapshot ds : groupsQueryDocumentSnapshots) {
                        observedGroupsIds.add(ds.getId());
                    }

                    allGroupsAdapter = new AllGroupsAdapter(allGroups, groups, filteredList, observedGroupsIds,
                            this, this, this);
                    allGroupsRecyclerView.setAdapter(allGroupsAdapter);
                    allGroupsRecyclerView.setLayoutManager(new LinearLayoutManager(this));
                    getAllGroupsFromFirebase();

                }).addOnFailureListener(e -> {
                    Toast.makeText(this, e.getLocalizedMessage(), Toast.LENGTH_LONG).show();
                });

            });

        });
    }

    private void getAllGroupsFromFirebase() {
        CollectionReference reference = FirebaseFirestore.getInstance().collection("Groups");
        reference.addSnapshotListener((value, error) -> {
            for (DocumentChange dc : value.getDocumentChanges()) {
                switch (dc.getType()) {
                    case ADDED:
                        Group group = dc.getDocument().toObject(Group.class);
                        groups.add(group);
                        allGroups.add(group);
                        allGroups.sort(Group.groupTitleComparator);
                        allGroupsAdapter.notifyDataSetChanged();
                        break;
                    case MODIFIED:
                        group = groups.stream().filter(g -> g.getId().equals(dc.getDocument().toObject(Group.class).getId())).collect(
                                Collectors.toList()
                        ).get(0);
                        Group editedGroup = dc.getDocument().toObject(Group.class);
                        groups.set(groups.indexOf(group), editedGroup);
                        allGroups.set(allGroups.indexOf(group), editedGroup);
                        allGroups.sort(Group.groupTitleComparator);
                        allGroupsAdapter.notifyDataSetChanged();
                        break;
                    case REMOVED:
                        group = groups.stream().filter(g -> g.getId().equals(dc.getDocument().toObject(Group.class).getId())).collect(
                                Collectors.toList()
                        ).get(0);
                        groups.remove(group);
                        allGroups.remove(group);
                        allGroupsAdapter.notifyDataSetChanged();
                        break;
                }


            }
            applyFilters(mObservedFirst, mLanguages);

        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.all_groups_toolbar_menu, menu);

        ((MenuBuilder) menu).setOptionalIconsVisible(true);

        SearchView searchView = (SearchView) menu.findItem(R.id.search_button).getActionView();
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String s) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String s) {
                allGroupsAdapter.getFilter().filter(s);
                applyFilters(mObservedFirst, mLanguages);
                return false;
            }
        });

        if (loggedIn()) {
            menu.findItem(R.id.add_new_group_button).setVisible(true);
            menu.findItem(R.id.user_profile_button).setVisible(true);
        }

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {

        switch (item.getItemId()) {
            case R.id.add_new_group_button:
                AddNewGroupDialog dialog = new AddNewGroupDialog(userId, null);
                dialog.show(getSupportFragmentManager(), ADD_NEW_GROUP_TAG);
                break;
            case R.id.user_profile_button:
                Intent intent = new Intent(this, UserProfileActivity.class);
                intent.putExtra(USER_ID, userId);
                FirebaseFirestore.getInstance().collection("Users").document(userId).get().addOnSuccessListener(documentSnapshot -> {
                    intent.putExtra(USER_NICKNAME, documentSnapshot.getString("nickname"));
                    startActivity(intent);
                });
                break;
            case R.id.filters_groups_button:
                FiltersDialog filtersDialog = new FiltersDialog(mObservedFirst, mLanguages);
                filtersDialog.show(getSupportFragmentManager(), FILTERS_TAG);
                break;
        }

        return true;
    }

    @Override
    public void onGroupClick(int position) {
        Intent intent = new Intent(this, GroupChatActivity.class);
        intent.putExtra(CHAT_ID, allGroups.get(position).getId());
        intent.putExtra(USER_ID, userId);
        startActivity(intent);
    }

    @Override
    public void onGroupLongClick(int position) {
        if (loggedIn() && allGroups.get(position).getOwner().equals(FirebaseAuth.getInstance().getUid())) {
            SettingsGroupDialog settingsGroupDialog = new SettingsGroupDialog(userId, allGroups.get(position).getId());
            settingsGroupDialog.show(getSupportFragmentManager(), SETTINGS_GROUP_TAG);
        }
    }

    @Override
    public void onObservedButtonClick(int position, boolean status) {
        if (status) {
            DocumentReference userReference = FirebaseFirestore.getInstance().collection("Users").document(userId);
            HashMap<String, Object> groupMap = new HashMap<>();
            groupMap.put("groupId", allGroups.get(position).getId());
            userReference.collection("observedGroups").document(allGroups.get(position).getId()).set(groupMap);
            observedGroupsIds.add(allGroups.get(position).getId());
            Snackbar.make(findViewById(android.R.id.content), getString(R.string.added_to_observed), BaseTransientBottomBar.LENGTH_SHORT).show();
        } else {
            FirebaseFirestore.getInstance().collection("Users").document(userId).collection("observedGroups")
                    .document(allGroups.get(position).getId()).delete();
            observedGroupsIds.remove(allGroups.get(position).getId());
            Snackbar.make(findViewById(android.R.id.content), getString(R.string.removed_from_observed), BaseTransientBottomBar.LENGTH_SHORT).show();
        }

        if(mObservedFirst){
            observesFirst();
        }
    }

    private boolean loggedIn() {
        return FirebaseAuth.getInstance().getCurrentUser() != null;
    }

    @Override
    public void applyFilters(boolean observedFirst, List<String> languages) {

        DocumentReference userReference = FirebaseFirestore.getInstance().collection("Users").document(userId);
        HashMap<String, Object> observedFirstMap = new HashMap<>();
        observedFirstMap.put("observedFirst", observedFirst);
        userReference.update(observedFirstMap);

        //For languages filter
        mLanguages = languages;
        allGroups.clear();
        for (Group group : groups) {
            if (languages.contains(group.getLanguage())) {
                allGroups.add(group);
            }
        }
        allGroups.sort(Group.groupTitleComparator);

        mObservedFirst = observedFirst;
        if (observedFirst) {
            observesFirst();
        }
        filteredList.clear();
        filteredList.addAll(allGroups);
        allGroupsAdapter.notifyDataSetChanged();

        userReference.collection("languages").get().addOnSuccessListener(queryDocumentSnapshots -> {
            for (DocumentSnapshot ds : queryDocumentSnapshots) {
                userReference.collection("languages").document(ds.getId()).delete();
            }
            for (String language : languages) {
                HashMap<String, Object> languageMap = new HashMap<>();
                languageMap.put("language", language);
                userReference.collection("languages").add(languageMap);
            }

            if(loadingDialog.isVisible())
            loadingDialog.dismiss();

        });

    }

    private void observesFirst(){
        List<Group> mAllGroups = new ArrayList<>();
        mAllGroups.addAll(allGroups);
        allGroups.clear();
        List<Group> observedGroups = new ArrayList<>();
        List<Group> otherGroups = new ArrayList<>();
        for(Group group : mAllGroups){
            if(observedGroupsIds.contains(group.getId())){
                observedGroups.add(group);
            }else{
                otherGroups.add(group);
            }
        }
        observedGroups.sort(Group.groupTitleComparator);
        otherGroups.sort(Group.groupTitleComparator);
        allGroups.addAll(observedGroups);
        allGroups.addAll(otherGroups);
        allGroupsAdapter.notifyDataSetChanged();
    }
}