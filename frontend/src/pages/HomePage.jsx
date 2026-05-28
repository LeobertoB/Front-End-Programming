import { Link } from 'react-router-dom';

export function HomePage() {
  return (
    <div className="home-page">
      <section className="hero">
        <div className="hero-copy">
          <p className="eyebrow">EventHub</p>
          <h1>Discover, book, and manage live experiences.</h1>
          <p>
            A full-stack event platform with role-based dashboards, bookings, reviews,
            venue intelligence, and weather context for upcoming events.
          </p>
          <div className="hero-actions">
            <Link className="button primary" to="/events">
              Browse events
            </Link>
            <Link className="button secondary" to="/register">
              Create account
            </Link>
          </div>
          <div className="hero-stats" aria-label="Platform highlights">
            <div>
              <strong>3</strong>
              <span>Role dashboards</span>
            </div>
            <div>
              <strong>2</strong>
              <span>External APIs</span>
            </div>
            <div>
              <strong>14</strong>
              <span>Backend tests</span>
            </div>
          </div>
        </div>
        <div className="hero-media" aria-hidden="true">
          <div className="calendar-tile">
            <span>JUN</span>
            <strong>10</strong>
            <small>Live night</small>
          </div>
        </div>
      </section>

      <section className="home-band" aria-label="Platform areas">
        <article>
          <span>Guests</span>
          <strong>Booking flow</strong>
          <p>Browse, reserve seats, manage bookings, and publish reviews after attending.</p>
        </article>
        <article>
          <span>Organizers</span>
          <strong>Event operations</strong>
          <p>Create drafts, publish listings, edit schedules, and manage event availability.</p>
        </article>
        <article>
          <span>Admins</span>
          <strong>Catalog control</strong>
          <p>Maintain categories, venues, capacity data, and aggregated platform metrics.</p>
        </article>
      </section>
    </div>
  );
}
