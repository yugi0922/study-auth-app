import { execSync } from 'child_process';

async function globalSetup() {
  console.log('🚀 docker compose up -d');
  execSync('docker compose up -d', { stdio: 'inherit' });

  console.log('⏳ SAS が起動するまで待機...');
  // healthcheck（例: jwksエンドポイントが返るまで待つ）
  for (let i = 0; i < 20; i++) {
    try {
      execSync('curl -f http://localhost:8081/.well-known/jwks.json');
      console.log('👌 SAS ready');
      return;
    } catch {
      await new Promise((r) => setTimeout(r, 1500));
    }
  }

  throw new Error('SAS 起動を確認できませんでした');
}

export default globalSetup;
