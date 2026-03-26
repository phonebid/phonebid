import { useState, useMemo, useEffect, useRef } from "react";
import type { QuoteDetail } from "types/QuoteTypes";
import type { BidCreateRequest, AdditionalServiceRequest, PricePlan } from "types/SellerTypes";
import {
  calculateInstallmentPrincipal,
  calculateMonthlyInstallment,
  calculateTotalMonthlyPayment,
} from "utils/bidUtils";
import { BID_FORM_DEFAULTS } from "utils/constants";

const DEFAULT_INSTALLMENT_MONTHS = 24;
const DEFAULT_INSTALLMENT_INTEREST_RATE = 3.3;

export interface BidFormData {
  devicePrice: number;
  publicSubsidy: number;
  additionalSubsidy: number;
  /** 페이백: 체크 시 추가지원금을 -공시지원금으로 동기화 */
  isPayback: boolean;
  purchaseMethod: "NUMBER_TRANSFER" | "DEVICE_CHANGE" | "NEW_SUBSCRIPTION" | "LOWEST_PRICE" | "ANY";
  carrier: "SKT" | "KT" | "LGU" | "SKT_ALD" | "KT_ALD" | "LGU_ALD" | "ANY";
  currentCarrier?: "SKT" | "KT" | "LGU" | "SKT_ALD" | "KT_ALD" | "LGU_ALD" | "ANY";
  activationMethod: "COMMON_SUBSIDY" | "SELECTIVE_SUBSIDY" | "ANY";
  installmentMonths: number;
  pricePlanId: string;
  selectedPricePlan: PricePlan | null;
  pricePlanMaintenanceMonths: number;
  lineMaintenanceMonths: number;
  additionalServices: AdditionalServiceRequest[];
  additionalServicesMaintenanceMonths: number;
  deliveryDays: number;
}

export interface BidFormErrors {
  devicePrice?: string;
  publicSubsidy?: string;
  additionalSubsidy?: string;
  installmentPrincipal?: string;
  pricePlanId?: string;
  deliveryDays?: string;
}

