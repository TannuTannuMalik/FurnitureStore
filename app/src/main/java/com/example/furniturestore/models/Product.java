package com.example.furniturestore.models;

import com.google.firebase.Timestamp;

import java.util.List;

public class Product {

    private String id;
    private String name;
    private String category;
    private String imageUrl;
    private List<String> imageUrls;
    private double price;
    private String description;
    private String sellerId;
    private int quantity;
    private Timestamp createdAt;

    // Required no-arg constructor for Firestore
    public Product() {
    }

    // Main constructor
    public Product(String id, String name, String category, String imageUrl,
                   double price, String description, String sellerId,
                   int quantity, List<String> imageUrls, Timestamp createdAt) {
        this.id = id;
        this.name = name;
        this.category = category;
        this.imageUrl = imageUrl;
        this.price = price;
        this.description = description;
        this.sellerId = sellerId;
        this.quantity = quantity;
        this.imageUrls = imageUrls;
        this.createdAt = createdAt;
    }

    // Simpler constructor for testing or temporary use
    public Product(String id, String name, String category, String imageUrl,
                   double price, String description) {
        this.id = id;
        this.name = name;
        this.category = category;
        this.imageUrl = imageUrl;
        this.price = price;
        this.description = description;
    }

    // Getters and Setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }

    public String getImageUrl() { return imageUrl; }
    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }

    public List<String> getImageUrls() { return imageUrls; }
    public void setImageUrls(List<String> imageUrls) { this.imageUrls = imageUrls; }

    public double getPrice() { return price; }
    public void setPrice(double price) { this.price = price; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getSellerId() { return sellerId; }
    public void setSellerId(String sellerId) { this.sellerId = sellerId; }

    public int getQuantity() { return quantity; }
    public void setQuantity(int quantity) { this.quantity = quantity; }

    public Timestamp getCreatedAt() { return createdAt; }
    public void setCreatedAt(Timestamp createdAt) { this.createdAt = createdAt; }
}