import { useCallback, useState } from "react";
import { toast } from "react-toastify";

import { Button } from "components/ui/button";
import { requestPortOnePaymentInit } from "services/paymentService";
import { loadPortOneSdk } from "utils/portoneLoader";
import type {
  PortOnePaymentRequest,
  PortOnePaymentResult,
} from "types/PaymentTypes";

interface PortOnePaymentButtonProps {
  requestPayload: PortOnePaymentRequest;
  onSuccess?: (result: PortOnePaymentResult) => void;
  onFailure?: (result: PortOnePaymentResult) => void;
  label?: string;
}

export const PortOnePaymentButton: React.FC<PortOnePaymentButtonProps> = ({
  requestPayload,
  onSuccess,
  onFailure,
  label = "결제하기",
}) => {
  const [isProcessing, setProcessing] = useState(false);

  const handleClick = useCallback(async () => {
    if (isProcessing) {
      return;
    }

    setProcessing(true);

    try {
      const initData = await requestPortOnePaymentInit(requestPayload);
      const PortOne = await loadPortOneSdk();

      const response = await PortOne.requestPayment({
        storeId: initData.storeId,
        channelKey: initData.channelKey,
        paymentId: initData.paymentId,
        orderName: initData.orderName,
        payMethod: "CARD",
        totalAmount: initData.amount,
        currency: "CURRENCY_KRW",
        redirectUrl: initData.redirectUrl,
        cancelUrl: initData.cancelUrl,
        customer: {
          fullName: initData.buyerName,
          phoneNumber: initData.buyerPhone,
          email: initData.buyerEmail,
        },
      });

      if (response.code) {
        const failureResult: PortOnePaymentResult = {
          paymentId: response.paymentId,
          code: response.code,
          message: response.message,
        };
        toast.error(response.message ?? "결제에 실패했습니다.");
        onFailure?.(failureResult);
        return;
      }

      const successResult: PortOnePaymentResult = {
        paymentId: response.paymentId,
      };
      toast.success("결제가 요청되었습니다.");
      onSuccess?.(successResult);
    } catch (error) {
      console.error("PortOne 결제 호출 중 오류", error);
      toast.error("결제를 시작하지 못했습니다.");
      onFailure?.({ message: "SDK 호출 실패" });
    } finally {
      setProcessing(false);
    }
  }, [isProcessing, onFailure, onSuccess, requestPayload]);

  return (
    <Button onClick={handleClick} disabled={isProcessing}>
      {isProcessing ? "결제 준비중..." : label}
    </Button>
  );
};

