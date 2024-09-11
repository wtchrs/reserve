import client from './api-client'
import {Auth, User} from '../type'
import {UpdatePasswordRequest, UpdateUserRequest} from '../schema'

abstract class UserService {
    static async getUser(username: string) {
        const res = await client.get<User>(`/users/${username}`)
        return res.data
    }

    static async updateUser({accessToken}: Auth, request: UpdateUserRequest) {
        await client.put('/users', request, {headers: {Authorization: accessToken}})
    }

    static async updatePassword({accessToken}: Auth, request: UpdatePasswordRequest) {
        await client.put('/users/password', request, {headers: {Authorization: accessToken}})
    }

    static async deactivateAccount({accessToken}: Auth) {
        await client.delete('/users', {headers: {Authorization: accessToken}})
    }
}

export default UserService
