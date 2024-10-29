package com.example.socialmediaplatform;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.example.socialmediaplatform.helpers.DatabaseHelper;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.List;

public class HomeActivity extends AppCompatActivity {

    FloatingActionButton fabAddContact;
    private ListView contactsListView;
    private DatabaseHelper databaseHelper;
//    private String userId = this.getCurrentUserId(); // Replace with actual user ID
    private ArrayAdapter<String> adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        fabAddContact = findViewById(R.id.fabAddContact);
        fabAddContact.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Open the ContactListActivity when the FAB is clicked
                Intent intent = new Intent(HomeActivity.this, ContactListActivity.class);
                startActivity(intent);
            }
        });

        contactsListView = findViewById(R.id.contactsListView);
        FloatingActionButton fabAddContact = findViewById(R.id.fabAddContact);

        databaseHelper = new DatabaseHelper(this);

        loadContacts();

        contactsListView.setOnItemClickListener((parent, view, position, id) -> {
            String selectedContact = adapter.getItem(position);
            String[] parts = selectedContact.split("\\|"); // Split to get name and user ID
            String contactName = parts[0];
            String contactUserId = parts[1];
            openChatWithContact(contactUserId, contactName);
        });
    }

    private void loadContacts() {
        String userId = getCurrentUserId();
        List<String> contactsList = databaseHelper.getContactsWithChatHistory(userId);
        if (contactsList.isEmpty()) {
            Toast.makeText(this, "No chat history available", Toast.LENGTH_SHORT).show();
        }
        adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, contactsList);
        contactsListView.setAdapter(adapter);
    }

    private void openChatWithContact(String contactId, String contactName) {

        String currentUserId = getCurrentUserId();

        Intent intent = new Intent(this, ChatRoomActivity.class);
        intent.putExtra("CONTACT_NAME", contactName);
        intent.putExtra("CONTACT_ID", contactId);
        intent.putExtra("USER_ID", currentUserId);
        startActivity(intent);
    }

    private String getCurrentUserId() {
        SharedPreferences sharedPreferences = getSharedPreferences("UserPrefs", MODE_PRIVATE);
        return sharedPreferences.getString("CURRENT_USER_ID", null); // Retrieve the current user's ID from shared preferences
    }
}
