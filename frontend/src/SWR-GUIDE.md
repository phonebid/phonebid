# SWR 사용 가이드

## 📋 개요

이 프로젝트에서 SWR(Stale-While-Revalidate)을 사용하여 API 데이터를 효율적으로 관리하는 방법을 설명합니다.

## 🚀 빠른 시작

### 1. 기본 사용법

```typescript
import { useCustomSWR } from "hooks/useSWR";

function UserProfile() {
  const { data, error, isLoading } = useCustomSWR<User>("/users/me");

  if (error) return <div>에러가 발생했습니다</div>;
  if (isLoading) return <div>로딩 중...</div>;

  return <div>안녕하세요, {data?.name}님!</div>;
}
```

### 2. 조건부 요청

```typescript
function QuoteDetail({ quoteId }: { quoteId?: string }) {
  // quoteId가 없으면 요청하지 않음
  const { data } = useCustomSWR<Quote>(quoteId ? `/quotes/${quoteId}` : null);

  return data ? <QuoteCard quote={data} /> : <div>견적을 선택하세요</div>;
}
```

### 3. URL 파라미터 사용

```typescript
function UserQuotes({ userId }: { userId: string }) {
  // URL path에 변수 포함
  const { data } = useCustomSWR<Quote[]>(`/users/${userId}/quotes`);

  return <QuoteList quotes={data} />;
}

function QuoteSearch({
  keyword,
  category,
}: {
  keyword: string;
  category?: string;
}) {
  // 쿼리 파라미터 생성
  const params = new URLSearchParams();
  if (keyword) params.append("keyword", keyword);
  if (category) params.append("category", category);

  const { data } = useCustomSWR<Quote[]>(`/quotes?${params.toString()}`);

  return <SearchResults quotes={data} />;
}
```

### 4. 커스텀 fetcher 사용

```typescript
// POST body가 필요한 경우 (검색 등)
function AdvancedSearch({ filters }: { filters: SearchFilters }) {
  const { data } = useCustomSWR<Quote[]>(
    ["search-quotes", filters], // 키를 배열로 사용
    async ([url, searchFilters]) => {
      // POST 요청으로 복잡한 검색 조건 전달
      return apiClient.post<Quote[]>("/quotes/search", searchFilters);
    }
  );

  return <SearchResults quotes={data} />;
}

// 커스텀 설정과 함께
function RealtimeData() {
  const { data } = useCustomSWR<Bid[]>("/bids", {
    refreshInterval: 5000, // 5초마다 자동 갱신
    revalidateOnFocus: true, // 포커스 시 재검증
  });

  return <BidList bids={data} />;
}
```

## 🔄 기존 코드에서 마이그레이션

### Before (기존 apiClient 사용)

```typescript
import { apiClient } from "services/apiClient";

function UserProfile() {
  const [user, setUser] = useState<User | null>(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    const fetchUser = async () => {
      try {
        setLoading(true);
        const userData = await apiClient.get<User>("/users/me");
        setUser(userData);
      } catch (err) {
        setError("Failed to fetch user");
      } finally {
        setLoading(false);
      }
    };

    fetchUser();
  }, []);

  if (error) return <div>{error}</div>;
  if (loading) return <div>로딩 중...</div>;

  return <div>안녕하세요, {user?.name}님!</div>;
}
```

### After (SWR 사용)

```typescript
import { useCustomSWR } from "hooks/useSWR";

function UserProfile() {
  const { data: user, error, isLoading } = useCustomSWR<User>("/users/me");

  if (error) return <div>에러가 발생했습니다</div>;
  if (isLoading) return <div>로딩 중...</div>;

  return <div>안녕하세요, {user?.name}님!</div>;
}
```

## 🗂️ 캐시 관리 & Mutation

### 방법 1: 기존 apiClient + 캐시 갱신 (권장)

```typescript
import { mutate } from "hooks/useSWR";
import { apiClient } from "services/apiClient";

function UpdateUserButton() {
  const handleUpdate = async () => {
    // 사용자 정보 업데이트 API 호출
    await apiClient.put("/users/me", userData);

    // SWR 캐시 갱신
    mutate("/users/me");
  };

  return <button onClick={handleUpdate}>정보 수정</button>;
}
```

### 방법 2: useSWRMutation 사용 (고급)

```typescript
import useSWRMutation from "swr/mutation";
import { apiClient } from "services/apiClient";

// mutation 함수 정의
async function updateUser(url: string, { arg }: { arg: UserData }) {
  return apiClient.put(url, arg);
}

function UpdateUserButton() {
  const { trigger, isMutating } = useSWRMutation("/users/me", updateUser);

  const handleUpdate = async () => {
    try {
      await trigger(userData);
      // 성공 시 자동으로 관련 캐시 재검증
    } catch (error) {
      console.error("업데이트 실패:", error);
    }
  };

  return (
    <button onClick={handleUpdate} disabled={isMutating}>
      {isMutating ? "업데이트 중..." : "정보 수정"}
    </button>
  );
}
```

