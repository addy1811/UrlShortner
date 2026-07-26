import { useEffect, useState } from 'react';
import { useParams, Link } from 'react-router';
import axiosClient from '@/api/axiosClient';

// This page exists specifically to solve one problem: a plain browser
// navigation (typing a URL, clicking a raw link) can NEVER carry a
// Bearer token, since browsers only attach custom headers to requests made
// by page JavaScript (fetch/axios), not to normal navigation. So instead of
// short links pointing straight at the backend's raw 302 redirect (which
// would always look "anonymous" to the server), they point HERE - this page
// runs in the browser, makes an authenticated axios call (which DOES carry
// the JWT), and only then redirects. PUBLIC links resolve instantly either
// way; PRIVATE/RESTRICTED links only work through this page.
export default function RedirectResolverPage() {
  const { code } = useParams();
  const [state, setState] = useState('loading'); // 'loading' | 'error'
  const [errorMessage, setErrorMessage] = useState('');

  useEffect(() => {
    let cancelled = false;

    async function resolve() {
      try {
        const { data } = await axiosClient.get(`/links/resolve/${code}`);
        if (!cancelled) {
          window.location.replace(data.destinationUrl);
        }
      } catch (err) {
        if (cancelled) return;
        setState('error');
        setErrorMessage(err.response?.data?.error || 'This link could not be resolved.');
      }
    }

    resolve();
    return () => { cancelled = true; };
  }, [code]);

  if (state === 'loading') {
    return (
      <div className="mx-auto mt-24 max-w-sm px-6 text-center">
        <p className="text-sm text-ink-muted">Taking you there…</p>
      </div>
    );
  }

  // Distinguish "you need to sign in" from other failures (expired, not found,
  // no access) - only the first case offers a login link, since logging in
  // wouldn't help with the others.
  const needsSignIn = errorMessage.toLowerCase().includes('signed in');

  return (
    <div className="mx-auto mt-24 max-w-sm px-6 text-center">
      <h1 className="mb-2 text-xl">Can&rsquo;t open this link</h1>
      <p className="mb-6 text-sm text-ink-muted">{errorMessage}</p>
      {needsSignIn && (
        <Link
          to="/login"
          state={{ from: `/r/${code}` }}
          className="rounded-md bg-signal px-4 py-2 text-sm font-medium text-white hover:bg-signal-dark"
        >
          Log in
        </Link>
      )}
    </div>
  );
}