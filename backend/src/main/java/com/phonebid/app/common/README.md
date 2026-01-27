# Common Package

이 디렉토리는 프로젝트 전역에서 재사용되는 공통 모듈, 예외 처리, DTO, 설정, 베이스 도메인 클래스를 포함합니다.

## 디렉터리 구조 및 설명

- **errorcode/**
  - 애플리케이션 전역에서 사용되는 에러 코드 정의
  - 명확한 책임 분리를 위해 별도 패키지로 분리
  - `ErrorCode.java`: 에러 코드 인터페이스 (모든 ErrorCode enum이 구현)
  - `CommonErrorCode.java`: 공통 에러 코드 (회원가입, 로그인, 서버 에러 등)
  - `NaverErrorCode.java`: 네이버 OAuth 관련 에러 코드
  - `KakaoErrorCode.java`: 카카오 OAuth 관련 에러 코드

- **exception/**
  - 예외 처리 및 에러 응답 관련 클래스
  - `CustomException.java`: 커스텀 예외의 베이스 클래스
  - `GlobalExceptionHandler.java`: 전역 예외 처리기
  - `ErrorResponse.java`: 표준 에러 응답 DTO (deprecated, ApiResponse 사용 권장)

- **dto/**
  - API 표준 응답 등 공통 DTO 클래스
  - `ApiResponse.java`: API 표준 응답 구조 제공 (성공/실패 envelope)

- **domain/**
  - 공통 도메인 및 베이스 엔티티 클래스
  - `BaseEntity.java`: JPA 감사(auditing) 필드 포함 베이스 엔티티 (일반 엔터티용)
    - `createdAt`, `updatedAt`, `createdBy`, `updatedBy`, `deletedAt`, `deletedBy`, `isDelete` 포함
  - `BaseTimeEntity.java`: 최소한의 시간 정보만 포함하는 베이스 엔티티 (Immutable 엔터티용)
    - `createdAt`, `deletedAt`만 포함
    - 수정되지 않는 엔터티(예: RefreshToken)에 사용

- **config/**
  - 공통 설정 및 JPA 감사 관련 설정 클래스
  - `JpaConfig.java`: JPA 감사 활성화 설정
  - `AuditAwareImpl.java`: 현재 사용자 정보 제공 (auditing용)


## 활용 예시

### 에러 코드 사용
```java
// 공통 에러 코드
throw new CustomException(CommonErrorCode.DUPLICATE_USERNAME);

// OAuth 관련 에러 코드
throw new CustomException(NaverErrorCode.NAVER_TOKEN_REQUEST_FAILED);
throw new CustomException(KakaoErrorCode.KAKAO_USER_INFO_REQUEST_FAILED);
```

### API 응답 표준화
```java
// 성공 응답
return ResponseEntity.ok(
    ApiResponse.success(HttpStatus.OK, "로그인 성공", responseDto)
);

// 에러 응답
return ResponseEntity.status(HttpStatus.BAD_REQUEST)
    .body(ApiResponse.error(HttpStatus.BAD_REQUEST, "잘못된 요청", null));
```

### 베이스 엔티티 활용
```java
@Entity
public class User extends BaseEntity {
    // 생성일시, 수정일시 자동 관리
}
```


> 이 디렉토리 내 코드는 프로젝트 전역에서 재사용할 수 있습니다.
> 유지보수성과 일관성을 높이기 위해 별도 관리됩니다.