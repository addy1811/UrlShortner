import axiosClient from './axiosClient';

export async function createLink(payload) {
  // payload: { destinationUrl, visibility?, customAlias?, expiresAt?, maxUses?, metadata? }
  const { data } = await axiosClient.post('/links', payload);
  return data;
}

export async function listMyLinks({ page = 0, size = 20 } = {}) {
  const { data } = await axiosClient.get('/links', { params: { page, size } });
  return data; // Spring Page<> shape: { content, totalElements, totalPages, number, ... }
}

export async function getLink(linkId) {
  const { data } = await axiosClient.get(`/links/${linkId}`);
  return data;
}

export async function updateLink(linkId, payload) {
  // payload fields all optional (PATCH semantics) - only send what changed.
  const { data } = await axiosClient.patch(`/links/${linkId}`, payload);
  return data;
}

export async function deleteLink(linkId) {
  await axiosClient.delete(`/links/${linkId}`);
}
