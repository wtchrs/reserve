import {Button, Dialog, DialogActions, DialogContent, DialogTitle, Grid, Typography} from '@mui/material'
import {useState} from 'react'
import {useNavigate} from 'react-router-dom'
import {useAuth} from '../../hooks/useAuth.tsx'
import storeService from '../../services/storeService.ts'
import ErrorMessages from '../ErrorMessages.tsx'

type Props = {
    open: boolean
    storeId: string
    onClose: () => void
}

function StoreDeleteDialog({open, storeId, onClose}: Props) {
    const navigate = useNavigate()
    const {auth} = useAuth()
    const [error, setError] = useState('')

    if (!auth) throw new Response('Unauthorized', {status: 401})

    const handleDelete = async () => {
        try {
            await storeService.delete(auth, storeId)
            navigate('/stores')
        } catch (_err) {
            setError('Something went wrong. Please try again later.')
        }
    }

    return (
        <Dialog open={open} onClose={onClose}>
            <DialogTitle color="error">Delete Store</DialogTitle>
            <DialogContent>
                <Typography>
                    Are you sure you want to delete this store? This action cannot be undone.
                </Typography>
            </DialogContent>
            <DialogActions sx={{mx: 2, mb: 2}}>
                <Grid container spacing={2}>
                    {error && (
                        <Grid item xs={12}>
                            <ErrorMessages errors={error}/>
                        </Grid>
                    )}
                    <Grid item xs={6}>
                        <Button variant="outlined" fullWidth onClick={onClose}>
                            Cancel
                        </Button>
                    </Grid>
                    <Grid item xs={6}>
                        <Button variant="outlined" fullWidth color="error" onClick={handleDelete}>
                            Delete Store
                        </Button>
                    </Grid>
                </Grid>
            </DialogActions>
        </Dialog>
    )
}

export default StoreDeleteDialog
