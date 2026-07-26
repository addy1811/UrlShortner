const VARIANTS = {
  primary: 'bg-signal text-white hover:bg-signal-dark',
  secondary:
    'border border-border text-ink-muted hover:border-ink-faint hover:text-ink',
  danger: 'bg-danger text-white hover:opacity-90',
};

export default function Button({
  variant = 'primary',
  disabled = false,
  type = 'button',
  onClick,
  children,
  className = '',
}) {
  return (
    <button
      type={type}
      disabled={disabled}
      onClick={onClick}
      className={`rounded-md px-4 py-2 text-sm font-medium transition-colors disabled:cursor-not-allowed disabled:opacity-60 ${VARIANTS[variant]} ${className}`}
    >
      {children}
    </button>
  );
}