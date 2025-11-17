interface UnreadBadgeProps {
  count: number;
  className?: string;
}

/**
 * 읽지 않은 메시지 수를 표시하는 배지 컴포넌트
 * 99개까지는 숫자로 표시, 99개 이상이면 "99+"로 표시
 */
export function UnreadBadge({ count, className = "" }: UnreadBadgeProps) {
  if (count === 0) {
    return null;
  }

  const displayCount = count > 99 ? "99+" : String(count);

  return (
    <span
      className={`bg-red-500 text-white rounded-md flex items-center justify-center min-w-[16px] h-[16px] px-1 text-[9px] font-medium leading-none ${className}`}
      aria-label={`읽지 않은 메시지 ${count}개`}
    >
      {displayCount}
    </span>
  );
}

