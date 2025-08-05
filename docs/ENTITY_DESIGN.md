# 엔터티 설계 문서

## 📋 개요

휴대폰 역경매 플랫폼의 JPA 엔터티 설계 문서입니다. 모든 엔터티는 Spring Boot 3 + JPA (Hibernate) + PostgreSQL 환경에서 동작하도록 설계되었습니다.

## 🏗️ 공통 설계 원칙

### BaseEntity

모든 엔터티는 `BaseEntity`를 상속받아 공통 필드를 포함합니다:

- `createdAt`: 생성 시각 (`@CreatedDate`)
- `updatedAt`: 수정 시각 (`@LastModifiedDate`)
- `createdBy`: 생성자 (`@CreatedBy`)
- `lastModifiedBy`: 최종 수정자 (`@LastModifiedBy`)
- `isDelete`: 소프트 삭제 플래그

### 식별자 전략

- **UUID**: 모든 엔터티의 기본키로 사용
- **분산 환경 친화적**: 서버 간 충돌 없는 ID 생성
- **보안성**: 순차적이지 않아 추측 불가능

### 인덱싱 전략

- **복합 인덱스**: 자주 함께 조회되는 컬럼들
- **단일 인덱스**: 개별 검색이 빈번한 컬럼들
- **성능 최적화**: 쿼리 패턴 분석 기반 인덱스 설계

## 📊 엔터티 상세 설계

### 1. User (사용자)

```java
@Entity
@Table(name = "users")
public class User extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "username", nullable = false, unique = true)
    @Pattern(regexp = "^[a-z0-9@._-]+$")
    @Size(min = 4, max = 255)
    private String username;

    @Column(name = "password", nullable = false)
    private String password;

    @Email
    @Column(name = "email", nullable = false, unique = true)
    private String email;

    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "nickname", nullable = false)
    @Pattern(regexp = "^[가-힣a-zA-Z0-9_-]+$")
    @Size(min = 2, max = 10)
    private String nickname;

    @Column(name = "phone", nullable = true)
    @Pattern(regexp = "^[0-9]+$")
    private String phone;

    @Enumerated(EnumType.STRING)
    @Column(name = "role", nullable = false)
    private Role role; // CONSUMER, SELLER, ADMIN

    @Enumerated(EnumType.STRING)
    @Column(name = "provider", nullable = true)
    private Provider provider; // KAKAO, NAVER

    @Column(name = "provider_id", nullable = true)
    private String providerId;
}
```

**비즈니스 로직:**

- 소셜 로그인 기반 사용자 관리
- 역할별 권한 제어 (Consumer/Seller/Admin)
- 이메일 중복 검증
- 전화번호 형식 검증 (숫자만 허용)

**인덱스:**

- `idx_users_email`: 이메일 기반 로그인
- `idx_users_provider`: 소셜 로그인 조회

### 2. Seller (판매자)

```java
@Entity
@Table(name = "sellers")
public class Seller extends BaseEntity {
    @Id
    @Column(name = "user_id")
    private UUID userId;

    @OneToOne(fetch = FetchType.LAZY)
    @MapsId
    @JoinColumn(name = "user_id")
    private User user;

    @Column(name = "business_number", nullable = false)
    private String businessNumber;

    @Column(name = "store_name", nullable = false)
    private String storeName;

    @Enumerated(EnumType.STRING)
    @Column(name = "approval_status", nullable = false)
    private ApprovalStatus approvalStatus; // PENDING, APPROVED, REJECTED

    @Embedded
    private Address storeAddress; // 판매점 주소 정보
}
```

**비즈니스 로직:**

- 사업자등록번호 기반 판매자 검증
- 승인 프로세스 관리 (대기 → 승인/거부)
- 판매점 주소 정보 관리
- `canSell()`: 승인된 판매자만 입찰 가능
- `approve()`, `reject()`: 승인 상태 변경

### 3. Address (주소 - 임베디드 타입)

```java
@Embeddable
public class Address {
    @Column(name = "postal_code")
    private String postalCode;

    @Column(name = "address")
    private String address;

    @Column(name = "detail_address")
    private String detailAddress;
}
```

**비즈니스 로직:**

- `getFullAddress()`: 전체 주소 문자열 생성
- `isComplete()`: 주소 완성도 검증
- `isEmpty()`: 빈 주소 여부 확인
- `hasDetailAddress()`: 상세주소 존재 여부

### 4. SellerDocument (판매자 제출 문서)

