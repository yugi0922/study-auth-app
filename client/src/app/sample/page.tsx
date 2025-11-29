"use client";
import { useState } from "react";

export default function ApiSamplePage() {
  const [message, setMessage] = useState<string>("");

  const callPublic = async () => {
    const res = await fetch("http://localhost:8082/public");
    const text = await res.text();
    setMessage(`✅ /public → ${text}`);
  };

  const callPrivateWithoutToken = async () => {
    const res = await fetch("http://localhost:8082/private");
    setMessage(`❌ /private（トークンなし）→ status ${res.status}`);
  };

  const callPrivateWithToken = async () => {
    const token = sessionStorage.getItem("access_token");
    if (!token) {
      setMessage("⚠️ access_token が見つかりません。まずログインしてください。");
      return;
    }

    const res = await fetch("http://localhost:8082/private", {
      headers: { Authorization: `Bearer ${token}` },
    });
    const text = await res.text();
    setMessage(`✅ /private（トークンあり）→ ${text}`);
  };

  // ⭐ ここを「verify API 呼び出し版」に置き換える
  const verifyIdToken = async () => {
    const idToken = sessionStorage.getItem("id_token");

    if (!idToken) {
      setMessage("⚠️ id_token が見つかりません。まずログインしてください。");
      return;
    }

    const res = await fetch("/api/auth/verify", {
      method: "POST",
      headers: { "Content-Type": "application/json" },
      body: JSON.stringify({ id_token: idToken }),
    });

    const data = await res.json();

    if (!res.ok || !data.ok) {
      setMessage(`❌ 検証失敗: ${data.reason}`);
      return;
    }

    // header + payload を表示（署名検証成功）
    setMessage(JSON.stringify(data, null, 2));
  };

  return (
    <main className="p-8 space-y-4">
      <h1 className="text-2xl font-semibold mb-4">API連携サンプル</h1>

      <p className="text-gray-600">
        Resource Serverとの連携、およびIDトークンの検証ができます。<br />
        本格的なプロフィール表示は{" "}
        <a href="/profile" className="text-blue-600 underline">
          こちら
        </a>
        。
      </p>

      <div className="space-x-3">
        <button
          onClick={callPublic}
          className="px-4 py-2 bg-gray-200 hover:bg-gray-300 rounded"
        >
          ① /public（認証なし）
        </button>

        <button
          onClick={callPrivateWithoutToken}
          className="px-4 py-2 bg-gray-200 hover:bg-gray-300 rounded"
        >
          ② /private（トークンなし）
        </button>

        <button
          onClick={callPrivateWithToken}
          className="px-4 py-2 bg-gray-200 hover:bg-gray-300 rounded"
        >
          ③ /private（トークンあり）
        </button>

        <button
          onClick={verifyIdToken}
          className="px-4 py-2 bg-indigo-200 hover:bg-indigo-300 rounded"
        >
          ④ IDトークン検証（署名＋claim）
        </button>
      </div>

      <pre className="bg-gray-100 p-3 rounded text-sm whitespace-pre-wrap">
        {message || "← ボタンを押して実行結果を確認してください"}
      </pre>
    </main>
  );
}
