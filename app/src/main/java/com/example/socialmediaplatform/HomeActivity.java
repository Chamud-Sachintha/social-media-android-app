package com.example.socialmediaplatform;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

public class HomeActivity extends AppCompatActivity {

    FloatingActionButton fabAddContact;

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
    }
}
