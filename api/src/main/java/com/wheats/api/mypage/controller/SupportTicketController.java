package com.wheats.api.mypage.controller;

import com.wheats.api.mypage.dto.CreateSupportTicketRequest;
import com.wheats.api.mypage.dto.SupportTicketResponse;
import com.wheats.api.mypage.service.SupportTicketService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/users/me/support-tickets")
public class SupportTicketController {

    private final SupportTicketService supportTicketService;

    public SupportTicketController(SupportTicketService supportTicketService) {
        this.supportTicketService = supportTicketService;
    }

    /* 내 1:1 문의 목록 조회 */
    @GetMapping
    public ResponseEntity<List<SupportTicketResponse>> getMySupportTickets() {
        Long userId = 1L; // TODO: 나중에 OAuth 붙이면 토큰에서 추출

        List<SupportTicketResponse> tickets = supportTicketService.getMyTickets(userId);
        return ResponseEntity.ok(tickets);
    }

    /* 새 1:1 문의 작성 */
    @PostMapping
    public ResponseEntity<SupportTicketResponse> createSupportTicket(
            @RequestBody CreateSupportTicketRequest request
    ) {
        Long userId = 1L; // TODO: 나중에 OAuth 붙이면 토큰에서 추출

        SupportTicketResponse created = supportTicketService.createTicket(userId, request);
        return ResponseEntity.ok(created);
    }
}
