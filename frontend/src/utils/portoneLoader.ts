declare global {
  interface Window {
    PortOne: typeof import("@portone/browser-sdk/v2").default | undefined;
  }
}

const PORTONE_SDK_URL = "https://cdn.portone.io/v2/browser-sdk.js";

type PortOneSdk = typeof import("@portone/browser-sdk/v2").default;

let loaderPromise: Promise<PortOneSdk> | null = null;

export const loadPortOneSdk = async (): Promise<PortOneSdk> => {
  if (window.PortOne) {
    return window.PortOne;
  }

  if (!loaderPromise) {
    loaderPromise = new Promise((resolve, reject) => {
      const script = document.createElement("script");
      script.src = PORTONE_SDK_URL;
      script.async = true;
      script.onload = () => {
        if (window.PortOne) {
          resolve(window.PortOne);
        } else {
          reject(new Error("PortOne SDK 로드에 실패했습니다."));
        }
      };
      script.onerror = () => reject(new Error("PortOne SDK 스크립트 로드 오류"));
      document.head.appendChild(script);
    });
  }

  return loaderPromise;
};

