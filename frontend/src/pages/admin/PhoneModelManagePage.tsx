import { useCallback, useMemo, useState, useRef, useEffect } from "react";
import useSWR, { mutate } from "swr";
import { defaultSWRConfig } from "services/swrConfig";
import {
  createPhoneModel,
  uploadPhoneModelImages,
  getPhoneModelImages,
  deletePhoneModelImage,
} from "services/phoneModelService";
import type {
  Brand,
  PhoneModelCreateRequest,
  PhoneModelOptionRequest,
  PhoneModelResponse,
  PhoneOptionType,
  PhoneModelImageResponse,
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
  selectedImages: File[];
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
  selectedImages: [],
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
  const [selectedModelId, setSelectedModelId] = useState<string | null>(null);
  const [modelImages, setModelImages] = useState<PhoneModelImageResponse[]>([]);
  const [isUploadingImages, setIsUploadingImages] = useState(false);
  const [isLoadingImages, setIsLoadingImages] = useState(false);
  const [imagePreviews, setImagePreviews] = useState<string[]>([]);
  const fileInputRef = useRef<HTMLInputElement>(null);

  const {
    data: models,
    isLoading,
    error,
  } = useSWR<PhoneModelResponse[]>("/phone/models", defaultSWRConfig);

  // const { data: brands } = useSWR("/phone/brands");
  const resetForm = useCallback(() => {
    setForm(DEFAULT_FORM);
    setImagePreviews([]);
    setSelectedModelId(null);
    setModelImages([]);
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

      const createdModel = await createPhoneModel(payload);

      // 모델 생성 후 선택된 이미지들 업로드
      if (form.selectedImages.length > 0) {
        try {
          setIsUploadingImages(true);
          await uploadPhoneModelImages(createdModel.id, form.selectedImages);
          toast.success("휴대폰 모델과 이미지가 생성되었습니다.");
        } catch (imageError) {
          console.error("Failed to upload images", imageError);
          toast.error("모델은 생성되었지만 이미지 업로드에 실패했습니다.");
        } finally {
          setIsUploadingImages(false);
        }
      } else {
        toast.success("휴대폰 모델이 생성되었습니다.");
      }

      setSelectedModelId(createdModel.id);
      resetForm();
      await mutate("/phone/models");
      // 생성된 모델의 이미지 로드
      await loadModelImages(createdModel.id);
    } catch (submitError) {
      console.error("Failed to create phone model", submitError);
    } finally {
      setIsSubmitting(false);
    }
  };

  const loadModelImages = async (modelId: string) => {
    try {
      setIsLoadingImages(true);
      const images = await getPhoneModelImages(modelId);
      setModelImages(images);
    } catch (error) {
      console.error("Failed to load model images", error);
    } finally {
      setIsLoadingImages(false);
    }
  };

  const handleImageUploadClick = () => {
    fileInputRef.current?.click();
  };

  const handleImageFileChange = (e: React.ChangeEvent<HTMLInputElement>) => {
    const files = Array.from(e.target.files || []);
    if (!files.length) return;

    // 파일 유효성 검사
    const allowedTypes = [
      "image/jpeg",
      "image/jpg",
      "image/png",
      "image/gif",
      "image/webp",
    ];
    const invalidFiles = files.filter((file) => !allowedTypes.includes(file.type));
    if (invalidFiles.length > 0) {
      toast.error(
        "지원하지 않는 파일 형식이 있습니다. (jpg, jpeg, png, gif, webp만 가능)"
      );
      return;
    }

    const maxSize = 5 * 1024 * 1024; // 5MB
    const oversizedFiles = files.filter((file) => file.size > maxSize);
    if (oversizedFiles.length > 0) {
      toast.error("파일 크기는 5MB 이하여야 합니다.");
      return;
    }

    const currentImageCount = form.selectedImages.length;
    if (currentImageCount + files.length > 10) {
      toast.error("이미지는 최대 10개까지 선택할 수 있습니다.");
      return;
    }

    // 이미지 파일 추가
    setForm((prev) => ({
      ...prev,
      selectedImages: [...prev.selectedImages, ...files],
    }));

    // 미리보기 생성
    const newPreviews = files.map((file) => URL.createObjectURL(file));
    setImagePreviews((prev) => [...prev, ...newPreviews]);

    // 파일 input 초기화
    if (fileInputRef.current) {
      fileInputRef.current.value = "";
    }
  };

  const handleRemoveSelectedImage = (index: number) => {
    setForm((prev) => ({
      ...prev,
      selectedImages: prev.selectedImages.filter((_, i) => i !== index),
    }));
    setImagePreviews((prev) => {
      const removedPreview = prev[index];
      if (removedPreview) {
        URL.revokeObjectURL(removedPreview);
      }
      return prev.filter((_, i) => i !== index);
    });
  };

  const handleImageDelete = async (imageId: string) => {
    if (!selectedModelId) return;

    if (!window.confirm("이미지를 삭제하시겠습니까?")) {
      return;
    }

    try {
      await deletePhoneModelImage(selectedModelId, imageId);
      setModelImages((prev) => prev.filter((img) => img.id !== imageId));
      toast.success("이미지가 삭제되었습니다.");
    } catch (error) {
      console.error("Failed to delete image", error);
    }
  };

  // 컴포넌트 언마운트 시 미리보기 URL 정리
  useEffect(() => {
    return () => {
      imagePreviews.forEach((preview) => URL.revokeObjectURL(preview));
    };
  }, [imagePreviews]);

  const handleSelectModel = async (modelId: string) => {
    setSelectedModelId(modelId);
    await loadModelImages(modelId);
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

          <div className="space-y-4">
            <div className="flex items-center justify-between">
              <div>
                <h3 className="text-lg font-semibold">모델 이미지</h3>
                <p className="text-sm text-muted-foreground">
                  모델 이미지를 업로드할 수 있습니다. (최대 10개, 선택사항)
                </p>
              </div>
              <Button
                type="button"
                variant="secondary"
                onClick={handleImageUploadClick}
                disabled={form.selectedImages.length >= 10}
              >
                이미지 추가
              </Button>
            </div>
            <input
              ref={fileInputRef}
              type="file"
              accept="image/jpeg,image/jpg,image/png,image/gif,image/webp"
              multiple
              className="hidden"
              onChange={handleImageFileChange}
            />
            {form.selectedImages.length > 0 ? (
              <div className="grid grid-cols-2 gap-4 md:grid-cols-4 lg:grid-cols-5">
                {form.selectedImages.map((_, index) => (
                  <div key={index} className="relative group">
                    <img
                      src={imagePreviews[index]}
                      alt={`미리보기 ${index + 1}`}
                      className="w-full h-32 object-cover rounded-lg border"
                    />
                    <button
                      className="absolute top-1 right-1 w-6 h-6 bg-red-500 rounded-full flex items-center justify-center opacity-0 group-hover:opacity-100 transition-opacity"
                      onClick={() => handleRemoveSelectedImage(index)}
                      type="button"
                      aria-label="이미지 제거"
                    >
                      <svg
                        className="w-4 h-4 text-white"
                        fill="none"
                        stroke="currentColor"
                        viewBox="0 0 24 24"
                      >
                        <path
                          strokeLinecap="round"
                          strokeLinejoin="round"
                          strokeWidth={2}
                          d="M6 18L18 6M6 6l12 12"
                        />
                      </svg>
                    </button>
                  </div>
                ))}
              </div>
            ) : (
              <p className="text-sm text-muted-foreground">
                선택된 이미지가 없습니다. 이미지를 추가하려면 위 버튼을 클릭하세요.
              </p>
            )}
            {form.selectedImages.length > 0 && (
              <p className="text-sm text-muted-foreground">
                선택된 이미지: {form.selectedImages.length} / 10
              </p>
            )}
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

      {selectedModelId && (
        <Card>
          <CardHeader>
            <CardTitle>모델 이미지 관리</CardTitle>
            <CardDescription>
              생성된 모델의 이미지를 업로드할 수 있습니다. (최대 10개)
            </CardDescription>
          </CardHeader>
          <CardContent className="space-y-4">
            <input
              ref={fileInputRef}
              type="file"
              accept="image/jpeg,image/jpg,image/png,image/gif,image/webp"
              multiple
              className="hidden"
              onChange={handleImageFileChange}
              disabled={isUploadingImages}
            />
            <div className="flex items-center justify-between">
              <p className="text-sm text-muted-foreground">
                업로드된 이미지: {modelImages.length} / 10
              </p>
              <Button
                type="button"
                variant="secondary"
                onClick={handleImageUploadClick}
                disabled={isUploadingImages || modelImages.length >= 10}
              >
                {isUploadingImages ? "업로드 중..." : "이미지 추가"}
              </Button>
            </div>
            {isLoadingImages ? (
              <p className="text-sm text-muted-foreground">
                이미지를 불러오는 중...
              </p>
            ) : modelImages.length > 0 ? (
              <div className="grid grid-cols-2 gap-4 md:grid-cols-4 lg:grid-cols-5">
                {modelImages.map((image) => (
                  <div key={image.id} className="relative group">
                    <img
                      src={image.imageUrl}
                      alt={`모델 이미지 ${image.displayOrder}`}
                      className="w-full h-32 object-cover rounded-lg border"
                    />
                    <button
                      className="absolute top-1 right-1 w-6 h-6 bg-red-500 rounded-full flex items-center justify-center opacity-0 group-hover:opacity-100 transition-opacity"
                      onClick={() => handleImageDelete(image.id)}
                      aria-label="이미지 삭제"
                    >
                      <svg
                        className="w-4 h-4 text-white"
                        fill="none"
                        stroke="currentColor"
                        viewBox="0 0 24 24"
                      >
                        <path
                          strokeLinecap="round"
                          strokeLinejoin="round"
                          strokeWidth={2}
                          d="M6 18L18 6M6 6l12 12"
                        />
                      </svg>
                    </button>
                  </div>
                ))}
              </div>
            ) : (
              <p className="text-sm text-muted-foreground">
                업로드된 이미지가 없습니다.
              </p>
            )}
          </CardContent>
        </Card>
      )}

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
                  <div
                    key={model.id}
                    className={`rounded-lg border p-4 cursor-pointer transition-colors ${
                      selectedModelId === model.id
                        ? "border-indigo-500 bg-indigo-50"
                        : "hover:bg-gray-50"
                    }`}
                    onClick={() => handleSelectModel(model.id)}
                  >
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
