package com.example.furniturestore.settings;

import android.os.Bundle;
import android.text.TextUtils;
import android.widget.*;

import androidx.appcompat.app.AppCompatActivity;

import com.example.furniturestore.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.*;

import java.util.HashMap;
import java.util.Map;

public class EditProfileActivity extends AppCompatActivity {

    private EditText editTextName, editTextPhone, editTextAge;
    private TextView textViewEmail, textViewPassword;
    private Button buttonSave;

    private FirebaseAuth auth;
    private FirebaseFirestore db;
    private FirebaseUser currentUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_profile);

        auth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
        currentUser = auth.getCurrentUser();

        editTextName = findViewById(R.id.editTextName);
        editTextPhone = findViewById(R.id.editTextPhone);
        editTextAge = findViewById(R.id.editTextAge);
        textViewEmail = findViewById(R.id.textEmail);             // Non-editable email
        textViewPassword = findViewById(R.id.textPasswordHint);   // Non-editable password field (dummy)

        buttonSave = findViewById(R.id.buttonSave);

        ImageView backIcon = findViewById(R.id.backIcon);
        backIcon.setOnClickListener(v -> onBackPressed());

        loadUserData();

        buttonSave.setOnClickListener(v -> saveUserData());
    }

    private void loadUserData() {
        if (currentUser == null) return;

        String uid = currentUser.getUid();

        db.collection("users").document(uid).get()
                .addOnSuccessListener(document -> {
                    if (document.exists()) {
                        editTextName.setText(document.getString("name"));
                        editTextPhone.setText(document.getString("phone"));
                        editTextAge.setText(document.getString("age"));
                        textViewEmail.setText(document.getString("email"));
                        textViewPassword.setText("********"); // Display placeholder
                    }
                })
                .addOnFailureListener(e ->
                        Toast.makeText(this, "Failed to load profile: " + e.getMessage(), Toast.LENGTH_SHORT).show()
                );
    }

    private void saveUserData() {
        if (currentUser == null) return;

        String uid = currentUser.getUid();
        String name = editTextName.getText().toString().trim();
        String phone = editTextPhone.getText().toString().trim();
        String age = editTextAge.getText().toString().trim();

        if (TextUtils.isEmpty(name)) {
            editTextName.setError("Name required");
            return;
        }

        Map<String, Object> updates = new HashMap<>();
        updates.put("name", name);
        updates.put("phone", phone);
        updates.put("age", age);

        db.collection("users").document(uid).update(updates)
                .addOnSuccessListener(unused -> {
                    Toast.makeText(this, "Profile updated", Toast.LENGTH_SHORT).show();
                    finish();
                })
                .addOnFailureListener(e ->
                        Toast.makeText(this, "Update failed: " + e.getMessage(), Toast.LENGTH_SHORT).show()
                );
    }
}
