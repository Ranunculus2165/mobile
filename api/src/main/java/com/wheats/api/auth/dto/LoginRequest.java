package com.wheats.api.auth.dto;

public class LoginRequest {

    private String email;

    // 기본 생성자 (Jackson 역직렬화용)
    public LoginRequest() {
    }

    public LoginRequest(String email) {
        this.email = email;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }
}
