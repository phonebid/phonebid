import { Link } from "react-router-dom";
import { useState } from "react";
import { AuroraBackground } from "components/ui/aurora-background";
import { LandingHeader } from "components/landing-header";

const TICKER_ITEMS = [
  "[부산-이OO님] 아이폰 16 Pro ₩145,000 구매",
  "[대전-박OO님] 갤럭시 S25 Ultra ₩450,000 구매",
  "[인천-최OO님] 아이폰 16 ₩58,000 구매",
  "[광주-정OO님] Z플립7 ₩248,000 구매",
];

const LIVE_QUOTES = [
  {
    id: 1,
    isLowest: true,
    sellerName: "정직한 폰박사",
    sellerBadge: "정직",
    badgeColor: "bg-blue-500",
    badgeShadow: "shadow-blue-500/30",
    rating: 4.9,
    stars: 5,
    productName: "갤럭시 Z플립7",
    productOption: "번호이동 | 선택약정",
    productImage:
      "https://lh3.googleusercontent.com/aida-public/AB6AXuBJCiOFjCZgJgwcsUWT2RHJd6poW-h9sINIBWOHzfxH7ksmzw09O9BnwI2BKmBNM9Vm21gEzIQVlrGm1PTbMpNPrx8VAzw9lTX4lOaVQE8d0aHb-AvjAYXtwR8KVAbqdLRHGLJubcVBHTIihARc5aIcSBoYeclNcnqO7TqjM5devk3jewsbuDf0yglrTYLkpiTqZW9Kwsk8UlFLAn_KDRbnWR7CisTrKdXWubT38XNiEsnVCSHd21OhJtmq9ZQav6rn_bWBNN4YWu3u",
    installmentAmount: "₩240,000",
    contractOption: "24개월 (25% 할인)",
    tags: [
      { label: "무료 케이스", highlight: false },
      { label: "보호필름", highlight: false },
      { label: "당일 배송", highlight: true },
    ],
  },
  {
    id: 2,
    isLowest: false,
    sellerName: "최고리뷰닷",
    sellerBadge: "최고",
    badgeColor: "bg-purple-500",
    badgeShadow: "shadow-purple-500/30",
    rating: 5.0,
    stars: 5,
    productName: "아이폰 17 프로",
    productOption: "기기변경 | 선택약정",
    productImage:
      "https://lh3.googleusercontent.com/aida-public/AB6AXuADXQ3n4cRYeeS4qyrYrt2rw-HW5hvx60a8WgQJgMT78e6d6XgJ50TC1Tr6X0FG_uuT81jsszkCwrpJE6nqmgDH5NvdPvHrlIT6MGzY5MRssu0nnlM2-VeItcTd3YhspseM4sv6nGBcqBaXHfvbV8g2BM8NOCws20dDvA0s6y8TAoqbogvD9pxW247nev39oFGDTiOKpj3-KKEclcJrFKCO6dZWfnl53LFCxxziNCT0XH71OiP7_23Z3mD-kbbUHT5ij5rJ7my9Ccuc",
    installmentAmount: "₩242,000",
    contractOption: "24개월 (25% 할인)",
    tags: [
      { label: "보호필름", highlight: false },
      { label: "무선충전기", highlight: false },
    ],
  },
  {
    id: 3,
    isLowest: false,
    sellerName: "폰고수 김프로",
    sellerBadge: "폰고",
    badgeColor: "bg-green-600",
    badgeShadow: "shadow-green-600/30",
    rating: 4.8,
    stars: 4,
    productName: "갤럭시 S25 울트라",
    productOption: "번호이동 | 선택약정",
    productImage:
      "https://lh3.googleusercontent.com/aida-public/AB6AXuAp0mVbTf_VIVJRKhkiE5x3LN_jRCGF0ussSWRapv8BaHqDCpPElTS1zMuUp7QDSUZeZRdNI3z92ual_vbIr2YxHq64Bgg8bZQwlOrSo6U6xQDGG8IdJMc84t1cSTmi4QKktphI9Q7X_VFoSbtQmFWmkpRWRmm6uYtpXj5J3ZVrC-I-lUz4FBdfML1B9JKW8vINjlCkwDZcSKmKPqXGI1tAvbIHcx0KugRj7l5-i_OXJdx-w22oNkOlipvUQtFkbqn-yt7LqIyPEoOW",
    installmentAmount: "₩245,000",
    contractOption: "24개월 (25% 할인)",
    tags: [
      { label: "차량용 거치대", highlight: false },
      { label: "익일 배송", highlight: true },
    ],
  },
  {
    id: 4,
    isLowest: false,
    sellerName: "양심만 팔아요",
    sellerBadge: "양심",
    badgeColor: "bg-red-600",
    badgeShadow: "shadow-red-600/30",
    rating: 4.7,
    stars: 4,
    productName: "갤럭시 Z플립7",
    productOption: "기기변경 | 선택약정",
    productImage:
      "https://lh3.googleusercontent.com/aida-public/AB6AXuA8OsUWKv5ZHZsmzWybYhjA41aeSfa40vDQvQyxzDx8s-twiVABVbxF_N1W4H9iROXR-VYmurPul0p3mwQRu8_ZA5GXXGxHvJC-L0B8mxw5hqOVgppWn1sbjGuv8cheVql8Az2CKOM9bc7BlGXT7ou0zovDMWAJ9otYZXl_M1wUuTy_eytQ-hLJeMxVQ5blQjztCuwZgV1v872TX-0b0fNvXzp4_mRDks0tj8Cr1cJthkU1Ti_RDtWm0Wwpp3424-vt9WrskLCZKchi",
    installmentAmount: "₩248,000",
    contractOption: "24개월 (25% 할인)",
    tags: [
      { label: "정품 케이스", highlight: false },
      { label: "당일 발송", highlight: true },
    ],
  },
];

