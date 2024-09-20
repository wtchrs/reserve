import client from './api-client'
import {Auth, User} from '../type'
import {DeleteUserRequest, UpdatePasswordRequest, UpdateUserRequest} from '../schema'

abstract class UserService {
    static async getUser(username: string) {
        const res = await client.get<User>(`/users/${username}`)
        return res.data
    }

    static async updateUser({accessToken}: Auth, request: UpdateUserRequest) {
        await client.put('/users', request, {headers: {Authorization: `Bearer ${accessToken}`}})
    }

    static async updatePassword({accessToken}: Auth, request: UpdatePasswordRequest) {
        await client.put('/users/password', request, {headers: {Authorization: `Bearer ${accessToken}`}})
    }

    static async deleteUser({accessToken}: Auth, request: DeleteUserRequest) {
        await client.delete('/users', {
            headers: {Authorization: `Bearer ${accessToken}`},
            data: request
        })
    }
}

export default UserService
