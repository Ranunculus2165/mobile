package com.wheats.api.store;

public class MenuItem {

    private Long id;
    private Long storeId;
    private String name;
    private String description;
    private int price;

    public MenuItem(Long id, Long storeId, String name, String description, int price) {
        this.id = id;
        this.storeId = storeId;
        this.name = name;
        this.description = description;
        this.price = price;
    }

    public MenuItem() {
    }

    public Long getId() {
        return id;
    }

    public Long getStoreId() {
        return storeId;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public int getPrice() {
        return price;
    }
}
