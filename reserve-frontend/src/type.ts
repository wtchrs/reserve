export type User = {
    userId: string
    username: string
    nickname: string
}

export type Auth = {
    user: User
    accessToken: string
}

