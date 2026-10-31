// Placeholder del personaje de marca (la ilustración 3D real vive en Figma /
// el Workspace). Mismo gesto de "flecha ascendente" que el logo, en versión
// amigable para pantallas de onboarding.
export function Mascot({ className = 'h-28 w-28' }: { className?: string }) {
  return (
    <svg viewBox="0 0 160 160" className={className} aria-hidden>
      <ellipse cx="80" cy="150" rx="40" ry="6" fill="#000" opacity="0.08" />
      <path
        d="M70 20c-18 0-28 14-28 30 0 12 6 20 14 26l-2 40c0 4 3 7 7 7h18c4 0 7-3 7-7l-2-40c18-4 28-18 28-36 0-11-16-20-42-20Z"
        fill="#2d9bdf"
      />
      <circle cx="64" cy="48" r="6" fill="#0a1926" />
      <circle cx="88" cy="48" r="6" fill="#0a1926" />
      <path d="M62 64c6 5 14 5 20 0" stroke="#0a1926" strokeWidth={3} strokeLinecap="round" fill="none" />
      <path d="M46 36l-10-10M106 36l10-10" stroke="#14b8a6" strokeWidth={4} strokeLinecap="round" />
    </svg>
  )
}
