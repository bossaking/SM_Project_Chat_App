package com.example.sm_project.Activities;

import android.content.Intent;
import android.service.autofill.RegexValidator;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import com.example.sm_project.R;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.snackbar.BaseTransientBottomBar;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.FirebaseDatabase;

import static com.example.sm_project.Activities.AllGroupsActivity.USER_ID;

public class LoginActivity extends AppCompatActivity {

    public static final int REGISTRATION_REQUEST_CODE = 0;
    public static final String EMAIL = "Email";
    public static final String PASSWORD = "Password";

    EditText emailEditText, passwordEditText;
    TextView guestLoginTextView, registrationTextView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        checkLoggingIn();

        emailEditText = findViewById(R.id.email_edit_text);
        passwordEditText = findViewById(R.id.password_edit_text);

        guestLoginTextView = findViewById(R.id.guest_login_text_view);
        guestLoginTextView.setOnClickListener(view -> signInLikeGuest());

        registrationTextView = findViewById(R.id.sign_up_text_view);
        registrationTextView.setOnClickListener(view -> startRegistrationActivity());

        Button signInButton = findViewById(R.id.sign_in_button);
        signInButton.setOnClickListener(view -> signInWithEmailAndPassword());
    }

    private void startRegistrationActivity() {
        Intent intent = new Intent(this, RegistrationActivity.class);
        startActivityForResult(intent, REGISTRATION_REQUEST_CODE);
    }

    private void checkLoggingIn(){
        if(FirebaseAuth.getInstance().getCurrentUser() != null) {
            launchGroupsActivityAndFinish();
        }
    }

    private void signInLikeGuest(){
        String userId = FirebaseDatabase.getInstance().getReference("Users").push().getKey();
        launchGroupsActivity(userId);
    }

    private void signInWithEmailAndPassword(){

        String email = emailEditText.getText().toString().trim();
        String password = passwordEditText.getText().toString().trim();

        if(!checkEmailAndPassword(email, password))
            return;

        FirebaseAuth.getInstance().signInWithEmailAndPassword(email, password).addOnSuccessListener(authResult -> {
            launchGroupsActivityAndFinish();
        }).addOnFailureListener(e -> {
            Snackbar.make(findViewById(android.R.id.content), e.getLocalizedMessage(), BaseTransientBottomBar.LENGTH_LONG).show();
        });
    }
    private boolean checkEmailAndPassword(String email, String password){

        if(email.isEmpty()){
            emailEditText.setError(getString(R.string.empty_field_error));
            return false;
        }
        if(!Patterns.EMAIL_ADDRESS.matcher(email).matches()){
            emailEditText.setError(getString(R.string.not_valid_email_error));
            return false;
        }
        if(password.isEmpty()){
            passwordEditText.setError(getString(R.string.empty_field_error));
            return false;
        }
        return true;
    }

    private void launchGroupsActivityAndFinish(){
        String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        Intent intent = new Intent(getApplicationContext(), AllGroupsActivity.class);
        intent.putExtra(USER_ID, userId);
        startActivity(intent);
        finish();
    }

    private void launchGroupsActivity(String userId){
        Intent intent = new Intent(getApplicationContext(), AllGroupsActivity.class);
        intent.putExtra(USER_ID, userId);
        startActivity(intent);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(requestCode == REGISTRATION_REQUEST_CODE && resultCode == RESULT_OK){
            emailEditText.setText(data.getStringExtra(EMAIL));
            passwordEditText.setText(data.getStringExtra(PASSWORD));
        }
    }
}