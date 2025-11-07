import { apiClient } from "./apiClient";
import type {
  PortOnePaymentInitResponse,
  PortOnePaymentRequest,
  PortOnePaymentConfirmRequest,
  PortOnePaymentStatusResponse,
} from "types/PaymentTypes";

const PORTONE_INIT_ENDPOINT = "/payments/portone/init";
const PORTONE_CONFIRM_ENDPOINT = "/payments/portone/confirm";

export const requestPortOnePaymentInit = async (
  payload: PortOnePaymentRequest
): Promise<PortOnePaymentInitResponse> => {
  return apiClient.post<PortOnePaymentInitResponse>(
    PORTONE_INIT_ENDPOINT,
    payload
  );
};

export const requestPortOnePaymentConfirm = async (
  payload: PortOnePaymentConfirmRequest
): Promise<PortOnePaymentStatusResponse> => {
  return apiClient.post<PortOnePaymentStatusResponse>(
    PORTONE_CONFIRM_ENDPOINT,
    payload
  );
};

