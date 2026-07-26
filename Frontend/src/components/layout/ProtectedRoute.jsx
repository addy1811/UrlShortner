import { useSelector } from 'react-redux';
import { Navigate, Outlet, useLocation } from 'react-router';

// Wraps any route that requires a logged-in user. Unlike the backend (which
// rejects at the request level), this only gates the UI - a determined user
// could still hit the API directly, but SecurityConfig's anyRequest().authenticated()
// is the actual enforcement boundary. This component just avoids showing a
// broken/empty authenticated page to someone who isn't logged in.
export default function ProtectedRoute() {
  const isAuthenticated = useSelector((state) => state.auth.isAuthenticated);
  const location = useLocation();

  if (!isAuthenticated) {
    return <Navigate to="/login" state={{ from: location }} replace />;
  }

  return <Outlet />;
}
