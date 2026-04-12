import { Link } from "react-router-dom";
import { useAuthStore } from "store/authStore";

export function LandingHeader() {
  const { isAuthenticated, isInitializing } = useAuthStore();

  const quoteTo = isAuthenticated ? "/" : "/login";
  const quoteState = isAuthenticated
    ? undefined
    : { from: { pathname: "/" } };

  return (
    <header className="sticky top-0 z-50 w-full bg-[#1e293b] border-b border-white/5">
      <div className="max-w-6xl mx-auto px-4 sm:px-6 h-14 sm:h-16 flex items-center justify-between">
        <Link
          to="/"
          className="text-white font-bold text-lg sm:text-xl lowercase tracking-tight hover:opacity-90 transition-opacity"
        >
          bidr
        </Link>
        <nav
          className="flex items-center gap-3 sm:gap-5 shrink-0"
          aria-label="랜딩 주요 이동"
        >
          <Link
            to="/seller/signup"
            className="text-white text-sm font-medium hover:text-white/90 transition-colors whitespace-nowrap"
          >
            판매자 입점하기
          </Link>
          {isInitializing ? (
            <span
              className="inline-flex items-center justify-center rounded-full bg-[#7c3aed]/70 text-white text-sm font-medium px-4 sm:px-6 py-2 sm:py-2.5 whitespace-nowrap shadow-sm cursor-wait"
              aria-busy="true"
            >
              견적 요청하기
            </span>
          ) : (
            <Link
              to={quoteTo}
              state={quoteState}
              className="inline-flex items-center justify-center rounded-full bg-[#7c3aed] hover:bg-[#6d28d9] text-white text-sm font-medium px-4 sm:px-6 py-2 sm:py-2.5 transition-colors whitespace-nowrap shadow-sm"
            >
              견적 요청하기
            </Link>
          )}
        </nav>
      </div>
    </header>
  );
}
