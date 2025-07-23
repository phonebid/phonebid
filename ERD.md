-- 사용자 테이블
CREATE TABLE users (
    id UUID PRIMARY KEY, -- 사용자 고유 ID
    email VARCHAR(255) NOT NULL,
    name VARCHAR(255) NOT NULL,
    role VARCHAR(255) NOT NULL, -- 사용자 역할: CONSUMER, SELLER, ADMIN
    provider VARCHAR(255) NOT NULL, -- 로그인 제공자: KAKAO, NAVER
    provider_id VARCHAR(255) NOT NULL, -- 소셜 로그인 ID
    created_at TIMESTAMP NOT NULL -- 가입 시각
);

-- 판매자 정보 테이블
CREATE TABLE sellers (
    user_id UUID PRIMARY KEY, -- users.id 와 1:1 관계
    business_number VARCHAR(255) NOT NULL, -- 사업자등록번호
    store_name VARCHAR(255) NOT NULL, -- 상호명
    approval_status VARCHAR(255) NOT NULL, -- 승인 상태: PENDING, APPROVE, REJECTED
    created_at TIMESTAMP NOT NULL
);

-- 판매자 제출 문서
CREATE TABLE seller_documents (
    id UUID PRIMARY KEY,
    seller_id UUID, -- sellers.user_id
    type VARCHAR(255), -- 문서 종류: BUSINESS_LICENSE, CONSENT_FORM
    file_url VARCHAR(255), -- S3 저장 위치
    uploaded_at TIMESTAMP
);

-- 견적 요청
CREATE TABLE quotes (
    id UUID PRIMARY KEY,
    user_id UUID NOT NULL, -- 견적 요청자 (users.id)
    model VARCHAR(255) NOT NULL, -- 기종명 (예: iPhone 16)
    storage VARCHAR(255) NOT NULL, -- 저장 용량 (예: 128GB)
    carrier VARCHAR(255) NOT NULL, -- 통신사: SKT, KT, LGU+
    color VARCHAR(255) NOT NULL,
    status VARCHAR(255) NOT NULL, -- OPEN, CLOSED, CONTRACTED
    expired_at TIMESTAMP NOT NULL, -- 경매 마감 시각
    created_at TIMESTAMP NOT NULL
);

-- 입찰 정보
CREATE TABLE bids (
    id UUID PRIMARY KEY,
    quote_id UUID NOT NULL, -- 대상 견적
    seller_id UUID NOT NULL, -- 입찰자 (sellers.user_id)
    price INTEGER NOT NULL, -- 입찰가
    delivery_days INTEGER NOT NULL, -- 배송 예상일
    rating_snapshot DOUBLE PRECISION, -- 당시 평점 스냅샷
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL
);

-- 입찰 수정 이력
CREATE TABLE bid_history (
    id UUID PRIMARY KEY,
    bid_id UUID NOT NULL, -- 연관 입찰
    version INTEGER NOT NULL, -- 수정 버전
    price INTEGER NOT NULL,
    delivery_days INTEGER NOT NULL,
    created_at TIMESTAMP NOT NULL
);

-- 계약
CREATE TABLE contracts (
    id UUID PRIMARY KEY,
    quote_id UUID NOT NULL, -- 연관 견적
    bid_id UUID NOT NULL, -- 선택된 입찰
    status VARCHAR(255) NOT NULL, -- 계약 상태: SIGNING, SIGNED, CANCELLED
    signed_at TIMESTAMP
);

-- 결제 정보
CREATE TABLE payments (
    id UUID PRIMARY KEY,
    contract_id UUID NOT NULL, -- 계약 ID
    amount INTEGER NOT NULL, -- 결제 금액
    method VARCHAR(255) NOT NULL, -- CARD, BANK, MOBILE
    status VARCHAR(255) NOT NULL, -- REQUESTED, PENDING_APPROVAL, PAID, FAILED
    pg_tid VARCHAR(255) NOT NULL, -- PG 거래 ID
    paid_at TIMESTAMP
);

-- 배송 정보
CREATE TABLE deliveries (
    id UUID PRIMARY KEY,
    contract_id UUID NOT NULL, -- 계약 ID
    courier VARCHAR(255) NOT NULL, -- 택배사: CJ_LOGISTICS, HANJIN, LOTTE
    invoice_number VARCHAR(255) NOT NULL, -- 송장 번호
    status VARCHAR(255) NOT NULL, -- 배송 상태: READY, SHIPPED, DELIVERED
    shipped_at TIMESTAMP
);

-- 사용자 알림
CREATE TABLE notifications (
    id UUID PRIMARY KEY,
    user_id UUID NOT NULL, -- 수신 대상
    type VARCHAR(255) NOT NULL, -- 알림 유형: QUOTE_CREATED, BID_ARRIVED 등
    channel VARCHAR(255) NOT NULL, -- KAKAO, PUSH, EMAIL
    is_read BOOLEAN NOT NULL, -- 읽음 여부
    created_at TIMESTAMP NOT NULL
);

-- 외래 키 설정
ALTER TABLE users ADD CONSTRAINT fk_users_seller FOREIGN KEY (id) REFERENCES sellers(user_id);
ALTER TABLE sellers ADD CONSTRAINT fk_sellers_user FOREIGN KEY (user_id) REFERENCES users(id);
ALTER TABLE seller_documents ADD CONSTRAINT fk_documents_seller FOREIGN KEY (seller_id) REFERENCES sellers(user_id);
ALTER TABLE quotes ADD CONSTRAINT fk_quotes_user FOREIGN KEY (user_id) REFERENCES users(id);
ALTER TABLE bids ADD CONSTRAINT fk_bids_quote FOREIGN KEY (quote_id) REFERENCES quotes(id);
ALTER TABLE bids ADD CONSTRAINT fk_bids_seller FOREIGN KEY (seller_id) REFERENCES sellers(user_id);
ALTER TABLE bid_history ADD CONSTRAINT fk_history_bid FOREIGN KEY (bid_id) REFERENCES bids(id);
ALTER TABLE contracts ADD CONSTRAINT fk_contracts_quote FOREIGN KEY (quote_id) REFERENCES quotes(id);
ALTER TABLE contracts ADD CONSTRAINT fk_contracts_bid FOREIGN KEY (bid_id) REFERENCES bids(id);
ALTER TABLE payments ADD CONSTRAINT fk_payments_contract FOREIGN KEY (contract_id) REFERENCES contracts(id);
ALTER TABLE deliveries ADD CONSTRAINT fk_deliveries_contract FOREIGN KEY (contract_id) REFERENCES contracts(id);
ALTER TABLE notifications ADD CONSTRAINT fk_notifications_user FOREIGN KEY (user_id) REFERENCES users(id);