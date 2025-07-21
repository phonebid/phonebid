# PhoneBid 프로젝트 개발 가이드라인 (AI Agent 전용)

## 프로젝트 개요

**프로젝트 성격**: 휴대폰 역경매 웹 플랫폼 (소비자 견적 등록 → 판매자 입찰)
**아키텍처**: React 프런트엔드 + Spring Boot 백엔드 (Monorepo)
**핵심 기술**: React 19 + TypeScript + Tailwind CSS v3 + pnpm workspace

**✅ 최신 업데이트**: React 19 + Zustand 상태관리 시스템 (Recoil에서 마이그레이션 완료)

## 필수 디렉터리 구조

```
/frontend/src/
├── assets/          # 이미지, 폰트, 아이콘
├── components/      # 재사용 가능한 UI 컴포넌트
│   ├── common/     # 공통 컴포넌트 (Button, Input, Modal 등)
│   ├── auction/    # 경매 관련 컴포넌트
│   └── layout/     # 레이아웃 컴포넌트 (Header, Footer, Sidebar)
├── pages/          # 라우팅 기반 페이지 컴포넌트
│   ├── consumer/   # 소비자 페이지
│   ├── seller/     # 판매자 페이지
│   └── admin/      # 관리자 페이지
├── services/       # API 호출 로직 (axios wrapper)
├── hooks/          # 커스텀 훅
├── store/          # 전역 상태관리 (Zustand stores)
├── utils/          # 헬퍼 함수, 상수, 유틸리티
├── types/          # TypeScript 타입 정의
└── styles/         # Tailwind 설정, 글로벌 CSS
```

**명령**: 새로운 기능 추가 시 반드시 위 구조를 따라 파일을 배치하라.

## 네이밍 컨벤션

### 파일명

- **컴포넌트**: PascalCase (`AuctionList.tsx`)
- **페이지**: PascalCase (`ConsumerDashboard.tsx`)
- **서비스/훅/유틸**: camelCase (`auctionService.ts`, `useAuctionTimer.ts`)
- **타입 정의**: PascalCase (`AuctionTypes.ts`)
- **Zustand 스토어**: camelCase (`authStore.ts`, `auctionStore.ts`)

### 코드 내 네이밍

- **컴포넌트명**: PascalCase (`const AuctionCard = () => {}`)
- **변수/함수**: camelCase (`const userName = ''`, `const fetchAuctions = () => {}`)
- **타입/인터페이스**: PascalCase (`interface UserData {}`, `type ApiResponse<T> = {}`)
- **상수**: UPPER_SNAKE_CASE (`const API_BASE_URL = ''`)
- **CSS 클래스**: Tailwind 유틸리티 클래스만 사용
- **Zustand 훅**: useXxxStore (`useAuthStore`, `useAuctionStore`)

## 필수 Import 규칙

### 절대 경로 Import

```typescript
// ✅ 올바른 방법
import Button from "components/common/Button";
import { useAuctionTimer } from "hooks/useAuctionTimer";
import { auctionService } from "services/auctionService";
import { useAuthStore } from "store/authStore";

// ❌ 금지된 방법
import Button from "../../../components/common/Button";
import { useAuctionTimer } from "../../hooks/useAuctionTimer";
```

**명령**: 모든 import는 절대 경로를 사용하라. 상대 경로 import 발견 시 즉시 절대 경로로 변경하라.

### React 19 Import 규칙

```typescript
// ✅ React 19에서 올바른 방법 (JSX Transform 자동 처리)
import { useState, useEffect } from "react";
import { Link } from "react-router-dom";

// ❌ React 19에서 불필요한 방법
import React from "react"; // JSX Transform이 자동 처리하므로 불필요

// ✅ 예외: main.tsx에서는 React import 필요
import React from "react";
import { createRoot } from "react-dom/client";
```

**명령**: React 19에서는 JSX 컴포넌트 작성 시 React import를 하지 마라. 단, main.tsx는 예외이다.

### Import 순서

