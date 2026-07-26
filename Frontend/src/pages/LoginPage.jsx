import { useState } from 'react';
import { Link } from 'react-router';
import { useAuth } from '@/hooks/useAuth';

export default function LoginPage() {
  const { login } = useAuth();
  const [usernameOrEmail, setUsernameOrEmail] = useState('');
  const [password, setPassword] = useState('');
  const [error, setError] = useState(null);
  const [isSubmitting, setIsSubmitting] = useState(false);

  async function handleSubmit(e) {
    e.preventDefault();
    setError(null);
    setIsSubmitting(true);
    try {
      await login({ usernameOrEmail, password });
    } catch (err) {
      // Backend's GlobalExceptionHandler returns a generic message for bad
      // credentials specifically to avoid confirming which part was wrong.
      setError(err.response?.data?.error || 'Something went wrong. Try again.');
    } finally {
      setIsSubmitting(false);
    }
  }

  return (
    <div className="mx-auto mt-16 max-w-sm px-6">
      <h1 className="mb-1 text-2xl">Log in</h1>
      <p className="mb-8 text-sm text-ink-muted">
        Access your links and their access-control settings.
      </p>

      <form onSubmit={handleSubmit} className="flex flex-col gap-4">
        <div>
          <label className="mb-1 block text-sm font-medium">Username or email</label>
          <input
            type="text"
            value={usernameOrEmail}
            onChange={(e) => setUsernameOrEmail(e.target.value)}
            required
            className="w-full rounded-md border border-border px-3 py-2 outline-none focus:border-signal"
          />
        </div>

        <div>
          <label className="mb-1 block text-sm font-medium">Password</label>
          <input
            type="password"
            value={password}
            onChange={(e) => setPassword(e.target.value)}
            required
            className="w-full rounded-md border border-border px-3 py-2 outline-none focus:border-signal"
          />
        </div>

        {error && <p className="text-sm text-danger">{error}</p>}

        <button
          type="submit"
          disabled={isSubmitting}
          className="mt-2 rounded-md bg-signal py-2 text-white hover:bg-signal-dark disabled:opacity-60"
        >
          {isSubmitting ? 'Logging in…' : 'Log in'}
        </button>
      </form>

      <p className="mt-6 text-sm text-ink-muted">
        Don&rsquo;t have an account?{' '}
        <Link to="/register" className="text-signal hover:underline">
          Sign up
        </Link>
      </p>
    </div>
  );
}