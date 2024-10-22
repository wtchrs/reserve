import {zodResolver} from '@hookform/resolvers/zod'
import {Box, Button, Grid, TextField, Typography} from '@mui/material'
import {useForm} from 'react-hook-form'
import {useNavigate} from 'react-router-dom'
import {useAuth} from '@hooks/useAuth.tsx'
import {CreateStoreRequest, createStoreSchema} from '@/schema.ts'
import storeService from '@services/storeService.ts'
import ErrorMessages from '@components/ErrorMessages.tsx'

function StoreCreatePage() {
    const navigate = useNavigate()
    const {auth} = useAuth()
    const {
        register,
        handleSubmit,
        setError,
        formState: {errors: fieldErrors, isValid},
    } = useForm<CreateStoreRequest>({resolver: zodResolver(createStoreSchema), mode: 'onChange'})

    if (!auth) {
        throw new Response('Unauthorized', {status: 401})
    }

    const hasFieldError = (field: string) => field in fieldErrors

    const onSubmit = async (data: CreateStoreRequest) => {
        if (!auth) return
        try {
            const storeId = await storeService.create(data)
            navigate('/stores/' + storeId)
        } catch (err) {
            console.log('err', err)
            setError('root', {message: 'Something went wrong. Please try again later.'})
        }
    }

    return (
        <Box sx={{mb: 4}}>
            <Button variant="text" onClick={() => navigate(-1)} sx={{mb: 3, textTransform: 'none'}}>
                {'< Go Back'}
            </Button>
            <Box sx={{padding: 4, maxWidth: 800, margin: '0 auto', textAlign: 'center'}}>
                <Typography variant="h4" gutterBottom>Register Store</Typography>

                <Box maxWidth="xs" mt={5} component="form" noValidate onSubmit={handleSubmit(onSubmit)}>
                    <ErrorMessages errors={fieldErrors}/>

                    <Grid container spacing={2}>
                        <Grid item xs={12}>
                            <TextField fullWidth id="name" label="Name" {...register('name')}
                                       error={hasFieldError('name')}/>
                        </Grid>
                        <Grid item xs={12}>
                            <TextField fullWidth id="address" label="Address" {...register('address')}
                                       error={hasFieldError('address')}/>
                        </Grid>
                        <Grid item xs={12}>
                            <TextField fullWidth multiline id="description"
                                       label="Description" {...register('description')}
                                       error={hasFieldError('description')}/>
                        </Grid>
                        <Grid item xs={6}>
                            <Button variant="outlined" fullWidth onClick={() => navigate(-1)} sx={{mt: 2}}>
                                Cancel
                            </Button>
                        </Grid>
                        <Grid item xs={6}>
                            <Button type="submit" variant="contained" fullWidth sx={{mt: 2}} disabled={!isValid}>
                                Create
                            </Button>
                        </Grid>
                    </Grid>
                </Box>
            </Box>
        </Box>
    )
}

export default StoreCreatePage
