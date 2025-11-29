import { NextRequest, NextResponse } from "next/server";
import { createRemoteJWKSet, jwtVerify } from "jose";
import { COOKIE_NONCE } from "@/lib/auth/cookies";

const ISSUER = process.env.NEXT_PUBLIC_AUTH_BASE!;   // http://localhost:8081
const AUDIENCE = process.env.NEXT_PUBLIC_CLIENT_ID!; // nextjs-client
const JWKS_URI = `${ISSUER}/oauth2/jwks`;

export async function POST(req: NextRequest) {
  try {
    // ----------------------------------------
    // 1. id_token を受け取り
    // ----------------------------------------
    const { id_token } = await req.json();
    if (!id_token) {
      return NextResponse.json(
        { ok: false, reason: "id_token is required" },
        { status: 400 },
      );
    }

    // ----------------------------------------
    // 2. nonce（PKCE/OIDC のリプレイ防止）をCookieから取得
    //    → クライアントから送らせるのはダメ（改ざんできる）
    // ----------------------------------------
    const expectedNonce = req.cookies.get(COOKIE_NONCE)?.value;
    if (!expectedNonce) {
      return NextResponse.json(
        { ok: false, reason: "nonce cookie not found" },
        { status: 400 },
      );
    }

    // ----------------------------------------
    // 3. 公開鍵セット（JWKS）を読み込み
    //    createRemoteJWKSet にはキャッシュ機能あり
    // ----------------------------------------
    const jwks = createRemoteJWKSet(new URL(JWKS_URI));

    // ----------------------------------------
    // 4. RS256 署名検証 + iss/aud/exp の標準チェック
    // ----------------------------------------
    const { payload, protectedHeader } = await jwtVerify(id_token, jwks, {
      issuer: ISSUER,
      audience: AUDIENCE,
      clockTolerance: 5, // 秒単位で多少の時間ズレを許容
    });

    // ----------------------------------------
    // 5. nonce チェック（OIDC 固有）
    // ----------------------------------------
    if (payload.nonce !== expectedNonce) {
      return NextResponse.json(
        { ok: false, reason: "invalid nonce" },
        { status: 400 },
      );
    }

    // ----------------------------------------
    // 6. 成功レスポンス → header と payload を返す
    // ----------------------------------------
    return NextResponse.json(
      {
        ok: true,
        header: protectedHeader,
        payload,
      },
      { status: 200 },
    );

  } catch (e: unknown) {
    const message = e instanceof Error ? e.message : "verify failed";
    return NextResponse.json(
      { ok: false, reason: message },
      { status: 400 },
    );
  }
}
