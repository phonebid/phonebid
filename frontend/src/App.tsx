import { useEffect } from "react";
import { ToastContainer } from "react-toastify";
import "react-toastify/dist/ReactToastify.css";
import AppRouter from "app/router";

import { useAuthStore } from "store/authStore";
import SWRProvider from "app/providers/SWRProvider";

function App() {
  // 앱 시작 시 인증 상태 복원 (마운트 시 한 번만 실행)
  useEffect(() => {
    const initialize = async () => {
      try {
        await useAuthStore.getState().initializeAuth();
      } catch (error) {
        // initializeAuth 내부에서 이미 에러 처리를 하므로 여기서는 로깅만
        console.error("앱 초기화 중 인증 확인 실패:", error);
      }
    };
    
    initialize();
  }, []);

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
