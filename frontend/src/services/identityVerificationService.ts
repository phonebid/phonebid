import { apiClient } from "services/apiClient";

interface IdentityVerificationInitInfo {
  storeId: string;
  channelKey: string;
}

interface IdentityVerificationResult {
  verified: boolean;
  name: string;
  phone: string;
  carrier: string | null;
}

export const getVerificationInitInfo =
  async (): Promise<IdentityVerificationInitInfo> => {
    return apiClient.get<IdentityVerificationInitInfo>(
      "/identity-verification/init"
    );
  };

export const confirmVerification = async (
  identityVerificationId: string
): Promise<IdentityVerificationResult> => {
  return apiClient.post<IdentityVerificationResult>(
    "/identity-verification/confirm",
    { identityVerificationId }
  );
};
