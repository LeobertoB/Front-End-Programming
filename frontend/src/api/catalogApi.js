import { apiClient } from './client';

export async function fetchCategories() {
  const { data } = await apiClient.get('/api/categories');
  return data;
}

export async function createCategory(payload) {
  const { data } = await apiClient.post('/api/admin/categories', payload);
  return data;
}

export async function updateCategory(categoryId, payload) {
  const { data } = await apiClient.put(`/api/admin/categories/${categoryId}`, payload);
  return data;
}

export async function deleteCategory(categoryId) {
  await apiClient.delete(`/api/admin/categories/${categoryId}`);
}

export async function fetchVenues(city) {
  const { data } = await apiClient.get('/api/venues', {
    params: city ? { city } : {},
  });
  return data;
}

export async function createVenue(payload) {
  const { data } = await apiClient.post('/api/admin/venues', payload);
  return data;
}

export async function updateVenue(venueId, payload) {
  const { data } = await apiClient.put(`/api/admin/venues/${venueId}`, payload);
  return data;
}

export async function deleteVenue(venueId) {
  await apiClient.delete(`/api/admin/venues/${venueId}`);
}

export async function fetchCountryInfo(country) {
  const { data } = await apiClient.get('/api/venues/country-info', {
    params: { country },
  });
  return data;
}
