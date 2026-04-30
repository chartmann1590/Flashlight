import { expect, test } from "@playwright/test";

test("website renders screenshots and release CTA", async ({ page }) => {
  await page.goto("/");

  await expect(page).toHaveTitle(/Flashlight/);
  await expect(page.getByRole("link", { name: /Download Latest Release/i })).toBeVisible();
  await expect(page.getByRole("link", { name: /View Source on GitHub/i })).toBeVisible();

  const screenshots = [
    page.locator('img[alt="Flashlight home screen"]'),
    page.locator('img[alt="Flashlight on screen"]'),
    page.locator('img[alt="Flashlight website on phone"]')
  ];

  for (const screenshot of screenshots) {
    await expect(screenshot).toBeVisible();
    await expect(screenshot).toHaveAttribute("src", /screenshots\//);
  }
});
