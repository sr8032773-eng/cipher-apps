interface Env {
  DB: D1Database;
  GITHUB_TOKEN: string;
  GITHUB_OWNER: string;
  GITHUB_REPO: string;
  ADMIN_USERNAME: string;
  ADMIN_PASSWORD: string;
  RESEND_API_KEY: string;
  EMAIL_FROM: string;
  APP_URL?: string;
}

type Ctx = EventContext<Env, string, unknown>;

const SESSION_TTL = 8 * 60 * 60;
const TOKEN_TTL = 24 * 60 * 60;
const LOCK_TTL = 2 * 60;

function json(data: unknown, status = 200, headers: HeadersInit = {}) {
  return new Response(JSON.stringify(data), {
    status,
    headers: { 'Content-Type': 'application/json; charset=utf-8', ...headers },
  });
}

function escapeHtml(value: string) {
  return value.replace(/[&<>'"]/g, (char) => ({
    '&': '&amp;', '<': '&lt;', '>': '&gt;', "'": '&#39;', '"': '&quot;'
  }[char] as string));
}

function parseCookies(request: Request): Record<string, string> {
  const raw = request.headers.get('Cookie') || '';
  const out: Record<string, string> = {};
  for (const part of raw.split(';')) {
    const [k, ...v] = part.trim().split('=');
    if (k) out[k] = decodeURIComponent(v.join('='));
  }
  return out;
}

function appUrl(request: Request, env: Env) {
  const configured = env.APP_URL?.trim().replace(/\/$/, '');
  if (configured) return configured;
  const url = new URL(request.url);
  return url.origin;
}

