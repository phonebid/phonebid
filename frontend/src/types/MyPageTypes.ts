export interface ProfileResponseDto {
  username: string;
  nickname: string;
  phone: string | null;
  name: string;
}

export interface ProfileUpdateRequestDto {
  name?: string | null;
  nickname?: string | null;
  phone?: string | null;
}

