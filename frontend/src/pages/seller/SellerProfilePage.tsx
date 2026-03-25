import { useEffect, useRef, useState } from "react";
import { SellerHeader } from "components/seller/SellerHeader";
import { sellerService } from "services/sellerService";
import type { SellerProfileResponseDto } from "types/SellerTypes";
import { mypageService } from "services/mypageService";
import { toast } from "react-toastify";
import { Trash2 } from "lucide-react";

export default function SellerProfilePage() {
  const [profile, setProfile] = useState<SellerProfileResponseDto | null>(null);
  const [isLoading, setIsLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const [isLoadingUserProfile, setIsLoadingUserProfile] = useState(true);
  const [isUploadingImage, setIsUploadingImage] = useState(false);
  const [profileImageUrl, setProfileImageUrl] = useState<string | null>(null);
  const fileInputRef = useRef<HTMLInputElement>(null);

  const loadUserProfile = async () => {
    try {
      setIsLoadingUserProfile(true);
      const response = await mypageService.getProfile();
      setProfileImageUrl(response.profileImageUrl || null);
    } catch (err) {
      console.error("유저 프로필 조회 실패:", err);
    } finally {
      setIsLoadingUserProfile(false);
    }
  };

  useEffect(() => {
    async function loadProfile() {
      try {
        setIsLoading(true);
        setError(null);
        const response = await sellerService.getSellerProfile();
        setProfile(response);
      } catch (err) {
        console.error("판매자 프로필 조회 실패:", err);
        setError("판매자 프로필 정보를 불러오지 못했습니다.");
      } finally {
        setIsLoading(false);
      }
    }

    void loadProfile();
  }, []);

  useEffect(() => {
    void loadUserProfile();
  }, []);

  const handleImageUploadClick = () => {
    fileInputRef.current?.click();
  };

  const handleImageFileChange = async (
    e: React.ChangeEvent<HTMLInputElement>
  ) => {
    const file = e.target.files?.[0];
    if (!file) return;

    const allowedTypes = [
      "image/jpeg",
      "image/jpg",
      "image/png",
      "image/gif",
      "image/webp",
    ];
    if (!allowedTypes.includes(file.type)) {
      toast.error("지원하지 않는 파일 형식입니다. (jpg, jpeg, png, gif, webp만 가능)");
      return;
    }

    const maxSize = 5 * 1024 * 1024; // 5MB
    if (file.size > maxSize) {
      toast.error("파일 크기는 5MB 이하여야 합니다.");
      return;
    }

    try {
      setIsUploadingImage(true);
      await mypageService.uploadProfileImage(file);
      toast.success("프로필 이미지가 업로드되었습니다.");
      await loadUserProfile();
      window.dispatchEvent(new Event("profile-image-updated"));
    } catch (err) {
      console.error("프로필 이미지 업로드 실패:", err);
    } finally {
      setIsUploadingImage(false);
      if (fileInputRef.current) {
        fileInputRef.current.value = "";
      }
    }
  };

  const handleImageDelete = async () => {
    if (!profileImageUrl) return;

    if (!window.confirm("프로필 이미지를 삭제하시겠습니까?")) {
      return;
    }

    try {
      setIsUploadingImage(true);
      await mypageService.deleteProfileImage();
      setProfileImageUrl(null);
      toast.success("프로필 이미지가 삭제되었습니다.");
      await loadUserProfile();
      window.dispatchEvent(new Event("profile-image-updated"));
    } catch (err) {
      console.error("프로필 이미지 삭제 실패:", err);
    } finally {
      setIsUploadingImage(false);
    }
  };

  return (
    <div className="min-h-screen bg-background flex flex-col">
      <SellerHeader />
      <div className="flex-1 bg-gray-100">
        <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-8">
          {/* 프로필 사진 */}
          <div className="mb-6 bg-white border border-gray-200 rounded-lg shadow-sm py-8">
            <div className="flex justify-center">
              <div className="relative group">
                <div className="w-32 h-32 bg-white rounded-full flex items-center justify-center shadow-lg overflow-hidden border border-gray-200">
                  {profileImageUrl ? (
                    <img
                      src={profileImageUrl}
                      alt="프로필 이미지"
                      className="w-full h-full object-cover"
                    />
                  ) : (
                    <svg
                      className="w-20 h-20 text-gray-400"
                      fill="none"
                      stroke="currentColor"
                      viewBox="0 0 24 24"
                    >
                      <path
                        strokeLinecap="round"
                        strokeLinejoin="round"
                        strokeWidth={2}
                        d="M16 7a4 4 0 11-8 0 4 4 0 018 0zM12 14a7 7 0 00-7 7h14a7 7 0 00-7-7z"
                      />
                    </svg>
                  )}
                </div>

                <input
                  ref={fileInputRef}
                  type="file"
                  accept="image/jpeg,image/jpg,image/png,image/gif,image/webp"
                  className="hidden"
                  onChange={handleImageFileChange}
                  disabled={isUploadingImage}
                />

                <button
                  type="button"
                  className="absolute bottom-0 right-0 w-8 h-8 bg-indigo-500 rounded-full flex items-center justify-center shadow-lg hover:bg-indigo-600 transition-colors disabled:opacity-50 disabled:cursor-not-allowed"
                  onClick={handleImageUploadClick}
                  disabled={isUploadingImage || isLoadingUserProfile}
                  aria-label="프로필 이미지 업로드"
                >
                  {isUploadingImage ? (
                    <svg
                      className="w-4 h-4 text-white animate-spin"
                      fill="none"
                      viewBox="0 0 24 24"
                    >
                      <circle
                        className="opacity-25"
                        cx="12"
                        cy="12"
                        r="10"
                        stroke="currentColor"
                        strokeWidth="4"
                      />
                      <path
                        className="opacity-75"
                        fill="currentColor"
                        d="M4 12a8 8 0 018-8V0C5.373 0 0 5.373 0 12h4zm2 5.291A7.962 7.962 0 014 12H0c0 3.042 1.135 5.824 3 7.938l3-2.647z"
                      />
                    </svg>
                  ) : (
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
                        d="M3 9a2 2 0 012-2h.93a2 2 0 001.664-.89l.812-1.22A2 2 0 0110.07 4h3.86a2 2 0 011.664.89l.812 1.22A2 2 0 0018.07 7H19a2 2 0 012 2v9a2 2 0 01-2 2H5a2 2 0 01-2-2V9z"
                      />
                      <path
                        strokeLinecap="round"
                        strokeLinejoin="round"
                        strokeWidth={2}
                        d="M15 13a3 3 0 11-6 0 3 3 0 016 0z"
                      />
                    </svg>
                  )}
                </button>

                {profileImageUrl && (
                  <button
                    type="button"
                    className="absolute top-0 right-0 w-8 h-8 bg-gray-700/80 backdrop-blur-sm rounded-full flex items-center justify-center opacity-0 group-hover:opacity-100 transition-opacity hover:bg-gray-800/90 disabled:opacity-50 disabled:cursor-not-allowed"
                    onClick={handleImageDelete}
                    disabled={isUploadingImage || isLoadingUserProfile}
                    aria-label="프로필 이미지 삭제"
                  >
                    <Trash2 className="w-4 h-4 text-white" />
                  </button>
                )}
              </div>
            </div>
          </div>

          <div className="bg-white border border-gray-200 rounded-lg shadow-sm p-6">
            {isLoading ? (
              <div className="py-12 text-center text-gray-500">불러오는 중...</div>
            ) : error ? (
              <div className="py-12 text-center text-red-600">{error}</div>
            ) : profile ? (
              <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
                <div>
                  <div className="text-sm text-gray-500">매장명</div>
                  <div className="mt-1 text-base font-semibold text-gray-900">
                    {profile.storeName}
                  </div>
                </div>
                <div>
                  <div className="text-sm text-gray-500">대표자명</div>
                  <div className="mt-1 text-base font-semibold text-gray-900">
                    {profile.representativeName}
                  </div>
                </div>
                <div>
                  <div className="text-sm text-gray-500">아이디</div>
                  <div className="mt-1 text-base text-gray-900">
                    {profile.username}
                  </div>
                </div>
                <div>
                  <div className="text-sm text-gray-500">사업자번호</div>
                  <div className="mt-1 text-base text-gray-900">
                    {profile.businessNumber}
                  </div>
                </div>
                <div>
                  <div className="text-sm text-gray-500">연락처</div>
                  <div className="mt-1 text-base text-gray-900">
                    {profile.phoneNumber}
                  </div>
                </div>
                <div>
                  <div className="text-sm text-gray-500">이메일</div>
                  <div className="mt-1 text-base text-gray-900">{profile.email}</div>
                </div>
                <div className="md:col-span-2">
                  <div className="text-sm text-gray-500">주소</div>
                  <div className="mt-1 text-base text-gray-900">
                    {profile.fullAddress}
                  </div>
                </div>
                <div className="md:col-span-2">
                  <div className="text-sm text-gray-500">승인 상태</div>
                  <div className="mt-1">
                    <span className="inline-flex items-center rounded-full bg-blue-50 px-3 py-1 text-sm font-medium text-blue-700">
                      {profile.approvalStatusDisplayName}
                    </span>
                  </div>
                </div>
              </div>
            ) : (
              <div className="py-12 text-center text-gray-500">
                프로필 정보가 없습니다.
              </div>
            )}
          </div>
        </div>
      </div>
    </div>
  );
}
