import { useEffect } from "react";

const AuctionListPage: React.FC = () => {
  useEffect(() => {
    document.title = "경매 목록 | PhoneBid";
  }, []);

  return (
    <div className="min-h-[60vh] bg-background">
      <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-10">
        <div className="mb-8">
          <h1 className="text-2xl sm:text-3xl font-bold tracking-tight text-foreground">
            경매 목록
          </h1>
          <p className="mt-2 text-sm text-muted-foreground">
            최신 등록된 견적에 판매자들이 입찰한 목록을 확인하세요.
          </p>
        </div>

        {/* 필터 영역 (목업) */}
        <div className="grid grid-cols-1 md:grid-cols-4 gap-4 mb-8">
          <select className="border border-input rounded-md h-10 px-3 bg-white text-sm">
            <option>기종 전체</option>
          </select>
          <select className="border border-input rounded-md h-10 px-3 bg-white text-sm">
            <option>통신사 전체</option>
          </select>
          <select className="border border-input rounded-md h-10 px-3 bg-white text-sm">
            <option>상태 전체</option>
          </select>
          <select className="border border-input rounded-md h-10 px-3 bg-white text-sm">
            <option>정렬: 최신순</option>
          </select>
        </div>

        {/* 카드 리스트 (목업) */}
        <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-3 gap-6">
          {Array.from({ length: 6 }).map((_, idx) => (
            <div
              key={idx}
              className="rounded-lg border border-border bg-card p-5 shadow-sm hover:shadow transition-shadow"
            >
              <div className="flex items-start justify-between">
                <div>
                  <h3 className="text-base font-semibold text-foreground">
                    iPhone 16 · 128GB · 블랙
                  </h3>
                  <p className="text-sm text-muted-foreground">
                    SKT · 신규/번이
                  </p>
                </div>
                <span className="text-xs px-2 py-1 rounded bg-secondary text-secondary-foreground">
                  OPEN
                </span>
              </div>
              <div className="mt-4 flex items-center justify-between text-sm">
                <div className="text-muted-foreground">입찰 5건</div>
                <div className="font-semibold text-primary-600">
                  최저 1,200,000원
                </div>
              </div>
              <div className="mt-2 text-xs text-muted-foreground">마감 D-1</div>
            </div>
          ))}
        </div>
      </div>
    </div>
  );
};

export default AuctionListPage;