### 패턴 기반 캐시 무효화

```typescript
import { invalidatePattern } from "hooks/useSWR";

function LogoutButton() {
  const handleLogout = async () => {
    await apiClient.post("/auth/logout");

    // 사용자 관련 모든 캐시 무효화
    await invalidatePattern("/users");
  };

  return <button onClick={handleLogout}>로그아웃</button>;
}
```

## 📱 실제 사용 예시

### 견적 목록 페이지 (페이지네이션)

```typescript
function QuoteListPage({
  page = 1,
  size = 10,
}: {
  page?: number;
  size?: number;
}) {
  // 쿼리 파라미터를 포함한 URL 생성
  const { data: quotes, error } = useCustomSWR<Quote[]>(
    `/quotes?page=${page}&size=${size}`
  );

  if (error) return <ErrorMessage />;
  if (!quotes) return <LoadingSpinner />;

  return (
    <div>
      <h1>견적 목록</h1>
      {quotes.map((quote) => (
        <QuoteCard key={quote.id} quote={quote} />
      ))}
    </div>
  );
}
```

### 사용자별 견적 조회

```typescript
function UserQuotesPage({ userId }: { userId: string }) {
  // URL path에 userId 포함
  const { data: quotes } = useCustomSWR<Quote[]>(`/users/${userId}/quotes`);
  const { data: user } = useCustomSWR<User>(`/users/${userId}`);

  if (!user || !quotes) return <LoadingSpinner />;

  return (
    <div>
      <h1>{user.name}님의 견적 목록</h1>
      {quotes.map((quote) => (
        <QuoteCard key={quote.id} quote={quote} />
      ))}
    </div>
  );
}
```

### 복잡한 검색 (POST body 사용)

```typescript
interface SearchFilters {
  keyword?: string;
  priceRange?: { min: number; max: number };
  brands?: string[];
  location?: string;
}

function QuoteSearchPage({ filters }: { filters: SearchFilters }) {
  // POST 요청이 필요한 복잡한 검색
  const { data: quotes, isLoading } = useCustomSWR<Quote[]>(
    ["quotes-search", filters], // 배열 키 사용
    async ([url, searchFilters]) => {
      // 커스텀 fetcher로 POST 요청
      return apiClient.post<Quote[]>("/quotes/search", searchFilters);
    }
  );

  if (isLoading) return <LoadingSpinner />;

  return (
    <div>
      <h1>검색 결과</h1>
      {quotes?.length ? (
        quotes.map((quote) => <QuoteCard key={quote.id} quote={quote} />)
      ) : (
        <div>검색 결과가 없습니다.</div>
      )}
    </div>
  );
}
```

### 입찰 실시간 업데이트

```typescript
function BidList({ quoteId }: { quoteId: string }) {
  const { data: bids } = useCustomSWR<Bid[]>(
    `/quotes/${quoteId}/bids`,
    { refreshInterval: 3000 } // 3초마다 갱신
  );

  return (
    <div>
      {bids?.map((bid) => (
        <BidItem key={bid.id} bid={bid} />
      ))}
    </div>
  );
}
```

### 최적화된 검색 컴포넌트

```typescript
import { useMemo } from "react";

interface SearchFilters {
  keyword?: string;
  category?: string;
  priceMin?: number;
  priceMax?: number;
}

function OptimizedSearch({ filters }: { filters: SearchFilters }) {
  // ✅ 방법 1: useMemo로 안정적인 키 생성
  const searchKey = useMemo(() => {
    if (!filters.keyword && !filters.category) return null;
    return [
      "search",
      filters.keyword,
      filters.category,
      filters.priceMin,
      filters.priceMax,
    ];
  }, [filters.keyword, filters.category, filters.priceMin, filters.priceMax]);

  const { data: quotes, isLoading } = useCustomSWR<Quote[]>(
    searchKey,
    searchKey
      ? async (key) => {
          const [, keyword, category, priceMin, priceMax] = key as string[];
          const params = new URLSearchParams();
          if (keyword) params.append("keyword", keyword);
          if (category) params.append("category", category);
          if (priceMin) params.append("priceMin", String(priceMin));
          if (priceMax) params.append("priceMax", String(priceMax));

          return apiClient.get<Quote[]>(`/quotes/search?${params}`);
        }
      : null
  );

  if (isLoading) return <LoadingSpinner />;

  return (
    <div>
      {quotes?.map((quote) => (
        <QuoteCard key={quote.id} quote={quote} />
      ))}
    </div>
  );
}

// ✅ 방법 2: 문자열 키 사용 (더 간단)
function SimpleSearch({
  keyword,
  category,
}: {
  keyword: string;
  category?: string;
}) {
  // 키가 문자열이므로 React가 자동으로 메모이제이션
  const { data } = useCustomSWR<Quote[]>(
    keyword
      ? `/quotes/search?keyword=${encodeURIComponent(keyword)}&category=${
          category || ""
        }`
      : null
  );

  return <SearchResults quotes={data} />;
}
```

