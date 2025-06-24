package com.example.furniturestore.dashboard;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.*;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.example.furniturestore.MainActivity;
import com.example.furniturestore.R;
import com.example.furniturestore.models.Product;
import com.example.furniturestore.seller.ManageSellerProductsActivity;
import com.example.furniturestore.seller.SellerOrdersActivity;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.*;

public class SellerDashboardActivity extends AppCompatActivity {

    private static final int PICK_MAIN_IMAGE_REQUEST = 101;
    private static final int PICK_SUB1_IMAGE_REQUEST = 102;
    private static final int PICK_SUB2_IMAGE_REQUEST = 103;

    private EditText nameInput, priceInput, descInput, quantityInput;
    private Spinner categorySpinner;
    private Button addProductBtn, logoutBtn, buttonViewOrders, buttonSellerRevenue;

    private ImageView imageMain, imageSub1, imageSub2;
    private Button buttonUploadMain, buttonUploadSub1, buttonUploadSub2;

    private Uri mainImageUri = null;
    private Uri sub1ImageUri = null;
    private Uri sub2ImageUri = null;

    private FirebaseFirestore db;
    private FirebaseAuth auth;
    private FirebaseStorage storage;

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
        buttonViewOrders = findViewById(R.id.buttonViewOrders);
        buttonSellerRevenue = findViewById(R.id.buttonSellerRevenue);

        Button buttonManageProducts = findViewById(R.id.buttonManageProducts);
        buttonManageProducts.setOnClickListener(v -> {
            Intent intent = new Intent(SellerDashboardActivity.this, ManageSellerProductsActivity.class);
            startActivity(intent);
        });

        Button buttonEditProfile = findViewById(R.id.buttonEditProfile);
        buttonEditProfile.setOnClickListener(v -> {
            Intent intent = new Intent(SellerDashboardActivity.this, com.example.furniturestore.settings.EditProfileActivity.class);
            startActivity(intent);
        });

        imageMain = findViewById(R.id.imageMain);
        imageSub1 = findViewById(R.id.imageSub1);
        imageSub2 = findViewById(R.id.imageSub2);

        buttonUploadMain = findViewById(R.id.buttonUploadMain);
        buttonUploadSub1 = findViewById(R.id.buttonUploadSub1);
        buttonUploadSub2 = findViewById(R.id.buttonUploadSub2);

