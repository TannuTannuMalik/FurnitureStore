package com.example.furniturestore.models;

import java.util.List;

public class Checkout {
    private String userId;
    private String name;
    private String phone;
    private String street;
    private String city;
    private String state;
    private String zip;
    private List<OrderItem> items;  // <--- This must match Firestore field name for order items
    private long createdAt;

    // Default constructor required for Firestore
    public Checkout() {}

    // Full constructor
    public Checkout(String userId, String name, String phone, String street, String city,
                    String state, String zip, List<OrderItem> items, long createdAt) {
        this.userId = userId;
        this.name = name;
        this.phone = phone;
        this.street = street;
        this.city = city;
        this.state = state;
        this.zip = zip;
        this.items = items;
        this.createdAt = createdAt;
    }

    // Getter for items (this is important)
    public List<OrderItem> getItems() {
        return items;
    }

    public void setItems(List<OrderItem> items) {
        this.items = items;
    }

    // Add other getters and setters as needed
    public long getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(long createdAt) {
        this.createdAt = createdAt;
    }

    // (Optional) Add getters/setters for all other fields if you access them
}
