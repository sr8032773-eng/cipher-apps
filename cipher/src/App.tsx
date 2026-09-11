import React, { useState, useEffect } from 'react';
import {
  Shield,
  Lock,
  Package,
  KeyRound,
  RefreshCw,
  CheckCircle2,
  Search,
  Layers,
  X,
  SlidersHorizontal,
  Check,
} from 'lucide-react';
import { Navbar } from './components/Navbar';
import { ReleaseCard } from './components/ReleaseCard';
import { RequestAccessModal } from './components/RequestAccessModal';
import { AdminDashboard } from './components/AdminDashboard';
import { DownloadErrorView } from './components/DownloadErrorView';
import { GitHubRelease, ReleaseAsset, StoreSyncStatus } from './types';

export default function App() {
  const [currentView, setCurrentView] = useState<'store' | 'admin' | 'error-preview'>(() => {
    if (window.location.pathname === '/admin') return 'admin';
    return 'store';
  });

  const [releases, setReleases] = useState<GitHubRelease[]>([]);
  const [syncStatus, setSyncStatus] = useState<StoreSyncStatus | null>(null);
  const [isLoadingReleases, setIsLoadingReleases] = useState<boolean>(true);
  const [searchTerm, setSearchTerm] = useState('');
  const [selectedCategory, setSelectedCategory] = useState<'all' | 'stable' | 'prerelease'>('all');

  // Request Access Modal State
  const [isRequestModalOpen, setIsRequestModalOpen] = useState(false);
  const [activeReleaseForModal, setActiveReleaseForModal] = useState<GitHubRelease | null>(null);
  const [activeAssetForModal, setActiveAssetForModal] = useState<ReleaseAsset | null>(null);

  // Browser history sync
  useEffect(() => {
    const handlePopState = () => {
      if (window.location.pathname === '/admin') {
        setCurrentView('admin');
      } else {
        setCurrentView('store');
      }
    };
    window.addEventListener('popstate', handlePopState);
    return () => window.removeEventListener('popstate', handlePopState);
  }, []);

  const navigateTo = (view: 'store' | 'admin' | 'error-preview') => {
    setCurrentView(view);
    if (view === 'admin') {
      window.history.pushState({}, '', '/admin');
    } else if (view === 'store') {
      window.history.pushState({}, '', '/');
    }
  };

  // Fetch Releases from Backend
  const fetchReleases = async () => {
    setIsLoadingReleases(true);
    try {
      const res = await fetch('/api/releases');
      const data = await res.json();

      if (res.ok && data.success) {
        setReleases(data.releases || []);
        setSyncStatus({
          connected: !data.errorMessage,
          owner: data.owner,
          repo: data.repo,
          hasToken: data.hasToken,
          isMockFallback: Boolean(data.isMockFallback),
          releasesCount: (data.releases || []).length,
          lastSyncedAt: new Date().toISOString(),
          errorMessage: data.errorMessage,
        });
      } else {
        throw new Error(data.error || 'Failed to fetch releases');
      }
    } catch (err: any) {
      console.error('Releases fetch failed:', err);
      setSyncStatus({
        connected: false,
        owner: 'cipher',
        repo: 'appstore',
        hasToken: false,
        isMockFallback: true,
        releasesCount: 0,
        lastSyncedAt: new Date().toISOString(),
      });
    } finally {
      setIsLoadingReleases(false);
    }
  };

  useEffect(() => {
    fetchReleases();
  }, []);

  const handleOpenRequestModal = (release: GitHubRelease, selectedAsset: ReleaseAsset) => {
    setActiveReleaseForModal(release);
    setActiveAssetForModal(selectedAsset);
    setIsRequestModalOpen(true);
  };

  // Filter releases by search term and category
  const filteredReleases = releases.filter((r) => {
    const term = searchTerm.toLowerCase().trim();
    const matchesSearch =
      !term ||
      r.name.toLowerCase().includes(term) ||
      r.tag_name.toLowerCase().includes(term) ||
      r.body.toLowerCase().includes(term) ||
      r.assets.some((a) => a.name.toLowerCase().includes(term));

    if (!matchesSearch) return false;

    if (selectedCategory === 'stable') {
      return !r.prerelease;
    }
    if (selectedCategory === 'prerelease') {
      return r.prerelease;
    }
    return true;
  });

  return (
    <div className="min-h-screen bg-slate-950 text-slate-100 flex flex-col selection:bg-cyan-500/30 selection:text-cyan-200 font-sans">
      {/* Navigation Bar */}
      <Navbar
        currentView={currentView}
        onNavigate={navigateTo}
        syncStatus={syncStatus}
        onRefreshReleases={fetchReleases}
        isRefreshing={isLoadingReleases}
      />

      {/* Main View Router */}
      {currentView === 'admin' ? (
        <AdminDashboard onBackToStore={() => navigateTo('store')} />
      ) : currentView === 'error-preview' ? (
        <DownloadErrorView onBackToStore={() => navigateTo('store')} />
      ) : (
        /* Public Storefront (Route: /) */
        <main className="flex-1 max-w-7xl w-full mx-auto px-4 sm:px-6 lg:px-8 py-8 space-y-8">
          {/* Hero Banner Section */}
          <div className="relative rounded-3xl bg-gradient-to-b from-slate-900 via-slate-900/90 to-slate-950 border border-slate-800/90 p-6 sm:p-10 shadow-2xl overflow-hidden">
            {/* Ambient Background Glows */}
            <div className="absolute top-0 right-1/4 w-96 h-96 bg-cyan-500/10 rounded-full blur-3xl pointer-events-none" />
            <div className="absolute bottom-0 left-10 w-80 h-80 bg-blue-600/10 rounded-full blur-3xl pointer-events-none" />

            <div className="relative z-10 max-w-3xl space-y-4">
              <div className="inline-flex items-center gap-2 px-3 py-1 rounded-full bg-cyan-950/80 border border-cyan-800/50 text-cyan-300 text-xs font-mono font-medium shadow-inner">
                <Shield className="w-3.5 h-3.5 text-cyan-400" />
                <span>Zero-Trust Distribution</span>
                <span className="w-1 h-1 rounded-full bg-cyan-400" />
                <span className="text-slate-400">Cryptographic Signing</span>
              </div>

              <h1 className="text-3xl sm:text-5xl font-extrabold tracking-tight text-white leading-tight">
                Cipher Private App Store
              </h1>

              {/* Status bar & Protocol Info */}
              <div className="pt-2 flex flex-wrap items-center gap-3 text-xs font-mono text-slate-400">
                <div className="flex items-center gap-1.5 px-3 py-1 rounded-lg bg-slate-950/80 border border-slate-800">
                  <Shield className="w-3.5 h-3.5 text-cyan-400" />
                  <span>Protocol:</span>
                  <span className="text-cyan-300">Zero-Trust Ephemeral Link</span>
                </div>

                <div className="flex items-center gap-1.5 px-3 py-1 rounded-lg bg-slate-950/80 border border-slate-800">
                  <Layers className="w-3.5 h-3.5 text-cyan-400" />
                  <span>Registry:</span>
                  <span className="text-slate-200">Vercel KV Key-Value Storage</span>
                </div>

                <div className="flex items-center gap-1.5 px-3 py-1 rounded-lg bg-slate-950/80 border border-slate-800">
                  <KeyRound className="w-3.5 h-3.5 text-cyan-400" />
                  <span>Token Life:</span>
                  <span className="text-slate-200">24-Hour Single-Use Expiry</span>
                </div>
              </div>
            </div>
          </div>

          {/* Prominent Storefront Search Bar */}
          <div className="rounded-2xl bg-slate-900/80 border border-slate-800 p-4 sm:p-5 shadow-xl space-y-4">
            <div className="flex flex-col sm:flex-row items-stretch sm:items-center gap-3">
              {/* Primary Search Input Field */}
              <div className="relative flex-1">
                <div className="absolute inset-y-0 left-0 pl-3.5 flex items-center pointer-events-none text-slate-400">
                  <Search className="w-4 h-4 text-cyan-400" />
                </div>
                <input
                  id="storefront-search-input"
                  type="text"
                  value={searchTerm}
                  onChange={(e) => setSearchTerm(e.target.value)}
                  placeholder="Search applications by name, version tag, or binary package..."
                  className="w-full pl-10 pr-10 py-3 bg-slate-950 border border-slate-700/80 rounded-xl text-sm text-white placeholder-slate-500 focus:outline-none focus:border-cyan-500 focus:ring-1 focus:ring-cyan-500 transition-all font-sans"
                />
                {searchTerm && (
                  <button
                    onClick={() => setSearchTerm('')}
                    className="absolute inset-y-0 right-0 pr-3.5 flex items-center text-slate-400 hover:text-white transition-colors"
                    title="Clear search"
                  >
                    <X className="w-4 h-4" />
                  </button>
                )}
              </div>

              {/* Category / Release Type Filter Pills */}
              <div className="flex items-center gap-1.5 bg-slate-950 p-1 rounded-xl border border-slate-800 shrink-0">
                <button
                  onClick={() => setSelectedCategory('all')}
                  className={`px-3 py-2 rounded-lg text-xs font-medium transition-all ${
                    selectedCategory === 'all'
                      ? 'bg-cyan-950 text-cyan-300 border border-cyan-800/60 shadow-sm'
                      : 'text-slate-400 hover:text-slate-200'
                  }`}
                >
                  All Releases
                </button>
                <button
                  onClick={() => setSelectedCategory('stable')}
                  className={`px-3 py-2 rounded-lg text-xs font-medium transition-all ${
                    selectedCategory === 'stable'
                      ? 'bg-cyan-950 text-cyan-300 border border-cyan-800/60 shadow-sm'
                      : 'text-slate-400 hover:text-slate-200'
                  }`}
                >
                  Stable Only
                </button>
                <button
                  onClick={() => setSelectedCategory('prerelease')}
                  className={`px-3 py-2 rounded-lg text-xs font-medium transition-all ${
                    selectedCategory === 'prerelease'
                      ? 'bg-cyan-950 text-cyan-300 border border-cyan-800/60 shadow-sm'
                      : 'text-slate-400 hover:text-slate-200'
                  }`}
                >
                  Pre-Releases
                </button>
              </div>
            </div>

            {/* Results Count & Filter Summary */}
            <div className="flex items-center justify-between text-xs text-slate-400 px-1 pt-1 border-t border-slate-800/60 font-mono">
              <div className="flex items-center gap-2">
                <Package className="w-3.5 h-3.5 text-cyan-400" />
                <span>
                  Showing <strong className="text-white font-semibold">{filteredReleases.length}</strong> of{' '}
                  <span className="text-slate-300">{releases.length}</span> available applications
                </span>
                {searchTerm && (
                  <span className="hidden sm:inline text-slate-500">
                    for &ldquo;<span className="text-cyan-300">{searchTerm}</span>&rdquo;
                  </span>
                )}
              </div>

              {searchTerm && (
                <button
                  onClick={() => setSearchTerm('')}
                  className="text-cyan-400 hover:underline flex items-center gap-1 text-[11px]"
                >
                  Reset Filter
                </button>
              )}
            </div>
          </div>

          {/* Releases Grid */}
          {isLoadingReleases ? (
            <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6">
              {[1, 2, 3].map((i) => (
                <div
                  key={i}
                  className="rounded-2xl bg-slate-900/50 border border-slate-800/60 p-6 animate-pulse flex flex-col justify-between gap-6"
                >
                  <div className="flex items-center gap-4">
                    <div className="w-14 h-14 sm:w-16 sm:h-16 rounded-xl bg-slate-800 shrink-0" />
                    <div className="flex-1 space-y-2">
                      <div className="h-5 w-3/4 bg-slate-800 rounded-lg" />
                      <div className="h-4 w-1/3 bg-slate-800/80 rounded" />
                    </div>
                  </div>
                  <div className="h-11 bg-slate-800 rounded-xl" />
                </div>
              ))}
            </div>
          ) : filteredReleases.length === 0 ? (
            <div className="p-12 rounded-2xl bg-slate-900/40 border border-slate-800 text-center space-y-4">
              <Package className="w-12 h-12 text-slate-600 mx-auto" />
              <div>
                <h3 className="text-base font-semibold text-white">No applications match your search</h3>
                <p className="text-xs text-slate-400 mt-1">
                  Try adjusting your search keywords or switching category filters.
                </p>
              </div>
              {(searchTerm || selectedCategory !== 'all') && (
                <button
                  onClick={() => {
                    setSearchTerm('');
                    setSelectedCategory('all');
                  }}
                  className="px-4 py-2 rounded-xl bg-slate-800 hover:bg-slate-700 text-cyan-300 text-xs font-medium transition-colors"
                >
                  Clear All Filters
                </button>
              )}
            </div>
          ) : (
            <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6">
              {filteredReleases.map((release) => (
                <ReleaseCard
                  key={release.id}
                  release={release}
                  onRequestAccess={handleOpenRequestModal}
                />
              ))}
            </div>
          )}
        </main>
      )}

      {/* Footer */}
      <footer className="border-t border-slate-800/80 bg-slate-950 py-6 mt-12 text-xs font-mono text-slate-500">
        <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 flex flex-col sm:flex-row items-center justify-between gap-3">
          <div className="flex items-center gap-2.5">
            <img
              src="/cipherlogo1_2.jpeg"
              alt="Cipher"
              className="w-6 h-6 object-contain shrink-0"
              referrerPolicy="no-referrer"
            />
            <span className="text-slate-300 font-semibold">CIPHER</span>
            <span>• Private App Store</span>
          </div>

          <div className="flex items-center gap-4">
            <button
              onClick={() => navigateTo('error-preview')}
              className="hover:text-slate-300 transition-colors text-[11px]"
            >
              Test 403 Error Page
            </button>
            <span>•</span>
            <span className="text-slate-400">Zero-Trust Distribution Engine</span>
          </div>
        </div>
      </footer>

      {/* Request Access Modal */}
      <RequestAccessModal
        isOpen={isRequestModalOpen}
        onClose={() => setIsRequestModalOpen(false)}
        release={activeReleaseForModal}
        selectedAsset={activeAssetForModal}
        onSuccessSubmitted={() => {
          // Keep open to show success state
        }}
      />
    </div>
  );
}
