# Common Package

이 디렉토리는 프로젝트 전역에서 재사용되는 공통 모듈, 예외 처리, DTO, 설정, 베이스 도메인 클래스를 포함합니다.

## 디렉터리 구조 및 설명

- **exception/**
  - 글로벌 예외 처리 및 에러 응답 관련 클래스
    - `CommonErrorCode.java`: 공통 에러 코드 정의
    - `CustomException.java`: 커스텀 예외의 베이스 클래스
    - `ErrorCode.java`: 에러 코드 인터페이스
    - `ErrorResponse.java`: 표준 에러 응답 DTO

- **dto/**
  - API 표준 응답 등 공통 DTO 클래스
    - `ApiResponse.java`: API 표준 응답 구조 제공

- **domain/**
  - 공통 도메인 및 베이스 엔티티 클래스
    - `BaseEntity.java`: JPA 감사(auditing) 필드 포함 베이스 엔티티

- **config/**
  - 공통 설정 및 JPA 감사 관련 설정 클래스
    - `JpaConfig.java`: JPA 감사 활성화 설정
    - `AuditAwareImpl.java`: 현재 사용자 정보 제공 (auditing용)

## 활용 예시
- 모든 도메인 엔티티는 `BaseEntity`를 상속하여 생성/수정자 자동 관리
- 컨트롤러에서 `ApiResponse`와 `ErrorResponse`를 사용해 일관된 응답 제공
- 비즈니스 예외 발생 시 `CustomException`을 활용해 표준화된 에러 처리

---

> 이 디렉토리 내 코드는 프로젝트 전역에서 재사용할 수 있습니다.
> 유지보수성과 일관성을 높이기 위해 별도 관리됩니다.