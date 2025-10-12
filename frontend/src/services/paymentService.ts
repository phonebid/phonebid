import { apiClient } from "./apiClient";
import type {
  PortOnePaymentInitResponse,
  PortOnePaymentRequest,
} from "types/PaymentTypes";

const PORTONE_INIT_ENDPOINT = "/payments/portone/init";

export const requestPortOnePaymentInit = async (
  payload: PortOnePaymentRequest
): Promise<PortOnePaymentInitResponse> => {
  return apiClient.post<PortOnePaymentInitResponse>(
    PORTONE_INIT_ENDPOINT,
    payload
  );
};

