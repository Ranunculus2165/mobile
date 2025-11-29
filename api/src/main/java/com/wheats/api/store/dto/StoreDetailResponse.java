package com.wheats.api.store.dto;

import java.util.List;

public class StoreDetailResponse {

    private Store store;
    private List<MenuItem> menus;

    public StoreDetailResponse(Store store, List<MenuItem> menus) {
        this.store = store;
        this.menus = menus;
    }

    public StoreDetailResponse() {
    }

    public Store getStore() {
        return store;
    }

    public List<MenuItem> getMenus() {
        return menus;
    }
}
