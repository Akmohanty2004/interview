import axios from 'axios';

const API_BASE_URL = import.meta.env.VITE_API_URL || 'http://localhost:8082';

const api = axios.create({
  baseURL: API_BASE_URL,
  headers: {
    'Content-Type': 'application/json',
  },
});

// Request interceptor to attach JWT token
api.interceptors.request.use(
  (config) => {
    const token = localStorage.getItem('interviewprep_token');
    if (token) {
      config.headers.Authorization = `Bearer ${token}`;
    }
    return config;
  },
  (error) => Promise.reject(error)
);

// Response interceptor to handle session expiration
api.interceptors.response.use(
  (response) => response,
  (error) => {
    if (error.response && error.response.status === 401) {
      // Clear token if expired or unauthorized
      if (localStorage.getItem('interviewprep_token')) {
        localStorage.removeItem('interviewprep_token');
        localStorage.removeItem('interviewprep_user');
        window.location.href = '/login?expired=true';
      }
    }
    return Promise.reject(error);
  }
);

// Auth endpoints
export const authApi = {
  login: (credentials) => api.post('/api/auth/login', credentials),
  register: (userData) => api.post('/api/auth/register', userData),
  verifyOtp: (data) => api.post('/api/auth/verify-otp', data),
  resendOtp: (email) => api.post(`/api/auth/resend-otp?email=${encodeURIComponent(email)}`),
  getCurrentUser: () => api.get('/api/auth/me'),
  changePassword: (data) => api.post('/api/auth/change-password', data),
};

// Subjects & Topics
export const subjectApi = {
  getAllActive: () => api.get('/api/subjects'),
  getById: (id) => api.get(`/api/subjects/${id}`),
  getTopics: (subjectId) => api.get(`/api/subjects/${subjectId}/topics`),
  getNotes: (subjectId) => api.get(`/api/subjects/${subjectId}/notes`),
};

// Questions
export const questionApi = {
  getQuestions: (params) => api.get('/api/questions', { params }),
  getById: (id) => api.get(`/api/questions/${id}`),
};

// Mock Tests
export const mockTestApi = {
  start: (config) => api.post('/api/mock/start', config),
  getById: (id) => api.get(`/api/mock/${id}`),
  saveAnswer: (testId, answerData) => api.post(`/api/mock/${testId}/answer`, answerData),
  submit: (submissionData) => api.post('/api/mock/submit', submissionData),
  getResult: (testId) => api.get(`/api/mock/results/${testId}`),
  getHistory: () => api.get('/api/mock/history'),
};

// Analytics
export const analyticsApi = {
  getStudentDashboard: () => api.get('/api/analytics/dashboard'),
  getWeakTopics: () => api.get('/api/analytics/weak-topics'),
  getSubjectPerformance: () => api.get('/api/analytics/subjects'),
};

// Bookmarks
export const bookmarkApi = {
  toggle: (questionId, notes) => api.post(`/api/bookmarks/toggle/${questionId}`, { notes }),
  getAll: () => api.get('/api/bookmarks'),
};

// Daily Challenge
export const dailyChallengeApi = {
  getToday: () => api.get('/api/daily-challenge/today'),
};

// Admin
export const adminApi = {
  getDashboard: () => api.get('/api/admin/dashboard'),
  getStudents: () => api.get('/api/admin/students'),
  getInterviews: () => api.get('/api/admin/interviews'),
  getStudentInterviews: (studentId) => api.get(`/api/admin/students/${studentId}/interviews`),
  createSubject: (data) => api.post('/api/admin/subjects', data),
  updateSubject: (id, data) => api.put(`/api/admin/subjects/${id}`, data),
  deleteSubject: (id) => api.delete(`/api/admin/subjects/${id}`),
  createTopic: (subjectId, data) => api.post(`/api/admin/subjects/${subjectId}/topics`, data),
  deleteTopic: (topicId) => api.delete(`/api/admin/topics/${topicId}`),
  createSubjectNote: (subjectId, data) => api.post(`/api/admin/subjects/${subjectId}/notes`, data),
  deleteSubjectNote: (noteId) => api.delete(`/api/admin/subjects/notes/${noteId}`),
  createQuestion: (data) => api.post('/api/admin/questions', data),
  updateQuestion: (id, data) => api.put(`/api/admin/questions/${id}`, data),
  deleteQuestion: (id) => api.delete(`/api/admin/questions/${id}`),
};

// AI Chatbot API
export const aiApi = {
  chat: (prompt) => api.post('/api/ai/chat', { prompt }),
};

export default api;
