# API 명세서

## 📋 개요

휴대폰 역경매 플랫폼의 RESTful API 명세서입니다. 모든 API는 JSON 형태로 데이터를 주고받으며, JWT 기반 인증을 사용합니다.

## 🔐 인증

### JWT 토큰

- **Header**: `Authorization: Bearer {JWT_TOKEN}`
- **만료시간**: 24시간
- **갱신**: 토큰 만료 전 자동 갱신

### 소셜 로그인

```http
POST /api/auth/login/{provider}
```

**Request:**

```json
{
  "code": "authorization_code",
  "redirectUri": "https://phonebid.com/callback"
}
```

**Response:**

```json
{
  "accessToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "refreshToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "user": {
    "id": "uuid",
    "email": "user@example.com",
    "name": "사용자",
    "role": "CONSUMER"
  }
}
```

## 📱 견적 관리 API

### 견적 등록

```http
POST /api/quotes
```

**Request:**

```json
{
  "model": "iPhone 16",
  "storage": "128GB",
  "carrier": "SKT",
  "color": "블랙",
  "expiredAt": "2024-12-31T23:59:59",
  "purchaseMethod": "NUMBER_TRANSFER",
  "currentCarrier": "KT",
  "activationMethod": "SELECTIVE_SUBSIDY"
}
```

**Response:**

```json
{
  "id": "quote-uuid",
  "user": {
    "id": "user-uuid",
    "name": "소비자"
  },
  "model": "iPhone 16",
  "storage": "128GB",
  "carrier": "SKT",
  "color": "블랙",
  "status": "OPEN",
  "expiredAt": "2024-12-31T23:59:59",
  "purchaseMethod": "NUMBER_TRANSFER",
  "currentCarrier": "KT",
  "activationMethod": "SELECTIVE_SUBSIDY",
  "createdAt": "2024-12-30T10:00:00"
}
```

### 견적 목록 조회

```http
GET /api/quotes?status=OPEN&page=0&size=20
```

**Response:**

```json
{
  "content": [
    {
      "id": "quote-uuid",
      "model": "iPhone 16",
      "storage": "128GB",
      "carrier": "SKT",
      "color": "블랙",
      "status": "OPEN",
      "purchaseMethod": "NUMBER_TRANSFER",
      "currentCarrier": "KT",
      "activationMethod": "SELECTIVE_SUBSIDY",
      "bidCount": 5,
      "lowestPrice": 1200000,
      "expiredAt": "2024-12-31T23:59:59",
      "createdAt": "2024-12-30T10:00:00"
    }
  ],
  "pageable": {
    "page": 0,
    "size": 20,
    "totalElements": 100,
    "totalPages": 5
  }
}
```

## 💰 입찰 관리 API

### 입찰 등록

```http
POST /api/bids
```

**Request:**

```json
{
  "quoteId": "quote-uuid",
  "price": 1200000,
  "deliveryDays": 3,
  "purchaseMethod": "NUMBER_TRANSFER",
  "carrier": "SKT",
  "currentCarrier": "KT",
  "activationMethod": "SELECTIVE_SUBSIDY",
  "additionalSubsidy": 50000,
  "installmentPrincipal": 1000000,
  "additionalServices": "보험, 액세서리",
  "pricePlan": {
    "planName": "5G 프리미엄",
    "planPrice": 89000
  },
  "contractMonths": 24
}
```

**Response:**

```json
{
  "id": "bid-uuid",
  "quote": {
    "id": "quote-uuid",
    "model": "iPhone 16"
  },
  "seller": {
    "id": "seller-uuid",
    "storeName": "휴대폰 할인마트",
    "storeAddress": {
      "postalCode": "12345",
      "address": "서울시 강남구 테헤란로 123",
      "detailAddress": "ABC빌딩 2층",
      "fullAddress": "(12345) 서울시 강남구 테헤란로 123 ABC빌딩 2층"
    }
  },
  "price": 1200000,
  "deliveryDays": 3,
  "purchaseMethod": "NUMBER_TRANSFER",
  "carrier": "SKT",
  "currentCarrier": "KT",
  "activationMethod": "SELECTIVE_SUBSIDY",
  "additionalSubsidy": 50000,
  "installmentPrincipal": 1000000,
  "additionalServices": "보험, 액세서리",
  "pricePlan": {
    "planName": "5G 프리미엄",
    "planPrice": 89000,
    "planSummary": "5G 프리미엄 (89,000원)"
  },
  "contractMonths": 24,
  "totalCost": 3338000,
  "monthlyAverageCost": 139083,
  "bidSummary": "입찰가: 1,200,000원, 배송예정: 3일, 요금제: 5G 프리미엄 (89,000원), 약정: 24개월",
  "createdAt": "2024-12-30T11:00:00"
}
```

### 견적별 입찰 목록 조회

```http
GET /api/quotes/{quoteId}/bids?sort=price,asc
```

**Response:**

