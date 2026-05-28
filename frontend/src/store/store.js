import { configureStore } from '@reduxjs/toolkit';
import authReducer from './authSlice';
import { attachAuthInterceptors } from '../api/client';

export const store = configureStore({
  reducer: {
    auth: authReducer,
  },
});

attachAuthInterceptors(store);
