import {createContext, ReactNode, useCallback, useContext, useEffect, useState} from 'react'
import {Auth} from '../type.ts'
import authService from '../services/authService.ts'
import {SignInRequest} from '../schema.ts'

type AuthContext = {
    auth?: Auth,
    signIn: (request: SignInRequest) => Promise<void>,
    signOut: () => Promise<void>,
    refresh: () => Promise<void>
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

    const signIn = useCallback(async (request: SignInRequest) => {
        const res = await authService.signIn(request)
        setAuth(res)
    }, [])

    const signOut = useCallback(async () => {
        await authService.signOut()
        setAuth(undefined)
    }, [])

    const refresh = useCallback(async () => {
        const res = await authService.refreshToken()
        setAuth(res)
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
