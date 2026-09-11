CREATE TABLE IF NOT EXISTS access_requests (
  id TEXT PRIMARY KEY,
  name TEXT,
  email TEXT NOT NULL,
  release_name TEXT NOT NULL,
  asset_id INTEGER NOT NULL,
  asset_name TEXT,
  status TEXT NOT NULL CHECK (status IN ('pending','approved','rejected')),
  created_at INTEGER NOT NULL,
  approved_at INTEGER,
  approved_token TEXT
);
CREATE INDEX IF NOT EXISTS idx_access_requests_created ON access_requests(created_at DESC);
CREATE INDEX IF NOT EXISTS idx_access_requests_status ON access_requests(status);

CREATE TABLE IF NOT EXISTS download_tokens (
  token TEXT PRIMARY KEY,
  email TEXT NOT NULL,
  asset_id INTEGER NOT NULL,
  release_name TEXT NOT NULL,
  asset_name TEXT,
  created_at INTEGER NOT NULL,
  expires_at INTEGER NOT NULL,
  status TEXT NOT NULL CHECK (status IN ('active','locked','used','revoked','expired')),
  lock_until INTEGER,
  used_at INTEGER
);
CREATE INDEX IF NOT EXISTS idx_download_tokens_status ON download_tokens(status);
CREATE INDEX IF NOT EXISTS idx_download_tokens_expires ON download_tokens(expires_at);

CREATE TABLE IF NOT EXISTS admin_sessions (
  session_id TEXT PRIMARY KEY,
  username TEXT NOT NULL,
  expires_at INTEGER NOT NULL
);
CREATE INDEX IF NOT EXISTS idx_admin_sessions_expires ON admin_sessions(expires_at);
