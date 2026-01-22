-- 사용자 테이블
CREATE TABLE users (
id UUID PRIMARY KEY, -- 사용자 고유 ID
username VARCHAR(255) NOT NULL UNIQUE, -- 유저ID (이메일 형식 허용)
password VARCHAR(255) NOT NULL, -- 비밀번호
email VARCHAR(255) NOT NULL UNIQUE, -- 이메일
name VARCHAR(255) NOT NULL, -- 이름
nickname VARCHAR(10) NOT NULL, -- 닉네임 (2-10자)
phone VARCHAR(20), -- 전화번호 (숫자만)
role VARCHAR(255) NOT NULL, -- 사용자 역할: CONSUMER, SELLER, ADMIN
provider VARCHAR(255), -- 로그인 제공자: KAKAO, NAVER
provider_id VARCHAR(255), -- 소셜 로그인 ID
created_at TIMESTAMP NOT NULL, -- 가입 시각
updated_at TIMESTAMP NOT NULL,
created_by VARCHAR(255),
last_modified_by VARCHAR(255),
is_delete BOOLEAN NOT NULL DEFAULT FALSE
);

-- Refresh Token 테이블
CREATE TABLE refresh_tokens (
id UUID PRIMARY KEY, -- Refresh Token 고유 ID
user_id UUID NOT NULL, -- 사용자 ID (users.id)
token VARCHAR(500) NOT NULL UNIQUE, -- Refresh Token 값
expires_at TIMESTAMP NOT NULL, -- 만료 시각
created_at TIMESTAMP NOT NULL, -- BaseEntity
updated_at TIMESTAMP NOT NULL,
created_by VARCHAR(255),
updated_by VARCHAR(255),
deleted_at TIMESTAMP,
deleted_by VARCHAR(255),
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
uploaded_at TIMESTAMP NOT NULL, -- 업로드 시각
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
phone_model_id UUID NOT NULL, -- 기종 (phone_models.id)
storage UUID, -- 저장 용량 옵션 (phone_options.id, nullable: 상관없음 선택 시)
carrier VARCHAR(255) NOT NULL, -- 통신사: SKT, KT, LGU
color UUID, -- 색상 옵션 (phone_options.id, nullable: 상관없음 선택 시)
status VARCHAR(255) NOT NULL, -- OPEN, CLOSED, CONTRACTED
expired_at TIMESTAMP NOT NULL, -- 경매 마감 시각
-- 구매 조건 필드들
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
-- 구매 조건 필드들
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

-- 인덱스 생성
CREATE INDEX idx_users_email ON users(email);
CREATE INDEX idx_users_provider ON users(provider, provider_id);
CREATE INDEX idx_refresh_tokens_user_id ON refresh_tokens(user_id);
CREATE INDEX idx_refresh_tokens_token ON refresh_tokens(token);
CREATE INDEX idx_sellers_approval_status ON sellers(approval_status);
CREATE INDEX idx_quotes_user_id ON quotes(user_id);
CREATE INDEX idx_quotes_status ON quotes(status);
CREATE INDEX idx_quotes_expired_at ON quotes(expired_at);
CREATE INDEX idx_bids_quote_id ON bids(quote_id);
CREATE INDEX idx_bids_seller_id ON bids(seller_id);
CREATE INDEX idx_bid_history_bid_id ON bid_history(bid_id);
CREATE INDEX idx_seller_documents_seller_id ON seller_documents(seller_id);
CREATE INDEX idx_seller_documents_type ON seller_documents(type);

-- 외래 키 설정
ALTER TABLE refresh_tokens ADD CONSTRAINT fk_refresh_tokens_user FOREIGN KEY (user_id) REFERENCES users(id);
ALTER TABLE sellers ADD CONSTRAINT fk_sellers_user FOREIGN KEY (user_id) REFERENCES users(id);
ALTER TABLE seller_documents ADD CONSTRAINT fk_documents_seller FOREIGN KEY (seller_id) REFERENCES sellers(user_id);
ALTER TABLE quotes ADD CONSTRAINT fk_quotes_user FOREIGN KEY (user_id) REFERENCES users(id);
ALTER TABLE quotes ADD CONSTRAINT fk_quotes_phone_model FOREIGN KEY (phone_model_id) REFERENCES phone_models(id);
ALTER TABLE quotes ADD CONSTRAINT fk_quotes_storage FOREIGN KEY (storage) REFERENCES phone_options(id);
ALTER TABLE quotes ADD CONSTRAINT fk_quotes_color FOREIGN KEY (color) REFERENCES phone_options(id);
ALTER TABLE bids ADD CONSTRAINT fk_bids_quote FOREIGN KEY (quote_id) REFERENCES quotes(id);
ALTER TABLE bids ADD CONSTRAINT fk_bids_seller FOREIGN KEY (seller_id) REFERENCES sellers(user_id);
ALTER TABLE bid_history ADD CONSTRAINT fk_history_bid FOREIGN KEY (bid_id) REFERENCES bids(id);

-- 휴대폰 카탈로그 테이블
CREATE TABLE phone_brands (
id UUID PRIMARY KEY, -- UUID PK (전역 고유)
name VARCHAR(255) NOT NULL UNIQUE, -- 브랜드명 유니크: "Apple", "Samsung"
created_at TIMESTAMP NOT NULL, -- BaseEntity
updated_at TIMESTAMP NOT NULL,
created_by VARCHAR(255),
last_modified_by VARCHAR(255),
is_delete BOOLEAN NOT NULL DEFAULT FALSE
);

CREATE TABLE phone_models (
id UUID PRIMARY KEY,
brand_id UUID NOT NULL, -- FK: phone_brands.id
model VARCHAR(255) NOT NULL, -- 모델명 예: "iPhone 16", "Galaxy S24"
model_number VARCHAR(255), -- 제조사 모델 번호 예: "A3101"(선택)
released_price INTEGER, -- 출시가(원)
released_at DATE, -- 출시일
created_at TIMESTAMP NOT NULL, -- BaseEntity
updated_at TIMESTAMP NOT NULL,
created_by VARCHAR(255),
last_modified_by VARCHAR(255),
is_delete BOOLEAN NOT NULL DEFAULT FALSE
);

ALTER TABLE phone_models ADD CONSTRAINT fk_phone_models_brand FOREIGN KEY (brand_id) REFERENCES phone_brands(id);

-- 동일 브랜드 내 동일 모델명 중복 방지
CREATE UNIQUE INDEX uq_phone_models_brand_model ON phone_models(brand_id, model);
-- 자주 조회되는 FK 인덱스
CREATE INDEX idx_phone_models_brand_id ON phone_models(brand_id);

CREATE TABLE phone_options (
id UUID PRIMARY KEY,
model_id UUID NOT NULL, -- FK: phone_models.id
option_type VARCHAR(100) NOT NULL, -- 예: 'COLOR', 'STORAGE'
option_value VARCHAR(255) NOT NULL, -- 예: 'Black', '128'
display_label VARCHAR(255), -- 표시명(예: '블랙', '128GB')
created_at TIMESTAMP NOT NULL, -- BaseEntity
updated_at TIMESTAMP NOT NULL,
created_by VARCHAR(255),
last_modified_by VARCHAR(255),
is_delete BOOLEAN NOT NULL DEFAULT FALSE,
CONSTRAINT chk_phone_options_type CHECK (option_type IN ('COLOR', 'STORAGE'))
);

ALTER TABLE phone_options ADD CONSTRAINT fk_phone_options_model FOREIGN KEY (model_id) REFERENCES phone_models(id);

-- 동일 모델 내 동일 타입-값 조합 유니크
CREATE UNIQUE INDEX uq_phone_options_model_type_value ON phone_options(model_id, option_type, option_value);
-- 모델/타입별 조회 인덱스
CREATE INDEX idx_phone_options_model_id ON phone_options(model_id);
CREATE INDEX idx_phone_options_model_type ON phone_options(model_id, option_type);
