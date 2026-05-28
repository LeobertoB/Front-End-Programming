export const ROLES = {
  USER: 'ROLE_USER',
  ORGANIZER: 'ROLE_ORGANIZER',
  ADMIN: 'ROLE_ADMIN',
};

export function hasRole(user, role) {
  return Boolean(user?.roles?.includes(role));
}

export function canManageEvents(user) {
  return hasRole(user, ROLES.ORGANIZER) || hasRole(user, ROLES.ADMIN);
}

export function canManagePlatform(user) {
  return hasRole(user, ROLES.ADMIN);
}

export function getPrimaryRoleLabel(user) {
  if (canManagePlatform(user)) return 'Admin';
  if (canManageEvents(user)) return 'Organizer';
  if (hasRole(user, ROLES.USER)) return 'User';
  return 'Guest';
}
