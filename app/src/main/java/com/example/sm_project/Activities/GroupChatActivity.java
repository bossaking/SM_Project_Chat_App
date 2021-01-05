package com.example.sm_project.Activities;

import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.text.Editable;
import android.text.TextWatcher;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import com.bumptech.glide.request.target.SimpleTarget;
import com.bumptech.glide.request.transition.Transition;
import com.example.sm_project.Adapters.MessageAdapter;
import com.example.sm_project.AlertDialogues.LoadingDialog;
import com.example.sm_project.Models.Group;
import com.example.sm_project.Models.Message;
import com.example.sm_project.R;
import com.google.firebase.database.*;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

import static com.example.sm_project.Activities.AllGroupsActivity.USER_ID;

public class GroupChatActivity extends AppCompatActivity implements MessageAdapter.onMessageClickListener {

    public static final String CHAT_ID = "";

    EditText messageEditText;
    Button sendMessageButton;

    MessageAdapter messageAdapter;
    RecyclerView messagesRecyclerView;

    List<Message> messages;

    private String userId;

    SimpleDateFormat format;

    private String chatId;

    LoadingDialog loadingDialog;

    DatabaseReference reference;
    ValueEventListener messagesEventListener;

    @Override
    protected void onResume() {
        super.onResume();
        try {

            Glide
                    .with(getApplicationContext())
                    .asBitmap()
                    .load(UserProfileActivity.backgroundImageUri)
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
        setContentView(R.layout.activity_chat_group);


        loadingDialog = new LoadingDialog();
        loadingDialog.show(getSupportFragmentManager(), null);

        userId = getIntent().getStringExtra(USER_ID);
        chatId = getIntent().getStringExtra(CHAT_ID);
        format = new SimpleDateFormat("HH:mm");

        messageEditText = findViewById(R.id.message_edit_text);
        messageEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                sendMessageButton.setEnabled(!messageEditText.getText().toString().trim().isEmpty());
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });

        sendMessageButton = findViewById(R.id.send_message_button);
        sendMessageButton.setOnClickListener(view -> {
            String message = messageEditText.getText().toString().trim();
            sendMessage(userId, message);
        });

        messagesRecyclerView = findViewById(R.id.messages_recycler_view);
        messagesRecyclerView.setHasFixedSize(true);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getApplicationContext());
        linearLayoutManager.setStackFromEnd(true);
        messagesRecyclerView.setLayoutManager(linearLayoutManager);

        messages = new ArrayList<>();
        messageAdapter = new MessageAdapter(this, messages, userId, this);
        messagesRecyclerView.setAdapter(messageAdapter);

        readMessages();

    }

    private void sendMessage(String sender, String message) {

        reference = FirebaseDatabase.getInstance().getReference();

        HashMap<String, Object> messageMap = new HashMap<>();
        messageMap.put("sender", sender);
        messageMap.put("messageContext", message);
        messageMap.put("messageTime", localToUTC("HH:mm", format.format(new Date())));

        reference.child("Chats").child(chatId).push().setValue(messageMap);

        messageEditText.setText("");
    }

    private void readMessages() {

        reference = FirebaseDatabase.getInstance().getReference("Chats").child(chatId);
        messagesEventListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {

                messages.clear();
                for (DataSnapshot snap : snapshot.getChildren()) {

                    Message message = snap.getValue(Message.class);
                    Objects.requireNonNull(message).setMessageTime(uTCToLocal("HH:mm", "HH:mm", message.getMessageTime()));
                    messages.add(message);
                    messageAdapter.notifyDataSetChanged();
                }
                    loadingDialog.dismiss();

                messagesRecyclerView.scrollToPosition(messages.size() - 1);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }

        };
        reference.addValueEventListener(messagesEventListener);
    }

    @Override
    protected void onPause() {
        super.onPause();
        reference.removeEventListener(messagesEventListener);


    }

    @Override
    public void onMessageClick(int position) {
        try {
            Intent intent = new Intent(Intent.ACTION_VIEW);
            intent.addFlags(Intent.FLAG_ACTIVITY_MULTIPLE_TASK);
            intent.setData(Uri.parse(messages.get(position).getMessageContext()));
            startActivity(intent);
        } catch (Exception ignored) {

        }

    }

    //Methods for time convertation
    public String localToUTC(String dateFormat, String datesToConvert) {


        String dateToReturn = datesToConvert;

        SimpleDateFormat sdf = new SimpleDateFormat(dateFormat);
        sdf.setTimeZone(TimeZone.getDefault());
        Date gmt;

        SimpleDateFormat sdfOutPutToSend = new SimpleDateFormat(dateFormat);
        sdfOutPutToSend.setTimeZone(TimeZone.getTimeZone("UTC"));

        try {

            gmt = sdf.parse(datesToConvert);
            dateToReturn = sdfOutPutToSend.format(Objects.requireNonNull(gmt));

        } catch (ParseException e) {
            e.printStackTrace();
        }
        return dateToReturn;
    }

    public static String uTCToLocal(String dateFormatInPut, String dateFormatOutPut, String datesToConvert) {


        String dateToReturn = datesToConvert;

        SimpleDateFormat sdf = new SimpleDateFormat(dateFormatInPut);
        sdf.setTimeZone(TimeZone.getTimeZone("UTC"));

        Date gmt;

        SimpleDateFormat sdfOutPutToSend = new SimpleDateFormat(dateFormatOutPut);
        sdfOutPutToSend.setTimeZone(TimeZone.getDefault());

        try {

            gmt = sdf.parse(datesToConvert);
            dateToReturn = sdfOutPutToSend.format(Objects.requireNonNull(gmt));

        } catch (ParseException e) {
            e.printStackTrace();
        }
        return dateToReturn;
    }


}