import { useState } from 'react';
import Input from '@/components/ui/Input';
import Button from '@/components/ui/Button';

// Mirrors GrantAccessRequest's isValid(): exactly one of username/email,
// enforced here client-side via a toggle rather than showing both fields at once.
export default function GrantAccessModal({ onGrant, onClose }) {
  const [mode, setMode] = useState('username'); // 'username' | 'email'
  const [value, setValue] = useState('');
  const [error, setError] = useState(null);
  const [isSubmitting, setIsSubmitting] = useState(false);

  async function handleSubmit(e) {
    e.preventDefault();
    setError(null);
    setIsSubmitting(true);

    try {
      await onGrant(mode === 'username' ? { username: value } : { email: value });
      onClose();
    } catch (err) {
      setError(err.response?.data?.error || 'Failed to grant access');
    } finally {
      setIsSubmitting(false);
    }
  }

  return (
    <div className="fixed inset-0 flex items-center justify-center bg-black/30 px-4">
      <div className="w-full max-w-sm rounded-lg bg-surface-raised p-6 shadow-lg">
        <h2 className="mb-1 text-lg">Grant access</h2>
        <p className="mb-4 text-sm text-ink-muted">
          Give a specific person access to this restricted link.
        </p>

        <form onSubmit={handleSubmit} className="flex flex-col gap-4">
          <div className="flex gap-2 text-sm">
            <button
              type="button"
              onClick={() => { setMode('username'); setValue(''); }}
              className={`rounded-md px-3 py-1 ${mode === 'username' ? 'bg-signal-light text-signal-dark' : 'text-ink-muted'}`}
            >
              By username
            </button>
            <button
              type="button"
              onClick={() => { setMode('email'); setValue(''); }}
              className={`rounded-md px-3 py-1 ${mode === 'email' ? 'bg-signal-light text-signal-dark' : 'text-ink-muted'}`}
            >
              By email (invite)
            </button>
          </div>

          <Input
            label={mode === 'username' ? 'Username' : 'Email'}
            type={mode === 'email' ? 'email' : 'text'}
            value={value}
            onChange={(e) => setValue(e.target.value)}
            required
            hint={mode === 'email' ? "If they don't have an account yet, the grant stays pending until they sign up." : undefined}
          />

          {error && <p className="text-sm text-danger">{error}</p>}

          <div className="flex justify-end gap-2">
            <Button variant="secondary" onClick={onClose} type="button">
              Cancel
            </Button>
            <Button type="submit" disabled={isSubmitting}>
              {isSubmitting ? 'Granting…' : 'Grant access'}
            </Button>
          </div>
        </form>
      </div>
    </div>
  );
}