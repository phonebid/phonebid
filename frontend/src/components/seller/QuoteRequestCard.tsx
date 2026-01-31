import { QuoteListItem } from "types/QuoteTypes";
import { getPurchaseMethodDisplayName } from "utils/quoteUtils";
import { formatRelativeTime } from "utils/formatters";
import { Badge } from "components/ui/badge";
import { cn } from "utils/cn";

interface QuoteRequestCardProps {
  quote: QuoteListItem;
  onClick?: () => void;
  className?: string;
}

const getBrandIcon = (brand: string) => {
  if (brand === "APPLE") {
    return (
      <svg className="w-6 h-6 text-gray-900" viewBox="0 0 24 24" fill="currentColor">
        <path d="M17.05 20.28c-.98.95-2.05.88-3.08.4-1.09-.5-2.08-.48-3.24 0-1.44.62-2.2.44-3.06-.4C2.79 15.25 3.51 7.59 9.05 7.31c1.35.07 2.29.74 3.08.8 1.18-.24 2.31-.93 3.57-.84 1.51.12 2.65.72 3.4 1.8-3.12 1.87-2.38 5.98.48 7.13-.57 1.5-1.31 2.99-2.54 4.09l.01-.01zM12.03 7.25c-.15-2.23 1.66-4.07 3.74-4.25.29 2.58-2.34 4.5-3.74 4.25z" />
      </svg>
    );
  }
  if (brand === "SAMSUNG") {
    return (
      <div className="w-6 h-6 flex items-center justify-center font-bold text-xs bg-blue-100 text-blue-700 rounded">
        SS
      </div>
    );
  }
  return null;
};

const getCarrierIcon = (carrier: string) => {
  const carrierConfig: Record<string, { name: string; color: string; fillColor: string }> = {
    SKT: { name: "SKT", color: "text-gray-700", fillColor: "#2563eb" },
    KT: { name: "KT", color: "text-gray-700", fillColor: "#dc2626" },
    LGU: { name: "LG U+", color: "text-gray-700", fillColor: "#9333ea" },
  };
  
  const config = carrierConfig[carrier] || { name: carrier, color: "text-gray-700", fillColor: "#6b7280" };
  
  return (
    <div className="flex items-center gap-1.5">
      <svg 
        className="w-5 h-5" 
        viewBox="0 0 20 16" 
        fill="none"
      >
        {/* SIM 카드 본체 - 모서리가 잘린 직사각형 */}
        <path
          d="M2 3C2 2.44772 2.44772 2 3 2H15C15.5523 2 16 2.44772 16 3V13C16 13.5523 15.5523 14 15 14H3C2.44772 14 2 13.5523 2 13V3Z"
          fill={config.fillColor}
          fillOpacity="0.15"
          stroke={config.fillColor}
          strokeWidth="1.2"
        />
        {/* SIM 카드 잘린 모서리 (왼쪽 상단) */}
        <path
          d="M2 3L5.5 2H3C2.44772 2 2 2.44772 2 3Z"
          fill={config.fillColor}
        />
        {/* SIM 카드 내부 격자 패턴 (금속 접점) - 더 세밀하게 */}
        <g fill={config.fillColor} fillOpacity="0.7">
          <rect x="6" y="5" width="1.2" height="1.2" rx="0.2" />
          <rect x="8" y="5" width="1.2" height="1.2" rx="0.2" />
          <rect x="10" y="5" width="1.2" height="1.2" rx="0.2" />
          <rect x="12" y="5" width="1.2" height="1.2" rx="0.2" />
          <rect x="6" y="7" width="1.2" height="1.2" rx="0.2" />
          <rect x="8" y="7" width="1.2" height="1.2" rx="0.2" />
          <rect x="10" y="7" width="1.2" height="1.2" rx="0.2" />
          <rect x="12" y="7" width="1.2" height="1.2" rx="0.2" />
          <rect x="6" y="9" width="1.2" height="1.2" rx="0.2" />
          <rect x="8" y="9" width="1.2" height="1.2" rx="0.2" />
          <rect x="10" y="9" width="1.2" height="1.2" rx="0.2" />
          <rect x="12" y="9" width="1.2" height="1.2" rx="0.2" />
        </g>
      </svg>
      <span className={`text-sm font-medium ${config.color}`}>{config.name}</span>
    </div>
  );
};

const getStatusBadge = (status: string) => {
  const statusMap = {
    OPEN: { text: "진행중", className: "bg-gray-100 text-gray-700" },
    CLOSED: { text: "마감", className: "bg-gray-100 text-gray-700" },
    CONTRACTED: { text: "완료", className: "bg-gray-100 text-gray-700" },
  };
  return statusMap[status as keyof typeof statusMap] || statusMap.OPEN;
};

export const QuoteRequestCard: React.FC<QuoteRequestCardProps> = ({
  quote,
  onClick,
  className,
}) => {
  const statusBadge = getStatusBadge(quote.status);
  const brandIcon = getBrandIcon(quote.phoneModel.brand);

  return (
    <tr
      className={cn(
        "border-b hover:bg-muted/50 transition-colors cursor-pointer",
        className
      )}
      onClick={onClick}
    >
      <td className="px-4 py-3">
        <div className="flex items-center justify-center">{brandIcon}</div>
      </td>
      <td className="px-4 py-3">
        <div className="font-bold text-foreground">
          {quote.phoneModel.model}
        </div>
      </td>
      <td className="px-4 py-3">
        <div className="text-sm text-muted-foreground">
          {quote.storage?.displayLabel || "-"} /{" "}
          {quote.color?.displayLabel || "-"}
        </div>
      </td>
      <td className="px-4 py-3">
        {getCarrierIcon(quote.carrier)}
      </td>
      <td className="px-4 py-3">
        <div className="text-sm">
          {getPurchaseMethodDisplayName(quote.purchaseMethod)}
        </div>
      </td>
      <td className="px-4 py-3">
        <div className="text-sm">
          {quote.currentCarrier ? (
            <div className="flex items-center gap-1">
              <span className="text-gray-500">→</span>
              {getCarrierIcon(quote.currentCarrier)}
            </div>
          ) : (
            <span className="text-muted-foreground">-</span>
          )}
        </div>
      </td>
      <td className="px-4 py-3">
        <div className="text-sm text-muted-foreground">
          {formatRelativeTime(quote.createdAt)}
        </div>
      </td>
      <td className="px-4 py-3">
        <div className="text-sm font-bold text-blue-600">{quote.bidCount || 0}</div>
      </td>
      <td className="px-4 py-3">
        <Badge className={statusBadge.className}>{statusBadge.text}</Badge>
      </td>
    </tr>
  );
};

