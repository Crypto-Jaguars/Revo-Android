import axios from "axios";
import AsyncStorage from "@react-native-async-storage/async-storage";

const API_BASE_URL = "http://127.0.0.1:8000/api";

const api = axios.create({ baseURL: API_BASE_URL });

// Attach token to each request
api.interceptors.request.use(
  async (config) => {
    const token = await AsyncStorage.getItem("access");
    if (token) {
      config.headers.Authorization = `Bearer ${token}`;
    }
    return config;
  },
  (error) => Promise.reject(error)
);

// Refresh token logic
api.interceptors.response.use(
  (response) => response,
  async (error) => {
    if (error.response?.status === 401) {
      const refresh = await AsyncStorage.getItem("refresh");

      if (refresh) {
        try {
          const refreshResponse = await axios.post(`${API_BASE_URL}/refresh/`, {
            refresh,
          });

          const newAccess = refreshResponse.data.access;
          await AsyncStorage.setItem("access", newAccess);

          // Retry the failed request
          error.config.headers.Authorization = `Bearer ${newAccess}`;
          return axios(error.config);
        } catch (refreshError) {
          await AsyncStorage.multiRemove(["access", "refresh"]);
          // Optionally navigate to login screen here
        }
      }
    }
    return Promise.reject(error);
  }
);

export default api;
