import {Box, Typography} from '@mui/material'
import type {Store} from '@customTypes/domain'
import StoreDetailSkeleton from './StoreDetailSkeleton'

type Props = {
    store?: Store
}

function StoreDetail({store}: Props) {
    if (!store) {
        return <StoreDetailSkeleton/>
    }

    return (
        <Box>
            <Box ml={3} mb={5}>
                <Typography variant="h3" mb={2}>{store.name}</Typography>
                <Box ml={1}>
                    <Typography variant="body2">{store.registrant}</Typography>
                    <Typography variant="body2">{store.address}</Typography>
                </Box>
            </Box>
            <Typography>{store.description}</Typography>
        </Box>
    )
}

export default StoreDetail