## ❓ 자주 묻는 질문

### Q: SWR은 GET 요청에만 사용하나요?

A: **네, 맞습니다!** SWR은 주로 **데이터 조회(GET)**에 특화되어 있습니다. POST/PUT/DELETE는 다음과 같이 처리합니다:

- **권장**: 기존 `apiClient` 사용 + `mutate()`로 캐시 갱신
- **고급**: `useSWRMutation` 훅 사용 (별도 설치 필요)

### Q: URL에 파라미터나 body 값을 전달하려면?

A: 여러 방법이 있습니다:

```typescript
// 1. URL path 파라미터
const { data } = useCustomSWR<User>(`/users/${userId}`);

// 2. 쿼리 파라미터
const params = new URLSearchParams({ page: "1", size: "10" });
const { data } = useCustomSWR<Quote[]>(`/quotes?${params}`);

// 3. POST body가 필요한 경우 (커스텀 fetcher)
const { data } = useCustomSWR<Quote[]>(
  ["search", filters], // 키를 배열로
  async ([url, searchFilters]) =>
    apiClient.post("/quotes/search", searchFilters)
);
```

### Q: POST/PUT/DELETE는 어떻게 처리하나요?

A: 두 가지 방법이 있습니다:

```typescript
// 방법 1: 기존 방식 (권장)
await apiClient.post("/quotes", data);
mutate("/quotes"); // 캐시 갱신

// 방법 2: useSWRMutation (고급)
const { trigger } = useSWRMutation("/quotes", postQuote);
await trigger(data);
```

### Q: 기존 apiClient와 함께 사용할 수 있나요?

A: 네! SWR은 데이터 페칭을 위해 내부적으로 apiClient를 사용합니다. 기존 코드를 점진적으로 마이그레이션할 수 있습니다.

### Q: 에러 처리는 어떻게 하나요?

A: `error` 객체를 확인하여 에러를 처리할 수 있습니다. 글로벌 에러 처리는 `swrConfig.ts`의 `onError`에서 설정됩니다.

### Q: 로딩 상태는 어떻게 확인하나요?

A: `isLoading`은 최초 로드 시, `isValidating`은 백그라운드 갱신 시 true가 됩니다.

### Q: 값이 바뀔 때 불필요한 재요청을 방지하려면?

A: SWR은 키가 동일하면 캐시를 재사용합니다. 최적화 방법들:

```typescript
// ❌ 매번 새로운 객체 생성 - 불필요한 재요청 발생
function BadExample({ filters }: { filters: SearchFilters }) {
  const { data } = useCustomSWR(["search", filters], fetcher); // filters 객체가 매번 새로 생성되면 재요청
}

// ✅ useMemo로 객체 메모이제이션
function GoodExample({ filters }: { filters: SearchFilters }) {
  const stableFilters = useMemo(
    () => filters,
    [filters.keyword, filters.category, filters.priceRange]
  );

  const { data } = useCustomSWR(["search", stableFilters], fetcher);
}

// ✅ 원시값으로 키 구성 (가장 권장)
function BestExample({
  keyword,
  category,
}: {
  keyword: string;
  category?: string;
}) {
  const { data } = useCustomSWR(
    keyword ? `search?keyword=${keyword}&category=${category || ""}` : null,
    fetcher
  );
}
```

### Q: 캐시를 비활성화하려면?

A: 컴포넌트별로 `{ revalidateIfStale: false, revalidateOnFocus: false, revalidateOnReconnect: false }` 설정을 사용하세요.

## ⚙️ SWR 설정 프리셋

프로젝트에서는 데이터 특성에 따라 최적화된 SWR 설정 프리셋을 제공합니다.

### 기본 설정 (defaultSWRConfig)

모든 SWR 요청의 기본 설정입니다:

```typescript
import { useCustomSWR } from "hooks/useSWR";

// 기본 설정 사용 (별도 설정 없이)
const { data } = useCustomSWR<User>("/users/me");
```

**특징:**

