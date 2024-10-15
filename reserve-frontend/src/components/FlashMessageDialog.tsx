import {Button, Dialog, DialogActions, DialogContent, DialogTitle} from '@mui/material'

type Props = {
    open: boolean
    onClose: () => void
    title?: string
    message: string
}

function FlashMessageDialog({open, onClose, title, message}: Props) {
    return (
        <Dialog open={open}>
            {title && <DialogTitle>{title}</DialogTitle>}
            <DialogContent>{message}</DialogContent>
            <DialogActions>
                <Button onClick={onClose}>Close</Button>
            </DialogActions>
        </Dialog>
    )
}

export default FlashMessageDialog
