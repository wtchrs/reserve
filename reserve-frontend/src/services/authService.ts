import client from './api-client'
import {jwtDecode} from 'jwt-decode'
import {isAxiosError} from 'axios'
import {SignInRequest, SignUpRequest} from '../schema'
import {Auth, UserId} from '../type'

abstract class AuthService {
    static async signIn(request: SignInRequest, setError: (error: string) => void) {
        try {
            console.log('AuthService.signIn')
            const res = await client.post('sign-in', request)
            const accessToken = res.headers['authorization']
            const decoded = jwtDecode(accessToken)
            const user = decoded.sub as UserId
            setError('')
            return {user, accessToken} as Auth
        } catch (err) {
            console.log('err', err)
            if (isAxiosError(err)) {
                if (err.response && err.response.status === 401) {
                    setError('Invalid username or password.')
                } else {
                    setError('Something went wrong. Please try again later.')
                }
            }
        }
    }

    static async signUp(request: SignUpRequest, setError: (error: string) => void) {
        try {
            await client.post('/sign-up', request)
            return true
        } catch (err) {
            console.log(err)
            if (isAxiosError(err)) {
                if (err.response && err.response.status === 409) {
                    setError('Username is already taken.')
                } else {
                    setError('Something went wrong. Please try again later.')
                }
            }
        }
    }
}

export default AuthService
