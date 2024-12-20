export type AuthUser = {
    userId: string
    username: string
    nickname: string
}

export type Auth = {
    user: AuthUser
    accessToken: string
}

export type User = {
    username: string
    nickname: string
    description: string
    signUpDate: string
}

export type Store = {
    storeId: bigint,
    registrant: string,
    name: string,
    address: string,
    description: string,
}

export type Menu = {
    menuId: bigint,
    storeId: bigint,
    name: string,
    price: number,
    description: string,
}

export type MenuListResponse = {
    count: number,
    results: Menu[],
}

export type ListResponse<T> = {
    count: number,
    pageSize: number,
    pageNumber: number,
    hasNext: boolean,
    results: T[],
}

export type CartItem = {
    menuId: bigint,
    name: string,
    price: number,
    quantity: number,
}

export type Cart = {
    store?: Store
    items: CartItem[]
}

export type Reservation = {
    reservationId: bigint,
    storeId: bigint,
    registrant: string,
    reservationName: string,
    date: Date,
    hour: number,
}

type Direction = 'asc' | 'desc'
// T must have only string keys.
type Key<T> = Extract<keyof T, string>
type PageSort<T> = Key<T> | `${Key<T>},${Direction}`

export type PageParams<T> = {
    page: number,
    size: number,
    sort: PageSort<T>[],
}

export type ErrorResponse = {
    code: number,
    message: string,
}
