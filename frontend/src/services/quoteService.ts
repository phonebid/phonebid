import { apiClient } from "services/apiClient";
import type {
  CreateQuoteRequest,
  QuoteDetail,
  QuoteSummary,
} from "types/QuoteTypes";

export const createQuote = async (payload: CreateQuoteRequest) => {
  return await apiClient.post("/quotes", payload);
};

export const getLatestQuotes = async (): Promise<QuoteSummary[]> => {
  return await apiClient.get("/quotes/latest");
};

export const getQuoteDetail = async (quoteId: string): Promise<QuoteDetail> => {
  return await apiClient.get(`/quotes/${quoteId}`);
};
