-- 사용자 테이블
CREATE TABLE users (
id UUID PRIMARY KEY, -- 사용자 고유 ID
email VARCHAR(255) NOT NULL,
name VARCHAR(255) NOT NULL,
role VARCHAR(255) NOT NULL, -- 사용자 역할: CONSUMER, SELLER, ADMIN
provider VARCHAR(255) NOT NULL, -- 로그인 제공자: KAKAO, NAVER
provider_id VARCHAR(255) NOT NULL, -- 소셜 로그인 ID
created_at TIMESTAMP NOT NULL, -- 가입 시각
updated_at TIMESTAMP NOT NULL,
created_by VARCHAR(255),
last_modified_by VARCHAR(255),
is_delete BOOLEAN NOT NULL DEFAULT FALSE
);

-- 판매자 정보 테이블
CREATE TABLE sellers (
user_id UUID PRIMARY KEY, -- users.id 와 1:1 관계
business_number VARCHAR(255) NOT NULL, -- 사업자등록번호
store_name VARCHAR(255) NOT NULL, -- 상호명
approval_status VARCHAR(255) NOT NULL, -- 승인 상태: PENDING, APPROVED, REJECTED
-- 판매점 주소 정보 (임베디드)
postal_code VARCHAR(255), -- 우편번호
address VARCHAR(255), -- 주소
detail_address VARCHAR(255), -- 상세주소
created_at TIMESTAMP NOT NULL,
updated_at TIMESTAMP NOT NULL,
created_by VARCHAR(255),
last_modified_by VARCHAR(255),
is_delete BOOLEAN NOT NULL DEFAULT FALSE
);

-- 판매자 제출 문서
CREATE TABLE seller_documents (
id UUID PRIMARY KEY,
seller_id UUID NOT NULL, -- sellers.user_id
type VARCHAR(255) NOT NULL, -- 문서 종류: BUSINESS_LICENSE, CONSENT_FORM
file_url VARCHAR(255) NOT NULL, -- S3 저장 위치
uploaded_at TIMESTAMP NOT NULL,
created_at TIMESTAMP NOT NULL,
updated_at TIMESTAMP NOT NULL,
created_by VARCHAR(255),
last_modified_by VARCHAR(255),
is_delete BOOLEAN NOT NULL DEFAULT FALSE
);

-- 견적 요청
CREATE TABLE quotes (
id UUID PRIMARY KEY,
user_id UUID NOT NULL, -- 견적 요청자 (users.id)
model VARCHAR(255) NOT NULL, -- 기종명 (예: iPhone 16)
storage VARCHAR(255) NOT NULL, -- 저장 용량 (예: 128GB)
carrier VARCHAR(255) NOT NULL, -- 통신사: SKT, KT, LGU
color VARCHAR(255) NOT NULL,
status VARCHAR(255) NOT NULL, -- OPEN, CLOSED, CONTRACTED
expired_at TIMESTAMP NOT NULL, -- 경매 마감 시각
-- 새로운 필드들
purchase_method VARCHAR(255), -- 구매방법: NUMBER_TRANSFER, DEVICE_CHANGE, NEW_SUBSCRIPTION, ANY
current_carrier VARCHAR(255), -- 기존 통신사 (번호이동/기기변경 시)
activation_method VARCHAR(255), -- 개통방법: SELECTIVE_SUBSIDY, COMMON_SUBSIDY, ANY
created_at TIMESTAMP NOT NULL,
updated_at TIMESTAMP NOT NULL,
created_by VARCHAR(255),
last_modified_by VARCHAR(255),
is_delete BOOLEAN NOT NULL DEFAULT FALSE
);

-- 입찰 정보
CREATE TABLE bids (
id UUID PRIMARY KEY,
quote_id UUID NOT NULL, -- 대상 견적
seller_id UUID NOT NULL, -- 입찰자 (sellers.user_id)
price INTEGER NOT NULL, -- 입찰가
delivery_days INTEGER NOT NULL, -- 배송 예상일
rating_snapshot DOUBLE PRECISION, -- 당시 평점 스냅샷
-- 새로운 필드들
purchase_method VARCHAR(255) NOT NULL, -- 구매방법: NUMBER_TRANSFER, DEVICE_CHANGE, NEW_SUBSCRIPTION
carrier VARCHAR(255) NOT NULL, -- 통신사 (이동할/사용할 통신사)
current_carrier VARCHAR(255), -- 기존 통신사 (번호이동/기기변경 시)
activation_method VARCHAR(255) NOT NULL, -- 개통방법: SELECTIVE_SUBSIDY, COMMON_SUBSIDY
additional_subsidy INTEGER, -- 추가지원금
installment_principal INTEGER, -- 할부원금
additional_services VARCHAR(500), -- 부가서비스
-- 요금제 정보 (임베디드)
plan_name VARCHAR(255), -- 요금제 이름
plan_price INTEGER, -- 요금제 가격
contract_months INTEGER, -- 약정개월
created_at TIMESTAMP NOT NULL,
updated_at TIMESTAMP NOT NULL,
created_by VARCHAR(255),
last_modified_by VARCHAR(255),
is_delete BOOLEAN NOT NULL DEFAULT FALSE
);

