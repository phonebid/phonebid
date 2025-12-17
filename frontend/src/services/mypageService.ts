import { apiClient } from "./apiClient";
import type {
  ProfileResponseDto,
  ProfileUpdateRequestDto,
} from "types/MyPageTypes";

export const mypageService = {
  getProfile: async (): Promise<ProfileResponseDto> => {
    return await apiClient.get<ProfileResponseDto>("/mypage/profile");
  },

  updateProfile: async (
    data: ProfileUpdateRequestDto
  ): Promise<void> => {
    return await apiClient.put<void>("/mypage/profile", data);
  },
};

