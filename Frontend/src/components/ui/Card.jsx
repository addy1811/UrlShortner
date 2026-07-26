export default function Card({ children, className = '' }) {
  return (
    <div
      className={`rounded-lg border border-border bg-surface-raised p-5 ${className}`}
    >
      {children}
    </div>
  );
}