```typescript
// 1. React 훅 (React import 제외)
import { useState, useEffect } from "react";

// 2. 외부 라이브러리
import axios from "axios";
import { toast } from "react-toastify";

// 3. 내부 컴포넌트/서비스
import Button from "components/common/Button";
import { auctionService } from "services/auctionService";

// 4. Zustand 스토어
import { useAuthStore } from "store/authStore";
import { useAuctionStore } from "store/auctionStore";

// 5. 타입 정의
import type { Auction, ApiResponse } from "types/AuctionTypes";
```

## 필수 의존성 관리

### pnpm Workspace 설정

**명령**: 새 패키지 설치 시 다음 명령어만 사용하라:

```bash
# 프론트엔드 전용 패키지
pnpm add [package] --filter frontend

# 전역 공통 패키지 (루트에 설치)
pnpm add [package] -w
```

### 필수 설치 패키지 목록

```json
{
  "dependencies": {
    "react": "^19.1.0",
    "react-dom": "^19.1.0",
    "react-router-dom": "^7.0.0",
    "zustand": "^5.0.0",
    "axios": "^1.0.0",
    "react-toastify": "^11.0.0"
  },
  "devDependencies": {
    "@types/react": "^19.1.0",
    "@types/react-dom": "^19.1.0",
    "tailwindcss": "^3.4.0",
    "@tailwindcss/forms": "^0.5.0",
    "@tailwindcss/typography": "^0.5.0"
  }
}
```

**✅ 최신 상태**:

- React 19 사용 (최신 안정 버전)
- Zustand 5.x 사용 (React 19 완벽 호환)
- Tailwind CSS v3.4.x 사용 (안정성 확보)

**명령**: 위 패키지들이 설치되지 않은 상태에서 관련 기능 구현 시 먼저 패키지를 설치하라.

## Zustand 상태 관리 규칙

### 스토어 정의 패턴

```typescript
// ✅ 필수 패턴
import { create } from "zustand";
import { devtools, persist } from "zustand/middleware";
import type { User } from "types/UserTypes";

interface AuthStore {
  // State
  isAuthenticated: boolean;
  user: User | null;
  accessToken: string | null;

  // Actions
  login: (user: User, accessToken: string) => void;
  logout: () => void;
  updateUser: (userData: Partial<User>) => void;
  setAccessToken: (token: string) => void;
}

export const useAuthStore = create<AuthStore>()(
  devtools(
    persist(
      (set, get) => ({
        // Initial state
        isAuthenticated: false,
        user: null,
        accessToken: null,

        // Actions
        login: (user: User, accessToken: string) => {
          set(
            {
              isAuthenticated: true,
              user,
              accessToken,
            },
            false,
            "auth/login"
          );
        },

        logout: () => {
          set(
            {
              isAuthenticated: false,
              user: null,
              accessToken: null,
            },
            false,
            "auth/logout"
          );
        },

        updateUser: (userData: Partial<User>) => {
          const currentUser = get().user;
          if (currentUser) {
            set(
              {
                user: { ...currentUser, ...userData },
              },
              false,
              "auth/updateUser"
            );
          }
        },

        setAccessToken: (token: string) => {
          set({ accessToken: token }, false, "auth/setAccessToken");
        },
      }),
      {
        name: "auth-storage",
        partialize: (state) => ({
          isAuthenticated: state.isAuthenticated,
          user: state.user,
          accessToken: state.accessToken,
        }),
      }
    ),
    {
      name: "auth-store",
    }
  )
);
```

### 컴포넌트에서 사용 패턴

```typescript
// ✅ 필수 패턴
const Header = () => {
  // 필요한 상태와 액션만 구조분해할당
  const { isAuthenticated, user, logout } = useAuthStore();

  const handleLogout = () => {
    logout();
    // 추가 로직...
  };

  return (
    // JSX 렌더링
  );
};
```

### Computed Values (Getters) 패턴

```typescript
// ✅ 필수 패턴
interface AuctionStore {
  auctions: Auction[];

  // Computed values as functions
  activeAuctions: () => Auction[];
  completedAuctions: () => Auction[];
  auctionCounts: () => {
    total: number;
    active: number;
    completed: number;
  };
}

export const useAuctionStore = create<AuctionStore>()(
  devtools((set, get) => ({
    auctions: [],

    // Computed values
    activeAuctions: () => {
      return get().auctions.filter((auction) => auction.status === "ACTIVE");
    },

    auctionCounts: () => {
      const auctions = get().auctions;
      return {
        total: auctions.length,
        active: auctions.filter((a) => a.status === "ACTIVE").length,
        completed: auctions.filter((a) => a.status === "COMPLETED").length,
      };
    },
  }))
);

// 컴포넌트에서 사용
const { activeAuctions, auctionCounts } = useAuctionStore();
const active = activeAuctions();
const counts = auctionCounts();
```

