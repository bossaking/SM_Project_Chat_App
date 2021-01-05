package com.example.sm_project.AlertDialogues;

import android.app.AlertDialog;
import android.content.res.Resources;
import android.graphics.Point;
import android.os.Bundle;
import android.view.*;
import android.widget.Button;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;
import com.example.sm_project.R;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.Objects;

public class SettingsGroupDialog extends DialogFragment {

    Button deleteGroupButton, editGroupButton;

    private final String groupId;
    private final String userId;

    public SettingsGroupDialog(String userId, String groupId) {
        this.userId = userId;
        this.groupId = groupId;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.settings_group_dialog_layout, null);

        deleteGroupButton = view.findViewById(R.id.delete_group_button);
        deleteGroupButton.setOnClickListener(view1 -> deleteGroup());

        editGroupButton = view.findViewById(R.id.edit_group_button);
        editGroupButton.setOnClickListener(view1 -> editGroup());
        
        return view;
    }

    private void editGroup() {
        AddNewGroupDialog addNewGroupDialog = new AddNewGroupDialog(userId, groupId);
        addNewGroupDialog.show(getChildFragmentManager(), null);
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

    @Override
    public void onResume() {
        super.onResume();
        Window window = Objects.requireNonNull(getDialog()).getWindow();

        int width = Resources.getSystem().getDisplayMetrics().widthPixels;

        window.setLayout(width, WindowManager.LayoutParams.WRAP_CONTENT);
        window.setGravity(Gravity.CENTER);
    }
}
