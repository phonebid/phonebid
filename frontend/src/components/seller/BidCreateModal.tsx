import { useEffect, useRef, useState } from "react";
import { useBidForm } from "hooks/useBidForm";
import { BidCreateFormContent } from "components/seller/BidCreateFormContent";
import { getQuoteDetail } from "services/quoteService";
import { sellerService } from "services/sellerService";
import { logError } from "utils/errorUtils";
import { toast } from "react-toastify";
import { ApiErrorClass } from "types/ApiTypes";
import type { QuoteDetail } from "types/QuoteTypes";

function ArrowLeftIcon() {
  return (
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
  );
}

function XIcon() {
  return (
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
        d="M6 18L18 6M6 6l12 12"
      />
    </svg>
  );
}

function SendIcon() {
  return (
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
        d="M6 12L3.269 3.126A59.768 59.768 0 0121.485 12 59.77 59.77 0 013.27 20.876L5.999 12zm0 0h7.5"
      />
    </svg>
  );
}

function InfoIcon() {
  return (
    <svg
      className="w-4 h-4 flex-shrink-0"
      fill="none"
      stroke="currentColor"
      viewBox="0 0 24 24"
    >
      <path
        strokeLinecap="round"
        strokeLinejoin="round"
        strokeWidth={2}
        d="M13 16h-1v-4h-1m1-4h.01M21 12a9 9 0 11-18 0 9 9 0 0118 0z"
      />
    </svg>
  );
}

interface BidCreateModalProps {
  isOpen: boolean;
  quoteId: string;
  onClose: () => void;
  onSuccess?: () => void;
}

export function BidCreateModal({
  isOpen,
  quoteId,
  onClose,
  onSuccess,
}: BidCreateModalProps) {
  const [quote, setQuote] = useState<QuoteDetail | null>(null);
  const [isLoading, setIsLoading] = useState(false);
  const [isSubmitting, setIsSubmitting] = useState(false);
  const loadQuoteRequestIdRef = useRef(0);

  const bidForm = useBidForm(quote);

  useEffect(() => {
    if (!isOpen || !quoteId) {
      setQuote(null);
      return;
    }

    const requestId = ++loadQuoteRequestIdRef.current;
    let ignore = false;

    const loadQuote = async () => {
      try {
        if (ignore || requestId !== loadQuoteRequestIdRef.current) return;
        setIsLoading(true);
        setQuote(null);
        const quoteData = await getQuoteDetail(quoteId);
        if (ignore || requestId !== loadQuoteRequestIdRef.current) return;
        setQuote(quoteData);
      } catch (error) {
        if (ignore || requestId !== loadQuoteRequestIdRef.current) return;
        logError("견적 조회 실패:", error);
        toast.error("견적 정보를 불러오는데 실패했습니다.");
        onClose();
      } finally {
        if (!ignore && requestId === loadQuoteRequestIdRef.current) {
          setIsLoading(false);
        }
      }
    };

    loadQuote();
    return () => {
      ignore = true;
    };
  }, [isOpen, quoteId, onClose]);

  useEffect(() => {
    const handleEscape = (e: KeyboardEvent) => {
      if (e.key === "Escape") onClose();
    };
    if (isOpen) {
      document.addEventListener("keydown", handleEscape);
    }
    return () => document.removeEventListener("keydown", handleEscape);
  }, [isOpen, onClose]);

  const handleBack = () => {
    onClose();
  };

  const handleSubmit = async () => {
    if (!bidForm.validate()) {
      toast.error("입력한 정보를 확인해주세요.");
      return;
    }

    const bidRequest = bidForm.toBidCreateRequest();
    if (!bidRequest) {
      toast.error("견적 정보를 불러오지 못했습니다.");
      return;
    }

    try {
      setIsSubmitting(true);
      await sellerService.createBid(bidRequest);
      toast.success("견적이 성공적으로 전송되었습니다.");
      onClose();
      onSuccess?.();
    } catch (error) {
      logError("견적 전송 실패:", error);
      if (error instanceof ApiErrorClass && error.code === 400) {
        const details = error.details;
        if (details && typeof details === "object") {
          bidForm.applyServerErrors(details as Record<string, unknown>);
        }
        return;
      }

      const reason =
        error instanceof Error && error.message ? ` (${error.message})` : "";
      toast.error(`견적 전송에 실패했습니다${reason}`);
    } finally {
      setIsSubmitting(false);
    }
  };

  if (!isOpen) return null;

  return (
    <div
      className="fixed inset-0 bg-black/50 flex items-center justify-center z-50 p-4"
      onClick={onClose}
    >
      <div
        className="bg-white rounded-xl shadow-xl w-full max-w-6xl max-h-[92vh] flex flex-col"
        onClick={(e) => e.stopPropagation()}
      >
        <div className="flex-shrink-0 flex items-center justify-between h-14 px-4 border-b">
          <button
            type="button"
            onClick={handleBack}
            className="p-2 -ml-2 text-muted-foreground hover:text-foreground transition-colors rounded-md"
            aria-label="뒤로가기"
          >
            <ArrowLeftIcon />
          </button>
          <h2 className="text-base font-semibold">견적서 작성</h2>
          <button
            type="button"
            onClick={onClose}
            className="p-2 -mr-2 text-muted-foreground hover:text-foreground transition-colors rounded-md"
            aria-label="닫기"
          >
            <XIcon />
          </button>
        </div>

        <div className="flex-1 overflow-y-auto px-6 py-6">
          {isLoading ? (
            <div className="flex justify-center items-center py-16">
              <div className="flex flex-col items-center gap-2">
                <div className="animate-spin rounded-full h-8 w-8 border-b-2 border-primary-600" />
                <span className="text-sm text-muted-foreground">
                  견적 정보를 불러오는 중...
                </span>
              </div>
            </div>
          ) : quote ? (
            <BidCreateFormContent quote={quote} bidForm={bidForm} />
          ) : null}
        </div>

        {quote && !isLoading && (
        <div className="flex-shrink-0 flex flex-col gap-3 sm:flex-row sm:items-center sm:justify-between px-6 py-4 border-t bg-muted/30">
          <div className="flex items-start gap-2 text-sm text-muted-foreground">
            <InfoIcon />
            <span>입찰 후 구매자가 선택하면 알림이 발송됩니다</span>
          </div>
          <button
              type="button"
              onClick={handleSubmit}
              disabled={isSubmitting}
              className="flex items-center gap-2 px-6 py-3 bg-primary-600 hover:bg-primary-700 disabled:opacity-50 disabled:cursor-not-allowed text-white font-semibold rounded-lg transition-colors"
            >
              <SendIcon />
              견적 제안하기
            </button>
          </div>
        )}
      </div>
    </div>
  );
}
