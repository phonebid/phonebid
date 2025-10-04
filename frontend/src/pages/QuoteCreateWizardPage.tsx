import { useEffect, useMemo, useState } from "react";
import { useNavigate } from "react-router-dom";
import { useQuoteCreateStore } from "store/quoteCreateStore";
import type { PurchaseMethod, Carrier } from "types/QuoteTypes";
import { Button } from "@/components/ui/button";
import {
  Card,
  CardContent,
  CardDescription,
  CardHeader,
  CardTitle,
} from "@/components/ui/card";
import { SelectionCard } from "@/components/ui/selection-card";

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

const colorOptions = [
  { id: "pink", title: "핑크" },
  { id: "black", title: "블랙" },
  { id: "sand", title: "샌드" },
  { id: "no-preference", title: "컬러 상관없이 최저가를 보고싶어요" },
];

const storageOptions = [
  { id: "64gb", title: "64GB" },
  { id: "128gb", title: "128GB" },
  { id: "256gb", title: "256GB" },
];

const carrierOptions: { id: Carrier; title: string }[] = [
  { id: "SKT", title: "SKT" },
  { id: "KT", title: "KT" },
  { id: "LGU+", title: "LG U+" },
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
    if (step === 3) {
      return Boolean(draft.color);
    }
    if (step === 4) {
      return Boolean(draft.storage);
    }
    if (step === 5) {
      return Boolean(draft.carrier);
    }
    return true;
  }, [
    draft.model,
    draft.purchaseMethod,
    draft.color,
    draft.storage,
    draft.carrier,
    step,
  ]);

  const handleSelectPlan = (value: PurchaseMethod) => {
    updateDraft({ purchaseMethod: value });
  };

  const handleSelectPhone = (option: PhoneOption) => {
    updateDraft({ model: option.title });
  };

  const handleSelectColor = (color: string) => {
    updateDraft({ color });
  };

  const handleSelectStorage = (storage: string) => {
    updateDraft({ storage });
  };

  const handleSelectCarrier = (carrier: Carrier) => {
    updateDraft({ carrier });
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
    if (step === 6) {
      // TODO: 생성 API 연동
      navigate("/auctions");
      return;
    }
    setLocalStep((prev) => Math.min(6, prev + 1));
  };

  const renderStep = () => {
    if (step === 1) {
      return (
        <section className="space-y-5">
          <h2 className="text-2xl flex items-center font-bold tracking-tight mb-8">
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
          <h2 className="text-2xl flex items-center font-bold tracking-tight mb-8">
            기종을 선택하세요.
          </h2>
          <div className="space-y-3">
            {phoneOptions.map((option) => {
              const selected = draft.model === option.title;
              return (
                <SelectionCard
                  key={option.id}
                  selected={selected}
                  onClick={() => handleSelectPhone(option)}
                  className="py-2"
                  showCheckIcon={true}
                >
                  <CardContent className="flex w-full items-center gap-4 py-2">
                    <div className="flex h-20 w-20 items-center justify-center rounded-xl bg-gradient-to-br from-primary/10 via-primary/20 to-primary/10 text-sm font-medium text-primary-foreground">
                      {option.title.slice(0, 2)}
                    </div>
                    <div className="flex flex-1 flex-col">
                      <div className="flex items-start justify-between">
                        <div className="space-y-1">
                          <CardTitle className="text-sm font-normal mt-2">
                            {option.title}
                          </CardTitle>
                          <div className="flex items-center gap-1">
                            <div className="text-xs text-muted-foreground line-through">
                              {option.originalPrice.toLocaleString()}원
                            </div>
                            <div className="text-xs text-primary">
                              {option.discountText}
                            </div>
                          </div>
                        </div>
                      </div>

                      <div className="text-lg font-semibold text-foreground">
                        {option.price.toLocaleString()}원
                      </div>
                      <div className="text-xs text-muted-foreground">
                        칠십삼만 원부터
                      </div>
                    </div>
                  </CardContent>
                </SelectionCard>
              );
            })}
          </div>
        </section>
      );
    }

    if (step === 3) {
      return (
        <section className="space-y-5">
          <div className="space-y-5">
            <div className="flex items-center gap-2">
              <div className="items-center gap-2">
                <h2 className="text-2xl font-bold tracking-tight">
                  {draft.model}
                </h2>
                <p className="text-sm text-muted-foreground"></p>
              </div>
              <img
                src={`https://picsum.photos/200/300`}
                alt={draft.model}
                className="flex h-20 w-20 items-center justify-center rounded-xl bg-gradient-to-br from-primary/10 via-primary/20 to-primary/10 text-sm font-medium text-primary-foreground ml-auto"
              />
            </div>
            <p className="text-base font-semibold">색상</p>
          </div>
          <div className="space-y-3">
            {colorOptions.map((option) => (
              <SelectionCard
                key={option.id}
                title={option.title}
                selected={draft.color === option.title}
                onClick={() => handleSelectColor(option.title)}
                className="py-4"
              />
            ))}
          </div>
        </section>
      );
    }

    if (step === 4) {
      return (
        <section className="space-y-5">
          <div className="space-y-5">
            <div className="flex items-center gap-2">
              <div className="items-center gap-2">
                <h2 className="text-2xl font-bold tracking-tight">
                  {draft.model}
                </h2>
                <p className="text-sm text-muted-foreground">{draft.color}</p>
              </div>
              <img
                src={`https://picsum.photos/200/300`}
                alt={draft.model}
                className="flex h-20 w-20 items-center justify-center rounded-xl bg-gradient-to-br from-primary/10 via-primary/20 to-primary/10 text-sm font-medium text-primary-foreground ml-auto"
              />
            </div>
            <p className="text-base font-semibold">용량</p>
          </div>
          <div className="space-y-3">
            {storageOptions.map((option) => (
              <SelectionCard
                key={option.id}
                title={option.title}
                selected={draft.storage === option.title}
                onClick={() => handleSelectStorage(option.title)}
                className="py-4"
              />
            ))}
          </div>
        </section>
      );
    }

    if (step === 5) {
      return (
        <section className="space-y-5">
          <div className="space-y-5">
            <div className="flex items-center gap-2">
              <div className="items-center gap-2">
                <h2 className="text-2xl font-bold tracking-tight">
                  {draft.model}
                </h2>
                <p className="text-sm text-muted-foreground">
                  {draft.color}, {draft.storage}
                </p>
              </div>
              <img
                src={`https://picsum.photos/200/300`}
                alt={draft.model}
                className="flex h-20 w-20 items-center justify-center rounded-xl bg-gradient-to-br from-primary/10 via-primary/20 to-primary/10 text-sm font-medium text-primary-foreground ml-auto"
              />
            </div>
            <p className="text-base font-semibold">통신사</p>
          </div>
          <div className="space-y-3">
            {carrierOptions.map((option) => (
              <SelectionCard
                key={option.id}
                title={option.title}
                selected={draft.carrier === option.id}
                onClick={() => handleSelectCarrier(option.id)}
                className="py-4"
              />
            ))}
          </div>
        </section>
      );
    }

    return (
      <section className="space-y-6">
        <h2 className=" font-bold tracking-tight mb-8">
          아래 내용으로 가격을 받아올게요. <br />
          마지막으로 견적을 확인하세요.
        </h2>
        <div className="space-y-5">
          <div className="flex items-center gap-2">
            <div className="items-center gap-2">
              <h2 className="text-2xl font-bold tracking-tight">
                {draft.model}
              </h2>
            </div>
            <img
              src={`https://picsum.photos/200/300`}
              alt={draft.model}
              className="flex h-20 w-20 items-center justify-center rounded-xl bg-gradient-to-br from-primary/10 via-primary/20 to-primary/10 text-sm font-medium text-primary-foreground ml-auto"
            />
          </div>
        </div>
        <div className="space-y-3">
          <SummaryCard
            label="구매 계획"
            value={getPurchasePlanLabel(draft.purchaseMethod)}
          />
          <SummaryCard
            label="선택한 기종"
            value={draft.model ?? "선택되지 않음"}
          />
          <SummaryCard label="색상" value={draft.color ?? "선택되지 않음"} />
          <SummaryCard label="용량" value={draft.storage ?? "선택되지 않음"} />
          <SummaryCard
            label="통신사"
            value={draft.carrier ?? "선택되지 않음"}
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
    <div className="min-h-screen bg-background">
      <div className="grid grid-cols-3 items-center h-14 px-4 max-w-md mx-auto">
        <Button
          className="flex justify-center"
          type="button"
          variant="ghost"
          size="icon"
          onClick={() => navigate(-1)}
          aria-label="뒤로가기"
        >
          <ArrowLeftIcon />
        </Button>
        <div className="flex justify-center text-base font-semibold">
          견적 작성
        </div>
      </div>
      <main className="mx-auto w-full max-w-md space-y-6 px-8 mt-6">
        {renderStep()}
      </main>
      <StepFooter
        isFirstStep={step === 1}
        onPrev={goPrev}
        onNext={goNext}
        disabled={!isComplete}
        nextLabel={step === 6 ? "경매 시작하기" : "다음"}
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
          className="w-32 justify-center items-center rounded-2xl "
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
  <Card className="bg-slate-50 rounded-2xl py-1">
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
