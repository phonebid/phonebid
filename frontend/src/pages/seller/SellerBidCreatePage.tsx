import { useEffect, useState } from "react";
import { useNavigate, useParams } from "react-router-dom";
import { useBidForm } from "hooks/useBidForm";
import { QuoteRequestInfo } from "components/seller/QuoteRequestInfo";
import { getQuoteDetail } from "services/quoteService";
import { sellerService } from "services/sellerService";
import { Card, CardContent, CardHeader, CardTitle } from "components/ui/card";
import { Button } from "components/ui/button";
import { formatNumber } from "utils/formatters";
import { logError } from "utils/errorUtils";
import { toast } from "react-toastify";
import type { QuoteDetail } from "types/QuoteTypes";

const SellerBidCreatePage: React.FC = () => {
  const navigate = useNavigate();
  const { quoteId } = useParams<{ quoteId: string }>();
  const [quote, setQuote] = useState<QuoteDetail | null>(null);
  const [isLoading, setIsLoading] = useState(true);
  const [isSubmitting, setIsSubmitting] = useState(false);

  const bidForm = useBidForm(quote);

  useEffect(() => {
    if (!quoteId) {
      navigate("/seller-center");
      return;
    }

    const loadQuote = async () => {
      try {
        setIsLoading(true);
        const quoteData = await getQuoteDetail(quoteId);
        setQuote(quoteData);
      } catch (error) {
        logError("견적 조회 실패:", error);
        toast.error("견적 정보를 불러오는데 실패했습니다.");
        navigate("/seller-center");
      } finally {
        setIsLoading(false);
      }
    };

    loadQuote();
  }, [quoteId, navigate]);

  const handleBack = () => {
    navigate("/seller-center");
  };

  const handleSubmit = async () => {
    if (!bidForm.validate()) {
      toast.error("입력한 정보를 확인해주세요.");
      return;
    }

    const bidRequest = bidForm.toBidCreateRequest();
    if (!bidRequest) {
      toast.error("견적 정보를 불러오지 못했습니다.");
      return;
    }

    try {
      setIsSubmitting(true);
      await sellerService.createBid(bidRequest);
      toast.success("견적이 성공적으로 전송되었습니다.");
      navigate("/seller-center");
    } catch (error) {
      logError("견적 전송 실패:", error);
    } finally {
      setIsSubmitting(false);
    }
  };

  if (isLoading || !quote) {
    return (
      <div className="min-h-screen bg-background flex items-center justify-center">
        <div className="text-muted-foreground">로딩 중...</div>
      </div>
    );
  }

  return (
    <div className="min-h-screen bg-background">
      <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-8">
        <div className="mb-6">
          <button
            onClick={handleBack}
            className="flex items-center gap-2 text-foreground hover:text-primary transition-colors mb-4 group"
          >
            <svg
              className="w-5 h-5 transition-transform group-hover:-translate-x-1"
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
            <span className="text-sm font-medium">요청 목록으로 돌아가기</span>
          </button>
        </div>

        <div className="grid grid-cols-1 lg:grid-cols-3 gap-6">
          <div className="lg:col-span-1">
            <QuoteRequestInfo quote={quote} />
          </div>

          <div className="lg:col-span-2">
            <Card>
              <CardHeader>
                <CardTitle>견적 작성</CardTitle>
              </CardHeader>
              <CardContent className="space-y-6">
                <div className="space-y-4">
                  <div className="flex items-center gap-2">
                    <label className="text-sm font-medium text-foreground">
                      기기 가격 출고가
                    </label>
                    <div className="text-lg font-semibold">
                      {formatNumber(bidForm.formData.devicePrice)}원
                    </div>
                  </div>

                  <div className="grid grid-cols-2 gap-4">
                    <div>
                      <label className="text-sm font-medium text-foreground">
                        공시지원금
                      </label>
                      <div className="relative mt-1">
                        <input
                          type="text"
                          value={
                            bidForm.formData.publicSubsidy > 0
                              ? formatNumber(bidForm.formData.publicSubsidy)
                              : ""
                          }
                          onChange={(e) => {
                            const numericValue = e.target.value.replace(/[^0-9]/g, "");
                            bidForm.updateField(
                              "publicSubsidy",
                              parseInt(numericValue) || 0
                            );
                          }}
                          className="w-full border border-input rounded-md h-10 px-3 pr-8 bg-background text-sm text-blue-600 font-medium text-right focus:outline-none focus:ring-2 focus:ring-ring"
                          placeholder="0"
                        />
                        <span className="absolute right-3 top-1/2 -translate-y-1/2 text-sm text-blue-600 font-medium pointer-events-none">
                          원
                        </span>
                      </div>
                      {bidForm.errors.publicSubsidy && (
                        <p className="mt-1 text-sm text-red-500">
                          {bidForm.errors.publicSubsidy}
                        </p>
                      )}
                    </div>

                    <div>
                      <label className="text-sm font-medium text-foreground">
                        추가지원금
                      </label>
                      <div className="relative mt-1">
                        <input
                          type="text"
                          value={
                            bidForm.formData.additionalSubsidy > 0
                              ? formatNumber(bidForm.formData.additionalSubsidy)
                              : ""
                          }
                          onChange={(e) => {
                            const numericValue = e.target.value.replace(/[^0-9]/g, "");
                            bidForm.updateField(
                              "additionalSubsidy",
                              parseInt(numericValue) || 0
                            );
                          }}
                          className="w-full border border-input rounded-md h-10 px-3 pr-8 bg-background text-sm text-blue-600 font-medium text-right focus:outline-none focus:ring-2 focus:ring-ring"
                          placeholder="0"
                        />
                        <span className="absolute right-3 top-1/2 -translate-y-1/2 text-sm text-blue-600 font-medium pointer-events-none">
                          원
                        </span>
                      </div>
                      {bidForm.errors.additionalSubsidy && (
                        <p className="mt-1 text-sm text-red-500">
                          {bidForm.errors.additionalSubsidy}
                        </p>
                      )}
                    </div>
                  </div>

                  <div className="bg-gray-50 rounded-md p-4">
                    <div className="flex items-center justify-between">
                      <label className="text-sm font-medium text-foreground">
                        할부원금
                      </label>
                      <div className="text-lg font-bold text-foreground">
                        {formatNumber(bidForm.calculations.installmentPrincipal)}원
                      </div>
                    </div>
                  </div>
                </div>

                <div className="pt-4 border-t">
                  <h3 className="text-base font-semibold text-foreground mb-4">
                    개통 및 할부 조건
                  </h3>
                  <div className="grid grid-cols-2 gap-4">
                    <div>
                      <label className="text-sm font-medium text-foreground">
                        개통 조건
                      </label>
                      <select
                        value={bidForm.formData.purchaseMethod}
                        onChange={(e) =>
                          bidForm.updateField(
                            "purchaseMethod",
                            e.target.value as any
                          )
                        }
                        className="mt-1 w-full border border-input rounded-md h-10 px-3 bg-background text-sm focus:outline-none focus:ring-2 focus:ring-ring"
                      >
                        <option value="DEVICE_CHANGE">기기변경</option>
                        <option value="NUMBER_TRANSFER">번호이동</option>
                        <option value="NEW_SUBSCRIPTION">신규가입</option>
                      </select>
                    </div>

                    <div>
                      <label className="text-sm font-medium text-foreground">
                        할부 조건
                      </label>
                      <select
                        value={bidForm.formData.installmentMonths}
                        onChange={(e) =>
                          bidForm.updateField(
                            "installmentMonths",
                            parseInt(e.target.value) || 24
                          )
                        }
                        className="mt-1 w-full border border-input rounded-md h-10 px-3 bg-background text-sm focus:outline-none focus:ring-2 focus:ring-ring"
                      >
                        <option value={24}>24개월</option>
                        <option value={36}>36개월</option>
                      </select>
                    </div>
                  </div>
                </div>

                <Card className="bg-primary-50 border-primary-200">
                  <CardContent className="py-2 px-4">
                    <div className="flex items-center gap-2 mb-2">
                      <svg
                        className="w-5 h-5 text-primary-600"
                        fill="none"
                        stroke="currentColor"
                        viewBox="0 0 24 24"
                      >
                        <path
                          strokeLinecap="round"
                          strokeLinejoin="round"
                          strokeWidth={2}
                          d="M9 7h6m0 10v-3m-3 3h.01M9 17h.01M9 14h.01M12 14h.01M15 11h.01M12 11h.01M9 11h.01M7 21h10a2 2 0 002-2V5a2 2 0 00-2-2H7a2 2 0 00-2 2v14a2 2 0 002 2z"
                        />
                      </svg>
                      <div className="text-sm font-medium text-foreground">
                        월 할부금
                      </div>
                    </div>
                    <div className="text-2xl font-bold text-primary-600">
                      {formatNumber(bidForm.calculations.monthlyInstallment)}원
                    </div>
                    <div className="text-xs text-muted-foreground mt-1">
                      3.3% 할부이자 포함
                    </div>
                  </CardContent>
                </Card>

                <div className="space-y-4 pt-4 border-t">
                  <div>
                    <label className="text-sm font-medium text-foreground">
                      요금제 선택
                    </label>
                    <input
                      type="text"
                      value={bidForm.formData.pricePlanName}
                      onChange={(e) =>
                        bidForm.updateField("pricePlanName", e.target.value)
                      }
                      placeholder="예: 5G 프리미어 에센셜"
                      className="mt-1 w-full border border-input rounded-md h-10 px-3 bg-background text-sm focus:outline-none focus:ring-2 focus:ring-ring"
                    />
                    {bidForm.errors.pricePlanName && (
                      <p className="mt-1 text-sm text-red-500">
                        {bidForm.errors.pricePlanName}
                      </p>
                    )}
                  </div>

                  <div>
                    <label className="text-sm font-medium text-foreground">
                      요금제 가격
                    </label>
                    <input
                      type="text"
                      value={
                        bidForm.formData.pricePlanPrice > 0
                          ? formatNumber(bidForm.formData.pricePlanPrice)
                          : ""
                      }
                      onChange={(e) => {
                        const numericValue = e.target.value.replace(/[^0-9]/g, "");
                        bidForm.updateField(
                          "pricePlanPrice",
                          parseInt(numericValue) || 0
                        );
                      }}
                      placeholder="0"
                      className="mt-1 w-full border border-input rounded-md h-10 px-3 bg-background text-sm focus:outline-none focus:ring-2 focus:ring-ring"
                    />
                    {bidForm.errors.pricePlanPrice && (
                      <p className="mt-1 text-sm text-red-500">
                        {bidForm.errors.pricePlanPrice}
                      </p>
                    )}
                  </div>

                  <div className="grid grid-cols-2 gap-4">
                    <div>
                      <label className="text-sm font-medium text-foreground">
                        요금제 유지기간
                      </label>
                      <select
                        value={bidForm.formData.pricePlanMaintenanceMonths}
                        onChange={(e) =>
                          bidForm.updateField(
                            "pricePlanMaintenanceMonths",
                            parseInt(e.target.value) || 24
                          )
                        }
                        className="mt-1 w-full border border-input rounded-md h-10 px-3 bg-background text-sm focus:outline-none focus:ring-2 focus:ring-ring"
                      >
                        <option value={24}>24개월 이상</option>
                        <option value={36}>36개월 이상</option>
                      </select>
                    </div>

                    <div>
                      <label className="text-sm font-medium text-foreground">
                        회선 유지기간
                      </label>
                      <select
                        value={bidForm.formData.lineMaintenanceMonths}
                        onChange={(e) =>
                          bidForm.updateField(
                            "lineMaintenanceMonths",
                            parseInt(e.target.value) || 24
                          )
                        }
                        className="mt-1 w-full border border-input rounded-md h-10 px-3 bg-background text-sm focus:outline-none focus:ring-2 focus:ring-ring"
                      >
                        <option value={24}>24개월 이상</option>
                        <option value={36}>36개월 이상</option>
                      </select>
                    </div>
                  </div>
                </div>

                <div className="space-y-4 pt-4 border-t">
                  <div>
                    <label className="text-sm font-bold text-foreground mb-4 block">
                      부가서비스
                    </label>
                    <div className="space-y-2">
                      <label className="flex items-center gap-2 border border-gray-200 rounded-md p-3 cursor-pointer hover:bg-gray-50 transition-colors">
                        <input
                          type="checkbox"
                          className="w-4 h-4"
                          checked={bidForm.formData.additionalServices.some(
                            (s) => s.serviceName === "T멤버십 VIP"
                          )}
                          onChange={(e) => {
                            const services = [...bidForm.formData.additionalServices];
                            if (e.target.checked) {
                              services.push({
                                serviceName: "T멤버십 VIP",
                                servicePrice: 5000,
                              });
                            } else {
                              const index = services.findIndex(
                                (s) => s.serviceName === "T멤버십 VIP"
                              );
                              if (index > -1) services.splice(index, 1);
                            }
                            bidForm.updateField("additionalServices", services);
                          }}
                        />
                        <span className="text-sm">
                          T멤버십 VIP (월 5,000원)
                        </span>
                      </label>
                      <label className="flex items-center gap-2 border border-gray-200 rounded-md p-3 cursor-pointer hover:bg-gray-50 transition-colors">
                        <input
                          type="checkbox"
                          className="w-4 h-4"
                          checked={bidForm.formData.additionalServices.some(
                            (s) => s.serviceName === "디바이스 케어"
                          )}
                          onChange={(e) => {
                            const services = [...bidForm.formData.additionalServices];
                            if (e.target.checked) {
                              services.push({
                                serviceName: "디바이스 케어",
                                servicePrice: 11000,
                              });
                            } else {
                              const index = services.findIndex(
                                (s) => s.serviceName === "디바이스 케어"
                              );
                              if (index > -1) services.splice(index, 1);
                            }
                            bidForm.updateField("additionalServices", services);
                          }}
                        />
                        <span className="text-sm">
                          디바이스 케어 (월 11,000원)
                        </span>
                      </label>
                      <label className="flex items-center gap-2 border border-gray-200 rounded-md p-3 cursor-pointer hover:bg-gray-50 transition-colors">
                        <input
                          type="checkbox"
                          className="w-4 h-4"
                          checked={bidForm.formData.additionalServices.some(
                            (s) => s.serviceName === "T우주 Pass"
                          )}
                          onChange={(e) => {
                            const services = [...bidForm.formData.additionalServices];
                            if (e.target.checked) {
                              services.push({
                                serviceName: "T우주 Pass",
                                servicePrice: 13000,
                              });
                            } else {
                              const index = services.findIndex(
                                (s) => s.serviceName === "T우주 Pass"
                              );
                              if (index > -1) services.splice(index, 1);
                            }
                            bidForm.updateField("additionalServices", services);
                          }}
                        />
                        <span className="text-sm">
                          T우주 Pass (월 13,000원)
                        </span>
                      </label>
                    </div>
                    <div className="mt-4">
                      <label className="text-xs text-muted-foreground">
                        부가서비스 유지기간
                      </label>
                      <select
                        value={bidForm.formData.additionalServicesMaintenanceMonths}
                        onChange={(e) =>
                          bidForm.updateField(
                            "additionalServicesMaintenanceMonths",
                            parseInt(e.target.value) || 0
                          )
                        }
                        className="mt-1 w-full border border-input rounded-md h-10 px-3 bg-background text-xs focus:outline-none focus:ring-2 focus:ring-ring"
                      >
                        <option value={0}>선택 안함</option>
                        <option value={24}>24개월 이상</option>
                        <option value={36}>36개월 이상</option>
                      </select>
                    </div>
                  </div>
                  <div className="border-t my-4"></div>
                </div>

                <div>
                  <Card className="border-pink-200 shadow-sm overflow-hidden" style={{ background: 'linear-gradient(to bottom right, #eff6ff, #e0f2fe, #fce7f3)' }}>
                    <CardContent className="py-3 px-4">
                      <div className="flex items-center justify-between mb-2">
                        <div className="text-sm font-medium text-foreground">
                          월 예상 납부액
                        </div>
                        <div className="text-xs text-muted-foreground">
                          (할부금 + 요금제)
                        </div>
                      </div>
                      <div className="text-right">
                        <div className="text-3xl font-bold text-foreground">
                          {formatNumber(bidForm.calculations.totalMonthlyPayment)}
                          <span className="text-xl font-normal ml-1">원</span>
                        </div>
                      </div>
                    </CardContent>
                  </Card>
                </div>

                <div className="pt-2">
                  <Button
                    onClick={handleSubmit}
                    disabled={isSubmitting}
                    className="w-full text-white shadow-lg transition-all font-bold"
                    style={{ background: 'linear-gradient(to right, #60a5fa, #3b82f6, #2563eb)' }}
                    size="lg"
                    onMouseEnter={(e) => {
                      if (!isSubmitting) {
                        e.currentTarget.style.background = 'linear-gradient(to right, #3b82f6, #2563eb, #1d4ed8)';
                      }
                    }}
                    onMouseLeave={(e) => {
                      if (!isSubmitting) {
                        e.currentTarget.style.background = 'linear-gradient(to right, #60a5fa, #3b82f6, #2563eb)';
                      }
                    }}
                  >
                    <svg
                      className="w-5 h-5 mr-2"
                      fill="none"
                      stroke="currentColor"
                      viewBox="0 0 24 24"
                    >
                      <path
                        strokeLinecap="round"
                        strokeLinejoin="round"
                        strokeWidth={2}
                        d="M6 12L3.269 3.126A59.768 59.768 0 0121.485 12 59.77 59.77 0 013.27 20.876L5.999 12zm0 0h7.5"
                      />
                    </svg>
                    견적 보내기
                  </Button>
                </div>
              </CardContent>
            </Card>
          </div>
        </div>
      </div>
    </div>
  );
};

export default SellerBidCreatePage;

