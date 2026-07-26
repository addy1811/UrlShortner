// Maps directly to the backend's Visibility enum (PUBLIC/PRIVATE/RESTRICTED).
// This dot is real information, not decoration - it's the same state that
// AccessControlService enforces on every redirect/form request.
const VISIBILITY_STYLES = {
  PUBLIC: { dot: 'bg-signal', text: 'text-signal-dark', label: 'Public' },
  PRIVATE: { dot: 'bg-ink-faint', text: 'text-ink-muted', label: 'Private' },
  RESTRICTED: { dot: 'bg-restricted', text: 'text-restricted', label: 'Restricted' },
};

export default function Badge({ visibility }) {
  const style = VISIBILITY_STYLES[visibility] ?? VISIBILITY_STYLES.PRIVATE;

  return (
    <span className={`inline-flex items-center gap-1.5 text-xs font-medium ${style.text}`}>
      <span className={`h-1.5 w-1.5 rounded-full ${style.dot}`} />
      {style.label}
    </span>
  );
}