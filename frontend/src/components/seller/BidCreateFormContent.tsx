import { useEffect, useMemo, useRef, useState } from "react";
import { QuoteRequestInfo } from "components/seller/QuoteRequestInfo";
import { Card, CardContent } from "components/ui/card";
import { formatNumber } from "utils/formatters";
import { getActivePricePlans } from "services/pricePlanService";
import { logError } from "utils/errorUtils";
import type { QuoteDetail } from "types/QuoteTypes";
import type { useBidForm } from "hooks/useBidForm";
import type { AdditionalServiceRequest, PricePlan, PricePlanCategory } from "types/SellerTypes";
import { cn } from "@/utils/cn";

type UseBidFormReturn = ReturnType<typeof useBidForm>;

interface BidCreateFormContentProps {
  quote: QuoteDetail;
  bidForm: UseBidFormReturn;
}

/** 할인 방식·판매 가격·필수 요금제·필수 유지 조건 등 동일 섹션 라벨 */
const SECTION_LABEL = "text-sm font-bold text-black";
/** SECTION_LABEL 블록 래퍼: 연한 회색 배경 + 테두리 */
const SECTION_BOX =
  "rounded-lg border border-border bg-muted/50 p-4 shadow-sm";
/** 필수 요금제 하위 필드(요금제, 월 요금대) */
const SUB_FIELD_LABEL =
  "mb-1 block text-xs font-normal text-zinc-600";

type AddonOption = { value: string; price: number; label: string };
type AddonCarrier = "SKT" | "KT" | "LGU";

const ADDON_NONE_OPTION: AddonOption = { value: "", price: 0, label: "선택 안 함" };

// 통신사별 부가서비스 옵션 매핑 (데이터는 추후 확장/교체 가능)
// - 기본적으로 각 통신사 배열의 첫 요소는 "선택 안 함"
const ADDON_OPTIONS_BY_CARRIER: Record<AddonCarrier, AddonOption[]> = {
  SKT: [
    ADDON_NONE_OPTION,
    { value: "T멤버십 VIP", price: 5000, label: "T멤버십 VIP (월 5,000원)" },
    { value: "디바이스 케어", price: 11000, label: "디바이스 케어 (월 11,000원)" },
    { value: "T우주 Pass", price: 13000, label: "T우주 Pass (월 13,000원)" },
  ],
  KT: [ADDON_NONE_OPTION],
  LGU: [ADDON_NONE_OPTION],
};

function normalizeAddonCarrier(carrier: QuoteDetail["carrier"]): AddonCarrier | null {
  if (carrier === "SKT" || carrier === "KT" || carrier === "LGU") return carrier;
  if (carrier === "SKT_ALD") return "SKT";
  if (carrier === "KT_ALD") return "KT";
  if (carrier === "LGU_ALD") return "LGU";
  return null; // ANY 등은 부가서비스 옵션 미노출
}

function PencilIcon() {
  return (
    <svg
      className="w-5 h-5 text-blue-600"
      fill="none"
      stroke="currentColor"
      viewBox="0 0 24 24"
    >
      <path
        strokeLinecap="round"
        strokeLinejoin="round"
        strokeWidth={2}
        d="M11 5H6a2 2 0 00-2 2v11a2 2 0 002 2h11a2 2 0 002-2v-5m-1.414-9.414a2 2 0 112.828 2.828L11.828 15H9v-2.828l8.586-8.586z"
      />
    </svg>
  );
}

