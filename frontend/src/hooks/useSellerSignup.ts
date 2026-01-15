import { useState, useEffect } from "react";
import { useNavigate } from "react-router-dom";
import { sellerService } from "services/sellerService";
import { toast } from "react-toastify";
import { AxiosError, isAxiosError } from "axios";
import type {
  SellerRegisterRequestDto,
  AddressDto,
  SettlementAccountDto,
  BankName,
} from "types/SellerTypes";
import { validateStep1, validateStep2, type Step1Data, type Step2Data } from "utils/sellerValidation";

export interface UseSellerSignupReturn {
  step: 1 | 2;
  step1Data: Step1Data;
  step2Data: Step2Data;
  errors: Record<string, string>;
  isLoading: boolean;
  setStep: React.Dispatch<React.SetStateAction<1 | 2>>;
  setStep1Data: React.Dispatch<React.SetStateAction<Step1Data>>;
  setStep2Data: React.Dispatch<React.SetStateAction<Step2Data>>;
  handleAddressSearch: (type: "business" | "store") => void;
  handleFileChange: (
    e: React.ChangeEvent<HTMLInputElement>,
    type: "businessLicense" | "consentForm"
  ) => Promise<void>;
  handleStep1Next: () => void;
  handleSubmit: () => Promise<void>;
}

export const useSellerSignup = (): UseSellerSignupReturn => {
  const navigate = useNavigate();
  const [step, setStep] = useState<1 | 2>(1);

  const [step1Data, setStep1Data] = useState<Step1Data>({
    isAgent: false,
    businessNumber: "",
    businessLicenseFile: null as File | null,
    businessLicenseFileUrl: "",
    storeName: "",
    representativeName: "",
    businessPostalCode: "",
    businessAddress: "",
    businessDetailAddress: "",
    storePostalCode: "",
    storeAddress: "",
    storeDetailAddress: "",
    consentNumber: "",
    consentFormFile: null as File | null,
    consentFormFileUrl: "",
    representativePhone: "",
    email: "",
    customerServicePhone: "",
    bankName: "" as BankName | "",
    accountNumber: "",
    accountHolderName: "",
  });

  const [step2Data, setStep2Data] = useState<Step2Data>({
    username: "",
    password: "",
    confirmPassword: "",
    name: "",
    nickname: "",
    termsOfService: false,
    privacyPolicy: false,
  });

  const [errors, setErrors] = useState<Record<string, string>>({});
  const [isLoading, setIsLoading] = useState(false);

  useEffect(() => {
    if (!window.daum) {
      const script = document.createElement("script");
      script.src =
        "//t1.daumcdn.net/mapjsapi/bundle/postcode/prod/postcode.v2.js";
      script.async = true;
      document.head.appendChild(script);
    }
  }, []);

  const handleAddressSearch = (type: "business" | "store") => {
    if (!window.daum || !window.daum.Postcode) {
      toast.error("주소 검색 서비스를 불러올 수 없습니다.");
      return;
    }

    new window.daum.Postcode({
      oncomplete: (data) => {
        if (type === "business") {
          setStep1Data((prev) => ({
            ...prev,
            businessPostalCode: data.zonecode,
            businessAddress: data.address,
          }));
        } else {
          setStep1Data((prev) => ({
            ...prev,
            storePostalCode: data.zonecode,
            storeAddress: data.address,
          }));
        }
      },
      width: "100%",
      height: "100%",
    }).open();
  };

  const handleFileUpload = async (
    file: File,
    type: "businessLicense" | "consentForm"
  ): Promise<string> => {
    const documentType =
      type === "businessLicense" ? "BUSINESS_LICENSE" : "CONSENT_FORM";
    const fileUrl = await sellerService.uploadDocument(file, documentType);
    return fileUrl;
  };

  const handleFileChange = async (
    e: React.ChangeEvent<HTMLInputElement>,
    type: "businessLicense" | "consentForm"
  ) => {
    const file = e.target.files?.[0];
    if (!file) return;

    if (file.size > 10 * 1024 * 1024) {
      toast.error("파일 크기는 10MB 이하여야 합니다.");
      return;
    }

    const allowedTypes = ["image/jpeg", "image/png", "application/pdf"];
    if (!allowedTypes.includes(file.type)) {
      toast.error("JPG, PNG, PDF 파일만 업로드 가능합니다.");
      return;
    }

    setIsLoading(true);
    try {
      const fileUrl = await handleFileUpload(file, type);
      if (type === "businessLicense") {
        setStep1Data((prev) => ({
          ...prev,
          businessLicenseFile: file,
          businessLicenseFileUrl: fileUrl,
        }));
      } else {
        setStep1Data((prev) => ({
          ...prev,
          consentFormFile: file,
          consentFormFileUrl: fileUrl,
        }));
      }
      toast.success("파일이 업로드되었습니다.");
    } catch (error) {
      toast.error("파일 업로드에 실패했습니다.");
    } finally {
      setIsLoading(false);
    }
  };

  const handleStep1Next = () => {
    const { isValid, errors: validationErrors } = validateStep1(step1Data);
    setErrors(validationErrors);
    if (isValid) {
      setStep(2);
    }
  };

  const handleSubmit = async () => {
    const { isValid, errors: validationErrors } = validateStep2(step2Data);
    setErrors(validationErrors);
    if (!isValid) {
      return;
    }

    setIsLoading(true);
    try {
      const businessAddress: AddressDto = {
        postalCode: step1Data.businessPostalCode,
        address: step1Data.businessAddress,
        detailAddress: step1Data.businessDetailAddress,
      };

      const storeAddress: AddressDto = step1Data.isAgent
        ? {
            postalCode: step1Data.businessPostalCode,
            address: step1Data.businessAddress,
            detailAddress: step1Data.businessDetailAddress,
          }
        : {
            postalCode: step1Data.storePostalCode,
            address: step1Data.storeAddress,
            detailAddress: step1Data.storeDetailAddress,
          };

      const settlementAccount: SettlementAccountDto = {
        bankName: step1Data.bankName,
        accountNumber: step1Data.accountNumber,
        accountHolderName: step1Data.accountHolderName,
      };

      const requestDto: SellerRegisterRequestDto = {
        businessNumber: step1Data.businessNumber.replace(/-/g, ""),
        businessLicenseFileUrl: step1Data.businessLicenseFileUrl,
        storeName: step1Data.storeName,
        representativeName: step1Data.representativeName,
        isAgent: step1Data.isAgent,
        businessAddress,
        storeAddress,
        consentNumber: step1Data.consentNumber || undefined,
        consentFormFileUrl: step1Data.consentFormFileUrl || undefined,
        representativePhone: step1Data.representativePhone,
        email: step1Data.email,
        customerServicePhone: step1Data.customerServicePhone || undefined,
        settlementAccount,
        userInfo: {
          username: step2Data.username,
          password: step2Data.password,
          name: step2Data.name,
          nickname: step2Data.nickname,
        },
      };

      await sellerService.registerSeller(requestDto);
      navigate("/seller/login");
    } catch (error: unknown) {
      let errorMessage = "회원가입에 실패했습니다.";
      if (isAxiosError(error) && error.response?.data?.message) {
        errorMessage = error.response.data.message;
      }
      toast.error(errorMessage);
    } finally {
      setIsLoading(false);
    }
  };

  return {
    step,
    step1Data,
    step2Data,
    errors,
    isLoading,
    setStep,
    setStep1Data,
    setStep2Data,
    handleAddressSearch,
    handleFileChange,
    handleStep1Next,
    handleSubmit,
  };
};

