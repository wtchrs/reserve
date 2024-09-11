declare module 'axios' {
    interface AxiosRequestConfig {
        urlParams?: Record<string, string>;
    }
}

export type AuthUser = {
    userId: string
    username: string
    nickname: string
}

export type Auth = {
    user: AuthUser
    accessToken: string
}

export type User = {
    username: string
    nickname: string
    description: string
    signUpDate: string
}
