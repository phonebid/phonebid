# ERD (구현 기준, DDL)

본 문서는 **현재 JPA 엔터티 구현(@Entity/@Table)**을 기준으로 작성된 DDL 기반 ERD입니다.  
대부분의 테이블은 공통 감사/소프트삭제 컬럼을 갖습니다.

## 관계 요약 (도메인 흐름)

### 핵심 거래 플로우
- **`Quote (견적)`**: 소비자가 원하는 단말/조건으로 경매(견적)를 생성합니다.
- **`Bid (입찰)`**: 판매자가 특정 견적에 입찰합니다. 입찰은 `PricePlan`(요금제) 및 조건(약정/추가지원금 등)과 함께 저장됩니다.
- **`Contract (계약)`**: 견적에서 선택된 입찰을 기준으로 **1:1 계약**을 생성합니다.
- **`Payment (결제)`**: 계약과 **1:1 관계**로 결제 상태를 관리합니다.
- **`Delivery (배송)`**: 계약과 **1:1 관계**로 배송(송장/상태)을 관리합니다.

요약 관계:
- `Quote 1 ─ N Bid`
- `Quote 1 ─ 1 Contract` (견적당 계약은 최대 1개)
- `Bid 1 ─ 1 Contract` (선택된 입찰 1개가 계약으로 연결)
- `Contract 1 ─ 1 Payment`
- `Contract 1 ─ 1 Delivery`

### 채팅
- **`ChatRoom`**은 거래 전 커뮤니케이션을 위한 단위이며, **`Quote`와 1:1로 연결**됩니다.
- **`ChatMessage`**는 `ChatRoom`에 종속된 메시지 엔티티입니다.
- **`UserChatRoom`**은 사용자별 “채팅방 참여/나가기(soft delete)” 상태를 남기기 위한 조인 엔티티로, 사용자-채팅방 N:M을 모델링합니다.

요약 관계:
- `Quote 1 ─ 1 ChatRoom`
- `ChatRoom 1 ─ N ChatMessage`
- `User N ─ M ChatRoom` (via `UserChatRoom`)

### 알림
- **`Notification`**: 사용자에게 전달되는 이벤트성 알림(읽음/발송 상태 포함)
- **`UserNotificationSetting`**: 사용자별/알림타입별/채널별 수신 동의를 관리 (유니크 제약 포함)

요약 관계:
- `User 1 ─ N Notification`
- `User 1 ─ N UserNotificationSetting`

### 판매자/서류
- **`Seller`**는 `User`와 1:1로 연결된 판매자 프로필입니다.
- **`SellerDocument`**는 판매자의 제출 서류(사업자등록증/승낙서 등)를 관리합니다.

요약 관계:
- `User 1 ─ 1 Seller`
- `Seller 1 ─ N SellerDocument`

### 카탈로그(단말/옵션/이미지)
- **`PhoneModel`**: 단말 모델 마스터
- **`PhoneOption`**: 모델별 옵션(색상/용량 등)
- **`PhoneModelImage`**: 모델별 이미지

요약 관계:
- `PhoneModel 1 ─ N PhoneOption`
- `PhoneModel 1 ─ N PhoneModelImage`

## 공통 컬럼

### `BaseEntity` 공통 컬럼
- `created_at`, `created_by`, `updated_at`, `updated_by`
- `deleted_at`, `deleted_by`, `is_delete`

### `BaseTimeEntity` 공통 컬럼 (RefreshToken 등 immutable 엔티티)
- `created_at`, `deleted_at`

---

## 1) 사용자/인증

CREATE TABLE users (
  id UUID PRIMARY KEY,
  username VARCHAR(255) NOT NULL UNIQUE,
  password VARCHAR(255) NOT NULL,
  email VARCHAR(255) NOT NULL UNIQUE,
  name VARCHAR(255) NOT NULL,
  nickname VARCHAR(10) NOT NULL,
  phone VARCHAR(20),
  role VARCHAR(255) NOT NULL,
  provider VARCHAR(255),
  provider_id VARCHAR(255),
  profile_image_url VARCHAR(2048),
  created_at TIMESTAMP,
  created_by VARCHAR(255),
  updated_at TIMESTAMP,
  updated_by VARCHAR(255),
  deleted_at TIMESTAMP,
  deleted_by VARCHAR(255),
  is_delete BOOLEAN NOT NULL DEFAULT FALSE
);

