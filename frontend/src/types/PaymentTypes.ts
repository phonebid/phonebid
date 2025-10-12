export interface PortOnePaymentInitResponse {
  storeId: string;
  channelKey: string;
  paymentId: string;
  orderName: string;
  amount: number;
  buyerName: string;
  buyerEmail?: string;
  buyerPhone?: string;
  redirectUrl?: string;
  cancelUrl?: string;
}

export interface PortOnePaymentRequest {
  merchantUid: string;
  amount: number;
  productName: string;
  buyerName: string;
  buyerEmail?: string;
  buyerPhone?: string;
  returnUrl?: string;
  cancelUrl?: string;
}

export interface PortOnePaymentResult {
  paymentId?: string;
  code?: string;
  message?: string;
}

