import { useEffect, useState } from "react";
import { useNavigate } from "react-router-dom";
import { useAuthStore } from "store/authStore";
import Input from "components/common/Input";
import Button from "components/common/Button";
import Checkbox from "components/common/Checkbox";

const SignupPage = () => {
  const navigate = useNavigate();
  const { isAuthenticated } = useAuthStore();

  // 회원가입 폼 상태
  const [formData, setFormData] = useState({
    name: "",
    username: "",
    email: "",
    password: "",
    confirmPassword: "",
    nickname: "",
  });

  const [errors, setErrors] = useState({
    name: "",
    username: "",
    email: "",
    password: "",
    confirmPassword: "",
    nickname: "",
    agreements: "",
  });

  // 약관 동의 상태
  const [agreements, setAgreements] = useState({
    allAgree: false,
    termsOfService: false,
    privacyPolicy: false,
    marketingConsent: false,
  });

  // 이미 로그인된 사용자는 홈으로 리다이렉트
  useEffect(() => {
    if (isAuthenticated) {
      navigate("/", { replace: true });
    }
  }, [isAuthenticated, navigate]);

  // 검증 함수들
  const validateEmail = (email: string): string => {
    const emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
    if (!email.trim()) return "이메일을 입력해주세요.";
    if (!emailRegex.test(email)) return "올바른 이메일 형식이 아닙니다.";
    return "";
  };

  const validateUsername = (username: string): string => {
    const usernameRegex = /^[a-z0-9]+$/;
    if (!username.trim()) return "아이디를 입력해주세요.";
    if (username.length < 4 || username.length > 10) {
      return "아이디는 4자 이상이어야 합니다.";
    }
    if (!usernameRegex.test(username)) {
      return "아이디는 알파벳 소문자와 숫자로만 구성되어야 합니다.";
    }
    return "";
  };

  const validatePassword = (password: string): string => {
    if (!password.trim()) return "비밀번호를 입력해주세요.";
    if (password.length < 8 || password.length > 20) {
      return "비밀번호는 8자 이상 20자 이하여야 합니다.";
    }
    return "";
  };

  const validateNickname = (nickname: string): string => {
    const nicknameRegex = /^[가-힣a-zA-Z0-9_-]+$/;
    if (!nickname.trim()) return "닉네임을 입력해주세요.";
    if (nickname.length < 2 || nickname.length > 10) {
      return "닉네임은 2자 이상 10자 이하여야 합니다.";
    }
    if (!nicknameRegex.test(nickname)) {
      return "닉네임은 한글, 영문, 숫자, _, -만 사용 가능합니다.";
    }
    return "";
  };

  const validateName = (name: string): string => {
    if (!name.trim()) return "이름을 입력해주세요.";
    return "";
  };

  const validateConfirmPassword = (
    password: string,
    confirmPassword: string
  ): string => {
    if (!confirmPassword.trim()) return "비밀번호 확인을 입력해주세요.";
    if (password !== confirmPassword) return "비밀번호가 일치하지 않습니다.";
    return "";
  };

  const validateAgreements = (agreementsData: {
    allAgree: boolean;
    termsOfService: boolean;
    privacyPolicy: boolean;
    marketingConsent: boolean;
  }): string => {
    if (!agreementsData.termsOfService || !agreementsData.privacyPolicy) {
      return "필수 약관에 동의해주세요.";
    }
    return "";
  };

  // 실시간 검증을 위한 입력 핸들러
  const handleInputChange = (field: keyof typeof formData, value: string) => {
    setFormData((prev) => ({ ...prev, [field]: value }));

    // 실시간 검증
    let error = "";
    switch (field) {
      case "name":
        error = validateName(value);
        break;
      case "username":
        error = validateUsername(value);
        break;
      case "email":
        error = validateEmail(value);
        break;
      case "password":
        error = validatePassword(value);
        // 비밀번호가 변경되면 비밀번호 확인도 다시 검증
        if (formData.confirmPassword) {
          setErrors((prev) => ({
            ...prev,
            confirmPassword: validateConfirmPassword(
              value,
              formData.confirmPassword
            ),
          }));
        }
        break;
      case "confirmPassword":
        error = validateConfirmPassword(formData.password, value);
        break;
      case "nickname":
        error = validateNickname(value);
        break;
    }

    setErrors((prev) => ({ ...prev, [field]: error }));
  };

  // 약관 동의 핸들러
  const handleAgreementChange = (
    field: keyof typeof agreements,
    checked: boolean
  ) => {
    if (field === "allAgree") {
      // 전체 동의 변경 시: 모든 하위 약관 동기화
      const newAgreements = {
        allAgree: checked,
        termsOfService: checked,
        privacyPolicy: checked,
        marketingConsent: checked,
      };
      setAgreements(newAgreements);

      // 실시간 약관 검증
      const agreementError = validateAgreements(newAgreements);
      setErrors((prev) => ({ ...prev, agreements: agreementError }));
    } else {
      // 개별 약관 변경 시
      const newAgreements = { ...agreements, [field]: checked };

      // 전체 동의 상태 자동 계산
      const allIndividualChecked =
        newAgreements.termsOfService &&
        newAgreements.privacyPolicy &&
        newAgreements.marketingConsent;
      newAgreements.allAgree = allIndividualChecked;

      setAgreements(newAgreements);

      // 실시간 약관 검증
      const agreementError = validateAgreements(newAgreements);
      setErrors((prev) => ({ ...prev, agreements: agreementError }));
    }
  };

  // 회원가입 처리
  const handleSignup = () => {
    // 에러 상태 초기화
    const newErrors = {
      name: "",
      username: "",
      email: "",
      password: "",
      confirmPassword: "",
      nickname: "",
      agreements: "",
    };

    // 순차적 검증 - 첫 번째 에러만 표시
    const nameError = validateName(formData.name);
    if (nameError) {
      newErrors.name = nameError;
      setErrors(newErrors);
      return;
    }

    const usernameError = validateUsername(formData.username);
    if (usernameError) {
      newErrors.username = usernameError;
      setErrors(newErrors);
      return;
    }

    const emailError = validateEmail(formData.email);
    if (emailError) {
      newErrors.email = emailError;
      setErrors(newErrors);
      return;
    }

    const passwordError = validatePassword(formData.password);
    if (passwordError) {
      newErrors.password = passwordError;
      setErrors(newErrors);
      return;
    }

    const confirmPasswordError = validateConfirmPassword(
      formData.password,
      formData.confirmPassword
    );
    if (confirmPasswordError) {
      newErrors.confirmPassword = confirmPasswordError;
      setErrors(newErrors);
      return;
    }

    const nicknameError = validateNickname(formData.nickname);
    if (nicknameError) {
      newErrors.nickname = nicknameError;
      setErrors(newErrors);
      return;
    }

    const agreementError = validateAgreements(agreements);
    if (agreementError) {
      newErrors.agreements = agreementError;
      setErrors(newErrors);
      return;
    }

    // 모든 검증 통과 시 회원가입 처리
    setErrors(newErrors); // 모든 에러 초기화
    console.log("회원가입 시도:", {
      username: formData.username,
      password: formData.password,
      email: formData.email,
      name: formData.name,
      nickname: formData.nickname,
      agreements,
    });
    // TODO: API 연결 후 실제 회원가입 처리
  };

  return (
    // 전체화면 배경
    <div className="min-h-screen w-full flex flex-col">
      <div className="flex-1 flex items-center justify-center px-4">
        <div className="max-w-md w-full">
          {/* 로고 및 헤더 */}
          <div className="text-center mb-12">
            <p className="text-lg text-gray-600">가장 저렴한 핸드폰 구매는</p>
            <h1 className="text-4xl font-bold text-gray-900 mb-3">PhoneBid</h1>
            <p className="text-sm text-gray-500">
              회원가입하고 최저가 혜택을 받아보세요
            </p>
          </div>

          {/* 회원가입 폼 */}
          <div className="space-y-4 mb-8">
            <Input
              label="이름"
              type="text"
              placeholder="이름을 입력하세요"
              value={formData.name}
              onChange={(value) => handleInputChange("name", value)}
              error={errors.name}
              required
            />

            <Input
              label="아이디"
              type="text"
              placeholder="아이디를 입력하세요 (4-10자, 영문소문자+숫자)"
              value={formData.username}
              onChange={(value) => handleInputChange("username", value)}
              error={errors.username}
              required
            />

            <Input
              label="이메일"
              type="email"
              placeholder="이메일을 입력하세요"
              value={formData.email}
              onChange={(value) => handleInputChange("email", value)}
              error={errors.email}
              required
            />

            <Input
              label="비밀번호"
              type="password"
              placeholder="비밀번호를 입력하세요 (8-20자)"
              value={formData.password}
              onChange={(value) => handleInputChange("password", value)}
              error={errors.password}
              required
            />

            <Input
              label="비밀번호 확인"
              type="password"
              placeholder="비밀번호를 다시 입력하세요"
              value={formData.confirmPassword}
              onChange={(value) => handleInputChange("confirmPassword", value)}
              error={errors.confirmPassword}
              required
            />

            <Input
              label="닉네임"
              type="text"
              placeholder="닉네임을 입력하세요 (2-10자)"
              value={formData.nickname}
              onChange={(value) => handleInputChange("nickname", value)}
              error={errors.nickname}
              required
            />

            {/* 약관 동의 */}
            <div className="space-y-3">
              {/* 약관 전체 동의 */}
              <div className="border-b border-gray-200">
                <Checkbox
                  label="약관 전체 동의"
                  checked={agreements.allAgree}
                  onChange={(checked) =>
                    handleAgreementChange("allAgree", checked)
                  }
                />
              </div>

              {/* 개별 약관들 */}
              <div className="ml-4">
                <Checkbox
                  label="이용약관 동의"
                  checked={agreements.termsOfService}
                  onChange={(checked) =>
                    handleAgreementChange("termsOfService", checked)
                  }
                  required
                />

                <Checkbox
                  label="개인정보 수집 및 이용동의"
                  checked={agreements.privacyPolicy}
                  onChange={(checked) =>
                    handleAgreementChange("privacyPolicy", checked)
                  }
                  required
                />

                <Checkbox
                  label="광고성 정보 수신동의 (선택)"
                  checked={agreements.marketingConsent}
                  onChange={(checked) =>
                    handleAgreementChange("marketingConsent", checked)
                  }
                />
              </div>

              {errors.agreements && (
                <p className="text-sm text-red-600">{errors.agreements}</p>
              )}
            </div>

            <Button
              onClick={handleSignup}
              className="bg-indigo-100 text-indigo-700 w-full px-6 py-3 rounded-lg font-medium text-sm border border-indigo-200 hover:bg-indigo-200 hover:border-indigo-300 transition-all duration-200"
            >
              회원가입
            </Button>
          </div>
        </div>
      </div>
    </div>
  );
};

export default SignupPage;
