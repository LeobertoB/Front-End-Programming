import { CloudSun, Star, Ticket } from 'lucide-react';
import { useEffect, useState } from 'react';
import { useSelector } from 'react-redux';
import { useParams } from 'react-router-dom';
import { createBooking } from '../api/bookingsApi';
import { getApiErrorMessage, toAssetUrl } from '../api/client';
import { fetchEvent, fetchEventWeather } from '../api/eventsApi';
import { fetchEventReviews, replyToReview } from '../api/reviewsApi';
import { selectCurrentUser, selectIsAuthenticated } from '../store/authSlice';
import { formatDateTime, formatMoney } from '../utils/formatters';
import { hasRole, ROLES } from '../utils/roles';

export function EventDetailPage() {
  const { eventId } = useParams();
  const isAuthenticated = useSelector(selectIsAuthenticated);
  const user = useSelector(selectCurrentUser);
  const canBook = hasRole(user, ROLES.USER);
  const [event, setEvent] = useState(null);
  const [weather, setWeather] = useState(null);
  const [reviews, setReviews] = useState([]);
  const [replyDrafts, setReplyDrafts] = useState({});
  const [quantity, setQuantity] = useState(1);
  const [status, setStatus] = useState('loading');
  const [message, setMessage] = useState(null);
  const [error, setError] = useState(null);

  useEffect(() => {
    let active = true;
    setStatus('loading');
    setError(null);

    Promise.all([
      fetchEvent(eventId),
      fetchEventReviews(eventId),
      fetchEventWeather(eventId).catch(() => null),
    ])
      .then(([eventData, reviewData, weatherData]) => {
        if (active) {
          setEvent(eventData);
          setReviews(reviewData);
          setWeather(weatherData);
          setStatus('succeeded');
        }
      })
      .catch((apiError) => {
        if (active) {
          setError(getApiErrorMessage(apiError));
          setStatus('failed');
        }
      });

    return () => {
      active = false;
    };
  }, [eventId]);

  async function handleBooking() {
    setMessage(null);
    setError(null);
    try {
      await createBooking({ eventId: Number(eventId), quantity: Number(quantity) });
      setMessage('Booking confirmed.');
      const updated = await fetchEvent(eventId);
      setEvent(updated);
    } catch (apiError) {
      setError(getApiErrorMessage(apiError));
    }
  }

  async function submitReply(reviewId) {
    setMessage(null);
    setError(null);
    const reply = replyDrafts[reviewId]?.trim();
    if (!reply) {
      setError('Reply text is required.');
      return;
    }

    try {
      const updated = await replyToReview(reviewId, reply);
      setReviews((current) => current.map((review) => review.id === updated.id ? updated : review));
      setReplyDrafts((current) => ({ ...current, [reviewId]: '' }));
      setMessage('Official reply published.');
    } catch (apiError) {
      setError(getApiErrorMessage(apiError));
    }
  }

  if (status === 'loading') {
    return <p className="muted">Loading event...</p>;
  }

  if (error && !event) {
    return <p className="form-error">{error}</p>;
  }

  return (
    <section className="detail-page">
      <img className="detail-image" src={toAssetUrl(event.imageUrl)} alt="" />
      {event.galleryImages?.length > 0 && (
        <div className="detail-gallery">
          {event.galleryImages.map((image) => (
            <img key={image.id} src={toAssetUrl(image.imageUrl)} alt="" />
          ))}
        </div>
      )}
      <div className="detail-layout">
        <article className="content-section">
          <p className="eyebrow">{event.category.name}</p>
          <h1>{event.title}</h1>
          <p>{event.description}</p>
          <dl className="meta-list">
            <div>
              <dt>When</dt>
              <dd>{formatDateTime(event.startsAt)}</dd>
            </div>
            <div>
              <dt>Where</dt>
              <dd>{event.venue.name}, {event.venue.city}</dd>
            </div>
            <div>
              <dt>Price</dt>
              <dd>{formatMoney(event.basePrice)}</dd>
            </div>
            <div>
              <dt>Seats</dt>
              <dd>{event.availableSeats}</dd>
            </div>
          </dl>
        </article>

        <aside className="side-panel">
          <h2>Book tickets</h2>
          {isAuthenticated && canBook ? (
            <>
              <label>
                Quantity
                <input
                  type="number"
                  min="1"
                  max={event.availableSeats}
                  value={quantity}
                  onChange={(inputEvent) => setQuantity(inputEvent.target.value)}
                />
              </label>
              <button className="button primary wide" type="button" onClick={handleBooking}>
                <Ticket size={18} aria-hidden="true" />
                Confirm booking
              </button>
            </>
          ) : isAuthenticated ? (
            <p className="muted">Organizer and admin accounts manage events instead of booking tickets.</p>
          ) : (
            <p className="muted">Login to book this event.</p>
          )}
          {message && <p className="success-message">{message}</p>}
          {error && <p className="form-error">{error}</p>}
        </aside>
      </div>

      <div className="info-grid">
        <section className="info-panel">
          <h2>
            <CloudSun size={20} aria-hidden="true" />
            Weather
          </h2>
          {weather ? <p>{weather.summary}</p> : <p className="muted">Weather unavailable.</p>}
        </section>
        <section className="info-panel">
          <h2>
            <Star size={20} aria-hidden="true" />
            Reviews
          </h2>
          {reviews.length ? (
            <div className="review-list">
              {reviews.map((review) => (
                <article key={review.id}>
                  <strong>{review.rating}/5 by {review.userName}</strong>
                  <p>{review.comment}</p>
                  {review.officialReply && (
                    <div className="official-reply">
                      <span>Official reply from {review.repliedByName}</span>
                      <p>{review.officialReply}</p>
                    </div>
                  )}
                  {review.userFollowUp && (
                    <div className="user-follow-up">
                      <span>Guest response</span>
                      <p>{review.userFollowUp}</p>
                    </div>
                  )}
                  {isAuthenticated && event && (hasRole(user, ROLES.ADMIN) || event.organizerId === user?.id) && (
                    <div className="reply-form">
                      <textarea
                        aria-label="Official reply"
                        placeholder={review.officialReply ? 'Update official reply' : 'Write official reply'}
                        value={replyDrafts[review.id] ?? ''}
                        onChange={(inputEvent) => setReplyDrafts((current) => ({
                          ...current,
                          [review.id]: inputEvent.target.value,
                        }))}
                      />
                      <button className="button secondary" type="button" onClick={() => submitReply(review.id)}>
                        {review.officialReply ? 'Update reply' : 'Reply'}
                      </button>
                    </div>
                  )}
                </article>
              ))}
            </div>
          ) : (
            <p className="muted">No reviews yet.</p>
          )}
        </section>
      </div>
    </section>
  );
}
