# EventHub

EventHub is a full-stack event management platform built with React, Redux, Spring Boot, PostgreSQL, JWT authentication, role-based permissions, and third-party API integrations.

The app supports three roles:

- `ROLE_USER`: browse events, book tickets, write reviews, update profile image
- `ROLE_ORGANIZER`: create, update, and delete events
- `ROLE_ADMIN`: manage categories, venues, and platform statistics

## Project Structure

```text
.
├── backend/      Spring Boot REST API
├── frontend/     React + Vite application
├── postman/      API collection
└── requirements.md
```

## Requirements

- Java 21+
- Maven 3.9+
- Node.js 20+
- PostgreSQL 15+

## Backend Setup

Create a PostgreSQL database and user:

```sql
CREATE DATABASE eventhub;
CREATE USER eventhub WITH PASSWORD 'eventhub';
GRANT ALL PRIVILEGES ON DATABASE eventhub TO eventhub;
```

Run the API:

```bash
cd backend
mvn spring-boot:run
```

Default backend URL:

```text
http://localhost:8080
```

Useful environment variables:

```text
DATABASE_URL=jdbc:postgresql://localhost:5432/eventhub
DATABASE_USERNAME=eventhub
DATABASE_PASSWORD=eventhub
JWT_SECRET=change-this-development-secret-key-with-at-least-32-chars
JWT_EXPIRATION_MINUTES=120
JPA_DDL_AUTO=update
OPEN_METEO_BASE_URL=https://api.open-meteo.com/v1
REST_COUNTRIES_BASE_URL=https://restcountries.com/v3.1
UPLOAD_DIRECTORY=uploads
```

For production-like runs, set `JPA_DDL_AUTO=validate` after the schema exists.

## Frontend Setup

Install dependencies and run the React app:

```bash
cd frontend
npm install
npm run dev
```

Default frontend URL:

```text
http://localhost:5173
```

Optional frontend API override:

```text
VITE_API_BASE_URL=http://localhost:8080
```

## Tests

Run backend tests:

```bash
cd backend
mvn test
```

Run frontend production build:

```bash
cd frontend
npm run build
```

## Postman

Import this collection:

```text
postman/EventHub.postman_collection.json
```

The collection includes authentication, role checks, categories, venues, events, bookings, reviews, third-party integrations, admin stats, validation examples, and cleanup requests.

Recommended flow:

1. Run the `Authentication` requests to register/login users and save tokens.
2. Run `Categories`, `Venues`, and `Events` to create catalog data.
3. Run `Bookings And Reviews`, `Admin`, and integration requests.
4. Run `Cleanup` only when you want to remove demo records.

## Main API Areas

- `POST /api/auth/register`
- `POST /api/auth/login`
- `GET /api/users/me`
- `PUT /api/users/me/profile-image`
- `POST /api/users/me/profile-image/upload`
- `GET /api/categories`
- `POST /api/admin/categories`
- `GET /api/venues`
- `POST /api/admin/venues`
- `GET /api/events`
- `GET /api/events/{id}`
- `GET /api/organizer/events`
- `POST /api/organizer/events`
- `PUT /api/organizer/events/{id}`
- `POST /api/organizer/events/{id}/image/upload`
- `POST /api/organizer/events/{id}/gallery`
- `DELETE /api/organizer/events/{id}/gallery/{imageId}`
- `DELETE /api/organizer/events/{id}`
- `POST /api/bookings`
- `GET /api/bookings/me`
- `POST /api/reviews`
- `GET /api/reviews/me`
- `GET /api/reviews/manageable`
- `PUT /api/reviews/{reviewId}/reply`
- `PUT /api/reviews/{reviewId}/follow-up`
- `GET /api/events/{eventId}/reviews`
- `GET /api/events/{eventId}/weather`
- `GET /api/venues/country-info?country=Italy`
- `GET /api/admin/stats`

## Feature Checklist

- React pages with routing and dynamic event details
- Redux global authentication state with async thunks
- Controlled forms with validation for auth, catalog management, event management, booking, reviews, and profile image updates
- Listing page with filters and pagination
- Role-based UI and protected routes
- Spring Boot REST API with validation and structured errors
- PostgreSQL persistence with more than eight domain tables
- Inheritance model through `Payment`, `CardPayment`, and `BankTransferPayment`
- JWT authentication and role-based authorization
- Third-party API integrations with Open-Meteo and REST Countries
- Backend integration tests and frontend production build verification
