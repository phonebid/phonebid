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

/**
 * 채팅용 날짜 포맷팅 (오늘/어제 또는 전체 날짜)
 */
export const formatChatDate = (dateString: string): string => {
  const date = new Date(dateString);
  const today = new Date();
  const yesterday = new Date(today);
  yesterday.setDate(yesterday.getDate() - 1);

  if (date.toDateString() === today.toDateString()) {
    return "오늘";
  } else if (date.toDateString() === yesterday.toDateString()) {
    return "어제";
  } else {
    return date.toLocaleDateString("ko-KR", {
      year: "numeric",
      month: "long",
      day: "numeric",
      weekday: "long",
    });
  }
};

/**
 * 날짜를 yyyy.MM.dd 형식으로 포맷팅
 */
export const formatDateSimple = (date: string | Date): string => {
  const dateObj = typeof date === "string" ? new Date(date) : date;
  const year = dateObj.getFullYear();
  const month = String(dateObj.getMonth() + 1).padStart(2, "0");
  const day = String(dateObj.getDate()).padStart(2, "0");
  return `${year}.${month}.${day}`;
};

/**
 * 날짜를 yyyy년MM월dd일 형식으로 포맷팅
 */
export const formatDateKorean = (date: string | Date): string => {
  const dateObj = typeof date === "string" ? new Date(date) : date;
  const year = dateObj.getFullYear();
  const month = String(dateObj.getMonth() + 1).padStart(2, "0");
  const day = String(dateObj.getDate()).padStart(2, "0");
  return `${year}년${month}월${day}일`;
};

/**
 * 가격을 한국 통화 형식으로 포맷팅 (숫자 + "원")
 */
export const formatPrice = (price: number): string => {
  return new Intl.NumberFormat("ko-KR").format(price) + "원";
};

/**
 * 사업자등록번호 포맷팅 (예: "123-45-67890")
 */
export const formatBusinessNumber = (value: string): string => {
  const numbers = value.replace(/-/g, "").slice(0, 10);
  if (numbers.length <= 3) return numbers;
  if (numbers.length <= 5)
    return `${numbers.slice(0, 3)}-${numbers.slice(3)}`;
  return `${numbers.slice(0, 3)}-${numbers.slice(3, 5)}-${numbers.slice(5)}`;
};

/**
 * 전화번호 포맷팅 (예: "010-1234-5678")
 */
export const formatPhoneNumber = (value: string): string => {
  const numbers = value.replace(/\D/g, "");
  
  if (numbers.length === 0) return "";
  if (numbers.length <= 2) return numbers;
  
  if (numbers.startsWith("02")) {
    if (numbers.length <= 5) return `${numbers.slice(0, 2)}-${numbers.slice(2)}`;
    if (numbers.length <= 9) return `${numbers.slice(0, 2)}-${numbers.slice(2, 5)}-${numbers.slice(5)}`;
    return `${numbers.slice(0, 2)}-${numbers.slice(2, 6)}-${numbers.slice(6, 10)}`;
  }
  
  if (numbers.length === 8) {
    return `${numbers.slice(0, 4)}-${numbers.slice(4)}`;
  }
  
  if (numbers.length >= 3) {
    const prefix = numbers.slice(0, 3);
    if (numbers.length <= 6) return `${prefix}-${numbers.slice(3)}`;
    if (numbers.length <= 10) return `${prefix}-${numbers.slice(3, 6)}-${numbers.slice(6)}`;
    return `${prefix}-${numbers.slice(3, 7)}-${numbers.slice(7, 11)}`;
  }
  
  if (numbers.length <= 7) return `${numbers.slice(0, 3)}-${numbers.slice(3)}`;
  return `${numbers.slice(0, 3)}-${numbers.slice(3, 7)}-${numbers.slice(7, 11)}`;
};