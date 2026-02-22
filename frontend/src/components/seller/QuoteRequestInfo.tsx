import { QuoteDetail } from "types/QuoteTypes";
import { Card, CardContent, CardHeader, CardTitle } from "components/ui/card";
import { getCarrierDisplayName, getPurchaseMethodDisplayName } from "utils/quoteUtils";
import { cn } from "@/utils/cn";

interface QuoteRequestInfoProps {
  quote: QuoteDetail;
  className?: string;
}

const InfoItem: React.FC<{ icon: React.ReactNode; label: string; value: string }> = ({
  icon,
  label,
  value,
}) => {
  return (
    <div className="flex items-center gap-3 py-2">
      <div className="flex-shrink-0">{icon}</div>
      <div className="flex-1">
        <div className="text-sm font-medium text-foreground">{label}</div>
        <div className="text-sm font-bold text-foreground mt-1">{value}</div>
      </div>
    </div>
  );
};

export const QuoteRequestInfo: React.FC<QuoteRequestInfoProps> = ({
  quote,
  className,
}) => {
  return (
    <div className={cn("space-y-4", className)}>
      <Card className="gap-0">
        <CardHeader className="pb-1">
          <CardTitle className="text-base">요청 정보</CardTitle>
        </CardHeader>
        <CardContent className="space-y-2 pt-0">
          <InfoItem
            icon={
              <div className="w-10 h-10 rounded-lg bg-blue-100 flex items-center justify-center">
                <svg
                  className="w-5 h-5 text-blue-600"
                  fill="none"
                  stroke="currentColor"
                  viewBox="0 0 24 24"
                >
                  <path
                    strokeLinecap="round"
                    strokeLinejoin="round"
                    strokeWidth={2}
                    d="M12 18h.01M8 21h8a2 2 0 002-2V5a2 2 0 00-2-2H8a2 2 0 00-2 2v14a2 2 0 002 2z"
                  />
                </svg>
              </div>
            }
            label="기기"
            value={quote.model}
          />
          <InfoItem
            icon={
              <div className="w-10 h-10 rounded-lg bg-purple-100 flex items-center justify-center">
                <svg
                  className="w-5 h-5 text-purple-600"
                  fill="none"
                  stroke="currentColor"
                  viewBox="0 0 24 24"
                >
                  <path
                    strokeLinecap="round"
                    strokeLinejoin="round"
                    strokeWidth={2}
                    d="M12 6.253v13m0-13C10.832 5.477 9.246 5 7.5 5S4.168 5.477 3 6.253v13C4.168 18.477 5.754 18 7.5 18s3.332.477 4.5 1.253m0-13C13.168 5.477 14.754 5 16.5 5c1.747 0 3.332.477 4.5 1.253v13C19.832 18.477 18.247 18 16.5 18c-1.746 0-3.332.477-4.5 1.253"
                  />
                  <circle cx="7.5" cy="12" r="1.5" fill="currentColor" />
                  <circle cx="16.5" cy="12" r="1.5" fill="currentColor" />
                  <circle cx="12" cy="8" r="1.5" fill="currentColor" />
                  <circle cx="12" cy="16" r="1.5" fill="currentColor" />
                </svg>
              </div>
            }
            label="색상"
            value={quote.color || "상관없음"}
          />
          <InfoItem
            icon={
              <div className="w-10 h-10 rounded-lg bg-green-100 flex items-center justify-center">
                <svg
                  className="w-5 h-5 text-green-600"
                  fill="none"
                  stroke="currentColor"
                  viewBox="0 0 24 24"
                >
                  <path
                    strokeLinecap="round"
                    strokeLinejoin="round"
                    strokeWidth={2}
                    d="M4 7v10c0 2.21 3.582 4 8 4s8-1.79 8-4V7M4 7c0 2.21 3.582 4 8 4s8-1.79 8-4M4 7c0-2.21 3.582-4 8-4s8 1.79 8 4"
                  />
                </svg>
              </div>
            }
            label="용량"
            value={quote.storage || "상관없음"}
          />
          <InfoItem
            icon={
              <div className="w-10 h-10 rounded-lg bg-orange-100 flex items-center justify-center">
                <svg
                  className="w-5 h-5 text-orange-600"
                  fill="none"
                  stroke="currentColor"
                  viewBox="0 0 24 24"
                >
                  <path
                    strokeLinecap="round"
                    strokeLinejoin="round"
                    strokeWidth={2}
                    d="M12 18h.01M8 21h8a2 2 0 002-2V5a2 2 0 00-2-2H8a2 2 0 00-2 2v14a2 2 0 002 2z"
                  />
                </svg>
              </div>
            }
            label="통신사"
            value={getCarrierDisplayName(quote.carrier)}
          />
          <InfoItem
            icon={
              <div className="w-10 h-10 rounded-lg bg-red-100 flex items-center justify-center">
                <svg
                  className="w-5 h-5 text-red-600"
                  fill="none"
                  stroke="currentColor"
                  viewBox="0 0 24 24"
                >
                  <path
                    strokeLinecap="round"
                    strokeLinejoin="round"
                    strokeWidth={2}
                    d="M4 4v5h.582m15.356 2A8.001 8.001 0 004.582 9m0 0H9m11 11v-5h-.581m0 0a8.003 8.003 0 01-15.357-2m15.357 2H15"
                  />
                </svg>
              </div>
            }
            label="개통 조건"
            value={getPurchaseMethodDisplayName(quote.purchaseMethod)}
          />
        </CardContent>
      </Card>

      <Card className="bg-blue-50 border-blue-200 gap-0">
        <CardHeader className="pb-1">
          <CardTitle className="text-base flex items-center gap-2">
            <div className="w-8 h-8 rounded-lg bg-blue-100 flex items-center justify-center">
              <svg
                className="w-5 h-5 text-blue-600"
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
            </div>
            구매자 요청사항
          </CardTitle>
        </CardHeader>
        <CardContent className="pt-0">
          <p className="text-sm text-muted-foreground">
            최대한 저렴하게 구매하고 싶습니다. 요금제는 5만원대 이하로 희망하며, 할부 24개월 원합니다.
          </p>
        </CardContent>
      </Card>
    </div>
  );
};

