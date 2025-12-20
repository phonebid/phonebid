import { useEffect, useState } from "react";
import { useNavigate, useParams } from "react-router-dom";
import { customerService } from "services/customerService";
import { toast } from "react-toastify";
import type { NoticeDetailResponseDto } from "types/CustomerServiceTypes";
import { formatDateSimple } from "utils/formatters";

const NoticeDetailPage = () => {
  const navigate = useNavigate();
  const { noticeId } = useParams<{ noticeId: string }>();
  const [isLoading, setIsLoading] = useState(true);
  const [notice, setNotice] = useState<NoticeDetailResponseDto | null>(null);

  useEffect(() => {
    if (noticeId) {
      loadNoticeDetail();
    }
  }, [noticeId]);

  const loadNoticeDetail = async () => {
    if (!noticeId) return;

    try {
      setIsLoading(true);
      const data = await customerService.getNoticeDetail(noticeId);
      setNotice(data);
    } catch (error: any) {
      console.error("공지사항 상세 조회 실패:", error);
      toast.error("공지사항 상세 정보를 불러오는데 실패했습니다.");
      navigate("/mypage/customer-service/notices");
    } finally {
      setIsLoading(false);
    }
  };

  const handleBack = () => {
    navigate("/mypage/customer-service/notices");
  };

  if (isLoading) {
    return (
      <div className="min-h-screen bg-gray-50 flex items-center justify-center">
        <div className="text-gray-500">로딩 중...</div>
      </div>
    );
  }

  if (!notice) {
    return null;
  }

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

        {/* 공지사항 상세 */}
        <div className="bg-gray-50 rounded-lg p-4 shadow-sm border border-gray-200">
          <h2 className="text-lg font-semibold text-gray-900 mb-4">
            {notice.title}
          </h2>

          <div className="text-xs text-gray-500 mb-4 space-y-1">
            <div>게시일 {formatDateSimple(notice.createdAt)}</div>
            {notice.adminNickname && (
              <div>작성자 {notice.adminNickname}</div>
            )}
          </div>

          <div className="text-sm text-gray-700 whitespace-pre-wrap">
            {notice.content}
          </div>
        </div>
      </div>
    </div>
  );
};

export default NoticeDetailPage;

