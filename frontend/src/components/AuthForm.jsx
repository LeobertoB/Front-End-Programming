import { useEffect, useState } from 'react';
import { useDispatch, useSelector } from 'react-redux';
import { useLocation, useNavigate } from 'react-router-dom';
import { getApiErrorMessage, toAssetUrl } from '../api/client';
import { uploadProfileImage } from '../api/usersApi';
import { login, register, selectAuth, updateCurrentUser } from '../store/authSlice';

const defaultRegisterState = {
  email: '',
  password: '',
  name: '',
  surname: '',
  profileImageUrl: 'https://images.unsplash.com/photo-1494790108377-be9c29b29330',
  city: '',
  favoriteEventType: '',
  role: 'ROLE_USER',
};

export function AuthForm({ mode }) {
  const isRegister = mode === 'register';
  const dispatch = useDispatch();
  const navigate = useNavigate();
  const location = useLocation();
  const { status, error } = useSelector(selectAuth);
  const [values, setValues] = useState(
    isRegister ? defaultRegisterState : { email: '', password: '' },
  );
  const [profileImageFile, setProfileImageFile] = useState(null);
  const [profileImagePreview, setProfileImagePreview] = useState(toAssetUrl(defaultRegisterState.profileImageUrl));
  const [localError, setLocalError] = useState(null);

  const isLoading = status === 'loading';

  useEffect(() => {
    if (!isRegister || profileImageFile) return;
    setProfileImagePreview(toAssetUrl(values.profileImageUrl));
  }, [isRegister, profileImageFile, values.profileImageUrl]);

  useEffect(() => () => {
    if (profileImagePreview?.startsWith('blob:')) {
      URL.revokeObjectURL(profileImagePreview);
    }
  }, [profileImagePreview]);

  function updateField(event) {
    const { name, value } = event.target;
    setValues((current) => ({ ...current, [name]: value }));
  }

  async function handleSubmit(event) {
    event.preventDefault();
    setLocalError(null);

    if (!values.email.includes('@')) {
      setLocalError('Enter a valid email address.');
      return;
    }
    if (values.password.length < 8) {
      setLocalError('Password must contain at least 8 characters.');
      return;
    }
    if (isRegister && (!values.name.trim() || !values.surname.trim())) {
      setLocalError('Name and surname are required.');
      return;
    }

    const action = isRegister ? register(values) : login(values);
    const result = await dispatch(action);

    if (result.meta.requestStatus === 'fulfilled') {
      if (isRegister && profileImageFile) {
        try {
          const updatedUser = await uploadProfileImage(profileImageFile);
          dispatch(updateCurrentUser(updatedUser));
        } catch (apiError) {
          setLocalError(getApiErrorMessage(apiError));
          return;
        }
      }
      const target = location.state?.from?.pathname ?? '/dashboard';
      navigate(target, { replace: true });
    }
  }

  function updateProfileImageFile(event) {
    const file = event.target.files?.[0] ?? null;
    setProfileImageFile(file);
    if (!file) {
      setProfileImagePreview(toAssetUrl(values.profileImageUrl));
      return;
    }
    if (profileImagePreview?.startsWith('blob:')) {
      URL.revokeObjectURL(profileImagePreview);
    }
    setProfileImagePreview(URL.createObjectURL(file));
  }

  return (
    <form className="form-panel" onSubmit={handleSubmit}>
      <div className="form-grid">
        <label>
          Email
          <input
            name="email"
            type="email"
            autoComplete="username"
            value={values.email}
            onChange={updateField}
            required
          />
        </label>

        <label>
          Password
          <input
            name="password"
            type="password"
            autoComplete={isRegister ? 'new-password' : 'current-password'}
            minLength="8"
            value={values.password}
            onChange={updateField}
            required
          />
        </label>

        {isRegister && (
          <>
            <label>
              Name
              <input
                name="name"
                autoComplete="given-name"
                value={values.name}
                onChange={updateField}
                required
              />
            </label>
            <label>
              Surname
              <input
                name="surname"
                autoComplete="family-name"
                value={values.surname}
                onChange={updateField}
                required
              />
            </label>
            <label>
              Profile image URL
              <input
                name="profileImageUrl"
                type="url"
                autoComplete="photo"
                value={values.profileImageUrl}
                onChange={updateField}
                required
              />
            </label>
            <label>
              Profile image upload
              <input
                type="file"
                accept="image/jpeg,image/png,image/webp,image/gif"
                onChange={updateProfileImageFile}
              />
            </label>
            <div className="register-image-preview span-2">
              <img src={profileImagePreview || toAssetUrl(values.profileImageUrl)} alt="" />
              <span>{profileImageFile ? profileImageFile.name : 'Current profile image preview'}</span>
            </div>
            <label>
              City
              <input
                name="city"
                autoComplete="address-level2"
                value={values.city}
                onChange={updateField}
              />
            </label>
            <label>
              Favorite event type
              <input
                name="favoriteEventType"
                autoComplete="off"
                value={values.favoriteEventType}
                onChange={updateField}
              />
            </label>
            <label>
              Role
              <select name="role" value={values.role} onChange={updateField}>
                <option value="ROLE_USER">Regular user</option>
                <option value="ROLE_ORGANIZER">Organizer</option>
                <option value="ROLE_ADMIN">Admin</option>
              </select>
            </label>
          </>
        )}
      </div>

      {(localError || error) && <p className="form-error">{localError ?? error}</p>}

      <button className="button primary wide" type="submit" disabled={isLoading}>
        {isLoading ? 'Please wait...' : isRegister ? 'Create account' : 'Login'}
      </button>
    </form>
  );
}
