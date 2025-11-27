import { formatChatDate } from "utils/formatters";

interface DateSeparatorProps {
  date: string;
}

/**
 * 날짜 구분선 컴포넌트
 */
export function DateSeparator({ date }: DateSeparatorProps) {
  return (
    <div className="text-center my-4">
      <span className="bg-gray-100 text-gray-600 text-xs px-3 py-1.5 rounded-lg">
        {formatChatDate(date)}
      </span>
    </div>
  );
}

