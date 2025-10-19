import { apiClient } from "services/apiClient";
import type {
  PhoneModelCreateRequest,
  PhoneModelResponse,
  PhoneModelUpdateRequest,
} from "types/PhoneModelTypes";

const BASE_URL = "/phone/models";

export const getPhoneModels = async (): Promise<PhoneModelResponse[]> => {
  return apiClient.get<PhoneModelResponse[]>(BASE_URL);
};

export const createPhoneModel = async (
  payload: PhoneModelCreateRequest
): Promise<PhoneModelResponse> => {
  return apiClient.post<PhoneModelResponse>(BASE_URL, payload);
};

export const updatePhoneModel = async (
  id: string,
  payload: PhoneModelUpdateRequest
): Promise<PhoneModelResponse> => {
  return apiClient.put<PhoneModelResponse>(`${BASE_URL}/${id}`, payload);
};

export const deletePhoneModel = async (id: string): Promise<void> => {
  return apiClient.delete<void>(`${BASE_URL}/${id}`);
};