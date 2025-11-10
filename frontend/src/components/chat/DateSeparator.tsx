interface DateSeparatorProps {
  date: string;
}

/**
 * 날짜 구분선 컴포넌트
 */
export function DateSeparator({ date }: DateSeparatorProps) {
  const formatDate = (dateString: string): string => {
    const date = new Date(dateString);
    const today = new Date();
    const yesterday = new Date(today);
    yesterday.setDate(yesterday.getDate() - 1);

    if (date.toDateString() === today.toDateString()) {
      return "오늘";
    } else if (date.toDateString() === yesterday.toDateString()) {
      return "어제";
    } else {
      return date.toLocaleDateString("ko-KR", {
        year: "numeric",
        month: "long",
        day: "numeric",
        weekday: "long",
      });
    }
  };

  return (
    <div className="text-center my-4">
      <span className="bg-gray-200 text-gray-600 text-xs px-3 py-1 rounded-full">
        {formatDate(date)}
      </span>
    </div>
  );
}

