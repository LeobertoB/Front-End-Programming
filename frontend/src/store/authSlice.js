import { createAsyncThunk, createSlice } from '@reduxjs/toolkit';
import { apiClient, getApiErrorMessage } from '../api/client';

const STORAGE_KEY = 'eventhub.auth';

function loadStoredAuth() {
  try {
    const raw = localStorage.getItem(STORAGE_KEY);
    return raw ? JSON.parse(raw) : null;
  } catch {
    return null;
  }
}

function persistAuth(auth) {
  if (auth?.token) {
    localStorage.setItem(STORAGE_KEY, JSON.stringify(auth));
  } else {
    localStorage.removeItem(STORAGE_KEY);
  }
}

const storedAuth = loadStoredAuth();

const initialState = {
  token: storedAuth?.token ?? null,
  user: storedAuth?.user ?? null,
  status: 'idle',
  error: null,
};

export const login = createAsyncThunk('auth/login', async (credentials, thunkApi) => {
  try {
    const { data } = await apiClient.post('/api/auth/login', credentials);
    return data;
  } catch (error) {
    return thunkApi.rejectWithValue(getApiErrorMessage(error));
  }
});

export const register = createAsyncThunk('auth/register', async (payload, thunkApi) => {
  try {
    const { data } = await apiClient.post('/api/auth/register', payload);
    return data;
  } catch (error) {
    return thunkApi.rejectWithValue(getApiErrorMessage(error));
  }
});

const authSlice = createSlice({
  name: 'auth',
  initialState,
  reducers: {
    clearCredentials(state) {
      state.token = null;
      state.user = null;
      state.status = 'idle';
      state.error = null;
      persistAuth(null);
    },
    updateCurrentUser(state, action) {
      state.user = {
        ...state.user,
        ...action.payload,
      };
      persistAuth({ token: state.token, user: state.user });
    },
  },
  extraReducers: (builder) => {
    builder
      .addCase(login.pending, (state) => {
        state.status = 'loading';
        state.error = null;
      })
      .addCase(login.fulfilled, (state, action) => {
        applyAuthPayload(state, action.payload);
      })
      .addCase(login.rejected, (state, action) => {
        state.status = 'failed';
        state.error = action.payload ?? 'Login failed';
      })
      .addCase(register.pending, (state) => {
        state.status = 'loading';
        state.error = null;
      })
      .addCase(register.fulfilled, (state, action) => {
        applyAuthPayload(state, action.payload);
      })
      .addCase(register.rejected, (state, action) => {
        state.status = 'failed';
        state.error = action.payload ?? 'Registration failed';
      });
  },
});

function applyAuthPayload(state, payload) {
  const user = {
    id: payload.userId,
    email: payload.email,
    name: payload.name,
    surname: payload.surname,
    profileImageUrl: payload.profileImageUrl,
    roles: payload.roles ?? [],
  };

  state.token = payload.token;
  state.user = user;
  state.status = 'succeeded';
  state.error = null;
  persistAuth({ token: payload.token, user });
}

export const { clearCredentials, updateCurrentUser } = authSlice.actions;
export const selectAuth = (state) => state.auth;
export const selectCurrentUser = (state) => state.auth.user;
export const selectIsAuthenticated = (state) => Boolean(state.auth.token);
export const selectHasAnyRole = (roles) => (state) =>
  roles.some((role) => state.auth.user?.roles?.includes(role));

export default authSlice.reducer;
