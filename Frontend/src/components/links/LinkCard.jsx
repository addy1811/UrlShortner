import { Link } from 'react-router';
import Card from '@/components/ui/Card';
import Badge from '@/components/ui/Badge';

export default function LinkCard({ link, onDelete }) {
  function handleCopy() {
    navigator.clipboard.writeText(link.shortUrl);
  }

  return (
    <Card className="flex items-center justify-between gap-4">
      <div className="min-w-0 flex-1">
        <div className="flex items-center gap-3">
          <span className="font-mono-code text-base font-medium text-ink">
            {link.shortCode}
          </span>
          <Badge visibility={link.visibility} />
          {!link.active && (
            <span className="text-xs font-medium text-danger">Inactive</span>
          )}
        </div>
        <p className="mt-1 truncate text-sm text-ink-muted" title={link.destinationUrl}>
          {link.destinationUrl || '(destination hidden)'}
        </p>
        <p className="mt-1 text-xs text-ink-faint">
          {link.useCount} use{link.useCount === 1 ? '' : 's'}
          {link.maxUses ? ` / ${link.maxUses} max` : ''}
          {link.expiresAt ? ` · expires ${new Date(link.expiresAt).toLocaleDateString()}` : ''}
        </p>
      </div>

      <div className="flex shrink-0 items-center gap-2">
        <button
          onClick={handleCopy}
          className="rounded-md border border-border px-3 py-1.5 text-sm text-ink-muted hover:border-ink-faint"
        >
          Copy
        </button>
        <Link
          to={`/links/${link.id}`}
          className="rounded-md border border-border px-3 py-1.5 text-sm text-ink-muted hover:border-ink-faint"
        >
          Manage
        </Link>
        <button
          onClick={() => onDelete(link.id)}
          className="rounded-md px-3 py-1.5 text-sm text-danger hover:bg-danger-light"
        >
          Delete
        </button>
      </div>
    </Card>
  );
}