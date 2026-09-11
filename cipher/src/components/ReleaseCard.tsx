import React from 'react';
import { KeyRound, ChevronRight } from 'lucide-react';
import { GitHubRelease, ReleaseAsset } from '../types';

interface ReleaseCardProps {
  release: GitHubRelease;
  onRequestAccess: (release: GitHubRelease, selectedAsset: ReleaseAsset) => void;
}

export const ReleaseCard: React.FC<ReleaseCardProps> = ({ release, onRequestAccess }) => {
  // 2. Dynamic Logos (Asset Pairing):
  // Check the assets array for each release. If an asset ends in .png, .jpg, or .jpeg,
  // use its download URL as the image for that app's card.
  // If no image is found, use the default Cipher logo (cipherlogo1_2.jpeg).
  const imageAsset = release.assets.find((asset) => {
    const name = (asset.name || '').toLowerCase();
    return name.endsWith('.png') || name.endsWith('.jpg') || name.endsWith('.jpeg');
  });

  const logoUrl = imageAsset?.browser_download_url || '/cipherlogo1_2.jpeg';

  // 3. Dynamic Downloads:
  // Find the specific binary's assetId (the .zip or .apk file, or target distribution binary)
  const binaryAsset =
    release.assets.find((asset) => {
      const name = (asset.name || '').toLowerCase();
      return name.endsWith('.zip') || name.endsWith('.apk');
    }) ||
    release.assets.find((asset) => {
      const name = (asset.name || '').toLowerCase();
      return !name.endsWith('.png') && !name.endsWith('.jpg') && !name.endsWith('.jpeg');
    }) ||
    (release.assets.length > 0 ? release.assets[0] : null);

  const targetAsset: ReleaseAsset = binaryAsset || {
    id: release.id,
    name: `${release.name || release.tag_name || 'package'}.zip`,
    size: 0,
    download_count: 0,
    content_type: 'application/octet-stream',
    browser_download_url: '',
    created_at: release.published_at || new Date().toISOString(),
    updated_at: release.published_at || new Date().toISOString(),
  };

  return (
    <div className="group relative rounded-2xl bg-slate-900/70 border border-slate-800/80 hover:border-cyan-500/40 p-6 transition-all duration-300 hover:shadow-xl hover:shadow-cyan-950/25 flex flex-col justify-between gap-6">
      {/* Subtle hover gradient */}
      <div className="absolute inset-0 rounded-2xl bg-gradient-to-b from-cyan-500/5 to-transparent opacity-0 group-hover:opacity-100 transition-opacity pointer-events-none" />

      {/* Top Section: Logo Space & App Name */}
      <div className="flex items-center gap-4 relative z-10">
        {/* Logo Space */}
        <div className="w-14 h-14 sm:w-16 sm:h-16 shrink-0 flex items-center justify-center">
          <img
            src={logoUrl}
            alt={release.name || release.tag_name}
            className="w-full h-full object-contain select-none transition-transform duration-300 group-hover:scale-105"
            referrerPolicy="no-referrer"
            onError={(e) => {
              const target = e.currentTarget;
              if (!target.src.includes('cipherlogo1_2.jpeg')) {
                target.src = '/cipherlogo1_2.jpeg';
              }
            }}
          />
        </div>

        {/* Name of the App */}
        <div className="min-w-0 flex-1">
          <h3 className="text-base sm:text-lg font-bold text-white tracking-tight leading-snug group-hover:text-cyan-100 transition-colors">
            {release.name || release.tag_name}
          </h3>
          {release.tag_name && (
            <div className="mt-1">
              <span className="inline-block px-2 py-0.5 rounded-md text-xs font-mono font-medium text-cyan-400 bg-cyan-950/60 border border-cyan-800/40">
                {release.tag_name}
              </span>
            </div>
          )}
        </div>
      </div>

      {/* Bottom Section: Access Request */}
      <div className="relative z-10">
        <button
          onClick={() => onRequestAccess(release, targetAsset)}
          className="w-full py-2.5 sm:py-3 px-4 rounded-xl bg-gradient-to-r from-cyan-600 to-blue-600 hover:from-cyan-500 hover:to-blue-500 text-white text-xs sm:text-sm font-semibold tracking-wide transition-all shadow-md shadow-cyan-950/40 hover:shadow-cyan-900/50 flex items-center justify-center gap-2 cursor-pointer active:scale-[0.99]"
        >
          <KeyRound className="w-4 h-4 shrink-0 text-cyan-200" />
          <span>Request Access</span>
          <ChevronRight className="w-4 h-4 shrink-0 opacity-80 group-hover:translate-x-0.5 transition-transform" />
        </button>
      </div>
    </div>
  );
};

