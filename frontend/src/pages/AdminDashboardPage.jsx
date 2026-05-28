import { useEffect, useState } from 'react';
import { useSelector } from 'react-redux';
import { fetchAdminStats } from '../api/adminApi';
import {
  createCategory,
  createVenue,
  deleteCategory,
  deleteVenue,
  fetchCategories,
  fetchVenues,
  updateCategory,
  updateVenue,
} from '../api/catalogApi';
import { getApiErrorMessage } from '../api/client';
import { selectCurrentUser } from '../store/authSlice';
import { formatMoney } from '../utils/formatters';
import { getPrimaryRoleLabel } from '../utils/roles';

const emptyCategoryForm = { name: '', description: '' };
const emptyVenueForm = {
  name: '',
  city: '',
  country: '',
  address: '',
  capacity: '100',
  latitude: '',
  longitude: '',
};

export function AdminDashboardPage() {
  const user = useSelector(selectCurrentUser);
  const [stats, setStats] = useState(null);
  const [categories, setCategories] = useState([]);
  const [venues, setVenues] = useState([]);
  const [categoryForm, setCategoryForm] = useState(emptyCategoryForm);
  const [venueForm, setVenueForm] = useState(emptyVenueForm);
  const [editingCategoryId, setEditingCategoryId] = useState(null);
  const [editingVenueId, setEditingVenueId] = useState(null);
  const [message, setMessage] = useState(null);
  const [error, setError] = useState(null);

  useEffect(() => {
    refresh();
  }, []);

  async function refresh() {
    try {
      const [statsData, categoryData, venueData] = await Promise.all([
        fetchAdminStats(),
        fetchCategories(),
        fetchVenues(),
      ]);
      setStats(statsData);
      setCategories(categoryData);
      setVenues(venueData);
    } catch (apiError) {
      setError(getApiErrorMessage(apiError));
    }
  }

  async function submitCategory(event) {
    event.preventDefault();
    setMessage(null);
    setError(null);
    if (categoryForm.name.trim().length < 2 || categoryForm.description.trim().length < 6) {
      setError('Category name must have at least 2 characters and description at least 6.');
      return;
    }
    try {
      if (editingCategoryId) {
        await updateCategory(editingCategoryId, categoryForm);
        setMessage('Category updated.');
      } else {
        await createCategory(categoryForm);
        setMessage('Category created.');
      }
      setCategoryForm(emptyCategoryForm);
      setEditingCategoryId(null);
      refresh();
    } catch (apiError) {
      setError(getApiErrorMessage(apiError));
    }
  }

  async function submitVenue(event) {
    event.preventDefault();
    setMessage(null);
    setError(null);
    if (!venueForm.name.trim() || !venueForm.city.trim() || !venueForm.country.trim() || !venueForm.address.trim()) {
      setError('Venue name, city, country, and address are required.');
      return;
    }
    if (Number(venueForm.capacity) < 1) {
      setError('Venue capacity must be at least 1.');
      return;
    }
    const payload = {
      ...venueForm,
      capacity: Number(venueForm.capacity),
      latitude: venueForm.latitude ? Number(venueForm.latitude) : null,
      longitude: venueForm.longitude ? Number(venueForm.longitude) : null,
    };
    try {
      if (editingVenueId) {
        await updateVenue(editingVenueId, payload);
        setMessage('Venue updated.');
      } else {
        await createVenue(payload);
        setMessage('Venue created.');
      }
      setVenueForm(emptyVenueForm);
      setEditingVenueId(null);
      refresh();
    } catch (apiError) {
      setError(getApiErrorMessage(apiError));
    }
  }

  function startCategoryEdit(category) {
    setEditingCategoryId(category.id);
    setCategoryForm({ name: category.name, description: category.description });
  }

  function startVenueEdit(venue) {
    setEditingVenueId(venue.id);
    setVenueForm({
      name: venue.name,
      city: venue.city,
      country: venue.country,
      address: venue.address,
      capacity: String(venue.capacity),
      latitude: venue.latitude ?? '',
      longitude: venue.longitude ?? '',
    });
  }

  async function removeCategory(categoryId) {
    setMessage(null);
    setError(null);
    try {
      await deleteCategory(categoryId);
      setMessage('Category deleted.');
      refresh();
    } catch (apiError) {
      setError(getApiErrorMessage(apiError));
    }
  }

  async function removeVenue(venueId) {
    setMessage(null);
    setError(null);
    try {
      await deleteVenue(venueId);
      setMessage('Venue deleted.');
      refresh();
    } catch (apiError) {
      setError(getApiErrorMessage(apiError));
    }
  }

  return (
    <section className="stack-page">
      <div className="section-heading">
        <p className="eyebrow">Admin Dashboard</p>
        <h1>Platform management</h1>
        <div className="role-strip">
          <span>{getPrimaryRoleLabel(user)}</span>
          <small>Admins can manage shared catalog data and review platform-level metrics.</small>
        </div>
      </div>

      {message && <p className="success-message">{message}</p>}
      {error && <p className="form-error">{error}</p>}

      {stats && (
        <div className="stats-grid">
          <div><span>Users</span><strong>{stats.users}</strong></div>
          <div><span>Events</span><strong>{stats.events}</strong></div>
          <div><span>Bookings</span><strong>{stats.confirmedBookings}</strong></div>
          <div><span>Revenue</span><strong>{formatMoney(stats.estimatedRevenue)}</strong></div>
        </div>
      )}

      <div className="management-grid">
        <form className="form-panel" onSubmit={submitCategory}>
          <h2>{editingCategoryId ? 'Edit category' : 'Create category'}</h2>
          <label>
            Name
            <input value={categoryForm.name} onChange={(event) => setCategoryForm((current) => ({ ...current, name: event.target.value }))} required />
          </label>
          <label>
            Description
            <input value={categoryForm.description} onChange={(event) => setCategoryForm((current) => ({ ...current, description: event.target.value }))} required />
          </label>
          <button className="button primary wide" type="submit">{editingCategoryId ? 'Save category' : 'Add category'}</button>
          {editingCategoryId && (
            <button className="button secondary wide" type="button" onClick={() => {
              setEditingCategoryId(null);
              setCategoryForm(emptyCategoryForm);
            }}>
              Cancel edit
            </button>
          )}
          <div className="compact-list">
            {categories.map((category) => (
              <div key={category.id}>
                <span>{category.name}</span>
                <div>
                  <button className="button secondary" type="button" onClick={() => startCategoryEdit(category)}>Edit</button>
                  <button className="button danger" type="button" onClick={() => removeCategory(category.id)}>Delete</button>
                </div>
              </div>
            ))}
          </div>
        </form>

        <form className="form-panel" onSubmit={submitVenue}>
          <h2>{editingVenueId ? 'Edit venue' : 'Create venue'}</h2>
          <div className="form-grid">
            {['name', 'city', 'country', 'address'].map((field) => (
              <label key={field}>
                {field[0].toUpperCase() + field.slice(1)}
                <input value={venueForm[field]} onChange={(event) => setVenueForm((current) => ({ ...current, [field]: event.target.value }))} required />
              </label>
            ))}
            <label>
              Capacity
              <input type="number" min="1" value={venueForm.capacity} onChange={(event) => setVenueForm((current) => ({ ...current, capacity: event.target.value }))} required />
            </label>
            <label>
              Latitude
              <input type="number" step="0.0001" value={venueForm.latitude} onChange={(event) => setVenueForm((current) => ({ ...current, latitude: event.target.value }))} />
            </label>
            <label>
              Longitude
              <input type="number" step="0.0001" value={venueForm.longitude} onChange={(event) => setVenueForm((current) => ({ ...current, longitude: event.target.value }))} />
            </label>
          </div>
          <button className="button primary wide" type="submit">{editingVenueId ? 'Save venue' : 'Add venue'}</button>
          {editingVenueId && (
            <button className="button secondary wide" type="button" onClick={() => {
              setEditingVenueId(null);
              setVenueForm(emptyVenueForm);
            }}>
              Cancel edit
            </button>
          )}
          <div className="compact-list">
            {venues.map((venue) => (
              <div key={venue.id}>
                <span>{venue.name} · {venue.city}</span>
                <div>
                  <button className="button secondary" type="button" onClick={() => startVenueEdit(venue)}>Edit</button>
                  <button className="button danger" type="button" onClick={() => removeVenue(venue.id)}>Delete</button>
                </div>
              </div>
            ))}
          </div>
        </form>
      </div>
    </section>
  );
}
