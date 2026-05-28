import { CalendarDays, LayoutDashboard, LogOut, ShieldCheck, Ticket } from 'lucide-react';
import { NavLink, Outlet } from 'react-router-dom';
import { useDispatch, useSelector } from 'react-redux';
import { clearCredentials, selectCurrentUser } from '../store/authSlice';
import { canManageEvents, canManagePlatform, getPrimaryRoleLabel } from '../utils/roles';

export function AppLayout() {
  const dispatch = useDispatch();
  const user = useSelector(selectCurrentUser);
  const isOrganizer = canManageEvents(user);
  const isAdmin = canManagePlatform(user);

  return (
    <div className="app">
      <header className="topbar">
        <NavLink className="brand" to="/">
          <CalendarDays size={24} aria-hidden="true" />
          <span>EventHub</span>
        </NavLink>

        <nav className="nav-links" aria-label="Main navigation">
          <NavLink to="/events">Events</NavLink>
          {user && (
            <NavLink to="/dashboard">
              <LayoutDashboard size={18} aria-hidden="true" />
              Dashboard
            </NavLink>
          )}
          {isOrganizer && <NavLink to="/organizer">Organizer</NavLink>}
          {isAdmin && (
            <NavLink to="/admin">
              <ShieldCheck size={18} aria-hidden="true" />
              Admin
            </NavLink>
          )}
        </nav>

        <div className="account-actions">
          {user ? (
            <>
              <span className="user-chip">{user.name} · {getPrimaryRoleLabel(user)}</span>
              <button
                className="icon-button"
                type="button"
                aria-label="Log out"
                title="Log out"
                onClick={() => dispatch(clearCredentials())}
              >
                <LogOut size={18} aria-hidden="true" />
              </button>
            </>
          ) : (
            <>
              <NavLink className="button secondary" to="/login">
                Login
              </NavLink>
              <NavLink className="button primary" to="/register">
                <Ticket size={18} aria-hidden="true" />
                Register
              </NavLink>
            </>
          )}
        </div>
      </header>

      <main className="page-shell">
        <Outlet />
      </main>
    </div>
  );
}
