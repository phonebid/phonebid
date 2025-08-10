import { useEffect } from "react";
import { Link } from "react-router-dom";
import RankingCard from "components/ranking/RankingCard";

const WeeklyRankingPage: React.FC = () => {
  useEffect(() => {
    document.title = "이번 주 랭킹 | PhoneBid";
  }, []);

  const rankingMock = [
    {
      modelName: "아이폰 16프로",
      originalPrice: 1250000,
      discountText: "최대 52만 원 할인",
      price: 730000,
      helperText: "월실사용 8만 원부터",
    },
    {
      modelName: "갤럭시 25",
      originalPrice: 1250000,
      discountText: "최대 52만 원 할인",
      price: 730000,
      helperText: "월실사용 8만 원부터",
    },
    {
      modelName: "아이폰 16프로",
      originalPrice: 1250000,
      discountText: "최대 52만 원 할인",
      price: 730000,
      helperText: "월실사용 8만 원부터",
    },
    {
      modelName: "갤럭시 25",
      originalPrice: 1250000,
      discountText: "최대 52만 원 할인",
      price: 730000,
      helperText: "월실사용 8만 원부터",
    },
  ];

  return (
    <div className="bg-background min-h-[60vh] pb-24">
      {/* CTA 공간 확보 */}
      <div className="max-w-md mx-auto px-4 py-6 space-y-4">
        {/* 모바일 퍼스트 폭 */}

        {/* 후기 카드 */}
        <div className="rounded-xl bg-gray-50 p-4">
          <div className="text-sm text-foreground leading-6">
            <span className="font-medium">SKT 번호 이동으로</span>
            <br />
            폴드7을{" "}
            <span className="text-indigo-500 font-extrabold underline underline-offset-4">
              97만 원
            </span>
            에 구입했어요!
          </div>
          <div className="mt-2 text-xs text-muted-foreground">-홍길동-</div>
        </div>

        {/* 섹션 타이틀 */}
        <h2 className="text-xl font-bold text-foreground mb-4">이번 주 랭킹</h2>

        {/* 랭킹 카드 리스트 */}
        <div className="space-y-3">
          {rankingMock.map((item, idx) => (
            <RankingCard key={idx} {...item} />
          ))}
        </div>
      </div>

      {/* 하단 고정 CTA (떠있는 느낌, 컨텐츠가 보이도록 투명 배경/여백) */}
      <div className="fixed inset-x-0 bottom-0 z-40 pointer-events-none">
        <div className="max-w-md mx-auto px-4 pb-[max(20px,env(safe-area-inset-bottom))]">
          <Link
            to="/auctions/create"
            className="pointer-events-auto block w-full text-center rounded-xl py-3 text-sm font-medium text-white bg-black "
          >
            핸드폰 가격 알아보기
          </Link>
        </div>
      </div>
    </div>
  );
};

export default WeeklyRankingPage;
