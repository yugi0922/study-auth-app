import { test, expect } from '@playwright/test';

const NEXT_BASE = 'http://localhost:3000';
const SAS_BASE = 'http://localhost:8081';
const SAS_LOGIN = `${SAS_BASE}/login`;

// 既存テスト：ログイン開始 → SAS /login まで
test('ログイン開始 → SASのログイン画面にリダイレクトされる', async ({ page }) => {
  // ① Next.js の /login ページへ
  await page.goto('/login');

  // ② 「ログイン開始」ボタンをクリック
  await page.getByRole('button', { name: 'ログイン開始' }).click();

  // ③ SAS 側の /login に飛ぶのを待つ
  await page.waitForURL((url) => {
    return url.origin === SAS_BASE && url.pathname === '/login';
  });

  // ④ URL を確認
  expect(page.url()).toBe(SAS_LOGIN);

  console.log('✅ ログイン開始 → SAS /login へのリダイレクトを確認');
});

// 新テスト：SASでログイン → /callback に code / state が付いて戻る
test('SASでログインすると /callback に code と state が付いて戻る', async ({ page }) => {
  // ① Next.js の /login ページへ
  await page.goto('/login');

  // ② 「ログイン開始」ボタンをクリック → SAS /login へ
  await page.getByRole('button', { name: 'ログイン開始' }).click();

  // ③ SAS /login への遷移を待機
  await page.waitForURL((url) => {
    return url.origin === SAS_BASE && url.pathname === '/login';
  });

  // ④ SAS のログインフォームに入力（ここは環境に合わせて修正）
  // --- Spring Security デフォルトログイン画面前提 ---
  await page.getByLabel('Username').fill('user');   // ← あなたのユーザー名に
  await page.getByLabel('Password').fill('password');   // ← あなたのパスワードに

  await page.getByRole('button', { name: 'Sign in' }).click();
  // ↑ ボタンのラベルが違う場合は 'ログイン' などに変更

  // ⑤ Next.js 側の callback に戻ってくるのを待機
  await page.waitForURL((url) => {
    return url.origin === NEXT_BASE && url.pathname === '/callback';
  });

  // ⑥ URL から code / state を確認
  const currentUrl = new URL(page.url());

  const code = currentUrl.searchParams.get('code');
  const state = currentUrl.searchParams.get('state');

  expect(code).not.toBeNull();
  expect(state).not.toBeNull();

  console.log('✅ SAS ログイン完了 → /callback に code / state が付いて戻るのを確認');
});
