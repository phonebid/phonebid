import { useState } from "react";
import { useNavigate } from "react-router-dom";
import { customerService } from "services/customerService";
import { toast } from "react-toastify";
import type { InquiryCreateRequestDto } from "types/CustomerServiceTypes";
import {
  InquiryCategory,
  INQUIRY_CATEGORY,
  INQUIRY_CATEGORY_LABELS,
} from "types/CustomerServiceTypes";
import { getErrorMessage, logError } from "utils/errorUtils";

const InquiryPage = () => {
  const navigate = useNavigate();
  const [isLoading, setIsLoading] = useState(false);

  const [formData, setFormData] = useState<InquiryCreateRequestDto>({
    category: INQUIRY_CATEGORY.PAYMENT,
    title: "",
    content: "",
  });

  const [errors, setErrors] = useState({
    category: "",
    title: "",
    content: "",
  });

  const validateTitle = (title: string): string => {
    if (!title.trim()) return "제목을 입력해주세요.";
    if (title.length > 200) return "제목은 200자 이하여야 합니다.";
    return "";
  };

  const validateContent = (content: string): string => {
    if (!content.trim()) return "내용을 입력해주세요.";
    return "";
  };

  const handleInputChange = (
    field: keyof InquiryCreateRequestDto,
    value: string | InquiryCategory
  ) => {
    setFormData((prev) => ({ ...prev, [field]: value }));

    let error = "";
    switch (field) {
      case "title":
        error = validateTitle(value as string);
        break;
      case "content":
        error = validateContent(value as string);
        break;
    }

    setErrors((prev) => ({ ...prev, [field]: error }));
  };

  const handleSubmit = async () => {
    const titleError = validateTitle(formData.title);
    const contentError = validateContent(formData.content);

    if (titleError || contentError) {
      setErrors({
        category: "",
        title: titleError,
        content: contentError,
      });
      return;
    }

    setIsLoading(true);
    try {
      await customerService.createInquiry({
        category: formData.category,
        title: formData.title.trim(),
        content: formData.content.trim(),
      });
      toast.success("문의가 등록되었습니다.");
      navigate("/mypage/customer-service/inquiries/my");
    } catch (error: unknown) {
      logError("문의 등록 실패:", error);
      const errorMessage = getErrorMessage(error) || "문의 등록에 실패했습니다.";
      toast.error(errorMessage);
    } finally {
      setIsLoading(false);
    }
  };

  const handleBack = () => {
    navigate("/mypage/customer-service");
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
            고객센터 - 1:1 문의
          </h1>
          <div className="w-9"></div>
        </div>

        {/* 문의 작성 폼 */}
        <div className="bg-white rounded-lg p-4 shadow-sm border border-gray-200">
          <div className="space-y-4">
            {/* 카테고리 */}
            <div>
              <label className="block text-sm font-medium text-gray-700 mb-1">
                카테고리
              </label>
              <select
                value={formData.category}
                onChange={(e) =>
                  handleInputChange("category", e.target.value as InquiryCategory)
                }
                className="w-full px-3 py-2 border border-gray-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-indigo-500"
              >
                {Object.values(INQUIRY_CATEGORY).map((category) => (
                  <option key={category} value={category}>
                    {INQUIRY_CATEGORY_LABELS[category]}
                  </option>
                ))}
              </select>
            </div>

            {/* 제목 */}
            <div>
              <label className="block text-sm font-medium text-gray-700 mb-1">
                제목
              </label>
              <input
                type="text"
                placeholder="문의 제목을 입력해주세요"
                value={formData.title}
                onChange={(e) => handleInputChange("title", e.target.value)}
                className={`w-full px-3 py-2 border rounded-lg focus:outline-none focus:ring-2 focus:ring-indigo-500 ${
                  errors.title ? "border-red-500" : "border-gray-300"
                }`}
              />
              {errors.title && (
                <p className="mt-1 text-xs text-red-600">{errors.title}</p>
              )}
            </div>

            {/* 내용 */}
            <div>
              <label className="block text-sm font-medium text-gray-700 mb-1">
                내용
              </label>
              <textarea
                placeholder="문의 내용을 입력해주세요"
                value={formData.content}
                onChange={(e) => handleInputChange("content", e.target.value)}
                rows={10}
                className={`w-full px-3 py-2 border rounded-lg focus:outline-none focus:ring-2 focus:ring-indigo-500 resize-none ${
                  errors.content ? "border-red-500" : "border-gray-300"
                }`}
              />
              {errors.content && (
                <p className="mt-1 text-xs text-red-600">{errors.content}</p>
              )}
            </div>

            {/* 제출 버튼 */}
            <button
              onClick={handleSubmit}
              disabled={isLoading}
              className="w-full px-4 py-2 bg-indigo-500 text-white rounded-lg font-medium hover:bg-indigo-600 transition-colors disabled:opacity-50 disabled:cursor-not-allowed"
            >
              {isLoading ? "등록 중..." : "문의 등록"}
            </button>
          </div>
        </div>
      </div>
    </div>
  );
};

export default InquiryPage;

