import client from './api-client'
import type {User} from '../../types/domain.d.ts'
import {DeleteUserRequest, UpdatePasswordRequest, UpdateUserRequest} from '../schema'

abstract class UserService {
    static async getUser(username: string) {
        const res = await client.get<User>(`/users/${username}`)
        return res.data
    }

    static async updateUser(request: UpdateUserRequest) {
        await client.put('/users', request, {withAccessToken: true})
    }

    static async updatePassword(request: UpdatePasswordRequest) {
        await client.put('/users/password', request, {withAccessToken: true})
    }

    static async deleteUser(request: DeleteUserRequest) {
        await client.delete('/users', {
            data: request,
            withAccessToken: true,
        })
    }
}

export default UserService
