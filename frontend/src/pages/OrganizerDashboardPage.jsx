import { useEffect, useState } from 'react';
import { useSelector } from 'react-redux';
import { fetchCategories, fetchVenues } from '../api/catalogApi';
import { getApiErrorMessage, toAssetUrl } from '../api/client';
import {
  createEvent,
  deleteEvent,
  deleteEventGalleryImage,
  fetchManagedEvents,
  updateEvent,
  uploadEventGalleryImages,
  uploadEventImage,
} from '../api/eventsApi';
import { selectCurrentUser } from '../store/authSlice';
import { getPrimaryRoleLabel } from '../utils/roles';

const initialEvent = {
  title: '',
  description: '',
  startsAt: '',
  endsAt: '',
  basePrice: '0',
  availableSeats: '50',
  imageUrl: 'https://images.unsplash.com/photo-1501281668745-f7f57925c3b4',
  venueId: '',
  categoryId: '',
  status: 'PUBLISHED',
};

export function OrganizerDashboardPage() {
  const user = useSelector(selectCurrentUser);
  const [categories, setCategories] = useState([]);
  const [venues, setVenues] = useState([]);
  const [eventForm, setEventForm] = useState(initialEvent);
  const [eventImageFile, setEventImageFile] = useState(null);
  const [eventImagePreview, setEventImagePreview] = useState(initialEvent.imageUrl);
  const [galleryFiles, setGalleryFiles] = useState([]);
  const [managedEvents, setManagedEvents] = useState([]);
  const [editingEventId, setEditingEventId] = useState(null);
  const [message, setMessage] = useState(null);
  const [error, setError] = useState(null);

  useEffect(() => {
    Promise.all([fetchCategories(), fetchVenues(), fetchManagedEvents({ size: 50, sort: 'startsAt,desc' })])
      .then(([categoryData, venueData, eventData]) => {
        setCategories(categoryData);
        setVenues(venueData);
        setManagedEvents(eventData.content ?? []);
      })
      .catch((apiError) => setError(getApiErrorMessage(apiError)));
  }, []);

  useEffect(() => {
    if (!eventImageFile) {
      setEventImagePreview(toAssetUrl(eventForm.imageUrl));
      return undefined;
    }
    const previewUrl = URL.createObjectURL(eventImageFile);
    setEventImagePreview(previewUrl);
    return () => URL.revokeObjectURL(previewUrl);
  }, [eventImageFile, eventForm.imageUrl]);

  function updateField(inputEvent) {
    const { name, value } = inputEvent.target;
    setEventForm((current) => ({ ...current, [name]: value }));
    if (name === 'imageUrl') {
      setEventImageFile(null);
    }
  }

  async function handleSubmit(inputEvent) {
    inputEvent.preventDefault();
    setMessage(null);
    setError(null);
    if (eventForm.title.trim().length < 4) {
      setError('Event title must have at least 4 characters.');
      return;
    }
    if (eventForm.description.trim().length < 12) {
      setError('Event description must have at least 12 characters.');
      return;
    }
    if (!eventForm.startsAt || !eventForm.endsAt || new Date(eventForm.endsAt) <= new Date(eventForm.startsAt)) {
      setError('End date must be after start date.');
      return;
    }
    if (Number(eventForm.basePrice) < 0 || Number(eventForm.availableSeats) < 1) {
      setError('Price must be zero or greater, and seats must be at least 1.');
      return;
    }
    const payload = {
      ...eventForm,
      basePrice: Number(eventForm.basePrice),
      availableSeats: Number(eventForm.availableSeats),
      venueId: Number(eventForm.venueId),
      categoryId: Number(eventForm.categoryId),
    };
    try {
      let saved;
      if (editingEventId) {
        saved = await updateEvent(editingEventId, payload);
        if (eventImageFile) {
          saved = await uploadEventImage(saved.id, eventImageFile);
        }
        if (galleryFiles.length) {
          saved = await uploadEventGalleryImages(saved.id, galleryFiles);
        }
        setManagedEvents((current) => current.map((event) => event.id === saved.id ? saved : event));
        setMessage(`Updated event: ${saved.title}`);
      } else {
        saved = await createEvent(payload);
        if (eventImageFile) {
          saved = await uploadEventImage(saved.id, eventImageFile);
        }
        if (galleryFiles.length) {
          saved = await uploadEventGalleryImages(saved.id, galleryFiles);
        }
        setManagedEvents((current) => [saved, ...current]);
        setMessage(`Created event: ${saved.title}`);
      }
      setEventForm(initialEvent);
      setEventImageFile(null);
      setEventImagePreview(initialEvent.imageUrl);
      setGalleryFiles([]);
      setEditingEventId(null);
    } catch (apiError) {
      setError(getApiErrorMessage(apiError));
    }
  }

  function startEventEdit(event) {
    setEditingEventId(event.id);
    setEventForm({
      title: event.title,
      description: event.description,
      startsAt: event.startsAt.slice(0, 16),
      endsAt: event.endsAt.slice(0, 16),
      basePrice: String(event.basePrice),
      availableSeats: String(event.availableSeats),
      imageUrl: event.imageUrl,
      venueId: String(event.venue.id),
      categoryId: String(event.category.id),
      status: event.status,
    });
    setEventImageFile(null);
    setEventImagePreview(toAssetUrl(event.imageUrl));
    setGalleryFiles([]);
  }

  async function removeEvent(eventId) {
    setMessage(null);
    setError(null);
    try {
      await deleteEvent(eventId);
      setManagedEvents((current) => current.filter((event) => event.id !== eventId));
      setMessage('Event deleted.');
    } catch (apiError) {
      setError(getApiErrorMessage(apiError));
    }
  }

  async function removeGalleryImage(eventId, imageId) {
    setMessage(null);
    setError(null);
    try {
      const updated = await deleteEventGalleryImage(eventId, imageId);
      setManagedEvents((current) => current.map((event) => event.id === updated.id ? updated : event));
      if (editingEventId === updated.id) {
        startEventEdit(updated);
      }
      setMessage('Gallery image removed.');
    } catch (apiError) {
      setError(getApiErrorMessage(apiError));
    }
  }

  return (
    <section className="stack-page">
      <div className="section-heading">
        <p className="eyebrow">Organizer Dashboard</p>
        <h1>{editingEventId ? 'Edit event' : 'Create an event'}</h1>
        <div className="role-strip">
          <span>{getPrimaryRoleLabel(user)}</span>
          <small>Organizers can create, update, and delete their own events. Admins can also access this area.</small>
        </div>
      </div>

      {message && <p className="success-message">{message}</p>}
      {error && <p className="form-error">{error}</p>}
      {(!categories.length || !venues.length) && (
        <p className="form-error">
          Create at least one category and one venue from the Admin Dashboard before creating events.
        </p>
      )}

      <form className="form-panel" onSubmit={handleSubmit}>
        <div className="form-grid">
          <div className="image-preview span-2">
            <img src={toAssetUrl(eventImagePreview)} alt="" />
          </div>
          <label>
            Title
            <input name="title" value={eventForm.title} onChange={updateField} required />
          </label>
          <label>
            Image URL
            <input name="imageUrl" type="url" value={eventForm.imageUrl} onChange={updateField} required />
          </label>
          <label>
            Upload cover image
            <input
              type="file"
              accept="image/jpeg,image/png,image/webp,image/gif"
              onChange={(event) => setEventImageFile(event.target.files?.[0] ?? null)}
            />
          </label>
          <label className="span-2">
            Add gallery images
            <input
              type="file"
              accept="image/jpeg,image/png,image/webp,image/gif"
              multiple
              onChange={(event) => setGalleryFiles(Array.from(event.target.files ?? []))}
            />
          </label>
          {galleryFiles.length > 0 && (
            <p className="muted span-2">{galleryFiles.length} gallery image{galleryFiles.length === 1 ? '' : 's'} selected.</p>
          )}
          {editingEventId && managedEvents.find((event) => event.id === editingEventId)?.galleryImages?.length > 0 && (
            <div className="gallery-strip span-2">
              {managedEvents.find((event) => event.id === editingEventId).galleryImages.map((image) => (
                <button
                  key={image.id}
                  className="gallery-thumb"
                  type="button"
                  onClick={() => removeGalleryImage(editingEventId, image.id)}
                  title="Remove gallery image"
                >
                  <img src={toAssetUrl(image.imageUrl)} alt="" />
                </button>
              ))}
            </div>
          )}
          <label className="span-2">
            Description
            <textarea name="description" value={eventForm.description} onChange={updateField} required />
          </label>
          <label>
            Starts at
            <input name="startsAt" type="datetime-local" value={eventForm.startsAt} onChange={updateField} required />
          </label>
          <label>
            Ends at
            <input name="endsAt" type="datetime-local" value={eventForm.endsAt} onChange={updateField} required />
          </label>
          <label>
            Price
            <input name="basePrice" type="number" min="0" step="0.01" value={eventForm.basePrice} onChange={updateField} required />
          </label>
          <label>
            Seats
            <input name="availableSeats" type="number" min="1" value={eventForm.availableSeats} onChange={updateField} required />
          </label>
          <label>
            Venue
            <select name="venueId" value={eventForm.venueId} onChange={updateField} required>
              <option value="">Select venue</option>
              {venues.map((venue) => (
                <option key={venue.id} value={venue.id}>{venue.name} · {venue.city}</option>
              ))}
            </select>
          </label>
          <label>
            Category
            <select name="categoryId" value={eventForm.categoryId} onChange={updateField} required>
              <option value="">Select category</option>
              {categories.map((category) => (
                <option key={category.id} value={category.id}>{category.name}</option>
              ))}
            </select>
          </label>
          <label>
            Status
            <select name="status" value={eventForm.status} onChange={updateField}>
              <option value="PUBLISHED">Published</option>
              <option value="DRAFT">Draft</option>
              <option value="CANCELLED">Cancelled</option>
              <option value="COMPLETED">Completed</option>
            </select>
          </label>
        </div>
        <button
          className="button primary wide"
          type="submit"
          disabled={!categories.length || !venues.length}
        >
          {editingEventId ? 'Save event' : 'Create event'}
        </button>
        {editingEventId && (
          <button className="button secondary wide" type="button" onClick={() => {
            setEditingEventId(null);
            setEventForm(initialEvent);
            setEventImageFile(null);
            setEventImagePreview(initialEvent.imageUrl);
            setGalleryFiles([]);
          }}>
            Cancel edit
          </button>
        )}
      </form>

      <section className="list-panel">
        <h2>Managed events</h2>
        {managedEvents.length ? (
          <div className="compact-list">
            {managedEvents.map((event) => (
              <div key={event.id}>
                <img src={toAssetUrl(event.imageUrl)} alt="" />
                <span>{event.title} · {event.status}</span>
                <div>
                  <button className="button secondary" type="button" onClick={() => startEventEdit(event)}>Edit</button>
                  <button className="button danger" type="button" onClick={() => removeEvent(event.id)}>Delete</button>
                </div>
              </div>
            ))}
          </div>
        ) : (
          <p className="muted">Created events will appear here for quick editing.</p>
        )}
      </section>
    </section>
  );
}
