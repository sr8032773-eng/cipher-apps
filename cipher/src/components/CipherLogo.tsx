import React from 'react';

interface CipherLogoProps {
  className?: string;
  size?: number | string;
  showText?: boolean;
}

export const CipherLogo: React.FC<CipherLogoProps> = ({
  className = '',
  size = 40,
  showText = false,
}) => {
  return (
    <div className={`inline-flex items-center gap-2.5 select-none ${className}`}>
      <svg
        width={size}
        height={size}
        viewBox="0 0 200 200"
        fill="none"
        xmlns="http://www.w3.org/2000/svg"
        className="shrink-0 transition-transform duration-200"
      >
        <defs>
          <linearGradient id="cipherGradient" x1="0%" y1="0%" x2="100%" y2="100%">
            <stop offset="0%" stopColor="#38bdf8" />
            <stop offset="50%" stopColor="#22d3ee" />
            <stop offset="100%" stopColor="#0284c7" />
          </linearGradient>
        </defs>

        {/* Central Cipher Aperture Rings - Box removed */}
        <g transform="translate(100, 100)" stroke="url(#cipherGradient)" strokeLinecap="round">
          {/* Inner Center Circle */}
          <circle cx="0" cy="0" r="14" strokeWidth="10" fill="none" />

          {/* Ring 1 - Segmented Arcs (r = 28) */}
          <path
            d="M -26 -10 A 28 28 0 0 1 10 -26"
            strokeWidth="10"
            fill="none"
          />
          <path
            d="M 26 10 A 28 28 0 0 1 -10 26"
            strokeWidth="10"
            fill="none"
          />

          {/* Ring 2 - Concentric Broken Segments (r = 44) */}
          <path
            d="M -38 -22 A 44 44 0 0 1 42 -14"
            strokeWidth="10.5"
            fill="none"
          />
          <path
            d="M 38 22 A 44 44 0 0 1 -42 14"
            strokeWidth="10.5"
            fill="none"
          />

          {/* Ring 3 - Outer Complex Segments (r = 60) */}
          <path
            d="M -42 -42 A 60 60 0 0 1 54 -26"
            strokeWidth="10.5"
            fill="none"
          />
          <path
            d="M 60 0 A 60 60 0 0 1 -20 56"
            strokeWidth="10.5"
            fill="none"
          />
          <path
            d="M -56 22 A 60 60 0 0 1 -58 -18"
            strokeWidth="10.5"
            fill="none"
          />

          {/* Outer Edge Satellite Arc (r = 76) */}
          <path
            d="M -26 -71 A 76 76 0 0 1 67 -36"
            strokeWidth="9.5"
            fill="none"
          />
          <path
            d="M -70 31 A 76 76 0 0 1 -46 61"
            strokeWidth="9.5"
            fill="none"
          />
        </g>
      </svg>

      {showText && (
        <span className="font-bold tracking-tight text-white font-sans text-lg">
          Cipher
        </span>
      )}
    </div>
  );
};
