"use client";
import { useEffect, useState } from "react";

type TokenResponse = {
  access_token: string;
  token_type: string;
  expires_in?: number;
  refresh_token?: string;
  id_token?: string;
  [key: string]: unknown; // 追加プロパティを許可したい場合
};

export default function Callback() {
  const [result, setResult] = useState<TokenResponse | null>(null);

  useEffect(() => {
    const url = new URL(window.location.href);
    const code = url.searchParams.get("code");
    const state = url.searchParams.get("state");

    if (!code || !state) return;

    (async () => {
      const tokenRes = await fetch("/api/auth/token", {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify({ code, state }),
      });

      if (!tokenRes.ok) {
        console.error("Token request failed:", tokenRes.statusText);
        return;
      }

      const data: TokenResponse = await tokenRes.json();
      setResult(data);

      // 学習用にアクセストークンを保存
      sessionStorage.setItem("access_token", data.access_token);
    })();
  }, []);

  return (
    <main className="p-8">
      <h1>Callbackページ</h1>
      <pre>{JSON.stringify(result, null, 2)}</pre>
    </main>
  );
}
