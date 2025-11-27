import { useEffect, useMemo, useState } from "react";
import useSWR from "swr";
import { useNavigate } from "react-router-dom";
import { useQuoteCreateStore } from "store/quoteCreateStore";
import type {
  PurchaseMethod,
  Carrier,
  ActivationMethod,
} from "types/QuoteTypes";
import { getPhoneModels } from "services/phoneModelService";
import type {
  PhoneModelResponse,
  PhoneOptionResponse,
} from "types/PhoneModelTypes";
import { Button } from "@/components/ui/button";
import {
  Card,
  CardContent,
  CardDescription,
  CardHeader,
  CardTitle,
} from "@/components/ui/card";
import { SelectionCard } from "@/components/ui/selection-card";
import { Skeleton } from "@/components/ui/skeleton";
import { createQuote } from "@/services/quoteService";
import { toast } from "react-toastify";

interface PurchasePlanOption {
  title: string;
  description: string;
  value: PurchaseMethod;
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
  {
    title: "최저가로 구매할게요.",
    description: "기기 변경, 통신사 변경 상관 없이 최저가로 구매합니다.",
    value: "LOWEST_PRICE",
  },
];

const carrierOptions: { id: Carrier; title: string }[] = [
  { id: "SKT", title: "SKT" },
  { id: "KT", title: "KT" },
  { id: "LGU", title: "LG U+" },
];