**명령**: 모든 전역 상태는 Zustand 스토어로 관리하라. Recoil atoms/selectors 사용을 금지한다.

## 필수 오류 처리 패턴

### API 호출 오류 처리

```typescript
// ✅ 필수 패턴
export async function fetchAuctions(): Promise<Auction[]> {
  try {
    const response = await axios.get<ApiResponse<Auction[]>>("/api/auctions");
    return response.data.data;
  } catch (error: any) {
    const errorMessage =
      error.response?.data?.message || "데이터를 불러오는데 실패했습니다.";
    console.error("fetchAuctions Error:", error);
    toast.error(errorMessage);
    throw new ApiErrorClass(error.response?.status || 500, errorMessage);
  }
}
```

### 컴포넌트 오류 처리

```typescript
// ✅ 필수 패턴
const AuctionList = () => {
  const [auctions, setAuctions] = useState<Auction[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    const loadAuctions = async () => {
      try {
        setLoading(true);
        setError(null);
        const data = await fetchAuctions();
        setAuctions(data);
      } catch (err) {
        setError('경매 목록을 불러올 수 없습니다.');
      } finally {
        setLoading(false);
      }
    };

    loadAuctions();
  }, []);

  if (loading) return <div>로딩중...</div>;
  if (error) return <div className="text-red-500">{error}</div>;

  return (
    // 컴포넌트 렌더링
  );
};
```

**명령**: 모든 비동기 작업에는 반드시 위 패턴을 적용하라.

## 스타일링 규칙

### Tailwind CSS 강제 사용

```tsx
// ✅ 올바른 방법
<button className="px-4 py-2 bg-blue-600 text-white rounded-md hover:bg-blue-700 transition-colors">
  입찰하기
</button>

// ❌ 금지된 방법
<button style={{ padding: '8px 16px', backgroundColor: 'blue' }}>
  입찰하기
</button>
```

**명령**: 인라인 스타일 사용을 금지한다. 모든 스타일링은 Tailwind CSS 클래스로만 처리하라.

### 반응형 디자인 필수 적용

```tsx
// ✅ 필수 패턴
<div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-4">
  {auctions.map((auction) => (
    <div key={auction.id} className="p-4 border rounded-lg">
      {/* 경매 카드 내용 */}
    </div>
  ))}
</div>
```

## API 통신 규칙

### 서비스 레이어 패턴

```typescript
// services/auctionService.ts
class AuctionService {
  private readonly baseURL = "/api/auctions";

  async getAuctions(): Promise<Auction[]> {
    const response = await axios.get<ApiResponse<Auction[]>>(this.baseURL);
    return response.data.data;
  }

  async createAuction(auction: CreateAuctionRequest): Promise<Auction> {
    const response = await axios.post<ApiResponse<Auction>>(
      this.baseURL,
      auction
    );
    return response.data.data;
  }

  async placeBid(auctionId: number, bid: PlaceBidRequest): Promise<Bid> {
    const response = await axios.post<ApiResponse<Bid>>(
      `${this.baseURL}/${auctionId}/bids`,
      bid
    );
    return response.data.data;
  }
}

export const auctionService = new AuctionService();
```

**명령**: 모든 API 호출은 서비스 클래스를 통해 수행하라. 컴포넌트에서 직접 axios 호출을 금지한다.

## 타입 정의 규칙

### API 응답 타입

```typescript
// types/ApiTypes.ts
export interface ApiResponse<T> {
  code: number;
  message: string;
  data: T;
}

export interface ApiError {
  code: number;
  message: string;
}
```

### 도메인 타입

```typescript
// types/AuctionTypes.ts
export interface Auction {
  id: number;
  product: string;
  model: string;
  capacity: string;
  color: string;
  carrier: string;
  condition: string;
  hopePrice: number;
  endTime: string;
  status: "ACTIVE" | "COMPLETED" | "CANCELLED";
  createdAt: string;
  updatedAt: string;
}

export interface CreateAuctionRequest {
  product: string;
  model: string;
  capacity: string;
  color: string;
  carrier: string;
  condition: string;
  hopePrice: number;
}
```

