# API 명세서 (구현 기준)

현재 백엔드에서 **실제로 구현되어 있는 API**를 기준으로 정리한 문서입니다.  
Base URL은 기본적으로 **`/api/v1`** 이며, 일부 설정 API는 예외적으로 `/api/*` 경로를 사용합니다(아래 참고).

---

## 🔐 인증 / 세션

### 토큰 전달 방식
- 본 프로젝트는 **쿠키 기반 JWT**(Access/Refresh)를 사용합니다.
- REST API 호출 시 인증은 보통 쿠키를 통해 처리되며, WebSocket도 쿠키 기반 인증을 사용합니다.

### 회원가입/로그인 (일반 계정)
```http
POST /api/v1/users/signup
POST /api/v1/users/login
POST /api/v1/users/logout
PUT  /api/v1/users/password
DELETE /api/v1/users/profile
```

### 소셜 로그인 (OAuth)
```http
GET  /api/v1/auth/kakao/login
GET  /api/v1/auth/kakao/callback?code=...
POST /api/v1/auth/kakao/token?code=...

GET  /api/v1/auth/naver/login
GET  /api/v1/auth/naver/callback?code=...
POST /api/v1/auth/naver/token?code=...
```

### Access Token 갱신 (Refresh Token Rotation)
```http
POST /api/v1/auth/refresh
```
- Refresh Token을 검증하고 **Refresh Token 로테이션**(기존 토큰 무효화 + 신규 발급)을 수행합니다.
- 응답에서 새로운 Access Token을 내려주며, Access/Refresh 쿠키가 함께 갱신됩니다.

---

## 📱 경매(견적) / 입찰 (Auction)

### 견적(Quotes)
```http
POST /api/v1/auction/quotes
GET  /api/v1/auction/quotes
GET  /api/v1/auction/quotes/{quoteId}
GET  /api/v1/auction/quotes/my
GET  /api/v1/auction/quotes/my/completed
PUT  /api/v1/auction/quotes/{quoteId}/close
GET  /api/v1/auction/quotes/{quoteId}/bids
```
- `GET /api/v1/auction/quotes`: 관리자/판매자 접근(진행중 전체 견적)
- `GET /api/v1/auction/quotes/my`: 소비자 본인 진행중 견적
- `GET /api/v1/auction/quotes/{quoteId}/bids`: 역할/소유권에 따라 조회 범위가 달라집니다.

### 입찰(Bids)
```http
POST /api/v1/auction/bids
PUT  /api/v1/auction/bids/{bidId}
GET  /api/v1/auction/bids/{bidId}
GET  /api/v1/auction/bids/my
GET  /api/v1/auction/bids/check/{quoteId}
```
- 입찰 생성/수정은 판매자 전용입니다.
- `check/{quoteId}`는 특정 견적에 이미 입찰했는지 여부를 반환합니다.

### 요금제(Price Plans)
```http
GET /api/v1/auction/price-plans
GET /api/v1/auction/price-plans/{id}
```
- `carrier`, `category` 필터를 지원합니다.

#### 관리자 요금제 관리
```http
GET  /api/v1/admin/auction/price-plans
POST /api/v1/admin/auction/price-plans
PUT  /api/v1/admin/auction/price-plans/{id}/activate
PUT  /api/v1/admin/auction/price-plans/{id}/deactivate
PUT  /api/v1/admin/auction/price-plans/{id}/display-order?displayOrder=0
```

---

## 🏪 판매자 (Seller)
```http
POST /api/v1/sellers/register
GET  /api/v1/sellers/profile
PUT  /api/v1/sellers/profile
```

### 판매자 서류 업로드/조회
```http
POST /api/v1/sellers/documents/temp                (multipart/form-data, 인증 불필요)
POST /api/v1/sellers/documents                     (multipart/form-data)
GET  /api/v1/sellers/documents
GET  /api/v1/sellers/documents/{documentType}
DELETE /api/v1/sellers/documents/file/{fileId}
```

---

## 🔔 알림 (Notifications)

### 알림 REST API
```http
GET    /api/v1/notifications
GET    /api/v1/notifications/unread-count
PUT    /api/v1/notifications/{notificationId}/read
PUT    /api/v1/notifications/read-all
DELETE /api/v1/notifications/{notificationId}
DELETE /api/v1/notifications/all
```

### SSE 실시간 스트림
```http
GET /api/v1/notifications/stream   (produces: text/event-stream)
```
- 연결 직후 최근 미읽음 알림을 초기로 내려줍니다(그룹화 적용).

### 알림 수신 설정 (경로 예외: /api/*)
> 아래 컨트롤러는 `/api/v1`이 아닌 `/api/notifications/settings` 경로를 사용합니다.

