export interface LoginRequest {
    email: string
    password: string
}

export interface LoginResponse {
    token: string
    refreshToken: string
}

export interface TokenRefreshRequest {
    refreshToken: string
}

export interface TokenRefreshResponse {
    accessToken: string
    refreshToken: string
}