import { Navigate, useParams } from "react-router-dom";

export function BidCreateRedirect() {
  const { quoteId } = useParams<{ quoteId: string }>();

  if (!quoteId) {
    return <Navigate to="/seller-center" replace />;
  }

  return (
    <Navigate
      to="/seller-center"
      replace
      state={{ openBidModal: quoteId }}
    />
  );
}
