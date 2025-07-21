import { Link } from "react-router-dom";
import { useAuthStore } from "store/authStore";

const Header: React.FC = () => {
  const { isAuthenticated, user, logout } = useAuthStore();

  const handleLogout = () => {
    logout();
    // 추후 로그아웃 API 호출 추가
  };

  return (
    <header className="bg-white shadow-sm border-b">
      <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
        <div className="flex justify-between items-center h-16">
          {/* 로고 */}
          <div className="flex items-center">
            <Link to="/" className="flex items-center space-x-2">
              <div className="w-8 h-8 bg-primary-600 rounded-lg flex items-center justify-center">
                <span className="text-white font-bold text-sm">PB</span>
              </div>
              <span className="text-xl font-bold text-gray-900">PhoneBid</span>
            </Link>
          </div>

          {/* 네비게이션 */}
          <nav className="hidden md:flex space-x-8">
            <Link
              to="/auctions"
              className="text-gray-700 hover:text-primary-600 px-3 py-2 rounded-md text-sm font-medium transition-colors"
            >
              경매 목록
            </Link>
            <Link
              to="/auctions/create"
              className="text-gray-700 hover:text-primary-600 px-3 py-2 rounded-md text-sm font-medium transition-colors"
            >
              견적 등록
            </Link>
            <Link
              to="/how-it-works"
              className="text-gray-700 hover:text-primary-600 px-3 py-2 rounded-md text-sm font-medium transition-colors"
            >
              이용방법
            </Link>
          </nav>

          {/* 사용자 메뉴 */}
          <div className="flex items-center space-x-4">
            {isAuthenticated && user ? (
              <div className="flex items-center space-x-3">
                <span className="text-sm text-gray-700">
                  안녕하세요, {user.name}님
                </span>
                <Link
                  to="/dashboard"
                  className="text-primary-600 hover:text-primary-700 text-sm font-medium"
                >
                  대시보드
                </Link>
                <button
                  onClick={handleLogout}
                  className="text-gray-500 hover:text-gray-700 text-sm font-medium"
                >
                  로그아웃
                </button>
              </div>
            ) : (
              <div className="flex items-center space-x-3">
                <Link
                  to="/login"
                  className="text-gray-700 hover:text-primary-600 text-sm font-medium"
                >
                  로그인
                </Link>
                <Link
                  to="/register"
                  className="bg-primary-600 text-white px-4 py-2 rounded-md text-sm font-medium hover:bg-primary-700 transition-colors"
                >
                  회원가입
                </Link>
              </div>
            )}
          </div>

          {/* 모바일 메뉴 버튼 */}
          <div className="md:hidden">
            <button
              type="button"
              className="text-gray-700 hover:text-primary-600 focus:outline-none focus:text-primary-600"
            >
              <svg
                className="h-6 w-6"
                fill="none"
                viewBox="0 0 24 24"
                stroke="currentColor"
              >
                <path
                  strokeLinecap="round"
                  strokeLinejoin="round"
                  strokeWidth={2}
                  d="M4 6h16M4 12h16M4 18h16"
                />
              </svg>
            </button>
          </div>
        </div>
      </div>
    </header>
  );
};

export default Header;
