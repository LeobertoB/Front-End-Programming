import { createBrowserRouter, RouterProvider } from 'react-router-dom';
import { ProtectedRoute } from './router/ProtectedRoute';
import { AppLayout } from './components/AppLayout';
import { AdminDashboardPage } from './pages/AdminDashboardPage';
import { DashboardPage } from './pages/DashboardPage';
import { EventDetailPage } from './pages/EventDetailPage';
import { EventsPage } from './pages/EventsPage';
import { HomePage } from './pages/HomePage';
import { LoginPage } from './pages/LoginPage';
import { NotFoundPage } from './pages/NotFoundPage';
import { OrganizerDashboardPage } from './pages/OrganizerDashboardPage';
import { RegisterPage } from './pages/RegisterPage';

const router = createBrowserRouter([
  {
    path: '/',
    element: <AppLayout />,
    children: [
      { index: true, element: <HomePage /> },
      { path: 'events', element: <EventsPage /> },
      { path: 'events/:eventId', element: <EventDetailPage /> },
      { path: 'login', element: <LoginPage /> },
      { path: 'register', element: <RegisterPage /> },
      {
        element: <ProtectedRoute />,
        children: [{ path: 'dashboard', element: <DashboardPage /> }],
      },
      {
        element: <ProtectedRoute roles={['ROLE_ORGANIZER', 'ROLE_ADMIN']} />,
        children: [{ path: 'organizer', element: <OrganizerDashboardPage /> }],
      },
      {
        element: <ProtectedRoute roles={['ROLE_ADMIN']} />,
        children: [{ path: 'admin', element: <AdminDashboardPage /> }],
      },
      { path: '*', element: <NotFoundPage /> },
    ],
  },
]);

export function App() {
  return <RouterProvider router={router} />;
}
