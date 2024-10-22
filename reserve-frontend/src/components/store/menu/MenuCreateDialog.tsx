import {useState} from 'react'
import {useForm} from 'react-hook-form'
import {zodResolver} from '@hookform/resolvers/zod'
import {
    Box,
    Button,
    CircularProgress,
    Dialog,
    DialogActions,
    DialogContent,
    DialogTitle,
    Grid,
    TextField,
} from '@mui/material'
import {useAuth} from '../../../hooks/useAuth.tsx'
import {CreateMenuRequest, createMenuSchema} from '../../../schema.ts'
import menuService from '../../../services/menuService.ts'
import ErrorMessages from '../../ErrorMessages.tsx'
import FlashMessageDialog from '../../FlashMessageDialog.tsx'

type Props = {
    storeId: string
    onClose: () => void | Promise<void>
    onCreated: () => void | Promise<void>
}

function MenuCreateDialog({storeId, onClose, onCreated}: Props) {
    const {auth} = useAuth()
    const {
        register,
        handleSubmit,
        reset,
        setError,
        formState: {errors: fieldErrors, isValid},
    } = useForm<CreateMenuRequest>({resolver: zodResolver(createMenuSchema), mode: 'onChange'})
    const [loading, setLoading] = useState(false)
    const [finished, setFinished] = useState(false)

    if (!auth) throw new Response('Unauthorized', {status: 401})

    const hasFieldError = (field: string) => field in fieldErrors

    const onSubmit = async (request: CreateMenuRequest) => {
        try {
            setLoading(true)
            await menuService.create(storeId, request)
            setFinished(true)
        } catch (_err) {
            setError('root', {message: 'Something went wrong. Please try again later.'})
        }
        setLoading(false)
    }

    const handleClose = () => {
        reset()
        setFinished(false)
        onClose()
    }

    const onCloseFlashMessage = async () => {
        handleClose()
        await onCreated()
    }

    return (
        <Dialog open={true} onClose={handleClose}>
            <DialogTitle>New Menu</DialogTitle>

            <Box component="form" noValidate onSubmit={handleSubmit(onSubmit)} hidden={loading}>
                <DialogContent>
                    <ErrorMessages errors={fieldErrors}/>

                    <Grid container spacing={2}>
                        <Grid item xs={12}>
                            <TextField required fullWidth id="name" label="Name" autoFocus error={hasFieldError('name')}
                                       {...register('name')}/>
                        </Grid>
                        <Grid item xs={12}>
                            <TextField required fullWidth id="price" label="Price" type="number"
                                       error={hasFieldError('price')} {...register('price', {valueAsNumber: true})}/>
                        </Grid>
                        <Grid item xs={12}>
                            <TextField required fullWidth id="description" label="Description"
                                       error={hasFieldError('description')} {...register('description')}/>
                        </Grid>
                    </Grid>
                </DialogContent>

                <DialogActions sx={{mx: 2, mb: 2}}>
                    <Grid container spacing={2}>
                        <Grid item xs={6}>
                            <Button variant="outlined" fullWidth onClick={handleClose}>
                                Cancel
                            </Button>
                        </Grid>
                        <Grid item xs={6}>
                            <Button type="submit" variant="contained" fullWidth disabled={!isValid}>
                                Create
                            </Button>
                        </Grid>
                    </Grid>
                </DialogActions>
            </Box>

            {loading && <CircularProgress sx={{display: 'block', margin: '0 auto', mt: 4}}/>}

            <FlashMessageDialog
                open={finished} onClose={onCloseFlashMessage}
                title="Menu Created" message="The menu has been created successfully."
            />
        </Dialog>
    )
}

export default MenuCreateDialog
