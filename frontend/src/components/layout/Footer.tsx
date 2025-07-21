import { Link } from "react-router-dom";

const Footer: React.FC = () => {
  return (
    <footer className="bg-gray-900 text-white">
      <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-12">
        <div className="grid grid-cols-1 md:grid-cols-4 gap-8">
          {/* Company Info */}
          <div>
            <h3 className="text-lg font-semibold mb-4">PhoneBid</h3>
            <p className="text-gray-300 text-sm">
              투명하고 합리적인 휴대폰 거래 플랫폼
            </p>
          </div>

          {/* Quick Links */}
          <div>
            <h3 className="text-lg font-semibold mb-4">빠른 링크</h3>
            <ul className="space-y-2">
              <li>
                <Link
                  to="/auctions"
                  className="text-gray-300 hover:text-white text-sm"
                >
                  경매 목록
                </Link>
              </li>
              <li>
                <Link
                  to="/how-it-works"
                  className="text-gray-300 hover:text-white text-sm"
                >
                  이용 방법
                </Link>
              </li>
              <li>
                <Link
                  to="/pricing"
                  className="text-gray-300 hover:text-white text-sm"
                >
                  요금 안내
                </Link>
              </li>
            </ul>
          </div>

          {/* Support */}
          <div>
            <h3 className="text-lg font-semibold mb-4">고객 지원</h3>
            <ul className="space-y-2">
              <li>
                <Link
                  to="/support"
                  className="text-gray-300 hover:text-white text-sm"
                >
                  고객센터
                </Link>
              </li>
              <li>
                <Link
                  to="/faq"
                  className="text-gray-300 hover:text-white text-sm"
                >
                  자주 묻는 질문
                </Link>
              </li>
              <li>
                <Link
                  to="/contact"
                  className="text-gray-300 hover:text-white text-sm"
                >
                  문의하기
                </Link>
              </li>
            </ul>
          </div>

          {/* Legal */}
          <div>
            <h3 className="text-lg font-semibold mb-4">약관 및 정책</h3>
            <ul className="space-y-2">
              <li>
                <Link
                  to="/terms"
                  className="text-gray-300 hover:text-white text-sm"
                >
                  이용약관
                </Link>
              </li>
              <li>
                <Link
                  to="/privacy"
                  className="text-gray-300 hover:text-white text-sm"
                >
                  개인정보처리방침
                </Link>
              </li>
              <li>
                <Link
                  to="/business-terms"
                  className="text-gray-300 hover:text-white text-sm"
                >
                  사업자 약관
                </Link>
              </li>
            </ul>
          </div>
        </div>

        <div className="border-t border-gray-800 mt-8 pt-8 text-center">
          <p className="text-gray-300 text-sm">
            © 2024 PhoneBid. All rights reserved.
          </p>
        </div>
      </div>
    </footer>
  );
};

export default Footer;
