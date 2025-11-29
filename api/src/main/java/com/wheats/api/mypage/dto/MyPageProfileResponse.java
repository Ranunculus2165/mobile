package com.wheats.api.mypage.dto;

import com.wheats.api.mypage.entity.UserEntity;

public class MyPageProfileResponse {

    private String name;
    private String email;
    private Integer point;

    public MyPageProfileResponse(String name, String email, Integer point) {
        this.name = name;
        this.email = email;
        this.point = point;
    }

    public static MyPageProfileResponse from(UserEntity user) {
        return new MyPageProfileResponse(
                user.getName(),
                user.getEmail(),
                user.getPoint()
        );
    }

    // ==== Getter만 제공 ====
    public String getName() {
        return name;
    }

    public String getEmail() {
        return email;
    }

    public Integer getPoint() {
        return point;
    }
}