-- RefreshToken (BaseTimeEntity)
CREATE TABLE refresh_tokens (
  id UUID PRIMARY KEY,
  user_id UUID NOT NULL,
  token VARCHAR(500) NOT NULL UNIQUE,
  expires_at TIMESTAMP NOT NULL,
  created_at TIMESTAMP,
  deleted_at TIMESTAMP
);

ALTER TABLE refresh_tokens
  ADD CONSTRAINT fk_refresh_tokens_user
  FOREIGN KEY (user_id) REFERENCES users(id);

CREATE INDEX idx_refresh_tokens_token ON refresh_tokens(token);
CREATE INDEX idx_refresh_tokens_user_deleted ON refresh_tokens(user_id, deleted_at);
CREATE INDEX idx_refresh_tokens_expires_deleted ON refresh_tokens(expires_at, deleted_at);

---

## 2) 판매자

CREATE TABLE sellers (
  seller_id UUID PRIMARY KEY,
  user_id UUID NOT NULL,
  business_number VARCHAR(255) NOT NULL,
  store_name VARCHAR(255) NOT NULL,
  approval_status VARCHAR(255) NOT NULL,

  -- storeAddress (Address)
  postal_code VARCHAR(255),
  address VARCHAR(255),
  detail_address VARCHAR(255),

  is_agent BOOLEAN NOT NULL,
  representative_name VARCHAR(255) NOT NULL,

  -- businessAddress (Address) - AttributeOverrides
  business_postal_code VARCHAR(255),
  business_address VARCHAR(255),
  business_detail_address VARCHAR(255),

  consent_number VARCHAR(255),
  customer_service_phone VARCHAR(255),

  created_at TIMESTAMP,
  created_by VARCHAR(255),
  updated_at TIMESTAMP,
  updated_by VARCHAR(255),
  deleted_at TIMESTAMP,
  deleted_by VARCHAR(255),
  is_delete BOOLEAN NOT NULL DEFAULT FALSE
);

ALTER TABLE sellers
  ADD CONSTRAINT fk_sellers_user
  FOREIGN KEY (user_id) REFERENCES users(id);

CREATE INDEX idx_sellers_approval_status ON sellers(approval_status);

CREATE TABLE seller_documents (
  id UUID PRIMARY KEY,
  seller_id UUID NOT NULL,
  type VARCHAR(255) NOT NULL,
  file_url VARCHAR(2048) NOT NULL,
  created_at TIMESTAMP,
  created_by VARCHAR(255),
  updated_at TIMESTAMP,
  updated_by VARCHAR(255),
  deleted_at TIMESTAMP,
  deleted_by VARCHAR(255),
  is_delete BOOLEAN NOT NULL DEFAULT FALSE
);

ALTER TABLE seller_documents
  ADD CONSTRAINT fk_seller_documents_seller
  FOREIGN KEY (seller_id) REFERENCES sellers(seller_id);

CREATE INDEX idx_seller_documents_seller_id ON seller_documents(seller_id);
CREATE INDEX idx_seller_documents_type ON seller_documents(type);

---

## 3) 휴대폰 카탈로그

-- PhoneModel은 brand를 별도 테이블(FK)로 두지 않고 Enum으로 저장합니다.
CREATE TABLE phone_models (
  id UUID PRIMARY KEY,
  brand VARCHAR(255) NOT NULL,
  model VARCHAR(255) NOT NULL,
  model_number VARCHAR(255),
  released_price INTEGER,
  released_at DATE,
  created_at TIMESTAMP,
  created_by VARCHAR(255),
  updated_at TIMESTAMP,
  updated_by VARCHAR(255),
  deleted_at TIMESTAMP,
  deleted_by VARCHAR(255),
  is_delete BOOLEAN NOT NULL DEFAULT FALSE
);

CREATE UNIQUE INDEX uq_phone_models_brand_model ON phone_models(brand, model);
CREATE INDEX idx_phone_models_brand ON phone_models(brand);

CREATE TABLE phone_options (
  id UUID PRIMARY KEY,
  model_id UUID NOT NULL,
  option_type VARCHAR(100) NOT NULL,
  option_value VARCHAR(255) NOT NULL,
  display_label VARCHAR(255),
  created_at TIMESTAMP,
  created_by VARCHAR(255),
  updated_at TIMESTAMP,
  updated_by VARCHAR(255),
  deleted_at TIMESTAMP,
  deleted_by VARCHAR(255),
  is_delete BOOLEAN NOT NULL DEFAULT FALSE
);

