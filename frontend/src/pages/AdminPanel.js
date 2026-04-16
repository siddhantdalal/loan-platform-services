import React, { useState, useEffect } from 'react';
import { loanApi } from '../services/api';

function StatusBadge({ status }) {
  const className = `badge badge-${status.toLowerCase().replace('_', '-')}`;
  return <span className={className}>{status.replace('_', ' ')}</span>;
}

function formatCurrency(amount) {
  return new Intl.NumberFormat('en-US', { style: 'currency', currency: 'USD' }).format(amount);
}

export default function AdminPanel() {
  const [loans, setLoans] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');
  const [success, setSuccess] = useState('');
  const [processingId, setProcessingId] = useState(null);

  useEffect(() => {
    fetchAllLoans();
  }, []);

  const fetchAllLoans = async () => {
    try {
      const response = await loanApi.getAll();
      const allLoans = response.data?.data || [];
      allLoans.sort((a, b) => b.id - a.id);
      setLoans(allLoans);
    } catch (err) {
      setError('Failed to load loan applications.');
    } finally {
      setLoading(false);
    }
  };

  const handleDecision = async (loanId, decision, interestRate) => {
    setProcessingId(loanId);
    setError('');
    setSuccess('');

    try {
      const payload = { decision };
      if (decision === 'APPROVED' && interestRate) {
        payload.interestRate = interestRate;
      }
      await loanApi.processDecision(loanId, payload);
      setSuccess(`Loan #${loanId} has been ${decision.toLowerCase()}.`);
      fetchAllLoans();
    } catch (err) {
      const msg = err.response?.data?.message || `Failed to process loan #${loanId}.`;
      setError(msg);
    } finally {
      setProcessingId(null);
    }
  };

  const pendingLoans = loans.filter((l) => l.status === 'PENDING' || l.status === 'UNDER_REVIEW');
  const processedLoans = loans.filter((l) => l.status === 'APPROVED' || l.status === 'REJECTED');

  if (loading) {
    return (
      <div className="loading">
        <div className="spinner"></div>
        Loading applications...
      </div>
    );
  }

  return (
    <div>
      <div className="page-header">
        <h1>Admin Panel</h1>
        <span className="badge badge-pending">{pendingLoans.length} pending</span>
      </div>

      {error && <div className="alert alert-error">{error}</div>}
      {success && <div className="alert alert-success">{success}</div>}

      <div className="card">
        <div className="card-header">
          <h2>Pending Applications</h2>
        </div>

        {pendingLoans.length === 0 ? (
          <div className="empty-state">
            <p>No pending applications to review.</p>
          </div>
        ) : (
          <div className="table-container">
            <table>
              <thead>
                <tr>
                  <th>ID</th>
                  <th>User ID</th>
                  <th>Amount</th>
                  <th>Term</th>
                  <th>Purpose</th>
                  <th>Est. Rate</th>
                  <th>Status</th>
                  <th>Actions</th>
                </tr>
              </thead>
              <tbody>
                {pendingLoans.map((loan) => (
                  <tr key={loan.id}>
                    <td>#{loan.id}</td>
                    <td>User #{loan.userId}</td>
                    <td><strong>{formatCurrency(loan.amount)}</strong></td>
                    <td>{loan.termMonths}mo</td>
                    <td>{loan.purpose}</td>
                    <td>{loan.interestRate}%</td>
                    <td><StatusBadge status={loan.status} /></td>
                    <td>
                      <div className="action-buttons">
                        <button
                          className="btn btn-success btn-sm"
                          onClick={() => handleDecision(loan.id, 'APPROVED', loan.interestRate)}
                          disabled={processingId === loan.id}
                        >
                          {processingId === loan.id ? '...' : 'Approve'}
                        </button>
                        <button
                          className="btn btn-danger btn-sm"
                          onClick={() => handleDecision(loan.id, 'REJECTED')}
                          disabled={processingId === loan.id}
                        >
                          {processingId === loan.id ? '...' : 'Reject'}
                        </button>
                      </div>
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        )}
      </div>

      {processedLoans.length > 0 && (
        <div className="card" style={{ marginTop: '1rem' }}>
          <div className="card-header">
            <h2>Processed Applications</h2>
          </div>
          <div className="table-container">
            <table>
              <thead>
                <tr>
                  <th>ID</th>
                  <th>User ID</th>
                  <th>Amount</th>
                  <th>Term</th>
                  <th>Interest Rate</th>
                  <th>Status</th>
                </tr>
              </thead>
              <tbody>
                {processedLoans.map((loan) => (
                  <tr key={loan.id}>
                    <td>#{loan.id}</td>
                    <td>User #{loan.userId}</td>
                    <td>{formatCurrency(loan.amount)}</td>
                    <td>{loan.termMonths}mo</td>
                    <td>{loan.interestRate ? `${loan.interestRate}%` : '-'}</td>
                    <td><StatusBadge status={loan.status} /></td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        </div>
      )}
    </div>
  );
}
