import { toast } from "react-toastify";
import { apiClient } from "services/apiClient";
import type {
  PhoneModelCreateRequest,
  PhoneModelResponse,
  PhoneModelUpdateRequest,
  PhoneModelImageResponse,
  PhoneModelImageUploadResponse,
} from "types/PhoneModelTypes";

const BASE_URL = "/phone/models";

export const getPhoneModels = async (): Promise<PhoneModelResponse[]> => {
  try {
    return await apiClient.get<PhoneModelResponse[]>(BASE_URL);
  } catch (err) {
    console.error(err);
    toast.error("휴대폰 모델 목록을 불러오는데 실패했습니다.");
    throw err;
  }
};

export const createPhoneModel = async (
  payload: PhoneModelCreateRequest
): Promise<PhoneModelResponse> => {
  try {
    return await apiClient.post<PhoneModelResponse>(BASE_URL, payload);
  } catch (err) {
    console.error(err);
    toast.error("휴대폰 모델 생성에 실패했습니다.");
    throw err;
  }
};

export const updatePhoneModel = async (
  id: string,
  payload: PhoneModelUpdateRequest
): Promise<PhoneModelResponse> => {
  try {
    return await apiClient.put<PhoneModelResponse>(
      `${BASE_URL}/${id}`,
      payload
    );
  } catch (err) {
    console.error(err);
    toast.error("휴대폰 모델 수정에 실패했습니다.");
    throw err;
  }
};

export const deletePhoneModel = async (id: string): Promise<void> => {
  try {
    return await apiClient.delete<void>(`${BASE_URL}/${id}`);
  } catch (err) {
    console.error(err);
    toast.error("휴대폰 모델 삭제에 실패했습니다.");
    throw err;
  }
};

export const uploadPhoneModelImages = async (
  phoneModelId: string,
  files: File[]
): Promise<PhoneModelImageUploadResponse> => {
  try {
    const formData = new FormData();
    files.forEach((file) => {
      formData.append("files", file);
    });

    return await apiClient.post<PhoneModelImageUploadResponse>(
      `${BASE_URL}/${phoneModelId}/images`,
      formData
    );
  } catch (err) {
    console.error(err);
    toast.error("핸드폰 모델 이미지 업로드에 실패했습니다.");
    throw err;
  }
};

export const getPhoneModelImages = async (
  phoneModelId: string
): Promise<PhoneModelImageResponse[]> => {
  try {
    return await apiClient.get<PhoneModelImageResponse[]>(
      `${BASE_URL}/${phoneModelId}/images`
    );
  } catch (err) {
    console.error(err);
    toast.error("핸드폰 모델 이미지 조회에 실패했습니다.");
    throw err;
  }
};

export const deletePhoneModelImage = async (
  phoneModelId: string,
  imageId: string
): Promise<void> => {
  try {
    return await apiClient.delete<void>(
      `${BASE_URL}/${phoneModelId}/images/${imageId}`
    );
  } catch (err) {
    console.error(err);
    toast.error("핸드폰 모델 이미지 삭제에 실패했습니다.");
    throw err;
  }
};
