interface UnreadBadgeProps {
  count: number;
  className?: string;
}

/**
 * 읽지 않은 메시지 수를 표시하는 배지 컴포넌트
 */
export function UnreadBadge({ count, className = "" }: UnreadBadgeProps) {
  if (count === 0) {
    return null;
  }

  const displayCount = count > 99 ? "99+" : String(count);

  return (
    <div
      className={`bg-red-500 text-white rounded-full flex items-center justify-center min-w-[20px] h-5 px-1.5 text-xs font-medium ${className}`}
      aria-label={`읽지 않은 메시지 ${count}개`}
    >
      {displayCount}
    </div>
  );
}