const PURCHASE_STEPS = [
  {
    step: "01",
    title: "견적요청",
    subtitle: "10초 만에 끝나는 견적 요청",
    description: "복잡한 인증이나 서류 없이\n원하는 조건만 요청",
    icon: "touch_app",
    iconColor:
      "bg-blue-100 dark:bg-blue-900/30 text-blue-600 dark:text-blue-400",
  },
  {
    step: "02",
    title: "가격비교",
    subtitle: "실시간으로 쏟아지는 견적",
    description: "견적을 실시간으로 확인하고\n최저가 선택",
    icon: "analytics",
    iconColor:
      "bg-purple-100 dark:bg-purple-900/30 text-purple-600 dark:text-purple-400",
  },
  {
    step: "03",
    title: "택배수령",
    subtitle: "안심하고 기다리는 문 앞 배송",
    description: "검증된 판매자가 직접 발송하는\n안심 택배 서비스",
    icon: "local_shipping",
    iconColor:
      "bg-indigo-100 dark:bg-indigo-900/30 text-indigo-600 dark:text-indigo-400",
  },
  {
    step: "04",
    title: "바로개통",
    subtitle: "전원만 켜면 끝",
    description: "택배 받고 전원만 딸깍-\n끊김 없는 핸드폰 사용",
    icon: "power_settings_new",
    iconColor:
      "bg-cyan-100 dark:bg-cyan-900/30 text-cyan-600 dark:text-cyan-400",
  },
];

const SAFETY_ITEMS = [
  {
    title: "한국정보통신진흥협회 인증",
    description: "공식 사전승낙서\n보유 업체만",
    icon: "verified_user",
    iconColor:
      "bg-blue-100 dark:bg-blue-900/30 text-blue-600 dark:text-blue-400",
  },
  {
    title: "대리점 코드",
    description: "이동통신사 공식\n대리점 확인",
    icon: "storefront",
    iconColor:
      "bg-purple-100 dark:bg-purple-900/30 text-purple-600 dark:text-purple-400",
  },
  {
    title: "실명 검증",
    description: "사업자등록번호\n실시간 확인",
    icon: "person_search",
    iconColor:
      "bg-green-100 dark:bg-green-900/30 text-green-600 dark:text-green-400",
  },
];