ALTER TABLE phone_options
  ADD CONSTRAINT fk_phone_options_model
  FOREIGN KEY (model_id) REFERENCES phone_models(id);

CREATE UNIQUE INDEX uq_phone_options_model_type_value ON phone_options(model_id, option_type, option_value);
CREATE INDEX idx_phone_options_model_id ON phone_options(model_id);
CREATE INDEX idx_phone_options_model_type ON phone_options(model_id, option_type);

CREATE TABLE phone_model_images (
  id UUID PRIMARY KEY,
  model_id UUID NOT NULL,
  image_url TEXT NOT NULL,
  display_order INTEGER NOT NULL,
  created_at TIMESTAMP,
  created_by VARCHAR(255),
  updated_at TIMESTAMP,
  updated_by VARCHAR(255),
  deleted_at TIMESTAMP,
  deleted_by VARCHAR(255),
  is_delete BOOLEAN NOT NULL DEFAULT FALSE
);

ALTER TABLE phone_model_images
  ADD CONSTRAINT fk_phone_model_images_model
  FOREIGN KEY (model_id) REFERENCES phone_models(id);

CREATE INDEX idx_phone_model_images_model_id ON phone_model_images(model_id);

---

## 4) 경매(견적) / 입찰

CREATE TABLE quotes (
  id UUID PRIMARY KEY,
  user_id UUID NOT NULL,
  phone_model_id UUID NOT NULL,
  storage UUID,
  color UUID,
  carrier VARCHAR(255) NOT NULL,
  status VARCHAR(255) NOT NULL,
  expired_at TIMESTAMP NOT NULL,
  purchase_method VARCHAR(255),
  current_carrier VARCHAR(255),
  activation_method VARCHAR(255),
  created_at TIMESTAMP,
  created_by VARCHAR(255),
  updated_at TIMESTAMP,
  updated_by VARCHAR(255),
  deleted_at TIMESTAMP,
  deleted_by VARCHAR(255),
  is_delete BOOLEAN NOT NULL DEFAULT FALSE
);

ALTER TABLE quotes ADD CONSTRAINT fk_quotes_user FOREIGN KEY (user_id) REFERENCES users(id);
ALTER TABLE quotes ADD CONSTRAINT fk_quotes_phone_model FOREIGN KEY (phone_model_id) REFERENCES phone_models(id);
ALTER TABLE quotes ADD CONSTRAINT fk_quotes_storage FOREIGN KEY (storage) REFERENCES phone_options(id);
ALTER TABLE quotes ADD CONSTRAINT fk_quotes_color FOREIGN KEY (color) REFERENCES phone_options(id);

CREATE INDEX idx_quotes_user_id ON quotes(user_id);
CREATE INDEX idx_quotes_status ON quotes(status);
CREATE INDEX idx_quotes_expired_at ON quotes(expired_at);

CREATE TABLE price_plans (
  id UUID PRIMARY KEY,
  carrier VARCHAR(255) NOT NULL,
  category VARCHAR(255) NOT NULL DEFAULT 'FIVE_G',
  plan_name VARCHAR(255) NOT NULL,
  monthly_fee INTEGER NOT NULL,
  data_allowance_text VARCHAR(255),
  throttle_speed_text VARCHAR(255),
  voice_sms_text VARCHAR(255),
  is_active BOOLEAN NOT NULL,
  display_order INTEGER,
  created_at TIMESTAMP,
  created_by VARCHAR(255),
  updated_at TIMESTAMP,
  updated_by VARCHAR(255),
  deleted_at TIMESTAMP,
  deleted_by VARCHAR(255),
  is_delete BOOLEAN NOT NULL DEFAULT FALSE
);

CREATE INDEX idx_price_plans_carrier_active ON price_plans(carrier, is_active);
CREATE INDEX idx_price_plans_category_active ON price_plans(category, is_active);

