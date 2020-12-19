package com.example.sm_project.Activities;

import android.content.Intent;
import android.service.autofill.RegexValidator;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import com.example.sm_project.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.FirebaseDatabase;

public class LoginActivity extends AppCompatActivity {

    EditText emailEditText, passwordEditText;
    TextView guestLoginTextView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        checkLoggingIn();

        emailEditText = findViewById(R.id.email_edit_text);
        passwordEditText = findViewById(R.id.password_edit_text);

        guestLoginTextView = findViewById(R.id.guest_login_text_view);
        guestLoginTextView.setOnClickListener(view -> signInLikeGuest());

        Button signInButton = findViewById(R.id.sign_in_button);
        signInButton.setOnClickListener(view -> signInWithEmailAndPassword());
    }

    private void checkLoggingIn(){
        if(FirebaseAuth.getInstance().getCurrentUser() != null) {
            String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
            launchMainActivity(userId);
        }
    }

    private void signInLikeGuest(){
        String userId = FirebaseDatabase.getInstance().getReference("Users").push().getKey();
        launchMainActivity(userId);
    }

    private void signInWithEmailAndPassword(){

        String email = emailEditText.getText().toString().trim();
        String password = passwordEditText.getText().toString().trim();

        if(!checkEmailAndPassword(email, password))
            return;

        String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        launchMainActivity(userId);
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

    private void launchMainActivity(String userId){
        Intent intent = new Intent(getApplicationContext(), MainActivity.class);
        intent.putExtra("USER_ID", userId);
        startActivity(intent);
        finish();
    }
}