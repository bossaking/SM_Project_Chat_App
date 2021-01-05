package com.example.sm_project.Activities;

import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.util.Patterns;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import androidx.core.content.ContextCompat;
import com.example.sm_project.AlertDialogues.LoadingDialog;
import com.example.sm_project.R;
import com.google.android.material.snackbar.BaseTransientBottomBar;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Objects;

import static com.example.sm_project.Activities.LoginActivity.EMAIL;
import static com.example.sm_project.Activities.LoginActivity.PASSWORD;
import static com.example.sm_project.AlertDialogues.LoadingDialog.LOADING_DIALOG_TAG;

public class RegistrationActivity extends AppCompatActivity {

    private EditText emailEditText, passwordEditText, repeatPasswordEditText, nicknameEditText;

    private Drawable errorIcon;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_registration);

        errorIcon = ContextCompat.getDrawable(this, R.drawable.ic_warning_sign);
        Objects.requireNonNull(errorIcon).setBounds(0, 0, errorIcon.getIntrinsicWidth(), errorIcon.getIntrinsicHeight());

        emailEditText = findViewById(R.id.email_edit_text);
        passwordEditText = findViewById(R.id.password_edit_text);
        repeatPasswordEditText = findViewById(R.id.repeat_password_edit_text);
        nicknameEditText = findViewById(R.id.nickname_edit_text);

        Button signUpButton = findViewById(R.id.sign_up_button);
        signUpButton.setOnClickListener(view -> registerUser());
    }


    private void registerUser() {
        String email = emailEditText.getText().toString().trim();
        String nickname = nicknameEditText.getText().toString().trim();
        String password = passwordEditText.getText().toString().trim();
        String repeatPassword = repeatPasswordEditText.getText().toString().trim();


        if (!CheckEmailAndPasswords(email, nickname, password, repeatPassword))
            return;

        LoadingDialog loadingDialog = new LoadingDialog();
        loadingDialog.show(getSupportFragmentManager(), LOADING_DIALOG_TAG);

        FirebaseAuth.getInstance().createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        DocumentReference reference = FirebaseFirestore.getInstance().collection("Users")
                                .document(Objects.requireNonNull(FirebaseAuth.getInstance().getUid()));
                        HashMap<String, Object> userMap = new HashMap<>();
                        userMap.put("id", FirebaseAuth.getInstance().getUid());
                        userMap.put("nickname", nickname);
                        reference.set(userMap);
                        Toast.makeText(this, "Success", Toast.LENGTH_SHORT).show();
                        Intent outputResult = new Intent();
                        outputResult.putExtra(EMAIL, email);
                        outputResult.putExtra(PASSWORD, password);
                        setResult(RESULT_OK, outputResult);
                        loadingDialog.dismiss();
                        finish();
                    } else {
                        loadingDialog.dismiss();
                        Snackbar.make(findViewById(android.R.id.content), Objects.requireNonNull(Objects.requireNonNull(task.getException()).getLocalizedMessage()), BaseTransientBottomBar.LENGTH_LONG).show();
                    }
                }).addOnFailureListener(e -> {

            loadingDialog.dismiss();
            Snackbar.make(findViewById(android.R.id.content), Objects.requireNonNull(e.getLocalizedMessage()), BaseTransientBottomBar.LENGTH_LONG).show();

        });

    }

    private boolean CheckEmailAndPasswords(String email, String nickname, String password, String repeatPassword) {

        if (email.isEmpty()) {
            emailEditText.setError(getString(R.string.empty_field_error), errorIcon);
            return false;
        }
        if (nickname.isEmpty()) {
            nicknameEditText.setError(getString(R.string.empty_field_error), errorIcon);
            return false;
        }
        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            emailEditText.setError(getString(R.string.not_valid_email_error), errorIcon);
            return false;
        }
        if (password.isEmpty()) {
            passwordEditText.setError(getString(R.string.empty_field_error), errorIcon);
            return false;
        }
        if (!password.equals(repeatPassword)) {
            repeatPasswordEditText.setError(getString(R.string.not_match_password_error), errorIcon);
            return false;
        }
        return true;

    }
}