import axios from "axios";
import { clearTokens, getAccessToken, getRefreshToken, setTokens } from "./auth";

export const api = axios.create({
  baseURL: import.meta.env.VITE_API_URL ?? "http://localhost:3000/api",
  withCredentials: true,
});

api.interceptors.request.use((config) => {
  const token = getAccessToken();
  if (token) {
    config.headers.Authorization = `Bearer ${token}`;
  }
  return config;
});

let refreshing: Promise<string | null> | null = null;

api.interceptors.response.use(
  (response) => response,
  async (error) => {
    const original = error.config;
    if (error.response?.status !== 401 || original?._retry) {
      return Promise.reject(error);
    }
    original._retry = true;
    refreshing ??= api
      .post("/auth/refresh", { refreshToken: getRefreshToken() })
      .then((response) => {
        const accessToken = response.data?.data?.accessToken ?? response.data?.accessToken;
        const refreshToken = response.data?.data?.refreshToken ?? response.data?.refreshToken;
        if (!accessToken) return null;
        setTokens(accessToken, refreshToken);
        return accessToken as string;
      })
      .catch(() => null)
      .finally(() => {
        refreshing = null;
      });
    const token = await refreshing;
    if (!token) {
      clearTokens();
      window.location.assign("/login");
      return Promise.reject(error);
    }
    original.headers.Authorization = `Bearer ${token}`;
    return api(original);
  },
);

export async function fetchOrFallback<T>(path: string, fallback: T, params?: Record<string, unknown>): Promise<T> {
  try {
    const response = await api.get(path, { params });
    return response.data?.data ?? response.data ?? fallback;
  } catch {
    return fallback;
  }
}

export async function mutateOrFallback<T>(request: Promise<unknown>, fallback: T): Promise<T> {
  try {
    const response = await request;
    if (typeof response === "object" && response && "data" in response) {
      return (response as { data?: { data?: T } | T }).data && "data" in ((response as { data?: unknown }).data as object)
        ? ((response as { data: { data: T } }).data.data)
        : ((response as { data: T }).data);
    }
    return fallback;
  } catch {
    return fallback;
  }
}
