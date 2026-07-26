import { useState } from 'react';
import Input from '@/components/ui/Input';
import Button from '@/components/ui/Button';

const DEFAULT_FORM = {
  destinationUrl: '',
  visibility: 'PRIVATE',
  customAlias: '',
  expiresAt: '',
  maxUses: '',
};

// Matches CreateLinkRequest: only destinationUrl is required, everything else
// is the "user picks what to include" optional configuration.
export default function CreateLinkForm({ onCreate }) {
  const [showAdvanced, setShowAdvanced] = useState(false);
  const [form, setForm] = useState(DEFAULT_FORM);
  const [error, setError] = useState(null);
  const [isSubmitting, setIsSubmitting] = useState(false);

  function update(field, value) {
    setForm((prev) => ({ ...prev, [field]: value }));
  }

  async function handleSubmit(e) {
    e.preventDefault();
    setError(null);
    setIsSubmitting(true);

    try {
      await onCreate({
        destinationUrl: form.destinationUrl,
        visibility: form.visibility,
        customAlias: form.customAlias || undefined,
        expiresAt: form.expiresAt ? new Date(form.expiresAt).toISOString() : undefined,
        maxUses: form.maxUses ? Number(form.maxUses) : undefined,
      });
      setForm(DEFAULT_FORM);
      setShowAdvanced(false);
    } catch (err) {
      // onCreate (DashboardPage) dispatches createLinkThunk, which uses
      // rejectWithValue - so unwrap() throws the backend's response data
      // directly here, not a full axios error with .response.
      const fieldErrors = err?.fieldErrors;
      setError(fieldErrors ? Object.values(fieldErrors)[0] : err?.error || 'Failed to create link');
    } finally {
      setIsSubmitting(false);
    }
  }

  return (
    <form onSubmit={handleSubmit} className="flex flex-col gap-4">
      <Input
        label="Destination URL"
        type="url"
        placeholder="https://example.com/your-long-url"
        value={form.destinationUrl}
        onChange={(e) => update('destinationUrl', e.target.value)}
        required
      />

      <div>
        <label className="mb-1 block text-sm font-medium">Visibility</label>
        <select
          value={form.visibility}
          onChange={(e) => update('visibility', e.target.value)}
          className="w-full rounded-md border border-border px-3 py-2 outline-none focus:border-signal"
        >
          <option value="PRIVATE">Private — only you</option>
          <option value="PUBLIC">Public — anyone with the link</option>
          <option value="RESTRICTED">Restricted — only people you grant access to</option>
        </select>
      </div>

      <button
        type="button"
        onClick={() => setShowAdvanced((v) => !v)}
        className="self-start text-sm text-signal hover:underline"
      >
        {showAdvanced ? 'Hide' : 'Show'} advanced options
      </button>

      {showAdvanced && (
        <div className="flex flex-col gap-4 border-l-2 border-border pl-4">
          <Input
            label="Custom alias (optional)"
            placeholder="my-portfolio"
            value={form.customAlias}
            onChange={(e) => update('customAlias', e.target.value)}
            hint="3-50 letters, numbers, or hyphens"
          />
          <Input
            label="Expires at (optional)"
            type="datetime-local"
            value={form.expiresAt}
            onChange={(e) => update('expiresAt', e.target.value)}
          />
          <Input
            label="Max uses (optional)"
            type="number"
            min="1"
            placeholder="Unlimited"
            value={form.maxUses}
            onChange={(e) => update('maxUses', e.target.value)}
          />
        </div>
      )}

      {error && <p className="text-sm text-danger">{error}</p>}

      <Button type="submit" disabled={isSubmitting}>
        {isSubmitting ? 'Creating…' : 'Create link'}
      </Button>
    </form>
  );
}