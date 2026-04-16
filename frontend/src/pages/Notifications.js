import React, { useState, useEffect } from 'react';
import { notificationApi } from '../services/api';
import { useAuth } from '../context/AuthContext';

function formatDate(dateString) {
  if (!dateString) return '';
  return new Date(dateString).toLocaleString('en-US', {
    month: 'short',
    day: 'numeric',
    year: 'numeric',
    hour: '2-digit',
    minute: '2-digit',
  });
}

export default function Notifications() {
  const { user } = useAuth();
  const [notifications, setNotifications] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');

  useEffect(() => {
    fetchNotifications();
  }, [user]);

  const fetchNotifications = async () => {
    if (!user?.id) return;
    try {
      const response = await notificationApi.getByUser(user.id);
      const data = response.data.data || [];
      data.sort((a, b) => b.id - a.id);
      setNotifications(data);
    } catch (err) {
      setError('Failed to load notifications.');
    } finally {
      setLoading(false);
    }
  };

  if (loading) {
    return (
      <div className="loading">
        <div className="spinner"></div>
        Loading notifications...
      </div>
    );
  }

  return (
    <div>
      <div className="page-header">
        <h1>Notifications</h1>
        <button className="btn btn-outline btn-sm" onClick={fetchNotifications}>
          Refresh
        </button>
      </div>

      {error && <div className="alert alert-error">{error}</div>}

      {notifications.length === 0 ? (
        <div className="card">
          <div className="empty-state">
            <p>No notifications yet. They will appear here as events happen in your account.</p>
          </div>
        </div>
      ) : (
        notifications.map((notification) => (
          <div key={notification.id} className={`notification-item type-${notification.type}`}>
            <div className="notification-subject">{notification.subject}</div>
            <div className="notification-content">{notification.content}</div>
            <div className="notification-meta">
              <span>{notification.channel}</span>
              <span>{notification.status}</span>
              <span>{formatDate(notification.createdAt)}</span>
            </div>
          </div>
        ))
      )}
    </div>
  );
}
