/**
 * JWT 토큰 유틸리티 함수
 */

/**
 * JWT 토큰에서 payload를 디코딩하여 username을 추출합니다.
 * 백엔드 JWT는 sub 필드에 username을 저장합니다.
 * @param token JWT 토큰 (Bearer 접두사 제거된 순수 토큰)
 * @returns username 또는 null
 */
export function getUsernameFromToken(token: string | null): string | null {
  if (!token) {
    return null;
  }

  try {
    // JWT는 header.payload.signature 형식
    const parts = token.split(".");
    if (parts.length !== 3 || !parts[1]) {
      return null;
    }

    // payload 부분 디코딩 (base64url 디코딩)
    const payload = parts[1];
    // base64url을 base64로 변환
    const base64 = payload.replace(/-/g, "+").replace(/_/g, "/");
    // padding 추가
    const padded = base64 + "=".repeat((4 - (base64.length % 4)) % 4);
    const decoded = JSON.parse(atob(padded));

    // 백엔드 JWT는 sub 필드에 username을 저장
    return decoded.sub || null;
  } catch (error) {
    console.error("JWT 디코딩 실패:", error);
    return null;
  }
}

/**
 * JWT 토큰에서 사용자 ID(UUID)를 추출합니다.
 * 현재 백엔드 JWT에는 사용자 ID가 포함되어 있지 않으므로,
 * username을 반환합니다 (임시 해결책).
 * @param token JWT 토큰 (Bearer 접두사 제거된 순수 토큰)
 * @returns 사용자 ID (UUID string) 또는 username 또는 null
 */
export function getUserIdFromToken(token: string | null): string | null {
  // 현재는 username을 반환 (백엔드에서 senderId가 username인지 UUID인지 확인 필요)
  return getUsernameFromToken(token);
}

