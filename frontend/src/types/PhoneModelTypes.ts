export type Brand = "APPLE" | "SAMSUNG";

export type PhoneOptionType = "COLOR" | "STORAGE";

export interface PhoneOptionResponse {
  id: string;
  modelId: string;
  optionType: PhoneOptionType;
  optionValue: string;
  displayLabel: string;
  createdAt: string;
  updatedAt: string;
}

export interface PhoneModelResponse {
  id: string;
  brand: Brand;
  model: string;
  modelNumber?: string | null;
  releasedPrice?: number | null;
  releasedAt?: string | null;
  options?: PhoneOptionResponse[];
  createdAt: string;
  updatedAt: string;
}

export interface PhoneModelOptionRequest {
  type: PhoneOptionType;
  value: string;
  displayLabel?: string;
}

export interface PhoneModelCreateRequest {
  brand: Brand; 
  model: string;
  modelNumber?: string;
  releasedPrice?: number;
  releasedAt?: string;
  options?: PhoneModelOptionRequest[];
}

export interface PhoneModelUpdateRequest {
  brand: Brand;
  model: string;
  modelNumber?: string;
  releasedPrice?: number;
  releasedAt?: string;
  options?: PhoneModelOptionRequest[];
}

export interface PhoneModelImageResponse {
  id: string;
  imageUrl: string;
  displayOrder: number;
}

export interface PhoneModelImageUploadResponse {
  images: PhoneModelImageResponse[];
}