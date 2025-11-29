package com.wheats.api.store;

import java.util.List;

public class Store {

    private Long id;
    private String name;
    private StoreStatus status;
    private List<MenuItem> menus;

    public Store() {
    }

    public Store(Long id, String name, StoreStatus status, List<MenuItem> menus) {
        this.id = id;
        this.name = name;
        this.status = status;
        this.menus = menus;
    }

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public StoreStatus getStatus() {
        return status;
    }

    public List<MenuItem> getMenus() {
        return menus;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setStatus(StoreStatus status) {
        this.status = status;
    }

    public void setMenus(List<MenuItem> menus) {
        this.menus = menus;
    }
}
