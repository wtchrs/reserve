import {createContext, Dispatch, ReactNode, useContext, useState} from 'react'
import {Auth} from '../type.ts'

type AuthContext = { auth?: Auth, setAuth: Dispatch<Auth> }
const authContext = createContext<AuthContext>({} as AuthContext)

export function AuthProvider({children}: { children: ReactNode }) {
    const [auth, setAuth] = useState<Auth>()
    return (
        <authContext.Provider value={{auth, setAuth}}>
            {children}
        </authContext.Provider>
    )
}

export function useAuth() {
    const context = useContext(authContext)
    if (!context) throw new Error('useUser must be used in UserContext.')
    return context
}
