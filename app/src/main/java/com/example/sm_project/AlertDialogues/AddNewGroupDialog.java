package com.example.sm_project.AlertDialogues;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;
import com.example.sm_project.R;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class AddNewGroupDialog extends DialogFragment {

    private EditText groupTitleEditText;
    private Spinner groupLanguageSpinner;
    private Button addGroupButton, cancelButton;
    private final String userId;

    String[] languages = {"English", "Polski", "Русский"};

    public AddNewGroupDialog(String userId) {
        this.userId = userId;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.add_new_group_dialog_layout, null);

        ArrayAdapter<String> languagesAdapter = new ArrayAdapter<>(getContext(), R.layout.group_languages_spinner,
                getResources().getStringArray(R.array.languages));
        languagesAdapter.setDropDownViewResource(R.layout.group_languages_spinner_dropdown);

        groupTitleEditText = view.findViewById(R.id.group_title_edit_text);
        groupLanguageSpinner = view.findViewById(R.id.group_language_spinner);
        groupLanguageSpinner.setAdapter(languagesAdapter);
        groupLanguageSpinner.setPrompt(getString(R.string.select_group_language));

        addGroupButton = view.findViewById(R.id.add_group_button);
        addGroupButton.setOnClickListener(view1 -> addNewGroup());

        cancelButton = view.findViewById(R.id.cancel_button);
        cancelButton.setOnClickListener(view1 -> dismiss());

        return view;
    }

    private void addNewGroup() {
        DocumentReference reference = FirebaseFirestore.getInstance().collection("Groups").document();
        Map<String, Object> group = new HashMap<>();
        group.put("id", reference.getId());
        group.put("owner", userId);
        group.put("title", groupTitleEditText.getText().toString().trim());
        group.put("language", groupLanguageSpinner.getSelectedItem().toString().trim());

        reference.set(group).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                Toast.makeText(getContext(), "Success", Toast.LENGTH_SHORT).show();
                dismiss();
            } else {
                Toast.makeText(getContext(), task.getException().getLocalizedMessage(), Toast.LENGTH_LONG).show();
            }
        }).addOnFailureListener(e -> {
            Toast.makeText(getContext(), e.getLocalizedMessage(), Toast.LENGTH_LONG).show();
        });
    }
}