```http
GET  /api/notifications/settings
POST /api/notifications/settings
PUT  /api/notifications/settings/batch
```

---

## 💬 채팅 (Chat)

### 채팅방 / 메시지 REST API
```http
POST   /api/v1/chat/rooms
GET    /api/v1/chat/rooms
GET    /api/v1/chat/rooms/{chatRoomId}
GET    /api/v1/chat/rooms/{chatRoomId}/messages                     (deprecated)
GET    /api/v1/chat/rooms/{chatRoomId}/messages/paginated
POST   /api/v1/chat/rooms/{chatRoomId}/messages/read
DELETE /api/v1/chat/rooms/{chatRoomId}
```

### 채팅 이미지 업로드
```http
POST /api/v1/chat/rooms/{chatRoomId}/images/upload   (multipart/form-data)
```

### WebSocket(STOMP)
- STOMP 엔드포인트: `/ws/chat` (SockJS 사용)
- Application prefix: `/app`
- Topic prefix: `/topic`
- 주요 메시지:
  - `/app/chat/{chatRoomId}/send`
  - `/app/chat/{chatRoomId}/typing`

---

## 📱 휴대폰 카탈로그 (Phone)

### 모델
```http
GET    /api/v1/phone/models
GET    /api/v1/phone/models/{id}
POST   /api/v1/phone/models            (ADMIN)
PUT    /api/v1/phone/models/{id}       (ADMIN)
DELETE /api/v1/phone/models/{id}       (ADMIN)
```

### 옵션
```http
POST   /api/v1/phone/options
DELETE /api/v1/phone/options
```

### 모델 이미지
```http
GET    /api/v1/phone/models/{phoneModelId}/images
POST   /api/v1/phone/models/{phoneModelId}/images        (ADMIN, multipart)
DELETE /api/v1/phone/models/{phoneModelId}/images/{imageId} (ADMIN)
```

---

## 👤 마이페이지 (MyPage)
```http
GET    /api/v1/mypage/profile
PUT    /api/v1/mypage/profile

GET    /api/v1/mypage/purchases
GET    /api/v1/mypage/purchases/{contractId}

POST   /api/v1/mypage/accounts
GET    /api/v1/mypage/accounts
DELETE /api/v1/mypage/accounts/{accountId}

GET    /api/v1/mypage/delivery-addresses/default
GET    /api/v1/mypage/delivery-addresses
POST   /api/v1/mypage/delivery-addresses
PUT    /api/v1/mypage/delivery-addresses/{addressId}/set-default
DELETE /api/v1/mypage/delivery-addresses/{addressId}

POST   /api/v1/mypage/profile/image       (multipart/form-data)
DELETE /api/v1/mypage/profile/image
```

---

## 🛟 고객센터 (MyPage Customer Service)

### 공지사항
```http
GET /api/v1/mypage/customerservice/notices
GET /api/v1/mypage/customerservice/notices/{noticeId}
```

### FAQ
```http
GET /api/v1/mypage/customerservice/faqs
GET /api/v1/mypage/customerservice/faqs/{faqId}
```

### 1:1 문의
```http
POST   /api/v1/mypage/customerservice/inquiries
GET    /api/v1/mypage/customerservice/inquiries/my
GET    /api/v1/mypage/customerservice/inquiries/{inquiryId}
PUT    /api/v1/mypage/customerservice/inquiries/{inquiryId}
DELETE /api/v1/mypage/customerservice/inquiries/{inquiryId}
```

### 고객센터 관리자 API
```http
POST   /api/v1/admin/customerservice/notices
PUT    /api/v1/admin/customerservice/notices/{noticeId}
DELETE /api/v1/admin/customerservice/notices/{noticeId}

POST   /api/v1/admin/customerservice/faqs
PUT    /api/v1/admin/customerservice/faqs/{faqId}
DELETE /api/v1/admin/customerservice/faqs/{faqId}

GET    /api/v1/admin/customerservice/inquiries
POST   /api/v1/admin/customerservice/inquiries/{inquiryId}/reply
PUT    /api/v1/admin/customerservice/inquiries/{inquiryId}/reply/{replyId}
DELETE /api/v1/admin/customerservice/inquiries/{inquiryId}/reply/{replyId}
```

---

## 💳 결제 (PortOne V2)
```http
POST /api/v1/payments/portone/init
POST /api/v1/payments/portone/confirm
POST /api/v1/payments/portone/webhook
```

---

## ☁️ S3 업로드 (테스트용)
```http
POST   /api/v1/s3-upload        (multipart/form-data)
DELETE /api/v1/s3-upload?fileName=...
```

---

## 🧩 미구현 / 추후 추가(TBD)
- 계약 관리 API
- 배송 관리 API
- 통계 API
