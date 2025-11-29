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
