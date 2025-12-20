import React from "react";
import { BrowserRouter as Router, Routes, Route } from "react-router-dom";

import Layout from "components/layout/Layout";
import LoginPage from "pages/LoginPage";
import SignupPage from "pages/SignupPage";
import AuthCallbackPage from "pages/AuthCallbackPage";
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

export const AppRouter: React.FC = () => {
  return (
    <Router>
      <Routes>
        {/* 전체화면 페이지들 (Layout 없음) */}
        <Route path="/login" element={<LoginPage />} />
        <Route path="/signup" element={<SignupPage />} />
        <Route path="/auth/callback" element={<AuthCallbackPage />} />
        <Route path="/confetti" element={<ConfettiTestPage />} />
        <Route path="/auctions/create" element={<QuoteCreateWizardPage />} />
        <Route path="/payment/success" element={<PaymentSuccessPage />} />
        <Route path="/payment/fail" element={<PaymentFailPage />} />
        <Route path="/chat" element={<ChatListPage />} />
        <Route path="/chat/:chatRoomId" element={<ChatRoomPage />} />
        <Route path="/mypage/profile" element={<ProfilePage />} />
        <Route path="/mypage/purchases" element={<PurchaseHistoryPage />} />
        <Route
          path="/mypage/purchases/:contractId"
          element={<PurchaseDetailPage />}
        />
        <Route path="/mypage/accounts" element={<AccountManagementPage />} />

        {/* Layout이 포함된 일반 페이지들 */}
        <Route
          path="/*"
          element={
            <Layout>
              <Routes>
                <Route path="/" element={<WeeklyRankingPage />} />
                {/* 추후 추가될 라우트들 */}
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
