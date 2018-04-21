package com.example.ramesh.chatapplication;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.ui.auth.AuthUI;
import com.firebase.ui.database.FirebaseListAdapter;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.FirebaseDatabase;

import java.util.Arrays;
import java.util.List;


/**
 * Created by hp on 6/26/2017.
 */

public class ChatRoom extends AppCompatActivity {

    private FirebaseListAdapter<ChatMessage> adapter;
    SharedPreferences sharedPreferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat_room);
        sharedPreferences = getSharedPreferences("MyPrefs", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString("language", "English");
        editor.apply();

        if (FirebaseAuth.getInstance().getCurrentUser() == null) {
            //start sign in/sign up activity
            startActivityForResult(AuthUI.getInstance()
                    .createSignInIntentBuilder()
                    .setLogo(R.mipmap.logo)
                    .setTheme(R.style.LoginTheme)
                    .build(), 10
            );
        } else {
            //User is already signed in
            Toast.makeText(ChatRoom.this, "Welcome " + FirebaseAuth.getInstance().getCurrentUser().getEmail(), Toast.LENGTH_SHORT).show();
            displayChatMessages();
        }

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                EditText message = (EditText) findViewById(R.id.input);
                //Read the input field and push the new Instance
                //of chat message to Firebase Database
                ChatMessage chatMessage = new ChatMessage(message.getText().toString(),
                        FirebaseAuth.getInstance().getCurrentUser().getEmail());
                chatMessage.setLanguage(sharedPreferences.getString("language", "English"));
                FirebaseDatabase.getInstance().getReference()
                        .push().setValue(chatMessage);
                //clear the input
                message.setText("");
            }
        });
    }

    private void displayChatMessages() {
        ListView list = (ListView) findViewById(R.id.list_of_messages);
        adapter = new FirebaseListAdapter<ChatMessage>(ChatRoom.this, ChatMessage.class,
                R.layout.message, FirebaseDatabase.getInstance().getReference()) {
            @Override
            protected void populateView(View v, ChatMessage model, int position) {
                //Get references to the view of message
                TextView messageText = (TextView) v.findViewById(R.id.message_text);
                TextView messageTime = (TextView) v.findViewById(R.id.message_time);
                TextView messageUser = (TextView) v.findViewById(R.id.message_user);

                //set values
                messageText.setText(model.getMessageText());
                messageUser.setText(model.getMessageUser());
                //messageTime.setText(java.text.DateFormat.getInstance().format(model.getMessageTime()));
                messageTime.setText(model.getMessageTime());

                //Format the time before showing it
                //messageTime.setText(model.getMessageTime(), DateFormat.format("dd-MM-yyyy(HH:mm:ss)"));
            }
        };
        list.setAdapter(adapter);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 10) {
            if (resultCode == RESULT_OK) {
                Toast.makeText(ChatRoom.this, "Welcome!!!" + FirebaseAuth.getInstance().getCurrentUser().getEmail(), Toast.LENGTH_SHORT).show();
                displayChatMessages();
            } else {
                Toast.makeText(ChatRoom.this, "We couldn't sign you in..", Toast.LENGTH_SHORT).show();
                finish();
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.menu_sign_out) {
            AuthUI.getInstance().signOut(ChatRoom.this)
                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            Toast.makeText(ChatRoom.this, "Signed out", Toast.LENGTH_SHORT).show();
                            finish();
                        }
                    });
        }
        else if (item.getItemId() == R.id.select_language) {
            SelectLanguage cdd = new SelectLanguage(ChatRoom.this);
            cdd.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            cdd.show();
        }
        return true;
    }
}
