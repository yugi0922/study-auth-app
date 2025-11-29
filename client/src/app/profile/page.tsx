"use client";

import { useEffect, useState } from "react";
import { extractIdTokenClaims } from "@/lib/jwt/view"; // 既にあなたが作ったやつ

type Claims = Record<string, unknown>;

export default function ProfilePage() {
  const [claims, setClaims] = useState<Claims | null>(null);
  const [error, setError] = useState<string>("");

useEffect(() => {
  const idToken = sessionStorage.getItem("id_token");

  if (!idToken) {
    queueMicrotask(() => {
      setError("⚠️ id_token が見つかりません。まずログインしてください。");
    });
    return;
  }

  try {
    const decoded = extractIdTokenClaims(idToken);
    queueMicrotask(() => setClaims(decoded));
  } catch  {
    queueMicrotask(() => {
      setError("❌ id_token のデコードに失敗しました。");
    });
  }
}, []);


  if (error) {
    return (
      <main className="p-8">
        <h1 className="text-2xl font-semibold mb-4">プロフィール</h1>
        <p className="text-red-500">{error}</p>
      </main>
    );
  }

  if (!claims) {
    return (
      <main className="p-8">
        <h1 className="text-2xl font-semibold mb-4">プロフィール</h1>
        <p>読込中...</p>
      </main>
    );
  }

  return (
    <main className="p-8 space-y-6">
      <h1 className="text-2xl font-semibold mb-4">プロフィール</h1>

      <table className="table-auto border-collapse">
        <tbody>
          {Object.entries(claims).map(([key, value]) => (
            <tr key={key} className="border-b">
              <td className="px-4 py-2 font-medium text-gray-700">{key}</td>
              <td className="px-4 py-2">{String(value)}</td>
            </tr>
          ))}
        </tbody>
      </table>

      <pre className="bg-gray-100 p-4 rounded text-sm whitespace-pre-wrap">
        {JSON.stringify(claims, null, 2)}
      </pre>
    </main>
  );
}
