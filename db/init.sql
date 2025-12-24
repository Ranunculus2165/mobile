USE wheats;

-- =========================
-- STORES (5개)
-- =========================
INSERT INTO stores
(name, category, description, min_order_price, delivery_tip, rating, review_count, is_open, image_url)
VALUES
    ('황금치킨', '치킨', '바삭함과 촉촉함을 동시에 살린 시그니처 후라이드 전문점',
     16000, 2000, 4.8, 320, 1,
     'https://wheats-cdn.com/images/chicken1.png'),

    ('두끼분식', '분식', '따끈한 국물 떡볶이와 매콤한 로제 떡볶이가 인기 메뉴인 분식집',
     9000, 1000, 4.5, 210, 1,
     'https://wheats-cdn.com/images/bunsik1.png'),

    ('돈카츠공방', '돈까스', '수제 생등심 돈카츠와 치즈 카츠가 유명한 정통 일본식 돈까스 전문점',
     13000, 2500, 4.7, 142, 1,
     'https://wheats-cdn.com/images/katsu1.png'),

    ('버거아일랜드', '패스트푸드', '직화 그릴로 구워낸 풍미 깊은 수제 버거 전문점',
     12000, 3000, 4.3, 87, 1,
     'https://wheats-cdn.com/images/burger1.png'),

    ('카페소풍', '카페·디저트', '핸드드립 커피와 직접 굽는 스콘·쿠키가 인기 메뉴인 감성 카페',
     7000, 1500, 4.6, 65, 1,
     'https://wheats-cdn.com/images/cafe1.png');

-- =========================
-- MENUS (각 가게 4개씩)
-- store_id 는 1~5 라고 가정
-- =========================

-- 1번 가게: 황금치킨
INSERT INTO menus (store_id, name, price, description, is_available, image_url) VALUES
    (1, '후라이드 치킨', 16000, '겉은 바삭, 속은 촉촉한 대표 메뉴', 1, NULL),
    (1, '양념 치킨',    17000, '달콤 매콤한 시그니처 양념치킨', 1, NULL),
    (1, '간장 치킨',    17000, '짭짤하고 고소한 간장치킨', 1, NULL),
    (1, '치즈볼',        5000, '한 입에 쏙 들어가는 치즈볼', 1, NULL);

-- 2번 가게: 두끼분식
INSERT INTO menus (store_id, name, price, description, is_available, image_url) VALUES
    (2, '국물 떡볶이', 8000, '진한 국물의 클래식 떡볶이', 1, NULL),
    (2, '로제 떡볶이', 9000, '크림이 들어간 매콤 로제 떡볶이', 1, NULL),
    (2, '김밥',        5000, '정성껏 말아낸 기본 김밥', 1, NULL),
    (2, '튀김 모둠',    7000, '야채튀김, 오징어튀김, 김말이 세트', 1, NULL);

-- 3번 가게: 돈카츠공방
INSERT INTO menus (store_id, name, price, description, is_available, image_url) VALUES
    (3, '등심 돈카츠', 12000, '두툼한 등심을 사용한 정통 돈카츠', 1, NULL),
    (3, '치즈 돈카츠', 13000, '치즈를 듬뿍 넣은 치즈카츠', 1, NULL),
    (3, '카레 돈카츠', 13000, '돈카츠 위에 카레를 얹은 메뉴', 1, NULL),
    (3, '고구마 고로케', 7000, '달콤한 고구마가 들어간 고로케', 1, NULL);

-- 4번 가게: 버거아일랜드
INSERT INTO menus (store_id, name, price, description, is_available, image_url) VALUES
    (4, '클래식 버거', 9000, '기본에 충실한 클래식 수제버거', 1, NULL),
    (4, '치즈 버거',  10000, '체다 치즈를 올린 버거', 1, NULL),
    (4, '더블 패티 버거', 12000, '패티를 두 장 사용한 푸짐한 버거', 1, NULL),
    (4, '프렌치 프라이', 4000, '바삭하게 튀긴 감자튀김', 1, NULL);

