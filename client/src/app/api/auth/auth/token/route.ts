import { NextRequest, NextResponse } from "next/server";
import { COOKIE_STATE, COOKIE_VERIFIER } from "@/lib/auth/cookies";

export async function POST(req: NextRequest) {
  const authBase = process.env.NEXT_PUBLIC_AUTH_BASE!;
  const clientId = process.env.NEXT_PUBLIC_CLIENT_ID!;
  const redirectUri = process.env.NEXT_PUBLIC_REDIRECT_URI!;
  const { code, state } = await req.json();

  const cookies = req.cookies;
  const verifier = cookies.get(COOKIE_VERIFIER)?.value;
  const storedState = cookies.get(COOKIE_STATE)?.value;

  if (!verifier || state !== storedState) {
    return NextResponse.json({ error: "invalid_state" }, { status: 400 });
  }

  const tokenRes = await fetch(`${authBase}/oauth2/token`, {
    method: "POST",
    headers: { "Content-Type": "application/x-www-form-urlencoded" },
    body: new URLSearchParams({
      grant_type: "authorization_code",
      code,
      redirect_uri: redirectUri,
      client_id: clientId,
      code_verifier: verifier,
    }),
  });

  const tokenJson = await tokenRes.json();

  const res = NextResponse.json(tokenJson);
  res.cookies.set(COOKIE_VERIFIER, "", { path: "/", maxAge: 0 });
  res.cookies.set(COOKIE_STATE, "", { path: "/", maxAge: 0 });
  return res;
}
