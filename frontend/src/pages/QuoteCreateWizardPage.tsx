import { useEffect, useMemo, useState } from "react";
import { useNavigate } from "react-router-dom";
import { useQuoteCreateStore } from "store/quoteCreateStore";
import type { PurchaseMethod } from "types/QuoteTypes";
import { Button } from "@/components/ui/button";
import { Badge } from "@/components/ui/badge";
import {
  Card,
  CardContent,
  CardDescription,
  CardHeader,
  CardTitle,
} from "@/components/ui/card";
import { SelectionCard } from "@/components/ui/selection-card";
import { cn } from "@/utils/cn";

interface PurchasePlanOption {
  title: string;
  description: string;
  value: PurchaseMethod;
}

interface PhoneOption {
  id: string;
  title: string;
  price: number;
  originalPrice: number;
  discountText: string;
}

const purchasePlanOptions: PurchasePlanOption[] = [
  {
    title: "기기만 변경할게요.",
    description: "기기만 변경할 경우, 번호는와 통신사는 그대로 유지 됩니다.",
    value: "DEVICE_CHANGE",
  },
  {
    title: "통신사도 바꿀게요.",
    description: "통신사를 바꾸면 기기 가격이 저렴해질 가능성이 높아집니다.",
    value: "NUMBER_TRANSFER",
  },
];

const phoneOptions: PhoneOption[] = [
  {
    id: "iphone_16_pro",
    title: "아이폰 16프로",
    price: 730000,
    originalPrice: 1250000,
    discountText: "최대 52만 원 할인",
  },
  {
    id: "galaxy_25",
    title: "갤럭시 25",
    price: 730000,
    originalPrice: 1250000,
    discountText: "최대 52만 원 할인",
  },
  {
    id: "iphone_16",
    title: "아이폰 16",
    price: 690000,
    originalPrice: 1090000,
    discountText: "최대 40만 원 할인",
  },
  {
    id: "galaxy_fold",
    title: "갤럭시 Z 폴드",
    price: 1200000,
    originalPrice: 1990000,
    discountText: "최대 79만 원 할인",
  },
];

