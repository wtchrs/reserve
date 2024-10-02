declare module 'axios' {
    interface AxiosRequestConfig {
        urlParams?: Record<string, string>;
    }
}

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

export type ListResponse<T> = {
    count: number,
    pageSize: number,
    pageNumber: number,
    hasNext: boolean,
    results: T[],
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
