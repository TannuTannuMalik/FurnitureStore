package com.example.furniturestore.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.furniturestore.R;
import com.example.furniturestore.models.Product;

import java.util.List;

public class ProductAdapter extends RecyclerView.Adapter<ProductAdapter.ProductViewHolder> {

    public interface OnItemClickListener {
        void onItemClick(Product product);
    }

    public interface OnDeleteClickListener {
        void onDeleteClick(Product product);
    }

    private final List<Product> productList;
    private final OnItemClickListener itemClickListener;
    private final OnDeleteClickListener deleteClickListener;

    public ProductAdapter(List<Product> productList, OnItemClickListener itemClickListener, OnDeleteClickListener deleteClickListener) {
        this.productList = productList;
        this.itemClickListener = itemClickListener;
        this.deleteClickListener = deleteClickListener;
    }

    @NonNull
    @Override
    public ProductViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_product, parent, false);
        return new ProductViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ProductViewHolder holder, int position) {
        Product product = productList.get(position);
        holder.bind(product, itemClickListener, deleteClickListener);
    }

    @Override
    public int getItemCount() {
        return productList.size();
    }

    static class ProductViewHolder extends RecyclerView.ViewHolder {

        private final TextView textName;
        private final TextView textCategory;
        private final TextView textPrice;
        private final ImageView imageProduct;
        private final Button buttonDelete;

        public ProductViewHolder(@NonNull View itemView) {
            super(itemView);
            textName = itemView.findViewById(R.id.productName);
            textCategory = itemView.findViewById(R.id.productCategory);
            textPrice = itemView.findViewById(R.id.productPrice);
            imageProduct = itemView.findViewById(R.id.productImage);
            buttonDelete = itemView.findViewById(R.id.buttonDelete);
        }

        public void bind(final Product product,
                         final OnItemClickListener itemClickListener,
                         final OnDeleteClickListener deleteClickListener) {
            textName.setText(product.getName());
            textCategory.setText(product.getCategory());
            textPrice.setText(String.format("$%.2f", product.getPrice()));

            Glide.with(imageProduct.getContext())
                    .load(product.getImageUrl())
                    .placeholder(R.drawable.placeholder_image)
                    .into(imageProduct);

            itemView.setOnClickListener(v -> itemClickListener.onItemClick(product));

            // Show or hide delete button based on listener
            if (deleteClickListener != null) {
                buttonDelete.setVisibility(View.VISIBLE);
                buttonDelete.setOnClickListener(v -> deleteClickListener.onDeleteClick(product));
            } else {
                buttonDelete.setVisibility(View.GONE);
            }
        }
    }
}
