package com.example.socialmediaplatform;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.example.socialmediaplatform.helpers.DatabaseHelper;

import java.util.ArrayList;
import java.util.List;

public class ContactListActivity extends AppCompatActivity {
    private static final int PERMISSIONS_REQUEST_READ_CONTACTS = 100;
    private DatabaseHelper dbHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_contact_list);

        // Check for permission
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_CONTACTS)
                != PackageManager.PERMISSION_GRANTED) {
            // Request permission
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.READ_CONTACTS},
                    PERMISSIONS_REQUEST_READ_CONTACTS);
        } else {
            // Permission already granted
            dbHelper = new DatabaseHelper(this);
            loadContacts();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == PERMISSIONS_REQUEST_READ_CONTACTS) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Permission granted
                loadContacts();
            } else {
                // Permission denied
                Toast.makeText(this, "Permission denied to read contacts", Toast.LENGTH_SHORT).show();
            }
        }
    }

    // Method to load contacts
    private void loadContacts() {
        List<String> contactList = new ArrayList<>();
        List<String> registeredContacts = getRegisteredContactsFromDatabase(); // Fetch registered contacts from SQLite

        // Query contacts from the device
        Cursor cursor = getContentResolver().query(
                ContactsContract.Contacts.CONTENT_URI,
                null,
                null,
                null,
                null);

        if (cursor != null) {
            while (cursor.moveToNext()) {
                @SuppressLint("Range") String displayName = cursor.getString(
                        cursor.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME));

                // Only add contacts that are in the registeredContacts list
                if (registeredContacts.contains(displayName)) {
                    // Assuming you have a method to get the user ID for the contact
                    String contactUserId = getUserIdForContact(displayName); // Replace with actual implementation
                    contactList.add(displayName + "|" + contactUserId); // Store display name and user ID
                }
            }
            cursor.close();
        }

        ListView listView = findViewById(R.id.listViewContacts);
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, contactList);
        listView.setAdapter(adapter);

        // Set an item click listener on the list view
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                String selectedContact = contactList.get(position);
                String[] parts = selectedContact.split("\\|"); // Split to get name and user ID
                String contactName = parts[0];
                String contactUserId = parts[1];

                // Get the current logged-in user ID
                String currentUserId = getCurrentUserId(); // Implement this method to retrieve the current user's ID

                // Open ChatRoomActivity and pass the contact name, current user ID, and selected contact's user ID
                Intent intent = new Intent(ContactListActivity.this, ChatRoomActivity.class);
                intent.putExtra("CONTACT_NAME", contactName);
                intent.putExtra("CONTACT_ID", contactUserId);
                intent.putExtra("USER_ID", currentUserId);
                startActivity(intent);
            }
        });
    }

    private List<String> getRegisteredContactsFromDatabase() {
        List<String> registeredContacts = new ArrayList<>();
        SQLiteDatabase db = dbHelper.getReadableDatabase(); // Assuming you have a method to get your database

        Cursor cursor = db.query(
                "users", // Replace with your table name
                new String[]{"NAME"}, // Replace with the column containing contact names
                null,
                null,
                null,
                null,
                null);

        if (cursor != null) {
            while (cursor.moveToNext()) {
                @SuppressLint("Range") String contactName = cursor.getString(cursor.getColumnIndex("NAME"));
                registeredContacts.add(contactName);
            }
            cursor.close();
        }

        db.close();
        return registeredContacts;
    }

    @SuppressLint("Range")
    private String getUserIdForContact(String contactName) {
        String userId = null;
        SQLiteDatabase db = dbHelper.getReadableDatabase(); // Replace with your database helper instance

        // Query the database to find the user ID
        String query = "SELECT id FROM users WHERE name = ?";
        Cursor cursor = db.rawQuery(query, new String[]{contactName});

        if (cursor != null) {
            if (cursor.moveToFirst()) {
                userId = cursor.getString(cursor.getColumnIndex("ID")); // Retrieve the user ID
            }
            cursor.close();
        }
        return userId; // Return the user ID or null if not found
    }

    private String getCurrentUserId() {
        SharedPreferences sharedPreferences = getSharedPreferences("UserPrefs", MODE_PRIVATE);
        return sharedPreferences.getString("CURRENT_USER_ID", null); // Retrieve the current user's ID from shared preferences
    }

}
