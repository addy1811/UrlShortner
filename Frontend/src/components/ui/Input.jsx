export default function Input({
  label,
  type = 'text',
  value,
  onChange,
  required = false,
  placeholder,
  error,
  hint,
  ...rest
}) {
  return (
    <div>
      {label && <label className="mb-1 block text-sm font-medium">{label}</label>}
      <input
        type={type}
        value={value}
        onChange={onChange}
        required={required}
        placeholder={placeholder}
        className={`w-full rounded-md border px-3 py-2 outline-none transition-colors ${
          error
            ? 'border-danger focus:border-danger'
            : 'border-border focus:border-signal'
        }`}
        {...rest}
      />
      {hint && !error && <p className="mt-1 text-xs text-ink-faint">{hint}</p>}
      {error && <p className="mt-1 text-xs text-danger">{error}</p>}
    </div>
  );
}