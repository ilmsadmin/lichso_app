"use client";

export function LogoSeal({ className = "w-14 h-14" }: { className?: string }) {
  return (
    <svg
      className={`${className} cursor-pointer drop-shadow-lg transition-all duration-300 hover:scale-105 hover:rotate-[15deg] hover:drop-shadow-xl`}
      viewBox="0 0 120 120"
      xmlns="http://www.w3.org/2000/svg"
    >
      <defs>
        <linearGradient id="ringGrad" x1="0%" y1="0%" x2="100%" y2="100%">
          <stop offset="0%" stopColor="#E8B86D" />
          <stop offset="35%" stopColor="#C4783A" />
          <stop offset="65%" stopColor="#D4956A" />
          <stop offset="100%" stopColor="#B8602A" />
        </linearGradient>
        <radialGradient id="discGrad" cx="40%" cy="35%" r="65%">
          <stop offset="0%" stopColor="#6BAAA8" />
          <stop offset="45%" stopColor="#3D7B7A" />
          <stop offset="100%" stopColor="#2A5A5C" />
        </radialGradient>
        <linearGradient id="spiralGrad" x1="0%" y1="0%" x2="100%" y2="100%">
          <stop offset="0%" stopColor="#E8A84A" />
          <stop offset="50%" stopColor="#D06030" />
          <stop offset="100%" stopColor="#C44A28" />
        </linearGradient>
        <linearGradient id="mazeGrad" x1="0%" y1="0%" x2="100%" y2="100%">
          <stop offset="0%" stopColor="#E8C070" />
          <stop offset="100%" stopColor="#C4783A" />
        </linearGradient>
        <filter id="glow" x="-20%" y="-20%" width="140%" height="140%">
          <feGaussianBlur stdDeviation="1.5" result="blur" />
          <feMerge>
            <feMergeNode in="blur" />
            <feMergeNode in="SourceGraphic" />
          </feMerge>
        </filter>
        <filter id="softGlow" x="-30%" y="-30%" width="160%" height="160%">
          <feGaussianBlur stdDeviation="2.5" result="blur" />
          <feMerge>
            <feMergeNode in="blur" />
            <feMergeNode in="SourceGraphic" />
          </feMerge>
        </filter>
        <clipPath id="circleClip">
          <circle cx="60" cy="60" r="52" />
        </clipPath>
      </defs>

      <circle cx="60" cy="60" r="58" fill="url(#ringGrad)" />
      <circle
        cx="60"
        cy="60"
        r="58"
        fill="none"
        stroke="rgba(255,230,160,0.45)"
        strokeWidth="1.5"
        strokeDasharray="110 260"
        strokeDashoffset="-30"
      />

      <circle cx="60" cy="60" r="52" fill="url(#discGrad)" />
      <circle
        cx="60"
        cy="60"
        r="49"
        fill="none"
        stroke="rgba(255,255,255,0.06)"
        strokeWidth="0.8"
      />
      <circle
        cx="60"
        cy="60"
        r="44"
        fill="none"
        stroke="rgba(255,255,255,0.05)"
        strokeWidth="0.5"
      />

      <g clipPath="url(#circleClip)">
        <circle
          cx="60"
          cy="60"
          r="40"
          fill="none"
          stroke="url(#mazeGrad)"
          strokeWidth="10"
          opacity="0.22"
        />
        <path
          d="M 60 20 A 40 40 0 0 1 95 45"
          fill="none"
          stroke="url(#mazeGrad)"
          strokeWidth="9"
          opacity="0.75"
        />
        <path
          d="M 95 45 A 40 40 0 0 1 88 82"
          fill="none"
          stroke="url(#mazeGrad)"
          strokeWidth="9"
          opacity="0.55"
        />
        <path
          d="M 60 20 A 40 40 0 0 0 25 45"
          fill="none"
          stroke="url(#mazeGrad)"
          strokeWidth="9"
          opacity="0.65"
        />
        <path
          d="M 25 45 A 40 40 0 0 0 32 82"
          fill="none"
          stroke="url(#mazeGrad)"
          strokeWidth="9"
          opacity="0.45"
        />
        <path
          d="M 32 82 A 40 40 0 0 0 88 82"
          fill="none"
          stroke="url(#mazeGrad)"
          strokeWidth="9"
          opacity="0.6"
        />
        <circle cx="60" cy="20" r="5" fill="url(#discGrad)" />
        <circle cx="95" cy="45" r="4.5" fill="url(#discGrad)" />
        <circle cx="32" cy="82" r="4.5" fill="url(#discGrad)" />
      </g>

      <circle cx="60" cy="60" r="28" fill="rgba(42,90,92,0.6)" />
      <circle cx="60" cy="60" r="28" fill="none" stroke="rgba(232,180,100,0.4)" strokeWidth="1" />

      <g filter="url(#glow)">
        <path
          d="M 60 60 C 60 46, 72 40, 78 48 C 84 56, 80 70, 70 74 C 60 78, 46 72, 44 62 C 42 52, 50 44, 58 44 C 66 44, 72 50, 72 58 C 72 66, 66 70, 60 70 C 54 70, 50 66, 50 60 C 50 54, 54 50, 60 50 C 64 50, 67 53, 67 57"
          fill="none"
          stroke="url(#spiralGrad)"
          strokeWidth="3.5"
          strokeLinecap="round"
        />
        <path
          d="M 60 60 C 57 57, 55 54, 57 52 C 59 50, 63 51, 64 54 C 65 57, 63 60, 60 61"
          fill="none"
          stroke="#E8A84A"
          strokeWidth="2.5"
          strokeLinecap="round"
        />
      </g>

      <circle cx="60" cy="60" r="3" fill="#E8B050" filter="url(#glow)" />
      <circle cx="60" cy="60" r="1.5" fill="#FFF0C0" />

      <g filter="url(#softGlow)" opacity="0.92">
        <path
          d="M 71 36 C 68 33, 64 33, 62 36 C 66 36, 69 39, 69 43 C 72 41, 74 38, 71 36 Z"
          fill="#F0D070"
        />
      </g>

      <g filter="url(#softGlow)" opacity="0.95">
        <path
          d="M 78 30 L 79.2 27 L 80.4 30 L 83.4 31.2 L 80.4 32.4 L 79.2 35.4 L 78 32.4 L 75 31.2 Z"
          fill="#FADF70"
        />
        <circle cx="79.2" cy="31.2" r="1" fill="#FFF8D0" />
      </g>

      <circle cx="74" cy="42" r="1.4" fill="#E8D070" opacity="0.8" />
      <circle cx="78" cy="48" r="1.1" fill="#D4B060" opacity="0.65" />
      <circle cx="70" cy="33" r="0.9" fill="#F0E080" opacity="0.6" />

      <circle cx="60" cy="60" r="52" fill="none" stroke="rgba(255,220,140,0.25)" strokeWidth="2" />
    </svg>
  );
}
