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

-- 유저 테이블
CREATE TABLE IF NOT EXISTS users (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  name VARCHAR(100) NOT NULL,
  email VARCHAR(255) NOT NULL,
  role ENUM('CONSUMER', 'MERCHANT', 'ADMIN') NOT NULL DEFAULT 'CONSUMER',
  point INT NOT NULL DEFAULT 100000,
  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  UNIQUE KEY uk_users_email (email)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- OAuth 계정 매핑 테이블
CREATE TABLE IF NOT EXISTS oauth_accounts (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  user_id BIGINT NOT NULL,
  provider VARCHAR(50) NOT NULL,
  provider_id VARCHAR(255) NOT NULL,
  CONSTRAINT fk_oauth_user
    FOREIGN KEY (user_id) REFERENCES users(id)
    ON DELETE CASCADE,
  UNIQUE KEY uk_oauth_provider (provider, provider_id),
  KEY idx_oauth_user (user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 장바구니 테이블
CREATE TABLE IF NOT EXISTS carts (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  user_id BIGINT NOT NULL,
  store_id BIGINT NOT NULL,
  status ENUM('ACTIVE', 'ORDERED', 'ABANDONED') NOT NULL DEFAULT 'ACTIVE',
  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  CONSTRAINT fk_carts_user
    FOREIGN KEY (user_id) REFERENCES users(id)
    ON DELETE CASCADE,
  CONSTRAINT fk_carts_store
    FOREIGN KEY (store_id) REFERENCES stores(id)
    ON DELETE CASCADE,
  KEY idx_carts_user (user_id),
  KEY idx_carts_store (store_id),
  UNIQUE KEY uk_carts_user_store_active (user_id, store_id, status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 장바구니 아이템 테이블
CREATE TABLE IF NOT EXISTS cart_items (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  cart_id BIGINT NOT NULL,
  menu_id BIGINT NOT NULL,
  quantity INT NOT NULL,
  CONSTRAINT fk_cart_items_cart
    FOREIGN KEY (cart_id) REFERENCES carts(id)
    ON DELETE CASCADE,
  CONSTRAINT fk_cart_items_menu
    FOREIGN KEY (menu_id) REFERENCES menus(id)
    ON DELETE CASCADE,
  KEY idx_cart_items_cart (cart_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 주문 테이블
CREATE TABLE IF NOT EXISTS orders (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  order_number VARCHAR(50) NOT NULL,
  user_id BIGINT NOT NULL,
  store_id BIGINT NOT NULL,
  cart_id BIGINT NOT NULL,
  status ENUM('PENDING', 'PAID', 'CANCELLED') NOT NULL DEFAULT 'PENDING',
  total_price INT NOT NULL,
  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  paid_at TIMESTAMP NULL DEFAULT NULL,
  receipt_flag VARCHAR(255) DEFAULT NULL, -- CTF용 플래그
  CONSTRAINT fk_orders_user
    FOREIGN KEY (user_id) REFERENCES users(id)
    ON DELETE CASCADE,
  CONSTRAINT fk_orders_store
    FOREIGN KEY (store_id) REFERENCES stores(id)
    ON DELETE CASCADE,
  CONSTRAINT fk_orders_cart
    FOREIGN KEY (cart_id) REFERENCES carts(id)
    ON DELETE RESTRICT,
  UNIQUE KEY uk_orders_order_number (order_number),
  KEY idx_orders_user_created (user_id, created_at),
  KEY idx_orders_store_created (store_id, created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 주문 아이템 테이블
CREATE TABLE IF NOT EXISTS order_items (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  order_id BIGINT NOT NULL,
  menu_id BIGINT NOT NULL,
  quantity INT NOT NULL,
  unit_price INT NOT NULL,
  CONSTRAINT fk_order_items_order
    FOREIGN KEY (order_id) REFERENCES orders(id)
    ON DELETE CASCADE,
  CONSTRAINT fk_order_items_menu
    FOREIGN KEY (menu_id) REFERENCES menus(id)
    ON DELETE CASCADE,
  KEY idx_order_items_order (order_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 1:1 문의(고객센터 티켓) 테이블
CREATE TABLE IF NOT EXISTS support_tickets (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  user_id BIGINT NOT NULL,
  store_id BIGINT NOT NULL,
  title VARCHAR(200) NOT NULL,
  message TEXT NOT NULL,
  status ENUM('OPEN', 'ANSWERED', 'CLOSED') NOT NULL DEFAULT 'OPEN',
  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  CONSTRAINT fk_tickets_user
    FOREIGN KEY (user_id) REFERENCES users(id)
    ON DELETE CASCADE,
  CONSTRAINT fk_tickets_store
    FOREIGN KEY (store_id) REFERENCES stores(id)
    ON DELETE CASCADE,
  KEY idx_tickets_user_created (user_id, created_at),
  KEY idx_tickets_store (store_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;