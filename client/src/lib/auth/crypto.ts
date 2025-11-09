// crypto.ts
import { randomBytes, createHash } from "crypto";

/**
 * URLセーフなランダム文字列を生成する
 * @param bytes バイト長（デフォルト32）
 * @returns URL安全なBase64文字列
 */
export function randomUrlSafeString(bytes = 32): string {
  return randomBytes(bytes).toString("base64url");
}

/**
 * 任意データをBase64URL形式に変換する
 * （Node.js 14.18+ では Buffer が base64url を直接サポート）
 */
export function base64url(input: Buffer | Uint8Array | string): string {
  return Buffer.from(input).toString("base64url");
}

/**
 * SHA-256ハッシュをBase64URL形式で返す
 * PKCEやJWTなどで使用
 */
export function sha256Base64url(input: string): string {
  const hash = createHash("sha256").update(input).digest();
  return hash.toString("base64url");
}
