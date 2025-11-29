-- db/schema.sql

-- DB가 없는 경우 생성 (docker-compose에서 MYSQL_DATABASE로도 생성되지만, 안전하게 한 번 더)
CREATE DATABASE IF NOT EXISTS wheats
  DEFAULT CHARACTER SET utf8mb4
  DEFAULT COLLATE utf8mb4_unicode_ci;

USE wheats;

-- 기존에 같은 이름의 테이블이 있으면 삭제
DROP TABLE IF EXISTS stores;

-- 가게 정보 테이블
CREATE TABLE stores (
                        id              BIGINT AUTO_INCREMENT PRIMARY KEY,   -- 가게 ID
                        name            VARCHAR(100) NOT NULL,               -- 가게 이름
                        category        VARCHAR(50),                         -- 한식, 분식, 치킨 등
                        description     TEXT,                                -- 가게 소개
                        min_order_price INT NOT NULL,                        -- 최소 주문 금액 (원)
                        delivery_tip    INT NOT NULL,                        -- 배달팁 (원)
                        rating          DECIMAL(2,1),                        -- 평점 (예: 4.5)
                        review_count    INT DEFAULT 0,                       -- 리뷰 개수
                        is_open         TINYINT(1) DEFAULT 1,                -- 영업 중 여부 (1: 영업, 0: 휴무)
                        image_url       TEXT,                                -- 썸네일 이미지 경로/URL
                        created_at      DATETIME DEFAULT CURRENT_TIMESTAMP,
                        updated_at      DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);
