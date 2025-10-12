import React from "react";
import { BrowserRouter as Router, Routes, Route } from "react-router-dom";

import Layout from "components/layout/Layout";
import LoginPage from "pages/LoginPage";
import SignupPage from "pages/SignupPage";
import AuthCallbackPage from "pages/AuthCallbackPage";
import ConfettiTestPage from "pages/ConfettiPage";
import AuctionListPage from "pages/AuctionListPage";
import WeeklyRankingPage from "pages/WeeklyRankingPage";
import QuoteCreatePage from "pages/QuoteCreatePage";
import QuoteCreateWizardPage from "@/pages/QuoteCreateWizardPage";

export const AppRouter: React.FC = () => {
  return (
    <Router>
      <Routes>
        {/* 전체화면 페이지들 (Layout 없음) */}
        <Route path="/login" element={<LoginPage />} />
        <Route path="/signup" element={<SignupPage />} />
        <Route path="/auth/callback" element={<AuthCallbackPage />} />
        <Route path="/confetti" element={<ConfettiTestPage />} />
        {/* <Route path="/auctions/create" element={<QuoteCreateWizardPage />} /> */}
        <Route path="/auctions/create" element={<QuoteCreatePage />} />

        {/* Layout이 포함된 일반 페이지들 */}
        <Route
          path="/*"
          element={
            <Layout>
              <Routes>
                <Route path="/" element={<WeeklyRankingPage />} />
                {/* 추후 추가될 라우트들 */}
                <Route path="/auctions" element={<AuctionListPage />} />
              </Routes>
            </Layout>
          }
        />
      </Routes>
    </Router>
  );
};

export default AppRouter;
