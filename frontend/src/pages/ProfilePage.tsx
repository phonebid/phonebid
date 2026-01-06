import { useEffect, useState, useRef } from "react";
import { useNavigate } from "react-router-dom";
import { mypageService } from "services/mypageService";
import { toast } from "react-toastify";
import type { ProfileUpdateRequestDto } from "types/MyPageTypes";
import { logError } from "utils/errorUtils";
import { Trash2 } from "lucide-react";

const ProfilePage = () => {
  const navigate = useNavigate();
  const [isLoading, setIsLoading] = useState(false);
  const [isLoadingProfile, setIsLoadingProfile] = useState(true);
  const [isUploadingImage, setIsUploadingImage] = useState(false);
  const [profileImageUrl, setProfileImageUrl] = useState<string | null>(null);
  const fileInputRef = useRef<HTMLInputElement>(null);

  const [formData, setFormData] = useState({
    username: "",
    name: "",
    nickname: "",
    phone: "",
  });

  const [errors, setErrors] = useState({
    name: "",
    nickname: "",
    phone: "",
  });

  const [verificationCode, setVerificationCode] = useState("");
  const [isVerificationSent, setIsVerificationSent] = useState(false);

  useEffect(() => {
    loadProfile();
  }, []);

  const loadProfile = async () => {
    try {
      setIsLoadingProfile(true);
      const data = await mypageService.getProfile();
      setFormData({
        username: data.username || "",
        name: data.name || "",
        nickname: data.nickname || "",
        phone: data.phone || "",
      });
      setProfileImageUrl(data.profileImageUrl || null);
    } catch (error: unknown) {
      logError("프로필 조회 실패:", error);
    } finally {
      setIsLoadingProfile(false);
    }
  };

  const validateName = (name: string): string => {
    if (!name.trim()) return "이름을 입력해주세요.";
    if (name.trim().length > 50) return "이름은 50자 이하여야 합니다.";
    return "";
  };

  const validateNickname = (nickname: string): string => {
    const nicknameRegex = /^[가-힣a-zA-Z0-9_-]+$/;
    const trimmedNickname = nickname.trim();
    if (!trimmedNickname) return "닉네임을 입력해주세요.";
    if (trimmedNickname.length < 2 || trimmedNickname.length > 10) {
      return "닉네임은 2자 이상 10자 이하여야 합니다.";
    }
    if (!nicknameRegex.test(trimmedNickname)) {
      return "닉네임은 한글, 영문, 숫자, _, -만 사용 가능합니다.";
    }
    return "";
  };

  const validatePhone = (phone: string): string => {
    const phoneRegex = /^[0-9]+$/;
    if (phone && phone.trim()) {
      if (phone.length < 10 || phone.length > 11) {
        return "휴대폰 번호는 10자리 또는 11자리여야 합니다.";
      }
      if (!phoneRegex.test(phone)) {
        return "휴대폰 번호는 숫자만 입력 가능합니다.";
      }
    }
    return "";
  };

  const handleInputChange = (field: keyof typeof formData, value: string) => {
    setFormData((prev) => ({ ...prev, [field]: value }));

    let error = "";
    switch (field) {
      case "name":
        error = validateName(value);
        break;
      case "nickname":
        error = validateNickname(value);
        break;
      case "phone":
        error = validatePhone(value);
        break;
    }

    setErrors((prev) => ({ ...prev, [field]: error }));
  };

  const handleSendVerification = () => {
    const phoneError = validatePhone(formData.phone);
    if (phoneError) {
      setErrors((prev) => ({ ...prev, phone: phoneError }));
      toast.error("올바른 휴대폰 번호를 입력해주세요.");
      return;
    }

    toast.info("문자인증 기능은 준비 중입니다.");
    setIsVerificationSent(true);
  };

  const handleVerifyCode = () => {
    toast.info("인증확인 기능은 준비 중입니다.");
  };

  const handleUpdate = async () => {
    const nameError = validateName(formData.name);
    const nicknameError = validateNickname(formData.nickname);
    const phoneError = validatePhone(formData.phone);

    if (nameError || nicknameError || phoneError) {
      setErrors({
        name: nameError,
        nickname: nicknameError,
        phone: phoneError,
      });
      return;
    }

    setIsLoading(true);
    try {
      const updateData: ProfileUpdateRequestDto = {
        name: formData.name.trim() || null,
        nickname: formData.nickname.trim() || null,
        phone: formData.phone.trim() || null,
      };

      await mypageService.updateProfile(updateData);
      toast.success("프로필이 수정되었습니다.");
      navigate("/mypage");
    } catch (error: unknown) {
      logError("프로필 수정 실패:", error);
    } finally {
      setIsLoading(false);
    }
  };

  const handleBack = () => {
    navigate("/mypage");
  };

  const handleImageUploadClick = () => {
    fileInputRef.current?.click();
  };

  const handleImageFileChange = async (e: React.ChangeEvent<HTMLInputElement>) => {
    const file = e.target.files?.[0];
    if (!file) return;

    // 파일 유효성 검사
    const allowedTypes = ["image/jpeg", "image/jpg", "image/png", "image/gif", "image/webp"];
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
      const response = await mypageService.uploadProfileImage(file);
      setProfileImageUrl(response.profileImageUrl);
      toast.success("프로필 이미지가 업로드되었습니다.");
      // 프로필 정보 갱신
      await loadProfile();
    } catch (error: unknown) {
      logError("프로필 이미지 업로드 실패:", error);
    } finally {
      setIsUploadingImage(false);
      // 파일 input 초기화
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
      // 프로필 정보 갱신
      await loadProfile();
    } catch (error: unknown) {
      logError("프로필 이미지 삭제 실패:", error);
    } finally {
      setIsUploadingImage(false);
    }
  };

  if (isLoadingProfile) {
    return (
      <div className="min-h-screen bg-gradient-to-br from-purple-200 via-purple-100 to-blue-200 flex items-center justify-center">
        <div className="text-gray-600">로딩 중...</div>
      </div>
    );
  }

  return (
    <div className="min-h-screen bg-gradient-to-br from-purple-200 via-purple-100 to-blue-200">
      <div className="max-w-md mx-auto px-4 py-4">
        {/* 헤더 */}
        <div className="flex items-center mb-10">
          <button
            onClick={handleBack}
            className="text-indigo-600 hover:text-indigo-700 mr-4"
          >
            <svg
              className="w-5 h-5"
              fill="none"
              stroke="currentColor"
              viewBox="0 0 24 24"
            >
              <path
                strokeLinecap="round"
                strokeLinejoin="round"
                strokeWidth={2}
                d="M15 19l-7-7 7-7"
              />
            </svg>
          </button>
          <h1 className="text-xl font-bold text-gray-900 flex-1 text-center">
            프로필
          </h1>
          <div className="w-9"></div>
        </div>

        {/* 프로필 사진 영역 */}
        <div className="flex justify-center mb-10">
          <div className="relative group">
            <div className="w-32 h-32 bg-white rounded-full flex items-center justify-center shadow-lg overflow-hidden">
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
              className="absolute bottom-0 right-0 w-7 h-7 bg-indigo-500 rounded-full flex items-center justify-center shadow-lg hover:bg-indigo-600 transition-colors disabled:opacity-50 disabled:cursor-not-allowed"
              onClick={handleImageUploadClick}
              disabled={isUploadingImage}
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
                className="absolute top-0 right-0 w-7 h-7 bg-gray-700/80 backdrop-blur-sm rounded-full flex items-center justify-center opacity-0 group-hover:opacity-100 transition-opacity hover:bg-gray-800/90 disabled:opacity-50 disabled:cursor-not-allowed"
                onClick={handleImageDelete}
                disabled={isUploadingImage}
                aria-label="프로필 이미지 삭제"
              >
                <Trash2 className="w-3.5 h-3.5 text-white" />
              </button>
            )}
          </div>
        </div>

        {/* 입력 필드들 */}
        <div className="space-y-2">
          {/* 아이디 */}
          <div className="bg-white rounded-lg p-2.5 shadow-sm border border-purple-100">
            <div className="flex items-center gap-2">
              <div className="w-8 h-8 bg-indigo-100 rounded-full flex items-center justify-center flex-shrink-0">
                <svg
                  className="w-4 h-4 text-indigo-500"
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
              </div>
              <div className="flex-1">
                <label className="block text-xs font-medium text-gray-700 mb-0.5">
                  아이디
                </label>
                <input
                  type="text"
                  value={formData.username}
                  disabled
                  className="w-full bg-gray-50 text-gray-500 border-0 focus:outline-none cursor-not-allowed text-sm"
                />
              </div>
            </div>
          </div>

          {/* 이름 */}
          <div className="bg-white rounded-lg p-2.5 shadow-sm border border-purple-100">
            <div className="flex items-center gap-2">
              <div className="w-8 h-8 bg-indigo-100 rounded-full flex items-center justify-center flex-shrink-0">
                <svg
                  className="w-4 h-4 text-indigo-500"
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
              </div>
              <div className="flex-1">
                <label className="block text-xs font-medium text-gray-700 mb-0.5">
                  이름
                </label>
                <input
                  type="text"
                  placeholder="이름을 입력해주세요"
                  value={formData.name}
                  onChange={(e) => handleInputChange("name", e.target.value)}
                  className={`w-full border-0 focus:outline-none focus:ring-0 p-0 text-sm ${
                    errors.name ? "text-red-600" : ""
                  }`}
                />
                {errors.name && (
                  <p className="mt-0.5 text-xs text-red-600">{errors.name}</p>
                )}
              </div>
            </div>
          </div>

          {/* 닉네임 */}
          <div className="bg-white rounded-lg p-2.5 shadow-sm border border-purple-100">
            <div className="flex items-center gap-2">
              <div className="w-8 h-8 bg-indigo-100 rounded-full flex items-center justify-center flex-shrink-0">
                <svg
                  className="w-4 h-4 text-indigo-500"
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
              </div>
              <div className="flex-1">
                <label className="block text-xs font-medium text-gray-700 mb-0.5">
                  닉네임
                </label>
                <input
                  type="text"
                  placeholder="닉네임을 입력해주세요"
                  value={formData.nickname}
                  onChange={(e) => handleInputChange("nickname", e.target.value)}
                  className={`w-full border-0 focus:outline-none focus:ring-0 p-0 text-sm ${
                    errors.nickname ? "text-red-600" : ""
                  }`}
                />
                {errors.nickname && (
                  <p className="mt-0.5 text-xs text-red-600">{errors.nickname}</p>
                )}
              </div>
            </div>
          </div>

          {/* 휴대폰 번호 */}
          <div className="bg-white rounded-lg p-2.5 shadow-sm border border-purple-100">
            <div className="flex items-center gap-2">
              <div className="w-8 h-8 bg-indigo-100 rounded-full flex items-center justify-center flex-shrink-0">
                <svg
                  className="w-4 h-4 text-indigo-500"
                  fill="none"
                  stroke="currentColor"
                  viewBox="0 0 24 24"
                >
                  <path
                    strokeLinecap="round"
                    strokeLinejoin="round"
                    strokeWidth={2}
                    d="M3 5a2 2 0 012-2h3.28a1 1 0 01.948.684l1.498 4.493a1 1 0 01-.502 1.21l-2.257 1.13a11.042 11.042 0 005.516 5.516l1.13-2.257a1 1 0 011.21-.502l4.493 1.498a1 1 0 01.684.949V19a2 2 0 01-2 2h-1C9.716 21 3 14.284 3 6V5z"
                  />
                </svg>
              </div>
              <div className="flex-1">
                <label className="block text-xs font-medium text-gray-700 mb-0.5">
                  휴대폰 번호
                </label>
                <div className="flex gap-1.5">
                  <input
                    type="tel"
                    placeholder="00000000000"
                    value={formData.phone}
                    onChange={(e) => handleInputChange("phone", e.target.value)}
                    className={`flex-1 border-0 focus:outline-none focus:ring-0 p-0 text-sm ${
                      errors.phone ? "text-red-600" : ""
                    }`}
                  />
                  <button
                    onClick={handleSendVerification}
                    className="px-2 py-1 bg-indigo-500 text-white rounded-md text-xs font-medium hover:bg-indigo-600 transition-colors whitespace-nowrap"
                  >
                    문자인증
                  </button>
                </div>
                {errors.phone && (
                  <p className="mt-0.5 text-xs text-red-600">{errors.phone}</p>
                )}
              </div>
            </div>
          </div>

          {/* 인증 번호 */}
          <div className="bg-white rounded-lg p-2.5 shadow-sm border border-purple-100">
            <div className="flex items-center gap-2">
              <div className="w-8 h-8 bg-indigo-100 rounded-full flex items-center justify-center flex-shrink-0">
                <svg
                  className="w-4 h-4 text-indigo-500"
                  fill="none"
                  stroke="currentColor"
                  viewBox="0 0 24 24"
                >
                  <path
                    strokeLinecap="round"
                    strokeLinejoin="round"
                    strokeWidth={2}
                    d="M12 15v2m-6 4h12a2 2 0 002-2v-6a2 2 0 00-2-2H6a2 2 0 00-2 2v6a2 2 0 002 2zm10-10V7a4 4 0 00-8 0v4h8z"
                  />
                </svg>
              </div>
              <div className="flex-1">
                <label className="block text-xs font-medium text-gray-700 mb-0.5">
                  인증 번호
                </label>
                <div className="flex gap-1.5">
                  <input
                    type="text"
                    placeholder="000000"
                    value={verificationCode}
                    onChange={(e) => setVerificationCode(e.target.value)}
                    disabled={!isVerificationSent}
                    className={`flex-1 border-0 focus:outline-none focus:ring-0 p-0 text-sm ${
                      !isVerificationSent
                        ? "bg-gray-50 text-gray-400 cursor-not-allowed"
                        : ""
                    }`}
                  />
                  <button
                    onClick={handleVerifyCode}
                    disabled={!isVerificationSent}
                    className="px-2 py-1 bg-indigo-500 text-white rounded-md text-xs font-medium hover:bg-indigo-600 transition-colors disabled:opacity-50 disabled:cursor-not-allowed whitespace-nowrap"
                  >
                    인증확인
                  </button>
                </div>
              </div>
            </div>
          </div>
        </div>

        {/* 버튼들 */}
        <div className="flex gap-2 mt-4">
          <button
            onClick={handleBack}
            className="flex-1 px-3 py-2 bg-white text-gray-700 rounded-lg text-sm font-medium hover:bg-gray-50 transition-colors border border-gray-200"
          >
            뒤로가기
          </button>
          <button
            onClick={handleUpdate}
            disabled={isLoading}
            className="flex-1 px-3 py-2 bg-indigo-500 text-white rounded-lg text-sm font-medium hover:bg-indigo-600 transition-colors disabled:opacity-50 disabled:cursor-not-allowed"
          >
            {isLoading ? "수정 중..." : "수정"}
          </button>
        </div>
      </div>
    </div>
  );
};

export default ProfilePage;
