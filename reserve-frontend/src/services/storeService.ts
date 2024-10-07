import client from './api-client'
import {SearchStoreParams} from '../schema'
import {ListResponse, PageParams, Store} from '../type'

abstract class StoreService {
    static async search(request: SearchStoreParams, page: PageParams<Store>) {
        const res = await client.get<ListResponse<Store>>('/stores', {
            params: {...request, ...page},
            paramsSerializer: {indexes: null},
        });
        return res.data
    }

    static async getStore(storeId: string) {
        const res = await client.get<Store>(`/stores/${storeId}`)
        return res.data
    }
}

export default StoreService
