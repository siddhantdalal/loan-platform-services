import React from 'react';
import { Link, useLocation } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';

export default function Navbar() {
  const { user, isAuthenticated, logout } = useAuth();
  const location = useLocation();

  const isActive = (path) => location.pathname === path ? 'active' : '';

  return (
    <nav className="navbar">
      <Link to="/" className="navbar-brand">
        Loan Platform
      </Link>

      <div className="navbar-links">
        {isAuthenticated ? (
          <>
            <Link to="/dashboard" className={isActive('/dashboard')}>Dashboard</Link>
            <Link to="/apply" className={isActive('/apply')}>Apply for Loan</Link>
            <Link to="/notifications" className={isActive('/notifications')}>Notifications</Link>
            {user?.role === 'ADMIN' && (
              <Link to="/admin" className={isActive('/admin')}>Admin Panel</Link>
            )}
            <span className="navbar-user">{user?.firstName} {user?.lastName}</span>
            <button onClick={logout}>Logout</button>
          </>
        ) : (
          <>
            <Link to="/login" className={isActive('/login')}>Login</Link>
            <Link to="/register" className={isActive('/register')}>Register</Link>
          </>
        )}
      </div>
    </nav>
  );
}
