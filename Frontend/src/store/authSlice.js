import { createSlice } from '@reduxjs/toolkit';

const initialState = {
  userId: localStorage.getItem('userId') || null,
  username: localStorage.getItem('username') || null,
  accessToken: localStorage.getItem('accessToken') || null,
  isAuthenticated: Boolean(localStorage.getItem('accessToken')),
};

const authSlice = createSlice({
  name: 'auth',
  initialState,
  reducers: {
    setCredentials(state, action) {
      const { userId, username, accessToken, refreshToken } = action.payload;
      state.userId = userId;
      state.username = username;
      state.accessToken = accessToken;
      state.isAuthenticated = true;

      localStorage.setItem('userId', userId);
      localStorage.setItem('username', username);
      localStorage.setItem('accessToken', accessToken);
      localStorage.setItem('refreshToken', refreshToken);
    },
    logout(state) {
      state.userId = null;
      state.username = null;
      state.accessToken = null;
      state.isAuthenticated = false;

      localStorage.removeItem('userId');
      localStorage.removeItem('username');
      localStorage.removeItem('accessToken');
      localStorage.removeItem('refreshToken');
    },
  },
});

export const { setCredentials, logout } = authSlice.actions;
export default authSlice.reducer;
