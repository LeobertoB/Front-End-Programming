import { apiClient } from './client';

export async function fetchEventReviews(eventId) {
  const { data } = await apiClient.get(`/api/events/${eventId}/reviews`);
  return data;
}

export async function createReview(payload) {
  const { data } = await apiClient.post('/api/reviews', payload);
  return data;
}

export async function fetchMyReviews() {
  const { data } = await apiClient.get('/api/reviews/me');
  return data;
}

export async function fetchManageableReviews() {
  const { data } = await apiClient.get('/api/reviews/manageable');
  return data;
}

export async function replyToReview(reviewId, reply) {
  const { data } = await apiClient.put(`/api/reviews/${reviewId}/reply`, { reply });
  return data;
}

export async function followUpReview(reviewId, followUp) {
  const { data } = await apiClient.put(`/api/reviews/${reviewId}/follow-up`, { followUp });
  return data;
}
