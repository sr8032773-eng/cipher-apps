import React from 'react';
import { Lock, RefreshCw, ShieldCheck } from 'lucide-react';
import { CipherLogo } from './CipherLogo';
import { StoreSyncStatus } from '../types';

interface NavbarProps {
  currentView: 'store' | 'admin' | 'error-preview';
  onNavigate: (view: 'store' | 'admin' | 'error-preview') => void;
  syncStatus: StoreSyncStatus | null;
  onRefreshReleases: () => void;
  isRefreshing: boolean;
}

export const Navbar: React.FC<NavbarProps> = ({
  currentView,
  onNavigate,
  syncStatus,
  onRefreshReleases,
  isRefreshing,
}) => {
  return (
    <header className="sticky top-0 z-40 w-full border-b border-slate-800/80 bg-slate-950/80 backdrop-blur-xl">
      <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 h-18 flex items-center justify-between gap-4">
        {/* Primary Branding */}
        <div
          className="flex items-center gap-2.5 sm:gap-3.5 cursor-pointer select-none group"
          onClick={() => onNavigate('store')}
        >
          <img
            src="/cipherlogo1_2.jpeg"
            alt="Cipher Logo"
            className="w-8 h-8 sm:w-10 sm:h-10 md:w-11 md:h-11 object-contain shrink-0 transition-transform duration-200 group-hover:scale-105"
            referrerPolicy="no-referrer"
          />

          <div className="flex flex-col justify-center">
            <div className="flex items-center gap-1.5 sm:gap-2">
              <span className="text-lg sm:text-xl font-bold tracking-tight text-white font-mono">
                CIPHER
              </span>
              <span className="px-1.5 sm:px-2 py-0.5 text-[9px] sm:text-[10px] uppercase font-semibold font-mono tracking-wider rounded-md bg-cyan-950 text-cyan-400 border border-cyan-800/50">
                Zero-Trust
              </span>
            </div>
            <p className="text-[11px] sm:text-xs text-slate-400 hidden sm:block">
              Private App Store
            </p>
          </div>
        </div>

        {/* Security & System Status Badge */}
        <div className="hidden md:flex items-center gap-2 px-3 py-1.5 rounded-lg bg-slate-900/90 border border-slate-800 text-xs">
          <span className="w-2 h-2 rounded-full bg-emerald-400 animate-pulse" />
          <ShieldCheck className="w-3.5 h-3.5 text-cyan-400" />
          <span className="text-slate-300 font-medium">Distribution Engine Active</span>
          <span className="text-slate-600">•</span>
          <span className="text-slate-400 text-[11px]">Ephemeral Tokens</span>
          <button
            onClick={onRefreshReleases}
            disabled={isRefreshing}
            title="Refresh application catalogue"
            className="ml-1.5 text-slate-400 hover:text-cyan-400 transition-colors"
          >
            <RefreshCw className={`w-3.5 h-3.5 ${isRefreshing ? 'animate-spin text-cyan-400' : ''}`} />
          </button>
        </div>

        {/* Navigation Actions */}
        <div className="flex items-center gap-1.5 sm:gap-3 shrink-0">
          <button
            onClick={() => onNavigate('store')}
            className={`px-2.5 sm:px-3.5 py-1.5 sm:py-2 rounded-xl text-xs sm:text-sm font-medium whitespace-nowrap transition-all ${
              currentView === 'store'
                ? 'bg-slate-800 text-cyan-300 border border-slate-700 shadow-sm'
                : 'text-slate-400 hover:text-slate-200 hover:bg-slate-900'
            }`}
          >
            Storefront
          </button>

          <button
            onClick={() => onNavigate('admin')}
            className={`px-2.5 sm:px-3.5 py-1.5 sm:py-2 rounded-xl text-xs sm:text-sm font-medium whitespace-nowrap transition-all flex items-center gap-1.5 ${
              currentView === 'admin'
                ? 'bg-cyan-950/80 text-cyan-300 border border-cyan-800/60 shadow-lg shadow-cyan-950/50'
                : 'text-slate-400 hover:text-slate-200 hover:bg-slate-900'
            }`}
          >
            <Lock className="w-3.5 h-3.5 text-cyan-400 shrink-0" />
            <span>Admin</span>
            <span className="hidden sm:inline">Portal</span>
          </button>
        </div>
      </div>
    </header>
  );
};
