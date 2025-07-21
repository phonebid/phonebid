import { BrowserRouter as Router, Routes, Route } from "react-router-dom";
import { ToastContainer } from "react-toastify";
import "react-toastify/dist/ReactToastify.css";

import Layout from "components/layout/Layout";
import HomePage from "pages/HomePage";

function App() {
  return (
    <Router>
      <Layout>
        <Routes>
          <Route path="/" element={<HomePage />} />
          {/* 추후 추가될 라우트들 */}
          {/* <Route path="/auctions" element={<AuctionListPage />} /> */}
          {/* <Route path="/auction/:id" element={<AuctionDetailPage />} /> */}
          {/* <Route path="/login" element={<LoginPage />} /> */}
          {/* <Route path="/register" element={<RegisterPage />} /> */}
          {/* <Route path="/profile" element={<ProfilePage />} /> */}
        </Routes>
      </Layout>

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
  );
}

export default App;
