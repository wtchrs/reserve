import {
    Avatar,
    Box,
    Button,
    Checkbox,
    Container,
    FormControlLabel,
    Grid,
    Link,
    TextField,
    Typography,
} from '@mui/material'
import LockOutlinedIcon from '@mui/icons-material/LockOutlined'
import {SubmitHandler, useForm} from 'react-hook-form'
import {isAxiosError} from 'axios'
import {zodResolver} from '@hookform/resolvers/zod'
import {useNavigate} from 'react-router-dom'
import {signInSchema as schema, SignInRequest} from '@/schema.ts'
import {useAuth} from '@hooks/useAuth.tsx'
import ErrorMessages from '@components/ErrorMessages.tsx'

function SignInPage() {
    const {auth, signIn} = useAuth()
    const navigate = useNavigate()

    const {
        handleSubmit,
        register,
        setError,
        formState: {errors: error},
    } = useForm<SignInRequest>({resolver: zodResolver(schema)})

    // Already signed in
    if (auth) navigate('/')

    const onSubmit: SubmitHandler<SignInRequest> = async data => {
        try {
            await signIn(data)
            navigate('/')
        } catch (err) {
            if (isAxiosError(err) && err.response?.status === 401) {
                setError('root', {message: 'Invalid username or password.'})
            } else {
                setError('root', {message: 'Something went wrong. Please try again later.'})
            }
        }
    }

    return (
        <Container maxWidth="xs">
            <Box sx={{marginTop: 8, display: 'flex', flexDirection: 'column', alignItems: 'center'}}>
                <Avatar sx={{m: 1, bgcolor: 'secondary.main'}}>
                    <LockOutlinedIcon/>
                </Avatar>
                <Typography component="h1" variant="h5">
                    Sign in
                </Typography>
                <Box component="form" onSubmit={handleSubmit(onSubmit)} noValidate sx={{mt: 1}}>
                    <ErrorMessages errors={error}/>

                    <TextField margin="normal" required fullWidth id="username" label="Username"
                               autoComplete="username" autoFocus {...register('username')}/>
                    <TextField margin="normal" required fullWidth label="Password" type="password"
                               id="password" autoComplete="current-password" {...register('password')}/>
                    <FormControlLabel
                        control={<Checkbox value="remember" color="primary"/>}
                        label="Remember me"
                    />
                    <Button type="submit" fullWidth variant="contained" sx={{mt: 3, mb: 2}}>
                        Sign In
                    </Button>
                    <Grid container>
                        <Grid item xs>
                            <Link href="#" variant="body2">
                                Forgot password?
                            </Link>
                        </Grid>
                        <Grid item>
                            <Link href="/sign-up" variant="body2">
                                {'Don\'t have an account? Sign Up'}
                            </Link>
                        </Grid>
                    </Grid>
                </Box>
            </Box>
        </Container>
    )
}

export default SignInPage
