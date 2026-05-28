import { apiClient } from './client';

export async function updateProfileImage(profileImageUrl) {
  const { data } = await apiClient.put('/api/users/me/profile-image', { profileImageUrl });
  return data;
}

export async function uploadProfileImage(file) {
  const formData = new FormData();
  formData.append('file', file);
  const { data } = await apiClient.post('/api/users/me/profile-image/upload', formData);
  return data;
}
