import { useEffect, useState } from "react";
import { useNavigate } from "react-router-dom";
import { getMyOpenQuotes, getMyCompletedQuotes } from "services/quoteService";
import type { QuoteListItem } from "types/QuoteTypes";
import { groupQuotesByDate } from "utils/quoteUtils";
import QuoteCard from "components/quote/QuoteCard";
import { logError } from "utils/errorUtils";

const MyQuotesPage = () => {
  const navigate = useNavigate();
  const [activeTab, setActiveTab] = useState<"open" | "completed">("open");
  const [isLoading, setIsLoading] = useState(true);
  const [quotes, setQuotes] = useState<QuoteListItem[]>([]);

  useEffect(() => {
    loadQuotes();
  }, [activeTab]);

  const loadQuotes = async () => {
    try {
      setIsLoading(true);
      const data =
        activeTab === "open"
          ? await getMyOpenQuotes(0, 100)
          : await getMyCompletedQuotes(0, 100);
      setQuotes(data.content);
    } catch (error: unknown) {
      logError("견적 조회 실패:", error);
    } finally {
      setIsLoading(false);
    }
  };

  const handleBack = () => {
    navigate("/mypage");
  };

  const handleViewBids = (quoteId: string) => {
    navigate(`/mypage/quotes/${quoteId}`);
  };

  const groupedQuotes = groupQuotesByDate(quotes);

  return (
    <div className="min-h-screen bg-gray-50">
      <div className="max-w-md mx-auto bg-white min-h-screen">
        <div className="sticky top-0 bg-white border-b border-gray-200 z-10">
          <div className="flex items-center px-4 py-3">
            <button
              onClick={handleBack}
              className="mr-3 text-gray-600 hover:text-gray-900"
            >
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
                  d="M15 19l-7-7 7-7"
                />
              </svg>
            </button>
            <h1 className="text-lg font-bold text-gray-900">진행중인 견적</h1>
          </div>

          <div className="flex border-b border-gray-200">
            <button
              onClick={() => setActiveTab("open")}
              className={`flex-1 py-3 text-center font-medium ${
                activeTab === "open"
                  ? "text-black border-b-2 border-black"
                  : "text-gray-400"
              }`}
            >
              진행중
            </button>
            <button
              onClick={() => setActiveTab("completed")}
              className={`flex-1 py-3 text-center font-medium ${
                activeTab === "completed"
                  ? "text-black border-b-2 border-black"
                  : "text-gray-400"
              }`}
            >
              완료
            </button>
          </div>
        </div>

        <div className="px-4 py-4">
          {isLoading ? (
            <div className="flex items-center justify-center py-8">
              <div className="text-gray-500">로딩 중...</div>
            </div>
          ) : groupedQuotes.length === 0 ? (
            <div className="flex items-center justify-center py-8">
              <div className="text-gray-500">
                {activeTab === "open"
                  ? "진행중인 견적이 없습니다."
                  : "완료된 견적이 없습니다."}
              </div>
            </div>
          ) : (
            <div className="space-y-6">
              {groupedQuotes.map((group) => (
                <div key={group.date}>
                  <div className="flex items-center justify-between mb-3">
                    <h2 className="text-base font-medium text-gray-900">
                      {group.date}
                    </h2>
                    <span className="text-sm text-gray-500">{group.count}건</span>
                  </div>
                  <div>
                    {group.quotes.map((quote) => (
                      <QuoteCard
                        key={quote.id}
                        quote={quote}
                        onViewBids={handleViewBids}
                      />
                    ))}
                  </div>
                </div>
              ))}
            </div>
          )}
        </div>
      </div>
    </div>
  );
};

export default MyQuotesPage;

