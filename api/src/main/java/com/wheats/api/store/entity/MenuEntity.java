package com.wheats.api.store.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "menus")
public class MenuEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // store_id FK
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "store_id", nullable = false)
    private StoreEntity store;

    private String name;
    private Integer price;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(name = "is_available")
    private Boolean isAvailable;

    @Column(name = "image_url")
    private String imageUrl;

    protected MenuEntity() {}

    public Long getId() { return id; }
    public StoreEntity getStore() { return store; }
    public String getName() { return name; }
    public Integer getPrice() { return price; }
    public String getDescription() { return description; }
    public Boolean getIsAvailable() { return isAvailable; }
    public String getImageUrl() { return imageUrl; }

    public void setId(Long id) { this.id = id; }
    public void setStore(StoreEntity store) { this.store = store; }
    public void setName(String name) { this.name = name; }
    public void setPrice(Integer price) { this.price = price; }
    public void setDescription(String description) { this.description = description; }
    public void setIsAvailable(Boolean isAvailable) { this.isAvailable = isAvailable; }
    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }
}
