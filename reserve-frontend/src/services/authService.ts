import client from './api-client'
import {jwtDecode} from 'jwt-decode'
import {SignInRequest, SignUpRequest} from '../schema'
import {Auth, AuthUser} from '../type'

type TokenDecoded = { sub: string, username: string, nickname: string }

abstract class AuthService {
    static extractAuth(token: string) {
        const decoded = jwtDecode<TokenDecoded>(token)
        const user: AuthUser = {userId: decoded.sub, username: decoded.username, nickname: decoded.nickname}
        return {user, accessToken: token} as Auth
    }

    static async signIn(request: SignInRequest) {
        const res = await client.post('/sign-in', request)
        const accessToken = res.headers['authorization']
        return this.extractAuth(accessToken)
    }

    static async signOut() {
        await client.post('/sign-out')
    }

    static async signUp(request: SignUpRequest) {
        await client.post('/sign-up', request)
        return true
    }

    static async refreshToken() {
        const res = await client.post('/token-refresh')
        const accessToken = res.headers['authorization']
        const decoded = jwtDecode<TokenDecoded>(accessToken)
        const user: AuthUser = {userId: decoded.sub, username: decoded.username, nickname: decoded.nickname}
        return {user, accessToken} as Auth
    }
}

export default AuthService
