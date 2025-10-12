import { useEffect, useState } from "react";
import { useSearchParams, Link } from "react-router-dom";
import { toast } from "react-toastify";

import { requestPortOnePaymentConfirm } from "services/paymentService";
import type { PortOnePaymentStatusResponse } from "types/PaymentTypes";

const PaymentSuccessPage: React.FC = () => {
  const [params] = useSearchParams();
  const paymentId = params.get("paymentId");
  const [status, setStatus] = useState<PortOnePaymentStatusResponse | null>(
    null
  );
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    const verify = async () => {
      if (!paymentId) {
        setError("paymentId가 전달되지 않았습니다.");
        return;
      }
      try {
        const result = await requestPortOnePaymentConfirm({ paymentId });
        setStatus(result);
      } catch (err) {
        console.error("결제 검증 실패", err);
        toast.error("결제 상태 확인에 실패했습니다.");
        setError("결제 상태 확인 중 오류가 발생했습니다.");
      }
    };
    verify();
  }, [paymentId]);

  return (
    <div className="max-w-md mx-auto px-4 py-12 space-y-6 text-center">
      <h1 className="text-2xl font-bold">결제 결과 확인</h1>
      {!paymentId && (
        <p className="text-red-500">paymentId가 전달되지 않았습니다.</p>
      )}
      {error && <p className="text-red-500">{error}</p>}
      {status && (
        <div className="space-y-2 text-sm">
          <p>결제 ID: {status.paymentId}</p>
          <p>상태: {status.status}</p>
          <p>
            금액: {status.amount?.toLocaleString()} {status.currency}
          </p>
        </div>
      )}
      <Link className="text-primary underline" to="/">
        홈으로 돌아가기
      </Link>
    </div>
  );
};

export default PaymentSuccessPage;

