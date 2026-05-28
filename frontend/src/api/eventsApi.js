import { apiClient } from './client';

export async function fetchEvents(params = {}) {
  const { data } = await apiClient.get('/api/events', { params });
  return data;
}

export async function fetchEvent(eventId) {
  const { data } = await apiClient.get(`/api/events/${eventId}`);
  return data;
}

export async function fetchManagedEvents(params = {}) {
  const { data } = await apiClient.get('/api/organizer/events', { params });
  return data;
}

export async function createEvent(payload) {
  const { data } = await apiClient.post('/api/organizer/events', payload);
  return data;
}

export async function updateEvent(eventId, payload) {
  const { data } = await apiClient.put(`/api/organizer/events/${eventId}`, payload);
  return data;
}

export async function uploadEventImage(eventId, file) {
  const formData = new FormData();
  formData.append('file', file);
  const { data } = await apiClient.post(`/api/organizer/events/${eventId}/image/upload`, formData);
  return data;
}

export async function uploadEventGalleryImages(eventId, files) {
  const formData = new FormData();
  files.forEach((file) => formData.append('files', file));
  const { data } = await apiClient.post(`/api/organizer/events/${eventId}/gallery`, formData);
  return data;
}

export async function deleteEventGalleryImage(eventId, imageId) {
  const { data } = await apiClient.delete(`/api/organizer/events/${eventId}/gallery/${imageId}`);
  return data;
}

export async function deleteEvent(eventId) {
  await apiClient.delete(`/api/organizer/events/${eventId}`);
}

export async function fetchEventWeather(eventId) {
  const { data } = await apiClient.get(`/api/events/${eventId}/weather`);
  return data;
}
