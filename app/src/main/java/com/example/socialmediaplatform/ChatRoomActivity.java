package com.example.socialmediaplatform;

import android.annotation.SuppressLint;
import android.content.DialogInterface;
import android.database.Cursor;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.example.socialmediaplatform.helpers.DatabaseHelper;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

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

                    FirebaseFirestore firestore = FirebaseFirestore.getInstance();

                    // Create a message object for Firebase
                    Map<String, Object> messageData = new HashMap<>();
                    messageData.put("userId", userId);
                    messageData.put("contactId", contactId);
                    messageData.put("message", message);
                    messageData.put("timestamp", timestamp);

                    firestore.collection("messages") // Adjust the collection name as needed
                            .add(messageData)
                            .addOnCompleteListener(new OnCompleteListener<DocumentReference>() {
                                @Override
                                public void onComplete(@NonNull Task<DocumentReference> task) {
                                    if (task.isSuccessful()) {
                                        editTextMessage.setText(""); // Clear input field
                                    } else {
                                        // Handle error
                                        Toast.makeText(getApplicationContext(), "Failed to send message to Firebase", Toast.LENGTH_SHORT).show();
                                    }
                                }
                            });

                    // Display the message in the chat
                    displayMessage("You: " + message, "0");
                    editTextMessage.setText(""); // Clear input field
                }
            }
        });

        loadChatHistory(); // Load messages from SQLite database

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
                @SuppressLint("Range") String messageId = cursor.getString(cursor.getColumnIndex("id"));
                String displayMessage = senderId.equals(userId) ? "You: " : contactId + ": ";
                displayMessage += message;
                displayMessage(displayMessage, messageId);
            } while (cursor.moveToNext());
        }
        cursor.close();
    }

    private void displayMessage(String message, String messageId) {
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

        // Set long click listener to edit message
        messageView.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                showEditDialog(messageId, message); // Open dialog to edit message
                return true; // Indicate that the long-click was handled
            }
        });
    }

    private void showEditDialog(String messageId, String currentMessage) {
        // Logic to show a dialog with an EditText to modify the message
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Edit Message");

        // Set up the input
        final EditText input = new EditText(this);
        input.setText(currentMessage);
        builder.setView(input);

        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String updatedMessage = input.getText().toString();
                if (!updatedMessage.isEmpty()) {
                    updateMessageInDatabase(messageId, updatedMessage); // Update the message in the database
                    refreshChatHistory(); // Reload the chat history to reflect changes
                }
            }
        });
        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });

        builder.show();
    }

    private void updateMessageInDatabase(String messageId, String newMessage) {
        // Your logic to update the message in the database
        databaseHelper.updateMessage(messageId, newMessage); // Example method, implement it in your DatabaseHelper class
    }

    private void refreshChatHistory() {
        chatMessagesLayout.removeAllViews(); // Clear current messages
        loadChatHistory(); // Reload messages from the database
    }
}
