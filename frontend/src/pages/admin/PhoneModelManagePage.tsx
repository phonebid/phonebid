import { useCallback, useMemo, useState } from "react";
import useSWR, { mutate } from "swr";
import { defaultSWRConfig } from "services/swrConfig";
import { createPhoneModel } from "services/phoneModelService";
import type {
  Brand,
  PhoneModelCreateRequest,
  PhoneModelOptionRequest,
  PhoneModelResponse,
  PhoneOptionType,
} from "types/PhoneModelTypes";
import { Button } from "@/components/ui/button";
import {
  Card,
  CardContent,
  CardDescription,
  CardHeader,
  CardTitle,
} from "@/components/ui/card";
import Input from "@/components/common/Input";
import { toast } from "react-toastify";

interface FormState {
  brand: Brand | "";
  model: string;
  modelNumber: string;
  releasedPrice: string;
  releasedAt: string;
  options: FormOption[];
}

interface FormOption {
  id: string;
  type: PhoneOptionType | "";
  value: string;
  displayLabel: string;
}

const DEFAULT_FORM: FormState = {
  brand: "",
  model: "",
  modelNumber: "",
  releasedPrice: "",
  releasedAt: "",
  options: [],
};

const BRAND_OPTIONS: { label: string; value: Brand }[] = [
  { label: "Apple", value: "APPLE" },
  { label: "Samsung", value: "SAMSUNG" },
];

const OPTION_TYPE_OPTIONS: { label: string; value: PhoneOptionType }[] = [
  { label: "색상", value: "COLOR" },
  { label: "저장용량", value: "STORAGE" },
];

