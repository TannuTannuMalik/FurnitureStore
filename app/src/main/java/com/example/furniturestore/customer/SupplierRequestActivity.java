package com.example.furniturestore.customer;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

//import androidx.activity.EdgeToEdge;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.furniturestore.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class SupplierRequestActivity extends AppCompatActivity {

    private static final int PICK_IMAGE_REQUEST = 1;
    private Uri selectedImageUri;
    private ImageView previewImage;
    private Button selectImageBtn, submitRequestBtn;
    private TextView statusText;
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    private StorageReference storageRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_supplier_request);

        selectImageBtn = findViewById(R.id.selectImageBtn);
        submitRequestBtn = findViewById(R.id.submitRequestBtn);
        previewImage = findViewById(R.id.previewImage);
        statusText = findViewById(R.id.statusText);

        db = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();
        storageRef = FirebaseStorage.getInstance().getReference("supplier_docs");

        selectImageBtn.setOnClickListener(v -> openImagePicker());

        submitRequestBtn.setOnClickListener(v -> {
            if (selectedImageUri == null) {
                Toast.makeText(this, "Please select an image", Toast.LENGTH_SHORT).show();
                return;
            }
            uploadImageAndSubmitRequest();
        });
    }

    private void openImagePicker() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("image/*");
        startActivityForResult(intent, PICK_IMAGE_REQUEST);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null) {
            selectedImageUri = data.getData();
            previewImage.setImageURI(selectedImageUri);
            statusText.setText("Image selected");
        }
    }

    private void uploadImageAndSubmitRequest() {
        String userId = mAuth.getCurrentUser().getUid();
        StorageReference fileRef = storageRef.child(userId + ".jpg");

        fileRef.putFile(selectedImageUri)
                .addOnSuccessListener(taskSnapshot ->
                        fileRef.getDownloadUrl().addOnSuccessListener(uri -> {
                            submitRequestToFirestore(userId, uri.toString());
                        })
                )
                .addOnFailureListener(e ->
                        Toast.makeText(this, "Upload failed: " + e.getMessage(), Toast.LENGTH_SHORT).show());
    }

    private void submitRequestToFirestore(String userId, String fileUrl) {
        Map<String, Object> request = new HashMap<>();
        request.put("userId", userId);
        request.put("status", "pending");
        request.put("documentUrl", fileUrl);
        request.put("timestamp", new Date());

        db.collection("supplier_requests").document(userId)
                .set(request)
                .addOnSuccessListener(unused -> {
                    Toast.makeText(this, "Request submitted!", Toast.LENGTH_SHORT).show();
                    finish();
                })
                .addOnFailureListener(e ->
                        Toast.makeText(this, "Failed: " + e.getMessage(), Toast.LENGTH_SHORT).show());
    }
}
