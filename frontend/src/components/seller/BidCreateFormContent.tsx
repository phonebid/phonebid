import { QuoteRequestInfo } from "components/seller/QuoteRequestInfo";
import { Card, CardContent } from "components/ui/card";
import { formatNumber } from "utils/formatters";
import type { QuoteDetail } from "types/QuoteTypes";
import type { useBidForm } from "hooks/useBidForm";
import type { AdditionalServiceRequest } from "types/SellerTypes";
import { cn } from "@/utils/cn";

type UseBidFormReturn = ReturnType<typeof useBidForm>;

interface BidCreateFormContentProps {
  quote: QuoteDetail;
  bidForm: UseBidFormReturn;
}

const PRICE_PLAN_OPTIONS: { value: string; label: string }[] = [
  { value: "", label: "요금제 선택" },
  { value: "5GX 프라임", label: "5GX 프라임" },
  { value: "5G 프리미어 에센셜", label: "5G 프리미어 에센셜" },
  { value: "5G 슬림", label: "5G 슬림" },
  { value: "LTE 베이직", label: "LTE 베이직" },
];

const PRICE_PLAN_PRICE_OPTIONS: { value: number; label: string }[] = [
  { value: 0, label: "월 요금대 선택" },
  { value: 45000, label: "4만원대" },
  { value: 55000, label: "5만원대" },
  { value: 65000, label: "6만원대" },
  { value: 75000, label: "7만원대" },
  { value: 85000, label: "8만원대 이상" },
];

/** 할인 방식·판매 가격·필수 요금제·필수 유지 조건 등 동일 섹션 라벨 */
const SECTION_LABEL = "text-sm font-bold text-black";
/** 필수 요금제 하위 필드(요금제, 월 요금대) */
const SUB_FIELD_LABEL =
  "mb-1 block text-xs font-normal text-zinc-600";

const ADDON_SERVICE_OPTIONS: { value: string; price: number; label: string }[] =
  [
    { value: "", price: 0, label: "선택 안 함" },
    { value: "T멤버십 VIP", price: 5000, label: "T멤버십 VIP (월 5,000원)" },
    { value: "디바이스 케어", price: 11000, label: "디바이스 케어 (월 11,000원)" },
    { value: "T우주 Pass", price: 13000, label: "T우주 Pass (월 13,000원)" },
  ];

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
  title,
  description,
}: {
  selected: boolean;
  onSelect: () => void;
  title: string;
  description?: string;
}) {
  return (
    <button
      type="button"
      onClick={onSelect}
      className={cn(
        "w-full rounded-xl border-2 p-3 text-left transition-colors",
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
    </button>
  );
}

export function BidCreateFormContent({ quote, bidForm }: BidCreateFormContentProps) {
  const { formData, errors, calculations, updateField } = bidForm;

  const presetPlanValues = PRICE_PLAN_OPTIONS.map((o) => o.value).filter(
    Boolean
  );
  const isPresetPlanName = presetPlanValues.includes(formData.pricePlanName);

  const handleAddonPrimaryChange = (value: string) => {
    const opt = ADDON_SERVICE_OPTIONS.find((o) => o.value === value);
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

        <section className="space-y-2">
          <p className={SECTION_LABEL}>할인 방식 선택</p>
          <div className="space-y-2">
            <RadioActivationCard
              selected={formData.activationMethod === "COMMON_SUBSIDY"}
              onSelect={() =>
                updateField("activationMethod", "COMMON_SUBSIDY")
              }
              title="공시지원금 (단말할인)"
            />
            <RadioActivationCard
              selected={formData.activationMethod === "SELECTIVE_SUBSIDY"}
              onSelect={() =>
                updateField("activationMethod", "SELECTIVE_SUBSIDY")
              }
              title="선택약정 (요금할인 25%)"
            />
          </div>
        </section>

        <section className="space-y-2">
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

        <section className="space-y-2">
          <p className={SECTION_LABEL}>필수 요금제 설정</p>
          <div>
            <label className={SUB_FIELD_LABEL}>요금제</label>
          <select
            value={isPresetPlanName ? formData.pricePlanName : "__custom__"}
            onChange={(e) => {
              const v = e.target.value;
              if (v === "__custom__") {
                updateField("pricePlanName", "");
                return;
              }
              updateField("pricePlanName", v);
            }}
            className="w-full rounded-lg border border-input bg-background h-11 px-3 text-sm focus:outline-none focus:ring-2 focus:ring-blue-600/30"
          >
            {PRICE_PLAN_OPTIONS.map((o) => (
              <option key={o.value || "empty"} value={o.value}>
                {o.label}
              </option>
            ))}
            <option value="__custom__">직접 입력</option>
          </select>
          </div>
          {(!isPresetPlanName || formData.pricePlanName === "") && (
            <input
              type="text"
              value={formData.pricePlanName}
              onChange={(e) =>
                updateField("pricePlanName", e.target.value)
              }
              placeholder="요금제명을 입력하세요"
              className="w-full rounded-lg border border-input bg-background h-11 px-3 text-sm focus:outline-none focus:ring-2 focus:ring-blue-600/30"
            />
          )}
          <div>
            <label className={SUB_FIELD_LABEL}>월 요금대</label>
          <select
            value={formData.pricePlanPrice}
            onChange={(e) =>
              updateField(
                "pricePlanPrice",
                parseInt(e.target.value) || 0
              )
            }
            className="w-full rounded-lg border border-input bg-background h-11 px-3 text-sm focus:outline-none focus:ring-2 focus:ring-blue-600/30"
          >
            {PRICE_PLAN_PRICE_OPTIONS.map((o) => (
              <option key={o.label} value={o.value}>
                {o.label}
              </option>
            ))}
          </select>
          </div>
          {errors.pricePlanName && (
            <p className="text-sm text-red-500">{errors.pricePlanName}</p>
          )}
          {errors.pricePlanPrice && (
            <p className="text-sm text-red-500">{errors.pricePlanPrice}</p>
          )}
        </section>

        <section className="space-y-2">
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

        <section className="space-y-2">
          <p className={SECTION_LABEL}>부가서비스</p>
          <div className="grid grid-cols-1 sm:grid-cols-2 gap-2 sm:gap-3">
            <div>
              <label className={SUB_FIELD_LABEL}>서비스</label>
            <select
              value={addonPrimaryValue}
              onChange={(e) => handleAddonPrimaryChange(e.target.value)}
              className="w-full rounded-lg border border-input bg-background h-11 px-3 text-sm focus:outline-none focus:ring-2 focus:ring-blue-600/30"
            >
              {ADDON_SERVICE_OPTIONS.map((o) => (
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
