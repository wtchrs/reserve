import {Box, Skeleton} from '@mui/material'

const UserDetailSkeleton = () => {
    return (
        <Box>
            <Skeleton variant="circular" width={120} height={120} sx={{margin: '0 auto', marginBottom: 2}}/>
            <Skeleton variant="text" width={180} height={40} sx={{margin: '0 auto', marginBottom: 1}}/>
            <Skeleton variant="text" width={120} height={30} sx={{margin: '0 auto', marginBottom: 2}}/>
            <Skeleton variant="rectangular" width="100%" height={80} sx={{marginBottom: 2}}/>
            <Skeleton variant="text" width={200} height={20} sx={{margin: '0 auto'}}/>
        </Box>
    )
}

export default UserDetailSkeleton
