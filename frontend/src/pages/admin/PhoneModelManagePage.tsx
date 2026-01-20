import { useCallback, useMemo, useState, useRef, useEffect } from "react";
import useSWR, { mutate } from "swr";
import { defaultSWRConfig } from "services/swrConfig";
import {
  createPhoneModel,
  updatePhoneModel,
  deletePhoneModel,
  uploadPhoneModelImages,
  getPhoneModelImages,
  deletePhoneModelImage,
} from "services/phoneModelService";
import type {
  Brand,
  PhoneModelCreateRequest,
  PhoneModelUpdateRequest,
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
import { Pencil, Trash2 } from "lucide-react";
import { getErrorMessage, logError } from "@/utils/errorUtils";

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

// 모델 목록 아이템 컴포넌트
const ModelListItem = ({
  model,
  selectedModelId,
  onSelect,
  onEdit,
  onDelete,
}: {
  model: PhoneModelResponse;
  selectedModelId: string | null;
  onSelect: () => void;
  onEdit: (model: PhoneModelResponse) => void;
  onDelete: (modelId: string) => void;
}) => {
  const { data: images } = useSWR<PhoneModelImageResponse[]>(
    `/phone/models/${model.id}/images`,
    defaultSWRConfig
  );

  const firstImage = images && images.length > 0 ? images[0] : null;

  const handleEditClick = (e: React.MouseEvent) => {
    e.stopPropagation();
    onEdit(model);
  };

  const handleDeleteClick = (e: React.MouseEvent) => {
    e.stopPropagation();
    if (window.confirm(`"${model.brand} ${model.model}" 모델을 삭제하시겠습니까?`)) {
      onDelete(model.id);
    }
  };

  return (
    <div
      className={`rounded-lg border p-4 transition-colors ${
        selectedModelId === model.id
          ? "border-indigo-500 bg-indigo-50"
          : "hover:bg-gray-50"
      }`}
    >
      <div className="flex flex-col gap-4 md:flex-row md:items-start">
        {/* 이미지 영역 */}
        <div className="flex-shrink-0 cursor-pointer" onClick={onSelect}>
          {firstImage ? (
            <img
              src={firstImage.imageUrl}
              alt={model.model}
              className="w-24 h-24 object-cover rounded-lg border"
            />
          ) : (
            <div className="w-24 h-24 rounded-lg border bg-muted flex items-center justify-center">
              <svg
                className="w-12 h-12 text-muted-foreground"
                fill="none"
                stroke="currentColor"
                viewBox="0 0 24 24"
              >
                <path
                  strokeLinecap="round"
                  strokeLinejoin="round"
                  strokeWidth={2}
                  d="M4 16l4.586-4.586a2 2 0 012.828 0L16 16m-2-2l1.586-1.586a2 2 0 012.828 0L20 14m-6-6h.01M6 20h12a2 2 0 002-2V6a2 2 0 00-2-2H6a2 2 0 00-2 2v12a2 2 0 002 2z"
                />
              </svg>
            </div>
          )}
        </div>

        {/* 모델 정보 영역 */}
        <div className="flex-1 flex flex-col gap-2 md:flex-row md:items-center md:justify-between">
          <div className="flex-1 cursor-pointer" onClick={onSelect}>
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
          <div className="flex items-center gap-4">
            <div className="text-sm text-muted-foreground">
              {model.releasedPrice ? (
                <p>출시가: {model.releasedPrice.toLocaleString()}원</p>
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
            <div className="flex gap-2">
              <button
                type="button"
                onClick={handleEditClick}
                className="p-2 text-gray-700 hover:text-gray-900 hover:bg-gray-100 rounded-md transition-colors"
                aria-label="수정"
              >
                <Pencil className="w-4 h-4" />
              </button>
              <button
                type="button"
                onClick={handleDeleteClick}
                className="p-2 text-gray-700 hover:text-gray-900 hover:bg-gray-100 rounded-md transition-colors"
                aria-label="삭제"
              >
                <Trash2 className="w-4 h-4" />
              </button>
            </div>
          </div>
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
                <span className="font-medium">{option.optionType}</span>
                <span>
                  {option.displayLabel || option.optionValue}
                </span>
              </div>
            ))}
          </div>
        </div>
      )}
    </div>
  );
};

