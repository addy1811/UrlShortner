import { useSelector, useDispatch } from 'react-redux';
import { useNavigate } from 'react-router';
import * as authApi from '@/api/authApi';
import { setCredentials, logout as logoutAction } from '@/store/authSlice';

export function useAuth() {
  const dispatch = useDispatch();
  const navigate = useNavigate();
  const { userId, username, isAuthenticated } = useSelector((state) => state.auth);

  async function login({ usernameOrEmail, password }) {
    const data = await authApi.login({ usernameOrEmail, password });
    dispatch(setCredentials(data));
    navigate('/dashboard');
  }

  async function register({ username: newUsername, email, password }) {
    const data = await authApi.register({ username: newUsername, email, password });
    dispatch(setCredentials(data));
    navigate('/dashboard');
  }

  function logout() {
    dispatch(logoutAction());
    navigate('/login');
  }

  return { userId, username, isAuthenticated, login, register, logout };
}
