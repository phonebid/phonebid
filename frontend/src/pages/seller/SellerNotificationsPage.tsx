import { NotificationsPage } from "pages/NotificationsPage";

const SELLER_NOTIFICATION_TYPES = [
  "QUOTE_CREATED",
  "BID_SELECTED",
  "LOWEST_PRICE_UPDATED",
  "CONTRACT_SIGNED",
  "PAYMENT_COMPLETED",
  "SELLER_APPROVED",
  "SELLER_REJECTED",
  "CHAT_MESSAGE_RECEIVED",
] as const;

export default function SellerNotificationsPage() {
  return (
    <NotificationsPage
      title="판매자 알림"
      typesFilter={[...SELLER_NOTIFICATION_TYPES]}
    />
  );
}
