import {createContext, Dispatch, ReactNode, useCallback, useContext, useEffect, useState} from 'react'
import {Auth} from '../type.ts'
import authService from '../services/authService.ts'
import {SignInRequest} from '../schema.ts'
import {isAxiosError} from 'axios'

type AuthContext = {
    auth?: Auth,
    signIn: (request: SignInRequest, setError?: Dispatch<string>) => Promise<void>,
    signOut: (setError?: Dispatch<string>) => Promise<void>,
    refresh: (setError?: Dispatch<string>) => Promise<void>
}

const authContext = createContext<AuthContext>({} as AuthContext)

export function AuthProvider({children}: { children: ReactNode }) {
    const [auth, setAuth] = useState<Auth>()

    useEffect(() => {
        authService.refreshToken()
            .then(res => {
                setAuth(res)
            })
    }, [])

    const signIn = useCallback(async (request: SignInRequest, setError?: Dispatch<string>) => {
        try {
            const res = await authService.signIn(request)
            setAuth(res)
        } catch (err) {
            console.log('err', err)
            if (setError) {
                if (isAxiosError(err) && err.response && err.response.status === 401) {
                    setError('Invalid username or password.')
                } else {
                    setError('Something went wrong. Please try again later.')
                }
            }
            return Promise.reject(err)
        }
    }, [])

    const signOut = useCallback(async (setError?: Dispatch<string>) => {
        try {
            await authService.signOut()
            setAuth(undefined)
        } catch (err) {
            if (setError) setError('Something went wrong. Please try again later.')
            return Promise.reject(err)
        }
    }, [])

    const refresh = useCallback(async (setError?: Dispatch<string>) => {
        try {
            const res = await authService.refreshToken()
            setAuth(res)
        } catch (err) {
            if (setError) setError('Something went wrong. Please try again later.')
            return Promise.reject(err)
        }
    }, [])

    return (
        <authContext.Provider value={{auth, signIn, signOut, refresh: refresh}}>
            {children}
        </authContext.Provider>
    )
}

export function useAuth() {
    const context = useContext(authContext)
    if (!context) throw new Error('useUser must be used in UserContext.')
    return context
}
