import { useState } from 'react';
import { Link } from 'react-router';
import { useAuth } from '@/hooks/useAuth';
import letterS from '@/assets/letter-s.png';

export default function Navbar() {
  const { isAuthenticated, username, logout } = useAuth();
  const [logoFailed, setLogoFailed] = useState(false);

  return (
    <nav className="border-b border-border bg-surface-raised">
      <div className="mx-auto flex max-w-5xl items-center justify-between px-6 py-4">
        <Link to="/" className="font-display text-lg font-semibold text-ink">
          {logoFailed ? (
            <span className="font-mono-code">S</span>
          ) : (
            <img
              src={letterS}
              alt="Logo"
              width={30}
              height={30}
              onError={() => setLogoFailed(true)}
            />
          )}
        </Link>

        <div className="flex items-center gap-4 text-sm">
          {isAuthenticated ? (
            <>
              <Link to="/dashboard" className="text-ink-muted hover:text-ink">
                Dashboard
              </Link>
              <span className="text-ink-faint">{username}</span>
              <button
                onClick={logout}
                className="rounded-md border border-border px-3 py-1.5 text-ink-muted hover:border-ink-faint"
              >
                Log out
              </button>
            </>
          ) : (
            <>
              <Link to="/login" className="text-ink-muted hover:text-ink">
                Log in
              </Link>
              <Link
                to="/register"
                className="rounded-md bg-signal px-3 py-1.5 text-white hover:bg-signal-dark"
              >
                Sign up
              </Link>
            </>
          )}
        </div>
      </div>
    </nav>
  );
}