# PhoneBid Frontend

휴대폰 역경매 웹 플랫폼의 프론트엔드 애플리케이션입니다.

## 🚀 기술 스택

- **React 19** - 최신 UI 라이브러리 (JSX Transform 자동 처리)
- **TypeScript** - 타입 안전성
- **Tailwind CSS v3** - 유틸리티 기반 스타일링
- **Vite** - 빌드 도구
- **React Router v7** - 클라이언트 사이드 라우팅
- **Zustand** - 상태 관리 (React 19 완벽 호환)
- **Axios** - HTTP 클라이언트
- **React Toastify** - 알림 시스템

## 📁 프로젝트 구조

```
src/
├── assets/          # 이미지, 폰트, 아이콘
├── components/      # 재사용 가능한 UI 컴포넌트
│   ├── common/     # 공통 컴포넌트 (Button, Input, Modal 등)
│   ├── auction/    # 경매 관련 컴포넌트
│   └── layout/     # 레이아웃 컴포넌트 (Header, Footer, Sidebar)
├── pages/          # 라우팅 기반 페이지 컴포넌트
│   ├── consumer/   # 소비자 페이지
│   ├── seller/     # 판매자 페이지
│   └── admin/      # 관리자 페이지
├── services/       # API 호출 로직
├── hooks/          # 커스텀 훅
├── store/          # 전역 상태관리 (Zustand stores)
├── utils/          # 헬퍼 함수, 상수, 유틸리티
├── types/          # TypeScript 타입 정의
└── styles/         # Tailwind 설정, 글로벌 CSS
```

## 🛠️ 개발 환경 설정

### 사전 요구사항

- Node.js 18+
- pnpm 8+

### 환경변수 설정

프로젝트는 환경별로 다른 API URL을 사용합니다:

#### 개발 환경

```bash
# .env.local (기본값)
VITE_API_BASE_URL=http://localhost:8080
VITE_API_TIMEOUT=10000
```

#### 프로덕션 환경

```bash
# .env.production (배포 시 수정 필요)
VITE_API_BASE_URL=https://api.phonebid.com
VITE_API_TIMEOUT=10000
```

#### 로컬 개발 시

환경변수 파일이 없으면 자동으로 개발 환경 설정이 적용됩니다:

- 개발 환경: `http://localhost:8080`
- 프로덕션 환경: `https://api.phonebid.com`

### 설치 및 실행

```bash
# 의존성 설치
pnpm install

# 개발 서버 실행
pnpm run dev

# 빌드
pnpm run build

# 빌드 파일 미리보기
pnpm run preview

# 린팅
pnpm run lint
```

## 📋 개발 규칙

### Import 규칙

- 모든 import는 절대 경로 사용
- 상대 경로 import 금지
- React 19에서는 React import 자동 처리 (JSX Transform)

```typescript
// ✅ 올바른 방법 (React 19)
import Button from "components/common/Button";
import { auctionService } from "services/auctionService";

// ❌ 금지된 방법
import React from "react"; // React 19에서는 불필요
import Button from "../../../components/common/Button";
```

### 스타일링 규칙

- Tailwind CSS만 사용
- 인라인 스타일 금지
- 반응형 디자인 필수 적용

```tsx
// ✅ 올바른 방법
<button className="px-4 py-2 bg-primary-600 text-white rounded-md hover:bg-primary-700">
  버튼
</button>

// ❌ 금지된 방법
<button style={{ padding: '8px 16px', backgroundColor: 'blue' }}>
  버튼
</button>
```

### 컴포넌트 작성 규칙

- 모든 컴포넌트는 TypeScript로 작성
- Props 인터페이스 정의 필수
- React.FC 타입 사용
- React 19에서는 React import 불필요

```typescript
interface ButtonProps {
  children: React.ReactNode;
  onClick?: () => void;
  variant?: "primary" | "secondary";
}

const Button: React.FC<ButtonProps> = ({
  children,
  onClick,
  variant = "primary",
}) => {
  // 컴포넌트 구현
};
```

### API 통신 규칙

