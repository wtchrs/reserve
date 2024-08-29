export type UserId = string

export type User = UserId | {
    userId: string
    username: string
    nickname: string
}

export type Auth = {
    user: User
    accessToken: string
}

