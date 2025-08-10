import { Link } from "react-router-dom";

const Footer: React.FC = () => {
  return (
    <footer className="bg-gray-900 text-white" role="contentinfo">
      <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-12">
        <div className="grid grid-cols-1 sm:grid-cols-2 md:grid-cols-4 gap-8">
          {/* Company Info */}
          <div>
            <h3 className="text-xl font-bold mb-3">PhoneBid</h3>
            <p className="text-gray-400 text-sm leading-6">
              투명하고 합리적인 휴대폰 거래 플랫폼
            </p>
          </div>

          {/* Quick Links */}
          <nav aria-label="푸터 빠른 링크">
            <h3 className="text-sm font-semibold text-gray-400 uppercase tracking-wider mb-4">
              빠른 링크
            </h3>
            <ul className="flex gap-2">
              <li>
                <Link
                  to="/auctions"
                  className="text-gray-300 hover:text-white hover:underline text-sm focus:outline-none focus-visible:ring-2 focus-visible:ring-primary-500 rounded"
                >
                  경매 목록
                </Link>
              </li>
              <li>
                <Link
                  to="/how-it-works"
                  className="text-gray-300 hover:text-white hover:underline text-sm focus:outline-none focus-visible:ring-2 focus-visible:ring-primary-500 rounded"
                >
                  이용 방법
                </Link>
              </li>
              <li>
                <Link
                  to="/pricing"
                  className="text-gray-300 hover:text-white hover:underline text-sm focus:outline-none focus-visible:ring-2 focus-visible:ring-primary-500 rounded"
                >
                  요금 안내
                </Link>
              </li>
            </ul>
          </nav>

          {/* Support */}
          <nav aria-label="푸터 고객 지원 링크">
            <h3 className="text-sm font-semibold text-gray-400 uppercase tracking-wider mb-4">
              고객 지원
            </h3>
            <ul className="flex gap-2">
              <li>
                <Link
                  to="/support"
                  className="text-gray-300 hover:text-white hover:underline text-sm focus:outline-none focus-visible:ring-2 focus-visible:ring-primary-500 rounded"
                >
                  고객센터
                </Link>
              </li>
              <li>
                <Link
                  to="/faq"
                  className="text-gray-300 hover:text-white hover:underline text-sm focus:outline-none focus-visible:ring-2 focus-visible:ring-primary-500 rounded"
                >
                  자주 묻는 질문
                </Link>
              </li>
              <li>
                <Link
                  to="/contact"
                  className="text-gray-300 hover:text-white hover:underline text-sm focus:outline-none focus-visible:ring-2 focus-visible:ring-primary-500 rounded"
                >
                  문의하기
                </Link>
              </li>
            </ul>
          </nav>

          {/* Legal */}
          <nav aria-label="푸터 약관 및 정책 링크">
            <h3 className="text-sm font-semibold text-gray-400 uppercase tracking-wider mb-4">
              약관 및 정책
            </h3>
            <ul className="flex gap-2">
              <li>
                <Link
                  to="/terms"
                  className="text-gray-300 hover:text-white hover:underline text-sm focus:outline-none focus-visible:ring-2 focus-visible:ring-primary-500 rounded"
                >
                  이용약관
                </Link>
              </li>
              <li>
                <Link
                  to="/privacy"
                  className="text-gray-300 hover:text-white hover:underline text-sm focus:outline-none focus-visible:ring-2 focus-visible:ring-primary-500 rounded"
                >
                  개인정보처리방침
                </Link>
              </li>
              <li>
                <Link
                  to="/business-terms"
                  className="text-gray-300 hover:text-white hover:underline text-sm focus:outline-none focus-visible:ring-2 focus-visible:ring-primary-500 rounded"
                >
                  사업자 약관
                </Link>
              </li>
            </ul>
          </nav>
        </div>

        <div className="border-t border-gray-800 mt-10 pt-8 text-center">
          <p className="text-gray-400 text-sm">
            © 2024 PhoneBid. All rights reserved.
          </p>
        </div>
      </div>
    </footer>
  );
};

export default Footer;
