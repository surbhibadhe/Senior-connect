// API Configuration
// When VITE_API_BASE_URL is left empty or not set, requests use relative paths ('')
// which are proxied by Vite dev server or reverse proxy to the Spring Boot backend.
export const API_BASE = (import.meta.env.VITE_API_BASE_URL as string | undefined)?.replace(/\/$/, '') || ''
