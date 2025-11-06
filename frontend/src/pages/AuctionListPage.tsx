import { useEffect } from "react";
import useSWR from "swr";
import { Quote } from "types/AuctionTypes";
import { PhoneModelResponse } from "types/PhoneModelTypes";
import { realtimeDataConfig, staticDataConfig } from "services/swrConfig";

const AuctionListPage: React.FC = () => {
  useEffect(() => {
    document.title = "경매 목록 | PhoneBid";
  }, []);

  // PhoneModel 목록 로드 (정적 데이터)
  const { data: models } = useSWR<PhoneModelResponse[]>(
    "/phone/models",
    staticDataConfig
  );

  // SWR을 사용한 데이터 페칭 (실시간 데이터 프리셋 사용)
  const {
    data: quotes,
    error,
    isLoading,
  } = useSWR<Quote[]>("/auction/quotes", realtimeDataConfig);

  // 디버깅용 로그
  useEffect(() => {
    if (quotes) {
      console.log("Quotes data:", quotes);
    }
    if (error) {
      console.error("Quotes error:", error);
    }
  }, [quotes, error]);

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
          <select className="border border-input rounded-md h-10 px-3 bg-white text-sm">
            <option value="">기종 전체</option>
            {models?.map((model) => (
              <option key={model.id} value={model.id}>
                {model.model}
              </option>
            ))}
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
          <>
            <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-3 gap-6">
              {quotes.map((quote) => {
                const statusMap = {
                  OPEN: {
                    text: "진행중",
                    color: "bg-green-100 text-green-800",
                  },
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
                          {quote.phoneModel?.model || "모델 정보 없음"} ·{" "}
                          {quote.storage?.displayLabel ||
                            quote.storage?.optionValue ||
                            "저장용량 정보 없음"}{" "}
                          ·{" "}
                          {quote.color?.displayLabel ||
                            quote.color?.optionValue ||
                            "색상 정보 없음"}
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
          </>
        )}
      </div>
    </div>
  );
};

export default AuctionListPage;
