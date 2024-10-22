import {Button, Dialog, DialogActions, DialogContent, DialogTitle, Grid, Typography} from '@mui/material'
import {useState} from 'react'
import {useAuth} from '../../../hooks/useAuth.tsx'
import menuService from '../../../services/menuService.ts'
import type {Menu} from '../../../../types/domain.d.ts'
import ErrorMessages from '../../ErrorMessages.tsx'
import FlashMessageDialog from '../../FlashMessageDialog.tsx'
import LoadingDialog from '../../LoadingDialog.tsx'

type Props = {
    menu: Menu
    onClose: () => void | Promise<void>
    onDeleted: () => void | Promise<void>
}

function MenuDeleteDialog({menu, onClose, onDeleted}: Props) {
    const {auth} = useAuth()
    const [error, setError] = useState('')
    const [loading, setLoading] = useState(false)
    const [finished, setFinished] = useState(false)

    const handleDelete = async () => {
        if (!auth) return
        setLoading(true)
        try {
            await menuService.delete(menu.menuId.toString())
            setFinished(true)
        } catch (_err) {
            setError('Something went wrong. Please try again later.')
        }
        setLoading(false)
    }

    const handleClose = async () => {
        setFinished(false)
        await onClose()
    }

    const handleCloseFlash = async () => {
        setFinished(false)
        await onClose()
        await onDeleted()
    }

    return (
        <Dialog open={true} onClose={handleClose}>
            <DialogTitle>Delete Menu</DialogTitle>
            <DialogContent>
                {error && <ErrorMessages errors={error}/>}
                <Typography>Are you sure you want to delete {menu.name}?</Typography>
            </DialogContent>
            <DialogActions sx={{mx: 2, mb: 2}}>
                <Grid container spacing={2}>
                    <Grid item xs={6}>
                        <Button variant="outlined" fullWidth onClick={handleClose}>Cancel</Button>
                    </Grid>
                    <Grid item xs={6}>
                        <Button variant="contained" fullWidth color="error" onClick={handleDelete}>Delete</Button>
                    </Grid>
                </Grid>
            </DialogActions>

            {loading && <LoadingDialog/>}
            {finished && (
              <FlashMessageDialog open={true} onClose={handleCloseFlash} message="Menu deleted successfully"/>
            )}
        </Dialog>
    )
}

export default MenuDeleteDialog
