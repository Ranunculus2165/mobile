-- DB 생성
CREATE DATABASE IF NOT EXISTS wheats
  DEFAULT CHARACTER SET utf8mb4
  COLLATE utf8mb4_unicode_ci;

USE wheats;

-- 가게 테이블
CREATE TABLE IF NOT EXISTS stores (
                                      id BIGINT AUTO_INCREMENT PRIMARY KEY,
                                      name VARCHAR(100) NOT NULL,
    category VARCHAR(50) NOT NULL,
    description TEXT,
    min_order_price INT,
    delivery_tip INT,
    rating DOUBLE,
    review_count INT,
    is_open TINYINT(1) DEFAULT 1,
    image_url VARCHAR(255),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
    ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- ✅ 메뉴 테이블 추가
CREATE TABLE IF NOT EXISTS menus (
                                     id BIGINT AUTO_INCREMENT PRIMARY KEY,
                                     store_id BIGINT NOT NULL,
                                     name VARCHAR(100) NOT NULL,
    price INT NOT NULL,
    description TEXT,
    is_available TINYINT(1) DEFAULT 1,
    image_url VARCHAR(255),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_menus_store
    FOREIGN KEY (store_id) REFERENCES stores(id)
    ON DELETE CASCADE
    ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
