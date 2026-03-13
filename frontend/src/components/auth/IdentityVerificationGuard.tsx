import { useState, useEffect } from "react";
import { useAuthStore } from "store/authStore";
import { IdentityVerificationModal } from "./IdentityVerificationModal";

export const IdentityVerificationGuard: React.FC<{
  children: React.ReactNode;
}> = ({ children }) => {
  const { isAuthenticated, isInitializing, user } = useAuthStore();
  const [showModal, setShowModal] = useState(false);

  useEffect(() => {
    if (
      !isInitializing &&
      isAuthenticated &&
      user?.role === "CONSUMER" &&
      user?.isIdentityVerified === false
    ) {
      setShowModal(true);
    } else {
      setShowModal(false);
    }
  }, [isAuthenticated, isInitializing, user]);

  return (
    <>
      {children}
      <IdentityVerificationModal
        isOpen={showModal}
        onClose={() => setShowModal(false)}
      />
    </>
  );
};
