import React, { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { loanApi } from '../services/api';
import { useAuth } from '../context/AuthContext';

export default function ApplyLoan() {
  const { user } = useAuth();
  const navigate = useNavigate();
  const [form, setForm] = useState({
    amount: '',
    termMonths: '12',
    purpose: '',
  });
  const [error, setError] = useState('');
  const [loading, setLoading] = useState(false);

  const handleChange = (e) => {
    setForm({ ...form, [e.target.name]: e.target.value });
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    setError('');
    setLoading(true);

    try {
      await loanApi.submit({
        userId: user.id,
        amount: parseFloat(form.amount),
        termMonths: parseInt(form.termMonths, 10),
        purpose: form.purpose,
      });
      navigate('/dashboard');
    } catch (err) {
      const msg = err.response?.data?.message || 'Failed to submit loan application.';
      setError(msg);
    } finally {
      setLoading(false);
    }
  };

  // Estimate monthly payment
  const estimatePayment = () => {
    const amount = parseFloat(form.amount);
    const months = parseInt(form.termMonths, 10);
    if (!amount || !months) return null;
    const rate = 0.06 / 12; // approximate 6% annual
    const payment = (amount * rate * Math.pow(1 + rate, months)) / (Math.pow(1 + rate, months) - 1);
    return payment;
  };

  const monthlyPayment = estimatePayment();

  return (
    <div className="form-container">
      <div className="card" style={{ padding: '2rem' }}>
        <h1 style={{ fontSize: '1.5rem', fontWeight: 700, marginBottom: '0.5rem' }}>
          Apply for a Loan
        </h1>
        <p style={{ color: '#6b7280', marginBottom: '1.5rem' }}>
          Fill out the form below to submit your loan application.
        </p>

        {error && <div className="alert alert-error">{error}</div>}

        <form onSubmit={handleSubmit}>
          <div className="form-group">
            <label htmlFor="amount">Loan Amount (USD)</label>
            <input
              type="number"
              id="amount"
              name="amount"
              value={form.amount}
              onChange={handleChange}
              placeholder="e.g., 50000"
              min="1000"
              max="10000000"
              step="100"
              required
            />
          </div>

          <div className="form-group">
            <label htmlFor="termMonths">Loan Term</label>
            <select
              id="termMonths"
              name="termMonths"
              value={form.termMonths}
              onChange={handleChange}
              required
            >
              <option value="6">6 months</option>
              <option value="12">12 months (1 year)</option>
              <option value="24">24 months (2 years)</option>
              <option value="36">36 months (3 years)</option>
              <option value="48">48 months (4 years)</option>
              <option value="60">60 months (5 years)</option>
              <option value="120">120 months (10 years)</option>
              <option value="180">180 months (15 years)</option>
              <option value="240">240 months (20 years)</option>
              <option value="360">360 months (30 years)</option>
            </select>
          </div>

          <div className="form-group">
            <label htmlFor="purpose">Purpose</label>
            <textarea
              id="purpose"
              name="purpose"
              value={form.purpose}
              onChange={handleChange}
              placeholder="Describe why you need this loan..."
              required
            />
          </div>

          {monthlyPayment && (
            <div className="alert alert-info" style={{ marginBottom: '1.25rem' }}>
              Estimated monthly payment: <strong>
                {new Intl.NumberFormat('en-US', { style: 'currency', currency: 'USD' }).format(monthlyPayment)}
              </strong>
              <span style={{ fontSize: '0.75rem', display: 'block', marginTop: '0.25rem' }}>
                Based on estimated 6% annual interest rate. Actual rate may vary.
              </span>
            </div>
          )}

          <button type="submit" className="btn btn-primary btn-block" disabled={loading}>
            {loading ? 'Submitting...' : 'Submit Application'}
          </button>
        </form>
      </div>
    </div>
  );
}
