import {Skeleton, Paper, Grid} from '@mui/material'

const UserDetailSkeleton = () => {
    return (
        <Paper
            elevation={3}
            sx={{
                padding: 4,
                maxWidth: 800,
                margin: '0 auto',
                textAlign: 'center',
            }}
        >
            <Skeleton variant="circular" width={120} height={120} sx={{margin: '0 auto', marginBottom: 2}}/>
            <Skeleton variant="text" width={180} height={40} sx={{margin: '0 auto', marginBottom: 1}}/>
            <Skeleton variant="text" width={120} height={30} sx={{margin: '0 auto', marginBottom: 2}}/>
            <Skeleton variant="rectangular" width="100%" height={80} sx={{marginBottom: 2}}/>
            <Skeleton variant="text" width={200} height={20} sx={{margin: '0 auto'}}/>
            <Grid container spacing={2} sx={{marginTop: 3}}>
                <Grid item xs={6}>
                    <Skeleton variant="rectangular" width="100%" height={40}/>
                </Grid>
                <Grid item xs={6}>
                    <Skeleton variant="rectangular" width="100%" height={40}/>
                </Grid>
            </Grid>
        </Paper>
    )
}

export default UserDetailSkeleton
