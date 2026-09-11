import React, { useState } from 'react';
import { X, Mail, ShieldAlert, CheckCircle2, Lock, ArrowRight, Loader2, User } from 'lucide-react';
import { GitHubRelease, ReleaseAsset } from '../types';

interface RequestAccessModalProps {
  isOpen: boolean;
  onClose: () => void;
  release: GitHubRelease | null;
  selectedAsset: ReleaseAsset | null;
  onSuccessSubmitted?: () => void;
}

export const RequestAccessModal: React.FC<RequestAccessModalProps> = ({
  isOpen,
  onClose,
  release,
  selectedAsset,
  onSuccessSubmitted,
}) => {
  const [name, setName] = useState('');
  const [email, setEmail] = useState('');
  const [isSubmitting, setIsSubmitting] = useState(false);
  const [errorMessage, setErrorMessage] = useState('');
  const [submittedData, setSubmittedData] = useState<{
    requestId: string;
    name?: string;
    email: string;
    releaseName: string;
  } | null>(null);

  if (!isOpen || !release) return null;

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!name.trim()) {
      setErrorMessage('Please enter your name.');
      return;
    }
    if (!email || !email.includes('@')) {
      setErrorMessage('Please enter a valid work or personal email address.');
      return;
    }

    setIsSubmitting(true);
    setErrorMessage('');

    try {
      const response = await fetch('/api/request-access', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({
          name: name.trim(),
          email: email.trim(),
          releaseName: release.name || release.tag_name,
          assetId: selectedAsset ? selectedAsset.id : release.id,
          assetName: selectedAsset?.name,
        }),
      });

      const data = await response.json();

      if (!response.ok || !data.success) {
        throw new Error(data.error || 'Failed to submit access request');
      }

      setSubmittedData({
        requestId: data.requestId,
        name: name.trim(),
        email: email.trim(),
        releaseName: release.name || release.tag_name,
      });

      if (onSuccessSubmitted) {
        onSuccessSubmitted();
      }
    } catch (err: any) {
      setErrorMessage(err.message || 'Network error occurred while submitting request');
    } finally {
      setIsSubmitting(false);
    }
  };

  const handleResetAndClose = () => {
    setName('');
    setEmail('');
    setSubmittedData(null);
    setErrorMessage('');
    onClose();
  };

  return (
    <div className="fixed inset-0 z-50 flex items-center justify-center p-4 bg-slate-950/80 backdrop-blur-md animate-in fade-in duration-200">
      <div
        className="relative w-full max-w-lg bg-slate-900 border border-slate-800 rounded-2xl shadow-2xl p-6 sm:p-8 overflow-hidden"
        onClick={(e) => e.stopPropagation()}
      >
        {/* Decorative corner glow */}
        <div className="absolute -top-20 -right-20 w-40 h-40 bg-cyan-500/10 rounded-full blur-3xl pointer-events-none" />

        {/* Close Button */}
        <button
          onClick={handleResetAndClose}
          className="absolute top-5 right-5 p-2 rounded-lg text-slate-400 hover:text-white hover:bg-slate-800 transition-colors"
        >
          <X className="w-5 h-5" />
        </button>

        {!submittedData ? (
          <div>
            {/* Modal Title */}
            <div className="flex items-center gap-3 mb-4">
              <div className="w-10 h-10 rounded-xl bg-cyan-950/80 border border-cyan-800/60 flex items-center justify-center text-cyan-400">
                <Lock className="w-5 h-5" />
              </div>
              <div>
                <h3 className="text-lg font-bold text-white tracking-tight">
                  Request Application Access
                </h3>
                <p className="text-xs text-slate-400">
                  Zero-Trust Distribution Gatekeeper
                </p>
              </div>
            </div>

            {/* Target App Details Box */}
            <div className="bg-slate-950/70 border border-slate-800/80 rounded-xl p-3.5 mb-5 space-y-1.5 font-mono text-xs">
              <div className="flex justify-between text-slate-400">
                <span>Target Application:</span>
                <span className="text-white font-semibold">{release.name || release.tag_name}</span>
              </div>
              <div className="flex justify-between text-slate-400">
                <span>Version Tag:</span>
                <span className="text-cyan-400">{release.tag_name}</span>
              </div>
              {selectedAsset && (
                <div className="flex justify-between text-slate-400">
                  <span>Selected Binary:</span>
                  <span className="text-slate-200 truncate max-w-[200px]">{selectedAsset.name}</span>
                </div>
              )}
              {selectedAsset && (
                <div className="flex justify-between text-slate-400">
                  <span>Asset ID:</span>
                  <span className="text-slate-300 font-mono bg-slate-900 px-1.5 py-0.2 rounded">
                    {selectedAsset.id}
                  </span>
                </div>
              )}
            </div>

            {/* Security Notice */}
            <div className="flex items-start gap-2.5 p-3 rounded-xl bg-slate-950/40 border border-slate-800/60 mb-5 text-xs text-slate-400">
              <ShieldAlert className="w-4 h-4 text-cyan-400 shrink-0 mt-0.5" />
              <p>
                Access requests are cryptographically verified by repository administrators. Upon approval, an ephemeral one-time download link (valid for 24 hours) will be authorized.
              </p>
            </div>

            {/* Form */}
            <form onSubmit={handleSubmit} className="space-y-4">
              <div>
                <label className="block text-xs font-semibold uppercase tracking-wider text-slate-300 mb-1.5">
                  Full Name
                </label>
                <div className="relative">
                  <div className="absolute inset-y-0 left-0 pl-3.5 flex items-center pointer-events-none text-slate-500">
                    <User className="w-4 h-4" />
                  </div>
                  <input
                    type="text"
                    required
                    value={name}
                    onChange={(e) => setName(e.target.value)}
                    placeholder="Jane Doe"
                    className="w-full pl-10 pr-4 py-2.5 bg-slate-950 border border-slate-800 rounded-xl text-sm text-white placeholder-slate-500 focus:outline-none focus:border-cyan-500 focus:ring-1 focus:ring-cyan-500 transition-all font-sans"
                  />
                </div>
              </div>

              <div>
                <label className="block text-xs font-semibold uppercase tracking-wider text-slate-300 mb-1.5">
                  EMAIL ID
                </label>
                <div className="relative">
                  <div className="absolute inset-y-0 left-0 pl-3.5 flex items-center pointer-events-none text-slate-500">
                    <Mail className="w-4 h-4" />
                  </div>
                  <input
                    type="email"
                    required
                    value={email}
                    onChange={(e) => setEmail(e.target.value)}
                    placeholder="developer@company.com"
                    className="w-full pl-10 pr-4 py-2.5 bg-slate-950 border border-slate-800 rounded-xl text-sm text-white placeholder-slate-500 focus:outline-none focus:border-cyan-500 focus:ring-1 focus:ring-cyan-500 transition-all font-mono"
                  />
                </div>
              </div>

              {errorMessage && (
                <div className="p-3 rounded-xl bg-rose-950/60 border border-rose-900/50 text-rose-300 text-xs">
                  {errorMessage}
                </div>
              )}

              <div className="flex items-center justify-end gap-3 pt-2">
                <button
                  type="button"
                  onClick={handleResetAndClose}
                  className="px-4 py-2.5 rounded-xl bg-slate-800 hover:bg-slate-700/80 text-slate-300 text-xs font-medium transition-colors"
                >
                  Cancel
                </button>
                <button
                  type="submit"
                  disabled={isSubmitting}
                  className="px-5 py-2.5 rounded-xl bg-gradient-to-r from-cyan-600 to-blue-600 hover:from-cyan-500 hover:to-blue-500 disabled:opacity-50 text-white text-xs font-semibold tracking-wide transition-all shadow-md shadow-cyan-950/50 flex items-center gap-2"
                >
                  {isSubmitting ? (
                    <>
                      <Loader2 className="w-4 h-4 animate-spin" />
                      Submitting Request...
                    </>
                  ) : (
                    <>
                      Submit Access Request
                      <ArrowRight className="w-4 h-4" />
                    </>
                  )}
                </button>
              </div>
            </form>
          </div>
        ) : (
          /* Success Screen */
          <div className="text-center py-4 space-y-4">
            <div className="w-16 h-16 rounded-2xl bg-emerald-950/80 border border-emerald-800/60 text-emerald-400 flex items-center justify-center mx-auto shadow-lg shadow-emerald-950/40">
              <CheckCircle2 className="w-8 h-8" />
            </div>

            <div>
              <h3 className="text-xl font-bold text-white tracking-tight">
                Request Submitted Successfully
              </h3>
              <p className="text-xs text-slate-400 mt-1 max-w-sm mx-auto">
                Your authorization request has been queued in the Cipher Vercel KV store for administrator approval.
              </p>
            </div>

            <div className="bg-slate-950 border border-slate-800/80 rounded-xl p-4 text-xs font-mono text-left space-y-1.5 text-slate-400">
              <div className="flex justify-between">
                <span>Request ID:</span>
                <span className="text-cyan-400">{submittedData.requestId}</span>
              </div>
              {submittedData.name && (
                <div className="flex justify-between">
                  <span>Requester:</span>
                  <span className="text-slate-200">{submittedData.name}</span>
                </div>
              )}
              <div className="flex justify-between">
                <span>Recipient:</span>
                <span className="text-slate-200">{submittedData.email}</span>
              </div>
              <div className="flex justify-between">
                <span>Application:</span>
                <span className="text-slate-200 truncate max-w-[200px]">{submittedData.releaseName}</span>
              </div>
              <div className="flex justify-between">
                <span>Status:</span>
                <span className="text-amber-400 uppercase font-semibold">Pending Admin Review</span>
              </div>
            </div>

            <div className="pt-2">
              <button
                onClick={handleResetAndClose}
                className="w-full py-2.5 px-4 rounded-xl bg-slate-800 hover:bg-slate-700/80 text-white text-xs font-semibold transition-colors"
              >
                Done
              </button>
            </div>
          </div>
        )}
      </div>
    </div>
  );
};
