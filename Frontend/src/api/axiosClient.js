import axios from 'axios';

// Vite's dev proxy (vite.config.js) forwards /api to the backend, so this can
// stay a relative path in both dev and prod.
const axiosClient = axios.create({
  baseURL: '/api',
  headers: {
    'Content-Type': 'application/json',
  },
});

// Attaches the access token to every outgoing request, if one exists.
axiosClient.interceptors.request.use((config) => {
  const token = localStorage.getItem('accessToken');
  if (token) {
    config.headers.Authorization = `Bearer ${token}`;
  }
  return config;
});

// NOTE: the backend's AuthController only exposes /register and /login -
// there's no /auth/refresh endpoint yet, so a 401 here can't silently retry
// with a fresh token. Instead, clear the stale token and send the user back
// to log in again. If a refresh endpoint gets added to AuthService/AuthController
// later, this is the place to add the retry-with-refresh-token logic.
axiosClient.interceptors.response.use(
  (response) => response,
  (error) => {
    if (error.response?.status === 401) {
      localStorage.removeItem('accessToken');
      localStorage.removeItem('refreshToken');
      if (window.location.pathname !== '/login') {
        window.location.href = '/login';
      }
    }
    return Promise.reject(error);
  }
);

export default axiosClient;