**명령**: 모든 API 요청/응답에는 명시적 타입을 정의하라. any 타입 사용을 금지한다.

## 컴포넌트 작성 규칙

### 함수형 컴포넌트 패턴 (React 19)

```typescript
// ✅ 필수 패턴 (React 19)
interface AuctionCardProps {
  auction: Auction;
  onBidClick: (auctionId: number) => void;
}

const AuctionCard: React.FC<AuctionCardProps> = ({ auction, onBidClick }) => {
  return (
    <div className="p-4 border rounded-lg shadow-sm">
      <h3 className="text-lg font-semibold">{auction.product}</h3>
      <p className="text-gray-600">{auction.model}</p>
      <div className="mt-4 flex justify-between items-center">
        <span className="text-xl font-bold text-blue-600">
          ₩{auction.hopePrice.toLocaleString()}
        </span>
        <button
          onClick={() => onBidClick(auction.id)}
          className="px-4 py-2 bg-blue-600 text-white rounded-md hover:bg-blue-700"
        >
          입찰하기
        </button>
      </div>
    </div>
  );
};

export default AuctionCard;
```

**명령**: 모든 컴포넌트는 Props 인터페이스를 정의하고 React.FC 타입을 사용하라. React 19에서는 React import를 하지 마라.

## 금지사항

### ❌ 절대 금지

1. **Recoil 사용**: Zustand로 완전 마이그레이션되었으므로 Recoil 사용 금지
2. **React import (컴포넌트)**: React 19 JSX Transform이 자동 처리하므로 불필요
3. **Tailwind CSS v4 사용**: 유틸리티 클래스 인식 문제로 인한 금지
4. **any 타입 사용**: 타입 안전성을 해치는 any 타입 사용 금지
5. **상대 경로 import**: `../../../` 형태의 상대 경로 import 금지
6. **인라인 스타일**: style 속성 사용 금지, Tailwind CSS만 사용
7. **컴포넌트 내 직접 API 호출**: 서비스 레이어 우회 금지
8. **전역 변수 사용**: window 객체 직접 조작 금지
9. **하드코딩**: 매직 넘버, 문자열 하드코딩 금지
10. **console.log**: 프로덕션 코드에 console.log 남기기 금지
11. **거대한 컴포넌트**: 300줄 이상 컴포넌트 작성 금지

### ❌ 패키지 설치 금지 목록

- recoil (Zustand로 대체)
- react@^18.x.x (React 19 사용)
- tailwindcss@^4.0.0 (유틸리티 클래스 문제)
- styled-components (Tailwind CSS 사용)
- emotion (Tailwind CSS 사용)
- material-ui (커스텀 디자인 시스템 사용)
- bootstrap (Tailwind CSS 사용)

## 필수 설정 파일

### vite.config.ts 필수 설정

```typescript
import { defineConfig } from "vite";
import react from "@vitejs/plugin-react";
import * as path from "path";
import { fileURLToPath } from "url";

const __dirname = path.dirname(fileURLToPath(import.meta.url));

export default defineConfig({
  plugins: [react()],
  resolve: {
    alias: {
      "@": path.resolve(__dirname, "./src"),
      components: path.resolve(__dirname, "./src/components"),
      pages: path.resolve(__dirname, "./src/pages"),
      services: path.resolve(__dirname, "./src/services"),
      hooks: path.resolve(__dirname, "./src/hooks"),
      store: path.resolve(__dirname, "./src/store"),
      utils: path.resolve(__dirname, "./src/utils"),
      types: path.resolve(__dirname, "./src/types"),
      assets: path.resolve(__dirname, "./src/assets"),
      styles: path.resolve(__dirname, "./src/styles"),
    },
  },
});
```

### tailwind.config.js 필수 설정

```javascript
/** @type {import('tailwindcss').Config} */
export default {
  content: ["./index.html", "./src/**/*.{js,ts,jsx,tsx}"],
  theme: {
    extend: {
      colors: {
        primary: {
          50: "#eff6ff",
          500: "#3b82f6",
          600: "#2563eb",
          700: "#1d4ed8",
        },
      },
    },
  },
  plugins: [require("@tailwindcss/forms"), require("@tailwindcss/typography")],
};
```

