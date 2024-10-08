import {z} from 'zod'

export const signInSchema = z.object({
    username: z.string(),
    password: z.string(),
    remember: z.boolean().optional(),
})

export const signUpSchema = z
    .object({
        username: z.string()
            .min(4, 'Username must be at least 4 characters.')
            .max(25, 'Username must be at most 25 characters.'),
        nickname: z.string()
            .min(2, 'Nickname must be at least 2 characters.')
            .max(30, 'Nickname must be at most 30 characters.'),
        password: z.string()
            .min(8, 'Password must be at least 8 characters.')
            .max(50, 'Password must be at most 50 characters.'),
        passwordConfirmation: z.string(),
    })
    .refine(data => data.password === data.passwordConfirmation, {
        message: 'Passwords do not match.',
        path: ['passwordConfirmation'],
    })

export type SignInRequest = z.infer<typeof signInSchema>
export type SignUpRequest = z.infer<typeof signUpSchema>

export const updateUserSchema = z.object({
    nickname: z.string()
        .min(2, 'Nickname must be at least 2 characters.')
        .max(30, 'Nickname must be at most 30 characters.'),
    description: z.string(),
})

export const updatePasswordSchema = z
    .object({
        oldPassword: z.string(),
        newPassword: z.string()
            .min(8, 'Password must be at least 8 characters.')
            .max(50, 'Password must be at most 50 characters.'),
        confirmation: z.string(),
    })
    .refine(data => data.newPassword === data.confirmation, {
        message: 'Passwords do not match.',
        path: ['confirmation'],
    })

export const deleteUserSchema = z.object({
    password: z.string(),
})

export type UpdateUserRequest = z.infer<typeof updateUserSchema>
export type UpdatePasswordRequest = z.infer<typeof updatePasswordSchema>
export type DeleteUserRequest = z.infer<typeof deleteUserSchema>

export const createStoreSchema = z.object({
    name: z.string().min(1, 'Name must not be empty.'),
    address: z.string().min(1, 'Address must not be empty.'),
    description: z.string().min(1, 'Description must not be empty.'),
})

export const updateStoreSchema = z.object({
    name: z.string().min(1, 'Name must not be empty.').optional(),
    address: z.string().min(1, 'Address must not be empty.').optional(),
    description: z.string().min(1, 'Description must not be empty.').optional(),
})

export const searchStoreSchema = z.object({
    registrant: z.union([z.literal(''), z.string().min(4)]).optional(),
    query: z.union([z.literal(''), z.string().min(2)]).optional(),
})

export type CreateStoreRequest = z.infer<typeof createStoreSchema>
export type UpdateStoreRequest = z.infer<typeof updateStoreSchema>
export type SearchStoreParams = z.infer<typeof searchStoreSchema>
