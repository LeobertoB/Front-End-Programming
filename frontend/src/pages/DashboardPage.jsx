import { useEffect, useState } from 'react';
import { useDispatch, useSelector } from 'react-redux';
import { Link } from 'react-router-dom';
import { fetchMyBookings } from '../api/bookingsApi';
import { getApiErrorMessage, toAssetUrl } from '../api/client';
import {
  createReview,
  fetchManageableReviews,
  fetchMyReviews,
  followUpReview,
  replyToReview,
} from '../api/reviewsApi';
import { updateProfileImage, uploadProfileImage } from '../api/usersApi';
import { selectCurrentUser, updateCurrentUser } from '../store/authSlice';
import { formatDateTime, formatMoney } from '../utils/formatters';
import { canManageEvents, canManagePlatform, hasRole, ROLES, getPrimaryRoleLabel } from '../utils/roles';

export function DashboardPage() {
  const dispatch = useDispatch();
  const user = useSelector(selectCurrentUser);
  const isRegularUser = hasRole(user, ROLES.USER);
  const canManage = canManageEvents(user) || canManagePlatform(user);
  const [bookings, setBookings] = useState([]);
  const [reviews, setReviews] = useState([]);
  const [managedReviews, setManagedReviews] = useState([]);
  const [profileImageUrl, setProfileImageUrl] = useState(user?.profileImageUrl ?? '');
  const [profileImageFile, setProfileImageFile] = useState(null);
  const [profileImagePreview, setProfileImagePreview] = useState(toAssetUrl(user?.profileImageUrl));
  const [reviewDrafts, setReviewDrafts] = useState({});
  const [followUpDrafts, setFollowUpDrafts] = useState({});
  const [officialReplyDrafts, setOfficialReplyDrafts] = useState({});
  const [message, setMessage] = useState(null);
  const [error, setError] = useState(null);

  useEffect(() => {
    const requests = [];
    if (isRegularUser) {
      requests.push(fetchMyBookings(), fetchMyReviews());
    }
    if (canManage) {
      requests.push(fetchManageableReviews());
    }

    Promise.all(requests)
      .then((results) => {
        let index = 0;
        if (isRegularUser) {
          setBookings(results[index]);
          setReviews(results[index + 1]);
          index += 2;
        } else {
          setBookings([]);
          setReviews([]);
        }
        setManagedReviews(canManage ? results[index] : []);
      })
      .catch((apiError) => setError(getApiErrorMessage(apiError)));
  }, [canManage, isRegularUser]);

  useEffect(() => {
    setProfileImageUrl(user?.profileImageUrl ?? '');
    setProfileImagePreview(toAssetUrl(user?.profileImageUrl));
  }, [user?.profileImageUrl]);

  useEffect(() => {
    if (!profileImageFile) {
      return undefined;
    }
    const previewUrl = URL.createObjectURL(profileImageFile);
    setProfileImagePreview(previewUrl);
    return () => URL.revokeObjectURL(previewUrl);
  }, [profileImageFile]);

  function updateDraft(eventId, field, value) {
    setReviewDrafts((current) => ({
      ...current,
      [eventId]: {
        rating: 5,
        comment: '',
        ...current[eventId],
        [field]: value,
      },
    }));
  }

  async function submitReview(eventId) {
    setMessage(null);
    setError(null);
    const draft = reviewDrafts[eventId] ?? { rating: 5, comment: '' };
    try {
      const created = await createReview({
        eventId,
        rating: Number(draft.rating),
        comment: draft.comment,
      });
      setMessage('Review submitted.');
      setReviews((current) => [created, ...current]);
      setReviewDrafts((current) => ({ ...current, [eventId]: { rating: 5, comment: '' } }));
    } catch (apiError) {
      setError(getApiErrorMessage(apiError));
    }
  }

  async function submitProfileImage(event) {
    event.preventDefault();
    setMessage(null);
    setError(null);

    if (!profileImageUrl.trim()) {
      setError('Profile image URL is required.');
      return;
    }

    try {
      const updatedUser = await updateProfileImage(profileImageUrl.trim());
      dispatch(updateCurrentUser(updatedUser));
      setProfileImageFile(null);
      setMessage('Profile image updated.');
    } catch (apiError) {
      setError(getApiErrorMessage(apiError));
    }
  }

  async function submitProfileImageUpload() {
    setMessage(null);
    setError(null);

    if (!profileImageFile) {
      setError('Choose an image file first.');
      return;
    }

    try {
      const updatedUser = await uploadProfileImage(profileImageFile);
      dispatch(updateCurrentUser(updatedUser));
      setProfileImageUrl(updatedUser.profileImageUrl);
      setProfileImageFile(null);
      setMessage('Profile image uploaded.');
    } catch (apiError) {
      setError(getApiErrorMessage(apiError));
    }
  }

  async function submitFollowUp(reviewId) {
    setMessage(null);
    setError(null);
    const followUp = followUpDrafts[reviewId]?.trim();
    if (!followUp) {
      setError('Reply text is required.');
      return;
    }

    try {
      const updated = await followUpReview(reviewId, followUp);
      setReviews((current) => current.map((review) => review.id === updated.id ? updated : review));
      setFollowUpDrafts((current) => ({ ...current, [reviewId]: '' }));
      setMessage('Your reply was published.');
    } catch (apiError) {
      setError(getApiErrorMessage(apiError));
    }
  }

  async function submitOfficialReply(reviewId) {
    setMessage(null);
    setError(null);
    const reply = officialReplyDrafts[reviewId]?.trim();
    if (!reply) {
      setError('Official reply text is required.');
      return;
    }

    try {
      const updated = await replyToReview(reviewId, reply);
      setManagedReviews((current) => current.map((review) => review.id === updated.id ? updated : review));
      setReviews((current) => current.map((review) => review.id === updated.id ? updated : review));
      setOfficialReplyDrafts((current) => ({ ...current, [reviewId]: '' }));
      setMessage('Official reply published.');
    } catch (apiError) {
      setError(getApiErrorMessage(apiError));
    }
  }

  return (
    <section className="stack-page">
      <div className="section-heading">
        <p className="eyebrow">User Dashboard</p>
        <h1>Welcome, {user?.name}</h1>
        <div className="role-strip">
          <span>{getPrimaryRoleLabel(user)}</span>
          <small>Bookings and reviews are available to authenticated users.</small>
        </div>
      </div>

      {canManage && (
        <div className="permission-grid">
          {canManageEvents(user) && (
            <Link className="permission-link" to="/organizer">
              <strong>Organizer tools</strong>
              <span>Create and maintain event listings.</span>
            </Link>
          )}
          {canManagePlatform(user) && (
            <Link className="permission-link" to="/admin">
              <strong>Admin tools</strong>
              <span>Manage platform catalog data and analytics.</span>
            </Link>
          )}
        </div>
      )}

      {message && <p className="success-message">{message}</p>}
      {error && <p className="form-error">{error}</p>}

      <div className="profile-panel">
        <img src={profileImagePreview || toAssetUrl(user?.profileImageUrl)} alt="" />
        <form className="inline-profile-form" onSubmit={submitProfileImage}>
          <label>
            Profile image URL
            <input
              type="url"
              value={profileImageUrl}
              onChange={(event) => setProfileImageUrl(event.target.value)}
              required
            />
          </label>
          <button className="button secondary" type="submit">Update image</button>
        </form>
        <div className="upload-row">
          <label>
            Upload profile image
            <input
              type="file"
              accept="image/jpeg,image/png,image/webp,image/gif"
              onChange={(event) => setProfileImageFile(event.target.files?.[0] ?? null)}
            />
          </label>
          <button className="button primary" type="button" onClick={submitProfileImageUpload}>
            Upload image
          </button>
        </div>
      </div>

      {canManage && (
        <div className="list-panel">
          <h2>Reviews to manage</h2>
          {managedReviews.length ? (
            <div className="review-list">
              {managedReviews.map((review) => (
                <article key={review.id} className="review-card">
                  <div className="review-heading">
                    <strong>{review.rating}/5 for {review.eventTitle}</strong>
                    <span>{formatDateTime(review.createdAt)}</span>
                  </div>
                  <small className="muted">Review by {review.userName}</small>
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
                  <div className="reply-form">
                    <textarea
                      aria-label="Official reply"
                      placeholder={review.officialReply ? 'Update official reply' : 'Write official reply'}
                      value={officialReplyDrafts[review.id] ?? ''}
                      onChange={(event) => setOfficialReplyDrafts((current) => ({
                        ...current,
                        [review.id]: event.target.value,
                      }))}
                    />
                    <button className="button secondary" type="button" onClick={() => submitOfficialReply(review.id)}>
                      {review.officialReply ? 'Update reply' : 'Reply'}
                    </button>
                  </div>
                </article>
              ))}
            </div>
          ) : (
            <p className="muted">Reviews for managed events will appear here.</p>
          )}
        </div>
      )}

      {isRegularUser && (
        <>
          <div className="list-panel">
            <h2>Your review activity</h2>
            {reviews.length ? (
              <div className="review-list">
                {reviews.map((review) => (
                  <article key={review.id} className="review-card">
                    <div className="review-heading">
                      <strong>{review.rating}/5</strong>
                      <span>{formatDateTime(review.createdAt)}</span>
                    </div>
                    <p>{review.comment}</p>
                    {review.officialReply ? (
                      <div className="official-reply">
                        <span>Official reply from {review.repliedByName}</span>
                        <p>{review.officialReply}</p>
                      </div>
                    ) : (
                      <small className="muted">No official reply yet.</small>
                    )}
                    {review.userFollowUp && (
                      <div className="user-follow-up">
                        <span>Your response</span>
                        <p>{review.userFollowUp}</p>
                      </div>
                    )}
                    {review.officialReply && (
                      <div className="reply-form">
                        <textarea
                          aria-label="Reply to official response"
                          placeholder={review.userFollowUp ? 'Update your response' : 'Reply to the official response'}
                          value={followUpDrafts[review.id] ?? ''}
                          onChange={(event) => setFollowUpDrafts((current) => ({
                            ...current,
                            [review.id]: event.target.value,
                          }))}
                        />
                        <button className="button secondary" type="button" onClick={() => submitFollowUp(review.id)}>
                          {review.userFollowUp ? 'Update response' : 'Reply'}
                        </button>
                      </div>
                    )}
                  </article>
                ))}
              </div>
            ) : (
              <p className="muted">Submitted reviews will appear here.</p>
            )}
          </div>

          <div className="list-panel">
            <h2>Your bookings</h2>
            {bookings.length ? (
              bookings.map((booking) => {
                const draft = reviewDrafts[booking.eventId] ?? { rating: 5, comment: '' };
                return (
                  <article className="booking-row" key={booking.id}>
                    <div>
                      <strong>{booking.eventTitle}</strong>
                      <p>{booking.quantity} tickets · {formatMoney(booking.totalPrice)} · {formatDateTime(booking.createdAt)}</p>
                    </div>
                    <div className="inline-form">
                      <select
                        aria-label="Review rating"
                        value={draft.rating}
                        onChange={(event) => updateDraft(booking.eventId, 'rating', event.target.value)}
                      >
                        {[5, 4, 3, 2, 1].map((rating) => (
                          <option key={rating} value={rating}>{rating}/5</option>
                        ))}
                      </select>
                      <input
                        aria-label="Review comment"
                        placeholder="Write a review"
                        value={draft.comment}
                        onChange={(event) => updateDraft(booking.eventId, 'comment', event.target.value)}
                      />
                      <button className="button secondary" type="button" onClick={() => submitReview(booking.eventId)}>
                        Review
                      </button>
                    </div>
                  </article>
                );
              })
            ) : (
              <p className="muted">No bookings yet.</p>
            )}
          </div>
        </>
      )}
    </section>
  );
}
