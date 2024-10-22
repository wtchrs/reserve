import {createContext, ReactNode, useCallback, useContext, useEffect, useState} from 'react'
import type {Auth} from '../../types/domain.d.ts'
import authService from '../services/authService.ts'
import {SignInRequest} from '../schema.ts'
import {logOnDev} from '../utils/log.ts'
import {getAccessToken,/*getAccessToken,*/ removeAccessToken, setAccessToken} from '../utils/token.ts'

type AuthContext = {
    auth?: Auth,
    signIn: (request: SignInRequest) => Promise<void>,
    signOut: () => Promise<void>,
}

const authContext = createContext<AuthContext>({} as AuthContext)

export function AuthProvider({children}: { children: ReactNode }) {
    const [auth, setAuth] = useState<Auth>()

    const handleAuthChange = useCallback((event: CustomEvent<string | null>) => {
        logOnDev('authChanged', event.detail)

        if (event.detail) {
            setAuth(authService.extractAuth(event.detail))
        } else {
            setAuth(undefined)
        }
    }, [])

    useEffect(() => {
        logOnDev('AuthProvider mounted')

        if (getAccessToken()) {
            authService.refreshToken().then(res => setAccessToken(res.accessToken))
        }
        window.addEventListener('authChanged', handleAuthChange as EventListener)
    }, [handleAuthChange])

    const signIn = useCallback(async (request: SignInRequest) => {
        const res = await authService.signIn(request)
        setAccessToken(res.accessToken)
    }, [])

    const signOut = useCallback(async () => {
        await authService.signOut()
        removeAccessToken()
    }, [])

    return (
        <authContext.Provider value={{auth, signIn, signOut}}>
            {children}
        </authContext.Provider>
    )
}

export function useAuth() {
    const context = useContext(authContext)
    if (!context) throw new Error('useUser must be used in UserContext.')
    return context
}