CREATE TABLE bids (
  id UUID PRIMARY KEY,
  quote_id UUID NOT NULL,
  seller_id UUID NOT NULL,
  price_plan_id UUID NOT NULL,
  price INTEGER NOT NULL,
  delivery_days INTEGER NOT NULL,
  rating_snapshot DOUBLE PRECISION,
  purchase_method VARCHAR(255) NOT NULL,
  carrier VARCHAR(255) NOT NULL,
  current_carrier VARCHAR(255),
  activation_method VARCHAR(255) NOT NULL,
  additional_subsidy INTEGER,
  installment_principal INTEGER,
  contract_months INTEGER,
  status VARCHAR(255) NOT NULL,
  created_at TIMESTAMP,
  created_by VARCHAR(255),
  updated_at TIMESTAMP,
  updated_by VARCHAR(255),
  deleted_at TIMESTAMP,
  deleted_by VARCHAR(255),
  is_delete BOOLEAN NOT NULL DEFAULT FALSE
);

ALTER TABLE bids ADD CONSTRAINT fk_bids_quote FOREIGN KEY (quote_id) REFERENCES quotes(id);
ALTER TABLE bids ADD CONSTRAINT fk_bids_seller FOREIGN KEY (seller_id) REFERENCES sellers(seller_id);
ALTER TABLE bids ADD CONSTRAINT fk_bids_price_plan FOREIGN KEY (price_plan_id) REFERENCES price_plans(id);

CREATE INDEX idx_bids_quote_id ON bids(quote_id);
CREATE INDEX idx_bids_seller_id ON bids(seller_id);

CREATE TABLE bid_additional_services (
  id UUID PRIMARY KEY,
  bid_id UUID NOT NULL,
  service_name VARCHAR(100) NOT NULL,
  service_price INTEGER NOT NULL,
  description VARCHAR(500),
  mandatory BOOLEAN,
  cancellable_after_months INTEGER,
  created_at TIMESTAMP,
  created_by VARCHAR(255),
  updated_at TIMESTAMP,
  updated_by VARCHAR(255),
  deleted_at TIMESTAMP,
  deleted_by VARCHAR(255),
  is_delete BOOLEAN NOT NULL DEFAULT FALSE
);

ALTER TABLE bid_additional_services
  ADD CONSTRAINT fk_bid_additional_services_bid
  FOREIGN KEY (bid_id) REFERENCES bids(id);

CREATE INDEX idx_bid_additional_services_bid_id ON bid_additional_services(bid_id);

CREATE TABLE bid_history (
  id UUID PRIMARY KEY,
  bid_id UUID NOT NULL,
  version INTEGER NOT NULL,
  price INTEGER NOT NULL,
  delivery_days INTEGER NOT NULL,
  created_at TIMESTAMP,
  created_by VARCHAR(255),
  updated_at TIMESTAMP,
  updated_by VARCHAR(255),
  deleted_at TIMESTAMP,
  deleted_by VARCHAR(255),
  is_delete BOOLEAN NOT NULL DEFAULT FALSE
);

ALTER TABLE bid_history ADD CONSTRAINT fk_bid_history_bid FOREIGN KEY (bid_id) REFERENCES bids(id);
CREATE INDEX idx_bid_history_bid_id ON bid_history(bid_id);
CREATE INDEX idx_bid_history_version ON bid_history(bid_id, version);

---

## 5) 거래(계약/결제/배송)

CREATE TABLE contracts (
  id UUID PRIMARY KEY,
  quote_id UUID NOT NULL UNIQUE,
  bid_id UUID NOT NULL UNIQUE,
  status VARCHAR(255) NOT NULL,
  signed_at TIMESTAMP,
  created_at TIMESTAMP,
  created_by VARCHAR(255),
  updated_at TIMESTAMP,
  updated_by VARCHAR(255),
  deleted_at TIMESTAMP,
  deleted_by VARCHAR(255),
  is_delete BOOLEAN NOT NULL DEFAULT FALSE
);

ALTER TABLE contracts ADD CONSTRAINT fk_contracts_quote FOREIGN KEY (quote_id) REFERENCES quotes(id);
ALTER TABLE contracts ADD CONSTRAINT fk_contracts_bid FOREIGN KEY (bid_id) REFERENCES bids(id);

CREATE INDEX idx_contracts_quote_id ON contracts(quote_id);
CREATE INDEX idx_contracts_bid_id ON contracts(bid_id);
CREATE INDEX idx_contracts_status ON contracts(status);

