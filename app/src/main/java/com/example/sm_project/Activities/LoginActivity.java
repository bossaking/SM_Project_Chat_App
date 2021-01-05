package com.example.sm_project.Activities;

import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.util.Patterns;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import androidx.cardview.widget.CardView;
import androidx.core.content.ContextCompat;
import com.example.sm_project.AlertDialogues.ForgotPasswordDialog;
import com.example.sm_project.AlertDialogues.LoadingDialog;
import com.example.sm_project.R;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.Task;
import com.google.android.material.snackbar.BaseTransientBottomBar;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.*;
import com.google.firebase.firestore.*;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.HashMap;
import java.util.Objects;

import static com.example.sm_project.Activities.AllGroupsActivity.USER_ID;
import static com.example.sm_project.AlertDialogues.LoadingDialog.LOADING_DIALOG_TAG;

public class LoginActivity extends AppCompatActivity {

    public static final int REGISTRATION_REQUEST_CODE = 0;
    private static final int RC_SIGN_IN = 1;
    public static final String EMAIL = "Email";
    public static final String PASSWORD = "Password";

    EditText emailEditText, passwordEditText;
    TextView forgotPasswordTextView, registrationTextView;
    CardView signInWithGoogleCardView;

    private Drawable errorIcon;

    LoadingDialog loadingDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        loadingDialog = new LoadingDialog();
        loadingDialog.show(getSupportFragmentManager(), LOADING_DIALOG_TAG);
        checkLoggingIn();

        errorIcon = ContextCompat.getDrawable(this, R.drawable.ic_warning_sign);
        Objects.requireNonNull(errorIcon).setBounds(0, 0, errorIcon.getIntrinsicWidth(), errorIcon.getIntrinsicHeight());

        emailEditText = findViewById(R.id.email_edit_text);
        passwordEditText = findViewById(R.id.password_edit_text);

        forgotPasswordTextView = findViewById(R.id.forgot_password_text_view);
        forgotPasswordTextView.setOnClickListener(view -> {
            ForgotPasswordDialog forgotPasswordDialog = new ForgotPasswordDialog();
            forgotPasswordDialog.show(getSupportFragmentManager(), null);
        });

        registrationTextView = findViewById(R.id.sign_up_text_view);
        registrationTextView.setOnClickListener(view -> startRegistrationActivity());

        Button signInButton = findViewById(R.id.sign_in_button);
        signInButton.setOnClickListener(view -> signInWithEmailAndPassword());

        signInWithGoogleCardView = findViewById(R.id.sign_in_with_google_card_view);
        signInWithGoogleCardView.setOnClickListener(view -> signInWithGoogle());
    }

    private void signInWithGoogle() {
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();

        GoogleSignInClient mGoogleSignInClient = GoogleSignIn.getClient(this, gso);

        Intent signInIntent = mGoogleSignInClient.getSignInIntent();
        startActivityForResult(signInIntent, RC_SIGN_IN);
    }

    private void startRegistrationActivity() {
        Intent intent = new Intent(this, RegistrationActivity.class);
        startActivityForResult(intent, REGISTRATION_REQUEST_CODE);
    }

    private void checkLoggingIn() {
        if (FirebaseAuth.getInstance().getCurrentUser() != null) {
            launchGroupsActivityAndFinish();
        } else {
            loadingDialog.dismiss();
        }
    }

    private void signInWithEmailAndPassword() {

        String email = emailEditText.getText().toString().trim();
        String password = passwordEditText.getText().toString().trim();

        if (!checkEmailAndPassword(email, password))
            return;


        loadingDialog.show(getSupportFragmentManager(), LOADING_DIALOG_TAG);

        FirebaseAuth.getInstance().signInWithEmailAndPassword(email, password).addOnSuccessListener(authResult -> {
            loadingDialog.dismiss();
            launchGroupsActivityAndFinish();
        }).addOnFailureListener(e -> {
            loadingDialog.dismiss();
            Snackbar.make(findViewById(android.R.id.content), Objects.requireNonNull(e.getLocalizedMessage()), BaseTransientBottomBar.LENGTH_LONG).show();
        });
    }

    private boolean checkEmailAndPassword(String email, String password) {

        if (email.isEmpty()) {
            emailEditText.setError(getString(R.string.empty_field_error), errorIcon);
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
        return true;
    }

    private void launchGroupsActivityAndFinish() {
        String userId = Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getUid();
        Intent intent = new Intent(getApplicationContext(), AllGroupsActivity.class);
        intent.putExtra(USER_ID, userId);

        StorageReference bgRef = FirebaseStorage.getInstance().getReference().child("users/" + userId + "/background.jpg");
        bgRef.getDownloadUrl().addOnSuccessListener(uri -> {
            UserProfileActivity.backgroundImageUri = uri;
            startActivity(intent);
            finish();
        }).addOnFailureListener(e -> {
            startActivity(intent);
            finish();
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == REGISTRATION_REQUEST_CODE && resultCode == RESULT_OK) {
            emailEditText.setText(Objects.requireNonNull(data).getStringExtra(EMAIL));
            passwordEditText.setText(data.getStringExtra(PASSWORD));
        } else if (requestCode == RC_SIGN_IN) {
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            try {
                GoogleSignInAccount account = task.getResult(ApiException.class);
                firebaseAuthWithGoogle(Objects.requireNonNull(account).getIdToken());
            } catch (ApiException e) {
                Toast.makeText(this, e.getLocalizedMessage(), Toast.LENGTH_LONG).show();
            }
        }
    }

    private void firebaseAuthWithGoogle(String idToken) {
        AuthCredential credential = GoogleAuthProvider.getCredential(idToken, null);
        FirebaseAuth mAuth = FirebaseAuth.getInstance();
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        DocumentReference reference = FirebaseFirestore.getInstance().collection("Users")
                                .document(Objects.requireNonNull(FirebaseAuth.getInstance().getUid()));
                        reference.addSnapshotListener((value, error) -> {

                            String nickname = value.getString("nickname");
                            if(nickname == null){
                                HashMap<String, Object> userMap = new HashMap<>();
                                userMap.put("id", FirebaseAuth.getInstance().getUid());
                                userMap.put("nickname", Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getEmail());
                                reference.set(userMap);
                            }
                            launchGroupsActivityAndFinish();

                        });

                    } else {
                        Toast.makeText(getApplicationContext(), Objects.requireNonNull(task.getException()).getLocalizedMessage(), Toast.LENGTH_LONG).show();
                    }
                });
    }
}