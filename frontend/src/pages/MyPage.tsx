import { useNavigate } from "react-router-dom";

const MyPage = () => {
  const navigate = useNavigate();

  const menuItems = [
    {
      id: "profile",
      label: "프로필",
      path: "/mypage/profile",
      enabled: true,
    },
    {
      id: "purchases",
      label: "구매내역",
      path: "/mypage/purchases",
      enabled: false,
    },
    {
      id: "accounts",
      label: "계좌관리",
      path: "/mypage/accounts",
      enabled: false,
    },
    {
      id: "addresses",
      label: "배송주소록",
      path: "/mypage/addresses",
      enabled: false,
    },
    {
      id: "customer-service",
      label: "고객센터",
      path: "/mypage/customer-service",
      enabled: false,
    },
    {
      id: "reviews",
      label: "리뷰관리",
      path: "/mypage/reviews",
      enabled: false,
    },
    {
      id: "seller",
      label: "판매자 바로가기",
      path: "/seller",
      enabled: false,
    },
    {
      id: "withdraw",
      label: "회원탈퇴",
      path: "/mypage/withdraw",
      enabled: false,
    },
  ];

  const handleMenuClick = (item: typeof menuItems[0]) => {
    if (item.enabled) {
      navigate(item.path);
    }
  };

  return (
    <div className="min-h-screen bg-gray-50">
      <div className="max-w-md mx-auto px-4 py-8">
        <h1 className="text-2xl font-bold text-gray-900 mb-8">마이페이지</h1>

        <div className="space-y-2">
          {menuItems.map((item) => (
            <button
              key={item.id}
              onClick={() => handleMenuClick(item)}
              disabled={!item.enabled}
              className={`
                w-full text-left px-4 py-4 bg-white rounded-lg
                border border-gray-200
                transition-all duration-200
                ${
                  item.enabled
                    ? "hover:bg-indigo-50 hover:border-indigo-200 cursor-pointer"
                    : "opacity-60 cursor-not-allowed"
                }
              `}
            >
              <span
                className={`text-base ${
                  item.enabled ? "text-gray-900" : "text-gray-500"
                }`}
              >
                {item.label}
              </span>
            </button>
          ))}
        </div>
      </div>
    </div>
  );
};

export default MyPage;

