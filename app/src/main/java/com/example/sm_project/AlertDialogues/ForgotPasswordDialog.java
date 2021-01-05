package com.example.sm_project.AlertDialogues;

import android.content.res.Resources;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.util.Patterns;
import android.view.*;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.DialogFragment;
import com.example.sm_project.R;
import com.google.firebase.auth.FirebaseAuth;

import java.util.Objects;

public class ForgotPasswordDialog extends DialogFragment {


    EditText emailEditText;
    Button sendEmailButton, cancelButton;

    private Drawable errorIcon;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.forgot_password_dialog_layout, null);

        errorIcon = ContextCompat.getDrawable(Objects.requireNonNull(getActivity()), R.drawable.ic_warning_sign);
        Objects.requireNonNull(errorIcon).setBounds(0,0, errorIcon.getIntrinsicWidth(), errorIcon.getIntrinsicHeight());

        emailEditText = view.findViewById(R.id.email_edit_text);

        sendEmailButton = view.findViewById(R.id.send_reset_email_button);
        sendEmailButton.setOnClickListener(view1 -> resetPassword());

        cancelButton = view.findViewById(R.id.cancel_button);
        cancelButton.setOnClickListener(view1 -> dismiss());

        return view;
    }

    private void resetPassword() {
        String email = emailEditText.getText().toString().trim();
        if(!checkEmail(email))
            return;

        FirebaseAuth.getInstance().sendPasswordResetEmail(email).addOnSuccessListener(unused -> {
            Toast.makeText(getContext(), getString(R.string.send_reset_password_email_toast), Toast.LENGTH_LONG).show();
            dismiss();
        }).addOnFailureListener(e -> Toast.makeText(getContext(), e.getLocalizedMessage(), Toast.LENGTH_LONG).show());
    }

    private boolean checkEmail(String email){
        if(email.isEmpty()){
            emailEditText.setError(getString(R.string.empty_field_error), errorIcon);
            return false;
        }
        if(!Patterns.EMAIL_ADDRESS.matcher(email).matches()){
            emailEditText.setError(getString(R.string.not_valid_email_error), errorIcon);
            return false;
        }
        return true;
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
