import { apiClient } from "services/apiClient";
import type {
  QuoteCreateRequestDto,
  QuoteDetail,
  QuoteSummary,
} from "types/QuoteTypes";

const BASE_URL = "/auction/quotes";

export const createQuote = async (payload: QuoteCreateRequestDto) => {
  return await apiClient.post(BASE_URL, payload);
};

export const getLatestQuotes = async (): Promise<QuoteSummary[]> => {
  return await apiClient.get(`${BASE_URL}/latest`);
};

export const getQuoteDetail = async (quoteId: string): Promise<QuoteDetail> => {
  return await apiClient.get(`${BASE_URL}/${quoteId}`);
};
