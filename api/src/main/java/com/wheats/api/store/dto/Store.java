package com.wheats.api.store.dto;

public class Store {

    private Long id;
    private String name;
    private String category;
    private String description;
    private Integer minOrderPrice;
    private Integer deliveryTip;
    private Double rating;
    private Integer reviewCount;
    private StoreStatus status;
    private String imageUrl;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public Integer getMinOrderPrice() { return minOrderPrice; }
    public void setMinOrderPrice(Integer minOrderPrice) { this.minOrderPrice = minOrderPrice; }

    public Integer getDeliveryTip() { return deliveryTip; }
    public void setDeliveryTip(Integer deliveryTip) { this.deliveryTip = deliveryTip; }

    public Double getRating() { return rating; }
    public void setRating(Double rating) { this.rating = rating; }

    public Integer getReviewCount() { return reviewCount; }
    public void setReviewCount(Integer reviewCount) { this.reviewCount = reviewCount; }

    public StoreStatus getStatus() { return status; }
    public void setStatus(StoreStatus status) { this.status = status; }

    public String getImageUrl() { return imageUrl; }
    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }
}