**명령**: 프로젝트 초기 설정 시 위 설정 파일들을 반드시 생성하라.

## Zustand 마이그레이션 가이드

### Recoil → Zustand 변환 패턴

```typescript
// ❌ 기존 Recoil 패턴 (사용 금지)
// import { atom, selector, useRecoilState, useRecoilValue } from 'recoil';
//
// const authState = atom<AuthState>({
//   key: 'authState',
//   default: { isAuthenticated: false, user: null }
// });

// ✅ Zustand 패턴 (필수 사용)
import { create } from "zustand";

interface AuthStore {
  isAuthenticated: boolean;
  user: User | null;
  login: (user: User) => void;
  logout: () => void;
}

export const useAuthStore = create<AuthStore>()((set) => ({
  isAuthenticated: false,
  user: null,
  login: (user) => set({ isAuthenticated: true, user }),
  logout: () => set({ isAuthenticated: false, user: null }),
}));

// 컴포넌트에서 사용
const { isAuthenticated, user, login, logout } = useAuthStore();
```

**명령**: Recoil atoms/selectors 발견 시 즉시 Zustand 스토어로 변환하라.

## 파일 간 상호작용 규칙

### 동시 수정 필요 파일들

1. **새 페이지 추가 시**:

   - `pages/` 디렉터리에 페이지 컴포넌트 생성
   - 라우팅 설정 파일에 경로 추가
   - 네비게이션 컴포넌트에 메뉴 추가

2. **새 API 엔드포인트 추가 시**:

   - `services/` 디렉터리에 서비스 메서드 추가
   - `types/` 디렉터리에 타입 정의 추가
   - 관련 Zustand 스토어에 액션 추가

3. **새 컴포넌트 추가 시**:
   - 컴포넌트 파일 생성
   - Props 타입 정의
   - 스토리북 스토리 생성 (있는 경우)

**명령**: 위 경우에 해당하는 작업 시 관련 파일들을 모두 함께 수정하라.

## AI 의사결정 가이드라인

### 우선순위 결정

1. **기능 구현 시**: React 19 호환성 → Zustand 패턴 → 타입 안전성 → 사용자 경험 → 성능 → 코드 가독성
2. **라이브러리 선택 시**: React 19 호환성 → 공식 지원 → 커뮤니티 크기 → 번들 크기 → 학습 곡선
3. **아키텍처 결정 시**: 확장성 → 유지보수성 → 개발 속도 → 복잡도

### 애매한 상황 처리

1. **상태관리 선택 시**: Zustand 우선 고려 (React 19 완벽 호환)
2. **타입 정의가 불명확한 경우**: 가장 구체적인 타입을 정의하고 주석으로 설명
3. **API 응답 구조가 불확실한 경우**: 옵셔널 필드로 정의하고 런타임 검증 추가
4. **컴포넌트 분리 기준 모호한 경우**: 재사용 가능성이 있으면 분리, 없으면 통합

**명령**: 위 가이드라인에 따라 일관된 결정을 내리고, 결정 이유를 주석으로 남겨라.

## 코드 품질 검증

### 체크리스트

- [ ] React 19.x 버전을 사용하고 있는가?
- [ ] Zustand 5.x 버전을 사용하고 있는가?
- [ ] Tailwind CSS v3.4.x를 사용하고 있는가?
- [ ] 모든 import가 절대 경로인가?
- [ ] React import를 불필요하게 하지 않았는가? (main.tsx 제외)
- [ ] 모든 타입이 명시적으로 정의되었는가?
- [ ] 오류 처리가 적절히 구현되었는가?
- [ ] Tailwind CSS만 사용했는가?
- [ ] 컴포넌트가 300줄을 초과하지 않는가?
- [ ] 서비스 레이어를 통해 API를 호출했는가?
- [ ] Zustand 스토어로 상태를 관리했는가?
- [ ] 반응형 디자인이 적용되었는가?

**명령**: 코드 작성 완료 후 위 체크리스트를 검증하고 문제가 있으면 수정하라.
