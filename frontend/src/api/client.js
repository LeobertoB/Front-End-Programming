import axios from 'axios';
import { clearCredentials } from '../store/authSlice';

const API_BASE_URL = import.meta.env.VITE_API_BASE_URL ?? 'http://localhost:8080';

export const apiClient = axios.create({
  baseURL: API_BASE_URL,
});

export function attachAuthInterceptors(store) {
  apiClient.interceptors.request.use((config) => {
    const token = store.getState().auth.token;
    if (token) {
      config.headers.Authorization = `Bearer ${token}`;
    }
    return config;
  });

  apiClient.interceptors.response.use(
    (response) => response,
    (error) => {
      if (error.response?.status === 401) {
        store.dispatch(clearCredentials());
      }
      return Promise.reject(error);
    },
  );
}

export function getApiErrorMessage(error) {
  return (
    error.response?.data?.message ??
    error.response?.data?.error ??
    error.message ??
    'Unexpected error'
  );
}

export function toAssetUrl(url) {
  if (!url) return '';
  if (url.startsWith('http://') || url.startsWith('https://') || url.startsWith('blob:')) {
    return url;
  }
  if (url.startsWith('/')) {
    return `${API_BASE_URL}${url}`;
  }
  return url;
}
