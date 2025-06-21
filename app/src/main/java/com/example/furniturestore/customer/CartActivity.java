package com.example.furniturestore.customer;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.furniturestore.R;
import com.example.furniturestore.adapters.CartAdapter;
import com.example.furniturestore.auth.LoginActivity;
import com.example.furniturestore.database.AppDatabase;
import com.example.furniturestore.models.CartItem;
import com.example.furniturestore.settings.ProfileActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.List;

public class CartActivity extends AppCompatActivity implements CartAdapter.CartItemListener {

    private RecyclerView recyclerView;
    private CartAdapter cartAdapter;
    private List<CartItem> cartItemList = new ArrayList<>();
    private TextView textViewTotalSummary;
    private AppDatabase db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cart);

        recyclerView = findViewById(R.id.recyclerViewCart);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        textViewTotalSummary = findViewById(R.id.textViewTotalSummary);
        Button buttonCheckout = findViewById(R.id.buttonCheckout);

        db = AppDatabase.getInstance(getApplicationContext());

        cartAdapter = new CartAdapter(this, cartItemList, this);
        recyclerView.setAdapter(cartAdapter);

        loadCartItems();

        // Checkout button listener
        buttonCheckout.setOnClickListener(v -> {
            FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
            if (user == null) {
                Toast.makeText(this, "Please log in to proceed with checkout", Toast.LENGTH_SHORT).show();
                Intent loginIntent = new Intent(this, LoginActivity.class);
                loginIntent.putExtra("redirectAfterLogin", "checkout");
                startActivity(loginIntent);
            } else {
                handleCheckout();
            }
        });

        // Location icon click
        LinearLayout locationContainer = findViewById(R.id.locationContainer);
        locationContainer.setOnClickListener(v -> showLocationPopup());

        // Back icon click
        ImageView iconBack = findViewById(R.id.iconBack);
        iconBack.setOnClickListener(v -> onBackPressed());

        // Profile icon click → open ProfileActivity
        ImageView profileIcon = findViewById(R.id.iconProfile);
        profileIcon.setOnClickListener(v -> {
            if (FirebaseAuth.getInstance().getCurrentUser() == null) {
                startActivity(new Intent(CartActivity.this, LoginActivity.class));
            } else {
                String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();
                FirebaseFirestore.getInstance()
                        .collection("users")
                        .document(uid)
                        .get()
                        .addOnSuccessListener(documentSnapshot -> {
                            if (documentSnapshot.exists()) {
                                String role = documentSnapshot.getString("role");
                                if (role == null) {
                                    Toast.makeText(CartActivity.this, "User role not defined", Toast.LENGTH_SHORT).show();
                                    return;
                                }

                                switch (role) {
                                    case "admin":
                                        startActivity(new Intent(CartActivity.this, com.example.furniturestore.dashboard.AdminDashboardActivity.class));
                                        break;
                                    case "customer":
                                        startActivity(new Intent(CartActivity.this, com.example.furniturestore.dashboard.CustomerDashboardActivity.class));
                                        break;
                                    case "seller":
                                        startActivity(new Intent(CartActivity.this, com.example.furniturestore.dashboard.SellerDashboardActivity.class));
                                        break;
                                    default:
                                        Toast.makeText(CartActivity.this, "Unknown user role", Toast.LENGTH_SHORT).show();
                                }
                            } else {
                                Toast.makeText(CartActivity.this, "User data not found", Toast.LENGTH_SHORT).show();
                            }
                        })
                        .addOnFailureListener(e ->
                                Toast.makeText(CartActivity.this, "Failed to fetch user role: " + e.getMessage(), Toast.LENGTH_SHORT).show());
            }
        });


        // If started from login redirect for checkout
        if (getIntent() != null && getIntent().getBooleanExtra("startCheckout", false)) {
            FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
            if (user == null) {
                Toast.makeText(this, "Please log in to proceed with checkout", Toast.LENGTH_SHORT).show();
                Intent loginIntent = new Intent(this, LoginActivity.class);
                loginIntent.putExtra("redirectAfterLogin", "checkout");
                startActivity(loginIntent);
            } else {
                handleCheckout();
            }
        }
    }

    private void showLocationPopup() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_location, null);
        TextView addressText = dialogView.findViewById(R.id.addressText);
        addressText.setText("121 Queen Street, Auckland");
        builder.setView(dialogView);
        AlertDialog dialog = builder.create();

        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.WHITE));
        }

        dialog.show();
    }

    private void handleCheckout() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) {
            Toast.makeText(this, "Please log in to proceed with checkout", Toast.LENGTH_SHORT).show();
            Intent loginIntent = new Intent(CartActivity.this, LoginActivity.class);
            loginIntent.putExtra("redirectAfterLogin", "checkout");
            startActivity(loginIntent);
            return;
        }

        // ONLY navigate to CheckoutActivity
        Intent intent = new Intent(CartActivity.this, CheckoutActivity.class);
        startActivity(intent);
    }

    private void loadCartItems() {
        String userId = FirebaseAuth.getInstance().getCurrentUser() != null
                ? FirebaseAuth.getInstance().getCurrentUser().getUid()
                : "guest_user";

        new Thread(() -> {
            List<CartItem> items = db.cartDao().getCartItemsForUser(userId);
            runOnUiThread(() -> {
                cartItemList.clear();
                cartItemList.addAll(items);
                cartAdapter.notifyDataSetChanged();
                calculateTotalSummary();
            });
        }).start();
    }

    private void calculateTotalSummary() {
        double totalPrice = 0;
        int totalQuantity = 0;

        for (CartItem item : cartItemList) {
            totalPrice += item.getPrice() * item.getQuantity();
            totalQuantity += item.getQuantity();
        }

        double deliveryCost = 0;
        if (totalQuantity > 0) {
            deliveryCost = 50 + (Math.max(0, totalQuantity - 1)) * 20;
        }

        double grandTotal = totalPrice + deliveryCost;

        String summaryText = String.format(
                "Items: %d\nTotal: $%.2f\nDelivery: $%.2f\nGrand Total: $%.2f",
                totalQuantity, totalPrice, deliveryCost, grandTotal);

        textViewTotalSummary.setText(summaryText);
    }

    // CartAdapter.CartItemListener methods

    @Override
    public void onIncreaseQuantity(CartItem item) {
        item.setQuantity(item.getQuantity() + 1);
        updateCartItem(item);
    }

    @Override
    public void onDecreaseQuantity(CartItem item) {
        if (item.getQuantity() > 1) {
            item.setQuantity(item.getQuantity() - 1);
            updateCartItem(item);
        } else {
            onRemoveItem(item);
        }
    }

    @Override
    public void onRemoveItem(CartItem item) {
        new Thread(() -> {
            db.cartDao().deleteCartItem(item);
            runOnUiThread(() -> {
                cartItemList.remove(item);
                cartAdapter.notifyDataSetChanged();
                calculateTotalSummary();
                Toast.makeText(CartActivity.this, "Item removed", Toast.LENGTH_SHORT).show();
            });
        }).start();
    }

    @Override
    public void onQuantityChanged() {
        calculateTotalSummary();
    }

    private void updateCartItem(CartItem item) {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) {
            item.setUserId(user.getUid());
            new Thread(() -> {
                db.cartDao().updateCartItem(item);
                runOnUiThread(() -> {
                    cartAdapter.notifyDataSetChanged();
                    calculateTotalSummary();
                });
            }).start();
        }
    }
}
