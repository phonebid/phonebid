/**
 * 에러 메시지를 안전하게 추출하는 헬퍼 함수
 * @param error - 알 수 없는 타입의 에러 객체
 * @returns 사용자에게 표시할 안전한 에러 메시지
 */
export function getErrorMessage(error: unknown): string {
  if (error instanceof Error) {
    return error.message || "알 수 없는 오류가 발생했습니다.";
  }
  
  if (typeof error === "string") {
    return error;
  }
  
  // Axios 에러 응답 처리
  if (
    error &&
    typeof error === "object" &&
    "response" in error &&
    error.response &&
    typeof error.response === "object" &&
    "data" in error.response &&
    error.response.data &&
    typeof error.response.data === "object" &&
    "message" in error.response.data &&
    typeof error.response.data.message === "string"
  ) {
    return error.response.data.message;
  }
  
  try {
    return String(error);
  } catch {
    return "알 수 없는 오류가 발생했습니다.";
  }
}

/**
 * 에러를 로깅하는 함수
 * 프로덕션 환경에서는 Sentry 등으로 교체 가능
 * @param message - 로그 메시지
 * @param error - 에러 객체
 */
export function logError(message: string, error: unknown): void {
  // 개발 환경에서는 console.error 사용
  if (process.env.NODE_ENV === "development") {
    console.error(message, error);
  }
  
  // 프로덕션 환경에서는 여기에 Sentry.captureException 등을 추가할 수 있습니다
  // 예: if (window.Sentry) { window.Sentry.captureException(error); }
}