- 보수적인 캐싱 전략으로 안정성 우선
- 포커스 시 재검증 비활성화 (성능 고려)
- 네트워크 재연결 시 재검증 활성화
- 최대 3회 에러 재시도

### 정적 데이터용 설정 (staticDataConfig)

**사용 대상:** 휴대폰 모델 목록, 통신사 목록, 색상 옵션 등 거의 변하지 않는 데이터

```typescript
import { useCustomSWR } from "hooks/useSWR";
import { staticDataConfig } from "services/swrConfig";

function PhoneModelsSelect() {
  const { data: models } = useCustomSWR<PhoneModel[]>(
    "/phone-models",
    staticDataConfig
  );

  return (
    <select>
      {models?.map((model) => (
        <option key={model.id} value={model.id}>
          {model.name}
        </option>
      ))}
    </select>
  );
}
```

**특징:**

- 장기간 캐싱 (1시간간 동일 요청 차단)
- 포커스/재연결 시에도 갱신 안 함
- 메모리 효율적인 캐싱

### 사용자 데이터용 설정 (userDataConfig)

**사용 대상:** 사용자 프로필, 설정 정보, 개인 정보 등 가끔 변하는 데이터

```typescript
import { useCustomSWR } from "hooks/useSWR";
import { userDataConfig } from "services/swrConfig";

function UserProfile() {
  const { data: user } = useCustomSWR<User>("/users/me", userDataConfig);

  return (
    <div>
      <h2>{user?.name}님의 프로필</h2>
      <p>이메일: {user?.email}</p>
    </div>
  );
}
```

**특징:**

- 중간 수준의 캐싱 (10분간 동일 요청 차단)
- 포커스 시 갱신 비활성화
- 개인정보 특성상 빈번한 변경이 없음을 고려

### 실시간 데이터용 설정 (realtimeDataConfig)

**사용 대상:** 입찰 현황, 경매 상태 등 자주 변하는 데이터

```typescript
import { useCustomSWR } from "hooks/useSWR";
import { realtimeDataConfig } from "services/swrConfig";

function LiveBidList({ quoteId }: { quoteId: string }) {
  const { data: bids } = useCustomSWR<Bid[]>(
    `/quotes/${quoteId}/bids`,
    realtimeDataConfig
  );

  return (
    <div>
      <h3>실시간 입찰 현황</h3>
      {bids?.map((bid) => (
        <div key={bid.id}>
          {bid.price.toLocaleString()}원 - {bid.seller.name}
        </div>
      ))}
    </div>
  );
}
```

**특징:**

- 30초마다 자동 갱신
- 포커스/재연결 시 즉시 갱신
- 짧은 중복 방지 간격 (5초)

### 민감한 데이터용 설정 (sensitiveDataConfig)

**사용 대상:** 결제 정보, 계약 상태 등 항상 최신이어야 하는 중요한 데이터

```typescript
import { useCustomSWR } from "hooks/useSWR";
import { sensitiveDataConfig } from "services/swrConfig";

function PaymentStatus({ contractId }: { contractId: string }) {
  const { data: payment } = useCustomSWR<Payment>(
    `/contracts/${contractId}/payment`,
    sensitiveDataConfig
  );

  return (
    <div>
      <h3>결제 상태</h3>
      <p>상태: {payment?.status}</p>
      <p>금액: {payment?.amount.toLocaleString()}원</p>
    </div>
  );
}
```

**특징:**

- 모든 상황에서 즉시 재검증
- 최소한의 중복 방지 (1초)
- 에러 재시도 1회로 제한 (빠른 실패)

### 설정 프리셋 선택 가이드

| 데이터 유형   | 변경 빈도      | 사용 설정             | 예시                     |
| ------------- | -------------- | --------------------- | ------------------------ |
| 마스터 데이터 | 거의 없음      | `staticDataConfig`    | 휴대폰 모델, 통신사 목록 |
| 사용자 데이터 | 가끔           | `userDataConfig`      | 프로필, 설정             |
| 일반 데이터   | 보통           | `defaultSWRConfig`    | 견적 목록, 게시글        |
| 실시간 데이터 | 자주           | `realtimeDataConfig`  | 입찰 현황, 채팅          |
| 민감한 데이터 | 즉시 반영 필요 | `sensitiveDataConfig` | 결제, 계약 상태          |

## 🔧 설정 파일

- **SWR 설정**: `services/swrConfig.ts`
- **SWR 훅**: `hooks/useSWR.ts`
- **SWR Provider**: `app/providers/SWRProvider.tsx`

## 📚 추가 학습 자료

- [SWR 공식 문서](https://swr.vercel.app/ko)
- [React SWR 가이드](https://swr.vercel.app/ko/docs/getting-started)
