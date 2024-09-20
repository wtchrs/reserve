import {Box, Grid, Skeleton} from '@mui/material'

function UserUpdateSkeleton() {
    return (
        <Box sx={{padding: 4, maxWidth: 600, margin: '0 auto', textAlign: 'center'}}>
            <Skeleton variant="circular" width={40} height={40} sx={{margin: '0 auto', marginBottom: 2}}/>
            <Skeleton variant="text" width={150} height={40} sx={{margin: '0 auto', marginBottom: 3}}/>

            <Grid container spacing={2}>
                <Grid item xs={12}>
                    <Skeleton variant="rectangular" width="100%" height={56} sx={{marginBottom: 2}}/>
                </Grid>
                <Grid item xs={12}>
                    <Skeleton variant="rectangular" width="100%" height={56} sx={{marginBottom: 2}}/>
                </Grid>
                <Grid item xs={12}>
                    <Skeleton variant="rectangular" width="100%" height={112} sx={{marginBottom: 2}}/>
                </Grid>
            </Grid>

            <Grid container spacing={2} sx={{marginTop: 3}}>
                <Grid item xs={6}>
                    <Skeleton variant="rectangular" width="100%" height={40}/>
                </Grid>
                <Grid item xs={6}>
                    <Skeleton variant="rectangular" width="100%" height={40}/>
                </Grid>
            </Grid>
        </Box>

    )
}

export default UserUpdateSkeleton
