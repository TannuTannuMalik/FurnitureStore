package com.example.furniturestore.customer;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.example.furniturestore.MainActivity;
import com.example.furniturestore.R;
import com.example.furniturestore.TermsConditionsActivity;
import com.example.furniturestore.database.AppDatabase;
import com.example.furniturestore.models.CartItem;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

public class ProductDetailActivity extends AppCompatActivity {

    private ImageView imageView, backIcon, homeIcon, cartIcon, profileIcon;
    private TextView nameText, categoryText, priceText, descriptionText, termsText, quantityValue;
    private Button addToCartBtn, buttonIncrease, buttonDecrease, wishlistBtn;

    private int quantity = 1;
    private int maxQuantity = 5;
    private final int minQuantity = 1;

    private String productId, name, category, description, imageUrl, sellerId;
    private double price;
    private int productQuantity;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_product_detail);

        final View topBar = findViewById(R.id.topBar);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            topBar.setOnApplyWindowInsetsListener((v, insets) -> {
                int statusBarHeight = insets.getSystemWindowInsetTop();
                v.setPadding(v.getPaddingLeft(), statusBarHeight, v.getPaddingRight(), v.getPaddingBottom());
                return insets.consumeSystemWindowInsets();
            });
            topBar.requestApplyInsets();
        }

        imageView = findViewById(R.id.imageViewProductDetail);
        backIcon = findViewById(R.id.backIcon);
        homeIcon = findViewById(R.id.homeIcon);
        cartIcon = findViewById(R.id.cartIcon);
        profileIcon = findViewById(R.id.profileIcon);
        wishlistBtn = findViewById(R.id.buttonAddToWishlist);
        nameText = findViewById(R.id.textViewProductNameDetail);
        categoryText = findViewById(R.id.textViewProductCategoryDetail);
        priceText = findViewById(R.id.textViewProductPriceDetail);
        descriptionText = findViewById(R.id.textViewProductDescriptionDetail);
        termsText = findViewById(R.id.textViewTerms);
        addToCartBtn = findViewById(R.id.buttonAddToCart);
        buttonIncrease = findViewById(R.id.buttonIncrease);
        buttonDecrease = findViewById(R.id.buttonDecrease);
        quantityValue = findViewById(R.id.textViewQuantityValue);

        // Get product details from intent
        productId = getIntent().getStringExtra("productId");
        name = getIntent().getStringExtra("productName");
        category = getIntent().getStringExtra("productCategory");
        price = getIntent().getDoubleExtra("productPrice", 0.0);
        description = getIntent().getStringExtra("productDescription");
        imageUrl = getIntent().getStringExtra("productImageUrl");
        productQuantity = getIntent().getIntExtra("productQuantity", 0);
        sellerId = getIntent().getStringExtra("productSellerId");

        maxQuantity = productQuantity;

        // Set product info
        nameText.setText(name);
        categoryText.setText(category);
        priceText.setText(String.format("$%.2f", price));
        descriptionText.setText(description);
        Glide.with(this).load(imageUrl).into(imageView);
        quantityValue.setText(String.valueOf(quantity));

        if (productQuantity <= 0) {
            addToCartBtn.setEnabled(false);
            addToCartBtn.setText("Out of Stock");
        }

        // Quantity increase/decrease
        buttonIncrease.setOnClickListener(v -> {
            if (quantity < maxQuantity) {
                quantity++;
                quantityValue.setText(String.valueOf(quantity));
            } else {
                Toast.makeText(this, "Only " + maxQuantity + " in stock", Toast.LENGTH_SHORT).show();
            }
        });

        buttonDecrease.setOnClickListener(v -> {
            if (quantity > minQuantity) {
                quantity--;
                quantityValue.setText(String.valueOf(quantity));
            }
        });

        addToCartBtn.setOnClickListener(v -> {
            FirebaseAuth auth = FirebaseAuth.getInstance();
            String userId = auth.getCurrentUser() != null ? auth.getCurrentUser().getUid() : "guest_user";

            if (auth.getCurrentUser() != null) {
                FirebaseFirestore.getInstance()
                        .collection("users")
                        .document(userId)
                        .get()
                        .addOnSuccessListener(documentSnapshot -> {
                            String role = documentSnapshot.getString("role");
                            String finalSellerId;

                            if ("seller".equals(role)) {
                                finalSellerId = userId;
                            } else if (sellerId != null && !sellerId.trim().isEmpty()) {
                                finalSellerId = sellerId;
                            } else {
                                finalSellerId = "D7KYKRmEHmfSS0VP9yRp8241OoB3"; // Default
                            }

                            insertCartItem(userId, finalSellerId);
                        })
                        .addOnFailureListener(e -> {
                            Toast.makeText(this, "Failed to get role. Using default seller.", Toast.LENGTH_SHORT).show();
                            insertCartItem(userId, "D7KYKRmEHmfSS0VP9yRp8241OoB3");
                        });
            } else {
                insertCartItem(userId, "D7KYKRmEHmfSS0VP9yRp8241OoB3");
            }
        });

        // Navigation
        backIcon.setOnClickListener(v -> finish());

        homeIcon.setOnClickListener(v -> {
            Intent intent = new Intent(ProductDetailActivity.this, MainActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
            finish();
        });

        cartIcon.setOnClickListener(v -> startActivity(new Intent(this, CartActivity.class)));

        profileIcon.setOnClickListener(v -> {
            if (FirebaseAuth.getInstance().getCurrentUser() == null) {
                startActivity(new Intent(this, com.example.furniturestore.auth.LoginActivity.class));
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
                                    Toast.makeText(this, "User role not defined", Toast.LENGTH_SHORT).show();
                                    return;
                                }

                                switch (role) {
                                    case "admin":
                                        startActivity(new Intent(this, com.example.furniturestore.dashboard.AdminDashboardActivity.class));
                                        break;
                                    case "customer":
                                        startActivity(new Intent(this, com.example.furniturestore.dashboard.CustomerDashboardActivity.class));
                                        break;
                                    case "seller":
                                        startActivity(new Intent(this, com.example.furniturestore.dashboard.SellerDashboardActivity.class));
                                        break;
                                    default:
                                        Toast.makeText(this, "Unknown role", Toast.LENGTH_SHORT).show();
                                }
                            } else {
                                Toast.makeText(this, "User data not found", Toast.LENGTH_SHORT).show();
                            }
                        })
                        .addOnFailureListener(e -> Toast.makeText(this, "Error fetching role", Toast.LENGTH_SHORT).show());
            }
        });

        wishlistBtn.setOnClickListener(v -> Toast.makeText(this, "Added to wishlist (not implemented)", Toast.LENGTH_SHORT).show());

        termsText.setOnClickListener(v -> startActivity(new Intent(this, TermsConditionsActivity.class)));
    }

    private void insertCartItem(String userId, String resolvedSellerId) {
        CartItem cartItem = new CartItem(
                userId,
                productId,
                name,
                quantity,
                price,
                imageUrl,
                resolvedSellerId
        );

        new Thread(() -> {
            AppDatabase db = AppDatabase.getInstance(getApplicationContext());
            db.cartDao().insertCartItem(cartItem);
            runOnUiThread(() -> Toast.makeText(this, "Added to cart", Toast.LENGTH_SHORT).show());
        }).start();
    }
}
