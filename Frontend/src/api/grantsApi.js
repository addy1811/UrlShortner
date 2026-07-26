import axiosClient from './axiosClient';

export async function grantAccess(linkId, { username, email }) {
  // Exactly one of username/email should be set - mirrors GrantAccessRequest's
  // isValid() XOR check on the backend.
  const { data } = await axiosClient.post(`/links/${linkId}/grants`, { username, email });
  return data;
}

export async function listGrants(linkId) {
  const { data } = await axiosClient.get(`/links/${linkId}/grants`);
  return data;
}

export async function revokeAccess(linkId, grantId) {
  await axiosClient.delete(`/links/${linkId}/grants/${grantId}`);
}

export async function reactivateAccess(linkId, grantId) {
  const { data } = await axiosClient.post(`/links/${linkId}/grants/${grantId}/reactivate`);
  return data;
}