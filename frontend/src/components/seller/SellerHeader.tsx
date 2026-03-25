import { Link } from "react-router-dom";
import { useAuthStore } from "store/authStore";
import { useState, useEffect, useRef } from "react";
import { sellerService } from "services/sellerService";
import type { SellerProfileResponseDto } from "types/SellerTypes";
import { NotificationBell } from "components/notification/NotificationBell";

const SELLER_NOTIFICATION_TYPES = [
  "QUOTE_CREATED",
  "BID_SELECTED",
  "LOWEST_PRICE_UPDATED",
  "CONTRACT_SIGNED",
  "PAYMENT_COMPLETED",
  "SELLER_APPROVED",
  "SELLER_REJECTED",
  "CHAT_MESSAGE_RECEIVED",
] as const;

export const SellerHeader: React.FC = () => {
  const { isAuthenticated, user, logout } = useAuthStore();
  const [isMobileMenuOpen, setIsMobileMenuOpen] = useState(false);
  const [isProfileMenuOpen, setIsProfileMenuOpen] = useState(false);
  const [sellerProfile, setSellerProfile] = useState<SellerProfileResponseDto | null>(null);
  const profileMenuRef = useRef<HTMLDivElement>(null);
  const profileButtonRef = useRef<HTMLButtonElement>(null);

  const toggleMobileMenu = () => setIsMobileMenuOpen((prev) => !prev);
  const toggleProfileMenu = () => setIsProfileMenuOpen((prev) => !prev);

  const handleLogout = async () => {
    try {
      await logout();
    } catch (error) {
      console.error("로그아웃 실패:", error);
    }
  };

  useEffect(() => {
    if (isAuthenticated && user?.role === "SELLER") {
      sellerService.getSellerProfile()
        .then((profile) => {
          setSellerProfile(profile);
        })
        .catch((error) => {
          console.error("판매자 프로필 조회 실패:", error);
        });
    }
  }, [isAuthenticated, user]);

  useEffect(() => {
    function handleClickOutside(event: MouseEvent) {
      if (
        profileMenuRef.current &&
        profileButtonRef.current &&
        !profileMenuRef.current.contains(event.target as Node) &&
        !profileButtonRef.current.contains(event.target as Node)
      ) {
        setIsProfileMenuOpen(false);
      }
    }

    if (isProfileMenuOpen) {
      document.addEventListener("mousedown", handleClickOutside);
      return () => {
        document.removeEventListener("mousedown", handleClickOutside);
      };
    }
  }, [isProfileMenuOpen]);

  useEffect(() => {
    function handleEscape(event: KeyboardEvent) {
      if (event.key === "Escape") {
        setIsProfileMenuOpen(false);
      }
    }

    if (isProfileMenuOpen) {
      document.addEventListener("keydown", handleEscape);
      return () => {
        document.removeEventListener("keydown", handleEscape);
      };
    }
  }, [isProfileMenuOpen]);

  return (
    <header className="sticky top-0 z-40 bg-white border-b">
      <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
        <div className="flex justify-between items-center h-16">
          {/* 로고 */}
          <Link
            to="/seller-center"
            className="flex items-center gap-3 hover:opacity-90 transition-opacity"
            aria-label="판매자센터 홈으로 이동"
          >
            <div className="w-10 h-10 bg-primary-600 rounded flex items-center justify-center">
              <svg
                className="w-6 h-6 text-white"
                fill="none"
                stroke="currentColor"
                viewBox="0 0 24 24"
              >
                <path
                  strokeLinecap="round"
                  strokeLinejoin="round"
                  strokeWidth={2}
                  d="M12 18h.01M8 21h8a2 2 0 002-2V5a2 2 0 00-2-2H8a2 2 0 00-2 2v14a2 2 0 002 2z"
                />
              </svg>
            </div>
            <span className="text-lg font-semibold text-gray-900">
              폰비드 판매자센터
            </span>
          </Link>

          {/* 네비게이션 */}
          <nav className="hidden md:flex items-center space-x-6">
            <Link
              to="/seller-center"
              className="text-gray-700 hover:text-primary-600 px-3 py-2 rounded-md text-sm font-medium transition-colors"
            >
              서비스 소개
            </Link>
            <Link
              to="/seller-center"
              className="text-gray-700 hover:text-primary-600 px-3 py-2 rounded-md text-sm font-medium transition-colors"
            >
              이용 가이드
            </Link>
            <Link
              to="/seller-center"
              className="text-gray-700 hover:text-primary-600 px-3 py-2 rounded-md text-sm font-medium transition-colors"
            >
              고객센터
            </Link>
          </nav>

          {/* 사용자 메뉴 */}
          <div className="hidden md:flex items-center space-x-4">
            {isAuthenticated && user ? (
              <div className="flex items-center space-x-3">
                <NotificationBell
                  typesFilter={[...SELLER_NOTIFICATION_TYPES]}
                  viewAllPath="/seller-center/notifications"
                />
                <div className="relative">
                  <button
                    ref={profileButtonRef}
                    type="button"
                    onClick={toggleProfileMenu}
                    className="flex items-center space-x-2 rounded-md px-2 py-1 hover:bg-gray-100 transition-colors"
                    aria-label="프로필 메뉴"
                    aria-expanded={isProfileMenuOpen}
                  >
                    <div className="w-8 h-8 bg-gray-300 rounded-full flex items-center justify-center">
                      <span className="text-xs text-gray-600">
                        {sellerProfile?.storeName?.[0] || user?.nickname?.[0] || "?"}
                      </span>
                    </div>
                    <span className="text-sm text-gray-900 font-medium">
                      {sellerProfile?.storeName || user?.nickname || "판매자"}
                    </span>
                    <svg
                      className="w-4 h-4 text-gray-600"
                      fill="none"
                      stroke="currentColor"
                      viewBox="0 0 24 24"
                    >
                      <path
                        strokeLinecap="round"
                        strokeLinejoin="round"
                        strokeWidth={2}
                        d="M19 9l-7 7-7-7"
                      />
                    </svg>
                  </button>

                  {isProfileMenuOpen && (
                    <div
                      ref={profileMenuRef}
                      className="absolute right-0 mt-2 w-44 bg-white border border-gray-200 rounded-md shadow-lg z-50 overflow-hidden"
                    >
                      <Link
                        to="/seller-center/profile"
                        className="block px-4 py-2 text-sm text-gray-700 hover:bg-gray-50"
                        onClick={() => setIsProfileMenuOpen(false)}
                      >
                        내 프로필
                      </Link>
                      <button
                        type="button"
                        onClick={handleLogout}
                        className="w-full text-left px-4 py-2 text-sm text-red-600 hover:bg-red-50"
                      >
                        로그아웃
                      </button>
                    </div>
                  )}
                </div>
              </div>
            ) : (
              <div className="flex items-center space-x-3">
                <Link
                  to="/seller/login"
                  className="text-gray-700 hover:text-primary-600 text-sm font-medium"
                >
                  로그인
                </Link>
              </div>
            )}
          </div>

          {/* 모바일 메뉴 버튼 */}
          <div className="md:hidden">
            <button
              type="button"
              onClick={toggleMobileMenu}
              aria-label="모바일 메뉴 열기"
              className="text-gray-700 hover:text-primary-600"
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
        <div className="md:hidden border-t bg-white">
          <div className="px-4 py-3 space-y-2">
            <Link
              to="/seller-center"
              className="block px-2 py-2 rounded-md text-gray-700 hover:bg-gray-100"
              onClick={() => setIsMobileMenuOpen(false)}
            >
              서비스 소개
            </Link>
            <Link
              to="/seller-center"
              className="block px-2 py-2 rounded-md text-gray-700 hover:bg-gray-100"
              onClick={() => setIsMobileMenuOpen(false)}
            >
              이용 가이드
            </Link>
            <Link
              to="/seller-center"
              className="block px-2 py-2 rounded-md text-gray-700 hover:bg-gray-100"
              onClick={() => setIsMobileMenuOpen(false)}
            >
              고객센터
            </Link>
            {isAuthenticated && user && (
              <>
                <Link
                  to="/seller-center/profile"
                  className="block px-2 py-2 rounded-md text-gray-700 hover:bg-gray-100"
                  onClick={() => setIsMobileMenuOpen(false)}
                >
                  내 프로필
                </Link>
                <button
                  type="button"
                  onClick={() => {
                    void handleLogout();
                    setIsMobileMenuOpen(false);
                  }}
                  className="w-full text-left px-2 py-2 rounded-md text-red-600 hover:bg-red-50"
                >
                  로그아웃
                </button>
              </>
            )}
          </div>
        </div>
      )}
    </header>
  );
};