```json
{
  "content": [
    {
      "id": "bid-uuid",
      "seller": {
        "id": "seller-uuid",
        "storeName": "휴대폰 할인마트",
        "storeAddress": {
          "fullAddress": "(12345) 서울시 강남구 테헤란로 123 ABC빌딩 2층"
        },
        "rating": 4.5
      },
      "price": 1200000,
      "deliveryDays": 3,
      "purchaseMethod": "NUMBER_TRANSFER",
      "carrier": "SKT",
      "currentCarrier": "KT",
      "activationMethod": "SELECTIVE_SUBSIDY",
      "pricePlan": {
        "planSummary": "5G 프리미엄 (89,000원)"
      },
      "contractMonths": 24,
      "totalCost": 3338000,
      "monthlyAverageCost": 139083,
      "bidSummary": "입찰가: 1,200,000원, 배송예정: 3일, 요금제: 5G 프리미엄 (89,000원), 약정: 24개월",
      "createdAt": "2024-12-30T11:00:00"
    }
  ]
}
```

### 입찰 수정

```http
PUT /api/bids/{bidId}
```

**Request:**

```json
{
  "price": 1150000,
  "deliveryDays": 2,
  "additionalSubsidy": 70000,
  "pricePlan": {
    "planName": "5G 스탠다드",
    "planPrice": 75000
  }
}
```

## 🏪 판매자 관리 API

### 판매자 정보 조회

```http
GET /api/sellers/{sellerId}
```

**Response:**

```json
{
  "id": "seller-uuid",
  "user": {
    "id": "user-uuid",
    "name": "판매자",
    "email": "seller@example.com"
  },
  "businessNumber": "123-45-67890",
  "storeName": "휴대폰 할인마트",
  "approvalStatus": "APPROVED",
  "storeAddress": {
    "postalCode": "12345",
    "address": "서울시 강남구 테헤란로 123",
    "detailAddress": "ABC빌딩 2층",
    "fullAddress": "(12345) 서울시 강남구 테헤란로 123 ABC빌딩 2층"
  },
  "rating": 4.5,
  "totalSales": 1500000000,
  "createdAt": "2024-01-01T00:00:00"
}
```

### 판매자 정보 수정

```http
PUT /api/sellers/{sellerId}
```

**Request:**

```json
{
  "storeName": "새로운 상호명",
  "storeAddress": {
    "postalCode": "54321",
    "address": "부산시 해운대구 해운대로 456",
    "detailAddress": "XYZ타워 10층"
  }
}
```

### 판매자 대시보드

```http
GET /api/sellers/{sellerId}/dashboard
```

**Response:**

```json
{
  "totalBids": 150,
  "activeBids": 25,
  "totalSales": 1500000000,
  "monthlyStats": [
    {
      "month": "2024-12",
      "bidCount": 45,
      "salesAmount": 200000000,
      "avgBidPrice": 1200000
    }
  ],
  "recentBids": [
    {
      "id": "bid-uuid",
      "quote": {
        "model": "iPhone 16",
        "color": "블랙"
      },
      "price": 1200000,
      "status": "ACTIVE",
      "createdAt": "2024-12-30T11:00:00"
    }
  ]
}
```

## 📋 계약 관리 API

### 계약 생성

```http
POST /api/contracts
```

**Request:**

```json
{
  "quoteId": "quote-uuid",
  "bidId": "bid-uuid"
}
```

**Response:**

```json
{
  "id": "contract-uuid",
  "quote": {
    "id": "quote-uuid",
    "model": "iPhone 16",
    "storage": "128GB"
  },
  "selectedBid": {
    "id": "bid-uuid",
    "price": 1200000,
    "seller": {
      "storeName": "휴대폰 할인마트"
    }
  },
  "status": "SIGNING",
  "contractAmount": 1200000,
  "createdAt": "2024-12-30T12:00:00"
}
```

### 계약 서명

```http
POST /api/contracts/{contractId}/sign
```

**Response:**

```json
{
  "id": "contract-uuid",
  "status": "SIGNED",
  "signedAt": "2024-12-30T12:30:00",
  "payment": {
    "id": "payment-uuid",
    "amount": 1200000,
    "status": "REQUESTED"
  }
}
```

## 💳 결제 관리 API

### 결제 처리

```http
POST /api/payments/{paymentId}/process
```

**Request:**

```json
{
  "method": "CARD",
  "cardInfo": {
    "cardNumber": "1234-5678-9012-3456",
    "expiryDate": "12/25",
    "cvc": "123"
  }
}
```

**Response:**

```json
{
  "id": "payment-uuid",
  "amount": 1200000,
  "method": "CARD",
  "status": "PAID",
  "pgTid": "pg-transaction-id",
  "paidAt": "2024-12-30T13:00:00"
}
```

## 🚚 배송 관리 API

### 배송 정보 등록

```http
POST /api/deliveries
```

**Request:**

```json
{
  "contractId": "contract-uuid",
  "courier": "CJ_LOGISTICS",
  "invoiceNumber": "123456789012",
  "deliveryMemo": "부재 시 경비실에 맡겨주세요"
}
```

