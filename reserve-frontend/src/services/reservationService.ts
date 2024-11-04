import {CreateReservationRequest, SearchReservationParams, UpdateReservationRequest} from '@/schema.ts'
import client from './api-client.ts'
import {ListResponse, PageParams, Reservation} from '@customTypes/domain.js'

const basePath = /https?:\/\/[a-zA-Z0-9@:%._+~#=]{2,256}\b(.*)/.exec(import.meta.env.VITE_API_URL)?.[1]

abstract class ReservationService {
    static async create(request: CreateReservationRequest) {
        const res = await client.post('/reservations', request, {withAccessToken: true})
        if (res.status !== 201) throw new Error('Failed to create menu')
        // Extract menuId
        const location = res.headers['location']
        if (!location) throw new Error('Location header is missing')
        return location.split(basePath + '/reservations/')[1]
    }

    static async search(request: SearchReservationParams, page: PageParams<Reservation>) {
        const res = await client.get<ListResponse<Reservation>>('/reservations', {
            params: {...request, ...page},
            withAccessToken: true,
        })
        return res.data
    }

    static async getReservation(reservationId: string) {
        const res = await client.get<Reservation>(`/reservations/${reservationId}`, {withAccessToken: true})
        return res.data
    }

    static async update(reservationId: string, request: UpdateReservationRequest) {
        await client.put(`/reservations/${reservationId}`, request, {withAccessToken: true})
    }

    static async delete(reservationId: string) {
        await client.delete(`/reservations/${reservationId}`, {withAccessToken: true})
    }
}

export default ReservationService
