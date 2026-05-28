import { apiClient } from './client';

export async function fetchAdminStats() {
  const { data } = await apiClient.get('/api/admin/stats');
  return data;
}
