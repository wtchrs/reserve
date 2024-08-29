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
