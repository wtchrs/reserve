import {Box, Button, Dialog, DialogContent, DialogTitle, Grid, TextField, Typography} from '@mui/material'
import {useNavigate} from 'react-router-dom'
import {SubmitHandler, useForm} from 'react-hook-form'
import {zodResolver} from '@hookform/resolvers/zod'
import {isAxiosError} from 'axios'
import ErrorMessages from '@components/ErrorMessages'
import {DeleteUserRequest, deleteUserSchema as schema} from '@/schema.ts'
import userService from '@services/userService'
import {useAuth} from '@hooks/useAuth.tsx'

type Props = {
    open: boolean
    onClose: () => void
}

function UserDeleteDialog({open, onClose}: Props) {
    const {auth} = useAuth()
    const navigate = useNavigate()

    const {
        handleSubmit,
        register,
        setError: setFieldError,
        formState: {errors: fieldErrors},
    } = useForm<DeleteUserRequest>({resolver: zodResolver(schema)})

    const hasFieldError = (field: string) => field in fieldErrors

    const onSubmit: SubmitHandler<DeleteUserRequest> = async data => {
        console.log('data', data)
        if (auth) {
            try {
                await userService.deleteUser(data)
                navigate('/')
            } catch (err) {
                if (isAxiosError(err) && err.response?.status === 403) {
                    setFieldError('password', {type: 'manual', message: 'Invalid password.'})
                } else {
                    setFieldError('root', {type: 'manual', message: 'Something went wrong. Please try again later.'})
                }
            }
        }
    }

    return (
        <Dialog open={open} onClose={onClose}>
            <DialogTitle color="error">Delete Account</DialogTitle>
            <DialogContent>
                <Typography>Are you sure you want to delete your account? This action cannot be undone.</Typography>
                <Box component="form" noValidate marginTop={2}>
                    {Object.keys(fieldErrors).length > 0 && <ErrorMessages errors={fieldErrors}/>}
                    <Grid container spacing={2}>
                        <Grid item xs={12}>
                            <TextField required fullWidth id="password" label="Password" type="password"
                                       autoComplete="password" {...register('password')}
                                       error={hasFieldError('password')}/>
                        </Grid>
                        <Grid item xs={12}>
                            <Button fullWidth variant="outlined" color="error" onClick={handleSubmit(onSubmit)}>
                                Delete
                            </Button>
                        </Grid>
                    </Grid>
                </Box>
            </DialogContent>
        </Dialog>
    )
}

export default UserDeleteDialog
