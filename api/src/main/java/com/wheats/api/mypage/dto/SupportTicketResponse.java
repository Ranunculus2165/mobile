package com.wheats.api.mypage.dto;

import com.wheats.api.mypage.entity.SupportTicketEntity;
import com.wheats.api.mypage.entity.SupportTicketStatus;

import java.time.LocalDateTime;

public class SupportTicketResponse {

    private Long id;
    private Long storeId;
    private String title;
    private String message;
    private SupportTicketStatus status;
    private LocalDateTime createdAt;

    public SupportTicketResponse(Long id,
                                 Long storeId,
                                 String title,
                                 String message,
                                 SupportTicketStatus status,
                                 LocalDateTime createdAt) {
        this.id = id;
        this.storeId = storeId;
        this.title = title;
        this.message = message;
        this.status = status;
        this.createdAt = createdAt;
    }

    public static SupportTicketResponse from(SupportTicketEntity entity) {
        return new SupportTicketResponse(
                entity.getId(),
                entity.getStoreId(),
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
