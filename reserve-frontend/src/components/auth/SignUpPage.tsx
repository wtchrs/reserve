import {useState} from 'react'
import {Avatar, Button, TextField, Link, Grid, Box, Typography, Container} from '@mui/material'
import {LockOutlined} from '@mui/icons-material'
import {useNavigate} from 'react-router-dom'
import {SubmitHandler, useForm} from 'react-hook-form'
import {zodResolver} from '@hookform/resolvers/zod'
import {signUpSchema as schema, SignUpRequest} from '../../schema.ts'
import authService from '../../services/authService.ts'
import ErrorMessages from '../ErrorMessages.tsx'
import {isAxiosError} from 'axios'

function SignUpPage() {
    const navigate = useNavigate()
    const {
        handleSubmit,
        register,
        formState: {errors: fieldErrors, isValid},
    } = useForm<SignUpRequest>({resolver: zodResolver(schema), mode: 'onChange'})

    // Server error or communication error
    const [error, setError] = useState<string>()

    const hasFieldError = (field: string) => field in fieldErrors

    const onSubmit: SubmitHandler<SignUpRequest> = async data => {
        try {
            if (await authService.signUp(data)) {
                navigate('/sign-in')
            }
        } catch (err) {
            if (isAxiosError(err) && err.response && err.response.status === 409) {
                setError('Username is already taken.')
            } else {
                setError('Something went wrong. Please try again later.')
            }
        }
    }

    return (
        <Container maxWidth="xs">
            <Box sx={{marginTop: 8, display: 'flex', flexDirection: 'column', alignItems: 'center'}}>
                <Avatar sx={{m: 1, bgcolor: 'secondary.main'}}>
                    <LockOutlined/>
                </Avatar>
                <Typography component="h1" variant="h5">
                    Sign up
                </Typography>
                <Box component="form" noValidate onSubmit={handleSubmit(onSubmit)} sx={{mt: 3}}>
                    {error && <ErrorMessages errors={error}/>}
                    {Object.keys(fieldErrors).length > 0 && <ErrorMessages errors={fieldErrors}/>}

                    <Grid container spacing={2}>
                        <Grid item xs={12}>
                            <TextField required fullWidth id="username" label="Username" autoComplete="username"
                                       autoFocus {...register('username')}
                                       error={hasFieldError('username')}/>
                        </Grid>
                        <Grid item xs={12}>
                            <TextField required fullWidth id="nickname" label="Nickname"
                                       autoComplete="nickname" {...register('nickname')}
                                       error={hasFieldError('nickname')}/>
                        </Grid>
                        <Grid item xs={12}>
                            <TextField required fullWidth id="password" label="Password" type="password"
                                       autoComplete="new-password" {...register('password')}
                                       error={hasFieldError('password')}/>
                        </Grid>
                        <Grid item xs={12}>
                            <TextField required fullWidth id="passwordConfirmation" label="Password confirmation"
                                       type="password" autoComplete="new-password"
                                       {...register('passwordConfirmation')}
                                       error={hasFieldError('passwordConfirmation')}/>
                        </Grid>
                    </Grid>
                    <Button type="submit" fullWidth variant="contained" sx={{mt: 3, mb: 2}} disabled={!isValid}>
                        Sign Up
                    </Button>
                    <Grid container justifyContent="flex-end">
                        <Grid item>
                            <Link href="/sign-in" variant="body2">
                                Already have an account? Sign in
                            </Link>
                        </Grid>
                    </Grid>
                </Box>
            </Box>
        </Container>
    )
}

export default SignUpPage
