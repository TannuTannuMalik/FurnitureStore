package com.example.furniturestore.adapters;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.furniturestore.R;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.List;
import java.util.Map;

public class SupplierRequestAdapter extends RecyclerView.Adapter<SupplierRequestAdapter.ViewHolder> {

    private final List<Map<String, Object>> requestList;
    private final Context context;

    public SupplierRequestAdapter(Context context, List<Map<String, Object>> requestList) {
        this.context = context;
        this.requestList = requestList;
    }

    @NonNull
    @Override
    public SupplierRequestAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_supplier_request, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull SupplierRequestAdapter.ViewHolder holder, int position) {
        Map<String, Object> request = requestList.get(position);

        String userId = (String) request.get("userId");
        String imageUrl = (String) request.get("documentUrl");

        holder.userIdText.setText("User ID: " + userId);

        holder.viewImageBtn.setOnClickListener(v -> {
            if (imageUrl != null) {
                Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(imageUrl));
                context.startActivity(intent);
            }
        });

        holder.approveBtn.setOnClickListener(v -> {
            if (userId != null) {
                FirebaseFirestore.getInstance()
                        .collection("users")
                        .document(userId)
                        .update("role", "seller")
                        .addOnSuccessListener(aVoid -> {
                            FirebaseFirestore.getInstance()
                                    .collection("supplier_requests")
                                    .document(userId)
                                    .update("status", "approved");
                        });
            }
        });

        holder.declineBtn.setOnClickListener(v -> {
            if (userId != null) {
                FirebaseFirestore.getInstance()
                        .collection("supplier_requests")
                        .document(userId)
                        .update("status", "declined");
            }
        });
    }

    @Override
    public int getItemCount() {
        return requestList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {

        TextView userIdText;
        Button viewImageBtn, approveBtn, declineBtn;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            userIdText = itemView.findViewById(R.id.userIdText);
            viewImageBtn = itemView.findViewById(R.id.viewImageBtn);
            approveBtn = itemView.findViewById(R.id.approveBtn);
            declineBtn = itemView.findViewById(R.id.declineBtn);
        }
    }
}