-- 5번 가게: 카페소풍
INSERT INTO menus (store_id, name, price, description, is_available, image_url) VALUES
    (5, '아메리카노', 4500, '직접 로스팅한 원두로 내린 커피', 1, NULL),
    (5, '카페라떼', 5000, '우유 거품이 부드러운 라떼', 1, NULL),
    (5, '수제 스콘', 4000, '매일매일 구워내는 플레인 스콘', 1, NULL),
    (5, '초코 쿠키', 3500, '진한 초코 맛이 나는 쿠키', 1, NULL);

-- =========================
-- USERS (테스트용 2명)
-- =========================
INSERT INTO users (
  id, name, email, role, point, created_at
) VALUES
  (
    1,
    '테스트 소비자',
    'consumer1@wheats.local',
    'CONSUMER',
    1000000,
    NOW()
  ),
  (
    2,
    '테스트 점주',
    'merchant1@wheats.local',
    'MERCHANT',
    1000000,
    NOW()
  ),
  (
    3,
    '테스트 관리자',
    'admin1@wheats.local',
    'ADMIN',
    1000000,
    NOW()
  );

  INSERT INTO oauth_accounts (
    id, user_id, provider, provider_id
  ) VALUES
  (
    1,
    1,
    'GOOGLE',
    '1234567890'
  ),
  (
    2,
    2,
    'GOOGLE',
    '1234567890'
  ),
  (
    3,
    3,
    'GOOGLE',
    '1234567890'
  );

-- =========================
-- CART (user_id = 1, store_id = 1)
-- =========================
INSERT INTO carts (
  id, user_id, store_id, status, created_at, updated_at
) VALUES (
  1,
  1,              -- 테스트 소비자
  1,              -- 1번 가게: 황금치킨
  'ACTIVE',       -- 현재 장바구니 ( /api/cart 테스트용 )
  NOW(),
  NOW()
);

-- =========================
-- CART_ITEMS (장바구니 안에 메뉴 2개)
-- 1번 가게(황금치킨)의 메뉴는 위에서 자동으로 id 1~4가 됐다고 가정
--   1: 후라이드 치킨 16000
--   4: 치즈볼        5000
-- =========================
INSERT INTO cart_items (
  id, cart_id, menu_id, quantity
) VALUES
  (
    1,
    1,    -- cart_id
    1,    -- 메뉴 id 1: 후라이드 치킨
    1
  ),
  (
    2,
    1,    -- cart_id
    4,    -- 메뉴 id 4: 치즈볼
    2
  );

-- =========================
-- ORDERS (user_id = 1, store_id = 1, cart_id = 1)
-- 총액은 메뉴 가격 기준 예시:
--   주문 1: 후라이드 1(16000) + 치즈볼 2(2*5000) = 26000
--   주문 2: 후라이드 1(16000) = 16000
-- =========================
INSERT INTO orders (
  id,
  order_number,
  user_id,
  store_id,
  cart_id,
  status,
  total_price,
  created_at,
  paid_at,
  receipt_flag
) VALUES
  (
    1,
    'ORD-TEST-0001',
    1,           -- 테스트 소비자
    1,           -- 황금치킨
    1,           -- cart_id 1을 사용 (과거 이 장바구니로 주문한 것으로 가정)
    'PAID',
    26000,
    NOW() - INTERVAL 2 DAY,
    NOW() - INTERVAL 2 DAY,
    'WHEATS{DUMMY_FLAG_1}'
  ),
  (
    2,
    'ORD-TEST-0002',
    1,
    1,
    1,
    'PAID',
    16000,
    NOW() - INTERVAL 1 DAY,
    NOW() - INTERVAL 1 DAY,
    'WHEATS{DUMMY_FLAG_2}'
  );

-- =========================
-- ORDER_ITEMS (각 주문 상세)
-- =========================
INSERT INTO order_items (
  id, order_id, menu_id, quantity, unit_price
) VALUES
  -- 주문 1: 후라이드 1 + 치즈볼 2
  (
    1,
    1,      -- ORD-TEST-0001
    1,      -- 후라이드 치킨
    1,
    16000
  ),
  (
    2,
    1,      -- ORD-TEST-0001
    4,      -- 치즈볼
    2,
    5000
  ),
  -- 주문 2: 후라이드 1
  (
    3,
    2,      -- ORD-TEST-0002
    1,      -- 후라이드 치킨
    1,
    16000
  );
