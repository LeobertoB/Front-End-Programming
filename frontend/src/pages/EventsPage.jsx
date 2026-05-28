import { Search } from 'lucide-react';
import { useEffect, useMemo, useState } from 'react';
import { Link } from 'react-router-dom';
import { fetchCategories } from '../api/catalogApi';
import { getApiErrorMessage, toAssetUrl } from '../api/client';
import { fetchEvents } from '../api/eventsApi';
import { formatDateTime, formatMoney } from '../utils/formatters';

const emptyPage = {
  content: [],
  page: 0,
  size: 6,
  totalElements: 0,
  totalPages: 0,
  first: true,
  last: true,
};

export function EventsPage() {
  const [eventsPage, setEventsPage] = useState(emptyPage);
  const [categories, setCategories] = useState([]);
  const [filters, setFilters] = useState({
    city: '',
    categoryId: '',
    status: 'PUBLISHED',
  });
  const [page, setPage] = useState(0);
  const [status, setStatus] = useState('idle');
  const [error, setError] = useState(null);

  const params = useMemo(
    () => ({
      page,
      size: 6,
      sort: 'startsAt,asc',
      status: filters.status || undefined,
      city: filters.city || undefined,
      categoryId: filters.categoryId || undefined,
    }),
    [filters, page],
  );

  useEffect(() => {
    fetchCategories()
      .then(setCategories)
      .catch(() => setCategories([]));
  }, []);

  useEffect(() => {
    let active = true;
    setStatus('loading');
    setError(null);

    fetchEvents(params)
      .then((data) => {
        if (active) {
          setEventsPage(data);
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
  }, [params]);

  function updateFilter(event) {
    const { name, value } = event.target;
    setFilters((current) => ({ ...current, [name]: value }));
    setPage(0);
  }

  return (
    <section className="stack-page">
      <div className="section-heading">
        <p className="eyebrow">Events</p>
        <h1>Find your next experience</h1>
        <p>
          Explore upcoming listings by city, category, and publication status.
        </p>
      </div>

      <form className="filter-bar">
        <label>
          City
          <input name="city" value={filters.city} onChange={updateFilter} placeholder="Rome" />
        </label>
        <label>
          Category
          <select name="categoryId" value={filters.categoryId} onChange={updateFilter}>
            <option value="">All categories</option>
            {categories.map((category) => (
              <option key={category.id} value={category.id}>
                {category.name}
              </option>
            ))}
          </select>
        </label>
        <label>
          Status
          <select name="status" value={filters.status} onChange={updateFilter}>
            <option value="PUBLISHED">Published</option>
            <option value="DRAFT">Draft</option>
            <option value="CANCELLED">Cancelled</option>
            <option value="COMPLETED">Completed</option>
          </select>
        </label>
        <div className="filter-icon" aria-hidden="true">
          <Search size={22} />
        </div>
      </form>

      {error && <p className="form-error">{error}</p>}

      {status === 'succeeded' && (
        <div className="result-summary">
          <strong>{eventsPage.totalElements}</strong>
          <span>{eventsPage.totalElements === 1 ? 'event found' : 'events found'}</span>
        </div>
      )}

      <div className="card-grid">
        {status === 'loading' && Array.from({ length: 3 }).map((_, index) => (
          <article className="event-card skeleton-card" key={index} aria-hidden="true">
            <div className="skeleton-image" />
            <div>
              <span className="skeleton-line short" />
              <span className="skeleton-line" />
              <span className="skeleton-line" />
              <span className="skeleton-line tiny" />
            </div>
          </article>
        ))}

        {status !== 'loading' && eventsPage.content.map((event) => (
          <article className="event-card" key={event.id}>
            <img src={toAssetUrl(event.imageUrl)} alt="" />
            <div>
              <div className="card-topline">
                <span className="status-pill">{event.status}</span>
                <span>{event.category.name}</span>
              </div>
              <h2>{event.title}</h2>
              <p>{event.description}</p>
              <dl className="card-meta">
                <div>
                  <dt>Place</dt>
                  <dd>{event.venue.city}, {event.venue.country}</dd>
                </div>
                <div>
                  <dt>Date</dt>
                  <dd>{formatDateTime(event.startsAt)}</dd>
                </div>
              </dl>
              <div className="card-price">
                <span>From</span>
                <strong>{formatMoney(event.basePrice)}</strong>
              </div>
            </div>
            <Link className="button secondary wide" to={`/events/${event.id}`}>
              View details
            </Link>
          </article>
        ))}
      </div>

      {status === 'succeeded' && eventsPage.content.length === 0 && (
        <div className="empty-state">
          <strong>No events match these filters.</strong>
          <span>Try a different city, category, or status.</span>
        </div>
      )}

      <div className="pagination">
        <button className="button secondary" type="button" disabled={eventsPage.first} onClick={() => setPage((value) => value - 1)}>
          Previous
        </button>
        <span>
          Page {eventsPage.totalPages === 0 ? 0 : eventsPage.page + 1} of {eventsPage.totalPages}
        </span>
        <button className="button secondary" type="button" disabled={eventsPage.last} onClick={() => setPage((value) => value + 1)}>
          Next
        </button>
      </div>
    </section>
  );
}
