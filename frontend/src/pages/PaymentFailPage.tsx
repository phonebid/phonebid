import { Link, useSearchParams } from "react-router-dom";

const PaymentFailPage: React.FC = () => {
  const [params] = useSearchParams();
  const code = params.get("code");
  const message = params.get("message");

  return (
    <div className="max-w-md mx-auto px-4 py-12 space-y-6 text-center">
      <h1 className="text-2xl font-bold text-red-500">결제 실패</h1>
      <div className="space-y-2 text-sm">
        {code && <p>오류 코드: {code}</p>}
        {message && <p>사유: {message}</p>}
        {!code && !message && <p>결제가 취소되었거나 실패했습니다.</p>}
      </div>
      <Link className="text-primary underline" to="/">
        홈으로 돌아가기
      </Link>
    </div>
  );
};

export default PaymentFailPage;

