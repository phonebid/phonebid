const EmptyDeliveryAddressCard = () => {
  return (
    <div className="border-2 border-dashed border-gray-300 rounded-lg p-8 text-center">
      <svg
        className="w-12 h-12 text-gray-400 mx-auto mb-3"
        fill="none"
        stroke="currentColor"
        viewBox="0 0 24 24"
      >
        <path
          strokeLinecap="round"
          strokeLinejoin="round"
          strokeWidth={2}
          d="M3 12l2-2m0 0l7-7 7 7M5 10v10a1 1 0 001 1h3m10-11l2 2m-2-2v10a1 1 0 01-1 1h-3m-6 0a1 1 0 001-1v-4a1 1 0 011-1h2a1 1 0 011 1v4a1 1 0 001 1m-6 0h6"
        />
      </svg>
      <p className="text-sm font-medium text-gray-700 mb-1">기본 배송지가 없습니다</p>
      <p className="text-xs text-gray-500">신규 배송지를 입력해주세요</p>
    </div>
  );
};

export default EmptyDeliveryAddressCard;

