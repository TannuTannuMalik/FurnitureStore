package com.example.furniturestore;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.furniturestore.adapters.CategoryAdapter;
import com.example.furniturestore.adapters.ProductAdapter;
import com.example.furniturestore.auth.LoginActivity;
import com.example.furniturestore.customer.CartActivity;
import com.example.furniturestore.customer.ProductDetailActivity;
import com.example.furniturestore.customer.ProductListActivity;
import com.example.furniturestore.dashboard.AdminDashboardActivity;
import com.example.furniturestore.dashboard.CustomerDashboardActivity;
import com.example.furniturestore.dashboard.SellerDashboardActivity;
import com.example.furniturestore.models.Category;
import com.example.furniturestore.models.Product;
import com.example.furniturestore.settings.ProfileActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";

    private RecyclerView furnitureRecyclerView, categoryRecyclerView;
    private ProductAdapter productAdapter;
    private CategoryAdapter categoryAdapter;
    private final List<Product> allProducts = new ArrayList<>();
    private final List<Product> productList = new ArrayList<>();
    private final List<Category> categoryList = new ArrayList<>();
    private FirebaseFirestore db;

    private Button buttonLogin, buttonRegister, buttonCart;
    private ImageView homeIcon, iconProfile, brandLogo;
    private TextView termsAndConditions;

    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main); // Ensure layout has brandLogo ImageView

        mAuth = FirebaseAuth.getInstance();

        // Initialize views
        furnitureRecyclerView = findViewById(R.id.furnitureRecyclerView);
        categoryRecyclerView = findViewById(R.id.categoryRecyclerView);
        buttonLogin = findViewById(R.id.buttonLogin);
        buttonRegister = findViewById(R.id.buttonRegister);
        buttonCart = findViewById(R.id.buttonCart);
        homeIcon = findViewById(R.id.homeIcon);
        iconProfile = findViewById(R.id.iconProfile);
        termsAndConditions = findViewById(R.id.termsAndConditions);
        brandLogo = findViewById(R.id.brandLogo);

        // Layout managers
        furnitureRecyclerView.setLayoutManager(new GridLayoutManager(this, 2));
        categoryRecyclerView.setLayoutManager(new GridLayoutManager(this, 2));

        // Product adapter
        productAdapter = new ProductAdapter(productList,
                product -> {
                    Intent intent = new Intent(MainActivity.this, ProductDetailActivity.class);
                    intent.putExtra("productId", product.getId());
                    intent.putExtra("productName", product.getName());
                    intent.putExtra("productCategory", product.getCategory());
                    intent.putExtra("productPrice", product.getPrice());
                    intent.putExtra("productDescription", product.getDescription());
                    intent.putExtra("productImageUrl", product.getImageUrl());
                    startActivity(intent);
                },
                product -> {
                    // Customers shouldn't delete products
                },
                false
        );

        furnitureRecyclerView.setAdapter(productAdapter);

        setupCategories();
        db = FirebaseFirestore.getInstance();
        loadProducts();

        // Show/hide login/profile based on auth status
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null) {
            buttonLogin.setVisibility(View.GONE);
            buttonRegister.setVisibility(View.GONE);
            iconProfile.setVisibility(View.VISIBLE);
        } else {
            buttonLogin.setVisibility(View.VISIBLE);
            buttonRegister.setVisibility(View.VISIBLE);
            iconProfile.setVisibility(View.GONE);
        }

        // Click listeners
        buttonLogin.setOnClickListener(v ->
                startActivity(new Intent(MainActivity.this, LoginActivity.class))
        );

        buttonRegister.setOnClickListener(v ->
                startActivity(new Intent(MainActivity.this, LoginActivity.class))
        );

        buttonCart.setOnClickListener(v ->
                startActivity(new Intent(MainActivity.this, CartActivity.class))
        );

        homeIcon.setOnClickListener(v ->
                Toast.makeText(this, "Already on Home", Toast.LENGTH_SHORT).show()
        );

        iconProfile.setOnClickListener(v -> {
            FirebaseUser user = mAuth.getCurrentUser();
            if (user == null) {
                startActivity(new Intent(MainActivity.this, LoginActivity.class));
            } else {
                String uid = user.getUid();
                FirebaseFirestore.getInstance().collection("users").document(uid).get()
                        .addOnSuccessListener(documentSnapshot -> {
                            if (documentSnapshot.exists()) {
                                String role = documentSnapshot.getString("role");
                                if (role == null) {
                                    Toast.makeText(MainActivity.this, "User role not defined", Toast.LENGTH_SHORT).show();
                                    return;
                                }
                                switch (role) {
                                    case "admin":
                                        startActivity(new Intent(MainActivity.this, ProfileActivity.class));
                                        break;
                                    case "customer":
                                        startActivity(new Intent(MainActivity.this, CustomerDashboardActivity.class));
                                        break;
                                    case "seller":
                                        startActivity(new Intent(MainActivity.this, SellerDashboardActivity.class));
                                        break;
                                    default:
                                        Toast.makeText(MainActivity.this, "Unknown user role", Toast.LENGTH_SHORT).show();
                                }
                            } else {
                                Toast.makeText(MainActivity.this, "User data not found", Toast.LENGTH_SHORT).show();
                            }
                        })
                        .addOnFailureListener(e -> {
                            Toast.makeText(MainActivity.this, "Failed to get user role: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                        });
            }
        });

        // Optional: Click on brand logo
        brandLogo.setOnClickListener(v ->
                Toast.makeText(this, "Welcome to ReCozy Living!", Toast.LENGTH_SHORT).show()
        );

        termsAndConditions.setOnClickListener(v -> showTermsDialog());
    }

    private void showTermsDialog() {
        new AlertDialog.Builder(this)
                .setTitle("Terms and Conditions")
                .setMessage("These are the Terms and Conditions of using ReCozy Living Furniture. You agree to use the app responsibly and comply with our privacy and product usage policies.")
                .setPositiveButton("OK", (dialog, which) -> dialog.dismiss())
                .show();
    }

    private void setupCategories() {
        categoryList.clear();
        categoryList.add(new Category("TallBoy", R.drawable.tallboy));
        categoryList.add(new Category("Couch", R.drawable.couch));
        categoryList.add(new Category("Bedside Drawers", R.drawable.bedside_drawers));
        categoryList.add(new Category("Dining Table", R.drawable.dining_table));
        categoryList.add(new Category("Beds", R.drawable.beds));
        categoryList.add(new Category("Hallway Table", R.drawable.hallway_table));
        categoryList.add(new Category("Chairs", R.drawable.chairs));
        categoryList.add(new Category("Cupboard", R.drawable.cupboards));

        categoryAdapter = new CategoryAdapter(categoryList, categoryName -> {
            Intent intent = new Intent(MainActivity.this, ProductListActivity.class);
            intent.putExtra("categoryName", categoryName);
            startActivity(intent);
        });
        categoryRecyclerView.setAdapter(categoryAdapter);
    }

    private void loadProducts() {
        db.collection("products").get()
                .addOnSuccessListener(qs -> {
                    allProducts.clear();
                    for (QueryDocumentSnapshot doc : qs) {
                        Product p = doc.toObject(Product.class);
                        if (p != null) allProducts.add(p);
                    }
                    productList.clear();
                    productList.addAll(allProducts);
                    productAdapter.updateList(new ArrayList<>(productList));
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Failed to load products", Toast.LENGTH_SHORT).show();
                    Log.e(TAG, "Error loading products", e);
                });
    }
}
