export interface ReleaseAsset {
  id: number;
  name: string;
  size: number;
  download_count: number;
  content_type: string;
  browser_download_url: string;
  created_at: string;
  updated_at: string;
}

export interface GitHubRelease {
  id: number;
  name: string;
  tag_name: string;
  body: string;
  html_url: string;
  published_at: string;
  prerelease: boolean;
  draft: boolean;
  assets: ReleaseAsset[];
}

export interface Asset {
  id: string | number;
  name: string;
  description: string;
  version?: string;
  category?: string;
  size?: string;
}

export interface AccessRequest {
  id: string;
  name?: string;
  email: string;
  assetId: number | string;
  releaseName: string;
  assetName: string;
  status: 'pending' | 'approved' | 'rejected';
  createdAt?: string;
  approvedAt?: string;
  approvedToken?: string;
}

export interface ActiveTokenPayload {
  email: string;
  assetId: number;
  releaseName?: string;
  assetName?: string;
  createdAt: string;
  expiresAt: string;
}

export interface TokenRecord {
  token: string;
  email: string;
  expiresAt: string;
  assetId?: number | string;
  releaseName?: string;
  assetName?: string;
  createdAt?: string;
  ttlSeconds?: number;
}

export interface StoreSyncStatus {
  connected: boolean;
  owner: string;
  repo: string;
  hasToken: boolean;
  isMockFallback: boolean;
  releasesCount: number;
  lastSyncedAt: string;
  errorMessage?: string;
}
