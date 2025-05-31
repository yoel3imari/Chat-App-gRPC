const ACCESS_TOKEN_KEY = 'access_token'
const REFRESH_TOKEN_KEY = 'refresh_token'

export default {
  // Save tokens
  setTokens(accessToken: string, refreshToken?: string, rememberMe = true) {
    const storage = rememberMe ? localStorage : sessionStorage
    storage.setItem(ACCESS_TOKEN_KEY, accessToken)
    if (refreshToken) {
      storage.setItem(REFRESH_TOKEN_KEY, refreshToken)
    }
  },

  // Get access token
  getAccessToken(): string | null {
    return localStorage.getItem(ACCESS_TOKEN_KEY) || sessionStorage.getItem(ACCESS_TOKEN_KEY)
  },

  // Get refresh token
  getRefreshToken(): string | null {
    return localStorage.getItem(REFRESH_TOKEN_KEY) || sessionStorage.getItem(REFRESH_TOKEN_KEY)
  },

  // Clear tokens
  clearTokens() {
    localStorage.removeItem(ACCESS_TOKEN_KEY)
    localStorage.removeItem(REFRESH_TOKEN_KEY)
    sessionStorage.removeItem(ACCESS_TOKEN_KEY)
    sessionStorage.removeItem(REFRESH_TOKEN_KEY)
  },
}