CREATE TABLE payments (
  id UUID PRIMARY KEY,
  contract_id UUID NOT NULL UNIQUE,
  amount INTEGER NOT NULL,
  method VARCHAR(255) NOT NULL,
  status VARCHAR(255) NOT NULL,
  pg_tid VARCHAR(255) NOT NULL UNIQUE,
  paid_at TIMESTAMP,
  created_at TIMESTAMP,
  created_by VARCHAR(255),
  updated_at TIMESTAMP,
  updated_by VARCHAR(255),
  deleted_at TIMESTAMP,
  deleted_by VARCHAR(255),
  is_delete BOOLEAN NOT NULL DEFAULT FALSE
);

ALTER TABLE payments ADD CONSTRAINT fk_payments_contract FOREIGN KEY (contract_id) REFERENCES contracts(id);

CREATE INDEX idx_payments_contract_id ON payments(contract_id);
CREATE INDEX idx_payments_pg_tid ON payments(pg_tid);
CREATE INDEX idx_payments_status ON payments(status);

CREATE TABLE deliveries (
  id UUID PRIMARY KEY,
  contract_id UUID NOT NULL UNIQUE,
  courier VARCHAR(255) NOT NULL,
  invoice_number VARCHAR(255) NOT NULL,
  status VARCHAR(255) NOT NULL,
  shipped_at TIMESTAMP,
  delivered_at TIMESTAMP,
  delivery_memo VARCHAR(255),
  created_at TIMESTAMP,
  created_by VARCHAR(255),
  updated_at TIMESTAMP,
  updated_by VARCHAR(255),
  deleted_at TIMESTAMP,
  deleted_by VARCHAR(255),
  is_delete BOOLEAN NOT NULL DEFAULT FALSE
);

ALTER TABLE deliveries ADD CONSTRAINT fk_deliveries_contract FOREIGN KEY (contract_id) REFERENCES contracts(id);

CREATE INDEX idx_deliveries_contract_id ON deliveries(contract_id);
CREATE INDEX idx_deliveries_status ON deliveries(status);
CREATE INDEX idx_deliveries_invoice_number ON deliveries(invoice_number);

---

## 6) 알림

CREATE TABLE notifications (
  id UUID PRIMARY KEY,
  user_id UUID NOT NULL,
  type VARCHAR(255) NOT NULL,
  channel VARCHAR(255) NOT NULL,
  title VARCHAR(255) NOT NULL,
  message VARCHAR(1000) NOT NULL,
  is_read BOOLEAN NOT NULL,
  send_status VARCHAR(255) NOT NULL,
  sent_at TIMESTAMP,
  reference_id UUID,
  created_at TIMESTAMP,
  created_by VARCHAR(255),
  updated_at TIMESTAMP,
  updated_by VARCHAR(255),
  deleted_at TIMESTAMP,
  deleted_by VARCHAR(255),
  is_delete BOOLEAN NOT NULL DEFAULT FALSE
);

ALTER TABLE notifications ADD CONSTRAINT fk_notifications_user FOREIGN KEY (user_id) REFERENCES users(id);

CREATE INDEX idx_notifications_user_id ON notifications(user_id);
CREATE INDEX idx_notifications_user_created ON notifications(user_id, created_at);
CREATE INDEX idx_notifications_created_at ON notifications(created_at);
CREATE INDEX idx_notifications_type ON notifications(type);
CREATE INDEX idx_notifications_channel ON notifications(channel);
CREATE INDEX idx_notifications_is_read ON notifications(is_read);

CREATE TABLE user_notification_settings (
  id UUID PRIMARY KEY,
  user_id UUID NOT NULL,
  notification_type VARCHAR(50) NOT NULL,
  notification_channel VARCHAR(20) NOT NULL,
  is_agreed BOOLEAN NOT NULL,
  is_marketing BOOLEAN NOT NULL,
  agreed_at TIMESTAMP,
  agreed_at_marketing TIMESTAMP,
  created_at TIMESTAMP,
  created_by VARCHAR(255),
  updated_at TIMESTAMP,
  updated_by VARCHAR(255),
  deleted_at TIMESTAMP,
  deleted_by VARCHAR(255),
  is_delete BOOLEAN NOT NULL DEFAULT FALSE,
  CONSTRAINT uk_user_notification_setting UNIQUE (user_id, notification_type, notification_channel)
);

ALTER TABLE user_notification_settings
  ADD CONSTRAINT fk_user_notification_setting_user
  FOREIGN KEY (user_id) REFERENCES users(id);

---

## 7) 채팅

