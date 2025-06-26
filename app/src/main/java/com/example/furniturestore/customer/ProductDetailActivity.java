package com.example.furniturestore.customer;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.*;

import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager2.widget.ViewPager2;

import com.example.furniturestore.MainActivity;
import com.example.furniturestore.R;
import com.example.furniturestore.adapters.ImageSliderAdapter;
import com.example.furniturestore.database.AppDatabase;
import com.example.furniturestore.models.CartItem;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;

public class ProductDetailActivity extends AppCompatActivity {

    private ViewPager2 viewPagerProductImages;
    private TabLayout tabLayoutIndicator;
    private ImageView backIcon, homeIcon, cartIcon, profileIcon;
    private ImageButton btnPrevImage, btnNextImage;
    private TextView nameText, categoryText, priceText, descriptionText, termsText, quantityValue;
    private Button addToCartBtn, buttonIncrease, buttonDecrease;

    private int quantity = 1;
    private int maxQuantity = 5;
    private final int minQuantity = 1;

    private String productId, name, category, description, sellerId;
    private double price;
    private int productQuantity;
    private ArrayList<String> imageUrls;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_product_detail);

        // Initialize Views
        viewPagerProductImages = findViewById(R.id.viewPagerProductImages);
        tabLayoutIndicator = findViewById(R.id.tabLayoutIndicator);
        backIcon = findViewById(R.id.backIcon);
        homeIcon = findViewById(R.id.homeIcon);
        cartIcon = findViewById(R.id.cartIcon);
        profileIcon = findViewById(R.id.profileIcon);
        btnPrevImage = findViewById(R.id.btnPrevImage);
        btnNextImage = findViewById(R.id.btnNextImage);
        nameText = findViewById(R.id.textViewProductNameDetail);
        categoryText = findViewById(R.id.textViewProductCategoryDetail);
        priceText = findViewById(R.id.textViewProductPriceDetail);
        descriptionText = findViewById(R.id.textViewProductDescriptionDetail);
        termsText = findViewById(R.id.textViewTerms);
        addToCartBtn = findViewById(R.id.buttonAddToCart);
        buttonIncrease = findViewById(R.id.buttonIncrease);
        buttonDecrease = findViewById(R.id.buttonDecrease);
        quantityValue = findViewById(R.id.textViewQuantityValue);

        // Handle status bar padding if needed
        final View topBar = findViewById(R.id.topBar);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            topBar.setOnApplyWindowInsetsListener((v, insets) -> {
                int statusBarHeight = insets.getSystemWindowInsetTop();
                v.setPadding(v.getPaddingLeft(), statusBarHeight, v.getPaddingRight(), v.getPaddingBottom());
                return insets.consumeSystemWindowInsets();
            });
            topBar.requestApplyInsets();
        }

        // Get product data from Intent
        Intent intent = getIntent();
        productId = intent.getStringExtra("productId");
        name = intent.getStringExtra("productName");
        category = intent.getStringExtra("productCategory");
        price = intent.getDoubleExtra("productPrice", 0.0);
        description = intent.getStringExtra("productDescription");
        productQuantity = intent.getIntExtra("productQuantity", 0);
        sellerId = intent.getStringExtra("productSellerId");
        imageUrls = intent.getStringArrayListExtra("productImageUrls");
        if (imageUrls == null) imageUrls = new ArrayList<>();

        maxQuantity = productQuantity;

        // Setup image slider
        ImageSliderAdapter adapter = new ImageSliderAdapter(this, imageUrls);
        viewPagerProductImages.setAdapter(adapter);
        new TabLayoutMediator(tabLayoutIndicator, viewPagerProductImages, (tab, position) -> {}).attach();

        viewPagerProductImages.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int position) {
                btnPrevImage.setEnabled(position > 0);
                btnNextImage.setEnabled(position < adapter.getItemCount() - 1);
            }
        });

        btnPrevImage.setOnClickListener(v -> {
            int currentItem = viewPagerProductImages.getCurrentItem();
            if (currentItem > 0) viewPagerProductImages.setCurrentItem(currentItem - 1, true);
        });

        btnNextImage.setOnClickListener(v -> {
            int currentItem = viewPagerProductImages.getCurrentItem();
            if (currentItem < adapter.getItemCount() - 1) viewPagerProductImages.setCurrentItem(currentItem + 1, true);
        });

        // Set UI data
        nameText.setText(name);
        categoryText.setText(category);
        priceText.setText(String.format("$%.2f", price));
        descriptionText.setText(description);
        quantityValue.setText(String.valueOf(quantity));

        if (productQuantity <= 0) {
            addToCartBtn.setEnabled(false);
            addToCartBtn.setText("Out of Stock");
        }

        // Quantity controls
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

        // Add to Cart logic
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
                            String finalSellerId = (sellerId != null && !sellerId.isEmpty()) ? sellerId : "DEFAULT_SELLER_ID";
                            insertCartItem(userId, finalSellerId);
                        })
                        .addOnFailureListener(e -> {
                            Toast.makeText(this, "Failed to get role. Using default seller.", Toast.LENGTH_SHORT).show();
                            insertCartItem(userId, "DEFAULT_SELLER_ID");
                        });
            } else {
                insertCartItem(userId, "DEFAULT_SELLER_ID");
            }
        });

        // Navigation
        backIcon.setOnClickListener(v -> finish());

        homeIcon.setOnClickListener(v -> {
            Intent intentHome = new Intent(ProductDetailActivity.this, MainActivity.class);
            intentHome.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intentHome);
            finish();
        });

        cartIcon.setOnClickListener(v -> startActivity(new Intent(this, CartActivity.class)));

        profileIcon.setOnClickListener(v -> {
            FirebaseAuth auth = FirebaseAuth.getInstance();
            if (auth.getCurrentUser() == null) {
                startActivity(new Intent(this, com.example.furniturestore.auth.LoginActivity.class));
            } else {
                String uid = auth.getCurrentUser().getUid();
                FirebaseFirestore.getInstance()
                        .collection("users")
                        .document(uid)
                        .get()
                        .addOnSuccessListener(documentSnapshot -> {
                            if (documentSnapshot.exists()) {
                                String role = documentSnapshot.getString("role");
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
    }

    private void insertCartItem(String userId, String resolvedSellerId) {
        String firstImage = (imageUrls != null && !imageUrls.isEmpty()) ? imageUrls.get(0) : "";
        CartItem cartItem = new CartItem(userId, productId, name, quantity, price, firstImage, resolvedSellerId);

        new Thread(() -> {
            AppDatabase db = AppDatabase.getInstance(getApplicationContext());
            db.cartDao().insertCartItem(cartItem);
            runOnUiThread(() -> Toast.makeText(this, "Added to cart", Toast.LENGTH_SHORT).show());
        }).start();
    }
}
