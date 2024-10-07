import {Box, Skeleton} from '@mui/material'

function StoreDetailSkeleton() {
    return (
        <Box>
            <Box ml={3} mb={5}>
                <Skeleton variant="text" width={200} height={40}/>
                <Box ml={1}>
                    <Skeleton variant="text" width={100} height={20}/>
                    <Skeleton variant="text" width={150} height={20}/>
                </Box>
            </Box>
            <Skeleton variant="text" width="100%" height={20}/>
        </Box>
    )
}

export default StoreDetailSkeleton
