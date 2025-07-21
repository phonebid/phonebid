/**
 * 숫자를 한국 통화 형식으로 포맷팅
 */
export const formatCurrency = (amount: number): string => {
  return new Intl.NumberFormat("ko-KR", {
    style: "currency",
    currency: "KRW",
  }).format(amount);
};

/**
 * 숫자를 콤마가 포함된 문자열로 포맷팅
 */
export const formatNumber = (num: number): string => {
  return new Intl.NumberFormat("ko-KR").format(num);
};

/**
 * 날짜를 한국 형식으로 포맷팅
 */
export const formatDate = (date: string | Date): string => {
  const dateObj = typeof date === "string" ? new Date(date) : date;
  return new Intl.DateTimeFormat("ko-KR", {
    year: "numeric",
    month: "long",
    day: "numeric",
    hour: "2-digit",
    minute: "2-digit",
  }).format(dateObj);
};

/**
 * 상대 시간 포맷팅 (예: "2시간 전")
 */
export const formatRelativeTime = (date: string | Date): string => {
  const dateObj = typeof date === "string" ? new Date(date) : date;
  const now = new Date();
  const diffInSeconds = Math.floor((now.getTime() - dateObj.getTime()) / 1000);

  if (diffInSeconds < 60) {
    return "방금 전";
  } else if (diffInSeconds < 3600) {
    const minutes = Math.floor(diffInSeconds / 60);
    return `${minutes}분 전`;
  } else if (diffInSeconds < 86400) {
    const hours = Math.floor(diffInSeconds / 3600);
    return `${hours}시간 전`;
  } else {
    const days = Math.floor(diffInSeconds / 86400);
    return `${days}일 전`;
  }
};

/**
 * 남은 시간 포맷팅 (예: "2시간 30분 남음")
 */
export const formatTimeRemaining = (endTime: string | Date): string => {
  const endDate = typeof endTime === "string" ? new Date(endTime) : endTime;
  const now = new Date();
  const diffInSeconds = Math.floor((endDate.getTime() - now.getTime()) / 1000);

  if (diffInSeconds <= 0) {
    return "경매 종료";
  }

  const days = Math.floor(diffInSeconds / 86400);
  const hours = Math.floor((diffInSeconds % 86400) / 3600);
  const minutes = Math.floor((diffInSeconds % 3600) / 60);

  if (days > 0) {
    return `${days}일 ${hours}시간 남음`;
  } else if (hours > 0) {
    return `${hours}시간 ${minutes}분 남음`;
  } else {
    return `${minutes}분 남음`;
  }
};

/**
 * 휴대폰 번호 마스킹 (예: "010-1234-5678" -> "010-****-5678")
 */
export const maskPhoneNumber = (phoneNumber: string): string => {
  return phoneNumber.replace(/(\d{3})-(\d{4})-(\d{4})/, "$1-****-$3");
};

/**
 * 이메일 마스킹 (예: "user@example.com" -> "u***@example.com")
 */
export const maskEmail = (email: string): string => {
  const [username, domain] = email.split("@");

  if (!username || !domain) {
    return email; // 유효하지 않은 이메일 형식인 경우 원본 반환
  }

  const maskedUsername = username.charAt(0) + "*".repeat(username.length - 1);
  return `${maskedUsername}@${domain}`;
};