function RadioActivationCard({
  selected,
  onSelect,
  name,
  value,
  title,
  description,
}: {
  selected: boolean;
  onSelect: () => void;
  name: string;
  value: string;
  title: string;
  description?: string;
}) {
  return (
    <label className="block">
      <input
        type="radio"
        name={name}
        value={value}
        checked={selected}
        onChange={onSelect}
        className="sr-only peer"
      />
      <div
        className={cn(
          "w-full rounded-xl border-2 p-3 text-left transition-colors cursor-pointer",
          "peer-focus-visible:outline-none peer-focus-visible:ring-2 peer-focus-visible:ring-blue-600/30 peer-focus-visible:ring-offset-2 peer-focus-visible:ring-offset-background",
          selected
            ? "border-blue-600 bg-blue-50/50 ring-1 ring-blue-600/20"
            : "border-border bg-card hover:bg-muted/40"
        )}
      >
        <div className="flex items-start gap-3">
          <span
            className={cn(
              "mt-0.5 flex h-5 w-5 shrink-0 items-center justify-center rounded-full border-2",
              selected ? "border-blue-600" : "border-muted-foreground/40"
            )}
          >
            {selected ? (
              <span className="h-2.5 w-2.5 rounded-full bg-blue-600" />
            ) : null}
          </span>
          <div>
            <div className="font-semibold text-foreground">{title}</div>
            {description ? (
              <div className="text-xs text-muted-foreground mt-0.5">
                {description}
              </div>
            ) : null}
          </div>
        </div>
      </div>
    </label>
  );
}

