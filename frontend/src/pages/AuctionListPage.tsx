import { useEffect, useState, useMemo } from "react";
import useSWR from "swr";
import { Quote } from "types/AuctionTypes";

interface FilterState {
  model: string;
  carrier: string;
  status: string;
  sort: string;
}

const AuctionListPage: React.FC = () => {
  useEffect(() => {
    document.title = "경매 목록 | PhoneBid";
  }, []);

  const [filters, setFilters] = useState<FilterState>({
    model: "",
    carrier: "",
    status: "",
    sort: "latest",
  });

  // 쿼리 파라미터 생성
  const queryString = useMemo(() => {
    const params = new URLSearchParams();
    if (filters.model) params.append("model", filters.model);
    if (filters.carrier) params.append("carrier", filters.carrier);
    if (filters.status) params.append("status", filters.status);
    if (filters.sort) params.append("sort", filters.sort);
    return params.toString();
  }, [filters]);

  // SWR을 사용한 데이터 페칭 (실시간 데이터 프리셋 사용)
  const {
    data: quotes,
    error,
    isLoading,
  } = useSWR<Quote[]>(queryString ? `/quotes?${queryString}` : "/quotes");

  const handleFilterChange = (key: keyof FilterState, value: string) => {
    setFilters((prev) => ({ ...prev, [key]: value }));
  };

  return (
    <div className="min-h-[60vh] bg-background">
      <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-10">
        <div className="mb-8">
          <h1 className="text-2xl sm:text-3xl font-bold tracking-tight text-foreground">
            경매 목록
          </h1>
          <p className="mt-2 text-sm text-muted-foreground">
            최신 등록된 견적에 판매자들이 입찰한 목록을 확인하세요.
          </p>
        </div>

        {/* 필터 영역 */}
        <div className="grid grid-cols-1 md:grid-cols-4 gap-4 mb-8">
          <select
            className="border border-input rounded-md h-10 px-3 bg-white text-sm"
            value={filters.model}
            onChange={(e) => handleFilterChange("model", e.target.value)}
          >
            <option value="">기종 전체</option>
            <option value="iPhone 16">iPhone 16</option>
            <option value="iPhone 16 Pro">iPhone 16 Pro</option>
            <option value="Galaxy S25">Galaxy S25</option>
            <option value="Galaxy Z Fold7">Galaxy Z Fold7</option>
          </select>
          <select
            className="border border-input rounded-md h-10 px-3 bg-white text-sm"
            value={filters.carrier}
            onChange={(e) => handleFilterChange("carrier", e.target.value)}
          >
            <option value="">통신사 전체</option>
            <option value="SKT">SKT</option>
            <option value="KT">KT</option>
            <option value="LGU">LG U+</option>
          </select>
          <select
            className="border border-input rounded-md h-10 px-3 bg-white text-sm"
            value={filters.status}
            onChange={(e) => handleFilterChange("status", e.target.value)}
          >
            <option value="">상태 전체</option>
            <option value="OPEN">진행중</option>
            <option value="CLOSED">마감</option>
            <option value="CONTRACTED">계약완료</option>
          </select>
          <select
            className="border border-input rounded-md h-10 px-3 bg-white text-sm"
            value={filters.sort}
            onChange={(e) => handleFilterChange("sort", e.target.value)}
          >
            <option value="latest">정렬: 최신순</option>
            <option value="expiring">마감임박순</option>
            <option value="popular">입찰많은순</option>
          </select>
        </div>

        {/* 로딩 상태 */}
        {isLoading && (
          <div className="flex justify-center items-center py-12">
            <div className="animate-spin rounded-full h-8 w-8 border-b-2 border-primary-600"></div>
            <span className="ml-2 text-muted-foreground">로딩 중...</span>
          </div>
        )}

        {/* 에러 상태 */}
        {error && (
          <div className="text-center py-12">
            <div className="text-red-500 mb-2">
              ⚠️ 데이터를 불러오는데 실패했습니다
            </div>
            <p className="text-muted-foreground text-sm">
              잠시 후 다시 시도해주세요.
            </p>
          </div>
        )}

        {/* 빈 상태 */}
        {!isLoading && !error && (!quotes || quotes.length === 0) && (
          <div className="text-center py-12">
            <div className="text-muted-foreground mb-2">
              📱 등록된 견적이 없습니다
            </div>
            <p className="text-muted-foreground text-sm">
              첫 번째 견적을 등록해보세요!
            </p>
          </div>
        )}

        {/* 견적 리스트 */}
        {!isLoading && !error && quotes && quotes.length > 0 && (
          <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-3 gap-6">
            {quotes.map((quote) => {
              const statusMap = {
                OPEN: { text: "진행중", color: "bg-green-100 text-green-800" },
                CLOSED: { text: "마감", color: "bg-gray-100 text-gray-800" },
                CONTRACTED: {
                  text: "계약완료",
                  color: "bg-blue-100 text-blue-800",
                },
              };

              const status = statusMap[quote.status] || statusMap.OPEN;

              // 마감까지 남은 시간 계산
              const expiredAt = new Date(quote.expiredAt);
              const now = new Date();
              const timeDiff = expiredAt.getTime() - now.getTime();
              const daysLeft = Math.ceil(timeDiff / (1000 * 60 * 60 * 24));
              const timeLeftText =
                daysLeft > 0 ? `마감 D-${daysLeft}` : "마감됨";

              return (
                <div
                  key={quote.id}
                  className="rounded-lg border border-border bg-card p-5 shadow-sm hover:shadow transition-shadow cursor-pointer"
                  onClick={() => {
                    // TODO: 견적 상세 페이지로 이동
                    // open quote detail
                  }}
                >
                  <div className="flex items-start justify-between">
                    <div>
                      <h3 className="text-base font-semibold text-foreground">
                        {quote.model} · {quote.storage} · {quote.color}
                      </h3>
                      <p className="text-sm text-muted-foreground">
                        {quote.carrier} · {quote.purchaseMethod || "신규"}
                      </p>
                    </div>
                    <span
                      className={`text-xs px-2 py-1 rounded ${status.color}`}
                    >
                      {status.text}
                    </span>
                  </div>
                  <div className="mt-4 flex items-center justify-between text-sm">
                    <div className="text-muted-foreground">입찰 진행중</div>
                    <div className="font-semibold text-primary-600">
                      경매 진행중
                    </div>
                  </div>
                  <div className="mt-2 text-xs text-muted-foreground">
                    {timeLeftText}
                  </div>
                </div>
              );
            })}
          </div>
        )}
      </div>
    </div>
  );
};

export default AuctionListPage;
