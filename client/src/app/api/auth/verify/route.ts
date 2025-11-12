import { NextRequest, NextResponse } from "next/server";
import { createRemoteJWKSet, jwtVerify } from "jose";

const ISSUER = process.env.NEXT_PUBLIC_AUTH_BASE!;   // 例: http://localhost:8081
const AUD     = process.env.NEXT_PUBLIC_CLIENT_ID!;  // 例: study-nextjs

// OIDC Discovery: http://issuer/.well-known/openid-configuration の jwks_uri と等価
const JWKS_URI = `${ISSUER}/oauth2/jwks`; // Spring Authorization Server の既定

export async function POST(req: NextRequest) {
  try {
    const { id_token, nonce } = await req.json(); // 画面から渡す
    if (!id_token) return NextResponse.json({ ok: false, reason: "missing id_token" }, { status: 400 });

    // 署名検証（RS256等はJWKSが教えてくれる）
    const jwks = createRemoteJWKSet(new URL(JWKS_URI));
    const { payload, protectedHeader } = await jwtVerify(id_token, jwks, {
      issuer: ISSUER,
      audience: AUD,
      // 多少の時刻ずれを許容（秒）
      clockTolerance: 5,
    });

    // 追加の論理チェック（nonce一致など）
    if (nonce && payload.nonce && payload.nonce !== nonce) {
      return NextResponse.json({ ok: false, reason: "invalid nonce" }, { status: 400 });
    }

    return NextResponse.json({
      ok: true,
      header: protectedHeader,
      payload,
    });
    
    } catch (e: unknown) {
    const message =
        e instanceof Error
        ? e.message
        : "verify failed";

    return NextResponse.json({ ok: false, reason: message }, { status: 400 });
    }
}