-- 입찰 수정 이력
CREATE TABLE bid_history (
id UUID PRIMARY KEY,
bid_id UUID NOT NULL, -- 연관 입찰
version INTEGER NOT NULL, -- 수정 버전
price INTEGER NOT NULL,
delivery_days INTEGER NOT NULL,
created_at TIMESTAMP NOT NULL,
updated_at TIMESTAMP NOT NULL,
created_by VARCHAR(255),
last_modified_by VARCHAR(255),
is_delete BOOLEAN NOT NULL DEFAULT FALSE
);

-- 계약
CREATE TABLE contracts (
id UUID PRIMARY KEY,
quote_id UUID NOT NULL, -- 연관 견적
bid_id UUID NOT NULL UNIQUE, -- 선택된 입찰 (1:1 관계)
status VARCHAR(255) NOT NULL, -- 계약 상태: SIGNING, SIGNED, CANCELLED
signed_at TIMESTAMP,
created_at TIMESTAMP NOT NULL,
updated_at TIMESTAMP NOT NULL,
created_by VARCHAR(255),
last_modified_by VARCHAR(255),
is_delete BOOLEAN NOT NULL DEFAULT FALSE
);

-- 결제 정보
CREATE TABLE payments (
id UUID PRIMARY KEY,
contract_id UUID NOT NULL UNIQUE, -- 계약 ID (1:1 관계)
amount INTEGER NOT NULL, -- 결제 금액
method VARCHAR(255) NOT NULL, -- CARD, BANK, MOBILE
status VARCHAR(255) NOT NULL, -- REQUESTED, PENDING_APPROVAL, PAID, FAILED
pg_tid VARCHAR(255) NOT NULL, -- PG 거래 ID
paid_at TIMESTAMP,
created_at TIMESTAMP NOT NULL,
updated_at TIMESTAMP NOT NULL,
created_by VARCHAR(255),
last_modified_by VARCHAR(255),
is_delete BOOLEAN NOT NULL DEFAULT FALSE
);

-- 배송 정보
CREATE TABLE deliveries (
id UUID PRIMARY KEY,
contract_id UUID NOT NULL UNIQUE, -- 계약 ID (1:1 관계)
courier VARCHAR(255) NOT NULL, -- 택배사: CJ_LOGISTICS, HANJIN, LOTTE
invoice_number VARCHAR(255) NOT NULL, -- 송장 번호
status VARCHAR(255) NOT NULL, -- 배송 상태: READY, SHIPPED, DELIVERED
shipped_at TIMESTAMP,
delivered_at TIMESTAMP,
delivery_memo VARCHAR(500), -- 배송 메모
created_at TIMESTAMP NOT NULL,
updated_at TIMESTAMP NOT NULL,
created_by VARCHAR(255),
last_modified_by VARCHAR(255),
is_delete BOOLEAN NOT NULL DEFAULT FALSE
);

-- 사용자 알림
CREATE TABLE notifications (
id UUID PRIMARY KEY,
user_id UUID NOT NULL, -- 수신 대상
type VARCHAR(255) NOT NULL, -- 알림 유형: QUOTE_CREATED, BID_ARRIVED 등
channel VARCHAR(255) NOT NULL, -- KAKAO, PUSH, EMAIL
title VARCHAR(255) NOT NULL, -- 알림 제목
content TEXT NOT NULL, -- 알림 내용
related_entity_type VARCHAR(255), -- 관련 엔터티 타입
related_entity_id UUID, -- 관련 엔터티 ID
is_read BOOLEAN NOT NULL DEFAULT FALSE, -- 읽음 여부
read_at TIMESTAMP, -- 읽은 시각
sent_at TIMESTAMP, -- 발송 시각
created_at TIMESTAMP NOT NULL,
updated_at TIMESTAMP NOT NULL,
created_by VARCHAR(255),
last_modified_by VARCHAR(255),
is_delete BOOLEAN NOT NULL DEFAULT FALSE
);

-- 인덱스 생성
CREATE INDEX idx_users_email ON users(email);
CREATE INDEX idx_users_provider ON users(provider, provider_id);
CREATE INDEX idx_sellers_approval_status ON sellers(approval_status);
CREATE INDEX idx_quotes_user_id ON quotes(user_id);
CREATE INDEX idx_quotes_status ON quotes(status);
CREATE INDEX idx_quotes_expired_at ON quotes(expired_at);
CREATE INDEX idx_bids_quote_id ON bids(quote_id);
CREATE INDEX idx_bids_seller_id ON bids(seller_id);
CREATE INDEX idx_bid_history_bid_id ON bid_history(bid_id);
CREATE INDEX idx_contracts_quote_id ON contracts(quote_id);
CREATE INDEX idx_contracts_bid_id ON contracts(bid_id);
CREATE INDEX idx_payments_contract_id ON payments(contract_id);
CREATE INDEX idx_deliveries_contract_id ON deliveries(contract_id);
CREATE INDEX idx_notifications_user_id ON notifications(user_id);
CREATE INDEX idx_notifications_type ON notifications(type);
CREATE INDEX idx_notifications_is_read ON notifications(is_read);

-- 외래 키 설정
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
