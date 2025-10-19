import { test, expect } from "@playwright/test";

test.describe("Admin Phone Models Page", () => {
  test("핵심 UI 요소가 렌더링된다", async ({ page }) => {
    await page.goto("/admin/phone-models");

    await expect(
      page.getByRole("heading", { name: "휴대폰 모델 관리" })
    ).toBeVisible();
    await expect(page.getByText("모델 생성", { exact: true })).toBeVisible();
    await expect(page.getByRole("button", { name: "옵션 추가" })).toBeVisible();
    await expect(
      page.getByRole("button", { name: "모델 등록" })
    ).toBeDisabled();

    await page.selectOption("select", { value: "APPLE" });

    const modelNameInput = page
      .locator("label")
      .filter({ hasText: "모델명" })
      .locator("..")
      .locator("input");

    await modelNameInput.fill("iPhone 16");

    await expect(page.getByRole("button", { name: "모델 등록" })).toBeEnabled();
  });
});
