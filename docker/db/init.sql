-- 초기 유저/점포 등 seed 데이터
INSERT INTO users (email, password_hash, name, role)
VALUES
('owner1@example.com', 'hashed_password_here', '점주1', 'OWNER'),
('user1@example.com', 'hashed_pw_user1', '고객1', 'CUSTOMER');

