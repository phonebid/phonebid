# bidr (비더)

휴대폰 **역경매** 기반 견적/입찰 플랫폼 **bidr(비더)** 입니다.  
오프라인에 편중된 “성지” 가격 경쟁 문화를 온라인으로 끌어올려, 소비자에게는 **투명한 최저가 비교**를, 판매자에게는 **합법적 경쟁과 신규 유입 채널**을 제공합니다.

> **2026년 4월 런칭 목표로 개발 중**입니다.

---

## 1. 프로젝트 간단 소개
- **소비자**가 원하는 단말 조건으로 견적(경매)을 등록하면
- **판매자**가 역경매 방식으로 가격/조건을 제안(입찰)하고
- 소비자는 제안들을 비교해 선택 및 계약/결제/배송까지 이어지는 흐름을 제공합니다.

---

## 2. 프로젝트 목표
- **가격 정보의 투명화**: 견적/입찰 데이터를 구조화해 비교 가능하게 제공
- **거래 프로세스 표준화**: 견적 → 입찰 → 선택 → 계약 → 결제 → 배송의 일관된 플로우
- **실시간성 강화**: 경매 상태/알림 등 이벤트 기반 UX 제공
- **확장 가능한 구조**: 인증/결제/알림/대시보드 등 고도화를 고려한 설계

---

## 3. 기술 스택

### Backend
- Java 17, Spring Boot 3 (MVC + WebFlux)
- Spring Security, JWT
- Spring Data JPA (Hibernate), Validation
- PostgreSQL
- SSE / WebSocket
- Resilience4j, Bucket4j, Caffeine
- AWS S3 연동
- 환경변수 관리: `spring-dotenv`

### Frontend
- React 19, TypeScript
- Vite
- Tailwind CSS
- React Router
- Zustand
- Axios, SWR
- Playwright (E2E)

### DevOps / Deploy
- Docker / Docker Compose (로컬 DB)
- (배포 구성은 추후 문서화 예정)

---

## 4. 주요 기능
- **인증/보안 (JWT + RefreshToken)**
  - Access Token / Refresh Token 분리 설계
  - **Refresh Token Rotate(로테이션)**: 재발급 시 **기존 Refresh Token을 무효화**하고 새 토큰을 발급
  - **서버 저장 RefreshToken은 원문이 아닌 해시만 저장**하여 DB 유출 리스크를 줄임
  - RefreshToken **Soft Delete + 만료 토큰 정리 스케줄링**으로 운영 시 토큰 저장소 관리

- **동기/비동기 처리 분리로 성능/안정성 확보**
  - 사용자 요청 흐름(핵심 비즈니스)은 **동기 처리로 즉시 응답**
  - 알림 발송 등 부가 작업은 **별도 비동기 쓰레드 풀**(`notificationExecutor`)로 분리하여 지연/실패를 격리
  - 외부 연동(알림톡 등)은 **Retry / Circuit Breaker / TimeLimiter(Resilience4j)** 기반으로 장애 전파를 최소화

- **SSE 기반 실시간 이벤트 스트림 + 안정성 강화**
  - `text/event-stream` 기반 **실시간 알림 스트림** 제공
  - 연결 직후 **Handshake 이벤트(`connected`)** 를 전송해 인프라 계층 타임아웃을 예방
  - **Heartbeat(ping)** 주기 전송으로 유령 연결 감지/정리
  - Cleanup 스케줄러로 콜백 누락 등 예외 상황에서도 **stale connection 정리**
  - 동시성 안전한 연결 저장소(`ConcurrentHashMap` + 원자적 `compute`)로 연결 관리

- **WebSocket(STOMP) 기반 실시간 채팅**
  - **SockJS 폴백**을 포함한 WebSocket(STOMP) 채팅으로 브라우저/네트워크 환경 대응
  - **쿠키 기반 JWT 인증**을 WebSocket 핸드셰이크/CONNECT 단계에서 검증하여 인증된 세션만 연결 허용
  - 채팅방 단위 Topic 구독으로 메시지 브로드캐스팅
  - **타이핑 이벤트** 스트리밍으로 실시간 입력 상태 공유
  - 프론트에서 연결 상태 관리 + 재연결 시도(최대 횟수 제한) + 구독 지연 처리로 UX 안정화

- **레이트 리밋/리소스 보호**
  - Bucket4j 기반 **IP 단위 Rate Limiting**
  - Caffeine Cache로 IP별 Bucket을 인메모리 관리(만료 정책 적용)

- **도메인 기능(역경매 플로우)**
  - **소비자**: 견적(경매) 등록 → 진행/마감 → 입찰 비교 및 선택 → 계약/결제/배송
  - **판매자**: 입찰 등록/수정 및 **입찰 수정 이력(Bid History)** 관리, 대시보드 제공
  - **알림**: 이벤트 발생 시 SSE/외부 채널(알림톡)로 사용자에게 전달

---

## 5. API 명세서
- 문서 링크: [`docs/API_SPECIFICATION.md`](./docs/API_SPECIFICATION.md)

---

## 6. ERD
**DDL 기반 ERD 문서**로 관리
- 문서 링크: [`docs/ERD.md`](./docs/ERD.md)

---

## 7. 인프라 설계도
- 현재는 **추후 추가 예정(TBD)** 입니다.

---

## 8. 프로젝트 실행 방법 (로컬)

### 사전 요구사항
- Node.js **20+**
- pnpm
- Java 17

### 1) DB 실행 (Docker Compose)
루트의 `docker-compose.yml`은 PostgreSQL을 **로컬 5433 포트**로 엽니다.

```bash
docker compose up -d
```

기본 DB 정보:
- Host: `localhost`
- Port: `5433`
- DB: `phonebid`
- User: `phonebid`
- Password: `phonebid`

### 2) Backend 실행 (8080)
백엔드는 `backend/.env` 파일을 사용합니다.

`backend/.env` 예시(로컬 최소 구성):

```env
DATABASE_URL=jdbc:postgresql://localhost:5433/phonebid
DATABASE_USER=phonebid
DATABASE_PASSWORD=phonebid
JWT_SECRET_KEY=change-me-in-local
```

선택(기능 사용 시 필요):
- OAuth: `KAKAO_CLIENT_ID`, `NAVER_CLIENT_ID`, `NAVER_CLIENT_SECRET` 등
- S3: `AWS_ACCESS_KEY`, `AWS_SECRET_KEY`, `AWS_S3_BUCKET`
- 결제(PortOne V2): `PORTONE_STORE_ID`, `PORTONE_API_SECRET`, `PORTONE_CHANNEL_KEY`, `PORTONE_WEBHOOK_SECRET`
- 알림톡(알리고): `ALIGO_USER_ID`, `ALIGO_API_KEY`, `ALIGO_SENDER_KEY` 등  
  - 예시 파일: [`backend/.env.aligo.example`](./backend/.env.aligo.example)

실행:

```bash
cd backend
./gradlew bootRun
```

Windows PowerShell:

```powershell
cd backend
.\gradlew bootRun
```

- 기본 포트: `8080`

### 3) Frontend 실행
프론트는 `frontend/`에서 실행하며, `frontend/.env.local` 파일을 사용합니다.

`frontend/.env.local` 예시:

```env
VITE_API_BASE_URL=http://localhost:8080
VITE_API_TIMEOUT=10000
```

실행:

```bash
cd frontend
pnpm install
pnpm dev
```

- 기본 접속: `http://localhost:5173`