const QuoteCreateWizardPage: React.FC = () => {
  const navigate = useNavigate();
  const { draft, updateDraft, reset, setStep } = useQuoteCreateStore();
  const [step, setLocalStep] = useState(1);

  useEffect(() => {
    document.title = "견적 작성 | PhoneBid";
  }, []);

  useEffect(() => {
    reset();
    setLocalStep(1);
    setStep(1);
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, []);

  useEffect(() => {
    setStep(step);
  }, [setStep, step]);

  const isComplete = useMemo(() => {
    if (step === 1) {
      return Boolean(draft.purchaseMethod);
    }
    if (step === 2) {
      return Boolean(draft.model);
    }
    return true;
  }, [draft.model, draft.purchaseMethod, step]);

  const handleSelectPlan = (value: PurchaseMethod) => {
    updateDraft({ purchaseMethod: value });
  };

  const handleSelectPhone = (option: PhoneOption) => {
    updateDraft({ model: option.title });
  };

  const goPrev = () => {
    if (step === 1) {
      navigate(-1);
      return;
    }
    setLocalStep((prev) => Math.max(1, prev - 1));
  };

  const goNext = () => {
    if (!isComplete) {
      return;
    }
    if (step === 3) {
      // TODO: 생성 API 연동
      navigate("/auctions");
      return;
    }
    setLocalStep((prev) => Math.min(3, prev + 1));
  };

  const renderStep = () => {
    if (step === 1) {
      return (
        <section className="space-y-5">
          <h2 className="text-2xl font-semibold tracking-tight">
            어떻게 구매할 계획인가요?
          </h2>
          <div className="space-y-3">
            {purchasePlanOptions.map((option) => (
              <SelectionCard
                key={option.value}
                title={option.title}
                description={option.description}
                selected={draft.purchaseMethod === option.value}
                onClick={() => handleSelectPlan(option.value)}
              />
            ))}
          </div>
        </section>
      );
    }

    if (step === 2) {
      return (
        <section className="space-y-5">
          <h2 className="text-2xl font-semibold tracking-tight">
            기종을 선택하세요.
          </h2>
          <div className="space-y-3">
            {phoneOptions.map((option) => {
              const selected = draft.model === option.title;
              return (
                <Card
                  key={option.id}
                  role="button"
                  tabIndex={0}
                  onClick={() => handleSelectPhone(option)}
                  onKeyDown={(event) => {
                    if (event.key === "Enter" || event.key === " ") {
                      event.preventDefault();
                      handleSelectPhone(option);
                    }
                  }}
                  className={cn(
                    "flex items-center gap-4 rounded-2xl border transition-colors",
                    selected
                      ? "border-primary bg-primary/5 shadow-sm"
                      : "bg-white"
                  )}
                >
                  <CardContent className="flex w-full items-center gap-4 p-4">
                    <div className="flex h-20 w-20 items-center justify-center rounded-xl bg-gradient-to-br from-primary/10 via-primary/20 to-primary/10 text-sm font-medium text-primary-foreground">
                      {option.title.slice(0, 2)}
                    </div>
                    <div className="flex flex-1 flex-col">
                      <div className="flex items-start justify-between">
                        <div className="space-y-1">
                          <CardTitle className="text-base font-semibold">
                            {option.title}
                          </CardTitle>
                          <div className="text-xs text-muted-foreground line-through">
                            {option.originalPrice.toLocaleString()}원
                          </div>
                          <Badge variant="secondary" className="w-fit">
                            {option.discountText}
                          </Badge>
                        </div>
                        <div
                          className={cn(
                            "mt-1 h-6 w-6 rounded-full border",
                            selected
                              ? "border-primary bg-primary"
                              : "border-border"
                          )}
                        />
                      </div>
                      <div className="mt-3 text-lg font-semibold text-foreground">
                        {option.price.toLocaleString()}원
                      </div>
                      <div className="text-xs text-muted-foreground">
                        칠십삼만 원부터
                      </div>
                    </div>
                  </CardContent>
                </Card>
              );
            })}
          </div>
        </section>
      );
    }

    return (
      <section className="space-y-6">
        <h2 className="text-2xl font-semibold tracking-tight">
          선택 내역을 확인하세요.
        </h2>
        <div className="space-y-3">
          <SummaryCard
            label="구매 계획"
            value={getPurchasePlanLabel(draft.purchaseMethod)}
          />
          <SummaryCard
            label="선택한 기종"
            value={draft.model ?? "선택되지 않음"}
          />
        </div>
        <p className="text-sm text-muted-foreground">
          경매 시작하기 버튼을 누르면 지금까지 선택한 정보를 바탕으로 견적이
          생성됩니다.
        </p>
      </section>
    );
  };

  return (
    <div className="min-h-screen bg-[#F6F7FB] pb-[max(120px,env(safe-area-inset-bottom)+80px)]">
      <div className="mx-auto flex h-14 w-full max-w-md items-center gap-3 px-4">
        <Button
          type="button"
          variant="ghost"
          size="icon"
          className="rounded-full bg-white shadow"
          onClick={() => navigate(-1)}
          aria-label="뒤로가기"
        >
          <ArrowLeftIcon />
        </Button>
        <span className="text-base font-semibold">견적 작성</span>
      </div>
      <main className="mx-auto mt-6 w-full max-w-md space-y-6 px-4">
        {renderStep()}
      </main>
      <StepFooter
        isFirstStep={step === 1}
        onPrev={goPrev}
        onNext={goNext}
        disabled={!isComplete}
        nextLabel={step === 3 ? "경매 시작하기" : "다음"}
      />
    </div>
  );
};

const StepFooter = ({
  onPrev,
  onNext,
  disabled,
  isFirstStep,
  nextLabel,
}: {
  onPrev: () => void;
  onNext: () => void;
  disabled: boolean;
  isFirstStep: boolean;
  nextLabel: string;
}) => {
  return (
    <div className="pointer-events-none fixed inset-x-0 bottom-0 z-30">
      <div className="pointer-events-auto mx-auto flex w-full max-w-md gap-3 px-4 pb-[max(24px,env(safe-area-inset-bottom)+12px)]">
        <Button
          type="button"
          variant="outline"
          onClick={onPrev}
          disabled={isFirstStep}
          className="flex-1 rounded-2xl"
        >
          이전
        </Button>
        <Button
          type="button"
          onClick={onNext}
          disabled={disabled}
          className="flex-1 rounded-2xl"
        >
          {nextLabel}
        </Button>
      </div>
    </div>
  );
};

const SummaryCard = ({ label, value }: { label: string; value?: string }) => (
  <Card className="rounded-2xl">
    <CardHeader className="p-4">
      <CardTitle className="text-sm font-medium text-muted-foreground">
        {label}
      </CardTitle>
      <CardDescription className="text-base font-semibold text-foreground">
        {value ?? ""}
      </CardDescription>
    </CardHeader>
  </Card>
);

const getPurchasePlanLabel = (value?: PurchaseMethod) => {
  if (value === "DEVICE_CHANGE") {
    return "기기만 변경";
  }
  if (value === "NUMBER_TRANSFER") {
    return "통신사 변경";
  }
  return "선택되지 않음";
};

const ArrowLeftIcon = () => (
  <svg
    width="20"
    height="20"
    viewBox="0 0 24 24"
    fill="none"
    xmlns="http://www.w3.org/2000/svg"
  >
    <path
      d="M15 6L9 12L15 18"
      stroke="currentColor"
      strokeWidth="2"
      strokeLinecap="round"
      strokeLinejoin="round"
    />
  </svg>
);

export default QuoteCreateWizardPage;
