import {Avatar, Box, Button, Container, Grid, TextField, Typography} from '@mui/material'
import {LockOutlined} from '@mui/icons-material'
import {SubmitHandler, useForm} from 'react-hook-form'
import {zodResolver} from '@hookform/resolvers/zod'
import {isAxiosError} from 'axios'
import {useNavigate} from 'react-router-dom'
import ErrorMessages from '../ErrorMessages'
import {UpdatePasswordRequest, updatePasswordSchema as schema} from '../../schema'
import userService from '../../services/userService'
import {useAuth} from '../../hooks/useAuth'

function PasswordUpdatePage() {
    const {auth} = useAuth()
    const navigate = useNavigate()

    const {
        handleSubmit,
        register,
        setError,
        formState: {errors: fieldErrors, isValid},
    } = useForm<UpdatePasswordRequest>({resolver: zodResolver(schema), mode: 'onChange'})

    const hasFieldError = (field: string) => field in fieldErrors

    const onSubmit: SubmitHandler<UpdatePasswordRequest> = async data => {
        if (!auth) {
            navigate('/sign-in')
            return
        }
        try {
            await userService.updatePassword(data)
            navigate('/')
        } catch (err) {
            if (isAxiosError(err) && err.response && err.response.status === 403) {
                setError('oldPassword', {message:'Password is incorrect.'})
            } else {
                setError('root', {message:'Something went wrong. Please try again later.'})
            }
        }
    }

    return (
        <Box>
            <Button variant="text" onClick={() => navigate(-1)} sx={{marginBottom: 3}}>
                {'< Go Back'}
            </Button>
            <Container maxWidth="xs"
                       sx={{marginTop: 8, display: 'flex', flexDirection: 'column', alignItems: 'center'}}>
                <Avatar sx={{m: 1, bgcolor: 'secondary.main'}}>
                    <LockOutlined/>
                </Avatar>
                <Typography component="h1" variant="h5">
                    Sign up
                </Typography>
                <Box component="form" noValidate onSubmit={handleSubmit(onSubmit)} sx={{mt: 3}}>
                    <ErrorMessages errors={fieldErrors}/>

                    <Grid container spacing={2}>
                        <Grid item xs={12}>
                            <TextField required fullWidth id="oldPassword" label="Password" type="password"
                                       autoComplete="password" {...register('oldPassword')}
                                       error={hasFieldError('oldPassword')}/>
                        </Grid>
                        <Grid item xs={12}>
                            <TextField required fullWidth id="newPassword" label="New password" type="password"
                                       autoComplete="new-password" {...register('newPassword')}
                                       error={hasFieldError('newPassword')}/>
                        </Grid>
                        <Grid item xs={12}>
                            <TextField required fullWidth id="confirmation" label="Confirmation"
                                       type="password" autoComplete="new-password"
                                       {...register('confirmation')}
                                       error={hasFieldError('confirmation')}/>
                        </Grid>
                    </Grid>
                    <Grid container spacing={2} sx={{marginTop:3}}>
                        <Grid item xs={6}>
                            <Button fullWidth variant="outlined" onClick={() => navigate(-1)} sx={{marginRight: 1}}>
                                Cancel
                            </Button>
                        </Grid>
                        <Grid item xs={6}>
                            <Button type="submit" fullWidth variant="contained" disabled={!isValid}>
                                Change password
                            </Button>
                        </Grid>
                    </Grid>
                </Box>
            </Container>
        </Box>
    )
}

export default PasswordUpdatePage