CREATE TABLE chat_rooms (
  id UUID PRIMARY KEY,
  quote_id UUID NOT NULL UNIQUE,
  consumer_id UUID NOT NULL,
  seller_id UUID NOT NULL,
  status VARCHAR(255) NOT NULL,
  created_at TIMESTAMP,
  created_by VARCHAR(255),
  updated_at TIMESTAMP,
  updated_by VARCHAR(255),
  deleted_at TIMESTAMP,
  deleted_by VARCHAR(255),
  is_delete BOOLEAN NOT NULL DEFAULT FALSE
);

ALTER TABLE chat_rooms ADD CONSTRAINT fk_chat_rooms_quote FOREIGN KEY (quote_id) REFERENCES quotes(id);
ALTER TABLE chat_rooms ADD CONSTRAINT fk_chat_rooms_consumer FOREIGN KEY (consumer_id) REFERENCES users(id);
ALTER TABLE chat_rooms ADD CONSTRAINT fk_chat_rooms_seller FOREIGN KEY (seller_id) REFERENCES sellers(seller_id);

CREATE TABLE user_chat_rooms (
  id UUID PRIMARY KEY,
  user_id UUID NOT NULL,
  chat_room_id UUID NOT NULL,
  joined_at TIMESTAMP NOT NULL,
  created_at TIMESTAMP,
  created_by VARCHAR(255),
  updated_at TIMESTAMP,
  updated_by VARCHAR(255),
  deleted_at TIMESTAMP,
  deleted_by VARCHAR(255),
  is_delete BOOLEAN NOT NULL DEFAULT FALSE
);

ALTER TABLE user_chat_rooms ADD CONSTRAINT fk_user_chat_rooms_user FOREIGN KEY (user_id) REFERENCES users(id);
ALTER TABLE user_chat_rooms ADD CONSTRAINT fk_user_chat_rooms_room FOREIGN KEY (chat_room_id) REFERENCES chat_rooms(id);

CREATE INDEX idx_user_chat_room_user ON user_chat_rooms(user_id);
CREATE INDEX idx_user_chat_room_chat_room ON user_chat_rooms(chat_room_id);
CREATE INDEX idx_user_chat_room_deleted_at ON user_chat_rooms(deleted_at);

CREATE TABLE chat_messages (
  id UUID PRIMARY KEY,
  chat_room_id UUID NOT NULL,
  sender_id UUID NOT NULL,
  message_type VARCHAR(255) NOT NULL,
  content TEXT NOT NULL,
  is_read BOOLEAN NOT NULL,
  created_at TIMESTAMP,
  created_by VARCHAR(255),
  updated_at TIMESTAMP,
  updated_by VARCHAR(255),
  deleted_at TIMESTAMP,
  deleted_by VARCHAR(255),
  is_delete BOOLEAN NOT NULL DEFAULT FALSE
);

ALTER TABLE chat_messages ADD CONSTRAINT fk_chat_messages_room FOREIGN KEY (chat_room_id) REFERENCES chat_rooms(id);
ALTER TABLE chat_messages ADD CONSTRAINT fk_chat_messages_sender FOREIGN KEY (sender_id) REFERENCES users(id);

CREATE INDEX idx_chat_messages_room_created ON chat_messages(chat_room_id, created_at);

---

## 8) 마이페이지 (계좌/배송지)

CREATE TABLE accounts (
  id UUID PRIMARY KEY,
  user_id UUID NOT NULL,
  bank VARCHAR(255) NOT NULL,
  account_number VARCHAR(255) NOT NULL,
  account_holder_name VARCHAR(255) NOT NULL,
  created_at TIMESTAMP,
  created_by VARCHAR(255),
  updated_at TIMESTAMP,
  updated_by VARCHAR(255),
  deleted_at TIMESTAMP,
  deleted_by VARCHAR(255),
  is_delete BOOLEAN NOT NULL DEFAULT FALSE
);

ALTER TABLE accounts ADD CONSTRAINT fk_accounts_user FOREIGN KEY (user_id) REFERENCES users(id);
CREATE INDEX idx_accounts_user_id ON accounts(user_id);
CREATE INDEX idx_accounts_bank ON accounts(bank);

