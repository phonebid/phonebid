import { useEffect, useMemo, useState } from "react";
import { useNotificationStore } from "store/notificationStore";
import { cn } from "utils/cn";

interface NotificationStatusBannerProps {
  className?: string;
}

export function NotificationStatusBanner({ className }: NotificationStatusBannerProps) {
  const { connectionStatus } = useNotificationStore();
  const [isOnline, setIsOnline] = useState(() => navigator.onLine);

  useEffect(() => {
    const handleOnline = () => setIsOnline(true);
    const handleOffline = () => setIsOnline(false);

    window.addEventListener("online", handleOnline);
    window.addEventListener("offline", handleOffline);

    return () => {
      window.removeEventListener("online", handleOnline);
      window.removeEventListener("offline", handleOffline);
    };
  }, []);

  const banner = useMemo(() => {
    if (!isOnline) {
      return {
        variant: "warning" as const,
        title: "오프라인 상태입니다",
        description: "네트워크가 연결되면 실시간 알림이 자동으로 다시 연결됩니다.",
      };
    }

    if (connectionStatus === "connecting") {
      return {
        variant: "info" as const,
        title: "실시간 알림 연결 중",
        description: "잠시만 기다려 주세요.",
      };
    }

    if (connectionStatus === "error" || connectionStatus === "disconnected") {
      return {
        variant: "warning" as const,
        title: "실시간 알림 연결이 불안정합니다",
        description: "알림이 즉시 반영되지 않을 수 있습니다. 잠시 후 자동으로 재연결을 시도합니다.",
      };
    }

    return null;
  }, [connectionStatus, isOnline]);

  if (!banner) return null;

  const styles =
    banner.variant === "info"
      ? "bg-blue-50 border-blue-200 text-blue-900"
      : "bg-yellow-50 border-yellow-200 text-yellow-900";

  return (
    <div
      className={cn(
        "w-full rounded-lg border px-4 py-3 text-sm",
        styles,
        className
      )}
      role="status"
      aria-live="polite"
    >
      <div className="font-semibold">{banner.title}</div>
      <div className="mt-1 text-xs opacity-90">{banner.description}</div>
    </div>
  );
}

