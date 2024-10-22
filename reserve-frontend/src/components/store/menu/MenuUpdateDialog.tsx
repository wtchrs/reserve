import {zodResolver} from '@hookform/resolvers/zod'
import {Box, Button, Dialog, DialogActions, DialogContent, DialogTitle, Grid, TextField} from '@mui/material'
import {useEffect, useState} from 'react'
import {useForm} from 'react-hook-form'
import {useAuth} from '@hooks/useAuth.tsx'
import {UpdateMenuRequest, updateMenuSchema} from '@/schema.ts'
import menuService from '@services/menuService.ts'
import type {Menu} from '@customTypes/domain'
import ErrorMessages from '@components/ErrorMessages.tsx'
import FlashMessageDialog from '@components/FlashMessageDialog.tsx'
import LoadingDialog from '@components/LoadingDialog.tsx'

type Props = {
    menu: Menu
    onClose: () => void | Promise<void>
    onUpdated: () => void | Promise<void>
}

function MenuUpdateDialog({menu, onClose, onUpdated}: Props) {
    const {auth} = useAuth()
    const [flashOpen, setFlashOpen] = useState(false)
    const [loading, setLoading] = useState(false)
    const {
        register,
        handleSubmit,
        reset,
        setError,
        formState: {errors: fieldErrors, isValid},
    } = useForm<UpdateMenuRequest>({resolver: zodResolver(updateMenuSchema), mode: 'onChange'})

    const hasFieldError = (field: string) => field in fieldErrors

    useEffect(() => {
        reset({name: menu.name, price: menu.price, description: menu.description})
    }, [menu, reset])

    const onSubmit = async (request: UpdateMenuRequest) => {
        if (!auth) return
        setLoading(true)
        try {
            await menuService.update(menu.menuId.toString(), request)
            setFlashOpen(true)
        } catch (_err) {
            setError('root', {message: 'Something went wrong. Please try again later.'})
        }
        setLoading(false)
    }

    const handleClose = async () => {
        setFlashOpen(false)
        await onClose()
    }

    const handleFlashClose = async () => {
        await handleClose()
        await onUpdated()
    }

    return (
        <Dialog open={true} onClose={handleClose}>
            <DialogTitle>Update Menu</DialogTitle>
            <Box component="form" onSubmit={handleSubmit(onSubmit)}>
                <DialogContent>
                    <ErrorMessages errors={fieldErrors}/>
                    <Grid container spacing={2}>
                        <Grid item xs={12}>
                            <TextField fullWidth required id="name" label="Name" error={hasFieldError('name')}
                                       {...register('name')}/>
                        </Grid>
                        <Grid item xs={12}>
                            <TextField fullWidth required id="price" label="Price" type="number"
                                       error={hasFieldError('price')} {...register('price', {valueAsNumber: true})}/>
                        </Grid>
                        <Grid item xs={12}>
                            <TextField fullWidth required id="description" label="Description"
                                       error={hasFieldError('description')} {...register('description')}/>
                        </Grid>
                    </Grid>
                </DialogContent>
                <DialogActions sx={{mx: 2, mb: 2}}>
                    <Grid container spacing={2}>
                        <Grid item xs={6}>
                            <Button variant="outlined" fullWidth onClick={handleClose}>Cancel</Button>
                        </Grid>
                        <Grid item xs={6}>
                            <Button variant="contained" fullWidth type="submit" disabled={!isValid}>Update</Button>
                        </Grid>
                    </Grid>
                </DialogActions>
            </Box>

            {loading && <LoadingDialog/>}
            <FlashMessageDialog open={flashOpen} onClose={handleFlashClose} message="Menu updated successfully"/>
        </Dialog>
    )
}

export default MenuUpdateDialog
