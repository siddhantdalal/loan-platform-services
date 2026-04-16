import React, { useState, useEffect, useCallback } from 'react';
import { Link } from 'react-router-dom';
import { loanApi } from '../services/api';
import { useAuth } from '../context/AuthContext';

function StatusBadge({ status }) {
  const className = `badge badge-${status.toLowerCase().replace('_', '-')}`;
  return <span className={className}>{status.replace('_', ' ')}</span>;
}

function formatCurrency(amount) {
  return new Intl.NumberFormat('en-US', { style: 'currency', currency: 'USD' }).format(amount);
}

function formatDate(dateString) {
  if (!dateString) return '-';
  return new Date(dateString).toLocaleDateString('en-US', {
    year: 'numeric',
    month: 'short',
    day: 'numeric',
  });
}

export default function Dashboard() {
  const { user } = useAuth();
  const [loans, setLoans] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');

  const fetchLoans = useCallback(async () => {
    if (!user?.id) return;
    try {
      const response = await loanApi.getByUser(user.id);
      setLoans(response.data.data || []);
    } catch (err) {
      setError('Failed to load loan applications.');
    } finally {
      setLoading(false);
    }
  }, [user]);

  useEffect(() => {
    fetchLoans();
  }, [fetchLoans]);

  const stats = {
    total: loans.length,
    pending: loans.filter((l) => l.status === 'PENDING' || l.status === 'UNDER_REVIEW').length,
    approved: loans.filter((l) => l.status === 'APPROVED').length,
    rejected: loans.filter((l) => l.status === 'REJECTED').length,
  };

  const totalAmount = loans.reduce((sum, l) => sum + (l.amount || 0), 0);

  if (loading) {
    return (
      <div className="loading">
        <div className="spinner"></div>
        Loading your dashboard...
      </div>
    );
  }

  return (
    <div>
      <div className="page-header">
        <h1>Dashboard</h1>
        <Link to="/apply" className="btn btn-primary">Apply for Loan</Link>
      </div>

      {error && <div className="alert alert-error">{error}</div>}

      <div className="stats-grid">
        <div className="stat-card">
          <div className="stat-value">{stats.total}</div>
          <div className="stat-label">Total Applications</div>
        </div>
        <div className="stat-card">
          <div className="stat-value" style={{ color: '#f59e0b' }}>{stats.pending}</div>
          <div className="stat-label">Pending Review</div>
        </div>
        <div className="stat-card">
          <div className="stat-value" style={{ color: '#10b981' }}>{stats.approved}</div>
          <div className="stat-label">Approved</div>
        </div>
        <div className="stat-card">
          <div className="stat-value">{formatCurrency(totalAmount)}</div>
          <div className="stat-label">Total Requested</div>
        </div>
      </div>

      <div className="card">
        <div className="card-header">
          <h2>Your Loan Applications</h2>
        </div>

        {loans.length === 0 ? (
          <div className="empty-state">
            <p>You haven't applied for any loans yet.</p>
            <Link to="/apply" className="btn btn-primary">Submit Your First Application</Link>
          </div>
        ) : (
          <div className="table-container">
            <table>
              <thead>
                <tr>
                  <th>ID</th>
                  <th>Amount</th>
                  <th>Term</th>
                  <th>Purpose</th>
                  <th>Interest Rate</th>
                  <th>Status</th>
                  <th>Applied On</th>
                </tr>
              </thead>
              <tbody>
                {loans.map((loan) => (
                  <tr key={loan.id}>
                    <td>#{loan.id}</td>
                    <td><strong>{formatCurrency(loan.amount)}</strong></td>
                    <td>{loan.termMonths} months</td>
                    <td>{loan.purpose}</td>
                    <td>{loan.interestRate ? `${loan.interestRate}%` : '-'}</td>
                    <td><StatusBadge status={loan.status} /></td>
                    <td>{formatDate(loan.createdAt)}</td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        )}
      </div>
    </div>
  );
}
