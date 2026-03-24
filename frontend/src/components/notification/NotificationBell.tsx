import { useState, useRef, useEffect } from "react";
import { useNotificationStore } from "store/notificationStore";
import { Badge } from "components/ui/badge";
import { NotificationDropdown } from "components/notification/NotificationDropdown";
import { cn } from "utils/cn";
import type { NotificationType } from "types/NotificationTypes";

interface NotificationBellProps {
  className?: string;
  typesFilter?: NotificationType[];
  viewAllPath?: string;
}

export function NotificationBell({
  className,
  typesFilter,
  viewAllPath,
}: NotificationBellProps) {
  const [isOpen, setIsOpen] = useState(false);
  const [shouldRing, setShouldRing] = useState(false);
  const dropdownRef = useRef<HTMLDivElement>(null);
  const buttonRef = useRef<HTMLButtonElement>(null);

  const { unreadCount } = useNotificationStore();
  const prevUnreadCountRef = useRef(unreadCount);

  // 새 알림이 도착하면 벨 애니메이션 실행
  useEffect(() => {
    if (unreadCount > prevUnreadCountRef.current) {
      setShouldRing(true);
      setTimeout(() => setShouldRing(false), 1000);
    }
    prevUnreadCountRef.current = unreadCount;
  }, [unreadCount]);

  // 외부 클릭 시 드롭다운 닫기
  useEffect(() => {
    function handleClickOutside(event: MouseEvent) {
      if (
        dropdownRef.current &&
        buttonRef.current &&
        !dropdownRef.current.contains(event.target as Node) &&
        !buttonRef.current.contains(event.target as Node)
      ) {
        setIsOpen(false);
      }
    }

    if (isOpen) {
      document.addEventListener("mousedown", handleClickOutside);
      return () => {
        document.removeEventListener("mousedown", handleClickOutside);
      };
    }
  }, [isOpen]);

  // ESC 키로 드롭다운 닫기
  useEffect(() => {
    function handleEscape(event: KeyboardEvent) {
      if (event.key === "Escape") {
        setIsOpen(false);
      }
    }

    if (isOpen) {
      document.addEventListener("keydown", handleEscape);
      return () => {
        document.removeEventListener("keydown", handleEscape);
      };
    }
  }, [isOpen]);

  const toggleDropdown = () => {
    setIsOpen((prev) => !prev);
  };

  return (
    <div className={cn("relative", className)}>
      <button
        ref={buttonRef}
        onClick={toggleDropdown}
        className={cn(
          "relative p-2 text-gray-700 hover:text-indigo-600 focus:outline-none focus:ring-2 focus:ring-indigo-500 focus:ring-offset-2 rounded-full transition-all duration-200",
          shouldRing && "animate-bell-ring"
        )}
        aria-label="알림"
        aria-expanded={isOpen}
      >
        {/* 벨 아이콘 */}
        <svg
          className={cn(
            "w-6 h-6 transition-transform",
            unreadCount > 0 && "text-indigo-600"
          )}
          fill="none"
          viewBox="0 0 24 24"
          stroke="currentColor"
        >
          <path
            strokeLinecap="round"
            strokeLinejoin="round"
            strokeWidth={2}
            d="M15 17h5l-1.405-1.405A2.032 2.032 0 0118 14.158V11a6.002 6.002 0 00-4-5.659V5a2 2 0 10-4 0v.341C7.67 6.165 6 8.388 6 11v3.159c0 .538-.214 1.055-.595 1.436L4 17h5m6 0v1a3 3 0 11-6 0v-1m6 0H9"
          />
        </svg>

        {/* 미읽음 배지 */}
        {unreadCount > 0 && (
          <Badge
            variant="destructive"
            className={cn(
              "absolute -top-1 -right-1 min-w-[20px] h-5 flex items-center justify-center px-1 text-xs font-bold",
              "animate-badge-pulse shadow-lg"
            )}
          >
            {unreadCount > 99 ? "99+" : unreadCount}
          </Badge>
        )}
      </button>

      {/* 드롭다운 */}
      {isOpen && (
        <div
          ref={dropdownRef}
          className="absolute right-0 mt-2 z-50 animate-fade-in-down"
        >
          <NotificationDropdown
            onClose={() => setIsOpen(false)}
            typesFilter={typesFilter}
            viewAllPath={viewAllPath}
          />
        </div>
      )}
    </div>
  );
}
