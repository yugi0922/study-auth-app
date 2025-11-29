"use client";
import { useEffect, useState } from "react";

type TokenResponse = {
  access_token: string;
  token_type: string;
  expires_in?: number;
  refresh_token?: string;
  id_token?: string;
  [key: string]: unknown; // 追加のキーも許容（柔軟なトークンレスポンス対応）
};

export default function Callback() {
  // トークンレスポンスを画面に表示するための state
  const [result, setResult] = useState<TokenResponse | null>(null);

  useEffect(() => {
    // 現在の URL（例: http://localhost:3000/callback?code=xxx&state=yyy）を解析
    const url = new URL(window.location.href);
    const code = url.searchParams.get("code");   // 認可コード
    const state = url.searchParams.get("state"); // CSRF 対策の state

    // oauth_pkce_auth_flow１６ state,codeの検証
    // code または state が無い場合は処理しない
    if (!code || !state) return;

    // oauth_pkce_auth_flow１７ トークン取得
    (async () => {
      // Next.js の API Route（/api/auth/token）にトークンリクエストを送る
      // ここで PKCE の code_verifier もサーバー側で使ってトークン発行する
      const tokenRes = await fetch("/api/auth/token", {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify({ code, state }), // 認可コードと state をサーバーへ渡す
      });

      // HTTP エラー時は何もせず終了
      if (!tokenRes.ok) {
        console.error("Token request failed:", tokenRes.statusText);
        return;
      }

      // 成功した場合は JSON としてトークンレスポンスを取得
      const data: TokenResponse = await tokenRes.json();

      // 画面表示用に state に保存
      setResult(data);

      // -------------------------------
      // 取得したトークンをブラウザに保存
      // -------------------------------

      // アクセストークンは protected API を叩くために使う
      sessionStorage.setItem("access_token", data.access_token);

      // ID トークンはユーザー情報（claims）をデコードするために使う
      if (data.id_token) {
        sessionStorage.setItem("id_token", data.id_token);
      }
    })();
  }, []);

  return (
    <main className="p-8">
      <h1>Callbackページ</h1>
      {/* トークンレスポンスを整形して表示 */}
      <pre>{JSON.stringify(result, null, 2)}</pre>
    </main>
  );
}
