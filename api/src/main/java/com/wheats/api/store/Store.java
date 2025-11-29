package com.wheats.api.store;

public class Store {

    private Long id;
    private String name;
    private String status;              // "OPEN", "CLOSED" ...
    private int minOrderAmount;         // 최소 주문 금액
    private String estimatedDeliveryTime; // "30~40분" 이런 문자열

    public Store(Long id, String name, String status, int minOrderAmount, String estimatedDeliveryTime) {
        this.id = id;
        this.name = name;
        this.status = status;
        this.minOrderAmount = minOrderAmount;
        this.estimatedDeliveryTime = estimatedDeliveryTime;
    }

    public Store() {
    }

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getStatus() {
        return status;
    }

    public int getMinOrderAmount() {
        return minOrderAmount;
    }

    public String getEstimatedDeliveryTime() {
        return estimatedDeliveryTime;
    }
}
