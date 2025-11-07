import { useEffect, useState } from "react";
import { useNavigate, useSearchParams } from "react-router-dom";
import { useQuoteCreateStore } from "store/quoteCreateStore";
import type {
  ActivationMethod,
  Carrier,
  PurchaseMethod,
} from "types/QuoteTypes";
import { DEFAULT_EXPIRED_HOURS } from "types/QuoteTypes";
import { PortOnePaymentButton } from "components/payment/PortOnePaymentButton";
import OptionGrid from "components/quote/OptionGrid";

const StepHeader = ({ title, step }: { title: string; step: number }) => (
  <div className="mb-4">
    <div className="text-xs text-muted-foreground">STEP {step} / 4</div>
    <h1 className="mt-1 text-xl font-bold">{title}</h1>
  </div>
);

const StepFooter = ({
  nextLabel = "다음",
  prevLabel = "이전",
  onNext,
  onPrev,
  disabled,
}: {
  nextLabel?: string;
  prevLabel?: string;
  onNext?: () => void;
  onPrev?: () => void;
  disabled?: boolean;
}) => (
  <div className="fixed inset-x-0 bottom-0 z-40 pointer-events-none">
    <div className="max-w-md mx-auto px-4 pb-[max(20px,env(safe-area-inset-bottom))]">
      <div className="flex gap-2 pointer-events-auto">
        {onPrev && (
          <button
            onClick={onPrev}
            className="flex-1 rounded-xl border border-input bg-white py-3 text-sm"
          >
            {prevLabel}
          </button>
        )}
        <button
          onClick={onNext}
          disabled={disabled}
          className="flex-1 rounded-xl bg-black text-white py-3 text-sm disabled:opacity-50"
        >
          {nextLabel}
        </button>
      </div>
    </div>
  </div>
);

