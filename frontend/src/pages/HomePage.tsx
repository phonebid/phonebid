import { Link } from "react-router-dom";
import Button from "components/common/Button";

const HomePage: React.FC = () => {
  return (
    <div className="bg-white">
      {/* Hero Section */}
      <section className="bg-gradient-to-r from-primary-600 to-primary-700 text-white">
        <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-24">
          <div className="text-center">
            <h1 className="text-4xl md:text-6xl font-bold mb-6">
              휴대폰 최저가를
              <br />
              <span className="text-primary-200">역경매</span>로 찾아보세요
            </h1>
            <p className="text-xl md:text-2xl mb-8 text-primary-100">
              원하는 휴대폰 정보만 등록하면
              <br />
              판매자들이 직접 가격을 제안합니다
            </p>
            <div className="flex flex-col sm:flex-row gap-4 justify-center">
              <Link to="/auctions/create">
                <Button
                  size="lg"
                  className="bg-white text-primary-600 hover:bg-gray-100"
                >
                  견적 등록하기
                </Button>
              </Link>
              <Link to="/auctions">
                <Button
                  size="lg"
                  variant="secondary"
                  className="border-white text-white hover:bg-white hover:text-primary-600"
                >
                  경매 둘러보기
                </Button>
              </Link>
            </div>
          </div>
        </div>
      </section>

      {/* How It Works */}
      <section className="py-16">
        <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
          <div className="text-center mb-12">
            <h2 className="text-3xl font-bold text-gray-900 mb-4">
              어떻게 작동하나요?
            </h2>
            <p className="text-lg text-gray-600">
              간단한 3단계로 최저가 휴대폰을 구매하세요
            </p>
          </div>
          <div className="grid grid-cols-1 md:grid-cols-3 gap-8">
            <div className="text-center">
              <div className="bg-primary-100 rounded-full w-16 h-16 flex items-center justify-center mx-auto mb-4">
                <span className="text-2xl font-bold text-primary-600">1</span>
              </div>
              <h3 className="text-xl font-semibold mb-2">견적 등록</h3>
              <p className="text-gray-600">
                원하는 휴대폰 모델, 용량, 색상 등<br />
                상세 정보를 등록합니다
              </p>
            </div>
            <div className="text-center">
              <div className="bg-primary-100 rounded-full w-16 h-16 flex items-center justify-center mx-auto mb-4">
                <span className="text-2xl font-bold text-primary-600">2</span>
              </div>
              <h3 className="text-xl font-semibold mb-2">입찰 받기</h3>
              <p className="text-gray-600">
                등록된 견적에 판매자들이
                <br />
                경쟁적으로 가격을 제안합니다
              </p>
            </div>
            <div className="text-center">
              <div className="bg-primary-100 rounded-full w-16 h-16 flex items-center justify-center mx-auto mb-4">
                <span className="text-2xl font-bold text-primary-600">3</span>
              </div>
              <h3 className="text-xl font-semibold mb-2">거래 완료</h3>
              <p className="text-gray-600">
                최적의 조건을 선택하고
                <br />
                안전하게 거래를 완료합니다
              </p>
            </div>
          </div>
        </div>
      </section>

      {/* Features */}
      <section className="bg-gray-50 py-16">
        <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
          <div className="text-center mb-12">
            <h2 className="text-3xl font-bold text-gray-900 mb-4">
              PhoneBid의 장점
            </h2>
          </div>
          <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-8">
            <div className="text-center">
              <div className="bg-white rounded-lg p-6 shadow-sm">
                <div className="text-3xl mb-4">💰</div>
                <h3 className="text-lg font-semibold mb-2">최저가 보장</h3>
                <p className="text-gray-600 text-sm">
                  여러 판매자의 경쟁으로
                  <br />
                  최저가를 보장받으세요
                </p>
              </div>
            </div>
            <div className="text-center">
              <div className="bg-white rounded-lg p-6 shadow-sm">
                <div className="text-3xl mb-4">🔒</div>
                <h3 className="text-lg font-semibold mb-2">안전한 거래</h3>
                <p className="text-gray-600 text-sm">
                  에스크로 시스템으로
                  <br />
                  안전한 거래를 보장합니다
                </p>
              </div>
            </div>
            <div className="text-center">
              <div className="bg-white rounded-lg p-6 shadow-sm">
                <div className="text-3xl mb-4">⚡</div>
                <h3 className="text-lg font-semibold mb-2">빠른 처리</h3>
                <p className="text-gray-600 text-sm">
                  24시간 경매 시스템으로
                  <br />
                  빠르게 거래가 성사됩니다
                </p>
              </div>
            </div>
            <div className="text-center">
              <div className="bg-white rounded-lg p-6 shadow-sm">
                <div className="text-3xl mb-4">📱</div>
                <h3 className="text-lg font-semibold mb-2">모든 기종</h3>
                <p className="text-gray-600 text-sm">
                  아이폰, 갤럭시 등<br />
                  모든 휴대폰을 지원합니다
                </p>
              </div>
            </div>
          </div>
        </div>
      </section>

      {/* CTA Section */}
      <section className="bg-primary-600 text-white py-16">
        <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 text-center">
          <h2 className="text-3xl font-bold mb-4">지금 시작해보세요</h2>
          <p className="text-xl mb-8 text-primary-100">
            몇 분만 투자하면 최저가 휴대폰을 만날 수 있습니다
          </p>
          <Link to="/auctions/create">
            <Button
              size="lg"
              className="bg-white text-primary-600 hover:bg-gray-100"
            >
              무료로 견적 등록하기
            </Button>
          </Link>
        </div>
      </section>
    </div>
  );
};

export default HomePage;
