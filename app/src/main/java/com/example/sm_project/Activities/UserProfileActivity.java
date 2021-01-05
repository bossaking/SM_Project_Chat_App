package com.example.sm_project.Activities;

import android.app.AlertDialog;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.media.Image;
import android.net.Uri;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import androidx.appcompat.widget.Toolbar;
import com.bumptech.glide.Glide;
import com.bumptech.glide.request.target.SimpleTarget;
import com.bumptech.glide.request.transition.Transition;
import com.example.sm_project.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Objects;

import static com.example.sm_project.Activities.AllGroupsActivity.USER_ID;
import static com.example.sm_project.Activities.AllGroupsActivity.USER_NICKNAME;

public class UserProfileActivity extends AppCompatActivity implements TextWatcher {

    public static Uri backgroundImageUri;

    private String userId;
    private String nickname;

    private EditText nicknameEditText;
    private Button applyChangesButton;

    @Override
    protected void onResume() {
        super.onResume();
        try {

            Glide
                    .with(getApplicationContext())
                    .asBitmap()
                    .load(backgroundImageUri)
                    .into(new SimpleTarget<Bitmap>(Resources.getSystem().getDisplayMetrics().widthPixels,
                            Resources.getSystem().getDisplayMetrics().heightPixels) {
                        @Override
                        public void onResourceReady(@NonNull Bitmap resource, @Nullable Transition<? super Bitmap> transition) {
                            Drawable dr = new BitmapDrawable(getResources(), resource);
                            getWindow().getDecorView().setBackground(dr);
                        }
                    });

        } catch (Exception e) {
            Toast.makeText(this, e.getLocalizedMessage(), Toast.LENGTH_LONG).show();
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_profile);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        Objects.requireNonNull(getSupportActionBar()).setHomeAsUpIndicator(R.drawable.ic_left_arrow);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowTitleEnabled(false);

        userId = getIntent().getStringExtra(USER_ID);
        nickname = getIntent().getStringExtra(USER_NICKNAME);

        nicknameEditText = findViewById(R.id.nickname_edit_text);
        nicknameEditText.setText(nickname);
        nicknameEditText.addTextChangedListener(this);

        Button changeBackgroundButton = findViewById(R.id.change_background_button);
        changeBackgroundButton.setOnClickListener(view -> changeBackground());

        applyChangesButton = findViewById(R.id.apply_changes_button);
        applyChangesButton.setOnClickListener(view -> changeNickname(nicknameEditText.getText().toString().trim()));
    }

    private void changeBackground() {
        CropImage.activity()
                .setGuidelines(CropImageView.Guidelines.ON).setCropShape(CropImageView.CropShape.RECTANGLE)
                .start(this);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            CropImage.ActivityResult result = CropImage.getActivityResult(data);
            if (resultCode == RESULT_OK) {
                Uri resultUri = Objects.requireNonNull(result).getUri();
                backgroundImageUri = resultUri;
                StorageReference fileRef = FirebaseStorage.getInstance().getReference().child("users/" + userId + "/background.jpg");
                fileRef.putFile(resultUri).addOnSuccessListener(taskSnapshot -> {
                    Toast.makeText(this, getString(R.string.success), Toast.LENGTH_SHORT).show();
                }).addOnFailureListener(e -> Toast.makeText(this, e.getLocalizedMessage(), Toast.LENGTH_LONG).show());
                try {
                    InputStream inputStream = getContentResolver().openInputStream(resultUri);
                    Drawable bg = Drawable.createFromStream(inputStream, resultUri.toString());
                    getWindow().getDecorView().setBackground(bg);
                } catch (FileNotFoundException ignored) {
                }
            } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {
                Toast.makeText(this, "Error: " + Objects.requireNonNull(result).getError().getLocalizedMessage(), Toast.LENGTH_LONG).show();
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.user_profile_toolbar_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {

        switch (item.getItemId()) {
            case R.id.logout_button:
                logout();
                break;
            case android.R.id.home:
                onBackPressed();
                break;
        }

        return true;
    }

    private void changeNickname(String newNickname) {
        HashMap<String, Object> updatedUserMap = new HashMap<>();
        updatedUserMap.put("nickname", newNickname);
        FirebaseFirestore.getInstance().collection("Users").document(userId).update(updatedUserMap).addOnSuccessListener(unused -> {
            nickname = newNickname;
            Toast.makeText(this, "Success", Toast.LENGTH_SHORT).show();
        }).addOnFailureListener(e -> Toast.makeText(this, e.getLocalizedMessage(), Toast.LENGTH_SHORT).show());
    }

    private void logout() {

        AlertDialog logoutDialog = new AlertDialog.Builder(this)
                .setTitle(getString(R.string.logout))
                .setMessage(R.string.logout_question)
                .setIcon(R.drawable.ic_exit)
                .setPositiveButton(R.string.logout, (dialogInterface, i) -> {
                    FirebaseAuth.getInstance().signOut();
                    startActivity(new Intent(this, LoginActivity.class));
                    finishAffinity();
                }).setNegativeButton(R.string.cancel, ((dialogInterface, i) -> {
                }
                )).create();

        logoutDialog.show();


    }

    @Override
    public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

    }

    @Override
    public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
        String newNickname = nicknameEditText.getText().toString().trim();
        applyChangesButton.setEnabled(!newNickname.equals(nickname) && !newNickname.isEmpty());
    }

    @Override
    public void afterTextChanged(Editable editable) {

    }
}