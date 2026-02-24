import axios from "axios";
import { getToken } from "../auth/authStorage";

// Create an Axios instance with a base URL and default headers, and set up an interceptor to include the authentication token in the Authorization header for all outgoing requests if a token is available, allowing the application to communicate with the backend API while ensuring that authenticated requests are properly authorized when users interact with the application and trigger API calls to protected endpoints
const apiClient = axios.create({
  baseURL: import.meta.env.VITE_API_BASE_URL || "http://localhost:8080",
  headers: { "Content-Type": "application/json" },
});

apiClient.interceptors.request.use((config) => {
  const token = getToken();
  if (token) {
    config.headers = config.headers || {};
    config.headers.Authorization = `Bearer ${token}`;
  }
  return config;
});

export default apiClient;
