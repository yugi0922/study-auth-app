import { NextResponse } from "next/server";
import { randomUrlSafeString, sha256Base64url } from "@/lib/auth/crypto";
import { COOKIE_VERIFIER, COOKIE_STATE, COOKIE_NONCE } from "@/lib/auth/cookies";

export async function GET() {
  const authBase = process.env.NEXT_PUBLIC_AUTH_BASE!;
  const clientId = process.env.NEXT_PUBLIC_CLIENT_ID!;
  const redirectUri = encodeURIComponent(process.env.NEXT_PUBLIC_REDIRECT_URI!);
  const scope = encodeURIComponent(process.env.NEXT_PUBLIC_SCOPE || "openid");

  const codeVerifier = randomUrlSafeString(64);
  const codeChallenge = sha256Base64url(codeVerifier);
  const state = randomUrlSafeString(32);
  const nonce = randomUrlSafeString(32);

  const authorizeUrl =
    `${authBase}/oauth2/authorize?response_type=code&client_id=${encodeURIComponent(clientId)}` +
    `&redirect_uri=${redirectUri}&scope=${scope}` +
    `&code_challenge=${codeChallenge}&code_challenge_method=S256` +
    `&state=${state}&nonce=${nonce}`;

  const res = NextResponse.json({ authorizeUrl });
  const cookieOpts = { httpOnly: true, sameSite: "lax" as const, path: "/" };

  res.cookies.set(COOKIE_VERIFIER, codeVerifier, cookieOpts);
  res.cookies.set(COOKIE_STATE, state, cookieOpts);
  res.cookies.set(COOKIE_NONCE, nonce, cookieOpts);

  return res;
}
