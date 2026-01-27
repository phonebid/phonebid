import { useEffect, useState } from "react";
import { Navigate, useLocation } from "react-router-dom";
import { useAuthStore } from "store/authStore";

interface ProtectedRouteProps {
  children: React.ReactElement;
  requiredRole?: string | string[];
  redirectTo?: string;
}

/**
 * 인증 및 역할 기반 라우트 보호 컴포넌트
 * @param children - 보호할 컴포넌트
 * @param requiredRole - 필요한 역할 (단일 문자열 또는 문자열 배열, 예: "SELLER", ["SELLER", "ADMIN"])
 * @param redirectTo - 인증 실패 시 리다이렉트할 경로 (기본값: "/login")
 */
export const ProtectedRoute: React.FC<ProtectedRouteProps> = ({
  children,
  requiredRole,
  redirectTo = "/login",
}) => {
  const { isAuthenticated, user, checkAuth } = useAuthStore();
  const location = useLocation();
  const [isChecking, setIsChecking] = useState(true);

  useEffect(() => {
    const verifyAuth = async () => {
      try {
        // 인증 상태가 없으면 확인 시도
        if (!isAuthenticated) {
          await checkAuth();
        }
      } catch (error) {
        // 인증 확인 실패 시 로그인 페이지로 리다이렉트
        console.error("인증 확인 실패:", error);
      } finally {
        setIsChecking(false);
      }
    };

    verifyAuth();
  }, [isAuthenticated, checkAuth]);

  // 인증 확인 중 로딩 상태
  if (isChecking) {
    return (
      <div className="min-h-screen bg-background flex items-center justify-center">
        <div className="text-muted-foreground">인증 확인 중...</div>
      </div>
    );
  }

  // 인증되지 않은 경우 로그인 페이지로 리다이렉트
  if (!isAuthenticated) {
    return <Navigate to={redirectTo} state={{ from: location }} replace />;
  }

  // 역할이 필요한 경우 역할 체크
  if (requiredRole) {
    const allowedRoles = Array.isArray(requiredRole) ? requiredRole : [requiredRole];
    
    // user.role이 없으면 즉시 리다이렉트
    if (!user?.role) {
      const roleRedirectTo = allowedRoles.includes("SELLER") ? "/seller/login" : redirectTo;
      return <Navigate to={roleRedirectTo} state={{ from: location }} replace />;
    }
    
    // user.role이 있으면 기존 로직대로 체크
    const hasRequiredRole = allowedRoles.includes(user.role);
    
    if (!hasRequiredRole) {
      // 역할이 맞지 않으면 판매자 로그인 페이지 또는 기본 리다이렉트 경로로 이동
      const roleRedirectTo = allowedRoles.includes("SELLER") ? "/seller/login" : redirectTo;
      return <Navigate to={roleRedirectTo} state={{ from: location }} replace />;
    }
  }

  // 인증 및 역할 확인 완료
  return children;
};

