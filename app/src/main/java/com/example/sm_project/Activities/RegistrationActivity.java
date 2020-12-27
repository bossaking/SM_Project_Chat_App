package com.example.sm_project.Activities;

import android.content.Intent;
import android.util.Patterns;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import com.example.sm_project.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.snackbar.BaseTransientBottomBar;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;

import static com.example.sm_project.Activities.LoginActivity.EMAIL;
import static com.example.sm_project.Activities.LoginActivity.PASSWORD;

public class RegistrationActivity extends AppCompatActivity {

    private EditText emailEditText, passwordEditText, repeatPasswordEditText, nicknameEditText;
    private Button signUpButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_registration);

        emailEditText = findViewById(R.id.email_edit_text);
        passwordEditText = findViewById(R.id.password_edit_text);
        repeatPasswordEditText = findViewById(R.id.repeat_password_edit_text);
        nicknameEditText = findViewById(R.id.nickname_edit_text);

        signUpButton = findViewById(R.id.sign_up_button);
        signUpButton.setOnClickListener(view -> registerUser());
    }


    private void registerUser() {
        String email = emailEditText.getText().toString().trim();
        String nickname = nicknameEditText.getText().toString().trim();
        String password = passwordEditText.getText().toString().trim();
        String repeatPassword = repeatPasswordEditText.getText().toString().trim();


        if (!CheckEmailAndPasswords(email, nickname, password, repeatPassword))
            return;

        FirebaseAuth.getInstance().createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        CollectionReference reference = FirebaseFirestore.getInstance().collection("Users");
                        HashMap<String, Object> userMap = new HashMap<>();
                        userMap.put("id", FirebaseAuth.getInstance().getUid());
                        userMap.put("nickname", nickname);
                        reference.add(userMap);
                        Toast.makeText(this, "Success", Toast.LENGTH_SHORT).show();
                        Intent outputResult = new Intent();
                        outputResult.putExtra(EMAIL, email);
                        outputResult.putExtra(PASSWORD, password);
                        setResult(RESULT_OK, outputResult);
                        finish();
                    } else {
                        Snackbar.make(findViewById(android.R.id.content), task.getException().getLocalizedMessage(), BaseTransientBottomBar.LENGTH_LONG).show();
                    }
                }).addOnFailureListener(e -> {

            Snackbar.make(findViewById(android.R.id.content), e.getLocalizedMessage(), BaseTransientBottomBar.LENGTH_LONG).show();

        });

    }

    private boolean CheckEmailAndPasswords(String email, String nickname, String password, String repeatPassword) {

        if (email.isEmpty()) {
            emailEditText.setError(getString(R.string.empty_field_error));
            return false;
        }
        if(nickname.isEmpty()){
            nicknameEditText.setError(getString(R.string.empty_field_error));
            return false;
        }
        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            emailEditText.setError(getString(R.string.not_valid_email_error));
            return false;
        }
        if (password.isEmpty()) {
            passwordEditText.setError(getString(R.string.empty_field_error));
            return false;
        }
        if (!password.equals(repeatPassword)) {
            repeatPasswordEditText.setError(getString(R.string.not_match_password_error));
            return false;
        }
        return true;

    }
}