const PhoneModelManagePage = () => {
  const [form, setForm] = useState<FormState>(DEFAULT_FORM);
  const [isSubmitting, setIsSubmitting] = useState(false);
  const [selectedModelId, setSelectedModelId] = useState<string | null>(null);
  const [modelImages, setModelImages] = useState<PhoneModelImageResponse[]>([]);
  const [isUploadingImages, setIsUploadingImages] = useState(false);
  const [isLoadingImages, setIsLoadingImages] = useState(false);
  const [imagePreviews, setImagePreviews] = useState<string[]>([]);
  const imagePreviewsRef = useRef<string[]>([]);
  const createFormFileInputRef = useRef<HTMLInputElement>(null);
  const modelImageFileInputRef = useRef<HTMLInputElement>(null);
  const [editingModel, setEditingModel] = useState<PhoneModelResponse | null>(null);
  const [editForm, setEditForm] = useState<FormState>(DEFAULT_FORM);
  const [isUpdating, setIsUpdating] = useState(false);
  const modalCardRef = useRef<HTMLDivElement>(null);

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
        } catch (e) {
          logError("모델은 생성되었지만 이미지 업로드에 실패했습니다.", getErrorMessage(e));
          toast.error("모델은 생성되었지만 이미지 업로드에 실패했습니다.");
        } finally {
          setIsUploadingImages(false);
        }
      } else {
        toast.success("휴대폰 모델이 생성되었습니다.");
      }

      resetForm();
      setSelectedModelId(createdModel.id);
      await mutate("/phone/models");
      // 생성된 모델의 이미지 로드
      await loadModelImages(createdModel.id);
    } catch (e) {
      logError("휴대폰 모델 생성에 실패했습니다.", getErrorMessage(e));
      toast.error("휴대폰 모델 생성에 실패했습니다.");
    } finally {
      setIsSubmitting(false);
    }
  };

  const loadModelImages = async (modelId: string) => {
    try {
      setIsLoadingImages(true);
      const images = await getPhoneModelImages(modelId);
      setModelImages(images);
    } catch (e) {
      logError("모델 이미지를 불러오는데 실패했습니다.", getErrorMessage(e));
      toast.error("모델 이미지를 불러오는데 실패했습니다.");
    } finally {
      setIsLoadingImages(false);
    }
  };

  const handleCreateFormImageUploadClick = () => {
    createFormFileInputRef.current?.click();
  };

  const handleModelImageUploadClick = () => {
    modelImageFileInputRef.current?.click();
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
    if (createFormFileInputRef.current) {
      createFormFileInputRef.current.value = "";
    }
  };

  const handleModelImageUpload = async (e: React.ChangeEvent<HTMLInputElement>) => {
    const files = Array.from(e.target.files || []);
    if (!files.length) return;

    if (!selectedModelId) {
      toast.error("모델을 선택해주세요.");
      return;
    }

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

    const currentImageCount = modelImages.length;
    if (currentImageCount + files.length > 10) {
      toast.error("이미지는 최대 10개까지 업로드할 수 있습니다.");
      return;
    }

    try {
      setIsUploadingImages(true);
      await uploadPhoneModelImages(selectedModelId, files);
      toast.success("이미지가 업로드되었습니다.");
      await loadModelImages(selectedModelId);
    } catch (e) {
      logError("이미지 업로드에 실패했습니다.", getErrorMessage(e));
      toast.error("이미지 업로드에 실패했습니다.");
    } finally {
      setIsUploadingImages(false);
      if (modelImageFileInputRef.current) {
        modelImageFileInputRef.current.value = "";
      }
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
    } catch (e) {
      logError("이미지 삭제에 실패했습니다.", getErrorMessage(e));
      toast.error("이미지 삭제에 실패했습니다.");
    }
  };

  // imagePreviews 변경 시 ref 업데이트
  useEffect(() => {
    imagePreviewsRef.current = imagePreviews;
  }, [imagePreviews]);

  // 컴포넌트 언마운트 시 미리보기 URL 정리
  useEffect(() => {
    return () => {
      imagePreviewsRef.current.forEach((preview) => URL.revokeObjectURL(preview));
    };
  }, []);

  // 모달 외부 클릭 시 닫기
  useEffect(() => {
    const handleClickOutside = (event: MouseEvent) => {
      if (
        editingModel &&
        modalCardRef.current &&
        !modalCardRef.current.contains(event.target as Node)
      ) {
        setEditingModel(null);
        setEditForm(DEFAULT_FORM);
      }
    };

    if (editingModel) {
      document.addEventListener("mousedown", handleClickOutside);
    }

    return () => {
      document.removeEventListener("mousedown", handleClickOutside);
    };
  }, [editingModel]);

  const handleSelectModel = async (modelId: string) => {
    setSelectedModelId(modelId);
    await loadModelImages(modelId);
  };

  const handleEditModel = (model: PhoneModelResponse) => {
    setEditingModel(model);
    let releasedAtValue: string = "";
    if (model.releasedAt) {
      try {
        const dateStr = new Date(model.releasedAt).toISOString().split("T")[0];
        releasedAtValue = dateStr || "";
      } catch {
        releasedAtValue = "";
      }
    }
    setEditForm({
      brand: model.brand,
      model: model.model,
      modelNumber: model.modelNumber ?? "",
      releasedPrice: model.releasedPrice?.toString() ?? "",
      releasedAt: releasedAtValue,
      options: model.options
        ? model.options.map((opt): FormOption => ({
            id: opt.id,
            type: opt.optionType,
            value: opt.optionValue,
            displayLabel: opt.displayLabel ?? "",
          }))
        : [],
      selectedImages: [],
    });
  };

  const handleUpdateModel = async () => {
    if (!editingModel) return;

    if (!editForm.brand || !editForm.model.trim()) {
      toast.error("브랜드와 모델명은 필수입니다.");
      return;
    }

    try {
      setIsUpdating(true);
      const payload: PhoneModelUpdateRequest = {
        brand: editForm.brand as Brand,
        model: editForm.model.trim(),
        modelNumber: editForm.modelNumber.trim() || undefined,
        releasedPrice: editForm.releasedPrice
          ? Number.parseInt(editForm.releasedPrice, 10)
          : undefined,
        releasedAt: editForm.releasedAt || undefined,
      };

      await updatePhoneModel(editingModel.id, payload);
      toast.success("모델이 수정되었습니다.");
      setEditingModel(null);
      setEditForm(DEFAULT_FORM);
      await mutate("/phone/models");
    } catch (e) {
      logError("모델 수정에 실패했습니다.", getErrorMessage(e));
      toast.error("모델 수정에 실패했습니다.");
    } finally {
      setIsUpdating(false);
    }
  };

  const handleDeleteModel = async (modelId: string) => {
    try {
      await deletePhoneModel(modelId);
      toast.success("모델이 삭제되었습니다.");
      if (selectedModelId === modelId) {
        setSelectedModelId(null);
        setModelImages([]);
      }
      await mutate("/phone/models");
    } catch (e) {
      logError("모델 삭제에 실패했습니다.", getErrorMessage(e));
      toast.error("모델 삭제에 실패했습니다.");
    }
  };

  const handleEditFormChange = <K extends keyof FormState>(
    key: K,
    value: FormState[K]
  ) => {
    setEditForm((prev) => ({
      ...prev,
      [key]: value,
    }));
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
                onClick={handleCreateFormImageUploadClick}
                disabled={form.selectedImages.length >= 10}
              >
                이미지 추가
              </Button>
            </div>
            <input
              ref={createFormFileInputRef}
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
              ref={modelImageFileInputRef}
              type="file"
              accept="image/jpeg,image/jpg,image/png,image/gif,image/webp"
              multiple
              className="hidden"
              onChange={handleModelImageUpload}
              disabled={isUploadingImages}
            />
            <div className="flex items-center justify-between">
              <p className="text-sm text-muted-foreground">
                업로드된 이미지: {modelImages.length} / 10
              </p>
              <Button
                type="button"
                variant="secondary"
                onClick={handleModelImageUploadClick}
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
                  <ModelListItem
                    key={model.id}
                    model={model}
                    selectedModelId={selectedModelId}
                    onSelect={() => handleSelectModel(model.id)}
                    onEdit={handleEditModel}
                    onDelete={handleDeleteModel}
                  />
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

      {/* 수정 모달 */}
      {editingModel && (
        <div className="fixed inset-0 bg-black/50 flex items-center justify-center z-50 p-4">
          <div ref={modalCardRef}>
            <Card className="w-full max-w-2xl max-h-[90vh] overflow-y-auto">
            <CardHeader>
              <CardTitle>모델 수정</CardTitle>
              <CardDescription>
                모델 정보를 수정할 수 있습니다.
              </CardDescription>
            </CardHeader>
            <CardContent className="space-y-6">
              <div className="grid grid-cols-1 gap-6 md:grid-cols-2">
                <div>
                  <label className="mb-1 block text-sm font-medium">브랜드</label>
                  <select
                    className="w-full rounded-md border px-3 py-2"
                    value={editForm.brand}
                    onChange={(event) =>
                      handleEditFormChange("brand", event.target.value as Brand | "")
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
                  value={editForm.model}
                  onChange={(value) => handleEditFormChange("model", value)}
                />
                <Input
                  label="모델 번호"
                  value={editForm.modelNumber}
                  onChange={(value) => handleEditFormChange("modelNumber", value)}
                />
                <Input
                  label="출시가"
                  type="number"
                  value={editForm.releasedPrice}
                  onChange={(value) => handleEditFormChange("releasedPrice", value)}
                />
                <div>
                  <label className="mb-1 block text-sm font-medium">출시일</label>
                  <input
                    type="date"
                    className="w-full rounded-md border px-3 py-2"
                    value={editForm.releasedAt || ""}
                    onChange={(event) =>
                      handleEditFormChange("releasedAt", event.target.value)
                    }
                  />
                </div>
              </div>

              <div className="flex justify-end gap-3">
                <Button
                  type="button"
                  variant="ghost"
                  onClick={() => {
                    setEditingModel(null);
                    setEditForm(DEFAULT_FORM);
                  }}
                >
                  취소
                </Button>
                <Button
                  type="button"
                  onClick={handleUpdateModel}
                  disabled={isUpdating || !editForm.brand || !editForm.model.trim()}
                >
                  {isUpdating ? "수정 중..." : "수정 완료"}
                </Button>
              </div>
            </CardContent>
          </Card>
          </div>
        </div>
      )}
    </div>
  );
};

export default PhoneModelManagePage;
