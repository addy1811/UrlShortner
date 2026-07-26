import { useState } from 'react';
import { Link } from 'react-router';
import { useAuth } from '@/hooks/useAuth';

export default function RegisterPage() {
  const { register } = useAuth();
  const [username, setUsername] = useState('');
  const [email, setEmail] = useState('');
  const [password, setPassword] = useState('');
  const [error, setError] = useState(null);
  const [isSubmitting, setIsSubmitting] = useState(false);

  async function handleSubmit(e) {
    e.preventDefault();
    setError(null);
    setIsSubmitting(true);
    try {
      await register({ username, email, password });
    } catch (err) {
      // Field-level validation errors come back as { error, fieldErrors: {...} }
      // from GlobalExceptionHandler's MethodArgumentNotValidException handler.
      const fieldErrors = err.response?.data?.fieldErrors;
      if (fieldErrors) {
        setError(Object.values(fieldErrors)[0]);
      } else {
        setError(err.response?.data?.error || 'Something went wrong. Try again.');
      }
    } finally {
      setIsSubmitting(false);
    }
  }

  return (
    <div className="mx-auto mt-16 max-w-sm px-6">
      <h1 className="mb-1 text-2xl">Create your account</h1>
      <p className="mb-8 text-sm text-ink-muted">
        Start creating encrypted, access-controlled links.
      </p>

      <form onSubmit={handleSubmit} className="flex flex-col gap-4">
        <div>
          <label className="mb-1 block text-sm font-medium">Username</label>
          <input
            type="text"
            value={username}
            onChange={(e) => setUsername(e.target.value)}
            required
            minLength={3}
            maxLength={50}
            pattern="[a-zA-Z0-9_]+"
            title="Letters, numbers, and underscores only"
            className="w-full rounded-md border border-border px-3 py-2 outline-none focus:border-signal"
          />
        </div>

        <div>
          <label className="mb-1 block text-sm font-medium">Email</label>
          <input
            type="email"
            value={email}
            onChange={(e) => setEmail(e.target.value)}
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
            minLength={8}
            title="At least 8 characters, with one uppercase, one lowercase, and one digit"
            className="w-full rounded-md border border-border px-3 py-2 outline-none focus:border-signal"
          />
          <p className="mt-1 text-xs text-ink-faint">
            At least 8 characters, with one uppercase, one lowercase, and one digit.
          </p>
        </div>

        {error && <p className="text-sm text-danger">{error}</p>}

        <button
          type="submit"
          disabled={isSubmitting}
          className="mt-2 rounded-md bg-signal py-2 text-white hover:bg-signal-dark disabled:opacity-60"
        >
          {isSubmitting ? 'Creating account…' : 'Create account'}
        </button>
      </form>

      <p className="mt-6 text-sm text-ink-muted">
        Already have an account?{' '}
        <Link to="/login" className="text-signal hover:underline">
          Log in
        </Link>
      </p>
    </div>
  );
}