export const useBidForm = (quote: QuoteDetail | null) => {
  const [formData, setFormData] = useState<BidFormData>({
    devicePrice: BID_FORM_DEFAULTS.DEVICE_PRICE,
    publicSubsidy: 0,
    additionalSubsidy: 0,
    isPayback: false,
    purchaseMethod: quote?.purchaseMethod || "DEVICE_CHANGE",
    carrier: quote?.carrier || "SKT",
    currentCarrier: quote?.currentCarrier,
    activationMethod: quote?.activationMethod || "COMMON_SUBSIDY",
    installmentMonths: DEFAULT_INSTALLMENT_MONTHS,
    pricePlanId: "",
    selectedPricePlan: null,
    pricePlanMaintenanceMonths: BID_FORM_DEFAULTS.PRICE_PLAN_MAINTENANCE_MONTHS,
    lineMaintenanceMonths: BID_FORM_DEFAULTS.LINE_MAINTENANCE_MONTHS,
    additionalServices: [],
    additionalServicesMaintenanceMonths: 0,
    deliveryDays: BID_FORM_DEFAULTS.DELIVERY_DAYS,
  });

  const [errors, setErrors] = useState<BidFormErrors>({});
  const quoteIdLoadedRef = useRef<string | null>(null);

  useEffect(() => {
    if (!quote) {
      quoteIdLoadedRef.current = null;
      return;
    }
    if (quoteIdLoadedRef.current === quote.id) {
      return;
    }
    quoteIdLoadedRef.current = quote.id;
    setFormData({
      devicePrice: BID_FORM_DEFAULTS.DEVICE_PRICE,
      publicSubsidy: 0,
      additionalSubsidy: 0,
      isPayback: false,
      purchaseMethod: quote.purchaseMethod || "DEVICE_CHANGE",
      carrier: quote.carrier || "SKT",
      currentCarrier: quote.currentCarrier,
      activationMethod:
        quote.activationMethod === "ANY"
          ? "COMMON_SUBSIDY"
          : quote.activationMethod || "COMMON_SUBSIDY",
      installmentMonths: DEFAULT_INSTALLMENT_MONTHS,
      pricePlanId: "",
      selectedPricePlan: null,
      pricePlanMaintenanceMonths: BID_FORM_DEFAULTS.PRICE_PLAN_MAINTENANCE_MONTHS,
      lineMaintenanceMonths: BID_FORM_DEFAULTS.LINE_MAINTENANCE_MONTHS,
      additionalServices: [],
      additionalServicesMaintenanceMonths: 0,
      deliveryDays: BID_FORM_DEFAULTS.DELIVERY_DAYS,
    });
    setErrors({});
  }, [quote]);

  const calculations = useMemo(() => {
    const installmentPrincipal = calculateInstallmentPrincipal(
      formData.devicePrice,
      formData.publicSubsidy,
      formData.additionalSubsidy
    );

    const monthlyInstallment = calculateMonthlyInstallment(
      installmentPrincipal,
      formData.installmentMonths,
      DEFAULT_INSTALLMENT_INTEREST_RATE
    );

    const additionalServicesPrice = formData.additionalServices.reduce(
      (sum, service) => sum + service.servicePrice,
      0
    );

    const pricePlanPrice = formData.selectedPricePlan?.monthlyFee ?? 0;

    const totalMonthlyPayment = calculateTotalMonthlyPayment(
      monthlyInstallment,
      pricePlanPrice,
      additionalServicesPrice
    );

    return {
      installmentPrincipal,
      monthlyInstallment,
      totalMonthlyPayment,
      additionalServicesPrice,
      pricePlanPrice,
    };
  }, [formData]);

  const updateField = <K extends keyof BidFormData>(
    field: K,
    value: BidFormData[K]
  ) => {
    setFormData((prev) => {
      if (field === "publicSubsidy" && typeof value === "number") {
        const nextPublic = value;
        return {
          ...prev,
          publicSubsidy: nextPublic,
          additionalSubsidy: prev.isPayback ? -nextPublic : prev.additionalSubsidy,
        };
      }
      if (field === "isPayback" && typeof value === "boolean") {
        return {
          ...prev,
          isPayback: value,
          additionalSubsidy: value ? -prev.publicSubsidy : 0,
        };
      }
      return { ...prev, [field]: value };
    });
    if (errors[field as keyof BidFormErrors]) {
      setErrors((prev) => ({ ...prev, [field]: undefined }));
    }
  };

  const validate = (): boolean => {
    const newErrors: BidFormErrors = {};

    if (formData.devicePrice <= 0) {
      newErrors.devicePrice = "기기 가격을 입력해주세요.";
    }

    if (formData.publicSubsidy < 0) {
      newErrors.publicSubsidy = "공시지원금은 0 이상이어야 합니다.";
    }

    if (!formData.isPayback && formData.additionalSubsidy < 0) {
      newErrors.additionalSubsidy = "추가지원금은 0 이상이어야 합니다.";
    }

    if (!formData.pricePlanId || !formData.selectedPricePlan) {
      newErrors.pricePlanId = "요금제를 선택해주세요.";
    }

    if (formData.deliveryDays < 1) {
      newErrors.deliveryDays = "배송 예상일은 1일 이상이어야 합니다.";
    }

    setErrors(newErrors);
    return Object.keys(newErrors).length === 0;
  };

  const applyServerErrors = (serverErrors: Record<string, unknown>) => {
    const allowedKeys: (keyof BidFormErrors)[] = [
      "devicePrice",
      "publicSubsidy",
      "additionalSubsidy",
      "installmentPrincipal",
      "pricePlanId",
      "deliveryDays",
    ];

    const next: BidFormErrors = {};

    for (const key of allowedKeys) {
      const raw =
        key === "installmentPrincipal"
          ? serverErrors["installmentPrincipal"]
          : serverErrors[key as string];

      if (typeof raw !== "string") {
        next[key] = undefined;
        continue;
      }

      const trimmed = raw.trim();
      next[key] = trimmed.length > 0 ? trimmed : undefined;
    }

    setErrors(next);
  };

  const toBidCreateRequest = (): BidCreateRequest | null => {
    if (!quote || !formData.pricePlanId) return null;
    return {
      quoteId: quote.id,
      price: formData.publicSubsidy + formData.additionalSubsidy,
      deliveryDays: formData.deliveryDays,
      purchaseMethod: formData.purchaseMethod,
      carrier: formData.carrier,
      currentCarrier: formData.currentCarrier,
      activationMethod: formData.activationMethod,
      additionalSubsidy:
        formData.additionalSubsidy !== 0
          ? formData.additionalSubsidy
          : undefined,
      installmentPrincipal: calculations.installmentPrincipal,
      contractMonths: formData.installmentMonths,
      pricePlanId: formData.pricePlanId,
      additionalServices: formData.additionalServices.length > 0 ? formData.additionalServices : undefined,
    };
  };

  const selectPricePlan = (pricePlan: PricePlan) => {
    setFormData((prev) => ({
      ...prev,
      pricePlanId: pricePlan.id,
      selectedPricePlan: pricePlan,
    }));
    if (errors.pricePlanId) {
      setErrors((prev) => ({ ...prev, pricePlanId: undefined }));
    }
  };

  return {
    formData,
    errors,
    calculations,
    updateField,
    validate,
    toBidCreateRequest,
    selectPricePlan,
    applyServerErrors,
  };
};

