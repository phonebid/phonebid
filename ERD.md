// 사용자 테이블: 소비자, 판매자, 관리자 공통
Table users {
  id UUID [pk] // 사용자 고유 ID
  email varchar // 이메일 (소셜 로그인)
  name varchar // 사용자 이름 또는 닉네임
  role varchar // 사용자 역할: CONSUMER, SELLER, ADMIN
  provider varchar // 로그인 제공자: KAKAO, NAVER
  provider_id varchar // 소셜 로그인 ID
  created_at timestamp // 가입 시각
  // + BaseEntity: 생성/수정/삭제 메타 포함
}

// 판매자 정보 테이블 (users와 1:1)
Table sellers {
  user_id UUID [pk, ref: > users.id] // 사용자 ID (판매자)
  business_number varchar // 사업자등록번호
  store_name varchar // 상호명
  approval_status varchar // 가입 승인 상태: PENDING, APPROVED, REJECTED
  created_at timestamp
  // + BaseEntity
}

// 판매자 문서 업로드 메타 (사전승낙서 등)
Table seller_documents {
  id UUID [pk]
  seller_id UUID [ref: > sellers.user_id] // 연관 판매자
  type varchar // 문서 종류: BUSINESS_LICENSE, CONSENT_FORM
  file_url varchar // S3 저장 위치
  uploaded_at timestamp
  // + BaseEntity
}

// 소비자가 등록한 휴대폰 견적
Table quotes {
  id UUID [pk]
  user_id UUID [ref: > users.id] // 견적 요청자
  model varchar // 기종명 (예: iPhone 16)
  storage varchar // 저장 용량 (예: 128GB)
  carrier varchar // 통신사: SKT, KT, LGU+
  color varchar // 색상
  status varchar // 상태: OPEN, CLOSED, CONTRACTED
  expired_at timestamp // 경매 마감 시각
  created_at timestamp
  // + BaseEntity
}

// 판매자가 등록하는 입찰 (실시간 수정 가능)
Table bids {
  id UUID [pk]
  quote_id UUID [ref: > quotes.id] // 견적 대상
  seller_id UUID [ref: > sellers.user_id] // 입찰한 판매자
  price int // 입찰가
  delivery_days int // 배송 예상일
  rating_snapshot float // 당시 평점 스냅샷
  created_at timestamp
  updated_at timestamp
  // + BaseEntity
}

// 입찰 수정 히스토리
Table bid_history {
  id UUID [pk]
  bid_id UUID [ref: > bids.id] // 수정 대상 입찰
  version int // 버전 번호
  price int
  delivery_days int
  created_at timestamp
  // + BaseEntity
}

// 소비자가 입찰을 선택하면 생성되는 계약 정보
Table contracts {
  id UUID [pk]
  quote_id UUID [ref: > quotes.id] // 연관된 견적
  bid_id UUID [ref: > bids.id] // 선택된 입찰
  status varchar // 계약 상태: SIGNING, SIGNED, CANCELED
  signed_at timestamp
  // + BaseEntity
}

// 결제 정보 저장 테이블
Table payments {
  id UUID [pk]
  contract_id UUID [ref: > contracts.id] // 계약 참조
  amount int // 결제 금액
  method varchar // 결제 수단: CARD, BANK, MOBILE
  status varchar // 상태: REQUESTED, PENDING_APPROVAL, PAID, FAILED
  pg_tid varchar // PG사 거래 ID
  paid_at timestamp
  // + BaseEntity
}

// 배송 정보 테이블
Table deliveries {
  id UUID [pk]
  contract_id UUID [ref: > contracts.id]
  courier varchar // 택배사: CJ_LOGISTICS, HANJIN, LOTTE 등
  invoice_number varchar // 송장 번호
  status varchar // 배송 상태: READY, SHIPPED, DELIVERED
  shipped_at timestamp
  // + BaseEntity
}

// 사용자 알림 테이블
Table notifications {
  id UUID [pk]
  user_id UUID [ref: > users.id] // 수신 대상
  type varchar // 알림 유형: QUOTE_CREATED, BID_ARRIVED 등
  channel varchar // 채널: KAKAO, PUSH, EMAIL
  is_read boolean // 읽음 여부
  created_at timestamp
  // + BaseEntity
}