```java
@Entity
@Table(name = "seller_documents")
public class SellerDocument extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "seller_id", nullable = false)
    private Seller seller;

    @Enumerated(EnumType.STRING)
    @Column(name = "type", nullable = false)
    private DocumentType type; // BUSINESS_LICENSE, CONSENT_FORM

    @Column(name = "file_url", nullable = false)
    private String fileUrl;

    @Column(name = "uploaded_at", nullable = false)
    private LocalDateTime uploadedAt;
}
```

**비즈니스 로직:**

- 사업자등록증, 사전승낙서 등 문서 관리
- S3 파일 URL 관리
- `getFileName()`: URL에서 파일명 추출
- `isS3Url()`: S3 저장 여부 확인

### 5. Quote (견적 요청)

```java
@Entity
@Table(name = "quotes")
public class Quote extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    // 기본 제품 정보
    @Column(name = "model", nullable = false)
    private String model;

    @Column(name = "storage", nullable = false)
    private String storage;

    @Enumerated(EnumType.STRING)
    @Column(name = "carrier", nullable = false)
    private Carrier carrier; // 희망 통신사

    @Column(name = "color", nullable = false)
    private String color;

    // 경매 관리
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private QuoteStatus status;

    @Column(name = "expired_at", nullable = false)
    private LocalDateTime expiredAt;

    // 구매 조건 필드들
    @Enumerated(EnumType.STRING)
    @Column(name = "purchase_method")
    private PurchaseMethod purchaseMethod; // 구매방법

    @Enumerated(EnumType.STRING)
    @Column(name = "current_carrier")
    private Carrier currentCarrier; // 기존 통신사

    @Enumerated(EnumType.STRING)
    @Column(name = "activation_method")
    private ActivationMethod activationMethod; // 개통방법
}
```

**비즈니스 로직:**

- 경매 시간 관리 (기본 24시간)
- 상태 전환 (OPEN → CLOSED → CONTRACTED)
- 입찰 가능 여부 검증 (`canReceiveBids()`)
- 마감 시간 연장 (`extendExpiration()`)

### 6. Bid (입찰)

```java
@Entity
@Table(name = "bids")
public class Bid extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "quote_id", nullable = false)
    private Quote quote;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "seller_id", nullable = false)
    private Seller seller;

    // 기본 입찰 정보
    @Column(name = "price", nullable = false)
    private Integer price;

    @Column(name = "delivery_days", nullable = false)
    private Integer deliveryDays;

    @Column(name = "rating_snapshot")
    private Double ratingSnapshot;

    // 구매 조건 (필수)
    @Enumerated(EnumType.STRING)
    @Column(name = "purchase_method", nullable = false)
    private PurchaseMethod purchaseMethod;

    @Enumerated(EnumType.STRING)
    @Column(name = "carrier", nullable = false)
    private Carrier carrier; // 이동할/사용할 통신사

    @Enumerated(EnumType.STRING)
    @Column(name = "current_carrier")
    private Carrier currentCarrier; // 기존 통신사

    @Enumerated(EnumType.STRING)
    @Column(name = "activation_method", nullable = false)
    private ActivationMethod activationMethod;

    // 추가 비용 정보
    @Column(name = "additional_subsidy")
    private Integer additionalSubsidy; // 추가지원금

    @Column(name = "installment_principal")
    private Integer installmentPrincipal; // 할부원금

    @Column(name = "additional_services", length = 500)
    private String additionalServices; // 부가서비스

    // 요금제 정보
    @Embedded
    private PricePlan pricePlan;

    @Column(name = "contract_months")
    private Integer contractMonths; // 약정개월
}
```

**비즈니스 로직:**

- `getTotalCost()`: 입찰가 + 추가지원금 + (요금제 × 약정개월)
- `getMonthlyAverageCost()`: 총 비용 ÷ 약정개월
- `getBidSummary()`: 확장된 입찰 요약 (요금제, 약정 정보 포함)
- **검증 로직**: 구매방법에 따른 필수 필드 검증
- `canModify()`: 입찰 수정 가능 여부

### 7. PricePlan (요금제 - 임베디드 타입)

```java
@Embeddable
public class PricePlan {
    @Column(name = "plan_name")
    private String planName;

    @Column(name = "plan_price")
    private Integer planPrice;
}
```

**비즈니스 로직:**

- `getPlanSummary()`: "요금제명 (가격원)" 형태 요약
- `isAffordable()`: 예산 내 이용 가능 여부
- `isUnlimited()`: 무제한 요금제 감지
- `isComplete()`: 완전한 요금제 정보 여부
- **검증**: 빈 문자열, 음수 가격, 길이 제한

### 8. BidHistory (입찰 수정 이력)

