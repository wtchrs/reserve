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

export const createMenuSchema = z.object({
    name: z.string().min(1, 'Name must not be empty.'),
    price: z.number().int().min(0, 'Price must be at least 0.'),
    description: z.string().min(1, 'Description must not be empty.'),
})

export const updateMenuSchema = z.object({
    name: z.string().min(1, 'Name must not be empty.').optional(),
    price: z.number().int().min(0, 'Price must be at least 0.').optional(),
    description: z.string().min(1, 'Description must not be empty.').optional(),
})

export type CreateMenuRequest = z.infer<typeof createMenuSchema>
export type UpdateMenuRequest = z.infer<typeof updateMenuSchema>

export const createReservationMenuSchema = z.object({
    menuId: z.number().or(z.bigint()),
    quantity: z.number().int().min(1, 'Quantity must be at least 1.'),
})

export const createReservationSchema = z.object({
    storeId: z.number().or(z.bigint()),
    date: z.date().min(new Date(Date.now()), 'Date must be in the future.'),
    hour: z.number().int()
        .min(0, 'Hour must be between 0 and 23.')
        .max(23, 'Hour must be between 0 and 23.'),
    menus: z.array(createReservationMenuSchema),
})

export const searchReservationSchema = z.object({
    type: z.union([z.literal('REGISTRANT'), z.literal('CUSTOMER')]),
    query: z.string().min(1).optional(),
    date: z.date().optional(),
})

export const updateReservationSchema = z.object({
    date: z.date().min(new Date(Date.now()), 'Date must be in the future.').optional(),
    hour: z.number().int()
        .min(0, 'Hour must be between 0 and 23.')
        .max(23, 'Hour must be between 0 and 23.')
        .optional(),
})

export type CreateReservationMenuRequest = z.infer<typeof createReservationMenuSchema>
export type CreateReservationRequest = z.infer<typeof createReservationSchema>
export type SearchReservationParams = z.infer<typeof searchReservationSchema>
export type UpdateReservationRequest = z.infer<typeof updateReservationSchema>