export function BidCreateFormContent({ quote, bidForm }: BidCreateFormContentProps) {
  const { formData, errors, calculations, updateField, selectPricePlan } = bidForm;

  const [pricePlans, setPricePlans] = useState<PricePlan[]>([]);
  const [selectedCategory, setSelectedCategory] =
    useState<PricePlanCategory>("FIVE_G");
  const pricePlansRequestIdRef = useRef(0);
  const bidFormRef = useRef(bidForm);
  bidFormRef.current = bidForm;

  useEffect(() => {
    const loadPricePlans = async () => {
      const requestId = ++pricePlansRequestIdRef.current;

      const carrierForPlans =
        quote.carrier === "ANY"
          ? undefined
          : quote.carrier === "SKT_ALD"
            ? "SKT"
            : quote.carrier === "KT_ALD"
              ? "KT"
              : quote.carrier === "LGU_ALD"
                ? "LGU"
                : (quote.carrier as "SKT" | "KT" | "LGU");

      try {
        const plans = await getActivePricePlans({
          carrier: carrierForPlans,
          category: selectedCategory,
        });
        if (requestId !== pricePlansRequestIdRef.current) {
          return;
        }
        setPricePlans(plans);

        const { pricePlanId, selectedPricePlan } = bidFormRef.current.formData;
        const selectedId = pricePlanId || selectedPricePlan?.id || "";
        if (selectedId && !plans.some((p) => p.id === selectedId)) {
          bidFormRef.current.updateField("pricePlanId", "");
          bidFormRef.current.updateField("selectedPricePlan", null);
        }
      } catch (error) {
        if (requestId !== pricePlansRequestIdRef.current) {
          return;
        }
        logError("요금제 목록 조회 실패:", error);
      }
    };

    loadPricePlans();
  }, [quote, selectedCategory]);

  const addonCarrier = useMemo(() => normalizeAddonCarrier(quote.carrier), [quote.carrier]);
  const addonOptions = useMemo(() => {
    // 옵션 데이터는 추후 채워 넣더라도, "선택 안 함"은 항상 제공
    if (!addonCarrier) return [ADDON_NONE_OPTION];
    const options = ADDON_OPTIONS_BY_CARRIER[addonCarrier] ?? [ADDON_NONE_OPTION];
    return options.length > 0 ? options : [ADDON_NONE_OPTION];
  }, [addonCarrier]);

  const handleAddonPrimaryChange = (value: string) => {
    const opt = addonOptions.find((o) => o.value === value);
    if (!opt || !value) {
      updateField("additionalServices", [] as AdditionalServiceRequest[]);
      return;
    }
    updateField("additionalServices", [
      { serviceName: opt.value, servicePrice: opt.price },
    ]);
  };

  const addonPrimaryValue =
    formData.additionalServices[0]?.serviceName ?? "";

  // 통신사가 바뀌거나 옵션이 바뀌었을 때, 현재 선택된 부가서비스가 지원되지 않으면 정리합니다.
  useEffect(() => {
    if (!addonPrimaryValue) return;
    const isSupported = addonOptions.some((o) => o.value === addonPrimaryValue);
    if (!isSupported) {
      updateField("additionalServices", [] as AdditionalServiceRequest[]);
    }
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [addonCarrier, addonOptions, addonPrimaryValue]);

  return (
    <div className="grid grid-cols-1 lg:grid-cols-2 gap-5 lg:gap-6 items-start">
      <div className="min-w-0">
        <QuoteRequestInfo quote={quote} />
      </div>

      <div className="min-w-0 space-y-4">
        <div className="flex items-center gap-2">
          <PencilIcon />
          <h3 className="text-base font-bold text-foreground">
            판매자 제안 입력
          </h3>
        </div>

        <section className={cn("space-y-2", SECTION_BOX)}>
          <p className={SECTION_LABEL}>할인 방식 선택</p>
          <div
            className="space-y-2"
            role="radiogroup"
            aria-label="할인 방식 선택"
          >
            <RadioActivationCard
              name="activationMethod"
              value="COMMON_SUBSIDY"
              selected={formData.activationMethod === "COMMON_SUBSIDY"}
              onSelect={() =>
                updateField("activationMethod", "COMMON_SUBSIDY")
              }
              title="공시지원금 (단말할인)"
            />
            <RadioActivationCard
              name="activationMethod"
              value="SELECTIVE_SUBSIDY"
              selected={formData.activationMethod === "SELECTIVE_SUBSIDY"}
              onSelect={() =>
                updateField("activationMethod", "SELECTIVE_SUBSIDY")
              }
              title="선택약정 (요금할인 25%)"
            />
          </div>
        </section>

        <section className={cn("space-y-2", SECTION_BOX)}>
          <p className={SECTION_LABEL}>판매 가격</p>
          <div className="flex flex-wrap items-end gap-3">
            <div className="flex-1 min-w-[200px]">
              <div className="relative">
                <input
                  type="text"
                  inputMode="numeric"
                  value={
                    formData.publicSubsidy > 0
                      ? formatNumber(formData.publicSubsidy)
                      : ""
                  }
                  onChange={(e) => {
                    const numericValue = e.target.value.replace(/[^0-9]/g, "");
                    updateField(
                      "publicSubsidy",
                      parseInt(numericValue) || 0
                    );
                  }}
                  className="w-full rounded-lg border border-input bg-background h-11 px-3 pr-10 text-sm font-medium text-right focus:outline-none focus:ring-2 focus:ring-blue-600/30 focus:border-blue-600"
                  placeholder="0"
                />
                <span className="absolute right-3 top-1/2 -translate-y-1/2 text-sm text-muted-foreground pointer-events-none">
                  원
                </span>
              </div>
            </div>
            <label className="flex items-center gap-2 pb-2 cursor-pointer shrink-0">
              <input
                type="checkbox"
                className="h-4 w-4 rounded border-input text-blue-600 focus:ring-blue-600"
                checked={formData.isPayback}
                onChange={(e) =>
                  updateField("isPayback", e.target.checked)
                }
              />
              <span className="text-sm font-medium text-zinc-600">페이백</span>
            </label>
          </div>
          <p className="text-xs text-muted-foreground">
            페이백인 경우 체크하거나 마이너스(-) 금액을 입력하세요
          </p>
          {errors.publicSubsidy && (
            <p className="text-sm text-red-500">{errors.publicSubsidy}</p>
          )}
        </section>

        <div className="rounded-lg border border-dashed border-border bg-muted/20 px-3 py-1.5 text-xs text-muted-foreground">
          출고가 기준 {formatNumber(formData.devicePrice)}원 · 할부원금{" "}
          {formatNumber(calculations.installmentPrincipal)}원
        </div>
        {errors.installmentPrincipal && (
          <p className="text-sm text-red-500">{errors.installmentPrincipal}</p>
        )}

        <section className={cn("space-y-2", SECTION_BOX)}>
          <p className={SECTION_LABEL}>필수 요금제 설정</p>
          <div>
            <label className={SUB_FIELD_LABEL} htmlFor="bid-create-modal-price-plan-select">
              요금제
            </label>
            <div className="flex gap-2 mb-2">
              <button
                type="button"
                aria-pressed={selectedCategory === "FIVE_G"}
                onClick={() => setSelectedCategory("FIVE_G")}
                className={cn(
                  "px-3 py-1.5 rounded-md text-xs font-medium transition-colors",
                  selectedCategory === "FIVE_G"
                    ? "bg-primary text-primary-foreground"
                    : "bg-muted text-muted-foreground hover:bg-muted/80"
                )}
              >
                5G
              </button>
              <button
                type="button"
                aria-pressed={selectedCategory === "LTE"}
                onClick={() => setSelectedCategory("LTE")}
                className={cn(
                  "px-3 py-1.5 rounded-md text-xs font-medium transition-colors",
                  selectedCategory === "LTE"
                    ? "bg-primary text-primary-foreground"
                    : "bg-muted text-muted-foreground hover:bg-muted/80"
                )}
              >
                LTE
              </button>
            </div>
            <select
              id="bid-create-modal-price-plan-select"
              value={formData.pricePlanId}
              onChange={(e) => {
                const id = e.target.value;
                const plan = pricePlans.find((p) => p.id === id);
                if (plan) {
                  selectPricePlan(plan);
                } else {
                  updateField("pricePlanId", "");
                  updateField("selectedPricePlan", null);
                }
              }}
              className="w-full rounded-lg border border-input bg-background h-11 px-3 text-sm focus:outline-none focus:ring-2 focus:ring-blue-600/30"
            >
              <option value="">요금제를 선택해주세요</option>
              {pricePlans.map((plan) => (
                <option key={plan.id} value={plan.id}>
                  {plan.planName} - {formatNumber(plan.monthlyFee)}원 (
                  {plan.dataAllowanceText || "데이터 정보 없음"})
                </option>
              ))}
            </select>
            {errors.pricePlanId && (
              <p className="mt-1 text-sm text-red-500">{errors.pricePlanId}</p>
            )}
            {formData.selectedPricePlan && (
              <div className="mt-2 rounded-md border border-border bg-muted/40 p-3 text-xs">
                <div className="font-medium text-foreground">
                  {formData.selectedPricePlan.planName}
                </div>
                <div className="text-muted-foreground mt-1 space-y-0.5">
                  <div>
                    월정액:{" "}
                    {formatNumber(formData.selectedPricePlan.monthlyFee)}원
                  </div>
                  <div>
                    데이터:{" "}
                    {formData.selectedPricePlan.dataAllowanceText || "-"}
                  </div>
                  {formData.selectedPricePlan.throttleSpeedText &&
                    formData.selectedPricePlan.throttleSpeedText !== "-" && (
                      <div>
                        소진 시: {formData.selectedPricePlan.throttleSpeedText}
                      </div>
                    )}
                  <div>
                    음성/문자:{" "}
                    {formData.selectedPricePlan.voiceSmsText || "-"}
                  </div>
                </div>
              </div>
            )}
          </div>
        </section>

        <section className={cn("space-y-2", SECTION_BOX)}>
          <p className={SECTION_LABEL}>필수 유지 조건</p>
          <label className={SUB_FIELD_LABEL}>회선 유지기간</label>
          <select
            value={formData.lineMaintenanceMonths}
            onChange={(e) =>
              updateField(
                "lineMaintenanceMonths",
                parseInt(e.target.value) || 24
              )
            }
            className="mt-1 w-full rounded-lg border border-input bg-background h-11 px-3 text-sm focus:outline-none focus:ring-2 focus:ring-blue-600/30"
          >
            <option value={24}>24개월 이상</option>
            <option value={36}>36개월 이상</option>
          </select>
        </section>

        <section className={cn("space-y-2", SECTION_BOX)}>
          <p className={SECTION_LABEL}>부가서비스</p>
          <div className="grid grid-cols-1 sm:grid-cols-2 gap-2 sm:gap-3">
            <div>
              <label className={SUB_FIELD_LABEL}>서비스</label>
              <select
                value={addonPrimaryValue}
                onChange={(e) => handleAddonPrimaryChange(e.target.value)}
                className="w-full rounded-lg border border-input bg-background h-11 px-3 text-sm focus:outline-none focus:ring-2 focus:ring-blue-600/30"
              >
                {addonOptions.map((o) => (
                  <option key={o.label} value={o.value}>
                    {o.label}
                  </option>
                ))}
              </select>
            </div>
            <div>
              <label className={SUB_FIELD_LABEL}>유지기간</label>
              <select
                value={formData.additionalServicesMaintenanceMonths}
                onChange={(e) =>
                  updateField(
                    "additionalServicesMaintenanceMonths",
                    parseInt(e.target.value) || 0
                  )
                }
                className="w-full rounded-lg border border-input bg-background h-11 px-3 text-sm focus:outline-none focus:ring-2 focus:ring-blue-600/30"
              >
                <option value={0}>유지기간 선택</option>
                <option value={3}>3개월 이상</option>
                <option value={12}>12개월 이상</option>
                <option value={24}>24개월 이상</option>
                <option value={36}>36개월 이상</option>
              </select>
            </div>
          </div>
        </section>

        <div className="grid grid-cols-2 gap-2 sm:gap-3">
          <div>
            <label className={SUB_FIELD_LABEL}>할부 기간</label>
            <select
              value={formData.installmentMonths}
              onChange={(e) =>
                updateField(
                  "installmentMonths",
                  parseInt(e.target.value) || 24
                )
              }
              className="mt-1 w-full rounded-lg border border-input bg-background h-11 px-3 text-sm"
            >
              <option value={24}>24개월</option>
              <option value={36}>36개월</option>
            </select>
          </div>
          <div>
            <label className={SUB_FIELD_LABEL}>
              요금제 유지기간
            </label>
            <select
              value={formData.pricePlanMaintenanceMonths}
              onChange={(e) =>
                updateField(
                  "pricePlanMaintenanceMonths",
                  parseInt(e.target.value) || 24
                )
              }
              className="mt-1 w-full rounded-lg border border-input bg-background h-11 px-3 text-sm"
            >
              <option value={24}>24개월 이상</option>
              <option value={36}>36개월 이상</option>
            </select>
          </div>
        </div>

        <Card className="bg-primary-50/80 border-primary-200">
          <CardContent className="py-2.5 px-4">
            <div className="flex items-center gap-2 mb-1">
              <span className="text-sm font-bold text-foreground">
                월 할부금
              </span>
              <span className="text-xs text-muted-foreground">
                (3.3% 할부이자 포함)
              </span>
            </div>
            <div className="text-xl font-bold text-blue-700 tabular-nums">
              {formatNumber(calculations.monthlyInstallment)}원
            </div>
          </CardContent>
        </Card>

        <Card className="border border-pink-100 shadow-sm overflow-hidden bg-gradient-to-br from-sky-50 via-white to-rose-50/80">
          <CardContent className="py-3 px-4">
            <div className="flex items-center justify-between mb-1">
              <span className="text-sm font-bold text-foreground">
                월 예상 납부액
              </span>
              <span className="text-xs text-muted-foreground">
                (할부금 + 요금제 + 부가)
              </span>
            </div>
            <div className="text-right">
              <span className="text-2xl sm:text-3xl font-bold text-foreground tabular-nums">
                {formatNumber(calculations.totalMonthlyPayment)}
              </span>
              <span className="text-lg font-normal ml-1">원</span>
            </div>
          </CardContent>
        </Card>
      </div>
    </div>
  );
}
