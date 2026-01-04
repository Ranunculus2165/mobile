package com.wheats.api.mypage.repository;

import com.wheats.api.mypage.entity.SupportTicketEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface SupportTicketRepository extends JpaRepository<SupportTicketEntity, Long> {

    // 내 문의 목록 (최신순)
    List<SupportTicketEntity> findByUserIdOrderByCreatedAtDesc(Long userId);

    // 관리자용: 전체 문의 목록 (최신순)
    List<SupportTicketEntity> findAllByOrderByCreatedAtDesc();
}
