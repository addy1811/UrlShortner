import { StrictMode } from 'react';
import { createRoot } from 'react-dom/client';
import { Provider } from 'react-redux';
import { BrowserRouter } from 'react-router';
import { store } from '@/store/store';
import { setCredentials, logout } from '@/store/authSlice';
import App from './App.jsx';
import './index.css';

window.addEventListener('pageshow', (event) => {
  if (event.persisted) {
    const accessToken = localStorage.getItem('accessToken');
    const refreshToken = localStorage.getItem('refreshToken');
    const userId = localStorage.getItem('userId');
    const username = localStorage.getItem('username');

    if (accessToken) {
      store.dispatch(setCredentials({ accessToken, refreshToken, userId, username }));
    } else {
      store.dispatch(logout());
    }
  }
});
createRoot(document.getElementById('root')).render(
  <StrictMode>
    <Provider store={store}>
      <BrowserRouter>
        <App />
      </BrowserRouter>
    </Provider>
  </StrictMode>
);
