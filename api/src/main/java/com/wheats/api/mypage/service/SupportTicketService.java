package com.wheats.api.mypage.service;

import com.wheats.api.mypage.dto.CreateSupportTicketRequest;
import com.wheats.api.mypage.dto.SupportTicketResponse;
import com.wheats.api.mypage.entity.SupportTicketEntity;
import com.wheats.api.mypage.repository.SupportTicketRepository;
import com.wheats.api.store.entity.StoreEntity;
import com.wheats.api.store.repository.StoreRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
public class SupportTicketService {

    private final SupportTicketRepository supportTicketRepository;
    private final StoreRepository storeRepository;

    public SupportTicketService(SupportTicketRepository supportTicketRepository,
                                 StoreRepository storeRepository) {
        this.supportTicketRepository = supportTicketRepository;
        this.storeRepository = storeRepository;
    }

    @Transactional(readOnly = true)
    public List<SupportTicketResponse> getMyTickets(Long userId) {
        List<SupportTicketEntity> tickets =
                supportTicketRepository.findByUserIdOrderByCreatedAtDesc(userId);

        List<SupportTicketResponse> responses = new ArrayList<>();
        for (SupportTicketEntity ticket : tickets) {
            // 가게 정보 조회
            StoreEntity store = storeRepository.findById(ticket.getStoreId())
                    .orElse(null);
            String storeName = (store != null) ? store.getName() : "(삭제된 가게)";
            
            responses.add(SupportTicketResponse.from(ticket, storeName));
        }
        return responses;
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
        
        // 가게 정보 조회
        StoreEntity store = storeRepository.findById(saved.getStoreId())
                .orElse(null);
        String storeName = (store != null) ? store.getName() : "(삭제된 가게)";
        
        return SupportTicketResponse.from(saved, storeName);
    }
}
