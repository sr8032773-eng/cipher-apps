# Cipher Private App Store — Cloudflare Free-Tier Edition

This version keeps the Cipher React/Tailwind UI and moves the backend to Cloudflare Pages Functions + D1.

## Services
- Cloudflare Pages: storefront hosting
- Cloudflare Pages Functions: API/backend
- Cloudflare D1: requests, admin sessions, single-use tokens
- GitHub Releases: application files
- Resend: approval emails

## Setup
1. Create a Cloudflare D1 database named `cipher-db`.
2. Copy its database ID into `wrangler.toml`.
3. Run `npx wrangler d1 execute cipher-db --remote --file=schema.sql`.
4. In Cloudflare Pages, create the project from the `cipher-store` GitHub repo.
5. Build command: `npm run build`
6. Output directory: `dist`
7. Add D1 binding named `DB` pointing to `cipher-db`.
8. Add encrypted environment variables: `GITHUB_OWNER`, `GITHUB_REPO`, `GITHUB_TOKEN`, `ADMIN_USERNAME`, `ADMIN_PASSWORD`, `RESEND_API_KEY`, `EMAIL_FROM`, `APP_URL`.

### Local development
Copy `.dev.vars.example` to `.dev.vars`, fill values, run:

`npm install`
`npx wrangler pages dev dist --d1=DB=cipher-db`

For local first-time D1 schema, use a local database with Wrangler or apply `schema.sql` to the remote database as described above.

## GitHub apps
Put downloadable applications in the separate `cipher-apps` repository as GitHub Releases. Cipher reads releases automatically and never exposes the GitHub token to the browser.