const QuoteCreateWizardPage: React.FC = () => {
  const navigate = useNavigate();
  const { draft, updateDraft, reset, setStep } = useQuoteCreateStore();
  const [step, setLocalStep] = useState(1);
  const [showSuccess, setShowSuccess] = useState(false);
  const {
    data: phoneModels,
    isLoading: isLoadingPhoneModels,
    error: phoneModelError,
  } = useSWR<PhoneModelResponse[]>("/phone/models", () => getPhoneModels());

  const selectedModel = useMemo(() => {
    if (!phoneModels || !draft.model) {
      return undefined;
    }
    return phoneModels.find((model) => model.id === draft.model);
  }, [draft.model, phoneModels]);

  const selectedModelLabel = selectedModel?.model ?? draft.model;

  const colorOptions = useMemo(() => {
    if (!selectedModel?.options) {
      return [] as PhoneOptionResponse[];
    }
    return selectedModel.options.filter(
      (option) => option.optionType === "COLOR"
    );
  }, [selectedModel]);

  const storageOptions = useMemo(() => {
    if (!selectedModel?.options) {
      return [] as PhoneOptionResponse[];
    }
    return selectedModel.options.filter(
      (option) => option.optionType === "STORAGE"
    );
  }, [selectedModel]);

  const hasColorOptions = colorOptions.length > 0;
  const hasStorageOptions = storageOptions.length > 0;

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
      // 색상 옵션이 있으면 항상 true (상관없음 선택 가능)
      return true;
    }
    if (step === 4) {
      // 용량 옵션이 있으면 항상 true (상관없음 선택 가능)
      return true;
    }
    if (step === 5) {
      return true;
    }
    return true;
  }, [
    draft.model,
    draft.purchaseMethod,
    draft.color,
    draft.storage,
    draft.carrier,
    step,
    hasColorOptions,
    hasStorageOptions,
  ]);

  const handleSelectPlan = (value: PurchaseMethod) => {
    updateDraft({ purchaseMethod: value });
  };

  const handleSelectPhone = (model: PhoneModelResponse) => {
    updateDraft({
      model: model.id,
      color: undefined,
      storage: undefined,
    });
  };

  const handleSelectColor = (option: PhoneOptionResponse) => {
    updateDraft({
      color: option,
    });
  };

  const handleSelectStorage = (option: PhoneOptionResponse) => {
    updateDraft({
      storage: option,
    });
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
      createQuote({
        phoneModelId: draft.model ?? "",
        storageOptionId: draft.storage?.id ?? "",
        colorOptionId: draft.color?.id ?? "",
        carrier: draft.carrier as Carrier,
        purchaseMethod: draft.purchaseMethod as PurchaseMethod,
        activationMethod: draft.activationMethod as ActivationMethod,
        currentCarrier: draft.currentCarrier as Carrier,
      })
        .then(() => {
          setShowSuccess(true);
        })
        .catch((error) => {
          toast.error(error.message);
        });
    }
    setLocalStep((prev) => Math.min(6, prev + 1));
  };

  const handleGoToProfile = () => {
    navigate("/consumer/quotes");
  };

  const handleGoHome = () => {
    navigate("/");
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
      if (isLoadingPhoneModels) {
        return (
          <section className="space-y-5">
            <h2 className="text-2xl flex items-center font-bold tracking-tight mb-8">
              기종을 선택하세요.
            </h2>
            <div className="space-y-3">
              {Array.from({ length: 3 }).map((_, index) => (
                <Card key={index} className="rounded-2xl">
                  <CardContent className="flex gap-4 py-4">
                    <Skeleton className="h-16 w-16 rounded-xl" />
                    <div className="flex-1 space-y-3">
                      <Skeleton className="h-4 w-24" />
                      <Skeleton className="h-4 w-40" />
                      <Skeleton className="h-4 w-28" />
                    </div>
                  </CardContent>
                </Card>
              ))}
            </div>
          </section>
        );
      }

      if (phoneModelError) {
        return (
          <section className="space-y-5">
            <h2 className="text-2xl flex items-center font-bold tracking-tight mb-8">
              기종을 선택하세요.
            </h2>
            <Card className="rounded-2xl border-destructive/40 bg-destructive/5">
              <CardContent className="py-6 text-sm text-destructive">
                기종 정보를 불러오지 못했습니다. 잠시 후 다시 시도해주세요.
              </CardContent>
            </Card>
          </section>
        );
      }

      if (!phoneModels || phoneModels.length === 0) {
        return (
          <section className="space-y-5">
            <h2 className="text-2xl flex items-center font-bold tracking-tight mb-8">
              기종을 선택하세요.
            </h2>
            <Card className="rounded-2xl">
              <CardContent className="py-6 text-sm text-muted-foreground">
                등록된 휴대폰 기종이 없습니다. 먼저 관리 페이지에서 기종을
                추가해주세요.
              </CardContent>
            </Card>
          </section>
        );
      }

      return (
        <section className="space-y-5">
          <h2 className="text-2xl flex items-center font-bold tracking-tight mb-8">
            기종을 선택하세요.
          </h2>
          <div className="space-y-3">
            {phoneModels.map((model) => {
              const selected = draft.model === model.id;
              return (
                <SelectionCard
                  key={model.id}
                  selected={selected}
                  onClick={() => handleSelectPhone(model)}
                  className="py-3"
                  showCheckIcon
                >
                  <CardContent className="flex w-full items-center gap-4 py-2">
                    <div className="flex h-16 w-16 items-center justify-center rounded-xl bg-gradient-to-br from-primary/10 via-primary/20 to-primary/10 text-xs font-medium text-primary">
                      {model.brand}
                    </div>
                    <div className="flex flex-1 flex-col gap-1">
                      <CardTitle className="text-base font-semibold">
                        {model.model}
                      </CardTitle>
                      {model.modelNumber ? (
                        <span className="text-xs text-muted-foreground">
                          모델 번호 {model.modelNumber}
                        </span>
                      ) : null}
                      {model.releasedPrice ? (
                        <span className="text-sm font-medium text-primary">
                          출시가 {model.releasedPrice.toLocaleString()}원
                        </span>
                      ) : null}
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
      const colorLabel = selectedModelLabel ?? "선택된 기종이 없습니다";
      if (!selectedModel) {
        return (
          <section className="space-y-5">
            <h2 className="text-2xl flex items-center font-bold tracking-tight mb-8">
              색상을 선택하세요.
            </h2>
            <Card className="rounded-2xl">
              <CardContent className="py-6 text-sm text-muted-foreground">
                기종을 먼저 선택해주세요.
              </CardContent>
            </Card>
          </section>
        );
      }

      if (!hasColorOptions) {
        return (
          <section className="space-y-5">
            <h2 className="text-2xl flex items-center font-bold tracking-tight mb-8">
              색상을 선택하세요.
            </h2>
            <Card className="rounded-2xl">
              <CardContent className="py-6 text-sm text-muted-foreground">
                선택한 기종에 색상 옵션이 없습니다. 다음 단계로 이동해주세요.
              </CardContent>
            </Card>
          </section>
        );
      }

      return (
        <section className="space-y-5">
          <div className="space-y-3">
            <h2 className="text-2xl font-bold tracking-tight">{colorLabel}</h2>
            <p className="text-sm text-muted-foreground">
              원하는 색상을 선택해주세요.
            </p>
          </div>
          <div className="space-y-3">
            {colorOptions.map((option) => {
              const label = option.displayLabel ?? option.optionValue;
              return (
                <SelectionCard
                  key={option.id}
                  title={label}
                  selected={draft.color?.id === option.id}
                  onClick={() => handleSelectColor(option)}
                  className="py-4"
                />
              );
            })}
            <SelectionCard
              key="no-preference-color"
              title="상관 없음"
              selected={draft.color === undefined}
              onClick={() => updateDraft({ color: undefined })}
              className="py-4"
            />
          </div>
        </section>
      );
    }

    if (step === 4) {
      const storageTitle = selectedModelLabel ?? "선택된 기종이 없습니다";
      if (!selectedModel) {
        return (
          <section className="space-y-5">
            <h2 className="text-2xl flex items-center font-bold tracking-tight mb-8">
              용량을 선택하세요.
            </h2>
            <Card className="rounded-2xl">
              <CardContent className="py-6 text-sm text-muted-foreground">
                기종을 먼저 선택해주세요.
              </CardContent>
            </Card>
          </section>
        );
      }

      if (!hasStorageOptions) {
        return (
          <section className="space-y-5">
            <h2 className="text-2xl flex items-center font-bold tracking-tight mb-8">
              용량을 선택하세요.
            </h2>
            <Card className="rounded-2xl">
              <CardContent className="py-6 text-sm text-muted-foreground">
                선택한 기종에 용량 옵션이 없습니다. 다음 단계로 이동해주세요.
              </CardContent>
            </Card>
          </section>
        );
      }

      return (
        <section className="space-y-5">
          <div className="space-y-3">
            <h2 className="text-2xl font-bold tracking-tight">
              {storageTitle}
            </h2>
            <p className="text-sm text-muted-foreground">
              {draft.color?.displayLabel ?? "색상 미선택"}
            </p>
            <p className="text-base font-semibold">용량</p>
          </div>
          <div className="space-y-3">
            {storageOptions.map((option) => {
              return (
                <SelectionCard
                  key={option.id}
                  title={option.displayLabel ?? option.optionValue}
                  selected={draft.storage?.id === option.id}
                  onClick={() => handleSelectStorage(option)}
                  className="py-4"
                />
              );
            })}
            <SelectionCard
              key="no-preference-storage"
              title="상관 없음"
              selected={draft.storage === undefined}
              onClick={() => updateDraft({ storage: undefined })}
              className="py-4"
            />
          </div>
        </section>
      );
    }

    if (step === 5) {
      const carrierTitle = selectedModelLabel ?? "통신사 선택";
      return (
        <section className="space-y-5">
          <div className="space-y-3">
            <h2 className="text-2xl font-bold tracking-tight">
              {carrierTitle}
            </h2>
            <p className="text-sm text-muted-foreground">
              {draft.color?.displayLabel ?? "색상 미선택"},{" "}
              {draft.storage?.displayLabel ?? "용량 미선택"}
            </p>
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
            <SelectionCard
              key="no-preference-carrier"
              title="상관 없음"
              selected={draft.carrier === undefined}
              onClick={() => updateDraft({ carrier: undefined })}
              className="py-4"
            />
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
        <div className="space-y-3">
          <h2 className="text-2xl font-bold tracking-tight">
            {selectedModelLabel ?? "기종 미선택"}
          </h2>
        </div>
        <div className="space-y-3">
          <SummaryCard
            label="구매 계획"
            value={getPurchasePlanLabel(draft.purchaseMethod)}
          />
          <SummaryCard
            label="선택한 기종"
            value={selectedModelLabel ?? "상관 없음"}
          />
          <SummaryCard
            label="색상"
            value={draft.color?.displayLabel ?? "상관 없음"}
          />
          <SummaryCard
            label="용량"
            value={draft.storage?.displayLabel ?? "상관 없음"}
          />
          <SummaryCard label="통신사" value={draft.carrier ?? "상관 없음"} />
        </div>
        <p className="text-sm text-muted-foreground">
          경매 시작하기 버튼을 누르면 지금까지 선택한 정보를 바탕으로 견적이
          생성됩니다.
        </p>
      </section>
    );
  };

  if (showSuccess) {
    return (
      <div className="flex min-h-screen items-center justify-center bg-background">
        <div className="mx-auto w-full max-w-md space-y-8 px-8 text-center">
          <div className="flex flex-col items-center space-y-6">
            <div className="flex h-20 w-20 items-center justify-center rounded-full bg-primary">
              <CheckIcon className="h-10 w-10 text-white" />
            </div>
            <div className="space-y-3">
              <h1 className="text-2xl font-bold tracking-tight">
                견적가 시작합니다!
              </h1>
              <p className="text-sm text-muted-foreground">
                곧바로 최저가를 찾아줄거에요.
                <br />
                조금만 기다려주세요.
              </p>
            </div>
          </div>
          <div className="space-y-3 pt-8">
            <Button
              onClick={handleGoToProfile}
              className="w-full rounded-2xl bg-primary/10 text-primary hover:bg-primary/20"
              variant="ghost"
            >
              진구들에게 프로필 공유하기
            </Button>
            <Button onClick={handleGoHome} className="w-full rounded-2xl">
              홈으로 돌아가기
            </Button>
          </div>
        </div>
      </div>
    );
  }

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
  if (value === "NEW_SUBSCRIPTION") {
    return "신규가입";
  }
  if (value === "LOWEST_PRICE") {
    return "최저가";
  }
  if (value === "ANY") {
    return "상관 없음";
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

const CheckIcon = ({ className }: { className?: string }) => (
  <svg
    className={className}
    viewBox="0 0 24 24"
    fill="none"
    xmlns="http://www.w3.org/2000/svg"
  >
    <path
      d="M20 6L9 17L4 12"
      stroke="currentColor"
      strokeWidth="2"
      strokeLinecap="round"
      strokeLinejoin="round"
    />
  </svg>
);

export default QuoteCreateWizardPage;
