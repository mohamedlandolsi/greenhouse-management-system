import axios, { AxiosInstance, AxiosError, InternalAxiosRequestConfig } from 'axios';
import { ApiError } from '@/types';

// Create axios instance with default config
const apiClient: AxiosInstance = axios.create({
  baseURL: process.env.NEXT_PUBLIC_API_URL || 'http://localhost:8080/api',
  timeout: 30000,
  headers: {
    'Content-Type': 'application/json',
  },
});

// Request interceptor for adding auth token
apiClient.interceptors.request.use(
  (config: InternalAxiosRequestConfig) => {
    // Get token from localStorage if auth is enabled
    if (typeof window !== 'undefined' && process.env.NEXT_PUBLIC_AUTH_ENABLED === 'true') {
      const token = localStorage.getItem('accessToken');
      if (token && config.headers) {
        config.headers.Authorization = `Bearer ${token}`;
      }
    }
    
    // Log request in debug mode
    if (process.env.NEXT_PUBLIC_DEBUG_MODE === 'true') {
      console.log(`[API] ${config.method?.toUpperCase()} ${config.url}`, config.data || '');
    }
    
    return config;
  },
  (error) => {
    return Promise.reject(error);
  }
);

// Response interceptor for error handling
apiClient.interceptors.response.use(
  (response) => {
    // Log response in debug mode
    if (process.env.NEXT_PUBLIC_DEBUG_MODE === 'true') {
      console.log(`[API] Response ${response.status}`, response.data);
    }
    return response;
  },
  async (error: AxiosError<ApiError>) => {
    const originalRequest = error.config as InternalAxiosRequestConfig & { _retry?: boolean };
    
    // Handle 401 - Token refresh
    if (error.response?.status === 401 && !originalRequest._retry) {
      originalRequest._retry = true;
      
      if (process.env.NEXT_PUBLIC_AUTH_ENABLED === 'true') {
        try {
          const refreshToken = localStorage.getItem('refreshToken');
          if (refreshToken) {
            const response = await axios.post(
              `${process.env.NEXT_PUBLIC_API_URL}/auth/refresh`,
              { refreshToken }
            );
            
            const { token } = response.data;
            localStorage.setItem('accessToken', token);
            
            if (originalRequest.headers) {
              originalRequest.headers.Authorization = `Bearer ${token}`;
            }
            
            return apiClient(originalRequest);
          }
        } catch (refreshError) {
          // Refresh failed, clear tokens and redirect to login
          localStorage.removeItem('accessToken');
          localStorage.removeItem('refreshToken');
          window.location.href = '/login';
          return Promise.reject(refreshError);
        }
      }
    }
    
    // Log error in debug mode
    if (process.env.NEXT_PUBLIC_DEBUG_MODE === 'true') {
      console.error('[API] Error:', error.response?.data || error.message);
    }
    
    // Format error response
    const apiError: ApiError = {
      message: error.response?.data?.message || error.message || 'Une erreur est survenue',
      status: error.response?.status || 500,
      timestamp: new Date().toISOString(),
      path: originalRequest?.url,
      errors: error.response?.data?.errors,
    };
    
    return Promise.reject(apiError);
  }
);

export default apiClient;

// Helper functions for common HTTP methods
export const api = {
  get: <T>(url: string, params?: Record<string, unknown>) => 
    apiClient.get<T>(url, { params }).then(res => res.data),
    
  post: <T>(url: string, data?: unknown) => 
    apiClient.post<T>(url, data).then(res => res.data),
    
  put: <T>(url: string, data?: unknown) => 
    apiClient.put<T>(url, data).then(res => res.data),
    
  patch: <T>(url: string, data?: unknown) => 
    apiClient.patch<T>(url, data).then(res => res.data),
    
  delete: <T>(url: string) => 
    apiClient.delete<T>(url).then(res => res.data),
};
