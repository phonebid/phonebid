import { Carrier } from "types/QuoteTypes";
import { getCarrierDisplayName } from "utils/quoteUtils";
import { Card, CardContent } from "components/ui/card";
import { cn } from "@/utils/cn";

interface QuoteFilterBarProps {
  selectedCarrier: Carrier | "ALL";
  selectedBrand: string | "ALL";
  sortBy: "NEWEST" | "OLDEST" | "BID_COUNT";
  onCarrierChange: (carrier: Carrier | "ALL") => void;
  onBrandChange: (brand: string | "ALL") => void;
  onSortChange: (sort: "NEWEST" | "OLDEST" | "BID_COUNT") => void;
  className?: string;
}

const CARRIERS: (Carrier | "ALL")[] = ["ALL", "SKT", "KT", "LGU"];
const BRANDS = ["ALL", "APPLE", "SAMSUNG"];
const SORT_OPTIONS = [
  { value: "NEWEST", label: "최신순" },
  { value: "OLDEST", label: "오래된순" },
  { value: "BID_COUNT", label: "입찰 많은순" },
] as const;

export const QuoteFilterBar: React.FC<QuoteFilterBarProps> = ({
  selectedCarrier,
  selectedBrand,
  sortBy,
  onCarrierChange,
  onBrandChange,
  onSortChange,
  className,
}) => {
  return (
    <Card className={cn("mb-4", className)}>
      <CardContent className="py-2 px-4">
        <div className="flex flex-wrap items-center gap-3">
          <div className="flex items-center gap-2">
            <svg
              className="w-4 h-4 text-foreground"
              fill="none"
              stroke="currentColor"
              viewBox="0 0 24 24"
            >
              <path
                strokeLinecap="round"
                strokeLinejoin="round"
                strokeWidth={2}
                d="M3 4a1 1 0 011-1h16a1 1 0 011 1v2.586a1 1 0 01-.293.707l-6.414 6.414a1 1 0 00-.293.707V17l-4 4v-6.586a1 1 0 00-.293-.707L3.293 7.293A1 1 0 013 6.586V4z"
              />
            </svg>
            <span className="text-sm font-medium text-foreground">필터</span>
          </div>
          <select
            value={selectedCarrier}
            onChange={(e) =>
              onCarrierChange(e.target.value as Carrier | "ALL")
            }
            className="border border-input rounded-md h-10 px-3 pr-8 bg-background text-sm focus:outline-none focus:ring-2 focus:ring-ring appearance-none bg-[url('data:image/svg+xml;charset=UTF-8,%3Csvg%20xmlns%3D%22http%3A%2F%2Fwww.w3.org%2F2000%2Fsvg%22%20width%3D%2212%22%20height%3D%2212%22%20viewBox%3D%220%200%2012%2012%22%3E%3Cpath%20fill%3D%22%23666%22%20d%3D%22M6%209l3%203%203-3H6z%22%2F%3E%3C%2Fsvg%3E')] bg-no-repeat bg-[length:12px_12px] bg-[right_8px_center]"
          >
            <option value="ALL">모든 통신사</option>
            {CARRIERS.filter((c) => c !== "ALL").map((carrier) => (
              <option key={carrier} value={carrier}>
                {getCarrierDisplayName(carrier)}
              </option>
            ))}
          </select>
          <select
            value={selectedBrand}
            onChange={(e) => onBrandChange(e.target.value)}
            className="border border-input rounded-md h-10 px-3 pr-8 bg-background text-sm focus:outline-none focus:ring-2 focus:ring-ring appearance-none bg-[url('data:image/svg+xml;charset=UTF-8,%3Csvg%20xmlns%3D%22http%3A%2F%2Fwww.w3.org%2F2000%2Fsvg%22%20width%3D%2212%22%20height%3D%2212%22%20viewBox%3D%220%200%2012%2012%22%3E%3Cpath%20fill%3D%22%23666%22%20d%3D%22M6%209l3%203%203-3H6z%22%2F%3E%3C%2Fsvg%3E')] bg-no-repeat bg-[length:12px_12px] bg-[right_8px_center]"
          >
            <option value="ALL">모든 기종</option>
            {BRANDS.filter((b) => b !== "ALL").map((brand) => (
              <option key={brand} value={brand}>
                {brand === "APPLE" ? "애플" : brand === "SAMSUNG" ? "삼성" : brand}
              </option>
            ))}
          </select>
          <div className="flex items-center gap-2 ml-auto">
            <label className="text-sm font-medium text-foreground">정렬:</label>
            <select
              value={sortBy}
              onChange={(e) =>
                onSortChange(e.target.value as "NEWEST" | "OLDEST" | "BID_COUNT")
              }
              className="border border-input rounded-md h-10 px-3 pr-8 bg-background text-sm focus:outline-none focus:ring-2 focus:ring-ring appearance-none bg-[url('data:image/svg+xml;charset=UTF-8,%3Csvg%20xmlns%3D%22http%3A%2F%2Fwww.w3.org%2F2000%2Fsvg%22%20width%3D%2212%22%20height%3D%2212%22%20viewBox%3D%220%200%2012%2012%22%3E%3Cpath%20fill%3D%22%23666%22%20d%3D%22M6%209l3%203%203-3H6z%22%2F%3E%3C%2Fsvg%3E')] bg-no-repeat bg-[length:12px_12px] bg-[right_8px_center]"
            >
              {SORT_OPTIONS.map((option) => (
                <option key={option.value} value={option.value}>
                  {option.label}
                </option>
              ))}
            </select>
          </div>
        </div>
      </CardContent>
    </Card>
  );
};

