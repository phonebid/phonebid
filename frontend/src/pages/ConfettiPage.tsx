import { useEffect } from "react";
import { Confetti } from "@/components/ui/confetti";

const ConfettiTestPage = () => {
  useEffect(() => {
    // 페이지 로드 시 confetti 실행
    const timer = setTimeout(() => {
      // 기본 confetti 효과
      const confetti = (window as any).confetti;
      if (confetti) {
        confetti({
          particleCount: 100,
          spread: 70,
          origin: { y: 0.6 },
        });
      }
    }, 500); // 0.5초 후 실행

    return () => clearTimeout(timer);
  }, []);

  return (
    <div className="min-h-screen bg-background flex items-center justify-center">
      <div className="text-center">
        <h1 className="text-6xl font-bold text-foreground mb-4">🎉 Phonebid</h1>
        <p className="text-muted-foreground text-xl">가입이 완료되었습니다.</p>
      </div>

      {/* 전체 화면 confetti */}
      <Confetti
        className="absolute left-0 top-0 z-0 size-full"
        manualstart={false}
        options={{
          particleCount: 150,
          spread: 90,
          origin: { y: 0.7 },
        }}
      />
    </div>
  );
};

export default ConfettiTestPage;
