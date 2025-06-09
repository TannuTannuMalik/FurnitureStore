package com.example.furniturestore.dashboard;

import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.*;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.furniturestore.R;
import com.example.furniturestore.adapters.ProductAdapter;
import com.example.furniturestore.models.Product;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

public class SellerDashboardActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private ProductAdapter adapter;
    private final List<Product> productList = new ArrayList<>();
    private FirebaseFirestore db;
    private FirebaseAuth auth;

    private EditText nameInput, categoryInput, imageInput, priceInput, descInput;
    private Button addProductBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_seller_dashboard);

        db = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();

        recyclerView = findViewById(R.id.recyclerViewSeller);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new ProductAdapter(productList,
                this::showUpdateDialog,
                this::deleteProduct);
        recyclerView.setAdapter(adapter);

        nameInput = findViewById(R.id.editTextProductName);
        categoryInput = findViewById(R.id.editTextCategory);
        imageInput = findViewById(R.id.editTextImageUrl);
        priceInput = findViewById(R.id.editTextPrice);
        descInput = findViewById(R.id.editTextDescription);
        addProductBtn = findViewById(R.id.buttonAddProduct);

        addProductBtn.setOnClickListener(v -> addProduct());

        loadSellerProducts();
    }

    private void addProduct() {
        String name = nameInput.getText().toString().trim();
        String category = categoryInput.getText().toString().trim();
        String imageUrl = imageInput.getText().toString().trim();
        String priceStr = priceInput.getText().toString().trim();
        String description = descInput.getText().toString().trim();

        if (TextUtils.isEmpty(name) || TextUtils.isEmpty(category) || TextUtils.isEmpty(priceStr)) {
            Toast.makeText(this, "Name, Category, and Price are required", Toast.LENGTH_SHORT).show();
            return;
        }

        double price = Double.parseDouble(priceStr);
        String id = db.collection("products").document().getId();
        String sellerId = auth.getCurrentUser().getUid();

        Product product = new Product(id, name, category, imageUrl, price, description, sellerId);

        product.setSellerId(sellerId);

        db.collection("products").document(id)
                .set(product)
                .addOnSuccessListener(unused -> {
                    Toast.makeText(this, "Product added", Toast.LENGTH_SHORT).show();
                    clearFields();
                    loadSellerProducts();
                })
                .addOnFailureListener(e ->
                        Toast.makeText(this, "Failed to add product", Toast.LENGTH_SHORT).show());
    }

    private void loadSellerProducts() {
        String sellerId = auth.getCurrentUser().getUid();

        db.collection("products")
                .whereEqualTo("sellerId", sellerId)
                .get()
                .addOnSuccessListener(snapshot -> {
                    productList.clear();
                    for (QueryDocumentSnapshot doc : snapshot) {
                        Product product = doc.toObject(Product.class);
                        productList.add(product);
                    }
                    adapter.notifyDataSetChanged();
                });
    }

    private void deleteProduct(Product product) {
        db.collection("products").document(product.getId())
                .delete()
                .addOnSuccessListener(unused -> {
                    Toast.makeText(this, "Deleted", Toast.LENGTH_SHORT).show();
                    loadSellerProducts();
                });
    }

    private void showUpdateDialog(Product product) {
        View view = LayoutInflater.from(this).inflate(R.layout.dialog_update_product, null);

        EditText name = view.findViewById(R.id.editTextProductName);
        EditText category = view.findViewById(R.id.editTextCategory);
        EditText image = view.findViewById(R.id.editTextImageUrl);
        EditText price = view.findViewById(R.id.editTextPrice);
        EditText desc = view.findViewById(R.id.editTextDescription);

        name.setText(product.getName());
        category.setText(product.getCategory());
        image.setText(product.getImageUrl());
        price.setText(String.valueOf(product.getPrice()));
        desc.setText(product.getDescription());

        new AlertDialog.Builder(this)
                .setTitle("Update Product")
                .setView(view)
                .setPositiveButton("Save", (dialog, which) -> {
                    product.setName(name.getText().toString());
                    product.setCategory(category.getText().toString());
                    product.setImageUrl(image.getText().toString());
                    product.setPrice(Double.parseDouble(price.getText().toString()));
                    product.setDescription(desc.getText().toString());

                    db.collection("products").document(product.getId())
                            .set(product)
                            .addOnSuccessListener(unused -> {
                                Toast.makeText(this, "Updated", Toast.LENGTH_SHORT).show();
                                loadSellerProducts();
                            });
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void clearFields() {
        nameInput.setText("");
        categoryInput.setText("");
        imageInput.setText("");
        priceInput.setText("");
        descInput.setText("");
    }
}
