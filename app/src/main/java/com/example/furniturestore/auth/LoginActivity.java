package com.example.furniturestore.auth;

import android.content.Intent;
import android.os.Bundle;
import android.util.Patterns;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.furniturestore.R;
import com.example.furniturestore.customer.CartActivity;
import com.example.furniturestore.dashboard.AdminDashboardActivity;
import com.example.furniturestore.dashboard.CustomerDashboardActivity;
import com.example.furniturestore.dashboard.SellerDashboardActivity;
import com.example.furniturestore.database.AppDatabase;
import com.example.furniturestore.models.CartItem;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class LoginActivity extends AppCompatActivity {

    private EditText editTextEmail, editTextPassword;
    private FirebaseAuth auth;
    private FirebaseFirestore db;
    private boolean redirectAfterLogin = false;  // Flag to handle redirect

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        editTextEmail = findViewById(R.id.editTextEmail);
        editTextPassword = findViewById(R.id.editTextPassword);
        Button buttonLogin = findViewById(R.id.buttonLogin);
        Button buttonGoBack = findViewById(R.id.buttonGoBack);
        TextView textViewRegister = findViewById(R.id.textViewRegister);

        auth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        // Check if intent contains redirect flag
        if (getIntent() != null && "checkout".equals(getIntent().getStringExtra("redirectAfterLogin"))) {
            redirectAfterLogin = true;
        }

        buttonLogin.setOnClickListener(v -> loginUser());

        buttonGoBack.setOnClickListener(v -> onBackPressed());

        textViewRegister.setOnClickListener(v ->
                startActivity(new Intent(LoginActivity.this, RegisterActivity.class))
        );
    }

    private void loginUser() {
        String email = editTextEmail.getText().toString().trim();
        String password = editTextPassword.getText().toString().trim();

        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            editTextEmail.setError("Enter valid email");
            return;
        }

        if (password.length() < 6) {
            editTextPassword.setError("Password must be at least 6 characters");
            return;
        }

        auth.signInWithEmailAndPassword(email, password)
                .addOnSuccessListener(authResult -> {
                    String uid = auth.getCurrentUser().getUid();

                    // Handle admin user specially - ensure Firestore doc exists or created
                    if (email.equalsIgnoreCase("admin@gmail.com")) {
                        // Create or overwrite admin user document with role
                        Map<String, Object> adminData = new HashMap<>();
                        adminData.put("role", "admin");
                        adminData.put("email", email);
                        adminData.put("name", "Admin");

                        db.collection("users").document(uid)
                                .set(adminData)
                                .addOnSuccessListener(unused -> {
                                    migrateGuestCartToUser(uid);
                                    startActivity(new Intent(LoginActivity.this, AdminDashboardActivity.class));
                                    finish();
                                })
                                .addOnFailureListener(e -> {
                                    Toast.makeText(this, "Error setting admin data: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                                });
                        return;
                    }

                    // For other users: load user doc and proceed accordingly
                    db.collection("users").document(uid).get()
                            .addOnSuccessListener(documentSnapshot -> {
                                if (documentSnapshot.exists()) {
                                    String role = documentSnapshot.getString("role");
                                    if (role == null) role = "customer";

                                    // Migrate guest cart items after login
                                    migrateGuestCartToUser(uid);

                                    if (redirectAfterLogin) {
                                        // Redirect to CartActivity (checkout flow)
                                        Intent intent = new Intent(LoginActivity.this, CartActivity.class);
                                        intent.putExtra("startCheckout", true);
                                        startActivity(intent);
                                        finish();
                                        return;
                                    }

                                    Intent intent = null;
                                    switch (role) {
                                        case "admin":
                                            intent = new Intent(this, AdminDashboardActivity.class);
                                            break;
                                        case "seller":
                                            intent = new Intent(this, SellerDashboardActivity.class);
                                            break;
                                        case "customer":
                                        default:
                                            Toast.makeText(this, "Login successful", Toast.LENGTH_SHORT).show();
                                            intent = new Intent(this, com.example.furniturestore.MainActivity.class);
                                            break;
                                    }
                                    if (intent != null) {
                                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                        startActivity(intent);
                                        finish();
                                    }

                                    finish();
                                } else {
                                    Toast.makeText(this, "User not found in database", Toast.LENGTH_SHORT).show();
                                }
                            })
                            .addOnFailureListener(e ->
                                    Toast.makeText(this, "Failed to fetch user data: " + e.getMessage(), Toast.LENGTH_SHORT).show()
                            );
                })
                .addOnFailureListener(e ->
                        Toast.makeText(this, "Login failed: " + e.getMessage(), Toast.LENGTH_SHORT).show());
    }

    // Guest cart migration logic — copy guest items to logged-in user and clear guest cart
    private void migrateGuestCartToUser(String newUserId) {
        new Thread(() -> {
            AppDatabase db = AppDatabase.getInstance(getApplicationContext());
            List<CartItem> guestCartItems = db.cartDao().getCartItemsForUser("guest_user");

            for (CartItem item : guestCartItems) {
                item.setUserId(newUserId);
                db.cartDao().insertCartItem(item); // Insert as new items under logged-in user
            }

            db.cartDao().clearCartForUser("guest_user");
        }).start();
    }
}
