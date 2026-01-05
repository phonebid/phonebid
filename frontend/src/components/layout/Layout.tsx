interface LayoutProps {
  children: React.ReactNode;
}

import Header from "./Header";

const Layout: React.FC<LayoutProps> = ({ children }) => {
  return (
    // 높이 100vh 설정
    <div className="min-h-screen h-screen flex flex-col overflow-hidden">
      <Header />
      <main className="flex-1 overflow-y-auto min-h-0">{children}</main>
    </div>
  );
};

export default Layout;
