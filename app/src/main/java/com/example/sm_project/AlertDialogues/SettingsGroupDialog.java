package com.example.sm_project.AlertDialogues;

import android.app.AlertDialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;
import com.example.sm_project.R;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

public class SettingsGroupDialog extends DialogFragment {

    Button deleteGroupButton;

    private final String groupId;

    public SettingsGroupDialog(String groupId) {
        this.groupId = groupId;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.settings_group_dialog_layout, null);

        deleteGroupButton = view.findViewById(R.id.delete_group_button);
        deleteGroupButton.setOnClickListener(view1 -> deleteGroup());

        return view;
    }

    private void deleteGroup() {

        AlertDialog deleteGroupDialog = new AlertDialog.Builder(getContext())
                .setTitle(R.string.delete_group)
                .setMessage(getString(R.string.delete_group_question))
                .setIcon(R.drawable.ic_delete)
                .setPositiveButton(R.string.delete, (dialogInterface, i) -> {
                    DocumentReference reference = FirebaseFirestore.getInstance().collection("Groups").document(groupId);
                    reference.delete();
                    dismiss();
                }).setNegativeButton(R.string.cancel, ((dialogInterface, i) ->
                        dismiss()
                )).create();

        deleteGroupDialog.show();


    }
}
