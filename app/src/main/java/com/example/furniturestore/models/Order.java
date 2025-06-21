package com.example.furniturestore.models;

import com.google.firebase.Timestamp;
import java.util.List;
import com.google.firebase.firestore.IgnoreExtraProperties;

@IgnoreExtraProperties
public class Order {

    private String id;
    private String userId;
    private String userEmail;
    private String address;
    private String phone;
    private Timestamp timestamp;
    private List<OrderItem> items;

    public Order() {
        // Required for Firestore
    }

    public Order(String userId, String userEmail, String address, String phone, Timestamp timestamp, List<OrderItem> items) {
        this.userId = userId;
        this.userEmail = userEmail;
        this.address = address;
        this.phone = phone;
        this.timestamp = timestamp;
        this.items = items;
    }

    // Getters and setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }

    public String getUserEmail() { return userEmail; }
    public void setUserEmail(String userEmail) { this.userEmail = userEmail; }

    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }

    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }

    public Timestamp getTimestamp() { return timestamp; }
    public void setTimestamp(Timestamp timestamp) { this.timestamp = timestamp; }

    public List<OrderItem> getItems() { return items; }
    public void setItems(List<OrderItem> items) { this.items = items; }
}
