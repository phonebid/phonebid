import { useState, useMemo } from "react";
import useSWR from "swr";
import { sellerService } from "services/sellerService";
import type { QuoteListItem } from "types/QuoteTypes";
import type { Carrier } from "types/QuoteTypes";
import { realtimeDataConfig } from "services/swrConfig";

type FilterCarrier = Carrier | "ALL";
type FilterBrand = "APPLE" | "SAMSUNG" | "ALL";
type SortOption = "NEWEST" | "OLDEST" | "BID_COUNT";

export const useSellerDashboard = () => {
  const [selectedCarrier, setSelectedCarrier] = useState<FilterCarrier>("ALL");
  const [selectedBrand, setSelectedBrand] = useState<FilterBrand>("ALL");
  const [sortBy, setSortBy] = useState<SortOption>("NEWEST");
  const [currentPage, setCurrentPage] = useState(1);
  const itemsPerPage = 10;

  const { data: quotes, error, isLoading } = useSWR<QuoteListItem[]>(
    "/auction/quotes",
    async () => {
      return await sellerService.getSellerQuotes();
    },
    realtimeDataConfig
  );

  const filteredAndSortedQuotes = useMemo(() => {
    if (!quotes) return [];

    let filtered = [...quotes];

    if (selectedCarrier !== "ALL") {
      filtered = filtered.filter((quote) => quote.carrier === selectedCarrier);
    }

    if (selectedBrand !== "ALL") {
      filtered = filtered.filter(
        (quote) => quote.phoneModel.brand === selectedBrand
      );
    }

    filtered.sort((a, b) => {
      switch (sortBy) {
        case "NEWEST":
          return (
            new Date(b.createdAt).getTime() - new Date(a.createdAt).getTime()
          );
        case "OLDEST":
          return (
            new Date(a.createdAt).getTime() - new Date(b.createdAt).getTime()
          );
        case "BID_COUNT":
          return (b.bidCount || 0) - (a.bidCount || 0);
        default:
          return 0;
      }
    });

    return filtered;
  }, [quotes, selectedCarrier, selectedBrand, sortBy]);

  const paginatedQuotes = useMemo(() => {
    const startIndex = (currentPage - 1) * itemsPerPage;
    const endIndex = startIndex + itemsPerPage;
    return filteredAndSortedQuotes.slice(startIndex, endIndex);
  }, [filteredAndSortedQuotes, currentPage]);

  const totalPages = Math.ceil(filteredAndSortedQuotes.length / itemsPerPage);

  return {
    quotes: paginatedQuotes,
    filteredQuotes: filteredAndSortedQuotes,
    isLoading,
    error,
    selectedCarrier,
    selectedBrand,
    sortBy,
    currentPage,
    totalPages,
    itemsPerPage,
    setSelectedCarrier,
    setSelectedBrand,
    setSortBy,
    setCurrentPage,
  };
};

