import { expect, test } from "@playwright/test";

test("website renders screenshots and release CTA", async ({ page }) => {
  await page.goto("/");

  await expect(page).toHaveTitle(/Flashlight/);
  await expect(page.getByRole("link", { name: /Download signed APK/i })).toBeVisible();
  await expect(page.getByRole("link", { name: /View Source on GitHub/i })).toBeVisible();
  await expect(page.locator('a[href="https://buymeacoffee.com/charleshartmann"]')).toHaveCount(2);
  await expect(page.locator('a[href="privacy.html"]')).toHaveCount(2);

  const screenshots = [
    page.locator('img[alt="Flashlight home screen"]'),
    page.locator('img[alt="Flashlight torch on"]'),
    page.locator('img[alt="Flashlight settings"]'),
    page.locator('img[alt="Flashlight screen light"]'),
    page.locator('img[alt="Flashlight website on phone"]')
  ];

  for (const screenshot of screenshots) {
    await expect(screenshot).toBeVisible();
    await expect(screenshot).toHaveAttribute("src", /screenshots\//);
  }
});

test("privacy policy page loads", async ({ page }) => {
  await page.goto("/privacy.html");
  await expect(page).toHaveTitle(/Privacy Policy/);
  await expect(page.getByRole("heading", { name: /^Privacy Policy$/ })).toBeVisible();
  await expect(page.getByRole("link", { name: /^Home$/ })).toBeVisible();
});
