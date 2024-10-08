import client from './api-client'
import {CreateStoreRequest, SearchStoreParams, UpdateStoreRequest} from '../schema'
import {Auth, ListResponse, PageParams, Store} from '../type'

const basePath = /https?:\/\/[a-zA-Z0-9@:%._+~#=]{2,256}\b(.*)/.exec(import.meta.env.VITE_API_URL)?.[1]

abstract class StoreService {
    static async create({accessToken}: Auth, request: CreateStoreRequest) {
        const res = await client.post('/stores', request, {
            headers: {
                Authorization: `Bearer ${accessToken}`,
            },
        })
        if (res.status !== 201) throw new Error('Failed to create store')
        // Extract storeId
        const location = res.headers['location']
        if (!location) throw new Error('Location header is missing')
        return location.split(basePath + '/stores/')[1]
    }

    static async getStore(storeId: string) {
        const res = await client.get<Store>(`/stores/${storeId}`)
        return res.data
    }

    static async search(request: SearchStoreParams, page: PageParams<Store>) {
        const res = await client.get<ListResponse<Store>>('/stores', {
            params: {...request, ...page},
            paramsSerializer: {indexes: null},
        })
        return res.data
    }

    static async getStoresByUsername(username: string, page: PageParams<Store>) {
        const res = await client.get<ListResponse<Store>>(`/stores`, {
            params: {registrant: username, ...page},
            paramsSerializer: {indexes: null},
        })
        return res.data
    }

    static async update({accessToken}: Auth, storeId: string, request: UpdateStoreRequest) {
        await client.put(`/stores/${storeId}`, request, {
            headers: {
                Authorization: `Bearer ${accessToken}`,
            },
        })
    }
}

export default StoreService
