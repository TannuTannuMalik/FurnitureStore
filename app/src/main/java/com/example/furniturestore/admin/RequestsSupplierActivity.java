package com.example.furniturestore.admin;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.furniturestore.R;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.example.furniturestore.adapters.SupplierRequestAdapter;


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

        loadSupplierRequests();
    }

    private void loadSupplierRequests() {
        db.collection("supplier_requests").get()
                .addOnSuccessListener(querySnapshot -> {
                    requestList.clear();
                    for (QueryDocumentSnapshot doc : querySnapshot) {
                        Map<String, Object> data = doc.getData();
                        data.put("docId", doc.getId()); // For update/delete actions
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