function randomToken(bytes = 32) {
  const data = crypto.getRandomValues(new Uint8Array(bytes));
  return btoa(String.fromCharCode(...data)).replace(/\+/g, '-').replace(/\//g, '_').replace(/=+$/, '');
}

function constantTimeEqual(a: string, b: string) {
  const aa = new TextEncoder().encode(a);
  const bb = new TextEncoder().encode(b);
  if (aa.length !== bb.length) return false;
  let diff = 0;
  for (let i = 0; i < aa.length; i++) diff |= aa[i] ^ bb[i];
  return diff === 0;
}

async function requireAdmin(request: Request, env: Env): Promise<{ username: string } | null> {
  const sessionId = parseCookies(request).cipher_admin_session;
  if (!sessionId) return null;
  const row = await env.DB.prepare(
    `SELECT username, expires_at FROM admin_sessions WHERE session_id = ? AND expires_at > unixepoch()`
  ).bind(sessionId).first<{ username: string; expires_at: number }>();
  return row ? { username: row.username } : null;
}

function forbiddenHtml(reason: string) {
  const safe = escapeHtml(reason);
  return `<!doctype html><html><head><meta charset="utf-8"><meta name="viewport" content="width=device-width,initial-scale=1"><title>403 Access Forbidden | Cipher</title></head><body style="margin:0;background:#020617;color:#f8fafc;min-height:100vh;display:flex;align-items:center;justify-content:center;font-family:Arial,sans-serif;padding:24px;box-sizing:border-box"><div style="max-width:520px;width:100%;background:#0f172a;border:1px solid #4c1d1d;border-radius:20px;padding:32px;box-sizing:border-box;box-shadow:0 25px 50px rgba(0,0,0,.45)"><div style="font-weight:800;font-size:18px;margin-bottom:10px">CIPHER <span style="font-size:10px;padding:4px 7px;border-radius:6px;background:#1e293b;color:#94a3b8">ZERO-TRUST</span></div><div style="font-size:12px;color:#94a3b8;margin-bottom:28px">Secure Binary Distribution Engine</div><div style="text-align:center"><div style="display:inline-block;padding:7px 11px;border-radius:999px;background:#4c0519;color:#fda4af;font-size:11px;font-weight:700">403 FORBIDDEN ACCESS</div><h1 style="font-size:22px;margin:16px 0 10px">One-Time Token Expired or Invalid</h1><p style="font-size:14px;line-height:1.7;color:#94a3b8">${safe}</p></div><div style="margin-top:24px;padding:14px;border-radius:12px;background:#020617;border:1px solid #1e293b;color:#94a3b8;font-size:12px;line-height:1.8"><div style="color:#a7f3d0">SECURITY ENFORCEMENT: VERIFIED</div><div>Protocol: Ephemeral Self-Destructing URL</div><div>Policy: Single-Use Authorization</div></div><div style="margin-top:24px"><a href="/" style="display:block;text-decoration:none;text-align:center;background:#0891b2;color:white;padding:12px;border-radius:10px;font-weight:700;font-size:14px">Return to Cipher Storefront</a></div></div></body></html>`;
}

async function sendApprovalEmail(request: Request, env: Env, data: { name?: string; email: string; releaseName: string; assetName?: string; url: string; expiresAt: string }) {
  if (!env.RESEND_API_KEY || !env.EMAIL_FROM) throw new Error('Email is not configured. Set RESEND_API_KEY and EMAIL_FROM.');
  const recipient = data.name ? `${data.name} <${data.email}>` : data.email;
  const release = escapeHtml(data.releaseName);
  const asset = escapeHtml(data.assetName || 'application binary');
  const download = escapeHtml(data.url);
  const expiry = new Date(data.expiresAt).toLocaleString('en-IN', { dateStyle: 'medium', timeStyle: 'short', timeZone: 'Asia/Kolkata' });
  const html = `<div style="font-family:Arial,sans-serif;line-height:1.6;color:#0f172a;max-width:620px;margin:auto"><h2>Cipher — Download Approved</h2><p>Your request for <strong>${release}</strong> has been approved.</p><p>Application: <strong>${asset}</strong></p><p><a href="${download}" style="display:inline-block;background:#0891b2;color:white;text-decoration:none;padding:12px 18px;border-radius:8px;font-weight:600">Download App</a></p><p style="font-size:13px;color:#475569">This is a single-use download link. It expires on ${escapeHtml(expiry)} (IST), or after the download is authorized.</p></div>`;
  const r = await fetch('https://api.resend.com/emails', {
    method: 'POST',
    headers: { Authorization: `Bearer ${env.RESEND_API_KEY}`, 'Content-Type': 'application/json' },
    body: JSON.stringify({ from: env.EMAIL_FROM, to: [recipient], subject: `Cipher download approved: ${data.releaseName}`, html, text: `Cipher download approved\n\nApplication: ${data.releaseName}\nFile: ${data.assetName || 'application binary'}\n\nDownload: ${data.url}\n\nThis is a single-use link.` })
  });
  if (!r.ok) throw new Error(`Email provider rejected the message (${r.status})`);
}

async function githubReleases(env: Env) {
  const owner = env.GITHUB_OWNER?.trim();
  const repo = env.GITHUB_REPO?.trim();
  if (!owner || !repo) throw new Error('GitHub repository is not configured.');
  const headers = { Accept: 'application/vnd.github+json', 'User-Agent': 'Cipher-Cloudflare', 'X-GitHub-Api-Version': '2022-11-28', ...(env.GITHUB_TOKEN ? { Authorization: `Bearer ${env.GITHUB_TOKEN}` } : {}) };
  const r = await fetch(`https://api.github.com/repos/${owner}/${repo}/releases`, { headers, cf: { cacheTtl: 60, cacheEverything: true } });
  if (!r.ok) throw new Error(`GitHub Releases request failed (${r.status})`);
  const releases = await r.json();
  return { owner, repo, releases: Array.isArray(releases) ? releases : [] };
}

async function githubAsset(env: Env, assetId: number) {
  const owner = env.GITHUB_OWNER?.trim();
  const repo = env.GITHUB_REPO?.trim();
  const headers = { Accept: 'application/octet-stream', 'User-Agent': 'Cipher-Cloudflare', 'X-GitHub-Api-Version': '2022-11-28', ...(env.GITHUB_TOKEN ? { Authorization: `Bearer ${env.GITHUB_TOKEN}` } : {}) };
  const r = await fetch(`https://api.github.com/repos/${owner}/${repo}/releases/assets/${assetId}`, { headers, redirect: 'manual' });
  if (r.status !== 302 && r.status !== 301) throw new Error(`GitHub asset request failed (${r.status})`);
  const location = r.headers.get('location');
  if (!location) throw new Error('GitHub did not return an asset download URL.');
  return location;
}

export const onRequest: PagesFunction<Env> = async (context) => {
  const { request, env } = context;
  const url = new URL(request.url);
  const path = url.pathname;

  try {
    if (path === '/api/releases' && request.method === 'GET') {
      const { owner, repo, releases } = await githubReleases(env);
      return json({ success: true, owner, repo, hasToken: Boolean(env.GITHUB_TOKEN), isMockFallback: false, releasesCount: releases.length, releases });
    }

    if (path === '/api/request-access' && request.method === 'POST') {
      const body = await request.json().catch(() => ({}));
      const name = typeof body.name === 'string' ? body.name.trim().slice(0, 100) : '';
      const email = typeof body.email === 'string' ? body.email.trim().toLowerCase() : '';
      const releaseName = typeof body.releaseName === 'string' ? body.releaseName.trim().slice(0, 200) : 'Cipher Application';
      const assetName = typeof body.assetName === 'string' ? body.assetName.trim().slice(0, 200) : null;
      const assetId = Number(body.assetId);
      if (!email || !/^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(email)) return json({ success: false, error: 'Valid email is required' }, 400);
      if (!Number.isSafeInteger(assetId) || assetId <= 0) return json({ success: false, error: 'Valid asset ID is required' }, 400);
      const id = `req_${Date.now()}_${randomToken(8)}`;
      await env.DB.prepare(`INSERT INTO access_requests (id,name,email,release_name,asset_id,asset_name,status,created_at) VALUES (?,?,?,?,?,?,?,unixepoch())`)
        .bind(id, name || null, email, releaseName, assetId, assetName, 'pending').run();
      return json({ success: true, requestId: id, message: 'Access request submitted successfully for review.' });
    }

    if (path === '/api/admin/login' && request.method === 'POST') {
      const body = await request.json().catch(() => ({}));
      const username = typeof body.username === 'string' ? body.username : '';
      const password = typeof body.password === 'string' ? body.password : '';
      if (!env.ADMIN_USERNAME || !env.ADMIN_PASSWORD) return json({ success: false, error: 'Admin credentials are not configured.' }, 500);
      if (!constantTimeEqual(username, env.ADMIN_USERNAME) || !constantTimeEqual(password, env.ADMIN_PASSWORD)) return json({ success: false, error: 'Invalid admin credentials' }, 401);
      const session = randomToken(32);
      await env.DB.prepare(`INSERT INTO admin_sessions (session_id,username,expires_at) VALUES (?,?,?)`).bind(session, env.ADMIN_USERNAME, Math.floor(Date.now()/1000) + SESSION_TTL).run();
      return json({ success: true, username: env.ADMIN_USERNAME }, 200, { 'Set-Cookie': `cipher_admin_session=${encodeURIComponent(session)}; HttpOnly; Secure; SameSite=Lax; Path=/; Max-Age=${SESSION_TTL}` });
    }

    if (path === '/api/admin/logout' && request.method === 'POST') {
      const sid = parseCookies(request).cipher_admin_session;
      if (sid) await env.DB.prepare(`DELETE FROM admin_sessions WHERE session_id=?`).bind(sid).run();
      return json({ success: true }, 200, { 'Set-Cookie': 'cipher_admin_session=; HttpOnly; Secure; SameSite=Lax; Path=/; Max-Age=0' });
    }

    if (path.startsWith('/api/admin/')) {
      if (!(await requireAdmin(request, env))) return json({ success: false, error: 'Admin authentication required' }, 401);

      if (path === '/api/admin/requests' && request.method === 'GET') {
        const rows = await env.DB.prepare(`SELECT id,name,email,release_name as releaseName,asset_id as assetId,asset_name as assetName,status,created_at as createdAt,approved_at as approvedAt,approved_token as approvedToken FROM access_requests ORDER BY created_at DESC LIMIT 500`).all();
        return json({ success: true, requests: rows.results || [] });
      }

      if (path === '/api/admin/tokens' && request.method === 'GET') {
        await env.DB.prepare(`UPDATE download_tokens SET status='expired' WHERE status IN ('active','locked') AND expires_at <= unixepoch()`).run();
        const rows = await env.DB.prepare(`SELECT token,email,asset_id as assetId,release_name as releaseName,asset_name as assetName,created_at as createdAt,expires_at as expiresAt,status FROM download_tokens WHERE status IN ('active','locked') ORDER BY created_at DESC`).all<{ token:string; email:string; assetId:number; releaseName:string; assetName:string|null; createdAt:number; expiresAt:number; status:string }>();
        return json({ success: true, tokens: (rows.results || []).map((r) => ({ ...r, ttlSeconds: Math.max(0, r.expiresAt - Math.floor(Date.now()/1000)) })) });
      }

      if (path === '/api/admin/revoke' && request.method === 'POST') {
        const body = await request.json().catch(() => ({}));
        const token = typeof body.token === 'string' ? body.token : '';
        if (!token) return json({ success: false, error: 'Token is required' }, 400);
        await env.DB.prepare(`UPDATE download_tokens SET status='revoked', used_at=unixepoch() WHERE token=?`).bind(token).run();
        return json({ success: true, message: 'Token successfully revoked' });
      }

      if (path === '/api/admin/approve' && request.method === 'POST') {
        const body = await request.json().catch(() => ({}));
        const requestId = typeof body.requestId === 'string' ? body.requestId : '';
        if (!requestId) return json({ success: false, error: 'Request ID is required' }, 400);
        const req = await env.DB.prepare(`SELECT * FROM access_requests WHERE id=?`).bind(requestId).first<any>();
        if (!req) return json({ success: false, error: 'Access request not found' }, 404);
        if (req.status !== 'pending') return json({ success: false, error: `Request is already ${req.status}` }, 409);
        const token = randomToken(32);
        const now = Math.floor(Date.now()/1000);
        const expires = now + TOKEN_TTL;
        await env.DB.prepare(`INSERT INTO download_tokens (token,email,asset_id,release_name,asset_name,created_at,expires_at,status) VALUES (?,?,?,?,?,?,?,'active')`).bind(token, req.email, req.asset_id, req.release_name, req.asset_name, now, expires).run();
        const downloadUrl = `${appUrl(request, env)}/api/download?token=${encodeURIComponent(token)}`;
        try {
          await sendApprovalEmail(request, env, { name: req.name || undefined, email: req.email, releaseName: req.release_name, assetName: req.asset_name || undefined, url: downloadUrl, expiresAt: new Date(expires * 1000).toISOString() });
        } catch (e) {
          await env.DB.prepare(`DELETE FROM download_tokens WHERE token=?`).bind(token).run();
          throw e;
        }
        await env.DB.prepare(`UPDATE access_requests SET status='approved',approved_at=?,approved_token=? WHERE id=? AND status='pending'`).bind(now, token, requestId).run();
        return json({ success: true, token, downloadUrl, expiresAt: new Date(expires * 1000).toISOString(), email: req.email, assetId: req.asset_id, emailSent: true });
      }
    }

    if (path === '/api/download' && request.method === 'GET') {
      const token = url.searchParams.get('token') || '';
      if (!token) return new Response(forbiddenHtml('No authorization token was provided with this download request.'), { status: 403, headers: { 'Content-Type': 'text/html; charset=utf-8' } });
      const row = await env.DB.prepare(`SELECT * FROM download_tokens WHERE token=?`).bind(token).first<any>();
      const now = Math.floor(Date.now()/1000);
      if (!row || row.expires_at <= now || !['active'].includes(row.status)) return new Response(forbiddenHtml('This one-time download link is invalid, expired, or has already been used.'), { status: 403, headers: { 'Content-Type': 'text/html; charset=utf-8' } });

      const lockUntil = now + LOCK_TTL;
      const lock = await env.DB.prepare(`UPDATE download_tokens SET status='locked',lock_until=? WHERE token=? AND status='active' AND expires_at>?`).bind(lockUntil, token, now).run();
      if ((lock.meta.changes || 0) !== 1) return new Response(forbiddenHtml('This download link is already being used in another download session.'), { status: 409, headers: { 'Content-Type': 'text/html; charset=utf-8' } });

      let location: string;
      try {
        location = await githubAsset(env, Number(row.asset_id));
      } catch (e) {
        await env.DB.prepare(`UPDATE download_tokens SET status='active',lock_until=NULL WHERE token=? AND status='locked'`).bind(token).run();
        return new Response(forbiddenHtml('The requested GitHub Release asset could not be retrieved.'), { status: 502, headers: { 'Content-Type': 'text/html; charset=utf-8' } });
      }

      const upstream = await fetch(location);
      if (!upstream.ok || !upstream.body) {
        await env.DB.prepare(`UPDATE download_tokens SET status='active',lock_until=NULL WHERE token=? AND status='locked'`).bind(token).run();
        return new Response(forbiddenHtml('The application file could not be downloaded from the GitHub Release.'), { status: 502, headers: { 'Content-Type': 'text/html; charset=utf-8' } });
      }

      await env.DB.prepare(`UPDATE download_tokens SET status='used',used_at=unixepoch(),lock_until=NULL WHERE token=? AND status='locked'`).bind(token).run();
      const filename = String(row.asset_name || `cipher-app-${row.asset_id}`).replace(/[\\/\r\n\"]/g, '_');
      const headers = new Headers();
      headers.set('Content-Type', upstream.headers.get('Content-Type') || 'application/octet-stream');
      headers.set('Content-Disposition', `attachment; filename="${filename}"`);
      headers.set('Cache-Control', 'no-store, private');
      headers.set('X-Cipher-Authorization', 'single-use');
      const length = upstream.headers.get('Content-Length');
      if (length) headers.set('Content-Length', length);
      return new Response(upstream.body, { status: 200, headers });
    }

    return json({ success: false, error: 'Not found' }, 404);
  } catch (error: any) {
    console.error('[Cipher]', error);
    return json({ success: false, error: error?.message || 'Internal server error' }, 500);
  }
};