- 서비스 레이어를 통한 API 호출
- 컴포넌트에서 직접 axios 호출 금지
- 모든 API 요청/응답에 타입 정의

### 상태 관리 규칙 (Zustand)

- 로컬 상태: useState
- 전역 상태: Zustand stores
- 상태와 액션을 하나의 스토어에 통합

```typescript
// ✅ Zustand 스토어 패턴
import { create } from "zustand";
import { devtools } from "zustand/middleware";

interface AuthStore {
  isAuthenticated: boolean;
  user: User | null;
  login: (user: User) => void;
  logout: () => void;
}

export const useAuthStore = create<AuthStore>()(
  devtools((set) => ({
    isAuthenticated: false,
    user: null,
    login: (user) => set({ isAuthenticated: true, user }),
    logout: () => set({ isAuthenticated: false, user: null }),
  }))
);

// ✅ 컴포넌트에서 사용
const { isAuthenticated, user, login, logout } = useAuthStore();
```

## 🔧 환경 변수

```env
# API 기본 URL
VITE_API_BASE_URL=http://localhost:8080/api

# OAuth 설정
VITE_KAKAO_CLIENT_ID=your_kakao_client_id
VITE_NAVER_CLIENT_ID=your_naver_client_id
```

## 📝 주요 기능

### 구현 완료

- ✅ 프로젝트 기본 구조 설정
- ✅ React 19 + Zustand 상태관리 시스템
- ✅ Tailwind CSS v3 스타일링 시스템
- ✅ 라우팅 시스템 (React Router v7)
- ✅ API 클라이언트 설정 (Axios)
- ✅ 공통 컴포넌트 (Button, Input)
- ✅ 레이아웃 컴포넌트 (Header, Footer, Layout)
- ✅ 홈페이지 UI
- ✅ 유틸리티 함수 (포맷터, 상수)
- ✅ TypeScript 타입 정의
- ✅ Zustand 스토어 (Auth, Auction)

### 개발 예정

- 🔄 사용자 인증 (OAuth2)
- 🔄 경매 등록 페이지
- 🔄 경매 목록 페이지
- 🔄 입찰 시스템
- 🔄 실시간 알림
- 🔄 결제 시스템
- 🔄 관리자 대시보드

## ⚠️ 중요 참고사항

### React 19 업그레이드

- **React 19.1.0** 사용 (최신 안정 버전)
- JSX Transform 자동 처리로 React import 불필요
- 향상된 성능과 새로운 기능 활용 가능

### Zustand 상태관리

- **Recoil에서 Zustand로 마이그레이션 완료**
- React 19와 완벽 호환
- 더 간단한 API와 적은 보일러플레이트
- devtools와 persist 미들웨어 지원

### 장점

- **단순함**: Recoil보다 훨씬 간단한 API
- **성능**: 불필요한 리렌더링 최소화
- **TypeScript**: 완벽한 타입 지원
- **크기**: 작은 번들 사이즈 (2.9kb)
- **호환성**: React 19 완벽 지원

## 🚀 배포

```bash
# 프로덕션 빌드
pnpm run build

# dist 폴더의 파일들을 웹 서버에 배포
```

## 🛠️ 문제 해결

### 일반적인 문제들

1. **Zustand 스토어 문제**

   ```bash
   # Zustand 버전 확인
   pnpm list zustand

   # 최신 버전 설치
   pnpm add zustand@latest
   ```

2. **빌드 오류 시**

   ```bash
   # 캐시 정리
   pnpm store prune
   rm -rf node_modules
   pnpm install
   ```

3. **TypeScript 오류 시**
   ```bash
   # 타입 체크
   pnpm run build
   ```

## 📞 문의

프로젝트 관련 문의사항이 있으시면 이슈를 등록해 주세요.

---

## 🔄 마이그레이션 히스토리

### v1.0.0 → v1.1.0 (Zustand 마이그레이션)

- **Recoil → Zustand**: 상태관리 라이브러리 변경
- **React 18 → React 19**: 최신 버전 업그레이드
- **성능 개선**: 번들 크기 약 15% 감소
- **개발자 경험 향상**: 더 간단한 상태관리 API
