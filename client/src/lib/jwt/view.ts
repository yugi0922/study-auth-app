export function decodeJwtWithoutVerify(jwt: string) {
  // 1) JWT を「.」で3つに分ける
  const [h, p, s] = jwt.split(".");

  // 2) Base64URL → Base64 → UTF-8 → JSON に直す関数
  const decode = (b64url: string) =>
    JSON.parse(
      Buffer.from(
        b64url.replace(/-/g, "+").replace(/_/g, "/"), // Base64URL → Base64に戻す
        "base64"
      ).toString("utf-8") // バイト列 → 文字列
    );

  // 3) header と payload をデコードして返す（署名はそのまま）
  return { header: decode(h), payload: decode(p), signatureB64Url: s };
}

// ID トークンの payload だけを取り出す（UI 用）
export function extractIdTokenClaims(jwt: string) {
  const { payload } = decodeJwtWithoutVerify(jwt);
  return payload;
}
