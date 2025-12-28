import type { QuoteListItem, Carrier, PurchaseMethod, BidListItem, ActivationMethod } from "types/QuoteTypes";

export interface GroupedQuotes {
  date: string;
  count: number;
  quotes: QuoteListItem[];
}

export const groupQuotesByDate = (
  quotes: QuoteListItem[]
): GroupedQuotes[] => {
  const grouped = new Map<string, QuoteListItem[]>();

  quotes.forEach((quote) => {
    const date = formatDate(quote.createdAt);
    if (!grouped.has(date)) {
      grouped.set(date, []);
    }
    grouped.get(date)!.push(quote);
  });

  return Array.from(grouped.entries())
    .map(([date, quotes]) => ({
      date,
      count: quotes.length,
      quotes,
    }))
    .sort((a, b) => {
      const dateA = new Date(a.quotes[0]?.createdAt ?? 0);
      const dateB = new Date(b.quotes[0]?.createdAt ?? 0);
      return dateB.getTime() - dateA.getTime();
    });
};

export const formatDate = (dateString: string): string => {
  const date = new Date(dateString);
  const year = date.getFullYear();
  const month = date.getMonth() + 1;
  const day = date.getDate();
  return `${year}년 ${month}월 ${day}일`;
};

export const formatPrice = (price: number | null | undefined): string => {
  if (price === null || price === undefined) {
    return "-";
  }
  return new Intl.NumberFormat("ko-KR").format(price) + "원";
};

export const getCarrierDisplayName = (carrier: Carrier): string => {
  const carrierMap: Record<Carrier, string> = {
    SKT: "SKT",
    KT: "KT",
    LGU: "LG U+",
    SKT_ALD: "SKT 알뜰폰",
    KT_ALD: "KT 알뜰폰",
    LGU_ALD: "LG U+ 알뜰폰",
    ANY: "상관없음",
  };
  return carrierMap[carrier] || carrier;
};

export const getPurchaseMethodDisplayName = (
  purchaseMethod: PurchaseMethod
): string => {
  const methodMap: Record<PurchaseMethod, string> = {
    NUMBER_TRANSFER: "번호이동",
    DEVICE_CHANGE: "기기변경",
    NEW_SUBSCRIPTION: "신규가입",
    LOWEST_PRICE: "최저가",
    ANY: "상관없음",
  };
  return methodMap[purchaseMethod] || purchaseMethod;
};

export const calculateRemainingTime = (expiredAt: string): string => {
  const now = new Date().getTime();
  const expired = new Date(expiredAt).getTime();
  const diff = expired - now;

  if (diff <= 0) {
    return "00:00:00";
  }

  const hours = Math.floor(diff / (1000 * 60 * 60));
  const minutes = Math.floor((diff % (1000 * 60 * 60)) / (1000 * 60));
  const seconds = Math.floor((diff % (1000 * 60)) / 1000);

  return `${String(hours).padStart(2, "0")}:${String(minutes).padStart(2, "0")}:${String(seconds).padStart(2, "0")}`;
};

export const sortBidsByMaintenanceCost = (bids: BidListItem[]): BidListItem[] => {
  return [...bids].sort((a, b) => a.totalMaintenanceCost - b.totalMaintenanceCost);
};

export const getActivationMethodDisplayName = (
  activationMethod: ActivationMethod
): string => {
  const methodMap: Record<ActivationMethod, string> = {
    COMMON_SUBSIDY: "공시지원금",
    SELECTIVE_SUBSIDY: "선택약정",
    ANY: "상관없음",
  };
  return methodMap[activationMethod] || activationMethod;
};

