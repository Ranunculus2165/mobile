package com.wheats.api.store.dto;

import java.util.List;

public class StoreDetailResponse {

    private Store store;
    private List<MenuItem> menus;

    public StoreDetailResponse(Store store, List<MenuItem> menus) {
        this.store = store;
        this.menus = menus;
    }

    public Store getStore() { return store; }
    public void setStore(Store store) { this.store = store; }

    public List<MenuItem> getMenus() { return menus; }
    public void setMenus(List<MenuItem> menus) { this.menus = menus; }
}
