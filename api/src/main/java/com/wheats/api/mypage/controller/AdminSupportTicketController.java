package com.wheats.api.mypage.controller;

import com.wheats.api.mypage.dto.SupportTicketResponse;
import com.wheats.api.mypage.service.SupportTicketService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 관리자용 문의 조회 API
 * admin scope가 있어야 접근 가능
 */
@RestController
@RequestMapping("/api/admin/support-tickets")
public class AdminSupportTicketController {

    private final SupportTicketService supportTicketService;

    public AdminSupportTicketController(SupportTicketService supportTicketService) {
        this.supportTicketService = supportTicketService;
    }

    /**
     * 관리자: 모든 문의 조회
     * admin scope 필요
     */
    @GetMapping
    public ResponseEntity<List<SupportTicketResponse>> getAllSupportTickets(HttpServletRequest request) {
        String scope = (String) request.getAttribute("scope");

        // 🚨 CTF: admin scope가 있어야 모든 문의 조회 가능
        if (scope == null || !scope.contains("admin")) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        List<SupportTicketResponse> tickets = supportTicketService.getAllTickets();
        return ResponseEntity.ok(tickets);
    }

    /**
     * 관리자: 특정 문의 상세 조회
     * admin scope 필요
     */
    @GetMapping("/{ticketId}")
    public ResponseEntity<SupportTicketResponse> getSupportTicketDetail(
            @PathVariable Long ticketId,
            HttpServletRequest request
    ) {
        String scope = (String) request.getAttribute("scope");

        // 🚨 CTF: admin scope가 있어야 문의 상세 조회 가능
        if (scope == null || !scope.contains("admin")) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        SupportTicketResponse ticket = supportTicketService.getTicketById(ticketId);
        if (ticket == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(ticket);
    }
}
