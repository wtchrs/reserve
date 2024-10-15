import client from './api-client.ts'
import {CreateMenuRequest, UpdateMenuRequest} from '../schema.ts'
import {Auth, Menu, MenuListResponse} from '../type.ts'

const basePath = /https?:\/\/[a-zA-Z0-9@:%._+~#=]{2,256}\b(.*)/.exec(import.meta.env.VITE_API_URL)?.[1]

abstract class MenuService {
    static async create({accessToken}: Auth, storeId: string, request: CreateMenuRequest) {
        const res = await client.post(`/stores/${storeId}/menus`, request, {
            headers: {
                Authorization: `Bearer ${accessToken}`,
            },
        })
        if (res.status !== 201) throw new Error('Failed to create menu')
        // Extract menuId
        const location = res.headers['location']
        if (!location) throw new Error('Location header is missing')
        return location.split(basePath + '/menus/')[1]
    }

    static async getMenuListForStore(storeId: string) {
        const res = await client.get<MenuListResponse>(`/stores/${storeId}/menus`)
        return res.data
    }
    
    static async getMenu(menuId: string) {
        const res = await client.get<Menu>(`/menus/${menuId}`)
        return res.data
    }

    static async update({accessToken}: Auth, menuId: string, request: UpdateMenuRequest) {
        await client.put(`/menus/${menuId}`, request, {
            headers: {
                Authorization: `Bearer ${accessToken}`,
            },
        })
    }

    static async delete({accessToken}: Auth, menuId: string) {
        await client.delete(`/menus/${menuId}`, {
            headers: {
                Authorization: `Bearer ${accessToken}`
            }
        })
    }
}

export default MenuService
