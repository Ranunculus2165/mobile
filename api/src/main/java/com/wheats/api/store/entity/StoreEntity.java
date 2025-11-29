package com.wheats.api.store.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "stores")
public class StoreEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;
    private String category;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(name = "min_order_price")
    private Integer minOrderPrice;

    @Column(name = "delivery_tip")
    private Integer deliveryTip;

    private Double rating;

    @Column(name = "review_count")
    private Integer reviewCount;

    @Column(name = "is_open")
    private Boolean isOpen;

    @Column(name = "image_url")
    private String imageUrl;

    protected StoreEntity() {}

    public Long getId() { return id; }
    public String getName() { return name; }
    public String getCategory() { return category; }
    public String getDescription() { return description; }
    public Integer getMinOrderPrice() { return minOrderPrice; }
    public Integer getDeliveryTip() { return deliveryTip; }
    public Double getRating() { return rating; }
    public Integer getReviewCount() { return reviewCount; }
    public Boolean getIsOpen() { return isOpen; }
    public String getImageUrl() { return imageUrl; }

    public void setId(Long id) { this.id = id; }
    public void setName(String name) { this.name = name; }
    public void setCategory(String category) { this.category = category; }
    public void setDescription(String description) { this.description = description; }
    public void setMinOrderPrice(Integer minOrderPrice) { this.minOrderPrice = minOrderPrice; }
    public void setDeliveryTip(Integer deliveryTip) { this.deliveryTip = deliveryTip; }
    public void setRating(Double rating) { this.rating = rating; }
    public void setReviewCount(Integer reviewCount) { this.reviewCount = reviewCount; }
    public void setIsOpen(Boolean isOpen) { this.isOpen = isOpen; }
    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }
}
