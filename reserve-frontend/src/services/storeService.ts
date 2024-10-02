import client from './api-client'
import {SearchStoreParams} from '../schema'
import {ListResponse, PageParams, Store} from '../type'

abstract class StoreService {
    static async search(request: SearchStoreParams, page: PageParams<Store>) {
        return await client.get<ListResponse<Store>>('/stores', {
            params: {...request, ...page},
            paramsSerializer: {indexes: null},
        })
    }
}

export default StoreService
