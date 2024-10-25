package com.example.socialmediaplatform;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;

public class ChatRoomActivity extends AppCompatActivity {
    private LinearLayout chatMessagesLayout;
    private EditText editTextMessage;
    private Button btnSend;
    private ScrollView scrollView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat_room);

        chatMessagesLayout = findViewById(R.id.chatMessagesLayout);
        editTextMessage = findViewById(R.id.editTextMessage);
        btnSend = findViewById(R.id.btnSend);
        scrollView = findViewById(R.id.scrollView);  // Get the ScrollView by ID

        // Get the contact name from the intent
        String contactName = getIntent().getStringExtra("CONTACT_NAME");
        setTitle("Chat with " + contactName);

        btnSend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String message = editTextMessage.getText().toString();
                if (!message.isEmpty()) {
                    // Display the message in the chat
                    displayMessage("You: " + message);
                    editTextMessage.setText(""); // Clear input field
                }
            }
        });
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
