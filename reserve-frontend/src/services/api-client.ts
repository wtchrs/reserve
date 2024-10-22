import axios, {AxiosError, AxiosInstance, AxiosResponse, InternalAxiosRequestConfig} from 'axios'
import {ErrorResponse} from '../../types/domain'
import {logOnDev} from '../utils/log.ts'
import {getAccessToken, removeAccessToken, setAccessToken} from '../utils/token.ts'
import '../../types/axios.d.ts'

const apiUrl = import.meta.env.VITE_API_URL

const apiClient: AxiosInstance = axios.create({
    baseURL: apiUrl,
})

apiClient.interceptors.request.use((config: InternalAxiosRequestConfig) => {
    logOnDev(`[API] ${config.method?.toUpperCase()} ${config.url} REQUEST`)

    if (config.withAccessToken) {
        const accessToken = getAccessToken()
        if (accessToken) {
            config.headers['Authorization'] = `Bearer ${accessToken}`
        }
    }
    return config
})

apiClient.interceptors.response.use(
    (res: AxiosResponse) => {
        logOnDev(`[API] ${res.config.method?.toUpperCase()} ${res.config.url} RESPONSE ${res.status}`)
        return res
    },
    async (err: AxiosError) => {
        const originalReq = err.config as InternalAxiosRequestConfig
        const errRes = err.response as AxiosResponse<ErrorResponse | any>

        logOnDev(
            `[API] %c${originalReq.method?.toUpperCase()} ${originalReq.url} ERROR ${err.status} - ${errRes.data?.code}`,
            'color: red',
        )

        logOnDev(err)

        if (err.status === 401 && errRes.data?.code === 120 && !originalReq._retry) {
            // Access token expired
            logOnDev('Trying to refresh token...')
            originalReq._retry = true
            try {
                const res = await apiClient.post('/token-refresh', null, {withCredentials: true})
                const accessToken = res.headers['authorization']
                setAccessToken(accessToken)
                return apiClient(originalReq)
            } catch (_err) {
                removeAccessToken()
                return Promise.reject(err)
            }
        }

        if (err.status === 401 && errRes.data?.code === 121) {
            // Refresh token expired
            removeAccessToken()
            return Promise.reject(err)
        }

        return err
    })

export default apiClient
