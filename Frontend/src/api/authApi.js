import axiosClient from './axiosClient';

export async function register({ username, email, password }) {
  const { data } = await axiosClient.post('/auth/register', { username, email, password });
  return data;
}

export async function login({ usernameOrEmail, password }) {
  const { data } = await axiosClient.post('/auth/login', { usernameOrEmail, password });
  return data;
}
