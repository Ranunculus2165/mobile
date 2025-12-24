package com.wheats.api.mypage.dto;

import com.wheats.api.mypage.entity.SupportTicketEntity;
import com.wheats.api.mypage.entity.SupportTicketStatus;

import java.time.LocalDateTime;

public class SupportTicketResponse {

    private Long id;
    private Long storeId;
    private String storeName;
    private String title;
    private String message;
    private SupportTicketStatus status;
    private LocalDateTime createdAt;

    public SupportTicketResponse(Long id,
                                 Long storeId,
                                 String storeName,
                                 String title,
                                 String message,
                                 SupportTicketStatus status,
                                 LocalDateTime createdAt) {
        this.id = id;
        this.storeId = storeId;
        this.storeName = storeName;
        this.title = title;
        this.message = message;
        this.status = status;
        this.createdAt = createdAt;
    }

    public static SupportTicketResponse from(SupportTicketEntity entity, String storeName) {
        return new SupportTicketResponse(
                entity.getId(),
                entity.getStoreId(),
                storeName,
                entity.getTitle(),
                entity.getMessage(),
                entity.getStatus(),
                entity.getCreatedAt()
        );
    }

    public Long getId() {
        return id;
    }

    public Long getStoreId() {
        return storeId;
    }

    public String getStoreName() {
        return storeName;
    }

    public String getTitle() {
        return title;
    }

    public String getMessage() {
        return message;
    }

    public SupportTicketStatus getStatus() {
        return status;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
}