```java
@Entity
@Table(name = "bid_history")
public class BidHistory extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "bid_id", nullable = false)
    private Bid bid;

    @Column(name = "version", nullable = false)
    private Integer version;

    @Column(name = "price", nullable = false)
    private Integer price;

    @Column(name = "delivery_days", nullable = false)
    private Integer deliveryDays;
}
```

**비즈니스 로직:**

- 입찰 수정 이력 추적
- 버전 관리로 변경 사항 추적
- 감사 로그 목적

## 🔄 Enum 타입들

### Role (사용자 역할)

```java
public enum Role {
    CONSUMER("소비자"),
    SELLER("판매자"),
    ADMIN("관리자");
}
```

### Provider (소셜 로그인 제공자)

```java
public enum Provider {
    KAKAO("카카오"),
    NAVER("네이버");
}
```

### ApprovalStatus (승인 상태)

```java
public enum ApprovalStatus {
    PENDING("승인 대기"),
    APPROVED("승인됨"),
    REJECTED("거부됨");
}
```

### DocumentType (문서 종류)

```java
public enum DocumentType {
    BUSINESS_LICENSE("사업자등록증"),
    CONSENT_FORM("사전승낙서");
}
```

### QuoteStatus (견적 상태)

```java
public enum QuoteStatus {
    OPEN("진행중"),
    CLOSED("마감됨"),
    CONTRACTED("계약됨");
}
```

### PurchaseMethod (구매방법)

```java
public enum PurchaseMethod {
    NUMBER_TRANSFER("번호이동", "기존 번호를 다른 통신사로 이동"),
    DEVICE_CHANGE("기기변경", "동일 통신사에서 단말기만 변경"),
    NEW_SUBSCRIPTION("신규가입", "새로운 번호로 가입"),
    ANY("상관없음", "구매방법 무관"); // Quote만 사용
}
```

### ActivationMethod (개통방법)

```java
public enum ActivationMethod {
    SELECTIVE_SUBSIDY("선택약정", "약정 기간에 따른 할인 혜택"),
    COMMON_SUBSIDY("공시지원금", "통신사 공통 지원금 적용"),
    ANY("상관없음", "개통방법 무관"); // Quote만 사용
}
```

### Carrier (통신사)

```java
public enum Carrier {
    SKT("SKT"),
    KT("KT"),
    LGU("LG U+");
}
```

### BidStatus (입찰 상태)

```java
public enum BidStatus {
    ACTIVE("활성"),
    WITHDRAWN("철회됨"),
    SELECTED("선택됨");
}
```

## 📈 성능 최적화

### 인덱스 전략

```sql
-- 조회 성능 최적화
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
```

### 페치 전략

- **LAZY 로딩**: 모든 연관관계는 기본적으로 LAZY
- **N+1 문제 방지**: 필요시 JOIN FETCH 사용
- **페이징**: 대용량 데이터 조회 시 Pageable 활용

## 🧪 테스트 전략

### 단위 테스트

- **엔터티 생성**: Builder 패턴 검증
- **비즈니스 로직**: 각 메서드별 기능 검증
- **검증 로직**: 예외 상황 처리 확인
- **상태 전환**: 올바른 상태 변경 검증

### 통합 테스트

- **JPA 매핑**: 실제 DB와의 연동 검증
- **트랜잭션**: 롤백 및 커밋 동작 확인
- **제약조건**: 외래키, 유니크 제약 검증

## 📋 마이그레이션 가이드

### 기존 시스템에서 업그레이드 시

1. **새로운 컬럼 추가**: NULL 허용으로 시작
2. **데이터 마이그레이션**: 기본값 설정 스크립트 실행
3. **제약조건 추가**: 데이터 정합성 확인 후 NOT NULL 적용
4. **인덱스 생성**: 서비스 중단 최소화를 위한 온라인 인덱스 생성

### 배포 순서

1. **스키마 변경**: DDL 스크립트 실행
2. **애플리케이션 배포**: 새로운 엔터티 적용
3. **데이터 검증**: 마이그레이션 결과 확인
4. **모니터링**: 성능 지표 확인

## 🔍 모니터링

### 성능 지표

- **쿼리 실행 시간**: 평균 200ms 이하 목표
- **인덱스 적중률**: 95% 이상 유지
- **DB 커넥션 풀**: 사용률 80% 이하 유지

### 알림 설정

- **슬로우 쿼리**: 1초 이상 쿼리 알림
- **데드락**: 발생 시 즉시 알림
- **커넥션 풀 부족**: 90% 이상 시 알림

이 설계 문서는 휴대폰 역경매 플랫폼의 모든 비즈니스 요구사항을 충족하도록 설계되었으며, 확장성과 유지보수성을 고려하여 작성되었습니다.
