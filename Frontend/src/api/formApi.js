import axiosClient from './axiosClient';

export async function defineFormFields(linkId, fields) {
  // fields: array of { fieldKey, label, fieldType, required, options, displayOrder }
  const { data } = await axiosClient.post(`/links/${linkId}/form`, fields);
  return data;
}

export async function getFormSchema(linkId) {
  const { data } = await axiosClient.get(`/links/${linkId}/form`);
  return data;
}

// Used by the public form-fill page - works whether or not the visitor is
// logged in (axiosClient attaches a token if one exists, omits it otherwise).
export async function submitForm(linkId, responseData) {
  await axiosClient.post(`/links/${linkId}/form/submit`, { responseData });
}

export async function getFormResponses(linkId, { page = 0, size = 20 } = {}) {
  const { data } = await axiosClient.get(`/links/${linkId}/form/responses`, {
    params: { page, size },
  });
  return data;
}
