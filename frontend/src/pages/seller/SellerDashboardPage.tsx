import { useEffect } from "react";
import { useNavigate } from "react-router-dom";
import { SellerHeader } from "components/seller/SellerHeader";
import { StatCard } from "components/seller/StatCard";
import { QuoteFilterBar } from "components/seller/QuoteFilterBar";
import { QuoteRequestCard } from "components/seller/QuoteRequestCard";
import { useSellerDashboard } from "hooks/useSellerDashboard";
import { Card, CardContent, CardHeader, CardTitle } from "components/ui/card";

const SellerDashboardPage: React.FC = () => {
  const navigate = useNavigate();
  const {
    quotes,
    isLoading,
    error,
    selectedCarrier,
    selectedBrand,
    sortBy,
    currentPage,
    totalPages,
    setSelectedCarrier,
    setSelectedBrand,
    setSortBy,
    setCurrentPage,
  } = useSellerDashboard();

  useEffect(() => {
    document.title = "판매자 대시보드 | PhoneBid";
  }, []);

  const handleQuoteClick = (quoteId: string) => {
    navigate(`/seller-center/quotes/${quoteId}/bid`);
  };

  // 최근 24시간 내에 생성된 요청을 "새로운 요청"으로 카운트
  const newRequestCount = quotes.filter((quote) => {
    const createdAt = new Date(quote.createdAt);
    const now = new Date();
    const diffInHours = (now.getTime() - createdAt.getTime()) / (1000 * 60 * 60);
    return diffInHours <= 24;
  }).length;

  return (
    <div className="min-h-screen bg-background flex flex-col">
      <SellerHeader />
      <div className="flex-1 bg-gray-100">
        <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-8">
        <div className="mb-8">
          <h1 className="text-3xl font-bold tracking-tight text-foreground">
            대시보드
          </h1>
          <p className="mt-2 text-sm text-muted-foreground">
            실시간 견적 요청을 확인하고 관리하세요
          </p>
        </div>

        <div className="grid grid-cols-1 md:grid-cols-3 gap-4 mb-4">
          <StatCard
            title="발송 수"
            value={89}
            description="기존에 발송한 총 견적"
            iconBgColor="bg-blue-500"
            icon={
              <svg
                className="w-6 h-6"
                fill="currentColor"
                viewBox="0 0 24 24"
              >
                <path d="M2.01 21L23 12 2.01 3 2 10l15 2-15 2z" />
              </svg>
            }
          />
          <StatCard
            title="거래중"
            value={34}
            description="진행 중인 거래"
            iconBgColor="bg-orange-500"
            icon={
              <svg
                className="w-6 h-6"
                fill="none"
                stroke="currentColor"
                viewBox="0 0 24 24"
              >
                <path
                  strokeLinecap="round"
                  strokeLinejoin="round"
                  strokeWidth={2}
                  d="M7 16.5V19m0-2.5v-6a1.5 1.5 0 113 0m-3 6a1.5 1.5 0 00-3 0v2a7.5 7.5 0 0015 0v-5a1.5 1.5 0 00-3 0m-6-3V11m0-5.5v-1a1.5 1.5 0 013 0v1m0 0V11m0-5.5a1.5 1.5 0 013 0v3m0 0V11"
                />
              </svg>
            }
          />
          <StatCard
            title="거래완료"
            value={124}
            description="완료된 거래"
            iconBgColor="bg-green-500"
            icon={
              <svg
                className="w-6 h-6"
                fill="none"
                stroke="currentColor"
                viewBox="0 0 24 24"
              >
                <path
                  strokeLinecap="round"
                  strokeLinejoin="round"
                  strokeWidth={2}
                  d="M9 12l2 2 4-4m6 2a9 9 0 11-18 0 9 9 0 0118 0z"
                />
              </svg>
            }
          />
        </div>

        <QuoteFilterBar
          selectedCarrier={selectedCarrier}
          selectedBrand={selectedBrand}
          sortBy={sortBy}
          onCarrierChange={setSelectedCarrier}
          onBrandChange={(brand) => setSelectedBrand(brand as "APPLE" | "SAMSUNG" | "ALL")}
          onSortChange={setSortBy}
        />

        <Card>
          <CardHeader>
            <div className="flex items-center justify-between">
              <CardTitle>실시간 견적 요청</CardTitle>
              <span className="text-sm text-foreground">
                새로운 요청 <span className="text-blue-600">{newRequestCount}</span>개
              </span>
            </div>
          </CardHeader>
          <CardContent className="p-0">
            {isLoading ? (
              <div className="flex justify-center items-center py-12">
                <div className="animate-spin rounded-full h-8 w-8 border-b-2 border-primary-600"></div>
                <span className="ml-2 text-muted-foreground">로딩 중...</span>
              </div>
            ) : error ? (
              <div className="text-center py-12">
                <div className="text-red-500 mb-2">
                  ⚠️ 데이터를 불러오는데 실패했습니다
                </div>
                <p className="text-muted-foreground text-sm">
                  잠시 후 다시 시도해주세요.
                </p>
              </div>
            ) : quotes.length === 0 ? (
              <div className="text-center py-12">
                <div className="text-muted-foreground mb-2">
                  📱 등록된 견적이 없습니다
                </div>
                <p className="text-muted-foreground text-sm">
                  새로운 견적 요청을 기다려주세요.
                </p>
              </div>
            ) : (
              <>
                <div className="overflow-x-auto">
                  <table className="w-full">
                    <thead>
                      <tr className="border-b bg-muted/50">
                        <th className="px-4 py-3 text-left text-sm font-medium text-muted-foreground">
                          제조사
                        </th>
                        <th className="px-4 py-3 text-left text-sm font-medium text-muted-foreground">
                          기종
                        </th>
                        <th className="px-4 py-3 text-left text-sm font-medium text-muted-foreground">
                          용량/색상
                        </th>
                        <th className="px-4 py-3 text-left text-sm font-medium text-muted-foreground">
                          통신사
                        </th>
                        <th className="px-4 py-3 text-left text-sm font-medium text-muted-foreground">
                          개통유형
                        </th>
                        <th className="px-4 py-3 text-left text-sm font-medium text-muted-foreground">
                          이동 통신사
                        </th>
                        <th className="px-4 py-3 text-left text-sm font-medium text-muted-foreground">
                          요청 시간
                        </th>
                        <th className="px-4 py-3 text-left text-sm font-medium text-muted-foreground">
                          입찰 건수
                        </th>
                        <th className="px-4 py-3 text-left text-sm font-medium text-muted-foreground">
                          상태
                        </th>
                      </tr>
                    </thead>
                    <tbody>
                      {quotes.map((quote) => (
                        <QuoteRequestCard
                          key={quote.id}
                          quote={quote}
                          onClick={() => handleQuoteClick(quote.id)}
                        />
                      ))}
                    </tbody>
                  </table>
                </div>

                {totalPages > 1 && (
                  <div className="flex items-center justify-center gap-2 px-4 py-4 border-t">
                    <button
                      onClick={() => setCurrentPage((prev) => Math.max(1, prev - 1))}
                      disabled={currentPage === 1}
                      className="flex items-center justify-center w-8 h-8 rounded border border-gray-300 bg-white text-gray-600 hover:bg-gray-50 disabled:opacity-50 disabled:cursor-not-allowed transition-colors"
                    >
                      <svg
                        className="w-4 h-4"
                        fill="none"
                        stroke="currentColor"
                        viewBox="0 0 24 24"
                      >
                        <path
                          strokeLinecap="round"
                          strokeLinejoin="round"
                          strokeWidth={2}
                          d="M15 19l-7-7 7-7"
                        />
                      </svg>
                    </button>
                    {Array.from({ length: totalPages }, (_, i) => i + 1).map(
                      (page) => (
                        <button
                          key={page}
                          onClick={() => setCurrentPage(page)}
                          className={`flex items-center justify-center w-8 h-8 rounded text-sm font-medium transition-colors ${
                            currentPage === page
                              ? "bg-blue-600 text-white"
                              : "bg-gray-100 text-gray-700 hover:bg-gray-200"
                          }`}
                        >
                          {page}
                        </button>
                      )
                    )}
                    <button
                      onClick={() =>
                        setCurrentPage((prev) => Math.min(totalPages, prev + 1))
                      }
                      disabled={currentPage === totalPages}
                      className="flex items-center justify-center w-8 h-8 rounded border border-gray-300 bg-white text-gray-600 hover:bg-gray-50 disabled:opacity-50 disabled:cursor-not-allowed transition-colors"
                    >
                      <svg
                        className="w-4 h-4"
                        fill="none"
                        stroke="currentColor"
                        viewBox="0 0 24 24"
                      >
                        <path
                          strokeLinecap="round"
                          strokeLinejoin="round"
                          strokeWidth={2}
                          d="M9 5l7 7-7 7"
                        />
                      </svg>
                    </button>
                  </div>
                )}
              </>
            )}
          </CardContent>
        </Card>
        </div>
      </div>

      {/* Footer */}
      <footer className="border-t border-gray-200 bg-white">
        <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-6">
          <div className="flex flex-col sm:flex-row items-center justify-between gap-4">
            <p className="text-sm text-gray-600">
              © 2026 PhoneQuote. All rights reserved.
            </p>
            <div className="flex items-center gap-6">
              <a
                href="/terms"
                className="text-sm text-gray-600 hover:text-gray-900 transition-colors"
              >
                이용약관
              </a>
              <a
                href="/privacy"
                className="text-sm text-gray-600 hover:text-gray-900 transition-colors"
              >
                개인정보처리방침
              </a>
              <a
                href="/customer-service"
                className="text-sm text-gray-600 hover:text-gray-900 transition-colors"
              >
                고객센터
              </a>
            </div>
          </div>
        </div>
      </footer>
    </div>
  );
};

export default SellerDashboardPage;