CREATE TABLE user_delivery_addresses (
  id UUID PRIMARY KEY,
  user_id UUID NOT NULL,
  address_name VARCHAR(255) NOT NULL,
  recipient_name VARCHAR(255) NOT NULL,
  recipient_phone VARCHAR(255) NOT NULL,
  postal_code VARCHAR(255) NOT NULL,
  address VARCHAR(255) NOT NULL,
  detail_address VARCHAR(255),
  is_default BOOLEAN NOT NULL,
  created_at TIMESTAMP,
  created_by VARCHAR(255),
  updated_at TIMESTAMP,
  updated_by VARCHAR(255),
  deleted_at TIMESTAMP,
  deleted_by VARCHAR(255),
  is_delete BOOLEAN NOT NULL DEFAULT FALSE
);

ALTER TABLE user_delivery_addresses ADD CONSTRAINT fk_user_delivery_addresses_user FOREIGN KEY (user_id) REFERENCES users(id);
CREATE INDEX idx_user_delivery_addresses_user_id ON user_delivery_addresses(user_id);
CREATE INDEX idx_user_delivery_addresses_is_default ON user_delivery_addresses(is_default);

---

## 9) 고객센터

CREATE TABLE notices (
  id UUID PRIMARY KEY,
  admin_id UUID NOT NULL,
  title VARCHAR(200) NOT NULL,
  content TEXT NOT NULL,
  is_important BOOLEAN NOT NULL,
  view_count BIGINT NOT NULL,
  created_at TIMESTAMP,
  created_by VARCHAR(255),
  updated_at TIMESTAMP,
  updated_by VARCHAR(255),
  deleted_at TIMESTAMP,
  deleted_by VARCHAR(255),
  is_delete BOOLEAN NOT NULL DEFAULT FALSE
);

ALTER TABLE notices ADD CONSTRAINT fk_notices_admin FOREIGN KEY (admin_id) REFERENCES users(id);
CREATE INDEX idx_notices_is_important ON notices(is_important);
CREATE INDEX idx_notices_created_at ON notices(created_at);

CREATE TABLE faqs (
  id UUID PRIMARY KEY,
  category VARCHAR(255) NOT NULL,
  question VARCHAR(200) NOT NULL,
  answer TEXT NOT NULL,
  view_count BIGINT NOT NULL,
  created_at TIMESTAMP,
  created_by VARCHAR(255),
  updated_at TIMESTAMP,
  updated_by VARCHAR(255),
  deleted_at TIMESTAMP,
  deleted_by VARCHAR(255),
  is_delete BOOLEAN NOT NULL DEFAULT FALSE
);

CREATE INDEX idx_faqs_category ON faqs(category);

CREATE TABLE inquiries (
  id UUID PRIMARY KEY,
  user_id UUID NOT NULL,
  category VARCHAR(255) NOT NULL,
  title VARCHAR(200) NOT NULL,
  content TEXT NOT NULL,
  status VARCHAR(255) NOT NULL,
  created_at TIMESTAMP,
  created_by VARCHAR(255),
  updated_at TIMESTAMP,
  updated_by VARCHAR(255),
  deleted_at TIMESTAMP,
  deleted_by VARCHAR(255),
  is_delete BOOLEAN NOT NULL DEFAULT FALSE
);

ALTER TABLE inquiries ADD CONSTRAINT fk_inquiries_user FOREIGN KEY (user_id) REFERENCES users(id);
CREATE INDEX idx_inquiries_user_id ON inquiries(user_id);
CREATE INDEX idx_inquiries_status ON inquiries(status);
CREATE INDEX idx_inquiries_category ON inquiries(category);

CREATE TABLE inquiry_replies (
  id UUID PRIMARY KEY,
  inquiry_id UUID NOT NULL UNIQUE,
  admin_id UUID NOT NULL,
  content TEXT NOT NULL,
  created_at TIMESTAMP,
  created_by VARCHAR(255),
  updated_at TIMESTAMP,
  updated_by VARCHAR(255),
  deleted_at TIMESTAMP,
  deleted_by VARCHAR(255),
  is_delete BOOLEAN NOT NULL DEFAULT FALSE
);

ALTER TABLE inquiry_replies ADD CONSTRAINT fk_inquiry_replies_inquiry FOREIGN KEY (inquiry_id) REFERENCES inquiries(id);
ALTER TABLE inquiry_replies ADD CONSTRAINT fk_inquiry_replies_admin FOREIGN KEY (admin_id) REFERENCES users(id);
CREATE INDEX idx_inquiry_replies_inquiry_id ON inquiry_replies(inquiry_id);
