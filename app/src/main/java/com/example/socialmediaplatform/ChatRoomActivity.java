package com.example.socialmediaplatform;

import android.annotation.SuppressLint;
import android.database.Cursor;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;

import com.example.socialmediaplatform.helpers.DatabaseHelper;

public class ChatRoomActivity extends AppCompatActivity {
    private LinearLayout chatMessagesLayout;
    private EditText editTextMessage;
    private Button btnSend;
    private ScrollView scrollView;
    private ImageButton btnBack;
    private DatabaseHelper databaseHelper;
    private String userId, contactId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat_room);

        chatMessagesLayout = findViewById(R.id.chatMessagesLayout);
        editTextMessage = findViewById(R.id.editTextMessage);
        btnSend = findViewById(R.id.btnSend);
        scrollView = findViewById(R.id.scrollView);  // Get the ScrollView by ID
        databaseHelper = new DatabaseHelper(this);
        btnBack = findViewById(R.id.btnBack);

        // Get the contact name from the intent
        String contactName = getIntent().getStringExtra("CONTACT_NAME");
        userId = getIntent().getStringExtra("USER_ID"); // Current user ID
        contactId = getIntent().getStringExtra("CONTACT_ID"); // Contact ID
        setTitle("Chat with " + contactName);

        loadChatHistory(); // Load messages from SQLite database

        btnSend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String message = editTextMessage.getText().toString();
                if (!message.isEmpty()) {

                    long timestamp = System.currentTimeMillis();
                    databaseHelper.addMessage(userId, contactId, message, timestamp);

                    // Display the message in the chat
                    displayMessage("You: " + message);
                    editTextMessage.setText(""); // Clear input field
                }
            }
        });

        // Set up the back button listener
        btnBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish(); // Close the current activity and return to the previous one
            }
        });

    }

    private void loadChatHistory() {
        Cursor cursor = databaseHelper.getMessages(userId, contactId);
        if (cursor.moveToFirst()) {
            do {
                @SuppressLint("Range") String senderId = cursor.getString(cursor.getColumnIndex("sender_id"));
                @SuppressLint("Range") String message = cursor.getString(cursor.getColumnIndex("message"));
                String displayMessage = senderId.equals(userId) ? "You: " : contactId + ": ";
                displayMessage += message;
                displayMessage(displayMessage);
            } while (cursor.moveToNext());
        }
        cursor.close();
    }

    private void displayMessage(String message) {
        TextView messageView = new TextView(this);
        messageView.setText(message);
        messageView.setTextSize(16); // Set text size
        messageView.setPadding(16, 8, 16, 8); // Add padding for better readability

        // Add the message to the chat layout
        chatMessagesLayout.addView(messageView);

        // Scroll to the bottom of the ScrollView
        scrollView.post(new Runnable() {
            @Override
            public void run() {
                scrollView.fullScroll(View.FOCUS_DOWN);
            }
        });
    }
}