        ArrayAdapter<String> categoryAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, categories);
        categoryAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        categorySpinner.setAdapter(categoryAdapter);

        buttonUploadMain.setOnClickListener(v -> openImagePicker(PICK_MAIN_IMAGE_REQUEST));
        buttonUploadSub1.setOnClickListener(v -> openImagePicker(PICK_SUB1_IMAGE_REQUEST));
        buttonUploadSub2.setOnClickListener(v -> openImagePicker(PICK_SUB2_IMAGE_REQUEST));

        addProductBtn.setOnClickListener(v -> addProduct());

        buttonViewOrders.setOnClickListener(v -> startActivity(new Intent(this, SellerOrdersActivity.class)));

        findViewById(R.id.buttonGoBack).setOnClickListener(v -> {
            startActivity(new Intent(this, MainActivity.class));
            finish();
        });

        logoutBtn.setOnClickListener(v -> {
            auth.signOut();
            Toast.makeText(this, "Logged out", Toast.LENGTH_SHORT).show();
            finish();
        });

        // ✅ Corrected: open RevenueActivity
        buttonSellerRevenue.setOnClickListener(v -> {
            startActivity(new Intent(SellerDashboardActivity.this, RevenueActivity.class));
        });
    }

    private void openImagePicker(int requestCode) {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("image/*");
        startActivityForResult(Intent.createChooser(intent, "Select Image"), requestCode);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK && data != null && data.getData() != null) {
            Uri selectedUri = data.getData();
            switch (requestCode) {
                case PICK_MAIN_IMAGE_REQUEST:
                    mainImageUri = selectedUri;
                    imageMain.setImageURI(mainImageUri);
                    break;
                case PICK_SUB1_IMAGE_REQUEST:
                    sub1ImageUri = selectedUri;
                    imageSub1.setImageURI(sub1ImageUri);
                    break;
                case PICK_SUB2_IMAGE_REQUEST:
                    sub2ImageUri = selectedUri;
                    imageSub2.setImageURI(sub2ImageUri);
                    break;
            }
        }
    }

    private void addProduct() {
        String name = nameInput.getText().toString().trim();
        String priceStr = priceInput.getText().toString().trim();
        String quantityStr = quantityInput.getText().toString().trim();
        String description = descInput.getText().toString().trim();
        String category = categorySpinner.getSelectedItem().toString();

        if (TextUtils.isEmpty(name) || TextUtils.isEmpty(priceStr) || TextUtils.isEmpty(quantityStr)) {
            Toast.makeText(this, "Please fill Name, Price and Quantity", Toast.LENGTH_SHORT).show();
            return;
        }

        if (mainImageUri == null) {
            Toast.makeText(this, "Please select a main image", Toast.LENGTH_SHORT).show();
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
            Toast.makeText(this, "Invalid price or quantity", Toast.LENGTH_SHORT).show();
            return;
        }

        addProductBtn.setEnabled(false);
        uploadImagesAndSaveProduct(name, category, price, description, quantity);
    }

    private void uploadImagesAndSaveProduct(String name, String category, double price,
                                            String description, int quantity) {

        uploadImage(mainImageUri, urlMain -> {
            if (urlMain == null) {
                showToastAndEnableButton("Failed to upload main image");
                return;
            }
            uploadImage(sub1ImageUri, urlSub1 -> {
                uploadImage(sub2ImageUri, urlSub2 -> {
                    saveProductToFirestore(name, category, price, description, quantity, urlMain, urlSub1, urlSub2);
                });
            });
        });
    }

    private void uploadImage(Uri uri, final UploadCallback callback) {
        if (uri == null) {
            callback.onUploadComplete(null);
            return;
        }

        String filename = "products/" + UUID.randomUUID().toString();
        StorageReference ref = storage.getReference().child(filename);
        ref.putFile(uri)
                .addOnSuccessListener(taskSnapshot ->
                        ref.getDownloadUrl().addOnSuccessListener(uri1 ->
                                        callback.onUploadComplete(uri1.toString()))
                                .addOnFailureListener(e -> callback.onUploadComplete(null)))
                .addOnFailureListener(e -> callback.onUploadComplete(null));
    }

    private void saveProductToFirestore(String name, String category, double price, String description,
                                        int quantity, String mainImageUrl, String sub1ImageUrl, String sub2ImageUrl) {

        String sellerId = auth.getCurrentUser() != null ? auth.getCurrentUser().getUid() : "unknown";

        List<String> imageUrls = new ArrayList<>();
        imageUrls.add(mainImageUrl);
        if (sub1ImageUrl != null && !sub1ImageUrl.isEmpty()) imageUrls.add(sub1ImageUrl);
        if (sub2ImageUrl != null && !sub2ImageUrl.isEmpty()) imageUrls.add(sub2ImageUrl);

        Map<String, Object> productMap = new HashMap<>();
        productMap.put("name", name);
        productMap.put("category", category);
        productMap.put("price", price);
        productMap.put("description", description);
        productMap.put("quantity", quantity);
        productMap.put("sellerId", sellerId);
        productMap.put("createdAt", Timestamp.now());
        productMap.put("imageUrl", mainImageUrl);
        productMap.put("imageUrls", imageUrls);

        db.collection("products")
                .add(productMap)
                .addOnSuccessListener(documentReference -> {
                    Toast.makeText(this, "Product added!", Toast.LENGTH_SHORT).show();
                    addProductBtn.setEnabled(true);
                    clearInputs();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Failed to add product: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    addProductBtn.setEnabled(true);
                });
    }

    private void clearInputs() {
        nameInput.setText("");
        priceInput.setText("");
        descInput.setText("");
        quantityInput.setText("");
        categorySpinner.setSelection(0);

        mainImageUri = null;
        sub1ImageUri = null;
        sub2ImageUri = null;

        imageMain.setImageResource(android.R.color.darker_gray);
        imageSub1.setImageResource(android.R.color.darker_gray);
        imageSub2.setImageResource(android.R.color.darker_gray);
    }

    private void showToastAndEnableButton(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
        addProductBtn.setEnabled(true);
    }

    private interface UploadCallback {
        void onUploadComplete(@Nullable String url);
    }
}