function MaterialIcon({
  name,
  className = "",
}: {
  name: string;
  className?: string;
}) {
  return (
    <span className={`material-icons-round ${className}`} aria-hidden>
      {name}
    </span>
  );
}

const LandingPage: React.FC = () => {
  const [name, setName] = useState("");
  const [phone, setPhone] = useState("");
  const [agreePrivacy, setAgreePrivacy] = useState(false);

  const handleLaunchSignup = (e: React.FormEvent) => {
    e.preventDefault();
  };

  return (
    <div className="bg-slate-50 dark:bg-slate-900 text-slate-900 dark:text-slate-100 antialiased overflow-x-hidden transition-colors duration-300">
      <LandingHeader />
      <div className="h-14 sm:h-16 shrink-0" aria-hidden />
      {/* Hero Section */}
      <header className="relative min-h-[85vh] overflow-hidden">
        <AuroraBackground
          className="!min-h-[85vh] !h-auto !bg-slate-900 dark:!bg-zinc-900 !items-stretch !justify-center"
          showRadialGradient={true}
        >
          <div className="relative z-10 pt-16 pb-16 px-4 max-w-6xl mx-auto flex flex-col lg:flex-row items-center gap-8 lg:gap-12 min-h-[85vh] w-full">
            <div className="flex-1 text-center lg:text-left order-2 lg:order-1">
              <div className="inline-flex items-center gap-1.5 px-3 py-1.5 rounded-full bg-slate-800/60 backdrop-blur-sm mb-6">
                <MaterialIcon name="bolt" className="text-blue-400 text-sm" />
                <span className="text-white/90 text-xs font-medium">
                  빠르고 편리한 전국 최저가 입찰 시스템
                </span>
              </div>
              <h1 className="text-3xl sm:text-4xl lg:text-5xl font-black text-white leading-tight mb-4">
                침대 밖은 위험하니까
                <br />
                핸드폰 구매도
                <br />
                <span className="text-transparent bg-clip-text bg-gradient-to-r from-cyan-400 to-teal-400">
                  침대에서
                </span>{" "}
                <span className="text-transparent bg-clip-text bg-gradient-to-r from-violet-400 to-purple-500">
                  &apos;딸깍&apos;
                </span>
              </h1>
              <p className="text-white/90 text-sm sm:text-base mb-8 leading-relaxed max-w-md mx-auto lg:mx-0">
                발품 없이 온라인 최저가 낙찰 클릭
                <br />한 번으로 배달되는 나만의 폰
              </p>
              <div className="flex flex-col sm:flex-row gap-3 justify-center lg:justify-start mb-0">
                <Link
                  to="/auctions/create"
                  className="w-full sm:w-auto bg-gradient-to-r from-violet-500 to-purple-700 hover:from-violet-600 hover:to-purple-800 text-white font-bold py-3.5 px-8 rounded-xl shadow-lg shadow-violet-500/30 transition transform active:scale-95 flex items-center justify-center gap-2"
                >
                  30초 만에 견적 요청하기
                  <MaterialIcon name="arrow_forward" className="text-lg" />
                </Link>
                <Link
                  to="/seller/signup"
                  className="w-full sm:w-auto bg-transparent border border-white/40 hover:border-white/70 text-white/90 hover:text-white font-medium py-3.5 px-8 rounded-xl transition"
                >
                  판매자 입점하기
                </Link>
              </div>
            </div>
            <div className="flex-1 relative w-full max-w-lg order-1 lg:order-2 flex items-center justify-center min-h-[420px]">
              {/* 배경 글로우 */}
              <div className="absolute inset-0 bg-violet-500/20 blur-3xl rounded-full pointer-events-none" />

              {/* 링 1 - 위쪽 호 (폰 뒤) */}
              <div
                className="absolute pointer-events-none"
                style={{
                  width: "540px",
                  height: "190px",
                  border: "1.5px solid rgba(129,140,248,0.75)",
                  borderRadius: "50%",
                  transform: "translate(-50%,-50%) rotate(18deg)",
                  clipPath: "polygon(0% 0%, 100% 0%, 100% 50%, 0% 50%)",
                  filter: "drop-shadow(0 0 6px rgba(99,102,241,0.9))",
                  top: "50%",
                  left: "50%",
                  zIndex: 5,
                }}
              />

              {/* 링 2 - 위쪽 호 (폰 뒤) */}
              <div
                className="absolute pointer-events-none"
                style={{
                  width: "440px",
                  height: "155px",
                  border: "1.5px solid rgba(192,132,252,0.65)",
                  borderRadius: "50%",
                  transform: "translate(-50%,-50%) rotate(-12deg)",
                  clipPath: "polygon(0% 0%, 100% 0%, 100% 50%, 0% 50%)",
                  filter: "drop-shadow(0 0 5px rgba(168,85,247,0.7))",
                  top: "50%",
                  left: "50%",
                  zIndex: 5,
                }}
              />

              {/* 폰 이미지 */}
              <div
                className="relative flex items-end justify-center w-full px-4"
                style={{ zIndex: 20 }}
              >
                <img
                  alt="Galaxy S26 Front"
                  className="w-[620px] lg:w-[900px] -rotate-[8deg] drop-shadow-2xl transition duration-500"
                  src="https://images.samsung.com/is/image/samsung/p6pim/us/s2602/gallery/us-galaxy-s26-s947-sm-s947uzvaxaa-550994937?fmt=png-alpha&wid=480"
                />
              </div>

              {/* 링 1 - 아래쪽 호 (폰 앞) */}
              <div
                className="absolute pointer-events-none"
                style={{
                  width: "540px",
                  height: "190px",
                  border: "2px solid rgba(34,211,238,0.9)",
                  borderRadius: "50%",
                  transform: "translate(-50%,-50%) rotate(18deg)",
                  clipPath: "polygon(0% 50%, 100% 50%, 100% 100%, 0% 100%)",
                  filter: "drop-shadow(0 0 8px rgba(34,211,238,1))",
                  top: "50%",
                  left: "50%",
                  zIndex: 35,
                }}
              />

              {/* 링 1 오른쪽 끝 빛나는 점 */}
              <div
                className="absolute pointer-events-none rounded-full"
                style={{
                  width: "10px",
                  height: "10px",
                  background: "#fff",
                  boxShadow:
                    "0 0 0 2px rgba(34,211,238,0.4), 0 0 12px 5px rgba(34,211,238,0.9)",
                  top: "calc(50% + 83px)",
                  left: "calc(50% + 257px)",
                  transform: "translate(-50%,-50%)",
                  zIndex: 40,
                }}
              />

              {/* 링 2 - 아래쪽 호 (폰 앞) */}
              <div
                className="absolute pointer-events-none"
                style={{
                  width: "440px",
                  height: "155px",
                  border: "2px solid rgba(167,139,250,0.85)",
                  borderRadius: "50%",
                  transform: "translate(-50%,-50%) rotate(-12deg)",
                  clipPath: "polygon(0% 50%, 100% 50%, 100% 100%, 0% 100%)",
                  filter: "drop-shadow(0 0 6px rgba(167,139,250,0.9))",
                  top: "50%",
                  left: "50%",
                  zIndex: 35,
                }}
              />

              {/* 링 2 오른쪽 끝 빛나는 점 */}
              <div
                className="absolute pointer-events-none rounded-full"
                style={{
                  width: "8px",
                  height: "8px",
                  background: "#fff",
                  boxShadow:
                    "0 0 0 2px rgba(167,139,250,0.4), 0 0 10px 4px rgba(167,139,250,0.9)",
                  top: "calc(50% - 44px)",
                  left: "calc(50% + 209px)",
                  transform: "translate(-50%,-50%)",
                  zIndex: 40,
                }}
              />

              {/* 실시간 입찰 floating card */}
              <div
                className="absolute bottom-4 right-0 lg:right-4 bg-white/10 backdrop-blur-xl border border-white/20 p-4 rounded-2xl shadow-2xl animate-bounce min-w-[400px]"
                style={{ animationDuration: "3s", zIndex: 50 }}
              >
                <div className="flex items-center justify-between mb-2">
                  <MaterialIcon name="bolt" className="text-blue-400 text-sm" />
                  <div className="flex items-center gap-1">
                    <MaterialIcon
                      name="local_fire_department"
                      className="text-orange-400 text-sm"
                    />
                    <span className="text-[10px] text-orange-400 font-bold">
                      실시간 입찰
                    </span>
                  </div>
                </div>
                <p className="text-sm text-white font-medium mb-1">
                  [서울-김OO님] S26 512GB ₩550,000 구매
                </p>
                <p className="text-[10px] text-slate-300 mb-3">5분 전</p>
              </div>
            </div>
          </div>
        </AuroraBackground>
      </header>

      {/* Ticker */}
      <section className="bg-[#0f172a] border-y border-slate-800 dark:bg-slate-900 dark:border-slate-800 py-3 overflow-hidden">
        <div className="flex gap-8 animate-marquee whitespace-nowrap">
          {[...TICKER_ITEMS, ...TICKER_ITEMS].map((item, i) => (
            <div key={i} className="flex items-center gap-2">
              <MaterialIcon
                name="local_offer"
                className="text-orange-500 text-sm"
              />
              <span className="text-xs text-slate-400">{item}</span>
            </div>
          ))}
        </div>
      </section>

      {/* Live Quotes Section */}
      <section className="py-16 bg-[#111827] dark:bg-slate-950 px-4">
        <div className="max-w-7xl mx-auto text-center mb-10">
          <h2 className="text-2xl font-bold text-white mb-2">
            전국에서 가장 싼 최저가
            <br />
            견적을 확인하세요
          </h2>
          <p className="text-slate-400 text-sm mb-6">
            1초마다 업데이트되는 실시간 가격 경쟁!
          </p>
          <div className="inline-flex items-center gap-2 px-3 py-1 bg-slate-800 rounded-full">
            <span className="w-2 h-2 bg-green-500 rounded-full animate-pulse" />
            <span className="text-xs text-slate-300">실시간 업데이트 중</span>
          </div>
        </div>
        <div className="max-w-7xl mx-auto flex overflow-x-auto gap-4 pb-8 px-2 snap-x snap-mandatory [&::-webkit-scrollbar]:hidden [-ms-overflow-style:none] [scrollbar-width:none] sm:grid sm:grid-cols-2 lg:grid-cols-4 sm:overflow-visible sm:justify-center">
          {LIVE_QUOTES.map((quote) => (
            <div
              key={quote.id}
              className="snap-center shrink-0 w-72 bg-slate-800/50 dark:bg-slate-900/80 border border-slate-700 rounded-2xl p-4 hover:border-violet-500/50 transition relative"
            >
              {quote.isLowest && (
                <div className="absolute -top-3 -right-3 bg-red-500 text-white text-[10px] font-bold px-2 py-1 rounded shadow-lg z-10">
                  최저가
                </div>
              )}
              <div className="flex items-center gap-3 mb-4">
                <div
                  className={`w-10 h-10 ${quote.badgeColor} rounded-lg flex items-center justify-center text-white font-bold text-xs shadow-lg ${quote.badgeShadow}`}
                >
                  {quote.sellerBadge}
                </div>
                <div>
                  <div className="flex items-center gap-1">
                    <h3 className="text-white font-bold text-sm">
                      {quote.sellerName}
                    </h3>
                    <MaterialIcon
                      name="verified"
                      className="text-green-500 text-xs"
                    />
                  </div>
                  <div className="flex items-center gap-1">
                    <span className="text-yellow-400 text-xs">
                      {"★".repeat(quote.stars)}
                      {"☆".repeat(5 - quote.stars)}
                    </span>
                    <span className="text-slate-400 text-[10px]">
                      {quote.rating}
                    </span>
                  </div>
                </div>
              </div>
              <div className="flex items-center gap-4 mb-4 bg-slate-900/50 p-3 rounded-xl">
                <img
                  alt={quote.productName}
                  className="w-10 h-14 object-contain"
                  src={quote.productImage}
                />
                <div>
                  <p className="text-white font-bold text-xs">
                    {quote.productName}
                  </p>
                  <p className="text-slate-500 text-[10px]">
                    {quote.productOption}
                  </p>
                </div>
              </div>
              <div className="flex justify-between items-end mb-4 border-b border-slate-700 pb-4">
                <div className="text-left">
                  <p className="text-slate-400 text-[10px]">할부원금</p>
                  <p className="text-slate-500 text-[10px]">선택약정</p>
                </div>
                <div className="text-right">
                  <p className="text-blue-400 font-bold text-lg">
                    {quote.installmentAmount}
                  </p>
                  <p className="text-slate-500 text-[10px]">
                    {quote.contractOption}
                  </p>
                </div>
              </div>
              <div className="flex gap-2 mb-4 flex-wrap">
                {quote.tags.map((tag) => (
                  <span
                    key={tag.label}
                    className={`px-2 py-0.5 text-[10px] rounded ${
                      tag.highlight
                        ? "bg-green-900/30 text-green-400 border border-green-900"
                        : "bg-slate-700 text-slate-300"
                    }`}
                  >
                    {tag.label}
                  </span>
                ))}
              </div>
              <Link
                to="/auctions/create"
                className={`block w-full py-2.5 rounded-xl font-bold text-sm text-center transition ${
                  quote.isLowest
                    ? "bg-violet-500/20 hover:bg-violet-500 text-violet-400 hover:text-white"
                    : "bg-slate-700 hover:bg-slate-600 text-white"
                }`}
              >
                선택하기
              </Link>
            </div>
          ))}
        </div>
        <div className="text-center mt-6">
          <p className="text-slate-500 text-xs mb-4">
            더 많은 견적이 실시간으로 업데이트됩니다
          </p>
          <Link
            to="/auctions/create"
            className="inline-block bg-violet-500 hover:bg-violet-600 text-white font-bold py-3 px-10 rounded-full text-sm shadow-lg shadow-violet-500/20 transition w-full sm:w-auto"
          >
            견적 받으러 가기
          </Link>
        </div>
      </section>

      {/* Purchase Flow Section */}
      <section className="py-20 bg-slate-50 dark:bg-slate-900 px-4">
        <div className="max-w-4xl mx-auto text-center mb-16">
          <div className="inline-block bg-violet-500/10 px-4 py-2 rounded-full mb-4">
            <div className="flex items-center gap-2 text-violet-500 font-medium text-xs">
              <MaterialIcon name="schedule" className="text-sm" />
              나의 시간은 소중하니까
            </div>
          </div>
          <h2 className="text-2xl sm:text-3xl font-bold text-slate-900 dark:text-white mb-4">
            빠르고 편리한 구매 경험
          </h2>
          <p className="text-slate-500 dark:text-slate-400 text-sm">
            복잡한 과정 없이,
            <br />
            견적 요청 한번으로 간편하게!
          </p>
        </div>
        <div className="grid grid-cols-2 lg:grid-cols-4 gap-4 max-w-6xl mx-auto">
          {PURCHASE_STEPS.map((step) => (
            <div
              key={step.step}
              className="bg-white dark:bg-slate-800 p-6 rounded-2xl shadow-lg shadow-slate-200/50 dark:shadow-none border border-slate-100 dark:border-slate-700 text-center flex flex-col items-center h-full"
            >
              <div className="w-12 h-12 rounded-xl bg-violet-500 text-white flex items-center justify-center font-bold text-lg mb-4 shadow-lg shadow-violet-500/30">
                {step.step}
              </div>
              <div
                className={`w-12 h-12 rounded-full flex items-center justify-center mb-4 ${step.iconColor}`}
              >
                <MaterialIcon name={step.icon} />
              </div>
              <h3 className="font-bold text-slate-900 dark:text-white mb-2 text-sm">
                {step.title}
              </h3>
              <p className="text-slate-400 text-[10px] mb-2">{step.subtitle}</p>
              <p className="text-slate-500 dark:text-slate-400 text-[11px] leading-relaxed whitespace-pre-line">
                {step.description}
              </p>
            </div>
          ))}
        </div>
      </section>

      {/* Safety Section */}
      <section className="py-16 bg-blue-50/50 dark:bg-[#161f32] px-4 border-t border-slate-200 dark:border-slate-800">
        <div className="max-w-4xl mx-auto text-center">
          <h2 className="text-2xl font-bold text-slate-900 dark:text-white mb-2">
            사기 위험 없는 안전한 거래 시스템
          </h2>
          <p className="text-slate-500 dark:text-slate-400 text-sm mb-10">
            철저한 판매자 검증 시스템으로,
            <br />
            안심하고 거래하세요
          </p>
          <div className="grid grid-cols-1 sm:grid-cols-3 gap-4">
            {SAFETY_ITEMS.map((item, i) => (
              <div
                key={i}
                className="bg-white dark:bg-slate-800 p-6 rounded-2xl shadow-sm border border-slate-100 dark:border-slate-700"
              >
                <div
                  className={`w-10 h-10 ${item.iconColor} rounded-lg flex items-center justify-center mx-auto mb-3`}
                >
                  <MaterialIcon name={item.icon} />
                </div>
                <h3 className="font-bold text-slate-900 dark:text-white text-sm mb-1">
                  {item.title}
                </h3>
                <p className="text-xs text-slate-400 whitespace-pre-line">
                  {item.description}
                </p>
              </div>
            ))}
          </div>
        </div>
      </section>

      {/* Launch Signup Section */}
      <section className="py-20 px-4 bg-gradient-to-b from-slate-800 to-slate-900 dark:from-slate-800 dark:to-slate-950 text-white">
        <div className="max-w-md mx-auto">
          <div className="text-center mb-10">
            <div className="inline-flex items-center gap-2 bg-violet-500 px-4 py-1.5 rounded-full mb-6">
              <span className="text-xs font-bold">🚀 2025년 3월 출시 예정</span>
            </div>
            <h2 className="text-3xl font-bold mb-4 leading-tight">
              핸드폰 구매의 새로운 시대가
              <br />
              <span className="text-violet-400">곧 시작됩니다</span>
            </h2>
            <p className="text-slate-400 text-sm">
              출시 알림을 신청하시면 오픈 소식과 특별 혜택을 가장 먼저 받아보실
              수 있어요
            </p>
          </div>
          <form
            onSubmit={handleLaunchSignup}
            className="bg-slate-800/50 dark:bg-slate-900/50 border border-slate-700 p-6 rounded-2xl backdrop-blur-sm shadow-2xl"
          >
            <div className="space-y-4">
              <div>
                <label className="block text-slate-400 text-xs mb-1 ml-1">
                  이름
                </label>
                <input
                  type="text"
                  value={name}
                  onChange={(e) => setName(e.target.value)}
                  placeholder="홍길동"
                  className="w-full bg-slate-900/80 border border-slate-700 rounded-xl px-4 py-3 text-sm text-white placeholder-slate-600 focus:outline-none focus:border-violet-500 transition"
                />
              </div>
              <div>
                <label className="block text-slate-400 text-xs mb-1 ml-1">
                  연락처
                </label>
                <input
                  type="tel"
                  value={phone}
                  onChange={(e) => setPhone(e.target.value)}
                  placeholder="010-1234-5678"
                  className="w-full bg-slate-900/80 border border-slate-700 rounded-xl px-4 py-3 text-sm text-white placeholder-slate-600 focus:outline-none focus:border-violet-500 transition"
                />
              </div>
              <div className="flex items-start gap-2 pt-2">
                <input
                  type="checkbox"
                  id="privacy"
                  checked={agreePrivacy}
                  onChange={(e) => setAgreePrivacy(e.target.checked)}
                  className="mt-0.5 rounded border-slate-600 bg-slate-800 text-violet-500 focus:ring-offset-slate-900"
                />
                <label
                  htmlFor="privacy"
                  className="text-[10px] text-slate-400 leading-tight cursor-pointer select-none"
                >
                  개인정보 수집 및 이용에 동의합니다.
                  <br />
                  <span className="text-slate-500">전문보기</span>
                </label>
              </div>
              <button
                type="submit"
                className="w-full bg-violet-500 hover:bg-violet-600 text-white font-bold py-3.5 rounded-xl shadow-lg shadow-violet-500/30 transition mt-4 flex items-center justify-center gap-2 group"
              >
                <MaterialIcon
                  name="notifications_active"
                  className="text-sm group-hover:animate-bell-ring"
                />
                출시 알림 신청하기
              </button>
            </div>
            <div className="mt-8 pt-6 border-t border-slate-700/50 text-center">
              <p className="text-slate-400 text-xs mb-3">
                혹시 입점을 희망하시는 대리점인가요?
              </p>
              <Link
                to="/seller/signup"
                className="inline-flex items-center gap-2 bg-slate-700/50 hover:bg-slate-700 text-slate-300 text-xs px-4 py-2 rounded-lg transition"
              >
                <MaterialIcon name="store" className="text-sm" />
                입점 신청하기
              </Link>
            </div>
          </form>
        </div>
      </section>

      {/* Footer */}
      <footer className="bg-black text-slate-500 py-12 px-4 text-[10px] leading-relaxed border-t border-slate-900">
        <div className="max-w-7xl mx-auto grid grid-cols-1 md:grid-cols-2 gap-8">
          <div>
            <h4 className="text-violet-500 font-bold text-sm mb-4">
              사업자 정보
            </h4>
            <div className="space-y-1">
              <p>
                <span className="font-bold text-slate-400">상호명:</span>{" "}
                (주)폰딜
              </p>
              <p>
                <span className="font-bold text-slate-400">대표자:</span> 김폰딜
              </p>
              <p>
                <span className="font-bold text-slate-400">
                  사업자등록번호:
                </span>{" "}
                123-45-67890
              </p>
              <p>
                <span className="font-bold text-slate-400">
                  통신판매업신고번호:
                </span>{" "}
                제2024-서울강남-1234호
              </p>
            </div>
          </div>
          <div>
            <h4 className="text-violet-500 font-bold text-sm mb-4">고객센터</h4>
            <div className="space-y-1">
              <p>
                <span className="font-bold text-slate-400">주소:</span>{" "}
                서울특별시 강남구 테헤란로 123, 456호
              </p>
              <p>
                <span className="font-bold text-slate-400">대표번호:</span>{" "}
                1588-1234
              </p>
              <p>
                <span className="font-bold text-slate-400">이메일:</span>{" "}
                help@phonedeal.co.kr
              </p>
              <p>
                <span className="font-bold text-slate-400">
                  개인정보보호책임자:
                </span>{" "}
                이보호 (privacy@phonedeal.co.kr)
              </p>
            </div>
          </div>
        </div>
        <div className="max-w-7xl mx-auto mt-8 pt-8 border-t border-slate-900 flex flex-col md:flex-row justify-between items-center gap-4">
          <p>© 2024 PhoneDeal. All rights reserved.</p>
          <div className="flex gap-4">
            <a
              href="#"
              className="text-slate-400 hover:text-white transition"
              aria-label="Facebook"
            >
              <MaterialIcon name="facebook" className="text-sm" />
            </a>
            <a
              href="#"
              className="text-slate-400 hover:text-white transition"
              aria-label="YouTube"
            >
              <MaterialIcon name="smart_display" className="text-sm" />
            </a>
            <a
              href="#"
              className="text-slate-400 hover:text-white transition"
              aria-label="Video"
            >
              <MaterialIcon name="ondemand_video" className="text-sm" />
            </a>
          </div>
        </div>
      </footer>
    </div>
  );
};

export default LandingPage;
