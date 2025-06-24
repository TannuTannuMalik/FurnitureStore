package com.example.furniturestore.seller;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.furniturestore.R;
import com.example.furniturestore.adapters.ProductAdapter;
import com.example.furniturestore.models.Product;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.*;

import java.util.ArrayList;
import java.util.List;

public class ManageSellerProductsActivity extends AppCompatActivity {

    private FirebaseFirestore db;
    private FirebaseAuth auth;
    private RecyclerView recyclerView;
    private ProductAdapter adapter;
    private final List<Product> sellerProducts = new ArrayList<>();
    private TextView noItemsText;

    private static final String TAG = "ManageSellerProducts";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_manage_seller_products);

        db = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();

        recyclerView = findViewById(R.id.recyclerViewManageProducts);
        noItemsText = findViewById(R.id.textNoItems);

        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        // ✅ Updated adapter with full CRUD support
        adapter = new ProductAdapter(
                sellerProducts,
                product -> {
                    // Optional: view product details
                },
                product -> deleteProduct(product),
                product -> editProduct(product),
                true // isAdminOrSeller
        );

        recyclerView.setAdapter(adapter);
        loadSellerProducts();
    }

    private void loadSellerProducts() {
        String sellerId = auth.getCurrentUser() != null ? auth.getCurrentUser().getUid() : null;

        if (sellerId == null) {
            Toast.makeText(this, "Not authenticated", Toast.LENGTH_SHORT).show();
            Log.e(TAG, "Seller ID is null. User not authenticated.");
            return;
        }

        Log.d(TAG, "Loading products for sellerId: " + sellerId);

        db.collection("products")
                .whereEqualTo("sellerId", sellerId)
                .orderBy("createdAt", Query.Direction.DESCENDING)
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    sellerProducts.clear();
                    for (QueryDocumentSnapshot doc : querySnapshot) {
                        Product product = doc.toObject(Product.class);
                        product.setId(doc.getId());
                        sellerProducts.add(product);
                    }

                    adapter.notifyDataSetChanged();

                    if (sellerProducts.isEmpty()) {
                        noItemsText.setText("No products listed yet.");
                        noItemsText.setVisibility(TextView.VISIBLE);
                    } else {
                        noItemsText.setVisibility(TextView.GONE);
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error loading products: ", e);
                    Toast.makeText(this, "Error loading products: " + e.getMessage(), Toast.LENGTH_LONG).show();
                });
    }

    private void deleteProduct(Product product) {
        db.collection("products").document(product.getId())
                .delete()
                .addOnSuccessListener(unused -> {
                    Toast.makeText(this, "Product deleted", Toast.LENGTH_SHORT).show();
                    sellerProducts.remove(product);
                    adapter.notifyDataSetChanged();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Delete failed: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    private void editProduct(Product product) {
        // ✅ Replace with your actual EditProductActivity
        Intent intent = new Intent(this, EditProductActivity.class);
        intent.putExtra("productId", product.getId());
        startActivity(intent);
    }
}
