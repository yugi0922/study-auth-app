import { NextRequest, NextResponse } from "next/server";

// クッキー名はauthorizeルートと揃える
const COOKIE_STATE = "oauth_state";
const COOKIE_VERIFIER = "pkce_verifier";

export async function POST(req: NextRequest) {
  try {
    const authBase = process.env.NEXT_PUBLIC_AUTH_BASE || "http://localhost:8081";
    const clientId = process.env.NEXT_PUBLIC_CLIENT_ID || "study-nextjs";
    const redirectUri = process.env.NEXT_PUBLIC_REDIRECT_URI || "http://localhost:3000/callback";

    // bodyからcodeとstateを取得
    const { code, state } = await req.json();

    // クッキーを取得（NextRequestは自動でパース済み）
    const cookies = req.cookies;
    const codeVerifier = cookies.get(COOKIE_VERIFIER)?.value;
    const storedState = cookies.get(COOKIE_STATE)?.value;

    if (!code || !state || !codeVerifier || !storedState) {
      return NextResponse.json({ error: "missing_parameters" }, { status: 400 });
    }

    if (state !== storedState) {
      return NextResponse.json({ error: "invalid_state" }, { status: 400 });
    }

    // トークンエンドポイントへPOST
    const tokenRes = await fetch(`${authBase}/oauth2/token`, {
      method: "POST",
      headers: { "Content-Type": "application/x-www-form-urlencoded" },
      body: new URLSearchParams({
        grant_type: "authorization_code",
        code,
        redirect_uri: redirectUri,
        client_id: clientId,
        code_verifier: codeVerifier,
      }),
    });

    const tokenJson = await tokenRes.json();

    // 成功時・失敗時の両方に対応
    const res = NextResponse.json(tokenJson, { status: tokenRes.status });

    // PKCE検証用cookieを削除（使い捨て）
    res.cookies.set(COOKIE_VERIFIER, "", { path: "/", maxAge: 0 });
    res.cookies.set(COOKIE_STATE, "", { path: "/", maxAge: 0 });

    return res;
  } catch (e) {
    console.error("token exchange error", e);
    return NextResponse.json({ error: "internal_server_error" }, { status: 500 });
  }
}
