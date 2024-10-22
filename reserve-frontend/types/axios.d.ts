import 'axios'

declare module 'axios' {
    export interface AxiosRequestConfig {
        urlParams?: Record<string, string>
        withAccessToken?: boolean
    }

    export interface InternalAxiosRequestConfig {
        urlParams?: Record<string, string>
        withAccessToken?: boolean
        _retry?: boolean
    }
}
