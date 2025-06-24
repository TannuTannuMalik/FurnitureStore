package com.example.furniturestore.admin;

import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.furniturestore.R;
import com.example.furniturestore.adapters.SupplierRequestAdapter;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class RequestsSupplierActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private SupplierRequestAdapter adapter;
    private final List<Map<String, Object>> requestList = new ArrayList<>();
    private final FirebaseFirestore db = FirebaseFirestore.getInstance();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_requests_supplier);

        recyclerView = findViewById(R.id.supplierRequestsRecyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new SupplierRequestAdapter(this, requestList);
        recyclerView.setAdapter(adapter);

        // Filter buttons
        Button btnPending = findViewById(R.id.btnPending);
        Button btnApproved = findViewById(R.id.btnApproved);
        Button btnDeclined = findViewById(R.id.btnDeclined);

        btnPending.setOnClickListener(v -> loadSupplierRequests("pending"));
        btnApproved.setOnClickListener(v -> loadSupplierRequests("approved"));
        btnDeclined.setOnClickListener(v -> loadSupplierRequests("declined"));

        loadSupplierRequests("pending"); // default load
    }

    private void loadSupplierRequests(String status) {
        db.collection("supplier_requests")
                .whereEqualTo("status", status)
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    requestList.clear();
                    for (QueryDocumentSnapshot doc : querySnapshot) {
                        Map<String, Object> data = doc.getData();
                        data.put("userId", doc.getId()); // already present
                        data.put("email", doc.getString("email")); // ✅ email added
                        requestList.add(data);
                    }
                    adapter.notifyDataSetChanged();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Failed to load requests", Toast.LENGTH_SHORT).show();
                    Log.e("SupplierRequests", "Error loading", e);
                });
    }

}
