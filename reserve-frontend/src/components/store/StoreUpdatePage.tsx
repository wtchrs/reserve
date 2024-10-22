import {zodResolver} from '@hookform/resolvers/zod'
import {Box, Button, CircularProgress, Grid, TextField, Typography} from '@mui/material'
import {useEffect, useState} from 'react'
import {useNavigate, useParams} from 'react-router-dom'
import {useForm} from 'react-hook-form'
import {useAuth} from '../../hooks/useAuth.tsx'
import storeService from '../../services/storeService.ts'
import ErrorMessages from '../ErrorMessages.tsx'
import {UpdateStoreRequest, updateStoreSchema} from '../../schema.ts'
import MenuListUpdate from './menu/MenuListUpdate.tsx'
import StoreDeleteDialog from './StoreDeleteDialog.tsx'

function StoreUpdatePage() {
    const navigate = useNavigate()
    const {storeId} = useParams<{ storeId: string }>()
    const {auth} = useAuth()

    const {
        register,
        handleSubmit,
        setError,
        reset,
        formState: {errors: fieldErrors, isValid},
    } = useForm<UpdateStoreRequest>({resolver: zodResolver(updateStoreSchema), mode: 'onChange'})

    const [loading, setLoading] = useState(false)
    const [deleteDialog, setDeleteDialog] = useState(false)

    const hasFieldError = (field: string) => field in fieldErrors

    useEffect(() => {
        if (!storeId) return
        setLoading(true)
        storeService.getStore(storeId)
            .then(res => {
                reset(res)
                setLoading(false)
            })
            .catch(_err => setError('root', {message: 'Something went wrong. Please try again later.'}))
    }, [reset, setError, storeId])

    const onSubmit = async (request: UpdateStoreRequest) => {
        try {
            if (!auth || !storeId) return
            setLoading(true)
            await storeService.update(storeId, request)
            setLoading(false)
            navigate(`/stores/${storeId}`)
        } catch (_err) {
            setError('root', {message: 'Something went wrong. Please try again later.'})
        }
    }

    if (!auth) throw new Response('Unauthorized', {status: 401})
    if (!storeId) throw new Response('Resource Not Found', {status: 404})

    return (
        <Box sx={{mb: 4}}>
            <Button variant="text" sx={{mb: 3, textTransform: 'none'}} onClick={() => navigate(-1)}>
                {'< Go Back'}
            </Button>

            <Box sx={{padding: 4, maxWidth: 800, margin: '0 auto', textAlign: 'center'}}>
                <Typography variant="h4" gutterBottom>Edit Store</Typography>

                {!loading && (
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
                                    Update
                                </Button>
                            </Grid>
                            <Grid item xs={12}>
                                <Button variant="outlined" color="error" fullWidth onClick={() => setDeleteDialog(true)}>
                                    Delete Store
                                </Button>
                            </Grid>
                        </Grid>
                    </Box>
                )}

                <MenuListUpdate storeId={storeId}/>

                {loading && <CircularProgress sx={{display: 'block', margin: '0 auto', mt: 4}}/>}

                <StoreDeleteDialog open={deleteDialog} storeId={storeId} onClose={() => setDeleteDialog(false)}/>
            </Box>
        </Box>
    )
}

export default StoreUpdatePage
