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
model VARCHAR(255) NOT NULL, -- 기종명 (예: iPhone 16)
storage VARCHAR(255) NOT NULL, -- 저장 용량 (예: 128GB)
carrier VARCHAR(255) NOT NULL, -- 통신사: SKT, KT, LGU
color VARCHAR(255) NOT NULL,
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
ALTER TABLE sellers ADD CONSTRAINT fk_sellers_user FOREIGN KEY (user_id) REFERENCES users(id);
ALTER TABLE seller_documents ADD CONSTRAINT fk_documents_seller FOREIGN KEY (seller_id) REFERENCES sellers(user_id);
ALTER TABLE quotes ADD CONSTRAINT fk_quotes_user FOREIGN KEY (user_id) REFERENCES users(id);
ALTER TABLE bids ADD CONSTRAINT fk_bids_quote FOREIGN KEY (quote_id) REFERENCES quotes(id);
ALTER TABLE bids ADD CONSTRAINT fk_bids_seller FOREIGN KEY (seller_id) REFERENCES sellers(user_id);
ALTER TABLE bid_history ADD CONSTRAINT fk_history_bid FOREIGN KEY (bid_id) REFERENCES bids(id);
