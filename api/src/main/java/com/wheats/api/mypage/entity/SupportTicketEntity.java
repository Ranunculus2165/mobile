package com.wheats.api.mypage.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "support_tickets")
public class SupportTicketEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // FK: users.id
    @Column(name = "user_id", nullable = false)
    private Long userId;

    // FK: stores.id
    @Column(name = "store_id", nullable = false)
    private Long storeId;

    @Column(nullable = false, length = 200)
    private String title;

    @Lob
    @Column(nullable = false)
    private String message;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private SupportTicketStatus status;

    // DB에서 DEFAULT CURRENT_TIMESTAMP 처리
    @Column(name = "created_at", insertable = false, updatable = false)
    private LocalDateTime createdAt;

    // JPA용 기본 생성자 (반드시 필요, protected 권장)
    protected SupportTicketEntity() {
    }

    // 우리가 사용할 생성자 (새 문의 생성용)
    public SupportTicketEntity(Long userId, Long storeId, String title, String message) {
        this.userId = userId;
        this.storeId = storeId;
        this.title = title;
        this.message = message;
        this.status = SupportTicketStatus.OPEN; // 새 티켓은 항상 OPEN으로 시작
    }

    // ===== Getter들 =====

    public Long getId() {
        return id;
    }

    public Long getUserId() {
        return userId;
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

    // 상태 변경이 필요해지면, 이렇게 “행위 메서드”를 쓸 수 있음
    public void close() {
        this.status = SupportTicketStatus.CLOSED;
    }

    public void answer() {
        this.status = SupportTicketStatus.ANSWERED;
    }
}
