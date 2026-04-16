import axios from 'axios';

const API_BASE_URL = process.env.REACT_APP_API_URL || 'http://localhost:8080';

const api = axios.create({
  baseURL: API_BASE_URL,
  headers: {
    'Content-Type': 'application/json',
  },
});

// Attach JWT token to every request
api.interceptors.request.use((config) => {
  const token = localStorage.getItem('token');
  if (token) {
    config.headers.Authorization = `Bearer ${token}`;
  }
  return config;
});

// Handle 401 responses globally
api.interceptors.response.use(
  (response) => response,
  (error) => {
    if (error.response && error.response.status === 401) {
      localStorage.removeItem('token');
      localStorage.removeItem('user');
      window.location.href = '/login';
    }
    return Promise.reject(error);
  }
);

// Auth APIs
export const authApi = {
  register: (data) => api.post('/api/auth/register', data),
  login: (data) => api.post('/api/auth/login', data),
};

// User APIs
export const userApi = {
  getById: (id) => api.get(`/api/users/${id}`),
  getCurrent: () => api.get('/api/users/me'),
  update: (id, data) => api.put(`/api/users/${id}`, data),
};

// Loan APIs
export const loanApi = {
  submit: (data) => api.post('/api/loans', data),
  getAll: () => api.get('/api/loans'),
  getById: (id) => api.get(`/api/loans/${id}`),
  getByUser: (userId) => api.get(`/api/loans/user/${userId}`),
  processDecision: (id, data) => api.put(`/api/loans/${id}/decision`, data),
};

// Notification APIs
export const notificationApi = {
  getByUser: (userId) => api.get(`/api/notifications/user/${userId}`),
};

export default api;
