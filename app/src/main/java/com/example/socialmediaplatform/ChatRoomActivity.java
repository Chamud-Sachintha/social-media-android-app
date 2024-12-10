package com.example.socialmediaplatform;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.DialogInterface;
import android.database.Cursor;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.util.Log;
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
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.HashMap;
import java.util.Map;

public class ChatRoomActivity extends AppCompatActivity {
    private LinearLayout chatMessagesLayout;
    private EditText editTextMessage;
    private Button btnSend;
    private ScrollView scrollView;
    private ImageButton btnBack;
    private DatabaseHelper databaseHelper;
    private String userId, contactId, contactName;
    FirebaseFirestore firestore;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat_room);

        EditText editTextSearch = findViewById(R.id.editTextSearch);
        Button btnSearch = findViewById(R.id.btnSearch);

        firestore = FirebaseFirestore.getInstance();

        chatMessagesLayout = findViewById(R.id.chatMessagesLayout);
        editTextMessage = findViewById(R.id.editTextMessage);
        btnSend = findViewById(R.id.btnSend);
        scrollView = findViewById(R.id.scrollView);  // Get the ScrollView by ID
        databaseHelper = new DatabaseHelper(this);
        btnBack = findViewById(R.id.btnBack);

        // Get the contact name from the intent
        contactName = getIntent().getStringExtra("CONTACT_NAME");
        userId = getIntent().getStringExtra("USER_ID");
        contactId = getIntent().getStringExtra("CONTACT_ID");
        setTitle("Chat with " + contactName);

        loadChatHistory();

        btnSend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String message = editTextMessage.getText().toString();
                if (!message.isEmpty()) {

                    long timestamp = System.currentTimeMillis();
                    long messageId = databaseHelper.addMessage(userId, contactId, message, timestamp);

                    FirebaseFirestore firestore = FirebaseFirestore.getInstance();

                    Map<String, Object> messageData = new HashMap<>();
                    messageData.put("userId", userId);
                    messageData.put("messageId", String.valueOf(messageId));
                    messageData.put("contactId", contactId);
                    messageData.put("message", message);
                    messageData.put("timestamp", timestamp);

                    firestore.collection("messages")
                            .add(messageData)
                            .addOnCompleteListener(new OnCompleteListener<DocumentReference>() {
                                @Override
                                public void onComplete(@NonNull Task<DocumentReference> task) {
                                    if (task.isSuccessful()) {
                                        editTextMessage.setText("");
                                    } else {
                                        Toast.makeText(getApplicationContext(), "Failed to send message to Firebase", Toast.LENGTH_SHORT).show();
                                    }
                                }
                            });


                    displayMessage("You: " + message, "0");
                    editTextMessage.setText("");
                }
            }
        });

        btnSearch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String searchTerm = editTextSearch.getText().toString();
                if (!searchTerm.isEmpty()) {
                    searchMessages(searchTerm);
                } else {
                    refreshChatHistory();
                }
            }
        });

        loadChatHistory();

        btnBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

    }

    private void searchMessages(String searchTerm) {
        Cursor cursor = databaseHelper.searchMessages(userId, contactId, searchTerm);
        chatMessagesLayout.removeAllViews();

        if (cursor.moveToFirst()) {
            do {
                @SuppressLint("Range") String senderId = cursor.getString(cursor.getColumnIndex("sender_id"));
                @SuppressLint("Range") String message = cursor.getString(cursor.getColumnIndex("message"));
                @SuppressLint("Range") String messageId = cursor.getString(cursor.getColumnIndex("id"));

                String displayMessage = senderId.equals(userId) ? "You: " : contactName + ": ";
                displayMessage += message;
                displayMessage(displayMessage, messageId);
            } while (cursor.moveToNext());
        } else {
            Toast.makeText(this, "No messages found", Toast.LENGTH_SHORT).show();
        }
        cursor.close();
    }

    private void loadChatHistory() {
        if (isInternetAvailable()) {
            syncMessagesFromFirebase();
        }

        loadMessagesFromSQLite();
    }

    private void syncMessagesFromFirebase() {
        firestore.collection("messages")
                .whereEqualTo("userId", userId)
                .whereEqualTo("contactId", contactId)
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful() && task.getResult() != null) {
                            for (QueryDocumentSnapshot document : task.getResult()) {
                                // Retrieve fields from Firestore document
                                String messageId = document.getString("messageId");
                                String senderId = document.getString("userId");
                                String message = document.getString("message");
                                Long timestamp = document.getLong("timestamp");

                                // Validate fields and save to SQLite if valid
                                if (senderId != null && message != null && timestamp != null) {
                                    if (!databaseHelper.isMessageExists(messageId)) {
                                        databaseHelper.addMessage(userId, contactId, message, timestamp);
                                    }
                                }
                            }
                        } else {
                            Log.d("FirestoreInfo", "No messages found or failed to fetch messages.");
                        }
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.e("FirestoreError", "Error fetching messages from Firestore", e);
                    }
                });
    }


    private void loadMessagesFromSQLite() {
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


    private boolean isInternetAvailable() {
        ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        return activeNetwork != null && activeNetwork.isConnectedOrConnecting();
    }

    private void displayMessage(String message, String messageId) {
        TextView messageView = new TextView(this);
        messageView.setText(message);
        messageView.setTextSize(16);
        messageView.setPadding(16, 8, 16, 8);

        chatMessagesLayout.addView(messageView);

        scrollView.post(new Runnable() {
            @Override
            public void run() {
                scrollView.fullScroll(View.FOCUS_DOWN);
            }
        });

        messageView.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                showEditDialog(messageId, message);
                return true;
            }
        });
    }

    private void showEditDialog(String messageId, String currentMessage) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Edit or Delete Message");

        final EditText input = new EditText(this);
        input.setText(currentMessage);
        builder.setView(input);

        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String updatedMessage = input.getText().toString();
                if (!updatedMessage.isEmpty()) {
                    updateMessageInDatabase(messageId, updatedMessage);
                    refreshChatHistory();
                }
            }
        });

        builder.setNeutralButton("Delete", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                deleteMessageFromDatabase(messageId);
                refreshChatHistory();
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
        databaseHelper.updateMessage(messageId, newMessage);
    }

    private void deleteMessageFromDatabase(String messageId) {
        databaseHelper.deleteMessage(messageId);
    }

    private void refreshChatHistory() {
        chatMessagesLayout.removeAllViews();
        loadChatHistory();
    }
}
