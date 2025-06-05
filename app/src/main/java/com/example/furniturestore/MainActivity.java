package com.example.furniturestore;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.*;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.furniturestore.adapters.ProductAdapter;
import com.example.furniturestore.models.Product;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";

    private RecyclerView furnitureRecyclerView;
    private ProductAdapter productAdapter;
    private final List<Product> productList = new ArrayList<>();
    private FirebaseFirestore db;

    private EditText editTextProductName, editTextCategory, editTextImageUrl, editTextPrice, editTextDescription;
    private Button buttonAddProduct;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Initialize views
        editTextProductName = findViewById(R.id.editTextProductName);
        editTextCategory = findViewById(R.id.editTextCategory);
        editTextImageUrl = findViewById(R.id.editTextImageUrl);
        editTextPrice = findViewById(R.id.editTextPrice);
        editTextDescription = findViewById(R.id.editTextDescription);
        buttonAddProduct = findViewById(R.id.buttonAddProduct);

        setupRecyclerView();
        db = FirebaseFirestore.getInstance();

        loadProducts();

        // Add product button click listener
        buttonAddProduct.setOnClickListener(v -> addProduct());
    }

    private void setupRecyclerView() {
        furnitureRecyclerView = findViewById(R.id.furnitureRecyclerView);
        furnitureRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        productAdapter = new ProductAdapter(productList,
                product -> showUpdateDialog(product),
                product -> deleteProduct(product));
        furnitureRecyclerView.setAdapter(productAdapter);
    }

    private void addProduct() {
        String name = editTextProductName.getText().toString().trim();
        String category = editTextCategory.getText().toString().trim();
        String imageUrl = editTextImageUrl.getText().toString().trim();
        String priceStr = editTextPrice.getText().toString().trim();
        String description = editTextDescription.getText().toString().trim();

        if (name.isEmpty() || category.isEmpty() || priceStr.isEmpty()) {
            Toast.makeText(this, "Please enter name, category, and price", Toast.LENGTH_SHORT).show();
            return;
        }

        double price;
        try {
            price = Double.parseDouble(priceStr);
        } catch (NumberFormatException e) {
            Toast.makeText(this, "Invalid price", Toast.LENGTH_SHORT).show();
            return;
        }

        // Generate unique ID for the product document
        String productId = db.collection("products").document().getId();

        Product newProduct = new Product(productId, name, category, imageUrl, price, description);

        db.collection("products")
                .document(productId)
                .set(newProduct)
                .addOnSuccessListener(unused -> {
                    Toast.makeText(this, "Product added successfully", Toast.LENGTH_SHORT).show();
                    clearInputFields();
                    loadProducts();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Failed to add product: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    Log.e(TAG, "Error adding product", e);
                });
    }

    private void showUpdateDialog(Product product) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Update Product");

        View viewInflated = LayoutInflater.from(this).inflate(R.layout.dialog_update_product, null);
        final EditText inputName = viewInflated.findViewById(R.id.editTextProductName);
        final EditText inputCategory = viewInflated.findViewById(R.id.editTextCategory);
        final EditText inputImageUrl = viewInflated.findViewById(R.id.editTextImageUrl);
        final EditText inputPrice = viewInflated.findViewById(R.id.editTextPrice);
        final EditText inputDescription = viewInflated.findViewById(R.id.editTextDescription);

        // Pre-fill values
        inputName.setText(product.getName());
        inputCategory.setText(product.getCategory());
        inputImageUrl.setText(product.getImageUrl());
        inputPrice.setText(String.valueOf(product.getPrice()));
        inputDescription.setText(product.getDescription());

        builder.setView(viewInflated);

        builder.setPositiveButton("Save", (dialog, which) -> {
            String name = inputName.getText().toString().trim();
            String category = inputCategory.getText().toString().trim();
            String imageUrl = inputImageUrl.getText().toString().trim();
            String priceStr = inputPrice.getText().toString().trim();
            String description = inputDescription.getText().toString().trim();

            if (name.isEmpty() || category.isEmpty() || priceStr.isEmpty()) {
                Toast.makeText(this, "Please enter name, category, and price", Toast.LENGTH_SHORT).show();
                return;
            }

            double price;
            try {
                price = Double.parseDouble(priceStr);
            } catch (NumberFormatException e) {
                Toast.makeText(this, "Invalid price", Toast.LENGTH_SHORT).show();
                return;
            }

            Product updatedProduct = new Product(product.getId(), name, category, imageUrl, price, description);

            db.collection("products").document(product.getId())
                    .set(updatedProduct)
                    .addOnSuccessListener(unused -> {
                        Toast.makeText(MainActivity.this, "Product updated", Toast.LENGTH_SHORT).show();
                        loadProducts(); // refresh list
                    })
                    .addOnFailureListener(e -> {
                        Toast.makeText(MainActivity.this, "Update failed", Toast.LENGTH_SHORT).show();
                        Log.e(TAG, "Update failed", e);
                    });
        });

        builder.setNegativeButton("Cancel", (dialog, which) -> dialog.cancel());

        builder.show();
    }

    private void deleteProduct(Product product) {
        db.collection("products").document(product.getId())
                .delete()
                .addOnSuccessListener(unused -> {
                    Toast.makeText(this, "Product deleted", Toast.LENGTH_SHORT).show();
                    loadProducts(); // refresh list
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Delete failed", Toast.LENGTH_SHORT).show();
                    Log.e(TAG, "Delete failed", e);
                });
    }

    private void clearInputFields() {
        editTextProductName.setText("");
        editTextCategory.setText("");
        editTextImageUrl.setText("");
        editTextPrice.setText("");
        editTextDescription.setText("");
    }

    private void loadProducts() {
        db.collection("products")
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    productList.clear();
                    for (QueryDocumentSnapshot doc : querySnapshot) {
                        Product product = doc.toObject(Product.class);
                        productList.add(product);
                    }
                    productAdapter.notifyDataSetChanged();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Failed to load products", Toast.LENGTH_SHORT).show();
                    Log.e(TAG, "Error loading products", e);
                });
    }
}
