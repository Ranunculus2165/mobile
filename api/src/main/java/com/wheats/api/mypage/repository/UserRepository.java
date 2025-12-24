package com.wheats.api.mypage.repository;

import com.wheats.api.mypage.entity.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserRepository extends JpaRepository<UserEntity, Long> {
    /**
     * 이메일로 사용자 조회
     * @param email 이메일
     * @return 사용자 엔티티 (Optional)
     */
    java.util.Optional<UserEntity> findByEmail(String email);
}
