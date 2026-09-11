import React from 'react';
import { Lock, ArrowLeft, ShieldAlert, KeyRound } from 'lucide-react';

interface DownloadErrorViewProps {
  onBackToStore: () => void;
  reason?: string;
}

export const DownloadErrorView: React.FC<DownloadErrorViewProps> = ({
  onBackToStore,
  reason = 'The download authorization token provided is invalid, has reached its 24-hour expiration limit, or has already been consumed and self-destructed upon initial download.',
}) => {
  return (
    <div className="min-h-[80vh] flex items-center justify-center p-4">
      <div className="max-w-md w-full bg-slate-900/90 border border-rose-900/50 rounded-2xl p-8 shadow-2xl backdrop-blur-xl relative overflow-hidden">
        {/* Ambient Glows */}
        <div className="absolute -top-24 -right-24 w-48 h-48 bg-rose-500/10 rounded-full blur-3xl pointer-events-none" />
        <div className="absolute -bottom-24 -left-24 w-48 h-48 bg-cyan-500/10 rounded-full blur-3xl pointer-events-none" />

        <div className="flex items-center gap-3 mb-6">
          <img
            src="/cipherlogo1_2.jpeg"
            alt="Cipher"
            className="w-10 h-10 object-contain shrink-0"
            referrerPolicy="no-referrer"
          />
          <div>
            <h1 className="text-lg font-bold tracking-tight text-white flex items-center gap-2">
              CIPHER <span className="text-xs px-2 py-0.5 rounded bg-slate-800 text-slate-400 font-mono">ZERO-TRUST</span>
            </h1>
            <p className="text-xs text-slate-400">Secure Binary Distribution Engine</p>
          </div>
        </div>

        <div className="w-16 h-16 rounded-2xl bg-rose-950/70 border border-rose-800/60 flex items-center justify-center mb-6 mx-auto text-rose-400 shadow-lg shadow-rose-950/40">
          <Lock className="w-8 h-8" />
        </div>

        <div className="text-center space-y-3 mb-8">
          <div className="inline-flex items-center gap-1.5 px-3 py-1 rounded-full bg-rose-950/80 border border-rose-800/40 text-rose-400 text-xs font-semibold uppercase tracking-wider">
            403 Access Forbidden
          </div>
          <h2 className="text-xl font-bold text-white tracking-tight">One-Time Token Expired or Invalid</h2>
          <p className="text-sm text-slate-400 leading-relaxed font-sans">
            {reason}
          </p>
        </div>

        <div className="bg-slate-950/80 border border-slate-800 rounded-xl p-4 text-xs font-mono text-slate-400 space-y-2 mb-6">
          <div className="flex justify-between items-center text-slate-500">
            <span>SECURITY ENFORCEMENT</span>
            <span className="text-emerald-400">ENFORCED</span>
          </div>
          <div className="text-slate-300">Protocol: Ephemeral Single-Use URL</div>
          <div className="text-slate-300">Storage: Zero-Trust Vercel KV</div>
          <div className="text-rose-400 font-medium">Status: Token purged from key-value registry</div>
        </div>

        <div className="flex flex-col gap-3">
          <button
            onClick={onBackToStore}
            className="w-full py-3 px-4 rounded-xl bg-gradient-to-r from-cyan-600 to-blue-600 hover:from-cyan-500 hover:to-blue-500 text-white text-sm font-semibold text-center transition-all shadow-lg shadow-cyan-900/30 flex items-center justify-center gap-2"
          >
            <ArrowLeft className="w-4 h-4" />
            Return to Cipher Storefront
          </button>
        </div>
      </div>
    </div>
  );
};
