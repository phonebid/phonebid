import { useEffect } from "react";
import { BrowserRouter as Router, Routes, Route } from "react-router-dom";
import { ToastContainer } from "react-toastify";
import "react-toastify/dist/ReactToastify.css";

import Layout from "components/layout/Layout";
import LoginPage from "pages/LoginPage";
import SignupPage from "pages/SignupPage";
import AuthCallbackPage from "pages/AuthCallbackPage";
import ConfettiTestPage from "@/pages/ConfettiPage";
import { useAuthStore } from "store/authStore";
import AuctionListPage from "./pages/AuctionListPage";
import WeeklyRankingPage from "./pages/WeeklyRankingPage";
import QuoteCreatePage from "./pages/QuoteCreatePage";
import SWRProvider from "app/providers/SWRProvider";

function App() {
  const { initializeAuth } = useAuthStore();

  // 앱 시작 시 인증 상태 복원
  useEffect(() => {
    initializeAuth();
  }, [initializeAuth]);

  return (
    <SWRProvider>
      <Router>
        <Routes>
          {/* 전체화면 페이지들 (Layout 없음) */}
          <Route path="/login" element={<LoginPage />} />
          <Route path="/signup" element={<SignupPage />} />
          <Route path="/auth/callback" element={<AuthCallbackPage />} />
          <Route path="/confetti" element={<ConfettiTestPage />} />
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

                  {/* <Route path="/register" element={<RegisterPage />} /> */}
                  {/* <Route path="/profile" element={<ProfilePage />} /> */}
                </Routes>
              </Layout>
            }
          />
        </Routes>

        <ToastContainer
          position="top-right"
          autoClose={3000}
          hideProgressBar={false}
          newestOnTop={false}
          closeOnClick
          rtl={false}
          pauseOnFocusLoss
          draggable
          pauseOnHover
          theme="light"
        />
      </Router>
    </SWRProvider>
  );
}

export default App;
