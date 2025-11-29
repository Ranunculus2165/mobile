package com.wheats.api.mypage.dto;

public class CreateSupportTicketRequest {

    private Long storeId;   // 어떤 가게에 대한 문의인지
    private String title;   // 문의 제목
    private String message; // 문의 내용

    public CreateSupportTicketRequest() {
        // JSON 역직렬화용 기본 생성자
    }

    // ===== Getter / Setter =====

    public Long getStoreId() {
        return storeId;
    }

    public void setStoreId(Long storeId) {
        this.storeId = storeId;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
