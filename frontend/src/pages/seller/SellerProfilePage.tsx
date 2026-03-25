import { useEffect, useState } from "react";
import { SellerHeader } from "components/seller/SellerHeader";
import { sellerService } from "services/sellerService";
import type { SellerProfileResponseDto } from "types/SellerTypes";

export default function SellerProfilePage() {
  const [profile, setProfile] = useState<SellerProfileResponseDto | null>(null);
  const [isLoading, setIsLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);

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

  return (
    <div className="min-h-screen bg-background flex flex-col">
      <SellerHeader />
      <div className="flex-1 bg-gray-100">
        <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-8">
          <div className="mb-8">
            <h1 className="text-3xl font-bold tracking-tight text-foreground">
              판매자 프로필
            </h1>
            <p className="mt-2 text-sm text-muted-foreground">
              판매자 계정 및 매장 정보를 확인하세요.
            </p>
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