const PhoneModelManagePage = () => {
  const [form, setForm] = useState<FormState>(DEFAULT_FORM);
  const [isSubmitting, setIsSubmitting] = useState(false);

  const {
    data: models,
    isLoading,
    error,
  } = useSWR<PhoneModelResponse[]>("/phone/models", defaultSWRConfig);

  // const { data: brands } = useSWR("/phone/brands");
  const resetForm = useCallback(() => {
    setForm(DEFAULT_FORM);
  }, []);

  const canSubmit = useMemo(() => {
    if (!form.brand || !form.model.trim()) {
      return false;
    }
    if (form.releasedPrice && Number.isNaN(Number(form.releasedPrice))) {
      return false;
    }
    return true;
  }, [form.brand, form.model, form.releasedPrice]);

  const handleChange = <K extends keyof FormState>(
    key: K,
    value: FormState[K]
  ) => {
    setForm((prev) => ({
      ...prev,
      [key]: value,
    }));
  };

  const handleOptionChange = (
    id: string,
    key: keyof FormOption,
    value: string
  ) => {
    setForm((prev) => ({
      ...prev,
      options: prev.options.map((option) =>
        option.id === id
          ? {
              ...option,
              [key]: value,
            }
          : option
      ),
    }));
  };

  const handleAddOption = () => {
    setForm((prev) => ({
      ...prev,
      options: [
        ...prev.options,
        {
          id: crypto.randomUUID(),
          type: "",
          value: "",
          displayLabel: "",
        },
      ],
    }));
  };

  const handleRemoveOption = (id: string) => {
    setForm((prev) => ({
      ...prev,
      options: prev.options.filter((option) => option.id !== id),
    }));
  };

  const handleSubmit = async () => {
    if (!canSubmit || isSubmitting) {
      return;
    }

    try {
      setIsSubmitting(true);

      const payload: PhoneModelCreateRequest = {
        brand: form.brand as Brand,
        model: form.model.trim(),
        modelNumber: form.modelNumber.trim() || undefined,
        releasedPrice: form.releasedPrice
          ? Number.parseInt(form.releasedPrice, 10)
          : undefined,
        releasedAt: form.releasedAt || undefined,
        options: form.options
          .filter((option) => option.type && option.value)
          .map<PhoneModelOptionRequest>((option) => ({
            type: option.type as PhoneOptionType,
            value: option.value.trim(),
            displayLabel: option.displayLabel.trim() || undefined,
          })),
      };

      await createPhoneModel(payload);

      toast.success("휴대폰 모델이 생성되었습니다.");
      resetForm();
      await mutate("/phone/models");
    } catch (submitError) {
      console.error("Failed to create phone model", submitError);
    } finally {
      setIsSubmitting(false);
    }
  };

  const renderOptions = () => {
    if (form.options.length === 0) {
      return (
        <p className="text-sm text-muted-foreground">
          옵션을 추가하려면 아래 버튼을 클릭하세요.
        </p>
      );
    }

    return (
      <div className="space-y-4">
        {form.options.map((option) => (
          <div
            key={option.id}
            className="grid grid-cols-1 gap-4 rounded-lg border p-4 md:grid-cols-[160px_repeat(3,minmax(0,1fr))_max-content]"
          >
            <div>
              <label className="mb-1 block text-sm font-medium">
                옵션 타입
              </label>
              <select
                className="w-full rounded-md border px-3 py-2"
                value={option.type}
                onChange={(event) =>
                  handleOptionChange(option.id, "type", event.target.value)
                }
              >
                <option value="">선택</option>
                {OPTION_TYPE_OPTIONS.map((item) => (
                  <option key={item.value} value={item.value}>
                    {item.label}
                  </option>
                ))}
              </select>
            </div>

            <Input
              label="값"
              value={option.value}
              onChange={(value) =>
                handleOptionChange(option.id, "value", value)
              }
              className="md:mt-6"
            />

            <Input
              label="표시명 (선택)"
              value={option.displayLabel}
              onChange={(value) =>
                handleOptionChange(option.id, "displayLabel", value)
              }
              className="md:mt-6"
            />

            <div className="flex items-end justify-end md:mt-6">
              <Button
                type="button"
                variant="ghost"
                onClick={() => handleRemoveOption(option.id)}
              >
                삭제
              </Button>
            </div>
          </div>
        ))}
      </div>
    );
  };

  return (
    <div className="mx-auto max-w-6xl space-y-8 p-6">
      <div>
        <h1 className="text-2xl font-bold">휴대폰 모델 관리</h1>
        <p className="text-sm text-muted-foreground">
          신규 모델을 등록하고 기본 옵션을 함께 생성할 수 있습니다.
        </p>
      </div>

      <Card>
        <CardHeader>
          <CardTitle>모델 생성</CardTitle>
          <CardDescription>
            브랜드와 모델명은 필수 입력입니다. 출시 정보와 옵션은 필요 시
            입력하세요.
          </CardDescription>
        </CardHeader>
        <CardContent className="space-y-6">
          <div className="grid grid-cols-1 gap-6 md:grid-cols-2">
            <div>
              <label className="mb-1 block text-sm font-medium">브랜드</label>
              <select
                className="w-full rounded-md border px-3 py-2"
                value={form.brand}
                onChange={(event) =>
                  handleChange("brand", event.target.value as Brand | "")
                }
              >
                <option value="">브랜드 선택</option>
                {BRAND_OPTIONS.map((option) => (
                  <option key={option.value} value={option.value}>
                    {option.label}
                  </option>
                ))}
              </select>
            </div>
            <Input
              label="모델명"
              required
              value={form.model}
              onChange={(value) => handleChange("model", value)}
            />
            <Input
              label="모델 번호"
              value={form.modelNumber}
              onChange={(value) => handleChange("modelNumber", value)}
            />
            <Input
              label="출시가"
              type="number"
              value={form.releasedPrice}
              onChange={(value) => handleChange("releasedPrice", value)}
            />
            <div>
              <label className="mb-1 block text-sm font-medium">출시일</label>
              <input
                type="date"
                className="w-full rounded-md border px-3 py-2"
                value={form.releasedAt}
                onChange={(event) =>
                  handleChange("releasedAt", event.target.value)
                }
              />
            </div>
          </div>

          <div className="space-y-4">
            <div className="flex items-center justify-between">
              <div>
                <h3 className="text-lg font-semibold">기본 옵션</h3>
                <p className="text-sm text-muted-foreground">
                  색상, 저장 용량 등의 옵션을 등록하면 견적 작성 시 선택할 수
                  있습니다.
                </p>
              </div>
              <Button
                type="button"
                variant="secondary"
                onClick={handleAddOption}
              >
                옵션 추가
              </Button>
            </div>

            {renderOptions()}
          </div>

          <div className="flex justify-end gap-3">
            <Button type="button" variant="ghost" onClick={resetForm}>
              초기화
            </Button>
            <Button
              type="button"
              onClick={handleSubmit}
              disabled={!canSubmit || isSubmitting}
            >
              {isSubmitting ? "등록 중..." : "모델 등록"}
            </Button>
          </div>
        </CardContent>
      </Card>

      <Card>
        <CardHeader>
          <CardTitle>등록된 모델 목록</CardTitle>
          <CardDescription>
            생성된 모델을 확인할 수 있습니다. 옵션 정보도 함께 표시됩니다.
          </CardDescription>
        </CardHeader>
        <CardContent>
          {isLoading && (
            <p className="text-sm text-muted-foreground">
              목록을 불러오는 중입니다...
            </p>
          )}
          {error && (
            <p className="text-sm text-red-600">목록을 불러오지 못했습니다.</p>
          )}
          {!isLoading && !error && (
            <div className="space-y-6">
              {models && models.length > 0 ? (
                models.map((model) => (
                  <div key={model.id} className="rounded-lg border p-4">
                    <div className="flex flex-col gap-2 md:flex-row md:items-center md:justify-between">
                      <div>
                        <p className="text-sm font-medium text-muted-foreground">
                          {model.brand}
                        </p>
                        <h3 className="text-lg font-semibold">{model.model}</h3>
                        {model.modelNumber && (
                          <p className="text-sm text-muted-foreground">
                            모델 번호: {model.modelNumber}
                          </p>
                        )}
                      </div>
                      <div className="text-sm text-muted-foreground">
                        {model.releasedPrice ? (
                          <p>
                            출시가: {model.releasedPrice.toLocaleString()}원
                          </p>
                        ) : (
                          <p>출시가 정보 없음</p>
                        )}
                        <p>
                          출시일:{" "}
                          {model.releasedAt
                            ? new Date(model.releasedAt).toLocaleDateString()
                            : "미입력"}
                        </p>
                      </div>
                    </div>
                    {model.options && model.options.length > 0 && (
                      <div className="mt-4 space-y-2">
                        <h4 className="text-sm font-semibold">옵션</h4>
                        <div className="grid grid-cols-1 gap-2 md:grid-cols-2">
                          {model.options.map((option) => (
                            <div
                              key={option.id}
                              className="flex items-center justify-between rounded-md bg-muted px-3 py-2 text-sm"
                            >
                              <span className="font-medium">
                                {option.optionType}
                              </span>
                              <span>
                                {option.displayLabel || option.optionValue}
                              </span>
                            </div>
                          ))}
                        </div>
                      </div>
                    )}
                  </div>
                ))
              ) : (
                <p className="text-sm text-muted-foreground">
                  등록된 모델이 없습니다.
                </p>
              )}
            </div>
          )}
        </CardContent>
      </Card>
    </div>
  );
};

export default PhoneModelManagePage;
