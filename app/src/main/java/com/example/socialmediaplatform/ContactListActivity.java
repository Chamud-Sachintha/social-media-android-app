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
import android.widget.Button;
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

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_CONTACTS)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.READ_CONTACTS},
                    PERMISSIONS_REQUEST_READ_CONTACTS);
        } else {
            dbHelper = new DatabaseHelper(this);
            loadContacts();
        }

        Button btnBack = findViewById(R.id.btnBack);
        btnBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Redirect to activity_home.xml
                Intent intent = new Intent(ContactListActivity.this, HomeActivity.class);
                startActivity(intent);
                finish(); // Finish the current activity
            }
        });
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
        List<String> registeredContacts = getRegisteredContactsFromDatabase();

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

                if (registeredContacts.contains(displayName)) {
                    String contactUserId = getUserIdForContact(displayName);
                    contactList.add(displayName + "|" + contactUserId);
                }
            }
            cursor.close();
        }

        ListView listView = findViewById(R.id.listViewContacts);
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, contactList);
        listView.setAdapter(adapter);

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                String selectedContact = contactList.get(position);
                String[] parts = selectedContact.split("\\|");
                String contactName = parts[0];
                String contactUserId = parts[1];

                // Get the current logged-in user ID
                String currentUserId = getCurrentUserId();

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
        SQLiteDatabase db = dbHelper.getReadableDatabase();

        Cursor cursor = db.query(
                "users",
                new String[]{"NAME"},
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
        SQLiteDatabase db = dbHelper.getReadableDatabase();

        // Query the database to find the user ID
        String query = "SELECT id FROM users WHERE name = ?";
        Cursor cursor = db.rawQuery(query, new String[]{contactName});

        if (cursor != null) {
            if (cursor.moveToFirst()) {
                userId = cursor.getString(cursor.getColumnIndex("ID"));
            }
            cursor.close();
        }
        return userId;
    }

    private String getCurrentUserId() {
        SharedPreferences sharedPreferences = getSharedPreferences("UserPrefs", MODE_PRIVATE);
        return sharedPreferences.getString("CURRENT_USER_ID", null);
    }

}
