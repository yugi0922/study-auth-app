import { NextResponse } from "next/server";
import { randomUrlSafeString, sha256Base64url } from "@/lib/auth/crypto";
import { COOKIE_VERIFIER, COOKIE_STATE, COOKIE_NONCE } from "@/lib/auth/cookies";

// oauth_pkce_auth_flow２
export async function GET() {
  // 認可サーバーの基本URL、クライアントID、リダイレクトURIなどを環境変数から取得
  const authBase = process.env.NEXT_PUBLIC_AUTH_BASE!;
  const clientId = process.env.NEXT_PUBLIC_CLIENT_ID!;
  const redirectUri = encodeURIComponent(process.env.NEXT_PUBLIC_REDIRECT_URI!);
  const scope = encodeURIComponent(process.env.NEXT_PUBLIC_SCOPE || "openid");

  // PKCE用の code_verifier を生成（ランダム文字列）
  const codeVerifier = randomUrlSafeString(64);

  // code_verifier を SHA-256 → Base64URL に変換して code_challenge を作成
  const codeChallenge = sha256Base64url(codeVerifier);

  // CSRF対策の state、リプレイ対策の nonce を生成
  const state = randomUrlSafeString(32);
  const nonce = randomUrlSafeString(32);

  // 認可エンドポイントにリダイレクトするための URL を組み立て
  const authorizeUrl =
    `${authBase}/oauth2/authorize?response_type=code&client_id=${encodeURIComponent(clientId)}` +
    `&redirect_uri=${redirectUri}&scope=${scope}` +
    `&code_challenge=${codeChallenge}&code_challenge_method=S256` +
    `&state=${state}&nonce=${nonce}`;

  // クライアントへ渡すレスポンス（Next.js の API Route の戻り値）
  const res = NextResponse.json({ authorizeUrl });

  // 生成した値を Cookie に保存（後で /callback で照合するため）
  // httpOnly にすることで JS から参照できず、安全性を確保
  const cookieOpts = { httpOnly: true, sameSite: "lax" as const, path: "/" };

  res.cookies.set(COOKIE_VERIFIER, codeVerifier, cookieOpts); // PKCE 検証用
  res.cookies.set(COOKIE_STATE, state, cookieOpts);           // CSRF 防止
  res.cookies.set(COOKIE_NONCE, nonce, cookieOpts);           // リプレイ対策

  // authorizeUrl を含んだ JSON を返す
  return res;
}