const QuoteCreatePage: React.FC = () => {
  const navigate = useNavigate();
  const { step, setStep, draft, updateDraft, reset } = useQuoteCreateStore();
  const [params, setParams] = useSearchParams();

  useEffect(() => {
    document.title = "견적 작성 | PhoneBid";
  }, []);

  // 항상 처음부터 시작: 초안/스텝 초기화 후 쿼리도 step=1로 설정
  useEffect(() => {
    reset();
    setStep(1);
    setDeviceSubStep(1);
    setPlanSubStep(1);
    params.set("step", "1");
    setParams(params, { replace: true });
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, []);

  useEffect(() => {
    params.set("step", String(step));
    setParams(params, { replace: true });
  }, [step, params, setParams]);

  const goNext = () => setStep(Math.min(4, step + 1));

  // Step 1 서브스텝: 1=모델, 2=용량, 3=색상
  const [deviceSubStep, setDeviceSubStep] = useState<1 | 2 | 3>(1);

  // 옵션 목데이터 (추후 서버/상수화 가능)
  const modelOptions = [
    { label: "아이폰 16", value: "iPhone 16" },
    { label: "아이폰 16프로", value: "iPhone 16 Pro" },
    { label: "갤럭시 Z 폴드7", value: "Galaxy Z Fold7" },
    { label: "갤럭시 25", value: "Galaxy 25" },
  ];
  const storageOptions = [
    { label: "128GB", value: "128GB" },
    { label: "256GB", value: "256GB" },
    { label: "512GB", value: "512GB" },
  ];
  const colorOptions = [
    { label: "블랙", value: "블랙" },
    { label: "화이트", value: "화이트" },
    { label: "블루", value: "블루" },
  ];

  const handleSelectModel = (value: string) => {
    updateDraft({ model: value });
    setDeviceSubStep(2);
  };
  const handleSelectStorage = (value: string) => {
    updateDraft({ storage: value });
    setDeviceSubStep(3);
  };
  const handleSelectColor = (value: string) => {
    updateDraft({ color: value });
    // 모든 선택 완료 시 자동 다음 스텝
    goNext();
  };

  // Step 2 서브스텝: 1=통신사, 2=구매방법, 3=개통방법, 4=현재 통신사(번호이동 시)
  const [planSubStep, setPlanSubStep] = useState<1 | 2 | 3 | 4>(1);
  const carrierOptions = [
    { label: "SKT", value: "SKT" },
    { label: "KT", value: "KT" },
    { label: "LG U+", value: "LGU" },
  ];
  const purchaseOptions = [
    { label: "신규가입", value: "NEW" },
    { label: "번호이동", value: "NUMBER_TRANSFER" },
    { label: "기기변경", value: "DEVICE_CHANGE" },
  ];
  const activationOptions = [
    { label: "자급제", value: "DEVICE_ONLY" },
    { label: "선택약정", value: "SELECTIVE_SUBSIDY" },
    { label: "약정", value: "CONTRACT" },
  ];

  const handleSelectCarrier = (value: string) => {
    updateDraft({ carrier: value as Carrier });
    setPlanSubStep(2);
  };
  const handleSelectPurchase = (value: string) => {
    updateDraft({ purchaseMethod: value as PurchaseMethod });
    setPlanSubStep(3);
  };
  const handleSelectActivation = (value: string) => {
    updateDraft({ activationMethod: value as ActivationMethod });
    if (draft.purchaseMethod === "NUMBER_TRANSFER") {
      setPlanSubStep(4);
    } else {
      goNext();
    }
  };
  const handleSelectCurrentCarrier = (value: string) => {
    updateDraft({ currentCarrier: value as Carrier });
    goNext();
  };

  return (
    <div className="bg-background min-h-[60vh] pb-24">
      <div className="max-w-md mx-auto px-4 py-6 space-y-4">
        {/* 로컬 헤더: 뒤로가기 + 타이틀(견적 받아보기) */}
        <div className="sticky top-0 z-10 -mx-4 px-4 pb-3 bg-background/80 backdrop-blur">
          <div className="flex items-center gap-3 h-11">
            <button
              aria-label="뒤로가기"
              onClick={() => navigate(-1)}
              className="flex mr-auto items-center size-9 rounded-md hover:bg-accent focus:outline-none focus-visible:ring-2 focus-visible:ring-primary-500"
            >
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
            </button>
            <div className="flex mx-auto text-base font-semibold">
              견적 작성
            </div>
            <div className="flex mx-auto" />
          </div>
        </div>
        {step === 1 && (
          <>
            <StepHeader title="기기 선택" step={1} />

            {/* 선택 진행 상태 표시 */}
            <div className="flex flex-wrap gap-2 text-xs text-muted-foreground">
              <span className="px-2 py-1 rounded bg-gray-100 text-gray-700">
                모델: {draft.model ? draft.model : "선택"}
              </span>
              <span className="px-2 py-1 rounded bg-gray-100 text-gray-700">
                용량: {draft.storage ? draft.storage : "선택"}
              </span>
              <span className="px-2 py-1 rounded bg-gray-100 text-gray-700">
                색상: {draft.color ? draft.color : "선택"}
              </span>
            </div>

            {/* 서브스텝 그리드 */}
            {deviceSubStep === 1 && (
              <div className="mt-3 space-y-2">
                <div className="text-sm font-medium">모델을 선택하세요</div>
                <OptionGrid
                  items={modelOptions}
                  selectedValue={draft.model}
                  onSelect={handleSelectModel}
                  columns={2}
                  ariaLabel="모델 선택"
                />
              </div>
            )}
            {deviceSubStep === 2 && (
              <div className="mt-3 space-y-2">
                <div className="text-sm font-medium">용량을 선택하세요</div>
                <OptionGrid
                  items={storageOptions}
                  selectedValue={draft.storage}
                  onSelect={handleSelectStorage}
                  columns={3}
                  ariaLabel="용량 선택"
                />
              </div>
            )}
            {deviceSubStep === 3 && (
              <div className="mt-3 space-y-2">
                <div className="text-sm font-medium">색상을 선택하세요</div>
                <OptionGrid
                  items={colorOptions}
                  selectedValue={draft.color}
                  onSelect={handleSelectColor}
                  columns={3}
                  ariaLabel="색상 선택"
                />
              </div>
            )}

            {/* 자동 진행이므로 다음 버튼은 보조용으로 유지하거나 숨길 수 있음 */}
            <StepFooter
              onNext={goNext}
              disabled={!draft.model || !draft.storage || !draft.color}
            />
          </>
        )}

        {step === 2 && (
          <>
            <StepHeader title="통신/구매 옵션" step={2} />

            <div className="flex flex-wrap gap-2 text-xs text-muted-foreground">
              <span className="px-2 py-1 rounded bg-gray-100 text-gray-700">
                통신사: {draft.carrier ? draft.carrier : "선택"}
              </span>
              <span className="px-2 py-1 rounded bg-gray-100 text-gray-700">
                구매: {draft.purchaseMethod ? draft.purchaseMethod : "선택"}
              </span>
              <span className="px-2 py-1 rounded bg-gray-100 text-gray-700">
                개통: {draft.activationMethod ? draft.activationMethod : "선택"}
              </span>
              {draft.purchaseMethod === "NUMBER_TRANSFER" && (
                <span className="px-2 py-1 rounded bg-gray-100 text-gray-700">
                  현재: {draft.currentCarrier ? draft.currentCarrier : "선택"}
                </span>
              )}
            </div>

            {planSubStep === 1 && (
              <div className="mt-3 space-y-2">
                <div className="text-sm font-medium">통신사를 선택하세요</div>
                <OptionGrid
                  items={carrierOptions}
                  selectedValue={draft.carrier as string}
                  onSelect={handleSelectCarrier}
                  columns={3}
                  ariaLabel="통신사 선택"
                />
              </div>
            )}
            {planSubStep === 2 && (
              <div className="mt-3 space-y-2">
                <div className="text-sm font-medium">
                  구매 방법을 선택하세요
                </div>
                <OptionGrid
                  items={purchaseOptions}
                  selectedValue={draft.purchaseMethod as string}
                  onSelect={handleSelectPurchase}
                  columns={3}
                  ariaLabel="구매 방법 선택"
                />
              </div>
            )}
            {planSubStep === 3 && (
              <div className="mt-3 space-y-2">
                <div className="text-sm font-medium">
                  개통 방법을 선택하세요
                </div>
                <OptionGrid
                  items={activationOptions}
                  selectedValue={draft.activationMethod as string}
                  onSelect={handleSelectActivation}
                  columns={3}
                  ariaLabel="개통 방법 선택"
                />
              </div>
            )}
            {planSubStep === 4 &&
              draft.purchaseMethod === "NUMBER_TRANSFER" && (
                <div className="mt-3 space-y-2">
                  <div className="text-sm font-medium">
                    현재 통신사를 선택하세요
                  </div>
                  <OptionGrid
                    items={carrierOptions}
                    selectedValue={draft.currentCarrier as string}
                    onSelect={handleSelectCurrentCarrier}
                    columns={3}
                    ariaLabel="현재 통신사 선택"
                  />
                </div>
              )}

            <StepFooter
              onNext={goNext}
              disabled={
                !draft.carrier ||
                !draft.purchaseMethod ||
                !draft.activationMethod ||
                (draft.purchaseMethod === "NUMBER_TRANSFER" &&
                  !draft.currentCarrier)
              }
            />
          </>
        )}

        {step === 3 && (
          <>
            <StepHeader title="가격/마감 설정" step={3} />

            {/* 희망가 칩 */}
            <div className="mt-1 space-y-2">
              <div className="text-sm font-medium">희망가를 선택하세요</div>
              <OptionGrid
                items={[60, 70, 80, 90, 100, 110].map((v) => ({
                  label: `${v}만`,
                  value: String(v * 10000),
                }))}
                selectedValue={
                  draft.hopePrice ? String(draft.hopePrice) : undefined
                }
                onSelect={(value) => updateDraft({ hopePrice: Number(value) })}
                columns={3}
                ariaLabel="희망가 선택"
              />
            </div>

            {/* 마감 시간 칩 */}
            <div className="mt-4 space-y-2">
              <div className="text-sm font-medium">마감 시간을 선택하세요</div>
              <OptionGrid
                items={[12, 24, 48, 72].map((h) => ({
                  label: `${h}시간`,
                  value: String(h),
                }))}
                selectedValue={String(
                  draft.expiredHours ?? DEFAULT_EXPIRED_HOURS
                )}
                onSelect={(value) =>
                  updateDraft({ expiredHours: Number(value) })
                }
                columns={4}
                ariaLabel="마감 시간 선택"
              />
            </div>

            {/* 두 값이 모두 있으면 자동 이동 */}
            {draft.hopePrice &&
              (draft.expiredHours ?? DEFAULT_EXPIRED_HOURS) && (
                <div className="sr-only">자동 진행</div>
              )}
            <StepFooter
              onNext={() => (draft.hopePrice ? goNext() : undefined)}
              disabled={!draft.hopePrice}
            />
          </>
        )}

        {step === 4 && (
          <>
            <StepHeader title="요약/확인" step={4} />
            <div className="rounded-xl border border-border p-4 space-y-2 bg-card">
              <div className="text-sm">
                {draft.model} · {draft.storage} · {draft.color}
              </div>
              <div className="text-sm">
                {draft.carrier} · {draft.purchaseMethod} ·{" "}
                {draft.activationMethod}
              </div>
              {draft.purchaseMethod === "NUMBER_TRANSFER" && (
                <div className="text-sm">
                  현재 통신사: {draft.currentCarrier}
                </div>
              )}
              <div className="text-sm">
                희망가: {draft.hopePrice?.toLocaleString()}원
              </div>
              <div className="text-sm">
                마감: {draft.expiredHours ?? DEFAULT_EXPIRED_HOURS}시간 후
              </div>
            </div>
            <div className="space-y-3">
              <PortOnePaymentButton
                label="결제 창 테스트"
                requestPayload={{
                  merchantUid: `demo-${Date.now()}`,
                  amount: 1000,
                  productName: draft.model ?? "PhoneBid 견적",
                  buyerName: "홍길동",
                  buyerEmail: "demo@example.com",
                  buyerPhone: "01012345678",
                  returnUrl: `${window.location.origin}/payment/success`,
                  cancelUrl: `${window.location.origin}/payment/fail`,
                }}
              />
              <StepFooter
                nextLabel="제출"
                onNext={() => navigate("/auctions")}
              />
            </div>
          </>
        )}
      </div>
    </div>
  );
};

export default QuoteCreatePage;
