import { useEffect, useState } from "react";
import { useNavigate } from "react-router-dom";
import { customerService } from "services/customerService";
import { toast } from "react-toastify";
import type { NoticeResponseDto, Page } from "types/CustomerServiceTypes";
import { formatDateSimple } from "utils/formatters";
import { logError } from "utils/errorUtils";

const NoticeListPage = () => {
  const navigate = useNavigate();
  const [isLoading, setIsLoading] = useState(true);
  const [currentPage, setCurrentPage] = useState(0);
  const [noticesPage, setNoticesPage] = useState<Page<NoticeResponseDto>>({
    content: [],
    totalElements: 0,
    totalPages: 0,
    size: 10,
    number: 0,
    first: true,
    last: true,
  });

  useEffect(() => {
    loadNotices();
  }, [currentPage]);

  const loadNotices = async () => {
    try {
      setIsLoading(true);
      const data = await customerService.getNotices(currentPage, 10);
      setNoticesPage(data);
    } catch (error: unknown) {
      logError("공지사항 목록 조회 실패:", error);
      if (error instanceof Error) {
        toast.error(error.message);
      } else {
        toast.error("공지사항 목록을 불러오는데 실패했습니다.");
      }
    } finally {
      setIsLoading(false);
    }
  };

  const handleBack = () => {
    navigate("/mypage/customer-service");
  };

  const handleNoticeClick = (noticeId: string) => {
    navigate(`/mypage/customer-service/notices/${noticeId}`);
  };

  const handlePageChange = (newPage: number) => {
    if (newPage >= 0 && newPage < noticesPage.totalPages) {
      setCurrentPage(newPage);
    }
  };

  return (
    <div className="min-h-screen bg-gray-50">
      <div className="max-w-md mx-auto px-4 py-8">
        {/* 헤더 */}
        <div className="flex items-center mb-8">
          <button
            onClick={handleBack}
            className="text-gray-700 hover:text-gray-900 mr-4"
          >
            <svg
              className="w-5 h-5"
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
          <h1 className="text-xl font-bold text-gray-900 flex-1 text-center">
            고객센터 - 공지사항
          </h1>
          <div className="w-9"></div>
        </div>

        {/* 공지사항 목록 */}
        <div className="bg-white rounded-lg p-4 shadow-sm border border-gray-200">
          {isLoading ? (
            <div className="text-center py-8 text-gray-500">로딩 중...</div>
          ) : noticesPage.content.length === 0 ? (
            <div className="text-center py-8 text-gray-500">
              등록된 공지사항이 없습니다.
            </div>
          ) : (
            <>
              <div className="space-y-3 mb-4">
                {noticesPage.content.map((notice) => (
                  <div
                    key={notice.id}
                    onClick={() => handleNoticeClick(notice.id)}
                    className="bg-gray-50 rounded-lg p-4 cursor-pointer hover:bg-gray-100 transition-colors"
                  >
                    <div className="flex items-center justify-between">
                      <div className="flex-1">
                        <div className="font-medium text-gray-900 mb-1">
                          {notice.title}
                        </div>
                        <div className="text-xs text-gray-500">
                          {formatDateSimple(notice.createdAt)}
                        </div>
                      </div>
                      <svg
                        className="w-5 h-5 text-gray-400 ml-4"
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
                    </div>
                  </div>
                ))}
              </div>

              {/* 페이징 컨트롤 */}
              {noticesPage.totalPages > 1 && (
                <div className="flex items-center justify-center gap-2 pt-4 border-t border-gray-200">
                  <button
                    onClick={() => handlePageChange(currentPage - 1)}
                    disabled={noticesPage.first}
                    className="bg-gray-100 rounded-lg p-2 text-gray-400 hover:text-gray-600 hover:bg-gray-200 disabled:opacity-30 disabled:cursor-not-allowed transition-colors"
                    aria-label="이전 페이지"
                  >
                    <svg
                      className="w-5 h-5"
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
                  <span className="text-sm text-gray-700">
                    {currentPage + 1}/{noticesPage.totalPages}
                  </span>
                  <button
                    onClick={() => handlePageChange(currentPage + 1)}
                    disabled={noticesPage.last}
                    className="bg-gray-100 rounded-lg p-2 text-gray-400 hover:text-gray-600 hover:bg-gray-200 disabled:opacity-30 disabled:cursor-not-allowed transition-colors"
                    aria-label="다음 페이지"
                  >
                    <svg
                      className="w-5 h-5"
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
        </div>
      </div>
    </div>
  );
};

export default NoticeListPage;

