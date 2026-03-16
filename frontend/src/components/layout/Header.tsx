import { Link } from "react-router-dom";
import { useAuthStore } from "store/authStore";
import { useState } from "react";
import { NotificationBell } from "components/notification/NotificationBell";

const Header: React.FC = () => {
  const { isAuthenticated, user, logout } = useAuthStore();

  const handleLogout = async () => {
    try {
      await logout();
    } catch (error) {
      console.error("로그아웃 실패:", error);
    }
  };
  const [isMobileMenuOpen, setIsMobileMenuOpen] = useState(false);

  const toggleMobileMenu = () => setIsMobileMenuOpen((prev) => !prev);

  return (
    <header className="sticky top-0 z-40 bg-white/80 backdrop-blur border-b">
      <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
        <div className="flex justify-between items-center h-16">
          <div className="flex justify-center"></div>
          {/* 로고 */}
          <div className="flex justify-center">
            <Link to="/" className="flex space-x-2">
              <span className="text-xl font-bold text-indigo-500">포포리</span>
            </Link>
          </div>

          {/* 네비게이션 */}
          <nav className="hidden md:flex items-center space-x-8">
            <Link
              to="/auctions"
              className="text-gray-700 hover:text-indigo-500 px-3 py-2 rounded-md text-sm font-medium transition-colors"
            >
              경매 목록
            </Link>
            <Link
              to="/auctions/create"
              className="text-gray-700 hover:text-indigo-500 px-3 py-2 rounded-md text-sm font-medium transition-colors"
            >
              견적 등록
            </Link>
            <Link
              to="/how-it-works"
              className="text-gray-700 hover:text-indigo-500 px-3 py-2 rounded-md text-sm font-medium transition-colors"
            >
              이용방법
            </Link>
            {isAuthenticated && (
              <>
                <Link
                  to="/chat"
                  className="text-gray-700 hover:text-indigo-500 px-3 py-2 rounded-md text-sm font-medium transition-colors"
                >
                  채팅
                </Link>
                <Link
                  to="/mypage"
                  className="text-gray-700 hover:text-indigo-500 px-3 py-2 rounded-md text-sm font-medium transition-colors"
                >
                  마이페이지
                </Link>
              </>
            )}
            <Link
              to="/admin/phone-models"
              className="text-gray-700 hover:text-indigo-500 px-3 py-2 rounded-md text-sm font-medium transition-colors"
            >
              휴대폰 모델 관리
            </Link>
          </nav>

          {/* 사용자 메뉴 */}
          <div className="hidden md:flex items-center space-x-4">
            {isAuthenticated && user ? (
              <div className="flex items-center space-x-4">
                <span className="text-sm text-gray-700">
                  안녕하세요, {user.nickname}님
                </span>
                
                <div className="flex items-center space-x-2">
                  <NotificationBell />
                  
                  <Link
                    to="/dashboard"
                    className="text-primary-600 hover:text-primary-700 text-sm font-medium transition-colors"
                  >
                    대시보드
                  </Link>
                  <button
                    onClick={handleLogout}
                    className="text-gray-500 hover:text-gray-700 text-sm font-medium transition-colors"
                  >
                    로그아웃
                  </button>
                </div>
              </div>
            ) : (
              <div className="flex items-center space-x-3">
                <Link
                  to="/login"
                  className="text-gray-700 hover:text-primary-600 text-sm font-medium transition-colors"
                >
                  로그인
                </Link>
                <Link
                  to="/signup"
                  className="bg-primary-600 text-white px-4 py-2 rounded-md text-sm font-medium hover:bg-primary-700 transition-colors"
                >
                  회원가입
                </Link>
              </div>
            )}
          </div>

          {/* 모바일 메뉴 버튼 & 알림 */}
          <div className="md:hidden flex items-center space-x-2">
            {isAuthenticated && (
              <NotificationBell className="scale-90" />
            )}
            <button
              type="button"
              onClick={toggleMobileMenu}
              aria-label="모바일 메뉴 열기"
              aria-expanded={isMobileMenuOpen}
              className="text-gray-700 hover:text-primary-600 focus:outline-none focus:text-primary-600 p-2"
            >
              {isMobileMenuOpen ? (
                <svg
                  className="h-6 w-6"
                  viewBox="0 0 24 24"
                  fill="none"
                  stroke="currentColor"
                >
                  <path
                    strokeLinecap="round"
                    strokeLinejoin="round"
                    strokeWidth={2}
                    d="M6 18L18 6M6 6l12 12"
                  />
                </svg>
              ) : (
                <svg
                  className="h-6 w-6"
                  viewBox="0 0 24 24"
                  fill="none"
                  stroke="currentColor"
                >
                  <path
                    strokeLinecap="round"
                    strokeLinejoin="round"
                    strokeWidth={2}
                    d="M4 6h16M4 12h16M4 18h16"
                  />
                </svg>
              )}
            </button>
          </div>
        </div>
      </div>

      {/* 모바일 드롭다운 메뉴 */}
      {isMobileMenuOpen && (
        <div className="md:hidden border-t bg-white/90 backdrop-blur">
          <div className="px-4 py-3 space-y-2">
            <Link
              to="/auctions"
              className="block px-2 py-2 rounded-md text-gray-700 hover:bg-gray-100"
              onClick={() => setIsMobileMenuOpen(false)}
            >
              경매 목록
            </Link>
            <Link
              to="/auctions/create"
              className="block px-2 py-2 rounded-md text-gray-700 hover:bg-gray-100"
              onClick={() => setIsMobileMenuOpen(false)}
            >
              견적 등록
            </Link>
            <Link
              to="/how-it-works"
              className="block px-2 py-2 rounded-md text-gray-700 hover:bg-gray-100"
              onClick={() => setIsMobileMenuOpen(false)}
            >
              이용방법
            </Link>
            {isAuthenticated && (
              <>
                <Link
                  to="/notifications"
                  className="block px-2 py-2 rounded-md text-gray-700 hover:bg-gray-100"
                  onClick={() => setIsMobileMenuOpen(false)}
                >
                  알림
                </Link>
                <Link
                  to="/chat"
                  className="block px-2 py-2 rounded-md text-gray-700 hover:bg-gray-100"
                  onClick={() => setIsMobileMenuOpen(false)}
                >
                  채팅
                </Link>
                <Link
                  to="/mypage"
                  className="block px-2 py-2 rounded-md text-gray-700 hover:bg-gray-100"
                  onClick={() => setIsMobileMenuOpen(false)}
                >
                  마이페이지
                </Link>
              </>
            )}

            <div className="border-t pt-2 mt-2">
              {isAuthenticated && user ? (
                <>
                  <Link
                    to="/dashboard"
                    className="block px-2 py-2 rounded-md text-primary-600 hover:bg-gray-100"
                    onClick={() => setIsMobileMenuOpen(false)}
                  >
                    대시보드
                  </Link>
                  <button
                    onClick={() => {
                      handleLogout();
                      setIsMobileMenuOpen(false);
                    }}
                    className="w-full text-left px-2 py-2 rounded-md text-gray-500 hover:bg-gray-100"
                  >
                    로그아웃
                  </button>
                </>
              ) : (
                <>
                  <Link
                    to="/login"
                    className="block px-2 py-2 rounded-md text-gray-700 hover:bg-gray-100"
                    onClick={() => setIsMobileMenuOpen(false)}
                  >
                    로그인
                  </Link>
                  <Link
                    to="/signup"
                    className="block px-2 py-2 rounded-md bg-primary-600 text-white text-center hover:bg-primary-700"
                    onClick={() => setIsMobileMenuOpen(false)}
                  >
                    회원가입
                  </Link>
                </>
              )}
            </div>
          </div>
        </div>
      )}
    </header>
  );
};

export default Header;
