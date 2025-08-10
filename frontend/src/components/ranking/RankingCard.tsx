import React from "react";

export interface RankingCardProps {
  modelName: string;
  imageUrl?: string;
  originalPrice?: number; // 원가(선택)
  discountText?: string; // "최대 52만 원 할인" 등
  price?: number; // 표시 가격(선택)
  helperText?: string; // "월실사용 n 원부터" 등
}

function formatCurrencyKRW(value: number | undefined): string {
  if (typeof value !== "number") return "";
  return value.toLocaleString("ko-KR");
}

const RankingCard: React.FC<RankingCardProps> = ({
  modelName,
  imageUrl,
  originalPrice,
  discountText,
  price,
  helperText,
}) => {
  return (
    <div className="rounded-xl bg-gray-50 p-4">
      <div className="flex items-center gap-4">
        {/* 썸네일 */}
        <div className="shrink-0">
          <div className="rounded-xl size-24 bg-gray-100 overflow-hidden flex items-center justify-center">
            {imageUrl ? (
              <img
                src={imageUrl}
                alt={modelName}
                className="h-full w-full object-cover"
              />
            ) : (
              <span className="text-2xl">📱</span>
            )}
          </div>
        </div>

        {/* 텍스트 블록 */}
        <div className="flex-1 min-w-0">
          <div className="mt-0.5 text-[16px] text-foreground truncate">
            {modelName}
          </div>

          <div className="mt-1 flex items-center gap-2">
            {typeof originalPrice === "number" && (
              <span className="text-xs text-gray-400 line-through">
                {formatCurrencyKRW(originalPrice)}원
              </span>
            )}
            {discountText && (
              <span className="text-xs text-indigo-500 font-medium">
                {discountText}
              </span>
            )}
          </div>

          <div className="mt-1 text-[18px] font-extrabold text-foreground">
            {typeof price === "number" ? `${formatCurrencyKRW(price)}원` : ""}
          </div>

          {helperText && (
            <div className="mt-0.5 text-[12px] text-muted-foreground">
              {helperText}
            </div>
          )}
        </div>
      </div>
    </div>
  );
};

export default RankingCard;
