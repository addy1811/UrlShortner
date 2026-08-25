import { Link } from 'react-router';

export default function HomePage() {
  return (
    <div className="mx-auto max-w-3xl px-6 py-20">
      <div className="text-center">
        <h1 className="text-4xl leading-tight">
          Short links,
          <br />
          <span className="text-signal">on your terms.</span>
        </h1>
        <p className="mx-auto mt-4 max-w-lg text-ink-muted">
          An encrypted URL shortener where you control exactly who can use each link 
          and can collect structured data from the people who do.
        </p>

        <div className="mt-8 flex justify-center gap-3">
          <Link
            to="/register"
            className="rounded-md bg-signal px-5 py-2.5 text-sm font-medium text-white hover:bg-signal-dark"
          >
            Create a free account
          </Link>
          <Link
            to="/login"
            className="rounded-md border border-border px-5 py-2.5 text-sm font-medium text-ink-muted hover:border-ink-faint hover:text-ink"
          >
            Log in
          </Link>
        </div>
      </div>
    </div>
  );
}