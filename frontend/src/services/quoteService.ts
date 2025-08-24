import { apiClient } from "lib/apiClient";
import type { CreateQuoteRequest } from "types/QuoteTypes";

export const createQuote = async (payload: CreateQuoteRequest) => {
  return await apiClient.post("/quotes", payload);
};

