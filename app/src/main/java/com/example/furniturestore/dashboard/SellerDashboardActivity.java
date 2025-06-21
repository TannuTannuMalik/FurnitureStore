package com.example.furniturestore.dashboard;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.*;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.furniturestore.MainActivity;
import com.example.furniturestore.R;
import com.example.furniturestore.adapters.ProductAdapter;
import com.example.furniturestore.models.Product;
import com.example.furniturestore.seller.SellerOrdersActivity;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

public class SellerDashboardActivity extends AppCompatActivity {

    private static final int PICK_IMAGE_REQUEST = 1;

    private RecyclerView recyclerView;
    private ProductAdapter adapter;
    private final List<Product> productList = new ArrayList<>();
    private FirebaseFirestore db;
    private FirebaseAuth auth;
    private FirebaseStorage storage;

    private EditText nameInput, priceInput, descInput, quantityInput;
    private Spinner categorySpinner;
    private Button addProductBtn, logoutBtn, uploadImageBtn;
    private ImageView imagePreview;

    private Uri imageUri;
    private String uploadedImageUrl = "";

    private final String[] categories = {
            "Choose Category", "TallBoy", "Couch", "Bedside Drawers",
            "Dining Table", "Beds", "Hallway Table", "Chairs", "Cupboard"
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_seller_dashboard);

        db = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();
        storage = FirebaseStorage.getInstance();

        nameInput = findViewById(R.id.editTextProductName);
        priceInput = findViewById(R.id.editTextPrice);
        descInput = findViewById(R.id.editTextDescription);
        quantityInput = findViewById(R.id.editTextQuantity);
        categorySpinner = findViewById(R.id.spinnerCategory);
        addProductBtn = findViewById(R.id.buttonAddProduct);
        logoutBtn = findViewById(R.id.buttonLogout);
        uploadImageBtn = findViewById(R.id.buttonUploadImage);
        imagePreview = findViewById(R.id.imagePreview);
        recyclerView = findViewById(R.id.recyclerViewSeller);

        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new ProductAdapter(productList, this::showUpdateDialog, this::deleteProduct, true);
        recyclerView.setAdapter(adapter);

        ArrayAdapter<String> categoryAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, categories);
        categoryAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        categorySpinner.setAdapter(categoryAdapter);

        uploadImageBtn.setOnClickListener(v -> openImagePicker());

        addProductBtn.setOnClickListener(v -> addProduct());
        Button viewOrdersBtn = findViewById(R.id.buttonViewOrders);

        viewOrdersBtn.setOnClickListener(v -> {
            Intent intent = new Intent(SellerDashboardActivity.this, SellerOrdersActivity.class);
            startActivity(intent);
        });

        ImageView backButton = findViewById(R.id.buttonGoBack);
        backButton.setOnClickListener(v -> {
            startActivity(new Intent(this, MainActivity.class));
            finish();
        });

        logoutBtn.setOnClickListener(v -> {
            auth.signOut();
            Toast.makeText(this, "Logged out", Toast.LENGTH_SHORT).show();
            finish();
        });

        loadSellerProducts();
    }

    private void openImagePicker() {
        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setType("image/*");
        startActivityForResult(intent, PICK_IMAGE_REQUEST);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null && data.getData() != null) {
            imageUri = data.getData();
            imagePreview.setImageURI(imageUri);
            uploadImageToFirebase();
        }
    }

    private void uploadImageToFirebase() {
        if (imageUri == null) return;

        StorageReference storageRef = storage.getReference("product_images/" + UUID.randomUUID().toString());

        storageRef.putFile(imageUri)
                .addOnSuccessListener(taskSnapshot -> storageRef.getDownloadUrl().addOnSuccessListener(uri -> {
                    uploadedImageUrl = uri.toString();
                    Toast.makeText(this, "Image uploaded", Toast.LENGTH_SHORT).show();
                }))
                .addOnFailureListener(e -> Toast.makeText(this, "Image upload failed", Toast.LENGTH_SHORT).show());
    }

    private void addProduct() {
        String name = nameInput.getText().toString().trim();
        String priceStr = priceInput.getText().toString().trim();
        String quantityStr = quantityInput.getText().toString().trim();
        String description = descInput.getText().toString().trim();
        String category = categorySpinner.getSelectedItem().toString();

        if (TextUtils.isEmpty(name) || TextUtils.isEmpty(priceStr) || TextUtils.isEmpty(quantityStr)) {
            Toast.makeText(this, "Name, Price, and Quantity are required", Toast.LENGTH_SHORT).show();
            return;
        }

        if (TextUtils.isEmpty(uploadedImageUrl)) {
            Toast.makeText(this, "Please upload an image", Toast.LENGTH_SHORT).show();
            return;
        }

        if (category.equals("Choose Category")) {
            Toast.makeText(this, "Please select a valid category", Toast.LENGTH_SHORT).show();
            return;
        }

        double price;
        int quantity;
        try {
            price = Double.parseDouble(priceStr);
            quantity = Integer.parseInt(quantityStr);
        } catch (NumberFormatException e) {
            Toast.makeText(this, "Invalid price or quantity format", Toast.LENGTH_SHORT).show();
            return;
        }

        String id = db.collection("products").document().getId();
        String sellerId = auth.getCurrentUser().getUid();

        Product product = new Product(id, name, category, uploadedImageUrl, price, description);
        product.setSellerId(sellerId);
        product.setQuantity(quantity);
        product.setCreatedAt(Timestamp.now());

        db.collection("products").document(id)
                .set(product)
                .addOnSuccessListener(unused -> {
                    Toast.makeText(this, "Product added", Toast.LENGTH_SHORT).show();
                    clearFields();
                    loadSellerProducts();
                })
                .addOnFailureListener(e -> Toast.makeText(this, "Failed to add product", Toast.LENGTH_SHORT).show());
    }

    private void clearFields() {
        nameInput.setText("");
        priceInput.setText("");
        descInput.setText("");
        quantityInput.setText("");
        categorySpinner.setSelection(0);
        imagePreview.setImageDrawable(null);
        uploadedImageUrl = "";
        imageUri = null;
    }

    private void loadSellerProducts() {
        String sellerId = auth.getCurrentUser().getUid();
        db.collection("products")
                .whereEqualTo("sellerId", sellerId)
                .orderBy("createdAt", com.google.firebase.firestore.Query.Direction.DESCENDING)
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
        new AlertDialog.Builder(this)
                .setTitle("Delete Product")
                .setMessage("Are you sure you want to delete this product?")
                .setPositiveButton("Delete", (dialog, which) ->
                        db.collection("products").document(product.getId()).delete().addOnSuccessListener(unused -> {
                            Toast.makeText(this, "Product deleted", Toast.LENGTH_SHORT).show();
                            loadSellerProducts();
                        }))
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void showUpdateDialog(Product product) {
        // You can adapt this method later to support image update with storage if needed
    }
}
