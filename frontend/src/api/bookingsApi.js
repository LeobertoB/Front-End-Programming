import { apiClient } from './client';

export async function fetchMyBookings() {
  const { data } = await apiClient.get('/api/bookings/me');
  return data;
}

export async function createBooking(payload) {
  const { data } = await apiClient.post('/api/bookings', payload);
  return data;
}
