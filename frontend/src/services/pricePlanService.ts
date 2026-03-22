import { apiClient } from "./apiClient";
import type { PricePlan, PricePlanCategory } from "types/SellerTypes";

const BASE_URL = "/auction/price-plans";

export interface PricePlanFilters {
  carrier?: "SKT" | "KT" | "LGU";
  category?: PricePlanCategory;
}

export async function getActivePricePlans(filters?: PricePlanFilters): Promise<PricePlan[]> {
  const params = new URLSearchParams();
  if (filters?.carrier) params.append("carrier", filters.carrier);
  if (filters?.category) params.append("category", filters.category);

  const queryString = params.toString();
  const url = queryString ? `${BASE_URL}?${queryString}` : BASE_URL;

  return await apiClient.get<PricePlan[]>(url);
}

export async function getPricePlanById(id: string): Promise<PricePlan> {
  return await apiClient.get<PricePlan>(`${BASE_URL}/${id}`);
}
