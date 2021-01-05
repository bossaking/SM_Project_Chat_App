package com.example.sm_project.AlertDialogues;

import android.content.res.Resources;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.*;
import android.widget.*;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.DialogFragment;
import com.example.sm_project.Models.Group;
import com.example.sm_project.R;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class AddNewGroupDialog extends DialogFragment {

    private EditText groupTitleEditText;
    private Spinner groupLanguageSpinner;
    private Button addGroupButton;
    private TextView newGroupTitle;
    private final String userId;
    private String groupId;

    private boolean editMode = false;

    Drawable errorIcon;

    public AddNewGroupDialog(String userId, String groupId) {

        this.userId = userId;
        if(groupId != null){
            this.groupId = groupId;
            editMode = true;
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.add_new_group_dialog_layout, null);

        errorIcon = ContextCompat.getDrawable(Objects.requireNonNull(getActivity()), R.drawable.ic_warning_sign);
        Objects.requireNonNull(errorIcon).setBounds(0,0, errorIcon.getIntrinsicWidth(), errorIcon.getIntrinsicHeight());

        newGroupTitle = view.findViewById(R.id.new_group_title);

        ArrayAdapter<String> languagesAdapter = new ArrayAdapter<>(getContext(), R.layout.group_languages_spinner,
                getResources().getStringArray(R.array.languages));
        languagesAdapter.setDropDownViewResource(R.layout.group_languages_spinner_dropdown);

        groupTitleEditText = view.findViewById(R.id.group_title_edit_text);
        groupLanguageSpinner = view.findViewById(R.id.group_language_spinner);
        groupLanguageSpinner.setAdapter(languagesAdapter);
        groupLanguageSpinner.setPrompt(getString(R.string.select_group_language));

        addGroupButton = view.findViewById(R.id.add_group_button);
        addGroupButton.setOnClickListener(view1 -> addNewGroup());

        Button cancelButton = view.findViewById(R.id.cancel_button);
        cancelButton.setOnClickListener(view1 -> dismiss());

        if(editMode){
            LoadingDialog loadingDialog = new LoadingDialog();
            loadingDialog.show(getChildFragmentManager(), null);
            DocumentReference groupReference = FirebaseFirestore.getInstance().collection("Groups").document(groupId);
            groupReference.get().addOnSuccessListener(documentSnapshot -> {
                Group group = documentSnapshot.toObject(Group.class);
                newGroupTitle.setText(R.string.edit_group);
                addGroupButton.setText(R.string.edit);
                groupTitleEditText.setText(Objects.requireNonNull(group).getTitle());
                groupLanguageSpinner.setSelection(languagesAdapter.getPosition(group.getLanguage()));
                loadingDialog.dismiss();
            });
        }

        return view;
    }

    private void addNewGroup() {

        String groupTitle = groupTitleEditText.getText().toString().trim();
        if(groupTitle.isEmpty()){
            groupTitleEditText.setError(getString(R.string.empty_field_error), errorIcon);
            return;
        }
        DocumentReference reference;
        if(editMode){
            reference = FirebaseFirestore.getInstance().collection("Groups").document(groupId);
        }else {
            reference = FirebaseFirestore.getInstance().collection("Groups").document();
        }

        Map<String, Object> group = new HashMap<>();
        group.put("id", reference.getId());
        group.put("owner", userId);
        group.put("title", groupTitleEditText.getText().toString().trim());
        group.put("language", groupLanguageSpinner.getSelectedItem().toString().trim());
        group.put("usersCount", 0);

        reference.set(group).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                Toast.makeText(getContext(), "Success", Toast.LENGTH_SHORT).show();
                dismiss();
            } else {
                Toast.makeText(getContext(), Objects.requireNonNull(task.getException()).getLocalizedMessage(), Toast.LENGTH_LONG).show();
            }
        }).addOnFailureListener(e -> Toast.makeText(getContext(), e.getLocalizedMessage(), Toast.LENGTH_LONG).show());
    }

    @Override
    public void onResume() {
        super.onResume();
        Window window = Objects.requireNonNull(getDialog()).getWindow();

        int width = Resources.getSystem().getDisplayMetrics().widthPixels;

        window.setLayout(width, WindowManager.LayoutParams.WRAP_CONTENT);
        window.setGravity(Gravity.CENTER);
    }
}
