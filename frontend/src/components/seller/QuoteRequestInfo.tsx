import { QuoteDetail } from "types/QuoteTypes";
import {
  getCarrierDisplayName,
  getPurchaseMethodDisplayName,
  getActivationMethodDisplayName,
} from "utils/quoteUtils";
import { cn } from "@/utils/cn";

interface QuoteRequestInfoProps {
  quote: QuoteDetail;
  className?: string;
}

function formatRequestDateTime(iso: string): string {
  const d = new Date(iso);
  if (Number.isNaN(d.getTime())) return iso;
  const y = d.getFullYear();
  const m = String(d.getMonth() + 1).padStart(2, "0");
  const day = String(d.getDate()).padStart(2, "0");
  const h = String(d.getHours()).padStart(2, "0");
  const min = String(d.getMinutes()).padStart(2, "0");
  return `${y}-${m}-${day} ${h}:${min}`;
}

function DocumentIcon() {
  return (
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
        d="M9 12h6m-6 4h6m2 5H7a2 2 0 01-2-2V5a2 2 0 012-2h5.586a1 1 0 01.707.293l5.414 5.414a1 1 0 01.293.707V19a2 2 0 01-2 2z"
      />
    </svg>
  );
}

function PhonePlaceholderIcon() {
  return (
    <div className="w-14 h-14 rounded-lg bg-muted flex items-center justify-center shrink-0">
      <svg
        className="w-8 h-8 text-muted-foreground"
        fill="none"
        stroke="currentColor"
        viewBox="0 0 24 24"
      >
        <path
          strokeLinecap="round"
          strokeLinejoin="round"
          strokeWidth={1.5}
          d="M12 18h.01M8 21h8a2 2 0 002-2V5a2 2 0 00-2-2H8a2 2 0 00-2 2v14a2 2 0 002 2z"
        />
      </svg>
    </div>
  );
}

export const QuoteRequestInfo: React.FC<QuoteRequestInfoProps> = ({
  quote,
  className,
}) => {
  const specs = [quote.storage, quote.color].filter(Boolean).join(" / ");
  const discountLabel =
    quote.activationMethod === "COMMON_SUBSIDY"
      ? `${getActivationMethodDisplayName(quote.activationMethod)} (단말할인)`
      : quote.activationMethod === "SELECTIVE_SUBSIDY"
        ? `${getActivationMethodDisplayName(quote.activationMethod)} (요금할인 25%)`
        : getActivationMethodDisplayName(quote.activationMethod);
  const memo = quote.buyerMemo?.trim() || "구매자 메모가 없습니다.";
  const region = quote.preferredRegion?.trim() || "—";

  return (
    <div className={cn("space-y-3", className)}>
      <div className="flex items-center gap-2">
        <DocumentIcon />
        <h3 className="text-base font-semibold text-foreground">
          구매자 요청 정보
        </h3>
      </div>

      <div className="rounded-xl border border-border bg-card p-4 shadow-sm">
        <div className="flex gap-3">
          <PhonePlaceholderIcon />
          <div className="min-w-0 flex-1">
            <div className="flex items-start justify-between gap-2">
              <div>
                <p className="font-bold text-foreground leading-tight">
                  {quote.model}
                </p>
                {specs ? (
                  <p className="text-sm text-muted-foreground mt-1">{specs}</p>
                ) : null}
              </div>
              <span className="shrink-0 rounded-md bg-blue-100 px-2.5 py-0.5 text-xs font-semibold text-blue-700">
                {getCarrierDisplayName(quote.carrier)}
              </span>
            </div>
          </div>
        </div>

        <dl className="mt-3 space-y-2 border-t border-border pt-3">
          <div className="flex justify-between gap-4 text-sm">
            <dt className="text-muted-foreground shrink-0">할인 방식</dt>
            <dd className="font-bold text-foreground text-right">{discountLabel}</dd>
          </div>
          <div className="flex justify-between gap-4 text-sm">
            <dt className="text-muted-foreground shrink-0">개통 유형</dt>
            <dd className="font-bold text-foreground text-right">
              {getPurchaseMethodDisplayName(quote.purchaseMethod)}
            </dd>
          </div>
          <div className="flex justify-between gap-4 text-sm">
            <dt className="text-muted-foreground shrink-0">희망 지역</dt>
            <dd className="font-bold text-foreground text-right">{region}</dd>
          </div>
          <div className="flex justify-between gap-4 text-sm">
            <dt className="text-muted-foreground shrink-0">요청 일시</dt>
            <dd className="font-bold text-foreground text-right tabular-nums">
              {formatRequestDateTime(quote.createdAt)}
            </dd>
          </div>
        </dl>
      </div>

      <div className="rounded-xl border border-amber-200/80 bg-amber-50/90 p-4">
        <div className="flex items-center gap-2 mb-2">
          <svg
            className="w-5 h-5 text-amber-700"
            fill="none"
            stroke="currentColor"
            viewBox="0 0 24 24"
          >
            <path
              strokeLinecap="round"
              strokeLinejoin="round"
              strokeWidth={2}
              d="M8 12h.01M12 12h.01M16 12h.01M21 12c0 4.418-4.03 8-9 8a9.863 9.863 0 01-4.255-.949L3 20l1.395-3.72C3.512 15.042 3 13.574 3 12c0-4.418 4.03-8 9-8s9 3.582 9 8z"
            />
          </svg>
          <span className="text-sm font-semibold text-amber-900">
            구매자 메모
          </span>
        </div>
        <p className="text-sm text-amber-950/90 leading-relaxed">
          {memo || "—"}
        </p>
      </div>

      <div className="flex items-center justify-between rounded-xl border border-sky-200 bg-sky-50/90 px-4 py-3">
        <div className="flex items-center gap-2">
          <svg
            className="w-5 h-5 text-sky-600"
            fill="none"
            stroke="currentColor"
            viewBox="0 0 24 24"
          >
            <path
              strokeLinecap="round"
              strokeLinejoin="round"
              strokeWidth={2}
              d="M17 20h5v-2a3 3 0 00-5.356-1.857M17 20H7m10 0v-2c0-.656-.126-1.283-.356-1.857M7 20H2v-2a3 3 0 015.356-1.857M7 20v-2c0-.656.126-1.283.356-1.857m0 0a5.002 5.002 0 019.288 0M15 7a3 3 0 11-6 0 3 3 0 016 0zm6 3a2 2 0 11-4 0 2 2 0 014 0zM7 10a2 2 0 11-4 0 2 2 0 014 0z"
            />
          </svg>
          <span className="text-sm font-medium text-sky-900">
            현재 입찰 현황
          </span>
        </div>
        <span className="text-lg font-bold text-sky-900 tabular-nums">
          {quote.bidCount}명
        </span>
      </div>
    </div>
  );
};
