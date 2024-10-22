import {useEffect, useState} from 'react'
import {Avatar, Box, Button, Container, Grid, TextField, Typography} from '@mui/material'
import {useNavigate} from 'react-router-dom'
import {SubmitHandler, useForm} from 'react-hook-form'
import {zodResolver} from '@hookform/resolvers/zod'
import {UpdateUserRequest, updateUserSchema} from '@/schema.ts'
import userService from '@services/userService.ts'
import {useAuth} from '@hooks/useAuth.tsx'
import type {User} from '@customTypes/domain'
import UserUpdateSkeleton from './UserUpdateSkeleton.tsx'
import ErrorMessages from '@components/ErrorMessages.tsx'

function UserUpdatePage() {
    const {auth} = useAuth()
    const navigate = useNavigate()
    const [user, setUser] = useState<User>()
    const {
        handleSubmit,
        register,
        reset,
        setError: setFieldError,
        formState: {errors: fieldErrors, isValid},
    } = useForm<UpdateUserRequest>({resolver: zodResolver(updateUserSchema)})
    const [error, setError] = useState<any>()

    useEffect(() => {
        if (auth) {
            userService.getUser(auth.user.username)
                .then(res => {
                    setUser(res)
                    reset({nickname: res.nickname, description: res.description})
                })
                .catch(err => setError(err))
        }
    }, [auth, navigate, reset])

    if (error) {
        throw error
    }

    if (!auth || !user) {
        return <UserUpdateSkeleton/>
    }

    const onSubmit: SubmitHandler<UpdateUserRequest> = async (data) => {
        try {
            await userService.updateUser(data)
            navigate(`/users/${auth.user.username}`)
        } catch (_err) {
            setFieldError('root', {message: 'Something went wrong. Please try again later.'})
        }
    }

    return (
        <Box>
            <Button variant="text" onClick={() => navigate(-1)} sx={{textTransform: 'none'}}>
                {'< Go Back'}
            </Button>
            <Container maxWidth="sm"
                       sx={{marginTop: 4, display: 'flex', flexDirection: 'column', alignItems: 'center'}}>
                <Avatar sx={{m: 1, bgcolor: 'secondary.main'}}/>
                <Typography component="h1" variant="h5">
                    Update User Information
                </Typography>
                <Box sx={{padding: 4, mt: 3}}>
                    <Box component="form" noValidate onSubmit={handleSubmit(onSubmit)}>
                        <ErrorMessages errors={fieldErrors}/>

                        <Grid container spacing={2}>
                            <Grid item xs={12}>
                                <TextField required fullWidth id="username" label="Username" disabled
                                           value={user.username}/>
                            </Grid>
                            <Grid item xs={12}>
                                <TextField required fullWidth id="nickname" label="Nickname" autoComplete="nickname"
                                           {...register('nickname')} error={!!fieldErrors.nickname}/>
                            </Grid>
                            <Grid item xs={12}>
                                <TextField fullWidth id="description" label="Description" multiline rows={4}
                                           {...register('description')} error={!!fieldErrors.description}/>
                            </Grid>
                        </Grid>
                        <Grid container spacing={2} sx={{marginTop: 3}}>
                            <Grid item xs={6}>
                                <Button fullWidth variant="outlined" onClick={() => navigate(-1)}>
                                    Cancel
                                </Button>
                            </Grid>
                            <Grid item xs={6}>
                                <Button type="submit" fullWidth variant="contained" disabled={!isValid}>
                                    Update
                                </Button>
                            </Grid>
                        </Grid>
                    </Box>
                </Box>
            </Container>
        </Box>
    )
}

export default UserUpdatePage
