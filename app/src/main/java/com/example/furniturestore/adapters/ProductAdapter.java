package com.example.furniturestore.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.furniturestore.R;
import com.example.furniturestore.models.Product;
import com.google.firebase.firestore.FirebaseFirestore;

import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;

public class ProductAdapter extends RecyclerView.Adapter<ProductAdapter.ProductViewHolder> {

    private final List<Product> productList;
    private final OnProductClickListener onProductClickListener;
    private final OnProductDeleteListener onProductDeleteListener;
    private final OnProductEditListener onProductEditListener;
    private final boolean isAdminOrSeller;

    public interface OnProductClickListener {
        void onProductClick(Product product);
    }

    public interface OnProductDeleteListener {
        void onProductDelete(Product product);
    }

    public interface OnProductEditListener {
        void onProductEdit(Product product);
    }

    public ProductAdapter(List<Product> productList,
                          OnProductClickListener clickListener,
                          OnProductDeleteListener deleteListener,
                          OnProductEditListener editListener,
                          boolean isAdminOrSeller) {
        this.productList = productList;
        this.onProductClickListener = clickListener;
        this.onProductDeleteListener = deleteListener;
        this.onProductEditListener = editListener;
        this.isAdminOrSeller = isAdminOrSeller;
    }

    public ProductAdapter(List<Product> productList,
                          OnProductClickListener clickListener,
                          OnProductDeleteListener deleteListener,
                          boolean isAdminOrSeller) {
        this(productList, clickListener, deleteListener, null, isAdminOrSeller);
    }

    public void updateList(List<Product> updatedList) {
        productList.clear();
        productList.addAll(updatedList);
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ProductViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_product, parent, false);
        return new ProductViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ProductViewHolder holder, int position) {
        Product product = productList.get(position);
        Context context = holder.itemView.getContext();

        holder.productName.setText(product.getName());
        holder.productCategory.setText(product.getCategory());
        holder.productPrice.setText("$" + product.getPrice());
        holder.productQuantity.setText("Stock: " + product.getQuantity());

        if (product.getCreatedAt() != null) {
            SimpleDateFormat sdf = new SimpleDateFormat("dd MMM yyyy", Locale.US);
            holder.productCreatedAt.setText("Created: " + sdf.format(product.getCreatedAt().toDate()));
        } else {
            holder.productCreatedAt.setText("Created: N/A");
        }

        List<String> imageUrls = product.getImageUrls();
        if (imageUrls != null && !imageUrls.isEmpty()) {
            Glide.with(context).load(imageUrls.get(0)).into(holder.productImageMain);
        } else if (product.getImageUrl() != null) {
            Glide.with(context).load(product.getImageUrl()).into(holder.productImageMain);
        } else {
            holder.productImageMain.setImageDrawable(null);
        }

        if (imageUrls != null && imageUrls.size() > 1)
            Glide.with(context).load(imageUrls.get(1)).into(holder.productImageSub1);
        else holder.productImageSub1.setImageDrawable(null);

        if (imageUrls != null && imageUrls.size() > 2)
            Glide.with(context).load(imageUrls.get(2)).into(holder.productImageSub2);
        else holder.productImageSub2.setImageDrawable(null);

        holder.itemView.setOnClickListener(v -> {
            if (onProductClickListener != null)
                onProductClickListener.onProductClick(product);
        });

        if (isAdminOrSeller) {
            holder.buttonDelete.setVisibility(View.VISIBLE);
            holder.buttonEdit.setVisibility(View.VISIBLE);

            holder.buttonDelete.setOnClickListener(v -> {
                if (onProductDeleteListener != null)
                    onProductDeleteListener.onProductDelete(product);
            });

            holder.buttonEdit.setOnClickListener(v -> showEditDialog(context, product));
        } else {
            holder.buttonDelete.setVisibility(View.GONE);
            holder.buttonEdit.setVisibility(View.GONE);
        }
    }

    private void showEditDialog(Context context, Product product) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle("Edit Product")
                .setMessage("Do you want to edit " + product.getName() + "?")
                .setPositiveButton("Edit", (dialog, which) -> {
                    if (onProductEditListener != null) {
                        onProductEditListener.onProductEdit(product);
                    } else {
                        Toast.makeText(context, "Edit handler not implemented", Toast.LENGTH_SHORT).show();
                    }
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    @Override
    public int getItemCount() {
        return productList.size();
    }

    static class ProductViewHolder extends RecyclerView.ViewHolder {
        ImageView productImageMain, productImageSub1, productImageSub2;
        TextView productName, productCategory, productPrice, productCreatedAt, productQuantity;
        Button buttonDelete, buttonEdit;

        public ProductViewHolder(@NonNull View itemView) {
            super(itemView);
            productImageMain = itemView.findViewById(R.id.productImageMain);
            productImageSub1 = itemView.findViewById(R.id.productImageSub1);
            productImageSub2 = itemView.findViewById(R.id.productImageSub2);
            productName = itemView.findViewById(R.id.productName);
            productCategory = itemView.findViewById(R.id.productCategory);
            productPrice = itemView.findViewById(R.id.productPrice);
            productCreatedAt = itemView.findViewById(R.id.productCreatedAt);
            productQuantity = itemView.findViewById(R.id.productQuantity);
            buttonDelete = itemView.findViewById(R.id.buttonDelete);
            buttonEdit = itemView.findViewById(R.id.buttonEdit);
        }
    }
}