import { useEffect } from "react";
import { ToastContainer } from "react-toastify";
import "react-toastify/dist/ReactToastify.css";
import AppRouter from "app/router";

import { useAuthStore } from "store/authStore";
import SWRProvider from "app/providers/SWRProvider";

function App() {
  const { initializeAuth } = useAuthStore();

  // 앱 시작 시 인증 상태 복원
  useEffect(() => {
    initializeAuth();
  }, [initializeAuth]);

  return (
    <SWRProvider>
      <AppRouter />
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
    </SWRProvider>
  );
}

export default App;
