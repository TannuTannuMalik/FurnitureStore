package com.example.furniturestore.customer;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import android.view.View;
import android.widget.AdapterView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.furniturestore.MainActivity;
import com.example.furniturestore.R;
import com.example.furniturestore.adapters.ProductAdapter;
import com.example.furniturestore.models.Product;
import com.example.furniturestore.settings.ProfileActivity;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class ProductListActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private ProductAdapter adapter;
    private List<Product> productList;
    private FirebaseFirestore firestore;

    private String selectedCategory;

    private TextView titleSearchResults, textTotalProducts;
    private Spinner spinnerSort;

    private static final String SORT_PRICE_LOW_HIGH = "Price: Low to High";
    private static final String SORT_PRICE_HIGH_LOW = "Price: High to Low";
    private static final String SORT_NEW_ARRIVALS = "New Arrivals";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_product_list);

        recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new GridLayoutManager(this, 2));

        titleSearchResults = findViewById(R.id.titleSearchResults);
        textTotalProducts = findViewById(R.id.textTotalProducts);
        spinnerSort = findViewById(R.id.spinnerSort);

        productList = new ArrayList<>();
        selectedCategory = getIntent().getStringExtra("categoryName");

        if (selectedCategory != null && !selectedCategory.isEmpty()) {
            titleSearchResults.setText("Search Results for \"" + selectedCategory + "\"");
        } else {
            titleSearchResults.setText("Search Results");
        }

        adapter = new ProductAdapter(productList,
                product -> {
                    Intent intent = new Intent(ProductListActivity.this, ProductDetailActivity.class);
                    intent.putExtra("productId", product.getId());
                    intent.putExtra("productName", product.getName());
                    intent.putExtra("productCategory", product.getCategory());
                    intent.putExtra("productPrice", product.getPrice());
                    intent.putExtra("productDescription", product.getDescription());
                    intent.putExtra("productImageUrl", product.getImageUrl());
                    intent.putExtra("productQuantity", product.getQuantity()); // ✅ This was missing

                    startActivity(intent);
                },
                product -> {
                    Toast.makeText(ProductListActivity.this, "Deleted: " + product.getName(), Toast.LENGTH_SHORT).show();
                    productList.remove(product);
                    adapter.notifyDataSetChanged();
                    updateTotalProductsCount();
                },
                false // <-- Added boolean argument for isAdminOrSeller
        );

        recyclerView.setAdapter(adapter);

        firestore = FirebaseFirestore.getInstance();

        setupSortSpinner();

        loadProductsFromFirestore();

        ImageView iconHome = findViewById(R.id.iconHome);
        ImageView iconProfile = findViewById(R.id.iconProfile);
        ImageView iconCart = findViewById(R.id.iconCart);

        iconHome.setOnClickListener(v -> {
            Intent intent = new Intent(ProductListActivity.this, MainActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
            finish();
        });

        iconProfile.setOnClickListener(v -> {
            if (com.google.firebase.auth.FirebaseAuth.getInstance().getCurrentUser() == null) {
                startActivity(new Intent(ProductListActivity.this, com.example.furniturestore.auth.LoginActivity.class));
            } else {
                String uid = com.google.firebase.auth.FirebaseAuth.getInstance().getCurrentUser().getUid();
                com.google.firebase.firestore.FirebaseFirestore.getInstance()
                        .collection("users")
                        .document(uid)
                        .get()
                        .addOnSuccessListener(documentSnapshot -> {
                            if (documentSnapshot.exists()) {
                                String role = documentSnapshot.getString("role");
                                if (role == null) {
                                    Toast.makeText(ProductListActivity.this, "User role not defined", Toast.LENGTH_SHORT).show();
                                    return;
                                }

                                switch (role) {
                                    case "admin":
                                        startActivity(new Intent(ProductListActivity.this, com.example.furniturestore.dashboard.AdminDashboardActivity.class));
                                        break;
                                    case "customer":
                                        startActivity(new Intent(ProductListActivity.this, com.example.furniturestore.dashboard.CustomerDashboardActivity.class));
                                        break;
                                    case "seller":
                                        startActivity(new Intent(ProductListActivity.this, com.example.furniturestore.dashboard.SellerDashboardActivity.class));
                                        break;
                                    default:
                                        Toast.makeText(ProductListActivity.this, "Unknown user role", Toast.LENGTH_SHORT).show();
                                }
                            } else {
                                Toast.makeText(ProductListActivity.this, "User data not found", Toast.LENGTH_SHORT).show();
                            }
                        })
                        .addOnFailureListener(e ->
                                Toast.makeText(ProductListActivity.this, "Failed to fetch user role: " + e.getMessage(), Toast.LENGTH_SHORT).show())
                ;
            }
        });


        iconCart.setOnClickListener(v -> {
            Intent intent = new Intent(ProductListActivity.this, CartActivity.class);
            startActivity(intent);
        });
    }

    private void setupSortSpinner() {
        ArrayAdapter<String> sortAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item,
                new String[]{SORT_PRICE_LOW_HIGH, SORT_PRICE_HIGH_LOW, SORT_NEW_ARRIVALS});
        sortAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerSort.setAdapter(sortAdapter);

        spinnerSort.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String selectedSort = (String) parent.getItemAtPosition(position);
                sortProductList(selectedSort);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                // no action
            }
        });
    }

    private void sortProductList(String sortBy) {
        switch (sortBy) {
            case SORT_PRICE_LOW_HIGH:
                Collections.sort(productList, Comparator.comparingDouble(Product::getPrice));
                break;
            case SORT_PRICE_HIGH_LOW:
                Collections.sort(productList, (p1, p2) -> Double.compare(p2.getPrice(), p1.getPrice()));
                break;
            case SORT_NEW_ARRIVALS:
                // Assuming Product has getCreatedAt() returning Comparable<Date> or Long timestamp
                Collections.sort(productList, (p1, p2) -> {
                    if (p1.getCreatedAt() == null || p2.getCreatedAt() == null) return 0;
                    return p2.getCreatedAt().compareTo(p1.getCreatedAt());
                });
                break;
        }
        adapter.notifyDataSetChanged();
    }

    private void updateTotalProductsCount() {
        textTotalProducts.setText("Products: " + productList.size());
    }

    private void loadProductsFromFirestore() {
        if (selectedCategory == null || selectedCategory.isEmpty()) {
            Toast.makeText(this, "No category selected", Toast.LENGTH_SHORT).show();
            return;
        }

        firestore.collection("products")
                .whereEqualTo("category", selectedCategory)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        productList.clear();
                        for (DocumentSnapshot doc : task.getResult()) {
                            Product product = doc.toObject(Product.class);
                            if (product != null) {
                                product.setId(doc.getId());
                                productList.add(product);
                            }
                        }
                        updateTotalProductsCount();
                        adapter.notifyDataSetChanged();
                    } else {
                        Toast.makeText(this, "Failed to load products.", Toast.LENGTH_SHORT).show();
                        Log.e("Firestore", "Error loading products", task.getException());
                    }
                });
    }
}
