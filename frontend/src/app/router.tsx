import React from "react";
import { BrowserRouter as Router, Routes, Route } from "react-router-dom";

import Layout from "components/layout/Layout";
import { ProtectedRoute } from "components/auth/ProtectedRoute";
import { useAuthStore } from "store/authStore";
import LoginPage from "pages/LoginPage";
import SignupPage from "pages/SignupPage";
import AuthCallbackPage from "pages/AuthCallbackPage";
import SellerLoginPage from "pages/seller/SellerLoginPage";
import SellerSignupPage from "pages/seller/SellerSignupPage";
import SellerDashboardPage from "pages/seller/SellerDashboardPage";
import SellerBidCreatePage from "pages/seller/SellerBidCreatePage";
import ConfettiTestPage from "pages/ConfettiPage";
import AuctionListPage from "pages/AuctionListPage";
import WeeklyRankingPage from "pages/WeeklyRankingPage";
import PhoneModelManagePage from "@/pages/admin/PhoneModelManagePage";
import PaymentSuccessPage from "pages/PaymentSuccessPage";
import PaymentFailPage from "pages/PaymentFailPage";
import QuoteCreateWizardPage from "@/pages/QuoteCreateWizardPage";
import ChatListPage from "pages/ChatListPage";
import ChatRoomPage from "pages/ChatRoomPage";
import MyPage from "pages/MyPage";
import ProfilePage from "pages/ProfilePage";
import PurchaseHistoryPage from "pages/PurchaseHistoryPage";
import PurchaseDetailPage from "pages/PurchaseDetailPage";
import AccountManagementPage from "pages/AccountManagementPage";
import CustomerServicePage from "pages/CustomerServicePage";
import InquiryPage from "pages/InquiryPage";
import MyInquiriesPage from "pages/MyInquiriesPage";
import InquiryDetailPage from "pages/InquiryDetailPage";
import NoticeListPage from "pages/NoticeListPage";
import NoticeDetailPage from "pages/NoticeDetailPage";
import FAQListPage from "pages/FAQListPage";
import FAQDetailPage from "pages/FAQDetailPage";
import MyQuotesPage from "pages/MyQuotesPage";
import QuoteDetailPage from "pages/QuoteDetailPage";
import BidDetailPage from "pages/BidDetailPage";
import DeliveryAddressPage from "pages/DeliveryAddressPage";
import DeliveryAddressListPage from "pages/DeliveryAddressListPage";
import PurchaseCompletePage from "pages/PurchaseCompletePage";
import LandingPage from "pages/LandingPage";
import { NotificationsPage } from "pages/NotificationsPage";

const RootPage: React.FC = () => {
  const { isAuthenticated, isInitializing } = useAuthStore();

  if (isInitializing) {
    return (
      <div className="min-h-screen bg-background flex items-center justify-center">
        <div className="text-muted-foreground">로딩 중...</div>
      </div>
    );
  }

  if (!isAuthenticated) {
    return <LandingPage />;
  }

  return (
    <Layout>
      <WeeklyRankingPage />
    </Layout>
  );
};

export const AppRouter: React.FC = () => {
  return (
    <Router>
      <Routes>
        {/* 전체화면 페이지들 (Layout 없음) */}
        <Route path="/login" element={<LoginPage />} />
        <Route path="/seller/login" element={<SellerLoginPage />} />
        <Route path="/seller/signup" element={<SellerSignupPage />} />
        <Route
          path="/seller-center"
          element={
            <ProtectedRoute
              requiredRole={["SELLER", "ADMIN"]}
              redirectTo="/seller/login"
            >
              <SellerDashboardPage />
            </ProtectedRoute>
          }
        />
        <Route
          path="/seller-center/quotes/:quoteId/bid"
          element={
            <ProtectedRoute
              requiredRole={["SELLER", "ADMIN"]}
              redirectTo="/seller/login"
            >
              <SellerBidCreatePage />
            </ProtectedRoute>
          }
        />
        <Route path="/signup" element={<SignupPage />} />
        <Route path="/auth/callback" element={<AuthCallbackPage />} />
        <Route path="/confetti" element={<ConfettiTestPage />} />
        <Route path="/auctions/create" element={<QuoteCreateWizardPage />} />
        <Route path="/payment/success" element={<PaymentSuccessPage />} />
        <Route path="/payment/fail" element={<PaymentFailPage />} />
        <Route path="/chat" element={<ChatListPage />} />
        <Route path="/chat/:chatRoomId" element={<ChatRoomPage />} />
        
        {/* 알림 페이지 */}
        <Route
          path="/notifications"
          element={
            <ProtectedRoute requiredRole={["USER", "SELLER", "ADMIN"]}>
              <NotificationsPage />
            </ProtectedRoute>
          }
        />
        
        <Route path="/mypage/profile" element={<ProfilePage />} />
        <Route path="/mypage/purchases" element={<PurchaseHistoryPage />} />
        <Route
          path="/mypage/purchases/:contractId"
          element={<PurchaseDetailPage />}
        />
        <Route path="/mypage/accounts" element={<AccountManagementPage />} />
        <Route path="/mypage/addresses" element={<DeliveryAddressListPage />} />
        <Route path="/mypage/quotes" element={<MyQuotesPage />} />
        <Route path="/mypage/quotes/:quoteId" element={<QuoteDetailPage />} />
        <Route
          path="/mypage/quotes/:quoteId/bids/:bidId"
          element={<BidDetailPage />}
        />
        <Route
          path="/mypage/quotes/:quoteId/bids/:bidId/delivery"
          element={<DeliveryAddressPage />}
        />
        <Route
          path="/mypage/quotes/:quoteId/bids/:bidId/complete"
          element={<PurchaseCompletePage />}
        />
        <Route
          path="/mypage/customer-service"
          element={<CustomerServicePage />}
        />
        <Route
          path="/mypage/customer-service/inquiry"
          element={<InquiryPage />}
        />
        <Route
          path="/mypage/customer-service/inquiries/my"
          element={<MyInquiriesPage />}
        />
        <Route
          path="/mypage/customer-service/inquiries/:inquiryId"
          element={<InquiryDetailPage />}
        />
        <Route
          path="/mypage/customer-service/notices"
          element={<NoticeListPage />}
        />
        <Route
          path="/mypage/customer-service/notices/:noticeId"
          element={<NoticeDetailPage />}
        />
        <Route path="/mypage/customer-service/faqs" element={<FAQListPage />} />
        <Route
          path="/mypage/customer-service/faqs/:faqId"
          element={<FAQDetailPage />}
        />

        {/* 루트: 비로그인 시 랜딩페이지, 로그인 시 Layout+WeeklyRankingPage */}
        <Route path="/" element={<RootPage />} />
        <Route path="/landing" element={<LandingPage />} />

        {/* Layout이 포함된 일반 페이지들 */}
        <Route
          path="/*"
          element={
            <Layout>
              <Routes>
                <Route path="/auctions" element={<AuctionListPage />} />
                <Route
                  path="/admin/phone-models"
                  element={<PhoneModelManagePage />}
                />
                <Route path="/mypage" element={<MyPage />} />
              </Routes>
            </Layout>
          }
        />
      </Routes>
    </Router>
  );
};

export default AppRouter;
