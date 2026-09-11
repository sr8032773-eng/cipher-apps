import React, { useState, useEffect } from 'react';
import {
  Lock,
  Key,
  User,
  CheckCircle2,
  XCircle,
  Copy,
  ExternalLink,
  Shield,
  Trash2,
  Clock,
  RefreshCw,
  AlertCircle,
  Check,
  Zap,
  ArrowLeft,
  FileCode,
} from 'lucide-react';
import { AccessRequest, TokenRecord } from '../types';

interface AdminDashboardProps {
  onBackToStore: () => void;
}

export const AdminDashboard: React.FC<AdminDashboardProps> = ({
  onBackToStore,
}) => {
  // Gatekeeper Auth State
  const [isAuthenticated, setIsAuthenticated] = useState<boolean>(() => {
    // sessionStorage is only a UI hint; the server-side HttpOnly cookie is the real auth.
    return sessionStorage.getItem('cipher_admin_auth') === 'true';
  });
  const [username, setUsername] = useState('');
  const [password, setPassword] = useState('');
  const [authError, setAuthError] = useState('');
  const [isLoggingIn, setIsLoggingIn] = useState(false);

  // Data States
  const [activeTab, setActiveTab] = useState<'requests' | 'tokens' | 'diagnostics'>('requests');
  const [requests, setRequests] = useState<AccessRequest[]>([]);
  const [tokens, setTokens] = useState<TokenRecord[]>([]);
  const [isLoading, setIsLoading] = useState(false);

  // Approved Link Modal / Notification State
  const [lastApproved, setLastApproved] = useState<{
    token: string;
    downloadUrl: string;
    email: string;
    releaseName: string;
    expiresAt: string;
  } | null>(null);

  const [copiedToken, setCopiedToken] = useState<string | null>(null);
  const [actionMessage, setActionMessage] = useState<string | null>(null);

  // Load Data on Mount or Login
  useEffect(() => {
    if (isAuthenticated) {
      loadAdminData();
    }
  }, [isAuthenticated]);

  const handleLogin = async (e: React.FormEvent) => {
    e.preventDefault();
    setIsLoggingIn(true);
    setAuthError('');

    try {
      const res = await fetch('/api/admin/login', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ username, password }),
      });

      const data = await res.json();
      if (res.ok && data.success) {
        setIsAuthenticated(true);
        sessionStorage.setItem('cipher_admin_auth', 'true');
      } else {
        setAuthError(data.error || 'Invalid credentials');
      }
    } catch (err: any) {
      setAuthError('Connection error occurred');
    } finally {
      setIsLoggingIn(false);
    }
  };

  const handleLogout = async () => {
    try {
      await fetch('/api/admin/logout', { method: 'POST', credentials: 'same-origin' });
    } catch (err) {
      console.error('Admin logout failed:', err);
    } finally {
      setIsAuthenticated(false);
      sessionStorage.removeItem('cipher_admin_auth');
    }
  };

  const loadAdminData = async () => {
    setIsLoading(true);
    try {
      const [reqRes, tokRes] = await Promise.all([
        fetch('/api/admin/requests', { credentials: 'same-origin' }),
        fetch('/api/admin/tokens', { credentials: 'same-origin' }),
      ]);

      if (reqRes.status === 401 || tokRes.status === 401) {
        setIsAuthenticated(false);
        sessionStorage.removeItem('cipher_admin_auth');
        return;
      }

      if (reqRes.ok) {
        const reqData = await reqRes.json();
        setRequests(reqData.requests || []);
      }
      if (tokRes.ok) {
        const tokData = await tokRes.json();
        setTokens(tokData.tokens || []);
      }
    } catch (err) {
      console.error('Failed to load admin data:', err);
    } finally {
      setIsLoading(false);
    }
  };

  // Approve Access Request
  const handleApprove = async (req: AccessRequest) => {
    try {
      const res = await fetch('/api/admin/approve', {
        method: 'POST',
        credentials: 'same-origin',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({
          requestId: req.id,
          email: req.email,
          assetId: req.assetId,
          releaseName: req.releaseName,
          assetName: req.assetName,
        }),
      });

      const data = await res.json();
      if (res.ok && data.success) {
        setLastApproved({
          token: data.token,
          downloadUrl: data.downloadUrl,
          email: req.email,
          releaseName: req.releaseName,
          expiresAt: data.expiresAt,
        });

        setActionMessage(`Approved download link generated and emailed to ${req.email}`);
        setTimeout(() => setActionMessage(null), 5000);
        await loadAdminData();
      } else if (res.status === 401) {
        setIsAuthenticated(false);
        sessionStorage.removeItem('cipher_admin_auth');
      } else {
        alert(data.error || 'Approval failed');
      }
    } catch (err: any) {
      alert('Network error approving request');
    }
  };

  // Revoke Token
  const handleRevokeToken = async (token: string) => {
    if (!confirm('Are you sure you want to revoke this download token immediately?')) return;

    try {
      const res = await fetch('/api/admin/revoke', {
        method: 'POST',
        credentials: 'same-origin',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ token }),
      });

      if (res.ok) {
        setActionMessage('Token has been successfully revoked and removed from secure storage');
        setTimeout(() => setActionMessage(null), 4000);
        await loadAdminData();
      } else if (res.status === 401) {
        setIsAuthenticated(false);
        sessionStorage.removeItem('cipher_admin_auth');
      }
    } catch (err) {
      alert('Failed to revoke token');
    }
  };

  const copyToClipboard = (text: string, identifier: string) => {
    navigator.clipboard.writeText(text);
    setCopiedToken(identifier);
    setTimeout(() => setCopiedToken(null), 2500);
  };

  const pendingRequests = requests.filter((r) => r.status === 'pending');
  const pastRequests = requests.filter((r) => r.status !== 'pending');

  // --- 1. Gatekeeper Login View ---
  if (!isAuthenticated) {
    return (
      <div className="min-h-[80vh] flex items-center justify-center p-4">
        <div className="w-full max-w-md bg-slate-900 border border-slate-800 rounded-2xl p-8 shadow-2xl relative overflow-hidden">
          <div className="absolute -top-20 -right-20 w-40 h-40 bg-cyan-500/10 rounded-full blur-3xl pointer-events-none" />

          {/* Header */}
          <div className="text-center mb-6">
            <img
              src="/cipherlogo1_2.jpeg"
              alt="Cipher Logo"
              className="w-12 h-12 sm:w-14 sm:h-14 mx-auto mb-3 object-contain select-none"
              referrerPolicy="no-referrer"
            />
            <h2 className="text-xl font-bold text-white tracking-tight">Admin Gatekeeper</h2>
            <p className="text-xs text-slate-400 mt-1">
              Cipher Zero-Trust Repository Access Control
            </p>
          </div>

          <form onSubmit={handleLogin} className="space-y-4">
            <div>
              <label className="block text-xs font-semibold uppercase tracking-wider text-slate-300 mb-1">
                Admin Username
              </label>
              <div className="relative">
                <div className="absolute inset-y-0 left-0 pl-3 flex items-center pointer-events-none text-slate-500">
                  <User className="w-4 h-4" />
                </div>
                <input
                  type="text"
                  required
                  value={username}
                  onChange={(e) => setUsername(e.target.value)}
                  placeholder="Enter admin username"
                  className="w-full pl-9 pr-3 py-2.5 bg-slate-950 border border-slate-800 rounded-xl text-sm text-white font-mono placeholder:text-slate-600 focus:outline-none focus:border-cyan-500"
                />
              </div>
            </div>

            <div>
              <label className="block text-xs font-semibold uppercase tracking-wider text-slate-300 mb-1">
                Admin Password
              </label>
              <div className="relative">
                <div className="absolute inset-y-0 left-0 pl-3 flex items-center pointer-events-none text-slate-500">
                  <Key className="w-4 h-4" />
                </div>
                <input
                  type="password"
                  required
                  value={password}
                  onChange={(e) => setPassword(e.target.value)}
                  placeholder="Enter admin password"
                  className="w-full pl-9 pr-3 py-2.5 bg-slate-950 border border-slate-800 rounded-xl text-sm text-white font-mono placeholder:text-slate-600 focus:outline-none focus:border-cyan-500"
                />
              </div>
            </div>

            {authError && (
              <div className="p-3 rounded-xl bg-rose-950/70 border border-rose-900/60 text-rose-300 text-xs">
                {authError}
              </div>
            )}

            <button
              type="submit"
              disabled={isLoggingIn}
              className="w-full py-2.5 px-4 rounded-xl bg-gradient-to-r from-cyan-600 to-blue-600 hover:from-cyan-500 hover:to-blue-500 text-white text-xs font-semibold tracking-wide transition-all shadow-md shadow-cyan-950/50"
            >
              {isLoggingIn ? 'Authenticating...' : 'Unlock Admin Portal'}
            </button>

            <div className="pt-2 text-center">
              <button
                type="button"
                onClick={onBackToStore}
                className="text-xs text-slate-400 hover:text-slate-200 transition-colors"
              >
                ← Return to Public Storefront
              </button>
            </div>
          </form>
        </div>
      </div>
    );
  }

  // --- 2. Authenticated Admin Dashboard ---
  return (
    <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-8 space-y-8">
      {/* Top Bar */}
      <div className="flex flex-col sm:flex-row items-start sm:items-center justify-between gap-4 pb-6 border-b border-slate-800/80">
        <div className="flex items-center gap-3">
          <button
            onClick={onBackToStore}
            className="p-2 rounded-xl bg-slate-900 border border-slate-800 hover:bg-slate-800 text-slate-300 transition-colors"
            title="Back to Store"
          >
            <ArrowLeft className="w-5 h-5" />
          </button>
          <div>
            <div className="flex items-center gap-2.5">
              <h1 className="text-2xl font-bold text-white tracking-tight">Admin Dashboard</h1>
              <span className="px-2 py-0.5 rounded text-[10px] uppercase font-mono font-semibold bg-emerald-950 text-emerald-400 border border-emerald-800/50">
                Authenticated
              </span>
            </div>
            <p className="text-xs text-slate-400 mt-0.5">
              Vercel KV Access Request Gatekeeper & Token Revocation Console
            </p>
          </div>
        </div>

        <div className="flex items-center gap-2 sm:gap-3">
          <button
            onClick={loadAdminData}
            disabled={isLoading}
            className="px-3.5 py-2 rounded-xl bg-slate-900 hover:bg-slate-800 border border-slate-800 text-slate-300 text-xs font-medium flex items-center gap-1.5 transition-colors"
          >
            <RefreshCw className={`w-3.5 h-3.5 ${isLoading ? 'animate-spin text-cyan-400' : ''}`} />
            Refresh Data
          </button>

          <button
            onClick={handleLogout}
            className="px-3.5 py-2 rounded-xl bg-slate-900 hover:bg-rose-950/40 border border-slate-800 hover:border-rose-900/50 text-slate-300 hover:text-rose-300 text-xs font-medium transition-colors"
          >
            Sign Out
          </button>
        </div>
      </div>

      {/* Action Notification Banner */}
      {actionMessage && (
        <div className="p-3.5 rounded-xl bg-emerald-950/80 border border-emerald-800/60 text-emerald-300 text-xs flex items-center justify-between animate-in fade-in">
          <div className="flex items-center gap-2">
            <CheckCircle2 className="w-4 h-4 text-emerald-400 shrink-0" />
            <span>{actionMessage}</span>
          </div>
          <button
            onClick={() => setActionMessage(null)}
            className="text-emerald-400 hover:text-emerald-200"
          >
            ✕
          </button>
        </div>
      )}

      {/* Last Approved Link Highlight Box */}
      {lastApproved && (
        <div className="p-5 rounded-2xl bg-cyan-950/40 border border-cyan-500/40 shadow-xl space-y-3 relative overflow-hidden">
          <div className="flex items-center justify-between">
            <div className="flex items-center gap-2">
              <Zap className="w-4 h-4 text-cyan-400" />
              <h3 className="text-sm font-bold text-white tracking-tight">
                One-Time Download Link Generated (24-Hour Expiration)
              </h3>
            </div>
            <button
              onClick={() => setLastApproved(null)}
              className="text-xs text-slate-400 hover:text-white"
            >
              Dismiss
            </button>
          </div>

          <div className="grid grid-cols-1 md:grid-cols-3 gap-2 text-xs font-mono text-slate-300">
            <div>
              <span className="text-slate-500">Recipient: </span>
              <span className="text-cyan-300">{lastApproved.email}</span>
            </div>
            <div>
              <span className="text-slate-500">Application: </span>
              <span className="text-white">{lastApproved.releaseName}</span>
            </div>
            <div>
              <span className="text-slate-500">Valid Until: </span>
              <span className="text-slate-400">{new Date(lastApproved.expiresAt).toLocaleTimeString()}</span>
            </div>
          </div>

          <div className="flex flex-col sm:flex-row items-center gap-2">
            <input
              type="text"
              readOnly
              value={lastApproved.downloadUrl}
              className="w-full px-3 py-2 bg-slate-950 border border-slate-800 rounded-xl text-xs font-mono text-cyan-300 select-all"
            />
            <div className="flex items-center gap-2 w-full sm:w-auto shrink-0">
              <button
                onClick={() => copyToClipboard(lastApproved.downloadUrl, 'approved-link')}
                className="w-full sm:w-auto px-4 py-2 rounded-xl bg-cyan-600 hover:bg-cyan-500 text-white text-xs font-semibold flex items-center justify-center gap-1.5 transition-colors shadow-sm"
              >
                {copiedToken === 'approved-link' ? (
                  <>
                    <Check className="w-3.5 h-3.5" />
                    Copied Link!
                  </>
                ) : (
                  <>
                    <Copy className="w-3.5 h-3.5" />
                    Copy Link
                  </>
                )}
              </button>

              <a
                href={lastApproved.downloadUrl}
                target="_blank"
                rel="noopener noreferrer"
                className="w-full sm:w-auto px-3 py-2 rounded-xl bg-slate-800 hover:bg-slate-700 border border-slate-700 text-slate-200 text-xs font-medium flex items-center justify-center gap-1 transition-colors"
                title="Verify single-use download flow"
              >
                <ExternalLink className="w-3.5 h-3.5" />
                Test Download
              </a>
            </div>
          </div>
          <p className="text-[11px] text-slate-400 font-mono">
            Note: Visiting this download link will verify against Vercel KV, self-destruct the token immediately, and stream the asset.
          </p>
        </div>
      )}

      {/* Tabs */}
      <div className="flex items-center gap-2 border-b border-slate-800">
        <button
          onClick={() => setActiveTab('requests')}
          className={`pb-3 px-3 text-xs sm:text-sm font-semibold border-b-2 transition-all flex items-center gap-2 ${
            activeTab === 'requests'
              ? 'border-cyan-500 text-cyan-400'
              : 'border-transparent text-slate-400 hover:text-slate-200'
          }`}
        >
          <span>Pending Access Requests</span>
          <span className="px-2 py-0.5 rounded-full text-[11px] bg-slate-800 text-slate-300 font-mono">
            {pendingRequests.length}
          </span>
        </button>

        <button
          onClick={() => setActiveTab('tokens')}
          className={`pb-3 px-3 text-xs sm:text-sm font-semibold border-b-2 transition-all flex items-center gap-2 ${
            activeTab === 'tokens'
              ? 'border-cyan-500 text-cyan-400'
              : 'border-transparent text-slate-400 hover:text-slate-200'
          }`}
        >
          <span>Active Download Tokens</span>
          <span className="px-2 py-0.5 rounded-full text-[11px] bg-slate-800 text-slate-300 font-mono">
            {tokens.length}
          </span>
        </button>
      </div>

      {/* --- Tab 1: Pending Access Requests Section --- */}
      {activeTab === 'requests' && (
        <div className="space-y-4">
          <div className="flex items-center justify-between">
            <h2 className="text-base font-bold text-white tracking-tight">
              Access Requests from Vercel KV
            </h2>
            <span className="text-xs text-slate-400 font-mono">
              Total Recorded: {requests.length}
            </span>
          </div>

          {pendingRequests.length === 0 ? (
            <div className="p-8 rounded-2xl bg-slate-900/50 border border-slate-800/80 text-center space-y-3">
              <CheckCircle2 className="w-10 h-10 text-emerald-400/80 mx-auto" />
              <h3 className="text-sm font-semibold text-white">No Pending Requests</h3>
              <p className="text-xs text-slate-400 max-w-md mx-auto">
                All application access requests have been approved or no users have submitted requests yet. You can submit a request on the public storefront to test approval.
              </p>
              <button
                onClick={onBackToStore}
                className="mt-2 px-4 py-2 rounded-xl bg-slate-800 hover:bg-slate-700 text-xs text-slate-200 font-medium"
              >
                Go to Storefront
              </button>
            </div>
          ) : (
            <div className="space-y-3">
              {pendingRequests.map((req) => (
                <div
                  key={req.id}
                  className="p-5 rounded-2xl bg-slate-900 border border-slate-800/90 hover:border-slate-700 transition-all flex flex-col md:flex-row items-start md:items-center justify-between gap-4"
                >
                  <div className="space-y-1.5">
                    <div className="flex items-center gap-2.5">
                      <span className="text-sm font-bold text-white font-mono">
                        {req.email}
                      </span>
                      {req.name && (
                        <span className="text-xs text-slate-300 font-sans px-2 py-0.5 rounded bg-slate-800 border border-slate-700">
                          {req.name}
                        </span>
                      )}
                      <span className="px-2 py-0.5 rounded text-[10px] uppercase font-mono font-semibold bg-amber-950/80 text-amber-300 border border-amber-800/50">
                        Pending Approval
                      </span>
                    </div>

                    <div className="text-xs text-slate-300 flex flex-wrap items-center gap-x-4 gap-y-1">
                      <span>
                        App: <strong className="text-cyan-300">{req.releaseName}</strong>
                      </span>
                      {req.assetName && (
                        <span className="text-slate-400 font-mono">
                          Binary: {req.assetName}
                        </span>
                      )}
                      <span className="text-slate-500 font-mono">
                        Asset ID: {req.assetId}
                      </span>
                      <span className="text-slate-500 font-mono">
                        Submitted: {new Date(req.createdAt).toLocaleString()}
                      </span>
                    </div>
                  </div>

                  <button
                    onClick={() => handleApprove(req)}
                    className="w-full md:w-auto px-5 py-2.5 rounded-xl bg-gradient-to-r from-cyan-600 to-blue-600 hover:from-cyan-500 hover:to-blue-500 text-white text-xs font-semibold tracking-wide flex items-center justify-center gap-1.5 transition-all shadow-md shadow-cyan-950/40 shrink-0"
                  >
                    <Check className="w-4 h-4" />
                    Approve & Generate Link
                  </button>
                </div>
              ))}
            </div>
          )}

          {/* Past/Approved History */}
          {pastRequests.length > 0 && (
            <div className="pt-6 space-y-3">
              <h3 className="text-xs font-semibold uppercase tracking-wider text-slate-400">
                Previously Approved Requests ({pastRequests.length})
              </h3>
              <div className="space-y-2">
                {pastRequests.map((req) => (
                  <div
                    key={req.id}
                    className="p-3.5 rounded-xl bg-slate-950 border border-slate-800/60 text-xs font-mono flex items-center justify-between text-slate-400"
                  >
                    <div>
                      {req.name && <span className="text-white font-sans mr-2">{req.name}</span>}
                      <span className="text-slate-200">{req.email}</span>
                      <span className="mx-2">•</span>
                      <span>{req.releaseName}</span>
                    </div>
                    <span className="text-emerald-400">Approved</span>
                  </div>
                ))}
              </div>
            </div>
          )}
        </div>
      )}

      {/* --- Tab 2: Active Tokens Section --- */}
      {activeTab === 'tokens' && (
        <div className="space-y-4">
          <div className="flex items-center justify-between">
            <div>
              <h2 className="text-base font-bold text-white tracking-tight">
                Active Unused Download Tokens in Vercel KV
              </h2>
              <p className="text-xs text-slate-400">
                Tokens automatically self-destruct once downloaded or when their 24h expiration expires.
              </p>
            </div>
            <span className="text-xs text-slate-400 font-mono">
              Count: {tokens.length}
            </span>
          </div>

          {tokens.length === 0 ? (
            <div className="p-8 rounded-2xl bg-slate-900/50 border border-slate-800/80 text-center space-y-3">
              <Clock className="w-10 h-10 text-slate-500 mx-auto" />
              <h3 className="text-sm font-semibold text-white">No Active Tokens</h3>
              <p className="text-xs text-slate-400 max-w-md mx-auto">
                There are currently no active unused download tokens. Tokens appear here when an access request is approved, and disappear as soon as the recipient downloads the file.
              </p>
            </div>
          ) : (
            <div className="space-y-3">
              {tokens.map((tok) => {
                const downloadUrl = `${window.location.origin}/api/download?token=${tok.token}`;
                return (
                  <div
                    key={tok.token}
                    className="p-5 rounded-2xl bg-slate-900 border border-slate-800 hover:border-slate-700 transition-all space-y-3"
                  >
                    <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-2">
                      <div className="flex items-center gap-2">
                        <span className="px-2 py-0.5 rounded text-[10px] uppercase font-mono font-semibold bg-cyan-950 text-cyan-400 border border-cyan-800/50">
                          Active Token
                        </span>
                        <span className="text-xs text-slate-300 font-mono">
                          UUID: {tok.token}
                        </span>
                      </div>

                      <div className="flex items-center gap-2">
                        <button
                          onClick={() => copyToClipboard(downloadUrl, tok.token)}
                          className="px-2.5 py-1.5 rounded-lg bg-slate-800 hover:bg-slate-700 text-xs text-slate-200 font-mono flex items-center gap-1 transition-colors"
                        >
                          {copiedToken === tok.token ? (
                            <Check className="w-3 h-3 text-emerald-400" />
                          ) : (
                            <Copy className="w-3 h-3 text-slate-400" />
                          )}
                          Copy Link
                        </button>

                        <button
                          onClick={() => handleRevokeToken(tok.token)}
                          className="px-2.5 py-1.5 rounded-lg bg-rose-950/60 hover:bg-rose-900 border border-rose-800/50 text-xs text-rose-300 font-mono flex items-center gap-1 transition-colors"
                          title="Revoke and delete token immediately from Vercel KV"
                        >
                          <Trash2 className="w-3 h-3" />
                          Revoke
                        </button>
                      </div>
                    </div>

                    <div className="grid grid-cols-1 sm:grid-cols-3 gap-2 text-xs font-mono text-slate-400 bg-slate-950/60 p-3 rounded-xl border border-slate-800/60">
                      <div>
                        <span className="text-slate-500">Recipient:</span>{' '}
                        <span className="text-slate-200">{tok.email}</span>
                      </div>
                      <div>
                        <span className="text-slate-500">Target App:</span>{' '}
                        <span className="text-white">{tok.releaseName || 'Cipher Package'}</span>
                      </div>
                      <div>
                        <span className="text-slate-500">Asset ID:</span>{' '}
                        <span className="text-cyan-400">{tok.assetId}</span>
                      </div>
                      <div>
                        <span className="text-slate-500">Issued:</span>{' '}
                        <span>{new Date(tok.createdAt).toLocaleTimeString()}</span>
                      </div>
                      <div>
                        <span className="text-slate-500">Expires:</span>{' '}
                        <span>{new Date(tok.expiresAt).toLocaleTimeString()}</span>
                      </div>
                      <div>
                        <span className="text-slate-500">Remaining TTL:</span>{' '}
                        <span className="text-amber-400">
                          {tok.ttlSeconds ? `${Math.round(tok.ttlSeconds / 60)} mins` : '< 24h'}
                        </span>
                      </div>
                    </div>
                  </div>
                );
              })}
            </div>
          )}
        </div>
      )}
    </div>
  );
};
