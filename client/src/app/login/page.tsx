"use client";
import { useState } from "react";

export default function LoginPage() {
  const [loading, setLoading] = useState(false);

  const start = async () => {
    setLoading(true);
    const res = await fetch("/api/auth/authorize");
    const { authorizeUrl } = await res.json();
    window.location.href = authorizeUrl;
  };

  return (
    <main className="p-8">
      <h1>ログイン画面</h1>
      <button
        onClick={start}
        disabled={loading}
        className="px-4 py-2 bg-black text-white rounded"
      >
        {loading ? "認可中..." : "ログイン開始"}
      </button>
    </main>
  );
}
