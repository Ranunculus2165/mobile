package com.wheats.api.mypage.service;

import com.wheats.api.mypage.dto.CreateSupportTicketRequest;
import com.wheats.api.mypage.dto.SupportTicketResponse;
import com.wheats.api.mypage.entity.SupportTicketEntity;
import com.wheats.api.mypage.repository.SupportTicketRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class SupportTicketService {

    private final SupportTicketRepository supportTicketRepository;

    public SupportTicketService(SupportTicketRepository supportTicketRepository) {
        this.supportTicketRepository = supportTicketRepository;
    }

    @Transactional(readOnly = true)
    public List<SupportTicketResponse> getMyTickets(Long userId) {
        List<SupportTicketEntity> tickets =
                supportTicketRepository.findByUserIdOrderByCreatedAtDesc(userId);

        return tickets.stream()
                .map(SupportTicketResponse::from)
                .toList();
    }

    @Transactional
    public SupportTicketResponse createTicket(Long userId, CreateSupportTicketRequest request) {
        SupportTicketEntity entity = new SupportTicketEntity(
                userId,
                request.getStoreId(),
                request.getTitle(),
                request.getMessage()
        );

        SupportTicketEntity saved = supportTicketRepository.save(entity);
        return SupportTicketResponse.from(saved);
    }
}
