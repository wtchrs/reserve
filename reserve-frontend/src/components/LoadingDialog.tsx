import {CircularProgress, Dialog, DialogContent} from '@mui/material'

function LoadingDialog() {
    return (
        <Dialog open={true}>
            <DialogContent>
                <CircularProgress sx={{display: 'block', margin: '0 auto'}}/>
            </DialogContent>
        </Dialog>
    )
}

export default LoadingDialog