**Response:**

```json
{
  "id": "delivery-uuid",
  "contract": {
    "id": "contract-uuid"
  },
  "courier": "CJ_LOGISTICS",
  "invoiceNumber": "123456789012",
  "status": "READY",
  "deliveryMemo": "부재 시 경비실에 맡겨주세요",
  "trackingUrl": "https://www.cjlogistics.com/ko/tool/parcel/tracking?gnbInvcNo=123456789012",
  "createdAt": "2024-12-30T14:00:00"
}
```

### 배송 상태 업데이트

```http
PUT /api/deliveries/{deliveryId}/status
```

**Request:**

```json
{
  "status": "SHIPPED"
}
```

**Response:**

```json
{
  "id": "delivery-uuid",
  "status": "SHIPPED",
  "shippedAt": "2024-12-30T15:00:00",
  "estimatedDeliveryDate": "2024-12-31T18:00:00"
}
```

## 🔔 알림 관리 API

### 알림 목록 조회

```http
GET /api/notifications?isRead=false&page=0&size=20
```

**Response:**

```json
{
  "content": [
    {
      "id": "notification-uuid",
      "type": "BID_ARRIVED",
      "channel": "KAKAO",
      "title": "새로운 입찰이 도착했습니다",
      "content": "iPhone 16 견적에 1,200,000원 입찰이 도착했습니다.",
      "relatedEntityType": "BID",
      "relatedEntityId": "bid-uuid",
      "isRead": false,
      "sentAt": "2024-12-30T11:05:00",
      "createdAt": "2024-12-30T11:05:00"
    }
  ],
  "unreadCount": 5
}
```

### 알림 읽음 처리

```http
PUT /api/notifications/{notificationId}/read
```

**Response:**

```json
{
  "id": "notification-uuid",
  "isRead": true,
  "readAt": "2024-12-30T16:00:00"
}
```

## 📊 통계 API

### 시장 동향

```http
GET /api/statistics/market-trends?model=iPhone%2016&period=30
```

**Response:**

```json
{
  "model": "iPhone 16",
  "period": 30,
  "avgPrice": 1250000,
  "minPrice": 1100000,
  "maxPrice": 1400000,
  "totalQuotes": 150,
  "totalBids": 680,
  "avgBidsPerQuote": 4.5,
  "priceHistory": [
    {
      "date": "2024-12-01",
      "avgPrice": 1300000,
      "quoteCount": 12
    }
  ]
}
```

## ❌ 에러 응답

### 표준 에러 형식

```json
{
  "code": "VALIDATION_ERROR",
  "message": "입력값이 올바르지 않습니다.",
  "details": [
    {
      "field": "price",
      "message": "가격은 0보다 커야 합니다."
    }
  ],
  "timestamp": "2024-12-30T16:00:00"
}
```

### 에러 코드 목록

| 코드                      | HTTP 상태 | 설명               |
| ------------------------- | --------- | ------------------ |
| `VALIDATION_ERROR`        | 400       | 입력값 검증 실패   |
| `UNAUTHORIZED`            | 401       | 인증 실패          |
| `FORBIDDEN`               | 403       | 권한 부족          |
| `NOT_FOUND`               | 404       | 리소스 없음        |
| `BUSINESS_RULE_VIOLATION` | 409       | 비즈니스 규칙 위반 |
| `QUOTE_EXPIRED`           | 409       | 견적 마감          |
| `BID_NOT_MODIFIABLE`      | 409       | 입찰 수정 불가     |
| `CONTRACT_ALREADY_SIGNED` | 409       | 이미 서명된 계약   |
| `PAYMENT_FAILED`          | 422       | 결제 실패          |
| `INTERNAL_SERVER_ERROR`   | 500       | 서버 내부 오류     |

## 🔄 상태 전환

### 견적 상태 전환

```
OPEN → CLOSED (경매 마감)
CLOSED → CONTRACTED (계약 체결)
```

### 계약 상태 전환

```
SIGNING → SIGNED (서명 완료)
SIGNING → CANCELLED (취소)
```

### 결제 상태 전환

```
REQUESTED → PENDING_APPROVAL → PAID
REQUESTED → FAILED
PENDING_APPROVAL → FAILED
```

### 배송 상태 전환

```
READY → SHIPPED → DELIVERED
```

## 📋 필터링 및 정렬

### 견적 목록 필터링

- `status`: 견적 상태 (OPEN, CLOSED, CONTRACTED)
- `model`: 기종명
- `carrier`: 통신사
- `purchaseMethod`: 구매방법
- `activationMethod`: 개통방법

### 입찰 목록 정렬

- `price,asc`: 가격 오름차순
- `price,desc`: 가격 내림차순
- `totalCost,asc`: 총 비용 오름차순
- `deliveryDays,asc`: 배송일 오름차순
- `createdAt,desc`: 최신순

이 API 명세서는 휴대폰 역경매 플랫폼의 모든 기능을 포괄하며, 확장된 엔터티 구조를 완전히 반영합니다.
